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

import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.LinkedList

/**
 * InputListener
 */
public class InputListener : KeyListener, MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener {

    override fun keyPressed(e: KeyEvent) {
        if (!((e.getModifiersEx() and InputEvent.ALT_GRAPH_DOWN_MASK) != 0)) {
            addEvent(LWJake2InputEvent(LWJake2InputEvent.KeyPress, e))
        }
    }

    override fun keyReleased(e: KeyEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.KeyRelease, e))
    }

    override fun keyTyped(e: KeyEvent) {
        if ((e.getModifiersEx() and InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            addEvent(LWJake2InputEvent(LWJake2InputEvent.KeyPress, e))
            addEvent(LWJake2InputEvent(LWJake2InputEvent.KeyRelease, e))
        }
    }

    override fun mouseClicked(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent) {
    }

    override fun mouseExited(e: MouseEvent) {
    }

    override fun mousePressed(e: MouseEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.ButtonPress, e))
    }

    override fun mouseReleased(e: MouseEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.ButtonRelease, e))
    }

    override fun mouseDragged(e: MouseEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.MotionNotify, e))
    }

    override fun mouseMoved(e: MouseEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.MotionNotify, e))
    }

    override fun componentHidden(e: ComponentEvent) {
    }

    override fun componentMoved(e: ComponentEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.ConfigureNotify, e))
    }

    override fun componentResized(e: ComponentEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.ConfigureNotify, e))
    }

    override fun componentShown(e: ComponentEvent) {
        JOGLKBD.c = e.getComponent()
        addEvent(LWJake2InputEvent(LWJake2InputEvent.CreateNotify, e))
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        addEvent(LWJake2InputEvent(LWJake2InputEvent.WheelMoved, e))
    }

    companion object {

        // modifications of eventQueue must be thread safe!
        private val eventQueue = LinkedList<LWJake2InputEvent>()

        fun addEvent(ev: LWJake2InputEvent) {
            synchronized (eventQueue) {
                eventQueue.addLast(ev)
            }
        }

        fun nextEvent(): LWJake2InputEvent {
            var ev: LWJake2InputEvent?
            synchronized (eventQueue) {
                ev = if ((!eventQueue.isEmpty())) eventQueue.removeFirst() as LWJake2InputEvent else null
            }
            return ev
        }
    }

}

