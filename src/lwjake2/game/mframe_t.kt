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

public class mframe_t {
    public constructor(ai: AIAdapter, dist: Float, think: EntThinkAdapter) {
        this.ai = ai
        this.dist = dist
        this.think = think
    }

    /** Empty constructor.  */
    public constructor() {
    }

    public var ai: AIAdapter
    public var dist: Float = 0.toFloat()
    public var think: EntThinkAdapter

    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeAdapter(ai)
        f.writeFloat(dist)
        f.writeAdapter(think)
    }

    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        ai = f.readAdapter() as AIAdapter
        dist = f.readFloat()
        think = f.readAdapter() as EntThinkAdapter
    }
}
