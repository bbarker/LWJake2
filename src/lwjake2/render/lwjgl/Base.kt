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

import org.lwjgl.opengl.GL11

/**
 * Base

 * @author dsanders/cwei
 */
public abstract class Base : LWJGLBase() {
    companion object {

        val GL_COLOR_INDEX8_EXT = GL11.GL_COLOR_INDEX

        val REF_VERSION = "GL 0.01"

        // up / down
        val PITCH = 0

        // left / right
        val YAW = 1

        // fall over
        val ROLL = 2

        /*
     * skins will be outline flood filled and mip mapped pics and sprites with
     * alpha will be outline flood filled pic won't be mip mapped
     * 
     * model skin sprite frame wall texture pic
     */
        // enum imagetype_t
        val it_skin = 0

        val it_sprite = 1

        val it_wall = 2

        val it_pic = 3

        val it_sky = 4

        // enum modtype_t
        val mod_bad = 0

        val mod_brush = 1

        val mod_sprite = 2

        val mod_alias = 3

        val TEXNUM_LIGHTMAPS = 1024

        val TEXNUM_SCRAPS = 1152

        val TEXNUM_IMAGES = 1153

        val MAX_GLTEXTURES = 1024

        val MAX_LBM_HEIGHT = 480

        val BACKFACE_EPSILON = 0.01.toFloat()

        /*
     * * GL config stuff
     */
        val GL_RENDERER_VOODOO = 1

        val GL_RENDERER_VOODOO2 = 2

        val GL_RENDERER_VOODOO_RUSH = 4

        val GL_RENDERER_BANSHEE = 8

        val GL_RENDERER_3DFX = 15

        val GL_RENDERER_PCX1 = 16

        val GL_RENDERER_PCX2 = 32

        val GL_RENDERER_PMX = 64

        val GL_RENDERER_POWERVR = 112

        val GL_RENDERER_PERMEDIA2 = 256

        val GL_RENDERER_GLINT_MX = 512

        val GL_RENDERER_GLINT_TX = 1024

        val GL_RENDERER_3DLABS_MISC = 2048

        val GL_RENDERER_3DLABS = 3840

        val GL_RENDERER_REALIZM = 4096

        val GL_RENDERER_REALIZM2 = 8192

        val GL_RENDERER_INTERGRAPH = 12288

        val GL_RENDERER_3DPRO = 16384

        val GL_RENDERER_REAL3D = 32768

        val GL_RENDERER_RIVA128 = 65536

        val GL_RENDERER_DYPIC = 131072

        val GL_RENDERER_V1000 = 262144

        val GL_RENDERER_V2100 = 524288

        val GL_RENDERER_V2200 = 1048576

        val GL_RENDERER_RENDITION = 1835008

        val GL_RENDERER_O2 = 1048576

        val GL_RENDERER_IMPACT = 2097152

        val GL_RENDERER_RE = 4194304

        val GL_RENDERER_IR = 8388608

        val GL_RENDERER_SGI = 15728640

        val GL_RENDERER_MCD = 16777216

        val GL_RENDERER_OTHER = -2147483648
    }
}