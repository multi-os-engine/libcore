/*
 * Copyright (c) 2001, 2010, Oracle and/or its affiliates. All rights reserved.
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

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include <sys/mman.h>
#include <stddef.h>
#include <stdlib.h>

#include "JNIHelp.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(Java_java_nio_ ## className ## _ ## functionName) }

JNIEXPORT jboolean JNICALL
Java_java_nio_MappedByteBuffer_isLoaded0(JNIEnv *env, jobject obj, jlong address,
                                         jlong len, jint numPages)
{
// MOE TODO: the second branch was copied from the Windows
// version of OpenJDK, check whether this is valid.
#ifndef MOE_WINDOWS
    jboolean loaded = JNI_TRUE;
    int result = 0;
    int i = 0;
    void *a = (void *) jlong_to_ptr(address);
#ifdef __linux__
    unsigned char *vec = (unsigned char *)malloc(numPages * sizeof(char));
#else
    char *vec = (char *)malloc(numPages * sizeof(char));
#endif

    if (vec == NULL) {
        JNU_ThrowOutOfMemoryError(env, NULL);
        return JNI_FALSE;
    }

    result = mincore(a, (size_t)len, vec);
    if (result == -1) {
        JNU_ThrowIOExceptionWithLastError(env, "mincore failed");
        free(vec);
        return JNI_FALSE;
    }

    for (i=0; i<numPages; i++) {
        if (vec[i] == 0) {
            loaded = JNI_FALSE;
            break;
        }
    }
    free(vec);
    return loaded;
#else
    jboolean loaded = JNI_FALSE;
    /* Information not available?
    MEMORY_BASIC_INFORMATION info;
    void *a = (void *) jlong_to_ptr(address);
    int result = VirtualQuery(a, &info, (DWORD)len);
    */
    return loaded;
#endif
}


JNIEXPORT void JNICALL
Java_java_nio_MappedByteBuffer_load0(JNIEnv *env, jobject obj, jlong address,
                                     jlong len)
{
// MOE TODO: the second branch was copied from the Windows
// version of OpenJDK, check whether this is valid.
#ifndef MOE_WINDOWS
    char *a = (char *)jlong_to_ptr(address);
    int result = madvise((caddr_t)a, (size_t)len, MADV_WILLNEED);
    if (result == -1) {
        JNU_ThrowIOExceptionWithLastError(env, "madvise failed");
    }
#else
    // no madvise available
#endif
}


JNIEXPORT void JNICALL
Java_java_nio_MappedByteBuffer_force0(JNIEnv *env, jobject obj, jobject fdo,
                                      jlong address, jlong len)
{
// MOE TODO: the second branch was copied from the Windows
// version of OpenJDK, check whether this is valid.
#ifndef MOE_WINDOWS
    void* a = (void *)jlong_to_ptr(address);
    int result = msync(a, (size_t)len, MS_SYNC);
    if (result == -1) {
        JNU_ThrowIOExceptionWithLastError(env, "msync failed");
    }
#else
    void *a = (void *) jlong_to_ptr(address);
    BOOL result;
    int retry;

    /*
     * FlushViewOfFile can fail with ERROR_LOCK_VIOLATION if the memory
     * system is writing dirty pages to disk. As there is no way to
     * synchronize the flushing then we retry a limited number of times.
     */
    retry = 0;
    do {
        result = FlushViewOfFile(a, (DWORD)len);
        if ((result != 0) || (GetLastError() != ERROR_LOCK_VIOLATION))
            break;
        retry++;
    } while (retry < 3);

    /**
     * FlushViewOfFile only initiates the writing of dirty pages to disk
     * so we have to call FlushFileBuffers to and ensure they are written.
     */
    if (result != 0) {
        // by right, the jfieldID initialization should be in a static
        // initializer but we do it here instead to avoiding needing to
        // load nio.dll during startup.
        static jfieldID handle_fdID;
        HANDLE h;
        if (handle_fdID == NULL) {
            jclass clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
            if (clazz == NULL)
                return; // exception thrown
            handle_fdID = (*env)->GetFieldID(env, clazz, "handle", "J");
        }
        h = jlong_to_ptr((*env)->GetLongField(env, fdo, handle_fdID));
        result = FlushFileBuffers(h);
        if (result == 0 && GetLastError() == ERROR_ACCESS_DENIED) {
            // read-only mapping
            result = 1;
        }
    }

    if (result == 0) {
        JNU_ThrowIOExceptionWithLastError(env, "Flush failed");
    }
#endif
}


static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(MappedByteBuffer, isLoaded0, "(JJI)Z"),
  NATIVE_METHOD(MappedByteBuffer, load0, "(JJ)V"),
  NATIVE_METHOD(MappedByteBuffer, force0, "(Ljava/io/FileDescriptor;JJ)V"),
};

void register_java_nio_MappedByteBuffer(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/nio/MappedByteBuffer", gMethods, NELEM(gMethods));
}
