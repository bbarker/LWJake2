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
import lwjake2.game.cvar_t
import lwjake2.sys.Sys

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Hashtable
import java.util.LinkedList

/**
 * FS

 * @author cwei
 */
public class FS : Globals() {

    /*
     * ==================================================
     * 
     * QUAKE FILESYSTEM
     * 
     * ==================================================
     */

    public class packfile_t {

        var name: String // char name[56]

        var filepos: Int = 0
        var filelen: Int = 0

        override fun toString(): String {
            return name + " [ length: " + filelen + " pos: " + filepos + " ]"
        }

        companion object {
            val SIZE = 64

            val NAME_SIZE = 56
        }
    }

    public class pack_t {
        var filename: String

        var handle: RandomAccessFile? = null

        var backbuffer: ByteBuffer? = null

        var numfiles: Int = 0

        var files: Hashtable<String, packfile_t>? = null // with packfile_t entries
    }

    public class filelink_t {
        var from: String

        var fromlength: Int = 0

        var to: String
    }

    public class searchpath_t {
        var filename: String

        var pack: pack_t? = null // only one of filename or pack will be used

        var next: searchpath_t
    }

    class dpackheader_t {
        var ident: Int = 0 // IDPAKHEADER

        var dirofs: Int = 0

        var dirlen: Int = 0
    }

    companion object {

        public var fs_gamedir: String? = null

        private var fs_userdir: String? = null

        public var fs_basedir: cvar_t

        public var fs_cddir: cvar_t

        public var fs_gamedirvar: cvar_t

        // with filelink_t entries
        public var fs_links: MutableList<filelink_t> = LinkedList<filelink_t>()

        public var fs_searchpaths: searchpath_t? = null

        // without gamedirs
        public var fs_base_searchpaths: searchpath_t

        /*
     * All of Quake's data access is through a hierchal file system, but the
     * contents of the file system can be transparently merged from several
     * sources.
     * 
     * The "base directory" is the path to the directory holding the quake.exe
     * and all game directories. The sys_* files pass this to host_init in
     * quakeparms_t->basedir. This can be overridden with the "-basedir" command
     * line parm to allow code debugging in a different directory. The base
     * directory is only used during filesystem initialization.
     * 
     * The "game directory" is the first tree on the search path and directory
     * that all generated files (savegames, screenshots, demos, config files)
     * will be saved to. This can be overridden with the "-game" command line
     * parameter. The game directory can never be changed while quake is
     * executing. This is a precacution against having a malicious server
     * instruct clients to write files over areas they shouldn't.
     *  
     */

        /*
     * CreatePath
     * 
     * Creates any directories needed to store the given filename.
     */
        public fun CreatePath(path: String) {
            val index = path.lastIndexOf('/')
            // -1 if not found and 0 means write to root
            if (index > 0) {
                val f = File(path.substring(0, index))
                if (!f.mkdirs() && !f.isDirectory()) {
                    Com.Printf("can't create path \"" + path + '"' + "\n")
                }
            }
        }

        /*
     * FCloseFile
     * 
     * For some reason, other dll's can't just call fclose() on files returned
     * by FS_FOpenFile...
     */
        throws(javaClass<IOException>())
        public fun FCloseFile(file: RandomAccessFile) {
            file.close()
        }

        throws(javaClass<IOException>())
        public fun FCloseFile(stream: InputStream) {
            stream.close()
        }

        public fun FileLength(filename: String): Int {
            var filename = filename
            var search: searchpath_t?
            var netpath: String
            var pak: pack_t
            var link: filelink_t

            file_from_pak = 0

            // check for links first
            run {
                val it = fs_links.iterator()
                while (it.hasNext()) {
                    link = it.next()

                    if (filename.regionMatches(0, link.from, 0, link.fromlength)) {
                        netpath = link.to + filename.substring(link.fromlength)
                        val file = File(netpath)
                        if (file.canRead()) {
                            Com.DPrintf("link file: " + netpath + '\n')
                            return file.length().toInt()
                        }
                        return -1
                    }
                }
            }

            // search through the path, one element at a time

            run {
                search = fs_searchpaths
                while (search != null) {
                    // is the element a pak file?
                    if (search.pack != null) {
                        // look through all the pak file elements
                        pak = search.pack
                        filename = filename.toLowerCase()
                        val entry = pak.files!!.get(filename)

                        if (entry != null) {
                            // found it!
                            file_from_pak = 1
                            Com.DPrintf("PackFile: " + pak.filename + " : " + filename + '\n')
                            // open a new file on the pakfile
                            val file = File(pak.filename)
                            if (!file.canRead()) {
                                Com.Error(Defines.ERR_FATAL, "Couldn't reopen " + pak.filename)
                            }
                            return entry!!.filelen
                        }
                    } else {
                        // check a file in the directory tree
                        netpath = search.filename.toInt() + '/' + filename.toInt()

                        val file = File(netpath)
                        if (!file.canRead())
                            continue

                        Com.DPrintf("FindFile: " + netpath + '\n')

                        return file.length().toInt()
                    }
                    search = search.next
                }
            }
            Com.DPrintf("FindFile: can't find " + filename + '\n')
            return -1
        }

        public var file_from_pak: Int = 0

        /*
     * FOpenFile
     * 
     * Finds the file in the search path. returns a RadomAccesFile. Used for
     * streaming data out of either a pak file or a seperate file.
     */
        throws(javaClass<IOException>())
        public fun FOpenFile(filename: String): RandomAccessFile? {
            var filename = filename
            var search: searchpath_t?
            var netpath: String
            var pak: pack_t
            var link: filelink_t
            var file: File? = null

            file_from_pak = 0

            // check for links first
            run {
                val it = fs_links.iterator()
                while (it.hasNext()) {
                    link = it.next()

                    //			if (!strncmp (filename, link->from, link->fromlength))
                    if (filename.regionMatches(0, link.from, 0, link.fromlength)) {
                        netpath = link.to + filename.substring(link.fromlength)
                        file = File(netpath)
                        if (file!!.canRead()) {
                            //Com.DPrintf ("link file: " + netpath +'\n');
                            return RandomAccessFile(file, "r")
                        }
                        return null
                    }
                }
            }

            //
            // search through the path, one element at a time
            //
            run {
                search = fs_searchpaths
                while (search != null) {
                    // is the element a pak file?
                    if (search.pack != null) {
                        // look through all the pak file elements
                        pak = search.pack
                        filename = filename.toLowerCase()
                        val entry = pak.files!!.get(filename)

                        if (entry != null) {
                            // found it!
                            file_from_pak = 1
                            //Com.DPrintf ("PackFile: " + pak.filename + " : " +
                            // filename + '\n');
                            file = File(pak.filename)
                            if (!file!!.canRead())
                                Com.Error(Defines.ERR_FATAL, "Couldn't reopen " + pak.filename)
                            if (pak.handle == null || !pak.handle!!.getFD().valid()) {
                                // hold the pakfile handle open
                                pak.handle = RandomAccessFile(pak.filename, "r")
                            }
                            // open a new file on the pakfile

                            val raf = RandomAccessFile(file, "r")
                            raf.seek(entry!!.filepos.toLong())

                            return raf
                        }
                    } else {
                        // check a file in the directory tree
                        netpath = search.filename.toInt() + '/' + filename.toInt()

                        file = File(netpath)
                        if (!file!!.canRead())
                            continue

                        //Com.DPrintf("FindFile: " + netpath +'\n');

                        return RandomAccessFile(file, "r")
                    }
                    search = search.next
                }
            }
            //Com.DPrintf ("FindFile: can't find " + filename + '\n');
            return null
        }

        // read in blocks of 64k
        public val MAX_READ: Int = 65536

        /**
         * Read

         * Properly handles partial reads
         */
        public fun Read(buffer: ByteArray, len: Int, f: RandomAccessFile) {

            val block: Int
            var remaining: Int
            var offset = 0
            var read = 0

            // read in chunks for progress bar
            remaining = len

            while (remaining != 0) {
                block = Math.min(remaining, MAX_READ)
                try {
                    read = f.read(buffer, offset, block)
                } catch (e: IOException) {
                    Com.Error(Defines.ERR_FATAL, e.toString())
                }


                if (read == 0) {
                    Com.Error(Defines.ERR_FATAL, "FS_Read: 0 bytes read")
                } else if (read == -1) {
                    Com.Error(Defines.ERR_FATAL, "FS_Read: -1 bytes read")
                }
                //
                // do some progress bar thing here...
                //
                remaining -= read
                offset += read
            }
        }

        /*
     * LoadFile
     * 
     * Filename are reletive to the quake search path a null buffer will just
     * return the file content as byte[]
     */
        public fun LoadFile(path: String): ByteArray? {
            var path = path
            val file: RandomAccessFile

            var buf: ByteArray? = null
            var len = 0

            // TODO hack for bad strings (fuck \0)
            val index = path.indexOf('\0')
            if (index != -1)
                path = path.substring(0, index)

            // look for it in the filesystem or pack files
            len = FileLength(path)

            if (len < 1)
                return null

            try {
                file = FOpenFile(path)
                //Read(buf = new byte[len], len, h);
                buf = ByteArray(len)
                file.readFully(buf)
                file.close()
            } catch (e: IOException) {
                Com.Error(Defines.ERR_FATAL, e.toString())
            }

            return buf
        }

        /*
     * LoadMappedFile
     * 
     * Filename are reletive to the quake search path a null buffer will just
     * return the file content as ByteBuffer (memory mapped)
     */
        public fun LoadMappedFile(filename: String): ByteBuffer? {
            var filename = filename
            var search: searchpath_t?
            var netpath: String
            var pak: pack_t
            var link: filelink_t
            var file: File? = null

            var fileLength = 0
            var channel: FileChannel? = null
            var input: FileInputStream? = null
            var buffer: ByteBuffer? = null

            file_from_pak = 0

            try {
                // check for links first
                run {
                    val it = fs_links.iterator()
                    while (it.hasNext()) {
                        link = it.next()

                        if (filename.regionMatches(0, link.from, 0, link.fromlength)) {
                            netpath = link.to + filename.substring(link.fromlength)
                            file = File(netpath)
                            if (file!!.canRead()) {
                                input = FileInputStream(file)
                                channel = input!!.getChannel()
                                fileLength = channel!!.size().toInt()
                                buffer = channel!!.map(FileChannel.MapMode.READ_ONLY, 0, fileLength.toLong())
                                input!!.close()
                                return buffer
                            }
                            return null
                        }
                    }
                }

                //
                // search through the path, one element at a time
                //
                run {
                    search = fs_searchpaths
                    while (search != null) {
                        // is the element a pak file?
                        if (search.pack != null) {
                            // look through all the pak file elements
                            pak = search.pack
                            filename = filename.toLowerCase()
                            val entry = pak.files!!.get(filename)

                            if (entry != null) {
                                // found it!
                                file_from_pak = 1
                                //Com.DPrintf ("PackFile: " + pak.filename + " : " +
                                // filename + '\n');
                                file = File(pak.filename)
                                if (!file!!.canRead())
                                    Com.Error(Defines.ERR_FATAL, "Couldn't reopen " + pak.filename)
                                if (pak.handle == null || !pak.handle!!.getFD().valid()) {
                                    // hold the pakfile handle open
                                    pak.handle = RandomAccessFile(pak.filename, "r")
                                }
                                // open a new file on the pakfile
                                if (pak.backbuffer == null) {
                                    channel = pak.handle!!.getChannel()
                                    pak.backbuffer = channel!!.map(FileChannel.MapMode.READ_ONLY, 0, pak.handle!!.length())
                                    channel!!.close()
                                }
                                pak.backbuffer!!.position(entry!!.filepos)
                                buffer = pak.backbuffer!!.slice()
                                buffer!!.limit(entry!!.filelen)
                                return buffer
                            }
                        } else {
                            // check a file in the directory tree
                            netpath = search.filename.toInt() + '/' + filename.toInt()

                            file = File(netpath)
                            if (!file!!.canRead())
                                continue

                            //Com.DPrintf("FindFile: " + netpath +'\n');
                            input = FileInputStream(file)
                            channel = input!!.getChannel()
                            fileLength = channel!!.size().toInt()
                            buffer = channel!!.map(FileChannel.MapMode.READ_ONLY, 0, fileLength.toLong())
                            input!!.close()
                            return buffer
                        }
                        search = search.next
                    }
                }
            } catch (e: Exception) {
            }

            try {
                if (input != null)
                    input!!.close()
                else if (channel != null && channel!!.isOpen())
                    channel!!.close()
            } catch (ioe: IOException) {
            }

            return null
        }

        /*
     * FreeFile
     */
        public fun FreeFile(buffer: ByteArray?) {
            var buffer = buffer
            buffer = null
        }

        val IDPAKHEADER = (('K' shl 24) + ('C' shl 16) + ('A' shl 8) + 'P')

        val MAX_FILES_IN_PACK = 4096

        // buffer for C-Strings char[56]
        var tmpText = ByteArray(packfile_t.NAME_SIZE)

        /*
     * LoadPackFile
     * 
     * Takes an explicit (not game tree related) path to a pak file.
     * 
     * Loads the header and directory, adding the files at the beginning of the
     * list so they override previous pack files.
     */
        fun LoadPackFile(packfile: String): pack_t? {

            val header: dpackheader_t
            val newfiles: Hashtable<String, packfile_t>
            val file: RandomAccessFile
            var numpackfiles = 0
            var pack: pack_t? = null
            //		unsigned checksum;
            //
            try {
                file = RandomAccessFile(packfile, "r")
                val fc = file.getChannel()
                val packhandle = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                packhandle!!.order(ByteOrder.LITTLE_ENDIAN)

                fc.close()

                if (packhandle == null || packhandle!!.limit() < 1)
                    return null
                //
                header = dpackheader_t()
                header.ident = packhandle!!.getInt()
                header.dirofs = packhandle!!.getInt()
                header.dirlen = packhandle!!.getInt()

                if (header.ident != IDPAKHEADER)
                    Com.Error(Defines.ERR_FATAL, packfile + " is not a packfile")

                numpackfiles = header.dirlen / packfile_t.SIZE

                if (numpackfiles > MAX_FILES_IN_PACK)
                    Com.Error(Defines.ERR_FATAL, packfile + " has " + numpackfiles + " files")

                newfiles = Hashtable<String, packfile_t>(numpackfiles)

                packhandle!!.position(header.dirofs)

                // parse the directory
                var entry: packfile_t? = null

                for (i in 0..numpackfiles - 1) {
                    packhandle!!.get(tmpText)

                    entry = packfile_t()
                    entry!!.name = String(tmpText).trim()
                    entry!!.filepos = packhandle!!.getInt()
                    entry!!.filelen = packhandle!!.getInt()

                    newfiles.put(entry!!.name.toLowerCase(), entry)
                }

            } catch (e: IOException) {
                Com.DPrintf(e.getMessage() + '\n')
                return null
            }


            pack = pack_t()
            pack!!.filename = String(packfile)
            pack!!.handle = file
            pack!!.numfiles = numpackfiles
            pack!!.files = newfiles

            Com.Printf("Added packfile " + packfile + " (" + numpackfiles + " files)\n")

            return pack
        }

        /*
     * AddGameDirectory
     * 
     * Sets fs_gamedir, adds the directory to the head of the path, then loads
     * and adds pak1.pak pak2.pak ...
     */
        fun AddGameDirectory(dir: String) {
            var i: Int
            var search: searchpath_t
            var pak: pack_t?
            var pakfile: String

            fs_gamedir = String(dir)

            //
            // add the directory to the search path
            // ensure fs_userdir is first in searchpath
            search = searchpath_t()
            search.filename = String(dir)
            if (fs_searchpaths != null) {
                search.next = fs_searchpaths!!.next
                fs_searchpaths!!.next = search
            } else {
                fs_searchpaths = search
            }

            //
            // add any pak files in the format pak0.pak pak1.pak, ...
            //
            run {
                i = 0
                while (i < 10) {
                    pakfile = dir + "/pak" + i + ".pak"
                    if (!(File(pakfile).canRead()))
                        continue

                    pak = LoadPackFile(pakfile)
                    if (pak == null)
                        continue

                    search = searchpath_t()
                    search.pack = pak
                    search.filename = ""
                    search.next = fs_searchpaths
                    fs_searchpaths = search
                    i++
                }
            }
        }

        /*
     * Gamedir
     * 
     * Called to find where to write a file (demos, savegames, etc)
     * this is modified to <user.home>/.lwjake2 
     */
        public fun Gamedir(): String {
            return if ((fs_userdir != null)) fs_userdir else Globals.BASEDIRNAME
        }

        /*
     * BaseGamedir
     * 
     * Called to find where to write a downloaded file
     */
        public fun BaseGamedir(): String {
            return if ((fs_gamedir != null)) fs_gamedir else Globals.BASEDIRNAME
        }

        /*
     * ExecAutoexec
     */
        public fun ExecAutoexec() {
            val dir = fs_userdir

            val name: String
            if (dir != null && dir.length() > 0) {
                name = dir + "/autoexec.cfg"
            } else {
                name = fs_basedir.string + '/' + Globals.BASEDIRNAME + "/autoexec.cfg"
            }

            val canthave = Defines.SFF_SUBDIR or Defines.SFF_HIDDEN or Defines.SFF_SYSTEM

            if (Sys.FindAll(name, 0, canthave) != null) {
                Cbuf.AddText("exec autoexec.cfg\n")
            }
        }

        /*
     * SetGamedir
     * 
     * Sets the gamedir and path to a different directory.
     */
        public fun SetGamedir(dir: String) {
            val next: searchpath_t

            if (dir.indexOf("..") != -1 || dir.indexOf("/") != -1 || dir.indexOf("\\") != -1 || dir.indexOf(":") != -1) {
                Com.Printf("Gamedir should be a single filename, not a path\n")
                return
            }

            //
            // free up any current game dir info
            //
            while (fs_searchpaths != fs_base_searchpaths) {
                if (fs_searchpaths!!.pack != null) {
                    try {
                        fs_searchpaths!!.pack!!.handle!!.close()
                    } catch (e: IOException) {
                        Com.DPrintf(e.getMessage() + '\n')
                    }

                    // clear the hashtable
                    fs_searchpaths!!.pack!!.files!!.clear()
                    fs_searchpaths!!.pack!!.files = null
                    fs_searchpaths!!.pack = null
                }
                next = fs_searchpaths!!.next
                fs_searchpaths = null
                fs_searchpaths = next
            }

            //
            // flush all data, so it will be forced to reload
            //
            if ((Globals.dedicated != null) && (Globals.dedicated.value == 0.0.toFloat()))
                Cbuf.AddText("vid_restart\nsnd_restart\n")

            fs_gamedir = fs_basedir.string + '/' + dir

            if (dir.equals(Globals.BASEDIRNAME) || (dir.length() == 0)) {
                Cvar.FullSet("gamedir", "", CVAR_SERVERINFO or CVAR_NOSET)
                Cvar.FullSet("game", "", CVAR_LATCH or CVAR_SERVERINFO)
            } else {
                Cvar.FullSet("gamedir", dir, CVAR_SERVERINFO or CVAR_NOSET)
                if (fs_cddir.string != null && fs_cddir.string.length() > 0)
                    AddGameDirectory(fs_cddir.string + '/' + dir)

                AddGameDirectory(fs_basedir.string + '/' + dir)
            }
        }

        /*
     * Link_f
     * 
     * Creates a filelink_t
     */
        public fun Link_f() {
            var entry: filelink_t? = null

            if (Cmd.Argc() != 3) {
                Com.Printf("USAGE: link <from> <to>\n")
                return
            }

            // see if the link already exists
            run {
                val it = fs_links.iterator()
                while (it.hasNext()) {
                    entry = it.next()

                    if (entry!!.from.equals(Cmd.Argv(1))) {
                        if (Cmd.Argv(2).length() < 1) {
                            // delete it
                            it.remove()
                            return
                        }
                        entry!!.to = String(Cmd.Argv(2))
                        return
                    }
                }
            }

            // create a new link if the <to> is not empty
            if (Cmd.Argv(2).length() > 0) {
                entry = filelink_t()
                entry!!.from = String(Cmd.Argv(1))
                entry!!.fromlength = entry!!.from.length()
                entry!!.to = String(Cmd.Argv(2))
                fs_links.add(entry)
            }
        }

        /*
     * ListFiles
     */
        public fun ListFiles(findname: String, musthave: Int, canthave: Int): Array<String> {
            var list = arrayOfNulls<String>(0)

            val files = Sys.FindAll(findname, musthave, canthave)

            if (files != null) {
                list = arrayOfNulls<String>(files!!.size())
                for (i in files!!.indices) {
                    list[i] = files!![i].getPath()
                }
            }

            return list
        }

        /*
     * Dir_f
     */
        public fun Dir_f() {
            var path: String? = null
            var findname: String? = null
            var wildcard = "*.*"
            val dirnames: Array<String>

            if (Cmd.Argc() != 1) {
                wildcard = Cmd.Argv(1)
            }

            while ((path = NextPath(path)) != null) {
                val tmp = findname

                findname = path!!.toInt() + '/' + wildcard.toInt()

                if (tmp != null)
                    tmp.replaceAll("\\\\", "/")

                Com.Printf("Directory of " + findname + '\n')
                Com.Printf("----\n")

                dirnames = ListFiles(findname, 0, 0)

                if (dirnames.size() != 0) {
                    var index = 0
                    for (i in dirnames.indices) {
                        if ((index = dirnames[i].lastIndexOf('/')) > 0) {
                            Com.Printf(dirnames[i].substring(index + 1, dirnames[i].length()) + '\n')
                        } else {
                            Com.Printf(dirnames[i].toInt() + '\n')
                        }
                    }
                }

                Com.Printf("\n")
            }
        }

        /*
     * Path_f
     */
        public fun Path_f() {

            var s: searchpath_t?
            var link: filelink_t

            Com.Printf("Current search path:\n")
            run {
                s = fs_searchpaths
                while (s != null) {
                    if (s == fs_base_searchpaths)
                        Com.Printf("----------\n")
                    if (s.pack != null)
                        Com.Printf(s.pack!!.filename + " (" + s.pack!!.numfiles + " files)\n")
                    else
                        Com.Printf(s.filename.toInt() + '\n')
                    s = s.next
                }
            }

            Com.Printf("\nLinks:\n")
            run {
                val it = fs_links.iterator()
                while (it.hasNext()) {
                    link = it.next()
                    Com.Printf(link.from + " : " + link.to + '\n')
                }
            }
        }

        /*
     * NextPath
     * 
     * Allows enumerating all of the directories in the search path
     */
        public fun NextPath(prevpath: String?): String? {
            var s: searchpath_t?
            var prev: String

            if (prevpath == null || prevpath.length() == 0)
                return fs_gamedir

            prev = fs_gamedir
            run {
                s = fs_searchpaths
                while (s != null) {
                    if (s.pack != null)
                        continue

                    if (prevpath == prev)
                        return s.filename

                    prev = s.filename
                    s = s.next
                }
            }

            return null
        }

        /*
     * InitFilesystem
     */
        public fun InitFilesystem() {
            Cmd.AddCommand("path", object : xcommand_t() {
                public fun execute() {
                    Path_f()
                }
            })
            Cmd.AddCommand("link", object : xcommand_t() {
                public fun execute() {
                    Link_f()
                }
            })
            Cmd.AddCommand("dir", object : xcommand_t() {
                public fun execute() {
                    Dir_f()
                }
            })

            fs_userdir = System.getProperty("user.home") + "/.lwjake2"
            FS.CreatePath(fs_userdir + "/")
            FS.AddGameDirectory(fs_userdir)

            //
            // basedir <path>
            // allows the game to run from outside the data tree
            //
            fs_basedir = Cvar.Get("basedir", ".", CVAR_NOSET)

            //
            // cddir <path>
            // Logically concatenates the cddir after the basedir for
            // allows the game to run from outside the data tree
            //

            setCDDir()

            //
            // start up with baseq2 by default
            //
            AddGameDirectory(fs_basedir.string + '/' + Globals.BASEDIRNAME)

            // any set gamedirs will be freed up to here
            markBaseSearchPaths()

            // check for game override
            checkOverride()
        }

        /**
         * set baseq2 directory
         */
        fun setCDDir() {
            fs_cddir = Cvar.Get("cddir", "", CVAR_ARCHIVE)
            if (fs_cddir.string.length() > 0)
                AddGameDirectory(fs_cddir.string + '/' + Globals.BASEDIRNAME)
        }

        /** Check for "+set game" override - Used to properly set gamedir  */
        fun checkOverride() {
            fs_gamedirvar = Cvar.Get("game", "", CVAR_LATCH or CVAR_SERVERINFO)

            if (fs_gamedirvar.string.length() > 0)
                SetGamedir(fs_gamedirvar.string)
        }

        fun markBaseSearchPaths() {
            // any set gamedirs will be freed up to here
            fs_base_searchpaths = fs_searchpaths
        }

        //	RAFAEL
        /*
     * Developer_searchpath
     */
        public fun Developer_searchpath(who: Int): Int {

            // PMM - warning removal
            //	 char *start;
            var s: searchpath_t?


            run {
                s = fs_searchpaths
                while (s != null) {
                    if (s.filename.indexOf("xatrix") != -1)
                        return 1

                    if (s.filename.indexOf("rogue") != -1)
                        return 2
                    s = s.next
                }
            }

            return 0
        }
    }
}