/* This file was generated from sun/nio/ch/Net.java and is licensed under the
 * same terms. The copyright and license information for sun/nio/ch/Net.java
 * follows.
 *
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class sun_nio_ch_PollArrayWrapper */

#ifndef _Included_sun_nio_ch_PollArrayWrapper
#define _Included_sun_nio_ch_PollArrayWrapper
#ifdef __cplusplus
extern "C" {
#endif
#undef sun_nio_ch_PollArrayWrapper_SIZE_POLLFD
#define sun_nio_ch_PollArrayWrapper_SIZE_POLLFD 8L
#undef sun_nio_ch_PollArrayWrapper_FD_OFFSET
#define sun_nio_ch_PollArrayWrapper_FD_OFFSET 0L
#undef sun_nio_ch_PollArrayWrapper_EVENT_OFFSET
#define sun_nio_ch_PollArrayWrapper_EVENT_OFFSET 4L
#undef sun_nio_ch_PollArrayWrapper_REVENT_OFFSET
#define sun_nio_ch_PollArrayWrapper_REVENT_OFFSET 6L
/*
 * Class:     sun_nio_ch_PollArrayWrapper
 * Method:    poll0
 * Signature: (JIJ)I
 */
JNIEXPORT jint JNICALL Java_sun_nio_ch_PollArrayWrapper_poll0
  (JNIEnv *, jobject, jlong, jint, jlong);

/*
 * Class:     sun_nio_ch_PollArrayWrapper
 * Method:    interrupt
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_sun_nio_ch_PollArrayWrapper_interrupt
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
