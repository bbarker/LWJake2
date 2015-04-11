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

package lwjake2.sys

/**
 * KBD
 */
public abstract class KBD {

    public abstract fun Init()

    public abstract fun Update()

    public abstract fun Close()
    public abstract fun Do_Key_Event(key: Int, down: Boolean)

    public abstract fun installGrabs()
    public abstract fun uninstallGrabs()

    companion object {

        var win_x = 0
        var win_y = 0

        // motion values
        public var mx: Int = 0
        public var my: Int = 0
    }
    //abstract public void centerMouse();
}

