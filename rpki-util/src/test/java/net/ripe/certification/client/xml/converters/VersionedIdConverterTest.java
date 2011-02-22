package net.ripe.certification.client.xml.converters;

import static org.junit.Assert.*;
import net.ripe.utils.VersionedId;

import org.junit.Test;


public class VersionedIdConverterTest {

    private VersionedIdConverter subject = new VersionedIdConverter();

    @Test
    public void shouldSupportVersionedId() {
        assertTrue(subject.canConvert(VersionedId.class));
    }

    @Test
    public void shouldParseVersionId() {
        assertEquals(new VersionedId(13, 0), subject.fromString("13"));
        assertEquals(new VersionedId(99, 0), subject.fromString("99:0"));
        assertEquals(new VersionedId(99, 13), subject.fromString("99:13"));
        assertEquals(new VersionedId(42), subject.fromString("42:-1"));
    }

    @Test
    public void shouldFormatVersionedId() {
        assertEquals("13:0", subject.toString(new VersionedId(13, 0)));
        assertEquals("42:-1", subject.toString(new VersionedId(42)));
        assertEquals("99:13", subject.toString(new VersionedId(99, 13)));
    }

}
