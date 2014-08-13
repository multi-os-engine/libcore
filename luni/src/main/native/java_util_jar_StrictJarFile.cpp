/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "StrictJarFile"

#include <string>

#include "JNIHelp.h"
#include "JniConstants.h"
#include "ScopedLocalRef.h"
#include "ScopedUtfChars.h"
#include "UniquePtr.h"
#include "jni.h"
#include "ziparchive/zip_archive.h"
#include "cutils/log.h"

struct ZipEntryNameDelete {
  void operator()(ZipEntryName* p) const {
    if (p != NULL) {
      delete[] p->name;
    }
    delete p;
  }
};

typedef UniquePtr<ZipEntryName, ZipEntryNameDelete> UniqueEntryNamePtr;

static void throwIoException(JNIEnv* env, const int32_t errorCode) {
  jniThrowException(env, "java/io/IOException", ErrorCodeString(errorCode));
}

static void throwOutOfMemoryError(JNIEnv* env) {
   jniThrowException(env, "java/lang/OutOfMemoryError", "");
}

static jobject newZipEntry(JNIEnv* env, const ZipEntry& entry, jstring entryName) {
  ScopedLocalRef<jclass> zipEntryClass(env, env->FindClass("java/util/zip/ZipEntry"));
  const jmethodID zipEntryCtor = env->GetMethodID(zipEntryClass.get(), "<init>",
                                   "(Ljava/lang/String;Ljava/lang/String;JJJIII[BJJ)V");

  return env->NewObject(zipEntryClass.get(),
                        zipEntryCtor,
                        entryName,
                        NULL,  // comment
                        static_cast<jlong>(entry.crc32),
                        static_cast<jlong>(entry.compressed_length),
                        static_cast<jlong>(entry.uncompressed_length),
                        static_cast<jint>(entry.method),
                        static_cast<jint>(0),  // time
                        static_cast<jint>(0),  // modData
                        NULL,  // byte[] extra
                        static_cast<jlong>(-1),  // local header offset
                        static_cast<jlong>(entry.offset));
}

static ZipEntryName* newZipEntryName(JNIEnv* env, ZipArchiveHandle handle, jstring name) {
  if (!env->EnsureLocalCapacity(3)) {
    return NULL;
  }
  ZipEntryName* result = NULL;
  ScopedLocalRef<jclass> string_class(env, env->FindClass("java/lang/String"));
  if (string_class.get() == NULL) {
    // ClassFormatError, ClassCircularityError, NoClassDefFoundError or OutOfMemoryError
    return NULL;
  }
  ScopedLocalRef<jstring> encoding(
      env,
      env->NewStringUTF(UsesUTF8ForNamesEncoding(handle) ? "UTF-8" : "Cp437"));
  jmethodID get_bytes_method_id = env->GetMethodID(string_class.get(), "getBytes",
                                                   "(Ljava/lang/String;)[B");
  if (get_bytes_method_id == NULL) {
    // NoSuchMethodError, ExceptionInInitializerError or OutOfMemoryError
    return NULL;
  }
  ScopedLocalRef<jbyteArray> bytes(
      env,
      reinterpret_cast<jbyteArray>(env->CallObjectMethod(name,
                                                         get_bytes_method_id, encoding.get())));
  if (!env->ExceptionCheck()) {
    // Exception from method
    result = new(std::nothrow) ZipEntryName;
    if (result == NULL) {
      throwOutOfMemoryError(env);
      return NULL;
    }
    result->name_length = env->GetArrayLength(bytes.get());
    result->name = new(std::nothrow) uint8_t[result->name_length];
    if (result->name == NULL) {
      delete result;
      throwOutOfMemoryError(env);
      return NULL;
    }
    env->GetByteArrayRegion(bytes.get(), 0, result->name_length,
                            reinterpret_cast<jbyte*>(const_cast<uint8_t*>(result->name)));
    if (env->ExceptionCheck()) {
      // ArrayIndexOutOfBoundsException
      delete[] result->name;
      delete result;
      return NULL;
    }
  }
  return result;
}

static jlong StrictJarFile_nativeOpenJarFile(JNIEnv* env, jobject, jstring fileName) {
  ScopedUtfChars fileChars(env, fileName);
  if (fileChars.c_str() == NULL) {
    return static_cast<jlong>(-1);
  }

  ZipArchiveHandle handle;
  int32_t error = OpenArchive(fileChars.c_str(), &handle);
  if (error) {
    throwIoException(env, error);
    return static_cast<jlong>(-1);
  }

  return reinterpret_cast<jlong>(handle);
}

class IterationHandle {
 public:
  IterationHandle() :
    cookie_(NULL) {
  }

  void** CookieAddress() {
    return &cookie_;
  }

  ~IterationHandle() {
    EndIteration(cookie_);
  }

 private:
  void* cookie_;
};


static jlong StrictJarFile_nativeStartIteration(JNIEnv* env, jobject, jlong nativeHandle,
                                                jstring prefix) {
  ZipArchiveHandle handle = reinterpret_cast<ZipArchiveHandle>(nativeHandle);
  UniqueEntryNamePtr prefix_name(newZipEntryName(env, handle, prefix));
  if (prefix_name.get() == NULL) {
    return static_cast<jlong>(-1);
  }

  IterationHandle* result = new IterationHandle();
  int32_t error = 0;
  if (prefix_name->name_length == 0) {
    error = StartIteration(handle, result->CookieAddress(), NULL);
  } else {
    error = StartIteration(handle, result->CookieAddress(), prefix_name.get());
  }

  if (error) {
    throwIoException(env, error);
    return static_cast<jlong>(-1);
  }

  return reinterpret_cast<jlong>(handle);
}

static jobject StrictJarFile_nativeNextEntry(JNIEnv* env, jobject, jlong iterationHandle) {
  IterationHandle* handle = reinterpret_cast<IterationHandle*>(iterationHandle);
  if (!env->EnsureLocalCapacity(3)) {
    delete handle;
    return NULL;
  }
  ScopedLocalRef<jclass> string_class(env, env->FindClass("java/lang/String"));
  if (string_class.get() == NULL) {
    // ClassFormatError, ClassCircularityError, NoClassDefFoundError or OutOfMemoryError
    delete handle;
    return NULL;
  }
  ScopedLocalRef<jstring> encoding(
      env,
      env->NewStringUTF(UsesUTF8ForNamesEncoding(handle) ? "UTF-8" : "Cp437"));
  jmethodID string_constructor_method_id = env->GetMethodID(string_class.get(), "<init>",
                                                   "([BLjava/lang/String;)V");
  if (string_constructor_method_id == NULL) {
    // NoSuchMethodError, ExceptionInInitializerError or OutOfMemoryError
    delete handle;
    return NULL;
  }

  ZipEntry data;
  ZipEntryName entryName;

  const int32_t error = Next(*handle->CookieAddress(), &data, &entryName);
  if (error) {
    delete handle;
    return NULL;
  }
  ScopedLocalRef<jbyteArray> bytes (env, env->NewByteArray(entryName.name_length));
  if (bytes.get() == NULL) {
    throwOutOfMemoryError(env);
    delete handle;
    return NULL;
  }
  env->SetByteArrayRegion(bytes.get(), 0, entryName.name_length,
                          reinterpret_cast<jbyte*>(const_cast<uint8_t*>(entryName.name)));
  if (env->ExceptionCheck()) {
    // ArrayIndexOutOfBoundsException
    delete handle;
    return NULL;
  }
  ScopedLocalRef<jstring> entryNameString(
      env,
      reinterpret_cast<jstring>(env->NewObject(string_class.get(),
                                              string_constructor_method_id,
                                              bytes.get(),
                                              encoding.get())));
  if (env->ExceptionCheck()) {
    delete handle;
    return NULL;
  }

  return newZipEntry(env, data, entryNameString.get());
}

static jobject StrictJarFile_nativeFindEntry(JNIEnv* env, jobject, jlong nativeHandle,
                                             jstring entryName) {
  ZipArchiveHandle handle = reinterpret_cast<ZipArchiveHandle>(nativeHandle);
  UniqueEntryNamePtr entry_name(newZipEntryName(env, handle, entryName));
  if (entry_name.get() == NULL) {
    return NULL;
  }

  ZipEntry data;
  const int32_t error = FindEntry(handle, *entry_name, &data);
  if (error) {
    return NULL;
  }

  return newZipEntry(env, data, entryName);
}

static void StrictJarFile_nativeClose(JNIEnv*, jobject, jlong nativeHandle) {
  CloseArchive(reinterpret_cast<ZipArchiveHandle>(nativeHandle));
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(StrictJarFile, nativeOpenJarFile, "(Ljava/lang/String;)J"),
  NATIVE_METHOD(StrictJarFile, nativeStartIteration, "(JLjava/lang/String;)J"),
  NATIVE_METHOD(StrictJarFile, nativeNextEntry, "(J)Ljava/util/zip/ZipEntry;"),
  NATIVE_METHOD(StrictJarFile, nativeFindEntry, "(JLjava/lang/String;)Ljava/util/zip/ZipEntry;"),
  NATIVE_METHOD(StrictJarFile, nativeClose, "(J)V"),
};

void register_java_util_jar_StrictJarFile(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/util/jar/StrictJarFile", gMethods, NELEM(gMethods));

}
