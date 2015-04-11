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

package lwjake2.server

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.GameBase
import lwjake2.game.GameSave
import lwjake2.game.cmodel_t
import lwjake2.game.edict_t
import lwjake2.game.game_import_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Com
import lwjake2.qcommon.MSG
import lwjake2.qcommon.SZ
import lwjake2.util.Math3D

public class SV_GAME {
    companion object {

        /**
         * PF_Unicast

         * Sends the contents of the mutlicast buffer to a single client.
         */
        public fun PF_Unicast(ent: edict_t?, reliable: Boolean) {
            val p: Int
            val client: client_t

            if (ent == null)
                return

            p = ent!!.index
            if (p < 1 || p > SV_MAIN.maxclients.value)
                return

            client = SV_INIT.svs.clients[p - 1]

            if (reliable)
                SZ.Write(client.netchan.message, SV_INIT.sv.multicast.data, SV_INIT.sv.multicast.cursize)
            else
                SZ.Write(client.datagram, SV_INIT.sv.multicast.data, SV_INIT.sv.multicast.cursize)

            SZ.Clear(SV_INIT.sv.multicast)
        }

        /**
         * PF_dprintf

         * Debug print to server console.
         */
        public fun PF_dprintf(fmt: String) {
            Com.Printf(fmt)
        }


        /**
         * Centerprintf for critical messages.
         */
        public fun PF_cprintfhigh(ent: edict_t, fmt: String) {
            PF_cprintf(ent, Defines.PRINT_HIGH, fmt)
        }

        /**
         * PF_cprintf

         * Print to a single client.
         */
        public fun PF_cprintf(ent: edict_t?, level: Int, fmt: String) {

            var n = 0

            if (ent != null) {
                n = ent!!.index
                if (n < 1 || n > SV_MAIN.maxclients.value)
                    Com.Error(Defines.ERR_DROP, "cprintf to a non-client")
            }

            if (ent != null)
                SV_SEND.SV_ClientPrintf(SV_INIT.svs.clients[n - 1], level, fmt)
            else
                Com.Printf(fmt)
        }

        /**
         * PF_centerprintf

         * centerprint to a single client.
         */
        public fun PF_centerprintf(ent: edict_t, fmt: String) {
            val n: Int

            n = ent.index
            if (n < 1 || n > SV_MAIN.maxclients.value)
                return  // Com_Error (ERR_DROP, "centerprintf to a non-client");

            MSG.WriteByte(SV_INIT.sv.multicast, Defines.svc_centerprint)
            MSG.WriteString(SV_INIT.sv.multicast, fmt)
            PF_Unicast(ent, true)
        }

        /**
         * PF_error

         * Abort the server with a game error.
         */
        public fun PF_error(fmt: String) {
            Com.Error(Defines.ERR_DROP, "Game Error: " + fmt)
        }

        public fun PF_error(level: Int, fmt: String) {
            Com.Error(level, fmt)
        }

        /**
         * PF_setmodel

         * Also sets mins and maxs for inline bmodels.
         */
        public fun PF_setmodel(ent: edict_t, name: String?) {
            val i: Int
            val mod: cmodel_t

            if (name == null)
                Com.Error(Defines.ERR_DROP, "PF_setmodel: NULL")

            i = SV_INIT.SV_ModelIndex(name)

            ent.s.modelindex = i

            // if it is an inline model, get the size information for it
            if (name!!.startsWith("*")) {
                mod = CM.InlineModel(name)
                Math3D.VectorCopy(mod.mins, ent.mins)
                Math3D.VectorCopy(mod.maxs, ent.maxs)
                SV_WORLD.SV_LinkEdict(ent)
            }
        }

        /**
         * PF_Configstring
         */
        public fun PF_Configstring(index: Int, `val`: String?) {
            var `val` = `val`
            if (index < 0 || index >= Defines.MAX_CONFIGSTRINGS)
                Com.Error(Defines.ERR_DROP, "configstring: bad index " + index + "\n")

            if (`val` == null)
                `val` = ""

            // change the string in sv
            SV_INIT.sv.configstrings[index] = `val`

            if (SV_INIT.sv.state != Defines.ss_loading) {
                // send the update to
                // everyone
                SZ.Clear(SV_INIT.sv.multicast)
                MSG.WriteChar(SV_INIT.sv.multicast, Defines.svc_configstring)
                MSG.WriteShort(SV_INIT.sv.multicast, index)
                MSG.WriteString(SV_INIT.sv.multicast, `val`)

                SV_SEND.SV_Multicast(Globals.vec3_origin, Defines.MULTICAST_ALL_R)
            }
        }

        public fun PF_WriteChar(c: Int) {
            MSG.WriteChar(SV_INIT.sv.multicast, c)
        }

        public fun PF_WriteByte(c: Int) {
            MSG.WriteByte(SV_INIT.sv.multicast, c)
        }

        public fun PF_WriteShort(c: Int) {
            MSG.WriteShort(SV_INIT.sv.multicast, c)
        }

        public fun PF_WriteLong(c: Int) {
            MSG.WriteLong(SV_INIT.sv.multicast, c)
        }

        public fun PF_WriteFloat(f: Float) {
            MSG.WriteFloat(SV_INIT.sv.multicast, f)
        }

        public fun PF_WriteString(s: String) {
            MSG.WriteString(SV_INIT.sv.multicast, s)
        }

        public fun PF_WritePos(pos: FloatArray) {
            MSG.WritePos(SV_INIT.sv.multicast, pos)
        }

        public fun PF_WriteDir(dir: FloatArray) {
            MSG.WriteDir(SV_INIT.sv.multicast, dir)
        }

        public fun PF_WriteAngle(f: Float) {
            MSG.WriteAngle(SV_INIT.sv.multicast, f)
        }

        /**
         * PF_inPVS

         * Also checks portalareas so that doors block sight.
         */
        public fun PF_inPVS(p1: FloatArray, p2: FloatArray): Boolean {
            var leafnum: Int
            var cluster: Int
            val area1: Int
            val area2: Int
            val mask: ByteArray?

            leafnum = CM.CM_PointLeafnum(p1)
            cluster = CM.CM_LeafCluster(leafnum)
            area1 = CM.CM_LeafArea(leafnum)
            mask = CM.CM_ClusterPVS(cluster)

            leafnum = CM.CM_PointLeafnum(p2)
            cluster = CM.CM_LeafCluster(leafnum)
            area2 = CM.CM_LeafArea(leafnum)

            // quake2 bugfix
            if (cluster == -1)
                return false
            if (mask != null && (0 == (mask[cluster.ushr(3)] and (1 shl (cluster and 7)))))
                return false

            if (!CM.CM_AreasConnected(area1, area2))
                return false // a door blocks sight

            return true
        }

        /**
         * PF_inPHS.

         * Also checks portalareas so that doors block sound.
         */
        public fun PF_inPHS(p1: FloatArray, p2: FloatArray): Boolean {
            var leafnum: Int
            var cluster: Int
            val area1: Int
            val area2: Int
            val mask: ByteArray?

            leafnum = CM.CM_PointLeafnum(p1)
            cluster = CM.CM_LeafCluster(leafnum)
            area1 = CM.CM_LeafArea(leafnum)
            mask = CM.CM_ClusterPHS(cluster)

            leafnum = CM.CM_PointLeafnum(p2)
            cluster = CM.CM_LeafCluster(leafnum)
            area2 = CM.CM_LeafArea(leafnum)

            // quake2 bugfix
            if (cluster == -1)
                return false
            if (mask != null && (0 == (mask[cluster shr 3] and (1 shl (cluster and 7)))))
                return false // more than one bounce away
            if (!CM.CM_AreasConnected(area1, area2))
                return false // a door blocks hearing

            return true
        }

        public fun PF_StartSound(entity: edict_t?, channel: Int, sound_num: Int, volume: Float, attenuation: Float, timeofs: Float) {

            if (null == entity)
                return
            SV_SEND.SV_StartSound(null, entity, channel, sound_num, volume, attenuation, timeofs)

        }


        /**
         * SV_ShutdownGameProgs

         * Called when either the entire server is being killed, or it is changing
         * to a different game directory.
         */
        public fun SV_ShutdownGameProgs() {
            GameBase.ShutdownGame()
        }

        /**
         * SV_InitGameProgs

         * Init the game subsystem for a new map.
         */

        public fun SV_InitGameProgs() {

            // unload anything we have now
            SV_ShutdownGameProgs()

            val gimport = game_import_t()

            // all functions set in game_export_t (rst)
            GameBase.GetGameApi(gimport)

            GameSave.InitGame()
        }
    }
}