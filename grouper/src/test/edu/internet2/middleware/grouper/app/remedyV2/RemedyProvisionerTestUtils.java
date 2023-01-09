package edu.internet2.middleware.grouper.app.remedyV2;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class RemedyProvisionerTestUtils {
  
  public static void setupRemedyExternalSystem() {
   
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.myRemedy.url").value("http://localhost:8080/grouper/mockServices/remedy/").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.myRemedy.tokenUrl").value("http://localhost:8080/grouper/mockServices/remedy/token/").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.myRemedy.username").value("test").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.myRemedy.password").value("test").store();
  }
  
  private static void configureProvisionerSuffix(RemedyProvisionerTestConfigInput remedyProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!remedyProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + remedyProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  public static void configureRemedyProvisioner(RemedyProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "remedyExternalSystemConfigId", "myRemedy");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "2");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "permissionGroup");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "permissionGroupId");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "personId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "remedyLoginId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectIdentifier");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "remedyLoginId");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "permissionGroup");

    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfMembershipAttributes", "4");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "permissionGroup");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "permissionGroupId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.name", "personId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.3.name", "remedyLoginId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.3.translateFromGrouperProvisioningEntityField", "subjectIdentifier");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "membership2AdvancedOptions", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "membershipMatchingIdExpression", "${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('permissionGroupId'), targetMembership.retrieveAttributeValueString('remedyLoginId'))}");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "permissionGroupId");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "personId");
    
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
