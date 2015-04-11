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

import lwjake2.Globals
import lwjake2.client.CL_input
import lwjake2.client.Key
import lwjake2.game.Cmd
import lwjake2.game.usercmd_t
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.xcommand_t
import lwjake2.util.Math3D

/**
 * IN
 */
public class IN : Globals() {
    companion object {

        var mouse_avail = true

        var mouse_active = false

        var ignorefirst = false

        var mouse_buttonstate: Int = 0

        var mouse_oldbuttonstate: Int = 0

        var old_mouse_x: Int = 0

        var old_mouse_y: Int = 0

        var mlooking: Boolean = false

        public fun ActivateMouse() {
            if (!mouse_avail)
                return
            if (!mouse_active) {
                KBD.mx = KBD.my = 0 // don't spazz
                install_grabs()
                mouse_active = true
            }
        }

        public fun DeactivateMouse() {
            // if (!mouse_avail || c == null) return;
            if (mouse_active) {
                uninstall_grabs()
                mouse_active = false
            }
        }

        private fun install_grabs() {
            Globals.re.getKeyboardHandler().installGrabs()
            ignorefirst = true
        }

        private fun uninstall_grabs() {
            Globals.re.getKeyboardHandler().uninstallGrabs()
        }

        public fun toggleMouse() {
            if (mouse_avail) {
                mouse_avail = false
                DeactivateMouse()
            } else {
                mouse_avail = true
                ActivateMouse()
            }
        }

        public fun Init() {
            in_mouse = Cvar.Get("in_mouse", "1", CVAR_ARCHIVE)
            in_joystick = Cvar.Get("in_joystick", "0", CVAR_ARCHIVE)
        }

        public fun Shutdown() {
            mouse_avail = false
        }

        public fun Real_IN_Init() {
            // mouse variables
            Globals.m_filter = Cvar.Get("m_filter", "0", 0)
            Globals.in_mouse = Cvar.Get("in_mouse", "1", CVAR_ARCHIVE)
            Globals.freelook = Cvar.Get("freelook", "1", 0)
            Globals.lookstrafe = Cvar.Get("lookstrafe", "0", 0)
            Globals.sensitivity = Cvar.Get("sensitivity", "3", 0)
            Globals.m_pitch = Cvar.Get("m_pitch", "0.022", 0)
            Globals.m_yaw = Cvar.Get("m_yaw", "0.022", 0)
            Globals.m_forward = Cvar.Get("m_forward", "1", 0)
            Globals.m_side = Cvar.Get("m_side", "0.8", 0)

            Cmd.AddCommand("+mlook", object : xcommand_t() {
                public fun execute() {
                    MLookDown()
                }
            })
            Cmd.AddCommand("-mlook", object : xcommand_t() {
                public fun execute() {
                    MLookUp()
                }
            })

            Cmd.AddCommand("force_centerview", object : xcommand_t() {
                public fun execute() {
                    Force_CenterView_f()
                }
            })

            Cmd.AddCommand("togglemouse", object : xcommand_t() {
                public fun execute() {
                    toggleMouse()
                }
            })

            IN.mouse_avail = true
        }

        public fun Commands() {
            var i: Int

            if (!IN.mouse_avail)
                return

            val kbd = Globals.re.getKeyboardHandler()
            run {
                i = 0
                while (i < 3) {
                    if ((IN.mouse_buttonstate and (1 shl i)) != 0 && (IN.mouse_oldbuttonstate and (1 shl i)) == 0)
                        kbd.Do_Key_Event(Key.K_MOUSE1 + i, true)

                    if ((IN.mouse_buttonstate and (1 shl i)) == 0 && (IN.mouse_oldbuttonstate and (1 shl i)) != 0)
                        kbd.Do_Key_Event(Key.K_MOUSE1 + i, false)
                    i++
                }
            }
            IN.mouse_oldbuttonstate = IN.mouse_buttonstate
        }

        public fun Frame() {

            if (!cl.refresh_prepped || cls.key_dest == key_console || cls.key_dest == key_menu)
                DeactivateMouse()
            else
                ActivateMouse()
        }

        public fun CenterView() {
            cl.viewangles[PITCH] = -Math3D.SHORT2ANGLE(cl.frame.playerstate.pmove.delta_angles[PITCH])
        }

        public fun Move(cmd: usercmd_t) {
            if (!IN.mouse_avail)
                return

            if (Globals.m_filter.value != 0.0.toFloat()) {
                KBD.mx = (KBD.mx + IN.old_mouse_x) / 2
                KBD.my = (KBD.my + IN.old_mouse_y) / 2
            }

            IN.old_mouse_x = KBD.mx
            IN.old_mouse_y = KBD.my

            KBD.mx = (KBD.mx * Globals.sensitivity.value) as Int
            KBD.my = (KBD.my * Globals.sensitivity.value) as Int

            // add mouse X/Y movement to cmd
            if ((CL_input.in_strafe.state and 1) != 0 || ((Globals.lookstrafe.value != 0) && IN.mlooking)) {
                cmd.sidemove += Globals.m_side.value * KBD.mx
            } else {
                Globals.cl.viewangles[YAW] -= Globals.m_yaw.value * KBD.mx
            }

            if ((IN.mlooking || Globals.freelook.value != 0.0.toFloat()) && (CL_input.in_strafe.state and 1) == 0) {
                Globals.cl.viewangles[PITCH] += Globals.m_pitch.value * KBD.my
            } else {
                cmd.forwardmove -= Globals.m_forward.value * KBD.my
            }
            KBD.mx = KBD.my = 0
        }

        fun MLookDown() {
            mlooking = true
        }

        fun MLookUp() {
            mlooking = false
            CenterView()
        }

        fun Force_CenterView_f() {
            Globals.cl.viewangles[PITCH] = 0
        }
    }

}