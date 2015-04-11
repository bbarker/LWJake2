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

public class glstate_t {
    public var inverse_intensity: Float = 0.toFloat()
    public var fullscreen: Boolean = false

    public var prev_mode: Int = 0

    public var d_16to8table: ByteArray

    public var lightmap_textures: Int = 0

    public var currenttextures: IntArray = intArray(0, 0)
    public var currenttmu: Int = 0

    public var camera_separation: Float = 0.toFloat()
    public var stereo_enabled: Boolean = false

    public var originalRedGammaTable: ByteArray = ByteArray(256)
    public var originalGreenGammaTable: ByteArray = ByteArray(256)
    public var originalBlueGammaTable: ByteArray = ByteArray(256)

}
