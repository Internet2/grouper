package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.helper.GrouperTest;

public class PspUtilsTest extends GrouperTest {

  public void testFormatWithSignificantDigits() {
    assertEquals("1.2", PspUtils.formatWithSignificantDigits(1.23314, 2));
    assertEquals("1.2", PspUtils.formatWithSignificantDigits(1.1532, 2));
    assertEquals("120", PspUtils.formatWithSignificantDigits(123.314, 2));
    assertEquals("0.0012", PspUtils.formatWithSignificantDigits(0.00123314, 2));
  }
}