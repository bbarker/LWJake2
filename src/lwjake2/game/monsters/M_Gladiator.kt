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

package lwjake2.game.monsters

import lwjake2.Defines
import lwjake2.game.EntDieAdapter
import lwjake2.game.EntInteractAdapter
import lwjake2.game.EntPainAdapter
import lwjake2.game.EntThinkAdapter
import lwjake2.game.GameAI
import lwjake2.game.GameBase
import lwjake2.game.GameMisc
import lwjake2.game.GameUtil
import lwjake2.game.GameWeapon
import lwjake2.game.Monster
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.game.monsters.M_Flash
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Gladiator {
    companion object {

        //	This file generated by ModelGen - Do NOT Modify

        public val FRAME_stand1: Int = 0

        public val FRAME_stand2: Int = 1

        public val FRAME_stand3: Int = 2

        public val FRAME_stand4: Int = 3

        public val FRAME_stand5: Int = 4

        public val FRAME_stand6: Int = 5

        public val FRAME_stand7: Int = 6

        public val FRAME_walk1: Int = 7

        public val FRAME_walk2: Int = 8

        public val FRAME_walk3: Int = 9

        public val FRAME_walk4: Int = 10

        public val FRAME_walk5: Int = 11

        public val FRAME_walk6: Int = 12

        public val FRAME_walk7: Int = 13

        public val FRAME_walk8: Int = 14

        public val FRAME_walk9: Int = 15

        public val FRAME_walk10: Int = 16

        public val FRAME_walk11: Int = 17

        public val FRAME_walk12: Int = 18

        public val FRAME_walk13: Int = 19

        public val FRAME_walk14: Int = 20

        public val FRAME_walk15: Int = 21

        public val FRAME_walk16: Int = 22

        public val FRAME_run1: Int = 23

        public val FRAME_run2: Int = 24

        public val FRAME_run3: Int = 25

        public val FRAME_run4: Int = 26

        public val FRAME_run5: Int = 27

        public val FRAME_run6: Int = 28

        public val FRAME_melee1: Int = 29

        public val FRAME_melee2: Int = 30

        public val FRAME_melee3: Int = 31

        public val FRAME_melee4: Int = 32

        public val FRAME_melee5: Int = 33

        public val FRAME_melee6: Int = 34

        public val FRAME_melee7: Int = 35

        public val FRAME_melee8: Int = 36

        public val FRAME_melee9: Int = 37

        public val FRAME_melee10: Int = 38

        public val FRAME_melee11: Int = 39

        public val FRAME_melee12: Int = 40

        public val FRAME_melee13: Int = 41

        public val FRAME_melee14: Int = 42

        public val FRAME_melee15: Int = 43

        public val FRAME_melee16: Int = 44

        public val FRAME_melee17: Int = 45

        public val FRAME_attack1: Int = 46

        public val FRAME_attack2: Int = 47

        public val FRAME_attack3: Int = 48

        public val FRAME_attack4: Int = 49

        public val FRAME_attack5: Int = 50

        public val FRAME_attack6: Int = 51

        public val FRAME_attack7: Int = 52

        public val FRAME_attack8: Int = 53

        public val FRAME_attack9: Int = 54

        public val FRAME_pain1: Int = 55

        public val FRAME_pain2: Int = 56

        public val FRAME_pain3: Int = 57

        public val FRAME_pain4: Int = 58

        public val FRAME_pain5: Int = 59

        public val FRAME_pain6: Int = 60

        public val FRAME_death1: Int = 61

        public val FRAME_death2: Int = 62

        public val FRAME_death3: Int = 63

        public val FRAME_death4: Int = 64

        public val FRAME_death5: Int = 65

        public val FRAME_death6: Int = 66

        public val FRAME_death7: Int = 67

        public val FRAME_death8: Int = 68

        public val FRAME_death9: Int = 69

        public val FRAME_death10: Int = 70

        public val FRAME_death11: Int = 71

        public val FRAME_death12: Int = 72

        public val FRAME_death13: Int = 73

        public val FRAME_death14: Int = 74

        public val FRAME_death15: Int = 75

        public val FRAME_death16: Int = 76

        public val FRAME_death17: Int = 77

        public val FRAME_death18: Int = 78

        public val FRAME_death19: Int = 79

        public val FRAME_death20: Int = 80

        public val FRAME_death21: Int = 81

        public val FRAME_death22: Int = 82

        public val FRAME_painup1: Int = 83

        public val FRAME_painup2: Int = 84

        public val FRAME_painup3: Int = 85

        public val FRAME_painup4: Int = 86

        public val FRAME_painup5: Int = 87

        public val FRAME_painup6: Int = 88

        public val FRAME_painup7: Int = 89

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_die: Int = 0

        var sound_gun: Int = 0

        var sound_cleaver_swing: Int = 0

        var sound_cleaver_hit: Int = 0

        var sound_cleaver_miss: Int = 0

        var sound_idle: Int = 0

        var sound_search: Int = 0

        var sound_sight: Int = 0

        var gladiator_idle: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_idle"
            }

            public fun think(self: edict_t): Boolean {

                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_idle, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var gladiator_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "gladiator_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {

                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var gladiator_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_search"
            }

            public fun think(self: edict_t): Boolean {

                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var gladiator_cleaver_swing: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_cleaver_swing"
            }

            public fun think(self: edict_t): Boolean {

                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_cleaver_swing, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var gladiator_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var gladiator_move_stand = mmove_t(FRAME_stand1, FRAME_stand7, gladiator_frames_stand, null)

        var gladiator_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_stand"
            }

            public fun think(self: edict_t): Boolean {

                self.monsterinfo.currentmove = gladiator_move_stand
                return true
            }
        }

        var gladiator_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 15, null), mframe_t(GameAI.ai_walk, 7, null), mframe_t(GameAI.ai_walk, 6, null), mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 2, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 2, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 12, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 2, null), mframe_t(GameAI.ai_walk, 2, null), mframe_t(GameAI.ai_walk, 1, null), mframe_t(GameAI.ai_walk, 8, null))

        var gladiator_move_walk = mmove_t(FRAME_walk1, FRAME_walk16, gladiator_frames_walk, null)

        var gladiator_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_walk"
            }

            public fun think(self: edict_t): Boolean {

                self.monsterinfo.currentmove = gladiator_move_walk

                return true
            }
        }

        var gladiator_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 23, null), mframe_t(GameAI.ai_run, 14, null), mframe_t(GameAI.ai_run, 14, null), mframe_t(GameAI.ai_run, 21, null), mframe_t(GameAI.ai_run, 12, null), mframe_t(GameAI.ai_run, 13, null))

        var gladiator_move_run = mmove_t(FRAME_run1, FRAME_run6, gladiator_frames_run, null)

        var gladiator_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_run"
            }

            public fun think(self: edict_t): Boolean {

                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = gladiator_move_stand
                else
                    self.monsterinfo.currentmove = gladiator_move_run

                return true
            }
        }

        var GaldiatorMelee: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "GaldiatorMelee"
            }

            public fun think(self: edict_t): Boolean {

                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, self.mins[0], -4)
                if (GameWeapon.fire_hit(self, aim, (20 + (Lib.rand() % 5)), 300))
                    GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_cleaver_hit, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_cleaver_miss, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var gladiator_frames_attack_melee = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, gladiator_cleaver_swing), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, GaldiatorMelee), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, gladiator_cleaver_swing), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, GaldiatorMelee), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null))

        var gladiator_move_attack_melee = mmove_t(FRAME_melee1, FRAME_melee17, gladiator_frames_attack_melee, gladiator_run)

        var gladiator_melee: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_melee"
            }

            public fun think(self: edict_t): Boolean {

                self.monsterinfo.currentmove = gladiator_move_attack_melee
                return true
            }
        }

        var GladiatorGun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "GladiatorGun"
            }

            public fun think(self: edict_t): Boolean {

                val start = floatArray(0.0, 0.0, 0.0)

                val dir = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_GLADIATOR_RAILGUN_1], forward, right, start)

                // calc direction to where we targted
                Math3D.VectorSubtract(self.pos1, start, dir)
                Math3D.VectorNormalize(dir)

                Monster.monster_fire_railgun(self, start, dir, 50, 100, Defines.MZ2_GLADIATOR_RAILGUN_1)

                return true
            }
        }

        var gladiator_frames_attack_gun = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, GladiatorGun), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null))

        var gladiator_move_attack_gun = mmove_t(FRAME_attack1, FRAME_attack9, gladiator_frames_attack_gun, gladiator_run)

        var gladiator_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_attack"
            }

            public fun think(self: edict_t): Boolean {

                val range: Float
                val v = floatArray(0.0, 0.0, 0.0)

                // a small safe zone
                Math3D.VectorSubtract(self.s.origin, self.enemy.s.origin, v)
                range = Math3D.VectorLength(v)
                if (range <= (Defines.MELEE_DISTANCE + 32))
                    return true

                // charge up the railgun
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_gun, 1, Defines.ATTN_NORM, 0)
                Math3D.VectorCopy(self.enemy.s.origin, self.pos1)
                //save for aiming the shot
                self.pos1[2] += self.enemy.viewheight
                self.monsterinfo.currentmove = gladiator_move_attack_gun
                return true
            }
        }

        var gladiator_frames_pain = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var gladiator_move_pain = mmove_t(FRAME_pain1, FRAME_pain6, gladiator_frames_pain, gladiator_run)

        var gladiator_frames_pain_air = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var gladiator_move_pain_air = mmove_t(FRAME_painup1, FRAME_painup7, gladiator_frames_pain_air, gladiator_run)

        var gladiator_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "gladiator_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {

                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time) {
                    if ((self.velocity[2] > 100) && (self.monsterinfo.currentmove == gladiator_move_pain))
                        self.monsterinfo.currentmove = gladiator_move_pain_air
                    return
                }

                self.pain_debounce_time = GameBase.level.time + 3

                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)

                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (self.velocity[2] > 100)
                    self.monsterinfo.currentmove = gladiator_move_pain_air
                else
                    self.monsterinfo.currentmove = gladiator_move_pain

            }
        }

        var gladiator_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "gladiator_dead"
            }

            public fun think(self: edict_t): Boolean {

                Math3D.VectorSet(self.mins, -16, -16, -24)
                Math3D.VectorSet(self.maxs, 16, 16, -8)
                self.movetype = Defines.MOVETYPE_TOSS
                self.svflags = self.svflags or Defines.SVF_DEADMONSTER
                self.nextthink = 0
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var gladiator_frames_death = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var gladiator_move_death = mmove_t(FRAME_death1, FRAME_death22, gladiator_frames_death, gladiator_dead)

        var gladiator_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "gladiator_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

                //	check for gib
                if (self.health <= self.gib_health) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, GameBase.gi.soundindex("misc/udeath.wav"), 1, Defines.ATTN_NORM, 0)
                    run {
                        n = 0
                        while (n < 2) {
                            GameMisc.ThrowGib(self, "models/objects/gibs/bone/tris.md2", damage, Defines.GIB_ORGANIC)
                            n++
                        }
                    }
                    run {
                        n = 0
                        while (n < 4) {
                            GameMisc.ThrowGib(self, "models/objects/gibs/sm_meat/tris.md2", damage, Defines.GIB_ORGANIC)
                            n++
                        }
                    }
                    GameMisc.ThrowHead(self, "models/objects/gibs/head2/tris.md2", damage, Defines.GIB_ORGANIC)
                    self.deadflag = Defines.DEAD_DEAD
                    return
                }

                if (self.deadflag == Defines.DEAD_DEAD)
                    return

                //	regular death
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_die, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES

                self.monsterinfo.currentmove = gladiator_move_death

            }
        }

        /*
     * QUAKED monster_gladiator (1 .5 0) (-32 -32 -24) (32 32 64) Ambush
     * Trigger_Spawn Sight
     */
        public fun SP_monster_gladiator(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_pain1 = GameBase.gi.soundindex("gladiator/pain.wav")
            sound_pain2 = GameBase.gi.soundindex("gladiator/gldpain2.wav")
            sound_die = GameBase.gi.soundindex("gladiator/glddeth2.wav")
            sound_gun = GameBase.gi.soundindex("gladiator/railgun.wav")
            sound_cleaver_swing = GameBase.gi.soundindex("gladiator/melee1.wav")
            sound_cleaver_hit = GameBase.gi.soundindex("gladiator/melee2.wav")
            sound_cleaver_miss = GameBase.gi.soundindex("gladiator/melee3.wav")
            sound_idle = GameBase.gi.soundindex("gladiator/gldidle1.wav")
            sound_search = GameBase.gi.soundindex("gladiator/gldsrch1.wav")
            sound_sight = GameBase.gi.soundindex("gladiator/sight.wav")

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/gladiatr/tris.md2")
            Math3D.VectorSet(self.mins, -32, -32, -24)
            Math3D.VectorSet(self.maxs, 32, 32, 64)

            self.health = 400
            self.gib_health = -175
            self.mass = 400

            self.pain = gladiator_pain
            self.die = gladiator_die

            self.monsterinfo.stand = gladiator_stand
            self.monsterinfo.walk = gladiator_walk
            self.monsterinfo.run = gladiator_run
            self.monsterinfo.dodge = null
            self.monsterinfo.attack = gladiator_attack
            self.monsterinfo.melee = gladiator_melee
            self.monsterinfo.sight = gladiator_sight
            self.monsterinfo.idle = gladiator_idle
            self.monsterinfo.search = gladiator_search

            GameBase.gi.linkentity(self)
            self.monsterinfo.currentmove = gladiator_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.walkmonster_start.think(self)
        }
    }
}