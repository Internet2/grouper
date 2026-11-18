package edu.internet2.middleware.grouper.filter;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * WS findGroups with queryFilterType FIND_BY_TYPE runs GroupTypeFilter, which returned every group
 * with the legacy group type from the unsecured Hib3GroupDAO.findAllByType, whatever the caller's
 * privileges.  The fix keeps only the groups where the caller may see the type assignment: ATTR_READ
 * on the type's attribute definition and GROUP_ATTR_READ (or ADMIN) on the group, the same rule as
 * Group.getTypes(true).
 */
public class GroupTypeFilterSecurityTest extends GrouperTest {

  /** group with the type */
  private static final String GROUP_NAME = "test190:secretFolder:secretGroup";

  /** legacy group type */
  private static final String TYPE_NAME = "test190GroupType";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupTypeFilterSecurityTest("testFindByTypeNeedsAttributeRead"));
  }

  /**
   *
   */
  public GroupTypeFilterSecurityTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupTypeFilterSecurityTest(String name) {
    super(name);
  }

  /**
   * so the test subjects only have the privileges granted below
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /**
   * as root: a private group with the type.  SUBJ0 nothing, SUBJ1 GROUP_ATTR_READ on the group and
   * ATTR_READ on the type definition, SUBJ2 GROUP_ATTR_READ (and VIEW, READ) on the group only
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      GroupType groupType = GroupTypeFinder.find(TYPE_NAME, false);
      if (groupType == null) {
        groupType = GroupType.createType(rootSession, TYPE_NAME);
      }
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      group.addType(groupType);

      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ, false);
      AttributeDef typeDef = groupType.getAttributeDefName().getAttributeDef();
      typeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);

      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * run the FIND_BY_TYPE filter as a subject from the root folder
   * @param subject caller
   * @return names of the groups returned
   */
  private static Set<String> findByTypeAs(Subject subject) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      GroupType groupType = GroupTypeFinder.find(TYPE_NAME, true);
      Set<String> names = new HashSet<String>();
      for (Group group : GrouperQuery.createQuery(grouperSession,
          new GroupTypeFilter(groupType, StemFinder.findRootStem(grouperSession))).getGroups()) {
        names.add(group.getName());
      }
      return names;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * no privilege: nothing; GROUP_ATTR_READ on the group but no ATTR_READ on the type: nothing;
   * both: the group; root: the group
   */
  public void testFindByTypeNeedsAttributeRead() {
    assertFalse(findByTypeAs(SubjectTestHelper.SUBJ0).contains(GROUP_NAME));
    assertFalse(findByTypeAs(SubjectTestHelper.SUBJ2).contains(GROUP_NAME));
    assertTrue(findByTypeAs(SubjectTestHelper.SUBJ1).contains(GROUP_NAME));
    assertTrue(findByTypeAs(SubjectFinder.findRootSubject()).contains(GROUP_NAME));
  }
}
