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

package lwjake2.render

import lwjake2.Defines
import lwjake2.game.cplane_t
import lwjake2.qcommon.qfiles
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.util.Arrays

public class model_t : Cloneable {

    public var name: String = ""

    public var registration_sequence: Int = 0

    // was enum modtype_t
    public var type: Int = 0
    public var numframes: Int = 0

    public var flags: Int = 0

    //
    // volume occupied by the model graphics
    //
    public var mins: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var maxs: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var radius: Float = 0.toFloat()

    //
    // solid volume for clipping
    //
    public var clipbox: Boolean = false
    public var clipmins: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var clipmaxs: FloatArray = floatArray(0.0, 0.0, 0.0)

    //
    // brush model
    //
    public var firstmodelsurface: Int = 0
    public var nummodelsurfaces: Int = 0
    public var lightmap: Int = 0 // only for submodels

    public var numsubmodels: Int = 0
    public var submodels: Array<mmodel_t>? = null

    public var numplanes: Int = 0
    public var planes: Array<cplane_t>? = null

    public var numleafs: Int = 0 // number of visible leafs, not counting 0
    public var leafs: Array<mleaf_t>? = null

    public var numvertexes: Int = 0
    public var vertexes: Array<mvertex_t>? = null

    public var numedges: Int = 0
    public var edges: Array<medge_t>? = null

    public var numnodes: Int = 0
    public var firstnode: Int = 0
    public var nodes: Array<mnode_t>? = null

    public var numtexinfo: Int = 0
    public var texinfo: Array<mtexinfo_t>? = null

    public var numsurfaces: Int = 0
    public var surfaces: Array<msurface_t>? = null

    public var numsurfedges: Int = 0
    public var surfedges: IntArray? = null

    public var nummarksurfaces: Int = 0
    public var marksurfaces: Array<msurface_t>? = null

    public var vis: qfiles.dvis_t? = null

    public var lightdata: ByteArray? = null

    // for alias models and skins
    // was image_t *skins[]; (array of pointers)
    public var skins: Array<image_t> = arrayOfNulls<image_t>(Defines.MAX_MD2SKINS)

    public var extradatasize: Int = 0

    // or whatever
    public var extradata: Object? = null

    public fun clear() {
        name = ""
        registration_sequence = 0

        // was enum modtype_t
        type = 0
        numframes = 0
        flags = 0

        //
        // volume occupied by the model graphics
        //
        Math3D.VectorClear(mins)
        Math3D.VectorClear(maxs)
        radius = 0

        //
        // solid volume for clipping
        //
        clipbox = false
        Math3D.VectorClear(clipmins)
        Math3D.VectorClear(clipmaxs)

        //
        // brush model
        //
        firstmodelsurface = nummodelsurfaces = 0
        lightmap = 0 // only for submodels

        numsubmodels = 0
        submodels = null

        numplanes = 0
        planes = null

        numleafs = 0 // number of visible leafs, not counting 0
        leafs = null

        numvertexes = 0
        vertexes = null

        numedges = 0
        edges = null

        numnodes = 0
        firstnode = 0
        nodes = null

        numtexinfo = 0
        texinfo = null

        numsurfaces = 0
        surfaces = null

        numsurfedges = 0
        surfedges = null

        nummarksurfaces = 0
        marksurfaces = null

        vis = null

        lightdata = null

        // for alias models and skins
        // was image_t *skins[]; (array of pointers)
        Arrays.fill(skins, null)

        extradatasize = 0
        // or whatever
        extradata = null
    }

    // TODO replace with set(model_t from)
    public fun copy(): model_t {
        var theClone: model_t? = null
        try {
            theClone = super.clone() as model_t
            theClone!!.mins = Lib.clone(this.mins)
            theClone!!.maxs = Lib.clone(this.maxs)
            theClone!!.clipmins = Lib.clone(this.clipmins)
            theClone!!.clipmaxs = Lib.clone(this.clipmaxs)

        } catch (e: CloneNotSupportedException) {
        }

        return theClone
    }
}
