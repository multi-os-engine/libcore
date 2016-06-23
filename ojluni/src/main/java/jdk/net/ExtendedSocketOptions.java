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

package jdk.net;

import java.net.SocketOption;

/**
 * Defines extended socket options, beyond those defined in
 * {@link java.net.StandardSocketOptions}. These options may be platform
 * specific.
 */
// @jdk.Exported
public final class ExtendedSocketOptions {

    private static class ExtSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;
        ExtSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override
        public String name() { return name; }
        @Override
        public Class<T> type() { return type; }
        @Override
        public String toString() { return name; }
    }

    private ExtendedSocketOptions() {}

    /**
     * Service level properties. When a security manager is installed,
     * setting or getting this option requires a {@link NetworkPermission}
     * {@code ("setOption.SO_FLOW_SLA")} or {@code "getOption.SO_FLOW_SLA"}
     * respectively.
     */
    public static final SocketOption<SocketFlow> SO_FLOW_SLA = new
        ExtSocketOption<SocketFlow>("SO_FLOW_SLA", SocketFlow.class);
}
