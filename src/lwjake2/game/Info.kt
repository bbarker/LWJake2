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

import java.util.StringTokenizer

public class Info {
    companion object {

        /**
         * Returns a value for a key from an info string.
         */
        public fun Info_ValueForKey(s: String, key: String): String {

            val tk = StringTokenizer(s, "\\")

            while (tk.hasMoreTokens()) {
                val key1 = tk.nextToken()

                if (!tk.hasMoreTokens()) {
                    Com.Printf("MISSING VALUE\n")
                    return s
                }
                val value1 = tk.nextToken()

                if (key.equals(key1))
                    return value1
            }

            return ""
        }

        /**
         * Sets a value for a key in the user info string.
         */
        public fun Info_SetValueForKey(s: String, key: String, value: String?): String {

            if (value == null || value.length() == 0)
                return s

            if (key.indexOf('\\') != -1 || value.indexOf('\\') != -1) {
                Com.Printf("Can't use keys or values with a \\\n")
                return s
            }

            if (key.indexOf(';') != -1) {
                Com.Printf("Can't use keys or values with a semicolon\n")
                return s
            }

            if (key.indexOf('"') != -1 || value.indexOf('"') != -1) {
                Com.Printf("Can't use keys or values with a \"\n")
                return s
            }

            if (key.length() > Defines.MAX_INFO_KEY - 1 || value.length() > Defines.MAX_INFO_KEY - 1) {
                Com.Printf("Keys and values must be < 64 characters.\n")
                return s
            }

            val sb = StringBuffer(Info_RemoveKey(s, key))

            if (sb.length() + 2 + key.length() + value.length() > Defines.MAX_INFO_STRING) {

                Com.Printf("Info string length exceeded\n")
                return s
            }

            sb.append('\\').append(key).append('\\').append(value)

            return sb.toString()
        }

        /**
         * Removes a key and value from an info string.
         */
        public fun Info_RemoveKey(s: String, key: String): String {

            val sb = StringBuffer(512)

            if (key.indexOf('\\') != -1) {
                Com.Printf("Can't use a key with a \\\n")
                return s
            }

            val tk = StringTokenizer(s, "\\")

            while (tk.hasMoreTokens()) {
                val key1 = tk.nextToken()

                if (!tk.hasMoreTokens()) {
                    Com.Printf("MISSING VALUE\n")
                    return s
                }
                val value1 = tk.nextToken()

                if (!key.equals(key1))
                    sb.append('\\').append(key1).append('\\').append(value1)
            }

            return sb.toString()

        }

        /**
         * Some characters are illegal in info strings because they can mess up the
         * server's parsing.
         */
        public fun Info_Validate(s: String): Boolean {
            return !((s.indexOf('"') != -1) || (s.indexOf(';') != -1))
        }

        private val fillspaces = "                     "

        public fun Print(s: String) {

            val sb = StringBuffer(512)
            val tk = StringTokenizer(s, "\\")

            while (tk.hasMoreTokens()) {

                val key1 = tk.nextToken()

                if (!tk.hasMoreTokens()) {
                    Com.Printf("MISSING VALUE\n")
                    return
                }

                val value1 = tk.nextToken()

                sb.append(key1)

                val len = key1.length()

                if (len < 20) {
                    sb.append(fillspaces.substring(len))
                }
                sb.append('=').append(value1).append('\n')
            }
            Com.Printf(sb.toString())
        }
    }
}