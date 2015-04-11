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
import lwjake2.client.M
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.util.Calendar

public class GameMisc {
    companion object {
        public fun SP_path_corner(self: edict_t) {
            if (self.targetname == null) {
                GameBase.gi.dprintf("path_corner with no targetname at " + Lib.vtos(self.s.origin) + "\n")
                GameUtil.G_FreeEdict(self)
                return
            }

            self.solid = Defines.SOLID_TRIGGER
            self.touch = path_corner_touch
            Math3D.VectorSet(self.mins, -8, -8, -8)
            Math3D.VectorSet(self.maxs, 8, 8, 8)
            self.svflags = self.svflags or Defines.SVF_NOCLIENT
            GameBase.gi.linkentity(self)
        }

        public fun SP_point_combat(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }
            self.solid = Defines.SOLID_TRIGGER
            self.touch = point_combat_touch
            Math3D.VectorSet(self.mins, -8, -8, -16)
            Math3D.VectorSet(self.maxs, 8, 8, 16)
            self.svflags = Defines.SVF_NOCLIENT
            GameBase.gi.linkentity(self)
        }

        public fun SP_viewthing(ent: edict_t) {
            GameBase.gi.dprintf("viewthing spawned\n")

            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            ent.s.renderfx = Defines.RF_FRAMELERP
            Math3D.VectorSet(ent.mins, -16, -16, -24)
            Math3D.VectorSet(ent.maxs, 16, 16, 32)
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/banner/tris.md2")
            GameBase.gi.linkentity(ent)
            ent.nextthink = GameBase.level.time + 0.5.toFloat()
            ent.think = TH_viewthing
            return
        }

        /*
     * QUAKED info_null (0 0.5 0) (-4 -4 -4) (4 4 4) Used as a positional target
     * for spotlights, etc.
     */
        public fun SP_info_null(self: edict_t) {
            GameUtil.G_FreeEdict(self)
        }

        /*
     * QUAKED info_notnull (0 0.5 0) (-4 -4 -4) (4 4 4) Used as a positional
     * target for lightning.
     */
        public fun SP_info_notnull(self: edict_t) {
            Math3D.VectorCopy(self.s.origin, self.absmin)
            Math3D.VectorCopy(self.s.origin, self.absmax)
        }

        public fun SP_light(self: edict_t) {
            // no targeted lights in deathmatch, because they cause global messages
            if (null == self.targetname || GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            if (self.style >= 32) {
                self.use = light_use
                if ((self.spawnflags and START_OFF) != 0)
                    GameBase.gi.configstring(Defines.CS_LIGHTS + self.style, "a")
                else
                    GameBase.gi.configstring(Defines.CS_LIGHTS + self.style, "m")
            }
        }

        public fun SP_func_wall(self: edict_t) {
            self.movetype = Defines.MOVETYPE_PUSH
            GameBase.gi.setmodel(self, self.model)

            if ((self.spawnflags and 8) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALL
            if ((self.spawnflags and 16) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALLFAST

            // just a wall
            if ((self.spawnflags and 7) == 0) {
                self.solid = Defines.SOLID_BSP
                GameBase.gi.linkentity(self)
                return
            }

            // it must be TRIGGER_SPAWN
            if (0 == (self.spawnflags and 1)) {
                GameBase.gi.dprintf("func_wall missing TRIGGER_SPAWN\n")
                self.spawnflags = self.spawnflags or 1
            }

            // yell if the spawnflags are odd
            if ((self.spawnflags and 4) != 0) {
                if (0 == (self.spawnflags and 2)) {
                    GameBase.gi.dprintf("func_wall START_ON without TOGGLE\n")
                    self.spawnflags = self.spawnflags or 2
                }
            }

            self.use = func_wall_use
            if ((self.spawnflags and 4) != 0) {
                self.solid = Defines.SOLID_BSP
            } else {
                self.solid = Defines.SOLID_NOT
                self.svflags = self.svflags or Defines.SVF_NOCLIENT
            }
            GameBase.gi.linkentity(self)
        }

        public fun SP_func_object(self: edict_t) {
            GameBase.gi.setmodel(self, self.model)

            self.mins[0] += 1
            self.mins[1] += 1
            self.mins[2] += 1
            self.maxs[0] -= 1
            self.maxs[1] -= 1
            self.maxs[2] -= 1

            if (self.dmg == 0)
                self.dmg = 100

            if (self.spawnflags == 0) {
                self.solid = Defines.SOLID_BSP
                self.movetype = Defines.MOVETYPE_PUSH
                self.think = func_object_release
                self.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            } else {
                self.solid = Defines.SOLID_NOT
                self.movetype = Defines.MOVETYPE_PUSH
                self.use = func_object_use
                self.svflags = self.svflags or Defines.SVF_NOCLIENT
            }

            if ((self.spawnflags and 2) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALL
            if ((self.spawnflags and 4) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALLFAST

            self.clipmask = Defines.MASK_MONSTERSOLID

            GameBase.gi.linkentity(self)
        }

        public fun SP_func_explosive(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                // auto-remove for deathmatch
                GameUtil.G_FreeEdict(self)
                return
            }

            self.movetype = Defines.MOVETYPE_PUSH

            GameBase.gi.modelindex("models/objects/debris1/tris.md2")
            GameBase.gi.modelindex("models/objects/debris2/tris.md2")

            GameBase.gi.setmodel(self, self.model)

            if ((self.spawnflags and 1) != 0) {
                self.svflags = self.svflags or Defines.SVF_NOCLIENT
                self.solid = Defines.SOLID_NOT
                self.use = func_explosive_spawn
            } else {
                self.solid = Defines.SOLID_BSP
                if (self.targetname != null)
                    self.use = func_explosive_use
            }

            if ((self.spawnflags and 2) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALL
            if ((self.spawnflags and 4) != 0)
                self.s.effects = self.s.effects or Defines.EF_ANIM_ALLFAST

            if (self.use != func_explosive_use) {
                if (self.health == 0)
                    self.health = 100
                self.die = func_explosive_explode
                self.takedamage = Defines.DAMAGE_YES
            }

            GameBase.gi.linkentity(self)
        }

        public fun SP_misc_explobox(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                // auto-remove for deathmatch
                GameUtil.G_FreeEdict(self)
                return
            }

            GameBase.gi.modelindex("models/objects/debris1/tris.md2")
            GameBase.gi.modelindex("models/objects/debris2/tris.md2")
            GameBase.gi.modelindex("models/objects/debris3/tris.md2")

            self.solid = Defines.SOLID_BBOX
            self.movetype = Defines.MOVETYPE_STEP

            self.model = "models/objects/barrels/tris.md2"
            self.s.modelindex = GameBase.gi.modelindex(self.model)
            Math3D.VectorSet(self.mins, -16, -16, 0)
            Math3D.VectorSet(self.maxs, 16, 16, 40)

            if (self.mass == 0)
                self.mass = 400
            if (0 == self.health)
                self.health = 10
            if (0 == self.dmg)
                self.dmg = 150

            self.die = barrel_delay
            self.takedamage = Defines.DAMAGE_YES
            self.monsterinfo.aiflags = Defines.AI_NOSTEP

            self.touch = barrel_touch

            self.think = M.M_droptofloor
            self.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME

            GameBase.gi.linkentity(self)
        }

        public fun SP_misc_blackhole(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_NOT
            Math3D.VectorSet(ent.mins, -64, -64, 0)
            Math3D.VectorSet(ent.maxs, 64, 64, 8)
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/black/tris.md2")
            ent.s.renderfx = Defines.RF_TRANSLUCENT
            ent.use = misc_blackhole_use
            ent.think = misc_blackhole_think
            ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_eastertank(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            Math3D.VectorSet(ent.mins, -32, -32, -16)
            Math3D.VectorSet(ent.maxs, 32, 32, 32)
            ent.s.modelindex = GameBase.gi.modelindex("models/monsters/tank/tris.md2")
            ent.s.frame = 254
            ent.think = misc_eastertank_think
            ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_easterchick(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            Math3D.VectorSet(ent.mins, -32, -32, 0)
            Math3D.VectorSet(ent.maxs, 32, 32, 32)
            ent.s.modelindex = GameBase.gi.modelindex("models/monsters/bitch/tris.md2")
            ent.s.frame = 208
            ent.think = misc_easterchick_think
            ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_easterchick2(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            Math3D.VectorSet(ent.mins, -32, -32, 0)
            Math3D.VectorSet(ent.maxs, 32, 32, 32)
            ent.s.modelindex = GameBase.gi.modelindex("models/monsters/bitch/tris.md2")
            ent.s.frame = 248
            ent.think = misc_easterchick2_think
            ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            GameBase.gi.linkentity(ent)
        }

        public fun SP_monster_commander_body(self: edict_t) {
            self.movetype = Defines.MOVETYPE_NONE
            self.solid = Defines.SOLID_BBOX
            self.model = "models/monsters/commandr/tris.md2"
            self.s.modelindex = GameBase.gi.modelindex(self.model)
            Math3D.VectorSet(self.mins, -32, -32, 0)
            Math3D.VectorSet(self.maxs, 32, 32, 48)
            self.use = commander_body_use
            self.takedamage = Defines.DAMAGE_YES
            self.flags = Defines.FL_GODMODE
            self.s.renderfx = self.s.renderfx or Defines.RF_FRAMELERP
            GameBase.gi.linkentity(self)

            GameBase.gi.soundindex("tank/thud.wav")
            GameBase.gi.soundindex("tank/pain.wav")

            self.think = commander_body_drop
            self.nextthink = GameBase.level.time + 5 * Defines.FRAMETIME
        }

        public fun SP_misc_banner(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_NOT
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/banner/tris.md2")
            ent.s.frame = Lib.rand() % 16
            GameBase.gi.linkentity(ent)

            ent.think = misc_banner_think
            ent.nextthink = GameBase.level.time + Defines.FRAMETIME
        }

        public fun SP_misc_deadsoldier(ent: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                // auto-remove for deathmatch
                GameUtil.G_FreeEdict(ent)
                return
            }

            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            ent.s.modelindex = GameBase.gi.modelindex("models/deadbods/dude/tris.md2")

            // Defaults to frame 0
            if ((ent.spawnflags and 2) != 0)
                ent.s.frame = 1
            else if ((ent.spawnflags and 4) != 0)
                ent.s.frame = 2
            else if ((ent.spawnflags and 8) != 0)
                ent.s.frame = 3
            else if ((ent.spawnflags and 16) != 0)
                ent.s.frame = 4
            else if ((ent.spawnflags and 32) != 0)
                ent.s.frame = 5
            else
                ent.s.frame = 0

            Math3D.VectorSet(ent.mins, -16, -16, 0)
            Math3D.VectorSet(ent.maxs, 16, 16, 16)
            ent.deadflag = Defines.DEAD_DEAD
            ent.takedamage = Defines.DAMAGE_YES
            ent.svflags = ent.svflags or (Defines.SVF_MONSTER or Defines.SVF_DEADMONSTER)
            ent.die = misc_deadsoldier_die
            ent.monsterinfo.aiflags = ent.monsterinfo.aiflags or Defines.AI_GOOD_GUY

            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_viper(ent: edict_t) {
            if (null == ent.target) {
                GameBase.gi.dprintf("misc_viper without a target at " + Lib.vtos(ent.absmin) + "\n")
                GameUtil.G_FreeEdict(ent)
                return
            }

            if (0 == ent.speed)
                ent.speed = 300

            ent.movetype = Defines.MOVETYPE_PUSH
            ent.solid = Defines.SOLID_NOT
            ent.s.modelindex = GameBase.gi.modelindex("models/ships/viper/tris.md2")
            Math3D.VectorSet(ent.mins, -16, -16, 0)
            Math3D.VectorSet(ent.maxs, 16, 16, 32)

            ent.think = GameFunc.func_train_find
            ent.nextthink = GameBase.level.time + Defines.FRAMETIME
            ent.use = misc_viper_use
            ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
            ent.moveinfo.accel = ent.moveinfo.decel = ent.moveinfo.speed = ent.speed

            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED misc_bigviper (1 .5 0) (-176 -120 -24) (176 120 72) This is a
     * large stationary viper as seen in Paul's intro
     */
        public fun SP_misc_bigviper(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            Math3D.VectorSet(ent.mins, -176, -120, -24)
            Math3D.VectorSet(ent.maxs, 176, 120, 72)
            ent.s.modelindex = GameBase.gi.modelindex("models/ships/bigviper/tris.md2")
            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_viper_bomb(self: edict_t) {
            self.movetype = Defines.MOVETYPE_NONE
            self.solid = Defines.SOLID_NOT
            Math3D.VectorSet(self.mins, -8, -8, -8)
            Math3D.VectorSet(self.maxs, 8, 8, 8)

            self.s.modelindex = GameBase.gi.modelindex("models/objects/bomb/tris.md2")

            if (self.dmg == 0)
                self.dmg = 1000

            self.use = misc_viper_bomb_use
            self.svflags = self.svflags or Defines.SVF_NOCLIENT

            GameBase.gi.linkentity(self)
        }

        public fun SP_misc_strogg_ship(ent: edict_t) {
            if (null == ent.target) {
                GameBase.gi.dprintf(ent.classname + " without a target at " + Lib.vtos(ent.absmin) + "\n")
                GameUtil.G_FreeEdict(ent)
                return
            }

            if (0 == ent.speed)
                ent.speed = 300

            ent.movetype = Defines.MOVETYPE_PUSH
            ent.solid = Defines.SOLID_NOT
            ent.s.modelindex = GameBase.gi.modelindex("models/ships/strogg1/tris.md2")
            Math3D.VectorSet(ent.mins, -16, -16, 0)
            Math3D.VectorSet(ent.maxs, 16, 16, 32)

            ent.think = GameFunc.func_train_find
            ent.nextthink = GameBase.level.time + Defines.FRAMETIME
            ent.use = misc_strogg_ship_use
            ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
            ent.moveinfo.accel = ent.moveinfo.decel = ent.moveinfo.speed = ent.speed

            GameBase.gi.linkentity(ent)
        }

        public fun SP_misc_satellite_dish(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            Math3D.VectorSet(ent.mins, -64, -64, 0)
            Math3D.VectorSet(ent.maxs, 64, 64, 128)
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/satellite/tris.md2")
            ent.use = misc_satellite_dish_use
            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED light_mine1 (0 1 0) (-2 -2 -12) (2 2 12)
     */
        public fun SP_light_mine1(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/minelite/light1/tris.md2")
            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED light_mine2 (0 1 0) (-2 -2 -12) (2 2 12)
     */
        public fun SP_light_mine2(ent: edict_t) {
            ent.movetype = Defines.MOVETYPE_NONE
            ent.solid = Defines.SOLID_BBOX
            ent.s.modelindex = GameBase.gi.modelindex("models/objects/minelite/light2/tris.md2")
            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED misc_gib_arm (1 0 0) (-8 -8 -8) (8 8 8) Intended for use with the
     * target_spawner
     */
        public fun SP_misc_gib_arm(ent: edict_t) {
            GameBase.gi.setmodel(ent, "models/objects/gibs/arm/tris.md2")
            ent.solid = Defines.SOLID_NOT
            ent.s.effects = ent.s.effects or Defines.EF_GIB
            ent.takedamage = Defines.DAMAGE_YES
            ent.die = gib_die
            ent.movetype = Defines.MOVETYPE_TOSS
            ent.svflags = ent.svflags or Defines.SVF_MONSTER
            ent.deadflag = Defines.DEAD_DEAD
            ent.avelocity[0] = Lib.random() * 200
            ent.avelocity[1] = Lib.random() * 200
            ent.avelocity[2] = Lib.random() * 200
            ent.think = GameUtil.G_FreeEdictA
            ent.nextthink = GameBase.level.time + 30
            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED misc_gib_leg (1 0 0) (-8 -8 -8) (8 8 8) Intended for use with the
     * target_spawner
     */
        public fun SP_misc_gib_leg(ent: edict_t) {
            GameBase.gi.setmodel(ent, "models/objects/gibs/leg/tris.md2")
            ent.solid = Defines.SOLID_NOT
            ent.s.effects = ent.s.effects or Defines.EF_GIB
            ent.takedamage = Defines.DAMAGE_YES
            ent.die = gib_die
            ent.movetype = Defines.MOVETYPE_TOSS
            ent.svflags = ent.svflags or Defines.SVF_MONSTER
            ent.deadflag = Defines.DEAD_DEAD
            ent.avelocity[0] = Lib.random() * 200
            ent.avelocity[1] = Lib.random() * 200
            ent.avelocity[2] = Lib.random() * 200
            ent.think = GameUtil.G_FreeEdictA
            ent.nextthink = GameBase.level.time + 30
            GameBase.gi.linkentity(ent)
        }

        /*
     * QUAKED misc_gib_head (1 0 0) (-8 -8 -8) (8 8 8) Intended for use with the
     * target_spawner
     */
        public fun SP_misc_gib_head(ent: edict_t) {
            GameBase.gi.setmodel(ent, "models/objects/gibs/head/tris.md2")
            ent.solid = Defines.SOLID_NOT
            ent.s.effects = ent.s.effects or Defines.EF_GIB
            ent.takedamage = Defines.DAMAGE_YES
            ent.die = gib_die
            ent.movetype = Defines.MOVETYPE_TOSS
            ent.svflags = ent.svflags or Defines.SVF_MONSTER
            ent.deadflag = Defines.DEAD_DEAD
            ent.avelocity[0] = Lib.random() * 200
            ent.avelocity[1] = Lib.random() * 200
            ent.avelocity[2] = Lib.random() * 200
            ent.think = GameUtil.G_FreeEdictA
            ent.nextthink = GameBase.level.time + 30
            GameBase.gi.linkentity(ent)
        }

        //=====================================================

        /*
     * QUAKED target_character (0 0 1) ? used with target_string (must be on
     * same "team") "count" is position in the string (starts at 1)
     */

        public fun SP_target_character(self: edict_t) {
            self.movetype = Defines.MOVETYPE_PUSH
            GameBase.gi.setmodel(self, self.model)
            self.solid = Defines.SOLID_BSP
            self.s.frame = 12
            GameBase.gi.linkentity(self)
            return
        }

        public fun SP_target_string(self: edict_t) {
            if (self.message == null)
                self.message = ""
            self.use = target_string_use
        }

        // don't let field width of any clock messages change, or it
        // could cause an overwrite after a game load

        public fun func_clock_reset(self: edict_t) {
            self.activator = null
            if ((self.spawnflags and 1) != 0) {
                self.health = 0
                self.wait = self.count
            } else if ((self.spawnflags and 2) != 0) {
                self.health = self.count
                self.wait = 0
            }
        }

        public fun func_clock_format_countdown(self: edict_t) {
            if (self.style == 0) {
                self.message = "" + self.health
                //Com_sprintf(self.message, CLOCK_MESSAGE_SIZE, "%2i",
                // self.health);
                return
            }

            if (self.style == 1) {
                self.message = "" + self.health / 60 + ":" + self.health % 60
                //Com_sprintf(self.message, CLOCK_MESSAGE_SIZE, "%2i:%2i",
                // self.health / 60, self.health % 60);
                /*
             * if (self.message.charAt(3) == ' ') self.message.charAt(3) = '0';
             */
                return
            }

            if (self.style == 2) {
                self.message = "" + self.health / 3600 + ":" + (self.health - (self.health / 3600) * 3600) / 60 + ":" + self.health % 60
                /*
             * Com_sprintf( self.message, CLOCK_MESSAGE_SIZE, "%2i:%2i:%2i",
             * self.health / 3600, (self.health - (self.health / 3600) * 3600) /
             * 60, self.health % 60); if (self.message[3] == ' ')
             * self.message[3] = '0'; if (self.message[6] == ' ')
             * self.message[6] = '0';
             */
                return
            }
        }

        public fun SP_func_clock(self: edict_t) {
            if (self.target == null) {
                GameBase.gi.dprintf(self.classname + " with no target at " + Lib.vtos(self.s.origin) + "\n")
                GameUtil.G_FreeEdict(self)
                return
            }

            if ((self.spawnflags and 2) != 0 && (0 == self.count)) {
                GameBase.gi.dprintf(self.classname + " with no count at " + Lib.vtos(self.s.origin) + "\n")
                GameUtil.G_FreeEdict(self)
                return
            }

            if ((self.spawnflags and 1) != 0 && (0 == self.count))
                self.count = 60 * 60

            func_clock_reset(self)

            self.message = String()

            self.think = func_clock_think

            if ((self.spawnflags and 4) != 0)
                self.use = func_clock_use
            else
                self.nextthink = GameBase.level.time + 1
        }

        /**
         * QUAKED misc_teleporter (1 0 0) (-32 -32 -24) (32 32 -16) Stepping onto
         * this disc will teleport players to the targeted misc_teleporter_dest
         * object.
         */
        public fun SP_misc_teleporter(ent: edict_t) {
            val trig: edict_t

            if (ent.target == null) {
                GameBase.gi.dprintf("teleporter without a target.\n")
                GameUtil.G_FreeEdict(ent)
                return
            }

            GameBase.gi.setmodel(ent, "models/objects/dmspot/tris.md2")
            ent.s.skinnum = 1
            ent.s.effects = Defines.EF_TELEPORTER
            ent.s.sound = GameBase.gi.soundindex("world/amb10.wav")
            ent.solid = Defines.SOLID_BBOX

            Math3D.VectorSet(ent.mins, -32, -32, -24)
            Math3D.VectorSet(ent.maxs, 32, 32, -16)
            GameBase.gi.linkentity(ent)

            trig = GameUtil.G_Spawn()
            trig.touch = teleporter_touch
            trig.solid = Defines.SOLID_TRIGGER
            trig.target = ent.target
            trig.owner = ent
            Math3D.VectorCopy(ent.s.origin, trig.s.origin)
            Math3D.VectorSet(trig.mins, -8, -8, 8)
            Math3D.VectorSet(trig.maxs, 8, 8, 24)
            GameBase.gi.linkentity(trig)
        }

        /**
         * QUAKED func_group (0 0 0) ? Used to group brushes together just for
         * editor convenience.
         */

        public fun VelocityForDamage(damage: Int, v: FloatArray) {
            v[0] = 100.0.toFloat() * Lib.crandom()
            v[1] = 100.0.toFloat() * Lib.crandom()
            v[2] = 200.0.toFloat() + 100.0.toFloat() * Lib.random()

            if (damage < 50)
                Math3D.VectorScale(v, 0.7.toFloat(), v)
            else
                Math3D.VectorScale(v, 1.2.toFloat(), v)
        }

        public fun BecomeExplosion1(self: edict_t) {
            GameBase.gi.WriteByte(Defines.svc_temp_entity)
            GameBase.gi.WriteByte(Defines.TE_EXPLOSION1)
            GameBase.gi.WritePosition(self.s.origin)
            GameBase.gi.multicast(self.s.origin, Defines.MULTICAST_PVS)

            GameUtil.G_FreeEdict(self)
        }

        public fun BecomeExplosion2(self: edict_t) {
            GameBase.gi.WriteByte(Defines.svc_temp_entity)
            GameBase.gi.WriteByte(Defines.TE_EXPLOSION2)
            GameBase.gi.WritePosition(self.s.origin)
            GameBase.gi.multicast(self.s.origin, Defines.MULTICAST_PVS)

            GameUtil.G_FreeEdict(self)
        }

        public fun ThrowGib(self: edict_t, gibname: String, damage: Int, type: Int) {
            val gib: edict_t

            val vd = floatArray(0.0, 0.0, 0.0)
            val origin = floatArray(0.0, 0.0, 0.0)
            val size = floatArray(0.0, 0.0, 0.0)
            val vscale: Float

            gib = GameUtil.G_Spawn()

            Math3D.VectorScale(self.size, 0.5.toFloat(), size)
            Math3D.VectorAdd(self.absmin, size, origin)
            gib.s.origin[0] = origin[0] + Lib.crandom() * size[0]
            gib.s.origin[1] = origin[1] + Lib.crandom() * size[1]
            gib.s.origin[2] = origin[2] + Lib.crandom() * size[2]

            GameBase.gi.setmodel(gib, gibname)
            gib.solid = Defines.SOLID_NOT
            gib.s.effects = gib.s.effects or Defines.EF_GIB
            gib.flags = gib.flags or Defines.FL_NO_KNOCKBACK
            gib.takedamage = Defines.DAMAGE_YES
            gib.die = gib_die

            if (type == Defines.GIB_ORGANIC) {
                gib.movetype = Defines.MOVETYPE_TOSS
                gib.touch = gib_touch
                vscale = 0.5.toFloat()
            } else {
                gib.movetype = Defines.MOVETYPE_BOUNCE
                vscale = 1.0.toFloat()
            }

            VelocityForDamage(damage, vd)
            Math3D.VectorMA(self.velocity, vscale, vd, gib.velocity)
            ClipGibVelocity(gib)
            gib.avelocity[0] = Lib.random() * 600
            gib.avelocity[1] = Lib.random() * 600
            gib.avelocity[2] = Lib.random() * 600

            gib.think = GameUtil.G_FreeEdictA
            gib.nextthink = GameBase.level.time + 10 + Lib.random() * 10

            GameBase.gi.linkentity(gib)
        }

        public fun ThrowHead(self: edict_t, gibname: String, damage: Int, type: Int) {
            val vd = floatArray(0.0, 0.0, 0.0)

            val vscale: Float

            self.s.skinnum = 0
            self.s.frame = 0
            Math3D.VectorClear(self.mins)
            Math3D.VectorClear(self.maxs)

            self.s.modelindex2 = 0
            GameBase.gi.setmodel(self, gibname)
            self.solid = Defines.SOLID_NOT
            self.s.effects = self.s.effects or Defines.EF_GIB
            self.s.effects = self.s.effects and Defines.EF_FLIES.inv()
            self.s.sound = 0
            self.flags = self.flags or Defines.FL_NO_KNOCKBACK
            self.svflags = self.svflags and Defines.SVF_MONSTER.inv()
            self.takedamage = Defines.DAMAGE_YES
            self.die = gib_die

            if (type == Defines.GIB_ORGANIC) {
                self.movetype = Defines.MOVETYPE_TOSS
                self.touch = gib_touch
                vscale = 0.5.toFloat()
            } else {
                self.movetype = Defines.MOVETYPE_BOUNCE
                vscale = 1.0.toFloat()
            }

            VelocityForDamage(damage, vd)
            Math3D.VectorMA(self.velocity, vscale, vd, self.velocity)
            ClipGibVelocity(self)

            self.avelocity[Defines.YAW] = Lib.crandom() * 600.toFloat()

            self.think = GameUtil.G_FreeEdictA
            self.nextthink = GameBase.level.time + 10 + Lib.random() * 10

            GameBase.gi.linkentity(self)
        }

        public fun ThrowClientHead(self: edict_t, damage: Int) {
            val vd = floatArray(0.0, 0.0, 0.0)
            val gibname: String

            if ((Lib.rand() and 1) != 0) {
                gibname = "models/objects/gibs/head2/tris.md2"
                self.s.skinnum = 1 // second skin is player
            } else {
                gibname = "models/objects/gibs/skull/tris.md2"
                self.s.skinnum = 0
            }

            self.s.origin[2] += 32
            self.s.frame = 0
            GameBase.gi.setmodel(self, gibname)
            Math3D.VectorSet(self.mins, -16, -16, 0)
            Math3D.VectorSet(self.maxs, 16, 16, 16)

            self.takedamage = Defines.DAMAGE_NO
            self.solid = Defines.SOLID_NOT
            self.s.effects = Defines.EF_GIB
            self.s.sound = 0
            self.flags = self.flags or Defines.FL_NO_KNOCKBACK

            self.movetype = Defines.MOVETYPE_BOUNCE
            VelocityForDamage(damage, vd)
            Math3D.VectorAdd(self.velocity, vd, self.velocity)

            if (self.client != null)
            // bodies in the queue don't have a client anymore
            {
                self.client.anim_priority = Defines.ANIM_DEATH
                self.client.anim_end = self.s.frame
            } else {
                self.think = null
                self.nextthink = 0
            }

            GameBase.gi.linkentity(self)
        }

        public fun ThrowDebris(self: edict_t, modelname: String, speed: Float, origin: FloatArray) {
            val chunk: edict_t
            val v = floatArray(0.0, 0.0, 0.0)

            chunk = GameUtil.G_Spawn()
            Math3D.VectorCopy(origin, chunk.s.origin)
            GameBase.gi.setmodel(chunk, modelname)
            v[0] = 100 * Lib.crandom()
            v[1] = 100 * Lib.crandom()
            v[2] = 100 + 100 * Lib.crandom()
            Math3D.VectorMA(self.velocity, speed, v, chunk.velocity)
            chunk.movetype = Defines.MOVETYPE_BOUNCE
            chunk.solid = Defines.SOLID_NOT
            chunk.avelocity[0] = Lib.random() * 600
            chunk.avelocity[1] = Lib.random() * 600
            chunk.avelocity[2] = Lib.random() * 600
            chunk.think = GameUtil.G_FreeEdictA
            chunk.nextthink = GameBase.level.time + 5 + Lib.random() * 5
            chunk.s.frame = 0
            chunk.flags = 0
            chunk.classname = "debris"
            chunk.takedamage = Defines.DAMAGE_YES
            chunk.die = debris_die
            GameBase.gi.linkentity(chunk)
        }

        public fun ClipGibVelocity(ent: edict_t) {
            if (ent.velocity[0] < -300)
                ent.velocity[0] = -300
            else if (ent.velocity[0] > 300)
                ent.velocity[0] = 300
            if (ent.velocity[1] < -300)
                ent.velocity[1] = -300
            else if (ent.velocity[1] > 300)
                ent.velocity[1] = 300
            if (ent.velocity[2] < 200)
                ent.velocity[2] = 200 // always some upwards
            else if (ent.velocity[2] > 500)
                ent.velocity[2] = 500
        }

        public var Use_Areaportal: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "use_areaportal"
            }

            public fun use(ent: edict_t, other: edict_t, activator: edict_t) {
                ent.count = ent.count xor 1 // toggle state
                //	gi.dprintf ("portalstate: %i = %i\n", ent.style, ent.count);
                GameBase.gi.SetAreaPortalState(ent.style, ent.count != 0)
            }
        }

        /**
         * QUAKED func_areaportal (0 0 0) ?

         * This is a non-visible object that divides the world into areas that are
         * seperated when this portal is not activated. Usually enclosed in the
         * middle of a door.
         */

        var SP_func_areaportal: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "sp_func_areaportal"
            }

            public fun think(ent: edict_t): Boolean {
                ent.use = Use_Areaportal
                ent.count = 0 // always start closed;
                return true
            }
        }

        /**
         * QUAKED path_corner (.5 .3 0) (-8 -8 -8) (8 8 8) TELEPORT Target: next
         * path corner Pathtarget: gets used when an entity that has this
         * path_corner targeted touches it
         */
        public var path_corner_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "path_corner_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val v = floatArray(0.0, 0.0, 0.0)
                var next: edict_t?

                if (other.movetarget != self)
                    return

                if (other.enemy != null)
                    return

                if (self.pathtarget != null) {
                    val savetarget: String

                    savetarget = self.target
                    self.target = self.pathtarget
                    GameUtil.G_UseTargets(self, other)
                    self.target = savetarget
                }

                if (self.target != null)
                    next = GameBase.G_PickTarget(self.target)
                else
                    next = null

                if ((next != null) && (next!!.spawnflags and 1) != 0) {
                    Math3D.VectorCopy(next!!.s.origin, v)
                    v[2] += next!!.mins[2]
                    v[2] -= other.mins[2]
                    Math3D.VectorCopy(v, other.s.origin)
                    next = GameBase.G_PickTarget(next!!.target)
                    other.s.event = Defines.EV_OTHER_TELEPORT
                }

                other.goalentity = other.movetarget = next

                if (self.wait != 0) {
                    other.monsterinfo.pausetime = GameBase.level.time + self.wait
                    other.monsterinfo.stand.think(other)
                    return
                }

                if (other.movetarget == null) {
                    other.monsterinfo.pausetime = GameBase.level.time + 100000000
                    other.monsterinfo.stand.think(other)
                } else {
                    Math3D.VectorSubtract(other.goalentity.s.origin, other.s.origin, v)
                    other.ideal_yaw = Math3D.vectoyaw(v)
                }
            }
        }

        /*
     * QUAKED point_combat (0.5 0.3 0) (-8 -8 -8) (8 8 8) Hold Makes this the
     * target of a monster and it will head here when first activated before
     * going after the activator. If hold is selected, it will stay here.
     */
        public var point_combat_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "point_combat_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val activator: edict_t

                if (other.movetarget != self)
                    return

                if (self.target != null) {
                    other.target = self.target
                    other.goalentity = other.movetarget = GameBase.G_PickTarget(other.target)
                    if (null == other.goalentity) {
                        GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " target " + self.target + " does not exist\n")
                        other.movetarget = self
                    }
                    self.target = null
                } else if ((self.spawnflags and 1) != 0 && 0 == (other.flags and (Defines.FL_SWIM or Defines.FL_FLY))) {
                    other.monsterinfo.pausetime = GameBase.level.time + 100000000
                    other.monsterinfo.aiflags = other.monsterinfo.aiflags or Defines.AI_STAND_GROUND
                    other.monsterinfo.stand.think(other)
                }

                if (other.movetarget == self) {
                    other.target = null
                    other.movetarget = null
                    other.goalentity = other.enemy
                    other.monsterinfo.aiflags = other.monsterinfo.aiflags and Defines.AI_COMBAT_POINT.inv()
                }

                if (self.pathtarget != null) {
                    val savetarget: String

                    savetarget = self.target
                    self.target = self.pathtarget
                    if (other.enemy != null && other.enemy.client != null)
                        activator = other.enemy
                    else if (other.oldenemy != null && other.oldenemy.client != null)
                        activator = other.oldenemy
                    else if (other.activator != null && other.activator.client != null)
                        activator = other.activator
                    else
                        activator = other
                    GameUtil.G_UseTargets(self, activator)
                    self.target = savetarget
                }
            }
        }

        /*
     * QUAKED viewthing (0 .5 .8) (-8 -8 -8) (8 8 8) Just for the debugging
     * level. Don't use
     */
        public var TH_viewthing: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "th_viewthing"
            }

            public fun think(ent: edict_t): Boolean {
                ent.s.frame = (ent.s.frame + 1) % 7
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        /*
     * QUAKED light (0 1 0) (-8 -8 -8) (8 8 8) START_OFF Non-displayed light.
     * Default light value is 300. Default style is 0. If targeted, will toggle
     * between on and off. Default _cone value is 10 (used to set size of light
     * for spotlights)
     */

        public val START_OFF: Int = 1

        public var light_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "light_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if ((self.spawnflags and START_OFF) != 0) {
                    GameBase.gi.configstring(Defines.CS_LIGHTS + self.style, "m")
                    self.spawnflags = self.spawnflags and START_OFF.inv()
                } else {
                    GameBase.gi.configstring(Defines.CS_LIGHTS + self.style, "a")
                    self.spawnflags = self.spawnflags or START_OFF
                }
            }
        }

        /*
     * QUAKED func_wall (0 .5 .8) ? TRIGGER_SPAWN TOGGLE START_ON ANIMATED
     * ANIMATED_FAST This is just a solid wall if not inhibited
     * 
     * TRIGGER_SPAWN the wall will not be present until triggered it will then
     * blink in to existance; it will kill anything that was in it's way
     * 
     * TOGGLE only valid for TRIGGER_SPAWN walls this allows the wall to be
     * turned on and off
     * 
     * START_ON only valid for TRIGGER_SPAWN walls the wall will initially be
     * present
     */

        var func_wall_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_wall_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if (self.solid == Defines.SOLID_NOT) {
                    self.solid = Defines.SOLID_BSP
                    self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                    GameUtil.KillBox(self)
                } else {
                    self.solid = Defines.SOLID_NOT
                    self.svflags = self.svflags or Defines.SVF_NOCLIENT
                }
                GameBase.gi.linkentity(self)

                if (0 == (self.spawnflags and 2))
                    self.use = null
            }
        }

        /*
     * QUAKED func_object (0 .5 .8) ? TRIGGER_SPAWN ANIMATED ANIMATED_FAST This
     * is solid bmodel that will fall if it's support it removed.
     */
        var func_object_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "func_object_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t?, surf: csurface_t) {
                // only squash thing we fall on top of
                if (plane == null)
                    return
                if (plane!!.normal[2] < 1.0)
                    return
                if (other.takedamage == Defines.DAMAGE_NO)
                    return
                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, self.s.origin, Globals.vec3_origin, self.dmg, 1, 0, Defines.MOD_CRUSH)
            }
        }

        var func_object_release: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "func_object_release"
            }

            public fun think(self: edict_t): Boolean {
                self.movetype = Defines.MOVETYPE_TOSS
                self.touch = func_object_touch
                return true
            }
        }

        var func_object_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_object_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.solid = Defines.SOLID_BSP
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.use = null
                GameUtil.KillBox(self)
                func_object_release.think(self)
            }
        }

        /*
     * QUAKED func_explosive (0 .5 .8) ? Trigger_Spawn ANIMATED ANIMATED_FAST
     * Any brush that you want to explode or break apart. If you want an
     * ex0plosion, set dmg and it will do a radius explosion of that amount at
     * the center of the bursh.
     * 
     * If targeted it will not be shootable.
     * 
     * health defaults to 100.
     * 
     * mass defaults to 75. This determines how much debris is emitted when it
     * explodes. You get one large chunk per 100 of mass (up to 8) and one small
     * chunk per 25 of mass (up to 16). So 800 gives the most.
     */
        public var func_explosive_explode: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "func_explosive_explode"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                val origin = floatArray(0.0, 0.0, 0.0)
                val chunkorigin = floatArray(0.0, 0.0, 0.0)
                val size = floatArray(0.0, 0.0, 0.0)
                var count: Int
                var mass: Int

                // bmodel origins are (0 0 0), we need to adjust that here
                Math3D.VectorScale(self.size, 0.5.toFloat(), size)
                Math3D.VectorAdd(self.absmin, size, origin)
                Math3D.VectorCopy(origin, self.s.origin)

                self.takedamage = Defines.DAMAGE_NO

                if (self.dmg != 0)
                    GameCombat.T_RadiusDamage(self, attacker, self.dmg, null, self.dmg + 40, Defines.MOD_EXPLOSIVE)

                Math3D.VectorSubtract(self.s.origin, inflictor.s.origin, self.velocity)
                Math3D.VectorNormalize(self.velocity)
                Math3D.VectorScale(self.velocity, 150, self.velocity)

                // start chunks towards the center
                Math3D.VectorScale(size, 0.5.toFloat(), size)

                mass = self.mass
                if (0 == mass)
                    mass = 75

                // big chunks
                if (mass >= 100) {
                    count = mass / 100
                    if (count > 8)
                        count = 8
                    while (count-- != 0) {
                        chunkorigin[0] = origin[0] + Lib.crandom() * size[0]
                        chunkorigin[1] = origin[1] + Lib.crandom() * size[1]
                        chunkorigin[2] = origin[2] + Lib.crandom() * size[2]
                        ThrowDebris(self, "models/objects/debris1/tris.md2", 1, chunkorigin)
                    }
                }

                // small chunks
                count = mass / 25
                if (count > 16)
                    count = 16
                while (count-- != 0) {
                    chunkorigin[0] = origin[0] + Lib.crandom() * size[0]
                    chunkorigin[1] = origin[1] + Lib.crandom() * size[1]
                    chunkorigin[2] = origin[2] + Lib.crandom() * size[2]
                    ThrowDebris(self, "models/objects/debris2/tris.md2", 2, chunkorigin)
                }

                GameUtil.G_UseTargets(self, attacker)

                if (self.dmg != 0)
                    BecomeExplosion1(self)
                else
                    GameUtil.G_FreeEdict(self)
            }
        }

        public var func_explosive_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_explosive_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                func_explosive_explode.die(self, self, other, self.health, Globals.vec3_origin)
            }
        }

        public var func_explosive_spawn: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_explosive_spawn"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.solid = Defines.SOLID_BSP
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.use = null
                GameUtil.KillBox(self)
                GameBase.gi.linkentity(self)
            }
        }

        /*
     * QUAKED misc_explobox (0 .5 .8) (-16 -16 0) (16 16 40) Large exploding
     * box. You can override its mass (100), health (80), and dmg (150).
     */

        public var barrel_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "barrel_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val ratio: Float
                val v = floatArray(0.0, 0.0, 0.0)

                if ((null == other.groundentity) || (other.groundentity == self))
                    return

                ratio = other.mass as Float / self.mass as Float
                Math3D.VectorSubtract(self.s.origin, other.s.origin, v)
                M.M_walkmove(self, Math3D.vectoyaw(v), 20 * ratio * Defines.FRAMETIME)
            }
        }

        public var barrel_explode: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "barrel_explode"
            }

            public fun think(self: edict_t): Boolean {

                val org = floatArray(0.0, 0.0, 0.0)
                var spd: Float
                val save = floatArray(0.0, 0.0, 0.0)

                GameCombat.T_RadiusDamage(self, self.activator, self.dmg, null, self.dmg + 40, Defines.MOD_BARREL)

                Math3D.VectorCopy(self.s.origin, save)
                Math3D.VectorMA(self.absmin, 0.5.toFloat(), self.size, self.s.origin)

                // a few big chunks
                spd = 1.5.toFloat() * self.dmg as Float / 200.0.toFloat()
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris1/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris1/tris.md2", spd, org)

                // bottom corners
                spd = 1.75.toFloat() * self.dmg as Float / 200.0.toFloat()
                Math3D.VectorCopy(self.absmin, org)
                ThrowDebris(self, "models/objects/debris3/tris.md2", spd, org)
                Math3D.VectorCopy(self.absmin, org)
                org[0] += self.size[0]
                ThrowDebris(self, "models/objects/debris3/tris.md2", spd, org)
                Math3D.VectorCopy(self.absmin, org)
                org[1] += self.size[1]
                ThrowDebris(self, "models/objects/debris3/tris.md2", spd, org)
                Math3D.VectorCopy(self.absmin, org)
                org[0] += self.size[0]
                org[1] += self.size[1]
                ThrowDebris(self, "models/objects/debris3/tris.md2", spd, org)

                // a bunch of little chunks
                spd = 2 * self.dmg / 200
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)
                org[0] = self.s.origin[0] + Lib.crandom() * self.size[0]
                org[1] = self.s.origin[1] + Lib.crandom() * self.size[1]
                org[2] = self.s.origin[2] + Lib.crandom() * self.size[2]
                ThrowDebris(self, "models/objects/debris2/tris.md2", spd, org)

                Math3D.VectorCopy(save, self.s.origin)
                if (self.groundentity != null)
                    BecomeExplosion2(self)
                else
                    BecomeExplosion1(self)

                return true
            }
        }

        public var barrel_delay: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "barrel_delay"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {

                self.takedamage = Defines.DAMAGE_NO
                self.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
                self.think = barrel_explode
                self.activator = attacker
            }
        }

        //
        // miscellaneous specialty items
        //

        /*
     * QUAKED misc_blackhole (1 .5 0) (-8 -8 -8) (8 8 8)
     */

        var misc_blackhole_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "misc_blavkhole_use"
            }

            public fun use(ent: edict_t, other: edict_t, activator: edict_t) {
                /*
             * gi.WriteByte (svc_temp_entity); gi.WriteByte (TE_BOSSTPORT);
             * gi.WritePosition (ent.s.origin); gi.multicast (ent.s.origin,
             * MULTICAST_PVS);
             */
                GameUtil.G_FreeEdict(ent)
            }
        }

        var misc_blackhole_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_blackhole_think"
            }

            public fun think(self: edict_t): Boolean {

                if (++self.s.frame < 19)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else {
                    self.s.frame = 0
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                }
                return true
            }
        }

        /*
     * QUAKED misc_eastertank (1 .5 0) (-32 -32 -16) (32 32 32)
     */

        var misc_eastertank_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_eastertank_think"
            }

            public fun think(self: edict_t): Boolean {
                if (++self.s.frame < 293)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else {
                    self.s.frame = 254
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                }
                return true
            }
        }

        /*
     * QUAKED misc_easterchick (1 .5 0) (-32 -32 0) (32 32 32)
     */

        var misc_easterchick_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_easterchick_think"
            }

            public fun think(self: edict_t): Boolean {
                if (++self.s.frame < 247)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else {
                    self.s.frame = 208
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                }
                return true
            }
        }

        /*
     * QUAKED misc_easterchick2 (1 .5 0) (-32 -32 0) (32 32 32)
     */
        var misc_easterchick2_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_easterchick2_think"
            }

            public fun think(self: edict_t): Boolean {
                if (++self.s.frame < 287)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else {
                    self.s.frame = 248
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                }
                return true
            }
        }

        /*
     * QUAKED monster_commander_body (1 .5 0) (-32 -32 0) (32 32 48) Not really
     * a monster, this is the Tank Commander's decapitated body. There should be
     * a item_commander_head that has this as it's target.
     */

        public var commander_body_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "commander_body_think"
            }

            public fun think(self: edict_t): Boolean {
                if (++self.s.frame < 24)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                else
                    self.nextthink = 0

                if (self.s.frame == 22)
                    GameBase.gi.sound(self, Defines.CHAN_BODY, GameBase.gi.soundindex("tank/thud.wav"), 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        public var commander_body_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "commander_body_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.think = commander_body_think
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
                GameBase.gi.sound(self, Defines.CHAN_BODY, GameBase.gi.soundindex("tank/pain.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }

        public var commander_body_drop: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "commander_body_group"
            }

            public fun think(self: edict_t): Boolean {
                self.movetype = Defines.MOVETYPE_TOSS
                self.s.origin[2] += 2
                return true
            }
        }

        /*
     * QUAKED misc_banner (1 .5 0) (-4 -4 -4) (4 4 4) The origin is the bottom
     * of the banner. The banner is 128 tall.
     */
        var misc_banner_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_banner_think"
            }

            public fun think(ent: edict_t): Boolean {
                ent.s.frame = (ent.s.frame + 1) % 16
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        /*
     * QUAKED misc_deadsoldier (1 .5 0) (-16 -16 0) (16 16 16) ON_BACK
     * ON_STOMACH BACK_DECAP FETAL_POS SIT_DECAP IMPALED This is the dead player
     * model. Comes in 6 exciting different poses!
     */
        var misc_deadsoldier_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "misc_deadsoldier_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

                if (self.health > -80)
                    return

                GameBase.gi.sound(self, Defines.CHAN_BODY, GameBase.gi.soundindex("misc/udeath.wav"), 1, Defines.ATTN_NORM, 0)
                run {
                    n = 0
                    while (n < 4) {
                        ThrowGib(self, "models/objects/gibs/sm_meat/tris.md2", damage, Defines.GIB_ORGANIC)
                        n++
                    }
                }
                ThrowHead(self, "models/objects/gibs/head2/tris.md2", damage, Defines.GIB_ORGANIC)
            }
        }

        /*
     * QUAKED misc_viper (1 .5 0) (-16 -16 0) (16 16 32) This is the Viper for
     * the flyby bombing. It is trigger_spawned, so you must have something use
     * it for it to show up. There must be a path for it to follow once it is
     * activated.
     * 
     * "speed" How fast the Viper should fly
     */

        var misc_viper_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "misc_viper_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.use = GameFunc.train_use
                GameFunc.train_use.use(self, other, activator)
            }
        }

        /*
     * QUAKED misc_viper_bomb (1 0 0) (-8 -8 -8) (8 8 8) "dmg" how much boom
     * should the bomb make?
     */
        var misc_viper_bomb_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "misc_viper_bomb_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                GameUtil.G_UseTargets(self, self.activator)

                self.s.origin[2] = self.absmin[2] + 1
                GameCombat.T_RadiusDamage(self, self, self.dmg, null, self.dmg + 40, Defines.MOD_BOMB)
                BecomeExplosion2(self)
            }
        }

        var misc_viper_bomb_prethink: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_viper_bomb_prethink"
            }

            public fun think(self: edict_t): Boolean {

                val v = floatArray(0.0, 0.0, 0.0)
                var diff: Float

                self.groundentity = null

                diff = self.timestamp - GameBase.level.time
                if (diff < -1.0)
                    diff = -1.0.toFloat()

                Math3D.VectorScale(self.moveinfo.dir, 1.0.toFloat() + diff, v)
                v[2] = diff

                diff = self.s.angles[2]
                Math3D.vectoangles(v, self.s.angles)
                self.s.angles[2] = diff + 10

                return true
            }
        }

        var misc_viper_bomb_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "misc_viper_bomb_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                var viper: edict_t? = null

                self.solid = Defines.SOLID_BBOX
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.s.effects = self.s.effects or Defines.EF_ROCKET
                self.use = null
                self.movetype = Defines.MOVETYPE_TOSS
                self.prethink = misc_viper_bomb_prethink
                self.touch = misc_viper_bomb_touch
                self.activator = activator

                var es: EdictIterator? = null

                es = GameBase.G_Find(es, GameBase.findByClass, "misc_viper")
                if (es != null)
                    viper = es!!.o

                Math3D.VectorScale(viper!!.moveinfo.dir, viper!!.moveinfo.speed, self.velocity)

                self.timestamp = GameBase.level.time
                Math3D.VectorCopy(viper!!.moveinfo.dir, self.moveinfo.dir)
            }
        }

        /*
     * QUAKED misc_strogg_ship (1 .5 0) (-16 -16 0) (16 16 32) This is a Storgg
     * ship for the flybys. It is trigger_spawned, so you must have something
     * use it for it to show up. There must be a path for it to follow once it
     * is activated.
     * 
     * "speed" How fast it should fly
     */

        var misc_strogg_ship_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "misc_strogg_ship_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.svflags = self.svflags and Defines.SVF_NOCLIENT.inv()
                self.use = GameFunc.train_use
                GameFunc.train_use.use(self, other, activator)
            }
        }

        /*
     * QUAKED misc_satellite_dish (1 .5 0) (-64 -64 0) (64 64 128)
     */
        var misc_satellite_dish_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "misc_satellite_dish_think"
            }

            public fun think(self: edict_t): Boolean {
                self.s.frame++
                if (self.s.frame < 38)
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                return true
            }
        }

        var misc_satellite_dish_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "misc_satellite_dish_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.s.frame = 0
                self.think = misc_satellite_dish_think
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
            }
        }

        /*
     * QUAKED target_string (0 0 1) (-8 -8 -8) (8 8 8)
     */

        var target_string_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "target_string_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                var e: edict_t?
                var n: Int
                val l: Int
                var c: Char

                l = self.message.length()
                run {
                    e = self.teammaster
                    while (e != null) {
                        if (e!!.count == 0)
                            continue
                        n = e!!.count - 1
                        if (n >= l) {
                            e!!.s.frame = 12
                            continue
                        }

                        c = self.message.charAt(n)
                        if (c >= '0' && c <= '9')
                            e!!.s.frame = c.toInt() - '0'
                        else if (c == '-')
                            e!!.s.frame = 10
                        else if (c == ':')
                            e!!.s.frame = 11
                        else
                            e!!.s.frame = 12
                        e = e!!.teamchain
                    }
                }
            }
        }

        /*
     * QUAKED func_clock (0 0 1) (-8 -8 -8) (8 8 8) TIMER_UP TIMER_DOWN
     * START_OFF MULTI_USE target a target_string with this
     * 
     * The default is to be a time of day clock
     * 
     * TIMER_UP and TIMER_DOWN run for "count" seconds and the fire "pathtarget"
     * If START_OFF, this entity must be used before it starts
     * 
     * "style" 0 "xx" 1 "xx:xx" 2 "xx:xx:xx"
     */

        public val CLOCK_MESSAGE_SIZE: Int = 16

        public var func_clock_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "func_clock_think"
            }

            public fun think(self: edict_t): Boolean {
                if (null == self.enemy) {

                    var es: EdictIterator? = null

                    es = GameBase.G_Find(es, GameBase.findByTarget, self.target)
                    if (es != null)
                        self.enemy = es!!.o
                    if (self.enemy == null)
                        return true
                }

                if ((self.spawnflags and 1) != 0) {
                    func_clock_format_countdown(self)
                    self.health++
                } else if ((self.spawnflags and 2) != 0) {
                    func_clock_format_countdown(self)
                    self.health--
                } else {
                    val c = Calendar.getInstance()
                    self.message = "" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)

                    /*
                 * struct tm * ltime; time_t gmtime;
                 * 
                 * time(& gmtime); ltime = localtime(& gmtime);
                 * Com_sprintf(self.message, CLOCK_MESSAGE_SIZE, "%2i:%2i:%2i",
                 * ltime.tm_hour, ltime.tm_min, ltime.tm_sec); if
                 * (self.message[3] == ' ') self.message[3] = '0'; if
                 * (self.message[6] == ' ') self.message[6] = '0';
                 */
                }

                self.enemy.message = self.message
                self.enemy.use.use(self.enemy, self, self)

                if (((self.spawnflags and 1) != 0 && (self.health > self.wait)) || ((self.spawnflags and 2) != 0 && (self.health < self.wait))) {
                    if (self.pathtarget != null) {
                        val savetarget: String
                        val savemessage: String

                        savetarget = self.target
                        savemessage = self.message
                        self.target = self.pathtarget
                        self.message = null
                        GameUtil.G_UseTargets(self, self.activator)
                        self.target = savetarget
                        self.message = savemessage
                    }

                    if (0 == (self.spawnflags and 8))
                        return true

                    func_clock_reset(self)

                    if ((self.spawnflags and 4) != 0)
                        return true
                }

                self.nextthink = GameBase.level.time + 1
                return true

            }
        }

        public var func_clock_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "func_clock_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if (0 == (self.spawnflags and 8))
                    self.use = null
                if (self.activator != null)
                    return
                self.activator = activator
                self.think.think(self)
            }
        }

        //=================================================================================

        var teleporter_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "teleporter_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val dest: edict_t?
                var i: Int

                if (other.client == null)
                    return

                dest = GameBase.G_Find(null, GameBase.findByTarget, self.target).o

                if (dest == null) {
                    GameBase.gi.dprintf("Couldn't find destination\n")
                    return
                }

                // unlink to make sure it can't possibly interfere with KillBox
                GameBase.gi.unlinkentity(other)

                Math3D.VectorCopy(dest!!.s.origin, other.s.origin)
                Math3D.VectorCopy(dest!!.s.origin, other.s.old_origin)
                other.s.origin[2] += 10

                // clear the velocity and hold them in place briefly
                Math3D.VectorClear(other.velocity)
                other.client.ps.pmove.pm_time = 160 shr 3 // hold time
                other.client.ps.pmove.pm_flags = other.client.ps.pmove.pm_flags or pmove_t.PMF_TIME_TELEPORT

                // draw the teleport splash at source and on the player
                self.owner.s.event = Defines.EV_PLAYER_TELEPORT
                other.s.event = Defines.EV_PLAYER_TELEPORT

                // set angles
                run {
                    i = 0
                    while (i < 3) {
                        other.client.ps.pmove.delta_angles[i] = Math3D.ANGLE2SHORT(dest!!.s.angles[i] - other.client.resp.cmd_angles[i]) as Short
                        i++
                    }
                }

                Math3D.VectorClear(other.s.angles)
                Math3D.VectorClear(other.client.ps.viewangles)
                Math3D.VectorClear(other.client.v_angle)

                // kill anything at the destination
                GameUtil.KillBox(other)

                GameBase.gi.linkentity(other)
            }
        }

        /*
     * QUAKED misc_teleporter_dest (1 0 0) (-32 -32 -24) (32 32 -16) Point
     * teleporters at these.
     */

        public var SP_misc_teleporter_dest: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_teleporter_dest"
            }

            public fun think(ent: edict_t): Boolean {
                GameBase.gi.setmodel(ent, "models/objects/dmspot/tris.md2")
                ent.s.skinnum = 0
                ent.solid = Defines.SOLID_BBOX
                //	ent.s.effects |= EF_FLIES;
                Math3D.VectorSet(ent.mins, -32, -32, -24)
                Math3D.VectorSet(ent.maxs, 32, 32, -16)
                GameBase.gi.linkentity(ent)
                return true
            }
        }

        public var gib_think: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gib_think"
            }

            public fun think(self: edict_t): Boolean {
                self.s.frame++
                self.nextthink = GameBase.level.time + Defines.FRAMETIME

                if (self.s.frame == 10) {
                    self.think = GameUtil.G_FreeEdictA
                    self.nextthink = GameBase.level.time + 8 + Globals.rnd.nextFloat() * 10
                }
                return true
            }
        }

        public var gib_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "gib_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t?, surf: csurface_t) {
                val normal_angles = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)

                if (null == self.groundentity)
                    return

                self.touch = null

                if (plane != null) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, GameBase.gi.soundindex("misc/fhit3.wav"), 1, Defines.ATTN_NORM, 0)

                    Math3D.vectoangles(plane!!.normal, normal_angles)
                    Math3D.AngleVectors(normal_angles, null, right, null)
                    Math3D.vectoangles(right, self.s.angles)

                    if (self.s.modelindex == GameBase.sm_meat_index) {
                        self.s.frame++
                        self.think = gib_think
                        self.nextthink = GameBase.level.time + Defines.FRAMETIME
                    }
                }
            }
        }

        public var gib_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "gib_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                GameUtil.G_FreeEdict(self)
            }
        }

        /**
         * Debris
         */
        public var debris_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "debris_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                GameUtil.G_FreeEdict(self)
            }
        }
    }
}