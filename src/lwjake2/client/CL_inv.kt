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
import lwjake2.qcommon.Com
import lwjake2.qcommon.MSG
import lwjake2.util.Lib
import lwjake2.util.Vargs

/**
 * CL_inv
 */
public class CL_inv {
    companion object {

        /*
	 * ================ CL_ParseInventory ================
	 */
        fun ParseInventory() {
            var i: Int

            run {
                i = 0
                while (i < Defines.MAX_ITEMS) {
                    Globals.cl.inventory[i] = MSG.ReadShort(Globals.net_message)
                    i++
                }
            }
        }

        /*
	 * ================ Inv_DrawString ================
	 */
        fun Inv_DrawString(x: Int, y: Int, string: String) {
            var x = x
            for (i in 0..string.length() - 1) {
                Globals.re.DrawChar(x, y, string.charAt(i))
                x += 8
            }
        }

        fun getHighBitString(s: String): String {
            val b = Lib.stringToBytes(s)
            for (i in b.indices) {
                b[i] = (b[i] or 128).toByte()
            }
            return Lib.bytesToString(b)
        }

        /*
	 * ================ CL_DrawInventory ================
	 */
        val DISPLAY_ITEMS = 17

        fun DrawInventory() {
            var i: Int
            var j: Int
            var num: Int
            var selected_num: Int
            var item: Int
            val index = IntArray(Defines.MAX_ITEMS)
            var string: String
            var x: Int
            var y: Int
            var binding: String
            var bind: String
            val selected: Int
            var top: Int

            selected = Globals.cl.frame.playerstate.stats[Defines.STAT_SELECTED_ITEM]

            num = 0
            selected_num = 0
            run {
                i = 0
                while (i < Defines.MAX_ITEMS) {
                    if (i == selected)
                        selected_num = num
                    if (Globals.cl.inventory[i] != 0) {
                        index[num] = i
                        num++
                    }
                    i++
                }
            }

            // determine scroll point
            top = selected_num - DISPLAY_ITEMS / 2
            if (num - top < DISPLAY_ITEMS)
                top = num - DISPLAY_ITEMS
            if (top < 0)
                top = 0

            x = (Globals.viddef.width - 256) / 2
            y = (Globals.viddef.height - 240) / 2

            // repaint everything next frame
            SCR.DirtyScreen()

            Globals.re.DrawPic(x, y + 8, "inventory")

            y += 24
            x += 24
            Inv_DrawString(x, y, "hotkey ### item")
            Inv_DrawString(x, y + 8, "------ --- ----")
            y += 16
            run {
                i = top
                while (i < num && i < top + DISPLAY_ITEMS) {
                    item = index[i]
                    // search for a binding
                    //Com_sprintf (binding, sizeof(binding), "use %s",
                    // cl.configstrings[CS_ITEMS+item]);
                    binding = "use " + Globals.cl.configstrings[Defines.CS_ITEMS + item]
                    bind = ""
                    run {
                        j = 0
                        while (j < 256) {
                            if (Globals.keybindings[j] != null && Globals.keybindings[j].equals(binding)) {
                                bind = Key.KeynumToString(j)
                                break
                            }
                            j++
                        }
                    }

                    string = Com.sprintf("%6s %3i %s", Vargs(3).add(bind).add(Globals.cl.inventory[item]).add(Globals.cl.configstrings[Defines.CS_ITEMS + item]))
                    if (item != selected)
                        string = getHighBitString(string)
                    else
                    // draw a blinky cursor by the selected item
                    {
                        if (((Globals.cls.realtime * 10) as Int and 1) != 0)
                            Globals.re.DrawChar(x - 8, y, 15)
                    }
                    Inv_DrawString(x, y, string)
                    y += 8
                    i++
                }
            }

        }
    }
}