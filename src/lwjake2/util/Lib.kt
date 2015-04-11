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

package lwjake2.util

import lwjake2.Globals
import lwjake2.qcommon.Com
import lwjake2.qcommon.FS

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

public class Lib {
    companion object {


        /** Converts a vector to a string.  */
        public fun vtos(v: FloatArray): String {
            return v[0].toInt() + " " + v[1].toInt() + " " + v[2].toInt()
        }

        /** Converts a vector to a string.  */
        public fun vtofs(v: FloatArray): String {
            return v[0] + " " + v[1] + " " + v[2]
        }

        /** Converts a vector to a beatiful string.  */
        public fun vtofsbeaty(v: FloatArray): String {
            return Com.sprintf("%8.2f %8.2f %8.2f", Vargs().add(v[0]).add(v[1]).add(v[2]))
        }

        /** Like in  libc.  */
        public fun rand(): Short {
            return Globals.rnd.nextInt(Short.MAX_VALUE + 1) as Short
        }

        /** Like in libc.  */
        public fun crandom(): Float {
            return (Globals.rnd.nextFloat() - 0.5.toFloat()) * 2.0.toFloat()
        }

        /** Like in libc.  */
        public fun random(): Float {
            return Globals.rnd.nextFloat()
        }

        /** Like in libc.  */
        public fun crand(): Float {
            return (Globals.rnd.nextFloat() - 0.5.toFloat()) * 2.0.toFloat()
        }

        /** Like in libc.  */
        public fun strcmp(in1: String, in2: String): Int {
            return in1.compareTo(in2)
        }

        /** Like in libc.  */
        public fun atof(`in`: String): Float {
            var res: Float = 0

            try {
                res = Float.parseFloat(`in`)
            } catch (e: Exception) {
            }


            return res
        }

        /** Like in quake2.  */
        public fun Q_stricmp(in1: String, in2: String): Int {
            return in1.compareToIgnoreCase(in2)
        }

        /** Like in libc.  */
        public fun atoi(`in`: String): Int {
            try {
                return Integer.parseInt(`in`)
            } catch (e: Exception) {
                try {
                    return Double.parseDouble(`in`) as Int
                } catch (e1: Exception) {
                    return 0
                }

            }

        }

        /** Converts a string to a vector. Needs improvement.  */
        public fun atov(v: String): FloatArray {
            val res = floatArray(0.0, 0.0, 0.0)
            val strres = v.split(" ")
            run {
                var n = 0
                while (n < 3 && n < strres.size()) {
                    res[n] = atof(strres[n])
                    n++
                }
            }
            return res
        }

        /** Like in libc.  */
        public fun strlen(`in`: CharArray): Int {
            for (i in `in`.indices)
                if (`in`[i] == 0)
                    return i
            return `in`.size()
        }

        /** Like in libc.  */
        public fun strlen(`in`: ByteArray): Int {
            for (i in `in`.indices)
                if (`in`[i] == 0)
                    return i
            return `in`.size()
        }

        /** Converts memory to a memory dump string.  */
        throws(javaClass<IOException>())
        public fun hexdumpfile(bb: ByteBuffer, len: Int): String {

            val bb1 = bb.slice()

            val buf = ByteArray(len)

            bb1.get(buf)

            return hexDump(buf, len, false)
        }

        /** Converts memory to a memory dump string.  */
        public fun hexDump(data1: ByteArray, len: Int, showAddress: Boolean): String {
            val result = StringBuffer()
            val charfield = StringBuffer()
            var i = 0
            while (i < len) {
                if ((i and 15) == 0) {
                    if (showAddress) {
                        var address = Integer.toHexString(i)
                        address = ("0000".substring(0, 4 - address.length()) + address).toUpperCase()
                        result.append(address + ": ")
                    }
                }
                val v = data1[i].toInt()

                result.append(hex2(v))
                result.append(" ")

                charfield.append(readableChar(v))
                i++

                // nach dem letzten, newline einfuegen
                if ((i and 15) == 0) {
                    result.append(charfield)
                    result.append("\n")
                    charfield.setLength(0)
                } else if ((i and 15) == 8) {
                    result.append(" ")
                }//	in der Mitte ein Luecke einfuegen ?
            }
            return result.toString()
        }

        /** Formats an hex byte.  */
        public fun hex2(i: Int): String {
            val `val` = Integer.toHexString(i and 255)
            return ("00".substring(0, 2 - `val`.length()) + `val`).toUpperCase()
        }

        /** Returns true if the char is alphanumeric.  */
        public fun readableChar(i: Int): Char {
            if ((i < 32) || (i > 127))
                return '.'
            else
                return i.toChar()
        }

        /** Prints a vector to the quake console.  */
        public fun printv(`in`: String, arr: FloatArray) {
            for (n in arr.indices) {
                Com.Println(`in` + "[" + n + "]: " + arr[n])
            }
        }

        val nullfiller = ByteArray(8192)

        /** Like in libc.  */
        throws(javaClass<IOException>())
        public fun fwriteString(s: String?, len: Int, f: RandomAccessFile) {
            if (s == null)
                return
            val diff = len - s.length()
            if (diff > 0) {
                f.write(stringToBytes(s))

                f.write(nullfiller, 0, diff)
            } else
                f.write(stringToBytes(s), 0, len)
        }

        /** Like in libc  */
        public fun fopen(name: String, mode: String): RandomAccessFile? {
            try {
                return RandomAccessFile(name, mode)
            } catch (e: Exception) {
                Com.DPrintf("Could not open file:" + name)
                return null
            }

        }

        /** Like in libc  */
        public fun fclose(f: RandomAccessFile) {
            try {
                f.close()
            } catch (e: Exception) {
            }

        }

        /** Like in libc  */
        public fun freadString(f: RandomAccessFile, len: Int): String {
            val buffer = ByteArray(len)
            FS.Read(buffer, len, f)

            return Lib.CtoJava(buffer)
        }

        /** Returns the right part of the string from the last occruence of c.  */
        public fun rightFrom(`in`: String, c: Char): String {
            val pos = `in`.lastIndexOf(c)
            if (pos == -1)
                return ""
            else if (pos < `in`.length())
                return `in`.substring(pos + 1, `in`.length())
            return ""
        }

        /** Returns the left part of the string from the last occruence of c.  */
        public fun leftFrom(`in`: String, c: Char): String {
            val pos = `in`.lastIndexOf(c)
            if (pos == -1)
                return ""
            else if (pos < `in`.length())
                return `in`.substring(0, pos)
            return ""
        }

        /** Renames a file.  */
        public fun rename(oldn: String, newn: String): Int {
            try {
                val f1 = File(oldn)
                val f2 = File(newn)
                f1.renameTo(f2)
                return 0
            } catch (e: Exception) {
                return 1
            }

        }

        /** Converts an int to 4 bytes java representation.  */
        public fun getIntBytes(c: Int): ByteArray {
            val b = ByteArray(4)
            b[0] = ((c and 255)).toByte()
            b[1] = ((c.ushr(8)) and 255).toByte()
            b[2] = ((c.ushr(16)) and 255).toByte()
            b[3] = ((c.ushr(24)) and 255).toByte()
            return b
        }

        /** Converts an 4 bytes java int representation to an int.  */
        public fun getInt(b: ByteArray): Int {
            return (b[0] and 255) or ((b[1] and 255) shl 8) or ((b[2] and 255) shl 16) or ((b[3] and 255) shl 24)
        }

        /** Duplicates a float array.  */
        public fun clone(`in`: FloatArray): FloatArray {
            val out = FloatArray(`in`.size())

            if (`in`.size() != 0)
                System.arraycopy(`in`, 0, out, 0, `in`.size())

            return out
        }

        /**
         * convert a java string to byte[] with 8bit latin 1

         * avoid String.getBytes() because it is using system specific character encoding.
         */
        public fun stringToBytes(value: String): ByteArray? {
            try {
                return value.getBytes("ISO-8859-1")
            } catch (e: UnsupportedEncodingException) {
                // can't happen: Latin 1 is a standard encoding
                return null
            }

        }

        /**
         * convert a byte[] with 8bit latin 1 to java string

         * avoid new String(bytes) because it is using system specific character encoding.
         */
        public fun bytesToString(value: ByteArray): String? {
            try {
                return String(value, "ISO-8859-1")
            } catch (e: UnsupportedEncodingException) {
                // can't happen: Latin 1 is a standard encoding
                return null
            }

        }

        /** Helper method that savely handles the null termination of old C String data.  */
        public fun CtoJava(old: String): String {
            val index = old.indexOf('\0')
            if (index == 0) return ""
            return if ((index > 0)) old.substring(0, index) else old
        }

        /** Helper method that savely handles the null termination of old C String data.  */
        public fun CtoJava(old: ByteArray): String {
            return CtoJava(old, 0, old.size())
        }

        /** Helper method that savely handles the null termination of old C String data.  */
        public fun CtoJava(old: ByteArray, offset: Int, maxLenght: Int): String {
            if (old.size() == 0 || old[0] == 0) return ""
            var i: Int
            run {
                i = offset
                while ((i - offset) < maxLenght && old[i] != 0) {
                    i++
                }
            }
            return String(old, offset, i - offset)
        }


        /* java.nio.* Buffer util functions */

        public val SIZEOF_FLOAT: Int = 4
        public val SIZEOF_INT: Int = 4

        public fun newFloatBuffer(numElements: Int): FloatBuffer {
            val bb = newByteBuffer(numElements * SIZEOF_FLOAT)
            return bb.asFloatBuffer()
        }

        public fun newFloatBuffer(numElements: Int, order: ByteOrder): FloatBuffer {
            val bb = newByteBuffer(numElements * SIZEOF_FLOAT, order)
            return bb.asFloatBuffer()
        }

        public fun newIntBuffer(numElements: Int): IntBuffer {
            val bb = newByteBuffer(numElements * SIZEOF_INT)
            return bb.asIntBuffer()
        }

        public fun newIntBuffer(numElements: Int, order: ByteOrder): IntBuffer {
            val bb = newByteBuffer(numElements * SIZEOF_INT, order)
            return bb.asIntBuffer()
        }

        public fun newByteBuffer(numElements: Int): ByteBuffer {
            val bb = ByteBuffer.allocateDirect(numElements)
            bb.order(ByteOrder.nativeOrder())
            return bb
        }

        public fun newByteBuffer(numElements: Int, order: ByteOrder): ByteBuffer {
            val bb = ByteBuffer.allocateDirect(numElements)
            bb.order(order)
            return bb
        }
    }
}
