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
import lwjake2.client.M
import lwjake2.qcommon.Com
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class Monster {
    companion object {

        // FIXME monsters should call these with a totally accurate direction
        //	and we can mess it up based on skill. Spread should be for normal
        //	and we can tighten or loosen based on skill. We could muck with
        //	the damages too, but I'm not sure that's such a good idea.
        public fun monster_fire_bullet(self: edict_t, start: FloatArray, dir: FloatArray, damage: Int, kick: Int, hspread: Int, vspread: Int, flashtype: Int) {
            GameWeapon.fire_bullet(self, start, dir, damage, kick, hspread, vspread, Defines.MOD_UNKNOWN)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the shotgun.  */
        public fun monster_fire_shotgun(self: edict_t, start: FloatArray, aimdir: FloatArray, damage: Int, kick: Int, hspread: Int, vspread: Int, count: Int, flashtype: Int) {
            GameWeapon.fire_shotgun(self, start, aimdir, damage, kick, hspread, vspread, count, Defines.MOD_UNKNOWN)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the blaster.  */
        public fun monster_fire_blaster(self: edict_t, start: FloatArray, dir: FloatArray, damage: Int, speed: Int, flashtype: Int, effect: Int) {
            GameWeapon.fire_blaster(self, start, dir, damage, speed, effect, false)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the grenade.  */
        public fun monster_fire_grenade(self: edict_t, start: FloatArray, aimdir: FloatArray, damage: Int, speed: Int, flashtype: Int) {
            GameWeapon.fire_grenade(self, start, aimdir, damage, speed, 2.5.toFloat(), damage + 40)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the rocket.  */
        public fun monster_fire_rocket(self: edict_t, start: FloatArray, dir: FloatArray, damage: Int, speed: Int, flashtype: Int) {
            GameWeapon.fire_rocket(self, start, dir, damage, speed, damage + 20, damage)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the railgun.  */
        public fun monster_fire_railgun(self: edict_t, start: FloatArray, aimdir: FloatArray, damage: Int, kick: Int, flashtype: Int) {
            GameWeapon.fire_rail(self, start, aimdir, damage, kick)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /** The Moster fires the bfg.  */
        public fun monster_fire_bfg(self: edict_t, start: FloatArray, aimdir: FloatArray, damage: Int, speed: Int, kick: Int, damage_radius: Float, flashtype: Int) {
            GameWeapon.fire_bfg(self, start, aimdir, damage, speed, damage_radius)

            GameBase.gi.WriteByte(Defines.svc_muzzleflash2)
            GameBase.gi.WriteShort(self.index)
            GameBase.gi.WriteByte(flashtype)
            GameBase.gi.multicast(start, Defines.MULTICAST_PVS)
        }

        /*
     * ================ monster_death_use
     * 
     * When a monster dies, it fires all of its targets with the current enemy
     * as activator. ================
     */
        public fun monster_death_use(self: edict_t) {
            self.flags = self.flags and (Defines.FL_FLY or Defines.FL_SWIM).inv()
            self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_GOOD_GUY

            if (self.item != null) {
                GameItems.Drop_Item(self, self.item)
                self.item = null
            }

            if (self.deathtarget != null)
                self.target = self.deathtarget

            if (self.target == null)
                return

            GameUtil.G_UseTargets(self, self.enemy)
        }

        // ============================================================================
        public fun monster_start(self: edict_t): Boolean {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return false
            }

            if ((self.spawnflags and 4) != 0 && 0 == (self.monsterinfo.aiflags and Defines.AI_GOOD_GUY)) {
                self.spawnflags = self.spawnflags and 4.inv()
                self.spawnflags = self.spawnflags or 1
                //		 gi.dprintf("fixed spawnflags on %s at %s\n", self.classname,
                // vtos(self.s.origin));
            }

            if (0 == (self.monsterinfo.aiflags and Defines.AI_GOOD_GUY))
                GameBase.level.total_monsters++

            self.nextthink = GameBase.level.time + Defines.FRAMETIME
            self.svflags = self.svflags or Defines.SVF_MONSTER
            self.s.renderfx = self.s.renderfx or Defines.RF_FRAMELERP
            self.takedamage = Defines.DAMAGE_AIM
            self.air_finished = GameBase.level.time + 12
            self.use = GameUtil.monster_use
            self.max_health = self.health
            self.clipmask = Defines.MASK_MONSTERSOLID

            self.s.skinnum = 0
            self.deadflag = Defines.DEAD_NO
            self.svflags = self.svflags and Defines.SVF_DEADMONSTER.inv()

            if (null == self.monsterinfo.checkattack)
                self.monsterinfo.checkattack = GameUtil.M_CheckAttack
            Math3D.VectorCopy(self.s.origin, self.s.old_origin)

            if (GameBase.st.item != null && GameBase.st.item.length() > 0) {
                self.item = GameItems.FindItemByClassname(GameBase.st.item)
                if (self.item == null)
                    GameBase.gi.dprintf("monster_start:" + self.classname + " at " + Lib.vtos(self.s.origin) + " has bad item: " + GameBase.st.item + "\n")
            }

            // randomize what frame they start on
            if (self.monsterinfo.currentmove != null)
                self.s.frame = self.monsterinfo.currentmove.firstframe + (Lib.rand() % (self.monsterinfo.currentmove.lastframe - self.monsterinfo.currentmove.firstframe + 1))

            return true
        }

        public fun monster_start_go(self: edict_t) {

            val v = floatArray(0.0, 0.0, 0.0)

            if (self.health <= 0)
                return

            // check for target to combat_point and change to combattarget
            if (self.target != null) {
                var notcombat: Boolean
                var fixup: Boolean
                var target: edict_t? = null
                notcombat = false
                fixup = false
                /*
             * if (true) { Com.Printf("all entities:\n");
             * 
             * for (int n = 0; n < Game.globals.num_edicts; n++) { edict_t ent =
             * GameBase.g_edicts[n]; Com.Printf( "|%4i | %25s
             * |%8.2f|%8.2f|%8.2f||%8.2f|%8.2f|%8.2f||%8.2f|%8.2f|%8.2f|\n", new
             * Vargs().add(n).add(ent.classname).
             * add(ent.s.origin[0]).add(ent.s.origin[1]).add(ent.s.origin[2])
             * .add(ent.mins[0]).add(ent.mins[1]).add(ent.mins[2])
             * .add(ent.maxs[0]).add(ent.maxs[1]).add(ent.maxs[2])); }
             * sleep(10); }
             */

                var edit: EdictIterator? = null

                while ((edit = GameBase.G_Find(edit, GameBase.findByTarget, self.target)) != null) {
                    target = edit!!.o
                    if (Lib.strcmp(target!!.classname, "point_combat") == 0) {
                        self.combattarget = self.target
                        fixup = true
                    } else {
                        notcombat = true
                    }
                }
                if (notcombat && self.combattarget != null)
                    GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " has target with mixed types\n")
                if (fixup)
                    self.target = null
            }

            // validate combattarget
            if (self.combattarget != null) {
                var target: edict_t? = null

                var edit: EdictIterator? = null
                while ((edit = GameBase.G_Find(edit, GameBase.findByTarget, self.combattarget)) != null) {
                    target = edit!!.o

                    if (Lib.strcmp(target!!.classname, "point_combat") != 0) {
                        GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " has bad combattarget " + self.combattarget + " : " + target!!.classname + " at " + Lib.vtos(target!!.s.origin))
                    }
                }
            }

            if (self.target != null) {
                self.goalentity = self.movetarget = GameBase.G_PickTarget(self.target)
                if (null == self.movetarget) {
                    GameBase.gi.dprintf(self.classname + " can't find target " + self.target + " at " + Lib.vtos(self.s.origin) + "\n")
                    self.target = null
                    self.monsterinfo.pausetime = 100000000
                    self.monsterinfo.stand.think(self)
                } else if (Lib.strcmp(self.movetarget.classname, "path_corner") == 0) {
                    Math3D.VectorSubtract(self.goalentity.s.origin, self.s.origin, v)
                    self.ideal_yaw = self.s.angles[Defines.YAW] = Math3D.vectoyaw(v)
                    self.monsterinfo.walk.think(self)
                    self.target = null
                } else {
                    self.goalentity = self.movetarget = null
                    self.monsterinfo.pausetime = 100000000
                    self.monsterinfo.stand.think(self)
                }
            } else {
                self.monsterinfo.pausetime = 100000000
                self.monsterinfo.stand.think(self)
            }

            self.think = Monster.monster_think
            self.nextthink = GameBase.level.time + Defines.FRAMETIME
        }

        public var monster_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "monster_think"
            }

            public fun think(self: edict_t): Boolean {

                M.M_MoveFrame(self)
                if (self.linkcount != self.monsterinfo.linkcount) {
                    self.monsterinfo.linkcount = self.linkcount
                    M.M_CheckGround(self)
                }
                M.M_CatagorizePosition(self)
                M.M_WorldEffects(self)
                M.M_SetEffects(self)
                return true
            }
        }

        public var monster_triggered_spawn: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "monster_trigger_spawn"
            }

            public fun think(self: edict_t): Boolean {

                self.s.origin[2] += 1
                GameUtil.KillBox(self)

                self.solid = Defines.SOLID_BBOX
                self.movetype = Defines.MOVETYPE_STEP
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.air_finished = GameBase.level.time + 12
                GameBase.gi.linkentity(self)

                Monster.monster_start_go(self)

                if (self.enemy != null && 0 == (self.spawnflags and 1) && 0 == (self.enemy.flags and Defines.FL_NOTARGET)) {
                    GameUtil.FoundTarget(self)
                } else {
                    self.enemy = null
                }
                return true
            }
        }

        //	we have a one frame delay here so we don't telefrag the guy who activated
        // us
        public var monster_triggered_spawn_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "monster_trigger_spawn_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.think = monster_triggered_spawn
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
                if (activator.client != null)
                    self.enemy = activator
                self.use = GameUtil.monster_use
            }
        }

        public var monster_triggered_start: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "monster_triggered_start"
            }

            public fun think(self: edict_t): Boolean {
                if (self.index == 312)
                    Com.Printf("monster_triggered_start\n")
                self.solid = Defines.SOLID_NOT
                self.movetype = Defines.MOVETYPE_NONE
                self.svflags = self.svflags or Defines.SVF_NOCLIENT
                self.nextthink = 0
                self.use = monster_triggered_spawn_use
                return true
            }
        }
    }
}