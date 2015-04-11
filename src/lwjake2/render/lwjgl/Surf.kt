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
import lwjake2.client.dlight_t
import lwjake2.client.entity_t
import lwjake2.client.lightstyle_t
import lwjake2.game.cplane_t
import lwjake2.qcommon.Com
import lwjake2.render.glpoly_t
import lwjake2.render.image_t
import lwjake2.render.medge_t
import lwjake2.render.mleaf_t
import lwjake2.render.mnode_t
import lwjake2.render.model_t
import lwjake2.render.msurface_t
import lwjake2.render.mtexinfo_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.Arrays

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBMultitexture
import org.lwjgl.opengl.GL11

/**
 * Surf

 * @author cwei
 */
public abstract class Surf : Draw() {

    // GL_RSURF.C: surface-related refresh code
    var modelorg = floatArray(0.0, 0.0, 0.0)        // relative to viewpoint

    var r_alpha_surfaces: msurface_t? = null

    var c_visible_lightmaps: Int = 0
    var c_visible_textures: Int = 0

    class gllightmapstate_t {
        var internal_format: Int = 0
        var current_lightmap_texture: Int = 0

        var lightmap_surfaces = arrayOfNulls<msurface_t>(MAX_LIGHTMAPS)
        var allocated = IntArray(BLOCK_WIDTH)

        // the lightmap texture data needs to be kept in
        // main memory so texsubimage can update properly
        //byte[] lightmap_buffer = new byte[4 * BLOCK_WIDTH * BLOCK_HEIGHT];
        var lightmap_buffer = Lib.newIntBuffer(BLOCK_WIDTH * BLOCK_HEIGHT, ByteOrder.LITTLE_ENDIAN)

        {
            for (i in 0..MAX_LIGHTMAPS - 1)
                lightmap_surfaces[i] = msurface_t()
        }

        public fun clearLightmapSurfaces() {
            for (i in 0..MAX_LIGHTMAPS - 1)
            // TODO lightmap_surfaces[i].clear();
                lightmap_surfaces[i] = msurface_t()
        }

    }

    var gl_lms = gllightmapstate_t()

    // Model.java
    abstract fun Mod_ClusterPVS(cluster: Int, model: model_t): ByteArray

    // Warp.java
    abstract fun R_DrawSkyBox()

    abstract fun R_AddSkySurface(surface: msurface_t)
    abstract fun R_ClearSkyBox()
    abstract fun EmitWaterPolys(fa: msurface_t)
    // Light.java
    abstract fun R_MarkLights(light: dlight_t, bit: Int, node: mnode_t)

    abstract fun R_SetCacheState(surf: msurface_t)
    abstract fun R_BuildLightMap(surf: msurface_t, dest: IntBuffer, stride: Int)

    /*
	=============================================================

		BRUSH MODELS

	=============================================================
	*/

    /**
     * R_TextureAnimation
     * Returns the proper texture for a given time and base texture
     */
    fun R_TextureAnimation(tex: mtexinfo_t): image_t {
        var tex = tex
        if (tex.next == null)
            return tex.image

        var c = currententity.frame % tex.numframes
        while (c != 0) {
            tex = tex.next
            c--
        }

        return tex.image
    }

    /**
     * DrawGLPoly
     */
    fun DrawGLPoly(p: glpoly_t) {
        GL11.glDrawArrays(GL11.GL_POLYGON, p.pos, p.numverts)
    }

    /**
     * DrawGLFlowingPoly
     * version that handles scrolling texture
     */
    fun DrawGLFlowingPoly(p: glpoly_t) {
        var scroll = -64 * ((r_newrefdef.time / 40.0.toFloat()) - (r_newrefdef.time / 40.0.toFloat()) as Int)
        if (scroll == 0.0.toFloat())
            scroll = -64.0.toFloat()
        p.beginScrolling(scroll)
        GL11.glDrawArrays(GL11.GL_POLYGON, p.pos, p.numverts)
        p.endScrolling()
    }

    /**
     * R_DrawTriangleOutlines
     */
    fun R_DrawTriangleOutlines() {
        if (gl_showtris.value == 0)
            return

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glColor4f(1, 1, 1, 1)

        var surf: msurface_t?
        var p: glpoly_t?
        var j: Int
        for (i in 0..MAX_LIGHTMAPS - 1) {
            run {
                surf = gl_lms.lightmap_surfaces[i]
                while (surf != null) {
                    run {
                        p = surf!!.polys
                        while (p != null) {
                            run {
                                j = 2
                                while (j < p!!.numverts) {
                                    GL11.glBegin(GL11.GL_LINE_STRIP)
                                    GL11.glVertex3f(p!!.x(0), p!!.y(0), p!!.z(0))
                                    GL11.glVertex3f(p!!.x(j - 1), p!!.y(j - 1), p!!.z(j - 1))
                                    GL11.glVertex3f(p!!.x(j), p!!.y(j), p!!.z(j))
                                    GL11.glVertex3f(p!!.x(0), p!!.y(0), p!!.z(0))
                                    GL11.glEnd()
                                    j++
                                }
                            }
                            p = p!!.chain
                        }
                    }
                    surf = surf!!.lightmapchain
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    private val temp2 = Lib.newIntBuffer(34 * 34, ByteOrder.LITTLE_ENDIAN)

    /**
     * R_RenderBrushPoly
     */
    fun R_RenderBrushPoly(fa: msurface_t) {
        c_brush_polys++

        val image = R_TextureAnimation(fa.texinfo)

        if ((fa.flags and Defines.SURF_DRAWTURB) != 0) {
            GL_Bind(image.texnum)

            // warp texture, no lightmaps
            GL_TexEnv(GL11.GL_MODULATE)
            GL11.glColor4f(gl_state.inverse_intensity, gl_state.inverse_intensity, gl_state.inverse_intensity, 1.0.toFloat())
            EmitWaterPolys(fa)
            GL_TexEnv(GL11.GL_REPLACE)

            return
        } else {
            GL_Bind(image.texnum)
            GL_TexEnv(GL11.GL_REPLACE)
        }

        //	  ======
        //	  PGM
        if ((fa.texinfo.flags and Defines.SURF_FLOWING) != 0)
            DrawGLFlowingPoly(fa.polys)
        else
            DrawGLPoly(fa.polys)
        //	  PGM
        //	  ======

        // ersetzt goto
        var gotoDynamic = false
        /*
		** check for lightmap modification
		*/
        var maps: Int
        run {
            maps = 0
            while (maps < Defines.MAXLIGHTMAPS && fa.styles[maps] != 255.toByte()) {
                if (r_newrefdef.lightstyles[fa.styles[maps] and 255].white != fa.cached_light[maps]) {
                    gotoDynamic = true
                    break
                }
                maps++
            }
        }

        // this is a hack from cwei
        if (maps == 4) maps--

        // dynamic this frame or dynamic previously
        var is_dynamic = false
        if (gotoDynamic || (fa.dlightframe == r_framecount)) {
            //	label dynamic:
            if (gl_dynamic.value != 0) {
                if ((fa.texinfo.flags and (Defines.SURF_SKY or Defines.SURF_TRANS33 or Defines.SURF_TRANS66 or Defines.SURF_WARP)) == 0) {
                    is_dynamic = true
                }
            }
        }

        if (is_dynamic) {
            if (((fa.styles[maps] and 255) >= 32 || fa.styles[maps] == 0) && (fa.dlightframe != r_framecount)) {
                // ist ersetzt durch temp2:	unsigned	temp[34*34];
                val smax: Int
                val tmax: Int

                smax = (fa.extents[0] shr 4) + 1
                tmax = (fa.extents[1] shr 4) + 1

                R_BuildLightMap(fa, temp2, smax)
                R_SetCacheState(fa)

                GL_Bind(gl_state.lightmap_textures + fa.lightmaptexturenum)

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, fa.light_s, fa.light_t, smax, tmax, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, temp2)

                fa.lightmapchain = gl_lms.lightmap_surfaces[fa.lightmaptexturenum]
                gl_lms.lightmap_surfaces[fa.lightmaptexturenum] = fa
            } else {
                fa.lightmapchain = gl_lms.lightmap_surfaces[0]
                gl_lms.lightmap_surfaces[0] = fa
            }
        } else {
            fa.lightmapchain = gl_lms.lightmap_surfaces[fa.lightmaptexturenum]
            gl_lms.lightmap_surfaces[fa.lightmaptexturenum] = fa
        }
    }


    /**
     * R_DrawAlphaSurfaces
     * Draw water surfaces and windows.
     * The BSP tree is waled front to back, so unwinding the chain
     * of alpha_surfaces will draw back to front, giving proper ordering.
     */
    fun R_DrawAlphaSurfaces() {
        r_world_matrix.clear()
        //
        // go back to the world matrix
        //
        GL11.glLoadMatrix(r_world_matrix)

        GL11.glEnable(GL11.GL_BLEND)
        GL_TexEnv(GL11.GL_MODULATE)


        // the textures are prescaled up for a better lighting range,
        // so scale it back down
        val intens = gl_state.inverse_intensity

        GL11.glInterleavedArrays(GL11.GL_T2F_V3F, Polygon.BYTE_STRIDE, globalPolygonInterleavedBuf)

        run {
            var s = r_alpha_surfaces
            while (s != null) {
                GL_Bind(s!!.texinfo.image.texnum)
                c_brush_polys++
                if ((s!!.texinfo.flags and Defines.SURF_TRANS33) != 0)
                    GL11.glColor4f(intens, intens, intens, 0.33.toFloat())
                else if ((s!!.texinfo.flags and Defines.SURF_TRANS66) != 0)
                    GL11.glColor4f(intens, intens, intens, 0.66.toFloat())
                else
                    GL11.glColor4f(intens, intens, intens, 1)
                if ((s!!.flags and Defines.SURF_DRAWTURB) != 0)
                    EmitWaterPolys(s)
                else if ((s!!.texinfo.flags and Defines.SURF_FLOWING) != 0)
                // PGM	9/16/98
                    DrawGLFlowingPoly(s!!.polys)                            // PGM
                else
                    DrawGLPoly(s!!.polys)
                s = s!!.texturechain
            }
        }

        GL_TexEnv(GL11.GL_REPLACE)
        GL11.glColor4f(1, 1, 1, 1)
        GL11.glDisable(GL11.GL_BLEND)

        r_alpha_surfaces = null
    }

    /**
     * DrawTextureChains
     */
    fun DrawTextureChains() {
        c_visible_textures = 0

        var s: msurface_t?
        var image: image_t
        var i: Int
        run {
            i = 0
            while (i < numgltextures) {
                image = gltextures[i]

                if (image.registration_sequence == 0)
                    continue
                if (image.texturechain == null)
                    continue
                c_visible_textures++

                run {
                    s = image.texturechain
                    while (s != null) {
                        if ((s!!.flags and Defines.SURF_DRAWTURB) == 0)
                            R_RenderBrushPoly(s)
                        s = s!!.texturechain
                    }
                }
                i++
            }
        }

        GL_EnableMultitexture(false)
        run {
            i = 0
            while (i < numgltextures) {
                image = gltextures[i]

                if (image.registration_sequence == 0)
                    continue
                s = image.texturechain
                if (s == null)
                    continue

                while (s != null) {
                    if ((s!!.flags and Defines.SURF_DRAWTURB) != 0)
                        R_RenderBrushPoly(s)
                    s = s!!.texturechain
                }

                image.texturechain = null
                i++
            }
        }

        GL_TexEnv(GL11.GL_REPLACE)
    }

    // direct buffer
    private val temp = Lib.newIntBuffer(128 * 128, ByteOrder.LITTLE_ENDIAN)

    /**
     * GL_RenderLightmappedPoly
     * @param surf
     */
    fun GL_RenderLightmappedPoly(surf: msurface_t) {

        // ersetzt goto
        var gotoDynamic = false
        var map: Int
        run {
            map = 0
            while (map < Defines.MAXLIGHTMAPS && (surf.styles[map] != 255.toByte())) {
                if (r_newrefdef.lightstyles[surf.styles[map] and 255].white != surf.cached_light[map]) {
                    gotoDynamic = true
                    break
                }
                map++
            }
        }

        // this is a hack from cwei
        if (map == 4) map--

        // dynamic this frame or dynamic previously
        var is_dynamic = false
        if (gotoDynamic || (surf.dlightframe == r_framecount)) {
            //	label dynamic:
            if (gl_dynamic.value != 0) {
                if ((surf.texinfo.flags and (Defines.SURF_SKY or Defines.SURF_TRANS33 or Defines.SURF_TRANS66 or Defines.SURF_WARP)) == 0) {
                    is_dynamic = true
                }
            }
        }

        var p: glpoly_t?
        val image = R_TextureAnimation(surf.texinfo)
        var lmtex = surf.lightmaptexturenum

        if (is_dynamic) {
            // ist raus gezogen worden int[] temp = new int[128*128];
            val smax: Int
            val tmax: Int

            if (((surf.styles[map] and 255) >= 32 || surf.styles[map] == 0) && (surf.dlightframe != r_framecount)) {
                smax = (surf.extents[0] shr 4) + 1
                tmax = (surf.extents[1] shr 4) + 1

                R_BuildLightMap(surf, temp, smax)
                R_SetCacheState(surf)

                GL_MBind(GL_TEXTURE1, gl_state.lightmap_textures + surf.lightmaptexturenum)

                lmtex = surf.lightmaptexturenum

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, surf.light_s, surf.light_t, smax, tmax, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, temp)

            } else {
                smax = (surf.extents[0] shr 4) + 1
                tmax = (surf.extents[1] shr 4) + 1

                R_BuildLightMap(surf, temp, smax)

                GL_MBind(GL_TEXTURE1, gl_state.lightmap_textures + 0)

                lmtex = 0

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, surf.light_s, surf.light_t, smax, tmax, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, temp)

            }

            c_brush_polys++

            GL_MBind(GL_TEXTURE0, image.texnum)
            GL_MBind(GL_TEXTURE1, gl_state.lightmap_textures + lmtex)

            // ==========
            //	  PGM
            if ((surf.texinfo.flags and Defines.SURF_FLOWING) != 0) {
                var scroll: Float

                scroll = -64 * ((r_newrefdef.time / 40.0.toFloat()) - (r_newrefdef.time / 40.0.toFloat()) as Int)
                if (scroll == 0.0.toFloat())
                    scroll = -64.0.toFloat()

                run {
                    p = surf.polys
                    while (p != null) {
                        p!!.beginScrolling(scroll)
                        GL11.glDrawArrays(GL11.GL_POLYGON, p!!.pos, p!!.numverts)
                        p!!.endScrolling()
                        p = p!!.chain
                    }
                }
            } else {
                run {
                    p = surf.polys
                    while (p != null) {
                        GL11.glDrawArrays(GL11.GL_POLYGON, p!!.pos, p!!.numverts)
                        p = p!!.chain
                    }
                }
            }
            // PGM
            // ==========
        } else {
            c_brush_polys++

            GL_MBind(GL_TEXTURE0, image.texnum)
            GL_MBind(GL_TEXTURE1, gl_state.lightmap_textures + lmtex)

            // ==========
            //	  PGM
            if ((surf.texinfo.flags and Defines.SURF_FLOWING) != 0) {
                var scroll: Float

                scroll = -64 * ((r_newrefdef.time / 40.0.toFloat()) - (r_newrefdef.time / 40.0.toFloat()) as Int)
                if (scroll == 0.0)
                    scroll = -64.0.toFloat()

                run {
                    p = surf.polys
                    while (p != null) {
                        p!!.beginScrolling(scroll)
                        GL11.glDrawArrays(GL11.GL_POLYGON, p!!.pos, p!!.numverts)
                        p!!.endScrolling()
                        p = p!!.chain
                    }
                }
            } else {
                // PGM
                //  ==========
                run {
                    p = surf.polys
                    while (p != null) {
                        GL11.glDrawArrays(GL11.GL_POLYGON, p!!.pos, p!!.numverts)
                        p = p!!.chain
                    }
                }

                // ==========
                // PGM
            }
            // PGM
            // ==========
        }
    }

    /**
     * R_DrawInlineBModel
     */
    fun R_DrawInlineBModel() {
        // calculate dynamic lighting for bmodel
        if (gl_flashblend.value == 0) {
            val lt: dlight_t
            for (k in 0..r_newrefdef.num_dlights - 1) {
                lt = r_newrefdef.dlights[k]
                R_MarkLights(lt, 1 shl k, currentmodel.nodes[currentmodel.firstnode])
            }
        }

        // psurf = &currentmodel->surfaces[currentmodel->firstmodelsurface];
        var psurfp = currentmodel.firstmodelsurface
        val surfaces = currentmodel.surfaces
        //psurf = surfaces[psurfp];

        if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1, 1, 1, 0.25.toFloat())
            GL_TexEnv(GL11.GL_MODULATE)
        }

        //
        // draw texture
        //
        val psurf: msurface_t
        val pplane: cplane_t
        val dot: Float
        for (i in 0..currentmodel.nummodelsurfaces - 1) {
            psurf = surfaces[psurfp++]
            // find which side of the node we are on
            pplane = psurf.plane

            dot = Math3D.DotProduct(modelorg, pplane.normal) - pplane.dist

            // draw the polygon
            if (((psurf.flags and Defines.SURF_PLANEBACK) != 0 && (dot < -BACKFACE_EPSILON)) || ((psurf.flags and Defines.SURF_PLANEBACK) == 0 && (dot > BACKFACE_EPSILON))) {
                if ((psurf.texinfo.flags and (Defines.SURF_TRANS33 or Defines.SURF_TRANS66)) != 0) {
                    // add to the translucent chain
                    psurf.texturechain = r_alpha_surfaces
                    r_alpha_surfaces = psurf
                } else if ((psurf.flags and Defines.SURF_DRAWTURB) == 0) {
                    GL_RenderLightmappedPoly(psurf)
                } else {
                    GL_EnableMultitexture(false)
                    R_RenderBrushPoly(psurf)
                    GL_EnableMultitexture(true)
                }
            }
        }

        if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0) {
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glColor4f(1, 1, 1, 1)
            GL_TexEnv(GL11.GL_REPLACE)
        }
    }

    // stack variable
    private val mins = floatArray(0.0, 0.0, 0.0)
    private val maxs = floatArray(0.0, 0.0, 0.0)
    private val org = floatArray(0.0, 0.0, 0.0)
    private val forward = floatArray(0.0, 0.0, 0.0)
    private val right = floatArray(0.0, 0.0, 0.0)
    private val up = floatArray(0.0, 0.0, 0.0)
    /**
     * R_DrawBrushModel
     */
    fun R_DrawBrushModel(e: entity_t) {
        if (currentmodel.nummodelsurfaces == 0)
            return

        currententity = e
        gl_state.currenttextures[0] = gl_state.currenttextures[1] = -1

        val rotated: Boolean
        if (e.angles[0] != 0 || e.angles[1] != 0 || e.angles[2] != 0) {
            rotated = true
            for (i in 0..3 - 1) {
                mins[i] = e.origin[i] - currentmodel.radius
                maxs[i] = e.origin[i] + currentmodel.radius
            }
        } else {
            rotated = false
            Math3D.VectorAdd(e.origin, currentmodel.mins, mins)
            Math3D.VectorAdd(e.origin, currentmodel.maxs, maxs)
        }

        if (R_CullBox(mins, maxs)) return

        GL11.glColor3f(1, 1, 1)

        // memset (gl_lms.lightmap_surfaces, 0, sizeof(gl_lms.lightmap_surfaces));

        // TODO wird beim multitexturing nicht gebraucht
        //gl_lms.clearLightmapSurfaces();

        Math3D.VectorSubtract(r_newrefdef.vieworg, e.origin, modelorg)
        if (rotated) {
            Math3D.VectorCopy(modelorg, org)
            Math3D.AngleVectors(e.angles, forward, right, up)
            modelorg[0] = Math3D.DotProduct(org, forward)
            modelorg[1] = -Math3D.DotProduct(org, right)
            modelorg[2] = Math3D.DotProduct(org, up)
        }

        GL11.glPushMatrix()

        e.angles[0] = -e.angles[0]    // stupid quake bug
        e.angles[2] = -e.angles[2]    // stupid quake bug
        R_RotateForEntity(e)
        e.angles[0] = -e.angles[0]    // stupid quake bug
        e.angles[2] = -e.angles[2]    // stupid quake bug

        GL_EnableMultitexture(true)
        GL_SelectTexture(GL_TEXTURE0)
        GL_TexEnv(GL11.GL_REPLACE)
        GL11.glInterleavedArrays(GL11.GL_T2F_V3F, Polygon.BYTE_STRIDE, globalPolygonInterleavedBuf)
        GL_SelectTexture(GL_TEXTURE1)
        GL_TexEnv(GL11.GL_MODULATE)
        GL11.glTexCoordPointer(2, Polygon.BYTE_STRIDE, globalPolygonTexCoord1Buf)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)

        R_DrawInlineBModel()

        ARBMultitexture.glClientActiveTextureARB(GL_TEXTURE1)
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)

        GL_EnableMultitexture(false)

        GL11.glPopMatrix()
    }

    /*
	=============================================================

		WORLD MODEL

	=============================================================
	*/

    /**
     * R_RecursiveWorldNode
     */
    fun R_RecursiveWorldNode(node: mnode_t) {
        if (node.contents == Defines.CONTENTS_SOLID)
            return        // solid

        if (node.visframe != r_visframecount)
            return

        if (R_CullBox(node.mins, node.maxs))
            return

        var c: Int
        var mark: msurface_t
        // if a leaf node, draw stuff
        if (node.contents != -1) {
            val pleaf = node as mleaf_t

            // check for door connected areas
            if (r_newrefdef.areabits != null) {
                if (((r_newrefdef.areabits[pleaf.area shr 3] and 255) and (1 shl (pleaf.area and 7))) == 0)
                    return        // not visible
            }

            var markp = 0

            mark = pleaf.getMarkSurface(markp) // first marked surface
            c = pleaf.nummarksurfaces

            if (c != 0) {
                do {
                    mark.visframe = r_framecount
                    mark = pleaf.getMarkSurface(++markp) // next surface
                } while (--c != 0)
            }

            return
        }

        // node is just a decision point, so go down the apropriate sides

        // find which side of the node we are on
        val plane = node.plane
        val dot: Float
        when (plane.type) {
            Defines.PLANE_X -> dot = modelorg[0] - plane.dist
            Defines.PLANE_Y -> dot = modelorg[1] - plane.dist
            Defines.PLANE_Z -> dot = modelorg[2] - plane.dist
            else -> dot = Math3D.DotProduct(modelorg, plane.normal) - plane.dist
        }

        val side: Int
        val sidebit: Int
        if (dot >= 0.0.toFloat()) {
            side = 0
            sidebit = 0
        } else {
            side = 1
            sidebit = Defines.SURF_PLANEBACK
        }

        // recurse down the children, front side first
        R_RecursiveWorldNode(node.children[side])

        // draw stuff
        var surf: msurface_t
        var image: image_t
        //for ( c = node.numsurfaces, surf = r_worldmodel.surfaces[node.firstsurface]; c != 0 ; c--, surf++)
        run {
            c = 0
            while (c < node.numsurfaces) {
                surf = r_worldmodel.surfaces[node.firstsurface + c]
                if (surf.visframe != r_framecount)
                    continue

                if ((surf.flags and Defines.SURF_PLANEBACK) != sidebit)
                    continue        // wrong side

                if ((surf.texinfo.flags and Defines.SURF_SKY) != 0) {
                    // just adds to visible sky bounds
                    R_AddSkySurface(surf)
                } else if ((surf.texinfo.flags and (Defines.SURF_TRANS33 or Defines.SURF_TRANS66)) != 0) {
                    // add to the translucent chain
                    surf.texturechain = r_alpha_surfaces
                    r_alpha_surfaces = surf
                } else {
                    if ((surf.flags and Defines.SURF_DRAWTURB) == 0) {
                        GL_RenderLightmappedPoly(surf)
                    } else {
                        // the polygon is visible, so add it to the texture
                        // sorted chain
                        // FIXME: this is a hack for animation
                        image = R_TextureAnimation(surf.texinfo)
                        surf.texturechain = image.texturechain
                        image.texturechain = surf
                    }
                }
                c++
            }
        }
        // recurse down the back side
        R_RecursiveWorldNode(node.children[1 - side])
    }

    private val worldEntity = entity_t()

    /**
     * R_DrawWorld
     */
    fun R_DrawWorld() {
        if (r_drawworld.value == 0)
            return

        if ((r_newrefdef.rdflags and Defines.RDF_NOWORLDMODEL) != 0)
            return

        currentmodel = r_worldmodel

        Math3D.VectorCopy(r_newrefdef.vieworg, modelorg)

        val ent = worldEntity
        // auto cycle the world frame for texture animation
        ent.clear()
        ent.frame = (r_newrefdef.time * 2) as Int
        currententity = ent

        gl_state.currenttextures[0] = gl_state.currenttextures[1] = -1

        GL11.glColor3f(1, 1, 1)
        // memset (gl_lms.lightmap_surfaces, 0, sizeof(gl_lms.lightmap_surfaces));
        // TODO wird bei multitexture nicht gebraucht
        //gl_lms.clearLightmapSurfaces();

        R_ClearSkyBox()

        GL_EnableMultitexture(true)

        GL_SelectTexture(GL_TEXTURE0)
        GL_TexEnv(GL11.GL_REPLACE)
        GL11.glInterleavedArrays(GL11.GL_T2F_V3F, Polygon.BYTE_STRIDE, globalPolygonInterleavedBuf)
        GL_SelectTexture(GL_TEXTURE1)
        GL11.glTexCoordPointer(2, Polygon.BYTE_STRIDE, globalPolygonTexCoord1Buf)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)

        if (gl_lightmap.value != 0)
            GL_TexEnv(GL11.GL_REPLACE)
        else
            GL_TexEnv(GL11.GL_MODULATE)

        R_RecursiveWorldNode(r_worldmodel.nodes[0]) // root node

        ARBMultitexture.glClientActiveTextureARB(GL_TEXTURE1)
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)

        GL_EnableMultitexture(false)

        DrawTextureChains()
        R_DrawSkyBox()
        R_DrawTriangleOutlines()
    }

    val fatvis = ByteArray(Defines.MAX_MAP_LEAFS / 8)

    /**
     * R_MarkLeaves
     * Mark the leaves and nodes that are in the PVS for the current
     * cluster
     */
    fun R_MarkLeaves() {
        if (r_oldviewcluster == r_viewcluster && r_oldviewcluster2 == r_viewcluster2 && r_novis.value == 0 && r_viewcluster != -1)
            return

        // development aid to let you run around and see exactly where
        // the pvs ends
        if (gl_lockpvs.value != 0)
            return

        r_visframecount++
        r_oldviewcluster = r_viewcluster
        r_oldviewcluster2 = r_viewcluster2

        var i: Int
        if (r_novis.value != 0 || r_viewcluster == -1 || r_worldmodel.vis == null) {
            // mark everything
            run {
                i = 0
                while (i < r_worldmodel.numleafs) {
                    r_worldmodel.leafs[i].visframe = r_visframecount
                    i++
                }
            }
            run {
                i = 0
                while (i < r_worldmodel.numnodes) {
                    r_worldmodel.nodes[i].visframe = r_visframecount
                    i++
                }
            }
            return
        }

        var vis = Mod_ClusterPVS(r_viewcluster, r_worldmodel)
        var c: Int
        // may have to combine two clusters because of solid water boundaries
        if (r_viewcluster2 != r_viewcluster) {
            // memcpy (fatvis, vis, (r_worldmodel.numleafs+7)/8);
            System.arraycopy(vis, 0, fatvis, 0, (r_worldmodel.numleafs + 7) shr 3)
            vis = Mod_ClusterPVS(r_viewcluster2, r_worldmodel)
            c = (r_worldmodel.numleafs + 31) shr 5
            c = c shl 2
            run {
                var k = 0
                while (k < c) {
                    fatvis[k] = fatvis[k] or vis[k]
                    fatvis[k + 1] = fatvis[k + 1] or vis[k + 1]
                    fatvis[k + 2] = fatvis[k + 2] or vis[k + 2]
                    fatvis[k + 3] = fatvis[k + 3] or vis[k + 3]
                    k += 4
                }
            }

            vis = fatvis
        }

        var node: mnode_t?
        var leaf: mleaf_t
        var cluster: Int
        run {
            i = 0
            while (i < r_worldmodel.numleafs) {
                leaf = r_worldmodel.leafs[i]
                cluster = leaf.cluster
                if (cluster == -1)
                    continue
                if (((vis[cluster shr 3] and 255) and (1 shl (cluster and 7))) != 0) {
                    node = leaf as mnode_t
                    do {
                        if (node!!.visframe == r_visframecount)
                            break
                        node!!.visframe = r_visframecount
                        node = node!!.parent
                    } while (node != null)
                }
                i++
            }
        }
    }

    /*
	=============================================================================

	  LIGHTMAP ALLOCATION

	=============================================================================
	*/

    /**
     * LM_InitBlock
     */
    fun LM_InitBlock() {
        Arrays.fill(gl_lms.allocated, 0)
    }

    /**
     * LM_UploadBlock
     * @param dynamic
     */
    fun LM_UploadBlock(dynamic: Boolean) {
        val texture = if ((dynamic)) 0 else gl_lms.current_lightmap_texture

        GL_Bind(gl_state.lightmap_textures + texture)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

        gl_lms.lightmap_buffer.rewind()
        if (dynamic) {
            var height = 0
            for (i in 0..BLOCK_WIDTH - 1) {
                if (gl_lms.allocated[i] > height)
                    height = gl_lms.allocated[i]
            }

            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, BLOCK_WIDTH, height, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, gl_lms.lightmap_buffer)
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, gl_lms.internal_format, BLOCK_WIDTH, BLOCK_HEIGHT, 0, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, gl_lms.lightmap_buffer)
            if (++gl_lms.current_lightmap_texture == MAX_LIGHTMAPS)
                Com.Error(Defines.ERR_DROP, "LM_UploadBlock() - MAX_LIGHTMAPS exceeded\n")

            //debugLightmap(gl_lms.lightmap_buffer, 128, 128, 4);
        }
    }

    /**
     * LM_AllocBlock
     * @param w
    * *
     * @param h
    * *
     * @param pos
    * *
     * @return a texture number and the position inside it
     */
    fun LM_AllocBlock(w: Int, h: Int, pos: pos_t): Boolean {
        var best = BLOCK_HEIGHT

        var best2: Int
        var i: Int
        var j: Int
        run {
            i = 0
            while (i < BLOCK_WIDTH - w) {
                best2 = 0

                run {
                    j = 0
                    while (j < w) {
                        if (gl_lms.allocated[i + j] >= best)
                            break
                        if (gl_lms.allocated[i + j] > best2)
                            best2 = gl_lms.allocated[i + j]
                        j++
                    }
                }
                if (j == w) {
                    // this is a valid spot
                    pos.x = i
                    pos.y = best = best2
                }
                i++
            }
        }

        if (best + h > BLOCK_HEIGHT)
            return false

        run {
            i = 0
            while (i < w) {
                gl_lms.allocated[pos.x + i] = best + h
                i++
            }
        }

        return true
    }

    /**
     * GL_BuildPolygonFromSurface
     */
    fun GL_BuildPolygonFromSurface(fa: msurface_t) {
        // reconstruct the polygon
        val pedges = currentmodel.edges
        val lnumverts = fa.numedges
        //
        // draw texture
        //
        // poly = Hunk_Alloc (sizeof(glpoly_t) + (lnumverts-4) * VERTEXSIZE*sizeof(float));
        val poly = Polygon.create(lnumverts)

        poly.next = fa.polys
        poly.flags = fa.flags
        fa.polys = poly

        val lindex: Int
        val vec: FloatArray
        val r_pedge: medge_t
        var s: Float
        var t: Float
        for (i in 0..lnumverts - 1) {
            lindex = currentmodel.surfedges[fa.firstedge + i]

            if (lindex > 0) {
                r_pedge = pedges[lindex]
                vec = currentmodel.vertexes[r_pedge.v[0]].position
            } else {
                r_pedge = pedges[-lindex]
                vec = currentmodel.vertexes[r_pedge.v[1]].position
            }
            s = Math3D.DotProduct(vec, fa.texinfo.vecs[0]) + fa.texinfo.vecs[0][3]
            s /= fa.texinfo.image.width

            t = Math3D.DotProduct(vec, fa.texinfo.vecs[1]) + fa.texinfo.vecs[1][3]
            t /= fa.texinfo.image.height

            poly.x(i, vec[0])
            poly.y(i, vec[1])
            poly.z(i, vec[2])

            poly.s1(i, s)
            poly.t1(i, t)

            //
            // lightmap texture coordinates
            //
            s = Math3D.DotProduct(vec, fa.texinfo.vecs[0]) + fa.texinfo.vecs[0][3]
            s -= fa.texturemins[0]
            s += fa.light_s * 16
            s += 8
            s /= (BLOCK_WIDTH * 16).toFloat() //fa.texinfo.texture.width;

            t = Math3D.DotProduct(vec, fa.texinfo.vecs[1]) + fa.texinfo.vecs[1][3]
            t -= fa.texturemins[1]
            t += fa.light_t * 16
            t += 8
            t /= (BLOCK_HEIGHT * 16).toFloat() //fa.texinfo.texture.height;

            poly.s2(i, s)
            poly.t2(i, t)
        }
    }

    /**
     * GL_CreateSurfaceLightmap
     */
    fun GL_CreateSurfaceLightmap(surf: msurface_t) {
        if ((surf.flags and (Defines.SURF_DRAWSKY or Defines.SURF_DRAWTURB)) != 0)
            return

        val smax = (surf.extents[0] shr 4) + 1
        val tmax = (surf.extents[1] shr 4) + 1

        var lightPos = pos_t(surf.light_s, surf.light_t)

        if (!LM_AllocBlock(smax, tmax, lightPos)) {
            LM_UploadBlock(false)
            LM_InitBlock()
            lightPos = pos_t(surf.light_s, surf.light_t)
            if (!LM_AllocBlock(smax, tmax, lightPos)) {
                Com.Error(Defines.ERR_FATAL, "Consecutive calls to LM_AllocBlock(" + smax + "," + tmax + ") failed\n")
            }
        }

        // kopiere die koordinaten zurueck
        surf.light_s = lightPos.x
        surf.light_t = lightPos.y

        surf.lightmaptexturenum = gl_lms.current_lightmap_texture

        val base = gl_lms.lightmap_buffer
        base.position(surf.light_t * BLOCK_WIDTH + surf.light_s)

        R_SetCacheState(surf)
        R_BuildLightMap(surf, base.slice(), BLOCK_WIDTH)
    }

    var lightstyles: Array<lightstyle_t>? = null
    private val dummy = BufferUtils.createIntBuffer(128 * 128)

    /**
     * GL_BeginBuildingLightmaps
     */
    fun GL_BeginBuildingLightmaps(m: model_t) {
        // static lightstyle_t	lightstyles[MAX_LIGHTSTYLES];
        var i: Int

        // init lightstyles
        if (lightstyles == null) {
            lightstyles = arrayOfNulls<lightstyle_t>(Defines.MAX_LIGHTSTYLES)
            run {
                i = 0
                while (i < lightstyles!!.size()) {
                    lightstyles[i] = lightstyle_t()
                    i++
                }
            }
        }

        // memset( gl_lms.allocated, 0, sizeof(gl_lms.allocated) );
        Arrays.fill(gl_lms.allocated, 0)

        r_framecount = 1        // no dlightcache

        GL_EnableMultitexture(true)
        GL_SelectTexture(GL_TEXTURE1)

        /*
		** setup the base lightstyles so the lightmaps won't have to be regenerated
		** the first time they're seen
		*/
        run {
            i = 0
            while (i < Defines.MAX_LIGHTSTYLES) {
                lightstyles!![i].rgb[0] = 1
                lightstyles!![i].rgb[1] = 1
                lightstyles!![i].rgb[2] = 1
                lightstyles!![i].white = 3
                i++
            }
        }
        r_newrefdef.lightstyles = lightstyles

        if (gl_state.lightmap_textures == 0) {
            gl_state.lightmap_textures = TEXNUM_LIGHTMAPS
        }

        gl_lms.current_lightmap_texture = 1

        /*
		** if mono lightmaps are enabled and we want to use alpha
		** blending (a,1-a) then we're likely running on a 3DLabs
		** Permedia2.  In a perfect world we'd use a GL_ALPHA lightmap
		** in order to conserve space and maximize bandwidth, however 
		** this isn't a perfect world.
		**
		** So we have to use alpha lightmaps, but stored in GL_RGBA format,
		** which means we only get 1/16th the color resolution we should when
		** using alpha lightmaps.  If we find another board that supports
		** only alpha lightmaps but that can at least support the GL_ALPHA
		** format then we should change this code to use real alpha maps.
		*/

        val format = gl_monolightmap.string.toUpperCase().charAt(0)

        if (format == 'A') {
            gl_lms.internal_format = gl_tex_alpha_format
        } else if (format == 'C') {
            gl_lms.internal_format = gl_tex_alpha_format
        } else if (format == 'I') {
            gl_lms.internal_format = GL11.GL_INTENSITY8
        } else if (format == 'L') {
            gl_lms.internal_format = GL11.GL_LUMINANCE8
        } else {
            gl_lms.internal_format = gl_tex_solid_format
        }/*
		** try to do hacked colored lighting with a blended texture
		*/

        /*
		** initialize the dynamic lightmap texture
		*/
        GL_Bind(gl_state.lightmap_textures + 0)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, gl_lms.internal_format, BLOCK_WIDTH, BLOCK_HEIGHT, 0, GL_LIGHTMAP_FORMAT, GL11.GL_UNSIGNED_BYTE, dummy)
    }

    /**
     * GL_EndBuildingLightmaps
     */
    fun GL_EndBuildingLightmaps() {
        LM_UploadBlock(false)
        GL_EnableMultitexture(false)
    }

    companion object {

        val DYNAMIC_LIGHT_WIDTH = 128
        val DYNAMIC_LIGHT_HEIGHT = 128

        val LIGHTMAP_BYTES = 4

        val BLOCK_WIDTH = 128
        val BLOCK_HEIGHT = 128

        val MAX_LIGHTMAPS = 128

        val GL_LIGHTMAP_FORMAT = GL11.GL_RGBA

        /*
	 * new buffers for vertex array handling
	 */
        var globalPolygonInterleavedBuf = Polygon.getInterleavedBuffer()
        var globalPolygonTexCoord1Buf: FloatBuffer? = null

        {
            globalPolygonInterleavedBuf.position(Polygon.STRIDE - 2)
            globalPolygonTexCoord1Buf = globalPolygonInterleavedBuf.slice()
            globalPolygonInterleavedBuf.position(0)
        }
    }

    //ImageFrame frame;

    //	void debugLightmap(byte[] buf, int w, int h, float scale) {
    //		IntBuffer pix = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
    //
    //		int[] pixel = new int[w * h];
    //
    //		pix.get(pixel);
    //
    //		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
    //		image.setRGB(0,  0, w, h, pixel, 0, w);
    //		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    //		BufferedImage tmp = op.filter(image, null);
    //
    //		if (frame == null) {
    //			frame = new ImageFrame(null);
    //			frame.show();
    //		}
    //		frame.showImage(tmp);
    //
    //	}

}
