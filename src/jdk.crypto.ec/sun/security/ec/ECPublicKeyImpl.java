/*
 * Copyright (c) 2006, 2023, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.security.ec;

import java.io.IOException;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import sun.security.util.ECParameters;
import sun.security.util.ECUtil;

import sun.security.x509.*;

/**
 * Key implementation for EC public keys.
 *
 * @since   1.6
 * @author  Andreas Sterbenz
 */
public final class ECPublicKeyImpl extends X509Key implements ECPublicKey {

    @java.io.Serial
    private static final long serialVersionUID = -2462037275160462289L;

    private ECPoint w;
    private ECParameterSpec params;

    /**
     * Construct a key from its components. Used by the
     * ECKeyFactory.
     */
    @SuppressWarnings("deprecation")
    ECPublicKeyImpl(ECPoint w, ECParameterSpec params)
            throws InvalidKeyException {
        this.w = w;
        this.params = params;
        // generate the encoding
        algid = new AlgorithmId
            (AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(params));
        key = ECUtil.encodePoint(w, params.getCurve());
    }

    /**
     * Construct a key from its encoding.
     */
    ECPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        decode(encoded);
    }

    // see JCA doc
    public String getAlgorithm() {
        return "EC";
    }

    // see JCA doc
    public ECPoint getW() {
        return w;
    }

    // see JCA doc
    public ECParameterSpec getParams() {
        return params;
    }

    // Internal API to get the encoded point. Currently used by SunPKCS11.
    // This may change/go away depending on what we do with the public API.
    @SuppressWarnings("deprecation")
    public byte[] getEncodedPublicValue() {
        return key.clone();
    }

    /**
     * Parse the key. Called by X509Key.
     */
    @SuppressWarnings("deprecation")
    protected void parseKeyBits() throws InvalidKeyException {
        AlgorithmParameters algParams = this.algid.getParameters();
        if (algParams == null) {
            throw new InvalidKeyException("EC domain parameters must be " +
                "encoded in the algorithm identifier");
        }

        try {
            params = algParams.getParameterSpec(ECParameterSpec.class);
            w = ECUtil.decodePoint(key, params.getCurve());
        } catch (IOException e) {
            throw new InvalidKeyException("Invalid EC key", e);
        } catch (InvalidParameterSpecException e) {
            throw new InvalidKeyException("Invalid EC key", e);
        }
    }

    // return a string representation of this key for debugging
    public String toString() {
        return "Sun EC public key, " + params.getCurve().getField().getFieldSize()
            + " bits\n  public x coord: " + w.getAffineX()
            + "\n  public y coord: " + w.getAffineY()
            + "\n  parameters: " + params;
    }

    @java.io.Serial
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }

    /**
     * Restores the state of this object from the stream.
     * <p>
     * Deserialization of this object is not supported.
     *
     * @param  stream the {@code ObjectInputStream} from which data is read
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if a serialized class cannot be loaded
     */
    @java.io.Serial
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(
                "ECPublicKeyImpl keys are not directly deserializable");
    }
}
