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

#define LOG_TAG "RelativeDateTimeFormatter"

#include "IcuUtilities.h"
#include "JniConstants.h"
#include "ScopedIcuLocale.h"
#include "ScopedJavaUnicodeString.h"
#include "cutils/log.h"
#include "unicode/reldatefmt.h"

static jlong RelativeDateTimeFormatter_createRelativeDateTimeFormatter(JNIEnv* env, jclass, jstring javaLocaleName, jint style) {
  ScopedIcuLocale icuLocale(env, javaLocaleName);
  if (!icuLocale.valid()) {
    return 0;
  }

  UErrorCode status = U_ZERO_ERROR;
  RelativeDateTimeFormatter* formatter = 
      new RelativeDateTimeFormatter(icuLocale.locale(), nullptr, (UDateRelativeDateTimeFormatterStyle)style,
                                    UDISPCTX_CAPITALIZATION_NONE, status);

  if (maybeThrowIcuException(env, "RelativeDateTimeFormatter::RelativeDateTimeFormatter", status)) {
    return 0;
  }

  return reinterpret_cast<uintptr_t>(formatter);
}

static void RelativeDateTimeFormatter_destroyRelativeDateTimeFormatter(JNIEnv*, jclass, jlong address) {
  delete reinterpret_cast<RelativeDateTimeFormatter*>(address);
}

static jstring RelativeDateTimeFormatter_formatWithRelativeUnit(JNIEnv* env, jclass, jlong address, jint quantity, jint direction, jint unit) {
  RelativeDateTimeFormatter* formatter(reinterpret_cast<RelativeDateTimeFormatter*>(address));
  UnicodeString s;
  UErrorCode status = U_ZERO_ERROR;
  // RelativeDateTimeFormatter::format() takes a double-type quantity.
  formatter->format((double)quantity, (UDateDirection)direction, (UDateRelativeUnit)unit, s, status);

  if (maybeThrowIcuException(env, "RelativeDateTimeFormatter::format", status)) {
    return NULL;
  }

  return env->NewString(s.getBuffer(), s.length());
}

static jstring RelativeDateTimeFormatter_formatWithAbsoluteUnit(JNIEnv* env, jclass, jlong address, jint direction, jint unit) {
  RelativeDateTimeFormatter* formatter(reinterpret_cast<RelativeDateTimeFormatter*>(address));
  UnicodeString s;
  UErrorCode status = U_ZERO_ERROR;
  formatter->format((UDateDirection)direction, (UDateAbsoluteUnit)unit, s, status);

  if (maybeThrowIcuException(env, "RelativeDateTimeFormatter::format", status)) {
    return NULL;
  }

  return env->NewString(s.getBuffer(), s.length());
}

static jstring RelativeDateTimeFormatter_combineDateAndTime(JNIEnv* env, jclass, jlong address, jstring relativeDateString0, jstring timeString0) {
  RelativeDateTimeFormatter* formatter(reinterpret_cast<RelativeDateTimeFormatter*>(address));
  ScopedJavaUnicodeString relativeDateString(env, relativeDateString0);
  ScopedJavaUnicodeString timeString(env, timeString0);
  UnicodeString s;
  UErrorCode status = U_ZERO_ERROR;
  formatter->combineDateAndTime(relativeDateString.unicodeString(), timeString.unicodeString(), s, status);

  if (maybeThrowIcuException(env, "RelativeDateTimeFormatter::combineDateAndTime", status)) {
    return NULL;
  }

  return env->NewString(s.getBuffer(), s.length());
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(RelativeDateTimeFormatter, createRelativeDateTimeFormatter,
                "(Ljava/lang/String;I)J"),
  NATIVE_METHOD(RelativeDateTimeFormatter, destroyRelativeDateTimeFormatter, "(J)V"),
  NATIVE_METHOD(RelativeDateTimeFormatter, formatWithRelativeUnit,
                "(JIII)Ljava/lang/String;"),
  NATIVE_METHOD(RelativeDateTimeFormatter, formatWithAbsoluteUnit,
                "(JII)Ljava/lang/String;"),
  NATIVE_METHOD(RelativeDateTimeFormatter, combineDateAndTime,
                "(JLjava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
};

void register_libcore_icu_RelativeDateTimeFormatter(JNIEnv* env) {
  jniRegisterNativeMethods(env, "libcore/icu/RelativeDateTimeFormatter", gMethods, NELEM(gMethods));
}
