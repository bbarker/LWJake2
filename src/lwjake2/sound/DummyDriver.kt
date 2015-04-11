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

import java.nio.ByteBuffer

/**
 * DummyDriver

 * @author cwei
 */
public class DummyDriver private() : Sound {

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#Init()
	 */
    public fun Init(): Boolean {
        return true
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#Shutdown()
	 */
    public fun Shutdown() {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#BeginRegistration()
	 */
    public fun BeginRegistration() {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#RegisterSound(java.lang.String)
	 */
    public fun RegisterSound(sample: String): sfx_t? {
        return null
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#EndRegistration()
	 */
    public fun EndRegistration() {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#StartLocalSound(java.lang.String)
	 */
    public fun StartLocalSound(sound: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#StartSound(float[], int, int, jake2.sound.sfx_t, float, float, float)
	 */
    public fun StartSound(origin: FloatArray, entnum: Int, entchannel: Int, sfx: sfx_t, fvol: Float, attenuation: Float, timeofs: Float) {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#Update(float[], float[], float[], float[])
	 */
    public fun Update(origin: FloatArray, forward: FloatArray, right: FloatArray, up: FloatArray) {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#RawSamples(int, int, int, int, byte[])
	 */
    public fun RawSamples(samples: Int, rate: Int, width: Int, channels: Int, data: ByteBuffer) {
    }

    public fun disableStreaming() {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#StopAllSounds()
	 */
    public fun StopAllSounds() {
    }

    /* (non-Javadoc)
	 * @see jake2.sound.Sound#getName()
	 */
    public fun getName(): String {
        return "dummy"
    }

    companion object {

        {
            S.register(DummyDriver())
        }
    }
}
