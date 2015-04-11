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

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.cvar_t
import lwjake2.game.usercmd_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.MSG
import lwjake2.qcommon.Netchan
import lwjake2.qcommon.SZ
import lwjake2.qcommon.sizebuf_t
import lwjake2.qcommon.xcommand_t
import lwjake2.sys.IN
import lwjake2.util.Lib
import lwjake2.util.Math3D

/**
 * CL_input
 */
public class CL_input {
    companion object {

        var frame_msec: Long = 0

        var old_sys_frame_time: Long = 0

        var cl_nodelta: cvar_t

        /*
	 * ===============================================================================
	 * 
	 * KEY BUTTONS
	 * 
	 * Continuous button event tracking is complicated by the fact that two
	 * different input sources (say, mouse button 1 and the control key) can
	 * both press the same button, but the button should only be released when
	 * both of the pressing key have been released.
	 * 
	 * When a key event issues a button command (+forward, +attack, etc), it
	 * appends its key number as a parameter to the command so it can be matched
	 * up with the release.
	 * 
	 * state bit 0 is the current state of the key state bit 1 is edge triggered
	 * on the up to down transition state bit 2 is edge triggered on the down to
	 * up transition
	 * 
	 * 
	 * Key_Event (int key, qboolean down, unsigned time);
	 * 
	 * +mlook src time
	 * 
	 * ===============================================================================
	 */

        var in_klook = kbutton_t()

        var in_left = kbutton_t()

        var in_right = kbutton_t()

        var in_forward = kbutton_t()

        var in_back = kbutton_t()

        var in_lookup = kbutton_t()

        var in_lookdown = kbutton_t()

        var in_moveleft = kbutton_t()

        var in_moveright = kbutton_t()

        public var in_strafe: kbutton_t = kbutton_t()

        var in_speed = kbutton_t()

        var in_use = kbutton_t()

        var in_attack = kbutton_t()

        var in_up = kbutton_t()

        var in_down = kbutton_t()

        var in_impulse: Int = 0

        fun KeyDown(b: kbutton_t) {
            val k: Int
            var c: String

            c = Cmd.Argv(1)
            if (c.length() > 0)
                k = Lib.atoi(c)
            else
                k = -1 // typed manually at the console for continuous down

            if (k == b.down[0] || k == b.down[1])
                return  // repeating key

            if (b.down[0] == 0)
                b.down[0] = k
            else if (b.down[1] == 0)
                b.down[1] = k
            else {
                Com.Printf("Three keys down for a button!\n")
                return
            }

            if ((b.state and 1) != 0)
                return  // still down

            // save timestamp
            c = Cmd.Argv(2)
            b.downtime = Lib.atoi(c)
            if (b.downtime == 0)
                b.downtime = Globals.sys_frame_time - 100

            b.state = b.state or 3 // down + impulse down
        }

        fun KeyUp(b: kbutton_t) {
            val k: Int
            var c: String
            val uptime: Int

            c = Cmd.Argv(1)
            if (c.length() > 0)
                k = Lib.atoi(c)
            else {
                // typed manually at the console, assume for unsticking, so clear
                // all
                b.down[0] = b.down[1] = 0
                b.state = 4 // impulse up
                return
            }

            if (b.down[0] == k)
                b.down[0] = 0
            else if (b.down[1] == k)
                b.down[1] = 0
            else
                return  // key up without coresponding down (menu pass through)
            if (b.down[0] != 0 || b.down[1] != 0)
                return  // some other key is still holding it down

            if ((b.state and 1) == 0)
                return  // still up (this should not happen)

            // save timestamp
            c = Cmd.Argv(2)
            uptime = Lib.atoi(c)
            if (uptime != 0)
                b.msec += uptime - b.downtime
            else
                b.msec += 10

            b.state = b.state and 1.inv() // now up
            b.state = b.state or 4 // impulse up
        }

        fun IN_KLookDown() {
            KeyDown(in_klook)
        }

        fun IN_KLookUp() {
            KeyUp(in_klook)
        }

        fun IN_UpDown() {
            KeyDown(in_up)
        }

        fun IN_UpUp() {
            KeyUp(in_up)
        }

        fun IN_DownDown() {
            KeyDown(in_down)
        }

        fun IN_DownUp() {
            KeyUp(in_down)
        }

        fun IN_LeftDown() {
            KeyDown(in_left)
        }

        fun IN_LeftUp() {
            KeyUp(in_left)
        }

        fun IN_RightDown() {
            KeyDown(in_right)
        }

        fun IN_RightUp() {
            KeyUp(in_right)
        }

        fun IN_ForwardDown() {
            KeyDown(in_forward)
        }

        fun IN_ForwardUp() {
            KeyUp(in_forward)
        }

        fun IN_BackDown() {
            KeyDown(in_back)
        }

        fun IN_BackUp() {
            KeyUp(in_back)
        }

        fun IN_LookupDown() {
            KeyDown(in_lookup)
        }

        fun IN_LookupUp() {
            KeyUp(in_lookup)
        }

        fun IN_LookdownDown() {
            KeyDown(in_lookdown)
        }

        fun IN_LookdownUp() {
            KeyUp(in_lookdown)
        }

        fun IN_MoveleftDown() {
            KeyDown(in_moveleft)
        }

        fun IN_MoveleftUp() {
            KeyUp(in_moveleft)
        }

        fun IN_MoverightDown() {
            KeyDown(in_moveright)
        }

        fun IN_MoverightUp() {
            KeyUp(in_moveright)
        }

        fun IN_SpeedDown() {
            KeyDown(in_speed)
        }

        fun IN_SpeedUp() {
            KeyUp(in_speed)
        }

        fun IN_StrafeDown() {
            KeyDown(in_strafe)
        }

        fun IN_StrafeUp() {
            KeyUp(in_strafe)
        }

        fun IN_AttackDown() {
            KeyDown(in_attack)
        }

        fun IN_AttackUp() {
            KeyUp(in_attack)
        }

        fun IN_UseDown() {
            KeyDown(in_use)
        }

        fun IN_UseUp() {
            KeyUp(in_use)
        }

        fun IN_Impulse() {
            in_impulse = Lib.atoi(Cmd.Argv(1))
        }

        /*
	 * =============== CL_KeyState
	 * 
	 * Returns the fraction of the frame that the key was down ===============
	 */
        fun KeyState(key: kbutton_t): Float {
            var `val`: Float
            val msec: Long

            key.state = key.state and 1 // clear impulses

            msec = key.msec
            key.msec = 0

            if (key.state != 0) {
                // still down
                msec += Globals.sys_frame_time - key.downtime
                key.downtime = Globals.sys_frame_time
            }

            `val` = msec.toFloat() / frame_msec.toFloat()
            if (`val` < 0)
                `val` = 0
            if (`val` > 1)
                `val` = 1

            return `val`
        }

        //	  ==========================================================================

        /*
	 * ================ CL_AdjustAngles
	 * 
	 * Moves the local angle positions ================
	 */
        fun AdjustAngles() {
            val speed: Float
            val up: Float
            val down: Float

            if ((in_speed.state and 1) != 0)
                speed = Globals.cls.frametime * Globals.cl_anglespeedkey.value
            else
                speed = Globals.cls.frametime

            if ((in_strafe.state and 1) == 0) {
                Globals.cl.viewangles[Defines.YAW] -= speed * Globals.cl_yawspeed.value * KeyState(in_right)
                Globals.cl.viewangles[Defines.YAW] += speed * Globals.cl_yawspeed.value * KeyState(in_left)
            }
            if ((in_klook.state and 1) != 0) {
                Globals.cl.viewangles[Defines.PITCH] -= speed * Globals.cl_pitchspeed.value * KeyState(in_forward)
                Globals.cl.viewangles[Defines.PITCH] += speed * Globals.cl_pitchspeed.value * KeyState(in_back)
            }

            up = KeyState(in_lookup)
            down = KeyState(in_lookdown)

            Globals.cl.viewangles[Defines.PITCH] -= speed * Globals.cl_pitchspeed.value * up
            Globals.cl.viewangles[Defines.PITCH] += speed * Globals.cl_pitchspeed.value * down
        }

        /*
	 * ================ CL_BaseMove
	 * 
	 * Send the intended movement message to the server ================
	 */
        fun BaseMove(cmd: usercmd_t) {
            AdjustAngles()

            //memset (cmd, 0, sizeof(*cmd));
            cmd.clear()

            Math3D.VectorCopy(Globals.cl.viewangles, cmd.angles)
            if ((in_strafe.state and 1) != 0) {
                cmd.sidemove += Globals.cl_sidespeed.value * KeyState(in_right)
                cmd.sidemove -= Globals.cl_sidespeed.value * KeyState(in_left)
            }

            cmd.sidemove += Globals.cl_sidespeed.value * KeyState(in_moveright)
            cmd.sidemove -= Globals.cl_sidespeed.value * KeyState(in_moveleft)

            cmd.upmove += Globals.cl_upspeed.value * KeyState(in_up)
            cmd.upmove -= Globals.cl_upspeed.value * KeyState(in_down)

            if ((in_klook.state and 1) == 0) {
                cmd.forwardmove += Globals.cl_forwardspeed.value * KeyState(in_forward)
                cmd.forwardmove -= Globals.cl_forwardspeed.value * KeyState(in_back)
            }

            //
            //	   adjust for speed key / running
            //
            if (((in_speed.state and 1) xor (Globals.cl_run.value) as Int) != 0) {
                cmd.forwardmove *= 2
                cmd.sidemove *= 2
                cmd.upmove *= 2
            }

        }

        fun ClampPitch() {

            var pitch: Float

            pitch = Math3D.SHORT2ANGLE(Globals.cl.frame.playerstate.pmove.delta_angles[Defines.PITCH])
            if (pitch > 180)
                pitch -= 360

            if (Globals.cl.viewangles[Defines.PITCH] + pitch < -360)
                Globals.cl.viewangles[Defines.PITCH] += 360 // wrapped
            if (Globals.cl.viewangles[Defines.PITCH] + pitch > 360)
                Globals.cl.viewangles[Defines.PITCH] -= 360 // wrapped

            if (Globals.cl.viewangles[Defines.PITCH] + pitch > 89)
                Globals.cl.viewangles[Defines.PITCH] = 89 - pitch
            if (Globals.cl.viewangles[Defines.PITCH] + pitch < -89)
                Globals.cl.viewangles[Defines.PITCH] = (-89).toFloat() - pitch
        }

        /*
	 * ============== CL_FinishMove ==============
	 */
        fun FinishMove(cmd: usercmd_t) {
            var ms: Int
            var i: Int

            //
            //	   figure button bits
            //
            if ((in_attack.state and 3) != 0)
                cmd.buttons = cmd.buttons or Defines.BUTTON_ATTACK
            in_attack.state = in_attack.state and 2.inv()

            if ((in_use.state and 3) != 0)
                cmd.buttons = cmd.buttons or Defines.BUTTON_USE
            in_use.state = in_use.state and 2.inv()

            if (Key.anykeydown != 0 && Globals.cls.key_dest == Defines.key_game)
                cmd.buttons = cmd.buttons or Defines.BUTTON_ANY

            // send milliseconds of time to apply the move
            ms = (Globals.cls.frametime * 1000) as Int
            if (ms > 250)
                ms = 100 // time was unreasonable
            cmd.msec = ms.toByte()

            ClampPitch()
            run {
                i = 0
                while (i < 3) {
                    cmd.angles[i] = Math3D.ANGLE2SHORT(Globals.cl.viewangles[i]) as Short
                    i++
                }
            }

            cmd.impulse = in_impulse.toByte()
            in_impulse = 0

            // send the ambient light level at the player's current position
            cmd.lightlevel = Globals.cl_lightlevel.value as Byte
        }

        /*
	 * ================= CL_CreateCmd =================
	 */
        fun CreateCmd(cmd: usercmd_t) {
            //usercmd_t cmd = new usercmd_t();

            frame_msec = Globals.sys_frame_time - old_sys_frame_time
            if (frame_msec < 1)
                frame_msec = 1
            if (frame_msec > 200)
                frame_msec = 200

            // get basic movement from keyboard
            BaseMove(cmd)

            // allow mice or other external controllers to add to the move
            IN.Move(cmd)

            FinishMove(cmd)

            old_sys_frame_time = Globals.sys_frame_time

            //return cmd;
        }

        /*
	 * ============ CL_InitInput ============
	 */
        fun InitInput() {
            Cmd.AddCommand("centerview", object : xcommand_t() {
                public fun execute() {
                    IN.CenterView()
                }
            })

            Cmd.AddCommand("+moveup", object : xcommand_t() {
                public fun execute() {
                    IN_UpDown()
                }
            })
            Cmd.AddCommand("-moveup", object : xcommand_t() {
                public fun execute() {
                    IN_UpUp()
                }
            })
            Cmd.AddCommand("+movedown", object : xcommand_t() {
                public fun execute() {
                    IN_DownDown()
                }
            })
            Cmd.AddCommand("-movedown", object : xcommand_t() {
                public fun execute() {
                    IN_DownUp()
                }
            })
            Cmd.AddCommand("+left", object : xcommand_t() {
                public fun execute() {
                    IN_LeftDown()
                }
            })
            Cmd.AddCommand("-left", object : xcommand_t() {
                public fun execute() {
                    IN_LeftUp()
                }
            })
            Cmd.AddCommand("+right", object : xcommand_t() {
                public fun execute() {
                    IN_RightDown()
                }
            })
            Cmd.AddCommand("-right", object : xcommand_t() {
                public fun execute() {
                    IN_RightUp()
                }
            })
            Cmd.AddCommand("+forward", object : xcommand_t() {
                public fun execute() {
                    IN_ForwardDown()
                }
            })
            Cmd.AddCommand("-forward", object : xcommand_t() {
                public fun execute() {
                    IN_ForwardUp()
                }
            })
            Cmd.AddCommand("+back", object : xcommand_t() {
                public fun execute() {
                    IN_BackDown()
                }
            })
            Cmd.AddCommand("-back", object : xcommand_t() {
                public fun execute() {
                    IN_BackUp()
                }
            })
            Cmd.AddCommand("+lookup", object : xcommand_t() {
                public fun execute() {
                    IN_LookupDown()
                }
            })
            Cmd.AddCommand("-lookup", object : xcommand_t() {
                public fun execute() {
                    IN_LookupUp()
                }
            })
            Cmd.AddCommand("+lookdown", object : xcommand_t() {
                public fun execute() {
                    IN_LookdownDown()
                }
            })
            Cmd.AddCommand("-lookdown", object : xcommand_t() {
                public fun execute() {
                    IN_LookdownUp()
                }
            })
            Cmd.AddCommand("+strafe", object : xcommand_t() {
                public fun execute() {
                    IN_StrafeDown()
                }
            })
            Cmd.AddCommand("-strafe", object : xcommand_t() {
                public fun execute() {
                    IN_StrafeUp()
                }
            })
            Cmd.AddCommand("+moveleft", object : xcommand_t() {
                public fun execute() {
                    IN_MoveleftDown()
                }
            })
            Cmd.AddCommand("-moveleft", object : xcommand_t() {
                public fun execute() {
                    IN_MoveleftUp()
                }
            })
            Cmd.AddCommand("+moveright", object : xcommand_t() {
                public fun execute() {
                    IN_MoverightDown()
                }
            })
            Cmd.AddCommand("-moveright", object : xcommand_t() {
                public fun execute() {
                    IN_MoverightUp()
                }
            })
            Cmd.AddCommand("+speed", object : xcommand_t() {
                public fun execute() {
                    IN_SpeedDown()
                }
            })
            Cmd.AddCommand("-speed", object : xcommand_t() {
                public fun execute() {
                    IN_SpeedUp()
                }
            })
            Cmd.AddCommand("+attack", object : xcommand_t() {
                public fun execute() {
                    IN_AttackDown()
                }
            })
            Cmd.AddCommand("-attack", object : xcommand_t() {
                public fun execute() {
                    IN_AttackUp()
                }
            })
            Cmd.AddCommand("+use", object : xcommand_t() {
                public fun execute() {
                    IN_UseDown()
                }
            })
            Cmd.AddCommand("-use", object : xcommand_t() {
                public fun execute() {
                    IN_UseUp()
                }
            })
            Cmd.AddCommand("impulse", object : xcommand_t() {
                public fun execute() {
                    IN_Impulse()
                }
            })
            Cmd.AddCommand("+klook", object : xcommand_t() {
                public fun execute() {
                    IN_KLookDown()
                }
            })
            Cmd.AddCommand("-klook", object : xcommand_t() {
                public fun execute() {
                    IN_KLookUp()
                }
            })

            cl_nodelta = Cvar.Get("cl_nodelta", "0", 0)
        }

        private val buf = sizebuf_t()
        private val data = ByteArray(128)
        private val nullcmd = usercmd_t()
        /*
	 * ================= CL_SendCmd =================
	 */
        fun SendCmd() {
            var i: Int
            var cmd: usercmd_t
            var oldcmd: usercmd_t
            val checksumIndex: Int

            // build a command even if not connected

            // save this command off for prediction
            i = Globals.cls.netchan.outgoing_sequence and (Defines.CMD_BACKUP - 1)
            cmd = Globals.cl.cmds[i]
            Globals.cl.cmd_time[i] = Globals.cls.realtime as Int // for netgraph
            // ping calculation

            // fill the cmd
            CreateCmd(cmd)

            Globals.cl.cmd.set(cmd)

            if (Globals.cls.state == Defines.ca_disconnected || Globals.cls.state == Defines.ca_connecting)
                return

            if (Globals.cls.state == Defines.ca_connected) {
                if (Globals.cls.netchan.message.cursize != 0 || Globals.curtime - Globals.cls.netchan.last_sent > 1000)
                    Netchan.Transmit(Globals.cls.netchan, 0, ByteArray(0))
                return
            }

            // send a userinfo update if needed
            if (Globals.userinfo_modified) {
                CL.FixUpGender()
                Globals.userinfo_modified = false
                MSG.WriteByte(Globals.cls.netchan.message, Defines.clc_userinfo)
                MSG.WriteString(Globals.cls.netchan.message, Cvar.Userinfo())
            }

            SZ.Init(buf, data, data.size())

            if (cmd.buttons != 0 && Globals.cl.cinematictime > 0 && !Globals.cl.attractloop && Globals.cls.realtime - Globals.cl.cinematictime > 1000) {
                // skip
                // the
                // rest
                // of
                // the
                // cinematic
                SCR.FinishCinematic()
            }

            // begin a client move command
            MSG.WriteByte(buf, Defines.clc_move)

            // save the position for a checksum byte
            checksumIndex = buf.cursize
            MSG.WriteByte(buf, 0)

            // let the server know what the last frame we
            // got was, so the next message can be delta compressed
            if (cl_nodelta.value != 0.0.toFloat() || !Globals.cl.frame.valid || Globals.cls.demowaiting)
                MSG.WriteLong(buf, -1) // no compression
            else
                MSG.WriteLong(buf, Globals.cl.frame.serverframe)

            // send this and the previous cmds in the message, so
            // if the last packet was dropped, it can be recovered
            i = (Globals.cls.netchan.outgoing_sequence - 2) and (Defines.CMD_BACKUP - 1)
            cmd = Globals.cl.cmds[i]
            //memset (nullcmd, 0, sizeof(nullcmd));
            nullcmd.clear()

            MSG.WriteDeltaUsercmd(buf, nullcmd, cmd)
            oldcmd = cmd

            i = (Globals.cls.netchan.outgoing_sequence - 1) and (Defines.CMD_BACKUP - 1)
            cmd = Globals.cl.cmds[i]

            MSG.WriteDeltaUsercmd(buf, oldcmd, cmd)
            oldcmd = cmd

            i = (Globals.cls.netchan.outgoing_sequence) and (Defines.CMD_BACKUP - 1)
            cmd = Globals.cl.cmds[i]

            MSG.WriteDeltaUsercmd(buf, oldcmd, cmd)

            // calculate a checksum over the move commands
            buf.data[checksumIndex] = Com.BlockSequenceCRCByte(buf.data, checksumIndex + 1, buf.cursize - checksumIndex - 1, Globals.cls.netchan.outgoing_sequence)

            //
            // deliver the message
            //
            Netchan.Transmit(Globals.cls.netchan, buf.cursize, buf.data)
        }
    }
}