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
/**
 * @author mchyzer
 * $Id: GrouperKimUtilsTest.java,v 1.2 2009-12-20 18:03:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.util;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GrouperKimUtilsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperKimUtilsTest.class);
    TestRunner.run(new GrouperKimUtilsTest("testTranslateIds"));
  }

  /**
   * 
   */
  public GrouperKimUtilsTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimUtilsTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperClientUtils.grouperClientOverrideMap().clear();
  }

  /**
   * 
   */
  public void testCalculateNamespaceCode() {
    GrouperClientUtils.grouperClientOverrideMap().put("kim.stem", "a:b");
    assertEquals("c", GrouperKimUtils.calculateNamespaceCode("a:b:c:d"));
    assertEquals(null, GrouperKimUtils.calculateNamespaceCode("a:b:c"));
  }
  
  /**
   * translate and untranslate various ids
   */
  public void testTranslateIds() {

    GrouperClientUtils.grouperClientOverrideMap().put("kuali.identity.sourceSeparator", "::::");
    assertEquals("a", GrouperKimUtils.translatePrincipalId("a"));
    assertEquals("b", GrouperKimUtils.translatePrincipalId("a::::b"));
    assertEquals("a::::b", GrouperKimUtils.untranslatePrincipalId("a", "b"));
    assertEquals("a", GrouperKimUtils.separateSourceId("a::::b"));
    assertEquals(null, GrouperKimUtils.separateSourceId("a"));

    //test skip the prepend source
    GrouperClientUtils.grouperClientOverrideMap().put("kuali.identity.ignoreSourceAppend", "true");

    assertEquals("b", GrouperKimUtils.untranslatePrincipalId("a", "b"));
    
    GrouperClientUtils.grouperClientOverrideMap().remove("kuali.identity.ignoreSourceAppend");
    
    GrouperClientUtils.grouperClientOverrideMap().put("grouper.kim.kimEntityIdToSubjectId_c", "123");
    GrouperClientUtils.propertiesCacheClear();
    GrouperKimUtils.subjectIdToPrincipalIdCacheClear();
    
    assertEquals("123", GrouperKimUtils.translatePrincipalId("c"));
    assertEquals("c", GrouperKimUtils.untranslatePrincipalId("a", "123"));

    assertEquals("a::::admin", GrouperKimUtils.untranslatePrincipalId("a", "admin"));
    GrouperClientUtils.grouperClientOverrideMap().put("kuali.identity.ignoreSourceAppend.subjectIds", "admin");
    GrouperClientUtils.propertiesCacheClear();

    assertEquals("admin", GrouperKimUtils.untranslatePrincipalId("a", "admin"));
    
    
    
  }
  
  /**
   * 
   */
  public void testFirstLastMiddleName() {
    assertEquals("Hyzer", GrouperKimUtils.lastName("Hyzer"));
    assertEquals(null, GrouperKimUtils.firstName("Hyzer"));
    assertEquals(null, GrouperKimUtils.middleName("Hyzer"));

    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris Hyzer"));
    assertEquals(null, GrouperKimUtils.middleName("Chris Hyzer"));
  
    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris M. Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris M. Hyzer"));
    assertEquals("M.", GrouperKimUtils.middleName("Chris M. Hyzer"));

    assertEquals("Hyzer", GrouperKimUtils.lastName("Chris M. C. Hyzer"));
    assertEquals("Chris", GrouperKimUtils.firstName("Chris M. C. Hyzer"));
    assertEquals("M. C.", GrouperKimUtils.middleName("Chris M. C. Hyzer"));
}

}
