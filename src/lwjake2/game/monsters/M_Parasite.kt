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
import lwjake2.Globals
import lwjake2.game.EntDieAdapter
import lwjake2.game.EntInteractAdapter
import lwjake2.game.EntPainAdapter
import lwjake2.game.EntThinkAdapter
import lwjake2.game.GameAI
import lwjake2.game.GameBase
import lwjake2.game.GameCombat
import lwjake2.game.GameMisc
import lwjake2.game.GameUtil
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.game.trace_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Parasite {
    companion object {

        // This file generated by ModelGen - Do NOT Modify

        public val FRAME_break01: Int = 0

        public val FRAME_break02: Int = 1

        public val FRAME_break03: Int = 2

        public val FRAME_break04: Int = 3

        public val FRAME_break05: Int = 4

        public val FRAME_break06: Int = 5

        public val FRAME_break07: Int = 6

        public val FRAME_break08: Int = 7

        public val FRAME_break09: Int = 8

        public val FRAME_break10: Int = 9

        public val FRAME_break11: Int = 10

        public val FRAME_break12: Int = 11

        public val FRAME_break13: Int = 12

        public val FRAME_break14: Int = 13

        public val FRAME_break15: Int = 14

        public val FRAME_break16: Int = 15

        public val FRAME_break17: Int = 16

        public val FRAME_break18: Int = 17

        public val FRAME_break19: Int = 18

        public val FRAME_break20: Int = 19

        public val FRAME_break21: Int = 20

        public val FRAME_break22: Int = 21

        public val FRAME_break23: Int = 22

        public val FRAME_break24: Int = 23

        public val FRAME_break25: Int = 24

        public val FRAME_break26: Int = 25

        public val FRAME_break27: Int = 26

        public val FRAME_break28: Int = 27

        public val FRAME_break29: Int = 28

        public val FRAME_break30: Int = 29

        public val FRAME_break31: Int = 30

        public val FRAME_break32: Int = 31

        public val FRAME_death101: Int = 32

        public val FRAME_death102: Int = 33

        public val FRAME_death103: Int = 34

        public val FRAME_death104: Int = 35

        public val FRAME_death105: Int = 36

        public val FRAME_death106: Int = 37

        public val FRAME_death107: Int = 38

        public val FRAME_drain01: Int = 39

        public val FRAME_drain02: Int = 40

        public val FRAME_drain03: Int = 41

        public val FRAME_drain04: Int = 42

        public val FRAME_drain05: Int = 43

        public val FRAME_drain06: Int = 44

        public val FRAME_drain07: Int = 45

        public val FRAME_drain08: Int = 46

        public val FRAME_drain09: Int = 47

        public val FRAME_drain10: Int = 48

        public val FRAME_drain11: Int = 49

        public val FRAME_drain12: Int = 50

        public val FRAME_drain13: Int = 51

        public val FRAME_drain14: Int = 52

        public val FRAME_drain15: Int = 53

        public val FRAME_drain16: Int = 54

        public val FRAME_drain17: Int = 55

        public val FRAME_drain18: Int = 56

        public val FRAME_pain101: Int = 57

        public val FRAME_pain102: Int = 58

        public val FRAME_pain103: Int = 59

        public val FRAME_pain104: Int = 60

        public val FRAME_pain105: Int = 61

        public val FRAME_pain106: Int = 62

        public val FRAME_pain107: Int = 63

        public val FRAME_pain108: Int = 64

        public val FRAME_pain109: Int = 65

        public val FRAME_pain110: Int = 66

        public val FRAME_pain111: Int = 67

        public val FRAME_run01: Int = 68

        public val FRAME_run02: Int = 69

        public val FRAME_run03: Int = 70

        public val FRAME_run04: Int = 71

        public val FRAME_run05: Int = 72

        public val FRAME_run06: Int = 73

        public val FRAME_run07: Int = 74

        public val FRAME_run08: Int = 75

        public val FRAME_run09: Int = 76

        public val FRAME_run10: Int = 77

        public val FRAME_run11: Int = 78

        public val FRAME_run12: Int = 79

        public val FRAME_run13: Int = 80

        public val FRAME_run14: Int = 81

        public val FRAME_run15: Int = 82

        public val FRAME_stand01: Int = 83

        public val FRAME_stand02: Int = 84

        public val FRAME_stand03: Int = 85

        public val FRAME_stand04: Int = 86

        public val FRAME_stand05: Int = 87

        public val FRAME_stand06: Int = 88

        public val FRAME_stand07: Int = 89

        public val FRAME_stand08: Int = 90

        public val FRAME_stand09: Int = 91

        public val FRAME_stand10: Int = 92

        public val FRAME_stand11: Int = 93

        public val FRAME_stand12: Int = 94

        public val FRAME_stand13: Int = 95

        public val FRAME_stand14: Int = 96

        public val FRAME_stand15: Int = 97

        public val FRAME_stand16: Int = 98

        public val FRAME_stand17: Int = 99

        public val FRAME_stand18: Int = 100

        public val FRAME_stand19: Int = 101

        public val FRAME_stand20: Int = 102

        public val FRAME_stand21: Int = 103

        public val FRAME_stand22: Int = 104

        public val FRAME_stand23: Int = 105

        public val FRAME_stand24: Int = 106

        public val FRAME_stand25: Int = 107

        public val FRAME_stand26: Int = 108

        public val FRAME_stand27: Int = 109

        public val FRAME_stand28: Int = 110

        public val FRAME_stand29: Int = 111

        public val FRAME_stand30: Int = 112

        public val FRAME_stand31: Int = 113

        public val FRAME_stand32: Int = 114

        public val FRAME_stand33: Int = 115

        public val FRAME_stand34: Int = 116

        public val FRAME_stand35: Int = 117

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_die: Int = 0

        var sound_launch: Int = 0

        var sound_impact: Int = 0

        var sound_suck: Int = 0

        var sound_reelin: Int = 0

        var sound_sight: Int = 0

        var sound_tap: Int = 0

        var sound_scratch: Int = 0

        var sound_search: Int = 0

        var parasite_launch: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_launch"
            }

            public fun think(self: edict_t): Boolean {

                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_launch, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var parasite_reel_in: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_reel_in"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_reelin, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var parasite_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "parasite_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var parasite_tap: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_tap"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_tap, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var parasite_scratch: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_scratch"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_scratch, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var parasite_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_search"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_search, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var parasite_start_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_start_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_start_walk
                return true
            }
        }

        var parasite_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_walk
                return true
            }
        }

        var parasite_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_stand
                return true
            }
        }

        var parasite_end_fidget: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_end_fidget"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_end_fidget
                return true
            }
        }

        var parasite_do_fidget: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_do_fidget"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_fidget
                return true
            }
        }

        var parasite_refidget: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_refidget"
            }

            public fun think(self: edict_t): Boolean {
                if (Lib.random() <= 0.8)
                    self.monsterinfo.currentmove = parasite_move_fidget
                else
                    self.monsterinfo.currentmove = parasite_move_end_fidget
                return true
            }
        }

        var parasite_idle: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_idle"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = parasite_move_start_fidget
                return true
            }
        }

        var parasite_start_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_start_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = parasite_move_stand
                else
                    self.monsterinfo.currentmove = parasite_move_start_run
                return true
            }
        }

        var parasite_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = parasite_move_stand
                else
                    self.monsterinfo.currentmove = parasite_move_run
                return true
            }
        }

        var parasite_frames_start_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var parasite_move_start_fidget = mmove_t(FRAME_stand18, FRAME_stand21, parasite_frames_start_fidget, parasite_do_fidget)

        var parasite_frames_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, parasite_scratch), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_scratch), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var parasite_move_fidget = mmove_t(FRAME_stand22, FRAME_stand27, parasite_frames_fidget, parasite_refidget)

        var parasite_frames_end_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, parasite_scratch), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var parasite_move_end_fidget = mmove_t(FRAME_stand28, FRAME_stand35, parasite_frames_end_fidget, parasite_stand)

        var parasite_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, parasite_tap))

        var parasite_move_stand = mmove_t(FRAME_stand01, FRAME_stand17, parasite_frames_stand, parasite_stand)

        var parasite_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 30, null), mframe_t(GameAI.ai_run, 30, null), mframe_t(GameAI.ai_run, 22, null), mframe_t(GameAI.ai_run, 19, null), mframe_t(GameAI.ai_run, 24, null), mframe_t(GameAI.ai_run, 28, null), mframe_t(GameAI.ai_run, 25, null))

        var parasite_move_run = mmove_t(FRAME_run03, FRAME_run09, parasite_frames_run, null)

        var parasite_frames_start_run = array<mframe_t>(mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 30, null))

        var parasite_move_start_run = mmove_t(FRAME_run01, FRAME_run02, parasite_frames_start_run, parasite_run)

        var parasite_frames_stop_run = array<mframe_t>(mframe_t(GameAI.ai_run, 20, null), mframe_t(GameAI.ai_run, 20, null), mframe_t(GameAI.ai_run, 12, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null))

        var parasite_move_stop_run = mmove_t(FRAME_run10, FRAME_run15, parasite_frames_stop_run, null)

        var parasite_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 30, null), mframe_t(GameAI.ai_walk, 30, null), mframe_t(GameAI.ai_walk, 22, null), mframe_t(GameAI.ai_walk, 19, null), mframe_t(GameAI.ai_walk, 24, null), mframe_t(GameAI.ai_walk, 28, null), mframe_t(GameAI.ai_walk, 25, null))

        var parasite_move_walk = mmove_t(FRAME_run03, FRAME_run09, parasite_frames_walk, parasite_walk)

        var parasite_frames_start_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 30, parasite_walk))

        var parasite_move_start_walk = mmove_t(FRAME_run01, FRAME_run02, parasite_frames_start_walk, null)

        var parasite_frames_stop_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 20, null), mframe_t(GameAI.ai_walk, 20, null), mframe_t(GameAI.ai_walk, 12, null), mframe_t(GameAI.ai_walk, 10, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null))

        var parasite_move_stop_walk = mmove_t(FRAME_run10, FRAME_run15, parasite_frames_stop_walk, null)

        var parasite_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 6, null), mframe_t(GameAI.ai_move, 16, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -7, null), mframe_t(GameAI.ai_move, 0, null))

        var parasite_move_pain1 = mmove_t(FRAME_pain101, FRAME_pain111, parasite_frames_pain1, parasite_start_run)

        var parasite_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "parasite_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3

                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)

                self.monsterinfo.currentmove = parasite_move_pain1
            }
        }

        var parasite_drain_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_drain_attack"
            }

            public fun think(self: edict_t): Boolean {
                val offset = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val f = floatArray(0.0, 0.0, 0.0)
                val r = floatArray(0.0, 0.0, 0.0)
                val end = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val tr: trace_t
                val damage: Int

                Math3D.AngleVectors(self.s.angles, f, r, null)
                Math3D.VectorSet(offset, 24, 0, 6)
                Math3D.G_ProjectSource(self.s.origin, offset, f, r, start)

                Math3D.VectorCopy(self.enemy.s.origin, end)
                if (!parasite_drain_attack_ok(start, end)) {
                    end[2] = self.enemy.s.origin[2] + self.enemy.maxs[2] - 8
                    if (!parasite_drain_attack_ok(start, end)) {
                        end[2] = self.enemy.s.origin[2] + self.enemy.mins[2] + 8
                        if (!parasite_drain_attack_ok(start, end))
                            return true
                    }
                }
                Math3D.VectorCopy(self.enemy.s.origin, end)

                tr = GameBase.gi.trace(start, null, null, end, self, Defines.MASK_SHOT)
                if (tr.ent != self.enemy)
                    return true

                if (self.s.frame == FRAME_drain03) {
                    damage = 5
                    GameBase.gi.sound(self.enemy, Defines.CHAN_AUTO, sound_impact, 1, Defines.ATTN_NORM, 0)
                } else {
                    if (self.s.frame == FRAME_drain04)
                        GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_suck, 1, Defines.ATTN_NORM, 0)
                    damage = 2
                }

                GameBase.gi.WriteByte(Defines.svc_temp_entity)
                GameBase.gi.WriteByte(Defines.TE_PARASITE_ATTACK)
                //gi.WriteShort(self - g_edicts);
                GameBase.gi.WriteShort(self.index)
                GameBase.gi.WritePosition(start)
                GameBase.gi.WritePosition(end)
                GameBase.gi.multicast(self.s.origin, Defines.MULTICAST_PVS)

                Math3D.VectorSubtract(start, end, dir)
                GameCombat.T_Damage(self.enemy, self, self, dir, self.enemy.s.origin, Globals.vec3_origin, damage, 0, Defines.DAMAGE_NO_KNOCKBACK, Defines.MOD_UNKNOWN)
                return true
            }
        }

        var parasite_frames_drain = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, parasite_launch), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 15, parasite_drain_attack), // Target hits)
                mframe_t(GameAI.ai_charge, 0, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, 0, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, 0, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, 0, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, -2, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, -2, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, -3, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, -2, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, 0, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, -1, parasite_drain_attack), // drain)
                mframe_t(GameAI.ai_charge, 0, parasite_reel_in), // let go)
                mframe_t(GameAI.ai_charge, -2, null), mframe_t(GameAI.ai_charge, -2, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 0, null))

        var parasite_move_drain = mmove_t(FRAME_drain01, FRAME_drain18, parasite_frames_drain, parasite_start_run)

        var parasite_frames_break = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 2, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 3, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -18, null), mframe_t(GameAI.ai_charge, 3, null), mframe_t(GameAI.ai_charge, 9, null), mframe_t(GameAI.ai_charge, 6, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -18, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 8, null), mframe_t(GameAI.ai_charge, 9, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -18, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), /* airborne */
                mframe_t(GameAI.ai_charge, 0, null), /* slides */
                mframe_t(GameAI.ai_charge, 0, null), /* slides */
                mframe_t(GameAI.ai_charge, 0, null), /* slides */
                mframe_t(GameAI.ai_charge, 0, null), /* slides */
                mframe_t(GameAI.ai_charge, 4, null), mframe_t(GameAI.ai_charge, 11, null), mframe_t(GameAI.ai_charge, -2, null), mframe_t(GameAI.ai_charge, -5, null), mframe_t(GameAI.ai_charge, 1, null))

        var parasite_move_break = mmove_t(FRAME_break01, FRAME_break32, parasite_frames_break, parasite_start_run)

        /*
     * === Break Stuff Ends ===
     */

        var parasite_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_attack"
            }

            public fun think(self: edict_t): Boolean {
                //	if (random() <= 0.2)
                //		self.monsterinfo.currentmove = &parasite_move_break;
                //	else
                self.monsterinfo.currentmove = parasite_move_drain
                return true
            }
        }

        /*
     * === Death Stuff Starts ===
     */

        var parasite_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "parasite_dead"
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

        var parasite_frames_death = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var parasite_move_death = mmove_t(FRAME_death101, FRAME_death107, parasite_frames_death, parasite_dead)

        var parasite_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "parasite_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

                // check for gib
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

                // regular death
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_die, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES
                self.monsterinfo.currentmove = parasite_move_death
            }
        }

        /*
     * === End Death Stuff ===
     */

        /*
     * QUAKED monster_parasite (1 .5 0) (-16 -16 -24) (16 16 32) Ambush
     * Trigger_Spawn Sight
     */

        public var SP_monster_parasite: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_parasite"
            }

            public fun think(self: edict_t): Boolean {
                if (GameBase.deathmatch.value != 0) {
                    GameUtil.G_FreeEdict(self)
                    return true
                }

                sound_pain1 = GameBase.gi.soundindex("parasite/parpain1.wav")
                sound_pain2 = GameBase.gi.soundindex("parasite/parpain2.wav")
                sound_die = GameBase.gi.soundindex("parasite/pardeth1.wav")
                sound_launch = GameBase.gi.soundindex("parasite/paratck1.wav")
                sound_impact = GameBase.gi.soundindex("parasite/paratck2.wav")
                sound_suck = GameBase.gi.soundindex("parasite/paratck3.wav")
                sound_reelin = GameBase.gi.soundindex("parasite/paratck4.wav")
                sound_sight = GameBase.gi.soundindex("parasite/parsght1.wav")
                sound_tap = GameBase.gi.soundindex("parasite/paridle1.wav")
                sound_scratch = GameBase.gi.soundindex("parasite/paridle2.wav")
                sound_search = GameBase.gi.soundindex("parasite/parsrch1.wav")

                self.s.modelindex = GameBase.gi.modelindex("models/monsters/parasite/tris.md2")
                Math3D.VectorSet(self.mins, -16, -16, -24)
                Math3D.VectorSet(self.maxs, 16, 16, 24)
                self.movetype = Defines.MOVETYPE_STEP
                self.solid = Defines.SOLID_BBOX

                self.health = 175
                self.gib_health = -50
                self.mass = 250

                self.pain = parasite_pain
                self.die = parasite_die

                self.monsterinfo.stand = parasite_stand
                self.monsterinfo.walk = parasite_start_walk
                self.monsterinfo.run = parasite_start_run
                self.monsterinfo.attack = parasite_attack
                self.monsterinfo.sight = parasite_sight
                self.monsterinfo.idle = parasite_idle

                GameBase.gi.linkentity(self)

                self.monsterinfo.currentmove = parasite_move_stand
                self.monsterinfo.scale = MODEL_SCALE

                GameAI.walkmonster_start.think(self)

                return true
            }
        }

        fun parasite_drain_attack_ok(start: FloatArray, end: FloatArray): Boolean {
            val dir = floatArray(0.0, 0.0, 0.0)
            val angles = floatArray(0.0, 0.0, 0.0)

            // check for max distance
            Math3D.VectorSubtract(start, end, dir)
            if (Math3D.VectorLength(dir) > 256)
                return false

            // check for min/max pitch
            Math3D.vectoangles(dir, angles)
            if (angles[0] < -180)
                angles[0] += 360
            if (Math.abs(angles[0]) > 30)
                return false

            return true
        }
    }
}