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

package lwjake2.render.lwjgl

import lwjake2.render.glpoly_t
import lwjake2.util.Lib

import java.nio.FloatBuffer

/**
 * Polygon

 * @author cwei
 */
public class Polygon private() : glpoly_t() {

    private fun clear() {
        next = null
        chain = null
        numverts = 0
        flags = 0
    }

    // the interleaved buffer has the format:
    // textureCoord0 (index 0, 1)
    // vertex (index 2, 3, 4)
    // textureCoord1 (index 5, 6)

    public fun x(index: Int): Float {
        return buffer.get((index + pos) * 7 + 2)
    }

    public fun x(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 2, value)
    }

    public fun y(index: Int): Float {
        return buffer.get((index + pos) * 7 + 3)
    }

    public fun y(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 3, value)
    }

    public fun z(index: Int): Float {
        return buffer.get((index + pos) * 7 + 4)
    }

    public fun z(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 4, value)
    }

    public fun s1(index: Int): Float {
        return buffer.get((index + pos) * 7 + 0)
    }

    public fun s1(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 0, value)
    }

    public fun t1(index: Int): Float {
        return buffer.get((index + pos) * 7 + 1)
    }

    public fun t1(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 1, value)
    }

    public fun s2(index: Int): Float {
        return buffer.get((index + pos) * 7 + 5)
    }

    public fun s2(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 5, value)
    }

    public fun t2(index: Int): Float {
        return buffer.get((index + pos) * 7 + 6)
    }

    public fun t2(index: Int, value: Float) {
        buffer.put((index + pos) * 7 + 6, value)
    }

    public fun beginScrolling(scroll: Float) {
        var scroll = scroll
        var index = pos * 7
        run {
            var i = 0
            while (i < numverts) {
                scroll += s1_old[i] = buffer.get(index)
                buffer.put(index, scroll)
                i++
                index += 7
            }
        }
    }

    public fun endScrolling() {
        var index = pos * 7
        run {
            var i = 0
            while (i < numverts) {
                buffer.put(index, s1_old[i])
                i++
                index += 7
            }
        }
    }

    companion object {

        private val MAX_POLYS = 20000
        private val MAX_BUFFER_VERTICES = 120000

        // backup for s1 scrolling
        private val s1_old = FloatArray(MAX_VERTICES)

        private val buffer = Lib.newFloatBuffer(MAX_BUFFER_VERTICES * STRIDE)
        private var bufferIndex = 0
        private var polyCount = 0
        private val polyCache = arrayOfNulls<Polygon>(MAX_POLYS)
        {
            for (i in polyCache.indices) {
                polyCache[i] = Polygon()
            }
        }

        fun create(numverts: Int): glpoly_t {
            val poly = polyCache[polyCount++]
            poly.clear()
            poly.numverts = numverts
            poly.pos = bufferIndex
            bufferIndex += numverts
            return poly
        }

        fun reset() {
            polyCount = 0
            bufferIndex = 0
        }

        fun getInterleavedBuffer(): FloatBuffer {
            return buffer.rewind() as FloatBuffer
        }
    }
}