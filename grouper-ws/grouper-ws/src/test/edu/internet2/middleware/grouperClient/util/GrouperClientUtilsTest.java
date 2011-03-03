/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;


/**
 *
 */
public class GrouperClientUtilsTest extends TestCase {

  /**
   * @param name
   */
  public GrouperClientUtilsTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testSubstituteVars() {
    Map<String, Object> substituteMap = new HashMap<String, Object>();
    
    substituteMap.put("string", "theString");
    
    assertNull(GrouperClientUtils.substituteExpressionLanguage(null, substituteMap));
    
    assertEquals("", GrouperClientUtils.substituteExpressionLanguage("", substituteMap));
    
    assertEquals("abc", GrouperClientUtils.substituteExpressionLanguage("abc", substituteMap));
    
    //non existant vars go to null
    assertEquals("abc null", GrouperClientUtils.substituteExpressionLanguage("abc ${notThere}", substituteMap));

    assertEquals("abc theString theString", 
        GrouperClientUtils.substituteExpressionLanguage("abc ${string} ${string}", substituteMap));
    
    assertEquals("abc theString\ntheString", 
        GrouperClientUtils.substituteExpressionLanguage("abc ${string}\n${string}", substituteMap));
    
    assertEquals("abc theString 9", 
        GrouperClientUtils.substituteExpressionLanguage("abc ${string} ${string.length()}", substituteMap));
  }
  
}
