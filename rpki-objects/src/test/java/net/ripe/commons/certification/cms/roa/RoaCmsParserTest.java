package net.ripe.commons.certification.cms.roa;

import static net.ripe.commons.certification.Asn1Util.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.ripe.ipresource.Asn;
import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.IpResourceType;

import org.bouncycastle.asn1.DERTags;
import org.junit.Before;
import org.junit.Test;

public class RoaCmsParserTest {

    public static final Asn TEST_ASN = new Asn(Long.valueOf(42));

    public static final RoaPrefix TEST_IPV4_PREFIX_1 = new RoaPrefix(IpRange.parse("10.64.0.0/12"), 24);
    public static final RoaPrefix TEST_IPV4_PREFIX_2 = new RoaPrefix(IpRange.parse("10.32.0.0/12"), null);
    public static final RoaPrefix TEST_IPV6_PREFIX = new RoaPrefix(IpRange.parse("2001:0:200::/39"), null);

    public static final byte[] ENCODED_ROA_IP_ADDRESS = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
            DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x40, // 10.64.0.0/12
            DERTags.INTEGER, 0x01, 0x18
    };

    public static final byte[] ENCODED_ROA_IP_ADDRESS_2 = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x05,
            DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x20 // 10.32.0.0/12
    };

    public static final byte[] ENCODED_ROA_IP_ADDRESS_FAMILY = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x17,
            DERTags.OCTET_STRING, 0x02, 0x00, 0x01, // IPv4
            DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x11,
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
                    DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x40, // 10.64.0.0/12
                    DERTags.INTEGER, 0x01, 0x18,
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x05,
                    DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x20 // 10.32.0.0/12
    };

    public static final byte[] ENCODED_ROA_IP_ADDRESS_FAMILY_SEQUENCE_IPV4 = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x19,
            DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x17,
                DERTags.OCTET_STRING, 0x02, 0x00, 0x01, // IPv4
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x11,
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
                        DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x40, // 10.64.0.0/12
                        DERTags.INTEGER, 0x01, 0x18,
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x05,
                        DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x20 // 10.32.0.0/12
    };

    public static final byte[] ENCODED_ROA_IP_ADDRESS_FAMILY_SEQUENCE_ALL = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x2b,
            DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x17,
                DERTags.OCTET_STRING, 0x02, 0x00, 0x01, // IPv4
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x11,
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
                        DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x40, // 10.64.0.0/12
                        DERTags.INTEGER, 0x01, 0x18,
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x05,
                        DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x20, // 10.32.0.0/12
            DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x10,
                DERTags.OCTET_STRING, 0x02, 0x00, 0x02, // IPv6
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x0a,
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
                        DERTags.BIT_STRING, 0x06, 0x01, 0x20, 0x01, 0x00, 0x00, 0x02 // 2001:0:200::/39
    };

    public static final byte[] ENCODED_ROUTE_ORIGIN_ATTESTATION = {
        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x1e,
//            (byte) (DERTags.TAGGED | DERTags.CONSTRUCTED | 0), 0x03, // Tag 0
//                DERTags.INTEGER, 0x01, 0x00, // version: 0
            DERTags.INTEGER, 0x01, 0x2a, // AS42
            DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x19,
                DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x17,
                    DERTags.OCTET_STRING, 0x02, 0x00, 0x01, // IPv4
                    DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x11,
                        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x08,
                            DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x40, // 10.64.0.0/12
                            DERTags.INTEGER, 0x01, 0x18,
                        DERTags.SEQUENCE | DERTags.CONSTRUCTED, 0x05,
                            DERTags.BIT_STRING, 0x03, 0x04, 0x0a, 0x20 // 10.32.0.0/12
    };

	private RoaCmsParser parser;

	private List<RoaPrefix> ipv4Prefixes;
    private List<RoaPrefix> allPrefixes;

    @Before
    public void setUp() {
        parser = new RoaCmsParser();
        ipv4Prefixes = new ArrayList<RoaPrefix>();
        ipv4Prefixes.add(TEST_IPV4_PREFIX_1);
        ipv4Prefixes.add(TEST_IPV4_PREFIX_2);
        allPrefixes = new ArrayList<RoaPrefix>(ipv4Prefixes);
        allPrefixes.add(TEST_IPV6_PREFIX);
        parser.parse("test", RoaCmsTest.createRoaCms(allPrefixes).getEncoded());
    }

	@Test
	public void shouldParseRoaIpAddress() {
		assertEquals(TEST_IPV4_PREFIX_1, parser.parseRoaIpAddressFamily(IpResourceType.IPv4, decode(ENCODED_ROA_IP_ADDRESS)));
		assertEquals(TEST_IPV4_PREFIX_2, parser.parseRoaIpAddressFamily(IpResourceType.IPv4, decode(ENCODED_ROA_IP_ADDRESS_2)));
	}

	@Test
	public void shouldParseRoaIpAddressFamily() {
		List<RoaPrefix> result = new ArrayList<RoaPrefix>();
		parser.parseRoaIpAddressFamily(result, decode(ENCODED_ROA_IP_ADDRESS_FAMILY));
		assertEquals(ipv4Prefixes, result);
	}

	@Test
	public void shouldParseRoaIpAddressFamilySequence() {
		assertEquals(ipv4Prefixes, parser.parseRoaIpAddressFamilySequence(decode(ENCODED_ROA_IP_ADDRESS_FAMILY_SEQUENCE_IPV4)));
		assertEquals(allPrefixes, parser.parseRoaIpAddressFamilySequence(decode(ENCODED_ROA_IP_ADDRESS_FAMILY_SEQUENCE_ALL)));
	}

	@Test
	public void shouldParseRouteOriginAttestation() {
		parser.parseRouteOriginAttestation(decode(ENCODED_ROUTE_ORIGIN_ATTESTATION));
		RoaCms roa = parser.getRoaCms();
		assertEquals(TEST_ASN, roa.getAsn());
		assertEquals(ipv4Prefixes, roa.getPrefixes());
	}

}
