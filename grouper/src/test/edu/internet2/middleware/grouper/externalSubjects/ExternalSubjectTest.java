/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import org.apache.commons.lang.StringUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * @author mchyzer
 *
 */
public class ExternalSubjectTest extends GrouperTest {

  /**
   * @param name
   */
  public ExternalSubjectTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSubjectTest("testPersistence"));
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
    externalSubject.store();
  }
  
}
