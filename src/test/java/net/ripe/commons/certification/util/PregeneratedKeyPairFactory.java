/**
 * The BSD License
 *
 * Copyright (c) 2010, 2011 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.commons.certification.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import net.ripe.commons.certification.ValidityPeriod;
import net.ripe.commons.certification.x509cert.X509ResourceCertificate;
import net.ripe.commons.certification.x509cert.X509ResourceCertificateBuilder;
import net.ripe.ipresource.IpResourceSet;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

/**
 * Caches generated keys in a key store so that they can be reused in the next test run. FOR TESTING ONLY!
 */
public final class PregeneratedKeyPairFactory extends KeyPairFactory {

    private static final PregeneratedKeyPairFactory INSTANCE = new PregeneratedKeyPairFactory();

    private static final char[] PASSPHRASE = "passphrase".toCharArray();

    private File keyStoreFile = new File(".pregenerated-test-key-pairs.keystore");
    private KeyStore pregeneratedKeys;

    private Map<Integer, Integer> keyCounters = new HashMap<Integer, Integer>();

    private PregeneratedKeyPairFactory() {
        super("SunRsaSign");
        initKeyStore();
    }

    private void initKeyStore() {
        InputStream input;
        try {
            input = new FileInputStream(keyStoreFile);
        } catch (FileNotFoundException e) {
            input = null;
        }
        try {
            pregeneratedKeys = KeyStore.getInstance("JKS", "SUN");
            pregeneratedKeys.load(input, PASSPHRASE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static PregeneratedKeyPairFactory getInstance() {
        return INSTANCE;
    }

    public synchronized KeyPair generate(int size) {
        try {
            int count = keyCounters.containsKey(size) ? keyCounters.get(size) : 0;
            keyCounters.put(size, count + 1);
            String alias = "key_" + size + "_" + count;
            PrivateKey key = (PrivateKey) pregeneratedKeys.getKey(alias, PASSPHRASE);
            KeyPair result;
            if (key == null) {
                result = super.generate(size);
                pregeneratedKeys.setKeyEntry(alias, result.getPrivate(), PASSPHRASE, new Certificate[] { createCertificate(result).getCertificate() });
                OutputStream output = new FileOutputStream(keyStoreFile);
                try {
                    pregeneratedKeys.store(output, PASSPHRASE);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            } else {
                Certificate certificate = pregeneratedKeys.getCertificateChain(alias)[0];
                result = new KeyPair(certificate.getPublicKey(), key);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static X509ResourceCertificate createCertificate(KeyPair keyPair) {
        X509ResourceCertificateBuilder builder = new X509ResourceCertificateBuilder();
        builder.withSignatureProvider("SunRsaSign");
        builder.withSerial(BigInteger.ONE);
        builder.withValidityPeriod(new ValidityPeriod(new DateTime().minusYears(2), new DateTime().minusYears(1)));
        builder.withCa(false);
        builder.withIssuerDN(new X500Principal("CN=issuer"));
        builder.withSubjectDN(new X500Principal("CN=subject"));
        builder.withResources(IpResourceSet.parse("AS1-AS10,10/8,ffc0::/16"));
        builder.withSigningKeyPair(keyPair);
        builder.withPublicKey(keyPair.getPublic());
        return builder.build();
    }
}
