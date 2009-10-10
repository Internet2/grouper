/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignEffMshipDelegate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.role.Role;

/**
 * @author mchyzer
 *
 */
public class AttributeAssignTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignTest("testHibernate"));
  }
  
  /**
   * 
   */
  public AttributeAssignTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /** top stem */
  private Role role;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
    this.role = this.top.addChildRole("role", "role");
    this.role.addMember(SubjectTestHelper.SUBJ0, false);
   
  }

  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName);
    attributeAssign.saveOrUpdate();
    
  }

  /**
   * attribute def
   */
  public void testDelegation() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");

    //assign a direct assignment to the user
    this.role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefName, SubjectTestHelper.SUBJ0);
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);
    AttributeAssignEffMshipDelegate attributeAssignEffMshipDelegate = ((Group)this.role).getAttributeDelegateEffMship(member);
    AttributeAssign attributeAssign = attributeAssignEffMshipDelegate.retrieveAssignments(attributeDefName).iterator().next();
    
    //lets try to delegate without permissions
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    
    
  }

  /**
   * attribute def
   */
  public void testHibernateDelegate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName);
    attributeAssign.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign.saveOrUpdate();
    
    
    
  }

}
