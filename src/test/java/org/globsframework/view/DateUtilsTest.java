package org.globsframework.view;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {

    public void testName() {
        assertEquals("2023-01-01T00:00+01:00[Europe/Paris]", DateUtils.parse("2023-01-01[Europe/Paris]").toString());
        assertEquals("2023-06-01T00:00+02:00[Europe/Paris]", DateUtils.parse("2023-06-01[Europe/Paris]").toString());
        assertEquals("2023-01-01T00:00Z[Europe/London]", DateUtils.parse("2023-01-01[Europe/London]").toString());
        assertEquals("2023-01-01T00:00-06:00[America/Chicago]", DateUtils.parse("2023-01-01[America/Chicago]").toString());

        assertEquals("2023-01-01T15:00+01:00[Europe/Paris]", DateUtils.parse("2023-01-01T15:00[Europe/Paris]").toString());
        assertEquals("2023-01-01T15:00Z[Europe/London]", DateUtils.parse("2023-01-01T15:00[Europe/London]").toString());
        assertEquals("2023-01-01T15:00-06:00[America/Chicago]", DateUtils.parse("2023-01-01T15:00[America/Chicago]").toString());
    }
}