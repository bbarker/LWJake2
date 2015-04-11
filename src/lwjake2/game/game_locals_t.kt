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

import lwjake2.Defines
import lwjake2.qcommon.Com
import lwjake2.util.QuakeFile

import java.io.IOException
import java.util.Date

public class game_locals_t {
    //
    //	this structure is left intact through an entire game
    //	it should be initialized at dll load time, and read/written to
    //	the server.ssv file for savegames
    //

    public var helpmessage1: String = ""

    public var helpmessage2: String = ""

    public var helpchanged: Int = 0 // flash F1 icon if non 0, play sound

    // and increment only if 1, 2, or 3

    public var clients: Array<gclient_t> = arrayOfNulls<gclient_t>(Defines.MAX_CLIENTS)

    // can't store spawnpoint in level, because
    // it would get overwritten by the savegame restore
    public var spawnpoint: String = "" // needed for coop respawns

    // store latched cvars here that we want to get at often
    public var maxclients: Int = 0

    public var maxentities: Int = 0

    // cross level triggers
    public var serverflags: Int = 0

    // items
    public var num_items: Int = 0

    public var autosaved: Boolean = false

    /** Reads the game locals from a file.  */
    throws(javaClass<IOException>())
    public fun load(f: QuakeFile) {
        f.readString() // Reads date?

        helpmessage1 = f.readString()
        helpmessage2 = f.readString()

        helpchanged = f.readInt()
        // gclient_t*

        spawnpoint = f.readString()
        maxclients = f.readInt()
        maxentities = f.readInt()
        serverflags = f.readInt()
        num_items = f.readInt()
        autosaved = f.readInt() != 0

        // rst's checker :-)
        if (f.readInt() != 1928)
            Com.DPrintf("error in loading game_locals, 1928\n")

    }

    /** Writes the game locals to a file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeString(Date().toString())

        f.writeString(helpmessage1)
        f.writeString(helpmessage2)

        f.writeInt(helpchanged)
        // gclient_t*

        f.writeString(spawnpoint)
        f.writeInt(maxclients)
        f.writeInt(maxentities)
        f.writeInt(serverflags)
        f.writeInt(num_items)
        f.writeInt(if (autosaved) 1 else 0)
        // rst's checker :-)
        f.writeInt(1928)
    }
}