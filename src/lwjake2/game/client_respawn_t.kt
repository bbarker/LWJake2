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

import lwjake2.util.Math3D
import lwjake2.util.QuakeFile

import java.io.IOException

/** Client data that stays across deathmatch respawns.  */
public class client_respawn_t {
    /** What to set client->pers to on a respawn  */
    protected var coop_respawn: client_persistant_t = client_persistant_t()

    /** Level.framenum the client entered the game.  */
    protected var enterframe: Int = 0

    /** frags, etc.  */
    protected var score: Int = 0

    /** angles sent over in the last command.  */
    protected var cmd_angles: FloatArray = floatArray(0.0, 0.0, 0.0)

    /** client is a spectator.  */
    protected var spectator: Boolean = false


    /** Copies the client respawn data.  */
    public fun set(from: client_respawn_t) {
        coop_respawn.set(from.coop_respawn)
        enterframe = from.enterframe
        score = from.score
        Math3D.VectorCopy(from.cmd_angles, cmd_angles)
        spectator = from.spectator
    }

    /** Clears the client reaspawn informations.  */
    public fun clear() {
        coop_respawn = client_persistant_t()
        enterframe = 0
        score = 0
        Math3D.VectorClear(cmd_angles)
        spectator = false
    }

    /** Reads a client_respawn from a file.  */
    throws(javaClass<IOException>())
    public fun read(f: QuakeFile) {
        coop_respawn.read(f)
        enterframe = f.readInt()
        score = f.readInt()
        cmd_angles[0] = f.readFloat()
        cmd_angles[1] = f.readFloat()
        cmd_angles[2] = f.readFloat()
        spectator = f.readInt() != 0
    }

    /** Writes a client_respawn to a file.  */
    throws(javaClass<IOException>())
    public fun write(f: QuakeFile) {
        coop_respawn.write(f)
        f.writeInt(enterframe)
        f.writeInt(score)
        f.writeFloat(cmd_angles[0])
        f.writeFloat(cmd_angles[1])
        f.writeFloat(cmd_angles[2])
        f.writeInt(if (spectator) 1 else 0)
    }
}
