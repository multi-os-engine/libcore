/*
 * Copyright (C) 2013 The Android Open Source Project
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

#define LOG_TAG "IcuUtilities"

#include "IcuUtilities.h"

#include "JniConstants.h"
#include "JniException.h"
#include "ScopedLocalRef.h"
#include "ScopedUtfChars.h"
#include "cutils/log.h"
#include "unicode/strenum.h"
#include "unicode/uloc.h"
#include "unicode/ustring.h"

#ifdef USE_APPLE_CF
#include <vector>
#include <string>
#include <cf_string.h>


jobjectArray fromStdVector(JNIEnv* env, UErrorCode& status, const std::vector<std::string> &  codes) {
    
    int32_t count = codes.size();
    
    jobjectArray result = env->NewObjectArray(count, JniConstants::stringClass, NULL);
    for (int32_t i = 0; i < count; ++i) {
        UnicodeString string = cf_String(codes[i]);

        ScopedLocalRef<jstring> javaString(env, env->NewString(string.getBuffer(), string.length()));
        env->SetObjectArrayElement(result, i, javaString.get());
    }
    return result;
}
#endif

jobjectArray fromStringEnumeration(JNIEnv* env, UErrorCode& status, const char* provider, StringEnumeration* se) {
  if (maybeThrowIcuException(env, provider, status)) {
    return NULL;
  }

  int32_t count = se->count(status);
  if (maybeThrowIcuException(env, "StringEnumeration::count", status)) {
    return NULL;
  }

  jobjectArray result = env->NewObjectArray(count, JniConstants::stringClass, NULL);
  for (int32_t i = 0; i < count; ++i) {
    const icu::UnicodeString* string = se->snext(status);
    if (maybeThrowIcuException(env, "StringEnumeration::snext", status)) {
      return NULL;
    }
    ScopedLocalRef<jstring> javaString(env, env->NewString(string->getBuffer(), string->length()));
    env->SetObjectArrayElement(result, i, javaString.get());
  }
  return result;
}

bool maybeThrowIcuException(JNIEnv* env, const char* function, UErrorCode error) {
  if (U_SUCCESS(error)) {
    return false;
  }
  const char* exceptionClass = "java/lang/RuntimeException";
  if (error == U_ILLEGAL_ARGUMENT_ERROR) {
    exceptionClass = "java/lang/IllegalArgumentException";
  } else if (error == U_INDEX_OUTOFBOUNDS_ERROR || error == U_BUFFER_OVERFLOW_ERROR) {
    exceptionClass = "java/lang/ArrayIndexOutOfBoundsException";
  } else if (error == U_UNSUPPORTED_ERROR) {
    exceptionClass = "java/lang/UnsupportedOperationException";
  } else if (error == U_FORMAT_INEXACT_ERROR) {
    exceptionClass = "java/lang/ArithmeticException";
  }
  jniThrowExceptionFmt(env, exceptionClass, "%s failed: %s", function, u_errorName(error));
  return true;
}
