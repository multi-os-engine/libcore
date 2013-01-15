package libcore.java.security.cert;

import tests.support.resource.Support_Resources;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class X509CertificateNistPkitsTest extends TestCase {
    private final X509Certificate getCertificate(CertificateFactory f, String name)
            throws Exception {
        final String fileName = "nist-pkits/certs/" + name;
        final InputStream is = Support_Resources.getStream(fileName);
        assertNotNull("File does not exist: " + fileName, is);
        try {
            return (X509Certificate) f.generateCertificate(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    private final X509Certificate[] getCertificates(CertificateFactory f, String[] names)
            throws Exception {
        X509Certificate[] certs = new X509Certificate[names.length];

        for (int i = 0; i < names.length; i++) {
            certs[i] = getCertificate(f, names[i]);
        }

        return certs;
    }

    private final X509CRL getCRL(CertificateFactory f, String name) throws Exception {
        final String fileName = "nist-pkits/crls/" + name;
        final InputStream is = Support_Resources.getStream(fileName);
        assertNotNull("File does not exist: " + fileName, is);
        try {
            return (X509CRL) f.generateCRL(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    private final X509CRL[] getCRLs(CertificateFactory f, String[] names) throws Exception {
        X509CRL[] crls = new X509CRL[names.length];

        for (int i = 0; i < names.length; i++) {
            crls[i] = getCRL(f, names[i]);
        }

        return crls;
    }

    private CertPath getTestPath(CertificateFactory f, String[] pathCerts) throws Exception {
        X509Certificate[] certs = getCertificates(f, pathCerts);
        return f.generateCertPath(Arrays.asList(certs));
    }

    private PKIXParameters getTestPathParams(CertificateFactory f, String trustedCAName,
            String[] pathCerts, String[] pathCRLs) throws Exception {
        X509Certificate[] certs = getCertificates(f, pathCerts);
        X509CRL[] crls = getCRLs(f, pathCRLs);
        X509Certificate trustedCA = getCertificate(f, trustedCAName);

        Collection<Object> certCollection = new ArrayList<Object>();
        certCollection.addAll(Arrays.asList(crls));
        certCollection.addAll(Arrays.asList(certs));
        certCollection.add(trustedCA);
        CollectionCertStoreParameters certStoreParams = new CollectionCertStoreParameters(
                certCollection);
        CertStore certStore = CertStore.getInstance("Collection", certStoreParams);

        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
        anchors.add(new TrustAnchor(trustedCA, null));

        PKIXParameters params = new PKIXParameters(anchors);
        params.addCertStore(certStore);

        return params;
    }

    private void assertInvalidPath(String trustAnchor, String[] certs, String[] crls)
            throws Exception, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");

        PKIXParameters params = getTestPathParams(f, trustAnchor, certs, crls);
        CertPath cp = getTestPath(f, certs);
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

        try {
            PKIXCertPathValidatorResult cpvResult = (PKIXCertPathValidatorResult) cpv.validate(cp,
                    params);
            fail();
        } catch (CertPathValidatorException expected) {
        }
    }

    private void assertValidPath(String trustAnchor, String[] certs, String[] crls)
            throws Exception, NoSuchAlgorithmException, CertPathValidatorException,
            InvalidAlgorithmParameterException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");

        PKIXParameters params = getTestPathParams(f, trustAnchor, certs, crls);
        CertPath cp = getTestPath(f, certs);
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

        PKIXCertPathValidatorResult cpvResult = (PKIXCertPathValidatorResult) cpv.validate(cp,
                params);
    }

    public void testSignature_ValidCertificatePathTest1() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidCertificatePathTest1EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testSignature_InvalidCASignatureTest2() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidCASignatureTest2EE.crt",
                "BadSignedCACert.crt",
        };

        String[] crls = new String[] {
                "BadSignedCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testSignature_InvalidEESignatureTest3() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidEESignatureTest3EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testSignature_ValidDSASignaturesTest4() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidDSASignaturesTest4EE.crt",
                "DSACACert.crt",
        };

        String[] crls = new String[] {
                "DSACACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testSignature_ValidDSAParameterInheritanceTest5() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidDSAParameterInheritanceTest5EE.crt",
                "DSAParametersInheritedCACert.crt",
                "DSACACert.crt",
        };

        String[] crls = new String[] {
                "DSACACRL.crl",
                "DSAParametersInheritedCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testSignature_InvalidDSASignatureTest6() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidDSASignatureTest6EE.crt",
                "DSACACert.crt",
        };

        String[] crls = new String[] {
                "DSACACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_InvalidCAnotBeforeDateTest1() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidCAnotBeforeDateTest1EE.crt",
                "BadnotBeforeDateCACert.crt",
        };

        String[] crls = new String[] {
                "BadnotBeforeDateCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_InvalidEEnotBeforeDateTest2() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidEEnotBeforeDateTest2EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_Validpre2000UTCnotBeforeDateTest3() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "Validpre2000UTCnotBeforeDateTest3EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testValidity_ValidGeneralizedTimenotBeforeDateTest4() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidGeneralizedTimenotBeforeDateTest4EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testValidity_InvalidCAnotAfterDateTest5() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidCAnotAfterDateTest5EE.crt",
                "BadnotAfterDateCACert.crt",
        };

        String[] crls = new String[] {
                "BadnotAfterDateCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_InvalidEEnotAfterDateTest6() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidEEnotAfterDateTest6EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_Invalidpre2000UTCEEnotAfterDateTest7() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "Invalidpre2000UTCEEnotAfterDateTest7EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testValidity_ValidGeneralizedTimenotAfterDateTest8() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidGeneralizedTimenotAfterDateTest8EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_InvalidNameChainingEETest1() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidNameChainingTest1EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_InvalidNameChainingOrderTest2() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidNameChainingOrderTest2EE.crt",
                "NameOrderingCACert.crt",
        };

        String[] crls = new String[] {
                "NameOrderCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidNameChainingWhitespaceTest3() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidNameChainingWhitespaceTest3EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidNameChainingWhitespaceTest4() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidNameChainingWhitespaceTest4EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidNameChainingCapitalizationTest5() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidNameChainingCapitalizationTest5EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidNameChainingUIDsTest6() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidNameUIDsTest6EE.crt",
                "UIDCACert.crt",
        };

        String[] crls = new String[] {
                "UIDCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidRFC3280MandatoryAttributeTypesTest7() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidRFC3280MandatoryAttributeTypesTest7EE.crt",
                "RFC3280MandatoryAttributeTypesCACert.crt",
        };

        String[] crls = new String[] {
                "RFC3280MandatoryAttributeTypesCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidRFC3280MandatoryAttributeTypesTest8() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidRFC3280OptionalAttributeTypesTest8EE.crt",
                "RFC3280OptionalAttributeTypesCACert.crt",
        };

        String[] crls = new String[] {
                "RFC3280OptionalAttributeTypesCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidUTF8StringEncodedNamesTest9() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidUTF8StringEncodedNamesTest9EE.crt",
                "UTF8StringEncodedNamesCACert.crt",
        };

        String[] crls = new String[] {
                "UTF8StringEncodedNamesCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidRolloverfromPrintableStringtoUTF8StringTest10()
            throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidRolloverfromPrintableStringtoUTF8StringTest10EE.crt",
                "RolloverfromPrintableStringtoUTF8StringCACert.crt",
        };

        String[] crls = new String[] {
                "RolloverfromPrintableStringtoUTF8StringCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testNameChaining_ValidUTF8StringCaseInsensitiveMatchTest11() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidUTF8StringCaseInsensitiveMatchTest11EE.crt",
                "UTF8StringCaseInsensitiveMatchCACert.crt",
        };

        String[] crls = new String[] {
                "UTF8StringCaseInsensitiveMatchCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_MissingCRLTest1() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidMissingCRLTest1EE.crt",
                "NoCRLCACert.crt",
        };

        String[] crls = new String[] {
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidRevokedCATest2() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidRevokedCATest2EE.crt",
                "RevokedsubCACert.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "RevokedsubCACRL.crl",
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidRevokedEETest3() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidRevokedEETest3EE.crt",
                "GoodCACert.crt",
        };

        String[] crls = new String[] {
                "GoodCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidBadCRLSignatureTest4() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidBadCRLSignatureTest4EE.crt",
                "BadCRLSignatureCACert.crt",
        };

        String[] crls = new String[] {
                "BadCRLSignatureCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidBadCRLIssuerNameTest5() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidBadCRLIssuerNameTest5EE.crt",
                "BadCRLIssuerNameCACert.crt",
        };

        String[] crls = new String[] {
                "BadCRLIssuerNameCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidWrongCRLTest6() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidWrongCRLTest6EE.crt",
                "WrongCRLCACert.crt",
        };

        String[] crls = new String[] {
                "WrongCRLCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_ValidTwoCRLsTest7() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "ValidTwoCRLsTest7EE.crt",
                "TwoCRLsCACert.crt",
        };

        String[] crls = new String[] {
                "TwoCRLsCAGoodCRL.crl",
                "TwoCRLsCABadCRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertValidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidUnknownCRLEntryExtensionTest8() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidUnknownCRLEntryExtensionTest8EE.crt",
                "UnknownCRLEntryExtensionCACert.crt",
        };

        String[] crls = new String[] {
                "UnknownCRLEntryExtensionCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidUnknownCRLExtensionTest9() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidUnknownCRLExtensionTest9EE.crt",
                "UnknownCRLExtensionCACert.crt",
        };

        String[] crls = new String[] {
                "UnknownCRLExtensionCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidUnknownCRLExtensionTest10() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidUnknownCRLExtensionTest10EE.crt",
                "UnknownCRLExtensionCACert.crt",
        };

        String[] crls = new String[] {
                "UnknownCRLExtensionCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_InvalidOldCRLnextUpdateTest11() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "InvalidOldCRLnextUpdateTest11EE.crt",
                "OldCRLnextUpdateCACert.crt",
        };

        String[] crls = new String[] {
                "OldCRLnextUpdateCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }

    public void testBasicRevocation_Invalidpre2000CRLnextUpdateTest12() throws Exception {
        String trustAnchor = "TrustAnchorRootCertificate.crt";

        String[] certs = new String[] {
                "Invalidpre2000CRLnextUpdateTest12EE.crt",
                "pre2000CRLnextUpdateCACert.crt",
        };

        String[] crls = new String[] {
                "pre2000CRLnextUpdateCACRL.crl",
                "TrustAnchorRootCRL.crl",
        };

        assertInvalidPath(trustAnchor, certs, crls);
    }
}
