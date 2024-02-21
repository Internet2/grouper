package edu.internet2.middleware.grouper.app.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  
  public void testInsertEditDelete() {
    
    GrouperDaemonConfiguration configuration = new GrouperDaemonChangeLogConsumerConfiguration();
    
    configuration.setConfigId("testChangeLogConsumer");
    
    assertEquals(4, configuration.retrieveAttributes().size());
    
    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    configuration.retrieveAttributes().get("syncAttributeName").setValue("etc:attribute:attrLoader:attributeLoader");
    
    configuration.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
    
    assertEquals(0, validationErrorsToDisplay.size());
    
    configuration = new GrouperDaemonChangeLogConsumerConfiguration();
    configuration.setConfigId("testChangeLogConsumer");
    
    configuration.retrieveAttributes().get("quartzCron").setValue("0 50 * * * ?");
    configuration.retrieveAttributes().get("retryOnError").setValue("false");
    List<String> actionsPerformed = new ArrayList<String>();
    configuration.editConfig(false, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
    
    assertEquals(0, validationErrorsToDisplay.size());
    
    configuration = new GrouperDaemonChangeLogConsumerConfiguration();
    configuration.setConfigId("testChangeLogConsumer");
    
    String value  = configuration.retrieveAttributes().get("quartzCron").getValue();
    assertEquals("0 50 * * * ?", value);
    
    value  = configuration.retrieveAttributes().get("retryOnError").getValue();
    assertEquals("false", value);
    
    configuration.deleteConfig(false);
    
    configuration = new GrouperDaemonChangeLogConsumerConfiguration();
    configuration.setConfigId("testChangeLogConsumer");
    
    value  = configuration.retrieveAttributes().get("quartzCron").getValue();
    assertEquals("", value);
    
    value  = configuration.retrieveAttributes().get("retryOnError").getValue();
    assertEquals("", value);
    
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
