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

import lwjake2.client.refdef_t
import lwjake2.client.refexport_t
import lwjake2.qcommon.xcommand_t
import lwjake2.sys.KBD

import java.awt.Dimension
import java.awt.DisplayMode

/**
 * DummyRenderer

 * @author cwei
 */
public class DummyRenderer : refexport_t {

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#Init(int, int)
	 */
    public fun Init(vid_xpos: Int, vid_ypos: Int): Boolean {
        return false
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#Shutdown()
	 */
    public fun Shutdown() {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#BeginRegistration(java.lang.String)
	 */
    public fun BeginRegistration(map: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#RegisterModel(java.lang.String)
	 */
    public fun RegisterModel(name: String): model_t? {
        return null
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#RegisterSkin(java.lang.String)
	 */
    public fun RegisterSkin(name: String): image_t? {
        return null
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#RegisterPic(java.lang.String)
	 */
    public fun RegisterPic(name: String): image_t? {
        return null
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#SetSky(java.lang.String, float, float[])
	 */
    public fun SetSky(name: String, rotate: Float, axis: FloatArray) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#EndRegistration()
	 */
    public fun EndRegistration() {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#RenderFrame(jake2.client.refdef_t)
	 */
    public fun RenderFrame(fd: refdef_t) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawGetPicSize(java.awt.Dimension, java.lang.String)
	 */
    public fun DrawGetPicSize(dim: Dimension, name: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawPic(int, int, java.lang.String)
	 */
    public fun DrawPic(x: Int, y: Int, name: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawStretchPic(int, int, int, int, java.lang.String)
	 */
    public fun DrawStretchPic(x: Int, y: Int, w: Int, h: Int, name: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawChar(int, int, int)
	 */
    public fun DrawChar(x: Int, y: Int, num: Int) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawTileClear(int, int, int, int, java.lang.String)
	 */
    public fun DrawTileClear(x: Int, y: Int, w: Int, h: Int, name: String) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawFill(int, int, int, int, int)
	 */
    public fun DrawFill(x: Int, y: Int, w: Int, h: Int, c: Int) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawFadeScreen()
	 */
    public fun DrawFadeScreen() {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#DrawStretchRaw(int, int, int, int, int, int, byte[])
	 */
    public fun DrawStretchRaw(x: Int, y: Int, w: Int, h: Int, cols: Int, rows: Int, data: ByteArray) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#CinematicSetPalette(byte[])
	 */
    public fun CinematicSetPalette(palette: ByteArray) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#BeginFrame(float)
	 */
    public fun BeginFrame(camera_separation: Float) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#EndFrame()
	 */
    public fun EndFrame() {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#AppActivate(boolean)
	 */
    public fun AppActivate(activate: Boolean) {
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#updateScreen(jake2.qcommon.xcommand_t)
	 */
    public fun updateScreen(callback: xcommand_t) {
        callback.execute()
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#apiVersion()
	 */
    public fun apiVersion(): Int {
        return 0
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#getModeList()
	 */
    public fun getModeList(): Array<DisplayMode>? {
        return null
    }

    /* (non-Javadoc)
	 * @see jake2.client.refexport_t#getKeyboardHandler()
	 */
    public fun getKeyboardHandler(): KBD? {
        return null
    }

}
