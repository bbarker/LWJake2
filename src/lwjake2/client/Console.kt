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
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.xcommand_t
import lwjake2.util.Lib
import lwjake2.util.Vargs

import java.io.IOException
import java.io.RandomAccessFile
import java.util.Arrays

/**
 * Console
 */
public class Console : Globals() {
    companion object {

        public var ToggleConsole_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                SCR.EndLoadingPlaque() // get rid of loading plaque

                if (Globals.cl.attractloop) {
                    Cbuf.AddText("killserver\n")
                    return
                }

                if (Globals.cls.state == Defines.ca_disconnected) {
                    // start the demo loop again
                    Cbuf.AddText("d1\n")
                    return
                }

                Key.ClearTyping()
                Console.ClearNotify()

                if (Globals.cls.key_dest == Defines.key_console) {
                    Menu.ForceMenuOff()
                    Cvar.Set("paused", "0")
                } else {
                    Menu.ForceMenuOff()
                    Globals.cls.key_dest = Defines.key_console

                    if (Cvar.VariableValue("maxclients") == 1 && Globals.server_state != 0)
                        Cvar.Set("paused", "1")
                }
            }
        }

        public var Clear_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Arrays.fill(Globals.con.text, ' '.toByte())
            }
        }

        public var Dump_f: xcommand_t = object : xcommand_t() {
            public fun execute() {

                var l: Int
                var x: Int
                var line: Int
                val f: RandomAccessFile?
                val buffer = ByteArray(1024)
                val name: String

                if (Cmd.Argc() != 2) {
                    Com.Printf("usage: condump <filename>\n")
                    return
                }

                //Com_sprintf (name, sizeof(name), "%s/%s.txt", FS_Gamedir(),
                // Cmd_Argv(1));
                name = FS.Gamedir() + "/" + Cmd.Argv(1) + ".txt"

                Com.Printf("Dumped console text to " + name + ".\n")
                FS.CreatePath(name)
                f = Lib.fopen(name, "rw")
                if (f == null) {
                    Com.Printf("ERROR: couldn't open.\n")
                    return
                }

                // skip empty lines
                run {
                    l = con.current - con.totallines + 1
                    while (l <= con.current) {
                        line = (l % con.totallines) * con.linewidth
                        run {
                            x = 0
                            while (x < con.linewidth) {
                                if (con.text[line + x] != ' ')
                                    break
                                x++
                            }
                        }
                        if (x != con.linewidth)
                            break
                        l++
                    }
                }

                // write the remaining lines
                buffer[con.linewidth] = 0
                while (l <= con.current) {
                    line = (l % con.totallines) * con.linewidth
                    //strncpy (buffer, line, con.linewidth);
                    System.arraycopy(con.text, line, buffer, 0, con.linewidth)
                    run {
                        x = con.linewidth - 1
                        while (x >= 0) {
                            if (buffer[x] == ' ')
                                buffer[x] = 0
                            else
                                break
                            x--
                        }
                    }
                    run {
                        x = 0
                        while (buffer[x] != 0) {
                            buffer[x] = buffer[x] and 127
                            x++
                        }
                    }

                    buffer[x] = '\n'
                    // fprintf (f, "%s\n", buffer);
                    try {
                        f!!.write(buffer, 0, x + 1)
                    } catch (e: IOException) {
                    }

                    l++
                }

                Lib.fclose(f)

            }
        }

        /**

         */
        public fun Init() {
            Globals.con.linewidth = -1

            CheckResize()

            Com.Printf("Console initialized.\n")

            //
            // register our commands
            //
            Globals.con_notifytime = Cvar.Get("con_notifytime", "3", 0)

            Cmd.AddCommand("toggleconsole", ToggleConsole_f)
            Cmd.AddCommand("togglechat", ToggleChat_f)
            Cmd.AddCommand("messagemode", MessageMode_f)
            Cmd.AddCommand("messagemode2", MessageMode2_f)
            Cmd.AddCommand("clear", Clear_f)
            Cmd.AddCommand("condump", Dump_f)
            Globals.con.initialized = true
        }

        /**
         * If the line width has changed, reformat the buffer.
         */
        public fun CheckResize() {

            var width = (Globals.viddef.width shr 3) - 2
            if (width > Defines.MAXCMDLINE) width = Defines.MAXCMDLINE

            if (width == Globals.con.linewidth)
                return

            if (width < 1) {
                // video hasn't been initialized yet
                width = 38
                Globals.con.linewidth = width
                Globals.con.totallines = Defines.CON_TEXTSIZE / Globals.con.linewidth
                Arrays.fill(Globals.con.text, ' '.toByte())
            } else {
                val oldwidth = Globals.con.linewidth
                Globals.con.linewidth = width
                val oldtotallines = Globals.con.totallines
                Globals.con.totallines = Defines.CON_TEXTSIZE / Globals.con.linewidth
                var numlines = oldtotallines

                if (Globals.con.totallines < numlines)
                    numlines = Globals.con.totallines

                var numchars = oldwidth

                if (Globals.con.linewidth < numchars)
                    numchars = Globals.con.linewidth

                val tbuf = ByteArray(Defines.CON_TEXTSIZE)
                System.arraycopy(Globals.con.text, 0, tbuf, 0, Defines.CON_TEXTSIZE)
                Arrays.fill(Globals.con.text, ' '.toByte())

                for (i in 0..numlines - 1) {
                    for (j in 0..numchars - 1) {
                        Globals.con.text[(Globals.con.totallines - 1 - i) * Globals.con.linewidth + j] = tbuf[((Globals.con.current - i + oldtotallines) % oldtotallines) * oldwidth + j]
                    }
                }

                Console.ClearNotify()
            }

            Globals.con.current = Globals.con.totallines - 1
            Globals.con.display = Globals.con.current
        }

        public fun ClearNotify() {
            var i: Int
            run {
                i = 0
                while (i < Defines.NUM_CON_TIMES) {
                    Globals.con.times[i] = 0
                    i++
                }
            }
        }

        fun DrawString(x: Int, y: Int, s: String) {
            var x = x
            for (i in 0..s.length() - 1) {
                Globals.re.DrawChar(x, y, s.charAt(i))
                x += 8
            }
        }

        fun DrawAltString(x: Int, y: Int, s: String) {
            var x = x
            for (i in 0..s.length() - 1) {
                Globals.re.DrawChar(x, y, s.charAt(i) xor 128)
                x += 8
            }
        }

        /*
     * ================ Con_ToggleChat_f ================
     */
        var ToggleChat_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Key.ClearTyping()

                if (cls.key_dest == key_console) {
                    if (cls.state == ca_active) {
                        Menu.ForceMenuOff()
                        cls.key_dest = key_game
                    }
                } else
                    cls.key_dest = key_console

                ClearNotify()
            }
        }

        /*
     * ================ Con_MessageMode_f ================
     */
        var MessageMode_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                chat_team = false
                cls.key_dest = key_message
            }
        }

        /*
     * ================ Con_MessageMode2_f ================
     */
        var MessageMode2_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                chat_team = true
                cls.key_dest = key_message
            }
        }

        /*
     * =============== Con_Linefeed ===============
     */
        fun Linefeed() {
            Globals.con.x = 0
            if (Globals.con.display == Globals.con.current)
                Globals.con.display++
            Globals.con.current++
            var i = (Globals.con.current % Globals.con.totallines) * Globals.con.linewidth
            val e = i + Globals.con.linewidth
            while (i++ < e)
                Globals.con.text[i] = ' '
        }

        /*
     * ================ Con_Print
     * 
     * Handles cursor positioning, line wrapping, etc All console printing must
     * go through this in order to be logged to disk If no console is visible,
     * the text will appear at the top of the game window ================
     */
        private var cr: Int = 0

        public fun Print(txt: String) {
            val y: Int
            val c: Int
            var l: Int
            val mask: Int
            var txtpos = 0

            if (!con.initialized)
                return

            if (txt.charAt(0) == 1 || txt.charAt(0) == 2) {
                mask = 128 // go to colored text
                txtpos++
            } else
                mask = 0

            while (txtpos < txt.length()) {
                c = txt.charAt(txtpos)
                // count word length
                run {
                    l = 0
                    while (l < con.linewidth && l < (txt.length() - txtpos)) {
                        if (txt.charAt(l + txtpos) <= ' ')
                            break
                        l++
                    }
                }

                // word wrap
                if (l != con.linewidth && (con.x + l > con.linewidth))
                    con.x = 0

                txtpos++

                if (cr != 0) {
                    con.current--
                    cr = 0
                }

                if (con.x == 0) {
                    Console.Linefeed()
                    // mark time for transparent overlay
                    if (con.current >= 0)
                        con.times[con.current % NUM_CON_TIMES] = cls.realtime
                }

                when (c) {
                    '\n' -> con.x = 0

                    '\r' -> {
                        con.x = 0
                        cr = 1
                    }

                    else // display character and advance
                    -> {
                        y = con.current % con.totallines
                        con.text[y * con.linewidth + con.x] = (c or mask or con.ormask) as Byte
                        con.x++
                        if (con.x >= con.linewidth)
                            con.x = 0
                    }
                }
            }
        }

        /*
     * ============== Con_CenteredPrint ==============
     */
        fun CenteredPrint(text: String) {
            var l = text.length()
            l = (con.linewidth - l) / 2
            if (l < 0)
                l = 0

            val sb = StringBuffer(1024)
            for (i in 0..l - 1)
                sb.append(' ')
            sb.append(text)
            sb.append('\n')

            sb.setLength(1024)

            Console.Print(sb.toString())
        }

        /*
     * ==============================================================================
     * 
     * DRAWING
     * 
     * ==============================================================================
     */

        /*
     * ================ Con_DrawInput
     * 
     * The input line scrolls horizontally if typing goes beyond the right edge
     * ================
     */
        fun DrawInput() {
            var i: Int
            val text: ByteArray

            if (cls.key_dest == key_menu)
                return
            if (cls.key_dest != key_console && cls.state == ca_active)
                return  // don't draw anything (always draw if not active)

            text = key_lines[edit_line]

            // add the cursor frame
            text[key_linepos] = (10 + ((cls.realtime shr 8) as Int and 1)).toByte()

            // fill out remainder with spaces
            run {
                i = key_linepos + 1
                while (i < con.linewidth) {
                    text[i] = ' '
                    i++
                }
            }

            // prestep if horizontally scrolling
            //if (key_linepos >= con.linewidth)
            //    start += 1 + key_linepos - con.linewidth;

            // draw it
            //		y = con.vislines-16;

            run {
                i = 0
                while (i < con.linewidth) {
                    re.DrawChar((i + 1) shl 3, con.vislines - 22, text[i])
                    i++
                }
            }

            // remove cursor
            key_lines[edit_line][key_linepos] = 0
        }

        /*
     * ================ Con_DrawNotify
     * 
     * Draws the last few lines of output transparently over the game top
     * ================
     */
        fun DrawNotify() {
            var x: Int
            var v: Int
            var text: Int
            var i: Int
            var time: Int
            var s: String
            val skip: Int

            v = 0
            run {
                i = con.current - NUM_CON_TIMES + 1
                while (i <= con.current) {
                    if (i < 0)
                        continue

                    time = con.times[i % NUM_CON_TIMES] as Int
                    if (time == 0)
                        continue

                    time = (cls.realtime - time) as Int
                    if (time > con_notifytime.value * 1000)
                        continue

                    text = (i % con.totallines) * con.linewidth

                    run {
                        x = 0
                        while (x < con.linewidth) {
                            re.DrawChar((x + 1) shl 3, v, con.text[text + x])
                            x++
                        }
                    }

                    v += 8
                    i++
                }
            }

            if (cls.key_dest == key_message) {
                if (chat_team) {
                    DrawString(8, v, "say_team:")
                    skip = 11
                } else {
                    DrawString(8, v, "say:")
                    skip = 5
                }

                s = chat_buffer
                if (chat_bufferlen > (viddef.width shr 3) - (skip + 1))
                    s = s.substring(chat_bufferlen - ((viddef.width shr 3) - (skip + 1)))

                run {
                    x = 0
                    while (x < s.length()) {
                        re.DrawChar((x + skip) shl 3, v, s.charAt(x))
                        x++
                    }
                }
                re.DrawChar((x + skip) shl 3, v, (10 + ((cls.realtime shr 8) and 1)) as Int)
                v += 8
            }

            if (v != 0) {
                SCR.AddDirtyPoint(0, 0)
                SCR.AddDirtyPoint(viddef.width - 1, v)
            }
        }

        /*
     * ================ Con_DrawConsole
     * 
     * Draws the console with the solid background ================
     */
        fun DrawConsole(frac: Float) {
            var i: Int
            var j: Int
            var x: Int
            var y: Int
            val n: Int
            var rows: Int
            var text: Int
            var row: Int
            var lines: Int
            val version: String

            lines = (viddef.height * frac) as Int
            if (lines <= 0)
                return

            if (lines > viddef.height)
                lines = viddef.height

            // draw the background
            re.DrawStretchPic(0, -viddef.height + lines, viddef.width, viddef.height, "conback")
            SCR.AddDirtyPoint(0, 0)
            SCR.AddDirtyPoint(viddef.width - 1, lines - 1)

            version = Com.sprintf("v%4.2f", Vargs(1).add(VERSION))
            run {
                x = 0
                while (x < 5) {
                    re.DrawChar(viddef.width - 44 + x * 8, lines - 12, 128 + version.charAt(x))
                    x++
                }
            }

            // draw the text
            con.vislines = lines

            rows = (lines - 22) shr 3 // rows of text to draw

            y = lines - 30

            // draw from the bottom up
            if (con.display != con.current) {
                // draw arrows to show the buffer is backscrolled
                run {
                    x = 0
                    while (x < con.linewidth) {
                        re.DrawChar((x + 1) shl 3, y, '^')
                        x += 4
                    }
                }

                y -= 8
                rows--
            }

            row = con.display
            run {
                i = 0
                while (i < rows) {
                    if (row < 0)
                        break
                    if (con.current - row >= con.totallines)
                        break // past scrollback wrap point

                    val first = (row % con.totallines) * con.linewidth

                    run {
                        x = 0
                        while (x < con.linewidth) {
                            re.DrawChar((x + 1) shl 3, y, con.text[x + first])
                            x++
                        }
                    }
                    i++
                    y -= 8
                    row--
                }
            }

            //ZOID
            // draw the download bar
            // figure out width
            if (cls.download != null) {
                if ((text = cls.downloadname.lastIndexOf('/')) != 0)
                    text++
                else
                    text = 0

                x = con.linewidth - ((con.linewidth * 7) / 40)
                y = x - (cls.downloadname.length() - text) - 8
                i = con.linewidth / 3
                val dlbar = StringBuffer(512)
                if (cls.downloadname.length() - text > i) {
                    y = x - i - 11
                    val end = text + i - 1
                    dlbar.append(cls.downloadname.substring(text, end))
                    dlbar.append("...")
                } else {
                    dlbar.append(cls.downloadname.substring(text))
                }
                dlbar.append(": ")
                dlbar.append(128.toChar())

                // where's the dot go?
                if (cls.downloadpercent == 0)
                    n = 0
                else
                    n = y * cls.downloadpercent / 100

                run {
                    j = 0
                    while (j < y) {
                        if (j == n)
                            dlbar.append(131.toChar())
                        else
                            dlbar.append(129.toChar())
                        j++
                    }
                }
                dlbar.append(130.toChar())
                dlbar.append(if ((cls.downloadpercent < 10)) " 0" else " ")
                dlbar.append(cls.downloadpercent).append('%')
                // draw it
                y = con.vislines - 12
                run {
                    i = 0
                    while (i < dlbar.length()) {
                        re.DrawChar((i + 1) shl 3, y, dlbar.charAt(i))
                        i++
                    }
                }
            }
            //ZOID

            // draw the input prompt, user text, and cursor if desired
            DrawInput()
        }
    }
}