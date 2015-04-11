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

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.util.Lib

/**
 * Cbuf
 */
public class Cbuf {
    companion object {

        private val line = ByteArray(1024)
        private val tmp = ByteArray(8192)

        /**

         */
        public fun Init() {
            SZ.Init(Globals.cmd_text, Globals.cmd_text_buf, Globals.cmd_text_buf.length)
        }

        public fun InsertText(text: String) {

            var templen = 0

            // copy off any commands still remaining in the exec buffer
            templen = Globals.cmd_text.cursize
            if (templen != 0) {
                System.arraycopy(Globals.cmd_text.data, 0, tmp, 0, templen)
                SZ.Clear(Globals.cmd_text)
            }

            // add the entire text of the file
            Cbuf.AddText(text)

            // add the copied off data
            if (templen != 0) {
                SZ.Write(Globals.cmd_text, tmp, templen)
            }
        }

        /**
         * @param clear
         */
        fun AddEarlyCommands(clear: Boolean) {

            run {
                var i = 0
                while (i < Com.Argc()) {
                    val s = Com.Argv(i)
                    if (!s.equals("+set"))
                        continue
                    Cbuf.AddText("set " + Com.Argv(i + 1) + " " + Com.Argv(i + 2) + "\n")
                    if (clear) {
                        Com.ClearArgv(i)
                        Com.ClearArgv(i + 1)
                        Com.ClearArgv(i + 2)
                    }
                    i += 2
                    i++
                }
            }
        }

        /**
         * @return
         */
        fun AddLateCommands(): Boolean {
            var i: Int
            var j: Int
            var ret = false

            // build the combined string to parse from
            var s = 0
            val argc = Com.Argc()
            run {
                i = 1
                while (i < argc) {
                    s += Com.Argv(i).length()
                    i++
                }
            }
            if (s == 0)
                return false

            var text: String? = ""
            run {
                i = 1
                while (i < argc) {
                    text += Com.Argv(i)
                    if (i != argc - 1)
                        text += " "
                    i++
                }
            }

            // pull out the commands
            var build: String? = ""
            run {
                i = 0
                while (i < text!!.length()) {
                    if (text!!.charAt(i) == '+') {
                        i++

                        run {
                            j = i
                            while (j < text!!.length() && (text!!.charAt(j) != '+') && (text!!.charAt(j) != '-')) {
                                j++
                            }
                        }

                        build += text!!.substring(i, j)
                        build += "\n"

                        i = j - 1
                    }
                    i++
                }
            }

            ret = (build!!.length() != 0)
            if (ret)
                Cbuf.AddText(build)

            text = null
            build = null

            return ret
        }

        /**
         * @param text
         */
        public fun AddText(text: String) {
            val l = text.length()

            if (Globals.cmd_text.cursize + l >= Globals.cmd_text.maxsize) {
                Com.Printf("Cbuf_AddText: overflow\n")
                return
            }
            SZ.Write(Globals.cmd_text, Lib.stringToBytes(text), l)
        }

        /**

         */
        public fun Execute() {

            var text: ByteArray? = null

            Globals.alias_count = 0 // don't allow infinite alias loops

            while (Globals.cmd_text.cursize != 0) {
                // find a \n or ; line break
                text = Globals.cmd_text.data

                var quotes = 0
                var i: Int

                run {
                    i = 0
                    while (i < Globals.cmd_text.cursize) {
                        if (text!![i] == '"')
                            quotes++
                        if (!(quotes % 2 != 0) && text!![i] == ';')
                            break // don't break if inside a quoted string
                        if (text!![i] == '\n')
                            break
                        i++
                    }
                }

                System.arraycopy(text, 0, line, 0, i)
                line[i] = 0

                // delete the text from the command buffer and move remaining
                // commands down
                // this is necessary because commands (exec, alias) can insert data
                // at the
                // beginning of the text buffer

                if (i == Globals.cmd_text.cursize)
                    Globals.cmd_text.cursize = 0
                else {
                    i++
                    Globals.cmd_text.cursize -= i
                    //byte[] tmp = new byte[Globals.cmd_text.cursize];

                    System.arraycopy(text, i, tmp, 0, Globals.cmd_text.cursize)
                    System.arraycopy(tmp, 0, text, 0, Globals.cmd_text.cursize)
                    text[Globals.cmd_text.cursize] = '\0'

                }

                // execute the command line
                val len = Lib.strlen(line)

                val cmd = String(line, 0, len)
                Cmd.ExecuteString(cmd)

                if (Globals.cmd_wait) {
                    // skip out while text still remains in buffer, leaving it
                    // for next frame
                    Globals.cmd_wait = false
                    break
                }
            }
        }

        public fun ExecuteText(exec_when: Int, text: String) {
            when (exec_when) {
                Defines.EXEC_NOW -> Cmd.ExecuteString(text)
                Defines.EXEC_INSERT -> Cbuf.InsertText(text)
                Defines.EXEC_APPEND -> Cbuf.AddText(text)
                else -> Com.Error(Defines.ERR_FATAL, "Cbuf_ExecuteText: bad exec_when")
            }
        }

        /*
     * ============ Cbuf_CopyToDefer ============
     */
        public fun CopyToDefer() {
            System.arraycopy(Globals.cmd_text_buf, 0, Globals.defer_text_buf, 0, Globals.cmd_text.cursize)
            Globals.defer_text_buf[Globals.cmd_text.cursize] = 0
            Globals.cmd_text.cursize = 0
        }

        /*
     * ============ Cbuf_InsertFromDefer ============
     */
        public fun InsertFromDefer() {
            InsertText(String(Globals.defer_text_buf).trim())
            Globals.defer_text_buf[0] = 0
        }
    }

}