package edu.internet2.middleware.grouper.privs;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.subject.Subject;

/**
 * The group privilege lists (admins, updaters, readers, viewers, ...) need ADMIN on the group to read,
 * but the bulk membership queries took READ unless the FIRST field asked for was a privilege list.
 * So a caller with only READ on a group could list who administers it (memberships with fieldType
 * access or serviceRole admin), or learn every READ-able group where a subject is an admin (getGroups
 * with fieldName admins, current and point in time, services where a subject is an admin).
 *
 * Setup: SUBJ4 is an admin of the group, SUBJ2 has VIEW and READ, SUBJ3 has ADMIN, SUBJ5 is a member.
 * The group is in a folder tagged with a service.
 */
public class AccessPrivilegeListReadTest extends GrouperTest {

  /** group under test */
  private static final String GROUP_NAME = "test172:serviceFolder:privLeakGroup";

  /** second group (same privileges), so the multi-group HQL path is used too */
  private static final String GROUP2_NAME = "test172:serviceFolder:privLeakGroup2";

  /** service folder */
  private static final String SERVICE_FOLDER = "test172:serviceFolder";

  /** service name */
  private static final String SERVICE_NAME = "test172:serviceFolder:myService172";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AccessPrivilegeListReadTest("testMembershipsFieldTypeAccessNeedsAdmin"));
  }

  /**
   *
   */
  public AccessPrivilegeListReadTest() {
    super();
  }

  /**
   * @param name
   */
  public AccessPrivilegeListReadTest(String name) {
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
   * as root: the groups, privileges, the service, and PIT synced
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {GROUP_NAME, GROUP2_NAME}) {
        Group group = new GroupSave(rootSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
        group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN, false);
        group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
        group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, false);
        group.addMember(SubjectTestHelper.SUBJ5, false);
      }

      AttributeDef serviceDef = new AttributeDefSave(rootSession).assignCreateParentStemsIfNotExist(true)
          .assignAttributeDefType(AttributeDefType.service).assignName(SERVICE_FOLDER + ":serviceDef172")
          .assignToStem(true).save();
      AttributeDefName service = new AttributeDefNameSave(rootSession, serviceDef)
          .assignCreateParentStemsIfNotExist(true).assignName(SERVICE_NAME).save();
      Stem serviceFolder = StemFinder.findByName(rootSession, SERVICE_FOLDER, true);
      serviceFolder.getAttributeDelegate().assignAttribute(service);

      ChangeLogTempToEntity.convertRecords();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * subject ids of the members of the memberships a finder returns, per field name
   * @param membershipFinder finder (run in the current session)
   * @return "fieldName/subjectId" strings
   */
  private static Set<String> rows(MembershipFinder membershipFinder) {
    Set<String> result = new HashSet<String>();
    for (Object[] row : membershipFinder.findMembershipsMembers()) {
      Membership membership = null;
      Member member = null;
      for (Object o : row) {
        if (o instanceof Membership) {
          membership = (Membership)o;
        } else if (o instanceof Member) {
          member = (Member)o;
        }
      }
      result.add(membership.getListName() + "/" + member.getSubjectId());
    }
    return result;
  }

  /**
   * memberships of both groups as a caller
   * @param caller the caller
   * @param fieldType field type, or null
   * @return rows
   */
  private static Set<String> membershipsAs(Subject caller, FieldType fieldType) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      MembershipFinder membershipFinder = new MembershipFinder().assignCheckSecurity(true);
      membershipFinder.addGroup(GroupFinder.findByName(grouperSession, GROUP_NAME, true));
      membershipFinder.addGroup(GroupFinder.findByName(grouperSession, GROUP2_NAME, true));
      if (fieldType != null) {
        membershipFinder.assignFieldType(fieldType);
      }
      return rows(membershipFinder);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * memberships of one group as a caller (the single group fast path)
   * @param caller the caller
   * @param fieldType field type
   * @return rows
   */
  private static Set<String> oneGroupMembershipsAs(Subject caller, FieldType fieldType) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      return rows(new MembershipFinder().assignCheckSecurity(true)
          .addGroup(GroupFinder.findByName(grouperSession, GROUP_NAME, true)).assignFieldType(fieldType));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * WS getMemberships fieldType=access with no fieldName: READ gets no privilege rows, ADMIN does;
   * the member list itself is unchanged for READ
   */
  public void testMembershipsFieldTypeAccessNeedsAdmin() {
    String adminRow = Field.FIELD_NAME_ADMINS + "/" + SubjectTestHelper.SUBJ4.getId();
    String memberRow = "members/" + SubjectTestHelper.SUBJ5.getId();

    Set<String> readerRows = membershipsAs(SubjectTestHelper.SUBJ2, FieldType.ACCESS);
    assertFalse(readerRows.toString(), readerRows.contains(adminRow));
    readerRows = oneGroupMembershipsAs(SubjectTestHelper.SUBJ2, FieldType.ACCESS);
    assertFalse(readerRows.toString(), readerRows.contains(adminRow));

    Set<String> adminRows = membershipsAs(SubjectTestHelper.SUBJ3, FieldType.ACCESS);
    assertTrue(adminRows.toString(), adminRows.contains(adminRow));
    adminRows = oneGroupMembershipsAs(SubjectTestHelper.SUBJ3, FieldType.ACCESS);
    assertTrue(adminRows.toString(), adminRows.contains(adminRow));

    // list memberships: READ is enough, as before
    readerRows = membershipsAs(SubjectTestHelper.SUBJ2, null);
    assertTrue(readerRows.toString(), readerRows.contains(memberRow));
    assertFalse(readerRows.toString(), readerRows.contains(adminRow));
  }

  /**
   * memberships with both a list field and a privilege list: the privilege rows are not returned
   * to READ, whatever the order of the fields
   */
  public void testMembershipsMixedFieldsNeedAdmin() {
    String adminRow = Field.FIELD_NAME_ADMINS + "/" + SubjectTestHelper.SUBJ4.getId();
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Set<String> readerRows = null;
      try {
        readerRows = rows(new MembershipFinder().assignCheckSecurity(true)
            .addGroup(GroupFinder.findByName(grouperSession, GROUP_NAME, true))
            .addGroup(GroupFinder.findByName(grouperSession, GROUP2_NAME, true))
            .addField(FieldFinder.find("members", true))
            .addField(FieldFinder.find(Field.FIELD_NAME_ADMINS, true)));
      } catch (RuntimeException re) {
        // some releases (e.g. 7.9.0) refuse a list field mixed with a privilege field in one
        // query, so nothing can leak this way there
        if (re.getMessage() != null && re.getMessage().contains("Expecting field type")) {
          System.out.println("skip testMembershipsMixedFieldsNeedAdmin: this release refuses mixed field types");
          return;
        }
        throw re;
      }
      assertFalse(readerRows.toString(), readerRows.contains(adminRow));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * WS getMemberships serviceRole=admin: READ on the groups of the service is not enough to list
   * their admins and updaters
   */
  public void testMembershipsServiceRoleAdminNeedsAdmin() {
    String serviceId = serviceId();
    for (Subject caller : new Subject[] {SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3}) {
      GrouperSession grouperSession = GrouperSession.start(caller);
      try {
        Set<String> subjectIds = new HashSet<String>();
        for (Member member : new MembershipFinder().assignServiceId(serviceId)
            .assignServiceRole(ServiceRole.admin).findMembershipResult().members()) {
          subjectIds.add(member.getSubjectId());
        }
        boolean isAdmin = caller == SubjectTestHelper.SUBJ3;
        assertEquals(caller.getId() + ": " + subjectIds, isAdmin, subjectIds.contains(SubjectTestHelper.SUBJ4.getId()));
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * WS findAttributeDefNames serviceRole=admin for a subject: READ on the groups of the service is
   * not enough to learn that the subject is an admin there; ADMIN is, and the subject itself sees
   * its own services
   */
  public void testServicesOfAdminNeedAdmin() {
    assertEquals(0, servicesWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ2));
    assertEquals(1, servicesWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ3));
    assertEquals(1, servicesWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ4));
  }

  /**
   * @param caller caller
   * @return number of services (named SERVICE_NAME) where SUBJ4 is an admin, as the caller
   */
  private static int servicesWhereSubj4IsAdminAs(Subject caller) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      int count = 0;
      for (AttributeDefName attributeDefName : new AttributeDefNameFinder().assignSubject(SubjectTestHelper.SUBJ4)
          .assignServiceRole(ServiceRole.admin).findAttributeNames()) {
        if (SERVICE_NAME.equals(attributeDefName.getName())) {
          count++;
        }
      }
      return count;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @return the service id, as root
   */
  private static String serviceId() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // by name: the scope plus findByUuidOrName query refers to alternateNameDb, which
      // AttributeDefName does not map in some releases (stock bug, not this issue)
      return AttributeDefNameFinder.findByName(SERVICE_NAME, true).getId();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * WS getGroups for SUBJ4 with fieldName=admins: the group is only listed to
   * ADMIN, and to SUBJ4 itself
   */
  public void testGroupsWhereSubjectIsAdminNeedAdmin() {
    assertFalse(groupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ2).contains(GROUP_NAME));
    assertTrue(groupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ3).contains(GROUP_NAME));
    assertTrue(groupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ4).contains(GROUP_NAME));

    // list field: READ is enough, as before
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ5, true);
      Set<String> names = new HashSet<String>();
      for (Group group : member.getGroups()) {
        names.add(group.getName());
      }
      assertTrue(names.toString(), names.contains(GROUP_NAME));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param caller caller
   * @return names of the groups where SUBJ4 is in admins, as the caller
   */
  private static Set<String> groupsWhereSubj4IsAdminAs(Subject caller) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
      Set<String> names = new HashSet<String>();
      for (Group group : member.getGroups(FieldFinder.find(Field.FIELD_NAME_ADMINS, true))) {
        names.add(group.getName());
      }
      return names;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * WS getGroups with pointInTimeFrom and fieldName=admins: same rule for point in time
   */
  public void testPitGroupsWhereSubjectIsAdminNeedAdmin() {
    assertFalse(pitGroupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ2).contains(GROUP_NAME));
    assertTrue(pitGroupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ3).contains(GROUP_NAME));
    assertTrue(pitGroupsWhereSubj4IsAdminAs(SubjectTestHelper.SUBJ4).contains(GROUP_NAME));
  }

  /**
   * @param caller caller
   * @return names of the PIT groups where SUBJ4 was in admins, as the caller
   */
  private static Set<String> pitGroupsWhereSubj4IsAdminAs(Subject caller) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
      Field admins = FieldFinder.find(Field.FIELD_NAME_ADMINS, true);
      Set<String> names = new HashSet<String>();
      for (PITGroup pitGroup : PITMember.getGroups(member.getUuid(), admins.getUuid(), null, (Stem)null, null,
          new Timestamp(0), null, null)) {
        names.add(pitGroup.getName());
      }
      return names;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
