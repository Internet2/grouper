package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperSyncLogWithOwner;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Provisioner configuration monitoring screens (activity, errors, logs, provisionable
 * groups, assignments) must not show groups the caller cannot VIEW or memberships of groups
 * the caller cannot READ, even when the caller is allowed to view the provisioner
 * (groupAllowedToView / groupAllowedToAssign).</p>
 *
 * <p>Security fix provisionerConfigViewsDisclosePrivateGroupsAndMemberships.  The rows are
 * built in memory (no grouper_sync rows needed) and handed to ProvisionerConfigurationContainer
 * the same way UiV2ProvisionerConfiguration does.  Before the fix every row is kept (and
 * ProvisionerConfigViewFilter does not exist, so this class does not compile).</p>
 *
 * <p>Not every release has every screen: 2.6.19 has activity (per group / member / membership)
 * and logs only, no errors screen (GrouperProvisioningError), combined activity list
 * (GuiProvisionerActivity), provisionable groups or assignments (GuiProvisioningAssignment).
 * Those parts are reached by reflection so this one file compiles on every release, and are
 * skipped (with a line on stdout) where the release does not have them.</p>
 */
public class ProvisionerConfigViewFilterTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ProvisionerConfigViewFilterTest("testActivityFiltered"));
  }

  /**
   * @param name
   */
  public ProvisionerConfigViewFilterTest(String name) {
    super(name);
  }

  /** SUBJ0 cannot VIEW or READ */
  private Group privateGroup;

  /** SUBJ0 can VIEW but not READ */
  private Group confidentialMembersGroup;

  /** SUBJ0 can VIEW and READ */
  private Group openGroup;

  /**
   * create the three groups as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Subject everyEntity = SubjectFinder.findAllSubject();

      this.privateGroup = new GroupSave(grouperSession).assignName("test:privateGroup").assignCreateParentStemsIfNotExist(true).save();
      this.privateGroup.revokePriv(everyEntity, AccessPrivilege.READ, false);
      this.privateGroup.revokePriv(everyEntity, AccessPrivilege.VIEW, false);

      this.confidentialMembersGroup = new GroupSave(grouperSession).assignName("test:confidentialMembersGroup").assignCreateParentStemsIfNotExist(true).save();
      this.confidentialMembersGroup.revokePriv(everyEntity, AccessPrivilege.READ, false);
      this.confidentialMembersGroup.revokePriv(everyEntity, AccessPrivilege.VIEW, false);
      this.confidentialMembersGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

      this.openGroup = new GroupSave(grouperSession).assignName("test:openGroup").assignCreateParentStemsIfNotExist(true).save();
      this.openGroup.revokePriv(everyEntity, AccessPrivilege.READ, false);
      this.openGroup.revokePriv(everyEntity, AccessPrivilege.VIEW, false);
      this.openGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------
  // fixtures
  // ---------------------------------------------------------------------------------------

  /**
   * @param group
   * @return in-memory sync group
   */
  private static GcGrouperSyncGroup syncGroup(Group group) {
    GcGrouperSyncGroup syncGroup = new GcGrouperSyncGroup();
    syncGroup.setGroupId(group.getId());
    syncGroup.setGroupName(group.getName());
    syncGroup.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    return syncGroup;
  }

  /**
   * @return in-memory sync member
   */
  private static GcGrouperSyncMember syncMember() {
    GcGrouperSyncMember syncMember = new GcGrouperSyncMember();
    syncMember.setSourceId("jdbc");
    syncMember.setSubjectId(SubjectTestHelper.SUBJ1.getId());
    syncMember.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    return syncMember;
  }

  /**
   * @param group
   * @return in-memory sync membership
   */
  private static GcGrouperSyncMembership syncMembership(Group group) {
    GcGrouperSyncMembership syncMembership = new GcGrouperSyncMembership();
    syncMembership.setGrouperSyncGroup(syncGroup(group));
    syncMembership.setGrouperSyncMember(syncMember());
    syncMembership.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    return syncMembership;
  }

  // ---------------------------------------------------------------------------------------
  // reflection for the parts not in every release
  // ---------------------------------------------------------------------------------------

  /** provisioning error row (errors screen, not in 2.6.19) */
  private static final String ERROR_CLASS = "edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningError";

  /** provisioning error counts (errors screen, not in 2.6.19) */
  private static final String ERROR_SUMMARY_CLASS = "edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningErrorSummary";

  /** combined activity row (not in 2.6.19) */
  private static final String ACTIVITY_CLASS = "edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisionerActivity";

  /** provisioning assignment row (not in 2.6.19) */
  private static final String ASSIGNMENT_CLASS = "edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiProvisioningAssignment";

  /**
   * @param className
   * @return the class or null if this release does not have it
   */
  private static Class<?> classOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }

  /**
   * @param className
   * @param what for the skip message
   * @return true if the release has the class, else print a skip line
   */
  private static boolean releaseHas(String className, String what) {
    if (classOrNull(className) != null) {
      return true;
    }
    System.out.println("ProvisionerConfigViewFilterTest: this release has no " + what + " ("
        + className.substring(className.lastIndexOf('.') + 1) + "), skipped that part");
    return false;
  }

  /**
   * call a public method by name and argument count (the methods used here are not overloaded)
   * @param target object, or a Class for a static method
   * @param methodName
   * @param args
   * @return the result
   */
  private static Object call(Object target, String methodName, Object... args) {
    Class<?> theClass = target instanceof Class ? (Class<?>)target : target.getClass();
    for (Method method : theClass.getMethods()) {
      if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
        try {
          return method.invoke(target instanceof Class ? null : target, args);
        } catch (InvocationTargetException ite) {
          if (ite.getCause() instanceof RuntimeException) {
            throw (RuntimeException)ite.getCause();
          }
          throw new RuntimeException(ite.getCause());
        } catch (IllegalAccessException iae) {
          throw new RuntimeException(iae);
        }
      }
    }
    throw new RuntimeException("no method " + methodName + " with " + args.length + " args on " + theClass.getName());
  }

  /**
   * @param className
   * @return a new instance
   */
  private static Object newInstance(String className) {
    try {
      return Class.forName(className).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param objectType
   * @param group may be null for entity errors
   * @return in-memory error (a GrouperProvisioningError)
   */
  private static Object error(String objectType, Group group) {
    Object error = newInstance(ERROR_CLASS);
    call(error, "setObjectType", objectType);
    call(error, "setGroupName", group == null ? null : group.getName());
    call(error, "setSubjectId", SubjectTestHelper.SUBJ1.getId());
    call(error, "setErrorCode", "ERROR");
    call(error, "setErrorDescription", "secret error text");
    return error;
  }

  /**
   * @return unfiltered summary as the service would compute it for the errors in errors()
   * (a GrouperProvisioningErrorSummary)
   */
  private static Object unfilteredSummary() {
    Object summary = newInstance(ERROR_SUMMARY_CLASS);
    call(summary, "setGroupErrorsCount", 3L);
    call(summary, "setMembershipErrorsCount", 3L);
    call(summary, "setEntityErrorsCount", 1L);
    call(summary, "setErrorsCount", 7L);
    return summary;
  }

  /**
   * @return group, membership and entity errors for all three groups
   */
  private List<Object> errors() {
    List<Object> errors = new ArrayList<Object>();
    for (Group group : new Group[] {this.privateGroup, this.confidentialMembersGroup, this.openGroup}) {
      errors.add(error("group", group));
      errors.add(error("membership", group));
    }
    errors.add(error("entity", null));
    return errors;
  }

  /**
   * @return one group-owned log per group plus a job log
   */
  private List<GuiProvisionerLog> logs() {
    List<GrouperSyncLogWithOwner> withOwners = new ArrayList<GrouperSyncLogWithOwner>();
    for (Group group : new Group[] {this.privateGroup, this.confidentialMembersGroup, this.openGroup}) {
      GrouperSyncLogWithOwner withOwner = new GrouperSyncLogWithOwner();
      withOwner.setGcGrouperSyncGroup(syncGroup(group));
      withOwners.add(withOwner);
    }
    GrouperSyncLogWithOwner jobLog = new GrouperSyncLogWithOwner();
    GcGrouperSyncJob job = new GcGrouperSyncJob();
    job.setSyncType("incrementalProvisionChangeLog");
    jobLog.setGcGrouperSyncJob(job);
    withOwners.add(jobLog);
    // a log whose owner id is set but resolved to nothing (owner deleted or never synced): non-root
    // callers do not see it, its description can be the log text for a private group (2026-11-10)
    GrouperSyncLogWithOwner orphanLog = new GrouperSyncLogWithOwner();
    edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog orphanSyncLog =
        new edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog();
    orphanSyncLog.setGrouperSyncOwnerId("secPatch116orphanOwnerId");
    orphanSyncLog.setDescription("log text of a group that no longer resolves");
    orphanLog.setGcGrouperSyncLog(orphanSyncLog);
    withOwners.add(orphanLog);
    return GuiProvisionerLog.convertFromGcGrouperSyncWithOwner(withOwners);
  }

  /**
   * @param syncGroups
   * @return group names
   */
  private static Set<String> groupNames(List<GcGrouperSyncGroup> syncGroups) {
    Set<String> result = new HashSet<String>();
    for (GcGrouperSyncGroup syncGroup : syncGroups) {
      result.add(syncGroup.getGroupName());
    }
    return result;
  }

  /**
   * @param syncMemberships
   * @return group names
   */
  private static Set<String> membershipGroupNames(List<GcGrouperSyncMembership> syncMemberships) {
    Set<String> result = new HashSet<String>();
    for (GcGrouperSyncMembership syncMembership : syncMemberships) {
      result.add(syncMembership.getGrouperSyncGroup().getGroupName());
    }
    return result;
  }

  /**
   * @param errors
   * @param objectType
   * @return group names of errors of that type
   */
  private static Set<String> errorGroupNames(List<?> errors, String objectType) {
    Set<String> result = new HashSet<String>();
    for (Object error : errors) {
      if (objectType.equals(call(error, "getObjectType"))) {
        result.add((String)call(error, "getGroupName"));
      }
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * activity: group rows need VIEW, membership rows need READ, entity rows stay; the combined
   * activity list is rebuilt from the filtered lists
   */
  public void testActivityFiltered() {
    List<GcGrouperSyncGroup> syncGroups = new ArrayList<GcGrouperSyncGroup>();
    List<GcGrouperSyncMembership> syncMemberships = new ArrayList<GcGrouperSyncMembership>();
    for (Group group : new Group[] {this.privateGroup, this.confidentialMembersGroup, this.openGroup}) {
      syncGroups.add(syncGroup(group));
      syncMemberships.add(syncMembership(group));
    }
    List<GcGrouperSyncMember> syncMembers = new ArrayList<GcGrouperSyncMember>();
    syncMembers.add(syncMember());

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ProvisionerConfigurationContainer container = new ProvisionerConfigurationContainer();
      container.setActivityForGroup(syncGroups);
      container.setActivityForMember(syncMembers);
      container.setActivityForMembership(syncMemberships);
      // the handler converts its own unfiltered lists (the combined list is not in 2.6.19)
      boolean combinedActivity = releaseHas(ACTIVITY_CLASS, "combined activity list");
      if (combinedActivity) {
        call(container, "setProvisionerActivity", call(classOrNull(ACTIVITY_CLASS), "convertFromGcGrouperSyncObjects",
            syncGroups, syncMembers, syncMemberships));
      }

      Set<String> expectedGroups = new HashSet<String>();
      expectedGroups.add(this.confidentialMembersGroup.getName());
      expectedGroups.add(this.openGroup.getName());
      assertEquals(expectedGroups, groupNames(container.getActivityForGroup()));

      Set<String> expectedMemberships = new HashSet<String>();
      expectedMemberships.add(this.openGroup.getName());
      assertEquals(expectedMemberships, membershipGroupNames(container.getActivityForMembership()));

      assertEquals(1, container.getActivityForMember().size());

      if (combinedActivity) {
        // 2 groups + 1 entity + 1 membership
        List<?> activities = (List<?>)call(container, "getProvisionerActivity");
        assertEquals(4, activities.size());
        for (Object activity : activities) {
          String name = (String)call(activity, "getName");
          assertFalse(name, name.contains(this.privateGroup.getName()));
          assertFalse(name, name.startsWith(this.confidentialMembersGroup.getName() + " - "));
        }
      }

      // the caller's lists are not changed
      assertEquals(3, syncGroups.size());
      assertEquals(3, syncMemberships.size());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * errors: group errors need VIEW, membership errors need READ, entity errors stay, and the
   * summary is rebuilt from the visible rows (either setter order)
   */
  public void testErrorsFiltered() {
    if (!releaseHas(ERROR_CLASS, "provisioning errors screen")) {
      return;
    }
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      for (boolean summaryFirst : new boolean[] {true, false}) {
        ProvisionerConfigurationContainer container = new ProvisionerConfigurationContainer();
        if (summaryFirst) {
          call(container, "setGrouperProvisioningErrorSummary", unfilteredSummary());
          call(container, "setGrouperProvisioningErrors", errors());
        } else {
          call(container, "setGrouperProvisioningErrors", errors());
          call(container, "setGrouperProvisioningErrorSummary", unfilteredSummary());
        }

        List<?> visible = (List<?>)call(container, "getGrouperProvisioningErrors");
        Set<String> expectedGroups = new HashSet<String>();
        expectedGroups.add(this.confidentialMembersGroup.getName());
        expectedGroups.add(this.openGroup.getName());
        assertEquals(expectedGroups, errorGroupNames(visible, "group"));

        Set<String> expectedMemberships = new HashSet<String>();
        expectedMemberships.add(this.openGroup.getName());
        assertEquals(expectedMemberships, errorGroupNames(visible, "membership"));
        assertEquals(4, visible.size());

        Object summary = call(container, "getGrouperProvisioningErrorSummary");
        assertEquals(2, ((Number)call(summary, "getGroupErrorsCount")).intValue());
        assertEquals(1, ((Number)call(summary, "getMembershipErrorsCount")).intValue());
        assertEquals(1, ((Number)call(summary, "getEntityErrorsCount")).intValue());
        assertEquals(4, ((Number)call(summary, "getErrorsCount")).intValue());
        assertEquals(Long.valueOf(2), ((Map<?, ?>)call(summary, "getGroupErrorTypeCount")).get("ERROR"));
        assertEquals(Long.valueOf(1), ((Map<?, ?>)call(summary, "getMembershipsErrorTypeCount")).get("ERROR"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * logs: the log owned by a group the caller cannot VIEW is dropped, the job log stays
   */
  public void testLogsFiltered() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ProvisionerConfigurationContainer container = new ProvisionerConfigurationContainer();
      container.setGuiProvisionerLogs(logs());
      List<GuiProvisionerLog> visible = container.getGuiProvisionerLogs();
      assertEquals(3, visible.size());
      for (GuiProvisionerLog log : visible) {
        GcGrouperSyncGroup syncGroup = log.getGcGrouperSyncLogWithOwner().getGcGrouperSyncGroup();
        if (syncGroup != null) {
          assertFalse(this.privateGroup.getId().equals(syncGroup.getGroupId()));
        }
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * provisionable groups and assignments: rows for groups the handler could not find (not
   * viewable) are dropped and not counted
   */
  /**
   * construct by reflection with the first public constructor the arguments fit (the declared
   * parameter types can be supertypes, e.g. GuiProvisioningAssignment(String, GuiObjectBase), which
   * Class.getConstructor would not find from the arguments' own classes)
   * @param theClass
   * @param args
   * @return the new instance
   * @throws Exception
   */
  private static Object newInstanceFitting(Class<?> theClass, Object... args) throws Exception {
    for (java.lang.reflect.Constructor<?> constructor : theClass.getConstructors()) {
      Class<?>[] types = constructor.getParameterTypes();
      if (types.length != args.length) {
        continue;
      }
      boolean fits = true;
      for (int i = 0; i < types.length && fits; i++) {
        fits = args[i] == null ? !types[i].isPrimitive() : types[i].isInstance(args[i]);
      }
      if (fits) {
        return constructor.newInstance(args);
      }
    }
    throw new RuntimeException("no constructor of " + theClass.getName() + " fits " + args.length + " args");
  }

  public void testProvisionableAndAssignmentsDropHiddenRows() throws Exception {
    if (!releaseHas(ASSIGNMENT_CLASS, "provisionable groups / assignments screens")) {
      return;
    }
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();
      // what the handler builds when GroupFinder did not return a group
      guiGroups.add(new GuiGroup(null));
      // the handler's GroupFinder has no privileges, so it does return private groups (2026-11-10)
      guiGroups.add(new GuiGroup(this.privateGroup));
      guiGroups.add(new GuiGroup(this.openGroup));

      Class<?> assignmentClass = classOrNull(ASSIGNMENT_CLASS);
      List<Object> assignments = new ArrayList<Object>();
      assignments.add(newInstanceFitting(assignmentClass, "group", new GuiGroup(null)));
      assignments.add(newInstanceFitting(assignmentClass, "group", new GuiGroup(this.privateGroup)));
      assignments.add(newInstanceFitting(assignmentClass, "group", new GuiGroup(this.openGroup)));

      ProvisionerConfigurationContainer container = new ProvisionerConfigurationContainer();
      call(container, "setGuiGroupsProvisionable", guiGroups);
      call(container, "setTotalProvisionableGroups", 3);
      call(container, "setGuiProvisioningAssignments", assignments);
      call(container, "setTotalProvisioningAssignments", 3);

      List<?> provisionable = (List<?>)call(container, "getGuiGroupsProvisionable");
      assertEquals(1, provisionable.size());
      assertEquals(this.openGroup.getId(), ((GuiGroup)provisionable.get(0)).getGroup().getId());
      assertEquals(1, ((Number)call(container, "getTotalProvisionableGroups")).intValue());
      assertEquals(1, ((List<?>)call(container, "getGuiProvisioningAssignments")).size());
      assertEquals(1, ((Number)call(container, "getTotalProvisioningAssignments")).intValue());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * root sees every activity, error and log row, and the original summary
   */
  public void testRootUnfiltered() {
    List<GcGrouperSyncGroup> syncGroups = new ArrayList<GcGrouperSyncGroup>();
    List<GcGrouperSyncMembership> syncMemberships = new ArrayList<GcGrouperSyncMembership>();
    for (Group group : new Group[] {this.privateGroup, this.confidentialMembersGroup, this.openGroup}) {
      syncGroups.add(syncGroup(group));
      syncMemberships.add(syncMembership(group));
    }
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      ProvisionerConfigurationContainer container = new ProvisionerConfigurationContainer();
      container.setActivityForGroup(syncGroups);
      container.setActivityForMembership(syncMemberships);
      boolean errorsScreen = releaseHas(ERROR_CLASS, "provisioning errors screen");
      Object summary = null;
      if (errorsScreen) {
        summary = unfilteredSummary();
        call(container, "setGrouperProvisioningErrorSummary", summary);
        call(container, "setGrouperProvisioningErrors", errors());
      }
      container.setGuiProvisionerLogs(logs());

      assertEquals(3, container.getActivityForGroup().size());
      assertEquals(3, container.getActivityForMembership().size());
      if (errorsScreen) {
        assertEquals(7, ((List<?>)call(container, "getGrouperProvisioningErrors")).size());
        assertSame(summary, call(container, "getGrouperProvisioningErrorSummary"));
      }
      assertEquals(5, container.getGuiProvisionerLogs().size());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
