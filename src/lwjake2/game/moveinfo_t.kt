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

import lwjake2.util.QuakeFile

import java.io.IOException

public class moveinfo_t {
    // fixed data
    var start_origin = floatArray(0.0, 0.0, 0.0)
    var start_angles = floatArray(0.0, 0.0, 0.0)
    var end_origin = floatArray(0.0, 0.0, 0.0)
    var end_angles = floatArray(0.0, 0.0, 0.0)

    var sound_start: Int = 0
    var sound_middle: Int = 0
    var sound_end: Int = 0

    var accel: Float = 0.toFloat()
    var speed: Float = 0.toFloat()
    var decel: Float = 0.toFloat()
    var distance: Float = 0.toFloat()

    var wait: Float = 0.toFloat()

    // state data
    var state: Int = 0
    var dir = floatArray(0.0, 0.0, 0.0)

    var current_speed: Float = 0.toFloat()
    var move_speed: Float = 0.toFloat()
    var next_speed: Float = 0.toFloat()
    var remaining_distance: Float = 0.toFloat()
    var decel_distance: Float = 0.toFloat()
    var endfunc: EntThinkAdapter

    /** saves the moveinfo to the file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeVector(start_origin)
        f.writeVector(start_angles)
        f.writeVector(end_origin)
        f.writeVector(end_angles)

        f.writeInt(sound_start)
        f.writeInt(sound_middle)
        f.writeInt(sound_end)

        f.writeFloat(accel)
        f.writeFloat(speed)
        f.writeFloat(decel)
        f.writeFloat(distance)

        f.writeFloat(wait)

        f.writeInt(state)
        f.writeVector(dir)

        f.writeFloat(current_speed)
        f.writeFloat(move_speed)
        f.writeFloat(next_speed)
        f.writeFloat(remaining_distance)
        f.writeFloat(decel_distance)
        f.writeAdapter(endfunc)
    }

    /** Reads the moveinfo from a file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        start_origin = f.readVector()
        start_angles = f.readVector()
        end_origin = f.readVector()
        end_angles = f.readVector()

        sound_start = f.readInt()
        sound_middle = f.readInt()
        sound_end = f.readInt()

        accel = f.readFloat()
        speed = f.readFloat()
        decel = f.readFloat()
        distance = f.readFloat()

        wait = f.readFloat()

        state = f.readInt()
        dir = f.readVector()

        current_speed = f.readFloat()
        move_speed = f.readFloat()
        next_speed = f.readFloat()
        remaining_distance = f.readFloat()
        decel_distance = f.readFloat()
        endfunc = f.readAdapter() as EntThinkAdapter
    }
}
