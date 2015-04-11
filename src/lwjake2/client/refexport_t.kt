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

package lwjake2.client

import lwjake2.qcommon.xcommand_t
import lwjake2.render.image_t
import lwjake2.render.model_t
import lwjake2.sys.KBD

import java.awt.Dimension
import java.awt.DisplayMode

/**
 * refexport_t

 * @author cwei
 */
public trait refexport_t {
    // ============================================================================
    // public interface for Renderer implementations
    //
    // ref.h, refexport_t
    // ============================================================================
    //
    // these are the functions exported by the refresh module
    //
    // called when the library is loaded
    public fun Init(vid_xpos: Int, vid_ypos: Int): Boolean

    // called before the library is unloaded
    public fun Shutdown()

    // All data that will be used in a level should be
    // registered before rendering any frames to prevent disk hits,
    // but they can still be registered at a later time
    // if necessary.
    //
    // EndRegistration will free any remaining data that wasn't registered.
    // Any model_s or skin_s pointers from before the BeginRegistration
    // are no longer valid after EndRegistration.
    //
    // Skins and images need to be differentiated, because skins
    // are flood filled to eliminate mip map edge errors, and pics have
    // an implicit "pics/" prepended to the name. (a pic name that starts with a
    // slash will not use the "pics/" prefix or the ".pcx" postfix)
    public fun BeginRegistration(map: String)

    public fun RegisterModel(name: String): model_t
    public fun RegisterSkin(name: String): image_t
    public fun RegisterPic(name: String): image_t
    public fun SetSky(name: String, rotate: Float, /* vec3_t */
                      axis: FloatArray)

    public fun EndRegistration()

    public fun RenderFrame(fd: refdef_t)

    public fun DrawGetPicSize(dim: Dimension /* int *w, *h */, name: String)
    // will return 0 0 if not found
    public fun DrawPic(x: Int, y: Int, name: String)

    public fun DrawStretchPic(x: Int, y: Int, w: Int, h: Int, name: String)
    public fun DrawChar(x: Int, y: Int, num: Int)  // num is 8 bit ASCII
    public fun DrawTileClear(x: Int, y: Int, w: Int, h: Int, name: String)
    public fun DrawFill(x: Int, y: Int, w: Int, h: Int, c: Int)
    public fun DrawFadeScreen()

    // Draw images for cinematic rendering (which can have a different palette). Note that calls
    public fun DrawStretchRaw(x: Int, y: Int, w: Int, h: Int, cols: Int, rows: Int, data: ByteArray)

    /*
	** video mode and refresh state management entry points
	*/
    /* 256 r,g,b values;	null = game palette, size = 768 bytes */
    public fun CinematicSetPalette(palette: ByteArray)

    public fun BeginFrame(camera_separation: Float)
    public fun EndFrame()

    public fun AppActivate(activate: Boolean)

    /**


     */
    public fun updateScreen(callback: xcommand_t)

    public fun apiVersion(): Int

    public fun getModeList(): Array<DisplayMode>

    public fun getKeyboardHandler(): KBD
}
