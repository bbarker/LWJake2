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

import lwjake2.game.GameBase
import lwjake2.game.GameItemList
import lwjake2.game.SuperAdapter
import lwjake2.game.edict_t
import lwjake2.game.gitem_t
import lwjake2.qcommon.Com

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

/**
 * RandomAccessFile, but handles readString/WriteString specially and offers
 * other helper functions
 */
public class QuakeFile
/** Standard Constructor.  */
[throws(javaClass<FileNotFoundException>())]
(filename: String, mode: String) : RandomAccessFile(filename, mode) {

    /** Writes a Vector to a RandomAccessFile.  */
    throws(javaClass<IOException>())
    public fun writeVector(v: FloatArray) {
        for (n in 0..3 - 1)
            writeFloat(v[n])
    }

    /** Writes a Vector to a RandomAccessFile.  */
    throws(javaClass<IOException>())
    public fun readVector(): FloatArray {
        val res = floatArray(0.0, 0.0, 0.0)
        for (n in 0..3 - 1)
            res[n] = readFloat()

        return res
    }

    /** Reads a length specified string from a file.  */
    throws(javaClass<IOException>())
    public fun readString(): String? {
        val len = readInt()

        if (len == -1)
            return null

        if (len == 0)
            return ""

        val bb = ByteArray(len)

        super.read(bb, 0, len)

        return String(bb, 0, len)
    }

    /** Writes a length specified string to a file.  */
    throws(javaClass<IOException>())
    public fun writeString(s: String?) {
        if (s == null) {
            writeInt(-1)
            return
        }

        writeInt(s.length())
        if (s.length() != 0)
            writeBytes(s)
    }

    /** Writes the edict reference.  */
    throws(javaClass<IOException>())
    public fun writeEdictRef(ent: edict_t?) {
        if (ent == null)
            writeInt(-1)
        else {
            writeInt(ent!!.s.number)
        }
    }

    /**
     * Reads an edict index from a file and returns the edict.
     */

    throws(javaClass<IOException>())
    public fun readEdictRef(): edict_t? {
        val i = readInt()

        // handle -1
        if (i < 0)
            return null

        if (i > GameBase.g_edicts.length) {
            Com.DPrintf("jake2: illegal edict num:" + i + "\n")
            return null
        }

        // valid edict.
        return GameBase.g_edicts[i]
    }

    /** Writes the Adapter-ID to the file.  */
    throws(javaClass<IOException>())
    public fun writeAdapter(a: SuperAdapter?) {
        writeInt(3988)
        if (a == null)
            writeString(null)
        else {
            val str = a!!.getID()
            if (str == null) {
                Com.DPrintf("writeAdapter: invalid Adapter id for " + a + "\n")
            }
            writeString(str)
        }
    }

    /** Reads the adapter id and returns the adapter.  */
    throws(javaClass<IOException>())
    public fun readAdapter(): SuperAdapter? {
        if (readInt() != 3988)
            Com.DPrintf("wrong read position: readadapter 3988 \n")

        val id = readString()

        if (id == null) {
            // null adapter. :-)
            return null
        }

        return SuperAdapter.getFromID(id)
    }

    /** Writes an item reference.  */
    throws(javaClass<IOException>())
    public fun writeItem(item: gitem_t?) {
        if (item == null)
            writeInt(-1)
        else
            writeInt(item!!.index)
    }

    /** Reads the item index and returns the game item.  */
    throws(javaClass<IOException>())
    public fun readItem(): gitem_t? {
        val ndx = readInt()
        if (ndx == -1)
            return null
        else
            return GameItemList.itemlist[ndx]
    }

}