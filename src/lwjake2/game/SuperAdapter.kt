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

package lwjake2.game

import lwjake2.qcommon.Com

import java.util.Hashtable

public abstract class SuperAdapter {

    {
        register(this, getID())
    }

    /** Returns the Adapter-ID.  */
    public abstract fun getID(): String

    companion object {

        /** Adapter registration.  */
        private fun register(sa: SuperAdapter, id: String) {
            adapters.put(id, sa)
        }

        /** Adapter repository.  */
        private val adapters = Hashtable<String, SuperAdapter>()

        /** Returns the adapter from the repository given by its ID.  */
        public fun getFromID(key: String): SuperAdapter {
            val sa = adapters.get(key) as SuperAdapter

            // try to create the adapter
            if (sa == null) {
                Com.DPrintf("SuperAdapter.getFromID():adapter not found->" + key + "\n")
            }

            return sa
        }
    }
}
/** Constructor, does the adapter registration.  */
