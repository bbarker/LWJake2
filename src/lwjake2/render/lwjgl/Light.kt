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
import lwjake2.client.dlight_t
import lwjake2.game.cplane_t
import lwjake2.qcommon.Com
import lwjake2.render.mnode_t
import lwjake2.render.msurface_t
import lwjake2.render.mtexinfo_t
import lwjake2.util.Math3D
import lwjake2.util.Vec3Cache

import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.Arrays

import org.lwjgl.opengl.GL11

/**
 * Light

 * @author cwei
 */
public abstract class Light : Warp() {
    // r_light.c

    var r_dlightframecount: Int = 0

    /*
	=============================================================================

	DYNAMIC LIGHTS BLEND RENDERING

	=============================================================================
	*/

    // stack variable
    private val v = floatArray(0.0, 0.0, 0.0)

    /**
     * R_RenderDlight
     */
    fun R_RenderDlight(light: dlight_t) {
        val rad = light.intensity * 0.35.toFloat()

        Math3D.VectorSubtract(light.origin, r_origin, v)

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glColor3f(light.color[0] * 0.2.toFloat(), light.color[1] * 0.2.toFloat(), light.color[2] * 0.2.toFloat())
        var i: Int
        run {
            i = 0
            while (i < 3) {
                v[i] = light.origin[i] - vpn[i] * rad
                i++
            }
        }

        GL11.glVertex3f(v[0], v[1], v[2])
        GL11.glColor3f(0, 0, 0)

        var j: Int
        var a: Float
        run {
            i = 16
            while (i >= 0) {
                a = (i.toFloat() / 16.0.toFloat() * Math.PI * 2) as Float
                run {
                    j = 0
                    while (j < 3) {
                        v[j] = (light.origin[j] + vright[j] * Math.cos(a) * rad + vup[j] * Math.sin(a) * rad) as Float
                        j++
                    }
                }
                GL11.glVertex3f(v[0], v[1], v[2])
                i--
            }
        }
        GL11.glEnd()
    }

    /**
     * R_RenderDlights
     */
    fun R_RenderDlights() {
        if (gl_flashblend.value == 0)
            return

        r_dlightframecount = r_framecount + 1    // because the count hasn't
        //  advanced yet for this frame
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE)

        for (i in 0..r_newrefdef.num_dlights - 1) {
            R_RenderDlight(r_newrefdef.dlights[i])
        }

        GL11.glColor3f(1, 1, 1)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDepthMask(true)
    }


    /*
	=============================================================================

	DYNAMIC LIGHTS

	=============================================================================
	*/

    /**
     * R_MarkLights
     */
    fun R_MarkLights(light: dlight_t, bit: Int, node: mnode_t) {
        if (node.contents != -1)
            return

        val splitplane = node.plane
        var dist = Math3D.DotProduct(light.origin, splitplane.normal) - splitplane.dist

        if (dist > light.intensity - DLIGHT_CUTOFF) {
            R_MarkLights(light, bit, node.children[0])
            return
        }
        if (dist < -light.intensity + DLIGHT_CUTOFF) {
            R_MarkLights(light, bit, node.children[1])
            return
        }

        // mark the polygons
        val surf: msurface_t
        val sidebit: Int
        for (i in 0..node.numsurfaces - 1) {

            surf = r_worldmodel.surfaces[node.firstsurface + i]

            /*
			 * cwei
			 * bugfix for dlight behind the walls
			 */
            dist = Math3D.DotProduct(light.origin, surf.plane.normal) - surf.plane.dist
            sidebit = if ((dist >= 0)) 0 else Defines.SURF_PLANEBACK
            if ((surf.flags and Defines.SURF_PLANEBACK) != sidebit)
                continue
            /*
			 * cwei
			 * bugfix end
			 */

            if (surf.dlightframe != r_dlightframecount) {
                surf.dlightbits = 0
                surf.dlightframe = r_dlightframecount
            }
            surf.dlightbits = surf.dlightbits or bit
        }

        R_MarkLights(light, bit, node.children[0])
        R_MarkLights(light, bit, node.children[1])
    }

    /**
     * R_PushDlights
     */
    fun R_PushDlights() {
        if (gl_flashblend.value != 0)
            return

        r_dlightframecount = r_framecount + 1    // because the count hasn't
        //  advanced yet for this frame
        val l: dlight_t
        for (i in 0..r_newrefdef.num_dlights - 1) {
            l = r_newrefdef.dlights[i]
            R_MarkLights(l, 1 shl i, r_worldmodel.nodes[0])
        }
    }

    /*
	=============================================================================

	LIGHT SAMPLING

	=============================================================================
	*/

    var pointcolor = floatArray(0.0, 0.0, 0.0) // vec3_t
    var lightplane: cplane_t // used as shadow plane
    var lightspot = floatArray(0.0, 0.0, 0.0) // vec3_t

    /**
     * RecursiveLightPoint
     * @param node
    * *
     * @param start
    * *
     * @param end
    * *
     * @return
     */
    fun RecursiveLightPoint(node: mnode_t, start: FloatArray, end: FloatArray): Int {
        if (node.contents != -1)
            return -1        // didn't hit anything

        // calculate mid point

        // FIXME: optimize for axial
        val plane = node.plane
        val front = Math3D.DotProduct(start, plane.normal) - plane.dist
        val back = Math3D.DotProduct(end, plane.normal) - plane.dist
        val side = (front < 0)
        val sideIndex = if ((side)) 1 else 0

        if ((back < 0) == side)
            return RecursiveLightPoint(node.children[sideIndex], start, end)

        val frac = front / (front - back)
        val mid = Vec3Cache.get()
        mid[0] = start[0] + (end[0] - start[0]) * frac
        mid[1] = start[1] + (end[1] - start[1]) * frac
        mid[2] = start[2] + (end[2] - start[2]) * frac

        // go down front side
        var r = RecursiveLightPoint(node.children[sideIndex], start, mid)
        if (r >= 0) {
            Vec3Cache.release() // mid
            return r        // hit something
        }

        if ((back < 0) == side) {
            Vec3Cache.release() // mid
            return -1 // didn't hit anuthing
        }

        // check for impact on this node
        Math3D.VectorCopy(mid, lightspot)
        lightplane = plane
        var surfIndex = node.firstsurface

        var surf: msurface_t
        var s: Int
        var t: Int
        var ds: Int
        var dt: Int
        var tex: mtexinfo_t
        var lightmap: ByteBuffer?
        var maps: Int
        run {
            var i = 0
            while (i < node.numsurfaces) {
                surf = r_worldmodel.surfaces[surfIndex]

                if ((surf.flags and (Defines.SURF_DRAWTURB or Defines.SURF_DRAWSKY)) != 0)
                    continue    // no lightmaps

                tex = surf.texinfo

                s = (Math3D.DotProduct(mid, tex.vecs[0]) + tex.vecs[0][3]) as Int
                t = (Math3D.DotProduct(mid, tex.vecs[1]) + tex.vecs[1][3]) as Int

                if (s < surf.texturemins[0] || t < surf.texturemins[1])
                    continue

                ds = s - surf.texturemins[0]
                dt = t - surf.texturemins[1]

                if (ds > surf.extents[0] || dt > surf.extents[1])
                    continue

                if (surf.samples == null)
                    return 0

                ds = ds shr 4
                dt = dt shr 4

                lightmap = surf.samples
                var lightmapIndex = 0

                Math3D.VectorCopy(Globals.vec3_origin, pointcolor)
                if (lightmap != null) {
                    var rgb: FloatArray
                    lightmapIndex += 3 * (dt * ((surf.extents[0] shr 4) + 1) + ds)

                    var scale0: Float
                    var scale1: Float
                    var scale2: Float
                    run {
                        maps = 0
                        while (maps < Defines.MAXLIGHTMAPS && surf.styles[maps] != 255.toByte()) {
                            rgb = r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb
                            scale0 = gl_modulate.value * rgb[0]
                            scale1 = gl_modulate.value * rgb[1]
                            scale2 = gl_modulate.value * rgb[2]

                            pointcolor[0] += (lightmap!!.get(lightmapIndex + 0) and 255).toFloat() * scale0 * (1.0.toFloat() / 255)
                            pointcolor[1] += (lightmap!!.get(lightmapIndex + 1) and 255).toFloat() * scale1 * (1.0.toFloat() / 255)
                            pointcolor[2] += (lightmap!!.get(lightmapIndex + 2) and 255).toFloat() * scale2 * (1.0.toFloat() / 255)
                            lightmapIndex += 3 * ((surf.extents[0] shr 4) + 1) * ((surf.extents[1] shr 4) + 1)
                            maps++
                        }
                    }
                }
                Vec3Cache.release() // mid
                return 1
                i++
                surfIndex++
            }
        }

        // go down back side
        r = RecursiveLightPoint(node.children[1 - sideIndex], mid, end)
        Vec3Cache.release() // mid
        return r
    }

    // stack variable
    private val end = floatArray(0.0, 0.0, 0.0)

    /**
     * R_LightPoint
     */
    fun R_LightPoint(p: FloatArray, color: FloatArray) {
        assert((p.size() == 3), "vec3_t bug")
        assert((color.size() == 3), "rgb bug")

        if (r_worldmodel.lightdata == null) {
            color[0] = color[1] = color[2] = 1.0.toFloat()
            return
        }

        end[0] = p[0]
        end[1] = p[1]
        end[2] = p[2] - 2048

        val r = RecursiveLightPoint(r_worldmodel.nodes[0], p, end).toFloat()

        if (r == -1) {
            Math3D.VectorCopy(Globals.vec3_origin, color)
        } else {
            Math3D.VectorCopy(pointcolor, color)
        }

        //
        // add dynamic lights
        //
        val dl: dlight_t
        var add: Float
        for (lnum in 0..r_newrefdef.num_dlights - 1) {
            dl = r_newrefdef.dlights[lnum]

            Math3D.VectorSubtract(currententity.origin, dl.origin, end)
            add = dl.intensity - Math3D.VectorLength(end)
            add *= (1.0.toFloat() / 256)
            if (add > 0) {
                Math3D.VectorMA(color, add, dl.color, color)
            }
        }
        Math3D.VectorScale(color, gl_modulate.value, color)
    }

    //	  ===================================================================

    var s_blocklights = FloatArray(34 * 34 * 3)

    // TODO sync with jogl renderer. hoz
    private val impact = floatArray(0.0, 0.0, 0.0)

    /**
     * R_AddDynamicLights
     */
    fun R_AddDynamicLights(surf: msurface_t) {
        var sd: Int
        var td: Int
        var fdist: Float
        val frad: Float
        var fminlight: Float
        var s: Int
        var t: Int
        val dl: dlight_t
        val pfBL: FloatArray
        var fsacc: Float
        var ftacc: Float

        val smax = (surf.extents[0] shr 4) + 1
        val tmax = (surf.extents[1] shr 4) + 1
        val tex = surf.texinfo

        val local0: Float
        val local1: Float
        for (lnum in 0..r_newrefdef.num_dlights - 1) {
            if ((surf.dlightbits and (1 shl lnum)) == 0)
                continue        // not lit by this light

            dl = r_newrefdef.dlights[lnum]
            frad = dl.intensity
            fdist = Math3D.DotProduct(dl.origin, surf.plane.normal) - surf.plane.dist
            frad -= Math.abs(fdist)
            // rad is now the highest intensity on the plane

            fminlight = DLIGHT_CUTOFF.toFloat()    // FIXME: make configurable?
            if (frad < fminlight)
                continue
            fminlight = frad - fminlight

            for (i in 0..3 - 1) {
                impact[i] = dl.origin[i] - surf.plane.normal[i] * fdist
            }

            local0 = Math3D.DotProduct(impact, tex.vecs[0]) + tex.vecs[0][3] - surf.texturemins[0]
            local1 = Math3D.DotProduct(impact, tex.vecs[1]) + tex.vecs[1][3] - surf.texturemins[1]

            pfBL = s_blocklights
            var pfBLindex = 0
            run {
                t = 0
                ftacc = 0
                while (t < tmax) {
                    td = (local1 - ftacc).toInt()
                    if (td < 0)
                        td = -td

                    run {
                        s = 0
                        fsacc = 0
                        while (s < smax) {
                            sd = (local0 - fsacc).toInt()

                            if (sd < 0)
                                sd = -sd

                            if (sd > td)
                                fdist = (sd + (td shr 1)).toFloat()
                            else
                                fdist = (td + (sd shr 1)).toFloat()

                            if (fdist < fminlight) {
                                pfBL[pfBLindex + 0] += (frad - fdist) * dl.color[0]
                                pfBL[pfBLindex + 1] += (frad - fdist) * dl.color[1]
                                pfBL[pfBLindex + 2] += (frad - fdist) * dl.color[2]
                            }
                            s++
                            fsacc += 16
                            pfBLindex += 3
                        }
                    }
                    t++
                    ftacc += 16
                }
            }
        }
    }

    /**
     * R_SetCacheState
     */
    fun R_SetCacheState(surf: msurface_t) {
        run {
            var maps = 0
            while (maps < Defines.MAXLIGHTMAPS && surf.styles[maps] != 255.toByte()) {
                surf.cached_light[maps] = r_newrefdef.lightstyles[surf.styles[maps] and 255].white
                maps++
            }
        }
    }

    private val gotoStore = Throwable()

    //	TODO sync with jogl renderer. hoz
    /**
     * R_BuildLightMap

     * Combine and scale multiple lightmaps into the floating format in blocklights
     */
    fun R_BuildLightMap(surf: msurface_t, dest: IntBuffer, stride: Int) {
        var stride = stride
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        var max: Int
        var i: Int
        var j: Int
        var nummaps: Int
        var bl: FloatArray
        //lightstyle_t style;

        if ((surf.texinfo.flags and (Defines.SURF_SKY or Defines.SURF_TRANS33 or Defines.SURF_TRANS66 or Defines.SURF_WARP)) != 0)
            Com.Error(Defines.ERR_DROP, "R_BuildLightMap called for non-lit surface")

        val smax = (surf.extents[0] shr 4) + 1
        val tmax = (surf.extents[1] shr 4) + 1
        val size = smax * tmax
        if (size > ((s_blocklights.size() * Defines.SIZE_OF_FLOAT) shr 4))
            Com.Error(Defines.ERR_DROP, "Bad s_blocklights size")

        try {
            // set to full bright if no light data
            if (surf.samples == null) {
                // int maps;

                run {
                    i = 0
                    while (i < size * 3) {
                        s_blocklights[i] = 255
                        i++
                    }
                }

                // TODO useless? hoz
                //				for (maps = 0 ; maps < Defines.MAXLIGHTMAPS &&
                // surf.styles[maps] != (byte)255; maps++)
                //				{
                //					style = r_newrefdef.lightstyles[surf.styles[maps] & 0xFF];
                //				}

                // goto store;
                throw gotoStore
            }

            // count the # of maps
            run {
                nummaps = 0
                while (nummaps < Defines.MAXLIGHTMAPS && surf.styles[nummaps] != 255.toByte()) {
                    nummaps++
                }
            }

            val lightmap = surf.samples
            var lightmapIndex = 0

            // add all the lightmaps
            var scale0: Float
            var scale1: Float
            var scale2: Float
            if (nummaps == 1) {
                var maps: Int

                run {
                    maps = 0
                    while (maps < Defines.MAXLIGHTMAPS && surf.styles[maps] != 255.toByte()) {
                        bl = s_blocklights
                        var blp = 0

                        //                    for (i = 0; i < 3; i++)
                        //                        scale[i] = gl_modulate.value
                        //                                * r_newrefdef.lightstyles[surf.styles[maps] & 0xFF].rgb[i];
                        scale0 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[0]
                        scale1 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[1]
                        scale2 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[2]

                        if (scale0 == 1.0.toFloat() && scale1 == 1.0.toFloat() && scale2 == 1.0.toFloat()) {
                            run {
                                i = 0
                                while (i < size) {
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    i++
                                }
                            }
                        } else {
                            run {
                                i = 0
                                while (i < size) {
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat() * scale0
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat() * scale1
                                    bl[blp++] = (lightmap.get(lightmapIndex++) and 255).toFloat() * scale2
                                    i++
                                }
                            }
                        }
                        maps++
                        //lightmap += size*3; // skip to next lightmap
                    }
                }
            } else {
                var maps: Int

                //			memset( s_blocklights, 0, sizeof( s_blocklights[0] ) * size *
                // 3 );

                Arrays.fill(s_blocklights, 0, size * 3, 0.0.toFloat())

                run {
                    maps = 0
                    while (maps < Defines.MAXLIGHTMAPS && surf.styles[maps] != 255.toByte()) {
                        bl = s_blocklights
                        var blp = 0

                        //                    for (i = 0; i < 3; i++)
                        //                        scale[i] = gl_modulate.value
                        //                                * r_newrefdef.lightstyles[surf.styles[maps] & 0xFF].rgb[i];
                        scale0 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[0]
                        scale1 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[1]
                        scale2 = gl_modulate.value * r_newrefdef.lightstyles[surf.styles[maps] and 255].rgb[2]




                        if (scale0 == 1.0.toFloat() && scale1 == 1.0.toFloat() && scale2 == 1.0.toFloat()) {
                            run {
                                i = 0
                                while (i < size) {
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat()
                                    i++
                                }
                            }
                        } else {
                            run {
                                i = 0
                                while (i < size) {
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat() * scale0
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat() * scale1
                                    bl[blp++] += (lightmap.get(lightmapIndex++) and 255).toFloat() * scale2
                                    i++
                                }
                            }
                        }
                        maps++
                        //lightmap += size*3; // skip to next lightmap
                    }
                }
            }

            // add all the dynamic lights
            if (surf.dlightframe == r_framecount)
                R_AddDynamicLights(surf)

            // label store:
        } catch (store: Throwable) {
        }


        // put into texture format
        stride -= smax
        bl = s_blocklights
        var blp = 0

        val monolightmap = gl_monolightmap.string.charAt(0)

        var destp = 0

        if (monolightmap == '0') {
            run {
                i = 0
                while (i < tmax) {
                    //dest.position(destp);

                    run {
                        j = 0
                        while (j < smax) {

                            r = bl[blp++].toInt()
                            g = bl[blp++].toInt()
                            b = bl[blp++].toInt()

                            // catch negative lights
                            if (r < 0)
                                r = 0
                            if (g < 0)
                                g = 0
                            if (b < 0)
                                b = 0

                            /*
                     * * determine the brightest of the three color components
                     */
                            if (r > g)
                                max = r
                            else
                                max = g
                            if (b > max)
                                max = b

                            /*
                     * * alpha is ONLY used for the mono lightmap case. For this
                     * reason * we set it to the brightest of the color
                     * components so that * things don't get too dim.
                     */
                            a = max

                            /*
                     * * rescale all the color components if the intensity of
                     * the greatest * channel exceeds 1.0
                     */
                            if (max > 255) {
                                val t = 255.0.toFloat() / max.toFloat()

                                r = (r.toFloat() * t).toInt()
                                g = (g.toFloat() * t).toInt()
                                b = (b.toFloat() * t).toInt()
                                a = (a.toFloat() * t).toInt()
                            }
                            //r &= 0xFF; g &= 0xFF; b &= 0xFF; a &= 0xFF;
                            dest.put(destp++, (a shl 24) or (b shl 16) or (g shl 8) or r)
                            j++
                        }
                    }
                    i++
                    destp += stride
                }
            }
        } else {
            run {
                i = 0
                while (i < tmax) {
                    //dest.position(destp);

                    run {
                        j = 0
                        while (j < smax) {

                            r = bl[blp++].toInt()
                            g = bl[blp++].toInt()
                            b = bl[blp++].toInt()

                            // catch negative lights
                            if (r < 0)
                                r = 0
                            if (g < 0)
                                g = 0
                            if (b < 0)
                                b = 0

                            /*
                     * * determine the brightest of the three color components
                     */
                            if (r > g)
                                max = r
                            else
                                max = g
                            if (b > max)
                                max = b

                            /*
                     * * alpha is ONLY used for the mono lightmap case. For this
                     * reason * we set it to the brightest of the color
                     * components so that * things don't get too dim.
                     */
                            a = max

                            /*
                     * * rescale all the color components if the intensity of
                     * the greatest * channel exceeds 1.0
                     */
                            if (max > 255) {
                                val t = 255.0.toFloat() / max.toFloat()

                                r = (r.toFloat() * t).toInt()
                                g = (g.toFloat() * t).toInt()
                                b = (b.toFloat() * t).toInt()
                                a = (a.toFloat() * t).toInt()
                            }

                            /*
                     * * So if we are doing alpha lightmaps we need to set the
                     * R, G, and B * components to 0 and we need to set alpha to
                     * 1-alpha.
                     */
                            when (monolightmap) {
                                'L', 'I' -> {
                                    r = a
                                    g = b = 0
                                }
                                'C' -> {
                                    // try faking colored lighting
                                    a = 255 - ((r + g + b) / 3)
                                    val af = a.toFloat() / 255.0.toFloat()
                                    r *= af.toInt()
                                    g *= af.toInt()
                                    b *= af.toInt()
                                }
                                'A', else -> {
                                r = g = b = 0
                                a = 255 - a
                            }
                            }
                            //r &= 0xFF; g &= 0xFF; b &= 0xFF; a &= 0xFF;
                            dest.put(destp++, (a shl 24) or (b shl 16) or (g shl 8) or r)
                            j++
                        }
                    }
                    i++
                    destp += stride
                }
            }
        }
    }

    companion object {

        val DLIGHT_CUTOFF = 64
    }

}