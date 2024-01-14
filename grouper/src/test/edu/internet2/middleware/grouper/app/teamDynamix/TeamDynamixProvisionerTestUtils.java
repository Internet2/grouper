package edu.internet2.middleware.grouper.app.teamDynamix;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class TeamDynamixProvisionerTestUtils {
  
  public static void setupTeamDynamixExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.teamDynamix.mock.configId").value("teamdx").store();

    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouper.tdx.provisioning.real", false)) {
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.beid")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.tdx.provisioning.real.beid")).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.url")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.tdx.provisioning.real.url")).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.webServicesKey")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.tdx.provisioning.real.webServicesKey")).store();
      
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.beid").value("fake value").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.url").value("http://localhost:8080/grouper/mockServices/teamdynamix/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.teamDynamix.teamdx.webServicesKey").value("fake value").store();
    }
    
  }
  
  private static void configureProvisionerSuffix(TeamDynamixProvisionerTestConfigInput provisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!provisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  public static void configureProvisioner(TeamDynamixProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "ExternalID");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "Name");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "8");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "FirstName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "LastName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "Company");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromStaticValues", "grouper_company_1");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "PrimaryEmail");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.name", "UserName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.6.name", "SecurityRoleID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.6.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.6.translateFromStaticValues", "573ef9e3-e01f-422b-bb1d-a5efbc8553a5");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.7.name", "ExternalID");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.7.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.7.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "Name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "Description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "teamDynamixExternalSystemConfigId", "teamdx");
    configureProvisionerSuffix(provisioningTestConfigInput, "threadPoolSize", "1");
    
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
