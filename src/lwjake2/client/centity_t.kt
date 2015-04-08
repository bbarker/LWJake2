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

import lwjake2.game.entity_state_t

public class centity_t {

    // delta from this if not from a previous frame
    var baseline: entity_state_t = entity_state_t(null)
    public var current: entity_state_t = entity_state_t(null)
    var prev = entity_state_t(null) // will always be valid, but might just be a copy of current

    var serverframe: Int = 0 // if not current, this ent isn't in the frame

    var trailcount: Int = 0 // for diminishing grenade trails
    var lerp_origin = floatArray(0.0f, 0.0f, 0.0f) // for trails (variable hz)

    var fly_stoptime: Int = 0
}
