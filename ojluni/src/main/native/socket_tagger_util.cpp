/*
 * Copyright 2016 Google Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Google designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Google in the LICENSE file that accompanied this code.
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
 */

#include "JNIHelp.h"

extern "C" {

int tagSocket(JNIEnv* env, int fd) {
    if (env->ExceptionOccurred()) { return fd; }
    jclass handlerCls = env->FindClass("dalvik/system/SocketTagger");
    jmethodID get = env->GetStaticMethodID(handlerCls, "get", "()Ldalvik/system/SocketTagger;");
    jobject socketTaggerInstance = env->CallStaticObjectMethod(handlerCls, get);
    jobject fileDescriptor = jniCreateFileDescriptor(env, fd);
    jmethodID tag = env->GetMethodID(handlerCls, "tag", "(Ljava/io/FileDescriptor;)V");
    env->CallVoidMethod(socketTaggerInstance, tag, fileDescriptor);
    return fd;
}

void untagSocket(JNIEnv* env, int fd) {
    if (env->ExceptionOccurred()) { return; }
    jclass handlerCls = env->FindClass("dalvik/system/SocketTagger");
    jmethodID get = env->GetStaticMethodID(handlerCls, "get", "()Ldalvik/system/SocketTagger;");
    jobject socketTaggerInstance = env->CallStaticObjectMethod(handlerCls, get);
    jobject fileDescriptor = jniCreateFileDescriptor(env, fd);
    jmethodID untag = env->GetMethodID(handlerCls, "untag", "(Ljava/io/FileDescriptor;)V");
    env->CallVoidMethod(socketTaggerInstance, untag, fileDescriptor);
}

}
