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
import lwjake2.game.edict_t
import lwjake2.game.usercmd_t
import lwjake2.qcommon.netchan_t
import lwjake2.qcommon.sizebuf_t

public class client_t {

    {
        for (n in 0..Defines.UPDATE_BACKUP - 1) {
            frames[n] = client_frame_t()
        }
    }

    var state: Int = 0

    var userinfo = ""

    var lastframe: Int = 0 // for delta compression
    var lastcmd = usercmd_t() // for filling in big drops

    var commandMsec: Int = 0 // every seconds this is reset, if user
    // commands exhaust it, assume time cheating

    var frame_latency = IntArray(LATENCY_COUNTS)
    var ping: Int = 0

    var message_size = IntArray(RATE_MESSAGES) // used to rate drop packets
    var rate: Int = 0
    var surpressCount: Int = 0 // number of messages rate supressed

    // pointer
    var edict: edict_t // EDICT_NUM(clientnum+1)

    //char				name[32];			// extracted from userinfo, high bits masked
    var name = "" // extracted from userinfo, high bits masked

    var messagelevel: Int = 0 // for filtering printed messages

    // The datagram is written to by sound calls, prints, temp ents, etc.
    // It can be harmlessly overflowed.
    var datagram = sizebuf_t()
    var datagram_buf = ByteArray(Defines.MAX_MSGLEN)

    var frames = arrayOfNulls<client_frame_t>(Defines.UPDATE_BACKUP) // updates can be delta'd from here

    var download: ByteArray // file being downloaded
    var downloadsize: Int = 0 // total bytes (can't use EOF because of paks)
    var downloadcount: Int = 0 // bytes sent

    var lastmessage: Int = 0 // sv.framenum when packet was last received
    var lastconnect: Int = 0

    var challenge: Int = 0 // challenge of this user, randomly generated

    var netchan = netchan_t()

    //this was introduced by rst, since java can't calculate the index out of the address.
    var serverindex: Int = 0

    companion object {

        public val LATENCY_COUNTS: Int = 16
        public val RATE_MESSAGES: Int = 10
    }
}
