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
import lwjake2.qcommon.Com
import lwjake2.qcommon.FS

/**
 * SND_MEM
 */
public class WaveLoader {

    class wavinfo_t {
        var rate: Int = 0
        var width: Int = 0
        var channels: Int = 0
        var loopstart: Int = 0
        var samples: Int = 0
        var dataofs: Int = 0 // chunk starts this many bytes from file start
    }

    companion object {

        /**
         * The ResampleSfx can squeeze and stretch samples to a default sample rate.
         * Since Joal and lwjgl sound drivers support this, we don't need it and the samples
         * can keep their original sample rate. Use this switch for reactivating resampling.
         */
        private val DONT_DO_A_RESAMPLING_FOR_JOAL_AND_LWJGL = true

        /**
         * This is the maximum sample length in bytes which has to be replaced by
         * a configurable variable.
         */
        private val maxsamplebytes = 2048 * 1024

        /**
         * Loads a sound from a wav file.
         */
        public fun LoadSound(s: sfx_t): sfxcache_t? {
            if (s.name.charAt(0) == '*')
                return null

            // see if still in memory
            var sc = s.cache
            if (sc != null)
                return sc

            val name: String
            // load it in
            if (s.truename != null)
                name = s.truename
            else
                name = s.name

            val namebuffer: String
            if (name.charAt(0) == '#')
                namebuffer = name.substring(1)
            else
                namebuffer = "sound/" + name

            var data = FS.LoadFile(namebuffer)

            if (data == null) {
                Com.DPrintf("Couldn't load " + namebuffer + "\n")
                return null
            }

            val size = data!!.size()

            val info = GetWavinfo(s.name, data, size)

            if (info.channels != 1) {
                Com.Printf(s.name + " is a stereo sample - ignoring\n")
                return null
            }

            val stepscale: Float
            if (DONT_DO_A_RESAMPLING_FOR_JOAL_AND_LWJGL)
                stepscale = 1
            else
                stepscale = info.rate.toFloat() / S.getDefaultSampleRate()

            var len = (info.samples.toFloat() / stepscale).toInt()
            len = len * info.width * info.channels

            // TODO: handle max sample bytes with a cvar
            if (len >= maxsamplebytes) {
                Com.Printf(s.name + " is too long: " + len + " bytes?! ignoring.\n")
                return null
            }

            sc = s.cache = sfxcache_t(len)

            sc!!.length = info.samples
            sc!!.loopstart = info.loopstart
            sc!!.speed = info.rate
            sc!!.width = info.width
            sc!!.stereo = info.channels

            ResampleSfx(s, sc!!.speed, sc!!.width, data, info.dataofs)
            data = null

            return sc
        }


        /**
         * Converts sample data with respect to the endianess and adjusts
         * the sample rate of a loaded sample, see flag DONT_DO_A_RESAMPLING_FOR_JOAL_AND_LWJGL.
         */
        public fun ResampleSfx(sfx: sfx_t, inrate: Int, inwidth: Int, data: ByteArray, offset: Int) {
            val outcount: Int
            var srcsample: Int
            var i: Int
            var sample: Int
            var samplefrac: Int
            val fracstep: Int
            val sc: sfxcache_t?

            sc = sfx.cache

            if (sc == null)
                return

            // again calculate the stretching factor.
            // this is usually 0.5, 1, or 2

            val stepscale: Float
            if (DONT_DO_A_RESAMPLING_FOR_JOAL_AND_LWJGL)
                stepscale = 1
            else
                stepscale = inrate.toFloat() / S.getDefaultSampleRate()
            outcount = (sc!!.length / stepscale) as Int
            sc!!.length = outcount

            if (sc!!.loopstart != -1)
                sc!!.loopstart = (sc!!.loopstart / stepscale) as Int

            // if resampled, sample has now the default sample rate
            if (DONT_DO_A_RESAMPLING_FOR_JOAL_AND_LWJGL == false)
                sc!!.speed = S.getDefaultSampleRate()

            sc!!.width = inwidth
            sc!!.stereo = 0
            samplefrac = 0
            fracstep = (stepscale * 256).toInt()

            run {
                i = 0
                while (i < outcount) {
                    srcsample = samplefrac shr 8
                    samplefrac += fracstep

                    if (inwidth == 2) {
                        sample = (data[offset + srcsample * 2] and 255) + (data[offset + srcsample * 2 + 1].toInt() shl 8)
                    } else {
                        sample = ((data[offset + srcsample] and 255) - 128) shl 8
                    }

                    if (sc!!.width == 2) {
                        if (Defines.LITTLE_ENDIAN) {
                            sc!!.data[i * 2] = (sample and 255).toByte()
                            sc!!.data[i * 2 + 1] = ((sample.ushr(8)) and 255).toByte()
                        } else {
                            sc!!.data[i * 2] = ((sample.ushr(8)) and 255).toByte()
                            sc!!.data[i * 2 + 1] = (sample and 255).toByte()
                        }
                    } else {
                        sc!!.data[i] = (sample shr 8).toByte()
                    }
                    i++
                }
            }
        }


        var data_b: ByteArray
        var data_p: Int = 0
        var iff_end: Int = 0
        var last_chunk: Int = 0
        var iff_data: Int = 0
        var iff_chunk_len: Int = 0


        fun GetLittleShort(): Short {
            var `val` = 0
            `val` = data_b[data_p] and 255
            data_p++
            `val` = `val` or ((data_b[data_p] and 255) shl 8)
            data_p++
            return `val`.toShort()
        }

        fun GetLittleLong(): Int {
            var `val` = 0
            `val` = data_b[data_p] and 255
            data_p++
            `val` = `val` or ((data_b[data_p] and 255) shl 8)
            data_p++
            `val` = `val` or ((data_b[data_p] and 255) shl 16)
            data_p++
            `val` = `val` or ((data_b[data_p] and 255) shl 24)
            data_p++
            return `val`
        }

        fun FindNextChunk(name: String) {
            while (true) {
                data_p = last_chunk

                if (data_p >= iff_end) {
                    // didn't find the chunk
                    data_p = 0
                    return
                }

                data_p += 4

                iff_chunk_len = GetLittleLong()

                if (iff_chunk_len < 0) {
                    data_p = 0
                    return
                }
                if (iff_chunk_len > 1024 * 1024) {
                    Com.Println(" Warning: FindNextChunk: length is past the 1 meg sanity limit")
                }
                data_p -= 8
                last_chunk = data_p + 8 + ((iff_chunk_len + 1) and 1.inv())
                val s = String(data_b, data_p, 4)
                if (s.equals(name))
                    return
            }
        }

        fun FindChunk(name: String) {
            last_chunk = iff_data
            FindNextChunk(name)
        }

        /*
	============
	GetWavinfo
	============
	*/
        fun GetWavinfo(name: String, wav: ByteArray?, wavlength: Int): wavinfo_t {
            val info = wavinfo_t()
            val i: Int
            val format: Int
            val samples: Int

            if (wav == null)
                return info

            iff_data = 0
            iff_end = wavlength
            data_b = wav

            // find "RIFF" chunk
            FindChunk("RIFF")
            var s = String(data_b, data_p + 8, 4)
            if (!s.equals("WAVE")) {
                Com.Printf("Missing RIFF/WAVE chunks\n")
                return info
            }

            //	   get "fmt " chunk
            iff_data = data_p + 12
            //	   DumpChunks ();

            FindChunk("fmt ")
            if (data_p == 0) {
                Com.Printf("Missing fmt chunk\n")
                return info
            }
            data_p += 8
            format = GetLittleShort().toInt()
            if (format != 1) {
                Com.Printf("Microsoft PCM format only\n")
                return info
            }

            info.channels = GetLittleShort().toInt()
            info.rate = GetLittleLong()
            data_p += 4 + 2
            info.width = GetLittleShort().toInt() / 8

            //	   get cue chunk
            FindChunk("cue ")
            if (data_p != 0) {
                data_p += 32
                info.loopstart = GetLittleLong()
                //			Com_Printf("loopstart=%d\n", sfx->loopstart);

                // if the next chunk is a LIST chunk, look for a cue length marker
                FindNextChunk("LIST")
                if (data_p != 0) {
                    if (data_b.size() >= data_p + 32) {
                        s = String(data_b, data_p + 28, 4)
                        if (s.equals("MARK")) {
                            // this is not a proper parse, but
                            // it works with cooledit...
                            data_p += 24
                            i = GetLittleLong() // samples in loop
                            info.samples = info.loopstart + i
                            //					Com_Printf("looped length: %i\n", i);
                        }
                    }
                }
            } else
                info.loopstart = -1

            //	   find data chunk
            FindChunk("data")
            if (data_p == 0) {
                Com.Printf("Missing data chunk\n")
                return info
            }

            data_p += 4
            samples = GetLittleLong() / info.width

            if (info.samples != 0) {
                if (samples < info.samples)
                    Com.Error(Defines.ERR_DROP, "Sound " + name + " has a bad loop length")
            } else {
                info.samples = samples
                if (info.loopstart > 0) info.samples -= info.loopstart
            }

            info.dataofs = data_p

            return info
        }
    }
}
