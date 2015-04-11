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
import lwjake2.game.cmodel_t
import lwjake2.game.entity_state_t
import lwjake2.qcommon.sizebuf_t

import java.io.RandomAccessFile

public class server_t {

    {
        models = arrayOfNulls<cmodel_t>(Defines.MAX_MODELS)
        for (n in 0..Defines.MAX_MODELS - 1)
            models[n] = cmodel_t()

        for (n in 0..Defines.MAX_EDICTS - 1)
            baselines[n] = entity_state_t(null)
    }

    var state: Int = 0 // precache commands are only valid during load

    var attractloop: Boolean = false // running cinematics and demos for the local system
    // only

    var loadgame: Boolean = false // client begins should reuse existing entity

    var time: Int = 0 // always sv.framenum * 100 msec

    var framenum: Int = 0

    var name = "" // map name, or cinematic name

    var models: Array<cmodel_t>

    var configstrings = arrayOfNulls<String>(Defines.MAX_CONFIGSTRINGS)

    var baselines = arrayOfNulls<entity_state_t>(Defines.MAX_EDICTS)

    // the multicast buffer is used to send a message to a set of clients
    // it is only used to marshall data until SV_Multicast is called
    var multicast = sizebuf_t()

    var multicast_buf = ByteArray(Defines.MAX_MSGLEN)

    // demo server information
    var demofile: RandomAccessFile

    var timedemo: Boolean = false // don't time sync
}