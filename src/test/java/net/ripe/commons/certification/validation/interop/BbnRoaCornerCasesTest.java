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
package net.ripe.commons.certification.validation.interop;

import net.ripe.commons.certification.cms.roa.RoaCmsParser;
import net.ripe.commons.certification.validation.ValidationLocation;
import net.ripe.commons.certification.validation.ValidationResult;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;

@Ignore
public class BbnRoaCornerCasesTest {

    private static final String PATH_TO_BBN_ROAS = "src/test/resources/bbn-psycho-cases/root";


    @Test
    public void shouldRejectBadRoaVersionV2Roa() throws IOException {
        byte[] encoded = FileUtils.readFileToByteArray(new File(PATH_TO_BBN_ROAS + "/badROAVersionV2.roa"));

//        System.err.println(ASN1Dump.dumpAsString(Asn1Util.decode(encoded)));

        RoaCmsParser parser = new RoaCmsParser();
        parser.parse("roa", encoded);
        ValidationResult validationResult = parser.getValidationResult();

        for (ValidationLocation location: validationResult.getValidatedLocations()) {
            System.err.println(location + "  ->  " + validationResult.getFailures(location));
        }

        assertFalse(validationResult.hasFailures());
    }

    @Test
    public void shouldAcceptGoodRoa() throws IOException {
        byte[] encoded = FileUtils.readFileToByteArray(new File(PATH_TO_BBN_ROAS + "/goodROANothingWrong.roa"));


//        System.err.println(ASN1Dump.dumpAsString(Asn1Util.decode(encoded)));

        RoaCmsParser parser = new RoaCmsParser();
        parser.parse("roa", encoded);
        ValidationResult validationResult = parser.getValidationResult();

        for (ValidationLocation location: validationResult.getValidatedLocations()) {
            System.err.println(location + "  ->  " + validationResult.getFailures(location));
        }

        assertFalse(validationResult.hasFailures());
    }

}
