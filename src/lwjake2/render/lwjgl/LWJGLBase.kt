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

package lwjake2.render.lwjgl

import lwjake2.Defines
import lwjake2.client.VID
import lwjake2.client.viddef_t
import lwjake2.game.cvar_t
import lwjake2.qcommon.xcommand_t

import java.awt.Dimension
import java.util.ArrayList
import java.util.LinkedList

import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11

/**
 * LWJGLBase

 * @author dsanders/cwei
 */
public abstract class LWJGLBase {
    // IMPORTED FUNCTIONS
    protected var oldDisplayMode: DisplayMode? = null


    // window position on the screen
    var window_xpos: Int = 0
    var window_ypos: Int = 0
    protected var vid: viddef_t = viddef_t()

    // handles the post initialization with LWJGLRenderer
    protected abstract fun R_Init2(): Boolean

    protected var vid_fullscreen: cvar_t

    private fun toAwtDisplayMode(m: DisplayMode): java.awt.DisplayMode {
        return java.awt.DisplayMode(m.getWidth(), m.getHeight(), m.getBitsPerPixel(), m.getFrequency())
    }

    public fun getModeList(): Array<java.awt.DisplayMode>? {
        try {
            val modes: Array<DisplayMode>

            modes = Display.getAvailableDisplayModes()

            val l = LinkedList<java.awt.DisplayMode>()
            l.add(toAwtDisplayMode(oldDisplayMode))

            for (i in modes.indices) {
                val m = modes[i]

                if (m.getBitsPerPixel() != oldDisplayMode!!.getBitsPerPixel()) continue
                if (m.getFrequency() > oldDisplayMode!!.getFrequency()) continue
                if (m.getHeight() < 240 || m.getWidth() < 320) continue

                var j = 0
                var ml: java.awt.DisplayMode? = null
                run {
                    j = 0
                    while (j < l.size()) {
                        ml = l.get(j) as java.awt.DisplayMode
                        if (ml!!.getWidth() > m.getWidth()) break
                        if (ml!!.getWidth() == m.getWidth() && ml!!.getHeight() >= m.getHeight()) break
                        j++
                    }
                }
                if (j == l.size()) {
                    l.addLast(toAwtDisplayMode(m))
                } else if (ml!!.getWidth() > m.getWidth() || ml!!.getHeight() > m.getHeight()) {
                    l.add(j, toAwtDisplayMode(m))
                } else if (m.getFrequency() > ml!!.getRefreshRate()) {
                    l.remove(j)
                    l.add(j, toAwtDisplayMode(m))
                }
            }
            val ma = arrayOfNulls<java.awt.DisplayMode>(l.size())
            l.toArray<java.awt.DisplayMode>(ma)
            return ma
        } catch (e: LWJGLException) {
            e.printStackTrace()
            System.exit(0)
        }

        return null
    }

    public fun getLWJGLModeList(): Array<DisplayMode>? {
        try {
            // Return value storage.
            val displayModes: ArrayList<DisplayMode>

            // Get all possible display modes.
            val allDisplayModes = Display.getAvailableDisplayModes()

            // Cut down all the ones with a height below 240.
            displayModes = ArrayList<DisplayMode>()
            for (x in allDisplayModes.indices) {
                if (allDisplayModes[x].getHeight() >= 240)
                    displayModes.add(allDisplayModes[x])
            }

            // Gnome sort the display modes by height, width, and refresh rate.
            var currentSpot = 0
            var needSwap = false
            val tempStore: DisplayMode
            while (currentSpot < displayModes.size() - 1) {
                // Check DisplayMode heights.
                if (displayModes.get(currentSpot).getHeight() > displayModes.get(currentSpot + 1).getHeight())
                    needSwap = true
                else if (displayModes.get(currentSpot).getHeight() == displayModes.get(currentSpot + 1).getHeight()) {
                    // Check DisplayMode widths.
                    if (displayModes.get(currentSpot).getWidth() > displayModes.get(currentSpot + 1).getWidth())
                        needSwap = true
                    else if (displayModes.get(currentSpot).getWidth() == displayModes.get(currentSpot + 1).getWidth())
                    // Doesn't sort frequencies, but removes the lesser ones entirely.
                        if (displayModes.get(currentSpot).getFrequency() < displayModes.get(currentSpot + 1).getFrequency()) {
                            displayModes.remove(currentSpot)
                            currentSpot--
                        } else if (displayModes.get(currentSpot).getFrequency() > displayModes.get(currentSpot + 1).getFrequency()) {
                            displayModes.remove(currentSpot + 1)
                            currentSpot--
                        }
                }
                if (needSwap) {
                    needSwap = false
                    tempStore = displayModes.get(currentSpot)
                    displayModes.set(currentSpot, displayModes.get(currentSpot + 1))
                    displayModes.set(currentSpot + 1, tempStore)
                    if (currentSpot > 0)
                        currentSpot--
                } else
                    currentSpot++
            }

            // Return the array.
            return displayModes.toArray<DisplayMode>(arrayOfNulls<DisplayMode>(displayModes.size()))

        } catch (e: LWJGLException) {
            e.printStackTrace()
            System.exit(0)
        }

        return null
    }

    private fun findDisplayMode(dim: Dimension): DisplayMode {
        var mode: DisplayMode? = null
        var m: DisplayMode? = null
        val modes = getLWJGLModeList()
        val w = dim.width
        val h = dim.height

        for (i in modes.indices) {
            m = modes[i]
            if (m!!.getWidth() == w && m!!.getHeight() == h) {
                mode = m
                break
            }
        }
        if (mode == null) mode = oldDisplayMode
        return mode
    }

    fun getModeString(m: DisplayMode): String {
        val sb = StringBuffer()
        sb.append(m.getWidth())
        sb.append('x')
        sb.append(m.getHeight())
        sb.append('x')
        sb.append(m.getBitsPerPixel())
        sb.append('@')
        sb.append(m.getFrequency())
        sb.append("Hz")
        return sb.toString()
    }

    /**
     * @param dim
    * *
     * @param mode
    * *
     * @param fullscreen
    * *
     * @return enum rserr_t
     */
    protected fun GLimp_SetMode(dim: Dimension, mode: Int, fullscreen: Boolean): Int {

        val newDim = Dimension()

        VID.Printf(Defines.PRINT_ALL, "Initializing OpenGL display\n")

        VID.Printf(Defines.PRINT_ALL, "...setting mode " + mode + ":")

        /*
		 * fullscreen handling
		 */
        if (oldDisplayMode == null) {
            oldDisplayMode = Display.getDisplayMode()
        }

        if (!VID.GetModeInfo(newDim, mode)) {
            VID.Printf(Defines.PRINT_ALL, " invalid mode\n")
            return rserr_invalid_mode
        }

        VID.Printf(Defines.PRINT_ALL, " " + newDim.width + " " + newDim.height + '\n')

        // destroy the existing window
        GLimp_Shutdown()

        Display.setTitle("LWJake2")

        val displayMode = findDisplayMode(newDim)
        newDim.width = displayMode.getWidth()
        newDim.height = displayMode.getHeight()

        if (fullscreen) {
            try {
                Display.setDisplayMode(displayMode)
            } catch (e: LWJGLException) {
                return rserr_invalid_mode
            }


            Display.setLocation(0, 0)

            try {
                Display.setFullscreen(true)
            } catch (e: LWJGLException) {
                return rserr_invalid_fullscreen
            }


            VID.Printf(Defines.PRINT_ALL, "...setting fullscreen " + getModeString(displayMode) + '\n')

        } else {
            try {
                Display.setFullscreen(false)
            } catch (e: LWJGLException) {
                return rserr_invalid_fullscreen
            }


            try {
                Display.setDisplayMode(displayMode)
            } catch (e: LWJGLException) {
                return rserr_invalid_mode
            }

            Display.setLocation(window_xpos, window_ypos)
        }

        vid.width = newDim.width
        vid.height = newDim.height

        try {
            Display.create()
        } catch (e: LWJGLException) {
            return rserr_unknown
        }


        // let the sound and input subsystems know about the new window
        VID.NewWindow(vid.width, vid.height)
        return rserr_ok
    }

    protected fun GLimp_Shutdown() {
        if (oldDisplayMode != null && Display.isFullscreen()) {
            try {
                Display.setDisplayMode(oldDisplayMode)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        while (Display.isCreated()) {
            Display.destroy()
        }
    }

    /**
     * @return true
     */
    protected fun GLimp_Init(xpos: Int, ypos: Int): Boolean {
        // do nothing
        window_xpos = xpos
        window_ypos = ypos
        return true
    }

    protected fun GLimp_EndFrame() {
        GL11.glFlush()
        // swap buffers
        Display.update()
    }

    protected fun GLimp_BeginFrame(camera_separation: Float) {
        // do nothing
    }

    protected fun GLimp_AppActivate(activate: Boolean) {
        // do nothing
    }

    protected fun GLimp_EnableLogging(enable: Boolean) {
        // do nothing
    }

    protected fun GLimp_LogNewFrame() {
        // do nothing
    }

    /**
     * this is a hack for jogl renderers.
     * @param callback
     */
    public fun updateScreen(callback: xcommand_t) {
        callback.execute()
    }

    companion object {

        // enum rserr_t
        protected val rserr_ok: Int = 0
        protected val rserr_invalid_fullscreen: Int = 1
        protected val rserr_invalid_mode: Int = 2
        protected val rserr_unknown: Int = 3
    }
}
