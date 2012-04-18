/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
