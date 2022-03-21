package edu.internet2.middleware.grouper.app.messagingProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class MessagingProvisionerTestUtils {
  
  public static void setupMessagingExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.messagingConnector.messaging1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/messaging").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.messagingConnector.messaging1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1H").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.messagingConnector.messaging1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.messagingConnector.messaging1.useSsl").value(ssl ? "true":"false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.messaging.mock.configId").value("messaging1").store();

  }
  
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

    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.messaging.GrouperMessagingProvisioner");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "messagingExternalSystemConfigId", "messaging1");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.insert", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.name", "loginId");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.update", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.0.valueType", "string");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.1.name", "id");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.1.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetEntityAttribute.1.valueType", "string");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.0.valueType", "string");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.update", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.1.valueType", "string");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(messagingProvisioningTestConfigInput, "targetGroupAttribute.2.valueType", "string");
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
