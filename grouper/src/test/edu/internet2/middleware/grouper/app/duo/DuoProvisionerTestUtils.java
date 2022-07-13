package edu.internet2.middleware.grouper.app.duo;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class DuoProvisionerTestUtils {
  
  public static void setupDuoExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1H").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.duo.mock.configId").value("duo1").store();

  }
  
  /**
   * 
   * @param duoProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(DuoProvisionerTestConfigInput duoProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!duoProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + duoProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param provisioningTestConfigInput     
   * DuoProvisionerTestUtils.configureDuoProvisioner(
   *       new DuoProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureDuoProvisioner(DuoProvisionerTestConfigInput provisioningTestConfigInput) {

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

    
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "loginId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "loginId");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "name");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    
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
