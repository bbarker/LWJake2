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

package lwjake2.client

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.SZ
import lwjake2.qcommon.qfiles
import lwjake2.qcommon.xcommand_t
import lwjake2.sound.S
import lwjake2.sys.Timer
import lwjake2.util.Lib
import lwjake2.util.Vargs

import java.awt.Dimension
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

/**
 * SCR
 */
public class SCR : Globals() {

    class dirty_t {
        var x1: Int = 0

        var x2: Int = 0

        var y1: Int = 0

        var y2: Int = 0

        fun set(src: dirty_t) {
            x1 = src.x1
            x2 = src.x2
            y1 = src.y1
            y2 = src.y2
        }

        fun clear() {
            x1 = 0
            x2 = 0
            y1 = 0
            y2 = 0
        }
    }

    /*
     * ===============================================================================
     * 
     * BAR GRAPHS
     * 
     * ===============================================================================
     */

    //	typedef struct
    //	{
    //		float value;
    //		int color;
    //	} graphsamp_t;
    class graphsamp_t {
        var value: Float = 0.toFloat()

        var color: Int = 0
    }

    /*
     * =================================================================
     * 
     * cl_cin.c
     * 
     * Play Cinematics
     * 
     * =================================================================
     */

    private class cinematics_t {
        var restart_sound: Boolean = false
        var s_rate: Int = 0
        var s_width: Int = 0
        var s_channels: Int = 0

        var width: Int = 0
        var height: Int = 0
        var pic: ByteArray? = null
        var pic_pending: ByteArray? = null
        // order 1 huffman stuff
        var hnodes1: IntArray? = null // [256][256][2];
        var numhnodes1 = IntArray(256)

        var h_used = IntArray(512)
        var h_count = IntArray(512)
    }

    companion object {

        //	cl_scrn.c -- master for refresh, status bar, console, chat, notify, etc

        var sb_nums = array<Array<String>>(array<String>("num_0", "num_1", "num_2", "num_3", "num_4", "num_5", "num_6", "num_7", "num_8", "num_9", "num_minus"), array<String>("anum_0", "anum_1", "anum_2", "anum_3", "anum_4", "anum_5", "anum_6", "anum_7", "anum_8", "anum_9", "anum_minus"))

        /*
     * full screen console put up loading plaque blanked background with loading
     * plaque blanked background with menu cinematics full screen image for quit
     * and victory
     * 
     * end of unit intermissions
     */

        var scr_con_current: Float = 0.toFloat() // aproaches scr_conlines at scr_conspeed

        var scr_conlines: Float = 0.toFloat() // 0.0 to 1.0 lines of console to display

        var scr_initialized: Boolean = false // ready to draw

        var scr_draw_loading: Int = 0

        // scr_vrect ist in Globals definiert
        // position of render window on screen

        var scr_viewsize: cvar_t

        var scr_conspeed: cvar_t

        var scr_centertime: cvar_t

        var scr_showturtle: cvar_t

        var scr_showpause: cvar_t

        var scr_printspeed: cvar_t

        var scr_netgraph: cvar_t

        var scr_timegraph: cvar_t

        var scr_debuggraph: cvar_t

        var scr_graphheight: cvar_t

        var scr_graphscale: cvar_t

        var scr_graphshift: cvar_t

        var scr_drawall: cvar_t

        public var fps: cvar_t = cvar_t()

        var scr_dirty = dirty_t()

        var scr_old_dirty = array<dirty_t>(dirty_t(), dirty_t())

        var crosshair_pic: String

        var crosshair_width: Int = 0
        var crosshair_height: Int = 0

        var current: Int = 0

        var values = arrayOfNulls<graphsamp_t>(1024)

        {
            for (n in 0..1024 - 1)
                values[n] = graphsamp_t()
        }

        /*
     * ============== SCR_DebugGraph ==============
     */
        public fun DebugGraph(value: Float, color: Int) {
            values[current and 1023].value = value
            values[current and 1023].color = color
            current++
        }

        /*
     * ============== SCR_DrawDebugGraph ==============
     */
        fun DrawDebugGraph() {
            var a: Int
            val x: Int
            val y: Int
            val w: Int
            var i: Int
            var h: Int
            var v: Float
            var color: Int

            // draw the graph

            w = scr_vrect.width

            x = scr_vrect.x
            y = scr_vrect.y + scr_vrect.height
            re.DrawFill(x, (y - scr_graphheight.value) as Int, w, scr_graphheight.value as Int, 8)

            run {
                a = 0
                while (a < w) {
                    i = (current - 1 - a + 1024) and 1023
                    v = values[i].value
                    color = values[i].color
                    v = v * scr_graphscale.value + scr_graphshift.value

                    if (v < 0)
                        v += scr_graphheight.value * (1 + (-v / scr_graphheight.value) as Int)
                    h = v.toInt() % scr_graphheight.value as Int
                    re.DrawFill(x + w - 1 - a, y - h, 1, h, color)
                    a++
                }
            }
        }

        /*
     * ===============================================================================
     * 
     * CENTER PRINTING
     * 
     * ===============================================================================
     */

        // char scr_centerstring[1024];
        var scr_centerstring: String

        var scr_centertime_start: Float = 0.toFloat() // for slow victory printing

        var scr_centertime_off: Float = 0.toFloat()

        var scr_center_lines: Int = 0

        var scr_erase_center: Int = 0

        /*
     * ============== SCR_CenterPrint
     * 
     * Called for important messages that should stay in the center of the
     * screen for a few moments ==============
     */
        fun CenterPrint(str: String) {
            //char *s;
            var s: Int
            val line = StringBuffer(64)
            var i: Int
            var j: Int
            var l: Int

            //strncpy (scr_centerstring, str, sizeof(scr_centerstring)-1);
            scr_centerstring = str
            scr_centertime_off = scr_centertime.value
            scr_centertime_start = cl.time

            // count the number of lines for centering
            scr_center_lines = 1
            s = 0
            while (s < str.length()) {
                if (str.charAt(s) == '\n')
                    scr_center_lines++
                s++
            }

            // echo it to the console
            Com.Printf("\n\n\35\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\37\n\n")

            s = 0

            if (str.length() != 0) {
                do {
                    // scan the width of the line

                    run {
                        l = 0
                        while (l < 40 && (l + s) < str.length()) {
                            if (str.charAt(s + l) == '\n' || str.charAt(s + l) == 0)
                                break
                            l++
                        }
                    }
                    run {
                        i = 0
                        while (i < (40 - l) / 2) {
                            line.append(' ')
                            i++
                        }
                    }

                    run {
                        j = 0
                        while (j < l) {
                            line.append(str.charAt(s + j))
                            j++
                        }
                    }

                    line.append('\n')

                    Com.Printf(line.toString())

                    while (s < str.length() && str.charAt(s) != '\n')
                        s++

                    if (s == str.length())
                        break
                    s++ // skip the \n
                } while (true)
            }
            Com.Printf("\n\n\35\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\37\n\n")
            Console.ClearNotify()
        }

        fun DrawCenterString() {
            val cs = scr_centerstring + "\0"
            var start: Int
            var l: Int
            var j: Int
            var x: Int
            var y: Int
            var remaining: Int

            if (cs == null || cs.length() == 0)
                return

            // the finale prints the characters one at a time
            remaining = 9999

            scr_erase_center = 0
            start = 0

            if (scr_center_lines <= 4)
                y = (viddef.height * 0.35) as Int
            else
                y = 48

            do {
                // scan the width of the line
                run {
                    l = 0
                    while (l < 40) {
                        if (start + l == cs.length() - 1 || cs.charAt(start + l) == '\n')
                            break
                        l++
                    }
                }
                x = (viddef.width - l * 8) / 2
                SCR.AddDirtyPoint(x, y)
                run {
                    j = 0
                    while (j < l) {
                        re.DrawChar(x, y, cs.charAt(start + j))
                        if (remaining == 0)
                            return
                        remaining--
                        j++
                        x += 8
                    }
                }
                SCR.AddDirtyPoint(x, y + 8)

                y += 8

                while (start < cs.length() && cs.charAt(start) != '\n')
                    start++

                if (start == cs.length())
                    break
                start++ // skip the \n
            } while (true)
        }

        fun CheckDrawCenterString() {
            scr_centertime_off -= cls.frametime

            if (scr_centertime_off <= 0)
                return

            DrawCenterString()
        }

        // =============================================================================

        /*
     * ================= SCR_CalcVrect
     * 
     * Sets scr_vrect, the coordinates of the rendered window =================
     */
        fun CalcVrect() {
            val size: Int

            // bound viewsize
            if (scr_viewsize.value < 40)
                Cvar.Set("viewsize", "40")
            if (scr_viewsize.value > 100)
                Cvar.Set("viewsize", "100")

            size = scr_viewsize.value as Int

            scr_vrect.width = viddef.width * size / 100
            scr_vrect.width = scr_vrect.width and 7.inv()

            scr_vrect.height = viddef.height * size / 100
            scr_vrect.height = scr_vrect.height and 1.inv()

            scr_vrect.x = (viddef.width - scr_vrect.width) / 2
            scr_vrect.y = (viddef.height - scr_vrect.height) / 2
        }

        /*
     * ================= SCR_SizeUp_f
     * 
     * Keybinding command =================
     */
        fun SizeUp_f() {
            Cvar.SetValue("viewsize", scr_viewsize.value + 10)
        }

        /*
     * ================= SCR_SizeDown_f
     * 
     * Keybinding command =================
     */
        fun SizeDown_f() {
            Cvar.SetValue("viewsize", scr_viewsize.value - 10)
        }

        /*
     * ================= SCR_Sky_f
     * 
     * Set a specific sky and rotation speed =================
     */
        fun Sky_f() {
            val rotate: Float
            val axis = floatArray(0.0, 0.0, 0.0)

            if (Cmd.Argc() < 2) {
                Com.Printf("Usage: sky <basename> <rotate> <axis x y z>\n")
                return
            }
            if (Cmd.Argc() > 2)
                rotate = Float.parseFloat(Cmd.Argv(2))
            else
                rotate = 0
            if (Cmd.Argc() == 6) {
                axis[0] = Float.parseFloat(Cmd.Argv(3))
                axis[1] = Float.parseFloat(Cmd.Argv(4))
                axis[2] = Float.parseFloat(Cmd.Argv(5))
            } else {
                axis[0] = 0
                axis[1] = 0
                axis[2] = 1
            }

            re.SetSky(Cmd.Argv(1), rotate, axis)
        }

        // ============================================================================

        /*
     * ================== SCR_Init ==================
     */
        fun Init() {
            scr_viewsize = Cvar.Get("viewsize", "100", CVAR_ARCHIVE)
            scr_conspeed = Cvar.Get("scr_conspeed", "3", 0)
            scr_showturtle = Cvar.Get("scr_showturtle", "0", 0)
            scr_showpause = Cvar.Get("scr_showpause", "1", 0)
            scr_centertime = Cvar.Get("scr_centertime", "2.5", 0)
            scr_printspeed = Cvar.Get("scr_printspeed", "8", 0)
            scr_netgraph = Cvar.Get("netgraph", "1", 0)
            scr_timegraph = Cvar.Get("timegraph", "1", 0)
            scr_debuggraph = Cvar.Get("debuggraph", "1", 0)
            scr_graphheight = Cvar.Get("graphheight", "32", 0)
            scr_graphscale = Cvar.Get("graphscale", "1", 0)
            scr_graphshift = Cvar.Get("graphshift", "0", 0)
            scr_drawall = Cvar.Get("scr_drawall", "1", 0)
            fps = Cvar.Get("fps", "0", 0)

            //
            // register our commands
            //
            Cmd.AddCommand("timerefresh", object : xcommand_t() {
                public fun execute() {
                    TimeRefresh_f()
                }
            })
            Cmd.AddCommand("loading", object : xcommand_t() {
                public fun execute() {
                    Loading_f()
                }
            })
            Cmd.AddCommand("sizeup", object : xcommand_t() {
                public fun execute() {
                    SizeUp_f()
                }
            })
            Cmd.AddCommand("sizedown", object : xcommand_t() {
                public fun execute() {
                    SizeDown_f()
                }
            })
            Cmd.AddCommand("sky", object : xcommand_t() {
                public fun execute() {
                    Sky_f()
                }
            })

            scr_initialized = true
        }

        /*
     * ============== SCR_DrawNet ==============
     */
        fun DrawNet() {
            if (cls.netchan.outgoing_sequence - cls.netchan.incoming_acknowledged < CMD_BACKUP - 1)
                return

            re.DrawPic(scr_vrect.x + 64, scr_vrect.y, "net")
        }

        /*
     * ============== SCR_DrawPause ==============
     */
        fun DrawPause() {
            val dim = Dimension()

            if (scr_showpause.value == 0)
            // turn off for screenshots
                return

            if (cl_paused.value == 0)
                return

            re.DrawGetPicSize(dim, "pause")
            re.DrawPic((viddef.width - dim.width) / 2, viddef.height / 2 + 8, "pause")
        }

        /*
     * ============== SCR_DrawLoading ==============
     */
        fun DrawLoading() {
            val dim = Dimension()

            if (scr_draw_loading == 0)
                return

            scr_draw_loading = 0
            re.DrawGetPicSize(dim, "loading")
            re.DrawPic((viddef.width - dim.width) / 2, (viddef.height - dim.height) / 2, "loading")
        }

        // =============================================================================

        /*
     * ================== SCR_RunConsole
     * 
     * Scroll it up or down ==================
     */
        fun RunConsole() {
            // decide on the height of the console
            if (cls.key_dest == key_console)
                scr_conlines = 0.5.toFloat() // half screen
            else
                scr_conlines = 0 // none visible

            if (scr_conlines < scr_con_current) {
                scr_con_current -= scr_conspeed.value * cls.frametime
                if (scr_conlines > scr_con_current)
                    scr_con_current = scr_conlines

            } else if (scr_conlines > scr_con_current) {
                scr_con_current += scr_conspeed.value * cls.frametime
                if (scr_conlines < scr_con_current)
                    scr_con_current = scr_conlines
            }
        }

        /*
     * ================== SCR_DrawConsole ==================
     */
        fun DrawConsole() {
            Console.CheckResize()

            if (cls.state == ca_disconnected || cls.state == ca_connecting) {
                // forced
                // full
                // screen
                // console
                Console.DrawConsole(1.0.toFloat())
                return
            }

            if (cls.state != ca_active || !cl.refresh_prepped) {
                // connected, but
                // can't render
                Console.DrawConsole(0.5.toFloat())
                re.DrawFill(0, viddef.height / 2, viddef.width, viddef.height / 2, 0)
                return
            }

            if (scr_con_current != 0) {
                Console.DrawConsole(scr_con_current)
            } else {
                if (cls.key_dest == key_game || cls.key_dest == key_message)
                    Console.DrawNotify() // only draw notify in game
            }
        }

        // =============================================================================

        /*
     * ================ SCR_BeginLoadingPlaque ================
     */
        public fun BeginLoadingPlaque() {
            S.StopAllSounds()
            cl.sound_prepped = false // don't play ambients

            if (cls.disable_screen != 0)
                return
            if (developer.value != 0)
                return
            if (cls.state == ca_disconnected)
                return  // if at console, don't bring up the plaque
            if (cls.key_dest == key_console)
                return
            if (cl.cinematictime > 0)
                scr_draw_loading = 2 // clear to balack first
            else
                scr_draw_loading = 1

            UpdateScreen()
            cls.disable_screen = Timer.Milliseconds()
            cls.disable_servercount = cl.servercount
        }

        /*
     * ================ SCR_EndLoadingPlaque ================
     */
        public fun EndLoadingPlaque() {
            cls.disable_screen = 0
            Console.ClearNotify()
        }

        /*
     * ================ SCR_Loading_f ================
     */
        fun Loading_f() {
            BeginLoadingPlaque()
        }

        /*
     * ================ SCR_TimeRefresh_f ================
     */
        fun TimeRefresh_f() {
            var i: Int
            val start: Int
            val stop: Int
            val time: Float

            if (cls.state != ca_active)
                return

            start = Timer.Milliseconds()

            if (Cmd.Argc() == 2) {
                // run without page flipping
                re.BeginFrame(0)
                run {
                    i = 0
                    while (i < 128) {
                        cl.refdef.viewangles[1] = i.toFloat() / 128.0.toFloat() * 360.0.toFloat()
                        re.RenderFrame(cl.refdef)
                        i++
                    }
                }
                re.EndFrame()
            } else {
                run {
                    i = 0
                    while (i < 128) {
                        cl.refdef.viewangles[1] = i.toFloat() / 128.0.toFloat() * 360.0.toFloat()

                        re.BeginFrame(0)
                        re.RenderFrame(cl.refdef)
                        re.EndFrame()
                        i++
                    }
                }
            }

            stop = Timer.Milliseconds()
            time = (stop - start).toFloat() / 1000.0.toFloat()
            Com.Printf("%f seconds (%f fps)\n", Vargs(2).add(time).add(128.0.toFloat() / time))
        }

        fun DirtyScreen() {
            AddDirtyPoint(0, 0)
            AddDirtyPoint(viddef.width - 1, viddef.height - 1)
        }

        /*
     * ============== SCR_TileClear
     * 
     * Clear any parts of the tiled background that were drawn on last frame
     * ==============
     */

        var clear = dirty_t()

        fun TileClear() {
            var i: Int
            var top: Int
            val bottom: Int
            val left: Int
            val right: Int
            clear.clear()

            if (scr_drawall.value != 0)
                DirtyScreen() // for power vr or broken page flippers...

            if (scr_con_current == 1.0.toFloat())
                return  // full screen console
            if (scr_viewsize.value == 100)
                return  // full screen rendering
            if (cl.cinematictime > 0)
                return  // full screen cinematic

            // erase rect will be the union of the past three frames
            // so tripple buffering works properly
            clear.set(scr_dirty)
            run {
                i = 0
                while (i < 2) {
                    if (scr_old_dirty[i].x1 < clear.x1)
                        clear.x1 = scr_old_dirty[i].x1
                    if (scr_old_dirty[i].x2 > clear.x2)
                        clear.x2 = scr_old_dirty[i].x2
                    if (scr_old_dirty[i].y1 < clear.y1)
                        clear.y1 = scr_old_dirty[i].y1
                    if (scr_old_dirty[i].y2 > clear.y2)
                        clear.y2 = scr_old_dirty[i].y2
                    i++
                }
            }

            scr_old_dirty[1].set(scr_old_dirty[0])
            scr_old_dirty[0].set(scr_dirty)

            scr_dirty.x1 = 9999
            scr_dirty.x2 = -9999
            scr_dirty.y1 = 9999
            scr_dirty.y2 = -9999

            // don't bother with anything convered by the console)
            top = (scr_con_current * viddef.height) as Int
            if (top >= clear.y1)
                clear.y1 = top

            if (clear.y2 <= clear.y1)
                return  // nothing disturbed

            top = scr_vrect.y
            bottom = top + scr_vrect.height - 1
            left = scr_vrect.x
            right = left + scr_vrect.width - 1

            if (clear.y1 < top) {
                // clear above view screen
                i = if (clear.y2 < top - 1) clear.y2 else top - 1
                re.DrawTileClear(clear.x1, clear.y1, clear.x2 - clear.x1 + 1, i - clear.y1 + 1, "backtile")
                clear.y1 = top
            }
            if (clear.y2 > bottom) {
                // clear below view screen
                i = if (clear.y1 > bottom + 1) clear.y1 else bottom + 1
                re.DrawTileClear(clear.x1, i, clear.x2 - clear.x1 + 1, clear.y2 - i + 1, "backtile")
                clear.y2 = bottom
            }
            if (clear.x1 < left) {
                // clear left of view screen
                i = if (clear.x2 < left - 1) clear.x2 else left - 1
                re.DrawTileClear(clear.x1, clear.y1, i - clear.x1 + 1, clear.y2 - clear.y1 + 1, "backtile")
                clear.x1 = left
            }
            if (clear.x2 > right) {
                // clear left of view screen
                i = if (clear.x1 > right + 1) clear.x1 else right + 1
                re.DrawTileClear(i, clear.y1, clear.x2 - i + 1, clear.y2 - clear.y1 + 1, "backtile")
                clear.x2 = right
            }

        }

        // ===============================================================

        val STAT_MINUS = 10 // num frame for '-' stats digit

        val ICON_WIDTH = 24

        val ICON_HEIGHT = 24

        val CHAR_WIDTH = 16

        val ICON_SPACE = 8

        /*
     * ================ SizeHUDString
     * 
     * Allow embedded \n in the string ================
     */
        fun SizeHUDString(string: String, dim: Dimension) {
            var lines: Int
            var width: Int
            var current: Int

            lines = 1
            width = 0

            current = 0
            for (i in 0..string.length() - 1) {
                if (string.charAt(i) == '\n') {
                    lines++
                    current = 0
                } else {
                    current++
                    if (current > width)
                        width = current
                }

            }

            dim.width = width * 8
            dim.height = lines * 8
        }

        fun DrawHUDString(string: String, x: Int, y: Int, centerwidth: Int, xor: Int) {
            var x = x
            var y = y
            val margin: Int
            //char line[1024];
            var line = StringBuffer(1024)
            var i: Int

            margin = x

            run {
                var l = 0
                while (l < string.length()) {
                    // scan out one line of text from the string
                    line = StringBuffer(1024)
                    while (l < string.length() && string.charAt(l) != '\n') {
                        line.append(string.charAt(l))
                        l++
                    }

                    if (centerwidth != 0)
                        x = margin + (centerwidth - line.length() * 8) / 2
                    else
                        x = margin
                    run {
                        i = 0
                        while (i < line.length()) {
                            re.DrawChar(x, y, line.charAt(i) xor xor)
                            x += 8
                            i++
                        }
                    }
                    if (l < string.length()) {
                        l++ // skip the \n
                        x = margin
                        y += 8
                    }
                }
            }
        }

        /*
     * ============== SCR_DrawField ==============
     */
        fun DrawField(x: Int, y: Int, color: Int, width: Int, value: Int) {
            var x = x
            var width = width
            var ptr: Char
            val num: String
            var l: Int
            val frame: Int

            if (width < 1)
                return

            // draw number string
            if (width > 5)
                width = 5

            AddDirtyPoint(x, y)
            AddDirtyPoint(x + width * CHAR_WIDTH + 2, y + 23)

            num = "" + value
            l = num.length()
            if (l > width)
                l = width
            x += 2 + CHAR_WIDTH * (width - l)

            ptr = num.charAt(0)

            for (i in 0..l - 1) {
                ptr = num.charAt(i)
                if (ptr == '-')
                    frame = STAT_MINUS
                else
                    frame = ptr.toInt() - '0'

                re.DrawPic(x, y, sb_nums[color][frame])
                x += CHAR_WIDTH
            }
        }

        /*
     * =============== SCR_TouchPics
     * 
     * Allows rendering code to cache all needed sbar graphics ===============
     */
        fun TouchPics() {
            var i: Int
            var j: Int

            run {
                i = 0
                while (i < 2) {
                    run {
                        j = 0
                        while (j < 11) {
                            re.RegisterPic(sb_nums[i][j])
                            j++
                        }
                    }
                    i++
                }
            }

            if (crosshair.value != 0.0.toFloat()) {
                if (crosshair.value > 3.0.toFloat() || crosshair.value < 0.0.toFloat())
                    crosshair.value = 3.0.toFloat()

                crosshair_pic = "ch" + crosshair.value as Int
                val dim = Dimension()
                re.DrawGetPicSize(dim, crosshair_pic)
                crosshair_width = dim.width
                crosshair_height = dim.height
                if (crosshair_width == 0)
                    crosshair_pic = ""
            }
        }

        /*
     * ================ SCR_ExecuteLayoutString
     * 
     * ================
     */
        fun ExecuteLayoutString(s: String?) {
            var x: Int
            var y: Int
            val value: Int
            var token: String
            var width: Int
            var index: Int
            var ci: clientinfo_t

            if (cls.state != ca_active || !cl.refresh_prepped)
                return

            //		if (!s[0])
            if (s == null || s.length() == 0)
                return

            x = 0
            y = 0
            width = 3

            val ph = Com.ParseHelp(s)

            while (!ph.isEof()) {
                token = Com.Parse(ph)
                if (token.equals("xl")) {
                    token = Com.Parse(ph)
                    x = Lib.atoi(token)
                    continue
                }
                if (token.equals("xr")) {
                    token = Com.Parse(ph)
                    x = viddef.width + Lib.atoi(token)
                    continue
                }
                if (token.equals("xv")) {
                    token = Com.Parse(ph)
                    x = viddef.width / 2 - 160 + Lib.atoi(token)
                    continue
                }

                if (token.equals("yt")) {
                    token = Com.Parse(ph)
                    y = Lib.atoi(token)
                    continue
                }
                if (token.equals("yb")) {
                    token = Com.Parse(ph)
                    y = viddef.height + Lib.atoi(token)
                    continue
                }
                if (token.equals("yv")) {
                    token = Com.Parse(ph)
                    y = viddef.height / 2 - 120 + Lib.atoi(token)
                    continue
                }

                if (token.equals("pic")) {
                    // draw a pic from a stat number
                    token = Com.Parse(ph)
                    value = cl.frame.playerstate.stats[Lib.atoi(token)]
                    if (value >= MAX_IMAGES)
                        Com.Error(ERR_DROP, "Pic >= MAX_IMAGES")
                    if (cl.configstrings[CS_IMAGES + value] != null) {
                        AddDirtyPoint(x, y)
                        AddDirtyPoint(x + 23, y + 23)
                        re.DrawPic(x, y, cl.configstrings[CS_IMAGES + value])
                    }
                    continue
                }

                if (token.equals("client")) {
                    // draw a deathmatch client block
                    val score: Int
                    val ping: Int
                    val time: Int

                    token = Com.Parse(ph)
                    x = viddef.width / 2 - 160 + Lib.atoi(token)
                    token = Com.Parse(ph)
                    y = viddef.height / 2 - 120 + Lib.atoi(token)
                    AddDirtyPoint(x, y)
                    AddDirtyPoint(x + 159, y + 31)

                    token = Com.Parse(ph)
                    value = Lib.atoi(token)
                    if (value >= MAX_CLIENTS || value < 0)
                        Com.Error(ERR_DROP, "client >= MAX_CLIENTS")
                    ci = cl.clientinfo[value]

                    token = Com.Parse(ph)
                    score = Lib.atoi(token)

                    token = Com.Parse(ph)
                    ping = Lib.atoi(token)

                    token = Com.Parse(ph)
                    time = Lib.atoi(token)

                    Console.DrawAltString(x + 32, y, ci.name)
                    Console.DrawString(x + 32, y + 8, "Score: ")
                    Console.DrawAltString(x + 32 + 7 * 8, y + 8, "" + score)
                    Console.DrawString(x + 32, y + 16, "Ping:  " + ping)
                    Console.DrawString(x + 32, y + 24, "Time:  " + time)

                    if (ci.icon == null)
                        ci = cl.baseclientinfo
                    re.DrawPic(x, y, ci.iconname)
                    continue
                }

                if (token.equals("ctf")) {
                    // draw a ctf client block
                    val score: Int
                    var ping: Int

                    token = Com.Parse(ph)
                    x = viddef.width / 2 - 160 + Lib.atoi(token)
                    token = Com.Parse(ph)
                    y = viddef.height / 2 - 120 + Lib.atoi(token)
                    AddDirtyPoint(x, y)
                    AddDirtyPoint(x + 159, y + 31)

                    token = Com.Parse(ph)
                    value = Lib.atoi(token)
                    if (value >= MAX_CLIENTS || value < 0)
                        Com.Error(ERR_DROP, "client >= MAX_CLIENTS")
                    ci = cl.clientinfo[value]

                    token = Com.Parse(ph)
                    score = Lib.atoi(token)

                    token = Com.Parse(ph)
                    ping = Lib.atoi(token)
                    if (ping > 999)
                        ping = 999

                    // sprintf(block, "%3d %3d %-12.12s", score, ping, ci->name);
                    val block = Com.sprintf("%3d %3d %-12.12s", Vargs(3).add(score).add(ping).add(ci.name))

                    if (value == cl.playernum)
                        Console.DrawAltString(x, y, block)
                    else
                        Console.DrawString(x, y, block)
                    continue
                }

                if (token.equals("picn")) {
                    // draw a pic from a name
                    token = Com.Parse(ph)
                    AddDirtyPoint(x, y)
                    AddDirtyPoint(x + 23, y + 23)
                    re.DrawPic(x, y, token)
                    continue
                }

                if (token.equals("num")) {
                    // draw a number
                    token = Com.Parse(ph)
                    width = Lib.atoi(token)
                    token = Com.Parse(ph)
                    value = cl.frame.playerstate.stats[Lib.atoi(token)]
                    DrawField(x, y, 0, width, value)
                    continue
                }

                if (token.equals("hnum")) {
                    // health number
                    val color: Int

                    width = 3
                    value = cl.frame.playerstate.stats[STAT_HEALTH]
                    if (value > 25)
                        color = 0 // green
                    else if (value > 0)
                        color = (cl.frame.serverframe shr 2) and 1 // flash
                    else
                        color = 1

                    if ((cl.frame.playerstate.stats[STAT_FLASHES] and 1) != 0)
                        re.DrawPic(x, y, "field_3")

                    DrawField(x, y, color, width, value)
                    continue
                }

                if (token.equals("anum")) {
                    // ammo number
                    val color: Int

                    width = 3
                    value = cl.frame.playerstate.stats[STAT_AMMO]
                    if (value > 5)
                        color = 0 // green
                    else if (value >= 0)
                        color = (cl.frame.serverframe shr 2) and 1 // flash
                    else
                        continue // negative number = don't show

                    if ((cl.frame.playerstate.stats[STAT_FLASHES] and 4) != 0)
                        re.DrawPic(x, y, "field_3")

                    DrawField(x, y, color, width, value)
                    continue
                }

                if (token.equals("rnum")) {
                    // armor number
                    val color: Int

                    width = 3
                    value = cl.frame.playerstate.stats[STAT_ARMOR]
                    if (value < 1)
                        continue

                    color = 0 // green

                    if ((cl.frame.playerstate.stats[STAT_FLASHES] and 2) != 0)
                        re.DrawPic(x, y, "field_3")

                    DrawField(x, y, color, width, value)
                    continue
                }

                if (token.equals("stat_string")) {
                    token = Com.Parse(ph)
                    index = Lib.atoi(token)
                    if (index < 0 || index >= MAX_CONFIGSTRINGS)
                        Com.Error(ERR_DROP, "Bad stat_string index")
                    index = cl.frame.playerstate.stats[index]
                    if (index < 0 || index >= MAX_CONFIGSTRINGS)
                        Com.Error(ERR_DROP, "Bad stat_string index")
                    Console.DrawString(x, y, cl.configstrings[index])
                    continue
                }

                if (token.equals("cstring")) {
                    token = Com.Parse(ph)
                    DrawHUDString(token, x, y, 320, 0)
                    continue
                }

                if (token.equals("string")) {
                    token = Com.Parse(ph)
                    Console.DrawString(x, y, token)
                    continue
                }

                if (token.equals("cstring2")) {
                    token = Com.Parse(ph)
                    DrawHUDString(token, x, y, 320, 128)
                    continue
                }

                if (token.equals("string2")) {
                    token = Com.Parse(ph)
                    Console.DrawAltString(x, y, token)
                    continue
                }

                if (token.equals("if")) {
                    // draw a number
                    token = Com.Parse(ph)
                    value = cl.frame.playerstate.stats[Lib.atoi(token)]
                    if (value == 0) {
                        // skip to endif
                        while (!ph.isEof() && !(token = Com.Parse(ph)).equals("endif"))
                        { /* nothing */ }
                    }
                    continue
                }

            }
        }

        /*
     * ================ SCR_DrawStats
     * 
     * The status bar is a small layout program that is based on the stats array
     * ================
     */
        fun DrawStats() {
            //TODO:
            SCR.ExecuteLayoutString(cl.configstrings[CS_STATUSBAR])
        }

        /*
     * ================ SCR_DrawLayout
     * 
     * ================
     */
        val STAT_LAYOUTS = 13

        fun DrawLayout() {
            if (cl.frame.playerstate.stats[STAT_LAYOUTS] != 0)
                SCR.ExecuteLayoutString(cl.layout)
        }

        // =======================================================

        /*
     * ================== SCR_UpdateScreen
     * 
     * This is called every frame, and can also be called explicitly to flush
     * text to the screen. ==================
     */
        private val separation = floatArray(0.0, 0.0)

        fun UpdateScreen2() {
            val numframes: Int
            var i: Int
            // if the screen is disabled (loading plaque is up, or vid mode
            // changing)
            // do nothing at all
            if (cls.disable_screen != 0) {
                if (Timer.Milliseconds() - cls.disable_screen > 120000) {
                    cls.disable_screen = 0
                    Com.Printf("Loading plaque timed out.\n")
                }
                return
            }

            if (!scr_initialized || !con.initialized)
                return  // not initialized yet

            /*
         * * range check cl_camera_separation so we don't inadvertently fry
         * someone's * brain
         */
            if (cl_stereo_separation.value > 1.0)
                Cvar.SetValue("cl_stereo_separation", 1.0.toFloat())
            else if (cl_stereo_separation.value < 0)
                Cvar.SetValue("cl_stereo_separation", 0.0.toFloat())

            if (cl_stereo.value != 0) {
                numframes = 2
                separation[0] = -cl_stereo_separation.value / 2
                separation[1] = cl_stereo_separation.value / 2
            } else {
                separation[0] = 0
                separation[1] = 0
                numframes = 1
            }

            run {
                i = 0
                while (i < numframes) {
                    re.BeginFrame(separation[i])

                    if (scr_draw_loading == 2) {
                        //  loading plaque over black screen
                        val dim = Dimension()

                        re.CinematicSetPalette(null)
                        scr_draw_loading = 0 // false
                        re.DrawGetPicSize(dim, "loading")
                        re.DrawPic((viddef.width - dim.width) / 2, (viddef.height - dim.height) / 2, "loading")
                    } else if (cl.cinematictime > 0) {
                        if (cls.key_dest == key_menu) {
                            if (cl.cinematicpalette_active) {
                                re.CinematicSetPalette(null)
                                cl.cinematicpalette_active = false
                            }
                            Menu.Draw()
                        } else if (cls.key_dest == key_console) {
                            if (cl.cinematicpalette_active) {
                                re.CinematicSetPalette(null)
                                cl.cinematicpalette_active = false
                            }
                            DrawConsole()
                        } else {
                            // TODO implement cinematics completely
                            DrawCinematic()
                        }
                    } else {
                        // make sure the game palette is active
                        if (cl.cinematicpalette_active) {
                            re.CinematicSetPalette(null)
                            cl.cinematicpalette_active = false
                        }

                        // do 3D refresh drawing, and then update the screen
                        CalcVrect()

                        // clear any dirty part of the background
                        TileClear()

                        V.RenderView(separation[i])

                        DrawStats()

                        if ((cl.frame.playerstate.stats[STAT_LAYOUTS] and 1) != 0)
                            DrawLayout()
                        if ((cl.frame.playerstate.stats[STAT_LAYOUTS] and 2) != 0)
                            CL_inv.DrawInventory()

                        DrawNet()
                        CheckDrawCenterString()
                        DrawFPS()

                        //
                        //				if (scr_timegraph->value)
                        //					SCR_DebugGraph (cls.frametime*300, 0);
                        //
                        //				if (scr_debuggraph->value || scr_timegraph->value ||
                        // scr_netgraph->value)
                        //					SCR_DrawDebugGraph ();
                        //
                        DrawPause()
                        DrawConsole()
                        Menu.Draw()
                        DrawLoading()
                    }// if a cinematic is supposed to be running, handle menus
                    // and console specially
                    i++
                }
            }

            Globals.re.EndFrame()
        }

        /*
     * ================= SCR_DrawCrosshair =================
     */
        fun DrawCrosshair() {
            if (crosshair.value == 0.0.toFloat())
                return

            if (crosshair.modified) {
                crosshair.modified = false
                SCR.TouchPics()
            }

            if (crosshair_pic.length() == 0)
                return

            re.DrawPic(scr_vrect.x + ((scr_vrect.width - crosshair_width) shr 1), scr_vrect.y + ((scr_vrect.height - crosshair_height) shr 1), crosshair_pic)
        }

        private val updateScreenCallback = object : xcommand_t() {
            public fun execute() {
                UpdateScreen2()
            }
        }

        // wird anstelle von der richtigen UpdateScreen benoetigt
        public fun UpdateScreen() {
            Globals.re.updateScreen(updateScreenCallback)
        }

        /*
     * ================= SCR_AddDirtyPoint =================
     */
        fun AddDirtyPoint(x: Int, y: Int) {
            if (x < scr_dirty.x1)
                scr_dirty.x1 = x
            if (x > scr_dirty.x2)
                scr_dirty.x2 = x
            if (y < scr_dirty.y1)
                scr_dirty.y1 = y
            if (y > scr_dirty.y2)
                scr_dirty.y2 = y
        }

        private var lastframes = 0

        private var lasttime = 0

        private var fpsvalue = ""

        fun DrawFPS() {
            if (fps.value > 0.0.toFloat()) {
                if (fps.modified) {
                    fps.modified = false
                    Cvar.SetValue("cl_maxfps", 1000)
                }

                val diff = cls.realtime - lasttime
                if (diff > (fps.value * 1000) as Int) {
                    fpsvalue = (cls.framecount - lastframes) * 100000 / diff / 100.0.toFloat() + " fps"
                    lastframes = cls.framecount
                    lasttime = cls.realtime
                }
                var x = viddef.width - 8 * fpsvalue.length() - 2
                for (i in 0..fpsvalue.length() - 1) {
                    re.DrawChar(x, 2, fpsvalue.charAt(i))
                    x += 8
                }
            } else if (fps.modified) {
                fps.modified = false
                Cvar.SetValue("cl_maxfps", 90)
            }
        }

        private val cin = cinematics_t()

        /**
         * LoadPCX
         */
        fun LoadPCX(filename: String, palette: ByteArray?, cin: cinematics_t?): Int {
            val pcx: qfiles.pcx_t

            // load the file
            val raw = FS.LoadMappedFile(filename)

            if (raw == null) {
                VID.Printf(Defines.PRINT_DEVELOPER, "Bad pcx file " + filename + '\n')
                return 0
            }

            // parse the PCX file
            pcx = qfiles.pcx_t(raw)

            if (pcx.manufacturer != 10 || pcx.version != 5 || pcx.encoding != 1 || pcx.bits_per_pixel != 8 || pcx.xmax >= 640 || pcx.ymax >= 480) {

                VID.Printf(Defines.PRINT_ALL, "Bad pcx file " + filename + '\n')
                return 0
            }

            val width = pcx.xmax - pcx.xmin + 1
            val height = pcx.ymax - pcx.ymin + 1

            val pix = ByteArray(width * height)

            if (palette != null) {
                raw!!.position(raw!!.limit() - 768)
                raw!!.get(palette)
            }

            if (cin != null) {
                cin.pic = pix
                cin.width = width
                cin.height = height
            }

            //
            // decode pcx
            //
            var count = 0
            var dataByte: Byte = 0
            var runLength = 0
            var x: Int
            var y: Int

            // simple counter for buffer indexing
            var p = 0

            run {
                y = 0
                while (y < height) {
                    run {
                        x = 0
                        while (x < width) {

                            dataByte = pcx.data.get(p++)

                            if ((dataByte and 192) == 192) {
                                runLength = dataByte and 63
                                dataByte = pcx.data.get(p++)
                                // write runLength pixel
                                while (runLength-- > 0) {
                                    pix[count++] = dataByte
                                    x++
                                }
                            } else {
                                // write one pixel
                                pix[count++] = dataByte
                                x++
                            }
                        }
                    }
                    y++
                }
            }
            return width * height
        }

        /**
         * StopCinematic
         */
        fun StopCinematic() {
            if (cin.restart_sound) {
                // done
                cl.cinematictime = 0
                cin.pic = null
                cin.pic_pending = null
                if (cl.cinematicpalette_active) {
                    re.CinematicSetPalette(null)
                    cl.cinematicpalette_active = false
                }
                if (cl.cinematic_file != null) {
                    // free the mapped byte buffer
                    cl.cinematic_file = null
                }
                if (cin.hnodes1 != null) {
                    cin.hnodes1 = null
                }

                S.disableStreaming()
                cin.restart_sound = false
            }
        }

        /**
         * FinishCinematic

         * Called when either the cinematic completes, or it is aborted
         */
        fun FinishCinematic() {
            // tell the server to advance to the next map / cinematic
            MSG.WriteByte(cls.netchan.message, clc_stringcmd)
            SZ.Print(cls.netchan.message, "nextserver " + cl.servercount + '\n')
        }

        // ==========================================================================

        /**
         * SmallestNode1

         */
        private fun SmallestNode1(numhnodes: Int): Int {

            var best = 99999999
            var bestnode = -1
            for (i in 0..numhnodes - 1) {
                if (cin.h_used[i] != 0)
                    continue
                if (cin.h_count[i] == 0)
                    continue
                if (cin.h_count[i] < best) {
                    best = cin.h_count[i]
                    bestnode = i
                }
            }

            if (bestnode == -1)
                return -1

            cin.h_used[bestnode] = 1 // true
            return bestnode
        }


        /**
         * Huff1TableInit

         * Reads the 64k counts table and initializes the node trees.

         */
        private fun Huff1TableInit() {
            val node: IntArray
            val counts = ByteArray(256)
            var numhnodes: Int

            cin.hnodes1 = IntArray(256 * 256 * 2)
            Arrays.fill(cin.hnodes1, 0)

            for (prev in 0..256 - 1) {
                Arrays.fill(cin.h_count, 0)
                Arrays.fill(cin.h_used, 0)

                // read a row of counts
                cl.cinematic_file.get(counts)
                for (j in 0..256 - 1)
                    cin.h_count[j] = counts[j] and 255

                // build the nodes
                numhnodes = 256
                val nodebase = 0 + prev * 256 * 2
                var index = 0
                node = cin.hnodes1
                while (numhnodes != 511) {
                    index = nodebase + (numhnodes - 256) * 2

                    // pick two lowest counts
                    node[index] = SmallestNode1(numhnodes)
                    if (node[index] == -1)
                        break // no more

                    node[index + 1] = SmallestNode1(numhnodes)
                    if (node[index + 1] == -1)
                        break

                    cin.h_count[numhnodes] = cin.h_count[node[index]] + cin.h_count[node[index + 1]]
                    numhnodes++
                }

                cin.numhnodes1[prev] = numhnodes - 1
            }
        }

        /**
         * Huff1Decompress

         */
        private fun Huff1Decompress(`in`: ByteArray, size: Int): ByteArray {
            // get decompressed count
            var count = (`in`[0] and 255) or ((`in`[1] and 255) shl 8) or ((`in`[2] and 255) shl 16) or ((`in`[3] and 255) shl 24)
            // used as index for in[];
            var input = 4
            val out = ByteArray(count)
            // used as index for out[];
            var out_p = 0

            // read bits

            val hnodesbase = -256 * 2 // nodes 0-255 aren't stored
            var index = hnodesbase
            val hnodes = cin.hnodes1
            var nodenum = cin.numhnodes1[0]
            var inbyte: Int
            while (count != 0) {
                inbyte = `in`[input++] and 255

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1

                if (nodenum < 256) {
                    index = hnodesbase + (nodenum shl 9)
                    out[out_p++] = nodenum.toByte()
                    if (--count == 0)
                        break
                    nodenum = cin.numhnodes1[nodenum]
                }
                nodenum = hnodes[index + nodenum * 2 + (inbyte and 1)]
                inbyte = inbyte shr 1
            }

            if (input != size && input != size + 1) {
                Com.Printf("Decompression overread by " + (input - size))
            }

            return out
        }

        private val compressed = ByteArray(131072)

        /**
         * ReadNextFrame
         */
        fun ReadNextFrame(): ByteArray? {

            val file = cl.cinematic_file

            // read the next frame
            val command = file.getInt()

            if (command == 2) {
                // last frame marker
                return null
            }

            if (command == 1) {
                // read palette
                file.get(cl.cinematicpalette)
                // dubious.... exposes an edge case
                cl.cinematicpalette_active = false
            }
            // decompress the next frame
            val size = file.getInt()
            if (size > compressed.size() || size < 1)
                Com.Error(ERR_DROP, "Bad compressed frame size:" + size)

            file.get(compressed, 0, size)

            // read sound
            val start = cl.cinematicframe * cin.s_rate / 14
            val end = (cl.cinematicframe + 1) * cin.s_rate / 14
            val count = end - start

            S.RawSamples(count, cin.s_rate, cin.s_width, cin.s_channels, file.slice())
            // skip the sound samples
            file.position(file.position() + count * cin.s_width * cin.s_channels)

            val pic = Huff1Decompress(compressed, size)
            cl.cinematicframe++

            return pic
        }

        /**
         * RunCinematic
         */
        fun RunCinematic() {
            if (cl.cinematictime <= 0) {
                StopCinematic()
                return
            }

            if (cl.cinematicframe == -1) {
                // static image
                return
            }

            if (cls.key_dest != key_game) {
                // pause if menu or console is up
                cl.cinematictime = cls.realtime - cl.cinematicframe * 1000 / 14
                return
            }

            val frame = ((cls.realtime - cl.cinematictime) * 14.0.toFloat() / 1000) as Int

            if (frame <= cl.cinematicframe)
                return

            if (frame > cl.cinematicframe + 1) {
                Com.Println("Dropped frame: " + frame + " > " + (cl.cinematicframe + 1))
                cl.cinematictime = cls.realtime - cl.cinematicframe * 1000 / 14
            }

            cin.pic = cin.pic_pending
            cin.pic_pending = ReadNextFrame()

            if (cin.pic_pending == null) {
                StopCinematic()
                FinishCinematic()
                // hack to get the black screen behind loading
                cl.cinematictime = 1
                BeginLoadingPlaque()
                cl.cinematictime = 0
                return
            }
        }

        /**
         * DrawCinematic

         * Returns true if a cinematic is active, meaning the view rendering should
         * be skipped.
         */
        fun DrawCinematic(): Boolean {
            if (cl.cinematictime <= 0) {
                return false
            }

            if (cls.key_dest == key_menu) {
                // blank screen and pause if menu is up
                Globals.re.CinematicSetPalette(null)
                cl.cinematicpalette_active = false
                return true
            }

            if (!cl.cinematicpalette_active) {
                re.CinematicSetPalette(cl.cinematicpalette)
                cl.cinematicpalette_active = true
            }

            if (cin.pic == null)
                return true

            Globals.re.DrawStretchRaw(0, 0, viddef.width, viddef.height, cin.width, cin.height, cin.pic)

            return true
        }

        /**
         * PlayCinematic
         */
        fun PlayCinematic(arg: String) {

            // make sure CD isn't playing music
            //CDAudio.Stop();

            cl.cinematicframe = 0
            if (arg.endsWith(".pcx")) {
                // static pcx image
                val name = "pics/" + arg
                val size = LoadPCX(name, cl.cinematicpalette, cin)
                cl.cinematicframe = -1
                cl.cinematictime = 1
                EndLoadingPlaque()
                cls.state = ca_active
                if (size == 0 || cin.pic == null) {
                    Com.Println(name + " not found.")
                    cl.cinematictime = 0
                }
                return
            }

            val name = "video/" + arg
            cl.cinematic_file = FS.LoadMappedFile(name)
            if (cl.cinematic_file == null) {
                //Com.Error(ERR_DROP, "Cinematic " + name + " not found.\n");
                FinishCinematic()
                // done
                cl.cinematictime = 0
                return
            }

            EndLoadingPlaque()

            cls.state = ca_active

            cl.cinematic_file.order(ByteOrder.LITTLE_ENDIAN)
            val file = cl.cinematic_file
            cin.width = file.getInt()
            cin.height = file.getInt()
            cin.s_rate = file.getInt()
            cin.s_width = file.getInt()
            cin.s_channels = file.getInt()

            Huff1TableInit()

            cin.restart_sound = true
            cl.cinematicframe = 0
            cin.pic = ReadNextFrame()
            cl.cinematictime = Timer.Milliseconds()
        }
    }
}