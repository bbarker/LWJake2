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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest


public class MD4// Constructors
//...........................................................................

() : MessageDigest("MD4"), Cloneable {

    /**
     * 4 32-bit words (interim result)
     */
    private var context = IntArray(4)

    /**
     * Number of bytes processed so far mod. 2 power of 64.
     */
    private var count: Long = 0

    /**
     * 512 bits input buffer = 16 x 32-bit words holds until reaches 512 bits.
     */
    private var buffer = ByteArray(BLOCK_LENGTH)

    /**
     * 512 bits work buffer = 16 x 32-bit words
     */
    private val X = IntArray(16)
    {
        engineReset()
    }

    /**
     * This constructor is here to implement cloneability of this class.
     */
    private constructor(md: MD4) : this() {
        context = md.context.clone() as IntArray
        buffer = md.buffer.clone() as ByteArray
        count = md.count
    }

    // Cloneable method implementation
    //...........................................................................

    /**
     * Returns a copy of this MD object.
     */
    override fun clone(): Object {
        return MD4(this)
    }

    // JCE methods
    //...........................................................................

    /**
     * Resets this object disregarding any temporary data present at the
     * time of the invocation of this call.
     */
    override fun engineReset() {
        // initial values of MD4 i.e. A, B, C, D
        // as per rfc-1320; they are low-order byte first
        context[0] = 1732584193
        context[1] = -271733879
        context[2] = -1732584194
        context[3] = 271733878
        count = 0
        for (i in 0..BLOCK_LENGTH - 1)
            buffer[i] = 0
    }

    /**
     * Continues an MD4 message digest using the input byte.
     */
    override fun engineUpdate(b: Byte) {
        // compute number of bytes still unhashed; ie. present in buffer
        val i = (count % BLOCK_LENGTH.toLong()).toInt()
        count++ // update number of bytes
        buffer[i] = b
        if (i == BLOCK_LENGTH - 1)
            transform(buffer, 0)
    }

    /**
     * MD4 block update operation.
     *
     *
     * Continues an MD4 message digest operation, by filling the buffer,
     * transform(ing) data in 512-bit message block(s), updating the variables
     * context and count, and leaving (buffering) the remaining bytes in buffer
     * for the next update or finish.

     * @param    input    input block
    * *
     * @param    offset    start of meaningful bytes in input
    * *
     * @param    len        count of bytes in input block to consider
     */
    override fun engineUpdate(input: ByteArray, offset: Int, len: Int) {
        // make sure we don't exceed input's allocated size/length
        if (offset < 0 || len < 0 || offset.toLong() + len.toLong() > input.size())
            throw ArrayIndexOutOfBoundsException()

        // compute number of bytes still unhashed; ie. present in buffer
        var bufferNdx = (count % BLOCK_LENGTH.toLong()).toInt()
        count += len.toLong() // update number of bytes
        val partLen = BLOCK_LENGTH - bufferNdx
        var i = 0
        if (len >= partLen) {
            System.arraycopy(input, offset, buffer, bufferNdx, partLen)

            transform(buffer, 0)

            run {
                i = partLen
                while (i + BLOCK_LENGTH - 1 < len) {
                    transform(input, offset + i)
                    i += BLOCK_LENGTH
                }
            }
            bufferNdx = 0
        }
        // buffer remaining input
        if (i < len)
            System.arraycopy(input, offset + i, buffer, bufferNdx, len - i)
    }

    /**
     * Completes the hash computation by performing final operations such
     * as padding. At the return of this engineDigest, the MD engine is
     * reset.

     * @return the array of bytes for the resulting hash value.
     */
    override fun engineDigest(): ByteArray {
        // pad output to 56 mod 64; as RFC1320 puts it: congruent to 448 mod 512
        val bufferNdx = (count % BLOCK_LENGTH.toLong()).toInt()
        val padLen = if ((bufferNdx < 56)) (56 - bufferNdx) else (120 - bufferNdx)

        // padding is alwas binary 1 followed by binary 0s
        val tail = ByteArray(padLen + 8)
        tail[0] = 128.toByte()

        // append length before final transform:
        // save number of bits, casting the long to an array of 8 bytes
        // save low-order byte first.
        for (i in 0..8 - 1)
            tail[padLen + i] = ((count * 8).ushr((8 * i).toLong())).toByte()

        engineUpdate(tail, 0, tail.size())

        val result = ByteArray(16)
        // cast this MD4's context (array of 4 ints) into an array of 16 bytes.
        for (i in 0..4 - 1)
            for (j in 0..4 - 1)
                result[i * 4 + j] = (context[i].ushr((8 * j))).toByte()

        // reset the engine
        engineReset()
        return result
    }

    // own methods
    //...........................................................................

    /**
     * MD4 basic transformation.
     *
     *
     * Transforms context based on 512 bits from input block starting
     * from the offset'th byte.

     * @param    block    input sub-array.
    * *
     * @param    offset    starting position of sub-array.
     */
    private fun transform(block: ByteArray, offset: Int) {
        var offset = offset

        // encodes 64 bytes from input block into an array of 16 32-bit
        // entities. Use A as a temp var.
        for (i in 0..16 - 1)
            X[i] = (block[offset++] and 255) or (block[offset++] and 255) shl 8 or (block[offset++] and 255) shl 16 or (block[offset++] and 255) shl 24

        var A = context[0]
        var B = context[1]
        var C = context[2]
        var D = context[3]

        A = FF(A, B, C, D, X[0], 3)
        D = FF(D, A, B, C, X[1], 7)
        C = FF(C, D, A, B, X[2], 11)
        B = FF(B, C, D, A, X[3], 19)
        A = FF(A, B, C, D, X[4], 3)
        D = FF(D, A, B, C, X[5], 7)
        C = FF(C, D, A, B, X[6], 11)
        B = FF(B, C, D, A, X[7], 19)
        A = FF(A, B, C, D, X[8], 3)
        D = FF(D, A, B, C, X[9], 7)
        C = FF(C, D, A, B, X[10], 11)
        B = FF(B, C, D, A, X[11], 19)
        A = FF(A, B, C, D, X[12], 3)
        D = FF(D, A, B, C, X[13], 7)
        C = FF(C, D, A, B, X[14], 11)
        B = FF(B, C, D, A, X[15], 19)

        A = GG(A, B, C, D, X[0], 3)
        D = GG(D, A, B, C, X[4], 5)
        C = GG(C, D, A, B, X[8], 9)
        B = GG(B, C, D, A, X[12], 13)
        A = GG(A, B, C, D, X[1], 3)
        D = GG(D, A, B, C, X[5], 5)
        C = GG(C, D, A, B, X[9], 9)
        B = GG(B, C, D, A, X[13], 13)
        A = GG(A, B, C, D, X[2], 3)
        D = GG(D, A, B, C, X[6], 5)
        C = GG(C, D, A, B, X[10], 9)
        B = GG(B, C, D, A, X[14], 13)
        A = GG(A, B, C, D, X[3], 3)
        D = GG(D, A, B, C, X[7], 5)
        C = GG(C, D, A, B, X[11], 9)
        B = GG(B, C, D, A, X[15], 13)

        A = HH(A, B, C, D, X[0], 3)
        D = HH(D, A, B, C, X[8], 9)
        C = HH(C, D, A, B, X[4], 11)
        B = HH(B, C, D, A, X[12], 15)
        A = HH(A, B, C, D, X[2], 3)
        D = HH(D, A, B, C, X[10], 9)
        C = HH(C, D, A, B, X[6], 11)
        B = HH(B, C, D, A, X[14], 15)
        A = HH(A, B, C, D, X[1], 3)
        D = HH(D, A, B, C, X[9], 9)
        C = HH(C, D, A, B, X[5], 11)
        B = HH(B, C, D, A, X[13], 15)
        A = HH(A, B, C, D, X[3], 3)
        D = HH(D, A, B, C, X[11], 9)
        C = HH(C, D, A, B, X[7], 11)
        B = HH(B, C, D, A, X[15], 15)

        context[0] += A
        context[1] += B
        context[2] += C
        context[3] += D
    }

    // The basic MD4 atomic functions.

    private fun FF(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int): Int {
        val t = a + ((b and c) or (b.inv() and d)) + x
        return t shl s or t.ushr((32 - s))
    }

    private fun GG(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int): Int {
        val t = a + ((b and (c or d)) or (c and d)) + x + 1518500249
        return t shl s or t.ushr((32 - s))
    }

    private fun HH(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int): Int {
        val t = a + (b xor c xor d) + x + 1859775393
        return t shl s or t.ushr((32 - s))
    }

    companion object {
        // MD4 specific object variables
        //...........................................................................

        /**
         * The size in bytes of the input block to the tranformation algorithm.
         */
        private val BLOCK_LENGTH = 64 //    = 512 / 8;

        /**
         * Bugfixed, now works prima (RST).
         */
        public fun Com_BlockChecksum(buffer: ByteArray, length: Int): Int {

            val `val`: Int
            val md4 = MD4()

            md4.engineUpdate(buffer, 0, length)
            val data = md4.engineDigest()
            val bb = ByteBuffer.wrap(data)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            `val` = bb.getInt() xor bb.getInt() xor bb.getInt() xor bb.getInt()
            return `val`
        }
    }
}
