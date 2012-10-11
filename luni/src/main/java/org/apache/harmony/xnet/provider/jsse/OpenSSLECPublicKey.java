package org.apache.harmony.xnet.provider.jsse;

import java.security.InvalidKeyException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

public class OpenSSLECPublicKey implements ECPublicKey {
    private static final String ALGORITHM = "EC";

    protected transient int curveType;

    protected transient OpenSSLKey key;

    protected transient OpenSSLECGroupContext group;

    public OpenSSLECPublicKey(int curveType, OpenSSLECGroupContext group, OpenSSLKey key) {
        this.group = group;
        this.key = key;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public String getFormat() {
        return "X.509";
    }

    @Override
    public byte[] getEncoded() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ECParameterSpec getParams() {
        return group.getECParameterSpec();
    }

    private ECPoint getPublicKey() {
        OpenSSLECPointContext pubKey = new OpenSSLECPointContext(curveType, group,
                NativeCrypto.EC_KEY_get_public_key(key.getPkeyContext()));

        return pubKey.getECPoint();
    }

    @Override
    public ECPoint getW() {
        return getPublicKey();
    }

    public OpenSSLKey getOpenSSLKey() {
        return key;
    }

    public static OpenSSLKey getInstance(ECPublicKey ecPublicKey) throws InvalidKeyException {
        try {
            OpenSSLECGroupContext group = OpenSSLECGroupContext
                    .getInstance(ecPublicKey.getParams());
            OpenSSLECPointContext pubKey = OpenSSLECPointContext.getInstance(
                    NativeCrypto.get_EC_GROUP_type(group.getContext()), group, ecPublicKey.getW());
            return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(group.getContext(),
                    pubKey.getContext(), null));
        } catch (Exception e) {
            throw new InvalidKeyException(e);
        }
    }
}
