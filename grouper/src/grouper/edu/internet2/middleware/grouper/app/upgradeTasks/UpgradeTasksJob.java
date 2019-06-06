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

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
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
    new UpgradeTasksJob().run(otherJobInput);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String groupName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_METADATA_GROUP;
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    String upgradeTasksVersionName = grouperUpgradeTasksStemName() + ":" + UpgradeTasksJob.UPGRADE_TASKS_VERSION_ATTR;
    String versionString = group.getAttributeValueDelegate().retrieveValueString(upgradeTasksVersionName);
    
    int oldDBVersion = Integer.parseInt(versionString);    
    int newDBVersion = UpgradeTasks.currentVersion();
    
    for (int version = oldDBVersion + 1; version <= newDBVersion; version++) {
      String enumName = "V" + version;
      UpgradeTasksInterface task = Enum.valueOf(UpgradeTasks.class, enumName);
      task.updateVersionFromPrevious();
      group.getAttributeValueDelegate().assignValue(upgradeTasksVersionName, "" + version);
      
      LOG.info("Upgraded to version " + enumName);
    }
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Finished running job, previousVersion=" + oldDBVersion + ", currentVersion=" + newDBVersion);
    otherJobInput.getHib3GrouperLoaderLog().store();

    LOG.info("UpgradeTasksJob finished successfully.");
    return null;
  }
  
  /**
   * stem name for upgrade tasks
   * @return stem name
   */
  public static String grouperUpgradeTasksStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":upgradeTasks";
  }
}
