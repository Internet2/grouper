package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Regression test for the reflected XSS in the configuration editor and configuration history
 * filter boxes (security issue configurationFilterReflectedXss).  configure.jsp and history.jsp
 * print ConfigurationContainer.getFilter() into a double-quoted HTML value attribute with plain
 * EL, and the filter comes from the request, so the getter must return the HTML-escaped value.
 * Plain JUnit, no database or servlet needed.
 */
public class ConfigurationContainerFilterEscapeTest extends TestCase {

  /**
   * run from the command line or Eclipse
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ConfigurationContainerFilterEscapeTest.class);
  }

  /**
   * @param name
   */
  public ConfigurationContainerFilterEscapeTest(String name) {
    super(name);
  }

  /**
   * the attribute-breakout payload from the report must come back encoded
   */
  public void testAttributeBreakoutIsEscaped() {
    ConfigurationContainer configurationContainer = new ConfigurationContainer();
    configurationContainer.setFilter("\"><img src=x onerror=alert(1)>");
    String filter = configurationContainer.getFilter();
    assertTrue(filter, filter.contains("&quot;&gt;&lt;img"));
    // nothing that can close the attribute or open a tag survives
    assertFalse(filter, filter.contains("\""));
    assertFalse(filter, filter.contains("<"));
    assertFalse(filter, filter.contains(">"));
  }

  /**
   * single quotes and ampersands are escaped too (in case a JSP variant single-quotes the attribute)
   */
  public void testSingleQuoteAndAmpersand() {
    ConfigurationContainer configurationContainer = new ConfigurationContainer();
    configurationContainer.setFilter("a'b&c");
    String filter = configurationContainer.getFilter();
    assertFalse(filter, filter.contains("'"));
    assertTrue(filter, filter.contains("&amp;"));
    assertFalse(filter, filter.contains("&c"));
  }

  /**
   * normal filters and null are unchanged
   */
  public void testPlainAndNull() {
    ConfigurationContainer configurationContainer = new ConfigurationContainer();
    assertNull(configurationContainer.getFilter());
    configurationContainer.setFilter("grouper.env.name");
    assertEquals("grouper.env.name", configurationContainer.getFilter());
  }

}
