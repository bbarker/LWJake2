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
import lwjake2.client.VID
import lwjake2.client.entity_t
import lwjake2.qcommon.qfiles
import lwjake2.render.image_t
import lwjake2.util.Math3D

import java.nio.FloatBuffer
import java.nio.IntBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBMultitexture
import org.lwjgl.opengl.GL11

/**
 * Mesh

 * @author cwei
 */
public abstract class Mesh : Light() {

    var r_avertexnormals = Anorms.VERTEXNORMALS
    var shadevector = floatArray(0.0, 0.0, 0.0)
    var shadelight = floatArray(0.0, 0.0, 0.0)

    var r_avertexnormal_dots = Anorms.VERTEXNORMAL_DOTS

    var shadedots = r_avertexnormal_dots[0]

    /**
     * GL_LerpVerts
     * @param nverts
    * *
     * @param ov
    * *
     * @param verts
    * *
     * @param move
    * *
     * @param frontv
    * *
     * @param backv
     */
    fun GL_LerpVerts(nverts: Int, ov: IntArray, v: IntArray, move: FloatArray, frontv: FloatArray, backv: FloatArray) {
        val lerp = vertexArrayBuf
        lerp.limit((nverts shl 2) - nverts) // nverts * 3

        val ovv: Int
        val vv: Int
        //PMM -- added RF_SHELL_DOUBLE, RF_SHELL_HALF_DAM
        if ((currententity.flags and (Defines.RF_SHELL_RED or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_BLUE or Defines.RF_SHELL_DOUBLE or Defines.RF_SHELL_HALF_DAM)) != 0) {
            val normal: FloatArray
            var j = 0
            for (i in 0..nverts - 1) {
                vv = v[i]
                normal = r_avertexnormals[(vv.ushr(24)) and 255]
                ovv = ov[i]
                lerp.put(j, move[0] + (ovv and 255).toFloat() * backv[0] + (vv and 255).toFloat() * frontv[0] + normal[0] * Defines.POWERSUIT_SCALE)
                lerp.put(j + 1, move[1] + ((ovv.ushr(8)) and 255).toFloat() * backv[1] + ((vv.ushr(8)) and 255).toFloat() * frontv[1] + normal[1] * Defines.POWERSUIT_SCALE)
                lerp.put(j + 2, move[2] + ((ovv.ushr(16)) and 255).toFloat() * backv[2] + ((vv.ushr(16)) and 255).toFloat() * frontv[2] + normal[2] * Defines.POWERSUIT_SCALE)
                j += 3
            }/* , v++, ov++, lerp+=4 */
        } else {
            var j = 0
            for (i in 0..nverts - 1) {
                ovv = ov[i]
                vv = v[i]

                lerp.put(j, move[0] + (ovv and 255).toFloat() * backv[0] + (vv and 255).toFloat() * frontv[0])
                lerp.put(j + 1, move[1] + ((ovv.ushr(8)) and 255).toFloat() * backv[1] + ((vv.ushr(8)) and 255).toFloat() * frontv[1])
                lerp.put(j + 2, move[2] + ((ovv.ushr(16)) and 255).toFloat() * backv[2] + ((vv.ushr(16)) and 255).toFloat() * frontv[2])
                j += 3
            }/* , v++, ov++, lerp+=4 */
        }
    }

    var colorArrayBuf = BufferUtils.createFloatBuffer(qfiles.MAX_VERTS * 4)
    var vertexArrayBuf = BufferUtils.createFloatBuffer(qfiles.MAX_VERTS * 3)
    var textureArrayBuf = BufferUtils.createFloatBuffer(qfiles.MAX_VERTS * 2)
    var isFilled = false
    var tmpVec = floatArray(0.0, 0.0, 0.0)
    var vectors = array<FloatArray>(floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0) // 3 mal vec3_t
            .toFloat())

    // stack variable
    private val move = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val frontv = floatArray(0.0, 0.0, 0.0) // vec3_t
    private val backv = floatArray(0.0, 0.0, 0.0) // vec3_t
    /**
     * GL_DrawAliasFrameLerp

     * interpolates between two frames and origins
     * FIXME: batch lerp all vertexes
     */
    fun GL_DrawAliasFrameLerp(paliashdr: qfiles.dmdl_t, backlerp: Float) {
        val frame = paliashdr.aliasFrames[currententity.frame]

        val verts = frame.verts

        val oldframe = paliashdr.aliasFrames[currententity.oldframe]

        val ov = oldframe.verts

        val alpha: Float
        if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0)
            alpha = currententity.alpha
        else
            alpha = 1.0.toFloat()

        // PMM - added double shell
        if ((currententity.flags and (Defines.RF_SHELL_RED or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_BLUE or Defines.RF_SHELL_DOUBLE or Defines.RF_SHELL_HALF_DAM)) != 0)
            GL11.glDisable(GL11.GL_TEXTURE_2D)

        val frontlerp = 1.0.toFloat() - backlerp

        // move should be the delta back to the previous frame * backlerp
        Math3D.VectorSubtract(currententity.oldorigin, currententity.origin, frontv)
        Math3D.AngleVectors(currententity.angles, vectors[0], vectors[1], vectors[2])

        move[0] = Math3D.DotProduct(frontv, vectors[0])    // forward
        move[1] = -Math3D.DotProduct(frontv, vectors[1])    // left
        move[2] = Math3D.DotProduct(frontv, vectors[2])    // up

        Math3D.VectorAdd(move, oldframe.translate, move)

        for (i in 0..3 - 1) {
            move[i] = backlerp * move[i] + frontlerp * frame.translate[i]
            frontv[i] = frontlerp * frame.scale[i]
            backv[i] = backlerp * oldframe.scale[i]
        }

        // ab hier wird optimiert

        GL_LerpVerts(paliashdr.num_xyz, ov, verts, move, frontv, backv)

        //GL11.glEnableClientState( GL11.GL_VERTEX_ARRAY );
        GL11.glVertexPointer(3, 0, vertexArrayBuf)

        // PMM - added double damage shell
        if ((currententity.flags and (Defines.RF_SHELL_RED or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_BLUE or Defines.RF_SHELL_DOUBLE or Defines.RF_SHELL_HALF_DAM)) != 0) {
            GL11.glColor4f(shadelight[0], shadelight[1], shadelight[2], alpha)
        } else {
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY)
            GL11.glColorPointer(4, 0, colorArrayBuf)

            //
            // pre light everything
            //
            val color = colorArrayBuf
            val l: Float
            val size = paliashdr.num_xyz
            var j = 0
            for (i in 0..size - 1) {
                l = shadedots[(verts[i].ushr(24)) and 255]
                color.put(j, l * shadelight[0])
                color.put(j + 1, l * shadelight[1])
                color.put(j + 2, l * shadelight[2])
                color.put(j + 3, alpha)
                j += 4
            }
        }

        ARBMultitexture.glClientActiveTextureARB(GL_TEXTURE0)
        GL11.glTexCoordPointer(2, 0, textureArrayBuf)
        //GL11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY);

        var pos = 0
        val counts = paliashdr.counts

        var srcIndexBuf: IntBuffer? = null

        val dstTextureCoords = textureArrayBuf
        val srcTextureCoords = paliashdr.textureCoordBuf

        var dstIndex = 0
        var srcIndex = 0
        var count: Int
        var mode: Int
        val size = counts.size()
        for (j in 0..size - 1) {

            // get the vertex count and primitive type
            count = counts[j]
            if (count == 0)
                break        // done

            srcIndexBuf = paliashdr.indexElements[j]

            mode = GL11.GL_TRIANGLE_STRIP
            if (count < 0) {
                mode = GL11.GL_TRIANGLE_FAN
                count = -count
            }
            srcIndex = pos shl 1
            srcIndex--
            for (k in 0..count - 1) {
                dstIndex = srcIndexBuf!!.get(k) shl 1
                dstTextureCoords.put(dstIndex, srcTextureCoords.get(++srcIndex))
                dstTextureCoords.put(++dstIndex, srcTextureCoords.get(++srcIndex))
            }

            GL11.glDrawElements(mode, srcIndexBuf)
            pos += count
        }

        // PMM - added double damage shell
        if ((currententity.flags and (Defines.RF_SHELL_RED or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_BLUE or Defines.RF_SHELL_DOUBLE or Defines.RF_SHELL_HALF_DAM)) != 0)
            GL11.glEnable(GL11.GL_TEXTURE_2D)

        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY)
    }

    private val point = floatArray(0.0, 0.0, 0.0)
    /**
     * GL_DrawAliasShadow
     */
    fun GL_DrawAliasShadow(paliashdr: qfiles.dmdl_t, posenum: Int) {
        val lheight = currententity.origin[2] - lightspot[2]
        // qfiles.daliasframe_t frame = paliashdr.aliasFrames[currententity.frame];
        val order = paliashdr.glCmds
        val height = -lheight + 1.0.toFloat()

        var orderIndex = 0
        var index = 0

        // TODO shadow drawing with vertex arrays

        var count: Int
        while (true) {
            // get the vertex count and primitive type
            count = order[orderIndex++]
            if (count == 0)
                break        // done
            if (count < 0) {
                count = -count
                GL11.glBegin(GL11.GL_TRIANGLE_FAN)
            } else
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP)

            do {
                index = order[orderIndex + 2] * 3
                point[0] = vertexArrayBuf.get(index)
                point[1] = vertexArrayBuf.get(index + 1)
                point[2] = vertexArrayBuf.get(index + 2)

                point[0] -= shadevector[0] * (point[2] + lheight)
                point[1] -= shadevector[1] * (point[2] + lheight)
                point[2] = height
                GL11.glVertex3f(point[0], point[1], point[2])

                orderIndex += 3

            } while (--count != 0)

            GL11.glEnd()
        }
    }

    //	TODO sync with jogl renderer. hoz
    // stack variable
    private val mins = floatArray(0.0, 0.0, 0.0)
    private val maxs = floatArray(0.0, 0.0, 0.0)
    /**
     * R_CullAliasModel
     */
    fun R_CullAliasModel(e: entity_t): Boolean {
        val paliashdr = currentmodel.extradata as qfiles.dmdl_t

        if ((e.frame >= paliashdr.num_frames) || (e.frame < 0)) {
            VID.Printf(Defines.PRINT_ALL, "R_CullAliasModel " + currentmodel.name + ": no such frame " + e.frame + '\n')
            e.frame = 0
        }
        if ((e.oldframe >= paliashdr.num_frames) || (e.oldframe < 0)) {
            VID.Printf(Defines.PRINT_ALL, "R_CullAliasModel " + currentmodel.name + ": no such oldframe " + e.oldframe + '\n')
            e.oldframe = 0
        }

        val pframe = paliashdr.aliasFrames[e.frame]
        val poldframe = paliashdr.aliasFrames[e.oldframe]

        /*
		** compute axially aligned mins and maxs
		*/
        if (pframe == poldframe) {
            for (i in 0..3 - 1) {
                mins[i] = pframe.translate[i]
                maxs[i] = mins[i] + pframe.scale[i] * 255
            }
        } else {
            val thismaxs: Float
            val oldmaxs: Float
            for (i in 0..3 - 1) {
                thismaxs = pframe.translate[i] + pframe.scale[i] * 255

                oldmaxs = poldframe.translate[i] + poldframe.scale[i] * 255

                if (pframe.translate[i] < poldframe.translate[i])
                    mins[i] = pframe.translate[i]
                else
                    mins[i] = poldframe.translate[i]

                if (thismaxs > oldmaxs)
                    maxs[i] = thismaxs
                else
                    maxs[i] = oldmaxs
            }
        }

        /*
		** compute a full bounding box
		*/
        val tmp: FloatArray
        for (i in 0..8 - 1) {
            tmp = bbox[i]
            if ((i and 1) != 0)
                tmp[0] = mins[0]
            else
                tmp[0] = maxs[0]

            if ((i and 2) != 0)
                tmp[1] = mins[1]
            else
                tmp[1] = maxs[1]

            if ((i and 4) != 0)
                tmp[2] = mins[2]
            else
                tmp[2] = maxs[2]
        }

        /*
		** rotate the bounding box
		*/
        tmp = mins
        Math3D.VectorCopy(e.angles, tmp)
        tmp[YAW] = -tmp[YAW]
        Math3D.AngleVectors(tmp, vectors[0], vectors[1], vectors[2])

        for (i in 0..8 - 1) {
            Math3D.VectorCopy(bbox[i], tmp)

            bbox[i][0] = Math3D.DotProduct(vectors[0], tmp)
            bbox[i][1] = -Math3D.DotProduct(vectors[1], tmp)
            bbox[i][2] = Math3D.DotProduct(vectors[2], tmp)

            Math3D.VectorAdd(e.origin, bbox[i], bbox[i])
        }

        var f: Int
        var mask: Int
        var aggregatemask = 0.inv() // 0xFFFFFFFF

        for (p in 0..8 - 1) {
            mask = 0

            run {
                f = 0
                while (f < 4) {
                    val dp = Math3D.DotProduct(frustum[f].normal, bbox[p])

                    if ((dp - frustum[f].dist) < 0) {
                        mask = mask or (1 shl f)
                    }
                    f++
                }
            }

            aggregatemask = aggregatemask and mask
        }

        if (aggregatemask != 0) {
            return true
        }

        return false
    }


    // bounding box
    var bbox = array<FloatArray>(floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat(), floatArray(0.0, 0.0, 0.0).toFloat())

    //	TODO sync with jogl renderer. hoz
    /**
     * R_DrawAliasModel
     */
    fun R_DrawAliasModel(e: entity_t) {
        if ((e.flags and Defines.RF_WEAPONMODEL) == 0) {
            if (R_CullAliasModel(e))
                return
        }

        if ((e.flags and Defines.RF_WEAPONMODEL) != 0) {
            if (r_lefthand.value == 2.0.toFloat())
                return
        }

        val paliashdr = currentmodel.extradata as qfiles.dmdl_t

        //
        // get lighting information
        //
        // PMM - rewrote, reordered to handle new shells & mixing
        // PMM - 3.20 code .. replaced with original way of doing it to keep mod authors happy
        //
        var i: Int
        if ((currententity.flags and (Defines.RF_SHELL_HALF_DAM or Defines.RF_SHELL_GREEN or Defines.RF_SHELL_RED or Defines.RF_SHELL_BLUE or Defines.RF_SHELL_DOUBLE)) != 0) {
            Math3D.VectorClear(shadelight)
            if ((currententity.flags and Defines.RF_SHELL_HALF_DAM) != 0) {
                shadelight[0] = 0.56.toFloat()
                shadelight[1] = 0.59.toFloat()
                shadelight[2] = 0.45.toFloat()
            }
            if ((currententity.flags and Defines.RF_SHELL_DOUBLE) != 0) {
                shadelight[0] = 0.9.toFloat()
                shadelight[1] = 0.7.toFloat()
            }
            if ((currententity.flags and Defines.RF_SHELL_RED) != 0)
                shadelight[0] = 1.0.toFloat()
            if ((currententity.flags and Defines.RF_SHELL_GREEN) != 0)
                shadelight[1] = 1.0.toFloat()
            if ((currententity.flags and Defines.RF_SHELL_BLUE) != 0)
                shadelight[2] = 1.0.toFloat()
        } else if ((currententity.flags and Defines.RF_FULLBRIGHT) != 0) {
            run {
                i = 0
                while (i < 3) {
                    shadelight[i] = 1.0.toFloat()
                    i++
                }
            }
        } else {
            R_LightPoint(currententity.origin, shadelight)

            // player lighting hack for communication back to server
            // big hack!
            if ((currententity.flags and Defines.RF_WEAPONMODEL) != 0) {
                // pick the greatest component, which should be the same
                // as the mono value returned by software
                if (shadelight[0] > shadelight[1]) {
                    if (shadelight[0] > shadelight[2])
                        r_lightlevel.value = 150 * shadelight[0]
                    else
                        r_lightlevel.value = 150 * shadelight[2]
                } else {
                    if (shadelight[1] > shadelight[2])
                        r_lightlevel.value = 150 * shadelight[1]
                    else
                        r_lightlevel.value = 150 * shadelight[2]
                }
            }

            if (gl_monolightmap.string.charAt(0) != '0') {
                var s = shadelight[0]

                if (s < shadelight[1])
                    s = shadelight[1]
                if (s < shadelight[2])
                    s = shadelight[2]

                shadelight[0] = s
                shadelight[1] = s
                shadelight[2] = s
            }
        }

        if ((currententity.flags and Defines.RF_MINLIGHT) != 0) {
            run {
                i = 0
                while (i < 3) {
                    if (shadelight[i] > 0.1.toFloat())
                        break
                    i++
                }
            }
            if (i == 3) {
                shadelight[0] = 0.1.toFloat()
                shadelight[1] = 0.1.toFloat()
                shadelight[2] = 0.1.toFloat()
            }
        }

        if ((currententity.flags and Defines.RF_GLOW) != 0) {
            // bonus items will pulse with time
            val scale: Float
            var min: Float

            scale = (0.1.toFloat() * Math.sin(r_newrefdef.time * 7)) as Float
            run {
                i = 0
                while (i < 3) {
                    min = shadelight[i] * 0.8.toFloat()
                    shadelight[i] += scale
                    if (shadelight[i] < min)
                        shadelight[i] = min
                    i++
                }
            }
        }

        // =================
        // PGM	ir goggles color override
        if ((r_newrefdef.rdflags and Defines.RDF_IRGOGGLES) != 0 && (currententity.flags and Defines.RF_IR_VISIBLE) != 0) {
            shadelight[0] = 1.0.toFloat()
            shadelight[1] = 0.0.toFloat()
            shadelight[2] = 0.0.toFloat()
        }
        // PGM
        // =================

        shadedots = r_avertexnormal_dots[((currententity.angles[1] * (SHADEDOT_QUANT.toDouble() / 360.0)) as Int) and (SHADEDOT_QUANT - 1)]

        val an = (currententity.angles[1] / 180 * Math.PI) as Float
        shadevector[0] = Math.cos(-an) as Float
        shadevector[1] = Math.sin(-an) as Float
        shadevector[2] = 1
        Math3D.VectorNormalize(shadevector)

        //
        // locate the proper data
        //

        c_alias_polys += paliashdr.num_tris

        //
        // draw all the triangles
        //
        if ((currententity.flags and Defines.RF_DEPTHHACK) != 0)
        // hack the depth range to prevent view model from poking into walls
            GL11.glDepthRange(gldepthmin, gldepthmin + 0.3 * (gldepthmax - gldepthmin))

        if ((currententity.flags and Defines.RF_WEAPONMODEL) != 0 && (r_lefthand.value == 1.0.toFloat())) {
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glScalef(-1, 1, 1)
            MYgluPerspective(r_newrefdef.fov_y, r_newrefdef.width as Float / r_newrefdef.height, 4, 4096)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)

            GL11.glCullFace(GL11.GL_BACK)
        }

        GL11.glPushMatrix()
        e.angles[PITCH] = -e.angles[PITCH]    // sigh.
        R_RotateForEntity(e)
        e.angles[PITCH] = -e.angles[PITCH]    // sigh.


        var skin: image_t?
        // select skin
        if (currententity.skin != null)
            skin = currententity.skin    // custom player skin
        else {
            if (currententity.skinnum >= qfiles.MAX_MD2SKINS)
                skin = currentmodel.skins[0]
            else {
                skin = currentmodel.skins[currententity.skinnum]
                if (skin == null)
                    skin = currentmodel.skins[0]
            }
        }
        if (skin == null)
            skin = r_notexture    // fallback...
        GL_Bind(skin!!.texnum)

        // draw it

        GL11.glShadeModel(GL11.GL_SMOOTH)

        GL_TexEnv(GL11.GL_MODULATE)
        if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0) {
            GL11.glEnable(GL11.GL_BLEND)
        }


        if ((currententity.frame >= paliashdr.num_frames) || (currententity.frame < 0)) {
            VID.Printf(Defines.PRINT_ALL, "R_DrawAliasModel " + currentmodel.name + ": no such frame " + currententity.frame + '\n')
            currententity.frame = 0
            currententity.oldframe = 0
        }

        if ((currententity.oldframe >= paliashdr.num_frames) || (currententity.oldframe < 0)) {
            VID.Printf(Defines.PRINT_ALL, "R_DrawAliasModel " + currentmodel.name + ": no such oldframe " + currententity.oldframe + '\n')
            currententity.frame = 0
            currententity.oldframe = 0
        }

        if (r_lerpmodels.value == 0.0.toFloat())
            currententity.backlerp = 0

        GL_DrawAliasFrameLerp(paliashdr, currententity.backlerp)

        GL_TexEnv(GL11.GL_REPLACE)
        GL11.glShadeModel(GL11.GL_FLAT)

        GL11.glPopMatrix()

        if ((currententity.flags and Defines.RF_WEAPONMODEL) != 0 && (r_lefthand.value == 1.0.toFloat())) {
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glCullFace(GL11.GL_FRONT)
        }

        if ((currententity.flags and Defines.RF_TRANSLUCENT) != 0) {
            GL11.glDisable(GL11.GL_BLEND)
        }

        if ((currententity.flags and Defines.RF_DEPTHHACK) != 0)
            GL11.glDepthRange(gldepthmin, gldepthmax)

        if (gl_shadows.value != 0.0.toFloat() && (currententity.flags and (Defines.RF_TRANSLUCENT or Defines.RF_WEAPONMODEL)) == 0) {
            GL11.glPushMatrix()
            R_RotateForEntity(e)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(0, 0, 0, 0.5.toFloat())
            GL_DrawAliasShadow(paliashdr, currententity.frame)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
        }
        GL11.glColor4f(1, 1, 1, 1)
    }

    companion object {

        // g_mesh.c: triangle model functions
        /*
	=============================================================

	  ALIAS MODELS

	=============================================================
	*/

        val NUMVERTEXNORMALS = 162

        // precalculated dot products for quantized angles
        val SHADEDOT_QUANT = 16
    }
}