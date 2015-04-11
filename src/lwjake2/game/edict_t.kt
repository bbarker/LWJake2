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
import lwjake2.util.QuakeFile

import java.io.IOException

public class edict_t
/** Constructor.  */
(
        /** Introduced by rst.  */
        public var index: Int) {

    {
        s.number = index
    }

    /** Used during level loading.  */
    public fun cleararealinks() {
        area = link_t(this)
    }

    /** Integrated entity state.  */
    public var s: entity_state_t = entity_state_t(this)

    public var inuse: Boolean = false

    public var linkcount: Int = 0

    /**
     * FIXME: move these fields to a server private sv_entity_t. linked to a
     * division node or leaf.
     */
    public var area: link_t = link_t(this)

    /** if -1, use headnode instead.  */
    public var num_clusters: Int = 0

    public var clusternums: IntArray? = IntArray(Defines.MAX_ENT_CLUSTERS)

    /** unused if num_clusters != -1.  */
    public var headnode: Int = 0

    public var areanum: Int = 0
    public var areanum2: Int = 0

    //================================

    /** SVF_NOCLIENT, SVF_DEADMONSTER, SVF_MONSTER, etc.  */
    public var svflags: Int = 0

    public var mins: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var maxs: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var absmin: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var absmax: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var size: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var solid: Int = 0

    public var clipmask: Int = 0

    //================================
    public var movetype: Int = 0

    public var flags: Int = 0

    public var model: String? = null

    /** sv.time when the object was freed.  */
    public var freetime: Float = 0.toFloat()

    //
    // only used locally in game, not by server
    //
    public var message: String? = null

    public var classname: String = ""

    public var spawnflags: Int = 0

    public var timestamp: Float = 0.toFloat()

    /** set in qe3, -1 = up, -2 = down  */
    public var angle: Float = 0.toFloat()

    public var target: String? = null

    public var targetname: String? = null

    public var killtarget: String? = null

    public var team: String? = null

    public var pathtarget: String? = null

    public var deathtarget: String? = null

    public var combattarget: String? = null

    public var target_ent: edict_t? = null

    public var speed: Float = 0.toFloat()
    public var accel: Float = 0.toFloat()
    public var decel: Float = 0.toFloat()

    public var movedir: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var pos1: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var pos2: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var velocity: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var avelocity: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var mass: Int = 0

    public var air_finished: Float = 0.toFloat()

    /** per entity gravity multiplier (1.0 is normal).  */
    public var gravity: Float = 0.toFloat()

    /** use for lowgrav artifact, flares.  */

    public var goalentity: edict_t? = null

    public var movetarget: edict_t? = null

    public var yaw_speed: Float = 0.toFloat()

    public var ideal_yaw: Float = 0.toFloat()

    public var nextthink: Float = 0.toFloat()

    public var prethink: EntThinkAdapter? = null

    public var think: EntThinkAdapter? = null

    public var blocked: EntBlockedAdapter? = null

    public var touch: EntTouchAdapter? = null

    public var use: EntUseAdapter? = null

    public var pain: EntPainAdapter? = null

    public var die: EntDieAdapter? = null

    /** Are all these legit? do we need more/less of them?  */
    public var touch_debounce_time: Float = 0.toFloat()

    public var pain_debounce_time: Float = 0.toFloat()

    public var damage_debounce_time: Float = 0.toFloat()

    /** Move to clientinfo.  */
    public var fly_sound_debounce_time: Float = 0.toFloat()

    public var last_move_time: Float = 0.toFloat()

    public var health: Int = 0

    public var max_health: Int = 0

    public var gib_health: Int = 0

    public var deadflag: Int = 0

    public var show_hostile: Int = 0

    public var powerarmor_time: Float = 0.toFloat()

    /** target_changelevel.  */
    public var map: String? = null

    /** Height above origin where eyesight is determined.  */
    public var viewheight: Int = 0

    public var takedamage: Int = 0

    public var dmg: Int = 0

    public var radius_dmg: Int = 0

    public var dmg_radius: Float = 0.toFloat()

    /** make this a spawntemp var?  */
    public var sounds: Int = 0

    public var count: Int = 0

    public var chain: edict_t? = null

    public var enemy: edict_t? = null

    public var oldenemy: edict_t? = null

    public var activator: edict_t? = null

    public var groundentity: edict_t? = null

    public var groundentity_linkcount: Int = 0

    public var teamchain: edict_t? = null

    public var teammaster: edict_t? = null

    /** can go in client only.  */
    public var mynoise: edict_t? = null

    public var mynoise2: edict_t? = null

    public var noise_index: Int = 0

    public var noise_index2: Int = 0

    public var volume: Float = 0.toFloat()

    public var attenuation: Float = 0.toFloat()

    /** Timing variables.  */
    public var wait: Float = 0.toFloat()

    /** before firing targets...  */
    public var delay: Float = 0.toFloat()

    public var random: Float = 0.toFloat()

    public var teleport_time: Float = 0.toFloat()

    public var watertype: Int = 0

    public var waterlevel: Int = 0

    public var move_origin: FloatArray = floatArray(0.0, 0.0, 0.0)

    public var move_angles: FloatArray = floatArray(0.0, 0.0, 0.0)

    /** move this to clientinfo? .  */
    public var light_level: Int = 0

    /** also used as areaportal number.  */
    public var style: Int = 0

    public var item: gitem_t // for bonus items

    /** common integrated data blocks.  */
    public var moveinfo: moveinfo_t = moveinfo_t()

    public var monsterinfo: monsterinfo_t = monsterinfo_t()

    public var client: gclient_t? = null

    public var owner: edict_t

    /////////////////////////////////////////////////

    public fun setField(key: String, value: String): Boolean {

        if (key.equals("classname")) {
            classname = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("model")) {
            model = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("spawnflags")) {
            spawnflags = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("speed")) {
            speed = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("accel")) {
            accel = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("decel")) {
            decel = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("target")) {
            target = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("targetname")) {
            targetname = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("pathtarget")) {
            pathtarget = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("deathtarget")) {
            deathtarget = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),
        if (key.equals("killtarget")) {
            killtarget = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("combattarget")) {
            combattarget = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("message")) {
            message = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("team")) {
            team = GameSpawn.ED_NewString(value)
            Com.dprintln("Monster Team:" + team)
            return true
        } // F_LSTRING),

        if (key.equals("wait")) {
            wait = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("delay")) {
            delay = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("random")) {
            random = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("move_origin")) {
            move_origin = Lib.atov(value)
            return true
        } // F_VECTOR),

        if (key.equals("move_angles")) {
            move_angles = Lib.atov(value)
            return true
        } // F_VECTOR),

        if (key.equals("style")) {
            style = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("count")) {
            count = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("health")) {
            health = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("sounds")) {
            sounds = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("light")) {
            return true
        } // F_IGNORE),

        if (key.equals("dmg")) {
            dmg = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("mass")) {
            mass = Lib.atoi(value)
            return true
        } // F_INT),

        if (key.equals("volume")) {
            volume = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("attenuation")) {
            attenuation = Lib.atof(value)
            return true
        } // F_FLOAT),

        if (key.equals("map")) {
            map = GameSpawn.ED_NewString(value)
            return true
        } // F_LSTRING),

        if (key.equals("origin")) {
            s.origin = Lib.atov(value)
            return true
        } // F_VECTOR),

        if (key.equals("angles")) {
            s.angles = Lib.atov(value)
            return true
        } // F_VECTOR),

        if (key.equals("angle")) {
            s.angles = floatArray(0.0, Lib.atof(value).toFloat(), 0.0)
            return true
        } // F_ANGLEHACK),

        if (key.equals("item")) {
            GameBase.gi.error("ent.set(\"item\") called.")
            return true
        } // F_ITEM)

        return false
    }

    /** Writes the entity to the file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {

        s.write(f)
        f.writeBoolean(inuse)
        f.writeInt(linkcount)
        f.writeInt(num_clusters)

        f.writeInt(9999)

        if (clusternums == null)
            f.writeInt(-1)
        else {
            f.writeInt(Defines.MAX_ENT_CLUSTERS)
            for (n in 0..Defines.MAX_ENT_CLUSTERS - 1)
                f.writeInt(clusternums!![n])

        }
        f.writeInt(headnode)
        f.writeInt(areanum)
        f.writeInt(areanum2)
        f.writeInt(svflags)
        f.writeVector(mins)
        f.writeVector(maxs)
        f.writeVector(absmin)
        f.writeVector(absmax)
        f.writeVector(size)
        f.writeInt(solid)
        f.writeInt(clipmask)

        f.writeInt(movetype)
        f.writeInt(flags)

        f.writeString(model)
        f.writeFloat(freetime)
        f.writeString(message)
        f.writeString(classname)
        f.writeInt(spawnflags)
        f.writeFloat(timestamp)

        f.writeFloat(angle)

        f.writeString(target)
        f.writeString(targetname)
        f.writeString(killtarget)
        f.writeString(team)
        f.writeString(pathtarget)
        f.writeString(deathtarget)
        f.writeString(combattarget)

        f.writeEdictRef(target_ent)

        f.writeFloat(speed)
        f.writeFloat(accel)
        f.writeFloat(decel)

        f.writeVector(movedir)

        f.writeVector(pos1)
        f.writeVector(pos2)

        f.writeVector(velocity)
        f.writeVector(avelocity)

        f.writeInt(mass)
        f.writeFloat(air_finished)

        f.writeFloat(gravity)

        f.writeEdictRef(goalentity)
        f.writeEdictRef(movetarget)

        f.writeFloat(yaw_speed)
        f.writeFloat(ideal_yaw)

        f.writeFloat(nextthink)

        f.writeAdapter(prethink)
        f.writeAdapter(think)
        f.writeAdapter(blocked)

        f.writeAdapter(touch)
        f.writeAdapter(use)
        f.writeAdapter(pain)
        f.writeAdapter(die)

        f.writeFloat(touch_debounce_time)
        f.writeFloat(pain_debounce_time)
        f.writeFloat(damage_debounce_time)

        f.writeFloat(fly_sound_debounce_time)
        f.writeFloat(last_move_time)

        f.writeInt(health)
        f.writeInt(max_health)

        f.writeInt(gib_health)
        f.writeInt(deadflag)
        f.writeInt(show_hostile)

        f.writeFloat(powerarmor_time)

        f.writeString(map)

        f.writeInt(viewheight)
        f.writeInt(takedamage)
        f.writeInt(dmg)
        f.writeInt(radius_dmg)
        f.writeFloat(dmg_radius)

        f.writeInt(sounds)
        f.writeInt(count)

        f.writeEdictRef(chain)
        f.writeEdictRef(enemy)
        f.writeEdictRef(oldenemy)
        f.writeEdictRef(activator)
        f.writeEdictRef(groundentity)
        f.writeInt(groundentity_linkcount)
        f.writeEdictRef(teamchain)
        f.writeEdictRef(teammaster)

        f.writeEdictRef(mynoise)
        f.writeEdictRef(mynoise2)

        f.writeInt(noise_index)
        f.writeInt(noise_index2)

        f.writeFloat(volume)
        f.writeFloat(attenuation)
        f.writeFloat(wait)
        f.writeFloat(delay)
        f.writeFloat(random)

        f.writeFloat(teleport_time)

        f.writeInt(watertype)
        f.writeInt(waterlevel)
        f.writeVector(move_origin)
        f.writeVector(move_angles)

        f.writeInt(light_level)
        f.writeInt(style)

        f.writeItem(item)

        moveinfo.write(f)
        monsterinfo.write(f)
        if (client == null)
            f.writeInt(-1)
        else
            f.writeInt(client!!.index)

        f.writeEdictRef(owner)

        // rst's checker :-)
        f.writeInt(9876)
    }

    /** Reads the entity from the file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        s.read(f)
        inuse = f.readBoolean()
        linkcount = f.readInt()
        num_clusters = f.readInt()

        if (f.readInt() != 9999)
            Throwable("wrong read pos!").printStackTrace()

        val len = f.readInt()

        if (len == -1)
            clusternums = null
        else {
            clusternums = IntArray(Defines.MAX_ENT_CLUSTERS)
            for (n in 0..Defines.MAX_ENT_CLUSTERS - 1)
                clusternums[n] = f.readInt()
        }

        headnode = f.readInt()
        areanum = f.readInt()
        areanum2 = f.readInt()
        svflags = f.readInt()
        mins = f.readVector()
        maxs = f.readVector()
        absmin = f.readVector()
        absmax = f.readVector()
        size = f.readVector()
        solid = f.readInt()
        clipmask = f.readInt()

        movetype = f.readInt()
        flags = f.readInt()

        model = f.readString()
        freetime = f.readFloat()
        message = f.readString()
        classname = f.readString()
        spawnflags = f.readInt()
        timestamp = f.readFloat()

        angle = f.readFloat()

        target = f.readString()
        targetname = f.readString()
        killtarget = f.readString()
        team = f.readString()
        pathtarget = f.readString()
        deathtarget = f.readString()
        combattarget = f.readString()

        target_ent = f.readEdictRef()

        speed = f.readFloat()
        accel = f.readFloat()
        decel = f.readFloat()

        movedir = f.readVector()

        pos1 = f.readVector()
        pos2 = f.readVector()

        velocity = f.readVector()
        avelocity = f.readVector()

        mass = f.readInt()
        air_finished = f.readFloat()

        gravity = f.readFloat()

        goalentity = f.readEdictRef()
        movetarget = f.readEdictRef()

        yaw_speed = f.readFloat()
        ideal_yaw = f.readFloat()

        nextthink = f.readFloat()

        prethink = f.readAdapter() as EntThinkAdapter
        think = f.readAdapter() as EntThinkAdapter
        blocked = f.readAdapter() as EntBlockedAdapter

        touch = f.readAdapter() as EntTouchAdapter
        use = f.readAdapter() as EntUseAdapter
        pain = f.readAdapter() as EntPainAdapter
        die = f.readAdapter() as EntDieAdapter

        touch_debounce_time = f.readFloat()
        pain_debounce_time = f.readFloat()
        damage_debounce_time = f.readFloat()

        fly_sound_debounce_time = f.readFloat()
        last_move_time = f.readFloat()

        health = f.readInt()
        max_health = f.readInt()

        gib_health = f.readInt()
        deadflag = f.readInt()
        show_hostile = f.readInt()

        powerarmor_time = f.readFloat()

        map = f.readString()

        viewheight = f.readInt()
        takedamage = f.readInt()
        dmg = f.readInt()
        radius_dmg = f.readInt()
        dmg_radius = f.readFloat()

        sounds = f.readInt()
        count = f.readInt()

        chain = f.readEdictRef()
        enemy = f.readEdictRef()

        oldenemy = f.readEdictRef()
        activator = f.readEdictRef()
        groundentity = f.readEdictRef()

        groundentity_linkcount = f.readInt()
        teamchain = f.readEdictRef()
        teammaster = f.readEdictRef()

        mynoise = f.readEdictRef()
        mynoise2 = f.readEdictRef()

        noise_index = f.readInt()
        noise_index2 = f.readInt()

        volume = f.readFloat()
        attenuation = f.readFloat()
        wait = f.readFloat()
        delay = f.readFloat()
        random = f.readFloat()

        teleport_time = f.readFloat()

        watertype = f.readInt()
        waterlevel = f.readInt()
        move_origin = f.readVector()
        move_angles = f.readVector()

        light_level = f.readInt()
        style = f.readInt()

        item = f.readItem()

        moveinfo.read(f)
        monsterinfo.read(f)

        val ndx = f.readInt()
        if (ndx == -1)
            client = null
        else
            client = GameBase.game.clients[ndx]

        owner = f.readEdictRef()

        // rst's checker :-)
        if (f.readInt() != 9876)
            System.err.println("ent load check failed for num " + index)
    }
}