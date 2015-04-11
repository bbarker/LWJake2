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
import lwjake2.game.Monster
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.game.monsters.M_Flash
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Hover {
    companion object {

        //	This file generated by ModelGen - Do NOT Modify

        public val FRAME_stand01: Int = 0

        public val FRAME_stand02: Int = 1

        public val FRAME_stand03: Int = 2

        public val FRAME_stand04: Int = 3

        public val FRAME_stand05: Int = 4

        public val FRAME_stand06: Int = 5

        public val FRAME_stand07: Int = 6

        public val FRAME_stand08: Int = 7

        public val FRAME_stand09: Int = 8

        public val FRAME_stand10: Int = 9

        public val FRAME_stand11: Int = 10

        public val FRAME_stand12: Int = 11

        public val FRAME_stand13: Int = 12

        public val FRAME_stand14: Int = 13

        public val FRAME_stand15: Int = 14

        public val FRAME_stand16: Int = 15

        public val FRAME_stand17: Int = 16

        public val FRAME_stand18: Int = 17

        public val FRAME_stand19: Int = 18

        public val FRAME_stand20: Int = 19

        public val FRAME_stand21: Int = 20

        public val FRAME_stand22: Int = 21

        public val FRAME_stand23: Int = 22

        public val FRAME_stand24: Int = 23

        public val FRAME_stand25: Int = 24

        public val FRAME_stand26: Int = 25

        public val FRAME_stand27: Int = 26

        public val FRAME_stand28: Int = 27

        public val FRAME_stand29: Int = 28

        public val FRAME_stand30: Int = 29

        public val FRAME_forwrd01: Int = 30

        public val FRAME_forwrd02: Int = 31

        public val FRAME_forwrd03: Int = 32

        public val FRAME_forwrd04: Int = 33

        public val FRAME_forwrd05: Int = 34

        public val FRAME_forwrd06: Int = 35

        public val FRAME_forwrd07: Int = 36

        public val FRAME_forwrd08: Int = 37

        public val FRAME_forwrd09: Int = 38

        public val FRAME_forwrd10: Int = 39

        public val FRAME_forwrd11: Int = 40

        public val FRAME_forwrd12: Int = 41

        public val FRAME_forwrd13: Int = 42

        public val FRAME_forwrd14: Int = 43

        public val FRAME_forwrd15: Int = 44

        public val FRAME_forwrd16: Int = 45

        public val FRAME_forwrd17: Int = 46

        public val FRAME_forwrd18: Int = 47

        public val FRAME_forwrd19: Int = 48

        public val FRAME_forwrd20: Int = 49

        public val FRAME_forwrd21: Int = 50

        public val FRAME_forwrd22: Int = 51

        public val FRAME_forwrd23: Int = 52

        public val FRAME_forwrd24: Int = 53

        public val FRAME_forwrd25: Int = 54

        public val FRAME_forwrd26: Int = 55

        public val FRAME_forwrd27: Int = 56

        public val FRAME_forwrd28: Int = 57

        public val FRAME_forwrd29: Int = 58

        public val FRAME_forwrd30: Int = 59

        public val FRAME_forwrd31: Int = 60

        public val FRAME_forwrd32: Int = 61

        public val FRAME_forwrd33: Int = 62

        public val FRAME_forwrd34: Int = 63

        public val FRAME_forwrd35: Int = 64

        public val FRAME_stop101: Int = 65

        public val FRAME_stop102: Int = 66

        public val FRAME_stop103: Int = 67

        public val FRAME_stop104: Int = 68

        public val FRAME_stop105: Int = 69

        public val FRAME_stop106: Int = 70

        public val FRAME_stop107: Int = 71

        public val FRAME_stop108: Int = 72

        public val FRAME_stop109: Int = 73

        public val FRAME_stop201: Int = 74

        public val FRAME_stop202: Int = 75

        public val FRAME_stop203: Int = 76

        public val FRAME_stop204: Int = 77

        public val FRAME_stop205: Int = 78

        public val FRAME_stop206: Int = 79

        public val FRAME_stop207: Int = 80

        public val FRAME_stop208: Int = 81

        public val FRAME_takeof01: Int = 82

        public val FRAME_takeof02: Int = 83

        public val FRAME_takeof03: Int = 84

        public val FRAME_takeof04: Int = 85

        public val FRAME_takeof05: Int = 86

        public val FRAME_takeof06: Int = 87

        public val FRAME_takeof07: Int = 88

        public val FRAME_takeof08: Int = 89

        public val FRAME_takeof09: Int = 90

        public val FRAME_takeof10: Int = 91

        public val FRAME_takeof11: Int = 92

        public val FRAME_takeof12: Int = 93

        public val FRAME_takeof13: Int = 94

        public val FRAME_takeof14: Int = 95

        public val FRAME_takeof15: Int = 96

        public val FRAME_takeof16: Int = 97

        public val FRAME_takeof17: Int = 98

        public val FRAME_takeof18: Int = 99

        public val FRAME_takeof19: Int = 100

        public val FRAME_takeof20: Int = 101

        public val FRAME_takeof21: Int = 102

        public val FRAME_takeof22: Int = 103

        public val FRAME_takeof23: Int = 104

        public val FRAME_takeof24: Int = 105

        public val FRAME_takeof25: Int = 106

        public val FRAME_takeof26: Int = 107

        public val FRAME_takeof27: Int = 108

        public val FRAME_takeof28: Int = 109

        public val FRAME_takeof29: Int = 110

        public val FRAME_takeof30: Int = 111

        public val FRAME_land01: Int = 112

        public val FRAME_pain101: Int = 113

        public val FRAME_pain102: Int = 114

        public val FRAME_pain103: Int = 115

        public val FRAME_pain104: Int = 116

        public val FRAME_pain105: Int = 117

        public val FRAME_pain106: Int = 118

        public val FRAME_pain107: Int = 119

        public val FRAME_pain108: Int = 120

        public val FRAME_pain109: Int = 121

        public val FRAME_pain110: Int = 122

        public val FRAME_pain111: Int = 123

        public val FRAME_pain112: Int = 124

        public val FRAME_pain113: Int = 125

        public val FRAME_pain114: Int = 126

        public val FRAME_pain115: Int = 127

        public val FRAME_pain116: Int = 128

        public val FRAME_pain117: Int = 129

        public val FRAME_pain118: Int = 130

        public val FRAME_pain119: Int = 131

        public val FRAME_pain120: Int = 132

        public val FRAME_pain121: Int = 133

        public val FRAME_pain122: Int = 134

        public val FRAME_pain123: Int = 135

        public val FRAME_pain124: Int = 136

        public val FRAME_pain125: Int = 137

        public val FRAME_pain126: Int = 138

        public val FRAME_pain127: Int = 139

        public val FRAME_pain128: Int = 140

        public val FRAME_pain201: Int = 141

        public val FRAME_pain202: Int = 142

        public val FRAME_pain203: Int = 143

        public val FRAME_pain204: Int = 144

        public val FRAME_pain205: Int = 145

        public val FRAME_pain206: Int = 146

        public val FRAME_pain207: Int = 147

        public val FRAME_pain208: Int = 148

        public val FRAME_pain209: Int = 149

        public val FRAME_pain210: Int = 150

        public val FRAME_pain211: Int = 151

        public val FRAME_pain212: Int = 152

        public val FRAME_pain301: Int = 153

        public val FRAME_pain302: Int = 154

        public val FRAME_pain303: Int = 155

        public val FRAME_pain304: Int = 156

        public val FRAME_pain305: Int = 157

        public val FRAME_pain306: Int = 158

        public val FRAME_pain307: Int = 159

        public val FRAME_pain308: Int = 160

        public val FRAME_pain309: Int = 161

        public val FRAME_death101: Int = 162

        public val FRAME_death102: Int = 163

        public val FRAME_death103: Int = 164

        public val FRAME_death104: Int = 165

        public val FRAME_death105: Int = 166

        public val FRAME_death106: Int = 167

        public val FRAME_death107: Int = 168

        public val FRAME_death108: Int = 169

        public val FRAME_death109: Int = 170

        public val FRAME_death110: Int = 171

        public val FRAME_death111: Int = 172

        public val FRAME_backwd01: Int = 173

        public val FRAME_backwd02: Int = 174

        public val FRAME_backwd03: Int = 175

        public val FRAME_backwd04: Int = 176

        public val FRAME_backwd05: Int = 177

        public val FRAME_backwd06: Int = 178

        public val FRAME_backwd07: Int = 179

        public val FRAME_backwd08: Int = 180

        public val FRAME_backwd09: Int = 181

        public val FRAME_backwd10: Int = 182

        public val FRAME_backwd11: Int = 183

        public val FRAME_backwd12: Int = 184

        public val FRAME_backwd13: Int = 185

        public val FRAME_backwd14: Int = 186

        public val FRAME_backwd15: Int = 187

        public val FRAME_backwd16: Int = 188

        public val FRAME_backwd17: Int = 189

        public val FRAME_backwd18: Int = 190

        public val FRAME_backwd19: Int = 191

        public val FRAME_backwd20: Int = 192

        public val FRAME_backwd21: Int = 193

        public val FRAME_backwd22: Int = 194

        public val FRAME_backwd23: Int = 195

        public val FRAME_backwd24: Int = 196

        public val FRAME_attak101: Int = 197

        public val FRAME_attak102: Int = 198

        public val FRAME_attak103: Int = 199

        public val FRAME_attak104: Int = 200

        public val FRAME_attak105: Int = 201

        public val FRAME_attak106: Int = 202

        public val FRAME_attak107: Int = 203

        public val FRAME_attak108: Int = 204

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_death1: Int = 0

        var sound_death2: Int = 0

        var sound_sight: Int = 0

        var sound_search1: Int = 0

        var sound_search2: Int = 0

        var hover_reattack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_reattack"
            }

            public fun think(self: edict_t): Boolean {
                if (self.enemy.health > 0)
                    if (GameUtil.visible(self, self.enemy))
                        if (Lib.random() <= 0.6) {
                            self.monsterinfo.currentmove = hover_move_attack1
                            return true
                        }
                self.monsterinfo.currentmove = hover_move_end_attack
                return true
            }
        }

        var hover_fire_blaster: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_fire_blaster"
            }

            public fun think(self: edict_t): Boolean {
                val start = floatArray(0.0, 0.0, 0.0)
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val end = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val effect: Int

                if (self.s.frame == FRAME_attak104)
                    effect = Defines.EF_HYPERBLASTER
                else
                    effect = 0

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_HOVER_BLASTER_1], forward, right, start)

                Math3D.VectorCopy(self.enemy.s.origin, end)
                end[2] += self.enemy.viewheight
                Math3D.VectorSubtract(end, start, dir)

                Monster.monster_fire_blaster(self, start, dir, 1, 1000, Defines.MZ2_HOVER_BLASTER_1, effect)
                return true
            }
        }

        var hover_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = hover_move_stand
                return true
            }
        }

        var hover_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = hover_move_stand
                else
                    self.monsterinfo.currentmove = hover_move_run
                return true
            }
        }

        var hover_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = hover_move_walk
                return true
            }
        }

        var hover_start_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_start_attack"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = hover_move_start_attack
                return true
            }
        }

        var hover_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_attack"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = hover_move_attack1
                return true
            }
        }

        var hover_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "hover_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3

                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (damage <= 25) {
                    if (Lib.random() < 0.5) {
                        GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                        self.monsterinfo.currentmove = hover_move_pain3
                    } else {
                        GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)
                        self.monsterinfo.currentmove = hover_move_pain2
                    }
                } else {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = hover_move_pain1
                }
            }
        }

        var hover_deadthink: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_deadthink"
            }

            public fun think(self: edict_t): Boolean {
                if (null == self.groundentity && GameBase.level.time < self.timestamp) {
                    self.nextthink = GameBase.level.time + Defines.FRAMETIME
                    return true
                }
                GameMisc.BecomeExplosion1(self)
                return true
            }
        }

        var hover_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_dead"
            }

            public fun think(self: edict_t): Boolean {
                Math3D.VectorSet(self.mins, -16, -16, -24)
                Math3D.VectorSet(self.maxs, 16, 16, -8)
                self.movetype = Defines.MOVETYPE_TOSS
                self.think = hover_deadthink
                self.nextthink = GameBase.level.time + Defines.FRAMETIME
                self.timestamp = GameBase.level.time + 15
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var hover_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "hover_die"
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
                        while (n < 2) {
                            GameMisc.ThrowGib(self, "models/objects/gibs/sm_meat/tris.md2", damage, Defines.GIB_ORGANIC)
                            n++
                        }
                    }
                    GameMisc.ThrowHead(self, "models/objects/gibs/sm_meat/tris.md2", damage, Defines.GIB_ORGANIC)
                    self.deadflag = Defines.DEAD_DEAD
                    return
                }

                if (self.deadflag == Defines.DEAD_DEAD)
                    return

                //	regular death
                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death1, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death2, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES
                self.monsterinfo.currentmove = hover_move_death1
            }
        }

        var hover_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "hover_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var hover_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "hover_search"
            }

            public fun think(self: edict_t): Boolean {
                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search1, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search2, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var hover_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var hover_move_stand = mmove_t(FRAME_stand01, FRAME_stand30, hover_frames_stand, null)

        var hover_frames_stop1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_stop1 = mmove_t(FRAME_stop101, FRAME_stop109, hover_frames_stop1, null)

        var hover_frames_stop2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_stop2 = mmove_t(FRAME_stop201, FRAME_stop208, hover_frames_stop2, null)

        var hover_frames_takeoff = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 5, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -9, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_takeoff = mmove_t(FRAME_takeof01, FRAME_takeof30, hover_frames_takeoff, null)

        var hover_frames_pain3 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_pain3 = mmove_t(FRAME_pain301, FRAME_pain309, hover_frames_pain3, hover_run)

        var hover_frames_pain2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_pain2 = mmove_t(FRAME_pain201, FRAME_pain212, hover_frames_pain2, hover_run)

        var hover_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, -8, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -3, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 7, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 5, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 4, null))

        var hover_move_pain1 = mmove_t(FRAME_pain101, FRAME_pain128, hover_frames_pain1, hover_run)

        var hover_frames_land = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null))

        var hover_move_land = mmove_t(FRAME_land01, FRAME_land01, hover_frames_land, null)

        var hover_frames_forward = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_forward = mmove_t(FRAME_forwrd01, FRAME_forwrd35, hover_frames_forward, null)

        var hover_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 4, null))

        var hover_move_walk = mmove_t(FRAME_forwrd01, FRAME_forwrd35, hover_frames_walk, null)

        var hover_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 10, null))

        var hover_move_run = mmove_t(FRAME_forwrd01, FRAME_forwrd35, hover_frames_run, null)

        var hover_frames_death1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -10, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 5, null), mframe_t(GameAI.ai_move, 4, null), mframe_t(GameAI.ai_move, 7, null))

        var hover_move_death1 = mmove_t(FRAME_death101, FRAME_death111, hover_frames_death1, hover_dead)

        var hover_frames_backward = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var hover_move_backward = mmove_t(FRAME_backwd01, FRAME_backwd24, hover_frames_backward, null)

        var hover_frames_start_attack = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null))

        var hover_move_start_attack = mmove_t(FRAME_attak101, FRAME_attak103, hover_frames_start_attack, hover_attack)

        var hover_frames_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, -10, hover_fire_blaster), mframe_t(GameAI.ai_charge, -10, hover_fire_blaster), mframe_t(GameAI.ai_charge, 0, hover_reattack))

        var hover_move_attack1 = mmove_t(FRAME_attak104, FRAME_attak106, hover_frames_attack1, null)

        var hover_frames_end_attack = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null))

        var hover_move_end_attack = mmove_t(FRAME_attak107, FRAME_attak108, hover_frames_end_attack, hover_run)

        /*
     * QUAKED monster_hover (1 .5 0) (-16 -16 -24) (16 16 32) Ambush
     * Trigger_Spawn Sight
     */
        public fun SP_monster_hover(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_pain1 = GameBase.gi.soundindex("hover/hovpain1.wav")
            sound_pain2 = GameBase.gi.soundindex("hover/hovpain2.wav")
            sound_death1 = GameBase.gi.soundindex("hover/hovdeth1.wav")
            sound_death2 = GameBase.gi.soundindex("hover/hovdeth2.wav")
            sound_sight = GameBase.gi.soundindex("hover/hovsght1.wav")
            sound_search1 = GameBase.gi.soundindex("hover/hovsrch1.wav")
            sound_search2 = GameBase.gi.soundindex("hover/hovsrch2.wav")

            GameBase.gi.soundindex("hover/hovatck1.wav")

            self.s.sound = GameBase.gi.soundindex("hover/hovidle1.wav")

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/hover/tris.md2")
            Math3D.VectorSet(self.mins, -24, -24, -24)
            Math3D.VectorSet(self.maxs, 24, 24, 32)

            self.health = 240
            self.gib_health = -100
            self.mass = 150

            self.pain = hover_pain
            self.die = hover_die

            self.monsterinfo.stand = hover_stand
            self.monsterinfo.walk = hover_walk
            self.monsterinfo.run = hover_run
            //	 self.monsterinfo.dodge = hover_dodge;
            self.monsterinfo.attack = hover_start_attack
            self.monsterinfo.sight = hover_sight
            self.monsterinfo.search = hover_search

            GameBase.gi.linkentity(self)

            self.monsterinfo.currentmove = hover_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.flymonster_start.think(self)
        }
    }
}