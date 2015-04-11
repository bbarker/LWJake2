/*
 * Copyright (C) 1997-2001 Id Software, Inc.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package lwjake2.sys

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.netadr_t
import lwjake2.qcommon.sizebuf_t
import lwjake2.util.Lib

import java.io.IOException
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

public class NET {

    public class loopmsg_t {
        var data = ByteArray(Defines.MAX_MSGLEN)

        var datalen: Int = 0
    }

    public class loopback_t {
        {
            msgs = arrayOfNulls<loopmsg_t>(MAX_LOOPBACK)
            for (n in 0..MAX_LOOPBACK - 1) {
                msgs[n] = loopmsg_t()
            }
        }

        var msgs: Array<loopmsg_t>

        var get: Int = 0
        var send: Int = 0
    }

    companion object {

        private val MAX_LOOPBACK = 4

        /** Local loopback adress.  */
        private val net_local_adr = netadr_t()

        public var loopbacks: Array<loopback_t> = arrayOfNulls(2)
        {
            loopbacks[0] = loopback_t()
            loopbacks[1] = loopback_t()
        }

        private val ip_channels = array<DatagramChannel>(null, null)

        private val ip_sockets = array<DatagramSocket>(null, null)

        /**
         * Compares ip address and port.
         */
        public fun CompareAdr(a: netadr_t, b: netadr_t): Boolean {
            return (a.ip[0] == b.ip[0] && a.ip[1] == b.ip[1] && a.ip[2] == b.ip[2] && a.ip[3] == b.ip[3] && a.port == b.port)
        }

        /**
         * Compares ip address without the port.
         */
        public fun CompareBaseAdr(a: netadr_t, b: netadr_t): Boolean {
            if (a.type != b.type)
                return false

            if (a.type == Defines.NA_LOOPBACK)
                return true

            if (a.type == Defines.NA_IP) {
                return (a.ip[0] == b.ip[0] && a.ip[1] == b.ip[1] && a.ip[2] == b.ip[2] && a.ip[3] == b.ip[3])
            }
            return false
        }

        /**
         * Returns a string holding ip address and port like "ip0.ip1.ip2.ip3:port".
         */
        public fun AdrToString(a: netadr_t): String {
            val sb = StringBuffer()
            sb.append(a.ip[0] and 255).append('.').append(a.ip[1] and 255)
            sb.append('.')
            sb.append(a.ip[2] and 255).append('.').append(a.ip[3] and 255)
            sb.append(':').append(a.port)
            return sb.toString()
        }

        /**
         * Returns IP address without the port as string.
         */
        public fun BaseAdrToString(a: netadr_t): String {
            val sb = StringBuffer()
            sb.append(a.ip[0] and 255).append('.').append(a.ip[1] and 255)
            sb.append('.')
            sb.append(a.ip[2] and 255).append('.').append(a.ip[3] and 255)
            return sb.toString()
        }

        /**
         * Creates an netadr_t from an string.
         */
        public fun StringToAdr(s: String, a: netadr_t): Boolean {
            if (s.equalsIgnoreCase("localhost") || s.equalsIgnoreCase("loopback")) {
                a.set(net_local_adr)
                return true
            }
            try {
                val address = s.split(":")
                val ia = InetAddress.getByName(address[0])
                a.ip = ia.getAddress()
                a.type = Defines.NA_IP
                if (address.size() == 2)
                    a.port = Lib.atoi(address[1])
                return true
            } catch (e: Exception) {
                Com.Println(e.getMessage())
                return false
            }

        }

        /**
         * Seems to return true, if the address is is on 127.0.0.1.
         */
        public fun IsLocalAddress(adr: netadr_t): Boolean {
            return CompareAdr(adr, net_local_adr)
        }

        /*
     * ==================================================
     * 
     * LOOPBACK BUFFERS FOR LOCAL PLAYER
     * 
     * ==================================================
     */

        /**
         * Gets a packet from internal loopback.
         */
        public fun GetLoopPacket(sock: Int, net_from: netadr_t, net_message: sizebuf_t): Boolean {
            val loop: loopback_t
            loop = loopbacks[sock]

            if (loop.send - loop.get > MAX_LOOPBACK)
                loop.get = loop.send - MAX_LOOPBACK

            if (loop.get >= loop.send)
                return false

            val i = loop.get and (MAX_LOOPBACK - 1)
            loop.get++

            System.arraycopy(loop.msgs[i].data, 0, net_message.data, 0, loop.msgs[i].datalen)
            net_message.cursize = loop.msgs[i].datalen

            net_from.set(net_local_adr)
            return true
        }

        /**
         * Sends a packet via internal loopback.
         */
        public fun SendLoopPacket(sock: Int, length: Int, data: ByteArray, to: netadr_t) {
            val i: Int
            val loop: loopback_t

            loop = loopbacks[sock xor 1]

            // modulo 4
            i = loop.send and (MAX_LOOPBACK - 1)
            loop.send++

            System.arraycopy(data, 0, loop.msgs[i].data, 0, length)
            loop.msgs[i].datalen = length
        }

        /**
         * Gets a packet from a network channel
         */
        public fun GetPacket(sock: Int, net_from: netadr_t, net_message: sizebuf_t): Boolean {

            if (GetLoopPacket(sock, net_from, net_message)) {
                return true
            }

            if (ip_sockets[sock] == null)
                return false

            try {
                val receiveBuffer = ByteBuffer.wrap(net_message.data)

                val srcSocket = ip_channels[sock].receive(receiveBuffer) as InetSocketAddress
                if (srcSocket == null)
                    return false

                net_from.ip = srcSocket!!.getAddress().getAddress()
                net_from.port = srcSocket!!.getPort()
                net_from.type = Defines.NA_IP

                val packetLength = receiveBuffer.position()

                if (packetLength > net_message.maxsize) {
                    Com.Println("Oversize packet from " + AdrToString(net_from))
                    return false
                }

                // set the size
                net_message.cursize = packetLength
                // set the sentinel
                net_message.data[packetLength] = 0
                return true

            } catch (e: IOException) {
                Com.DPrintf("NET_GetPacket: " + e + " from " + AdrToString(net_from) + "\n")
                return false
            }

        }

        /**
         * Sends a Packet.
         */
        public fun SendPacket(sock: Int, length: Int, data: ByteArray, to: netadr_t) {
            if (to.type == Defines.NA_LOOPBACK) {
                SendLoopPacket(sock, length, data, to)
                return
            }

            if (ip_sockets[sock] == null)
                return

            if (to.type != Defines.NA_BROADCAST && to.type != Defines.NA_IP) {
                Com.Error(Defines.ERR_FATAL, "NET_SendPacket: bad address type")
                return
            }

            try {
                val dstSocket = InetSocketAddress(to.getInetAddress(), to.port)
                ip_channels[sock].send(ByteBuffer.wrap(data, 0, length), dstSocket)
            } catch (e: Exception) {
                Com.Println("NET_SendPacket ERROR: " + e + " to " + AdrToString(to))
            }

        }

        /**
         * OpenIP, creates the network sockets.
         */
        public fun OpenIP() {
            val port: cvar_t
            val ip: cvar_t
            val clientport: cvar_t

            port = Cvar.Get("port", "" + Defines.PORT_SERVER, Defines.CVAR_NOSET)
            ip = Cvar.Get("ip", "localhost", Defines.CVAR_NOSET)
            clientport = Cvar.Get("clientport", "" + Defines.PORT_CLIENT, Defines.CVAR_NOSET)

            if (ip_sockets[Defines.NS_SERVER] == null)
                ip_sockets[Defines.NS_SERVER] = Socket(Defines.NS_SERVER, ip.string, port.value as Int)

            if (ip_sockets[Defines.NS_CLIENT] == null)
                ip_sockets[Defines.NS_CLIENT] = Socket(Defines.NS_CLIENT, ip.string, clientport.value as Int)
            if (ip_sockets[Defines.NS_CLIENT] == null)
                ip_sockets[Defines.NS_CLIENT] = Socket(Defines.NS_CLIENT, ip.string, Defines.PORT_ANY)
        }

        /**
         * Config multi or singlepalyer - A single player game will only use the loopback code.
         */
        public fun Config(multiplayer: Boolean) {
            if (!multiplayer) {
                // shut down any existing sockets
                for (i in 0..2 - 1) {
                    if (ip_sockets[i] != null) {
                        ip_sockets[i].close()
                        ip_sockets[i] = null
                    }
                }
            } else {
                // open sockets
                OpenIP()
            }
        }

        /**
         * Init
         */
        public fun Init() {
            // nothing to do
        }

        /*
     * Socket
     */
        public fun Socket(sock: Int, ip: String?, port: Int): DatagramSocket {

            var newsocket: DatagramSocket? = null
            try {
                if (ip_channels[sock] == null || !ip_channels[sock].isOpen())
                    ip_channels[sock] = DatagramChannel.open()

                if (ip == null || ip.length() == 0 || ip.equals("localhost")) {
                    if (port == Defines.PORT_ANY) {
                        newsocket = ip_channels[sock].socket()
                        newsocket!!.bind(InetSocketAddress(0))
                    } else {
                        newsocket = ip_channels[sock].socket()
                        newsocket!!.bind(InetSocketAddress(port))
                    }
                } else {
                    val ia = InetAddress.getByName(ip)
                    newsocket = ip_channels[sock].socket()
                    newsocket!!.bind(InetSocketAddress(ia, port))
                }

                // nonblocking channel
                ip_channels[sock].configureBlocking(false)
                // the socket have to be broadcastable
                newsocket!!.setBroadcast(true)
            } catch (e: Exception) {
                Com.Println("Error: " + e.toString())
                newsocket = null
            }

            return newsocket
        }

        /**
         * Shutdown - closes the sockets
         */
        public fun Shutdown() {
            // close sockets
            Config(false)
        }

        /** Sleeps msec or until net socket is ready.  */
        public fun Sleep(msec: Int) {
            if (ip_sockets[Defines.NS_SERVER] == null || (Globals.dedicated != null && Globals.dedicated.value == 0))
                return  // we're not a server, just run full speed

            try {
                //TODO: check for timeout
                Thread.sleep(msec)
            } catch (e: InterruptedException) {
            }

            //ip_sockets[NS_SERVER].

            // this should wait up to 100ms until a packet
            /*
         * struct timeval timeout; 
         * fd_set fdset; 
         * extern cvar_t *dedicated;
         * extern qboolean stdin_active;
         * 
         * if (!ip_sockets[NS_SERVER] || (dedicated && !dedicated.value))
         * 		return; // we're not a server, just run full speed
         * 
         * FD_ZERO(&fdset);
         *  
         * if (stdin_active) 
         * 		FD_SET(0, &fdset); // stdin is processed too 
         * 
         * FD_SET(ip_sockets[NS_SERVER], &fdset); // network socket 
         * 
         * timeout.tv_sec = msec/1000; 
         * timeout.tv_usec = (msec%1000)*1000; 
         * 
         * select(ip_sockets[NS_SERVER]+1, &fdset, NULL, NULL, &timeout);
         */
        }
    }
}