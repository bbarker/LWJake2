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
import lwjake2.Globals
import lwjake2.game.monsters.M_Player
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.MSG
import lwjake2.qcommon.SZ
import lwjake2.qcommon.cmd_function_t
import lwjake2.qcommon.xcommand_t
import lwjake2.server.SV_GAME
import lwjake2.util.Lib

import java.util.Arrays
import java.util.Comparator
import java.util.Vector

/**
 * Cmd
 */
public class Cmd {
    companion object {
        var List_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                var cmd = Cmd.cmd_functions
                var i = 0

                while (cmd != null) {
                    Com.Printf(cmd!!.name + '\n')
                    i++
                    cmd = cmd!!.next
                }
                Com.Printf(i + " commands\n")
            }
        }

        var Exec_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                if (Cmd.Argc() != 2) {
                    Com.Printf("exec <filename> : execute a script file\n")
                    return
                }

                var f: ByteArray? = null
                f = FS.LoadFile(Cmd.Argv(1))
                if (f == null) {
                    Com.Printf("couldn't exec " + Cmd.Argv(1) + "\n")
                    return
                }
                Com.Printf("execing " + Cmd.Argv(1) + "\n")

                Cbuf.InsertText(String(f))

                FS.FreeFile(f)
            }
        }

        var Echo_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                for (i in 1..Cmd.Argc() - 1) {
                    Com.Printf(Cmd.Argv(i) + " ")
                }
                Com.Printf("'\n")
            }
        }

        var Alias_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                var a: cmdalias_t? = null
                if (Cmd.Argc() == 1) {
                    Com.Printf("Current alias commands:\n")
                    run {
                        a = Globals.cmd_alias
                        while (a != null) {
                            Com.Printf(a!!.name + " : " + a!!.value)
                            a = a!!.next
                        }
                    }
                    return
                }

                val s = Cmd.Argv(1)
                if (s.length() > Defines.MAX_ALIAS_NAME) {
                    Com.Printf("Alias name is too long\n")
                    return
                }

                // if the alias already exists, reuse it
                run {
                    a = Globals.cmd_alias
                    while (a != null) {
                        if (s.equalsIgnoreCase(a!!.name)) {
                            a!!.value = null
                            break
                        }
                        a = a!!.next
                    }
                }

                if (a == null) {
                    a = cmdalias_t()
                    a!!.next = Globals.cmd_alias
                    Globals.cmd_alias = a
                }
                a!!.name = s

                // copy the rest of the command line
                var cmd = ""
                val c = Cmd.Argc()
                for (i in 2..c - 1) {
                    cmd = cmd.toInt() + Cmd.Argv(i).toInt()
                    if (i != (c - 1))
                        cmd = cmd + " "
                }
                cmd = cmd + "\n"

                a!!.value = cmd
            }
        }

        public var Wait_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Globals.cmd_wait = true
            }
        }

        public var cmd_functions: cmd_function_t? = null

        public var cmd_argc: Int = 0

        public var cmd_argv: Array<String> = arrayOfNulls(Defines.MAX_STRING_TOKENS)

        public var cmd_args: String

        public val ALIAS_LOOP_COUNT: Int = 16

        /**
         * Register our commands.
         */
        public fun Init() {

            Cmd.AddCommand("exec", Exec_f)
            Cmd.AddCommand("echo", Echo_f)
            Cmd.AddCommand("cmdlist", List_f)
            Cmd.AddCommand("alias", Alias_f)
            Cmd.AddCommand("wait", Wait_f)
        }

        private val expanded = CharArray(Defines.MAX_STRING_CHARS)

        private val temporary = CharArray(Defines.MAX_STRING_CHARS)

        public var PlayerSort: Comparator<Integer> = object : Comparator<Integer> {
            override fun compare(o1: Integer, o2: Integer): Int {

                val anum1 = GameBase.game.clients[o1].ps.stats[Defines.STAT_FRAGS]
                val bnum1 = GameBase.game.clients[o2].ps.stats[Defines.STAT_FRAGS]

                if (anum1 < bnum1)
                    return -1
                if (anum1 > bnum1)
                    return 1
                return 0
            }
        }

        /**
         * Cmd_MacroExpandString.
         */
        public fun MacroExpandString(text: CharArray, len: Int): CharArray? {
            var len = len
            var i: Int
            var j: Int
            var count: Int
            var inquote: Boolean

            var scan: CharArray

            var token: String
            inquote = false

            scan = text

            if (len >= Defines.MAX_STRING_CHARS) {
                Com.Printf("Line exceeded " + Defines.MAX_STRING_CHARS + " chars, discarded.\n")
                return null
            }

            count = 0

            run {
                i = 0
                while (i < len) {
                    if (scan[i] == '"')
                        inquote = !inquote

                    if (inquote)
                        continue // don't expand inside quotes

                    if (scan[i] != '$')
                        continue

                    // scan out the complete macro, without $
                    val ph = Com.ParseHelp(text, i + 1)
                    token = Com.Parse(ph)

                    if (ph.data == null)
                        continue

                    token = Cvar.VariableString(token)

                    j = token.length()

                    len += j

                    if (len >= Defines.MAX_STRING_CHARS) {
                        Com.Printf("Expanded line exceeded " + Defines.MAX_STRING_CHARS + " chars, discarded.\n")
                        return null
                    }

                    System.arraycopy(scan, 0, temporary, 0, i)
                    System.arraycopy(token.toCharArray(), 0, temporary, i, token.length())
                    System.arraycopy(ph.data, ph.index, temporary, i + j, len - ph.index - j)

                    System.arraycopy(temporary, 0, expanded, 0, 0)
                    scan = expanded
                    i--
                    if (++count == 100) {
                        Com.Printf("Macro expansion loop, discarded.\n")
                        return null
                    }
                    i++
                }
            }

            if (inquote) {
                Com.Printf("Line has unmatched quote, discarded.\n")
                return null
            }

            return scan
        }

        /**
         * Cmd_TokenizeString

         * Parses the given string into command line tokens. $Cvars will be expanded
         * unless they are in a quoted token.
         */
        public fun TokenizeString(text: CharArray?, macroExpand: Boolean) {
            var text = text
            val com_token: String

            cmd_argc = 0
            cmd_args = ""

            var len = Lib.strlen(text)

            // macro expand the text
            if (macroExpand)
                text = MacroExpandString(text, len)

            if (text == null)
                return

            len = Lib.strlen(text)

            val ph = Com.ParseHelp(text)

            while (true) {

                // skip whitespace up to a /n
                var c = ph.skipwhitestoeol()

                if (c == '\n') {
                    // a newline seperates commands in the buffer
                    c = ph.nextchar()
                    break
                }

                if (c == 0)
                    return

                // set cmd_args to everything after the first arg
                if (cmd_argc == 1) {
                    cmd_args = String(text, ph.index, len - ph.index)
                    cmd_args.trim()
                }

                com_token = Com.Parse(ph)

                if (ph.data == null)
                    return

                if (cmd_argc < Defines.MAX_STRING_TOKENS) {
                    cmd_argv[cmd_argc] = com_token
                    cmd_argc++
                }
            }
        }

        public fun AddCommand(cmd_name: String, function: xcommand_t) {
            var cmd: cmd_function_t?
            //Com.DPrintf("Cmd_AddCommand: " + cmd_name + "\n");
            // fail if the command is a variable name
            if ((Cvar.VariableString(cmd_name)).length() > 0) {
                Com.Printf("Cmd_AddCommand: " + cmd_name + " already defined as a var\n")
                return
            }

            // fail if the command already exists
            run {
                cmd = cmd_functions
                while (cmd != null) {
                    if (cmd_name.equals(cmd!!.name)) {
                        Com.Printf("Cmd_AddCommand: " + cmd_name + " already defined\n")
                        return
                    }
                    cmd = cmd!!.next
                }
            }

            cmd = cmd_function_t()
            cmd!!.name = cmd_name

            cmd!!.function = function
            cmd!!.next = cmd_functions
            cmd_functions = cmd
        }

        /**
         * Cmd_RemoveCommand
         */
        public fun RemoveCommand(cmd_name: String) {
            val cmd: cmd_function_t?
            var back: cmd_function_t? = null

            back = cmd = cmd_functions

            while (true) {

                if (cmd == null) {
                    Com.Printf("Cmd_RemoveCommand: " + cmd_name + " not added\n")
                    return
                }
                if (0 == Lib.strcmp(cmd_name, cmd!!.name)) {
                    if (cmd == cmd_functions)
                        cmd_functions = cmd!!.next
                    else
                        back!!.next = cmd!!.next
                    return
                }
                back = cmd
                cmd = cmd!!.next
            }
        }

        /**
         * Cmd_Exists
         */
        public fun Exists(cmd_name: String): Boolean {
            var cmd: cmd_function_t?

            run {
                cmd = cmd_functions
                while (cmd != null) {
                    if (cmd!!.name.equals(cmd_name))
                        return true
                    cmd = cmd!!.next
                }
            }

            return false
        }

        public fun Argc(): Int {
            return cmd_argc
        }

        public fun Argv(i: Int): String {
            if (i < 0 || i >= cmd_argc)
                return ""
            return cmd_argv[i]
        }

        public fun Args(): String {
            return String(cmd_args)
        }

        /**
         * Cmd_ExecuteString

         * A complete command line has been parsed, so try to execute it
         * FIXME: lookupnoadd the token to speed search?
         */
        public fun ExecuteString(text: String) {

            var cmd: cmd_function_t?
            var a: cmdalias_t?

            TokenizeString(text.toCharArray(), true)

            // execute the command line
            if (Argc() == 0)
                return  // no tokens

            // check functions
            run {
                cmd = cmd_functions
                while (cmd != null) {
                    if (cmd_argv[0].equalsIgnoreCase(cmd!!.name)) {
                        if (null == cmd!!.function) {
                            // forward to server command
                            Cmd.ExecuteString("cmd " + text)
                        } else {
                            cmd!!.function.execute()
                        }
                        return
                    }
                    cmd = cmd!!.next
                }
            }

            // check alias
            run {
                a = Globals.cmd_alias
                while (a != null) {

                    if (cmd_argv[0].equalsIgnoreCase(a!!.name)) {

                        if (++Globals.alias_count == ALIAS_LOOP_COUNT) {
                            Com.Printf("ALIAS_LOOP_COUNT\n")
                            return
                        }
                        Cbuf.InsertText(a!!.value)
                        return
                    }
                    a = a!!.next
                }
            }

            // check cvars
            if (Cvar.Command())
                return

            // send it as a server command if we are connected
            Cmd.ForwardToServer()
        }

        /**
         * Cmd_Give_f

         * Give items to a client.
         */
        public fun Give_f(ent: edict_t) {
            var name: String
            var it: gitem_t?
            val index: Int
            var i: Int
            val give_all: Boolean
            val it_ent: edict_t

            if (GameBase.deathmatch.value != 0 && GameBase.sv_cheats.value == 0) {
                SV_GAME.PF_cprintfhigh(ent, "You must run the server with '+set cheats 1' to enable this command.\n")
                return
            }

            name = Cmd.Args()

            if (0 == Lib.Q_stricmp(name, "all"))
                give_all = true
            else
                give_all = false

            if (give_all || 0 == Lib.Q_stricmp(Cmd.Argv(1), "health")) {
                if (Cmd.Argc() == 3)
                    ent.health = Lib.atoi(Cmd.Argv(2))
                else
                    ent.health = ent.max_health
                if (!give_all)
                    return
            }

            if (give_all || 0 == Lib.Q_stricmp(name, "weapons")) {
                run {
                    i = 1
                    while (i < GameBase.game.num_items) {
                        it = GameItemList.itemlist[i]
                        if (null == it!!.pickup)
                            continue
                        if (0 == (it!!.flags and Defines.IT_WEAPON))
                            continue
                        ent.client.pers.inventory[i] += 1
                        i++
                    }
                }
                if (!give_all)
                    return
            }

            if (give_all || 0 == Lib.Q_stricmp(name, "ammo")) {
                run {
                    i = 1
                    while (i < GameBase.game.num_items) {
                        it = GameItemList.itemlist[i]
                        if (null == it!!.pickup)
                            continue
                        if (0 == (it!!.flags and Defines.IT_AMMO))
                            continue
                        GameItems.Add_Ammo(ent, it, 1000)
                        i++
                    }
                }
                if (!give_all)
                    return
            }

            if (give_all || Lib.Q_stricmp(name, "armor") == 0) {
                val info: gitem_armor_t

                it = GameItems.FindItem("Jacket Armor")
                ent.client.pers.inventory[GameItems.ITEM_INDEX(it)] = 0

                it = GameItems.FindItem("Combat Armor")
                ent.client.pers.inventory[GameItems.ITEM_INDEX(it)] = 0

                it = GameItems.FindItem("Body Armor")
                info = it!!.info as gitem_armor_t
                ent.client.pers.inventory[GameItems.ITEM_INDEX(it)] = info.max_count

                if (!give_all)
                    return
            }

            if (give_all || Lib.Q_stricmp(name, "Power Shield") == 0) {
                it = GameItems.FindItem("Power Shield")
                it_ent = GameUtil.G_Spawn()
                it_ent.classname = it!!.classname
                GameItems.SpawnItem(it_ent, it)
                GameItems.Touch_Item(it_ent, ent, GameBase.dummyplane, null)
                if (it_ent.inuse)
                    GameUtil.G_FreeEdict(it_ent)

                if (!give_all)
                    return
            }

            if (give_all) {
                run {
                    i = 1
                    while (i < GameBase.game.num_items) {
                        it = GameItemList.itemlist[i]
                        if (it!!.pickup != null)
                            continue
                        if ((it!!.flags and (Defines.IT_ARMOR or Defines.IT_WEAPON or Defines.IT_AMMO)) != 0)
                            continue
                        ent.client.pers.inventory[i] = 1
                        i++
                    }
                }
                return
            }

            it = GameItems.FindItem(name)
            if (it == null) {
                name = Cmd.Argv(1)
                it = GameItems.FindItem(name)
                if (it == null) {
                    SV_GAME.PF_cprintf(ent, Defines.PRINT_HIGH, "unknown item\n")
                    return
                }
            }

            if (it!!.pickup == null) {
                SV_GAME.PF_cprintf(ent, Defines.PRINT_HIGH, "non-pickup item\n")
                return
            }

            index = GameItems.ITEM_INDEX(it)

            if ((it!!.flags and Defines.IT_AMMO) != 0) {
                if (Cmd.Argc() == 3)
                    ent.client.pers.inventory[index] = Lib.atoi(Cmd.Argv(2))
                else
                    ent.client.pers.inventory[index] += it!!.quantity
            } else {
                it_ent = GameUtil.G_Spawn()
                it_ent.classname = it!!.classname
                GameItems.SpawnItem(it_ent, it)
                GameItems.Touch_Item(it_ent, ent, GameBase.dummyplane, null)
                if (it_ent.inuse)
                    GameUtil.G_FreeEdict(it_ent)
            }
        }

        /**
         * Cmd_God_f

         * Sets client to godmode

         * argv(0) god
         */
        public fun God_f(ent: edict_t) {
            val msg: String

            if (GameBase.deathmatch.value != 0 && GameBase.sv_cheats.value == 0) {
                SV_GAME.PF_cprintfhigh(ent, "You must run the server with '+set cheats 1' to enable this command.\n")
                return
            }

            ent.flags = ent.flags xor Defines.FL_GODMODE
            if (0 == (ent.flags and Defines.FL_GODMODE))
                msg = "godmode OFF\n"
            else
                msg = "godmode ON\n"

            SV_GAME.PF_cprintf(ent, Defines.PRINT_HIGH, msg)
        }

        /**
         * Cmd_Notarget_f

         * Sets client to notarget

         * argv(0) notarget.
         */
        public fun Notarget_f(ent: edict_t) {
            val msg: String

            if (GameBase.deathmatch.value != 0 && GameBase.sv_cheats.value == 0) {
                SV_GAME.PF_cprintfhigh(ent, "You must run the server with '+set cheats 1' to enable this command.\n")
                return
            }

            ent.flags = ent.flags xor Defines.FL_NOTARGET
            if (0 == (ent.flags and Defines.FL_NOTARGET))
                msg = "notarget OFF\n"
            else
                msg = "notarget ON\n"

            SV_GAME.PF_cprintfhigh(ent, msg)
        }

        /**
         * Cmd_Noclip_f

         * argv(0) noclip.
         */
        public fun Noclip_f(ent: edict_t) {
            val msg: String

            if (GameBase.deathmatch.value != 0 && GameBase.sv_cheats.value == 0) {
                SV_GAME.PF_cprintfhigh(ent, "You must run the server with '+set cheats 1' to enable this command.\n")
                return
            }

            if (ent.movetype == Defines.MOVETYPE_NOCLIP) {
                ent.movetype = Defines.MOVETYPE_WALK
                msg = "noclip OFF\n"
            } else {
                ent.movetype = Defines.MOVETYPE_NOCLIP
                msg = "noclip ON\n"
            }

            SV_GAME.PF_cprintfhigh(ent, msg)
        }

        /**
         * Cmd_Use_f

         * Use an inventory item.
         */
        public fun Use_f(ent: edict_t) {
            val index: Int
            val it: gitem_t?
            val s: String

            s = Cmd.Args()

            it = GameItems.FindItem(s)
            Com.dprintln("using:" + s)
            if (it == null) {
                SV_GAME.PF_cprintfhigh(ent, "unknown item: " + s + "\n")
                return
            }
            if (it!!.use == null) {
                SV_GAME.PF_cprintfhigh(ent, "Item is not usable.\n")
                return
            }
            index = GameItems.ITEM_INDEX(it)
            if (0 == ent.client.pers.inventory[index]) {
                SV_GAME.PF_cprintfhigh(ent, "Out of item: " + s + "\n")
                return
            }

            it!!.use.use(ent, it)
        }

        /**
         * Cmd_Drop_f

         * Drop an inventory item.
         */
        public fun Drop_f(ent: edict_t) {
            val index: Int
            val it: gitem_t?
            val s: String

            s = Cmd.Args()
            it = GameItems.FindItem(s)
            if (it == null) {
                SV_GAME.PF_cprintfhigh(ent, "unknown item: " + s + "\n")
                return
            }
            if (it!!.drop == null) {
                SV_GAME.PF_cprintf(ent, Defines.PRINT_HIGH, "Item is not dropable.\n")
                return
            }
            index = GameItems.ITEM_INDEX(it)
            if (0 == ent.client.pers.inventory[index]) {
                SV_GAME.PF_cprintfhigh(ent, "Out of item: " + s + "\n")
                return
            }

            it!!.drop.drop(ent, it)
        }

        /**
         * Cmd_Inven_f.
         */
        public fun Inven_f(ent: edict_t) {
            var i: Int
            val cl: gclient_t

            cl = ent.client

            cl.showscores = false
            cl.showhelp = false

            if (cl.showinventory) {
                cl.showinventory = false
                return
            }

            cl.showinventory = true

            GameBase.gi.WriteByte(Defines.svc_inventory)
            run {
                i = 0
                while (i < Defines.MAX_ITEMS) {
                    GameBase.gi.WriteShort(cl.pers.inventory[i])
                    i++
                }
            }
            GameBase.gi.unicast(ent, true)
        }

        /**
         * Cmd_InvUse_f.
         */
        public fun InvUse_f(ent: edict_t) {
            val it: gitem_t

            Cmd.ValidateSelectedItem(ent)

            if (ent.client.pers.selected_item == -1) {
                SV_GAME.PF_cprintfhigh(ent, "No item to use.\n")
                return
            }

            it = GameItemList.itemlist[ent.client.pers.selected_item]
            if (it.use == null) {
                SV_GAME.PF_cprintfhigh(ent, "Item is not usable.\n")
                return
            }
            it.use.use(ent, it)
        }

        /**
         * Cmd_WeapPrev_f.
         */
        public fun WeapPrev_f(ent: edict_t) {
            val cl: gclient_t
            var i: Int
            var index: Int
            var it: gitem_t
            val selected_weapon: Int

            cl = ent.client

            if (cl.pers.weapon == null)
                return

            selected_weapon = GameItems.ITEM_INDEX(cl.pers.weapon)

            // scan for the next valid one
            run {
                i = 1
                while (i <= Defines.MAX_ITEMS) {
                    index = (selected_weapon + i) % Defines.MAX_ITEMS
                    if (0 == cl.pers.inventory[index])
                        continue

                    it = GameItemList.itemlist[index]
                    if (it.use == null)
                        continue

                    if (0 == (it.flags and Defines.IT_WEAPON))
                        continue
                    it.use.use(ent, it)
                    if (cl.pers.weapon == it)
                        return  // successful
                    i++
                }
            }
        }

        /**
         * Cmd_WeapNext_f.
         */
        public fun WeapNext_f(ent: edict_t) {
            val cl: gclient_t
            var i: Int
            var index: Int
            var it: gitem_t
            val selected_weapon: Int

            cl = ent.client

            if (null == cl.pers.weapon)
                return

            selected_weapon = GameItems.ITEM_INDEX(cl.pers.weapon)

            // scan for the next valid one
            run {
                i = 1
                while (i <= Defines.MAX_ITEMS) {
                    index = (selected_weapon + Defines.MAX_ITEMS - i) % Defines.MAX_ITEMS
                    //bugfix rst
                    if (index == 0)
                        index++
                    if (0 == cl.pers.inventory[index])
                        continue
                    it = GameItemList.itemlist[index]
                    if (null == it.use)
                        continue
                    if (0 == (it.flags and Defines.IT_WEAPON))
                        continue
                    it.use.use(ent, it)
                    if (cl.pers.weapon == it)
                        return  // successful
                    i++
                }
            }
        }

        /**
         * Cmd_WeapLast_f.
         */
        public fun WeapLast_f(ent: edict_t) {
            val cl: gclient_t
            val index: Int
            val it: gitem_t

            cl = ent.client

            if (null == cl.pers.weapon || null == cl.pers.lastweapon)
                return

            index = GameItems.ITEM_INDEX(cl.pers.lastweapon)
            if (0 == cl.pers.inventory[index])
                return
            it = GameItemList.itemlist[index]
            if (null == it.use)
                return
            if (0 == (it.flags and Defines.IT_WEAPON))
                return
            it.use.use(ent, it)
        }

        /**
         * Cmd_InvDrop_f
         */
        public fun InvDrop_f(ent: edict_t) {
            val it: gitem_t

            Cmd.ValidateSelectedItem(ent)

            if (ent.client.pers.selected_item == -1) {
                SV_GAME.PF_cprintfhigh(ent, "No item to drop.\n")
                return
            }

            it = GameItemList.itemlist[ent.client.pers.selected_item]
            if (it.drop == null) {
                SV_GAME.PF_cprintfhigh(ent, "Item is not dropable.\n")
                return
            }
            it.drop.drop(ent, it)
        }

        /**
         * Cmd_Score_f

         * Display the scoreboard.

         */
        public fun Score_f(ent: edict_t) {
            ent.client.showinventory = false
            ent.client.showhelp = false

            if (0 == GameBase.deathmatch.value && 0 == GameBase.coop.value)
                return

            if (ent.client.showscores) {
                ent.client.showscores = false
                return
            }

            ent.client.showscores = true
            PlayerHud.DeathmatchScoreboard(ent)
        }

        /**
         * Cmd_Help_f

         * Display the current help message.

         */
        public fun Help_f(ent: edict_t) {
            // this is for backwards compatability
            if (GameBase.deathmatch.value != 0) {
                Score_f(ent)
                return
            }

            ent.client.showinventory = false
            ent.client.showscores = false

            if (ent.client.showhelp && (ent.client.pers.game_helpchanged == GameBase.game.helpchanged)) {
                ent.client.showhelp = false
                return
            }

            ent.client.showhelp = true
            ent.client.pers.helpchanged = 0
            PlayerHud.HelpComputer(ent)
        }

        /**
         * Cmd_Kill_f
         */
        public fun Kill_f(ent: edict_t) {
            if ((GameBase.level.time - ent.client.respawn_time) < 5)
                return
            ent.flags = ent.flags and Defines.FL_GODMODE.inv()
            ent.health = 0
            GameBase.meansOfDeath = Defines.MOD_SUICIDE
            PlayerClient.player_die.die(ent, ent, ent, 100000, Globals.vec3_origin)
        }

        /**
         * Cmd_PutAway_f
         */
        public fun PutAway_f(ent: edict_t) {
            ent.client.showscores = false
            ent.client.showhelp = false
            ent.client.showinventory = false
        }

        /**
         * Cmd_Players_f
         */
        public fun Players_f(ent: edict_t) {
            var i: Int
            var count: Int
            var small: String
            var large: String

            val index = arrayOfNulls<Integer>(256)

            count = 0
            run {
                i = 0
                while (i < GameBase.maxclients.value) {
                    if (GameBase.game.clients[i].pers.connected) {
                        index[count] = Integer(i)
                        count++
                    }
                    i++
                }
            }

            // sort by frags
            Arrays.sort<Integer>(index, 0, count - 1, Cmd.PlayerSort)

            // print information
            large = ""

            run {
                i = 0
                while (i < count) {
                    small = GameBase.game.clients[index[i].intValue()].ps.stats[Defines.STAT_FRAGS] + " " + GameBase.game.clients[index[i].intValue()].pers.netname + "\n"

                    if (small.length() + large.length() > 1024 - 100) {
                        // can't print all of them in one packet
                        large += "...\n"
                        break
                    }
                    large += small
                    i++
                }
            }

            SV_GAME.PF_cprintfhigh(ent, large + "\n" + count + " players\n")
        }

        /**
         * Cmd_Wave_f
         */
        public fun Wave_f(ent: edict_t) {
            val i: Int

            i = Lib.atoi(Cmd.Argv(1))

            // can't wave when ducked
            if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0)
                return

            if (ent.client.anim_priority > Defines.ANIM_WAVE)
                return

            ent.client.anim_priority = Defines.ANIM_WAVE

            when (i) {
                0 -> {
                    SV_GAME.PF_cprintfhigh(ent, "flipoff\n")
                    ent.s.frame = M_Player.FRAME_flip01 - 1
                    ent.client.anim_end = M_Player.FRAME_flip12
                }
                1 -> {
                    SV_GAME.PF_cprintfhigh(ent, "salute\n")
                    ent.s.frame = M_Player.FRAME_salute01 - 1
                    ent.client.anim_end = M_Player.FRAME_salute11
                }
                2 -> {
                    SV_GAME.PF_cprintfhigh(ent, "taunt\n")
                    ent.s.frame = M_Player.FRAME_taunt01 - 1
                    ent.client.anim_end = M_Player.FRAME_taunt17
                }
                3 -> {
                    SV_GAME.PF_cprintfhigh(ent, "wave\n")
                    ent.s.frame = M_Player.FRAME_wave01 - 1
                    ent.client.anim_end = M_Player.FRAME_wave11
                }
                4, else -> {
                SV_GAME.PF_cprintfhigh(ent, "point\n")
                ent.s.frame = M_Player.FRAME_point01 - 1
                ent.client.anim_end = M_Player.FRAME_point12
            }
            }
        }

        /**
         * Command to print the players own position.
         */
        public fun ShowPosition_f(ent: edict_t) {
            SV_GAME.PF_cprintfhigh(ent, "pos=" + Lib.vtofsbeaty(ent.s.origin) + "\n")
        }

        /**
         * Cmd_Say_f
         */
        public fun Say_f(ent: edict_t, team: Boolean, arg0: Boolean) {
            var team = team

            var i: Int
            var j: Int
            var other: edict_t
            var text: String
            val cl: gclient_t

            if (Cmd.Argc() < 2 && !arg0)
                return

            if (0 == ((GameBase.dmflags.value) as Int and (Defines.DF_MODELTEAMS or Defines.DF_SKINTEAMS)))
                team = false

            if (team)
                text = "(" + ent.client.pers.netname + "): "
            else
                text = "" + ent.client.pers.netname + ": "

            if (arg0) {
                text += Cmd.Argv(0)
                text += " "
                text += Cmd.Args()
            } else {
                if (Cmd.Args().startsWith("\""))
                    text += Cmd.Args().substring(1, Cmd.Args().length() - 1)
                else
                    text += Cmd.Args()
            }

            // don't let text be too long for malicious reasons
            if (text.length() > 150)
            //text[150] = 0;
                text = text.substring(0, 150)

            text += "\n"

            if (GameBase.flood_msgs.value != 0) {
                cl = ent.client

                if (GameBase.level.time < cl.flood_locktill) {
                    SV_GAME.PF_cprintfhigh(ent, "You can't talk for " + (cl.flood_locktill - GameBase.level.time) as Int + " more seconds\n")
                    return
                }
                i = (cl.flood_whenhead - GameBase.flood_msgs.value + 1) as Int
                if (i < 0)
                    i = (10) + i
                if (cl.flood_when[i] != 0 && GameBase.level.time - cl.flood_when[i] < GameBase.flood_persecond.value) {
                    cl.flood_locktill = GameBase.level.time + GameBase.flood_waitdelay.value
                    SV_GAME.PF_cprintf(ent, Defines.PRINT_CHAT, "Flood protection:  You can't talk for " + GameBase.flood_waitdelay.value as Int + " seconds.\n")
                    return
                }

                cl.flood_whenhead = (cl.flood_whenhead + 1) % 10
                cl.flood_when[cl.flood_whenhead] = GameBase.level.time
            }

            if (Globals.dedicated.value != 0)
                SV_GAME.PF_cprintf(null, Defines.PRINT_CHAT, "" + text + "")

            run {
                j = 1
                while (j <= GameBase.game.maxclients) {
                    other = GameBase.g_edicts[j]
                    if (!other.inuse)
                        continue
                    if (other.client == null)
                        continue
                    if (team) {
                        if (!GameUtil.OnSameTeam(ent, other))
                            continue
                    }
                    SV_GAME.PF_cprintf(other, Defines.PRINT_CHAT, "" + text + "")
                    j++
                }
            }

        }

        /**
         * Returns the playerlist. TODO: The list is badly formatted at the moment.
         */
        public fun PlayerList_f(ent: edict_t) {
            var i: Int
            var st: String
            var text: String
            var e2: edict_t

            // connect time, ping, score, name
            text = ""

            run {
                i = 0
                while (i < GameBase.maxclients.value) {
                    e2 = GameBase.g_edicts[1 + i]
                    if (!e2.inuse)
                        continue

                    st = "" + (GameBase.level.framenum - e2.client.resp.enterframe) / 600 + ":" + ((GameBase.level.framenum - e2.client.resp.enterframe) % 600) / 10 + " " + e2.client.ping + " " + e2.client.resp.score + " " + e2.client.pers.netname + " " + (if (e2.client.resp.spectator) " (spectator)" else "") + "\n"

                    if (text.length() + st.length() > 1024 - 50) {
                        text += "And more...\n"
                        SV_GAME.PF_cprintfhigh(ent, "" + text + "")
                        return
                    }
                    text += st
                    i++
                }
            }
            SV_GAME.PF_cprintfhigh(ent, text)
        }

        /**
         * Adds the current command line as a clc_stringcmd to the client message.
         * things like godmode, noclip, etc, are commands directed to the server, so
         * when they are typed in at the console, they will need to be forwarded.
         */
        public fun ForwardToServer() {
            val cmd: String

            cmd = Cmd.Argv(0)
            if (Globals.cls.state <= Defines.ca_connected || cmd.charAt(0) == '-' || cmd.charAt(0) == '+') {
                Com.Printf("Unknown command \"" + cmd + "\"\n")
                return
            }

            MSG.WriteByte(Globals.cls.netchan.message, Defines.clc_stringcmd)
            SZ.Print(Globals.cls.netchan.message, cmd)
            if (Cmd.Argc() > 1) {
                SZ.Print(Globals.cls.netchan.message, " ")
                SZ.Print(Globals.cls.netchan.message, Cmd.Args())
            }
        }

        /**
         * Cmd_CompleteCommand.
         */
        public fun CompleteCommand(partial: String): Vector<String> {
            val cmds = Vector<String>()

            // check for match
            run {
                var cmd = cmd_functions
                while (cmd != null) {
                    if (cmd!!.name.startsWith(partial))
                        cmds.add(cmd!!.name)
                    cmd = cmd!!.next
                }
            }
            run {
                var a = Globals.cmd_alias
                while (a != null) {
                    if (a!!.name.startsWith(partial))
                        cmds.add(a!!.name)
                    a = a!!.next
                }
            }

            return cmds
        }

        /**
         * Processes the commands the player enters in the quake console.
         */
        public fun ClientCommand(ent: edict_t) {
            val cmd: String

            if (ent.client == null)
                return  // not fully in game yet

            cmd = GameBase.gi.argv(0).toLowerCase()

            if (cmd.equals("players")) {
                Players_f(ent)
                return
            }
            if (cmd.equals("say")) {
                Say_f(ent, false, false)
                return
            }
            if (cmd.equals("say_team")) {
                Say_f(ent, true, false)
                return
            }
            if (cmd.equals("score")) {
                Score_f(ent)
                return
            }
            if (cmd.equals("help")) {
                Help_f(ent)
                return
            }

            if (GameBase.level.intermissiontime != 0)
                return

            if (cmd.equals("use"))
                Use_f(ent)
            else if (cmd.equals("drop"))
                Drop_f(ent)
            else if (cmd.equals("give"))
                Give_f(ent)
            else if (cmd.equals("god"))
                God_f(ent)
            else if (cmd.equals("notarget"))
                Notarget_f(ent)
            else if (cmd.equals("noclip"))
                Noclip_f(ent)
            else if (cmd.equals("inven"))
                Inven_f(ent)
            else if (cmd.equals("invnext"))
                GameItems.SelectNextItem(ent, -1)
            else if (cmd.equals("invprev"))
                GameItems.SelectPrevItem(ent, -1)
            else if (cmd.equals("invnextw"))
                GameItems.SelectNextItem(ent, Defines.IT_WEAPON)
            else if (cmd.equals("invprevw"))
                GameItems.SelectPrevItem(ent, Defines.IT_WEAPON)
            else if (cmd.equals("invnextp"))
                GameItems.SelectNextItem(ent, Defines.IT_POWERUP)
            else if (cmd.equals("invprevp"))
                GameItems.SelectPrevItem(ent, Defines.IT_POWERUP)
            else if (cmd.equals("invuse"))
                InvUse_f(ent)
            else if (cmd.equals("invdrop"))
                InvDrop_f(ent)
            else if (cmd.equals("weapprev"))
                WeapPrev_f(ent)
            else if (cmd.equals("weapnext"))
                WeapNext_f(ent)
            else if (cmd.equals("weaplast"))
                WeapLast_f(ent)
            else if (cmd.equals("kill"))
                Kill_f(ent)
            else if (cmd.equals("putaway"))
                PutAway_f(ent)
            else if (cmd.equals("wave"))
                Wave_f(ent)
            else if (cmd.equals("playerlist"))
                PlayerList_f(ent)
            else if (cmd.equals("showposition"))
                ShowPosition_f(ent)
            else
            // anything that doesn't match a command will be a chat
                Say_f(ent, false, true)
        }

        public fun ValidateSelectedItem(ent: edict_t) {
            val cl = ent.client

            if (cl.pers.inventory[cl.pers.selected_item] != 0)
                return  // valid

            GameItems.SelectNextItem(ent, -1)
        }
    }
}