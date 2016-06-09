/*
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
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
#include "jvm.h"

#include "JNIHelp.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

static jclass noSuchMethodErrCl;

static void ObjectStreamClass_initNative(JNIEnv *env)
{
    jclass cl = (*env)->FindClass(env, "java/lang/NoSuchMethodError");
    if (cl == NULL) {           /* exception thrown */
        return;
    }
    noSuchMethodErrCl = (*env)->NewGlobalRef(env, cl);
}

/*
 * Class:     java_io_ObjectStreamClass
 * Method:    hasStaticInitializer
 * Signature: (Ljava/lang/Class;)Z
 *
 * Returns true if the given class defines a <clinit>()V method; returns false
 * otherwise.
 */
JNIEXPORT jboolean JNICALL
ObjectStreamClass_hasStaticInitializer(JNIEnv *env, jclass this,
                                                    jclass clazz)
{
    jclass superCl = NULL;
    jmethodID superClinitId = NULL;
    jmethodID clinitId =
        (*env)->GetStaticMethodID(env, clazz, "<clinit>", "()V");
    if (clinitId == NULL) {     /* error thrown */
        jthrowable th = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);    /* normal return */
        if (!(*env)->IsInstanceOf(env, th, noSuchMethodErrCl)) {
            (*env)->Throw(env, th);
        }
        return JNI_FALSE;
    }

    // Android-changed, removed check for superclass clinitId != child clinitId.
    // While technicaly valid check, android has been always returning true
    // in this particular case.
    // We're returning true to enable deserializing classes without explicit serialVersionID
    // that would fail in this check (b/29064453).
    return JNI_TRUE;
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(ObjectStreamClass, hasStaticInitializer, "(Ljava/lang/Class;)Z"),
};

void register_java_io_ObjectStreamClass(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/io/ObjectStreamClass", gMethods, NELEM(gMethods));
  ObjectStreamClass_initNative(env);
}
