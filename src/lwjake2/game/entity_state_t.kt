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

import java.io.IOException

import lwjake2.util.Math3D
import lwjake2.util.QuakeFile

public class entity_state_t
/** entity_state_t is the information conveyed from the server
 * in an update message about entities that the client will
 * need to render in some way.  */
(ent: edict_t?) : Cloneable {
    {
        this.surrounding_ent = ent
        if (ent != null)
            number = ent!!.index
    }

    /** edict index. TODO: this is critical. The index has to be proper managed.  */
    public var number: Int = 0
    // TODO: why was this introduced?
    public var surrounding_ent: edict_t? = null
    public var origin: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var angles: FloatArray = floatArray(0.0, 0.0, 0.0)

    /** for lerping.  */
    public var old_origin: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var modelindex: Int = 0
    /** weapons, CTF flags, etc.  */
    public var modelindex2: Int = 0
    public var modelindex3: Int = 0
    public var modelindex4: Int = 0
    public var frame: Int = 0
    public var skinnum: Int = 0
    /** PGM - we're filling it, so it needs to be unsigned.  */
    public var effects: Int = 0
    public var renderfx: Int = 0
    public var solid: Int = 0
    // for client side prediction, 8*(bits 0-4) is x/y radius
    // 8*(bits 5-9) is z down distance, 8(bits10-15) is z up
    // gi.linkentity sets this properly
    public var sound: Int = 0 // for looping sounds, to guarantee shutoff
    public var event: Int = 0 // impulse events -- muzzle flashes, footsteps, etc
    // events only go out for a single frame, they
    // are automatically cleared each frame

    /** Writes the entity state to the file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        f.writeEdictRef(surrounding_ent)
        f.writeVector(origin)
        f.writeVector(angles)
        f.writeVector(old_origin)

        f.writeInt(modelindex)

        f.writeInt(modelindex2)
        f.writeInt(modelindex3)
        f.writeInt(modelindex4)

        f.writeInt(frame)
        f.writeInt(skinnum)

        f.writeInt(effects)
        f.writeInt(renderfx)
        f.writeInt(solid)

        f.writeInt(sound)
        f.writeInt(event)

    }

    /** Reads the entity state from the file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        surrounding_ent = f.readEdictRef()
        origin = f.readVector()
        angles = f.readVector()
        old_origin = f.readVector()

        modelindex = f.readInt()

        modelindex2 = f.readInt()
        modelindex3 = f.readInt()
        modelindex4 = f.readInt()

        frame = f.readInt()
        skinnum = f.readInt()

        effects = f.readInt()
        renderfx = f.readInt()
        solid = f.readInt()

        sound = f.readInt()
        event = f.readInt()


    }


    public fun getClone(): entity_state_t {
        val out = entity_state_t(this.surrounding_ent)
        out.set(this)
        return out
    }

    public fun set(from: entity_state_t) {
        number = from.number
        Math3D.VectorCopy(from.origin, origin)
        Math3D.VectorCopy(from.angles, angles)
        Math3D.VectorCopy(from.old_origin, old_origin)

        modelindex = from.modelindex
        modelindex2 = from.modelindex2
        modelindex3 = from.modelindex3
        modelindex4 = from.modelindex4

        frame = from.frame
        skinnum = from.skinnum
        effects = from.effects
        renderfx = from.renderfx
        solid = from.solid
        sound = from.sound
        event = from.event
    }

    public fun clear() {
        //TODO: this is critical. The index has to be proper managed.
        number = 0
        surrounding_ent = null
        Math3D.VectorClear(origin)
        Math3D.VectorClear(angles)
        Math3D.VectorClear(old_origin)
        modelindex = 0
        modelindex2 = modelindex3 = modelindex4 = 0
        frame = 0
        skinnum = 0
        effects = 0
        renderfx = 0
        solid = 0
        sound = 0
        event = 0
    }
}