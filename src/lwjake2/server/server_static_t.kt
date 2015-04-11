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
import lwjake2.game.entity_state_t
import lwjake2.qcommon.sizebuf_t

import java.io.RandomAccessFile

public class server_static_t {
    {
        for (n in 0..Defines.MAX_CHALLENGES - 1) {
            challenges[n] = challenge_t()
        }
    }

    var initialized: Boolean = false // sv_init has completed

    var realtime: Int = 0 // always increasing, no clamping, etc

    var mapcmd = "" // ie: *intro.cin+base

    var spawncount: Int = 0 // incremented each server start

    // used to check late spawns

    var clients: Array<client_t> // [maxclients->value];

    var num_client_entities: Int = 0 // maxclients->value*UPDATE_BACKUP*MAX_PACKET_ENTITIES

    var next_client_entities: Int = 0 // next client_entity to use

    var client_entities: Array<entity_state_t> // [num_client_entities]

    var last_heartbeat: Int = 0

    var challenges = arrayOfNulls<challenge_t>(Defines.MAX_CHALLENGES) // to
    // prevent
    // invalid
    // IPs
    // from
    // connecting

    // serverrecord values
    var demofile: RandomAccessFile

    var demo_multicast = sizebuf_t()

    var demo_multicast_buf = ByteArray(Defines.MAX_MSGLEN)
}