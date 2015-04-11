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
import lwjake2.game.Info
import lwjake2.game.cvar_t
import lwjake2.util.Lib

import java.io.IOException
import java.io.RandomAccessFile
import java.util.Vector

/**
 * Cvar implements console variables. The original code is located in cvar.c
 */
public class Cvar : Globals() {
    companion object {

        /**
         * @param var_name
        * *
         * @param var_value
        * *
         * @param flags
        * *
         * @return
         */
        public fun Get(var_name: String, var_value: String?, flags: Int): cvar_t? {
            var `var`: cvar_t?

            if ((flags and (CVAR_USERINFO or CVAR_SERVERINFO)) != 0) {
                if (!InfoValidate(var_name)) {
                    Com.Printf("invalid info cvar name\n")
                    return null
                }
            }

            `var` = Cvar.FindVar(var_name)
            if (`var` != null) {
                `var`!!.flags = `var`!!.flags or flags
                return `var`
            }

            if (var_value == null)
                return null

            if ((flags and (CVAR_USERINFO or CVAR_SERVERINFO)) != 0) {
                if (!InfoValidate(var_value)) {
                    Com.Printf("invalid info cvar value\n")
                    return null
                }
            }
            `var` = cvar_t()
            `var`!!.name = String(var_name)
            `var`!!.string = String(var_value)
            `var`!!.modified = true
            // handles atof(var.string)
            try {
                `var`!!.value = Float.parseFloat(`var`!!.string)
            } catch (e: NumberFormatException) {
                `var`!!.value = 0.0.toFloat()
            }

            // link the variable in
            `var`!!.next = Globals.cvar_vars
            Globals.cvar_vars = `var`

            `var`!!.flags = flags

            return `var`
        }

        fun Init() {
            Cmd.AddCommand("set", Set_f)
            Cmd.AddCommand("cvarlist", List_f)
        }

        public fun VariableString(var_name: String): String {
            val `var`: cvar_t?
            `var` = FindVar(var_name)
            return if ((`var` == null)) "" else `var`!!.string
        }

        fun FindVar(var_name: String): cvar_t? {
            var `var`: cvar_t?

            run {
                `var` = Globals.cvar_vars
                while (`var` != null) {
                    if (var_name.equals(`var`!!.name))
                        return `var`
                    `var` = `var`!!.next
                }
            }

            return null
        }

        /**
         * Creates a variable if not found and sets their value, the parsed float value and their flags.
         */
        public fun FullSet(var_name: String, value: String, flags: Int): cvar_t {
            val `var`: cvar_t?

            `var` = Cvar.FindVar(var_name)
            if (null == `var`) {
                // create it
                return Cvar.Get(var_name, value, flags)
            }

            `var`!!.modified = true

            if ((`var`!!.flags and CVAR_USERINFO) != 0)
                Globals.userinfo_modified = true // transmit at next oportunity

            `var`!!.string = value
            try {
                `var`!!.value = Float.parseFloat(`var`!!.string)
            } catch (e: Exception) {
                `var`!!.value = 0.0.toFloat()
            }


            `var`!!.flags = flags

            return `var`
        }

        /**
         * Sets the value of the variable without forcing.
         */
        public fun Set(var_name: String, value: String): cvar_t {
            return Set2(var_name, value, false)
        }

        /**
         * Sets the value of the variable with forcing.
         */
        public fun ForceSet(var_name: String, value: String): cvar_t {
            return Cvar.Set2(var_name, value, true)
        }

        /**
         * Gereric set function, sets the value of the variable, with forcing its even possible to
         * override the variables write protection.
         */
        fun Set2(var_name: String, value: String, force: Boolean): cvar_t {

            val `var` = Cvar.FindVar(var_name)
            if (`var` == null) {
                // create it
                return Cvar.Get(var_name, value, 0)
            }

            if ((`var`!!.flags and (CVAR_USERINFO or CVAR_SERVERINFO)) != 0) {
                if (!InfoValidate(value)) {
                    Com.Printf("invalid info cvar value\n")
                    return `var`
                }
            }

            if (!force) {
                if ((`var`!!.flags and CVAR_NOSET) != 0) {
                    Com.Printf(var_name + " is write protected.\n")
                    return `var`
                }

                if ((`var`!!.flags and CVAR_LATCH) != 0) {
                    if (`var`!!.latched_string != null) {
                        if (value.equals(`var`!!.latched_string))
                            return `var`
                        `var`!!.latched_string = null
                    } else {
                        if (value.equals(`var`!!.string))
                            return `var`
                    }

                    if (Globals.server_state != 0) {
                        Com.Printf(var_name + " will be changed for next game.\n")
                        `var`!!.latched_string = value
                    } else {
                        `var`!!.string = value
                        try {
                            `var`!!.value = Float.parseFloat(`var`!!.string)
                        } catch (e: Exception) {
                            `var`!!.value = 0.0.toFloat()
                        }

                        if (`var`!!.name.equals("game")) {
                            FS.SetGamedir(`var`!!.string)
                            FS.ExecAutoexec()
                        }
                    }
                    return `var`
                }
            } else {
                if (`var`!!.latched_string != null) {
                    `var`!!.latched_string = null
                }
            }

            if (value.equals(`var`!!.string))
                return `var` // not changed

            `var`!!.modified = true

            if ((`var`!!.flags and CVAR_USERINFO) != 0)
                Globals.userinfo_modified = true // transmit at next oportunity

            `var`!!.string = value
            try {
                `var`!!.value = Float.parseFloat(`var`!!.string)
            } catch (e: Exception) {
                `var`!!.value = 0.0.toFloat()
            }


            return `var`
        }

        /**
         * Set command, sets variables.
         */

        var Set_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                val c: Int
                val flags: Int

                c = Cmd.Argc()
                if (c != 3 && c != 4) {
                    Com.Printf("usage: set <variable> <value> [u / s]\n")
                    return
                }

                if (c == 4) {
                    if (Cmd.Argv(3).equals("u"))
                        flags = CVAR_USERINFO
                    else if (Cmd.Argv(3).equals("s"))
                        flags = CVAR_SERVERINFO
                    else {
                        Com.Printf("flags can only be 'u' or 's'\n")
                        return
                    }
                    Cvar.FullSet(Cmd.Argv(1), Cmd.Argv(2), flags)
                } else
                    Cvar.Set(Cmd.Argv(1), Cmd.Argv(2))

            }

        }

        /**
         * List command, lists all available commands.
         */
        var List_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                var `var`: cvar_t?
                var i: Int

                i = 0
                run {
                    `var` = Globals.cvar_vars
                    while (`var` != null) {
                        if ((`var`!!.flags and CVAR_ARCHIVE) != 0)
                            Com.Printf("*")
                        else
                            Com.Printf(" ")
                        if ((`var`!!.flags and CVAR_USERINFO) != 0)
                            Com.Printf("U")
                        else
                            Com.Printf(" ")
                        if ((`var`!!.flags and CVAR_SERVERINFO) != 0)
                            Com.Printf("S")
                        else
                            Com.Printf(" ")
                        if ((`var`!!.flags and CVAR_NOSET) != 0)
                            Com.Printf("-")
                        else if ((`var`!!.flags and CVAR_LATCH) != 0)
                            Com.Printf("L")
                        else
                            Com.Printf(" ")
                        Com.Printf(" " + `var`!!.name + " \"" + `var`!!.string + "\"\n")
                        `var` = `var`!!.next
                        i++
                    }
                }
                Com.Printf(i + " cvars\n")
            }
        }


        /**
         * Sets a float value of a variable.

         * The overloading is very important, there was a problem with
         * networt "rate" string --> 10000 became "10000.0" and that wasn't right.
         */
        public fun SetValue(var_name: String, value: Int) {
            Cvar.Set(var_name, "" + value)
        }

        public fun SetValue(var_name: String, value: Float) {
            if (value == value.toInt()) {
                Cvar.Set(var_name, "" + value.toInt())
            } else {
                Cvar.Set(var_name, "" + value)
            }
        }

        /**
         * Returns the float value of a variable.
         */
        public fun VariableValue(var_name: String): Float {
            val `var` = Cvar.FindVar(var_name)
            if (`var` == null)
                return 0
            var `val` = 0.0.toFloat()
            try {
                `val` = Float.parseFloat(`var`!!.string)
            } catch (e: Exception) {
            }

            return `val`
        }

        /**
         * Handles variable inspection and changing from the console.
         */
        public fun Command(): Boolean {
            val v: cvar_t?

            // check variables
            v = Cvar.FindVar(Cmd.Argv(0))
            if (v == null)
                return false

            // perform a variable print or set
            if (Cmd.Argc() == 1) {
                Com.Printf("\"" + v!!.name + "\" is \"" + v!!.string + "\"\n")
                return true
            }

            Cvar.Set(v!!.name, Cmd.Argv(1))
            return true
        }

        public fun BitInfo(bit: Int): String {
            var info: String
            var `var`: cvar_t?

            info = ""

            run {
                `var` = Globals.cvar_vars
                while (`var` != null) {
                    if ((`var`!!.flags and bit) != 0)
                        info = Info.Info_SetValueForKey(info, `var`!!.name, `var`!!.string)
                    `var` = `var`!!.next
                }
            }
            return info
        }

        /**
         * Returns an info string containing all the CVAR_SERVERINFO cvars.
         */
        public fun Serverinfo(): String {
            return BitInfo(Defines.CVAR_SERVERINFO)
        }


        /**
         * Any variables with latched values will be updated.
         */
        public fun GetLatchedVars() {
            var `var`: cvar_t?

            run {
                `var` = Globals.cvar_vars
                while (`var` != null) {
                    if (`var`!!.latched_string == null || `var`!!.latched_string.length() == 0)
                        continue
                    `var`!!.string = `var`!!.latched_string
                    `var`!!.latched_string = null
                    try {
                        `var`!!.value = Float.parseFloat(`var`!!.string)
                    } catch (e: NumberFormatException) {
                        `var`!!.value = 0.0.toFloat()
                    }

                    if (`var`!!.name.equals("game")) {
                        FS.SetGamedir(`var`!!.string)
                        FS.ExecAutoexec()
                    }
                    `var` = `var`!!.next
                }
            }
        }

        /**
         * Returns an info string containing all the CVAR_USERINFO cvars.
         */
        public fun Userinfo(): String {
            return BitInfo(CVAR_USERINFO)
        }

        /**
         * Appends lines containing \"set vaqriable value\" for all variables
         * with the archive flag set true.
         */

        public fun WriteVariables(path: String) {
            var `var`: cvar_t?
            val f: RandomAccessFile?
            var buffer: String

            f = Lib.fopen(path, "rw")
            if (f == null)
                return

            try {
                f!!.seek(f!!.length())
            } catch (e1: IOException) {
                Lib.fclose(f)
                return
            }

            run {
                `var` = cvar_vars
                while (`var` != null) {
                    if ((`var`!!.flags and CVAR_ARCHIVE) != 0) {
                        buffer = "set " + `var`!!.name + " \"" + `var`!!.string + "\"\n"
                        try {
                            f!!.writeBytes(buffer)
                        } catch (e: IOException) {
                        }

                    }
                    `var` = `var`!!.next
                }
            }
            Lib.fclose(f)
        }

        /**
         * Variable typing auto completition.
         */
        public fun CompleteVariable(partial: String): Vector<String> {

            val vars = Vector<String>()

            // check match
            run {
                var cvar = Globals.cvar_vars
                while (cvar != null) {
                    if (cvar!!.name.startsWith(partial))
                        vars.add(cvar!!.name)
                    cvar = cvar!!.next
                }
            }

            return vars
        }

        /**
         * Some characters are invalid for info strings.
         */
        fun InfoValidate(s: String): Boolean {
            if (s.indexOf("\\") != -1)
                return false
            if (s.indexOf("\"") != -1)
                return false
            if (s.indexOf(";") != -1)
                return false
            return true
        }
    }
}