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
import lwjake2.client.particle_t
import lwjake2.game.cvar_t
import lwjake2.qcommon.Com
import lwjake2.qcommon.Cvar
import lwjake2.qcommon.FS
import lwjake2.qcommon.qfiles
import lwjake2.render.image_t
import lwjake2.util.Lib
import lwjake2.util.Vargs

import java.awt.Dimension
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.Arrays

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBImaging
import org.lwjgl.opengl.ARBMultitexture
import org.lwjgl.opengl.EXTSharedTexturePalette
import org.lwjgl.opengl.GL11

/**
 * Image

 * @author cwei
 */
public abstract class Image : Main() {

    var draw_chars: image_t? = null

    var gltextures = arrayOfNulls<image_t>(MAX_GLTEXTURES)
    //Map gltextures = new Hashtable(MAX_GLTEXTURES); // image_t
    var numgltextures: Int = 0
    var base_textureid: Int = 0 // gltextures[i] = base_textureid+i

    var intensitytable = ByteArray(256)
    var gammatable = ByteArray(256)

    var intensity: cvar_t

    //
    //	qboolean GL_Upload8 (byte *data, int width, int height,  qboolean mipmap, qboolean is_sky );
    //	qboolean GL_Upload32 (unsigned *data, int width, int height,  qboolean mipmap);
    //

    var gl_solid_format = 3
    var gl_alpha_format = 4

    var gl_tex_solid_format = 3
    var gl_tex_alpha_format = 4

    var gl_filter_min = GL11.GL_LINEAR_MIPMAP_NEAREST
    var gl_filter_max = GL11.GL_LINEAR

    {
        // init the texture cache
        for (i in gltextures.indices) {
            gltextures[i] = image_t(i)
        }
        numgltextures = 0
    }

    fun GL_SetTexturePalette(palette: IntArray?) {

        assert((palette != null && palette.size() == 256), "int palette[256] bug")

        var i: Int
        //byte[] temptable = new byte[768];

        if (qglColorTableEXT && gl_ext_palettedtexture.value != 0.0.toFloat()) {
            val temptable = BufferUtils.createByteBuffer(768)
            run {
                i = 0
                while (i < 256) {
                    temptable.put(i * 3 + 0, ((palette!![i] shr 0) and 255).toByte())
                    temptable.put(i * 3 + 1, ((palette[i] shr 8) and 255).toByte())
                    temptable.put(i * 3 + 2, ((palette[i] shr 16) and 255).toByte())
                    i++
                }
            }

            ARBImaging.glColorTable(EXTSharedTexturePalette.GL_SHARED_TEXTURE_PALETTE_EXT, GL11.GL_RGB, 256, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, temptable)
        }
    }

    fun GL_EnableMultitexture(enable: Boolean) {
        if (enable) {
            GL_SelectTexture(GL_TEXTURE1)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL_TexEnv(GL11.GL_REPLACE)
        } else {
            GL_SelectTexture(GL_TEXTURE1)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL_TexEnv(GL11.GL_REPLACE)
        }
        GL_SelectTexture(GL_TEXTURE0)
        GL_TexEnv(GL11.GL_REPLACE)
    }

    fun GL_SelectTexture(texture: Int /* GLenum */) {
        val tmu: Int

        tmu = if ((texture == GL_TEXTURE0)) 0 else 1

        if (tmu == gl_state.currenttmu) {
            return
        }

        gl_state.currenttmu = tmu

        ARBMultitexture.glActiveTextureARB(texture)
        ARBMultitexture.glClientActiveTextureARB(texture)
    }

    var lastmodes = intArray(-1, -1)

    fun GL_TexEnv(mode: Int /* GLenum */) {

        if (mode != lastmodes[gl_state.currenttmu]) {
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, mode)
            lastmodes[gl_state.currenttmu] = mode
        }
    }

    fun GL_Bind(texnum: Int) {
        var texnum = texnum

        if ((gl_nobind.value != 0) && (draw_chars != null)) {
            // performance evaluation option
            texnum = draw_chars!!.texnum
        }
        if (gl_state.currenttextures[gl_state.currenttmu] == texnum)
            return

        gl_state.currenttextures[gl_state.currenttmu] = texnum
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texnum)
    }

    fun GL_MBind(target: Int /* GLenum */, texnum: Int) {
        GL_SelectTexture(target)
        if (target == GL_TEXTURE0) {
            if (gl_state.currenttextures[0] == texnum)
                return
        } else {
            if (gl_state.currenttextures[1] == texnum)
                return
        }
        GL_Bind(texnum)
    }

    // glmode_t
    class glmode_t(var name: String, var minimize: Int, var maximize: Int)

    // gltmode_t
    class gltmode_t(var name: String, var mode: Int)

    /*
	===============
	GL_TextureMode
	===============
	*/
    fun GL_TextureMode(string: String) {

        var i: Int
        run {
            i = 0
            while (i < NUM_GL_MODES) {
                if (modes[i].name.equalsIgnoreCase(string))
                    break
                i++
            }
        }

        if (i == NUM_GL_MODES) {
            VID.Printf(Defines.PRINT_ALL, "bad filter name: [" + string + "]\n")
            return
        }

        gl_filter_min = modes[i].minimize
        gl_filter_max = modes[i].maximize

        var glt: image_t
        // change all the existing mipmap texture objects
        run {
            i = 0
            while (i < numgltextures) {
                glt = gltextures[i]

                if (glt.type != it_pic && glt.type != it_sky) {
                    GL_Bind(glt.texnum)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, gl_filter_min)
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, gl_filter_max)
                }
                i++
            }
        }
    }

    /*
	===============
	GL_TextureAlphaMode
	===============
	*/
    fun GL_TextureAlphaMode(string: String) {

        var i: Int
        run {
            i = 0
            while (i < NUM_GL_ALPHA_MODES) {
                if (gl_alpha_modes[i].name.equalsIgnoreCase(string))
                    break
                i++
            }
        }

        if (i == NUM_GL_ALPHA_MODES) {
            VID.Printf(Defines.PRINT_ALL, "bad alpha texture mode name: [" + string + "]\n")
            return
        }

        gl_tex_alpha_format = gl_alpha_modes[i].mode
    }

    /*
	===============
	GL_TextureSolidMode
	===============
	*/
    fun GL_TextureSolidMode(string: String) {
        var i: Int
        run {
            i = 0
            while (i < NUM_GL_SOLID_MODES) {
                if (gl_solid_modes[i].name.equalsIgnoreCase(string))
                    break
                i++
            }
        }

        if (i == NUM_GL_SOLID_MODES) {
            VID.Printf(Defines.PRINT_ALL, "bad solid texture mode name: [" + string + "]\n")
            return
        }

        gl_tex_solid_format = gl_solid_modes[i].mode
    }

    /*
	===============
	GL_ImageList_f
	===============
	*/
    fun GL_ImageList_f() {

        val image: image_t
        val texels: Int
        val palstrings = array<String>("RGB", "PAL")

        VID.Printf(Defines.PRINT_ALL, "------------------\n")
        texels = 0

        for (i in 0..numgltextures - 1) {
            image = gltextures[i]
            if (image.texnum <= 0)
                continue

            texels += image.upload_width * image.upload_height
            when (image.type) {
                it_skin -> VID.Printf(Defines.PRINT_ALL, "M")
                it_sprite -> VID.Printf(Defines.PRINT_ALL, "S")
                it_wall -> VID.Printf(Defines.PRINT_ALL, "W")
                it_pic -> VID.Printf(Defines.PRINT_ALL, "P")
                else -> VID.Printf(Defines.PRINT_ALL, " ")
            }

            VID.Printf(Defines.PRINT_ALL, " %3i %3i %s: %s\n", Vargs(4).add(image.upload_width).add(image.upload_height).add(palstrings[if ((image.paletted)) 1 else 0]).add(image.name))
        }
        VID.Printf(Defines.PRINT_ALL, "Total texel count (not counting mipmaps): " + texels + '\n')
    }

    var scrap_allocated = Array<IntArray>(MAX_SCRAPS, { IntArray(BLOCK_WIDTH) })
    var scrap_texels = Array<ByteArray>(MAX_SCRAPS, { ByteArray(BLOCK_WIDTH * BLOCK_HEIGHT) })
    var scrap_dirty: Boolean = false

    class pos_t(var x: Int, var y: Int)

    // returns a texture number and the position inside it
    fun Scrap_AllocBlock(w: Int, h: Int, pos: pos_t): Int {
        var i: Int
        var j: Int
        var best: Int
        var best2: Int
        var texnum: Int

        run {
            texnum = 0
            while (texnum < MAX_SCRAPS) {
                best = BLOCK_HEIGHT

                run {
                    i = 0
                    while (i < BLOCK_WIDTH - w) {
                        best2 = 0

                        run {
                            j = 0
                            while (j < w) {
                                if (scrap_allocated[texnum][i + j] >= best)
                                    break
                                if (scrap_allocated[texnum][i + j] > best2)
                                    best2 = scrap_allocated[texnum][i + j]
                                j++
                            }
                        }
                        if (j == w) {
                            // this is a valid spot
                            pos.x = i
                            pos.y = best = best2
                        }
                        i++
                    }
                }

                if (best + h > BLOCK_HEIGHT)
                    continue

                run {
                    i = 0
                    while (i < w) {
                        scrap_allocated[texnum][pos.x + i] = best + h
                        i++
                    }
                }

                return texnum
                texnum++
            }
        }

        return -1
        // Sys_Error ("Scrap_AllocBlock: full");
    }

    var scrap_uploads = 0

    fun Scrap_Upload() {
        scrap_uploads++
        GL_Bind(TEXNUM_SCRAPS)
        GL_Upload8(scrap_texels[0], BLOCK_WIDTH, BLOCK_HEIGHT, false, false)
        scrap_dirty = false
    }

    /*
	=================================================================
	
	PCX LOADING
	
	=================================================================
	*/

    /*
	==============
	LoadPCX
	==============
	*/
    fun LoadPCX(filename: String, palette: Array<ByteArray>?, dim: Dimension?): ByteArray? {
        val pcx: qfiles.pcx_t

        //
        // load the file
        //
        val raw = FS.LoadFile(filename)

        if (raw == null) {
            VID.Printf(Defines.PRINT_DEVELOPER, "Bad pcx file " + filename + '\n')
            return null
        }

        //
        // parse the PCX file
        //
        pcx = qfiles.pcx_t(raw)

        if (pcx.manufacturer != 10 || pcx.version != 5 || pcx.encoding != 1 || pcx.bits_per_pixel != 8 || pcx.xmax >= 640 || pcx.ymax >= 480) {

            VID.Printf(Defines.PRINT_ALL, "Bad pcx file " + filename + '\n')
            return null
        }

        val width = pcx.xmax - pcx.xmin + 1
        val height = pcx.ymax - pcx.ymin + 1

        val pix = ByteArray(width * height)

        if (palette != null) {
            palette[0] = ByteArray(768)
            System.arraycopy(raw, raw!!.size() - 768, palette[0], 0, 768)
        }

        if (dim != null) {
            dim!!.width = width
            dim!!.height = height
        }

        //
        // decode pcx
        //
        var count = 0
        var dataByte: Byte = 0
        var runLength = 0
        var x: Int
        var y: Int

        run {
            y = 0
            while (y < height) {
                run {
                    x = 0
                    while (x < width) {

                        dataByte = pcx.data.get()

                        if ((dataByte and 192) == 192) {
                            runLength = dataByte and 63
                            dataByte = pcx.data.get()
                            // write runLength pixel
                            while (runLength-- > 0) {
                                pix[count++] = dataByte
                                x++
                            }
                        } else {
                            // write one pixel
                            pix[count++] = dataByte
                            x++
                        }
                    }
                }
                y++
            }
        }
        return pix
    }

    private val gotoBreakOut = Throwable()
    private val gotoDone = gotoBreakOut

    //	/*
    //	=========================================================
    //
    //	TARGA LOADING
    //
    //	=========================================================
    //	*/
    /*
	=============
	LoadTGA
	=============
	*/
    fun LoadTGA(name: String, dim: Dimension?): ByteArray? {
        val columns: Int
        val rows: Int
        val numPixels: Int
        var pixbuf: Int // index into pic
        var row: Int
        var column: Int
        val raw: ByteArray?
        val buf_p: ByteBuffer
        val targa_header: qfiles.tga_t
        var pic: ByteArray? = null

        //
        // load the file
        //
        raw = FS.LoadFile(name)

        if (raw == null) {
            VID.Printf(Defines.PRINT_DEVELOPER, "Bad tga file " + name + '\n')
            return null
        }

        targa_header = qfiles.tga_t(raw)

        if (targa_header.image_type != 2 && targa_header.image_type != 10)
            Com.Error(Defines.ERR_DROP, "LoadTGA: Only type 2 and 10 targa RGB images supported\n")

        if (targa_header.colormap_type != 0 || (targa_header.pixel_size != 32 && targa_header.pixel_size != 24))
            Com.Error(Defines.ERR_DROP, "LoadTGA: Only 32 or 24 bit images supported (no colormaps)\n")

        columns = targa_header.width
        rows = targa_header.height
        numPixels = columns * rows

        if (dim != null) {
            dim!!.width = columns
            dim!!.height = rows
        }

        pic = ByteArray(numPixels * 4) // targa_rgba;

        if (targa_header.id_length != 0)
            targa_header.data.position(targa_header.id_length)  // skip TARGA image comment

        buf_p = targa_header.data

        var red: Byte
        var green: Byte
        var blue: Byte
        var alphabyte: Byte
        red = green = blue = alphabyte = 0
        var packetHeader: Int
        var packetSize: Int
        var j: Int

        if (targa_header.image_type == 2) {
            // Uncompressed, RGB images
            run {
                row = rows - 1
                while (row >= 0) {

                    pixbuf = row * columns * 4

                    run {
                        column = 0
                        while (column < columns) {
                            when (targa_header.pixel_size) {
                                24 -> {

                                    blue = buf_p.get()
                                    green = buf_p.get()
                                    red = buf_p.get()
                                    pic[pixbuf++] = red
                                    pic[pixbuf++] = green
                                    pic[pixbuf++] = blue
                                    pic[pixbuf++] = 255.toByte()
                                }
                                32 -> {
                                    blue = buf_p.get()
                                    green = buf_p.get()
                                    red = buf_p.get()
                                    alphabyte = buf_p.get()
                                    pic[pixbuf++] = red
                                    pic[pixbuf++] = green
                                    pic[pixbuf++] = blue
                                    pic[pixbuf++] = alphabyte
                                }
                            }
                            column++
                        }
                    }
                    row--
                }
            }
        } else if (targa_header.image_type == 10) {
            // Runlength encoded RGB images
            run {
                row = rows - 1
                while (row >= 0) {

                    pixbuf = row * columns * 4
                    try {

                        run {
                            column = 0
                            while (column < columns) {

                                packetHeader = buf_p.get() and 255
                                packetSize = 1 + (packetHeader and 127)

                                if ((packetHeader and 128) != 0) {
                                    // run-length packet
                                    when (targa_header.pixel_size) {
                                        24 -> {
                                            blue = buf_p.get()
                                            green = buf_p.get()
                                            red = buf_p.get()
                                            alphabyte = 255.toByte()
                                        }
                                        32 -> {
                                            blue = buf_p.get()
                                            green = buf_p.get()
                                            red = buf_p.get()
                                            alphabyte = buf_p.get()
                                        }
                                    }

                                    run {
                                        j = 0
                                        while (j < packetSize) {
                                            pic[pixbuf++] = red
                                            pic[pixbuf++] = green
                                            pic[pixbuf++] = blue
                                            pic[pixbuf++] = alphabyte
                                            column++
                                            if (column == columns) {
                                                // run spans across rows
                                                column = 0
                                                if (row > 0)
                                                    row--
                                                else
                                                // goto label breakOut;
                                                    throw gotoBreakOut

                                                pixbuf = row * columns * 4
                                            }
                                            j++
                                        }
                                    }
                                } else {
                                    // non run-length packet
                                    run {
                                        j = 0
                                        while (j < packetSize) {
                                            when (targa_header.pixel_size) {
                                                24 -> {
                                                    blue = buf_p.get()
                                                    green = buf_p.get()
                                                    red = buf_p.get()
                                                    pic[pixbuf++] = red
                                                    pic[pixbuf++] = green
                                                    pic[pixbuf++] = blue
                                                    pic[pixbuf++] = 255.toByte()
                                                }
                                                32 -> {
                                                    blue = buf_p.get()
                                                    green = buf_p.get()
                                                    red = buf_p.get()
                                                    alphabyte = buf_p.get()
                                                    pic[pixbuf++] = red
                                                    pic[pixbuf++] = green
                                                    pic[pixbuf++] = blue
                                                    pic[pixbuf++] = alphabyte
                                                }
                                            }
                                            column++
                                            if (column == columns) {
                                                // pixel packet run spans across rows
                                                column = 0
                                                if (row > 0)
                                                    row--
                                                else
                                                // goto label breakOut;
                                                    throw gotoBreakOut

                                                pixbuf = row * columns * 4
                                            }
                                            j++
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        // label breakOut:
                    }

                    row--
                }
            }
        }
        return pic
    }

    /*
	====================================================================
	
	IMAGE FLOOD FILLING
	
	====================================================================
	*/

    /*
	=================
	Mod_FloodFillSkin
	
	Fill background pixels so mipmapping doesn't have haloes
	=================
	*/

    class floodfill_t {
        var x: Short = 0
        var y: Short = 0
    }

    // TODO check this: R_FloodFillSkin( byte[] skin, int skinwidth, int skinheight)
    fun R_FloodFillSkin(skin: ByteArray, skinwidth: Int, skinheight: Int) {
        //		byte				fillcolor = *skin; // assume this is the pixel to fill
        val fillcolor = skin[0] and 255
        //		floodfill_t[] fifo = new floodfill_t[FLOODFILL_FIFO_SIZE];
        var inpt = 0
        var outpt = 0
        var filledcolor = -1
        var i: Int

        //		for (int j = 0; j < fifo.length; j++) {
        //			fifo[j] = new floodfill_t();
        //		}

        if (filledcolor == -1) {
            filledcolor = 0
            // attempt to find opaque black
            run {
                i = 0
                while (i < 256) {
                    // TODO check this
                    if (d_8to24table[i] == -16777216) {
                        // alpha 1.0
                        //if (d_8to24table[i] == (255 << 0)) // alpha 1.0
                        filledcolor = i
                        break
                    }
                    ++i
                }
            }
        }

        // can't fill to filled color or to transparent color (used as visited marker)
        if ((fillcolor == filledcolor) || (fillcolor == 255)) {
            return
        }

        fifo[inpt].x = 0
        fifo[inpt].y = 0
        inpt = (inpt + 1) and FLOODFILL_FIFO_MASK

        while (outpt != inpt) {
            val x = fifo[outpt].x.toInt()
            val y = fifo[outpt].y.toInt()
            var fdc = filledcolor
            //			byte		*pos = &skin[x + skinwidth * y];
            val pos = x + skinwidth * y
            //
            outpt = (outpt + 1) and FLOODFILL_FIFO_MASK

            val off: Int
            val dx: Int
            val dy: Int

            if (x > 0) {
                // FLOODFILL_STEP( -1, -1, 0 );
                off = -1
                dx = -1
                dy = 0
                if (skin[pos + off] == fillcolor.toByte()) {
                    skin[pos + off] = 255.toByte()
                    fifo[inpt].x = (x + dx).toShort()
                    fifo[inpt].y = (y + dy).toShort()
                    inpt = (inpt + 1) and FLOODFILL_FIFO_MASK
                } else if (skin[pos + off] != 255.toByte())
                    fdc = skin[pos + off] and 255
            }

            if (x < skinwidth - 1) {
                // FLOODFILL_STEP( 1, 1, 0 );
                off = 1
                dx = 1
                dy = 0
                if (skin[pos + off] == fillcolor.toByte()) {
                    skin[pos + off] = 255.toByte()
                    fifo[inpt].x = (x + dx).toShort()
                    fifo[inpt].y = (y + dy).toShort()
                    inpt = (inpt + 1) and FLOODFILL_FIFO_MASK
                } else if (skin[pos + off] != 255.toByte())
                    fdc = skin[pos + off] and 255
            }

            if (y > 0) {
                // FLOODFILL_STEP( -skinwidth, 0, -1 );
                off = -skinwidth
                dx = 0
                dy = -1
                if (skin[pos + off] == fillcolor.toByte()) {
                    skin[pos + off] = 255.toByte()
                    fifo[inpt].x = (x + dx).toShort()
                    fifo[inpt].y = (y + dy).toShort()
                    inpt = (inpt + 1) and FLOODFILL_FIFO_MASK
                } else if (skin[pos + off] != 255.toByte())
                    fdc = skin[pos + off] and 255
            }

            if (y < skinheight - 1) {
                // FLOODFILL_STEP( skinwidth, 0, 1 );
                off = skinwidth
                dx = 0
                dy = 1
                if (skin[pos + off] == fillcolor.toByte()) {
                    skin[pos + off] = 255.toByte()
                    fifo[inpt].x = (x + dx).toShort()
                    fifo[inpt].y = (y + dy).toShort()
                    inpt = (inpt + 1) and FLOODFILL_FIFO_MASK
                } else if (skin[pos + off] != 255.toByte())
                    fdc = skin[pos + off] and 255

            }

            skin[x + skinwidth * y] = fdc.toByte()
        }
    }

    //	  =======================================================

    /*
	================
	GL_ResampleTexture
	================
	*/
    // cwei :-)
    fun GL_ResampleTexture(`in`: IntArray, inwidth: Int, inheight: Int, out: IntArray, outwidth: Int, outheight: Int) {
        //		int		i, j;
        //		unsigned	*inrow, *inrow2;
        //		int frac, fracstep;
        //		int[] p1 = new int[1024];
        //		int[] p2 = new int[1024];
        //

        // *** this source do the same ***
        val image = BufferedImage(inwidth, inheight, BufferedImage.TYPE_INT_ARGB)

        image.setRGB(0, 0, inwidth, inheight, `in`, 0, inwidth)

        val op = AffineTransformOp(AffineTransform.getScaleInstance(outwidth.toDouble() * 1.0 / inwidth.toDouble(), outheight.toDouble() * 1.0 / inheight.toDouble()), AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
        val tmp = op.filter(image, null)

        tmp.getRGB(0, 0, outwidth, outheight, out, 0, outwidth)

        // *** end ***

        //		byte		*pix1, *pix2, *pix3, *pix4;
        //
        //		fracstep = inwidth*0x10000/outwidth;
        //
        //		frac = fracstep>>2;
        //		for (i=0 ; i<outwidth ; i++)
        //		{
        //			p1[i] = 4*(frac>>16);
        //			frac += fracstep;
        //		}
        //		frac = 3*(fracstep>>2);
        //		for (i=0 ; i<outwidth ; i++)
        //		{
        //			p2[i] = 4*(frac>>16);
        //			frac += fracstep;
        //		}
        //
        //		for (i=0 ; i<outheight ; i++, out += outwidth)
        //		{
        //			inrow = in + inwidth*(int)((i+0.25)*inheight/outheight);
        //			inrow2 = in + inwidth*(int)((i+0.75)*inheight/outheight);
        //			frac = fracstep >> 1;
        //			for (j=0 ; j<outwidth ; j++)
        //			{
        //				pix1 = (byte *)inrow + p1[j];
        //				pix2 = (byte *)inrow + p2[j];
        //				pix3 = (byte *)inrow2 + p1[j];
        //				pix4 = (byte *)inrow2 + p2[j];
        //				((byte *)(out+j))[0] = (pix1[0] + pix2[0] + pix3[0] + pix4[0])>>2;
        //				((byte *)(out+j))[1] = (pix1[1] + pix2[1] + pix3[1] + pix4[1])>>2;
        //				((byte *)(out+j))[2] = (pix1[2] + pix2[2] + pix3[2] + pix4[2])>>2;
        //				((byte *)(out+j))[3] = (pix1[3] + pix2[3] + pix3[3] + pix4[3])>>2;
        //			}
        //		}
    }

    /*
	================
	GL_LightScaleTexture
	
	Scale up the pixel values in a texture to increase the
	lighting range
	================
	*/
    fun GL_LightScaleTexture(`in`: IntArray, inwidth: Int, inheight: Int, only_gamma: Boolean) {
        if (only_gamma) {
            var i: Int
            val c: Int
            var r: Int
            var g: Int
            var b: Int
            var color: Int

            c = inwidth * inheight
            run {
                i = 0
                while (i < c) {
                    color = `in`[i]
                    r = (color shr 0) and 255
                    g = (color shr 8) and 255
                    b = (color shr 16) and 255

                    r = gammatable[r] and 255
                    g = gammatable[g] and 255
                    b = gammatable[b] and 255

                    `in`[i] = (r shl 0) or (g shl 8) or (b shl 16) or (color and -16777216)
                    i++
                }
            }
        } else {
            var i: Int
            val c: Int
            var r: Int
            var g: Int
            var b: Int
            var color: Int

            c = inwidth * inheight
            run {
                i = 0
                while (i < c) {
                    color = `in`[i]
                    r = (color shr 0) and 255
                    g = (color shr 8) and 255
                    b = (color shr 16) and 255

                    r = gammatable[intensitytable[r] and 255] and 255
                    g = gammatable[intensitytable[g] and 255] and 255
                    b = gammatable[intensitytable[b] and 255] and 255

                    `in`[i] = (r shl 0) or (g shl 8) or (b shl 16) or (color and -16777216)
                    i++
                }
            }

        }
    }

    /*
	================
	GL_MipMap
	
	Operates in place, quartering the size of the texture
	================
	*/
    fun GL_MipMap(`in`: IntArray, width: Int, height: Int) {
        var i: Int
        var j: Int
        val out: IntArray

        out = `in`

        var inIndex = 0
        var outIndex = 0

        var r: Int
        var g: Int
        var b: Int
        var a: Int
        var p1: Int
        var p2: Int
        var p3: Int
        var p4: Int

        run {
            i = 0
            while (i < height) {
                run {
                    j = 0
                    while (j < width) {

                        p1 = `in`[inIndex + 0]
                        p2 = `in`[inIndex + 1]
                        p3 = `in`[inIndex + width + 0]
                        p4 = `in`[inIndex + width + 1]

                        r = (((p1 shr 0) and 255) + ((p2 shr 0) and 255) + ((p3 shr 0) and 255) + ((p4 shr 0) and 255)) shr 2
                        g = (((p1 shr 8) and 255) + ((p2 shr 8) and 255) + ((p3 shr 8) and 255) + ((p4 shr 8) and 255)) shr 2
                        b = (((p1 shr 16) and 255) + ((p2 shr 16) and 255) + ((p3 shr 16) and 255) + ((p4 shr 16) and 255)) shr 2
                        a = (((p1 shr 24) and 255) + ((p2 shr 24) and 255) + ((p3 shr 24) and 255) + ((p4 shr 24) and 255)) shr 2

                        out[outIndex] = (r shl 0) or (g shl 8) or (b shl 16) or (a shl 24)
                        j += 2
                        outIndex += 1
                        inIndex += 2
                    }
                }
                i += 2
                inIndex += width
            }
        }
    }

    /*
	===============
	GL_Upload32
	
	Returns has_alpha
	===============
	*/
    fun GL_BuildPalettedTexture(paletted_texture: ByteBuffer, scaled: IntArray, scaled_width: Int, scaled_height: Int) {

        val r: Int
        val g: Int
        val b: Int
        val c: Int
        val size = scaled_width * scaled_height

        for (i in 0..size - 1) {

            r = (scaled[i] shr 3) and 31
            g = (scaled[i] shr 10) and 63
            b = (scaled[i] shr 19) and 31

            c = r or (g shl 5) or (b shl 11)

            paletted_texture.put(i, gl_state.d_16to8table[c])
        }
    }

    var upload_width: Int = 0
    var upload_height: Int = 0
    var uploaded_paletted: Boolean = false

    /*
	===============
	GL_Upload32
	
	Returns has_alpha
	===============
	*/
    var scaled = IntArray(256 * 256)
    //byte[] paletted_texture = new byte[256 * 256];
    var paletted_texture = BufferUtils.createByteBuffer(256 * 256)
    var tex = Lib.newIntBuffer(512 * 256, ByteOrder.LITTLE_ENDIAN)

    fun GL_Upload32(data: IntArray, width: Int, height: Int, mipmap: Boolean): Boolean {
        var samples: Int
        var scaled_width: Int
        var scaled_height: Int
        var i: Int
        val c: Int
        val comp: Int

        Arrays.fill(scaled, 0)
        // Arrays.fill(paletted_texture, (byte)0);
        paletted_texture.clear()
        for (j in 0..256 * 256 - 1) paletted_texture.put(j, 0.toByte())

        uploaded_paletted = false

        run {
            scaled_width = 1
            while (scaled_width < width) {
                scaled_width = scaled_width shl 1
            }
        }
        if (gl_round_down.value > 0.0.toFloat() && scaled_width > width && mipmap)
            scaled_width = scaled_width shr 1
        run {
            scaled_height = 1
            while (scaled_height < height) {
                scaled_height = scaled_height shl 1
            }
        }
        if (gl_round_down.value > 0.0.toFloat() && scaled_height > height && mipmap)
            scaled_height = scaled_height shr 1

        // let people sample down the world textures for speed
        if (mipmap) {
            scaled_width = scaled_width shr gl_picmip.value as Int
            scaled_height = scaled_height shr gl_picmip.value as Int
        }

        // don't ever bother with >256 textures
        if (scaled_width > 256)
            scaled_width = 256
        if (scaled_height > 256)
            scaled_height = 256

        if (scaled_width < 1)
            scaled_width = 1
        if (scaled_height < 1)
            scaled_height = 1

        upload_width = scaled_width
        upload_height = scaled_height

        if (scaled_width * scaled_height > 256 * 256)
            Com.Error(Defines.ERR_DROP, "GL_Upload32: too big")

        // scan the texture for any non-255 alpha
        c = width * height
        samples = gl_solid_format

        run {
            i = 0
            while (i < c) {
                if ((data[i] and -16777216) != -16777216) {
                    samples = gl_alpha_format
                    break
                }
                i++
            }
        }

        if (samples == gl_solid_format)
            comp = gl_tex_solid_format
        else if (samples == gl_alpha_format)
            comp = gl_tex_alpha_format
        else {
            VID.Printf(Defines.PRINT_ALL, "Unknown number of texture components " + samples + '\n')
            comp = samples
        }

        // simulates a goto
        try {
            if (scaled_width == width && scaled_height == height) {
                if (!mipmap) {
                    if (qglColorTableEXT && gl_ext_palettedtexture.value != 0.0.toFloat() && samples == gl_solid_format) {
                        uploaded_paletted = true
                        GL_BuildPalettedTexture(paletted_texture, data, scaled_width, scaled_height)
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_COLOR_INDEX8_EXT, scaled_width, scaled_height, 0, GL11.GL_COLOR_INDEX, GL11.GL_UNSIGNED_BYTE, paletted_texture)
                    } else {
                        tex.rewind()
                        tex.put(data)
                        tex.rewind()
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, comp, scaled_width, scaled_height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, tex)
                    }
                    //goto done;
                    throw gotoDone
                }
                //memcpy (scaled, data, width*height*4); were bytes
                System.arraycopy(data, 0, scaled, 0, width * height)
            } else
                GL_ResampleTexture(data, width, height, scaled, scaled_width, scaled_height)

            GL_LightScaleTexture(scaled, scaled_width, scaled_height, !mipmap)

            if (qglColorTableEXT && gl_ext_palettedtexture.value != 0.0.toFloat() && (samples == gl_solid_format)) {
                uploaded_paletted = true
                GL_BuildPalettedTexture(paletted_texture, scaled, scaled_width, scaled_height)
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_COLOR_INDEX8_EXT, scaled_width, scaled_height, 0, GL11.GL_COLOR_INDEX, GL11.GL_UNSIGNED_BYTE, paletted_texture)
            } else {
                tex.rewind()
                tex.put(scaled)
                tex.rewind()
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, comp, scaled_width, scaled_height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, tex)
            }

            if (mipmap) {
                var miplevel: Int
                miplevel = 0
                while (scaled_width > 1 || scaled_height > 1) {
                    GL_MipMap(scaled, scaled_width, scaled_height)
                    scaled_width = scaled_width shr 1
                    scaled_height = scaled_height shr 1
                    if (scaled_width < 1)
                        scaled_width = 1
                    if (scaled_height < 1)
                        scaled_height = 1

                    miplevel++
                    if (qglColorTableEXT && gl_ext_palettedtexture.value != 0.0.toFloat() && samples == gl_solid_format) {
                        uploaded_paletted = true
                        GL_BuildPalettedTexture(paletted_texture, scaled, scaled_width, scaled_height)
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, miplevel, GL_COLOR_INDEX8_EXT, scaled_width, scaled_height, 0, GL11.GL_COLOR_INDEX, GL11.GL_UNSIGNED_BYTE, paletted_texture)
                    } else {
                        tex.rewind()
                        tex.put(scaled)
                        tex.rewind()
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, miplevel, comp, scaled_width, scaled_height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, tex)
                    }
                }
            }
            // label done:
        } catch (e: Throwable) {
            // replaces label done
        }


        if (mipmap) {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, gl_filter_min)
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, gl_filter_max)
        } else {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, gl_filter_max)
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, gl_filter_max)
        }

        return (samples == gl_alpha_format)
    }

    /*
	===============
	GL_Upload8
	
	Returns has_alpha
	===============
	*/

    var trans = IntArray(512 * 256)

    fun GL_Upload8(data: ByteArray, width: Int, height: Int, mipmap: Boolean, is_sky: Boolean): Boolean {

        Arrays.fill(trans, 0)

        val s = width * height

        if (s > trans.size())
            Com.Error(Defines.ERR_DROP, "GL_Upload8: too large")

        if (qglColorTableEXT && gl_ext_palettedtexture.value != 0.0.toFloat() && is_sky) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_COLOR_INDEX8_EXT, width, height, 0, GL11.GL_COLOR_INDEX, GL11.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data))

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, gl_filter_max)
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, gl_filter_max)

            // TODO check this
            return false
        } else {
            var p: Int
            for (i in 0..s - 1) {
                p = data[i] and 255
                trans[i] = d_8to24table[p]

                if (p == 255) {
                    // transparent, so scan around for another color
                    // to avoid alpha fringes
                    // FIXME: do a full flood fill so mips work...
                    if (i > width && (data[i - width] and 255) != 255)
                        p = data[i - width] and 255
                    else if (i < s - width && (data[i + width] and 255) != 255)
                        p = data[i + width] and 255
                    else if (i > 0 && (data[i - 1] and 255) != 255)
                        p = data[i - 1] and 255
                    else if (i < s - 1 && (data[i + 1] and 255) != 255)
                        p = data[i + 1] and 255
                    else
                        p = 0
                    // copy rgb components

                    // ((byte *)&trans[i])[0] = ((byte *)&d_8to24table[p])[0];
                    // ((byte *)&trans[i])[1] = ((byte *)&d_8to24table[p])[1];
                    // ((byte *)&trans[i])[2] = ((byte *)&d_8to24table[p])[2];

                    trans[i] = d_8to24table[p] and 16777215 // only rgb
                }
            }

            return GL_Upload32(trans, width, height, mipmap)
        }
    }

    /*
	================
	GL_LoadPic
	
	This is also used as an entry point for the generated r_notexture
	================
	*/
    fun GL_LoadPic(name: String, pic: ByteArray, width: Int, height: Int, type: Int, bits: Int): image_t {
        var image: image_t
        var i: Int

        // find a free image_t
        run {
            i = 0
            while (i < numgltextures) {
                image = gltextures[i]
                if (image.texnum == 0)
                    break
                i++
            }
        }

        if (i == numgltextures) {
            if (numgltextures == MAX_GLTEXTURES)
                Com.Error(Defines.ERR_DROP, "MAX_GLTEXTURES")

            numgltextures++
        }
        image = gltextures[i]

        if (name.length() > Defines.MAX_QPATH)
            Com.Error(Defines.ERR_DROP, "Draw_LoadPic: \"" + name + "\" is too long")

        image.name = name
        image.registration_sequence = registration_sequence

        image.width = width
        image.height = height
        image.type = type


        if (type == it_skin && bits == 8)
            R_FloodFillSkin(pic, width, height)

        // load little pics into the scrap
        if (image.type == it_pic && bits == 8 && image.width < 64 && image.height < 64) {
            val pos = pos_t(0, 0)
            var j: Int
            var k: Int

            val texnum = Scrap_AllocBlock(image.width, image.height, pos)

            if (texnum == -1) {
                // replace goto nonscrap

                image.scrap = false

                image.texnum = TEXNUM_IMAGES + image.getId() // image pos in array
                GL_Bind(image.texnum)

                if (bits == 8) {
                    image.has_alpha = GL_Upload8(pic, width, height, (image.type != it_pic && image.type != it_sky), image.type == it_sky)
                } else {
                    val tmp = IntArray(pic.size() / 4)

                    run {
                        i = 0
                        while (i < tmp.size()) {
                            tmp[i] = ((pic[4 * i + 0] and 255) shl 0) // & 0x000000FF;
                            tmp[i] = tmp[i] or ((pic[4 * i + 1] and 255) shl 8) // & 0x0000FF00;
                            tmp[i] = tmp[i] or ((pic[4 * i + 2] and 255) shl 16) // & 0x00FF0000;
                            tmp[i] = tmp[i] or ((pic[4 * i + 3] and 255) shl 24) // & 0xFF000000;
                            i++
                        }
                    }

                    image.has_alpha = GL_Upload32(tmp, width, height, (image.type != it_pic && image.type != it_sky))
                }

                image.upload_width = upload_width // after power of 2 and scales
                image.upload_height = upload_height
                image.paletted = uploaded_paletted
                image.sl = 0
                image.sh = 1
                image.tl = 0
                image.th = 1

                return image
            }

            scrap_dirty = true

            // copy the texels into the scrap block
            k = 0
            run {
                i = 0
                while (i < image.height) {
                    run {
                        j = 0
                        while (j < image.width) {
                            scrap_texels[texnum][(pos.y + i) * BLOCK_WIDTH + pos.x + j] = pic[k]
                            j++
                            k++
                        }
                    }
                    i++
                }
            }

            image.texnum = TEXNUM_SCRAPS + texnum
            image.scrap = true
            image.has_alpha = true
            image.sl = (pos.x.toFloat() + 0.01.toFloat()) / BLOCK_WIDTH.toFloat()
            image.sh = (pos.x + image.width - 0.01.toFloat()) / BLOCK_WIDTH.toFloat()
            image.tl = (pos.y.toFloat() + 0.01.toFloat()) / BLOCK_WIDTH.toFloat()
            image.th = (pos.y + image.height - 0.01.toFloat()) / BLOCK_WIDTH.toFloat()

        } else {
            // this was label nonscrap

            image.scrap = false

            image.texnum = TEXNUM_IMAGES + image.getId() //image pos in array
            GL_Bind(image.texnum)

            if (bits == 8) {
                image.has_alpha = GL_Upload8(pic, width, height, (image.type != it_pic && image.type != it_sky), image.type == it_sky)
            } else {
                val tmp = IntArray(pic.size() / 4)

                run {
                    i = 0
                    while (i < tmp.size()) {
                        tmp[i] = ((pic[4 * i + 0] and 255) shl 0) // & 0x000000FF;
                        tmp[i] = tmp[i] or ((pic[4 * i + 1] and 255) shl 8) // & 0x0000FF00;
                        tmp[i] = tmp[i] or ((pic[4 * i + 2] and 255) shl 16) // & 0x00FF0000;
                        tmp[i] = tmp[i] or ((pic[4 * i + 3] and 255) shl 24) // & 0xFF000000;
                        i++
                    }
                }

                image.has_alpha = GL_Upload32(tmp, width, height, (image.type != it_pic && image.type != it_sky))
            }
            image.upload_width = upload_width // after power of 2 and scales
            image.upload_height = upload_height
            image.paletted = uploaded_paletted
            image.sl = 0
            image.sh = 1
            image.tl = 0
            image.th = 1
        }
        return image
    }

    /*
	================
	GL_LoadWal
	================
	*/
    fun GL_LoadWal(name: String): image_t {

        var image: image_t? = null

        val raw = FS.LoadFile(name)
        if (raw == null) {
            VID.Printf(Defines.PRINT_ALL, "GL_FindImage: can't load " + name + '\n')
            return r_notexture
        }

        val mt = qfiles.miptex_t(raw)

        val pix = ByteArray(mt.width * mt.height)
        System.arraycopy(raw, mt.offsets[0], pix, 0, pix.size())

        image = GL_LoadPic(name, pix, mt.width, mt.height, it_wall, 8)

        return image
    }

    /*
	===============
	GL_FindImage
	
	Finds or loads the given image
	===============
	*/
    fun GL_FindImage(name: String?, type: Int): image_t? {
        var image: image_t? = null

        //		// TODO loest das grossschreibungs problem
        //		name = name.toLowerCase();
        //		// bughack for bad strings (fuck \0)
        //		int index = name.indexOf('\0');
        //		if (index != -1)
        //			name = name.substring(0, index);

        if (name == null || name.length() < 5)
            return null //	Com.Error (ERR_DROP, "GL_FindImage: NULL name");
        //	Com.Error (ERR_DROP, "GL_FindImage: bad name: %s", name);

        // look for it
        for (i in 0..numgltextures - 1) {
            image = gltextures[i]
            if (name.equals(image!!.name)) {
                image!!.registration_sequence = registration_sequence
                return image
            }
        }

        //
        // load the pic from disk
        //
        image = null
        var pic: ByteArray? = null
        val dim = Dimension()

        if (name.endsWith(".pcx")) {

            pic = LoadPCX(name, null, dim)
            if (pic == null)
                return null
            image = GL_LoadPic(name, pic, dim.width, dim.height, type, 8)

        } else if (name.endsWith(".wal")) {

            image = GL_LoadWal(name)

        } else if (name.endsWith(".tga")) {

            pic = LoadTGA(name, dim)

            if (pic == null)
                return null

            image = GL_LoadPic(name, pic, dim.width, dim.height, type, 32)

        }

        return image
    }

    /*
	===============
	R_RegisterSkin
	===============
	*/
    protected fun R_RegisterSkin(name: String): image_t {
        return GL_FindImage(name, it_skin)
    }

    var texnumBuffer = BufferUtils.createIntBuffer(1)

    /*
	================
	GL_FreeUnusedImages
	
	Any image that was not touched on this registration sequence
	will be freed.
	================
	*/
    fun GL_FreeUnusedImages() {

        // never free r_notexture or particle texture
        r_notexture.registration_sequence = registration_sequence
        r_particletexture.registration_sequence = registration_sequence

        var image: image_t? = null

        for (i in 0..numgltextures - 1) {
            image = gltextures[i]
            // used this sequence
            if (image!!.registration_sequence == registration_sequence)
                continue
            // free image_t slot
            if (image!!.registration_sequence == 0)
                continue
            // don't free pics
            if (image!!.type == it_pic)
                continue

            // free it
            // TODO jogl bug
            texnumBuffer.clear()
            texnumBuffer.put(0, image!!.texnum)
            GL11.glDeleteTextures(texnumBuffer)
            image!!.clear()
        }
    }

    /*
	===============
	Draw_GetPalette
	===============
	*/
    protected fun Draw_GetPalette() {
        val r: Int
        val g: Int
        val b: Int
        val palette = arrayOfNulls<ByteArray>(1) //new byte[768];

        // get the palette

        LoadPCX("pics/colormap.pcx", palette, Dimension())

        if (palette[0] == null || palette[0].size() != 768)
            Com.Error(Defines.ERR_FATAL, "Couldn't load pics/colormap.pcx")

        val pal = palette[0]

        var j = 0
        for (i in 0..256 - 1) {
            r = pal[j++] and 255
            g = pal[j++] and 255
            b = pal[j++] and 255

            d_8to24table[i] = (255 shl 24) or (b shl 16) or (g shl 8) or (r shl 0)
        }

        d_8to24table[255] = d_8to24table[255] and 16777215 // 255 is transparent

        particle_t.setColorPalette(d_8to24table)
    }

    /*
	===============
	GL_InitImages
	===============
	*/
    fun GL_InitImages() {
        var i: Int
        var j: Int
        var g = vid_gamma.value

        registration_sequence = 1

        // init intensity conversions
        intensity = Cvar.Get("intensity", "2", 0)

        if (intensity.value <= 1)
            Cvar.Set("intensity", "1")

        gl_state.inverse_intensity = 1 / intensity.value

        Draw_GetPalette()

        if (qglColorTableEXT) {
            gl_state.d_16to8table = FS.LoadFile("pics/16to8.dat")
            if (gl_state.d_16to8table == null)
                Com.Error(Defines.ERR_FATAL, "Couldn't load pics/16to8.pcx")
        }

        if ((gl_config.renderer and (GL_RENDERER_VOODOO or GL_RENDERER_VOODOO2)) != 0) {
            g = 1.0.toFloat()
        }

        run {
            i = 0
            while (i < 256) {

                if (g == 1.0.toFloat()) {
                    gammatable[i] = i.toByte()
                } else {

                    var inf = (255.0.toFloat() * Math.pow((i.toDouble() + 0.5) / 255.5, g) + 0.5) as Int
                    if (inf < 0)
                        inf = 0
                    if (inf > 255)
                        inf = 255
                    gammatable[i] = inf.toByte()
                }
                i++
            }
        }

        run {
            i = 0
            while (i < 256) {
                j = (i * intensity.value) as Int
                if (j > 255)
                    j = 255
                intensitytable[i] = j.toByte()
                i++
            }
        }
    }

    /*
	===============
	GL_ShutdownImages
	===============
	*/
    fun GL_ShutdownImages() {
        val image: image_t

        for (i in 0..numgltextures - 1) {
            image = gltextures[i]

            if (image.registration_sequence == 0)
                continue // free image_t slot
            // free it
            // TODO jogl bug
            texnumBuffer.clear()
            texnumBuffer.put(0, image.texnum)
            GL11.glDeleteTextures(texnumBuffer)
            image.clear()
        }
    }

    companion object {

        val modes = array<glmode_t>(glmode_t("GL_NEAREST", GL11.GL_NEAREST, GL11.GL_NEAREST), glmode_t("GL_LINEAR", GL11.GL_LINEAR, GL11.GL_LINEAR), glmode_t("GL_NEAREST_MIPMAP_NEAREST", GL11.GL_NEAREST_MIPMAP_NEAREST, GL11.GL_NEAREST), glmode_t("GL_LINEAR_MIPMAP_NEAREST", GL11.GL_LINEAR_MIPMAP_NEAREST, GL11.GL_LINEAR), glmode_t("GL_NEAREST_MIPMAP_LINEAR", GL11.GL_NEAREST_MIPMAP_LINEAR, GL11.GL_NEAREST), glmode_t("GL_LINEAR_MIPMAP_LINEAR", GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR))

        val NUM_GL_MODES = modes.size()

        val gl_alpha_modes = array<gltmode_t>(gltmode_t("default", 4), gltmode_t("GL_RGBA", GL11.GL_RGBA), gltmode_t("GL_RGBA8", GL11.GL_RGBA8), gltmode_t("GL_RGB5_A1", GL11.GL_RGB5_A1), gltmode_t("GL_RGBA4", GL11.GL_RGBA4), gltmode_t("GL_RGBA2", GL11.GL_RGBA2))

        val NUM_GL_ALPHA_MODES = gl_alpha_modes.size()

        val gl_solid_modes = array<gltmode_t>(gltmode_t("default", 3), gltmode_t("GL_RGB", GL11.GL_RGB), gltmode_t("GL_RGB8", GL11.GL_RGB8), gltmode_t("GL_RGB5", GL11.GL_RGB5), gltmode_t("GL_RGB4", GL11.GL_RGB4), gltmode_t("GL_R3_G3_B2", GL11.GL_R3_G3_B2))//	#ifdef GL_RGB2_EXT
        //new gltmode_t("GL_RGB2", GL.GL_RGB2_EXT)
        //	#endif

        val NUM_GL_SOLID_MODES = gl_solid_modes.size()

        /*
	=============================================================================
	
	  scrap allocation
	
	  Allocate all the little status bar objects into a single texture
	  to crutch up inefficient hardware / drivers
	
	=============================================================================
	*/

        val MAX_SCRAPS = 1
        val BLOCK_WIDTH = 256
        val BLOCK_HEIGHT = 256

        // must be a power of 2
        val FLOODFILL_FIFO_SIZE = 4096
        val FLOODFILL_FIFO_MASK = FLOODFILL_FIFO_SIZE - 1
        //
        //	#define FLOODFILL_STEP( off, dx, dy ) \
        //	{ \
        //		if (pos[off] == fillcolor) \
        //		{ \
        //			pos[off] = 255; \
        //			fifo[inpt].x = x + (dx), fifo[inpt].y = y + (dy); \
        //			inpt = (inpt + 1) & FLOODFILL_FIFO_MASK; \
        //		} \
        //		else if (pos[off] != 255) fdc = pos[off]; \
        //	}

        //	void FLOODFILL_STEP( int off, int dx, int dy )
        //	{
        //		if (pos[off] == fillcolor)
        //		{
        //			pos[off] = 255;
        //			fifo[inpt].x = x + dx; fifo[inpt].y = y + dy;
        //			inpt = (inpt + 1) & FLOODFILL_FIFO_MASK;
        //		}
        //		else if (pos[off] != 255) fdc = pos[off];
        //	}
        var fifo = arrayOfNulls<floodfill_t>(FLOODFILL_FIFO_SIZE)
        {
            for (j in fifo.indices) {
                fifo[j] = floodfill_t()
            }
        }
    }

}
