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

import lwjake2.Defines
import lwjake2.Globals
import lwjake2.game.Cmd
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.xcommand_t
import lwjake2.render.Renderer
import lwjake2.sound.S
import lwjake2.sys.IN
import lwjake2.util.Vargs

import java.awt.Dimension
import java.awt.DisplayMode

/**
 * VID is a video driver.

 * source: client/vid.h linux/vid_so.c

 * @author cwei
 */
public class VID : Globals() {
    companion object {
        //	   Main windowed and fullscreen graphics interface module. This module
        //	   is used for both the software and OpenGL rendering versions of the
        //	   Quake refresh engine.

        // Global variables used internally by this module
        // Globals.viddef
        // global video state; used by other modules

        // Structure containing functions exported from refresh DLL
        // Globals.re;

        // Console variables that we need to access from this module
        var vid_gamma: cvar_t
        var vid_ref: cvar_t            // Name of Refresh DLL loaded
        var vid_xpos: cvar_t            // X coordinate of window position
        var vid_ypos: cvar_t            // Y coordinate of window position
        var vid_width: cvar_t
        var vid_height: cvar_t
        var vid_fullscreen: cvar_t

        // Global variables used internally by this module
        // void *reflib_library;		// Handle to refresh DLL
        var reflib_active = false
        // const char so_file[] = "/etc/quake2.conf";

        /*
	==========================================================================

	DLL GLUE

	==========================================================================
	*/

        public fun Printf(print_level: Int, fmt: String) {
            Printf(print_level, fmt, null)
        }

        public fun Printf(print_level: Int, fmt: String, vargs: Vargs?) {
            // static qboolean inupdate;
            if (print_level == Defines.PRINT_ALL)
                Com.Printf(fmt, vargs)
            else
                Com.DPrintf(fmt, vargs)
        }

        // ==========================================================================

        /*
	============
	VID_Restart_f

	Console command to re-start the video mode and refresh DLL. We do this
	simply by setting the modified flag for the vid_ref variable, which will
	cause the entire video mode and refresh DLL to be reset on the next frame.
	============
	*/
        fun Restart_f() {
            vid_modes[11].width = vid_width.value as Int
            vid_modes[11].height = vid_height.value as Int

            vid_ref.modified = true
        }

        /*
	** VID_GetModeInfo
	*/
        var vid_modes = array<vidmode_t>(vidmode_t("Mode 0: 320x240", 320, 240, 0), vidmode_t("Mode 1: 400x300", 400, 300, 1), vidmode_t("Mode 2: 512x384", 512, 384, 2), vidmode_t("Mode 3: 640x480", 640, 480, 3), vidmode_t("Mode 4: 800x600", 800, 600, 4), vidmode_t("Mode 5: 960x720", 960, 720, 5), vidmode_t("Mode 6: 1024x768", 1024, 768, 6), vidmode_t("Mode 7: 1152x864", 1152, 864, 7), vidmode_t("Mode 8: 1280x1024", 1280, 1024, 8), vidmode_t("Mode 9: 1600x1200", 1600, 1200, 9), vidmode_t("Mode 10: 2048x1536", 2048, 1536, 10), vidmode_t("Mode 11: user", 640, 480, 11))
        var fs_modes: Array<vidmode_t>? = null

        public fun GetModeInfo(dim: Dimension, mode: Int): Boolean {
            if (fs_modes == null) initModeList()

            var modes = vid_modes
            if (vid_fullscreen.value != 0.0.toFloat()) modes = fs_modes

            if (mode < 0 || mode >= modes.size())
                return false

            dim.width = modes[mode].width
            dim.height = modes[mode].height

            return true
        }

        /*
	** VID_NewWindow
	*/
        public fun NewWindow(width: Int, height: Int) {
            Globals.viddef.width = width
            Globals.viddef.height = height
        }

        fun FreeReflib() {
            if (Globals.re != null) {
                Globals.re.getKeyboardHandler().Close()
                IN.Shutdown()
            }

            Globals.re = null
            reflib_active = false
        }

        /*
	==============
	VID_LoadRefresh
	==============
	*/
        fun LoadRefresh(name: String): Boolean {

            if (reflib_active) {
                Globals.re.getKeyboardHandler().Close()
                IN.Shutdown()

                Globals.re.Shutdown()
                FreeReflib()
            }

            Com.Printf("------- Loading " + name + " -------\n")

            var found = false

            val driverNames = Renderer.getDriverNames()
            for (i in driverNames.indices) {
                if (driverNames[i].equals(name)) {
                    found = true
                    break
                }
            }

            if (!found) {
                Com.Printf("LoadLibrary(\"" + name + "\") failed\n")
                return false
            }

            Com.Printf("LoadLibrary(\"" + name + "\")\n")
            Globals.re = Renderer.getDriver(name)

            if (Globals.re == null) {
                Com.Error(Defines.ERR_FATAL, name + " can't load but registered")
            }

            if (Globals.re.apiVersion() != Defines.API_VERSION) {
                FreeReflib()
                Com.Error(Defines.ERR_FATAL, name + " has incompatible api_version")
            }

            IN.Real_IN_Init()

            if (!Globals.re.Init(vid_xpos.value as Int, vid_ypos.value as Int)) {
                Globals.re.Shutdown()
                FreeReflib()
                return false
            }

            /* Init KBD */
            Globals.re.getKeyboardHandler().Init()

            Com.Printf("------------------------------------\n")
            reflib_active = true
            return true
        }

        /*
	============
	VID_CheckChanges

	This function gets called once just before drawing each frame, and it's sole purpose in life
	is to check to see if any of the video mode parameters have changed, and if they have to 
	update the rendering DLL and/or video mode to match.
	============
	*/
        public fun CheckChanges() {
            val gl_mode: cvar_t

            if (vid_ref.modified) {
                S.StopAllSounds()
            }

            while (vid_ref.modified) {
                /*
			** refresh has changed
			*/
                vid_ref.modified = false
                vid_fullscreen.modified = true
                Globals.cl.refresh_prepped = false
                Globals.cls.disable_screen = 1.0.toFloat() // true;


                if (!LoadRefresh(vid_ref.string)) {
                    var renderer: String
                    if (vid_ref.string.equals(Renderer.getPreferedName())) {
                        // try the default renderer as fallback after prefered
                        renderer = Renderer.getDefaultName()
                    } else {
                        // try the prefered renderer as first fallback
                        renderer = Renderer.getPreferedName()
                    }
                    if (vid_ref.string.equals(Renderer.getDefaultName())) {
                        renderer = vid_ref.string
                        Com.Printf("Refresh failed\n")
                        gl_mode = Cvar.Get("gl_mode", "0", 0)
                        if (gl_mode.value != 0.0.toFloat()) {
                            Com.Printf("Trying mode 0\n")
                            Cvar.SetValue("gl_mode", 0)
                            if (!LoadRefresh(vid_ref.string))
                                Com.Error(Defines.ERR_FATAL, "Couldn't fall back to " + renderer + " refresh!")
                        } else
                            Com.Error(Defines.ERR_FATAL, "Couldn't fall back to " + renderer + " refresh!")
                    }

                    Cvar.Set("vid_ref", renderer)

                    /*
				 * drop the console if we fail to load a refresh
				 */
                    if (Globals.cls.key_dest != Defines.key_console) {
                        try {
                            Console.ToggleConsole_f.execute()
                        } catch (e: Exception) {
                        }

                    }
                }
                Globals.cls.disable_screen = 0.0.toFloat() //false;
            }
        }

        /*
	============
	VID_Init
	============
	*/
        public fun Init() {
            /* Create the video variables so we know how to start the graphics drivers */
            vid_ref = Cvar.Get("vid_ref", Renderer.getPreferedName(), CVAR_ARCHIVE)
            vid_xpos = Cvar.Get("vid_xpos", "3", CVAR_ARCHIVE)
            vid_ypos = Cvar.Get("vid_ypos", "22", CVAR_ARCHIVE)
            vid_width = Cvar.Get("vid_width", "640", CVAR_ARCHIVE)
            vid_height = Cvar.Get("vid_height", "480", CVAR_ARCHIVE)
            vid_fullscreen = Cvar.Get("vid_fullscreen", "0", CVAR_ARCHIVE)
            vid_gamma = Cvar.Get("vid_gamma", "1", CVAR_ARCHIVE)

            vid_modes[11].width = vid_width.value as Int
            vid_modes[11].height = vid_height.value as Int

            /* Add some console commands that we want to handle */
            Cmd.AddCommand("vid_restart", object : xcommand_t() {
                public fun execute() {
                    Restart_f()
                }
            })

            /* Disable the 3Dfx splash screen */
            // putenv("FX_GLIDE_NO_SPLASH=0");

            /* Start the graphics mode and load refresh DLL */
            CheckChanges()
        }

        /*
	============
	VID_Shutdown
	============
	*/
        public fun Shutdown() {
            if (reflib_active) {
                Globals.re.getKeyboardHandler().Close()
                IN.Shutdown()

                Globals.re.Shutdown()
                FreeReflib()
            }
        }

        // ==========================================================================
        //
        //	vid_menu.c
        //
        // ==========================================================================

        val REF_OPENGL_JOGL = 0
        val REF_OPENGL_FASTJOGL = 1
        val REF_OPENGL_LWJGL = 2

        var gl_mode: cvar_t? = null
        var gl_driver: cvar_t? = null
        var gl_picmip: cvar_t? = null
        var gl_ext_palettedtexture: cvar_t? = null

        var sw_mode: cvar_t? = null
        var sw_stipplealpha: cvar_t? = null

        var _windowed_mouse: cvar_t? = null

        /*
	====================================================================

	MENU INTERACTION

	====================================================================
	*/

        var s_opengl_menu = Menu.menuframework_s()
        var s_current_menu: Menu.menuframework_s // referenz

        var s_mode_list = Menu.menulist_s()

        var s_ref_list = Menu.menulist_s()

        var s_tq_slider = Menu.menuslider_s()
        var s_screensize_slider = Menu.menuslider_s()

        var s_brightness_slider = Menu.menuslider_s()

        var s_fs_box = Menu.menulist_s()

        var s_stipple_box = Menu.menulist_s()
        var s_paletted_texture_box = Menu.menulist_s()
        var s_windowed_mouse = Menu.menulist_s()
        var s_apply_action = Menu.menuaction_s()

        var s_defaults_action = Menu.menuaction_s()

        fun DriverCallback(unused: Object) {
            s_current_menu = s_opengl_menu // s_software_menu;
        }

        fun ScreenSizeCallback(s: Object) {
            val slider = s as Menu.menuslider_s

            Cvar.SetValue("viewsize", slider.curvalue * 10)
        }

        fun BrightnessCallback(s: Object) {
            val slider = s as Menu.menuslider_s

            // if ( stricmp( vid_ref.string, "soft" ) == 0 ||
            //	stricmp( vid_ref.string, "softx" ) == 0 )
            if (vid_ref.string.equalsIgnoreCase("soft") || vid_ref.string.equalsIgnoreCase("softx")) {
                val gamma = (0.8.toFloat() - (slider.curvalue / 10.0.toFloat() - 0.5.toFloat())) + 0.5.toFloat()

                Cvar.SetValue("vid_gamma", gamma)
            }
        }

        fun ResetDefaults(unused: Object) {
            MenuInit()
        }

        fun ApplyChanges(unused: Object) {

            /*
		** invert sense so greater = brighter, and scale to a range of 0.5 to 1.3
		*/
            // the original was modified, because on CRTs it was too dark.
            // the slider range is [5; 13]
            // gamma: [1.1; 0.7]
            val gamma = (0.4.toFloat() - (s_brightness_slider.curvalue / 20.0.toFloat() - 0.25.toFloat())) + 0.7.toFloat()
            // modulate:  [1.0; 2.6]
            val modulate = s_brightness_slider.curvalue * 0.2.toFloat()

            Cvar.SetValue("vid_gamma", gamma)
            Cvar.SetValue("gl_modulate", modulate)
            Cvar.SetValue("sw_stipplealpha", s_stipple_box.curvalue)
            Cvar.SetValue("gl_picmip", 3 - s_tq_slider.curvalue)
            Cvar.SetValue("vid_fullscreen", s_fs_box.curvalue)
            Cvar.SetValue("gl_ext_palettedtexture", s_paletted_texture_box.curvalue)
            Cvar.SetValue("gl_mode", s_mode_list.curvalue)
            Cvar.SetValue("_windowed_mouse", s_windowed_mouse.curvalue)

            Cvar.Set("vid_ref", drivers[s_ref_list.curvalue])
            Cvar.Set("gl_driver", drivers[s_ref_list.curvalue])
            if (gl_driver!!.modified)
                vid_ref.modified = true

            Menu.ForceMenuOff()
        }

        val resolutions = array<String>("[320 240  ]", "[400 300  ]", "[512 384  ]", "[640 480  ]", "[800 600  ]", "[960 720  ]", "[1024 768 ]", "[1152 864 ]", "[1280 1024]", "[1600 1200]", "[2048 1536]", "user mode")
        var fs_resolutions: Array<String>
        var mode_x: Int = 0

        var refs: Array<String>
        var drivers: Array<String>

        val yesno_names = array<String>("no", "yes")

        fun initModeList() {
            val modes = re.getModeList()
            fs_resolutions = arrayOfNulls<String>(modes.size())
            fs_modes = arrayOfNulls<vidmode_t>(modes.size())
            for (i in modes.indices) {
                val m = modes[i]
                val sb = StringBuffer(18)
                sb.append('[')
                sb.append(m.getWidth())
                sb.append(' ')
                sb.append(m.getHeight())
                while (sb.length() < 10) sb.append(' ')
                sb.append(']')
                fs_resolutions[i] = sb.toString()
                sb.setLength(0)
                sb.append("Mode ")
                sb.append(i)
                sb.append(':')
                sb.append(m.getWidth())
                sb.append('x')
                sb.append(m.getHeight())
                fs_modes[i] = vidmode_t(sb.toString(), m.getWidth(), m.getHeight(), i)
            }
        }

        private fun initRefs() {
            drivers = Renderer.getDriverNames()
            refs = arrayOfNulls<String>(drivers.size())
            val sb = StringBuffer()
            for (i in drivers.indices) {
                sb.setLength(0)
                sb.append("[OpenGL ").append(drivers[i])
                while (sb.length() < 16) sb.append(" ")
                sb.append("]")
                refs[i] = sb.toString()
            }
        }

        /*
	** VID_MenuInit
	*/
        public fun MenuInit() {

            initRefs()

            if (gl_driver == null)
                gl_driver = Cvar.Get("gl_driver", Renderer.getPreferedName(), 0)
            if (gl_picmip == null)
                gl_picmip = Cvar.Get("gl_picmip", "0", 0)
            if (gl_mode == null)
                gl_mode = Cvar.Get("gl_mode", "3", 0)
            if (sw_mode == null)
                sw_mode = Cvar.Get("sw_mode", "0", 0)
            if (gl_ext_palettedtexture == null)
                gl_ext_palettedtexture = Cvar.Get("gl_ext_palettedtexture", "1", CVAR_ARCHIVE)

            if (sw_stipplealpha == null)
                sw_stipplealpha = Cvar.Get("sw_stipplealpha", "0", CVAR_ARCHIVE)

            if (_windowed_mouse == null)
                _windowed_mouse = Cvar.Get("_windowed_mouse", "0", CVAR_ARCHIVE)

            s_mode_list.curvalue = gl_mode!!.value as Int
            if (vid_fullscreen.value != 0.0.toFloat()) {
                s_mode_list.itemnames = fs_resolutions
                if (s_mode_list.curvalue >= fs_resolutions.size() - 1) {
                    s_mode_list.curvalue = 0
                }
                mode_x = fs_modes!![s_mode_list.curvalue].width
            } else {
                s_mode_list.itemnames = resolutions
                if (s_mode_list.curvalue >= resolutions.size() - 1) {
                    s_mode_list.curvalue = 0
                }
                mode_x = vid_modes[s_mode_list.curvalue].width
            }

            if (SCR.scr_viewsize == null)
                SCR.scr_viewsize = Cvar.Get("viewsize", "100", CVAR_ARCHIVE)

            s_screensize_slider.curvalue = (SCR.scr_viewsize.value / 10) as Int

            for (i in drivers.indices) {
                if (vid_ref.string.equals(drivers[i])) {
                    s_ref_list.curvalue = i
                }
            }

            s_opengl_menu.x = (viddef.width * 0.50.toFloat()) as Int
            s_opengl_menu.nitems = 0

            s_ref_list.type = MTYPE_SPINCONTROL
            s_ref_list.name = "driver"
            s_ref_list.x = 0
            s_ref_list.y = 0
            s_ref_list.callback = object : Menu.mcallback() {
                public fun execute(self: Object) {
                    DriverCallback(self)
                }
            }
            s_ref_list.itemnames = refs

            s_mode_list.type = MTYPE_SPINCONTROL
            s_mode_list.name = "video mode"
            s_mode_list.x = 0
            s_mode_list.y = 10

            s_screensize_slider.type = MTYPE_SLIDER
            s_screensize_slider.x = 0
            s_screensize_slider.y = 20
            s_screensize_slider.name = "screen size"
            s_screensize_slider.minvalue = 3
            s_screensize_slider.maxvalue = 12
            s_screensize_slider.callback = object : Menu.mcallback() {
                public fun execute(self: Object) {
                    ScreenSizeCallback(self)
                }
            }
            s_brightness_slider.type = MTYPE_SLIDER
            s_brightness_slider.x = 0
            s_brightness_slider.y = 30
            s_brightness_slider.name = "brightness"
            s_brightness_slider.callback = object : Menu.mcallback() {
                public fun execute(self: Object) {
                    BrightnessCallback(self)
                }
            }
            s_brightness_slider.minvalue = 5
            s_brightness_slider.maxvalue = 13
            s_brightness_slider.curvalue = (1.3.toFloat() - vid_gamma.value + 0.5.toFloat()) * 10

            s_fs_box.type = MTYPE_SPINCONTROL
            s_fs_box.x = 0
            s_fs_box.y = 40
            s_fs_box.name = "fullscreen"
            s_fs_box.itemnames = yesno_names
            s_fs_box.curvalue = vid_fullscreen.value as Int
            s_fs_box.callback = object : Menu.mcallback() {
                public fun execute(o: Object) {
                    val fs = (o as Menu.menulist_s).curvalue
                    if (fs == 0) {
                        s_mode_list.itemnames = resolutions
                        var i = vid_modes.size() - 2
                        while (i > 0 && vid_modes[i].width > mode_x) i--
                        s_mode_list.curvalue = i
                    } else {
                        s_mode_list.itemnames = fs_resolutions
                        var i = fs_modes!!.size() - 1
                        while (i > 0 && fs_modes!![i].width > mode_x) i--
                        s_mode_list.curvalue = i
                    }
                }
            }

            s_defaults_action.type = MTYPE_ACTION
            s_defaults_action.name = "reset to default"
            s_defaults_action.x = 0
            s_defaults_action.y = 90
            s_defaults_action.callback = object : Menu.mcallback() {
                public fun execute(self: Object) {
                    ResetDefaults(self)
                }
            }

            s_apply_action.type = MTYPE_ACTION
            s_apply_action.name = "apply"
            s_apply_action.x = 0
            s_apply_action.y = 100
            s_apply_action.callback = object : Menu.mcallback() {
                public fun execute(self: Object) {
                    ApplyChanges(self)
                }
            }


            s_stipple_box.type = MTYPE_SPINCONTROL
            s_stipple_box.x = 0
            s_stipple_box.y = 60
            s_stipple_box.name = "stipple alpha"
            s_stipple_box.curvalue = sw_stipplealpha!!.value as Int
            s_stipple_box.itemnames = yesno_names

            s_windowed_mouse.type = MTYPE_SPINCONTROL
            s_windowed_mouse.x = 0
            s_windowed_mouse.y = 72
            s_windowed_mouse.name = "windowed mouse"
            s_windowed_mouse.curvalue = _windowed_mouse!!.value as Int
            s_windowed_mouse.itemnames = yesno_names

            s_tq_slider.type = MTYPE_SLIDER
            s_tq_slider.x = 0
            s_tq_slider.y = 60
            s_tq_slider.name = "texture quality"
            s_tq_slider.minvalue = 0
            s_tq_slider.maxvalue = 3
            s_tq_slider.curvalue = 3 - gl_picmip!!.value

            s_paletted_texture_box.type = MTYPE_SPINCONTROL
            s_paletted_texture_box.x = 0
            s_paletted_texture_box.y = 70
            s_paletted_texture_box.name = "8-bit textures"
            s_paletted_texture_box.itemnames = yesno_names
            s_paletted_texture_box.curvalue = gl_ext_palettedtexture!!.value as Int

            Menu.Menu_AddItem(s_opengl_menu, s_ref_list)
            Menu.Menu_AddItem(s_opengl_menu, s_mode_list)
            Menu.Menu_AddItem(s_opengl_menu, s_screensize_slider)
            Menu.Menu_AddItem(s_opengl_menu, s_brightness_slider)
            Menu.Menu_AddItem(s_opengl_menu, s_fs_box)
            Menu.Menu_AddItem(s_opengl_menu, s_tq_slider)
            Menu.Menu_AddItem(s_opengl_menu, s_paletted_texture_box)

            Menu.Menu_AddItem(s_opengl_menu, s_defaults_action)
            Menu.Menu_AddItem(s_opengl_menu, s_apply_action)

            Menu.Menu_Center(s_opengl_menu)
            s_opengl_menu.x -= 8
        }

        /*
	================
	VID_MenuDraw
	================
	*/
        fun MenuDraw() {
            s_current_menu = s_opengl_menu

            /*
		** draw the banner
		*/
            val dim = Dimension()
            re.DrawGetPicSize(dim, "m_banner_video")
            re.DrawPic(viddef.width / 2 - dim.width / 2, viddef.height / 2 - 110, "m_banner_video")

            /*
		** move cursor to a reasonable starting position
		*/
            Menu.Menu_AdjustCursor(s_current_menu, 1)

            /*
		** draw the menu
		*/
            Menu.Menu_Draw(s_current_menu)
        }

        /*
	================
	VID_MenuKey
	================
	*/
        fun MenuKey(key: Int): String? {
            val m = s_current_menu
            val sound = "misc/menu1.wav"

            when (key) {
                K_ESCAPE -> {
                    Menu.PopMenu()
                    return null
                }
                K_UPARROW -> {
                    m.cursor--
                    Menu.Menu_AdjustCursor(m, -1)
                }
                K_DOWNARROW -> {
                    m.cursor++
                    Menu.Menu_AdjustCursor(m, 1)
                }
                K_LEFTARROW -> Menu.Menu_SlideItem(m, -1)
                K_RIGHTARROW -> Menu.Menu_SlideItem(m, 1)
                K_ENTER -> Menu.Menu_SelectItem(m)
            }

            return sound
        }
    }

}
