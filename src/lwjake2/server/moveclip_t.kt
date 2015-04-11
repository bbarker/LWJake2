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

import lwjake2.game.edict_t
import lwjake2.game.trace_t
import lwjake2.util.Math3D

public class moveclip_t {
    var boxmins = floatArray(0.0, 0.0, 0.0)
    var boxmaxs = floatArray(0.0, 0.0, 0.0)// enclose the test object along entire move
    var mins: FloatArray
    var maxs: FloatArray    // size of the moving object
    var mins2 = floatArray(0.0, 0.0, 0.0)
    var maxs2 = floatArray(0.0, 0.0, 0.0)    // size when clipping against mosnters
    var start: FloatArray
    var end: FloatArray? = null
    // mem
    var trace = trace_t()
    var passedict: edict_t? = null
    var contentmask: Int = 0

    public fun clear() {
        Math3D.VectorClear(boxmins)
        Math3D.VectorClear(boxmaxs)
        Math3D.VectorClear(mins)
        Math3D.VectorClear(maxs)
        Math3D.VectorClear(mins2)
        Math3D.VectorClear(maxs2)
        start = end = null
        trace.clear()
        passedict = null
        contentmask = 0
    }
}
