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

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
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
    
        if (sortedOldDbVersions.contains(version)) {
          // version is already there; skip it
        } else {
          String enumName = "V" + version;
          UpgradeTasks task = GrouperUtil.enumValueOfIgnoreCase(UpgradeTasks.class, enumName, false, false);
          if (task != null ) {         
            
            UpgradeTasksInterface upgradeTasksInterface = task.upgradeTask();
            boolean upgradeTaskIsDdl = upgradeTasksInterface.upgradeTaskIsDdl();
            boolean doesUpgradeTaskHaveDdlWorkToDo = upgradeTasksInterface.doesUpgradeTaskHaveDdlWorkToDo();
            boolean doTask = true;
            
            if (GrouperDdlEngine.installedGrouperFromScratchWithRunScript) {
              if (!upgradeTasksInterface.runOnNewInstall()) {
                doTask = false;
                
                group.getAttributeValueDelegate().addValue(upgradeTasksVersionName, "" + version);
                otherJobInput.getHib3GrouperLoaderLog().appendJobMessage("Skipping upgrade due to new install to version "+enumName + ". \n");
              }
            } else {
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
}
