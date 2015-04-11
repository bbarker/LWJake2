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

package lwjake2.sound.lwjgl

import lwjake2.Globals
import lwjake2.util.Math3D

/**
 * PlaySound

 * @author cwei
 */
public class PlaySound private() {

    // sound attributes
    var type: Int = 0
    var entnum: Int = 0
    var entchannel: Int = 0
    var bufferId: Int = 0
    var volume: Float = 0.toFloat()
    var attenuation: Float = 0.toFloat()
    var origin = floatArray(0.0, 0.0, 0.0)

    // begin time in ms
    private var beginTime: Long = 0

    // for linked list
    private var prev: PlaySound? = null
    private var next: PlaySound? = null

    {
        prev = next = null
        this.clear()
    }

    private fun clear() {
        type = bufferId = entnum = entchannel = -1
        // volume = attenuation = beginTime = 0;
        attenuation = (beginTime = 0).toFloat()
        // Math3D.VectorClear(origin);
    }

    companion object {

        val MAX_PLAYSOUNDS = 128

        // list with sentinel
        private var freeList: PlaySound? = null
        private var playableList: PlaySound? = null

        private val backbuffer = arrayOfNulls<PlaySound>(MAX_PLAYSOUNDS)
        {
            for (i in backbuffer.indices) {
                backbuffer[i] = PlaySound()
            }
            // init the sentinels
            freeList = PlaySound()
            playableList = PlaySound()
            // reset the lists
            reset()
        }

        fun reset() {
            // init the sentinels
            freeList!!.next = freeList!!.prev = freeList
            playableList!!.next = playableList!!.prev = playableList

            // concat the the freeList
            val ps: PlaySound
            for (i in backbuffer.indices) {
                ps = backbuffer[i]
                ps.clear()
                ps.prev = freeList
                ps.next = freeList!!.next
                ps.prev!!.next = ps
                ps.next!!.prev = ps
            }
        }

        fun nextPlayableSound(): PlaySound? {
            var ps: PlaySound? = null
            while (true) {
                ps = playableList!!.next
                if (ps == playableList || ps!!.beginTime > Globals.cl.time)
                    return null
                PlaySound.release(ps)
                return ps
            }
        }

        private fun get(): PlaySound? {
            val ps = freeList!!.next
            if (ps == freeList)
                return null

            ps.prev!!.next = ps.next
            ps.next!!.prev = ps.prev
            return ps
        }

        private fun add(ps: PlaySound) {

            var sort: PlaySound = playableList!!.next

            while (sort != playableList && sort.beginTime < ps.beginTime) {
                sort = sort.next
            }
            ps.next = sort
            ps.prev = sort.prev
            ps.next!!.prev = ps
            ps.prev!!.next = ps
        }

        private fun release(ps: PlaySound) {
            ps.prev!!.next = ps.next
            ps.next!!.prev = ps.prev
            // add to free list
            ps.next = freeList!!.next
            freeList!!.next!!.prev = ps
            ps.prev = freeList
            freeList!!.next = ps
        }

        fun allocate(origin: FloatArray?, entnum: Int, entchannel: Int, bufferId: Int, volume: Float, attenuation: Float, timeoffset: Float) {

            val ps = PlaySound.get()

            if (ps != null) {
                // find the right sound type
                if (entnum == Globals.cl.playernum + 1) {
                    ps.type = Channel.LISTENER
                } else if (origin != null) {
                    ps.type = Channel.FIXED
                    Math3D.VectorCopy(origin, ps.origin)
                } else {
                    ps.type = Channel.DYNAMIC
                }
                ps.entnum = entnum
                ps.entchannel = entchannel
                ps.bufferId = bufferId
                ps.volume = volume
                ps.attenuation = attenuation
                ps.beginTime = Globals.cl.time + (timeoffset * 1000).toLong()
                PlaySound.add(ps)
            } else {
                System.err.println("PlaySounds out of Limit")
            }
        }
    }
}
