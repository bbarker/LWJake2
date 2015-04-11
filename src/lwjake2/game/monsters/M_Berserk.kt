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
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Berserk {
    companion object {

        public val FRAME_stand1: Int = 0

        public val FRAME_stand2: Int = 1

        public val FRAME_stand3: Int = 2

        public val FRAME_stand4: Int = 3

        public val FRAME_stand5: Int = 4

        public val FRAME_standb1: Int = 5

        public val FRAME_standb2: Int = 6

        public val FRAME_standb3: Int = 7

        public val FRAME_standb4: Int = 8

        public val FRAME_standb5: Int = 9

        public val FRAME_standb6: Int = 10

        public val FRAME_standb7: Int = 11

        public val FRAME_standb8: Int = 12

        public val FRAME_standb9: Int = 13

        public val FRAME_standb10: Int = 14

        public val FRAME_standb11: Int = 15

        public val FRAME_standb12: Int = 16

        public val FRAME_standb13: Int = 17

        public val FRAME_standb14: Int = 18

        public val FRAME_standb15: Int = 19

        public val FRAME_standb16: Int = 20

        public val FRAME_standb17: Int = 21

        public val FRAME_standb18: Int = 22

        public val FRAME_standb19: Int = 23

        public val FRAME_standb20: Int = 24

        public val FRAME_walkc1: Int = 25

        public val FRAME_walkc2: Int = 26

        public val FRAME_walkc3: Int = 27

        public val FRAME_walkc4: Int = 28

        public val FRAME_walkc5: Int = 29

        public val FRAME_walkc6: Int = 30

        public val FRAME_walkc7: Int = 31

        public val FRAME_walkc8: Int = 32

        public val FRAME_walkc9: Int = 33

        public val FRAME_walkc10: Int = 34

        public val FRAME_walkc11: Int = 35

        public val FRAME_run1: Int = 36

        public val FRAME_run2: Int = 37

        public val FRAME_run3: Int = 38

        public val FRAME_run4: Int = 39

        public val FRAME_run5: Int = 40

        public val FRAME_run6: Int = 41

        public val FRAME_att_a1: Int = 42

        public val FRAME_att_a2: Int = 43

        public val FRAME_att_a3: Int = 44

        public val FRAME_att_a4: Int = 45

        public val FRAME_att_a5: Int = 46

        public val FRAME_att_a6: Int = 47

        public val FRAME_att_a7: Int = 48

        public val FRAME_att_a8: Int = 49

        public val FRAME_att_a9: Int = 50

        public val FRAME_att_a10: Int = 51

        public val FRAME_att_a11: Int = 52

        public val FRAME_att_a12: Int = 53

        public val FRAME_att_a13: Int = 54

        public val FRAME_att_b1: Int = 55

        public val FRAME_att_b2: Int = 56

        public val FRAME_att_b3: Int = 57

        public val FRAME_att_b4: Int = 58

        public val FRAME_att_b5: Int = 59

        public val FRAME_att_b6: Int = 60

        public val FRAME_att_b7: Int = 61

        public val FRAME_att_b8: Int = 62

        public val FRAME_att_b9: Int = 63

        public val FRAME_att_b10: Int = 64

        public val FRAME_att_b11: Int = 65

        public val FRAME_att_b12: Int = 66

        public val FRAME_att_b13: Int = 67

        public val FRAME_att_b14: Int = 68

        public val FRAME_att_b15: Int = 69

        public val FRAME_att_b16: Int = 70

        public val FRAME_att_b17: Int = 71

        public val FRAME_att_b18: Int = 72

        public val FRAME_att_b19: Int = 73

        public val FRAME_att_b20: Int = 74

        public val FRAME_att_b21: Int = 75

        public val FRAME_att_c1: Int = 76

        public val FRAME_att_c2: Int = 77

        public val FRAME_att_c3: Int = 78

        public val FRAME_att_c4: Int = 79

        public val FRAME_att_c5: Int = 80

        public val FRAME_att_c6: Int = 81

        public val FRAME_att_c7: Int = 82

        public val FRAME_att_c8: Int = 83

        public val FRAME_att_c9: Int = 84

        public val FRAME_att_c10: Int = 85

        public val FRAME_att_c11: Int = 86

        public val FRAME_att_c12: Int = 87

        public val FRAME_att_c13: Int = 88

        public val FRAME_att_c14: Int = 89

        public val FRAME_att_c15: Int = 90

        public val FRAME_att_c16: Int = 91

        public val FRAME_att_c17: Int = 92

        public val FRAME_att_c18: Int = 93

        public val FRAME_att_c19: Int = 94

        public val FRAME_att_c20: Int = 95

        public val FRAME_att_c21: Int = 96

        public val FRAME_att_c22: Int = 97

        public val FRAME_att_c23: Int = 98

        public val FRAME_att_c24: Int = 99

        public val FRAME_att_c25: Int = 100

        public val FRAME_att_c26: Int = 101

        public val FRAME_att_c27: Int = 102

        public val FRAME_att_c28: Int = 103

        public val FRAME_att_c29: Int = 104

        public val FRAME_att_c30: Int = 105

        public val FRAME_att_c31: Int = 106

        public val FRAME_att_c32: Int = 107

        public val FRAME_att_c33: Int = 108

        public val FRAME_att_c34: Int = 109

        public val FRAME_r_att1: Int = 110

        public val FRAME_r_att2: Int = 111

        public val FRAME_r_att3: Int = 112

        public val FRAME_r_att4: Int = 113

        public val FRAME_r_att5: Int = 114

        public val FRAME_r_att6: Int = 115

        public val FRAME_r_att7: Int = 116

        public val FRAME_r_att8: Int = 117

        public val FRAME_r_att9: Int = 118

        public val FRAME_r_att10: Int = 119

        public val FRAME_r_att11: Int = 120

        public val FRAME_r_att12: Int = 121

        public val FRAME_r_att13: Int = 122

        public val FRAME_r_att14: Int = 123

        public val FRAME_r_att15: Int = 124

        public val FRAME_r_att16: Int = 125

        public val FRAME_r_att17: Int = 126

        public val FRAME_r_att18: Int = 127

        public val FRAME_r_attb1: Int = 128

        public val FRAME_r_attb2: Int = 129

        public val FRAME_r_attb3: Int = 130

        public val FRAME_r_attb4: Int = 131

        public val FRAME_r_attb5: Int = 132

        public val FRAME_r_attb6: Int = 133

        public val FRAME_r_attb7: Int = 134

        public val FRAME_r_attb8: Int = 135

        public val FRAME_r_attb9: Int = 136

        public val FRAME_r_attb10: Int = 137

        public val FRAME_r_attb11: Int = 138

        public val FRAME_r_attb12: Int = 139

        public val FRAME_r_attb13: Int = 140

        public val FRAME_r_attb14: Int = 141

        public val FRAME_r_attb15: Int = 142

        public val FRAME_r_attb16: Int = 143

        public val FRAME_r_attb17: Int = 144

        public val FRAME_r_attb18: Int = 145

        public val FRAME_slam1: Int = 146

        public val FRAME_slam2: Int = 147

        public val FRAME_slam3: Int = 148

        public val FRAME_slam4: Int = 149

        public val FRAME_slam5: Int = 150

        public val FRAME_slam6: Int = 151

        public val FRAME_slam7: Int = 152

        public val FRAME_slam8: Int = 153

        public val FRAME_slam9: Int = 154

        public val FRAME_slam10: Int = 155

        public val FRAME_slam11: Int = 156

        public val FRAME_slam12: Int = 157

        public val FRAME_slam13: Int = 158

        public val FRAME_slam14: Int = 159

        public val FRAME_slam15: Int = 160

        public val FRAME_slam16: Int = 161

        public val FRAME_slam17: Int = 162

        public val FRAME_slam18: Int = 163

        public val FRAME_slam19: Int = 164

        public val FRAME_slam20: Int = 165

        public val FRAME_slam21: Int = 166

        public val FRAME_slam22: Int = 167

        public val FRAME_slam23: Int = 168

        public val FRAME_duck1: Int = 169

        public val FRAME_duck2: Int = 170

        public val FRAME_duck3: Int = 171

        public val FRAME_duck4: Int = 172

        public val FRAME_duck5: Int = 173

        public val FRAME_duck6: Int = 174

        public val FRAME_duck7: Int = 175

        public val FRAME_duck8: Int = 176

        public val FRAME_duck9: Int = 177

        public val FRAME_duck10: Int = 178

        public val FRAME_fall1: Int = 179

        public val FRAME_fall2: Int = 180

        public val FRAME_fall3: Int = 181

        public val FRAME_fall4: Int = 182

        public val FRAME_fall5: Int = 183

        public val FRAME_fall6: Int = 184

        public val FRAME_fall7: Int = 185

        public val FRAME_fall8: Int = 186

        public val FRAME_fall9: Int = 187

        public val FRAME_fall10: Int = 188

        public val FRAME_fall11: Int = 189

        public val FRAME_fall12: Int = 190

        public val FRAME_fall13: Int = 191

        public val FRAME_fall14: Int = 192

        public val FRAME_fall15: Int = 193

        public val FRAME_fall16: Int = 194

        public val FRAME_fall17: Int = 195

        public val FRAME_fall18: Int = 196

        public val FRAME_fall19: Int = 197

        public val FRAME_fall20: Int = 198

        public val FRAME_painc1: Int = 199

        public val FRAME_painc2: Int = 200

        public val FRAME_painc3: Int = 201

        public val FRAME_painc4: Int = 202

        public val FRAME_painb1: Int = 203

        public val FRAME_painb2: Int = 204

        public val FRAME_painb3: Int = 205

        public val FRAME_painb4: Int = 206

        public val FRAME_painb5: Int = 207

        public val FRAME_painb6: Int = 208

        public val FRAME_painb7: Int = 209

        public val FRAME_painb8: Int = 210

        public val FRAME_painb9: Int = 211

        public val FRAME_painb10: Int = 212

        public val FRAME_painb11: Int = 213

        public val FRAME_painb12: Int = 214

        public val FRAME_painb13: Int = 215

        public val FRAME_painb14: Int = 216

        public val FRAME_painb15: Int = 217

        public val FRAME_painb16: Int = 218

        public val FRAME_painb17: Int = 219

        public val FRAME_painb18: Int = 220

        public val FRAME_painb19: Int = 221

        public val FRAME_painb20: Int = 222

        public val FRAME_death1: Int = 223

        public val FRAME_death2: Int = 224

        public val FRAME_death3: Int = 225

        public val FRAME_death4: Int = 226

        public val FRAME_death5: Int = 227

        public val FRAME_death6: Int = 228

        public val FRAME_death7: Int = 229

        public val FRAME_death8: Int = 230

        public val FRAME_death9: Int = 231

        public val FRAME_death10: Int = 232

        public val FRAME_death11: Int = 233

        public val FRAME_death12: Int = 234

        public val FRAME_death13: Int = 235

        public val FRAME_deathc1: Int = 236

        public val FRAME_deathc2: Int = 237

        public val FRAME_deathc3: Int = 238

        public val FRAME_deathc4: Int = 239

        public val FRAME_deathc5: Int = 240

        public val FRAME_deathc6: Int = 241

        public val FRAME_deathc7: Int = 242

        public val FRAME_deathc8: Int = 243

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_pain: Int = 0

        var sound_die: Int = 0

        var sound_idle: Int = 0

        var sound_punch: Int = 0

        var sound_sight: Int = 0

        var sound_search: Int = 0

        var berserk_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "berserk_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var berserk_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_search"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var berserk_fidget: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_fidget"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    return true

                if (Lib.random() > 0.15.toFloat())
                    return true

                self.monsterinfo.currentmove = berserk_move_stand_fidget
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_idle, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var berserk_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, berserk_fidget), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var berserk_move_stand = mmove_t(FRAME_stand1, FRAME_stand5, berserk_frames_stand, null)

        var berserk_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = berserk_move_stand
                return true
            }
        }

        var berserk_frames_stand_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var berserk_move_stand_fidget = mmove_t(FRAME_standb1, FRAME_standb20, berserk_frames_stand_fidget, berserk_stand)

        var berserk_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 9.1.toFloat(), null), mframe_t(GameAI.ai_walk, 6.3.toFloat(), null), mframe_t(GameAI.ai_walk, 4.9.toFloat(), null), mframe_t(GameAI.ai_walk, 6.7.toFloat(), null), mframe_t(GameAI.ai_walk, 6.0.toFloat(), null), mframe_t(GameAI.ai_walk, 8.2.toFloat(), null), mframe_t(GameAI.ai_walk, 7.2.toFloat(), null), mframe_t(GameAI.ai_walk, 6.1.toFloat(), null), mframe_t(GameAI.ai_walk, 4.9.toFloat(), null), mframe_t(GameAI.ai_walk, 4.7.toFloat(), null), mframe_t(GameAI.ai_walk, 4.7.toFloat(), null), mframe_t(GameAI.ai_walk, 4.8.toFloat(), null))

        var berserk_move_walk = mmove_t(FRAME_walkc1, FRAME_walkc11, berserk_frames_walk, null)

        var berserk_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = berserk_move_walk
                return true
            }
        }

        /*
     * 
     * **************************** SKIPPED THIS FOR NOW!
     * ****************************
     * 
     * Running . Arm raised in air
     * 
     * void() berserk_runb1 =[ $r_att1 , berserk_runb2 ] {ai_run(21);}; void()
     * berserk_runb2 =[ $r_att2 , berserk_runb3 ] {ai_run(11);}; void()
     * berserk_runb3 =[ $r_att3 , berserk_runb4 ] {ai_run(21);}; void()
     * berserk_runb4 =[ $r_att4 , berserk_runb5 ] {ai_run(25);}; void()
     * berserk_runb5 =[ $r_att5 , berserk_runb6 ] {ai_run(18);}; void()
     * berserk_runb6 =[ $r_att6 , berserk_runb7 ] {ai_run(19);}; // running with
     * arm in air : start loop void() berserk_runb7 =[ $r_att7 , berserk_runb8 ]
     * {ai_run(21);}; void() berserk_runb8 =[ $r_att8 , berserk_runb9 ]
     * {ai_run(11);}; void() berserk_runb9 =[ $r_att9 , berserk_runb10 ]
     * {ai_run(21);}; void() berserk_runb10 =[ $r_att10 , berserk_runb11 ]
     * {ai_run(25);}; void() berserk_runb11 =[ $r_att11 , berserk_runb12 ]
     * {ai_run(18);}; void() berserk_runb12 =[ $r_att12 , berserk_runb7 ]
     * {ai_run(19);}; // running with arm in air : end loop
     */

        var berserk_frames_run1 = array<mframe_t>(mframe_t(GameAI.ai_run, 21, null), mframe_t(GameAI.ai_run, 11, null), mframe_t(GameAI.ai_run, 21, null), mframe_t(GameAI.ai_run, 25, null), mframe_t(GameAI.ai_run, 18, null), mframe_t(GameAI.ai_run, 19, null))

        var berserk_move_run1 = mmove_t(FRAME_run1, FRAME_run6, berserk_frames_run1, null)

        var berserk_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = berserk_move_stand
                else
                    self.monsterinfo.currentmove = berserk_move_run1
                return true
            }
        }

        var berserk_attack_spike: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_attack_spike"
            }

            public fun think(self: edict_t): Boolean {
                val aim = floatArray(Defines.MELEE_DISTANCE.toFloat(), 0.toFloat(), (-24.toFloat()).toFloat())

                GameWeapon.fire_hit(self, aim, (15 + (Lib.rand() % 6)), 400)
                //	Faster attack -- upwards and backwards

                return true
            }
        }

        var berserk_swing: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_swing"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_punch, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var berserk_frames_attack_spike = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, berserk_swing), mframe_t(GameAI.ai_charge, 0, berserk_attack_spike), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null))

        var berserk_move_attack_spike = mmove_t(FRAME_att_c1, FRAME_att_c8, berserk_frames_attack_spike, berserk_run)

        var berserk_attack_club: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_attack_club"
            }

            public fun think(self: edict_t): Boolean {
                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, self.mins[0], -4)
                GameWeapon.fire_hit(self, aim, (5 + (Lib.rand() % 6)), 400) // Slower
                // attack

                return true
            }
        }

        var berserk_frames_attack_club = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, berserk_swing), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, berserk_attack_club), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null))

        var berserk_move_attack_club = mmove_t(FRAME_att_c9, FRAME_att_c20, berserk_frames_attack_club, berserk_run)

        var berserk_strike: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_strike"
            }

            public fun think(self: edict_t): Boolean {
                return true
            }
        }

        var berserk_frames_attack_strike = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, berserk_swing), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, berserk_strike), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 9.7.toFloat(), null), mframe_t(GameAI.ai_move, 13.6.toFloat(), null))

        var berserk_move_attack_strike = mmove_t(FRAME_att_c21, FRAME_att_c34, berserk_frames_attack_strike, berserk_run)

        var berserk_melee: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_melee"
            }

            public fun think(self: edict_t): Boolean {
                if ((Lib.rand() % 2) == 0)
                    self.monsterinfo.currentmove = berserk_move_attack_spike
                else
                    self.monsterinfo.currentmove = berserk_move_attack_club
                return true
            }
        }

        /*
     * void() berserk_atke1 =[ $r_attb1, berserk_atke2 ] {ai_run(9);}; void()
     * berserk_atke2 =[ $r_attb2, berserk_atke3 ] {ai_run(6);}; void()
     * berserk_atke3 =[ $r_attb3, berserk_atke4 ] {ai_run(18.4);}; void()
     * berserk_atke4 =[ $r_attb4, berserk_atke5 ] {ai_run(25);}; void()
     * berserk_atke5 =[ $r_attb5, berserk_atke6 ] {ai_run(14);}; void()
     * berserk_atke6 =[ $r_attb6, berserk_atke7 ] {ai_run(20);}; void()
     * berserk_atke7 =[ $r_attb7, berserk_atke8 ] {ai_run(8.5);}; void()
     * berserk_atke8 =[ $r_attb8, berserk_atke9 ] {ai_run(3);}; void()
     * berserk_atke9 =[ $r_attb9, berserk_atke10 ] {ai_run(17.5);}; void()
     * berserk_atke10 =[ $r_attb10, berserk_atke11 ] {ai_run(17);}; void()
     * berserk_atke11 =[ $r_attb11, berserk_atke12 ] {ai_run(9);}; void()
     * berserk_atke12 =[ $r_attb12, berserk_atke13 ] {ai_run(25);}; void()
     * berserk_atke13 =[ $r_attb13, berserk_atke14 ] {ai_run(3.7);}; void()
     * berserk_atke14 =[ $r_attb14, berserk_atke15 ] {ai_run(2.6);}; void()
     * berserk_atke15 =[ $r_attb15, berserk_atke16 ] {ai_run(19);}; void()
     * berserk_atke16 =[ $r_attb16, berserk_atke17 ] {ai_run(25);}; void()
     * berserk_atke17 =[ $r_attb17, berserk_atke18 ] {ai_run(19.6);}; void()
     * berserk_atke18 =[ $r_attb18, berserk_run1 ] {ai_run(7.8);};
     */

        var berserk_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var berserk_move_pain1 = mmove_t(FRAME_painc1, FRAME_painc4, berserk_frames_pain1, berserk_run)

        var berserk_frames_pain2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var berserk_move_pain2 = mmove_t(FRAME_painb1, FRAME_painb20, berserk_frames_pain2, berserk_run)

        var berserk_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "berserk_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain, 1, Defines.ATTN_NORM, 0)

                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if ((damage < 20) || (Lib.random() < 0.5))
                    self.monsterinfo.currentmove = berserk_move_pain1
                else
                    self.monsterinfo.currentmove = berserk_move_pain2
            }
        }

        var berserk_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "berserk_dead"
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

        var berserk_frames_death1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var berserk_move_death1 = mmove_t(FRAME_death1, FRAME_death13, berserk_frames_death1, berserk_dead)

        var berserk_frames_death2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var berserk_move_death2 = mmove_t(FRAME_deathc1, FRAME_deathc8, berserk_frames_death2, berserk_dead)

        var berserk_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "berserk_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

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

                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_die, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES

                if (damage >= 50)
                    self.monsterinfo.currentmove = berserk_move_death1
                else
                    self.monsterinfo.currentmove = berserk_move_death2
            }
        }

        /*
     * QUAKED monster_berserk (1 .5 0) (-16 -16 -24) (16 16 32) Ambush
     * Trigger_Spawn Sight
     */
        public fun SP_monster_berserk(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            // pre-caches
            sound_pain = GameBase.gi.soundindex("berserk/berpain2.wav")
            sound_die = GameBase.gi.soundindex("berserk/berdeth2.wav")
            sound_idle = GameBase.gi.soundindex("berserk/beridle1.wav")
            sound_punch = GameBase.gi.soundindex("berserk/attack.wav")
            sound_search = GameBase.gi.soundindex("berserk/bersrch1.wav")
            sound_sight = GameBase.gi.soundindex("berserk/sight.wav")

            self.s.modelindex = GameBase.gi.modelindex("models/monsters/berserk/tris.md2")
            Math3D.VectorSet(self.mins, -16, -16, -24)
            Math3D.VectorSet(self.maxs, 16, 16, 32)
            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX

            self.health = 240
            self.gib_health = -60
            self.mass = 250

            self.pain = berserk_pain
            self.die = berserk_die

            self.monsterinfo.stand = berserk_stand
            self.monsterinfo.walk = berserk_walk
            self.monsterinfo.run = berserk_run
            self.monsterinfo.dodge = null
            self.monsterinfo.attack = null
            self.monsterinfo.melee = berserk_melee
            self.monsterinfo.sight = berserk_sight
            self.monsterinfo.search = berserk_search

            self.monsterinfo.currentmove = berserk_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameBase.gi.linkentity(self)

            GameAI.walkmonster_start.think(self)
        }
    }
}