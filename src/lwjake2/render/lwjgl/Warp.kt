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
import lwjake2.qcommon.Com
import lwjake2.render.glpoly_t
import lwjake2.render.image_t
import lwjake2.render.msurface_t
import lwjake2.util.Math3D
import lwjake2.util.Vec3Cache

import org.lwjgl.opengl.GL11

/**
 * Warp

 * @author cwei
 */
public abstract class Warp : Model() {

    var skyname: String
    var skyrotate: Float = 0.toFloat()
    var skyaxis = floatArray(0.0, 0.0, 0.0)
    var sky_images = arrayOfNulls<image_t>(6)

    var warpface: msurface_t

    /**
     * BoundPoly
     * @param numverts
    * *
     * @param verts
    * *
     * @param mins
    * *
     * @param maxs
     */
    fun BoundPoly(numverts: Int, verts: Array<FloatArray>, mins: FloatArray, maxs: FloatArray) {
        mins[0] = mins[1] = mins[2] = 9999
        maxs[0] = maxs[1] = maxs[2] = (-9999).toFloat()

        var j: Int
        val v: FloatArray
        for (i in 0..numverts - 1) {
            v = verts[i]
            run {
                j = 0
                while (j < 3) {
                    if (v[j] < mins[j])
                        mins[j] = v[j]
                    if (v[j] > maxs[j])
                        maxs[j] = v[j]
                    j++
                }
            }
        }
    }

    /**
     * SubdividePolygon
     * @param numverts
    * *
     * @param verts
     */
    fun SubdividePolygon(numverts: Int, verts: Array<FloatArray>) {
        var i: Int
        var j: Int
        var k: Int
        var m: Float
        val front = Array<FloatArray>(64, { FloatArray(3) })
        val back = Array<FloatArray>(64, { FloatArray(3) })

        var f: Int
        var b: Int
        val dist = FloatArray(64)
        var frac: Float

        if (numverts > 60)
            Com.Error(Defines.ERR_DROP, "numverts = " + numverts)

        val mins = Vec3Cache.get()
        val maxs = Vec3Cache.get()

        BoundPoly(numverts, verts, mins, maxs)
        var v: FloatArray
        // x,y und z
        run {
            i = 0
            while (i < 3) {
                m = (mins[i] + maxs[i]) * 0.5.toFloat()
                m = SUBDIVIDE_SIZE.toFloat() * Math.floor(m / SUBDIVIDE_SIZE.toFloat() + 0.5.toFloat()) as Float
                if (maxs[i] - m < 8)
                    continue
                if (m - mins[i] < 8)
                    continue

                // cut it
                run {
                    j = 0
                    while (j < numverts) {
                        dist[j] = verts[j][i] - m
                        j++
                    }
                }

                // wrap cases
                dist[j] = dist[0]

                Math3D.VectorCopy(verts[0], verts[numverts])

                f = b = 0
                run {
                    j = 0
                    while (j < numverts) {
                        v = verts[j]
                        if (dist[j] >= 0) {
                            Math3D.VectorCopy(v, front[f])
                            f++
                        }
                        if (dist[j] <= 0) {
                            Math3D.VectorCopy(v, back[b])
                            b++
                        }
                        if (dist[j] == 0 || dist[j + 1] == 0) continue

                        if ((dist[j] > 0) != (dist[j + 1] > 0)) {
                            // clip point
                            frac = dist[j] / (dist[j] - dist[j + 1])
                            run {
                                k = 0
                                while (k < 3) {
                                    front[f][k] = back[b][k] = v[k] + frac * (verts[j + 1][k] - v[k])
                                    k++
                                }
                            }

                            f++
                            b++
                        }
                        j++
                    }
                }

                SubdividePolygon(f, front)
                SubdividePolygon(b, back)

                Vec3Cache.release(2) // mins, maxs
                return
                i++
            }
        }

        Vec3Cache.release(2) // mins, maxs

        // add a point in the center to help keep warp valid

        // wird im Konstruktor erschlagen
        // poly = Hunk_Alloc (sizeof(glpoly_t) + ((numverts-4)+2) * VERTEXSIZE*sizeof(float));

        // init polys
        val poly = Polygon.create(numverts + 2)

        poly.next = warpface.polys
        warpface.polys = poly

        val total = Vec3Cache.get()
        Math3D.VectorClear(total)
        var total_s: Float = 0
        var total_t: Float = 0
        var s: Float
        var t: Float
        run {
            i = 0
            while (i < numverts) {
                poly.x(i + 1, verts[i][0])
                poly.y(i + 1, verts[i][1])
                poly.z(i + 1, verts[i][2])
                s = Math3D.DotProduct(verts[i], warpface.texinfo.vecs[0])
                t = Math3D.DotProduct(verts[i], warpface.texinfo.vecs[1])

                total_s += s
                total_t += t
                Math3D.VectorAdd(total, verts[i], total)

                poly.s1(i + 1, s)
                poly.t1(i + 1, t)
                i++
            }
        }

        val scale = 1.0.toFloat() / numverts.toFloat()
        poly.x(0, total[0] * scale)
        poly.y(0, total[1] * scale)
        poly.z(0, total[2] * scale)
        poly.s1(0, total_s * scale)
        poly.t1(0, total_t * scale)

        poly.x(i + 1, poly.x(1))
        poly.y(i + 1, poly.y(1))
        poly.z(i + 1, poly.z(1))
        poly.s1(i + 1, poly.s1(1))
        poly.t1(i + 1, poly.t1(1))
        poly.s2(i + 1, poly.s2(1))
        poly.t2(i + 1, poly.t2(1))

        Vec3Cache.release() // total
    }

    private val tmpVerts = Array<FloatArray>(64, { FloatArray(3) })
    /**
     * GL_SubdivideSurface
     * Breaks a polygon up along axial 64 unit
     * boundaries so that turbulent and sky warps
     * can be done reasonably.
     */
    fun GL_SubdivideSurface(fa: msurface_t) {
        val verts = tmpVerts
        val vec: FloatArray
        warpface = fa
        //
        // convert edges back to a normal polygon
        //
        var numverts = 0
        for (i in 0..fa.numedges - 1) {
            val lindex = loadmodel.surfedges[fa.firstedge + i]

            if (lindex > 0)
                vec = loadmodel.vertexes[loadmodel.edges[lindex].v[0]].position
            else
                vec = loadmodel.vertexes[loadmodel.edges[-lindex].v[1]].position
            Math3D.VectorCopy(vec, verts[numverts])
            numverts++
        }
        SubdividePolygon(numverts, verts)
    }

    /**
     * EmitWaterPolys
     * Does a water warp on the pre-fragmented glpoly_t chain
     */
    fun EmitWaterPolys(fa: msurface_t) {
        val rdt = r_newrefdef.time

        val scroll: Float
        if ((fa.texinfo.flags and Defines.SURF_FLOWING) != 0)
            scroll = -64 * ((r_newrefdef.time * 0.5.toFloat()) - (r_newrefdef.time * 0.5.toFloat()) as Int)
        else
            scroll = 0

        var i: Int
        var s: Float
        var t: Float
        var os: Float
        var ot: Float
        var p: glpoly_t
        var bp: glpoly_t?
        run {
            bp = fa.polys
            while (bp != null) {
                p = bp

                GL11.glBegin(GL11.GL_TRIANGLE_FAN)
                run {
                    i = 0
                    while (i < p.numverts) {
                        os = p.s1(i)
                        ot = p.t1(i)

                        s = os + Warp.SIN[((ot * 0.125.toFloat() + r_newrefdef.time) * TURBSCALE) as Int and 255]
                        s += scroll
                        s *= (1.0.toFloat() / 64)

                        t = ot + Warp.SIN[((os * 0.125.toFloat() + rdt) * TURBSCALE).toInt() and 255]
                        t *= (1.0.toFloat() / 64)

                        GL11.glTexCoord2f(s, t)
                        GL11.glVertex3f(p.x(i), p.y(i), p.z(i))
                        i++
                    }
                }
                GL11.glEnd()
                bp = bp!!.next
            }
        }
    }

    //	  ===================================================================

    var skyclip = array<FloatArray>(floatArray(1.0, 1.0, 0.0).toFloat(), floatArray(1.0, (-1).toFloat(), 0.0).toFloat(), floatArray(0.0, (-1).toFloat(), 1.0).toFloat(), floatArray(0.0, 1.0, 1.0).toFloat(), floatArray(1.0, 0.0, 1.0).toFloat(), floatArray((-1).toFloat(), 0.0, 1.0).toFloat())

    var c_sky: Int = 0

    // 1 = s, 2 = t, 3 = 2048
    var st_to_vec = array<IntArray>(intArray(3, -1, 2), intArray(-3, 1, 2),
            intArray(1, 3, 2), intArray(-1, -3, 2),
            intArray(-2, -1, 3), // 0 degrees yaw, look straight up
            intArray(2, -1, -3)        // look straight down
    )

    var vec_to_st = array<IntArray>(intArray(-2, 3, 1), intArray(2, 3, -1),
            intArray(1, 3, 2), intArray(-1, 3, -2),
            intArray(-2, -1, 3), intArray(-2, 1, -3))

    var skymins = Array<FloatArray>(2, { FloatArray(6) })
    var skymaxs = Array<FloatArray>(2, { FloatArray(6) })
    var sky_min: Float = 0.toFloat()
    var sky_max: Float = 0.toFloat()

    // stack variable
    private val v = floatArray(0.0, 0.0, 0.0)
    private val av = floatArray(0.0, 0.0, 0.0)
    /**
     * DrawSkyPolygon
     * @param nump
    * *
     * @param vecs
     */
    fun DrawSkyPolygon(nump: Int, vecs: Array<FloatArray>) {
        c_sky++
        // decide which face it maps to
        Math3D.VectorCopy(Globals.vec3_origin, v)
        var i: Int
        val axis: Int
        run {
            i = 0
            while (i < nump) {
                Math3D.VectorAdd(vecs[i], v, v)
                i++
            }
        }
        av[0] = Math.abs(v[0])
        av[1] = Math.abs(v[1])
        av[2] = Math.abs(v[2])
        if (av[0] > av[1] && av[0] > av[2]) {
            if (v[0] < 0)
                axis = 1
            else
                axis = 0
        } else if (av[1] > av[2] && av[1] > av[0]) {
            if (v[1] < 0)
                axis = 3
            else
                axis = 2
        } else {
            if (v[2] < 0)
                axis = 5
            else
                axis = 4
        }

        // project new texture coords
        var s: Float
        var t: Float
        var dv: Float
        var j: Int
        run {
            i = 0
            while (i < nump) {
                j = vec_to_st[axis][2]
                if (j > 0)
                    dv = vecs[i][j - 1]
                else
                    dv = -vecs[i][-j - 1]
                if (dv < 0.001.toFloat())
                    continue    // don't divide by zero
                j = vec_to_st[axis][0]
                if (j < 0)
                    s = -vecs[i][-j - 1] / dv
                else
                    s = vecs[i][j - 1] / dv
                j = vec_to_st[axis][1]
                if (j < 0)
                    t = -vecs[i][-j - 1] / dv
                else
                    t = vecs[i][j - 1] / dv

                if (s < skymins[0][axis])
                    skymins[0][axis] = s
                if (t < skymins[1][axis])
                    skymins[1][axis] = t
                if (s > skymaxs[0][axis])
                    skymaxs[0][axis] = s
                if (t > skymaxs[1][axis])
                    skymaxs[1][axis] = t
                i++
            }
        }
    }

    var dists = FloatArray(MAX_CLIP_VERTS)
    var sides = IntArray(MAX_CLIP_VERTS)
    var newv = Array<Array<Array<FloatArray>>>(6, { Array<Array<FloatArray>>(2, { Array<FloatArray>(MAX_CLIP_VERTS, { FloatArray(3) }) }) })

    /**
     * ClipSkyPolygon
     * @param nump
    * *
     * @param vecs
    * *
     * @param stage
     */
    fun ClipSkyPolygon(nump: Int, vecs: Array<FloatArray>, stage: Int) {
        if (nump > MAX_CLIP_VERTS - 2)
            Com.Error(Defines.ERR_DROP, "ClipSkyPolygon: MAX_CLIP_VERTS")
        if (stage == 6) {
            // fully clipped, so draw it
            DrawSkyPolygon(nump, vecs)
            return
        }

        var front = false
        var back = false
        val norm = skyclip[stage]

        var i: Int
        var d: Float
        run {
            i = 0
            while (i < nump) {
                d = Math3D.DotProduct(vecs[i], norm)
                if (d > ON_EPSILON) {
                    front = true
                    sides[i] = SIDE_FRONT
                } else if (d < -ON_EPSILON) {
                    back = true
                    sides[i] = SIDE_BACK
                } else
                    sides[i] = SIDE_ON
                dists[i] = d
                i++
            }
        }

        if (!front || !back) {
            // not clipped
            ClipSkyPolygon(nump, vecs, stage + 1)
            return
        }

        // clip it
        sides[i] = sides[0]
        dists[i] = dists[0]
        Math3D.VectorCopy(vecs[0], vecs[i])

        var newc0 = 0
        var newc1 = 0
        var v: FloatArray
        var e: Float
        var j: Int
        run {
            i = 0
            while (i < nump) {
                v = vecs[i]
                when (sides[i]) {
                    SIDE_FRONT -> {
                        Math3D.VectorCopy(v, newv[stage][0][newc0])
                        newc0++
                    }
                    SIDE_BACK -> {
                        Math3D.VectorCopy(v, newv[stage][1][newc1])
                        newc1++
                    }
                    SIDE_ON -> {
                        Math3D.VectorCopy(v, newv[stage][0][newc0])
                        newc0++
                        Math3D.VectorCopy(v, newv[stage][1][newc1])
                        newc1++
                    }
                }

                if (sides[i] == SIDE_ON || sides[i + 1] == SIDE_ON || sides[i + 1] == sides[i])
                    continue

                d = dists[i] / (dists[i] - dists[i + 1])
                run {
                    j = 0
                    while (j < 3) {
                        e = v[j] + d * (vecs[i + 1][j] - v[j])
                        newv[stage][0][newc0][j] = e
                        newv[stage][1][newc1][j] = e
                        j++
                    }
                }
                newc0++
                newc1++
                i++
            }
        }

        // continue
        ClipSkyPolygon(newc0, newv[stage][0], stage + 1)
        ClipSkyPolygon(newc1, newv[stage][1], stage + 1)
    }

    var verts = Array<FloatArray>(MAX_CLIP_VERTS, { FloatArray(3) })

    /**
     * R_AddSkySurface
     */
    fun R_AddSkySurface(fa: msurface_t) {
        // calculate vertex values for sky box
        run {
            var p = fa.polys
            while (p != null) {
                for (i in 0..p!!.numverts - 1) {
                    verts[i][0] = p!!.x(i) - r_origin[0]
                    verts[i][1] = p!!.y(i) - r_origin[1]
                    verts[i][2] = p!!.z(i) - r_origin[2]
                }
                ClipSkyPolygon(p!!.numverts, verts, 0)
                p = p!!.next
            }
        }
    }

    /**
     * R_ClearSkyBox
     */
    fun R_ClearSkyBox() {
        val skymins0 = skymins[0]
        val skymins1 = skymins[1]
        val skymaxs0 = skymaxs[0]
        val skymaxs1 = skymaxs[1]

        for (i in 0..6 - 1) {
            skymins0[i] = skymins1[i] = 9999
            skymaxs0[i] = skymaxs1[i] = (-9999).toFloat()
        }
    }

    // stack variable
    private val v1 = floatArray(0.0, 0.0, 0.0)
    private val b = floatArray(0.0, 0.0, 0.0)
    /**
     * MakeSkyVec
     * @param s
    * *
     * @param t
    * *
     * @param axis
     */
    fun MakeSkyVec(s: Float, t: Float, axis: Int) {
        var s = s
        var t = t
        b[0] = s * 2300
        b[1] = t * 2300
        b[2] = 2300

        var j: Int
        var k: Int
        run {
            j = 0
            while (j < 3) {
                k = st_to_vec[axis][j]
                if (k < 0)
                    v1[j] = -b[-k - 1]
                else
                    v1[j] = b[k - 1]
                j++
            }
        }

        // avoid bilerp seam
        s = (s + 1) * 0.5.toFloat()
        t = (t + 1) * 0.5.toFloat()

        if (s < sky_min)
            s = sky_min
        else if (s > sky_max)
            s = sky_max
        if (t < sky_min)
            t = sky_min
        else if (t > sky_max)
            t = sky_max

        t = 1.0.toFloat() - t
        GL11.glTexCoord2f(s, t)
        GL11.glVertex3f(v1[0], v1[1], v1[2])
    }

    var skytexorder = intArray(0, 2, 1, 3, 4, 5)

    /**
     * R_DrawSkyBox
     */
    fun R_DrawSkyBox() {
        var i: Int

        if (skyrotate != 0) {
            // check for no sky at all
            run {
                i = 0
                while (i < 6) {
                    if (skymins[0][i] < skymaxs[0][i] && skymins[1][i] < skymaxs[1][i])
                        break
                    i++
                }
            }
            if (i == 6)
                return        // nothing visible
        }

        GL11.glPushMatrix()
        GL11.glTranslatef(r_origin[0], r_origin[1], r_origin[2])
        GL11.glRotatef(r_newrefdef.time * skyrotate, skyaxis[0], skyaxis[1], skyaxis[2])

        run {
            i = 0
            while (i < 6) {
                if (skyrotate != 0) {
                    // hack, forces full sky to draw when rotating
                    skymins[0][i] = (-1).toFloat()
                    skymins[1][i] = (-1).toFloat()
                    skymaxs[0][i] = 1
                    skymaxs[1][i] = 1
                }

                if (skymins[0][i] >= skymaxs[0][i] || skymins[1][i] >= skymaxs[1][i])
                    continue

                GL_Bind(sky_images[skytexorder[i]].texnum)

                GL11.glBegin(GL11.GL_QUADS)
                MakeSkyVec(skymins[0][i], skymins[1][i], i)
                MakeSkyVec(skymins[0][i], skymaxs[1][i], i)
                MakeSkyVec(skymaxs[0][i], skymaxs[1][i], i)
                MakeSkyVec(skymaxs[0][i], skymins[1][i], i)
                GL11.glEnd()
                i++
            }
        }
        GL11.glPopMatrix()
    }

    // 3dstudio environment map names
    var suf = array<String>("rt", "bk", "lf", "ft", "up", "dn")

    /**
     * R_SetSky
     * @param name
    * *
     * @param rotate
    * *
     * @param axis
     */
    protected fun R_SetSky(name: String, rotate: Float, axis: FloatArray) {
        assert((axis.size() == 3), "vec3_t bug")
        val pathname: String
        skyname = name

        skyrotate = rotate
        Math3D.VectorCopy(axis, skyaxis)

        for (i in 0..6 - 1) {
            // chop down rotating skies for less memory
            if (gl_skymip.value != 0 || skyrotate != 0)
                gl_picmip.value++

            if (qglColorTableEXT && gl_ext_palettedtexture.value != 0) {
                //	Com_sprintf (pathname, sizeof(pathname), "env/%s%s.pcx", skyname, suf[i]);
                pathname = "env/" + skyname + suf[i] + ".pcx"
            } else {
                // Com_sprintf (pathname, sizeof(pathname), "env/%s%s.tga", skyname, suf[i]);
                pathname = "env/" + skyname + suf[i] + ".tga"
            }

            sky_images[i] = GL_FindImage(pathname, it_sky)

            if (sky_images[i] == null)
                sky_images[i] = r_notexture

            if (gl_skymip.value != 0 || skyrotate != 0) {
                // take less memory
                gl_picmip.value--
                sky_min = 1.0.toFloat() / 256
                sky_max = 255.0.toFloat() / 256
            } else {
                sky_min = 1.0.toFloat() / 512
                sky_max = 511.0.toFloat() / 512
            }
        }
    }

    companion object {
        // warpsin.h
        public val SIN: FloatArray = floatArray(0.toFloat(), 0.19633.toFloat(), 0.392541.toFloat(), 0.588517.toFloat(), 0.784137.toFloat(), 0.979285.toFloat(), 1.17384.toFloat(), 1.3677.toFloat(), 1.56072.toFloat(), 1.75281.toFloat(), 1.94384.toFloat(), 2.1337.toFloat(), 2.32228.toFloat(), 2.50945.toFloat(), 2.69512.toFloat(), 2.87916.toFloat(), 3.06147.toFloat(), 3.24193.toFloat(), 3.42044.toFloat(), 3.59689.toFloat(), 3.77117.toFloat(), 3.94319.toFloat(), 4.11282.toFloat(), 4.27998.toFloat(), 4.44456.toFloat(), 4.60647.toFloat(), 4.76559.toFloat(), 4.92185.toFloat(), 5.07515.toFloat(), 5.22538.toFloat(), 5.37247.toFloat(), 5.51632.toFloat(), 5.65685.toFloat(), 5.79398.toFloat(), 5.92761.toFloat(), 6.05767.toFloat(), 6.18408.toFloat(), 6.30677.toFloat(), 6.42566.toFloat(), 6.54068.toFloat(), 6.65176.toFloat(), 6.75883.toFloat(), 6.86183.toFloat(), 6.9607.toFloat(), 7.05537.toFloat(), 7.14579.toFloat(), 7.23191.toFloat(), 7.31368.toFloat(), 7.39104.toFloat(), 7.46394.toFloat(), 7.53235.toFloat(), 7.59623.toFloat(), 7.65552.toFloat(), 7.71021.toFloat(), 7.76025.toFloat(), 7.80562.toFloat(), 7.84628.toFloat(), 7.88222.toFloat(), 7.91341.toFloat(), 7.93984.toFloat(), 7.96148.toFloat(), 7.97832.toFloat(), 7.99036.toFloat(), 7.99759.toFloat(), 8.toFloat(), 7.99759.toFloat(), 7.99036.toFloat(), 7.97832.toFloat(), 7.96148.toFloat(), 7.93984.toFloat(), 7.91341.toFloat(), 7.88222.toFloat(), 7.84628.toFloat(), 7.80562.toFloat(), 7.76025.toFloat(), 7.71021.toFloat(), 7.65552.toFloat(), 7.59623.toFloat(), 7.53235.toFloat(), 7.46394.toFloat(), 7.39104.toFloat(), 7.31368.toFloat(), 7.23191.toFloat(), 7.14579.toFloat(), 7.05537.toFloat(), 6.9607.toFloat(), 6.86183.toFloat(), 6.75883.toFloat(), 6.65176.toFloat(), 6.54068.toFloat(), 6.42566.toFloat(), 6.30677.toFloat(), 6.18408.toFloat(), 6.05767.toFloat(), 5.92761.toFloat(), 5.79398.toFloat(), 5.65685.toFloat(), 5.51632.toFloat(), 5.37247.toFloat(), 5.22538.toFloat(), 5.07515.toFloat(), 4.92185.toFloat(), 4.76559.toFloat(), 4.60647.toFloat(), 4.44456.toFloat(), 4.27998.toFloat(), 4.11282.toFloat(), 3.94319.toFloat(), 3.77117.toFloat(), 3.59689.toFloat(), 3.42044.toFloat(), 3.24193.toFloat(), 3.06147.toFloat(), 2.87916.toFloat(), 2.69512.toFloat(), 2.50945.toFloat(), 2.32228.toFloat(), 2.1337.toFloat(), 1.94384.toFloat(), 1.75281.toFloat(), 1.56072.toFloat(), 1.3677.toFloat(), 1.17384.toFloat(), 0.979285.toFloat(), 0.784137.toFloat(), 0.588517.toFloat(), 0.392541.toFloat(), 0.19633.toFloat(), 9.79717e-16.toFloat(), (-0.19633.toFloat()).toFloat(), (-0.392541.toFloat()).toFloat(), (-0.588517.toFloat()).toFloat(), (-0.784137.toFloat()).toFloat(), (-0.979285.toFloat()).toFloat(), (-1.17384.toFloat()).toFloat(), (-1.3677.toFloat()).toFloat(), (-1.56072.toFloat()).toFloat(), (-1.75281.toFloat()).toFloat(), (-1.94384.toFloat()).toFloat(), (-2.1337.toFloat()).toFloat(), (-2.32228.toFloat()).toFloat(), (-2.50945.toFloat()).toFloat(), (-2.69512.toFloat()).toFloat(), (-2.87916.toFloat()).toFloat(), (-3.06147.toFloat()).toFloat(), (-3.24193.toFloat()).toFloat(), (-3.42044.toFloat()).toFloat(), (-3.59689.toFloat()).toFloat(), (-3.77117.toFloat()).toFloat(), (-3.94319.toFloat()).toFloat(), (-4.11282.toFloat()).toFloat(), (-4.27998.toFloat()).toFloat(), (-4.44456.toFloat()).toFloat(), (-4.60647.toFloat()).toFloat(), (-4.76559.toFloat()).toFloat(), (-4.92185.toFloat()).toFloat(), (-5.07515.toFloat()).toFloat(), (-5.22538.toFloat()).toFloat(), (-5.37247.toFloat()).toFloat(), (-5.51632.toFloat()).toFloat(), (-5.65685.toFloat()).toFloat(), (-5.79398.toFloat()).toFloat(), (-5.92761.toFloat()).toFloat(), (-6.05767.toFloat()).toFloat(), (-6.18408.toFloat()).toFloat(), (-6.30677.toFloat()).toFloat(), (-6.42566.toFloat()).toFloat(), (-6.54068.toFloat()).toFloat(), (-6.65176.toFloat()).toFloat(), (-6.75883.toFloat()).toFloat(), (-6.86183.toFloat()).toFloat(), (-6.9607.toFloat()).toFloat(), (-7.05537.toFloat()).toFloat(), (-7.14579.toFloat()).toFloat(), (-7.23191.toFloat()).toFloat(), (-7.31368.toFloat()).toFloat(), (-7.39104.toFloat()).toFloat(), (-7.46394.toFloat()).toFloat(), (-7.53235.toFloat()).toFloat(), (-7.59623.toFloat()).toFloat(), (-7.65552.toFloat()).toFloat(), (-7.71021.toFloat()).toFloat(), (-7.76025.toFloat()).toFloat(), (-7.80562.toFloat()).toFloat(), (-7.84628.toFloat()).toFloat(), (-7.88222.toFloat()).toFloat(), (-7.91341.toFloat()).toFloat(), (-7.93984.toFloat()).toFloat(), (-7.96148.toFloat()).toFloat(), (-7.97832.toFloat()).toFloat(), (-7.99036.toFloat()).toFloat(), (-7.99759.toFloat()).toFloat(), (-8.toFloat()).toFloat(), (-7.99759.toFloat()).toFloat(), (-7.99036.toFloat()).toFloat(), (-7.97832.toFloat()).toFloat(), (-7.96148.toFloat()).toFloat(), (-7.93984.toFloat()).toFloat(), (-7.91341.toFloat()).toFloat(), (-7.88222.toFloat()).toFloat(), (-7.84628.toFloat()).toFloat(), (-7.80562.toFloat()).toFloat(), (-7.76025.toFloat()).toFloat(), (-7.71021.toFloat()).toFloat(), (-7.65552.toFloat()).toFloat(), (-7.59623.toFloat()).toFloat(), (-7.53235.toFloat()).toFloat(), (-7.46394.toFloat()).toFloat(), (-7.39104.toFloat()).toFloat(), (-7.31368.toFloat()).toFloat(), (-7.23191.toFloat()).toFloat(), (-7.14579.toFloat()).toFloat(), (-7.05537.toFloat()).toFloat(), (-6.9607.toFloat()).toFloat(), (-6.86183.toFloat()).toFloat(), (-6.75883.toFloat()).toFloat(), (-6.65176.toFloat()).toFloat(), (-6.54068.toFloat()).toFloat(), (-6.42566.toFloat()).toFloat(), (-6.30677.toFloat()).toFloat(), (-6.18408.toFloat()).toFloat(), (-6.05767.toFloat()).toFloat(), (-5.92761.toFloat()).toFloat(), (-5.79398.toFloat()).toFloat(), (-5.65685.toFloat()).toFloat(), (-5.51632.toFloat()).toFloat(), (-5.37247.toFloat()).toFloat(), (-5.22538.toFloat()).toFloat(), (-5.07515.toFloat()).toFloat(), (-4.92185.toFloat()).toFloat(), (-4.76559.toFloat()).toFloat(), (-4.60647.toFloat()).toFloat(), (-4.44456.toFloat()).toFloat(), (-4.27998.toFloat()).toFloat(), (-4.11282.toFloat()).toFloat(), (-3.94319.toFloat()).toFloat(), (-3.77117.toFloat()).toFloat(), (-3.59689.toFloat()).toFloat(), (-3.42044.toFloat()).toFloat(), (-3.24193.toFloat()).toFloat(), (-3.06147.toFloat()).toFloat(), (-2.87916.toFloat()).toFloat(), (-2.69512.toFloat()).toFloat(), (-2.50945.toFloat()).toFloat(), (-2.32228.toFloat()).toFloat(), (-2.1337.toFloat()).toFloat(), (-1.94384.toFloat()).toFloat(), (-1.75281.toFloat()).toFloat(), (-1.56072.toFloat()).toFloat(), (-1.3677.toFloat()).toFloat(), (-1.17384.toFloat()).toFloat(), (-0.979285.toFloat()).toFloat(), (-0.784137.toFloat()).toFloat(), (-0.588517.toFloat()).toFloat(), (-0.392541.toFloat()).toFloat(), (-0.19633.toFloat()).toFloat())

        val SUBDIVIDE_SIZE = 64

        // =========================================================
        val TURBSCALE = (256.0.toFloat() / (2 * Math.PI)) as Float

        val ON_EPSILON = 0.1.toFloat() // point on plane side epsilon
        val MAX_CLIP_VERTS = 64

        val SIDE_BACK = 1
        val SIDE_FRONT = 0
        val SIDE_ON = 2
    }
}