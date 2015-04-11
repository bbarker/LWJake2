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

package lwjake2

import lwjake2.client.centity_t
import lwjake2.client.client_state_t
import lwjake2.client.client_static_t
import lwjake2.client.console_t
import lwjake2.client.refexport_t
import lwjake2.client.viddef_t
import lwjake2.client.vrect_t
import lwjake2.game.cmdalias_t
import lwjake2.game.cvar_t
import lwjake2.game.entity_state_t
import lwjake2.qcommon.netadr_t
import lwjake2.qcommon.sizebuf_t
import lwjake2.render.DummyRenderer
import lwjake2.render.model_t

import java.io.FileWriter
import java.io.RandomAccessFile
import java.util.Random

/**
 * Globals ist the collection of global variables and constants.
 * It is more elegant to use these vars by inheritance to separate
 * it with eclipse refactoring later.

 * As consequence you dont have to touch that much code this time.
 */
open public class Globals : Defines() {
    companion object {

        public val __DATE__: String = "2003"

        public val VERSION: Float = 3.21.toFloat()

        public val BASEDIRNAME: String = "baseq2"

        /*
	 * global variables
	 */
        public var curtime: Int = 0
        public var cmd_wait: Boolean = false

        public var alias_count: Int = 0
        public var c_traces: Int = 0
        public var c_brush_traces: Int = 0
        public var c_pointcontents: Int = 0
        public var server_state: Int = 0

        public var cl_add_blend: cvar_t
        public var cl_add_entities: cvar_t
        public var cl_add_lights: cvar_t
        public var cl_add_particles: cvar_t
        public var cl_anglespeedkey: cvar_t
        public var cl_autoskins: cvar_t
        public var cl_footsteps: cvar_t
        public var cl_forwardspeed: cvar_t
        public var cl_gun: cvar_t
        public var cl_maxfps: cvar_t
        public var cl_noskins: cvar_t
        public var cl_pitchspeed: cvar_t
        public var cl_predict: cvar_t
        public var cl_run: cvar_t
        public var cl_sidespeed: cvar_t
        public var cl_stereo: cvar_t
        public var cl_stereo_separation: cvar_t
        public var cl_timedemo: cvar_t = cvar_t()
        public var cl_timeout: cvar_t
        public var cl_upspeed: cvar_t
        public var cl_yawspeed: cvar_t
        public var dedicated: cvar_t
        public var developer: cvar_t
        public var fixedtime: cvar_t
        public var freelook: cvar_t
        public var host_speeds: cvar_t
        public var log_stats: cvar_t
        public var logfile_active: cvar_t
        public var lookspring: cvar_t
        public var lookstrafe: cvar_t
        public var nostdout: cvar_t
        public var sensitivity: cvar_t
        public var showtrace: cvar_t
        public var timescale: cvar_t
        public var in_mouse: cvar_t
        public var in_joystick: cvar_t


        public var net_message: sizebuf_t = sizebuf_t()

        /*
	=============================================================================
	
							COMMAND BUFFER
	
	=============================================================================
	*/

        public var cmd_text: sizebuf_t = sizebuf_t()

        public var defer_text_buf: ByteArray = ByteArray(8192)

        public var cmd_text_buf: ByteArray = ByteArray(8192)
        public var cmd_alias: cmdalias_t

        //=============================================================================

        public var net_message_buffer: ByteArray = ByteArray(MAX_MSGLEN)

        public var time_before_game: Int = 0
        public var time_after_game: Int = 0
        public var time_before_ref: Int = 0
        public var time_after_ref: Int = 0

        public var log_stats_file: FileWriter? = null

        public var m_pitch: cvar_t
        public var m_yaw: cvar_t
        public var m_forward: cvar_t
        public var m_side: cvar_t

        public var cl_lightlevel: cvar_t

        //
        //	   userinfo
        //
        public var info_password: cvar_t
        public var info_spectator: cvar_t
        public var name: cvar_t
        public var skin: cvar_t
        public var rate: cvar_t
        public var fov: cvar_t
        public var msg: cvar_t
        public var hand: cvar_t
        public var gender: cvar_t
        public var gender_auto: cvar_t

        public var cl_vwep: cvar_t

        public var cls: client_static_t = client_static_t()
        public var cl: client_state_t = client_state_t()

        public var cl_entities: Array<centity_t> = arrayOfNulls<centity_t>(Defines.MAX_EDICTS)
        {
            for (i in cl_entities.indices) {
                cl_entities[i] = centity_t()
            }
        }

        public var cl_parse_entities: Array<entity_state_t> = arrayOfNulls<entity_state_t>(Defines.MAX_PARSE_ENTITIES)

        {
            for (i in cl_parse_entities.indices) {
                cl_parse_entities[i] = entity_state_t(null)
            }
        }

        public var rcon_client_password: cvar_t
        public var rcon_address: cvar_t

        public var cl_shownet: cvar_t
        public var cl_showmiss: cvar_t
        public var cl_showclamp: cvar_t

        public var cl_paused: cvar_t

        // client/anorms.h
        public val bytedirs: Array<FloatArray> = array<FloatArray>(
                /**
                 */
                
floatArray((-0.525731.toFloat()).toFloat(), 0.000000.toFloat(), 0.850651.toFloat()).toFloat(), 
floatArray((-0.442863.toFloat()).toFloat(), 0.238856.toFloat(), 0.864188.toFloat()).toFloat(), 
floatArray((-0.295242.toFloat()).toFloat(), 0.000000.toFloat(), 0.955423.toFloat()).toFloat(), 
floatArray((-0.309017.toFloat()).toFloat(), 0.500000.toFloat(), 0.809017.toFloat()).toFloat(), 
floatArray((-0.162460.toFloat()).toFloat(), 0.262866.toFloat(), 0.951056.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.000000.toFloat(), 1.000000.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.850651.toFloat(), 0.525731.toFloat()).toFloat(), 
floatArray((-0.147621.toFloat()).toFloat(), 0.716567.toFloat(), 0.681718.toFloat()).toFloat(), 
floatArray(0.147621.toFloat(), 0.716567.toFloat(), 0.681718.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.525731.toFloat(), 0.850651.toFloat()).toFloat(), 
floatArray(0.309017.toFloat(), 0.500000.toFloat(), 0.809017.toFloat()).toFloat(), 
floatArray(0.525731.toFloat(), 0.000000.toFloat(), 0.850651.toFloat()).toFloat(), 
floatArray(0.295242.toFloat(), 0.000000.toFloat(), 0.955423.toFloat()).toFloat(), 
floatArray(0.442863.toFloat(), 0.238856.toFloat(), 0.864188.toFloat()).toFloat(), 
floatArray(0.162460.toFloat(), 0.262866.toFloat(), 0.951056.toFloat()).toFloat(), 
floatArray((-0.681718.toFloat()).toFloat(), 0.147621.toFloat(), 0.716567.toFloat()).toFloat(), 
floatArray((-0.809017.toFloat()).toFloat(), 0.309017.toFloat(), 0.500000.toFloat()).toFloat(), 
floatArray((-0.587785.toFloat()).toFloat(), 0.425325.toFloat(), 0.688191.toFloat()).toFloat(), 
floatArray((-0.850651.toFloat()).toFloat(), 0.525731.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.864188.toFloat()).toFloat(), 0.442863.toFloat(), 0.238856.toFloat()).toFloat(), 
floatArray((-0.716567.toFloat()).toFloat(), 0.681718.toFloat(), 0.147621.toFloat()).toFloat(), 
floatArray((-0.688191.toFloat()).toFloat(), 0.587785.toFloat(), 0.425325.toFloat()).toFloat(), 
floatArray((-0.500000.toFloat()).toFloat(), 0.809017.toFloat(), 0.309017.toFloat()).toFloat(), 
floatArray((-0.238856.toFloat()).toFloat(), 0.864188.toFloat(), 0.442863.toFloat()).toFloat(), 
floatArray((-0.425325.toFloat()).toFloat(), 0.688191.toFloat(), 0.587785.toFloat()).toFloat(), 
floatArray((-0.716567.toFloat()).toFloat(), 0.681718.toFloat(), (-0.147621.toFloat()).toFloat()).toFloat(), 
floatArray((-0.500000.toFloat()).toFloat(), 0.809017.toFloat(), (-0.309017.toFloat()).toFloat()).toFloat(), 
floatArray((-0.525731.toFloat()).toFloat(), 0.850651.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.850651.toFloat(), (-0.525731.toFloat()).toFloat()).toFloat(), 
floatArray((-0.238856.toFloat()).toFloat(), 0.864188.toFloat(), (-0.442863.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.955423.toFloat(), (-0.295242.toFloat()).toFloat()).toFloat(), 
floatArray((-0.262866.toFloat()).toFloat(), 0.951056.toFloat(), (-0.162460.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 1.000000.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.955423.toFloat(), 0.295242.toFloat()).toFloat(), 
floatArray((-0.262866.toFloat()).toFloat(), 0.951056.toFloat(), 0.162460.toFloat()).toFloat(), 
floatArray(0.238856.toFloat(), 0.864188.toFloat(), 0.442863.toFloat()).toFloat(), 
floatArray(0.262866.toFloat(), 0.951056.toFloat(), 0.162460.toFloat()).toFloat(), 
floatArray(0.500000.toFloat(), 0.809017.toFloat(), 0.309017.toFloat()).toFloat(), 
floatArray(0.238856.toFloat(), 0.864188.toFloat(), (-0.442863.toFloat()).toFloat()).toFloat(), 
floatArray(0.262866.toFloat(), 0.951056.toFloat(), (-0.162460.toFloat()).toFloat()).toFloat(), 
floatArray(0.500000.toFloat(), 0.809017.toFloat(), (-0.309017.toFloat()).toFloat()).toFloat(), 
floatArray(0.850651.toFloat(), 0.525731.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.716567.toFloat(), 0.681718.toFloat(), 0.147621.toFloat()).toFloat(), 
floatArray(0.716567.toFloat(), 0.681718.toFloat(), (-0.147621.toFloat()).toFloat()).toFloat(), 
floatArray(0.525731.toFloat(), 0.850651.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.425325.toFloat(), 0.688191.toFloat(), 0.587785.toFloat()).toFloat(), 
floatArray(0.864188.toFloat(), 0.442863.toFloat(), 0.238856.toFloat()).toFloat(), 
floatArray(0.688191.toFloat(), 0.587785.toFloat(), 0.425325.toFloat()).toFloat(), 
floatArray(0.809017.toFloat(), 0.309017.toFloat(), 0.500000.toFloat()).toFloat(), 
floatArray(0.681718.toFloat(), 0.147621.toFloat(), 0.716567.toFloat()).toFloat(), 
floatArray(0.587785.toFloat(), 0.425325.toFloat(), 0.688191.toFloat()).toFloat(), 
floatArray(0.955423.toFloat(), 0.295242.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(1.000000.toFloat(), 0.000000.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.951056.toFloat(), 0.162460.toFloat(), 0.262866.toFloat()).toFloat(), 
floatArray(0.850651.toFloat(), (-0.525731.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.955423.toFloat(), (-0.295242.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.864188.toFloat(), (-0.442863.toFloat()).toFloat(), 0.238856.toFloat()).toFloat(), 
floatArray(0.951056.toFloat(), (-0.162460.toFloat()).toFloat(), 0.262866.toFloat()).toFloat(), 
floatArray(0.809017.toFloat(), (-0.309017.toFloat()).toFloat(), 0.500000.toFloat()).toFloat(), 
floatArray(0.681718.toFloat(), (-0.147621.toFloat()).toFloat(), 0.716567.toFloat()).toFloat(), 
floatArray(0.850651.toFloat(), 0.000000.toFloat(), 0.525731.toFloat()).toFloat(), 
floatArray(0.864188.toFloat(), 0.442863.toFloat(), (-0.238856.toFloat()).toFloat()).toFloat(), 
floatArray(0.809017.toFloat(), 0.309017.toFloat(), (-0.500000.toFloat()).toFloat()).toFloat(), 
floatArray(0.951056.toFloat(), 0.162460.toFloat(), (-0.262866.toFloat()).toFloat()).toFloat(), 
floatArray(0.525731.toFloat(), 0.000000.toFloat(), (-0.850651.toFloat()).toFloat()).toFloat(), 
floatArray(0.681718.toFloat(), 0.147621.toFloat(), (-0.716567.toFloat()).toFloat()).toFloat(), 
floatArray(0.681718.toFloat(), (-0.147621.toFloat()).toFloat(), (-0.716567.toFloat()).toFloat()).toFloat(), 
floatArray(0.850651.toFloat(), 0.000000.toFloat(), (-0.525731.toFloat()).toFloat()).toFloat(), 
floatArray(0.809017.toFloat(), (-0.309017.toFloat()).toFloat(), (-0.500000.toFloat()).toFloat()).toFloat(), 
floatArray(0.864188.toFloat(), (-0.442863.toFloat()).toFloat(), (-0.238856.toFloat()).toFloat()).toFloat(), 
floatArray(0.951056.toFloat(), (-0.162460.toFloat()).toFloat(), (-0.262866.toFloat()).toFloat()).toFloat(), 
floatArray(0.147621.toFloat(), 0.716567.toFloat(), (-0.681718.toFloat()).toFloat()).toFloat(), 
floatArray(0.309017.toFloat(), 0.500000.toFloat(), (-0.809017.toFloat()).toFloat()).toFloat(), 
floatArray(0.425325.toFloat(), 0.688191.toFloat(), (-0.587785.toFloat()).toFloat()).toFloat(), 
floatArray(0.442863.toFloat(), 0.238856.toFloat(), (-0.864188.toFloat()).toFloat()).toFloat(), 
floatArray(0.587785.toFloat(), 0.425325.toFloat(), (-0.688191.toFloat()).toFloat()).toFloat(), 
floatArray(0.688191.toFloat(), 0.587785.toFloat(), (-0.425325.toFloat()).toFloat()).toFloat(), 
floatArray((-0.147621.toFloat()).toFloat(), 0.716567.toFloat(), (-0.681718.toFloat()).toFloat()).toFloat(), 
floatArray((-0.309017.toFloat()).toFloat(), 0.500000.toFloat(), (-0.809017.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.525731.toFloat(), (-0.850651.toFloat()).toFloat()).toFloat(), 
floatArray((-0.525731.toFloat()).toFloat(), 0.000000.toFloat(), (-0.850651.toFloat()).toFloat()).toFloat(), 
floatArray((-0.442863.toFloat()).toFloat(), 0.238856.toFloat(), (-0.864188.toFloat()).toFloat()).toFloat(), 
floatArray((-0.295242.toFloat()).toFloat(), 0.000000.toFloat(), (-0.955423.toFloat()).toFloat()).toFloat(), 
floatArray((-0.162460.toFloat()).toFloat(), 0.262866.toFloat(), (-0.951056.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), 0.000000.toFloat(), (-1.000000.toFloat()).toFloat()).toFloat(), 
floatArray(0.295242.toFloat(), 0.000000.toFloat(), (-0.955423.toFloat()).toFloat()).toFloat(), 
floatArray(0.162460.toFloat(), 0.262866.toFloat(), (-0.951056.toFloat()).toFloat()).toFloat(), 
floatArray((-0.442863.toFloat()).toFloat(), (-0.238856.toFloat()).toFloat(), (-0.864188.toFloat()).toFloat()).toFloat(), 
floatArray((-0.309017.toFloat()).toFloat(), (-0.500000.toFloat()).toFloat(), (-0.809017.toFloat()).toFloat()).toFloat(), 
floatArray((-0.162460.toFloat()).toFloat(), (-0.262866.toFloat()).toFloat(), (-0.951056.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.850651.toFloat()).toFloat(), (-0.525731.toFloat()).toFloat()).toFloat(), 
floatArray((-0.147621.toFloat()).toFloat(), (-0.716567.toFloat()).toFloat(), (-0.681718.toFloat()).toFloat()).toFloat(), 
floatArray(0.147621.toFloat(), (-0.716567.toFloat()).toFloat(), (-0.681718.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.525731.toFloat()).toFloat(), (-0.850651.toFloat()).toFloat()).toFloat(), 
floatArray(0.309017.toFloat(), (-0.500000.toFloat()).toFloat(), (-0.809017.toFloat()).toFloat()).toFloat(), 
floatArray(0.442863.toFloat(), (-0.238856.toFloat()).toFloat(), (-0.864188.toFloat()).toFloat()).toFloat(), 
floatArray(0.162460.toFloat(), (-0.262866.toFloat()).toFloat(), (-0.951056.toFloat()).toFloat()).toFloat(), 
floatArray(0.238856.toFloat(), (-0.864188.toFloat()).toFloat(), (-0.442863.toFloat()).toFloat()).toFloat(), 
floatArray(0.500000.toFloat(), (-0.809017.toFloat()).toFloat(), (-0.309017.toFloat()).toFloat()).toFloat(), 
floatArray(0.425325.toFloat(), (-0.688191.toFloat()).toFloat(), (-0.587785.toFloat()).toFloat()).toFloat(), 
floatArray(0.716567.toFloat(), (-0.681718.toFloat()).toFloat(), (-0.147621.toFloat()).toFloat()).toFloat(), 
floatArray(0.688191.toFloat(), (-0.587785.toFloat()).toFloat(), (-0.425325.toFloat()).toFloat()).toFloat(), 
floatArray(0.587785.toFloat(), (-0.425325.toFloat()).toFloat(), (-0.688191.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.955423.toFloat()).toFloat(), (-0.295242.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-1.000000.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray(0.262866.toFloat(), (-0.951056.toFloat()).toFloat(), (-0.162460.toFloat()).toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.850651.toFloat()).toFloat(), 0.525731.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.955423.toFloat()).toFloat(), 0.295242.toFloat()).toFloat(), 
floatArray(0.238856.toFloat(), (-0.864188.toFloat()).toFloat(), 0.442863.toFloat()).toFloat(), 
floatArray(0.262866.toFloat(), (-0.951056.toFloat()).toFloat(), 0.162460.toFloat()).toFloat(), 
floatArray(0.500000.toFloat(), (-0.809017.toFloat()).toFloat(), 0.309017.toFloat()).toFloat(), 
floatArray(0.716567.toFloat(), (-0.681718.toFloat()).toFloat(), 0.147621.toFloat()).toFloat(), 
floatArray(0.525731.toFloat(), (-0.850651.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.238856.toFloat()).toFloat(), (-0.864188.toFloat()).toFloat(), (-0.442863.toFloat()).toFloat()).toFloat(), 
floatArray((-0.500000.toFloat()).toFloat(), (-0.809017.toFloat()).toFloat(), (-0.309017.toFloat()).toFloat()).toFloat(), 
floatArray((-0.262866.toFloat()).toFloat(), (-0.951056.toFloat()).toFloat(), (-0.162460.toFloat()).toFloat()).toFloat(), 
floatArray((-0.850651.toFloat()).toFloat(), (-0.525731.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.716567.toFloat()).toFloat(), (-0.681718.toFloat()).toFloat(), (-0.147621.toFloat()).toFloat()).toFloat(), 
floatArray((-0.716567.toFloat()).toFloat(), (-0.681718.toFloat()).toFloat(), 0.147621.toFloat()).toFloat(), 
floatArray((-0.525731.toFloat()).toFloat(), (-0.850651.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.500000.toFloat()).toFloat(), (-0.809017.toFloat()).toFloat(), 0.309017.toFloat()).toFloat(), 
floatArray((-0.238856.toFloat()).toFloat(), (-0.864188.toFloat()).toFloat(), 0.442863.toFloat()).toFloat(), 
floatArray((-0.262866.toFloat()).toFloat(), (-0.951056.toFloat()).toFloat(), 0.162460.toFloat()).toFloat(), 
floatArray((-0.864188.toFloat()).toFloat(), (-0.442863.toFloat()).toFloat(), 0.238856.toFloat()).toFloat(), 
floatArray((-0.809017.toFloat()).toFloat(), (-0.309017.toFloat()).toFloat(), 0.500000.toFloat()).toFloat(), 
floatArray((-0.688191.toFloat()).toFloat(), (-0.587785.toFloat()).toFloat(), 0.425325.toFloat()).toFloat(), 
floatArray((-0.681718.toFloat()).toFloat(), (-0.147621.toFloat()).toFloat(), 0.716567.toFloat()).toFloat(), 
floatArray((-0.442863.toFloat()).toFloat(), (-0.238856.toFloat()).toFloat(), 0.864188.toFloat()).toFloat(), 
floatArray((-0.587785.toFloat()).toFloat(), (-0.425325.toFloat()).toFloat(), 0.688191.toFloat()).toFloat(), 
floatArray((-0.309017.toFloat()).toFloat(), (-0.500000.toFloat()).toFloat(), 0.809017.toFloat()).toFloat(), 
floatArray((-0.147621.toFloat()).toFloat(), (-0.716567.toFloat()).toFloat(), 0.681718.toFloat()).toFloat(), 
floatArray((-0.425325.toFloat()).toFloat(), (-0.688191.toFloat()).toFloat(), 0.587785.toFloat()).toFloat(), 
floatArray((-0.162460.toFloat()).toFloat(), (-0.262866.toFloat()).toFloat(), 0.951056.toFloat()).toFloat(), 
floatArray(0.442863.toFloat(), (-0.238856.toFloat()).toFloat(), 0.864188.toFloat()).toFloat(), 
floatArray(0.162460.toFloat(), (-0.262866.toFloat()).toFloat(), 0.951056.toFloat()).toFloat(), 
floatArray(0.309017.toFloat(), (-0.500000.toFloat()).toFloat(), 0.809017.toFloat()).toFloat(), 
floatArray(0.147621.toFloat(), (-0.716567.toFloat()).toFloat(), 0.681718.toFloat()).toFloat(), 
floatArray(0.000000.toFloat(), (-0.525731.toFloat()).toFloat(), 0.850651.toFloat()).toFloat(), 
floatArray(0.425325.toFloat(), (-0.688191.toFloat()).toFloat(), 0.587785.toFloat()).toFloat(), 
floatArray(0.587785.toFloat(), (-0.425325.toFloat()).toFloat(), 0.688191.toFloat()).toFloat(), 
floatArray(0.688191.toFloat(), (-0.587785.toFloat()).toFloat(), 0.425325.toFloat()).toFloat(), 
floatArray((-0.955423.toFloat()).toFloat(), 0.295242.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.951056.toFloat()).toFloat(), 0.162460.toFloat(), 0.262866.toFloat()).toFloat(), 
floatArray((-1.000000.toFloat()).toFloat(), 0.000000.toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.850651.toFloat()).toFloat(), 0.000000.toFloat(), 0.525731.toFloat()).toFloat(), 
floatArray((-0.955423.toFloat()).toFloat(), (-0.295242.toFloat()).toFloat(), 0.000000.toFloat()).toFloat(), 
floatArray((-0.951056.toFloat()).toFloat(), (-0.162460.toFloat()).toFloat(), 0.262866.toFloat()).toFloat(), 
floatArray((-0.864188.toFloat()).toFloat(), 0.442863.toFloat(), (-0.238856.toFloat()).toFloat()).toFloat(), 
floatArray((-0.951056.toFloat()).toFloat(), 0.162460.toFloat(), (-0.262866.toFloat()).toFloat()).toFloat(), 
floatArray((-0.809017.toFloat()).toFloat(), 0.309017.toFloat(), (-0.500000.toFloat()).toFloat()).toFloat(), 
floatArray((-0.864188.toFloat()).toFloat(), (-0.442863.toFloat()).toFloat(), (-0.238856.toFloat()).toFloat()).toFloat(), 
floatArray((-0.951056.toFloat()).toFloat(), (-0.162460.toFloat()).toFloat(), (-0.262866.toFloat()).toFloat()).toFloat(), 
floatArray((-0.809017.toFloat()).toFloat(), (-0.309017.toFloat()).toFloat(), (-0.500000.toFloat()).toFloat()).toFloat(), 
floatArray((-0.681718.toFloat()).toFloat(), 0.147621.toFloat(), (-0.716567.toFloat()).toFloat()).toFloat(), 
floatArray((-0.681718.toFloat()).toFloat(), (-0.147621.toFloat()).toFloat(), (-0.716567.toFloat()).toFloat()).toFloat(), 
floatArray((-0.850651.toFloat()).toFloat(), 0.000000.toFloat(), (-0.525731.toFloat()).toFloat()).toFloat(), 
floatArray((-0.688191.toFloat()).toFloat(), 0.587785.toFloat(), (-0.425325.toFloat()).toFloat()).toFloat(), 
floatArray((-0.587785.toFloat()).toFloat(), 0.425325.toFloat(), (-0.688191.toFloat()).toFloat()).toFloat(), 
floatArray((-0.425325.toFloat()).toFloat(), 0.688191.toFloat(), (-0.587785.toFloat()).toFloat()).toFloat(), 
floatArray((-0.425325.toFloat()).toFloat(), (-0.688191.toFloat()).toFloat(), (-0.587785.toFloat()).toFloat()).toFloat(), 
floatArray((-0.587785.toFloat()).toFloat(), (-0.425325.toFloat()).toFloat(), (-0.688191.toFloat()).toFloat()).toFloat(), 
floatArray((-0.688191.toFloat()).toFloat(), (-0.587785.toFloat()).toFloat(), (-0.425325.toFloat()).toFloat()).toFloat())

        public var userinfo_modified: Boolean = false

        public var cvar_vars: cvar_t
        public val con: console_t = console_t()
        public var con_notifytime: cvar_t
        public var viddef: viddef_t = viddef_t()
        // Renderer interface used by VID, SCR, ...
        public var re: refexport_t = DummyRenderer()

        public var keybindings: Array<String> = arrayOfNulls(256)
        public var keydown: BooleanArray = BooleanArray(256)
        public var chat_team: Boolean = false
        public var chat_buffer: String = ""
        public var key_lines: Array<ByteArray> = arrayOfNulls(32)
        public var key_linepos: Int = 0

        {
            for (i in key_lines.indices)
                key_lines[i] = ByteArray(Defines.MAXCMDLINE)
        }

        public var edit_line: Int = 0

        public var crosshair: cvar_t
        public var scr_vrect: vrect_t = vrect_t()
        public var sys_frame_time: Int = 0
        public var chat_bufferlen: Int = 0
        public var gun_frame: Int = 0
        public var gun_model: model_t
        public var net_from: netadr_t = netadr_t()

        // logfile
        public var logfile: RandomAccessFile? = null

        public var vec3_origin: FloatArray = floatArray(0.0.toFloat(), 0.0.toFloat(), 0.0.toFloat())

        public var m_filter: cvar_t
        public var vidref_val: Int = VIDREF_GL

        public var rnd: Random = Random()
    }
}
