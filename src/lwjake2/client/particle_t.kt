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

package lwjake2.client

import lwjake2.Defines
import lwjake2.util.Lib

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

public class particle_t {
    companion object {

        // lwjgl renderer needs a ByteBuffer
        private val colorByteArray = Lib.newByteBuffer(Defines.MAX_PARTICLES * Lib.SIZEOF_INT, ByteOrder.LITTLE_ENDIAN)

        public var vertexArray: FloatBuffer = Lib.newFloatBuffer(Defines.MAX_PARTICLES * 3)
        public var colorTable: IntArray = IntArray(256)
        public var colorArray: IntBuffer = colorByteArray.asIntBuffer()


        public fun setColorPalette(palette: IntArray) {
            for (i in 0..256 - 1) {
                colorTable[i] = palette[i] and 16777215
            }
        }

        public fun getColorAsByteBuffer(): ByteBuffer {
            return colorByteArray
        }
    }
}
