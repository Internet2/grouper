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
    TestRunner.run(new ExternalSubjectAttributeTest("testInvalidAttribute"));
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    if (!StringUtils.equals(GrouperConfig.getProperty("externalSubjects.attributes.jabber.systemName"), "jabber")) {
      fail("You need the external subject attribute jabber set in grouper.properties");
    }
    
  }
  
  /**
   * test insert/update/delete on external subject
   */
  public void testPersistence() {
    
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setEmail("a@b.c");
    externalSubject.setIdentifier("a@id.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.setDescription("My Description");
    externalSubject.store();
    
    assertTrue(externalSubject.assignAttribute("jabber", "a@w.e"));
    assertFalse(externalSubject.assignAttribute("jabber", "a@w.e"));

    //lets find subject by hib api
    //externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@id.b.c", true, null);
    
    assertEquals("a@w.e", externalSubject.retrieveAttribute("jabber", true).getAttributeValue());
    assertEquals(1, GrouperUtil.length(externalSubject.retrieveAttributes()));
    assertEquals("a@w.e", externalSubject.retrieveAttributes().iterator().next().getAttributeValue());
    
    assertTrue(externalSubject.removeAttribute("jabber"));
    assertFalse(externalSubject.removeAttribute("jabber"));

    assertNull(externalSubject.retrieveAttribute("jabber", false));
    
    //externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    externalSubject =  ExternalSubjectStorageController.findByIdentifier("a@id.b.c", true, null);
    
    assertNull(externalSubject.retrieveAttribute("jabber", false));

    try {
      externalSubject.retrieveAttribute("jabber", true);
      fail("shouldnt get here");
    } catch (Exception e) {
      //this is ok
    }
  }

  /**
   * test insert/update/delete on external subject
   */
  public void testInvalidAttribute() {
    
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setEmail("a@b.c");
    externalSubject.setIdentifier("a@id.b.c");
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
