package edu.internet2.middleware.grouper.privs;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

public class PrivilegeHelperLoaderEditTest extends GrouperTest {

  public PrivilegeHelperLoaderEditTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(PrivilegeHelperLoaderEditTest.class);
  }

  private Group targetGroup;
  private Group restrictGroup;
  private Subject subj0;
  private Subject subj1;

  public void setUp() {
    super.setUp();
    GrouperSession.startRootSession();

    targetGroup = new GroupSave().assignName("test:targetGroup").assignCreateParentStemsIfNotExist(true).save();
    restrictGroup = new GroupSave().assignName("test:abacEditors").assignCreateParentStemsIfNotExist(true).save();

    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;

    // give subj0 admin on the target group
    targetGroup.grantPriv(subj0, AccessPrivilege.ADMIN);

    // subj1 has no privileges on the target group
  }

  public void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.edit.if.in.group");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.recentMemberships.edit.if.in.group");
    super.tearDown();
  }

  // ===== ABAC loader tests =====

  public void testCanEditAbacLoader_wheelAlwaysAllowed() {
    Subject root = SubjectFinder.findRootSubject();
    assertTrue(PrivilegeHelper.canEditAbacLoader(root, targetGroup));
  }

  public void testCanEditAbacLoader_groupAdminAllowed_noRestrictGroup() {
    assertTrue(PrivilegeHelper.canEditAbacLoader(subj0, targetGroup));
  }

  public void testCanEditAbacLoader_nonAdminDenied() {
    assertFalse(PrivilegeHelper.canEditAbacLoader(subj1, targetGroup));
  }

  public void testCanEditAbacLoader_groupAdminAndInRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "test:abacEditors");
    restrictGroup.addMember(subj0);
    assertTrue(PrivilegeHelper.canEditAbacLoader(subj0, targetGroup));
  }

  public void testCanEditAbacLoader_groupAdminNotInRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "test:abacEditors");
    assertFalse(PrivilegeHelper.canEditAbacLoader(subj0, targetGroup));
  }

  public void testCanEditAbacLoader_nonAdminInRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "test:abacEditors");
    restrictGroup.addMember(subj1);
    assertFalse(PrivilegeHelper.canEditAbacLoader(subj1, targetGroup));
  }

  public void testCanEditAbacLoader_wheelAllowedEvenWithRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "test:abacEditors");
    Subject root = SubjectFinder.findRootSubject();
    assertTrue(PrivilegeHelper.canEditAbacLoader(root, targetGroup));
  }

  // ===== Recent memberships loader tests =====

  public void testCanEditRecentMembershipsLoader_wheelAlwaysAllowed() {
    Subject root = SubjectFinder.findRootSubject();
    assertTrue(PrivilegeHelper.canEditRecentMembershipsLoader(root, targetGroup));
  }

  public void testCanEditRecentMembershipsLoader_groupAdminAllowed_noRestrictGroup() {
    assertTrue(PrivilegeHelper.canEditRecentMembershipsLoader(subj0, targetGroup));
  }

  public void testCanEditRecentMembershipsLoader_nonAdminDenied() {
    assertFalse(PrivilegeHelper.canEditRecentMembershipsLoader(subj1, targetGroup));
  }

  public void testCanEditRecentMembershipsLoader_groupAdminAndInRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.recentMemberships.edit.if.in.group", "test:abacEditors");
    restrictGroup.addMember(subj0);
    assertTrue(PrivilegeHelper.canEditRecentMembershipsLoader(subj0, targetGroup));
  }

  public void testCanEditRecentMembershipsLoader_groupAdminNotInRestrictGroup() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.recentMemberships.edit.if.in.group", "test:abacEditors");
    assertFalse(PrivilegeHelper.canEditRecentMembershipsLoader(subj0, targetGroup));
  }

}
