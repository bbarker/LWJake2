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
import lwjake2.game.cplane_t
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.lump_t
import lwjake2.qcommon.qfiles
import lwjake2.qcommon.texinfo_t
import lwjake2.render.medge_t
import lwjake2.render.mleaf_t
import lwjake2.render.mmodel_t
import lwjake2.render.mnode_t
import lwjake2.render.model_t
import lwjake2.render.msurface_t
import lwjake2.render.mtexinfo_t
import lwjake2.render.mvertex_t
import lwjake2.util.Math3D
import lwjake2.util.Vargs

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.Arrays
import java.util.Vector

import org.lwjgl.BufferUtils

/**
 * Model

 * @author cwei
 */
public abstract class Model : Surf() {

    // models.c -- model loading and caching

    var loadmodel: model_t
    var modfilelen: Int = 0

    var mod_novis = ByteArray(Defines.MAX_MAP_LEAFS / 8)
    var mod_known = arrayOfNulls<model_t>(MAX_MOD_KNOWN)
    var mod_numknown: Int = 0

    // the inline * models from the current map are kept seperate
    var mod_inline = arrayOfNulls<model_t>(MAX_MOD_KNOWN)

    abstract fun GL_SubdivideSurface(surface: msurface_t)  // Warp.java

    /*
	===============
	Mod_PointInLeaf
	===============
	*/
    fun Mod_PointInLeaf(p: FloatArray, model: model_t?): mleaf_t {
        var node: mnode_t
        val d: Float
        val plane: cplane_t

        if (model == null || model!!.nodes == null)
            Com.Error(Defines.ERR_DROP, "Mod_PointInLeaf: bad model")

        node = model!!.nodes[0] // root node
        while (true) {
            if (node.contents != -1)
                return node as mleaf_t

            plane = node.plane
            d = Math3D.DotProduct(p, plane.normal) - plane.dist
            if (d > 0)
                node = node.children[0]
            else
                node = node.children[1]
        }
        // never reached
    }


    var decompressed = ByteArray(Defines.MAX_MAP_LEAFS / 8)
    var model_visibility = ByteArray(Defines.MAX_MAP_VISIBILITY)

    /*
	===================
	Mod_DecompressVis
	===================
	*/
    fun Mod_DecompressVis(`in`: ByteArray?, offset: Int, model: model_t): ByteArray {
        var c: Int
        val out: ByteArray
        var outp: Int
        var inp: Int
        var row: Int

        row = (model.vis.numclusters + 7) shr 3
        out = decompressed
        outp = 0
        inp = offset

        if (`in` == null) {
            // no vis info, so make all visible
            while (row != 0) {
                out[outp++] = 255.toByte()
                row--
            }
            return decompressed
        }

        do {
            if (`in`[inp] != 0) {
                out[outp++] = `in`[inp++]
                continue
            }

            c = `in`[inp + 1] and 255
            inp += 2
            while (c != 0) {
                out[outp++] = 0
                c--
            }
        } while (outp < row)

        return decompressed
    }

    /*
	==============
	Mod_ClusterPVS
	==============
	*/
    fun Mod_ClusterPVS(cluster: Int, model: model_t): ByteArray {
        if (cluster == -1 || model.vis == null)
            return mod_novis
        //return Mod_DecompressVis( (byte *)model.vis + model.vis.bitofs[cluster][Defines.DVIS_PVS], model);
        return Mod_DecompressVis(model_visibility, model.vis.bitofs[cluster][Defines.DVIS_PVS], model)
    }


    //	  ===============================================================================

    /*
	================
	Mod_Modellist_f
	================
	*/
    fun Mod_Modellist_f() {
        var i: Int
        var mod: model_t
        val total: Int

        total = 0
        VID.Printf(Defines.PRINT_ALL, "Loaded models:\n")
        run {
            i = 0
            while (i < mod_numknown) {
                mod = mod_known[i]
                if (mod.name.length() == 0)
                    continue

                VID.Printf(Defines.PRINT_ALL, "%8i : %s\n", Vargs(2).add(mod.extradatasize).add(mod.name))
                total += mod.extradatasize
                i++
            }
        }
        VID.Printf(Defines.PRINT_ALL, "Total resident: " + total + '\n')
    }

    /*
	===============
	Mod_Init
	===============
	*/
    fun Mod_Init() {
        // init mod_known
        for (i in 0..MAX_MOD_KNOWN - 1) {
            mod_known[i] = model_t()
        }
        Arrays.fill(mod_novis, 255.toByte())
    }

    var fileBuffer: ByteArray? = null

    /*
	==================
	Mod_ForName

	Loads in a model for the given name
	==================
	*/
    fun Mod_ForName(name: String?, crash: Boolean): model_t? {
        var mod: model_t? = null
        var i: Int

        if (name == null || name.length() == 0)
            Com.Error(Defines.ERR_DROP, "Mod_ForName: NULL name")

        //
        // inline models are grabbed only from worldmodel
        //
        if (name!!.charAt(0) == '*') {
            i = Integer.parseInt(name.substring(1))
            if (i < 1 || r_worldmodel == null || i >= r_worldmodel.numsubmodels)
                Com.Error(Defines.ERR_DROP, "bad inline model number")
            return mod_inline[i]
        }

        //
        // search the currently loaded models
        //
        run {
            i = 0
            while (i < mod_numknown) {
                mod = mod_known[i]

                if (mod!!.name.length() == 0)
                    continue
                if (mod!!.name.equals(name))
                    return mod
                i++
            }
        }

        //
        // find a free model slot spot
        //
        run {
            i = 0
            while (i < mod_numknown) {
                mod = mod_known[i]

                if (mod!!.name.length() == 0)
                    break    // free spot
                i++
            }
        }
        if (i == mod_numknown) {
            if (mod_numknown == MAX_MOD_KNOWN)
                Com.Error(Defines.ERR_DROP, "mod_numknown == MAX_MOD_KNOWN")
            mod_numknown++
            mod = mod_known[i]
        }

        mod!!.name = name

        //
        // load the file
        //
        fileBuffer = FS.LoadFile(name)

        if (fileBuffer == null) {
            if (crash)
                Com.Error(Defines.ERR_DROP, "Mod_NumForName: " + mod!!.name + " not found")

            mod!!.name = ""
            return null
        }

        modfilelen = fileBuffer!!.size()

        loadmodel = mod

        //
        // fill it in
        //
        val bb = ByteBuffer.wrap(fileBuffer)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        // call the apropriate loader

        bb.mark()
        val ident = bb.getInt()

        bb.reset()

        when (ident) {
            qfiles.IDALIASHEADER -> Mod_LoadAliasModel(mod, bb)
            qfiles.IDSPRITEHEADER -> Mod_LoadSpriteModel(mod, bb)
            qfiles.IDBSPHEADER -> Mod_LoadBrushModel(mod, bb)
            else -> Com.Error(Defines.ERR_DROP, "Mod_NumForName: unknown fileid for " + mod!!.name)
        }

        this.fileBuffer = null // free it for garbage collection
        return mod
    }

    /*
	===============================================================================

						BRUSHMODEL LOADING

	===============================================================================
	*/

    var mod_base: ByteArray


    /*
	=================
	Mod_LoadLighting
	=================
	*/
    fun Mod_LoadLighting(l: lump_t) {
        if (l.filelen == 0) {
            loadmodel.lightdata = null
            return
        }
        // memcpy (loadmodel.lightdata, mod_base + l.fileofs, l.filelen);
        loadmodel.lightdata = ByteArray(l.filelen)
        System.arraycopy(mod_base, l.fileofs, loadmodel.lightdata, 0, l.filelen)
    }


    /*
	=================
	Mod_LoadVisibility
	=================
	*/
    fun Mod_LoadVisibility(l: lump_t) {

        if (l.filelen == 0) {
            loadmodel.vis = null
            return
        }

        System.arraycopy(mod_base, l.fileofs, model_visibility, 0, l.filelen)

        val bb = ByteBuffer.wrap(model_visibility, 0, l.filelen)

        loadmodel.vis = qfiles.dvis_t(bb.order(ByteOrder.LITTLE_ENDIAN))

        /* done:
		memcpy (loadmodel.vis, mod_base + l.fileofs, l.filelen);

		loadmodel.vis.numclusters = LittleLong (loadmodel.vis.numclusters);
		for (i=0 ; i<loadmodel.vis.numclusters ; i++)
		{
			loadmodel.vis.bitofs[i][0] = LittleLong (loadmodel.vis.bitofs[i][0]);
			loadmodel.vis.bitofs[i][1] = LittleLong (loadmodel.vis.bitofs[i][1]);
		}
		*/
    }


    /*
	=================
	Mod_LoadVertexes
	=================
	*/
    fun Mod_LoadVertexes(l: lump_t) {
        val vertexes: Array<mvertex_t>
        var i: Int
        val count: Int

        if ((l.filelen % mvertex_t.DISK_SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / mvertex_t.DISK_SIZE

        vertexes = arrayOfNulls<mvertex_t>(count)

        loadmodel.vertexes = vertexes
        loadmodel.numvertexes = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                vertexes[i] = mvertex_t(bb)
                i++
            }
        }
    }

    /*
	=================
	RadiusFromBounds
	=================
	*/
    fun RadiusFromBounds(mins: FloatArray, maxs: FloatArray): Float {
        val corner = floatArray(0.0, 0.0, 0.0)

        for (i in 0..3 - 1) {
            corner[i] = if (Math.abs(mins[i]) > Math.abs(maxs[i])) Math.abs(mins[i]) else Math.abs(maxs[i])
        }
        return Math3D.VectorLength(corner)
    }


    /*
	=================
	Mod_LoadSubmodels
	=================
	*/
    fun Mod_LoadSubmodels(l: lump_t) {

        if ((l.filelen % qfiles.dmodel_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        var i: Int
        var j: Int

        val count = l.filelen / qfiles.dmodel_t.SIZE
        // out = Hunk_Alloc ( count*sizeof(*out));
        var out: mmodel_t
        val outs = arrayOfNulls<mmodel_t>(count)
        run {
            i = 0
            while (i < count) {
                outs[i] = mmodel_t()
                i++
            }
        }

        loadmodel.submodels = outs
        loadmodel.numsubmodels = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        var `in`: qfiles.dmodel_t

        run {
            i = 0
            while (i < count) {
                `in` = qfiles.dmodel_t(bb)
                out = outs[i]
                run {
                    j = 0
                    while (j < 3) {
                        // spread the mins / maxs by a
                        // pixel
                        out.mins[j] = `in`.mins[j] - 1
                        out.maxs[j] = `in`.maxs[j] + 1
                        out.origin[j] = `in`.origin[j]
                        j++
                    }
                }
                out.radius = RadiusFromBounds(out.mins, out.maxs)
                out.headnode = `in`.headnode
                out.firstface = `in`.firstface
                out.numfaces = `in`.numfaces
                i++
            }
        }
    }

    /*
	=================
	Mod_LoadEdges
	=================
	*/
    fun Mod_LoadEdges(l: lump_t) {
        val edges: Array<medge_t>
        var i: Int
        val count: Int

        if ((l.filelen % medge_t.DISK_SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / medge_t.DISK_SIZE
        // out = Hunk_Alloc ( (count + 1) * sizeof(*out));
        edges = arrayOfNulls<medge_t>(count + 1)

        loadmodel.edges = edges
        loadmodel.numedges = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                edges[i] = medge_t(bb)
                i++
            }
        }
    }

    /*
	=================
	Mod_LoadTexinfo
	=================
	*/
    fun Mod_LoadTexinfo(l: lump_t) {
        var `in`: texinfo_t
        val out: Array<mtexinfo_t>
        var step: mtexinfo_t?
        var i: Int
        val count: Int
        var next: Int
        var name: String

        if ((l.filelen % texinfo_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / texinfo_t.SIZE
        // out = Hunk_Alloc ( count*sizeof(*out));
        out = arrayOfNulls<mtexinfo_t>(count)
        run {
            i = 0
            while (i < count) {
                out[i] = mtexinfo_t()
                i++
            }
        }

        loadmodel.texinfo = out
        loadmodel.numtexinfo = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {

                `in` = texinfo_t(bb)
                out[i].vecs = `in`.vecs
                out[i].flags = `in`.flags
                next = `in`.nexttexinfo
                if (next > 0)
                    out[i].next = loadmodel.texinfo[next]
                else
                    out[i].next = null

                name = "textures/" + `in`.texture + ".wal"

                out[i].image = GL_FindImage(name, it_wall)
                if (out[i].image == null) {
                    VID.Printf(Defines.PRINT_ALL, "Couldn't load " + name + '\n')
                    out[i].image = r_notexture
                }
                i++
            }
        }

        // count animation frames
        run {
            i = 0
            while (i < count) {
                out[i].numframes = 1
                run {
                    step = out[i].next
                    while ((step != null) && (step != out[i])) {
                        out[i].numframes++
                        step = step!!.next
                    }
                }
                i++
            }
        }
    }

    /*
	================
	CalcSurfaceExtents

	Fills in s.texturemins[] and s.extents[]
	================
	*/
    fun CalcSurfaceExtents(s: msurface_t) {
        val mins = floatArray(0.0, 0.0)
        val maxs = floatArray(0.0, 0.0)
        var `val`: Float

        var j: Int
        val e: Int
        val v: mvertex_t
        val bmins = intArray(0, 0)
        val bmaxs = intArray(0, 0)

        mins[0] = mins[1] = 999999
        maxs[0] = maxs[1] = (-99999).toFloat()

        val tex = s.texinfo

        for (i in 0..s.numedges - 1) {
            e = loadmodel.surfedges[s.firstedge + i]
            if (e >= 0)
                v = loadmodel.vertexes[loadmodel.edges[e].v[0]]
            else
                v = loadmodel.vertexes[loadmodel.edges[-e].v[1]]

            run {
                j = 0
                while (j < 2) {
                    `val` = v.position[0] * tex.vecs[j][0] + v.position[1] * tex.vecs[j][1] + v.position[2] * tex.vecs[j][2] + tex.vecs[j][3]
                    if (`val` < mins[j])
                        mins[j] = `val`
                    if (`val` > maxs[j])
                        maxs[j] = `val`
                    j++
                }
            }
        }

        for (i in 0..2 - 1) {
            bmins[i] = Math.floor(mins[i] / 16) as Int
            bmaxs[i] = Math.ceil(maxs[i] / 16) as Int

            s.texturemins[i] = (bmins[i] * 16).toShort()
            s.extents[i] = ((bmaxs[i] - bmins[i]) * 16).toShort()

        }
    }

    /*
	=================
	Mod_LoadFaces
	=================
	*/
    fun Mod_LoadFaces(l: lump_t) {

        var i: Int
        var surfnum: Int
        var planenum: Int
        var side: Int
        var ti: Int

        if ((l.filelen % qfiles.dface_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        val count = l.filelen / qfiles.dface_t.SIZE
        // out = Hunk_Alloc ( count*sizeof(*out));
        val outs = arrayOfNulls<msurface_t>(count)
        run {
            i = 0
            while (i < count) {
                outs[i] = msurface_t()
                i++
            }
        }

        loadmodel.surfaces = outs
        loadmodel.numsurfaces = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        currentmodel = loadmodel

        GL_BeginBuildingLightmaps(loadmodel)

        var `in`: qfiles.dface_t
        var out: msurface_t

        run {
            surfnum = 0
            while (surfnum < count) {
                `in` = qfiles.dface_t(bb)
                out = outs[surfnum]
                out.firstedge = `in`.firstedge
                out.numedges = `in`.numedges
                out.flags = 0
                out.polys = null

                planenum = `in`.planenum
                side = `in`.side
                if (side != 0)
                    out.flags = out.flags or Defines.SURF_PLANEBACK

                out.plane = loadmodel.planes[planenum]

                ti = `in`.texinfo
                if (ti < 0 || ti >= loadmodel.numtexinfo)
                    Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: bad texinfo number")

                out.texinfo = loadmodel.texinfo[ti]

                CalcSurfaceExtents(out)

                // lighting info

                run {
                    i = 0
                    while (i < Defines.MAXLIGHTMAPS) {
                        out.styles[i] = `in`.styles[i]
                        i++
                    }
                }

                i = `in`.lightofs
                if (i == -1)
                    out.samples = null
                else {
                    var pointer = ByteBuffer.wrap(loadmodel.lightdata)
                    pointer.position(i)
                    pointer = pointer.slice()
                    pointer.mark()
                    out.samples = pointer // subarray
                }

                // set the drawing flags

                if ((out.texinfo.flags and Defines.SURF_WARP) != 0) {
                    out.flags = out.flags or Defines.SURF_DRAWTURB
                    run {
                        i = 0
                        while (i < 2) {
                            out.extents[i] = 16384
                            out.texturemins[i] = -8192
                            i++
                        }
                    }
                    GL_SubdivideSurface(out) // cut up polygon for warps
                }

                // create lightmaps and polygons
                if ((out.texinfo.flags and (Defines.SURF_SKY or Defines.SURF_TRANS33 or Defines.SURF_TRANS66 or Defines.SURF_WARP)) == 0)
                    GL_CreateSurfaceLightmap(out)

                if ((out.texinfo.flags and Defines.SURF_WARP) == 0)
                    GL_BuildPolygonFromSurface(out)
                surfnum++

            }
        }
        GL_EndBuildingLightmaps()
    }


    /*
	=================
	Mod_SetParent
	=================
	*/
    fun Mod_SetParent(node: mnode_t, parent: mnode_t?) {
        node.parent = parent
        if (node.contents != -1) return
        Mod_SetParent(node.children[0], node)
        Mod_SetParent(node.children[1], node)
    }

    /*
	=================
	Mod_LoadNodes
	=================
	*/
    fun Mod_LoadNodes(l: lump_t) {
        var i: Int
        var j: Int
        val count: Int
        var p: Int
        var `in`: qfiles.dnode_t
        val out: Array<mnode_t>

        if ((l.filelen % qfiles.dnode_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / qfiles.dnode_t.SIZE
        // out = Hunk_Alloc ( count*sizeof(*out));
        out = arrayOfNulls<mnode_t>(count)

        loadmodel.nodes = out
        loadmodel.numnodes = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        // initialize the tree array
        run {
            i = 0
            while (i < count) {
                out[i] = mnode_t()
                i++
            }
        } // do first before linking

        // fill and link the nodes
        run {
            i = 0
            while (i < count) {
                `in` = qfiles.dnode_t(bb)
                run {
                    j = 0
                    while (j < 3) {
                        out[i].mins[j] = `in`.mins[j]
                        out[i].maxs[j] = `in`.maxs[j]
                        j++
                    }
                }

                p = `in`.planenum
                out[i].plane = loadmodel.planes[p]

                out[i].firstsurface = `in`.firstface
                out[i].numsurfaces = `in`.numfaces
                out[i].contents = -1    // differentiate from leafs

                run {
                    j = 0
                    while (j < 2) {
                        p = `in`.children[j]
                        if (p >= 0)
                            out[i].children[j] = loadmodel.nodes[p]
                        else
                            out[i].children[j] = loadmodel.leafs[-1 - p] // mleaf_t extends mnode_t
                        j++
                    }
                }
                i++
            }
        }

        Mod_SetParent(loadmodel.nodes[0], null)    // sets nodes and leafs
    }

    /*
	=================
	Mod_LoadLeafs
	=================
	*/
    fun Mod_LoadLeafs(l: lump_t) {
        var `in`: qfiles.dleaf_t
        val out: Array<mleaf_t>
        var i: Int
        var j: Int
        val count: Int

        if ((l.filelen % qfiles.dleaf_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / qfiles.dleaf_t.SIZE
        // out = Hunk_Alloc ( count*sizeof(*out));
        out = arrayOfNulls<mleaf_t>(count)

        loadmodel.leafs = out
        loadmodel.numleafs = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                `in` = qfiles.dleaf_t(bb)
                out[i] = mleaf_t()
                run {
                    j = 0
                    while (j < 3) {
                        out[i].mins[j] = `in`.mins[j]
                        out[i].maxs[j] = `in`.maxs[j]
                        j++

                    }
                }

                out[i].contents = `in`.contents
                out[i].cluster = `in`.cluster
                out[i].area = `in`.area

                out[i].setMarkSurface(`in`.firstleafface, loadmodel.marksurfaces)
                out[i].nummarksurfaces = `in`.numleaffaces
                i++
            }
        }
    }


    /*
	=================
	Mod_LoadMarksurfaces
	=================
	*/
    fun Mod_LoadMarksurfaces(l: lump_t) {
        var i: Int
        var j: Int
        val count: Int

        val out: Array<msurface_t>

        if ((l.filelen % Defines.SIZE_OF_SHORT) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)
        count = l.filelen / Defines.SIZE_OF_SHORT
        // out = Hunk_Alloc ( count*sizeof(*out));
        out = arrayOfNulls<msurface_t>(count)

        loadmodel.marksurfaces = out
        loadmodel.nummarksurfaces = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                j = bb.getShort().toInt()
                if (j < 0 || j >= loadmodel.numsurfaces)
                    Com.Error(Defines.ERR_DROP, "Mod_ParseMarksurfaces: bad surface number")

                out[i] = loadmodel.surfaces[j]
                i++
            }
        }
    }


    /*
	=================
	Mod_LoadSurfedges
	=================
	*/
    fun Mod_LoadSurfedges(l: lump_t) {
        var i: Int
        val count: Int
        val offsets: IntArray

        if ((l.filelen % Defines.SIZE_OF_INT) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / Defines.SIZE_OF_INT
        if (count < 1 || count >= Defines.MAX_MAP_SURFEDGES)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: bad surfedges count in " + loadmodel.name + ": " + count)

        offsets = IntArray(count)

        loadmodel.surfedges = offsets
        loadmodel.numsurfedges = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                offsets[i] = bb.getInt()
                i++
            }
        }
    }


    /*
	=================
	Mod_LoadPlanes
	=================
	*/
    fun Mod_LoadPlanes(l: lump_t) {
        var i: Int
        var j: Int
        val out: Array<cplane_t>
        var `in`: qfiles.dplane_t
        val count: Int
        var bits: Int

        if ((l.filelen % qfiles.dplane_t.SIZE) != 0)
            Com.Error(Defines.ERR_DROP, "MOD_LoadBmodel: funny lump size in " + loadmodel.name)

        count = l.filelen / qfiles.dplane_t.SIZE
        // out = Hunk_Alloc ( count*2*sizeof(*out));
        out = arrayOfNulls<cplane_t>(count * 2)
        run {
            i = 0
            while (i < count) {
                out[i] = cplane_t()
                i++
            }
        }

        loadmodel.planes = out
        loadmodel.numplanes = count

        val bb = ByteBuffer.wrap(mod_base, l.fileofs, l.filelen)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        run {
            i = 0
            while (i < count) {
                bits = 0
                `in` = qfiles.dplane_t(bb)
                run {
                    j = 0
                    while (j < 3) {
                        out[i].normal[j] = `in`.normal[j]
                        if (out[i].normal[j] < 0)
                            bits = bits or (1 shl j)
                        j++
                    }
                }

                out[i].dist = `in`.dist
                out[i].type = `in`.type as Byte
                out[i].signbits = bits.toByte()
                i++
            }
        }
    }

    /*
	=================
	Mod_LoadBrushModel
	=================
	*/
    fun Mod_LoadBrushModel(mod: model_t, buffer: ByteBuffer) {
        var i: Int
        val header: qfiles.dheader_t
        var bm: mmodel_t

        loadmodel.type = mod_brush
        if (loadmodel != mod_known[0])
            Com.Error(Defines.ERR_DROP, "Loaded a brush model after the world")

        header = qfiles.dheader_t(buffer)

        i = header.version
        if (i != Defines.BSPVERSION)
            Com.Error(Defines.ERR_DROP, "Mod_LoadBrushModel: " + mod.name + " has wrong version number (" + i + " should be " + Defines.BSPVERSION + ")")

        mod_base = fileBuffer //(byte *)header;

        // load into heap
        Mod_LoadVertexes(header.lumps[Defines.LUMP_VERTEXES]) // ok
        Mod_LoadEdges(header.lumps[Defines.LUMP_EDGES]) // ok
        Mod_LoadSurfedges(header.lumps[Defines.LUMP_SURFEDGES]) // ok
        Mod_LoadLighting(header.lumps[Defines.LUMP_LIGHTING]) // ok
        Mod_LoadPlanes(header.lumps[Defines.LUMP_PLANES]) // ok
        Mod_LoadTexinfo(header.lumps[Defines.LUMP_TEXINFO]) // ok
        Mod_LoadFaces(header.lumps[Defines.LUMP_FACES]) // ok
        Mod_LoadMarksurfaces(header.lumps[Defines.LUMP_LEAFFACES])
        Mod_LoadVisibility(header.lumps[Defines.LUMP_VISIBILITY]) // ok
        Mod_LoadLeafs(header.lumps[Defines.LUMP_LEAFS]) // ok
        Mod_LoadNodes(header.lumps[Defines.LUMP_NODES]) // ok
        Mod_LoadSubmodels(header.lumps[Defines.LUMP_MODELS])
        mod.numframes = 2        // regular and alternate animation

        //
        // set up the submodels
        //
        val starmod: model_t

        run {
            i = 0
            while (i < mod.numsubmodels) {

                bm = mod.submodels[i]
                starmod = mod_inline[i] = loadmodel.copy()

                starmod.firstmodelsurface = bm.firstface
                starmod.nummodelsurfaces = bm.numfaces
                starmod.firstnode = bm.headnode
                if (starmod.firstnode >= loadmodel.numnodes)
                    Com.Error(Defines.ERR_DROP, "Inline model " + i + " has bad firstnode")

                Math3D.VectorCopy(bm.maxs, starmod.maxs)
                Math3D.VectorCopy(bm.mins, starmod.mins)
                starmod.radius = bm.radius

                if (i == 0)
                    loadmodel = starmod.copy()

                starmod.numleafs = bm.visleafs
                i++
            }
        }
    }

    /*
	==============================================================================

	ALIAS MODELS

	==============================================================================
	*/

    /*
	=================
	Mod_LoadAliasModel
	=================
	*/
    fun Mod_LoadAliasModel(mod: model_t, buffer: ByteBuffer) {
        var i: Int
        val pheader: qfiles.dmdl_t
        val poutst: Array<qfiles.dstvert_t>
        val pouttri: Array<qfiles.dtriangle_t>
        val poutframe: Array<qfiles.daliasframe_t>
        val poutcmd: IntArray

        pheader = qfiles.dmdl_t(buffer)

        if (pheader.version != qfiles.ALIAS_VERSION)
            Com.Error(Defines.ERR_DROP, "%s has wrong version number (%i should be %i)", Vargs(3).add(mod.name).add(pheader.version).add(qfiles.ALIAS_VERSION))

        if (pheader.skinheight > MAX_LBM_HEIGHT)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has a skin taller than " + MAX_LBM_HEIGHT)

        if (pheader.num_xyz <= 0)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has no vertices")

        if (pheader.num_xyz > qfiles.MAX_VERTS)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has too many vertices")

        if (pheader.num_st <= 0)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has no st vertices")

        if (pheader.num_tris <= 0)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has no triangles")

        if (pheader.num_frames <= 0)
            Com.Error(Defines.ERR_DROP, "model " + mod.name + " has no frames")

        //
        // load base s and t vertices (not used in gl version)
        //
        poutst = arrayOfNulls<qfiles.dstvert_t>(pheader.num_st)
        buffer.position(pheader.ofs_st)
        run {
            i = 0
            while (i < pheader.num_st) {
                poutst[i] = qfiles.dstvert_t(buffer)
                i++
            }
        }

        //
        //	   load triangle lists
        //
        pouttri = arrayOfNulls<qfiles.dtriangle_t>(pheader.num_tris)
        buffer.position(pheader.ofs_tris)
        run {
            i = 0
            while (i < pheader.num_tris) {
                pouttri[i] = qfiles.dtriangle_t(buffer)
                i++
            }
        }

        //
        //	   load the frames
        //
        poutframe = arrayOfNulls<qfiles.daliasframe_t>(pheader.num_frames)
        buffer.position(pheader.ofs_frames)
        run {
            i = 0
            while (i < pheader.num_frames) {
                poutframe[i] = qfiles.daliasframe_t(buffer)
                // verts are all 8 bit, so no swapping needed
                poutframe[i].verts = IntArray(pheader.num_xyz)
                for (k in 0..pheader.num_xyz - 1) {
                    poutframe[i].verts[k] = buffer.getInt()
                }
                i++
            }
        }

        mod.type = mod_alias

        //
        // load the glcmds
        //
        poutcmd = IntArray(pheader.num_glcmds)
        buffer.position(pheader.ofs_glcmds)
        run {
            i = 0
            while (i < pheader.num_glcmds) {
                poutcmd[i] = buffer.getInt()
                i++
            }
        } // LittleLong (pincmd[i]);

        // register all skins
        val skinNames = arrayOfNulls<String>(pheader.num_skins)
        val nameBuf = ByteArray(qfiles.MAX_SKINNAME)
        buffer.position(pheader.ofs_skins)
        run {
            i = 0
            while (i < pheader.num_skins) {
                buffer.get(nameBuf)
                skinNames[i] = String(nameBuf)
                val n = skinNames[i].indexOf('\0')
                if (n > -1) {
                    skinNames[i] = skinNames[i].substring(0, n)
                }
                mod.skins[i] = GL_FindImage(skinNames[i], it_skin)
                i++
            }
        }

        // set the model arrays
        pheader.skinNames = skinNames // skin names
        pheader.stVerts = poutst // textur koordinaten
        pheader.triAngles = pouttri // dreiecke
        pheader.glCmds = poutcmd // STRIP or FAN
        pheader.aliasFrames = poutframe // frames mit vertex array

        mod.extradata = pheader

        mod.mins[0] = -32
        mod.mins[1] = -32
        mod.mins[2] = -32
        mod.maxs[0] = 32
        mod.maxs[1] = 32
        mod.maxs[2] = 32

        precompileGLCmds(pheader)
    }

    /*
	==============================================================================

	SPRITE MODELS

	==============================================================================
	*/

    /*
	=================
	Mod_LoadSpriteModel
	=================
	*/
    fun Mod_LoadSpriteModel(mod: model_t, buffer: ByteBuffer) {
        val sprout = qfiles.dsprite_t(buffer)

        if (sprout.version != qfiles.SPRITE_VERSION)
            Com.Error(Defines.ERR_DROP, "%s has wrong version number (%i should be %i)", Vargs(3).add(mod.name).add(sprout.version).add(qfiles.SPRITE_VERSION))

        if (sprout.numframes > qfiles.MAX_MD2SKINS)
            Com.Error(Defines.ERR_DROP, "%s has too many frames (%i > %i)", Vargs(3).add(mod.name).add(sprout.numframes).add(qfiles.MAX_MD2SKINS))

        for (i in 0..sprout.numframes - 1) {
            mod.skins[i] = GL_FindImage(sprout.frames[i].name, it_sprite)
        }

        mod.type = mod_sprite
        mod.extradata = sprout
    }

    //	  =============================================================================

    /*
	@@@@@@@@@@@@@@@@@@@@@
	R_BeginRegistration

	Specifies the model that will be used as the world
	@@@@@@@@@@@@@@@@@@@@@
	*/
    protected fun R_BeginRegistration(model: String) {
        resetModelArrays()
        Polygon.reset()

        val flushmap: cvar_t

        registration_sequence++
        r_oldviewcluster = -1        // force markleafs

        val fullname = "maps/" + model + ".bsp"

        // explicitly free the old map if different
        // this guarantees that mod_known[0] is the world map
        flushmap = Cvar.Get("flushmap", "0", 0)
        if (!mod_known[0].name.equals(fullname) || flushmap.value != 0.0.toFloat())
            Mod_Free(mod_known[0])
        r_worldmodel = Mod_ForName(fullname, true)

        r_viewcluster = -1
    }


    /*
	@@@@@@@@@@@@@@@@@@@@@
	R_RegisterModel

	@@@@@@@@@@@@@@@@@@@@@
	*/
    protected fun R_RegisterModel(name: String): model_t {
        var mod: model_t? = null
        var i: Int
        val sprout: qfiles.dsprite_t
        val pheader: qfiles.dmdl_t

        mod = Mod_ForName(name, false)
        if (mod != null) {
            mod!!.registration_sequence = registration_sequence

            // register any images used by the models
            if (mod!!.type == mod_sprite) {
                sprout = mod!!.extradata as qfiles.dsprite_t
                run {
                    i = 0
                    while (i < sprout.numframes) {
                        mod!!.skins[i] = GL_FindImage(sprout.frames[i].name, it_sprite)
                        i++
                    }
                }
            } else if (mod!!.type == mod_alias) {
                pheader = mod!!.extradata as qfiles.dmdl_t
                run {
                    i = 0
                    while (i < pheader.num_skins) {
                        mod!!.skins[i] = GL_FindImage(pheader.skinNames[i], it_skin)
                        i++
                    }
                }
                // PGM
                mod!!.numframes = pheader.num_frames
                // PGM
            } else if (mod!!.type == mod_brush) {
                run {
                    i = 0
                    while (i < mod!!.numtexinfo) {
                        mod!!.texinfo[i].image.registration_sequence = registration_sequence
                        i++
                    }
                }
            }
        }
        return mod
    }


    /*
	@@@@@@@@@@@@@@@@@@@@@
	R_EndRegistration

	@@@@@@@@@@@@@@@@@@@@@
	*/
    protected fun R_EndRegistration() {
        val mod: model_t

        for (i in 0..mod_numknown - 1) {
            mod = mod_known[i]
            if (mod.name.length() == 0)
                continue
            if (mod.registration_sequence != registration_sequence) {
                // don't need this model
                Mod_Free(mod)
            } else {
                // precompile AliasModels
                if (mod.type == mod_alias)
                    precompileGLCmds(mod.extradata as qfiles.dmdl_t)
            }
        }
        GL_FreeUnusedImages()
        //modelMemoryUsage();
    }


    //	  =============================================================================


    /*
	================
	Mod_Free
	================
	*/
    fun Mod_Free(mod: model_t) {
        mod.clear()
    }

    /*
	================
	Mod_FreeAll
	================
	*/
    fun Mod_FreeAll() {
        for (i in 0..mod_numknown - 1) {
            if (mod_known[i].extradata != null)
                Mod_Free(mod_known[i])
        }
    }

    fun precompileGLCmds(model: qfiles.dmdl_t) {
        model.textureCoordBuf = globalModelTextureCoordBuf.slice()
        model.vertexIndexBuf = globalModelVertexIndexBuf.slice()
        val tmp = Vector<Integer>()

        var count = 0
        val order = model.glCmds
        var orderIndex = 0
        while (true) {
            // get the vertex count and primitive type
            count = order[orderIndex++]
            if (count == 0)
                break        // done

            tmp.addElement(Integer(count))

            if (count < 0) {
                count = -count
                //gl.glBegin (GL.GL_TRIANGLE_FAN);
            } else {
                //gl.glBegin (GL.GL_TRIANGLE_STRIP);
            }

            do {
                // texture coordinates come from the draw list
                globalModelTextureCoordBuf.put(Float.intBitsToFloat(order[orderIndex + 0]))
                globalModelTextureCoordBuf.put(Float.intBitsToFloat(order[orderIndex + 1]))
                globalModelVertexIndexBuf.put(order[orderIndex + 2])

                orderIndex += 3
            } while (--count != 0)
        }

        val size = tmp.size()

        model.counts = IntArray(size)
        model.indexElements = arrayOfNulls<IntBuffer>(size)

        count = 0
        var pos = 0
        for (i in 0..model.counts.length - 1) {
            count = (tmp.get(i) as Integer).intValue()
            model.counts[i] = count

            count = if ((count < 0)) -count else count
            model.vertexIndexBuf.position(pos)
            model.indexElements[i] = model.vertexIndexBuf.slice()
            model.indexElements[i].limit(count)
            pos += count
        }
    }

    companion object {

        val MAX_MOD_KNOWN = 512

        /*
	 * new functions for vertex array handling
	 */
        val MODEL_BUFFER_SIZE = 50000
        var globalModelTextureCoordBuf = BufferUtils.createFloatBuffer(MODEL_BUFFER_SIZE * 2)
        var globalModelVertexIndexBuf = BufferUtils.createIntBuffer(MODEL_BUFFER_SIZE)

        fun resetModelArrays() {
            globalModelTextureCoordBuf.rewind()
            globalModelVertexIndexBuf.rewind()
        }

        fun modelMemoryUsage() {
            System.out.println("AliasModels: globalVertexBuffer size " + globalModelVertexIndexBuf.position())
        }
    }
}
