package edu.internet2.middleware.grouper.app.daemon;

import java.util.Map;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderIncrementalJob;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class GrouperDaemonConfigurationTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonConfigurationTest("testRetrieveImplementationFromJobName"));
  }
  
  public GrouperDaemonConfigurationTest(String name) {
    super(name);
  }

  public void testRetrieveAttributes() {
    
    setupOtherFindBadMembershipDaemonConfiguration();
    
    GrouperDaemonConfiguration configuration = new GrouperDaemonOtherJobFindBadMembershipsConfiguration();
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = configuration.retrieveAttributes();
    
    assertEquals(GrouperUtil.toStringForLog(attributes), 3, attributes.size());
    
    configuration = new GrouperDaemonOtherJobConfiguration();
    configuration.setConfigId("notExist");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 3, attributes.size());

    configuration = new GrouperDaemonOtherJobConfiguration();
    configuration.setConfigId("xyz");
    attributes = configuration.retrieveAttributes();
    
    assertEquals(GrouperUtil.toStringForLog(attributes), 4, attributes.size());
    
    configuration = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
    configuration.setConfigId("notExist");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 6, attributes.size());
    
    configuration = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
    configuration.setConfigId("abc");
    attributes = configuration.retrieveAttributes();

    assertEquals(GrouperUtil.toStringForLog(attributes), 7, attributes.size());
    
  }
  
  public void testRetrieveImplementationFromJobName() {
    
    GrouperDaemonConfiguration grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("CHANGE_LOG_changeLogTempToChangeLog");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonChangeLogTempToChangeLogConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("MAINTENANCE__builtinMessagingDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonBuiltInMessagingConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("CHANGE_LOG_consumer_grouperRules");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonChangeLogRulesConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("CHANGE_LOG_consumer_recentMemberships");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonChangeLogRecentMembershipsConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("CHANGE_LOG_consumer_syncGroups");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonChangeLogSyncGroupsConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("MAINTENANCE__enabledDisabled");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonEnabledDisabledConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("MAINTENANCE__rules");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonRulesConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("MAINTENANCE_cleanLogs");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonCleanLogsConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("MESSAGE_LISTENER_rabbitListener");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonMessagingListenerConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_attestationDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobAttestationConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_deprovisioningDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobDeprovisioningConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_findBadMemberships");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobFindBadMembershipsConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_grouperObjectTypeDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobObjectTypeConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_grouperProvisioningDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobProvisioningConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_grouperReportClearDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobReportClearConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_grouperWorkflowDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobWorkflowConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_grouperWorkflowReminderDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobWorkflowReminderConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_schedulerCheckDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobSchedulerCheckConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_tierInstrumentationDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobInstrumentationConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_upgradeTasks");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobUpgradeTasksConfiguration);
    
    grouperDaemonConfig = GrouperDaemonConfiguration.retrieveImplementationFromJobName("OTHER_JOB_usduDaemon");
    assertTrue(grouperDaemonConfig instanceof GrouperDaemonOtherJobUsduConfiguration);
    
  }
  
  private static void setupOtherFindBadMembershipDaemonConfiguration() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.findBadMemberships.somethingElse", "whatever");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.class", "a.b.c.MyOtherJob");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.quartzCron", "123");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.xyz.somethingElse2", "345");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.class", GrouperLoaderIncrementalJob.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.quartzCron", "qwe");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.databaseName", "ert");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.tableName", "rty");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.fullSyncThreshold", "tyu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.abc.somethingElse3", "vbn");

  }
 
}
