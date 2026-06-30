/**
 * Copyright 2019 Internet2
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

package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.ddl.DdlVersionable;
import edu.internet2.middleware.grouper.ddl.GrouperDdlEngine;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * @author shilen
 */
@DisallowConcurrentExecution
public class UpgradeTasksJob extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTasksJob.class);
  
  /**
   * attribute definition for upgrade tasks
   */
  public static final String UPGRADE_TASKS_DEF = "upgradeTasksDef";

  /**
   * version
   */
  public static final String UPGRADE_TASKS_VERSION_ATTR = "upgradeTasksVersion";
  
  /**
   * group holding metadata
   */
  public static final String UPGRADE_TASKS_METADATA_GROUP = "upgradeTasksMetadataGroup";
  
  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static String runDaemonStandalone() {
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public String callback(GrouperSession grouperSession) throws GrouperSessionException {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        
        hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
        String jobName = "OTHER_JOB_upgradeTasks";

        hib3GrouperLoaderLog.setJobName(jobName);
        hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();
        
        OtherJobInput otherJobInput = new OtherJobInput();
        otherJobInput.setJobName(jobName);
        otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
        otherJobInput.setGrouperSession(grouperSession);
        try {          
          new UpgradeTasksJob().run(otherJobInput);
          if (!GrouperLoaderStatus.valueOfIgnoreCase(hib3GrouperLoaderLog.getStatus(), true).isError()) {
            hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
          }
          hib3GrouperLoaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
          hib3GrouperLoaderLog.store();
          GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();
          if (grouperContext == null || grouperContext.getGrouperEngine() == null
              || grouperContext.getGrouperEngine() != GrouperEngineBuiltin.JUNIT) {
            LOG.warn("Success: upgrade task output: "+hib3GrouperLoaderLog.getJobMessage());
            System.out.println("Success: upgrade task output: " + hib3GrouperLoaderLog.getJobMessage());
          } else {
            LOG.info("Success: upgrade task output: "+hib3GrouperLoaderLog.getJobMessage());
          }
        } catch (Exception e) {
          LOG.error("Error on upgrade tasks: "+hib3GrouperLoaderLog.getJobMessage(), e);
          System.out.println("Error on upgrade tasks: "+hib3GrouperLoaderLog.getJobMessage());
          e.printStackTrace();
          hib3GrouperLoaderLog.setJobMessage(GrouperUtil.getFullStackTrace(e));
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          hib3GrouperLoaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
          hib3GrouperLoaderLog.store();
        }
        
        if (GrouperLoaderStatus.valueOfIgnoreCase(hib3GrouperLoaderLog.getStatus(), true).isError()) {
          return hib3GrouperLoaderLog.getJobMessage();
        }
        return null;
      }
    });
  }
  
  public static boolean canRunDdl() {
    
    String objectName = "Grouper";
    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion(objectName); 
    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion(objectName, javaVersion);
    GrouperVersion grouperVersionJava = new GrouperVersion(ddlVersionableJava.getGrouperVersion());
    
    boolean autoDdlFor = GrouperDdlUtils.autoDdlFor(grouperVersionJava);
    return autoDdlFor;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String groupName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    String upgradeTasksVersionName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
    
    Set<Integer> sortedOldDbVersions = getDBVersions();
    
    boolean isThereWorkToDo = isThereWorkToDo(sortedOldDbVersions);
    
    if (isThereWorkToDo) {
      
      int highestEnumVersion = UpgradeTasks.currentVersion();
      otherJobInput.getHib3GrouperLoaderLog().setTotalCount(highestEnumVersion);
      for (Integer version = 1; version <= highestEnumVersion; version++) {
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();
        
        if (sortedOldDbVersions.contains(version)) {
          // version is already there; skip it
        } else {
          String enumName = "V" + version;
          UpgradeTasks task = GrouperUtil.enumValueOfIgnoreCase(UpgradeTasks.class, enumName, false, false);
          if (task != null ) {         
            
            UpgradeTasksInterface upgradeTasksInterface = task.upgradeTask();
            boolean upgradeTaskIsDdl = upgradeTasksInterface.upgradeTaskIsDdl();
            boolean doTask = true;
            
            if (GrouperDdlEngine.installedGrouperFromScratchWithRunScript) {
              if (!upgradeTasksInterface.runOnNewInstall()) {
                doTask = false;
                
                group.getAttributeValueDelegate().addValue(upgradeTasksVersionName, "" + version);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping upgrade due to new install to version "+enumName + ". \n");
              }
            } else {
              // Only the DDL detection path uses this, and it can be expensive (each assert reads
              // the full GROUPER% model).  Compute it lazily - never on the from-scratch-install path
              // above where the result is unused - and share one model read across the asserts in
              // this single check via the DDL model cache.
              boolean doesUpgradeTaskHaveDdlWorkToDo = false;
              if (upgradeTaskIsDdl) {
                GrouperDdlUtils.ddlModelCacheStart();
                try {
                  doesUpgradeTaskHaveDdlWorkToDo = upgradeTasksInterface.doesUpgradeTaskHaveDdlWorkToDo();
                } finally {
                  GrouperDdlUtils.ddlModelCacheStop();
                }
              }

              if (upgradeTaskIsDdl && !doesUpgradeTaskHaveDdlWorkToDo) {
                doTask = false;
                group.getAttributeValueDelegate().addValue(upgradeTasksVersionName, "" + version);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping upgrade task due to the ddl has been detected to have been already run "+enumName + ". \n");
              }

              if (upgradeTaskIsDdl && doesUpgradeTaskHaveDdlWorkToDo && !canRunDdl()) {
                otherJobInput.getHib3GrouperLoaderLog().addUnresolvableSubjectCount(1);
                String message = "There's DDL work to do that has been configured not to be automatic but upgrade task number "+ version + " has not been done manually yet.";
                LOG.error(message);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(message);
                otherJobInput.getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.ERROR.name());
                throw new RuntimeException(message);
              }
            }
            
            if (doTask) {
              try {     
                
                upgradeTasksInterface.updateVersionFromPrevious(otherJobInput);
                group.getAttributeValueDelegate().addValue(upgradeTasksVersionName, "" + version);
                LOG.info("Upgraded to version " + enumName);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(" Upgraded to version "+enumName + ". \n");
                otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
              } catch (RuntimeException e) {
                otherJobInput.getHib3GrouperLoaderLog().addUnresolvableSubjectCount(1);
                GrouperUtil.injectInException(e, "Upgrade task "+version + ", ");
                LOG.error("Error", e);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.getFullStackTrace(e));
                otherJobInput.getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.ERROR.name());
                throw e;
              }
       
            }
           
          }
        }
      }
      
    } else {
      int highestEnumVersion = UpgradeTasks.currentVersion();
      otherJobInput.getHib3GrouperLoaderLog().setTotalCount(highestEnumVersion);
    }
    
    otherJobInput.getHib3GrouperLoaderLog().store();

    LOG.info("UpgradeTasksJob finished successfully.");
    return null;
  }
  
  public static boolean isThereWorkToDo(Set<Integer> sortedOldDbVersions) {
    
    if (sortedOldDbVersions == null) {
      // if there are no tables then dont do anything
      try {
        new GcDbAccess().sql("select 1 from grouper_groups where id = 'abc'").select(int.class);
      } catch (Exception e) {
        return false;
      }
      sortedOldDbVersions = getDBVersions();
    }
    
    int highestEnumVersion = UpgradeTasks.currentVersion();
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    for (Integer version = 1; version <= highestEnumVersion; version++) {
      
      String enumName = "V" + version;
      UpgradeTasks task = GrouperUtil.enumValueOfIgnoreCase(UpgradeTasks.class, enumName, false, false);
      if (task != null && !sortedOldDbVersions.contains(version)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * stem name for upgrade tasks
   * @return stem name
   */
  public static String grouperUpgradeTasksStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks";
  }
  
  public static AttributeDef grouperUpgradeTasksAttributeDef() {
    String upgradeTasksDefName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_DEF;
    return AttributeDefFinder.findByName(upgradeTasksDefName, true);
  }

  /**
   * DO NOT CALL THIS METHOD SINCE THE ATTRIBUTE CHANGED TO MULTIVALUED
   */
  public static int getDBVersion() {
    String groupName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    String upgradeTasksVersionName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
    
    String versionString = group.getAttributeValueDelegate().retrieveValueString(upgradeTasksVersionName);
    
    int oldDBVersion = GrouperUtil.intValue(versionString, 0);
    return oldDBVersion;
  }
  
  public static Set<Integer> getDBVersions() {
    Set<Integer> result = new TreeSet<Integer>();
    try {
      List<String> versionsAlreadyUpgraded = new GcDbAccess().sql("""
          select value_string from grouper_aval_asn_group_v gaagv where group_name = ?
          and attribute_def_name_name = ?
          """)
          .addBindVar(GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksMetadataGroup")
          .addBindVar(GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks:upgradeTasksVersion")
          .selectList(String.class);
      
      for (String existingVersion: GrouperUtil.nonNull(versionsAlreadyUpgraded)) {
        try {          
          result.add(GrouperUtil.intValue(existingVersion, 0));
        } catch (Exception e) {
          LOG.error("Invalid upgrade version: '"+existingVersion+"'", e);
        }
      }
    } catch (Exception e) {
      LOG.error("cannot find completed upgraded tasks", e);
      throw new RuntimeException("cannot find completed upgraded tasks", e);
    }
    return result;
  }

  /**
   * Status of a single upgrade task, as shown on the Configure -&gt; Upgrade tasks admin screen.
   */
  public enum UpgradeTaskStatus {

    /** the version is recorded complete on the metadata group (the daemon will not run it again) */
    COMPLETE,

    /** the version is not recorded complete and (for a DDL task) still has DDL work to do */
    NOT_COMPLETE,

    /** a DDL task that is not recorded complete but has no DDL work to do on this database
     * (e.g. it was already applied out of band, or it is a manual-only step on this platform) */
    NOT_APPLICABLE;
  }

  /**
   * The metadata group (etc:attribute:upgradeTasks:upgradeTasksMetadataGroup) whose multivalued
   * upgradeTasksVersion attribute records which upgrade task versions have been completed.
   * @return the metadata group
   */
  public static Group grouperUpgradeTasksMetadataGroup() {
    String groupName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    return GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
  }

  /**
   * full name of the multivalued attribute def name that records completed upgrade task versions
   * @return the attribute def name
   */
  public static String grouperUpgradeTasksVersionAttributeName() {
    return grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
  }

  /**
   * Look up an upgrade task enum constant by its version number.
   * @param version e.g. 43
   * @return the UpgradeTasks enum constant, or null if there is no task for that version
   */
  public static UpgradeTasks retrieveUpgradeTask(int version) {
    return GrouperUtil.enumValueOfIgnoreCase(UpgradeTasks.class, "V" + version, false, false);
  }

  /**
   * Mark an upgrade task version complete by adding its number to the multivalued upgradeTasksVersion
   * attribute on the metadata group - the same write the daemon (run()) does inline.  This tells Grouper
   * the task is done so it will not be run again.  Idempotent: a no-op if the version is already recorded.
   * @param version the upgrade task version number
   */
  public static void markUpgradeTaskComplete(int version) {
    if (getDBVersions().contains(version)) {
      return;
    }
    grouperUpgradeTasksMetadataGroup().getAttributeValueDelegate()
        .addValue(grouperUpgradeTasksVersionAttributeName(), "" + version);
  }

  /**
   * Remove the completed marker for an upgrade task version so the daemon (or a manual run) will run it
   * again.  Idempotent: a no-op if the version is not currently recorded as complete.
   * @param version the upgrade task version number
   */
  public static void markUpgradeTaskNotComplete(int version) {
    if (!getDBVersions().contains(version)) {
      return;
    }
    grouperUpgradeTasksMetadataGroup().getAttributeValueDelegate()
        .deleteValue(grouperUpgradeTasksVersionAttributeName(), "" + version);
  }

  /**
   * Whether a single upgrade task currently has DDL work left to do.  Non-DDL tasks always return false.
   * For DDL tasks this is the expensive check (doesUpgradeTaskHaveDdlWorkToDo reads the live schema), so
   * it is wrapped in the DDL model cache - the same pattern run() uses - so the schema is read once and
   * shared across the asserts in this single check.
   * @param upgradeTasksInterface the task to check
   * @return true if this is a DDL task that still has automatic DDL work to do
   */
  public static boolean upgradeTaskHasDdlWorkToDo(UpgradeTasksInterface upgradeTasksInterface) {
    if (!upgradeTasksInterface.upgradeTaskIsDdl()) {
      return false;
    }
    GrouperDdlUtils.ddlModelCacheStart();
    try {
      return upgradeTasksInterface.doesUpgradeTaskHaveDdlWorkToDo();
    } finally {
      GrouperDdlUtils.ddlModelCacheStop();
    }
  }

  /**
   * Compute the status of a single upgrade task for display.
   * @param version the upgrade task version number
   * @param completedVersions the set of versions already recorded complete (pass getDBVersions() once
   *   and reuse across rows rather than re-querying per row)
   * @param checkDdl if true, a not-complete DDL task with no DDL work to do is reported NOT_APPLICABLE
   *   (this runs the expensive schema check via {@link #upgradeTaskHasDdlWorkToDo}); if false, a
   *   not-complete task is simply NOT_COMPLETE (cheap - no schema read)
   * @return the status
   */
  public static UpgradeTaskStatus retrieveUpgradeTaskStatus(int version, Set<Integer> completedVersions, boolean checkDdl) {
    if (completedVersions.contains(version)) {
      return UpgradeTaskStatus.COMPLETE;
    }
    if (checkDdl) {
      UpgradeTasks task = retrieveUpgradeTask(version);
      if (task != null && task.upgradeTask().upgradeTaskIsDdl() && !upgradeTaskHasDdlWorkToDo(task.upgradeTask())) {
        return UpgradeTaskStatus.NOT_APPLICABLE;
      }
    }
    return UpgradeTaskStatus.NOT_COMPLETE;
  }

  /**
   * Run a single upgrade task by version number and, on success, record it complete.  This performs the
   * same per-task work the daemon (run()) does for one version, in a standalone loader-log context so it
   * can be triggered individually - e.g. from the Configure -&gt; Upgrade tasks admin screen.  Runs as the
   * root grouper session.  Note the tasks are individually idempotent (each re-checks its own
   * preconditions), but they are designed to run in ascending version order.
   * @param version the upgrade task version number to run
   * @return the loader job message accumulated while running the task
   */
  public static String runUpgradeTask(final int version) {
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public String callback(GrouperSession grouperSession) throws GrouperSessionException {

        UpgradeTasks task = retrieveUpgradeTask(version);
        if (task == null) {
          throw new RuntimeException("There is no upgrade task for version " + version);
        }

        // standalone loader log so an individually-triggered run is recorded like the daemon's
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
        hib3GrouperLoaderLog.setJobName("OTHER_JOB_upgradeTasks_V" + version);
        hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();

        OtherJobInput otherJobInput = new OtherJobInput();
        otherJobInput.setJobName(hib3GrouperLoaderLog.getJobName());
        otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
        otherJobInput.setGrouperSession(grouperSession);

        try {
          task.upgradeTask().updateVersionFromPrevious(otherJobInput);
          // record it done so the daemon will not run it again
          markUpgradeTaskComplete(version);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
        } catch (RuntimeException e) {
          LOG.error("Error running upgrade task V" + version, e);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          hib3GrouperLoaderLog.appendJobMessage(GrouperUtil.getFullStackTrace(e));
          hib3GrouperLoaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
          hib3GrouperLoaderLog.store();
          throw e;
        }

        hib3GrouperLoaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
        hib3GrouperLoaderLog.store();

        return hib3GrouperLoaderLog.getJobMessage();
      }
    });
  }
}
