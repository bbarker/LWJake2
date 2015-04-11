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
import lwjake2.game.EntPainAdapter
import lwjake2.game.EntThinkAdapter
import lwjake2.game.GameAI
import lwjake2.game.GameBase
import lwjake2.game.GameUtil
import lwjake2.game.Monster
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.game.trace_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Boss2 {
    companion object {

        public val FRAME_stand30: Int = 0

        public val FRAME_stand31: Int = 1

        public val FRAME_stand32: Int = 2

        public val FRAME_stand33: Int = 3

        public val FRAME_stand34: Int = 4

        public val FRAME_stand35: Int = 5

        public val FRAME_stand36: Int = 6

        public val FRAME_stand37: Int = 7

        public val FRAME_stand38: Int = 8

        public val FRAME_stand39: Int = 9

        public val FRAME_stand40: Int = 10

        public val FRAME_stand41: Int = 11

        public val FRAME_stand42: Int = 12

        public val FRAME_stand43: Int = 13

        public val FRAME_stand44: Int = 14

        public val FRAME_stand45: Int = 15

        public val FRAME_stand46: Int = 16

        public val FRAME_stand47: Int = 17

        public val FRAME_stand48: Int = 18

        public val FRAME_stand49: Int = 19

        public val FRAME_stand50: Int = 20

        public val FRAME_stand1: Int = 21

        public val FRAME_stand2: Int = 22

        public val FRAME_stand3: Int = 23

        public val FRAME_stand4: Int = 24

        public val FRAME_stand5: Int = 25

        public val FRAME_stand6: Int = 26

        public val FRAME_stand7: Int = 27

        public val FRAME_stand8: Int = 28

        public val FRAME_stand9: Int = 29

        public val FRAME_stand10: Int = 30

        public val FRAME_stand11: Int = 31

        public val FRAME_stand12: Int = 32

        public val FRAME_stand13: Int = 33

        public val FRAME_stand14: Int = 34

        public val FRAME_stand15: Int = 35

        public val FRAME_stand16: Int = 36

        public val FRAME_stand17: Int = 37

        public val FRAME_stand18: Int = 38

        public val FRAME_stand19: Int = 39

        public val FRAME_stand20: Int = 40

        public val FRAME_stand21: Int = 41

        public val FRAME_stand22: Int = 42

        public val FRAME_stand23: Int = 43

        public val FRAME_stand24: Int = 44

        public val FRAME_stand25: Int = 45

        public val FRAME_stand26: Int = 46

        public val FRAME_stand27: Int = 47

        public val FRAME_stand28: Int = 48

        public val FRAME_stand29: Int = 49

        public val FRAME_walk1: Int = 50

        public val FRAME_walk2: Int = 51

        public val FRAME_walk3: Int = 52

        public val FRAME_walk4: Int = 53

        public val FRAME_walk5: Int = 54

        public val FRAME_walk6: Int = 55

        public val FRAME_walk7: Int = 56

        public val FRAME_walk8: Int = 57

        public val FRAME_walk9: Int = 58

        public val FRAME_walk10: Int = 59

        public val FRAME_walk11: Int = 60

        public val FRAME_walk12: Int = 61

        public val FRAME_walk13: Int = 62

        public val FRAME_walk14: Int = 63

        public val FRAME_walk15: Int = 64

        public val FRAME_walk16: Int = 65

        public val FRAME_walk17: Int = 66

        public val FRAME_walk18: Int = 67

        public val FRAME_walk19: Int = 68

        public val FRAME_walk20: Int = 69

        public val FRAME_attack1: Int = 70

        public val FRAME_attack2: Int = 71

        public val FRAME_attack3: Int = 72

        public val FRAME_attack4: Int = 73

        public val FRAME_attack5: Int = 74

        public val FRAME_attack6: Int = 75

        public val FRAME_attack7: Int = 76

        public val FRAME_attack8: Int = 77

        public val FRAME_attack9: Int = 78

        public val FRAME_attack10: Int = 79

        public val FRAME_attack11: Int = 80

        public val FRAME_attack12: Int = 81

        public val FRAME_attack13: Int = 82

        public val FRAME_attack14: Int = 83

        public val FRAME_attack15: Int = 84

        public val FRAME_attack16: Int = 85

        public val FRAME_attack17: Int = 86

        public val FRAME_attack18: Int = 87

        public val FRAME_attack19: Int = 88

        public val FRAME_attack20: Int = 89

        public val FRAME_attack21: Int = 90

        public val FRAME_attack22: Int = 91

        public val FRAME_attack23: Int = 92

        public val FRAME_attack24: Int = 93

        public val FRAME_attack25: Int = 94

        public val FRAME_attack26: Int = 95

        public val FRAME_attack27: Int = 96

        public val FRAME_attack28: Int = 97

        public val FRAME_attack29: Int = 98

        public val FRAME_attack30: Int = 99

        public val FRAME_attack31: Int = 100

        public val FRAME_attack32: Int = 101

        public val FRAME_attack33: Int = 102

        public val FRAME_attack34: Int = 103

        public val FRAME_attack35: Int = 104

        public val FRAME_attack36: Int = 105

        public val FRAME_attack37: Int = 106

        public val FRAME_attack38: Int = 107

        public val FRAME_attack39: Int = 108

        public val FRAME_attack40: Int = 109

        public val FRAME_pain2: Int = 110

        public val FRAME_pain3: Int = 111

        public val FRAME_pain4: Int = 112

        public val FRAME_pain5: Int = 113

        public val FRAME_pain6: Int = 114

        public val FRAME_pain7: Int = 115

        public val FRAME_pain8: Int = 116

        public val FRAME_pain9: Int = 117

        public val FRAME_pain10: Int = 118

        public val FRAME_pain11: Int = 119

        public val FRAME_pain12: Int = 120

        public val FRAME_pain13: Int = 121

        public val FRAME_pain14: Int = 122

        public val FRAME_pain15: Int = 123

        public val FRAME_pain16: Int = 124

        public val FRAME_pain17: Int = 125

        public val FRAME_pain18: Int = 126

        public val FRAME_pain19: Int = 127

        public val FRAME_pain20: Int = 128

        public val FRAME_pain21: Int = 129

        public val FRAME_pain22: Int = 130

        public val FRAME_pain23: Int = 131

        public val FRAME_death2: Int = 132

        public val FRAME_death3: Int = 133

        public val FRAME_death4: Int = 134

        public val FRAME_death5: Int = 135

        public val FRAME_death6: Int = 136

        public val FRAME_death7: Int = 137

        public val FRAME_death8: Int = 138

        public val FRAME_death9: Int = 139

        public val FRAME_death10: Int = 140

        public val FRAME_death11: Int = 141

        public val FRAME_death12: Int = 142

        public val FRAME_death13: Int = 143

        public val FRAME_death14: Int = 144

        public val FRAME_death15: Int = 145

        public val FRAME_death16: Int = 146

        public val FRAME_death17: Int = 147

        public val FRAME_death18: Int = 148

        public val FRAME_death19: Int = 149

        public val FRAME_death20: Int = 150

        public val FRAME_death21: Int = 151

        public val FRAME_death22: Int = 152

        public val FRAME_death23: Int = 153

        public val FRAME_death24: Int = 154

        public val FRAME_death25: Int = 155

        public val FRAME_death26: Int = 156

        public val FRAME_death27: Int = 157

        public val FRAME_death28: Int = 158

        public val FRAME_death29: Int = 159

        public val FRAME_death30: Int = 160

        public val FRAME_death31: Int = 161

        public val FRAME_death32: Int = 162

        public val FRAME_death33: Int = 163

        public val FRAME_death34: Int = 164

        public val FRAME_death35: Int = 165

        public val FRAME_death36: Int = 166

        public val FRAME_death37: Int = 167

        public val FRAME_death38: Int = 168

        public val FRAME_death39: Int = 169

        public val FRAME_death40: Int = 170

        public val FRAME_death41: Int = 171

        public val FRAME_death42: Int = 172

        public val FRAME_death43: Int = 173

        public val FRAME_death44: Int = 174

        public val FRAME_death45: Int = 175

        public val FRAME_death46: Int = 176

        public val FRAME_death47: Int = 177

        public val FRAME_death48: Int = 178

        public val FRAME_death49: Int = 179

        public val FRAME_death50: Int = 180

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_pain3: Int = 0

        var sound_death: Int = 0

        var sound_search1: Int = 0

        var boss2_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = boss2_move_stand
                return true
            }
        }

        var boss2_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = boss2_move_stand
                else
                    self.monsterinfo.currentmove = boss2_move_run
                return true
            }
        }

        var boss2_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = boss2_move_stand

                self.monsterinfo.currentmove = boss2_move_walk
                return true
            }
        }

        var boss2_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_attack"
            }

            public fun think(self: edict_t): Boolean {
                val vec = floatArray(0.0, 0.0, 0.0)

                val range: Float

                Math3D.VectorSubtract(self.enemy.s.origin, self.s.origin, vec)
                range = Math3D.VectorLength(vec)

                if (range <= 125) {
                    self.monsterinfo.currentmove = boss2_move_attack_pre_mg
                } else {
                    if (Lib.random() <= 0.6)
                        self.monsterinfo.currentmove = boss2_move_attack_pre_mg
                    else
                        self.monsterinfo.currentmove = boss2_move_attack_rocket
                }
                return true
            }
        }

        var boss2_attack_mg: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_attack_mg"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = boss2_move_attack_mg
                return true
            }
        }

        var boss2_reattack_mg: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_reattack_mg"
            }

            public fun think(self: edict_t): Boolean {
                if (GameUtil.infront(self, self.enemy))
                    if (Lib.random() <= 0.7)
                        self.monsterinfo.currentmove = boss2_move_attack_mg
                    else
                        self.monsterinfo.currentmove = boss2_move_attack_post_mg
                else
                    self.monsterinfo.currentmove = boss2_move_attack_post_mg
                return true
            }
        }

        var boss2_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "boss2_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3
                //	   American wanted these at no attenuation
                if (damage < 10) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain3, 1, Defines.ATTN_NONE, 0)
                    self.monsterinfo.currentmove = boss2_move_pain_light
                } else if (damage < 30) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NONE, 0)
                    self.monsterinfo.currentmove = boss2_move_pain_light
                } else {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NONE, 0)
                    self.monsterinfo.currentmove = boss2_move_pain_heavy
                }
            }
        }

        var boss2_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_dead"
            }

            public fun think(self: edict_t): Boolean {
                Math3D.VectorSet(self.mins, -56, -56, 0)
                Math3D.VectorSet(self.maxs, 56, 56, 80)
                self.movetype = Defines.MOVETYPE_TOSS
                self.svflags = self.svflags or Defines.SVF_DEADMONSTER
                self.nextthink = 0
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var boss2_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "boss2_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death, 1, Defines.ATTN_NONE, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_NO
                self.count = 0
                self.monsterinfo.currentmove = boss2_move_death

            }
        }

        var Boss2_CheckAttack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Boss2_CheckAttack"
            }

            public fun think(self: edict_t): Boolean {
                val spot1 = floatArray(0.0, 0.0, 0.0)
                val spot2 = floatArray(0.0, 0.0, 0.0)
                val temp = floatArray(0.0, 0.0, 0.0)
                val chance: Float
                val tr: trace_t
                val enemy_range: Int
                val enemy_yaw: Float

                if (self.enemy.health > 0) {
                    // see if any entities are in the way of the shot
                    Math3D.VectorCopy(self.s.origin, spot1)
                    spot1[2] += self.viewheight
                    Math3D.VectorCopy(self.enemy.s.origin, spot2)
                    spot2[2] += self.enemy.viewheight

                    tr = GameBase.gi.trace(spot1, null, null, spot2, self, Defines.CONTENTS_SOLID or Defines.CONTENTS_MONSTER or Defines.CONTENTS_SLIME or Defines.CONTENTS_LAVA)

                    // do we have a clear shot?
                    if (tr.ent != self.enemy)
                        return false
                }

                enemy_range = GameUtil.range(self, self.enemy)
                Math3D.VectorSubtract(self.enemy.s.origin, self.s.origin, temp)
                enemy_yaw = Math3D.vectoyaw(temp)

                self.ideal_yaw = enemy_yaw

                // melee attack
                if (enemy_range == Defines.RANGE_MELEE) {
                    if (self.monsterinfo.melee != null)
                        self.monsterinfo.attack_state = Defines.AS_MELEE
                    else
                        self.monsterinfo.attack_state = Defines.AS_MISSILE
                    return true
                }

                //	   missile attack
                if (self.monsterinfo.attack == null)
                    return false

                if (GameBase.level.time < self.monsterinfo.attack_finished)
                    return false

                if (enemy_range == Defines.RANGE_FAR)
                    return false

                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0) {
                    chance = 0.4.toFloat()
                } else if (enemy_range == Defines.RANGE_MELEE) {
                    chance = 0.8.toFloat()
                } else if (enemy_range == Defines.RANGE_NEAR) {
                    chance = 0.8.toFloat()
                } else if (enemy_range == Defines.RANGE_MID) {
                    chance = 0.8.toFloat()
                } else {
                    return false
                }

                if (Lib.random() < chance) {
                    self.monsterinfo.attack_state = Defines.AS_MISSILE
                    self.monsterinfo.attack_finished = GameBase.level.time + 2 * Lib.random()
                    return true
                }

                if ((self.flags and Defines.FL_FLY) != 0) {
                    if (Lib.random() < 0.3)
                        self.monsterinfo.attack_state = Defines.AS_SLIDING
                    else
                        self.monsterinfo.attack_state = Defines.AS_STRAIGHT
                }

                return false
            }
        }

        var boss2_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_search"
            }

            public fun think(self: edict_t): Boolean {
                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search1, 1, Defines.ATTN_NONE, 0)
                return true
            }
        }

        var Boss2Rocket: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Boss2Rocket"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val vec = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)

                //	  1
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_ROCKET_1], forward, right, start)
                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                Monster.monster_fire_rocket(self, start, dir, 50, 500, Defines.MZ2_BOSS2_ROCKET_1)

                //	  2
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_ROCKET_2], forward, right, start)
                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                Monster.monster_fire_rocket(self, start, dir, 50, 500, Defines.MZ2_BOSS2_ROCKET_2)

                //	  3
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_ROCKET_3], forward, right, start)
                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                Monster.monster_fire_rocket(self, start, dir, 50, 500, Defines.MZ2_BOSS2_ROCKET_3)

                //	  4
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_ROCKET_4], forward, right, start)
                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                Monster.monster_fire_rocket(self, start, dir, 50, 500, Defines.MZ2_BOSS2_ROCKET_4)
                return true
            }
        }

        var boss2_firebullet_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_firebullet_right"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val target = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_MACHINEGUN_R1], forward, right, start)

                Math3D.VectorMA(self.enemy.s.origin, -0.2.toFloat(), self.enemy.velocity, target)
                target[2] += self.enemy.viewheight
                Math3D.VectorSubtract(target, start, forward)
                Math3D.VectorNormalize(forward)

                Monster.monster_fire_bullet(self, start, forward, 6, 4, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MZ2_BOSS2_MACHINEGUN_R1)

                return true
            }
        }

        var boss2_firebullet_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "boss2_firebullet_left"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val target = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_BOSS2_MACHINEGUN_L1], forward, right, start)

                Math3D.VectorMA(self.enemy.s.origin, -0.2.toFloat(), self.enemy.velocity, target)

                target[2] += self.enemy.viewheight
                Math3D.VectorSubtract(target, start, forward)
                Math3D.VectorNormalize(forward)

                Monster.monster_fire_bullet(self, start, forward, 6, 4, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MZ2_BOSS2_MACHINEGUN_L1)

                return true
            }
        }

        var Boss2MachineGun: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Boss2MachineGun"
            }

            public fun think(self: edict_t): Boolean {
                /*
             * RST: this was disabled ! float[] forward={0,0,0}, right={0,0,0};
             * float[] start={0,0,0}; float[] dir={0,0,0}; float[] vec={0,0,0};
             * int flash_number;
             * 
             * AngleVectors (self.s.angles, forward, right, null);
             * 
             * flash_number = MZ2_BOSS2_MACHINEGUN_1 + (self.s.frame -
             * FRAME_attack10); G_ProjectSource (self.s.origin,
             * monster_flash_offset[flash_number], forward, right, start);
             * 
             * VectorCopy (self.enemy.s.origin, vec); vec[2] +=
             * self.enemy.viewheight; VectorSubtract (vec, start, dir);
             * VectorNormalize (dir); monster_fire_bullet (self, start, dir, 3,
             * 4, DEFAULT_BULLET_HSPREAD, DEFAULT_BULLET_VSPREAD, flash_number);
             */
                boss2_firebullet_left.think(self)
                boss2_firebullet_right.think(self)
                return true
            }
        }

        var boss2_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var boss2_move_stand = mmove_t(FRAME_stand30, FRAME_stand50, boss2_frames_stand, null)

        var boss2_frames_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var boss2_move_fidget = mmove_t(FRAME_stand1, FRAME_stand30, boss2_frames_fidget, null)

        var boss2_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 8, null))

        var boss2_move_walk = mmove_t(FRAME_walk1, FRAME_walk20, boss2_frames_walk, null)

        var boss2_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 8, null))

        var boss2_move_run = mmove_t(FRAME_walk1, FRAME_walk20, boss2_frames_run, null)

        var boss2_frames_attack_pre_mg = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, boss2_attack_mg))

        var boss2_move_attack_pre_mg = mmove_t(FRAME_attack1, FRAME_attack9, boss2_frames_attack_pre_mg, null)

        //	   Loop this
        var boss2_frames_attack_mg = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, Boss2MachineGun), mframe_t(GameAI.ai_charge, 1, Boss2MachineGun), mframe_t(GameAI.ai_charge, 1, Boss2MachineGun), mframe_t(GameAI.ai_charge, 1, Boss2MachineGun), mframe_t(GameAI.ai_charge, 1, Boss2MachineGun), mframe_t(GameAI.ai_charge, 1, boss2_reattack_mg))

        var boss2_move_attack_mg = mmove_t(FRAME_attack10, FRAME_attack15, boss2_frames_attack_mg, null)

        var boss2_frames_attack_post_mg = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null))

        var boss2_move_attack_post_mg = mmove_t(FRAME_attack16, FRAME_attack19, boss2_frames_attack_post_mg, boss2_run)

        var boss2_frames_attack_rocket = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_move, -20, Boss2Rocket), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null))

        var boss2_move_attack_rocket = mmove_t(FRAME_attack20, FRAME_attack40, boss2_frames_attack_rocket, boss2_run)

        var boss2_frames_pain_heavy = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var boss2_move_pain_heavy = mmove_t(FRAME_pain2, FRAME_pain19, boss2_frames_pain_heavy, boss2_run)

        var boss2_frames_pain_light = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var boss2_move_pain_light = mmove_t(FRAME_pain20, FRAME_pain23, boss2_frames_pain_light, boss2_run)

        var boss2_frames_death = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, M_Supertank.BossExplode))

        /*
     * static EntThinkAdapter xxx = new EntThinkAdapter() { public boolean
     * think(edict_t self) { return true; } };
     */

        var boss2_move_death = mmove_t(FRAME_death2, FRAME_death50, boss2_frames_death, boss2_dead)

        /*
     * QUAKED monster_boss2 (1 .5 0) (-56 -56 0) (56 56 80) Ambush Trigger_Spawn
     * Sight
     */
        public fun SP_monster_boss2(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_pain1 = GameBase.gi.soundindex("bosshovr/bhvpain1.wav")
            sound_pain2 = GameBase.gi.soundindex("bosshovr/bhvpain2.wav")
            sound_pain3 = GameBase.gi.soundindex("bosshovr/bhvpain3.wav")
            sound_death = GameBase.gi.soundindex("bosshovr/bhvdeth1.wav")
            sound_search1 = GameBase.gi.soundindex("bosshovr/bhvunqv1.wav")

            self.s.sound = GameBase.gi.soundindex("bosshovr/bhvengn1.wav")

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/boss2/tris.md2")
            Math3D.VectorSet(self.mins, -56, -56, 0)
            Math3D.VectorSet(self.maxs, 56, 56, 80)

            self.health = 2000
            self.gib_health = -200
            self.mass = 1000

            self.flags = self.flags or Defines.FL_IMMUNE_LASER

            self.pain = boss2_pain
            self.die = boss2_die

            self.monsterinfo.stand = boss2_stand
            self.monsterinfo.walk = boss2_walk
            self.monsterinfo.run = boss2_run
            self.monsterinfo.attack = boss2_attack
            self.monsterinfo.search = boss2_search
            self.monsterinfo.checkattack = Boss2_CheckAttack
            GameBase.gi.linkentity(self)

            self.monsterinfo.currentmove = boss2_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.flymonster_start.think(self)
        }
    }
}