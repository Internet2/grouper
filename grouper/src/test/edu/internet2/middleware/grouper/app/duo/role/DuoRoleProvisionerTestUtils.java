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
   * @param duoRoleProvisioningTestConfigInput     
   * DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(
   *       new DuoRoleProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureDuoRoleProvisioner(DuoRoleProvisionerTestConfigInput duoRoleProvisioningTestConfigInput) {

    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "duoExternalSystemConfigId", "duo1");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.insert", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.name", "loginId");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.update", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.0.valueType", "string");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.1.name", "id");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.1.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetEntityAttribute.1.valueType", "string");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.0.valueType", "string");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.update", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.1.valueType", "string");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "targetGroupAttribute.2.valueType", "string");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(duoRoleProvisioningTestConfigInput, "updateGroups", "true");
    
    for (String key: duoRoleProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = duoRoleProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + duoRoleProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
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
