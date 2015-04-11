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

package lwjake2.client

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.player_state_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.MSG
import lwjake2.render.model_t
import lwjake2.sound.S
import lwjake2.sound.sfx_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

/**
 * CL_tent
 */
public class CL_tent {

    class explosion_t {
        var type: Int = 0

        var ent = entity_t()

        var frames: Int = 0

        var light: Float = 0.toFloat()

        var lightcolor = FloatArray(3)

        var start: Float = 0.toFloat()

        var baseframe: Int = 0

        fun clear() {
            lightcolor[0] = lightcolor[1] = lightcolor[2] = light = start = (type = frames = baseframe = 0).toFloat()
            ent.clear()
        }
    }

    class beam_t {
        var entity: Int = 0

        var dest_entity: Int = 0

        var model: model_t? = null

        var endtime: Int = 0

        var offset = FloatArray(3)

        var start = FloatArray(3)

        var end = FloatArray(3)

        fun clear() {
            offset[0] = offset[1] = offset[2] = start[0] = start[1] = start[2] = end[0] = end[1] = end[2] = (entity = dest_entity = endtime = 0).toFloat()
            model = null
        }
    }

    class laser_t {
        var ent = entity_t()

        var endtime: Int = 0

        fun clear() {
            endtime = 0
            ent.clear()
        }
    }

    companion object {

        val MAX_EXPLOSIONS = 32

        var cl_explosions = arrayOfNulls<explosion_t>(MAX_EXPLOSIONS)

        val MAX_BEAMS = 32

        var cl_beams = arrayOfNulls<beam_t>(MAX_BEAMS)

        //	  PMM - added this for player-linked beams. Currently only used by the
        // plasma beam
        var cl_playerbeams = arrayOfNulls<beam_t>(MAX_BEAMS)

        val MAX_LASERS = 32

        var cl_lasers = arrayOfNulls<laser_t>(MAX_LASERS)

        //	  ROGUE
        val MAX_SUSTAINS = 32

        var cl_sustains = arrayOfNulls<cl_sustain_t>(MAX_SUSTAINS)

        {
            for (i in cl_explosions.indices)
                cl_explosions[i] = explosion_t()
        }
        {
            for (i in cl_beams.indices)
                cl_beams[i] = beam_t()
            for (i in cl_playerbeams.indices)
                cl_playerbeams[i] = beam_t()
        }

        {
            for (i in cl_lasers.indices)
                cl_lasers[i] = laser_t()
        }

        {
            for (i in cl_sustains.indices)
                cl_sustains[i] = cl_sustain_t()
        }

        val ex_free = 0

        val ex_explosion = 1

        val ex_misc = 2

        val ex_flash = 3

        val ex_mflash = 4

        val ex_poly = 5

        val ex_poly2 = 6

        //	  ROGUE

        // all are references;
        var cl_sfx_ric1: sfx_t

        var cl_sfx_ric2: sfx_t

        var cl_sfx_ric3: sfx_t

        var cl_sfx_lashit: sfx_t

        var cl_sfx_spark5: sfx_t

        var cl_sfx_spark6: sfx_t

        var cl_sfx_spark7: sfx_t

        var cl_sfx_railg: sfx_t

        var cl_sfx_rockexp: sfx_t

        var cl_sfx_grenexp: sfx_t

        var cl_sfx_watrexp: sfx_t

        // RAFAEL
        var cl_sfx_plasexp: sfx_t

        var cl_sfx_footsteps = arrayOfNulls<sfx_t>(4)

        var cl_mod_explode: model_t

        var cl_mod_smoke: model_t

        var cl_mod_flash: model_t

        var cl_mod_parasite_segment: model_t

        var cl_mod_grapple_cable: model_t

        var cl_mod_parasite_tip: model_t

        var cl_mod_explo4: model_t

        var cl_mod_bfg_explo: model_t

        var cl_mod_powerscreen: model_t

        //	   RAFAEL
        var cl_mod_plasmaexplo: model_t

        //	  ROGUE
        var cl_sfx_lightning: sfx_t

        var cl_sfx_disrexp: sfx_t

        var cl_mod_lightning: model_t

        var cl_mod_heatbeam: model_t? = null

        var cl_mod_monster_heatbeam: model_t

        var cl_mod_explo4_big: model_t

        //	  ROGUE
        /*
     * ================= CL_RegisterTEntSounds =================
     */
        fun RegisterTEntSounds() {
            var i: Int
            var name: String

            // PMM - version stuff
            //		Com_Printf ("%s\n", ROGUE_VERSION_STRING);
            // PMM
            cl_sfx_ric1 = S.RegisterSound("world/ric1.wav")
            cl_sfx_ric2 = S.RegisterSound("world/ric2.wav")
            cl_sfx_ric3 = S.RegisterSound("world/ric3.wav")
            cl_sfx_lashit = S.RegisterSound("weapons/lashit.wav")
            cl_sfx_spark5 = S.RegisterSound("world/spark5.wav")
            cl_sfx_spark6 = S.RegisterSound("world/spark6.wav")
            cl_sfx_spark7 = S.RegisterSound("world/spark7.wav")
            cl_sfx_railg = S.RegisterSound("weapons/railgf1a.wav")
            cl_sfx_rockexp = S.RegisterSound("weapons/rocklx1a.wav")
            cl_sfx_grenexp = S.RegisterSound("weapons/grenlx1a.wav")
            cl_sfx_watrexp = S.RegisterSound("weapons/xpld_wat.wav")
            // RAFAEL
            // cl_sfx_plasexp = S.RegisterSound ("weapons/plasexpl.wav");
            S.RegisterSound("player/land1.wav")

            S.RegisterSound("player/fall2.wav")
            S.RegisterSound("player/fall1.wav")

            run {
                i = 0
                while (i < 4) {
                    //Com_sprintf (name, sizeof(name), "player/step%i.wav", i+1);
                    name = "player/step" + (i + 1) + ".wav"
                    cl_sfx_footsteps[i] = S.RegisterSound(name)
                    i++
                }
            }

            //	  PGM
            cl_sfx_lightning = S.RegisterSound("weapons/tesla.wav")
            cl_sfx_disrexp = S.RegisterSound("weapons/disrupthit.wav")
            // version stuff
            //		sprintf (name, "weapons/sound%d.wav", ROGUE_VERSION_ID);
            //		if (name[0] == 'w')
            //			name[0] = 'W';
            //	  PGM
        }

        /*
     * ================= CL_RegisterTEntModels =================
     */
        fun RegisterTEntModels() {
            cl_mod_explode = Globals.re.RegisterModel("models/objects/explode/tris.md2")
            cl_mod_smoke = Globals.re.RegisterModel("models/objects/smoke/tris.md2")
            cl_mod_flash = Globals.re.RegisterModel("models/objects/flash/tris.md2")
            cl_mod_parasite_segment = Globals.re.RegisterModel("models/monsters/parasite/segment/tris.md2")
            cl_mod_grapple_cable = Globals.re.RegisterModel("models/ctf/segment/tris.md2")
            cl_mod_parasite_tip = Globals.re.RegisterModel("models/monsters/parasite/tip/tris.md2")
            cl_mod_explo4 = Globals.re.RegisterModel("models/objects/r_explode/tris.md2")
            cl_mod_bfg_explo = Globals.re.RegisterModel("sprites/s_bfg2.sp2")
            cl_mod_powerscreen = Globals.re.RegisterModel("models/items/armor/effect/tris.md2")

            Globals.re.RegisterModel("models/objects/laser/tris.md2")
            Globals.re.RegisterModel("models/objects/grenade2/tris.md2")
            Globals.re.RegisterModel("models/weapons/v_machn/tris.md2")
            Globals.re.RegisterModel("models/weapons/v_handgr/tris.md2")
            Globals.re.RegisterModel("models/weapons/v_shotg2/tris.md2")
            Globals.re.RegisterModel("models/objects/gibs/bone/tris.md2")
            Globals.re.RegisterModel("models/objects/gibs/sm_meat/tris.md2")
            Globals.re.RegisterModel("models/objects/gibs/bone2/tris.md2")
            //	   RAFAEL
            //	   re.RegisterModel ("models/objects/blaser/tris.md2");

            Globals.re.RegisterPic("w_machinegun")
            Globals.re.RegisterPic("a_bullets")
            Globals.re.RegisterPic("i_health")
            Globals.re.RegisterPic("a_grenades")

            //	  ROGUE
            cl_mod_explo4_big = Globals.re.RegisterModel("models/objects/r_explode2/tris.md2")
            cl_mod_lightning = Globals.re.RegisterModel("models/proj/lightning/tris.md2")
            cl_mod_heatbeam = Globals.re.RegisterModel("models/proj/beam/tris.md2")
            cl_mod_monster_heatbeam = Globals.re.RegisterModel("models/proj/widowbeam/tris.md2")
            //	  ROGUE
        }

        /*
     * ================= CL_ClearTEnts =================
     */
        fun ClearTEnts() {
            //		memset (cl_beams, 0, sizeof(cl_beams));
            for (i in cl_beams.indices)
                cl_beams[i].clear()
            //		memset (cl_explosions, 0, sizeof(cl_explosions));
            for (i in cl_explosions.indices)
                cl_explosions[i].clear()
            //		memset (cl_lasers, 0, sizeof(cl_lasers));
            for (i in cl_lasers.indices)
                cl_lasers[i].clear()
            //
            //	  ROGUE
            //		memset (cl_playerbeams, 0, sizeof(cl_playerbeams));
            for (i in cl_playerbeams.indices)
                cl_playerbeams[i].clear()
            //		memset (cl_sustains, 0, sizeof(cl_sustains));
            for (i in cl_sustains.indices)
                cl_sustains[i].clear()
            //	  ROGUE
        }

        /*
     * ================= CL_AllocExplosion =================
     */
        fun AllocExplosion(): explosion_t {
            var i: Int
            var time: Int
            var index: Int

            run {
                i = 0
                while (i < MAX_EXPLOSIONS) {
                    if (cl_explosions[i].type == ex_free) {
                        //memset (&cl_explosions[i], 0, sizeof (cl_explosions[i]));
                        cl_explosions[i].clear()
                        return cl_explosions[i]
                    }
                    i++
                }
            }
            //	   find the oldest explosion
            time = Globals.cl.time
            index = 0

            run {
                i = 0
                while (i < MAX_EXPLOSIONS) {
                    if (cl_explosions[i].start < time) {
                        time = cl_explosions[i].start.toInt()
                        index = i
                    }
                    i++
                }
            }
            //memset (&cl_explosions[index], 0, sizeof (cl_explosions[index]));
            cl_explosions[index].clear()
            return cl_explosions[index]
        }

        /*
     * ================= CL_SmokeAndFlash =================
     */
        fun SmokeAndFlash(origin: FloatArray) {
            var ex: explosion_t

            ex = AllocExplosion()
            Math3D.VectorCopy(origin, ex.ent.origin)
            ex.type = ex_misc
            ex.frames = 4
            ex.ent.flags = Defines.RF_TRANSLUCENT
            ex.start = Globals.cl.frame.servertime - 100
            ex.ent.model = cl_mod_smoke

            ex = AllocExplosion()
            Math3D.VectorCopy(origin, ex.ent.origin)
            ex.type = ex_flash
            ex.ent.flags = Defines.RF_FULLBRIGHT
            ex.frames = 2
            ex.start = Globals.cl.frame.servertime - 100
            ex.ent.model = cl_mod_flash
        }

        /*
     * =================
     * CL_ParseBeam
     * =================
     */
        fun ParseBeam(model: model_t): Int {
            val ent: Int
            val start = FloatArray(3)
            val end = FloatArray(3)
            var b: Array<beam_t>
            var i: Int

            ent = MSG.ReadShort(Globals.net_message)

            MSG.ReadPos(Globals.net_message, start)
            MSG.ReadPos(Globals.net_message, end)

            //	   override any beam with the same entity
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].entity == ent) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorClear(b[i].offset)
                        return ent
                    }
                    i++
                }
            }

            //	   find a free beam
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].model == null || b[i].endtime < Globals.cl.time) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorClear(b[i].offset)
                        return ent
                    }
                    i++
                }
            }
            Com.Printf("beam list overflow!\n")
            return ent
        }

        /*
     * ================= CL_ParseBeam2 =================
     */
        fun ParseBeam2(model: model_t): Int {
            val ent: Int
            val start = FloatArray(3)
            val end = FloatArray(3)
            val offset = FloatArray(3)
            var b: Array<beam_t>
            var i: Int

            ent = MSG.ReadShort(Globals.net_message)

            MSG.ReadPos(Globals.net_message, start)
            MSG.ReadPos(Globals.net_message, end)
            MSG.ReadPos(Globals.net_message, offset)

            //		Com_Printf ("end- %f %f %f\n", end[0], end[1], end[2]);

            //	   override any beam with the same entity
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].entity == ent) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorCopy(offset, b[i].offset)
                        return ent
                    }
                    i++
                }
            }

            //	   find a free beam
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].model == null || b[i].endtime < Globals.cl.time) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorCopy(offset, b[i].offset)
                        return ent
                    }
                    i++
                }
            }
            Com.Printf("beam list overflow!\n")
            return ent
        }

        //	   ROGUE
        /*
     * ================= CL_ParsePlayerBeam - adds to the cl_playerbeam array
     * instead of the cl_beams array =================
     */
        fun ParsePlayerBeam(model: model_t): Int {
            var model = model
            val ent: Int
            val start = FloatArray(3)
            val end = FloatArray(3)
            val offset = FloatArray(3)
            var b: Array<beam_t>
            var i: Int

            ent = MSG.ReadShort(Globals.net_message)

            MSG.ReadPos(Globals.net_message, start)
            MSG.ReadPos(Globals.net_message, end)
            // PMM - network optimization
            if (model == cl_mod_heatbeam)
                Math3D.VectorSet(offset, 2, 7, -3)
            else if (model == cl_mod_monster_heatbeam) {
                model = cl_mod_heatbeam
                Math3D.VectorSet(offset, 0, 0, 0)
            } else
                MSG.ReadPos(Globals.net_message, offset)

            //		Com_Printf ("end- %f %f %f\n", end[0], end[1], end[2]);

            //	   override any beam with the same entity
            //	   PMM - For player beams, we only want one per player (entity) so..
            b = cl_playerbeams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].entity == ent) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorCopy(offset, b[i].offset)
                        return ent
                    }
                    i++
                }
            }

            //	   find a free beam
            b = cl_playerbeams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].model == null || b[i].endtime < Globals.cl.time) {
                        b[i].entity = ent
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 100 // PMM - this needs to be
                        // 100 to prevent multiple
                        // heatbeams
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorCopy(offset, b[i].offset)
                        return ent
                    }
                    i++
                }
            }
            Com.Printf("beam list overflow!\n")
            return ent
        }

        //	  rogue

        // stack variable
        private val start = FloatArray(3)
        private val end = FloatArray(3)
        /*
     * ================= CL_ParseLightning =================
     */
        fun ParseLightning(model: model_t): Int {
            val srcEnt: Int
            val destEnt: Int
            var b: Array<beam_t>
            var i: Int

            srcEnt = MSG.ReadShort(Globals.net_message)
            destEnt = MSG.ReadShort(Globals.net_message)

            MSG.ReadPos(Globals.net_message, start)
            MSG.ReadPos(Globals.net_message, end)

            //	   override any beam with the same source AND destination entities
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].entity == srcEnt && b[i].dest_entity == destEnt) {
                        //				Com_Printf("%d: OVERRIDE %d . %d\n", cl.time, srcEnt,
                        // destEnt);
                        b[i].entity = srcEnt
                        b[i].dest_entity = destEnt
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorClear(b[i].offset)
                        return srcEnt
                    }
                    i++
                }
            }

            //	   find a free beam
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].model == null || b[i].endtime < Globals.cl.time) {
                        //				Com_Printf("%d: NORMAL %d . %d\n", cl.time, srcEnt, destEnt);
                        b[i].entity = srcEnt
                        b[i].dest_entity = destEnt
                        b[i].model = model
                        b[i].endtime = Globals.cl.time + 200
                        Math3D.VectorCopy(start, b[i].start)
                        Math3D.VectorCopy(end, b[i].end)
                        Math3D.VectorClear(b[i].offset)
                        return srcEnt
                    }
                    i++
                }
            }
            Com.Printf("beam list overflow!\n")
            return srcEnt
        }

        // stack variable
        // start, end
        /*
     * ================= CL_ParseLaser =================
     */
        fun ParseLaser(colors: Int) {
            val l: Array<laser_t>
            var i: Int

            MSG.ReadPos(Globals.net_message, start)
            MSG.ReadPos(Globals.net_message, end)

            l = cl_lasers
            run {
                i = 0
                while (i < MAX_LASERS) {
                    if (l[i].endtime < Globals.cl.time) {
                        l[i].ent.flags = Defines.RF_TRANSLUCENT or Defines.RF_BEAM
                        Math3D.VectorCopy(start, l[i].ent.origin)
                        Math3D.VectorCopy(end, l[i].ent.oldorigin)
                        l[i].ent.alpha = 0.30.toFloat()
                        l[i].ent.skinnum = (colors shr ((Lib.rand() % 4) * 8)) and 255
                        l[i].ent.model = null
                        l[i].ent.frame = 4
                        l[i].endtime = Globals.cl.time + 100
                        return
                    }
                    i++
                }
            }
        }

        // stack variable
        private val pos = FloatArray(3)
        private val dir = FloatArray(3)
        //	  =============
        //	  ROGUE
        fun ParseSteam() {
            val id: Int
            var i: Int
            val r: Int
            val cnt: Int
            val color: Int
            var magnitude: Int
            val s: Array<cl_sustain_t>
            var free_sustain: cl_sustain_t?

            id = MSG.ReadShort(Globals.net_message) // an id of -1 is an instant
            // effect
            if (id != -1)
            // sustains
            {
                //				Com_Printf ("Sustain effect id %d\n", id);
                free_sustain = null
                s = cl_sustains
                run {
                    i = 0
                    while (i < MAX_SUSTAINS) {
                        if (s[i].id == 0) {
                            free_sustain = s[i]
                            break
                        }
                        i++
                    }
                }
                if (free_sustain != null) {
                    s[i].id = id
                    s[i].count = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, s[i].org)
                    MSG.ReadDir(Globals.net_message, s[i].dir)
                    r = MSG.ReadByte(Globals.net_message)
                    s[i].color = r and 255
                    s[i].magnitude = MSG.ReadShort(Globals.net_message)
                    s[i].endtime = Globals.cl.time + MSG.ReadLong(Globals.net_message)
                    s[i].think = object : cl_sustain_t.ThinkAdapter() {
                        fun think(self: cl_sustain_t) {
                            CL_newfx.ParticleSteamEffect2(self)
                        }
                    }
                    s[i].thinkinterval = 100
                    s[i].nextthink = Globals.cl.time
                } else {
                    //					Com_Printf ("No free sustains!\n");
                    // FIXME - read the stuff anyway
                    cnt = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    r = MSG.ReadByte(Globals.net_message)
                    magnitude = MSG.ReadShort(Globals.net_message)
                    magnitude = MSG.ReadLong(Globals.net_message) // really
                    // interval
                }
            } else
            // instant
            {
                cnt = MSG.ReadByte(Globals.net_message)
                MSG.ReadPos(Globals.net_message, pos)
                MSG.ReadDir(Globals.net_message, dir)
                r = MSG.ReadByte(Globals.net_message)
                magnitude = MSG.ReadShort(Globals.net_message)
                color = r and 255
                CL_newfx.ParticleSteamEffect(pos, dir, color, cnt, magnitude)
                //			S_StartSound (pos, 0, 0, cl_sfx_lashit, 1, ATTN_NORM, 0);
            }
        }

        // stack variable
        // pos
        fun ParseWidow() {
            val id: Int
            var i: Int
            val s: Array<cl_sustain_t>
            var free_sustain: cl_sustain_t?

            id = MSG.ReadShort(Globals.net_message)

            free_sustain = null
            s = cl_sustains
            run {
                i = 0
                while (i < MAX_SUSTAINS) {
                    if (s[i].id == 0) {
                        free_sustain = s[i]
                        break
                    }
                    i++
                }
            }
            if (free_sustain != null) {
                s[i].id = id
                MSG.ReadPos(Globals.net_message, s[i].org)
                s[i].endtime = Globals.cl.time + 2100
                s[i].think = object : cl_sustain_t.ThinkAdapter() {
                    fun think(self: cl_sustain_t) {
                        CL_newfx.Widowbeamout(self)
                    }
                }
                s[i].thinkinterval = 1
                s[i].nextthink = Globals.cl.time
            } else
            // no free sustains
            {
                // FIXME - read the stuff anyway
                MSG.ReadPos(Globals.net_message, pos)
            }
        }

        // stack variable
        // pos
        fun ParseNuke() {
            var i: Int
            val s: Array<cl_sustain_t>
            var free_sustain: cl_sustain_t?

            free_sustain = null
            s = cl_sustains
            run {
                i = 0
                while (i < MAX_SUSTAINS) {
                    if (s[i].id == 0) {
                        free_sustain = s[i]
                        break
                    }
                    i++
                }
            }
            if (free_sustain != null) {
                s[i].id = 21000
                MSG.ReadPos(Globals.net_message, s[i].org)
                s[i].endtime = Globals.cl.time + 1000
                s[i].think = object : cl_sustain_t.ThinkAdapter() {
                    fun think(self: cl_sustain_t) {
                        CL_newfx.Nukeblast(self)
                    }
                }
                s[i].thinkinterval = 1
                s[i].nextthink = Globals.cl.time
            } else
            // no free sustains
            {
                // FIXME - read the stuff anyway
                MSG.ReadPos(Globals.net_message, pos)
            }
        }

        //	  ROGUE
        //	  =============

        /*
     * ================= CL_ParseTEnt =================
     */
        var splash_color = intArray(0, 224, 176, 80, 208, 224, 232)
        // stack variable
        // pos, dir
        private val pos2 = floatArray(0.0, 0.0, 0.0)

        fun ParseTEnt() {
            val type: Int
            val ex: explosion_t
            val cnt: Int
            val color: Int
            var r: Int
            val ent: Int
            val magnitude: Int

            type = MSG.ReadByte(Globals.net_message)

            when (type) {
                Defines.TE_BLOOD // bullet hitting flesh
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    CL_fx.ParticleEffect(pos, dir, 232, 60)
                }

                Defines.TE_GUNSHOT // bullet hitting wall
                    , Defines.TE_SPARKS, Defines.TE_BULLET_SPARKS -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    if (type == Defines.TE_GUNSHOT)
                        CL_fx.ParticleEffect(pos, dir, 0, 40)
                    else
                        CL_fx.ParticleEffect(pos, dir, 224, 6)

                    if (type != Defines.TE_SPARKS) {
                        SmokeAndFlash(pos)

                        // impact sound
                        cnt = Lib.rand() and 15
                        if (cnt == 1)
                            S.StartSound(pos, 0, 0, cl_sfx_ric1, 1, Defines.ATTN_NORM, 0)
                        else if (cnt == 2)
                            S.StartSound(pos, 0, 0, cl_sfx_ric2, 1, Defines.ATTN_NORM, 0)
                        else if (cnt == 3)
                            S.StartSound(pos, 0, 0, cl_sfx_ric3, 1, Defines.ATTN_NORM, 0)
                    }
                }

                Defines.TE_SCREEN_SPARKS, Defines.TE_SHIELD_SPARKS -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    if (type == Defines.TE_SCREEN_SPARKS)
                        CL_fx.ParticleEffect(pos, dir, 208, 40)
                    else
                        CL_fx.ParticleEffect(pos, dir, 176, 40)
                    //FIXME : replace or remove this sound
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_SHOTGUN // bullet hitting wall
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    CL_fx.ParticleEffect(pos, dir, 0, 20)
                    SmokeAndFlash(pos)
                }

                Defines.TE_SPLASH // bullet hitting water
                -> {
                    cnt = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    r = MSG.ReadByte(Globals.net_message)
                    if (r > 6)
                        color = 0
                    else
                        color = splash_color[r]
                    CL_fx.ParticleEffect(pos, dir, color, cnt)

                    if (r == Defines.SPLASH_SPARKS) {
                        r = Lib.rand() and 3
                        if (r == 0)
                            S.StartSound(pos, 0, 0, cl_sfx_spark5, 1, Defines.ATTN_STATIC, 0)
                        else if (r == 1)
                            S.StartSound(pos, 0, 0, cl_sfx_spark6, 1, Defines.ATTN_STATIC, 0)
                        else
                            S.StartSound(pos, 0, 0, cl_sfx_spark7, 1, Defines.ATTN_STATIC, 0)
                    }
                }

                Defines.TE_LASER_SPARKS -> {
                    cnt = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    color = MSG.ReadByte(Globals.net_message)
                    CL_fx.ParticleEffect2(pos, dir, color, cnt)
                }

            // RAFAEL
                Defines.TE_BLUEHYPERBLASTER -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, dir)
                    CL_fx.BlasterParticles(pos, dir)
                }

                Defines.TE_BLASTER // blaster hitting wall
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    CL_fx.BlasterParticles(pos, dir)

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.ent.angles[0] = (Math.acos(dir[2]) / Math.PI * 180) as Float
                    // PMM - fixed to correct for pitch of 0
                    if (dir[0] != 0.0.toFloat())
                        ex.ent.angles[1] = (Math.atan2(dir[1], dir[0]) / Math.PI * 180) as Float
                    else if (dir[1] > 0)
                        ex.ent.angles[1] = 90
                    else if (dir[1] < 0)
                        ex.ent.angles[1] = 270
                    else
                        ex.ent.angles[1] = 0

                    ex.type = ex_misc
                    ex.ent.flags = Defines.RF_FULLBRIGHT or Defines.RF_TRANSLUCENT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 150
                    ex.lightcolor[0] = 1
                    ex.lightcolor[1] = 1
                    ex.ent.model = cl_mod_explode
                    ex.frames = 4
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_RAILTRAIL // railgun effect
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, pos2)
                    CL_fx.RailTrail(pos, pos2)
                    S.StartSound(pos2, 0, 0, cl_sfx_railg, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_EXPLOSION2, Defines.TE_GRENADE_EXPLOSION, Defines.TE_GRENADE_EXPLOSION_WATER -> {
                    MSG.ReadPos(Globals.net_message, pos)

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_poly
                    ex.ent.flags = Defines.RF_FULLBRIGHT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 350
                    ex.lightcolor[0] = 1.0.toFloat()
                    ex.lightcolor[1] = 0.5.toFloat()
                    ex.lightcolor[2] = 0.5.toFloat()
                    ex.ent.model = cl_mod_explo4
                    ex.frames = 19
                    ex.baseframe = 30
                    ex.ent.angles[1] = Lib.rand() % 360
                    CL_fx.ExplosionParticles(pos)
                    if (type == Defines.TE_GRENADE_EXPLOSION_WATER)
                        S.StartSound(pos, 0, 0, cl_sfx_watrexp, 1, Defines.ATTN_NORM, 0)
                    else
                        S.StartSound(pos, 0, 0, cl_sfx_grenexp, 1, Defines.ATTN_NORM, 0)
                }

            // RAFAEL
                Defines.TE_PLASMA_EXPLOSION -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_poly
                    ex.ent.flags = Defines.RF_FULLBRIGHT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 350
                    ex.lightcolor[0] = 1.0.toFloat()
                    ex.lightcolor[1] = 0.5.toFloat()
                    ex.lightcolor[2] = 0.5.toFloat()
                    ex.ent.angles[1] = Lib.rand() % 360
                    ex.ent.model = cl_mod_explo4
                    if (Globals.rnd.nextFloat() < 0.5)
                        ex.baseframe = 15
                    ex.frames = 15
                    CL_fx.ExplosionParticles(pos)
                    S.StartSound(pos, 0, 0, cl_sfx_rockexp, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_EXPLOSION1, Defines.TE_EXPLOSION1_BIG // PMM
                    , Defines.TE_ROCKET_EXPLOSION, Defines.TE_ROCKET_EXPLOSION_WATER, Defines.TE_EXPLOSION1_NP // PMM
                -> {
                    MSG.ReadPos(Globals.net_message, pos)

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_poly
                    ex.ent.flags = Defines.RF_FULLBRIGHT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 350
                    ex.lightcolor[0] = 1.0.toFloat()
                    ex.lightcolor[1] = 0.5.toFloat()
                    ex.lightcolor[2] = 0.5.toFloat()
                    ex.ent.angles[1] = Lib.rand() % 360
                    if (type != Defines.TE_EXPLOSION1_BIG)
                    // PMM
                        ex.ent.model = cl_mod_explo4 // PMM
                    else
                        ex.ent.model = cl_mod_explo4_big
                    if (Globals.rnd.nextFloat() < 0.5)
                        ex.baseframe = 15
                    ex.frames = 15
                    if ((type != Defines.TE_EXPLOSION1_BIG) && (type != Defines.TE_EXPLOSION1_NP))
                    // PMM
                        CL_fx.ExplosionParticles(pos) // PMM
                    if (type == Defines.TE_ROCKET_EXPLOSION_WATER)
                        S.StartSound(pos, 0, 0, cl_sfx_watrexp, 1, Defines.ATTN_NORM, 0)
                    else
                        S.StartSound(pos, 0, 0, cl_sfx_rockexp, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_BFG_EXPLOSION -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_poly
                    ex.ent.flags = Defines.RF_FULLBRIGHT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 350
                    ex.lightcolor[0] = 0.0.toFloat()
                    ex.lightcolor[1] = 1.0.toFloat()
                    ex.lightcolor[2] = 0.0.toFloat()
                    ex.ent.model = cl_mod_bfg_explo
                    ex.ent.flags = ex.ent.flags or Defines.RF_TRANSLUCENT
                    ex.ent.alpha = 0.30.toFloat()
                    ex.frames = 4
                }

                Defines.TE_BFG_BIGEXPLOSION -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_fx.BFGExplosionParticles(pos)
                }

                Defines.TE_BFG_LASER -> ParseLaser(-791555373)

                Defines.TE_BUBBLETRAIL -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, pos2)
                    CL_fx.BubbleTrail(pos, pos2)
                }

                Defines.TE_PARASITE_ATTACK, Defines.TE_MEDIC_CABLE_ATTACK -> ent = ParseBeam(cl_mod_parasite_segment)

                Defines.TE_BOSSTPORT // boss teleporting to station
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_fx.BigTeleportParticles(pos)
                    S.StartSound(pos, 0, 0, S.RegisterSound("misc/bigtele.wav"), 1, Defines.ATTN_NONE, 0)
                }

                Defines.TE_GRAPPLE_CABLE -> ent = ParseBeam2(cl_mod_grapple_cable)

            // RAFAEL
                Defines.TE_WELDING_SPARKS -> {
                    cnt = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    color = MSG.ReadByte(Globals.net_message)
                    CL_fx.ParticleEffect2(pos, dir, color, cnt)

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_flash
                    // note to self
                    // we need a better no draw flag
                    ex.ent.flags = Defines.RF_BEAM
                    ex.start = Globals.cl.frame.servertime - 0.1.toFloat()
                    ex.light = 100 + (Lib.rand() % 75)
                    ex.lightcolor[0] = 1.0.toFloat()
                    ex.lightcolor[1] = 1.0.toFloat()
                    ex.lightcolor[2] = 0.3.toFloat()
                    ex.ent.model = cl_mod_flash
                    ex.frames = 2
                }

                Defines.TE_GREENBLOOD -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    CL_fx.ParticleEffect2(pos, dir, 223, 30)
                }

            // RAFAEL
                Defines.TE_TUNNEL_SPARKS -> {
                    cnt = MSG.ReadByte(Globals.net_message)
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    color = MSG.ReadByte(Globals.net_message)
                    CL_fx.ParticleEffect3(pos, dir, color, cnt)
                }

            //	  =============
            //	  PGM
            // PMM -following code integrated for flechette (different color)
                Defines.TE_BLASTER2 // green blaster hitting wall
                    , Defines.TE_FLECHETTE // flechette
                -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)

                    // PMM
                    if (type == Defines.TE_BLASTER2)
                        CL_newfx.BlasterParticles2(pos, dir, 208)
                    else
                        CL_newfx.BlasterParticles2(pos, dir, 111) // 75

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.ent.angles[0] = (Math.acos(dir[2]) / Math.PI * 180) as Float
                    // PMM - fixed to correct for pitch of 0
                    if (dir[0] != 0.0.toFloat())
                        ex.ent.angles[1] = (Math.atan2(dir[1], dir[0]) / Math.PI * 180) as Float
                    else if (dir[1] > 0)
                        ex.ent.angles[1] = 90
                    else if (dir[1] < 0)
                        ex.ent.angles[1] = 270
                    else
                        ex.ent.angles[1] = 0

                    ex.type = ex_misc
                    ex.ent.flags = Defines.RF_FULLBRIGHT or Defines.RF_TRANSLUCENT

                    // PMM
                    if (type == Defines.TE_BLASTER2)
                        ex.ent.skinnum = 1
                    else
                    // flechette
                        ex.ent.skinnum = 2

                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 150
                    // PMM
                    if (type == Defines.TE_BLASTER2)
                        ex.lightcolor[1] = 1
                    else
                    // flechette
                    {
                        ex.lightcolor[0] = 0.19.toFloat()
                        ex.lightcolor[1] = 0.41.toFloat()
                        ex.lightcolor[2] = 0.75.toFloat()
                    }
                    ex.ent.model = cl_mod_explode
                    ex.frames = 4
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_LIGHTNING -> {
                    ent = ParseLightning(cl_mod_lightning)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, cl_sfx_lightning, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_DEBUGTRAIL -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, pos2)
                    CL_newfx.DebugTrail(pos, pos2)
                }

                Defines.TE_PLAIN_EXPLOSION -> {
                    MSG.ReadPos(Globals.net_message, pos)

                    ex = AllocExplosion()
                    Math3D.VectorCopy(pos, ex.ent.origin)
                    ex.type = ex_poly
                    ex.ent.flags = Defines.RF_FULLBRIGHT
                    ex.start = Globals.cl.frame.servertime - 100
                    ex.light = 350
                    ex.lightcolor[0] = 1.0.toFloat()
                    ex.lightcolor[1] = 0.5.toFloat()
                    ex.lightcolor[2] = 0.5.toFloat()
                    ex.ent.angles[1] = Lib.rand() % 360
                    ex.ent.model = cl_mod_explo4
                    if (Globals.rnd.nextFloat() < 0.5)
                        ex.baseframe = 15
                    ex.frames = 15
                    if (type == Defines.TE_ROCKET_EXPLOSION_WATER)
                        S.StartSound(pos, 0, 0, cl_sfx_watrexp, 1, Defines.ATTN_NORM, 0)
                    else
                        S.StartSound(pos, 0, 0, cl_sfx_rockexp, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_FLASHLIGHT -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    ent = MSG.ReadShort(Globals.net_message)
                    CL_newfx.Flashlight(ent, pos)
                }

                Defines.TE_FORCEWALL -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, pos2)
                    color = MSG.ReadByte(Globals.net_message)
                    CL_newfx.ForceWall(pos, pos2, color)
                }

                Defines.TE_HEATBEAM -> ent = ParsePlayerBeam(cl_mod_heatbeam)

                Defines.TE_MONSTER_HEATBEAM -> ent = ParsePlayerBeam(cl_mod_monster_heatbeam)

                Defines.TE_HEATBEAM_SPARKS -> {
                    //			cnt = MSG.ReadByte (net_message);
                    cnt = 50
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    //			r = MSG.ReadByte (net_message);
                    //			magnitude = MSG.ReadShort (net_message);
                    r = 8
                    magnitude = 60
                    color = r and 255
                    CL_newfx.ParticleSteamEffect(pos, dir, color, cnt, magnitude)
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_HEATBEAM_STEAM -> {
                    //			cnt = MSG.ReadByte (net_message);
                    cnt = 20
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    //			r = MSG.ReadByte (net_message);
                    //			magnitude = MSG.ReadShort (net_message);
                    //			color = r & 0xff;
                    color = 224
                    magnitude = 60
                    CL_newfx.ParticleSteamEffect(pos, dir, color, cnt, magnitude)
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_STEAM -> ParseSteam()

                Defines.TE_BUBBLETRAIL2 -> {
                    //			cnt = MSG.ReadByte (net_message);
                    cnt = 8
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadPos(Globals.net_message, pos2)
                    CL_newfx.BubbleTrail2(pos, pos2, cnt)
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_MOREBLOOD -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    CL_fx.ParticleEffect(pos, dir, 232, 250)
                }

                Defines.TE_CHAINFIST_SMOKE -> {
                    dir[0] = 0
                    dir[1] = 0
                    dir[2] = 1
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_newfx.ParticleSmokeEffect(pos, dir, 0, 20, 20)
                }

                Defines.TE_ELECTRIC_SPARKS -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    MSG.ReadDir(Globals.net_message, dir)
                    //			CL_ParticleEffect (pos, dir, 109, 40);
                    CL_fx.ParticleEffect(pos, dir, 117, 40)
                    //FIXME : replace or remove this sound
                    S.StartSound(pos, 0, 0, cl_sfx_lashit, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_TRACKER_EXPLOSION -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_newfx.ColorFlash(pos, 0, 150, -1, -1, -1)
                    CL_newfx.ColorExplosionParticles(pos, 0, 1)
                    S.StartSound(pos, 0, 0, cl_sfx_disrexp, 1, Defines.ATTN_NORM, 0)
                }

                Defines.TE_TELEPORT_EFFECT, Defines.TE_DBALL_GOAL -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_fx.TeleportParticles(pos)
                }

                Defines.TE_WIDOWBEAMOUT -> ParseWidow()

                Defines.TE_NUKEBLAST -> ParseNuke()

                Defines.TE_WIDOWSPLASH -> {
                    MSG.ReadPos(Globals.net_message, pos)
                    CL_newfx.WidowSplash(pos)
                }
            //	  PGM
            //	  ==============

                else -> Com.Error(Defines.ERR_DROP, "CL_ParseTEnt: bad type")
            }
        }

        // stack variable
        // dist, org
        private val ent = entity_t()

        /*
     * ================= CL_AddBeams =================
     */
        fun AddBeams() {
            var i: Int
            var j: Int
            val b: Array<beam_t>
            var d: Float
            var yaw: Float
            var pitch: Float
            var forward: Float
            var len: Float
            var steps: Float
            var model_length: Float

            //	   update beams
            b = cl_beams
            run {
                i = 0
                while (i < MAX_BEAMS) {
                    if (b[i].model == null || b[i].endtime < Globals.cl.time)
                        continue

                    // if coming from the player, update the start position
                    if (b[i].entity == Globals.cl.playernum + 1)
                    // entity 0 is the
                    // world
                    {
                        Math3D.VectorCopy(Globals.cl.refdef.vieworg, b[i].start)
                        b[i].start[2] -= 22 // adjust for view height
                    }
                    Math3D.VectorAdd(b[i].start, b[i].offset, org)

                    // calculate pitch and yaw
                    Math3D.VectorSubtract(b[i].end, org, dist)

                    if (dist[1] == 0 && dist[0] == 0) {
                        yaw = 0
                        if (dist[2] > 0)
                            pitch = 90
                        else
                            pitch = 270
                    } else {
                        // PMM - fixed to correct for pitch of 0
                        if (dist[0] != 0.0.toFloat())
                            yaw = (Math.atan2(dist[1], dist[0]) * 180 / Math.PI) as Float
                        else if (dist[1] > 0)
                            yaw = 90
                        else
                            yaw = 270
                        if (yaw < 0)
                            yaw += 360

                        forward = Math.sqrt(dist[0] * dist[0] + dist[1] * dist[1]) as Float
                        pitch = (Math.atan2(dist[2], forward) * -180.0 / Math.PI) as Float
                        if (pitch < 0)
                            pitch += 360.0
                    }

                    // add new entities for the beams
                    d = Math3D.VectorNormalize(dist)

                    //memset (&ent, 0, sizeof(ent));
                    ent.clear()
                    if (b[i].model == cl_mod_lightning) {
                        model_length = 35.0.toFloat()
                        d -= 20.0 // correction so it doesn't end in middle of tesla
                    } else {
                        model_length = 30.0.toFloat()
                    }
                    steps = Math.ceil(d / model_length) as Float
                    len = (d - model_length) / (steps - 1)

                    // PMM - special case for lightning model .. if the real length is
                    // shorter than the model,
                    // flip it around & draw it from the end to the start. This prevents
                    // the model from going
                    // through the tesla mine (instead it goes through the target)
                    if ((b[i].model == cl_mod_lightning) && (d <= model_length)) {
                        //				Com_Printf ("special case\n");
                        Math3D.VectorCopy(b[i].end, ent.origin)
                        // offset to push beam outside of tesla model (negative because
                        // dist is from end to start
                        // for this beam)
                        //				for (j=0 ; j<3 ; j++)
                        //					ent.origin[j] -= dist[j]*10.0;
                        ent.model = b[i].model
                        ent.flags = Defines.RF_FULLBRIGHT
                        ent.angles[0] = pitch
                        ent.angles[1] = yaw
                        ent.angles[2] = Lib.rand() % 360
                        V.AddEntity(ent)
                        return
                    }
                    while (d > 0) {
                        Math3D.VectorCopy(org, ent.origin)
                        ent.model = b[i].model
                        if (b[i].model == cl_mod_lightning) {
                            ent.flags = Defines.RF_FULLBRIGHT
                            ent.angles[0] = -pitch
                            ent.angles[1] = yaw + 180.0.toFloat()
                            ent.angles[2] = Lib.rand() % 360
                        } else {
                            ent.angles[0] = pitch
                            ent.angles[1] = yaw
                            ent.angles[2] = Lib.rand() % 360
                        }

                        //				Com_Printf("B: %d . %d\n", b[i].entity, b[i].dest_entity);
                        V.AddEntity(ent)

                        run {
                            j = 0
                            while (j < 3) {
                                org[j] += dist[j] * len
                                j++
                            }
                        }
                        d -= model_length
                    }
                    i++
                }
            }
        }

        //extern cvar_t *hand;

        // stack variable
        private val dist = FloatArray(3)
        private val org = FloatArray(3)
        private val f = FloatArray(3)
        private val u = FloatArray(3)
        private val r = FloatArray(3)
        /*
     * ================= ROGUE - draw player locked beams CL_AddPlayerBeams
     * =================
     */
        fun AddPlayerBeams() {
            var d: Float
            //entity_t ent = new entity_t();
            var yaw: Float
            var pitch: Float
            val forward: Float
            val len: Float
            val steps: Float
            var framenum = 0
            val model_length: Float

            val hand_multiplier: Float
            var oldframe: frame_t
            val ps: player_state_t
            val ops: player_state_t

            //	  PMM
            if (Globals.hand != null) {
                if (Globals.hand.value == 2)
                    hand_multiplier = 0
                else if (Globals.hand.value == 1)
                    hand_multiplier = (-1).toFloat()
                else
                    hand_multiplier = 1
            } else {
                hand_multiplier = 1
            }
            //	  PMM

            //	   update beams
            val b = cl_playerbeams
            for (i in 0..MAX_BEAMS - 1) {

                if (b[i].model == null || b[i].endtime < Globals.cl.time)
                    continue

                if (cl_mod_heatbeam != null && (b[i].model == cl_mod_heatbeam)) {

                    // if coming from the player, update the start position
                    if (b[i].entity == Globals.cl.playernum + 1)
                    // entity 0 is the
                    // world
                    {
                        // set up gun position
                        // code straight out of CL_AddViewWeapon
                        ps = Globals.cl.frame.playerstate
                        var j = (Globals.cl.frame.serverframe - 1) and Defines.UPDATE_MASK
                        oldframe = Globals.cl.frames[j]

                        if (oldframe.serverframe != Globals.cl.frame.serverframe - 1 || !oldframe.valid)
                            oldframe = Globals.cl.frame // previous frame was
                        // dropped or involid

                        ops = oldframe.playerstate
                        run {
                            j = 0
                            while (j < 3) {
                                b[i].start[j] = Globals.cl.refdef.vieworg[j] + ops.gunoffset[j] + Globals.cl.lerpfrac * (ps.gunoffset[j] - ops.gunoffset[j])
                                j++
                            }
                        }
                        Math3D.VectorMA(b[i].start, (hand_multiplier * b[i].offset[0]), Globals.cl.v_right, org)
                        Math3D.VectorMA(org, b[i].offset[1], Globals.cl.v_forward, org)
                        Math3D.VectorMA(org, b[i].offset[2], Globals.cl.v_up, org)
                        if ((Globals.hand != null) && (Globals.hand.value == 2)) {
                            Math3D.VectorMA(org, -1, Globals.cl.v_up, org)
                        }
                        // FIXME - take these out when final
                        Math3D.VectorCopy(Globals.cl.v_right, r)
                        Math3D.VectorCopy(Globals.cl.v_forward, f)
                        Math3D.VectorCopy(Globals.cl.v_up, u)

                    } else
                        Math3D.VectorCopy(b[i].start, org)
                } else {
                    // if coming from the player, update the start position
                    if (b[i].entity == Globals.cl.playernum + 1)
                    // entity 0 is the
                    // world
                    {
                        Math3D.VectorCopy(Globals.cl.refdef.vieworg, b[i].start)
                        b[i].start[2] -= 22 // adjust for view height
                    }
                    Math3D.VectorAdd(b[i].start, b[i].offset, org)
                }

                // calculate pitch and yaw
                Math3D.VectorSubtract(b[i].end, org, dist)

                //	  PMM
                if (cl_mod_heatbeam != null && (b[i].model == cl_mod_heatbeam) && (b[i].entity == Globals.cl.playernum + 1)) {

                    len = Math3D.VectorLength(dist)
                    Math3D.VectorScale(f, len, dist)
                    Math3D.VectorMA(dist, (hand_multiplier * b[i].offset[0]), r, dist)
                    Math3D.VectorMA(dist, b[i].offset[1], f, dist)
                    Math3D.VectorMA(dist, b[i].offset[2], u, dist)
                    if ((Globals.hand != null) && (Globals.hand.value == 2)) {
                        Math3D.VectorMA(org, -1, Globals.cl.v_up, org)
                    }
                }
                //	  PMM

                if (dist[1] == 0 && dist[0] == 0) {
                    yaw = 0
                    if (dist[2] > 0)
                        pitch = 90
                    else
                        pitch = 270
                } else {
                    // PMM - fixed to correct for pitch of 0
                    if (dist[0] != 0.0.toFloat())
                        yaw = (Math.atan2(dist[1], dist[0]) * 180 / Math.PI) as Float
                    else if (dist[1] > 0)
                        yaw = 90
                    else
                        yaw = 270
                    if (yaw < 0)
                        yaw += 360

                    forward = Math.sqrt(dist[0] * dist[0] + dist[1] * dist[1]) as Float
                    pitch = (Math.atan2(dist[2], forward) * -180.0 / Math.PI) as Float
                    if (pitch < 0)
                        pitch += 360.0
                }

                if (cl_mod_heatbeam != null && (b[i].model == cl_mod_heatbeam)) {
                    if (b[i].entity != Globals.cl.playernum + 1) {
                        framenum = 2
                        //					Com_Printf ("Third person\n");
                        ent.angles[0] = -pitch
                        ent.angles[1] = yaw + 180.0.toFloat()
                        ent.angles[2] = 0
                        //					Com_Printf ("%f %f - %f %f %f\n", -pitch, yaw+180.0,
                        // b[i].offset[0], b[i].offset[1], b[i].offset[2]);
                        Math3D.AngleVectors(ent.angles, f, r, u)

                        // if it's a non-origin offset, it's a player, so use the
                        // hardcoded player offset
                        if (!Math3D.VectorEquals(b[i].offset, Globals.vec3_origin)) {
                            Math3D.VectorMA(org, -(b[i].offset[0]) + 1, r, org)
                            Math3D.VectorMA(org, -(b[i].offset[1]), f, org)
                            Math3D.VectorMA(org, -(b[i].offset[2]) - 10, u, org)
                        } else {
                            // if it's a monster, do the particle effect
                            CL_newfx.MonsterPlasma_Shell(b[i].start)
                        }
                    } else {
                        framenum = 1
                    }
                }

                // if it's the heatbeam, draw the particle effect
                if ((cl_mod_heatbeam != null && (b[i].model == cl_mod_heatbeam) && (b[i].entity == Globals.cl.playernum + 1))) {
                    CL_newfx.Heatbeam(org, dist)
                }

                // add new entities for the beams
                d = Math3D.VectorNormalize(dist)

                //memset (&ent, 0, sizeof(ent));
                ent.clear()

                if (b[i].model == cl_mod_heatbeam) {
                    model_length = 32.0.toFloat()
                } else if (b[i].model == cl_mod_lightning) {
                    model_length = 35.0.toFloat()
                    d -= 20.0 // correction so it doesn't end in middle of tesla
                } else {
                    model_length = 30.0.toFloat()
                }
                steps = Math.ceil(d / model_length) as Float
                len = (d - model_length) / (steps - 1)

                // PMM - special case for lightning model .. if the real length is
                // shorter than the model,
                // flip it around & draw it from the end to the start. This prevents
                // the model from going
                // through the tesla mine (instead it goes through the target)
                if ((b[i].model == cl_mod_lightning) && (d <= model_length)) {
                    //				Com_Printf ("special case\n");
                    Math3D.VectorCopy(b[i].end, ent.origin)
                    // offset to push beam outside of tesla model (negative because
                    // dist is from end to start
                    // for this beam)
                    //				for (j=0 ; j<3 ; j++)
                    //					ent.origin[j] -= dist[j]*10.0;
                    ent.model = b[i].model
                    ent.flags = Defines.RF_FULLBRIGHT
                    ent.angles[0] = pitch
                    ent.angles[1] = yaw
                    ent.angles[2] = Lib.rand() % 360
                    V.AddEntity(ent)
                    return
                }
                while (d > 0) {
                    Math3D.VectorCopy(org, ent.origin)
                    ent.model = b[i].model
                    if (cl_mod_heatbeam != null && (b[i].model == cl_mod_heatbeam)) {
                        //					ent.flags = RF_FULLBRIGHT|RF_TRANSLUCENT;
                        //					ent.alpha = 0.3;
                        ent.flags = Defines.RF_FULLBRIGHT
                        ent.angles[0] = -pitch
                        ent.angles[1] = yaw + 180.0.toFloat()
                        ent.angles[2] = (Globals.cl.time) % 360
                        //					ent.angles[2] = rand()%360;
                        ent.frame = framenum
                    } else if (b[i].model == cl_mod_lightning) {
                        ent.flags = Defines.RF_FULLBRIGHT
                        ent.angles[0] = -pitch
                        ent.angles[1] = yaw + 180.0.toFloat()
                        ent.angles[2] = Lib.rand() % 360
                    } else {
                        ent.angles[0] = pitch
                        ent.angles[1] = yaw
                        ent.angles[2] = Lib.rand() % 360
                    }

                    //				Com_Printf("B: %d . %d\n", b[i].entity, b[i].dest_entity);
                    V.AddEntity(ent)

                    for (j in 0..3 - 1)
                        org[j] += dist[j] * len
                    d -= model_length
                }
            }
        }

        /*
     * ================= CL_AddExplosions =================
     */
        fun AddExplosions() {
            var ent: entity_t?
            var i: Int
            val ex: Array<explosion_t>
            var frac: Float
            var f: Int

            //memset (&ent, 0, sizeof(ent)); Pointer!
            ent = null
            ex = cl_explosions
            run {
                i = 0
                while (i < MAX_EXPLOSIONS) {
                    if (ex[i].type == ex_free)
                        continue
                    frac = (Globals.cl.time - ex[i].start) / 100.0.toFloat()
                    f = Math.floor(frac) as Int

                    ent = ex[i].ent

                    when (ex[i].type) {
                        ex_mflash -> if (f >= ex[i].frames - 1)
                            ex[i].type = ex_free
                        ex_misc -> {
                            if (f >= ex[i].frames - 1) {
                                ex[i].type = ex_free
                                break
                            }
                            ent!!.alpha = 1.0.toFloat() - frac / (ex[i].frames - 1).toFloat()
                        }
                        ex_flash -> {
                            if (f >= 1) {
                                ex[i].type = ex_free
                                break
                            }
                            ent!!.alpha = 1.0.toFloat()
                        }
                        ex_poly -> {
                            if (f >= ex[i].frames - 1) {
                                ex[i].type = ex_free
                                break
                            }

                            ent!!.alpha = (16.0.toFloat() - f.toFloat()) / 16.0.toFloat()

                            if (f < 10) {
                                ent!!.skinnum = (f shr 1)
                                if (ent!!.skinnum < 0)
                                    ent!!.skinnum = 0
                            } else {
                                ent!!.flags = ent!!.flags or Defines.RF_TRANSLUCENT
                                if (f < 13)
                                    ent!!.skinnum = 5
                                else
                                    ent!!.skinnum = 6
                            }
                        }
                        ex_poly2 -> {
                            if (f >= ex[i].frames - 1) {
                                ex[i].type = ex_free
                                break
                            }

                            ent!!.alpha = (5.0.toFloat() - f.toFloat()) / 5.0.toFloat()
                            ent!!.skinnum = 0
                            ent!!.flags = ent!!.flags or Defines.RF_TRANSLUCENT
                        }
                    }

                    if (ex[i].type == ex_free)
                        continue
                    if (ex[i].light != 0.0.toFloat()) {
                        V.AddLight(ent!!.origin, ex[i].light * ent!!.alpha, ex[i].lightcolor[0], ex[i].lightcolor[1], ex[i].lightcolor[2])
                    }

                    Math3D.VectorCopy(ent!!.origin, ent!!.oldorigin)

                    if (f < 0)
                        f = 0
                    ent!!.frame = ex[i].baseframe + f + 1
                    ent!!.oldframe = ex[i].baseframe + f
                    ent!!.backlerp = 1.0.toFloat() - Globals.cl.lerpfrac

                    V.AddEntity(ent)
                    i++
                }
            }
        }

        /*
     * ================= CL_AddLasers =================
     */
        fun AddLasers() {
            val l: Array<laser_t>
            var i: Int

            l = cl_lasers
            run {
                i = 0
                while (i < MAX_LASERS) {
                    if (l[i].endtime >= Globals.cl.time)
                        V.AddEntity(l[i].ent)
                    i++
                }
            }
        }

        /* PMM - CL_Sustains */
        fun ProcessSustain() {
            val s: Array<cl_sustain_t>
            var i: Int

            s = cl_sustains
            run {
                i = 0
                while (i < MAX_SUSTAINS) {
                    if (s[i].id != 0)
                        if ((s[i].endtime >= Globals.cl.time) && (Globals.cl.time >= s[i].nextthink)) {
                            s[i].think.think(s[i])
                        } else if (s[i].endtime < Globals.cl.time)
                            s[i].id = 0
                    i++
                }
            }
        }

        /*
     * ================= CL_AddTEnts =================
     */
        fun AddTEnts() {
            AddBeams()
            // PMM - draw plasma beams
            AddPlayerBeams()
            AddExplosions()
            AddLasers()
            // PMM - set up sustain
            ProcessSustain()
        }
    }
}