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

package lwjake2.render.lwjgl

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.client.VID
import lwjake2.client.entity_t
import lwjake2.client.particle_t
import lwjake2.client.refdef_t
import lwjake2.game.Cmd
import lwjake2.game.cplane_t
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.qfiles
import lwjake2.qcommon.xcommand_t
import lwjake2.render.glconfig_t
import lwjake2.render.glstate_t
import lwjake2.render.image_t
import lwjake2.render.mleaf_t
import lwjake2.render.model_t
import lwjake2.util.Math3D
import lwjake2.util.Vargs

import java.awt.Dimension
import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBMultitexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

/**
 * Main

 * @author cwei
 */
public abstract class Main : Base() {

    var c_visible_lightmaps: Int = 0
    var c_visible_textures: Int = 0

    var registration_sequence: Int = 0

    // this a hack for function pointer test
    // default disabled
    var qglColorTableEXT = false
    var qglActiveTextureARB = false
    var qglPointParameterfEXT = false
    var qglLockArraysEXT = false
    var qwglSwapIntervalEXT = false

    //	=================
    //  abstract methods
    //	=================
    protected abstract fun Draw_GetPalette()

    abstract fun GL_ImageList_f()
    abstract fun GL_ScreenShot_f()
    abstract fun GL_SetTexturePalette(palette: IntArray)
    abstract fun GL_Strings_f()

    abstract fun Mod_Modellist_f()
    abstract fun Mod_PointInLeaf(point: FloatArray, model: model_t): mleaf_t

    abstract fun GL_SetDefaultState()

    abstract fun GL_InitImages()
    abstract fun Mod_Init()  // Model.java
    abstract fun R_InitParticleTexture()  // MIsc.java
    abstract fun R_DrawAliasModel(e: entity_t)  // Mesh.java
    abstract fun R_DrawBrushModel(e: entity_t)  // Surf.java
    abstract fun Draw_InitLocal()
    abstract fun R_LightPoint(p: FloatArray, color: FloatArray)
    abstract fun R_PushDlights()
    abstract fun R_MarkLeaves()
    abstract fun R_DrawWorld()
    abstract fun R_RenderDlights()
    abstract fun R_DrawAlphaSurfaces()

    abstract fun Mod_FreeAll()

    abstract fun GL_ShutdownImages()
    abstract fun GL_Bind(texnum: Int)
    abstract fun GL_TexEnv(mode: Int)
    abstract fun GL_TextureMode(string: String)
    abstract fun GL_TextureAlphaMode(string: String)
    abstract fun GL_TextureSolidMode(string: String)
    abstract fun GL_UpdateSwapInterval()

    /*
	====================================================================
	
	from gl_rmain.c
	
	====================================================================
	*/

    var GL_TEXTURE0 = GL13.GL_TEXTURE0
    var GL_TEXTURE1 = GL13.GL_TEXTURE1

    var r_worldmodel: model_t? = null

    var gldepthmin: Float = 0.toFloat()
    var gldepthmax: Float = 0.toFloat()

    var gl_config = glconfig_t()
    var gl_state = glstate_t()

    var r_notexture: image_t // use for bad textures
    var r_particletexture: image_t // little dot for particles

    var currententity: entity_t
    var currentmodel: model_t? = null

    var frustum = array<cplane_t>(cplane_t(), cplane_t(), cplane_t(), cplane_t())

    var r_visframecount: Int = 0 // bumped when going to a new PVS
    var r_framecount: Int = 0 // used for dlight push checking

    var c_brush_polys: Int = 0
    var c_alias_polys: Int = 0

    var v_blend = floatArray(0.0, 0.0, 0.0, 0.0) // final blending color

    //
    //	   view origin
    //
    var vup = floatArray(0.0, 0.0, 0.0)
    var vpn = floatArray(0.0, 0.0, 0.0)
    var vright = floatArray(0.0, 0.0, 0.0)
    var r_origin = floatArray(0.0, 0.0, 0.0)

    //float r_world_matrix[] = new float[16];
    var r_world_matrix = BufferUtils.createFloatBuffer(16)

    var r_base_world_matrix = FloatArray(16)

    //
    //	   screen size info
    //
    var r_newrefdef: refdef_t? = refdef_t()

    var r_viewcluster: Int = 0
    var r_viewcluster2: Int = 0
    var r_oldviewcluster: Int = 0
    var r_oldviewcluster2: Int = 0

    var r_norefresh: cvar_t
    var r_drawentities: cvar_t
    var r_drawworld: cvar_t
    var r_speeds: cvar_t
    var r_fullbright: cvar_t
    var r_novis: cvar_t
    var r_nocull: cvar_t
    var r_lerpmodels: cvar_t
    var r_lefthand: cvar_t

    var r_lightlevel: cvar_t
    // FIXME: This is a HACK to get the client's light level

    var gl_nosubimage: cvar_t
    var gl_allow_software: cvar_t

    var gl_vertex_arrays: cvar_t

    var gl_particle_min_size: cvar_t
    var gl_particle_max_size: cvar_t
    var gl_particle_size: cvar_t
    var gl_particle_att_a: cvar_t
    var gl_particle_att_b: cvar_t
    var gl_particle_att_c: cvar_t

    var gl_ext_swapinterval: cvar_t
    var gl_ext_palettedtexture: cvar_t
    var gl_ext_multitexture: cvar_t
    var gl_ext_pointparameters: cvar_t
    var gl_ext_compiled_vertex_array: cvar_t

    var gl_log: cvar_t
    var gl_bitdepth: cvar_t
    var gl_drawbuffer: cvar_t
    var gl_driver: cvar_t
    var gl_lightmap: cvar_t
    var gl_shadows: cvar_t
    var gl_mode: cvar_t
    var gl_dynamic: cvar_t
    var gl_monolightmap: cvar_t
    var gl_modulate: cvar_t
    var gl_nobind: cvar_t
    var gl_round_down: cvar_t
    var gl_picmip: cvar_t
    var gl_skymip: cvar_t
    var gl_showtris: cvar_t
    var gl_ztrick: cvar_t
    var gl_finish: cvar_t
    var gl_clear: cvar_t
    var gl_cull: cvar_t
    var gl_polyblend: cvar_t
    var gl_flashblend: cvar_t
    var gl_playermip: cvar_t
    var gl_saturatelighting: cvar_t
    var gl_swapinterval: cvar_t
    var gl_texturemode: cvar_t
    var gl_texturealphamode: cvar_t
    var gl_texturesolidmode: cvar_t
    var gl_lockpvs: cvar_t

    var gl_3dlabs_broken: cvar_t

    var vid_gamma: cvar_t
    var vid_ref: cvar_t

    // ============================================================================
    // to port from gl_rmain.c, ...
    // ============================================================================

    /**
     * R_CullBox
     * Returns true if the box is completely outside the frustum
     */
    fun R_CullBox(mins: FloatArray, maxs: FloatArray): Boolean {
        assert((mins.size() == 3 && maxs.size() == 3), "vec3_t bug")

        if (r_nocull.value != 0)
            return false

        for (i in 0..4 - 1) {
            if (Math3D.BoxOnPlaneSide(mins, maxs, frustum[i]) == 2)
                return true
        }
        return false
    }

    /**
     * R_RotateForEntity
     */
    fun R_RotateForEntity(e: entity_t) {
        GL11.glTranslatef(e.origin[0], e.origin[1], e.origin[2])

        GL11.glRotatef(e.angles[1], 0, 0, 1)
        GL11.glRotatef(-e.angles[0], 0, 1, 0)
        GL11.glRotatef(-e.angles[2], 1, 0, 0)
    }

    /*
	=============================================================
	
	   SPRITE MODELS
	
	=============================================================
	*/

    // stack variable
    private val point = floatArray(0.0, 0.0, 0.0)

    /**
     * R_DrawSpriteModel
     */
    fun R_DrawSpriteModel(e: entity_t) {
        var alpha = 1.0.toFloat()

        val frame: qfiles.dsprframe_t
        val psprite: qfiles.dsprite_t

        // don't even bother culling, because it's just a single
        // polygon without a surface cache

        psprite = currentmodel!!.extradata as qfiles.dsprite_t

        e.frame %= psprite.numframes

        frame = psprite.frames[e.frame]

        if ((e.flags and Defines.RF_TRANSLUCENT) != 0)
            alpha = e.alpha

        if (alpha != 1.0.toFloat())
            GL11.glEnable(GL11.GL_BLEND)

        GL11.glColor4f(1, 1, 1, alpha)

        GL_Bind(currentmodel!!.skins[e.frame].texnum)

        GL_TexEnv(GL11.GL_MODULATE)

        if (alpha == 1.0)
            GL11.glEnable(GL11.GL_ALPHA_TEST)
        else
            GL11.glDisable(GL11.GL_ALPHA_TEST)

        GL11.glBegin(GL11.GL_QUADS)

        GL11.glTexCoord2f(0, 1)
        Math3D.VectorMA(e.origin, -frame.origin_y, vup, point)
        Math3D.VectorMA(point, -frame.origin_x, vright, point)
        GL11.glVertex3f(point[0], point[1], point[2])

        GL11.glTexCoord2f(0, 0)
        Math3D.VectorMA(e.origin, frame.height - frame.origin_y, vup, point)
        Math3D.VectorMA(point, -frame.origin_x, vright, point)
        GL11.glVertex3f(point[0], point[1], point[2])

        GL11.glTexCoord2f(1, 0)
        Math3D.VectorMA(e.origin, frame.height - frame.origin_y, vup, point)
        Math3D.VectorMA(point, frame.width - frame.origin_x, vright, point)
        GL11.glVertex3f(point[0], point[1], point[2])

        GL11.glTexCoord2f(1, 1)
        Math3D.VectorMA(e.origin, -frame.origin_y, vup, point)
        Math3D.VectorMA(point, frame.width - frame.origin_x, vright, point)
        GL11.glVertex3f(point[0], point[1], point[2])

        GL11.glEnd()

        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL_TexEnv(GL11.GL_REPLACE)

        if (alpha != 1.0.toFloat())
            GL11.glDisable(GL11.GL_BLEND)

        GL11.glColor4f(1, 1, 1, 1)
    }

    // ==================================================================================

    // stack variable
    private val shadelight = floatArray(0.0, 0.0, 0.0)

    /**
     * R_DrawNullModel
     */
    fun R_DrawNullModel() {
        if ((currententity.flags and Defines.RF_FULLBRIGHT) != 0) {
            // cwei wollte blau: shadelight[0] = shadelight[1] = shadelight[2] = 1.0F;
            shadelight[0] = shadelight[1] = shadelight[2] = 0.0.toFloat()
            shadelight[2] = 0.8.toFloat()
        } else {
            R_LightPoint(currententity.origin, shadelight)
        }

        GL11.glPushMatrix()
        R_RotateForEntity(currententity)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glColor3f(shadelight[0], shadelight[1], shadelight[2])

        // this replaces the TRIANGLE_FAN
        //glut.glutWireCube(gl, 20);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex3f(0, 0, -16)
        var i: Int
        run {
            i = 0
            while (i <= 4) {
                GL11.glVertex3f((16.0.toFloat() * Math.cos(i * Math.PI / 2)) as Float, (16.0.toFloat() * Math.sin(i * Math.PI / 2)) as Float, 0.0.toFloat())
                i++
            }
        }
        GL11.glEnd()

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex3f(0, 0, 16)
        run {
            i = 4
            while (i >= 0) {
                GL11.glVertex3f((16.0.toFloat() * Math.cos(i * Math.PI / 2)) as Float, (16.0.toFloat() * Math.sin(i * Math.PI / 2)) as Float, 0.0.toFloat())
                i--
            }
        }
        GL11.glEnd()


        GL11.glColor3f(1, 1, 1)
        GL11.glPopMatrix()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    /**
     * R_DrawEntitiesOnList
     */
    fun R_DrawEntitiesOnList() {
        if (r_drawentities.value == 0.0.toFloat())
            return

        // draw non-transparent first
        var i: Int
        run {
            i = 0
            while (i < r_newrefdef!!.num_entities) {
                currententity = r_newrefdef!!.entities[i]
                if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0)
                    continue // solid

                if ((currententity.flags and Defines.RF_BEAM) != 0) {
                    R_DrawBeam(currententity)
                } else {
                    currentmodel = currententity.model
                    if (currentmodel == null) {
                        R_DrawNullModel()
                        continue
                    }
                    when (currentmodel!!.type) {
                        mod_alias -> R_DrawAliasModel(currententity)
                        mod_brush -> R_DrawBrushModel(currententity)
                        mod_sprite -> R_DrawSpriteModel(currententity)
                        else -> Com.Error(Defines.ERR_DROP, "Bad modeltype")
                    }
                }
                i++
            }
        }
        // draw transparent entities
        // we could sort these if it ever becomes a problem...
        GL11.glDepthMask(false) // no z writes
        run {
            i = 0
            while (i < r_newrefdef!!.num_entities) {
                currententity = r_newrefdef!!.entities[i]
                if ((currententity.flags and Defines.RF_TRANSLUCENT) == 0)
                    continue // solid

                if ((currententity.flags and Defines.RF_BEAM) != 0) {
                    R_DrawBeam(currententity)
                } else {
                    currentmodel = currententity.model

                    if (currentmodel == null) {
                        R_DrawNullModel()
                        continue
                    }
                    when (currentmodel!!.type) {
                        mod_alias -> R_DrawAliasModel(currententity)
                        mod_brush -> R_DrawBrushModel(currententity)
                        mod_sprite -> R_DrawSpriteModel(currententity)
                        else -> Com.Error(Defines.ERR_DROP, "Bad modeltype")
                    }
                }
                i++
            }
        }
        GL11.glDepthMask(true) // back to writing
    }

    // stack variable
    private val up = floatArray(0.0, 0.0, 0.0)
    private val right = floatArray(0.0, 0.0, 0.0)
    /**
     * GL_DrawParticles
     */
    fun GL_DrawParticles(num_particles: Int) {
        var origin_x: Float
        var origin_y: Float
        var origin_z: Float

        Math3D.VectorScale(vup, 1.5.toFloat(), up)
        Math3D.VectorScale(vright, 1.5.toFloat(), right)

        GL_Bind(r_particletexture.texnum)
        GL11.glDepthMask(false) // no z buffering
        GL11.glEnable(GL11.GL_BLEND)
        GL_TexEnv(GL11.GL_MODULATE)

        GL11.glBegin(GL11.GL_TRIANGLES)

        val sourceVertices = particle_t.vertexArray
        val sourceColors = particle_t.colorArray
        var scale: Float
        var color: Int
        run {
            var j = 0
            var i = 0
            while (i < num_particles) {
                origin_x = sourceVertices.get(j++)
                origin_y = sourceVertices.get(j++)
                origin_z = sourceVertices.get(j++)

                // hack a scale up to keep particles from disapearing
                scale = (origin_x - r_origin[0]) * vpn[0] + (origin_y - r_origin[1]) * vpn[1] + (origin_z - r_origin[2]) * vpn[2]

                scale = if ((scale < 20)) 1 else 1 + scale * 0.004.toFloat()

                color = sourceColors.get(i)

                GL11.glColor4ub(((color) and 255).toByte(), ((color shr 8) and 255).toByte(), ((color shr 16) and 255).toByte(), ((color.ushr(24))).toByte())
                // first vertex
                GL11.glTexCoord2f(0.0625.toFloat(), 0.0625.toFloat())
                GL11.glVertex3f(origin_x, origin_y, origin_z)
                // second vertex
                GL11.glTexCoord2f(1.0625.toFloat(), 0.0625.toFloat())
                GL11.glVertex3f(origin_x + up[0] * scale, origin_y + up[1] * scale, origin_z + up[2] * scale)
                // third vertex
                GL11.glTexCoord2f(0.0625.toFloat(), 1.0625.toFloat())
                GL11.glVertex3f(origin_x + right[0] * scale, origin_y + right[1] * scale, origin_z + right[2] * scale)
                i++
            }
        }
        GL11.glEnd()

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor4f(1, 1, 1, 1)
        GL11.glDepthMask(true) // back to normal Z buffering
        GL_TexEnv(GL11.GL_REPLACE)
    }

    /**
     * R_DrawParticles
     */
    fun R_DrawParticles() {

        if (gl_ext_pointparameters.value != 0.0.toFloat() && qglPointParameterfEXT) {

            //GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glVertexPointer(3, 0, particle_t.vertexArray)
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY)
            GL11.glColorPointer(4, true, 0, particle_t.getColorAsByteBuffer())

            GL11.glDepthMask(false)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glPointSize(gl_particle_size.value)

            GL11.glDrawArrays(GL11.GL_POINTS, 0, r_newrefdef!!.num_particles)

            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY)
            //GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glColor4f(1.0.toFloat(), 1.0.toFloat(), 1.0.toFloat(), 1.0.toFloat())
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_TEXTURE_2D)

        } else {
            GL_DrawParticles(r_newrefdef!!.num_particles)
        }
    }

    /**
     * R_PolyBlend
     */
    fun R_PolyBlend() {
        if (gl_polyblend.value == 0.0.toFloat())
            return

        if (v_blend[3] == 0.0.toFloat())
            return

        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_TEXTURE_2D)

        GL11.glLoadIdentity()

        // FIXME: get rid of these
        GL11.glRotatef(-90, 1, 0, 0) // put Z going up
        GL11.glRotatef(90, 0, 0, 1) // put Z going up

        GL11.glColor4f(v_blend[0], v_blend[1], v_blend[2], v_blend[3])

        GL11.glBegin(GL11.GL_QUADS)

        GL11.glVertex3f(10, 100, 100)
        GL11.glVertex3f(10, -100, 100)
        GL11.glVertex3f(10, -100, -100)
        GL11.glVertex3f(10, 100, -100)
        GL11.glEnd()

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_ALPHA_TEST)

        GL11.glColor4f(1, 1, 1, 1)
    }

    // =======================================================================

    /**
     * SignbitsForPlane
     */
    fun SignbitsForPlane(out: cplane_t): Int {
        // for fast box on planeside test
        var bits = 0
        for (j in 0..3 - 1) {
            if (out.normal[j] < 0)
                bits = bits or (1 shl j)
        }
        return bits
    }

    /**
     * R_SetFrustum
     */
    fun R_SetFrustum() {
        // rotate VPN right by FOV_X/2 degrees
        Math3D.RotatePointAroundVector(frustum[0].normal, vup, vpn, -(90.toFloat() - r_newrefdef!!.fov_x / 2.toFloat()))
        // rotate VPN left by FOV_X/2 degrees
        Math3D.RotatePointAroundVector(frustum[1].normal, vup, vpn, 90.toFloat() - r_newrefdef!!.fov_x / 2.toFloat())
        // rotate VPN up by FOV_X/2 degrees
        Math3D.RotatePointAroundVector(frustum[2].normal, vright, vpn, 90.toFloat() - r_newrefdef!!.fov_y / 2.toFloat())
        // rotate VPN down by FOV_X/2 degrees
        Math3D.RotatePointAroundVector(frustum[3].normal, vright, vpn, -(90.toFloat() - r_newrefdef!!.fov_y / 2.toFloat()))

        for (i in 0..4 - 1) {
            frustum[i].type = Defines.PLANE_ANYZ
            frustum[i].dist = Math3D.DotProduct(r_origin, frustum[i].normal)
            frustum[i].signbits = SignbitsForPlane(frustum[i]).toByte()
        }
    }

    // =======================================================================

    // stack variable
    private val temp = floatArray(0.0, 0.0, 0.0)

    /**
     * R_SetupFrame
     */
    fun R_SetupFrame() {
        r_framecount++

        //	build the transformation matrix for the given view angles
        Math3D.VectorCopy(r_newrefdef!!.vieworg, r_origin)

        Math3D.AngleVectors(r_newrefdef!!.viewangles, vpn, vright, vup)

        //	current viewcluster
        var leaf: mleaf_t
        if ((r_newrefdef!!.rdflags and Defines.RDF_NOWORLDMODEL) == 0) {
            r_oldviewcluster = r_viewcluster
            r_oldviewcluster2 = r_viewcluster2
            leaf = Mod_PointInLeaf(r_origin, r_worldmodel)
            r_viewcluster = r_viewcluster2 = leaf.cluster

            // check above and below so crossing solid water doesn't draw wrong
            if (leaf.contents == 0) {
                // look down a bit
                Math3D.VectorCopy(r_origin, temp)
                temp[2] -= 16
                leaf = Mod_PointInLeaf(temp, r_worldmodel)
                if ((leaf.contents and Defines.CONTENTS_SOLID) == 0 && (leaf.cluster != r_viewcluster2))
                    r_viewcluster2 = leaf.cluster
            } else {
                // look up a bit
                Math3D.VectorCopy(r_origin, temp)
                temp[2] += 16
                leaf = Mod_PointInLeaf(temp, r_worldmodel)
                if ((leaf.contents and Defines.CONTENTS_SOLID) == 0 && (leaf.cluster != r_viewcluster2))
                    r_viewcluster2 = leaf.cluster
            }
        }

        for (i in 0..4 - 1)
            v_blend[i] = r_newrefdef!!.blend[i]

        c_brush_polys = 0
        c_alias_polys = 0

        // clear out the portion of the screen that the NOWORLDMODEL defines
        if ((r_newrefdef!!.rdflags and Defines.RDF_NOWORLDMODEL) != 0) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glClearColor(0.3.toFloat(), 0.3.toFloat(), 0.3.toFloat(), 1.0.toFloat())
            GL11.glScissor(r_newrefdef!!.x, vid.height - r_newrefdef!!.height - r_newrefdef!!.y, r_newrefdef!!.width, r_newrefdef!!.height)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            GL11.glClearColor(1.0.toFloat(), 0.0.toFloat(), 0.5.toFloat(), 0.5.toFloat())
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    /**
     * MYgluPerspective

     * @param fovy
    * *
     * @param aspect
    * *
     * @param zNear
    * *
     * @param zFar
     */
    fun MYgluPerspective(fovy: Double, aspect: Double, zNear: Double, zFar: Double) {
        val ymax = zNear * Math.tan(fovy * Math.PI / 360.0)
        val ymin = -ymax

        var xmin = ymin * aspect
        var xmax = ymax * aspect

        xmin += -(2 * gl_state.camera_separation) / zNear
        xmax += -(2 * gl_state.camera_separation) / zNear

        GL11.glFrustum(xmin, xmax, ymin, ymax, zNear, zFar)
    }

    /**
     * R_SetupGL
     */
    fun R_SetupGL() {

        //
        // set up viewport
        //
        //int x = (int) Math.floor(r_newrefdef.x * vid.width / vid.width);
        val x = r_newrefdef!!.x
        //int x2 = (int) Math.ceil((r_newrefdef.x + r_newrefdef.width) * vid.width / vid.width);
        val x2 = r_newrefdef!!.x + r_newrefdef!!.width
        //int y = (int) Math.floor(vid.height - r_newrefdef.y * vid.height / vid.height);
        val y = vid.height - r_newrefdef!!.y
        //int y2 = (int) Math.ceil(vid.height - (r_newrefdef.y + r_newrefdef.height) * vid.height / vid.height);
        val y2 = vid.height - (r_newrefdef!!.y + r_newrefdef!!.height)

        val w = x2 - x
        val h = y - y2

        GL11.glViewport(x, y2, w, h)

        //
        // set up projection matrix
        //
        val screenaspect = r_newrefdef!!.width as Float / r_newrefdef!!.height
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        MYgluPerspective(r_newrefdef!!.fov_y, screenaspect.toDouble(), 4, 4096)

        GL11.glCullFace(GL11.GL_FRONT)

        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()

        GL11.glRotatef(-90, 1, 0, 0) // put Z going up
        GL11.glRotatef(90, 0, 0, 1) // put Z going up
        GL11.glRotatef(-r_newrefdef!!.viewangles[2], 1, 0, 0)
        GL11.glRotatef(-r_newrefdef!!.viewangles[0], 0, 1, 0)
        GL11.glRotatef(-r_newrefdef!!.viewangles[1], 0, 0, 1)
        GL11.glTranslatef(-r_newrefdef!!.vieworg[0], -r_newrefdef!!.vieworg[1], -r_newrefdef!!.vieworg[2])

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, r_world_matrix)
        r_world_matrix.clear()

        //
        // set drawing parms
        //
        if (gl_cull.value != 0.0.toFloat())
            GL11.glEnable(GL11.GL_CULL_FACE)
        else
            GL11.glDisable(GL11.GL_CULL_FACE)

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    var trickframe = 0

    /**
     * R_Clear
     */
    fun R_Clear() {
        if (gl_ztrick.value != 0.0.toFloat()) {

            if (gl_clear.value != 0.0.toFloat()) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
            }

            trickframe++
            if ((trickframe and 1) != 0) {
                gldepthmin = 0
                gldepthmax = 0.49999.toFloat()
                GL11.glDepthFunc(GL11.GL_LEQUAL)
            } else {
                gldepthmin = 1
                gldepthmax = 0.5.toFloat()
                GL11.glDepthFunc(GL11.GL_GEQUAL)
            }
        } else {
            if (gl_clear.value != 0.0.toFloat())
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            else
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT)

            gldepthmin = 0
            gldepthmax = 1
            GL11.glDepthFunc(GL11.GL_LEQUAL)
        }
        GL11.glDepthRange(gldepthmin, gldepthmax)
    }

    /**
     * R_Flash
     */
    fun R_Flash() {
        R_PolyBlend()
    }

    /**
     * R_RenderView
     * r_newrefdef must be set before the first call
     */
    fun R_RenderView(fd: refdef_t) {

        if (r_norefresh.value != 0.0.toFloat())
            return

        r_newrefdef = fd

        // included by cwei
        if (r_newrefdef == null) {
            Com.Error(Defines.ERR_DROP, "R_RenderView: refdef_t fd is null")
        }

        if (r_worldmodel == null && (r_newrefdef!!.rdflags and Defines.RDF_NOWORLDMODEL) == 0)
            Com.Error(Defines.ERR_DROP, "R_RenderView: NULL worldmodel")

        if (r_speeds.value != 0.0.toFloat()) {
            c_brush_polys = 0
            c_alias_polys = 0
        }

        R_PushDlights()

        if (gl_finish.value != 0.0.toFloat())
            GL11.glFinish()

        R_SetupFrame()

        R_SetFrustum()

        R_SetupGL()

        R_MarkLeaves() // done here so we know if we're in water

        R_DrawWorld()

        R_DrawEntitiesOnList()

        R_RenderDlights()

        R_DrawParticles()

        R_DrawAlphaSurfaces()

        R_Flash()

        if (r_speeds.value != 0.0.toFloat()) {
            VID.Printf(Defines.PRINT_ALL, "%4i wpoly %4i epoly %i tex %i lmaps\n", Vargs(4).add(c_brush_polys).add(c_alias_polys).add(c_visible_textures).add(c_visible_lightmaps))
        }
    }

    /**
     * R_SetGL2D
     */
    fun R_SetGL2D() {
        // set 2D virtual screen size
        GL11.glViewport(0, 0, vid.width, vid.height)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        GL11.glOrtho(0, vid.width, vid.height, 0, -99999, 99999)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glColor4f(1, 1, 1, 1)
    }

    // stack variable
    private val light = floatArray(0.0, 0.0, 0.0)

    /**
     * R_SetLightLevel
     */
    fun R_SetLightLevel() {
        if ((r_newrefdef!!.rdflags and Defines.RDF_NOWORLDMODEL) != 0)
            return

        // save off light value for server to look at (BIG HACK!)

        R_LightPoint(r_newrefdef!!.vieworg, light)

        // pick the greatest component, which should be the same
        // as the mono value returned by software
        if (light[0] > light[1]) {
            if (light[0] > light[2])
                r_lightlevel.value = 150 * light[0]
            else
                r_lightlevel.value = 150 * light[2]
        } else {
            if (light[1] > light[2])
                r_lightlevel.value = 150 * light[1]
            else
                r_lightlevel.value = 150 * light[2]
        }
    }

    /**
     * R_RenderFrame
     */
    protected fun R_RenderFrame(fd: refdef_t) {
        R_RenderView(fd)
        R_SetLightLevel()
        R_SetGL2D()
    }

    /**
     * R_Register
     */
    protected fun R_Register() {
        r_lefthand = Cvar.Get("hand", "0", Globals.CVAR_USERINFO or Globals.CVAR_ARCHIVE)
        r_norefresh = Cvar.Get("r_norefresh", "0", 0)
        r_fullbright = Cvar.Get("r_fullbright", "0", 0)
        r_drawentities = Cvar.Get("r_drawentities", "1", 0)
        r_drawworld = Cvar.Get("r_drawworld", "1", 0)
        r_novis = Cvar.Get("r_novis", "0", 0)
        r_nocull = Cvar.Get("r_nocull", "0", 0)
        r_lerpmodels = Cvar.Get("r_lerpmodels", "1", 0)
        r_speeds = Cvar.Get("r_speeds", "0", 0)

        r_lightlevel = Cvar.Get("r_lightlevel", "1", 0)

        gl_nosubimage = Cvar.Get("gl_nosubimage", "0", 0)
        gl_allow_software = Cvar.Get("gl_allow_software", "0", 0)

        gl_particle_min_size = Cvar.Get("gl_particle_min_size", "2", Globals.CVAR_ARCHIVE)
        gl_particle_max_size = Cvar.Get("gl_particle_max_size", "40", Globals.CVAR_ARCHIVE)
        gl_particle_size = Cvar.Get("gl_particle_size", "40", Globals.CVAR_ARCHIVE)
        gl_particle_att_a = Cvar.Get("gl_particle_att_a", "0.01", Globals.CVAR_ARCHIVE)
        gl_particle_att_b = Cvar.Get("gl_particle_att_b", "0.0", Globals.CVAR_ARCHIVE)
        gl_particle_att_c = Cvar.Get("gl_particle_att_c", "0.01", Globals.CVAR_ARCHIVE)

        gl_modulate = Cvar.Get("gl_modulate", "1.5", Globals.CVAR_ARCHIVE)
        gl_log = Cvar.Get("gl_log", "0", 0)
        gl_bitdepth = Cvar.Get("gl_bitdepth", "0", 0)
        gl_mode = Cvar.Get("gl_mode", "3", Globals.CVAR_ARCHIVE) // 640x480
        gl_lightmap = Cvar.Get("gl_lightmap", "0", 0)
        gl_shadows = Cvar.Get("gl_shadows", "0", Globals.CVAR_ARCHIVE)
        gl_dynamic = Cvar.Get("gl_dynamic", "1", 0)
        gl_nobind = Cvar.Get("gl_nobind", "0", 0)
        gl_round_down = Cvar.Get("gl_round_down", "1", 0)
        gl_picmip = Cvar.Get("gl_picmip", "0", 0)
        gl_skymip = Cvar.Get("gl_skymip", "0", 0)
        gl_showtris = Cvar.Get("gl_showtris", "0", 0)
        gl_ztrick = Cvar.Get("gl_ztrick", "0", 0)
        gl_finish = Cvar.Get("gl_finish", "0", Globals.CVAR_ARCHIVE)
        gl_clear = Cvar.Get("gl_clear", "0", 0)
        gl_cull = Cvar.Get("gl_cull", "1", 0)
        gl_polyblend = Cvar.Get("gl_polyblend", "1", 0)
        gl_flashblend = Cvar.Get("gl_flashblend", "0", 0)
        gl_playermip = Cvar.Get("gl_playermip", "0", 0)
        gl_monolightmap = Cvar.Get("gl_monolightmap", "0", 0)
        gl_driver = Cvar.Get("gl_driver", "opengl32", Globals.CVAR_ARCHIVE)
        gl_texturemode = Cvar.Get("gl_texturemode", "GL_LINEAR_MIPMAP_NEAREST", Globals.CVAR_ARCHIVE)
        gl_texturealphamode = Cvar.Get("gl_texturealphamode", "default", Globals.CVAR_ARCHIVE)
        gl_texturesolidmode = Cvar.Get("gl_texturesolidmode", "default", Globals.CVAR_ARCHIVE)
        gl_lockpvs = Cvar.Get("gl_lockpvs", "0", 0)

        gl_vertex_arrays = Cvar.Get("gl_vertex_arrays", "1", Globals.CVAR_ARCHIVE)

        gl_ext_swapinterval = Cvar.Get("gl_ext_swapinterval", "1", Globals.CVAR_ARCHIVE)
        gl_ext_palettedtexture = Cvar.Get("gl_ext_palettedtexture", "0", Globals.CVAR_ARCHIVE)
        gl_ext_multitexture = Cvar.Get("gl_ext_multitexture", "1", Globals.CVAR_ARCHIVE)
        gl_ext_pointparameters = Cvar.Get("gl_ext_pointparameters", "1", Globals.CVAR_ARCHIVE)
        gl_ext_compiled_vertex_array = Cvar.Get("gl_ext_compiled_vertex_array", "1", Globals.CVAR_ARCHIVE)

        gl_drawbuffer = Cvar.Get("gl_drawbuffer", "GL_BACK", 0)
        gl_swapinterval = Cvar.Get("gl_swapinterval", "0", Globals.CVAR_ARCHIVE)

        gl_saturatelighting = Cvar.Get("gl_saturatelighting", "0", 0)

        gl_3dlabs_broken = Cvar.Get("gl_3dlabs_broken", "1", Globals.CVAR_ARCHIVE)

        vid_fullscreen = Cvar.Get("vid_fullscreen", "0", Globals.CVAR_ARCHIVE)
        vid_gamma = Cvar.Get("vid_gamma", "1.0", Globals.CVAR_ARCHIVE)
        vid_ref = Cvar.Get("vid_ref", "lwjgl", Globals.CVAR_ARCHIVE)

        Cmd.AddCommand("imagelist", object : xcommand_t() {
            public fun execute() {
                GL_ImageList_f()
            }
        })

        Cmd.AddCommand("screenshot", object : xcommand_t() {
            public fun execute() {
                GL_ScreenShot_f()
            }
        })
        Cmd.AddCommand("modellist", object : xcommand_t() {
            public fun execute() {
                Mod_Modellist_f()
            }
        })
        Cmd.AddCommand("gl_strings", object : xcommand_t() {
            public fun execute() {
                GL_Strings_f()
            }
        })
    }

    /**
     * R_SetMode
     */
    protected fun R_SetMode(): Boolean {
        val fullscreen = (vid_fullscreen.value > 0.0.toFloat())

        vid_fullscreen.modified = false
        gl_mode.modified = false

        val dim = Dimension(vid.width, vid.height)

        var err: Int //  enum rserr_t
        if ((err = GLimp_SetMode(dim, gl_mode.value as Int, fullscreen)) == rserr_ok) {
            gl_state.prev_mode = gl_mode.value as Int
        } else {
            if (err == rserr_invalid_fullscreen) {
                Cvar.SetValue("vid_fullscreen", 0)
                vid_fullscreen.modified = false
                VID.Printf(Defines.PRINT_ALL, "ref_gl::R_SetMode() - fullscreen unavailable in this mode\n")
                if ((err = GLimp_SetMode(dim, gl_mode.value as Int, false)) == rserr_ok)
                    return true
            } else if (err == rserr_invalid_mode) {
                Cvar.SetValue("gl_mode", gl_state.prev_mode)
                gl_mode.modified = false
                VID.Printf(Defines.PRINT_ALL, "ref_gl::R_SetMode() - invalid mode\n")
            }

            // try setting it back to something safe
            if ((err = GLimp_SetMode(dim, gl_state.prev_mode, false)) != rserr_ok) {
                VID.Printf(Defines.PRINT_ALL, "ref_gl::R_SetMode() - could not revert to safe mode\n")
                return false
            }
        }
        return true
    }

    var r_turbsin = FloatArray(256)

    /**
     * R_Init
     */
    protected fun R_Init(vid_xpos: Int, vid_ypos: Int): Boolean {

        assert((Warp.SIN.length == 256), "warpsin table bug")

        // fill r_turbsin
        for (j in 0..256 - 1) {
            r_turbsin[j] = Warp.SIN[j] * 0.5.toFloat()
        }

        VID.Printf(Defines.PRINT_ALL, "ref_gl version: " + REF_VERSION + '\n')

        Draw_GetPalette()

        R_Register()

        // set our "safe" modes
        gl_state.prev_mode = 3

        // create the window and set up the context
        if (!R_SetMode()) {
            VID.Printf(Defines.PRINT_ALL, "ref_gl::R_Init() - could not R_SetMode()\n")
            return false
        }
        return true
    }

    /**
     * R_Init2
     */
    protected fun R_Init2(): Boolean {
        VID.MenuInit()

        /*
		** get our various GL strings
		*/
        gl_config.vendor_string = GL11.glGetString(GL11.GL_VENDOR)
        VID.Printf(Defines.PRINT_ALL, "GL_VENDOR: " + gl_config.vendor_string + '\n')
        gl_config.renderer_string = GL11.glGetString(GL11.GL_RENDERER)
        VID.Printf(Defines.PRINT_ALL, "GL_RENDERER: " + gl_config.renderer_string + '\n')
        gl_config.version_string = GL11.glGetString(GL11.GL_VERSION)
        VID.Printf(Defines.PRINT_ALL, "GL_VERSION: " + gl_config.version_string + '\n')
        gl_config.extensions_string = GL11.glGetString(GL11.GL_EXTENSIONS)
        VID.Printf(Defines.PRINT_ALL, "GL_EXTENSIONS: " + gl_config.extensions_string + '\n')

        gl_config.parseOpenGLVersion()

        val renderer_buffer = gl_config.renderer_string.toLowerCase()
        val vendor_buffer = gl_config.vendor_string.toLowerCase()

        if (renderer_buffer.indexOf("voodoo") >= 0) {
            if (renderer_buffer.indexOf("rush") < 0)
                gl_config.renderer = GL_RENDERER_VOODOO
            else
                gl_config.renderer = GL_RENDERER_VOODOO_RUSH
        } else if (vendor_buffer.indexOf("sgi") >= 0)
            gl_config.renderer = GL_RENDERER_SGI
        else if (renderer_buffer.indexOf("permedia") >= 0)
            gl_config.renderer = GL_RENDERER_PERMEDIA2
        else if (renderer_buffer.indexOf("glint") >= 0)
            gl_config.renderer = GL_RENDERER_GLINT_MX
        else if (renderer_buffer.indexOf("glzicd") >= 0)
            gl_config.renderer = GL_RENDERER_REALIZM
        else if (renderer_buffer.indexOf("gdi") >= 0)
            gl_config.renderer = GL_RENDERER_MCD
        else if (renderer_buffer.indexOf("pcx2") >= 0)
            gl_config.renderer = GL_RENDERER_PCX2
        else if (renderer_buffer.indexOf("verite") >= 0)
            gl_config.renderer = GL_RENDERER_RENDITION
        else
            gl_config.renderer = GL_RENDERER_OTHER

        val monolightmap = gl_monolightmap.string.toUpperCase()
        if (monolightmap.length() < 2 || monolightmap.charAt(1) != 'F') {
            if (gl_config.renderer == GL_RENDERER_PERMEDIA2) {
                Cvar.Set("gl_monolightmap", "A")
                VID.Printf(Defines.PRINT_ALL, "...using gl_monolightmap 'a'\n")
            } else if ((gl_config.renderer and GL_RENDERER_POWERVR) != 0) {
                Cvar.Set("gl_monolightmap", "0")
            } else {
                Cvar.Set("gl_monolightmap", "0")
            }
        }

        // power vr can't have anything stay in the framebuffer, so
        // the screen needs to redraw the tiled background every frame
        if ((gl_config.renderer and GL_RENDERER_POWERVR) != 0) {
            Cvar.Set("scr_drawall", "1")
        } else {
            Cvar.Set("scr_drawall", "0")
        }

        // MCD has buffering issues
        if (gl_config.renderer == GL_RENDERER_MCD) {
            Cvar.SetValue("gl_finish", 1)
        }

        if ((gl_config.renderer and GL_RENDERER_3DLABS) != 0) {
            if (gl_3dlabs_broken.value != 0.0.toFloat())
                gl_config.allow_cds = false
            else
                gl_config.allow_cds = true
        } else {
            gl_config.allow_cds = true
        }

        if (gl_config.allow_cds)
            VID.Printf(Defines.PRINT_ALL, "...allowing CDS\n")
        else
            VID.Printf(Defines.PRINT_ALL, "...disabling CDS\n")

        /*
		** grab extensions
		*/
        if (gl_config.extensions_string.indexOf("GL_EXT_compiled_vertex_array") >= 0 || gl_config.extensions_string.indexOf("GL_SGI_compiled_vertex_array") >= 0) {
            VID.Printf(Defines.PRINT_ALL, "...enabling GL_EXT_compiled_vertex_array\n")
            //		 qglLockArraysEXT = ( void * ) qwglGetProcAddress( "glLockArraysEXT" );
            if (gl_ext_compiled_vertex_array.value != 0.0.toFloat())
                qglLockArraysEXT = true
            else
                qglLockArraysEXT = false
            //		 qglUnlockArraysEXT = ( void * ) qwglGetProcAddress( "glUnlockArraysEXT" );
            //qglUnlockArraysEXT = true;
        } else {
            VID.Printf(Defines.PRINT_ALL, "...GL_EXT_compiled_vertex_array not found\n")
            qglLockArraysEXT = false
        }

        if (gl_config.extensions_string.indexOf("WGL_EXT_swap_control") >= 0) {
            qwglSwapIntervalEXT = true
            VID.Printf(Defines.PRINT_ALL, "...enabling WGL_EXT_swap_control\n")
        } else {
            qwglSwapIntervalEXT = false
            VID.Printf(Defines.PRINT_ALL, "...WGL_EXT_swap_control not found\n")
        }

        if (gl_config.extensions_string.indexOf("GL_EXT_point_parameters") >= 0) {
            if (gl_ext_pointparameters.value != 0.0.toFloat()) {
                //			 qglPointParameterfEXT = ( void (APIENTRY *)( GLenum, GLfloat ) ) qwglGetProcAddress( "glPointParameterfEXT" );
                qglPointParameterfEXT = true
                //			 qglPointParameterfvEXT = ( void (APIENTRY *)( GLenum, const GLfloat * ) ) qwglGetProcAddress( "glPointParameterfvEXT" );
                VID.Printf(Defines.PRINT_ALL, "...using GL_EXT_point_parameters\n")
            } else {
                VID.Printf(Defines.PRINT_ALL, "...ignoring GL_EXT_point_parameters\n")
            }
        } else {
            VID.Printf(Defines.PRINT_ALL, "...GL_EXT_point_parameters not found\n")
        }

        // #ifdef __linux__
        //	 if ( strstr( gl_config.extensions_string, "3DFX_set_global_palette" ))
        //	 {
        //		 if ( gl_ext_palettedtexture->value )
        //		 {
        //			 VID.Printf( Defines.PRINT_ALL, "...using 3DFX_set_global_palette\n" );
        //			 qgl3DfxSetPaletteEXT = ( void ( APIENTRY * ) (GLuint *) )qwglGetProcAddress( "gl3DfxSetPaletteEXT" );
        ////			 qglColorTableEXT = Fake_glColorTableEXT;
        //		 }
        //		 else
        //		 {
        //			 VID.Printf( Defines.PRINT_ALL, "...ignoring 3DFX_set_global_palette\n" );
        //		 }
        //	 }
        //	 else
        //	 {
        //		 VID.Printf( Defines.PRINT_ALL, "...3DFX_set_global_palette not found\n" );
        //	 }
        // #endif

        if (!qglColorTableEXT && gl_config.extensions_string.indexOf("GL_EXT_paletted_texture") >= 0 && gl_config.extensions_string.indexOf("GL_EXT_shared_texture_palette") >= 0) {
            if (gl_ext_palettedtexture.value != 0.0.toFloat()) {
                VID.Printf(Defines.PRINT_ALL, "...using GL_EXT_shared_texture_palette\n")
                qglColorTableEXT = false // true; TODO jogl bug
            } else {
                VID.Printf(Defines.PRINT_ALL, "...ignoring GL_EXT_shared_texture_palette\n")
                qglColorTableEXT = false
            }
        } else {
            VID.Printf(Defines.PRINT_ALL, "...GL_EXT_shared_texture_palette not found\n")
        }

        if (gl_config.extensions_string.indexOf("GL_ARB_multitexture") >= 0) {
            VID.Printf(Defines.PRINT_ALL, "...using GL_ARB_multitexture\n")
            qglActiveTextureARB = true
            GL_TEXTURE0 = ARBMultitexture.GL_TEXTURE0_ARB
            GL_TEXTURE1 = ARBMultitexture.GL_TEXTURE1_ARB
        } else {
            VID.Printf(Defines.PRINT_ALL, "...GL_ARB_multitexture not found\n")
        }

        if (!(qglActiveTextureARB))
            return false

        GL_SetDefaultState()

        GL_InitImages()
        Mod_Init()
        R_InitParticleTexture()
        Draw_InitLocal()

        val err = GL11.glGetError()
        if (err != GL11.GL_NO_ERROR)
            VID.Printf(Defines.PRINT_ALL, "glGetError() = 0x%x\n\t%s\n", Vargs(2).add(err).add("" + GL11.glGetString(err)))

        return true
    }

    /**
     * R_Shutdown
     */
    protected fun R_Shutdown() {
        Cmd.RemoveCommand("modellist")
        Cmd.RemoveCommand("screenshot")
        Cmd.RemoveCommand("imagelist")
        Cmd.RemoveCommand("gl_strings")

        Mod_FreeAll()

        GL_ShutdownImages()

        /*
		 * shut down OS specific OpenGL stuff like contexts, etc.
		 */
        GLimp_Shutdown()
    }

    /**
     * R_BeginFrame
     */
    protected fun R_BeginFrame(camera_separation: Float) {

        gl_state.camera_separation = camera_separation

        /*
		** change modes if necessary
		*/
        if (gl_mode.modified || vid_fullscreen.modified) {
            // FIXME: only restart if CDS is required
            val ref: cvar_t

            ref = Cvar.Get("vid_ref", "lwjgl", 0)
            ref.modified = true
        }

        if (gl_log.modified) {
            GLimp_EnableLogging((gl_log.value != 0.0.toFloat()))
            gl_log.modified = false
        }

        if (gl_log.value != 0.0.toFloat()) {
            GLimp_LogNewFrame()
        }

        /*
		** update 3Dfx gamma -- it is expected that a user will do a vid_restart
		** after tweaking this value
		*/
        if (vid_gamma.modified) {
            vid_gamma.modified = false

            if ((gl_config.renderer and GL_RENDERER_VOODOO) != 0) {
                // wird erstmal nicht gebraucht

                /*
				char envbuffer[1024];
				float g;
				
				g = 2.00 * ( 0.8 - ( vid_gamma->value - 0.5 ) ) + 1.0F;
				Com_sprintf( envbuffer, sizeof(envbuffer), "SSTV2_GAMMA=%f", g );
				putenv( envbuffer );
				Com_sprintf( envbuffer, sizeof(envbuffer), "SST_GAMMA=%f", g );
				putenv( envbuffer );
				*/
                VID.Printf(Defines.PRINT_DEVELOPER, "gamma anpassung fuer VOODOO nicht gesetzt")
            }
        }

        GLimp_BeginFrame(camera_separation)

        /*
		** go into 2D mode
		*/
        GL11.glViewport(0, 0, vid.width, vid.height)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        GL11.glOrtho(0, vid.width, vid.height, 0, -99999, 99999)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glLoadIdentity()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glColor4f(1, 1, 1, 1)

        /*
		** draw buffer stuff
		*/
        if (gl_drawbuffer.modified) {
            gl_drawbuffer.modified = false

            if (gl_state.camera_separation == 0 || !gl_state.stereo_enabled) {
                if (gl_drawbuffer.string.equalsIgnoreCase("GL_FRONT"))
                    GL11.glDrawBuffer(GL11.GL_FRONT)
                else
                    GL11.glDrawBuffer(GL11.GL_BACK)
            }
        }

        /*
		** texturemode stuff
		*/
        if (gl_texturemode.modified) {
            GL_TextureMode(gl_texturemode.string)
            gl_texturemode.modified = false
        }

        if (gl_texturealphamode.modified) {
            GL_TextureAlphaMode(gl_texturealphamode.string)
            gl_texturealphamode.modified = false
        }

        if (gl_texturesolidmode.modified) {
            GL_TextureSolidMode(gl_texturesolidmode.string)
            gl_texturesolidmode.modified = false
        }

        /*
		** swapinterval stuff
		*/
        GL_UpdateSwapInterval()

        //
        // clear screen if desired
        //
        R_Clear()
    }

    var r_rawpalette = IntArray(256)

    /**
     * R_SetPalette
     */
    protected fun R_SetPalette(palette: ByteArray?) {
        // 256 RGB values (768 bytes)
        // or null
        var i: Int
        var color = 0

        if (palette != null) {
            var j = 0
            run {
                i = 0
                while (i < 256) {
                    color = (palette[j++] and 255) shl 0
                    color = color or ((palette[j++] and 255) shl 8)
                    color = color or ((palette[j++] and 255) shl 16)
                    color = color or -16777216
                    r_rawpalette[i] = color
                    i++
                }
            }
        } else {
            run {
                i = 0
                while (i < 256) {
                    r_rawpalette[i] = d_8to24table[i] or -16777216
                    i++
                }
            }
        }
        GL_SetTexturePalette(r_rawpalette)

        GL11.glClearColor(0, 0, 0, 0)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        GL11.glClearColor(1.toFloat(), 0.toFloat(), 0.5.toFloat(), 0.5.toFloat())
    }

    var start_points = Array<FloatArray>(NUM_BEAM_SEGS, { FloatArray(3) })
    // array of vec3_t
    var end_points = Array<FloatArray>(NUM_BEAM_SEGS, { FloatArray(3) }) // array of vec3_t

    // stack variable
    private val perpvec = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val direction = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val normalized_direction = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val oldorigin = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val origin = floatArray(0.0, 0.0, 0.0) // vec3_t
    /**
     * R_DrawBeam
     */
    fun R_DrawBeam(e: entity_t) {
        oldorigin[0] = e.oldorigin[0]
        oldorigin[1] = e.oldorigin[1]
        oldorigin[2] = e.oldorigin[2]

        origin[0] = e.origin[0]
        origin[1] = e.origin[1]
        origin[2] = e.origin[2]

        normalized_direction[0] = direction[0] = oldorigin[0] - origin[0]
        normalized_direction[1] = direction[1] = oldorigin[1] - origin[1]
        normalized_direction[2] = direction[2] = oldorigin[2] - origin[2]

        if (Math3D.VectorNormalize(normalized_direction) == 0.0.toFloat())
            return

        Math3D.PerpendicularVector(perpvec, normalized_direction)
        Math3D.VectorScale(perpvec, e.frame / 2, perpvec)

        for (i in 0..6 - 1) {
            Math3D.RotatePointAroundVector(start_points[i], normalized_direction, perpvec, (360.0.toFloat() / NUM_BEAM_SEGS.toFloat()) * i.toFloat())

            Math3D.VectorAdd(start_points[i], origin, start_points[i])
            Math3D.VectorAdd(start_points[i], direction, end_points[i])
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDepthMask(false)

        var r = ((d_8to24table[e.skinnum and 255]) and 255).toFloat()
        var g = ((d_8to24table[e.skinnum and 255] shr 8) and 255).toFloat()
        var b = ((d_8to24table[e.skinnum and 255] shr 16) and 255).toFloat()

        r *= 1 / 255.0.toFloat()
        g *= 1 / 255.0.toFloat()
        b *= 1 / 255.0.toFloat()

        GL11.glColor4f(r, g, b, e.alpha)

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)

        var v: FloatArray

        for (i in 0..NUM_BEAM_SEGS - 1) {
            v = start_points[i]
            GL11.glVertex3f(v[0], v[1], v[2])
            v = end_points[i]
            GL11.glVertex3f(v[0], v[1], v[2])
            v = start_points[(i + 1) % NUM_BEAM_SEGS]
            GL11.glVertex3f(v[0], v[1], v[2])
            v = end_points[(i + 1) % NUM_BEAM_SEGS]
            GL11.glVertex3f(v[0], v[1], v[2])
        }
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDepthMask(true)
    }

    companion object {

        public var d_8to24table: IntArray = IntArray(256)

        val NUM_BEAM_SEGS = 6
    }
}