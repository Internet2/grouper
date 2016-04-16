/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
package edu.internet2.middleware.tierApiAuthzServer.util;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

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
    TestRunner.run(new StandardApiServerUtilsTest("testPathParentFolderName"));
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
  
  /**
   * 
   */
  public void testConvertPathToList() {
    List<String> extensionList = StandardApiServerUtils.convertPathToExtensionList("a:b:c");
    
    assertEquals(3, extensionList.size());
    assertEquals("a", extensionList.get(0));
    assertEquals("b", extensionList.get(1));
    assertEquals("c", extensionList.get(2));
    
    String path = StandardApiServerUtils.convertPathFromExtensionList(extensionList);
    
    assertEquals("a:b:c", path);
    
    //#######################
    extensionList = StandardApiServerUtils.convertPathToExtensionList("a");
    
    assertEquals(1, extensionList.size());
    assertEquals("a", extensionList.get(0));
    
    path = StandardApiServerUtils.convertPathFromExtensionList(extensionList);
    
    assertEquals("a", path);
    
    //########################
    extensionList = StandardApiServerUtils.convertPathToExtensionList("a\\:\\::a\\\\b:\\\\\\:c");
    
    assertEquals(3, extensionList.size());
    assertEquals("a::", extensionList.get(0));
    assertEquals("a\\b", extensionList.get(1));
    assertEquals("\\:c", extensionList.get(2));
    
    path = StandardApiServerUtils.convertPathFromExtensionList(extensionList);
    
    assertEquals("a\\:\\::a\\\\b:\\\\\\:c", path);
    
    
  }
  
  /**
   * 
   */
  public void testPathParentFolderName() {
    
    assertEquals("a:b", StandardApiServerUtils.pathParentFolderName("a:b:c"));

    assertEquals(":", StandardApiServerUtils.pathParentFolderName("a"));

    assertNull(StandardApiServerUtils.pathParentFolderName(":"));
    
  }
  
  /**
   * 
   */
  public void testFullUrlToServletUrl() {
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet/whatever/whatever", "/servlet", AsasRestContentType.json));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet", "/servlet", AsasRestContentType.json));
    assertEquals("https://whatever/appName/servlet", StandardApiServerUtils.fullUrlToServletUrl("https://whatever/appName/servlet/", "/servlet", AsasRestContentType.json));
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
