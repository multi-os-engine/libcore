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

#include <memory>
#include <string>

#include <pthread.h>
#include <sys/prctl.h>

#include <jni.h>
#include "JNIHelp.h"

static JavaVM* javaVm = nullptr;

static void* AttachAndReturnName(void* /* arg */) {
    const std::string expected_thread_name = "foozball";
    pthread_setname_np(pthread_self(), expected_thread_name.c_str());

    JNIEnv* env;
    if (javaVm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
        jniThrowException(env, "java/lang/IllegalStateException", "Attach failed");
        return nullptr;
    }

    std::unique_ptr<char[]> thread_name(new char[32]);
    if (prctl(PR_GET_NAME, reinterpret_cast<unsigned long>(thread_name.get()), 0L, 0L, 0L) != 0) {
        std::string exception_message("prctl(PR_GET_NAME) failed :");
        exception_message += strerror(errno);
        jniThrowException(env, "java/lang/IllegalStateException", exception_message.c_str());
        return nullptr;
    }

    if (thread_name.get() != expected_thread_name) {
        std::string exception_message("expected_thread_name != thread_name");
        exception_message += "expected :";
        exception_message += expected_thread_name;
        exception_message += " was :";
        exception_message += thread_name.get();
        jniThrowException(env, "java/lang/IllegalStateException", exception_message.c_str());
    }

    if (javaVm->DetachCurrentThread() != JNI_OK) {
        jniThrowException(env, "java/lang/IllegalStateException", "Attach failed");
        return nullptr;
    }

    return nullptr;
}

extern "C" void Java_libcore_java_lang_ThreadTest_testNativeThreadNames(
    JNIEnv* env, jobject /* object */) {
  pthread_t attacher;
  if (pthread_create(&attacher, nullptr, AttachAndReturnName, NULL) != 0) {
      jniThrowException(env, "java/lang/IllegalStateException", "Attach failed");
  }

  if (pthread_join(attacher, nullptr) != 0) {
      jniThrowException(env, "java/lang/IllegalStateException", "Join failed");
  }
}

extern "C" int JNI_OnLoad(JavaVM* vm, void*) {
    javaVm = vm;
    return JNI_VERSION_1_6;
}
