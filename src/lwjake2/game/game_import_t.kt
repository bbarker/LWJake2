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
import lwjake2.qcommon.CM
import lwjake2.qcommon.Cbuf
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.PMove
import lwjake2.server.SV_GAME
import lwjake2.server.SV_INIT
import lwjake2.server.SV_SEND
import lwjake2.server.SV_WORLD

//
//	collection of functions provided by the main engine
//
public class game_import_t {
    // special messages
    public fun bprintf(printlevel: Int, s: String) {
        SV_SEND.SV_BroadcastPrintf(printlevel, s)
    }

    public fun dprintf(s: String) {
        SV_GAME.PF_dprintf(s)
    }

    public fun cprintf(ent: edict_t, printlevel: Int, s: String) {
        SV_GAME.PF_cprintf(ent, printlevel, s)
    }

    public fun centerprintf(ent: edict_t, s: String) {
        SV_GAME.PF_centerprintf(ent, s)
    }

    public fun sound(ent: edict_t, channel: Int, soundindex: Int, volume: Float, attenuation: Float, timeofs: Float) {
        SV_GAME.PF_StartSound(ent, channel, soundindex, volume, attenuation, timeofs)
    }

    public fun positioned_sound(origin: FloatArray, ent: edict_t, channel: Int, soundinedex: Int, volume: Float, attenuation: Float, timeofs: Float) {

        SV_SEND.SV_StartSound(origin, ent, channel, soundinedex, volume, attenuation, timeofs)
    }

    // config strings hold all the index strings, the lightstyles,
    // and misc data like the sky definition and cdtrack.
    // All of the current configstrings are sent to clients when
    // they connect, and changes are sent to all connected clients.
    public fun configstring(num: Int, string: String) {
        SV_GAME.PF_Configstring(num, string)
    }

    public fun error(err: String) {
        Com.Error(Defines.ERR_FATAL, err)
    }

    public fun error(level: Int, err: String) {
        SV_GAME.PF_error(level, err)
    }

    // the *index functions create configstrings and some internal server state
    public fun modelindex(name: String): Int {
        return SV_INIT.SV_ModelIndex(name)
    }

    public fun soundindex(name: String): Int {
        return SV_INIT.SV_SoundIndex(name)
    }

    public fun imageindex(name: String): Int {
        return SV_INIT.SV_ImageIndex(name)
    }

    public fun setmodel(ent: edict_t, name: String) {
        SV_GAME.PF_setmodel(ent, name)
    }

    // collision detection
    public fun trace(start: FloatArray, mins: FloatArray, maxs: FloatArray, end: FloatArray, passent: edict_t, contentmask: Int): trace_t {
        return SV_WORLD.SV_Trace(start, mins, maxs, end, passent, contentmask)
    }

    public var pointcontents: pmove_t.PointContentsAdapter = object : pmove_t.PointContentsAdapter() {
        public fun pointcontents(o: FloatArray): Int {
            return 0
        }
    }

    public fun inPHS(p1: FloatArray, p2: FloatArray): Boolean {
        return SV_GAME.PF_inPHS(p1, p2)
    }

    public fun SetAreaPortalState(portalnum: Int, open: Boolean) {
        CM.CM_SetAreaPortalState(portalnum, open)
    }

    public fun AreasConnected(area1: Int, area2: Int): Boolean {
        return CM.CM_AreasConnected(area1, area2)
    }

    // an entity will never be sent to a client or used for collision
    // if it is not passed to linkentity. If the size, position, or
    // solidity changes, it must be relinked.
    public fun linkentity(ent: edict_t) {
        SV_WORLD.SV_LinkEdict(ent)
    }

    public fun unlinkentity(ent: edict_t) {
        SV_WORLD.SV_UnlinkEdict(ent)
    }

    // call before removing an interactive edict
    public fun BoxEdicts(mins: FloatArray, maxs: FloatArray, list: Array<edict_t>, maxcount: Int, areatype: Int): Int {
        return SV_WORLD.SV_AreaEdicts(mins, maxs, list, maxcount, areatype)
    }

    public fun Pmove(pmove: pmove_t) {
        PMove.Pmove(pmove)
    }

    // player movement code common with client prediction
    // network messaging
    public fun multicast(origin: FloatArray, to: Int) {
        SV_SEND.SV_Multicast(origin, to)
    }

    public fun unicast(ent: edict_t, reliable: Boolean) {
        SV_GAME.PF_Unicast(ent, reliable)
    }


    public fun WriteByte(c: Int) {
        SV_GAME.PF_WriteByte(c)
    }

    public fun WriteShort(c: Int) {
        SV_GAME.PF_WriteShort(c)
    }

    public fun WriteString(s: String) {
        SV_GAME.PF_WriteString(s)
    }

    public fun WritePosition(pos: FloatArray) {
        SV_GAME.PF_WritePos(pos)
    }

    // some fractional bits
    public fun WriteDir(pos: FloatArray) {
        SV_GAME.PF_WriteDir(pos)
    }

    // console variable interaction
    public fun cvar(var_name: String, value: String, flags: Int): cvar_t {
        return Cvar.Get(var_name, value, flags)
    }

    // console variable interaction
    public fun cvar_set(var_name: String, value: String): cvar_t {
        return Cvar.Set(var_name, value)
    }

    // console variable interaction
    public fun cvar_forceset(var_name: String, value: String): cvar_t {
        return Cvar.ForceSet(var_name, value)
    }

    // ClientCommand and ServerCommand parameter access
    public fun argc(): Int {
        return Cmd.Argc()
    }


    public fun argv(n: Int): String {
        return Cmd.Argv(n)
    }

    // concatenation of all argv >= 1
    public fun args(): String {
        return Cmd.Args()
    }

    // add commands to the server console as if they were typed in
    // for map changing, etc
    public fun AddCommandString(text: String) {
        Cbuf.AddText(text)
    }

}