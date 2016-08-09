/*
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
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

#include <sys/types.h>
#include <string.h>
#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include "nio_util.h"
#include "JNIHelp.h"

// MOE: The methods of the Windows version of NativeThread Java class
// don't do anything and instead of using a different Java source for
// Windows we simply achieve the same behaviour by modifying the native
// implementations here.

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

#ifndef MOE_WINDOWS

#include <pthread.h>
#include <sys/signal.h>
#ifdef MOE
#include <signal.h>
#endif

/* Also defined in src/solaris/native/java/net/linux_close.c */
#ifndef MOE
#define INTERRUPT_SIGNAL (__SIGRTMAX - 2)
#else
#define INTERRUPT_SIGNAL SIGUSR1
#endif

static void
nullHandler(int sig)
{
}

static void  NativeThread_init(JNIEnv *env)
{

    /* Install the null handler for INTERRUPT_SIGNAL.  This might overwrite the
     * handler previously installed by java/net/linux_close.c, but that's okay
     * since neither handler actually does anything.  We install our own
     * handler here simply out of paranoia; ultimately the two mechanisms
     * should somehow be unified, perhaps within the VM.
     */

    sigset_t ss;
    struct sigaction sa, osa;
    sa.sa_handler = nullHandler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    if (sigaction(INTERRUPT_SIGNAL, &sa, &osa) < 0)
        JNU_ThrowIOExceptionWithLastError(env, "sigaction");

}

JNIEXPORT jlong JNICALL
NativeThread_current(JNIEnv *env, jclass cl)
{
    return (long)pthread_self();
}

JNIEXPORT void JNICALL
NativeThread_signal(JNIEnv *env, jclass cl, jlong thread)
{
    if (pthread_kill((pthread_t)thread, INTERRUPT_SIGNAL))
        JNU_ThrowIOExceptionWithLastError(env, "Thread signal failed");
}
#else
JNIEXPORT void JNICALL
NativeThread_init(JNIEnv *env, jclass cl)
{
    return;
}

JNIEXPORT jlong JNICALL
NativeThread_current(JNIEnv *env, jclass cl)
{
    return 0;
}

JNIEXPORT void JNICALL
NativeThread_signal(JNIEnv *env, jclass cl, jlong thread)
{
    return;
}
#endif

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(NativeThread, current, "()J"),
  NATIVE_METHOD(NativeThread, signal, "(J)V"),
};

void register_sun_nio_ch_NativeThread(JNIEnv* env) {
  jniRegisterNativeMethods(env, "sun/nio/ch/NativeThread", gMethods, NELEM(gMethods));
  NativeThread_init(env);
}
