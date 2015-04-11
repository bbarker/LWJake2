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

/** Father of all GameObjects.  */

package lwjake2.game

import lwjake2.Defines
import lwjake2.client.M
import lwjake2.qcommon.Com
import lwjake2.server.SV
import lwjake2.server.SV_WORLD
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.util.StringTokenizer

public class GameBase {
    companion object {
        public var dummyplane: cplane_t = cplane_t()

        public var game: game_locals_t = game_locals_t()

        public var level: level_locals_t = level_locals_t()

        public var gi: game_import_t = game_import_t()

        public var st: spawn_temp_t = spawn_temp_t()

        public var sm_meat_index: Int = 0

        public var snd_fry: Int = 0

        public var meansOfDeath: Int = 0

        public var num_edicts: Int = 0

        public var g_edicts: Array<edict_t> = arrayOfNulls<edict_t>(Defines.MAX_EDICTS)
        {
            for (n in 0..Defines.MAX_EDICTS - 1)
                g_edicts[n] = edict_t(n)
        }

        public var deathmatch: cvar_t = cvar_t()

        public var coop: cvar_t = cvar_t()

        public var dmflags: cvar_t = cvar_t()

        public var skill: cvar_t // = new cvar_t();

        public var fraglimit: cvar_t = cvar_t()

        public var timelimit: cvar_t = cvar_t()

        public var password: cvar_t = cvar_t()

        public var spectator_password: cvar_t = cvar_t()

        public var needpass: cvar_t = cvar_t()

        public var maxclients: cvar_t = cvar_t()

        public var maxspectators: cvar_t = cvar_t()

        public var maxentities: cvar_t = cvar_t()

        public var g_select_empty: cvar_t = cvar_t()

        public var filterban: cvar_t = cvar_t()

        public var sv_maxvelocity: cvar_t = cvar_t()

        public var sv_gravity: cvar_t = cvar_t()

        public var sv_rollspeed: cvar_t = cvar_t()

        public var sv_rollangle: cvar_t = cvar_t()

        public var gun_x: cvar_t = cvar_t()

        public var gun_y: cvar_t = cvar_t()

        public var gun_z: cvar_t = cvar_t()

        public var run_pitch: cvar_t = cvar_t()

        public var run_roll: cvar_t = cvar_t()

        public var bob_up: cvar_t = cvar_t()

        public var bob_pitch: cvar_t = cvar_t()

        public var bob_roll: cvar_t = cvar_t()

        public var sv_cheats: cvar_t = cvar_t()

        public var flood_msgs: cvar_t = cvar_t()

        public var flood_persecond: cvar_t = cvar_t()

        public var flood_waitdelay: cvar_t = cvar_t()

        public var sv_maplist: cvar_t = cvar_t()

        public val STOP_EPSILON: Float = 0.1.toFloat()

        /**
         * Slide off of the impacting object returns the blocked flags (1 = floor, 2 =
         * step / wall).
         */
        public fun ClipVelocity(`in`: FloatArray, normal: FloatArray, out: FloatArray, overbounce: Float): Int {
            val backoff: Float
            var change: Float
            var i: Int
            var blocked: Int

            blocked = 0
            if (normal[2] > 0)
                blocked = blocked or 1 // floor
            if (normal[2] == 0.0.toFloat())
                blocked = blocked or 2 // step

            backoff = Math3D.DotProduct(`in`, normal) * overbounce

            run {
                i = 0
                while (i < 3) {
                    change = normal[i] * backoff
                    out[i] = `in`[i] - change
                    if (out[i] > -STOP_EPSILON && out[i] < STOP_EPSILON)
                        out[i] = 0
                    i++
                }
            }

            return blocked
        }


        /**
         * Searches all active entities for the next one that holds the matching
         * string at fieldofs (use the FOFS() macro) in the structure.

         * Searches beginning at the edict after from, or the beginning if null null
         * will be returned if the end of the list is reached.

         */

        public fun G_Find(from: EdictIterator?, eff: EdictFindFilter, s: String): EdictIterator? {
            var from = from

            if (from == null)
                from = EdictIterator(0)
            else
                from!!.i++

            while (from!!.i < num_edicts) {
                from!!.o = g_edicts[from!!.i]
                if (from!!.o.classname == null) {
                    Com.Printf("edict with classname = null" + from!!.o.index)
                }

                if (!from!!.o.inuse)
                    continue

                if (eff.matches(from!!.o, s))
                    return from
                from!!.i++
            }

            return null
        }

        // comfort version (rst)
        public fun G_FindEdict(from: EdictIterator, eff: EdictFindFilter, s: String): edict_t? {
            val ei = G_Find(from, eff, s)
            if (ei == null)
                return null
            else
                return ei!!.o
        }

        /**
         * Returns entities that have origins within a spherical area.
         */
        public fun findradius(from: EdictIterator?, org: FloatArray, rad: Float): EdictIterator? {
            var from = from
            val eorg = floatArray(0.0, 0.0, 0.0)
            var j: Int

            if (from == null)
                from = EdictIterator(0)
            else
                from!!.i++

            while (from!!.i < num_edicts) {
                from!!.o = g_edicts[from!!.i]
                if (!from!!.o.inuse)
                    continue

                if (from!!.o.solid == Defines.SOLID_NOT)
                    continue

                run {
                    j = 0
                    while (j < 3) {
                        eorg[j] = org[j] - (from!!.o.s.origin[j] + (from!!.o.mins[j] + from!!.o.maxs[j]) * 0.5.toFloat())
                        j++
                    }
                }

                if (Math3D.VectorLength(eorg) > rad)
                    continue
                return from
                from!!.i++
            }

            return null
        }

        /**
         * Searches all active entities for the next one that holds the matching
         * string at fieldofs (use the FOFS() macro) in the structure.

         * Searches beginning at the edict after from, or the beginning if null null
         * will be returned if the end of the list is reached.
         */

        public var MAXCHOICES: Int = 8

        public fun G_PickTarget(targetname: String?): edict_t? {
            var num_choices = 0
            val choice = arrayOfNulls<edict_t>(MAXCHOICES)

            if (targetname == null) {
                gi.dprintf("G_PickTarget called with null targetname\n")
                return null
            }

            var es: EdictIterator? = null

            while ((es = G_Find(es, findByTarget, targetname)) != null) {
                choice[num_choices++] = es!!.o
                if (num_choices == MAXCHOICES)
                    break
            }

            if (num_choices == 0) {
                gi.dprintf("G_PickTarget: target " + targetname + " not found\n")
                return null
            }

            return choice[Lib.rand() % num_choices]
        }

        public var VEC_UP: FloatArray = floatArray(0.0, (-1).toFloat(), 0.0)

        public var MOVEDIR_UP: FloatArray = floatArray(0.0, 0.0, 1.0)

        public var VEC_DOWN: FloatArray = floatArray(0.0, (-2).toFloat(), 0.0)

        public var MOVEDIR_DOWN: FloatArray = floatArray(0.0, 0.0, (-1).toFloat())

        public fun G_SetMovedir(angles: FloatArray, movedir: FloatArray) {
            if (Math3D.VectorEquals(angles, VEC_UP)) {
                Math3D.VectorCopy(MOVEDIR_UP, movedir)
            } else if (Math3D.VectorEquals(angles, VEC_DOWN)) {
                Math3D.VectorCopy(MOVEDIR_DOWN, movedir)
            } else {
                Math3D.AngleVectors(angles, movedir, null, null)
            }

            Math3D.VectorClear(angles)
        }

        public fun G_CopyString(`in`: String): String {
            return String(`in`)
        }

        /**
         * G_TouchTriggers
         */

        var touch = arrayOfNulls<edict_t>(Defines.MAX_EDICTS)

        public fun G_TouchTriggers(ent: edict_t) {
            var i: Int
            val num: Int
            var hit: edict_t

            // dead things don't activate triggers!
            if ((ent.client != null || (ent.svflags and Defines.SVF_MONSTER) != 0) && (ent.health <= 0))
                return

            num = gi.BoxEdicts(ent.absmin, ent.absmax, touch, Defines.MAX_EDICTS, Defines.AREA_TRIGGERS)

            // be careful, it is possible to have an entity in this
            // list removed before we get to it (killtriggered)
            run {
                i = 0
                while (i < num) {
                    hit = touch[i]

                    if (!hit.inuse)
                        continue

                    if (hit.touch == null)
                        continue

                    hit.touch.touch(hit, ent, dummyplane, null)
                    i++
                }
            }
        }

        public var pushed: Array<pushed_t> = arrayOfNulls<pushed_t>(Defines.MAX_EDICTS)
        {
            for (n in 0..Defines.MAX_EDICTS - 1)
                pushed[n] = pushed_t()
        }

        public var pushed_p: Int = 0

        public var obstacle: edict_t

        public var c_yes: Int = 0
        public var c_no: Int = 0

        public var STEPSIZE: Int = 18

        /**
         * G_RunEntity
         */
        public fun G_RunEntity(ent: edict_t) {

            if (ent.prethink != null)
                ent.prethink.think(ent)

            when (ent.movetype as Int) {
                Defines.MOVETYPE_PUSH, Defines.MOVETYPE_STOP -> SV.SV_Physics_Pusher(ent)
                Defines.MOVETYPE_NONE -> SV.SV_Physics_None(ent)
                Defines.MOVETYPE_NOCLIP -> SV.SV_Physics_Noclip(ent)
                Defines.MOVETYPE_STEP -> SV.SV_Physics_Step(ent)
                Defines.MOVETYPE_TOSS, Defines.MOVETYPE_BOUNCE, Defines.MOVETYPE_FLY, Defines.MOVETYPE_FLYMISSILE -> SV.SV_Physics_Toss(ent)
                else -> gi.error("SV_Physics: bad movetype " + ent.movetype as Int)
            }
        }

        public fun ClearBounds(mins: FloatArray, maxs: FloatArray) {
            mins[0] = mins[1] = mins[2] = 99999
            maxs[0] = maxs[1] = maxs[2] = (-99999).toFloat()
        }

        public fun AddPointToBounds(v: FloatArray, mins: FloatArray, maxs: FloatArray) {
            var i: Int
            var `val`: Float

            run {
                i = 0
                while (i < 3) {
                    `val` = v[i]
                    if (`val` < mins[i])
                        mins[i] = `val`
                    if (`val` > maxs[i])
                        maxs[i] = `val`
                    i++
                }
            }
        }

        public var findByTarget: EdictFindFilter = object : EdictFindFilter() {
            public fun matches(e: edict_t, s: String): Boolean {
                if (e.targetname == null)
                    return false
                return e.targetname.equalsIgnoreCase(s)
            }
        }

        public var findByClass: EdictFindFilter = object : EdictFindFilter() {
            public fun matches(e: edict_t, s: String): Boolean {
                return e.classname.equalsIgnoreCase(s)
            }
        }

        public fun ShutdownGame() {
            gi.dprintf("==== ShutdownGame ====\n")
        }

        /**
         * ClientEndServerFrames.
         */
        public fun ClientEndServerFrames() {
            var i: Int
            var ent: edict_t

            // calc the player views now that all pushing
            // and damage has been added
            run {
                i = 0
                while (i < maxclients.value) {
                    ent = g_edicts[1 + i]
                    if (!ent.inuse || null == ent.client)
                        continue
                    PlayerView.ClientEndServerFrame(ent)
                    i++
                }
            }

        }

        /**
         * Returns the created target changelevel.
         */
        public fun CreateTargetChangeLevel(map: String): edict_t {
            val ent: edict_t

            ent = GameUtil.G_Spawn()
            ent.classname = "target_changelevel"
            level.nextmap = map
            ent.map = level.nextmap
            return ent
        }

        /**
         * The timelimit or fraglimit has been exceeded.
         */
        public fun EndDMLevel() {
            val ent: edict_t
            //char * s, * t, * f;
            //static const char * seps = " ,\n\r";
            val s: String
            val t: String
            var f: String?
            val seps = " ,\n\r"

            // stay on same level flag
            if ((dmflags.value as Int and Defines.DF_SAME_LEVEL) != 0) {
                PlayerHud.BeginIntermission(CreateTargetChangeLevel(level.mapname))
                return
            }

            // see if it's in the map list
            if (sv_maplist.string.length() > 0) {
                s = sv_maplist.string
                f = null
                val tk = StringTokenizer(s, seps)

                while (tk.hasMoreTokens()) {
                    t = tk.nextToken()

                    // store first map
                    if (f == null)
                        f = t

                    if (t.equalsIgnoreCase(level.mapname)) {
                        // it's in the list, go to the next one
                        if (!tk.hasMoreTokens()) {
                            // end of list, go to first one
                            if (f == null)
                            // there isn't a first one, same level
                                PlayerHud.BeginIntermission(CreateTargetChangeLevel(level.mapname))
                            else
                                PlayerHud.BeginIntermission(CreateTargetChangeLevel(f))
                        } else
                            PlayerHud.BeginIntermission(CreateTargetChangeLevel(tk.nextToken()))
                        return
                    }
                }
            }

            //not in the map list
            if (level.nextmap.length() > 0)
            // go to a specific map
                PlayerHud.BeginIntermission(CreateTargetChangeLevel(level.nextmap))
            else {
                // search for a changelevel
                var edit: EdictIterator? = null
                edit = G_Find(edit, findByClass, "target_changelevel")
                if (edit == null) {
                    // the map designer didn't include a
                    // changelevel,
                    // so create a fake ent that goes back to the same level
                    PlayerHud.BeginIntermission(CreateTargetChangeLevel(level.mapname))
                    return
                }
                ent = edit!!.o
                PlayerHud.BeginIntermission(ent)
            }
        }

        /**
         * CheckNeedPass.
         */
        public fun CheckNeedPass() {
            var need: Int

            // if password or spectator_password has changed, update needpass
            // as needed
            if (password.modified || spectator_password.modified) {
                password.modified = spectator_password.modified = false

                need = 0

                if ((password.string.length() > 0) && 0 != Lib.Q_stricmp(password.string, "none"))
                    need = need or 1
                if ((spectator_password.string.length() > 0) && 0 != Lib.Q_stricmp(spectator_password.string, "none"))
                    need = need or 2

                gi.cvar_set("needpass", "" + need)
            }
        }

        /**
         * CheckDMRules.
         */
        public fun CheckDMRules() {
            var i: Int
            var cl: gclient_t

            if (level.intermissiontime != 0)
                return

            if (0 == deathmatch.value)
                return

            if (timelimit.value != 0) {
                if (level.time >= timelimit.value * 60) {
                    gi.bprintf(Defines.PRINT_HIGH, "Timelimit hit.\n")
                    EndDMLevel()
                    return
                }
            }

            if (fraglimit.value != 0) {
                run {
                    i = 0
                    while (i < maxclients.value) {
                        cl = game.clients[i]
                        if (!g_edicts[i + 1].inuse)
                            continue

                        if (cl.resp.score >= fraglimit.value) {
                            gi.bprintf(Defines.PRINT_HIGH, "Fraglimit hit.\n")
                            EndDMLevel()
                            return
                        }
                        i++
                    }
                }
            }
        }

        /**
         * Exits a level.
         */
        public fun ExitLevel() {
            var i: Int
            var ent: edict_t

            val command = "gamemap \"" + level.changemap + "\"\n"
            gi.AddCommandString(command)
            level.changemap = null
            level.exitintermission = false
            level.intermissiontime = 0
            ClientEndServerFrames()

            // clear some things before going to next level
            run {
                i = 0
                while (i < maxclients.value) {
                    ent = g_edicts[1 + i]
                    if (!ent.inuse)
                        continue
                    if (ent.health > ent.client.pers.max_health)
                        ent.health = ent.client.pers.max_health
                    i++
                }
            }
        }

        /**
         * G_RunFrame

         * Advances the world by Defines.FRAMETIME (0.1) seconds.
         */
        public fun G_RunFrame() {
            var i: Int
            var ent: edict_t

            level.framenum++
            level.time = level.framenum * Defines.FRAMETIME

            // choose a client for monsters to target this frame
            GameAI.AI_SetSightClient()

            // exit intermissions

            if (level.exitintermission) {
                ExitLevel()
                return
            }

            //
            // treat each object in turn
            // even the world gets a chance to think
            //

            run {
                i = 0
                while (i < num_edicts) {
                    ent = g_edicts[i]
                    if (!ent.inuse)
                        continue

                    level.current_entity = ent

                    Math3D.VectorCopy(ent.s.origin, ent.s.old_origin)

                    // if the ground entity moved, make sure we are still on it
                    if ((ent.groundentity != null) && (ent.groundentity.linkcount != ent.groundentity_linkcount)) {
                        ent.groundentity = null
                        if (0 == (ent.flags and (Defines.FL_SWIM or Defines.FL_FLY)) && (ent.svflags and Defines.SVF_MONSTER) != 0) {
                            M.M_CheckGround(ent)
                        }
                    }

                    if (i > 0 && i <= maxclients.value) {
                        PlayerClient.ClientBeginServerFrame(ent)
                        continue
                    }

                    G_RunEntity(ent)
                    i++
                }
            }

            // see if it is time to end a deathmatch
            CheckDMRules()

            // see if needpass needs updated
            CheckNeedPass()

            // build the playerstate_t structures for all players
            ClientEndServerFrames()
        }

        /**
         * This return a pointer to the structure with all entry points and global
         * variables.
         */

        public fun GetGameApi(imp: game_import_t) {
            gi = imp
            gi.pointcontents = object : pmove_t.PointContentsAdapter() {
                public fun pointcontents(o: FloatArray): Int {
                    return SV_WORLD.SV_PointContents(o)
                }
            }
        }
    }
}