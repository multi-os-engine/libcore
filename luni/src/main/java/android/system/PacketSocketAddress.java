/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.system;

import libcore.util.Objects;
import java.net.SocketAddress;

/**
 * Packet socket address.
 *
 * Corresponds to Linux's {@code struct sockaddr_ll}.
 *
 * @hide
 */
public final class PacketSocketAddress extends SocketAddress {
    /** Protocol. The standard Ethernet protocol type. */
    private final short protocol;

    /** Interface index. */
    private final int ifindex;

    /** ARP hardware type. One of the {@code ARPHRD_*} constants. */
    private final short hatype;

    /** Packet type. One of the {@code PACKET_*} constants, such as {@code PACKET_OTHERHOST}. */
    private final byte pkttype;

    /** Hardware address. */
    private final byte[] addr;

    /** Constructs a new PacketSocketAddress. */
    public PacketSocketAddress(
            short protocol, int ifindex, short hatype, byte pkttype, byte[] addr) {
        this.protocol = protocol;
        this.ifindex = ifindex;
        this.hatype = hatype;
        this.pkttype = pkttype;
        this.addr = addr;
    }

    /** Constructs a new PacketSocketAddress suitable for binding. */
    public PacketSocketAddress(short protocol, int ifindex) {
        this(protocol, ifindex, (short) 0, (byte) 0, null);
    }

    public short getProtocol() {
        return protocol;
    }

    public int getIfindex() {
        return ifindex;
    }

    public short getHatype() {
        return hatype;
    }

    public byte getPkttype() {
        return pkttype;
    }

    public byte[] getAddr() {
        return addr;
    }

    @Override
    public String toString() {
      return Objects.toString(this);
    }
}
