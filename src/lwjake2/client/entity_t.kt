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

package lwjake2.client

import lwjake2.render.image_t
import lwjake2.render.model_t
import lwjake2.util.Math3D

public class entity_t : Cloneable {
    //ptr
    public var model: model_t? = null // opaque type outside refresh
    public var angles: FloatArray = floatArray(0.0, 0.0, 0.0)

    /*
	** most recent data
	*/
    public var origin: FloatArray = floatArray(0.0, 0.0, 0.0) // also used as RF_BEAM's "from"
    public var frame: Int = 0 // also used as RF_BEAM's diameter

    /*
	** previous data for lerping
	*/
    public var oldorigin: FloatArray = floatArray(0.0, 0.0, 0.0) // also used as RF_BEAM's "to"
    public var oldframe: Int = 0

    /*
	** misc
	*/
    public var backlerp: Float = 0.toFloat() // 0.0 = current, 1.0 = old
    public var skinnum: Int = 0 // also used as RF_BEAM's palette index

    public var lightstyle: Int = 0 // for flashing entities
    public var alpha: Float = 0.toFloat() // ignore if RF_TRANSLUCENT isn't set

    // reference
    public var skin: image_t? = null // NULL for inline skin
    public var flags: Int = 0


    public fun set(src: entity_t) {
        this.model = src.model
        Math3D.VectorCopy(src.angles, this.angles)
        Math3D.VectorCopy(src.origin, this.origin)
        this.frame = src.frame
        Math3D.VectorCopy(src.oldorigin, this.oldorigin)
        this.oldframe = src.oldframe
        this.backlerp = src.backlerp
        this.skinnum = src.skinnum
        this.lightstyle = src.lightstyle
        this.alpha = src.alpha
        this.skin = src.skin
        this.flags = src.flags
    }

    public fun clear() {
        model = null
        Math3D.VectorClear(angles)
        Math3D.VectorClear(origin)
        frame = 0
        Math3D.VectorClear(oldorigin)
        oldframe = 0
        backlerp = 0
        skinnum = 0
        lightstyle = 0
        alpha = 0
        skin = null
        flags = 0
    }

}
