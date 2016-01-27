/*
 * Copyright 2016 The Android Open Source Project
 */

package sun.security.provider;

import java.security.Provider;

/**
 * A security provider that provides the OpenJDK version of the CertPathBuilder and
 * CertPathVerifier.
 */
public final class CertPathProvider extends Provider {

    public CertPathProvider() {
        super("CertPathProvider", 1.0, "Provider of CertPathBuilder and CertPathVerifier");

        // CertPathBuilder
        put("CertPathBuilder.PKIX", "sun.security.provider.certpath.SunCertPathBuilder"); 
        put("CertPathBuilder.PKIX ImplementedIn", "Software");
        put("CertPathBuilder.PKIX ValidationAlgorithm", "RFC3280");

        // CertPathValidator
        put("CertPathValidator.PKIX", "sun.security.provider.certPath.PKIXCertPathValidator");
        put("CertPathValidator.PKIX ImplementedIn", "Software");
        put("CertPathValidator.PKIX ValidationAlgorithm", "RFC3280");
    }
}
