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
import lwjake2.game.GameBase
import lwjake2.game.Info
import lwjake2.game.PlayerClient
import lwjake2.game.edict_t
import lwjake2.game.entity_state_t
import lwjake2.game.usercmd_t
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.SZ
import lwjake2.util.Lib

import java.io.IOException

public class SV_USER {

    public class ucmd_t(var name:
                        String, var r:
                        Runnable)

    companion object {

        var sv_player: edict_t

        var u1 = ucmd_t("new", object : Runnable {
            override fun run() {
                SV_USER.SV_New_f()
            }
        })

        var ucmds = array<ucmd_t>(// auto issued
                ucmd_t("new", object : Runnable {
                    override fun run() {
                        SV_USER.SV_New_f()
                    }
                }), ucmd_t("configstrings", object : Runnable {
            override fun run() {
                SV_USER.SV_Configstrings_f()
            }
        }), ucmd_t("baselines", object : Runnable {
            override fun run() {
                SV_USER.SV_Baselines_f()
            }
        }), ucmd_t("begin", object : Runnable {
            override fun run() {
                SV_USER.SV_Begin_f()
            }
        }), ucmd_t("nextserver", object : Runnable {
            override fun run() {
                SV_USER.SV_Nextserver_f()
            }
        }), ucmd_t("disconnect", object : Runnable {
            override fun run() {
                SV_USER.SV_Disconnect_f()
            }
        }),
                // issued by hand at client consoles
                ucmd_t("info", object : Runnable {
                    override fun run() {
                        SV_USER.SV_ShowServerinfo_f()
                    }
                }), ucmd_t("download", object : Runnable {
            override fun run() {
                SV_USER.SV_BeginDownload_f()
            }
        }), ucmd_t("nextdl", object : Runnable {
            override fun run() {
                SV_USER.SV_NextDownload_f()
            }
        }))

        public val MAX_STRINGCMDS: Int = 8

        /*
     * ============================================================
     * 
     * USER STRINGCMD EXECUTION
     * 
     * sv_client and sv_player will be valid.
     * ============================================================
     */

        /*
     * ================== SV_BeginDemoServer ==================
     */
        public fun SV_BeginDemoserver() {
            val name: String

            name = "demos/" + SV_INIT.sv.name
            try {
                SV_INIT.sv.demofile = FS.FOpenFile(name)
            } catch (e: IOException) {
                Com.Error(Defines.ERR_DROP, "Couldn't open " + name + "\n")
            }

            if (SV_INIT.sv.demofile == null)
                Com.Error(Defines.ERR_DROP, "Couldn't open " + name + "\n")
        }

        /*
     * ================ SV_New_f
     * 
     * Sends the first message from the server to a connected client. This will
     * be sent on the initial connection and upon each server load.
     * ================
     */
        public fun SV_New_f() {
            val gamedir: String
            val playernum: Int
            val ent: edict_t

            Com.DPrintf("New() from " + SV_MAIN.sv_client.name + "\n")

            if (SV_MAIN.sv_client.state != Defines.cs_connected) {
                Com.Printf("New not valid -- already spawned\n")
                return
            }

            // demo servers just dump the file message
            if (SV_INIT.sv.state == Defines.ss_demo) {
                SV_BeginDemoserver()
                return
            }

            //
            // serverdata needs to go over for all types of servers
            // to make sure the protocol is right, and to set the gamedir
            //
            gamedir = Cvar.VariableString("gamedir")

            // send the serverdata
            MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_serverdata)
            MSG.WriteInt(SV_MAIN.sv_client.netchan.message, Defines.PROTOCOL_VERSION)

            MSG.WriteLong(SV_MAIN.sv_client.netchan.message, SV_INIT.svs.spawncount)
            MSG.WriteByte(SV_MAIN.sv_client.netchan.message, if (SV_INIT.sv.attractloop) 1 else 0)
            MSG.WriteString(SV_MAIN.sv_client.netchan.message, gamedir)

            if (SV_INIT.sv.state == Defines.ss_cinematic || SV_INIT.sv.state == Defines.ss_pic)
                playernum = -1
            else
            //playernum = sv_client - svs.clients;
                playernum = SV_MAIN.sv_client.serverindex

            MSG.WriteShort(SV_MAIN.sv_client.netchan.message, playernum)

            // send full levelname
            MSG.WriteString(SV_MAIN.sv_client.netchan.message, SV_INIT.sv.configstrings[Defines.CS_NAME])

            //
            // game server
            //
            if (SV_INIT.sv.state == Defines.ss_game) {
                // set up the entity for the client
                ent = GameBase.g_edicts[playernum + 1]
                ent.s.number = playernum + 1
                SV_MAIN.sv_client.edict = ent
                SV_MAIN.sv_client.lastcmd = usercmd_t()

                // begin fetching configstrings
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_stufftext)
                MSG.WriteString(SV_MAIN.sv_client.netchan.message, "cmd configstrings " + SV_INIT.svs.spawncount + " 0\n")
            }

        }

        /*
     * ================== SV_Configstrings_f ==================
     */
        public fun SV_Configstrings_f() {
            var start: Int

            Com.DPrintf("Configstrings() from " + SV_MAIN.sv_client.name + "\n")

            if (SV_MAIN.sv_client.state != Defines.cs_connected) {
                Com.Printf("configstrings not valid -- already spawned\n")
                return
            }

            // handle the case of a level changing while a client was connecting
            if (Lib.atoi(Cmd.Argv(1)) != SV_INIT.svs.spawncount) {
                Com.Printf("SV_Configstrings_f from different level\n")
                SV_New_f()
                return
            }

            start = Lib.atoi(Cmd.Argv(2))

            // write a packet full of data

            while (SV_MAIN.sv_client.netchan.message.cursize < Defines.MAX_MSGLEN / 2 && start < Defines.MAX_CONFIGSTRINGS) {
                if (SV_INIT.sv.configstrings[start] != null && SV_INIT.sv.configstrings[start].length() != 0) {
                    MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_configstring)
                    MSG.WriteShort(SV_MAIN.sv_client.netchan.message, start)
                    MSG.WriteString(SV_MAIN.sv_client.netchan.message, SV_INIT.sv.configstrings[start])
                }
                start++
            }

            // send next command

            if (start == Defines.MAX_CONFIGSTRINGS) {
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_stufftext)
                MSG.WriteString(SV_MAIN.sv_client.netchan.message, "cmd baselines " + SV_INIT.svs.spawncount + " 0\n")
            } else {
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_stufftext)
                MSG.WriteString(SV_MAIN.sv_client.netchan.message, "cmd configstrings " + SV_INIT.svs.spawncount + " " + start + "\n")
            }
        }

        /*
     * ================== SV_Baselines_f ==================
     */
        public fun SV_Baselines_f() {
            var start: Int
            val nullstate: entity_state_t
            val base: entity_state_t

            Com.DPrintf("Baselines() from " + SV_MAIN.sv_client.name + "\n")

            if (SV_MAIN.sv_client.state != Defines.cs_connected) {
                Com.Printf("baselines not valid -- already spawned\n")
                return
            }

            // handle the case of a level changing while a client was connecting
            if (Lib.atoi(Cmd.Argv(1)) != SV_INIT.svs.spawncount) {
                Com.Printf("SV_Baselines_f from different level\n")
                SV_New_f()
                return
            }

            start = Lib.atoi(Cmd.Argv(2))

            //memset (&nullstate, 0, sizeof(nullstate));
            nullstate = entity_state_t(null)

            // write a packet full of data

            while (SV_MAIN.sv_client.netchan.message.cursize < Defines.MAX_MSGLEN / 2 && start < Defines.MAX_EDICTS) {
                base = SV_INIT.sv.baselines[start]
                if (base.modelindex != 0 || base.sound != 0 || base.effects != 0) {
                    MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_spawnbaseline)
                    MSG.WriteDeltaEntity(nullstate, base, SV_MAIN.sv_client.netchan.message, true, true)
                }
                start++
            }

            // send next command

            if (start == Defines.MAX_EDICTS) {
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_stufftext)
                MSG.WriteString(SV_MAIN.sv_client.netchan.message, "precache " + SV_INIT.svs.spawncount + "\n")
            } else {
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_stufftext)
                MSG.WriteString(SV_MAIN.sv_client.netchan.message, "cmd baselines " + SV_INIT.svs.spawncount + " " + start + "\n")
            }
        }

        /*
     * ================== SV_Begin_f ==================
     */
        public fun SV_Begin_f() {
            Com.DPrintf("Begin() from " + SV_MAIN.sv_client.name + "\n")

            // handle the case of a level changing while a client was connecting
            if (Lib.atoi(Cmd.Argv(1)) != SV_INIT.svs.spawncount) {
                Com.Printf("SV_Begin_f from different level\n")
                SV_New_f()
                return
            }

            SV_MAIN.sv_client.state = Defines.cs_spawned

            // call the game begin function
            PlayerClient.ClientBegin(SV_USER.sv_player)

            Cbuf.InsertFromDefer()
        }

        //=============================================================================

        /*
     * ================== SV_NextDownload_f ==================
     */
        public fun SV_NextDownload_f() {
            var r: Int
            val percent: Int
            var size: Int

            if (SV_MAIN.sv_client.download == null)
                return

            r = SV_MAIN.sv_client.downloadsize - SV_MAIN.sv_client.downloadcount
            if (r > 1024)
                r = 1024

            MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_download)
            MSG.WriteShort(SV_MAIN.sv_client.netchan.message, r)

            SV_MAIN.sv_client.downloadcount += r
            size = SV_MAIN.sv_client.downloadsize
            if (size == 0)
                size = 1
            percent = SV_MAIN.sv_client.downloadcount * 100 / size
            MSG.WriteByte(SV_MAIN.sv_client.netchan.message, percent)
            SZ.Write(SV_MAIN.sv_client.netchan.message, SV_MAIN.sv_client.download, SV_MAIN.sv_client.downloadcount - r, r)

            if (SV_MAIN.sv_client.downloadcount != SV_MAIN.sv_client.downloadsize)
                return

            FS.FreeFile(SV_MAIN.sv_client.download)
            SV_MAIN.sv_client.download = null
        }

        /*
     * ================== SV_BeginDownload_f ==================
     */
        public fun SV_BeginDownload_f() {
            val name: String
            var offset = 0

            name = Cmd.Argv(1)

            if (Cmd.Argc() > 2)
                offset = Lib.atoi(Cmd.Argv(2)) // downloaded offset

            // hacked by zoid to allow more conrol over download
            // first off, no .. or global allow check

            if (name.indexOf("..") != -1 || SV_MAIN.allow_download.value == 0 // leading dot is no good
                    || name.charAt(0) == '.' // leading slash bad as well, must be
                    || name.charAt(0) == '/' // next up, skin check
                    || (name.startsWith("players/") && 0 == SV_MAIN.allow_download_players.value) // now
                    || (name.startsWith("models/") && 0 == SV_MAIN.allow_download_models.value) // now
                    || (name.startsWith("sound/") && 0 == SV_MAIN.allow_download_sounds.value) || (name.startsWith("maps/") && 0 == SV_MAIN.allow_download_maps.value) // MUST
                    || name.indexOf('/') == -1// in subdir
            // models
            // sounds
            // now maps (note special case for maps, must not be in pak)
            // be
            // in a
            // subdirectory
            ) {
                // don't allow anything with ..
                // path
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_download)
                MSG.WriteShort(SV_MAIN.sv_client.netchan.message, -1)
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, 0)
                return
            }

            if (SV_MAIN.sv_client.download != null)
                FS.FreeFile(SV_MAIN.sv_client.download)

            SV_MAIN.sv_client.download = FS.LoadFile(name)

            // rst: this handles loading errors, no message yet visible
            if (SV_MAIN.sv_client.download == null) {
                return
            }

            SV_MAIN.sv_client.downloadsize = SV_MAIN.sv_client.download.length
            SV_MAIN.sv_client.downloadcount = offset

            if (offset > SV_MAIN.sv_client.downloadsize)
                SV_MAIN.sv_client.downloadcount = SV_MAIN.sv_client.downloadsize

            if (SV_MAIN.sv_client.download == null // special check for maps, if it
                    || (name.startsWith("maps/") && FS.file_from_pak != 0)// came from a pak file, don't
            // allow
            // download ZOID
            ) {
                Com.DPrintf("Couldn't download " + name + " to " + SV_MAIN.sv_client.name + "\n")
                if (SV_MAIN.sv_client.download != null) {
                    FS.FreeFile(SV_MAIN.sv_client.download)
                    SV_MAIN.sv_client.download = null
                }

                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, Defines.svc_download)
                MSG.WriteShort(SV_MAIN.sv_client.netchan.message, -1)
                MSG.WriteByte(SV_MAIN.sv_client.netchan.message, 0)
                return
            }

            SV_NextDownload_f()
            Com.DPrintf("Downloading " + name + " to " + SV_MAIN.sv_client.name + "\n")
        }

        //============================================================================

        /*
     * ================= SV_Disconnect_f
     * 
     * The client is going to disconnect, so remove the connection immediately
     * =================
     */
        public fun SV_Disconnect_f() {
            //	SV_EndRedirect ();
            SV_MAIN.SV_DropClient(SV_MAIN.sv_client)
        }

        /*
     * ================== SV_ShowServerinfo_f
     * 
     * Dumps the serverinfo info string ==================
     */
        public fun SV_ShowServerinfo_f() {
            Info.Print(Cvar.Serverinfo())
        }

        public fun SV_Nextserver() {
            val v: String

            //ZOID, ss_pic can be nextserver'd in coop mode
            if (SV_INIT.sv.state == Defines.ss_game || (SV_INIT.sv.state == Defines.ss_pic && 0 == Cvar.VariableValue("coop")))
                return  // can't nextserver while playing a normal game

            SV_INIT.svs.spawncount++ // make sure another doesn't sneak in
            v = Cvar.VariableString("nextserver")
            //if (!v[0])
            if (v.length() == 0)
                Cbuf.AddText("killserver\n")
            else {
                Cbuf.AddText(v)
                Cbuf.AddText("\n")
            }
            Cvar.Set("nextserver", "")
        }

        /*
     * ================== SV_Nextserver_f
     * 
     * A cinematic has completed or been aborted by a client, so move to the
     * next server, ==================
     */
        public fun SV_Nextserver_f() {
            if (Lib.atoi(Cmd.Argv(1)) != SV_INIT.svs.spawncount) {
                Com.DPrintf("Nextserver() from wrong level, from " + SV_MAIN.sv_client.name + "\n")
                return  // leftover from last server
            }

            Com.DPrintf("Nextserver() from " + SV_MAIN.sv_client.name + "\n")

            SV_Nextserver()
        }

        /*
     * ================== SV_ExecuteUserCommand ==================
     */
        public fun SV_ExecuteUserCommand(s: String) {

            Com.dprintln("SV_ExecuteUserCommand:" + s)
            var u: SV_USER.ucmd_t? = null

            Cmd.TokenizeString(s.toCharArray(), true)
            SV_USER.sv_player = SV_MAIN.sv_client.edict

            //	SV_BeginRedirect (RD_CLIENT);

            var i = 0
            while (i < SV_USER.ucmds.size()) {
                u = SV_USER.ucmds[i]
                if (Cmd.Argv(0).equals(u!!.name)) {
                    u!!.r.run()
                    break
                }
                i++
            }

            if (i == SV_USER.ucmds.size() && SV_INIT.sv.state == Defines.ss_game)
                Cmd.ClientCommand(SV_USER.sv_player)

            //	SV_EndRedirect ();
        }

        /*
     * ===========================================================================
     * 
     * USER CMD EXECUTION
     * 
     * ===========================================================================
     */

        public fun SV_ClientThink(cl: client_t, cmd: usercmd_t) {
            cl.commandMsec -= cmd.msec and 255

            if (cl.commandMsec < 0 && SV_MAIN.sv_enforcetime.value != 0) {
                Com.DPrintf("commandMsec underflow from " + cl.name + "\n")
                return
            }

            PlayerClient.ClientThink(cl.edict, cmd)
        }

        /*
     * =================== SV_ExecuteClientMessage
     * 
     * The current net_message is parsed for the given client
     * ===================
     */
        public fun SV_ExecuteClientMessage(cl: client_t) {
            val c: Int
            val s: String

            var nullcmd = usercmd_t()
            val oldest = usercmd_t()
            val oldcmd = usercmd_t()
            val newcmd = usercmd_t()
            val net_drop: Int
            val stringCmdCount: Int
            val checksum: Int
            val calculatedChecksum: Int
            val checksumIndex: Int
            val move_issued: Boolean
            val lastframe: Int

            SV_MAIN.sv_client = cl
            SV_USER.sv_player = SV_MAIN.sv_client.edict

            // only allow one move command
            move_issued = false
            stringCmdCount = 0

            while (true) {
                if (Globals.net_message.readcount > Globals.net_message.cursize) {
                    Com.Printf("SV_ReadClientMessage: bad read:\n")
                    Com.Printf(Lib.hexDump(Globals.net_message.data, 32, false))
                    SV_MAIN.SV_DropClient(cl)
                    return
                }

                c = MSG.ReadByte(Globals.net_message)
                if (c == -1)
                    break

                when (c) {
                    else -> {
                        Com.Printf("SV_ReadClientMessage: unknown command char\n")
                        SV_MAIN.SV_DropClient(cl)
                        return
                    }

                    Defines.clc_nop -> {
                    }

                    Defines.clc_userinfo -> {
                        cl.userinfo = MSG.ReadString(Globals.net_message)
                        SV_MAIN.SV_UserinfoChanged(cl)
                    }

                    Defines.clc_move -> {
                        if (move_issued)
                            return  // someone is trying to cheat...

                        move_issued = true
                        checksumIndex = Globals.net_message.readcount
                        checksum = MSG.ReadByte(Globals.net_message)
                        lastframe = MSG.ReadLong(Globals.net_message)

                        if (lastframe != cl.lastframe) {
                            cl.lastframe = lastframe
                            if (cl.lastframe > 0) {
                                cl.frame_latency[cl.lastframe and (Defines.LATENCY_COUNTS - 1)] = SV_INIT.svs.realtime - cl.frames[cl.lastframe and Defines.UPDATE_MASK].senttime
                            }
                        }

                        //memset (nullcmd, 0, sizeof(nullcmd));
                        nullcmd = usercmd_t()
                        MSG.ReadDeltaUsercmd(Globals.net_message, nullcmd, oldest)
                        MSG.ReadDeltaUsercmd(Globals.net_message, oldest, oldcmd)
                        MSG.ReadDeltaUsercmd(Globals.net_message, oldcmd, newcmd)

                        if (cl.state != Defines.cs_spawned) {
                            cl.lastframe = -1
                            break
                        }

                        // if the checksum fails, ignore the rest of the packet

                        calculatedChecksum = Com.BlockSequenceCRCByte(Globals.net_message.data, checksumIndex + 1, Globals.net_message.readcount - checksumIndex - 1, cl.netchan.incoming_sequence)

                        if ((calculatedChecksum and 255) != checksum) {
                            Com.DPrintf("Failed command checksum for " + cl.name + " (" + calculatedChecksum + " != " + checksum + ")/" + cl.netchan.incoming_sequence + "\n")
                            return
                        }

                        if (0 == SV_MAIN.sv_paused.value) {
                            net_drop = cl.netchan.dropped
                            if (net_drop < 20) {

                                //if (net_drop > 2)

                                //	Com.Printf ("drop %i\n", net_drop);
                                while (net_drop > 2) {
                                    SV_ClientThink(cl, cl.lastcmd)

                                    net_drop--
                                }
                                if (net_drop > 1)
                                    SV_ClientThink(cl, oldest)

                                if (net_drop > 0)
                                    SV_ClientThink(cl, oldcmd)

                            }
                            SV_ClientThink(cl, newcmd)
                        }

                        // copy.
                        cl.lastcmd.set(newcmd)
                    }

                    Defines.clc_stringcmd -> {
                        s = MSG.ReadString(Globals.net_message)

                        // malicious users may try using too many string commands
                        if (++stringCmdCount < SV_USER.MAX_STRINGCMDS)
                            SV_ExecuteUserCommand(s)

                        if (cl.state == Defines.cs_zombie)
                            return  // disconnect command
                    }
                }
            }
        }
    }
}