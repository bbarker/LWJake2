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

/** Contains the definitions for the game engine.  */

package lwjake2

import java.nio.ByteOrder

public class Defines {
    companion object {

        public val WEAPON_READY: Int = 0
        public val WEAPON_ACTIVATING: Int = 1
        public val WEAPON_DROPPING: Int = 2
        public val WEAPON_FIRING: Int = 3

        public val GRENADE_TIMER: Float = 3.0.toFloat()
        public val GRENADE_MINSPEED: Int = 400
        public val GRENADE_MAXSPEED: Int = 800

        // -----------------
        // client/q_shared.h

        // can accelerate and turn
        public val PM_NORMAL: Int = 0
        public val PM_SPECTATOR: Int = 1
        // no acceleration or turning
        public val PM_DEAD: Int = 2
        public val PM_GIB: Int = 3 // different bounding box
        public val PM_FREEZE: Int = 4

        public val EV_NONE: Int = 0
        public val EV_ITEM_RESPAWN: Int = 1
        public val EV_FOOTSTEP: Int = 2
        public val EV_FALLSHORT: Int = 3
        public val EV_FALL: Int = 4
        public val EV_FALLFAR: Int = 5
        public val EV_PLAYER_TELEPORT: Int = 6
        public val EV_OTHER_TELEPORT: Int = 7

        //	angle indexes
        public val PITCH: Int = 0 // up / down
        public val YAW: Int = 1 // left / right
        public val ROLL: Int = 2 // fall over

        public val MAX_STRING_CHARS: Int = 1024 // max length of a string passed to Cmd_TokenizeString
        public val MAX_STRING_TOKENS: Int = 80 // max tokens resulting from Cmd_TokenizeString
        public val MAX_TOKEN_CHARS: Int = 1024 // max length of an individual token

        public val MAX_QPATH: Int = 64 // max length of a quake game pathname
        public val MAX_OSPATH: Int = 128 // max length of a filesystem pathname

        //	per-level limits
        public val MAX_CLIENTS: Int = 256 // absolute limit
        public val MAX_EDICTS: Int = 1024 // must change protocol to increase more
        public val MAX_LIGHTSTYLES: Int = 256
        public val MAX_MODELS: Int = 256 // these are sent over the net as bytes
        public val MAX_SOUNDS: Int = 256 // so they cannot be blindly increased
        public val MAX_IMAGES: Int = 256
        public val MAX_ITEMS: Int = 256
        public val MAX_GENERAL: Int = (MAX_CLIENTS * 2) // general config strings

        //	game print flags
        public val PRINT_LOW: Int = 0 // pickup messages
        public val PRINT_MEDIUM: Int = 1 // death messages
        public val PRINT_HIGH: Int = 2 // critical messages
        public val PRINT_CHAT: Int = 3 // chat messages

        public val ERR_FATAL: Int = 0 // exit the entire game with a popup window
        public val ERR_DROP: Int = 1 // print to console and disconnect from game
        public val ERR_DISCONNECT: Int = 2 // don't kill server

        public val PRINT_ALL: Int = 0
        public val PRINT_DEVELOPER: Int = 1 // only print when "developer 1"
        public val PRINT_ALERT: Int = 2

        //	key / value info strings
        public val MAX_INFO_KEY: Int = 64
        public val MAX_INFO_VALUE: Int = 64
        public val MAX_INFO_STRING: Int = 512

        // directory searching
        public val SFF_ARCH: Int = 1
        public val SFF_HIDDEN: Int = 2
        public val SFF_RDONLY: Int = 4
        public val SFF_SUBDIR: Int = 8
        public val SFF_SYSTEM: Int = 16

        public val CVAR_ARCHIVE: Int = 1 // set to cause it to be saved to vars.rc
        public val CVAR_USERINFO: Int = 2 // added to userinfo when changed
        public val CVAR_SERVERINFO: Int = 4 // added to serverinfo when changed
        public val CVAR_NOSET: Int = 8 // don't allow change from console at all,
        // but can be set from the command line
        public val CVAR_LATCH: Int = 16 // save changes until server restart

        // lower bits are stronger, and will eat weaker brushes completely
        public val CONTENTS_SOLID: Int = 1 // an eye is never valid in a solid
        public val CONTENTS_WINDOW: Int = 2 // translucent, but not watery
        public val CONTENTS_AUX: Int = 4
        public val CONTENTS_LAVA: Int = 8
        public val CONTENTS_SLIME: Int = 16
        public val CONTENTS_WATER: Int = 32
        public val CONTENTS_MIST: Int = 64
        public val LAST_VISIBLE_CONTENTS: Int = 64

        // remaining contents are non-visible, and don't eat brushes
        public val CONTENTS_AREAPORTAL: Int = 32768

        public val CONTENTS_PLAYERCLIP: Int = 65536
        public val CONTENTS_MONSTERCLIP: Int = 131072

        // currents can be added to any other contents, and may be mixed
        public val CONTENTS_CURRENT_0: Int = 262144
        public val CONTENTS_CURRENT_90: Int = 524288
        public val CONTENTS_CURRENT_180: Int = 1048576
        public val CONTENTS_CURRENT_270: Int = 2097152
        public val CONTENTS_CURRENT_UP: Int = 4194304
        public val CONTENTS_CURRENT_DOWN: Int = 8388608

        public val CONTENTS_ORIGIN: Int = 16777216 // removed before bsping an entity

        public val CONTENTS_MONSTER: Int = 33554432 // should never be on a brush, only in game
        public val CONTENTS_DEADMONSTER: Int = 67108864
        public val CONTENTS_DETAIL: Int = 134217728 // brushes to be added after vis leafs
        public val CONTENTS_TRANSLUCENT: Int = 268435456 // auto set if any surface has trans
        public val CONTENTS_LADDER: Int = 536870912

        public val SURF_LIGHT: Int = 1 // value will hold the light strength
        public val SURF_SLICK: Int = 2 // effects game physics

        public val SURF_SKY: Int = 4 // don't draw, but add to skybox
        public val SURF_WARP: Int = 8 // turbulent water warp
        public val SURF_TRANS33: Int = 16
        public val SURF_TRANS66: Int = 32
        public val SURF_FLOWING: Int = 64 // scroll towards angle
        public val SURF_NODRAW: Int = 128 // don't bother referencing the texture

        //
        // button bits
        //
        public val BUTTON_ATTACK: Int = 1
        public val BUTTON_USE: Int = 2
        public val BUTTON_ANY: Int = 128 // any key whatsoever

        public val MAXTOUCH: Int = 32

        // entity_state_t->effects
        // Effects are things handled on the client side (lights, particles, frame animations)
        // that happen constantly on the given entity.
        // An entity that has effects will be sent to the client
        // even if it has a zero index model.
        public val EF_ROTATE: Int = 1 // rotate (bonus items)
        public val EF_GIB: Int = 2 // leave a trail
        public val EF_BLASTER: Int = 8 // redlight + trail
        public val EF_ROCKET: Int = 16 // redlight + trail
        public val EF_GRENADE: Int = 32
        public val EF_HYPERBLASTER: Int = 64
        public val EF_BFG: Int = 128
        public val EF_COLOR_SHELL: Int = 256
        public val EF_POWERSCREEN: Int = 512
        public val EF_ANIM01: Int = 1024 // automatically cycle between frames 0 and 1 at 2 hz
        public val EF_ANIM23: Int = 2048 // automatically cycle between frames 2 and 3 at 2 hz
        public val EF_ANIM_ALL: Int = 4096 // automatically cycle through all frames at 2hz
        public val EF_ANIM_ALLFAST: Int = 8192 // automatically cycle through all frames at 10hz
        public val EF_FLIES: Int = 16384
        public val EF_QUAD: Int = 32768
        public val EF_PENT: Int = 65536
        public val EF_TELEPORTER: Int = 131072 // particle fountain
        public val EF_FLAG1: Int = 262144
        public val EF_FLAG2: Int = 524288
        // RAFAEL
        public val EF_IONRIPPER: Int = 1048576
        public val EF_GREENGIB: Int = 2097152
        public val EF_BLUEHYPERBLASTER: Int = 4194304
        public val EF_SPINNINGLIGHTS: Int = 8388608
        public val EF_PLASMA: Int = 16777216
        public val EF_TRAP: Int = 33554432

        //ROGUE
        public val EF_TRACKER: Int = 67108864
        public val EF_DOUBLE: Int = 134217728
        public val EF_SPHERETRANS: Int = 268435456
        public val EF_TAGTRAIL: Int = 536870912
        public val EF_HALF_DAMAGE: Int = 1073741824
        public val EF_TRACKERTRAIL: Int = -2147483648
        //ROGUE

        // entity_state_t->renderfx flags
        public val RF_MINLIGHT: Int = 1 // allways have some light (viewmodel)
        public val RF_VIEWERMODEL: Int = 2 // don't draw through eyes, only mirrors
        public val RF_WEAPONMODEL: Int = 4 // only draw through eyes
        public val RF_FULLBRIGHT: Int = 8 // allways draw full intensity
        public val RF_DEPTHHACK: Int = 16 // for view weapon Z crunching
        public val RF_TRANSLUCENT: Int = 32
        public val RF_FRAMELERP: Int = 64
        public val RF_BEAM: Int = 128
        public val RF_CUSTOMSKIN: Int = 256 // skin is an index in image_precache
        public val RF_GLOW: Int = 512 // pulse lighting for bonus items
        public val RF_SHELL_RED: Int = 1024
        public val RF_SHELL_GREEN: Int = 2048
        public val RF_SHELL_BLUE: Int = 4096

        //ROGUE
        public val RF_IR_VISIBLE: Int = 32768 // 32768
        public val RF_SHELL_DOUBLE: Int = 65536 // 65536
        public val RF_SHELL_HALF_DAM: Int = 131072
        public val RF_USE_DISGUISE: Int = 262144
        //ROGUE

        // player_state_t->refdef flags
        public val RDF_UNDERWATER: Int = 1 // warp the screen as apropriate
        public val RDF_NOWORLDMODEL: Int = 2 // used for player configuration screen

        //ROGUE
        public val RDF_IRGOGGLES: Int = 4
        public val RDF_UVGOGGLES: Int = 8
        //ROGUE

        // muzzle flashes / player effects
        public val MZ_BLASTER: Int = 0
        public val MZ_MACHINEGUN: Int = 1
        public val MZ_SHOTGUN: Int = 2
        public val MZ_CHAINGUN1: Int = 3
        public val MZ_CHAINGUN2: Int = 4
        public val MZ_CHAINGUN3: Int = 5
        public val MZ_RAILGUN: Int = 6
        public val MZ_ROCKET: Int = 7
        public val MZ_GRENADE: Int = 8
        public val MZ_LOGIN: Int = 9
        public val MZ_LOGOUT: Int = 10
        public val MZ_RESPAWN: Int = 11
        public val MZ_BFG: Int = 12
        public val MZ_SSHOTGUN: Int = 13
        public val MZ_HYPERBLASTER: Int = 14
        public val MZ_ITEMRESPAWN: Int = 15
        // RAFAEL
        public val MZ_IONRIPPER: Int = 16
        public val MZ_BLUEHYPERBLASTER: Int = 17
        public val MZ_PHALANX: Int = 18
        public val MZ_SILENCED: Int = 128 // bit flag ORed with one of the above numbers

        //ROGUE
        public val MZ_ETF_RIFLE: Int = 30
        public val MZ_UNUSED: Int = 31
        public val MZ_SHOTGUN2: Int = 32
        public val MZ_HEATBEAM: Int = 33
        public val MZ_BLASTER2: Int = 34
        public val MZ_TRACKER: Int = 35
        public val MZ_NUKE1: Int = 36
        public val MZ_NUKE2: Int = 37
        public val MZ_NUKE4: Int = 38
        public val MZ_NUKE8: Int = 39
        //ROGUE

        //
        // monster muzzle flashes
        //
        public val MZ2_TANK_BLASTER_1: Int = 1
        public val MZ2_TANK_BLASTER_2: Int = 2
        public val MZ2_TANK_BLASTER_3: Int = 3
        public val MZ2_TANK_MACHINEGUN_1: Int = 4
        public val MZ2_TANK_MACHINEGUN_2: Int = 5
        public val MZ2_TANK_MACHINEGUN_3: Int = 6
        public val MZ2_TANK_MACHINEGUN_4: Int = 7
        public val MZ2_TANK_MACHINEGUN_5: Int = 8
        public val MZ2_TANK_MACHINEGUN_6: Int = 9
        public val MZ2_TANK_MACHINEGUN_7: Int = 10
        public val MZ2_TANK_MACHINEGUN_8: Int = 11
        public val MZ2_TANK_MACHINEGUN_9: Int = 12
        public val MZ2_TANK_MACHINEGUN_10: Int = 13
        public val MZ2_TANK_MACHINEGUN_11: Int = 14
        public val MZ2_TANK_MACHINEGUN_12: Int = 15
        public val MZ2_TANK_MACHINEGUN_13: Int = 16
        public val MZ2_TANK_MACHINEGUN_14: Int = 17
        public val MZ2_TANK_MACHINEGUN_15: Int = 18
        public val MZ2_TANK_MACHINEGUN_16: Int = 19
        public val MZ2_TANK_MACHINEGUN_17: Int = 20
        public val MZ2_TANK_MACHINEGUN_18: Int = 21
        public val MZ2_TANK_MACHINEGUN_19: Int = 22
        public val MZ2_TANK_ROCKET_1: Int = 23
        public val MZ2_TANK_ROCKET_2: Int = 24
        public val MZ2_TANK_ROCKET_3: Int = 25

        public val MZ2_INFANTRY_MACHINEGUN_1: Int = 26
        public val MZ2_INFANTRY_MACHINEGUN_2: Int = 27
        public val MZ2_INFANTRY_MACHINEGUN_3: Int = 28
        public val MZ2_INFANTRY_MACHINEGUN_4: Int = 29
        public val MZ2_INFANTRY_MACHINEGUN_5: Int = 30
        public val MZ2_INFANTRY_MACHINEGUN_6: Int = 31
        public val MZ2_INFANTRY_MACHINEGUN_7: Int = 32
        public val MZ2_INFANTRY_MACHINEGUN_8: Int = 33
        public val MZ2_INFANTRY_MACHINEGUN_9: Int = 34
        public val MZ2_INFANTRY_MACHINEGUN_10: Int = 35
        public val MZ2_INFANTRY_MACHINEGUN_11: Int = 36
        public val MZ2_INFANTRY_MACHINEGUN_12: Int = 37
        public val MZ2_INFANTRY_MACHINEGUN_13: Int = 38

        public val MZ2_SOLDIER_BLASTER_1: Int = 39
        public val MZ2_SOLDIER_BLASTER_2: Int = 40
        public val MZ2_SOLDIER_SHOTGUN_1: Int = 41
        public val MZ2_SOLDIER_SHOTGUN_2: Int = 42
        public val MZ2_SOLDIER_MACHINEGUN_1: Int = 43
        public val MZ2_SOLDIER_MACHINEGUN_2: Int = 44

        public val MZ2_GUNNER_MACHINEGUN_1: Int = 45
        public val MZ2_GUNNER_MACHINEGUN_2: Int = 46
        public val MZ2_GUNNER_MACHINEGUN_3: Int = 47
        public val MZ2_GUNNER_MACHINEGUN_4: Int = 48
        public val MZ2_GUNNER_MACHINEGUN_5: Int = 49
        public val MZ2_GUNNER_MACHINEGUN_6: Int = 50
        public val MZ2_GUNNER_MACHINEGUN_7: Int = 51
        public val MZ2_GUNNER_MACHINEGUN_8: Int = 52
        public val MZ2_GUNNER_GRENADE_1: Int = 53
        public val MZ2_GUNNER_GRENADE_2: Int = 54
        public val MZ2_GUNNER_GRENADE_3: Int = 55
        public val MZ2_GUNNER_GRENADE_4: Int = 56

        public val MZ2_CHICK_ROCKET_1: Int = 57

        public val MZ2_FLYER_BLASTER_1: Int = 58
        public val MZ2_FLYER_BLASTER_2: Int = 59

        public val MZ2_MEDIC_BLASTER_1: Int = 60

        public val MZ2_GLADIATOR_RAILGUN_1: Int = 61

        public val MZ2_HOVER_BLASTER_1: Int = 62

        public val MZ2_ACTOR_MACHINEGUN_1: Int = 63

        public val MZ2_SUPERTANK_MACHINEGUN_1: Int = 64
        public val MZ2_SUPERTANK_MACHINEGUN_2: Int = 65
        public val MZ2_SUPERTANK_MACHINEGUN_3: Int = 66
        public val MZ2_SUPERTANK_MACHINEGUN_4: Int = 67
        public val MZ2_SUPERTANK_MACHINEGUN_5: Int = 68
        public val MZ2_SUPERTANK_MACHINEGUN_6: Int = 69
        public val MZ2_SUPERTANK_ROCKET_1: Int = 70
        public val MZ2_SUPERTANK_ROCKET_2: Int = 71
        public val MZ2_SUPERTANK_ROCKET_3: Int = 72

        public val MZ2_BOSS2_MACHINEGUN_L1: Int = 73
        public val MZ2_BOSS2_MACHINEGUN_L2: Int = 74
        public val MZ2_BOSS2_MACHINEGUN_L3: Int = 75
        public val MZ2_BOSS2_MACHINEGUN_L4: Int = 76
        public val MZ2_BOSS2_MACHINEGUN_L5: Int = 77
        public val MZ2_BOSS2_ROCKET_1: Int = 78
        public val MZ2_BOSS2_ROCKET_2: Int = 79
        public val MZ2_BOSS2_ROCKET_3: Int = 80
        public val MZ2_BOSS2_ROCKET_4: Int = 81

        public val MZ2_FLOAT_BLASTER_1: Int = 82

        public val MZ2_SOLDIER_BLASTER_3: Int = 83
        public val MZ2_SOLDIER_SHOTGUN_3: Int = 84
        public val MZ2_SOLDIER_MACHINEGUN_3: Int = 85
        public val MZ2_SOLDIER_BLASTER_4: Int = 86
        public val MZ2_SOLDIER_SHOTGUN_4: Int = 87
        public val MZ2_SOLDIER_MACHINEGUN_4: Int = 88
        public val MZ2_SOLDIER_BLASTER_5: Int = 89
        public val MZ2_SOLDIER_SHOTGUN_5: Int = 90
        public val MZ2_SOLDIER_MACHINEGUN_5: Int = 91
        public val MZ2_SOLDIER_BLASTER_6: Int = 92
        public val MZ2_SOLDIER_SHOTGUN_6: Int = 93
        public val MZ2_SOLDIER_MACHINEGUN_6: Int = 94
        public val MZ2_SOLDIER_BLASTER_7: Int = 95
        public val MZ2_SOLDIER_SHOTGUN_7: Int = 96
        public val MZ2_SOLDIER_MACHINEGUN_7: Int = 97
        public val MZ2_SOLDIER_BLASTER_8: Int = 98
        public val MZ2_SOLDIER_SHOTGUN_8: Int = 99
        public val MZ2_SOLDIER_MACHINEGUN_8: Int = 100

        // --- Xian shit below ---
        public val MZ2_MAKRON_BFG: Int = 101
        public val MZ2_MAKRON_BLASTER_1: Int = 102
        public val MZ2_MAKRON_BLASTER_2: Int = 103
        public val MZ2_MAKRON_BLASTER_3: Int = 104
        public val MZ2_MAKRON_BLASTER_4: Int = 105
        public val MZ2_MAKRON_BLASTER_5: Int = 106
        public val MZ2_MAKRON_BLASTER_6: Int = 107
        public val MZ2_MAKRON_BLASTER_7: Int = 108
        public val MZ2_MAKRON_BLASTER_8: Int = 109
        public val MZ2_MAKRON_BLASTER_9: Int = 110
        public val MZ2_MAKRON_BLASTER_10: Int = 111
        public val MZ2_MAKRON_BLASTER_11: Int = 112
        public val MZ2_MAKRON_BLASTER_12: Int = 113
        public val MZ2_MAKRON_BLASTER_13: Int = 114
        public val MZ2_MAKRON_BLASTER_14: Int = 115
        public val MZ2_MAKRON_BLASTER_15: Int = 116
        public val MZ2_MAKRON_BLASTER_16: Int = 117
        public val MZ2_MAKRON_BLASTER_17: Int = 118
        public val MZ2_MAKRON_RAILGUN_1: Int = 119
        public val MZ2_JORG_MACHINEGUN_L1: Int = 120
        public val MZ2_JORG_MACHINEGUN_L2: Int = 121
        public val MZ2_JORG_MACHINEGUN_L3: Int = 122
        public val MZ2_JORG_MACHINEGUN_L4: Int = 123
        public val MZ2_JORG_MACHINEGUN_L5: Int = 124
        public val MZ2_JORG_MACHINEGUN_L6: Int = 125
        public val MZ2_JORG_MACHINEGUN_R1: Int = 126
        public val MZ2_JORG_MACHINEGUN_R2: Int = 127
        public val MZ2_JORG_MACHINEGUN_R3: Int = 128
        public val MZ2_JORG_MACHINEGUN_R4: Int = 129
        public val MZ2_JORG_MACHINEGUN_R5: Int = 130
        public val MZ2_JORG_MACHINEGUN_R6: Int = 131
        public val MZ2_JORG_BFG_1: Int = 132
        public val MZ2_BOSS2_MACHINEGUN_R1: Int = 133
        public val MZ2_BOSS2_MACHINEGUN_R2: Int = 134
        public val MZ2_BOSS2_MACHINEGUN_R3: Int = 135
        public val MZ2_BOSS2_MACHINEGUN_R4: Int = 136
        public val MZ2_BOSS2_MACHINEGUN_R5: Int = 137

        //ROGUE
        public val MZ2_CARRIER_MACHINEGUN_L1: Int = 138
        public val MZ2_CARRIER_MACHINEGUN_R1: Int = 139
        public val MZ2_CARRIER_GRENADE: Int = 140
        public val MZ2_TURRET_MACHINEGUN: Int = 141
        public val MZ2_TURRET_ROCKET: Int = 142
        public val MZ2_TURRET_BLASTER: Int = 143
        public val MZ2_STALKER_BLASTER: Int = 144
        public val MZ2_DAEDALUS_BLASTER: Int = 145
        public val MZ2_MEDIC_BLASTER_2: Int = 146
        public val MZ2_CARRIER_RAILGUN: Int = 147
        public val MZ2_WIDOW_DISRUPTOR: Int = 148
        public val MZ2_WIDOW_BLASTER: Int = 149
        public val MZ2_WIDOW_RAIL: Int = 150
        public val MZ2_WIDOW_PLASMABEAM: Int = 151 // PMM - not used
        public val MZ2_CARRIER_MACHINEGUN_L2: Int = 152
        public val MZ2_CARRIER_MACHINEGUN_R2: Int = 153
        public val MZ2_WIDOW_RAIL_LEFT: Int = 154
        public val MZ2_WIDOW_RAIL_RIGHT: Int = 155
        public val MZ2_WIDOW_BLASTER_SWEEP1: Int = 156
        public val MZ2_WIDOW_BLASTER_SWEEP2: Int = 157
        public val MZ2_WIDOW_BLASTER_SWEEP3: Int = 158
        public val MZ2_WIDOW_BLASTER_SWEEP4: Int = 159
        public val MZ2_WIDOW_BLASTER_SWEEP5: Int = 160
        public val MZ2_WIDOW_BLASTER_SWEEP6: Int = 161
        public val MZ2_WIDOW_BLASTER_SWEEP7: Int = 162
        public val MZ2_WIDOW_BLASTER_SWEEP8: Int = 163
        public val MZ2_WIDOW_BLASTER_SWEEP9: Int = 164
        public val MZ2_WIDOW_BLASTER_100: Int = 165
        public val MZ2_WIDOW_BLASTER_90: Int = 166
        public val MZ2_WIDOW_BLASTER_80: Int = 167
        public val MZ2_WIDOW_BLASTER_70: Int = 168
        public val MZ2_WIDOW_BLASTER_60: Int = 169
        public val MZ2_WIDOW_BLASTER_50: Int = 170
        public val MZ2_WIDOW_BLASTER_40: Int = 171
        public val MZ2_WIDOW_BLASTER_30: Int = 172
        public val MZ2_WIDOW_BLASTER_20: Int = 173
        public val MZ2_WIDOW_BLASTER_10: Int = 174
        public val MZ2_WIDOW_BLASTER_0: Int = 175
        public val MZ2_WIDOW_BLASTER_10L: Int = 176
        public val MZ2_WIDOW_BLASTER_20L: Int = 177
        public val MZ2_WIDOW_BLASTER_30L: Int = 178
        public val MZ2_WIDOW_BLASTER_40L: Int = 179
        public val MZ2_WIDOW_BLASTER_50L: Int = 180
        public val MZ2_WIDOW_BLASTER_60L: Int = 181
        public val MZ2_WIDOW_BLASTER_70L: Int = 182
        public val MZ2_WIDOW_RUN_1: Int = 183
        public val MZ2_WIDOW_RUN_2: Int = 184
        public val MZ2_WIDOW_RUN_3: Int = 185
        public val MZ2_WIDOW_RUN_4: Int = 186
        public val MZ2_WIDOW_RUN_5: Int = 187
        public val MZ2_WIDOW_RUN_6: Int = 188
        public val MZ2_WIDOW_RUN_7: Int = 189
        public val MZ2_WIDOW_RUN_8: Int = 190
        public val MZ2_CARRIER_ROCKET_1: Int = 191
        public val MZ2_CARRIER_ROCKET_2: Int = 192
        public val MZ2_CARRIER_ROCKET_3: Int = 193
        public val MZ2_CARRIER_ROCKET_4: Int = 194
        public val MZ2_WIDOW2_BEAMER_1: Int = 195
        public val MZ2_WIDOW2_BEAMER_2: Int = 196
        public val MZ2_WIDOW2_BEAMER_3: Int = 197
        public val MZ2_WIDOW2_BEAMER_4: Int = 198
        public val MZ2_WIDOW2_BEAMER_5: Int = 199
        public val MZ2_WIDOW2_BEAM_SWEEP_1: Int = 200
        public val MZ2_WIDOW2_BEAM_SWEEP_2: Int = 201
        public val MZ2_WIDOW2_BEAM_SWEEP_3: Int = 202
        public val MZ2_WIDOW2_BEAM_SWEEP_4: Int = 203
        public val MZ2_WIDOW2_BEAM_SWEEP_5: Int = 204
        public val MZ2_WIDOW2_BEAM_SWEEP_6: Int = 205
        public val MZ2_WIDOW2_BEAM_SWEEP_7: Int = 206
        public val MZ2_WIDOW2_BEAM_SWEEP_8: Int = 207
        public val MZ2_WIDOW2_BEAM_SWEEP_9: Int = 208
        public val MZ2_WIDOW2_BEAM_SWEEP_10: Int = 209
        public val MZ2_WIDOW2_BEAM_SWEEP_11: Int = 210

        public val SPLASH_UNKNOWN: Int = 0
        public val SPLASH_SPARKS: Int = 1
        public val SPLASH_BLUE_WATER: Int = 2
        public val SPLASH_BROWN_WATER: Int = 3
        public val SPLASH_SLIME: Int = 4
        public val SPLASH_LAVA: Int = 5
        public val SPLASH_BLOOD: Int = 6

        //	   sound channels
        //	   channel 0 never willingly overrides
        //	   other channels (1-7) allways override a playing sound on that channel
        public val CHAN_AUTO: Int = 0
        public val CHAN_WEAPON: Int = 1
        public val CHAN_VOICE: Int = 2
        public val CHAN_ITEM: Int = 3
        public val CHAN_BODY: Int = 4
        //	   modifier flags
        public val CHAN_NO_PHS_ADD: Int = 8
        // send to all clients, not just ones in PHS (ATTN 0 will also do this)
        public val CHAN_RELIABLE: Int = 16 // send by reliable message, not datagram

        //	   sound attenuation values
        public val ATTN_NONE: Int = 0 // full volume the entire level
        public val ATTN_NORM: Int = 1
        public val ATTN_IDLE: Int = 2
        public val ATTN_STATIC: Int = 3 // diminish very rapidly with distance

        //	   player_state->stats[] indexes
        public val STAT_HEALTH_ICON: Int = 0
        public val STAT_HEALTH: Int = 1
        public val STAT_AMMO_ICON: Int = 2
        public val STAT_AMMO: Int = 3
        public val STAT_ARMOR_ICON: Int = 4
        public val STAT_ARMOR: Int = 5
        public val STAT_SELECTED_ICON: Int = 6
        public val STAT_PICKUP_ICON: Int = 7
        public val STAT_PICKUP_STRING: Int = 8
        public val STAT_TIMER_ICON: Int = 9
        public val STAT_TIMER: Int = 10
        public val STAT_HELPICON: Int = 11
        public val STAT_SELECTED_ITEM: Int = 12
        public val STAT_LAYOUTS: Int = 13
        public val STAT_FRAGS: Int = 14
        public val STAT_FLASHES: Int = 15 // cleared each frame, 1 = health, 2 = armor
        public val STAT_CHASE: Int = 16
        public val STAT_SPECTATOR: Int = 17

        public val MAX_STATS: Int = 32

        //	   dmflags->value flags
        public val DF_NO_HEALTH: Int = 1 // 1
        public val DF_NO_ITEMS: Int = 2 // 2
        public val DF_WEAPONS_STAY: Int = 4 // 4
        public val DF_NO_FALLING: Int = 8 // 8
        public val DF_INSTANT_ITEMS: Int = 16 // 16
        public val DF_SAME_LEVEL: Int = 32 // 32
        public val DF_SKINTEAMS: Int = 64 // 64
        public val DF_MODELTEAMS: Int = 128 // 128
        public val DF_NO_FRIENDLY_FIRE: Int = 256 // 256
        public val DF_SPAWN_FARTHEST: Int = 512 // 512
        public val DF_FORCE_RESPAWN: Int = 1024 // 1024
        public val DF_NO_ARMOR: Int = 2048 // 2048
        public val DF_ALLOW_EXIT: Int = 4096 // 4096
        public val DF_INFINITE_AMMO: Int = 8192 // 8192
        public val DF_QUAD_DROP: Int = 16384 // 16384
        public val DF_FIXED_FOV: Int = 32768 // 32768

        //	   RAFAEL
        public val DF_QUADFIRE_DROP: Int = 65536 // 65536

        //	  ROGUE
        public val DF_NO_MINES: Int = 131072
        public val DF_NO_STACK_DOUBLE: Int = 262144
        public val DF_NO_NUKES: Int = 524288
        public val DF_NO_SPHERES: Int = 1048576
        //	  ROGUE

        //
        //	config strings are a general means of communication from
        //	the server to all connected clients.
        //	Each config string can be at most MAX_QPATH characters.
        //
        public val CS_NAME: Int = 0
        public val CS_CDTRACK: Int = 1
        public val CS_SKY: Int = 2
        public val CS_SKYAXIS: Int = 3 // %f %f %f format
        public val CS_SKYROTATE: Int = 4
        public val CS_STATUSBAR: Int = 5 // display program string

        public val CS_AIRACCEL: Int = 29 // air acceleration control
        public val CS_MAXCLIENTS: Int = 30
        public val CS_MAPCHECKSUM: Int = 31 // for catching cheater maps

        public val CS_MODELS: Int = 32
        public val CS_SOUNDS: Int = (CS_MODELS + MAX_MODELS)
        public val CS_IMAGES: Int = (CS_SOUNDS + MAX_SOUNDS)
        public val CS_LIGHTS: Int = (CS_IMAGES + MAX_IMAGES)
        public val CS_ITEMS: Int = (CS_LIGHTS + MAX_LIGHTSTYLES)
        public val CS_PLAYERSKINS: Int = (CS_ITEMS + MAX_ITEMS)
        public val CS_GENERAL: Int = (CS_PLAYERSKINS + MAX_CLIENTS)
        public val MAX_CONFIGSTRINGS: Int = (CS_GENERAL + MAX_GENERAL)

        public val HEALTH_IGNORE_MAX: Int = 1
        public val HEALTH_TIMED: Int = 2

        // gi.BoxEdicts() can return a list of either solid or trigger entities
        // FIXME: eliminate AREA_ distinction?
        public val AREA_SOLID: Int = 1
        public val AREA_TRIGGERS: Int = 2

        public val TE_GUNSHOT: Int = 0
        public val TE_BLOOD: Int = 1
        public val TE_BLASTER: Int = 2
        public val TE_RAILTRAIL: Int = 3
        public val TE_SHOTGUN: Int = 4
        public val TE_EXPLOSION1: Int = 5
        public val TE_EXPLOSION2: Int = 6
        public val TE_ROCKET_EXPLOSION: Int = 7
        public val TE_GRENADE_EXPLOSION: Int = 8
        public val TE_SPARKS: Int = 9
        public val TE_SPLASH: Int = 10
        public val TE_BUBBLETRAIL: Int = 11
        public val TE_SCREEN_SPARKS: Int = 12
        public val TE_SHIELD_SPARKS: Int = 13
        public val TE_BULLET_SPARKS: Int = 14
        public val TE_LASER_SPARKS: Int = 15
        public val TE_PARASITE_ATTACK: Int = 16
        public val TE_ROCKET_EXPLOSION_WATER: Int = 17
        public val TE_GRENADE_EXPLOSION_WATER: Int = 18
        public val TE_MEDIC_CABLE_ATTACK: Int = 19
        public val TE_BFG_EXPLOSION: Int = 20
        public val TE_BFG_BIGEXPLOSION: Int = 21
        public val TE_BOSSTPORT: Int = 22 // used as '22' in a map, so DON'T RENUMBER!!!
        public val TE_BFG_LASER: Int = 23
        public val TE_GRAPPLE_CABLE: Int = 24
        public val TE_WELDING_SPARKS: Int = 25
        public val TE_GREENBLOOD: Int = 26
        public val TE_BLUEHYPERBLASTER: Int = 27
        public val TE_PLASMA_EXPLOSION: Int = 28
        public val TE_TUNNEL_SPARKS: Int = 29
        //ROGUE
        public val TE_BLASTER2: Int = 30
        public val TE_RAILTRAIL2: Int = 31
        public val TE_FLAME: Int = 32
        public val TE_LIGHTNING: Int = 33
        public val TE_DEBUGTRAIL: Int = 34
        public val TE_PLAIN_EXPLOSION: Int = 35
        public val TE_FLASHLIGHT: Int = 36
        public val TE_FORCEWALL: Int = 37
        public val TE_HEATBEAM: Int = 38
        public val TE_MONSTER_HEATBEAM: Int = 39
        public val TE_STEAM: Int = 40
        public val TE_BUBBLETRAIL2: Int = 41
        public val TE_MOREBLOOD: Int = 42
        public val TE_HEATBEAM_SPARKS: Int = 43
        public val TE_HEATBEAM_STEAM: Int = 44
        public val TE_CHAINFIST_SMOKE: Int = 45
        public val TE_ELECTRIC_SPARKS: Int = 46
        public val TE_TRACKER_EXPLOSION: Int = 47
        public val TE_TELEPORT_EFFECT: Int = 48
        public val TE_DBALL_GOAL: Int = 49
        public val TE_WIDOWBEAMOUT: Int = 50
        public val TE_NUKEBLAST: Int = 51
        public val TE_WIDOWSPLASH: Int = 52
        public val TE_EXPLOSION1_BIG: Int = 53
        public val TE_EXPLOSION1_NP: Int = 54
        public val TE_FLECHETTE: Int = 55

        //	content masks
        public val MASK_ALL: Int = (-1)
        public val MASK_SOLID: Int = (CONTENTS_SOLID or CONTENTS_WINDOW)
        public val MASK_PLAYERSOLID: Int = (CONTENTS_SOLID or CONTENTS_PLAYERCLIP or CONTENTS_WINDOW or CONTENTS_MONSTER)
        public val MASK_DEADSOLID: Int = (CONTENTS_SOLID or CONTENTS_PLAYERCLIP or CONTENTS_WINDOW)
        public val MASK_MONSTERSOLID: Int = (CONTENTS_SOLID or CONTENTS_MONSTERCLIP or CONTENTS_WINDOW or CONTENTS_MONSTER)
        public val MASK_WATER: Int = (CONTENTS_WATER or CONTENTS_LAVA or CONTENTS_SLIME)
        public val MASK_OPAQUE: Int = (CONTENTS_SOLID or CONTENTS_SLIME or CONTENTS_LAVA)
        public val MASK_SHOT: Int = (CONTENTS_SOLID or CONTENTS_MONSTER or CONTENTS_WINDOW or CONTENTS_DEADMONSTER)
        public val MASK_CURRENT: Int = (CONTENTS_CURRENT_0 or CONTENTS_CURRENT_90 or CONTENTS_CURRENT_180 or CONTENTS_CURRENT_270 or CONTENTS_CURRENT_UP or CONTENTS_CURRENT_DOWN)

        // item spawnflags
        public val ITEM_TRIGGER_SPAWN: Int = 1
        public val ITEM_NO_TOUCH: Int = 2
        // 6 bits reserved for editor flags
        // 8 bits used as power cube id bits for coop games
        public val DROPPED_ITEM: Int = 65536
        public val DROPPED_PLAYER_ITEM: Int = 131072
        public val ITEM_TARGETS_USED: Int = 262144

        // (machen nur GL)
        public val VIDREF_GL: Int = 1
        public val VIDREF_SOFT: Int = 2
        public val VIDREF_OTHER: Int = 3

        // --------------
        // game/g_local.h

        public val FFL_SPAWNTEMP: Int = 1
        public val FFL_NOSPAWN: Int = 2

        // enum fieldtype_t
        public val F_INT: Int = 0
        public val F_FLOAT: Int = 1
        public val F_LSTRING: Int = 2 // string on disk, pointer in memory, TAG_LEVEL
        public val F_GSTRING: Int = 3 // string on disk, pointer in memory, TAG_GAME
        public val F_VECTOR: Int = 4
        public val F_ANGLEHACK: Int = 5
        public val F_EDICT: Int = 6 // index on disk, pointer in memory
        public val F_ITEM: Int = 7 // index on disk, pointer in memory
        public val F_CLIENT: Int = 8 // index on disk, pointer in memory
        public val F_FUNCTION: Int = 9
        public val F_MMOVE: Int = 10
        public val F_IGNORE: Int = 11

        public val DEFAULT_BULLET_HSPREAD: Int = 300
        public val DEFAULT_BULLET_VSPREAD: Int = 500
        public val DEFAULT_SHOTGUN_HSPREAD: Int = 1000
        public val DEFAULT_SHOTGUN_VSPREAD: Int = 500
        public val DEFAULT_DEATHMATCH_SHOTGUN_COUNT: Int = 12
        public val DEFAULT_SHOTGUN_COUNT: Int = 12
        public val DEFAULT_SSHOTGUN_COUNT: Int = 20

        public val ANIM_BASIC: Int = 0 // stand / run
        public val ANIM_WAVE: Int = 1
        public val ANIM_JUMP: Int = 2
        public val ANIM_PAIN: Int = 3
        public val ANIM_ATTACK: Int = 4
        public val ANIM_DEATH: Int = 5
        public val ANIM_REVERSE: Int = 6

        public val AMMO_BULLETS: Int = 0
        public val AMMO_SHELLS: Int = 1
        public val AMMO_ROCKETS: Int = 2
        public val AMMO_GRENADES: Int = 3
        public val AMMO_CELLS: Int = 4
        public val AMMO_SLUGS: Int = 5

        //	view pitching times
        public val DAMAGE_TIME: Float = 0.5.toFloat()
        public val FALL_TIME: Float = 0.3.toFloat()

        //	damage flags
        public val DAMAGE_RADIUS: Int = 1 // damage was indirect
        public val DAMAGE_NO_ARMOR: Int = 2 // armour does not protect from this damage
        public val DAMAGE_ENERGY: Int = 4 // damage is from an energy based weapon
        public val DAMAGE_NO_KNOCKBACK: Int = 8 // do not affect velocity, just view angles
        public val DAMAGE_BULLET: Int = 16 // damage is from a bullet (used for ricochets)
        public val DAMAGE_NO_PROTECTION: Int = 32
        // armor, shields, invulnerability, and godmode have no effect

        public val DAMAGE_NO: Int = 0
        public val DAMAGE_YES: Int = 1 // will take damage if hit
        public val DAMAGE_AIM: Int = 2 // auto targeting recognizes this

        //	means of death
        public val MOD_UNKNOWN: Int = 0
        public val MOD_BLASTER: Int = 1
        public val MOD_SHOTGUN: Int = 2
        public val MOD_SSHOTGUN: Int = 3
        public val MOD_MACHINEGUN: Int = 4
        public val MOD_CHAINGUN: Int = 5
        public val MOD_GRENADE: Int = 6
        public val MOD_G_SPLASH: Int = 7
        public val MOD_ROCKET: Int = 8
        public val MOD_R_SPLASH: Int = 9
        public val MOD_HYPERBLASTER: Int = 10
        public val MOD_RAILGUN: Int = 11
        public val MOD_BFG_LASER: Int = 12
        public val MOD_BFG_BLAST: Int = 13
        public val MOD_BFG_EFFECT: Int = 14
        public val MOD_HANDGRENADE: Int = 15
        public val MOD_HG_SPLASH: Int = 16
        public val MOD_WATER: Int = 17
        public val MOD_SLIME: Int = 18
        public val MOD_LAVA: Int = 19
        public val MOD_CRUSH: Int = 20
        public val MOD_TELEFRAG: Int = 21
        public val MOD_FALLING: Int = 22
        public val MOD_SUICIDE: Int = 23
        public val MOD_HELD_GRENADE: Int = 24
        public val MOD_EXPLOSIVE: Int = 25
        public val MOD_BARREL: Int = 26
        public val MOD_BOMB: Int = 27
        public val MOD_EXIT: Int = 28
        public val MOD_SPLASH: Int = 29
        public val MOD_TARGET_LASER: Int = 30
        public val MOD_TRIGGER_HURT: Int = 31
        public val MOD_HIT: Int = 32
        public val MOD_TARGET_BLASTER: Int = 33
        public val MOD_FRIENDLY_FIRE: Int = 134217728

        //	edict->spawnflags
        //	these are set with checkboxes on each entity in the map editor
        public val SPAWNFLAG_NOT_EASY: Int = 256
        public val SPAWNFLAG_NOT_MEDIUM: Int = 512
        public val SPAWNFLAG_NOT_HARD: Int = 1024
        public val SPAWNFLAG_NOT_DEATHMATCH: Int = 2048
        public val SPAWNFLAG_NOT_COOP: Int = 4096

        //	edict->flags
        public val FL_FLY: Int = 1
        public val FL_SWIM: Int = 2 // implied immunity to drowining
        public val FL_IMMUNE_LASER: Int = 4
        public val FL_INWATER: Int = 8
        public val FL_GODMODE: Int = 16
        public val FL_NOTARGET: Int = 32
        public val FL_IMMUNE_SLIME: Int = 64
        public val FL_IMMUNE_LAVA: Int = 128
        public val FL_PARTIALGROUND: Int = 256 // not all corners are valid
        public val FL_WATERJUMP: Int = 512 // player jumping out of water
        public val FL_TEAMSLAVE: Int = 1024 // not the first on the team
        public val FL_NO_KNOCKBACK: Int = 2048
        public val FL_POWER_ARMOR: Int = 4096 // power armor (if any) is active
        public val FL_RESPAWN: Int = -2147483648 // used for item respawning

        public val FRAMETIME: Float = 0.1.toFloat()

        //	memory tags to allow dynamic memory to be cleaned up
        public val TAG_GAME: Int = 765 // clear when unloading the dll
        public val TAG_LEVEL: Int = 766 // clear when loading a new level

        public val MELEE_DISTANCE: Int = 80

        public val BODY_QUEUE_SIZE: Int = 8

        //	deadflag
        public val DEAD_NO: Int = 0
        public val DEAD_DYING: Int = 1
        public val DEAD_DEAD: Int = 2
        public val DEAD_RESPAWNABLE: Int = 3

        //	range
        public val RANGE_MELEE: Int = 0
        public val RANGE_NEAR: Int = 1
        public val RANGE_MID: Int = 2
        public val RANGE_FAR: Int = 3

        //	gib types
        public val GIB_ORGANIC: Int = 0
        public val GIB_METALLIC: Int = 1

        //	monster ai flags
        public val AI_STAND_GROUND: Int = 1
        public val AI_TEMP_STAND_GROUND: Int = 2
        public val AI_SOUND_TARGET: Int = 4
        public val AI_LOST_SIGHT: Int = 8
        public val AI_PURSUIT_LAST_SEEN: Int = 16
        public val AI_PURSUE_NEXT: Int = 32
        public val AI_PURSUE_TEMP: Int = 64
        public val AI_HOLD_FRAME: Int = 128
        public val AI_GOOD_GUY: Int = 256
        public val AI_BRUTAL: Int = 512
        public val AI_NOSTEP: Int = 1024
        public val AI_DUCKED: Int = 2048
        public val AI_COMBAT_POINT: Int = 4096
        public val AI_MEDIC: Int = 8192
        public val AI_RESURRECTING: Int = 16384

        //	monster attack state
        public val AS_STRAIGHT: Int = 1
        public val AS_SLIDING: Int = 2
        public val AS_MELEE: Int = 3
        public val AS_MISSILE: Int = 4

        //	 armor types
        public val ARMOR_NONE: Int = 0
        public val ARMOR_JACKET: Int = 1
        public val ARMOR_COMBAT: Int = 2
        public val ARMOR_BODY: Int = 3
        public val ARMOR_SHARD: Int = 4

        //	 power armor types
        public val POWER_ARMOR_NONE: Int = 0
        public val POWER_ARMOR_SCREEN: Int = 1
        public val POWER_ARMOR_SHIELD: Int = 2

        //	 handedness values
        public val RIGHT_HANDED: Int = 0
        public val LEFT_HANDED: Int = 1
        public val CENTER_HANDED: Int = 2

        //	 game.serverflags values
        public val SFL_CROSS_TRIGGER_1: Int = 1
        public val SFL_CROSS_TRIGGER_2: Int = 2
        public val SFL_CROSS_TRIGGER_3: Int = 4
        public val SFL_CROSS_TRIGGER_4: Int = 8
        public val SFL_CROSS_TRIGGER_5: Int = 16
        public val SFL_CROSS_TRIGGER_6: Int = 32
        public val SFL_CROSS_TRIGGER_7: Int = 64
        public val SFL_CROSS_TRIGGER_8: Int = 128
        public val SFL_CROSS_TRIGGER_MASK: Int = 255

        //	 noise types for PlayerNoise
        public val PNOISE_SELF: Int = 0
        public val PNOISE_WEAPON: Int = 1
        public val PNOISE_IMPACT: Int = 2

        //	gitem_t->flags
        public val IT_WEAPON: Int = 1 // use makes active weapon
        public val IT_AMMO: Int = 2
        public val IT_ARMOR: Int = 4
        public val IT_STAY_COOP: Int = 8
        public val IT_KEY: Int = 16
        public val IT_POWERUP: Int = 32

        //	gitem_t->weapmodel for weapons indicates model index
        public val WEAP_BLASTER: Int = 1
        public val WEAP_SHOTGUN: Int = 2
        public val WEAP_SUPERSHOTGUN: Int = 3
        public val WEAP_MACHINEGUN: Int = 4
        public val WEAP_CHAINGUN: Int = 5
        public val WEAP_GRENADES: Int = 6
        public val WEAP_GRENADELAUNCHER: Int = 7
        public val WEAP_ROCKETLAUNCHER: Int = 8
        public val WEAP_HYPERBLASTER: Int = 9
        public val WEAP_RAILGUN: Int = 10
        public val WEAP_BFG: Int = 11

        //	edict->movetype values
        public val MOVETYPE_NONE: Int = 0 // never moves
        public val MOVETYPE_NOCLIP: Int = 1 // origin and angles change with no interaction
        public val MOVETYPE_PUSH: Int = 2 // no clip to world, push on box contact
        public val MOVETYPE_STOP: Int = 3 // no clip to world, stops on box contact

        public val MOVETYPE_WALK: Int = 4 // gravity
        public val MOVETYPE_STEP: Int = 5 // gravity, special edge handling
        public val MOVETYPE_FLY: Int = 6
        public val MOVETYPE_TOSS: Int = 7 // gravity
        public val MOVETYPE_FLYMISSILE: Int = 8 // extra size to monsters
        public val MOVETYPE_BOUNCE: Int = 9

        public val MULTICAST_ALL: Int = 0
        public val MULTICAST_PHS: Int = 1
        public val MULTICAST_PVS: Int = 2
        public val MULTICAST_ALL_R: Int = 3
        public val MULTICAST_PHS_R: Int = 4
        public val MULTICAST_PVS_R: Int = 5

        // -------------
        // client/game.h

        public val SOLID_NOT: Int = 0 // no interaction with other objects
        public val SOLID_TRIGGER: Int = 1 // only touch when inside, after moving
        public val SOLID_BBOX: Int = 2 // touch on edge
        public val SOLID_BSP: Int = 3 // bsp clip, touch on edge

        public val GAME_API_VERSION: Int = 3

        //	   edict->svflags
        public val SVF_NOCLIENT: Int = 1 // don't send entity to clients, even if it has effects
        public val SVF_DEADMONSTER: Int = 2 // treat as CONTENTS_DEADMONSTER for collision
        public val SVF_MONSTER: Int = 4 // treat as CONTENTS_MONSTER for collision

        public val MAX_ENT_CLUSTERS: Int = 16

        public val sv_stopspeed: Int = 100
        public val sv_friction: Int = 6
        public val sv_waterfriction: Int = 1

        public val PLAT_LOW_TRIGGER: Int = 1

        public val STATE_TOP: Int = 0
        public val STATE_BOTTOM: Int = 1
        public val STATE_UP: Int = 2
        public val STATE_DOWN: Int = 3

        public val DOOR_START_OPEN: Int = 1
        public val DOOR_REVERSE: Int = 2
        public val DOOR_CRUSHER: Int = 4
        public val DOOR_NOMONSTER: Int = 8
        public val DOOR_TOGGLE: Int = 32
        public val DOOR_X_AXIS: Int = 64
        public val DOOR_Y_AXIS: Int = 128

        // R E N D E R E R
        ////////////////////
        public val MAX_DLIGHTS: Int = 32
        public val MAX_ENTITIES: Int = 128
        public val MAX_PARTICLES: Int = 4096

        // gl_model.h
        public val SURF_PLANEBACK: Int = 2
        public val SURF_DRAWSKY: Int = 4
        public val SURF_DRAWTURB: Int = 16
        public val SURF_DRAWBACKGROUND: Int = 64
        public val SURF_UNDERWATER: Int = 128

        public val POWERSUIT_SCALE: Float = 4.0.toFloat()

        public val SHELL_RED_COLOR: Int = 242
        public val SHELL_GREEN_COLOR: Int = 208
        public val SHELL_BLUE_COLOR: Int = 243

        public val SHELL_RG_COLOR: Int = 220

        public val SHELL_RB_COLOR: Int = 104 //0x86
        public val SHELL_BG_COLOR: Int = 120

        // ROGUE
        public val SHELL_DOUBLE_COLOR: Int = 223 // 223
        public val SHELL_HALF_DAM_COLOR: Int = 144
        public val SHELL_CYAN_COLOR: Int = 114

        // ---------
        // qcommon.h

        public val svc_bad: Int = 0

        // these ops are known to the game dll
        // protocol bytes that can be directly added to messages

        public val svc_muzzleflash: Int = 1
        public val svc_muzzleflash2: Int = 2
        public val svc_temp_entity: Int = 3
        public val svc_layout: Int = 4
        public val svc_inventory: Int = 5

        // the rest are private to the client and server
        public val svc_nop: Int = 6
        public val svc_disconnect: Int = 7
        public val svc_reconnect: Int = 8
        public val svc_sound: Int = 9 // <see code>
        public val svc_print: Int = 10 // [byte] id [string] null terminated string
        public val svc_stufftext: Int = 11
        // [string] stuffed into client's console buffer, should be \n terminated
        public val svc_serverdata: Int = 12 // [long] protocol ...
        public val svc_configstring: Int = 13 // [short] [string]
        public val svc_spawnbaseline: Int = 14
        public val svc_centerprint: Int = 15 // [string] to put in center of the screen
        public val svc_download: Int = 16 // [short] size [size bytes]
        public val svc_playerinfo: Int = 17 // variable
        public val svc_packetentities: Int = 18 // [...]
        public val svc_deltapacketentities: Int = 19 // [...]
        public val svc_frame: Int = 20

        public val NUMVERTEXNORMALS: Int = 162
        public val PROTOCOL_VERSION: Int = 34
        public val PORT_MASTER: Int = 27900
        public val PORT_CLIENT: Int = 27901
        public val PORT_SERVER: Int = 27910
        public val PORT_ANY: Int = -1

        public val PS_M_TYPE: Int = (1 shl 0)
        public val PS_M_ORIGIN: Int = (1 shl 1)
        public val PS_M_VELOCITY: Int = (1 shl 2)
        public val PS_M_TIME: Int = (1 shl 3)
        public val PS_M_FLAGS: Int = (1 shl 4)
        public val PS_M_GRAVITY: Int = (1 shl 5)
        public val PS_M_DELTA_ANGLES: Int = (1 shl 6)

        public val UPDATE_BACKUP: Int = 16 // copies of entity_state_t to keep buffered
        // must be power of two
        public val UPDATE_MASK: Int = (UPDATE_BACKUP - 1)

        public val PS_VIEWOFFSET: Int = (1 shl 7)
        public val PS_VIEWANGLES: Int = (1 shl 8)
        public val PS_KICKANGLES: Int = (1 shl 9)
        public val PS_BLEND: Int = (1 shl 10)
        public val PS_FOV: Int = (1 shl 11)
        public val PS_WEAPONINDEX: Int = (1 shl 12)
        public val PS_WEAPONFRAME: Int = (1 shl 13)
        public val PS_RDFLAGS: Int = (1 shl 14)

        public val CM_ANGLE1: Int = (1 shl 0)
        public val CM_ANGLE2: Int = (1 shl 1)
        public val CM_ANGLE3: Int = (1 shl 2)
        public val CM_FORWARD: Int = (1 shl 3)
        public val CM_SIDE: Int = (1 shl 4)
        public val CM_UP: Int = (1 shl 5)
        public val CM_BUTTONS: Int = (1 shl 6)
        public val CM_IMPULSE: Int = (1 shl 7)

        // try to pack the common update flags into the first byte
        public val U_ORIGIN1: Int = (1 shl 0)
        public val U_ORIGIN2: Int = (1 shl 1)
        public val U_ANGLE2: Int = (1 shl 2)
        public val U_ANGLE3: Int = (1 shl 3)
        public val U_FRAME8: Int = (1 shl 4) // frame is a byte
        public val U_EVENT: Int = (1 shl 5)
        public val U_REMOVE: Int = (1 shl 6) // REMOVE this entity, don't add it
        public val U_MOREBITS1: Int = (1 shl 7) // read one additional byte

        // second byte
        public val U_NUMBER16: Int = (1 shl 8) // NUMBER8 is implicit if not set
        public val U_ORIGIN3: Int = (1 shl 9)
        public val U_ANGLE1: Int = (1 shl 10)
        public val U_MODEL: Int = (1 shl 11)
        public val U_RENDERFX8: Int = (1 shl 12) // fullbright, etc
        public val U_EFFECTS8: Int = (1 shl 14) // autorotate, trails, etc
        public val U_MOREBITS2: Int = (1 shl 15) // read one additional byte

        // third byte
        public val U_SKIN8: Int = (1 shl 16)
        public val U_FRAME16: Int = (1 shl 17) // frame is a short
        public val U_RENDERFX16: Int = (1 shl 18) // 8 + 16 = 32
        public val U_EFFECTS16: Int = (1 shl 19) // 8 + 16 = 32
        public val U_MODEL2: Int = (1 shl 20) // weapons, flags, etc
        public val U_MODEL3: Int = (1 shl 21)
        public val U_MODEL4: Int = (1 shl 22)
        public val U_MOREBITS3: Int = (1 shl 23) // read one additional byte

        // fourth byte
        public val U_OLDORIGIN: Int = (1 shl 24) // FIXME: get rid of this
        public val U_SKIN16: Int = (1 shl 25)
        public val U_SOUND: Int = (1 shl 26)
        public val U_SOLID: Int = (1 shl 27)

        public val SHELL_WHITE_COLOR: Int = 215

        public val MAX_TRIANGLES: Int = 4096
        public val MAX_VERTS: Int = 2048
        public val MAX_FRAMES: Int = 512
        public val MAX_MD2SKINS: Int = 32
        public val MAX_SKINNAME: Int = 64

        public val MAXLIGHTMAPS: Int = 4
        public val MIPLEVELS: Int = 4

        public val clc_bad: Int = 0
        public val clc_nop: Int = 1
        public val clc_move: Int = 2 // [[usercmd_t]
        public val clc_userinfo: Int = 3 // [[userinfo string]
        public val clc_stringcmd: Int = 4 // [string] message

        public val NS_CLIENT: Int = 0
        public val NS_SERVER: Int = 1

        public val NA_LOOPBACK: Int = 0
        public val NA_BROADCAST: Int = 1
        public val NA_IP: Int = 2
        public val NA_IPX: Int = 3
        public val NA_BROADCAST_IPX: Int = 4

        public val SND_VOLUME: Int = (1 shl 0) // a byte
        public val SND_ATTENUATION: Int = (1 shl 1) // a byte
        public val SND_POS: Int = (1 shl 2) // three coordinates
        public val SND_ENT: Int = (1 shl 3) // a short 0-2: channel, 3-12: entity
        public val SND_OFFSET: Int = (1 shl 4) // a byte, msec offset from frame start

        public val DEFAULT_SOUND_PACKET_VOLUME: Float = 1.0.toFloat()
        public val DEFAULT_SOUND_PACKET_ATTENUATION: Float = 1.0.toFloat()

        // --------
        // client.h
        public val MAX_PARSE_ENTITIES: Int = 1024
        public val MAX_CLIENTWEAPONMODELS: Int = 20

        public var CMD_BACKUP: Int = 64 // allow a lot of command backups for very fast systems

        public val ca_uninitialized: Int = 0
        public val ca_disconnected: Int = 1
        public val ca_connecting: Int = 2
        public val ca_connected: Int = 3
        public val ca_active: Int = 4

        public val MAX_ALIAS_NAME: Int = 32
        public val MAX_NUM_ARGVS: Int = 50

        public val MAX_MSGLEN: Int = 1400

        // ---------
        // console.h
        public val NUM_CON_TIMES: Int = 4
        public val CON_TEXTSIZE: Int = 32768

        public val BSPVERSION: Int = 38

        // --------
        // qfiles.h

        // upper design bounds
        // leaffaces, leafbrushes, planes, and verts are still bounded by
        // 16 bit short limits
        public val MAX_MAP_MODELS: Int = 1024
        public val MAX_MAP_BRUSHES: Int = 8192
        public val MAX_MAP_ENTITIES: Int = 2048
        public val MAX_MAP_ENTSTRING: Int = 262144
        public val MAX_MAP_TEXINFO: Int = 8192

        public val MAX_MAP_AREAS: Int = 256
        public val MAX_MAP_AREAPORTALS: Int = 1024
        public val MAX_MAP_PLANES: Int = 65536
        public val MAX_MAP_NODES: Int = 65536
        public val MAX_MAP_BRUSHSIDES: Int = 65536
        public val MAX_MAP_LEAFS: Int = 65536
        public val MAX_MAP_VERTS: Int = 65536
        public val MAX_MAP_FACES: Int = 65536
        public val MAX_MAP_LEAFFACES: Int = 65536
        public val MAX_MAP_LEAFBRUSHES: Int = 65536
        public val MAX_MAP_PORTALS: Int = 65536
        public val MAX_MAP_EDGES: Int = 128000
        public val MAX_MAP_SURFEDGES: Int = 256000
        public val MAX_MAP_LIGHTING: Int = 2097152
        public val MAX_MAP_VISIBILITY: Int = 1048576

        // key / value pair sizes
        public val MAX_KEY: Int = 32
        public val MAX_VALUE: Int = 1024

        // 0-2 are axial planes
        public val PLANE_X: Int = 0
        public val PLANE_Y: Int = 1
        public val PLANE_Z: Int = 2

        // 3-5 are non-axial planes snapped to the nearest
        public val PLANE_ANYX: Int = 3
        public val PLANE_ANYY: Int = 4
        public val PLANE_ANYZ: Int = 5

        public val LUMP_ENTITIES: Int = 0
        public val LUMP_PLANES: Int = 1
        public val LUMP_VERTEXES: Int = 2
        public val LUMP_VISIBILITY: Int = 3
        public val LUMP_NODES: Int = 4
        public val LUMP_TEXINFO: Int = 5
        public val LUMP_FACES: Int = 6
        public val LUMP_LIGHTING: Int = 7
        public val LUMP_LEAFS: Int = 8
        public val LUMP_LEAFFACES: Int = 9
        public val LUMP_LEAFBRUSHES: Int = 10
        public val LUMP_EDGES: Int = 11
        public val LUMP_SURFEDGES: Int = 12
        public val LUMP_MODELS: Int = 13
        public val LUMP_BRUSHES: Int = 14
        public val LUMP_BRUSHSIDES: Int = 15
        public val LUMP_POP: Int = 16
        public val LUMP_AREAS: Int = 17
        public val LUMP_AREAPORTALS: Int = 18
        public val HEADER_LUMPS: Int = 19

        public val DTRIVERTX_V0: Int = 0
        public val DTRIVERTX_V1: Int = 1
        public val DTRIVERTX_V2: Int = 2
        public val DTRIVERTX_LNI: Int = 3
        public val DTRIVERTX_SIZE: Int = 4

        public val ALIAS_VERSION: Int = 8
        public val GAMEVERSION: String = "baseq2"
        public val API_VERSION: Int = 3 // ref_library (refexport_t)

        public val DVIS_PVS: Int = 0
        public val DVIS_PHS: Int = 1

        // ----------------
        // client/keydest_t
        public val key_game: Int = 0
        public val key_console: Int = 1
        public val key_message: Int = 2
        public val key_menu: Int = 3

        // ---------------
        // server/server.h
        public val cs_free: Int = 0 // can be reused for a new connection
        public val cs_zombie: Int = 1 // client has been disconnected, but don't reuse
        // connection for a couple seconds
        public val cs_connected: Int = 2 // has been assigned to a client_t, but not in game yet
        public val cs_spawned: Int = 3

        public val MAX_CHALLENGES: Int = 1024

        public val ss_dead: Int = 0 // no map loaded
        public val ss_loading: Int = 1 // spawning level edicts
        public val ss_game: Int = 2 // actively running
        public val ss_cinematic: Int = 3
        public val ss_demo: Int = 4
        public val ss_pic: Int = 5

        public val SV_OUTPUTBUF_LENGTH: Int = (MAX_MSGLEN - 16)
        public val RD_NONE: Int = 0
        public val RD_CLIENT: Int = 1
        public val RD_PACKET: Int = 2

        public val RATE_MESSAGES: Int = 10

        public val LATENCY_COUNTS: Int = 16

        public val MAXCMDLINE: Int = 256

        public val MAX_MASTERS: Int = 8

        //server/sv_world.h
        public val AREA_DEPTH: Int = 4
        public val AREA_NODES: Int = 32

        public val EXEC_NOW: Int = 0
        public val EXEC_INSERT: Int = 1
        public val EXEC_APPEND: Int = 2

        //client/qmenu.h
        public val MAXMENUITEMS: Int = 64

        public val MTYPE_SLIDER: Int = 0
        public val MTYPE_LIST: Int = 1
        public val MTYPE_ACTION: Int = 2
        public val MTYPE_SPINCONTROL: Int = 3
        public val MTYPE_SEPARATOR: Int = 4
        public val MTYPE_FIELD: Int = 5

        public val K_TAB: Int = 9
        public val K_ENTER: Int = 13
        public val K_ESCAPE: Int = 27
        public val K_SPACE: Int = 32

        // normal keys should be passed as lowercased ascii

        public val K_BACKSPACE: Int = 127
        public val K_UPARROW: Int = 128
        public val K_DOWNARROW: Int = 129
        public val K_LEFTARROW: Int = 130
        public val K_RIGHTARROW: Int = 131

        public val QMF_LEFT_JUSTIFY: Int = 1
        public val QMF_GRAYED: Int = 2
        public val QMF_NUMBERSONLY: Int = 4

        public val RCOLUMN_OFFSET: Int = 16
        public val LCOLUMN_OFFSET: Int = -16

        public val MAX_DISPLAYNAME: Int = 16
        public val MAX_PLAYERMODELS: Int = 1024

        public val MAX_LOCAL_SERVERS: Int = 8
        public val NO_SERVER_STRING: String = "<no server>"
        public val NUM_ADDRESSBOOK_ENTRIES: Int = 9

        public val STEPSIZE: Int = 18


        public val MOVE_STOP_EPSILON: Float = 0.1.toFloat()

        public val MIN_STEP_NORMAL: Float = 0.7.toFloat() // can't step up onto very steep slopes


        // used by filefinders in Sys
        public val FILEISREADABLE: Int = 1

        public val FILEISWRITABLE: Int = 2

        public val FILEISFILE: Int = 4

        public val FILEISDIRECTORY: Int = 8

        // datentyp konstanten
        // groesse in bytes
        public val LITTLE_ENDIAN: Boolean = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)

        public val SIZE_OF_SHORT: Int = 2

        public val SIZE_OF_INT: Int = 4

        public val SIZE_OF_LONG: Int = 8

        public val SIZE_OF_FLOAT: Int = 4

        public val SIZE_OF_DOUBLE: Int = 8
    }

}