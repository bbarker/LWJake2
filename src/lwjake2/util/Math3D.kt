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

package lwjake2.util

import lwjake2.Defines
import lwjake2.game.cplane_t
import lwjake2.qcommon.Com

public class Math3D {
    companion object {

        val shortratio = 360.0.toFloat() / 65536.0.toFloat()
        val piratio = (Math.PI / 360.0) as Float
        public fun set(v1: FloatArray, v2: FloatArray) {
            v1[0] = v2[0]
            v1[1] = v2[1]
            v1[2] = v2[2]
        }

        public fun VectorSubtract(a: FloatArray, b: FloatArray, c: FloatArray) {
            c[0] = a[0] - b[0]
            c[1] = a[1] - b[1]
            c[2] = a[2] - b[2]
        }

        public fun VectorSubtract(a: ShortArray, b: ShortArray, c: IntArray) {
            c[0] = a[0].toInt() - b[0].toInt()
            c[1] = a[1].toInt() - b[1].toInt()
            c[2] = a[2].toInt() - b[2].toInt()
        }

        public fun VectorAdd(a: FloatArray, b: FloatArray, to: FloatArray) {
            to[0] = a[0] + b[0]
            to[1] = a[1] + b[1]
            to[2] = a[2] + b[2]
        }

        public fun VectorCopy(from: FloatArray, to: FloatArray) {
            to[0] = from[0]
            to[1] = from[1]
            to[2] = from[2]
        }

        public fun VectorCopy(from: ShortArray, to: ShortArray) {
            to[0] = from[0]
            to[1] = from[1]
            to[2] = from[2]
        }

        public fun VectorCopy(from: ShortArray, to: FloatArray) {
            to[0] = from[0].toFloat()
            to[1] = from[1].toFloat()
            to[2] = from[2].toFloat()
        }

        public fun VectorCopy(from: FloatArray, to: ShortArray) {
            to[0] = from[0].toShort()
            to[1] = from[1].toShort()
            to[2] = from[2].toShort()
        }

        public fun VectorClear(a: FloatArray) {
            a[0] = a[1] = a[2] = 0
        }

        public fun VectorEquals(v1: FloatArray, v2: FloatArray): Boolean {
            if (v1[0] != v2[0] || v1[1] != v2[1] || v1[2] != v2[2])
                return false

            return true
        }

        public fun VectorNegate(from: FloatArray, to: FloatArray) {
            to[0] = -from[0]
            to[1] = -from[1]
            to[2] = -from[2]
        }

        public fun VectorSet(v: FloatArray, x: Float, y: Float, z: Float) {
            v[0] = (x)
            v[1] = (y)
            v[2] = (z)
        }

        public fun VectorMA(veca: FloatArray, scale: Float, vecb: FloatArray, to: FloatArray) {
            to[0] = veca[0] + scale * vecb[0]
            to[1] = veca[1] + scale * vecb[1]
            to[2] = veca[2] + scale * vecb[2]
        }

        public fun VectorNormalize(v: FloatArray): Float {

            val length = VectorLength(v)
            if (length != 0.0.toFloat()) {
                val ilength = 1.0.toFloat() / length
                v[0] *= ilength
                v[1] *= ilength
                v[2] *= ilength
            }
            return length
        }

        public fun VectorLength(v: FloatArray): Float {
            return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) as Float
        }

        public fun VectorInverse(v: FloatArray) {
            v[0] = -v[0]
            v[1] = -v[1]
            v[2] = -v[2]
        }

        public fun VectorScale(`in`: FloatArray, scale: Float, out: FloatArray) {
            out[0] = `in`[0] * scale
            out[1] = `in`[1] * scale
            out[2] = `in`[2] * scale
        }

        public fun vectoyaw(vec: FloatArray): Float {
            var yaw: Float

            if (/*vec[YAW] == 0 &&*/
            vec[Defines.PITCH] == 0) {
                yaw = 0
                if (vec[Defines.YAW] > 0)
                    yaw = 90
                else if (vec[Defines.YAW] < 0)
                    yaw = (-90).toFloat()
            } else {

                yaw = ((Math.atan2(vec[Defines.YAW], vec[Defines.PITCH]) * 180 / Math.PI) as Int).toFloat()
                if (yaw < 0)
                    yaw += 360
            }

            return yaw
        }

        public fun vectoangles(value1: FloatArray, angles: FloatArray) {

            var yaw: Float
            var pitch: Float

            if (value1[1] == 0 && value1[0] == 0) {
                yaw = 0
                if (value1[2] > 0)
                    pitch = 90
                else
                    pitch = 270
            } else {
                if (value1[0] != 0)
                    yaw = ((Math.atan2(value1[1], value1[0]) * 180 / Math.PI) as Int).toFloat()
                else if (value1[1] > 0)
                    yaw = 90
                else
                    yaw = (-90).toFloat()
                if (yaw < 0)
                    yaw += 360

                val forward = Math.sqrt(value1[0] * value1[0] + value1[1] * value1[1]) as Float
                pitch = ((Math.atan2(value1[2], forward) * 180 / Math.PI) as Int).toFloat()
                if (pitch < 0)
                    pitch += 360
            }

            angles[Defines.PITCH] = -pitch
            angles[Defines.YAW] = yaw
            angles[Defines.ROLL] = 0
        }

        private val m = Array<FloatArray>(3, { FloatArray(3) })
        private val im = Array<FloatArray>(3, { FloatArray(3) })
        private val tmpmat = Array<FloatArray>(3, { FloatArray(3) })
        private val zrot = Array<FloatArray>(3, { FloatArray(3) })

        // to reduce garbage
        private val vr = floatArray(0.0, 0.0, 0.0)
        private val vup = floatArray(0.0, 0.0, 0.0)
        private val vf = floatArray(0.0, 0.0, 0.0)

        public fun RotatePointAroundVector(dst: FloatArray, dir: FloatArray, point: FloatArray, degrees: Float) {
            vf[0] = dir[0]
            vf[1] = dir[1]
            vf[2] = dir[2]

            PerpendicularVector(vr, dir)
            CrossProduct(vr, vf, vup)

            m[0][0] = vr[0]
            m[1][0] = vr[1]
            m[2][0] = vr[2]

            m[0][1] = vup[0]
            m[1][1] = vup[1]
            m[2][1] = vup[2]

            m[0][2] = vf[0]
            m[1][2] = vf[1]
            m[2][2] = vf[2]

            im[0][0] = m[0][0]
            im[0][1] = m[1][0]
            im[0][2] = m[2][0]
            im[1][0] = m[0][1]
            im[1][1] = m[1][1]
            im[1][2] = m[2][1]
            im[2][0] = m[0][2]
            im[2][1] = m[1][2]
            im[2][2] = m[2][2]

            zrot[0][2] = zrot[1][2] = zrot[2][0] = zrot[2][1] = 0.0.toFloat()

            zrot[2][2] = 1.0.toFloat()

            zrot[0][0] = zrot[1][1] = Math.cos(DEG2RAD(degrees)) as Float
            zrot[0][1] = Math.sin(DEG2RAD(degrees)) as Float
            zrot[1][0] = -zrot[0][1]

            R_ConcatRotations(m, zrot, tmpmat)
            R_ConcatRotations(tmpmat, im, zrot)

            for (i in 0..3 - 1) {
                dst[i] = zrot[i][0] * point[0] + zrot[i][1] * point[1] + zrot[i][2] * point[2]
            }
        }

        public fun MakeNormalVectors(forward: FloatArray, right: FloatArray, up: FloatArray) {
            // this rotate and negat guarantees a vector
            // not colinear with the original
            right[1] = -forward[0]
            right[2] = forward[1]
            right[0] = forward[2]

            val d = DotProduct(right, forward)
            VectorMA(right, -d, forward, right)
            VectorNormalize(right)
            CrossProduct(right, forward, up)
        }

        public fun SHORT2ANGLE(x: Int): Float {
            return (x.toFloat() * shortratio)
        }

        /*
	================
	R_ConcatTransforms
	================
	*/
        public fun R_ConcatTransforms(in1: Array<FloatArray>, in2: Array<FloatArray>, out: Array<FloatArray>) {
            out[0][0] = in1[0][0] * in2[0][0] + in1[0][1] * in2[1][0] + in1[0][2] * in2[2][0]
            out[0][1] = in1[0][0] * in2[0][1] + in1[0][1] * in2[1][1] + in1[0][2] * in2[2][1]
            out[0][2] = in1[0][0] * in2[0][2] + in1[0][1] * in2[1][2] + in1[0][2] * in2[2][2]
            out[0][3] = in1[0][0] * in2[0][3] + in1[0][1] * in2[1][3] + in1[0][2] * in2[2][3] + in1[0][3]
            out[1][0] = in1[1][0] * in2[0][0] + in1[1][1] * in2[1][0] + in1[1][2] * in2[2][0]
            out[1][1] = in1[1][0] * in2[0][1] + in1[1][1] * in2[1][1] + in1[1][2] * in2[2][1]
            out[1][2] = in1[1][0] * in2[0][2] + in1[1][1] * in2[1][2] + in1[1][2] * in2[2][2]
            out[1][3] = in1[1][0] * in2[0][3] + in1[1][1] * in2[1][3] + in1[1][2] * in2[2][3] + in1[1][3]
            out[2][0] = in1[2][0] * in2[0][0] + in1[2][1] * in2[1][0] + in1[2][2] * in2[2][0]
            out[2][1] = in1[2][0] * in2[0][1] + in1[2][1] * in2[1][1] + in1[2][2] * in2[2][1]
            out[2][2] = in1[2][0] * in2[0][2] + in1[2][1] * in2[1][2] + in1[2][2] * in2[2][2]
            out[2][3] = in1[2][0] * in2[0][3] + in1[2][1] * in2[1][3] + in1[2][2] * in2[2][3] + in1[2][3]
        }

        /**
         * concatenates 2 matrices each [3][3].
         */
        public fun R_ConcatRotations(in1: Array<FloatArray>, in2: Array<FloatArray>, out: Array<FloatArray>) {
            out[0][0] = in1[0][0] * in2[0][0] + in1[0][1] * in2[1][0] + in1[0][2] * in2[2][0]
            out[0][1] = in1[0][0] * in2[0][1] + in1[0][1] * in2[1][1] + in1[0][2] * in2[2][1]
            out[0][2] = in1[0][0] * in2[0][2] + in1[0][1] * in2[1][2] + in1[0][2] * in2[2][2]
            out[1][0] = in1[1][0] * in2[0][0] + in1[1][1] * in2[1][0] + in1[1][2] * in2[2][0]
            out[1][1] = in1[1][0] * in2[0][1] + in1[1][1] * in2[1][1] + in1[1][2] * in2[2][1]
            out[1][2] = in1[1][0] * in2[0][2] + in1[1][1] * in2[1][2] + in1[1][2] * in2[2][2]
            out[2][0] = in1[2][0] * in2[0][0] + in1[2][1] * in2[1][0] + in1[2][2] * in2[2][0]
            out[2][1] = in1[2][0] * in2[0][1] + in1[2][1] * in2[1][1] + in1[2][2] * in2[2][1]
            out[2][2] = in1[2][0] * in2[0][2] + in1[2][1] * in2[1][2] + in1[2][2] * in2[2][2]
        }

        public fun ProjectPointOnPlane(dst: FloatArray, p: FloatArray, normal: FloatArray) {

            val inv_denom = 1.0.toFloat() / DotProduct(normal, normal)

            val d = DotProduct(normal, p) * inv_denom

            dst[0] = normal[0] * inv_denom
            dst[1] = normal[1] * inv_denom
            dst[2] = normal[2] * inv_denom

            dst[0] = p[0] - d * dst[0]
            dst[1] = p[1] - d * dst[1]
            dst[2] = p[2] - d * dst[2]
        }

        private val PLANE_XYZ = array<FloatArray>(floatArray(1.0, 0.0, 0.0).toFloat(), floatArray(0.0, 1.0, 0.0).toFloat(), floatArray(0.0, 0.0, 1.0).toFloat())

        /** assumes "src" is normalized  */
        public fun PerpendicularVector(dst: FloatArray, src: FloatArray) {
            var pos: Int
            var i: Int
            var minelem = 1.0.toFloat()

            // find the smallest magnitude axially aligned vector
            run {
                pos = 0
                i = 0
                while (i < 3) {
                    if (Math.abs(src[i]) < minelem) {
                        pos = i
                        minelem = Math.abs(src[i])
                    }
                    i++
                }
            }
            // project the point onto the plane defined by src
            ProjectPointOnPlane(dst, PLANE_XYZ[pos], src)

            //normalize the result
            VectorNormalize(dst)
        }
        //=====================================================================
        /**
         * stellt fest, auf welcher Seite sich die Kiste befindet, wenn die Ebene
         * durch Entfernung und Senkrechten-Normale gegeben ist.
         * erste Version mit vec3_t...  */
        public fun BoxOnPlaneSide(emins: FloatArray, emaxs: FloatArray, p: cplane_t): Int {

            assert((emins.size() == 3 && emaxs.size() == 3), "vec3_t bug")

            val dist1: Float
            val dist2: Float
            var sides: Int

            //	   fast axial cases
            if (p.type < 3) {
                if (p.dist <= emins[p.type])
                    return 1
                if (p.dist >= emaxs[p.type])
                    return 2
                return 3
            }

            //	   general case
            when (p.signbits) {
                0 -> {
                    dist1 = p.normal[0] * emaxs[0] + p.normal[1] * emaxs[1] + p.normal[2] * emaxs[2]
                    dist2 = p.normal[0] * emins[0] + p.normal[1] * emins[1] + p.normal[2] * emins[2]
                }
                1 -> {
                    dist1 = p.normal[0] * emins[0] + p.normal[1] * emaxs[1] + p.normal[2] * emaxs[2]
                    dist2 = p.normal[0] * emaxs[0] + p.normal[1] * emins[1] + p.normal[2] * emins[2]
                }
                2 -> {
                    dist1 = p.normal[0] * emaxs[0] + p.normal[1] * emins[1] + p.normal[2] * emaxs[2]
                    dist2 = p.normal[0] * emins[0] + p.normal[1] * emaxs[1] + p.normal[2] * emins[2]
                }
                3 -> {
                    dist1 = p.normal[0] * emins[0] + p.normal[1] * emins[1] + p.normal[2] * emaxs[2]
                    dist2 = p.normal[0] * emaxs[0] + p.normal[1] * emaxs[1] + p.normal[2] * emins[2]
                }
                4 -> {
                    dist1 = p.normal[0] * emaxs[0] + p.normal[1] * emaxs[1] + p.normal[2] * emins[2]
                    dist2 = p.normal[0] * emins[0] + p.normal[1] * emins[1] + p.normal[2] * emaxs[2]
                }
                5 -> {
                    dist1 = p.normal[0] * emins[0] + p.normal[1] * emaxs[1] + p.normal[2] * emins[2]
                    dist2 = p.normal[0] * emaxs[0] + p.normal[1] * emins[1] + p.normal[2] * emaxs[2]
                }
                6 -> {
                    dist1 = p.normal[0] * emaxs[0] + p.normal[1] * emins[1] + p.normal[2] * emins[2]
                    dist2 = p.normal[0] * emins[0] + p.normal[1] * emaxs[1] + p.normal[2] * emaxs[2]
                }
                7 -> {
                    dist1 = p.normal[0] * emins[0] + p.normal[1] * emins[1] + p.normal[2] * emins[2]
                    dist2 = p.normal[0] * emaxs[0] + p.normal[1] * emaxs[1] + p.normal[2] * emaxs[2]
                }
                else -> {
                    dist1 = dist2 = 0
                    assert((false), "BoxOnPlaneSide bug")
                }
            }

            sides = 0
            if (dist1 >= p.dist)
                sides = 1
            if (dist2 < p.dist)
                sides = sides or 2

            assert((sides != 0), "BoxOnPlaneSide(): sides == 0 bug")

            return sides
        }

        //	this is the slow, general version
        private val corners = Array<FloatArray>(2, { FloatArray(3) })

        public fun BoxOnPlaneSide2(emins: FloatArray, emaxs: FloatArray, p: cplane_t): Int {

            for (i in 0..3 - 1) {
                if (p.normal[i] < 0) {
                    corners[0][i] = emins[i]
                    corners[1][i] = emaxs[i]
                } else {
                    corners[1][i] = emins[i]
                    corners[0][i] = emaxs[i]
                }
            }
            val dist1 = DotProduct(p.normal, corners[0]) - p.dist
            val dist2 = DotProduct(p.normal, corners[1]) - p.dist
            var sides = 0
            if (dist1 >= 0)
                sides = 1
            if (dist2 < 0)
                sides = sides or 2

            return sides
        }

        public fun AngleVectors(angles: FloatArray, forward: FloatArray?, right: FloatArray?, up: FloatArray?) {

            var cr = 2.0.toFloat() * piratio
            var angle = (angles[Defines.YAW] * (cr)).toFloat()
            val sy = Math.sin(angle) as Float
            val cy = Math.cos(angle) as Float
            angle = (angles[Defines.PITCH] * (cr)).toFloat()
            val sp = Math.sin(angle) as Float
            val cp = Math.cos(angle) as Float

            if (forward != null) {
                forward[0] = cp * cy
                forward[1] = cp * sy
                forward[2] = -sp
            }

            if (right != null || up != null) {
                angle = (angles[Defines.ROLL] * (cr)).toFloat()
                val sr = Math.sin(angle) as Float
                cr = Math.cos(angle) as Float

                if (right != null) {
                    right[0] = (-sr * sp * cy + cr * sy)
                    right[1] = (-sr * sp * sy + -cr * cy)
                    right[2] = -sr * cp
                }
                if (up != null) {
                    up[0] = (cr * sp * cy + sr * sy)
                    up[1] = (cr * sp * sy + -sr * cy)
                    up[2] = cr * cp
                }
            }
        }

        public fun G_ProjectSource(point: FloatArray, distance: FloatArray, forward: FloatArray, right: FloatArray, result: FloatArray) {
            result[0] = point[0] + forward[0] * distance[0] + right[0] * distance[1]
            result[1] = point[1] + forward[1] * distance[0] + right[1] * distance[1]
            result[2] = point[2] + forward[2] * distance[0] + right[2] * distance[1] + distance[2]
        }

        public fun DotProduct(x: FloatArray, y: FloatArray): Float {
            return x[0] * y[0] + x[1] * y[1] + x[2] * y[2]
        }

        public fun CrossProduct(v1: FloatArray, v2: FloatArray, cross: FloatArray) {
            cross[0] = v1[1] * v2[2] - v1[2] * v2[1]
            cross[1] = v1[2] * v2[0] - v1[0] * v2[2]
            cross[2] = v1[0] * v2[1] - v1[1] * v2[0]
        }

        public fun Q_log2(`val`: Int): Int {
            var `val` = `val`
            var answer = 0
            while ((`val` = `val` shr 1) > 0)
                answer++
            return answer
        }

        public fun DEG2RAD(`in`: Float): Float {
            return (`in` * Math.PI as Float) / 180.0.toFloat()
        }

        public fun anglemod(a: Float): Float {
            return (shortratio).toFloat() * ((a / (shortratio)).toInt() and 65535).toFloat()
        }

        public fun ANGLE2SHORT(x: Float): Int {
            return (((x) / shortratio).toInt() and 65535)
        }

        public fun LerpAngle(a2: Float, a1: Float, frac: Float): Float {
            var a1 = a1
            if (a1 - a2 > 180)
                a1 -= 360
            if (a1 - a2 < -180)
                a1 += 360
            return a2 + frac * (a1 - a2)
        }

        public fun CalcFov(fov_x: Float, width: Float, height: Float): Float {
            var a: Double = 0.0.toFloat()
            val x: Double

            if (fov_x < 1.0.toFloat() || fov_x > 179.0.toFloat())
                Com.Error(Defines.ERR_DROP, "Bad fov: " + fov_x)

            x = width / Math.tan(fov_x * piratio)

            a = Math.atan(height.toDouble() / x)

            a = a / piratio.toDouble()

            return a.toFloat()
        }
    }
}
