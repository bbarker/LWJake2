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

package lwjake2.game

import lwjake2.util.Math3D

//a trace is returned when a box is swept through the world
public class trace_t {
    public var allsolid: Boolean = false // if true, plane is not valid
    public var startsolid: Boolean = false // if true, the initial point was in a solid area
    public var fraction: Float = 0.toFloat() // time completed, 1.0 = didn't hit anything
    public var endpos: FloatArray = floatArray(0.0, 0.0, 0.0) // final position
    // memory
    public var plane: cplane_t = cplane_t() // surface normal at impact
    // pointer
    public var surface: csurface_t? = null // surface hit
    public var contents: Int = 0 // contents on other side of surface hit
    // pointer
    public var ent: edict_t? = null // not set by CM_*() functions

    public fun set(from: trace_t) {
        allsolid = from.allsolid
        startsolid = from.allsolid
        fraction = from.fraction
        Math3D.VectorCopy(from.endpos, endpos)
        plane.set(from.plane)
        surface = from.surface
        contents = from.contents
        ent = from.ent
    }

    public fun clear() {
        allsolid = false
        startsolid = false
        fraction = 0
        Math3D.VectorClear(endpos)
        plane.clear()
        surface = null
        contents = 0
        ent = null
    }
}
