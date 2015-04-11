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
import lwjake2.server.SV_WORLD
import lwjake2.util.Math3D

public class GameChase {
    companion object {

        public fun UpdateChaseCam(ent: edict_t) {
            val o = floatArray(0.0, 0.0, 0.0)
            val ownerv = floatArray(0.0, 0.0, 0.0)
            val goal = floatArray(0.0, 0.0, 0.0)
            val targ: edict_t
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            var trace: trace_t
            var i: Int
            val oldgoal = floatArray(0.0, 0.0, 0.0)
            val angles = floatArray(0.0, 0.0, 0.0)

            // is our chase target gone?
            if (!ent.client.chase_target.inuse || ent.client.chase_target.client.resp.spectator) {
                val old = ent.client.chase_target
                ChaseNext(ent)
                if (ent.client.chase_target == old) {
                    ent.client.chase_target = null
                    ent.client.ps.pmove.pm_flags = ent.client.ps.pmove.pm_flags and pmove_t.PMF_NO_PREDICTION.inv()
                    return
                }
            }

            targ = ent.client.chase_target

            Math3D.VectorCopy(targ.s.origin, ownerv)
            Math3D.VectorCopy(ent.s.origin, oldgoal)

            ownerv[2] += targ.viewheight

            Math3D.VectorCopy(targ.client.v_angle, angles)
            if (angles[Defines.PITCH] > 56)
                angles[Defines.PITCH] = 56
            Math3D.AngleVectors(angles, forward, right, null)
            Math3D.VectorNormalize(forward)
            Math3D.VectorMA(ownerv, -30, forward, o)

            if (o[2] < targ.s.origin[2] + 20)
                o[2] = targ.s.origin[2] + 20

            // jump animation lifts
            if (targ.groundentity == null)
                o[2] += 16

            trace = GameBase.gi.trace(ownerv, Globals.vec3_origin, Globals.vec3_origin, o, targ, Defines.MASK_SOLID)

            Math3D.VectorCopy(trace.endpos, goal)

            Math3D.VectorMA(goal, 2, forward, goal)

            // pad for floors and ceilings
            Math3D.VectorCopy(goal, o)
            o[2] += 6
            trace = GameBase.gi.trace(goal, Globals.vec3_origin, Globals.vec3_origin, o, targ, Defines.MASK_SOLID)
            if (trace.fraction < 1) {
                Math3D.VectorCopy(trace.endpos, goal)
                goal[2] -= 6
            }

            Math3D.VectorCopy(goal, o)
            o[2] -= 6
            trace = GameBase.gi.trace(goal, Globals.vec3_origin, Globals.vec3_origin, o, targ, Defines.MASK_SOLID)
            if (trace.fraction < 1) {
                Math3D.VectorCopy(trace.endpos, goal)
                goal[2] += 6
            }

            if (targ.deadflag != 0)
                ent.client.ps.pmove.pm_type = Defines.PM_DEAD
            else
                ent.client.ps.pmove.pm_type = Defines.PM_FREEZE

            Math3D.VectorCopy(goal, ent.s.origin)
            run {
                i = 0
                while (i < 3) {
                    ent.client.ps.pmove.delta_angles[i] = Math3D.ANGLE2SHORT(targ.client.v_angle[i] - ent.client.resp.cmd_angles[i]) as Short
                    i++
                }
            }

            if (targ.deadflag != 0) {
                ent.client.ps.viewangles[Defines.ROLL] = 40
                ent.client.ps.viewangles[Defines.PITCH] = -15
                ent.client.ps.viewangles[Defines.YAW] = targ.client.killer_yaw
            } else {
                Math3D.VectorCopy(targ.client.v_angle, ent.client.ps.viewangles)
                Math3D.VectorCopy(targ.client.v_angle, ent.client.v_angle)
            }

            ent.viewheight = 0
            ent.client.ps.pmove.pm_flags = ent.client.ps.pmove.pm_flags or pmove_t.PMF_NO_PREDICTION
            SV_WORLD.SV_LinkEdict(ent)
        }

        public fun ChaseNext(ent: edict_t) {
            var i: Int
            val e: edict_t

            if (null == ent.client.chase_target)
                return

            i = ent.client.chase_target.index
            do {
                i++
                if (i > GameBase.maxclients.value)
                    i = 1
                e = GameBase.g_edicts[i]

                if (!e.inuse)
                    continue
                if (!e.client.resp.spectator)
                    break
            } while (e != ent.client.chase_target)

            ent.client.chase_target = e
            ent.client.update_chase = true
        }

        public fun ChasePrev(ent: edict_t) {
            var i: Int
            val e: edict_t

            if (ent.client.chase_target == null)
                return

            i = ent.client.chase_target.index
            do {
                i--
                if (i < 1)
                    i = GameBase.maxclients.value as Int
                e = GameBase.g_edicts[i]
                if (!e.inuse)
                    continue
                if (!e.client.resp.spectator)
                    break
            } while (e != ent.client.chase_target)

            ent.client.chase_target = e
            ent.client.update_chase = true
        }

        public fun GetChaseTarget(ent: edict_t) {
            var i: Int
            var other: edict_t

            run {
                i = 1
                while (i <= GameBase.maxclients.value) {
                    other = GameBase.g_edicts[i]
                    if (other.inuse && !other.client.resp.spectator) {
                        ent.client.chase_target = other
                        ent.client.update_chase = true
                        UpdateChaseCam(ent)
                        return
                    }
                    i++
                }
            }
            GameBase.gi.centerprintf(ent, "No other players to chase.")
        }
    }
}
