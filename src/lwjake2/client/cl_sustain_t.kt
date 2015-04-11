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

/**
 * cl_sustain_t
 */
public class cl_sustain_t {
    abstract class ThinkAdapter {
        abstract fun think(self: cl_sustain_t)
    }

    var id: Int = 0
    var type: Int = 0
    var endtime: Int = 0
    var nextthink: Int = 0
    var thinkinterval: Int = 0
    var org = FloatArray(3)
    var dir = FloatArray(3)
    var color: Int = 0
    var count: Int = 0
    var magnitude: Int = 0

    var think: ThinkAdapter? = null

    fun clear() {
        org[0] = org[1] = org[2] = dir[0] = dir[1] = dir[2] = (id = type = endtime = nextthink = thinkinterval = color = count = magnitude = 0).toFloat()
        think = null
    }
}
