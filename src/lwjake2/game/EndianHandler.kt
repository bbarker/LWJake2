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

public abstract class EndianHandler {

    public abstract fun BigFloat(f: Float): Float
    public abstract fun BigShort(s: Short): Short
    public abstract fun BigLong(i: Int): Int
    public abstract fun LittleFloat(f: Float): Float
    public abstract fun LittleShort(s: Short): Short
    public abstract fun LittleLong(i: Int): Int

    companion object {
        private val mask = 255

        public fun swapFloat(f: Float): Float {
            var f = f
            var i = Float.floatToRawIntBits(f)
            i = swapInt(i)
            f = Float.intBitsToFloat(i)

            return f
        }

        public fun swapInt(i: Int): Int {
            var i = i

            var a = i and mask
            i = i ushr 8

            a = a shl 24

            var b = i and mask

            i = i ushr 8
            b = b shl 16

            var c = i and mask
            i = i ushr 8
            c = c shl 8

            return i or c or b or a
        }

        public fun swapShort(s: Short): Short {
            var a = s and mask
            a = a shl 8
            val b = (s.toInt().ushr(8)) and mask

            return (b or a).toShort()
        }
    }
}
