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

package lwjake2.client

import lwjake2.qcommon.netchan_t

import java.io.RandomAccessFile

public class client_static_t {

    // was enum connstate_t
    public var state: Int = 0

    // was enum keydest_t
    public var key_dest: Int = 0

    public var framecount: Int = 0
    public var realtime: Int = 0 // always increasing, no clamping, etc
    public var frametime: Float = 0.toFloat() // seconds since last frame

    //	   screen rendering information
    public var disable_screen: Float = 0.toFloat() // showing loading plaque between levels
    // or changing rendering dlls
    // if time gets > 30 seconds ahead, break it
    public var disable_servercount: Int = 0 // when we receive a frame and cl.servercount
    // > cls.disable_servercount, clear disable_screen

    //	   connection information
    public var servername: String = "" // name of server from original connect
    public var connect_time: Float = 0.toFloat() // for connection retransmits

    var quakePort: Int = 0 // a 16 bit value that allows quake servers
    // to work around address translating routers
    public var netchan: netchan_t = netchan_t()
    public var serverProtocol: Int = 0 // in case we are doing some kind of version hack

    public var challenge: Int = 0 // from the server to use for connecting

    public var download: RandomAccessFile // file transfer from server
    public var downloadtempname: String = ""
    public var downloadname: String = ""
    public var downloadnumber: Int = 0
    // was enum dltype_t
    public var downloadtype: Int = 0
    public var downloadpercent: Int = 0

    //	   demo recording info must be here, so it isn't cleared on level change
    public var demorecording: Boolean = false
    public var demowaiting: Boolean = false // don't record until a non-delta message is received
    public var demofile: RandomAccessFile
}
