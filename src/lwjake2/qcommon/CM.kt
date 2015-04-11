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

package lwjake2.qcommon

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.cmodel_t
import lwjake2.game.cplane_t
import lwjake2.game.cvar_t
import lwjake2.game.mapsurface_t
import lwjake2.game.trace_t
import lwjake2.util.Lib
import lwjake2.util.Math3D
import lwjake2.util.Vargs
import lwjake2.util.Vec3Cache

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.Arrays

public class CM {

    public class cnode_t {
        var plane: cplane_t // ptr

        var children = intArray(0, 0) // negative numbers are leafs
    }

    public class cbrushside_t {
        var plane: cplane_t // ptr

        var surface: mapsurface_t // ptr
    }

    public class cleaf_t {
        var contents: Int = 0

        var cluster: Int = 0

        var area: Int = 0

        // was unsigned short, but is ok (rst)
        var firstleafbrush: Short = 0

        // was unsigned short, but is ok (rst)
        var numleafbrushes: Short = 0
    }

    public class cbrush_t {
        var contents: Int = 0

        var numsides: Int = 0

        var firstbrushside: Int = 0

        var checkcount: Int = 0 // to avoid repeated testings
    }

    public class carea_t {
        var numareaportals: Int = 0

        var firstareaportal: Int = 0

        var floodnum: Int = 0 // if two areas have equal floodnums, they are connected

        var floodvalid: Int = 0
    }

    companion object {

        var checkcount: Int = 0

        var map_name = ""

        var numbrushsides: Int = 0

        var map_brushsides = arrayOfNulls<cbrushside_t>(Defines.MAX_MAP_BRUSHSIDES)
        {
            for (n in 0..Defines.MAX_MAP_BRUSHSIDES - 1)
                map_brushsides[n] = cbrushside_t()
        }

        public var numtexinfo: Int = 0

        public var map_surfaces: Array<mapsurface_t> = arrayOfNulls<mapsurface_t>(Defines.MAX_MAP_TEXINFO)
        {
            for (n in 0..Defines.MAX_MAP_TEXINFO - 1)
                map_surfaces[n] = mapsurface_t()
        }

        var numplanes: Int = 0

        /** Extra for box hull ( +6)  */
        var map_planes = arrayOfNulls<cplane_t>(Defines.MAX_MAP_PLANES + 6)

        {
            for (n in 0..Defines.MAX_MAP_PLANES + 6 - 1)
                map_planes[n] = cplane_t()
        }

        var numnodes: Int = 0

        /** Extra for box hull ( +6)  */
        var map_nodes = arrayOfNulls<cnode_t>(Defines.MAX_MAP_NODES + 6)

        {
            for (n in 0..Defines.MAX_MAP_NODES + 6 - 1)
                map_nodes[n] = cnode_t()
        }

        var numleafs = 1 // allow leaf funcs to be called without a map

        var map_leafs = arrayOfNulls<cleaf_t>(Defines.MAX_MAP_LEAFS)
        {
            for (n in 0..Defines.MAX_MAP_LEAFS - 1)
                map_leafs[n] = cleaf_t()
        }

        var emptyleaf: Int = 0
        var solidleaf: Int = 0

        var numleafbrushes: Int = 0

        public var map_leafbrushes: IntArray = IntArray(Defines.MAX_MAP_LEAFBRUSHES)

        public var numcmodels: Int = 0

        public var map_cmodels: Array<cmodel_t> = arrayOfNulls<cmodel_t>(Defines.MAX_MAP_MODELS)
        {
            for (n in 0..Defines.MAX_MAP_MODELS - 1)
                map_cmodels[n] = cmodel_t()

        }

        public var numbrushes: Int = 0

        public var map_brushes: Array<cbrush_t> = arrayOfNulls(Defines.MAX_MAP_BRUSHES)
        {
            for (n in 0..Defines.MAX_MAP_BRUSHES - 1)
                map_brushes[n] = cbrush_t()

        }

        public var numvisibility: Int = 0

        public var map_visibility: ByteArray = ByteArray(Defines.MAX_MAP_VISIBILITY)

        /** Main visibility data.  */
        public var map_vis: qfiles.dvis_t = qfiles.dvis_t(ByteBuffer.wrap(map_visibility))

        public var numentitychars: Int = 0

        public var map_entitystring: String

        public var numareas: Int = 1

        public var map_areas: Array<carea_t> = arrayOfNulls(Defines.MAX_MAP_AREAS)
        {
            for (n in 0..Defines.MAX_MAP_AREAS - 1)
                map_areas[n] = carea_t()

        }

        public var numareaportals: Int = 0

        public var map_areaportals: Array<qfiles.dareaportal_t> = arrayOfNulls<qfiles.dareaportal_t>(Defines.MAX_MAP_AREAPORTALS)

        {
            for (n in 0..Defines.MAX_MAP_AREAPORTALS - 1)
                map_areaportals[n] = qfiles.dareaportal_t()

        }

        public var numclusters: Int = 1

        public var nullsurface: mapsurface_t = mapsurface_t()

        public var floodvalid: Int = 0

        public var portalopen: BooleanArray = BooleanArray(Defines.MAX_MAP_AREAPORTALS)

        public var map_noareas: cvar_t

        public var cmod_base: ByteArray

        public var checksum: Int = 0

        public var last_checksum: Int = 0

        /**
         * Loads in the map and all submodels.
         */
        public fun CM_LoadMap(name: String?, clientload: Boolean, checksum: IntArray): cmodel_t {
            Com.DPrintf("CM_LoadMap(" + name + ")...\n")
            val buf: ByteArray?
            val header: qfiles.dheader_t
            val length: Int

            map_noareas = Cvar.Get("map_noareas", "0", 0)

            if (map_name.equals(name) && (clientload || 0 == Cvar.VariableValue("flushmap"))) {

                checksum[0] = last_checksum

                if (!clientload) {
                    Arrays.fill(portalopen, false)
                    FloodAreaConnections()
                }
                return map_cmodels[0] // still have the right version
            }

            // free old stuff
            numnodes = 0
            numleafs = 0
            numcmodels = 0
            numvisibility = 0
            numentitychars = 0
            map_entitystring = ""
            map_name = ""

            if (name == null || name.length() == 0) {
                numleafs = 1
                numclusters = 1
                numareas = 1
                checksum[0] = 0
                return map_cmodels[0]
                // cinematic servers won't have anything at all
            }

            //
            // load the file
            //
            buf = FS.LoadFile(name)

            if (buf == null)
                Com.Error(Defines.ERR_DROP, "Couldn't load " + name)

            length = buf.size()

            val bbuf = ByteBuffer.wrap(buf)

            last_checksum = MD4.Com_BlockChecksum(buf, length)
            checksum[0] = last_checksum

            header = qfiles.dheader_t(bbuf.slice())

            if (header.version != Defines.BSPVERSION)
                Com.Error(Defines.ERR_DROP, "CMod_LoadBrushModel: " + name + " has wrong version number (" + header.version + " should be " + Defines.BSPVERSION + ")")

            cmod_base = buf

            // load into heap
            CMod_LoadSurfaces(header.lumps[Defines.LUMP_TEXINFO]) // ok
            CMod_LoadLeafs(header.lumps[Defines.LUMP_LEAFS])
            CMod_LoadLeafBrushes(header.lumps[Defines.LUMP_LEAFBRUSHES])
            CMod_LoadPlanes(header.lumps[Defines.LUMP_PLANES])
            CMod_LoadBrushes(header.lumps[Defines.LUMP_BRUSHES])
            CMod_LoadBrushSides(header.lumps[Defines.LUMP_BRUSHSIDES])
            CMod_LoadSubmodels(header.lumps[Defines.LUMP_MODELS])

            CMod_LoadNodes(header.lumps[Defines.LUMP_NODES])
            CMod_LoadAreas(header.lumps[Defines.LUMP_AREAS])
            CMod_LoadAreaPortals(header.lumps[Defines.LUMP_AREAPORTALS])
            CMod_LoadVisibility(header.lumps[Defines.LUMP_VISIBILITY])
            CMod_LoadEntityString(header.lumps[Defines.LUMP_ENTITIES])

            FS.FreeFile(buf)

            CM_InitBoxHull()

            Arrays.fill(portalopen, false)

            FloodAreaConnections()

            map_name = name

            return map_cmodels[0]
        }

        /** Loads Submodels.  */
        public fun CMod_LoadSubmodels(l: lump_t) {
            Com.DPrintf("CMod_LoadSubmodels()\n")
            var `in`: qfiles.dmodel_t
            var out: cmodel_t
            var i: Int
            var j: Int
            val count: Int

            if ((l.filelen % qfiles.dmodel_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "CMod_LoadBmodel: funny lump size")

            count = l.filelen / qfiles.dmodel_t.SIZE

            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map with no models")
            if (count > Defines.MAX_MAP_MODELS)
                Com.Error(Defines.ERR_DROP, "Map has too many models")

            Com.DPrintf(" numcmodels=" + count + "\n")
            numcmodels = count

            if (debugloadmap) {
                Com.DPrintf("submodles(headnode, <origin>, <mins>, <maxs>)\n")
            }
            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dmodel_t(ByteBuffer.wrap(cmod_base, i * qfiles.dmodel_t.SIZE + l.fileofs, qfiles.dmodel_t.SIZE))
                    out = map_cmodels[i]

                    run {
                        j = 0
                        while (j < 3) {
                            // spread the mins / maxs by a pixel
                            out.mins[j] = `in`.mins[j] - 1
                            out.maxs[j] = `in`.maxs[j] + 1
                            out.origin[j] = `in`.origin[j]
                            j++
                        }
                    }
                    out.headnode = `in`.headnode
                    if (debugloadmap) {
                        Com.DPrintf("|%6i|%8.2f|%8.2f|%8.2f|  %8.2f|%8.2f|%8.2f|   %8.2f|%8.2f|%8.2f|\n", Vargs().add(out.headnode).add(out.origin[0]).add(out.origin[1]).add(out.origin[2]).add(out.mins[0]).add(out.mins[1]).add(out.mins[2]).add(out.maxs[0]).add(out.maxs[1]).add(out.maxs[2]))
                    }
                    i++
                }
            }
        }

        var debugloadmap = false

        /** Loads surfaces.  */
        public fun CMod_LoadSurfaces(l: lump_t) {
            Com.DPrintf("CMod_LoadSurfaces()\n")
            var `in`: texinfo_t
            val out: mapsurface_t
            var i: Int
            val count: Int

            if ((l.filelen % texinfo_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / texinfo_t.SIZE
            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map with no surfaces")
            if (count > Defines.MAX_MAP_TEXINFO)
                Com.Error(Defines.ERR_DROP, "Map has too many surfaces")

            numtexinfo = count
            Com.DPrintf(" numtexinfo=" + count + "\n")
            if (debugloadmap)
                Com.DPrintf("surfaces:\n")

            run {
                i = 0
                while (i < count) {
                    out = map_surfaces[i] = mapsurface_t()
                    `in` = texinfo_t(cmod_base, l.fileofs + i * texinfo_t.SIZE, texinfo_t.SIZE)

                    out.c.name = `in`.texture
                    out.rname = `in`.texture
                    out.c.flags = `in`.flags
                    out.c.value = `in`.value

                    if (debugloadmap) {
                        Com.DPrintf("|%20s|%20s|%6i|%6i|\n", Vargs().add(out.c.name).add(out.rname).add(out.c.value).add(out.c.flags))
                    }
                    i++

                }
            }
        }

        /** Loads nodes.  */
        public fun CMod_LoadNodes(l: lump_t) {
            Com.DPrintf("CMod_LoadNodes()\n")
            var `in`: qfiles.dnode_t
            var child: Int
            var out: cnode_t
            var i: Int
            var j: Int
            val count: Int

            if ((l.filelen % qfiles.dnode_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size:" + l.fileofs + "," + qfiles.dnode_t.SIZE)
            count = l.filelen / qfiles.dnode_t.SIZE

            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map has no nodes")
            if (count > Defines.MAX_MAP_NODES)
                Com.Error(Defines.ERR_DROP, "Map has too many nodes")

            numnodes = count
            Com.DPrintf(" numnodes=" + count + "\n")

            if (debugloadmap) {
                Com.DPrintf("nodes(planenum, child[0], child[1])\n")
            }

            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dnode_t(ByteBuffer.wrap(cmod_base, qfiles.dnode_t.SIZE * i + l.fileofs, qfiles.dnode_t.SIZE))
                    out = map_nodes[i]

                    out.plane = map_planes[`in`.planenum]
                    run {
                        j = 0
                        while (j < 2) {
                            child = `in`.children[j]
                            out.children[j] = child
                            j++
                        }
                    }
                    if (debugloadmap) {
                        Com.DPrintf("|%6i| %6i| %6i|\n", Vargs().add(`in`.planenum).add(out.children[0]).add(out.children[1]))
                    }
                    i++
                }
            }
        }

        /** Loads brushes.  */
        public fun CMod_LoadBrushes(l: lump_t) {
            Com.DPrintf("CMod_LoadBrushes()\n")
            var `in`: qfiles.dbrush_t
            var out: cbrush_t
            var i: Int
            val count: Int

            if ((l.filelen % qfiles.dbrush_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / qfiles.dbrush_t.SIZE

            if (count > Defines.MAX_MAP_BRUSHES)
                Com.Error(Defines.ERR_DROP, "Map has too many brushes")

            numbrushes = count
            Com.DPrintf(" numbrushes=" + count + "\n")
            if (debugloadmap) {
                Com.DPrintf("brushes:(firstbrushside, numsides, contents)\n")
            }
            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dbrush_t(ByteBuffer.wrap(cmod_base, i * qfiles.dbrush_t.SIZE + l.fileofs, qfiles.dbrush_t.SIZE))
                    out = map_brushes[i]
                    out.firstbrushside = `in`.firstside
                    out.numsides = `in`.numsides
                    out.contents = `in`.contents

                    if (debugloadmap) {
                        Com.DPrintf("| %6i| %6i| %8X|\n", Vargs().add(out.firstbrushside).add(out.numsides).add(out.contents))
                    }
                    i++
                }
            }
        }

        /** Loads leafs.    */
        public fun CMod_LoadLeafs(l: lump_t) {
            Com.DPrintf("CMod_LoadLeafs()\n")
            var i: Int
            var out: cleaf_t
            var `in`: qfiles.dleaf_t
            val count: Int

            if ((l.filelen % qfiles.dleaf_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / qfiles.dleaf_t.SIZE

            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map with no leafs")

            // need to save space for box planes
            if (count > Defines.MAX_MAP_PLANES)
                Com.Error(Defines.ERR_DROP, "Map has too many planes")

            Com.DPrintf(" numleafes=" + count + "\n")

            numleafs = count
            numclusters = 0
            if (debugloadmap)
                Com.DPrintf("cleaf-list:(contents, cluster, area, firstleafbrush, numleafbrushes)\n")
            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dleaf_t(cmod_base, i * qfiles.dleaf_t.SIZE + l.fileofs, qfiles.dleaf_t.SIZE)

                    out = map_leafs[i]

                    out.contents = `in`.contents
                    out.cluster = `in`.cluster
                    out.area = `in`.area
                    out.firstleafbrush = `in`.firstleafbrush as Short
                    out.numleafbrushes = `in`.numleafbrushes as Short

                    if (out.cluster >= numclusters)
                        numclusters = out.cluster + 1

                    if (debugloadmap) {
                        Com.DPrintf("|%8x|%6i|%6i|%6i|\n", Vargs().add(out.contents).add(out.cluster).add(out.area).add(out.firstleafbrush).add(out.numleafbrushes))
                    }
                    i++

                }
            }

            Com.DPrintf(" numclusters=" + numclusters + "\n")

            if (map_leafs[0].contents != Defines.CONTENTS_SOLID)
                Com.Error(Defines.ERR_DROP, "Map leaf 0 is not CONTENTS_SOLID")

            solidleaf = 0
            emptyleaf = -1

            run {
                i = 1
                while (i < numleafs) {
                    if (map_leafs[i].contents == 0) {
                        emptyleaf = i
                        break
                    }
                    i++
                }
            }

            if (emptyleaf == -1)
                Com.Error(Defines.ERR_DROP, "Map does not have an empty leaf")
        }

        /** Loads planes.  */
        public fun CMod_LoadPlanes(l: lump_t) {
            Com.DPrintf("CMod_LoadPlanes()\n")
            var i: Int
            var j: Int
            var out: cplane_t
            var `in`: qfiles.dplane_t
            val count: Int
            var bits: Int

            if ((l.filelen % qfiles.dplane_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / qfiles.dplane_t.SIZE

            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map with no planes")

            // need to save space for box planes
            if (count > Defines.MAX_MAP_PLANES)
                Com.Error(Defines.ERR_DROP, "Map has too many planes")

            Com.DPrintf(" numplanes=" + count + "\n")

            numplanes = count
            if (debugloadmap) {
                Com.DPrintf("cplanes(normal[0],normal[1],normal[2], dist, type, signbits)\n")
            }

            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dplane_t(ByteBuffer.wrap(cmod_base, i * qfiles.dplane_t.SIZE + l.fileofs, qfiles.dplane_t.SIZE))

                    out = map_planes[i]

                    bits = 0
                    run {
                        j = 0
                        while (j < 3) {
                            out.normal[j] = `in`.normal[j]

                            if (out.normal[j] < 0)
                                bits = bits or (1 shl j)
                            j++
                        }
                    }

                    out.dist = `in`.dist
                    out.type = `in`.type as Byte
                    out.signbits = bits.toByte()

                    if (debugloadmap) {
                        Com.DPrintf("|%6.2f|%6.2f|%6.2f| %10.2f|%3i| %1i|\n", Vargs().add(out.normal[0]).add(out.normal[1]).add(out.normal[2]).add(out.dist).add(out.type).add(out.signbits))
                    }
                    i++
                }
            }
        }

        /** Loads leaf brushes.  */
        public fun CMod_LoadLeafBrushes(l: lump_t) {
            Com.DPrintf("CMod_LoadLeafBrushes()\n")
            var i: Int
            val out: IntArray
            val count: Int

            if ((l.filelen % 2) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / 2

            Com.DPrintf(" numbrushes=" + count + "\n")

            if (count < 1)
                Com.Error(Defines.ERR_DROP, "Map with no planes")

            // need to save space for box planes
            if (count > Defines.MAX_MAP_LEAFBRUSHES)
                Com.Error(Defines.ERR_DROP, "Map has too many leafbrushes")

            out = map_leafbrushes
            numleafbrushes = count

            val bb = ByteBuffer.wrap(cmod_base, l.fileofs, count * 2).order(ByteOrder.LITTLE_ENDIAN)

            if (debugloadmap) {
                Com.DPrintf("map_brushes:\n")
            }

            run {
                i = 0
                while (i < count) {
                    out[i] = bb.getShort().toInt()
                    if (debugloadmap) {
                        Com.DPrintf("|%6i|%6i|\n", Vargs().add(i).add(out[i]))
                    }
                    i++
                }
            }
        }

        /** Loads brush sides.  */
        public fun CMod_LoadBrushSides(l: lump_t) {
            Com.DPrintf("CMod_LoadBrushSides()\n")
            var i: Int
            var j: Int
            var out: cbrushside_t
            var `in`: qfiles.dbrushside_t
            val count: Int
            var num: Int

            if ((l.filelen % qfiles.dbrushside_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")
            count = l.filelen / qfiles.dbrushside_t.SIZE

            // need to save space for box planes
            if (count > Defines.MAX_MAP_BRUSHSIDES)
                Com.Error(Defines.ERR_DROP, "Map has too many planes")

            numbrushsides = count

            Com.DPrintf(" numbrushsides=" + count + "\n")

            if (debugloadmap) {
                Com.DPrintf("brushside(planenum, surfacenum):\n")
            }
            run {
                i = 0
                while (i < count) {

                    `in` = qfiles.dbrushside_t(ByteBuffer.wrap(cmod_base, i * qfiles.dbrushside_t.SIZE + l.fileofs, qfiles.dbrushside_t.SIZE))

                    out = map_brushsides[i]

                    num = `in`.planenum

                    out.plane = map_planes[num] // pointer

                    j = `in`.texinfo

                    if (j >= numtexinfo)
                        Com.Error(Defines.ERR_DROP, "Bad brushside texinfo")

                    // java specific handling of -1
                    if (j == -1)
                        out.surface = mapsurface_t() // just for safety
                    else
                        out.surface = map_surfaces[j]

                    if (debugloadmap) {
                        Com.DPrintf("| %6i| %6i|\n", Vargs().add(num).add(j))
                    }
                    i++
                }
            }
        }

        /** Loads areas.  */
        public fun CMod_LoadAreas(l: lump_t) {
            Com.DPrintf("CMod_LoadAreas()\n")
            var i: Int
            var out: carea_t
            var `in`: qfiles.darea_t
            val count: Int

            if ((l.filelen % qfiles.darea_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")

            count = l.filelen / qfiles.darea_t.SIZE

            if (count > Defines.MAX_MAP_AREAS)
                Com.Error(Defines.ERR_DROP, "Map has too many areas")

            Com.DPrintf(" numareas=" + count + "\n")
            numareas = count

            if (debugloadmap) {
                Com.DPrintf("areas(numportals, firstportal)\n")
            }

            run {
                i = 0
                while (i < count) {

                    `in` = qfiles.darea_t(ByteBuffer.wrap(cmod_base, i * qfiles.darea_t.SIZE + l.fileofs, qfiles.darea_t.SIZE))
                    out = map_areas[i]

                    out.numareaportals = `in`.numareaportals
                    out.firstareaportal = `in`.firstareaportal
                    out.floodvalid = 0
                    out.floodnum = 0
                    if (debugloadmap) {
                        Com.DPrintf("| %6i| %6i|\n", Vargs().add(out.numareaportals).add(out.firstareaportal))
                    }
                    i++
                }
            }
        }

        /** Loads area portals.  */
        public fun CMod_LoadAreaPortals(l: lump_t) {
            Com.DPrintf("CMod_LoadAreaPortals()\n")
            var i: Int
            var out: qfiles.dareaportal_t
            var `in`: qfiles.dareaportal_t
            val count: Int

            if ((l.filelen % qfiles.dareaportal_t.SIZE) != 0)
                Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size")
            count = l.filelen / qfiles.dareaportal_t.SIZE

            if (count > Defines.MAX_MAP_AREAS)
                Com.Error(Defines.ERR_DROP, "Map has too many areas")

            numareaportals = count
            Com.DPrintf(" numareaportals=" + count + "\n")
            if (debugloadmap) {
                Com.DPrintf("areaportals(portalnum, otherarea)\n")
            }
            run {
                i = 0
                while (i < count) {
                    `in` = qfiles.dareaportal_t(ByteBuffer.wrap(cmod_base, i * qfiles.dareaportal_t.SIZE + l.fileofs, qfiles.dareaportal_t.SIZE))

                    out = map_areaportals[i]

                    out.portalnum = `in`.portalnum
                    out.otherarea = `in`.otherarea

                    if (debugloadmap) {
                        Com.DPrintf("|%6i|%6i|\n", Vargs().add(out.portalnum).add(out.otherarea))
                    }
                    i++
                }
            }
        }

        /** Loads visibility data.  */
        public fun CMod_LoadVisibility(l: lump_t) {
            Com.DPrintf("CMod_LoadVisibility()\n")

            numvisibility = l.filelen

            Com.DPrintf(" numvisibility=" + numvisibility + "\n")

            if (l.filelen > Defines.MAX_MAP_VISIBILITY)
                Com.Error(Defines.ERR_DROP, "Map has too large visibility lump")

            System.arraycopy(cmod_base, l.fileofs, map_visibility, 0, l.filelen)

            val bb = ByteBuffer.wrap(map_visibility, 0, l.filelen)
            bb.order(ByteOrder.LITTLE_ENDIAN)

            map_vis = qfiles.dvis_t(bb)

        }

        /** Loads entity strings.  */
        public fun CMod_LoadEntityString(l: lump_t) {
            Com.DPrintf("CMod_LoadEntityString()\n")

            numentitychars = l.filelen

            if (l.filelen > Defines.MAX_MAP_ENTSTRING)
                Com.Error(Defines.ERR_DROP, "Map has too large entity lump")

            var x = 0
            while (x < l.filelen && cmod_base[x + l.fileofs] != 0) {
                x++
            }

            map_entitystring = String(cmod_base, l.fileofs, x).trim()
            Com.dprintln("entitystring=" + map_entitystring.length() + " bytes, [" + map_entitystring.substring(0, Math.min(map_entitystring.length(), 15)) + "...]")
        }

        /** Returns the model with a given id "*" +   */
        public fun InlineModel(name: String?): cmodel_t {
            val num: Int

            if (name == null || name.charAt(0) != '*')
                Com.Error(Defines.ERR_DROP, "CM_InlineModel: bad name")

            num = Lib.atoi(name!!.substring(1))

            if (num < 1 || num >= numcmodels)
                Com.Error(Defines.ERR_DROP, "CM_InlineModel: bad number")

            return map_cmodels[num]
        }

        public fun CM_NumClusters(): Int {
            return numclusters
        }

        public fun CM_NumInlineModels(): Int {
            return numcmodels
        }

        public fun CM_EntityString(): String {
            return map_entitystring
        }

        public fun CM_LeafContents(leafnum: Int): Int {
            if (leafnum < 0 || leafnum >= numleafs)
                Com.Error(Defines.ERR_DROP, "CM_LeafContents: bad number")
            return map_leafs[leafnum].contents
        }

        public fun CM_LeafCluster(leafnum: Int): Int {
            if (leafnum < 0 || leafnum >= numleafs)
                Com.Error(Defines.ERR_DROP, "CM_LeafCluster: bad number")
            return map_leafs[leafnum].cluster
        }

        public fun CM_LeafArea(leafnum: Int): Int {
            if (leafnum < 0 || leafnum >= numleafs)
                Com.Error(Defines.ERR_DROP, "CM_LeafArea: bad number")
            return map_leafs[leafnum].area
        }

        var box_planes: Array<cplane_t>

        var box_headnode: Int = 0

        var box_brush: cbrush_t

        var box_leaf: cleaf_t

        /** Set up the planes and nodes so that the six floats of a bounding box can
         * just be stored out and get a proper clipping hull structure.
         */
        public fun CM_InitBoxHull() {
            var i: Int
            var side: Int
            var c: cnode_t
            var p: cplane_t
            var s: cbrushside_t

            box_headnode = numnodes //rst: still room for 6 brushes left?

            box_planes = array<cplane_t>(map_planes[numplanes], map_planes[numplanes + 1], map_planes[numplanes + 2], map_planes[numplanes + 3], map_planes[numplanes + 4], map_planes[numplanes + 5], map_planes[numplanes + 6], map_planes[numplanes + 7], map_planes[numplanes + 8], map_planes[numplanes + 9], map_planes[numplanes + 10], map_planes[numplanes + 11], map_planes[numplanes + 12])

            if (numnodes + 6 > Defines.MAX_MAP_NODES || numbrushes + 1 > Defines.MAX_MAP_BRUSHES || numleafbrushes + 1 > Defines.MAX_MAP_LEAFBRUSHES || numbrushsides + 6 > Defines.MAX_MAP_BRUSHSIDES || numplanes + 12 > Defines.MAX_MAP_PLANES)
                Com.Error(Defines.ERR_DROP, "Not enough room for box tree")

            box_brush = map_brushes[numbrushes]
            box_brush.numsides = 6
            box_brush.firstbrushside = numbrushsides
            box_brush.contents = Defines.CONTENTS_MONSTER

            box_leaf = map_leafs[numleafs]
            box_leaf.contents = Defines.CONTENTS_MONSTER
            box_leaf.firstleafbrush = numleafbrushes.toShort()
            box_leaf.numleafbrushes = 1

            map_leafbrushes[numleafbrushes] = numbrushes

            run {
                i = 0
                while (i < 6) {
                    side = i and 1

                    // brush sides
                    s = map_brushsides[numbrushsides + i]
                    s.plane = map_planes[(numplanes + i * 2 + side)]
                    s.surface = nullsurface

                    // nodes
                    c = map_nodes[box_headnode + i]
                    c.plane = map_planes[(numplanes + i * 2)]
                    c.children[side] = -1 - emptyleaf
                    if (i != 5)
                        c.children[side xor 1] = box_headnode + i + 1
                    else
                        c.children[side xor 1] = -1 - numleafs

                    // planes
                    p = box_planes[i * 2]
                    p.type = (i shr 1).toByte()
                    p.signbits = 0
                    Math3D.VectorClear(p.normal)
                    p.normal[i shr 1] = 1

                    p = box_planes[i * 2 + 1]
                    p.type = (3 + (i shr 1)).toByte()
                    p.signbits = 0
                    Math3D.VectorClear(p.normal)
                    p.normal[i shr 1] = -1
                    i++
                }
            }
        }

        /** To keep everything totally uniform, bounding boxes are turned into small
         * BSP trees instead of being compared directly.  */
        public fun HeadnodeForBox(mins: FloatArray, maxs: FloatArray): Int {
            box_planes[0].dist = maxs[0]
            box_planes[1].dist = -maxs[0]
            box_planes[2].dist = mins[0]
            box_planes[3].dist = -mins[0]
            box_planes[4].dist = maxs[1]
            box_planes[5].dist = -maxs[1]
            box_planes[6].dist = mins[1]
            box_planes[7].dist = -mins[1]
            box_planes[8].dist = maxs[2]
            box_planes[9].dist = -maxs[2]
            box_planes[10].dist = mins[2]
            box_planes[11].dist = -mins[2]

            return box_headnode
        }

        /** Recursively searches the leaf number that contains the 3d point.  */
        private fun CM_PointLeafnum_r(p: FloatArray, num: Int): Int {
            var num = num
            val d: Float
            val node: cnode_t
            val plane: cplane_t

            while (num >= 0) {
                node = map_nodes[num]
                plane = node.plane

                if (plane.type < 3)
                    d = p[plane.type] - plane.dist
                else
                    d = Math3D.DotProduct(plane.normal, p) - plane.dist
                if (d < 0)
                    num = node.children[1]
                else
                    num = node.children[0]
            }

            Globals.c_pointcontents++ // optimize counter

            return -1 - num
        }

        /** Searches the leaf number that contains the 3d point.  */
        public fun CM_PointLeafnum(p: FloatArray): Int {
            // sound may call this without map loaded
            if (numplanes == 0)
                return 0
            return CM_PointLeafnum_r(p, 0)
        }


        private var leaf_count: Int = 0
        private var leaf_maxcount: Int = 0

        private var leaf_list: IntArray? = null

        private var leaf_mins: FloatArray? = null
        private var leaf_maxs: FloatArray? = null

        private var leaf_topnode: Int = 0

        /** Recursively fills in a list of all the leafs touched.  */
        private fun CM_BoxLeafnums_r(nodenum: Int) {
            var nodenum = nodenum
            val plane: cplane_t
            val node: cnode_t
            val s: Int

            while (true) {
                if (nodenum < 0) {
                    if (leaf_count >= leaf_maxcount) {
                        Com.DPrintf("CM_BoxLeafnums_r: overflow\n")
                        return
                    }
                    leaf_list[leaf_count++] = -1 - nodenum
                    return
                }

                node = map_nodes[nodenum]
                plane = node.plane

                s = Math3D.BoxOnPlaneSide(leaf_mins, leaf_maxs, plane)

                if (s == 1)
                    nodenum = node.children[0]
                else if (s == 2)
                    nodenum = node.children[1]
                else {
                    // go down both
                    if (leaf_topnode == -1)
                        leaf_topnode = nodenum
                    CM_BoxLeafnums_r(node.children[0])
                    nodenum = node.children[1]
                }
            }
        }

        /** Fills in a list of all the leafs touched and starts with the head node.  */
        private fun CM_BoxLeafnums_headnode(mins: FloatArray, maxs: FloatArray, list: IntArray, listsize: Int, headnode: Int, topnode: IntArray?): Int {
            leaf_list = list
            leaf_count = 0
            leaf_maxcount = listsize
            leaf_mins = mins
            leaf_maxs = maxs

            leaf_topnode = -1

            CM_BoxLeafnums_r(headnode)

            if (topnode != null)
                topnode[0] = leaf_topnode

            return leaf_count
        }

        /** Fills in a list of all the leafs touched.  */
        public fun CM_BoxLeafnums(mins: FloatArray, maxs: FloatArray, list: IntArray, listsize: Int, topnode: IntArray): Int {
            return CM_BoxLeafnums_headnode(mins, maxs, list, listsize, map_cmodels[0].headnode, topnode)
        }

        /** Returns a tag that describes the content of the point.  */
        public fun PointContents(p: FloatArray, headnode: Int): Int {
            val l: Int

            if (numnodes == 0)
            // map not loaded
                return 0

            l = CM_PointLeafnum_r(p, headnode)

            return map_leafs[l].contents
        }

        /*
     * ================== CM_TransformedPointContents
     * 
     * Handles offseting and rotation of the end points for moving and rotating
     * entities ==================
     */
        public fun TransformedPointContents(p: FloatArray, headnode: Int, origin: FloatArray, angles: FloatArray): Int {
            val p_l = floatArray(0.0, 0.0, 0.0)
            val temp = floatArray(0.0, 0.0, 0.0)
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            val up = floatArray(0.0, 0.0, 0.0)
            val l: Int

            // subtract origin offset
            Math3D.VectorSubtract(p, origin, p_l)

            // rotate start and end into the models frame of reference
            if (headnode != box_headnode && (angles[0] != 0 || angles[1] != 0 || angles[2] != 0)) {
                Math3D.AngleVectors(angles, forward, right, up)

                Math3D.VectorCopy(p_l, temp)
                p_l[0] = Math3D.DotProduct(temp, forward)
                p_l[1] = -Math3D.DotProduct(temp, right)
                p_l[2] = Math3D.DotProduct(temp, up)
            }

            l = CM_PointLeafnum_r(p_l, headnode)

            return map_leafs[l].contents
        }

        /*
     * ===============================================================================
     * 
     * BOX TRACING
     * 
     * ===============================================================================
     */

        // 1/32 epsilon to keep floating point happy
        private val DIST_EPSILON = 0.03125.toFloat()

        private val trace_start = floatArray(0.0, 0.0, 0.0)
        private val trace_end = floatArray(0.0, 0.0, 0.0)

        private val trace_mins = floatArray(0.0, 0.0, 0.0)
        private val trace_maxs = floatArray(0.0, 0.0, 0.0)

        private val trace_extents = floatArray(0.0, 0.0, 0.0)

        private var trace_trace = trace_t()

        private var trace_contents: Int = 0

        private var trace_ispoint: Boolean = false // optimized case

        /*
     * ================ CM_ClipBoxToBrush ================
     */
        public fun CM_ClipBoxToBrush(mins: FloatArray, maxs: FloatArray, p1: FloatArray, p2: FloatArray, trace: trace_t, brush: cbrush_t) {
            var i: Int
            var j: Int
            var plane: cplane_t
            var clipplane: cplane_t?
            var dist: Float
            var enterfrac: Float
            var leavefrac: Float
            val ofs = floatArray(0.0, 0.0, 0.0)
            var d1: Float
            var d2: Float
            var getout: Boolean
            var startout: Boolean
            var f: Float
            var side: cbrushside_t
            var leadside: cbrushside_t?

            enterfrac = (-1).toFloat()
            leavefrac = 1
            clipplane = null

            if (brush.numsides == 0)
                return

            Globals.c_brush_traces++

            getout = false
            startout = false
            leadside = null

            run {
                i = 0
                while (i < brush.numsides) {
                    side = map_brushsides[brush.firstbrushside + i]
                    plane = side.plane

                    // FIXME: special case for axial

                    if (!trace_ispoint) {
                        // general box case

                        // push the plane out apropriately for mins/maxs

                        // FIXME: use signbits into 8 way lookup for each mins/maxs
                        run {
                            j = 0
                            while (j < 3) {
                                if (plane.normal[j] < 0)
                                    ofs[j] = maxs[j]
                                else
                                    ofs[j] = mins[j]
                                j++
                            }
                        }
                        dist = Math3D.DotProduct(ofs, plane.normal)
                        dist = plane.dist - dist
                    } else {
                        // special point case
                        dist = plane.dist
                    }

                    d1 = Math3D.DotProduct(p1, plane.normal) - dist
                    d2 = Math3D.DotProduct(p2, plane.normal) - dist

                    if (d2 > 0)
                        getout = true // endpoint is not in solid
                    if (d1 > 0)
                        startout = true

                    // if completely in front of face, no intersection
                    if (d1 > 0 && d2 >= d1)
                        return

                    if (d1 <= 0 && d2 <= 0)
                        continue

                    // crosses face
                    if (d1 > d2) {
                        // enter
                        f = (d1 - DIST_EPSILON) / (d1 - d2)
                        if (f > enterfrac) {
                            enterfrac = f
                            clipplane = plane
                            leadside = side
                        }
                    } else {
                        // leave
                        f = (d1 + DIST_EPSILON) / (d1 - d2)
                        if (f < leavefrac)
                            leavefrac = f
                    }
                    i++
                }
            }

            if (!startout) {
                // original point was inside brush
                trace.startsolid = true
                if (!getout)
                    trace.allsolid = true
                return
            }
            if (enterfrac < leavefrac) {
                if (enterfrac > -1 && enterfrac < trace.fraction) {
                    if (enterfrac < 0)
                        enterfrac = 0
                    trace.fraction = enterfrac
                    // copy
                    trace.plane.set(clipplane)
                    trace.surface = leadside!!.surface.c
                    trace.contents = brush.contents
                }
            }
        }

        /*
     * ================ CM_TestBoxInBrush ================
     */
        public fun CM_TestBoxInBrush(mins: FloatArray, maxs: FloatArray, p1: FloatArray, trace: trace_t, brush: cbrush_t) {
            var i: Int
            var j: Int
            var plane: cplane_t
            var dist: Float
            val ofs = floatArray(0.0, 0.0, 0.0)
            var d1: Float
            var side: cbrushside_t

            if (brush.numsides == 0)
                return

            run {
                i = 0
                while (i < brush.numsides) {
                    side = map_brushsides[brush.firstbrushside + i]
                    plane = side.plane

                    // FIXME: special case for axial
                    // general box case
                    // push the plane out apropriately for mins/maxs
                    // FIXME: use signbits into 8 way lookup for each mins/maxs

                    run {
                        j = 0
                        while (j < 3) {
                            if (plane.normal[j] < 0)
                                ofs[j] = maxs[j]
                            else
                                ofs[j] = mins[j]
                            j++
                        }
                    }
                    dist = Math3D.DotProduct(ofs, plane.normal)
                    dist = plane.dist - dist

                    d1 = Math3D.DotProduct(p1, plane.normal) - dist

                    // if completely in front of face, no intersection
                    if (d1 > 0)
                        return
                    i++

                }
            }

            // inside this brush
            trace.startsolid = trace.allsolid = true
            trace.fraction = 0
            trace.contents = brush.contents
        }

        /*
     * ================ CM_TraceToLeaf ================
     */
        public fun CM_TraceToLeaf(leafnum: Int) {
            var k: Int
            var brushnum: Int
            val leaf: cleaf_t
            var b: cbrush_t

            leaf = map_leafs[leafnum]
            if (0 == (leaf.contents and trace_contents))
                return

            // trace line against all brushes in the leaf
            run {
                k = 0
                while (k < leaf.numleafbrushes) {

                    brushnum = map_leafbrushes[leaf.firstleafbrush.toInt() + k]
                    b = map_brushes[brushnum]
                    if (b.checkcount == checkcount)
                        continue // already checked this brush in another leaf
                    b.checkcount = checkcount

                    if (0 == (b.contents and trace_contents))
                        continue
                    CM_ClipBoxToBrush(trace_mins, trace_maxs, trace_start, trace_end, trace_trace, b)
                    if (0 == trace_trace.fraction)
                        return
                    k++
                }
            }

        }

        /*
     * ================ CM_TestInLeaf ================
     */
        public fun CM_TestInLeaf(leafnum: Int) {
            var k: Int
            var brushnum: Int
            val leaf: cleaf_t
            var b: cbrush_t

            leaf = map_leafs[leafnum]
            if (0 == (leaf.contents and trace_contents))
                return
            // trace line against all brushes in the leaf
            run {
                k = 0
                while (k < leaf.numleafbrushes) {
                    brushnum = map_leafbrushes[leaf.firstleafbrush.toInt() + k]
                    b = map_brushes[brushnum]
                    if (b.checkcount == checkcount)
                        continue // already checked this brush in another leaf
                    b.checkcount = checkcount

                    if (0 == (b.contents and trace_contents))
                        continue
                    CM_TestBoxInBrush(trace_mins, trace_maxs, trace_start, trace_trace, b)
                    if (0 == trace_trace.fraction)
                        return
                    k++
                }
            }

        }

        /*
     * ================== CM_RecursiveHullCheck ==================
     */
        public fun CM_RecursiveHullCheck(num: Int, p1f: Float, p2f: Float, p1: FloatArray, p2: FloatArray) {
            val node: cnode_t
            val plane: cplane_t
            val t1: Float
            val t2: Float
            val offset: Float
            var frac: Float
            var frac2: Float
            val idist: Float
            var i: Int
            val side: Int
            var midf: Float

            if (trace_trace.fraction <= p1f)
                return  // already hit something nearer

            // if < 0, we are in a leaf node
            if (num < 0) {
                CM_TraceToLeaf(-1 - num)
                return
            }

            //
            // find the point distances to the seperating plane
            // and the offset for the size of the box
            //
            node = map_nodes[num]
            plane = node.plane

            if (plane.type < 3) {
                t1 = p1[plane.type] - plane.dist
                t2 = p2[plane.type] - plane.dist
                offset = trace_extents[plane.type]
            } else {
                t1 = Math3D.DotProduct(plane.normal, p1) - plane.dist
                t2 = Math3D.DotProduct(plane.normal, p2) - plane.dist
                if (trace_ispoint)
                    offset = 0
                else
                    offset = Math.abs(trace_extents[0] * plane.normal[0]) + Math.abs(trace_extents[1] * plane.normal[1]) + Math.abs(trace_extents[2] * plane.normal[2])
            }

            // see which sides we need to consider
            if (t1 >= offset && t2 >= offset) {
                CM_RecursiveHullCheck(node.children[0], p1f, p2f, p1, p2)
                return
            }
            if (t1 < -offset && t2 < -offset) {
                CM_RecursiveHullCheck(node.children[1], p1f, p2f, p1, p2)
                return
            }

            // put the crosspoint DIST_EPSILON pixels on the near side
            if (t1 < t2) {
                idist = 1.0.toFloat() / (t1 - t2)
                side = 1
                frac2 = (t1 + offset + DIST_EPSILON) * idist
                frac = (t1 - offset + DIST_EPSILON) * idist
            } else if (t1 > t2) {
                idist = 1.0.toFloat() / (t1 - t2)
                side = 0
                frac2 = (t1 - offset - DIST_EPSILON) * idist
                frac = (t1 + offset + DIST_EPSILON) * idist
            } else {
                side = 0
                frac = 1
                frac2 = 0
            }

            // move up to the node
            if (frac < 0)
                frac = 0
            if (frac > 1)
                frac = 1

            midf = p1f + (p2f - p1f) * frac
            val mid = Vec3Cache.get()

            run {
                i = 0
                while (i < 3) {
                    mid[i] = p1[i] + frac * (p2[i] - p1[i])
                    i++
                }
            }

            CM_RecursiveHullCheck(node.children[side], p1f, midf, p1, mid)

            // go past the node
            if (frac2 < 0)
                frac2 = 0
            if (frac2 > 1)
                frac2 = 1

            midf = p1f + (p2f - p1f) * frac2
            run {
                i = 0
                while (i < 3) {
                    mid[i] = p1[i] + frac2 * (p2[i] - p1[i])
                    i++
                }
            }

            CM_RecursiveHullCheck(node.children[side xor 1], midf, p2f, mid, p2)
            Vec3Cache.release()
        }

        //======================================================================

        /*
     * ================== CM_BoxTrace ==================
     */
        public fun BoxTrace(start: FloatArray, end: FloatArray, mins: FloatArray, maxs: FloatArray, headnode: Int, brushmask: Int): trace_t {

            // for multi-check avoidance
            checkcount++

            // for statistics, may be zeroed
            Globals.c_traces++

            // fill in a default trace
            //was: memset(& trace_trace, 0, sizeof(trace_trace));
            trace_trace = trace_t()

            trace_trace.fraction = 1
            trace_trace.surface = nullsurface.c

            if (numnodes == 0) {
                // map not loaded
                return trace_trace
            }

            trace_contents = brushmask
            Math3D.VectorCopy(start, trace_start)
            Math3D.VectorCopy(end, trace_end)
            Math3D.VectorCopy(mins, trace_mins)
            Math3D.VectorCopy(maxs, trace_maxs)

            //
            // check for position test special case
            //
            if (start[0] == end[0] && start[1] == end[1] && start[2] == end[2]) {

                val leafs = IntArray(1024)
                var i: Int
                val numleafs: Int
                val c1 = floatArray(0.0, 0.0, 0.0)
                val c2 = floatArray(0.0, 0.0, 0.0)
                var topnode = 0

                Math3D.VectorAdd(start, mins, c1)
                Math3D.VectorAdd(start, maxs, c2)

                run {
                    i = 0
                    while (i < 3) {
                        c1[i] -= 1
                        c2[i] += 1
                        i++
                    }
                }

                val tn = intArray(topnode)

                numleafs = CM_BoxLeafnums_headnode(c1, c2, leafs, 1024, headnode, tn)
                topnode = tn[0]
                run {
                    i = 0
                    while (i < numleafs) {
                        CM_TestInLeaf(leafs[i])
                        if (trace_trace.allsolid)
                            break
                        i++
                    }
                }
                Math3D.VectorCopy(start, trace_trace.endpos)
                return trace_trace
            }

            //
            // check for point special case
            //
            if (mins[0] == 0 && mins[1] == 0 && mins[2] == 0 && maxs[0] == 0 && maxs[1] == 0 && maxs[2] == 0) {
                trace_ispoint = true
                Math3D.VectorClear(trace_extents)
            } else {
                trace_ispoint = false
                trace_extents[0] = if (-mins[0] > maxs[0]) -mins[0] else maxs[0]
                trace_extents[1] = if (-mins[1] > maxs[1]) -mins[1] else maxs[1]
                trace_extents[2] = if (-mins[2] > maxs[2]) -mins[2] else maxs[2]
            }

            //
            // general sweeping through world
            //
            CM_RecursiveHullCheck(headnode, 0, 1, start, end)

            if (trace_trace.fraction == 1) {
                Math3D.VectorCopy(end, trace_trace.endpos)
            } else {
                for (i in 0..3 - 1)
                    trace_trace.endpos[i] = start[i] + trace_trace.fraction * (end[i] - start[i])
            }
            return trace_trace
        }

        /*
     * ================== CM_TransformedBoxTrace
     * 
     * Handles offseting and rotation of the end points for moving and rotating
     * entities ==================
     */
        public fun TransformedBoxTrace(start: FloatArray, end: FloatArray, mins: FloatArray, maxs: FloatArray, headnode: Int, brushmask: Int, origin: FloatArray, angles: FloatArray): trace_t {
            val trace: trace_t
            val start_l = floatArray(0.0, 0.0, 0.0)
            val end_l = floatArray(0.0, 0.0, 0.0)
            val a = floatArray(0.0, 0.0, 0.0)
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            val up = floatArray(0.0, 0.0, 0.0)
            val temp = floatArray(0.0, 0.0, 0.0)
            val rotated: Boolean

            // subtract origin offset
            Math3D.VectorSubtract(start, origin, start_l)
            Math3D.VectorSubtract(end, origin, end_l)

            // rotate start and end into the models frame of reference
            if (headnode != box_headnode && (angles[0] != 0 || angles[1] != 0 || angles[2] != 0))
                rotated = true
            else
                rotated = false

            if (rotated) {
                Math3D.AngleVectors(angles, forward, right, up)

                Math3D.VectorCopy(start_l, temp)
                start_l[0] = Math3D.DotProduct(temp, forward)
                start_l[1] = -Math3D.DotProduct(temp, right)
                start_l[2] = Math3D.DotProduct(temp, up)

                Math3D.VectorCopy(end_l, temp)
                end_l[0] = Math3D.DotProduct(temp, forward)
                end_l[1] = -Math3D.DotProduct(temp, right)
                end_l[2] = Math3D.DotProduct(temp, up)
            }

            // sweep the box through the model
            trace = BoxTrace(start_l, end_l, mins, maxs, headnode, brushmask)

            if (rotated && trace.fraction != 1.0) {
                // FIXME: figure out how to do this with existing angles
                Math3D.VectorNegate(angles, a)
                Math3D.AngleVectors(a, forward, right, up)

                Math3D.VectorCopy(trace.plane.normal, temp)
                trace.plane.normal[0] = Math3D.DotProduct(temp, forward)
                trace.plane.normal[1] = -Math3D.DotProduct(temp, right)
                trace.plane.normal[2] = Math3D.DotProduct(temp, up)
            }

            trace.endpos[0] = start[0] + trace.fraction * (end[0] - start[0])
            trace.endpos[1] = start[1] + trace.fraction * (end[1] - start[1])
            trace.endpos[2] = start[2] + trace.fraction * (end[2] - start[2])

            return trace
        }

        /*
     * ===============================================================================
     * PVS / PHS
     * ===============================================================================
     */

        /*
     * =================== CM_DecompressVis ===================
     */
        public fun CM_DecompressVis(`in`: ByteArray?, offset: Int, out: ByteArray) {
            var c: Int

            var row: Int

            row = (numclusters + 7) shr 3
            var outp = 0
            var inp = offset

            if (`in` == null || numvisibility == 0) {
                // no vis info, so make all
                // visible
                while (row != 0) {
                    out[outp++] = 255.toByte()
                    row--
                }
                return
            }

            do {
                if (`in`[inp] != 0) {
                    out[outp++] = `in`[inp++]
                    continue
                }

                c = `in`[inp + 1] and 255
                inp += 2
                if (outp + c > row) {
                    c = row - (outp)
                    Com.DPrintf("warning: Vis decompression overrun\n")
                }
                while (c != 0) {
                    out[outp++] = 0
                    c--
                }
            } while (outp < row)
        }

        public var pvsrow: ByteArray = ByteArray(Defines.MAX_MAP_LEAFS / 8)

        public var phsrow: ByteArray = ByteArray(Defines.MAX_MAP_LEAFS / 8)

        public fun CM_ClusterPVS(cluster: Int): ByteArray {
            if (cluster == -1)
                Arrays.fill(pvsrow, 0, (numclusters + 7) shr 3, 0.toByte())
            else
                CM_DecompressVis(map_visibility, map_vis.bitofs[cluster][Defines.DVIS_PVS], pvsrow)
            return pvsrow
        }

        public fun CM_ClusterPHS(cluster: Int): ByteArray {
            if (cluster == -1)
                Arrays.fill(phsrow, 0, (numclusters + 7) shr 3, 0.toByte())
            else
                CM_DecompressVis(map_visibility, map_vis.bitofs[cluster][Defines.DVIS_PHS], phsrow)
            return phsrow
        }

        /*
     * ===============================================================================
     * AREAPORTALS
     * ===============================================================================
     */

        public fun FloodArea_r(area: carea_t, floodnum: Int) {
            //Com.Printf("FloodArea_r(" + floodnum + ")...\n");
            var i: Int
            var p: qfiles.dareaportal_t

            if (area.floodvalid == floodvalid) {
                if (area.floodnum == floodnum)
                    return
                Com.Error(Defines.ERR_DROP, "FloodArea_r: reflooded")
            }

            area.floodnum = floodnum
            area.floodvalid = floodvalid

            run {
                i = 0
                while (i < area.numareaportals) {
                    p = map_areaportals[area.firstareaportal + i]
                    if (portalopen[p.portalnum])
                        FloodArea_r(map_areas[p.otherarea], floodnum)
                    i++
                }
            }
        }

        /*
     * ==================== FloodAreaConnections ====================
     */
        public fun FloodAreaConnections() {
            Com.DPrintf("FloodAreaConnections...\n")

            var i: Int
            var area: carea_t
            var floodnum: Int

            // all current floods are now invalid
            floodvalid++
            floodnum = 0

            // area 0 is not used
            run {
                i = 1
                while (i < numareas) {

                    area = map_areas[i]

                    if (area.floodvalid == floodvalid)
                        continue // already flooded into
                    floodnum++
                    FloodArea_r(area, floodnum)
                    i++
                }
            }
        }

        /*
     * ================= CM_SetAreaPortalState =================
     */
        public fun CM_SetAreaPortalState(portalnum: Int, open: Boolean) {
            if (portalnum > numareaportals)
                Com.Error(Defines.ERR_DROP, "areaportal > numareaportals")

            portalopen[portalnum] = open
            FloodAreaConnections()
        }

        /*
     * ================= CM_AreasConnected =================
     */

        public fun CM_AreasConnected(area1: Int, area2: Int): Boolean {
            if (map_noareas.value != 0)
                return true

            if (area1 > numareas || area2 > numareas)
                Com.Error(Defines.ERR_DROP, "area > numareas")

            if (map_areas[area1].floodnum == map_areas[area2].floodnum)
                return true

            return false
        }

        /*
     * ================= CM_WriteAreaBits
     * 
     * Writes a length byte followed by a bit vector of all the areas that area
     * in the same flood as the area parameter
     * 
     * This is used by the client refreshes to cull visibility =================
     */
        public fun CM_WriteAreaBits(buffer: ByteArray, area: Int): Int {
            var i: Int
            val floodnum: Int
            val bytes: Int

            bytes = (numareas + 7) shr 3

            if (map_noareas.value != 0) {
                // for debugging, send everything
                Arrays.fill(buffer, 0, bytes, 255.toByte())
            } else {
                Arrays.fill(buffer, 0, bytes, 0.toByte())
                floodnum = map_areas[area].floodnum
                run {
                    i = 0
                    while (i < numareas) {
                        if (map_areas[i].floodnum == floodnum || area == 0)
                            buffer[i shr 3] = buffer[i shr 3] or (1 shl (i and 7)).toByte()
                        i++
                    }
                }
            }

            return bytes
        }

        /*
     * =================== CM_WritePortalState
     * 
     * Writes the portal state to a savegame file ===================
     */

        public fun CM_WritePortalState(os: RandomAccessFile) {

            //was: fwrite(portalopen, sizeof(portalopen), 1, f);
            try {

                for (n in portalopen.indices)
                    if (portalopen[n])
                        os.writeInt(1)
                    else
                        os.writeInt(0)
            } catch (e: Exception) {
                Com.Printf("ERROR:" + e)
                e.printStackTrace()
            }

        }

        /*
     * =================== CM_ReadPortalState
     * 
     * Reads the portal state from a savegame file and recalculates the area
     * connections ===================
     */
        public fun CM_ReadPortalState(f: RandomAccessFile) {

            //was: FS_Read(portalopen, sizeof(portalopen), f);
            val len = portalopen.size() * 4

            val buf = ByteArray(len)

            FS.Read(buf, len, f)

            val bb = ByteBuffer.wrap(buf)
            val ib = bb.asIntBuffer()

            for (n in portalopen.indices)
                portalopen[n] = ib.get() != 0

            FloodAreaConnections()
        }

        /*
     * ============= CM_HeadnodeVisible
     * 
     * Returns true if any leaf under headnode has a cluster that is potentially
     * visible =============
     */
        public fun CM_HeadnodeVisible(nodenum: Int, visbits: ByteArray): Boolean {
            val leafnum: Int
            val cluster: Int
            val node: cnode_t

            if (nodenum < 0) {
                leafnum = -1 - nodenum
                cluster = map_leafs[leafnum].cluster
                if (cluster == -1)
                    return false
                if (0 != (visbits[cluster.ushr(3)] and (1 shl (cluster and 7))))
                    return true
                return false
            }

            node = map_nodes[nodenum]
            if (CM_HeadnodeVisible(node.children[0], visbits))
                return true
            return CM_HeadnodeVisible(node.children[1], visbits)
        }
    }
}