package org.apache.harmony.xnet.provider.jsse;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.HashMap;
import java.util.Map;

public class OpenSSLECKeyPairGenerator extends KeyPairGenerator {
    private static final String ALGORITHM = "EC";

    private static final int DEFAULT_KEY_SIZE = 192;

    private static final Map<Integer, String> SIZE_TO_CURVE_NAME = new HashMap<Integer, String>();

    static {
        /* NIST curves */
        SIZE_TO_CURVE_NAME.put(192, "prime192v1");
        SIZE_TO_CURVE_NAME.put(224, "P-224");
        SIZE_TO_CURVE_NAME.put(239, "prime239v1");
        SIZE_TO_CURVE_NAME.put(256, "prime256v1");
        SIZE_TO_CURVE_NAME.put(384, "P-384");
        SIZE_TO_CURVE_NAME.put(521, "P-521");
    }

    private int curveType;

    private OpenSSLECGroupContext group;

    public OpenSSLECKeyPairGenerator() {
        super(ALGORITHM);
    }

    @Override
    public KeyPair generateKeyPair() {
        if (group == null) {
            final String curveName = SIZE_TO_CURVE_NAME.get(DEFAULT_KEY_SIZE);
            group = OpenSSLECGroupContext.getCurveByName(curveName);
        }

        final OpenSSLKey key = new OpenSSLKey(NativeCrypto.EC_KEY_generate_key(group.getContext()));
        return new KeyPair(new OpenSSLECPublicKey(curveType, group, key), new OpenSSLECPrivateKey(
                group, key));
    }

    @Override
    public void initialize(int keysize, SecureRandom random) {
        final String name = SIZE_TO_CURVE_NAME.get(keysize);
        if (name == null) {
            throw new InvalidParameterException("unknown key size " + keysize);
        }

        group = OpenSSLECGroupContext.getCurveByName(name);
    }

    @Override
    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (param instanceof ECParameterSpec) {
            ECParameterSpec ecParam = (ECParameterSpec) param;

            group = OpenSSLECGroupContext.getInstance(ecParam);
        } else if (param instanceof ECGenParameterSpec) {
            ECGenParameterSpec ecParam = (ECGenParameterSpec) param;

            final String curveName = ecParam.getName();

            OpenSSLECGroupContext possibleGroup = OpenSSLECGroupContext.getCurveByName(curveName);

            if (possibleGroup == null) {
                throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
            }

            group = possibleGroup;
        } else {
            throw new InvalidAlgorithmParameterException(
                    "parameter must be ECParameterSpec or ECGenParameterSpec");
        }

        curveType = NativeCrypto.get_EC_GROUP_type(group.getContext());
    }
}
