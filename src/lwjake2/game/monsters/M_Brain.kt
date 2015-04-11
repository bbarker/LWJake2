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
import lwjake2.game.EntDodgeAdapter
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

public class M_Brain {
    companion object {

        public val FRAME_walk101: Int = 0

        public val FRAME_walk102: Int = 1

        public val FRAME_walk103: Int = 2

        public val FRAME_walk104: Int = 3

        public val FRAME_walk105: Int = 4

        public val FRAME_walk106: Int = 5

        public val FRAME_walk107: Int = 6

        public val FRAME_walk108: Int = 7

        public val FRAME_walk109: Int = 8

        public val FRAME_walk110: Int = 9

        public val FRAME_walk111: Int = 10

        public val FRAME_walk112: Int = 11

        public val FRAME_walk113: Int = 12

        public val FRAME_walk201: Int = 13

        public val FRAME_walk202: Int = 14

        public val FRAME_walk203: Int = 15

        public val FRAME_walk204: Int = 16

        public val FRAME_walk205: Int = 17

        public val FRAME_walk206: Int = 18

        public val FRAME_walk207: Int = 19

        public val FRAME_walk208: Int = 20

        public val FRAME_walk209: Int = 21

        public val FRAME_walk210: Int = 22

        public val FRAME_walk211: Int = 23

        public val FRAME_walk212: Int = 24

        public val FRAME_walk213: Int = 25

        public val FRAME_walk214: Int = 26

        public val FRAME_walk215: Int = 27

        public val FRAME_walk216: Int = 28

        public val FRAME_walk217: Int = 29

        public val FRAME_walk218: Int = 30

        public val FRAME_walk219: Int = 31

        public val FRAME_walk220: Int = 32

        public val FRAME_walk221: Int = 33

        public val FRAME_walk222: Int = 34

        public val FRAME_walk223: Int = 35

        public val FRAME_walk224: Int = 36

        public val FRAME_walk225: Int = 37

        public val FRAME_walk226: Int = 38

        public val FRAME_walk227: Int = 39

        public val FRAME_walk228: Int = 40

        public val FRAME_walk229: Int = 41

        public val FRAME_walk230: Int = 42

        public val FRAME_walk231: Int = 43

        public val FRAME_walk232: Int = 44

        public val FRAME_walk233: Int = 45

        public val FRAME_walk234: Int = 46

        public val FRAME_walk235: Int = 47

        public val FRAME_walk236: Int = 48

        public val FRAME_walk237: Int = 49

        public val FRAME_walk238: Int = 50

        public val FRAME_walk239: Int = 51

        public val FRAME_walk240: Int = 52

        public val FRAME_attak101: Int = 53

        public val FRAME_attak102: Int = 54

        public val FRAME_attak103: Int = 55

        public val FRAME_attak104: Int = 56

        public val FRAME_attak105: Int = 57

        public val FRAME_attak106: Int = 58

        public val FRAME_attak107: Int = 59

        public val FRAME_attak108: Int = 60

        public val FRAME_attak109: Int = 61

        public val FRAME_attak110: Int = 62

        public val FRAME_attak111: Int = 63

        public val FRAME_attak112: Int = 64

        public val FRAME_attak113: Int = 65

        public val FRAME_attak114: Int = 66

        public val FRAME_attak115: Int = 67

        public val FRAME_attak116: Int = 68

        public val FRAME_attak117: Int = 69

        public val FRAME_attak118: Int = 70

        public val FRAME_attak201: Int = 71

        public val FRAME_attak202: Int = 72

        public val FRAME_attak203: Int = 73

        public val FRAME_attak204: Int = 74

        public val FRAME_attak205: Int = 75

        public val FRAME_attak206: Int = 76

        public val FRAME_attak207: Int = 77

        public val FRAME_attak208: Int = 78

        public val FRAME_attak209: Int = 79

        public val FRAME_attak210: Int = 80

        public val FRAME_attak211: Int = 81

        public val FRAME_attak212: Int = 82

        public val FRAME_attak213: Int = 83

        public val FRAME_attak214: Int = 84

        public val FRAME_attak215: Int = 85

        public val FRAME_attak216: Int = 86

        public val FRAME_attak217: Int = 87

        public val FRAME_pain101: Int = 88

        public val FRAME_pain102: Int = 89

        public val FRAME_pain103: Int = 90

        public val FRAME_pain104: Int = 91

        public val FRAME_pain105: Int = 92

        public val FRAME_pain106: Int = 93

        public val FRAME_pain107: Int = 94

        public val FRAME_pain108: Int = 95

        public val FRAME_pain109: Int = 96

        public val FRAME_pain110: Int = 97

        public val FRAME_pain111: Int = 98

        public val FRAME_pain112: Int = 99

        public val FRAME_pain113: Int = 100

        public val FRAME_pain114: Int = 101

        public val FRAME_pain115: Int = 102

        public val FRAME_pain116: Int = 103

        public val FRAME_pain117: Int = 104

        public val FRAME_pain118: Int = 105

        public val FRAME_pain119: Int = 106

        public val FRAME_pain120: Int = 107

        public val FRAME_pain121: Int = 108

        public val FRAME_pain201: Int = 109

        public val FRAME_pain202: Int = 110

        public val FRAME_pain203: Int = 111

        public val FRAME_pain204: Int = 112

        public val FRAME_pain205: Int = 113

        public val FRAME_pain206: Int = 114

        public val FRAME_pain207: Int = 115

        public val FRAME_pain208: Int = 116

        public val FRAME_pain301: Int = 117

        public val FRAME_pain302: Int = 118

        public val FRAME_pain303: Int = 119

        public val FRAME_pain304: Int = 120

        public val FRAME_pain305: Int = 121

        public val FRAME_pain306: Int = 122

        public val FRAME_death101: Int = 123

        public val FRAME_death102: Int = 124

        public val FRAME_death103: Int = 125

        public val FRAME_death104: Int = 126

        public val FRAME_death105: Int = 127

        public val FRAME_death106: Int = 128

        public val FRAME_death107: Int = 129

        public val FRAME_death108: Int = 130

        public val FRAME_death109: Int = 131

        public val FRAME_death110: Int = 132

        public val FRAME_death111: Int = 133

        public val FRAME_death112: Int = 134

        public val FRAME_death113: Int = 135

        public val FRAME_death114: Int = 136

        public val FRAME_death115: Int = 137

        public val FRAME_death116: Int = 138

        public val FRAME_death117: Int = 139

        public val FRAME_death118: Int = 140

        public val FRAME_death201: Int = 141

        public val FRAME_death202: Int = 142

        public val FRAME_death203: Int = 143

        public val FRAME_death204: Int = 144

        public val FRAME_death205: Int = 145

        public val FRAME_duck01: Int = 146

        public val FRAME_duck02: Int = 147

        public val FRAME_duck03: Int = 148

        public val FRAME_duck04: Int = 149

        public val FRAME_duck05: Int = 150

        public val FRAME_duck06: Int = 151

        public val FRAME_duck07: Int = 152

        public val FRAME_duck08: Int = 153

        public val FRAME_defens01: Int = 154

        public val FRAME_defens02: Int = 155

        public val FRAME_defens03: Int = 156

        public val FRAME_defens04: Int = 157

        public val FRAME_defens05: Int = 158

        public val FRAME_defens06: Int = 159

        public val FRAME_defens07: Int = 160

        public val FRAME_defens08: Int = 161

        public val FRAME_stand01: Int = 162

        public val FRAME_stand02: Int = 163

        public val FRAME_stand03: Int = 164

        public val FRAME_stand04: Int = 165

        public val FRAME_stand05: Int = 166

        public val FRAME_stand06: Int = 167

        public val FRAME_stand07: Int = 168

        public val FRAME_stand08: Int = 169

        public val FRAME_stand09: Int = 170

        public val FRAME_stand10: Int = 171

        public val FRAME_stand11: Int = 172

        public val FRAME_stand12: Int = 173

        public val FRAME_stand13: Int = 174

        public val FRAME_stand14: Int = 175

        public val FRAME_stand15: Int = 176

        public val FRAME_stand16: Int = 177

        public val FRAME_stand17: Int = 178

        public val FRAME_stand18: Int = 179

        public val FRAME_stand19: Int = 180

        public val FRAME_stand20: Int = 181

        public val FRAME_stand21: Int = 182

        public val FRAME_stand22: Int = 183

        public val FRAME_stand23: Int = 184

        public val FRAME_stand24: Int = 185

        public val FRAME_stand25: Int = 186

        public val FRAME_stand26: Int = 187

        public val FRAME_stand27: Int = 188

        public val FRAME_stand28: Int = 189

        public val FRAME_stand29: Int = 190

        public val FRAME_stand30: Int = 191

        public val FRAME_stand31: Int = 192

        public val FRAME_stand32: Int = 193

        public val FRAME_stand33: Int = 194

        public val FRAME_stand34: Int = 195

        public val FRAME_stand35: Int = 196

        public val FRAME_stand36: Int = 197

        public val FRAME_stand37: Int = 198

        public val FRAME_stand38: Int = 199

        public val FRAME_stand39: Int = 200

        public val FRAME_stand40: Int = 201

        public val FRAME_stand41: Int = 202

        public val FRAME_stand42: Int = 203

        public val FRAME_stand43: Int = 204

        public val FRAME_stand44: Int = 205

        public val FRAME_stand45: Int = 206

        public val FRAME_stand46: Int = 207

        public val FRAME_stand47: Int = 208

        public val FRAME_stand48: Int = 209

        public val FRAME_stand49: Int = 210

        public val FRAME_stand50: Int = 211

        public val FRAME_stand51: Int = 212

        public val FRAME_stand52: Int = 213

        public val FRAME_stand53: Int = 214

        public val FRAME_stand54: Int = 215

        public val FRAME_stand55: Int = 216

        public val FRAME_stand56: Int = 217

        public val FRAME_stand57: Int = 218

        public val FRAME_stand58: Int = 219

        public val FRAME_stand59: Int = 220

        public val FRAME_stand60: Int = 221

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_chest_open: Int = 0

        var sound_tentacles_extend: Int = 0

        var sound_tentacles_retract: Int = 0

        var sound_death: Int = 0

        var sound_idle1: Int = 0

        var sound_idle2: Int = 0

        var sound_idle3: Int = 0

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_sight: Int = 0

        var sound_search: Int = 0

        var sound_melee1: Int = 0

        var sound_melee2: Int = 0

        var sound_melee3: Int = 0

        var brain_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "brain_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var brain_search: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_search"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_search, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        //
        //	   STAND
        //

        var brain_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var brain_move_stand = mmove_t(FRAME_stand01, FRAME_stand30, brain_frames_stand, null)

        var brain_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = brain_move_stand
                return true
            }
        }

        //
        //	   IDLE
        //

        var brain_frames_idle = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var brain_move_idle = mmove_t(FRAME_stand31, FRAME_stand60, brain_frames_idle, brain_stand)

        var brain_idle: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_idle"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_AUTO, sound_idle3, 1, Defines.ATTN_IDLE, 0)
                self.monsterinfo.currentmove = brain_move_idle
                return true
            }
        }

        //
        //	   WALK
        //
        var brain_frames_walk1 = array<mframe_t>(mframe_t(GameAI.ai_walk, 7, null), mframe_t(GameAI.ai_walk, 2, null), mframe_t(GameAI.ai_walk, 3, null), mframe_t(GameAI.ai_walk, 3, null), mframe_t(GameAI.ai_walk, 1, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 0, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, -4, null), mframe_t(GameAI.ai_walk, -1, null), mframe_t(GameAI.ai_walk, 2, null))

        var brain_move_walk1 = mmove_t(FRAME_walk101, FRAME_walk111, brain_frames_walk1, null)

        //	   walk2 is FUBAR, do not use
        /*
     * # if 0 void brain_walk2_cycle(edict_t self) { if (random() > 0.1)
     * self.monsterinfo.nextframe= FRAME_walk220; }
     * 
     * static mframe_t brain_frames_walk2[]= new mframe_t[] { new
     * mframe_t(ai_walk, 3, null), new mframe_t(ai_walk, -2, null), new
     * mframe_t(ai_walk, -4, null), new mframe_t(ai_walk, -3, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 1, null), new
     * mframe_t(ai_walk, 12, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, -3, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, -2, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 1, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 10, null, // Cycle
     * Start)
     * 
     * new mframe_t(ai_walk, -1, null), new mframe_t(ai_walk, 7, null), new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 3, null), new
     * mframe_t(ai_walk, -3, null), new mframe_t(ai_walk, 2, null), new
     * mframe_t(ai_walk, 4, null), new mframe_t(ai_walk, -3, null), new
     * mframe_t(ai_walk, 2, null), new mframe_t(ai_walk, 0, null), new
     * mframe_t(ai_walk, 4, brain_walk2_cycle), new mframe_t(ai_walk, -1, null),
     * new mframe_t(ai_walk, -1, null), new mframe_t(ai_walk, -8, null,) new
     * mframe_t(ai_walk, 0, null), new mframe_t(ai_walk, 1, null), new
     * mframe_t(ai_walk, 5, null), new mframe_t(ai_walk, 2, null), new
     * mframe_t(ai_walk, -1, null), new mframe_t(ai_walk, -5, null)}; static
     * mmove_t brain_move_walk2= new mmove_t(FRAME_walk201, FRAME_walk240,
     * brain_frames_walk2, null);
     *  # endif
     */
        var brain_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_walk"
            }

            public fun think(self: edict_t): Boolean {
                //			if (random() <= 0.5)
                self.monsterinfo.currentmove = brain_move_walk1
                //		else
                //			self.monsterinfo.currentmove = &brain_move_walk2;
                return true
            }
        }

        //
        //	   DUCK
        //

        var brain_duck_down: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_duck_down"
            }

            public fun think(self: edict_t): Boolean {

                if ((self.monsterinfo.aiflags and Defines.AI_DUCKED) != 0)
                    return true
                self.monsterinfo.aiflags = self.monsterinfo.aiflags or Defines.AI_DUCKED
                self.maxs[2] -= 32
                self.takedamage = Defines.DAMAGE_YES
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var brain_duck_hold: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_duck_hold"
            }

            public fun think(self: edict_t): Boolean {
                if (GameBase.level.time >= self.monsterinfo.pausetime)
                    self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_HOLD_FRAME.inv()
                else
                    self.monsterinfo.aiflags = self.monsterinfo.aiflags or Defines.AI_HOLD_FRAME
                return true
            }
        }

        var brain_duck_up: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_duck_up"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_DUCKED.inv()
                self.maxs[2] += 32
                self.takedamage = Defines.DAMAGE_AIM
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var brain_dodge: EntDodgeAdapter = object : EntDodgeAdapter() {
            public fun getID(): String {
                return "brain_dodge"
            }

            public fun dodge(self: edict_t, attacker: edict_t, eta: Float) {
                if (Lib.random() > 0.25)
                    return

                if (self.enemy == null)
                    self.enemy = attacker

                self.monsterinfo.pausetime = GameBase.level.time + eta + 0.5.toFloat()
                self.monsterinfo.currentmove = brain_move_duck
                return
            }
        }

        var brain_frames_death2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 9, null), mframe_t(GameAI.ai_move, 0, null))

        var brain_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_dead"
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

        var brain_move_death2 = mmove_t(FRAME_death201, FRAME_death205, brain_frames_death2, brain_dead)

        var brain_frames_death1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 9, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var brain_move_death1 = mmove_t(FRAME_death101, FRAME_death118, brain_frames_death1, brain_dead)

        //
        //	   MELEE
        //

        var brain_swing_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_swing_right"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_melee1, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var brain_hit_right: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_hit_right"
            }

            public fun think(self: edict_t): Boolean {
                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, self.maxs[0], 8)
                if (GameWeapon.fire_hit(self, aim, (15 + (Lib.rand() % 5)), 40))
                    GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_melee3, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var brain_swing_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_swing_left"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_melee2, 1, Defines.ATTN_NORM, 0)

                return true
            }
        }

        var brain_hit_left: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_hit_left"
            }

            public fun think(self: edict_t): Boolean {
                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, self.mins[0], 8)
                if (GameWeapon.fire_hit(self, aim, (15 + (Lib.rand() % 5)), 40))
                    GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_melee3, 1, Defines.ATTN_NORM, 0)

                return true
            }
        }

        var brain_chest_open: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_chest_open"
            }

            public fun think(self: edict_t): Boolean {
                self.spawnflags = self.spawnflags and 65536.inv()
                self.monsterinfo.power_armor_type = Defines.POWER_ARMOR_NONE
                GameBase.gi.sound(self, Defines.CHAN_BODY, sound_chest_open, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var brain_tentacle_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_tentacle_attack"
            }

            public fun think(self: edict_t): Boolean {

                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, 0, 8)
                if (GameWeapon.fire_hit(self, aim, (10 + (Lib.rand() % 5)), -600) && GameBase.skill.value > 0)
                    self.spawnflags = self.spawnflags or 65536
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_tentacles_retract, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var brain_frames_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, 8, null), mframe_t(GameAI.ai_charge, 3, null), mframe_t(GameAI.ai_charge, 5, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -3, brain_swing_right), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -5, null), mframe_t(GameAI.ai_charge, -7, brain_hit_right), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 6, brain_swing_left), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 2, brain_hit_left), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 6, null), mframe_t(GameAI.ai_charge, -1, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 2, null), mframe_t(GameAI.ai_charge, -11, null))

        var brain_chest_closed: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_chest_closed"
            }

            public fun think(self: edict_t): Boolean {

                self.monsterinfo.power_armor_type = Defines.POWER_ARMOR_SCREEN
                if ((self.spawnflags and 65536) != 0) {
                    self.spawnflags = self.spawnflags and 65536.inv()
                    self.monsterinfo.currentmove = brain_move_attack1
                }
                return true
            }
        }

        var brain_frames_attack2 = array<mframe_t>(mframe_t(GameAI.ai_charge, 5, null), mframe_t(GameAI.ai_charge, -4, null), mframe_t(GameAI.ai_charge, -4, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 0, brain_chest_open), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 13, brain_tentacle_attack), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 2, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -9, brain_chest_closed), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 4, null), mframe_t(GameAI.ai_charge, 3, null), mframe_t(GameAI.ai_charge, 2, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, -6, null))

        var brain_melee: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_melee"
            }

            public fun think(self: edict_t): Boolean {
                if (Lib.random() <= 0.5)
                    self.monsterinfo.currentmove = brain_move_attack1
                else
                    self.monsterinfo.currentmove = brain_move_attack2

                return true
            }
        }

        //
        //	   RUN
        //

        var brain_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 9, null), mframe_t(GameAI.ai_run, 2, null), mframe_t(GameAI.ai_run, 3, null), mframe_t(GameAI.ai_run, 3, null), mframe_t(GameAI.ai_run, 1, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 10, null), mframe_t(GameAI.ai_run, -4, null), mframe_t(GameAI.ai_run, -1, null), mframe_t(GameAI.ai_run, 2, null))

        var brain_move_run = mmove_t(FRAME_walk101, FRAME_walk111, brain_frames_run, null)

        var brain_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "brain_run"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.power_armor_type = Defines.POWER_ARMOR_SCREEN
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    self.monsterinfo.currentmove = brain_move_stand
                else
                    self.monsterinfo.currentmove = brain_move_run
                return true
            }
        }

        var brain_frames_defense = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var brain_move_defense = mmove_t(FRAME_defens01, FRAME_defens08, brain_frames_defense, null)

        var brain_frames_pain3 = array<mframe_t>(mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -4, null))

        var brain_move_pain3 = mmove_t(FRAME_pain301, FRAME_pain306, brain_frames_pain3, brain_run)

        var brain_frames_pain2 = array<mframe_t>(mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, -2, null))

        var brain_move_pain2 = mmove_t(FRAME_pain201, FRAME_pain208, brain_frames_pain2, brain_run)

        var brain_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 7, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, -1, null))

        var brain_move_pain1 = mmove_t(FRAME_pain101, FRAME_pain121, brain_frames_pain1, brain_run)

        var brain_frames_duck = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -2, brain_duck_down), mframe_t(GameAI.ai_move, 17, brain_duck_hold), mframe_t(GameAI.ai_move, -3, null), mframe_t(GameAI.ai_move, -1, brain_duck_up), mframe_t(GameAI.ai_move, -5, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, -6, null))

        var brain_move_duck = mmove_t(FRAME_duck01, FRAME_duck08, brain_frames_duck, brain_run)

        var brain_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "brain_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                val r: Float

                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3
                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                r = Lib.random()
                if (r < 0.33) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = brain_move_pain1
                } else if (r < 0.66) {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = brain_move_pain2
                } else {
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                    self.monsterinfo.currentmove = brain_move_pain3
                }
            }

        }

        var brain_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "brain_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

                self.s.effects = 0
                self.monsterinfo.power_armor_type = Defines.POWER_ARMOR_NONE

                //	   check for gib
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

                //	   regular death
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death, 1, Defines.ATTN_NORM, 0)
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES
                if (Lib.random() <= 0.5)
                    self.monsterinfo.currentmove = brain_move_death1
                else
                    self.monsterinfo.currentmove = brain_move_death2
            }
        }

        var brain_move_attack1 = mmove_t(FRAME_attak101, FRAME_attak118, brain_frames_attack1, brain_run)

        var brain_move_attack2 = mmove_t(FRAME_attak201, FRAME_attak217, brain_frames_attack2, brain_run)

        /*
     * QUAKED monster_brain (1 .5 0) (-16 -16 -24) (16 16 32) Ambush
     * Trigger_Spawn Sight
     */
        public fun SP_monster_brain(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_chest_open = GameBase.gi.soundindex("brain/brnatck1.wav")
            sound_tentacles_extend = GameBase.gi.soundindex("brain/brnatck2.wav")
            sound_tentacles_retract = GameBase.gi.soundindex("brain/brnatck3.wav")
            sound_death = GameBase.gi.soundindex("brain/brndeth1.wav")
            sound_idle1 = GameBase.gi.soundindex("brain/brnidle1.wav")
            sound_idle2 = GameBase.gi.soundindex("brain/brnidle2.wav")
            sound_idle3 = GameBase.gi.soundindex("brain/brnlens1.wav")
            sound_pain1 = GameBase.gi.soundindex("brain/brnpain1.wav")
            sound_pain2 = GameBase.gi.soundindex("brain/brnpain2.wav")
            sound_sight = GameBase.gi.soundindex("brain/brnsght1.wav")
            sound_search = GameBase.gi.soundindex("brain/brnsrch1.wav")
            sound_melee1 = GameBase.gi.soundindex("brain/melee1.wav")
            sound_melee2 = GameBase.gi.soundindex("brain/melee2.wav")
            sound_melee3 = GameBase.gi.soundindex("brain/melee3.wav")

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/brain/tris.md2")
            Math3D.VectorSet(self.mins, -16, -16, -24)
            Math3D.VectorSet(self.maxs, 16, 16, 32)

            self.health = 300
            self.gib_health = -150
            self.mass = 400

            self.pain = brain_pain
            self.die = brain_die

            self.monsterinfo.stand = brain_stand
            self.monsterinfo.walk = brain_walk
            self.monsterinfo.run = brain_run
            self.monsterinfo.dodge = brain_dodge
            //		self.monsterinfo.attack = brain_attack;
            self.monsterinfo.melee = brain_melee
            self.monsterinfo.sight = brain_sight
            self.monsterinfo.search = brain_search
            self.monsterinfo.idle = brain_idle

            self.monsterinfo.power_armor_type = Defines.POWER_ARMOR_SCREEN
            self.monsterinfo.power_armor_power = 100

            GameBase.gi.linkentity(self)

            self.monsterinfo.currentmove = brain_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.walkmonster_start.think(self)
        }
    }
}