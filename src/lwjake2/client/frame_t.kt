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

import lwjake2.game.player_state_t

import java.util.Arrays

public class frame_t {

    var valid: Boolean = false            // cleared if delta parsing was invalid
    var serverframe: Int = 0
    public var servertime: Int = 0        // server time the message is valid for (in msec)
    var deltaframe: Int = 0
    var areabits = ByteArray(MAX_MAP_AREAS / 8)        // portalarea visibility bits
    public var playerstate: player_state_t = player_state_t() // mem
    public var num_entities: Int = 0
    public var parse_entities: Int = 0    // non-masked index into cl_parse_entities array

    public fun set(from: frame_t) {
        valid = from.valid
        serverframe = from.serverframe
        deltaframe = from.deltaframe
        num_entities = from.num_entities
        parse_entities = from.parse_entities
        System.arraycopy(from.areabits, 0, areabits, 0, areabits.size())
        playerstate.set(from.playerstate)
    }

    public fun reset() {
        valid = false
        serverframe = servertime = deltaframe = 0
        Arrays.fill(areabits, 0.toByte())
        playerstate.clear()
        num_entities = parse_entities = 0
    }

    companion object {

        public val MAX_MAP_AREAS: Int = 256
    }
}
