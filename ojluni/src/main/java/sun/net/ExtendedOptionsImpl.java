/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package sun.net;

import java.io.FileDescriptor;
import java.net.SocketOption;
import java.security.AccessController;
import java.security.PrivilegedAction;

import jdk.net.*;

/**
 * Contains the native implementation for extended socket options
 * together with some other static utilities
 */
public class ExtendedOptionsImpl {

    static {
        AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
            System.loadLibrary("net");
            return null;
        });
        init();
    }

    private ExtendedOptionsImpl() {}

    public static void checkSetOptionPermission(SocketOption<?> option) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        String check = "setOption." + option.name();
        sm.checkPermission(new NetworkPermission(check));
    }

    public static void checkGetOptionPermission(SocketOption<?> option) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        String check = "getOption." + option.name();
        sm.checkPermission(new NetworkPermission(check));
    }

    public static void checkValueType(Object value, Class<?> type) {
        if (!type.isAssignableFrom(value.getClass())) {
            String s = "Found: " + value.getClass().toString() + " Expected: "
                        + type.toString();
            throw new IllegalArgumentException(s);
        }
    }

    private static native void init();

    /*
     * Extension native implementations
     *
     * SO_FLOW_SLA
     */
    public static native void setFlowOption(FileDescriptor fd, SocketFlow f);
    public static native void getFlowOption(FileDescriptor fd, SocketFlow f);
    public static native boolean flowSupported();
}
