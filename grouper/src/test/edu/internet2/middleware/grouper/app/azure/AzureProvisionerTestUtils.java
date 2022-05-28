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
   * @param provisioningTestConfigInput     
   * AzureProvisionerTestUtils.configureAzureProvisioner(
   *       new AzureProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .assignGroupAttributeCount(8)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureAzureProvisioner(AzureProvisionerTestConfigInput provisioningTestConfigInput) {

    if (5 != provisioningTestConfigInput.getGroupAttributeCount() && 8 != provisioningTestConfigInput.getGroupAttributeCount()) {
      throw new RuntimeException("Expecting 5, 8 for groupAttributeCount but was '" + provisioningTestConfigInput.getGroupAttributeCount() + "'");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "azureExternalSystemConfigId", "myAzure");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "common.subjectLink.entityAttributeValueCache0", "${subject.getAttributeValue('email')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "5");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount() + "");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllMembershipsDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "accountEnabled");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${'true'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.matchingId", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.searchAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "mailNickname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");

    if (provisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "id");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "userPrincipalName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${gcGrouperSyncMember.entityAttributeValueCache0}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "mailEnabled");
    if (provisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${'true'}");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${'false'}");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.insert", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "mailNickname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    if (provisioningTestConfigInput.getGroupAttributeCount() == 5) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "name");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "extension");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.insert", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "securityEnabled");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "${'true'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");

    if (provisioningTestConfigInput.getGroupAttributeCount() == 8) {
      configureProvisionerSuffix(provisioningTestConfigInput, "allowOnlyMembersToPost", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "resourceProvisioningOptionsTeams", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "hideGroupInOutlook", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.insert", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "allowOnlyMembersToPost");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.select", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpression", "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_allowOnlyMembersToPost'), 'false')}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.update", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.insert", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.name", "welcomeEmailDisabled");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.select", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.translateExpression", "${'true'}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.update", "false");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.insert", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.name", "resourceProvisioningOptionsTeams");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.select", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.translateExpression", "${'true'}");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.update", "true");
    }
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
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
