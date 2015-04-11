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
import lwjake2.game.cmodel_t
import lwjake2.game.edict_t
import lwjake2.game.entity_state_t
import lwjake2.game.pmove_t
import lwjake2.game.trace_t
import lwjake2.game.usercmd_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Com
import lwjake2.qcommon.PMove
import lwjake2.util.Math3D

/**
 * CL_pred
 */
public class CL_pred {
    companion object {

        /*
     * =================== CL_CheckPredictionError ===================
     */
        fun CheckPredictionError() {
            var frame: Int
            val delta = IntArray(3)
            var i: Int
            val len: Int

            if (Globals.cl_predict.value == 0.0.toFloat() || (Globals.cl.frame.playerstate.pmove.pm_flags and pmove_t.PMF_NO_PREDICTION) != 0)
                return

            // calculate the last usercmd_t we sent that the server has processed
            frame = Globals.cls.netchan.incoming_acknowledged
            frame = frame and (Defines.CMD_BACKUP - 1)

            // compare what the server returned with what we had predicted it to be
            Math3D.VectorSubtract(Globals.cl.frame.playerstate.pmove.origin, Globals.cl.predicted_origins[frame], delta)

            // save the prediction error for interpolation
            len = Math.abs(delta[0]) + Math.abs(delta[1]) + Math.abs(delta[2])
            if (len > 640)
            // 80 world units
            {
                // a teleport or something
                Math3D.VectorClear(Globals.cl.prediction_error)
            } else {
                if (Globals.cl_showmiss.value != 0.0.toFloat() && (delta[0] != 0 || delta[1] != 0 || delta[2] != 0))
                    Com.Printf("prediction miss on " + Globals.cl.frame.serverframe + ": " + (delta[0] + delta[1] + delta[2]) + "\n")

                Math3D.VectorCopy(Globals.cl.frame.playerstate.pmove.origin, Globals.cl.predicted_origins[frame])

                // save for error itnerpolation
                run {
                    i = 0
                    while (i < 3) {
                        Globals.cl.prediction_error[i] = delta[i].toFloat() * 0.125.toFloat()
                        i++
                    }
                }
            }
        }

        /*
     * ==================== CL_ClipMoveToEntities
     * 
     * ====================
     */
        fun ClipMoveToEntities(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray, tr: trace_t) {
            var i: Int
            var x: Int
            var zd: Int
            var zu: Int
            var trace: trace_t
            var headnode: Int
            var angles: FloatArray
            var ent: entity_state_t
            var num: Int
            var cmodel: cmodel_t?
            val bmins = FloatArray(3)
            val bmaxs = FloatArray(3)

            run {
                i = 0
                while (i < Globals.cl.frame.num_entities) {
                    num = (Globals.cl.frame.parse_entities + i) and (Defines.MAX_PARSE_ENTITIES - 1)
                    ent = Globals.cl_parse_entities[num]

                    if (ent.solid == 0)
                        continue

                    if (ent.number == Globals.cl.playernum + 1)
                        continue

                    if (ent.solid == 31) {
                        // special value for bmodel
                        cmodel = Globals.cl.model_clip[ent.modelindex]
                        if (cmodel == null)
                            continue
                        headnode = cmodel!!.headnode
                        angles = ent.angles
                    } else {
                        // encoded bbox
                        x = 8 * (ent.solid and 31)
                        zd = 8 * ((ent.solid.ushr(5)) and 31)
                        zu = 8 * ((ent.solid.ushr(10)) and 63) - 32

                        bmins[0] = bmins[1] = (-x).toFloat()
                        bmaxs[0] = bmaxs[1] = x.toFloat()
                        bmins[2] = (-zd).toFloat()
                        bmaxs[2] = zu.toFloat()

                        headnode = CM.HeadnodeForBox(bmins, bmaxs)
                        angles = Globals.vec3_origin // boxes don't rotate
                    }

                    if (tr.allsolid)
                        return

                    trace = CM.TransformedBoxTrace(start, end, mins, maxs, headnode, Defines.MASK_PLAYERSOLID, ent.origin, angles)

                    if (trace.allsolid || trace.startsolid || trace.fraction < tr.fraction) {
                        trace.ent = ent.surrounding_ent
                        if (tr.startsolid) {
                            tr.set(trace) // rst: solved the Z U P P E L - P R O B L E
                            // M
                            tr.startsolid = true
                        } else
                            tr.set(trace) // rst: solved the Z U P P E L - P R O B L E
                        // M
                    } else if (trace.startsolid)
                        tr.startsolid = true
                    i++
                }
            }
        }

        /*
     * ================ CL_PMTrace ================
     */

        public var DUMMY_ENT: edict_t = edict_t(-1)

        fun PMTrace(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray): trace_t {
            val t: trace_t

            // check against world
            t = CM.BoxTrace(start, end, mins, maxs, 0, Defines.MASK_PLAYERSOLID)

            if (t.fraction < 1.0.toFloat()) {
                t.ent = DUMMY_ENT
            }

            // check all other solid models
            ClipMoveToEntities(start, mins, maxs, end, t)

            return t
        }

        /*
     * ================= PMpointcontents
     * 
     * Returns the content identificator of the point. =================
     */
        fun PMpointcontents(point: FloatArray): Int {
            var i: Int
            var ent: entity_state_t
            var num: Int
            var cmodel: cmodel_t?
            var contents: Int

            contents = CM.PointContents(point, 0)

            run {
                i = 0
                while (i < Globals.cl.frame.num_entities) {
                    num = (Globals.cl.frame.parse_entities + i) and (Defines.MAX_PARSE_ENTITIES - 1)
                    ent = Globals.cl_parse_entities[num]

                    if (ent.solid != 31)
                    // special value for bmodel
                        continue

                    cmodel = Globals.cl.model_clip[ent.modelindex]
                    if (cmodel == null)
                        continue

                    contents = contents or CM.TransformedPointContents(point, cmodel!!.headnode, ent.origin, ent.angles)
                    i++
                }
            }
            return contents
        }

        /*
     * ================= CL_PredictMovement
     * 
     * Sets cl.predicted_origin and cl.predicted_angles =================
     */
        fun PredictMovement() {

            if (Globals.cls.state != Defines.ca_active)
                return

            if (Globals.cl_paused.value != 0.0.toFloat())
                return

            if (Globals.cl_predict.value == 0.0.toFloat() || (Globals.cl.frame.playerstate.pmove.pm_flags and pmove_t.PMF_NO_PREDICTION) != 0) {
                // just set angles
                for (i in 0..3 - 1) {
                    Globals.cl.predicted_angles[i] = Globals.cl.viewangles[i] + Math3D.SHORT2ANGLE(Globals.cl.frame.playerstate.pmove.delta_angles[i])
                }
                return
            }

            var ack = Globals.cls.netchan.incoming_acknowledged
            val current = Globals.cls.netchan.outgoing_sequence

            // if we are too far out of date, just freeze
            if (current - ack >= Defines.CMD_BACKUP) {
                if (Globals.cl_showmiss.value != 0.0.toFloat())
                    Com.Printf("exceeded CMD_BACKUP\n")
                return
            }

            // copy current state to pmove
            //memset (pm, 0, sizeof(pm));
            val pm = pmove_t()

            pm.trace = object : pmove_t.TraceAdapter() {
                public fun trace(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray): trace_t {
                    return PMTrace(start, mins, maxs, end)
                }
            }
            pm.pointcontents = object : pmove_t.PointContentsAdapter() {
                public fun pointcontents(point: FloatArray): Int {
                    return PMpointcontents(point)
                }
            }

            try {
                PMove.pm_airaccelerate = Float.parseFloat(Globals.cl.configstrings[Defines.CS_AIRACCEL])
            } catch (e: Exception) {
                PMove.pm_airaccelerate = 0
            }


            // bugfix (rst) yeah !!!!!!!! found the solution to the B E W E G U N G
            // S P R O B L E M.
            pm.s.set(Globals.cl.frame.playerstate.pmove)

            // SCR_DebugGraph (current - ack - 1, 0);
            var frame = 0

            // run frames
            val cmd: usercmd_t
            while (++ack < current) {
                frame = ack and (Defines.CMD_BACKUP - 1)
                cmd = Globals.cl.cmds[frame]

                pm.cmd.set(cmd)

                PMove.Pmove(pm)

                // save for debug checking
                Math3D.VectorCopy(pm.s.origin, Globals.cl.predicted_origins[frame])
            }

            val oldframe = (ack - 2) and (Defines.CMD_BACKUP - 1)
            val oldz = Globals.cl.predicted_origins[oldframe][2]
            val step = pm.s.origin[2] - oldz
            if (step > 63 && step < 160 && (pm.s.pm_flags and pmove_t.PMF_ON_GROUND) != 0) {
                Globals.cl.predicted_step = step.toFloat() * 0.125.toFloat()
                Globals.cl.predicted_step_time = (Globals.cls.realtime - Globals.cls.frametime * 500) as Int
            }

            // copy results out for rendering
            Globals.cl.predicted_origin[0] = pm.s.origin[0] * 0.125.toFloat()
            Globals.cl.predicted_origin[1] = pm.s.origin[1] * 0.125.toFloat()
            Globals.cl.predicted_origin[2] = pm.s.origin[2] * 0.125.toFloat()

            Math3D.VectorCopy(pm.viewangles, Globals.cl.predicted_angles)
        }
    }
}