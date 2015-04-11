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

package lwjake2.sound

import lwjake2.Defines
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar

import java.nio.ByteBuffer
import java.util.Vector

/**
 * S
 */
public class S {
    companion object {

        var impl: Sound
        var s_impl: cvar_t

        var drivers = Vector<Sound>(1)

        /**
         * Searches for and initializes all known sound drivers.
         */
        {
            // dummy driver (no sound)
            try {
                Class.forName("lwjake2.sound.DummyDriver")
                // initialize impl with the default value
                // this is  necessary for dedicated mode
                useDriver("dummy")
            } catch (e: Throwable) {
                Com.DPrintf("could not init dummy sound driver class.")
            }


            try {
                Class.forName("org.lwjgl.openal.AL")
                Class.forName("lwjake2.sound.lwjgl.LWJGLSoundImpl")
            } catch (e: Throwable) {
                // ignore the lwjgl driver if runtime not in classpath
                Com.DPrintf("could not init lwjgl sound driver class.")
            }

        }

        /**
         * Registers a new Sound Implementor.
         */
        public fun register(driver: Sound?) {
            if (driver == null) {
                throw IllegalArgumentException("Sound implementation can't be null")
            }
            if (!drivers.contains(driver)) {
                drivers.add(driver)
            }
        }

        /**
         * Switches to the specific sound driver.
         */
        public fun useDriver(driverName: String) {
            var driver: Sound? = null
            val count = drivers.size()
            for (i in 0..count - 1) {
                driver = drivers.get(i)
                if (driver!!.getName().equals(driverName)) {
                    impl = driver
                    return
                }
            }
            // if driver not found use dummy
            impl = drivers.lastElement()
        }

        /**
         * Initializes the sound module.
         */
        public fun Init() {

            Com.Printf("\n------- sound initialization -------\n")

            val cv = Cvar.Get("s_initsound", "1", 0)
            if (cv.value == 0.0.toFloat()) {
                Com.Printf("not initializing.\n")
                useDriver("dummy")
                return
            }

            // set the last registered driver as default
            var defaultDriver = "dummy"
            if (drivers.size() > 1) {
                defaultDriver = (drivers.lastElement()).getName()
            }

            s_impl = Cvar.Get("s_impl", defaultDriver, Defines.CVAR_ARCHIVE)
            useDriver(s_impl.string)

            if (impl.Init()) {
                // driver ok
                Cvar.Set("s_impl", impl.getName())
            } else {
                // fallback
                useDriver("dummy")
            }

            Com.Printf("\n------- use sound driver \"" + impl.getName() + "\" -------\n")
            StopAllSounds()
        }

        public fun Shutdown() {
            impl.Shutdown()
        }

        /**
         * Called before the sounds are to be loaded and registered.
         */
        public fun BeginRegistration() {
            impl.BeginRegistration()
        }

        /**
         * Registers and loads a sound.
         */
        public fun RegisterSound(sample: String): sfx_t {
            return impl.RegisterSound(sample)
        }

        /**
         * Called after all sounds are registered and loaded.
         */
        public fun EndRegistration() {
            impl.EndRegistration()
        }

        /**
         * Starts a local sound.
         */
        public fun StartLocalSound(sound: String) {
            impl.StartLocalSound(sound)
        }

        /**
         * StartSound - Validates the parms and ques the sound up
         * if pos is NULL, the sound will be dynamically sourced from the entity
         * Entchannel 0 will never override a playing sound
         */
        public fun StartSound(origin: FloatArray, entnum: Int, entchannel: Int, sfx: sfx_t, fvol: Float, attenuation: Float, timeofs: Float) {
            impl.StartSound(origin, entnum, entchannel, sfx, fvol, attenuation, timeofs)
        }

        /**
         * Updates the sound renderer according to the changes in the environment,
         * called once each time through the main loop.
         */
        public fun Update(origin: FloatArray, forward: FloatArray, right: FloatArray, up: FloatArray) {
            impl.Update(origin, forward, right, up)
        }

        /**
         * Cinematic streaming and voice over network.
         */
        public fun RawSamples(samples: Int, rate: Int, width: Int, channels: Int, data: ByteBuffer) {
            impl.RawSamples(samples, rate, width, channels, data)
        }

        /**
         * Switches off the sound streaming.
         */
        public fun disableStreaming() {
            impl.disableStreaming()
        }

        /**
         * Stops all sounds.
         */
        public fun StopAllSounds() {
            impl.StopAllSounds()
        }

        public fun getDriverName(): String {
            return impl.getName()
        }

        /**
         * Returns a string array containing all sound driver names.
         */
        public fun getDriverNames(): Array<String> {
            val names = arrayOfNulls<String>(drivers.size())
            for (i in names.indices) {
                names[i] = (drivers.get(i)).getName()
            }
            return names
        }

        /**
         * This is used, when resampling to this default sampling rate is activated
         * in the wavloader. It is placed here that sound implementors can override
         * this one day.
         */
        public fun getDefaultSampleRate(): Int {
            return 44100
        }
    }
}