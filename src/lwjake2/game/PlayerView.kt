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
import lwjake2.game.monsters.M_Player
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class PlayerView {
    companion object {

        public var current_player: edict_t

        public var current_client: gclient_t

        public var forward: FloatArray = floatArray(0.0, 0.0, 0.0)

        public var right: FloatArray = floatArray(0.0, 0.0, 0.0)

        public var up: FloatArray = floatArray(0.0, 0.0, 0.0)

        /**
         * SV_CalcRoll.
         */
        public fun SV_CalcRoll(angles: FloatArray, velocity: FloatArray): Float {
            val sign: Float
            var side: Float
            val value: Float

            side = Math3D.DotProduct(velocity, right)
            sign = (if (side < 0) -1 else 1).toFloat()
            side = Math.abs(side)

            value = GameBase.sv_rollangle.value

            if (side < GameBase.sv_rollspeed.value)
                side = side * value / GameBase.sv_rollspeed.value
            else
                side = value

            return side * sign
        }

        /*
     * =============== 
     * P_DamageFeedback
     * 
     * Handles color blends and view kicks 
     * ===============
     */

        public fun P_DamageFeedback(player: edict_t) {
            val client: gclient_t
            var side: Float
            val realcount: Float
            var count: Float
            var kick: Float
            val v = floatArray(0.0, 0.0, 0.0)
            val r: Int
            val l: Int
            val power_color = floatArray(0.0.toFloat(), 1.0.toFloat(), 0.0.toFloat())
            val acolor = floatArray(1.0.toFloat(), 1.0.toFloat(), 1.0.toFloat())
            val bcolor = floatArray(1.0.toFloat(), 0.0.toFloat(), 0.0.toFloat())

            client = player.client

            // flash the backgrounds behind the status numbers
            client.ps.stats[Defines.STAT_FLASHES] = 0
            if (client.damage_blood != 0)
                client.ps.stats[Defines.STAT_FLASHES] = client.ps.stats[Defines.STAT_FLASHES] or 1
            if (client.damage_armor != 0 && 0 == (player.flags and Defines.FL_GODMODE) && (client.invincible_framenum <= GameBase.level.framenum))
                client.ps.stats[Defines.STAT_FLASHES] = client.ps.stats[Defines.STAT_FLASHES] or 2

            // total points of damage shot at the player this frame
            count = (client.damage_blood + client.damage_armor + client.damage_parmor)

            if (count == 0)
                return  // didn't take any damage

            // start a pain animation if still in the player model
            if ((client.anim_priority < Defines.ANIM_PAIN) and (player.s.modelindex == 255)) {
                client.anim_priority = Defines.ANIM_PAIN
                if ((client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                    player.s.frame = M_Player.FRAME_crpain1 - 1
                    client.anim_end = M_Player.FRAME_crpain4
                } else {

                    xxxi = (xxxi + 1) % 3
                    when (xxxi) {
                        0 -> {
                            player.s.frame = M_Player.FRAME_pain101 - 1
                            client.anim_end = M_Player.FRAME_pain104
                        }
                        1 -> {
                            player.s.frame = M_Player.FRAME_pain201 - 1
                            client.anim_end = M_Player.FRAME_pain204
                        }
                        2 -> {
                            player.s.frame = M_Player.FRAME_pain301 - 1
                            client.anim_end = M_Player.FRAME_pain304
                        }
                    }
                }
            }

            realcount = count
            if (count < 10)
                count = 10 // always make a visible effect

            // play an apropriate pain sound
            if ((GameBase.level.time > player.pain_debounce_time) && 0 == (player.flags and Defines.FL_GODMODE) && (client.invincible_framenum <= GameBase.level.framenum)) {
                r = 1 + (Lib.rand() and 1)
                player.pain_debounce_time = GameBase.level.time + 0.7.toFloat()
                if (player.health < 25)
                    l = 25
                else if (player.health < 50)
                    l = 50
                else if (player.health < 75)
                    l = 75
                else
                    l = 100
                GameBase.gi.sound(player, Defines.CHAN_VOICE, GameBase.gi.soundindex("*pain" + l + "_" + r + ".wav"), 1, Defines.ATTN_NORM, 0)
            }

            // the total alpha of the blend is always proportional to count
            if (client.damage_alpha < 0)
                client.damage_alpha = 0
            client.damage_alpha += count * 0.01.toFloat()
            if (client.damage_alpha < 0.2.toFloat())
                client.damage_alpha = 0.2.toFloat()
            if (client.damage_alpha > 0.6.toFloat())
                client.damage_alpha = 0.6.toFloat() // don't go too saturated

            // the color of the blend will vary based on how much was absorbed
            // by different armors
            //

            Math3D.VectorClear(v)
            if (client.damage_parmor != 0)
                Math3D.VectorMA(v, client.damage_parmor as Float / realcount, power_color, v)

            if (client.damage_armor != 0)
                Math3D.VectorMA(v, client.damage_armor as Float / realcount, acolor, v)

            if (client.damage_blood != 0)
                Math3D.VectorMA(v, client.damage_blood as Float / realcount, bcolor, v)
            Math3D.VectorCopy(v, client.damage_blend)

            //
            // calculate view angle kicks
            //
            kick = Math.abs(client.damage_knockback)
            if (kick != 0 && player.health > 0)
            // kick of 0 means no view adjust at
            // all
            {
                kick = kick * 100 / player.health

                if (kick < count.toDouble() * 0.5)
                    kick = count * 0.5.toFloat()
                if (kick > 50)
                    kick = 50

                Math3D.VectorSubtract(client.damage_from, player.s.origin, v)
                Math3D.VectorNormalize(v)

                side = Math3D.DotProduct(v, right)
                client.v_dmg_roll = kick * side * 0.3.toFloat()

                side = -Math3D.DotProduct(v, forward)
                client.v_dmg_pitch = kick * side * 0.3.toFloat()

                client.v_dmg_time = GameBase.level.time + Defines.DAMAGE_TIME
            }

            //
            // clear totals
            //
            client.damage_blood = 0
            client.damage_armor = 0
            client.damage_parmor = 0
            client.damage_knockback = 0
        }

        /**

         * fall from 128: 400 = 160000
         * fall from 256: 580 = 336400
         * fall from 384: 720 = 518400
         * fall from 512: 800 = 640000
         * fall from 640: 960 =
         * damage = deltavelocity*deltavelocity * 0.0001
         */
        public fun SV_CalcViewOffset(ent: edict_t) {
            var angles = floatArray(0.0, 0.0, 0.0)
            var bob: Float
            var ratio: Float
            var delta: Float
            val v = floatArray(0.0, 0.0, 0.0)

            // base angles
            angles = ent.client.ps.kick_angles

            // if dead, fix the angle and don't add any kick
            if (ent.deadflag != 0) {
                Math3D.VectorClear(angles)

                ent.client.ps.viewangles[Defines.ROLL] = 40
                ent.client.ps.viewangles[Defines.PITCH] = -15
                ent.client.ps.viewangles[Defines.YAW] = ent.client.killer_yaw
            } else {

                // add angles based on weapon kick
                Math3D.VectorCopy(ent.client.kick_angles, angles)

                // add angles based on damage kick
                ratio = (ent.client.v_dmg_time - GameBase.level.time) / Defines.DAMAGE_TIME
                if (ratio < 0) {
                    ratio = 0
                    ent.client.v_dmg_pitch = 0
                    ent.client.v_dmg_roll = 0
                }
                angles[Defines.PITCH] += ratio * ent.client.v_dmg_pitch
                angles[Defines.ROLL] += ratio * ent.client.v_dmg_roll

                // add pitch based on fall kick
                ratio = (ent.client.fall_time - GameBase.level.time) / Defines.FALL_TIME
                if (ratio < 0)
                    ratio = 0
                angles[Defines.PITCH] += ratio * ent.client.fall_value

                // add angles based on velocity
                delta = Math3D.DotProduct(ent.velocity, forward)
                angles[Defines.PITCH] += delta * GameBase.run_pitch.value

                delta = Math3D.DotProduct(ent.velocity, right)
                angles[Defines.ROLL] += delta * GameBase.run_roll.value

                // add angles based on bob
                delta = bobfracsin * GameBase.bob_pitch.value * xyspeed
                if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0)
                    delta *= 6 // crouching
                angles[Defines.PITCH] += delta
                delta = bobfracsin * GameBase.bob_roll.value * xyspeed
                if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0)
                    delta *= 6 // crouching
                if ((bobcycle and 1) != 0)
                    delta = -delta
                angles[Defines.ROLL] += delta
            }

            // base origin
            Math3D.VectorClear(v)

            // add view height
            v[2] += ent.viewheight

            // add fall height
            ratio = (ent.client.fall_time - GameBase.level.time) / Defines.FALL_TIME
            if (ratio < 0)
                ratio = 0
            v[2] -= ratio * ent.client.fall_value * 0.4

            // add bob height
            bob = bobfracsin * xyspeed * GameBase.bob_up.value
            if (bob > 6)
                bob = 6

            //gi.DebugGraph (bob *2, 255);
            v[2] += bob

            // add kick offset

            Math3D.VectorAdd(v, ent.client.kick_origin, v)

            // absolutely bound offsets
            // so the view can never be outside the player box

            if (v[0] < -14)
                v[0] = (-14).toFloat()
            else if (v[0] > 14)
                v[0] = 14
            if (v[1] < -14)
                v[1] = (-14).toFloat()
            else if (v[1] > 14)
                v[1] = 14
            if (v[2] < -22)
                v[2] = (-22).toFloat()
            else if (v[2] > 30)
                v[2] = 30

            Math3D.VectorCopy(v, ent.client.ps.viewoffset)
        }

        /**
         * Calculates where to draw the gun.
         */
        public fun SV_CalcGunOffset(ent: edict_t) {
            var i: Int
            var delta: Float

            // gun angles from bobbing
            ent.client.ps.gunangles[Defines.ROLL] = xyspeed * bobfracsin * 0.005.toFloat()
            ent.client.ps.gunangles[Defines.YAW] = xyspeed * bobfracsin * 0.01.toFloat()
            if ((bobcycle and 1) != 0) {
                ent.client.ps.gunangles[Defines.ROLL] = -ent.client.ps.gunangles[Defines.ROLL]
                ent.client.ps.gunangles[Defines.YAW] = -ent.client.ps.gunangles[Defines.YAW]
            }

            ent.client.ps.gunangles[Defines.PITCH] = xyspeed * bobfracsin * 0.005.toFloat()

            // gun angles from delta movement
            run {
                i = 0
                while (i < 3) {
                    delta = ent.client.oldviewangles[i] - ent.client.ps.viewangles[i]
                    if (delta > 180)
                        delta -= 360
                    if (delta < -180)
                        delta += 360
                    if (delta > 45)
                        delta = 45
                    if (delta < -45)
                        delta = (-45).toFloat()
                    if (i == Defines.YAW)
                        ent.client.ps.gunangles[Defines.ROLL] += 0.1 * delta.toDouble()
                    ent.client.ps.gunangles[i] += 0.2 * delta.toDouble()
                    i++
                }
            }

            // gun height
            Math3D.VectorClear(ent.client.ps.gunoffset)
            //	ent.ps.gunorigin[2] += bob;

            // gun_x / gun_y / gun_z are development tools
            run {
                i = 0
                while (i < 3) {
                    ent.client.ps.gunoffset[i] += forward[i] * (GameBase.gun_y.value)
                    ent.client.ps.gunoffset[i] += right[i] * GameBase.gun_x.value
                    ent.client.ps.gunoffset[i] += up[i] * (-GameBase.gun_z.value)
                    i++
                }
            }
        }

        /**
         * Adds a blending effect to the clients view.
         */
        public fun SV_AddBlend(r: Float, g: Float, b: Float, a: Float, v_blend: FloatArray) {
            val a2: Float
            val a3: Float

            if (a <= 0)
                return
            a2 = v_blend[3] + (1 - v_blend[3]) * a // new total alpha
            a3 = v_blend[3] / a2 // fraction of color from old

            v_blend[0] = v_blend[0] * a3 + r * (1 - a3)
            v_blend[1] = v_blend[1] * a3 + g * (1 - a3)
            v_blend[2] = v_blend[2] * a3 + b * (1 - a3)
            v_blend[3] = a2
        }

        /**
         * Calculates the blending color according to the players environment.
         */
        public fun SV_CalcBlend(ent: edict_t) {
            val contents: Int
            val vieworg = floatArray(0.0, 0.0, 0.0)
            val remaining: Int

            ent.client.ps.blend[0] = ent.client.ps.blend[1] = ent.client.ps.blend[2] = ent.client.ps.blend[3] = 0

            // add for contents
            Math3D.VectorAdd(ent.s.origin, ent.client.ps.viewoffset, vieworg)
            contents = GameBase.gi.pointcontents.pointcontents(vieworg)
            if ((contents and (Defines.CONTENTS_LAVA or Defines.CONTENTS_SLIME or Defines.CONTENTS_WATER)) != 0)
                ent.client.ps.rdflags = ent.client.ps.rdflags or Defines.RDF_UNDERWATER
            else
                ent.client.ps.rdflags = ent.client.ps.rdflags and Defines.RDF_UNDERWATER.inv()

            if ((contents and (Defines.CONTENTS_SOLID or Defines.CONTENTS_LAVA)) != 0)
                SV_AddBlend(1.0.toFloat(), 0.3.toFloat(), 0.0.toFloat(), 0.6.toFloat(), ent.client.ps.blend)
            else if ((contents and Defines.CONTENTS_SLIME) != 0)
                SV_AddBlend(0.0.toFloat(), 0.1.toFloat(), 0.05.toFloat(), 0.6.toFloat(), ent.client.ps.blend)
            else if ((contents and Defines.CONTENTS_WATER) != 0)
                SV_AddBlend(0.5.toFloat(), 0.3.toFloat(), 0.2.toFloat(), 0.4.toFloat(), ent.client.ps.blend)

            // add for powerups
            if (ent.client.quad_framenum > GameBase.level.framenum) {
                remaining = (ent.client.quad_framenum - GameBase.level.framenum) as Int
                if (remaining == 30)
                // beginning to fade
                    GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage2.wav"), 1, Defines.ATTN_NORM, 0)
                if (remaining > 30 || (remaining and 4) != 0)
                    SV_AddBlend(0, 0, 1, 0.08.toFloat(), ent.client.ps.blend)
            } else if (ent.client.invincible_framenum > GameBase.level.framenum) {
                remaining = ent.client.invincible_framenum as Int - GameBase.level.framenum
                if (remaining == 30)
                // beginning to fade
                    GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/protect2.wav"), 1, Defines.ATTN_NORM, 0)
                if (remaining > 30 || (remaining and 4) != 0)
                    SV_AddBlend(1, 1, 0, 0.08.toFloat(), ent.client.ps.blend)
            } else if (ent.client.enviro_framenum > GameBase.level.framenum) {
                remaining = ent.client.enviro_framenum as Int - GameBase.level.framenum
                if (remaining == 30)
                // beginning to fade
                    GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/airout.wav"), 1, Defines.ATTN_NORM, 0)
                if (remaining > 30 || (remaining and 4) != 0)
                    SV_AddBlend(0, 1, 0, 0.08.toFloat(), ent.client.ps.blend)
            } else if (ent.client.breather_framenum > GameBase.level.framenum) {
                remaining = ent.client.breather_framenum as Int - GameBase.level.framenum
                if (remaining == 30)
                // beginning to fade
                    GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/airout.wav"), 1, Defines.ATTN_NORM, 0)
                if (remaining > 30 || (remaining and 4) != 0)
                    SV_AddBlend(0.4.toFloat(), 1, 0.4.toFloat(), 0.04.toFloat(), ent.client.ps.blend)
            }

            // add for damage
            if (ent.client.damage_alpha > 0)
                SV_AddBlend(ent.client.damage_blend[0], ent.client.damage_blend[1], ent.client.damage_blend[2], ent.client.damage_alpha, ent.client.ps.blend)

            if (ent.client.bonus_alpha > 0)
                SV_AddBlend(0.85.toFloat(), 0.7.toFloat(), 0.3.toFloat(), ent.client.bonus_alpha, ent.client.ps.blend)

            // drop the damage value
            ent.client.damage_alpha -= 0.06
            if (ent.client.damage_alpha < 0)
                ent.client.damage_alpha = 0

            // drop the bonus value
            ent.client.bonus_alpha -= 0.1
            if (ent.client.bonus_alpha < 0)
                ent.client.bonus_alpha = 0
        }

        /**
         * Calculates damage and effect when a player falls down.
         */
        public fun P_FallingDamage(ent: edict_t) {
            var delta: Float
            var damage: Int
            val dir = floatArray(0.0, 0.0, 0.0)

            if (ent.s.modelindex != 255)
                return  // not in the player model

            if (ent.movetype == Defines.MOVETYPE_NOCLIP)
                return

            if ((ent.client.oldvelocity[2] < 0) && (ent.velocity[2] > ent.client.oldvelocity[2]) && (null == ent.groundentity)) {
                delta = ent.client.oldvelocity[2]
            } else {
                if (ent.groundentity == null)
                    return
                delta = ent.velocity[2] - ent.client.oldvelocity[2]
            }
            delta = delta * delta * 0.0001.toFloat()

            // never take falling damage if completely underwater
            if (ent.waterlevel == 3)
                return
            if (ent.waterlevel == 2)
                delta *= 0.25
            if (ent.waterlevel == 1)
                delta *= 0.5

            if (delta < 1)
                return

            if (delta < 15) {
                ent.s.event = Defines.EV_FOOTSTEP
                return
            }

            ent.client.fall_value = delta * 0.5.toFloat()
            if (ent.client.fall_value > 40)
                ent.client.fall_value = 40
            ent.client.fall_time = GameBase.level.time + Defines.FALL_TIME

            if (delta > 30) {
                if (ent.health > 0) {
                    if (delta >= 55)
                        ent.s.event = Defines.EV_FALLFAR
                    else
                        ent.s.event = Defines.EV_FALL
                }
                ent.pain_debounce_time = GameBase.level.time // no normal pain
                // sound
                damage = ((delta - 30) / 2).toInt()
                if (damage < 1)
                    damage = 1
                Math3D.VectorSet(dir, 0, 0, 1)

                if (GameBase.deathmatch.value == 0 || 0 == (GameBase.dmflags.value as Int and Defines.DF_NO_FALLING))
                    GameCombat.T_Damage(ent, GameBase.g_edicts[0], GameBase.g_edicts[0], dir, ent.s.origin, Globals.vec3_origin, damage, 0, 0, Defines.MOD_FALLING)
            } else {
                ent.s.event = Defines.EV_FALLSHORT
                return
            }
        }

        /**
         * General effect handling for a player.
         */
        public fun P_WorldEffects() {
            val breather: Boolean
            val envirosuit: Boolean
            val waterlevel: Int
            val old_waterlevel: Int

            if (current_player.movetype == Defines.MOVETYPE_NOCLIP) {
                current_player.air_finished = GameBase.level.time + 12 // don't
                // need air
                return
            }

            waterlevel = current_player.waterlevel
            old_waterlevel = current_client.old_waterlevel
            current_client.old_waterlevel = waterlevel

            breather = current_client.breather_framenum > GameBase.level.framenum
            envirosuit = current_client.enviro_framenum > GameBase.level.framenum

            //
            // if just entered a water volume, play a sound
            //
            if (old_waterlevel == 0 && waterlevel != 0) {
                PlayerWeapon.PlayerNoise(current_player, current_player.s.origin, Defines.PNOISE_SELF)
                if ((current_player.watertype and Defines.CONTENTS_LAVA) != 0)
                    GameBase.gi.sound(current_player, Defines.CHAN_BODY, GameBase.gi.soundindex("player/lava_in.wav"), 1, Defines.ATTN_NORM, 0)
                else if ((current_player.watertype and Defines.CONTENTS_SLIME) != 0)
                    GameBase.gi.sound(current_player, Defines.CHAN_BODY, GameBase.gi.soundindex("player/watr_in.wav"), 1, Defines.ATTN_NORM, 0)
                else if ((current_player.watertype and Defines.CONTENTS_WATER) != 0)
                    GameBase.gi.sound(current_player, Defines.CHAN_BODY, GameBase.gi.soundindex("player/watr_in.wav"), 1, Defines.ATTN_NORM, 0)
                current_player.flags = current_player.flags or Defines.FL_INWATER

                // clear damage_debounce, so the pain sound will play immediately
                current_player.damage_debounce_time = GameBase.level.time - 1
            }

            //
            // if just completely exited a water volume, play a sound
            //
            if (old_waterlevel != 0 && waterlevel == 0) {
                PlayerWeapon.PlayerNoise(current_player, current_player.s.origin, Defines.PNOISE_SELF)
                GameBase.gi.sound(current_player, Defines.CHAN_BODY, GameBase.gi.soundindex("player/watr_out.wav"), 1, Defines.ATTN_NORM, 0)
                current_player.flags = current_player.flags and Defines.FL_INWATER.inv()
            }

            //
            // check for head just going under water
            //
            if (old_waterlevel != 3 && waterlevel == 3) {
                GameBase.gi.sound(current_player, Defines.CHAN_BODY, GameBase.gi.soundindex("player/watr_un.wav"), 1, Defines.ATTN_NORM, 0)
            }

            //
            // check for head just coming out of water
            //
            if (old_waterlevel == 3 && waterlevel != 3) {
                if (current_player.air_finished < GameBase.level.time) {
                    // gasp for
                    // air
                    GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("player/gasp1.wav"), 1, Defines.ATTN_NORM, 0)
                    PlayerWeapon.PlayerNoise(current_player, current_player.s.origin, Defines.PNOISE_SELF)
                } else if (current_player.air_finished < GameBase.level.time + 11) {
                    // just
                    // break
                    // surface
                    GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("player/gasp2.wav"), 1, Defines.ATTN_NORM, 0)
                }
            }

            //
            // check for drowning
            //
            if (waterlevel == 3) {
                // breather or envirosuit give air
                if (breather || envirosuit) {
                    current_player.air_finished = GameBase.level.time + 10

                    if (((current_client.breather_framenum - GameBase.level.framenum) as Int % 25) == 0) {
                        if (current_client.breather_sound == 0)
                            GameBase.gi.sound(current_player, Defines.CHAN_AUTO, GameBase.gi.soundindex("player/u_breath1.wav"), 1, Defines.ATTN_NORM, 0)
                        else
                            GameBase.gi.sound(current_player, Defines.CHAN_AUTO, GameBase.gi.soundindex("player/u_breath2.wav"), 1, Defines.ATTN_NORM, 0)
                        current_client.breather_sound = current_client.breather_sound xor 1
                        PlayerWeapon.PlayerNoise(current_player, current_player.s.origin, Defines.PNOISE_SELF)
                        //FIXME: release a bubble?
                    }
                }

                // if out of air, start drowning
                if (current_player.air_finished < GameBase.level.time) {
                    // drown!
                    if (current_player.client.next_drown_time < GameBase.level.time && current_player.health > 0) {
                        current_player.client.next_drown_time = GameBase.level.time + 1

                        // take more damage the longer underwater
                        current_player.dmg += 2
                        if (current_player.dmg > 15)
                            current_player.dmg = 15

                        // play a gurp sound instead of a normal pain sound
                        if (current_player.health <= current_player.dmg)
                            GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("player/drown1.wav"), 1, Defines.ATTN_NORM, 0)
                        else if ((Lib.rand() and 1) != 0)
                            GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("*gurp1.wav"), 1, Defines.ATTN_NORM, 0)
                        else
                            GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("*gurp2.wav"), 1, Defines.ATTN_NORM, 0)

                        current_player.pain_debounce_time = GameBase.level.time

                        GameCombat.T_Damage(current_player, GameBase.g_edicts[0], GameBase.g_edicts[0], Globals.vec3_origin, current_player.s.origin, Globals.vec3_origin, current_player.dmg, 0, Defines.DAMAGE_NO_ARMOR, Defines.MOD_WATER)
                    }
                }
            } else {
                current_player.air_finished = GameBase.level.time + 12
                current_player.dmg = 2
            }

            //
            // check for sizzle damage
            //
            if (waterlevel != 0 && 0 != (current_player.watertype and (Defines.CONTENTS_LAVA or Defines.CONTENTS_SLIME))) {
                if ((current_player.watertype and Defines.CONTENTS_LAVA) != 0) {
                    if (current_player.health > 0 && current_player.pain_debounce_time <= GameBase.level.time && current_client.invincible_framenum < GameBase.level.framenum) {
                        if ((Lib.rand() and 1) != 0)
                            GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("player/burn1.wav"), 1, Defines.ATTN_NORM, 0)
                        else
                            GameBase.gi.sound(current_player, Defines.CHAN_VOICE, GameBase.gi.soundindex("player/burn2.wav"), 1, Defines.ATTN_NORM, 0)
                        current_player.pain_debounce_time = GameBase.level.time + 1
                    }

                    if (envirosuit)
                    // take 1/3 damage with envirosuit
                        GameCombat.T_Damage(current_player, GameBase.g_edicts[0], GameBase.g_edicts[0], Globals.vec3_origin, current_player.s.origin, Globals.vec3_origin, 1 * waterlevel, 0, 0, Defines.MOD_LAVA)
                    else
                        GameCombat.T_Damage(current_player, GameBase.g_edicts[0], GameBase.g_edicts[0], Globals.vec3_origin, current_player.s.origin, Globals.vec3_origin, 3 * waterlevel, 0, 0, Defines.MOD_LAVA)
                }

                if ((current_player.watertype and Defines.CONTENTS_SLIME) != 0) {
                    if (!envirosuit) {
                        // no damage from slime with envirosuit
                        GameCombat.T_Damage(current_player, GameBase.g_edicts[0], GameBase.g_edicts[0], Globals.vec3_origin, current_player.s.origin, Globals.vec3_origin, 1 * waterlevel, 0, 0, Defines.MOD_SLIME)
                    }
                }
            }
        }

        /*
     * =============== 
     * G_SetClientEffects 
     * ===============
     */
        public fun G_SetClientEffects(ent: edict_t) {
            val pa_type: Int
            val remaining: Int

            ent.s.effects = 0
            ent.s.renderfx = 0

            if (ent.health <= 0 || GameBase.level.intermissiontime != 0)
                return

            if (ent.powerarmor_time > GameBase.level.time) {
                pa_type = GameItems.PowerArmorType(ent)
                if (pa_type == Defines.POWER_ARMOR_SCREEN) {
                    ent.s.effects = ent.s.effects or Defines.EF_POWERSCREEN
                } else if (pa_type == Defines.POWER_ARMOR_SHIELD) {
                    ent.s.effects = ent.s.effects or Defines.EF_COLOR_SHELL
                    ent.s.renderfx = ent.s.renderfx or Defines.RF_SHELL_GREEN
                }
            }

            if (ent.client.quad_framenum > GameBase.level.framenum) {
                remaining = ent.client.quad_framenum as Int - GameBase.level.framenum
                if (remaining > 30 || 0 != (remaining and 4))
                    ent.s.effects = ent.s.effects or Defines.EF_QUAD
            }

            if (ent.client.invincible_framenum > GameBase.level.framenum) {
                remaining = ent.client.invincible_framenum as Int - GameBase.level.framenum
                if (remaining > 30 || 0 != (remaining and 4))
                    ent.s.effects = ent.s.effects or Defines.EF_PENT
            }

            // show cheaters!!!
            if ((ent.flags and Defines.FL_GODMODE) != 0) {
                ent.s.effects = ent.s.effects or Defines.EF_COLOR_SHELL
                ent.s.renderfx = ent.s.renderfx or (Defines.RF_SHELL_RED or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_BLUE)
            }
        }

        /*
     * =============== 
     * G_SetClientEvent 
     * ===============
     */
        public fun G_SetClientEvent(ent: edict_t) {
            if (ent.s.event != 0)
                return

            if (ent.groundentity != null && xyspeed > 225) {
                if ((current_client.bobtime + bobmove) as Int != bobcycle)
                    ent.s.event = Defines.EV_FOOTSTEP
            }
        }

        /*
     * =============== 
     * G_SetClientSound 
     * ===============
     */
        public fun G_SetClientSound(ent: edict_t) {
            val weap: String

            if (ent.client.pers.game_helpchanged != GameBase.game.helpchanged) {
                ent.client.pers.game_helpchanged = GameBase.game.helpchanged
                ent.client.pers.helpchanged = 1
            }

            // help beep (no more than three times)
            if (ent.client.pers.helpchanged != 0 && ent.client.pers.helpchanged <= 3 && 0 == (GameBase.level.framenum and 63)) {
                ent.client.pers.helpchanged++
                GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("misc/pc_up.wav"), 1, Defines.ATTN_STATIC, 0)
            }

            if (ent.client.pers.weapon != null)
                weap = ent.client.pers.weapon.classname
            else
                weap = ""

            if (ent.waterlevel != 0 && 0 != (ent.watertype and (Defines.CONTENTS_LAVA or Defines.CONTENTS_SLIME)))
                ent.s.sound = GameBase.snd_fry
            else if (Lib.strcmp(weap, "weapon_railgun") == 0)
                ent.s.sound = GameBase.gi.soundindex("weapons/rg_hum.wav")
            else if (Lib.strcmp(weap, "weapon_bfg") == 0)
                ent.s.sound = GameBase.gi.soundindex("weapons/bfg_hum.wav")
            else if (ent.client.weapon_sound != 0)
                ent.s.sound = ent.client.weapon_sound
            else
                ent.s.sound = 0
        }

        /*
     * =============== 
     * G_SetClientFrame 
     * ===============
     */
        public fun G_SetClientFrame(ent: edict_t) {
            val client: gclient_t
            val duck: Boolean
            val run: Boolean

            if (ent.s.modelindex != 255)
                return  // not in the player model

            client = ent.client

            if ((client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0)
                duck = true
            else
                duck = false
            if (xyspeed != 0)
                run = true
            else
                run = false

            var skip = false
            // check for stand/duck and stop/go transitions
            if (duck != client.anim_duck && client.anim_priority < Defines.ANIM_DEATH)
                skip = true

            if (run != client.anim_run && client.anim_priority == Defines.ANIM_BASIC)
                skip = true

            if (null == ent.groundentity && client.anim_priority <= Defines.ANIM_WAVE)
                skip = true

            if (!skip) {
                if (client.anim_priority == Defines.ANIM_REVERSE) {
                    if (ent.s.frame > client.anim_end) {
                        ent.s.frame--
                        return
                    }
                } else if (ent.s.frame < client.anim_end) {
                    // continue an animation
                    ent.s.frame++
                    return
                }

                if (client.anim_priority == Defines.ANIM_DEATH)
                    return  // stay there
                if (client.anim_priority == Defines.ANIM_JUMP) {
                    if (null == ent.groundentity)
                        return  // stay there
                    ent.client.anim_priority = Defines.ANIM_WAVE
                    ent.s.frame = M_Player.FRAME_jump3
                    ent.client.anim_end = M_Player.FRAME_jump6
                    return
                }
            }

            // return to either a running or standing frame
            client.anim_priority = Defines.ANIM_BASIC
            client.anim_duck = duck
            client.anim_run = run

            if (null == ent.groundentity) {
                client.anim_priority = Defines.ANIM_JUMP
                if (ent.s.frame != M_Player.FRAME_jump2)
                    ent.s.frame = M_Player.FRAME_jump1
                client.anim_end = M_Player.FRAME_jump2
            } else if (run) {
                // running
                if (duck) {
                    ent.s.frame = M_Player.FRAME_crwalk1
                    client.anim_end = M_Player.FRAME_crwalk6
                } else {
                    ent.s.frame = M_Player.FRAME_run1
                    client.anim_end = M_Player.FRAME_run6
                }
            } else {
                // standing
                if (duck) {
                    ent.s.frame = M_Player.FRAME_crstnd01
                    client.anim_end = M_Player.FRAME_crstnd19
                } else {
                    ent.s.frame = M_Player.FRAME_stand01
                    client.anim_end = M_Player.FRAME_stand40
                }
            }
        }


        /**
         * Called for each player at the end of the server frame and right after
         * spawning.
         */
        public fun ClientEndServerFrame(ent: edict_t) {
            var bobtime: Float
            var i: Int

            current_player = ent
            current_client = ent.client

            //
            // If the origin or velocity have changed since ClientThink(),
            // update the pmove values. This will happen when the client
            // is pushed by a bmodel or kicked by an explosion.
            //
            // If it wasn't updated here, the view position would lag a frame
            // behind the body position when pushed -- "sinking into plats"
            //
            run {
                i = 0
                while (i < 3) {
                    current_client.ps.pmove.origin[i] = (ent.s.origin[i] * 8.0) as Short
                    current_client.ps.pmove.velocity[i] = (ent.velocity[i] * 8.0) as Short
                    i++
                }
            }

            //
            // If the end of unit layout is displayed, don't give
            // the player any normal movement attributes
            //
            if (GameBase.level.intermissiontime != 0) {
                // FIXME: add view drifting here?
                current_client.ps.blend[3] = 0
                current_client.ps.fov = 90
                PlayerHud.G_SetStats(ent)
                return
            }

            Math3D.AngleVectors(ent.client.v_angle, forward, right, up)

            // burn from lava, etc
            P_WorldEffects()

            //
            // set model angles from view angles so other things in
            // the world can tell which direction you are looking
            //
            if (ent.client.v_angle[Defines.PITCH] > 180)
                ent.s.angles[Defines.PITCH] = (-360 + ent.client.v_angle[Defines.PITCH]) / 3
            else
                ent.s.angles[Defines.PITCH] = ent.client.v_angle[Defines.PITCH] / 3
            ent.s.angles[Defines.YAW] = ent.client.v_angle[Defines.YAW]
            ent.s.angles[Defines.ROLL] = 0
            ent.s.angles[Defines.ROLL] = SV_CalcRoll(ent.s.angles, ent.velocity) * 4

            //
            // calculate speed and cycle to be used for
            // all cyclic walking effects
            //
            xyspeed = Math.sqrt(ent.velocity[0] * ent.velocity[0] + ent.velocity[1] * ent.velocity[1]) as Float

            if (xyspeed < 5) {
                bobmove = 0
                current_client.bobtime = 0 // start at beginning of cycle again
            } else if (ent.groundentity != null) {
                // so bobbing only cycles when on
                // ground
                if (xyspeed > 210)
                    bobmove = 0.25.toFloat()
                else if (xyspeed > 100)
                    bobmove = 0.125.toFloat()
                else
                    bobmove = 0.0625.toFloat()
            }

            bobtime = (current_client.bobtime += bobmove)

            if ((current_client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0)
                bobtime *= 4

            bobcycle = bobtime.toInt()
            bobfracsin = Math.abs(Math.sin(bobtime * Math.PI)) as Float

            // detect hitting the floor
            P_FallingDamage(ent)

            // apply all the damage taken this frame
            P_DamageFeedback(ent)

            // determine the view offsets
            SV_CalcViewOffset(ent)

            // determine the gun offsets
            SV_CalcGunOffset(ent)

            // determine the full screen color blend
            // must be after viewoffset, so eye contents can be
            // accurately determined
            // FIXME: with client prediction, the contents
            // should be determined by the client
            SV_CalcBlend(ent)

            // chase cam stuff
            if (ent.client.resp.spectator)
                PlayerHud.G_SetSpectatorStats(ent)
            else
                PlayerHud.G_SetStats(ent)
            PlayerHud.G_CheckChaseStats(ent)

            G_SetClientEvent(ent)

            G_SetClientEffects(ent)

            G_SetClientSound(ent)

            G_SetClientFrame(ent)

            Math3D.VectorCopy(ent.velocity, ent.client.oldvelocity)
            Math3D.VectorCopy(ent.client.ps.viewangles, ent.client.oldviewangles)

            // clear weapon kicks
            Math3D.VectorClear(ent.client.kick_origin)
            Math3D.VectorClear(ent.client.kick_angles)

            // if the scoreboard is up, update it
            if (ent.client.showscores && 0 == (GameBase.level.framenum and 31)) {
                PlayerHud.DeathmatchScoreboardMessage(ent, ent.enemy)
                GameBase.gi.unicast(ent, false)
            }
        }

        public var xyspeed: Float = 0.toFloat()

        public var bobmove: Float = 0.toFloat()

        public var bobcycle: Int = 0 // odd cycles are right foot going forward

        public var bobfracsin: Float = 0.toFloat() // sin(bobfrac*M_PI)}

        private var xxxi = 0
    }
}