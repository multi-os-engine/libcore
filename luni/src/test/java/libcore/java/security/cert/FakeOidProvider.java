package libcore.java.security.cert;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;

public class FakeOidProvider extends Provider {
    /**
     * Used for testing some effects of algorithm OID mapping.
     */
    public static final String SIGALG_OID = "1.2.34359737229.1.1.5";

    /**
     * Used for testing some effects of algorithm OID mapping.
     */
    public static final String SIGALG_OID_NAME = "FAKEwithFAKE";

    public static final String PROVIDER_NAME = "FakeOidProvider";

    protected FakeOidProvider() {
        super(PROVIDER_NAME, 1.0, "Fake OID Provider for Tests");

        put("Signature." + SIGALG_OID, FakeOidSignature.class.getName());
    }

    public static class FakeOidSignature extends SignatureSpi {
        @Override
        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            System.out.println("yay got it");
        }

        @Override
        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        }

        @Override
        protected void engineUpdate(byte b) throws SignatureException {
        }

        @Override
        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        }

        @Override
        protected byte[] engineSign() throws SignatureException {
            return null;
        }

        @Override
        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
            return true;
        }

        @Override
        protected void engineSetParameter(String param, Object value)
                throws InvalidParameterException {
        }

        @Override
        protected Object engineGetParameter(String param) throws InvalidParameterException {
            return null;
        }
    }

    @Override
    public synchronized Service getService(String type, String algorithm) {

        Service s = super.getService(type, algorithm);
        System.out.println("Called for type=" + type + " algorithm=" + algorithm + " ["
                + toString() + "]");
        for (Service p : getServices()) {
            System.out.println("  Registered: " + p.toString());
        }
        return s;
    }
}
