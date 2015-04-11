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

import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.xcommand_t
import lwjake2.sys.Timer
import lwjake2.util.Math3D
import lwjake2.util.Vargs

import java.io.IOException
import java.nio.FloatBuffer

/**
 * V
 */
public class V : Globals() {
    companion object {

        var cl_testblend: cvar_t

        var cl_testparticles: cvar_t

        var cl_testentities: cvar_t

        var cl_testlights: cvar_t

        var cl_stats: cvar_t

        var r_numdlights: Int = 0

        var r_dlights = arrayOfNulls<dlight_t>(MAX_DLIGHTS)

        var r_numentities: Int = 0

        var r_entities = arrayOfNulls<entity_t>(MAX_ENTITIES)

        var r_numparticles: Int = 0

        //static particle_t[] r_particles = new particle_t[MAX_PARTICLES];

        var r_lightstyles = arrayOfNulls<lightstyle_t>(MAX_LIGHTSTYLES)
        {
            for (i in r_dlights.indices)
                r_dlights[i] = dlight_t()
            for (i in r_entities.indices)
                r_entities[i] = entity_t()
            for (i in r_lightstyles.indices)
                r_lightstyles[i] = lightstyle_t()
        }

        /*
     * ==================== V_ClearScene
     * 
     * Specifies the model that will be used as the world ====================
     */
        fun ClearScene() {
            r_numdlights = 0
            r_numentities = 0
            r_numparticles = 0
        }

        /*
     * ===================== V_AddEntity
     * 
     * =====================
     */
        fun AddEntity(ent: entity_t) {
            if (r_numentities >= MAX_ENTITIES)
                return
            r_entities[r_numentities++].set(ent)
        }

        /*
     * ===================== V_AddParticle
     * 
     * =====================
     */
        fun AddParticle(org: FloatArray, color: Int, alpha: Float) {
            if (r_numparticles >= MAX_PARTICLES)
                return

            var i = r_numparticles++

            var c = particle_t.colorTable[color]
            c = c or ((alpha * 255).toInt() shl 24)
            particle_t.colorArray.put(i, c)

            i *= 3
            val vertexBuf = particle_t.vertexArray
            vertexBuf.put(i++, org[0])
            vertexBuf.put(i++, org[1])
            vertexBuf.put(i++, org[2])
        }

        /*
     * ===================== V_AddLight
     * 
     * =====================
     */
        fun AddLight(org: FloatArray, intensity: Float, r: Float, g: Float, b: Float) {
            val dl: dlight_t

            if (r_numdlights >= MAX_DLIGHTS)
                return
            dl = r_dlights[r_numdlights++]
            Math3D.VectorCopy(org, dl.origin)
            dl.intensity = intensity
            dl.color[0] = r
            dl.color[1] = g
            dl.color[2] = b
        }

        /*
     * ===================== V_AddLightStyle
     * 
     * =====================
     */
        fun AddLightStyle(style: Int, r: Float, g: Float, b: Float) {
            val ls: lightstyle_t

            if (style < 0 || style > MAX_LIGHTSTYLES)
                Com.Error(ERR_DROP, "Bad light style " + style)
            ls = r_lightstyles[style]

            ls.white = r + g + b
            ls.rgb[0] = r
            ls.rgb[1] = g
            ls.rgb[2] = b
        }

        // stack variable
        private val origin = floatArray(0.0, 0.0, 0.0)

        /*
     * ================ V_TestParticles
     * 
     * If cl_testparticles is set, create 4096 particles in the view
     * ================
     */
        fun TestParticles() {
            var i: Int
            var j: Int
            var d: Float
            var r: Float
            var u: Float

            r_numparticles = 0
            run {
                i = 0
                while (i < MAX_PARTICLES) {
                    d = i.toFloat() * 0.25.toFloat()
                    r = 4 * ((i and 7).toFloat() - 3.5.toFloat())
                    u = 4 * (((i shr 3) and 7).toFloat() - 3.5.toFloat())

                    run {
                        j = 0
                        while (j < 3) {
                            origin[j] = cl.refdef.vieworg[j] + cl.v_forward[j] * d + cl.v_right[j] * r + cl.v_up[j] * u
                            j++
                        }
                    }

                    AddParticle(origin, 8, cl_testparticles.value)
                    i++
                }
            }
        }

        /*
     * ================ V_TestEntities
     * 
     * If cl_testentities is set, create 32 player models ================
     */
        fun TestEntities() {
            var i: Int
            var j: Int
            var f: Float
            var r: Float
            var ent: entity_t

            r_numentities = 32
            //memset (r_entities, 0, sizeof(r_entities));
            run {
                i = 0
                while (i < r_entities.size()) {
                    r_entities[i].clear()
                    i++
                }
            }

            run {
                i = 0
                while (i < r_numentities) {
                    ent = r_entities[i]

                    r = 64 * ((i % 4).toFloat() - 1.5.toFloat())
                    f = (64 * (i / 4) + 128).toFloat()

                    run {
                        j = 0
                        while (j < 3) {
                            ent.origin[j] = cl.refdef.vieworg[j] + cl.v_forward[j] * f + cl.v_right[j] * r
                            j++
                        }
                    }

                    ent.model = cl.baseclientinfo.model
                    ent.skin = cl.baseclientinfo.skin
                    i++
                }
            }
        }

        /*
     * ================ V_TestLights
     * 
     * If cl_testlights is set, create 32 lights models ================
     */
        fun TestLights() {
            var i: Int
            var j: Int
            var f: Float
            var r: Float
            var dl: dlight_t

            r_numdlights = 32
            //memset (r_dlights, 0, sizeof(r_dlights));
            run {
                i = 0
                while (i < r_dlights.size()) {
                    r_dlights[i] = dlight_t()
                    i++
                }
            }

            run {
                i = 0
                while (i < r_numdlights) {
                    dl = r_dlights[i]

                    r = 64 * ((i % 4).toFloat() - 1.5.toFloat())
                    f = (64 * (i / 4) + 128).toFloat()

                    run {
                        j = 0
                        while (j < 3) {
                            dl.origin[j] = cl.refdef.vieworg[j] + cl.v_forward[j] * f + cl.v_right[j] * r
                            j++
                        }
                    }
                    dl.color[0] = ((i % 6) + 1) and 1
                    dl.color[1] = (((i % 6) + 1) and 2) shr 1
                    dl.color[2] = (((i % 6) + 1) and 4) shr 2
                    dl.intensity = 200
                    i++
                }
            }
        }

        var Gun_Next_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                gun_frame++
                Com.Printf("frame " + gun_frame + "\n")
            }
        }

        var Gun_Prev_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                gun_frame--
                if (gun_frame < 0)
                    gun_frame = 0
                Com.Printf("frame " + gun_frame + "\n")
            }
        }

        var Gun_Model_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                if (Cmd.Argc() != 2) {
                    gun_model = null
                    return
                }
                val name = "models/" + Cmd.Argv(1) + "/tris.md2"
                gun_model = re.RegisterModel(name)
            }
        }

        /*
     * ================== V_RenderView
     * 
     * ==================
     */
        fun RenderView(stereo_separation: Float) {
            //		extern int entitycmpfnc( const entity_t *, const entity_t * );
            //
            if (cls.state != ca_active)
                return

            if (!cl.refresh_prepped)
                return  // still loading

            if (cl_timedemo.value != 0.0.toFloat()) {
                if (cl.timedemo_start == 0)
                    cl.timedemo_start = Timer.Milliseconds()
                cl.timedemo_frames++
            }

            // an invalid frame will just use the exact previous refdef
            // we can't use the old frame if the video mode has changed, though...
            if (cl.frame.valid && (cl.force_refdef || cl_paused.value == 0.0.toFloat())) {
                cl.force_refdef = false

                V.ClearScene()

                // build a refresh entity list and calc cl.sim*
                // this also calls CL_CalcViewValues which loads
                // v_forward, etc.
                CL_ents.AddEntities()

                if (cl_testparticles.value != 0.0.toFloat())
                    TestParticles()
                if (cl_testentities.value != 0.0.toFloat())
                    TestEntities()
                if (cl_testlights.value != 0.0.toFloat())
                    TestLights()
                if (cl_testblend.value != 0.0.toFloat()) {
                    cl.refdef.blend[0] = 1.0.toFloat()
                    cl.refdef.blend[1] = 0.5.toFloat()
                    cl.refdef.blend[2] = 0.25.toFloat()
                    cl.refdef.blend[3] = 0.5.toFloat()
                }

                // offset vieworg appropriately if we're doing stereo separation
                if (stereo_separation != 0) {
                    val tmp = FloatArray(3)

                    Math3D.VectorScale(cl.v_right, stereo_separation, tmp)
                    Math3D.VectorAdd(cl.refdef.vieworg, tmp, cl.refdef.vieworg)
                }

                // never let it sit exactly on a node line, because a water plane
                // can
                // dissapear when viewed with the eye exactly on it.
                // the server protocol only specifies to 1/8 pixel, so add 1/16 in
                // each axis
                cl.refdef.vieworg[0] += 1.0 / 16
                cl.refdef.vieworg[1] += 1.0 / 16
                cl.refdef.vieworg[2] += 1.0 / 16

                cl.refdef.x = scr_vrect.x
                cl.refdef.y = scr_vrect.y
                cl.refdef.width = scr_vrect.width
                cl.refdef.height = scr_vrect.height
                cl.refdef.fov_y = Math3D.CalcFov(cl.refdef.fov_x, cl.refdef.width, cl.refdef.height)
                cl.refdef.time = cl.time * 0.001.toFloat()

                cl.refdef.areabits = cl.frame.areabits

                if (cl_add_entities.value == 0.0.toFloat())
                    r_numentities = 0
                if (cl_add_particles.value == 0.0.toFloat())
                    r_numparticles = 0
                if (cl_add_lights.value == 0.0.toFloat())
                    r_numdlights = 0
                if (cl_add_blend.value == 0) {
                    Math3D.VectorClear(cl.refdef.blend)
                }

                cl.refdef.num_entities = r_numentities
                cl.refdef.entities = r_entities
                cl.refdef.num_particles = r_numparticles
                cl.refdef.num_dlights = r_numdlights
                cl.refdef.dlights = r_dlights
                cl.refdef.lightstyles = r_lightstyles

                cl.refdef.rdflags = cl.frame.playerstate.rdflags

                // sort entities for better cache locality
                // !!! useless in Java !!!
                //Arrays.sort(cl.refdef.entities, entitycmpfnc);
            }

            re.RenderFrame(cl.refdef)
            if (cl_stats.value != 0.0.toFloat())
                Com.Printf("ent:%i  lt:%i  part:%i\n", Vargs(3).add(r_numentities).add(r_numdlights).add(r_numparticles))
            if (log_stats.value != 0.0.toFloat() && (log_stats_file != null))
                try {
                    log_stats_file.write(r_numentities + "," + r_numdlights + "," + r_numparticles)
                } catch (e: IOException) {
                }


            SCR.AddDirtyPoint(scr_vrect.x, scr_vrect.y)
            SCR.AddDirtyPoint(scr_vrect.x + scr_vrect.width - 1, scr_vrect.y + scr_vrect.height - 1)

            SCR.DrawCrosshair()
        }

        /*
     * ============= V_Viewpos_f =============
     */
        var Viewpos_f: xcommand_t = object : xcommand_t() {
            public fun execute() {
                Com.Printf("(%i %i %i) : %i\n", Vargs(4).add(cl.refdef.vieworg[0] as Int).add(cl.refdef.vieworg[1] as Int).add(cl.refdef.vieworg[2] as Int).add(cl.refdef.viewangles[YAW] as Int))
            }
        }

        public fun Init() {
            Cmd.AddCommand("gun_next", Gun_Next_f)
            Cmd.AddCommand("gun_prev", Gun_Prev_f)
            Cmd.AddCommand("gun_model", Gun_Model_f)

            Cmd.AddCommand("viewpos", Viewpos_f)

            crosshair = Cvar.Get("crosshair", "0", CVAR_ARCHIVE)

            cl_testblend = Cvar.Get("cl_testblend", "0", 0)
            cl_testparticles = Cvar.Get("cl_testparticles", "0", 0)
            cl_testentities = Cvar.Get("cl_testentities", "0", 0)
            cl_testlights = Cvar.Get("cl_testlights", "0", 0)

            cl_stats = Cvar.Get("cl_stats", "0", 0)
        }
    }
}