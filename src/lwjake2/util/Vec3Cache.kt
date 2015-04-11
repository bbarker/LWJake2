/*
 * Copyright (C) 2003 Carsten "cwei" Weisse
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

package lwjake2.util

/**
 * Vec3Cache contains float[3] for temporary usage.
 * The usage can reduce the garbage at runtime.

 * @author cwei
 */
public class Vec3Cache {
    companion object {

        //private static Stack cache = new Stack();
        private val cache = Array<FloatArray>(64, { FloatArray(3) })
        private var index = 0
        private val max = 0

        public fun get(): FloatArray {
            //max = Math.max(index, max);
            return cache[index++]
        }

        public fun release() {
            index--
        }

        public fun release(count: Int) {
            index -= count
        }

        public fun debug() {
            System.err.println("Vec3Cache: max. " + (max + 1) + " vectors used.")
        }
    }
}