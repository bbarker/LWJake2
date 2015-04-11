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

package lwjake2.qcommon

public class CRC {
	companion object {

		public val CRC_INIT_VALUE: Short = 65535.toShort()
		public val CRC_XOR_VALUE: Short = 0.toShort()

		private val crctable = intArray(0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302, 37689, 33560, 45947, 41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, 42346, 46411, 34088, 38153, 58862, 62927, 50604, 54669, 13907, 9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681, 34616, 63455, 59390, 55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789, 59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640, 14899, 10770, 56317, 52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879, 19684, 23749, 11298, 15363, 3168, 7233, 60846, 64911, 52716, 56781, 44330, 48395, 36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761, 3696, 65439, 61374, 57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453, 61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979, 46042, 49981, 54044, 58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270, 46570, 42443, 38312, 34185, 62830, 58703, 54572, 50445, 13538, 9411, 5280, 1153, 29798, 25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358, 50973, 55100, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368, 35305, 47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093, 56156, 60223, 64286, 35833, 39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801, 6864, 10931, 14994, 64814, 60687, 56684, 52557, 48554, 44427, 40424, 36297, 31782, 27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085, 57212, 44955, 49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920)

		fun CRC_Block(start: ByteArray, count: Int): Int {
			var count = count
			var crc = CRC_INIT_VALUE

			var ndx = 0

			while (count-- > 0)
				crc = ((crc.toInt() shl 8) xor crctable[255 and ((crc.toInt() shr 8) xor start[ndx++])]).toShort()

			// unsigned short
			return crc and 65535
		}

		public fun main(args: Array<String>) {
			val data = byteArray(113.toByte(), 169.toByte(), 5.toByte(), 206.toByte(), 141.toByte(), 117.toByte(), 40.toByte(), 200.toByte(), 186.toByte(), 151.toByte(),
					69.toByte(), 233.toByte(), 138.toByte(), 224.toByte(), 55.toByte(), 189.toByte(), 108.toByte(), 109.toByte(), 103.toByte(), 74.toByte(), 33.toByte())
			System.out.println("crc:" + (CRC_Block(data, 21) and 65535))
			System.out.println("----")
			for (n in 0..5 - 1)
				System.out.println("seq:" + (Com.BlockSequenceCRCByte(data, 0, 21, n * 10) and 255))

		}
	}

	/* c test:
 * 
 * D:\Rene\gamesrc\quake2-3.21\qcommon>crc
 * crc=-12353
 * ----
 * seq:215
 * seq:252
 * seq:164
 * seq:202
 * seq:201
 * 
int main()
{
    byte data[21] =
    {
                  0x71,
                  0xa9,
                  0x05,
                  0xce,
                  0x8d,
                  0x75,
                  0x28,
                  0xc8,
                  0xba,
                  0x97,

                  0x45,
                  0xe9,
                  0x8a,
                  0xe0,
                  0x37,
                  0xbd,
                  0x6c,
                  0x6d,
                  0x67,
                  0x4a, 0x21 };
    int n=0;

    printf("crc=%d\n", (short) CRC_Block(&data, 21));

    printf("----\n");
    for (n=0; n < 5; n++)
        printf("seq:%d\n", COM_BlockSequenceCRCByte( &data,21, n*10) );
} 
 */

}
