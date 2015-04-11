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

package lwjake2.qcommon

import lwjake2.Defines

public class miptex_t {
    //char		name[32];
    var name = ""

    var width: Int = 0
    var height: Int = 0

    //unsigned	offsets[MIPLEVELS];		// four mip maps stored
    var offsets = IntArray(Defines.MIPLEVELS)        // four mip maps stored

    //char		animname[32];			// next frame in animation chain
    var animframe = ""

    var flags: Int = 0
    var contents: Int = 0
    var value: Int = 0
}
