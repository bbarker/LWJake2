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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * qfiles

 * @author cwei
 */
public class qfiles {
    //
    // qfiles.h: quake file formats
    // This file must be identical in the quake and utils directories
    //

    /*
	========================================================================
	
	The .pak files are just a linear collapse of a directory tree
	
	========================================================================
	*/

    /*
	========================================================================
	
	PCX files are used for as many images as possible
	
	========================================================================
	*/
    public class pcx_t(b: ByteBuffer) {

        public var manufacturer: Byte = 0
        public var version: Byte = 0
        public var encoding: Byte = 0
        public var bits_per_pixel: Byte = 0
        public var xmin: Int = 0
        public var ymin: Int = 0
        public var xmax: Int = 0
        public var ymax: Int = 0 // unsigned short
        public var hres: Int = 0
        public var vres: Int = 0 // unsigned short
        public var palette: ByteArray //unsigned byte; size 48
        public var reserved: Byte = 0
        public var color_planes: Byte = 0
        public var bytes_per_line: Int = 0 // unsigned short
        public var palette_type: Int = 0 // unsigned short
        public var filler: ByteArray // size 58
        public var data: ByteBuffer //unbounded data

        public constructor(dataBytes: ByteArray) : this(ByteBuffer.wrap(dataBytes)) {
        }

        {
            // is stored as little endian
            b.order(ByteOrder.LITTLE_ENDIAN)

            // fill header
            manufacturer = b.get()
            version = b.get()
            encoding = b.get()
            bits_per_pixel = b.get()
            xmin = b.getShort() and 65535
            ymin = b.getShort() and 65535
            xmax = b.getShort() and 65535
            ymax = b.getShort() and 65535
            hres = b.getShort() and 65535
            vres = b.getShort() and 65535
            b.get(palette = ByteArray(PALETTE_SIZE))
            reserved = b.get()
            color_planes = b.get()
            bytes_per_line = b.getShort() and 65535
            palette_type = b.getShort() and 65535
            b.get(filler = ByteArray(FILLER_SIZE))

            // fill data
            data = b.slice()
        }

        companion object {

            // size of byte arrays
            val PALETTE_SIZE = 48
            val FILLER_SIZE = 58
        }
    }

    /*
	========================================================================
	
	TGA files are used for sky planes
	
	========================================================================
	*/
    public class tga_t(b: ByteBuffer) {

        // targa header
        public var id_length: Int = 0
        public var colormap_type: Int = 0
        public var image_type: Int = 0 // unsigned char
        public var colormap_index: Int = 0
        public var colormap_length: Int = 0 // unsigned short
        public var colormap_size: Int = 0 // unsigned char
        public var x_origin: Int = 0
        public var y_origin: Int = 0
        public var width: Int = 0
        public var height: Int = 0 // unsigned short
        public var pixel_size: Int = 0
        public var attributes: Int = 0 // unsigned char

        public var data: ByteBuffer // (un)compressed data

        public constructor(dataBytes: ByteArray) : this(ByteBuffer.wrap(dataBytes)) {
        }

        {
            // is stored as little endian
            b.order(ByteOrder.LITTLE_ENDIAN)

            // fill header
            id_length = b.get() and 255
            colormap_type = b.get() and 255
            image_type = b.get() and 255
            colormap_index = b.getShort() and 65535
            colormap_length = b.getShort() and 65535
            colormap_size = b.get() and 255
            x_origin = b.getShort() and 65535
            y_origin = b.getShort() and 65535
            width = b.getShort() and 65535
            height = b.getShort() and 65535
            pixel_size = b.get() and 255
            attributes = b.get() and 255

            // fill data
            data = b.slice()
        }

    }

    public class dstvert_t(b: ByteBuffer) {
        public var s: Short = 0
        public var t: Short = 0

        {
            s = b.getShort()
            t = b.getShort()
        }
    }

    public class dtriangle_t(b: ByteBuffer) {
        public var index_xyz: ShortArray = shortArray(0, 0, 0)
        public var index_st: ShortArray = shortArray(0, 0, 0)

        {
            index_xyz[0] = b.getShort()
            index_xyz[1] = b.getShort()
            index_xyz[2] = b.getShort()

            index_st[0] = b.getShort()
            index_st[1] = b.getShort()
            index_st[2] = b.getShort()
        }
    }

    public class daliasframe_t(b: ByteBuffer) {
        public var scale: FloatArray = floatArray(0.0, 0.0, 0.0) // multiply byte verts by this
        public var translate: FloatArray = floatArray(0.0, 0.0, 0.0)    // then add this
        public var name: String // frame name from grabbing (size 16)
        public var verts: IntArray    // variable sized

        {
            scale[0] = b.getFloat()
            scale[1] = b.getFloat()
            scale[2] = b.getFloat()
            translate[0] = b.getFloat()
            translate[1] = b.getFloat()
            translate[2] = b.getFloat()
            val nameBuf = ByteArray(16)
            b.get(nameBuf)
            name = String(nameBuf).trim()
        }
    }

    //	   the glcmd format:
    //	   a positive integer starts a tristrip command, followed by that many
    //	   vertex structures.
    //	   a negative integer starts a trifan command, followed by -x vertexes
    //	   a zero indicates the end of the command list.
    //	   a vertex consists of a floating point s, a floating point t,
    //	   and an integer vertex index.

    public class dmdl_t(b: ByteBuffer) {
        public var ident: Int = 0
        public var version: Int = 0

        public var skinwidth: Int = 0
        public var skinheight: Int = 0
        public var framesize: Int = 0 // byte size of each frame

        public var num_skins: Int = 0
        public var num_xyz: Int = 0
        public var num_st: Int = 0 // greater than num_xyz for seams
        public var num_tris: Int = 0
        public var num_glcmds: Int = 0 // dwords in strip/fan command list
        public var num_frames: Int = 0

        public var ofs_skins: Int = 0 // each skin is a MAX_SKINNAME string
        public var ofs_st: Int = 0 // byte offset from start for stverts
        public var ofs_tris: Int = 0 // offset for dtriangles
        public var ofs_frames: Int = 0 // offset for first frame
        public var ofs_glcmds: Int = 0
        public var ofs_end: Int = 0 // end of file

        // wird extra gebraucht
        public var skinNames: Array<String>
        public var stVerts: Array<dstvert_t>
        public var triAngles: Array<dtriangle_t>
        public var glCmds: IntArray
        public var aliasFrames: Array<daliasframe_t>


        {
            ident = b.getInt()
            version = b.getInt()

            skinwidth = b.getInt()
            skinheight = b.getInt()
            framesize = b.getInt() // byte size of each frame

            num_skins = b.getInt()
            num_xyz = b.getInt()
            num_st = b.getInt() // greater than num_xyz for seams
            num_tris = b.getInt()
            num_glcmds = b.getInt() // dwords in strip/fan command list
            num_frames = b.getInt()

            ofs_skins = b.getInt() // each skin is a MAX_SKINNAME string
            ofs_st = b.getInt() // byte offset from start for stverts
            ofs_tris = b.getInt() // offset for dtriangles
            ofs_frames = b.getInt() // offset for first frame
            ofs_glcmds = b.getInt()
            ofs_end = b.getInt() // end of file
        }

        /*
		 * new members for vertex array handling
		 */
        public var textureCoordBuf: FloatBuffer? = null
        public var vertexIndexBuf: IntBuffer? = null
        public var counts: IntArray? = null
        public var indexElements: Array<IntBuffer>? = null
    }

    public class dsprframe_t(b: ByteBuffer) {
        public var width: Int = 0
        public var height: Int = 0
        public var origin_x: Int = 0
        public var origin_y: Int = 0 // raster coordinates inside pic
        public var name: String // name of pcx file (MAX_SKINNAME)

        {
            width = b.getInt()
            height = b.getInt()
            origin_x = b.getInt()
            origin_y = b.getInt()

            val nameBuf = ByteArray(MAX_SKINNAME)
            b.get(nameBuf)
            name = String(nameBuf).trim()
        }
    }

    public class dsprite_t(b: ByteBuffer) {
        public var ident: Int = 0
        public var version: Int = 0
        public var numframes: Int = 0
        public var frames: Array<dsprframe_t> // variable sized

        {
            ident = b.getInt()
            version = b.getInt()
            numframes = b.getInt()

            frames = arrayOfNulls<dsprframe_t>(numframes)
            for (i in 0..numframes - 1) {
                frames[i] = dsprframe_t(b)
            }
        }
    }

    /*
	==============================================================================
	
	  .WAL texture file format
	
	==============================================================================
	*/
    public class miptex_t(b: ByteBuffer) {

        public var name: String // char name[32];
        public var width: Int = 0
        public var height: Int = 0
        public var offsets: IntArray = IntArray(MIPLEVELS) // 4 mip maps stored
        // next frame in animation chain
        public var animname: String //	char	animname[32];
        public var flags: Int = 0
        public var contents: Int = 0
        public var value: Int = 0

        public constructor(dataBytes: ByteArray) : this(ByteBuffer.wrap(dataBytes)) {
        }

        {
            // is stored as little endian
            b.order(ByteOrder.LITTLE_ENDIAN)

            val nameBuf = ByteArray(NAME_SIZE)
            // fill header
            b.get(nameBuf)
            name = String(nameBuf).trim()
            width = b.getInt()
            height = b.getInt()
            offsets[0] = b.getInt()
            offsets[1] = b.getInt()
            offsets[2] = b.getInt()
            offsets[3] = b.getInt()
            b.get(nameBuf)
            animname = String(nameBuf).trim()
            flags = b.getInt()
            contents = b.getInt()
            value = b.getInt()
        }

        companion object {

            val MIPLEVELS = 4
            val NAME_SIZE = 32
        }

    }

    // =============================================================================

    public class dheader_t(bb: ByteBuffer) {

        {
            bb.order(ByteOrder.LITTLE_ENDIAN)
            this.ident = bb.getInt()
            this.version = bb.getInt()

            for (n in 0..Defines.HEADER_LUMPS - 1)
                lumps[n] = lump_t(bb.getInt(), bb.getInt())

        }

        public var ident: Int = 0
        public var version: Int = 0
        public var lumps: Array<lump_t> = arrayOfNulls<lump_t>(Defines.HEADER_LUMPS)
    }

    public class dmodel_t(bb: ByteBuffer) {

        {
            bb.order(ByteOrder.LITTLE_ENDIAN)

            for (j in 0..3 - 1)
                mins[j] = bb.getFloat()

            for (j in 0..3 - 1)
                maxs[j] = bb.getFloat()

            for (j in 0..3 - 1)
                origin[j] = bb.getFloat()

            headnode = bb.getInt()
            firstface = bb.getInt()
            numfaces = bb.getInt()
        }

        public var mins: FloatArray = floatArray(0.0, 0.0, 0.0)
        public var maxs: FloatArray = floatArray(0.0, 0.0, 0.0)
        public var origin: FloatArray = floatArray(0.0, 0.0, 0.0) // for sounds or lights
        public var headnode: Int = 0
        public var firstface: Int = 0
        public var numfaces: Int = 0 // submodels just draw faces

        companion object {
            // without walking the bsp tree

            public var SIZE: Int = 3 * 4 + 3 * 4 + 3 * 4 + 4 + 8
        }
    }

    public class dvertex_t(b: ByteBuffer) {

        public var point: FloatArray = floatArray(0.0, 0.0, 0.0)

        {
            point[0] = b.getFloat()
            point[1] = b.getFloat()
            point[2] = b.getFloat()
        }

        companion object {

            public val SIZE: Int = 3 * 4 // 3 mal 32 bit float
        }
    }


    // planes (x&~1) and (x&~1)+1 are always opposites
    public class dplane_t(bb: ByteBuffer) {

        {
            bb.order(ByteOrder.LITTLE_ENDIAN)

            normal[0] = (bb.getFloat())
            normal[1] = (bb.getFloat())
            normal[2] = (bb.getFloat())

            dist = (bb.getFloat())
            type = (bb.getInt())
        }

        public var normal: FloatArray = floatArray(0.0, 0.0, 0.0)
        public var dist: Float = 0.toFloat()
        public var type: Int = 0 // PLANE_X - PLANE_ANYZ ?remove? trivial to regenerate

        companion object {

            public val SIZE: Int = 3 * 4 + 4 + 4
        }
    }

    public class dnode_t(bb: ByteBuffer) {

        {

            bb.order(ByteOrder.LITTLE_ENDIAN)
            planenum = bb.getInt()

            children[0] = bb.getInt()
            children[1] = bb.getInt()

            for (j in 0..3 - 1)
                mins[j] = bb.getShort()

            for (j in 0..3 - 1)
                maxs[j] = bb.getShort()

            firstface = bb.getShort() and 65535
            numfaces = bb.getShort() and 65535

        }

        public var planenum: Int = 0
        public var children: IntArray = intArray(0, 0)
        // negative numbers are -(leafs+1), not nodes
        public var mins: ShortArray = shortArray(0, 0, 0) // for frustom culling
        public var maxs: ShortArray = shortArray(0, 0, 0)

        /*
		unsigned short	firstface;
		unsigned short	numfaces;	// counting both sides
		*/

        public var firstface: Int = 0
        public var numfaces: Int = 0

        companion object {

            public var SIZE: Int = 4 + 8 + 6 + 6 + 2 + 2 // counting both sides
        }
    }



    // note that edge 0 is never used, because negative edge nums are used for
    // counterclockwise use of the edge in a face

    public class dedge_t {
        // unsigned short v[2];
        var v = intArray(0, 0)
    }

    public class dface_t(b: ByteBuffer) {

        //unsigned short	planenum;
        public var planenum: Int = 0
        public var side: Short = 0

        public var firstedge: Int = 0 // we must support > 64k edges
        public var numedges: Short = 0
        public var texinfo: Short = 0

        // lighting info
        public var styles: ByteArray = ByteArray(Defines.MAXLIGHTMAPS)
        public var lightofs: Int = 0 // start of [numstyles*surfsize] samples

        {
            planenum = b.getShort() and 65535
            side = b.getShort()
            firstedge = b.getInt()
            numedges = b.getShort()
            texinfo = b.getShort()
            b.get(styles)
            lightofs = b.getInt()
        }

        companion object {

            public val SIZE: Int = 4 * Defines.SIZE_OF_SHORT + 2 * Defines.SIZE_OF_INT + Defines.MAXLIGHTMAPS
        }

    }

    public class dleaf_t(bb: ByteBuffer) {

        public constructor(cmod_base: ByteArray, i: Int, j: Int) : this(ByteBuffer.wrap(cmod_base, i, j).order(ByteOrder.LITTLE_ENDIAN)) {
        }

        {
            contents = bb.getInt()
            cluster = bb.getShort()
            area = bb.getShort()

            mins[0] = bb.getShort()
            mins[1] = bb.getShort()
            mins[2] = bb.getShort()

            maxs[0] = bb.getShort()
            maxs[1] = bb.getShort()
            maxs[2] = bb.getShort()

            firstleafface = bb.getShort() and 65535
            numleaffaces = bb.getShort() and 65535

            firstleafbrush = bb.getShort() and 65535
            numleafbrushes = bb.getShort() and 65535
        }

        public var contents: Int = 0 // OR of all brushes (not needed?)

        public var cluster: Short = 0
        public var area: Short = 0

        public var mins: ShortArray = shortArray(0, 0, 0) // for frustum culling
        public var maxs: ShortArray = shortArray(0, 0, 0)

        public var firstleafface: Int = 0 // unsigned short
        public var numleaffaces: Int = 0 // unsigned short

        public var firstleafbrush: Int = 0 // unsigned short
        public var numleafbrushes: Int = 0 // unsigned short

        companion object {

            public val SIZE: Int = 4 + 8 * 2 + 4 * 2
        }
    }

    public class dbrushside_t(bb: ByteBuffer) {

        {
            bb.order(ByteOrder.LITTLE_ENDIAN)

            planenum = bb.getShort() and 65535
            texinfo = bb.getShort()
        }

        //unsigned short planenum;
        var planenum: Int = 0 // facing out of the leaf

        var texinfo: Short = 0

        companion object {

            public var SIZE: Int = 4
        }
    }

    public class dbrush_t(bb: ByteBuffer) {

        {
            bb.order(ByteOrder.LITTLE_ENDIAN)
            firstside = bb.getInt()
            numsides = bb.getInt()
            contents = bb.getInt()
        }

        var firstside: Int = 0
        var numsides: Int = 0
        var contents: Int = 0

        companion object {

            public var SIZE: Int = 3 * 4
        }
    }

    //	#define	ANGLE_UP	-1
    //	#define	ANGLE_DOWN	-2

    // the visibility lump consists of a header with a count, then
    // byte offsets for the PVS and PHS of each cluster, then the raw
    // compressed bit vectors
    // #define	DVIS_PVS	0
    // #define	DVIS_PHS	1

    public class dvis_t(bb: ByteBuffer) {

        {
            numclusters = bb.getInt()
            bitofs = Array<IntArray>(numclusters, { IntArray(2) })

            for (i in 0..numclusters - 1) {
                bitofs[i][0] = bb.getInt()
                bitofs[i][1] = bb.getInt()
            }
        }

        public var numclusters: Int = 0
        public var bitofs: Array<IntArray> = Array(8, { IntArray(2) }) // bitofs[numclusters][2]
    }

    // each area has a list of portals that lead into other areas
    // when portals are closed, other areas may not be visible or
    // hearable even if the vis info says that it should be

    public class dareaportal_t {

        public constructor() {
        }

        public constructor(bb: ByteBuffer) {
            bb.order(ByteOrder.LITTLE_ENDIAN)
            portalnum = bb.getInt()
            otherarea = bb.getInt()
        }

        var portalnum: Int = 0
        var otherarea: Int = 0

        companion object {

            public var SIZE: Int = 8
        }
    }

    public class darea_t(bb: ByteBuffer) {

        {

            bb.order(ByteOrder.LITTLE_ENDIAN)

            numareaportals = bb.getInt()
            firstareaportal = bb.getInt()

        }

        var numareaportals: Int = 0
        var firstareaportal: Int = 0

        companion object {

            public var SIZE: Int = 8
        }
    }

    companion object {

        /*
	========================================================================
	
	.MD2 triangle model file format
	
	========================================================================
	*/

        public val IDALIASHEADER: Int = (('2' shl 24) + ('P' shl 16) + ('D' shl 8) + 'I')
        public val ALIAS_VERSION: Int = 8

        public val MAX_TRIANGLES: Int = 4096
        public val MAX_VERTS: Int = 2048
        public val MAX_FRAMES: Int = 512
        public val MAX_MD2SKINS: Int = 32
        public val MAX_SKINNAME: Int = 64

        public val DTRIVERTX_V0: Int = 0
        public val DTRIVERTX_V1: Int = 1
        public val DTRIVERTX_V2: Int = 2
        public val DTRIVERTX_LNI: Int = 3
        public val DTRIVERTX_SIZE: Int = 4

        /*
	========================================================================
	
	.SP2 sprite file format
	
	========================================================================
	*/
        // little-endian "IDS2"
        public val IDSPRITEHEADER: Int = (('2' shl 24) + ('S' shl 16) + ('D' shl 8) + 'I')
        public val SPRITE_VERSION: Int = 2

        /*
	==============================================================================
	
	  .BSP file format
	
	==============================================================================
	*/

        public val IDBSPHEADER: Int = (('P' shl 24) + ('S' shl 16) + ('B' shl 8) + 'I')
    }

}