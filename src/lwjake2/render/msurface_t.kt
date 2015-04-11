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

import java.nio.ByteBuffer

public class msurface_t {

    public var visframe: Int = 0 // should be drawn when node is crossed

    public var plane: cplane_t
    public var flags: Int = 0

    public var firstedge: Int = 0 // look up in model->surfedges[], negative numbers
    public var numedges: Int = 0 // are backwards edges

    public var texturemins: ShortArray = shortArray(0, 0)
    public var extents: ShortArray = shortArray(0, 0)

    public var light_s: Int = 0
    public var light_t: Int = 0 // gl lightmap coordinates
    public var dlight_s: Int = 0
    public var dlight_t: Int = 0
    // gl lightmap coordinates for dynamic lightmaps

    public var polys: glpoly_t? = null // multiple if warped
    public var texturechain: msurface_t? = null
    public var lightmapchain: msurface_t? = null

    // TODO check this
    public var texinfo: mtexinfo_t = mtexinfo_t()

    // lighting info
    public var dlightframe: Int = 0
    public var dlightbits: Int = 0

    public var lightmaptexturenum: Int = 0
    public var styles: ByteArray = ByteArray(Defines.MAXLIGHTMAPS)
    public var cached_light: FloatArray = FloatArray(Defines.MAXLIGHTMAPS)
    // values currently used in lightmap
    //public byte samples[]; // [numstyles*surfsize]
    public var samples: ByteBuffer? = null // [numstyles*surfsize]

    public fun clear() {
        visframe = 0
        plane.clear()
        flags = 0

        firstedge = 0
        numedges = 0

        texturemins[0] = texturemins[1] = (-1).toShort()
        extents[0] = extents[1] = 0

        light_s = light_t = 0
        dlight_s = dlight_t = 0

        polys = null
        texturechain = null
        lightmapchain = null

        //texinfo = new mtexinfo_t();
        texinfo.clear()

        dlightframe = 0
        dlightbits = 0

        lightmaptexturenum = 0

        for (i in styles.indices) {
            styles[i] = 0
        }
        for (i in cached_light.indices) {
            cached_light[i] = 0
        }
        if (samples != null) samples!!.clear()
    }
}
