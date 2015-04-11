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

public class PlayerWeapon {
    companion object {

        public var Weapon_Grenade: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Grenade"
            }

            public fun think(ent: edict_t): Boolean {
                if ((ent.client.newweapon != null) && (ent.client.weaponstate == Defines.WEAPON_READY)) {
                    ChangeWeapon(ent)
                    return true
                }

                if (ent.client.weaponstate == Defines.WEAPON_ACTIVATING) {
                    ent.client.weaponstate = Defines.WEAPON_READY
                    ent.client.ps.gunframe = 16
                    return true
                }

                if (ent.client.weaponstate == Defines.WEAPON_READY) {
                    if (((ent.client.latched_buttons or ent.client.buttons) and Defines.BUTTON_ATTACK) != 0) {
                        ent.client.latched_buttons = ent.client.latched_buttons and Defines.BUTTON_ATTACK.inv()
                        if (0 != ent.client.pers.inventory[ent.client.ammo_index]) {
                            ent.client.ps.gunframe = 1
                            ent.client.weaponstate = Defines.WEAPON_FIRING
                            ent.client.grenade_time = 0
                        } else {
                            if (GameBase.level.time >= ent.pain_debounce_time) {
                                GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("weapons/noammo.wav"), 1, Defines.ATTN_NORM, 0)
                                ent.pain_debounce_time = GameBase.level.time + 1
                            }
                            NoAmmoWeaponChange(ent)
                        }
                        return true
                    }

                    if ((ent.client.ps.gunframe == 29) || (ent.client.ps.gunframe == 34) || (ent.client.ps.gunframe == 39) || (ent.client.ps.gunframe == 48)) {
                        if ((Lib.rand() and 15) != 0)
                            return true
                    }

                    if (++ent.client.ps.gunframe > 48)
                        ent.client.ps.gunframe = 16
                    return true
                }

                if (ent.client.weaponstate == Defines.WEAPON_FIRING) {
                    if (ent.client.ps.gunframe == 5)
                        GameBase.gi.sound(ent, Defines.CHAN_WEAPON, GameBase.gi.soundindex("weapons/hgrena1b.wav"), 1, Defines.ATTN_NORM, 0)

                    if (ent.client.ps.gunframe == 11) {
                        if (0 == ent.client.grenade_time) {
                            ent.client.grenade_time = GameBase.level.time + Defines.GRENADE_TIMER + 0.2.toFloat()
                            ent.client.weapon_sound = GameBase.gi.soundindex("weapons/hgrenc1b.wav")
                        }

                        // they waited too long, detonate it in their hand
                        if (!ent.client.grenade_blew_up && GameBase.level.time >= ent.client.grenade_time) {
                            ent.client.weapon_sound = 0
                            weapon_grenade_fire(ent, true)
                            ent.client.grenade_blew_up = true
                        }

                        if ((ent.client.buttons and Defines.BUTTON_ATTACK) != 0)
                            return true

                        if (ent.client.grenade_blew_up) {
                            if (GameBase.level.time >= ent.client.grenade_time) {
                                ent.client.ps.gunframe = 15
                                ent.client.grenade_blew_up = false
                            } else {
                                return true
                            }
                        }
                    }

                    if (ent.client.ps.gunframe == 12) {
                        ent.client.weapon_sound = 0
                        weapon_grenade_fire(ent, false)
                    }

                    if ((ent.client.ps.gunframe == 15) && (GameBase.level.time < ent.client.grenade_time))
                        return true

                    ent.client.ps.gunframe++

                    if (ent.client.ps.gunframe == 16) {
                        ent.client.grenade_time = 0
                        ent.client.weaponstate = Defines.WEAPON_READY
                    }
                }
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * GRENADE LAUNCHER
     * 
     * ======================================================================
     */

        public var weapon_grenadelauncher_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "weapon_grenadelauncher_fire"
            }

            public fun think(ent: edict_t): Boolean {
                val offset = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                var damage = 120
                val radius: Float

                radius = (damage + 40).toFloat()
                if (is_quad)
                    damage *= 4

                Math3D.VectorSet(offset, 8, 8, ent.viewheight - 8)
                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

                Math3D.VectorScale(forward, -2, ent.client.kick_origin)
                ent.client.kick_angles[0] = -1

                GameWeapon.fire_grenade(ent, start, forward, damage, 600, 2.5.toFloat(), radius)

                GameBase.gi.WriteByte(Defines.svc_muzzleflash)
                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_GRENADE or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                ent.client.ps.gunframe++

                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index]--

                return true
            }
        }

        public var Weapon_GrenadeLauncher: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_GrenadeLauncher"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(34, 51, 59, 0)
                val fire_frames = intArray(6, 0)

                Weapon_Generic(ent, 5, 16, 59, 64, pause_frames, fire_frames, weapon_grenadelauncher_fire)
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * ROCKET
     * 
     * ======================================================================
     */

        public var Weapon_RocketLauncher_Fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_RocketLauncher_Fire"
            }

            public fun think(ent: edict_t): Boolean {

                val offset = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                var damage: Int
                val damage_radius: Float
                var radius_damage: Int

                damage = 100 + (Lib.random() * 20.0) as Int
                radius_damage = 120
                damage_radius = 120
                if (is_quad) {
                    damage *= 4
                    radius_damage *= 4
                }

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)

                Math3D.VectorScale(forward, -2, ent.client.kick_origin)
                ent.client.kick_angles[0] = -1

                Math3D.VectorSet(offset, 8, 8, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)
                GameWeapon.fire_rocket(ent, start, forward, damage, 650, damage_radius, radius_damage)

                // send muzzle flash
                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_ROCKET or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                ent.client.ps.gunframe++

                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index]--

                return true
            }
        }

        public var Weapon_RocketLauncher: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_RocketLauncher"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(25, 33, 42, 50, 0)
                val fire_frames = intArray(5, 0)

                Weapon_Generic(ent, 4, 12, 50, 54, pause_frames, fire_frames, Weapon_RocketLauncher_Fire)
                return true
            }
        }

        public var Weapon_Blaster_Fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Blaster_Fire"
            }

            public fun think(ent: edict_t): Boolean {

                val damage: Int

                if (GameBase.deathmatch.value != 0)
                    damage = 15
                else
                    damage = 10
                Blaster_Fire(ent, Globals.vec3_origin, damage, false, Defines.EF_BLASTER)
                ent.client.ps.gunframe++
                return true
            }
        }

        public var Weapon_Blaster: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Blaster"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(19, 32, 0)
                val fire_frames = intArray(5, 0)

                Weapon_Generic(ent, 4, 8, 52, 55, pause_frames, fire_frames, Weapon_Blaster_Fire)
                return true
            }
        }

        public var Weapon_HyperBlaster_Fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_HyperBlaster_Fire"
            }

            public fun think(ent: edict_t): Boolean {
                val rotation: Float
                val offset = floatArray(0.0, 0.0, 0.0)
                val effect: Int
                val damage: Int

                ent.client.weapon_sound = GameBase.gi.soundindex("weapons/hyprbl1a.wav")

                if (0 == (ent.client.buttons and Defines.BUTTON_ATTACK)) {
                    ent.client.ps.gunframe++
                } else {
                    if (0 == ent.client.pers.inventory[ent.client.ammo_index]) {
                        if (GameBase.level.time >= ent.pain_debounce_time) {
                            GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("weapons/noammo.wav"), 1, Defines.ATTN_NORM, 0)
                            ent.pain_debounce_time = GameBase.level.time + 1
                        }
                        NoAmmoWeaponChange(ent)
                    } else {
                        rotation = ((ent.client.ps.gunframe - 5) * 2 * Math.PI / 6) as Float
                        offset[0] = (-4 * Math.sin(rotation)) as Float
                        offset[1] = 0.toFloat()
                        offset[2] = (4 * Math.cos(rotation)) as Float

                        if ((ent.client.ps.gunframe == 6) || (ent.client.ps.gunframe == 9))
                            effect = Defines.EF_HYPERBLASTER
                        else
                            effect = 0
                        if (GameBase.deathmatch.value != 0)
                            damage = 15
                        else
                            damage = 20
                        Blaster_Fire(ent, offset, damage, true, effect)
                        if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                            ent.client.pers.inventory[ent.client.ammo_index]--

                        ent.client.anim_priority = Defines.ANIM_ATTACK
                        if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                            ent.s.frame = M_Player.FRAME_crattak1 - 1
                            ent.client.anim_end = M_Player.FRAME_crattak9
                        } else {
                            ent.s.frame = M_Player.FRAME_attack1 - 1
                            ent.client.anim_end = M_Player.FRAME_attack8
                        }
                    }

                    ent.client.ps.gunframe++
                    if (ent.client.ps.gunframe == 12 && 0 != ent.client.pers.inventory[ent.client.ammo_index])
                        ent.client.ps.gunframe = 6
                }

                if (ent.client.ps.gunframe == 12) {
                    GameBase.gi.sound(ent, Defines.CHAN_AUTO, GameBase.gi.soundindex("weapons/hyprbd1a.wav"), 1, Defines.ATTN_NORM, 0)
                    ent.client.weapon_sound = 0
                }

                return true

            }
        }

        public var Weapon_HyperBlaster: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_HyperBlaster"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(0)
                val fire_frames = intArray(6, 7, 8, 9, 10, 11, 0)

                Weapon_Generic(ent, 5, 20, 49, 53, pause_frames, fire_frames, Weapon_HyperBlaster_Fire)
                return true
            }
        }

        public var Weapon_Machinegun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Machinegun"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(23, 45, 0)
                val fire_frames = intArray(4, 5, 0)

                Weapon_Generic(ent, 3, 5, 45, 49, pause_frames, fire_frames, Machinegun_Fire)
                return true
            }
        }

        public var Weapon_Chaingun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Chaingun"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(38, 43, 51, 61, 0)
                val fire_frames = intArray(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 0)

                Weapon_Generic(ent, 4, 31, 61, 64, pause_frames, fire_frames, Chaingun_Fire)
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * SHOTGUN / SUPERSHOTGUN
     * 
     * ======================================================================
     */

        public var weapon_shotgun_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "weapon_shotgun_fire"
            }

            public fun think(ent: edict_t): Boolean {

                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val offset = floatArray(0.0, 0.0, 0.0)
                var damage = 4
                var kick = 8

                if (ent.client.ps.gunframe == 9) {
                    ent.client.ps.gunframe++
                    return true
                }

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)

                Math3D.VectorScale(forward, -2, ent.client.kick_origin)
                ent.client.kick_angles[0] = -2

                Math3D.VectorSet(offset, 0, 8, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

                if (is_quad) {
                    damage *= 4
                    kick *= 4
                }

                if (GameBase.deathmatch.value != 0)
                    GameWeapon.fire_shotgun(ent, start, forward, damage, kick, 500, 500, Defines.DEFAULT_DEATHMATCH_SHOTGUN_COUNT, Defines.MOD_SHOTGUN)
                else
                    GameWeapon.fire_shotgun(ent, start, forward, damage, kick, 500, 500, Defines.DEFAULT_SHOTGUN_COUNT, Defines.MOD_SHOTGUN)

                // send muzzle flash
                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_SHOTGUN or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                ent.client.ps.gunframe++
                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index]--

                return true
            }
        }

        public var Weapon_Shotgun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Shotgun"
            }

            public fun think(ent: edict_t): Boolean {
                val pause_frames = intArray(22, 28, 34, 0)
                val fire_frames = intArray(8, 9, 0)

                Weapon_Generic(ent, 7, 18, 36, 39, pause_frames, fire_frames, weapon_shotgun_fire)
                return true
            }
        }

        public var weapon_supershotgun_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "weapon_supershotgun_fire"
            }

            public fun think(ent: edict_t): Boolean {

                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val offset = floatArray(0.0, 0.0, 0.0)
                val v = floatArray(0.0, 0.0, 0.0)
                var damage = 6
                var kick = 12

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)

                Math3D.VectorScale(forward, -2, ent.client.kick_origin)
                ent.client.kick_angles[0] = -2

                Math3D.VectorSet(offset, 0, 8, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

                if (is_quad) {
                    damage *= 4
                    kick *= 4
                }

                v[Defines.PITCH] = ent.client.v_angle[Defines.PITCH]
                v[Defines.YAW] = ent.client.v_angle[Defines.YAW] - 5
                v[Defines.ROLL] = ent.client.v_angle[Defines.ROLL]
                Math3D.AngleVectors(v, forward, null, null)
                GameWeapon.fire_shotgun(ent, start, forward, damage, kick, Defines.DEFAULT_SHOTGUN_HSPREAD, Defines.DEFAULT_SHOTGUN_VSPREAD, Defines.DEFAULT_SSHOTGUN_COUNT / 2, Defines.MOD_SSHOTGUN)
                v[Defines.YAW] = ent.client.v_angle[Defines.YAW] + 5
                Math3D.AngleVectors(v, forward, null, null)
                GameWeapon.fire_shotgun(ent, start, forward, damage, kick, Defines.DEFAULT_SHOTGUN_HSPREAD, Defines.DEFAULT_SHOTGUN_VSPREAD, Defines.DEFAULT_SSHOTGUN_COUNT / 2, Defines.MOD_SSHOTGUN)

                // send muzzle flash
                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_SSHOTGUN or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                ent.client.ps.gunframe++
                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index] -= 2

                return true
            }
        }

        public var Weapon_SuperShotgun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_SuperShotgun"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(29, 42, 57, 0)
                val fire_frames = intArray(7, 0)

                Weapon_Generic(ent, 6, 17, 57, 61, pause_frames, fire_frames, weapon_supershotgun_fire)
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * RAILGUN
     * 
     * ======================================================================
     */
        public var weapon_railgun_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "weapon_railgun_fire"
            }

            public fun think(ent: edict_t): Boolean {

                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val offset = floatArray(0.0, 0.0, 0.0)
                var damage: Int
                var kick: Int

                if (GameBase.deathmatch.value != 0) {
                    // normal damage is too
                    // extreme in dm
                    damage = 100
                    kick = 200
                } else {
                    damage = 150
                    kick = 250
                }

                if (is_quad) {
                    damage *= 4
                    kick *= 4
                }

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)

                Math3D.VectorScale(forward, -3, ent.client.kick_origin)
                ent.client.kick_angles[0] = -3

                Math3D.VectorSet(offset, 0, 7, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)
                GameWeapon.fire_rail(ent, start, forward, damage, kick)

                // send muzzle flash
                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_RAILGUN or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                ent.client.ps.gunframe++
                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index]--

                return true
            }
        }

        public var Weapon_Railgun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_Railgun"
            }

            public fun think(ent: edict_t): Boolean {

                val pause_frames = intArray(56, 0)
                val fire_frames = intArray(4, 0)
                Weapon_Generic(ent, 3, 18, 56, 61, pause_frames, fire_frames, weapon_railgun_fire)
                return true
            }
        }

        /*
     * ======================================================================
     * 
     * BFG10K
     * 
     * ======================================================================
     */

        public var weapon_bfg_fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "weapon_bfg_fire"
            }

            public fun think(ent: edict_t): Boolean {

                val offset = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                var damage: Int
                val damage_radius = 1000

                if (GameBase.deathmatch.value != 0)
                    damage = 200
                else
                    damage = 500

                if (ent.client.ps.gunframe == 9) {
                    // send muzzle flash
                    GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                    GameBase.gi.WriteShort(ent.index)
                    GameBase.gi.WriteByte(Defines.MZ_BFG or is_silenced)
                    GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                    ent.client.ps.gunframe++

                    PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)
                    return true
                }

                // cells can go down during windup (from power armor hits), so
                // check again and abort firing if we don't have enough now
                if (ent.client.pers.inventory[ent.client.ammo_index] < 50) {
                    ent.client.ps.gunframe++
                    return true
                }

                if (is_quad)
                    damage *= 4

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)

                Math3D.VectorScale(forward, -2, ent.client.kick_origin)

                // make a big pitch kick with an inverse fall
                ent.client.v_dmg_pitch = -40
                ent.client.v_dmg_roll = Lib.crandom() * 8
                ent.client.v_dmg_time = GameBase.level.time + Defines.DAMAGE_TIME

                Math3D.VectorSet(offset, 8, 8, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)
                GameWeapon.fire_bfg(ent, start, forward, damage, 400, damage_radius)

                ent.client.ps.gunframe++

                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index] -= 50

                return true
            }
        }

        public var Weapon_BFG: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Weapon_BFG"
            }

            public fun think(ent: edict_t): Boolean {

                Weapon_Generic(ent, 8, 32, 55, 58, pause_frames, fire_frames, weapon_bfg_fire)
                return true
            }
        }

        public var is_quad: Boolean = false

        public var is_silenced: Byte = 0


        /*
     * ================ 
     * Use_Weapon
     * 
     * Make the weapon ready if there is ammo 
     * ================
     */
        public var Use_Weapon: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "Use_Weapon"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                val ammo_index: Int
                val ammo_item: gitem_t

                // see if we're already using it
                if (item == ent.client.pers.weapon)
                    return

                if (item.ammo != null && 0 == GameBase.g_select_empty.value && 0 == (item.flags and Defines.IT_AMMO)) {

                    ammo_item = GameItems.FindItem(item.ammo)
                    ammo_index = GameItems.ITEM_INDEX(ammo_item)

                    if (0 == ent.client.pers.inventory[ammo_index]) {
                        GameBase.gi.cprintf(ent, Defines.PRINT_HIGH, "No " + ammo_item.pickup_name + " for " + item.pickup_name + ".\n")
                        return
                    }

                    if (ent.client.pers.inventory[ammo_index] < item.quantity) {
                        GameBase.gi.cprintf(ent, Defines.PRINT_HIGH, "Not enough " + ammo_item.pickup_name + " for " + item.pickup_name + ".\n")
                        return
                    }
                }

                // change to this weapon when down
                ent.client.newweapon = item
            }
        }

        /*
     * ================ 
     * Drop_Weapon 
     * ================
     */

        public var Drop_Weapon: ItemDropAdapter = object : ItemDropAdapter() {
            public fun getID(): String {
                return "Drop_Weapon"
            }

            public fun drop(ent: edict_t, item: gitem_t) {
                val index: Int

                if (0 != ((GameBase.dmflags.value) as Int and Defines.DF_WEAPONS_STAY))
                    return

                index = GameItems.ITEM_INDEX(item)
                // see if we're already using it
                if (((item == ent.client.pers.weapon) || (item == ent.client.newweapon)) && (ent.client.pers.inventory[index] == 1)) {
                    GameBase.gi.cprintf(ent, Defines.PRINT_HIGH, "Can't drop current weapon\n")
                    return
                }

                GameItems.Drop_Item(ent, item)
                ent.client.pers.inventory[index]--
            }
        }

        /*
     * ======================================================================
     * 
     * MACHINEGUN / CHAINGUN
     * 
     * ======================================================================
     */

        public var Machinegun_Fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Machinegun_Fire"
            }

            public fun think(ent: edict_t): Boolean {

                var i: Int
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val angles = floatArray(0.0, 0.0, 0.0)
                var damage = 8
                var kick = 2
                val offset = floatArray(0.0, 0.0, 0.0)

                if (0 == (ent.client.buttons and Defines.BUTTON_ATTACK)) {
                    ent.client.machinegun_shots = 0
                    ent.client.ps.gunframe++
                    return true
                }

                if (ent.client.ps.gunframe == 5)
                    ent.client.ps.gunframe = 4
                else
                    ent.client.ps.gunframe = 5

                if (ent.client.pers.inventory[ent.client.ammo_index] < 1) {
                    ent.client.ps.gunframe = 6
                    if (GameBase.level.time >= ent.pain_debounce_time) {
                        GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("weapons/noammo.wav"), 1, Defines.ATTN_NORM, 0)
                        ent.pain_debounce_time = GameBase.level.time + 1
                    }
                    NoAmmoWeaponChange(ent)
                    return true
                }

                if (is_quad) {
                    damage *= 4
                    kick *= 4
                }

                run {
                    i = 1
                    while (i < 3) {
                        ent.client.kick_origin[i] = Lib.crandom() * 0.35.toFloat()
                        ent.client.kick_angles[i] = Lib.crandom() * 0.7.toFloat()
                        i++
                    }
                }
                ent.client.kick_origin[0] = Lib.crandom() * 0.35.toFloat()
                ent.client.kick_angles[0] = ent.client.machinegun_shots * -1.5.toFloat()

                // raise the gun as it is firing
                if (0 == GameBase.deathmatch.value) {
                    ent.client.machinegun_shots++
                    if (ent.client.machinegun_shots > 9)
                        ent.client.machinegun_shots = 9
                }

                // get start / end positions
                Math3D.VectorAdd(ent.client.v_angle, ent.client.kick_angles, angles)
                Math3D.AngleVectors(angles, forward, right, null)
                Math3D.VectorSet(offset, 0, 8, ent.viewheight - 8)
                P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)
                GameWeapon.fire_bullet(ent, start, forward, damage, kick, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MOD_MACHINEGUN)

                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte(Defines.MZ_MACHINEGUN or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index]--

                ent.client.anim_priority = Defines.ANIM_ATTACK
                if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                    ent.s.frame = M_Player.FRAME_crattak1 - (Lib.random() + 0.25) as Int
                    ent.client.anim_end = M_Player.FRAME_crattak9
                } else {
                    ent.s.frame = M_Player.FRAME_attack1 - (Lib.random() + 0.25) as Int
                    ent.client.anim_end = M_Player.FRAME_attack8
                }
                return true
            }
        }

        public var Chaingun_Fire: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Chaingun_Fire"
            }

            public fun think(ent: edict_t): Boolean {

                var i: Int
                var shots: Int
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val up = floatArray(0.0, 0.0, 0.0)
                var r: Float
                var u: Float
                val offset = floatArray(0.0, 0.0, 0.0)
                var damage: Int
                var kick = 2

                if (GameBase.deathmatch.value != 0)
                    damage = 6
                else
                    damage = 8

                if (ent.client.ps.gunframe == 5)
                    GameBase.gi.sound(ent, Defines.CHAN_AUTO, GameBase.gi.soundindex("weapons/chngnu1a.wav"), 1, Defines.ATTN_IDLE, 0)

                if ((ent.client.ps.gunframe == 14) && 0 == (ent.client.buttons and Defines.BUTTON_ATTACK)) {
                    ent.client.ps.gunframe = 32
                    ent.client.weapon_sound = 0
                    return true
                } else if ((ent.client.ps.gunframe == 21) && (ent.client.buttons and Defines.BUTTON_ATTACK) != 0 && 0 != ent.client.pers.inventory[ent.client.ammo_index]) {
                    ent.client.ps.gunframe = 15
                } else {
                    ent.client.ps.gunframe++
                }

                if (ent.client.ps.gunframe == 22) {
                    ent.client.weapon_sound = 0
                    GameBase.gi.sound(ent, Defines.CHAN_AUTO, GameBase.gi.soundindex("weapons/chngnd1a.wav"), 1, Defines.ATTN_IDLE, 0)
                } else {
                    ent.client.weapon_sound = GameBase.gi.soundindex("weapons/chngnl1a.wav")
                }

                ent.client.anim_priority = Defines.ANIM_ATTACK
                if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                    ent.s.frame = M_Player.FRAME_crattak1 - (ent.client.ps.gunframe and 1)
                    ent.client.anim_end = M_Player.FRAME_crattak9
                } else {
                    ent.s.frame = M_Player.FRAME_attack1 - (ent.client.ps.gunframe and 1)
                    ent.client.anim_end = M_Player.FRAME_attack8
                }

                if (ent.client.ps.gunframe <= 9)
                    shots = 1
                else if (ent.client.ps.gunframe <= 14) {
                    if ((ent.client.buttons and Defines.BUTTON_ATTACK) != 0)
                        shots = 2
                    else
                        shots = 1
                } else
                    shots = 3

                if (ent.client.pers.inventory[ent.client.ammo_index] < shots)
                    shots = ent.client.pers.inventory[ent.client.ammo_index]

                if (0 == shots) {
                    if (GameBase.level.time >= ent.pain_debounce_time) {
                        GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("weapons/noammo.wav"), 1, Defines.ATTN_NORM, 0)
                        ent.pain_debounce_time = GameBase.level.time + 1
                    }
                    NoAmmoWeaponChange(ent)
                    return true
                }

                if (is_quad) {
                    damage *= 4
                    kick *= 4
                }

                run {
                    i = 0
                    while (i < 3) {
                        ent.client.kick_origin[i] = Lib.crandom() * 0.35.toFloat()
                        ent.client.kick_angles[i] = Lib.crandom() * 0.7.toFloat()
                        i++
                    }
                }

                run {
                    i = 0
                    while (i < shots) {
                        // get start / end positions
                        Math3D.AngleVectors(ent.client.v_angle, forward, right, up)
                        r = 7 + Lib.crandom() * 4
                        u = Lib.crandom() * 4
                        Math3D.VectorSet(offset, 0, r, u + ent.viewheight - 8)
                        P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

                        GameWeapon.fire_bullet(ent, start, forward, damage, kick, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MOD_CHAINGUN)
                        i++
                    }
                }

                // send muzzle flash
                GameBase.gi.WriteByte(Defines.svc_muzzleflash)

                GameBase.gi.WriteShort(ent.index)
                GameBase.gi.WriteByte((Defines.MZ_CHAINGUN1 + shots - 1) or is_silenced)
                GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

                PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)

                if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                    ent.client.pers.inventory[ent.client.ammo_index] -= shots

                return true
            }
        }

        public var pause_frames: IntArray = intArray(39, 45, 50, 55, 0)

        public var fire_frames: IntArray = intArray(9, 17, 0)

        public var Pickup_Weapon: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "Pickup_Weapon"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                val index: Int
                val ammo: gitem_t

                index = GameItems.ITEM_INDEX(ent.item)

                if ((((GameBase.dmflags.value) as Int and Defines.DF_WEAPONS_STAY) != 0 || GameBase.coop.value != 0) && 0 != other.client.pers.inventory[index]) {
                    if (0 == (ent.spawnflags and (Defines.DROPPED_ITEM or Defines.DROPPED_PLAYER_ITEM)))
                        return false // leave the weapon for others to pickup
                }

                other.client.pers.inventory[index]++

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM)) {
                    // give them some ammo with it
                    ammo = GameItems.FindItem(ent.item.ammo)
                    if ((GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO) != 0)
                        GameItems.Add_Ammo(other, ammo, 1000)
                    else
                        GameItems.Add_Ammo(other, ammo, ammo.quantity)

                    if (0 == (ent.spawnflags and Defines.DROPPED_PLAYER_ITEM)) {
                        if (GameBase.deathmatch.value != 0) {
                            if (((GameBase.dmflags.value) as Int and Defines.DF_WEAPONS_STAY) != 0)
                                ent.flags = ent.flags or Defines.FL_RESPAWN
                            else
                                GameItems.SetRespawn(ent, 30)
                        }
                        if (GameBase.coop.value != 0)
                            ent.flags = ent.flags or Defines.FL_RESPAWN
                    }
                }

                if (other.client.pers.weapon != ent.item && (other.client.pers.inventory[index] == 1) && (0 == GameBase.deathmatch.value || other.client.pers.weapon == GameItems.FindItem("blaster")))
                    other.client.newweapon = ent.item

                return true
            }
        }

        public fun P_ProjectSource(client: gclient_t, point: FloatArray, distance: FloatArray, forward: FloatArray, right: FloatArray, result: FloatArray) {
            val _distance = floatArray(0.0, 0.0, 0.0)

            Math3D.VectorCopy(distance, _distance)
            if (client.pers.hand == Defines.LEFT_HANDED)
                _distance[1] *= (-1).toFloat()
            else if (client.pers.hand == Defines.CENTER_HANDED)
                _distance[1] = 0
            Math3D.G_ProjectSource(point, _distance, forward, right, result)
        }

        /*
     * =============== 
     * ChangeWeapon
     * 
     * The old weapon has been dropped all the way, so make the new one current
     * ===============
     */
        public fun ChangeWeapon(ent: edict_t) {
            val i: Int

            if (ent.client.grenade_time != 0) {
                ent.client.grenade_time = GameBase.level.time
                ent.client.weapon_sound = 0
                weapon_grenade_fire(ent, false)
                ent.client.grenade_time = 0
            }

            ent.client.pers.lastweapon = ent.client.pers.weapon
            ent.client.pers.weapon = ent.client.newweapon
            ent.client.newweapon = null
            ent.client.machinegun_shots = 0

            // set visible model
            if (ent.s.modelindex == 255) {
                if (ent.client.pers.weapon != null)
                    i = ((ent.client.pers.weapon.weapmodel and 255) shl 8)
                else
                    i = 0
                ent.s.skinnum = (ent.index - 1) or i
            }

            if (ent.client.pers.weapon != null && ent.client.pers.weapon.ammo != null)

                ent.client.ammo_index = GameItems.ITEM_INDEX(GameItems.FindItem(ent.client.pers.weapon.ammo))
            else
                ent.client.ammo_index = 0

            if (ent.client.pers.weapon == null) {
                // dead
                ent.client.ps.gunindex = 0
                return
            }

            ent.client.weaponstate = Defines.WEAPON_ACTIVATING
            ent.client.ps.gunframe = 0
            ent.client.ps.gunindex = GameBase.gi.modelindex(ent.client.pers.weapon.view_model)

            ent.client.anim_priority = Defines.ANIM_PAIN
            if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                ent.s.frame = M_Player.FRAME_crpain1
                ent.client.anim_end = M_Player.FRAME_crpain4
            } else {
                ent.s.frame = M_Player.FRAME_pain301
                ent.client.anim_end = M_Player.FRAME_pain304

            }
        }

        /*
     * ================= 
     * NoAmmoWeaponChange 
     * =================
     */
        public fun NoAmmoWeaponChange(ent: edict_t) {
            if (0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("slugs"))] && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("railgun"))]) {
                ent.client.newweapon = GameItems.FindItem("railgun")
                return
            }
            if (0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("cells"))] && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("hyperblaster"))]) {
                ent.client.newweapon = GameItems.FindItem("hyperblaster")
                return
            }
            if (0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("bullets"))] && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("chaingun"))]) {
                ent.client.newweapon = GameItems.FindItem("chaingun")
                return
            }
            if (0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("bullets"))] && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("machinegun"))]) {
                ent.client.newweapon = GameItems.FindItem("machinegun")
                return
            }
            if (ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("shells"))] > 1 && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("super shotgun"))]) {
                ent.client.newweapon = GameItems.FindItem("super shotgun")
                return
            }
            if (0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("shells"))] && 0 != ent.client.pers.inventory[GameItems.ITEM_INDEX(GameItems.FindItem("shotgun"))]) {
                ent.client.newweapon = GameItems.FindItem("shotgun")
                return
            }
            ent.client.newweapon = GameItems.FindItem("blaster")
        }

        /*
     * ================= 
     * Think_Weapon
     * 
     * Called by ClientBeginServerFrame and ClientThink 
     * =================
     */
        public fun Think_Weapon(ent: edict_t) {
            // if just died, put the weapon away
            if (ent.health < 1) {
                ent.client.newweapon = null
                ChangeWeapon(ent)
            }

            // call active weapon think routine
            if (null != ent.client.pers.weapon && null != ent.client.pers.weapon.weaponthink) {
                is_quad = (ent.client.quad_framenum > GameBase.level.framenum)
                if (ent.client.silencer_shots != 0)
                    is_silenced = Defines.MZ_SILENCED as Byte
                else
                    is_silenced = 0
                ent.client.pers.weapon.weaponthink.think(ent)
            }
        }

        /*
     * ================ 
     * Weapon_Generic
     * 
     * A generic function to handle the basics of weapon thinking
     * ================
     */

        public fun Weapon_Generic(ent: edict_t, FRAME_ACTIVATE_LAST: Int, FRAME_FIRE_LAST: Int, FRAME_IDLE_LAST: Int, FRAME_DEACTIVATE_LAST: Int, pause_frames: IntArray?, fire_frames: IntArray, fire: EntThinkAdapter) {
            val FRAME_FIRE_FIRST = (FRAME_ACTIVATE_LAST + 1)
            val FRAME_IDLE_FIRST = (FRAME_FIRE_LAST + 1)
            val FRAME_DEACTIVATE_FIRST = (FRAME_IDLE_LAST + 1)

            var n: Int

            if (ent.deadflag != 0 || ent.s.modelindex != 255)
            // VWep animations
            // screw up corpses
            {
                return
            }

            if (ent.client.weaponstate == Defines.WEAPON_DROPPING) {
                if (ent.client.ps.gunframe == FRAME_DEACTIVATE_LAST) {
                    ChangeWeapon(ent)
                    return
                } else if ((FRAME_DEACTIVATE_LAST - ent.client.ps.gunframe) == 4) {
                    ent.client.anim_priority = Defines.ANIM_REVERSE
                    if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                        ent.s.frame = M_Player.FRAME_crpain4 + 1
                        ent.client.anim_end = M_Player.FRAME_crpain1
                    } else {
                        ent.s.frame = M_Player.FRAME_pain304 + 1
                        ent.client.anim_end = M_Player.FRAME_pain301
                    }
                }

                ent.client.ps.gunframe++
                return
            }

            if (ent.client.weaponstate == Defines.WEAPON_ACTIVATING) {
                if (ent.client.ps.gunframe == FRAME_ACTIVATE_LAST) {
                    ent.client.weaponstate = Defines.WEAPON_READY
                    ent.client.ps.gunframe = FRAME_IDLE_FIRST
                    return
                }

                ent.client.ps.gunframe++
                return
            }

            if ((ent.client.newweapon != null) && (ent.client.weaponstate != Defines.WEAPON_FIRING)) {
                ent.client.weaponstate = Defines.WEAPON_DROPPING
                ent.client.ps.gunframe = FRAME_DEACTIVATE_FIRST

                if ((FRAME_DEACTIVATE_LAST - FRAME_DEACTIVATE_FIRST) < 4) {
                    ent.client.anim_priority = Defines.ANIM_REVERSE
                    if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                        ent.s.frame = M_Player.FRAME_crpain4 + 1
                        ent.client.anim_end = M_Player.FRAME_crpain1
                    } else {
                        ent.s.frame = M_Player.FRAME_pain304 + 1
                        ent.client.anim_end = M_Player.FRAME_pain301

                    }
                }
                return
            }

            if (ent.client.weaponstate == Defines.WEAPON_READY) {
                if (((ent.client.latched_buttons or ent.client.buttons) and Defines.BUTTON_ATTACK) != 0) {
                    ent.client.latched_buttons = ent.client.latched_buttons and Defines.BUTTON_ATTACK.inv()
                    if ((0 == ent.client.ammo_index) || (ent.client.pers.inventory[ent.client.ammo_index] >= ent.client.pers.weapon.quantity)) {
                        ent.client.ps.gunframe = FRAME_FIRE_FIRST
                        ent.client.weaponstate = Defines.WEAPON_FIRING

                        // start the animation
                        ent.client.anim_priority = Defines.ANIM_ATTACK
                        if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                            ent.s.frame = M_Player.FRAME_crattak1 - 1
                            ent.client.anim_end = M_Player.FRAME_crattak9
                        } else {
                            ent.s.frame = M_Player.FRAME_attack1 - 1
                            ent.client.anim_end = M_Player.FRAME_attack8
                        }
                    } else {
                        if (GameBase.level.time >= ent.pain_debounce_time) {
                            GameBase.gi.sound(ent, Defines.CHAN_VOICE, GameBase.gi.soundindex("weapons/noammo.wav"), 1, Defines.ATTN_NORM, 0)
                            ent.pain_debounce_time = GameBase.level.time + 1
                        }
                        NoAmmoWeaponChange(ent)
                    }
                } else {
                    if (ent.client.ps.gunframe == FRAME_IDLE_LAST) {
                        ent.client.ps.gunframe = FRAME_IDLE_FIRST
                        return
                    }

                    if (pause_frames != null) {
                        run {
                            n = 0
                            while (pause_frames[n] != 0) {
                                if (ent.client.ps.gunframe == pause_frames[n]) {
                                    if ((Lib.rand() and 15) != 0)
                                        return
                                }
                                n++
                            }
                        }
                    }

                    ent.client.ps.gunframe++
                    return
                }
            }

            if (ent.client.weaponstate == Defines.WEAPON_FIRING) {
                run {
                    n = 0
                    while (fire_frames[n] != 0) {
                        if (ent.client.ps.gunframe == fire_frames[n]) {
                            if (ent.client.quad_framenum > GameBase.level.framenum)
                                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage3.wav"), 1, Defines.ATTN_NORM, 0)

                            fire.think(ent)
                            break
                        }
                        n++
                    }
                }

                if (0 == fire_frames[n])
                    ent.client.ps.gunframe++

                if (ent.client.ps.gunframe == FRAME_IDLE_FIRST + 1)
                    ent.client.weaponstate = Defines.WEAPON_READY
            }
        }

        /*
     * ======================================================================
     * 
     * GRENADE
     * 
     * ======================================================================
     */

        public fun weapon_grenade_fire(ent: edict_t, held: Boolean) {
            val offset = floatArray(0.0, 0.0, 0.0)
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            val start = floatArray(0.0, 0.0, 0.0)
            var damage = 125
            val timer: Float
            val speed: Int
            val radius: Float

            radius = (damage + 40).toFloat()
            if (is_quad)
                damage *= 4

            Math3D.VectorSet(offset, 8, 8, ent.viewheight - 8)
            Math3D.AngleVectors(ent.client.v_angle, forward, right, null)
            P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

            timer = ent.client.grenade_time - GameBase.level.time
            speed = (Defines.GRENADE_MINSPEED + (Defines.GRENADE_TIMER - timer) * ((Defines.GRENADE_MAXSPEED - Defines.GRENADE_MINSPEED) / Defines.GRENADE_TIMER)) as Int
            GameWeapon.fire_grenade2(ent, start, forward, damage, speed, timer, radius, held)

            if (0 == (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO))
                ent.client.pers.inventory[ent.client.ammo_index]--

            ent.client.grenade_time = GameBase.level.time + 1.0.toFloat()

            if (ent.deadflag != 0 || ent.s.modelindex != 255)
            // VWep animations
            // screw up corpses
            {
                return
            }

            if (ent.health <= 0)
                return

            if ((ent.client.ps.pmove.pm_flags and pmove_t.PMF_DUCKED) != 0) {
                ent.client.anim_priority = Defines.ANIM_ATTACK
                ent.s.frame = M_Player.FRAME_crattak1 - 1
                ent.client.anim_end = M_Player.FRAME_crattak3
            } else {
                ent.client.anim_priority = Defines.ANIM_REVERSE
                ent.s.frame = M_Player.FRAME_wave08
                ent.client.anim_end = M_Player.FRAME_wave01
            }
        }

        /*
     * ======================================================================
     * 
     * BLASTER / HYPERBLASTER
     * 
     * ======================================================================
     */

        public fun Blaster_Fire(ent: edict_t, g_offset: FloatArray, damage: Int, hyper: Boolean, effect: Int) {
            var damage = damage
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            val start = floatArray(0.0, 0.0, 0.0)
            val offset = floatArray(0.0, 0.0, 0.0)

            if (is_quad)
                damage *= 4
            Math3D.AngleVectors(ent.client.v_angle, forward, right, null)
            Math3D.VectorSet(offset, 24, 8, ent.viewheight - 8)
            Math3D.VectorAdd(offset, g_offset, offset)
            P_ProjectSource(ent.client, ent.s.origin, offset, forward, right, start)

            Math3D.VectorScale(forward, -2, ent.client.kick_origin)
            ent.client.kick_angles[0] = -1

            GameWeapon.fire_blaster(ent, start, forward, damage, 1000, effect, hyper)

            // send muzzle flash
            GameBase.gi.WriteByte(Defines.svc_muzzleflash)
            GameBase.gi.WriteShort(ent.index)
            if (hyper)
                GameBase.gi.WriteByte(Defines.MZ_HYPERBLASTER or is_silenced)
            else
                GameBase.gi.WriteByte(Defines.MZ_BLASTER or is_silenced)
            GameBase.gi.multicast(ent.s.origin, Defines.MULTICAST_PVS)

            PlayerWeapon.PlayerNoise(ent, start, Defines.PNOISE_WEAPON)
        }

        /*
     * =============== 
     * PlayerNoise
     * 
     * Each player can have two noise objects associated with it: a personal
     * noise (jumping, pain, weapon firing), and a weapon target noise (bullet
     * wall impacts)
     * 
     * Monsters that don't directly see the player can move to a noise in hopes
     * of seeing the player from there. 
     * ===============
     */
        fun PlayerNoise(who: edict_t, where: FloatArray, type: Int) {
            var noise: edict_t

            if (type == Defines.PNOISE_WEAPON) {
                if (who.client.silencer_shots > 0) {
                    who.client.silencer_shots--
                    return
                }
            }

            if (GameBase.deathmatch.value != 0)
                return

            if ((who.flags and Defines.FL_NOTARGET) != 0)
                return

            if (who.mynoise == null) {
                noise = GameUtil.G_Spawn()
                noise.classname = "player_noise"
                Math3D.VectorSet(noise.mins, -8, -8, -8)
                Math3D.VectorSet(noise.maxs, 8, 8, 8)
                noise.owner = who
                noise.svflags = Defines.SVF_NOCLIENT
                who.mynoise = noise

                noise = GameUtil.G_Spawn()
                noise.classname = "player_noise"
                Math3D.VectorSet(noise.mins, -8, -8, -8)
                Math3D.VectorSet(noise.maxs, 8, 8, 8)
                noise.owner = who
                noise.svflags = Defines.SVF_NOCLIENT
                who.mynoise2 = noise
            }

            if (type == Defines.PNOISE_SELF || type == Defines.PNOISE_WEAPON) {
                noise = who.mynoise
                GameBase.level.sound_entity = noise
                GameBase.level.sound_entity_framenum = GameBase.level.framenum
            } else
            // type == PNOISE_IMPACT
            {
                noise = who.mynoise2
                GameBase.level.sound2_entity = noise
                GameBase.level.sound2_entity_framenum = GameBase.level.framenum
            }

            Math3D.VectorCopy(where, noise.s.origin)
            Math3D.VectorSubtract(where, noise.maxs, noise.absmin)
            Math3D.VectorAdd(where, noise.maxs, noise.absmax)
            noise.teleport_time = GameBase.level.time
            GameBase.gi.linkentity(noise)
        }
    }
}