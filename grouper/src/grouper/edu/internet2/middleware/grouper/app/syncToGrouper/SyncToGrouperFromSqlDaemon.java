package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SyncToGrouperFromSqlDaemon extends OtherJobBase {

  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_syncToGrouperFromTrainingDb");

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(SyncToGrouperFromSqlDaemon.class);

  
  public SyncToGrouperFromSqlDaemon() {
  }

  private OtherJobInput otherJobInput = null;

  private SyncToGrouper syncToGrouper = null;
  
  /**
   * this is also the config id
   */
  private String jobName = null;
  
  private SyncToGrouperReport syncToGrouperReport = null;
  
  private boolean logOutput = false;
  
  @Override
  public OtherJobOutput run(OtherJobInput theOtherJobInput) {
    
    this.otherJobInput = theOtherJobInput;
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        jobName = otherJobInput.getJobName();
        
        // jobName = OTHER_JOB_csvSync
        jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

        syncToGrouper = new SyncToGrouper();

        configureSync();

        RuntimeException runtimeException = null;
        // pricess report even if exception
        
        try {
          syncToGrouper.syncLogic();
        } catch (RuntimeException re) {
          runtimeException = re;
        }
        syncToGrouperReport = syncToGrouper.getSyncToGrouperReport();
        
        generateReports();
            
        otherJobInput.getHib3GrouperLoaderLog().setJobMessage(abbreviatedReport.toString());
        
        if (logOutput && LOG.isDebugEnabled()) {
          LOG.debug(fullReport.toString());
        }
        
        if (runtimeException != null) {
          throw runtimeException;
        }
        return null;
      }
    });
    
    return null;
  }

  public void generateReports() {
    appendToReports(true, true, "differences", syncToGrouperReport.getDifferenceCountOverall(), false);
    appendToReports(true, true, "changeCount", syncToGrouperReport.getChangeCountOverall(), false);
    appendToReports(true, true, "errors", GrouperUtil.length(syncToGrouperReport.getErrorLines()), false);
    
    appendToReports(true, true, "stemInserts", syncToGrouperReport.getStemInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getStemInserts());
    appendToReports(true, true, "stemUpdates", syncToGrouperReport.getStemUpdates(), true);
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(syncToGrouperReport.getStemUpdates());
    appendToReports(true, true, "stemDeletes", syncToGrouperReport.getStemDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getStemDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncStemToGrouperBeans()));

    
    appendToReports(true, true, "groupInserts", syncToGrouperReport.getGroupInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getGroupInserts());
    appendToReports(true, true, "groupUpdates", syncToGrouperReport.getGroupUpdates(), true);
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(syncToGrouperReport.getGroupUpdates());
    appendToReports(true, true, "groupDeletes", syncToGrouperReport.getGroupDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getGroupDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperBeans()));

    appendToReports(true, true, "compositeInserts", syncToGrouperReport.getCompositeInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getCompositeInserts());
    appendToReports(true, true, "compositeUpdates", syncToGrouperReport.getCompositeUpdates(), true);
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(syncToGrouperReport.getCompositeUpdates());
    appendToReports(true, true, "compositeDeletes", syncToGrouperReport.getCompositeDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getCompositeDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncCompositeToGrouperBeans()));

    appendToReports(true, true, "membershipInserts", syncToGrouperReport.getMembershipInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getMembershipInserts());
    appendToReports(true, true, "membershipUpdates", syncToGrouperReport.getMembershipUpdates(), true);
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(syncToGrouperReport.getMembershipUpdates());
    appendToReports(true, true, "membershipDeletes", syncToGrouperReport.getMembershipDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getMembershipDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncMembershipToGrouperBeans()));

    appendToReports(true, true, "groupPrivInserts", syncToGrouperReport.getPrivilegeGroupInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getPrivilegeGroupInserts());
    appendToReports(true, true, "groupPrivDeletes", syncToGrouperReport.getPrivilegeGroupDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getPrivilegeGroupDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncPrivilegeGroupToGrouperBeans()));

    appendToReports(true, true, "stemPrivInserts", syncToGrouperReport.getPrivilegeStemInserts(), true);
    otherJobInput.getHib3GrouperLoaderLog().addInsertCount(syncToGrouperReport.getPrivilegeStemInserts());
    appendToReports(true, true, "stemPrivDeletes", syncToGrouperReport.getPrivilegeStemDeletes(), true);
    otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(syncToGrouperReport.getPrivilegeStemDeletes());
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(syncToGrouper.getSyncPrivilegeStemToGrouperBeans()));

    appendToReports(true, true, 500, "errors", GrouperUtil.toStringForLog(syncToGrouperReport.getErrorLines()));
    appendToReports(true, true, 500, "output", GrouperUtil.toStringForLog(syncToGrouperReport.getOutputLines()));

    appendToReports(true, true, 100, "stemInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getStemInsertsNames()));
    appendToReports(true, true, 100, "stemUpdateNames", GrouperUtil.toStringForLog(syncToGrouperReport.getStemUpdatesNames()));
    appendToReports(true, true, 100, "stemDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getStemDeletesNames()));

    appendToReports(true, true, 100, "groupInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getGroupInsertsNames()));
    appendToReports(true, true, 100, "groupUpdateNames", GrouperUtil.toStringForLog(syncToGrouperReport.getGroupUpdatesNames()));
    appendToReports(true, true, 100, "groupDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getGroupDeletesNames()));

    appendToReports(true, true, 100, "compositeInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getCompositeInsertsNames()));
    appendToReports(true, true, 100, "compositeUpdateNames", GrouperUtil.toStringForLog(syncToGrouperReport.getCompositeUpdatesNames()));
    appendToReports(true, true, 100, "compositeDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getCompositeDeletesNames()));

    appendToReports(true, true, 100, "membershipInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getMembershipInsertsNames()));
    appendToReports(true, true, 100, "membershipUpdateNames", GrouperUtil.toStringForLog(syncToGrouperReport.getMembershipUpdatesNames()));
    appendToReports(true, true, 100, "membershipDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getMembershipDeleteNames()));

    appendToReports(true, true, 100, "groupPrivInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getPrivilegeGroupInsertsNames()));
    appendToReports(true, true, 100, "groupPrivDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getPrivilegeGroupDeleteNames()));

    appendToReports(true, true, 100, "stemPrivInsertNames", GrouperUtil.toStringForLog(syncToGrouperReport.getPrivilegeStemInsertsNames()));
    appendToReports(true, true, 100, "stemPrivDeleteNames", GrouperUtil.toStringForLog(syncToGrouperReport.getPrivilegeStemDeleteNames()));

    // maybe everything just fits
    if (this.fullReport.length() < 4000) {
      this.abbreviatedReport = this.fullReport;
    }
  }

  public void configureSync() {
    // notification, summary
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId(GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperDatabaseConfigId"));
    
    syncToGrouper.setReadWrite(!GrouperLoaderConfig
        .retrieveConfig().propertyValueBooleanRequired("otherJob." + jobName + ".sqlSyncToGrouperReadonly"));
    
    this.logOutput = GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperLogOutput", false);
    
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoadFromAnotherGrouper(GrouperLoaderConfig
        .retrieveConfig().propertyValueBooleanRequired("otherJob." + jobName + ".sqlSyncToGrouperFromAnotherGrouper"));
    
    if (syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
      syncToGrouper.getSyncToGrouperFromSql().setDatabaseSyncFromAnotherGrouperSchema(GrouperLoaderConfig
          .retrieveConfig().propertyValueString("otherJob." + jobName + ".sqlSyncToGrouperDatabaseSyncFromAnotherGrouperSchema"));
    }
    
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoadAutoConfigureColumns(GrouperLoaderConfig
        .retrieveConfig().propertyValueBooleanRequired("otherJob." + jobName + ".sqlSyncToGrouperAutoconfigureColumns"));
    
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseSyncFromAnotherGrouperTopLevelStems(GrouperUtil.splitTrimToList(
        GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperTopLevelStems"), ","));

    syncToGrouper.getSyncToGrouperBehavior().setStemSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isStemSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setStemSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setStemSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperStemSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setStemInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemUpdate", false));

      syncToGrouper.getSyncToGrouperBehavior().setStemDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldAlternateName(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSyncFieldAlternateName", false));
        syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSyncFieldDescription", false));
        syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSyncFieldDisplayName", false));
        if (syncToGrouper.getSyncToGrouperBehavior().isStemInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdIndexOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSyncFieldIdIndexOnInsert", false));
          syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperStemSyncFieldIdOnInsert", false));
        }
      }
    }
    
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isGroupSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setGroupSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperGroupSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupUpdate", false));

      syncToGrouper.getSyncToGrouperBehavior().setGroupDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldAlternateName", false));
        syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldDescription", false));
        syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldDisplayName", false));
        syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldEnabledDisabled(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldEnabledDisabled", false));
        if (syncToGrouper.getSyncToGrouperBehavior().isGroupInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldIdIndexOnInsert", false));
          syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldIdOnInsert", false));
        }
        syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldTypeOfGroup(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperGroupSyncFieldTypeOfGroup", false));
      }
    }

    syncToGrouper.getSyncToGrouperBehavior().setCompositeSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperCompositeSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isCompositeSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setCompositeSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperCompositeSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setCompositeInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperCompositeInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setCompositeUpdate(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperCompositeUpdate", false));

      syncToGrouper.getSyncToGrouperBehavior().setCompositeDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperCompositeDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        if (syncToGrouper.getSyncToGrouperBehavior().isCompositeInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperCompositeSyncFieldIdOnInsert", false));
        }
      }
    }

    syncToGrouper.getSyncToGrouperBehavior().setMembershipSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isMembershipSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setMembershipSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setMembershipSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperMembershipSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setMembershipInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setMembershipUpdate(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipUpdate", false));

      syncToGrouper.getSyncToGrouperBehavior().setMembershipDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        syncToGrouper.getSyncToGrouperBehavior().setMembershipSyncFieldsEnabledDisabled(GrouperLoaderConfig
            .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipSyncFieldsEnabledDisabled", false));
        if (syncToGrouper.getSyncToGrouperBehavior().isMembershipInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setMembershipSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperMembershipSyncFieldIdOnInsert", false));
        }
      }
    }

    syncToGrouper.getSyncToGrouperBehavior().setPrivilegeGroupSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeGroupSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeGroupSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setPrivilegeGroupSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeGroupSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeGroupInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeGroupInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeGroupDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeGroupDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        if (syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setPrivilegeGroupSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeGroupSyncFieldIdOnInsert", false));
        }
      }
    }

    syncToGrouper.getSyncToGrouperBehavior().setPrivilegeStemSync(GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeStemSync", false));

    if (syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSync()) {

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeStemSyncFromStems(true);

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
        syncToGrouper.getSyncToGrouperFromSql().setPrivilegeStemSql(GrouperLoaderConfig
            .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeStemSql"));
      }

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeStemInsert(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeStemInsert", false));

      syncToGrouper.getSyncToGrouperBehavior().setPrivilegeStemDeleteExtra(GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeStemDeleteExtra", false));

      if (!syncToGrouper.getSyncToGrouperBehavior().isSqlLoadAutoConfigureColumns()) {
        if (syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemInsert()) {
          syncToGrouper.getSyncToGrouperBehavior().setPrivilegeStemSyncFieldIdOnInsert(GrouperLoaderConfig
              .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sqlSyncToGrouperPrivilegeStemSyncFieldIdOnInsert", false));
        }
      }
    }
  }

  private static Set<String> logEntriesToIgnore = GrouperUtil.toSet(
      GrouperUtil.toStringForLog(new ArrayList<String>()),
      GrouperUtil.toStringForLog(new HashSet<String>()),
      GrouperUtil.toStringForLog(new TreeSet<String>())); 
  
  private void appendToReports(boolean abbreviated, boolean full, String label, int value, boolean onlyShowIfNonZero) {
    if (!onlyShowIfNonZero || value != 0) {
      appendToReports(abbreviated, full, -1, label, Integer.toString(value));
    }
  }

  private void appendToReports(boolean abbreviated, boolean full, int maxForAbbreviated, String label, String value) {
    
    // dont log empty collections
    if (logEntriesToIgnore.contains(value)) {
      return;
    }
    this.appendToReport(abbreviatedReport, abbreviated, maxForAbbreviated, label, value);
    this.appendToReport(fullReport, full, -1, label, value);
  }
  
  private void appendToReport(StringBuilder report, boolean shouldAppend, int maxLength, String label, String value) {
    if (shouldAppend) {
      if (maxLength > 0) {
        value = StringUtils.abbreviate(value, maxLength);
      }
      report.append(label).append(": ").append(value).append("\n");
    }
  }
  
  private StringBuilder abbreviatedReport = new StringBuilder();
  private StringBuilder fullReport = new StringBuilder();

}
