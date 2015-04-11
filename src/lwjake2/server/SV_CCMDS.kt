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

package lwjake2.server

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.EndianHandler
import lwjake2.game.GameSVCmds
import lwjake2.game.GameSave
import lwjake2.game.Info
import lwjake2.game.cvar_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.Netchan
import lwjake2.qcommon.SZ
import lwjake2.qcommon.netadr_t
import lwjake2.qcommon.sizebuf_t
import lwjake2.qcommon.xcommand_t
import lwjake2.sys.NET
import lwjake2.sys.Sys
import lwjake2.util.Lib
import lwjake2.util.QuakeFile
import lwjake2.util.Vargs

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Calendar

public class SV_CCMDS {
    companion object {

        /*
	===============================================================================
	
	OPERATOR CONSOLE ONLY COMMANDS
	
	These commands can only be entered from stdin or by a remote operator datagram
	===============================================================================
	*/

        /*
	====================
	SV_SetMaster_f
	
	Specify a list of master servers
	====================
	*/
        public fun SV_SetMaster_f() {
            var i: Int
            var slot: Int

            // only dedicated servers send heartbeats
            if (Globals.dedicated.value == 0) {
                Com.Printf("Only dedicated servers use masters.\n")
                return
            }

            // make sure the server is listed public
            Cvar.Set("public", "1")

            run {
                i = 1
                while (i < Defines.MAX_MASTERS) {
                    SV_MAIN.master_adr[i] = netadr_t()
                    i++
                }
            }

            slot = 1 // slot 0 will always contain the id master
            run {
                i = 1
                while (i < Cmd.Argc()) {
                    if (slot == Defines.MAX_MASTERS)
                        break

                    if (!NET.StringToAdr(Cmd.Argv(i), SV_MAIN.master_adr[i])) {
                        Com.Printf("Bad address: " + Cmd.Argv(i) + "\n")
                        continue
                    }
                    if (SV_MAIN.master_adr[slot].port == 0)
                        SV_MAIN.master_adr[slot].port = Defines.PORT_MASTER

                    Com.Printf("Master server at " + NET.AdrToString(SV_MAIN.master_adr[slot]) + "\n")
                    Com.Printf("Sending a ping.\n")

                    Netchan.OutOfBandPrint(Defines.NS_SERVER, SV_MAIN.master_adr[slot], "ping")

                    slot++
                    i++
                }
            }

            SV_INIT.svs.last_heartbeat = -9999999
        }

        /*
	==================
	SV_SetPlayer
	
	Sets sv_client and sv_player to the player with idnum Cmd.Argv(1)
	==================
	*/
        public fun SV_SetPlayer(): Boolean {
            var cl: client_t
            var i: Int
            val idnum: Int
            val s: String

            if (Cmd.Argc() < 2)
                return false

            s = Cmd.Argv(1)

            // numeric values are just slot numbers
            if (s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                idnum = Lib.atoi(Cmd.Argv(1))
                if (idnum < 0 || idnum >= SV_MAIN.maxclients.value) {
                    Com.Printf("Bad client slot: " + idnum + "\n")
                    return false
                }

                SV_MAIN.sv_client = SV_INIT.svs.clients[idnum]
                SV_USER.sv_player = SV_MAIN.sv_client.edict
                if (0 == SV_MAIN.sv_client.state) {
                    Com.Printf("Client " + idnum + " is not active\n")
                    return false
                }
                return true
            }

            // check for a name match
            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (0 == cl.state)
                        continue
                    if (0 == Lib.strcmp(cl.name, s)) {
                        SV_MAIN.sv_client = cl
                        SV_USER.sv_player = SV_MAIN.sv_client.edict
                        return true
                    }
                    i++
                }
            }

            Com.Printf("Userid " + s + " is not on the server\n")
            return false
        }
        /*
	===============================================================================
	
	SAVEGAME FILES
	
	===============================================================================
	*/

        public fun remove(name: String) {
            try {
                File(name).delete()
            } catch (e: Exception) {
            }

        }

        /** Delete save files save/(number)/.   */
        public fun SV_WipeSavegame(savename: String) {

            var name: String

            Com.DPrintf("SV_WipeSaveGame(" + savename + ")\n")

            name = FS.Gamedir() + "/save/" + savename + "/server.ssv"
            remove(name)

            name = FS.Gamedir() + "/save/" + savename + "/game.ssv"
            remove(name)

            name = FS.Gamedir() + "/save/" + savename + "/*.sav"

            var f = Sys.FindFirst(name, 0, 0)
            while (f != null) {
                f!!.delete()
                f = Sys.FindNext()
            }
            Sys.FindClose()

            name = FS.Gamedir() + "/save/" + savename + "/*.sv2"

            f = Sys.FindFirst(name, 0, 0)

            while (f != null) {
                f!!.delete()
                f = Sys.FindNext()
            }
            Sys.FindClose()
        }

        /*
	================
	CopyFile
	================
	*/
        public fun CopyFile(src: String, dst: String) {
            val f1: RandomAccessFile
            val f2: RandomAccessFile
            var l = -1
            val buffer = ByteArray(65536)

            //Com.DPrintf("CopyFile (" + src + ", " + dst + ")\n");
            try {
                f1 = RandomAccessFile(src, "r")
            } catch (e: Exception) {
                return
            }

            try {
                f2 = RandomAccessFile(dst, "rw")
            } catch (e: Exception) {
                try {
                    f1.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

                return
            }


            while (true) {

                try {
                    l = f1.read(buffer, 0, 65536)
                } catch (e1: IOException) {

                    e1.printStackTrace()
                }

                if (l == -1)
                    break
                try {
                    f2.write(buffer, 0, l)
                } catch (e2: IOException) {

                    e2.printStackTrace()
                }

            }

            try {
                f1.close()
            } catch (e1: IOException) {

                e1.printStackTrace()
            }

            try {
                f2.close()
            } catch (e2: IOException) {

                e2.printStackTrace()
            }

        }

        /*
	================
	SV_CopySaveGame
	================
	*/
        public fun SV_CopySaveGame(src: String, dst: String) {
            var found: File?

            var name: String
            var name2: String

            Com.DPrintf("SV_CopySaveGame(" + src + "," + dst + ")\n")

            SV_WipeSavegame(dst)

            // copy the savegame over
            name = FS.Gamedir() + "/save/" + src + "/server.ssv"
            name2 = FS.Gamedir() + "/save/" + dst + "/server.ssv"
            FS.CreatePath(name2)
            CopyFile(name, name2)

            name = FS.Gamedir() + "/save/" + src + "/game.ssv"
            name2 = FS.Gamedir() + "/save/" + dst + "/game.ssv"
            CopyFile(name, name2)

            val name1 = FS.Gamedir() + "/save/" + src + "/"
            name = FS.Gamedir() + "/save/" + src + "/*.sav"

            found = Sys.FindFirst(name, 0, 0)

            while (found != null) {
                name = name1 + found!!.getName()
                name2 = FS.Gamedir() + "/save/" + dst + "/" + found!!.getName()

                CopyFile(name, name2)

                // change sav to sv2
                name = name.substring(0, name.length() - 3) + "sv2"
                name2 = name2.substring(0, name2.length() - 3) + "sv2"

                CopyFile(name, name2)

                found = Sys.FindNext()
            }
            Sys.FindClose()
        }

        /*
	==============
	SV_WriteLevelFile
	
	==============
	*/
        public fun SV_WriteLevelFile() {

            var name: String
            val f: QuakeFile

            Com.DPrintf("SV_WriteLevelFile()\n")

            name = FS.Gamedir() + "/save/current/" + SV_INIT.sv.name + ".sv2"

            try {
                f = QuakeFile(name, "rw")

                for (i in 0..Defines.MAX_CONFIGSTRINGS - 1)
                    f.writeString(SV_INIT.sv.configstrings[i])

                CM.CM_WritePortalState(f)
                f.close()
            } catch (e: Exception) {
                Com.Printf("Failed to open " + name + "\n")
                e.printStackTrace()
            }


            name = FS.Gamedir() + "/save/current/" + SV_INIT.sv.name + ".sav"
            GameSave.WriteLevel(name)
        }

        /*
	==============
	SV_ReadLevelFile
	
	==============
	*/
        public fun SV_ReadLevelFile() {
            //char name[MAX_OSPATH];
            var name: String
            val f: QuakeFile

            Com.DPrintf("SV_ReadLevelFile()\n")

            name = FS.Gamedir() + "/save/current/" + SV_INIT.sv.name + ".sv2"
            try {
                f = QuakeFile(name, "r")

                for (n in 0..Defines.MAX_CONFIGSTRINGS - 1)
                    SV_INIT.sv.configstrings[n] = f.readString()

                CM.CM_ReadPortalState(f)

                f.close()
            } catch (e1: IOException) {
                Com.Printf("Failed to open " + name + "\n")
                e1.printStackTrace()
            }


            name = FS.Gamedir() + "/save/current/" + SV_INIT.sv.name + ".sav"
            GameSave.ReadLevel(name)
        }

        /*
	==============
	SV_WriteServerFile
	
	==============
	*/
        public fun SV_WriteServerFile(autosave: Boolean) {
            val f: QuakeFile
            var `var`: cvar_t?

            var filename: String
            var name: String
            var string: String
            var comment: String

            Com.DPrintf("SV_WriteServerFile(" + (if (autosave) "true" else "false") + ")\n")

            filename = FS.Gamedir() + "/save/current/server.ssv"
            try {
                f = QuakeFile(filename, "rw")

                if (!autosave) {
                    val c = Calendar.getInstance()
                    comment = Com.sprintf("%2i:%2i %2i/%2i  ", Vargs().add(c.get(Calendar.HOUR_OF_DAY)).add(c.get(Calendar.MINUTE)).add(c.get(Calendar.MONTH) + 1).add(c.get(Calendar.DAY_OF_MONTH)))
                    comment += SV_INIT.sv.configstrings[Defines.CS_NAME]
                } else {
                    // autosaved
                    comment = "ENTERING " + SV_INIT.sv.configstrings[Defines.CS_NAME]
                }

                f.writeString(comment)
                f.writeString(SV_INIT.svs.mapcmd)

                // write the mapcmd

                // write all CVAR_LATCH cvars
                // these will be things like coop, skill, deathmatch, etc
                run {
                    `var` = Globals.cvar_vars
                    while (`var` != null) {
                        if (0 == (`var`!!.flags and Defines.CVAR_LATCH))
                            continue
                        if (`var`!!.name.length() >= Defines.MAX_OSPATH - 1 || `var`!!.string.length() >= 128 - 1) {
                            Com.Printf("Cvar too long: " + `var`!!.name + " = " + `var`!!.string + "\n")
                            continue
                        }

                        name = `var`!!.name
                        string = `var`!!.string
                        try {
                            f.writeString(name)
                            f.writeString(string)
                        } catch (e2: IOException) {
                        }

                        `var` = `var`!!.next

                    }
                }
                // rst: for termination.
                f.writeString(null)
                f.close()
            } catch (e: Exception) {
                Com.Printf("Couldn't write " + filename + "\n")
            }


            // write game state
            filename = FS.Gamedir() + "/save/current/game.ssv"
            GameSave.WriteGame(filename, autosave)
        }

        /*
	==============
	SV_ReadServerFile
	
	==============
	*/
        public fun SV_ReadServerFile() {
            var filename = ""
            var name: String? = ""
            val string: String
            var mapcmd: String
            try {
                val f: QuakeFile

                mapcmd = ""

                Com.DPrintf("SV_ReadServerFile()\n")

                filename = FS.Gamedir() + "/save/current/server.ssv"

                f = QuakeFile(filename, "r")

                // read the mapcmd
                mapcmd = f.readString()

                // read all CVAR_LATCH cvars
                // these will be things like coop, skill, deathmatch, etc
                while (true) {
                    name = f.readString()
                    if (name == null)
                        break
                    string = f.readString()

                    Com.DPrintf("Set " + name + " = " + string + "\n")
                    Cvar.ForceSet(name, string)
                }

                f.close()

                // start a new game fresh with new cvars
                SV_INIT.SV_InitGame()

                SV_INIT.svs.mapcmd = mapcmd

                // read game state
                filename = FS.Gamedir() + "/save/current/game.ssv"
                GameSave.ReadGame(filename)
            } catch (e: Exception) {
                Com.Printf("Couldn't read file " + filename + "\n")
                e.printStackTrace()
            }

        }
        //=========================================================

        /*
	==================
	SV_DemoMap_f
	
	Puts the server in demo mode on a specific map/cinematic
	==================
	*/
        public fun SV_DemoMap_f() {
            SV_INIT.SV_Map(true, Cmd.Argv(1), false)
        }

        /*
	==================
	SV_GameMap_f
	
	Saves the state of the map just being exited and goes to a new map.
	
	If the initial character of the map string is '*', the next map is
	in a new unit, so the current savegame directory is cleared of
	map files.
	
	Example:
	
	*inter.cin+jail
	
	Clears the archived maps, plays the inter.cin cinematic, then
	goes to map jail.bsp.
	==================
	*/
        public fun SV_GameMap_f() {
            val map: String
            var i: Int
            var cl: client_t
            var savedInuse: BooleanArray?

            if (Cmd.Argc() != 2) {
                Com.Printf("USAGE: gamemap <map>\n")
                return
            }

            Com.DPrintf("SV_GameMap(" + Cmd.Argv(1) + ")\n")

            FS.CreatePath(FS.Gamedir() + "/save/current/")

            // check for clearing the current savegame
            map = Cmd.Argv(1)
            if (map.charAt(0) == '*') {
                // wipe all the *.sav files
                SV_WipeSavegame("current")
            } else {
                // save the map just exited
                if (SV_INIT.sv.state == Defines.ss_game) {
                    // clear all the client inuse flags before saving so that
                    // when the level is re-entered, the clients will spawn
                    // at spawn points instead of occupying body shells
                    savedInuse = BooleanArray(SV_MAIN.maxclients.value as Int)
                    run {
                        i = 0
                        while (i < SV_MAIN.maxclients.value) {
                            cl = SV_INIT.svs.clients[i]
                            savedInuse[i] = cl.edict.inuse
                            cl.edict.inuse = false
                            i++
                        }
                    }

                    SV_WriteLevelFile()

                    // we must restore these for clients to transfer over correctly
                    run {
                        i = 0
                        while (i < SV_MAIN.maxclients.value) {
                            cl = SV_INIT.svs.clients[i]
                            cl.edict.inuse = savedInuse[i]
                            i++

                        }
                    }
                    savedInuse = null
                }
            }

            // start up the next map
            SV_INIT.SV_Map(false, Cmd.Argv(1), false)

            // archive server state
            SV_INIT.svs.mapcmd = Cmd.Argv(1)

            // copy off the level to the autosave slot
            if (0 == Globals.dedicated.value) {
                SV_WriteServerFile(true)
                SV_CopySaveGame("current", "save0")
            }
        }

        /*
	==================
	SV_Map_f
	
	Goes directly to a given map without any savegame archiving.
	For development work
	==================
	*/
        public fun SV_Map_f() {
            val map: String
            //char expanded[MAX_QPATH];
            val expanded: String

            // if not a pcx, demo, or cinematic, check to make sure the level exists
            map = Cmd.Argv(1)
            if (map.indexOf(".") < 0) {
                expanded = "maps/" + map + ".bsp"
                if (FS.LoadFile(expanded) == null) {

                    Com.Printf("Can't find " + expanded + "\n")
                    return
                }
            }

            SV_INIT.sv.state = Defines.ss_dead // don't save current level when changing

            SV_WipeSavegame("current")
            SV_GameMap_f()
        }
        /*
	=====================================================================
	
	  SAVEGAMES
	
	=====================================================================
	*/

        /*
	==============
	SV_Loadgame_f
	
	==============
	*/
        public fun SV_Loadgame_f() {

            val name: String
            val f: RandomAccessFile
            val dir: String

            if (Cmd.Argc() != 2) {
                Com.Printf("USAGE: loadgame <directory>\n")
                return
            }

            Com.Printf("Loading game...\n")

            dir = Cmd.Argv(1)
            if ((dir.indexOf("..") > -1) || (dir.indexOf("/") > -1) || (dir.indexOf("\\") > -1)) {
                Com.Printf("Bad savedir.\n")
            }

            // make sure the server.ssv file exists
            name = FS.Gamedir() + "/save/" + Cmd.Argv(1) + "/server.ssv"
            try {
                f = RandomAccessFile(name, "r")
            } catch (e: FileNotFoundException) {
                Com.Printf("No such savegame: " + name + "\n")
                return
            }


            try {
                f.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }


            SV_CopySaveGame(Cmd.Argv(1), "current")
            SV_ReadServerFile()

            // go to the map
            SV_INIT.sv.state = Defines.ss_dead // don't save current level when changing
            SV_INIT.SV_Map(false, SV_INIT.svs.mapcmd, true)
        }

        /*
	==============
	SV_Savegame_f
	
	==============
	*/
        public fun SV_Savegame_f() {
            val dir: String

            if (SV_INIT.sv.state != Defines.ss_game) {
                Com.Printf("You must be in a game to save.\n")
                return
            }

            if (Cmd.Argc() != 2) {
                Com.Printf("USAGE: savegame <directory>\n")
                return
            }

            if (Cvar.VariableValue("deathmatch") != 0) {
                Com.Printf("Can't savegame in a deathmatch\n")
                return
            }

            if (0 == Lib.strcmp(Cmd.Argv(1), "current")) {
                Com.Printf("Can't save to 'current'\n")
                return
            }

            if (SV_MAIN.maxclients.value == 1 && SV_INIT.svs.clients[0].edict.client.ps.stats[Defines.STAT_HEALTH] <= 0) {
                Com.Printf("\nCan't savegame while dead!\n")
                return
            }

            dir = Cmd.Argv(1)
            if ((dir.indexOf("..") > -1) || (dir.indexOf("/") > -1) || (dir.indexOf("\\") > -1)) {
                Com.Printf("Bad savedir.\n")
            }

            Com.Printf("Saving game...\n")

            // archive current level, including all client edicts.
            // when the level is reloaded, they will be shells awaiting
            // a connecting client
            SV_WriteLevelFile()

            // save server state
            try {
                SV_WriteServerFile(false)
            } catch (e: Exception) {
                Com.Printf("IOError in SV_WriteServerFile: " + e)
            }


            // copy it off
            SV_CopySaveGame("current", dir)
            Com.Printf("Done.\n")
        }

        //===============================================================
        /*
	==================
	SV_Kick_f
	
	Kick a user off of the server
	==================
	*/
        public fun SV_Kick_f() {
            if (!SV_INIT.svs.initialized) {
                Com.Printf("No server running.\n")
                return
            }

            if (Cmd.Argc() != 2) {
                Com.Printf("Usage: kick <userid>\n")
                return
            }

            if (!SV_SetPlayer())
                return

            SV_SEND.SV_BroadcastPrintf(Defines.PRINT_HIGH, SV_MAIN.sv_client.name + " was kicked\n")
            // print directly, because the dropped client won't get the
            // SV_BroadcastPrintf message
            SV_SEND.SV_ClientPrintf(SV_MAIN.sv_client, Defines.PRINT_HIGH, "You were kicked from the game\n")
            SV_MAIN.SV_DropClient(SV_MAIN.sv_client)
            SV_MAIN.sv_client.lastmessage = SV_INIT.svs.realtime // min case there is a funny zombie
        }

        /*
	================
	SV_Status_f
	================
	*/
        public fun SV_Status_f() {
            var i: Int
            var j: Int
            var l: Int
            var cl: client_t
            var s: String
            var ping: Int
            if (SV_INIT.svs.clients == null) {
                Com.Printf("No server running.\n")
                return
            }
            Com.Printf("map              : " + SV_INIT.sv.name + "\n")

            Com.Printf("num score ping name            lastmsg address               qport \n")
            Com.Printf("--- ----- ---- --------------- ------- --------------------- ------\n")
            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    cl = SV_INIT.svs.clients[i]
                    if (0 == cl.state)
                        continue

                    Com.Printf("%3i ", Vargs().add(i))
                    Com.Printf("%5i ", Vargs().add(cl.edict.client.ps.stats[Defines.STAT_FRAGS]))

                    if (cl.state == Defines.cs_connected)
                        Com.Printf("CNCT ")
                    else if (cl.state == Defines.cs_zombie)
                        Com.Printf("ZMBI ")
                    else {
                        ping = if (cl.ping < 9999) cl.ping else 9999
                        Com.Printf("%4i ", Vargs().add(ping))
                    }

                    Com.Printf("%s", Vargs().add(cl.name))
                    l = 16 - cl.name.length()
                    run {
                        j = 0
                        while (j < l) {
                            Com.Printf(" ")
                            j++
                        }
                    }

                    Com.Printf("%7i ", Vargs().add(SV_INIT.svs.realtime - cl.lastmessage))

                    s = NET.AdrToString(cl.netchan.remote_address)
                    Com.Printf(s)
                    l = 22 - s.length()
                    run {
                        j = 0
                        while (j < l) {
                            Com.Printf(" ")
                            j++
                        }
                    }

                    Com.Printf("%5i", Vargs().add(cl.netchan.qport))

                    Com.Printf("\n")
                    i++
                }
            }
            Com.Printf("\n")
        }

        /*
	==================
	SV_ConSay_f
	==================
	*/
        public fun SV_ConSay_f() {
            var client: client_t
            var j: Int
            var p: String
            var text: String // char[1024];

            if (Cmd.Argc() < 2)
                return

            text = "console: "
            p = Cmd.Args()

            if (p.charAt(0) == '"') {
                p = p.substring(1, p.length() - 1)
            }

            text += p

            run {
                j = 0
                while (j < SV_MAIN.maxclients.value) {
                    client = SV_INIT.svs.clients[j]
                    if (client.state != Defines.cs_spawned)
                        continue
                    SV_SEND.SV_ClientPrintf(client, Defines.PRINT_CHAT, text + "\n")
                    j++
                }
            }
        }

        /*
	==================
	SV_Heartbeat_f
	==================
	*/
        public fun SV_Heartbeat_f() {
            SV_INIT.svs.last_heartbeat = -9999999
        }

        /*
	===========
	SV_Serverinfo_f
	
	  Examine or change the serverinfo string
	===========
	*/
        public fun SV_Serverinfo_f() {
            Com.Printf("Server info settings:\n")
            Info.Print(Cvar.Serverinfo())
        }

        /*
	===========
	SV_DumpUser_f
	
	Examine all a users info strings
	===========
	*/
        public fun SV_DumpUser_f() {
            if (Cmd.Argc() != 2) {
                Com.Printf("Usage: info <userid>\n")
                return
            }

            if (!SV_SetPlayer())
                return

            Com.Printf("userinfo\n")
            Com.Printf("--------\n")
            Info.Print(SV_MAIN.sv_client.userinfo)

        }

        /*
	==============
	SV_ServerRecord_f
	
	Begins server demo recording.  Every entity and every message will be
	recorded, but no playerinfo will be stored.  Primarily for demo merging.
	==============
	*/
        public fun SV_ServerRecord_f() {
            //char	name[MAX_OSPATH];
            val name: String
            val buf_data = ByteArray(32768)
            val buf = sizebuf_t()
            val len: Int
            var i: Int

            if (Cmd.Argc() != 2) {
                Com.Printf("serverrecord <demoname>\n")
                return
            }

            if (SV_INIT.svs.demofile != null) {
                Com.Printf("Already recording.\n")
                return
            }

            if (SV_INIT.sv.state != Defines.ss_game) {
                Com.Printf("You must be in a level to record.\n")
                return
            }

            //
            // open the demo file
            //
            name = FS.Gamedir() + "/demos/" + Cmd.Argv(1) + ".dm2"

            Com.Printf("recording to " + name + ".\n")
            FS.CreatePath(name)
            try {
                SV_INIT.svs.demofile = RandomAccessFile(name, "rw")
            } catch (e: Exception) {
                Com.Printf("ERROR: couldn't open.\n")
                return
            }


            // setup a buffer to catch all multicasts
            SZ.Init(SV_INIT.svs.demo_multicast, SV_INIT.svs.demo_multicast_buf, SV_INIT.svs.demo_multicast_buf.length)

            //
            // write a single giant fake message with all the startup info
            //
            SZ.Init(buf, buf_data, buf_data.size())

            //
            // serverdata needs to go over for all types of servers
            // to make sure the protocol is right, and to set the gamedir
            //
            // send the serverdata
            MSG.WriteByte(buf, Defines.svc_serverdata)
            MSG.WriteLong(buf, Defines.PROTOCOL_VERSION)
            MSG.WriteLong(buf, SV_INIT.svs.spawncount)
            // 2 means server demo
            MSG.WriteByte(buf, 2) // demos are always attract loops
            MSG.WriteString(buf, Cvar.VariableString("gamedir"))
            MSG.WriteShort(buf, -1)
            // send full levelname
            MSG.WriteString(buf, SV_INIT.sv.configstrings[Defines.CS_NAME])

            run {
                i = 0
                while (i < Defines.MAX_CONFIGSTRINGS) {
                    if (SV_INIT.sv.configstrings[i].length() == 0) {
                        MSG.WriteByte(buf, Defines.svc_configstring)
                        MSG.WriteShort(buf, i)
                        MSG.WriteString(buf, SV_INIT.sv.configstrings[i])
                    }
                    i++
                }
            }

            // write it to the demo file
            Com.DPrintf("signon message length: " + buf.cursize + "\n")
            len = EndianHandler.swapInt(buf.cursize)
            //fwrite(len, 4, 1, svs.demofile);
            //fwrite(buf.data, buf.cursize, 1, svs.demofile);
            try {
                SV_INIT.svs.demofile.writeInt(len)
                SV_INIT.svs.demofile.write(buf.data, 0, buf.cursize)
            } catch (e1: IOException) {
                // TODO: do quake2 error handling!
                e1.printStackTrace()
            }


            // the rest of the demo file will be individual frames
        }

        /*
	==============
	SV_ServerStop_f
	
	Ends server demo recording
	==============
	*/
        public fun SV_ServerStop_f() {
            if (SV_INIT.svs.demofile == null) {
                Com.Printf("Not doing a serverrecord.\n")
                return
            }
            try {
                SV_INIT.svs.demofile.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            SV_INIT.svs.demofile = null
            Com.Printf("Recording completed.\n")
        }

        /*
	===============
	SV_KillServer_f
	
	Kick everyone off, possibly in preparation for a new game
	
	===============
	*/
        public fun SV_KillServer_f() {
            if (!SV_INIT.svs.initialized)
                return
            SV_MAIN.SV_Shutdown("Server was killed.\n", false)
            NET.Config(false) // close network sockets
        }

        /*
	===============
	SV_ServerCommand_f
	
	Let the game dll handle a command
	===============
	*/
        public fun SV_ServerCommand_f() {

            GameSVCmds.ServerCommand()
        }
        //===========================================================

        /*
	==================
	SV_InitOperatorCommands
	==================
	*/
        public fun SV_InitOperatorCommands() {
            Cmd.AddCommand("heartbeat", object : xcommand_t() {
                public fun execute() {
                    SV_Heartbeat_f()
                }
            })
            Cmd.AddCommand("kick", object : xcommand_t() {
                public fun execute() {
                    SV_Kick_f()
                }
            })
            Cmd.AddCommand("status", object : xcommand_t() {
                public fun execute() {
                    SV_Status_f()
                }
            })
            Cmd.AddCommand("serverinfo", object : xcommand_t() {
                public fun execute() {
                    SV_Serverinfo_f()
                }
            })
            Cmd.AddCommand("dumpuser", object : xcommand_t() {
                public fun execute() {
                    SV_DumpUser_f()
                }
            })

            Cmd.AddCommand("map", object : xcommand_t() {
                public fun execute() {
                    SV_Map_f()
                }
            })
            Cmd.AddCommand("demomap", object : xcommand_t() {
                public fun execute() {
                    SV_DemoMap_f()
                }
            })
            Cmd.AddCommand("gamemap", object : xcommand_t() {
                public fun execute() {
                    SV_GameMap_f()
                }
            })
            Cmd.AddCommand("setmaster", object : xcommand_t() {
                public fun execute() {
                    SV_SetMaster_f()
                }
            })

            if (Globals.dedicated.value != 0)
                Cmd.AddCommand("say", object : xcommand_t() {
                    public fun execute() {
                        SV_ConSay_f()
                    }
                })

            Cmd.AddCommand("serverrecord", object : xcommand_t() {
                public fun execute() {
                    SV_ServerRecord_f()
                }
            })
            Cmd.AddCommand("serverstop", object : xcommand_t() {
                public fun execute() {
                    SV_ServerStop_f()
                }
            })

            Cmd.AddCommand("save", object : xcommand_t() {
                public fun execute() {
                    SV_Savegame_f()
                }
            })
            Cmd.AddCommand("load", object : xcommand_t() {
                public fun execute() {
                    SV_Loadgame_f()
                }
            })

            Cmd.AddCommand("killserver", object : xcommand_t() {
                public fun execute() {
                    SV_KillServer_f()
                }
            })

            Cmd.AddCommand("sv", object : xcommand_t() {
                public fun execute() {
                    SV_ServerCommand_f()
                }
            })
        }
    }
}
