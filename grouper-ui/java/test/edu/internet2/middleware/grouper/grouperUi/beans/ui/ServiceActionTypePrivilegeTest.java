/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Security patch 119 (GRP-7472): the built-in UI templates (new service, policy group) run
 * ServiceActionType.grouperType and ServiceActionType.inheritedPrivilege with a root session.
 * The caller now needs STEM_ADMIN on the folder (or ADMIN on the group) first, and the stem
 * action needs STEM_ADMIN on a folder that already exists.  Same file for every release (no
 * Java 9+ APIs, extends GrouperTest which every release has).
 * Goes in grouper-ui/java/test/edu/internet2/middleware/grouper/grouperUi/beans/ui/
 */
public class ServiceActionTypePrivilegeTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ServiceActionTypePrivilegeTest.class);
  }

  /**
   * @param name
   */
  public ServiceActionTypePrivilegeTest(String name) {
    super(name);
  }

  /** folder where SUBJ0 has only CREATE */
  private Stem base;

  /** existing group in the folder, which SUBJ0 can read but not administer */
  private Group existingGroup;

  /** SUBJ0's own group, whose subject the inherited privilege would be granted to */
  private Group mine0;

  /** SUBJ1's own group, for the positive control */
  private Group mine1;

  /**
   * build the folder and groups as root
   */
  private void setupObjects() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      this.base = new StemSave(rootSession).assignName("test119:base").assignCreateParentStemsIfNotExist(true).save();
      this.existingGroup = new GroupSave(rootSession).assignName("test119:base:existingGroup").save();
      this.mine0 = new GroupSave(rootSession).assignName("test119:mine0").assignCreateParentStemsIfNotExist(true).save();
      this.mine1 = new GroupSave(rootSession).assignName("test119:mine1").assignCreateParentStemsIfNotExist(true).save();

      // SUBJ0: CREATE on the folder (enough to open the template screen), READ/UPDATE on the existing group
      this.base.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
      this.existingGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.existingGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
      this.mine0.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      // SUBJ1: STEM_ADMIN on the folder, ADMIN on its own group
      this.base.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
      this.mine1.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * a service action of a type with these name/value args
   * @param serviceActionType
   * @param nameValues name, value, name, value...
   * @return the action
   */
  private static ServiceAction serviceAction(ServiceActionType serviceActionType, String... nameValues) {
    ServiceAction serviceAction = new ServiceAction();
    serviceAction.setId("test119_" + serviceActionType.name());
    serviceAction.setServiceActionType(serviceActionType);
    List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    for (int i = 0; i < nameValues.length; i += 2) {
      args.add(new ServiceActionArgument(nameValues[i], nameValues[i + 1]));
    }
    serviceAction.setArgs(args);
    return serviceAction;
  }

  /**
   * inherited privilege rule on a folder: refused for a user with only CREATE, allowed for STEM_ADMIN
   */
  public void testInheritedPrivilegeNeedsStemAdmin() {
    setupObjects();

    GrouperSession session0 = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ServiceActionType.inheritedPrivilege.createTemplateItem(serviceAction(ServiceActionType.inheritedPrivilege,
          "groupName", "test119:mine0", "parentStemName", "test119:base",
          "internalPrivilegeName", "admin", "templateItemType", "Groups"));
      fail("SUBJ0 has only CREATE on the folder and must not be able to add an inherited privilege rule");
    } catch (InsufficientPrivilegeException ipe) {
      // expected
    } finally {
      GrouperSession.stopQuietly(session0);
    }

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // no rule was made, so SUBJ0's group did not get ADMIN on the existing group
      assertFalse(this.existingGroup.hasAdmin(this.mine0.toSubject()));
      assertFalse(this.existingGroup.hasAdmin(SubjectTestHelper.SUBJ0));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // positive control: STEM_ADMIN may do it, and the rule grants ADMIN on the existing group
    GrouperSession session1 = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      ServiceActionType.inheritedPrivilege.createTemplateItem(serviceAction(ServiceActionType.inheritedPrivilege,
          "groupName", "test119:mine1", "parentStemName", "test119:base",
          "internalPrivilegeName", "admin", "templateItemType", "Groups"));
    } finally {
      GrouperSession.stopQuietly(session1);
    }

    rootSession = GrouperSession.startRootSession();
    try {
      assertTrue(this.existingGroup.hasAdmin(this.mine1.toSubject()));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * object type on a folder or a group: refused without STEM_ADMIN / ADMIN
   */
  public void testGrouperTypeNeedsAdmin() {
    setupObjects();

    int stemAssignsBefore = assignmentCount(this.base, null);
    int groupAssignsBefore = assignmentCount(null, this.existingGroup);

    GrouperSession session0 = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      try {
        ServiceActionType.grouperType.createTemplateItem(serviceAction(ServiceActionType.grouperType,
            "stemName", "test119:base", "type", "app"));
        fail("SUBJ0 has only CREATE on the folder and must not be able to assign an object type to it");
      } catch (InsufficientPrivilegeException ipe) {
        // expected
      }
      try {
        ServiceActionType.grouperType.createTemplateItem(serviceAction(ServiceActionType.grouperType,
            "groupName", "test119:base:existingGroup", "type", "ref"));
        fail("SUBJ0 has only READ/UPDATE on the group and must not be able to assign an object type to it");
      } catch (InsufficientPrivilegeException ipe) {
        // expected
      }
      try {
        // an existing folder: the template's stem action must not carry on for a non-admin
        ServiceActionType.stem.createTemplateItem(serviceAction(ServiceActionType.stem,
            "stemName", "test119:base", "stemDisplayName", "test119:base", "stemDescription", "x"));
        fail("SUBJ0 has only CREATE on the existing folder");
      } catch (InsufficientPrivilegeException ipe) {
        // expected
      }
    } finally {
      GrouperSession.stopQuietly(session0);
    }

    assertEquals(stemAssignsBefore, assignmentCount(this.base, null));
    assertEquals(groupAssignsBefore, assignmentCount(null, this.existingGroup));

    // positive control: STEM_ADMIN may assign a type to the folder
    GrouperSession session1 = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      ServiceActionType.grouperType.createTemplateItem(serviceAction(ServiceActionType.grouperType,
          "stemName", "test119:base", "type", "app"));
      ServiceActionType.stem.createTemplateItem(serviceAction(ServiceActionType.stem,
          "stemName", "test119:base", "stemDisplayName", "test119:base", "stemDescription", "x"));
    } finally {
      GrouperSession.stopQuietly(session1);
    }
    assertTrue(assignmentCount(this.base, null) > stemAssignsBefore);
  }

  /**
   * attribute assignments on a folder or group, read as root
   * @param stem
   * @param group
   * @return the count
   */
  private static int assignmentCount(Stem stem, Group group) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return stem != null ? GrouperUtil.length(stem.getAttributeDelegate().retrieveAssignments())
          : GrouperUtil.length(group.getAttributeDelegate().retrieveAssignments());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }
}
