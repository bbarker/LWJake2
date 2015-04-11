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

public class PlayerTrail {
    companion object {

        /*
     * ==============================================================================
     * 
     * PLAYER TRAIL
     * 
     * ==============================================================================
     * 
     * This is a circular list containing the a list of points of where the
     * player has been recently. It is used by monsters for pursuit.
     * 
     * .origin the spot .owner forward link .aiment backward link
     */

        var TRAIL_LENGTH = 8

        var trail = arrayOfNulls<edict_t>(TRAIL_LENGTH)

        var trail_head: Int = 0

        var trail_active = false

        {
            //TODO: potential error
            for (n in 0..TRAIL_LENGTH - 1)
                trail[n] = edict_t(n)
        }

        fun NEXT(n: Int): Int {
            return (n + 1) % PlayerTrail.TRAIL_LENGTH
        }

        fun PREV(n: Int): Int {
            return (n + PlayerTrail.TRAIL_LENGTH - 1) % PlayerTrail.TRAIL_LENGTH
        }

        fun Init() {

            // FIXME || coop
            if (GameBase.deathmatch.value != 0)
                return

            for (n in 0..PlayerTrail.TRAIL_LENGTH - 1) {
                PlayerTrail.trail[n] = GameUtil.G_Spawn()
                PlayerTrail.trail[n].classname = "player_trail"
            }

            trail_head = 0
            trail_active = true
        }

        fun Add(spot: FloatArray) {
            val temp = floatArray(0.0, 0.0, 0.0)

            if (!trail_active)
                return

            Math3D.VectorCopy(spot, PlayerTrail.trail[trail_head].s.origin)

            PlayerTrail.trail[trail_head].timestamp = GameBase.level.time

            Math3D.VectorSubtract(spot, PlayerTrail.trail[PREV(trail_head)].s.origin, temp)
            PlayerTrail.trail[trail_head].s.angles[1] = Math3D.vectoyaw(temp)

            trail_head = NEXT(trail_head)
        }

        fun New(spot: FloatArray) {
            if (!trail_active)
                return

            Init()
            Add(spot)
        }

        fun PickFirst(self: edict_t): edict_t? {

            if (!trail_active)
                return null

            var marker = trail_head

            run {
                var n = PlayerTrail.TRAIL_LENGTH
                while (n > 0) {
                    if (PlayerTrail.trail[marker].timestamp <= self.monsterinfo.trail_time)
                        marker = NEXT(marker)
                    else
                        break
                    n--
                }
            }

            if (GameUtil.visible(self, PlayerTrail.trail[marker])) {
                return PlayerTrail.trail[marker]
            }

            if (GameUtil.visible(self, PlayerTrail.trail[PREV(marker)])) {
                return PlayerTrail.trail[PREV(marker)]
            }

            return PlayerTrail.trail[marker]
        }

        fun PickNext(self: edict_t): edict_t? {
            var marker: Int
            var n: Int

            if (!trail_active)
                return null

            run {
                marker = trail_head
                n = PlayerTrail.TRAIL_LENGTH
                while (n > 0) {
                    if (PlayerTrail.trail[marker].timestamp <= self.monsterinfo.trail_time)
                        marker = NEXT(marker)
                    else
                        break
                    n--
                }
            }

            return PlayerTrail.trail[marker]
        }

        fun LastSpot(): edict_t {
            return PlayerTrail.trail[PREV(trail_head)]
        }
    }
}