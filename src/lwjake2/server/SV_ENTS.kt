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
import lwjake2.game.EndianHandler
import lwjake2.game.GameBase
import lwjake2.game.edict_t
import lwjake2.game.entity_state_t
import lwjake2.game.player_state_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Com
import lwjake2.qcommon.MSG
import lwjake2.qcommon.SZ
import lwjake2.qcommon.sizebuf_t
import lwjake2.util.Math3D

import java.io.IOException

public class SV_ENTS {
    companion object {

        /**
         * =============================================================================

         * Build a client frame structure

         * =============================================================================
         */

        public var fatpvs: ByteArray = ByteArray(65536 / 8) // 32767 is MAX_MAP_LEAFS

        /*
     * =============================================================================
     * 
     * Encode a client frame onto the network channel
     * 
     * =============================================================================
     */

        /**
         * Writes a delta update of an entity_state_t list to the message.
         */
        fun SV_EmitPacketEntities(from: client_frame_t?, to: client_frame_t, msg: sizebuf_t) {
            var oldent: entity_state_t? = null
            var newent: entity_state_t? = null
            var oldindex: Int
            var newindex: Int
            val oldnum: Int
            val newnum: Int
            val from_num_entities: Int
            var bits: Int

            MSG.WriteByte(msg, Defines.svc_packetentities)

            if (from == null)
                from_num_entities = 0
            else
                from_num_entities = from!!.num_entities

            newindex = 0
            oldindex = 0
            while (newindex < to.num_entities || oldindex < from_num_entities) {
                if (newindex >= to.num_entities)
                    newnum = 9999
                else {
                    newent = SV_INIT.svs.client_entities[(to.first_entity + newindex) % SV_INIT.svs.num_client_entities]
                    newnum = newent!!.number
                }

                if (oldindex >= from_num_entities)
                    oldnum = 9999
                else {
                    oldent = SV_INIT.svs.client_entities[(from!!.first_entity + oldindex) % SV_INIT.svs.num_client_entities]
                    oldnum = oldent!!.number
                }

                if (newnum == oldnum) {
                    // delta update from old position
                    // because the force parm is false, this will not result
                    // in any bytes being emited if the entity has not changed at
                    // all note that players are always 'newentities', this updates
                    // their oldorigin always
                    // and prevents warping
                    MSG.WriteDeltaEntity(oldent, newent, msg, false, newent!!.number <= SV_MAIN.maxclients.value)
                    oldindex++
                    newindex++
                    continue
                }

                if (newnum < oldnum) {
                    // this is a new entity, send it from the baseline
                    MSG.WriteDeltaEntity(SV_INIT.sv.baselines[newnum], newent, msg, true, true)
                    newindex++
                    continue
                }

                if (newnum > oldnum) {
                    // the old entity isn't present in the new message
                    bits = Defines.U_REMOVE
                    if (oldnum >= 256)
                        bits = bits or (Defines.U_NUMBER16 or Defines.U_MOREBITS1)

                    MSG.WriteByte(msg, bits and 255)
                    if ((bits and 65280) != 0)
                        MSG.WriteByte(msg, (bits shr 8) and 255)

                    if ((bits and Defines.U_NUMBER16) != 0)
                        MSG.WriteShort(msg, oldnum)
                    else
                        MSG.WriteByte(msg, oldnum)

                    oldindex++
                    continue
                }
            }

            MSG.WriteShort(msg, 0) // end of packetentities

        }

        /**
         * Writes the status of a player to a client system.
         */
        fun SV_WritePlayerstateToClient(from: client_frame_t?, to: client_frame_t, msg: sizebuf_t) {
            var i: Int
            var pflags: Int
            // ptr
            val ps: player_state_t
            val ops: player_state_t
            // mem
            val dummy: player_state_t
            var statbits: Int

            ps = to.ps
            if (from == null) {
                //memset (dummy, 0, sizeof(dummy));
                dummy = player_state_t()
                ops = dummy
            } else
                ops = from!!.ps

            // determine what needs to be sent
            pflags = 0

            if (ps.pmove.pm_type != ops.pmove.pm_type)
                pflags = pflags or Defines.PS_M_TYPE

            if (ps.pmove.origin[0] != ops.pmove.origin[0] || ps.pmove.origin[1] != ops.pmove.origin[1] || ps.pmove.origin[2] != ops.pmove.origin[2])
                pflags = pflags or Defines.PS_M_ORIGIN

            if (ps.pmove.velocity[0] != ops.pmove.velocity[0] || ps.pmove.velocity[1] != ops.pmove.velocity[1] || ps.pmove.velocity[2] != ops.pmove.velocity[2])
                pflags = pflags or Defines.PS_M_VELOCITY

            if (ps.pmove.pm_time != ops.pmove.pm_time)
                pflags = pflags or Defines.PS_M_TIME

            if (ps.pmove.pm_flags != ops.pmove.pm_flags)
                pflags = pflags or Defines.PS_M_FLAGS

            if (ps.pmove.gravity != ops.pmove.gravity)
                pflags = pflags or Defines.PS_M_GRAVITY

            if (ps.pmove.delta_angles[0] != ops.pmove.delta_angles[0] || ps.pmove.delta_angles[1] != ops.pmove.delta_angles[1] || ps.pmove.delta_angles[2] != ops.pmove.delta_angles[2])
                pflags = pflags or Defines.PS_M_DELTA_ANGLES

            if (ps.viewoffset[0] != ops.viewoffset[0] || ps.viewoffset[1] != ops.viewoffset[1] || ps.viewoffset[2] != ops.viewoffset[2])
                pflags = pflags or Defines.PS_VIEWOFFSET

            if (ps.viewangles[0] != ops.viewangles[0] || ps.viewangles[1] != ops.viewangles[1] || ps.viewangles[2] != ops.viewangles[2])
                pflags = pflags or Defines.PS_VIEWANGLES

            if (ps.kick_angles[0] != ops.kick_angles[0] || ps.kick_angles[1] != ops.kick_angles[1] || ps.kick_angles[2] != ops.kick_angles[2])
                pflags = pflags or Defines.PS_KICKANGLES

            if (ps.blend[0] != ops.blend[0] || ps.blend[1] != ops.blend[1] || ps.blend[2] != ops.blend[2] || ps.blend[3] != ops.blend[3])
                pflags = pflags or Defines.PS_BLEND

            if (ps.fov != ops.fov)
                pflags = pflags or Defines.PS_FOV

            if (ps.rdflags != ops.rdflags)
                pflags = pflags or Defines.PS_RDFLAGS

            if (ps.gunframe != ops.gunframe)
                pflags = pflags or Defines.PS_WEAPONFRAME

            pflags = pflags or Defines.PS_WEAPONINDEX

            // write it
            MSG.WriteByte(msg, Defines.svc_playerinfo)
            MSG.WriteShort(msg, pflags)

            // write the pmove_state_t
            if ((pflags and Defines.PS_M_TYPE) != 0)
                MSG.WriteByte(msg, ps.pmove.pm_type)

            if ((pflags and Defines.PS_M_ORIGIN) != 0) {
                MSG.WriteShort(msg, ps.pmove.origin[0])
                MSG.WriteShort(msg, ps.pmove.origin[1])
                MSG.WriteShort(msg, ps.pmove.origin[2])
            }

            if ((pflags and Defines.PS_M_VELOCITY) != 0) {
                MSG.WriteShort(msg, ps.pmove.velocity[0])
                MSG.WriteShort(msg, ps.pmove.velocity[1])
                MSG.WriteShort(msg, ps.pmove.velocity[2])
            }

            if ((pflags and Defines.PS_M_TIME) != 0)
                MSG.WriteByte(msg, ps.pmove.pm_time)

            if ((pflags and Defines.PS_M_FLAGS) != 0)
                MSG.WriteByte(msg, ps.pmove.pm_flags)

            if ((pflags and Defines.PS_M_GRAVITY) != 0)
                MSG.WriteShort(msg, ps.pmove.gravity)

            if ((pflags and Defines.PS_M_DELTA_ANGLES) != 0) {
                MSG.WriteShort(msg, ps.pmove.delta_angles[0])
                MSG.WriteShort(msg, ps.pmove.delta_angles[1])
                MSG.WriteShort(msg, ps.pmove.delta_angles[2])
            }

            // write the rest of the player_state_t
            if ((pflags and Defines.PS_VIEWOFFSET) != 0) {
                MSG.WriteChar(msg, ps.viewoffset[0] * 4)
                MSG.WriteChar(msg, ps.viewoffset[1] * 4)
                MSG.WriteChar(msg, ps.viewoffset[2] * 4)
            }

            if ((pflags and Defines.PS_VIEWANGLES) != 0) {
                MSG.WriteAngle16(msg, ps.viewangles[0])
                MSG.WriteAngle16(msg, ps.viewangles[1])
                MSG.WriteAngle16(msg, ps.viewangles[2])
            }

            if ((pflags and Defines.PS_KICKANGLES) != 0) {
                MSG.WriteChar(msg, ps.kick_angles[0] * 4)
                MSG.WriteChar(msg, ps.kick_angles[1] * 4)
                MSG.WriteChar(msg, ps.kick_angles[2] * 4)
            }

            if ((pflags and Defines.PS_WEAPONINDEX) != 0) {
                MSG.WriteByte(msg, ps.gunindex)
            }

            if ((pflags and Defines.PS_WEAPONFRAME) != 0) {
                MSG.WriteByte(msg, ps.gunframe)
                MSG.WriteChar(msg, ps.gunoffset[0] * 4)
                MSG.WriteChar(msg, ps.gunoffset[1] * 4)
                MSG.WriteChar(msg, ps.gunoffset[2] * 4)
                MSG.WriteChar(msg, ps.gunangles[0] * 4)
                MSG.WriteChar(msg, ps.gunangles[1] * 4)
                MSG.WriteChar(msg, ps.gunangles[2] * 4)
            }

            if ((pflags and Defines.PS_BLEND) != 0) {
                MSG.WriteByte(msg, ps.blend[0] * 255)
                MSG.WriteByte(msg, ps.blend[1] * 255)
                MSG.WriteByte(msg, ps.blend[2] * 255)
                MSG.WriteByte(msg, ps.blend[3] * 255)
            }
            if ((pflags and Defines.PS_FOV) != 0)
                MSG.WriteByte(msg, ps.fov)
            if ((pflags and Defines.PS_RDFLAGS) != 0)
                MSG.WriteByte(msg, ps.rdflags)

            // send stats
            statbits = 0
            run {
                i = 0
                while (i < Defines.MAX_STATS) {
                    if (ps.stats[i] != ops.stats[i])
                        statbits = statbits or (1 shl i)
                    i++
                }
            }
            MSG.WriteLong(msg, statbits)
            run {
                i = 0
                while (i < Defines.MAX_STATS) {
                    if ((statbits and (1 shl i)) != 0)
                        MSG.WriteShort(msg, ps.stats[i])
                    i++
                }
            }
        }

        /**
         * Writes a frame to a client system.
         */
        public fun SV_WriteFrameToClient(client: client_t, msg: sizebuf_t) {
            //ptr
            val frame: client_frame_t
            val oldframe: client_frame_t?
            val lastframe: Int

            //Com.Printf ("%i . %i\n", new
            // Vargs().add(client.lastframe).add(sv.framenum));
            // this is the frame we are creating
            frame = client.frames[SV_INIT.sv.framenum and Defines.UPDATE_MASK]
            if (client.lastframe <= 0) {
                // client is asking for a retransmit
                oldframe = null
                lastframe = -1
            } else if (SV_INIT.sv.framenum - client.lastframe >= (Defines.UPDATE_BACKUP - 3)) {
                // client hasn't gotten a good message through in a long time
                // Com_Printf ("%s: Delta request from out-of-date packet.\n",
                // client.name);
                oldframe = null
                lastframe = -1
            } else {
                // we have a valid message to delta from
                oldframe = client.frames[client.lastframe and Defines.UPDATE_MASK]
                lastframe = client.lastframe
            }

            MSG.WriteByte(msg, Defines.svc_frame)
            MSG.WriteLong(msg, SV_INIT.sv.framenum)
            MSG.WriteLong(msg, lastframe) // what we are delta'ing from
            MSG.WriteByte(msg, client.surpressCount) // rate dropped packets
            client.surpressCount = 0

            // send over the areabits
            MSG.WriteByte(msg, frame.areabytes)
            SZ.Write(msg, frame.areabits, frame.areabytes)

            // delta encode the playerstate
            SV_WritePlayerstateToClient(oldframe, frame, msg)

            // delta encode the entities
            SV_EmitPacketEntities(oldframe, frame, msg)
        }

        /**
         * The client will interpolate the view position, so we can't use a single
         * PVS point.
         */
        public fun SV_FatPVS(org: FloatArray) {
            val leafs = IntArray(64)
            var i: Int
            var j: Int
            val count: Int
            val longs: Int
            var src: ByteArray
            val mins = floatArray(0.0, 0.0, 0.0)
            val maxs = floatArray(0.0, 0.0, 0.0)

            run {
                i = 0
                while (i < 3) {
                    mins[i] = org[i] - 8
                    maxs[i] = org[i] + 8
                    i++
                }
            }

            count = CM.CM_BoxLeafnums(mins, maxs, leafs, 64, null)

            if (count < 1)
                Com.Error(Defines.ERR_FATAL, "SV_FatPVS: count < 1")

            longs = (CM.CM_NumClusters() + 31) shr 5

            // convert leafs to clusters
            run {
                i = 0
                while (i < count) {
                    leafs[i] = CM.CM_LeafCluster(leafs[i])
                    i++
                }
            }

            System.arraycopy(CM.CM_ClusterPVS(leafs[0]), 0, SV_ENTS.fatpvs, 0, longs shl 2)
            // or in all the other leaf bits
            run {
                i = 1
                while (i < count) {
                    run {
                        j = 0
                        while (j < i) {
                            if (leafs[i] == leafs[j])
                                break
                            j++
                        }
                    }
                    if (j != i)
                        continue // already have the cluster we want

                    src = CM.CM_ClusterPVS(leafs[i])

                    //for (j=0 ; j<longs ; j++)
                    //	((long *)fatpvs)[j] |= ((long *)src)[j];
                    var k = 0
                    run {
                        j = 0
                        while (j < longs) {
                            SV_ENTS.fatpvs[k] = SV_ENTS.fatpvs[k] or src[k++]
                            SV_ENTS.fatpvs[k] = SV_ENTS.fatpvs[k] or src[k++]
                            SV_ENTS.fatpvs[k] = SV_ENTS.fatpvs[k] or src[k++]
                            SV_ENTS.fatpvs[k] = SV_ENTS.fatpvs[k] or src[k++]
                            j++
                        }
                    }
                    i++
                }
            }
        }

        /**
         * Decides which entities are going to be visible to the client, and copies
         * off the playerstat and areabits.
         */
        public fun SV_BuildClientFrame(client: client_t) {
            var e: Int
            var i: Int
            val org = floatArray(0.0, 0.0, 0.0)
            var ent: edict_t
            val clent: edict_t
            val frame: client_frame_t
            var state: entity_state_t
            var l: Int
            val clientarea: Int
            val clientcluster: Int
            val leafnum: Int
            val clientphs: ByteArray
            var bitvector: ByteArray

            clent = client.edict
            if (clent.client == null)
                return  // not in game yet

            // this is the frame we are creating
            frame = client.frames[SV_INIT.sv.framenum and Defines.UPDATE_MASK]

            frame.senttime = SV_INIT.svs.realtime // save it for ping calc later

            // find the client's PVS
            run {
                i = 0
                while (i < 3) {
                    org[i] = clent.client.ps.pmove.origin[i] * 0.125.toFloat() + clent.client.ps.viewoffset[i]
                    i++
                }
            }

            leafnum = CM.CM_PointLeafnum(org)
            clientarea = CM.CM_LeafArea(leafnum)
            clientcluster = CM.CM_LeafCluster(leafnum)

            // calculate the visible areas
            frame.areabytes = CM.CM_WriteAreaBits(frame.areabits, clientarea)

            // grab the current player_state_t
            frame.ps.set(clent.client.ps)

            SV_FatPVS(org)
            clientphs = CM.CM_ClusterPHS(clientcluster)

            // build up the list of visible entities
            frame.num_entities = 0
            frame.first_entity = SV_INIT.svs.next_client_entities

            run {
                e = 1
                while (e < GameBase.num_edicts) {
                    ent = GameBase.g_edicts[e]

                    // ignore ents without visible models
                    if ((ent.svflags and Defines.SVF_NOCLIENT) != 0)
                        continue

                    // ignore ents without visible models unless they have an effect
                    if (0 == ent.s.modelindex && 0 == ent.s.effects && 0 == ent.s.sound && 0 == ent.s.event)
                        continue

                    // ignore if not touching a PV leaf
                    // check area
                    if (ent != clent) {
                        if (!CM.CM_AreasConnected(clientarea, ent.areanum)) {
                            // doors can legally straddle two areas, so we may need to check another one
                            if (0 == ent.areanum2 || !CM.CM_AreasConnected(clientarea, ent.areanum2))
                                continue // blocked by a door
                        }

                        // beams just check one point for PHS
                        if ((ent.s.renderfx and Defines.RF_BEAM) != 0) {
                            l = ent.clusternums[0]
                            if (0 == (clientphs[l shr 3] and (1 shl (l and 7))))
                                continue
                        } else {
                            // FIXME: if an ent has a model and a sound, but isn't
                            // in the PVS, only the PHS, clear the model
                            if (ent.s.sound == 0) {
                                bitvector = SV_ENTS.fatpvs //clientphs;
                            } else
                                bitvector = SV_ENTS.fatpvs

                            if (ent.num_clusters == -1) {
                                // too many leafs for
                                // individual check, go by
                                // headnode
                                if (!CM.CM_HeadnodeVisible(ent.headnode, bitvector))
                                    continue
                            } else {
                                // check individual leafs
                                run {
                                    i = 0
                                    while (i < ent.num_clusters) {
                                        l = ent.clusternums[i]
                                        if ((bitvector[l shr 3] and (1 shl (l and 7))) != 0)
                                            break
                                        i++
                                    }
                                }
                                if (i == ent.num_clusters)
                                    continue // not visible
                            }

                            if (ent.s.modelindex == 0) {
                                // don't send sounds if they
                                // will be attenuated away
                                val delta = floatArray(0.0, 0.0, 0.0)
                                val len: Float

                                Math3D.VectorSubtract(org, ent.s.origin, delta)
                                len = Math3D.VectorLength(delta)
                                if (len > 400)
                                    continue
                            }
                        }
                    }

                    // add it to the circular client_entities array
                    val ix = SV_INIT.svs.next_client_entities % SV_INIT.svs.num_client_entities
                    state = SV_INIT.svs.client_entities[ix]
                    if (ent.s.number != e) {
                        Com.DPrintf("FIXING ENT.S.NUMBER!!!\n")
                        ent.s.number = e
                    }

                    //*state = ent.s;
                    SV_INIT.svs.client_entities[ix].set(ent.s)

                    // don't mark players missiles as solid
                    if (ent.owner == client.edict)
                        state.solid = 0

                    SV_INIT.svs.next_client_entities++
                    frame.num_entities++
                    e++
                }
            }
        }

        /**
         * Save everything in the world out without deltas. Used for recording
         * footage for merged or assembled demos.
         */
        public fun SV_RecordDemoMessage() {
            var e: Int
            var ent: edict_t
            val nostate = entity_state_t(null)
            val buf = sizebuf_t()
            val buf_data = ByteArray(32768)
            val len: Int

            if (SV_INIT.svs.demofile == null)
                return

            //memset (nostate, 0, sizeof(nostate));
            SZ.Init(buf, buf_data, buf_data.size())

            // write a frame message that doesn't contain a player_state_t
            MSG.WriteByte(buf, Defines.svc_frame)
            MSG.WriteLong(buf, SV_INIT.sv.framenum)

            MSG.WriteByte(buf, Defines.svc_packetentities)

            e = 1
            ent = GameBase.g_edicts[e]

            while (e < GameBase.num_edicts) {
                // ignore ents without visible models unless they have an effect
                if (ent.inuse && ent.s.number != 0 && (ent.s.modelindex != 0 || ent.s.effects != 0 || ent.s.sound != 0 || ent.s.event != 0) && 0 == (ent.svflags and Defines.SVF_NOCLIENT))
                    MSG.WriteDeltaEntity(nostate, ent.s, buf, false, true)

                e++
                ent = GameBase.g_edicts[e]
            }

            MSG.WriteShort(buf, 0) // end of packetentities

            // now add the accumulated multicast information
            SZ.Write(buf, SV_INIT.svs.demo_multicast.data, SV_INIT.svs.demo_multicast.cursize)
            SZ.Clear(SV_INIT.svs.demo_multicast)

            // now write the entire message to the file, prefixed by the length
            len = EndianHandler.swapInt(buf.cursize)

            try {
                //fwrite (len, 4, 1, svs.demofile);
                SV_INIT.svs.demofile.writeInt(len)
                //fwrite (buf.data, buf.cursize, 1, svs.demofile);
                SV_INIT.svs.demofile.write(buf.data, 0, buf.cursize)
            } catch (e1: IOException) {
                Com.Printf("Error writing demo file:" + e)
            }

        }
    }
}