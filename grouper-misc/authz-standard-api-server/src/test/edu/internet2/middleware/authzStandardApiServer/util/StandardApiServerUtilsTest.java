package edu.internet2.middleware.authzStandardApiServer.util;

import java.util.Calendar;
import java.util.Date;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class StandardApiServerUtilsTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new StandardApiServerUtilsTest("testFullUrlToServletUrl"));
  }
  
  /**
   * 
   */
  public StandardApiServerUtilsTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public StandardApiServerUtilsTest(String name) {
    super(name);
  }
  
  public void testFullUrlToServletUrl() {
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet/whatever/whatever", "/servlet", AsasRestContentType.json));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet", "/servlet", AsasRestContentType.json));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet/", "/servlet", AsasRestContentType.json));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet.xml", "/servlet.xml", AsasRestContentType.xml));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet.json", "/servlet.json", AsasRestContentType.json));
    

  }
  
  /**
   * 
   */
  public void testConvertToIso8601() {
    
    Date date = StandardApiServerUtils.dateValue("2001/02/03 04:05:06.789");
    String dateString = StandardApiServerUtils.convertToIso8601(date);
    assertEquals("2001-02-03T04:05:06.789Z", dateString);
  }
  
}
