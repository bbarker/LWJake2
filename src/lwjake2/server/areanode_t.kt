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

import lwjake2.game.link_t

public class areanode_t {
    var axis: Int = 0 // -1 = leaf node
    var dist: Float = 0.toFloat()
    var children = arrayOfNulls<areanode_t>(2)
    var trigger_edicts = link_t(this)
    var solid_edicts = link_t(this)

    // used for debugging
    var mins_rst = floatArray(0.0, 0.0, 0.0)
    var maxs_rst = floatArray(0.0, 0.0, 0.0)
}
