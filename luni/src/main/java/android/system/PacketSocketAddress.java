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
    /** Protocol. An Ethernet protocol type, e.g., {@code ETH_P_IPV6}. */
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
    public PacketSocketAddress(short protocol, int interfaceIndex,
            short hardwareType, byte packetType, byte[] address) {
        this.protocol = protocol;
        this.ifindex = interfaceIndex;
        this.hatype = hardwareType;
        this.pkttype = packetType;
        this.addr = address;
    }

    /** Constructs a new PacketSocketAddress suitable for binding to. */
    public PacketSocketAddress(short protocol, int interfaceIndex) {
        this(protocol, interfaceIndex, (short) 0, (byte) 0, null);
    }

    /** Constructs a new PacketSocketAddress suitable for sending to. */
    public PacketSocketAddress(int interfaceIndex, byte[] address) {
        this((short) 0, interfaceIndex, (short) 0, (byte) 0, address);
    }

    public short getProtocol() {
        return protocol;
    }

    public int getInterfaceIndex() {
        return ifindex;
    }

    public short getHardwareType() {
        return hatype;
    }

    public byte getPacketType() {
        return pkttype;
    }

    public byte[] getAddress() {
        return addr;
    }

    @Override
    public String toString() {
      return Objects.toString(this);
    }
}
