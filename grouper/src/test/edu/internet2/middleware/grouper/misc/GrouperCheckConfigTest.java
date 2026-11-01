package edu.internet2.middleware.grouper.misc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdlWorker;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import junit.textui.TestRunner;

public class GrouperCheckConfigTest extends GrouperTest {

  private static final String RACING_GROUP_NAME = "test:checkConfigRace:firstGroup";

  private static final String LATER_GROUP_NAME = "test:checkConfigRace:laterGroup";

  public GrouperCheckConfigTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperCheckConfigTest("testConfiguredMembershipDoesNotMistakeEffectiveForDirectMember"));
  }

  /**
   * GRP-7530: concurrent startup callers must both finish creating configured groups.
   * Only run the group phase to avoid unrelated races in the other phases' static maps.
   */
  public void testConcurrentCheckGroups() throws Exception {
    new StemSave().assignName("test:checkConfigRace")
        .assignCreateParentStemsIfNotExist(true).save();
    assertNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, false));
    assertNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), LATER_GROUP_NAME, false));

    Map<String, String> overrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    Map<String, String> originalOverrides = new HashMap<String, String>(overrides);
    String[] propertyNames = {
        "configuration.autocreate.group.name.0",
        "configuration.autocreate.group.name.1",
        "configuration.autocreate.group.name.2",
        "configuration.autocreate.group.subjects.0",
        "configuration.autocreate.group.subjects.1"
    };
    final String[] groupNames = {RACING_GROUP_NAME, LATER_GROUP_NAME};
    final CountDownLatch ready = new CountDownLatch(2);
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch finished = new CountDownLatch(2);
    final CountDownLatch insertReady = new CountDownLatch(2);
    final Throwable[] failures = new Throwable[2];
    final String[][] groupIds = new String[2][groupNames.length];
    Thread[] threads = new Thread[2];
    boolean wasInCheckConfig = GrouperCheckConfig.inCheckConfig;

    try {
      overrides.put(propertyNames[0], RACING_GROUP_NAME);
      overrides.put(propertyNames[1], LATER_GROUP_NAME);
      overrides.put(propertyNames[2], null);
      overrides.put(propertyNames[3], null);
      overrides.put(propertyNames[4], null);

      ConcurrentInsertHook.insertReady = insertReady;
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), ConcurrentInsertHook.class);
      // Resolve the hook before starting threads so hook-cache initialization cannot hide the race.
      GrouperHookType.hooksInstances(GrouperHookType.GROUP, GroupHooks.METHOD_GROUP_PRE_INSERT, HooksGroupBean.class);

      for (int i = 0; i < threads.length; i++) {
        final int threadIndex = i;
        threads[i] = new Thread(new Runnable() {
          @Override
          public void run() {
            GrouperSession grouperSession = null;
            try {
              ready.countDown();
              if (!start.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to start checkGroups");
              }
              grouperSession = GrouperSession.startRootSession();
              GrouperCheckConfig.checkGroups();
              for (int groupIndex = 0; groupIndex < groupNames.length; groupIndex++) {
                groupIds[threadIndex][groupIndex] =
                    GroupFinder.findByName(grouperSession, groupNames[groupIndex], true).getUuid();
              }
            } catch (Throwable t) {
              failures[threadIndex] = t;
            } finally {
              GrouperSession.stopQuietly(grouperSession);
              finished.countDown();
            }
          }
        }, "check-config-race-" + i);
        threads[i].setDaemon(true);
        threads[i].start();
      }

      assertTrue("Both checkGroups callers must be ready", ready.await(30, TimeUnit.SECONDS));
      start.countDown();
      assertTrue("Both checkGroups callers must finish", finished.await(90, TimeUnit.SECONDS));

      for (int i = 0; i < failures.length; i++) {
        if (failures[i] != null) {
          throw new AssertionError("Concurrent checkGroups caller " + i + " failed", failures[i]);
        }
      }
      assertTrue("The test hook must observe creation of the missing group", insertReady.getCount() < 2);

      for (int groupIndex = 0; groupIndex < groupNames.length; groupIndex++) {
        String groupName = groupNames[groupIndex];
        assertEquals("There must be exactly one group named " + groupName, 1,
            new GcDbAccess().sql("select count(*) from grouper_groups where name = ?")
                .addBindVar(groupName).select(int.class).intValue());
        String groupId = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true).getUuid();
        for (int threadIndex = 0; threadIndex < threads.length; threadIndex++) {
          assertEquals("Every caller must find " + groupName, groupId, groupIds[threadIndex][groupIndex]);
        }
      }
      assertNull(ddlWorker().getHeartbeat());
    } finally {
      start.countDown();
      while (insertReady.getCount() > 0) {
        insertReady.countDown();
      }
      try {
        for (Thread thread : threads) {
          if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread.join(10000);
          }
        }
      } finally {
        ConcurrentInsertHook.insertReady = null;
        ConcurrentInsertHook.workerUuidDuringInsert = null;
        GrouperCheckConfig.inCheckConfig = wasInCheckConfig;
        for (String propertyName : propertyNames) {
          if (originalOverrides.containsKey(propertyName)) {
            overrides.put(propertyName, originalOverrides.get(propertyName));
          } else {
            overrides.remove(propertyName);
          }
        }
        GrouperHooksUtils.reloadHooks();
      }
    }
  }

  public void testCheckObjectsWithoutWorkDoesNotUpdateDdlWorker() {
    Hib3GrouperDdlWorker before = ddlWorker();
    GrouperCheckConfig.checkObjects();
    assertWorkerUnchanged(before);
  }

  public void testWaiterRechecksAfterAnotherWorkerCreatesGroup() throws Exception {
    Map<String, String> original = saveOverrides();
    GrouperDdlEngine otherWorker = new GrouperDdlEngine();
    final Throwable[] failures = new Throwable[1];
    final CountDownLatch finished = new CountDownLatch(1);
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        GrouperSession session = null;
        try {
          session = GrouperSession.startRootSession();
          GrouperCheckConfig.checkGroups();
        } catch (Throwable t) {
          failures[0] = t;
        } finally {
          GrouperSession.stopQuietly(session);
          finished.countDown();
        }
      }
    }, "check-config-other-worker");
    thread.setDaemon(true);
    try {
      new StemSave().assignName("test:checkConfigRace").assignCreateParentStemsIfNotExist(true).save();
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      assertTrue(otherWorker.acquireDatabaseLock());
      String otherWorkerUuid = ddlWorker().getWorkerUuid();
      thread.start();

      boolean waitingForLock = false;
      for (int i = 0; i < 100 && !waitingForLock; i++) {
        for (StackTraceElement frame : thread.getStackTrace()) {
          if (frame.getClassName().equals(GrouperDdlEngine.class.getName())
              && frame.getMethodName().equals("waitForOtherJvmsOrLockInDatabase")) {
            waitingForLock = true;
            break;
          }
        }
        if (!waitingForLock) {
          Thread.sleep(100);
        }
      }
      assertTrue("The check-config caller must wait for the other database worker", waitingForLock);

      new GroupSave().assignName(RACING_GROUP_NAME).save();
      otherWorker.releaseDatabaseLock();
      Hib3GrouperDdlWorker afterOtherWorker = ddlWorker();
      assertTrue("The waiting caller must finish", finished.await(90, TimeUnit.SECONDS));
      if (failures[0] != null) {
        throw new AssertionError("The waiting checkGroups caller failed", failures[0]);
      }
      assertNotNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, true));
      assertEquals(otherWorkerUuid, ddlWorker().getWorkerUuid());
      assertWorkerUnchanged(afterOtherWorker);
    } finally {
      otherWorker.releaseDatabaseLock();
      try {
        if (thread.isAlive()) {
          thread.interrupt();
          thread.join(10000);
        }
      } finally {
        restoreOverrides(original);
      }
    }
  }

  public void testLockStateDoesNotAcquireWhenNothingIsMissing() {
    RecordingObjectCheckState state = new RecordingObjectCheckState();
    try {
      assertFalse(state.shouldSave(Collections.singleton("test:existing"), Collections.singleton("test:existing")));
      assertEquals(0, state.acquisitions);
    } finally {
      state.close();
    }
    assertEquals(0, state.releases);
  }

  public void testLockStateReusesOneLockAcrossPhases() {
    RecordingObjectCheckState state = new RecordingObjectCheckState(true);
    try {
      assertTrue(state.shouldSave(Collections.singleton("test:first"), Collections.<String>emptySet()));
      assertTrue(state.shouldSave(Collections.singleton("test:later"), Collections.<String>emptySet()));
      assertEquals(1, state.acquisitions);
    } finally {
      state.close();
    }
    assertEquals(1, state.releases);
  }

  public void testLockStateDoesNotReacquireAfterWaitWhenWorkIsDone() {
    RecordingObjectCheckState state = new RecordingObjectCheckState(false);
    try {
      expectLockWait(state, true);
      assertFalse(state.shouldSave(Collections.singleton("test:missing"), Collections.singleton("test:missing")));
      assertEquals(1, state.acquisitions);
    } finally {
      state.close();
    }
  }

  public void testLockStateCanAcquireOnSecondAttempt() {
    RecordingObjectCheckState state = new RecordingObjectCheckState(false, true);
    try {
      expectLockWait(state, true);
      assertTrue(state.shouldSave(Collections.singleton("test:missing"), Collections.<String>emptySet()));
      assertTrue(state.shouldSave(Collections.singleton("test:later"), Collections.<String>emptySet()));
      assertEquals(2, state.acquisitions);
    } finally {
      state.close();
    }
    assertEquals(1, state.releases);
  }

  public void testLockStateDefersAfterTwoWaits() {
    RecordingObjectCheckState state = new RecordingObjectCheckState(false, false);
    try {
      expectLockWait(state, true);
      expectLockWait(state, false);
      try {
        state.shouldSave(Collections.singleton("test:missing"), Collections.<String>emptySet());
        fail("Still-missing work must be deferred without a third acquisition");
      } catch (GrouperCheckConfig.ObjectCheckDeferredException e) {
        assertEquals(Collections.singleton("test:missing"), e.work);
      }
      assertFalse(state.shouldSave(Collections.singleton("test:missing"), Collections.singleton("test:missing")));
      assertEquals(2, state.acquisitions);
    } finally {
      state.close();
    }
  }

  private static void expectLockWait(RecordingObjectCheckState state, boolean restartAll) {
    try {
      state.shouldSave(Collections.singleton("test:missing"), Collections.<String>emptySet());
      fail("A caller that waited must re-read instead of creating without the lock");
    } catch (GrouperCheckConfig.ObjectCheckRestartException e) {
      assertEquals(restartAll, e.restartAll);
    }
  }

  private static class RecordingObjectCheckState extends GrouperCheckConfig.ObjectCheckState {
    private final boolean[] acquisitionResults;
    private int acquisitions;
    private int releases;

    RecordingObjectCheckState(boolean... acquisitionResults) {
      this.acquisitionResults = acquisitionResults;
    }

    @Override
    protected GrouperDdlEngine newDdlEngine() {
      return new GrouperDdlEngine() {
        @Override
        public boolean acquireDatabaseLock() {
          assertTrue("Unexpected database lock acquisition", acquisitions < acquisitionResults.length);
          return acquisitionResults[acquisitions++];
        }

        @Override
        public void releaseDatabaseLock() {
          releases++;
        }
      };
    }
  }

  public void testAutocreateDisabledDoesNotAcquireLock() {
    Map<String, String> original = saveOverrides();
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.system.groups", "false");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      Hib3GrouperDdlWorker before = ddlWorker();
      GrouperCheckConfig.checkObjects();
      assertNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, false));
      assertWorkerUnchanged(before);
    } finally {
      restoreOverrides(original);
    }
  }

  public void testCheckObjectsCreatesParentsAndAttributesUnderOneLock() {
    Map<String, String> original = saveOverrides();
    try {
      String attributeRoot = "test:checkConfigObjects:attribute";
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.attribute.rootStem", attributeRoot);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      installInsertHook(false);

      assertNull(StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:checkConfigRace", false));
      GrouperCheckConfig.checkObjects();

      assertNotNull(StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:checkConfigRace", true));
      assertNotNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, true));
      assertTrue(RuleUtils.attributeRuleStemName().startsWith(attributeRoot + ":"));
      assertNotNull(AttributeDefFinder.findByName(RuleUtils.attributeRuleStemName() + ":rulesTypeDef", true));
      assertNotNull(AttributeDefNameFinder.findByName(RuleUtils.attributeRuleStemName() + ":rule", true));
      Hib3GrouperDdlWorker after = ddlWorker();
      assertNotNull(ConcurrentInsertHook.workerUuidDuringInsert);
      assertEquals("All later phases must reuse the group's lock",
          ConcurrentInsertHook.workerUuidDuringInsert, after.getWorkerUuid());
      assertNull(after.getHeartbeat());
    } finally {
      restoreOverrides(original);
    }
  }

  public void testLockReleasedWhenCreationFails() {
    Map<String, String> original = saveOverrides();
    try {
      new StemSave().assignName("test:checkConfigRace").assignCreateParentStemsIfNotExist(true).save();
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      installInsertHook(true);
      boolean wasInCheckConfig = GrouperCheckConfig.inCheckConfig;
      try {
        GrouperCheckConfig.checkGroups();
        fail("The insert hook should fail");
      } catch (RuntimeException e) {
        boolean foundExpectedFailure = false;
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
          if (cause.getMessage() != null && cause.getMessage().contains("GRP-7530 injected insert failure")) {
            foundExpectedFailure = true;
            break;
          }
        }
        assertTrue("An unrelated exception must not satisfy the test", foundExpectedFailure);
      }
      assertEquals(wasInCheckConfig, GrouperCheckConfig.inCheckConfig);
      assertNull(ddlWorker().getHeartbeat());
      assertNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, false));

      String failedWorkerUuid = ConcurrentInsertHook.workerUuidDuringInsert;
      ConcurrentInsertHook.failInsert = false;
      GrouperCheckConfig.checkGroups();
      assertNotNull(GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, true));
      assertFalse(failedWorkerUuid.equals(ConcurrentInsertHook.workerUuidDuringInsert));
      assertNull(ddlWorker().getHeartbeat());
    } finally {
      restoreOverrides(original);
    }
  }

  public void testConfiguredMembershipTakesLockOnlyWhenMissing() {
    Map<String, String> original = saveOverrides();
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      GrouperCheckConfig.checkGroups();
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), RACING_GROUP_NAME, true);
      assertFalse(group.hasImmediateMember(SubjectTestHelper.SUBJ0));
      Hib3GrouperDdlWorker before = ddlWorker();

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
          "configuration.autocreate.group.subjects.0", SubjectTestHelper.SUBJ0_ID);
      GrouperCheckConfig.checkGroups();
      assertTrue(group.hasImmediateMember(SubjectTestHelper.SUBJ0));
      Hib3GrouperDdlWorker after = ddlWorker();
      assertFalse(before.getWorkerUuid().equals(after.getWorkerUuid()));
      assertNull(after.getHeartbeat());

      GrouperCheckConfig.checkGroups();
      assertWorkerUnchanged(after);
    } finally {
      restoreOverrides(original);
    }
  }

  public void testConfiguredMembershipDoesNotMistakeEffectiveForDirectMember() {
    Map<String, String> original = saveOverrides();
    try {
      GrouperSession session = GrouperSession.staticGrouperSession();
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.group.name.0", RACING_GROUP_NAME);
      GrouperCheckConfig.checkGroups();
      Group group = GroupFinder.findByName(session, RACING_GROUP_NAME, true);
      Group childGroup = new GroupSave().assignName("test:checkConfigRace:childGroup").save();
      childGroup.addMember(SubjectTestHelper.SUBJ0);
      group.addMember(childGroup.toSubject());
      assertTrue("The subject must already be an indirect member", group.hasMember(SubjectTestHelper.SUBJ0));
      assertNull(MembershipFinder.findImmediateMembership(session, group, SubjectTestHelper.SUBJ0, false));
      Hib3GrouperDdlWorker before = ddlWorker();

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
          "configuration.autocreate.group.subjects.0", SubjectTestHelper.SUBJ0_ID);
      GrouperCheckConfig.checkGroups();
      assertNotNull(MembershipFinder.findImmediateMembership(session, group, SubjectTestHelper.SUBJ0, false));
      assertTrue("The existing child-group membership must remain", group.hasImmediateMember(childGroup.toSubject()));
      assertFalse(before.getWorkerUuid().equals(ddlWorker().getWorkerUuid()));
      assertNull(ddlWorker().getHeartbeat());
    } finally {
      restoreOverrides(original);
    }
  }

  public void testMessagingActionRepairUsesLock() {
    AttributeDef topicDef = AttributeDefFinder.findByName(
        GrouperBuiltinMessagingSystem.grouperMessageTopicNameOfDef(), true);
    topicDef.getAttributeDefActionDelegate().configureActionList(
        GrouperBuiltinMessagingSystem.actionSendToTopic + ",extraStartupAction");
    Hib3GrouperDdlWorker before = ddlWorker();

    GrouperCheckConfig.checkObjects();
    topicDef = AttributeDefFinder.findByName(
        GrouperBuiltinMessagingSystem.grouperMessageTopicNameOfDef(), true);
    assertEquals(Collections.singleton(GrouperBuiltinMessagingSystem.actionSendToTopic),
        topicDef.getAttributeDefActionDelegate().allowedActionStrings());
    Hib3GrouperDdlWorker after = ddlWorker();
    assertFalse(before.getWorkerUuid().equals(after.getWorkerUuid()));
    assertNull(after.getHeartbeat());

    GrouperCheckConfig.checkObjects();
    assertWorkerUnchanged(after);
  }

  private static Map<String, String> saveOverrides() {
    return new HashMap<String, String>(GrouperConfig.retrieveConfig().propertiesOverrideMap());
  }

  private static void restoreOverrides(Map<String, String> original) {
    ConcurrentInsertHook.insertReady = null;
    ConcurrentInsertHook.workerUuidDuringInsert = null;
    ConcurrentInsertHook.failInsert = false;
    GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().putAll(original);
    ExpirableCache.clearAll();
    GrouperHooksUtils.reloadHooks();
  }

  private static Hib3GrouperDdlWorker ddlWorker() {
    List<Hib3GrouperDdlWorker> workers = HibernateSession.bySqlStatic().listSelect(
        Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null);
    assertEquals(1, workers.size());
    return workers.get(0);
  }

  private static void assertWorkerUnchanged(Hib3GrouperDdlWorker before) {
    Hib3GrouperDdlWorker after = ddlWorker();
    assertEquals(before.getId(), after.getId());
    assertEquals(before.getWorkerUuid(), after.getWorkerUuid());
    assertEquals(before.getHeartbeat(), after.getHeartbeat());
    assertEquals(before.getLastUpdated(), after.getLastUpdated());
  }

  private static void installInsertHook(boolean failInsert) {
    ConcurrentInsertHook.insertReady = new CountDownLatch(0);
    ConcurrentInsertHook.workerUuidDuringInsert = null;
    ConcurrentInsertHook.failInsert = failInsert;
    GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), ConcurrentInsertHook.class);
    GrouperHookType.hooksInstances(GrouperHookType.GROUP, GroupHooks.METHOD_GROUP_PRE_INSERT, HooksGroupBean.class);
  }

  /**
   * Hold the insert after the existence checks, without changing production code.
   */
  public static class ConcurrentInsertHook extends GroupHooks {

    private static volatile CountDownLatch insertReady;
    private static volatile String workerUuidDuringInsert;
    private static volatile boolean failInsert;

    @Override
    public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
      CountDownLatch latch = insertReady;
      if (latch == null || !RACING_GROUP_NAME.equals(preInsertBean.getGroup().getName())) {
        return;
      }
      Hib3GrouperDdlWorker worker = ddlWorker();
      assertNotNull("The insert must hold the database lock, not just a JVM monitor", worker.getHeartbeat());
      workerUuidDuringInsert = worker.getWorkerUuid();
      if (failInsert) {
        throw new IllegalStateException("GRP-7530 injected insert failure");
      }
      latch.countDown();
      try {
        // A future lock will serialize callers, so the first insert must also proceed alone.
        latch.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while coordinating the checkGroups race", e);
      }
    }
  }
}
