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

public class gitem_t {

    public constructor(xxx: Int) {
        index = xxx
    }

    public constructor(classname: String, pickup: EntInteractAdapter, use: ItemUseAdapter, drop: ItemDropAdapter, weaponthink: EntThinkAdapter) {
    }

    public constructor(classname: String, pickup: EntInteractAdapter, use: ItemUseAdapter, drop: ItemDropAdapter, weaponthink: EntThinkAdapter, pickup_sound: String, world_model: String, world_model_flags: Int, view_model: String, icon: String, pickup_name: String, count_width: Int, quantity: Int, ammo: String, flags: Int, weapmodel: Int, info: gitem_armor_t, tag: Int, precaches: String) {
        this.classname = classname
        this.pickup = pickup
        this.use = use
        this.drop = drop
        this.weaponthink = weaponthink
        this.pickup_sound = pickup_sound
        this.world_model = world_model
        this.world_model_flags = world_model_flags
        this.view_model = view_model
        this.icon = icon
        this.pickup_name = pickup_name
        this.count_width = count_width
        this.quantity = quantity
        this.ammo = ammo
        this.flags = flags
        this.weapmodel = weapmodel
        this.info = info
        this.tag = tag
        this.precaches = precaches

        this.index = id++
    }

    var classname: String // spawning name

    var pickup: EntInteractAdapter

    var use: ItemUseAdapter

    var drop: ItemDropAdapter

    var weaponthink: EntThinkAdapter

    var pickup_sound: String

    var world_model: String

    var world_model_flags: Int = 0

    var view_model: String

    // client side info
    var icon: String

    var pickup_name: String // for printing on pickup

    var count_width: Int = 0 // number of digits to display by icon

    var quantity: Int = 0 // for ammo how much, for weapons how much is used per shot

    var ammo: String // for weapons

    var flags: Int = 0 // IT_* flags

    var weapmodel: Int = 0 // weapon model index (for weapons)

    var info: Object

    var tag: Int = 0

    var precaches: String // string of all models, sounds, and images this item will
    // use

    public var index: Int = 0

    companion object {
        private var id = 0
    }
}