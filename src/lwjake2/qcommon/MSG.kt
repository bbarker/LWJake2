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

import lwjake2.Globals
import lwjake2.game.entity_state_t
import lwjake2.game.usercmd_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class MSG : Globals() {
    companion object {

        //
        // writing functions
        //

        //ok.
        public fun WriteChar(sb: sizebuf_t, c: Int) {
            sb.data[SZ.GetSpace(sb, 1)] = (c and 255).toByte()
        }

        //ok.
        public fun WriteChar(sb: sizebuf_t, c: Float) {

            WriteChar(sb, c.toInt())
        }

        //ok.
        public fun WriteByte(sb: sizebuf_t, c: Int) {
            sb.data[SZ.GetSpace(sb, 1)] = (c and 255).toByte()
        }

        //ok.
        public fun WriteByte(sb: sizebuf_t, c: Float) {
            WriteByte(sb, c.toInt())
        }

        public fun WriteShort(sb: sizebuf_t, c: Int) {
            var i = SZ.GetSpace(sb, 2)
            sb.data[i++] = (c and 255).toByte()
            sb.data[i] = ((c.ushr(8)) and 255).toByte()
        }

        //ok.
        public fun WriteInt(sb: sizebuf_t, c: Int) {
            var i = SZ.GetSpace(sb, 4)
            sb.data[i++] = ((c and 255)).toByte()
            sb.data[i++] = ((c.ushr(8)) and 255).toByte()
            sb.data[i++] = ((c.ushr(16)) and 255).toByte()
            sb.data[i++] = ((c.ushr(24)) and 255).toByte()
        }

        //ok.
        public fun WriteLong(sb: sizebuf_t, c: Int) {
            WriteInt(sb, c)
        }

        //ok.
        public fun WriteFloat(sb: sizebuf_t, f: Float) {
            WriteInt(sb, Float.floatToIntBits(f))
        }

        // had a bug, now its ok.
        public fun WriteString(sb: sizebuf_t, s: String?) {
            var x: String = s

            if (s == null)
                x = ""

            SZ.Write(sb, Lib.stringToBytes(x))
            WriteByte(sb, 0)
            //Com.dprintln("MSG.WriteString:" + s.replace('\0', '@'));
        }

        //ok.
        public fun WriteString(sb: sizebuf_t, s: ByteArray) {
            WriteString(sb, String(s).trim())
        }

        public fun WriteCoord(sb: sizebuf_t, f: Float) {
            WriteShort(sb, (f * 8).toInt())
        }

        public fun WritePos(sb: sizebuf_t, pos: FloatArray) {
            assert((pos.size() == 3), "vec3_t bug")
            WriteShort(sb, (pos[0] * 8).toInt())
            WriteShort(sb, (pos[1] * 8).toInt())
            WriteShort(sb, (pos[2] * 8).toInt())
        }

        public fun WriteAngle(sb: sizebuf_t, f: Float) {
            WriteByte(sb, (f * 256 / 360).toInt() and 255)
        }

        public fun WriteAngle16(sb: sizebuf_t, f: Float) {
            WriteShort(sb, Math3D.ANGLE2SHORT(f))
        }

        public fun WriteDeltaUsercmd(buf: sizebuf_t, from: usercmd_t, cmd: usercmd_t) {
            var bits: Int

            //
            // send the movement message
            //
            bits = 0
            if (cmd.angles[0] != from.angles[0])
                bits = bits or CM_ANGLE1
            if (cmd.angles[1] != from.angles[1])
                bits = bits or CM_ANGLE2
            if (cmd.angles[2] != from.angles[2])
                bits = bits or CM_ANGLE3
            if (cmd.forwardmove != from.forwardmove)
                bits = bits or CM_FORWARD
            if (cmd.sidemove != from.sidemove)
                bits = bits or CM_SIDE
            if (cmd.upmove != from.upmove)
                bits = bits or CM_UP
            if (cmd.buttons != from.buttons)
                bits = bits or CM_BUTTONS
            if (cmd.impulse != from.impulse)
                bits = bits or CM_IMPULSE

            WriteByte(buf, bits)

            if ((bits and CM_ANGLE1) != 0)
                WriteShort(buf, cmd.angles[0])
            if ((bits and CM_ANGLE2) != 0)
                WriteShort(buf, cmd.angles[1])
            if ((bits and CM_ANGLE3) != 0)
                WriteShort(buf, cmd.angles[2])

            if ((bits and CM_FORWARD) != 0)
                WriteShort(buf, cmd.forwardmove)
            if ((bits and CM_SIDE) != 0)
                WriteShort(buf, cmd.sidemove)
            if ((bits and CM_UP) != 0)
                WriteShort(buf, cmd.upmove)

            if ((bits and CM_BUTTONS) != 0)
                WriteByte(buf, cmd.buttons)
            if ((bits and CM_IMPULSE) != 0)
                WriteByte(buf, cmd.impulse)

            WriteByte(buf, cmd.msec)
            WriteByte(buf, cmd.lightlevel)
        }

        //should be ok.
        public fun WriteDir(sb: sizebuf_t, dir: FloatArray?) {
            var i: Int
            var best: Int
            var d: Float
            var bestd: Float

            if (dir == null) {
                WriteByte(sb, 0)
                return
            }

            bestd = 0
            best = 0
            run {
                i = 0
                while (i < NUMVERTEXNORMALS) {
                    d = Math3D.DotProduct(dir, bytedirs[i])
                    if (d > bestd) {
                        bestd = d
                        best = i
                    }
                    i++
                }
            }
            WriteByte(sb, best)
        }

        //should be ok.
        public fun ReadDir(sb: sizebuf_t, dir: FloatArray) {
            val b: Int

            b = ReadByte(sb)
            if (b >= NUMVERTEXNORMALS)
                Com.Error(ERR_DROP, "MSF_ReadDir: out of range")
            Math3D.VectorCopy(bytedirs[b], dir)
        }

        /*
     * ================== WriteDeltaEntity
     * 
     * Writes part of a packetentities message. Can delta from either a baseline
     * or a previous packet_entity ==================
     */
        public fun WriteDeltaEntity(from: entity_state_t, to: entity_state_t, msg: sizebuf_t, force: Boolean, newentity: Boolean) {
            var bits: Int

            if (0 == to.number)
                Com.Error(ERR_FATAL, "Unset entity number")
            if (to.number >= MAX_EDICTS)
                Com.Error(ERR_FATAL, "Entity number >= MAX_EDICTS")

            // send an update
            bits = 0

            if (to.number >= 256)
                bits = bits or U_NUMBER16 // number8 is implicit otherwise

            if (to.origin[0] != from.origin[0])
                bits = bits or U_ORIGIN1
            if (to.origin[1] != from.origin[1])
                bits = bits or U_ORIGIN2
            if (to.origin[2] != from.origin[2])
                bits = bits or U_ORIGIN3

            if (to.angles[0] != from.angles[0])
                bits = bits or U_ANGLE1
            if (to.angles[1] != from.angles[1])
                bits = bits or U_ANGLE2
            if (to.angles[2] != from.angles[2])
                bits = bits or U_ANGLE3

            if (to.skinnum != from.skinnum) {
                if (to.skinnum < 256)
                    bits = bits or U_SKIN8
                else if (to.skinnum < 65536)
                    bits = bits or U_SKIN16
                else
                    bits = bits or (U_SKIN8 or U_SKIN16)
            }

            if (to.frame != from.frame) {
                if (to.frame < 256)
                    bits = bits or U_FRAME8
                else
                    bits = bits or U_FRAME16
            }

            if (to.effects != from.effects) {
                if (to.effects < 256)
                    bits = bits or U_EFFECTS8
                else if (to.effects < 32768)
                    bits = bits or U_EFFECTS16
                else
                    bits = bits or (U_EFFECTS8 or U_EFFECTS16)
            }

            if (to.renderfx != from.renderfx) {
                if (to.renderfx < 256)
                    bits = bits or U_RENDERFX8
                else if (to.renderfx < 32768)
                    bits = bits or U_RENDERFX16
                else
                    bits = bits or (U_RENDERFX8 or U_RENDERFX16)
            }

            if (to.solid != from.solid)
                bits = bits or U_SOLID

            // event is not delta compressed, just 0 compressed
            if (to.event != 0)
                bits = bits or U_EVENT

            if (to.modelindex != from.modelindex)
                bits = bits or U_MODEL
            if (to.modelindex2 != from.modelindex2)
                bits = bits or U_MODEL2
            if (to.modelindex3 != from.modelindex3)
                bits = bits or U_MODEL3
            if (to.modelindex4 != from.modelindex4)
                bits = bits or U_MODEL4

            if (to.sound != from.sound)
                bits = bits or U_SOUND

            if (newentity || (to.renderfx and RF_BEAM) != 0)
                bits = bits or U_OLDORIGIN

            //
            // write the message
            //
            if (bits == 0 && !force)
                return  // nothing to send!

            //----------

            if ((bits and -16777216) != 0)
                bits = bits or U_MOREBITS3 or U_MOREBITS2 or U_MOREBITS1
            else if ((bits and 16711680) != 0)
                bits = bits or (U_MOREBITS2 or U_MOREBITS1)
            else if ((bits and 65280) != 0)
                bits = bits or U_MOREBITS1

            WriteByte(msg, bits and 255)

            if ((bits and -16777216) != 0) {
                WriteByte(msg, (bits.ushr(8)) and 255)
                WriteByte(msg, (bits.ushr(16)) and 255)
                WriteByte(msg, (bits.ushr(24)) and 255)
            } else if ((bits and 16711680) != 0) {
                WriteByte(msg, (bits.ushr(8)) and 255)
                WriteByte(msg, (bits.ushr(16)) and 255)
            } else if ((bits and 65280) != 0) {
                WriteByte(msg, (bits.ushr(8)) and 255)
            }

            //----------

            if ((bits and U_NUMBER16) != 0)
                WriteShort(msg, to.number)
            else
                WriteByte(msg, to.number)

            if ((bits and U_MODEL) != 0)
                WriteByte(msg, to.modelindex)
            if ((bits and U_MODEL2) != 0)
                WriteByte(msg, to.modelindex2)
            if ((bits and U_MODEL3) != 0)
                WriteByte(msg, to.modelindex3)
            if ((bits and U_MODEL4) != 0)
                WriteByte(msg, to.modelindex4)

            if ((bits and U_FRAME8) != 0)
                WriteByte(msg, to.frame)
            if ((bits and U_FRAME16) != 0)
                WriteShort(msg, to.frame)

            if ((bits and U_SKIN8) != 0 && (bits and U_SKIN16) != 0)
            //used for laser
            // colors
                WriteInt(msg, to.skinnum)
            else if ((bits and U_SKIN8) != 0)
                WriteByte(msg, to.skinnum)
            else if ((bits and U_SKIN16) != 0)
                WriteShort(msg, to.skinnum)

            if ((bits and (U_EFFECTS8 or U_EFFECTS16)) == (U_EFFECTS8 or U_EFFECTS16))
                WriteInt(msg, to.effects)
            else if ((bits and U_EFFECTS8) != 0)
                WriteByte(msg, to.effects)
            else if ((bits and U_EFFECTS16) != 0)
                WriteShort(msg, to.effects)

            if ((bits and (U_RENDERFX8 or U_RENDERFX16)) == (U_RENDERFX8 or U_RENDERFX16))
                WriteInt(msg, to.renderfx)
            else if ((bits and U_RENDERFX8) != 0)
                WriteByte(msg, to.renderfx)
            else if ((bits and U_RENDERFX16) != 0)
                WriteShort(msg, to.renderfx)

            if ((bits and U_ORIGIN1) != 0)
                WriteCoord(msg, to.origin[0])
            if ((bits and U_ORIGIN2) != 0)
                WriteCoord(msg, to.origin[1])
            if ((bits and U_ORIGIN3) != 0)
                WriteCoord(msg, to.origin[2])

            if ((bits and U_ANGLE1) != 0)
                WriteAngle(msg, to.angles[0])
            if ((bits and U_ANGLE2) != 0)
                WriteAngle(msg, to.angles[1])
            if ((bits and U_ANGLE3) != 0)
                WriteAngle(msg, to.angles[2])

            if ((bits and U_OLDORIGIN) != 0) {
                WriteCoord(msg, to.old_origin[0])
                WriteCoord(msg, to.old_origin[1])
                WriteCoord(msg, to.old_origin[2])
            }

            if ((bits and U_SOUND) != 0)
                WriteByte(msg, to.sound)
            if ((bits and U_EVENT) != 0)
                WriteByte(msg, to.event)
            if ((bits and U_SOLID) != 0)
                WriteShort(msg, to.solid)
        }

        //============================================================

        //
        // reading functions
        //

        public fun BeginReading(msg: sizebuf_t) {
            msg.readcount = 0
        }

        // returns -1 if no more characters are available, but also [-128 , 127]
        public fun ReadChar(msg_read: sizebuf_t): Int {
            val c: Int

            if (msg_read.readcount + 1 > msg_read.cursize)
                c = -1
            else
                c = msg_read.data[msg_read.readcount]
            msg_read.readcount++
            // kickangles bugfix (rst)
            return c
        }

        public fun ReadByte(msg_read: sizebuf_t): Int {
            val c: Int

            if (msg_read.readcount + 1 > msg_read.cursize)
                c = -1
            else
                c = msg_read.data[msg_read.readcount] and 255

            msg_read.readcount++

            return c
        }

        public fun ReadShort(msg_read: sizebuf_t): Short {
            val c: Int

            if (msg_read.readcount + 2 > msg_read.cursize)
                c = -1
            else
                c = (((msg_read.data[msg_read.readcount] and 255) + (msg_read.data[msg_read.readcount + 1] shl 8)) as Short).toInt()

            msg_read.readcount += 2

            return c.toShort()
        }

        public fun ReadLong(msg_read: sizebuf_t): Int {
            val c: Int

            if (msg_read.readcount + 4 > msg_read.cursize) {
                Com.Printf("buffer underrun in ReadLong!")
                c = -1
            } else
                c = (msg_read.data[msg_read.readcount] and 255) or ((msg_read.data[msg_read.readcount + 1] and 255) shl 8) or ((msg_read.data[msg_read.readcount + 2] and 255) shl 16) or ((msg_read.data[msg_read.readcount + 3] and 255) shl 24)

            msg_read.readcount += 4

            return c
        }

        public fun ReadFloat(msg_read: sizebuf_t): Float {
            val n = ReadLong(msg_read)
            return Float.intBitsToFloat(n)
        }

        // 2k read buffer.
        public var readbuf: ByteArray = ByteArray(2048)

        public fun ReadString(msg_read: sizebuf_t): String {

            val c: Byte
            var l = 0
            do {
                c = ReadByte(msg_read).toByte()
                if (c == -1 || c == 0)
                    break

                readbuf[l] = c
                l++
            } while (l < 2047)

            val ret = String(readbuf, 0, l)
            // Com.dprintln("MSG.ReadString:[" + ret + "]");
            return ret
        }

        public fun ReadStringLine(msg_read: sizebuf_t): String {

            var l: Int
            val c: Byte

            l = 0
            do {
                c = ReadChar(msg_read).toByte()
                if (c == -1 || c == 0 || c == 10)
                    break
                readbuf[l] = c
                l++
            } while (l < 2047)

            val ret = String(readbuf, 0, l).trim()
            Com.dprintln("MSG.ReadStringLine:[" + ret.replace('\0', '@') + "]")
            return ret
        }

        public fun ReadCoord(msg_read: sizebuf_t): Float {
            return ReadShort(msg_read).toFloat() * (1.0.toFloat() / 8)
        }

        public fun ReadPos(msg_read: sizebuf_t, pos: FloatArray) {
            assert((pos.size() == 3), "vec3_t bug")
            pos[0] = ReadShort(msg_read).toFloat() * (1.0.toFloat() / 8)
            pos[1] = ReadShort(msg_read).toFloat() * (1.0.toFloat() / 8)
            pos[2] = ReadShort(msg_read).toFloat() * (1.0.toFloat() / 8)
        }

        public fun ReadAngle(msg_read: sizebuf_t): Float {
            return ReadChar(msg_read).toFloat() * (360.0.toFloat() / 256)
        }

        public fun ReadAngle16(msg_read: sizebuf_t): Float {
            return Math3D.SHORT2ANGLE(ReadShort(msg_read))
        }

        public fun ReadDeltaUsercmd(msg_read: sizebuf_t, from: usercmd_t, move: usercmd_t) {
            val bits: Int

            //memcpy(move, from, sizeof(* move));
            // IMPORTANT!! copy without new
            move.set(from)
            bits = ReadByte(msg_read)

            // read current angles
            if ((bits and CM_ANGLE1) != 0)
                move.angles[0] = ReadShort(msg_read)
            if ((bits and CM_ANGLE2) != 0)
                move.angles[1] = ReadShort(msg_read)
            if ((bits and CM_ANGLE3) != 0)
                move.angles[2] = ReadShort(msg_read)

            // read movement
            if ((bits and CM_FORWARD) != 0)
                move.forwardmove = ReadShort(msg_read)
            if ((bits and CM_SIDE) != 0)
                move.sidemove = ReadShort(msg_read)
            if ((bits and CM_UP) != 0)
                move.upmove = ReadShort(msg_read)

            // read buttons
            if ((bits and CM_BUTTONS) != 0)
                move.buttons = ReadByte(msg_read).toByte()

            if ((bits and CM_IMPULSE) != 0)
                move.impulse = ReadByte(msg_read).toByte()

            // read time to run command
            move.msec = ReadByte(msg_read).toByte()

            // read the light level
            move.lightlevel = ReadByte(msg_read).toByte()

        }

        public fun ReadData(msg_read: sizebuf_t, data: ByteArray, len: Int) {
            for (i in 0..len - 1)
                data[i] = ReadByte(msg_read).toByte()
        }
    }

}