/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegateOptions;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
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
    this.role.addMember(SubjectTestHelper.SUBJ1, false);
    this.role.addMember(SubjectTestHelper.SUBJ2, false);
    this.role.addMember(SubjectTestHelper.SUBJ7, false);
   
  }

  /**
   * attribute def
   */
  public void testHibernateFail() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName);
    
    try {
      attributeAssign.saveOrUpdate();
      fail("Should throw exception");
    } catch (AttributeAssignNotAllowed aana) {
      //good
    }
    
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    //should work now
    attributeAssign.saveOrUpdate();
  }

  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName);
    
    //should work now
    attributeAssign.saveOrUpdate();
  }

  /**
   * attribute def
   */
  public void testDelegation() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.perm);

    AttributeDefName attributeDefName0 = this.top.addChildAttributeDefName(attributeDef, "testName0", "test name0");
    AttributeDefName attributeDefName1 = this.top.addChildAttributeDefName(attributeDef, "testName1", "test name1");
    AttributeDefName attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");

    //assign a direct assignment to the user
    //subj0 can delegate
    //subj1 cannot delegate
    //subj2 can grant delegation
    AttributeAssignResult attributeAssignResult0 = this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName0, SubjectTestHelper.SUBJ0);
    this.role.getPermissionRoleDelegate()
      .assignSubjectRolePermission(attributeDefName1, SubjectTestHelper.SUBJ1);
    AttributeAssignResult attributeAssignResult2 = this.role.getPermissionRoleDelegate()
    .assignSubjectRolePermission(attributeDefName2, SubjectTestHelper.SUBJ2);
    
    AttributeAssign attributeAssign0 = attributeAssignResult0.getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssignResult2.getAttributeAssign();
    
    attributeAssign0.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
    attributeAssign0.saveOrUpdate();
    
    attributeAssign2.setAttributeAssignDelegatable(AttributeAssignDelegatable.GRANT);
    attributeAssign2.saveOrUpdate();
    
    Member member7 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ7, true);
    
    //lets try to delegate with permissions
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, true, null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.startRootSession();
    
    PermissionEntry permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).iterator().next();
    assertTrue(permissionEntry != null);
    
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //try to grant, cant
    try {
      AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
      attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
      attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
          member7, true, attributeAssignDelegateOptions);
      fail();
    } catch (Exception e) {
      //ok
    }
    
    //ungrant
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName0, 
        member7, false, null);
    assertEquals(0, GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName0.getId()).size());

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    //try to delegate, cant
    try {
      this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName1, 
          member7, true, null);
      fail(); 
    } catch (Exception e) {
      //ok
    }

    //#############################################
    this.grouperSession.stop();
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    AttributeAssignDelegateOptions attributeAssignDelegateOptions = new AttributeAssignDelegateOptions();
    attributeAssignDelegateOptions.setAssignAttributeAssignDelegatable(true);
    attributeAssignDelegateOptions.setAttributeAssignDelegatable(AttributeAssignDelegatable.TRUE);

    //try to grant, can
    this.role.getPermissionRoleDelegate().delegateSubjectRolePermission(attributeDefName2, 
        member7, true, attributeAssignDelegateOptions);

    permissionEntry = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findByMemberIdAndAttributeDefNameId(member7.getUuid(), attributeDefName2.getId()).iterator().next();
    assertEquals(AttributeAssignDelegatable.TRUE, permissionEntry.getAttributeAssignDelegatable());
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
