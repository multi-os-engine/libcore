/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.apache.harmony.xnet.provider.jsse;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

import libcore.util.EmptyArray;

public abstract class OpenSSLCipher extends CipherSpi {

    /**
     * Modes that a block cipher may support.
     */
    protected static enum Mode {
        CBC,
        CFB, CFB1, CFB8, CFB128,
        CTR,
        CTS,
        ECB,
        OFB, OFB64, OFB128,
        PCBC,
    }

    /**
     * Paddings that a block cipher may support.
     */
    protected static enum Padding {
        NOPADDING,
        PKCS5PADDING,
        ISO10126PADDING,
    }

    /**
     * Native pointer for the OpenSSL EVP_CIPHER context.
     */
    private OpenSSLCipherContext cipherCtx;

    /**
     * The current cipher mode.
     */
    private Mode mode = Mode.ECB;

    /**
     * The current cipher padding.
     */
    private Padding padding = Padding.PKCS5PADDING;

    /**
     * The Initial Vector (IV) used for the current cipher.
     */
    private byte[] iv;

    /**
     * Current cipher mode: encrypting or decrypting.
     */
    private boolean encrypting;

    /**
     * The block size of the current cipher.
     */
    private int blockSize;

    /**
     * Buffer to hold a block-sized entry before calling into OpenSSL.
     */
    private byte[] buffer;

    /**
     * Current offset in the buffer.
     */
    private int bufferOffset;

    protected OpenSSLCipher() {
    }

    /**
     * Returns the OpenSSL cipher name for the particular {@code keySize} and
     * cipher {@code mode}.
     */
    protected abstract String getCipherName(int keySize, Mode mode);

    /**
     * Checks whether the cipher supports this particular {@code keySize} (in
     * bytes) and throws {@code InvalidKeyException} if it doesn't.
     */
    protected abstract void checkSupportedKeySize(int keySize) throws InvalidKeyException;

    /**
     * Checks whether the cipher supports this particular cipher {@code mode}
     * and throws {@code NoSuchAlgorithmException} if it doesn't.
     */
    protected abstract void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException;

    /**
     * Checks whether the cipher supports this particular cipher {@code padding}
     * and throws {@code NoSuchPaddingException} if it doesn't.
     */
    protected abstract void checkSupportedPadding(Padding padding) throws NoSuchPaddingException;

    @Override
    protected void engineSetMode(String modeStr) throws NoSuchAlgorithmException {
        final Mode mode;
        try {
            mode = Mode.valueOf(modeStr.toUpperCase(Locale.US));
        } catch (IllegalArgumentException e) {
            NoSuchAlgorithmException newE = new NoSuchAlgorithmException("No such mode: "
                    + modeStr);
            newE.initCause(e);
            throw newE;
        }
        checkSupportedMode(mode);
        this.mode = mode;
    }

    @Override
    protected void engineSetPadding(String paddingStr) throws NoSuchPaddingException {
        final String paddingStrUpper = paddingStr.toUpperCase(Locale.US);
        final Padding padding;
        try {
            padding = Padding.valueOf(paddingStrUpper);
        } catch (IllegalArgumentException e) {
            NoSuchPaddingException newE = new NoSuchPaddingException("No such padding: "
                    + paddingStr);
            newE.initCause(e);
            throw newE;
        }
        checkSupportedPadding(padding);
        this.padding = padding;
    }

    @Override
    protected int engineGetBlockSize() {
        return blockSize;
    }

    /**
     * The size of output if {@code doFinal()} is called with this
     * {@code inputLen}. If padding is enabled and the size of the input puts it
     * right at the block size, it will add another block for the padding.
     */
    private final int getFinalOutputSize(int inputLen) {
        final int totalLen = bufferOffset + inputLen;
        final int overrunLen = totalLen % blockSize;

        if (overrunLen == 0) {
            if ((padding == Padding.NOPADDING) && (totalLen > 0)) {
                return totalLen;
            } else {
                return totalLen + blockSize;
            }
        } else {
            return totalLen - overrunLen + blockSize;
        }
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {
        return getFinalOutputSize(inputLen);
    }

    @Override
    protected byte[] engineGetIV() {
        return iv;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    private void engineInitInternal(int opmode, Key key, byte[] iv) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE) {
            encrypting = true;
        } else if (opmode == Cipher.DECRYPT_MODE || opmode == Cipher.UNWRAP_MODE) {
            encrypting = false;
        } else {
            throw new InvalidParameterException("Unsupported opmode " + opmode);
        }

        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Only SecretKey is supported");
        }

        final byte[] encodedKey = key.getEncoded();
        if (encodedKey == null) {
            throw new InvalidKeyException("key.getEncoded() == null");
        }

        checkSupportedKeySize(encodedKey.length);

        final int cipherType = NativeCrypto.EVP_get_cipherbyname(getCipherName(encodedKey.length,
                mode));

        final int ivLength = NativeCrypto.EVP_CIPHER_iv_length(cipherType);
        if (iv == null) {
            if (ivLength > 0) {
                throw new InvalidAlgorithmParameterException("expected IV length of " + ivLength);
            }
        } else if (iv.length != ivLength) {
            throw new InvalidAlgorithmParameterException("expected IV length of " + ivLength);
        }

        this.iv = iv;

        cipherCtx = new OpenSSLCipherContext(NativeCrypto.EVP_CIPHER_CTX_new());
        NativeCrypto.EVP_CipherInit_ex(cipherCtx.getContext(), cipherType, encodedKey, iv,
                encrypting);
        blockSize = NativeCrypto.EVP_CIPHER_CTX_block_size(cipherCtx.getContext());

        // OpenSSL only supports PKCS5 Padding.
        NativeCrypto.EVP_CIPHER_CTX_set_padding(cipherCtx.getContext(),
                padding == Padding.PKCS5PADDING);

        buffer = new byte[blockSize];
        bufferOffset = 0;
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        try {
            engineInitInternal(opmode, key, null);
        } catch (InvalidAlgorithmParameterException ignore) {
            // Shouldn't happen.
        }
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        final byte[] iv;
        if (params instanceof IvParameterSpec) {
            IvParameterSpec ivParams = (IvParameterSpec) params;
            iv = ivParams.getIV();
        } else {
            iv = null;
        }

        engineInitInternal(opmode, key, iv);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        final AlgorithmParameterSpec spec;
        try {
            spec = params.getParameterSpec(IvParameterSpec.class);
        } catch (InvalidParameterSpecException e) {
            throw new InvalidAlgorithmParameterException(e);
        }

        engineInit(opmode, key, spec, random);
    }

    private final int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset, int totalLen, int fullBlocksSize) throws ShortBufferException {
        final int intialOutputOffset = outputOffset;

        /* Take care of existing buffered bytes. */
        final int remainingBuffer = buffer.length - bufferOffset;
        if (bufferOffset > 0 && inputLen >= remainingBuffer) {
            System.arraycopy(input, inputOffset, buffer, bufferOffset, remainingBuffer);
            final int writtenBytes = NativeCrypto.EVP_CipherUpdate(cipherCtx.getContext(), output,
                    outputOffset, buffer, 0, blockSize);
            fullBlocksSize -= writtenBytes;
            outputOffset += writtenBytes;

            inputLen -= remainingBuffer;
            inputOffset += remainingBuffer;

            bufferOffset = 0;
        }

        /* Take care of the bytes that would fill up our block-sized buffer. */
        if (fullBlocksSize > 0) {
            NativeCrypto.EVP_CipherUpdate(cipherCtx.getContext(), output, outputOffset, input,
                    inputOffset, fullBlocksSize);
            inputLen -= fullBlocksSize;
            inputOffset += fullBlocksSize;
        }

        /* Put the rest into the buffer for next time. */
        if (inputLen > 0) {
            System.arraycopy(input, inputOffset, buffer, bufferOffset, inputLen);
            bufferOffset += inputLen;
            outputOffset += inputLen;
        }

        return outputOffset - intialOutputOffset;
    }

    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        final int totalLen = bufferOffset + inputLen;
        final int fullBlocksSize = totalLen - (totalLen % blockSize);

        /* See how large our output buffer would need to be. */
        final byte[] output;
        if (fullBlocksSize > 0) {
            output = new byte[fullBlocksSize];
        } else {
            output = EmptyArray.BYTE;
        }

        try {
            updateInternal(input, inputOffset, inputLen, output, 0, totalLen, fullBlocksSize);
        } catch (ShortBufferException e) {
            /* This shouldn't happen. */
            throw new AssertionError("calculated buffer size was wrong: " + fullBlocksSize);
        }

        return output;
    }

    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset) throws ShortBufferException {
        final int totalLen = bufferOffset + inputLen;
        final int fullBlocksSize = totalLen - (totalLen % blockSize);
        return updateInternal(input, inputOffset, inputLen, output, outputOffset, totalLen, fullBlocksSize);
    }

    private int doFinalInternal(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset, int totalLen, int trailingLen, int fullBlocksSize)
            throws IllegalBlockSizeException, BadPaddingException {
        if ((padding == Padding.NOPADDING) && (trailingLen != 0)) {
            throw new IllegalBlockSizeException("not multiple of block size " + trailingLen
                    + " != " + blockSize);
        }

        /* Remember this so we can tell how many characters were written. */
        final int initialOutputOffset = outputOffset;

        /* Take care of existing buffered bytes. */
        if (bufferOffset > 0) {
            final int writtenBytes = NativeCrypto.EVP_CipherUpdate(cipherCtx.getContext(), output,
                    outputOffset, buffer, 0, bufferOffset);
            outputOffset += writtenBytes;
            bufferOffset = 0;
        }

        /* Take care of the non-buffered bytes. */
        if (inputLen > 0) {
            final int writtenBytes = NativeCrypto.EVP_CipherUpdate(cipherCtx.getContext(), output,
                    outputOffset, input, inputOffset, inputLen);
            outputOffset += writtenBytes;
        }

        /* Allow OpenSSL to pad if necessary and clean up state. */
        final int writtenBytes = NativeCrypto.EVP_CipherFinal_ex(cipherCtx.getContext(), output,
                outputOffset);
        outputOffset += writtenBytes;

        return outputOffset - initialOutputOffset;
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        final int totalLen = bufferOffset + inputLen;
        final int trailingLen = totalLen % blockSize;

        final int fullBlocksSize;
        if (trailingLen > 0) {
            fullBlocksSize = totalLen - trailingLen + blockSize;
        } else {
            fullBlocksSize = totalLen;
        }

        byte[] output = new byte[fullBlocksSize];
        final int bytesWritten = doFinalInternal(input, inputOffset, inputLen, output, 0, totalLen,
                trailingLen, fullBlocksSize);

        /*
         * If we had some sort of padding to trim off, the final size will be a
         * bit smaller.
         */
        if (bytesWritten != output.length) {
            byte[] temp = new byte[bytesWritten];
            System.arraycopy(output, 0, temp, 0, bytesWritten);
            output = temp;
        }

        return output;
    }

    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset) throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        if (output == null) {
            throw new NullPointerException("output == null");
        }

        final int totalLen = bufferOffset + inputLen;
        final int trailingLen = totalLen % blockSize;

        final int fullBlocksSize;
        if (trailingLen > 0) {
            fullBlocksSize = totalLen - trailingLen + blockSize;
        } else {
            fullBlocksSize = totalLen;
        }

        if ((output.length - outputOffset) < fullBlocksSize) {
            throw new ShortBufferException("output buffer too small: "
                    + (output.length - outputOffset) + " < " + fullBlocksSize);
        }

        return doFinalInternal(input, inputOffset, inputLen, output, outputOffset, totalLen, trailingLen,
                fullBlocksSize);
    }

    public static class AES extends OpenSSLCipher {
        public AES() {
        }

        @Override
        protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
            switch (keyLength) {
                case 16: // AES 128
                case 24: // AES 192
                case 32: // AES 256
                    return;
                default:
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
            }
        }

        @Override
        protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
            switch (mode) {
                case CBC:
                case CFB:
                case CFB1:
                case CFB8:
                case CFB128:
                case CTR:
                case ECB:
                case OFB:
                    return;
                default:
                    throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
            }
        }

        @Override
        protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
            switch (padding) {
                case NOPADDING:
                case PKCS5PADDING:
                    return;
                default:
                    throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
            }
        }

        @Override
        protected String getCipherName(int keyLength, Mode mode) {
            return "aes-" + (keyLength * 8) + "-" + mode.toString().toLowerCase(Locale.US);
        }
    }
}
