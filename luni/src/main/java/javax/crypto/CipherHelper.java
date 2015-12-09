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
 * limitations under the License
 */
package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * @hide
 */
public abstract class CipherHelper {
    /** The attribute used for supported paddings. */
    private static final String ATTRIBUTE_PADDINGS = "SupportedPaddings";

    /** The attribute used for supported modes. */
    private static final String ATTRIBUTE_MODES = "SupportedModes";

    /**
     * If the attribute listed exists, check that it matches the regular
     * expression.
     */
    static boolean matchAttribute(Provider.Service service, String attr, String value) {
        if (value == null) {
            return true;
        }
        final String pattern = service.getAttribute(attr);
        if (pattern == null) {
            return true;
        }
        final String valueUc = value.toUpperCase(Locale.US);
        return valueUc.matches(pattern.toUpperCase(Locale.US));
    }

    //abstract List<Provider.Service> getServices();

    /** Items that need to be set on the Cipher instance. */
    enum NeedToSet {
        NONE, MODE, PADDING, BOTH,
    }

    /**
     * Expresses the various types of transforms that may be used during
     * initialization.
     */
    static class Transform {
        private final String name;
        private final NeedToSet needToSet;

        public Transform(String name, NeedToSet needToSet) {
            this.name = name;
            this.needToSet = needToSet;
        }
    }

    /**
     * Keeps track of the possible arguments to {@code Cipher#init(...)}.
     */
    static class InitParams {
        final InitType initType;
        final int opmode;
        final Key key;
        final SecureRandom random;
        final AlgorithmParameterSpec spec;
        final AlgorithmParameters params;

        InitParams(InitType initType, int opmode, Key key, SecureRandom random,
                AlgorithmParameterSpec spec, AlgorithmParameters params) {
            this.initType = initType;
            this.opmode = opmode;
            this.key = key;
            this.random = random;
            this.spec = spec;
            this.params = params;
        }
    }

    /**
     * Used to keep track of which underlying {@code CipherSpi#engineInit(...)}
     * variant to call when testing suitability.
     */
    static enum InitType {
        KEY, ALGORITHM_PARAMS, ALGORITHM_PARAM_SPEC,
    }

    static abstract class SpiAndProviderUpdater {
        /**
         * Lock held while the SPI is initializing.
         */
        private final Object initSpiLock = new Object();
        /**
         * The transformation split into parts.
         */
        private final String[] transformParts;

        /**
         * The provider specified when instance created.
         */
        private final Provider specifiedProvider;

        /**
         * The SPI implementation.
         */
        private final CipherSpi specifiedSpi;

        SpiAndProviderUpdater(
                String[] transformParts, Provider specifiedProvider, CipherSpi specifiedSpi) {
            this.transformParts = transformParts;
            this.specifiedProvider = specifiedProvider;
            this.specifiedSpi = specifiedSpi;
        }

        abstract void setCipherSpiImplAndProvider(CipherSpi cipherSpi, Provider provider);

        /**
         * Makes sure a CipherSpi that matches this type is selected. If
         * {@code key != null} then it assumes that a suitable provider exists for
         * this instance (used by {@link Cipher#init}. If the {@code initParams} is passed
         * in, then the {@code CipherSpi} returned will be initialized.
         *
         * @throws InvalidKeyException if the specified key cannot be used to
         *                             initialize this cipher.
         */
        CipherSpiAndProvider updateAndGetSpiAndProvider(
                InitParams initParams,
                CipherSpi spiImpl,
                Provider provider)
                throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (specifiedSpi != null) {
                return new CipherSpiAndProvider(specifiedSpi, provider);
            }
            synchronized (initSpiLock) {
                // This is not only a matter of performance. Many methods like update, doFinal, etc.
                // call {@code #getSpi()} (ie, {@code #getSpi(null /* params */)}) and without this
                // shortcut they would override an spi that was chosen using the key.
                if (spiImpl != null && initParams == null) {
                    return new CipherSpiAndProvider(spiImpl, provider);
                }
                final CipherSpiAndProvider sap = CipherHelper.tryCombinations(
                        initParams, specifiedProvider, transformParts);
                if (sap == null) {
                    throw new ProviderException("No provider found for "
                            + Arrays.toString(transformParts));
                }
                setCipherSpiImplAndProvider(sap.cipherSpi, sap.provider);
                return new CipherSpiAndProvider(sap.cipherSpi, sap.provider);
            }
        }

        /**
         * Convenience call when the Key is not available.
         */
        CipherSpiAndProvider updateAndGetSpiAndProvider(CipherSpi spiImpl, Provider provider) {
            try {
                return updateAndGetSpiAndProvider(null, spiImpl, provider);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
                throw new ProviderException("Exception thrown when params == null", e);
            }
        }

        CipherSpi getCurrentSpi(CipherSpi spiImpl) {
            if (specifiedSpi != null) {
                return specifiedSpi;
            }

            synchronized (initSpiLock) {
                return spiImpl;
            }
        }
    }

    /**
     * Tries to find the correct {@code Cipher} transform to use. Returns a
     * {@link org.apache.harmony.security.fortress.Engine.SpiAndProvider}, throws the first exception that was
     * encountered during attempted initialization, or {@code null} if there are
     * no providers that support the {@code initParams}.
     * <p>
     * {@code transformParts} must be in the format returned by
     * {@link Cipher#checkTransformation(String)}. The combinations of mode strings
     * tried are as follows:
     * <ul>
     * <li><code>[cipher]/[mode]/[padding]</code>
     * <li><code>[cipher]/[mode]</code>
     * <li><code>[cipher]//[padding]</code>
     * <li><code>[cipher]</code>
     * </ul>
     * {@code services} is a list of cipher services. Needs to be non-null only if
     * {@code provider != null}
     */
    static CipherSpiAndProvider tryCombinations(InitParams initParams, Provider provider,
            String[] transformParts)
            throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        // Enumerate all the transforms we need to try
        ArrayList<Transform> transforms = new ArrayList<Transform>();
        if (transformParts[1] != null && transformParts[2] != null) {
            transforms.add(new Transform(transformParts[0] + "/" + transformParts[1] + "/"
                    + transformParts[2], NeedToSet.NONE));
        }
        if (transformParts[1] != null) {
            transforms.add(new Transform(transformParts[0] + "/" + transformParts[1],
                    NeedToSet.PADDING));
        }
        if (transformParts[2] != null) {
            transforms.add(new Transform(transformParts[0] + "//" + transformParts[2],
                    NeedToSet.MODE));
        }
        transforms.add(new Transform(transformParts[0], NeedToSet.BOTH));

        // Try each of the transforms and keep track of the first exception
        // encountered.
        Exception cause = null;

        if (provider != null) {
            for (Transform transform : transforms) {
                Provider.Service service = provider.getService("Cipher", transform.name);
                if (service == null) {
                    continue;
                }
                return tryTransformWithProvider(initParams, transformParts, transform.needToSet,
                                service);
            }
        } else {
            for (Provider prov : Security.getProviders()) {
                for (Transform transform : transforms) {
                    Provider.Service service = prov.getService("Cipher", transform.name);
                    if (service == null) {
                        continue;
                    }

                    if (initParams == null || initParams.key == null
                            || service.supportsParameter(initParams.key)) {
                        try {
                            CipherSpiAndProvider sap = tryTransformWithProvider(initParams,
                                    transformParts, transform.needToSet, service);
                            if (sap != null) {
                                return sap;
                            }
                        } catch (Exception e) {
                            if (cause == null) {
                                cause = e;
                            }
                        }
                    }
                }
            }
        }
        if (cause instanceof InvalidKeyException) {
            throw (InvalidKeyException) cause;
        } else if (cause instanceof InvalidAlgorithmParameterException) {
            throw (InvalidAlgorithmParameterException) cause;
        } else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else if (cause != null) {
            throw new InvalidKeyException("No provider can be initialized with given key", cause);
        } else if (initParams == null || initParams.key == null) {
            return null;
        } else {
            // Since the key is not null, a suitable provider exists,
            // and it is an InvalidKeyException.
            throw new InvalidKeyException(
                    "No provider offers " + Arrays.toString(transformParts) + " for "
                    + initParams.key.getAlgorithm() + " key of class "
                    + initParams.key.getClass().getName() + " and export format "
                    + initParams.key.getFormat());
        }
    }

    static class CipherSpiAndProvider {
        CipherSpi cipherSpi;
        Provider provider;

        CipherSpiAndProvider(CipherSpi cipherSpi, Provider provider) {
            this.cipherSpi = cipherSpi;
            this.provider = provider;
        }
    }

    /**
     * Tries to initialize the {@code Cipher} from a given {@code service}. If
     * initialization is successful, the initialized {@code spi} is returned. If
     * the {@code service} cannot be initialized with the specified
     * {@code initParams}, then it's expected to throw
     * {@code InvalidKeyException} or {@code InvalidAlgorithmParameterException}
     * as a hint to the caller that it should continue searching for a
     * {@code Service} that will work.
     */
    static CipherSpiAndProvider tryTransformWithProvider(InitParams initParams,
            String[] transformParts, NeedToSet type, Provider.Service service)
                throws InvalidKeyException, InvalidAlgorithmParameterException  {
        try {
            /*
             * Check to see if the Cipher even supports the attributes before
             * trying to instantiate it.
             */
            if (!matchAttribute(service, ATTRIBUTE_MODES, transformParts[1])
                    || !matchAttribute(service, ATTRIBUTE_PADDINGS, transformParts[2])) {
                return null;
            }

            //CipherSpi service.newInstance(null);
            CipherSpiAndProvider sap = new CipherSpiAndProvider((CipherSpi) service.newInstance(null), service.getProvider());
            if (sap.cipherSpi == null || sap.provider == null) {
                return null;
            }
            CipherSpi spi = sap.cipherSpi;
            if (((type == NeedToSet.MODE) || (type == NeedToSet.BOTH))
                    && (transformParts[1] != null)) {
                spi.engineSetMode(transformParts[1]);
            }
            if (((type == NeedToSet.PADDING) || (type == NeedToSet.BOTH))
                    && (transformParts[2] != null)) {
                spi.engineSetPadding(transformParts[2]);
            }

            if (initParams != null) {
                switch (initParams.initType) {
                    case ALGORITHM_PARAMS:
                        spi.engineInit(initParams.opmode, initParams.key, initParams.params,
                                initParams.random);
                        break;
                    case ALGORITHM_PARAM_SPEC:
                        spi.engineInit(initParams.opmode, initParams.key, initParams.spec,
                                initParams.random);
                        break;
                    case KEY:
                        spi.engineInit(initParams.opmode, initParams.key, initParams.random);
                        break;
                    default:
                        throw new AssertionError("This should never be reached");
                }
            }
            return new CipherSpiAndProvider(spi, sap.provider);
        } catch (NoSuchAlgorithmException ignored) {
        } catch (NoSuchPaddingException ignored) {
        }
        return null;
    }
}
