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
import lwjake2.qcommon.Com
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.util.StringTokenizer


public class GameItems {
    companion object {

        public var jacketarmor_info: gitem_armor_t = gitem_armor_t(25, 50, .30.toFloat(), .00.toFloat(), Defines.ARMOR_JACKET)
        public var combatarmor_info: gitem_armor_t = gitem_armor_t(50, 100, .60.toFloat(), .30.toFloat(), Defines.ARMOR_COMBAT)
        public var bodyarmor_info: gitem_armor_t = gitem_armor_t(100, 200, .80.toFloat(), .60.toFloat(), Defines.ARMOR_BODY)
        var quad_drop_timeout_hack = 0
        var jacket_armor_index: Int = 0
        var combat_armor_index: Int = 0
        var body_armor_index: Int = 0
        var power_screen_index: Int = 0
        var power_shield_index: Int = 0

        var DoRespawn: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "do_respawn"
            }

            public fun think(ent: edict_t?): Boolean {
                var ent = ent
                if (ent!!.team != null) {
                    val master: edict_t
                    var count: Int
                    var choice = 0

                    master = ent!!.teammaster

                    // count the depth
                    run {
                        count = 0
                        ent = master
                        while (ent != null) {
                            ent = ent!!.chain
                            count++
                        }
                    }

                    choice = Lib.rand() % count

                    run {
                        count = 0
                        ent = master
                        while (count < choice) {
                            ent = ent!!.chain
                            count++
                        }
                    }
                }

                ent!!.svflags = ent!!.svflags and Defines.SVF_NOCLIENT.inv()
                ent!!.solid = Defines.SOLID_TRIGGER
                GameBase.gi.linkentity(ent)

                // send an effect
                ent!!.s.event = Defines.EV_ITEM_RESPAWN

                return false
            }
        }
        var Pickup_Pack: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_pack"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {

                var item: gitem_t?
                val index: Int

                if (other.client.pers.max_bullets < 300)
                    other.client.pers.max_bullets = 300
                if (other.client.pers.max_shells < 200)
                    other.client.pers.max_shells = 200
                if (other.client.pers.max_rockets < 100)
                    other.client.pers.max_rockets = 100
                if (other.client.pers.max_grenades < 100)
                    other.client.pers.max_grenades = 100
                if (other.client.pers.max_cells < 300)
                    other.client.pers.max_cells = 300
                if (other.client.pers.max_slugs < 100)
                    other.client.pers.max_slugs = 100

                item = FindItem("Bullets")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_bullets)
                        other.client.pers.inventory[index] = other.client.pers.max_bullets
                }

                item = FindItem("Shells")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_shells)
                        other.client.pers.inventory[index] = other.client.pers.max_shells
                }

                item = FindItem("Cells")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_cells)
                        other.client.pers.inventory[index] = other.client.pers.max_cells
                }

                item = FindItem("Grenades")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_grenades)
                        other.client.pers.inventory[index] = other.client.pers.max_grenades
                }

                item = FindItem("Rockets")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_rockets)
                        other.client.pers.inventory[index] = other.client.pers.max_rockets
                }

                item = FindItem("Slugs")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_slugs)
                        other.client.pers.inventory[index] = other.client.pers.max_slugs
                }

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, ent.item.quantity)

                return true
            }
        }
        val Pickup_Health: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_health"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {

                if (0 == (ent.style and Defines.HEALTH_IGNORE_MAX))
                    if (other.health >= other.max_health)
                        return false

                other.health += ent.count

                if (0 == (ent.style and Defines.HEALTH_IGNORE_MAX)) {
                    if (other.health > other.max_health)
                        other.health = other.max_health
                }

                if (0 != (ent.style and Defines.HEALTH_TIMED)) {
                    ent.think = GameUtil.MegaHealth_think
                    ent.nextthink = GameBase.level.time + 5.toFloat()
                    ent.owner = other
                    ent.flags = ent.flags or Defines.FL_RESPAWN
                    ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
                    ent.solid = Defines.SOLID_NOT
                } else {
                    if (!((ent.spawnflags and Defines.DROPPED_ITEM) != 0) && (GameBase.deathmatch.value != 0))
                        SetRespawn(ent, 30)
                }

                return true
            }

        }
        var Touch_Item: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "touch_item"
            }

            public fun touch(ent: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                val taken: Boolean

                if (ent.classname.equals("item_breather"))
                    taken = false

                if (other.client == null)
                    return
                if (other.health < 1)
                    return  // dead people can't pickup
                if (ent.item.pickup == null)
                    return  // not a grabbable item?

                taken = ent.item.pickup.interact(ent, other)

                if (taken) {
                    // flash the screen
                    other.client.bonus_alpha = 0.25.toFloat()

                    // show icon and name on status bar
                    other.client.ps.stats[Defines.STAT_PICKUP_ICON] = GameBase.gi.imageindex(ent.item.icon) as Short
                    other.client.ps.stats[Defines.STAT_PICKUP_STRING] = (Defines.CS_ITEMS + ITEM_INDEX(ent.item)) as Short
                    other.client.pickup_msg_time = GameBase.level.time + 3.0.toFloat()

                    // change selected item
                    if (ent.item.use != null)
                        other.client.pers.selected_item = other.client.ps.stats[Defines.STAT_SELECTED_ITEM] = ITEM_INDEX(ent.item).toShort()

                    if (ent.item.pickup == Pickup_Health) {
                        if (ent.count == 2)
                            GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/s_health.wav"), 1, Defines.ATTN_NORM, 0)
                        else if (ent.count == 10)
                            GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/n_health.wav"), 1, Defines.ATTN_NORM, 0)
                        else if (ent.count == 25)
                            GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/l_health.wav"), 1, Defines.ATTN_NORM, 0)
                        else
                        // (ent.count == 100)
                            GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/m_health.wav"), 1, Defines.ATTN_NORM, 0)
                    } else if (ent.item.pickup_sound != null) {
                        GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex(ent.item.pickup_sound), 1, Defines.ATTN_NORM, 0)
                    }
                }

                if (0 == (ent.spawnflags and Defines.ITEM_TARGETS_USED)) {
                    GameUtil.G_UseTargets(ent, other)
                    ent.spawnflags = ent.spawnflags or Defines.ITEM_TARGETS_USED
                }

                if (!taken)
                    return

                Com.dprintln("Picked up:" + ent.classname)

                if (!((GameBase.coop.value != 0) && (ent.item.flags and Defines.IT_STAY_COOP) != 0) || 0 != (ent.spawnflags and (Defines.DROPPED_ITEM or Defines.DROPPED_PLAYER_ITEM))) {
                    if ((ent.flags and Defines.FL_RESPAWN) != 0)
                        ent.flags = ent.flags and Defines.FL_RESPAWN.inv()
                    else
                        GameUtil.G_FreeEdict(ent)
                }
            }
        }
        var drop_temp_touch: EntTouchAdapter = object : EntTouchAdapter() {
            public fun getID(): String {
                return "drop_temp_touch"
            }

            public fun touch(ent: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
                if (other == ent.owner)
                    return

                Touch_Item.touch(ent, other, plane, surf)
            }
        }
        var drop_make_touchable: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "drop_make_touchable"
            }

            public fun think(ent: edict_t): Boolean {
                ent.touch = Touch_Item
                if (GameBase.deathmatch.value != 0) {
                    ent.nextthink = GameBase.level.time + 29
                    ent.think = GameUtil.G_FreeEdictA
                }
                return false
            }
        }
        var Use_Quad: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_quad"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                val timeout: Int

                ent.client.pers.inventory[ITEM_INDEX(item)]--
                GameUtil.ValidateSelectedItem(ent)

                if (quad_drop_timeout_hack != 0) {
                    timeout = quad_drop_timeout_hack
                    quad_drop_timeout_hack = 0
                } else {
                    timeout = 300
                }

                if (ent.client.quad_framenum > GameBase.level.framenum)
                    ent.client.quad_framenum += timeout
                else
                    ent.client.quad_framenum = GameBase.level.framenum + timeout

                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }

        var Use_Invulnerability: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_invulnerability"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                ent.client.pers.inventory[ITEM_INDEX(item)]--
                GameUtil.ValidateSelectedItem(ent)

                if (ent.client.invincible_framenum > GameBase.level.framenum)
                    ent.client.invincible_framenum += 300
                else
                    ent.client.invincible_framenum = GameBase.level.framenum + 300

                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/protect.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }
        var Use_Breather: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_breather"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                ent.client.pers.inventory[ITEM_INDEX(item)]--

                GameUtil.ValidateSelectedItem(ent)

                if (ent.client.breather_framenum > GameBase.level.framenum)
                    ent.client.breather_framenum += 300
                else
                    ent.client.breather_framenum = GameBase.level.framenum + 300

                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }
        var Use_Envirosuit: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_envirosuit"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                ent.client.pers.inventory[ITEM_INDEX(item)]--
                GameUtil.ValidateSelectedItem(ent)

                if (ent.client.enviro_framenum > GameBase.level.framenum)
                    ent.client.enviro_framenum += 300
                else
                    ent.client.enviro_framenum = GameBase.level.framenum + 300

                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }
        var Use_Silencer: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_silencer"
            }

            public fun use(ent: edict_t, item: gitem_t) {

                ent.client.pers.inventory[ITEM_INDEX(item)]--
                GameUtil.ValidateSelectedItem(ent)
                ent.client.silencer_shots += 30

                GameBase.gi.sound(ent, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/damage.wav"), 1, Defines.ATTN_NORM, 0)
            }
        }
        var Pickup_Key: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_key"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                if (GameBase.coop.value != 0) {
                    if (Lib.strcmp(ent.classname, "key_power_cube") == 0) {
                        if ((other.client.pers.power_cubes and ((ent.spawnflags and 65280) shr 8)) != 0)
                            return false
                        other.client.pers.inventory[ITEM_INDEX(ent.item)]++
                        other.client.pers.power_cubes = other.client.pers.power_cubes or ((ent.spawnflags and 65280) shr 8)
                    } else {
                        if (other.client.pers.inventory[ITEM_INDEX(ent.item)] != 0)
                            return false
                        other.client.pers.inventory[ITEM_INDEX(ent.item)] = 1
                    }
                    return true
                }
                other.client.pers.inventory[ITEM_INDEX(ent.item)]++
                return true
            }
        }
        public var Pickup_Ammo: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_ammo"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                val oldcount: Int
                val count: Int
                val weapon: Boolean

                weapon = (ent.item.flags and Defines.IT_WEAPON) != 0
                if ((weapon) && (GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO) != 0)
                    count = 1000
                else if (ent.count != 0)
                    count = ent.count
                else
                    count = ent.item.quantity

                oldcount = other.client.pers.inventory[ITEM_INDEX(ent.item)]

                if (!Add_Ammo(other, ent.item, count))
                    return false

                if (weapon && 0 == oldcount) {
                    if (other.client.pers.weapon != ent.item && (0 == GameBase.deathmatch.value || other.client.pers.weapon == FindItem("blaster")))
                        other.client.newweapon = ent.item
                }

                if (0 == (ent.spawnflags and (Defines.DROPPED_ITEM or Defines.DROPPED_PLAYER_ITEM)) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, 30)
                return true
            }
        }
        public var Pickup_Armor: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_armor"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                val old_armor_index: Int
                val oldinfo: gitem_armor_t
                val newinfo: gitem_armor_t
                var newcount: Int
                val salvage: Float
                val salvagecount: Int

                // get info on new armor
                newinfo = ent.item.info as gitem_armor_t

                old_armor_index = ArmorIndex(other)

                // handle armor shards specially
                if (ent.item.tag == Defines.ARMOR_SHARD) {
                    if (0 == old_armor_index)
                        other.client.pers.inventory[jacket_armor_index] = 2
                    else
                        other.client.pers.inventory[old_armor_index] += 2
                } else if (0 == old_armor_index) {
                    other.client.pers.inventory[ITEM_INDEX(ent.item)] = newinfo.base_count
                } else {
                    // get info on old armor
                    if (old_armor_index == jacket_armor_index)
                        oldinfo = jacketarmor_info
                    else if (old_armor_index == combat_armor_index)
                        oldinfo = combatarmor_info
                    else
                    // (old_armor_index == body_armor_index)
                        oldinfo = bodyarmor_info

                    if (newinfo.normal_protection > oldinfo.normal_protection) {
                        // calc new armor values
                        salvage = oldinfo.normal_protection / newinfo.normal_protection
                        salvagecount = salvage.toInt() * other.client.pers.inventory[old_armor_index]
                        newcount = newinfo.base_count + salvagecount
                        if (newcount > newinfo.max_count)
                            newcount = newinfo.max_count

                        // zero count of old armor so it goes away
                        other.client.pers.inventory[old_armor_index] = 0

                        // change armor to new item with computed value
                        other.client.pers.inventory[ITEM_INDEX(ent.item)] = newcount
                    } else {
                        // calc new armor values
                        salvage = newinfo.normal_protection / oldinfo.normal_protection
                        salvagecount = salvage.toInt() * newinfo.base_count
                        newcount = other.client.pers.inventory[old_armor_index] + salvagecount
                        if (newcount > oldinfo.max_count)
                            newcount = oldinfo.max_count

                        // if we're already maxed out then we don't need the new
                        // armor
                        if (other.client.pers.inventory[old_armor_index] >= newcount)
                            return false

                        // update current armor value
                        other.client.pers.inventory[old_armor_index] = newcount
                    }
                }// use the better armor
                // if player has no armor, just use it

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, 20)

                return true
            }
        }
        public var Pickup_PowerArmor: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_powerarmor"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {

                val quantity: Int

                quantity = other.client.pers.inventory[ITEM_INDEX(ent.item)]

                other.client.pers.inventory[ITEM_INDEX(ent.item)]++

                if (GameBase.deathmatch.value != 0) {
                    if (0 == (ent.spawnflags and Defines.DROPPED_ITEM))
                        SetRespawn(ent, ent.item.quantity)
                    // auto-use for DM only if we didn't already have one
                    if (0 == quantity)
                        ent.item.use.use(other, ent.item)
                }
                return true
            }
        }
        public var Pickup_Powerup: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_powerup"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                val quantity: Int

                quantity = other.client.pers.inventory[ITEM_INDEX(ent.item)]
                if ((GameBase.skill.value == 1 && quantity >= 2) || (GameBase.skill.value >= 2 && quantity >= 1))
                    return false

                if ((GameBase.coop.value != 0) && (ent.item.flags and Defines.IT_STAY_COOP) != 0 && (quantity > 0))
                    return false

                other.client.pers.inventory[ITEM_INDEX(ent.item)]++

                if (GameBase.deathmatch.value != 0) {
                    if (0 == (ent.spawnflags and Defines.DROPPED_ITEM))
                        SetRespawn(ent, ent.item.quantity)
                    if ((GameBase.dmflags.value as Int and Defines.DF_INSTANT_ITEMS) != 0 || ((ent.item.use == Use_Quad) && 0 != (ent.spawnflags and Defines.DROPPED_PLAYER_ITEM))) {
                        if ((ent.item.use == Use_Quad) && 0 != (ent.spawnflags and Defines.DROPPED_PLAYER_ITEM))
                            quad_drop_timeout_hack = ((ent.nextthink - GameBase.level.time) / Defines.FRAMETIME) as Int

                        ent.item.use.use(other, ent.item)
                    }
                }

                return true
            }
        }
        public var Pickup_Adrenaline: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_adrenaline"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                if (GameBase.deathmatch.value == 0)
                    other.max_health += 1

                if (other.health < other.max_health)
                    other.health = other.max_health

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, ent.item.quantity)

                return true

            }
        }
        public var Pickup_AncientHead: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_ancienthead"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                other.max_health += 2

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, ent.item.quantity)

                return true
            }
        }
        public var Pickup_Bandolier: EntInteractAdapter = object : EntInteractAdapter() {
            public fun getID(): String {
                return "pickup_bandolier"
            }

            public fun interact(ent: edict_t, other: edict_t): Boolean {
                var item: gitem_t?
                val index: Int

                if (other.client.pers.max_bullets < 250)
                    other.client.pers.max_bullets = 250
                if (other.client.pers.max_shells < 150)
                    other.client.pers.max_shells = 150
                if (other.client.pers.max_cells < 250)
                    other.client.pers.max_cells = 250
                if (other.client.pers.max_slugs < 75)
                    other.client.pers.max_slugs = 75

                item = FindItem("Bullets")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_bullets)
                        other.client.pers.inventory[index] = other.client.pers.max_bullets
                }

                item = FindItem("Shells")
                if (item != null) {
                    index = ITEM_INDEX(item)
                    other.client.pers.inventory[index] += item!!.quantity
                    if (other.client.pers.inventory[index] > other.client.pers.max_shells)
                        other.client.pers.inventory[index] = other.client.pers.max_shells
                }

                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM) && (GameBase.deathmatch.value != 0))
                    SetRespawn(ent, ent.item.quantity)

                return true

            }
        }
        public var Drop_Ammo: ItemDropAdapter = object : ItemDropAdapter() {
            public fun getID(): String {
                return "drop_ammo"
            }

            public fun drop(ent: edict_t, item: gitem_t) {
                val dropped: edict_t
                val index: Int

                index = ITEM_INDEX(item)
                dropped = Drop_Item(ent, item)
                if (ent.client.pers.inventory[index] >= item.quantity)
                    dropped.count = item.quantity
                else
                    dropped.count = ent.client.pers.inventory[index]

                if (ent.client.pers.weapon != null && ent.client.pers.weapon.tag == Defines.AMMO_GRENADES && item.tag == Defines.AMMO_GRENADES && ent.client.pers.inventory[index] - dropped.count <= 0) {
                    GameBase.gi.cprintf(ent, Defines.PRINT_HIGH, "Can't drop current weapon\n")
                    GameUtil.G_FreeEdict(dropped)
                    return
                }

                ent.client.pers.inventory[index] -= dropped.count
                Cmd.ValidateSelectedItem(ent)
            }
        }
        public var Drop_General: ItemDropAdapter = object : ItemDropAdapter() {
            public fun getID(): String {
                return "drop_general"
            }

            public fun drop(ent: edict_t, item: gitem_t) {
                Drop_Item(ent, item)
                ent.client.pers.inventory[ITEM_INDEX(item)]--
                Cmd.ValidateSelectedItem(ent)
            }
        }

        public var Drop_PowerArmor: ItemDropAdapter = object : ItemDropAdapter() {
            public fun getID(): String {
                return "drop_powerarmor"
            }

            public fun drop(ent: edict_t, item: gitem_t) {
                if (0 != (ent.flags and Defines.FL_POWER_ARMOR) && (ent.client.pers.inventory[ITEM_INDEX(item)] == 1))
                    Use_PowerArmor.use(ent, item)
                Drop_General.drop(ent, item)
            }
        }

        public var droptofloor: EntThinkAdapter = object : EntThinkAdapter() {
            public fun getID(): String {
                return "drop_to_floor"
            }

            public fun think(ent: edict_t): Boolean {
                val tr: trace_t
                val dest = floatArray(0.0, 0.0, 0.0)

                //float v[];

                //v = Lib.tv(-15, -15, -15);
                //Math3D.VectorCopy(v, ent.mins);
                ent.mins[0] = ent.mins[1] = ent.mins[2] = -15
                //v = Lib.tv(15, 15, 15);
                //Math3D.VectorCopy(v, ent.maxs);
                ent.maxs[0] = ent.maxs[1] = ent.maxs[2] = 15

                if (ent.model != null)
                    GameBase.gi.setmodel(ent, ent.model)
                else
                    GameBase.gi.setmodel(ent, ent.item.world_model)
                ent.solid = Defines.SOLID_TRIGGER
                ent.movetype = Defines.MOVETYPE_TOSS
                ent.touch = Touch_Item

                val v = floatArray(0.0, 0.0, (-128).toFloat())
                Math3D.VectorAdd(ent.s.origin, v, dest)

                tr = GameBase.gi.trace(ent.s.origin, ent.mins, ent.maxs, dest, ent, Defines.MASK_SOLID)
                if (tr.startsolid) {
                    GameBase.gi.dprintf("droptofloor: " + ent.classname + " startsolid at " + Lib.vtos(ent.s.origin) + "\n")
                    GameUtil.G_FreeEdict(ent)
                    return true
                }

                Math3D.VectorCopy(tr.endpos, ent.s.origin)

                if (ent.team != null) {
                    ent.flags = ent.flags and Defines.FL_TEAMSLAVE.inv()
                    ent.chain = ent.teamchain
                    ent.teamchain = null

                    ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
                    ent.solid = Defines.SOLID_NOT
                    if (ent == ent.teammaster) {
                        ent.nextthink = GameBase.level.time + Defines.FRAMETIME
                        ent.think = DoRespawn
                    }
                }

                if ((ent.spawnflags and Defines.ITEM_NO_TOUCH) != 0) {
                    ent.solid = Defines.SOLID_BBOX
                    ent.touch = null
                    ent.s.effects = ent.s.effects and Defines.EF_ROTATE.inv()
                    ent.s.renderfx = ent.s.renderfx and Defines.RF_GLOW.inv()
                }

                if ((ent.spawnflags and Defines.ITEM_TRIGGER_SPAWN) != 0) {
                    ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
                    ent.solid = Defines.SOLID_NOT
                    ent.use = Use_Item
                }

                GameBase.gi.linkentity(ent)
                return true
            }
        }
        public var Use_PowerArmor: ItemUseAdapter = object : ItemUseAdapter() {
            public fun getID(): String {
                return "use_powerarmor"
            }

            public fun use(ent: edict_t, item: gitem_t) {
                val index: Int

                if ((ent.flags and Defines.FL_POWER_ARMOR) != 0) {
                    ent.flags = ent.flags and Defines.FL_POWER_ARMOR.inv()
                    GameBase.gi.sound(ent, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/power2.wav"), 1, Defines.ATTN_NORM, 0)
                } else {
                    index = ITEM_INDEX(FindItem("cells"))
                    if (0 == ent.client.pers.inventory[index]) {
                        GameBase.gi.cprintf(ent, Defines.PRINT_HIGH, "No cells for power armor.\n")
                        return
                    }
                    ent.flags = ent.flags or Defines.FL_POWER_ARMOR
                    GameBase.gi.sound(ent, Defines.CHAN_AUTO, GameBase.gi.soundindex("misc/power1.wav"), 1, Defines.ATTN_NORM, 0)
                }
            }
        }
        public var Use_Item: EntUseAdapter = object : EntUseAdapter() {
            public fun getID(): String {
                return "use_item"
            }

            public fun use(ent: edict_t, other: edict_t, activator: edict_t) {
                ent.svflags = ent.svflags and Defines.SVF_NOCLIENT.inv()
                ent.use = null

                if ((ent.spawnflags and Defines.ITEM_NO_TOUCH) != 0) {
                    ent.solid = Defines.SOLID_BBOX
                    ent.touch = null
                } else {
                    ent.solid = Defines.SOLID_TRIGGER
                    ent.touch = Touch_Item
                }

                GameBase.gi.linkentity(ent)
            }
        }

        /*
     * =============== GetItemByIndex ===============
     */
        public fun GetItemByIndex(index: Int): gitem_t? {
            if (index == 0 || index >= GameBase.game.num_items)
                return null

            return GameItemList.itemlist[index]
        }

        /*
     * =============== FindItemByClassname
     * 
     * ===============
     */
        fun FindItemByClassname(classname: String): gitem_t? {

            for (i in 1..GameBase.game.num_items - 1) {
                val it = GameItemList.itemlist[i]

                if (it.classname == null)
                    continue
                if (it.classname.equalsIgnoreCase(classname))
                    return it
            }

            return null
        }

        /*
     * =============== FindItem ===============
     */
        //geht.
        fun FindItem(pickup_name: String): gitem_t? {
            for (i in 1..GameBase.game.num_items - 1) {
                val it = GameItemList.itemlist[i]

                if (it.pickup_name == null)
                    continue
                if (it.pickup_name.equalsIgnoreCase(pickup_name))
                    return it
            }
            Com.Println("Item not found:" + pickup_name)
            return null
        }

        fun SetRespawn(ent: edict_t, delay: Float) {
            ent.flags = ent.flags or Defines.FL_RESPAWN
            ent.svflags = ent.svflags or Defines.SVF_NOCLIENT
            ent.solid = Defines.SOLID_NOT
            ent.nextthink = GameBase.level.time + delay
            ent.think = DoRespawn
            GameBase.gi.linkentity(ent)
        }

        fun ITEM_INDEX(item: gitem_t): Int {
            return item.index
        }

        fun Drop_Item(ent: edict_t, item: gitem_t): edict_t {
            val dropped: edict_t
            val forward = floatArray(0.0, 0.0, 0.0)
            val right = floatArray(0.0, 0.0, 0.0)
            val offset = floatArray(0.0, 0.0, 0.0)

            dropped = GameUtil.G_Spawn()

            dropped.classname = item.classname
            dropped.item = item
            dropped.spawnflags = Defines.DROPPED_ITEM
            dropped.s.effects = item.world_model_flags
            dropped.s.renderfx = Defines.RF_GLOW
            Math3D.VectorSet(dropped.mins, -15, -15, -15)
            Math3D.VectorSet(dropped.maxs, 15, 15, 15)
            GameBase.gi.setmodel(dropped, dropped.item.world_model)
            dropped.solid = Defines.SOLID_TRIGGER
            dropped.movetype = Defines.MOVETYPE_TOSS

            dropped.touch = drop_temp_touch

            dropped.owner = ent

            if (ent.client != null) {
                val trace: trace_t

                Math3D.AngleVectors(ent.client.v_angle, forward, right, null)
                Math3D.VectorSet(offset, 24, 0, -16)
                Math3D.G_ProjectSource(ent.s.origin, offset, forward, right, dropped.s.origin)
                trace = GameBase.gi.trace(ent.s.origin, dropped.mins, dropped.maxs, dropped.s.origin, ent, Defines.CONTENTS_SOLID)
                Math3D.VectorCopy(trace.endpos, dropped.s.origin)
            } else {
                Math3D.AngleVectors(ent.s.angles, forward, right, null)
                Math3D.VectorCopy(ent.s.origin, dropped.s.origin)
            }

            Math3D.VectorScale(forward, 100, dropped.velocity)
            dropped.velocity[2] = 300

            dropped.think = drop_make_touchable
            dropped.nextthink = GameBase.level.time + 1

            GameBase.gi.linkentity(dropped)

            return dropped
        }

        fun Use_Item(ent: edict_t, other: edict_t, activator: edict_t) {
            ent.svflags = ent.svflags and Defines.SVF_NOCLIENT.inv()
            ent.use = null

            if ((ent.spawnflags and Defines.ITEM_NO_TOUCH) != 0) {
                ent.solid = Defines.SOLID_BBOX
                ent.touch = null
            } else {
                ent.solid = Defines.SOLID_TRIGGER
                ent.touch = Touch_Item
            }

            GameBase.gi.linkentity(ent)
        }

        fun PowerArmorType(ent: edict_t): Int {
            if (ent.client == null)
                return Defines.POWER_ARMOR_NONE

            if (0 == (ent.flags and Defines.FL_POWER_ARMOR))
                return Defines.POWER_ARMOR_NONE

            if (ent.client.pers.inventory[power_shield_index] > 0)
                return Defines.POWER_ARMOR_SHIELD

            if (ent.client.pers.inventory[power_screen_index] > 0)
                return Defines.POWER_ARMOR_SCREEN

            return Defines.POWER_ARMOR_NONE
        }

        fun ArmorIndex(ent: edict_t): Int {
            if (ent.client == null)
                return 0

            if (ent.client.pers.inventory[jacket_armor_index] > 0)
                return jacket_armor_index

            if (ent.client.pers.inventory[combat_armor_index] > 0)
                return combat_armor_index

            if (ent.client.pers.inventory[body_armor_index] > 0)
                return body_armor_index

            return 0
        }

        public fun Pickup_PowerArmor(ent: edict_t, other: edict_t): Boolean {
            val quantity: Int

            quantity = other.client.pers.inventory[ITEM_INDEX(ent.item)]

            other.client.pers.inventory[ITEM_INDEX(ent.item)]++

            if (GameBase.deathmatch.value != 0) {
                if (0 == (ent.spawnflags and Defines.DROPPED_ITEM))
                    SetRespawn(ent, ent.item.quantity)
                // auto-use for DM only if we didn't already have one
                if (0 == quantity)
                    ent.item.use.use(other, ent.item)
            }

            return true
        }

        public fun Add_Ammo(ent: edict_t, item: gitem_t, count: Int): Boolean {
            val index: Int
            val max: Int

            if (null == ent.client)
                return false

            if (item.tag == Defines.AMMO_BULLETS)
                max = ent.client.pers.max_bullets
            else if (item.tag == Defines.AMMO_SHELLS)
                max = ent.client.pers.max_shells
            else if (item.tag == Defines.AMMO_ROCKETS)
                max = ent.client.pers.max_rockets
            else if (item.tag == Defines.AMMO_GRENADES)
                max = ent.client.pers.max_grenades
            else if (item.tag == Defines.AMMO_CELLS)
                max = ent.client.pers.max_cells
            else if (item.tag == Defines.AMMO_SLUGS)
                max = ent.client.pers.max_slugs
            else
                return false

            index = ITEM_INDEX(item)

            if (ent.client.pers.inventory[index] == max)
                return false

            ent.client.pers.inventory[index] += count

            if (ent.client.pers.inventory[index] > max)
                ent.client.pers.inventory[index] = max

            return true
        }

        public fun InitItems() {
            GameBase.game.num_items = GameItemList.itemlist.length - 1
        }

        /*
     * =============== SetItemNames
     * 
     * Called by worldspawn ===============
     */
        public fun SetItemNames() {
            var i: Int
            var it: gitem_t

            run {
                i = 1
                while (i < GameBase.game.num_items) {
                    it = GameItemList.itemlist[i]
                    GameBase.gi.configstring(Defines.CS_ITEMS + i, it.pickup_name)
                    i++
                }
            }

            jacket_armor_index = ITEM_INDEX(FindItem("Jacket Armor"))
            combat_armor_index = ITEM_INDEX(FindItem("Combat Armor"))
            body_armor_index = ITEM_INDEX(FindItem("Body Armor"))
            power_screen_index = ITEM_INDEX(FindItem("Power Screen"))
            power_shield_index = ITEM_INDEX(FindItem("Power Shield"))
        }

        public fun SelectNextItem(ent: edict_t, itflags: Int) {
            val cl: gclient_t
            var i: Int
            var index: Int
            var it: gitem_t

            cl = ent.client

            if (cl.chase_target != null) {
                GameChase.ChaseNext(ent)
                return
            }

            // scan for the next valid one
            run {
                i = 1
                while (i <= Defines.MAX_ITEMS) {
                    index = (cl.pers.selected_item + i) % Defines.MAX_ITEMS
                    if (0 == cl.pers.inventory[index])
                        continue
                    it = GameItemList.itemlist[index]
                    if (it.use == null)
                        continue
                    if (0 == (it.flags and itflags))
                        continue

                    cl.pers.selected_item = index
                    return
                    i++
                }
            }

            cl.pers.selected_item = -1
        }

        public fun SelectPrevItem(ent: edict_t, itflags: Int) {
            val cl: gclient_t
            var i: Int
            var index: Int
            var it: gitem_t

            cl = ent.client

            if (cl.chase_target != null) {
                GameChase.ChasePrev(ent)
                return
            }

            // scan for the next valid one
            run {
                i = 1
                while (i <= Defines.MAX_ITEMS) {
                    index = (cl.pers.selected_item + Defines.MAX_ITEMS - i) % Defines.MAX_ITEMS
                    if (0 == cl.pers.inventory[index])
                        continue
                    it = GameItemList.itemlist[index]
                    if (null == it.use)
                        continue
                    if (0 == (it.flags and itflags))
                        continue

                    cl.pers.selected_item = index
                    return
                    i++
                }
            }

            cl.pers.selected_item = -1
        }

        /*
     * =============== PrecacheItem
     * 
     * Precaches all data needed for a given item. This will be called for each
     * item spawned in a level, and for each item in each client's inventory.
     * ===============
     */
        public fun PrecacheItem(it: gitem_t?) {
            val s: String?
            val data: String
            val len: Int
            val ammo: gitem_t

            if (it == null)
                return

            if (it!!.pickup_sound != null)
                GameBase.gi.soundindex(it!!.pickup_sound)

            if (it!!.world_model != null)
                GameBase.gi.modelindex(it!!.world_model)

            if (it!!.view_model != null)
                GameBase.gi.modelindex(it!!.view_model)

            if (it!!.icon != null)
                GameBase.gi.imageindex(it!!.icon)

            // parse everything for its ammo
            if (it!!.ammo != null && it!!.ammo.length() != 0) {
                ammo = FindItem(it!!.ammo)
                if (ammo != it)
                    PrecacheItem(ammo)
            }

            // parse the space seperated precache string for other items
            s = it!!.precaches
            if (s == null || s.length() != 0)
                return

            val tk = StringTokenizer(s)

            while (tk.hasMoreTokens()) {
                data = tk.nextToken()

                len = data.length()

                if (len >= Defines.MAX_QPATH || len < 5)
                    GameBase.gi.error("PrecacheItem: it.classname has bad precache string: " + s)

                // determine type based on extension
                if (data.endsWith("md2"))
                    GameBase.gi.modelindex(data)
                else if (data.endsWith("sp2"))
                    GameBase.gi.modelindex(data)
                else if (data.endsWith("wav"))
                    GameBase.gi.soundindex(data)
                else if (data.endsWith("pcx"))
                    GameBase.gi.imageindex(data)
                else
                    GameBase.gi.error("PrecacheItem: bad precache string: " + data)
            }
        }

        /*
     * ============ SpawnItem
     * 
     * Sets the clipping size and plants the object on the floor.
     * 
     * Items can't be immediately dropped to floor, because they might be on an
     * entity that hasn't spawned yet. ============
     */
        public fun SpawnItem(ent: edict_t, item: gitem_t) {
            PrecacheItem(item)

            if (ent.spawnflags != 0) {
                if (Lib.strcmp(ent.classname, "key_power_cube") != 0) {
                    ent.spawnflags = 0
                    GameBase.gi.dprintf("" + ent.classname + " at " + Lib.vtos(ent.s.origin) + " has invalid spawnflags set\n")
                }
            }

            // some items will be prevented in deathmatch
            if (GameBase.deathmatch.value != 0) {
                if ((GameBase.dmflags.value as Int and Defines.DF_NO_ARMOR) != 0) {
                    if (item.pickup == Pickup_Armor || item.pickup == Pickup_PowerArmor) {
                        GameUtil.G_FreeEdict(ent)
                        return
                    }
                }
                if ((GameBase.dmflags.value as Int and Defines.DF_NO_ITEMS) != 0) {
                    if (item.pickup == Pickup_Powerup) {
                        GameUtil.G_FreeEdict(ent)
                        return
                    }
                }
                if ((GameBase.dmflags.value as Int and Defines.DF_NO_HEALTH) != 0) {
                    if (item.pickup == Pickup_Health || item.pickup == Pickup_Adrenaline || item.pickup == Pickup_AncientHead) {
                        GameUtil.G_FreeEdict(ent)
                        return
                    }
                }
                if ((GameBase.dmflags.value as Int and Defines.DF_INFINITE_AMMO) != 0) {
                    if ((item.flags == Defines.IT_AMMO) || (Lib.strcmp(ent.classname, "weapon_bfg") == 0)) {
                        GameUtil.G_FreeEdict(ent)
                        return
                    }
                }
            }

            if (GameBase.coop.value != 0 && (Lib.strcmp(ent.classname, "key_power_cube") == 0)) {
                ent.spawnflags = ent.spawnflags or (1 shl (8 + GameBase.level.power_cubes))
                GameBase.level.power_cubes++
            }

            // don't let them drop items that stay in a coop game
            if ((GameBase.coop.value != 0) && (item.flags and Defines.IT_STAY_COOP) != 0) {
                item.drop = null
            }

            ent.item = item
            ent.nextthink = GameBase.level.time + 2 * Defines.FRAMETIME
            // items start after other solids
            ent.think = droptofloor
            ent.s.effects = item.world_model_flags
            ent.s.renderfx = Defines.RF_GLOW

            if (ent.model != null)
                GameBase.gi.modelindex(ent.model)
        }

        /*
     * QUAKED item_health (.3 .3 1) (-16 -16 -16) (16 16 16)
     */
        public fun SP_item_health(self: edict_t) {
            if (GameBase.deathmatch.value != 0 && (GameBase.dmflags.value as Int and Defines.DF_NO_HEALTH) != 0) {
                GameUtil.G_FreeEdict(self)
            }

            self.model = "models/items/healing/medium/tris.md2"
            self.count = 10
            SpawnItem(self, FindItem("Health"))
            GameBase.gi.soundindex("items/n_health.wav")
        }

        /*
     * QUAKED item_health_small (.3 .3 1) (-16 -16 -16) (16 16 16)
     */
        fun SP_item_health_small(self: edict_t) {
            if (GameBase.deathmatch.value != 0 && (GameBase.dmflags.value as Int and Defines.DF_NO_HEALTH) != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            self.model = "models/items/healing/stimpack/tris.md2"
            self.count = 2
            SpawnItem(self, FindItem("Health"))
            self.style = Defines.HEALTH_IGNORE_MAX
            GameBase.gi.soundindex("items/s_health.wav")
        }

        /*
     * QUAKED item_health_large (.3 .3 1) (-16 -16 -16) (16 16 16)
     */
        fun SP_item_health_large(self: edict_t) {
            if (GameBase.deathmatch.value != 0 && (GameBase.dmflags.value as Int and Defines.DF_NO_HEALTH) != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            self.model = "models/items/healing/large/tris.md2"
            self.count = 25
            SpawnItem(self, FindItem("Health"))
            GameBase.gi.soundindex("items/l_health.wav")
        }

        /*
     * QUAKED item_health_mega (.3 .3 1) (-16 -16 -16) (16 16 16)
     */
        fun SP_item_health_mega(self: edict_t) {
            if (GameBase.deathmatch.value != 0 && (GameBase.dmflags.value as Int and Defines.DF_NO_HEALTH) != 0) {
                GameUtil.G_FreeEdict(self)
                return
            }

            self.model = "models/items/mega_h/tris.md2"
            self.count = 100
            SpawnItem(self, FindItem("Health"))
            GameBase.gi.soundindex("items/m_health.wav")
            self.style = Defines.HEALTH_IGNORE_MAX or Defines.HEALTH_TIMED
        }

        /*
     * =============== 
     * Touch_Item 
     * ===============
     */
        public fun Touch_Item(ent: edict_t, other: edict_t, plane: cplane_t, surf: csurface_t) {
            val taken: Boolean

            // freed edicts have not items.
            if (other.client == null || ent.item == null)
                return
            if (other.health < 1)
                return  // dead people can't pickup
            if (ent.item.pickup == null)
                return  // not a grabbable item?

            taken = ent.item.pickup.interact(ent, other)

            if (taken) {
                // flash the screen
                other.client.bonus_alpha = 0.25.toFloat()

                // show icon and name on status bar
                other.client.ps.stats[Defines.STAT_PICKUP_ICON] = GameBase.gi.imageindex(ent.item.icon) as Short
                other.client.ps.stats[Defines.STAT_PICKUP_STRING] = (Defines.CS_ITEMS + ITEM_INDEX(ent.item)) as Short
                other.client.pickup_msg_time = GameBase.level.time + 3.0.toFloat()

                // change selected item
                if (ent.item.use != null)
                    other.client.pers.selected_item = other.client.ps.stats[Defines.STAT_SELECTED_ITEM] = ITEM_INDEX(ent.item).toShort()

                if (ent.item.pickup == Pickup_Health) {
                    if (ent.count == 2)
                        GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/s_health.wav"), 1, Defines.ATTN_NORM, 0)
                    else if (ent.count == 10)
                        GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/n_health.wav"), 1, Defines.ATTN_NORM, 0)
                    else if (ent.count == 25)
                        GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/l_health.wav"), 1, Defines.ATTN_NORM, 0)
                    else
                    // (ent.count == 100)
                        GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex("items/m_health.wav"), 1, Defines.ATTN_NORM, 0)
                } else if (ent.item.pickup_sound != null) {
                    GameBase.gi.sound(other, Defines.CHAN_ITEM, GameBase.gi.soundindex(ent.item.pickup_sound), 1, Defines.ATTN_NORM, 0)
                }
            }

            if (0 == (ent.spawnflags and Defines.ITEM_TARGETS_USED)) {
                GameUtil.G_UseTargets(ent, other)
                ent.spawnflags = ent.spawnflags or Defines.ITEM_TARGETS_USED
            }

            if (!taken)
                return

            if (!((GameBase.coop.value != 0) && (ent.item.flags and Defines.IT_STAY_COOP) != 0) || 0 != (ent.spawnflags and (Defines.DROPPED_ITEM or Defines.DROPPED_PLAYER_ITEM))) {
                if ((ent.flags and Defines.FL_RESPAWN) != 0)
                    ent.flags = ent.flags and Defines.FL_RESPAWN.inv()
                else
                    GameUtil.G_FreeEdict(ent)
            }
        }
    }

}
