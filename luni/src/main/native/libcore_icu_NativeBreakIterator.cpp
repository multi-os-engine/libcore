/*
 * Copyright (C) 2006 The Android Open Source Project
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

#define LOG_TAG "NativeBreakIterator"

#include "IcuUtilities.h"
#include "JNIHelp.h"
#include "JniConstants.h"
#include "JniException.h"
#include "ScopedIcuLocale.h"
#include "ScopedUtfChars.h"
#include "unicode/brkiter.h"
#include "unicode/putil.h"
#include <stdlib.h>

// ICU documentation: http://icu-project.org/apiref/icu4c/classBreakIterator.html

// Buffer size (in UChar, aka. uint16_t) of our text window.
static const size_t kBufferSize = 1024;

static UText* Clone(UText*, const UText*, UBool, UErrorCode*);
static int64_t NativeLength(UText*);
static UBool Access(UText*, int64_t, UBool);
static void Close(UText*);

static struct UTextFuncs provider_funcs = {
  sizeof(UTextFuncs),
  0 /* reserved1 */,
  0 /* reserved2 */,
  0 /* reserved3 */,
  Clone,
  NativeLength,
  Access,
  nullptr /* extract */,
  nullptr /* replace */,
  nullptr /* copy */,
  nullptr /* mapOffsetToNative */,
  nullptr /* mapNativeIndexToUtf16 */,
  Close,
  nullptr /* spare1 */,
  nullptr /* spare2 */,
  nullptr /* spare3 */
};

static UText* Clone(UText* dest, const UText* source, UBool deep, UErrorCode* status) {
  // Don't support deep clones for now. This requires us to copy
  // the underlying string that we're "providing" and that would negate
  // most of the advantages of using this code.
  if (deep == TRUE) {
      abort();
  }

  if (U_FAILURE(*status)) {
    return 0;
  }

  UText* result = utext_setup(dest, sizeof(uint16_t) * kBufferSize, status);
  if (U_FAILURE(*status)) {
    return dest;
  }

  result->flags = source->flags;
  result->providerProperties = source->providerProperties;

  result->chunkNativeLimit = source->chunkNativeLimit;
  result->nativeIndexingLimit = source->nativeIndexingLimit;
  result->chunkNativeStart = source->chunkNativeStart;
  result->chunkOffset = source->chunkOffset;
  result->chunkLength = source->chunkLength;

  // Copy the pExtra field over from the source object. Note that pExtra
  // is equivalent to the chunk contents.
  //
  // TODO: Is this really necessary ? We're going to bear the cost of a 1kb
  // buffer copy every time we call Clone (which is every time we call
  // refreshInputText).
  //
  // TODO: This could just be source->chunkLength, which would bring the
  // for smaller strings.
  memcpy(result->pExtra, source->chunkContents, sizeof(uint16_t) * kBufferSize);
  result->chunkContents = (UChar*)result->pExtra;

  result->pFuncs = &provider_funcs;
  result->context = source->context;

  result->a = source->a;
  return result;
}

static int64_t NativeLength(UText* uText) {
  return uText->a;
}

static UBool Access(UText* uText, int64_t index, UBool forward) {
  const int64_t length = uText->a;

  if (forward) {
    // We've been asked for an index that's already inside our current
    // chunk. We update the offset and we're golden.
    if (index < uText->chunkNativeLimit && index >= uText->chunkNativeStart) {
      // TODO: Adjust this offset down to the start of a code point.
      uText->chunkOffset = static_cast<int32_t>(index - uText->chunkNativeStart);
      // U16_SET_CP_START();
      return TRUE;
    }

    // We've been asked for a chunk that's larger than the length of the string.
    // If our current chunk represents the end of the string, we're golden.
    if (index >= length && uText->chunkNativeLimit == length) {
      uText->chunkOffset = uText->chunkLength;
      return FALSE;
    }

    // We've been asked for an index that's "further ahead" in our string than
    // the current chunk. We pull down a chunk that starts at that index.
    uText->chunkNativeStart = index;
    uText->chunkNativeLimit = uText->chunkNativeStart + kBufferSize;
    // Clamp the chunk size down to the length of the string.
    if (uText->chunkNativeLimit > length) {
      uText->chunkNativeLimit = length;
    }

    uText->chunkOffset = 0;
  } else {
    if (index <= uText->chunkNativeLimit && index > uText->chunkNativeStart) {
      // TODO: Adjust this offset down to the start of a code point.
      uText->chunkOffset = (int32_t)(index - uText->chunkNativeStart);
      return TRUE;
    }

    if ((index == 0) && (uText->chunkNativeStart == 0)) {
      uText->chunkOffset = 0;
      return FALSE;
    }

    uText->chunkNativeLimit = index;
    if (uText->chunkNativeLimit > length) {
      uText->chunkNativeLimit = length;
    }

    uText->chunkNativeStart = uText->chunkNativeLimit - kBufferSize;
    if (uText->chunkNativeStart < 0) {
      uText->chunkNativeStart = 0;
    }

    uText->chunkOffset = uText->chunkLength;
  }


  uText->chunkLength = static_cast<size_t>(uText->chunkNativeLimit - uText->chunkNativeStart);

  const std::pair<JNIEnv*, jstring>* ctx =
      reinterpret_cast<const std::pair<JNIEnv*, jstring>*>(uText->context);
  JNIEnv* env = ctx->first;
  jstring str = ctx->second;
  env->GetStringRegion(str,
          static_cast<jsize>(uText->chunkNativeStart),
          static_cast<jsize>(uText->chunkLength),
          reinterpret_cast<jchar*>(const_cast<UChar*>(uText->chunkContents)));

  const UChar last = uText->chunkContents[uText->chunkLength - 1];
  if (U16_IS_LEAD(last)) {
      uText->chunkLength--;
      uText->chunkNativeLimit--;
      if (uText->chunkOffset == uText->chunkLength) {
          --uText->chunkOffset;
      }
  }

  // We're a UTF-16 source so our nativeIndexingLimit will always
  // be equal to our chunk length.
  uText->nativeIndexingLimit = uText->chunkLength;

  return TRUE;
}

static void Close(UText*) {
  // TODO: Fill this in.
}

class BreakIteratorWrapper {
 public:
  BreakIteratorWrapper(icu::BreakIterator* it) : it_(it), utext_(nullptr), context_(nullptr) {
  }

  bool SetText(JNIEnv* env, jstring str) {
    const jsize length = env->GetStringLength(str);

    UErrorCode err = U_ZERO_ERROR;
    // TODO: We can allocale less than sizeof(uint16_t) here for
    // smaller strings. make that optimization.
    UText* provider = utext_setup(nullptr, sizeof(uint16_t) * kBufferSize, &err);
    if (U_FAILURE(err)) {
      return false;
    }

    std::pair<JNIEnv*, jstring>* context = new std::pair<JNIEnv*, jstring>(env, str);

    provider->a = static_cast<int64_t>(length);
    provider->context = context;
    provider->pFuncs = &provider_funcs;
    provider->chunkContents = reinterpret_cast<UChar*>(provider->pExtra);

    utext_ = provider;
    context_ = context;

    it_->setText(provider, err);

    return !U_FAILURE(err);
  }

  bool Refresh(JNIEnv* env, jstring string) {
    if (context_ == nullptr) {
      return SetText(env, string);
    }

    context_->first = env;
    context_->second = string;
    return true;
  }

  icu::BreakIterator* get() const {
    return it_;
  }

  ~BreakIteratorWrapper() {
    // Is this appropriate ? what utext_* functions must we call here.
    delete utext_;
  }

 private:
  BreakIteratorWrapper(icu::BreakIterator* it, UText* utext,
                       std::pair<JNIEnv*, jstring>* context) :
      it_(it),
      utext_(utext),
      context_(context) {
  }

  icu::BreakIterator* const it_;
  UText* utext_;
  std::pair<JNIEnv*, jstring>* context_;
};

/**
 * We use ICU4C's BreakIterator class, but our input is on the Java heap and potentially moving
 * around between calls. This wrapper class ensures that our RegexMatcher is always pointing at
 * the current location of the char[]. Earlier versions of Android simply copied the data to the
 * native heap, but that's wasteful and hides allocations from the garbage collector.
 */

/*
class BreakIteratorAccessor {
 public:
  BreakIteratorAccessor(JNIEnv* env, jlong address, jstring javaInput, bool reset) {
    init(env, address);
    mJavaInput = javaInput;

    if (mJavaInput == NULL) {
      return;
    }

    mChars = env->GetStringChars(mJavaInput, NULL);
    if (mChars == NULL) {
      return;
    }

    mUText = utext_openUChars(NULL, mChars, env->GetStringLength(mJavaInput), &mStatus);
    if (mUText == NULL) {
      return;
    }

    if (reset) {
      mBreakIterator->setText(mUText, mStatus);
    } else {
      mBreakIterator->refreshInputText(mUText, mStatus);
    }
  }

  BreakIteratorAccessor(JNIEnv* env, jlong address) {
    init(env, address);
  }

  ~BreakIteratorAccessor() {
    utext_close(mUText);
    if (mJavaInput) {
      mEnv->ReleaseStringChars(mJavaInput, mChars);
    }
    maybeThrowIcuException(mEnv, "utext_close", mStatus);
  }

  icu::BreakIterator* operator->() {
    return mBreakIterator;
  }

  UErrorCode& status() {
    return mStatus;
  }

 private:
  void init(JNIEnv* env, jlong address) {
    mEnv = env;
    mJavaInput = NULL;
    mBreakIterator = toBreakIterator(address);
    mChars = NULL;
    mStatus = U_ZERO_ERROR;
    mUText = NULL;
  }

  JNIEnv* mEnv;
  jstring mJavaInput;
  icu::BreakIterator* mBreakIterator;
  const jchar* mChars;
  UErrorCode mStatus;
  UText* mUText;

  // Disallow copy and assignment.
  BreakIteratorAccessor(const BreakIteratorAccessor&);
  void operator=(const BreakIteratorAccessor&);
};
*/

BreakIteratorWrapper* breakIteratorWrapper(jlong address) {
  return reinterpret_cast<BreakIteratorWrapper*>(static_cast<uintptr_t>(address));
}

icu::BreakIterator* refreshedIterator(jlong address, JNIEnv* env, jstring str) {
  BreakIteratorWrapper* wrapper = breakIteratorWrapper(address);
  wrapper->Refresh(env, str);
  return wrapper->get();
}


#define MAKE_BREAK_ITERATOR_INSTANCE(F) \
  ScopedIcuLocale icuLocale(env, javaLocaleName); \
  if (!icuLocale.valid()) { \
    return 0; \
  } \
  UErrorCode status = U_ZERO_ERROR; \
  icu::BreakIterator* it = F(icuLocale.locale(), status); \
  if (maybeThrowIcuException(env, "ubrk_open", status)) { \
    return 0; \
  } \
  return reinterpret_cast<uintptr_t>(new BreakIteratorWrapper(it))

static jlong NativeBreakIterator_cloneImpl(JNIEnv*, jclass, jlong address) {
  // icu::BreakIterator* it = breakIteratorWrapper(address)->get()->clone();
  //
  // TODO: We need to create a BreakIteratorWrapper out of this chap.
  return reinterpret_cast<uintptr_t>(breakIteratorWrapper(address)->get()->clone());
}

static void NativeBreakIterator_closeImpl(JNIEnv*, jclass, jlong address) {
  delete breakIteratorWrapper(address);
}

static jint NativeBreakIterator_currentImpl(JNIEnv* env, jclass, jlong address, jstring javaInput) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->current();
}

static jint NativeBreakIterator_firstImpl(JNIEnv* env, jclass, jlong address, jstring javaInput) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->first();
}

static jint NativeBreakIterator_followingImpl(JNIEnv* env, jclass, jlong address, jstring javaInput, jint offset) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->following(offset);
}

static jlong NativeBreakIterator_getCharacterInstanceImpl(JNIEnv* env, jclass, jstring javaLocaleName) {
  MAKE_BREAK_ITERATOR_INSTANCE(icu::BreakIterator::createCharacterInstance);
}

static jlong NativeBreakIterator_getLineInstanceImpl(JNIEnv* env, jclass, jstring javaLocaleName) {
  MAKE_BREAK_ITERATOR_INSTANCE(icu::BreakIterator::createLineInstance);
}

static jlong NativeBreakIterator_getSentenceInstanceImpl(JNIEnv* env, jclass, jstring javaLocaleName) {
  MAKE_BREAK_ITERATOR_INSTANCE(icu::BreakIterator::createSentenceInstance);
}

static jlong NativeBreakIterator_getWordInstanceImpl(JNIEnv* env, jclass, jstring javaLocaleName) {
  MAKE_BREAK_ITERATOR_INSTANCE(icu::BreakIterator::createWordInstance);
}

static jboolean NativeBreakIterator_isBoundaryImpl(JNIEnv* env, jclass, jlong address, jstring javaInput, jint offset) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->isBoundary(offset);
}

static jint NativeBreakIterator_lastImpl(JNIEnv* env, jclass, jlong address, jstring javaInput) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->last();
}

static jint NativeBreakIterator_nextImpl(JNIEnv* env, jclass, jlong address, jstring javaInput, jint n) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  if (n < 0) {
    while (n++ < -1) {
      it->previous();
    }
    return it->previous();
  } else if (n == 0) {
    return it->current();
  } else {
    while (n-- > 1) {
      it->next();
    }
    return it->next();
  }
  return -1;
}

static jint NativeBreakIterator_precedingImpl(JNIEnv* env, jclass, jlong address, jstring javaInput, jint offset) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->preceding(offset);
}

static jint NativeBreakIterator_previousImpl(JNIEnv* env, jclass, jlong address, jstring javaInput) {
  icu::BreakIterator* it = refreshedIterator(address, env, javaInput);
  return it->previous();
}

static void NativeBreakIterator_setTextImpl(JNIEnv* env, jclass, jlong address, jstring javaInput) {
  BreakIteratorWrapper* wrapper = breakIteratorWrapper(address);
  wrapper->SetText(env, javaInput);
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(NativeBreakIterator, cloneImpl, "(J)J"),
  NATIVE_METHOD(NativeBreakIterator, closeImpl, "(J)V"),
  NATIVE_METHOD(NativeBreakIterator, currentImpl, "(JLjava/lang/String;)I"),
  NATIVE_METHOD(NativeBreakIterator, firstImpl, "(JLjava/lang/String;)I"),
  NATIVE_METHOD(NativeBreakIterator, followingImpl, "(JLjava/lang/String;I)I"),
  NATIVE_METHOD(NativeBreakIterator, getCharacterInstanceImpl, "(Ljava/lang/String;)J"),
  NATIVE_METHOD(NativeBreakIterator, getLineInstanceImpl, "(Ljava/lang/String;)J"),
  NATIVE_METHOD(NativeBreakIterator, getSentenceInstanceImpl, "(Ljava/lang/String;)J"),
  NATIVE_METHOD(NativeBreakIterator, getWordInstanceImpl, "(Ljava/lang/String;)J"),
  NATIVE_METHOD(NativeBreakIterator, isBoundaryImpl, "(JLjava/lang/String;I)Z"),
  NATIVE_METHOD(NativeBreakIterator, lastImpl, "(JLjava/lang/String;)I"),
  NATIVE_METHOD(NativeBreakIterator, nextImpl, "(JLjava/lang/String;I)I"),
  NATIVE_METHOD(NativeBreakIterator, precedingImpl, "(JLjava/lang/String;I)I"),
  NATIVE_METHOD(NativeBreakIterator, previousImpl, "(JLjava/lang/String;)I"),
  NATIVE_METHOD(NativeBreakIterator, setTextImpl, "(JLjava/lang/String;)V"),
};
void register_libcore_icu_NativeBreakIterator(JNIEnv* env) {
  jniRegisterNativeMethods(env, "libcore/icu/NativeBreakIterator", gMethods, NELEM(gMethods));
}
