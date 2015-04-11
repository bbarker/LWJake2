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
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class GameFunc {
    companion object {

        fun Move_Calc(ent: edict_t, dest: FloatArray, func: EntThinkAdapter) {
            Math3D.VectorClear(ent.velocity)
            Math3D.VectorSubtract(dest, ent.s.origin, ent.moveinfo.dir)
            ent.moveinfo.remaining_distance = Math3D.VectorNormalize(ent.moveinfo.dir)

            ent.moveinfo.endfunc = func

            if (ent.moveinfo.speed == ent.moveinfo.accel && ent.moveinfo.speed == ent.moveinfo.decel) {
                if (GameBase.level.current_entity == (if ((ent.flags and Defines.FL_TEAMSLAVE) != 0)
                    ent.teammaster
                else
                    ent)) {
                    Move_Begin.think(ent)
                } else {
                    ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                    ent.think = Move_Begin
                }
            } else {
                // accelerative
                ent.moveinfo.current_speed = 0
                ent.think = Think_AccelMove
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
            }
        }

        fun AngleMove_Calc(ent: edict_t, func: EntThinkAdapter) {
            Math3D.VectorClear(ent.avelocity)
            ent.moveinfo.endfunc = func
            if (GameBase.level.current_entity == (if ((ent.flags and Defines.FL_TEAMSLAVE) != 0)
                ent.teammaster
            else
                ent)) {
                AngleMove_Begin.think(ent)
            } else {
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                ent.think = AngleMove_Begin
            }
        }

        /**
         * Think_AccelMove

         * The team has completed a frame of movement, so change the speed for the
         * next frame.
         */
        fun AccelerationDistance(target: Float, rate: Float): Float {
            return target * ((target / rate) + 1) / 2
        }

        fun plat_CalcAcceleratedMove(moveinfo: moveinfo_t) {
            val accel_dist: Float
            var decel_dist: Float

            moveinfo.move_speed = moveinfo.speed

            if (moveinfo.remaining_distance < moveinfo.accel) {
                moveinfo.current_speed = moveinfo.remaining_distance
                return
            }

            accel_dist = AccelerationDistance(moveinfo.speed, moveinfo.accel)
            decel_dist = AccelerationDistance(moveinfo.speed, moveinfo.decel)

            if ((moveinfo.remaining_distance - accel_dist - decel_dist) < 0) {
                val f: Float

                f = (moveinfo.accel + moveinfo.decel) / (moveinfo.accel * moveinfo.decel)
                moveinfo.move_speed = ((-2 + Math.sqrt(4 - 4 * f * (-2 * moveinfo.remaining_distance))) / (2 * f)) as Float
                decel_dist = AccelerationDistance(moveinfo.move_speed, moveinfo.decel)
            }

            moveinfo.decel_distance = decel_dist
        }

        fun plat_Accelerate(moveinfo: moveinfo_t) {
            // are we decelerating?
            if (moveinfo.remaining_distance <= moveinfo.decel_distance) {
                if (moveinfo.remaining_distance < moveinfo.decel_distance) {
                    if (moveinfo.next_speed != 0) {
                        moveinfo.current_speed = moveinfo.next_speed
                        moveinfo.next_speed = 0
                        return
                    }
                    if (moveinfo.current_speed > moveinfo.decel)
                        moveinfo.current_speed -= moveinfo.decel
                }
                return
            }

            // are we at full speed and need to start decelerating during this move?
            if (moveinfo.current_speed == moveinfo.move_speed)
                if ((moveinfo.remaining_distance - moveinfo.current_speed) < moveinfo.decel_distance) {
                    val p1_distance: Float
                    val p2_distance: Float
                    val distance: Float

                    p1_distance = moveinfo.remaining_distance - moveinfo.decel_distance
                    p2_distance = moveinfo.move_speed * (1.0.toFloat() - (p1_distance / moveinfo.move_speed))
                    distance = p1_distance + p2_distance
                    moveinfo.current_speed = moveinfo.move_speed
                    moveinfo.next_speed = moveinfo.move_speed - moveinfo.decel * (p2_distance / distance)
                    return
                }

            // are we accelerating?
            if (moveinfo.current_speed < moveinfo.speed) {
                val old_speed: Float
                val p1_distance: Float
                val p1_speed: Float
                val p2_distance: Float
                val distance: Float

                old_speed = moveinfo.current_speed

                // figure simple acceleration up to move_speed
                moveinfo.current_speed += moveinfo.accel
                if (moveinfo.current_speed > moveinfo.speed)
                    moveinfo.current_speed = moveinfo.speed

                // are we accelerating throughout this entire move?
                if ((moveinfo.remaining_distance - moveinfo.current_speed) >= moveinfo.decel_distance)
                    return

                // during this move we will accelrate from current_speed to
                // move_speed
                // and cross over the decel_distance; figure the average speed for
                // the
                // entire move
                p1_distance = moveinfo.remaining_distance - moveinfo.decel_distance
                p1_speed = (old_speed + moveinfo.move_speed) / 2.0.toFloat()
                p2_distance = moveinfo.move_speed * (1.0.toFloat() - (p1_distance / p1_speed))
                distance = p1_distance + p2_distance
                moveinfo.current_speed = (p1_speed * (p1_distance / distance)) + (moveinfo.move_speed * (p2_distance / distance))
                moveinfo.next_speed = moveinfo.move_speed - moveinfo.decel * (p2_distance / distance)
                return
            }

            // we are at constant velocity (move_speed)
            return
        }

        fun plat_go_up(ent: edict_t) {
            if (0 == (ent.flags and Defines.FL_TEAMSLAVE)) {
                if (ent.moveinfo.sound_start != 0)
                    GameBase.gi.sound(ent, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, ent.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                ent.s.sound = ent.moveinfo.sound_middle
            }
            ent.moveinfo.state = STATE_UP
            Move_Calc(ent, ent.moveinfo.start_origin, plat_hit_top)
        }

        fun plat_spawn_inside_trigger(ent: edict_t) {
            val trigger: edict_t
            val tmin = floatArray(0.0, 0.0, 0.0)
            val tmax = floatArray(0.0, 0.0, 0.0)

            //
            //	   middle trigger
            //
            trigger = GameUtil.G_Spawn()
            trigger.touch = Touch_Plat_Center
            trigger.movetype = Defines.MOVETYPE_NONE
            trigger.solid = Defines.SOLID_TRIGGER
            trigger.enemy = ent

            tmin[0] = ent.mins[0] + 25
            tmin[1] = ent.mins[1] + 25
            tmin[2] = ent.mins[2]

            tmax[0] = ent.maxs[0] - 25
            tmax[1] = ent.maxs[1] - 25
            tmax[2] = ent.maxs[2] + 8

            tmin[2] = tmax[2] - (ent.pos1[2] - ent.pos2[2] + GameBase.st.lip)

            if ((ent.spawnflags and PLAT_LOW_TRIGGER) != 0)
                tmax[2] = tmin[2] + 8

            if (tmax[0] - tmin[0] <= 0) {
                tmin[0] = (ent.mins[0] + ent.maxs[0]) * 0.5.toFloat()
                tmax[0] = tmin[0] + 1
            }
            if (tmax[1] - tmin[1] <= 0) {
                tmin[1] = (ent.mins[1] + ent.maxs[1]) * 0.5.toFloat()
                tmax[1] = tmin[1] + 1
            }

            Math3D.VectorCopy(tmin, trigger.mins)
            Math3D.VectorCopy(tmax, trigger.maxs)

            GameBase.gi.linkentity(trigger)
        }

        /**
         * QUAKED func_plat (0 .5 .8) ? PLAT_LOW_TRIGGER speed default 150

         * Plats are always drawn in the extended position, so they will light
         * correctly.

         * If the plat is the target of another trigger or button, it will start out
         * disabled in the extended position until it is trigger, when it will lower
         * and become a normal plat.

         * "speed" overrides default 200. "accel" overrides default 500 "lip"
         * overrides default 8 pixel lip

         * If the "height" key is set, that will determine the amount the plat
         * moves, instead of being implicitly determoveinfoned by the model's
         * height.

         * Set "sounds" to one of the following: 1) base fast 2) chain slow
         */
        fun SP_func_plat(ent: edict_t) {
            Math3D.VectorClear(ent.s.angles)
            ent.solid = Defines.SOLID_BSP
            ent.movetype = Defines.MOVETYPE_PUSH

            GameBase.gi.setmodel(ent, ent.model)

            ent.blocked = plat_blocked

            if (0 == ent.speed)
                ent.speed = 20
            else
                ent.speed *= 0.1

            if (ent.accel == 0)
                ent.accel = 5
            else
                ent.accel *= 0.1

            if (ent.decel == 0)
                ent.decel = 5
            else
                ent.decel *= 0.1

            if (ent.dmg == 0)
                ent.dmg = 2

            if (GameBase.st.lip == 0)
                GameBase.st.lip = 8

            // pos1 is the top position, pos2 is the bottom
            Math3D.VectorCopy(ent.s.origin, ent.pos1)
            Math3D.VectorCopy(ent.s.origin, ent.pos2)
            if (GameBase.st.height != 0)
                ent.pos2[2] -= GameBase.st.height
            else
                ent.pos2[2] -= (ent.maxs[2] - ent.mins[2]) - GameBase.st.lip

            ent.use = Use_Plat

            plat_spawn_inside_trigger(ent) // the "start moving" trigger

            if (ent.targetname != null) {
                ent.moveinfo.state = STATE_UP
            } else {
                Math3D.VectorCopy(ent.pos2, ent.s.origin)
                GameBase.gi.linkentity(ent)
                ent.moveinfo.state = STATE_BOTTOM
            }

            ent.moveinfo.speed = ent.speed
            ent.moveinfo.accel = ent.accel
            ent.moveinfo.decel = ent.decel
            ent.moveinfo.wait = ent.wait
            Math3D.VectorCopy(ent.pos1, ent.moveinfo.start_origin)
            Math3D.VectorCopy(ent.s.angles, ent.moveinfo.start_angles)
            Math3D.VectorCopy(ent.pos2, ent.moveinfo.end_origin)
            Math3D.VectorCopy(ent.s.angles, ent.moveinfo.end_angles)

            ent.moveinfo.sound_start = GameBase.gi.soundindex("plats/pt1_strt.wav")
            ent.moveinfo.sound_middle = GameBase.gi.soundindex("plats/pt1_mid.wav")
            ent.moveinfo.sound_end = GameBase.gi.soundindex("plats/pt1_end.wav")
        }

        /**
         * DOORS

         * spawn a trigger surrounding the entire team unless it is already targeted
         * by another.

         */

        /**
         * QUAKED func_door (0 .5 .8) ? START_OPEN x CRUSHER NOMONSTER ANIMATED
         * TOGGLE ANIMATED_FAST TOGGLE wait in both the start and end states for a
         * trigger event. START_OPEN the door to moves to its destination when
         * spawned, and operate in reverse. It is used to temporarily or permanently
         * close off an area when triggered (not useful for touch or takedamage
         * doors). NOMONSTER monsters will not trigger this door

         * "message" is printed when the door is touched if it is a trigger door and
         * it hasn't been fired yet "angle" determines the opening direction
         * "targetname" if set, no touch field will be spawned and a remote button
         * or trigger field activates the door. "health" if set, door must be shot
         * open "speed" movement speed (100 default) "wait" wait before returning (3
         * default, -1 = never return) "lip" lip remaining at end of move (8
         * default) "dmg" damage to inflict when blocked (2 default) "sounds" 1)
         * silent 2) light 3) medium 4) heavy
         */

        fun door_use_areaportals(self: edict_t, open: Boolean) {
            var t: edict_t? = null

            if (self.target == null)
                return

            var edit: EdictIterator? = null

            while ((edit = GameBase.G_Find(edit, GameBase.findByTarget, self.target)) != null) {
                t = edit!!.o
                if (Lib.Q_stricmp(t!!.classname, "func_areaportal") == 0) {
                    GameBase.gi.SetAreaPortalState(t!!.style, open)
                }
            }
        }

        fun door_go_up(self: edict_t, activator: edict_t) {
            if (self.moveinfo.state == STATE_UP)
                return  // already going up

            if (self.moveinfo.state == STATE_TOP) {
                // reset top wait time
                if (self.moveinfo.wait >= 0)
                    self.nextthink = GameBase.level.time + self.moveinfo.wait
                return
            }

            if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                if (self.moveinfo.sound_start != 0)
                    GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                self.s.sound = self.moveinfo.sound_middle
            }
            self.moveinfo.state = STATE_UP
            if (Lib.strcmp(self.classname, "func_door") == 0)
                Move_Calc(self, self.moveinfo.end_origin, door_hit_top)
            else if (Lib.strcmp(self.classname, "func_door_rotating") == 0)
                AngleMove_Calc(self, door_hit_top)

            GameUtil.G_UseTargets(self, activator)
            door_use_areaportals(self, true)
        }

        /**
         * QUAKED func_water (0 .5 .8) ? START_OPEN func_water is a moveable water
         * brush. It must be targeted to operate. Use a non-water texture at your
         * own risk.

         * START_OPEN causes the water to move to its destination when spawned and
         * operate in reverse.

         * "angle" determines the opening direction (up or down only) "speed"
         * movement speed (25 default) "wait" wait before returning (-1 default, -1 =
         * TOGGLE) "lip" lip remaining at end of move (0 default) "sounds" (yes,
         * these need to be changed) 0) no sound 1) water 2) lava
         */

        fun SP_func_water(self: edict_t) {
            val abs_movedir = floatArray(0.0, 0.0, 0.0)

            GameBase.G_SetMovedir(self.s.angles, self.movedir)
            self.movetype = Defines.MOVETYPE_PUSH
            self.solid = Defines.SOLID_BSP
            GameBase.gi.setmodel(self, self.model)

            when (self.sounds) {
                else -> {
                }

                1 // water
                -> {
                    self.moveinfo.sound_start = GameBase.gi.soundindex("world/mov_watr.wav")
                    self.moveinfo.sound_end = GameBase.gi.soundindex("world/stp_watr.wav")
                }

                2 // lava
                -> {
                    self.moveinfo.sound_start = GameBase.gi.soundindex("world/mov_watr.wav")
                    self.moveinfo.sound_end = GameBase.gi.soundindex("world/stp_watr.wav")
                }
            }

            // calculate second position
            Math3D.VectorCopy(self.s.origin, self.pos1)
            abs_movedir[0] = Math.abs(self.movedir[0])
            abs_movedir[1] = Math.abs(self.movedir[1])
            abs_movedir[2] = Math.abs(self.movedir[2])
            self.moveinfo.distance = abs_movedir[0] * self.size[0] + abs_movedir[1] * self.size[1] + abs_movedir[2] * self.size[2] - GameBase.st.lip
            Math3D.VectorMA(self.pos1, self.moveinfo.distance, self.movedir, self.pos2)

            // if it starts open, switch the positions
            if ((self.spawnflags and DOOR_START_OPEN) != 0) {
                Math3D.VectorCopy(self.pos2, self.s.origin)
                Math3D.VectorCopy(self.pos1, self.pos2)
                Math3D.VectorCopy(self.s.origin, self.pos1)
            }

            Math3D.VectorCopy(self.pos1, self.moveinfo.start_origin)
            Math3D.VectorCopy(self.s.angles, self.moveinfo.start_angles)
            Math3D.VectorCopy(self.pos2, self.moveinfo.end_origin)
            Math3D.VectorCopy(self.s.angles, self.moveinfo.end_angles)

            self.moveinfo.state = STATE_BOTTOM

            if (0 == self.speed)
                self.speed = 25
            self.moveinfo.accel = self.moveinfo.decel = self.moveinfo.speed = self.speed

            if (0 == self.wait)
                self.wait = -1
            self.moveinfo.wait = self.wait

            self.use = door_use

            if (self.wait == -1)
                self.spawnflags = self.spawnflags or DOOR_TOGGLE

            self.classname = "func_door"

            GameBase.gi.linkentity(self)
        }

        fun train_resume(self: edict_t) {
            val ent: edict_t
            val dest = floatArray(0.0, 0.0, 0.0)

            ent = self.target_ent

            Math3D.VectorSubtract(ent.s.origin, self.mins, dest)
            self.moveinfo.state = STATE_TOP
            Math3D.VectorCopy(self.s.origin, self.moveinfo.start_origin)
            Math3D.VectorCopy(dest, self.moveinfo.end_origin)
            Move_Calc(self, dest, train_wait)
            self.spawnflags = self.spawnflags or TRAIN_START_ON

        }

        fun SP_func_train(self: edict_t) {
            self.movetype = Defines.MOVETYPE_PUSH

            Math3D.VectorClear(self.s.angles)
            self.blocked = train_blocked
            if ((self.spawnflags and TRAIN_BLOCK_STOPS) != 0)
                self.dmg = 0
            else {
                if (0 == self.dmg)
                    self.dmg = 100
            }
            self.solid = Defines.SOLID_BSP
            GameBase.gi.setmodel(self, self.model)

            if (GameBase.st.noise != null)
                self.moveinfo.sound_middle = GameBase.gi.soundindex(GameBase.st.noise)

            if (0 == self.speed)
                self.speed = 100

            self.moveinfo.speed = self.speed
            self.moveinfo.accel = self.moveinfo.decel = self.moveinfo.speed

            self.use = train_use

            GameBase.gi.linkentity(self)

            if (self.target != null) {
                // start trains on the second frame, to make sure their targets have
                // had
                // a chance to spawn
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
                self.think = func_train_find
            } else {
                GameBase.gi.dprintf("func_train without a target at " + Lib.vtos(self.absmin) + "\n")
            }
        }

        fun SP_func_timer(self: edict_t) {
            if (0 == self.wait)
                self.wait = 1.0.toFloat()

            self.use = func_timer_use
            self.think = func_timer_think

            if (self.random >= self.wait) {
                self.random = self.wait - Defines.FRAMETIME
                GameBase.gi.dprintf("func_timer at " + Lib.vtos(self.s.origin) + " has random >= wait\n")
            }

            if ((self.spawnflags and 1) != 0) {
                self.nextthink = GameBase.level.time + 1.0.toFloat() + GameBase.st.pausetime + self.delay + self.wait + Lib.crandom() * self.random
                self.activator = self
            }

            self.svflags = Defines.SVF_NOCLIENT
        }

        /**
         * PLATS

         * movement options:

         * linear smooth start, hard stop smooth start, smooth stop

         * start end acceleration speed deceleration begin sound end sound target
         * fired when reaching end wait at end

         * object characteristics that use move segments
         * --------------------------------------------- movetype_push, or
         * movetype_stop action when touched action when blocked action when used
         * disabled? auto trigger spawning

         */

        public val PLAT_LOW_TRIGGER: Int = 1

        public val STATE_TOP: Int = 0

        public val STATE_BOTTOM: Int = 1

        public val STATE_UP: Int = 2

        public val STATE_DOWN: Int = 3

        public val DOOR_START_OPEN: Int = 1

        public val DOOR_REVERSE: Int = 2

        public val DOOR_CRUSHER: Int = 4

        public val DOOR_NOMONSTER: Int = 8

        public val DOOR_TOGGLE: Int = 32

        public val DOOR_X_AXIS: Int = 64

        public val DOOR_Y_AXIS: Int = 128

        //
        //	   Support routines for movement (changes in origin using velocity)
        //

        var Move_Done: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "move_done"
            }

            public fun think(ent: edict_t): Boolean {
                Math3D.VectorClear(ent.velocity)
                ent.moveinfo.endfunc.think(ent)
                return true
            }
        }

        var Move_Final: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "move_final"
            }

            public fun think(ent: edict_t): Boolean {

                if (ent.moveinfo.remaining_distance == 0) {
                    Move_Done.think(ent)
                    return true
                }

                Math3D.VectorScale(ent.moveinfo.dir, ent.moveinfo.remaining_distance / Defines.FRAMETIME, ent.velocity)

                ent.think = Move_Done
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        var Move_Begin: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "move_begin"
            }

            public fun think(ent: edict_t): Boolean {

                val frames: Float

                if ((ent.moveinfo.speed * Defines.FRAMETIME) >= ent.moveinfo.remaining_distance) {
                    Move_Final.think(ent)
                    return true
                }
                Math3D.VectorScale(ent.moveinfo.dir, ent.moveinfo.speed, ent.velocity)
                frames = Math.floor((ent.moveinfo.remaining_distance / ent.moveinfo.speed) / Defines.FRAMETIME) as Float
                ent.moveinfo.remaining_distance -= frames * ent.moveinfo.speed * Defines.FRAMETIME
                ent.nextthink = GameBase.level.time + (frames * Defines.FRAMETIME)
                ent.think = Move_Final
                return true
            }
        }

        //
        //	   Support routines for angular movement (changes in angle using avelocity)
        //

        var AngleMove_Done: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "agnle_move_done"
            }

            public fun think(ent: edict_t): Boolean {
                Math3D.VectorClear(ent.avelocity)
                ent.moveinfo.endfunc.think(ent)
                return true
            }
        }

        var AngleMove_Final: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "angle_move_final"
            }

            public fun think(ent: edict_t): Boolean {
                val move = floatArray(0.0, 0.0, 0.0)

                if (ent.moveinfo.state == STATE_UP)
                    Math3D.VectorSubtract(ent.moveinfo.end_angles, ent.s.angles, move)
                else
                    Math3D.VectorSubtract(ent.moveinfo.start_angles, ent.s.angles, move)

                if (Math3D.VectorEquals(move, Globals.vec3_origin)) {
                    AngleMove_Done.think(ent)
                    return true
                }

                Math3D.VectorScale(move, 1.0.toFloat() / Defines.FRAMETIME, ent.avelocity)

                ent.think = AngleMove_Done
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        var AngleMove_Begin: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "angle_move_begin"
            }

            public fun think(ent: edict_t): Boolean {
                val destdelta = floatArray(0.0, 0.0, 0.0)
                val len: Float
                val traveltime: Float
                val frames: Float

                // set destdelta to the vector needed to move
                if (ent.moveinfo.state == STATE_UP)
                    Math3D.VectorSubtract(ent.moveinfo.end_angles, ent.s.angles, destdelta)
                else
                    Math3D.VectorSubtract(ent.moveinfo.start_angles, ent.s.angles, destdelta)

                // calculate length of vector
                len = Math3D.VectorLength(destdelta)

                // divide by speed to get time to reach dest
                traveltime = len / ent.moveinfo.speed

                if (traveltime < Defines.FRAMETIME) {
                    AngleMove_Final.think(ent)
                    return true
                }

                frames = (Math.floor(traveltime / Defines.FRAMETIME)) as Float

                // scale the destdelta vector by the time spent traveling to get
                // velocity
                Math3D.VectorScale(destdelta, 1.0.toFloat() / traveltime, ent.avelocity)

                // set nextthink to trigger a think when dest is reached
                ent.nextthink = GameBase.level.time + frames * Defines.FRAMETIME
                ent.think = AngleMove_Final
                return true
            }
        }

        var Think_AccelMove: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "thinc_accelmove"
            }

            public fun think(ent: edict_t): Boolean {
                ent.moveinfo.remaining_distance -= ent.moveinfo.current_speed

                if (ent.moveinfo.current_speed == 0)
                // starting or blocked
                    plat_CalcAcceleratedMove(ent.moveinfo)

                plat_Accelerate(ent.moveinfo)

                // will the entire move complete on next frame?
                if (ent.moveinfo.remaining_distance <= ent.moveinfo.current_speed) {
                    Move_Final.think(ent)
                    return true
                }

                Math3D.VectorScale(ent.moveinfo.dir, ent.moveinfo.current_speed * 10, ent.velocity)
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                ent.think = Think_AccelMove
                return true
            }
        }

        var plat_hit_top: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "plat_hit_top"
            }

            public fun think(ent: edict_t): Boolean {
                if (0 == (ent.flags and Defines.FL_TEAMSLAVE)) {
                    if (ent.moveinfo.sound_end != 0)
                        GameBase.gi.sound(ent, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, ent.moveinfo.sound_end, 1, Defines.ATTN_STATIC, 0)
                    ent.s.sound = 0
                }
                ent.moveinfo.state = STATE_TOP

                ent.think = plat_go_down
                ent.nextthink = GameBase.level.time + 3
                return true
            }
        }

        var plat_hit_bottom: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "plat_hit_bottom"
            }

            public fun think(ent: edict_t): Boolean {

                if (0 == (ent.flags and Defines.FL_TEAMSLAVE)) {
                    if (ent.moveinfo.sound_end != 0)
                        GameBase.gi.sound(ent, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, ent.moveinfo.sound_end, 1, Defines.ATTN_STATIC, 0)
                    ent.s.sound = 0
                }
                ent.moveinfo.state = STATE_BOTTOM
                return true
            }
        }

        var plat_go_down: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "plat_go_down"
            }

            public fun think(ent: edict_t): Boolean {
                if (0 == (ent.flags and Defines.FL_TEAMSLAVE)) {
                    if (ent.moveinfo.sound_start != 0)
                        GameBase.gi.sound(ent, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, ent.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                    ent.s.sound = ent.moveinfo.sound_middle
                }
                ent.moveinfo.state = STATE_DOWN
                Move_Calc(ent, ent.moveinfo.end_origin, plat_hit_bottom)
                return true
            }
        }

        var plat_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "plat_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t?) {
                if (0 == (other!!.svflags and Defines.SVF_MONSTER) && (null == other!!.client)) {
                    // give it a chance to go away on it's own terms (like gibs)
                    GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, 100000, 1, 0, Defines.MOD_CRUSH)
                    // if it's still there, nuke it
                    if (other != null)
                        GameMisc.BecomeExplosion1(other)
                    return
                }

                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)

                if (self.moveinfo.state == STATE_UP)
                    plat_go_down.think(self)
                else if (self.moveinfo.state == STATE_DOWN)
                    plat_go_up(self)

            }
        }

        var Use_Plat: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "use_plat"
            }

            public fun use(ent: edict_t, other: edict_t, activator: edict_t) {
                if (ent.think != null)
                    return  // already down
                plat_go_down.think(ent)
            }
        }

        var Touch_Plat_Center: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "touch_plat_center"
            }

            public fun touch(ent: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                var ent = ent
                if (other.client == null)
                    return

                if (other.health <= 0)
                    return

                ent = ent.enemy // now point at the plat, not the trigger
                if (ent.moveinfo.state == STATE_BOTTOM)
                    plat_go_up(ent)
                else if (ent.moveinfo.state == STATE_TOP) {
                    ent.nextthink = GameBase.level.time + 1 // the player is still
                    // on the plat, so
                    // delay going down
                }
            }
        }

        /**
         * QUAKED func_rotating (0 .5 .8) ? START_ON REVERSE X_AXIS Y_AXIS
         * TOUCH_PAIN STOP ANIMATED ANIMATED_FAST You need to have an origin brush
         * as part of this entity. The center of that brush will be the point around
         * which it is rotated. It will rotate around the Z axis by default. You can
         * check either the X_AXIS or Y_AXIS box to change that.

         * "speed" determines how fast it moves; default value is 100. "dmg" damage
         * to inflict when blocked (2 default)

         * REVERSE will cause the it to rotate in the opposite direction. STOP mean
         * it will stop moving instead of pushing entities
         */

        var rotating_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "rotating_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t) {
                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)
            }
        }

        var rotating_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "rotating_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (self.avelocity[0] != 0 || self.avelocity[1] != 0 || self.avelocity[2] != 0)
                    GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)
            }
        }

        var rotating_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "rotating_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if (!Math3D.VectorEquals(self.avelocity, Globals.vec3_origin)) {
                    self.s.sound = 0
                    Math3D.VectorClear(self.avelocity)
                    self.touch = null
                } else {
                    self.s.sound = self.moveinfo.sound_middle
                    Math3D.VectorScale(self.movedir, self.speed, self.avelocity)
                    if ((self.spawnflags and 16) != 0)
                        self.touch = rotating_touch
                }
            }
        }

        var SP_func_rotating: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_rotating"
            }

            public fun think(ent: edict_t): Boolean {
                ent.solid = Defines.SOLID_BSP
                if ((ent.spawnflags and 32) != 0)
                    ent.movetype = Defines.MOVETYPE_STOP
                else
                    ent.movetype = Defines.MOVETYPE_PUSH

                // set the axis of rotation
                Math3D.VectorClear(ent.movedir)
                if ((ent.spawnflags and 4) != 0)
                    ent.movedir[2] = 1.0.toFloat()
                else if ((ent.spawnflags and 8) != 0)
                    ent.movedir[0] = 1.0.toFloat()
                else
                // Z_AXIS
                    ent.movedir[1] = 1.0.toFloat()

                // check for reverse rotation
                if ((ent.spawnflags and 2) != 0)
                    Math3D.VectorNegate(ent.movedir, ent.movedir)

                if (0 == ent.speed)
                    ent.speed = 100
                if (0 == ent.dmg)
                    ent.dmg = 2

                //		ent.moveinfo.sound_middle = "doors/hydro1.wav";

                ent.use = rotating_use
                if (ent.dmg != 0)
                    ent.blocked = rotating_blocked

                if ((ent.spawnflags and 1) != 0)
                    ent.use.use(ent, null, null)

                if ((ent.spawnflags and 64) != 0)
                    ent.s.effects = ent.s.effects or Defines.EF_ANIM_ALL
                if ((ent.spawnflags and 128) != 0)
                    ent.s.effects = ent.s.effects or Defines.EF_ANIM_ALLFAST

                GameBase.gi.setmodel(ent, ent.model)
                GameBase.gi.linkentity(ent)
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * BUTTONS
     * 
     * ======================================================================
     */

        /*
     * QUAKED func_button (0 .5 .8) ? When a button is touched, it moves some
     * distance in the direction of it's angle, triggers all of it's targets,
     * waits some time, then returns to it's original position where it can be
     * triggered again.
     * 
     * "angle" determines the opening direction "target" all entities with a
     * matching targetname will be used "speed" override the default 40 speed
     * "wait" override the default 1 second wait (-1 = never return) "lip"
     * override the default 4 pixel lip remaining at end of move "health" if
     * set, the button must be killed instead of touched "sounds" 1) silent 2)
     * steam metal 3) wooden clunk 4) metallic click 5) in-out
     */

        var button_done: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "button_done"
            }

            public fun think(self: edict_t): Boolean {

                self.moveinfo.state = STATE_BOTTOM
                self.s.effects = self.s.effects and Defines.EF_ANIM23.inv()
                self.s.effects = self.s.effects or Defines.EF_ANIM01
                return true
            }
        }

        var button_return: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "button_return"
            }

            public fun think(self: edict_t): Boolean {
                self.moveinfo.state = STATE_DOWN

                Move_Calc(self, self.moveinfo.start_origin, button_done)

                self.s.frame = 0

                if (self.health != 0)
                    self.takedamage = Defines.DAMAGE_YES
                return true
            }
        }

        var button_wait: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "button_wait"
            }

            public fun think(self: edict_t): Boolean {
                self.moveinfo.state = STATE_TOP
                self.s.effects = self.s.effects and Defines.EF_ANIM01.inv()
                self.s.effects = self.s.effects or Defines.EF_ANIM23

                GameUtil.G_UseTargets(self, self.activator)
                self.s.frame = 1
                if (self.moveinfo.wait >= 0) {
                    self.nextthink = GameBase.level.time + self.moveinfo.wait
                    self.think = button_return
                }
                return true
            }
        }

        var button_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "button_fire"
            }

            public fun think(self: edict_t): Boolean {
                if (self.moveinfo.state == STATE_UP || self.moveinfo.state == STATE_TOP)
                    return true

                self.moveinfo.state = STATE_UP
                if (self.moveinfo.sound_start != 0 && 0 == (self.flags and Defines.FL_TEAMSLAVE))
                    GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                Move_Calc(self, self.moveinfo.end_origin, button_wait)
                return true
            }
        }

        var button_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "button_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.activator = activator
                button_fire.think(self)
                return
            }
        }

        var button_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "button_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (null == other.client)
                    return

                if (other.health <= 0)
                    return

                self.activator = other
                button_fire.think(self)

            }
        }

        var button_killed: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "button_killed"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                self.activator = attacker
                self.health = self.max_health
                self.takedamage = Defines.DAMAGE_NO
                button_fire.think(self)

            }
        }

        var SP_func_button: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_button"
            }

            public fun think(ent: edict_t): Boolean {
                val abs_movedir = floatArray(0.0, 0.0, 0.0)
                val dist: Float

                GameBase.G_SetMovedir(ent.s.angles, ent.movedir)
                ent.movetype = Defines.MOVETYPE_STOP
                ent.solid = Defines.SOLID_BSP
                GameBase.gi.setmodel(ent, ent.model)

                if (ent.sounds != 1)
                    ent.moveinfo.sound_start = GameBase.gi.soundindex("switches/butn2.wav")

                if (0 == ent.speed)
                    ent.speed = 40
                if (0 == ent.accel)
                    ent.accel = ent.speed
                if (0 == ent.decel)
                    ent.decel = ent.speed

                if (0 == ent.wait)
                    ent.wait = 3
                if (0 == GameBase.st.lip)
                    GameBase.st.lip = 4

                Math3D.VectorCopy(ent.s.origin, ent.pos1)
                abs_movedir[0] = Math.abs(ent.movedir[0]) as Float
                abs_movedir[1] = Math.abs(ent.movedir[1]) as Float
                abs_movedir[2] = Math.abs(ent.movedir[2]) as Float
                dist = abs_movedir[0] * ent.size[0] + abs_movedir[1] * ent.size[1] + abs_movedir[2] * ent.size[2] - GameBase.st.lip
                Math3D.VectorMA(ent.pos1, dist, ent.movedir, ent.pos2)

                ent.use = button_use
                ent.s.effects = ent.s.effects or Defines.EF_ANIM01

                if (ent.health != 0) {
                    ent.max_health = ent.health
                    ent.die = button_killed
                    ent.takedamage = Defines.DAMAGE_YES
                } else if (null == ent.targetname)
                    ent.touch = button_touch

                ent.moveinfo.state = STATE_BOTTOM

                ent.moveinfo.speed = ent.speed
                ent.moveinfo.accel = ent.accel
                ent.moveinfo.decel = ent.decel
                ent.moveinfo.wait = ent.wait
                Math3D.VectorCopy(ent.pos1, ent.moveinfo.start_origin)
                Math3D.VectorCopy(ent.s.angles, ent.moveinfo.start_angles)
                Math3D.VectorCopy(ent.pos2, ent.moveinfo.end_origin)
                Math3D.VectorCopy(ent.s.angles, ent.moveinfo.end_angles)

                GameBase.gi.linkentity(ent)
                return true
            }
        }

        var door_hit_top: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_hit_top"
            }

            public fun think(self: edict_t): Boolean {
                if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                    if (self.moveinfo.sound_end != 0)
                        GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_end, 1, Defines.ATTN_STATIC, 0)
                    self.s.sound = 0
                }
                self.moveinfo.state = STATE_TOP
                if ((self.spawnflags and DOOR_TOGGLE) != 0)
                    return true
                if (self.moveinfo.wait >= 0) {
                    self.think = door_go_down
                    self.nextthink = GameBase.level.time + self.moveinfo.wait
                }
                return true
            }
        }

        var door_hit_bottom: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_hit_bottom"
            }

            public fun think(self: edict_t): Boolean {
                if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                    if (self.moveinfo.sound_end != 0)
                        GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_end, 1, Defines.ATTN_STATIC, 0)
                    self.s.sound = 0
                }
                self.moveinfo.state = STATE_BOTTOM
                door_use_areaportals(self, false)
                return true
            }
        }

        var door_go_down: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_go_down"
            }

            public fun think(self: edict_t): Boolean {
                if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                    if (self.moveinfo.sound_start != 0)
                        GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                    self.s.sound = self.moveinfo.sound_middle
                }
                if (self.max_health != 0) {
                    self.takedamage = Defines.DAMAGE_YES
                    self.health = self.max_health
                }

                self.moveinfo.state = STATE_DOWN
                if (Lib.strcmp(self.classname, "func_door") == 0)
                    Move_Calc(self, self.moveinfo.start_origin, door_hit_bottom)
                else if (Lib.strcmp(self.classname, "func_door_rotating") == 0)
                    AngleMove_Calc(self, door_hit_bottom)
                return true
            }
        }

        var door_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "door_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                var ent: edict_t?

                if ((self.flags and Defines.FL_TEAMSLAVE) != 0)
                    return

                if ((self.spawnflags and DOOR_TOGGLE) != 0) {
                    if (self.moveinfo.state == STATE_UP || self.moveinfo.state == STATE_TOP) {
                        // trigger all paired doors
                        run {
                            ent = self
                            while (ent != null) {
                                ent!!.message = null
                                ent!!.touch = null
                                door_go_down.think(ent)
                                ent = ent!!.teamchain
                            }
                        }
                        return
                    }
                }

                // trigger all paired doors
                run {
                    ent = self
                    while (ent != null) {
                        ent!!.message = null
                        ent!!.touch = null
                        door_go_up(ent, activator)
                        ent = ent!!.teamchain
                    }
                }
            }
        }

        var Touch_DoorTrigger: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "touch_door_trigger"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (other.health <= 0)
                    return

                if (0 == (other.svflags and Defines.SVF_MONSTER) && (null == other.client))
                    return

                if (0 != (self.owner.spawnflags and DOOR_NOMONSTER) && 0 != (other.svflags and Defines.SVF_MONSTER))
                    return

                if (GameBase.level.time < self.touch_debounce_time)
                    return
                self.touch_debounce_time = GameBase.level.time + 1.0.toFloat()

                door_use.use(self.owner, other, other)
            }
        }

        var Think_CalcMoveSpeed: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "think_calc_movespeed"
            }

            public fun think(self: edict_t): Boolean {
                var ent: edict_t?
                var min: Float
                val time: Float
                var newspeed: Float
                var ratio: Float
                var dist: Float

                if ((self.flags and Defines.FL_TEAMSLAVE) != 0)
                    return true // only the team master does this

                // find the smallest distance any member of the team will be moving
                min = Math.abs(self.moveinfo.distance)
                run {
                    ent = self.teamchain
                    while (ent != null) {
                        dist = Math.abs(ent!!.moveinfo.distance)
                        if (dist < min)
                            min = dist
                        ent = ent!!.teamchain
                    }
                }

                time = min / self.moveinfo.speed

                // adjust speeds so they will all complete at the same time
                run {
                    ent = self
                    while (ent != null) {
                        newspeed = Math.abs(ent!!.moveinfo.distance) / time
                        ratio = newspeed / ent!!.moveinfo.speed
                        if (ent!!.moveinfo.accel == ent!!.moveinfo.speed)
                            ent!!.moveinfo.accel = newspeed
                        else
                            ent!!.moveinfo.accel *= ratio
                        if (ent!!.moveinfo.decel == ent!!.moveinfo.speed)
                            ent!!.moveinfo.decel = newspeed
                        else
                            ent!!.moveinfo.decel *= ratio
                        ent!!.moveinfo.speed = newspeed
                        ent = ent!!.teamchain
                    }
                }
                return true
            }
        }

        var Think_SpawnDoorTrigger: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "think_spawn_door_trigger"
            }

            public fun think(ent: edict_t): Boolean {
                var other: edict_t?
                val mins = floatArray(0.0, 0.0, 0.0)
                val maxs = floatArray(0.0, 0.0, 0.0)

                if ((ent.flags and Defines.FL_TEAMSLAVE) != 0)
                    return true // only the team leader spawns a trigger

                Math3D.VectorCopy(ent.absmin, mins)
                Math3D.VectorCopy(ent.absmax, maxs)

                run {
                    other = ent.teamchain
                    while (other != null) {
                        GameBase.AddPointToBounds(other!!.absmin, mins, maxs)
                        GameBase.AddPointToBounds(other!!.absmax, mins, maxs)
                        other = other!!.teamchain
                    }
                }

                // expand
                mins[0] -= 60
                mins[1] -= 60
                maxs[0] += 60
                maxs[1] += 60

                other = GameUtil.G_Spawn()
                Math3D.VectorCopy(mins, other!!.mins)
                Math3D.VectorCopy(maxs, other!!.maxs)
                other!!.owner = ent
                other!!.solid = Defines.SOLID_TRIGGER
                other!!.movetype = Defines.MOVETYPE_NONE
                other!!.touch = Touch_DoorTrigger
                GameBase.gi.linkentity(other)

                if ((ent.spawnflags and DOOR_START_OPEN) != 0)
                    door_use_areaportals(ent, true)

                Think_CalcMoveSpeed.think(ent)
                return true
            }
        }

        var door_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "door_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t?) {
                var ent: edict_t?

                if (0 == (other!!.svflags and Defines.SVF_MONSTER) && (null == other!!.client)) {
                    // give it a chance to go away on it's own terms (like gibs)
                    GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, 100000, 1, 0, Defines.MOD_CRUSH)
                    // if it's still there, nuke it
                    if (other != null)
                        GameMisc.BecomeExplosion1(other)
                    return
                }

                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)

                if ((self.spawnflags and DOOR_CRUSHER) != 0)
                    return

                // if a door has a negative wait, it would never come back if
                // blocked,
                // so let it just squash the object to death real fast
                if (self.moveinfo.wait >= 0) {
                    if (self.moveinfo.state == STATE_DOWN) {
                        run {
                            ent = self.teammaster
                            while (ent != null) {
                                door_go_up(ent, ent!!.activator)
                                ent = ent!!.teamchain
                            }
                        }
                    } else {
                        run {
                            ent = self.teammaster
                            while (ent != null) {
                                door_go_down.think(ent)
                                ent = ent!!.teamchain
                            }
                        }
                    }
                }
            }
        }

        var door_killed: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "door_killed"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var ent: edict_t?

                run {
                    ent = self.teammaster
                    while (ent != null) {
                        ent!!.health = ent!!.max_health
                        ent!!.takedamage = Defines.DAMAGE_NO
                        ent = ent!!.teamchain
                    }
                }
                door_use.use(self.teammaster, attacker, attacker)
            }
        }

        var door_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "door_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (null == other.client)
                    return

                if (GameBase.level.time < self.touch_debounce_time)
                    return
                self.touch_debounce_time = GameBase.level.time + 5.0.toFloat()

                GameBase.gi.centerprintf(other, self.message)
                GameBase.gi.sound(other, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/talk1.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }

        var SP_func_door: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_door"
            }

            public fun think(ent: edict_t): Boolean {
                val abs_movedir = floatArray(0.0, 0.0, 0.0)

                if (ent.sounds != 1) {
                    ent.moveinfo.sound_start = GameBase.gi.soundindex("doors/dr1_strt.wav")
                    ent.moveinfo.sound_middle = GameBase.gi.soundindex("doors/dr1_mid.wav")
                    ent.moveinfo.sound_end = GameBase.gi.soundindex("doors/dr1_end.wav")
                }

                GameBase.G_SetMovedir(ent.s.angles, ent.movedir)
                ent.movetype = Defines.MOVETYPE_PUSH
                ent.solid = Defines.SOLID_BSP
                GameBase.gi.setmodel(ent, ent.model)

                ent.blocked = door_blocked
                ent.use = door_use

                if (0 == ent.speed)
                    ent.speed = 100
                if (GameBase.deathmatch.value != 0)
                    ent.speed *= 2

                if (0 == ent.accel)
                    ent.accel = ent.speed
                if (0 == ent.decel)
                    ent.decel = ent.speed

                if (0 == ent.wait)
                    ent.wait = 3
                if (0 == GameBase.st.lip)
                    GameBase.st.lip = 8
                if (0 == ent.dmg)
                    ent.dmg = 2

                // calculate second position
                Math3D.VectorCopy(ent.s.origin, ent.pos1)
                abs_movedir[0] = Math.abs(ent.movedir[0])
                abs_movedir[1] = Math.abs(ent.movedir[1])
                abs_movedir[2] = Math.abs(ent.movedir[2])
                ent.moveinfo.distance = abs_movedir[0] * ent.size[0] + abs_movedir[1] * ent.size[1] + abs_movedir[2] * ent.size[2] - GameBase.st.lip

                Math3D.VectorMA(ent.pos1, ent.moveinfo.distance, ent.movedir, ent.pos2)

                // if it starts open, switch the positions
                if ((ent.spawnflags and DOOR_START_OPEN) != 0) {
                    Math3D.VectorCopy(ent.pos2, ent.s.origin)
                    Math3D.VectorCopy(ent.pos1, ent.pos2)
                    Math3D.VectorCopy(ent.s.origin, ent.pos1)
                }

                ent.moveinfo.state = STATE_BOTTOM

                if (ent.health != 0) {
                    ent.takedamage = Defines.DAMAGE_YES
                    ent.die = door_killed
                    ent.max_health = ent.health
                } else if (ent.targetname != null && ent.message != null) {
                    GameBase.gi.soundindex("misc/talk.wav")
                    ent.touch = door_touch
                }

                ent.moveinfo.speed = ent.speed
                ent.moveinfo.accel = ent.accel
                ent.moveinfo.decel = ent.decel
                ent.moveinfo.wait = ent.wait
                Math3D.VectorCopy(ent.pos1, ent.moveinfo.start_origin)
                Math3D.VectorCopy(ent.s.angles, ent.moveinfo.start_angles)
                Math3D.VectorCopy(ent.pos2, ent.moveinfo.end_origin)
                Math3D.VectorCopy(ent.s.angles, ent.moveinfo.end_angles)

                if ((ent.spawnflags and 16) != 0)
                    ent.s.effects = ent.s.effects or Defines.EF_ANIM_ALL
                if ((ent.spawnflags and 64) != 0)
                    ent.s.effects = ent.s.effects or Defines.EF_ANIM_ALLFAST

                // to simplify logic elsewhere, make non-teamed doors into a team of
                // one
                if (null == ent.team)
                    ent.teammaster = ent

                GameBase.gi.linkentity(ent)

                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                if (ent.health != 0 || ent.targetname != null)
                    ent.think = Think_CalcMoveSpeed
                else
                    ent.think = Think_SpawnDoorTrigger
                return true
            }
        }

        /*
     * QUAKED func_door_rotating (0 .5 .8) ? START_OPEN REVERSE CRUSHER
     * NOMONSTER ANIMATED TOGGLE X_AXIS Y_AXIS TOGGLE causes the door to wait in
     * both the start and end states for a trigger event.
     * 
     * START_OPEN the door to moves to its destination when spawned, and operate
     * in reverse. It is used to temporarily or permanently close off an area
     * when triggered (not useful for touch or takedamage doors). NOMONSTER
     * monsters will not trigger this door
     * 
     * You need to have an origin brush as part of this entity. The center of
     * that brush will be the point around which it is rotated. It will rotate
     * around the Z axis by default. You can check either the X_AXIS or Y_AXIS
     * box to change that.
     * 
     * "distance" is how many degrees the door will be rotated. "speed"
     * determines how fast the door moves; default value is 100.
     * 
     * REVERSE will cause the door to rotate in the opposite direction.
     * 
     * "message" is printed when the door is touched if it is a trigger door and
     * it hasn't been fired yet "angle" determines the opening direction
     * "targetname" if set, no touch field will be spawned and a remote button
     * or trigger field activates the door. "health" if set, door must be shot
     * open "speed" movement speed (100 default) "wait" wait before returning (3
     * default, -1 = never return) "dmg" damage to inflict when blocked (2
     * default) "sounds" 1) silent 2) light 3) medium 4) heavy
     */

        var SP_func_door_rotating: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_door_rotating"
            }

            public fun think(ent: edict_t): Boolean {
                Math3D.VectorClear(ent.s.angles)

                // set the axis of rotation
                Math3D.VectorClear(ent.movedir)
                if ((ent.spawnflags and DOOR_X_AXIS) != 0)
                    ent.movedir[2] = 1.0.toFloat()
                else if ((ent.spawnflags and DOOR_Y_AXIS) != 0)
                    ent.movedir[0] = 1.0.toFloat()
                else
                // Z_AXIS
                    ent.movedir[1] = 1.0.toFloat()

                // check for reverse rotation
                if ((ent.spawnflags and DOOR_REVERSE) != 0)
                    Math3D.VectorNegate(ent.movedir, ent.movedir)

                if (0 == GameBase.st.distance) {
                    GameBase.gi.dprintf(ent.classname + " at " + Lib.vtos(ent.s.origin) + " with no distance set\n")
                    GameBase.st.distance = 90
                }

                Math3D.VectorCopy(ent.s.angles, ent.pos1)
                Math3D.VectorMA(ent.s.angles, GameBase.st.distance, ent.movedir, ent.pos2)
                ent.moveinfo.distance = GameBase.st.distance

                ent.movetype = Defines.MOVETYPE_PUSH
                ent.solid = Defines.SOLID_BSP
                GameBase.gi.setmodel(ent, ent.model)

                ent.blocked = door_blocked
                ent.use = door_use

                if (0 == ent.speed)
                    ent.speed = 100
                if (0 == ent.accel)
                    ent.accel = ent.speed
                if (0 == ent.decel)
                    ent.decel = ent.speed

                if (0 == ent.wait)
                    ent.wait = 3
                if (0 == ent.dmg)
                    ent.dmg = 2

                if (ent.sounds != 1) {
                    ent.moveinfo.sound_start = GameBase.gi.soundindex("doors/dr1_strt.wav")
                    ent.moveinfo.sound_middle = GameBase.gi.soundindex("doors/dr1_mid.wav")
                    ent.moveinfo.sound_end = GameBase.gi.soundindex("doors/dr1_end.wav")
                }

                // if it starts open, switch the positions
                if ((ent.spawnflags and DOOR_START_OPEN) != 0) {
                    Math3D.VectorCopy(ent.pos2, ent.s.angles)
                    Math3D.VectorCopy(ent.pos1, ent.pos2)
                    Math3D.VectorCopy(ent.s.angles, ent.pos1)
                    Math3D.VectorNegate(ent.movedir, ent.movedir)
                }

                if (ent.health != 0) {
                    ent.takedamage = Defines.DAMAGE_YES
                    ent.die = door_killed
                    ent.max_health = ent.health
                }

                if (ent.targetname != null && ent.message != null) {
                    GameBase.gi.soundindex("misc/talk.wav")
                    ent.touch = door_touch
                }

                ent.moveinfo.state = STATE_BOTTOM
                ent.moveinfo.speed = ent.speed
                ent.moveinfo.accel = ent.accel
                ent.moveinfo.decel = ent.decel
                ent.moveinfo.wait = ent.wait
                Math3D.VectorCopy(ent.s.origin, ent.moveinfo.start_origin)
                Math3D.VectorCopy(ent.pos1, ent.moveinfo.start_angles)
                Math3D.VectorCopy(ent.s.origin, ent.moveinfo.end_origin)
                Math3D.VectorCopy(ent.pos2, ent.moveinfo.end_angles)

                if ((ent.spawnflags and 16) != 0)
                    ent.s.effects = ent.s.effects or Defines.EF_ANIM_ALL

                // to simplify logic elsewhere, make non-teamed doors into a team of
                // one
                if (ent.team == null)
                    ent.teammaster = ent

                GameBase.gi.linkentity(ent)

                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                if (ent.health != 0 || ent.targetname != null)
                    ent.think = Think_CalcMoveSpeed
                else
                    ent.think = Think_SpawnDoorTrigger
                return true
            }
        }

        public val TRAIN_START_ON: Int = 1

        public val TRAIN_TOGGLE: Int = 2

        public val TRAIN_BLOCK_STOPS: Int = 4

        /*
     * QUAKED func_train (0 .5 .8) ? START_ON TOGGLE BLOCK_STOPS Trains are
     * moving platforms that players can ride. The targets origin specifies the
     * min point of the train at each corner. The train spawns at the first
     * target it is pointing at. If the train is the target of a button or
     * trigger, it will not begin moving until activated. speed default 100 dmg
     * default 2 noise looping sound to play when the train is in motion
     *  
     */

        var train_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "train_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t?) {
                if (0 == (other!!.svflags and Defines.SVF_MONSTER) && (null == other!!.client)) {
                    // give it a chance to go away on it's own terms (like gibs)
                    GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, 100000, 1, 0, Defines.MOD_CRUSH)
                    // if it's still there, nuke it
                    if (other != null)
                        GameMisc.BecomeExplosion1(other)
                    return
                }

                if (GameBase.level.time < self.touch_debounce_time)
                    return

                if (self.dmg == 0)
                    return
                self.touch_debounce_time = GameBase.level.time + 0.5.toFloat()
                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)
            }
        }

        var train_wait: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "train_wait"
            }

            public fun think(self: edict_t): Boolean {
                if (self.target_ent.pathtarget != null) {
                    val savetarget: String
                    val ent: edict_t

                    ent = self.target_ent
                    savetarget = ent.target
                    ent.target = ent.pathtarget
                    GameUtil.G_UseTargets(ent, self.activator)
                    ent.target = savetarget

                    // make sure we didn't get killed by a killtarget
                    if (!self.inuse)
                        return true
                }

                if (self.moveinfo.wait != 0) {
                    if (self.moveinfo.wait > 0) {
                        self.nextthink = GameBase.level.time + self.moveinfo.wait
                        self.think = train_next
                    } else if (0 != (self.spawnflags and TRAIN_TOGGLE))
                    // && wait < 0
                    {
                        train_next.think(self)
                        self.spawnflags = self.spawnflags and TRAIN_START_ON.inv()
                        Math3D.VectorClear(self.velocity)
                        self.nextthink = 0
                    }

                    if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                        if (self.moveinfo.sound_end != 0)
                            GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_end, 1, Defines.ATTN_STATIC, 0)
                        self.s.sound = 0
                    }
                } else {
                    train_next.think(self)
                }
                return true
            }
        }

        var train_next: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "train_next"
            }

            public fun think(self: edict_t): Boolean {
                var ent: edict_t? = null
                val dest = floatArray(0.0, 0.0, 0.0)
                var first: Boolean

                first = true

                var dogoto = true
                while (dogoto) {
                    if (null == self.target) {
                        //			gi.dprintf ("train_next: no next target\n");
                        return true
                    }

                    ent = GameBase.G_PickTarget(self.target)
                    if (null == ent) {
                        GameBase.gi.dprintf("train_next: bad target " + self.target + "\n")
                        return true
                    }

                    self.target = ent!!.target
                    dogoto = false
                    // check for a teleport path_corner
                    if ((ent!!.spawnflags and 1) != 0) {
                        if (!first) {
                            GameBase.gi.dprintf("connected teleport path_corners, see " + ent!!.classname + " at " + Lib.vtos(ent!!.s.origin) + "\n")
                            return true
                        }
                        first = false
                        Math3D.VectorSubtract(ent!!.s.origin, self.mins, self.s.origin)
                        Math3D.VectorCopy(self.s.origin, self.s.old_origin)
                        self.s.event = Defines.EV_OTHER_TELEPORT
                        GameBase.gi.linkentity(self)
                        dogoto = true
                    }
                }
                self.moveinfo.wait = ent!!.wait
                self.target_ent = ent

                if (0 == (self.flags and Defines.FL_TEAMSLAVE)) {
                    if (self.moveinfo.sound_start != 0)
                        GameBase.gi.sound(self, Defines.CHAN_NO_PHS_ADD + Defines.CHAN_VOICE, self.moveinfo.sound_start, 1, Defines.ATTN_STATIC, 0)
                    self.s.sound = self.moveinfo.sound_middle
                }

                Math3D.VectorSubtract(ent!!.s.origin, self.mins, dest)
                self.moveinfo.state = STATE_TOP
                Math3D.VectorCopy(self.s.origin, self.moveinfo.start_origin)
                Math3D.VectorCopy(dest, self.moveinfo.end_origin)
                Move_Calc(self, dest, train_wait)
                self.spawnflags = self.spawnflags or TRAIN_START_ON
                return true
            }
        }

        public var func_train_find: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "func_train_find"
            }

            public fun think(self: edict_t): Boolean {
                val ent: edict_t?

                if (null == self.target) {
                    GameBase.gi.dprintf("train_find: no target\n")
                    return true
                }
                ent = GameBase.G_PickTarget(self.target)
                if (null == ent) {
                    GameBase.gi.dprintf("train_find: target " + self.target + " not found\n")
                    return true
                }
                self.target = ent!!.target

                Math3D.VectorSubtract(ent!!.s.origin, self.mins, self.s.origin)
                GameBase.gi.linkentity(self)

                // if not triggered, start immediately
                if (null == self.targetname)
                    self.spawnflags = self.spawnflags or TRAIN_START_ON

                if ((self.spawnflags and TRAIN_START_ON) != 0) {
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                    self.think = train_next
                    self.activator = self
                }
                return true
            }
        }

        public var train_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "train_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.activator = activator

                if ((self.spawnflags and TRAIN_START_ON) != 0) {
                    if (0 == (self.spawnflags and TRAIN_TOGGLE))
                        return
                    self.spawnflags = self.spawnflags and TRAIN_START_ON.inv()
                    Math3D.VectorClear(self.velocity)
                    self.nextthink = 0
                } else {
                    if (self.target_ent != null)
                        train_resume(self)
                    else
                        train_next.think(self)
                }
            }
        }

        /*
     * QUAKED trigger_elevator (0.3 0.1 0.6) (-8 -8 -8) (8 8 8)
     */
        var trigger_elevator_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "trigger_elevator_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                val target: edict_t?

                if (0 != self.movetarget.nextthink) {
                    //			gi.dprintf("elevator busy\n");
                    return
                }

                if (null == other.pathtarget) {
                    GameBase.gi.dprintf("elevator used with no pathtarget\n")
                    return
                }

                target = GameBase.G_PickTarget(other.pathtarget)
                if (null == target) {
                    GameBase.gi.dprintf("elevator used with bad pathtarget: " + other.pathtarget + "\n")
                    return
                }

                self.movetarget.target_ent = target
                train_resume(self.movetarget)
            }
        }

        var trigger_elevator_init: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "trigger_elevator_init"
            }

            public fun think(self: edict_t): Boolean {
                if (null == self.target) {
                    GameBase.gi.dprintf("trigger_elevator has no target\n")
                    return true
                }
                self.movetarget = GameBase.G_PickTarget(self.target)
                if (null == self.movetarget) {
                    GameBase.gi.dprintf("trigger_elevator unable to find target " + self.target + "\n")
                    return true
                }
                if (Lib.strcmp(self.movetarget.classname, "func_train") != 0) {
                    GameBase.gi.dprintf("trigger_elevator target " + self.target + " is not a train\n")
                    return true
                }

                self.use = trigger_elevator_use
                self.svflags = Defines.SVF_NOCLIENT
                return true
            }
        }

        var SP_trigger_elevator: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_trigger_elevator"
            }

            public fun think(self: edict_t): Boolean {
                self.think = trigger_elevator_init
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        /*
     * QUAKED func_timer (0.3 0.1 0.6) (-8 -8 -8) (8 8 8) START_ON "wait" base
     * time between triggering all targets, default is 1 "random" wait variance,
     * default is 0
     * 
     * so, the basic time between firing is a random time between (wait -
     * random) and (wait + random)
     * 
     * "delay" delay before first firing when turned on, default is 0
     * 
     * "pausetime" additional delay used only the very first time and only if
     * spawned with START_ON
     * 
     * These can used but not touched.
     */

        var func_timer_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "func_timer_think"
            }

            public fun think(self: edict_t): Boolean {
                GameUtil.G_UseTargets(self, self.activator)
                self.nextthink = GameBase.level.time + self.wait + Lib.crandom() * self.random
                return true
            }
        }

        var func_timer_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_timer_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.activator = activator

                // if on, turn it off
                if (self.nextthink != 0) {
                    self.nextthink = 0
                    return
                }

                // turn it on
                if (self.delay != 0)
                    self.nextthink = GameBase.level.time + self.delay
                else
                    func_timer_think.think(self)
            }
        }

        /*
     * QUAKED func_conveyor (0 .5 .8) ? START_ON TOGGLE Conveyors are stationary
     * brushes that move what's on them. The brush should be have a surface with
     * at least one current content enabled. speed default 100
     */

        var func_conveyor_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_conveyor_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if ((self.spawnflags and 1) != 0) {
                    self.speed = 0
                    self.spawnflags = self.spawnflags and 1.inv()
                } else {
                    self.speed = self.count
                    self.spawnflags = self.spawnflags or 1
                }

                if (0 == (self.spawnflags and 2))
                    self.count = 0
            }
        }

        var SP_func_conveyor: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_conveyor"
            }

            public fun think(self: edict_t): Boolean {

                if (0 == self.speed)
                    self.speed = 100

                if (0 == (self.spawnflags and 1)) {
                    self.count = self.speed as Int
                    self.speed = 0
                }

                self.use = func_conveyor_use

                GameBase.gi.setmodel(self, self.model)
                self.solid = Defines.SOLID_BSP
                GameBase.gi.linkentity(self)
                return true
            }
        }

        /*
     * QUAKED func_door_secret (0 .5 .8) ? always_shoot 1st_left 1st_down A
     * secret door. Slide back and then to the side.
     * 
     * open_once doors never closes 1st_left 1st move is left of arrow 1st_down
     * 1st move is down from arrow always_shoot door is shootebale even if
     * targeted
     * 
     * "angle" determines the direction "dmg" damage to inflic when blocked
     * (default 2) "wait" how long to hold in the open position (default 5, -1
     * means hold)
     */

        public val SECRET_ALWAYS_SHOOT: Int = 1

        public val SECRET_1ST_LEFT: Int = 2

        public val SECRET_1ST_DOWN: Int = 4

        var door_secret_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "door_secret_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                // make sure we're not already moving
                if (!Math3D.VectorEquals(self.s.origin, Globals.vec3_origin))
                    return

                Move_Calc(self, self.pos1, door_secret_move1)
                door_use_areaportals(self, true)
            }
        }

        var door_secret_move1: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move1"
            }

            public fun think(self: edict_t): Boolean {
                self.nextthink = GameBase.level.time + 1.0.toFloat()
                self.think = door_secret_move2
                return true
            }
        }

        var door_secret_move2: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move2"
            }

            public fun think(self: edict_t): Boolean {
                Move_Calc(self, self.pos2, door_secret_move3)
                return true
            }
        }

        var door_secret_move3: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move3"
            }

            public fun think(self: edict_t): Boolean {
                if (self.wait == -1)
                    return true
                self.nextthink = GameBase.level.time + self.wait
                self.think = door_secret_move4
                return true
            }
        }

        var door_secret_move4: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move4"
            }

            public fun think(self: edict_t): Boolean {
                Move_Calc(self, self.pos1, door_secret_move5)
                return true
            }
        }

        var door_secret_move5: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move5"
            }

            public fun think(self: edict_t): Boolean {
                self.nextthink = GameBase.level.time + 1.0.toFloat()
                self.think = door_secret_move6
                return true
            }
        }

        var door_secret_move6: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move6"
            }

            public fun think(self: edict_t): Boolean {
                Move_Calc(self, Globals.vec3_origin, door_secret_done)
                return true
            }
        }

        var door_secret_done: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "door_secret_move7"
            }

            public fun think(self: edict_t): Boolean {
                if (null == (self.targetname) || 0 != (self.spawnflags and SECRET_ALWAYS_SHOOT)) {
                    self.health = 0
                    self.takedamage = Defines.DAMAGE_YES
                }
                door_use_areaportals(self, false)
                return true
            }
        }

        var door_secret_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "door_secret_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t?) {
                if (0 == (other!!.svflags and Defines.SVF_MONSTER) && (null == other!!.client)) {
                    // give it a chance to go away on it's own terms (like gibs)
                    GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, 100000, 1, 0, Defines.MOD_CRUSH)
                    // if it's still there, nuke it
                    if (other != null)
                        GameMisc.BecomeExplosion1(other)
                    return
                }

                if (GameBase.level.time < self.touch_debounce_time)
                    return
                self.touch_debounce_time = GameBase.level.time + 0.5.toFloat()

                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other!!.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)
            }
        }

        var door_secret_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "door_secret_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                self.takedamage = Defines.DAMAGE_NO
                door_secret_use.use(self, attacker, attacker)
            }
        }

        var SP_func_door_secret: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_door_secret"
            }

            public fun think(ent: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val up = floatArray(0.0, 0.0, 0.0)
                val side: Float
                val width: Float
                val length: Float

                ent.moveinfo.sound_start = GameBase.gi.soundindex("doors/dr1_strt.wav")
                ent.moveinfo.sound_middle = GameBase.gi.soundindex("doors/dr1_mid.wav")
                ent.moveinfo.sound_end = GameBase.gi.soundindex("doors/dr1_end.wav")

                ent.movetype = Defines.MOVETYPE_PUSH
                ent.solid = Defines.SOLID_BSP
                GameBase.gi.setmodel(ent, ent.model)

                ent.blocked = door_secret_blocked
                ent.use = door_secret_use

                if (null == (ent.targetname) || 0 != (ent.spawnflags and SECRET_ALWAYS_SHOOT)) {
                    ent.health = 0
                    ent.takedamage = Defines.DAMAGE_YES
                    ent.die = door_secret_die
                }

                if (0 == ent.dmg)
                    ent.dmg = 2

                if (0 == ent.wait)
                    ent.wait = 5

                ent.moveinfo.accel = ent.moveinfo.decel = ent.moveinfo.speed = 50

                // calculate positions
                Math3D.AngleVectors(ent.s.angles, forward, right, up)
                Math3D.VectorClear(ent.s.angles)
                side = 1.0.toFloat() - (ent.spawnflags and SECRET_1ST_LEFT)
                if ((ent.spawnflags and SECRET_1ST_DOWN) != 0)
                    width = Math.abs(Math3D.DotProduct(up, ent.size))
                else
                    width = Math.abs(Math3D.DotProduct(right, ent.size))
                length = Math.abs(Math3D.DotProduct(forward, ent.size))
                if ((ent.spawnflags and SECRET_1ST_DOWN) != 0)
                    Math3D.VectorMA(ent.s.origin, (-1).toFloat() * width, up, ent.pos1)
                else
                    Math3D.VectorMA(ent.s.origin, side * width, right, ent.pos1)
                Math3D.VectorMA(ent.pos1, length, forward, ent.pos2)

                if (ent.health != 0) {
                    ent.takedamage = Defines.DAMAGE_YES
                    ent.die = door_killed
                    ent.max_health = ent.health
                } else if (ent.targetname != null && ent.message != null) {
                    GameBase.gi.soundindex("misc/talk.wav")
                    ent.touch = door_touch
                }

                ent.classname = "func_door"

                GameBase.gi.linkentity(ent)
                return true
            }
        }

        /**
         * QUAKED func_killbox (1 0 0) ? Kills everything inside when fired,
         * irrespective of protection.
         */
        var use_killbox: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "use_killbox"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                GameUtil.KillBox(self)
            }
        }

        var SP_func_killbox: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_killbox"
            }

            public fun think(ent: edict_t): Boolean {
                GameBase.gi.setmodel(ent, ent.model)
                ent.use = use_killbox
                ent.svflags = Defines.SVF_NOCLIENT
                return true
            }
        }
    }
}