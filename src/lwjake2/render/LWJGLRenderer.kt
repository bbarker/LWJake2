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

package lwjake2.render

import lwjake2.Defines
import lwjake2.client.VID
import lwjake2.client.refdef_t
import lwjake2.client.refexport_t
import lwjake2.render.lwjgl.Misc
import lwjake2.sys.KBD
import lwjake2.sys.LWJGLKBD

import java.awt.Dimension

/**
 * LWJGLRenderer

 * @author dsanders/cwei
 */
class LWJGLRenderer private() : Misc(), refexport_t, Ref {

    private val kbd = LWJGLKBD()

    // ============================================================================
    // public interface for Renderer implementations
    //
    // refexport_t (ref.h)
    // ============================================================================

    /**
     * @see jake2.client.refexport_t.Init
     */
    public fun Init(vid_xpos: Int, vid_ypos: Int): Boolean {

        // pre init
        if (!R_Init(vid_xpos, vid_ypos)) return false
        // post init
        val ok = R_Init2()
        if (!ok) {
            VID.Printf(Defines.PRINT_ALL, "Missing multi-texturing for LWJGL renderer\n")
        }
        return ok
    }

    /**
     * @see jake2.client.refexport_t.Shutdown
     */
    public fun Shutdown() {
        R_Shutdown()
    }

    /**
     * @see jake2.client.refexport_t.BeginRegistration
     */
    public fun BeginRegistration(map: String) {
        R_BeginRegistration(map)
    }

    /**
     * @see jake2.client.refexport_t.RegisterModel
     */
    public fun RegisterModel(name: String): model_t {
        return R_RegisterModel(name)
    }

    /**
     * @see jake2.client.refexport_t.RegisterSkin
     */
    public fun RegisterSkin(name: String): image_t {
        return R_RegisterSkin(name)
    }

    /**
     * @see jake2.client.refexport_t.RegisterPic
     */
    public fun RegisterPic(name: String): image_t {
        return Draw_FindPic(name)
    }

    /**
     * @see jake2.client.refexport_t.SetSky
     */
    public fun SetSky(name: String, rotate: Float, axis: FloatArray) {
        R_SetSky(name, rotate, axis)
    }

    /**
     * @see jake2.client.refexport_t.EndRegistration
     */
    public fun EndRegistration() {
        R_EndRegistration()
    }

    /**
     * @see jake2.client.refexport_t.RenderFrame
     */
    public fun RenderFrame(fd: refdef_t) {
        R_RenderFrame(fd)
    }

    /**
     * @see jake2.client.refexport_t.DrawGetPicSize
     */
    public fun DrawGetPicSize(dim: Dimension, name: String) {
        Draw_GetPicSize(dim, name)
    }

    /**
     * @see jake2.client.refexport_t.DrawPic
     */
    public fun DrawPic(x: Int, y: Int, name: String) {
        Draw_Pic(x, y, name)
    }

    /**
     * @see jake2.client.refexport_t.DrawStretchPic
     */
    public fun DrawStretchPic(x: Int, y: Int, w: Int, h: Int, name: String) {
        Draw_StretchPic(x, y, w, h, name)
    }

    /**
     * @see jake2.client.refexport_t.DrawChar
     */
    public fun DrawChar(x: Int, y: Int, num: Int) {
        Draw_Char(x, y, num)
    }

    /**
     * @see jake2.client.refexport_t.DrawTileClear
     */
    public fun DrawTileClear(x: Int, y: Int, w: Int, h: Int, name: String) {
        Draw_TileClear(x, y, w, h, name)
    }

    /**
     * @see jake2.client.refexport_t.DrawFill
     */
    public fun DrawFill(x: Int, y: Int, w: Int, h: Int, c: Int) {
        Draw_Fill(x, y, w, h, c)
    }

    /**
     * @see jake2.client.refexport_t.DrawFadeScreen
     */
    public fun DrawFadeScreen() {
        Draw_FadeScreen()
    }

    /**
     * @see jake2.client.refexport_t.DrawStretchRaw
     */
    public fun DrawStretchRaw(x: Int, y: Int, w: Int, h: Int, cols: Int, rows: Int, data: ByteArray) {
        Draw_StretchRaw(x, y, w, h, cols, rows, data)
    }

    /**
     * @see jake2.client.refexport_t.CinematicSetPalette
     */
    public fun CinematicSetPalette(palette: ByteArray) {
        R_SetPalette(palette)
    }

    /**
     * @see jake2.client.refexport_t.BeginFrame
     */
    public fun BeginFrame(camera_separation: Float) {
        R_BeginFrame(camera_separation)
    }

    /**
     * @see jake2.client.refexport_t.EndFrame
     */
    public fun EndFrame() {
        GLimp_EndFrame()
    }

    /**
     * @see jake2.client.refexport_t.AppActivate
     */
    public fun AppActivate(activate: Boolean) {
        GLimp_AppActivate(activate)
    }

    public fun apiVersion(): Int {
        return Defines.API_VERSION
    }

    // ============================================================================
    // Ref interface
    // ============================================================================

    public fun getName(): String {
        return DRIVER_NAME
    }

    public fun toString(): String {
        return DRIVER_NAME
    }

    public fun GetRefAPI(): refexport_t {
        return this
    }

    public fun getKeyboardHandler(): KBD {
        return kbd
    }

    companion object {

        public val DRIVER_NAME: String = "lwjgl"

        {
            Renderer.register(LWJGLRenderer())
        }
    }
}