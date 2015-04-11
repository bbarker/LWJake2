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

public class mleaf_t : mnode_t() {

    //	common with node
    /*
	public int contents; // wil be a negative contents number
	public int visframe; // node needs to be traversed if current

	public float minmaxs[] = new float[6]; // for bounding box culling

	public mnode_t parent;
	*/

    //	leaf specific
    public var cluster: Int = 0
    public var area: Int = 0

    //public msurface_t firstmarksurface;
    public var nummarksurfaces: Int = 0

    // added by cwei
    var markIndex: Int = 0
    var markSurfaces: Array<msurface_t>

    public fun setMarkSurface(markIndex: Int, markSurfaces: Array<msurface_t>) {
        this.markIndex = markIndex
        this.markSurfaces = markSurfaces
    }

    public fun getMarkSurface(index: Int): msurface_t? {
        assert((index >= 0 && index <= nummarksurfaces)) { "mleaf: markSurface bug (index = " + index + "; num = " + nummarksurfaces + ")" }
        // TODO code in Surf.R_RecursiveWorldNode aendern (der Pointer wird wie in C zu weit gezaehlt)
        return if ((index < nummarksurfaces)) markSurfaces[markIndex + index] else null
    }

}
