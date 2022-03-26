package edu.internet2.middleware.grouper.app.messagingProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class MessagingProvisionerTestUtils {
  
  /**
   * 
   * @param messagingProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(MessagingProvisionerTestConfigInput messagingProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!messagingProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + messagingProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param messagingProvisioningTestConfigInput     
   * MessagingProvisionerTestUtils.configureMessagingProvisioner(
   *       new MessagingProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureMessagingProvisioner(MessagingProvisionerTestConfigInput messagingProvisioningTestConfigInput) {

    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "messagingExternalSystemConfigId", "grouperBuiltinMessaging");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.messagingProvisioning.GrouperMessagingProvisioner");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteEntitiesIfGrouperDeleted", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteGroupsIfGrouperDeleted", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteMembershipsIfGrouperDeleted", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "messagingFormatType", "EsbEventJson");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "queueOrTopicName", "testSqsQueue");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "queueType", "queue");
    
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "updateGroups", "true");


    for (String key: messagingProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = messagingProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + messagingProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.provisionerConfigId").value("myMessagingProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.messagingProvTestCLC.publisher.debug").value("true").store();

    
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
