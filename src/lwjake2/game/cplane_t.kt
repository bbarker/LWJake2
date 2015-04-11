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

public class cplane_t {
    public var normal: FloatArray = FloatArray(3)
    public var dist: Float = 0.toFloat()
    /** This is for fast side tests, 0=xplane, 1=yplane, 2=zplane and 3=arbitrary.  */
    public var type: Byte = 0
    /** This represents signx + (signy<<1) + (signz << 1).  */
    public var signbits: Byte = 0 // signx + (signy<<1) + (signz<<1)
    public var pad: ByteArray = byteArray(0, 0)

    public fun set(c: cplane_t) {
        Math3D.set(normal, c.normal)
        dist = c.dist
        type = c.type
        signbits = c.signbits
        pad[0] = c.pad[0]
        pad[1] = c.pad[1]
    }

    public fun clear() {
        Math3D.VectorClear(normal)
        dist = 0
        type = 0
        signbits = 0
        pad[0] = 0
        pad[1] = 0
    }
}
