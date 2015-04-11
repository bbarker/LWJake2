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
import lwjake2.client.CL
import lwjake2.client.Console
import lwjake2.game.Cmd
import lwjake2.server.SV_MAIN
import lwjake2.sys.Sys
import lwjake2.util.PrintfFormat
import lwjake2.util.Vargs

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Com

 */
public class Com {

    public abstract class RD_Flusher {
        public abstract fun rd_flush(target: Int, buffer: StringBuffer)
    }

    // helper class to replace the pointer-pointer
    public class ParseHelp {
        public constructor(`in`: String?) {
            if (`in` == null) {
                data = null
                length = 0
            } else {
                data = `in`.toCharArray()
                length = data!!.size()
            }
            index = 0
        }

        public constructor(`in`: CharArray) : this(`in`, 0) {
        }

        public constructor(`in`: CharArray, offset: Int) {
            data = `in`
            index = offset
            if (data != null)
                length = data!!.size()
            else
                length = 0
        }

        public fun getchar(): Char {
            if (index < length) {
                return data!![index]
            }
            return 0
        }

        public fun nextchar(): Char {
            // faster than if
            index++
            if (index < length) {
                return data!![index]
            }
            return 0
        }

        public fun prevchar(): Char {
            if (index > 0) {
                index--
                return data!![index]
            }
            return 0
        }

        public fun isEof(): Boolean {
            return index >= length
        }

        public var index: Int = 0
        public var data: CharArray? = null
        private var length: Int = 0

        public fun skipwhites(): Char {
            var c: Char = 0
            while (index < length && ((c = data!![index]) <= ' ') && c != 0)
                index++
            return c
        }

        public fun skipwhitestoeol(): Char {
            var c: Char = 0
            while (index < length && ((c = data!![index]) <= ' ') && c != '\n' && c != 0)
                index++
            return c
        }

        public fun skiptoeol(): Char {
            var c: Char = 0
            while (index < length && (c = data!![index]) != '\n' && c != 0)
                index++
            return c
        }
    }

    companion object {

        var debugContext = ""
        var _debugContext = ""

        var com_argc: Int = 0
        var com_argv = arrayOfNulls<String>(Defines.MAX_NUM_ARGVS)

        var rd_target: Int = 0
        var rd_buffer: StringBuffer? = null
        var rd_buffersize: Int = 0
        var rd_flusher: RD_Flusher? = null

        public fun BeginRedirect(target: Int, buffer: StringBuffer?, buffersize: Int, flush: RD_Flusher?) {
            if (0 == target || null == buffer || 0 == buffersize || null == flush)
                return

            rd_target = target
            rd_buffer = buffer
            rd_buffersize = buffersize
            rd_flusher = flush

            rd_buffer!!.setLength(0)
        }

        public fun EndRedirect() {
            rd_flusher!!.rd_flush(rd_target, rd_buffer)

            rd_target = 0
            rd_buffer = null
            rd_buffersize = 0
            rd_flusher = null
        }

        var recursive = false

        var msg = ""

        public var com_token: CharArray = CharArray(Defines.MAX_TOKEN_CHARS)

        // See GameSpanw.ED_ParseEdict() to see how to use it now.
        public fun Parse(hlp: ParseHelp): String {
            var c: Int
            var len = 0

            if (hlp.data == null) {
                return ""
            }

            while (true) {
                //	   skip whitespace
                hlp.skipwhites()
                if (hlp.isEof()) {
                    hlp.data = null
                    return ""
                }

                //	   skip // comments
                if (hlp.getchar() == '/') {
                    if (hlp.nextchar() == '/') {
                        hlp.skiptoeol()
                        // goto skip whitespace
                        continue
                    } else {
                        hlp.prevchar()
                        break
                    }
                } else
                    break
            }

            //	   handle quoted strings specially
            if (hlp.getchar() == '\"') {
                hlp.nextchar()
                while (true) {
                    c = hlp.getchar().toInt()
                    hlp.nextchar()
                    if (c == '\"' || c == 0) {
                        return String(com_token, 0, len)
                    }
                    if (len < Defines.MAX_TOKEN_CHARS) {
                        com_token[len] = c.toChar()
                        len++
                    }
                }
            }

            //	   parse a regular word
            c = hlp.getchar().toInt()
            do {
                if (len < Defines.MAX_TOKEN_CHARS) {
                    com_token[len] = c.toChar()
                    len++
                }
                c = hlp.nextchar().toInt()
            } while (c > 32)

            if (len == Defines.MAX_TOKEN_CHARS) {
                Com.Printf("Token exceeded " + Defines.MAX_TOKEN_CHARS + " chars, discarded.\n")
                len = 0
            }

            return String(com_token, 0, len)
        }

        public var Error_f: xcommand_t = object : xcommand_t() {
            throws(javaClass<longjmpException>())
            public fun execute() {
                Error(Defines.ERR_FATAL, Cmd.Argv(1))
            }
        }

        throws(javaClass<longjmpException>())
        public fun Error(code: Int, fmt: String) {
            Error(code, fmt, null)
        }

        throws(javaClass<longjmpException>())
        public fun Error(code: Int, fmt: String, vargs: Vargs?) {
            // va_list argptr;
            // static char msg[MAXPRINTMSG];

            if (recursive) {
                Sys.Error("recursive error after: " + msg)
            }
            recursive = true

            msg = sprintf(fmt, vargs)

            if (code == Defines.ERR_DISCONNECT) {
                CL.Drop()
                recursive = false
                throw longjmpException()
            } else if (code == Defines.ERR_DROP) {
                Com.Printf("********************\nERROR: " + msg + "\n********************\n")
                SV_MAIN.SV_Shutdown("Server crashed: " + msg + "\n", false)
                CL.Drop()
                recursive = false
                throw longjmpException()
            } else {
                SV_MAIN.SV_Shutdown("Server fatal crashed: %s" + msg + "\n", false)
                CL.Shutdown()
            }

            Sys.Error(msg)
        }

        /**
         * Com_InitArgv checks the number of command line arguments
         * and copies all arguments with valid length into com_argv.
         */
        throws(javaClass<longjmpException>())
        fun InitArgv(args: Array<String>) {

            if (args.size() > Defines.MAX_NUM_ARGVS) {
                Com.Error(Defines.ERR_FATAL, "argc > MAX_NUM_ARGVS")
            }

            Com.com_argc = args.size()
            for (i in 0..Com.com_argc - 1) {
                if (args[i].length() >= Defines.MAX_TOKEN_CHARS)
                    Com.com_argv[i] = ""
                else
                    Com.com_argv[i] = args[i]
            }
        }

        public fun DPrintf(fmt: String) {
            _debugContext = debugContext
            DPrintf(fmt, null)
            _debugContext = ""
        }

        public fun dprintln(fmt: String) {
            DPrintf(_debugContext + fmt + "\n", null)
        }

        public fun Printf(fmt: String) {
            Printf(_debugContext.toInt() + fmt.toInt(), null)
        }

        public fun DPrintf(fmt: String, vargs: Vargs?) {
            if (Globals.developer == null || Globals.developer.value == 0)
                return  // don't confuse non-developers with techie stuff...
            _debugContext = debugContext
            Printf(fmt, vargs)
            _debugContext = ""
        }

        /** Prints out messages, which can also be redirected to a remote client.  */
        public fun Printf(fmt: String, vargs: Vargs?) {
            val msg = sprintf(_debugContext.toInt() + fmt.toInt(), vargs)
            if (rd_target != 0) {
                if ((msg.length() + rd_buffer!!.length()) > (rd_buffersize - 1)) {
                    rd_flusher!!.rd_flush(rd_target, rd_buffer)
                    rd_buffer!!.setLength(0)
                }
                rd_buffer!!.append(msg)
                return
            }

            Console.Print(msg)

            // also echo to debugging console
            Sys.ConsoleOutput(msg)

            // logfile
            if (Globals.logfile_active != null && Globals.logfile_active.value != 0) {
                val name: String

                if (Globals.logfile == null) {
                    name = FS.Gamedir() + "/qconsole.log"
                    if (Globals.logfile_active.value > 2)
                        try {
                            Globals.logfile = RandomAccessFile(name, "rw")
                            Globals.logfile.seek(Globals.logfile.length())
                        } catch (e: Exception) {
                            // TODO: do quake2 error handling!
                            e.printStackTrace()
                        }
                    else
                        try {
                            Globals.logfile = RandomAccessFile(name, "rw")
                        } catch (e1: FileNotFoundException) {
                            // TODO: do quake2 error handling!
                            e1.printStackTrace()
                        }

                }
                if (Globals.logfile != null)
                    try {
                        Globals.logfile.writeChars(msg)
                    } catch (e: IOException) {
                        // TODO: do quake2 error handling!
                        e.printStackTrace()
                    }

                if (Globals.logfile_active.value > 1)  // do nothing
                // fflush (logfile);		// force it to save every time
            }
        }

        public fun Println(fmt: String) {
            Printf(_debugContext + fmt + "\n")
        }

        public fun sprintf(fmt: String, vargs: Vargs?): String {
            var msg = ""
            if (vargs == null || vargs!!.size() == 0) {
                msg = fmt
            } else {
                msg = PrintfFormat(fmt).sprintf(vargs!!.toArray())
            }
            return msg
        }

        public fun Argc(): Int {
            return Com.com_argc
        }

        public fun Argv(arg: Int): String {
            if (arg < 0 || arg >= Com.com_argc || Com.com_argv[arg].length() < 1)
                return ""
            return Com.com_argv[arg]
        }

        public fun ClearArgv(arg: Int) {
            if (arg < 0 || arg >= Com.com_argc || Com.com_argv[arg].length() < 1)
                return
            Com.com_argv[arg] = ""
        }

        public fun Quit() {
            SV_MAIN.SV_Shutdown("Server quit\n", false)
            CL.Shutdown()

            if (Globals.logfile != null) {
                try {
                    Globals.logfile.close()
                } catch (e: IOException) {
                }

                Globals.logfile = null
            }

            Sys.Quit()
        }

        public fun SetServerState(i: Int) {
            Globals.server_state = i
        }

        public fun BlockChecksum(buf: ByteArray, length: Int): Int {
            return MD4.Com_BlockChecksum(buf, length)
        }

        public fun StripExtension(string: String): String {
            val i = string.lastIndexOf('.')
            if (i < 0)
                return string
            return string.substring(0, i)
        }

        /**
         * CRC table.
         */
        var chktbl = byteArray(132.toByte(), 71.toByte(), 81.toByte(), 193.toByte(), 147.toByte(), 34.toByte(), 33.toByte(), 36.toByte(), 47.toByte(), 102.toByte(), 96.toByte(), 77.toByte(), 176.toByte(), 124.toByte(), 218.toByte(), 136.toByte(), 84.toByte(), 21.toByte(), 43.toByte(), 198.toByte(), 108.toByte(), 137.toByte(), 197.toByte(), 157.toByte(), 72.toByte(), 238.toByte(), 230.toByte(), 138.toByte(), 181.toByte(), 244.toByte(), 203.toByte(), 251.toByte(), 241.toByte(), 12.toByte(), 46.toByte(), 160.toByte(), 215.toByte(), 201.toByte(), 31.toByte(), 214.toByte(), 6.toByte(), 154.toByte(), 9.toByte(), 65.toByte(), 84.toByte(), 103.toByte(), 70.toByte(), 199.toByte(), 116.toByte(), 227.toByte(), 200.toByte(), 182.toByte(), 93.toByte(), 166.toByte(), 54.toByte(), 196.toByte(), 171.toByte(), 44.toByte(), 126.toByte(), 133.toByte(), 168.toByte(), 164.toByte(), 166.toByte(), 77.toByte(), 150.toByte(), 25.toByte(), 25.toByte(), 154.toByte(), 204.toByte(), 216.toByte(), 172.toByte(), 57.toByte(), 94.toByte(), 60.toByte(), 242.toByte(), 245.toByte(), 90.toByte(), 114.toByte(), 229.toByte(), 169.toByte(), 209.toByte(), 179.toByte(), 35.toByte(), 130.toByte(), 111.toByte(), 41.toByte(), 203.toByte(), 209.toByte(), 204.toByte(), 113.toByte(), 251.toByte(), 234.toByte(), 146.toByte(), 235.toByte(), 28.toByte(), 202.toByte(), 76.toByte(), 112.toByte(), 254.toByte(), 77.toByte(), 201.toByte(), 103.toByte(), 67.toByte(), 71.toByte(), 148.toByte(), 185.toByte(), 71.toByte(), 188.toByte(), 63.toByte(), 1.toByte(), 171.toByte(), 123.toByte(), 166.toByte(), 226.toByte(), 118.toByte(), 239.toByte(), 90.toByte(), 122.toByte(), 41.toByte(), 11.toByte(), 81.toByte(), 84.toByte(), 103.toByte(), 216.toByte(), 28.toByte(), 20.toByte(), 62.toByte(), 41.toByte(), 236.toByte(), 233.toByte(), 45.toByte(), 72.toByte(), 103.toByte(), 255.toByte(), 237.toByte(), 84.toByte(), 79.toByte(), 72.toByte(), 192.toByte(), 170.toByte(), 97.toByte(), 247.toByte(), 120.toByte(), 18.toByte(), 3.toByte(), 122.toByte(), 158.toByte(), 139.toByte(), 207.toByte(), 131.toByte(), 123.toByte(), 174.toByte(), 202.toByte(), 123.toByte(), 217.toByte(), 233.toByte(), 83.toByte(), 42.toByte(), 235.toByte(), 210.toByte(), 216.toByte(), 205.toByte(), 163.toByte(), 16.toByte(), 37.toByte(), 120.toByte(), 90.toByte(), 181.toByte(), 35.toByte(), 6.toByte(), 147.toByte(), 183.toByte(), 132.toByte(), 210.toByte(), 189.toByte(), 150.toByte(), 117.toByte(), 165.toByte(), 94.toByte(), 207.toByte(), 78.toByte(), 233.toByte(), 80.toByte(), 161.toByte(), 230.toByte(), 157.toByte(), 177.toByte(), 227.toByte(), 133.toByte(), 102.toByte(), 40.toByte(), 78.toByte(), 67.toByte(), 220.toByte(), 110.toByte(), 187.toByte(), 51.toByte(), 158.toByte(), 243.toByte(), 13.toByte(), 0.toByte(), 193.toByte(), 207.toByte(), 103.toByte(), 52.toByte(), 6.toByte(), 124.toByte(), 113.toByte(), 227.toByte(), 99.toByte(), 183.toByte(), 183.toByte(), 223.toByte(), 146.toByte(), 196.toByte(), 194.toByte(), 37.toByte(), 92.toByte(), 255.toByte(), 195.toByte(), 110.toByte(), 252.toByte(), 170.toByte(), 30.toByte(), 42.toByte(), 72.toByte(), 17.toByte(), 28.toByte(), 54.toByte(), 104.toByte(), 120.toByte(), 134.toByte(), 121.toByte(), 48.toByte(), 195.toByte(), 214.toByte(), 222.toByte(), 188.toByte(), 58.toByte(), 42.toByte(), 109.toByte(), 30.toByte(), 70.toByte(), 221.toByte(), 224.toByte(), 128.toByte(), 30.toByte(), 68.toByte(), 59.toByte(), 111.toByte(), 175.toByte(), 49.toByte(), 218.toByte(), 162.toByte(), 189.toByte(), 119.toByte(), 6.toByte(), 86.toByte(), 192.toByte(), 183.toByte(), 146.toByte(), 75.toByte(), 55.toByte(), 192.toByte(), 252.toByte(), 194.toByte(), 213.toByte(), 251.toByte(), 168.toByte(), 218.toByte(), 245.toByte(), 87.toByte(), 168.toByte(), 24.toByte(), 192.toByte(), 223.toByte(), 231.toByte(), 170.toByte(), 42.toByte(), 224.toByte(), 124.toByte(), 111.toByte(), 119.toByte(), 177.toByte(), 38.toByte(), 186.toByte(), 249.toByte(), 46.toByte(), 29.toByte(), 22.toByte(), 203.toByte(), 184.toByte(), 162.toByte(), 68.toByte(), 213.toByte(), 47.toByte(), 26.toByte(), 121.toByte(), 116.toByte(), 135.toByte(), 75.toByte(), 0.toByte(), 201.toByte(), 74.toByte(), 58.toByte(), 101.toByte(), 143.toByte(), 230.toByte(), 93.toByte(), 229.toByte(), 10.toByte(), 119.toByte(), 216.toByte(), 26.toByte(), 20.toByte(), 65.toByte(), 117.toByte(), 177.toByte(), 226.toByte(), 80.toByte(), 44.toByte(), 147.toByte(), 56.toByte(), 43.toByte(), 109.toByte(), 243.toByte(), 246.toByte(), 219.toByte(), 31.toByte(), 205.toByte(), 255.toByte(), 20.toByte(), 112.toByte(), 231.toByte(), 22.toByte(), 232.toByte(), 61.toByte(), 240.toByte(), 227.toByte(), 188.toByte(), 94.toByte(), 182.toByte(), 63.toByte(), 204.toByte(), 129.toByte(), 36.toByte(), 103.toByte(), 243.toByte(), 151.toByte(), 59.toByte(), 254.toByte(), 58.toByte(), 150.toByte(), 133.toByte(), 223.toByte(), 228.toByte(), 110.toByte(), 60.toByte(), 133.toByte(), 5.toByte(), 14.toByte(), 163.toByte(), 43.toByte(), 7.toByte(), 200.toByte(), 191.toByte(), 229.toByte(), 19.toByte(), 130.toByte(), 98.toByte(), 8.toByte(), 97.toByte(), 105.toByte(), 75.toByte(), 71.toByte(), 98.toByte(), 115.toByte(), 68.toByte(), 100.toByte(), 142.toByte(), 226.toByte(), 145.toByte(), 166.toByte(), 154.toByte(), 183.toByte(), 233.toByte(), 4.toByte(), 182.toByte(), 84.toByte(), 12.toByte(), 197.toByte(), 169.toByte(), 71.toByte(), 166.toByte(), 201.toByte(), 8.toByte(), 254.toByte(), 78.toByte(), 166.toByte(), 204.toByte(), 138.toByte(), 91.toByte(), 144.toByte(), 111.toByte(), 43.toByte(), 63.toByte(), 182.toByte(), 10.toByte(), 150.toByte(), 192.toByte(), 120.toByte(), 88.toByte(), 60.toByte(), 118.toByte(), 109.toByte(), 148.toByte(), 26.toByte(), 228.toByte(), 78.toByte(), 184.toByte(), 56.toByte(), 187.toByte(), 245.toByte(), 235.toByte(), 41.toByte(), 216.toByte(), 176.toByte(), 243.toByte(), 21.toByte(), 30.toByte(), 153.toByte(), 150.toByte(), 60.toByte(), 93.toByte(), 99.toByte(), 213.toByte(), 177.toByte(), 173.toByte(), 82.toByte(), 184.toByte(), 85.toByte(), 112.toByte(), 117.toByte(), 62.toByte(), 26.toByte(), 213.toByte(), 218.toByte(), 246.toByte(), 122.toByte(), 72.toByte(), 125.toByte(), 68.toByte(), 65.toByte(), 249.toByte(), 17.toByte(), 206.toByte(), 215.toByte(), 202.toByte(), 165.toByte(), 61.toByte(), 122.toByte(), 121.toByte(), 126.toByte(), 125.toByte(), 37.toByte(), 27.toByte(), 119.toByte(), 188.toByte(), 247.toByte(), 199.toByte(), 15.toByte(), 132.toByte(), 149.toByte(), 16.toByte(), 146.toByte(), 103.toByte(), 21.toByte(), 17.toByte(), 90.toByte(), 94.toByte(), 65.toByte(), 102.toByte(), 15.toByte(), 56.toByte(), 3.toByte(), 178.toByte(), 241.toByte(), 93.toByte(), 248.toByte(), 171.toByte(), 192.toByte(), 2.toByte(), 118.toByte(), 132.toByte(), 40.toByte(), 244.toByte(), 157.toByte(), 86.toByte(), 70.toByte(), 96.toByte(), 32.toByte(), 219.toByte(), 104.toByte(), 167.toByte(), 187.toByte(), 238.toByte(), 172.toByte(), 21.toByte(), 1.toByte(), 47.toByte(), 32.toByte(), 9.toByte(), 219.toByte(), 192.toByte(), 22.toByte(), 161.toByte(), 137.toByte(), 249.toByte(), 148.toByte(), 89.toByte(), 0.toByte(), 193.toByte(), 118.toByte(), 191.toByte(), 193.toByte(), 77.toByte(), 93.toByte(), 45.toByte(), 169.toByte(), 133.toByte(), 44.toByte(), 214.toByte(), 211.toByte(), 20.toByte(), 204.toByte(), 2.toByte(), 195.toByte(), 194.toByte(), 250.toByte(), 107.toByte(), 183.toByte(), 166.toByte(), 239.toByte(), 221.toByte(), 18.toByte(), 38.toByte(), 164.toByte(), 99.toByte(), 227.toByte(), 98.toByte(), 189.toByte(), 86.toByte(), 138.toByte(), 82.toByte(), 43.toByte(), 185.toByte(), 223.toByte(), 9.toByte(), 188.toByte(), 14.toByte(), 151.toByte(), 169.toByte(), 176.toByte(), 130.toByte(), 70.toByte(), 8.toByte(), 213.toByte(), 26.toByte(), 142.toByte(), 27.toByte(), 167.toByte(), 144.toByte(), 152.toByte(), 185.toByte(), 187.toByte(), 60.toByte(), 23.toByte(), 154.toByte(), 242.toByte(), 130.toByte(), 186.toByte(), 100.toByte(), 10.toByte(), 127.toByte(), 202.toByte(), 90.toByte(), 140.toByte(), 124.toByte(), 211.toByte(), 121.toByte(), 9.toByte(), 91.toByte(), 38.toByte(), 187.toByte(), 189.toByte(), 37.toByte(), 223.toByte(), 61.toByte(), 111.toByte(), 154.toByte(), 143.toByte(), 238.toByte(), 33.toByte(), 102.toByte(), 176.toByte(), 141.toByte(), 132.toByte(), 76.toByte(), 145.toByte(), 69.toByte(), 212.toByte(), 119.toByte(), 79.toByte(), 179.toByte(), 140.toByte(), 188.toByte(), 168.toByte(), 153.toByte(), 170.toByte(), 25.toByte(), 83.toByte(), 124.toByte(), 2.toByte(), 135.toByte(), 187.toByte(), 11.toByte(), 124.toByte(), 26.toByte(), 45.toByte(), 223.toByte(), 72.toByte(), 68.toByte(), 6.toByte(), 214.toByte(), 125.toByte(), 12.toByte(), 45.toByte(), 53.toByte(), 118.toByte(), 174.toByte(), 196.toByte(), 95.toByte(), 113.toByte(), 133.toByte(), 151.toByte(), 196.toByte(), 61.toByte(), 239.toByte(), 82.toByte(), 190.toByte(), 0.toByte(), 228.toByte(), 205.toByte(), 73.toByte(), 209.toByte(), 209.toByte(), 28.toByte(), 60.toByte(), 208.toByte(), 28.toByte(), 66.toByte(), 175.toByte(), 212.toByte(), 189.toByte(), 88.toByte(), 52.toByte(), 7.toByte(), 50.toByte(), 238.toByte(), 185.toByte(), 181.toByte(), 234.toByte(), 255.toByte(), 215.toByte(), 140.toByte(), 13.toByte(), 46.toByte(), 47.toByte(), 175.toByte(), 135.toByte(), 187.toByte(), 230.toByte(), 82.toByte(), 113.toByte(), 34.toByte(), 245.toByte(), 37.toByte(), 23.toByte(), 161.toByte(), 130.toByte(), 4.toByte(), 194.toByte(), 74.toByte(), 189.toByte(), 87.toByte(), 198.toByte(), 171.toByte(), 200.toByte(), 53.toByte(), 12.toByte(), 60.toByte(), 217.toByte(), 194.toByte(), 67.toByte(), 219.toByte(), 39.toByte(), 146.toByte(), 207.toByte(), 184.toByte(), 37.toByte(), 96.toByte(), 250.toByte(), 33.toByte(), 59.toByte(), 4.toByte(), 82.toByte(), 200.toByte(), 150.toByte(), 186.toByte(), 116.toByte(), 227.toByte(), 103.toByte(), 62.toByte(), 142.toByte(), 141.toByte(), 97.toByte(), 144.toByte(), 146.toByte(), 89.toByte(), 182.toByte(), 26.toByte(), 28.toByte(), 94.toByte(), 33.toByte(), 193.toByte(), 101.toByte(), 229.toByte(), 166.toByte(), 52.toByte(), 5.toByte(), 111.toByte(), 197.toByte(), 96.toByte(), 177.toByte(), 131.toByte(), 193.toByte(), 213.toByte(), 213.toByte(), 237.toByte(), 217.toByte(), 199.toByte(), 17.toByte(), 123.toByte(), 73.toByte(), 122.toByte(), 249.toByte(), 249.toByte(), 132.toByte(), 71.toByte(), 155.toByte(), 226.toByte(), 165.toByte(), 130.toByte(), 224.toByte(), 194.toByte(), 136.toByte(), 208.toByte(), 178.toByte(), 88.toByte(), 136.toByte(), 127.toByte(), 69.toByte(), 9.toByte(), 103.toByte(), 116.toByte(), 97.toByte(), 191.toByte(), 230.toByte(), 64.toByte(), 226.toByte(), 157.toByte(), 194.toByte(), 71.toByte(), 5.toByte(), 137.toByte(), 237.toByte(), 203.toByte(), 187.toByte(), 183.toByte(), 39.toByte(), 231.toByte(), 220.toByte(), 122.toByte(), 253.toByte(), 191.toByte(), 168.toByte(), 208.toByte(), 170.toByte(), 16.toByte(), 57.toByte(), 60.toByte(), 32.toByte(), 240.toByte(), 211.toByte(), 110.toByte(), 177.toByte(), 114.toByte(), 248.toByte(), 230.toByte(), 15.toByte(), 239.toByte(), 55.toByte(), 229.toByte(), 9.toByte(), 51.toByte(), 90.toByte(), 131.toByte(), 67.toByte(), 128.toByte(), 79.toByte(), 101.toByte(), 47.toByte(), 124.toByte(), 140.toByte(), 106.toByte(), 160.toByte(), 130.toByte(), 12.toByte(), 212.toByte(), 212.toByte(), 250.toByte(), 129.toByte(), 96.toByte(), 61.toByte(), 223.toByte(), 6.toByte(), 241.toByte(), 95.toByte(), 8.toByte(), 13.toByte(), 109.toByte(), 67.toByte(), 242.toByte(), 227.toByte(), 17.toByte(), 125.toByte(), 128.toByte(), 50.toByte(), 197.toByte(), 251.toByte(), 197.toByte(), 217.toByte(), 39.toByte(), 236.toByte(), 198.toByte(), 78.toByte(), 101.toByte(), 39.toByte(), 118.toByte(), 135.toByte(), 166.toByte(), 238.toByte(), 238.toByte(), 215.toByte(), 139.toByte(), 209.toByte(), 160.toByte(), 92.toByte(), 176.toByte(), 66.toByte(), 19.toByte(), 14.toByte(), 149.toByte(), 74.toByte(), 242.toByte(), 6.toByte(), 198.toByte(), 67.toByte(), 51.toByte(), 244.toByte(), 199.toByte(), 248.toByte(), 231.toByte(), 31.toByte(), 221.toByte(), 228.toByte(), 70.toByte(), 74.toByte(), 112.toByte(), 57.toByte(), 108.toByte(), 208.toByte(), 237.toByte(), 202.toByte(), 190.toByte(), 96.toByte(), 59.toByte(), 209.toByte(), 123.toByte(), 87.toByte(), 72.toByte(), 229.toByte(), 58.toByte(), 121.toByte(), 193.toByte(), 105.toByte(), 51.toByte(), 83.toByte(), 27.toByte(), 128.toByte(), 184.toByte(), 145.toByte(), 125.toByte(), 180.toByte(), 246.toByte(), 23.toByte(), 26.toByte(), 29.toByte(), 90.toByte(), 50.toByte(), 214.toByte(), 204.toByte(), 113.toByte(), 41.toByte(), 63.toByte(), 40.toByte(), 187.toByte(), 243.toByte(), 94.toByte(), 113.toByte(), 184.toByte(), 67.toByte(), 175.toByte(), 248.toByte(), 185.toByte(), 100.toByte(), 239.toByte(), 196.toByte(), 165.toByte(), 108.toByte(), 8.toByte(), 83.toByte(), 199.toByte(), 0.toByte(), 16.toByte(), 57.toByte(), 79.toByte(), 221.toByte(), 228.toByte(), 182.toByte(), 25.toByte(), 39.toByte(), 251.toByte(), 184.toByte(), 245.toByte(), 50.toByte(), 115.toByte(), 229.toByte(), 203.toByte(), 50.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        var chkb = ByteArray(60 + 4)

        /**
         * Calculates a crc checksum-sequence over an array.
         */
        public fun BlockSequenceCRCByte(base: ByteArray, offset: Int, length: Int, sequence: Int): Byte {
            var length = length
            if (sequence < 0)
                Sys.Error("sequence < 0, this shouldn't happen\n")

            //p_ndx = (sequence % (sizeof(chktbl) - 4));
            val p_ndx = (sequence % (1024 - 4))

            //memcpy(chkb, base, length);
            length = Math.min(60, length)
            System.arraycopy(base, offset, chkb, 0, length)

            chkb[length] = chktbl[p_ndx + 0]
            chkb[length + 1] = chktbl[p_ndx + 1]
            chkb[length + 2] = chktbl[p_ndx + 2]
            chkb[length + 3] = chktbl[p_ndx + 3]

            length += 4

            // unsigned short
            var crc = CRC.CRC_Block(chkb, length)

            var x = 0
            for (n in 0..length - 1)
                x += chkb[n] and 255

            crc = crc xor x

            return (crc and 255).toByte()
        }
    }

}