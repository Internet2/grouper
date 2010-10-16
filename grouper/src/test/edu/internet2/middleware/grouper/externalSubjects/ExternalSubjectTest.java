/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import org.apache.commons.lang.StringUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.Subject;


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
    externalSubject.setDescription("My Description");
    externalSubject.store();
    
    String uuid = externalSubject.getUuid();
    
    assertTrue(!StringUtils.isBlank(uuid));
    
    //lets find the subject by subject api
    Subject subject = SubjectFinder.findByIdentifier("a@id.b.c", true);
    
    assertEquals("My Name", subject.getName());
    assertEquals("My Description", subject.getDescription());
    assertEquals(uuid, subject.getId());
    
    //lets find subject by hib api
    externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    
    assertEquals("My Name", externalSubject.getName());
    assertEquals("My Description", externalSubject.getDescription());
    assertEquals(uuid, externalSubject.getUuid());
    assertEquals("My Institution", externalSubject.getInstitution());
    assertEquals("a@b.c", externalSubject.getEmail());
    
    
    //lets update it
    externalSubject.setName("New Name");
    externalSubject.store();

    externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", true, null);
    
    assertEquals("New Name", externalSubject.getName());
    
    //lets delete it
    externalSubject.delete();

    externalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier("a@id.b.c", false, null);

    assertNull(externalSubject);
    
  }
  
}
