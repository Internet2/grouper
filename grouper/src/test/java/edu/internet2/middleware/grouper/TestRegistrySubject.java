/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class TestRegistrySubject extends GrouperTest {

  /**
   * 
   */
  public TestRegistrySubject() {
  }

  /**
   * @param name
   */
  public TestRegistrySubject(String name) {
    super(name);

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestRegistrySubject("testSubjects"));
  }

  /**
   * 
   */
  public void testSubjects() {
    
    //see if the builtins are ok
    RegistrySubject registrySubject = RegistrySubject.find("test.subject.0", false);
    assertNotNull(registrySubject);
    assertEquals("id.test.subject.0", registrySubject.getAttributeValue("loginid"));
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    registrySubject = RegistrySubject.add(grouperSession, "testId", "person", "test id name");
    
    assertEquals("testId", registrySubject.getId());
    assertEquals("person", registrySubject.getTypeString());
    assertEquals("test id name", registrySubject.getName());
    assertEquals("name.testId", registrySubject.getAttributeValue("name"));
    assertEquals("description.testId", registrySubject.getAttributeValue("description"));
    assertEquals("id.testId", registrySubject.getAttributeValue("loginid"));
    assertEquals("testId@somewhere.someSchool.edu", registrySubject.getAttributeValue("email"));
    
    registrySubject = RegistrySubject.find("testId", false);
    assertEquals("id.testId", registrySubject.getAttributeValue("loginid"));
    
    Subject subject = SubjectFinder.findById("testId", true);
    assertEquals("id.testId", subject.getAttributeValue("loginid"));
    
    RegistrySubjectAttribute registrySubjectAttribute = GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().find("testId", "loginid", true);
    assertEquals("id.testId", registrySubjectAttribute.getValue());
    assertEquals("id.testid", registrySubjectAttribute.getSearchValue());
    registrySubjectAttribute.delete();
    
    registrySubjectAttribute = RegistrySubjectAttribute.addOrUpdate("testId", "myAttribute", "someValue");
    assertEquals("someValue", RegistrySubjectAttribute.find("testId", "myAttribute", true).getValue());
    registrySubjectAttribute.delete();
    
    registrySubject.delete(grouperSession);
    
    registrySubject = RegistrySubject.find("testId", false);
    
    assertNull(registrySubject);
    
    try {
      
      registrySubject = RegistrySubject.find("testId", true);
      fail("Shouldnt find");
    } catch (Throwable t) {
      
    }
    
  }
  
}
