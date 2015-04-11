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
import lwjake2.client.CL
import lwjake2.client.Key
import lwjake2.client.SCR
import lwjake2.game.Cmd
import lwjake2.server.SV_MAIN
import lwjake2.sys.NET
import lwjake2.sys.Sys
import lwjake2.sys.Timer
import lwjake2.util.Vargs

import java.io.FileWriter
import java.io.IOException

/**
 * Qcommon contains some  basic routines for the game engine
 * namely initialization, shutdown and frame generation.
 */
public class Qcommon : Globals() {
    companion object {

        public val BUILDSTRING: String = "Java " + System.getProperty("java.version")
        public val CPUSTRING: String = System.getProperty("os.arch")

        /**
         * This function initializes the different subsystems of
         * the game engine. The setjmp/longjmp mechanism of the original
         * was replaced with exceptions.
         * @param args the original unmodified command line arguments
         */
        public fun Init(args: Array<String>) {
            try {

                // prepare enough of the subsystems to handle
                // cvar and command buffer management
                Com.InitArgv(args)

                Cbuf.Init()

                Cmd.Init()
                Cvar.Init()

                Key.Init()

                // we need to add the early commands twice, because
                // a basedir or cddir needs to be set before execing
                // config files, but we want other parms to override
                // the settings of the config files
                Cbuf.AddEarlyCommands(false)
                Cbuf.Execute()

                FS.InitFilesystem()

                reconfigure(false)

                FS.setCDDir() // use cddir from config.cfg
                FS.markBaseSearchPaths() // mark the default search paths
                FS.checkOverride()

                reconfigure(true) // reload default.cfg and config.cfg

                //
                // init commands and vars
                //
                Cmd.AddCommand("error", Com.Error_f)

                Globals.host_speeds = Cvar.Get("host_speeds", "0", 0)
                Globals.log_stats = Cvar.Get("log_stats", "0", 0)
                Globals.developer = Cvar.Get("developer", "0", CVAR_ARCHIVE)
                Globals.timescale = Cvar.Get("timescale", "0", 0)
                Globals.fixedtime = Cvar.Get("fixedtime", "0", 0)
                Globals.logfile_active = Cvar.Get("logfile", "0", 0)
                Globals.showtrace = Cvar.Get("showtrace", "0", 0)
                Globals.dedicated = Cvar.Get("dedicated", "0", CVAR_NOSET)
                val s = Com.sprintf("%4.2f %s %s %s", Vargs(4).add(Globals.VERSION).add(CPUSTRING).add(Globals.__DATE__).add(BUILDSTRING))

                Cvar.Get("version", s, CVAR_SERVERINFO or CVAR_NOSET)

                NET.Init()    //ok
                Netchan.Netchan_Init()    //ok

                SV_MAIN.SV_Init()    //ok

                CL.Init()

                // add + commands from command line
                if (!Cbuf.AddLateCommands()) {
                    // if the user didn't give any commands, run default action
                    if (Globals.dedicated.value == 0)
                        Cbuf.AddText("d1\n")
                    else
                        Cbuf.AddText("dedicated_start\n")

                    Cbuf.Execute()
                } else {
                    // the user asked for something explicit
                    // so drop the loading plaque
                    SCR.EndLoadingPlaque()
                }

                Com.Printf("====== Quake2 Initialized ======\n\n")

                // save config when configuration is completed
                CL.WriteConfiguration()

            } catch (e: longjmpException) {
                Sys.Error("Error during initialization")
            }

        }

        /**
         * Trigger generation of a frame for the given time. The setjmp/longjmp
         * mechanism of the original was replaced with exceptions.
         * @param msec the current game time
         */
        public fun Frame(msec: Int) {
            var msec = msec
            try {

                if (Globals.log_stats.modified) {
                    Globals.log_stats.modified = false

                    if (Globals.log_stats.value != 0.0.toFloat()) {

                        try {
                            Globals.log_stats_file.close()
                        } catch (e: IOException) {
                        }

                        Globals.log_stats_file = null

                        try {
                            Globals.log_stats_file = FileWriter("stats.log")
                        } catch (e: IOException) {
                            Globals.log_stats_file = null
                        }

                        if (Globals.log_stats_file != null) {
                            try {
                                Globals.log_stats_file.write("entities,dlights,parts,frame time\n")
                            } catch (e: IOException) {
                            }

                        }

                    } else {

                        if (Globals.log_stats_file != null) {
                            try {
                                Globals.log_stats_file.close()
                            } catch (e: IOException) {
                            }

                            Globals.log_stats_file = null
                        }
                    }
                }

                if (Globals.fixedtime.value != 0.0.toFloat()) {
                    msec = Globals.fixedtime.value as Int
                } else if (Globals.timescale.value != 0.0.toFloat()) {
                    msec *= Globals.timescale.value
                    if (msec < 1)
                        msec = 1
                }

                if (Globals.showtrace.value != 0.0.toFloat()) {
                    Com.Printf("%4i traces  %4i points\n", Vargs(2).add(Globals.c_traces).add(Globals.c_pointcontents))


                    Globals.c_traces = 0
                    Globals.c_brush_traces = 0
                    Globals.c_pointcontents = 0
                }

                Cbuf.Execute()

                var time_before = 0
                var time_between = 0
                var time_after = 0

                if (Globals.host_speeds.value != 0.0.toFloat())
                    time_before = Timer.Milliseconds()

                Com.debugContext = "SV:"
                SV_MAIN.SV_Frame(msec)

                if (Globals.host_speeds.value != 0.0.toFloat())
                    time_between = Timer.Milliseconds()

                Com.debugContext = "CL:"
                CL.Frame(msec)

                if (Globals.host_speeds.value != 0.0.toFloat()) {
                    time_after = Timer.Milliseconds()

                    val all = time_after - time_before
                    var sv = time_between - time_before
                    var cl = time_after - time_between
                    val gm = Globals.time_after_game - Globals.time_before_game
                    val rf = Globals.time_after_ref - Globals.time_before_ref
                    sv -= gm
                    cl -= rf

                    Com.Printf("all:%3i sv:%3i gm:%3i cl:%3i rf:%3i\n", Vargs(5).add(all).add(sv).add(gm).add(cl).add(rf))
                }

            } catch (e: longjmpException) {
                Com.DPrintf("lonjmp exception:" + e)
            }

        }

        fun reconfigure(clear: Boolean) {
            val dir = Cvar.Get("cddir", "", CVAR_ARCHIVE).string
            Cbuf.AddText("exec default.cfg\n")
            Cbuf.AddText("bind MWHEELUP weapnext\n")
            Cbuf.AddText("bind MWHEELDOWN weapprev\n")
            Cbuf.AddText("bind w +forward\n")
            Cbuf.AddText("bind s +back\n")
            Cbuf.AddText("bind a +moveleft\n")
            Cbuf.AddText("bind d +moveright\n")
            Cbuf.Execute()
            Cvar.Set("vid_fullscreen", "0")
            Cbuf.AddText("exec config.cfg\n")

            Cbuf.AddEarlyCommands(clear)
            Cbuf.Execute()
            if (!("".equals(dir))) Cvar.Set("cddir", dir)
        }
    }
}
