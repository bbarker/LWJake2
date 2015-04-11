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

public class mmodel_t {
    public var mins: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var maxs: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var origin: FloatArray = floatArray(0.0, 0.0, 0.0) // for sounds or lights
    public var radius: Float = 0.toFloat()
    public var headnode: Int = 0
    public var visleafs: Int = 0 // not including the solid leaf 0
    public var firstface: Int = 0
    public var numfaces: Int = 0
}
