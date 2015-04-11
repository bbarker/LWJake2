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

import lwjake2.Defines
import lwjake2.util.QuakeFile

import java.io.IOException

public class client_persistant_t {

    public fun set(from: client_persistant_t) {

        userinfo = from.userinfo
        netname = from.netname
        hand = from.hand
        connected = from.connected
        health = from.health
        max_health = from.max_health
        savedFlags = from.savedFlags
        selected_item = from.selected_item
        System.arraycopy(from.inventory, 0, inventory, 0, inventory.size())
        max_bullets = from.max_bullets
        max_shells = from.max_shells
        max_rockets = from.max_rockets
        max_grenades = from.max_grenades
        max_cells = from.max_cells
        max_slugs = from.max_slugs
        weapon = from.weapon
        lastweapon = from.lastweapon
        power_cubes = from.power_cubes
        score = from.score
        game_helpchanged = from.game_helpchanged
        helpchanged = from.helpchanged
        spectator = from.spectator
    }

    //	client data that stays across multiple level loads
    var userinfo = ""
    var netname = ""
    var hand: Int = 0

    var connected: Boolean = false // a loadgame will leave valid entities that
    // just don't have a connection yet

    // values saved and restored from edicts when changing levels
    var health: Int = 0
    var max_health: Int = 0
    var savedFlags: Int = 0

    var selected_item: Int = 0
    var inventory = IntArray(Defines.MAX_ITEMS)

    // ammo capacities
    public var max_bullets: Int = 0
    public var max_shells: Int = 0
    public var max_rockets: Int = 0
    public var max_grenades: Int = 0
    public var max_cells: Int = 0
    public var max_slugs: Int = 0
    //pointer
    var weapon: gitem_t
    //pointer
    var lastweapon: gitem_t
    var power_cubes: Int = 0 // used for tracking the cubes in coop games
    var score: Int = 0 // for calculating total unit score in coop games
    var game_helpchanged: Int = 0
    var helpchanged: Int = 0
    var spectator: Boolean = false // client is a spectator

    /** Reads a client_persistant structure from a file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {

        userinfo = f.readString()
        netname = f.readString()

        hand = f.readInt()

        connected = f.readInt() != 0
        health = f.readInt()

        max_health = f.readInt()
        savedFlags = f.readInt()
        selected_item = f.readInt()

        for (n in 0..Defines.MAX_ITEMS - 1)
            inventory[n] = f.readInt()

        max_bullets = f.readInt()
        max_shells = f.readInt()
        max_rockets = f.readInt()
        max_grenades = f.readInt()
        max_cells = f.readInt()
        max_slugs = f.readInt()

        weapon = f.readItem()
        lastweapon = f.readItem()
        power_cubes = f.readInt()
        score = f.readInt()

        game_helpchanged = f.readInt()
        helpchanged = f.readInt()
        spectator = f.readInt() != 0
    }

    /** Writes a client_persistant structure to a file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        // client persistant_t
        f.writeString(userinfo)
        f.writeString(netname)

        f.writeInt(hand)

        f.writeInt(if (connected) 1 else 0)
        f.writeInt(health)

        f.writeInt(max_health)
        f.writeInt(savedFlags)
        f.writeInt(selected_item)

        for (n in 0..Defines.MAX_ITEMS - 1)
            f.writeInt(inventory[n])

        f.writeInt(max_bullets)
        f.writeInt(max_shells)
        f.writeInt(max_rockets)
        f.writeInt(max_grenades)
        f.writeInt(max_cells)
        f.writeInt(max_slugs)

        f.writeItem(weapon)
        f.writeItem(lastweapon)
        f.writeInt(power_cubes)
        f.writeInt(score)

        f.writeInt(game_helpchanged)
        f.writeInt(helpchanged)
        f.writeInt(if (spectator) 1 else 0)
    }
}