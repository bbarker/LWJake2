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

import lwjake2.Defines
import lwjake2.render.image_t
import lwjake2.render.model_t

public class clientinfo_t {
    var name = ""
    var cinfo = ""
    var skin: image_t    // ptr
    var icon: image_t    // ptr
    var iconname = ""
    var model: model_t    // ptr
    var weaponmodel = arrayOfNulls<model_t>(Defines.MAX_CLIENTWEAPONMODELS) // arary of references

    //	public void reset()
    //	{
    //		set(new clientinfo_t());
    //	}

    public fun set(from: clientinfo_t) {
        name = from.name
        cinfo = from.cinfo
        skin = from.skin
        icon = from.icon
        iconname = from.iconname
        model = from.model
        System.arraycopy(from.weaponmodel, 0, weaponmodel, 0, Defines.MAX_CLIENTWEAPONMODELS)
    }
}
