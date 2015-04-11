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

package lwjake2.game

import lwjake2.Defines
import lwjake2.game.monsters.M_Actor
import lwjake2.game.monsters.M_Berserk
import lwjake2.game.monsters.M_Boss2
import lwjake2.game.monsters.M_Boss3
import lwjake2.game.monsters.M_Boss31
import lwjake2.game.monsters.M_Brain
import lwjake2.game.monsters.M_Chick
import lwjake2.game.monsters.M_Flipper
import lwjake2.game.monsters.M_Float
import lwjake2.game.monsters.M_Flyer
import lwjake2.game.monsters.M_Gladiator
import lwjake2.game.monsters.M_Gunner
import lwjake2.game.monsters.M_Hover
import lwjake2.game.monsters.M_Infantry
import lwjake2.game.monsters.M_Insane
import lwjake2.game.monsters.M_Medic
import lwjake2.game.monsters.M_Mutant
import lwjake2.game.monsters.M_Parasite
import lwjake2.game.monsters.M_Soldier
import lwjake2.game.monsters.M_Supertank
import lwjake2.game.monsters.M_Tank
import lwjake2.qcommon.Com
import lwjake2.util.Lib

public class GameSpawn {
    companion object {

        var SP_item_health: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_item_health"
            }

            public fun think(ent: edict_t): Boolean {
                GameItems.SP_item_health(ent)
                return true
            }
        }

        var SP_item_health_small: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_item_health_small"
            }

            public fun think(ent: edict_t): Boolean {
                GameItems.SP_item_health_small(ent)
                return true
            }
        }

        var SP_item_health_large: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_item_health_large"
            }

            public fun think(ent: edict_t): Boolean {
                GameItems.SP_item_health_large(ent)
                return true
            }
        }

        var SP_item_health_mega: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_item_health_mega"
            }

            public fun think(ent: edict_t): Boolean {
                GameItems.SP_item_health_mega(ent)
                return true
            }
        }

        var SP_info_player_start: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_player_start"
            }

            public fun think(ent: edict_t): Boolean {
                PlayerClient.SP_info_player_start(ent)
                return true
            }
        }

        var SP_info_player_deathmatch: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_player_deathmatch"
            }

            public fun think(ent: edict_t): Boolean {
                PlayerClient.SP_info_player_deathmatch(ent)
                return true
            }
        }

        var SP_info_player_coop: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_player_coop"
            }

            public fun think(ent: edict_t): Boolean {
                PlayerClient.SP_info_player_coop(ent)
                return true
            }
        }

        var SP_info_player_intermission: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_player_intermission"
            }

            public fun think(ent: edict_t): Boolean {
                PlayerClient.SP_info_player_intermission()
                return true
            }
        }

        var SP_func_plat: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_plat"
            }

            public fun think(ent: edict_t): Boolean {
                GameFunc.SP_func_plat(ent)
                return true
            }
        }


        var SP_func_water: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_water"
            }

            public fun think(ent: edict_t): Boolean {
                GameFunc.SP_func_water(ent)
                return true
            }
        }

        var SP_func_train: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_train"
            }

            public fun think(ent: edict_t): Boolean {
                GameFunc.SP_func_train(ent)
                return true
            }
        }

        var SP_func_clock: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_clock"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_func_clock(ent)
                return true
            }
        }

        /**
         * QUAKED worldspawn (0 0 0) ?

         * Only used for the world. "sky" environment map name "skyaxis" vector axis
         * for rotating sky "skyrotate" speed of rotation in degrees/second "sounds"
         * music cd track number "gravity" 800 is default gravity "message" text to
         * print at user logon
         */

        var SP_worldspawn: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_worldspawn"
            }

            public fun think(ent: edict_t): Boolean {
                ent.movetype = Defines.MOVETYPE_PUSH
                ent.solid = Defines.SOLID_BSP
                ent.inuse = true
                // since the world doesn't use G_Spawn()
                ent.s.modelindex = 1
                // world model is always index 1
                //---------------
                // reserve some spots for dead player bodies for coop / deathmatch
                PlayerClient.InitBodyQue()
                // set configstrings for items
                GameItems.SetItemNames()
                if (GameBase.st.nextmap != null)
                    GameBase.level.nextmap = GameBase.st.nextmap
                // make some data visible to the server
                if (ent.message != null && ent.message.length() > 0) {
                    GameBase.gi.configstring(Defines.CS_NAME, ent.message)
                    GameBase.level.level_name = ent.message
                } else
                    GameBase.level.level_name = GameBase.level.mapname
                if (GameBase.st.sky != null && GameBase.st.sky.length() > 0)
                    GameBase.gi.configstring(Defines.CS_SKY, GameBase.st.sky)
                else
                    GameBase.gi.configstring(Defines.CS_SKY, "unit1_")
                GameBase.gi.configstring(Defines.CS_SKYROTATE, "" + GameBase.st.skyrotate)
                GameBase.gi.configstring(Defines.CS_SKYAXIS, Lib.vtos(GameBase.st.skyaxis))
                GameBase.gi.configstring(Defines.CS_CDTRACK, "" + ent.sounds)
                GameBase.gi.configstring(Defines.CS_MAXCLIENTS, "" + (GameBase.maxclients.value) as Int)
                // status bar program
                if (GameBase.deathmatch.value != 0)
                    GameBase.gi.configstring(Defines.CS_STATUSBAR, "" + dm_statusbar)
                else
                    GameBase.gi.configstring(Defines.CS_STATUSBAR, "" + single_statusbar)
                //---------------
                // help icon for statusbar
                GameBase.gi.imageindex("i_help")
                GameBase.level.pic_health = GameBase.gi.imageindex("i_health")
                GameBase.gi.imageindex("help")
                GameBase.gi.imageindex("field_3")
                if ("".equals(GameBase.st.gravity))
                    GameBase.gi.cvar_set("sv_gravity", "800")
                else
                    GameBase.gi.cvar_set("sv_gravity", GameBase.st.gravity)
                GameBase.snd_fry = GameBase.gi.soundindex("player/fry.wav")
                // standing in lava / slime
                GameItems.PrecacheItem(GameItems.FindItem("Blaster"))
                GameBase.gi.soundindex("player/lava1.wav")
                GameBase.gi.soundindex("player/lava2.wav")
                GameBase.gi.soundindex("misc/pc_up.wav")
                GameBase.gi.soundindex("misc/talk1.wav")
                GameBase.gi.soundindex("misc/udeath.wav")
                // gibs
                GameBase.gi.soundindex("items/respawn1.wav")
                // sexed sounds
                GameBase.gi.soundindex("*death1.wav")
                GameBase.gi.soundindex("*death2.wav")
                GameBase.gi.soundindex("*death3.wav")
                GameBase.gi.soundindex("*death4.wav")
                GameBase.gi.soundindex("*fall1.wav")
                GameBase.gi.soundindex("*fall2.wav")
                GameBase.gi.soundindex("*gurp1.wav")
                // drowning damage
                GameBase.gi.soundindex("*gurp2.wav")
                GameBase.gi.soundindex("*jump1.wav")
                // player jump
                GameBase.gi.soundindex("*pain25_1.wav")
                GameBase.gi.soundindex("*pain25_2.wav")
                GameBase.gi.soundindex("*pain50_1.wav")
                GameBase.gi.soundindex("*pain50_2.wav")
                GameBase.gi.soundindex("*pain75_1.wav")
                GameBase.gi.soundindex("*pain75_2.wav")
                GameBase.gi.soundindex("*pain100_1.wav")
                GameBase.gi.soundindex("*pain100_2.wav")
                // sexed models
                // THIS ORDER MUST MATCH THE DEFINES IN g_local.h
                // you can add more, max 15
                GameBase.gi.modelindex("#w_blaster.md2")
                GameBase.gi.modelindex("#w_shotgun.md2")
                GameBase.gi.modelindex("#w_sshotgun.md2")
                GameBase.gi.modelindex("#w_machinegun.md2")
                GameBase.gi.modelindex("#w_chaingun.md2")
                GameBase.gi.modelindex("#a_grenades.md2")
                GameBase.gi.modelindex("#w_glauncher.md2")
                GameBase.gi.modelindex("#w_rlauncher.md2")
                GameBase.gi.modelindex("#w_hyperblaster.md2")
                GameBase.gi.modelindex("#w_railgun.md2")
                GameBase.gi.modelindex("#w_bfg.md2")
                //-------------------
                GameBase.gi.soundindex("player/gasp1.wav")
                // gasping for air
                GameBase.gi.soundindex("player/gasp2.wav")
                // head breaking surface, not gasping
                GameBase.gi.soundindex("player/watr_in.wav")
                // feet hitting water
                GameBase.gi.soundindex("player/watr_out.wav")
                // feet leaving water
                GameBase.gi.soundindex("player/watr_un.wav")
                // head going underwater
                GameBase.gi.soundindex("player/u_breath1.wav")
                GameBase.gi.soundindex("player/u_breath2.wav")
                GameBase.gi.soundindex("items/pkup.wav")
                // bonus item pickup
                GameBase.gi.soundindex("world/land.wav")
                // landing thud
                GameBase.gi.soundindex("misc/h2ohit1.wav")
                // landing splash
                GameBase.gi.soundindex("items/damage.wav")
                GameBase.gi.soundindex("items/protect.wav")
                GameBase.gi.soundindex("items/protect4.wav")
                GameBase.gi.soundindex("weapons/noammo.wav")
                GameBase.gi.soundindex("infantry/inflies1.wav")
                GameBase.sm_meat_index = GameBase.gi.modelindex("models/objects/gibs/sm_meat/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/arm/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/bone/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/bone2/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/chest/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/skull/tris.md2")
                GameBase.gi.modelindex("models/objects/gibs/head2/tris.md2")
                //
                // Setup light animation tables. 'a' is total darkness, 'z' is
                // doublebright.
                //
                // 0 normal
                GameBase.gi.configstring(Defines.CS_LIGHTS + 0, "m")
                // 1 FLICKER (first variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 1, "mmnmmommommnonmmonqnmmo")
                // 2 SLOW STRONG PULSE
                GameBase.gi.configstring(Defines.CS_LIGHTS + 2, "abcdefghijklmnopqrstuvwxyzyxwvutsrqponmlkjihgfedcba")
                // 3 CANDLE (first variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 3, "mmmmmaaaaammmmmaaaaaabcdefgabcdefg")
                // 4 FAST STROBE
                GameBase.gi.configstring(Defines.CS_LIGHTS + 4, "mamamamamama")
                // 5 GENTLE PULSE 1
                GameBase.gi.configstring(Defines.CS_LIGHTS + 5, "jklmnopqrstuvwxyzyxwvutsrqponmlkj")
                // 6 FLICKER (second variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 6, "nmonqnmomnmomomno")
                // 7 CANDLE (second variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 7, "mmmaaaabcdefgmmmmaaaammmaamm")
                // 8 CANDLE (third variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 8, "mmmaaammmaaammmabcdefaaaammmmabcdefmmmaaaa")
                // 9 SLOW STROBE (fourth variety)
                GameBase.gi.configstring(Defines.CS_LIGHTS + 9, "aaaaaaaazzzzzzzz")
                // 10 FLUORESCENT FLICKER
                GameBase.gi.configstring(Defines.CS_LIGHTS + 10, "mmamammmmammamamaaamammma")
                // 11 SLOW PULSE NOT FADE TO BLACK
                GameBase.gi.configstring(Defines.CS_LIGHTS + 11, "abcdefghijklmnopqrrqponmlkjihgfedcba")
                // styles 32-62 are assigned by the light program for switchable
                // lights
                // 63 testing
                GameBase.gi.configstring(Defines.CS_LIGHTS + 63, "a")
                return true
            }
        }

        /**
         * ED_NewString.
         */
        fun ED_NewString(string: String): String {

            val l = string.length()
            val newb = StringBuffer(l)

            run {
                var i = 0
                while (i < l) {
                    var c = string.charAt(i)
                    if (c == '\\' && i < l - 1) {
                        c = string.charAt(++i)
                        if (c == 'n')
                            newb.append('\n')
                        else
                            newb.append('\\')
                    } else
                        newb.append(c)
                    i++
                }
            }

            return newb.toString()
        }

        /**
         * ED_ParseField

         * Takes a key/value pair and sets the binary values in an edict.
         */
        fun ED_ParseField(key: String, value: String, ent: edict_t) {

            if (key.equals("nextmap"))
                Com.Println("nextmap: " + value)
            if (!GameBase.st.set(key, value))
                if (!ent.setField(key, value))
                    GameBase.gi.dprintf("??? The key [" + key + "] is not a field\n")

        }

        /**
         * ED_ParseEdict

         * Parses an edict out of the given string, returning the new position ed
         * should be a properly initialized empty edict.
         */

        fun ED_ParseEdict(ph: Com.ParseHelp, ent: edict_t) {

            var init: Boolean
            val keyname: String
            var com_token: String
            init = false

            GameBase.st = spawn_temp_t()
            while (true) {

                // parse key
                com_token = Com.Parse(ph)
                if (com_token.equals("}"))
                    break

                if (ph.isEof())
                    GameBase.gi.error("ED_ParseEntity: EOF without closing brace")

                keyname = com_token

                // parse value
                com_token = Com.Parse(ph)

                if (ph.isEof())
                    GameBase.gi.error("ED_ParseEntity: EOF without closing brace")

                if (com_token.equals("}"))
                    GameBase.gi.error("ED_ParseEntity: closing brace without data")

                init = true
                // keynames with a leading underscore are used for utility comments,
                // and are immediately discarded by quake
                if (keyname.charAt(0) == '_')
                    continue

                ED_ParseField(keyname.toLowerCase(), com_token, ent)

            }

            if (!init) {
                GameUtil.G_ClearEdict(ent)
            }

            return
        }

        /**
         * G_FindTeams

         * Chain together all entities with a matching team field.

         * All but the first will have the FL_TEAMSLAVE flag set. All but the last
         * will have the teamchain field set to the next one.
         */

        fun G_FindTeams() {
            var e: edict_t
            var e2: edict_t
            var chain: edict_t
            var i: Int
            var j: Int
            run {
                i = 1
                while (i < GameBase.num_edicts) {
                    e = GameBase.g_edicts[i]

                    if (!e.inuse)
                        continue
                    if (e.team == null)
                        continue
                    if ((e.flags and Defines.FL_TEAMSLAVE) != 0)
                        continue
                    chain = e
                    e.teammaster = e

                    run {
                        j = i + 1
                        while (j < GameBase.num_edicts) {
                            e2 = GameBase.g_edicts[j]
                            if (!e2.inuse)
                                continue
                            if (null == e2.team)
                                continue
                            if ((e2.flags and Defines.FL_TEAMSLAVE) != 0)
                                continue
                            if (0 == Lib.strcmp(e.team, e2.team)) {
                                chain.teamchain = e2
                                e2.teammaster = e
                                chain = e2
                                e2.flags = e2.flags or Defines.FL_TEAMSLAVE

                            }
                            j++
                        }
                    }
                    i++
                }
            }
        }

        /**
         * SpawnEntities

         * Creates a server's entity / program execution context by parsing textual
         * entity definitions out of an ent file.
         */

        public fun SpawnEntities(mapname: String, entities: String, spawnpoint: String) {

            Com.dprintln("SpawnEntities(), mapname=" + mapname)
            var ent: edict_t?
            var inhibit: Int
            val com_token: String
            var i: Int
            var skill_level: Float
            //skill.value =2.0f;
            skill_level = Math.floor(GameBase.skill.value) as Float

            if (skill_level < 0)
                skill_level = 0
            if (skill_level > 3)
                skill_level = 3
            if (GameBase.skill.value != skill_level)
                GameBase.gi.cvar_forceset("skill", "" + skill_level)

            PlayerClient.SaveClientData()

            GameBase.level = level_locals_t()
            for (n in 0..GameBase.game.maxentities - 1) {
                GameBase.g_edicts[n] = edict_t(n)
            }

            GameBase.level.mapname = mapname
            GameBase.game.spawnpoint = spawnpoint

            // set client fields on player ents
            run {
                i = 0
                while (i < GameBase.game.maxclients) {
                    GameBase.g_edicts[i + 1].client = GameBase.game.clients[i]
                    i++
                }
            }

            ent = null
            inhibit = 0

            val ph = Com.ParseHelp(entities)

            while (true) {
                // parse the opening brace

                com_token = Com.Parse(ph)
                if (ph.isEof())
                    break
                if (!com_token.startsWith("{"))
                    GameBase.gi.error("ED_LoadFromFile: found " + com_token + " when expecting {")

                if (ent == null)
                    ent = GameBase.g_edicts[0]
                else
                    ent = GameUtil.G_Spawn()

                ED_ParseEdict(ph, ent)
                Com.DPrintf("spawning ent[" + ent!!.index + "], classname=" + ent!!.classname + ", flags= " + Integer.toHexString(ent!!.spawnflags))

                // yet another map hack
                if (0 == Lib.Q_stricmp(GameBase.level.mapname, "command") && 0 == Lib.Q_stricmp(ent!!.classname, "trigger_once") && 0 == Lib.Q_stricmp(ent!!.model, "*27"))
                    ent!!.spawnflags = ent!!.spawnflags and Defines.SPAWNFLAG_NOT_HARD.inv()

                // remove things (except the world) from different skill levels or
                // deathmatch
                if (ent != GameBase.g_edicts[0]) {
                    if (GameBase.deathmatch.value != 0) {
                        if ((ent!!.spawnflags and Defines.SPAWNFLAG_NOT_DEATHMATCH) != 0) {

                            Com.DPrintf("->inhibited.\n")
                            GameUtil.G_FreeEdict(ent)
                            inhibit++
                            continue
                        }
                    } else {
                        if (/*
                         * ((coop.value) && (ent.spawnflags &
                         * SPAWNFLAG_NOT_COOP)) ||
                         */
                        ((GameBase.skill.value == 0) && (ent!!.spawnflags and Defines.SPAWNFLAG_NOT_EASY) != 0) || ((GameBase.skill.value == 1) && (ent!!.spawnflags and Defines.SPAWNFLAG_NOT_MEDIUM) != 0) || (((GameBase.skill.value == 2) || (GameBase.skill.value == 3)) && (ent!!.spawnflags and Defines.SPAWNFLAG_NOT_HARD) != 0)) {

                            Com.DPrintf("->inhibited.\n")
                            GameUtil.G_FreeEdict(ent)
                            inhibit++

                            continue
                        }
                    }

                    ent!!.spawnflags = ent!!.spawnflags and (Defines.SPAWNFLAG_NOT_EASY or Defines.SPAWNFLAG_NOT_MEDIUM or Defines.SPAWNFLAG_NOT_HARD or Defines.SPAWNFLAG_NOT_COOP or Defines.SPAWNFLAG_NOT_DEATHMATCH).inv()
                }
                ED_CallSpawn(ent)
                Com.DPrintf("\n")
            }
            Com.DPrintf("player skill level:" + GameBase.skill.value + "\n")
            Com.DPrintf(inhibit + " entities inhibited.\n")
            i = 1
            G_FindTeams()
            PlayerTrail.Init()
        }

        var single_statusbar = "yb	-24 " //	   health
        + "xv	0 " + "hnum " + "xv	50 " + "pic 0 " //	   ammo
        + "if 2 " + "	xv	100 " + "	anum " + "	xv	150 " + "	pic 2 " + "endif " //	   armor
        + "if 4 " + "	xv	200 " + "	rnum " + "	xv	250 " + "	pic 4 " + "endif " //	   selected item
        + "if 6 " + "	xv	296 " + "	pic 6 " + "endif " + "yb	-50 " //	   picked
        + "if 7 " + "	xv	0 " + "	pic 7 " + "	xv	26 " + "	yb	-42 " + "	stat_string 8 " + "	yb	-50 " + "endif " + "if 9 " + "	xv	262 " + "	num	2	10 " + "	xv	296 " + "	pic	9 " + "endif " + "if 11 " + "	xv	148 " + "	pic	11 " + "endif "// up
        // item
        //	   timer
        //		help / weapon icon

        var dm_statusbar = "yb	-24 " //	   health
        + "xv	0 " + "hnum " + "xv	50 " + "pic 0 " //	   ammo
        + "if 2 " + "	xv	100 " + "	anum " + "	xv	150 " + "	pic 2 " + "endif " //	   armor
        + "if 4 " + "	xv	200 " + "	rnum " + "	xv	250 " + "	pic 4 " + "endif " //	   selected item
        + "if 6 " + "	xv	296 " + "	pic 6 " + "endif " + "yb	-50 " //	   picked
        + "if 7 " + "	xv	0 " + "	pic 7 " + "	xv	26 " + "	yb	-42 " + "	stat_string 8 " + "	yb	-50 " + "endif " + "if 9 " + "	xv	246 " + "	num	2	10 " + "	xv	296 " + "	pic	9 " + "endif " + "if 11 " + "	xv	148 " + "	pic	11 " + "endif " //		frags
        + "xr	-50 " + "yt 2 " + "num 3 14 " //	   spectator
        + "if 17 " + "xv 0 " + "yb -58 " + "string2 \"SPECTATOR MODE\" " + "endif " //	   chase camera
        + "if 16 " + "xv 0 " + "yb -68 " + "string \"Chasing\" " + "xv 64 " + "stat_string 16 " + "endif "// up
        // item
        //	   timer
        //		help / weapon icon

        var spawns = array<spawn_t>(spawn_t("item_health", SP_item_health), spawn_t("item_health_small", SP_item_health_small), spawn_t("item_health_large", SP_item_health_large), spawn_t("item_health_mega", SP_item_health_mega), spawn_t("info_player_start", SP_info_player_start), spawn_t("info_player_deathmatch", SP_info_player_deathmatch), spawn_t("info_player_coop", SP_info_player_coop), spawn_t("info_player_intermission", SP_info_player_intermission), spawn_t("func_plat", SP_func_plat), spawn_t("func_button", GameFunc.SP_func_button), spawn_t("func_door", GameFunc.SP_func_door), spawn_t("func_door_secret", GameFunc.SP_func_door_secret), spawn_t("func_door_rotating", GameFunc.SP_func_door_rotating), spawn_t("func_rotating", GameFunc.SP_func_rotating), spawn_t("func_train", SP_func_train), spawn_t("func_water", SP_func_water), spawn_t("func_conveyor", GameFunc.SP_func_conveyor), spawn_t("func_areaportal", GameMisc.SP_func_areaportal), spawn_t("func_clock", SP_func_clock), spawn_t("func_wall", object : EntThinkAdapter() {
            public fun getID(): String {
                return "func_wall"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_func_wall(ent)
                return true
            }
        }), spawn_t("func_object", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_object"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_func_object(ent)
                return true
            }
        }), spawn_t("func_timer", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_timer"
            }

            public fun think(ent: edict_t): Boolean {
                GameFunc.SP_func_timer(ent)
                return true
            }
        }), spawn_t("func_explosive", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_func_explosive"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_func_explosive(ent)
                return true
            }
        }), spawn_t("func_killbox", GameFunc.SP_func_killbox), spawn_t("trigger_always", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_always"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_always(ent)
                return true
            }
        }), spawn_t("trigger_once", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_once"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_once(ent)
                return true
            }
        }), spawn_t("trigger_multiple", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_multiple"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_multiple(ent)
                return true
            }
        }), spawn_t("trigger_relay", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_relay"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_relay(ent)
                return true
            }
        }), spawn_t("trigger_push", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_push"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_push(ent)
                return true
            }
        }), spawn_t("trigger_hurt", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_hurt"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_hurt(ent)
                return true
            }
        }), spawn_t("trigger_key", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_key"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_key(ent)
                return true
            }
        }), spawn_t("trigger_counter", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_counter"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_counter(ent)
                return true
            }
        }), spawn_t("trigger_elevator", GameFunc.SP_trigger_elevator), spawn_t("trigger_gravity", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_gravity"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_gravity(ent)
                return true
            }
        }), spawn_t("trigger_monsterjump", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_trigger_monsterjump"
            }

            public fun think(ent: edict_t): Boolean {
                GameTrigger.SP_trigger_monsterjump(ent)
                return true
            }
        }), spawn_t("target_temp_entity", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_temp_entity"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_temp_entity(ent)
                return true
            }
        }), spawn_t("target_speaker", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_speaker"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_speaker(ent)
                return true
            }
        }), spawn_t("target_explosion", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_explosion"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_explosion(ent)
                return true
            }
        }), spawn_t("target_changelevel", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_changelevel"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_changelevel(ent)
                return true
            }
        }), spawn_t("target_secret", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_secret"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_secret(ent)
                return true
            }
        }), spawn_t("target_goal", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_goal"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_goal(ent)
                return true
            }
        }), spawn_t("target_splash", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_splash"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_splash(ent)
                return true
            }
        }), spawn_t("target_spawner", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_spawner"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_spawner(ent)
                return true
            }
        }), spawn_t("target_blaster", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_blaster"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_blaster(ent)
                return true
            }
        }), spawn_t("target_crosslevel_trigger", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_crosslevel_trigger"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_crosslevel_trigger(ent)
                return true
            }
        }), spawn_t("target_crosslevel_target", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_crosslevel_target"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_crosslevel_target(ent)
                return true
            }
        }), spawn_t("target_laser", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_laser"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_laser(ent)
                return true
            }
        }), spawn_t("target_help", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_help"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_help(ent)
                return true
            }
        }), spawn_t("target_actor", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_actor"
            }

            public fun think(ent: edict_t): Boolean {
                M_Actor.SP_target_actor(ent)
                return true
            }
        }), spawn_t("target_lightramp", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_lightramp"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_lightramp(ent)
                return true
            }
        }), spawn_t("target_earthquake", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_earthquake"
            }

            public fun think(ent: edict_t): Boolean {
                GameTarget.SP_target_earthquake(ent)
                return true
            }
        }), spawn_t("target_character", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_character"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_target_character(ent)
                return true
            }
        }), spawn_t("target_string", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_target_string"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_target_string(ent)
                return true
            }
        }), spawn_t("worldspawn", SP_worldspawn), spawn_t("viewthing", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_viewthing"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_viewthing(ent)
                return true
            }
        }), spawn_t("light", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_light"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_light(ent)
                return true
            }
        }), spawn_t("light_mine1", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_light_mine1"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_light_mine1(ent)
                return true
            }
        }), spawn_t("light_mine2", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_light_mine2"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_light_mine2(ent)
                return true
            }
        }), spawn_t("info_null", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_null"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_info_null(ent)
                return true
            }
        }), spawn_t("func_group", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_info_null"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_info_null(ent)
                return true
            }
        }), spawn_t("info_notnull", object : EntThinkAdapter() {
            public fun getID(): String {
                return "info_notnull"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_info_notnull(ent)
                return true
            }
        }), spawn_t("path_corner", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_path_corner"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_path_corner(ent)
                return true
            }
        }), spawn_t("point_combat", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_point_combat"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_point_combat(ent)
                return true
            }
        }), spawn_t("misc_explobox", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_explobox"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_explobox(ent)
                return true
            }
        }), spawn_t("misc_banner", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_banner"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_banner(ent)
                return true
            }
        }), spawn_t("misc_satellite_dish", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_satellite_dish"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_satellite_dish(ent)
                return true
            }
        }), spawn_t("misc_actor", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_actor"
            }

            public fun think(ent: edict_t): Boolean {
                M_Actor.SP_misc_actor(ent)
                return false
            }
        }), spawn_t("misc_gib_arm", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_gib_arm"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_gib_arm(ent)
                return true
            }
        }), spawn_t("misc_gib_leg", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_gib_leg"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_gib_leg(ent)
                return true
            }
        }), spawn_t("misc_gib_head", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_gib_head"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_gib_head(ent)
                return true
            }
        }), spawn_t("misc_insane", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_insane"
            }

            public fun think(ent: edict_t): Boolean {
                M_Insane.SP_misc_insane(ent)
                return true
            }
        }), spawn_t("misc_deadsoldier", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_deadsoldier"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_deadsoldier(ent)
                return true
            }
        }), spawn_t("misc_viper", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_viper"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_viper(ent)
                return true
            }
        }), spawn_t("misc_viper_bomb", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_viper_bomb"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_viper_bomb(ent)
                return true
            }
        }), spawn_t("misc_bigviper", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_bigviper"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_bigviper(ent)
                return true
            }
        }), spawn_t("misc_strogg_ship", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_strogg_ship"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_strogg_ship(ent)
                return true
            }
        }), spawn_t("misc_teleporter", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_teleporter"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_teleporter(ent)
                return true
            }
        }), spawn_t("misc_teleporter_dest", GameMisc.SP_misc_teleporter_dest), spawn_t("misc_blackhole", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_blackhole"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_blackhole(ent)
                return true
            }
        }), spawn_t("misc_eastertank", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_eastertank"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_eastertank(ent)
                return true
            }
        }), spawn_t("misc_easterchick", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_easterchick"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_easterchick(ent)
                return true
            }
        }), spawn_t("misc_easterchick2", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_misc_easterchick2"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_misc_easterchick2(ent)
                return true
            }
        }), spawn_t("monster_berserk", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_berserk"
            }

            public fun think(ent: edict_t): Boolean {
                M_Berserk.SP_monster_berserk(ent)
                return true
            }
        }), spawn_t("monster_gladiator", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_gladiator"
            }

            public fun think(ent: edict_t): Boolean {
                M_Gladiator.SP_monster_gladiator(ent)
                return true
            }
        }), spawn_t("monster_gunner", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_gunner"
            }

            public fun think(ent: edict_t): Boolean {
                M_Gunner.SP_monster_gunner(ent)
                return true
            }
        }), spawn_t("monster_infantry", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_infantry"
            }

            public fun think(ent: edict_t): Boolean {
                M_Infantry.SP_monster_infantry(ent)
                return true
            }
        }), spawn_t("monster_soldier_light", M_Soldier.SP_monster_soldier_light), spawn_t("monster_soldier", M_Soldier.SP_monster_soldier), spawn_t("monster_soldier_ss", M_Soldier.SP_monster_soldier_ss), spawn_t("monster_tank", M_Tank.SP_monster_tank), spawn_t("monster_tank_commander", M_Tank.SP_monster_tank), spawn_t("monster_medic", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_medic"
            }

            public fun think(ent: edict_t): Boolean {
                M_Medic.SP_monster_medic(ent)
                return true
            }
        }), spawn_t("monster_flipper", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_flipper"
            }

            public fun think(ent: edict_t): Boolean {
                M_Flipper.SP_monster_flipper(ent)
                return true
            }
        }), spawn_t("monster_chick", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_chick"
            }

            public fun think(ent: edict_t): Boolean {
                M_Chick.SP_monster_chick(ent)
                return true
            }
        }), spawn_t("monster_parasite", M_Parasite.SP_monster_parasite), spawn_t("monster_flyer", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_flyer"
            }

            public fun think(ent: edict_t): Boolean {
                M_Flyer.SP_monster_flyer(ent)
                return true
            }
        }), spawn_t("monster_brain", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_brain"
            }

            public fun think(ent: edict_t): Boolean {
                M_Brain.SP_monster_brain(ent)
                return true
            }
        }), spawn_t("monster_floater", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_floater"
            }

            public fun think(ent: edict_t): Boolean {
                M_Float.SP_monster_floater(ent)
                return true
            }
        }), spawn_t("monster_hover", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_hover"
            }

            public fun think(ent: edict_t): Boolean {
                M_Hover.SP_monster_hover(ent)
                return true
            }
        }), spawn_t("monster_mutant", M_Mutant.SP_monster_mutant), spawn_t("monster_supertank", M_Supertank.SP_monster_supertank), spawn_t("monster_boss2", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_boss2"
            }

            public fun think(ent: edict_t): Boolean {
                M_Boss2.SP_monster_boss2(ent)
                return true
            }
        }), spawn_t("monster_boss3_stand", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_boss3_stand"
            }

            public fun think(ent: edict_t): Boolean {
                M_Boss3.SP_monster_boss3_stand(ent)
                return true
            }
        }), spawn_t("monster_jorg", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_jorg"
            }

            public fun think(ent: edict_t): Boolean {
                M_Boss31.SP_monster_jorg(ent)
                return true
            }
        }), spawn_t("monster_commander_body", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_monster_commander_body"
            }

            public fun think(ent: edict_t): Boolean {
                GameMisc.SP_monster_commander_body(ent)
                return true
            }
        }), spawn_t("turret_breach", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_turret_breach"
            }

            public fun think(ent: edict_t): Boolean {
                GameTurret.SP_turret_breach(ent)
                return true
            }
        }), spawn_t("turret_base", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_turret_base"
            }

            public fun think(ent: edict_t): Boolean {
                GameTurret.SP_turret_base(ent)
                return true
            }
        }), spawn_t("turret_driver", object : EntThinkAdapter() {
            public fun getID(): String {
                return "SP_turret_driver"
            }

            public fun think(ent: edict_t): Boolean {
                GameTurret.SP_turret_driver(ent)
                return true
            }
        }), spawn_t(null, null))

        /**
         * ED_CallSpawn

         * Finds the spawn function for the entity and calls it.
         */
        public fun ED_CallSpawn(ent: edict_t) {

            var s: spawn_t
            var item: gitem_t?
            var i: Int
            if (null == ent.classname) {
                GameBase.gi.dprintf("ED_CallSpawn: null classname\n")
                return
            } // check item spawn functions
            run {
                i = 1
                while (i < GameBase.game.num_items) {

                    item = GameItemList.itemlist[i]

                    if (item == null)
                        GameBase.gi.error("ED_CallSpawn: null item in pos " + i)

                    if (item!!.classname == null)
                        continue
                    if (item!!.classname.equalsIgnoreCase(ent.classname)) {
                        // found it
                        GameItems.SpawnItem(ent, item)
                        return
                    }
                    i++
                }
            } // check normal spawn functions

            run {
                i = 0
                while ((s = spawns[i]) != null && s.name != null) {
                    if (s.name.equalsIgnoreCase(ent.classname)) {
                        // found it

                        if (s.spawn == null)
                            GameBase.gi.error("ED_CallSpawn: null-spawn on index=" + i)
                        s.spawn.think(ent)
                        return
                    }
                    i++
                }
            }
            GameBase.gi.dprintf(ent.classname + " doesn't have a spawn function\n")
        }
    }
}