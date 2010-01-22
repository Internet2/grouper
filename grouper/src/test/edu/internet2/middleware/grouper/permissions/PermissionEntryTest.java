/**
 * @author Kate
 * $Id: PermissionEntryTest.java,v 1.5 2009-11-10 03:35:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;


/**
 *
 */
public class PermissionEntryTest extends GrouperTest {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionEntryTest("testAddLookup"));
  }

  /**
   * 
   */
  public PermissionEntryTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionEntryTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }

  /**
   * permission entry
   */
  public void testHibernate() {
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true); 
    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
      //System.out.println(permissionEntry);
      assertNotNull(permissionEntry);
    }
  }
  
  /**
   * 
   */
  public void testAddLookup() {
    Role role = this.top.addChildRole("test", "test");
    ((Group)role).addMember(SubjectTestHelper.SUBJ5);
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true); 
    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
      System.out.println(permissionEntry);
    }

    AttributeDefName attributeDefNameEff = this.top.addChildAttributeDefName(attributeDef, "testNameEff", "test name effective");
    role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefNameEff, member);
    
    permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
      System.out.println(permissionEntry);
    }
    
    
  }
  
}
