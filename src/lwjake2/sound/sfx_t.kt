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

package lwjake2.sound

public class sfx_t {
    public var name: String
    public var registration_sequence: Int = 0
    public var cache: sfxcache_t? = null
    public var truename: String? = null

    // is used for AL buffers
    public var bufferId: Int = -1
    public var isCached: Boolean = false

    public fun clear() {
        name = truename = null
        cache = null
        registration_sequence = 0
        bufferId = -1
        isCached = false
    }
}
