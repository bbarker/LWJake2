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

package lwjake2.game

public class usercmd_t : Cloneable {
    public var msec: Byte = 0
    public var buttons: Byte = 0
    public var angles: ShortArray = ShortArray(3)
    public var forwardmove: Short = 0
    public var sidemove: Short = 0
    public var upmove: Short = 0
    public var impulse: Byte = 0 // remove?
    public var lightlevel: Byte = 0 // light level the player is standing on

    public fun clear() {
        forwardmove = sidemove = upmove = (msec = buttons = impulse = lightlevel = 0).toShort()
        angles[0] = angles[1] = angles[2] = 0
    }

    public constructor() {
    }

    public constructor(from: usercmd_t) {
        msec = from.msec
        buttons = from.buttons
        angles[0] = from.angles[0]
        angles[1] = from.angles[1]
        angles[2] = from.angles[2]
        forwardmove = from.forwardmove
        sidemove = from.sidemove
        upmove = from.upmove
        impulse = from.impulse
        lightlevel = from.lightlevel
    }

    public fun set(from: usercmd_t): usercmd_t {
        msec = from.msec
        buttons = from.buttons
        angles[0] = from.angles[0]
        angles[1] = from.angles[1]
        angles[2] = from.angles[2]
        forwardmove = from.forwardmove
        sidemove = from.sidemove
        upmove = from.upmove
        impulse = from.impulse
        lightlevel = from.lightlevel

        return this
    }
}