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

package lwjake2.render

import lwjake2.util.Lib

public abstract class glpoly_t protected() {

    public var next: glpoly_t
    public var chain: glpoly_t
    public var numverts: Int = 0
    public var flags: Int = 0 // for SURF_UNDERWATER (not needed anymore?)

    // the array position (glDrawArrays)
    public var pos: Int = 0

    public abstract fun x(index: Int): Float

    public abstract fun x(index: Int, value: Float)

    public abstract fun y(index: Int): Float

    public abstract fun y(index: Int, value: Float)

    public abstract fun z(index: Int): Float

    public abstract fun z(index: Int, value: Float)

    public abstract fun s1(index: Int): Float

    public abstract fun s1(index: Int, value: Float)

    public abstract fun t1(index: Int): Float

    public abstract fun t1(index: Int, value: Float)

    public abstract fun s2(index: Int): Float

    public abstract fun s2(index: Int, value: Float)

    public abstract fun t2(index: Int): Float

    public abstract fun t2(index: Int, value: Float)

    public abstract fun beginScrolling(s1: Float)

    public abstract fun endScrolling()

    companion object {
        public val STRIDE: Int = 7
        public val BYTE_STRIDE: Int = 7 * Lib.SIZEOF_FLOAT
        public val MAX_VERTICES: Int = 64
    }
}