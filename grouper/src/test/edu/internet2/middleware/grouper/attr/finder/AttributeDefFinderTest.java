/**
 * 
 */
package edu.internet2.middleware.grouper.attr.finder;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;


/**
 * @author mchyzer
 *
 */
public class AttributeDefFinderTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefFinderTest("testFindByIdIndex"));
  }
  
  /**
   * 
   * @param name
   */
  public AttributeDefFinderTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testFindByIdIndex() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef")
        .assignCreateParentStemsIfNotExist(true).save();

    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
    attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    AttributeDef found = AttributeDefFinder.findByIdIndexSecure(attributeDef.getIdIndex(), true, null);
    
    assertEquals(found.getName(), attributeDef.getName());
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    found = AttributeDefFinder.findByIdIndexSecure(attributeDef.getIdIndex(), false, null);
    
    assertNull(found);
    
    try {
      AttributeDefFinder.findByIdIndexSecure(attributeDef.getIdIndex(), true, null);
      fail("shouldnt get here");
    } catch (AttributeDefNotFoundException gnfe) {
      //good
    }
    
    try {
      AttributeDefFinder.findByIdIndexSecure(123456789L, true, null);
      fail("shouldnt get here");
    } catch (AttributeDefNotFoundException gnfe) {
      //good
    }

  }
}
