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

package lwjake2.render

import lwjake2.client.refexport_t

import java.util.Vector

/**
 * Renderer

 * @author cwei
 */
public class Renderer {
    companion object {

        var drivers = Vector<Ref>(1)

        {
            try {
                try {
                    Class.forName("org.lwjgl.opengl.GL11")
                    Class.forName("lwjake2.render.LWJGLRenderer")
                } catch (e: ClassNotFoundException) {
                    // ignore the lwjgl driver if runtime not in classpath
                }

            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }

        public fun register(impl: Ref?) {
            if (impl == null) {
                throw IllegalArgumentException("Ref implementation can't be null")
            }
            if (!drivers.contains(impl)) {
                drivers.add(impl)
            }
        }

        /**
         * Factory method to get the Renderer implementation.
         * @return refexport_t (Renderer singleton)
         */
        public fun getDriver(driverName: String): refexport_t? {
            // find a driver
            var driver: Ref? = null
            val count = drivers.size()
            for (i in 0..count - 1) {
                driver = drivers.get(i)
                if (driver!!.getName().equals(driverName)) {
                    return driver!!.GetRefAPI()
                }
            }
            // null if driver not found
            return null
        }

        public fun getDefaultName(): String {
            return if ((drivers.isEmpty())) null else (drivers.firstElement()).getName()
        }

        public fun getPreferedName(): String {
            return if ((drivers.isEmpty())) null else (drivers.lastElement()).getName()
        }

        public fun getDriverNames(): Array<String>? {
            if (drivers.isEmpty()) return null
            val count = drivers.size()
            val names = arrayOfNulls<String>(count)
            for (i in 0..count - 1) {
                names[i] = (drivers.get(i)).getName()
            }
            return names
        }
    }
}