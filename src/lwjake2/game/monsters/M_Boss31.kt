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

public class M_Boss31 {
    companion object {

        public val FRAME_attak101: Int = 0

        public val FRAME_attak102: Int = 1

        public val FRAME_attak103: Int = 2

        public val FRAME_attak104: Int = 3

        public val FRAME_attak105: Int = 4

        public val FRAME_attak106: Int = 5

        public val FRAME_attak107: Int = 6

        public val FRAME_attak108: Int = 7

        public val FRAME_attak109: Int = 8

        public val FRAME_attak110: Int = 9

        public val FRAME_attak111: Int = 10

        public val FRAME_attak112: Int = 11

        public val FRAME_attak113: Int = 12

        public val FRAME_attak114: Int = 13

        public val FRAME_attak115: Int = 14

        public val FRAME_attak116: Int = 15

        public val FRAME_attak117: Int = 16

        public val FRAME_attak118: Int = 17

        public val FRAME_attak201: Int = 18

        public val FRAME_attak202: Int = 19

        public val FRAME_attak203: Int = 20

        public val FRAME_attak204: Int = 21

        public val FRAME_attak205: Int = 22

        public val FRAME_attak206: Int = 23

        public val FRAME_attak207: Int = 24

        public val FRAME_attak208: Int = 25

        public val FRAME_attak209: Int = 26

        public val FRAME_attak210: Int = 27

        public val FRAME_attak211: Int = 28

        public val FRAME_attak212: Int = 29

        public val FRAME_attak213: Int = 30

        public val FRAME_death01: Int = 31

        public val FRAME_death02: Int = 32

        public val FRAME_death03: Int = 33

        public val FRAME_death04: Int = 34

        public val FRAME_death05: Int = 35

        public val FRAME_death06: Int = 36

        public val FRAME_death07: Int = 37

        public val FRAME_death08: Int = 38

        public val FRAME_death09: Int = 39

        public val FRAME_death10: Int = 40

        public val FRAME_death11: Int = 41

        public val FRAME_death12: Int = 42

        public val FRAME_death13: Int = 43

        public val FRAME_death14: Int = 44

        public val FRAME_death15: Int = 45

        public val FRAME_death16: Int = 46

        public val FRAME_death17: Int = 47

        public val FRAME_death18: Int = 48

        public val FRAME_death19: Int = 49

        public val FRAME_death20: Int = 50

        public val FRAME_death21: Int = 51

        public val FRAME_death22: Int = 52

        public val FRAME_death23: Int = 53

        public val FRAME_death24: Int = 54

        public val FRAME_death25: Int = 55

        public val FRAME_death26: Int = 56

        public val FRAME_death27: Int = 57

        public val FRAME_death28: Int = 58

        public val FRAME_death29: Int = 59

        public val FRAME_death30: Int = 60

        public val FRAME_death31: Int = 61

        public val FRAME_death32: Int = 62

        public val FRAME_death33: Int = 63

        public val FRAME_death34: Int = 64

        public val FRAME_death35: Int = 65

        public val FRAME_death36: Int = 66

        public val FRAME_death37: Int = 67

        public val FRAME_death38: Int = 68

        public val FRAME_death39: Int = 69

        public val FRAME_death40: Int = 70

        public val FRAME_death41: Int = 71

        public val FRAME_death42: Int = 72

        public val FRAME_death43: Int = 73

        public val FRAME_death44: Int = 74

        public val FRAME_death45: Int = 75

        public val FRAME_death46: Int = 76

        public val FRAME_death47: Int = 77

        public val FRAME_death48: Int = 78

        public val FRAME_death49: Int = 79

        public val FRAME_death50: Int = 80

        public val FRAME_pain101: Int = 81

        public val FRAME_pain102: Int = 82

        public val FRAME_pain103: Int = 83

        public val FRAME_pain201: Int = 84

        public val FRAME_pain202: Int = 85

        public val FRAME_pain203: Int = 86

        public val FRAME_pain301: Int = 87

        public val FRAME_pain302: Int = 88

        public val FRAME_pain303: Int = 89

        public val FRAME_pain304: Int = 90

        public val FRAME_pain305: Int = 91

        public val FRAME_pain306: Int = 92

        public val FRAME_pain307: Int = 93

        public val FRAME_pain308: Int = 94

        public val FRAME_pain309: Int = 95

        public val FRAME_pain310: Int = 96

        public val FRAME_pain311: Int = 97

        public val FRAME_pain312: Int = 98

        public val FRAME_pain313: Int = 99

        public val FRAME_pain314: Int = 100

        public val FRAME_pain315: Int = 101

        public val FRAME_pain316: Int = 102

        public val FRAME_pain317: Int = 103

        public val FRAME_pain318: Int = 104

        public val FRAME_pain319: Int = 105

        public val FRAME_pain320: Int = 106

        public val FRAME_pain321: Int = 107

        public val FRAME_pain322: Int = 108

        public val FRAME_pain323: Int = 109

        public val FRAME_pain324: Int = 110

        public val FRAME_pain325: Int = 111

        public val FRAME_stand01: Int = 112

        public val FRAME_stand02: Int = 113

        public val FRAME_stand03: Int = 114

        public val FRAME_stand04: Int = 115

        public val FRAME_stand05: Int = 116

        public val FRAME_stand06: Int = 117

        public val FRAME_stand07: Int = 118

        public val FRAME_stand08: Int = 119

        public val FRAME_stand09: Int = 120

        public val FRAME_stand10: Int = 121

        public val FRAME_stand11: Int = 122

        public val FRAME_stand12: Int = 123

        public val FRAME_stand13: Int = 124

        public val FRAME_stand14: Int = 125

        public val FRAME_stand15: Int = 126

        public val FRAME_stand16: Int = 127

        public val FRAME_stand17: Int = 128

        public val FRAME_stand18: Int = 129

        public val FRAME_stand19: Int = 130

        public val FRAME_stand20: Int = 131

        public val FRAME_stand21: Int = 132

        public val FRAME_stand22: Int = 133

        public val FRAME_stand23: Int = 134

        public val FRAME_stand24: Int = 135

        public val FRAME_stand25: Int = 136

        public val FRAME_stand26: Int = 137

        public val FRAME_stand27: Int = 138

        public val FRAME_stand28: Int = 139

        public val FRAME_stand29: Int = 140

        public val FRAME_stand30: Int = 141

        public val FRAME_stand31: Int = 142

        public val FRAME_stand32: Int = 143

        public val FRAME_stand33: Int = 144

        public val FRAME_stand34: Int = 145

        public val FRAME_stand35: Int = 146

        public val FRAME_stand36: Int = 147

        public val FRAME_stand37: Int = 148

        public val FRAME_stand38: Int = 149

        public val FRAME_stand39: Int = 150

        public val FRAME_stand40: Int = 151

        public val FRAME_stand41: Int = 152

        public val FRAME_stand42: Int = 153

        public val FRAME_stand43: Int = 154

        public val FRAME_stand44: Int = 155

        public val FRAME_stand45: Int = 156

        public val FRAME_stand46: Int = 157

        public val FRAME_stand47: Int = 158

        public val FRAME_stand48: Int = 159

        public val FRAME_stand49: Int = 160

        public val FRAME_stand50: Int = 161

        public val FRAME_stand51: Int = 162

        public val FRAME_walk01: Int = 163

        public val FRAME_walk02: Int = 164

        public val FRAME_walk03: Int = 165

        public val FRAME_walk04: Int = 166

        public val FRAME_walk05: Int = 167

        public val FRAME_walk06: Int = 168

        public val FRAME_walk07: Int = 169

        public val FRAME_walk08: Int = 170

        public val FRAME_walk09: Int = 171

        public val FRAME_walk10: Int = 172

        public val FRAME_walk11: Int = 173

        public val FRAME_walk12: Int = 174

        public val FRAME_walk13: Int = 175

        public val FRAME_walk14: Int = 176

        public val FRAME_walk15: Int = 177

        public val FRAME_walk16: Int = 178

        public val FRAME_walk17: Int = 179

        public val FRAME_walk18: Int = 180

        public val FRAME_walk19: Int = 181

        public val FRAME_walk20: Int = 182

        public val FRAME_walk21: Int = 183

        public val FRAME_walk22: Int = 184

        public val FRAME_walk23: Int = 185

        public val FRAME_walk24: Int = 186

        public val FRAME_walk25: Int = 187

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        /*
     * ==============================================================================
     * 
     * jorg
     * 
     * ==============================================================================
     */

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_pain3: Int = 0

        var sound_idle: Int = 0

        var sound_death: Int = 0

        var sound_search1: Int = 0

        var sound_search2: Int = 0

        var sound_search3: Int = 0

        var sound_attack1: Int = 0

        var sound_attack2: Int = 0

        var sound_firegun: Int = 0

        var sound_step_left: Int = 0

        var sound_step_right: Int = 0

        var sound_death_hit: Int = 0

        /*
     * static EntThinkAdapter xxx = new EntThinkAdapter() { public boolean
     * think(edict_t self) { return true; } };
     */

        var jorg_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_search"
            }

            public fun think(self: edict_t): Boolean {
                val r: Float

                r = Lib.random()

                if (r <= 0.3)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search1, 1, Defines.ATTN_NORM, 0)
                else if (r <= 0.6)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search2, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search3, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var jorg_idle: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_idle"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_idle, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var jorg_death_hit: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_death_hit"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_death_hit, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var jorg_step_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_step_left"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_step_left, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var jorg_step_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_step_right"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_step_right, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var jorg_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = jorg_move_stand
                return true
            }
        }

        var jorg_reattack1: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_reattack1"
            }

            public fun think(self: edict_t): Boolean {
                if (GameUtil.visible(self, self.enemy))
                    if (Lib.random() < 0.9)
                        self.monsterinfo.currentmove = jorg_move_attack1
                    else {
                        self.s.sound = 0
                        self.monsterinfo.currentmove = jorg_move_end_attack1
                    }
                else {
                    self.s.sound = 0
                    self.monsterinfo.currentmove = jorg_move_end_attack1
                }
                return true
            }
        }

        var jorg_attack1: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_attack1"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = jorg_move_attack1
                return true
            }
        }

        var jorg_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "jorg_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                self.s.sound = 0

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                // Lessen the chance of him going into his pain frames if he takes
                // little damage
                if (damage <= 40)
                    if (Lib.random() <= 0.6)
                        return

                /*
             * If he's entering his attack1 or using attack1, lessen the chance
             * of him going into pain
             */

                if ((self.s.frame >= FRAME_attak101) && (self.s.frame <= FRAME_attak108))
                    if (Lib.random() <= 0.005)
                        return

                if ((self.s.frame >= FRAME_attak109) && (self.s.frame <= FRAME_attak114))
                    if (Lib.random() <= 0.00005)
                        return

                if ((self.s.frame >= FRAME_attak201) && (self.s.frame <= FRAME_attak208))
                    if (Lib.random() <= 0.005)
                        return

                self.pain_debounce_time = GameBase.level.time + 3
                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (damage <= 50) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = jorg_move_pain1
                } else if (damage <= 100) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = jorg_move_pain2
                } else {
                    if (Lib.random() <= 0.3) {
                        GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain3, 1, Defines.ATTN_NORM, 0)
                        self.monsterinfo.currentmove = jorg_move_pain3
                    }
                }

            }
        }

        var jorgBFG: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorgBFG"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)

                val start = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val vec = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_JORG_BFG_1], forward, right, start)

                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_attack2, 1, Defines.ATTN_NORM, 0)
                /*
             * void monster_fire_bfg (edict_t self, float [] start, float []
             * aimdir, int damage, int speed, int kick, float damage_radius, int
             * flashtype)
             */
                Monster.monster_fire_bfg(self, start, dir, 50, 300, 100, 200, Defines.MZ2_JORG_BFG_1)
                return true
            }
        }

        var jorg_firebullet_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_firebullet_right"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val target = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_JORG_MACHINEGUN_R1], forward, right, start)

                Math3D.VectorMA(self.enemy.s.origin, -0.2.toFloat(), self.enemy.velocity, target)
                target[2] += self.enemy.viewheight
                Math3D.VectorSubtract(target, start, forward)
                Math3D.VectorNormalize(forward)

                Monster.monster_fire_bullet(self, start, forward, 6, 4, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MZ2_JORG_MACHINEGUN_R1)
                return true
            }
        }

        var jorg_firebullet_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_firebullet_left"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val target = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_JORG_MACHINEGUN_L1], forward, right, start)

                Math3D.VectorMA(self.enemy.s.origin, -0.2.toFloat(), self.enemy.velocity, target)
                target[2] += self.enemy.viewheight
                Math3D.VectorSubtract(target, start, forward)
                Math3D.VectorNormalize(forward)

                Monster.monster_fire_bullet(self, start, forward, 6, 4, Defines.DEFAULT_BULLET_HSPREAD, Defines.DEFAULT_BULLET_VSPREAD, Defines.MZ2_JORG_MACHINEGUN_L1)
                return true
            }
        }

        var jorg_firebullet: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_firebullet"
            }

            public fun think(self: edict_t): Boolean {
                jorg_firebullet_left.think(self)
                jorg_firebullet_right.think(self)
                return true
            }
        }

        var jorg_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_attack"
            }

            public fun think(self: edict_t): Boolean {
                val vec = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSubtract(self.enemy.s.origin, self.s.origin, vec)

                if (Lib.random() <= 0.75) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_attack1, 1, Defines.ATTN_NORM, 0)
                    self.s.sound = GameBase.gi.soundindex("boss3/w_loop.wav")
                    self.monsterinfo.currentmove = jorg_move_start_attack1
                } else {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_attack2, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = jorg_move_attack2
                }
                return true
            }
        }

        /** Was disabled. RST.  */
        var jorg_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_dead"
            }

            public fun think(self: edict_t): Boolean {
                /*
             * edict_t tempent;
             * 
             * //VectorSet (self.mins, -16, -16, -24); //VectorSet (self.maxs,
             * 16, 16, -8); // Jorg is on modelindex2. Do not clear him.
             * VectorSet( self.mins, -60, -60, 0); VectorSet(self.maxs, 60, 60,
             * 72); self.movetype= MOVETYPE_TOSS; self.nextthink= 0;
             * gi.linkentity(self);
             * 
             * tempent= G_Spawn(); VectorCopy(self.s.origin, tempent.s.origin);
             * VectorCopy(self.s.angles, tempent.s.angles); tempent.killtarget=
             * self.killtarget; tempent.target= self.target; tempent.activator=
             * self.enemy; self.killtarget= 0; self.target= 0;
             * SP_monster_makron(tempent);
             *  
             */
                return true
            }
        }

        var jorg_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "jorg_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_NO
                self.s.sound = 0
                self.count = 0
                self.monsterinfo.currentmove = jorg_move_death
                return
            }
        }

        var Jorg_CheckAttack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Jorg_CheckAttack"
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

                //	   missile attack ?
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
                    chance = 0.4.toFloat()
                } else if (enemy_range == Defines.RANGE_MID) {
                    chance = 0.2.toFloat()
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

        //
        //	   stand
        //

        var jorg_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, jorg_idle), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 10
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 20
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), // 30
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 19, null), mframe_t(GameAI.ai_stand, 11, jorg_step_left), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 6, null), mframe_t(GameAI.ai_stand, 9, jorg_step_right), mframe_t(GameAI.ai_stand, 0, null), // 40
                mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, -2, null), mframe_t(GameAI.ai_stand, -17, jorg_step_left), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, -12, null), // 50
                mframe_t(GameAI.ai_stand, -14, jorg_step_right) // 51
        )

        var jorg_move_stand = mmove_t(FRAME_stand01, FRAME_stand51, jorg_frames_stand, null)

        var jorg_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 17, jorg_step_left), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 12, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 33, jorg_step_right), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 9, null), mframe_t(GameAI.ai_run, 9, null), mframe_t(GameAI.ai_run, 9, null))

        var jorg_move_run = mmove_t(FRAME_walk06, FRAME_walk19, jorg_frames_run, null)

        //
        //	   walk
        //

        var jorg_frames_start_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 6, null), mframe_t(GameAI.ai_walk, 7, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, 15, null))

        var jorg_move_start_walk = mmove_t(FRAME_walk01, FRAME_walk05, jorg_frames_start_walk, null)

        var jorg_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 17, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 12, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 10, null), mframe_t(GameAI.ai_walk, 33, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, 9, null))

        var jorg_move_walk = mmove_t(FRAME_walk06, FRAME_walk19, jorg_frames_walk, null)

        var jorg_frames_end_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 11, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, -8, null))

        var jorg_move_end_walk = mmove_t(FRAME_walk20, FRAME_walk25, jorg_frames_end_walk, null)

        var jorg_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = jorg_move_walk
                return true
            }
        }

        var jorg_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "jorg_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = jorg_move_stand
                else
                    self.monsterinfo.currentmove = jorg_move_run
                return true
            }
        }

        var jorg_frames_pain3 = array<mframe_t>(mframe_t(GameAI.ai_move, -28, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -3, jorg_step_left), mframe_t(GameAI.ai_move, -9, null), mframe_t(GameAI.ai_move, 0, jorg_step_right), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -7, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, -11, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 10, null), mframe_t(GameAI.ai_move, 11, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 10, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 10, null), mframe_t(GameAI.ai_move, 7, jorg_step_left), mframe_t(GameAI.ai_move, 17, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, jorg_step_right))

        var jorg_move_pain3 = mmove_t(FRAME_pain301, FRAME_pain325, jorg_frames_pain3, jorg_run)

        var jorg_frames_pain2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var jorg_move_pain2 = mmove_t(FRAME_pain201, FRAME_pain203, jorg_frames_pain2, jorg_run)

        var jorg_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var jorg_move_pain1 = mmove_t(FRAME_pain101, FRAME_pain103, jorg_frames_pain1, jorg_run)

        var jorg_frames_death1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 10
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 20
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 30
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), // 40
                mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, M_Boss32.MakronToss), mframe_t(GameAI.ai_move, 0, M_Supertank.BossExplode) // 50
        )

        var jorg_move_death = mmove_t(FRAME_death01, FRAME_death50, jorg_frames_death1, jorg_dead)

        var jorg_frames_attack2 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, jorgBFG), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var jorg_move_attack2 = mmove_t(FRAME_attak201, FRAME_attak213, jorg_frames_attack2, jorg_run)

        var jorg_frames_start_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null))

        var jorg_move_start_attack1 = mmove_t(FRAME_attak101, FRAME_attak108, jorg_frames_start_attack1, jorg_attack1)

        var jorg_frames_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, jorg_firebullet), mframe_t(GameAI.ai_charge, 0, jorg_firebullet), mframe_t(GameAI.ai_charge, 0, jorg_firebullet), mframe_t(GameAI.ai_charge, 0, jorg_firebullet), mframe_t(GameAI.ai_charge, 0, jorg_firebullet), mframe_t(GameAI.ai_charge, 0, jorg_firebullet))

        var jorg_move_attack1 = mmove_t(FRAME_attak109, FRAME_attak114, jorg_frames_attack1, jorg_reattack1)

        var jorg_frames_end_attack1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var jorg_move_end_attack1 = mmove_t(FRAME_attak115, FRAME_attak118, jorg_frames_end_attack1, jorg_run)

        /*
     * QUAKED monster_jorg (1 .5 0) (-80 -80 0) (90 90 140) Ambush Trigger_Spawn
     * Sight
     */
        public fun SP_monster_jorg(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_pain1 = GameBase.gi.soundindex("boss3/bs3pain1.wav")
            sound_pain2 = GameBase.gi.soundindex("boss3/bs3pain2.wav")
            sound_pain3 = GameBase.gi.soundindex("boss3/bs3pain3.wav")
            sound_death = GameBase.gi.soundindex("boss3/bs3deth1.wav")
            sound_attack1 = GameBase.gi.soundindex("boss3/bs3atck1.wav")
            sound_attack2 = GameBase.gi.soundindex("boss3/bs3atck2.wav")
            sound_search1 = GameBase.gi.soundindex("boss3/bs3srch1.wav")
            sound_search2 = GameBase.gi.soundindex("boss3/bs3srch2.wav")
            sound_search3 = GameBase.gi.soundindex("boss3/bs3srch3.wav")
            sound_idle = GameBase.gi.soundindex("boss3/bs3idle1.wav")
            sound_step_left = GameBase.gi.soundindex("boss3/step1.wav")
            sound_step_right = GameBase.gi.soundindex("boss3/step2.wav")
            sound_firegun = GameBase.gi.soundindex("boss3/xfire.wav")
            sound_death_hit = GameBase.gi.soundindex("boss3/d_hit.wav")

            M_Boss32.MakronPrecache()

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/boss3/rider/tris.md2")
            self.s.modelindex2 = GameBase.gi.modelindex("models/monsters/boss3/jorg/tris.md2")
            Math3D.VectorSet(self.mins, -80, -80, 0)
            Math3D.VectorSet(self.maxs, 80, 80, 140)

            self.health = 3000
            self.gib_health = -2000
            self.mass = 1000

            self.pain = jorg_pain
            self.die = jorg_die
            self.monsterinfo.stand = jorg_stand
            self.monsterinfo.walk = jorg_walk
            self.monsterinfo.run = jorg_run
            self.monsterinfo.dodge = null
            self.monsterinfo.attack = jorg_attack
            self.monsterinfo.search = jorg_search
            self.monsterinfo.melee = null
            self.monsterinfo.sight = null
            self.monsterinfo.checkattack = Jorg_CheckAttack
            GameBase.gi.linkentity(self)

            self.monsterinfo.currentmove = jorg_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.walkmonster_start.think(self)
        }
    }
}