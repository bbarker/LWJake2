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

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.client.CL_ents
import lwjake2.game.entity_state_t
import lwjake2.qcommon.Com
import lwjake2.sound.Sound
import lwjake2.sound.sfx_t
import lwjake2.sound.sfxcache_t
import lwjake2.util.Lib
import lwjake2.util.Math3D

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.Hashtable

import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import org.lwjgl.openal.EFX10

/**
 * Channel

 * @author dsanders/cwei
 */
public class Channel private(private val sourceId: Int) {

    // sound attributes
    private var type: Int = 0
    private var entnum: Int = 0
    private var entchannel: Int = 0
    private var bufferId: Int = 0
    private var volume: Float = 0.toFloat()
    private var rolloff: Float = 0.toFloat()
    private val origin = floatArray(0.0, 0.0, 0.0)

    // update flags
    private var autosound: Boolean = false
    private var active: Boolean = false
    private var modified: Boolean = false
    private var bufferChanged: Boolean = false
    private var volumeChanged: Boolean = false

    {
        clear()
        volumeChanged = false
        volume = 1.0.toFloat()
    }

    private fun clear() {
        entnum = entchannel = bufferId = -1
        bufferChanged = false
        rolloff = 0
        autosound = false
        active = false
        modified = false
    }

    companion object {

        val LISTENER = 0
        val FIXED = 1
        val DYNAMIC = 2
        val MAX_CHANNELS = 128

        private val channels = arrayOfNulls<Channel>(MAX_CHANNELS)
        private val sources = Lib.newIntBuffer(MAX_CHANNELS)
        // a reference of LWJGLSoundImpl.buffers
        private var buffers: IntBuffer? = null
        private val looptable = Hashtable<Integer, Channel>(MAX_CHANNELS)

        private var numChannels: Int = 0

        // stream handling
        private var streamingEnabled = false
        private var streamQueue = 0

        private val tmp = Lib.newIntBuffer(1)

        fun init(buffers: IntBuffer): Int {
            Channel.buffers = buffers
            // create channels
            val sourceId: Int
            for (i in 0..MAX_CHANNELS - 1) {

                AL10.alGenSources(tmp)
                sourceId = tmp.get(0)

                // can't generate more sources
                if (sourceId <= 0) break

                sources.put(i, sourceId)

                channels[i] = Channel(sourceId)
                numChannels++

                // set default values for AL sources
                AL10.alSourcef(sourceId, AL10.AL_GAIN, 1.0.toFloat())
                AL10.alSourcef(sourceId, AL10.AL_PITCH, 1.0.toFloat())
                AL10.alSourcei(sourceId, AL10.AL_SOURCE_ABSOLUTE, AL10.AL_TRUE)
                AL10.alSource3f(sourceId, AL10.AL_VELOCITY, 0, 0, 0)
                AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_FALSE)
                AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, 200.0.toFloat())
                AL10.alSourcef(sourceId, AL10.AL_MIN_GAIN, 0.0005.toFloat())
                AL10.alSourcef(sourceId, AL10.AL_MAX_GAIN, 1.0.toFloat())
            }
            return numChannels
        }

        fun reset() {
            for (i in 0..numChannels - 1) {
                AL10.alSourceStop(sources.get(i))
                AL10.alSourcei(sources.get(i), AL10.AL_BUFFER, 0)
                channels[i].clear()
            }
        }

        fun shutdown() {
            AL10.alDeleteSources(sources)
            numChannels = 0
        }

        fun enableStreaming() {
            if (streamingEnabled) return

            // use the last source
            numChannels--
            streamingEnabled = true
            streamQueue = 0

            val source = channels[numChannels].sourceId
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE)
            AL10.alSourcef(source, AL10.AL_GAIN, 1.0.toFloat())
            channels[numChannels].volumeChanged = true

            Com.DPrintf("streaming enabled\n")
        }

        fun disableStreaming() {
            if (!streamingEnabled) return
            unqueueStreams()
            val source = channels[numChannels].sourceId
            AL10.alSourcei(source, AL10.AL_SOURCE_ABSOLUTE, AL10.AL_TRUE)

            // free the last source
            //numChannels++;
            streamingEnabled = false
            Com.DPrintf("streaming disabled\n")
        }

        fun unqueueStreams() {
            if (!streamingEnabled) return
            val source = channels[numChannels].sourceId

            // stop streaming
            AL10.alSourceStop(source)
            var count = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED)
            Com.DPrintf("unqueue " + count + " buffers\n")
            while (count-- > 0) {
                AL10.alSourceUnqueueBuffers(source, tmp)
            }
            streamQueue = 0
        }

        fun updateStream(samples: ByteBuffer, count: Int, format: Int, rate: Int) {
            enableStreaming()
            val source = channels[numChannels].sourceId
            val processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED)

            val playing = (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING)
            val interupted = !playing && streamQueue > 2

            val buffer = tmp
            if (interupted) {
                unqueueStreams()
                buffer.put(0, buffers!!.get(Sound.MAX_SFX + streamQueue++))
                Com.DPrintf("queue " + (streamQueue - 1) + '\n')
            } else if (processed < 2) {
                // check queue overrun
                if (streamQueue >= Sound.STREAM_QUEUE) return
                buffer.put(0, buffers!!.get(Sound.MAX_SFX + streamQueue++))
                Com.DPrintf("queue " + (streamQueue - 1) + '\n')
            } else {
                // reuse the buffer
                AL10.alSourceUnqueueBuffers(source, buffer)
            }

            samples.position(0)
            samples.limit(count)
            AL10.alBufferData(buffer.get(0), format, samples, rate)
            AL10.alSourceQueueBuffers(source, buffer)

            if (streamQueue > 1 && !playing) {
                Com.DPrintf("start sound\n")
                AL10.alSourcePlay(source)
            }
        }

        fun addPlaySounds() {
            while (Channel.assign(PlaySound.nextPlayableSound()))
        }

        private fun assign(ps: PlaySound?): Boolean {
            if (ps == null) return false
            var ch: Channel? = null
            var i: Int
            run {
                i = 0
                while (i < numChannels) {
                    ch = channels[i]

                    if (ps!!.entchannel != 0 && ch!!.entnum == ps!!.entnum && ch!!.entchannel == ps!!.entchannel) {
                        // always override sound from same entity
                        if (ch!!.bufferId != ps!!.bufferId) {
                            AL10.alSourceStop(ch!!.sourceId)
                        }
                        break
                    }

                    // don't let monster sounds override player sounds
                    if ((ch!!.entnum == Globals.cl.playernum + 1) && (ps!!.entnum != Globals.cl.playernum + 1) && ch!!.bufferId != -1)
                        continue

                    // looking for a free AL source
                    if (!ch!!.active) {
                        break
                    }
                    i++
                }
            }

            if (i == numChannels)
                return false

            ch!!.type = ps!!.type
            if (ps!!.type == Channel.FIXED)
                Math3D.VectorCopy(ps!!.origin, ch!!.origin)
            ch!!.entnum = ps!!.entnum
            ch!!.entchannel = ps!!.entchannel
            ch!!.bufferChanged = (ch!!.bufferId != ps!!.bufferId)
            ch!!.bufferId = ps!!.bufferId
            ch!!.rolloff = ps!!.attenuation * 2
            ch!!.volumeChanged = (ch!!.volume != ps!!.volume)
            ch!!.volume = ps!!.volume
            ch!!.active = true
            ch!!.modified = true
            return true
        }

        private fun pickForLoop(bufferId: Int, attenuation: Float): Channel? {
            val ch: Channel
            for (i in 0..numChannels - 1) {
                ch = channels[i]
                // looking for a free AL source
                if (!ch.active) {
                    ch.entnum = 0
                    ch.entchannel = 0
                    ch.bufferChanged = (ch.bufferId != bufferId)
                    ch.bufferId = bufferId
                    ch.volumeChanged = (ch.volume != 1.0.toFloat())
                    ch.volume = 1.0.toFloat()
                    ch.rolloff = attenuation * 2
                    ch.active = true
                    ch.modified = true
                    return ch
                }
            }
            return null
        }

        private val sourceOriginBuffer = Lib.newFloatBuffer(3)

        //stack variable
        private val entityOrigin = floatArray(0.0, 0.0, 0.0)

        fun playAllSounds(listenerOrigin: FloatBuffer, currentEffectIndex: Int, currentFilterIndex: Int) {
            val sourceOrigin = sourceOriginBuffer
            val ch: Channel
            val sourceId: Int
            val state: Int

            for (i in 0..numChannels - 1) {
                ch = channels[i]
                if (ch.active) {
                    sourceId = ch.sourceId
                    when (ch.type) {
                        Channel.LISTENER -> {
                            sourceOrigin.put(0, listenerOrigin.get(0))
                            sourceOrigin.put(1, listenerOrigin.get(1))
                            sourceOrigin.put(2, listenerOrigin.get(2))
                        }
                        Channel.DYNAMIC -> {
                            CL_ents.GetEntitySoundOrigin(ch.entnum, entityOrigin)
                            convertVector(entityOrigin, sourceOrigin)
                        }
                        Channel.FIXED -> convertVector(ch.origin, sourceOrigin)
                    }

                    if (ch.modified) {
                        if (ch.bufferChanged) {
                            AL10.alSourcei(sourceId, AL10.AL_BUFFER, ch.bufferId)
                        }
                        if (ch.volumeChanged) {
                            AL10.alSourcef(sourceId, AL10.AL_GAIN, ch.volume)
                        }
                        AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, ch.rolloff)
                        AL10.alSource3f(sourceId, AL10.AL_POSITION, sourceOrigin.get(), sourceOrigin.get(), sourceOrigin.get())
                        AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, currentEffectIndex, 0, EFX10.AL_FILTER_NULL)
                        AL10.alSourcei(sourceId, EFX10.AL_DIRECT_FILTER, currentFilterIndex)
                        AL10.alSourcePlay(sourceId)
                        sourceOrigin.rewind()
                        ch.modified = false
                    } else {
                        state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE)
                        if (state == AL10.AL_PLAYING) {
                            AL10.alSource3f(sourceId, AL10.AL_POSITION, sourceOrigin.get(), sourceOrigin.get(), sourceOrigin.get())
                            sourceOrigin.rewind()
                        } else {
                            ch.clear()
                        }
                    }
                    ch.autosound = false
                }
            }
        }

        /*
	 * 	adddLoopSounds
	 * 	Entities with a ->sound field will generated looped sounds
	 * 	that are automatically started, stopped, and merged together
	 * 	as the entities are sent to the client
	 */
        fun addLoopSounds() {

            if ((Globals.cl_paused.value != 0.0.toFloat()) || (Globals.cls.state != Globals.ca_active) || !Globals.cl.sound_prepped) {
                removeUnusedLoopSounds()
                return
            }

            var ch: Channel?
            val sfx: sfx_t?
            val sc: sfxcache_t?
            val num: Int
            val ent: entity_state_t
            val key: Int
            var sound = 0

            for (i in 0..Globals.cl.frame.num_entities - 1) {
                num = (Globals.cl.frame.parse_entities + i) and (Defines.MAX_PARSE_ENTITIES - 1)
                ent = Globals.cl_parse_entities[num]
                sound = ent.sound

                if (sound == 0) continue

                key = ent.number
                ch = looptable.get(key)

                if (ch != null) {
                    // keep on looping
                    ch.autosound = true
                    Math3D.VectorCopy(ent.origin, ch.origin)
                    continue
                }

                sfx = Globals.cl.sound_precache[sound]
                if (sfx == null)
                    continue        // bad sound effect

                sc = sfx!!.cache
                if (sc == null)
                    continue

                // allocate a channel
                ch = Channel.pickForLoop(buffers!!.get(sfx!!.bufferId), 6)
                if (ch == null)
                    break

                ch.type = FIXED
                Math3D.VectorCopy(ent.origin, ch.origin)
                ch.autosound = true

                looptable.put(key, ch)
                AL10.alSourcei(ch.sourceId, AL10.AL_LOOPING, AL10.AL_TRUE)
            }

            removeUnusedLoopSounds()

        }

        private fun removeUnusedLoopSounds() {
            var ch: Channel
            // stop unused loopsounds
            run {
                val iter = looptable.values().iterator()
                while (iter.hasNext()) {
                    ch = iter.next()
                    if (!ch.autosound) {
                        AL10.alSourceStop(ch.sourceId)
                        AL10.alSourcei(ch.sourceId, AL10.AL_LOOPING, AL10.AL_FALSE)
                        iter.remove()
                        ch.clear()
                    }
                }
            }
        }

        fun convertVector(from: FloatArray, to: FloatBuffer) {
            to.put(0, from[0])
            to.put(1, from[2])
            to.put(2, -from[1])
        }

        fun convertOrientation(forward: FloatArray, up: FloatArray, orientation: FloatBuffer) {
            orientation.put(0, forward[0])
            orientation.put(1, forward[2])
            orientation.put(2, -forward[1])
            orientation.put(3, up[0])
            orientation.put(4, up[2])
            orientation.put(5, -up[1])
        }
    }

}
