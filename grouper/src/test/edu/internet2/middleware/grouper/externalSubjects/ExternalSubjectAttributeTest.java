/**
 * Copyright 2014 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public class ExternalSubjectAttributeTest extends GrouperTest {

  /**
   * @param name
   */
  public ExternalSubjectAttributeTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new ExternalSubjectAttributeTest("testInvalidAttribute"));
    TestRunner.run(ExternalSubjectAttributeTest.class);
  }


  /** if we are testing jabber */
  private static boolean hasJabber = false;

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    hasJabber = StringUtils.equals(GrouperConfig.getProperty("externalSubjects.attributes.jabber.systemName"), "jabber");

    
  }
  
  /**
   * test insert/update/delete on external subject
   */
  public void testPersistence() {
    
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setEmail("a@b.c");
    externalSubject.setIdentifier("a@idp.example.edu");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.setDescription("My Description");
    externalSubject.store();
    
    if (hasJabber) {
      assertTrue(externalSubject.assignAttribute("jabber", "a@w.e"));
      assertFalse(externalSubject.assignAttribute("jabber", "a@w.e"));
      
    }

    //lets find subject by hib api
    //externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@idp.example.edu", true, null);
    
    if (hasJabber) {
      assertEquals("a@w.e", externalSubject.retrieveAttribute("jabber", true).getAttributeValue());
      assertEquals(1, GrouperUtil.length(externalSubject.retrieveAttributes()));
      assertEquals("a@w.e", externalSubject.retrieveAttributes().iterator().next().getAttributeValue());
      
      assertTrue(externalSubject.removeAttribute("jabber"));
      assertFalse(externalSubject.removeAttribute("jabber"));
  
      assertNull(externalSubject.retrieveAttribute("jabber", false));
    }
    
    //externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    externalSubject =  ExternalSubjectStorageController.findByIdentifier("a@idp.example.edu", true, null);
    
    if (hasJabber) {
      assertNull(externalSubject.retrieveAttribute("jabber", false));
  
      try {
        externalSubject.retrieveAttribute("jabber", true);
        fail("shouldnt get here");
      } catch (Exception e) {
        //this is ok
      }
    }
  }

  /**
   * test insert/update/delete on external subject
   */
  public void testInvalidAttribute() {
    
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setEmail("a@b.c");
    externalSubject.setIdentifier("a@idp.example.edu");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.setDescription("My Description");
    externalSubject.store();
    
    try {
      externalSubject.assignAttribute("jabber1", "a@w.e"); 
      fail("Shouldnt get here");
    } catch (Exception e) {
      assertTrue(ExceptionUtils.getFullStackTrace(e), ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("invalid attribute"));
    }
  }
  
}
