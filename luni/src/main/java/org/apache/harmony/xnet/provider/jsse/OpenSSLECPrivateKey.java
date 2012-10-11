package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;

public class OpenSSLECPrivateKey implements ECPrivateKey {
    private static final long serialVersionUID = -4036633595001083922L;

    private static final String ALGORITHM = "EC";

    protected transient OpenSSLKey key;

    protected transient OpenSSLECGroupContext group;

    public OpenSSLECPrivateKey(OpenSSLECGroupContext group, OpenSSLKey key) {
        this.group = group;
        this.key = key;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public String getFormat() {
        return "PKCS#8";
    }

    @Override
    public byte[] getEncoded() {
        return NativeCrypto.i2d_PKCS8_PRIV_KEY_INFO(key.getPkeyContext());
    }

    @Override
    public ECParameterSpec getParams() {
        return group.getECParameterSpec();
    }

    @Override
    public BigInteger getS() {
        return getPrivateKey();
    }

    private BigInteger getPrivateKey() {
        return new BigInteger(NativeCrypto.EC_KEY_get_private_key(key.getPkeyContext()));
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        final ECParameterSpec params = (ECParameterSpec) stream.readObject();
        final BigInteger privkey = (BigInteger) stream.readObject();

        OpenSSLECGroupContext group;
        try {
            group = OpenSSLECGroupContext.getInstance(params);
        } catch (InvalidAlgorithmParameterException e) {
            throw new ClassNotFoundException("cannot restore field type", e);
        }

        key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(group.getContext(), 0,
                privkey.toByteArray()));
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (key.isEngineBased()) {
            throw new NotSerializableException("engine-based keys can not be serialized");
        }

        stream.defaultWriteObject();
        stream.writeObject(getParams());
        stream.writeObject(getPrivateKey());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ECPrivateKey)) {
            return false;
        }

        if (o instanceof OpenSSLECPrivateKey) {
            OpenSSLECPrivateKey other = (OpenSSLECPrivateKey) o;
            return key.equals(other.key);
        }

        final ECPrivateKey other = (ECPrivateKey) o;
        if (!other.getS().equals(getPrivateKey())) {
            return false;
        }

        final ECParameterSpec spec = getParams();
        final ECParameterSpec otherSpec = other.getParams();

        return spec.getCurve().equals(otherSpec.getCurve())
                && spec.getGenerator().equals(otherSpec.getGenerator())
                && spec.getOrder().equals(otherSpec.getOrder())
                && spec.getCofactor() == otherSpec.getCofactor();
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    public OpenSSLKey getOpenSSLKey() {
        return key;
    }

    public static OpenSSLKey getInstance(ECPrivateKey ecPrivateKey) throws InvalidKeyException {
        try {
            OpenSSLECGroupContext group = OpenSSLECGroupContext.getInstance(ecPrivateKey
                    .getParams());
            final BigInteger privKey = ecPrivateKey.getS();
            return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(group.getContext(), 0,
                    privKey.toByteArray()));
        } catch (Exception e) {
            throw new InvalidKeyException(e);
        }
    }
}
