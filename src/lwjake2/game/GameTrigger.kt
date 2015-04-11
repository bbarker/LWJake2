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

public class GameTrigger {
    companion object {

        public fun InitTrigger(self: edict_t) {
            if (!Math3D.VectorEquals(self.s.angles, Globals.vec3_origin))
                GameBase.G_SetMovedir(self.s.angles, self.movedir)

            self.solid = Defines.SOLID_TRIGGER
            self.movetype = Defines.MOVETYPE_NONE
            GameBase.gi.setmodel(self, self.model)
            self.svflags = Defines.SVF_NOCLIENT
        }

        // the trigger was just activated
        // ent.activator should be set to the activator so it can be held through a
        // delay so wait for the delay time before firing
        public fun multi_trigger(ent: edict_t) {
            if (ent.nextthink != 0)
                return  // already been triggered

            GameUtil.G_UseTargets(ent, ent.activator)

            if (ent.wait > 0) {
                ent.think = multi_wait
                ent.nextthink = GameBase.level.time + ent.wait
            } else {
                // we can't just remove (self) here, because this is a touch
                // function
                // called while looping through area links...
                ent.touch = null
                ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                ent.think = GameUtil.G_FreeEdictA
            }
        }

        public fun SP_trigger_multiple(ent: edict_t) {
            if (ent.sounds == 1)
                ent.noise_index = GameBase.gi.soundindex("misc/secret.wav")
            else if (ent.sounds == 2)
                ent.noise_index = GameBase.gi.soundindex("misc/talk.wav")
            else if (ent.sounds == 3)
                ent.noise_index = GameBase.gi.soundindex("misc/trigger1.wav")

            if (ent.wait == 0)
                ent.wait = 0.2.toFloat()

            ent.touch = Touch_Multi
            ent.movetype = Defines.MOVETYPE_NONE
            ent.svflags = ent.svflags or Defines.SVF_NOCLIENT

            if ((ent.spawnflags and 4) != 0) {
                ent.solid = Defines.SOLID_NOT
                ent.use = trigger_enable
            } else {
                ent.solid = Defines.SOLID_TRIGGER
                ent.use = Use_Multi
            }

            if (!Math3D.VectorEquals(ent.s.angles, Globals.vec3_origin))
                GameBase.G_SetMovedir(ent.s.angles, ent.movedir)

            GameBase.gi.setmodel(ent, ent.model)
            GameBase.gi.linkentity(ent)
        }

        /**
         * QUAKED trigger_once (.5 .5 .5) ? x x TRIGGERED Triggers once, then
         * removes itself. You must set the key "target" to the name of another
         * object in the level that has a matching "targetname".

         * If TRIGGERED, this trigger must be triggered before it is live.

         * sounds 1) secret 2) beep beep 3) large switch 4)

         * "message" string to be displayed when triggered
         */

        public fun SP_trigger_once(ent: edict_t) {
            // make old maps work because I messed up on flag assignments here
            // triggered was on bit 1 when it should have been on bit 4
            if ((ent.spawnflags and 1) != 0) {
                val v = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorMA(ent.mins, 0.5.toFloat(), ent.size, v)
                ent.spawnflags = ent.spawnflags and 1.inv()
                ent.spawnflags = ent.spawnflags or 4
                GameBase.gi.dprintf("fixed TRIGGERED flag on " + ent.classname + " at " + Lib.vtos(v) + "\n")
            }

            ent.wait = -1
            SP_trigger_multiple(ent)
        }

        public fun SP_trigger_relay(self: edict_t) {
            self.use = trigger_relay_use
        }

        public fun SP_trigger_key(self: edict_t) {
            if (GameBase.st.item == null) {
                GameBase.gi.dprintf("no key item for trigger_key at " + Lib.vtos(self.s.origin) + "\n")
                return
            }
            self.item = GameItems.FindItemByClassname(GameBase.st.item)

            if (null == self.item) {
                GameBase.gi.dprintf("item " + GameBase.st.item + " not found for trigger_key at " + Lib.vtos(self.s.origin) + "\n")
                return
            }

            if (self.target == null) {
                GameBase.gi.dprintf(self.classname + " at " + Lib.vtos(self.s.origin) + " has no target\n")
                return
            }

            GameBase.gi.soundindex("misc/keytry.wav")
            GameBase.gi.soundindex("misc/keyuse.wav")

            self.use = trigger_key_use
        }

        public fun SP_trigger_counter(self: edict_t) {
            self.wait = -1
            if (0 == self.count)
                self.count = 2

            self.use = trigger_counter_use
        }

        /*
     * ==============================================================================
     * 
     * trigger_always
     * 
     * ==============================================================================
     */

        /*
     * QUAKED trigger_always (.5 .5 .5) (-8 -8 -8) (8 8 8) This trigger will
     * always fire. It is activated by the world.
     */
        public fun SP_trigger_always(ent: edict_t) {
            // we must have some delay to make sure our use targets are present
            if (ent.delay < 0.2.toFloat())
                ent.delay = 0.2.toFloat()
            GameUtil.G_UseTargets(ent, ent)
        }

        /*
     * QUAKED trigger_push (.5 .5 .5) ? PUSH_ONCE Pushes the player "speed"
     * defaults to 1000
     */
        public fun SP_trigger_push(self: edict_t) {
            InitTrigger(self)
            windsound = GameBase.gi.soundindex("misc/windfly.wav")
            self.touch = trigger_push_touch
            if (0 == self.speed)
                self.speed = 1000
            GameBase.gi.linkentity(self)
        }

        public fun SP_trigger_hurt(self: edict_t) {
            InitTrigger(self)

            self.noise_index = GameBase.gi.soundindex("world/electro.wav")
            self.touch = hurt_touch

            if (0 == self.dmg)
                self.dmg = 5

            if ((self.spawnflags and 1) != 0)
                self.solid = Defines.SOLID_NOT
            else
                self.solid = Defines.SOLID_TRIGGER

            if ((self.spawnflags and 2) != 0)
                self.use = hurt_use

            GameBase.gi.linkentity(self)
        }

        public fun SP_trigger_gravity(self: edict_t) {
            if (GameBase.st.gravity == null) {
                GameBase.gi.dprintf("trigger_gravity without gravity set at " + Lib.vtos(self.s.origin) + "\n")
                GameUtil.G_FreeEdict(self)
                return
            }

            InitTrigger(self)
            self.gravity = Lib.atoi(GameBase.st.gravity)
            self.touch = trigger_gravity_touch
        }

        public fun SP_trigger_monsterjump(self: edict_t) {
            if (0 == self.speed)
                self.speed = 200
            if (0 == GameBase.st.height)
                GameBase.st.height = 200
            if (self.s.angles[Defines.YAW] == 0)
                self.s.angles[Defines.YAW] = 360
            InitTrigger(self)
            self.touch = trigger_monsterjump_touch
            self.movedir[2] = GameBase.st.height
        }

        // the wait time has passed, so set back up for another activation
        public var multi_wait: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "multi_wait"
            }

            public fun think(ent: edict_t): Boolean {

                ent.nextthink = 0
                return true
            }
        }

        var Use_Multi: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "Use_Multi"
            }

            public fun use(ent: edict_t, other: edict_t, activator: edict_t) {
                ent.activator = activator
                multi_trigger(ent)
            }
        }

        var Touch_Multi: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "Touch_Multi"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (other.client != null) {
                    if ((self.spawnflags and 2) != 0)
                        return
                } else if ((other.svflags and Defines.SVF_MONSTER) != 0) {
                    if (0 == (self.spawnflags and 1))
                        return
                } else
                    return

                if (!Math3D.VectorEquals(self.movedir, Globals.vec3_origin)) {
                    val forward = floatArray(0.0, 0.0, 0.0)

                    Math3D.AngleVectors(other.s.angles, forward, null, null)
                    if (Math3D.DotProduct(forward, self.movedir) < 0)
                        return
                }

                self.activator = other
                multi_trigger(self)
            }
        }

        /**
         * QUAKED trigger_multiple (.5 .5 .5) ? MONSTER NOT_PLAYER TRIGGERED
         * Variable sized repeatable trigger. Must be targeted at one or more
         * entities. If "delay" is set, the trigger waits some time after activating
         * before firing. "wait" : Seconds between triggerings. (.2 default) sounds
         * 1) secret 2) beep beep 3) large switch 4) set "message" to text string
         */
        var trigger_enable: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "trigger_enable"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                self.solid = Defines.SOLID_TRIGGER
                self.use = Use_Multi
                GameBase.gi.linkentity(self)
            }
        }

        /**
         * QUAKED trigger_relay (.5 .5 .5) (-8 -8 -8) (8 8 8) This fixed size
         * trigger cannot be touched, it can only be fired by other events.
         */
        public var trigger_relay_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "trigger_relay_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                GameUtil.G_UseTargets(self, activator)
            }
        }

        /*
     * ==============================================================================
     * 
     * trigger_key
     * 
     * ==============================================================================
     */

        /**
         * QUAKED trigger_key (.5 .5 .5) (-8 -8 -8) (8 8 8) A relay trigger that
         * only fires it's targets if player has the proper key. Use "item" to
         * specify the required key, for example "key_data_cd"
         */

        var trigger_key_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "trigger_key_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                val index: Int

                if (self.item == null)
                    return
                if (activator.client == null)
                    return

                index = GameItems.ITEM_INDEX(self.item)
                if (activator.client.pers.inventory[index] == 0) {
                    if (GameBase.level.time < self.touch_debounce_time)
                        return
                    self.touch_debounce_time = GameBase.level.time + 5.0.toFloat()
                    GameBase.gi.centerprintf(activator, "You need the " + self.item.pickup_name)
                    GameBase.gi.sound(activator, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/keytry.wav"), 1, Defines.ATTN_NORM, 0)
                    return
                }

                GameBase.gi.sound(activator, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/keyuse.wav"), 1, Defines.ATTN_NORM, 0)
                if (GameBase.coop.value != 0) {
                    var player: Int
                    var ent: edict_t

                    if (Lib.strcmp(self.item.classname, "key_power_cube") == 0) {
                        var cube: Int

                        run {
                            cube = 0
                            while (cube < 8) {
                                if ((activator.client.pers.power_cubes and (1 shl cube)) != 0)
                                    break
                                cube++
                            }
                        }
                        run {
                            player = 1
                            while (player <= GameBase.game.maxclients) {
                                ent = GameBase.g_edicts[player]
                                if (!ent.inuse)
                                    continue
                                if (null == ent.client)
                                    continue
                                if ((ent.client.pers.power_cubes and (1 shl cube)) != 0) {
                                    ent.client.pers.inventory[index]--
                                    ent.client.pers.power_cubes = ent.client.pers.power_cubes and (1 shl cube).inv()
                                }
                                player++
                            }
                        }
                    } else {
                        run {
                            player = 1
                            while (player <= GameBase.game.maxclients) {
                                ent = GameBase.g_edicts[player]
                                if (!ent.inuse)
                                    continue
                                if (ent.client == null)
                                    continue
                                ent.client.pers.inventory[index] = 0
                                player++
                            }
                        }
                    }
                } else {
                    activator.client.pers.inventory[index]--
                }

                GameUtil.G_UseTargets(self, activator)

                self.use = null
            }
        }

        /**
         * QUAKED trigger_counter (.5 .5 .5) ? nomessage Acts as an intermediary for
         * an action that takes multiple inputs.

         * If nomessage is not set, t will print "1 more.. " etc when triggered and
         * "sequence complete" when finished.

         * After the counter has been triggered "count" times (default 2), it will
         * fire all of it's targets and remove itself.
         */
        var trigger_counter_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "trigger_counter_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if (self.count == 0)
                    return

                self.count--

                if (self.count != 0) {
                    if (0 == (self.spawnflags and 1)) {
                        GameBase.gi.centerprintf(activator, self.count + " more to go...")
                        GameBase.gi.sound(activator, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/talk1.wav"), 1, Defines.ATTN_NORM, 0)
                    }
                    return
                }

                if (0 == (self.spawnflags and 1)) {
                    GameBase.gi.centerprintf(activator, "Sequence completed!")
                    GameBase.gi.sound(activator, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/talk1.wav"), 1, Defines.ATTN_NORM, 0)
                }
                self.activator = activator
                multi_trigger(self)
            }
        }

        /*
     * ==============================================================================
     * 
     * trigger_push
     * 
     * ==============================================================================
     */

        public val PUSH_ONCE: Int = 1

        public var windsound: Int = 0

        var trigger_push_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "trigger_push_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (Lib.strcmp(other.classname, "grenade") == 0) {
                    Math3D.VectorScale(self.movedir, self.speed * 10, other.velocity)
                } else if (other.health > 0) {
                    Math3D.VectorScale(self.movedir, self.speed * 10, other.velocity)

                    if (other.client != null) {
                        // don't take falling damage immediately from this
                        Math3D.VectorCopy(other.velocity, other.client.oldvelocity)
                        if (other.fly_sound_debounce_time < GameBase.level.time) {
                            other.fly_sound_debounce_time = GameBase.level.time + 1.5.toFloat()
                            GameBase.gi.sound(other, Defines.CHAN_AUTO, windsound, 1, Defines.ATTN_NORM, 0)
                        }
                    }
                }
                if ((self.spawnflags and PUSH_ONCE) != 0)
                    GameUtil.G_FreeEdict(self)
            }
        }


        /**
         * QUAKED trigger_hurt (.5 .5 .5) ? START_OFF TOGGLE SILENT NO_PROTECTION
         * SLOW Any entity that touches this will be hurt.

         * It does dmg points of damage each server frame

         * SILENT supresses playing the sound SLOW changes the damage rate to once
         * per second NO_PROTECTION *nothing* stops the damage

         * "dmg" default 5 (whole numbers only)

         */
        var hurt_use: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "hurt_use"
            }

            public fun use(self: edict_t, other: edict_t, activator: edict_t) {
                if (self.solid == Defines.SOLID_NOT)
                    self.solid = Defines.SOLID_TRIGGER
                else
                    self.solid = Defines.SOLID_NOT
                GameBase.gi.linkentity(self)

                if (0 == (self.spawnflags and 2))
                    self.use = null
            }
        }

        var hurt_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "hurt_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val dflags: Int

                if (other.takedamage == 0)
                    return

                if (self.timestamp > GameBase.level.time)
                    return

                if ((self.spawnflags and 16) != 0)
                    self.timestamp = GameBase.level.time + 1
                else
                    self.timestamp = GameBase.level.time + Defines.FRAMETIME

                if (0 == (self.spawnflags and 4)) {
                    if ((GameBase.level.framenum % 10) == 0)
                        GameBase.gi.sound(other, Defines.CHAN_AUTO, self.noise_index, 1, Defines.ATTN_NORM, 0)
                }

                if ((self.spawnflags and 8) != 0)
                    dflags = Defines.DAMAGE_NO_PROTECTION
                else
                    dflags = 0
                GameCombat.T_Damage(other, self, self, Globals.vec3_origin, other.s.origin, Globals.vec3_origin, self.dmg, self.dmg, dflags, Defines.MOD_TRIGGER_HURT)
            }
        }

        /*
     * ==============================================================================
     * 
     * trigger_gravity
     * 
     * ==============================================================================
     */

        /**
         * QUAKED trigger_gravity (.5 .5 .5) ? Changes the touching entites gravity
         * to the value of "gravity". 1.0 is standard gravity for the level.
         */

        var trigger_gravity_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "trigger_gravity_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                other.gravity = self.gravity
            }
        }

        /*
     * ==============================================================================
     * 
     * trigger_monsterjump
     * 
     * ==============================================================================
     */

        /**
         * QUAKED trigger_monsterjump (.5 .5 .5) ? Walking monsters that touch this
         * will jump in the direction of the trigger's angle "speed" default to 200,
         * the speed thrown forward "height" default to 200, the speed thrown
         * upwards
         */

        var trigger_monsterjump_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "trigger_monsterjump_touch"
            }

            public fun touch(self: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if ((other.flags and (Defines.FL_FLY or Defines.FL_SWIM)) != 0)
                    return
                if ((other.svflags and Defines.SVF_DEADMONSTER) != 0)
                    return
                if (0 == (other.svflags and Defines.SVF_MONSTER))
                    return

                // set XY even if not on ground, so the jump will clear lips
                other.velocity[0] = self.movedir[0] * self.speed
                other.velocity[1] = self.movedir[1] * self.speed

                if (other.groundentity != null)
                    return

                other.groundentity = null
                other.velocity[2] = self.movedir[2]
            }
        }
    }
}