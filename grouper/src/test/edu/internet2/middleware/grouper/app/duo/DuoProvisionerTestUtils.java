package edu.internet2.middleware.grouper.app.duo;

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
   * @param duoProvisioningTestConfigInput     
   * DuoProvisionerTestUtils.configureDuoProvisioner(
   *       new DuoProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureDuoProvisioner(DuoProvisionerTestConfigInput duoProvisioningTestConfigInput) {

    configureProvisionerSuffix(duoProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "duoExternalSystemConfigId", "duo1");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.insert", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.matchingId", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.name", "loginId");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.searchAttribute", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.update", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.0.valueType", "string");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.1.name", "id");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.1.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetEntityAttribute.1.valueType", "string");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.0.valueType", "string");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.update", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.1.valueType", "string");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "targetGroupAttribute.2.valueType", "string");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(duoProvisioningTestConfigInput, "updateGroups", "true");
    
    for (String key: duoProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = duoProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + duoProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.provisionerConfigId").value("myDuoProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.duoProvTestCLC.publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
