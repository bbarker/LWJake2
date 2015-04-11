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
import lwjake2.util.Lib

/**
 * SZ
 */
public class SZ {
    companion object {

        public fun Clear(buf: sizebuf_t) {
            buf.clear()
        }

        //===========================================================================

        public fun Init(buf: sizebuf_t, data: ByteArray, length: Int) {
            // TODO check this. cwei
            buf.readcount = 0

            buf.data = data
            buf.maxsize = length
            buf.cursize = 0
            buf.allowoverflow = buf.overflowed = false
        }


        /** Ask for the pointer using sizebuf_t.cursize (RST)  */
        public fun GetSpace(buf: sizebuf_t, length: Int): Int {
            val oldsize: Int

            if (buf.cursize + length > buf.maxsize) {
                if (!buf.allowoverflow)
                    Com.Error(Defines.ERR_FATAL, "SZ_GetSpace: overflow without allowoverflow set")

                if (length > buf.maxsize)
                    Com.Error(Defines.ERR_FATAL, "SZ_GetSpace: " + length + " is > full buffer size")

                Com.Printf("SZ_GetSpace: overflow\n")
                Clear(buf)
                buf.overflowed = true
            }

            oldsize = buf.cursize
            buf.cursize += length

            return oldsize
        }

        public fun Write(buf: sizebuf_t, data: ByteArray, length: Int) {
            //memcpy(SZ_GetSpace(buf, length), data, length);
            System.arraycopy(data, 0, buf.data, GetSpace(buf, length), length)
        }

        public fun Write(buf: sizebuf_t, data: ByteArray, offset: Int, length: Int) {
            System.arraycopy(data, offset, buf.data, GetSpace(buf, length), length)
        }

        public fun Write(buf: sizebuf_t, data: ByteArray) {
            val length = data.size()
            //memcpy(SZ_GetSpace(buf, length), data, length);
            System.arraycopy(data, 0, buf.data, GetSpace(buf, length), length)
        }

        //
        public fun Print(buf: sizebuf_t, data: String) {
            Com.dprintln("SZ.print():<" + data + ">")
            val length = data.length()
            val str = Lib.stringToBytes(data)

            if (buf.cursize != 0) {

                if (buf.data[buf.cursize - 1] != 0) {
                    //memcpy( SZ_GetSpace(buf, len), data, len); // no trailing 0
                    System.arraycopy(str, 0, buf.data, GetSpace(buf, length + 1), length)
                } else {
                    System.arraycopy(str, 0, buf.data, GetSpace(buf, length) - 1, length)
                    //memcpy(SZ_GetSpace(buf, len - 1) - 1, data, len); // write over trailing 0
                }
            } else
            // first print.
                System.arraycopy(str, 0, buf.data, GetSpace(buf, length), length)
            //memcpy(SZ_GetSpace(buf, len), data, len);

            buf.data[buf.cursize - 1] = 0
        }
    }
}
