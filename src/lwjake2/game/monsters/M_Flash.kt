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

public class M_Flash {
        companion object {

                //	   m_flash.c

                //	   this file is included in both the game dll and quake2,
                //	   the game needs it to source shot locations, the client
                //	   needs it to position muzzle flashes
                public var monster_flash_offset: Array<FloatArray> = array<FloatArray>(//		flash 0 is not used
                        floatArray(0.0.toFloat(), 0.0.toFloat(), 0.0.toFloat()).toFloat(),
                        //		MZ2_TANK_BLASTER_1 1
                        floatArray(20.7.toFloat(), (-18.5.toFloat()).toFloat(), 28.7.toFloat()).toFloat(), //		MZ2_TANK_BLASTER_2 2
                        floatArray(16.6.toFloat(), (-21.5.toFloat()).toFloat(), 30.1.toFloat()).toFloat(), //		MZ2_TANK_BLASTER_3 3
                        floatArray(11.8.toFloat(), (-23.9.toFloat()).toFloat(), 32.1.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_1 4
                        floatArray(22.9.toFloat(), (-0.7.toFloat()).toFloat(), 25.3.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_2 5
                        floatArray(22.2.toFloat(), 6.2.toFloat(), 22.3.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_3 6
                        floatArray(19.4.toFloat(), 13.1.toFloat(), 18.6.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_4 7
                        floatArray(19.4.toFloat(), 18.8.toFloat(), 18.6.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_5 8
                        floatArray(17.9.toFloat(), 25.0.toFloat(), 18.6.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_6 9
                        floatArray(14.1.toFloat(), 30.5.toFloat(), 20.6.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_7 10
                        floatArray(9.3.toFloat(), 35.3.toFloat(), 22.1.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_8 11
                        floatArray(4.7.toFloat(), 38.4.toFloat(), 22.1.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_9 12
                        floatArray((-1.1.toFloat()).toFloat(), 40.4.toFloat(), 24.1.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_10 13
                        floatArray((-6.5.toFloat()).toFloat(), 41.2.toFloat(), 24.1.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_11 14
                        floatArray(3.2.toFloat(), 40.1.toFloat(), 24.7.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_12 15
                        floatArray(11.7.toFloat(), 36.7.toFloat(), 26.0.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_13 16
                        floatArray(18.9.toFloat(), 31.3.toFloat(), 26.0.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_14 17
                        floatArray(24.4.toFloat(), 24.4.toFloat(), 26.4.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_15 18
                        floatArray(27.1.toFloat(), 17.1.toFloat(), 27.2.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_16 19
                        floatArray(28.5.toFloat(), 9.1.toFloat(), 28.0.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_17 20
                        floatArray(27.1.toFloat(), 2.2.toFloat(), 28.0.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_18 21
                        floatArray(24.9.toFloat(), (-2.8.toFloat()).toFloat(), 28.0.toFloat()).toFloat(), //		MZ2_TANK_MACHINEGUN_19 22
                        floatArray(21.6.toFloat(), (-7.0.toFloat()).toFloat(), 26.4.toFloat()).toFloat(), //		MZ2_TANK_ROCKET_1 23
                        floatArray(6.2.toFloat(), 29.1.toFloat(), 49.1.toFloat()).toFloat(), //		MZ2_TANK_ROCKET_2 24
                        floatArray(6.9.toFloat(), 23.8.toFloat(), 49.1.toFloat()).toFloat(), //		MZ2_TANK_ROCKET_3 25
                        floatArray(8.3.toFloat(), 17.8.toFloat(), 49.5.toFloat()).toFloat(),
                        //		MZ2_INFANTRY_MACHINEGUN_1 26
                        floatArray(26.6.toFloat(), 7.1.toFloat(), 13.1.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_2 27
                        floatArray(18.2.toFloat(), 7.5.toFloat(), 15.4.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_3 28
                        floatArray(17.2.toFloat(), 10.3.toFloat(), 17.9.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_4 29
                        floatArray(17.0.toFloat(), 12.8.toFloat(), 20.1.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_5 30
                        floatArray(15.1.toFloat(), 14.1.toFloat(), 21.8.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_6 31
                        floatArray(11.8.toFloat(), 17.2.toFloat(), 23.1.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_7 32
                        floatArray(11.4.toFloat(), 20.2.toFloat(), 21.0.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_8 33
                        floatArray(9.0.toFloat(), 23.0.toFloat(), 18.9.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_9 34
                        floatArray(13.9.toFloat(), 18.6.toFloat(), 17.7.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_10 35
                        floatArray(15.4.toFloat(), 15.6.toFloat(), 15.8.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_11 36
                        floatArray(10.2.toFloat(), 15.2.toFloat(), 25.1.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_12 37
                        floatArray((-1.9.toFloat()).toFloat(), 15.1.toFloat(), 28.2.toFloat()).toFloat(), //		MZ2_INFANTRY_MACHINEGUN_13 38
                        floatArray((-12.4.toFloat()).toFloat(), 13.0.toFloat(), 20.2.toFloat()).toFloat(),
                        //		MZ2_SOLDIER_BLASTER_1 39
                        floatArray((10.6.toFloat() * 1.2.toFloat()).toFloat(), (7.7.toFloat() * 1.2.toFloat()).toFloat(), (7.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_2 40
                        floatArray((21.1.toFloat() * 1.2.toFloat()).toFloat(), (3.6.toFloat() * 1.2.toFloat()).toFloat(), (19.0.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_1 41
                        floatArray((10.6.toFloat() * 1.2.toFloat()).toFloat(), (7.7.toFloat() * 1.2.toFloat()).toFloat(), (7.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_2 42
                        floatArray((21.1.toFloat() * 1.2.toFloat()).toFloat(), (3.6.toFloat() * 1.2.toFloat()).toFloat(), (19.0.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_1 43
                        floatArray((10.6.toFloat() * 1.2.toFloat()).toFloat(), (7.7.toFloat() * 1.2.toFloat()).toFloat(), (7.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_2 44
                        floatArray((21.1.toFloat() * 1.2.toFloat()).toFloat(), (3.6.toFloat() * 1.2.toFloat()).toFloat(), (19.0.toFloat() * 1.2.toFloat()).toFloat()).toFloat(),
                        //		MZ2_GUNNER_MACHINEGUN_1 45
                        floatArray((30.1.toFloat() * 1.15.toFloat()).toFloat(), (3.9.toFloat() * 1.15.toFloat()).toFloat(), (19.6.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_2 46
                        floatArray((29.1.toFloat() * 1.15.toFloat()).toFloat(), (2.5.toFloat() * 1.15.toFloat()).toFloat(), (20.7.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_3 47
                        floatArray((28.2.toFloat() * 1.15.toFloat()).toFloat(), (2.5.toFloat() * 1.15.toFloat()).toFloat(), (22.2.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_4 48
                        floatArray((28.2.toFloat() * 1.15.toFloat()).toFloat(), (3.6.toFloat() * 1.15.toFloat()).toFloat(), (22.0.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_5 49
                        floatArray((26.9.toFloat() * 1.15.toFloat()).toFloat(), (2.0.toFloat() * 1.15.toFloat()).toFloat(), (23.4.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_6 50
                        floatArray((26.5.toFloat() * 1.15.toFloat()).toFloat(), (0.6.toFloat() * 1.15.toFloat()).toFloat(), (20.8.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_7 51
                        floatArray((26.9.toFloat() * 1.15.toFloat()).toFloat(), (0.5.toFloat() * 1.15.toFloat()).toFloat(), (21.5.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_MACHINEGUN_8 52
                        floatArray((29.0.toFloat() * 1.15.toFloat()).toFloat(), (2.4.toFloat() * 1.15.toFloat()).toFloat(), (19.5.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_GRENADE_1 53
                        floatArray((4.6.toFloat() * 1.15.toFloat()).toFloat(), (-16.8.toFloat() * 1.15.toFloat()).toFloat(), (7.3.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_GRENADE_2 54
                        floatArray((4.6.toFloat() * 1.15.toFloat()).toFloat(), (-16.8.toFloat() * 1.15.toFloat()).toFloat(), (7.3.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_GRENADE_3 55
                        floatArray((4.6.toFloat() * 1.15.toFloat()).toFloat(), (-16.8.toFloat() * 1.15.toFloat()).toFloat(), (7.3.toFloat() * 1.15.toFloat()).toFloat()).toFloat(), //		MZ2_GUNNER_GRENADE_4 56
                        floatArray((4.6.toFloat() * 1.15.toFloat()).toFloat(), (-16.8.toFloat() * 1.15.toFloat()).toFloat(), (7.3.toFloat() * 1.15.toFloat()).toFloat()).toFloat(),
                        //		MZ2_CHICK_ROCKET_1 57
                        //		 -24.8f, -9.0f, 39.0f},
                        floatArray(24.8.toFloat(), (-9.0.toFloat()).toFloat(), 39.0.toFloat()).toFloat(), // PGM - this was incorrect in Q2

                        //		MZ2_FLYER_BLASTER_1 58
                        floatArray(12.1.toFloat(), 13.4.toFloat(), (-14.5.toFloat()).toFloat()).toFloat(), //		MZ2_FLYER_BLASTER_2 59
                        floatArray(12.1.toFloat(), (-7.4.toFloat()).toFloat(), (-14.5.toFloat()).toFloat()).toFloat(),
                        //		MZ2_MEDIC_BLASTER_1 60
                        floatArray(12.1.toFloat(), 5.4.toFloat(), 16.5.toFloat()).toFloat(),
                        //		MZ2_GLADIATOR_RAILGUN_1 61
                        floatArray(30.0.toFloat(), 18.0.toFloat(), 28.0.toFloat()).toFloat(),
                        //		MZ2_HOVER_BLASTER_1 62
                        floatArray(32.5.toFloat(), (-0.8.toFloat()).toFloat(), 10.0.toFloat()).toFloat(),
                        //		MZ2_ACTOR_MACHINEGUN_1 63
                        floatArray(18.4.toFloat(), 7.4.toFloat(), 9.6.toFloat()).toFloat(),
                        //		MZ2_SUPERTANK_MACHINEGUN_1 64
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_MACHINEGUN_2 65
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_MACHINEGUN_3 66
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_MACHINEGUN_4 67
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_MACHINEGUN_5 68
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_MACHINEGUN_6 69
                        floatArray(30.0.toFloat(), 30.0.toFloat(), 88.5.toFloat()).toFloat(), //		MZ2_SUPERTANK_ROCKET_1 70
                        floatArray(16.0.toFloat(), (-22.5.toFloat()).toFloat(), 91.2.toFloat()).toFloat(), //		MZ2_SUPERTANK_ROCKET_2 71
                        floatArray(16.0.toFloat(), (-33.4.toFloat()).toFloat(), 86.7.toFloat()).toFloat(), //		MZ2_SUPERTANK_ROCKET_3 72
                        floatArray(16.0.toFloat(), (-42.8.toFloat()).toFloat(), 83.3.toFloat()).toFloat(),
                        //		--- Start Xian Stuff ---
                        //		MZ2_BOSS2_MACHINEGUN_L1 73
                        floatArray(32.toFloat(), (-40.toFloat()).toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_L2 74
                        floatArray(32.toFloat(), (-40.toFloat()).toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_L3 75
                        floatArray(32.toFloat(), (-40.toFloat()).toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_L4 76
                        floatArray(32.toFloat(), (-40.toFloat()).toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_L5 77
                        floatArray(32.toFloat(), (-40.toFloat()).toFloat(), 70.toFloat()).toFloat(), //		--- End Xian Stuff

                        //		MZ2_BOSS2_ROCKET_1 78
                        floatArray(22.0.toFloat(), 16.0.toFloat(), 10.0.toFloat()).toFloat(), //		MZ2_BOSS2_ROCKET_2 79
                        floatArray(22.0.toFloat(), 8.0.toFloat(), 10.0.toFloat()).toFloat(), //		MZ2_BOSS2_ROCKET_3 80
                        floatArray(22.0.toFloat(), (-8.0.toFloat()).toFloat(), 10.0.toFloat()).toFloat(), //		MZ2_BOSS2_ROCKET_4 81
                        floatArray(22.0.toFloat(), (-16.0.toFloat()).toFloat(), 10.0.toFloat()).toFloat(),
                        //		MZ2_FLOAT_BLASTER_1 82
                        floatArray(32.5.toFloat(), (-0.8.toFloat()).toFloat(), 10.toFloat()).toFloat(),
                        //		MZ2_SOLDIER_BLASTER_3 83
                        floatArray((20.8.toFloat() * 1.2.toFloat()).toFloat(), (10.1.toFloat() * 1.2.toFloat()).toFloat(), (-2.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_3 84
                        floatArray((20.8.toFloat() * 1.2.toFloat()).toFloat(), (10.1.toFloat() * 1.2.toFloat()).toFloat(), (-2.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_3 85
                        floatArray((20.8.toFloat() * 1.2.toFloat()).toFloat(), (10.1.toFloat() * 1.2.toFloat()).toFloat(), (-2.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_4 86
                        floatArray((7.6.toFloat() * 1.2.toFloat()).toFloat(), (9.3.toFloat() * 1.2.toFloat()).toFloat(), (0.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_4 87
                        floatArray((7.6.toFloat() * 1.2.toFloat()).toFloat(), (9.3.toFloat() * 1.2.toFloat()).toFloat(), (0.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_4 88
                        floatArray((7.6.toFloat() * 1.2.toFloat()).toFloat(), (9.3.toFloat() * 1.2.toFloat()).toFloat(), (0.8.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_5 89
                        floatArray((30.5.toFloat() * 1.2.toFloat()).toFloat(), (9.9.toFloat() * 1.2.toFloat()).toFloat(), (-18.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_5 90
                        floatArray((30.5.toFloat() * 1.2.toFloat()).toFloat(), (9.9.toFloat() * 1.2.toFloat()).toFloat(), (-18.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_5 91
                        floatArray((30.5.toFloat() * 1.2.toFloat()).toFloat(), (9.9.toFloat() * 1.2.toFloat()).toFloat(), (-18.7.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_6 92
                        floatArray((27.6.toFloat() * 1.2.toFloat()).toFloat(), (3.4.toFloat() * 1.2.toFloat()).toFloat(), (-10.4.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_6 93
                        floatArray((27.6.toFloat() * 1.2.toFloat()).toFloat(), (3.4.toFloat() * 1.2.toFloat()).toFloat(), (-10.4.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_6 94
                        floatArray((27.6.toFloat() * 1.2.toFloat()).toFloat(), (3.4.toFloat() * 1.2.toFloat()).toFloat(), (-10.4.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_7 95
                        floatArray((28.9.toFloat() * 1.2.toFloat()).toFloat(), (4.6.toFloat() * 1.2.toFloat()).toFloat(), (-8.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_7 96
                        floatArray((28.9.toFloat() * 1.2.toFloat()).toFloat(), (4.6.toFloat() * 1.2.toFloat()).toFloat(), (-8.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_7 97
                        floatArray((28.9.toFloat() * 1.2.toFloat()).toFloat(), (4.6.toFloat() * 1.2.toFloat()).toFloat(), (-8.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_BLASTER_8 98
                        //		 34.5f * 1.2f, 9.6f * 1.2f, 6.1f * 1.2f},
                        floatArray((31.5.toFloat() * 1.2.toFloat()).toFloat(), (9.6.toFloat() * 1.2.toFloat()).toFloat(), (10.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_SHOTGUN_8 99
                        floatArray((34.5.toFloat() * 1.2.toFloat()).toFloat(), (9.6.toFloat() * 1.2.toFloat()).toFloat(), (6.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(), //		MZ2_SOLDIER_MACHINEGUN_8 100
                        floatArray((34.5.toFloat() * 1.2.toFloat()).toFloat(), (9.6.toFloat() * 1.2.toFloat()).toFloat(), (6.1.toFloat() * 1.2.toFloat()).toFloat()).toFloat(),
                        //		--- Xian shit below ---
                        //		MZ2_MAKRON_BFG 101
                        floatArray(17.toFloat(), (-19.5.toFloat()).toFloat(), 62.9.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_1 102
                        floatArray((-3.6.toFloat()).toFloat(), (-24.1.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_2 103
                        floatArray((-1.6.toFloat()).toFloat(), (-19.3.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_3 104
                        floatArray((-0.1.toFloat()).toFloat(), (-14.4.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_4 105
                        floatArray(2.0.toFloat(), (-7.6.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_5 106
                        floatArray(3.4.toFloat(), 1.3.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_6 107
                        floatArray(3.7.toFloat(), 11.1.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_7 108
                        floatArray((-0.3.toFloat()).toFloat(), 22.3.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_8 109
                        floatArray((-6.toFloat()).toFloat(), 33.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_9 110
                        floatArray((-9.3.toFloat()).toFloat(), 36.4.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_10 111
                        floatArray((-7.toFloat()).toFloat(), 35.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_11 112
                        floatArray((-2.1.toFloat()).toFloat(), 29.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_12 113
                        floatArray(3.9.toFloat(), 17.3.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_13 114
                        floatArray(6.1.toFloat(), 5.8.toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_14 115
                        floatArray(5.9.toFloat(), (-4.4.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_15 116
                        floatArray(4.2.toFloat(), (-14.1.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_16 117
                        floatArray(2.4.toFloat(), (-18.8.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_BLASTER_17 118
                        floatArray((-1.8.toFloat()).toFloat(), (-25.5.toFloat()).toFloat(), 59.5.toFloat()).toFloat(), //		MZ2_MAKRON_RAILGUN_1 119
                        floatArray((-17.3.toFloat()).toFloat(), 7.8.toFloat(), 72.4.toFloat()).toFloat(),
                        //		MZ2_JORG_MACHINEGUN_L1 120
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_L2 121
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_L3 122
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_L4 123
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_L5 124
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_L6 125
                        floatArray(78.5.toFloat(), (-47.1.toFloat()).toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R1 126
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R2 127
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R3 128
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R4 129
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R5 130
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_MACHINEGUN_R6 131
                        floatArray(78.5.toFloat(), 46.7.toFloat(), 96.toFloat()).toFloat(), //		MZ2_JORG_BFG_1 132
                        floatArray(6.3.toFloat(), (-9.toFloat()).toFloat(), 111.2.toFloat()).toFloat(),
                        //		MZ2_BOSS2_MACHINEGUN_R1 73
                        floatArray(32.toFloat(), 40.toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_R2 74
                        floatArray(32.toFloat(), 40.toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_R3 75
                        floatArray(32.toFloat(), 40.toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_R4 76
                        floatArray(32.toFloat(), 40.toFloat(), 70.toFloat()).toFloat(), //		MZ2_BOSS2_MACHINEGUN_R5 77
                        floatArray(32.toFloat(), 40.toFloat(), 70.toFloat()).toFloat(),
                        //		--- End Xian Shit ---

                        //		ROGUE
                        //		note that the above really ends at 137
                        //		carrier machineguns
                        //		MZ2_CARRIER_MACHINEGUN_L1
                        floatArray(56.toFloat(), (-32.toFloat()).toFloat(), 32.toFloat()).toFloat(), //		MZ2_CARRIER_MACHINEGUN_R1
                        floatArray(56.toFloat(), 32.toFloat(), 32.toFloat()).toFloat(), //		MZ2_CARRIER_GRENADE
                        floatArray(42.toFloat(), 24.toFloat(), 50.toFloat()).toFloat(), //		MZ2_TURRET_MACHINEGUN 141
                        floatArray(16.toFloat(), 0.toFloat(), 0.toFloat()).toFloat(), //		MZ2_TURRET_ROCKET 142
                        floatArray(16.toFloat(), 0.toFloat(), 0.toFloat()).toFloat(), //		MZ2_TURRET_BLASTER 143
                        floatArray(16.toFloat(), 0.toFloat(), 0.toFloat()).toFloat(), //		MZ2_STALKER_BLASTER 144
                        floatArray(24.toFloat(), 0.toFloat(), 6.toFloat()).toFloat(), //		MZ2_DAEDALUS_BLASTER 145
                        floatArray(32.5.toFloat(), (-0.8.toFloat()).toFloat(), 10.0.toFloat()).toFloat(), //		MZ2_MEDIC_BLASTER_2 146
                        floatArray(12.1.toFloat(), 5.4.toFloat(), 16.5.toFloat()).toFloat(), //		MZ2_CARRIER_RAILGUN 147
                        floatArray(32.toFloat(), 0.toFloat(), 6.toFloat()).toFloat(), //		MZ2_WIDOW_DISRUPTOR 148
                        floatArray(57.72.toFloat(), 14.50.toFloat(), 88.81.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER 149
                        floatArray(56.toFloat(), 32.toFloat(), 32.toFloat()).toFloat(), //		MZ2_WIDOW_RAIL 150
                        floatArray(62.toFloat(), (-20.toFloat()).toFloat(), 84.toFloat()).toFloat(), //		MZ2_WIDOW_PLASMABEAM 151 // PMM - not used!
                        floatArray(32.toFloat(), 0.toFloat(), 6.toFloat()).toFloat(), //		MZ2_CARRIER_MACHINEGUN_L2 152
                        floatArray(61.toFloat(), (-32.toFloat()).toFloat(), 12.toFloat()).toFloat(), //		MZ2_CARRIER_MACHINEGUN_R2 153
                        floatArray(61.toFloat(), 32.toFloat(), 12.toFloat()).toFloat(), //		MZ2_WIDOW_RAIL_LEFT 154
                        floatArray(17.toFloat(), (-62.toFloat()).toFloat(), 91.toFloat()).toFloat(), //		MZ2_WIDOW_RAIL_RIGHT 155
                        floatArray(68.toFloat(), 12.toFloat(), 86.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP1 156 pmm - the sweeps need to be in
                        // sequential order
                        floatArray(47.5.toFloat(), 56.toFloat(), 89.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP2 157
                        floatArray(54.toFloat(), 52.toFloat(), 91.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP3 158
                        floatArray(58.toFloat(), 40.toFloat(), 91.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP4 159
                        floatArray(68.toFloat(), 30.toFloat(), 88.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP5 160
                        floatArray(74.toFloat(), 20.toFloat(), 88.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP6 161
                        floatArray(73.toFloat(), 11.toFloat(), 87.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP7 162
                        floatArray(73.toFloat(), 3.toFloat(), 87.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP8 163
                        floatArray(70.toFloat(), (-12.toFloat()).toFloat(), 87.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_SWEEP9 164
                        floatArray(67.toFloat(), (-20.toFloat()).toFloat(), 90.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_100 165
                        floatArray((-20.toFloat()).toFloat(), 76.toFloat(), 90.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_90 166
                        floatArray((-8.toFloat()).toFloat(), 74.toFloat(), 90.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_80 167
                        floatArray(0.toFloat(), 72.toFloat(), 90.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_70 168 d06
                        floatArray(10.toFloat(), 71.toFloat(), 89.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_60 169 d07
                        floatArray(23.toFloat(), 70.toFloat(), 87.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_50 170 d08
                        floatArray(32.toFloat(), 64.toFloat(), 85.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_40 171
                        floatArray(40.toFloat(), 58.toFloat(), 84.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_30 172 d10
                        floatArray(48.toFloat(), 50.toFloat(), 83.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_20 173
                        floatArray(54.toFloat(), 42.toFloat(), 82.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_10 174 d12
                        floatArray(56.toFloat(), 34.toFloat(), 82.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_0 175
                        floatArray(58.toFloat(), 26.toFloat(), 82.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_10L 176 d14
                        floatArray(60.toFloat(), 16.toFloat(), 82.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_20L 177
                        floatArray(59.toFloat(), 6.toFloat(), 81.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_30L 178 d16
                        floatArray(58.toFloat(), (-2.toFloat()).toFloat(), 80.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_40L 179
                        floatArray(57.toFloat(), (-10.toFloat()).toFloat(), 79.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_50L 180 d18
                        floatArray(54.toFloat(), (-18.toFloat()).toFloat(), 78.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_60L 181
                        floatArray(42.toFloat(), (-32.toFloat()).toFloat(), 80.toFloat()).toFloat(), //		MZ2_WIDOW_BLASTER_70L 182 d20
                        floatArray(36.toFloat(), (-40.toFloat()).toFloat(), 78.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_1 183
                        floatArray(68.4.toFloat(), 10.88.toFloat(), 82.08.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_2 184
                        floatArray(68.51.toFloat(), 8.64.toFloat(), 85.14.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_3 185
                        floatArray(68.66.toFloat(), 6.38.toFloat(), 88.78.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_4 186
                        floatArray(68.73.toFloat(), 5.1.toFloat(), 84.47.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_5 187
                        floatArray(68.82.toFloat(), 4.79.toFloat(), 80.52.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_6 188
                        floatArray(68.77.toFloat(), 6.11.toFloat(), 85.37.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_7 189
                        floatArray(68.67.toFloat(), 7.99.toFloat(), 90.24.toFloat()).toFloat(), //		MZ2_WIDOW_RUN_8 190
                        floatArray(68.55.toFloat(), 9.54.toFloat(), 87.36.toFloat()).toFloat(), //		MZ2_CARRIER_ROCKET_1 191
                        floatArray(0.toFloat(), 0.toFloat(), (-5.toFloat()).toFloat()).toFloat(), //		MZ2_CARRIER_ROCKET_2 192
                        floatArray(0.toFloat(), 0.toFloat(), (-5.toFloat()).toFloat()).toFloat(), //		MZ2_CARRIER_ROCKET_3 193
                        floatArray(0.toFloat(), 0.toFloat(), (-5.toFloat()).toFloat()).toFloat(), //		MZ2_CARRIER_ROCKET_4 194
                        floatArray(0.toFloat(), 0.toFloat(), (-5.toFloat()).toFloat()).toFloat(), //		MZ2_WIDOW2_BEAMER_1 195
                        //		 72.13f, -17.63f, 93.77f},
                        floatArray(69.00.toFloat(), (-17.63.toFloat()).toFloat(), 93.77.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAMER_2 196
                        //		 71.46f, -17.08f, 89.82f},
                        floatArray(69.00.toFloat(), (-17.08.toFloat()).toFloat(), 89.82.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAMER_3 197
                        //		 71.47f, -18.40f, 90.70f},
                        floatArray(69.00.toFloat(), (-18.40.toFloat()).toFloat(), 90.70.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAMER_4 198
                        //		 71.96f, -18.34f, 94.32f},
                        floatArray(69.00.toFloat(), (-18.34.toFloat()).toFloat(), 94.32.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAMER_5 199
                        //		 72.25f, -18.30f, 97.98f},
                        floatArray(69.00.toFloat(), (-18.30.toFloat()).toFloat(), 97.98.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_1 200
                        floatArray(45.04.toFloat(), (-59.02.toFloat()).toFloat(), 92.24.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_2 201
                        floatArray(50.68.toFloat(), (-54.70.toFloat()).toFloat(), 91.96.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_3 202
                        floatArray(56.57.toFloat(), (-47.72.toFloat()).toFloat(), 91.65.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_4 203
                        floatArray(61.75.toFloat(), (-38.75.toFloat()).toFloat(), 91.38.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_5 204
                        floatArray(65.55.toFloat(), (-28.76.toFloat()).toFloat(), 91.24.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_6 205
                        floatArray(67.79.toFloat(), (-18.90.toFloat()).toFloat(), 91.22.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_7 206
                        floatArray(68.60.toFloat(), (-9.52.toFloat()).toFloat(), 91.23.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_8 207
                        floatArray(68.08.toFloat(), 0.18.toFloat(), 91.32.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_9 208
                        floatArray(66.14.toFloat(), 9.79.toFloat(), 91.44.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_10 209
                        floatArray(62.77.toFloat(), 18.91.toFloat(), 91.65.toFloat()).toFloat(), //		MZ2_WIDOW2_BEAM_SWEEP_11 210
                        floatArray(58.29.toFloat(), 27.11.toFloat(), 92.00.toFloat()).toFloat(),
                        //		end of table
                        floatArray(0.0.toFloat(), 0.0.toFloat(), 0.0.toFloat()).toFloat())
        }

}