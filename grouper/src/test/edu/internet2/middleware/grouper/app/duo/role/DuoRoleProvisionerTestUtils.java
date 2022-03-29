package edu.internet2.middleware.grouper.app.duo.role;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
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
  
  /**
   * @param provisioningTestConfigInput     
   * DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(
   *       new DuoRoleProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureDuoRoleProvisioner(DuoRoleProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "duoExternalSystemConfigId", "duo1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.insert", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "loginId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.valueType", "string");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.valueType", "string");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.valueType", "string");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.valueType", "string");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.valueType", "string");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    
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
