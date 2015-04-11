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

import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.cvar_t
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.netadr_t
import lwjake2.qcommon.xcommand_t
import lwjake2.sound.S
import lwjake2.sys.NET
import lwjake2.sys.Sys
import lwjake2.sys.Timer
import lwjake2.util.Lib
import lwjake2.util.Math3D
import lwjake2.util.QuakeFile

import java.awt.Dimension
import java.io.RandomAccessFile
import java.util.Arrays
import java.util.Comparator

/**
 * Menu


 */

abstract class keyfunc_t {
    abstract fun execute(key: Int): String
}

public class Menu : Key() {

    public class menulayer_t {
        var draw: xcommand_t

        var key: keyfunc_t
    }

    class menuframework_s {
        var x: Int = 0
        var y: Int = 0

        var cursor: Int = 0

        var nitems: Int = 0

        var nslots: Int = 0

        var items = arrayOfNulls<menucommon_s>(64)

        var statusbar: String

        //void (*cursordraw)( struct _tag_menuframework *m );
        var cursordraw: mcallback? = null

    }

    abstract class mcallback {
        public abstract fun execute(self: Object)
    }

    class menucommon_s {
        var type: Int = 0

        var name = ""

        var x: Int = 0
        var y: Int = 0

        var parent: menuframework_s

        var cursor_offset: Int = 0

        var localdata = intArray(0, 0, 0, 0)

        var flags: Int = 0

        var n = -1 //position in an array.

        var statusbar: String? = null

        var callback: mcallback

        var statusbarfunc: mcallback? = null

        var ownerdraw: mcallback

        var cursordraw: mcallback? = null
    }

    class menufield_s : menucommon_s() {
        //char buffer[80];
        var buffer: StringBuffer //allow deletion.

        var cursor: Int = 0

        var length: Int = 0

        var visible_length: Int = 0

        var visible_offset: Int = 0
    }

    class menuslider_s : menucommon_s() {

        var minvalue: Float = 0.toFloat()

        var maxvalue: Float = 0.toFloat()

        var curvalue: Float = 0.toFloat()

        var range: Float = 0.toFloat()
    }

    class menulist_s : menucommon_s() {
        var curvalue: Int = 0

        var itemnames: Array<String>? = null
    }

    class menuaction_s : menucommon_s()

    class menuseparator_s : menucommon_s()

    var keys_cursor: Int = 0

    class playermodelinfo_s {
        var nskins: Int = 0

        var skindisplaynames: Array<String>? = null

        //char displayname[MAX_DISPLAYNAME];
        var displayname: String

        //char directory[MAX_QPATH];
        var directory: String
    }

    companion object {

        var m_main_cursor: Int = 0

        val NUM_CURSOR_FRAMES = 15

        val menu_in_sound = "misc/menu1.wav"

        val menu_move_sound = "misc/menu2.wav"

        val menu_out_sound = "misc/menu3.wav"

        var m_entersound: Boolean = false // play after drawing a frame, so caching

        // won't disrupt the sound

        var m_drawfunc: xcommand_t? = null

        var m_keyfunc: keyfunc_t? = null

        //	  =============================================================================
        /* Support Routines */

        public val MAX_MENU_DEPTH: Int = 8

        public var m_layers: Array<menulayer_t> = arrayOfNulls(MAX_MENU_DEPTH)

        public var m_menudepth: Int = 0

        fun Banner(name: String) {
            val dim = Dimension()
            Globals.re.DrawGetPicSize(dim, name)

            Globals.re.DrawPic(viddef.width / 2 - dim.width / 2, viddef.height / 2 - 110, name)
        }

        fun PushMenu(draw: xcommand_t, key: keyfunc_t) {
            //, String(*key)
            // (int k) ) {
            var i: Int

            if (Cvar.VariableValue("maxclients") == 1 && Globals.server_state != 0)
                Cvar.Set("paused", "1")

            // if this menu is already present, drop back to that level
            // to avoid stacking menus by hotkeys
            run {
                i = 0
                while (i < m_menudepth) {
                    if (m_layers[i].draw == draw && m_layers[i].key == key) {
                        m_menudepth = i
                    }
                    i++
                }
            }

            if (i == m_menudepth) {
                if (m_menudepth >= MAX_MENU_DEPTH)
                    Com.Error(ERR_FATAL, "PushMenu: MAX_MENU_DEPTH")

                m_layers[m_menudepth].draw = draw//m_drawfunc;
                m_layers[m_menudepth].key = key//m_keyfunc;     
            }
            m_menudepth++
            m_drawfunc = draw
            m_keyfunc = key

            m_entersound = true

            cls.key_dest = key_menu
        }

        fun ForceMenuOff() {
            m_drawfunc = null
            m_keyfunc = null
            cls.key_dest = key_game
            m_menudepth = 0
            Key.ClearStates()
            Cvar.Set("paused", "0")
        }

        fun PopMenu() {
            S.StartLocalSound(menu_out_sound)
            m_menudepth--
            if (m_menudepth < 0)
                Com.Error(ERR_FATAL, "PopMenu: depth < 1")

            if (0 < m_menudepth) {
                m_drawfunc = m_layers[m_menudepth - 1].draw
                m_keyfunc = m_layers[m_menudepth - 1].key
            }

            if (0 == m_menudepth)
                ForceMenuOff()


        }

        fun Default_MenuKey(m: menuframework_s?, key: Int): String? {
            var sound: String? = null
            val item: menucommon_s

            if (m != null) {
                if ((item = (Menu_ItemAtCursor(m) as menucommon_s)) != null) {
                    if (item.type == MTYPE_FIELD) {
                        if (Field_Key(item as menufield_s, key))
                            return null
                    }
                }
            }

            when (key) {
                K_ESCAPE -> {
                    PopMenu()
                    return menu_out_sound
                }
                K_KP_UPARROW, K_UPARROW -> if (m != null) {
                    m.cursor--
                    Menu_AdjustCursor(m, -1)
                    sound = menu_move_sound
                }
                K_TAB -> if (m != null) {
                    m.cursor++
                    Menu_AdjustCursor(m, 1)
                    sound = menu_move_sound
                }
                K_KP_DOWNARROW, K_DOWNARROW -> if (m != null) {
                    m.cursor++
                    Menu_AdjustCursor(m, 1)
                    sound = menu_move_sound
                }
                K_KP_LEFTARROW, K_LEFTARROW -> if (m != null) {
                    Menu_SlideItem(m, -1)
                    sound = menu_move_sound
                }
                K_KP_RIGHTARROW, K_RIGHTARROW -> if (m != null) {
                    Menu_SlideItem(m, 1)
                    sound = menu_move_sound
                }

                K_MOUSE1, K_MOUSE2, K_MOUSE3, K_JOY1, K_JOY2, K_JOY3, K_JOY4, /*
         * case K_AUX1 : case K_AUX2 : case K_AUX3 : case K_AUX4 : case K_AUX5 :
         * case K_AUX6 : case K_AUX7 : case K_AUX8 : case K_AUX9 : case K_AUX10 :
         * case K_AUX11 : case K_AUX12 : case K_AUX13 : case K_AUX14 : case
         * K_AUX15 : case K_AUX16 : case K_AUX17 : case K_AUX18 : case K_AUX19 :
         * case K_AUX20 : case K_AUX21 : case K_AUX22 : case K_AUX23 : case
         * K_AUX24 : case K_AUX25 : case K_AUX26 : case K_AUX27 : case K_AUX28 :
         * case K_AUX29 : case K_AUX30 : case K_AUX31 : case K_AUX32 :
         */
                K_KP_ENTER, K_ENTER -> {
                    if (m != null)
                        Menu_SelectItem(m)
                    sound = menu_move_sound
                }
            }

            return sound
        }

        /*
     * ================ DrawCharacter
     * 
     * Draws one solid graphics character cx and cy are in 320*240 coordinates,
     * and will be centered on higher res screens. ================
     */
        public fun DrawCharacter(cx: Int, cy: Int, num: Int) {
            re.DrawChar(cx + ((viddef.width - 320) shr 1), cy + ((viddef.height - 240) shr 1), num)
        }

        public fun Print(cx: Int, cy: Int, str: String) {
            var cx = cx
            //while (*str)
            for (n in 0..str.length() - 1) {
                DrawCharacter(cx, cy, str.charAt(n) + 128)
                //str++;
                cx += 8
            }
        }

        public fun PrintWhite(cx: Int, cy: Int, str: String) {
            var cx = cx
            for (n in 0..str.length() - 1) {
                DrawCharacter(cx, cy, str.charAt(n))
                //str++;
                cx += 8
            }
        }

        public fun DrawPic(x: Int, y: Int, pic: String) {
            re.DrawPic(x + ((viddef.width - 320) shr 1), y + ((viddef.height - 240) shr 1), pic)
        }

        /*
     * ============= DrawCursor
     * 
     * Draws an animating cursor with the point at x,y. The pic will extend to
     * the left of x, and both above and below y. =============
     */
        var cached: Boolean = false

        fun DrawCursor(x: Int, y: Int, f: Int) {
            var f = f
            //char cursorname[80];
            var cursorname: String

            assert((f >= 0), "negative time and cursor bug")

            f = Math.abs(f)

            if (!cached) {
                var i: Int

                run {
                    i = 0
                    while (i < NUM_CURSOR_FRAMES) {
                        cursorname = "m_cursor" + i

                        re.RegisterPic(cursorname)
                        i++
                    }
                }
                cached = true
            }

            cursorname = "m_cursor" + f
            re.DrawPic(x, y, cursorname)
        }

        public fun DrawTextBox(x: Int, y: Int, width: Int, lines: Int) {
            var width = width
            var cx: Int
            var cy: Int
            var n: Int

            // draw left side
            cx = x
            cy = y
            DrawCharacter(cx, cy, 1)

            run {
                n = 0
                while (n < lines) {
                    cy += 8
                    DrawCharacter(cx, cy, 4)
                    n++
                }
            }
            DrawCharacter(cx, cy + 8, 7)

            // draw middle
            cx += 8
            while (width > 0) {
                cy = y
                DrawCharacter(cx, cy, 2)

                run {
                    n = 0
                    while (n < lines) {
                        cy += 8
                        DrawCharacter(cx, cy, 5)
                        n++
                    }
                }
                DrawCharacter(cx, cy + 8, 8)

                width -= 1
                cx += 8
            }

            // draw right side
            cy = y
            DrawCharacter(cx, cy, 3)
            run {
                n = 0
                while (n < lines) {
                    cy += 8
                    DrawCharacter(cx, cy, 6)
                    n++

                }
            }
            DrawCharacter(cx, cy + 8, 9)

        }

        /*
     * =======================================================================
     * 
     * MAIN MENU
     * 
     * =======================================================================
     */
        val MAIN_ITEMS = 5

        var Main_Draw: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Main_Draw()
            }
        }

        fun Main_Draw() {
            var i: Int
            var w: Int
            var h: Int
            val ystart: Int
            val xoffset: Int
            var widest = -1
            val litname: String
            val names = array<String>("m_main_game", "m_main_multiplayer", "m_main_options", "m_main_video", "m_main_quit")
            val dim = Dimension()

            run {
                i = 0
                while (i < names.size()) {
                    Globals.re.DrawGetPicSize(dim, names[i])
                    w = dim.width
                    h = dim.height

                    if (w > widest)
                        widest = w
                    i++
                }
            }

            ystart = (Globals.viddef.height / 2 - 110)
            xoffset = (Globals.viddef.width - widest + 70) / 2

            run {
                i = 0
                while (i < names.size()) {
                    if (i != m_main_cursor)
                        Globals.re.DrawPic(xoffset, ystart + i * 40 + 13, names[i])
                    i++
                }
            }

            //strcat(litname, "_sel");
            litname = names[m_main_cursor] + "_sel"
            Globals.re.DrawPic(xoffset, ystart + m_main_cursor * 40 + 13, litname)

            DrawCursor(xoffset - 25, ystart + m_main_cursor * 40 + 11, ((Globals.cls.realtime / 100)) as Int % NUM_CURSOR_FRAMES)

            Globals.re.DrawGetPicSize(dim, "m_main_plaque")
            w = dim.width
            h = dim.height
            Globals.re.DrawPic(xoffset - 30 - w, ystart, "m_main_plaque")

            Globals.re.DrawPic(xoffset - 30 - w, ystart + h + 5, "m_main_logo")
        }

        var Main_Key: keyfunc_t = object : keyfunc_t() {
            public fun execute(key: Int): String {
                return Main_Key(key)
            }
        }

        fun Main_Key(key: Int): String? {
            val sound = menu_move_sound

            when (key) {
                Key.K_ESCAPE -> PopMenu()

                Key.K_KP_DOWNARROW, Key.K_DOWNARROW -> {
                    if (++m_main_cursor >= MAIN_ITEMS)
                        m_main_cursor = 0
                    return sound
                }

                Key.K_KP_UPARROW, Key.K_UPARROW -> {
                    if (--m_main_cursor < 0)
                        m_main_cursor = MAIN_ITEMS - 1
                    return sound
                }

                Key.K_KP_ENTER, Key.K_ENTER -> {
                    m_entersound = true

                    when (m_main_cursor) {
                        0 -> Menu_Game_f()

                        1 -> Menu_Multiplayer_f()

                        2 -> Menu_Options_f()

                        3 -> Menu_Video_f()

                        4 -> Menu_Quit_f()
                    }
                }
            }

            return null
        }

        var Menu_Main: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Main_f()
            }
        }

        fun Menu_Main_f() {
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Main_Draw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Main_Key(key)
                }
            })
        }

        /*
     * =======================================================================
     * 
     * MULTIPLAYER MENU
     * 
     * =======================================================================
     */
        var s_multiplayer_menu = menuframework_s()

        var s_join_network_server_action = menuaction_s()

        var s_start_network_server_action = menuaction_s()

        var s_player_setup_action = menuaction_s()

        fun Multiplayer_MenuDraw() {
            Banner("m_banner_multiplayer")

            Menu_AdjustCursor(s_multiplayer_menu, 1)
            Menu_Draw(s_multiplayer_menu)
        }

        fun PlayerSetupFunc(unused: Object) {
            Menu_PlayerConfig_f()
        }

        fun JoinNetworkServerFunc(unused: Object) {
            Menu_JoinServer_f()
        }

        fun StartNetworkServerFunc(unused: Object) {
            Menu_StartServer_f()
        }

        fun Multiplayer_MenuInit() {
            s_multiplayer_menu.x = (viddef.width * 0.50.toFloat() - 64) as Int
            s_multiplayer_menu.nitems = 0

            s_join_network_server_action.type = MTYPE_ACTION
            s_join_network_server_action.flags = QMF_LEFT_JUSTIFY
            s_join_network_server_action.x = 0
            s_join_network_server_action.y = 0
            s_join_network_server_action.name = " join network server"
            s_join_network_server_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    JoinNetworkServerFunc(o)
                }
            }

            s_start_network_server_action.type = MTYPE_ACTION
            s_start_network_server_action.flags = QMF_LEFT_JUSTIFY
            s_start_network_server_action.x = 0
            s_start_network_server_action.y = 10
            s_start_network_server_action.name = " start network server"
            s_start_network_server_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    StartNetworkServerFunc(o)
                }
            }

            s_player_setup_action.type = MTYPE_ACTION
            s_player_setup_action.flags = QMF_LEFT_JUSTIFY
            s_player_setup_action.x = 0
            s_player_setup_action.y = 20
            s_player_setup_action.name = " player setup"
            s_player_setup_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    PlayerSetupFunc(o)
                }
            }

            Menu_AddItem(s_multiplayer_menu, s_join_network_server_action)
            Menu_AddItem(s_multiplayer_menu, s_start_network_server_action)
            Menu_AddItem(s_multiplayer_menu, s_player_setup_action)

            Menu_SetStatusBar(s_multiplayer_menu, null)

            Menu_Center(s_multiplayer_menu)
        }

        fun Multiplayer_MenuKey(key: Int): String {
            return Default_MenuKey(s_multiplayer_menu, key)
        }

        var Menu_Multiplayer: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Multiplayer_f()
            }
        }

        fun Menu_Multiplayer_f() {
            Multiplayer_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Multiplayer_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Multiplayer_MenuKey(key)
                }
            })
        }

        /*
     * =======================================================================
     * 
     * KEYS MENU
     * 
     * =======================================================================
     */
        var bindnames = array<Array<String>>(array<String>("+attack", "attack"), array<String>("weapnext", "next weapon"), array<String>("+forward", "walk forward"), array<String>("+back", "backpedal"), array<String>("+left", "turn left"), array<String>("+right", "turn right"), array<String>("+speed", "run"), array<String>("+moveleft", "step left"), array<String>("+moveright", "step right"), array<String>("+strafe", "sidestep"), array<String>("+lookup", "look up"), array<String>("+lookdown", "look down"), array<String>("centerview", "center view"), array<String>("+mlook", "mouse look"), array<String>("+klook", "keyboard look"), array<String>("+moveup", "up / jump"), array<String>("+movedown", "down / crouch"), array<String>(
                "inven", "inventory"), array<String>("invuse", "use item"), array<String>("invdrop", "drop item"), array<String>("invprev", "prev item"), array<String>("invnext", "next item"), array<String>(
                "cmd help", "help computer"), array<String>(null, null))

        var bind_grab: Boolean = false

        var s_keys_menu = menuframework_s()

        var s_keys_attack_action = menuaction_s()

        var s_keys_change_weapon_action = menuaction_s()

        var s_keys_walk_forward_action = menuaction_s()

        var s_keys_backpedal_action = menuaction_s()

        var s_keys_turn_left_action = menuaction_s()

        var s_keys_turn_right_action = menuaction_s()

        var s_keys_run_action = menuaction_s()

        var s_keys_step_left_action = menuaction_s()

        var s_keys_step_right_action = menuaction_s()

        var s_keys_sidestep_action = menuaction_s()

        var s_keys_look_up_action = menuaction_s()

        var s_keys_look_down_action = menuaction_s()

        var s_keys_center_view_action = menuaction_s()

        var s_keys_mouse_look_action = menuaction_s()

        var s_keys_keyboard_look_action = menuaction_s()

        var s_keys_move_up_action = menuaction_s()

        var s_keys_move_down_action = menuaction_s()

        var s_keys_inventory_action = menuaction_s()

        var s_keys_inv_use_action = menuaction_s()

        var s_keys_inv_drop_action = menuaction_s()

        var s_keys_inv_prev_action = menuaction_s()

        var s_keys_inv_next_action = menuaction_s()

        var s_keys_help_computer_action = menuaction_s()

        fun UnbindCommand(command: String) {
            var j: Int
            var b: String?

            run {
                j = 0
                while (j < 256) {
                    b = keybindings[j]
                    if (b == null)
                        continue
                    if (b.equals(command))
                        Key.SetBinding(j, "")
                    j++
                }
            }
        }

        fun FindKeysForCommand(command: String, twokeys: IntArray) {
            var count: Int
            var j: Int
            var b: String?

            twokeys[0] = twokeys[1] = -1
            count = 0

            run {
                j = 0
                while (j < 256) {
                    b = keybindings[j]
                    if (b == null)
                        continue

                    if (b.equals(command)) {
                        twokeys[count] = j
                        count++
                        if (count == 2)
                            break
                    }
                    j++
                }
            }
        }

        fun KeyCursorDrawFunc(menu: menuframework_s) {
            if (bind_grab)
                re.DrawChar(menu.x, menu.y + menu.cursor * 9, '=')
            else
                re.DrawChar(menu.x, menu.y + menu.cursor * 9, 12 + ((Timer.Milliseconds() / 250) as Int and 1))
        }

        fun DrawKeyBindingFunc(self: Object) {
            val keys = intArray(0, 0)
            val a = self as menuaction_s

            FindKeysForCommand(bindnames[a.localdata[0]][0], keys)

            if (keys[0] == -1) {
                Menu_DrawString(a.x + a.parent.x + 16, a.y + a.parent.y, "???")
            } else {
                val x: Int
                val name: String

                name = Key.KeynumToString(keys[0])

                Menu_DrawString(a.x + a.parent.x + 16, a.y + a.parent.y, name)

                x = name.length() * 8

                if (keys[1] != -1) {
                    Menu_DrawString(a.x + a.parent.x + 24 + x, a.y + a.parent.y, "or")
                    Menu_DrawString(a.x + a.parent.x + 48 + x, a.y + a.parent.y, Key.KeynumToString(keys[1]))
                }
            }
        }

        fun KeyBindingFunc(self: Object) {
            val a = self as menuaction_s
            val keys = intArray(0, 0)

            FindKeysForCommand(bindnames[a.localdata[0]][0], keys)

            if (keys[1] != -1)
                UnbindCommand(bindnames[a.localdata[0]][0])

            bind_grab = true

            Menu_SetStatusBar(s_keys_menu, "press a key or button for this action")
        }

        fun Keys_MenuInit() {
            var y = 0
            var i = 0

            s_keys_menu.x = (viddef.width * 0.50) as Int
            s_keys_menu.nitems = 0
            s_keys_menu.cursordraw = object : mcallback() {
                public fun execute(o: Object) {
                    KeyCursorDrawFunc(o as menuframework_s)
                }
            }

            s_keys_attack_action.type = MTYPE_ACTION
            s_keys_attack_action.flags = QMF_GRAYED
            s_keys_attack_action.x = 0
            s_keys_attack_action.y = y
            s_keys_attack_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_attack_action.localdata[0] = i
            s_keys_attack_action.name = bindnames[s_keys_attack_action.localdata[0]][1]

            s_keys_change_weapon_action.type = MTYPE_ACTION
            s_keys_change_weapon_action.flags = QMF_GRAYED
            s_keys_change_weapon_action.x = 0
            s_keys_change_weapon_action.y = y += 9
            s_keys_change_weapon_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_change_weapon_action.localdata[0] = ++i
            s_keys_change_weapon_action.name = bindnames[s_keys_change_weapon_action.localdata[0]][1]

            s_keys_walk_forward_action.type = MTYPE_ACTION
            s_keys_walk_forward_action.flags = QMF_GRAYED
            s_keys_walk_forward_action.x = 0
            s_keys_walk_forward_action.y = y += 9
            s_keys_walk_forward_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_walk_forward_action.localdata[0] = ++i
            s_keys_walk_forward_action.name = bindnames[s_keys_walk_forward_action.localdata[0]][1]

            s_keys_backpedal_action.type = MTYPE_ACTION
            s_keys_backpedal_action.flags = QMF_GRAYED
            s_keys_backpedal_action.x = 0
            s_keys_backpedal_action.y = y += 9
            s_keys_backpedal_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_backpedal_action.localdata[0] = ++i
            s_keys_backpedal_action.name = bindnames[s_keys_backpedal_action.localdata[0]][1]

            s_keys_turn_left_action.type = MTYPE_ACTION
            s_keys_turn_left_action.flags = QMF_GRAYED
            s_keys_turn_left_action.x = 0
            s_keys_turn_left_action.y = y += 9
            s_keys_turn_left_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_turn_left_action.localdata[0] = ++i
            s_keys_turn_left_action.name = bindnames[s_keys_turn_left_action.localdata[0]][1]

            s_keys_turn_right_action.type = MTYPE_ACTION
            s_keys_turn_right_action.flags = QMF_GRAYED
            s_keys_turn_right_action.x = 0
            s_keys_turn_right_action.y = y += 9
            s_keys_turn_right_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_turn_right_action.localdata[0] = ++i
            s_keys_turn_right_action.name = bindnames[s_keys_turn_right_action.localdata[0]][1]

            s_keys_run_action.type = MTYPE_ACTION
            s_keys_run_action.flags = QMF_GRAYED
            s_keys_run_action.x = 0
            s_keys_run_action.y = y += 9
            s_keys_run_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_run_action.localdata[0] = ++i
            s_keys_run_action.name = bindnames[s_keys_run_action.localdata[0]][1]

            s_keys_step_left_action.type = MTYPE_ACTION
            s_keys_step_left_action.flags = QMF_GRAYED
            s_keys_step_left_action.x = 0
            s_keys_step_left_action.y = y += 9
            s_keys_step_left_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }
            s_keys_step_left_action.localdata[0] = ++i
            s_keys_step_left_action.name = bindnames[s_keys_step_left_action.localdata[0]][1]

            s_keys_step_right_action.type = MTYPE_ACTION
            s_keys_step_right_action.flags = QMF_GRAYED
            s_keys_step_right_action.x = 0
            s_keys_step_right_action.y = y += 9
            s_keys_step_right_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_step_right_action.localdata[0] = ++i
            s_keys_step_right_action.name = bindnames[s_keys_step_right_action.localdata[0]][1]

            s_keys_sidestep_action.type = MTYPE_ACTION
            s_keys_sidestep_action.flags = QMF_GRAYED
            s_keys_sidestep_action.x = 0
            s_keys_sidestep_action.y = y += 9
            s_keys_sidestep_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_sidestep_action.localdata[0] = ++i
            s_keys_sidestep_action.name = bindnames[s_keys_sidestep_action.localdata[0]][1]

            s_keys_look_up_action.type = MTYPE_ACTION
            s_keys_look_up_action.flags = QMF_GRAYED
            s_keys_look_up_action.x = 0
            s_keys_look_up_action.y = y += 9
            s_keys_look_up_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_look_up_action.localdata[0] = ++i
            s_keys_look_up_action.name = bindnames[s_keys_look_up_action.localdata[0]][1]

            s_keys_look_down_action.type = MTYPE_ACTION
            s_keys_look_down_action.flags = QMF_GRAYED
            s_keys_look_down_action.x = 0
            s_keys_look_down_action.y = y += 9
            s_keys_look_down_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_look_down_action.localdata[0] = ++i
            s_keys_look_down_action.name = bindnames[s_keys_look_down_action.localdata[0]][1]

            s_keys_center_view_action.type = MTYPE_ACTION
            s_keys_center_view_action.flags = QMF_GRAYED
            s_keys_center_view_action.x = 0
            s_keys_center_view_action.y = y += 9
            s_keys_center_view_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_center_view_action.localdata[0] = ++i
            s_keys_center_view_action.name = bindnames[s_keys_center_view_action.localdata[0]][1]

            s_keys_mouse_look_action.type = MTYPE_ACTION
            s_keys_mouse_look_action.flags = QMF_GRAYED
            s_keys_mouse_look_action.x = 0
            s_keys_mouse_look_action.y = y += 9
            s_keys_mouse_look_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_mouse_look_action.localdata[0] = ++i
            s_keys_mouse_look_action.name = bindnames[s_keys_mouse_look_action.localdata[0]][1]

            s_keys_keyboard_look_action.type = MTYPE_ACTION
            s_keys_keyboard_look_action.flags = QMF_GRAYED
            s_keys_keyboard_look_action.x = 0
            s_keys_keyboard_look_action.y = y += 9
            s_keys_keyboard_look_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_keyboard_look_action.localdata[0] = ++i
            s_keys_keyboard_look_action.name = bindnames[s_keys_keyboard_look_action.localdata[0]][1]

            s_keys_move_up_action.type = MTYPE_ACTION
            s_keys_move_up_action.flags = QMF_GRAYED
            s_keys_move_up_action.x = 0
            s_keys_move_up_action.y = y += 9
            s_keys_move_up_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_move_up_action.localdata[0] = ++i
            s_keys_move_up_action.name = bindnames[s_keys_move_up_action.localdata[0]][1]

            s_keys_move_down_action.type = MTYPE_ACTION
            s_keys_move_down_action.flags = QMF_GRAYED
            s_keys_move_down_action.x = 0
            s_keys_move_down_action.y = y += 9
            s_keys_move_down_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_move_down_action.localdata[0] = ++i
            s_keys_move_down_action.name = bindnames[s_keys_move_down_action.localdata[0]][1]

            s_keys_inventory_action.type = MTYPE_ACTION
            s_keys_inventory_action.flags = QMF_GRAYED
            s_keys_inventory_action.x = 0
            s_keys_inventory_action.y = y += 9
            s_keys_inventory_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_inventory_action.localdata[0] = ++i
            s_keys_inventory_action.name = bindnames[s_keys_inventory_action.localdata[0]][1]

            s_keys_inv_use_action.type = MTYPE_ACTION
            s_keys_inv_use_action.flags = QMF_GRAYED
            s_keys_inv_use_action.x = 0
            s_keys_inv_use_action.y = y += 9
            s_keys_inv_use_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_inv_use_action.localdata[0] = ++i
            s_keys_inv_use_action.name = bindnames[s_keys_inv_use_action.localdata[0]][1]

            s_keys_inv_drop_action.type = MTYPE_ACTION
            s_keys_inv_drop_action.flags = QMF_GRAYED
            s_keys_inv_drop_action.x = 0
            s_keys_inv_drop_action.y = y += 9
            s_keys_inv_drop_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_inv_drop_action.localdata[0] = ++i
            s_keys_inv_drop_action.name = bindnames[s_keys_inv_drop_action.localdata[0]][1]

            s_keys_inv_prev_action.type = MTYPE_ACTION
            s_keys_inv_prev_action.flags = QMF_GRAYED
            s_keys_inv_prev_action.x = 0
            s_keys_inv_prev_action.y = y += 9
            s_keys_inv_prev_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_inv_prev_action.localdata[0] = ++i
            s_keys_inv_prev_action.name = bindnames[s_keys_inv_prev_action.localdata[0]][1]

            s_keys_inv_next_action.type = MTYPE_ACTION
            s_keys_inv_next_action.flags = QMF_GRAYED
            s_keys_inv_next_action.x = 0
            s_keys_inv_next_action.y = y += 9
            s_keys_inv_next_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_inv_next_action.localdata[0] = ++i
            s_keys_inv_next_action.name = bindnames[s_keys_inv_next_action.localdata[0]][1]

            s_keys_help_computer_action.type = MTYPE_ACTION
            s_keys_help_computer_action.flags = QMF_GRAYED
            s_keys_help_computer_action.x = 0
            s_keys_help_computer_action.y = y += 9
            s_keys_help_computer_action.ownerdraw = object : mcallback() {
                public fun execute(o: Object) {
                    DrawKeyBindingFunc(o)
                }
            }

            s_keys_help_computer_action.localdata[0] = ++i
            s_keys_help_computer_action.name = bindnames[s_keys_help_computer_action.localdata[0]][1]

            Menu_AddItem(s_keys_menu, s_keys_attack_action)
            Menu_AddItem(s_keys_menu, s_keys_change_weapon_action)
            Menu_AddItem(s_keys_menu, s_keys_walk_forward_action)
            Menu_AddItem(s_keys_menu, s_keys_backpedal_action)
            Menu_AddItem(s_keys_menu, s_keys_turn_left_action)
            Menu_AddItem(s_keys_menu, s_keys_turn_right_action)
            Menu_AddItem(s_keys_menu, s_keys_run_action)
            Menu_AddItem(s_keys_menu, s_keys_step_left_action)
            Menu_AddItem(s_keys_menu, s_keys_step_right_action)
            Menu_AddItem(s_keys_menu, s_keys_sidestep_action)
            Menu_AddItem(s_keys_menu, s_keys_look_up_action)
            Menu_AddItem(s_keys_menu, s_keys_look_down_action)
            Menu_AddItem(s_keys_menu, s_keys_center_view_action)
            Menu_AddItem(s_keys_menu, s_keys_mouse_look_action)
            Menu_AddItem(s_keys_menu, s_keys_keyboard_look_action)
            Menu_AddItem(s_keys_menu, s_keys_move_up_action)
            Menu_AddItem(s_keys_menu, s_keys_move_down_action)

            Menu_AddItem(s_keys_menu, s_keys_inventory_action)
            Menu_AddItem(s_keys_menu, s_keys_inv_use_action)
            Menu_AddItem(s_keys_menu, s_keys_inv_drop_action)
            Menu_AddItem(s_keys_menu, s_keys_inv_prev_action)
            Menu_AddItem(s_keys_menu, s_keys_inv_next_action)

            Menu_AddItem(s_keys_menu, s_keys_help_computer_action)

            Menu_SetStatusBar(s_keys_menu, "enter to change, backspace to clear")
            Menu_Center(s_keys_menu)
        }

        var Keys_MenuDraw: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Keys_MenuDraw_f()
            }
        }

        fun Keys_MenuDraw_f() {
            Menu_AdjustCursor(s_keys_menu, 1)
            Menu_Draw(s_keys_menu)
        }

        var Keys_MenuKey: keyfunc_t = object : keyfunc_t() {
            public fun execute(key: Int): String {
                return Keys_MenuKey_f(key)
            }
        }

        fun Keys_MenuKey_f(key: Int): String {
            val item = Menu_ItemAtCursor(s_keys_menu) as menuaction_s

            if (bind_grab) {
                if (key != K_ESCAPE && key != '`') {
                    //char cmd[1024];
                    val cmd: String

                    //Com_sprintf(cmd, sizeof(cmd), "bind \"%s\" \"%s\"\n",
                    // Key_KeynumToString(key), bindnames[item.localdata[0]][0]);
                    cmd = "bind \"" + Key.KeynumToString(key) + "\" \"" + bindnames[item.localdata[0]][0] + "\""
                    Cbuf.InsertText(cmd)
                }

                Menu_SetStatusBar(s_keys_menu, "enter to change, backspace to clear")
                bind_grab = false
                return menu_out_sound
            }

            when (key) {
                K_KP_ENTER, K_ENTER -> {
                    KeyBindingFunc(item)
                    return menu_in_sound
                }
                K_BACKSPACE // delete bindings
                    , K_DEL // delete bindings
                    , K_KP_DEL -> {
                    UnbindCommand(bindnames[item.localdata[0]][0])
                    return menu_out_sound
                }
                else -> return Default_MenuKey(s_keys_menu, key)
            }
        }

        var Menu_Keys: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Keys_f()
            }
        }

        fun Menu_Keys_f() {
            Keys_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Keys_MenuDraw_f()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Keys_MenuKey_f(key)
                }
            })
        }

        /*
     * =======================================================================
     * 
     * CONTROLS MENU
     * 
     * =======================================================================
     */
        var win_noalttab: cvar_t

        var s_options_menu = menuframework_s()

        var s_options_defaults_action = menuaction_s()

        var s_options_customize_options_action = menuaction_s()

        var s_options_sensitivity_slider = menuslider_s()

        var s_options_freelook_box = menulist_s()

        var s_options_noalttab_box = menulist_s()

        var s_options_alwaysrun_box = menulist_s()

        var s_options_invertmouse_box = menulist_s()

        var s_options_lookspring_box = menulist_s()

        var s_options_lookstrafe_box = menulist_s()

        var s_options_crosshair_box = menulist_s()

        var s_options_sfxvolume_slider = menuslider_s()

        var s_options_joystick_box = menulist_s()

        var s_options_cdvolume_box = menulist_s()

        var s_options_quality_list = menulist_s()

        //static menulist_s s_options_compatibility_list = new menulist_s();
        var s_options_console_action = menuaction_s()

        fun CrosshairFunc(unused: Object) {
            Cvar.SetValue("crosshair", s_options_crosshair_box.curvalue)
        }

        fun JoystickFunc(unused: Object) {
            Cvar.SetValue("in_joystick", s_options_joystick_box.curvalue)
        }

        fun CustomizeControlsFunc(unused: Object) {
            Menu_Keys_f()
        }

        fun AlwaysRunFunc(unused: Object) {
            Cvar.SetValue("cl_run", s_options_alwaysrun_box.curvalue)
        }

        fun FreeLookFunc(unused: Object) {
            Cvar.SetValue("freelook", s_options_freelook_box.curvalue)
        }

        fun MouseSpeedFunc(unused: Object) {
            Cvar.SetValue("sensitivity", s_options_sensitivity_slider.curvalue / 2.0.toFloat())
        }

        fun NoAltTabFunc(unused: Object) {
            Cvar.SetValue("win_noalttab", s_options_noalttab_box.curvalue)
        }

        fun ClampCvar(min: Float, max: Float, value: Float): Float {
            if (value < min)
                return min
            if (value > max)
                return max
            return value
        }

        fun ControlsSetMenuItemValues() {
            s_options_sfxvolume_slider.curvalue = Cvar.VariableValue("s_volume") * 10
            s_options_cdvolume_box.curvalue = 1 - (Cvar.VariableValue("cd_nocd") as Int)
            //s_options_quality_list.curvalue = 1 - ((int)
            // Cvar.VariableValue("s_loadas8bit"));
            val s = Cvar.VariableString("s_impl")
            for (i in s_drivers.indices) {
                if (s.equals(s_drivers[i])) {
                    s_options_quality_list.curvalue = i
                }
            }

            s_options_sensitivity_slider.curvalue = (sensitivity.value) * 2

            Cvar.SetValue("cl_run", ClampCvar(0, 1, cl_run.value))
            s_options_alwaysrun_box.curvalue = cl_run.value as Int

            s_options_invertmouse_box.curvalue = if (m_pitch.value < 0) 1 else 0

            Cvar.SetValue("lookspring", ClampCvar(0, 1, lookspring.value))
            s_options_lookspring_box.curvalue = lookspring.value as Int

            Cvar.SetValue("lookstrafe", ClampCvar(0, 1, lookstrafe.value))
            s_options_lookstrafe_box.curvalue = lookstrafe.value as Int

            Cvar.SetValue("freelook", ClampCvar(0, 1, freelook.value))
            s_options_freelook_box.curvalue = freelook.value as Int

            Cvar.SetValue("crosshair", ClampCvar(0, 3, Globals.crosshair.value))
            s_options_crosshair_box.curvalue = Globals.crosshair.value as Int

            Cvar.SetValue("in_joystick", ClampCvar(0, 1, in_joystick.value))
            s_options_joystick_box.curvalue = in_joystick.value as Int

            s_options_noalttab_box.curvalue = win_noalttab.value as Int
        }

        fun ControlsResetDefaultsFunc(unused: Object) {
            Cbuf.AddText("exec default.cfg\n")
            Cbuf.Execute()

            ControlsSetMenuItemValues()
        }

        fun InvertMouseFunc(unused: Object) {
            Cvar.SetValue("m_pitch", -m_pitch.value)
        }

        fun LookspringFunc(unused: Object) {
            Cvar.SetValue("lookspring", 1 - lookspring.value)
        }

        fun LookstrafeFunc(unused: Object) {
            Cvar.SetValue("lookstrafe", 1 - lookstrafe.value)
        }

        fun UpdateVolumeFunc(unused: Object) {
            Cvar.SetValue("s_volume", s_options_sfxvolume_slider.curvalue / 10)
        }

        fun UpdateCDVolumeFunc(unused: Object) {
            Cvar.SetValue("cd_nocd", 1 - s_options_cdvolume_box.curvalue)
        }

        fun ConsoleFunc(unused: Object) {
            /*
         * * the proper way to do this is probably to have ToggleConsole_f
         * accept a parameter
         */

            if (cl.attractloop) {
                Cbuf.AddText("killserver\n")
                return
            }

            Key.ClearTyping()
            Console.ClearNotify()

            ForceMenuOff()
            cls.key_dest = key_console
        }

        fun UpdateSoundQualityFunc(unused: Object) {
            var driverNotChanged = false
            val current = s_drivers[s_options_quality_list.curvalue]
            driverNotChanged = S.getDriverName().equals(current)
            //        if (s_options_quality_list.curvalue != 0) {
            //            //			Cvar.SetValue("s_khz", 22);
            //            //			Cvar.SetValue("s_loadas8bit", 0);
            //            driverNotChanged = S.getDriverName().equals("dummy");
            //            Cvar.Set("s_impl", "dummy");
            //        } else {
            //            //			Cvar.SetValue("s_khz", 11);
            //            //			Cvar.SetValue("s_loadas8bit", 1);
            //            driverNotChanged = S.getDriverName().equals("joal");
            //            Cvar.Set("s_impl", "joal");
            //        }

            //Cvar.SetValue("s_primary", s_options_compatibility_list.curvalue);

            if (driverNotChanged) {
                re.EndFrame()
                return
            } else {
                Cvar.Set("s_impl", current)

                DrawTextBox(8, 120 - 48, 36, 3)
                Print(16 + 16, 120 - 48 + 8, "Restarting the sound system. This")
                Print(16 + 16, 120 - 48 + 16, "could take up to a minute, so")
                Print(16 + 16, 120 - 48 + 24, "please be patient.")

                // the text box won't show up unless we do a buffer swap
                re.EndFrame()

                CL.Snd_Restart_f.execute()
            }
        }

        var cd_music_items = array<String>("disabled", "enabled")

        var compatibility_items = array<String>("max compatibility", "max performance")

        var yesno_names = array<String>("no", "yes")

        var crosshair_names = array<String>("none", "cross", "dot", "angle")

        var s_labels: Array<String>
        var s_drivers: Array<String>

        fun Options_MenuInit() {

            s_drivers = S.getDriverNames()
            s_labels = arrayOfNulls<String>(s_drivers.size())
            for (i in s_drivers.indices) {
                if ("dummy".equals(s_drivers[i])) {
                    s_labels[i] = "off"
                } else {
                    s_labels[i] = s_drivers[i]
                }
            }

            win_noalttab = Cvar.Get("win_noalttab", "0", CVAR_ARCHIVE)

            /*
         * * configure controls menu and menu items
         */
            s_options_menu.x = viddef.width / 2
            s_options_menu.y = viddef.height / 2 - 58
            s_options_menu.nitems = 0

            s_options_sfxvolume_slider.type = MTYPE_SLIDER
            s_options_sfxvolume_slider.x = 0
            s_options_sfxvolume_slider.y = 0
            s_options_sfxvolume_slider.name = "effects volume"
            s_options_sfxvolume_slider.callback = object : mcallback() {
                public fun execute(o: Object) {
                    UpdateVolumeFunc(o)
                }
            }
            s_options_sfxvolume_slider.minvalue = 0
            s_options_sfxvolume_slider.maxvalue = 10
            s_options_sfxvolume_slider.curvalue = Cvar.VariableValue("s_volume") * 10

            s_options_cdvolume_box.type = MTYPE_SPINCONTROL
            s_options_cdvolume_box.x = 0
            s_options_cdvolume_box.y = 10
            s_options_cdvolume_box.name = "CD music"
            s_options_cdvolume_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    UpdateCDVolumeFunc(o)
                }
            }
            s_options_cdvolume_box.itemnames = cd_music_items
            s_options_cdvolume_box.curvalue = 1 - Cvar.VariableValue("cd_nocd") as Int

            s_options_quality_list.type = MTYPE_SPINCONTROL
            s_options_quality_list.x = 0
            s_options_quality_list.y = 20
            s_options_quality_list.name = "sound"
            s_options_quality_list.callback = object : mcallback() {
                public fun execute(o: Object) {
                    UpdateSoundQualityFunc(o)
                }
            }
            s_options_quality_list.itemnames = s_labels

            s_options_sensitivity_slider.type = MTYPE_SLIDER
            s_options_sensitivity_slider.x = 0
            s_options_sensitivity_slider.y = 50
            s_options_sensitivity_slider.name = "mouse speed"
            s_options_sensitivity_slider.callback = object : mcallback() {
                public fun execute(o: Object) {
                    MouseSpeedFunc(o)
                }
            }
            s_options_sensitivity_slider.minvalue = 2
            s_options_sensitivity_slider.maxvalue = 22

            s_options_alwaysrun_box.type = MTYPE_SPINCONTROL
            s_options_alwaysrun_box.x = 0
            s_options_alwaysrun_box.y = 60
            s_options_alwaysrun_box.name = "always run"
            s_options_alwaysrun_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    AlwaysRunFunc(o)
                }
            }
            s_options_alwaysrun_box.itemnames = yesno_names

            s_options_invertmouse_box.type = MTYPE_SPINCONTROL
            s_options_invertmouse_box.x = 0
            s_options_invertmouse_box.y = 70
            s_options_invertmouse_box.name = "invert mouse"
            s_options_invertmouse_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    InvertMouseFunc(o)
                }
            }
            s_options_invertmouse_box.itemnames = yesno_names

            s_options_lookspring_box.type = MTYPE_SPINCONTROL
            s_options_lookspring_box.x = 0
            s_options_lookspring_box.y = 80
            s_options_lookspring_box.name = "lookspring"
            s_options_lookspring_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    LookspringFunc(o)
                }
            }
            s_options_lookspring_box.itemnames = yesno_names

            s_options_lookstrafe_box.type = MTYPE_SPINCONTROL
            s_options_lookstrafe_box.x = 0
            s_options_lookstrafe_box.y = 90
            s_options_lookstrafe_box.name = "lookstrafe"
            s_options_lookstrafe_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    LookstrafeFunc(o)
                }
            }
            s_options_lookstrafe_box.itemnames = yesno_names

            s_options_freelook_box.type = MTYPE_SPINCONTROL
            s_options_freelook_box.x = 0
            s_options_freelook_box.y = 100
            s_options_freelook_box.name = "free look"
            s_options_freelook_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    FreeLookFunc(o)
                }
            }
            s_options_freelook_box.itemnames = yesno_names

            s_options_crosshair_box.type = MTYPE_SPINCONTROL
            s_options_crosshair_box.x = 0
            s_options_crosshair_box.y = 110
            s_options_crosshair_box.name = "crosshair"
            s_options_crosshair_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    CrosshairFunc(o)
                }
            }
            s_options_crosshair_box.itemnames = crosshair_names
            /*
         * s_options_noalttab_box.type = MTYPE_SPINCONTROL;
         * s_options_noalttab_box.x = 0; s_options_noalttab_box.y = 110;
         * s_options_noalttab_box.name = "disable alt-tab";
         * s_options_noalttab_box.callback = NoAltTabFunc;
         * s_options_noalttab_box.itemnames = yesno_names;
         */
            s_options_joystick_box.type = MTYPE_SPINCONTROL
            s_options_joystick_box.x = 0
            s_options_joystick_box.y = 120
            s_options_joystick_box.name = "use joystick"
            s_options_joystick_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    JoystickFunc(o)
                }
            }
            s_options_joystick_box.itemnames = yesno_names

            s_options_customize_options_action.type = MTYPE_ACTION
            s_options_customize_options_action.x = 0
            s_options_customize_options_action.y = 140
            s_options_customize_options_action.name = "customize controls"
            s_options_customize_options_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    CustomizeControlsFunc(o)
                }
            }

            s_options_defaults_action.type = MTYPE_ACTION
            s_options_defaults_action.x = 0
            s_options_defaults_action.y = 150
            s_options_defaults_action.name = "reset defaults"
            s_options_defaults_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    ControlsResetDefaultsFunc(o)
                }
            }

            s_options_console_action.type = MTYPE_ACTION
            s_options_console_action.x = 0
            s_options_console_action.y = 160
            s_options_console_action.name = "go to console"
            s_options_console_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    ConsoleFunc(o)
                }
            }

            ControlsSetMenuItemValues()

            Menu_AddItem(s_options_menu, s_options_sfxvolume_slider)

            Menu_AddItem(s_options_menu, s_options_cdvolume_box)
            Menu_AddItem(s_options_menu, s_options_quality_list)
            //		Menu_AddItem(s_options_menu, s_options_compatibility_list);
            Menu_AddItem(s_options_menu, s_options_sensitivity_slider)
            Menu_AddItem(s_options_menu, s_options_alwaysrun_box)
            Menu_AddItem(s_options_menu, s_options_invertmouse_box)
            Menu_AddItem(s_options_menu, s_options_lookspring_box)
            Menu_AddItem(s_options_menu, s_options_lookstrafe_box)
            Menu_AddItem(s_options_menu, s_options_freelook_box)
            Menu_AddItem(s_options_menu, s_options_crosshair_box)
            //		Menu_AddItem(s_options_menu, s_options_joystick_box);
            Menu_AddItem(s_options_menu, s_options_customize_options_action)
            Menu_AddItem(s_options_menu, s_options_defaults_action)
            Menu_AddItem(s_options_menu, s_options_console_action)
        }

        fun Options_MenuDraw() {
            Banner("m_banner_options")
            Menu_AdjustCursor(s_options_menu, 1)
            Menu_Draw(s_options_menu)
        }

        fun Options_MenuKey(key: Int): String {
            return Default_MenuKey(s_options_menu, key)
        }

        var Menu_Options: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Options_f()
            }
        }

        fun Menu_Options_f() {
            Options_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Options_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Options_MenuKey(key)
                }
            })
        }

        /*
     * =======================================================================
     * 
     * VIDEO MENU
     * 
     * =======================================================================
     */

        var Menu_Video: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Video_f()
            }
        }

        fun Menu_Video_f() {
            VID.MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    VID.MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return VID.MenuKey(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * END GAME MENU
     * 
     * =============================================================================
     */
        var credits_start_time: Int = 0

        var creditsIndex = arrayOfNulls<String>(256)

        var creditsBuffer: String? = null

        var idcredits = array<String>("+QUAKE II BY ID SOFTWARE", "", "+PROGRAMMING", "John Carmack", "John Cash", "Brian Hook", "", "+JAVA PORT BY BYTONIC", "Carsten Weisse", "Holger Zickner", "Rene Stoeckel", "", "+ART", "Adrian Carmack", "Kevin Cloud", "Paul Steed", "", "+LEVEL DESIGN", "Tim Willits", "American McGee", "Christian Antkow", "Paul Jaquays", "Brandon James", "", "+BIZ", "Todd Hollenshead", "Barrett (Bear) Alexander", "Donna Jackson", "", "", "+SPECIAL THANKS", "Ben Donges for beta testing", "", "", "", "", "", "", "+ADDITIONAL SUPPORT", "", "+LINUX PORT AND CTF", "Dave \"Zoid\" Kirsch", "", "+CINEMATIC SEQUENCES", "Ending Cinematic by Blur Studio - ", "Venice, CA", "", "Environment models for Introduction", "Cinematic by Karl Dolgener", "", "Assistance with environment design", "by Cliff Iwai", "", "+SOUND EFFECTS AND MUSIC", "Sound Design by Soundelux Media Labs.", "Music Composed and Produced by", "Soundelux Media Labs.  Special thanks", "to Bill Brown, Tom Ozanich, Brian", "Celano, Jeff Eisner, and The Soundelux", "Players.", "", "\"Level Music\" by Sonic Mayhem", "www.sonicmayhem.com", "", "\"Quake II Theme Song\"", "(C) 1997 Rob Zombie. All Rights", "Reserved.", "", "Track 10 (\"Climb\") by Jer Sypult", "", "Voice of computers by", "Carly Staehlin-Taylor", "", "+THANKS TO ACTIVISION", "+IN PARTICULAR:", "", "John Tam", "Steve Rosenthal", "Marty Stratton", "Henk Hartong", "", "Quake II(tm) (C)1997 Id Software, Inc.", "All Rights Reserved.  Distributed by", "Activision, Inc. under license.", "Quake II(tm), the Id Software name,", "the \"Q II\"(tm) logo and id(tm)", "logo are trademarks of Id Software,", "Inc. Activision(R) is a registered", "trademark of Activision, Inc. All", "other trademarks and trade names are", "properties of their respective owners.", null)

        var credits = idcredits

        var xatcredits = array<String>("+QUAKE II MISSION PACK: THE RECKONING", "+BY", "+XATRIX ENTERTAINMENT, INC.", "", "+DESIGN AND DIRECTION", "Drew Markham", "", "+PRODUCED BY", "Greg Goodrich", "", "+PROGRAMMING", "Rafael Paiz", "", "+LEVEL DESIGN / ADDITIONAL GAME DESIGN", "Alex Mayberry", "", "+LEVEL DESIGN", "Mal Blackwell", "Dan Koppel", "", "+ART DIRECTION", "Michael \"Maxx\" Kaufman", "", "+COMPUTER GRAPHICS SUPERVISOR AND", "+CHARACTER ANIMATION DIRECTION", "Barry Dempsey", "", "+SENIOR ANIMATOR AND MODELER", "Jason Hoover", "", "+CHARACTER ANIMATION AND", "+MOTION CAPTURE SPECIALIST", "Amit Doron", "", "+ART", "Claire Praderie-Markham", "Viktor Antonov", "Corky Lehmkuhl", "", "+INTRODUCTION ANIMATION", "Dominique Drozdz", "", "+ADDITIONAL LEVEL DESIGN", "Aaron Barber", "Rhett Baldwin", "", "+3D CHARACTER ANIMATION TOOLS", "Gerry Tyra, SA Technology", "", "+ADDITIONAL EDITOR TOOL PROGRAMMING", "Robert Duffy", "", "+ADDITIONAL PROGRAMMING", "Ryan Feltrin", "", "+PRODUCTION COORDINATOR", "Victoria Sylvester", "", "+SOUND DESIGN", "Gary Bradfield", "", "+MUSIC BY", "Sonic Mayhem", "", "", "", "+SPECIAL THANKS", "+TO", "+OUR FRIENDS AT ID SOFTWARE", "", "John Carmack", "John Cash", "Brian Hook", "Adrian Carmack", "Kevin Cloud", "Paul Steed", "Tim Willits", "Christian Antkow", "Paul Jaquays", "Brandon James", "Todd Hollenshead", "Barrett (Bear) Alexander", "Dave \"Zoid\" Kirsch", "Donna Jackson", "", "", "", "+THANKS TO ACTIVISION", "+IN PARTICULAR:", "", "Marty Stratton", "Henk \"The Original Ripper\" Hartong", "Kevin Kraff", "Jamey Gottlieb", "Chris Hepburn", "", "+AND THE GAME TESTERS", "", "Tim Vanlaw", "Doug Jacobs", "Steven Rosenthal", "David Baker", "Chris Campbell", "Aaron Casillas", "Steve Elwell", "Derek Johnstone", "Igor Krinitskiy", "Samantha Lee", "Michael Spann", "Chris Toft", "Juan Valdes", "", "+THANKS TO INTERGRAPH COMPUTER SYTEMS", "+IN PARTICULAR:", "", "Michael T. Nicolaou", "", "", "Quake II Mission Pack: The Reckoning", "(tm) (C)1998 Id Software, Inc. All", "Rights Reserved. Developed by Xatrix", "Entertainment, Inc. for Id Software,", "Inc. Distributed by Activision Inc.", "under license. Quake(R) is a", "registered trademark of Id Software,", "Inc. Quake II Mission Pack: The", "Reckoning(tm), Quake II(tm), the Id", "Software name, the \"Q II\"(tm) logo", "and id(tm) logo are trademarks of Id", "Software, Inc. Activision(R) is a", "registered trademark of Activision,", "Inc. Xatrix(R) is a registered", "trademark of Xatrix Entertainment,", "Inc. All other trademarks and trade", "names are properties of their", "respective owners.", null)

        var roguecredits = array<String>("+QUAKE II MISSION PACK 2: GROUND ZERO", "+BY", "+ROGUE ENTERTAINMENT, INC.", "", "+PRODUCED BY", "Jim Molinets", "", "+PROGRAMMING", "Peter Mack", "Patrick Magruder", "", "+LEVEL DESIGN", "Jim Molinets", "Cameron Lamprecht", "Berenger Fish", "Robert Selitto", "Steve Tietze", "Steve Thoms", "", "+ART DIRECTION", "Rich Fleider", "", "+ART", "Rich Fleider", "Steve Maines", "Won Choi", "", "+ANIMATION SEQUENCES", "Creat Studios", "Steve Maines", "", "+ADDITIONAL LEVEL DESIGN", "Rich Fleider", "Steve Maines", "Peter Mack", "", "+SOUND", "James Grunke", "", "+GROUND ZERO THEME", "+AND", "+MUSIC BY", "Sonic Mayhem", "", "+VWEP MODELS", "Brent \"Hentai\" Dill", "", "", "", "+SPECIAL THANKS", "+TO", "+OUR FRIENDS AT ID SOFTWARE", "", "John Carmack", "John Cash", "Brian Hook", "Adrian Carmack", "Kevin Cloud", "Paul Steed", "Tim Willits", "Christian Antkow", "Paul Jaquays", "Brandon James", "Todd Hollenshead", "Barrett (Bear) Alexander", "Katherine Anna Kang", "Donna Jackson", "Dave \"Zoid\" Kirsch", "", "", "", "+THANKS TO ACTIVISION", "+IN PARTICULAR:", "", "Marty Stratton", "Henk Hartong", "Mitch Lasky", "Steve Rosenthal", "Steve Elwell", "", "+AND THE GAME TESTERS", "", "The Ranger Clan", "Dave \"Zoid\" Kirsch", "Nihilistic Software", "Robert Duffy", "", "And Countless Others", "", "", "", "Quake II Mission Pack 2: Ground Zero", "(tm) (C)1998 Id Software, Inc. All", "Rights Reserved. Developed by Rogue", "Entertainment, Inc. for Id Software,", "Inc. Distributed by Activision Inc.", "under license. Quake(R) is a", "registered trademark of Id Software,", "Inc. Quake II Mission Pack 2: Ground", "Zero(tm), Quake II(tm), the Id", "Software name, the \"Q II\"(tm) logo", "and id(tm) logo are trademarks of Id", "Software, Inc. Activision(R) is a", "registered trademark of Activision,", "Inc. Rogue(R) is a registered", "trademark of Rogue Entertainment,", "Inc. All other trademarks and trade", "names are properties of their", "respective owners.", null)

        public fun Credits_MenuDraw() {
            var i: Int
            var y: Int

            /*
         * * draw the credits
         */
            run {
                i = 0
                y = (viddef.height - ((cls.realtime - credits_start_time) / 40.0.toFloat())) as Int
                while (credits[i] != null && y < viddef.height) {
                    var j: Int
                    var stringoffset = 0
                    var bold = false

                    if (y <= -8)
                        continue

                    if (credits[i].length() > 0 && credits[i].charAt(0) == '+') {
                        bold = true
                        stringoffset = 1
                    } else {
                        bold = false
                        stringoffset = 0
                    }

                    run {
                        j = 0
                        while (j + stringoffset < credits[i].length()) {
                            val x: Int

                            x = (viddef.width - credits[i].length() * 8 - stringoffset * 8) / 2 + (j + stringoffset) * 8

                            if (bold)
                                re.DrawChar(x, y, credits[i].charAt(j + stringoffset) + 128)
                            else
                                re.DrawChar(x, y, credits[i].charAt(j + stringoffset))
                            j++
                        }
                    }
                    y += 10
                    i++
                }
            }

            if (y < 0)
                credits_start_time = cls.realtime
        }

        public fun Credits_Key(key: Int): String {
            when (key) {
                K_ESCAPE -> {
                    if (creditsBuffer != null)
                    //FS.FreeFile(creditsBuffer);
                        PopMenu()
                }
            }

            return menu_out_sound

        }

        var Menu_Credits: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Credits_f()
            }
        }

        fun Menu_Credits_f() {
            var n: Int
            var isdeveloper = 0

            val b = FS.LoadFile("credits")

            if (b != null) {
                creditsBuffer = String(b)
                val line = creditsBuffer!!.split("\r\n")

                run {
                    n = 0
                    while (n < line.size()) {
                        creditsIndex[n] = line[n]
                        n++
                    }
                }

                creditsIndex[n] = null
                credits = creditsIndex
            } else {
                isdeveloper = FS.Developer_searchpath(1)

                if (isdeveloper == 1)
                // xatrix
                    credits = xatcredits
                else if (isdeveloper == 2)
                // ROGUE
                    credits = roguecredits
                else {
                    credits = idcredits
                }

            }

            credits_start_time = cls.realtime
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Credits_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Credits_Key(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * GAME MENU
     * 
     * =============================================================================
     */

        var m_game_cursor: Int = 0

        var s_game_menu = menuframework_s()

        var s_easy_game_action = menuaction_s()

        var s_medium_game_action = menuaction_s()

        var s_hard_game_action = menuaction_s()

        var s_load_game_action = menuaction_s()

        var s_save_game_action = menuaction_s()

        var s_credits_action = menuaction_s()

        var s_blankline = menuseparator_s()

        fun StartGame() {
            // disable updates and start the cinematic going
            cl.servercount = -1
            ForceMenuOff()
            Cvar.SetValue("deathmatch", 0)
            Cvar.SetValue("coop", 0)

            Cvar.SetValue("gamerules", 0) //PGM

            Cbuf.AddText("loading ; killserver ; wait ; newgame\n")
            cls.key_dest = key_game
        }

        fun EasyGameFunc(data: Object) {
            Cvar.ForceSet("skill", "0")
            StartGame()
        }

        fun MediumGameFunc(data: Object) {
            Cvar.ForceSet("skill", "1")
            StartGame()
        }

        fun HardGameFunc(data: Object) {
            Cvar.ForceSet("skill", "2")
            StartGame()
        }

        fun LoadGameFunc(unused: Object) {
            Menu_LoadGame_f()
        }

        fun SaveGameFunc(unused: Object) {
            Menu_SaveGame_f()
        }

        fun CreditsFunc(unused: Object) {
            Menu_Credits_f()
        }

        var difficulty_names = array<String>("easy", "medium", "fuckin shitty hard")

        fun Game_MenuInit() {

            s_game_menu.x = (viddef.width * 0.50) as Int
            s_game_menu.nitems = 0

            s_easy_game_action.type = MTYPE_ACTION
            s_easy_game_action.flags = QMF_LEFT_JUSTIFY
            s_easy_game_action.x = 0
            s_easy_game_action.y = 0
            s_easy_game_action.name = "easy"
            s_easy_game_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    EasyGameFunc(o)
                }
            }

            s_medium_game_action.type = MTYPE_ACTION
            s_medium_game_action.flags = QMF_LEFT_JUSTIFY
            s_medium_game_action.x = 0
            s_medium_game_action.y = 10
            s_medium_game_action.name = "medium"
            s_medium_game_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    MediumGameFunc(o)
                }
            }

            s_hard_game_action.type = MTYPE_ACTION
            s_hard_game_action.flags = QMF_LEFT_JUSTIFY
            s_hard_game_action.x = 0
            s_hard_game_action.y = 20
            s_hard_game_action.name = "hard"
            s_hard_game_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    HardGameFunc(o)
                }
            }

            s_blankline.type = MTYPE_SEPARATOR

            s_load_game_action.type = MTYPE_ACTION
            s_load_game_action.flags = QMF_LEFT_JUSTIFY
            s_load_game_action.x = 0
            s_load_game_action.y = 40
            s_load_game_action.name = "load game"
            s_load_game_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    LoadGameFunc(o)
                }
            }

            s_save_game_action.type = MTYPE_ACTION
            s_save_game_action.flags = QMF_LEFT_JUSTIFY
            s_save_game_action.x = 0
            s_save_game_action.y = 50
            s_save_game_action.name = "save game"
            s_save_game_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    SaveGameFunc(o)
                }
            }

            s_credits_action.type = MTYPE_ACTION
            s_credits_action.flags = QMF_LEFT_JUSTIFY
            s_credits_action.x = 0
            s_credits_action.y = 60
            s_credits_action.name = "credits"
            s_credits_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    CreditsFunc(o)
                }
            }

            Menu_AddItem(s_game_menu, s_easy_game_action)
            Menu_AddItem(s_game_menu, s_medium_game_action)
            Menu_AddItem(s_game_menu, s_hard_game_action)
            Menu_AddItem(s_game_menu, s_blankline)
            Menu_AddItem(s_game_menu, s_load_game_action)
            Menu_AddItem(s_game_menu, s_save_game_action)
            Menu_AddItem(s_game_menu, s_blankline)
            Menu_AddItem(s_game_menu, s_credits_action)

            Menu_Center(s_game_menu)
        }

        fun Game_MenuDraw() {
            Banner("m_banner_game")
            Menu_AdjustCursor(s_game_menu, 1)
            Menu_Draw(s_game_menu)
        }

        fun Game_MenuKey(key: Int): String {
            return Default_MenuKey(s_game_menu, key)
        }

        var Menu_Game: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Game_f()
            }
        }

        fun Menu_Game_f() {
            Game_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Game_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Game_MenuKey(key)
                }
            })
            m_game_cursor = 1
        }

        /*
     * =============================================================================
     * 
     * LOADGAME MENU
     * 
     * =============================================================================
     */

        public val MAX_SAVEGAMES: Int = 15

        var s_savegame_menu = menuframework_s()

        var s_loadgame_menu = menuframework_s()

        var s_loadgame_actions = arrayOfNulls<menuaction_s>(MAX_SAVEGAMES)

        {
            for (n in 0..MAX_SAVEGAMES - 1)
                s_loadgame_actions[n] = menuaction_s()
        }

        //String m_savestrings[] = new String [MAX_SAVEGAMES][32];
        var m_savestrings = arrayOfNulls<String>(MAX_SAVEGAMES)

        {
            for (n in 0..MAX_SAVEGAMES - 1)
                m_savestrings[n] = ""
        }

        var m_savevalid = BooleanArray(MAX_SAVEGAMES)

        /** Search the save dir for saved games and their names.  */
        fun Create_Savestrings() {
            var i: Int
            var f: QuakeFile
            var name: String

            run {
                i = 0
                while (i < MAX_SAVEGAMES) {

                    m_savestrings[i] = "<EMPTY>"
                    name = FS.Gamedir() + "/save/save" + i + "/server.ssv"

                    try {
                        f = QuakeFile(name, "r")
                        val str = f.readString()
                        if (str != null)
                            m_savestrings[i] = str
                        f.close()
                        m_savevalid[i] = true
                    } catch (e: Exception) {
                        m_savestrings[i] = "<EMPTY>"
                        m_savevalid[i] = false
                    }

                    i++
                }
            }
        }

        fun LoadGameCallback(self: Object) {
            val a = self as menuaction_s

            if (m_savevalid[a.localdata[0]])
                Cbuf.AddText("load save" + a.localdata[0] + "\n")
            ForceMenuOff()
        }

        fun LoadGame_MenuInit() {
            var i: Int

            s_loadgame_menu.x = viddef.width / 2 - 120
            s_loadgame_menu.y = viddef.height / 2 - 58
            s_loadgame_menu.nitems = 0

            Create_Savestrings()

            run {
                i = 0
                while (i < MAX_SAVEGAMES) {
                    s_loadgame_actions[i].name = m_savestrings[i]
                    s_loadgame_actions[i].flags = QMF_LEFT_JUSTIFY
                    s_loadgame_actions[i].localdata[0] = i
                    s_loadgame_actions[i].callback = object : mcallback() {
                        public fun execute(o: Object) {
                            LoadGameCallback(o)
                        }
                    }

                    s_loadgame_actions[i].x = 0
                    s_loadgame_actions[i].y = (i) * 10
                    if (i > 0)
                    // separate from autosave
                        s_loadgame_actions[i].y += 10

                    s_loadgame_actions[i].type = MTYPE_ACTION

                    Menu_AddItem(s_loadgame_menu, s_loadgame_actions[i])
                    i++
                }
            }
        }

        fun LoadGame_MenuDraw() {
            Banner("m_banner_load_game")
            //		Menu_AdjustCursor( &s_loadgame_menu, 1 );
            Menu_Draw(s_loadgame_menu)
        }

        fun LoadGame_MenuKey(key: Int): String {
            if (key == K_ESCAPE || key == K_ENTER) {
                s_savegame_menu.cursor = s_loadgame_menu.cursor - 1
                if (s_savegame_menu.cursor < 0)
                    s_savegame_menu.cursor = 0
            }
            return Default_MenuKey(s_loadgame_menu, key)
        }

        var Menu_LoadGame: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_LoadGame_f()
            }
        }

        fun Menu_LoadGame_f() {
            LoadGame_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    LoadGame_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return LoadGame_MenuKey(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * SAVEGAME MENU
     * 
     * =============================================================================
     */
        //static menuframework_s s_savegame_menu;
        var s_savegame_actions = arrayOfNulls<menuaction_s>(MAX_SAVEGAMES)

        {
            for (n in 0..MAX_SAVEGAMES - 1)
                s_savegame_actions[n] = menuaction_s()

        }

        fun SaveGameCallback(self: Object) {
            val a = self as menuaction_s

            Cbuf.AddText("save save" + a.localdata[0] + "\n")
            ForceMenuOff()
        }

        fun SaveGame_MenuDraw() {
            Banner("m_banner_save_game")
            Menu_AdjustCursor(s_savegame_menu, 1)
            Menu_Draw(s_savegame_menu)
        }

        fun SaveGame_MenuInit() {
            var i: Int

            s_savegame_menu.x = viddef.width / 2 - 120
            s_savegame_menu.y = viddef.height / 2 - 58
            s_savegame_menu.nitems = 0

            Create_Savestrings()

            // don't include the autosave slot
            run {
                i = 0
                while (i < MAX_SAVEGAMES - 1) {
                    s_savegame_actions[i].name = m_savestrings[i + 1]
                    s_savegame_actions[i].localdata[0] = i + 1
                    s_savegame_actions[i].flags = QMF_LEFT_JUSTIFY
                    s_savegame_actions[i].callback = object : mcallback() {
                        public fun execute(o: Object) {
                            SaveGameCallback(o)
                        }
                    }

                    s_savegame_actions[i].x = 0
                    s_savegame_actions[i].y = (i) * 10

                    s_savegame_actions[i].type = MTYPE_ACTION

                    Menu_AddItem(s_savegame_menu, s_savegame_actions[i])
                    i++
                }
            }
        }

        fun SaveGame_MenuKey(key: Int): String {
            if (key == K_ENTER || key == K_ESCAPE) {
                s_loadgame_menu.cursor = s_savegame_menu.cursor - 1
                if (s_loadgame_menu.cursor < 0)
                    s_loadgame_menu.cursor = 0
            }
            return Default_MenuKey(s_savegame_menu, key)
        }

        var Menu_SaveGame: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_SaveGame_f()
            }
        }

        fun Menu_SaveGame_f() {
            if (0 == Globals.server_state)
                return  // not playing a game

            SaveGame_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    SaveGame_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return SaveGame_MenuKey(key)
                }
            })
            Create_Savestrings()
        }

        /*
     * =============================================================================
     * 
     * JOIN SERVER MENU
     * 
     * =============================================================================
     */

        var s_joinserver_menu = menuframework_s()

        var s_joinserver_server_title = menuseparator_s()

        var s_joinserver_search_action = menuaction_s()

        var s_joinserver_address_book_action = menuaction_s()

        var local_server_netadr = arrayOfNulls<netadr_t>(MAX_LOCAL_SERVERS)

        var local_server_names = arrayOfNulls<String>(MAX_LOCAL_SERVERS) //[80];

        var s_joinserver_server_actions = arrayOfNulls<menuaction_s>(MAX_LOCAL_SERVERS)

        //	   user readable information
        //	   network address
        {
            for (n in 0..MAX_LOCAL_SERVERS - 1) {
                local_server_netadr[n] = netadr_t()
                local_server_names[n] = ""
                s_joinserver_server_actions[n] = menuaction_s()
                s_joinserver_server_actions[n].n = n
            }
        }

        var m_num_servers: Int = 0

        fun AddToServerList(adr: netadr_t, info: String) {
            var i: Int

            if (m_num_servers == MAX_LOCAL_SERVERS)
                return

            val x = info.trim()

            // ignore if duplicated

            run {
                i = 0
                while (i < m_num_servers) {
                    if (x.equals(local_server_names[i]))
                        return
                    i++
                }
            }

            local_server_netadr[m_num_servers].set(adr)
            local_server_names[m_num_servers] = x
            s_joinserver_server_actions[m_num_servers].name = x
            m_num_servers++
        }

        fun JoinServerFunc(self: Object) {
            val buffer: String
            val index: Int

            index = (self as menucommon_s).n

            if (Lib.Q_stricmp(local_server_names[index], NO_SERVER_STRING) == 0)
                return

            if (index >= m_num_servers)
                return

            buffer = "connect " + NET.AdrToString(local_server_netadr[index]) + "\n"
            Cbuf.AddText(buffer)
            ForceMenuOff()
        }

        fun AddressBookFunc(self: Object) {
            Menu_AddressBook_f()
        }

        fun NullCursorDraw(self: Object) {
        }

        fun SearchLocalGames() {
            var i: Int

            m_num_servers = 0
            run {
                i = 0
                while (i < MAX_LOCAL_SERVERS) {
                    local_server_names[i] = NO_SERVER_STRING
                    i++
                }
            }

            DrawTextBox(8, 120 - 48, 36, 3)
            Print(16 + 16, 120 - 48 + 8, "Searching for local servers, this")
            Print(16 + 16, 120 - 48 + 16, "could take up to a minute, so")
            Print(16 + 16, 120 - 48 + 24, "please be patient.")

            // the text box won't show up unless we do a buffer swap
            re.EndFrame()

            // send out info packets
            CL.PingServers_f.execute()
        }

        fun SearchLocalGamesFunc(self: Object) {
            SearchLocalGames()
        }

        fun JoinServer_MenuInit() {
            var i: Int

            s_joinserver_menu.x = (viddef.width * 0.50 - 120) as Int
            s_joinserver_menu.nitems = 0

            s_joinserver_address_book_action.type = MTYPE_ACTION
            s_joinserver_address_book_action.name = "address book"
            s_joinserver_address_book_action.flags = QMF_LEFT_JUSTIFY
            s_joinserver_address_book_action.x = 0
            s_joinserver_address_book_action.y = 0
            s_joinserver_address_book_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    AddressBookFunc(o)
                }
            }

            s_joinserver_search_action.type = MTYPE_ACTION
            s_joinserver_search_action.name = "refresh server list"
            s_joinserver_search_action.flags = QMF_LEFT_JUSTIFY
            s_joinserver_search_action.x = 0
            s_joinserver_search_action.y = 10
            s_joinserver_search_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    SearchLocalGamesFunc(o)
                }
            }
            s_joinserver_search_action.statusbar = "search for servers"

            s_joinserver_server_title.type = MTYPE_SEPARATOR
            s_joinserver_server_title.name = "connect to..."
            s_joinserver_server_title.x = 80
            s_joinserver_server_title.y = 30

            run {
                i = 0
                while (i < MAX_LOCAL_SERVERS) {
                    s_joinserver_server_actions[i].type = MTYPE_ACTION
                    local_server_names[i] = NO_SERVER_STRING
                    s_joinserver_server_actions[i].name = local_server_names[i]
                    s_joinserver_server_actions[i].flags = QMF_LEFT_JUSTIFY
                    s_joinserver_server_actions[i].x = 0
                    s_joinserver_server_actions[i].y = 40 + i * 10
                    s_joinserver_server_actions[i].callback = object : mcallback() {
                        public fun execute(o: Object) {
                            JoinServerFunc(o)
                        }
                    }
                    s_joinserver_server_actions[i].statusbar = "press ENTER to connect"
                    i++
                }
            }

            Menu_AddItem(s_joinserver_menu, s_joinserver_address_book_action)
            Menu_AddItem(s_joinserver_menu, s_joinserver_server_title)
            Menu_AddItem(s_joinserver_menu, s_joinserver_search_action)

            run {
                i = 0
                while (i < 8) {
                    Menu_AddItem(s_joinserver_menu, s_joinserver_server_actions[i])
                    i++
                }
            }

            Menu_Center(s_joinserver_menu)

            SearchLocalGames()
        }

        fun JoinServer_MenuDraw() {
            Banner("m_banner_join_server")
            Menu_Draw(s_joinserver_menu)
        }

        fun JoinServer_MenuKey(key: Int): String {
            return Default_MenuKey(s_joinserver_menu, key)
        }

        var Menu_JoinServer: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_JoinServer_f()
            }
        }

        fun Menu_JoinServer_f() {
            JoinServer_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    JoinServer_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return JoinServer_MenuKey(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * START SERVER MENU
     * 
     * =============================================================================
     */
        var s_startserver_menu = menuframework_s()

        var mapnames: Array<String>? = null

        var nummaps: Int = 0

        var s_startserver_start_action = menuaction_s()

        var s_startserver_dmoptions_action = menuaction_s()

        var s_timelimit_field = menufield_s()

        var s_fraglimit_field = menufield_s()

        var s_maxclients_field = menufield_s()

        var s_hostname_field = menufield_s()

        var s_startmap_list = menulist_s()

        var s_rules_box = menulist_s()

        fun DMOptionsFunc(self: Object) {
            if (s_rules_box.curvalue == 1)
                return
            Menu_DMOptions_f()
        }

        fun RulesChangeFunc(self: Object?) {
            // DM
            if (s_rules_box.curvalue == 0) {
                s_maxclients_field.statusbar = null
                s_startserver_dmoptions_action.statusbar = null
            } else if (s_rules_box.curvalue == 1)
            // coop // PGM
            {
                s_maxclients_field.statusbar = "4 maximum for cooperative"
                if (Lib.atoi(s_maxclients_field.buffer.toString()) > 4)
                    s_maxclients_field.buffer = StringBuffer("4")
                s_startserver_dmoptions_action.statusbar = "N/A for cooperative"
            } else if (FS.Developer_searchpath(2) == 2) {
                if (s_rules_box.curvalue == 2)
                // tag
                {
                    s_maxclients_field.statusbar = null
                    s_startserver_dmoptions_action.statusbar = null
                }
                /*
             * else if(s_rules_box.curvalue == 3) // deathball {
             * s_maxclients_field.statusbar = null;
             * s_startserver_dmoptions_action.statusbar = null; }
             */
            }//	  =====
            //	  PGM
            // ROGUE GAMES
            //	  PGM
            //	  =====
        }

        fun StartServerActionFunc(self: Object) {
            //char startmap[1024];
            val startmap: String
            val timelimit: Int
            val fraglimit: Int
            val maxclients: Int
            var spot: String?

            //strcpy(startmap, strchr(mapnames[s_startmap_list.curvalue], '\n') +
            // 1);
            val x = mapnames!![s_startmap_list.curvalue]

            val pos = x.indexOf('\n')
            if (pos == -1)
                startmap = x
            else
                startmap = x.substring(pos + 1, x.length())

            maxclients = Lib.atoi(s_maxclients_field.buffer.toString())
            timelimit = Lib.atoi(s_timelimit_field.buffer.toString())
            fraglimit = Lib.atoi(s_fraglimit_field.buffer.toString())

            Cvar.SetValue("maxclients", ClampCvar(0, maxclients.toFloat(), maxclients.toFloat()))
            Cvar.SetValue("timelimit", ClampCvar(0, timelimit.toFloat(), timelimit.toFloat()))
            Cvar.SetValue("fraglimit", ClampCvar(0, fraglimit.toFloat(), fraglimit.toFloat()))
            Cvar.Set("hostname", s_hostname_field.buffer.toString())
            //		Cvar.SetValue ("deathmatch", !s_rules_box.curvalue );
            //		Cvar.SetValue ("coop", s_rules_box.curvalue );

            //	  PGM
            if ((s_rules_box.curvalue < 2) || (FS.Developer_searchpath(2) != 2)) {
                Cvar.SetValue("deathmatch", 1 - (s_rules_box.curvalue).toInt())
                Cvar.SetValue("coop", s_rules_box.curvalue)
                Cvar.SetValue("gamerules", 0)
            } else {
                Cvar.SetValue("deathmatch", 1)
                // deathmatch is always true for rogue games, right?
                Cvar.SetValue("coop", 0)
                // FIXME - this might need to depend on which game we're running
                Cvar.SetValue("gamerules", s_rules_box.curvalue)
            }
            //	  PGM

            spot = null
            if (s_rules_box.curvalue == 1)
            // PGM
            {
                if (Lib.Q_stricmp(startmap, "bunk1") == 0)
                    spot = "start"
                else if (Lib.Q_stricmp(startmap, "mintro") == 0)
                    spot = "start"
                else if (Lib.Q_stricmp(startmap, "fact1") == 0)
                    spot = "start"
                else if (Lib.Q_stricmp(startmap, "power1") == 0)
                    spot = "pstart"
                else if (Lib.Q_stricmp(startmap, "biggun") == 0)
                    spot = "bstart"
                else if (Lib.Q_stricmp(startmap, "hangar1") == 0)
                    spot = "unitstart"
                else if (Lib.Q_stricmp(startmap, "city1") == 0)
                    spot = "unitstart"
                else if (Lib.Q_stricmp(startmap, "boss1") == 0)
                    spot = "bosstart"
            }

            if (spot != null) {
                if (Globals.server_state != 0)
                    Cbuf.AddText("disconnect\n")
                Cbuf.AddText("gamemap \"*" + startmap + "$" + spot + "\"\n")
            } else {
                Cbuf.AddText("map " + startmap + "\n")
            }

            ForceMenuOff()
        }

        var dm_coop_names = array<String>("deathmatch", "cooperative")

        var dm_coop_names_rogue = array<String>("deathmatch", "cooperative", "tag")

        fun StartServer_MenuInit() {

            //	  =======
            //	  PGM
            //	  =======

            var buffer: ByteArray? = null
            var mapsname: String
            val s: String
            var i: Int
            var fp: RandomAccessFile?

            /*
         * * load the list of map names
         */
            mapsname = FS.Gamedir() + "/maps.lst"

            // Check user dir first (default ~/.lwjake2)
            if ((fp = Lib.fopen(mapsname, "r")) == null) {
                // Check base dir first (baseq2 folder)
                mapsname = FS.BaseGamedir() + "/maps.lst"
                if ((fp = Lib.fopen(mapsname, "r")) == null) {
                    // Open the pak's maplist
                    buffer = FS.LoadFile("maps.lst")
                    if (buffer == null)
                        Com.Error(ERR_DROP, "couldn't find maps.lst\n")
                } else {
                    try {
                        val len = fp!!.length().toInt()
                        buffer = ByteArray(len)
                        fp!!.readFully(buffer)
                    } catch (e: Exception) {
                        Com.Error(ERR_DROP, "couldn't load maps.lst\n")
                    }

                }
            } else {
                try {
                    val len = fp!!.length().toInt()
                    buffer = ByteArray(len)
                    fp!!.readFully(buffer)
                } catch (e: Exception) {
                    Com.Error(ERR_DROP, "couldn't load maps.lst\n")
                }

            }

            s = String(buffer)
            val lines = s.split("\r\n")

            nummaps = lines.size()

            if (nummaps == 0)
                Com.Error(ERR_DROP, "no maps in maps.lst\n")

            mapnames = arrayOfNulls<String>(nummaps)

            run {
                i = 0
                while (i < nummaps) {
                    val shortname: String
                    val longname: String
                    val scratch: String

                    val ph = Com.ParseHelp(lines[i])

                    shortname = Com.Parse(ph).toUpperCase()
                    longname = Com.Parse(ph)
                    scratch = longname + "\n" + shortname
                    mapnames[i] = scratch
                    i++
                }
            }

            if (fp != null) {
                Lib.fclose(fp)
                fp = null

            } else {
                FS.FreeFile(buffer)
            }

            /*
         * * initialize the menu stuff
         */
            s_startserver_menu.x = (viddef.width * 0.50) as Int
            s_startserver_menu.nitems = 0

            s_startmap_list.type = MTYPE_SPINCONTROL
            s_startmap_list.x = 0
            s_startmap_list.y = 0
            s_startmap_list.name = "initial map"
            s_startmap_list.itemnames = mapnames

            s_rules_box.type = MTYPE_SPINCONTROL
            s_rules_box.x = 0
            s_rules_box.y = 20
            s_rules_box.name = "rules"

            //	  PGM - rogue games only available with rogue DLL.
            if (FS.Developer_searchpath(2) == 2)
                s_rules_box.itemnames = dm_coop_names_rogue
            else
                s_rules_box.itemnames = dm_coop_names
            //	  PGM

            if (Cvar.VariableValue("coop") != 0)
                s_rules_box.curvalue = 1
            else
                s_rules_box.curvalue = 0
            s_rules_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    RulesChangeFunc(o)
                }
            }

            s_timelimit_field.type = MTYPE_FIELD
            s_timelimit_field.name = "time limit"
            s_timelimit_field.flags = QMF_NUMBERSONLY
            s_timelimit_field.x = 0
            s_timelimit_field.y = 36
            s_timelimit_field.statusbar = "0 = no limit"
            s_timelimit_field.length = 3
            s_timelimit_field.visible_length = 3
            s_timelimit_field.buffer = StringBuffer(Cvar.VariableString("timelimit"))

            s_fraglimit_field.type = MTYPE_FIELD
            s_fraglimit_field.name = "frag limit"
            s_fraglimit_field.flags = QMF_NUMBERSONLY
            s_fraglimit_field.x = 0
            s_fraglimit_field.y = 54
            s_fraglimit_field.statusbar = "0 = no limit"
            s_fraglimit_field.length = 3
            s_fraglimit_field.visible_length = 3
            s_fraglimit_field.buffer = StringBuffer(Cvar.VariableString("fraglimit"))

            /*
         * * maxclients determines the maximum number of players that can join *
         * the game. If maxclients is only "1" then we should default the menu *
         * option to 8 players, otherwise use whatever its current value is. *
         * Clamping will be done when the server is actually started.
         */
            s_maxclients_field.type = MTYPE_FIELD
            s_maxclients_field.name = "max players"
            s_maxclients_field.flags = QMF_NUMBERSONLY
            s_maxclients_field.x = 0
            s_maxclients_field.y = 72
            s_maxclients_field.statusbar = null
            s_maxclients_field.length = 3
            s_maxclients_field.visible_length = 3
            if (Cvar.VariableValue("maxclients") == 1)
                s_maxclients_field.buffer = StringBuffer("8")
            else
                s_maxclients_field.buffer = StringBuffer(Cvar.VariableString("maxclients"))

            s_hostname_field.type = MTYPE_FIELD
            s_hostname_field.name = "hostname"
            s_hostname_field.flags = 0
            s_hostname_field.x = 0
            s_hostname_field.y = 90
            s_hostname_field.statusbar = null
            s_hostname_field.length = 12
            s_hostname_field.visible_length = 12
            s_hostname_field.buffer = StringBuffer(Cvar.VariableString("hostname"))
            s_hostname_field.cursor = s_hostname_field.buffer.length()

            s_startserver_dmoptions_action.type = MTYPE_ACTION
            s_startserver_dmoptions_action.name = " deathmatch flags"
            s_startserver_dmoptions_action.flags = QMF_LEFT_JUSTIFY
            s_startserver_dmoptions_action.x = 24
            s_startserver_dmoptions_action.y = 108
            s_startserver_dmoptions_action.statusbar = null
            s_startserver_dmoptions_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMOptionsFunc(o)
                }
            }

            s_startserver_start_action.type = MTYPE_ACTION
            s_startserver_start_action.name = " begin"
            s_startserver_start_action.flags = QMF_LEFT_JUSTIFY
            s_startserver_start_action.x = 24
            s_startserver_start_action.y = 128
            s_startserver_start_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    StartServerActionFunc(o)
                }
            }

            Menu_AddItem(s_startserver_menu, s_startmap_list)
            Menu_AddItem(s_startserver_menu, s_rules_box)
            Menu_AddItem(s_startserver_menu, s_timelimit_field)
            Menu_AddItem(s_startserver_menu, s_fraglimit_field)
            Menu_AddItem(s_startserver_menu, s_maxclients_field)
            Menu_AddItem(s_startserver_menu, s_hostname_field)
            Menu_AddItem(s_startserver_menu, s_startserver_dmoptions_action)
            Menu_AddItem(s_startserver_menu, s_startserver_start_action)

            Menu_Center(s_startserver_menu)

            // call this now to set proper inital state
            RulesChangeFunc(null)
        }

        fun StartServer_MenuDraw() {
            Menu_Draw(s_startserver_menu)
        }

        fun StartServer_MenuKey(key: Int): String {
            if (key == K_ESCAPE) {
                if (mapnames != null) {
                    var i: Int

                    run {
                        i = 0
                        while (i < nummaps) {
                            mapnames[i] = null
                            i++
                        }
                    }

                }
                mapnames = null
                nummaps = 0
            }

            return Default_MenuKey(s_startserver_menu, key)
        }

        var Menu_StartServer: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_StartServer_f()
            }
        }

        var startServer_MenuDraw: xcommand_t = object : xcommand_t() {
            public fun execute() {
                StartServer_MenuDraw()
            }
        }
        var startServer_MenuKey: keyfunc_t = object : keyfunc_t() {
            public fun execute(key: Int): String {
                return StartServer_MenuKey(key)
            }
        }

        fun Menu_StartServer_f() {
            StartServer_MenuInit()
            PushMenu(startServer_MenuDraw, startServer_MenuKey)
        }

        /*
     * =============================================================================
     * 
     * DMOPTIONS BOOK MENU
     * 
     * =============================================================================
     */
        var dmoptions_statusbar: String //[128];

        var s_dmoptions_menu = menuframework_s()

        var s_friendlyfire_box = menulist_s()

        var s_falls_box = menulist_s()

        var s_weapons_stay_box = menulist_s()

        var s_instant_powerups_box = menulist_s()

        var s_powerups_box = menulist_s()

        var s_health_box = menulist_s()

        var s_spawn_farthest_box = menulist_s()

        var s_teamplay_box = menulist_s()

        var s_samelevel_box = menulist_s()

        var s_force_respawn_box = menulist_s()

        var s_armor_box = menulist_s()

        var s_allow_exit_box = menulist_s()

        var s_infinite_ammo_box = menulist_s()

        var s_fixed_fov_box = menulist_s()

        var s_quad_drop_box = menulist_s()

        //	  ROGUE
        var s_no_mines_box = menulist_s()

        var s_no_nukes_box = menulist_s()

        var s_stack_double_box = menulist_s()

        var s_no_spheres_box = menulist_s()

        //	  ROGUE

        fun setvalue(flags: Int) {
            Cvar.SetValue("dmflags", flags)
            dmoptions_statusbar = "dmflags = " + flags
        }

        fun DMFlagCallback(self: Object?) {
            val f = self as menulist_s
            var flags: Int
            var bit = 0

            flags = Cvar.VariableValue("dmflags") as Int

            if (f == s_friendlyfire_box) {
                if (f.curvalue != 0)
                    flags = flags and DF_NO_FRIENDLY_FIRE.inv()
                else
                    flags = flags or DF_NO_FRIENDLY_FIRE
                setvalue(flags)
                return
            } else if (f == s_falls_box) {
                if (f.curvalue != 0)
                    flags = flags and DF_NO_FALLING.inv()
                else
                    flags = flags or DF_NO_FALLING
                setvalue(flags)
                return
            } else if (f == s_weapons_stay_box) {
                bit = DF_WEAPONS_STAY
            } else if (f == s_instant_powerups_box) {
                bit = DF_INSTANT_ITEMS
            } else if (f == s_allow_exit_box) {
                bit = DF_ALLOW_EXIT
            } else if (f == s_powerups_box) {
                if (f.curvalue != 0)
                    flags = flags and DF_NO_ITEMS.inv()
                else
                    flags = flags or DF_NO_ITEMS
                setvalue(flags)
                return
            } else if (f == s_health_box) {
                if (f.curvalue != 0)
                    flags = flags and DF_NO_HEALTH.inv()
                else
                    flags = flags or DF_NO_HEALTH
                setvalue(flags)
                return
            } else if (f == s_spawn_farthest_box) {
                bit = DF_SPAWN_FARTHEST
            } else if (f == s_teamplay_box) {
                if (f.curvalue == 1) {
                    flags = flags or DF_SKINTEAMS
                    flags = flags and DF_MODELTEAMS.inv()
                } else if (f.curvalue == 2) {
                    flags = flags or DF_MODELTEAMS
                    flags = flags and DF_SKINTEAMS.inv()
                } else {
                    flags = flags and (DF_MODELTEAMS or DF_SKINTEAMS).inv()
                }

                setvalue(flags)
                return
            } else if (f == s_samelevel_box) {
                bit = DF_SAME_LEVEL
            } else if (f == s_force_respawn_box) {
                bit = DF_FORCE_RESPAWN
            } else if (f == s_armor_box) {
                if (f.curvalue != 0)
                    flags = flags and DF_NO_ARMOR.inv()
                else
                    flags = flags or DF_NO_ARMOR
                setvalue(flags)
                return
            } else if (f == s_infinite_ammo_box) {
                bit = DF_INFINITE_AMMO
            } else if (f == s_fixed_fov_box) {
                bit = DF_FIXED_FOV
            } else if (f == s_quad_drop_box) {
                bit = DF_QUAD_DROP
            } else if (FS.Developer_searchpath(2) == 2) {
                if (f == s_no_mines_box) {
                    bit = DF_NO_MINES
                } else if (f == s_no_nukes_box) {
                    bit = DF_NO_NUKES
                } else if (f == s_stack_double_box) {
                    bit = DF_NO_STACK_DOUBLE
                } else if (f == s_no_spheres_box) {
                    bit = DF_NO_SPHERES
                }
            }//	  =======
            //	  ROGUE
            //	  ROGUE
            //	  =======

            if (f != null) {
                if (f.curvalue == 0)
                    flags = flags and bit.inv()
                else
                    flags = flags or bit
            }

            Cvar.SetValue("dmflags", flags)

            dmoptions_statusbar = "dmflags = " + flags

        }

        //static String yes_no_names[] = { "no", "yes", 0 };
        var teamplay_names = array<String>("disabled", "by skin", "by model")

        fun DMOptions_MenuInit() {

            val dmflags = Cvar.VariableValue("dmflags") as Int
            var y = 0

            s_dmoptions_menu.x = (viddef.width * 0.50) as Int
            s_dmoptions_menu.nitems = 0

            s_falls_box.type = MTYPE_SPINCONTROL
            s_falls_box.x = 0
            s_falls_box.y = y
            s_falls_box.name = "falling damage"
            s_falls_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_falls_box.itemnames = yes_no_names
            s_falls_box.curvalue = if ((dmflags and DF_NO_FALLING) == 0) 1 else 0

            s_weapons_stay_box.type = MTYPE_SPINCONTROL
            s_weapons_stay_box.x = 0
            s_weapons_stay_box.y = y += 10
            s_weapons_stay_box.name = "weapons stay"
            s_weapons_stay_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_weapons_stay_box.itemnames = yes_no_names
            s_weapons_stay_box.curvalue = if ((dmflags and DF_WEAPONS_STAY) != 0) 1 else 0

            s_instant_powerups_box.type = MTYPE_SPINCONTROL
            s_instant_powerups_box.x = 0
            s_instant_powerups_box.y = y += 10
            s_instant_powerups_box.name = "instant powerups"
            s_instant_powerups_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_instant_powerups_box.itemnames = yes_no_names
            s_instant_powerups_box.curvalue = if ((dmflags and DF_INSTANT_ITEMS) != 0)
                1
            else
                0

            s_powerups_box.type = MTYPE_SPINCONTROL
            s_powerups_box.x = 0
            s_powerups_box.y = y += 10
            s_powerups_box.name = "allow powerups"
            s_powerups_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_powerups_box.itemnames = yes_no_names
            s_powerups_box.curvalue = if ((dmflags and DF_NO_ITEMS) == 0) 1 else 0

            s_health_box.type = MTYPE_SPINCONTROL
            s_health_box.x = 0
            s_health_box.y = y += 10
            s_health_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_health_box.name = "allow health"
            s_health_box.itemnames = yes_no_names
            s_health_box.curvalue = if ((dmflags and DF_NO_HEALTH) == 0) 1 else 0

            s_armor_box.type = MTYPE_SPINCONTROL
            s_armor_box.x = 0
            s_armor_box.y = y += 10
            s_armor_box.name = "allow armor"
            s_armor_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_armor_box.itemnames = yes_no_names
            s_armor_box.curvalue = if ((dmflags and DF_NO_ARMOR) == 0) 1 else 0

            s_spawn_farthest_box.type = MTYPE_SPINCONTROL
            s_spawn_farthest_box.x = 0
            s_spawn_farthest_box.y = y += 10
            s_spawn_farthest_box.name = "spawn farthest"
            s_spawn_farthest_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_spawn_farthest_box.itemnames = yes_no_names
            s_spawn_farthest_box.curvalue = if ((dmflags and DF_SPAWN_FARTHEST) != 0)
                1
            else
                0

            s_samelevel_box.type = MTYPE_SPINCONTROL
            s_samelevel_box.x = 0
            s_samelevel_box.y = y += 10
            s_samelevel_box.name = "same map"
            s_samelevel_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_samelevel_box.itemnames = yes_no_names
            s_samelevel_box.curvalue = if ((dmflags and DF_SAME_LEVEL) != 0) 1 else 0

            s_force_respawn_box.type = MTYPE_SPINCONTROL
            s_force_respawn_box.x = 0
            s_force_respawn_box.y = y += 10
            s_force_respawn_box.name = "force respawn"
            s_force_respawn_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_force_respawn_box.itemnames = yes_no_names
            s_force_respawn_box.curvalue = if ((dmflags and DF_FORCE_RESPAWN) != 0)
                1
            else
                0

            s_teamplay_box.type = MTYPE_SPINCONTROL
            s_teamplay_box.x = 0
            s_teamplay_box.y = y += 10
            s_teamplay_box.name = "teamplay"
            s_teamplay_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_teamplay_box.itemnames = teamplay_names

            s_allow_exit_box.type = MTYPE_SPINCONTROL
            s_allow_exit_box.x = 0
            s_allow_exit_box.y = y += 10
            s_allow_exit_box.name = "allow exit"
            s_allow_exit_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_allow_exit_box.itemnames = yes_no_names
            s_allow_exit_box.curvalue = if ((dmflags and DF_ALLOW_EXIT) != 0) 1 else 0

            s_infinite_ammo_box.type = MTYPE_SPINCONTROL
            s_infinite_ammo_box.x = 0
            s_infinite_ammo_box.y = y += 10
            s_infinite_ammo_box.name = "infinite ammo"
            s_infinite_ammo_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_infinite_ammo_box.itemnames = yes_no_names
            s_infinite_ammo_box.curvalue = if ((dmflags and DF_INFINITE_AMMO) != 0)
                1
            else
                0

            s_fixed_fov_box.type = MTYPE_SPINCONTROL
            s_fixed_fov_box.x = 0
            s_fixed_fov_box.y = y += 10
            s_fixed_fov_box.name = "fixed FOV"
            s_fixed_fov_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_fixed_fov_box.itemnames = yes_no_names
            s_fixed_fov_box.curvalue = if ((dmflags and DF_FIXED_FOV) != 0) 1 else 0

            s_quad_drop_box.type = MTYPE_SPINCONTROL
            s_quad_drop_box.x = 0
            s_quad_drop_box.y = y += 10
            s_quad_drop_box.name = "quad drop"
            s_quad_drop_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_quad_drop_box.itemnames = yes_no_names
            s_quad_drop_box.curvalue = if ((dmflags and DF_QUAD_DROP) != 0) 1 else 0

            s_friendlyfire_box.type = MTYPE_SPINCONTROL
            s_friendlyfire_box.x = 0
            s_friendlyfire_box.y = y += 10
            s_friendlyfire_box.name = "friendly fire"
            s_friendlyfire_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DMFlagCallback(o)
                }
            }
            s_friendlyfire_box.itemnames = yes_no_names
            s_friendlyfire_box.curvalue = if ((dmflags and DF_NO_FRIENDLY_FIRE) == 0)
                1
            else
                0

            //	  ============
            //	  ROGUE
            if (FS.Developer_searchpath(2) == 2) {
                s_no_mines_box.type = MTYPE_SPINCONTROL
                s_no_mines_box.x = 0
                s_no_mines_box.y = y += 10
                s_no_mines_box.name = "remove mines"
                s_no_mines_box.callback = object : mcallback() {
                    public fun execute(o: Object) {
                        DMFlagCallback(o)
                    }
                }
                s_no_mines_box.itemnames = yes_no_names
                s_no_mines_box.curvalue = if ((dmflags and DF_NO_MINES) != 0) 1 else 0

                s_no_nukes_box.type = MTYPE_SPINCONTROL
                s_no_nukes_box.x = 0
                s_no_nukes_box.y = y += 10
                s_no_nukes_box.name = "remove nukes"
                s_no_nukes_box.callback = object : mcallback() {
                    public fun execute(o: Object) {
                        DMFlagCallback(o)
                    }
                }
                s_no_nukes_box.itemnames = yes_no_names
                s_no_nukes_box.curvalue = if ((dmflags and DF_NO_NUKES) != 0) 1 else 0

                s_stack_double_box.type = MTYPE_SPINCONTROL
                s_stack_double_box.x = 0
                s_stack_double_box.y = y += 10
                s_stack_double_box.name = "2x/4x stacking off"
                s_stack_double_box.callback = object : mcallback() {
                    public fun execute(o: Object) {
                        DMFlagCallback(o)
                    }
                }
                s_stack_double_box.itemnames = yes_no_names
                s_stack_double_box.curvalue = (dmflags and DF_NO_STACK_DOUBLE)

                s_no_spheres_box.type = MTYPE_SPINCONTROL
                s_no_spheres_box.x = 0
                s_no_spheres_box.y = y += 10
                s_no_spheres_box.name = "remove spheres"
                s_no_spheres_box.callback = object : mcallback() {
                    public fun execute(o: Object) {
                        DMFlagCallback(o)
                    }
                }
                s_no_spheres_box.itemnames = yes_no_names
                s_no_spheres_box.curvalue = if ((dmflags and DF_NO_SPHERES) != 0) 1 else 0

            }
            //	  ROGUE
            //	  ============

            Menu_AddItem(s_dmoptions_menu, s_falls_box)
            Menu_AddItem(s_dmoptions_menu, s_weapons_stay_box)
            Menu_AddItem(s_dmoptions_menu, s_instant_powerups_box)
            Menu_AddItem(s_dmoptions_menu, s_powerups_box)
            Menu_AddItem(s_dmoptions_menu, s_health_box)
            Menu_AddItem(s_dmoptions_menu, s_armor_box)
            Menu_AddItem(s_dmoptions_menu, s_spawn_farthest_box)
            Menu_AddItem(s_dmoptions_menu, s_samelevel_box)
            Menu_AddItem(s_dmoptions_menu, s_force_respawn_box)
            Menu_AddItem(s_dmoptions_menu, s_teamplay_box)
            Menu_AddItem(s_dmoptions_menu, s_allow_exit_box)
            Menu_AddItem(s_dmoptions_menu, s_infinite_ammo_box)
            Menu_AddItem(s_dmoptions_menu, s_fixed_fov_box)
            Menu_AddItem(s_dmoptions_menu, s_quad_drop_box)
            Menu_AddItem(s_dmoptions_menu, s_friendlyfire_box)

            //	  =======
            //	  ROGUE
            if (FS.Developer_searchpath(2) == 2) {
                Menu_AddItem(s_dmoptions_menu, s_no_mines_box)
                Menu_AddItem(s_dmoptions_menu, s_no_nukes_box)
                Menu_AddItem(s_dmoptions_menu, s_stack_double_box)
                Menu_AddItem(s_dmoptions_menu, s_no_spheres_box)
            }
            //	  ROGUE
            //	  =======

            Menu_Center(s_dmoptions_menu)

            // set the original dmflags statusbar
            DMFlagCallback(null)
            Menu_SetStatusBar(s_dmoptions_menu, dmoptions_statusbar)
        }

        fun DMOptions_MenuDraw() {
            Menu_Draw(s_dmoptions_menu)
        }

        fun DMOptions_MenuKey(key: Int): String {
            return Default_MenuKey(s_dmoptions_menu, key)
        }

        var Menu_DMOptions: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_DMOptions_f()
            }
        }

        fun Menu_DMOptions_f() {
            DMOptions_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    DMOptions_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return DMOptions_MenuKey(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * DOWNLOADOPTIONS BOOK MENU
     * 
     * =============================================================================
     */
        var s_downloadoptions_menu = menuframework_s()

        var s_download_title = menuseparator_s()

        var s_allow_download_box = menulist_s()

        var s_allow_download_maps_box = menulist_s()

        var s_allow_download_models_box = menulist_s()

        var s_allow_download_players_box = menulist_s()

        var s_allow_download_sounds_box = menulist_s()

        fun DownloadCallback(self: Object) {
            val f = self as menulist_s

            if (f == s_allow_download_box) {
                Cvar.SetValue("allow_download", f.curvalue)
            } else if (f == s_allow_download_maps_box) {
                Cvar.SetValue("allow_download_maps", f.curvalue)
            } else if (f == s_allow_download_models_box) {
                Cvar.SetValue("allow_download_models", f.curvalue)
            } else if (f == s_allow_download_players_box) {
                Cvar.SetValue("allow_download_players", f.curvalue)
            } else if (f == s_allow_download_sounds_box) {
                Cvar.SetValue("allow_download_sounds", f.curvalue)
            }
        }

        var yes_no_names = array<String>("no", "yes")

        fun DownloadOptions_MenuInit() {

            var y = 0

            s_downloadoptions_menu.x = (viddef.width * 0.50) as Int
            s_downloadoptions_menu.nitems = 0

            s_download_title.type = MTYPE_SEPARATOR
            s_download_title.name = "Download Options"
            s_download_title.x = 48
            s_download_title.y = y

            s_allow_download_box.type = MTYPE_SPINCONTROL
            s_allow_download_box.x = 0
            s_allow_download_box.y = y += 20
            s_allow_download_box.name = "allow downloading"
            s_allow_download_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadCallback(o)
                }
            }
            s_allow_download_box.itemnames = yes_no_names
            s_allow_download_box.curvalue = if ((Cvar.VariableValue("allow_download") != 0))
                1
            else
                0

            s_allow_download_maps_box.type = MTYPE_SPINCONTROL
            s_allow_download_maps_box.x = 0
            s_allow_download_maps_box.y = y += 20
            s_allow_download_maps_box.name = "maps"
            s_allow_download_maps_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadCallback(o)
                }
            }
            s_allow_download_maps_box.itemnames = yes_no_names
            s_allow_download_maps_box.curvalue = if ((Cvar.VariableValue("allow_download_maps") != 0))
                1
            else
                0

            s_allow_download_players_box.type = MTYPE_SPINCONTROL
            s_allow_download_players_box.x = 0
            s_allow_download_players_box.y = y += 10
            s_allow_download_players_box.name = "player models/skins"
            s_allow_download_players_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadCallback(o)
                }
            }
            s_allow_download_players_box.itemnames = yes_no_names
            s_allow_download_players_box.curvalue = if ((Cvar.VariableValue("allow_download_players") != 0))
                1
            else
                0

            s_allow_download_models_box.type = MTYPE_SPINCONTROL
            s_allow_download_models_box.x = 0
            s_allow_download_models_box.y = y += 10
            s_allow_download_models_box.name = "models"
            s_allow_download_models_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadCallback(o)
                }
            }
            s_allow_download_models_box.itemnames = yes_no_names
            s_allow_download_models_box.curvalue = if ((Cvar.VariableValue("allow_download_models") != 0))
                1
            else
                0

            s_allow_download_sounds_box.type = MTYPE_SPINCONTROL
            s_allow_download_sounds_box.x = 0
            s_allow_download_sounds_box.y = y += 10
            s_allow_download_sounds_box.name = "sounds"
            s_allow_download_sounds_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadCallback(o)
                }
            }
            s_allow_download_sounds_box.itemnames = yes_no_names
            s_allow_download_sounds_box.curvalue = if ((Cvar.VariableValue("allow_download_sounds") != 0))
                1
            else
                0

            Menu_AddItem(s_downloadoptions_menu, s_download_title)
            Menu_AddItem(s_downloadoptions_menu, s_allow_download_box)
            Menu_AddItem(s_downloadoptions_menu, s_allow_download_maps_box)
            Menu_AddItem(s_downloadoptions_menu, s_allow_download_players_box)
            Menu_AddItem(s_downloadoptions_menu, s_allow_download_models_box)
            Menu_AddItem(s_downloadoptions_menu, s_allow_download_sounds_box)

            Menu_Center(s_downloadoptions_menu)

            // skip over title
            if (s_downloadoptions_menu.cursor == 0)
                s_downloadoptions_menu.cursor = 1
        }

        fun DownloadOptions_MenuDraw() {
            Menu_Draw(s_downloadoptions_menu)
        }

        fun DownloadOptions_MenuKey(key: Int): String {
            return Default_MenuKey(s_downloadoptions_menu, key)
        }

        var Menu_DownloadOptions: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_DownloadOptions_f()
            }
        }

        fun Menu_DownloadOptions_f() {
            DownloadOptions_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    DownloadOptions_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return DownloadOptions_MenuKey(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * ADDRESS BOOK MENU
     * 
     * =============================================================================
     */

        var s_addressbook_menu = menuframework_s()

        var s_addressbook_fields = arrayOfNulls<menufield_s>(NUM_ADDRESSBOOK_ENTRIES)
        {
            for (n in 0..NUM_ADDRESSBOOK_ENTRIES - 1)
                s_addressbook_fields[n] = menufield_s()
        }

        fun AddressBook_MenuInit() {
            s_addressbook_menu.x = viddef.width / 2 - 142
            s_addressbook_menu.y = viddef.height / 2 - 58
            s_addressbook_menu.nitems = 0

            for (i in 0..NUM_ADDRESSBOOK_ENTRIES - 1) {
                val adr = Cvar.Get("adr" + i, "", CVAR_ARCHIVE)

                s_addressbook_fields[i].type = MTYPE_FIELD
                s_addressbook_fields[i].name = null
                s_addressbook_fields[i].callback = null
                s_addressbook_fields[i].x = 0
                s_addressbook_fields[i].y = i * 18 + 0
                s_addressbook_fields[i].localdata[0] = i
                // put the cursor to the end of text for editing
                s_addressbook_fields[i].cursor = adr.string.length()
                s_addressbook_fields[i].length = 60
                s_addressbook_fields[i].visible_length = 30

                s_addressbook_fields[i].buffer = StringBuffer(adr.string)

                Menu_AddItem(s_addressbook_menu, s_addressbook_fields[i])
            }
        }

        var AddressBook_MenuKey: keyfunc_t = object : keyfunc_t() {
            public fun execute(key: Int): String {
                return AddressBook_MenuKey_f(key)
            }
        }

        fun AddressBook_MenuKey_f(key: Int): String {
            if (key == K_ESCAPE) {
                for (index in 0..NUM_ADDRESSBOOK_ENTRIES - 1) {
                    Cvar.Set("adr" + index, s_addressbook_fields[index].buffer.toString())
                }
            }
            return Default_MenuKey(s_addressbook_menu, key)
        }

        var AddressBook_MenuDraw: xcommand_t = object : xcommand_t() {
            public fun execute() {
                AddressBook_MenuDraw_f()
            }
        }

        fun AddressBook_MenuDraw_f() {
            Banner("m_banner_addressbook")
            Menu_Draw(s_addressbook_menu)
        }

        var Menu_AddressBook: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_AddressBook_f()
            }
        }

        fun Menu_AddressBook_f() {
            AddressBook_MenuInit()
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    AddressBook_MenuDraw_f()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return AddressBook_MenuKey_f(key)
                }
            })
        }

        /*
     * =============================================================================
     * 
     * PLAYER CONFIG MENU
     * 
     * =============================================================================
     */
        var s_player_config_menu = menuframework_s()

        var s_player_name_field = menufield_s()

        var s_player_model_box = menulist_s()

        var s_player_skin_box = menulist_s()

        var s_player_handedness_box = menulist_s()

        var s_player_rate_box = menulist_s()

        var s_player_skin_title = menuseparator_s()

        var s_player_model_title = menuseparator_s()

        var s_player_hand_title = menuseparator_s()

        var s_player_rate_title = menuseparator_s()

        var s_player_download_action = menuaction_s()

        var s_pmi = arrayOfNulls<playermodelinfo_s>(MAX_PLAYERMODELS)

        var s_pmnames = arrayOfNulls<String>(MAX_PLAYERMODELS)

        var s_numplayermodels: Int = 0

        var rate_tbl = intArray(2500, 3200, 5000, 10000, 25000, 0)

        var rate_names = array<String>("28.8 Modem", "33.6 Modem", "Single ISDN", "Dual ISDN/Cable", "T1/LAN", "User defined")

        fun DownloadOptionsFunc(self: Object) {
            Menu_DownloadOptions_f()
        }

        fun HandednessCallback(unused: Object) {
            Cvar.SetValue("hand", s_player_handedness_box.curvalue)
        }

        fun RateCallback(unused: Object) {
            if (s_player_rate_box.curvalue != rate_tbl.size() - 1)
            //sizeof(rate_tbl)
            // / sizeof(*
            // rate_tbl) - 1)
                Cvar.SetValue("rate", rate_tbl[s_player_rate_box.curvalue])
        }

        fun ModelCallback(unused: Object) {
            s_player_skin_box.itemnames = s_pmi[s_player_model_box.curvalue].skindisplaynames
            s_player_skin_box.curvalue = 0
        }

        fun IconOfSkinExists(skin: String, pcxfiles: Array<String>, npcxfiles: Int): Boolean {

            var scratch: String

            //strcpy(scratch, skin);
            scratch = skin
            val pos = scratch.lastIndexOf('.')
            if (pos != -1)
                scratch = scratch.substring(0, pos) + "_i.pcx"
            else
                scratch += "_i.pcx"

            for (i in 0..npcxfiles - 1) {
                if (pcxfiles[i].equals(scratch))
                    return true
            }

            return false
        }

        fun PlayerConfig_ScanDirectories(): Boolean {
            //char findname[1024];
            val findname: String
            //char scratch[1024];
            var scratch: String

            var ndirs = 0
            var npms = 0
            var a: Int
            var b: Int
            var c: Int
            val dirnames: Array<String>?

            var path: String? = null

            var i: Int

            //extern String * FS_ListFiles(String , int *, unsigned, unsigned);

            s_numplayermodels = 0

            /*
         * * get a list of directories
         */
            do {
                path = FS.NextPath(path)
                findname = path + "/players/*.*"

                if ((dirnames = FS.ListFiles(findname, 0, SFF_SUBDIR)) != null) {
                    ndirs = dirnames!!.size()
                    break
                }
            } while (path != null)

            if (dirnames == null)
                return false

            /*
         * * go through the subdirectories
         */
            npms = ndirs
            if (npms > MAX_PLAYERMODELS)
                npms = MAX_PLAYERMODELS

            run {
                i = 0
                while (i < npms) {
                    var k: Int
                    var s: Int
                    //String a, b, c;
                    val pcxnames: Array<String>
                    val skinnames: Array<String>
                    val npcxfiles: Int
                    var nskins = 0

                    if (dirnames[i] == null)
                        continue

                    // verify the existence of tris.md2
                    scratch = dirnames[i]
                    scratch += "/tris.md2"
                    if (Sys.FindFirst(scratch, 0, SFF_SUBDIR or SFF_HIDDEN or SFF_SYSTEM) == null) {
                        //free(dirnames[i]);
                        dirnames[i] = null
                        Sys.FindClose()
                        continue
                    }
                    Sys.FindClose()

                    // verify the existence of at least one pcx skin
                    scratch = dirnames[i] + "/*.pcx"
                    pcxnames = FS.ListFiles(scratch, 0, 0)
                    npcxfiles = pcxnames.size()

                    // count valid skins, which consist of a skin with a matching "_i"
                    // icon
                    run {
                        k = 0
                        while (k < npcxfiles - 1) {
                            if (!pcxnames[k].endsWith("_i.pcx")) {
                                //if (!strstr(pcxnames[k], "_i.pcx")) {
                                if (IconOfSkinExists(pcxnames[k], pcxnames, npcxfiles)) {
                                    nskins++
                                }
                            }
                            k++
                        }
                    }
                    if (nskins == 0)
                        continue

                    skinnames = arrayOfNulls<String>(nskins + 1) //malloc(sizeof(String) *
                    // (nskins + 1));
                    //memset(skinnames, 0, sizeof(String) * (nskins + 1));

                    // copy the valid skins
                    run {
                        s = 0
                        k = 0
                        while (k < npcxfiles) {

                            if (pcxnames[k].indexOf("_i.pcx") < 0) {
                                if (IconOfSkinExists(pcxnames[k], pcxnames, npcxfiles)) {
                                    a = pcxnames[k].lastIndexOf('/')
                                    b = pcxnames[k].lastIndexOf('\\')

                                    if (a > b)
                                        c = a
                                    else
                                        c = b

                                    scratch = pcxnames[k].substring(c + 1, pcxnames[k].length())
                                    val pos = scratch.lastIndexOf('.')
                                    if (pos != -1)
                                        scratch = scratch.substring(0, pos)

                                    skinnames[s] = scratch
                                    s++
                                }
                            }
                            k++
                        }
                    }

                    // at this point we have a valid player model
                    if (s_pmi[s_numplayermodels] == null)
                        s_pmi[s_numplayermodels] = playermodelinfo_s()

                    s_pmi[s_numplayermodels].nskins = nskins
                    s_pmi[s_numplayermodels].skindisplaynames = skinnames

                    // make short name for the model
                    a = dirnames[i].lastIndexOf('/')
                    b = dirnames[i].lastIndexOf('\\')

                    if (a > b)
                        c = a
                    else
                        c = b

                    s_pmi[s_numplayermodels].displayname = dirnames[i].substring(c + 1)
                    s_pmi[s_numplayermodels].directory = dirnames[i].substring(c + 1)

                    s_numplayermodels++
                    i++
                }
            }

            return true

        }

        fun pmicmpfnc(a: playermodelinfo_s, b: playermodelinfo_s): Int {

            /*
         * * sort by male, female, then alphabetical
         */
            if (a.directory.equals("male"))
                return -1
            else if (b.directory.equals("male"))
                return 1

            if (a.directory.equals("female"))
                return -1
            else if (b.directory.equals("female"))
                return 1

            return a.directory.compareTo(b.directory)
        }

        var handedness = array<String>("right", "left", "center")

        fun PlayerConfig_MenuInit(): Boolean {
            /*
         * extern cvar_t * name; extern cvar_t * team; extern cvar_t * skin;
         */
            //har currentdirectory[1024];
            var currentdirectory: String
            //char currentskin[1024];
            val currentskin: String

            var i = 0

            var currentdirectoryindex = 0
            var currentskinindex = 0

            val hand = Cvar.Get("hand", "0", CVAR_USERINFO or CVAR_ARCHIVE)

            PlayerConfig_ScanDirectories()

            if (s_numplayermodels == 0)
                return false

            if (hand.value < 0 || hand.value > 2)
                Cvar.SetValue("hand", 0)

            currentdirectory = skin.string

            if (currentdirectory.lastIndexOf('/') != -1) {
                currentskin = Lib.rightFrom(currentdirectory, '/')
                currentdirectory = Lib.leftFrom(currentdirectory, '/')
            } else if (currentdirectory.lastIndexOf('\\') != -1) {
                currentskin = Lib.rightFrom(currentdirectory, '\\')
                currentdirectory = Lib.leftFrom(currentdirectory, '\\')
            } else {
                currentdirectory = "male"
                currentskin = "grunt"
            }

            //qsort(s_pmi, s_numplayermodels, sizeof(s_pmi[0]), pmicmpfnc);
            Arrays.sort<playermodelinfo_s>(s_pmi, 0, s_numplayermodels, object : Comparator<playermodelinfo_s> {
                override fun compare(o1: playermodelinfo_s, o2: playermodelinfo_s): Int {
                    return pmicmpfnc(o1, o2)
                }
            })

            //memset(s_pmnames, 0, sizeof(s_pmnames));
            s_pmnames = arrayOfNulls<String>(MAX_PLAYERMODELS)

            run {
                i = 0
                while (i < s_numplayermodels) {
                    s_pmnames[i] = s_pmi[i].displayname
                    if (Lib.Q_stricmp(s_pmi[i].directory, currentdirectory) == 0) {
                        var j: Int

                        currentdirectoryindex = i

                        run {
                            j = 0
                            while (j < s_pmi[i].nskins) {
                                if (Lib.Q_stricmp(s_pmi[i].skindisplaynames!![j], currentskin) == 0) {
                                    currentskinindex = j
                                    break
                                }
                                j++
                            }
                        }
                    }
                    i++
                }
            }

            s_player_config_menu.x = viddef.width / 2 - 95
            s_player_config_menu.y = viddef.height / 2 - 97
            s_player_config_menu.nitems = 0

            s_player_name_field.type = MTYPE_FIELD
            s_player_name_field.name = "name"
            s_player_name_field.callback = null
            s_player_name_field.x = 0
            s_player_name_field.y = 0
            s_player_name_field.length = 20
            s_player_name_field.visible_length = 20
            s_player_name_field.buffer = StringBuffer(name.string)
            s_player_name_field.cursor = name.string.length()

            s_player_model_title.type = MTYPE_SEPARATOR
            s_player_model_title.name = "model"
            s_player_model_title.x = -8
            s_player_model_title.y = 60

            s_player_model_box.type = MTYPE_SPINCONTROL
            s_player_model_box.x = -56
            s_player_model_box.y = 70
            s_player_model_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    ModelCallback(o)
                }
            }
            s_player_model_box.cursor_offset = -48
            s_player_model_box.curvalue = currentdirectoryindex
            s_player_model_box.itemnames = s_pmnames

            s_player_skin_title.type = MTYPE_SEPARATOR
            s_player_skin_title.name = "skin"
            s_player_skin_title.x = -16
            s_player_skin_title.y = 84

            s_player_skin_box.type = MTYPE_SPINCONTROL
            s_player_skin_box.x = -56
            s_player_skin_box.y = 94
            s_player_skin_box.name = null
            s_player_skin_box.callback = null
            s_player_skin_box.cursor_offset = -48
            s_player_skin_box.curvalue = currentskinindex
            s_player_skin_box.itemnames = s_pmi[currentdirectoryindex].skindisplaynames

            s_player_hand_title.type = MTYPE_SEPARATOR
            s_player_hand_title.name = "handedness"
            s_player_hand_title.x = 32
            s_player_hand_title.y = 108

            s_player_handedness_box.type = MTYPE_SPINCONTROL
            s_player_handedness_box.x = -56
            s_player_handedness_box.y = 118
            s_player_handedness_box.name = null
            s_player_handedness_box.cursor_offset = -48
            s_player_handedness_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    HandednessCallback(o)
                }
            }
            s_player_handedness_box.curvalue = Cvar.VariableValue("hand") as Int
            s_player_handedness_box.itemnames = handedness

            run {
                i = 0
                while (i < rate_tbl.size() - 1) {
                    if (Cvar.VariableValue("rate") == rate_tbl[i])
                        break
                    i++
                }
            }

            s_player_rate_title.type = MTYPE_SEPARATOR
            s_player_rate_title.name = "connect speed"
            s_player_rate_title.x = 56
            s_player_rate_title.y = 156

            s_player_rate_box.type = MTYPE_SPINCONTROL
            s_player_rate_box.x = -56
            s_player_rate_box.y = 166
            s_player_rate_box.name = null
            s_player_rate_box.cursor_offset = -48
            s_player_rate_box.callback = object : mcallback() {
                public fun execute(o: Object) {
                    RateCallback(o)
                }
            }
            s_player_rate_box.curvalue = i
            s_player_rate_box.itemnames = rate_names

            s_player_download_action.type = MTYPE_ACTION
            s_player_download_action.name = "download options"
            s_player_download_action.flags = QMF_LEFT_JUSTIFY
            s_player_download_action.x = -24
            s_player_download_action.y = 186
            s_player_download_action.statusbar = null
            s_player_download_action.callback = object : mcallback() {
                public fun execute(o: Object) {
                    DownloadOptionsFunc(o)
                }
            }

            Menu_AddItem(s_player_config_menu, s_player_name_field)
            Menu_AddItem(s_player_config_menu, s_player_model_title)
            Menu_AddItem(s_player_config_menu, s_player_model_box)
            if (s_player_skin_box.itemnames != null) {
                Menu_AddItem(s_player_config_menu, s_player_skin_title)
                Menu_AddItem(s_player_config_menu, s_player_skin_box)
            }
            Menu_AddItem(s_player_config_menu, s_player_hand_title)
            Menu_AddItem(s_player_config_menu, s_player_handedness_box)
            Menu_AddItem(s_player_config_menu, s_player_rate_title)
            Menu_AddItem(s_player_config_menu, s_player_rate_box)
            Menu_AddItem(s_player_config_menu, s_player_download_action)

            return true
        }

        var yaw: Int = 0

        private val entity = entity_t()

        fun PlayerConfig_MenuDraw() {

            val refdef = refdef_t()
            //char scratch[MAX_QPATH];
            var scratch: String

            //memset(refdef, 0, sizeof(refdef));

            refdef.x = viddef.width / 2
            refdef.y = viddef.height / 2 - 72
            refdef.width = 144
            refdef.height = 168
            refdef.fov_x = 40
            refdef.fov_y = Math3D.CalcFov(refdef.fov_x, refdef.width, refdef.height)
            refdef.time = cls.realtime * 0.001.toFloat()

            if (s_pmi[s_player_model_box.curvalue].skindisplaynames != null) {

                entity.clear()

                scratch = "players/" + s_pmi[s_player_model_box.curvalue].directory + "/tris.md2"

                entity.model = re.RegisterModel(scratch)

                scratch = "players/" + s_pmi[s_player_model_box.curvalue].directory + "/" + s_pmi[s_player_model_box.curvalue].skindisplaynames!![s_player_skin_box.curvalue] + ".pcx"

                entity.skin = re.RegisterSkin(scratch)
                entity.flags = RF_FULLBRIGHT
                entity.origin[0] = 80
                entity.origin[1] = 0
                entity.origin[2] = 0
                Math3D.VectorCopy(entity.origin, entity.oldorigin)
                entity.frame = 0
                entity.oldframe = 0
                entity.backlerp = 0.0.toFloat()
                entity.angles[1] = yaw++
                if (++yaw > 360)
                    yaw -= 360

                refdef.areabits = null
                refdef.num_entities = 1
                refdef.entities = array<entity_t>(entity)
                refdef.lightstyles = null
                refdef.rdflags = RDF_NOWORLDMODEL

                Menu_Draw(s_player_config_menu)

                DrawTextBox(((refdef.x) * (320.0.toFloat() / viddef.width) - 8) as Int, ((viddef.height / 2) * (240.0.toFloat() / viddef.height) - 77) as Int, refdef.width / 8, refdef.height / 8)
                refdef.height += 4

                re.RenderFrame(refdef)

                scratch = "/players/" + s_pmi[s_player_model_box.curvalue].directory + "/" + s_pmi[s_player_model_box.curvalue].skindisplaynames!![s_player_skin_box.curvalue] + "_i.pcx"

                re.DrawPic(s_player_config_menu.x - 40, refdef.y, scratch)
            }
        }

        fun PlayerConfig_MenuKey(key: Int): String {
            var i: Int

            if (key == K_ESCAPE) {
                val scratch: String

                Cvar.Set("name", s_player_name_field.buffer.toString())

                scratch = s_pmi[s_player_model_box.curvalue].directory + "/" + s_pmi[s_player_model_box.curvalue].skindisplaynames!![s_player_skin_box.curvalue]

                Cvar.Set("skin", scratch)

                run {
                    i = 0
                    while (i < s_numplayermodels) {
                        var j: Int

                        run {
                            j = 0
                            while (j < s_pmi[i].nskins) {
                                if (s_pmi[i].skindisplaynames!![j] != null)
                                    s_pmi[i].skindisplaynames[j] = null
                                j++
                            }
                        }
                        s_pmi[i].skindisplaynames = null
                        s_pmi[i].nskins = 0
                        i++
                    }
                }
            }
            return Default_MenuKey(s_player_config_menu, key)
        }

        var Menu_PlayerConfig: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_PlayerConfig_f()
            }
        }

        fun Menu_PlayerConfig_f() {
            if (!PlayerConfig_MenuInit()) {
                Menu_SetStatusBar(s_multiplayer_menu, "No valid player models found")
                return
            }
            Menu_SetStatusBar(s_multiplayer_menu, null)
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    PlayerConfig_MenuDraw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return PlayerConfig_MenuKey(key)
                }
            })
        }

        /*
     * =======================================================================
     * 
     * QUIT MENU
     * 
     * =======================================================================
     */

        fun Quit_Key(key: Int): String? {
            when (key) {
                K_ESCAPE, 'n', 'N' -> PopMenu()

                'Y', 'y' -> {
                    cls.key_dest = key_console
                    CL.Quit_f.execute()
                }

                else -> {
                }
            }

            return null

        }

        fun Quit_Draw() {
            val w: Int
            val h: Int
            val d = Dimension()
            re.DrawGetPicSize(d, "quit")
            w = d.width
            h = d.height
            re.DrawPic((viddef.width - w) / 2, (viddef.height - h) / 2, "quit")
        }

        var Menu_Quit: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Menu_Quit_f()
            }
        }

        fun Menu_Quit_f() {
            PushMenu(object : xcommand_t() {
                public fun execute() {
                    Quit_Draw()
                }
            }, object : keyfunc_t() {
                public fun execute(key: Int): String {
                    return Quit_Key(key)
                }
            })
        }

        //	  =============================================================================
        /* Menu Subsystem */

        /**
         * Init
         */
        public fun Init() {
            Cmd.AddCommand("menu_main", Menu_Main)
            Cmd.AddCommand("menu_game", Menu_Game)
            Cmd.AddCommand("menu_loadgame", Menu_LoadGame)
            Cmd.AddCommand("menu_savegame", Menu_SaveGame)
            Cmd.AddCommand("menu_joinserver", Menu_JoinServer)
            Cmd.AddCommand("menu_addressbook", Menu_AddressBook)
            Cmd.AddCommand("menu_startserver", Menu_StartServer)
            Cmd.AddCommand("menu_dmoptions", Menu_DMOptions)
            Cmd.AddCommand("menu_playerconfig", Menu_PlayerConfig)
            Cmd.AddCommand("menu_downloadoptions", Menu_DownloadOptions)
            Cmd.AddCommand("menu_credits", Menu_Credits)
            Cmd.AddCommand("menu_multiplayer", Menu_Multiplayer)
            Cmd.AddCommand("menu_video", Menu_Video)
            Cmd.AddCommand("menu_options", Menu_Options)
            Cmd.AddCommand("menu_keys", Menu_Keys)
            Cmd.AddCommand("menu_quit", Menu_Quit)

            for (i in m_layers.indices) {
                m_layers[i] = menulayer_t()
            }
        }

        /*
     * ================= Draw =================
     */
        fun Draw() {
            if (cls.key_dest != key_menu)
                return

            // repaint everything next frame
            SCR.DirtyScreen()

            // dim everything behind it down
            if (cl.cinematictime > 0)
                re.DrawFill(0, 0, viddef.width, viddef.height, 0)
            else
                re.DrawFadeScreen()

            m_drawfunc!!.execute()

            // delay playing the enter sound until after the
            // menu has been drawn, to avoid delay while
            // caching images
            if (m_entersound) {
                S.StartLocalSound(menu_in_sound)
                m_entersound = false
            }
        }

        /*
     * ================= Keydown =================
     */
        fun Keydown(key: Int) {
            val s: String

            if (m_keyfunc != null)
                if ((s = m_keyfunc!!.execute(key)) != null)
                    S.StartLocalSound(s)
        }

        public fun Action_DoEnter(a: menuaction_s) {
            if (a.callback != null)
                a.callback.execute(a)
        }

        public fun Action_Draw(a: menuaction_s) {
            if ((a.flags and QMF_LEFT_JUSTIFY) != 0) {
                if ((a.flags and QMF_GRAYED) != 0)
                    Menu_DrawStringDark(a.x + a.parent.x + LCOLUMN_OFFSET, a.y + a.parent.y, a.name)
                else
                    Menu_DrawString(a.x + a.parent.x + LCOLUMN_OFFSET, a.y + a.parent.y, a.name)
            } else {
                if ((a.flags and QMF_GRAYED) != 0)
                    Menu_DrawStringR2LDark(a.x + a.parent.x + LCOLUMN_OFFSET, a.y + a.parent.y, a.name)
                else
                    Menu_DrawStringR2L(a.x + a.parent.x + LCOLUMN_OFFSET, a.y + a.parent.y, a.name)
            }
            if (a.ownerdraw != null)
                a.ownerdraw.execute(a)
        }

        public fun Field_DoEnter(f: menufield_s): Boolean {
            if (f.callback != null) {
                f.callback.execute(f)
                return true
            }
            return false
        }

        public fun Field_Draw(f: menufield_s) {
            var i: Int
            val tempbuffer: String
            //[128] = "";

            if (f.name != null)
                Menu_DrawStringR2LDark(f.x + f.parent.x + LCOLUMN_OFFSET, f.y + f.parent.y, f.name)

            //strncpy(tempbuffer, f.buffer + f.visible_offset, f.visible_length);
            val s = f.buffer.toString()
            tempbuffer = s.substring(f.visible_offset, s.length())
            re.DrawChar(f.x + f.parent.x + 16, f.y + f.parent.y - 4, 18)
            re.DrawChar(f.x + f.parent.x + 16, f.y + f.parent.y + 4, 24)

            re.DrawChar(f.x + f.parent.x + 24 + f.visible_length * 8, f.y + f.parent.y - 4, 20)
            re.DrawChar(f.x + f.parent.x + 24 + f.visible_length * 8, f.y + f.parent.y + 4, 26)

            run {
                i = 0
                while (i < f.visible_length) {
                    re.DrawChar(f.x + f.parent.x + 24 + i * 8, f.y + f.parent.y - 4, 19)
                    re.DrawChar(f.x + f.parent.x + 24 + i * 8, f.y + f.parent.y + 4, 25)
                    i++
                }
            }

            Menu_DrawString(f.x + f.parent.x + 24, f.y + f.parent.y, tempbuffer)

            if (Menu_ItemAtCursor(f.parent) == f) {
                val offset: Int

                if (f.visible_offset != 0)
                    offset = f.visible_length
                else
                    offset = f.cursor

                if ((((Timer.Milliseconds() / 250) as Int) and 1) != 0) {
                    re.DrawChar(f.x + f.parent.x + (offset + 2) * 8 + 8, f.y + f.parent.y, 11)
                } else {
                    re.DrawChar(f.x + f.parent.x + (offset + 2) * 8 + 8, f.y + f.parent.y, ' ')
                }
            }
        }

        public fun Field_Key(f: menufield_s, k: Int): Boolean {
            var key = k.toChar()

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

            if (key > 127) {
                when (key) {
                    K_DEL, else -> return false
                }
            }

            /*
         * * support pasting from the clipboard
         */
            if ((Character.toUpperCase(key) == 'V' && keydown[K_CTRL]) || (((key == K_INS) || (key == K_KP_INS)) && keydown[K_SHIFT])) {
                val cbd: String

                if ((cbd = Sys.GetClipboardData()) != null) {
                    //strtok(cbd, "\n\r\b");
                    val lines = cbd.split("\r\n")
                    if (lines.size() > 0 && lines[0].length() != 0) {
                        //strncpy(f.buffer, cbd, f.length - 1);
                        f.buffer = StringBuffer(lines[0])
                        f.cursor = f.buffer.length()

                        f.visible_offset = f.cursor - f.visible_length

                        if (f.visible_offset < 0)
                            f.visible_offset = 0
                    }
                }
                return true
            }

            when (key) {
                K_KP_LEFTARROW, K_LEFTARROW, K_BACKSPACE -> if (f.cursor > 0) {
                    f.buffer.deleteCharAt(f.cursor - 1)
                    //memmove(f.buffer[f.cursor - 1], f.buffer[f.cursor], strlen(&
                    // f.buffer[f.cursor]) + 1);
                    f.cursor--

                    if (f.visible_offset != 0) {
                        f.visible_offset--
                    }
                }

                K_KP_DEL, K_DEL -> //memmove(& f.buffer[f.cursor], & f.buffer[f.cursor + 1], strlen(&
                    // f.buffer[f.cursor + 1]) + 1);
                    f.buffer.deleteCharAt(f.cursor)

                K_KP_ENTER, K_ENTER, K_ESCAPE, K_TAB -> return false

                K_SPACE, else -> {
                if (!Character.isDigit(key) && (f.flags and QMF_NUMBERSONLY) != 0)
                    return false

                if (f.cursor < f.length) {
                    f.buffer.append(key)
                    f.cursor++

                    if (f.cursor > f.visible_length) {
                        f.visible_offset++
                    }
                }
            }
            }

            return true
        }

        public fun Menu_AddItem(menu: menuframework_s, item: menucommon_s) {
            if (menu.nitems == 0)
                menu.nslots = 0

            if (menu.nitems < MAXMENUITEMS) {
                menu.items[menu.nitems] = item
                (menu.items[menu.nitems] as menucommon_s).parent = menu
                menu.nitems++
            }

            menu.nslots = Menu_TallySlots(menu)
        }

        /*
     * * Menu_AdjustCursor * * This function takes the given menu, the
     * direction, and attempts * to adjust the menu's cursor so that it's at the
     * next available * slot.
     */
        public fun Menu_AdjustCursor(m: menuframework_s, dir: Int) {
            val citem: menucommon_s?

            /*
         * * see if it's in a valid spot
         */
            if (m.cursor >= 0 && m.cursor < m.nitems) {
                if ((citem = Menu_ItemAtCursor(m)) != null) {
                    if (citem!!.type != MTYPE_SEPARATOR)
                        return
                }
            }

            /*
         * * it's not in a valid spot, so crawl in the direction indicated until
         * we * find a valid spot
         */
            if (dir == 1) {
                while (true) {
                    citem = Menu_ItemAtCursor(m)
                    if (citem != null)
                        if (citem.type != MTYPE_SEPARATOR)
                            break
                    m.cursor += dir
                    if (m.cursor >= m.nitems)
                        m.cursor = 0
                }
            } else {
                while (true) {
                    citem = Menu_ItemAtCursor(m)
                    if (citem != null)
                        if (citem.type != MTYPE_SEPARATOR)
                            break
                    m.cursor += dir
                    if (m.cursor < 0)
                        m.cursor = m.nitems - 1
                }
            }
        }

        public fun Menu_Center(menu: menuframework_s) {
            var height: Int

            height = (menu.items[menu.nitems - 1] as menucommon_s).y
            height += 10

            menu.y = (viddef.height - height) / 2
        }

        public fun Menu_Draw(menu: menuframework_s) {
            var i: Int
            val item: menucommon_s?

            /*
         * * draw contents
         */
            run {
                i = 0
                while (i < menu.nitems) {
                    when ((menu.items[i] as menucommon_s).type) {
                        MTYPE_FIELD -> Field_Draw(menu.items[i] as menufield_s)
                        MTYPE_SLIDER -> Slider_Draw(menu.items[i] as menuslider_s)
                        MTYPE_LIST -> MenuList_Draw(menu.items[i] as menulist_s)
                        MTYPE_SPINCONTROL -> SpinControl_Draw(menu.items[i] as menulist_s)
                        MTYPE_ACTION -> Action_Draw(menu.items[i] as menuaction_s)
                        MTYPE_SEPARATOR -> Separator_Draw(menu.items[i] as menuseparator_s)
                    }
                    i++
                }
            }

            item = Menu_ItemAtCursor(menu)

            if (item != null && item.cursordraw != null) {
                item.cursordraw!!.execute(item)
            } else if (menu.cursordraw != null) {
                menu.cursordraw!!.execute(menu)
            } else if (item != null && item.type != MTYPE_FIELD) {
                if ((item.flags and QMF_LEFT_JUSTIFY) != 0) {
                    re.DrawChar(menu.x + item.x - 24 + item.cursor_offset, menu.y + item.y, 12 + ((Timer.Milliseconds() / 250) as Int and 1))
                } else {
                    re.DrawChar(menu.x + item.cursor_offset, menu.y + item.y, 12 + ((Timer.Milliseconds() / 250) as Int and 1))
                }
            }

            if (item != null) {
                if (item.statusbarfunc != null)
                    item.statusbarfunc!!.execute(item)
                else if (item.statusbar != null)
                    Menu_DrawStatusBar(item.statusbar)
                else
                    Menu_DrawStatusBar(menu.statusbar)

            } else {
                Menu_DrawStatusBar(menu.statusbar)
            }
        }

        public fun Menu_DrawStatusBar(string: String?) {
            if (string != null) {
                val l = string.length()
                val maxcol = viddef.width / 8
                val col = maxcol / 2 - l / 2

                re.DrawFill(0, viddef.height - 8, viddef.width, 8, 4)
                Menu_DrawString(col * 8, viddef.height - 8, string)
            } else {
                re.DrawFill(0, viddef.height - 8, viddef.width, 8, 0)
            }
        }

        public fun Menu_DrawString(x: Int, y: Int, string: String) {
            var i: Int

            run {
                i = 0
                while (i < string.length()) {
                    re.DrawChar((x + i * 8), y, string.charAt(i))
                    i++
                }
            }
        }

        public fun Menu_DrawStringDark(x: Int, y: Int, string: String) {
            var i: Int

            run {
                i = 0
                while (i < string.length()) {
                    re.DrawChar((x + i * 8), y, string.charAt(i) + 128)
                    i++
                }
            }
        }

        public fun Menu_DrawStringR2L(x: Int, y: Int, string: String) {
            var i: Int

            val l = string.length()
            run {
                i = 0
                while (i < l) {
                    re.DrawChar((x - i * 8), y, string.charAt(l - i - 1))
                    i++
                }
            }
        }

        public fun Menu_DrawStringR2LDark(x: Int, y: Int, string: String) {
            var i: Int

            val l = string.length()
            run {
                i = 0
                while (i < l) {
                    re.DrawChar((x - i * 8), y, string.charAt(l - i - 1) + 128)
                    i++
                }
            }
        }

        public fun Menu_ItemAtCursor(m: menuframework_s): menucommon_s? {
            if (m.cursor < 0 || m.cursor >= m.nitems)
                return null

            return m.items[m.cursor] as menucommon_s
        }

        fun Menu_SelectItem(s: menuframework_s): Boolean {
            val item = Menu_ItemAtCursor(s)

            if (item != null) {
                when (item.type) {
                    MTYPE_FIELD -> return Field_DoEnter(item as menufield_s)
                    MTYPE_ACTION -> {
                        Action_DoEnter(item as menuaction_s)
                        return true
                    }
                    MTYPE_LIST -> //			Menulist_DoEnter( ( menulist_s ) item );
                        return false
                    MTYPE_SPINCONTROL -> //			SpinControl_DoEnter( ( menulist_s ) item );
                        return false
                }
            }
            return false
        }

        public fun Menu_SetStatusBar(m: menuframework_s, string: String?) {
            m.statusbar = string
        }

        public fun Menu_SlideItem(s: menuframework_s, dir: Int) {
            val item = Menu_ItemAtCursor(s) as menucommon_s

            if (item != null) {
                when (item.type) {
                    MTYPE_SLIDER -> Slider_DoSlide(item as menuslider_s, dir)
                    MTYPE_SPINCONTROL -> SpinControl_DoSlide(item as menulist_s, dir)
                }
            }
        }

        public fun Menu_TallySlots(menu: menuframework_s): Int {
            var i: Int
            var total = 0

            run {
                i = 0
                while (i < menu.nitems) {
                    if ((menu.items[i] as menucommon_s).type == MTYPE_LIST) {
                        var nitems = 0
                        val n = (menu.items[i] as menulist_s).itemnames

                        while (n[nitems] != null)
                            nitems++

                        total += nitems
                    } else {
                        total++
                    }
                    i++
                }
            }

            return total
        }

        public fun Menulist_DoEnter(l: menulist_s) {
            val start: Int

            start = l.y / 10 + 1

            l.curvalue = l.parent.cursor - start

            if (l.callback != null)
                l.callback.execute(l)
        }

        public fun MenuList_Draw(l: menulist_s) {
            val n: Array<String>
            var y = 0

            Menu_DrawStringR2LDark(l.x + l.parent.x + LCOLUMN_OFFSET, l.y + l.parent.y, l.name)

            n = l.itemnames

            re.DrawFill(l.x - 112 + l.parent.x, l.parent.y + l.y + l.curvalue * 10 + 10, 128, 10, 16)
            var i = 0

            while (n[i] != null) {
                Menu_DrawStringR2LDark(l.x + l.parent.x + LCOLUMN_OFFSET, l.y + l.parent.y + y + 10, n[i])

                i++
                y += 10
            }
        }

        public fun Separator_Draw(s: menuseparator_s) {
            if (s.name != null)
                Menu_DrawStringR2LDark(s.x + s.parent.x, s.y + s.parent.y, s.name)
        }

        public fun Slider_DoSlide(s: menuslider_s, dir: Int) {
            s.curvalue += dir.toFloat()

            if (s.curvalue > s.maxvalue)
                s.curvalue = s.maxvalue
            else if (s.curvalue < s.minvalue)
                s.curvalue = s.minvalue

            if (s.callback != null)
                s.callback.execute(s)
        }

        public val SLIDER_RANGE: Int = 10

        public fun Slider_Draw(s: menuslider_s) {
            var i: Int

            Menu_DrawStringR2LDark(s.x + s.parent.x + LCOLUMN_OFFSET, s.y + s.parent.y, s.name)

            s.range = (s.curvalue - s.minvalue) / (s.maxvalue - s.minvalue).toFloat()

            if (s.range < 0)
                s.range = 0
            if (s.range > 1)
                s.range = 1
            re.DrawChar(s.x + s.parent.x + RCOLUMN_OFFSET, s.y + s.parent.y, 128)
            run {
                i = 0
                while (i < SLIDER_RANGE) {
                    re.DrawChar(RCOLUMN_OFFSET + s.x + i * 8 + s.parent.x + 8, s.y + s.parent.y, 129)
                    i++
                }
            }
            re.DrawChar(RCOLUMN_OFFSET + s.x + i * 8 + s.parent.x + 8, s.y + s.parent.y, 130)
            re.DrawChar((8 + RCOLUMN_OFFSET + s.parent.x + s.x + (SLIDER_RANGE - 1).toFloat() * 8 * s.range) as Int, s.y + s.parent.y, 131)
        }

        public fun SpinControl_DoEnter(s: menulist_s) {
            s.curvalue++
            if (s.itemnames!![s.curvalue] == null)
                s.curvalue = 0

            if (s.callback != null)
                s.callback.execute(s)
        }

        public fun SpinControl_DoSlide(s: menulist_s, dir: Int) {
            s.curvalue += dir

            if (s.curvalue < 0)
                s.curvalue = 0
            else if (s.curvalue >= s.itemnames!!.size() || s.itemnames!![s.curvalue] == null)
                s.curvalue--

            if (s.callback != null)
                s.callback.execute(s)
        }

        public fun SpinControl_Draw(s: menulist_s) {
            //char buffer[100];

            if (s.name != null) {
                Menu_DrawStringR2LDark(s.x + s.parent.x + LCOLUMN_OFFSET, s.y + s.parent.y, s.name)
            }

            if (s.itemnames!![s.curvalue].indexOf('\n') == -1) {
                Menu_DrawString(RCOLUMN_OFFSET + s.x + s.parent.x, s.y + s.parent.y, s.itemnames!![s.curvalue])
            } else {
                val line1: String
                var line2: String
                line1 = Lib.leftFrom(s.itemnames!![s.curvalue], '\n')
                Menu_DrawString(RCOLUMN_OFFSET + s.x + s.parent.x, s.y + s.parent.y, line1)

                line2 = Lib.rightFrom(s.itemnames!![s.curvalue], '\n')

                val pos = line2.indexOf('\n')
                if (pos != -1)
                    line2 = line2.substring(0, pos)

                Menu_DrawString(RCOLUMN_OFFSET + s.x + s.parent.x, s.y + s.parent.y + 10, line2)
            }
        }
    }
}