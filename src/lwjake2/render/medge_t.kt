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

import lwjake2.Defines

import java.nio.ByteBuffer

public class medge_t(b: ByteBuffer) {

    // unsigned short
    public var v: IntArray = IntArray(2)

    public var cachededgeoffset: Int = 0

    {
        v[0] = b.getShort() and 65535
        v[1] = b.getShort() and 65535
    }

    companion object {

        public val DISK_SIZE: Int = 2 * Defines.SIZE_OF_SHORT

        public val MEM_SIZE: Int = 3 * Defines.SIZE_OF_INT
    }
}