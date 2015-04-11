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
import lwjake2.game.cmodel_t
import lwjake2.game.edict_t
import lwjake2.game.link_t
import lwjake2.game.trace_t
import lwjake2.qcommon.CM
import lwjake2.qcommon.Com
import lwjake2.util.Math3D

public class SV_WORLD {
    companion object {
        // world.c -- world query functions
        //
        //
        //===============================================================================
        //
        //ENTITY AREA CHECKING
        //
        //FIXME: this use of "area" is different from the bsp file use
        //===============================================================================
        public var sv_areanodes: Array<areanode_t> = arrayOfNulls<areanode_t>(Defines.AREA_NODES)
        {
            SV_WORLD.initNodes()
        }

        public var sv_numareanodes: Int = 0

        public var area_mins: FloatArray
        public var area_maxs: FloatArray

        public var area_list: Array<edict_t>

        public var area_count: Int = 0
        public var area_maxcount: Int = 0

        public var area_type: Int = 0

        public val MAX_TOTAL_ENT_LEAFS: Int = 128

        var leafs = IntArray(MAX_TOTAL_ENT_LEAFS)

        var clusters = IntArray(MAX_TOTAL_ENT_LEAFS)

        //===========================================================================
        var touch = arrayOfNulls<edict_t>(Defines.MAX_EDICTS)

        //===========================================================================
        var touchlist = arrayOfNulls<edict_t>(Defines.MAX_EDICTS)

        public fun initNodes() {
            for (n in 0..Defines.AREA_NODES - 1)
                SV_WORLD.sv_areanodes[n] = areanode_t()
        }

        // ClearLink is used for new headnodes
        public fun ClearLink(l: link_t) {
            l.prev = l.next = l
        }

        public fun RemoveLink(l: link_t) {
            l.next.prev = l.prev
            l.prev.next = l.next
        }

        public fun InsertLinkBefore(l: link_t, before: link_t) {
            l.next = before
            l.prev = before.prev
            l.prev.next = l
            l.next.prev = l
        }

        /*
     * =============== SV_CreateAreaNode
     * 
     * Builds a uniformly subdivided tree for the given world size
     * ===============
     */
        public fun SV_CreateAreaNode(depth: Int, mins: FloatArray, maxs: FloatArray): areanode_t {
            val anode: areanode_t
            val size = floatArray(0.0, 0.0, 0.0)
            val mins1 = floatArray(0.0, 0.0, 0.0)
            val maxs1 = floatArray(0.0, 0.0, 0.0)
            val mins2 = floatArray(0.0, 0.0, 0.0)
            val maxs2 = floatArray(0.0, 0.0, 0.0)
            anode = SV_WORLD.sv_areanodes[SV_WORLD.sv_numareanodes]
            // just for debugging (rst)
            Math3D.VectorCopy(mins, anode.mins_rst)
            Math3D.VectorCopy(maxs, anode.maxs_rst)
            SV_WORLD.sv_numareanodes++
            ClearLink(anode.trigger_edicts)
            ClearLink(anode.solid_edicts)
            if (depth == Defines.AREA_DEPTH) {
                anode.axis = -1
                anode.children[0] = anode.children[1] = null
                return anode
            }
            Math3D.VectorSubtract(maxs, mins, size)
            if (size[0] > size[1])
                anode.axis = 0
            else
                anode.axis = 1
            anode.dist = 0.5.toFloat() * (maxs[anode.axis] + mins[anode.axis])
            Math3D.VectorCopy(mins, mins1)
            Math3D.VectorCopy(mins, mins2)
            Math3D.VectorCopy(maxs, maxs1)
            Math3D.VectorCopy(maxs, maxs2)
            maxs1[anode.axis] = mins2[anode.axis] = anode.dist
            anode.children[0] = SV_CreateAreaNode(depth + 1, mins2, maxs2)
            anode.children[1] = SV_CreateAreaNode(depth + 1, mins1, maxs1)
            return anode
        }

        /*
     * =============== SV_ClearWorld
     * 
     * ===============
     */
        public fun SV_ClearWorld() {
            initNodes()
            SV_WORLD.sv_numareanodes = 0
            SV_CreateAreaNode(0, SV_INIT.sv.models[1].mins, SV_INIT.sv.models[1].maxs)
            /*
         * Com.p("areanodes:" + sv_numareanodes + " (sollten 32 sein)."); for
         * (int n = 0; n < sv_numareanodes; n++) { Com.Printf( "|%3i|%2i|%8.2f
         * |%8.2f|%8.2f|%8.2f| %8.2f|%8.2f|%8.2f|\n", new Vargs() .add(n)
         * .add(sv_areanodes[n].axis) .add(sv_areanodes[n].dist)
         * .add(sv_areanodes[n].mins_rst[0]) .add(sv_areanodes[n].mins_rst[1])
         * .add(sv_areanodes[n].mins_rst[2]) .add(sv_areanodes[n].maxs_rst[0])
         * .add(sv_areanodes[n].maxs_rst[1]) .add(sv_areanodes[n].maxs_rst[2])); }
         */
        }

        /*
     * =============== SV_UnlinkEdict ===============
     */
        public fun SV_UnlinkEdict(ent: edict_t) {
            if (null == ent.area.prev)
                return  // not linked in anywhere
            RemoveLink(ent.area)
            ent.area.prev = ent.area.next = null
        }

        public fun SV_LinkEdict(ent: edict_t) {
            var node: areanode_t
            val num_leafs: Int
            var j: Int
            var k: Int
            val area: Int
            var topnode = 0
            if (ent.area.prev != null)
                SV_UnlinkEdict(ent) // unlink from old position
            if (ent == GameBase.g_edicts[0])
                return  // don't add the world
            if (!ent.inuse)
                return
            // set the size
            Math3D.VectorSubtract(ent.maxs, ent.mins, ent.size)
            // encode the size into the entity_state for client prediction
            if (ent.solid == Defines.SOLID_BBOX && 0 == (ent.svflags and Defines.SVF_DEADMONSTER)) {
                // assume that x/y are equal and symetric
                var i = (ent.maxs[0] / 8) as Int
                if (i < 1)
                    i = 1
                if (i > 31)
                    i = 31
                // z is not symetric
                j = ((-ent.mins[2]) / 8) as Int
                if (j < 1)
                    j = 1
                if (j > 31)
                    j = 31
                // and z maxs can be negative...
                k = ((ent.maxs[2] + 32) / 8) as Int
                if (k < 1)
                    k = 1
                if (k > 63)
                    k = 63
                ent.s.solid = (k shl 10) or (j shl 5) or i
            } else if (ent.solid == Defines.SOLID_BSP) {
                ent.s.solid = 31 // a solid_bbox will never create this value
            } else
                ent.s.solid = 0
            // set the abs box
            if (ent.solid == Defines.SOLID_BSP && (ent.s.angles[0] != 0 || ent.s.angles[1] != 0 || ent.s.angles[2] != 0)) {
                // expand for rotation
                var max: Float
                var v: Float
                max = 0
                for (i in 0..3 - 1) {
                    v = Math.abs(ent.mins[i])
                    if (v > max)
                        max = v
                    v = Math.abs(ent.maxs[i])
                    if (v > max)
                        max = v
                }
                for (i in 0..3 - 1) {
                    ent.absmin[i] = ent.s.origin[i] - max
                    ent.absmax[i] = ent.s.origin[i] + max
                }
            } else {
                // normal
                Math3D.VectorAdd(ent.s.origin, ent.mins, ent.absmin)
                Math3D.VectorAdd(ent.s.origin, ent.maxs, ent.absmax)
            }
            // because movement is clipped an epsilon away from an actual edge,
            // we must fully check even when bounding boxes don't quite touch
            ent.absmin[0]--
            ent.absmin[1]--
            ent.absmin[2]--
            ent.absmax[0]++
            ent.absmax[1]++
            ent.absmax[2]++
            // link to PVS leafs
            ent.num_clusters = 0
            ent.areanum = 0
            ent.areanum2 = 0
            // get all leafs, including solids
            val iw = intArray(topnode)
            num_leafs = CM.CM_BoxLeafnums(ent.absmin, ent.absmax, SV_WORLD.leafs, SV_WORLD.MAX_TOTAL_ENT_LEAFS, iw)
            topnode = iw[0]
            // set areas
            for (i in 0..num_leafs - 1) {
                SV_WORLD.clusters[i] = CM.CM_LeafCluster(SV_WORLD.leafs[i])
                area = CM.CM_LeafArea(SV_WORLD.leafs[i])
                if (area != 0) {
                    // doors may legally straggle two areas,
                    // but nothing should evern need more than that
                    if (ent.areanum != 0 && ent.areanum != area) {
                        if (ent.areanum2 != 0 && ent.areanum2 != area && SV_INIT.sv.state == Defines.ss_loading)
                            Com.DPrintf("Object touching 3 areas at " + ent.absmin[0] + " " + ent.absmin[1] + " " + ent.absmin[2] + "\n")
                        ent.areanum2 = area
                    } else
                        ent.areanum = area
                }
            }
            if (num_leafs >= SV_WORLD.MAX_TOTAL_ENT_LEAFS) {
                // assume we missed some leafs, and mark by headnode
                ent.num_clusters = -1
                ent.headnode = topnode
            } else {
                ent.num_clusters = 0
                for (i in 0..num_leafs - 1) {
                    if (SV_WORLD.clusters[i] == -1)
                        continue // not a visible leaf
                    run {
                        j = 0
                        while (j < i) {
                            if (SV_WORLD.clusters[j] == SV_WORLD.clusters[i])
                                break
                            j++
                        }
                    }
                    if (j == i) {
                        if (ent.num_clusters == Defines.MAX_ENT_CLUSTERS) {
                            // assume we missed some leafs, and mark by headnode
                            ent.num_clusters = -1
                            ent.headnode = topnode
                            break
                        }
                        ent.clusternums[ent.num_clusters++] = SV_WORLD.clusters[i]
                    }
                }
            }
            // if first time, make sure old_origin is valid
            if (0 == ent.linkcount) {
                Math3D.VectorCopy(ent.s.origin, ent.s.old_origin)
            }
            ent.linkcount++
            if (ent.solid == Defines.SOLID_NOT)
                return
            // find the first node that the ent's box crosses
            node = SV_WORLD.sv_areanodes[0]
            while (true) {
                if (node.axis == -1)
                    break
                if (ent.absmin[node.axis] > node.dist)
                    node = node.children[0]
                else if (ent.absmax[node.axis] < node.dist)
                    node = node.children[1]
                else
                    break // crosses the node
            }
            // link it in
            if (ent.solid == Defines.SOLID_TRIGGER)
                InsertLinkBefore(ent.area, node.trigger_edicts)
            else
                InsertLinkBefore(ent.area, node.solid_edicts)
        }

        /*
     * ==================== SV_AreaEdicts_r
     * 
     * ====================
     */
        public fun SV_AreaEdicts_r(node: areanode_t) {
            var l: link_t
            var next: link_t
            val start: link_t
            var check: edict_t
            // touch linked edicts
            if (SV_WORLD.area_type == Defines.AREA_SOLID)
                start = node.solid_edicts
            else
                start = node.trigger_edicts
            run {
                l = start.next
                while (l != start) {
                    next = l.next
                    check = l.o as edict_t
                    if (check.solid == Defines.SOLID_NOT)
                        continue // deactivated
                    if (check.absmin[0] > SV_WORLD.area_maxs[0] || check.absmin[1] > SV_WORLD.area_maxs[1] || check.absmin[2] > SV_WORLD.area_maxs[2] || check.absmax[0] < SV_WORLD.area_mins[0] || check.absmax[1] < SV_WORLD.area_mins[1] || check.absmax[2] < SV_WORLD.area_mins[2])
                        continue // not touching
                    if (SV_WORLD.area_count == SV_WORLD.area_maxcount) {
                        Com.Printf("SV_AreaEdicts: MAXCOUNT\n")
                        return
                    }
                    SV_WORLD.area_list[SV_WORLD.area_count] = check
                    SV_WORLD.area_count++
                    l = next
                }
            }
            if (node.axis == -1)
                return  // terminal node
            // recurse down both sides
            if (SV_WORLD.area_maxs[node.axis] > node.dist)
                SV_AreaEdicts_r(node.children[0])
            if (SV_WORLD.area_mins[node.axis] < node.dist)
                SV_AreaEdicts_r(node.children[1])
        }

        /*
     * ================ SV_AreaEdicts ================
     */
        public fun SV_AreaEdicts(mins: FloatArray, maxs: FloatArray, list: Array<edict_t>, maxcount: Int, areatype: Int): Int {
            SV_WORLD.area_mins = mins
            SV_WORLD.area_maxs = maxs
            SV_WORLD.area_list = list
            SV_WORLD.area_count = 0
            SV_WORLD.area_maxcount = maxcount
            SV_WORLD.area_type = areatype
            SV_AreaEdicts_r(SV_WORLD.sv_areanodes[0])
            return SV_WORLD.area_count
        }

        /*
     * ============= SV_PointContents =============
     */
        public fun SV_PointContents(p: FloatArray): Int {
            var hit: edict_t
            var i: Int
            val num: Int
            var contents: Int
            var c2: Int
            var headnode: Int
            // get base contents from world
            contents = CM.PointContents(p, SV_INIT.sv.models[1].headnode)
            // or in contents from all the other entities
            num = SV_AreaEdicts(p, p, SV_WORLD.touch, Defines.MAX_EDICTS, Defines.AREA_SOLID)
            run {
                i = 0
                while (i < num) {
                    hit = SV_WORLD.touch[i]
                    // might intersect, so do an exact clip
                    headnode = SV_HullForEntity(hit)
                    c2 = CM.TransformedPointContents(p, headnode, hit.s.origin, hit.s.angles)
                    contents = contents or c2
                    i++
                }
            }
            return contents
        }

        /*
     * ================ SV_HullForEntity
     * 
     * Returns a headnode that can be used for testing or clipping an object of
     * mins/maxs size. Offset is filled in to contain the adjustment that must
     * be added to the testing object's origin to get a point to use with the
     * returned hull. ================
     */
        public fun SV_HullForEntity(ent: edict_t): Int {
            val model: cmodel_t?
            // decide which clipping hull to use, based on the size
            if (ent.solid == Defines.SOLID_BSP) {
                // explicit hulls in the BSP model
                model = SV_INIT.sv.models[ent.s.modelindex]
                if (null == model)
                    Com.Error(Defines.ERR_FATAL, "MOVETYPE_PUSH with a non bsp model")
                return model!!.headnode
            }
            // create a temp hull from bounding box sizes
            return CM.HeadnodeForBox(ent.mins, ent.maxs)
        }

        public fun SV_ClipMoveToEntities(clip: moveclip_t) {
            var i: Int
            val num: Int
            var touch: edict_t
            var trace: trace_t
            var headnode: Int
            var angles: FloatArray
            num = SV_AreaEdicts(clip.boxmins, clip.boxmaxs, SV_WORLD.touchlist, Defines.MAX_EDICTS, Defines.AREA_SOLID)
            // be careful, it is possible to have an entity in this
            // list removed before we get to it (killtriggered)
            run {
                i = 0
                while (i < num) {
                    touch = SV_WORLD.touchlist[i]
                    if (touch.solid == Defines.SOLID_NOT)
                        continue
                    if (touch == clip.passedict)
                        continue
                    if (clip.trace.allsolid)
                        return
                    if (clip.passedict != null) {
                        if (touch.owner == clip.passedict)
                            continue // don't clip against own missiles
                        if (clip.passedict.owner == touch)
                            continue // don't clip against owner
                    }
                    if (0 == (clip.contentmask and Defines.CONTENTS_DEADMONSTER) && 0 != (touch.svflags and Defines.SVF_DEADMONSTER))
                        continue
                    // might intersect, so do an exact clip
                    headnode = SV_HullForEntity(touch)
                    angles = touch.s.angles
                    if (touch.solid != Defines.SOLID_BSP)
                        angles = Globals.vec3_origin // boxes don't rotate
                    if ((touch.svflags and Defines.SVF_MONSTER) != 0)
                        trace = CM.TransformedBoxTrace(clip.start, clip.end, clip.mins2, clip.maxs2, headnode, clip.contentmask, touch.s.origin, angles)
                    else
                        trace = CM.TransformedBoxTrace(clip.start, clip.end, clip.mins, clip.maxs, headnode, clip.contentmask, touch.s.origin, angles)
                    if (trace.allsolid || trace.startsolid || trace.fraction < clip.trace.fraction) {
                        trace.ent = touch
                        if (clip.trace.startsolid) {
                            clip.trace = trace
                            clip.trace.startsolid = true
                        } else
                            clip.trace.set(trace)
                    } else if (trace.startsolid)
                        clip.trace.startsolid = true
                    i++
                }
            }
        }

        /*
     * ================== SV_TraceBounds ==================
     */
        public fun SV_TraceBounds(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray, boxmins: FloatArray, boxmaxs: FloatArray) {
            var i: Int
            run {
                i = 0
                while (i < 3) {
                    if (end[i] > start[i]) {
                        boxmins[i] = start[i] + mins[i] - 1
                        boxmaxs[i] = end[i] + maxs[i] + 1
                    } else {
                        boxmins[i] = end[i] + mins[i] - 1
                        boxmaxs[i] = start[i] + maxs[i] + 1
                    }
                    i++
                }
            }
        }

        /*
     * ================== SV_Trace
     * 
     * Moves the given mins/maxs volume through the world from start to end.
     * 
     * Passedict and edicts owned by passedict are explicitly not checked.
     * 
     * ==================
     */
        public fun SV_Trace(start: FloatArray, mins: FloatArray?, maxs: FloatArray?, end: FloatArray, passedict: edict_t, contentmask: Int): trace_t {
            var mins = mins
            var maxs = maxs
            val clip = moveclip_t()
            if (mins == null)
                mins = Globals.vec3_origin
            if (maxs == null)
                maxs = Globals.vec3_origin

            // clip to world
            clip.trace = CM.BoxTrace(start, end, mins, maxs, 0, contentmask)
            clip.trace.ent = GameBase.g_edicts[0]
            if (clip.trace.fraction == 0)
                return clip.trace // blocked by the world
            clip.contentmask = contentmask
            clip.start = start
            clip.end = end
            clip.mins = mins
            clip.maxs = maxs
            clip.passedict = passedict
            Math3D.VectorCopy(mins, clip.mins2)
            Math3D.VectorCopy(maxs, clip.maxs2)
            // create the bounding box of the entire move
            SV_TraceBounds(start, clip.mins2, clip.maxs2, end, clip.boxmins, clip.boxmaxs)
            // clip to other solid entities
            SV_ClipMoveToEntities(clip)
            return clip.trace
        }
    }
}