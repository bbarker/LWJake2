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

import lwjake2.Defines

public class netchan_t {

    public var fatal_error: Boolean = false

    // was enum {NS_CLIENT, NS_SERVER}
    public var sock: Int = 0

    public var dropped: Int = 0 // between last packet and previous

    public var last_received: Int = 0 // for timeouts

    public var last_sent: Int = 0 // for retransmits

    public var remote_address: netadr_t = netadr_t()

    public var qport: Int = 0 // qport value to write when transmitting

    // sequencing variables
    public var incoming_sequence: Int = 0

    public var incoming_acknowledged: Int = 0

    public var incoming_reliable_acknowledged: Int = 0 // single bit

    public var incoming_reliable_sequence: Int = 0 // single bit, maintained local

    public var outgoing_sequence: Int = 0

    public var reliable_sequence: Int = 0 // single bit

    public var last_reliable_sequence: Int = 0 // sequence number of last send

    //	   reliable staging and holding areas
    public var message: sizebuf_t = sizebuf_t() // writing buffer to send to
    // server

    public var message_buf: ByteArray = ByteArray(Defines.MAX_MSGLEN - 16) // leave
    // space for
    // header

    //	   message is copied to this buffer when it is first transfered
    public var reliable_length: Int = 0

    public var reliable_buf: ByteArray = ByteArray(Defines.MAX_MSGLEN - 16) // unpcked
    // reliable
    // message

    //ok.
    public fun clear() {
        sock = dropped = last_received = last_sent = 0
        remote_address = netadr_t()
        qport = incoming_sequence = incoming_acknowledged = incoming_reliable_acknowledged = incoming_reliable_sequence = outgoing_sequence = reliable_sequence = last_reliable_sequence = 0
        message = sizebuf_t()

        message_buf = ByteArray(Defines.MAX_MSGLEN - 16)

        reliable_length = 0
        reliable_buf = ByteArray(Defines.MAX_MSGLEN - 16)
    }

}