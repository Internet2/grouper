package edu.internet2.middleware.grouper.app.truefoundry;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Test utils for TrueFoundry provisioner
 */
public class TrueFoundryProvisionerTestUtils {

  public static void setupTrueFoundryExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.trueFoundryDev.endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/truefoundry").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.trueFoundryDev.httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.trueFoundryDev.accessTokenPassword")
        .value("{\"apiToken\": \"testApiToken123\", \"scimToken\": \"testScimToken456\"}").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.exampleTrueFoundry.mockExternalSystem.configId").value("trueFoundryDev").store();
  }

  /**
   * @param provisioningTestConfigInput
   * @param suffix
   * @param value
   */
  public static void configureProvisionerSuffix(TrueFoundryProvisionerTestConfigInput provisioningTestConfigInput, String suffix, String value) {
    if (!provisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(TrueFoundryProvisionerTestConfigInput provisioningTestConfigInput) {
    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "trueFoundryExternalSystemConfigId", "trueFoundryDev");
    configureProvisionerSuffix(provisioningTestConfigInput, "trueFoundryDefaultTeamMemberEmail", "svc-grouper-test@example.com");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");

    // entity config
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
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

    // entity attributes: id (=email), email, displayName
    // id = email because TrueFoundry is email-based; having an explicit "id" attribute is required
    // so the framework populates grouperTargetEntity.getId() for membership provisioningEntityId.
    // email is also used as the SCIM user identifier for display name updates.
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "id");

    // entity attribute value cache: cache0=id (=email)
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");

    // group attributes: id, name
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute1name", "name");

    // group attribute value cache: cache0=id, cache1=name
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
  public static void configureTrueFoundryProvisioner(TrueFoundryProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisioner(provisioningTestConfigInput);

    for (String key : provisioningTestConfigInput.getExtraConfig().keySet()) {
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

}
