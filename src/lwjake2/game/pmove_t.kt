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
import lwjake2.util.Math3D

import java.util.Arrays

public class pmove_t {

    open public class TraceAdapter {
        // callbacks to test the world
        public fun trace(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray): trace_t? {
            return null
        }
    }

    open public class PointContentsAdapter {
        // callbacks to test the world
        public fun pointcontents(point: FloatArray): Int {
            return 0
        }
    }

    // state (in / out)
    public var s: pmove_state_t = pmove_state_t()

    // command (in)
    public var cmd: usercmd_t = usercmd_t()

    public var snapinitial: Boolean = false // if s has been changed outside pmove

    // results (out)
    public var numtouch: Int = 0

    public var touchents: Array<edict_t> = arrayOfNulls<edict_t>(Defines.MAXTOUCH)

    public var viewangles: FloatArray = floatArray(0.0, 0.0, 0.0) // clamped

    public var viewheight: Float = 0.toFloat()

    public var mins: FloatArray = floatArray(0.0, 0.0, 0.0)
    public var maxs: FloatArray = floatArray(0.0, 0.0, 0.0) // bounding box size

    public var groundentity: edict_t? = null

    public var watertype: Int = 0

    public var waterlevel: Int = 0

    public var trace: TraceAdapter? = null

    public var pointcontents: PointContentsAdapter? = null
    // prediction (used for
    // grappling hook)

    public fun clear() {
        groundentity = null
        waterlevel = watertype = 0
        trace = null
        pointcontents = null
        Math3D.VectorClear(mins)
        Math3D.VectorClear(maxs)
        viewheight = 0
        Math3D.VectorClear(viewangles)
        Arrays.fill(touchents, null)
        numtouch = 0
        snapinitial = false
        cmd.clear()
        s.clear()
    }

    companion object {

        // pmove->pm_flags
        public val PMF_DUCKED: Int = 1

        public val PMF_JUMP_HELD: Int = 2

        public val PMF_ON_GROUND: Int = 4

        public val PMF_TIME_WATERJUMP: Int = 8 // pm_time is waterjump

        public val PMF_TIME_LAND: Int = 16 // pm_time is time before rejump

        public val PMF_TIME_TELEPORT: Int = 32 // pm_time is non-moving
        // time

        public val PMF_NO_PREDICTION: Int = 64 // temporarily disables
    }
}