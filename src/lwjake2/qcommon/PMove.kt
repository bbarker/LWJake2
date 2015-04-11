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
import lwjake2.game.csurface_t
import lwjake2.game.pmove_t
import lwjake2.game.trace_t
import lwjake2.server.SV
import lwjake2.util.Math3D

public class PMove {

    // all of the locals will be zeroed before each
    // pmove, just to make damn sure we don't have
    // any differences when running on client or server

    public class pml_t {
        public var origin: FloatArray = floatArray(0.0, 0.0, 0.0) // full float precision

        public var velocity: FloatArray = floatArray(0.0, 0.0, 0.0) // full float precision

        public var forward: FloatArray = floatArray(0.0, 0.0, 0.0)
        public var right: FloatArray = floatArray(0.0, 0.0, 0.0)
        public var up: FloatArray = floatArray(0.0, 0.0, 0.0)

        public var frametime: Float = 0.toFloat()

        public var groundsurface: csurface_t? = null

        public var groundcontents: Int = 0

        public var previous_origin: FloatArray = floatArray(0.0, 0.0, 0.0)

        public var ladder: Boolean = false
    }

    companion object {

        public var pm: pmove_t

        public var pml: pml_t = pml_t()

        // movement parameters
        public var pm_stopspeed: Float = 100

        public var pm_maxspeed: Float = 300

        public var pm_duckspeed: Float = 100

        public var pm_accelerate: Float = 10

        public var pm_airaccelerate: Float = 0

        public var pm_wateraccelerate: Float = 10

        public var pm_friction: Float = 6

        public var pm_waterfriction: Float = 1

        public var pm_waterspeed: Float = 400

        // try all single bits first
        public var jitterbits: IntArray = intArray(0, 4, 1, 2, 3, 5, 6, 7)

        public var offset: IntArray = intArray(0, -1, 1)


        /**
         * Slide off of the impacting object returns the blocked flags (1 = floor, 2 = step / wall)
         */
        public fun PM_ClipVelocity(`in`: FloatArray, normal: FloatArray, out: FloatArray, overbounce: Float) {
            val backoff: Float
            var change: Float
            var i: Int

            backoff = Math3D.DotProduct(`in`, normal) * overbounce

            run {
                i = 0
                while (i < 3) {
                    change = normal[i] * backoff
                    out[i] = `in`[i] - change
                    if (out[i] > -Defines.MOVE_STOP_EPSILON && out[i] < Defines.MOVE_STOP_EPSILON)
                        out[i] = 0
                    i++
                }
            }
        }

        var planes = Array<FloatArray>(SV.MAX_CLIP_PLANES, { FloatArray(3) })

        public fun PM_StepSlideMove_() {
            var bumpcount: Int
            val numbumps: Int
            val dir = floatArray(0.0, 0.0, 0.0)
            var d: Float
            var numplanes: Int

            val primal_velocity = floatArray(0.0, 0.0, 0.0)
            var i: Int
            var j: Int
            var trace: trace_t
            val end = floatArray(0.0, 0.0, 0.0)
            val time_left: Float

            numbumps = 4

            Math3D.VectorCopy(pml.velocity, primal_velocity)
            numplanes = 0

            time_left = pml.frametime

            run {
                bumpcount = 0
                while (bumpcount < numbumps) {
                    run {
                        i = 0
                        while (i < 3) {
                            end[i] = pml.origin[i] + time_left * pml.velocity[i]
                            i++
                        }
                    }

                    trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, end)

                    if (trace.allsolid) {
                        // entity is trapped in another solid
                        pml.velocity[2] = 0 // don't build up falling damage
                        return
                    }

                    if (trace.fraction > 0) {
                        // actually covered some distance
                        Math3D.VectorCopy(trace.endpos, pml.origin)
                        numplanes = 0
                    }

                    if (trace.fraction == 1)
                        break // moved the entire distance

                    // save entity for contact
                    if (pm.numtouch < Defines.MAXTOUCH && trace.ent != null) {
                        pm.touchents[pm.numtouch] = trace.ent
                        pm.numtouch++
                    }

                    time_left -= time_left * trace.fraction

                    // slide along this plane
                    if (numplanes >= SV.MAX_CLIP_PLANES) {
                        // this shouldn't really happen
                        Math3D.VectorCopy(Globals.vec3_origin, pml.velocity)
                        break
                    }

                    Math3D.VectorCopy(trace.plane.normal, planes[numplanes])
                    numplanes++

                    // modify original_velocity so it parallels all of the clip planes
                    run {
                        i = 0
                        while (i < numplanes) {
                            PM_ClipVelocity(pml.velocity, planes[i], pml.velocity, 1.01.toFloat())
                            run {
                                j = 0
                                while (j < numplanes) {
                                    if (j != i) {
                                        if (Math3D.DotProduct(pml.velocity, planes[j]) < 0)
                                            break // not ok
                                    }
                                    j++
                                }
                            }
                            if (j == numplanes)
                                break
                            i++
                        }
                    }

                    if (i != numplanes) {
                        // go along this plane
                    } else {
                        // go along the crease
                        if (numplanes != 2) {
                            // Com.printf("clip velocity, numplanes == " + numplanes + "\n");
                            Math3D.VectorCopy(Globals.vec3_origin, pml.velocity)
                            break
                        }
                        Math3D.CrossProduct(planes[0], planes[1], dir)
                        d = Math3D.DotProduct(dir, pml.velocity)
                        Math3D.VectorScale(dir, d, pml.velocity)
                    }


                    // if velocity is against the original velocity, stop dead
                    // to avoid tiny occilations in sloping corners
                    if (Math3D.DotProduct(pml.velocity, primal_velocity) <= 0) {
                        Math3D.VectorCopy(Globals.vec3_origin, pml.velocity)
                        break
                    }
                    bumpcount++
                }
            }

            if (pm.s.pm_time != 0) {
                Math3D.VectorCopy(primal_velocity, pml.velocity)
            }
        }

        /**
         * Each intersection will try to step over the obstruction instead of
         * sliding along it.

         * Returns a new origin, velocity, and contact entity.
         * Does not modify any world state?
         */
        public fun PM_StepSlideMove() {
            val start_o = floatArray(0.0, 0.0, 0.0)
            val start_v = floatArray(0.0, 0.0, 0.0)
            val down_o = floatArray(0.0, 0.0, 0.0)
            val down_v = floatArray(0.0, 0.0, 0.0)
            var trace: trace_t
            val down_dist: Float
            val up_dist: Float
            //	float [] delta;
            val up = floatArray(0.0, 0.0, 0.0)
            val down = floatArray(0.0, 0.0, 0.0)

            Math3D.VectorCopy(pml.origin, start_o)
            Math3D.VectorCopy(pml.velocity, start_v)

            PM_StepSlideMove_()

            Math3D.VectorCopy(pml.origin, down_o)
            Math3D.VectorCopy(pml.velocity, down_v)

            Math3D.VectorCopy(start_o, up)
            up[2] += Defines.STEPSIZE

            trace = pm.trace.trace(up, pm.mins, pm.maxs, up)
            if (trace.allsolid)
                return  // can't step up

            // try sliding above
            Math3D.VectorCopy(up, pml.origin)
            Math3D.VectorCopy(start_v, pml.velocity)

            PM_StepSlideMove_()

            // push down the final amount
            Math3D.VectorCopy(pml.origin, down)
            down[2] -= Defines.STEPSIZE
            trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, down)
            if (!trace.allsolid) {
                Math3D.VectorCopy(trace.endpos, pml.origin)
            }

            Math3D.VectorCopy(pml.origin, up)

            // decide which one went farther
            down_dist = (down_o[0] - start_o[0]) * (down_o[0] - start_o[0]) + (down_o[1] - start_o[1]) * (down_o[1] - start_o[1])
            up_dist = (up[0] - start_o[0]) * (up[0] - start_o[0]) + (up[1] - start_o[1]) * (up[1] - start_o[1])

            if (down_dist > up_dist || trace.plane.normal[2] < Defines.MIN_STEP_NORMAL) {
                Math3D.VectorCopy(down_o, pml.origin)
                Math3D.VectorCopy(down_v, pml.velocity)
                return
            }
            //!! Special case
            // if we were walking along a plane, then we need to copy the Z over
            pml.velocity[2] = down_v[2]
        }

        /**
         * Handles both ground friction and water friction.
         */
        public fun PM_Friction() {
            val vel: FloatArray
            val speed: Float
            var newspeed: Float
            val control: Float
            val friction: Float
            var drop: Float

            vel = pml.velocity

            speed = (Math.sqrt(vel[0] * vel[0] + vel[1] * vel[1] + vel[2] * vel[2])) as Float
            if (speed < 1) {
                vel[0] = 0
                vel[1] = 0
                return
            }

            drop = 0

            // apply ground friction
            if ((pm.groundentity != null && pml.groundsurface != null && 0 == (pml.groundsurface!!.flags and Defines.SURF_SLICK)) || (pml.ladder)) {
                friction = pm_friction
                control = if (speed < pm_stopspeed) pm_stopspeed else speed
                drop += control * friction * pml.frametime
            }

            // apply water friction
            if (pm.waterlevel != 0 && !pml.ladder)
                drop += speed * pm_waterfriction * pm.waterlevel * pml.frametime

            // scale the velocity
            newspeed = speed - drop
            if (newspeed < 0) {
                newspeed = 0
            }
            newspeed /= speed

            vel[0] = vel[0] * newspeed
            vel[1] = vel[1] * newspeed
            vel[2] = vel[2] * newspeed
        }

        /**
         * Handles user intended acceleration.
         */
        public fun PM_Accelerate(wishdir: FloatArray, wishspeed: Float, accel: Float) {
            var i: Int
            val addspeed: Float
            var accelspeed: Float
            val currentspeed: Float

            currentspeed = Math3D.DotProduct(pml.velocity, wishdir)
            addspeed = wishspeed - currentspeed
            if (addspeed <= 0)
                return
            accelspeed = accel * pml.frametime * wishspeed
            if (accelspeed > addspeed)
                accelspeed = addspeed

            run {
                i = 0
                while (i < 3) {
                    pml.velocity[i] += accelspeed * wishdir[i]
                    i++
                }
            }
        }

        /**
         * PM_AirAccelerate.
         */

        public fun PM_AirAccelerate(wishdir: FloatArray, wishspeed: Float, accel: Float) {
            var i: Int
            val addspeed: Float
            var accelspeed: Float
            val currentspeed: Float
            var wishspd = wishspeed

            if (wishspd > 30)
                wishspd = 30
            currentspeed = Math3D.DotProduct(pml.velocity, wishdir)
            addspeed = wishspd - currentspeed
            if (addspeed <= 0)
                return
            accelspeed = accel * wishspeed * pml.frametime
            if (accelspeed > addspeed)
                accelspeed = addspeed

            run {
                i = 0
                while (i < 3) {
                    pml.velocity[i] += accelspeed * wishdir[i]
                    i++
                }
            }
        }

        /**
         * PM_AddCurrents.
         */
        public fun PM_AddCurrents(wishvel: FloatArray) {
            val v = floatArray(0.0, 0.0, 0.0)
            var s: Float

            // account for ladders
            if (pml.ladder && Math.abs(pml.velocity[2]) <= 200) {
                if ((pm.viewangles[Defines.PITCH] <= -15) && (pm.cmd.forwardmove > 0))
                    wishvel[2] = 200
                else if ((pm.viewangles[Defines.PITCH] >= 15) && (pm.cmd.forwardmove > 0))
                    wishvel[2] = (-200).toFloat()
                else if (pm.cmd.upmove > 0)
                    wishvel[2] = 200
                else if (pm.cmd.upmove < 0)
                    wishvel[2] = (-200).toFloat()
                else
                    wishvel[2] = 0

                // limit horizontal speed when on a ladder
                if (wishvel[0] < -25)
                    wishvel[0] = (-25).toFloat()
                else if (wishvel[0] > 25)
                    wishvel[0] = 25

                if (wishvel[1] < -25)
                    wishvel[1] = (-25).toFloat()
                else if (wishvel[1] > 25)
                    wishvel[1] = 25
            }

            // add water currents
            if ((pm.watertype and Defines.MASK_CURRENT) != 0) {
                Math3D.VectorClear(v)

                if ((pm.watertype and Defines.CONTENTS_CURRENT_0) != 0)
                    v[0] += 1
                if ((pm.watertype and Defines.CONTENTS_CURRENT_90) != 0)
                    v[1] += 1
                if ((pm.watertype and Defines.CONTENTS_CURRENT_180) != 0)
                    v[0] -= 1
                if ((pm.watertype and Defines.CONTENTS_CURRENT_270) != 0)
                    v[1] -= 1
                if ((pm.watertype and Defines.CONTENTS_CURRENT_UP) != 0)
                    v[2] += 1
                if ((pm.watertype and Defines.CONTENTS_CURRENT_DOWN) != 0)
                    v[2] -= 1

                s = pm_waterspeed
                if ((pm.waterlevel == 1) && (pm.groundentity != null))
                    s /= 2

                Math3D.VectorMA(wishvel, s, v, wishvel)
            }

            // add conveyor belt velocities
            if (pm.groundentity != null) {
                Math3D.VectorClear(v)

                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_0) != 0)
                    v[0] += 1
                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_90) != 0)
                    v[1] += 1
                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_180) != 0)
                    v[0] -= 1
                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_270) != 0)
                    v[1] -= 1
                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_UP) != 0)
                    v[2] += 1
                if ((pml.groundcontents and Defines.CONTENTS_CURRENT_DOWN) != 0)
                    v[2] -= 1

                Math3D.VectorMA(wishvel, 100 /* pm.groundentity.speed */, v, wishvel)
            }
        }

        /**
         * PM_WaterMove.
         */
        public fun PM_WaterMove() {
            var i: Int
            val wishvel = floatArray(0.0, 0.0, 0.0)
            var wishspeed: Float
            val wishdir = floatArray(0.0, 0.0, 0.0)


            // user intentions
            run {
                i = 0
                while (i < 3) {
                    wishvel[i] = pml.forward[i] * pm.cmd.forwardmove + pml.right[i] * pm.cmd.sidemove
                    i++
                }
            }

            if (0 == pm.cmd.forwardmove && 0 == pm.cmd.sidemove && 0 == pm.cmd.upmove)
                wishvel[2] -= 60 // drift towards bottom
            else
                wishvel[2] += pm.cmd.upmove

            PM_AddCurrents(wishvel)

            Math3D.VectorCopy(wishvel, wishdir)
            wishspeed = Math3D.VectorNormalize(wishdir)

            if (wishspeed > pm_maxspeed) {
                Math3D.VectorScale(wishvel, pm_maxspeed / wishspeed, wishvel)
                wishspeed = pm_maxspeed
            }
            wishspeed *= 0.5

            PM_Accelerate(wishdir, wishspeed, pm_wateraccelerate)

            PM_StepSlideMove()
        }

        /**
         * PM_AirMove.
         */
        public fun PM_AirMove() {
            val wishvel = floatArray(0.0, 0.0, 0.0)
            val fmove: Float
            val smove: Float
            val wishdir = floatArray(0.0, 0.0, 0.0)
            var wishspeed: Float
            val maxspeed: Float

            fmove = pm.cmd.forwardmove
            smove = pm.cmd.sidemove

            wishvel[0] = pml.forward[0] * fmove + pml.right[0] * smove
            wishvel[1] = pml.forward[1] * fmove + pml.right[1] * smove

            wishvel[2] = 0

            PM_AddCurrents(wishvel)

            Math3D.VectorCopy(wishvel, wishdir)
            wishspeed = Math3D.VectorNormalize(wishdir)


            // clamp to server defined max speed
            maxspeed = if ((pm.s.pm_flags and pmove_t.PMF_DUCKED) != 0)
                pm_duckspeed
            else
                pm_maxspeed

            if (wishspeed > maxspeed) {
                Math3D.VectorScale(wishvel, maxspeed / wishspeed, wishvel)
                wishspeed = maxspeed
            }

            if (pml.ladder) {
                PM_Accelerate(wishdir, wishspeed, pm_accelerate)
                if (0 == wishvel[2]) {
                    if (pml.velocity[2] > 0) {
                        pml.velocity[2] -= pm.s.gravity * pml.frametime
                        if (pml.velocity[2] < 0)
                            pml.velocity[2] = 0
                    } else {
                        pml.velocity[2] += pm.s.gravity * pml.frametime
                        if (pml.velocity[2] > 0)
                            pml.velocity[2] = 0
                    }
                }
                PM_StepSlideMove()
            } else if (pm.groundentity != null) {
                // walking on ground
                pml.velocity[2] = 0 //!!! this is before the accel
                PM_Accelerate(wishdir, wishspeed, pm_accelerate)

                // PGM -- fix for negative trigger_gravity fields
                //		pml.velocity[2] = 0;
                if (pm.s.gravity > 0)
                    pml.velocity[2] = 0
                else
                    pml.velocity[2] -= pm.s.gravity * pml.frametime
                // PGM
                if (0 == pml.velocity[0] && 0 == pml.velocity[1])
                    return
                PM_StepSlideMove()
            } else {
                // not on ground, so little effect on velocity
                if (pm_airaccelerate != 0)
                    PM_AirAccelerate(wishdir, wishspeed, pm_accelerate)
                else
                    PM_Accelerate(wishdir, wishspeed, 1)
                // add gravity
                pml.velocity[2] -= pm.s.gravity * pml.frametime
                PM_StepSlideMove()
            }
        }

        /**
         * PM_CatagorizePosition.
         */
        public fun PM_CatagorizePosition() {
            val point = floatArray(0.0, 0.0, 0.0)
            var cont: Int
            val trace: trace_t
            val sample1: Int
            val sample2: Int

            // if the player hull point one unit down is solid, the player
            // is on ground

            // see if standing on something solid
            point[0] = pml.origin[0]
            point[1] = pml.origin[1]
            point[2] = pml.origin[2] - 0.25.toFloat()
            if (pml.velocity[2] > 180)
            //!!ZOID changed from 100 to 180 (ramp
            // accel)
            {
                pm.s.pm_flags = pm.s.pm_flags and pmove_t.PMF_ON_GROUND.inv()
                pm.groundentity = null
            } else {
                trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, point)
                pml.groundsurface = trace.surface
                pml.groundcontents = trace.contents

                if (null == trace.ent || (trace.plane.normal[2] < 0.7 && !trace.startsolid)) {
                    pm.groundentity = null
                    pm.s.pm_flags = pm.s.pm_flags and pmove_t.PMF_ON_GROUND.inv()
                } else {
                    pm.groundentity = trace.ent
                    // hitting solid ground will end a waterjump
                    if ((pm.s.pm_flags and pmove_t.PMF_TIME_WATERJUMP) != 0) {
                        pm.s.pm_flags = pm.s.pm_flags and (pmove_t.PMF_TIME_WATERJUMP or pmove_t.PMF_TIME_LAND or pmove_t.PMF_TIME_TELEPORT).inv()
                        pm.s.pm_time = 0
                    }

                    if (0 == (pm.s.pm_flags and pmove_t.PMF_ON_GROUND)) {

                        // just hit the ground
                        pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_ON_GROUND
                        // don't do landing time if we were just going down a slope
                        if (pml.velocity[2] < -200) {
                            pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_TIME_LAND
                            // don't allow another jump for a little while
                            if (pml.velocity[2] < -400)
                                pm.s.pm_time = 25
                            else
                                pm.s.pm_time = 18
                        }
                    }
                }

                if (pm.numtouch < Defines.MAXTOUCH && trace.ent != null) {
                    pm.touchents[pm.numtouch] = trace.ent
                    pm.numtouch++
                }
            }


            // get waterlevel, accounting for ducking

            pm.waterlevel = 0
            pm.watertype = 0

            sample2 = (pm.viewheight - pm.mins[2]) as Int
            sample1 = sample2 / 2

            point[2] = pml.origin[2] + pm.mins[2] + 1
            cont = pm.pointcontents.pointcontents(point)

            if ((cont and Defines.MASK_WATER) != 0) {
                pm.watertype = cont
                pm.waterlevel = 1
                point[2] = pml.origin[2] + pm.mins[2] + sample1
                cont = pm.pointcontents.pointcontents(point)
                if ((cont and Defines.MASK_WATER) != 0) {
                    pm.waterlevel = 2
                    point[2] = pml.origin[2] + pm.mins[2] + sample2
                    cont = pm.pointcontents.pointcontents(point)
                    if ((cont and Defines.MASK_WATER) != 0)
                        pm.waterlevel = 3
                }
            }

        }

        /**
         * PM_CheckJump.
         */
        public fun PM_CheckJump() {
            if ((pm.s.pm_flags and pmove_t.PMF_TIME_LAND) != 0) {
                // hasn't been long enough since landing to jump again
                return
            }

            if (pm.cmd.upmove < 10) {
                // not holding jump
                pm.s.pm_flags = pm.s.pm_flags and pmove_t.PMF_JUMP_HELD.inv()
                return
            }

            // must wait for jump to be released
            if ((pm.s.pm_flags and pmove_t.PMF_JUMP_HELD) != 0)
                return

            if (pm.s.pm_type == Defines.PM_DEAD)
                return

            if (pm.waterlevel >= 2) {
                // swimming, not jumping
                pm.groundentity = null

                if (pml.velocity[2] <= -300)
                    return

                if (pm.watertype == Defines.CONTENTS_WATER)
                    pml.velocity[2] = 100
                else if (pm.watertype == Defines.CONTENTS_SLIME)
                    pml.velocity[2] = 80
                else
                    pml.velocity[2] = 50
                return
            }

            if (pm.groundentity == null)
                return  // in air, so no effect

            pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_JUMP_HELD

            pm.groundentity = null
            pml.velocity[2] += 270
            if (pml.velocity[2] < 270)
                pml.velocity[2] = 270
        }

        /**
         * PM_CheckSpecialMovement.
         */
        public fun PM_CheckSpecialMovement() {
            val spot = floatArray(0.0, 0.0, 0.0)
            var cont: Int
            val flatforward = floatArray(0.0, 0.0, 0.0)
            val trace: trace_t

            if (pm.s.pm_time != 0)
                return

            pml.ladder = false

            // check for ladder
            flatforward[0] = pml.forward[0]
            flatforward[1] = pml.forward[1]
            flatforward[2] = 0
            Math3D.VectorNormalize(flatforward)

            Math3D.VectorMA(pml.origin, 1, flatforward, spot)
            trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, spot)
            if ((trace.fraction < 1) && (trace.contents and Defines.CONTENTS_LADDER) != 0)
                pml.ladder = true

            // check for water jump
            if (pm.waterlevel != 2)
                return

            Math3D.VectorMA(pml.origin, 30, flatforward, spot)
            spot[2] += 4
            cont = pm.pointcontents.pointcontents(spot)
            if (0 == (cont and Defines.CONTENTS_SOLID))
                return

            spot[2] += 16
            cont = pm.pointcontents.pointcontents(spot)
            if (cont != 0)
                return
            // jump out of water
            Math3D.VectorScale(flatforward, 50, pml.velocity)
            pml.velocity[2] = 350

            pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_TIME_WATERJUMP
            pm.s.pm_time = -1 // was 255
        }

        /**
         * PM_FlyMove.
         */
        public fun PM_FlyMove(doclip: Boolean) {
            val speed: Float
            var drop: Float
            val friction: Float
            val control: Float
            var newspeed: Float
            val currentspeed: Float
            val addspeed: Float
            var accelspeed: Float
            var i: Int
            val wishvel = floatArray(0.0, 0.0, 0.0)
            val fmove: Float
            val smove: Float
            val wishdir = floatArray(0.0, 0.0, 0.0)
            var wishspeed: Float
            val end = floatArray(0.0, 0.0, 0.0)
            val trace: trace_t

            pm.viewheight = 22

            // friction

            speed = Math3D.VectorLength(pml.velocity)
            if (speed < 1) {
                Math3D.VectorCopy(Globals.vec3_origin, pml.velocity)
            } else {
                drop = 0

                friction = pm_friction * 1.5.toFloat() // extra friction
                control = if (speed < pm_stopspeed) pm_stopspeed else speed
                drop += control * friction * pml.frametime

                // scale the velocity
                newspeed = speed - drop
                if (newspeed < 0)
                    newspeed = 0
                newspeed /= speed

                Math3D.VectorScale(pml.velocity, newspeed, pml.velocity)
            }

            // accelerate
            fmove = pm.cmd.forwardmove
            smove = pm.cmd.sidemove

            Math3D.VectorNormalize(pml.forward)
            Math3D.VectorNormalize(pml.right)

            run {
                i = 0
                while (i < 3) {
                    wishvel[i] = pml.forward[i] * fmove + pml.right[i] * smove
                    i++
                }
            }
            wishvel[2] += pm.cmd.upmove

            Math3D.VectorCopy(wishvel, wishdir)
            wishspeed = Math3D.VectorNormalize(wishdir)

            // clamp to server defined max speed
            if (wishspeed > pm_maxspeed) {
                Math3D.VectorScale(wishvel, pm_maxspeed / wishspeed, wishvel)
                wishspeed = pm_maxspeed
            }

            currentspeed = Math3D.DotProduct(pml.velocity, wishdir)
            addspeed = wishspeed - currentspeed
            if (addspeed <= 0)
                return
            accelspeed = pm_accelerate * pml.frametime * wishspeed
            if (accelspeed > addspeed)
                accelspeed = addspeed

            run {
                i = 0
                while (i < 3) {
                    pml.velocity[i] += accelspeed * wishdir[i]
                    i++
                }
            }

            if (doclip) {
                run {
                    i = 0
                    while (i < 3) {
                        end[i] = pml.origin[i] + pml.frametime * pml.velocity[i]
                        i++
                    }
                }

                trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, end)

                Math3D.VectorCopy(trace.endpos, pml.origin)
            } else {
                // move
                Math3D.VectorMA(pml.origin, pml.frametime, pml.velocity, pml.origin)
            }
        }

        /**
         * Sets mins, maxs, and pm.viewheight.
         */
        public fun PM_CheckDuck() {
            val trace: trace_t

            pm.mins[0] = -16
            pm.mins[1] = -16

            pm.maxs[0] = 16
            pm.maxs[1] = 16

            if (pm.s.pm_type == Defines.PM_GIB) {
                pm.mins[2] = 0
                pm.maxs[2] = 16
                pm.viewheight = 8
                return
            }

            pm.mins[2] = -24

            if (pm.s.pm_type == Defines.PM_DEAD) {
                pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_DUCKED
            } else if (pm.cmd.upmove < 0 && (pm.s.pm_flags and pmove_t.PMF_ON_GROUND) != 0) {
                // duck
                pm.s.pm_flags = pm.s.pm_flags or pmove_t.PMF_DUCKED
            } else {
                // stand up if possible
                if ((pm.s.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                    // try to stand up
                    pm.maxs[2] = 32
                    trace = pm.trace.trace(pml.origin, pm.mins, pm.maxs, pml.origin)
                    if (!trace.allsolid)
                        pm.s.pm_flags = pm.s.pm_flags and pmove_t.PMF_DUCKED.inv()
                }
            }

            if ((pm.s.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                pm.maxs[2] = 4
                pm.viewheight = -2
            } else {
                pm.maxs[2] = 32
                pm.viewheight = 22
            }
        }

        /**
         * Dead bodies have extra friction.
         */
        public fun PM_DeadMove() {
            var forward: Float

            if (null == pm.groundentity)
                return

            // extra friction
            forward = Math3D.VectorLength(pml.velocity)
            forward -= 20
            if (forward <= 0) {
                Math3D.VectorClear(pml.velocity)
            } else {
                Math3D.VectorNormalize(pml.velocity)
                Math3D.VectorScale(pml.velocity, forward, pml.velocity)
            }
        }

        public fun PM_GoodPosition(): Boolean {
            val trace: trace_t
            val origin = floatArray(0.0, 0.0, 0.0)
            val end = floatArray(0.0, 0.0, 0.0)
            var i: Int

            if (pm.s.pm_type == Defines.PM_SPECTATOR)
                return true

            run {
                i = 0
                while (i < 3) {
                    origin[i] = end[i] = pm.s.origin[i] * 0.125.toFloat()
                    i++
                }
            }
            trace = pm.trace.trace(origin, pm.mins, pm.maxs, end)

            return !trace.allsolid
        }

        /**
         * On exit, the origin will have a value that is pre-quantized to the 0.125
         * precision of the network channel and in a valid position.
         */

        public fun PM_SnapPosition() {
            val sign = intArray(0, 0, 0)
            var i: Int
            var j: Int
            var bits: Int
            val base = shortArray(0, 0, 0)

            // snap velocity to eigths
            run {
                i = 0
                while (i < 3) {
                    pm.s.velocity[i] = (pml.velocity[i] * 8).toShort()
                    i++
                }
            }

            run {
                i = 0
                while (i < 3) {
                    if (pml.origin[i] >= 0)
                        sign[i] = 1
                    else
                        sign[i] = -1
                    pm.s.origin[i] = (pml.origin[i] * 8).toShort()
                    if (pm.s.origin[i] * 0.125 == pml.origin[i])
                        sign[i] = 0
                    i++
                }
            }
            Math3D.VectorCopy(pm.s.origin, base)

            // try all combinations
            run {
                j = 0
                while (j < 8) {
                    bits = jitterbits[j]
                    Math3D.VectorCopy(base, pm.s.origin)
                    run {
                        i = 0
                        while (i < 3) {
                            if ((bits and (1 shl i)) != 0)
                                pm.s.origin[i] += sign[i]
                            i++
                        }
                    }

                    if (PM_GoodPosition())
                        return
                    j++
                }
            }

            // go back to the last position
            Math3D.VectorCopy(pml.previous_origin, pm.s.origin)
            // Com.DPrintf("using previous_origin\n");
        }

        /**
         * Snaps the origin of the player move to 0.125 grid.
         */
        public fun PM_InitialSnapPosition() {
            var x: Int
            var y: Int
            var z: Int
            val base = shortArray(0, 0, 0)

            Math3D.VectorCopy(pm.s.origin, base)

            run {
                z = 0
                while (z < 3) {
                    pm.s.origin[2] = (base[2].toInt() + offset[z]).toShort()
                    run {
                        y = 0
                        while (y < 3) {
                            pm.s.origin[1] = (base[1].toInt() + offset[y]).toShort()
                            run {
                                x = 0
                                while (x < 3) {
                                    pm.s.origin[0] = (base[0].toInt() + offset[x]).toShort()
                                    if (PM_GoodPosition()) {
                                        pml.origin[0] = pm.s.origin[0] * 0.125.toFloat()
                                        pml.origin[1] = pm.s.origin[1] * 0.125.toFloat()
                                        pml.origin[2] = pm.s.origin[2] * 0.125.toFloat()
                                        Math3D.VectorCopy(pm.s.origin, pml.previous_origin)
                                        return
                                    }
                                    x++
                                }
                            }
                            y++
                        }
                    }
                    z++
                }
            }

            Com.DPrintf("Bad InitialSnapPosition\n")
        }

        /**
         * PM_ClampAngles.
         */
        public fun PM_ClampAngles() {
            var temp: Short
            var i: Int

            if ((pm.s.pm_flags and pmove_t.PMF_TIME_TELEPORT) != 0) {
                pm.viewangles[Defines.YAW] = Math3D.SHORT2ANGLE(pm.cmd.angles[Defines.YAW] + pm.s.delta_angles[Defines.YAW])
                pm.viewangles[Defines.PITCH] = 0
                pm.viewangles[Defines.ROLL] = 0
            } else {
                // circularly clamp the angles with deltas
                run {
                    i = 0
                    while (i < 3) {
                        temp = (pm.cmd.angles[i] + pm.s.delta_angles[i]) as Short
                        pm.viewangles[i] = Math3D.SHORT2ANGLE(temp)
                        i++
                    }
                }

                // don't let the player look up or down more than 90 degrees
                if (pm.viewangles[Defines.PITCH] > 89 && pm.viewangles[Defines.PITCH] < 180)
                    pm.viewangles[Defines.PITCH] = 89
                else if (pm.viewangles[Defines.PITCH] < 271 && pm.viewangles[Defines.PITCH] >= 180)
                    pm.viewangles[Defines.PITCH] = 271
            }
            Math3D.AngleVectors(pm.viewangles, pml.forward, pml.right, pml.up)
        }

        /**
         * Can be called by either the server or the client.
         */
        public fun Pmove(pmove: pmove_t) {
            pm = pmove

            // clear results
            pm.numtouch = 0
            Math3D.VectorClear(pm.viewangles)
            pm.viewheight = 0
            pm.groundentity = null
            pm.watertype = 0
            pm.waterlevel = 0

            pml.groundsurface = null
            pml.groundcontents = 0

            // convert origin and velocity to float values
            pml.origin[0] = pm.s.origin[0] * 0.125.toFloat()
            pml.origin[1] = pm.s.origin[1] * 0.125.toFloat()
            pml.origin[2] = pm.s.origin[2] * 0.125.toFloat()

            pml.velocity[0] = pm.s.velocity[0] * 0.125.toFloat()
            pml.velocity[1] = pm.s.velocity[1] * 0.125.toFloat()
            pml.velocity[2] = pm.s.velocity[2] * 0.125.toFloat()

            // save old org in case we get stuck
            Math3D.VectorCopy(pm.s.origin, pml.previous_origin)

            pml.frametime = (pm.cmd.msec and 255) * 0.001.toFloat()

            PM_ClampAngles()

            if (pm.s.pm_type == Defines.PM_SPECTATOR) {
                PM_FlyMove(false)
                PM_SnapPosition()
                return
            }

            if (pm.s.pm_type >= Defines.PM_DEAD) {
                pm.cmd.forwardmove = 0
                pm.cmd.sidemove = 0
                pm.cmd.upmove = 0
            }

            if (pm.s.pm_type == Defines.PM_FREEZE)
                return  // no movement at all

            // set mins, maxs, and viewheight
            PM_CheckDuck()

            if (pm.snapinitial)
                PM_InitialSnapPosition()

            // set groundentity, watertype, and waterlevel
            PM_CatagorizePosition()

            if (pm.s.pm_type == Defines.PM_DEAD)
                PM_DeadMove()

            PM_CheckSpecialMovement()

            // drop timing counter
            if (pm.s.pm_time != 0) {
                var msec: Int

                // TOD o bugfix cwei
                msec = pm.cmd.msec.ushr(3)
                if (msec == 0)
                    msec = 1
                if (msec >= (pm.s.pm_time and 255)) {
                    pm.s.pm_flags = pm.s.pm_flags and (pmove_t.PMF_TIME_WATERJUMP or pmove_t.PMF_TIME_LAND or pmove_t.PMF_TIME_TELEPORT).inv()
                    pm.s.pm_time = 0
                } else
                    pm.s.pm_time = ((pm.s.pm_time and 255) - msec) as Byte
            }

            if ((pm.s.pm_flags and pmove_t.PMF_TIME_TELEPORT) != 0) {
                // teleport pause stays exaclty in place
            } else if ((pm.s.pm_flags and pmove_t.PMF_TIME_WATERJUMP) != 0) {
                // waterjump has no control, but falls
                pml.velocity[2] -= pm.s.gravity * pml.frametime
                if (pml.velocity[2] < 0) {
                    // cancel as soon as we are falling down again
                    pm.s.pm_flags = pm.s.pm_flags and (pmove_t.PMF_TIME_WATERJUMP or pmove_t.PMF_TIME_LAND or pmove_t.PMF_TIME_TELEPORT).inv()
                    pm.s.pm_time = 0
                }

                PM_StepSlideMove()
            } else {
                PM_CheckJump()

                PM_Friction()

                if (pm.waterlevel >= 2)
                    PM_WaterMove()
                else {
                    val angles = floatArray(0.0, 0.0, 0.0)

                    Math3D.VectorCopy(pm.viewangles, angles)

                    if (angles[Defines.PITCH] > 180)
                        angles[Defines.PITCH] = angles[Defines.PITCH] - 360

                    angles[Defines.PITCH] /= 3

                    Math3D.AngleVectors(angles, pml.forward, pml.right, pml.up)

                    PM_AirMove()
                }
            }

            // set groundentity, watertype, and waterlevel for final spot
            PM_CatagorizePosition()
            PM_SnapPosition()
        }
    }
}