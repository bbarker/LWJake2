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
import lwjake2.game.entity_state_t
import lwjake2.game.monsters.M_Flash
import lwjake2.qcommon.Com
import lwjake2.qcommon.MSG
import lwjake2.sound.S
import lwjake2.util.Lib
import lwjake2.util.Math3D

/**
 * Client Graphics Effects.
 */
public class CL_fx {

    class cdlight_t {
        var key: Int = 0 // so entities can reuse same entry

        var color = floatArray(0.0, 0.0, 0.0)

        var origin = floatArray(0.0, 0.0, 0.0)

        var radius: Float = 0.toFloat()

        var die: Float = 0.toFloat() // stop lighting after this time

        var minlight: Float = 0.toFloat() // don't add when contributing less

        fun clear() {
            radius = minlight = color[0] = color[1] = color[2] = 0
        }
    }

    /*
	 * ==============================================================
	 * 
	 * LIGHT STYLE MANAGEMENT
	 * 
	 * ==============================================================
	 */

    class clightstyle_t {
        var length: Int = 0

        var value = FloatArray(3)

        var map = FloatArray(Defines.MAX_QPATH)

        fun clear() {
            value[0] = value[1] = value[2] = (length = 0).toFloat()
            for (i in map.indices)
                map[i] = 0.0.toFloat()
        }
    }

    companion object {

        var particles = arrayOfNulls<cparticle_t>(Defines.MAX_PARTICLES)
        {
            for (i in particles.indices)
                particles[i] = cparticle_t()
        }

        var cl_numparticles = Defines.MAX_PARTICLES

        val INSTANT_PARTICLE = -10000.0.toFloat()

        var avelocities = Array<FloatArray>(Defines.NUMVERTEXNORMALS, { FloatArray(3) })

        var cl_lightstyle = arrayOfNulls<clightstyle_t>(Defines.MAX_LIGHTSTYLES)

        var lastofs: Int = 0

        {
            for (i in cl_lightstyle.indices) {
                cl_lightstyle[i] = clightstyle_t()
            }
        }

        /*
	 * ==============================================================
	 * 
	 * DLIGHT MANAGEMENT
	 * 
	 * ==============================================================
	 */

        var cl_dlights = arrayOfNulls<cdlight_t>(Defines.MAX_DLIGHTS)
        {
            for (i in cl_dlights.indices)
                cl_dlights[i] = cdlight_t()
        }

        /*
	 * ================ CL_ClearDlights ================
	 */
        fun ClearDlights() {
            //		memset (cl_dlights, 0, sizeof(cl_dlights));
            for (i in cl_dlights.indices) {
                cl_dlights[i].clear()
            }
        }

        /*
	 * ================ CL_ClearLightStyles ================
	 */
        fun ClearLightStyles() {
            //memset (cl_lightstyle, 0, sizeof(cl_lightstyle));
            for (i in cl_lightstyle.indices)
                cl_lightstyle[i].clear()
            lastofs = -1
        }

        /*
	 * ================ CL_RunLightStyles ================
	 */
        fun RunLightStyles() {
            val ls: clightstyle_t

            val ofs = Globals.cl.time / 100
            if (ofs == lastofs)
                return
            lastofs = ofs

            for (i in cl_lightstyle.indices) {
                ls = cl_lightstyle[i]
                if (ls.length == 0) {
                    ls.value[0] = ls.value[1] = ls.value[2] = 1.0.toFloat()
                    continue
                }
                if (ls.length == 1)
                    ls.value[0] = ls.value[1] = ls.value[2] = ls.map[0]
                else
                    ls.value[0] = ls.value[1] = ls.value[2] = ls.map[ofs % ls.length]
            }
        }

        fun SetLightstyle(i: Int) {
            val s: String
            val j: Int
            var k: Int

            s = Globals.cl.configstrings[i + Defines.CS_LIGHTS]

            j = s.length()
            if (j >= Defines.MAX_QPATH)
                Com.Error(Defines.ERR_DROP, "svc_lightstyle length=" + j)

            cl_lightstyle[i].length = j

            run {
                k = 0
                while (k < j) {
                    cl_lightstyle[i].map[k] = (s.charAt(k) - 'a') as Float / ('m' - 'a').toFloat()
                    k++
                }
            }
        }

        /*
	 * ================ CL_AddLightStyles ================
	 */
        fun AddLightStyles() {
            val ls: clightstyle_t

            for (i in cl_lightstyle.indices) {
                ls = cl_lightstyle[i]
                V.AddLightStyle(i, ls.value[0], ls.value[1], ls.value[2])
            }
        }

        /*
	 * =============== CL_AllocDlight
	 * 
	 * ===============
	 */
        fun AllocDlight(key: Int): cdlight_t {
            var i: Int
            var dl: cdlight_t

            //	   first look for an exact key match
            if (key != 0) {
                run {
                    i = 0
                    while (i < Defines.MAX_DLIGHTS) {
                        dl = cl_dlights[i]
                        if (dl.key == key) {
                            //memset (dl, 0, sizeof(*dl));
                            dl.clear()
                            dl.key = key
                            return dl
                        }
                        i++
                    }
                }
            }

            //	   then look for anything else
            run {
                i = 0
                while (i < Defines.MAX_DLIGHTS) {
                    dl = cl_dlights[i]
                    if (dl.die < Globals.cl.time) {
                        //memset (dl, 0, sizeof(*dl));
                        dl.clear()
                        dl.key = key
                        return dl
                    }
                    i++
                }
            }

            //dl = &cl_dlights[0];
            //memset (dl, 0, sizeof(*dl));
            dl = cl_dlights[0]
            dl.clear()
            dl.key = key
            return dl
        }


        /*
	 * =============== 
	 * CL_RunDLights
	 * ===============
	 */
        fun RunDLights() {
            val dl: cdlight_t

            for (i in 0..Defines.MAX_DLIGHTS - 1) {
                dl = cl_dlights[i]
                if (dl.radius == 0.0.toFloat())
                    continue

                if (dl.die < Globals.cl.time) {
                    dl.radius = 0.0.toFloat()
                    return
                }
            }
        }

        // stack variable
        private val fv = floatArray(0.0, 0.0, 0.0)
        private val rv = floatArray(0.0, 0.0, 0.0)
        /*
	 * ==============
	 *  CL_ParseMuzzleFlash
	 * ==============
	 */
        fun ParseMuzzleFlash() {
            val volume: Float
            var soundname: String

            val i = MSG.ReadShort(Globals.net_message)
            if (i < 1 || i >= Defines.MAX_EDICTS)
                Com.Error(Defines.ERR_DROP, "CL_ParseMuzzleFlash: bad entity")

            var weapon = MSG.ReadByte(Globals.net_message)
            val silenced = weapon and Defines.MZ_SILENCED
            weapon = weapon and Defines.MZ_SILENCED.inv()

            val pl = Globals.cl_entities[i]

            val dl = AllocDlight(i)
            Math3D.VectorCopy(pl.current.origin, dl.origin)
            Math3D.AngleVectors(pl.current.angles, fv, rv, null)
            Math3D.VectorMA(dl.origin, 18, fv, dl.origin)
            Math3D.VectorMA(dl.origin, 16, rv, dl.origin)
            if (silenced != 0)
                dl.radius = 100 + (Globals.rnd.nextInt() and 31)
            else
                dl.radius = 200 + (Globals.rnd.nextInt() and 31)
            dl.minlight = 32
            dl.die = Globals.cl.time // + 0.1;

            if (silenced != 0)
                volume = 0.2.toFloat()
            else
                volume = 1

            when (weapon) {
                Defines.MZ_BLASTER -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/blastf1a.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_BLUEHYPERBLASTER -> {
                    dl.color[0] = 0
                    dl.color[1] = 0
                    dl.color[2] = 1
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/hyprbf1a.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_HYPERBLASTER -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/hyprbf1a.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_MACHINEGUN -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_SHOTGUN -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/shotgf1b.wav"), volume, Defines.ATTN_NORM, 0)
                    S.StartSound(null, i, Defines.CHAN_AUTO, S.RegisterSound("weapons/shotgr1b.wav"), volume, Defines.ATTN_NORM, 0.1.toFloat())
                }
                Defines.MZ_SSHOTGUN -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/sshotf1b.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_CHAINGUN1 -> {
                    dl.radius = 200 + (Globals.rnd.nextInt() and 31)
                    dl.color[0] = 1
                    dl.color[1] = 0.25.toFloat()
                    dl.color[2] = 0
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_CHAINGUN2 -> {
                    dl.radius = 225 + (Globals.rnd.nextInt() and 31)
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 0.1.toFloat() // long delay
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0)
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0.05.toFloat())
                }
                Defines.MZ_CHAINGUN3 -> {
                    dl.radius = 250 + (Globals.rnd.nextInt() and 31)
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 0.1.toFloat() // long delay
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0)
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0.033.toFloat())
                    //Com_sprintf(soundname, sizeof(soundname),
                    // "weapons/machgf%ib.wav", (rand() % 5) + 1);
                    soundname = "weapons/machgf" + ((Globals.rnd.nextInt(5)) + 1) + "b.wav"
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound(soundname), volume, Defines.ATTN_NORM, 0.066.toFloat())
                }
                Defines.MZ_RAILGUN -> {
                    dl.color[0] = 0.5.toFloat()
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 1.0.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/railgf1a.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_ROCKET -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.2.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/rocklf1a.wav"), volume, Defines.ATTN_NORM, 0)
                    S.StartSound(null, i, Defines.CHAN_AUTO, S.RegisterSound("weapons/rocklr1b.wav"), volume, Defines.ATTN_NORM, 0.1.toFloat())
                }
                Defines.MZ_GRENADE -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/grenlf1a.wav"), volume, Defines.ATTN_NORM, 0)
                    S.StartSound(null, i, Defines.CHAN_AUTO, S.RegisterSound("weapons/grenlr1b.wav"), volume, Defines.ATTN_NORM, 0.1.toFloat())
                }
                Defines.MZ_BFG -> {
                    dl.color[0] = 0
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/bfg__f1y.wav"), volume, Defines.ATTN_NORM, 0)
                }

                Defines.MZ_LOGIN -> {
                    dl.color[0] = 0
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 1.0.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/grenlf1a.wav"), 1, Defines.ATTN_NORM, 0)
                    LogoutEffect(pl.current.origin, weapon)
                }
                Defines.MZ_LOGOUT -> {
                    dl.color[0] = 1
                    dl.color[1] = 0
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 1.0.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/grenlf1a.wav"), 1, Defines.ATTN_NORM, 0)
                    LogoutEffect(pl.current.origin, weapon)
                }
                Defines.MZ_RESPAWN -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 1.0.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/grenlf1a.wav"), 1, Defines.ATTN_NORM, 0)
                    LogoutEffect(pl.current.origin, weapon)
                }
            // RAFAEL
                Defines.MZ_PHALANX -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.5.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/plasshot.wav"), volume, Defines.ATTN_NORM, 0)
                }
            // RAFAEL
                Defines.MZ_IONRIPPER -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.5.toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/rippfire.wav"), volume, Defines.ATTN_NORM, 0)
                }

            //	   ======================
            //	   PGM
                Defines.MZ_ETF_RIFLE -> {
                    dl.color[0] = 0.9.toFloat()
                    dl.color[1] = 0.7.toFloat()
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/nail1.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_SHOTGUN2 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/shotg2.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_HEATBEAM -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 100
                }
                Defines.MZ_BLASTER2 -> {
                    dl.color[0] = 0
                    dl.color[1] = 1
                    dl.color[2] = 0
                    // FIXME - different sound for blaster2 ??
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/blastf1a.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_TRACKER -> {
                    // negative flashes handled the same in gl/soft until CL_AddDLights
                    dl.color[0] = (-1).toFloat()
                    dl.color[1] = (-1).toFloat()
                    dl.color[2] = (-1).toFloat()
                    S.StartSound(null, i, Defines.CHAN_WEAPON, S.RegisterSound("weapons/disint2.wav"), volume, Defines.ATTN_NORM, 0)
                }
                Defines.MZ_NUKE1 -> {
                    dl.color[0] = 1
                    dl.color[1] = 0
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 100
                }
                Defines.MZ_NUKE2 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 100
                }
                Defines.MZ_NUKE4 -> {
                    dl.color[0] = 0
                    dl.color[1] = 0
                    dl.color[2] = 1
                    dl.die = Globals.cl.time + 100
                }
                Defines.MZ_NUKE8 -> {
                    dl.color[0] = 0
                    dl.color[1] = 1
                    dl.color[2] = 1
                    dl.die = Globals.cl.time + 100
                }
            }//			S.StartSound (null, i, CHAN_WEAPON,
            // S.RegisterSound("weapons/bfg__l1a.wav"), volume, ATTN_NORM, 0);
            //	   PGM
            //	   ======================
        }

        // stack variable
        private val origin = floatArray(0.0, 0.0, 0.0)
        private val forward = floatArray(0.0, 0.0, 0.0)
        private val right = floatArray(0.0, 0.0, 0.0)
        /*
	 * ============== CL_ParseMuzzleFlash2 ==============
	 */
        fun ParseMuzzleFlash2() {
            val soundname: String

            val ent = MSG.ReadShort(Globals.net_message)
            if (ent < 1 || ent >= Defines.MAX_EDICTS)
                Com.Error(Defines.ERR_DROP, "CL_ParseMuzzleFlash2: bad entity")

            val flash_number = MSG.ReadByte(Globals.net_message)

            // locate the origin
            Math3D.AngleVectors(Globals.cl_entities[ent].current.angles, forward, right, null)
            origin[0] = Globals.cl_entities[ent].current.origin[0] + forward[0] * M_Flash.monster_flash_offset[flash_number][0] + right[0] * M_Flash.monster_flash_offset[flash_number][1]
            origin[1] = Globals.cl_entities[ent].current.origin[1] + forward[1] * M_Flash.monster_flash_offset[flash_number][0] + right[1] * M_Flash.monster_flash_offset[flash_number][1]
            origin[2] = Globals.cl_entities[ent].current.origin[2] + forward[2] * M_Flash.monster_flash_offset[flash_number][0] + right[2] * M_Flash.monster_flash_offset[flash_number][1] + M_Flash.monster_flash_offset[flash_number][2]

            val dl = AllocDlight(ent)
            Math3D.VectorCopy(origin, dl.origin)
            dl.radius = 200 + (Globals.rnd.nextInt() and 31)
            dl.minlight = 32
            dl.die = Globals.cl.time // + 0.1;

            when (flash_number) {
                Defines.MZ2_INFANTRY_MACHINEGUN_1, Defines.MZ2_INFANTRY_MACHINEGUN_2, Defines.MZ2_INFANTRY_MACHINEGUN_3, Defines.MZ2_INFANTRY_MACHINEGUN_4, Defines.MZ2_INFANTRY_MACHINEGUN_5, Defines.MZ2_INFANTRY_MACHINEGUN_6, Defines.MZ2_INFANTRY_MACHINEGUN_7, Defines.MZ2_INFANTRY_MACHINEGUN_8, Defines.MZ2_INFANTRY_MACHINEGUN_9, Defines.MZ2_INFANTRY_MACHINEGUN_10, Defines.MZ2_INFANTRY_MACHINEGUN_11, Defines.MZ2_INFANTRY_MACHINEGUN_12, Defines.MZ2_INFANTRY_MACHINEGUN_13 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("infantry/infatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_SOLDIER_MACHINEGUN_1, Defines.MZ2_SOLDIER_MACHINEGUN_2, Defines.MZ2_SOLDIER_MACHINEGUN_3, Defines.MZ2_SOLDIER_MACHINEGUN_4, Defines.MZ2_SOLDIER_MACHINEGUN_5, Defines.MZ2_SOLDIER_MACHINEGUN_6, Defines.MZ2_SOLDIER_MACHINEGUN_7, Defines.MZ2_SOLDIER_MACHINEGUN_8 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("soldier/solatck3.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_GUNNER_MACHINEGUN_1, Defines.MZ2_GUNNER_MACHINEGUN_2, Defines.MZ2_GUNNER_MACHINEGUN_3, Defines.MZ2_GUNNER_MACHINEGUN_4, Defines.MZ2_GUNNER_MACHINEGUN_5, Defines.MZ2_GUNNER_MACHINEGUN_6, Defines.MZ2_GUNNER_MACHINEGUN_7, Defines.MZ2_GUNNER_MACHINEGUN_8 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("gunner/gunatck2.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_ACTOR_MACHINEGUN_1, Defines.MZ2_SUPERTANK_MACHINEGUN_1, Defines.MZ2_SUPERTANK_MACHINEGUN_2, Defines.MZ2_SUPERTANK_MACHINEGUN_3, Defines.MZ2_SUPERTANK_MACHINEGUN_4, Defines.MZ2_SUPERTANK_MACHINEGUN_5, Defines.MZ2_SUPERTANK_MACHINEGUN_6, Defines.MZ2_TURRET_MACHINEGUN // PGM
                -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0

                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("infantry/infatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_BOSS2_MACHINEGUN_L1, Defines.MZ2_BOSS2_MACHINEGUN_L2, Defines.MZ2_BOSS2_MACHINEGUN_L3, Defines.MZ2_BOSS2_MACHINEGUN_L4, Defines.MZ2_BOSS2_MACHINEGUN_L5, Defines.MZ2_CARRIER_MACHINEGUN_L1 // PMM
                    , Defines.MZ2_CARRIER_MACHINEGUN_L2 // PMM
                -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0

                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("infantry/infatck1.wav"), 1, Defines.ATTN_NONE, 0)
                }

                Defines.MZ2_SOLDIER_BLASTER_1, Defines.MZ2_SOLDIER_BLASTER_2, Defines.MZ2_SOLDIER_BLASTER_3, Defines.MZ2_SOLDIER_BLASTER_4, Defines.MZ2_SOLDIER_BLASTER_5, Defines.MZ2_SOLDIER_BLASTER_6, Defines.MZ2_SOLDIER_BLASTER_7, Defines.MZ2_SOLDIER_BLASTER_8, Defines.MZ2_TURRET_BLASTER // PGM
                -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("soldier/solatck2.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_FLYER_BLASTER_1, Defines.MZ2_FLYER_BLASTER_2 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("flyer/flyatck3.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_MEDIC_BLASTER_1 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("medic/medatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_HOVER_BLASTER_1 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("hover/hovatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_FLOAT_BLASTER_1 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("floater/fltatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_SOLDIER_SHOTGUN_1, Defines.MZ2_SOLDIER_SHOTGUN_2, Defines.MZ2_SOLDIER_SHOTGUN_3, Defines.MZ2_SOLDIER_SHOTGUN_4, Defines.MZ2_SOLDIER_SHOTGUN_5, Defines.MZ2_SOLDIER_SHOTGUN_6, Defines.MZ2_SOLDIER_SHOTGUN_7, Defines.MZ2_SOLDIER_SHOTGUN_8 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("soldier/solatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_TANK_BLASTER_1, Defines.MZ2_TANK_BLASTER_2, Defines.MZ2_TANK_BLASTER_3 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("tank/tnkatck3.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_TANK_MACHINEGUN_1, Defines.MZ2_TANK_MACHINEGUN_2, Defines.MZ2_TANK_MACHINEGUN_3, Defines.MZ2_TANK_MACHINEGUN_4, Defines.MZ2_TANK_MACHINEGUN_5, Defines.MZ2_TANK_MACHINEGUN_6, Defines.MZ2_TANK_MACHINEGUN_7, Defines.MZ2_TANK_MACHINEGUN_8, Defines.MZ2_TANK_MACHINEGUN_9, Defines.MZ2_TANK_MACHINEGUN_10, Defines.MZ2_TANK_MACHINEGUN_11, Defines.MZ2_TANK_MACHINEGUN_12, Defines.MZ2_TANK_MACHINEGUN_13, Defines.MZ2_TANK_MACHINEGUN_14, Defines.MZ2_TANK_MACHINEGUN_15, Defines.MZ2_TANK_MACHINEGUN_16, Defines.MZ2_TANK_MACHINEGUN_17, Defines.MZ2_TANK_MACHINEGUN_18, Defines.MZ2_TANK_MACHINEGUN_19 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    //Com_sprintf(soundname, sizeof(soundname), "tank/tnkatk2%c.wav",
                    // 'a' + rand() % 5);
                    soundname = "tank/tnkatk2" + ('a' + Globals.rnd.nextInt(5)) as Char + ".wav"
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound(soundname), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_CHICK_ROCKET_1, Defines.MZ2_TURRET_ROCKET // PGM
                -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.2.toFloat()
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("chick/chkatck2.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_TANK_ROCKET_1, Defines.MZ2_TANK_ROCKET_2, Defines.MZ2_TANK_ROCKET_3 -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.2.toFloat()
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("tank/tnkatck1.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_SUPERTANK_ROCKET_1, Defines.MZ2_SUPERTANK_ROCKET_2, Defines.MZ2_SUPERTANK_ROCKET_3, Defines.MZ2_BOSS2_ROCKET_1, Defines.MZ2_BOSS2_ROCKET_2, Defines.MZ2_BOSS2_ROCKET_3, Defines.MZ2_BOSS2_ROCKET_4, Defines.MZ2_CARRIER_ROCKET_1 -> {
                    //		case MZ2_CARRIER_ROCKET_2:
                    //		case MZ2_CARRIER_ROCKET_3:
                    //		case MZ2_CARRIER_ROCKET_4:
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0.2.toFloat()
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("tank/rocket.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_GUNNER_GRENADE_1, Defines.MZ2_GUNNER_GRENADE_2, Defines.MZ2_GUNNER_GRENADE_3, Defines.MZ2_GUNNER_GRENADE_4 -> {
                    dl.color[0] = 1
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("gunner/gunatck3.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_GLADIATOR_RAILGUN_1, // PMM
                Defines.MZ2_CARRIER_RAILGUN, Defines.MZ2_WIDOW_RAIL -> {
                    // pmm
                    dl.color[0] = 0.5.toFloat()
                    dl.color[1] = 0.5.toFloat()
                    dl.color[2] = 1.0.toFloat()
                }

            //	   --- Xian's shit starts ---
                Defines.MZ2_MAKRON_BFG -> {
                    dl.color[0] = 0.5.toFloat()
                    dl.color[1] = 1
                    dl.color[2] = 0.5.toFloat()
                }

                Defines.MZ2_MAKRON_BLASTER_1, Defines.MZ2_MAKRON_BLASTER_2, Defines.MZ2_MAKRON_BLASTER_3, Defines.MZ2_MAKRON_BLASTER_4, Defines.MZ2_MAKRON_BLASTER_5, Defines.MZ2_MAKRON_BLASTER_6, Defines.MZ2_MAKRON_BLASTER_7, Defines.MZ2_MAKRON_BLASTER_8, Defines.MZ2_MAKRON_BLASTER_9, Defines.MZ2_MAKRON_BLASTER_10, Defines.MZ2_MAKRON_BLASTER_11, Defines.MZ2_MAKRON_BLASTER_12, Defines.MZ2_MAKRON_BLASTER_13, Defines.MZ2_MAKRON_BLASTER_14, Defines.MZ2_MAKRON_BLASTER_15, Defines.MZ2_MAKRON_BLASTER_16, Defines.MZ2_MAKRON_BLASTER_17 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("makron/blaster.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_JORG_MACHINEGUN_L1, Defines.MZ2_JORG_MACHINEGUN_L2, Defines.MZ2_JORG_MACHINEGUN_L3, Defines.MZ2_JORG_MACHINEGUN_L4, Defines.MZ2_JORG_MACHINEGUN_L5, Defines.MZ2_JORG_MACHINEGUN_L6 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("boss3/xfire.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_JORG_MACHINEGUN_R1, Defines.MZ2_JORG_MACHINEGUN_R2, Defines.MZ2_JORG_MACHINEGUN_R3, Defines.MZ2_JORG_MACHINEGUN_R4, Defines.MZ2_JORG_MACHINEGUN_R5, Defines.MZ2_JORG_MACHINEGUN_R6 -> {
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                }

                Defines.MZ2_JORG_BFG_1 -> {
                    dl.color[0] = 0.5.toFloat()
                    dl.color[1] = 1
                    dl.color[2] = 0.5.toFloat()
                }

                Defines.MZ2_BOSS2_MACHINEGUN_R1, Defines.MZ2_BOSS2_MACHINEGUN_R2, Defines.MZ2_BOSS2_MACHINEGUN_R3, Defines.MZ2_BOSS2_MACHINEGUN_R4, Defines.MZ2_BOSS2_MACHINEGUN_R5, Defines.MZ2_CARRIER_MACHINEGUN_R1 // PMM
                    , Defines.MZ2_CARRIER_MACHINEGUN_R2 // PMM
                -> {

                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0

                    ParticleEffect(origin, Globals.vec3_origin, 0, 40)
                    CL_tent.SmokeAndFlash(origin)
                }

            //	   ======
            //	   ROGUE
                Defines.MZ2_STALKER_BLASTER, Defines.MZ2_DAEDALUS_BLASTER, Defines.MZ2_MEDIC_BLASTER_2, Defines.MZ2_WIDOW_BLASTER, Defines.MZ2_WIDOW_BLASTER_SWEEP1, Defines.MZ2_WIDOW_BLASTER_SWEEP2, Defines.MZ2_WIDOW_BLASTER_SWEEP3, Defines.MZ2_WIDOW_BLASTER_SWEEP4, Defines.MZ2_WIDOW_BLASTER_SWEEP5, Defines.MZ2_WIDOW_BLASTER_SWEEP6, Defines.MZ2_WIDOW_BLASTER_SWEEP7, Defines.MZ2_WIDOW_BLASTER_SWEEP8, Defines.MZ2_WIDOW_BLASTER_SWEEP9, Defines.MZ2_WIDOW_BLASTER_100, Defines.MZ2_WIDOW_BLASTER_90, Defines.MZ2_WIDOW_BLASTER_80, Defines.MZ2_WIDOW_BLASTER_70, Defines.MZ2_WIDOW_BLASTER_60, Defines.MZ2_WIDOW_BLASTER_50, Defines.MZ2_WIDOW_BLASTER_40, Defines.MZ2_WIDOW_BLASTER_30, Defines.MZ2_WIDOW_BLASTER_20, Defines.MZ2_WIDOW_BLASTER_10, Defines.MZ2_WIDOW_BLASTER_0, Defines.MZ2_WIDOW_BLASTER_10L, Defines.MZ2_WIDOW_BLASTER_20L, Defines.MZ2_WIDOW_BLASTER_30L, Defines.MZ2_WIDOW_BLASTER_40L, Defines.MZ2_WIDOW_BLASTER_50L, Defines.MZ2_WIDOW_BLASTER_60L, Defines.MZ2_WIDOW_BLASTER_70L, Defines.MZ2_WIDOW_RUN_1, Defines.MZ2_WIDOW_RUN_2, Defines.MZ2_WIDOW_RUN_3, Defines.MZ2_WIDOW_RUN_4, Defines.MZ2_WIDOW_RUN_5, Defines.MZ2_WIDOW_RUN_6, Defines.MZ2_WIDOW_RUN_7, Defines.MZ2_WIDOW_RUN_8 -> {
                    dl.color[0] = 0
                    dl.color[1] = 1
                    dl.color[2] = 0
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("tank/tnkatck3.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_WIDOW_DISRUPTOR -> {
                    dl.color[0] = (-1).toFloat()
                    dl.color[1] = (-1).toFloat()
                    dl.color[2] = (-1).toFloat()
                    S.StartSound(null, ent, Defines.CHAN_WEAPON, S.RegisterSound("weapons/disint2.wav"), 1, Defines.ATTN_NORM, 0)
                }

                Defines.MZ2_WIDOW_PLASMABEAM, Defines.MZ2_WIDOW2_BEAMER_1, Defines.MZ2_WIDOW2_BEAMER_2, Defines.MZ2_WIDOW2_BEAMER_3, Defines.MZ2_WIDOW2_BEAMER_4, Defines.MZ2_WIDOW2_BEAMER_5, Defines.MZ2_WIDOW2_BEAM_SWEEP_1, Defines.MZ2_WIDOW2_BEAM_SWEEP_2, Defines.MZ2_WIDOW2_BEAM_SWEEP_3, Defines.MZ2_WIDOW2_BEAM_SWEEP_4, Defines.MZ2_WIDOW2_BEAM_SWEEP_5, Defines.MZ2_WIDOW2_BEAM_SWEEP_6, Defines.MZ2_WIDOW2_BEAM_SWEEP_7, Defines.MZ2_WIDOW2_BEAM_SWEEP_8, Defines.MZ2_WIDOW2_BEAM_SWEEP_9, Defines.MZ2_WIDOW2_BEAM_SWEEP_10, Defines.MZ2_WIDOW2_BEAM_SWEEP_11 -> {
                    dl.radius = 300 + (Globals.rnd.nextInt() and 100)
                    dl.color[0] = 1
                    dl.color[1] = 1
                    dl.color[2] = 0
                    dl.die = Globals.cl.time + 200
                }
            }//S.StartSound (null, ent, CHAN_WEAPON,
            // S.RegisterSound("makron/bfg_fire.wav"), 1, ATTN_NORM, 0);
            //	   ROGUE
            //	   ======
            //	   --- Xian's shit ends ---
        }

        /*
	 * =============== CL_AddDLights
	 * 
	 * ===============
	 */
        fun AddDLights() {
            val dl: cdlight_t

            //	  =====
            //	  PGM
            if (Globals.vidref_val == Defines.VIDREF_GL) {
                for (i in 0..Defines.MAX_DLIGHTS - 1) {
                    dl = cl_dlights[i]
                    if (dl.radius == 0.0.toFloat())
                        continue
                    V.AddLight(dl.origin, dl.radius, dl.color[0], dl.color[1], dl.color[2])
                }
            } else {
                for (i in 0..Defines.MAX_DLIGHTS - 1) {
                    dl = cl_dlights[i]
                    if (dl.radius == 0.0.toFloat())
                        continue

                    // negative light in software. only black allowed
                    if ((dl.color[0] < 0) || (dl.color[1] < 0) || (dl.color[2] < 0)) {
                        dl.radius = -(dl.radius)
                        dl.color[0] = 1
                        dl.color[1] = 1
                        dl.color[2] = 1
                    }
                    V.AddLight(dl.origin, dl.radius, dl.color[0], dl.color[1], dl.color[2])
                }
            }
            //	  PGM
            //	  =====
        }

        /*
	 * =============== CL_ClearParticles ===============
	 */
        fun ClearParticles() {
            free_particles = particles[0]
            active_particles = null

            for (i in 0..particles.size() - 1 - 1)
                particles[i].next = particles[i + 1]
            particles[particles.size() - 1].next = null
        }

        /*
	 * =============== CL_ParticleEffect
	 * 
	 * Wall impact puffs ===============
	 */
        fun ParticleEffect(org: FloatArray, dir: FloatArray, color: Int, count: Int) {
            var j: Int
            val p: cparticle_t
            val d: Float

            for (i in 0..count - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = color + (Lib.rand() and 7)

                d = Lib.rand() and 31
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = org[j] + ((Lib.rand() and 7) - 4) + d * dir[j]
                        p.vel[j] = Lib.crand() * 20
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        /*
	 * =============== CL_ParticleEffect2 ===============
	 */
        fun ParticleEffect2(org: FloatArray, dir: FloatArray, color: Int, count: Int) {
            var j: Int
            val p: cparticle_t
            val d: Float

            for (i in 0..count - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = color

                d = Lib.rand() and 7
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = org[j] + ((Lib.rand() and 7) - 4) + d * dir[j]
                        p.vel[j] = Lib.crand() * 20
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        //	   RAFAEL
        /*
	 * =============== CL_ParticleEffect3 ===============
	 */
        fun ParticleEffect3(org: FloatArray, dir: FloatArray, color: Int, count: Int) {
            var j: Int
            val p: cparticle_t
            val d: Float

            for (i in 0..count - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = color

                d = Lib.rand() and 7
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = org[j] + ((Lib.rand() and 7) - 4) + d * dir[j]
                        p.vel[j] = Lib.crand() * 20
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        /*
	 * =============== CL_TeleporterParticles ===============
	 */
        fun TeleporterParticles(ent: entity_state_t) {
            var j: Int
            val p: cparticle_t

            for (i in 0..8 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = 219

                run {
                    j = 0
                    while (j < 2) {
                        p.org[j] = ent.origin[j] - 16 + (Lib.rand() and 31)
                        p.vel[j] = Lib.crand() * 14
                        j++
                    }
                }

                p.org[2] = ent.origin[2] - 8 + (Lib.rand() and 7)
                p.vel[2] = 80 + (Lib.rand() and 7)

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -0.5.toFloat()
            }
        }

        /*
	 * =============== CL_LogoutEffect
	 * 
	 * ===============
	 */
        fun LogoutEffect(org: FloatArray, type: Int) {
            var j: Int
            val p: cparticle_t

            for (i in 0..500 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time

                if (type == Defines.MZ_LOGIN)
                    p.color = 208 + (Lib.rand() and 7) // green
                else if (type == Defines.MZ_LOGOUT)
                    p.color = 64 + (Lib.rand() and 7) // red
                else
                    p.color = 224 + (Lib.rand() and 7) // yellow

                p.org[0] = org[0] - 16 + Globals.rnd.nextFloat() * 32
                p.org[1] = org[1] - 16 + Globals.rnd.nextFloat() * 32
                p.org[2] = org[2] - 24 + Globals.rnd.nextFloat() * 56

                run {
                    j = 0
                    while (j < 3) {
                        p.vel[j] = Lib.crand() * 20
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        /*
	 * =============== CL_ItemRespawnParticles
	 * 
	 * ===============
	 */
        fun ItemRespawnParticles(org: FloatArray) {
            var j: Int
            val p: cparticle_t

            for (i in 0..64 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time

                p.color = 212 + (Lib.rand() and 3) // green

                p.org[0] = org[0] + Lib.crand() * 8
                p.org[1] = org[1] + Lib.crand() * 8
                p.org[2] = org[2] + Lib.crand() * 8

                run {
                    j = 0
                    while (j < 3) {
                        p.vel[j] = Lib.crand() * 8
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = (-PARTICLE_GRAVITY).toFloat() * 0.2.toFloat()
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        /*
	 * =============== CL_ExplosionParticles ===============
	 */
        fun ExplosionParticles(org: FloatArray) {
            var j: Int
            val p: cparticle_t

            for (i in 0..256 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = 224 + (Lib.rand() and 7)

                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = org[j] + ((Lib.rand() % 32) - 16)
                        p.vel[j] = (Lib.rand() % 384) - 192
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0.0.toFloat()
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -0.8.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        fun BigTeleportParticles(org: FloatArray) {
            val p: cparticle_t
            val angle: Float
            val dist: Float

            for (i in 0..4096 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time

                p.color = colortable[Lib.rand() and 3]

                angle = (Math.PI * 2 * (Lib.rand() and 1023) / 1023.0) as Float
                dist = Lib.rand() and 31
                p.org[0] = (org[0] + Math.cos(angle) * dist) as Float
                p.vel[0] = (Math.cos(angle) * (70 + (Lib.rand() and 63))) as Float
                p.accel[0] = (-Math.cos(angle) * 100) as Float

                p.org[1] = (org[1] + Math.sin(angle) * dist) as Float
                p.vel[1] = (Math.sin(angle) * (70 + (Lib.rand() and 63))) as Float
                p.accel[1] = (-Math.sin(angle) * 100) as Float

                p.org[2] = org[2] + 8 + (Lib.rand() % 90)
                p.vel[2] = -100 + (Lib.rand() and 31)
                p.accel[2] = PARTICLE_GRAVITY * 4
                p.alpha = 1.0.toFloat()

                p.alphavel = -0.3.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        /*
	 * =============== CL_BlasterParticles
	 * 
	 * Wall impact puffs ===============
	 */
        fun BlasterParticles(org: FloatArray, dir: FloatArray) {
            var j: Int
            val p: cparticle_t
            val d: Float

            val count = 40
            for (i in 0..count - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = 224 + (Lib.rand() and 7)

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
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -1.0.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        // stack variable
        private val move = floatArray(0.0, 0.0, 0.0)
        private val vec = floatArray(0.0, 0.0, 0.0)
        /*
	 * =============== CL_BlasterTrail
	 * 
	 * ===============
	 */
        fun BlasterTrail(start: FloatArray, end: FloatArray) {
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

                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = -1.0.toFloat() / (0.3.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = 224
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

        // stack variable
        // move, vec
        /*
	 * ===============
	 *  CL_FlagTrail
	 * ===============
	 */
        fun FlagTrail(start: FloatArray, end: FloatArray, color: Float) {
            var len: Float
            var j: Int
            val p: cparticle_t
            val dec: Int

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 5
            Math3D.VectorScale(vec, 5, vec)

            while (len > 0) {
                len -= dec.toFloat()

                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p
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

        // stack variable
        // move, vec
        /*
	 * =============== CL_DiminishingTrail
	 * 
	 * ===============
	 */
        fun DiminishingTrail(start: FloatArray, end: FloatArray, old: centity_t, flags: Int) {
            val p: cparticle_t
            val orgscale: Float
            val velscale: Float

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            var len = Math3D.VectorNormalize(vec)

            val dec = 0.5.toFloat()
            Math3D.VectorScale(vec, dec, vec)

            if (old.trailcount > 900) {
                orgscale = 4
                velscale = 15
            } else if (old.trailcount > 800) {
                orgscale = 2
                velscale = 10
            } else {
                orgscale = 1
                velscale = 5
            }

            while (len > 0) {
                len -= dec

                if (free_particles == null)
                    return

                // drop less particles as it flies
                if ((Lib.rand() and 1023) < old.trailcount) {
                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p
                    Math3D.VectorClear(p.accel)

                    p.time = Globals.cl.time

                    if ((flags and Defines.EF_GIB) != 0) {
                        p.alpha = 1.0.toFloat()
                        p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.4.toFloat())
                        p.color = 232 + (Lib.rand() and 7)
                        for (j in 0..3 - 1) {
                            p.org[j] = move[j] + Lib.crand() * orgscale
                            p.vel[j] = Lib.crand() * velscale
                            p.accel[j] = 0
                        }
                        p.vel[2] -= PARTICLE_GRAVITY
                    } else if ((flags and Defines.EF_GREENGIB) != 0) {
                        p.alpha = 1.0.toFloat()
                        p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.4.toFloat())
                        p.color = 219 + (Lib.rand() and 7)
                        for (j in 0..3 - 1) {
                            p.org[j] = move[j] + Lib.crand() * orgscale
                            p.vel[j] = Lib.crand() * velscale
                            p.accel[j] = 0
                        }
                        p.vel[2] -= PARTICLE_GRAVITY
                    } else {
                        p.alpha = 1.0.toFloat()
                        p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                        p.color = 4 + (Lib.rand() and 7)
                        for (j in 0..3 - 1) {
                            p.org[j] = move[j] + Lib.crand() * orgscale
                            p.vel[j] = Lib.crand() * velscale
                        }
                        p.accel[2] = 20
                    }
                }

                old.trailcount -= 5
                if (old.trailcount < 100)
                    old.trailcount = 100
                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // move, vec
        /*
	 * =============== CL_RocketTrail
	 * 
	 * ===============
	 */
        fun RocketTrail(start: FloatArray, end: FloatArray, old: centity_t) {
            var len: Float
            var j: Int
            val p: cparticle_t
            val dec: Float

            // smoke
            DiminishingTrail(start, end, old, Defines.EF_ROCKET)

            // fire
            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 1
            Math3D.VectorScale(vec, dec, vec)

            while (len > 0) {
                len -= dec

                if (free_particles == null)
                    return

                if ((Lib.rand() and 7) == 0) {
                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p

                    Math3D.VectorClear(p.accel)
                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                    p.color = 220 + (Lib.rand() and 3)
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = move[j] + Lib.crand() * 5
                            p.vel[j] = Lib.crand() * 20
                            j++
                        }
                    }
                    p.accel[2] = -PARTICLE_GRAVITY
                }
                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // move, vec
        /*
	 * =============== CL_RailTrail
	 * 
	 * ===============
	 */
        fun RailTrail(start: FloatArray, end: FloatArray) {
            var len: Float
            var j: Int
            var p: cparticle_t
            val dec: Float
            val right = FloatArray(3)
            val up = FloatArray(3)
            var i: Int
            var d: Float
            var c: Float
            var s: Float
            val dir = FloatArray(3)
            val clr = 116

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            Math3D.MakeNormalVectors(vec, right, up)

            run {
                i = 0
                while (i < len) {
                    if (free_particles == null)
                        return

                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p

                    p.time = Globals.cl.time
                    Math3D.VectorClear(p.accel)

                    d = i.toFloat() * 0.1.toFloat()
                    c = Math.cos(d) as Float
                    s = Math.sin(d) as Float

                    Math3D.VectorScale(right, c, dir)
                    Math3D.VectorMA(dir, s, up, dir)

                    p.alpha = 1.0.toFloat()
                    p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                    p.color = clr + (Lib.rand() and 7)
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = move[j] + dir[j] * 3
                            p.vel[j] = dir[j] * 6
                            j++
                        }
                    }

                    Math3D.VectorAdd(move, vec, move)
                    i++
                }
            }

            dec = 0.75.toFloat()
            Math3D.VectorScale(vec, dec, vec)
            Math3D.VectorCopy(start, move)

            while (len > 0) {
                len -= dec

                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                Math3D.VectorClear(p.accel)

                p.alpha = 1.0.toFloat()
                p.alphavel = -1.0.toFloat() / (0.6.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = 0 + Lib.rand() and 15

                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = move[j] + Lib.crand() * 3
                        p.vel[j] = Lib.crand() * 3
                        p.accel[j] = 0
                        j++
                    }
                }

                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // move, vec
        /*
	 * =============== CL_IonripperTrail ===============
	 */
        fun IonripperTrail(start: FloatArray, ent: FloatArray) {
            var len: Float
            var j: Int
            val p: cparticle_t
            val dec: Int
            var left = 0

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(ent, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 5
            Math3D.VectorScale(vec, 5, vec)

            while (len > 0) {
                len -= dec.toFloat()

                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time
                p.alpha = 0.5.toFloat()
                p.alphavel = -1.0.toFloat() / (0.3.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = 228 + (Lib.rand() and 3)

                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = move[j]
                        p.accel[j] = 0
                        j++
                    }
                }
                if (left != 0) {
                    left = 0
                    p.vel[0] = 10
                } else {
                    left = 1
                    p.vel[0] = -10
                }

                p.vel[1] = 0
                p.vel[2] = 0

                Math3D.VectorAdd(move, vec, move)
            }
        }

        // stack variable
        // move, vec
        /*
	 * =============== CL_BubbleTrail
	 * 
	 * ===============
	 */
        fun BubbleTrail(start: FloatArray, end: FloatArray) {
            val len: Float
            var i: Int
            var j: Int
            var p: cparticle_t
            val dec: Float

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 32
            Math3D.VectorScale(vec, dec, vec)

            run {
                i = 0
                while (i < len) {
                    if (free_particles == null)
                        return

                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p

                    Math3D.VectorClear(p.accel)
                    p.time = Globals.cl.time

                    p.alpha = 1.0.toFloat()
                    p.alphavel = -1.0.toFloat() / (1.0.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                    p.color = 4 + (Lib.rand() and 7)
                    run {
                        j = 0
                        while (j < 3) {
                            p.org[j] = move[j] + Lib.crand() * 2
                            p.vel[j] = Lib.crand() * 5
                            j++
                        }
                    }
                    p.vel[2] += 6

                    Math3D.VectorAdd(move, vec, move)
                    i += dec.toInt()
                }
            }
        }

        // stack variable
        // forward
        /*
	 * =============== CL_FlyParticles ===============
	 */
        fun FlyParticles(origin: FloatArray, count: Int) {
            var count = count
            var i: Int
            var p: cparticle_t
            var angle: Float
            var sp: Float
            var sy: Float
            var cp: Float
            var cy: Float
            var dist: Float = 64
            val ltime: Float

            if (count > Defines.NUMVERTEXNORMALS)
                count = Defines.NUMVERTEXNORMALS

            if (avelocities[0][0] == 0.0.toFloat()) {
                run {
                    i = 0
                    while (i < Defines.NUMVERTEXNORMALS) {
                        avelocities[i][0] = (Lib.rand() and 255) * 0.01.toFloat()
                        avelocities[i][1] = (Lib.rand() and 255) * 0.01.toFloat()
                        avelocities[i][2] = (Lib.rand() and 255) * 0.01.toFloat()
                        i++
                    }
                }
            }

            ltime = Globals.cl.time / 1000.0.toFloat()
            run {
                i = 0
                while (i < count) {
                    angle = ltime * avelocities[i][0]
                    sy = Math.sin(angle) as Float
                    cy = Math.cos(angle) as Float
                    angle = ltime * avelocities[i][1]
                    sp = Math.sin(angle) as Float
                    cp = Math.cos(angle) as Float
                    angle = ltime * avelocities[i][2]

                    forward[0] = cp * cy
                    forward[1] = cp * sy
                    forward[2] = -sp

                    if (free_particles == null)
                        return
                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p

                    p.time = Globals.cl.time

                    dist = Math.sin(ltime + i.toFloat()) as Float * 64
                    p.org[0] = origin[0] + Globals.bytedirs[i][0] * dist + forward[0] * BEAMLENGTH.toFloat()
                    p.org[1] = origin[1] + Globals.bytedirs[i][1] * dist + forward[1] * BEAMLENGTH.toFloat()
                    p.org[2] = origin[2] + Globals.bytedirs[i][2] * dist + forward[2] * BEAMLENGTH.toFloat()

                    Math3D.VectorClear(p.vel)
                    Math3D.VectorClear(p.accel)

                    p.color = 0
                    //p.colorvel = 0;

                    p.alpha = 1
                    p.alphavel = -100
                    i += 2
                }
            }
        }

        fun FlyEffect(ent: centity_t, origin: FloatArray) {
            var n: Int
            val count: Int
            val starttime: Int

            if (ent.fly_stoptime < Globals.cl.time) {
                starttime = Globals.cl.time
                ent.fly_stoptime = Globals.cl.time + 60000
            } else {
                starttime = ent.fly_stoptime - 60000
            }

            n = Globals.cl.time - starttime
            if (n < 20000)
                count = ((n * 162).toDouble() / 20000.0).toInt()
            else {
                n = ent.fly_stoptime - Globals.cl.time
                if (n < 20000)
                    count = ((n * 162).toDouble() / 20000.0).toInt()
                else
                    count = 162
            }

            FlyParticles(origin, count)
        }

        // stack variable
        private val v = floatArray(0.0, 0.0, 0.0)

        // forward
        /*
	 * =============== CL_BfgParticles ===============
	 */
        //#define BEAMLENGTH 16
        fun BfgParticles(ent: entity_t) {
            var i: Int
            var p: cparticle_t
            var angle: Float
            var sp: Float
            var sy: Float
            var cp: Float
            var cy: Float
            var dist: Float = 64
            val ltime: Float

            if (avelocities[0][0] == 0.0.toFloat()) {
                run {
                    i = 0
                    while (i < Defines.NUMVERTEXNORMALS) {
                        avelocities[i][0] = (Lib.rand() and 255) * 0.01.toFloat()
                        avelocities[i][1] = (Lib.rand() and 255) * 0.01.toFloat()
                        avelocities[i][2] = (Lib.rand() and 255) * 0.01.toFloat()
                        i++
                    }
                }
            }

            ltime = Globals.cl.time / 1000.0.toFloat()
            run {
                i = 0
                while (i < Defines.NUMVERTEXNORMALS) {
                    angle = ltime * avelocities[i][0]
                    sy = Math.sin(angle) as Float
                    cy = Math.cos(angle) as Float
                    angle = ltime * avelocities[i][1]
                    sp = Math.sin(angle) as Float
                    cp = Math.cos(angle) as Float
                    angle = ltime * avelocities[i][2]

                    forward[0] = cp * cy
                    forward[1] = cp * sy
                    forward[2] = -sp

                    if (free_particles == null)
                        return
                    p = free_particles
                    free_particles = p.next
                    p.next = active_particles
                    active_particles = p

                    p.time = Globals.cl.time

                    dist = (Math.sin(ltime + i.toFloat()) * 64) as Float
                    p.org[0] = ent.origin[0] + Globals.bytedirs[i][0] * dist + forward[0] * BEAMLENGTH.toFloat()
                    p.org[1] = ent.origin[1] + Globals.bytedirs[i][1] * dist + forward[1] * BEAMLENGTH.toFloat()
                    p.org[2] = ent.origin[2] + Globals.bytedirs[i][2] * dist + forward[2] * BEAMLENGTH.toFloat()

                    Math3D.VectorClear(p.vel)
                    Math3D.VectorClear(p.accel)

                    Math3D.VectorSubtract(p.org, ent.origin, v)
                    dist = Math3D.VectorLength(v) / 90.0.toFloat()
                    p.color = Math.floor(208 + dist * 7) as Float
                    //p.colorvel = 0;

                    p.alpha = 1.0.toFloat() - dist
                    p.alphavel = -100
                    i++
                }
            }
        }

        // stack variable
        // move, vec
        private val start = floatArray(0.0, 0.0, 0.0)
        private val end = floatArray(0.0, 0.0, 0.0)
        /*
	 * =============== CL_TrapParticles ===============
	 */
        //	   RAFAEL
        fun TrapParticles(ent: entity_t) {
            var len: Float
            var j: Int
            var p: cparticle_t
            val dec: Int

            ent.origin[2] -= 14
            Math3D.VectorCopy(ent.origin, start)
            Math3D.VectorCopy(ent.origin, end)
            end[2] += 64

            Math3D.VectorCopy(start, move)
            Math3D.VectorSubtract(end, start, vec)
            len = Math3D.VectorNormalize(vec)

            dec = 5
            Math3D.VectorScale(vec, 5, vec)

            // FIXME: this is a really silly way to have a loop
            while (len > 0) {
                len -= dec.toFloat()

                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p
                Math3D.VectorClear(p.accel)

                p.time = Globals.cl.time

                p.alpha = 1.0.toFloat()
                p.alphavel = -1.0.toFloat() / (0.3.toFloat() + Globals.rnd.nextFloat() * 0.2.toFloat())
                p.color = 224
                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = move[j] + Lib.crand()
                        p.vel[j] = Lib.crand() * 15
                        p.accel[j] = 0
                        j++
                    }
                }
                p.accel[2] = PARTICLE_GRAVITY

                Math3D.VectorAdd(move, vec, move)
            }

            var i: Int
            var k: Int
            //cparticle_t p;
            var vel: Float
            val dir = FloatArray(3)
            val org = FloatArray(3)

            ent.origin[2] += 14
            Math3D.VectorCopy(ent.origin, org)

            run {
                i = -2
                while (i <= 2) {
                    run {
                        j = -2
                        while (j <= 2) {
                            run {
                                k = -2
                                while (k <= 4) {
                                    if (free_particles == null)
                                        return
                                    p = free_particles
                                    free_particles = p.next
                                    p.next = active_particles
                                    active_particles = p

                                    p.time = Globals.cl.time
                                    p.color = 224 + (Lib.rand() and 3)

                                    p.alpha = 1.0.toFloat()
                                    p.alphavel = -1.0.toFloat() / (0.3.toFloat() + (Lib.rand() and 7) * 0.02.toFloat())

                                    p.org[0] = org[0] + i + ((Lib.rand() and 23) * Lib.crand())
                                    p.org[1] = org[1] + j + ((Lib.rand() and 23) * Lib.crand())
                                    p.org[2] = org[2] + k + ((Lib.rand() and 23) * Lib.crand())

                                    dir[0] = (j * 8).toFloat()
                                    dir[1] = (i * 8).toFloat()
                                    dir[2] = (k * 8).toFloat()

                                    Math3D.VectorNormalize(dir)
                                    vel = 50 + Lib.rand() and 63
                                    Math3D.VectorScale(dir, vel, p.vel)

                                    p.accel[0] = p.accel[1] = 0
                                    p.accel[2] = -PARTICLE_GRAVITY
                                    k += 4
                                }
                            }
                            j += 4
                        }
                    }
                    i += 4
                }
            }

        }

        /*
	 * =============== CL_BFGExplosionParticles ===============
	 */
        //	  FIXME combined with CL_ExplosionParticles
        fun BFGExplosionParticles(org: FloatArray) {
            var j: Int
            val p: cparticle_t

            for (i in 0..256 - 1) {
                if (free_particles == null)
                    return
                p = free_particles
                free_particles = p.next
                p.next = active_particles
                active_particles = p

                p.time = Globals.cl.time
                p.color = 208 + (Lib.rand() and 7)

                run {
                    j = 0
                    while (j < 3) {
                        p.org[j] = org[j] + ((Lib.rand() % 32) - 16)
                        p.vel[j] = (Lib.rand() % 384) - 192
                        j++
                    }
                }

                p.accel[0] = p.accel[1] = 0
                p.accel[2] = -PARTICLE_GRAVITY
                p.alpha = 1.0.toFloat()

                p.alphavel = -0.8.toFloat() / (0.5.toFloat() + Globals.rnd.nextFloat() * 0.3.toFloat())
            }
        }

        // stack variable
        private val dir = floatArray(0.0, 0.0, 0.0)

        /*
	 * =============== CL_TeleportParticles
	 * 
	 * ===============
	 */
        fun TeleportParticles(org: FloatArray) {
            var p: cparticle_t
            var vel: Float

            run {
                var i = -16
                while (i <= 16) {
                    run {
                        var j = -16
                        while (j <= 16) {
                            run {
                                var k = -16
                                while (k <= 32) {
                                    if (free_particles == null)
                                        return
                                    p = free_particles
                                    free_particles = p.next
                                    p.next = active_particles
                                    active_particles = p

                                    p.time = Globals.cl.time
                                    p.color = 7 + (Lib.rand() and 7)

                                    p.alpha = 1.0.toFloat()
                                    p.alphavel = -1.0.toFloat() / (0.3.toFloat() + (Lib.rand() and 7) * 0.02.toFloat())

                                    p.org[0] = org[0] + i + (Lib.rand() and 3)
                                    p.org[1] = org[1] + j + (Lib.rand() and 3)
                                    p.org[2] = org[2] + k + (Lib.rand() and 3)

                                    dir[0] = (j * 8).toFloat()
                                    dir[1] = (i * 8).toFloat()
                                    dir[2] = (k * 8).toFloat()

                                    Math3D.VectorNormalize(dir)
                                    vel = 50 + (Lib.rand() and 63)
                                    Math3D.VectorScale(dir, vel, p.vel)

                                    p.accel[0] = p.accel[1] = 0
                                    p.accel[2] = -PARTICLE_GRAVITY
                                    k += 4
                                }
                            }
                            j += 4
                        }
                    }
                    i += 4
                }
            }
        }

        // stack variable
        private val org = floatArray(0.0, 0.0, 0.0)

        /*
	 * =============== CL_AddParticles ===============
	 */
        fun AddParticles() {
            var p: cparticle_t?
            var next: cparticle_t
            var alpha: Float
            var time = 0.0.toFloat()
            var time2: Float
            var color: Int
            val active: cparticle_t?
            var tail: cparticle_t?

            active = null
            tail = null

            run {
                p = active_particles
                while (p != null) {
                    next = p!!.next

                    // PMM - added INSTANT_PARTICLE handling for heat beam
                    if (p!!.alphavel != INSTANT_PARTICLE) {
                        time = (Globals.cl.time - p!!.time) * 0.001.toFloat()
                        alpha = p!!.alpha + time * p!!.alphavel
                        if (alpha <= 0) {
                            // faded out
                            p!!.next = free_particles
                            free_particles = p
                            continue
                        }
                    } else {
                        alpha = p!!.alpha
                    }

                    p!!.next = null
                    if (tail == null)
                        active = tail = p
                    else {
                        tail!!.next = p
                        tail = p
                    }

                    if (alpha > 1.0)
                        alpha = 1
                    color = p!!.color as Int

                    time2 = time * time

                    org[0] = p!!.org[0] + p!!.vel[0] * time + p!!.accel[0] * time2
                    org[1] = p!!.org[1] + p!!.vel[1] * time + p!!.accel[1] * time2
                    org[2] = p!!.org[2] + p!!.vel[2] * time + p!!.accel[2] * time2

                    V.AddParticle(org, color, alpha)
                    // PMM
                    if (p!!.alphavel == INSTANT_PARTICLE) {
                        p!!.alphavel = 0.0.toFloat()
                        p!!.alpha = 0.0.toFloat()
                    }
                    p = next
                }
            }

            active_particles = active
        }

        /*
	 * ============== CL_EntityEvent
	 * 
	 * An entity has just been parsed that has an event value
	 * 
	 * the female events are there for backwards compatability ==============
	 */
        fun EntityEvent(ent: entity_state_t) {
            when (ent.event) {
                Defines.EV_ITEM_RESPAWN -> {
                    S.StartSound(null, ent.number, Defines.CHAN_WEAPON, S.RegisterSound("items/respawn1.wav"), 1, Defines.ATTN_IDLE, 0)
                    ItemRespawnParticles(ent.origin)
                }
                Defines.EV_PLAYER_TELEPORT -> {
                    S.StartSound(null, ent.number, Defines.CHAN_WEAPON, S.RegisterSound("misc/tele1.wav"), 1, Defines.ATTN_IDLE, 0)
                    TeleportParticles(ent.origin)
                }
                Defines.EV_FOOTSTEP -> if (Globals.cl_footsteps.value != 0.0.toFloat())
                    S.StartSound(null, ent.number, Defines.CHAN_BODY, CL_tent.cl_sfx_footsteps[Lib.rand() and 3], 1, Defines.ATTN_NORM, 0)
                Defines.EV_FALLSHORT -> S.StartSound(null, ent.number, Defines.CHAN_AUTO, S.RegisterSound("player/land1.wav"), 1, Defines.ATTN_NORM, 0)
                Defines.EV_FALL -> S.StartSound(null, ent.number, Defines.CHAN_AUTO, S.RegisterSound("*fall2.wav"), 1, Defines.ATTN_NORM, 0)
                Defines.EV_FALLFAR -> S.StartSound(null, ent.number, Defines.CHAN_AUTO, S.RegisterSound("*fall1.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }

        /*
	 * ============== CL_ClearEffects
	 * 
	 * ==============
	 */
        fun ClearEffects() {
            ClearParticles()
            ClearDlights()
            ClearLightStyles()
        }

        /*
	 * ==============================================================
	 * 
	 * PARTICLE MANAGEMENT
	 * 
	 * ==============================================================
	 */

        val PARTICLE_GRAVITY = 40

        var active_particles: cparticle_t? = null
        var free_particles: cparticle_t? = null

        /*
	 * =============== CL_BigTeleportParticles ===============
	 */
        private val colortable = intArray(2 * 8, 13 * 8, 21 * 8, 18 * 8)

        private val BEAMLENGTH = 16
    }

}