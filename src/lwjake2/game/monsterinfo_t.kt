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

public class monsterinfo_t {

    public var currentmove: mmove_t? = null
    public var aiflags: Int = 0
    public var nextframe: Int = 0
    public var scale: Float = 0.toFloat()

    public var stand: EntThinkAdapter
    public var idle: EntThinkAdapter
    public var search: EntThinkAdapter
    public var walk: EntThinkAdapter
    public var run: EntThinkAdapter

    public var dodge: EntDodgeAdapter

    public var attack: EntThinkAdapter
    public var melee: EntThinkAdapter

    public var sight: EntInteractAdapter

    public var checkattack: EntThinkAdapter

    public var pausetime: Float = 0.toFloat()
    public var attack_finished: Float = 0.toFloat()

    public var saved_goal: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var search_time: Float = 0.toFloat()
    public var trail_time: Float = 0.toFloat()
    public var last_sighting: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var attack_state: Int = 0
    public var lefty: Int = 0
    public var idle_time: Float = 0.toFloat()
    public var linkcount: Int = 0

    public var power_armor_type: Int = 0
    public var power_armor_power: Int = 0

    /** Writes the monsterinfo to the file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeBoolean(currentmove != null)
        if (currentmove != null)
            currentmove!!.write(f)
        f.writeInt(aiflags)
        f.writeInt(nextframe)
        f.writeFloat(scale)
        f.writeAdapter(stand)
        f.writeAdapter(idle)
        f.writeAdapter(search)
        f.writeAdapter(walk)
        f.writeAdapter(run)

        f.writeAdapter(dodge)

        f.writeAdapter(attack)
        f.writeAdapter(melee)

        f.writeAdapter(sight)

        f.writeAdapter(checkattack)

        f.writeFloat(pausetime)
        f.writeFloat(attack_finished)

        f.writeVector(saved_goal)

        f.writeFloat(search_time)
        f.writeFloat(trail_time)

        f.writeVector(last_sighting)

        f.writeInt(attack_state)
        f.writeInt(lefty)

        f.writeFloat(idle_time)
        f.writeInt(linkcount)

        f.writeInt(power_armor_power)
        f.writeInt(power_armor_type)
    }

    /** Writes the monsterinfo to the file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        if (f.readBoolean()) {
            currentmove = mmove_t()
            currentmove!!.read(f)
        } else
            currentmove = null
        aiflags = f.readInt()
        nextframe = f.readInt()
        scale = f.readFloat()
        stand = f.readAdapter() as EntThinkAdapter
        idle = f.readAdapter() as EntThinkAdapter
        search = f.readAdapter() as EntThinkAdapter
        walk = f.readAdapter() as EntThinkAdapter
        run = f.readAdapter() as EntThinkAdapter

        dodge = f.readAdapter() as EntDodgeAdapter

        attack = f.readAdapter() as EntThinkAdapter
        melee = f.readAdapter() as EntThinkAdapter

        sight = f.readAdapter() as EntInteractAdapter

        checkattack = f.readAdapter() as EntThinkAdapter

        pausetime = f.readFloat()
        attack_finished = f.readFloat()

        saved_goal = f.readVector()

        search_time = f.readFloat()
        trail_time = f.readFloat()

        last_sighting = f.readVector()

        attack_state = f.readInt()
        lefty = f.readInt()

        idle_time = f.readFloat()
        linkcount = f.readInt()

        power_armor_power = f.readInt()
        power_armor_type = f.readInt()

    }


}
