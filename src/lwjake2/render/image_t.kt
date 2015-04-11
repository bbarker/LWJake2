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

public class image_t(// used to get the pos in array
        // added by cwei
        public val id: Int) {

    // quake 2 variables
    public var name: String = "" // game path, including extension
    // enum imagetype_t
    public var type: Int = 0
    public var width: Int = 0
    public var height: Int = 0 // source image
    public var upload_width: Int = 0
    public var upload_height: Int = 0 // after power of two and picmip
    public var registration_sequence: Int = 0 // 0 = free
    public var texturechain: msurface_t? = null // for sort-by-texture world drawing
    public var texnum: Int = 0 // gl texture binding
    public var sl: Float = 0.toFloat()
    public var tl: Float = 0.toFloat()
    public var sh: Float = 0.toFloat()
    public var th: Float = 0.toFloat() // 0,0 - 1,1 unless part of the scrap
    public var scrap: Boolean = false
    public var has_alpha: Boolean = false

    public var paletted: Boolean = false

    public fun clear() {
        // don't clear the id
        // wichtig !!!
        name = ""
        type = 0
        width = height = 0
        upload_width = upload_height = 0
        registration_sequence = 0 // 0 = free
        texturechain = null
        texnum = 0 // gl texture binding
        sl = tl = sh = th = 0
        scrap = false
        has_alpha = false
        paletted = false
    }

    override fun toString(): String {
        return name + ":" + texnum
    }

    companion object {

        public val MAX_NAME_SIZE: Int = Defines.MAX_QPATH
    }
}
