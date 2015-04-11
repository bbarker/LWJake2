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

package lwjake2.server

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.GameBase
import lwjake2.game.Info
import lwjake2.game.PlayerClient
import lwjake2.game.cvar_t
import lwjake2.game.edict_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.Netchan
import lwjake2.qcommon.SZ
import lwjake2.qcommon.netadr_t
import lwjake2.sys.NET
import lwjake2.sys.Timer
import lwjake2.util.Lib

import java.io.IOException

open public class SV_MAIN {
    companion object {

        /** Address of group servers.  */
        public var master_adr: Array<netadr_t> = arrayOfNulls<netadr_t>(Defines.MAX_MASTERS)


        {
            for (i in 0..Defines.MAX_MASTERS - 1) {
                master_adr[i] = netadr_t()
            }
        }

        public var sv_client: client_t // current client

        public var sv_paused: cvar_t

        public var sv_timedemo: cvar_t

        public var sv_enforcetime: cvar_t

        public var timeout: cvar_t // seconds without any message

        public var zombietime: cvar_t // seconds to sink messages after
        // disconnect

        public var rcon_password: cvar_t // password for remote server commands

        public var allow_download: cvar_t

        public var allow_download_players: cvar_t

        public var allow_download_models: cvar_t

        public var allow_download_sounds: cvar_t

        public var allow_download_maps: cvar_t

        public var sv_airaccelerate: cvar_t

        public var sv_noreload: cvar_t // don't reload level state when
        // reentering

        public var maxclients: cvar_t // FIXME: rename sv_maxclients

        public var sv_showclamp: cvar_t

        public var hostname: cvar_t

        public var public_server: cvar_t? = null // should heartbeats be sent

        public var sv_reconnect_limit: cvar_t // minimum seconds between connect
        // messages

        /**
         * Send a message to the master every few minutes to let it know we are
         * alive, and log information.
         */
        public val HEARTBEAT_SECONDS: Int = 300

        /**
         * Called when the player is totally leaving the server, either willingly or
         * unwillingly. This is NOT called if the entire server is quiting or
         * crashing.
         */
        public fun SV_DropClient(drop: client_t) {
            // add the disconnect
            MSG.WriteByte(drop.netchan.message, Defines.svc_disconnect)

            if (drop.state == Defines.cs_spawned) {
                // call the prog function for removing a client
                // this will remove the body, among other things
                PlayerClient.ClientDisconnect(drop.edict)
            }

            if (drop.download != null) {
                FS.FreeFile(drop.download)
                drop.download = null
            }

            drop.state = Defines.cs_zombie // become free in a few seconds
            drop.name = ""
        }


        /* ==============================================================================
     * 
     * CONNECTIONLESS COMMANDS
     * 
     * ==============================================================================*/

        /**
         * Builds the string that is sent as heartbeats and status replies.
         */
        public fun SV_StatusString(): String {
            var player: String
            var status = ""
            var i: Int
            var cl: client_t
            var statusLength: Int
            var playerLength: Int

            status = Cvar.Serverinfo() + "\n"

            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state == Defines.cs_connected || cl.state == Defines.cs_spawned) {
                        player = "" + cl.edict.client.ps.stats[Defines.STAT_FRAGS] + " " + cl.ping + "\"" + cl.name + "\"\n"

                        playerLength = player.length()
                        statusLength = status.length()

                        if (statusLength + playerLength >= 1024)
                            break // can't hold any more

                        status += player
                    }
                    i++
                }
            }

            return status
        }

        /**
         * Responds with all the info that qplug or qspy can see
         */
        public fun SVC_Status() {
            Netchan.OutOfBandPrint(Defines.NS_SERVER, Globals.net_from, "print\n" + SV_StatusString())
        }

        /**
         * SVC_Ack
         */
        public fun SVC_Ack() {
            Com.Printf("Ping acknowledge from " + NET.AdrToString(Globals.net_from) + "\n")
        }

        /**
         * SVC_Info, responds with short info for broadcast scans The second parameter should
         * be the current protocol version number.
         */
        public fun SVC_Info() {
            val string: String
            var i: Int
            var count: Int
            val version: Int

            if (SV_MAIN.maxclients.value == 1)
                return  // ignore in single player

            version = Lib.atoi(Cmd.Argv(1))

            if (version != Defines.PROTOCOL_VERSION)
                string = SV_MAIN.hostname.string + ": wrong version\n"
            else {
                count = 0
                run {
                    i = 0
                    while (i < SV_MAIN.maxclients.value) {
                        if (SV_INIT.svs.clients[i].state >= Defines.cs_connected)
                            count++
                        i++
                    }
                }

                string = SV_MAIN.hostname.string + " " + SV_INIT.sv.name + " " + count + "/" + SV_MAIN.maxclients.value as Int + "\n"
            }

            Netchan.OutOfBandPrint(Defines.NS_SERVER, Globals.net_from, "info\n" + string)
        }

        /**
         * SVC_Ping, Just responds with an acknowledgement.
         */
        public fun SVC_Ping() {
            Netchan.OutOfBandPrint(Defines.NS_SERVER, Globals.net_from, "ack")
        }

        /**
         * Returns a challenge number that can be used in a subsequent
         * client_connect command. We do this to prevent denial of service attacks
         * that flood the server with invalid connection IPs. With a challenge, they
         * must give a valid IP address.
         */
        public fun SVC_GetChallenge() {
            var i: Int
            var oldest: Int
            var oldestTime: Int

            oldest = 0
            oldestTime = 2147483647

            // see if we already have a challenge for this ip
            run {
                i = 0
                while (i < Defines.MAX_CHALLENGES) {
                    if (NET.CompareBaseAdr(Globals.net_from, SV_INIT.svs.challenges[i].adr))
                        break
                    if (SV_INIT.svs.challenges[i].time < oldestTime) {
                        oldestTime = SV_INIT.svs.challenges[i].time
                        oldest = i
                    }
                    i++
                }
            }

            if (i == Defines.MAX_CHALLENGES) {
                // overwrite the oldest
                SV_INIT.svs.challenges[oldest].challenge = Lib.rand() and 32767
                SV_INIT.svs.challenges[oldest].adr = Globals.net_from
                SV_INIT.svs.challenges[oldest].time = Globals.curtime as Int
                i = oldest
            }

            // send it back
            Netchan.OutOfBandPrint(Defines.NS_SERVER, Globals.net_from, "challenge " + SV_INIT.svs.challenges[i].challenge)
        }

        /**
         * A connection request that did not come from the master.
         */
        public fun SVC_DirectConnect() {
            var userinfo: String
            val adr: netadr_t
            var i: Int
            var cl: client_t

            val version: Int
            val qport: Int

            adr = Globals.net_from

            Com.DPrintf("SVC_DirectConnect ()\n")

            version = Lib.atoi(Cmd.Argv(1))
            if (version != Defines.PROTOCOL_VERSION) {
                Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nServer is version " + Globals.VERSION + "\n")
                Com.DPrintf("    rejected connect from version " + version + "\n")
                return
            }

            qport = Lib.atoi(Cmd.Argv(2))
            val challenge = Lib.atoi(Cmd.Argv(3))
            userinfo = Cmd.Argv(4)

            // force the IP key/value pair so the game can filter based on ip
            userinfo = Info.Info_SetValueForKey(userinfo, "ip", NET.AdrToString(Globals.net_from))

            // attractloop servers are ONLY for local clients
            if (SV_INIT.sv.attractloop) {
                if (!NET.IsLocalAddress(adr)) {
                    Com.Printf("Remote connect in attract loop.  Ignored.\n")
                    Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nConnection refused.\n")
                    return
                }
            }

            // see if the challenge is valid
            if (!NET.IsLocalAddress(adr)) {
                run {
                    i = 0
                    while (i < Defines.MAX_CHALLENGES) {
                        if (NET.CompareBaseAdr(Globals.net_from, SV_INIT.svs.challenges[i].adr)) {
                            if (challenge == SV_INIT.svs.challenges[i].challenge)
                                break // good
                            Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nBad challenge.\n")
                            return
                        }
                        i++
                    }
                }
                if (i == Defines.MAX_CHALLENGES) {
                    Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nNo challenge for address.\n")
                    return
                }
            }

            // if there is already a slot for this ip, reuse it
            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]

                    if (cl.state == Defines.cs_free)
                        continue
                    if (NET.CompareBaseAdr(adr, cl.netchan.remote_address) && (cl.netchan.qport == qport || adr.port == cl.netchan.remote_address.port)) {
                        if (!NET.IsLocalAddress(adr) && (SV_INIT.svs.realtime - cl.lastconnect) < (SV_MAIN.sv_reconnect_limit.value as Int * 1000)) {
                            Com.DPrintf(NET.AdrToString(adr) + ":reconnect rejected : too soon\n")
                            return
                        }
                        Com.Printf(NET.AdrToString(adr) + ":reconnect\n")

                        gotnewcl(i, challenge, userinfo, adr, qport)
                        return
                    }
                    i++
                }
            }

            // find a client slot
            //newcl = null;
            var index = -1
            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state == Defines.cs_free) {
                        index = i
                        break
                    }
                    i++
                }
            }
            if (index == -1) {
                Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nServer is full.\n")
                Com.DPrintf("Rejected a connection.\n")
                return
            }
            gotnewcl(index, challenge, userinfo, adr, qport)
        }

        /**
         * Initializes player structures after successfull connection.
         */
        public fun gotnewcl(i: Int, challenge: Int, userinfo: String, adr: netadr_t, qport: Int) {
            // build a new connection
            // accept the new client
            // this is the only place a client_t is ever initialized

            SV_MAIN.sv_client = SV_INIT.svs.clients[i]

            val edictnum = i + 1

            val ent = GameBase.g_edicts[edictnum]
            SV_INIT.svs.clients[i].edict = ent

            // save challenge for checksumming
            SV_INIT.svs.clients[i].challenge = challenge



            // get the game a chance to reject this connection or modify the
            // userinfo
            if (!(PlayerClient.ClientConnect(ent, userinfo))) {
                if (Info.Info_ValueForKey(userinfo, "rejmsg") != null)
                    Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\n" + Info.Info_ValueForKey(userinfo, "rejmsg") + "\nConnection refused.\n")
                else
                    Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "print\nConnection refused.\n")
                Com.DPrintf("Game rejected a connection.\n")
                return
            }

            // parse some info from the info strings
            SV_INIT.svs.clients[i].userinfo = userinfo
            SV_UserinfoChanged(SV_INIT.svs.clients[i])

            // send the connect packet to the client
            Netchan.OutOfBandPrint(Defines.NS_SERVER, adr, "client_connect")

            Netchan.Setup(Defines.NS_SERVER, SV_INIT.svs.clients[i].netchan, adr, qport)

            SV_INIT.svs.clients[i].state = Defines.cs_connected

            SZ.Init(SV_INIT.svs.clients[i].datagram, SV_INIT.svs.clients[i].datagram_buf, SV_INIT.svs.clients[i].datagram_buf.length)

            SV_INIT.svs.clients[i].datagram.allowoverflow = true
            SV_INIT.svs.clients[i].lastmessage = SV_INIT.svs.realtime // don't timeout
            SV_INIT.svs.clients[i].lastconnect = SV_INIT.svs.realtime
            Com.DPrintf("new client added.\n")
        }


        /**
         * Checks if the rcon password is corect.
         */
        public fun Rcon_Validate(): Int {
            if (0 == SV_MAIN.rcon_password.string.length())
                return 0

            if (0 != Lib.strcmp(Cmd.Argv(1), SV_MAIN.rcon_password.string))
                return 0

            return 1
        }

        /**
         * A client issued an rcon command. Shift down the remaining args Redirect
         * all printfs fromt hte server to the client.
         */
        public fun SVC_RemoteCommand() {
            var i: Int
            var remaining: String

            i = Rcon_Validate()

            val msg = Lib.CtoJava(Globals.net_message.data, 4, 1024)

            if (i == 0)
                Com.Printf("Bad rcon from " + NET.AdrToString(Globals.net_from) + ":\n" + msg + "\n")
            else
                Com.Printf("Rcon from " + NET.AdrToString(Globals.net_from) + ":\n" + msg + "\n")

            Com.BeginRedirect(Defines.RD_PACKET, SV_SEND.sv_outputbuf, Defines.SV_OUTPUTBUF_LENGTH, object : Com.RD_Flusher() {
                public fun rd_flush(target: Int, buffer: StringBuffer) {
                    SV_SEND.SV_FlushRedirect(target, Lib.stringToBytes(buffer.toString()))
                }
            })

            if (0 == Rcon_Validate()) {
                Com.Printf("Bad rcon_password.\n")
            } else {
                remaining = ""

                run {
                    i = 2
                    while (i < Cmd.Argc()) {
                        remaining += Cmd.Argv(i)
                        remaining += " "
                        i++
                    }
                }

                Cmd.ExecuteString(remaining)
            }

            Com.EndRedirect()
        }

        /**
         * A connectionless packet has four leading 0xff characters to distinguish
         * it from a game channel. Clients that are in the game can still send
         * connectionless packets. It is used also by rcon commands.
         */
        public fun SV_ConnectionlessPacket() {
            val s: String
            val c: String

            MSG.BeginReading(Globals.net_message)
            MSG.ReadLong(Globals.net_message) // skip the -1 marker

            s = MSG.ReadStringLine(Globals.net_message)

            Cmd.TokenizeString(s.toCharArray(), false)

            c = Cmd.Argv(0)

            //for debugging purposes
            //Com.Printf("Packet " + NET.AdrToString(Netchan.net_from) + " : " + c + "\n");
            //Com.Printf(Lib.hexDump(net_message.data, 64, false) + "\n");

            if (0 == Lib.strcmp(c, "ping"))
                SVC_Ping()
            else if (0 == Lib.strcmp(c, "ack"))
                SVC_Ack()
            else if (0 == Lib.strcmp(c, "status"))
                SVC_Status()
            else if (0 == Lib.strcmp(c, "info"))
                SVC_Info()
            else if (0 == Lib.strcmp(c, "getchallenge"))
                SVC_GetChallenge()
            else if (0 == Lib.strcmp(c, "connect"))
                SVC_DirectConnect()
            else if (0 == Lib.strcmp(c, "rcon"))
                SVC_RemoteCommand()
            else {
                Com.Printf("bad connectionless packet from " + NET.AdrToString(Globals.net_from) + "\n")
                Com.Printf("[" + s + "]\n")
                Com.Printf("" + Lib.hexDump(Globals.net_message.data, 128, false))
            }
        }

        /**
         * Updates the cl.ping variables.
         */
        public fun SV_CalcPings() {
            var i: Int
            var j: Int
            var cl: client_t
            var total: Int
            var count: Int

            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state != Defines.cs_spawned)
                        continue

                    total = 0
                    count = 0
                    run {
                        j = 0
                        while (j < Defines.LATENCY_COUNTS) {
                            if (cl.frame_latency[j] > 0) {
                                count++
                                total += cl.frame_latency[j]
                            }
                            j++
                        }
                    }
                    if (0 == count)
                        cl.ping = 0
                    else
                        cl.ping = total / count

                    // let the game dll know about the ping
                    cl.edict.client.ping = cl.ping
                    i++
                }
            }
        }

        /**
         * Every few frames, gives all clients an allotment of milliseconds for
         * their command moves. If they exceed it, assume cheating.
         */
        public fun SV_GiveMsec() {
            var i: Int
            var cl: client_t

            if ((SV_INIT.sv.framenum and 15) != 0)
                return

            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state == Defines.cs_free)
                        continue

                    cl.commandMsec = 1800 // 1600 + some slop
                    i++
                }
            }
        }

        /**
         * Reads packets from the network or loopback.
         */
        public fun SV_ReadPackets() {
            var i: Int
            var cl: client_t
            var qport = 0

            while (NET.GetPacket(Defines.NS_SERVER, Globals.net_from, Globals.net_message)) {

                // check for connectionless packet (0xffffffff) first
                if ((Globals.net_message.data[0] == -1) && (Globals.net_message.data[1] == -1) && (Globals.net_message.data[2] == -1) && (Globals.net_message.data[3] == -1)) {
                    SV_ConnectionlessPacket()
                    continue
                }

                // read the qport out of the message so we can fix up
                // stupid address translating routers
                MSG.BeginReading(Globals.net_message)
                MSG.ReadLong(Globals.net_message) // sequence number
                MSG.ReadLong(Globals.net_message) // sequence number
                qport = MSG.ReadShort(Globals.net_message) and 65535

                // check for packets from connected clients
                run {
                    i = 0
                    while (i < SV_MAIN.maxclients.value) {
                        cl = SV_INIT.svs.clients[i]
                        if (cl.state == Defines.cs_free)
                            continue
                        if (!NET.CompareBaseAdr(Globals.net_from, cl.netchan.remote_address))
                            continue
                        if (cl.netchan.qport != qport)
                            continue
                        if (cl.netchan.remote_address.port != Globals.net_from.port) {
                            Com.Printf("SV_ReadPackets: fixing up a translated port\n")
                            cl.netchan.remote_address.port = Globals.net_from.port
                        }

                        if (Netchan.Process(cl.netchan, Globals.net_message)) {
                            // this is a valid, sequenced packet, so process it
                            if (cl.state != Defines.cs_zombie) {
                                cl.lastmessage = SV_INIT.svs.realtime // don't timeout
                                SV_USER.SV_ExecuteClientMessage(cl)
                            }
                        }
                        break
                        i++
                    }
                }

                if (i != SV_MAIN.maxclients.value)
                    continue
            }
        }

        /**
         * If a packet has not been received from a client for timeout.value
         * seconds, drop the conneciton. Server frames are used instead of realtime
         * to avoid dropping the local client while debugging.

         * When a client is normally dropped, the client_t goes into a zombie state
         * for a few seconds to make sure any final reliable message gets resent if
         * necessary.
         */
        public fun SV_CheckTimeouts() {
            var i: Int
            var cl: client_t
            val droppoint: Int
            val zombiepoint: Int

            droppoint = (SV_INIT.svs.realtime - 1000 * SV_MAIN.timeout.value) as Int
            zombiepoint = (SV_INIT.svs.realtime - 1000 * SV_MAIN.zombietime.value) as Int

            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    // message times may be wrong across a changelevel
                    if (cl.lastmessage > SV_INIT.svs.realtime)
                        cl.lastmessage = SV_INIT.svs.realtime

                    if (cl.state == Defines.cs_zombie && cl.lastmessage < zombiepoint) {
                        cl.state = Defines.cs_free // can now be reused
                        continue
                    }
                    if ((cl.state == Defines.cs_connected || cl.state == Defines.cs_spawned) && cl.lastmessage < droppoint) {
                        SV_SEND.SV_BroadcastPrintf(Defines.PRINT_HIGH, cl.name + " timed out\n")
                        SV_DropClient(cl)
                        cl.state = Defines.cs_free // don't bother with zombie state
                    }
                    i++
                }
            }
        }

        /**
         * SV_PrepWorldFrame

         * This has to be done before the world logic, because player processing
         * happens outside RunWorldFrame.
         */
        public fun SV_PrepWorldFrame() {
            var ent: edict_t
            var i: Int

            run {
                i = 0
                while (i < GameBase.num_edicts) {
                    ent = GameBase.g_edicts[i]
                    // events only last for a single message
                    ent.s.event = 0
                    i++
                }
            }

        }

        /**
         * SV_RunGameFrame.
         */
        public fun SV_RunGameFrame() {
            if (Globals.host_speeds.value != 0)
                Globals.time_before_game = Timer.Milliseconds()

            // we always need to bump framenum, even if we
            // don't run the world, otherwise the delta
            // compression can get confused when a client
            // has the "current" frame
            SV_INIT.sv.framenum++
            SV_INIT.sv.time = SV_INIT.sv.framenum * 100

            // don't run if paused
            if (0 == SV_MAIN.sv_paused.value || SV_MAIN.maxclients.value > 1) {
                GameBase.G_RunFrame()

                // never get more than one tic behind
                if (SV_INIT.sv.time < SV_INIT.svs.realtime) {
                    if (SV_MAIN.sv_showclamp.value != 0)
                        Com.Printf("sv highclamp\n")
                    SV_INIT.svs.realtime = SV_INIT.sv.time
                }
            }

            if (Globals.host_speeds.value != 0)
                Globals.time_after_game = Timer.Milliseconds()

        }

        /**
         * SV_Frame.
         */
        public fun SV_Frame(msec: Long) {
            Globals.time_before_game = Globals.time_after_game = 0

            // if server is not active, do nothing
            if (!SV_INIT.svs.initialized)
                return

            SV_INIT.svs.realtime += msec

            // keep the random time dependent
            Lib.rand()

            // check timeouts
            SV_CheckTimeouts()

            // get packets from clients
            SV_ReadPackets()

            //if (Game.g_edicts[1] !=null)
            //	Com.p("player at:" + Lib.vtofsbeaty(Game.g_edicts[1].s.origin ));

            // move autonomous things around if enough time has passed
            if (0 == SV_MAIN.sv_timedemo.value && SV_INIT.svs.realtime < SV_INIT.sv.time) {
                // never let the time get too far off
                if (SV_INIT.sv.time - SV_INIT.svs.realtime > 100) {
                    if (SV_MAIN.sv_showclamp.value != 0)
                        Com.Printf("sv lowclamp\n")
                    SV_INIT.svs.realtime = SV_INIT.sv.time - 100
                }
                NET.Sleep(SV_INIT.sv.time - SV_INIT.svs.realtime)
                return
            }

            // update ping based on the last known frame from all clients
            SV_CalcPings()

            // give the clients some timeslices
            SV_GiveMsec()

            // let everything in the world think and move
            SV_RunGameFrame()

            // send messages back to the clients that had packets read this frame
            SV_SEND.SV_SendClientMessages()

            // save the entire world state if recording a serverdemo
            SV_ENTS.SV_RecordDemoMessage()

            // send a heartbeat to the master if needed
            Master_Heartbeat()

            // clear teleport flags, etc for next frame
            SV_PrepWorldFrame()

        }

        public fun Master_Heartbeat() {
            val string: String
            var i: Int

            // pgm post3.19 change, cvar pointer not validated before dereferencing
            if (Globals.dedicated == null || 0 == Globals.dedicated.value)
                return  // only dedicated servers send heartbeats

            // pgm post3.19 change, cvar pointer not validated before dereferencing
            if (null == SV_MAIN.public_server || 0 == SV_MAIN.public_server!!.value)
                return  // a private dedicated game

            // check for time wraparound
            if (SV_INIT.svs.last_heartbeat > SV_INIT.svs.realtime)
                SV_INIT.svs.last_heartbeat = SV_INIT.svs.realtime

            if (SV_INIT.svs.realtime - SV_INIT.svs.last_heartbeat < SV_MAIN.HEARTBEAT_SECONDS * 1000)
                return  // not time to send yet

            SV_INIT.svs.last_heartbeat = SV_INIT.svs.realtime

            // send the same string that we would give for a status OOB command
            string = SV_StatusString()

            // send to group master
            run {
                i = 0
                while (i < Defines.MAX_MASTERS) {
                    if (SV_MAIN.master_adr[i].port != 0) {
                        Com.Printf("Sending heartbeat to " + NET.AdrToString(SV_MAIN.master_adr[i]) + "\n")
                        Netchan.OutOfBandPrint(Defines.NS_SERVER, SV_MAIN.master_adr[i], "heartbeat\n" + string)
                    }
                    i++
                }
            }
        }


        /**
         * Master_Shutdown, Informs all masters that this server is going down.
         */
        public fun Master_Shutdown() {
            var i: Int

            // pgm post3.19 change, cvar pointer not validated before dereferencing
            if (null == Globals.dedicated || 0 == Globals.dedicated.value)
                return  // only dedicated servers send heartbeats

            // pgm post3.19 change, cvar pointer not validated before dereferencing
            if (null == SV_MAIN.public_server || 0 == SV_MAIN.public_server!!.value)
                return  // a private dedicated game

            // send to group master
            run {
                i = 0
                while (i < Defines.MAX_MASTERS) {
                    if (SV_MAIN.master_adr[i].port != 0) {
                        if (i > 0)
                            Com.Printf("Sending heartbeat to " + NET.AdrToString(SV_MAIN.master_adr[i]) + "\n")
                        Netchan.OutOfBandPrint(Defines.NS_SERVER, SV_MAIN.master_adr[i], "shutdown")
                    }
                    i++
                }
            }
        }


        /**
         * Pull specific info from a newly changed userinfo string into a more C
         * freindly form.
         */
        public fun SV_UserinfoChanged(cl: client_t) {
            var `val`: String
            val i: Int

            // call prog code to allow overrides
            PlayerClient.ClientUserinfoChanged(cl.edict, cl.userinfo)

            // name for C code
            cl.name = Info.Info_ValueForKey(cl.userinfo, "name")

            // mask off high bit
            //TODO: masking for german umlaute
            //for (i=0 ; i<sizeof(cl.name) ; i++)
            //	cl.name[i] &= 127;

            // rate command
            `val` = Info.Info_ValueForKey(cl.userinfo, "rate")
            if (`val`.length() > 0) {
                i = Lib.atoi(`val`)
                cl.rate = i
                if (cl.rate < 100)
                    cl.rate = 100
                if (cl.rate > 15000)
                    cl.rate = 15000
            } else
                cl.rate = 5000

            // msg command
            `val` = Info.Info_ValueForKey(cl.userinfo, "msg")
            if (`val`.length() > 0) {
                cl.messagelevel = Lib.atoi(`val`)
            }

        }

        /**
         * Only called at quake2.exe startup, not for each game
         */
        public fun SV_Init() {
            SV_CCMDS.SV_InitOperatorCommands() //ok.

            SV_MAIN.rcon_password = Cvar.Get("rcon_password", "", 0)
            Cvar.Get("skill", "1", 0)
            Cvar.Get("deathmatch", "0", Defines.CVAR_LATCH)
            Cvar.Get("coop", "0", Defines.CVAR_LATCH)
            Cvar.Get("dmflags", "" + Defines.DF_INSTANT_ITEMS, Defines.CVAR_SERVERINFO)
            Cvar.Get("fraglimit", "0", Defines.CVAR_SERVERINFO)
            Cvar.Get("timelimit", "0", Defines.CVAR_SERVERINFO)
            Cvar.Get("cheats", "0", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            Cvar.Get("protocol", "" + Defines.PROTOCOL_VERSION, Defines.CVAR_SERVERINFO or Defines.CVAR_NOSET)

            SV_MAIN.maxclients = Cvar.Get("maxclients", "1", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            SV_MAIN.hostname = Cvar.Get("hostname", "noname", Defines.CVAR_SERVERINFO or Defines.CVAR_ARCHIVE)
            SV_MAIN.timeout = Cvar.Get("timeout", "125", 0)
            SV_MAIN.zombietime = Cvar.Get("zombietime", "2", 0)
            SV_MAIN.sv_showclamp = Cvar.Get("showclamp", "0", 0)
            SV_MAIN.sv_paused = Cvar.Get("paused", "0", 0)
            SV_MAIN.sv_timedemo = Cvar.Get("timedemo", "0", 0)
            SV_MAIN.sv_enforcetime = Cvar.Get("sv_enforcetime", "0", 0)

            SV_MAIN.allow_download = Cvar.Get("allow_download", "1", Defines.CVAR_ARCHIVE)
            SV_MAIN.allow_download_players = Cvar.Get("allow_download_players", "0", Defines.CVAR_ARCHIVE)
            SV_MAIN.allow_download_models = Cvar.Get("allow_download_models", "1", Defines.CVAR_ARCHIVE)
            SV_MAIN.allow_download_sounds = Cvar.Get("allow_download_sounds", "1", Defines.CVAR_ARCHIVE)
            SV_MAIN.allow_download_maps = Cvar.Get("allow_download_maps", "1", Defines.CVAR_ARCHIVE)

            SV_MAIN.sv_noreload = Cvar.Get("sv_noreload", "0", 0)
            SV_MAIN.sv_airaccelerate = Cvar.Get("sv_airaccelerate", "0", Defines.CVAR_LATCH)
            SV_MAIN.public_server = Cvar.Get("public", "0", 0)
            SV_MAIN.sv_reconnect_limit = Cvar.Get("sv_reconnect_limit", "3", Defines.CVAR_ARCHIVE)

            SZ.Init(Globals.net_message, Globals.net_message_buffer, Globals.net_message_buffer.length)
        }

        /**
         * Used by SV_Shutdown to send a final message to all connected clients
         * before the server goes down. The messages are sent immediately, not just
         * stuck on the outgoing message list, because the server is going to
         * totally exit after returning from this function.
         */
        public fun SV_FinalMessage(message: String, reconnect: Boolean) {
            var i: Int
            var cl: client_t

            SZ.Clear(Globals.net_message)
            MSG.WriteByte(Globals.net_message, Defines.svc_print)
            MSG.WriteByte(Globals.net_message, Defines.PRINT_HIGH)
            MSG.WriteString(Globals.net_message, message)

            if (reconnect)
                MSG.WriteByte(Globals.net_message, Defines.svc_reconnect)
            else
                MSG.WriteByte(Globals.net_message, Defines.svc_disconnect)

            // send it twice
            // stagger the packets to crutch operating system limited buffers

            run {
                i = 0
                while (i < SV_INIT.svs.clients.length) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state >= Defines.cs_connected)
                        Netchan.Transmit(cl.netchan, Globals.net_message.cursize, Globals.net_message.data)
                    i++
                }
            }
            run {
                i = 0
                while (i < SV_INIT.svs.clients.length) {
                    cl = SV_INIT.svs.clients[i]
                    if (cl.state >= Defines.cs_connected)
                        Netchan.Transmit(cl.netchan, Globals.net_message.cursize, Globals.net_message.data)
                    i++
                }
            }
        }

        /**
         * Called when each game quits, before Sys_Quit or Sys_Error.
         */
        public fun SV_Shutdown(finalmsg: String, reconnect: Boolean) {
            if (SV_INIT.svs.clients != null)
                SV_FinalMessage(finalmsg, reconnect)

            Master_Shutdown()

            SV_GAME.SV_ShutdownGameProgs()

            // free current level
            if (SV_INIT.sv.demofile != null)
                try {
                    SV_INIT.sv.demofile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            SV_INIT.sv = server_t()

            Globals.server_state = SV_INIT.sv.state

            if (SV_INIT.svs.demofile != null)
                try {
                    SV_INIT.svs.demofile.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }


            SV_INIT.svs = server_static_t()
        }
    }
}