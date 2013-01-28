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

#include "UniquePtr.h"

#include <stdarg.h>
#include <string.h>
#include <unistd.h>

#include <openssl/objects.h>
#include <openssl/engine.h>
#include <openssl/evp.h>
#include <openssl/pem.h>

#define DYNAMIC_ENGINE
#define TEST_ENGINE_ID   "test"
#define TEST_ENGINE_NAME "libcore test engine"

struct RSA_Delete {
    void operator()(RSA* p) const {
        RSA_free(p);
    }
};
typedef UniquePtr<RSA, RSA_Delete> Unique_RSA;

static EVP_PKEY *test_load_key(ENGINE* e, const char *key_id,
        EVP_PKEY* (*read_func)(BIO*, EVP_PKEY**, pem_password_cb*, void*)) {
    void* data = static_cast<void*>(const_cast<char*>(key_id));
    BIO* in = BIO_new_mem_buf(data, strlen(key_id));
    if (!in) {
        return NULL;
    }

    EVP_PKEY *key = read_func(in, NULL, 0, NULL);
    BIO_free(in);

    if (key != NULL && EVP_PKEY_type(key->type) == EVP_PKEY_RSA) {
        ENGINE_init(e);

        Unique_RSA rsa(EVP_PKEY_get1_RSA(key));
        rsa->engine = e;
        rsa->flags |= RSA_FLAG_EXT_PKEY;
    }

    return key;
}

static EVP_PKEY* test_load_privkey(ENGINE* e, const char* key_id, UI_METHOD*, void*) {
    return test_load_key(e, key_id, PEM_read_bio_PrivateKey);
}

static EVP_PKEY* test_load_pubkey(ENGINE* e, const char* key_id, UI_METHOD*, void*) {
    return test_load_key(e, key_id, PEM_read_bio_PUBKEY);
}

static int test_engine_setup(ENGINE* e) {
    if (!ENGINE_set_id(e, TEST_ENGINE_ID)
            || !ENGINE_set_name(e, TEST_ENGINE_NAME)
            || !ENGINE_set_flags(e, 0)
            || !ENGINE_set_RSA(e, RSA_get_default_method())
            || !ENGINE_set_load_privkey_function(e, test_load_privkey)
            || !ENGINE_set_load_pubkey_function(e, test_load_pubkey)) {
        return 0;
    }

    return 1;
}

static int test_engine_bind_fn(ENGINE *e, const char *id) {
    if (id && (strcmp(id, TEST_ENGINE_ID) != 0)) {
        return 0;
    }

    if (!test_engine_setup(e)) {
        return 0;
    }

    return 1;
}

extern "C" {
#undef OPENSSL_EXPORT
#define OPENSSL_EXPORT extern __attribute__ ((visibility ("default")))

IMPLEMENT_DYNAMIC_CHECK_FN()
IMPLEMENT_DYNAMIC_BIND_FN(test_engine_bind_fn)
};
