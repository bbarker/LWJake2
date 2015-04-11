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

public class gclient_t(public var index: Int) {
    //	this structure is cleared on each PutClientInServer(),
    //	except for 'client->pers'

    // known to server
    public var ps: player_state_t = player_state_t() // communicated by server to clients
    public var ping: Int = 0

    // private to game
    public var pers: client_persistant_t = client_persistant_t()
    public var resp: client_respawn_t = client_respawn_t()
    public var old_pmove: pmove_state_t = pmove_state_t() // for detecting out-of-pmove changes

    public var showscores: Boolean = false // set layout stat
    public var showinventory: Boolean = false // set layout stat
    public var showhelp: Boolean = false
    public var showhelpicon: Boolean = false

    public var ammo_index: Int = 0

    public var buttons: Int = 0
    public var oldbuttons: Int = 0
    public var latched_buttons: Int = 0

    public var weapon_thunk: Boolean = false

    public var newweapon: gitem_t? = null

    // sum up damage over an entire frame, so
    // shotgun blasts give a single big kick
    public var damage_armor: Int = 0 // damage absorbed by armor
    public var damage_parmor: Int = 0 // damage absorbed by power armor
    public var damage_blood: Int = 0 // damage taken out of health
    public var damage_knockback: Int = 0 // impact damage
    public var damage_from: FloatArray = floatArray(0.0, 0.0, 0.0) // origin for vector calculation

    public var killer_yaw: Float = 0.toFloat() // when dead, look at killer

    public var weaponstate: Int = 0
    public var kick_angles: FloatArray = floatArray(0.0, 0.0, 0.0) // weapon kicks
    public var kick_origin: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var v_dmg_roll: Float = 0.toFloat()
    public var v_dmg_pitch: Float = 0.toFloat()
    public var v_dmg_time: Float = 0.toFloat() // damage kicks
    public var fall_time: Float = 0.toFloat()
    public var fall_value: Float = 0.toFloat() // for view drop on fall
    public var damage_alpha: Float = 0.toFloat()
    public var bonus_alpha: Float = 0.toFloat()
    public var damage_blend: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var v_angle: FloatArray = floatArray(0.0, 0.0, 0.0) // aiming direction
    public var bobtime: Float = 0.toFloat() // so off-ground doesn't change it
    public var oldviewangles: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var oldvelocity: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var next_drown_time: Float = 0.toFloat()
    public var old_waterlevel: Int = 0
    public var breather_sound: Int = 0

    public var machinegun_shots: Int = 0 // for weapon raising

    // animation vars
    public var anim_end: Int = 0
    public var anim_priority: Int = 0
    public var anim_duck: Boolean = false
    public var anim_run: Boolean = false

    // powerup timers
    public var quad_framenum: Float = 0.toFloat()
    public var invincible_framenum: Float = 0.toFloat()
    public var breather_framenum: Float = 0.toFloat()
    public var enviro_framenum: Float = 0.toFloat()

    public var grenade_blew_up: Boolean = false
    public var grenade_time: Float = 0.toFloat()
    public var silencer_shots: Int = 0
    public var weapon_sound: Int = 0

    public var pickup_msg_time: Float = 0.toFloat()

    public var flood_locktill: Float = 0.toFloat() // locked from talking
    public var flood_when: FloatArray = FloatArray(10) // when messages were said
    public var flood_whenhead: Int = 0 // head pointer for when said

    public var respawn_time: Float = 0.toFloat() // can respawn when time > this

    public var chase_target: edict_t? = null // player we are chasing
    public var update_chase: Boolean = false // need to update chase info?

    /** Clears the game client structure.  */
    public fun clear() {
        ping = 0

        pers = client_persistant_t()
        resp = client_respawn_t()
        old_pmove = pmove_state_t()

        showscores = false // set layout stat
        showinventory = false // set layout stat
        showhelp = false
        showhelpicon = false

        ammo_index = 0

        buttons = oldbuttons = latched_buttons = 0
        weapon_thunk = false
        newweapon = null
        damage_armor = 0
        damage_parmor = 0
        damage_blood = 0
        damage_knockback = 0

        killer_yaw = 0
        damage_from = FloatArray(3)
        weaponstate = 0
        kick_angles = FloatArray(3)
        kick_origin = FloatArray(3)
        v_dmg_roll = v_dmg_pitch = v_dmg_time = 0
        fall_time = fall_value = 0
        damage_alpha = 0
        bonus_alpha = 0
        damage_blend = FloatArray(3)
        v_angle = FloatArray(3)
        bobtime = 0

        oldviewangles = FloatArray(3)

        oldvelocity = FloatArray(3)

        next_drown_time = 0

        old_waterlevel = 0

        breather_sound = 0
        machinegun_shots = 0

        anim_end = 0
        anim_priority = 0
        anim_duck = false
        anim_run = false

        // powerup timers
        quad_framenum = 0
        invincible_framenum = 0
        breather_framenum = 0
        enviro_framenum = 0

        grenade_blew_up = false
        grenade_time = 0
        silencer_shots = 0
        weapon_sound = 0

        pickup_msg_time = 0

        flood_locktill = 0 // locked from talking
        flood_when = FloatArray(10) // when messages were said
        flood_whenhead = 0 // head pointer for when said

        respawn_time = 0 // can respawn when time > this

        chase_target = null // player we are chasing
        update_chase = false // need to update chase info?
    }

    /** Reads a game client from the file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {

        ps.load(f)

        ping = f.readInt()

        pers.read(f)
        resp.read(f)

        old_pmove.load(f)

        showscores = f.readInt() != 0
        showinventory = f.readInt() != 0
        showhelp = f.readInt() != 0
        showhelpicon = f.readInt() != 0
        ammo_index = f.readInt()

        buttons = f.readInt()
        oldbuttons = f.readInt()
        latched_buttons = f.readInt()

        weapon_thunk = f.readInt() != 0

        newweapon = f.readItem()


        damage_armor = f.readInt()
        damage_parmor = f.readInt()
        damage_blood = f.readInt()
        damage_knockback = f.readInt()

        damage_from[0] = f.readFloat()
        damage_from[1] = f.readFloat()
        damage_from[2] = f.readFloat()

        killer_yaw = f.readFloat()

        weaponstate = f.readInt()

        kick_angles[0] = f.readFloat()
        kick_angles[1] = f.readFloat()
        kick_angles[2] = f.readFloat()

        kick_origin[0] = f.readFloat()
        kick_origin[1] = f.readFloat()
        kick_origin[2] = f.readFloat()

        v_dmg_roll = f.readFloat()
        v_dmg_pitch = f.readFloat()
        v_dmg_time = f.readFloat()
        fall_time = f.readFloat()
        fall_value = f.readFloat()
        damage_alpha = f.readFloat()
        bonus_alpha = f.readFloat()

        damage_blend[0] = f.readFloat()
        damage_blend[1] = f.readFloat()
        damage_blend[2] = f.readFloat()

        v_angle[0] = f.readFloat()
        v_angle[1] = f.readFloat()
        v_angle[2] = f.readFloat()

        bobtime = f.readFloat()

        oldviewangles[0] = f.readFloat()
        oldviewangles[1] = f.readFloat()
        oldviewangles[2] = f.readFloat()

        oldvelocity[0] = f.readFloat()
        oldvelocity[1] = f.readFloat()
        oldvelocity[2] = f.readFloat()

        next_drown_time = f.readFloat()

        old_waterlevel = f.readInt()
        breather_sound = f.readInt()
        machinegun_shots = f.readInt()
        anim_end = f.readInt()
        anim_priority = f.readInt()
        anim_duck = f.readInt() != 0
        anim_run = f.readInt() != 0

        quad_framenum = f.readFloat()
        invincible_framenum = f.readFloat()
        breather_framenum = f.readFloat()
        enviro_framenum = f.readFloat()

        grenade_blew_up = f.readInt() != 0
        grenade_time = f.readFloat()
        silencer_shots = f.readInt()
        weapon_sound = f.readInt()
        pickup_msg_time = f.readFloat()
        flood_locktill = f.readFloat()
        flood_when[0] = f.readFloat()
        flood_when[1] = f.readFloat()
        flood_when[2] = f.readFloat()
        flood_when[3] = f.readFloat()
        flood_when[4] = f.readFloat()
        flood_when[5] = f.readFloat()
        flood_when[6] = f.readFloat()
        flood_when[7] = f.readFloat()
        flood_when[8] = f.readFloat()
        flood_when[9] = f.readFloat()
        flood_whenhead = f.readInt()
        respawn_time = f.readFloat()
        chase_target = f.readEdictRef()
        update_chase = f.readInt() != 0

        if (f.readInt() != 8765)
            System.err.println("game client load failed for num=" + index)
    }

    /** Writes a game_client_t (a player) to a file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        ps.write(f)

        f.writeInt(ping)

        pers.write(f)
        resp.write(f)

        old_pmove.write(f)

        f.writeInt(if (showscores) 1 else 0)
        f.writeInt(if (showinventory) 1 else 0)
        f.writeInt(if (showhelp) 1 else 0)
        f.writeInt(if (showhelpicon) 1 else 0)
        f.writeInt(ammo_index)

        f.writeInt(buttons)
        f.writeInt(oldbuttons)
        f.writeInt(latched_buttons)

        f.writeInt(if (weapon_thunk) 1 else 0)
        f.writeItem(newweapon)


        f.writeInt(damage_armor)
        f.writeInt(damage_parmor)
        f.writeInt(damage_blood)
        f.writeInt(damage_knockback)

        f.writeFloat(damage_from[0])
        f.writeFloat(damage_from[1])
        f.writeFloat(damage_from[2])

        f.writeFloat(killer_yaw)

        f.writeInt(weaponstate)

        f.writeFloat(kick_angles[0])
        f.writeFloat(kick_angles[1])
        f.writeFloat(kick_angles[2])

        f.writeFloat(kick_origin[0])
        f.writeFloat(kick_origin[1])
        f.writeFloat(kick_origin[2])

        f.writeFloat(v_dmg_roll)
        f.writeFloat(v_dmg_pitch)
        f.writeFloat(v_dmg_time)
        f.writeFloat(fall_time)
        f.writeFloat(fall_value)
        f.writeFloat(damage_alpha)
        f.writeFloat(bonus_alpha)

        f.writeFloat(damage_blend[0])
        f.writeFloat(damage_blend[1])
        f.writeFloat(damage_blend[2])

        f.writeFloat(v_angle[0])
        f.writeFloat(v_angle[1])
        f.writeFloat(v_angle[2])

        f.writeFloat(bobtime)

        f.writeFloat(oldviewangles[0])
        f.writeFloat(oldviewangles[1])
        f.writeFloat(oldviewangles[2])

        f.writeFloat(oldvelocity[0])
        f.writeFloat(oldvelocity[1])
        f.writeFloat(oldvelocity[2])

        f.writeFloat(next_drown_time)

        f.writeInt(old_waterlevel)
        f.writeInt(breather_sound)
        f.writeInt(machinegun_shots)
        f.writeInt(anim_end)
        f.writeInt(anim_priority)
        f.writeInt(if (anim_duck) 1 else 0)
        f.writeInt(if (anim_run) 1 else 0)

        f.writeFloat(quad_framenum)
        f.writeFloat(invincible_framenum)
        f.writeFloat(breather_framenum)
        f.writeFloat(enviro_framenum)

        f.writeInt(if (grenade_blew_up) 1 else 0)
        f.writeFloat(grenade_time)
        f.writeInt(silencer_shots)
        f.writeInt(weapon_sound)
        f.writeFloat(pickup_msg_time)
        f.writeFloat(flood_locktill)
        f.writeFloat(flood_when[0])
        f.writeFloat(flood_when[1])
        f.writeFloat(flood_when[2])
        f.writeFloat(flood_when[3])
        f.writeFloat(flood_when[4])
        f.writeFloat(flood_when[5])
        f.writeFloat(flood_when[6])
        f.writeFloat(flood_when[7])
        f.writeFloat(flood_when[8])
        f.writeFloat(flood_when[9])
        f.writeInt(flood_whenhead)
        f.writeFloat(respawn_time)
        f.writeEdictRef(chase_target)
        f.writeInt(if (update_chase) 1 else 0)

        f.writeInt(8765)
    }
}
