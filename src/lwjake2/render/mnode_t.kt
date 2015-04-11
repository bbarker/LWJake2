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

import lwjake2.game.cplane_t

open public class mnode_t {
    //	common with leaf
    public var contents: Int = 0 // -1, to differentiate from leafs
    public var visframe: Int = 0 // node needs to be traversed if current

    //public float minmaxs[] = new float[6]; // for bounding box culling
    public var mins: FloatArray = FloatArray(3) // for bounding box culling
    public var maxs: FloatArray = FloatArray(3) // for bounding box culling

    public var parent: mnode_t

    //	node specific
    public var plane: cplane_t
    public var children: Array<mnode_t> = arrayOfNulls(2)

    // unsigned short
    public var firstsurface: Int = 0
    public var numsurfaces: Int = 0

}
