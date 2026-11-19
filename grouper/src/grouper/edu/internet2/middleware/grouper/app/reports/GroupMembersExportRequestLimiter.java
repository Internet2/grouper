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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Limits and cleanup for the emailed group members export (UiV2GroupImport.groupExportEmailSubmit).
 *
 * Why: the emailed export needs only READ on the group, and every request saves a system managed
 * GROUP_MEMBERS_EXPORT report config on the group, registers a durable Quartz job, and runs a
 * report as root.  The config and job were only removed when someone downloaded the export, so a
 * READ user could submit without limit and leave configs and jobs behind forever.
 *
 * This class:
 * - assertCanSubmit: at most one export per requester per group in PENDING_WINDOW_MILLIS (an older
 *   one on the same group is deleted and replaced), and at most MAX_RECENT_EXPORTS_PER_REQUESTER
 *   per requester across all groups in that window.  Wheel and root are not limited.
 * - scheduleOneShot: registers the job as non durable with a one shot trigger, so Quartz removes
 *   the job after it runs, whether it succeeds or fails.
 * - deleteStaleExports: called by the reports clear daemon, deletes the whole export (instances,
 *   stored file, config, job) once it is stale, not only its instances.
 *
 * The limits are constants so this class can ship as a drop in file; making them configurable is
 * a mainline follow-on.  Only uses APIs that exist in 7.6.0 and later (the export is new in 7.6.0).
 * </pre>
 */
public class GroupMembersExportRequestLimiter {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GroupMembersExportRequestLimiter.class);

  /** config name prefix the UI uses for the per request export configs */
  public static final String CONFIG_NAME_PREFIX = "groupMembersExport_";

  /** a requester can have one export per group in this window (60 minutes) */
  public static final long PENDING_WINDOW_MILLIS = 60L * 60L * 1000L;

  /** a requester can start this many exports across all groups in the window */
  public static final int MAX_RECENT_EXPORTS_PER_REQUESTER = 5;

  /** an export config with no report instance this old did not run (or failed before saving one): 1 day */
  public static final long NO_INSTANCE_STALE_MILLIS = 24L * 60L * 60L * 1000L;

  /** an export whose newest instance is this old is deleted, same 30 days as the instance cleanup */
  public static final long INSTANCE_RETENTION_MILLIS = 30L * 24L * 60L * 60L * 1000L;

  /**
   * thrown when an export request is refused.  The reason is a short code the UI maps to a message
   */
  public static class ExportRequestRefusedException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /** export already pending on this group */
    public static final String REASON_PENDING = "pending";

    /** too many recent exports */
    public static final String REASON_TOO_MANY = "tooMany";

    /** reason code */
    private final String reason;

    /**
     * @param reason code
     * @param message for logs
     */
    public ExportRequestRefusedException(String reason, String message) {
      super(message);
      this.reason = reason;
    }

    /**
     * @return the reason code
     */
    public String getReason() {
      return this.reason;
    }
  }

  /**
   * no instances
   */
  private GroupMembersExportRequestLimiter() {
  }

  /**
   * requester string stored in the export's sqlConfig (same format the UI uses)
   * @param subject
   * @return sourceId::subjectId
   */
  public static String requesterString(Subject subject) {
    return subject.getSourceId() + "::" + subject.getId();
  }

  /**
   * see if a config bean is an emailed group members export
   * @param configBean
   * @return true if so
   */
  private static boolean isGroupMembersExport(GrouperReportConfigurationBean configBean) {
    return configBean != null && configBean.getReportConfigType() == ReportConfigType.GROUP_MEMBERS_EXPORT
        && configBean.getReportConfigName() != null && configBean.getReportConfigName().startsWith(CONFIG_NAME_PREFIX);
  }

  /**
   * when the config's marker assignment was created
   * @param configBean
   * @return millis since 1970, or null if unknown
   */
  private static Long createdMillis(GrouperReportConfigurationBean configBean) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(configBean.getAttributeAssignmentMarkerId(), false);
    return attributeAssign == null ? null : attributeAssign.getCreatedOnDb();
  }

  // ---------------------------------------------------------------------------------------------
  // submit
  // ---------------------------------------------------------------------------------------------

  /**
   * make sure the requester may start another emailed export of this group.  Deletes an older
   * export of the same group by the same requester (it is replaced by the new one).  Runs as root.
   * @param group being exported
   * @param requester logged in subject
   * @throws ExportRequestRefusedException if refused
   */
  public static void assertCanSubmit(final Group group, final Subject requester) {

    // admins are not limited
    if (PrivilegeHelper.isWheelOrRoot(requester)) {
      return;
    }

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        final String requesterString = requesterString(requester);
        final long now = System.currentTimeMillis();

        // one per group: refuse a recent one, delete and replace an older one
        for (GrouperReportConfigurationBean configBean : GrouperReportConfigService.getGrouperReportConfigs(group)) {
          if (!isGroupMembersExport(configBean) || !requesterString.equals(configBean.getSqlConfig())) {
            continue;
          }
          Long created = createdMillis(configBean);
          if (created != null && now - created < PENDING_WINDOW_MILLIS) {
            throw new ExportRequestRefusedException(ExportRequestRefusedException.REASON_PENDING,
                "Export of group " + group.getName() + " already requested by " + requesterString);
          }
          deleteExport(group, configBean);
        }

        // across all groups: count this requester's recent exports
        int recentCount = 0;
        Set<AttributeAssign> attributeAssigns = GrouperReportConfigService.getAllAttributeAssignsForReportConfigs();
        for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {
          // only group owned configs can be exports; skip the rest before building the bean
          if (GrouperUtil.isBlank(attributeAssign.getOwnerGroupId())) {
            continue;
          }
          Long created = attributeAssign.getCreatedOnDb();
          if (created == null || now - created >= PENDING_WINDOW_MILLIS) {
            continue;
          }
          GrouperReportConfigurationBean configBean = null;
          try {
            configBean = GrouperReportConfigService.buildGrouperReportConfigurationBeanFromAssign(attributeAssign);
          } catch (RuntimeException re) {
            // a broken config should not block exports
            LOG.debug("Cant build report config bean for assign: " + attributeAssign.getId(), re);
            continue;
          }
          if (isGroupMembersExport(configBean) && requesterString.equals(configBean.getSqlConfig())) {
            recentCount++;
          }
        }
        if (recentCount >= MAX_RECENT_EXPORTS_PER_REQUESTER) {
          throw new ExportRequestRefusedException(ExportRequestRefusedException.REASON_TOO_MANY,
              "Too many recent group exports by " + requesterString + ": " + recentCount);
        }
        return null;
      }
    });
  }

  /**
   * register the export's report job so it runs once and is then removed by Quartz.  It is not
   * durable, and its one shot trigger completes after the run whether the run succeeds or fails,
   * so no job is left behind.  The job name is the one GrouperReportJob and the download cleanup
   * expect.
   * @param group exported group
   * @param savedBean saved export config (with its marker assignment id)
   * @throws SchedulerException
   */
  public static void scheduleOneShot(Group group, GrouperReportConfigurationBean savedBean) throws SchedulerException {

    String jobName = "grouper_report_" + group.getId() + "_" + savedBean.getAttributeAssignmentMarkerId();

    JobDetail jobDetail = JobBuilder.newJob(GrouperReportJob.class)
        .withIdentity(jobName)
        .build();

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOneShot_" + jobName)
        .forJob(jobDetail)
        .startNow()
        .build();

    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    scheduler.scheduleJob(jobDetail, trigger);
  }

  // ---------------------------------------------------------------------------------------------
  // cleanup
  // ---------------------------------------------------------------------------------------------

  /**
   * delete stale emailed exports entirely (instances, stored file, config, job): exports that have
   * no instance a day after they were requested (the run failed or never happened), and exports
   * whose newest instance is past the retention.  Called by the reports clear daemon.  Runs as root.
   * @return number of exports deleted
   */
  public static int deleteStaleExports() {

    if (!GrouperReportSettings.grouperReportsEnabled()) {
      return 0;
    }

    return (Integer)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        int deleted = 0;
        long now = System.currentTimeMillis();

        Set<AttributeAssign> attributeAssigns = GrouperReportConfigService.getAllAttributeAssignsForReportConfigs();
        for (AttributeAssign attributeAssign : new ArrayList<AttributeAssign>(GrouperUtil.nonNull(attributeAssigns))) {

          try {
            // exports are always owned by a group
            if (GrouperUtil.isBlank(attributeAssign.getOwnerGroupId())) {
              continue;
            }
            GrouperReportConfigurationBean configBean = GrouperReportConfigService.buildGrouperReportConfigurationBeanFromAssign(attributeAssign);
            if (!isGroupMembersExport(configBean)) {
              continue;
            }
            Group group = attributeAssign.getOwnerGroup();

            List<GrouperReportInstance> instances = GrouperReportInstanceService.getReportInstances(group, configBean.getAttributeAssignmentMarkerId());

            boolean stale = false;
            if (GrouperUtil.length(instances) == 0) {
              Long created = attributeAssign.getCreatedOnDb();
              stale = created != null && now - created > NO_INSTANCE_STALE_MILLIS;
            } else {
              long newest = 0;
              for (GrouperReportInstance instance : instances) {
                Long millis = instance.getReportInstanceMillisSince1970();
                if (millis != null && millis > newest) {
                  newest = millis;
                }
              }
              stale = now - newest > INSTANCE_RETENTION_MILLIS;
            }

            if (stale) {
              deleteExport(group, configBean);
              deleted++;
            }
          } catch (Exception e) {
            // keep going, one bad config should not stop the cleanup
            LOG.error("Error deleting stale group members export, assign: " + attributeAssign.getId(), e);
          }
        }
        return deleted;
      }
    });
  }

  /**
   * delete one export: database stored file, instances (and S3 / filesystem file), config, job
   * @param group owner
   * @param configBean export config
   */
  private static void deleteExport(Group group, GrouperReportConfigurationBean configBean) {
    try {
      // the instance delete does not remove a database stored file in every release, so remove it
      // first (a no-op when the file is not stored in the database), as the export download does
      for (GrouperReportInstance instance : GrouperUtil.nonNull(
          GrouperReportInstanceService.getReportInstances(group, configBean.getAttributeAssignmentMarkerId()))) {
        if (!instance.isReportStoredInS3()) {
          GrouperReportLogic.deleteFileFromDatabase(instance);
        }
      }
      // instances (and S3 / filesystem files), the config, and the quartz job by name
      GrouperReportConfigService.deleteGrouperReportConfig(group, configBean);
    } catch (SchedulerException se) {
      throw new RuntimeException("Error deleting group members export " + configBean.getReportConfigName()
          + " on group " + group.getName(), se);
    }
  }

}
