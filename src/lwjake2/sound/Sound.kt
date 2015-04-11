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

import java.nio.ByteBuffer

/**
 * Sound

 * @author cwei
 */
public trait Sound {

	public fun getName(): String

	public fun Init(): Boolean
	public fun Shutdown()

	/*
    =====================
    S_BeginRegistration
    =====================
    */
	public fun BeginRegistration()

	/*
    =====================
    S_RegisterSound
    =====================
    */
	public fun RegisterSound(sample: String): sfx_t

	/*
    =====================
    S_EndRegistration
    =====================
    */
	public fun EndRegistration()

	/*
    ==================
    S_StartLocalSound
    ==================
    */
	public fun StartLocalSound(sound: String)

	/*
    ====================
    S_StartSound

    Validates the parms and ques the sound up
    if pos is NULL, the sound will be dynamically sourced from the entity
    Entchannel 0 will never override a playing sound
    ====================
    */
	public fun StartSound(origin: FloatArray, entnum: Int, entchannel: Int, sfx: sfx_t, fvol: Float, attenuation: Float, timeofs: Float)

	/*
    ============
    S_Update

    Called once each time through the main loop
    ============
    */
	public fun Update(origin: FloatArray, forward: FloatArray, right: FloatArray, up: FloatArray)

	/*
    ============
    S_RawSamples

    Cinematic streaming and voice over network
    ============
    */
	public fun RawSamples(samples: Int, rate: Int, width: Int, channels: Int, data: ByteBuffer)

	public fun disableStreaming()
	/*
    ==================
    S_StopAllSounds
    ==================
    */
	public fun StopAllSounds()

	companion object {

		public val MAX_SFX: Int = Defines.MAX_SOUNDS * 2
		public val STREAM_QUEUE: Int = 8
	}

}
