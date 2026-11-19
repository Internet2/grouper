/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.List;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for advisory M3: the emailed group members export (READ on the group is enough)
 * saved a new report config and a durable Quartz job per request, with no limit, and both were
 * only removed when the export was downloaded.  GroupMembersExportRequestLimiter limits requests
 * (one pending per requester per group, a few per hour per requester), makes the job one shot and
 * non durable, and the reports clear daemon deletes stale exports entirely.
 */
public class GroupMembersExportRequestLimiterTest extends GrouperTest {

  /**
   *
   */
  public GroupMembersExportRequestLimiterTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupMembersExportRequestLimiterTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupMembersExportRequestLimiterTest("testOnePendingExportPerGroup"));
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperReporting.enable");
    super.tearDown();
  }

  /**
   * group the test subjects can READ
   * @param name
   * @return the group
   */
  private static Group readableGroup(String name) {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    return group;
  }

  /**
   * save an export config the way UiV2GroupImport.groupExportEmailSubmit does (as root)
   * @param group
   * @param requester
   * @return the saved bean
   */
  private static GrouperReportConfigurationBean saveExportConfig(Group group, Subject requester) {
    String configName = GroupMembersExportRequestLimiter.CONFIG_NAME_PREFIX + GrouperUuid.getUuid();
    GrouperReportConfigurationBean configBean = new GrouperReportConfigurationBean();
    configBean.setReportConfigName(configName);
    configBean.setReportConfigDescription("System managed configuration for the group members export feature");
    configBean.setReportConfigType(ReportConfigType.GROUP_MEMBERS_EXPORT);
    configBean.setReportConfigFormat(ReportConfigFormat.CSV);
    configBean.setReportConfigFilename("groupMembersExport_" + group.getExtension() + "_$$timestamp$$.csv");
    configBean.setReportConfigEnabled(true);
    configBean.setReportConfigSendEmail(true);
    configBean.setReportConfigSendEmailWithNoData(true);
    configBean.setReportConfigStoreWithNoData(true);
    configBean.setReportConfigSendEmailToViewers(false);
    configBean.setReportConfigQuery("ids");
    configBean.setSqlConfig(GroupMembersExportRequestLimiter.requesterString(requester));
    GrouperReportConfigService.saveOrUpdateReportConfigAttributes(configBean, group);
    return GrouperReportConfigService.getGrouperReportConfigBean(group, configName);
  }

  /**
   * make a saved config look older than it is
   * @param configBean
   * @param ageMillis
   */
  private static void backdate(GrouperReportConfigurationBean configBean, long ageMillis) {
    new GcDbAccess().sql("update grouper_attribute_assign set created_on = ? where id = ?")
        .addBindVar(System.currentTimeMillis() - ageMillis).addBindVar(configBean.getAttributeAssignmentMarkerId())
        .executeSql();
    // the raw sql bypasses hibernate, so flush the cached assign (FindById query cache region)
    // or the limiter still sees the original created_on
    EhcacheController.ehcacheController().flushCache();
  }

  /**
   * @param group
   * @param configName
   * @return true if the config is still on the group
   */
  private static boolean configExists(Group group, String configName) {
    return GrouperReportConfigService.getGrouperReportConfigBean(group, configName) != null;
  }

  /**
   * a second export of the same group by the same requester is refused while one is pending;
   * an old one is deleted and replaced; another requester is not affected
   */
  public void testOnePendingExportPerGroup() {

    GrouperSession.startRootSession();
    Group group = readableGroup("test:export1");

    GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectTestHelper.SUBJ0);
    GrouperReportConfigurationBean first = saveExportConfig(group, SubjectTestHelper.SUBJ0);

    try {
      GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectTestHelper.SUBJ0);
      fail("second export while one is pending must be refused");
    } catch (GroupMembersExportRequestLimiter.ExportRequestRefusedException e) {
      assertEquals(GroupMembersExportRequestLimiter.ExportRequestRefusedException.REASON_PENDING, e.getReason());
    }

    // another requester is not limited by SUBJ0's export
    GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectTestHelper.SUBJ1);

    // once the pending window is over, the old export is deleted and a new one allowed
    backdate(first, GroupMembersExportRequestLimiter.PENDING_WINDOW_MILLIS + 60000L);
    GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectTestHelper.SUBJ0);
    assertFalse("old export replaced", configExists(group, first.getReportConfigName()));

    // wheel / root are not limited
    saveExportConfig(group, SubjectTestHelper.SUBJ0);
    GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectFinder.findRootSubject());
  }

  /**
   * a requester can start only a few exports per hour across all groups
   */
  public void testTooManyRecentExports() {

    GrouperSession.startRootSession();
    for (int i = 0; i < GroupMembersExportRequestLimiter.MAX_RECENT_EXPORTS_PER_REQUESTER; i++) {
      Group group = readableGroup("test:exportMany" + i);
      GroupMembersExportRequestLimiter.assertCanSubmit(group, SubjectTestHelper.SUBJ0);
      saveExportConfig(group, SubjectTestHelper.SUBJ0);
    }
    Group oneMore = readableGroup("test:exportManyLast");
    try {
      GroupMembersExportRequestLimiter.assertCanSubmit(oneMore, SubjectTestHelper.SUBJ0);
      fail("too many recent exports must be refused");
    } catch (GroupMembersExportRequestLimiter.ExportRequestRefusedException e) {
      assertEquals(GroupMembersExportRequestLimiter.ExportRequestRefusedException.REASON_TOO_MANY, e.getReason());
    }
    GroupMembersExportRequestLimiter.assertCanSubmit(oneMore, SubjectTestHelper.SUBJ1);
  }

  /**
   * the export job is one shot: not durable and a single non repeating trigger, so Quartz removes
   * it after it runs (success or failure).  It used to be a durable job with no trigger.
   * @throws Exception
   */
  public void testScheduleOneShotIsNotDurable() throws Exception {

    GrouperSession.startRootSession();
    Group group = readableGroup("test:exportJob");
    GrouperReportConfigurationBean configBean = saveExportConfig(group, SubjectTestHelper.SUBJ0);

    // the scheduler may run the job right away; then it is already gone, which is also fine
    GroupMembersExportRequestLimiter.scheduleOneShot(group, configBean);

    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    JobKey jobKey = JobKey.jobKey("grouper_report_" + group.getId() + "_" + configBean.getAttributeAssignmentMarkerId());
    try {
      JobDetail jobDetail = scheduler.getJobDetail(jobKey);
      if (jobDetail != null) {
        assertFalse("export job must not be durable", jobDetail.isDurable());
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        assertEquals("one trigger", 1, triggers.size());
        assertTrue("simple trigger", triggers.get(0) instanceof SimpleTrigger);
        assertEquals("no repeat", 0, ((SimpleTrigger)triggers.get(0)).getRepeatCount());
      }
      // else: the scheduler already ran it and removed it, which is the point
    } finally {
      scheduler.deleteJob(jobKey);
    }
  }

  /**
   * the clear daemon deletes stale exports entirely: one that never produced an instance a day
   * after it was requested; fresh exports and normal report configs are kept
   * @throws Exception
   */
  public void testDeleteStaleExports() throws Exception {

    GrouperSession.startRootSession();
    Group group = readableGroup("test:exportStale");

    GrouperReportConfigurationBean stale = saveExportConfig(group, SubjectTestHelper.SUBJ0);
    backdate(stale, GroupMembersExportRequestLimiter.NO_INSTANCE_STALE_MILLIS + 60000L);
    GroupMembersExportRequestLimiter.scheduleOneShot(group, stale);

    GrouperReportConfigurationBean fresh = saveExportConfig(group, SubjectTestHelper.SUBJ1);

    assertTrue("at least the stale export deleted", GroupMembersExportRequestLimiter.deleteStaleExports() >= 1);

    assertFalse("stale export config deleted", configExists(group, stale.getReportConfigName()));
    assertTrue("fresh export kept", configExists(group, fresh.getReportConfigName()));

    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    assertNull("stale export job deleted", scheduler.getJobDetail(
        JobKey.jobKey("grouper_report_" + group.getId() + "_" + stale.getAttributeAssignmentMarkerId())));
  }

}
