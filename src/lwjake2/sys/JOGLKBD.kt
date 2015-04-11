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

import lwjake2.client.Key

import java.awt.AWTException
import java.awt.Component
import java.awt.Container
import java.awt.Cursor
import java.awt.Insets
import java.awt.Point
import java.awt.Robot
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

import javax.swing.ImageIcon

public class JOGLKBD : KBD() {

    public fun Init() {
    }

    public fun Update() {
        // get events
        HandleEvents()
    }

    public fun Close() {
    }

    private fun HandleEvents() {
        val key: Int

        val event: LWJake2InputEvent
        while ((event = InputListener.nextEvent()) != null) {
            when (event.type) {
                LWJake2InputEvent.KeyPress, LWJake2InputEvent.KeyRelease -> Do_Key_Event(XLateKey(event.ev as KeyEvent), event.type == LWJake2InputEvent.KeyPress)

                LWJake2InputEvent.MotionNotify -> //					if (IN.ignorefirst) {
                    //						IN.ignorefirst = false;
                    //						break;
                    //					}
                    if (IN.mouse_active) {
                        mx = ((event.ev as MouseEvent).getX() - win_w2) * 2
                        my = ((event.ev as MouseEvent).getY() - win_h2) * 2
                    } else {
                        mx = 0
                        my = 0
                    }
            // see java.awt.MouseEvent
                LWJake2InputEvent.ButtonPress -> {
                    key = mouseEventToKey(event.ev as MouseEvent)
                    Do_Key_Event(key, true)
                }

                LWJake2InputEvent.ButtonRelease -> {
                    key = mouseEventToKey(event.ev as MouseEvent)
                    Do_Key_Event(key, false)
                }

                LWJake2InputEvent.WheelMoved -> {
                    val dir = (event.ev as MouseWheelEvent).getWheelRotation()
                    if (dir > 0) {
                        Do_Key_Event(Key.K_MWHEELDOWN, true)
                        Do_Key_Event(Key.K_MWHEELDOWN, false)
                    } else {
                        Do_Key_Event(Key.K_MWHEELUP, true)
                        Do_Key_Event(Key.K_MWHEELUP, false)
                    }
                }

                LWJake2InputEvent.CreateNotify, LWJake2InputEvent.ConfigureNotify -> {
                    var c: Component? = (event.ev as ComponentEvent).getComponent()
                    win_x = 0
                    win_y = 0
                    win_w2 = c!!.getWidth() / 2
                    win_h2 = c!!.getHeight() / 2
                    while (c != null) {
                        if (c is Container) {
                            val insets = (c as Container).getInsets()
                            win_x += insets.left
                            win_y += insets.top
                        }
                        win_x += c!!.getX()
                        win_y += c!!.getY()
                        c = c!!.getParent()
                    }
                }
            }
        }

        if (mx != 0 || my != 0) {
            // move the mouse to the window center again
            robot.mouseMove(win_x + win_w2, win_y + win_h2)
        }
    }

    // strange button numbering in java.awt.MouseEvent
    // BUTTON1(left) BUTTON2(center) BUTTON3(right)
    // K_MOUSE1      K_MOUSE3        K_MOUSE2
    private fun mouseEventToKey(ev: MouseEvent): Int {
        when (ev.getButton()) {
            MouseEvent.BUTTON3 -> return Key.K_MOUSE2
            MouseEvent.BUTTON2 -> return Key.K_MOUSE3
            else -> return Key.K_MOUSE1
        }
    }

    public fun Do_Key_Event(key: Int, down: Boolean) {
        Key.Event(key, down, Timer.Milliseconds())
    }

    public fun centerMouse() {
        robot.mouseMove(win_x + win_w2, win_y + win_h2)
    }

    public fun installGrabs() {
        if (emptyCursor == null) {
            val emptyIcon = ImageIcon(ByteArray(0))
            emptyCursor = c!!.getToolkit().createCustomCursor(emptyIcon.getImage(), Point(0, 0), "emptyCursor")
        }
        c!!.setCursor(emptyCursor)
        centerMouse()
    }

    public fun uninstallGrabs() {
        c!!.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
    }

    companion object {
        var robot: Robot
        public var listener: InputListener = InputListener()
        var emptyCursor: Cursor? = null
        var c: Component? = null

        var win_w2 = 0
        var win_h2 = 0

        {
            try {
                robot = Robot()
            } catch (e: AWTException) {
                System.exit(1)
            }

        }

        private fun XLateKey(ev: KeyEvent): Int {

            var key = 0
            val code = ev.getKeyCode()

            when (code) {
            //	00626                 case XK_KP_Page_Up:      key = K_KP_PGUP; break;
                KeyEvent.VK_PAGE_UP -> key = Key.K_PGUP

            //	00629                 case XK_KP_Page_Down: key = K_KP_PGDN; break;
                KeyEvent.VK_PAGE_DOWN -> key = Key.K_PGDN

            //	00632                 case XK_KP_Home: key = K_KP_HOME; break;
                KeyEvent.VK_HOME -> key = Key.K_HOME

            //	00635                 case XK_KP_End:  key = K_KP_END; break;
                KeyEvent.VK_END -> key = Key.K_END

                KeyEvent.VK_KP_LEFT -> key = Key.K_KP_LEFTARROW
                KeyEvent.VK_LEFT -> key = Key.K_LEFTARROW

                KeyEvent.VK_KP_RIGHT -> key = Key.K_KP_RIGHTARROW
                KeyEvent.VK_RIGHT -> key = Key.K_RIGHTARROW

                KeyEvent.VK_KP_DOWN -> key = Key.K_KP_DOWNARROW
                KeyEvent.VK_DOWN -> key = Key.K_DOWNARROW

                KeyEvent.VK_KP_UP -> key = Key.K_KP_UPARROW
                KeyEvent.VK_UP -> key = Key.K_UPARROW

                KeyEvent.VK_ESCAPE -> key = Key.K_ESCAPE


                KeyEvent.VK_ENTER -> key = Key.K_ENTER
            //	00652                 case XK_KP_Enter: key = K_KP_ENTER;     break;

                KeyEvent.VK_TAB -> key = Key.K_TAB

                KeyEvent.VK_F1 -> key = Key.K_F1
                KeyEvent.VK_F2 -> key = Key.K_F2
                KeyEvent.VK_F3 -> key = Key.K_F3
                KeyEvent.VK_F4 -> key = Key.K_F4
                KeyEvent.VK_F5 -> key = Key.K_F5
                KeyEvent.VK_F6 -> key = Key.K_F6
                KeyEvent.VK_F7 -> key = Key.K_F7
                KeyEvent.VK_F8 -> key = Key.K_F8
                KeyEvent.VK_F9 -> key = Key.K_F9
                KeyEvent.VK_F10 -> key = Key.K_F10
                KeyEvent.VK_F11 -> key = Key.K_F11
                KeyEvent.VK_F12 -> key = Key.K_F12

                KeyEvent.VK_BACK_SPACE -> key = Key.K_BACKSPACE

                KeyEvent.VK_DELETE -> key = Key.K_DEL
            //	00683                 case XK_KP_Delete: key = K_KP_DEL; break;

                KeyEvent.VK_PAUSE -> key = Key.K_PAUSE

                KeyEvent.VK_SHIFT -> key = Key.K_SHIFT
                KeyEvent.VK_CONTROL -> key = Key.K_CTRL

                KeyEvent.VK_ALT, KeyEvent.VK_ALT_GRAPH -> key = Key.K_ALT

            //	00700                 case XK_KP_Begin: key = K_KP_5; break;
            //	00701
                KeyEvent.VK_INSERT -> key = Key.K_INS
            // toggle console for DE and US keyboards
                KeyEvent.VK_DEAD_ACUTE, KeyEvent.VK_CIRCUMFLEX, KeyEvent.VK_DEAD_CIRCUMFLEX -> key = '`'

                else -> {
                    key = ev.getKeyChar().toInt()

                    if (key >= 'A' && key <= 'Z')
                        key = key - 'A' + 'a'
                }
            }
            if (key > 255) key = 0

            return key
        }
    }
}
