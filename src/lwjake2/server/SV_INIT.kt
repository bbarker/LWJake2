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
import lwjake2.client.CL
import lwjake2.client.SCR
import lwjake2.game.GameBase
import lwjake2.game.GameSpawn
import lwjake2.game.edict_t
import lwjake2.game.entity_state_t
import lwjake2.game.usercmd_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.PMove
import lwjake2.qcommon.SZ
import lwjake2.sys.NET
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.io.IOException
import java.io.RandomAccessFile

public class SV_INIT {
    companion object {

        /**
         * SV_FindIndex.
         */
        public fun SV_FindIndex(name: String?, start: Int, max: Int, create: Boolean): Int {
            var i: Int

            if (name == null || name.length() == 0)
                return 0

            run {
                i = 1
                while (i < max && sv.configstrings[start + i] != null) {
                    if (0 == Lib.strcmp(sv.configstrings[start + i], name))
                        return i
                    i++
                }
            }

            if (!create)
                return 0

            if (i == max)
                Com.Error(Defines.ERR_DROP, "*Index: overflow")

            sv.configstrings[start + i] = name

            if (sv.state != Defines.ss_loading) {
                // send the update to everyone
                SZ.Clear(sv.multicast)
                MSG.WriteChar(sv.multicast, Defines.svc_configstring)
                MSG.WriteShort(sv.multicast, start + i)
                MSG.WriteString(sv.multicast, name)
                SV_SEND.SV_Multicast(Globals.vec3_origin, Defines.MULTICAST_ALL_R)
            }

            return i
        }

        public fun SV_ModelIndex(name: String): Int {
            return SV_FindIndex(name, Defines.CS_MODELS, Defines.MAX_MODELS, true)
        }

        public fun SV_SoundIndex(name: String): Int {
            return SV_FindIndex(name, Defines.CS_SOUNDS, Defines.MAX_SOUNDS, true)
        }

        public fun SV_ImageIndex(name: String): Int {
            return SV_FindIndex(name, Defines.CS_IMAGES, Defines.MAX_IMAGES, true)
        }

        /**
         * SV_CreateBaseline

         * Entity baselines are used to compress the update messages to the clients --
         * only the fields that differ from the baseline will be transmitted.
         */
        public fun SV_CreateBaseline() {
            var svent: edict_t
            var entnum: Int

            run {
                entnum = 1
                while (entnum < GameBase.num_edicts) {
                    svent = GameBase.g_edicts[entnum]

                    if (!svent.inuse)
                        continue
                    if (0 == svent.s.modelindex && 0 == svent.s.sound && 0 == svent.s.effects)
                        continue

                    svent.s.number = entnum

                    // take current state as baseline
                    Math3D.VectorCopy(svent.s.origin, svent.s.old_origin)
                    sv.baselines[entnum].set(svent.s)
                    entnum++
                }
            }
        }

        /**
         * SV_CheckForSavegame.
         */
        public fun SV_CheckForSavegame() {

            val name: String
            val f: RandomAccessFile

            var i: Int

            if (SV_MAIN.sv_noreload.value != 0)
                return

            if (Cvar.VariableValue("deathmatch") != 0)
                return

            name = FS.Gamedir() + "/save/current/" + sv.name + ".sav"
            try {
                f = RandomAccessFile(name, "r")
            } catch (e: Exception) {
                return
            }


            try {
                f.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }


            SV_WORLD.SV_ClearWorld()

            // get configstrings and areaportals
            SV_CCMDS.SV_ReadLevelFile()

            if (!sv.loadgame) {
                // coming back to a level after being in a different
                // level, so run it for ten seconds

                // rlava2 was sending too many lightstyles, and overflowing the
                // reliable data. temporarily changing the server state to loading
                // prevents these from being passed down.
                val previousState: Int // PGM

                previousState = sv.state // PGM
                sv.state = Defines.ss_loading // PGM
                run {
                    i = 0
                    while (i < 100) {
                        GameBase.G_RunFrame()
                        i++
                    }
                }

                sv.state = previousState // PGM
            }
        }

        /**
         * SV_SpawnServer.

         * Change the server to a new map, taking all connected clients along with
         * it.
         */
        public fun SV_SpawnServer(server: String, spawnpoint: String, serverstate: Int, attractloop: Boolean, loadgame: Boolean) {
            var i: Int
            var checksum = 0

            if (attractloop)
                Cvar.Set("paused", "0")

            Com.Printf("------- Server Initialization -------\n")

            Com.DPrintf("SpawnServer: " + server + "\n")
            if (sv.demofile != null)
                try {
                    sv.demofile.close()
                } catch (e: Exception) {
                }


            // any partially connected client will be restarted
            svs.spawncount++

            sv.state = Defines.ss_dead

            Globals.server_state = sv.state

            // wipe the entire per-level structure
            sv = server_t()

            svs.realtime = 0
            sv.loadgame = loadgame
            sv.attractloop = attractloop

            // save name for levels that don't set message
            sv.configstrings[Defines.CS_NAME] = server

            if (Cvar.VariableValue("deathmatch") != 0) {
                sv.configstrings[Defines.CS_AIRACCEL] = "" + SV_MAIN.sv_airaccelerate.value
                PMove.pm_airaccelerate = SV_MAIN.sv_airaccelerate.value
            } else {
                sv.configstrings[Defines.CS_AIRACCEL] = "0"
                PMove.pm_airaccelerate = 0
            }

            SZ.Init(sv.multicast, sv.multicast_buf, sv.multicast_buf.length)

            sv.name = server

            // leave slots at start for clients only
            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    // needs to reconnect
                    if (svs.clients[i].state > Defines.cs_connected)
                        svs.clients[i].state = Defines.cs_connected
                    svs.clients[i].lastframe = -1
                    i++
                }
            }

            sv.time = 1000

            sv.name = server
            sv.configstrings[Defines.CS_NAME] = server

            val iw = intArray(checksum)

            if (serverstate != Defines.ss_game) {
                sv.models[1] = CM.CM_LoadMap("", false, iw) // no real map
            } else {
                sv.configstrings[Defines.CS_MODELS + 1] = "maps/" + server + ".bsp"
                sv.models[1] = CM.CM_LoadMap(sv.configstrings[Defines.CS_MODELS + 1], false, iw)
            }
            checksum = iw[0]
            sv.configstrings[Defines.CS_MAPCHECKSUM] = "" + checksum


            // clear physics interaction links

            SV_WORLD.SV_ClearWorld()

            run {
                i = 1
                while (i < CM.CM_NumInlineModels()) {
                    sv.configstrings[Defines.CS_MODELS + 1 + i] = "*" + i

                    // copy references
                    sv.models[i + 1] = CM.InlineModel(sv.configstrings[Defines.CS_MODELS + 1 + i])
                    i++
                }
            }


            // spawn the rest of the entities on the map

            // precache and static commands can be issued during
            // map initialization

            sv.state = Defines.ss_loading
            Globals.server_state = sv.state

            // load and spawn all other entities
            GameSpawn.SpawnEntities(sv.name, CM.CM_EntityString(), spawnpoint)

            // run two frames to allow everything to settle
            GameBase.G_RunFrame()
            GameBase.G_RunFrame()

            // all precaches are complete
            sv.state = serverstate
            Globals.server_state = sv.state

            // create a baseline for more efficient communications
            SV_CreateBaseline()

            // check for a savegame
            SV_CheckForSavegame()

            // set serverinfo variable
            Cvar.FullSet("mapname", sv.name, Defines.CVAR_SERVERINFO or Defines.CVAR_NOSET)
        }

        /**
         * SV_InitGame.

         * A brand new game has been started.
         */
        public fun SV_InitGame() {
            var i: Int
            var ent: edict_t
            //char idmaster[32];
            val idmaster: String

            if (svs.initialized) {
                // cause any connected clients to reconnect
                SV_MAIN.SV_Shutdown("Server restarted\n", true)
            } else {
                // make sure the client is down
                CL.Drop()
                SCR.BeginLoadingPlaque()
            }

            // get any latched variable changes (maxclients, etc)
            Cvar.GetLatchedVars()

            svs.initialized = true

            if (Cvar.VariableValue("coop") != 0 && Cvar.VariableValue("deathmatch") != 0) {
                Com.Printf("Deathmatch and Coop both set, disabling Coop\n")
                Cvar.FullSet("coop", "0", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            }

            // dedicated servers are can't be single player and are usually DM
            // so unless they explicity set coop, force it to deathmatch
            if (Globals.dedicated.value != 0) {
                if (0 == Cvar.VariableValue("coop"))
                    Cvar.FullSet("deathmatch", "1", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            }

            // init clients
            if (Cvar.VariableValue("deathmatch") != 0) {
                if (SV_MAIN.maxclients.value <= 1)
                    Cvar.FullSet("maxclients", "8", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
                else if (SV_MAIN.maxclients.value > Defines.MAX_CLIENTS)
                    Cvar.FullSet("maxclients", "" + Defines.MAX_CLIENTS, Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            } else if (Cvar.VariableValue("coop") != 0) {
                if (SV_MAIN.maxclients.value <= 1 || SV_MAIN.maxclients.value > 4)
                    Cvar.FullSet("maxclients", "4", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)

            } else
            // non-deathmatch, non-coop is one player
            {
                Cvar.FullSet("maxclients", "1", Defines.CVAR_SERVERINFO or Defines.CVAR_LATCH)
            }

            svs.spawncount = Lib.rand()
            svs.clients = arrayOfNulls<client_t>(SV_MAIN.maxclients.value as Int)
            for (n in 0..svs.clients.length - 1) {
                svs.clients[n] = client_t()
                svs.clients[n].serverindex = n
            }
            svs.num_client_entities = (SV_MAIN.maxclients.value as Int) * Defines.UPDATE_BACKUP * 64 //ok.

            svs.client_entities = arrayOfNulls<entity_state_t>(svs.num_client_entities)
            for (n in 0..svs.client_entities.length - 1)
                svs.client_entities[n] = entity_state_t(null)

            // init network stuff
            NET.Config((SV_MAIN.maxclients.value > 1))

            // heartbeats will always be sent to the id master
            svs.last_heartbeat = -99999 // send immediately
            idmaster = "192.246.40.37:" + Defines.PORT_MASTER
            NET.StringToAdr(idmaster, SV_MAIN.master_adr[0])

            // init game
            SV_GAME.SV_InitGameProgs()

            run {
                i = 0
                while (i < SV_MAIN.maxclients.value) {
                    ent = GameBase.g_edicts[i + 1]
                    svs.clients[i].edict = ent
                    svs.clients[i].lastcmd = usercmd_t()
                    i++
                }
            }
        }

        private var firstmap = ""

        /**
         * SV_Map

         * the full syntax is:

         * map [*] $ +

         * command from the console or progs. Map can also be a.cin, .pcx, or .dm2 file.

         * Nextserver is used to allow a cinematic to play, then proceed to
         * another level:

         * map tram.cin+jail_e3
         */
        public fun SV_Map(attractloop: Boolean, levelstring: String, loadgame: Boolean) {

            val l: Int
            var level: String
            val spawnpoint: String

            sv.loadgame = loadgame
            sv.attractloop = attractloop

            if (sv.state == Defines.ss_dead && !sv.loadgame)
                SV_InitGame() // the game is just starting

            level = levelstring // bis hier her ok.

            // if there is a + in the map, set nextserver to the remainder

            val c = level.indexOf('+')
            if (c != -1) {
                Cvar.Set("nextserver", "gamemap \"" + level.substring(c + 1) + "\"")
                level = level.substring(0, c)
            } else {
                Cvar.Set("nextserver", "")
            }

            // rst: base1 works for full, damo1 works for demo, so we need to store first map.
            if (firstmap.length() == 0) {
                if (!levelstring.endsWith(".cin") && !levelstring.endsWith(".pcx") && !levelstring.endsWith(".dm2")) {
                    val pos = levelstring.indexOf('+')
                    firstmap = levelstring.substring(pos + 1)
                }
            }

            // ZOID: special hack for end game screen in coop mode
            if (Cvar.VariableValue("coop") != 0 && level.equals("victory.pcx"))
                Cvar.Set("nextserver", "gamemap \"*" + firstmap + "\"")

            // if there is a $, use the remainder as a spawnpoint
            val pos = level.indexOf('$')
            if (pos != -1) {
                spawnpoint = level.substring(pos + 1)
                level = level.substring(0, pos)

            } else
                spawnpoint = ""

            // skip the end-of-unit flag * if necessary
            if (level.charAt(0) == '*')
                level = level.substring(1)

            l = level.length()
            if (l > 4 && level.endsWith(".cin")) {
                SCR.BeginLoadingPlaque() // for local system
                SV_SEND.SV_BroadcastCommand("changing\n")
                SV_SpawnServer(level, spawnpoint, Defines.ss_cinematic, attractloop, loadgame)
            } else if (l > 4 && level.endsWith(".dm2")) {
                SCR.BeginLoadingPlaque() // for local system
                SV_SEND.SV_BroadcastCommand("changing\n")
                SV_SpawnServer(level, spawnpoint, Defines.ss_demo, attractloop, loadgame)
            } else if (l > 4 && level.endsWith(".pcx")) {
                SCR.BeginLoadingPlaque() // for local system
                SV_SEND.SV_BroadcastCommand("changing\n")
                SV_SpawnServer(level, spawnpoint, Defines.ss_pic, attractloop, loadgame)
            } else {
                SCR.BeginLoadingPlaque() // for local system
                SV_SEND.SV_BroadcastCommand("changing\n")
                SV_SEND.SV_SendClientMessages()
                SV_SpawnServer(level, spawnpoint, Defines.ss_game, attractloop, loadgame)
                Cbuf.CopyToDefer()
            }

            SV_SEND.SV_BroadcastCommand("reconnect\n")
        }

        public var svs: server_static_t = server_static_t() // persistant
        // server info

        public var sv: server_t = server_t() // local server
    }
}