package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class DigitalMarketplaceProvisionerTestUtils {
  
  public static void setupDigitalMarketplaceExternalSystem() {
   
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyDigitalMarketplaceConnector.myDigitalMarketplace.url").value("http://localhost:8080/grouper/mockServices/digitalMarketplace/").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyDigitalMarketplaceConnector.myDigitalMarketplace.tokenUrl").value("http://localhost:8080/grouper/mockServices/digitalMarketplace/token/").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyDigitalMarketplaceConnector.myDigitalMarketplace.username").value("test").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyDigitalMarketplaceConnector.myDigitalMarketplace.password").value("test").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyDigitalMarketplaceConnector.myDigitalMarketplace.xRequestedByHeader").value("test@example.com").store();
  }
  
  private static void configureProvisionerSuffix(DigitalMarketplaceProvisionerTestConfigInput DigitalMarketplaceProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!DigitalMarketplaceProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + DigitalMarketplaceProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  public static void configureDigitalMarketplaceProvisioner(DigitalMarketplaceProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "digitalMarketplaceExternalSystemConfigId", "myDigitalMarketplace");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceProvisioner");
                                                                      
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "2");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "groupName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "longGroupName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "loginName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectIdentifier0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "userId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "groupName");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "loginName");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfMembershipAttributes", "2");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "groupName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperTargetGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperTargetGroupField", "groupName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "loginName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperTargetEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperTargetEntityField", "loginName");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "membership2AdvancedOptions", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "membershipMatchingIdExpression", "${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('groupName'), targetMembership.retrieveAttributeValueString('loginName'))}");
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
//    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
//    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "permissionGroupId");
//    
//    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
//    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
//    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
//    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "personId");
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }

}
