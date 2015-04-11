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

public class refdef_t {
    public var x: Int = 0
    public var y: Int = 0
    public var width: Int = 0
    public var height: Int = 0// in virtual screen coordinates
    public var fov_x: Float = 0.toFloat()
    public var fov_y: Float = 0.toFloat()
    public var vieworg: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var viewangles: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var blend: FloatArray = floatArray(0.0, 0.0, 0.0, 0.0)            // rgba 0-1 full screen blend
    public var time: Float = 0.toFloat()                // time is uesed to auto animate
    public var rdflags: Int = 0            // RDF_UNDERWATER, etc

    public var areabits: ByteArray            // if not NULL, only areas with set bits will be drawn

    public var lightstyles: Array<lightstyle_t>    // [MAX_LIGHTSTYLES]

    public var num_entities: Int = 0
    public var entities: Array<entity_t>

    public var num_dlights: Int = 0
    public var dlights: Array<dlight_t>

    public var num_particles: Int = 0
    //public particle_t	particles[];
}
