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
import lwjake2.game.monsters.M_Infantry
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class GameTurret {
    companion object {

        public fun AnglesNormalize(vec: FloatArray) {
            while (vec[0] > 360)
                vec[0] -= 360
            while (vec[0] < 0)
                vec[0] += 360
            while (vec[1] > 360)
                vec[1] -= 360
            while (vec[1] < 0)
                vec[1] += 360
        }

        public fun SnapToEights(x: Float): Float {
            var x = x
            x *= 8.0
            if (x > 0.0)
                x += 0.5
            else
                x -= 0.5
            return 0.125.toFloat() * x.toInt().toFloat()
        }

        /**
         * QUAKED turret_breach (0 0 0) ? This portion of the turret can change both
         * pitch and yaw. The model should be made with a flat pitch. It (and the
         * associated base) need to be oriented towards 0. Use "angle" to set the
         * starting angle.

         * "speed" default 50 "dmg" default 10 "angle" point this forward "target"
         * point this at an info_notnull at the muzzle tip "minpitch" min acceptable
         * pitch angle : default -30 "maxpitch" max acceptable pitch angle : default
         * 30 "minyaw" min acceptable yaw angle : default 0 "maxyaw" max acceptable
         * yaw angle : default 360
         */

        public fun turret_breach_fire(self: edict_t) {
            val f = floatArray(0.0, 0.0, 0.0)
            val r = floatArray(0.0, 0.0, 0.0)
            val u = floatArray(0.0, 0.0, 0.0)
            val start = floatArray(0.0, 0.0, 0.0)
            val damage: Int
            val speed: Int

            Math3D.AngleVectors(self.s.angles, f, r, u)
            Math3D.VectorMA(self.s.origin, self.move_origin[0], f, start)
            Math3D.VectorMA(start, self.move_origin[1], r, start)
            Math3D.VectorMA(start, self.move_origin[2], u, start)

            damage = (100 + Lib.random() * 50) as Int
            speed = (550 + 50 * GameBase.skill.value) as Int
            GameWeapon.fire_rocket(self.teammaster.owner, start, f, damage, speed, 150, damage)
            GameBase.gi.positioned_sound(start, self, Defines.CHAN_WEAPON, GameBase.gi.soundindex("weapons/rocklf1a.wav"), 1, Defines.ATTN_NORM, 0)
        }

        public fun SP_turret_breach(self: edict_t) {
            self.solid = Defines.SOLID_BSP
            self.movetype = Defines.MOVETYPE_PUSH
            GameBase.gi.setmodel(self, self.model)

            if (self.speed == 0)
                self.speed = 50
            if (self.dmg == 0)
                self.dmg = 10

            if (GameBase.st.minpitch == 0)
                GameBase.st.minpitch = -30
            if (GameBase.st.maxpitch == 0)
                GameBase.st.maxpitch = 30
            if (GameBase.st.maxyaw == 0)
                GameBase.st.maxyaw = 360

            self.pos1[Defines.PITCH] = -1 * GameBase.st.minpitch
            self.pos1[Defines.YAW] = GameBase.st.minyaw
            self.pos2[Defines.PITCH] = -1 * GameBase.st.maxpitch
            self.pos2[Defines.YAW] = GameBase.st.maxyaw

            self.ideal_yaw = self.s.angles[Defines.YAW]
            self.move_angles[Defines.YAW] = self.ideal_yaw

            self.blocked = turret_blocked

            self.think = turret_breach_finish_init
            self.nextthink = GameBase.level.time + Defines.FRAMETIME
            GameBase.gi.linkentity(self)
        }

        /**
         * QUAKED turret_base (0 0 0) ? This portion of the turret changes yaw only.
         * MUST be teamed with a turret_breach.
         */

        public fun SP_turret_base(self: edict_t) {
            self.solid = Defines.SOLID_BSP
            self.movetype = Defines.MOVETYPE_PUSH
            GameBase.gi.setmodel(self, self.model)
            self.blocked = turret_blocked
            GameBase.gi.linkentity(self)
        }

        public fun SP_turret_driver(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            self.movetype = Defines.MOVETYPE_PUSH
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/infantry/tris.md2")
            Math3D.VectorSet(self.mins, -16, -16, -24)
            Math3D.VectorSet(self.maxs, 16, 16, 32)

            self.health = 100
            self.gib_health = 0
            self.mass = 200
            self.viewheight = 24

            self.die = turret_driver_die
            self.monsterinfo.stand = M_Infantry.infantry_stand

            self.flags = self.flags or Defines.FL_NO_KNOCKBACK

            GameBase.level.total_monsters++

            self.svflags = self.svflags or Defines.SVF_MONSTER
            self.s.renderfx = self.s.renderfx or Defines.RF_FRAMELERP
            self.takedamage = Defines.DAMAGE_AIM
            self.use = GameUtil.monster_use
            self.clipmask = Defines.MASK_MONSTERSOLID
            Math3D.VectorCopy(self.s.origin, self.s.old_origin)
            self.monsterinfo.aiflags = self.monsterinfo.aiflags or (Defines.AI_STAND_GROUND or Defines.AI_DUCKED)

            if (GameBase.st.item != null) {
                self.item = GameItems.FindItemByClassname(GameBase.st.item)
                if (self.item == null)
                    GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " has bad item: " + GameBase.st.item + "\n")
            }

            self.think = turret_driver_link
            self.nextthink = GameBase.level.time + Defines.FRAMETIME

            GameBase.gi.linkentity(self)
        }

        var turret_blocked: EntBlockedAdapter = object : EntBlockedAdapter() {
            public fun getID(): String {
                return "turret_blocked"
            }

            public fun blocked(self: edict_t, other: edict_t) {
                val attacker: edict_t

                if (other.takedamage != 0) {
                    if (self.teammaster.owner != null)
                        attacker = self.teammaster.owner
                    else
                        attacker = self.teammaster
                    GameCombat.T_Damage(other, self, attacker, Globals.vec3_origin, other.s.origin, Globals.vec3_origin, self.teammaster.dmg, 10, 0, Defines.MOD_CRUSH)
                }
            }
        }

        var turret_breach_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "turret_breach_think"
            }

            public fun think(self: edict_t): Boolean {

                var ent: edict_t?
                val current_angles = floatArray(0.0, 0.0, 0.0)
                val delta = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorCopy(self.s.angles, current_angles)
                AnglesNormalize(current_angles)

                AnglesNormalize(self.move_angles)
                if (self.move_angles[Defines.PITCH] > 180)
                    self.move_angles[Defines.PITCH] -= 360

                // clamp angles to mins & maxs
                if (self.move_angles[Defines.PITCH] > self.pos1[Defines.PITCH])
                    self.move_angles[Defines.PITCH] = self.pos1[Defines.PITCH]
                else if (self.move_angles[Defines.PITCH] < self.pos2[Defines.PITCH])
                    self.move_angles[Defines.PITCH] = self.pos2[Defines.PITCH]

                if ((self.move_angles[Defines.YAW] < self.pos1[Defines.YAW]) || (self.move_angles[Defines.YAW] > self.pos2[Defines.YAW])) {
                    var dmin: Float
                    var dmax: Float

                    dmin = Math.abs(self.pos1[Defines.YAW] - self.move_angles[Defines.YAW])
                    if (dmin < -180)
                        dmin += 360
                    else if (dmin > 180)
                        dmin -= 360
                    dmax = Math.abs(self.pos2[Defines.YAW] - self.move_angles[Defines.YAW])
                    if (dmax < -180)
                        dmax += 360
                    else if (dmax > 180)
                        dmax -= 360
                    if (Math.abs(dmin) < Math.abs(dmax))
                        self.move_angles[Defines.YAW] = self.pos1[Defines.YAW]
                    else
                        self.move_angles[Defines.YAW] = self.pos2[Defines.YAW]
                }

                Math3D.VectorSubtract(self.move_angles, current_angles, delta)
                if (delta[0] < -180)
                    delta[0] += 360
                else if (delta[0] > 180)
                    delta[0] -= 360
                if (delta[1] < -180)
                    delta[1] += 360
                else if (delta[1] > 180)
                    delta[1] -= 360
                delta[2] = 0

                if (delta[0] > self.speed * Defines.FRAMETIME)
                    delta[0] = self.speed * Defines.FRAMETIME
                if (delta[0] < -1 * self.speed * Defines.FRAMETIME)
                    delta[0] = -1 * self.speed * Defines.FRAMETIME
                if (delta[1] > self.speed * Defines.FRAMETIME)
                    delta[1] = self.speed * Defines.FRAMETIME
                if (delta[1] < -1 * self.speed * Defines.FRAMETIME)
                    delta[1] = -1 * self.speed * Defines.FRAMETIME

                Math3D.VectorScale(delta, 1.0.toFloat() / Defines.FRAMETIME, self.avelocity)

                self.nextthink = GameBase.level.time + Defines.FRAMETIME

                run {
                    ent = self.teammaster
                    while (ent != null) {
                        ent!!.avelocity[1] = self.avelocity[1]
                        ent = ent!!.teamchain
                    }
                }

                // if we have adriver, adjust his velocities
                if (self.owner != null) {
                    var angle: Float
                    val target_z: Float
                    val diff: Float
                    val target = floatArray(0.0, 0.0, 0.0)
                    val dir = floatArray(0.0, 0.0, 0.0)

                    // angular is easy, just copy ours
                    self.owner.avelocity[0] = self.avelocity[0]
                    self.owner.avelocity[1] = self.avelocity[1]

                    // x & y
                    angle = self.s.angles[1] + self.owner.move_origin[1]
                    angle *= (Math.PI * 2 / 360)
                    target[0] = GameTurret.SnapToEights((self.s.origin[0] + Math.cos(angle) * self.owner.move_origin[0]) as Float)
                    target[1] = GameTurret.SnapToEights((self.s.origin[1] + Math.sin(angle) * self.owner.move_origin[0]) as Float)
                    target[2] = self.owner.s.origin[2]

                    Math3D.VectorSubtract(target, self.owner.s.origin, dir)
                    self.owner.velocity[0] = dir[0] * 1.0.toFloat() / Defines.FRAMETIME
                    self.owner.velocity[1] = dir[1] * 1.0.toFloat() / Defines.FRAMETIME

                    // z
                    angle = self.s.angles[Defines.PITCH] * (Math.PI * 2.toFloat() / 360.toFloat()) as Float
                    target_z = GameTurret.SnapToEights((self.s.origin[2] + self.owner.move_origin[0] * Math.tan(angle) + self.owner.move_origin[2]) as Float)

                    diff = target_z - self.owner.s.origin[2]
                    self.owner.velocity[2] = diff * 1.0.toFloat() / Defines.FRAMETIME

                    if ((self.spawnflags and 65536) != 0) {
                        turret_breach_fire(self)
                        self.spawnflags = self.spawnflags and 65536.inv()
                    }
                }
                return true
            }
        }

        var turret_breach_finish_init: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "turret_breach_finish_init"
            }

            public fun think(self: edict_t): Boolean {

                // get and save info for muzzle location
                if (self.target == null) {
                    GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " needs a target\n")
                } else {
                    self.target_ent = GameBase.G_PickTarget(self.target)
                    Math3D.VectorSubtract(self.target_ent.s.origin, self.s.origin, self.move_origin)
                    GameUtil.G_FreeEdict(self.target_ent)
                }

                self.teammaster.dmg = self.dmg
                self.think = turret_breach_think
                self.think.think(self)
                return true
            }
        }

        /*
     * QUAKED turret_driver (1 .5 0) (-16 -16 -24) (16 16 32) Must NOT be on the
     * team with the rest of the turret parts. Instead it must target the
     * turret_breach.
     */
        var turret_driver_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "turret_driver_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {

                var ent: edict_t

                // level the gun
                self.target_ent.move_angles[0] = 0

                // remove the driver from the end of them team chain
                run {
                    ent = self.target_ent.teammaster
                    while (ent.teamchain != self) {
                        ent = ent.teamchain
                    }
                }
                ent.teamchain = null
                self.teammaster = null
                self.flags = self.flags and Defines.FL_TEAMSLAVE.inv()

                self.target_ent.owner = null
                self.target_ent.teammaster.owner = null

                M_Infantry.infantry_die.die(self, inflictor, attacker, damage, null)
            }
        }

        var turret_driver_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "turret_driver_think"
            }

            public fun think(self: edict_t): Boolean {

                val target = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val reaction_time: Float

                self.nextthink = GameBase.level.time + Defines.FRAMETIME

                if (self.enemy != null && (!self.enemy.inuse || self.enemy.health <= 0))
                    self.enemy = null

                if (null == self.enemy) {
                    if (!GameUtil.FindTarget(self))
                        return true
                    self.monsterinfo.trail_time = GameBase.level.time
                    self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_LOST_SIGHT.inv()
                } else {
                    if (GameUtil.visible(self, self.enemy)) {
                        if ((self.monsterinfo.aiflags and Defines.AI_LOST_SIGHT) != 0) {
                            self.monsterinfo.trail_time = GameBase.level.time
                            self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_LOST_SIGHT.inv()
                        }
                    } else {
                        self.monsterinfo.aiflags = self.monsterinfo.aiflags or Defines.AI_LOST_SIGHT
                        return true
                    }
                }

                // let the turret know where we want it to aim
                Math3D.VectorCopy(self.enemy.s.origin, target)
                target[2] += self.enemy.viewheight
                Math3D.VectorSubtract(target, self.target_ent.s.origin, dir)
                Math3D.vectoangles(dir, self.target_ent.move_angles)

                // decide if we should shoot
                if (GameBase.level.time < self.monsterinfo.attack_finished)
                    return true

                reaction_time = (3 - GameBase.skill.value) * 1.0.toFloat()
                if ((GameBase.level.time - self.monsterinfo.trail_time) < reaction_time)
                    return true

                self.monsterinfo.attack_finished = GameBase.level.time + reaction_time + 1.0.toFloat()
                //FIXME how do we really want to pass this along?
                self.target_ent.spawnflags = self.target_ent.spawnflags or 65536
                return true
            }
        }

        public var turret_driver_link: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "turret_driver_link"
            }

            public fun think(self: edict_t): Boolean {

                val vec = floatArray(0.0, 0.0, 0.0)
                var ent: edict_t

                self.think = turret_driver_think
                self.nextthink = GameBase.level.time + Defines.FRAMETIME

                self.target_ent = GameBase.G_PickTarget(self.target)
                self.target_ent.owner = self
                self.target_ent.teammaster.owner = self
                Math3D.VectorCopy(self.target_ent.s.angles, self.s.angles)

                vec[0] = self.target_ent.s.origin[0] - self.s.origin[0]
                vec[1] = self.target_ent.s.origin[1] - self.s.origin[1]
                vec[2] = 0
                self.move_origin[0] = Math3D.VectorLength(vec)

                Math3D.VectorSubtract(self.s.origin, self.target_ent.s.origin, vec)
                Math3D.vectoangles(vec, vec)
                AnglesNormalize(vec)

                self.move_origin[1] = vec[1]
                self.move_origin[2] = self.s.origin[2] - self.target_ent.s.origin[2]

                // add the driver to the end of them team chain
                run {
                    ent = self.target_ent.teammaster
                    while (ent.teamchain != null) {
                        ent = ent.teamchain
                    }
                }
                ent.teamchain = self
                self.teammaster = self.target_ent.teammaster
                self.flags = self.flags or Defines.FL_TEAMSLAVE
                return true
            }
        }
    }
}