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

import java.security.BasicPermission;

/**
 * Represents permission to access the extended networking capabilities
 * defined in the jdk.net package. These permissions contain a target
 * name, but no actions list. Callers either possess the permission or not.
 * <p>
 * The following targets are defined:
 * <p>
 * <table border=1 cellpadding=5 summary="permission target name,
 *  what the target allows,and associated risks">
 * <tr>
 *   <th>Permission Target Name</th>
 *   <th>What the Permission Allows</th>
 *   <th>Risks of Allowing this Permission</th>
 * </tr>
 * <tr>
 *   <td>setOption.SO_FLOW_SLA</td>
 *   <td>set the {@link ExtendedSocketOptions#SO_FLOW_SLA SO_FLOW_SLA} option
 *       on any socket that supports it</td>
 *   <td>allows caller to set a higher priority or bandwidth allocation
 *       to sockets it creates, than they might otherwise be allowed.</td>
 * </tr>
 * <tr>
 *   <td>getOption.SO_FLOW_SLA</td>
 *   <td>retrieve the {@link ExtendedSocketOptions#SO_FLOW_SLA SO_FLOW_SLA}
 *       setting from any socket that supports the option</td>
 *   <td>allows caller access to SLA information that it might not
 *       otherwise have</td>
 * </tr></table>
 *
 * @see jdk.net.ExtendedSocketOptions
 */

// @jdk.Exported
public final class NetworkPermission extends BasicPermission {

    private static final long serialVersionUID = -2012939586906722291L;

    /**
     * Creates a NetworkPermission with the given target name.
     *
     * @param name the permission target name
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws IllegalArgumentException if {@code name} is empty.
     */
    public NetworkPermission(String name) {
        super(name);
    }

    /**
     * Creates a NetworkPermission with the given target name.
     *
     * @param name the permission target name
     * @param actions should be {@code null}. Is ignored if not.
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws IllegalArgumentException if {@code name} is empty.
     */
    public NetworkPermission(String name, String actions) {
        super(name, actions);
    }
}
