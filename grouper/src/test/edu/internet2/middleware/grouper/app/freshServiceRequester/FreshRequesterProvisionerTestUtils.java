package edu.internet2.middleware.grouper.app.freshServiceRequester;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Test utils for FreshRequester provisioner
 */
public class FreshRequesterProvisionerTestUtils {

  public static void setupFreshRequesterExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/freshRequester").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.httpAuthnType").value("basicAuth").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.basicAuthUser").value("X").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.basicAuthPassword").value("somepass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.basicAuthStandardUserOrder").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.testUrlSuffix").value("/api/v2/requesters").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.freshServiceDev.testUrlResponseBodyRegex").value(".*requesters.*").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.exampleFreshRequester.mockExternalSystem.configId").value("freshServiceDev").store();
  }

  /**
   * @param provisioningTestConfigInput
   * @param suffix
   * @param value
   */
  public static void configureProvisionerSuffix(FreshRequesterProvisionerTestConfigInput provisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!provisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(FreshRequesterProvisionerTestConfigInput provisioningTestConfigInput) {
    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "freshserviceExternalSystemConfigId", "freshServiceDev");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", FreshRequesterProvisioner.class.getName());
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");

    // entity config
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "loadEntitiesToGrouperTable", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "true");

    // group config
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfGrouperDeleted", "true");

    // membership config
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");

    // group of users to provision (conditional)
    if (provisioningTestConfigInput.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(provisioningTestConfigInput, "entity2advanced", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupIdOfUsersToProvision", provisioningTestConfigInput.getGroupOfUsersToProvision().getUuid());
    }

    // logging and advanced
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

    // entity attributes: id, email
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute1name", "email");

    // entity attribute value caches: cache0=id, cache1=email
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "email");

    // group attributes: name, id, description
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute1name", "name");

    // group attribute value cache: cache2=id
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1groupAttribute", "name");
  }

  /**
   * @param provisioningTestConfigInput
   */
  public static void configureFreshRequesterProvisioner(FreshRequesterProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisioner(provisioningTestConfigInput);

    for (String key : provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    String changelogConsumerConfigId = "provisioner_incremental_" + provisioningTestConfigInput.getConfigId();

    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".quartzCron", "9 59 23 31 12 ? 2099");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".provisionerConfigId", provisioningTestConfigInput.getConfigId());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + changelogConsumerConfigId + ".publisher.debug", "true");

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
