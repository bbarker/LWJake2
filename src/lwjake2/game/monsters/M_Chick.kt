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
import lwjake2.game.Monster
import lwjake2.game.edict_t
import lwjake2.game.mframe_t
import lwjake2.game.mmove_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

public class M_Chick {
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

        public val FRAME_attak119: Int = 18

        public val FRAME_attak120: Int = 19

        public val FRAME_attak121: Int = 20

        public val FRAME_attak122: Int = 21

        public val FRAME_attak123: Int = 22

        public val FRAME_attak124: Int = 23

        public val FRAME_attak125: Int = 24

        public val FRAME_attak126: Int = 25

        public val FRAME_attak127: Int = 26

        public val FRAME_attak128: Int = 27

        public val FRAME_attak129: Int = 28

        public val FRAME_attak130: Int = 29

        public val FRAME_attak131: Int = 30

        public val FRAME_attak132: Int = 31

        public val FRAME_attak201: Int = 32

        public val FRAME_attak202: Int = 33

        public val FRAME_attak203: Int = 34

        public val FRAME_attak204: Int = 35

        public val FRAME_attak205: Int = 36

        public val FRAME_attak206: Int = 37

        public val FRAME_attak207: Int = 38

        public val FRAME_attak208: Int = 39

        public val FRAME_attak209: Int = 40

        public val FRAME_attak210: Int = 41

        public val FRAME_attak211: Int = 42

        public val FRAME_attak212: Int = 43

        public val FRAME_attak213: Int = 44

        public val FRAME_attak214: Int = 45

        public val FRAME_attak215: Int = 46

        public val FRAME_attak216: Int = 47

        public val FRAME_death101: Int = 48

        public val FRAME_death102: Int = 49

        public val FRAME_death103: Int = 50

        public val FRAME_death104: Int = 51

        public val FRAME_death105: Int = 52

        public val FRAME_death106: Int = 53

        public val FRAME_death107: Int = 54

        public val FRAME_death108: Int = 55

        public val FRAME_death109: Int = 56

        public val FRAME_death110: Int = 57

        public val FRAME_death111: Int = 58

        public val FRAME_death112: Int = 59

        public val FRAME_death201: Int = 60

        public val FRAME_death202: Int = 61

        public val FRAME_death203: Int = 62

        public val FRAME_death204: Int = 63

        public val FRAME_death205: Int = 64

        public val FRAME_death206: Int = 65

        public val FRAME_death207: Int = 66

        public val FRAME_death208: Int = 67

        public val FRAME_death209: Int = 68

        public val FRAME_death210: Int = 69

        public val FRAME_death211: Int = 70

        public val FRAME_death212: Int = 71

        public val FRAME_death213: Int = 72

        public val FRAME_death214: Int = 73

        public val FRAME_death215: Int = 74

        public val FRAME_death216: Int = 75

        public val FRAME_death217: Int = 76

        public val FRAME_death218: Int = 77

        public val FRAME_death219: Int = 78

        public val FRAME_death220: Int = 79

        public val FRAME_death221: Int = 80

        public val FRAME_death222: Int = 81

        public val FRAME_death223: Int = 82

        public val FRAME_duck01: Int = 83

        public val FRAME_duck02: Int = 84

        public val FRAME_duck03: Int = 85

        public val FRAME_duck04: Int = 86

        public val FRAME_duck05: Int = 87

        public val FRAME_duck06: Int = 88

        public val FRAME_duck07: Int = 89

        public val FRAME_pain101: Int = 90

        public val FRAME_pain102: Int = 91

        public val FRAME_pain103: Int = 92

        public val FRAME_pain104: Int = 93

        public val FRAME_pain105: Int = 94

        public val FRAME_pain201: Int = 95

        public val FRAME_pain202: Int = 96

        public val FRAME_pain203: Int = 97

        public val FRAME_pain204: Int = 98

        public val FRAME_pain205: Int = 99

        public val FRAME_pain301: Int = 100

        public val FRAME_pain302: Int = 101

        public val FRAME_pain303: Int = 102

        public val FRAME_pain304: Int = 103

        public val FRAME_pain305: Int = 104

        public val FRAME_pain306: Int = 105

        public val FRAME_pain307: Int = 106

        public val FRAME_pain308: Int = 107

        public val FRAME_pain309: Int = 108

        public val FRAME_pain310: Int = 109

        public val FRAME_pain311: Int = 110

        public val FRAME_pain312: Int = 111

        public val FRAME_pain313: Int = 112

        public val FRAME_pain314: Int = 113

        public val FRAME_pain315: Int = 114

        public val FRAME_pain316: Int = 115

        public val FRAME_pain317: Int = 116

        public val FRAME_pain318: Int = 117

        public val FRAME_pain319: Int = 118

        public val FRAME_pain320: Int = 119

        public val FRAME_pain321: Int = 120

        public val FRAME_stand101: Int = 121

        public val FRAME_stand102: Int = 122

        public val FRAME_stand103: Int = 123

        public val FRAME_stand104: Int = 124

        public val FRAME_stand105: Int = 125

        public val FRAME_stand106: Int = 126

        public val FRAME_stand107: Int = 127

        public val FRAME_stand108: Int = 128

        public val FRAME_stand109: Int = 129

        public val FRAME_stand110: Int = 130

        public val FRAME_stand111: Int = 131

        public val FRAME_stand112: Int = 132

        public val FRAME_stand113: Int = 133

        public val FRAME_stand114: Int = 134

        public val FRAME_stand115: Int = 135

        public val FRAME_stand116: Int = 136

        public val FRAME_stand117: Int = 137

        public val FRAME_stand118: Int = 138

        public val FRAME_stand119: Int = 139

        public val FRAME_stand120: Int = 140

        public val FRAME_stand121: Int = 141

        public val FRAME_stand122: Int = 142

        public val FRAME_stand123: Int = 143

        public val FRAME_stand124: Int = 144

        public val FRAME_stand125: Int = 145

        public val FRAME_stand126: Int = 146

        public val FRAME_stand127: Int = 147

        public val FRAME_stand128: Int = 148

        public val FRAME_stand129: Int = 149

        public val FRAME_stand130: Int = 150

        public val FRAME_stand201: Int = 151

        public val FRAME_stand202: Int = 152

        public val FRAME_stand203: Int = 153

        public val FRAME_stand204: Int = 154

        public val FRAME_stand205: Int = 155

        public val FRAME_stand206: Int = 156

        public val FRAME_stand207: Int = 157

        public val FRAME_stand208: Int = 158

        public val FRAME_stand209: Int = 159

        public val FRAME_stand210: Int = 160

        public val FRAME_stand211: Int = 161

        public val FRAME_stand212: Int = 162

        public val FRAME_stand213: Int = 163

        public val FRAME_stand214: Int = 164

        public val FRAME_stand215: Int = 165

        public val FRAME_stand216: Int = 166

        public val FRAME_stand217: Int = 167

        public val FRAME_stand218: Int = 168

        public val FRAME_stand219: Int = 169

        public val FRAME_stand220: Int = 170

        public val FRAME_stand221: Int = 171

        public val FRAME_stand222: Int = 172

        public val FRAME_stand223: Int = 173

        public val FRAME_stand224: Int = 174

        public val FRAME_stand225: Int = 175

        public val FRAME_stand226: Int = 176

        public val FRAME_stand227: Int = 177

        public val FRAME_stand228: Int = 178

        public val FRAME_stand229: Int = 179

        public val FRAME_stand230: Int = 180

        public val FRAME_walk01: Int = 181

        public val FRAME_walk02: Int = 182

        public val FRAME_walk03: Int = 183

        public val FRAME_walk04: Int = 184

        public val FRAME_walk05: Int = 185

        public val FRAME_walk06: Int = 186

        public val FRAME_walk07: Int = 187

        public val FRAME_walk08: Int = 188

        public val FRAME_walk09: Int = 189

        public val FRAME_walk10: Int = 190

        public val FRAME_walk11: Int = 191

        public val FRAME_walk12: Int = 192

        public val FRAME_walk13: Int = 193

        public val FRAME_walk14: Int = 194

        public val FRAME_walk15: Int = 195

        public val FRAME_walk16: Int = 196

        public val FRAME_walk17: Int = 197

        public val FRAME_walk18: Int = 198

        public val FRAME_walk19: Int = 199

        public val FRAME_walk20: Int = 200

        public val FRAME_walk21: Int = 201

        public val FRAME_walk22: Int = 202

        public val FRAME_walk23: Int = 203

        public val FRAME_walk24: Int = 204

        public val FRAME_walk25: Int = 205

        public val FRAME_walk26: Int = 206

        public val FRAME_walk27: Int = 207

        public val FRAME_recln201: Int = 208

        public val FRAME_recln202: Int = 209

        public val FRAME_recln203: Int = 210

        public val FRAME_recln204: Int = 211

        public val FRAME_recln205: Int = 212

        public val FRAME_recln206: Int = 213

        public val FRAME_recln207: Int = 214

        public val FRAME_recln208: Int = 215

        public val FRAME_recln209: Int = 216

        public val FRAME_recln210: Int = 217

        public val FRAME_recln211: Int = 218

        public val FRAME_recln212: Int = 219

        public val FRAME_recln213: Int = 220

        public val FRAME_recln214: Int = 221

        public val FRAME_recln215: Int = 222

        public val FRAME_recln216: Int = 223

        public val FRAME_recln217: Int = 224

        public val FRAME_recln218: Int = 225

        public val FRAME_recln219: Int = 226

        public val FRAME_recln220: Int = 227

        public val FRAME_recln221: Int = 228

        public val FRAME_recln222: Int = 229

        public val FRAME_recln223: Int = 230

        public val FRAME_recln224: Int = 231

        public val FRAME_recln225: Int = 232

        public val FRAME_recln226: Int = 233

        public val FRAME_recln227: Int = 234

        public val FRAME_recln228: Int = 235

        public val FRAME_recln229: Int = 236

        public val FRAME_recln230: Int = 237

        public val FRAME_recln231: Int = 238

        public val FRAME_recln232: Int = 239

        public val FRAME_recln233: Int = 240

        public val FRAME_recln234: Int = 241

        public val FRAME_recln235: Int = 242

        public val FRAME_recln236: Int = 243

        public val FRAME_recln237: Int = 244

        public val FRAME_recln238: Int = 245

        public val FRAME_recln239: Int = 246

        public val FRAME_recln240: Int = 247

        public val FRAME_recln101: Int = 248

        public val FRAME_recln102: Int = 249

        public val FRAME_recln103: Int = 250

        public val FRAME_recln104: Int = 251

        public val FRAME_recln105: Int = 252

        public val FRAME_recln106: Int = 253

        public val FRAME_recln107: Int = 254

        public val FRAME_recln108: Int = 255

        public val FRAME_recln109: Int = 256

        public val FRAME_recln110: Int = 257

        public val FRAME_recln111: Int = 258

        public val FRAME_recln112: Int = 259

        public val FRAME_recln113: Int = 260

        public val FRAME_recln114: Int = 261

        public val FRAME_recln115: Int = 262

        public val FRAME_recln116: Int = 263

        public val FRAME_recln117: Int = 264

        public val FRAME_recln118: Int = 265

        public val FRAME_recln119: Int = 266

        public val FRAME_recln120: Int = 267

        public val FRAME_recln121: Int = 268

        public val FRAME_recln122: Int = 269

        public val FRAME_recln123: Int = 270

        public val FRAME_recln124: Int = 271

        public val FRAME_recln125: Int = 272

        public val FRAME_recln126: Int = 273

        public val FRAME_recln127: Int = 274

        public val FRAME_recln128: Int = 275

        public val FRAME_recln129: Int = 276

        public val FRAME_recln130: Int = 277

        public val FRAME_recln131: Int = 278

        public val FRAME_recln132: Int = 279

        public val FRAME_recln133: Int = 280

        public val FRAME_recln134: Int = 281

        public val FRAME_recln135: Int = 282

        public val FRAME_recln136: Int = 283

        public val FRAME_recln137: Int = 284

        public val FRAME_recln138: Int = 285

        public val FRAME_recln139: Int = 286

        public val FRAME_recln140: Int = 287

        public val MODEL_SCALE: Float = 1.000000.toFloat()

        var sound_missile_prelaunch: Int = 0

        var sound_missile_launch: Int = 0

        var sound_melee_swing: Int = 0

        var sound_melee_hit: Int = 0

        var sound_missile_reload: Int = 0

        var sound_death1: Int = 0

        var sound_death2: Int = 0

        var sound_fall_down: Int = 0

        var sound_idle1: Int = 0

        var sound_idle2: Int = 0

        var sound_pain1: Int = 0

        var sound_pain2: Int = 0

        var sound_pain3: Int = 0

        var sound_sight: Int = 0

        var sound_search: Int = 0

        var ChickMoan: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "ChickMoan"
            }

            public fun think(self: edict_t): Boolean {
                if (Lib.random() < 0.5)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_idle1, 1, Defines.ATTN_IDLE, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_idle2, 1, Defines.ATTN_IDLE, 0)
                return true
            }
        }

        var chick_frames_fidget = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, ChickMoan), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null))

        var chick_stand: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_stand"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_stand
                return true
            }
        }

        var chick_move_fidget = mmove_t(FRAME_stand201, FRAME_stand230, chick_frames_fidget, chick_stand)

        var chick_fidget: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_fidget"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0)
                    return true
                if (Lib.random() <= 0.3)
                    self.monsterinfo.currentmove = chick_move_fidget
                return true
            }
        }

        var chick_frames_stand = array<mframe_t>(mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, null), mframe_t(GameAI.ai_stand, 0, chick_fidget))

        var chick_move_stand = mmove_t(FRAME_stand101, FRAME_stand130, chick_frames_stand, null)

        var chick_run: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_run"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_STAND_GROUND) != 0) {
                    self.monsterinfo.currentmove = chick_move_stand
                    return true
                }

                if (self.monsterinfo.currentmove == chick_move_walk || self.monsterinfo.currentmove == chick_move_start_run) {
                    self.monsterinfo.currentmove = chick_move_run
                } else {
                    self.monsterinfo.currentmove = chick_move_start_run
                }
                return true
            }
        }

        var chick_frames_start_run = array<mframe_t>(mframe_t(GameAI.ai_run, 1, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, -1, null), mframe_t(GameAI.ai_run, -1, null), mframe_t(GameAI.ai_run, 0, null), mframe_t(GameAI.ai_run, 1, null), mframe_t(GameAI.ai_run, 3, null), mframe_t(GameAI.ai_run, 6, null), mframe_t(GameAI.ai_run, 3, null))

        var chick_move_start_run = mmove_t(FRAME_walk01, FRAME_walk10, chick_frames_start_run, chick_run)

        var chick_frames_run = array<mframe_t>(mframe_t(GameAI.ai_run, 6, null), mframe_t(GameAI.ai_run, 8, null), mframe_t(GameAI.ai_run, 13, null), mframe_t(GameAI.ai_run, 5, null), mframe_t(GameAI.ai_run, 7, null), mframe_t(GameAI.ai_run, 4, null), mframe_t(GameAI.ai_run, 11, null), mframe_t(GameAI.ai_run, 5, null), mframe_t(GameAI.ai_run, 9, null), mframe_t(GameAI.ai_run, 7, null))

        var chick_move_run = mmove_t(FRAME_walk11, FRAME_walk20, chick_frames_run, null)

        var chick_frames_walk = array<mframe_t>(mframe_t(GameAI.ai_walk, 6, null), mframe_t(GameAI.ai_walk, 8, null), mframe_t(GameAI.ai_walk, 13, null), mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 7, null), mframe_t(GameAI.ai_walk, 4, null), mframe_t(GameAI.ai_walk, 11, null), mframe_t(GameAI.ai_walk, 5, null), mframe_t(GameAI.ai_walk, 9, null), mframe_t(GameAI.ai_walk, 7, null))

        var chick_move_walk = mmove_t(FRAME_walk11, FRAME_walk20, chick_frames_walk, null)

        var chick_walk: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_walk"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_walk
                return true
            }
        }

        var chick_frames_pain1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var chick_move_pain1 = mmove_t(FRAME_pain101, FRAME_pain105, chick_frames_pain1, chick_run)

        var chick_frames_pain2 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var chick_move_pain2 = mmove_t(FRAME_pain201, FRAME_pain205, chick_frames_pain2, chick_run)

        var chick_frames_pain3 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 11, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 4, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -3, null), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, 5, null), mframe_t(GameAI.ai_move, 7, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, -5, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, -8, null), mframe_t(GameAI.ai_move, 2, null))

        var chick_move_pain3 = mmove_t(FRAME_pain301, FRAME_pain321, chick_frames_pain3, chick_run)

        var chick_pain: EntPainAdapter = object : EntPainAdapter() {
            public fun getID(): String {
                return "chick_pain"
            }

            public fun pain(self: edict_t, other: edict_t, kick: Float, damage: Int) {
                val r: Float

                if (self.health < (self.max_health / 2))
                    self.s.skinnum = 1

                if (GameBase.level.time < self.pain_debounce_time)
                    return

                self.pain_debounce_time = GameBase.level.time + 3

                r = Lib.random()
                if (r < 0.33)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain1, 1, Defines.ATTN_NORM, 0)
                else if (r < 0.66)
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain2, 1, Defines.ATTN_NORM, 0)
                else
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_pain3, 1, Defines.ATTN_NORM, 0)

                if (GameBase.skill.value == 3)
                    return  // no pain anims in nightmare

                if (damage <= 10)
                    self.monsterinfo.currentmove = chick_move_pain1
                else if (damage <= 25)
                    self.monsterinfo.currentmove = chick_move_pain2
                else
                    self.monsterinfo.currentmove = chick_move_pain3
                return
            }
        }

        var chick_dead: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_dead"
            }

            public fun think(self: edict_t): Boolean {
                Math3D.VectorSet(self.mins, -16, -16, 0)
                Math3D.VectorSet(self.maxs, 16, 16, 16)
                self.movetype = Defines.MOVETYPE_TOSS
                self.svflags = self.svflags or Defines.SVF_DEADMONSTER
                self.nextthink = 0
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var chick_frames_death2 = array<mframe_t>(mframe_t(GameAI.ai_move, -6, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, -5, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -1, null), mframe_t(GameAI.ai_move, -2, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 10, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 2, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, -3, null), mframe_t(GameAI.ai_move, -5, null), mframe_t(GameAI.ai_move, 4, null), mframe_t(GameAI.ai_move, 15, null), mframe_t(GameAI.ai_move, 14, null), mframe_t(GameAI.ai_move, 1, null))

        var chick_move_death2 = mmove_t(FRAME_death201, FRAME_death223, chick_frames_death2, chick_dead)

        var chick_frames_death1 = array<mframe_t>(mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, -7, null), mframe_t(GameAI.ai_move, 4, null), mframe_t(GameAI.ai_move, 11, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null), mframe_t(GameAI.ai_move, 0, null))

        var chick_move_death1 = mmove_t(FRAME_death101, FRAME_death112, chick_frames_death1, chick_dead)

        var chick_die: EntDieAdapter = object : EntDieAdapter() {
            public fun getID(): String {
                return "chick_die"
            }

            public fun die(self: edict_t, inflictor: edict_t, attacker: edict_t, damage: Int, point: FloatArray) {
                var n: Int

                //		   check for gib
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

                //		   regular death
                self.deadflag = Defines.DEAD_DEAD
                self.takedamage = Defines.DAMAGE_YES

                n = Lib.rand() % 2
                if (n == 0) {
                    self.monsterinfo.currentmove = chick_move_death1
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death1, 1, Defines.ATTN_NORM, 0)
                } else {
                    self.monsterinfo.currentmove = chick_move_death2
                    GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_death2, 1, Defines.ATTN_NORM, 0)
                }
            }

        }

        var chick_duck_down: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_duck_down"
            }

            public fun think(self: edict_t): Boolean {
                if ((self.monsterinfo.aiflags and Defines.AI_DUCKED) != 0)
                    return true
                self.monsterinfo.aiflags = self.monsterinfo.aiflags or Defines.AI_DUCKED
                self.maxs[2] -= 32
                self.takedamage = Defines.DAMAGE_YES
                self.monsterinfo.pausetime = GameBase.level.time + 1
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var chick_duck_hold: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_duck_hold"
            }

            public fun think(self: edict_t): Boolean {
                if (GameBase.level.time >= self.monsterinfo.pausetime)
                    self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_HOLD_FRAME.inv()
                else
                    self.monsterinfo.aiflags = self.monsterinfo.aiflags or Defines.AI_HOLD_FRAME
                return true
            }
        }

        var chick_duck_up: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_duck_up"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.aiflags = self.monsterinfo.aiflags and Defines.AI_DUCKED.inv()
                self.maxs[2] += 32
                self.takedamage = Defines.DAMAGE_AIM
                GameBase.gi.linkentity(self)
                return true
            }
        }

        var chick_frames_duck = array<mframe_t>(mframe_t(GameAI.ai_move, 0, chick_duck_down), mframe_t(GameAI.ai_move, 1, null), mframe_t(GameAI.ai_move, 4, chick_duck_hold), mframe_t(GameAI.ai_move, -4, null), mframe_t(GameAI.ai_move, -5, chick_duck_up), mframe_t(GameAI.ai_move, 3, null), mframe_t(GameAI.ai_move, 1, null))

        var chick_move_duck = mmove_t(FRAME_duck01, FRAME_duck07, chick_frames_duck, chick_run)

        var chick_dodge: EntDodgeAdapter = object : EntDodgeAdapter() {
            public fun getID(): String {
                return "chick_dodge"
            }

            public fun dodge(self: edict_t, attacker: edict_t, eta: Float) {
                if (Lib.random() > 0.25)
                    return

                if (self.enemy != null)
                    self.enemy = attacker

                self.monsterinfo.currentmove = chick_move_duck
                return
            }
        }

        var ChickSlash: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "ChickSlash"
            }

            public fun think(self: edict_t): Boolean {
                val aim = floatArray(0.0, 0.0, 0.0)

                Math3D.VectorSet(aim, Defines.MELEE_DISTANCE, self.mins[0], 10)
                GameBase.gi.sound(self, Defines.CHAN_WEAPON, sound_melee_swing, 1, Defines.ATTN_NORM, 0)
                GameWeapon.fire_hit(self, aim, (10 + (Lib.rand() % 6)), 100)
                return true
            }
        }

        var ChickRocket: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "ChickRocket"
            }

            public fun think(self: edict_t): Boolean {
                val forward = floatArray(0.0, 0.0, 0.0)
                val right = floatArray(0.0, 0.0, 0.0)
                val start = floatArray(0.0, 0.0, 0.0)
                val dir = floatArray(0.0, 0.0, 0.0)
                val vec = floatArray(0.0, 0.0, 0.0)

                Math3D.AngleVectors(self.s.angles, forward, right, null)
                Math3D.G_ProjectSource(self.s.origin, M_Flash.monster_flash_offset[Defines.MZ2_CHICK_ROCKET_1], forward, right, start)

                Math3D.VectorCopy(self.enemy.s.origin, vec)
                vec[2] += self.enemy.viewheight
                Math3D.VectorSubtract(vec, start, dir)
                Math3D.VectorNormalize(dir)

                Monster.monster_fire_rocket(self, start, dir, 50, 500, Defines.MZ2_CHICK_ROCKET_1)
                return true
            }
        }

        var Chick_PreAttack1: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "Chick_PreAttack1"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_missile_prelaunch, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var ChickReload: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "ChickReload"
            }

            public fun think(self: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_missile_reload, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        var chick_attack1: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_attack1"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_attack1
                return true
            }
        }

        var chick_rerocket: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_rerocket"
            }

            public fun think(self: edict_t): Boolean {
                if (self.enemy.health > 0) {
                    if (GameUtil.range(self, self.enemy) > Defines.RANGE_MELEE)
                        if (GameUtil.visible(self, self.enemy))
                            if (Lib.random() <= 0.6) {
                                self.monsterinfo.currentmove = chick_move_attack1
                                return true
                            }
                }
                self.monsterinfo.currentmove = chick_move_end_attack1
                return true
            }
        }

        var chick_frames_start_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, 0, Chick_PreAttack1), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 4, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 3, null), mframe_t(GameAI.ai_charge, 5, null), mframe_t(GameAI.ai_charge, 7, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 0, chick_attack1))

        var chick_move_start_attack1 = mmove_t(FRAME_attak101, FRAME_attak113, chick_frames_start_attack1, null)

        var chick_frames_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, 19, ChickRocket), mframe_t(GameAI.ai_charge, -6, null), mframe_t(GameAI.ai_charge, -5, null), mframe_t(GameAI.ai_charge, -2, null), mframe_t(GameAI.ai_charge, -7, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 10, ChickReload), mframe_t(GameAI.ai_charge, 4, null), mframe_t(GameAI.ai_charge, 5, null), mframe_t(GameAI.ai_charge, 6, null), mframe_t(GameAI.ai_charge, 6, null), mframe_t(GameAI.ai_charge, 4, null), mframe_t(GameAI.ai_charge, 3, chick_rerocket))

        var chick_move_attack1 = mmove_t(FRAME_attak114, FRAME_attak127, chick_frames_attack1, null)

        var chick_frames_end_attack1 = array<mframe_t>(mframe_t(GameAI.ai_charge, -3, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, -6, null), mframe_t(GameAI.ai_charge, -4, null), mframe_t(GameAI.ai_charge, -2, null))

        var chick_move_end_attack1 = mmove_t(FRAME_attak128, FRAME_attak132, chick_frames_end_attack1, chick_run)

        var chick_reslash: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_reslash"
            }

            public fun think(self: edict_t): Boolean {
                if (self.enemy.health > 0) {
                    if (GameUtil.range(self, self.enemy) == Defines.RANGE_MELEE)
                        if (Lib.random() <= 0.9) {
                            self.monsterinfo.currentmove = chick_move_slash
                            return true
                        } else {
                            self.monsterinfo.currentmove = chick_move_end_slash
                            return true
                        }
                }
                self.monsterinfo.currentmove = chick_move_end_slash
                return true
            }
        }

        var chick_frames_slash = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 7, ChickSlash), mframe_t(GameAI.ai_charge, -7, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, -1, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 0, null), mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, -2, chick_reslash))

        var chick_move_slash = mmove_t(FRAME_attak204, FRAME_attak212, chick_frames_slash, null)

        var chick_frames_end_slash = array<mframe_t>(mframe_t(GameAI.ai_charge, -6, null), mframe_t(GameAI.ai_charge, -1, null), mframe_t(GameAI.ai_charge, -6, null), mframe_t(GameAI.ai_charge, 0, null))

        var chick_move_end_slash = mmove_t(FRAME_attak213, FRAME_attak216, chick_frames_end_slash, chick_run)

        var chick_slash: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_slash"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_slash
                return true
            }
        }

        var chick_frames_start_slash = array<mframe_t>(mframe_t(GameAI.ai_charge, 1, null), mframe_t(GameAI.ai_charge, 8, null), mframe_t(GameAI.ai_charge, 3, null))

        var chick_move_start_slash = mmove_t(FRAME_attak201, FRAME_attak203, chick_frames_start_slash, chick_slash)

        var chick_melee: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_melee"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_start_slash
                return true
            }
        }

        var chick_attack: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "chick_attack"
            }

            public fun think(self: edict_t): Boolean {
                self.monsterinfo.currentmove = chick_move_start_attack1
                return true
            }
        }

        var chick_sight: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "chick_sight"
            }

            public fun interact(self: edict_t, other: edict_t): Boolean {
                GameBase.gi.sound(self, Defines.CHAN_VOICE, sound_sight, 1, Defines.ATTN_NORM, 0)
                return true
            }
        }

        /*
     * QUAKED monster_chick (1 .5 0) (-16 -16 -24) (16 16 32) Ambush
     * Trigger_Spawn Sight
     */
        public fun SP_monster_chick(self: edict_t) {
            if (GameBase.deathmatch.value != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            sound_missile_prelaunch = GameBase.gi.soundindex("chick/chkatck1.wav")
            sound_missile_launch = GameBase.gi.soundindex("chick/chkatck2.wav")
            sound_melee_swing = GameBase.gi.soundindex("chick/chkatck3.wav")
            sound_melee_hit = GameBase.gi.soundindex("chick/chkatck4.wav")
            sound_missile_reload = GameBase.gi.soundindex("chick/chkatck5.wav")
            sound_death1 = GameBase.gi.soundindex("chick/chkdeth1.wav")
            sound_death2 = GameBase.gi.soundindex("chick/chkdeth2.wav")
            sound_fall_down = GameBase.gi.soundindex("chick/chkfall1.wav")
            sound_idle1 = GameBase.gi.soundindex("chick/chkidle1.wav")
            sound_idle2 = GameBase.gi.soundindex("chick/chkidle2.wav")
            sound_pain1 = GameBase.gi.soundindex("chick/chkpain1.wav")
            sound_pain2 = GameBase.gi.soundindex("chick/chkpain2.wav")
            sound_pain3 = GameBase.gi.soundindex("chick/chkpain3.wav")
            sound_sight = GameBase.gi.soundindex("chick/chksght1.wav")
            sound_search = GameBase.gi.soundindex("chick/chksrch1.wav")

            self.movetype = Defines.MOVETYPE_STEP
            self.solid = Defines.SOLID_BBOX
            self.s.modelindex = GameBase.gi.modelindex("models/monsters/bitch/tris.md2")
            Math3D.VectorSet(self.mins, -16, -16, 0)
            Math3D.VectorSet(self.maxs, 16, 16, 56)

            self.health = 175
            self.gib_health = -70
            self.mass = 200

            self.pain = chick_pain
            self.die = chick_die

            self.monsterinfo.stand = chick_stand
            self.monsterinfo.walk = chick_walk
            self.monsterinfo.run = chick_run
            self.monsterinfo.dodge = chick_dodge
            self.monsterinfo.attack = chick_attack
            self.monsterinfo.melee = chick_melee
            self.monsterinfo.sight = chick_sight

            GameBase.gi.linkentity(self)

            self.monsterinfo.currentmove = chick_move_stand
            self.monsterinfo.scale = MODEL_SCALE

            GameAI.walkmonster_start.think(self)
        }
    }
}