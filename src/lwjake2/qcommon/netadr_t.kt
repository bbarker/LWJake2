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

package lwjake2.qcommon

import lwjake2.Defines
import lwjake2.sys.NET

import java.net.InetAddress
import java.net.UnknownHostException

public class netadr_t {

    public var type: Int = 0

    public var port: Int = 0

    public var ip: ByteArray

    {
        this.type = Defines.NA_LOOPBACK
        this.port = 0 // any
        try {
            // localhost / 127.0.0.1
            this.ip = InetAddress.getByName(null).getAddress()
        } catch (e: UnknownHostException) {
        }

    }

    throws(javaClass<UnknownHostException>())
    public fun getInetAddress(): InetAddress? {
        when (type) {
            Defines.NA_BROADCAST -> return InetAddress.getByName("255.255.255.255")
            Defines.NA_LOOPBACK -> // localhost / 127.0.0.1
                return InetAddress.getByName(null)
            Defines.NA_IP -> return InetAddress.getByAddress(ip)
            else -> return null
        }
    }

    public fun set(from: netadr_t) {
        type = from.type
        port = from.port
        ip[0] = from.ip[0]
        ip[1] = from.ip[1]
        ip[2] = from.ip[2]
        ip[3] = from.ip[3]
    }

    override fun toString(): String {
        return if ((type == Defines.NA_LOOPBACK))
            "loopback"
        else
            NET.AdrToString(this)
    }
}