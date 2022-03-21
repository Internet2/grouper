package edu.internet2.middleware.grouper.app.azure;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class AzureProvisionerTestUtils {
  
  public static void setupAzureExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientId").value("myClient").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientSecret").value("pass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphEndpoint").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphVersion").value("v1.0").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupAttribute").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupValueFormat").value("${group.getName()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.loginEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/auth/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resource").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.tenantId").value("myTenant").store();

  }
  
  /**
   * 
   * @param azureProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(AzureProvisionerTestConfigInput azureProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!azureProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + azureProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param azureProvisioningTestConfigInput     
   * AzureProvisionerTestUtils.configureAzureProvisioner(
   *       new AzureProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .assignGroupAttributeCount(8)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureAzureProvisioner(AzureProvisionerTestConfigInput azureProvisioningTestConfigInput) {

    if (5 != azureProvisioningTestConfigInput.getGroupAttributeCount() && 8 != azureProvisioningTestConfigInput.getGroupAttributeCount()) {
      throw new RuntimeException("Expecting 5, 8 for groupAttributeCount but was '" + azureProvisioningTestConfigInput.getGroupAttributeCount() + "'");
    }
    
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "azureExternalSystemConfigId", "myAzure");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "common.subjectLink.memberFromId2", "${subject.getAttributeValue('email')}");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "numberOfEntityAttributes", "5");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "numberOfGroupAttributes", "" + azureProvisioningTestConfigInput.getGroupAttributeCount() + "");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectAllMembershipsDuringDiagnostics", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.name", "accountEnabled");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${'true'}");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.1.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.matchingId", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.name", "displayName");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.searchAttribute", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.2.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.name", "mailNickname");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");

    if (azureProvisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    } else {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "id");
    }
    
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.3.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.name", "userPrincipalName");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${gcGrouperSyncMember.memberFromId2}");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetEntityAttribute.4.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.name", "displayName");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.name", "mailEnabled");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    if (azureProvisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${'true'}");
    } else {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${'false'}");
    }
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.name", "mailNickname");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    if (azureProvisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "name");
    } else {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "extension");
    }
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.3.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.insert", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.name", "securityEnabled");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.select", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "${'true'}");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.4.update", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(azureProvisioningTestConfigInput, "updateGroups", "true");

    if (azureProvisioningTestConfigInput.getGroupAttributeCount() == 8) {
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "allowOnlyMembersToPost", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "resourceProvisioningOptionsTeams", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "hideGroupInOutlook", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.insert", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.name", "allowOnlyMembersToPost");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.select", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpression", "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_allowOnlyMembersToPost'), 'false')}");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "translationScript");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.5.update", "false");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.insert", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.name", "welcomeEmailDisabled");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.select", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.translateExpression", "${'true'}");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.translateExpressionType", "translationScript");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.6.update", "false");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.insert", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.name", "resourceProvisioningOptionsTeams");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.select", "true");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.translateExpression", "${'true'}");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.translateExpressionType", "translationScript");
      configureProvisionerSuffix(azureProvisioningTestConfigInput, "targetGroupAttribute.7.update", "true");
    }
    
    for (String key: azureProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = azureProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + azureProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.class", EsbConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.provisionerConfigId", "myAzureProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.publisher.debug", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.azureProvTestCLC.quartzCron",  "0 0 5 * * 2000");
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
