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
import lwjake2.util.Lib

import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.StringTokenizer

public class GameSVCmds {

    /**

     * PACKET FILTERING


     * You can add or remove addresses from the filter list with:

     * addip  removeip

     * The ip address is specified in dot format, and any unspecified digits
     * will match any value, so you can specify an entire class C network with
     * "addip 192.246.40".

     * Removeip will only remove an address specified exactly the same way. You
     * cannot addip a subnet, then removeip a single host.

     * listip Prints the current list of filters.

     * writeip Dumps "addip " commands to listip.cfg so it can be execed at
     * a later date. The filter lists are not saved and restored by default,
     * because I beleive it would cause too much confusion.

     * filterban <0 or 1>

     * If 1 (the default), then ip addresses matching the current list will be
     * prohibited from entering the game. This is the default setting.

     * If 0, then only addresses matching the list will be allowed. This lets
     * you easily set up a private game, or a game that only allows players from
     * your local network.

     */

    public class ipfilter_t {
        var mask: Int = 0

        var compare: Int = 0
    }

    companion object {

        public fun Svcmd_Test_f() {
            GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Svcmd_Test_f()\n")
        }

        public val MAX_IPFILTERS: Int = 1024

        var ipfilters = arrayOfNulls<GameSVCmds.ipfilter_t>(MAX_IPFILTERS)

        var numipfilters: Int = 0

        {
            for (n in 0..GameSVCmds.MAX_IPFILTERS - 1)
                GameSVCmds.ipfilters[n] = ipfilter_t()
        }

        /**
         * StringToFilter.
         */
        fun StringToFilter(s: String, f: GameSVCmds.ipfilter_t): Boolean {

            val b = byteArray(0, 0, 0, 0)
            val m = byteArray(0, 0, 0, 0)

            try {
                val tk = StringTokenizer(s, ". ")

                for (n in 0..4 - 1) {
                    b[n] = Lib.atoi(tk.nextToken()) as Byte
                    if (b[n] != 0)
                        m[n] = (-1).toByte()
                }

                f.mask = ByteBuffer.wrap(m).getInt()
                f.compare = ByteBuffer.wrap(b).getInt()
            } catch (e: Exception) {
                GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Bad filter address: " + s + "\n")
                return false
            }


            return true
        }

        /**
         * SV_FilterPacket.
         */
        fun SV_FilterPacket(from: String): Boolean {
            var i: Int
            val `in`: Int
            val m = intArray(0, 0, 0, 0)

            var p = 0
            var c: Char

            i = 0

            while (p < from.length() && i < 4) {
                m[i] = 0

                c = from.charAt(p)
                while (c >= '0' && c <= '9') {
                    m[i] = m[i] * 10 + (c.toInt() - '0')
                    c = from.charAt(p++)
                }
                if (p == from.length() || c == ':')
                    break

                i++
                p++
            }

            `in` = (m[0] and 255) or ((m[1] and 255) shl 8) or ((m[2] and 255) shl 16) or ((m[3] and 255) shl 24)

            run {
                i = 0
                while (i < numipfilters) {
                    if ((`in` and ipfilters[i].mask) == ipfilters[i].compare)
                        return (GameBase.filterban.value as Int) != 0
                    i++
                }
            }

            return (1.toInt() - GameBase.filterban.value) != 0
        }

        /**
         * SV_AddIP_f.
         */
        fun SVCmd_AddIP_f() {
            var i: Int

            if (GameBase.gi.argc() < 3) {
                GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Usage:  addip <ip-mask>\n")
                return
            }

            run {
                i = 0
                while (i < numipfilters) {
                    if (ipfilters[i].compare == -1)
                        break
                    i++
                }
            } // free spot
            if (i == numipfilters) {
                if (numipfilters == MAX_IPFILTERS) {
                    GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "IP filter list is full\n")
                    return
                }
                numipfilters++
            }

            if (!StringToFilter(GameBase.gi.argv(2), ipfilters[i]))
                ipfilters[i].compare = -1
        }

        /**
         * SV_RemoveIP_f.
         */
        fun SVCmd_RemoveIP_f() {
            val f = GameSVCmds.ipfilter_t()
            var i: Int
            var j: Int

            if (GameBase.gi.argc() < 3) {
                GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Usage:  sv removeip <ip-mask>\n")
                return
            }

            if (!StringToFilter(GameBase.gi.argv(2), f))
                return

            run {
                i = 0
                while (i < numipfilters) {
                    if (ipfilters[i].mask == f.mask && ipfilters[i].compare == f.compare) {
                        run {
                            j = i + 1
                            while (j < numipfilters) {
                                ipfilters[j - 1] = ipfilters[j]
                                j++
                            }
                        }
                        numipfilters--
                        GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Removed.\n")
                        return
                    }
                    i++
                }
            }
            GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Didn't find " + GameBase.gi.argv(2) + ".\n")
        }

        /**
         * SV_ListIP_f.
         */
        fun SVCmd_ListIP_f() {
            var i: Int
            var b: ByteArray

            GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Filter list:\n")
            run {
                i = 0
                while (i < numipfilters) {
                    b = Lib.getIntBytes(ipfilters[i].compare)
                    GameBase.gi.cprintf(null, Defines.PRINT_HIGH, (b[0] and 255) + "." + (b[1] and 255) + "." + (b[2] and 255) + "." + (b[3] and 255))
                    i++
                }
            }
        }

        /**
         * SV_WriteIP_f.
         */
        fun SVCmd_WriteIP_f() {
            val f: RandomAccessFile?
            //char name[MAX_OSPATH];
            val name: String
            var b: ByteArray

            var i: Int
            val game: cvar_t

            game = GameBase.gi.cvar("game", "", 0)

            if (game.string == null)
                name = Defines.GAMEVERSION + "/listip.cfg"
            else
                name = game.string + "/listip.cfg"

            GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Writing " + name + ".\n")

            f = Lib.fopen(name, "rw")
            if (f == null) {
                GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Couldn't open " + name + "\n")
                return
            }

            try {
                f!!.writeChars("set filterban " + GameBase.filterban.value as Int + "\n")

                run {
                    i = 0
                    while (i < numipfilters) {
                        b = Lib.getIntBytes(ipfilters[i].compare)
                        f!!.writeChars("sv addip " + (b[0] and 255) + "." + (b[1] and 255) + "." + (b[2] and 255) + "." + (b[3] and 255) + "\n")
                        i++
                    }
                }

            } catch (e: IOException) {
                Com.Printf("IOError in SVCmd_WriteIP_f:" + e)
            }


            Lib.fclose(f)
        }

        /**
         * ServerCommand

         * ServerCommand will be called when an "sv" command is issued. The game can
         * issue gi.argc() / gi.argv() commands to get the rest of the parameters
         */
        public fun ServerCommand() {
            val cmd: String

            cmd = GameBase.gi.argv(1)
            if (Lib.Q_stricmp(cmd, "test") == 0)
                Svcmd_Test_f()
            else if (Lib.Q_stricmp(cmd, "addip") == 0)
                SVCmd_AddIP_f()
            else if (Lib.Q_stricmp(cmd, "removeip") == 0)
                SVCmd_RemoveIP_f()
            else if (Lib.Q_stricmp(cmd, "listip") == 0)
                SVCmd_ListIP_f()
            else if (Lib.Q_stricmp(cmd, "writeip") == 0)
                SVCmd_WriteIP_f()
            else
                GameBase.gi.cprintf(null, Defines.PRINT_HIGH, "Unknown server command \"" + cmd + "\"\n")
        }
    }
}