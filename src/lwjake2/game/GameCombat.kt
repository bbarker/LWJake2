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
import lwjake2.qcommon.Com
import lwjake2.util.Math3D

public class GameCombat {
    companion object {

        /**
         * CanDamage

         * Returns true if the inflictor can directly damage the target. Used for
         * explosions and melee attacks.
         */
        fun CanDamage(targ: edict_t, inflictor: edict_t): Boolean {
            val dest = floatArray(0.0, 0.0, 0.0)
            var trace: trace_t

            // bmodels need special checking because their origin is 0,0,0
            if (targ.movetype == Defines.MOVETYPE_PUSH) {
                Math3D.VectorAdd(targ.absmin, targ.absmax, dest)
                Math3D.VectorScale(dest, 0.5.toFloat(), dest)
                trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, dest, inflictor, Defines.MASK_SOLID)
                if (trace.fraction == 1.0.toFloat())
                    return true
                if (trace.ent == targ)
                    return true
                return false
            }

            trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, targ.s.origin, inflictor, Defines.MASK_SOLID)
            if (trace.fraction == 1.0)
                return true

            Math3D.VectorCopy(targ.s.origin, dest)
            dest[0] += 15.0
            dest[1] += 15.0
            trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, dest, inflictor, Defines.MASK_SOLID)
            if (trace.fraction == 1.0)
                return true

            Math3D.VectorCopy(targ.s.origin, dest)
            dest[0] += 15.0
            dest[1] -= 15.0
            trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, dest, inflictor, Defines.MASK_SOLID)
            if (trace.fraction == 1.0)
                return true

            Math3D.VectorCopy(targ.s.origin, dest)
            dest[0] -= 15.0
            dest[1] += 15.0
            trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, dest, inflictor, Defines.MASK_SOLID)
            if (trace.fraction == 1.0)
                return true

            Math3D.VectorCopy(targ.s.origin, dest)
            dest[0] -= 15.0
            dest[1] -= 15.0
            trace = GameBase.gi.trace(inflictor.s.origin, Globals.vec3_origin, Globals.vec3_origin, dest, inflictor, Defines.MASK_SOLID)
            if (trace.fraction == 1.0)
                return true

            return false
        }

        /**
         * Killed.
         */
        public fun Killed(targ: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
            Com.DPrintf("Killing a " + targ.classname + "\n")
            if (targ.health < -999)
                targ.health = -999

            targ.enemy = attacker

            if ((targ.svflags and Defines.SVF_MONSTER) != 0 && (targ.deadflag != Defines.DEAD_DEAD)) {
                //			targ.svflags |= SVF_DEADMONSTER; // now treat as a different
                // content type
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_GOOD_GUY)) {
                    GameBase.level.killed_monsters++
                    if (GameBase.coop.value != 0 && attacker.client != null)
                        attacker.client.resp.score++
                    // medics won't heal monsters that they kill themselves
                    if (attacker.classname.equals("monster_medic"))
                        targ.owner = attacker
                }
            }

            if (targ.movetype == Defines.MOVETYPE_PUSH || targ.movetype == Defines.MOVETYPE_STOP || targ.movetype == Defines.MOVETYPE_NONE) {
                // doors, triggers,
                // etc
                targ.die.die(targ, inflictor, attacker, damage, point)
                return
            }

            if ((targ.svflags and Defines.SVF_MONSTER) != 0 && (targ.deadflag != Defines.DEAD_DEAD)) {
                targ.touch = null
                Monster.monster_death_use(targ)
            }

            targ.die.die(targ, inflictor, attacker, damage, point)
        }

        /**
         * SpawnDamage.
         */
        fun SpawnDamage(type: Int, origin: FloatArray, normal: FloatArray, damage: Int) {
            var damage = damage
            if (damage > 255)
                damage = 255
            GameBase.gi.WriteByte(Defines.svc_temp_entity)
            GameBase.gi.WriteByte(type)
            //		gi.WriteByte (damage);
            GameBase.gi.WritePosition(origin)
            GameBase.gi.WriteDir(normal)
            GameBase.gi.multicast(origin, Defines.MULTICAST_PVS)
        }

        fun CheckPowerArmor(ent: edict_t, point: FloatArray, normal: FloatArray, damage: Int, dflags: Int): Int {
            var damage = damage
            val client: gclient_t?
            var save: Int
            val power_armor_type: Int
            var index = 0
            val damagePerCell: Int
            val pa_te_type: Int
            var power = 0
            val power_used: Int

            if (damage == 0)
                return 0

            client = ent.client

            if ((dflags and Defines.DAMAGE_NO_ARMOR) != 0)
                return 0

            if (client != null) {
                power_armor_type = GameItems.PowerArmorType(ent)
                if (power_armor_type != Defines.POWER_ARMOR_NONE) {
                    index = GameItems.ITEM_INDEX(GameItems.FindItem("Cells"))
                    power = client!!.pers.inventory[index]
                }
            } else if ((ent.svflags and Defines.SVF_MONSTER) != 0) {
                power_armor_type = ent.monsterinfo.power_armor_type
                power = ent.monsterinfo.power_armor_power
            } else
                return 0

            if (power_armor_type == Defines.POWER_ARMOR_NONE)
                return 0
            if (power == 0)
                return 0

            if (power_armor_type == Defines.POWER_ARMOR_SCREEN) {
                val vec = floatArray(0.0, 0.0, 0.0)
                val dot: Float
                val forward = floatArray(0.0, 0.0, 0.0)

                // only works if damage point is in front
                Math3D.AngleVectors(ent.s.angles, forward, null, null)
                Math3D.VectorSubtract(point, ent.s.origin, vec)
                Math3D.VectorNormalize(vec)
                dot = Math3D.DotProduct(vec, forward)
                if (dot <= 0.3)
                    return 0

                damagePerCell = 1
                pa_te_type = Defines.TE_SCREEN_SPARKS
                damage = damage / 3
            } else {
                damagePerCell = 2
                pa_te_type = Defines.TE_SHIELD_SPARKS
                damage = (2 * damage) / 3
            }

            save = power * damagePerCell

            if (save == 0)
                return 0
            if (save > damage)
                save = damage

            SpawnDamage(pa_te_type, point, normal, save)
            ent.powerarmor_time = GameBase.level.time + 0.2.toFloat()

            power_used = save / damagePerCell

            if (client != null)
                client!!.pers.inventory[index] -= power_used
            else
                ent.monsterinfo.power_armor_power -= power_used
            return save
        }

        fun CheckArmor(ent: edict_t, point: FloatArray, normal: FloatArray, damage: Int, te_sparks: Int, dflags: Int): Int {
            val client: gclient_t?
            var save: Int
            val index: Int
            val armor: gitem_t

            if (damage == 0)
                return 0

            client = ent.client

            if (client == null)
                return 0

            if ((dflags and Defines.DAMAGE_NO_ARMOR) != 0)
                return 0

            index = GameItems.ArmorIndex(ent)

            if (index == 0)
                return 0

            armor = GameItems.GetItemByIndex(index)
            val garmor = armor.info as gitem_armor_t

            if (0 != (dflags and Defines.DAMAGE_ENERGY))
                save = Math.ceil(garmor.energy_protection * damage) as Int
            else
                save = Math.ceil(garmor.normal_protection * damage) as Int

            if (save >= client!!.pers.inventory[index])
                save = client!!.pers.inventory[index]

            if (save == 0)
                return 0

            client!!.pers.inventory[index] -= save
            SpawnDamage(te_sparks, point, normal, save)

            return save
        }

        public fun M_ReactToDamage(targ: edict_t, attacker: edict_t) {
            if ((null != attacker.client) && 0 != (attacker.svflags and Defines.SVF_MONSTER))
                return

            if (attacker == targ || attacker == targ.enemy)
                return

            // if we are a good guy monster and our attacker is a player
            // or another good guy, do not get mad at them
            if (0 != (targ.monsterinfo.aiflags and Defines.AI_GOOD_GUY)) {
                if (attacker.client != null || (attacker.monsterinfo.aiflags and Defines.AI_GOOD_GUY) != 0)
                    return
            }

            // we now know that we are not both good guys

            // if attacker is a client, get mad at them because he's good and we're
            // not
            if (attacker.client != null) {
                targ.monsterinfo.aiflags = targ.monsterinfo.aiflags and Defines.AI_SOUND_TARGET.inv()

                // this can only happen in coop (both new and old enemies are
                // clients)
                // only switch if can't see the current enemy
                if (targ.enemy != null && targ.enemy.client != null) {
                    if (GameUtil.visible(targ, targ.enemy)) {
                        targ.oldenemy = attacker
                        return
                    }
                    targ.oldenemy = targ.enemy
                }
                targ.enemy = attacker
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_DUCKED))
                    GameUtil.FoundTarget(targ)
                return
            }

            // it's the same base (walk/swim/fly) type and a different classname and
            // it's not a tank
            // (they spray too much), get mad at them
            if (((targ.flags and (Defines.FL_FLY or Defines.FL_SWIM)) == (attacker.flags and (Defines.FL_FLY or Defines.FL_SWIM))) && (!(targ.classname.equals(attacker.classname))) && (!(attacker.classname.equals("monster_tank"))) && (!(attacker.classname.equals("monster_supertank"))) && (!(attacker.classname.equals("monster_makron"))) && (!(attacker.classname.equals("monster_jorg")))) {
                if (targ.enemy != null && targ.enemy.client != null)
                    targ.oldenemy = targ.enemy
                targ.enemy = attacker
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_DUCKED))
                    GameUtil.FoundTarget(targ)
            } else if (attacker.enemy == targ) {
                if (targ.enemy != null && targ.enemy.client != null)
                    targ.oldenemy = targ.enemy
                targ.enemy = attacker
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_DUCKED))
                    GameUtil.FoundTarget(targ)
            } else if (attacker.enemy != null && attacker.enemy != targ) {
                if (targ.enemy != null && targ.enemy.client != null)
                    targ.oldenemy = targ.enemy
                targ.enemy = attacker.enemy
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_DUCKED))
                    GameUtil.FoundTarget(targ)
            }// otherwise get mad at whoever they are mad at (help our buddy) unless
            // it is us!
            // if they *meant* to shoot us, then shoot back
        }

        fun CheckTeamDamage(targ: edict_t, attacker: edict_t): Boolean {
            //FIXME make the next line real and uncomment this block
            // if ((ability to damage a teammate == OFF) && (targ's team ==
            // attacker's team))
            return false
        }

        /**
         * T_RadiusDamage.
         */
        fun T_RadiusDamage(inflictor: edict_t, attacker: edict_t, damage: Float, ignore: edict_t, radius: Float, mod: Int) {
            var points: Float
            var edictit: EdictIterator? = null

            val v = floatArray(0.0, 0.0, 0.0)
            val dir = floatArray(0.0, 0.0, 0.0)

            while ((edictit = GameBase.findradius(edictit, inflictor.s.origin, radius)) != null) {
                val ent = edictit!!.o
                if (ent == ignore)
                    continue
                if (ent.takedamage == 0)
                    continue

                Math3D.VectorAdd(ent.mins, ent.maxs, v)
                Math3D.VectorMA(ent.s.origin, 0.5.toFloat(), v, v)
                Math3D.VectorSubtract(inflictor.s.origin, v, v)
                points = damage - 0.5.toFloat() * Math3D.VectorLength(v)
                if (ent == attacker)
                    points = points * 0.5.toFloat()
                if (points > 0) {
                    if (CanDamage(ent, inflictor)) {
                        Math3D.VectorSubtract(ent.s.origin, inflictor.s.origin, dir)
                        T_Damage(ent, inflictor, attacker, dir, inflictor.s.origin, Globals.vec3_origin, points.toInt(), points.toInt(), Defines.DAMAGE_RADIUS, mod)
                    }
                }
            }
        }

        public fun T_Damage(targ: edict_t, inflictor: edict_t, attacker: edict_t, dir: FloatArray, point: FloatArray, normal: FloatArray, damage: Int, knockback: Int, dflags: Int, mod: Int) {
            var damage = damage
            var knockback = knockback
            var mod = mod
            val client: gclient_t?
            var take: Int
            var save: Int
            var asave: Int
            val psave: Int
            val te_sparks: Int

            if (targ.takedamage == 0)
                return

            // friendly fire avoidance
            // if enabled you can't hurt teammates (but you can hurt yourself)
            // knockback still occurs
            if ((targ != attacker) && ((GameBase.deathmatch.value != 0 && 0 != ((GameBase.dmflags.value) as Int and (Defines.DF_MODELTEAMS or Defines.DF_SKINTEAMS))) || GameBase.coop.value != 0)) {
                if (GameUtil.OnSameTeam(targ, attacker)) {
                    if (((GameBase.dmflags.value) as Int and Defines.DF_NO_FRIENDLY_FIRE) != 0)
                        damage = 0
                    else
                        mod = mod or Defines.MOD_FRIENDLY_FIRE
                }
            }
            GameBase.meansOfDeath = mod

            // easy mode takes half damage
            if (GameBase.skill.value == 0 && GameBase.deathmatch.value == 0 && targ.client != null) {
                damage *= 0.5
                if (damage == 0)
                    damage = 1
            }

            client = targ.client

            if ((dflags and Defines.DAMAGE_BULLET) != 0)
                te_sparks = Defines.TE_BULLET_SPARKS
            else
                te_sparks = Defines.TE_SPARKS

            Math3D.VectorNormalize(dir)

            // bonus damage for suprising a monster
            if (0 == (dflags and Defines.DAMAGE_RADIUS) && (targ.svflags and Defines.SVF_MONSTER) != 0 && (attacker.client != null) && (targ.enemy == null) && (targ.health > 0))
                damage *= 2

            if ((targ.flags and Defines.FL_NO_KNOCKBACK) != 0)
                knockback = 0

            // figure momentum add
            if (0 == (dflags and Defines.DAMAGE_NO_KNOCKBACK)) {
                if ((knockback != 0) && (targ.movetype != Defines.MOVETYPE_NONE) && (targ.movetype != Defines.MOVETYPE_BOUNCE) && (targ.movetype != Defines.MOVETYPE_PUSH) && (targ.movetype != Defines.MOVETYPE_STOP)) {
                    val kvel = floatArray(0.0, 0.0, 0.0)
                    val mass: Float

                    if (targ.mass < 50)
                        mass = 50
                    else
                        mass = targ.mass

                    if (targ.client != null && attacker == targ)
                        Math3D.VectorScale(dir, 1600.0.toFloat() * knockback.toFloat() / mass, kvel)
                    else
                        Math3D.VectorScale(dir, 500.0.toFloat() * knockback.toFloat() / mass, kvel)// the rocket jump hack...

                    Math3D.VectorAdd(targ.velocity, kvel, targ.velocity)
                }
            }

            take = damage
            save = 0

            // check for godmode
            if ((targ.flags and Defines.FL_GODMODE) != 0 && 0 == (dflags and Defines.DAMAGE_NO_PROTECTION)) {
                take = 0
                save = damage
                SpawnDamage(te_sparks, point, normal, save)
            }

            // check for invincibility
            if ((client != null && client!!.invincible_framenum > GameBase.level.framenum) && 0 == (dflags and Defines.DAMAGE_NO_PROTECTION)) {
                if (targ.pain_debounce_time < GameBase.level.time) {
                    GameBase.gi.sound(targ, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/protect4.wav"), 1, Defines.ATTN_NORM, 0)
                    targ.pain_debounce_time = GameBase.level.time + 2
                }
                take = 0
                save = damage
            }

            psave = CheckPowerArmor(targ, point, normal, take, dflags)
            take -= psave

            asave = CheckArmor(targ, point, normal, take, te_sparks, dflags)
            take -= asave

            // treat cheat/powerup savings the same as armor
            asave += save

            // team damage avoidance
            if (0 == (dflags and Defines.DAMAGE_NO_PROTECTION) && CheckTeamDamage(targ, attacker))
                return

            // do the damage
            if (take != 0) {
                if (0 != (targ.svflags and Defines.SVF_MONSTER) || (client != null))
                    SpawnDamage(Defines.TE_BLOOD, point, normal, take)
                else
                    SpawnDamage(te_sparks, point, normal, take)

                targ.health = targ.health - take

                if (targ.health <= 0) {
                    if ((targ.svflags and Defines.SVF_MONSTER) != 0 || (client != null))
                        targ.flags = targ.flags or Defines.FL_NO_KNOCKBACK
                    Killed(targ, inflictor, attacker, take, point)
                    return
                }
            }

            if ((targ.svflags and Defines.SVF_MONSTER) != 0) {
                M_ReactToDamage(targ, attacker)
                if (0 == (targ.monsterinfo.aiflags and Defines.AI_DUCKED) && (take != 0)) {
                    targ.pain.pain(targ, attacker, knockback, take)
                    // nightmare mode monsters don't go into pain frames often
                    if (GameBase.skill.value == 3)
                        targ.pain_debounce_time = GameBase.level.time + 5
                }
            } else if (client != null) {
                if (((targ.flags and Defines.FL_GODMODE) == 0) && (take != 0))
                    targ.pain.pain(targ, attacker, knockback, take)
            } else if (take != 0) {
                if (targ.pain != null)
                    targ.pain.pain(targ, attacker, knockback, take)
            }

            // add to the damage inflicted on a player this frame
            // the total will be turned into screen blends and view angle kicks
            // at the end of the frame
            if (client != null) {
                client!!.damage_parmor += psave
                client!!.damage_armor += asave
                client!!.damage_blood += take
                client!!.damage_knockback += knockback
                Math3D.VectorCopy(point, client!!.damage_from)
            }
        }
    }
}
