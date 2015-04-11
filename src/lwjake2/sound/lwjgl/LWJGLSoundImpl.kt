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
import lwjake2.game.Cmd
import lwjake2.game.GameBase
import lwjake2.game.cvar_t
import lwjake2.game.entity_state_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.xcommand_t
import lwjake2.sound.S
import lwjake2.sound.Sound
import lwjake2.sound.WaveLoader
import lwjake2.sound.sfx_t
import lwjake2.sound.sfxcache_t
import lwjake2.util.Lib
import lwjake2.util.Vargs

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

import com.flibitijibibo.flibitEFX.EFXFilterLowPass
import org.lwjgl.LWJGLException
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC10
import org.lwjgl.openal.EFX10
import org.lwjgl.openal.OpenALException

/**
 * LWJGLSoundImpl

 * @author dsanders/cwei
 */
public class LWJGLSoundImpl// singleton 
private() : Sound {

    private var s_volume: cvar_t? = null

    // the last 4 buffers are used for cinematics streaming
    private val buffers = Lib.newIntBuffer(MAX_SFX + STREAM_QUEUE)

    /** EFX Variables  */
    private var currentEffectIndex: Int = 0
    private var currentFilterIndex: Int = 0
    private var underwaterFilter: EFXFilterLowPass? = null

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#Init()
	 */
    public fun Init(): Boolean {

        try {
            initOpenAL()
            checkError()
            initOpenALExtensions()
        } catch (e: OpenALException) {
            Com.Printf(e.getMessage() + '\n')
            return false
        } catch (e: Exception) {
            Com.DPrintf(e.getMessage() + '\n')
            return false
        }


        // set the listerner (master) volume
        s_volume = Cvar.Get("s_volume", "0.7", Defines.CVAR_ARCHIVE)
        AL10.alGenBuffers(buffers)
        val count = Channel.init(buffers)
        Com.Printf("... using " + count + " channels\n")
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED)
        Cmd.AddCommand("play", object : xcommand_t() {
            public fun execute() {
                Play()
            }
        })
        Cmd.AddCommand("stopsound", object : xcommand_t() {
            public fun execute() {
                StopAllSounds()
            }
        })
        Cmd.AddCommand("soundlist", object : xcommand_t() {
            public fun execute() {
                SoundList()
            }
        })
        Cmd.AddCommand("soundinfo", object : xcommand_t() {
            public fun execute() {
                SoundInfo_f()
            }
        })

        num_sfx = 0

        Com.Printf("sound sampling rate: 44100Hz\n")

        StopAllSounds()
        Com.Printf("------------------------------------\n")
        return true
    }


    throws(javaClass<OpenALException>())
    private fun initOpenAL() {
        try {
            AL.create()
        } catch (e: LWJGLException) {
            throw OpenALException(e)
        }

        var deviceName: String? = null

        val os = System.getProperty("os.name")
        if (os.startsWith("Windows")) {
            deviceName = "DirectSound3D"
        }

        val defaultSpecifier = ALC10.alcGetString(AL.getDevice(), ALC10.ALC_DEFAULT_DEVICE_SPECIFIER)

        Com.Printf(os + " using " + (if ((deviceName == null)) defaultSpecifier else deviceName) + '\n')

        // Check for an error.
        if (ALC10.alcGetError(AL.getDevice()) != ALC10.ALC_NO_ERROR) {
            Com.DPrintf("Error with SoundDevice")
        }
    }

    /** Initializes OpenAL EFX effects.  */
    private fun initOpenALExtensions() {
        Com.Printf("... using EFX effects:\n")
        underwaterFilter = EFXFilterLowPass()
        underwaterFilter!!.setGain(1.0.toFloat())
        underwaterFilter!!.setGainHF(0.0.toFloat())
    }


    fun exitOpenAL() {
        // Unload EFX Effects
        underwaterFilter!!.killFilter()

        // Release the context and the device.
        AL.destroy()
    }

    // TODO check the sfx direct buffer size
    // 2MB sfx buffer
    private val sfxDataBuffer = Lib.newByteBuffer(2 * 1024 * 1024)

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#RegisterSound(jake2.sound.sfx_t)
	 */
    private fun initBuffer(samples: ByteArray, bufferId: Int, freq: Int) {
        val data = sfxDataBuffer.slice()
        data.put(samples).flip()
        AL10.alBufferData(buffers.get(bufferId), AL10.AL_FORMAT_MONO16, data, freq)
    }

    private fun checkError() {
        Com.DPrintf("AL Error: " + alErrorString() + '\n')
    }

    private fun alErrorString(): String {
        val error: Int
        var message = ""
        if ((error = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            when (error) {
                AL10.AL_INVALID_OPERATION -> message = "invalid operation"
                AL10.AL_INVALID_VALUE -> message = "invalid value"
                AL10.AL_INVALID_ENUM -> message = "invalid enum"
                AL10.AL_INVALID_NAME -> message = "invalid name"
                else -> message = "" + error
            }
        }
        return message
    }

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#Shutdown()
	 */
    public fun Shutdown() {
        StopAllSounds()
        Channel.shutdown()
        AL10.alDeleteBuffers(buffers)
        exitOpenAL()

        Cmd.RemoveCommand("play")
        Cmd.RemoveCommand("stopsound")
        Cmd.RemoveCommand("soundlist")
        Cmd.RemoveCommand("soundinfo")

        // free all sounds
        for (i in 0..num_sfx - 1) {
            if (known_sfx[i].name == null)
                continue
            known_sfx[i].clear()
        }
        num_sfx = 0
    }

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#StartSound(float[], int, int, jake2.sound.sfx_t, float, float, float)
	 */
    public fun StartSound(origin: FloatArray?, entnum: Int, entchannel: Int, sfx: sfx_t?, fvol: Float, attenuation: Float, timeofs: Float) {
        var sfx = sfx
        var attenuation = attenuation

        if (sfx == null)
            return

        if (sfx!!.name.charAt(0) == '*')
            sfx = RegisterSexedSound(Globals.cl_entities[entnum].current, sfx!!.name)

        if (LoadSound(sfx) == null)
            return  // can't load sound

        if (attenuation != Defines.ATTN_STATIC)
            attenuation *= 0.5.toFloat()

        PlaySound.allocate(origin, entnum, entchannel, buffers.get(sfx!!.bufferId), fvol, attenuation, timeofs)
    }

    private val listenerOrigin = Lib.newFloatBuffer(3)
    private val listenerOrientation = Lib.newFloatBuffer(6)

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#Update(float[], float[], float[], float[])
	 */
    public fun Update(origin: FloatArray, forward: FloatArray, right: FloatArray, up: FloatArray) {

        Channel.convertVector(origin, listenerOrigin)
        AL10.alListener(AL10.AL_POSITION, listenerOrigin)

        Channel.convertOrientation(forward, up, listenerOrientation)
        AL10.alListener(AL10.AL_ORIENTATION, listenerOrientation)

        // set the master volume
        AL10.alListenerf(AL10.AL_GAIN, s_volume!!.value)

        // Detect EFX Conditions
        if ((GameBase.gi.pointcontents.pointcontents(origin) and Defines.MASK_WATER) != 0) {
            currentFilterIndex = underwaterFilter!!.getIndex()
        } else {
            currentEffectIndex = EFX10.AL_EFFECTSLOT_NULL
            currentFilterIndex = EFX10.AL_FILTER_NULL
        }

        Channel.addLoopSounds()
        Channel.addPlaySounds()
        Channel.playAllSounds(listenerOrigin, currentEffectIndex, currentFilterIndex)
    }

    /* (non-Javadoc)
	 * @see jake2.sound.SoundImpl#StopAllSounds()
	 */
    public fun StopAllSounds() {
        // mute the listener (master)
        AL10.alListenerf(AL10.AL_GAIN, 0)
        PlaySound.reset()
        Channel.reset()
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#getName()
	 */
    public fun getName(): String {
        return "lwjgl"
    }

    var s_registration_sequence: Int = 0
    var s_registering: Boolean = false

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#BeginRegistration()
	 */
    public fun BeginRegistration() {
        s_registration_sequence++
        s_registering = true
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#RegisterSound(java.lang.String)
	 */
    public fun RegisterSound(name: String): sfx_t {
        val sfx = FindName(name, true)
        sfx.registration_sequence = s_registration_sequence

        if (!s_registering)
            LoadSound(sfx)

        return sfx
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#EndRegistration()
	 */
    public fun EndRegistration() {
        var i: Int
        var sfx: sfx_t

        // free any sounds not from this registration sequence
        run {
            i = 0
            while (i < num_sfx) {
                sfx = known_sfx[i]
                if (sfx.name == null)
                    continue
                if (sfx.registration_sequence != s_registration_sequence) {
                    // don't need this sound
                    sfx.clear()
                }
                i++
            }
        }

        // load everything in
        run {
            i = 0
            while (i < num_sfx) {
                sfx = known_sfx[i]
                if (sfx.name == null)
                    continue
                LoadSound(sfx)
                i++
            }
        }

        s_registering = false
    }

    fun RegisterSexedSound(ent: entity_state_t, base: String): sfx_t {

        var sfx: sfx_t? = null

        // determine what model the client is using
        var model: String? = null
        val n = Globals.CS_PLAYERSKINS + ent.number - 1
        if (Globals.cl.configstrings[n] != null) {
            var p = Globals.cl.configstrings[n].indexOf('\\')
            if (p >= 0) {
                p++
                model = Globals.cl.configstrings[n].substring(p)
                //strcpy(model, p);
                p = model!!.indexOf('/')
                if (p > 0)
                    model = model!!.substring(0, p)
            }
        }
        // if we can't figure it out, they're male
        if (model == null || model!!.length() == 0)
            model = "male"

        // see if we already know of the model specific sound
        val sexedFilename = "#players/" + model + "/" + base.substring(1)
        //Com_sprintf (sexedFilename, sizeof(sexedFilename), "#players/%s/%s", model, base+1);
        sfx = FindName(sexedFilename, false)

        if (sfx != null) return sfx

        //
        // fall back strategies
        //
        // not found , so see if it exists
        if (FS.FileLength(sexedFilename.substring(1)) > 0) {
            // yes, register it
            return RegisterSound(sexedFilename)
        }
        // try it with the female sound in the pak0.pak
        if (model!!.equalsIgnoreCase("female")) {
            val femaleFilename = "player/female/" + base.substring(1)
            if (FS.FileLength("sound/" + femaleFilename) > 0)
                return AliasName(sexedFilename, femaleFilename)
        }
        // no chance, revert to the male sound in the pak0.pak
        val maleFilename = "player/male/" + base.substring(1)
        return AliasName(sexedFilename, maleFilename)
    }

    fun FindName(name: String?, create: Boolean): sfx_t? {
        var i: Int
        var sfx: sfx_t? = null

        if (name == null)
            Com.Error(Defines.ERR_FATAL, "S_FindName: NULL\n")
        if (name!!.length() == 0)
            Com.Error(Defines.ERR_FATAL, "S_FindName: empty name\n")

        if (name.length() >= Defines.MAX_QPATH)
            Com.Error(Defines.ERR_FATAL, "Sound name too long: " + name)

        // see if already loaded
        run {
            i = 0
            while (i < num_sfx) {
                if (name.equals(known_sfx[i].name)) {
                    return known_sfx[i]
                }
                i++
            }
        }

        if (!create)
            return null

        // find a free sfx
        run {
            i = 0
            while (i < num_sfx) {
                if (known_sfx[i].name == null)
                // registration_sequence < s_registration_sequence)
                    break
                i++
            }
        }

        if (i == num_sfx) {
            if (num_sfx == MAX_SFX)
                Com.Error(Defines.ERR_FATAL, "S_FindName: out of sfx_t")
            num_sfx++
        }

        sfx = known_sfx[i]
        sfx!!.clear()
        sfx!!.name = name
        sfx!!.registration_sequence = s_registration_sequence
        sfx!!.bufferId = i

        return sfx
    }

    /*
	==================
	S_AliasName

	==================
	*/
    fun AliasName(aliasname: String, truename: String): sfx_t {
        var sfx: sfx_t? = null
        val s: String
        var i: Int

        s = String(truename)

        // find a free sfx
        run {
            i = 0
            while (i < num_sfx) {
                if (known_sfx[i].name == null)
                    break
                i++
            }
        }

        if (i == num_sfx) {
            if (num_sfx == MAX_SFX)
                Com.Error(Defines.ERR_FATAL, "S_FindName: out of sfx_t")
            num_sfx++
        }

        sfx = known_sfx[i]
        sfx!!.clear()
        sfx!!.name = String(aliasname)
        sfx!!.registration_sequence = s_registration_sequence
        sfx!!.truename = s
        // set the AL bufferId
        sfx!!.bufferId = i

        return sfx
    }

    /*
	==============
	S_LoadSound
	==============
	*/
    public fun LoadSound(s: sfx_t): sfxcache_t? {
        if (s.isCached) return s.cache
        val sc = WaveLoader.LoadSound(s)
        if (sc != null) {
            initBuffer(sc!!.data, s.bufferId, sc!!.speed)
            s.isCached = true
            // free samples for GC
            s.cache.data = null
        }
        return sc
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#StartLocalSound(java.lang.String)
	 */
    public fun StartLocalSound(sound: String) {
        val sfx: sfx_t?

        sfx = RegisterSound(sound)
        if (sfx == null) {
            Com.Printf("S_StartLocalSound: can't cache " + sound + "\n")
            return
        }
        StartSound(null, Globals.cl.playernum + 1, 0, sfx, 1, 1, 0)
    }

    private val streamBuffer = sfxDataBuffer.slice().order(ByteOrder.BIG_ENDIAN).asShortBuffer()

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#RawSamples(int, int, int, int, byte[])
	 */
    public fun RawSamples(samples: Int, rate: Int, width: Int, channels: Int, data: ByteBuffer) {
        var width = width
        var data = data
        var format: Int
        if (channels == 2) {
            format = if ((width == 2))
                AL10.AL_FORMAT_STEREO16
            else
                AL10.AL_FORMAT_STEREO8
        } else {
            format = if ((width == 2))
                AL10.AL_FORMAT_MONO16
            else
                AL10.AL_FORMAT_MONO8
        }

        // convert to signed 16 bit samples
        if (format == AL10.AL_FORMAT_MONO8) {
            val sampleData = streamBuffer
            val value: Int
            for (i in 0..samples - 1) {
                value = (data.get(i) and 255) - 128
                sampleData.put(i, value.toShort())
            }
            format = AL10.AL_FORMAT_MONO16
            width = 2
            data = sfxDataBuffer.slice()
        }

        Channel.updateStream(data, samples * channels * width, format, rate)
    }

    public fun disableStreaming() {
        Channel.disableStreaming()
    }
    /*
	===============================================================================

	console functions

	===============================================================================
	*/

    fun Play() {
        var i: Int
        var name: String
        val sfx: sfx_t

        i = 1
        while (i < Cmd.Argc()) {
            name = String(Cmd.Argv(i))
            if (name.indexOf('.') == -1)
                name += ".wav"

            sfx = RegisterSound(name)
            StartSound(null, Globals.cl.playernum + 1, 0, sfx, 1.0.toFloat(), 1.0.toFloat(), 0.0.toFloat())
            i++
        }
    }

    fun SoundList() {
        var i: Int
        var sfx: sfx_t
        var sc: sfxcache_t?
        var size: Int
        var total: Int

        total = 0
        run {
            i = 0
            while (i < num_sfx) {
                sfx = known_sfx[i]
                if (sfx.registration_sequence == 0)
                    continue
                sc = sfx.cache
                if (sc != null) {
                    size = sc!!.length * sc!!.width * (sc!!.stereo + 1)
                    total += size
                    if (sc!!.loopstart >= 0)
                        Com.Printf("L")
                    else
                        Com.Printf(" ")
                    Com.Printf("(%2db) %6i : %s\n", Vargs(3).add(sc!!.width * 8).add(size).add(sfx.name))
                } else {
                    if (sfx.name.charAt(0) == '*')
                        Com.Printf("  placeholder : " + sfx.name + "\n")
                    else
                        Com.Printf("  not loaded  : " + sfx.name + "\n")
                }
                i++
            }
        }
        Com.Printf("Total resident: " + total + "\n")
    }

    fun SoundInfo_f() {

        Com.Printf("%5d stereo\n", Vargs(1).add(1))
        Com.Printf("%5d samples\n", Vargs(1).add(22050))
        Com.Printf("%5d samplebits\n", Vargs(1).add(16))
        Com.Printf("%5d speed\n", Vargs(1).add(44100))
    }

    companion object {

        {
            S.register(LWJGLSoundImpl())
        }


        var known_sfx = arrayOfNulls<sfx_t>(MAX_SFX)
        {
            for (i in known_sfx.indices)
                known_sfx[i] = sfx_t()
        }
        var num_sfx: Int = 0
    }

}
