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
import lwjake2.client.Key
import lwjake2.qcommon.Cbuf

import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

/**
 * @author dsanders
 */
public class LWJGLKBD : KBD() {

    private var lwjglKeycodeMap: CharArray? = null
    private var pressed: IntArray? = null

    public fun Init() {
        try {
            if (!Keyboard.isCreated()) Keyboard.create()
            if (!Mouse.isCreated()) Mouse.create()

            // Old code from old LWJGL, not sure if it's needed - flibit

            // if (!Keyboard.isBuffered()) Keyboard.enableBuffer();
            // if (!Keyboard.isTranslationEnabled()) Keyboard.enableTranslation();
            // if (!Mouse.isBuffered()) Mouse.enableBuffer();

            if (lwjglKeycodeMap == null) lwjglKeycodeMap = CharArray(256)
            if (pressed == null) pressed = IntArray(256)

            lastRepeat = Timer.Milliseconds()
        } catch (e: Exception) {
        }

    }

    public fun Update() {
        // get events
        HandleEvents()
    }

    public fun Close() {
        Keyboard.destroy()
        Mouse.destroy()
        // free the memory for GC
        lwjglKeycodeMap = null
        pressed = null
    }

    private fun HandleEvents() {
        Keyboard.poll()

        if (Display.isCloseRequested()) {
            Cbuf.ExecuteText(Defines.EXEC_APPEND, "quit")
        }

        while (Keyboard.next()) {
            val key = Keyboard.getEventKey()
            val ch = Keyboard.getEventCharacter()
            val down = Keyboard.getEventKeyState()

            // fill the character translation table
            // this is needed because the getEventCharacter() returns \0 if a key is released
            // keycode is correct but the charachter value is not
            if (down) {
                lwjglKeycodeMap[key] = ch
                pressed[key] = Globals.sys_frame_time
            } else {
                pressed[key] = 0
            }

            Do_Key_Event(XLateKey(key, ch.toInt()), down)
        }

        generateRepeats()

        if (IN.mouse_active) {
            mx = Mouse.getDX() shl 1
            my = -Mouse.getDY() shl 1
        } else {
            mx = 0
            my = 0
        }

        while (Mouse.next()) {
            var button = Mouse.getEventButton()
            if (button >= 0) {
                Do_Key_Event(Key.K_MOUSE1 + button, Mouse.getEventButtonState())
            } else {
                button = Mouse.getEventDWheel()
                if (button > 0) {
                    Do_Key_Event(Key.K_MWHEELUP, true)
                    Do_Key_Event(Key.K_MWHEELUP, false)
                } else if (button < 0) {
                    Do_Key_Event(Key.K_MWHEELDOWN, true)
                    Do_Key_Event(Key.K_MWHEELDOWN, false)
                }
            }
        }
    }

    private fun generateRepeats() {
        val time = Globals.sys_frame_time
        if (time - lastRepeat > 50) {
            for (i in pressed!!.indices) {
                if (pressed!![i] > 0 && time - pressed!![i] > 500)
                    Do_Key_Event(XLateKey(i, lwjglKeycodeMap!![i].toInt()), true)
            }
            lastRepeat = time
        }
    }

    private fun XLateKey(code: Int, ch: Int): Int {
        var key = 0

        when (code) {
        //	00626                 case XK_KP_Page_Up:      key = K_KP_PGUP; break;
            Keyboard.KEY_PRIOR -> key = Key.K_PGUP

        //	00629                 case XK_KP_Page_Down: key = K_KP_PGDN; break;
            Keyboard.KEY_NEXT -> key = Key.K_PGDN

        //	00632                 case XK_KP_Home: key = K_KP_HOME; break;
            Keyboard.KEY_HOME -> key = Key.K_HOME

        //	00635                 case XK_KP_End:  key = K_KP_END; break;
            Keyboard.KEY_END -> key = Key.K_END

        // case Keyboard.KEY_LEFT: key = Key.K_KP_LEFTARROW; break;
            Keyboard.KEY_LEFT -> key = Key.K_LEFTARROW

        // case Keyboard.KEY_RIGHT: key = Key.K_KP_RIGHTARROW; break;
            Keyboard.KEY_RIGHT -> key = Key.K_RIGHTARROW

        // case Keyboard.KEY_DOWN: key = Key.K_KP_DOWNARROW; break;
            Keyboard.KEY_DOWN -> key = Key.K_DOWNARROW

        // case Keyboard.KEY_UP: key = Key.K_KP_UPARROW; break;
            Keyboard.KEY_UP -> key = Key.K_UPARROW

            Keyboard.KEY_ESCAPE -> key = Key.K_ESCAPE


            Keyboard.KEY_RETURN -> key = Key.K_ENTER
        //	00652                 case XK_KP_Enter: key = K_KP_ENTER;     break;

            Keyboard.KEY_TAB -> key = Key.K_TAB

            Keyboard.KEY_F1 -> key = Key.K_F1
            Keyboard.KEY_F2 -> key = Key.K_F2
            Keyboard.KEY_F3 -> key = Key.K_F3
            Keyboard.KEY_F4 -> key = Key.K_F4
            Keyboard.KEY_F5 -> key = Key.K_F5
            Keyboard.KEY_F6 -> key = Key.K_F6
            Keyboard.KEY_F7 -> key = Key.K_F7
            Keyboard.KEY_F8 -> key = Key.K_F8
            Keyboard.KEY_F9 -> key = Key.K_F9
            Keyboard.KEY_F10 -> key = Key.K_F10
            Keyboard.KEY_F11 -> key = Key.K_F11
            Keyboard.KEY_F12 -> key = Key.K_F12

            Keyboard.KEY_BACK -> key = Key.K_BACKSPACE

            Keyboard.KEY_DELETE -> key = Key.K_DEL
        //	00683                 case XK_KP_Delete: key = K_KP_DEL; break;

            Keyboard.KEY_PAUSE -> key = Key.K_PAUSE

            Keyboard.KEY_RSHIFT, Keyboard.KEY_LSHIFT -> key = Key.K_SHIFT

            Keyboard.KEY_RCONTROL, Keyboard.KEY_LCONTROL -> key = Key.K_CTRL

            Keyboard.KEY_LMENU, Keyboard.KEY_RMENU -> key = Key.K_ALT

        //	00700                 case XK_KP_Begin: key = K_KP_5; break;
        //	00701
            Keyboard.KEY_INSERT -> key = Key.K_INS
        // toggle console for DE and US keyboards
            Keyboard.KEY_GRAVE, Keyboard.KEY_CIRCUMFLEX -> key = '`'

            else -> {
                key = lwjglKeycodeMap!![code].toInt()
                if (key >= 'A' && key <= 'Z')
                    key = key - 'A' + 'a'
            }
        }
        if (key > 255) key = 0
        return key
    }

    public fun Do_Key_Event(key: Int, down: Boolean) {
        Key.Event(key, down, Timer.Milliseconds())
    }

    public fun installGrabs() {
        Mouse.setGrabbed(true)
    }

    public fun uninstallGrabs() {
        Mouse.setGrabbed(false)
    }

    companion object {

        private var lastRepeat: Int = 0
    }
}
