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

package lwjake2.sys

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.client.CL
import lwjake2.qcommon.Com

import java.io.File
import java.io.FilenameFilter
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Sys
 */
public class Sys : Defines() {

    /**
     * Match the pattern findpattern against the filename.

     * In the pattern string, `*' matches any sequence of characters, `?'
     * matches any character, [SET] matches any character in the specified set,
     * [!SET] matches any character not in the specified set. A set is composed
     * of characters or ranges; a range looks like character hyphen character
     * (as in 0-9 or A-Z). [0-9a-zA-Z_] is the set of characters allowed in C
     * identifiers. Any other character in the pattern must be matched exactly.
     * To suppress the special syntactic significance of any of `[]*?!-\', and
     * match the character exactly, precede it with a `\'.
     */
    class FileFilter(findpattern: String, var musthave:
    Int, var canthave: Int) : FilenameFilter {

        var regexpr: String

        {
            this.regexpr = convert2regexpr(findpattern)

        }

        /*
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public fun accept(dir: File, name: String): Boolean {
            if (name.matches(regexpr)) {
                return CompareAttributes(dir, musthave, canthave)
            }
            return false
        }

        fun convert2regexpr(pattern: String): String {

            val sb = StringBuffer()

            val c: Char
            var escape = false

            var subst: String?

            // convert pattern
            for (i in 0..pattern.length() - 1) {
                c = pattern.charAt(i)
                subst = null
                when (c) {
                    '*' -> subst = if ((!escape)) ".*" else "*"
                    '.' -> subst = if ((!escape)) "\\." else "."
                    '!' -> subst = if ((!escape)) "^" else "!"
                    '?' -> subst = if ((!escape)) "." else "?"
                    '\\' -> escape = !escape
                    else -> escape = false
                }
                if (subst != null) {
                    sb.append(subst)
                    escape = false
                } else
                    sb.append(c)
            }

            // the converted pattern
            val regexpr = sb.toString()

            //Com.DPrintf("pattern: " + pattern + " regexpr: " + regexpr +
            // '\n');
            try {
                Pattern.compile(regexpr)
            } catch (e: PatternSyntaxException) {
                Com.Printf("invalid file pattern ( *.* is used instead )\n")
                return ".*" // the default
            }

            return regexpr
        }

        fun CompareAttributes(dir: File, musthave: Int, canthave: Int): Boolean {
            // . and .. never match
            val name = dir.getName()

            if (name.equals(".") || name.equals(".."))
                return false

            return true
        }

    }

    companion object {

        public fun Error(error: String) {

            CL.Shutdown()
            //StackTrace();
            Exception(error).printStackTrace()
            System.exit(1)
        }

        public fun Quit() {
            CL.Shutdown()

            System.exit(0)
        }

        //ok!
        public fun FindAll(path: String, musthave: Int, canthave: Int): Array<File>? {

            val index = path.lastIndexOf('/')

            if (index != -1) {
                findbase = path.substring(0, index)
                findpattern = path.substring(index + 1, path.length())
            } else {
                findbase = path
                findpattern = "*"
            }

            if (findpattern.equals("*.*")) {
                findpattern = "*"
            }

            val fdir = File(findbase)

            if (!fdir.exists())
                return null

            val filter = FileFilter(findpattern, musthave, canthave)

            return fdir.listFiles(filter)
        }


        //============================================

        var fdir: Array<File>? = null

        var fileindex: Int = 0

        var findbase: String

        var findpattern: String

        // ok.
        public fun FindFirst(path: String, musthave: Int, canthave: Int): File? {

            if (fdir != null)
                Sys.Error("Sys_BeginFind without close")

            //	COM_FilePath (path, findbase);

            fdir = FindAll(path, canthave, musthave)
            fileindex = 0

            if (fdir == null)
                return null

            return FindNext()
        }

        public fun FindNext(): File? {

            if (fileindex >= fdir!!.size())
                return null

            return fdir!![fileindex++]
        }

        public fun FindClose() {
            fdir = null
        }

        public fun SendKeyEvents() {
            Globals.re.getKeyboardHandler().Update()

            // grab frame time
            Globals.sys_frame_time = Timer.Milliseconds()
        }

        public fun GetClipboardData(): String? {
            // TODO: implement GetClipboardData
            return null
        }

        public fun ConsoleOutput(msg: String) {
            if (Globals.nostdout != null && Globals.nostdout.value != 0)
                return

            System.out.print(msg)
        }
    }

}