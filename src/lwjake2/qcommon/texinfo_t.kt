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

import lwjake2.util.Lib

import java.nio.ByteBuffer
import java.nio.ByteOrder

public class texinfo_t(bb: ByteBuffer) {

    // works fine.
    public constructor(cmod_base: ByteArray, o: Int, len: Int) : this(ByteBuffer.wrap(cmod_base, o, len).order(ByteOrder.LITTLE_ENDIAN)) {
    }

    {

        val str = ByteArray(32)

        vecs[0] = floatArray(bb.getFloat().toFloat(), bb.getFloat().toFloat(), bb.getFloat().toFloat(), bb.getFloat().toFloat())
        vecs[1] = floatArray(bb.getFloat().toFloat(), bb.getFloat().toFloat(), bb.getFloat().toFloat(), bb.getFloat().toFloat())

        flags = bb.getInt()
        value = bb.getInt()

        bb.get(str)
        texture = String(str, 0, Lib.strlen(str))
        nexttexinfo = bb.getInt()
    }

    //float			vecs[2][4];		// [s/t][xyz offset]
    public var vecs: Array<FloatArray> = array<FloatArray>(floatArray(0.0, 0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0, 0.0).toFloat())
    public var flags: Int = 0 // miptex flags + overrides
    public var value: Int = 0 // light emission, etc
    //char			texture[32];	// texture name (textures/*.wal)
    public var texture: String = ""
    public var nexttexinfo: Int = 0 // for animations, -1 = end of chain

    companion object {

        public val SIZE: Int = 32 + 4 + 4 + 32 + 4
    }
}
