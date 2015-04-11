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

/**
 * cparticle_t

 * @author cwei
 */
public class cparticle_t {

    public var next: cparticle_t
    public var time: Float = 0.toFloat()

    public var org: FloatArray = floatArray(0.0, 0.0, 0.0) // vec3_t
    public var vel: FloatArray = floatArray(0.0, 0.0, 0.0) // vec3_t
    public var accel: FloatArray = floatArray(0.0, 0.0, 0.0) // vec3_t

    public var color: Float = 0.toFloat()
    //public float colorvel;
    public var alpha: Float = 0.toFloat()
    public var alphavel: Float = 0.toFloat()
}
