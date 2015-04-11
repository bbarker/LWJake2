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
import lwjake2.qcommon.xcommand_t
import lwjake2.util.Lib

import java.io.IOException
import java.io.RandomAccessFile
import java.util.Vector

/**
 * Key
 */
open public class Key : Globals() {
    companion object {
        //
        // these are the key numbers that should be passed to Key_Event
        //
        public val K_TAB: Int = 9
        public val K_ENTER: Int = 13
        public val K_ESCAPE: Int = 27
        public val K_SPACE: Int = 32

        // normal keys should be passed as lowercased ascii

        public val K_BACKSPACE: Int = 127
        public val K_UPARROW: Int = 128
        public val K_DOWNARROW: Int = 129
        public val K_LEFTARROW: Int = 130
        public val K_RIGHTARROW: Int = 131

        public val K_ALT: Int = 132
        public val K_CTRL: Int = 133
        public val K_SHIFT: Int = 134
        public val K_F1: Int = 135
        public val K_F2: Int = 136
        public val K_F3: Int = 137
        public val K_F4: Int = 138
        public val K_F5: Int = 139
        public val K_F6: Int = 140
        public val K_F7: Int = 141
        public val K_F8: Int = 142
        public val K_F9: Int = 143
        public val K_F10: Int = 144
        public val K_F11: Int = 145
        public val K_F12: Int = 146
        public val K_INS: Int = 147
        public val K_DEL: Int = 148
        public val K_PGDN: Int = 149
        public val K_PGUP: Int = 150
        public val K_HOME: Int = 151
        public val K_END: Int = 152

        public val K_KP_HOME: Int = 160
        public val K_KP_UPARROW: Int = 161
        public val K_KP_PGUP: Int = 162
        public val K_KP_LEFTARROW: Int = 163
        public val K_KP_5: Int = 164
        public val K_KP_RIGHTARROW: Int = 165
        public val K_KP_END: Int = 166
        public val K_KP_DOWNARROW: Int = 167
        public val K_KP_PGDN: Int = 168
        public val K_KP_ENTER: Int = 169
        public val K_KP_INS: Int = 170
        public val K_KP_DEL: Int = 171
        public val K_KP_SLASH: Int = 172
        public val K_KP_MINUS: Int = 173
        public val K_KP_PLUS: Int = 174

        public val K_PAUSE: Int = 255

        //
        // mouse buttons generate virtual keys
        //
        public val K_MOUSE1: Int = 200
        public val K_MOUSE2: Int = 201
        public val K_MOUSE3: Int = 202

        //
        // joystick buttons
        //
        public val K_JOY1: Int = 203
        public val K_JOY2: Int = 204
        public val K_JOY3: Int = 205
        public val K_JOY4: Int = 206

        public val K_MWHEELDOWN: Int = 239
        public val K_MWHEELUP: Int = 240

        var anykeydown = 0
        var key_waiting: Int = 0
        var history_line = 0
        var shift_down = false
        var key_repeats = IntArray(256)
        //static int[] keyshift = new int[256];
        var menubound = BooleanArray(256)
        var consolekeys = BooleanArray(256)

        var keynames = arrayOfNulls<String>(256)

        init {
            keynames[K_TAB] = "TAB"
            keynames[K_ENTER] = "ENTER"
            keynames[K_ESCAPE] = "ESCAPE"
            keynames[K_SPACE] = "SPACE"
            keynames[K_BACKSPACE] = "BACKSPACE"
            keynames[K_UPARROW] = "UPARROW"
            keynames[K_DOWNARROW] = "DOWNARROW"
            keynames[K_LEFTARROW] = "LEFTARROW"
            keynames[K_RIGHTARROW] = "RIGHTARROW"
            keynames[K_ALT] = "ALT"
            keynames[K_CTRL] = "CTRL"
            keynames[K_SHIFT] = "SHIFT"

            keynames[K_F1] = "F1"
            keynames[K_F2] = "F2"
            keynames[K_F3] = "F3"
            keynames[K_F4] = "F4"
            keynames[K_F5] = "F5"
            keynames[K_F6] = "F6"
            keynames[K_F7] = "F7"
            keynames[K_F8] = "F8"
            keynames[K_F9] = "F9"
            keynames[K_F10] = "F10"
            keynames[K_F11] = "F11"
            keynames[K_F12] = "F12"

            keynames[K_INS] = "INS"
            keynames[K_DEL] = "DEL"
            keynames[K_PGDN] = "PGDN"
            keynames[K_PGUP] = "PGUP"
            keynames[K_HOME] = "HOME"
            keynames[K_END] = "END"

            keynames[K_MOUSE1] = "MOUSE1"
            keynames[K_MOUSE2] = "MOUSE2"
            keynames[K_MOUSE3] = "MOUSE3"

            //	00092         {"JOY1", K_JOY1},
            //	00093         {"JOY2", K_JOY2},
            //	00094         {"JOY3", K_JOY3},
            //	00095         {"JOY4", K_JOY4},

            keynames[K_KP_HOME] = "KP_HOME"
            keynames[K_KP_UPARROW] = "KP_UPARROW"
            keynames[K_KP_PGUP] = "KP_PGUP"
            keynames[K_KP_LEFTARROW] = "KP_LEFTARROW"
            keynames[K_KP_5] = "KP_5"
            keynames[K_KP_RIGHTARROW] = "KP_RIGHTARROW"
            keynames[K_KP_END] = "KP_END"
            keynames[K_KP_DOWNARROW] = "KP_DOWNARROW"
            keynames[K_KP_PGDN] = "KP_PGDN"
            keynames[K_KP_ENTER] = "KP_ENTER"
            keynames[K_KP_INS] = "KP_INS"
            keynames[K_KP_DEL] = "KP_DEL"
            keynames[K_KP_SLASH] = "KP_SLASH"

            keynames[K_KP_PLUS] = "KP_PLUS"
            keynames[K_KP_MINUS] = "KP_MINUS"

            keynames[K_MWHEELUP] = "MWHEELUP"
            keynames[K_MWHEELDOWN] = "MWHEELDOWN"

            keynames[K_PAUSE] = "PAUSE"
            keynames[';'] = "SEMICOLON" // because a raw semicolon seperates commands

            keynames[0] = "NULL"
        }

        /**

         */
        public fun Init() {
            for (i in 0..32 - 1) {
                Globals.key_lines[i][0] = ']'
                Globals.key_lines[i][1] = 0
            }
            Globals.key_linepos = 1

            //
            // init ascii characters in console mode
            //
            for (i in 32..128 - 1)
                consolekeys[i] = true
            consolekeys[K_ENTER] = true
            consolekeys[K_KP_ENTER] = true
            consolekeys[K_TAB] = true
            consolekeys[K_LEFTARROW] = true
            consolekeys[K_KP_LEFTARROW] = true
            consolekeys[K_RIGHTARROW] = true
            consolekeys[K_KP_RIGHTARROW] = true
            consolekeys[K_UPARROW] = true
            consolekeys[K_KP_UPARROW] = true
            consolekeys[K_DOWNARROW] = true
            consolekeys[K_KP_DOWNARROW] = true
            consolekeys[K_BACKSPACE] = true
            consolekeys[K_HOME] = true
            consolekeys[K_KP_HOME] = true
            consolekeys[K_END] = true
            consolekeys[K_KP_END] = true
            consolekeys[K_PGUP] = true
            consolekeys[K_KP_PGUP] = true
            consolekeys[K_PGDN] = true
            consolekeys[K_KP_PGDN] = true
            consolekeys[K_SHIFT] = true
            consolekeys[K_INS] = true
            consolekeys[K_KP_INS] = true
            consolekeys[K_KP_DEL] = true
            consolekeys[K_KP_SLASH] = true
            consolekeys[K_KP_PLUS] = true
            consolekeys[K_KP_MINUS] = true
            consolekeys[K_KP_5] = true

            consolekeys['`'] = false
            consolekeys['~'] = false

            //		for (int i = 0; i < 256; i++)
            //			keyshift[i] = i;
            //		for (int i = 'a'; i <= 'z'; i++)
            //			keyshift[i] = i - 'a' + 'A';
            //		keyshift['1'] = '!';
            //		keyshift['2'] = '@';
            //		keyshift['3'] = '#';
            //		keyshift['4'] = '$';
            //		keyshift['5'] = '%';
            //		keyshift['6'] = '^';
            //		keyshift['7'] = '&';
            //		keyshift['8'] = '*';
            //		keyshift['9'] = '(';
            //		keyshift['0'] = ')';
            //		keyshift['-'] = '_';
            //		keyshift['='] = '+';
            //		keyshift[','] = '<';
            //		keyshift['.'] = '>';
            //		keyshift['/'] = '?';
            //		keyshift[';'] = ':';
            //		keyshift['\''] = '"';
            //		keyshift['['] = '{';
            //		keyshift[']'] = '}';
            //		keyshift['`'] = '~';
            //		keyshift['\\'] = '|';

            menubound[K_ESCAPE] = true
            for (i in 0..12 - 1)
                menubound[K_F1 + i] = true

            //
            // register our functions
            //
            Cmd.AddCommand("bind", Key.Bind_f)
            Cmd.AddCommand("unbind", Key.Unbind_f)
            Cmd.AddCommand("unbindall", Key.Unbindall_f)
            Cmd.AddCommand("bindlist", Key.Bindlist_f)
        }

        public fun ClearTyping() {
            Globals.key_lines[Globals.edit_line][1] = 0 // clear any typing
            Globals.key_linepos = 1
        }

        /**
         * Called by the system between frames for both key up and key down events.
         */
        public fun Event(key: Int, down: Boolean, time: Int) {
            var key = key
            val kb: String?
            val cmd: String

            // hack for modal presses
            if (key_waiting == -1) {
                if (down)
                    key_waiting = key
                return
            }

            // update auto-repeat status
            if (down) {
                key_repeats[key]++
                if (key_repeats[key] > 1 && Globals.cls.key_dest == Defines.key_game && !(Globals.cls.state == Defines.ca_disconnected))
                    return  // ignore most autorepeats

                if (key >= 200 && Globals.keybindings[key] == null)
                    Com.Printf(Key.KeynumToString(key) + " is unbound, hit F4 to set.\n")
            } else {
                key_repeats[key] = 0
            }

            if (key == K_SHIFT)
                shift_down = down

            // console key is hardcoded, so the user can never unbind it
            if (key == '`' || key == '~') {
                if (!down)
                    return

                Console.ToggleConsole_f.execute()
                return
            }

            // any key during the attract mode will bring up the menu
            if (Globals.cl.attractloop && Globals.cls.key_dest != Defines.key_menu && !(key >= K_F1 && key <= K_F12))
                key = K_ESCAPE

            // menu key is hardcoded, so the user can never unbind it
            if (key == K_ESCAPE) {
                if (!down)
                    return

                if (Globals.cl.frame.playerstate.stats[Defines.STAT_LAYOUTS] != 0 && Globals.cls.key_dest == Defines.key_game) {
                    // put away help computer / inventory
                    Cbuf.AddText("cmd putaway\n")
                    return
                }
                when (Globals.cls.key_dest) {
                    Defines.key_message -> Key.Message(key)
                    Defines.key_menu -> Menu.Keydown(key)
                    Defines.key_game, Defines.key_console -> Menu.Menu_Main_f()
                    else -> Com.Error(Defines.ERR_FATAL, "Bad cls.key_dest")
                }
                return
            }

            // track if any key is down for BUTTON_ANY
            Globals.keydown[key] = down
            if (down) {
                if (key_repeats[key] == 1)
                    Key.anykeydown++
            } else {
                Key.anykeydown--
                if (Key.anykeydown < 0)
                    Key.anykeydown = 0
            }

            //
            // key up events only generate commands if the game key binding is
            // a button command (leading + sign).  These will occur even in console mode,
            // to keep the character from continuing an action started before a console
            // switch.  Button commands include the kenum as a parameter, so multiple
            // downs can be matched with ups
            //
            if (!down) {
                kb = Globals.keybindings[key]
                if (kb != null && kb.length() > 0 && kb.charAt(0) == '+') {
                    cmd = "-" + kb.substring(1) + " " + key + " " + time + "\n"
                    Cbuf.AddText(cmd)
                }
                //			if (keyshift[key] != key) {
                //				kb = Globals.keybindings[keyshift[key]];
                //				if (kb != null && kb.length()>0 && kb.charAt(0) == '+') {
                //					cmd = "-" + kb.substring(1) + " " + key + " " + time + "\n";
                //					Cbuf.AddText(cmd);
                //				}
                //			}
                return
            }

            //
            // if not a consolekey, send to the interpreter no matter what mode is
            //
            if ((Globals.cls.key_dest == Defines.key_menu && menubound[key]) || (Globals.cls.key_dest == Defines.key_console && !consolekeys[key]) || (Globals.cls.key_dest == Defines.key_game && (Globals.cls.state == Defines.ca_active || !consolekeys[key]))) {
                kb = Globals.keybindings[key]
                if (kb != null) {
                    if (kb.length() > 0 && kb.charAt(0) == '+') {
                        // button commands add keynum and time as a parm
                        cmd = kb + " " + key + " " + time + "\n"
                        Cbuf.AddText(cmd)
                    } else {
                        Cbuf.AddText(kb + "\n")
                    }
                }
                return
            }

            if (!down)
                return  // other systems only care about key down events

            //		if (shift_down)
            //			key = keyshift[key];

            when (Globals.cls.key_dest) {
                Defines.key_message -> Key.Message(key)
                Defines.key_menu -> Menu.Keydown(key)

                Defines.key_game, Defines.key_console -> Key.Console(key)
                else -> Com.Error(Defines.ERR_FATAL, "Bad cls.key_dest")
            }
        }

        /**
         * Returns a string (either a single ascii char, or a K_* name) for the
         * given keynum.
         */
        public fun KeynumToString(keynum: Int): String {
            if (keynum < 0 || keynum > 255)
                return "<KEY NOT FOUND>"
            if (keynum > 32 && keynum < 127)
                return Character.toString(keynum.toChar())

            if (keynames[keynum] != null)
                return keynames[keynum]

            return "<UNKNOWN KEYNUM>"
        }

        /**
         * Returns a key number to be used to index keybindings[] by looking at
         * the given string. Single ascii characters return themselves, while
         * the K_* names are matched up.
         */
        fun StringToKeynum(str: String?): Int {

            if (str == null)
                return -1

            if (str.length() == 1)
                return str.charAt(0)

            for (i in keynames.indices) {
                if (str.equalsIgnoreCase(keynames[i]))
                    return i
            }

            return -1
        }

        public fun Message(key: Int) {

            if (key == K_ENTER || key == K_KP_ENTER) {
                if (Globals.chat_team)
                    Cbuf.AddText("say_team \"")
                else
                    Cbuf.AddText("say \"")

                Cbuf.AddText(Globals.chat_buffer)
                Cbuf.AddText("\"\n")

                Globals.cls.key_dest = Defines.key_game
                Globals.chat_buffer = ""
                return
            }

            if (key == K_ESCAPE) {
                Globals.cls.key_dest = Defines.key_game
                Globals.chat_buffer = ""
                return
            }

            if (key < 32 || key > 127)
                return  // non printable

            if (key == K_BACKSPACE) {
                if (Globals.chat_buffer.length() > 2) {
                    Globals.chat_buffer = Globals.chat_buffer.substring(0, Globals.chat_buffer.length() - 2)
                } else
                    Globals.chat_buffer = ""
                return
            }

            if (Globals.chat_buffer.length() > Defines.MAXCMDLINE)
                return  // all full

            Globals.chat_buffer += key.toChar()
        }

        /**
         * Interactive line editing and console scrollback.
         */
        public fun Console(key: Int) {
            var key = key

            when (key) {
                K_KP_SLASH -> key = '/'
                K_KP_MINUS -> key = '-'
                K_KP_PLUS -> key = '+'
                K_KP_HOME -> key = '7'
                K_KP_UPARROW -> key = '8'
                K_KP_PGUP -> key = '9'
                K_KP_LEFTARROW -> key = '4'
                K_KP_5 -> key = '5'
                K_KP_RIGHTARROW -> key = '6'
                K_KP_END -> key = '1'
                K_KP_DOWNARROW -> key = '2'
                K_KP_PGDN -> key = '3'
                K_KP_INS -> key = '0'
                K_KP_DEL -> key = '.'
            }

            if (key == 'l') {
                if (Globals.keydown[K_CTRL]) {
                    Cbuf.AddText("clear\n")
                    return
                }
            }

            if (key == K_ENTER || key == K_KP_ENTER) {
                // backslash text are commands, else chat
                if (Globals.key_lines[Globals.edit_line][1] == '\\' || Globals.key_lines[Globals.edit_line][1] == '/')
                    Cbuf.AddText(String(Globals.key_lines[Globals.edit_line], 2, Lib.strlen(Globals.key_lines[Globals.edit_line]) - 2))
                else
                    Cbuf.AddText(String(Globals.key_lines[Globals.edit_line], 1, Lib.strlen(Globals.key_lines[Globals.edit_line]) - 1))


                Cbuf.AddText("\n")

                Com.Printf(String(Globals.key_lines[Globals.edit_line], 0, Lib.strlen(Globals.key_lines[Globals.edit_line])) + "\n")
                Globals.edit_line = (Globals.edit_line + 1) and 31
                history_line = Globals.edit_line

                Globals.key_lines[Globals.edit_line][0] = ']'
                Globals.key_linepos = 1
                if (Globals.cls.state == Defines.ca_disconnected)
                    SCR.UpdateScreen() // force an update, because the command may take some time
                return
            }

            if (key == K_TAB) {
                // command completion
                CompleteCommand()
                return
            }

            if ((key == K_BACKSPACE) || (key == K_LEFTARROW) || (key == K_KP_LEFTARROW) || ((key == 'h') && (Globals.keydown[K_CTRL]))) {
                if (Globals.key_linepos > 1)
                    Globals.key_linepos--
                return
            }

            if ((key == K_UPARROW) || (key == K_KP_UPARROW) || ((key == 'p') && Globals.keydown[K_CTRL])) {
                do {
                    history_line = (history_line - 1) and 31
                } while (history_line != Globals.edit_line && Globals.key_lines[history_line][1] == 0)
                if (history_line == Globals.edit_line)
                    history_line = (Globals.edit_line + 1) and 31
                //Lib.strcpy(Globals.key_lines[Globals.edit_line], Globals.key_lines[history_line]);
                System.arraycopy(Globals.key_lines[history_line], 0, Globals.key_lines[Globals.edit_line], 0, Globals.key_lines[Globals.edit_line].length)
                Globals.key_linepos = Lib.strlen(Globals.key_lines[Globals.edit_line])
                return
            }

            if ((key == K_DOWNARROW) || (key == K_KP_DOWNARROW) || ((key == 'n') && Globals.keydown[K_CTRL])) {
                if (history_line == Globals.edit_line)
                    return
                do {
                    history_line = (history_line + 1) and 31
                } while (history_line != Globals.edit_line && Globals.key_lines[history_line][1] == 0)
                if (history_line == Globals.edit_line) {
                    Globals.key_lines[Globals.edit_line][0] = ']'
                    Globals.key_linepos = 1
                } else {
                    //Lib.strcpy(Globals.key_lines[Globals.edit_line], Globals.key_lines[history_line]);
                    System.arraycopy(Globals.key_lines[history_line], 0, Globals.key_lines[Globals.edit_line], 0, Globals.key_lines[Globals.edit_line].length)
                    Globals.key_linepos = Lib.strlen(Globals.key_lines[Globals.edit_line])
                }
                return
            }

            if (key == K_PGUP || key == K_KP_PGUP) {
                Globals.con.display -= 2
                return
            }

            if (key == K_PGDN || key == K_KP_PGDN) {
                Globals.con.display += 2
                if (Globals.con.display > Globals.con.current)
                    Globals.con.display = Globals.con.current
                return
            }

            if (key == K_HOME || key == K_KP_HOME) {
                Globals.con.display = Globals.con.current - Globals.con.totallines + 10
                return
            }

            if (key == K_END || key == K_KP_END) {
                Globals.con.display = Globals.con.current
                return
            }

            if (key < 32 || key > 127)
                return  // non printable

            if (Globals.key_linepos < Defines.MAXCMDLINE - 1) {
                Globals.key_lines[Globals.edit_line][Globals.key_linepos] = key.toByte()
                Globals.key_linepos++
                Globals.key_lines[Globals.edit_line][Globals.key_linepos] = 0
            }

        }

        private fun printCompletions(type: String, compl: Vector<String>) {
            Com.Printf(type)
            for (i in compl.indices) {
                Com.Printf(compl.get(i) as String + " ")
            }
            Com.Printf("\n")
        }

        fun CompleteCommand() {

            var start = 1
            if (key_lines[edit_line][start] == '\\' || key_lines[edit_line][start] == '/')
                start++

            var end = start
            while (key_lines[edit_line][end] != 0) end++

            var s = String(key_lines[edit_line], start, end - start)

            val cmds = Cmd.CompleteCommand(s)
            val vars = Cvar.CompleteVariable(s)

            val c = cmds.size()
            val v = vars.size()

            if ((c + v) > 1) {
                if (c > 0) printCompletions("\nCommands:\n", cmds)
                if (v > 0) printCompletions("\nVariables:\n", vars)
                return
            } else if (c == 1) {
                s = cmds.get(0) as String
            } else if (v == 1) {
                s = vars.get(0) as String
            } else
                return

            key_lines[edit_line][1] = '/'
            val bytes = Lib.stringToBytes(s)
            System.arraycopy(bytes, 0, key_lines[edit_line], 2, bytes.size())
            key_linepos = bytes.size() + 2
            key_lines[edit_line][key_linepos++] = ' '
            key_lines[edit_line][key_linepos] = 0

            return
        }

        public var Bind_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Key_Bind_f()
            }
        }

        fun Key_Bind_f() {
            val c = Cmd.Argc()

            if (c < 2) {
                Com.Printf("bind <key> [command] : attach a command to a key\n")
                return
            }
            val b = StringToKeynum(Cmd.Argv(1))
            if (b == -1) {
                Com.Printf("\"" + Cmd.Argv(1) + "\" isn't a valid key\n")
                return
            }

            if (c == 2) {
                if (Globals.keybindings[b] != null)
                    Com.Printf("\"" + Cmd.Argv(1) + "\" = \"" + Globals.keybindings[b] + "\"\n")
                else
                    Com.Printf("\"" + Cmd.Argv(1) + "\" is not bound\n")
                return
            }

            // copy the rest of the command line
            var cmd = "" // start out with a null string
            for (i in 2..c - 1) {
                cmd += Cmd.Argv(i)
                if (i != (c - 1))
                    cmd += " "
            }

            SetBinding(b, cmd)
        }

        fun SetBinding(keynum: Int, binding: String?) {
            if (keynum == -1)
                return

            // free old bindings
            Globals.keybindings[keynum] = null

            Globals.keybindings[keynum] = binding
        }

        var Unbind_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Key_Unbind_f()
            }
        }

        fun Key_Unbind_f() {

            if (Cmd.Argc() != 2) {
                Com.Printf("unbind <key> : remove commands from a key\n")
                return
            }

            val b = Key.StringToKeynum(Cmd.Argv(1))
            if (b == -1) {
                Com.Printf("\"" + Cmd.Argv(1) + "\" isn't a valid key\n")
                return
            }

            Key.SetBinding(b, null)
        }

        var Unbindall_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Key_Unbindall_f()
            }
        }

        fun Key_Unbindall_f() {
            for (i in 0..256 - 1)
                Key.SetBinding(i, null)
        }

        var Bindlist_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Key_Bindlist_f()
            }
        }

        fun Key_Bindlist_f() {
            for (i in 0..256 - 1)
                if (Globals.keybindings[i] != null && Globals.keybindings[i].length() != 0)
                    Com.Printf(Key.KeynumToString(i) + " \"" + Globals.keybindings[i] + "\"\n")
        }

        fun ClearStates() {
            var i: Int

            Key.anykeydown = 0

            run {
                i = 0
                while (i < 256) {
                    if (keydown[i] || key_repeats[i] != 0)
                        Event(i, false, 0)
                    keydown[i] = false
                    key_repeats[i] = 0
                    i++
                }
            }
        }

        public fun WriteBindings(f: RandomAccessFile) {
            for (i in 0..256 - 1)
                if (keybindings[i] != null && keybindings[i].length() > 0)
                    try {
                        f.writeBytes("bind " + KeynumToString(i) + " \"" + keybindings[i] + "\"\n")
                    } catch (e: IOException) {
                    }

        }
    }

}
