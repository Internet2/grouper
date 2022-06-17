package edu.internet2.middleware.grouper.app.duo.role;

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
public class DuoRoleProvisionerTestUtils {
  
  /**
   * 
   * @param duoRoleProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(DuoRoleProvisionerTestConfigInput duoRoleProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!duoRoleProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + duoRoleProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  public static void setupDuoRoleExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duoRole").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1H").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.duo.mock.configId").value("duo1").store();

  }
  
  /**
   * @param provisioningTestConfigInput     
   * DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(
   *       new DuoRoleProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureDuoRoleProvisioner(DuoRoleProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "duoExternalSystemConfigId", "duo1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfGrouperDeleted", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");

    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "entityAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpression", "grouperProvisioningEntity.retrieveAttributeValueString('md_grouper_duoEmail')");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAttributeValidation", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.required", "true");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "email");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "email");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.showAttributeValidation", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.required", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "role");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.showAttributeValidation", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.required", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeName", "role");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMembershipAttributeValue", "groupAttributeValueCache0");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "role");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpression", "grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_duoRoles')");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAttributeValidation", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.required", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "role");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    
        
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.provisionerConfigId", "myDuoRoleProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.publisher.debug", "true");
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
