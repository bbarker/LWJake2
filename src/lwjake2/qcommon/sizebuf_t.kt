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

import java.util.Arrays

/**
 * sizebuf_t
 */
public class sizebuf_t {
    public var allowoverflow: Boolean = false
    public var overflowed: Boolean = false
    public var data: ByteArray? = null
    public var maxsize: Int = 0
    public var cursize: Int = 0
    public var readcount: Int = 0

    public fun clear() {
        if (data != null)
            Arrays.fill(data, 0.toByte())
        cursize = 0
        overflowed = false
    }
}
