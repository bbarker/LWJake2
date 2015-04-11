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

import lwjake2.util.QuakeFile

import java.io.IOException

public class mmove_t {
    public constructor(firstframe: Int, lastframe: Int, frame: Array<mframe_t>, endfunc: EntThinkAdapter) {

        this.firstframe = firstframe
        this.lastframe = lastframe
        this.frame = frame
        this.endfunc = endfunc
    }

    public constructor() {
    }

    public var firstframe: Int = 0
    public var lastframe: Int = 0
    public var frame: Array<mframe_t>? = null //ptr
    public var endfunc: EntThinkAdapter


    /** Writes the structure to a random acccess file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeInt(firstframe)
        f.writeInt(lastframe)
        if (frame == null)
            f.writeInt(-1)
        else {
            f.writeInt(frame!!.size())
            for (n in frame!!.indices)
                frame!![n].write(f)
        }
        f.writeAdapter(endfunc)
    }

    /** Read the mmove_t from the RandomAccessFile.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        firstframe = f.readInt()
        lastframe = f.readInt()

        val len = f.readInt()

        frame = arrayOfNulls<mframe_t>(len)
        for (n in 0..len - 1) {
            frame[n] = mframe_t()
            frame!![n].read(f)
        }
        endfunc = f.readAdapter() as EntThinkAdapter
    }
}
