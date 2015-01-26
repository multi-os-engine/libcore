/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * Netlink socket address.
 *
 * Corresponds to Linux's {@code struct sockaddr_nl} from
 * <a href="https://github.com/torvalds/linux/blob/master/include/uapi/linux/netlink.h">&lt;linux/netlink.h&gt;</a>.
 *
 * @hide
 */
public final class NetlinkSocketAddress extends SocketAddress {
    public final int nl_family = OsConstants.AF_NETLINK;

    /** port ID */
    public final int nl_pid;

    /** multicast groups mask */
    public final int nl_groups;

    public NetlinkSocketAddress() {
        this(0, 0);
    }

    public NetlinkSocketAddress(int nl_pid) {
        this(nl_pid, 0);
    }

    public NetlinkSocketAddress(int nl_pid, int nl_groups) {
        this.nl_pid = nl_pid;
        this.nl_groups = nl_groups;
    }

    @Override public String toString() {
      return Objects.toString(this);
    }
}
