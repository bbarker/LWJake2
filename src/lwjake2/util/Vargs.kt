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

package lwjake2.util

import java.util.Vector

/**
 * Vargs is a helper class to encapsulate printf arguments.

 * @author cwei
 */
public class Vargs(initialSize: Int = Vargs.SIZE) {

    var v: Vector<Object>? = null

    {
        if (v != null)
            v!!.clear() // clear previous list for GC
        v = Vector<Object>(initialSize)
    }

    public fun add(value: Boolean): Vargs {
        v!!.add(Boolean(value))
        return this
    }

    public fun add(value: Byte): Vargs {
        v!!.add(Byte(value))
        return this
    }

    public fun add(value: Char): Vargs {
        v!!.add(Character(value))
        return this
    }

    public fun add(value: Short): Vargs {
        v!!.add(Short(value))
        return this
    }

    public fun add(value: Int): Vargs {
        v!!.add(Integer(value))
        return this
    }

    public fun add(value: Long): Vargs {
        v!!.add(Long(value))
        return this
    }

    public fun add(value: Float): Vargs {
        v!!.add(Float(value))
        return this
    }

    public fun add(value: Double): Vargs {
        v!!.add(Double(value))
        return this
    }

    public fun add(value: String): Vargs {
        v!!.add(value)
        return this
    }

    public fun add(value: Object): Vargs {
        v!!.add(value)
        return this
    }

    public fun clear(): Vargs {
        v!!.clear()
        return this
    }

    /* This apparently isn't even used? - flibit
	public Vector toVector() {
		//		Vector tmp = v;
		//		v = null;
		//		return tmp;
		return (Vector) v.clone();
	}
	*/

    public fun toArray(): Array<Object> {
        return v!!.toArray()
    }

    public fun size(): Int {
        return v!!.size()
    }

    companion object {

        // initial capacity
        val SIZE = 5
    }
}
