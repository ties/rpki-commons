/**
 * The BSD License
 *
 * Copyright (c) 2010-2020 RIPE NCC
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
package net.ripe.rpki.commons.provisioning.identity;

import net.ripe.rpki.commons.provisioning.x509.ProvisioningIdentityCertificate;
import net.ripe.rpki.commons.provisioning.x509.ProvisioningIdentityCertificateParser;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.xml.DomXmlSerializer;
import net.ripe.rpki.commons.xml.DomXmlSerializerException;
import org.w3c.dom.Document;

import java.util.Base64;
import java.util.Optional;


public abstract class IdentitySerializer<T> extends DomXmlSerializer<T> {

    public static final String XMLNS = "http://www.hactrn.net/uris/rpki/rpki-setup/";

    public IdentitySerializer() {
        super(XMLNS);
    }

    protected Optional<String> getBpkiElementContent(final Document doc, final String nodeName) {
        return getElement(doc, nodeName).map(e -> e.getTextContent().replaceAll("\\s+", ""));
    }

    protected ProvisioningIdentityCertificate getProvisioningIdentityCertificate(final String bpkiTa) {
        final ProvisioningIdentityCertificateParser parser = new ProvisioningIdentityCertificateParser();
        parser.parse(ValidationResult.withLocation("unknown.cer"), Base64.getMimeDecoder().decode(bpkiTa));
        return parser.getCertificate();
    }

    public static class IdentitySerializerException extends DomXmlSerializerException {
        public IdentitySerializerException(Exception e) {
            super(e);
        }

        public IdentitySerializerException(final String message) {
            super(message);
        }

        public IdentitySerializerException(final String message, final Exception e) {
            super(message, e);
        }
    }
}
