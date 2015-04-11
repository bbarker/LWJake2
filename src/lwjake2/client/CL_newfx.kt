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
import lwjake2.util.Lib
import lwjake2.util.Math3D

/**
 * CL_newfx
 */
public class CL_newfx {
    companion object {

        fun Flashlight(ent: Int, pos: FloatArray) {
            val dl: CL_fx.cdlight_t

            dl = CL_fx.AllocDlight(ent)
            Math3D.VectorCopy(pos, dl.origin)
            dl.radius = 400
            dl.minlight = 250
            dl.die = Globals.cl.time + 100
            dl.color[0] = 1
            dl.color[1] = 1
            dl.color[2] = 1
        }

        /*
     * ====== CL_ColorFlash - flash of light ======
     */
        fun ColorFlash(pos: FloatArray, ent: Int, intensity: Int, r: Float, g: Float, b: Float) {
            var intensity = intensity
            var r = r
            var g = g
            var b = b
            val dl: CL_fx.cdlight_t

            if ((Globals.vidref_val == Defines.VIDREF_SOFT) && ((r < 0) || (g < 0) || (b < 0))) {
                intensity = -intensity
                r = -r
                g = -g
                b = -b
            }

            dl = CL_fx.AllocDlight(ent)
            Math3D.VectorCopy(pos, dl.origin)
            dl.radius = intensity
            dl.minlight = 250
            dl.die = Globals.cl.time + 100
            dl.color[0] = r
            dl.color[1] = g
            dl.color[2] = b
        }

        // stack variable
        private val move = floatArray(0.0, 0.0, 0.0)
        private val vec = floatArray(0.0, 0.0, 0.0)
        private val right = floatArray(0.0, 0.0, 0.0)
        private val up = floatArray(0.0, 0.0, 0.0)
        /*
     * ====== CL_DebugTrail ======
     */
        fun DebugTrail(start: FloatArray, end: FloatArray) {
            var len: Float
            //		int j;
            val p: cparticle_t
            val dec: Float
            //		int i;
            //		float d, c, s;
            //		float[] dir;

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            Math3D.MakeNormalVectors(vec, right, up)

            //		VectorScale(vec, RT2_SKIP, vec);

            //		dec = 1.0;
            //		dec = 0.75;
            dec = 3
            Math3D.VectorScale(vec, dec, vec)
            Math3D.VectorCopy(start, move)

            while (len > 0) {
                len -= dec

                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p

                p.time = Globals.cl.time
                Math3D.VectorClear(p.accel)
                Math3D.VectorClear(p.vel)
                p.alpha = 1.0.toFloat()
                p.alphavel = -0.1.toFloat()
                //			p.alphavel = 0;
                p.color = 116 + (Lib.rand() and 7)
                Math3D.VectorCopy(move, p.org)
                /*
             * for (j=0 ; j <3 ; j++) { p.org[j] = move[j] + crand()*2; p.vel[j] =
             * crand()*3; p.accel[j] = 0; }
             */
                Math3D.VectorAdd(move, vec, move)
            }

        }

        // stack variable
        // move, vec
        fun ForceWall(start: FloatArray, end: FloatArray, color: Int) {
            var len: Float
            var j: Int
            val p: cparticle_t

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            Math3D.VectorScale(vec, 4, vec)

            // FIXME: this is a really silly way to have a loop
            while (len > 0) {
                len -= 4

                if (CL_fx.free_particles == null)
                    return

                if (Globals.rnd.nextFloat() > 0.3) {
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p
                    Math3D.VectorClear(p.accel)

                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = -1.0.toFloat() / (3.0.toFloat() + Globals.rnd.nextFloat() * 0.5.toFloat())
                    p.color = color
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = move[j] + Lib.crand() * 3
                            p.accel[j] = 0
                            j++
                        }
                    }
                    p.vel[0] = 0
                    p.vel[1] = 0
                    p.vel[2] = -40 - (Lib.crand() * 10)
                }

                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // move, vec
        /*
     * =============== CL_BubbleTrail2 (lets you control the # of bubbles by
     * setting the distance between the spawns)
     * 
     * ===============
     */
        fun BubbleTrail2(start: FloatArray, end: FloatArray, dist: Int) {
            val len: Float
            var i: Int
            var j: Int
            var p: cparticle_t
            val dec: Float

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = dist.toFloat()
            Math3D.VectorScale(vec, dec, vec)

            run {
                i = 0
                while (i < len) {
                    if (CL_fx.free_particles == null)
                        return

                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    Math3D.VectorClear(p.accel)
                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = -1.0.toFloat() / (1 + Globals.rnd.nextFloat() * 0.1.toFloat())
                    p.color = 4 + (Lib.rand() and 7)
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = move[j] + Lib.crand() * 2
                            p.vel[j] = Lib.crand() * 10
                            j++
                        }
                    }
                    p.org[2] -= 4
                    //			p.vel[2] += 6;
                    p.vel[2] += 20

                    Math3D.VectorAdd(move, vec, move)
                    i += dec.toInt()
                }
            }
        }

        // stack variable
        // move, vec, right, up
        private val dir = floatArray(0.0, 0.0, 0.0)
        private val end = floatArray(0.0, 0.0, 0.0)

        fun Heatbeam(start: FloatArray, forward: FloatArray) {
            val len: Float
            var j: Int
            var p: cparticle_t
            var i: Int
            var c: Float
            var s: Float
            val ltime: Float
            val step = 32.0.toFloat()
            val rstep: Float
            val start_pt: Float
            var rot: Float
            var variance: Float

            Math3D.VectorMA(start, 4096, forward, end)

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            // FIXME - pmm - these might end up using old values?
            //		MakeNormalVectors (vec, right, up);
            Math3D.VectorCopy(Globals.cl.v_right, right)
            Math3D.VectorCopy(Globals.cl.v_up, up)
            if (Globals.vidref_val == Defines.VIDREF_GL) {
                // GL mode
                Math3D.VectorMA(move, -0.5.toFloat(), right, move)
                Math3D.VectorMA(move, -0.5.toFloat(), up, move)
            }
            // otherwise assume SOFT

            ltime = Globals.cl.time as Float / 1000.0.toFloat()
            start_pt = ltime * 96.0.toFloat() % step
            Math3D.VectorMA(move, start_pt, vec, move)

            Math3D.VectorScale(vec, step, vec)

            //		Com_Printf ("%f\n", ltime);
            rstep = (Math.PI / 10.0) as Float
            val M_PI2 = (Math.PI * 2.0) as Float
            run {
                i = start_pt.toInt()
                while (i < len) {
                    if (i > step * 5)
                    // don't bother after the 5th ring
                        break

                    run {
                        rot = 0
                        while (rot < M_PI2) {

                            if (CL_fx.free_particles == null)
                                return

                            p = CL_fx.free_particles
                            CL_fx.free_particles = p.next
                            p.next = CL_fx.active_particles
                            CL_fx.active_particles = p

                            p.time = Globals.cl.time
                            Math3D.VectorClear(p.accel)
                            //				rot+= fmod(ltime, 12.0)*M_PI;
                            //				c = cos(rot)/2.0;
                            //				s = sin(rot)/2.0;
                            //				variance = 0.4 + ((float)rand()/(float)RAND_MAX) *0.2;
                            variance = 0.5.toFloat()
                            c = (Math.cos(rot) * variance) as Float
                            s = (Math.sin(rot) * variance) as Float

                            // trim it so it looks like it's starting at the origin
                            if (i < 10) {
                                Math3D.VectorScale(right, c * (i.toFloat() / 10.0.toFloat()), dir)
                                Math3D.VectorMA(dir, s * (i.toFloat() / 10.0.toFloat()), up, dir)
                            } else {
                                Math3D.VectorScale(right, c, dir)
                                Math3D.VectorMA(dir, s, up, dir)
                            }

                            p.alpha = 0.5.toFloat()
                            //		p.alphavel = -1.0 / (1+frand()*0.2);
                            p.alphavel = -1000.0.toFloat()
                            //		p.color = 0x74 + (rand()&7);
                            p.color = 223 - (Lib.rand() and 7)
                            run {
                                j = 0
                                while (j < 3) {
                                    p.org[j] = move[j] + dir[j] * 3
                                    //			p.vel[j] = dir[j]*6;
                                    p.vel[j] = 0
                                    j++
                                }
                            }
                            rot += rstep
                        }
                    }
                    Math3D.VectorAdd(move, vec, move)
                    i += step.toInt()
                }
            }
        }

        // stack variable
        private val r = floatArray(0.0, 0.0, 0.0)
        private val u = floatArray(0.0, 0.0, 0.0)
        /*
     * =============== CL_ParticleSteamEffect
     * 
     * Puffs with velocity along direction, with some randomness thrown in
     * ===============
     */
        fun ParticleSteamEffect(org: FloatArray, dir: FloatArray, color: Int, count: Int, magnitude: Int) {
            var i: Int
            var j: Int
            var p: cparticle_t
            var d: Float

            //		vectoangles2 (dir, angle_dir);
            //		AngleVectors (angle_dir, f, r, u);

            Math3D.MakeNormalVectors(dir, r, u)

            run {
                i = 0
                while (i < count) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = color + (Lib.rand() and 7)

                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = org[j] + magnitude * 0.1.toFloat() * Lib.crand()
                            j++
                            //				p.vel[j] = dir[j]*magnitude;
                        }
                    }
                    Math3D.VectorScale(dir, magnitude, p.vel)
                    d = Lib.crand() * magnitude / 3
                    Math3D.VectorMA(p.vel, d, r, p.vel)
                    d = Lib.crand() * magnitude / 3
                    Math3D.VectorMA(p.vel, d, u, p.vel)

                    p.accel[0] = p.accel[1] = 0
                    p.accel[2] = -CL_fx.PARTICLE_GRAVITY / 2
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
                    i++
                }
            }
        }

        // stack variable
        // r, u, dir
        fun ParticleSteamEffect2(self: cl_sustain_t) //	  float[] org, float[] dir, int color, int count, int magnitude)
        {
            var i: Int
            var j: Int
            var p: cparticle_t
            var d: Float

            //		vectoangles2 (dir, angle_dir);
            //		AngleVectors (angle_dir, f, r, u);

            Math3D.VectorCopy(self.dir, dir)
            Math3D.MakeNormalVectors(dir, r, u)

            run {
                i = 0
                while (i < self.count) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = self.color + (Lib.rand() and 7)

                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = self.org[j] + self.magnitude * 0.1.toFloat() * Lib.crand()
                            j++
                            //				p.vel[j] = dir[j]*magnitude;
                        }
                    }
                    Math3D.VectorScale(dir, self.magnitude, p.vel)
                    d = Lib.crand() * self.magnitude / 3
                    Math3D.VectorMA(p.vel, d, r, p.vel)
                    d = Lib.crand() * self.magnitude / 3
                    Math3D.VectorMA(p.vel, d, u, p.vel)

                    p.accel[0] = p.accel[1] = 0
                    p.accel[2] = -CL_fx.PARTICLE_GRAVITY / 2
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
                    i++
                }
            }
            self.nextthink += self.thinkinterval
        }

        // stack variable
        // move, vec, right, up
        private val forward = floatArray(0.0, 0.0, 0.0)
        private val angle_dir = floatArray(0.0, 0.0, 0.0)
        /*
     * =============== CL_TrackerTrail ===============
     */
        fun TrackerTrail(start: FloatArray, end: FloatArray, particleColor: Int) {
            var len: Float
            val p: cparticle_t
            val dec: Int
            val dist: Float

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            Math3D.VectorCopy(vec, forward)
            Math3D.vectoangles(forward, angle_dir)
            Math3D.AngleVectors(angle_dir, forward, right, up)

            dec = 3
            Math3D.VectorScale(vec, 3, vec)

            // FIXME: this is a really silly way to have a loop
            while (len > 0) {
                len -= dec.toFloat()

                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = -2.0.toFloat()
                p.color = particleColor
                dist = Math3D.DotProduct(move, forward)
                Math3D.VectorMA(move, (8 * Math.cos(dist)) as Float, up, p.org)
                for (j in 0..3 - 1) {
                    p.vel[j] = 0
                    p.accel[j] = 0
                }
                p.vel[2] = 5

                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // dir
        fun Tracker_Shell(origin: FloatArray) {
            val p: cparticle_t

            for (i in 0..300 - 1) {
                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = CL_fx.INSTANT_PARTICLE
                p.color = 0

                dir[0] = Lib.crand()
                dir[1] = Lib.crand()
                dir[2] = Lib.crand()
                Math3D.VectorNormalize(dir)

                Math3D.VectorMA(origin, 40, dir, p.org)
            }
        }

        // stack variable
        // dir
        fun MonsterPlasma_Shell(origin: FloatArray) {
            val p: cparticle_t

            for (i in 0..40 - 1) {
                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = CL_fx.INSTANT_PARTICLE
                p.color = 224

                dir[0] = Lib.crand()
                dir[1] = Lib.crand()
                dir[2] = Lib.crand()
                Math3D.VectorNormalize(dir)

                Math3D.VectorMA(origin, 10, dir, p.org)
                //			VectorMA(origin, 10*(((rand () & 0x7fff) / ((float)0x7fff))),
                // dir, p.org);
            }
        }

        private val wb_colortable = intArray(2 * 8, 13 * 8, 21 * 8, 18 * 8)

        // stack variable
        // dir
        fun Widowbeamout(self: cl_sustain_t) {
            var i: Int
            var p: cparticle_t

            val ratio: Float

            ratio = 1.0.toFloat() - ((self.endtime as Float - Globals.cl.time as Float) / 2100.0.toFloat())

            run {
                i = 0
                while (i < 300) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p
                    Math3D.VectorClear(p.accel)

                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = CL_fx.INSTANT_PARTICLE
                    p.color = wb_colortable[Lib.rand() and 3]

                    dir[0] = Lib.crand()
                    dir[1] = Lib.crand()
                    dir[2] = Lib.crand()
                    Math3D.VectorNormalize(dir)

                    Math3D.VectorMA(self.org, (45.0.toFloat() * ratio), dir, p.org)
                    i++
                    //			VectorMA(origin, 10*(((rand () & 0x7fff) / ((float)0x7fff))),
                    // dir, p.org);
                }
            }
        }

        private val nb_colortable = intArray(110, 112, 114, 116)

        // stack variable
        // dir
        fun Nukeblast(self: cl_sustain_t) {
            var i: Int
            var p: cparticle_t

            val ratio: Float

            ratio = 1.0.toFloat() - ((self.endtime as Float - Globals.cl.time as Float) / 1000.0.toFloat())

            run {
                i = 0
                while (i < 700) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p
                    Math3D.VectorClear(p.accel)

                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = CL_fx.INSTANT_PARTICLE
                    p.color = nb_colortable[Lib.rand() and 3]

                    dir[0] = Lib.crand()
                    dir[1] = Lib.crand()
                    dir[2] = Lib.crand()
                    Math3D.VectorNormalize(dir)

                    Math3D.VectorMA(self.org, (200.0.toFloat() * ratio), dir, p.org)
                    i++
                    //			VectorMA(origin, 10*(((rand () & 0x7fff) / ((float)0x7fff))),
                    // dir, p.org);
                }
            }
        }

        private val ws_colortable = intArray(2 * 8, 13 * 8, 21 * 8, 18 * 8)

        // stack variable
        // dir
        fun WidowSplash(org: FloatArray) {
            var i: Int
            var p: cparticle_t

            run {
                i = 0
                while (i < 256) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = ws_colortable[Lib.rand() and 3]

                    dir[0] = Lib.crand()
                    dir[1] = Lib.crand()
                    dir[2] = Lib.crand()
                    Math3D.VectorNormalize(dir)
                    Math3D.VectorMA(org, 45.0.toFloat(), dir, p.org)
                    Math3D.VectorMA(Globals.vec3_origin, 40.0.toFloat(), dir, p.vel)

                    p.accel[0] = p.accel[1] = 0
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -0.8.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
                    i++
                }
            }

        }

        // stack variable
        // move, vec
        /*
     * ===============
     *  CL_TagTrail
     * ===============
     */
        fun TagTrail(start: FloatArray, end: FloatArray, color: Float) {
            var len: Float
            var j: Int
            val p: cparticle_t
            val dec: Int

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 5
            Math3D.VectorScale(vec, 5, vec)

            while (len >= 0) {
                len -= dec.toFloat()

                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = -1.0.toFloat() / (0.8.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = color
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = move[j] + Lib.crand() * 16
                        p.vel[j] = Lib.crand() * 5
                        p.accel[j] = 0
                        j++
                    }
                }

                Math3D.VectorAdd(move, vec, move)
            }
        }

        /*
     * =============== CL_ColorExplosionParticles ===============
     */
        fun ColorExplosionParticles(org: FloatArray, color: Int, run: Int) {
            val i: Int
            val j: Int
            val p: cparticle_t

            run {
                i = 0
                while (i < 128) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = color + (Lib.rand() % run)

                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = org[j] + ((Lib.rand() % 32) - 16)
                            p.vel[j] = (Lib.rand() % 256) - 128
                            j++
                        }
                    }

                    p.accel[0] = p.accel[1] = 0
                    p.accel[2] = -CL_fx.PARTICLE_GRAVITY
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -0.4.toFloat() / (0.6.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                    i++
                }
            }
        }

        // stack variable
        // r, u
        /*
     * =============== CL_ParticleSmokeEffect - like the steam effect, but
     * unaffected by gravity ===============
     */
        fun ParticleSmokeEffect(org: FloatArray, dir: FloatArray, color: Int, count: Int, magnitude: Int) {
            var i: Int
            var j: Int
            var p: cparticle_t
            var d: Float

            Math3D.MakeNormalVectors(dir, r, u)

            run {
                i = 0
                while (i < count) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = color + (Lib.rand() and 7)

                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = org[j] + magnitude * 0.1.toFloat() * Lib.crand()
                            j++
                            //				p.vel[j] = dir[j]*magnitude;
                        }
                    }
                    Math3D.VectorScale(dir, magnitude, p.vel)
                    d = Lib.crand() * magnitude / 3
                    Math3D.VectorMA(p.vel, d, r, p.vel)
                    d = Lib.crand() * magnitude / 3
                    Math3D.VectorMA(p.vel, d, u, p.vel)

                    p.accel[0] = p.accel[1] = p.accel[2] = 0
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
                    i++
                }
            }
        }

        /*
     * =============== CL_BlasterParticles2
     * 
     * Wall impact puffs (Green) ===============
     */
        fun BlasterParticles2(org: FloatArray, dir: FloatArray, color: Long) {
            var i: Int
            var j: Int
            var p: cparticle_t
            var d: Float
            val count: Int

            count = 40
            run {
                i = 0
                while (i < count) {
                    if (CL_fx.free_particles == null)
                        return
                    p = CL_fx.free_particles
                    CL_fx.free_particles = p.next
                    p.next = CL_fx.active_particles
                    CL_fx.active_particles = p

                    p.time = Globals.cl.time
                    p.color = color + (Lib.rand() and 7)

                    d = Lib.rand() and 15
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = org[j] + ((Lib.rand() and 7) - 4) + d * dir[j]
                            p.vel[j] = dir[j] * 30 + Lib.crand() * 40
                            j++
                        }
                    }

                    p.accel[0] = p.accel[1] = 0
                    p.accel[2] = -CL_fx.PARTICLE_GRAVITY
                    p.alpha = 1.0.toFloat()

                    p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
                    i++
                }
            }
        }

        // stack variable
        // move, vec
        /*
     * =============== CL_BlasterTrail2
     * 
     * Green! ===============
     */
        fun BlasterTrail2(start: FloatArray, end: FloatArray) {
            var len: Float
            var j: Int
            val p: cparticle_t
            val dec: Int

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 5
            Math3D.VectorScale(vec, 5, vec)

            // FIXME: this is a really silly way to have a loop
            while (len > 0) {
                len -= dec.toFloat()

                if (CL_fx.free_particles == null)
                    return
                p = CL_fx.free_particles
                CL_fx.free_particles = p.next
                p.next = CL_fx.active_particles
                CL_fx.active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = -1.0.toFloat() / (0.3.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = 208
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = move[j] + Lib.crand()
                        p.vel[j] = Lib.crand() * 5
                        p.accel[j] = 0
                        j++
                    }
                }

                Math3D.VectorAdd(move, vec, move)
            }
        }
    }
}