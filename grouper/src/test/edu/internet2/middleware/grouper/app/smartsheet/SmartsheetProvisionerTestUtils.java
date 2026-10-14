package edu.internet2.middleware.grouper.app.smartsheet;

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
 * Test utilities for the Smartsheet provisioner: configures a mock external system pointing at the
 * mock service, and builds the full provisioner config (group CRUD, user insert / update /
 * deactivate, membership add / remove) plus the full-sync and incremental job entries.
 */
public class SmartsheetProvisionerTestUtils {

  /** external system config id used in tests */
  public static final String CONFIG_ID = "smartsheetDev";

  /** static bearer token the mock validates */
  public static final String TEST_TOKEN = "testSmartsheetToken123";

  /**
   * Point the smartsheetDev WsBearerToken external system at the local mock service with a static
   * bearer token.
   */
  public static void setupSmartsheetExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/smartsheet").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".accessTokenPassword").value(TEST_TOKEN).store();

    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.exampleSmartsheet.mockExternalSystem.configId").value(CONFIG_ID).store();

    // make the just-written config visible to this JVM right away. The mock runs in the separate
    // Tomcat JVM and picks up this DB config on its own refresh (it clears its cache on a token
    // mismatch) -- bounce Tomcat if it has never seen the smartsheetDev keys before.
    ConfigPropertiesCascadeBase.clearCache();
  }

  public static void configureProvisionerSuffix(SmartsheetProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(SmartsheetProvisionerTestConfigInput input) {
    GrouperUtil.assertion(!StringUtils.isBlank(input.getConfigId()), "Config ID required");

    configureProvisionerSuffix(input, "class", SmartsheetProvisioner.class.getName());
    configureProvisionerSuffix(input, "smartsheetExternalSystemConfigId", CONFIG_ID);
    configureProvisionerSuffix(input, "debugLog", "true");

    // entities (users): insert + update; delete is opted into by tests that need it
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "makeChangesToEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "true");
    configureProvisionerSuffix(input, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(input, "customizeEntityCrud", "true");
    configureProvisionerSuffix(input, "insertEntities", "true");
    configureProvisionerSuffix(input, "updateEntities", "true");
    configureProvisionerSuffix(input, "deleteEntities", "false");
    configureProvisionerSuffix(input, "deleteEntitiesIfNotExistInGrouper", "false");

    // groups: full CRUD, only delete groups Grouper created
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(input, "customizeGroupCrud", "true");
    configureProvisionerSuffix(input, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(input, "insertGroups", "true");
    configureProvisionerSuffix(input, "updateGroups", "true");
    configureProvisionerSuffix(input, "deleteGroups", "true");
    configureProvisionerSuffix(input, "deleteGroupsIfGrouperCreated", "true");

    // memberships
    configureProvisionerSuffix(input, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(input, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(input, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(input, "insertMemberships", "true");
    configureProvisionerSuffix(input, "deleteMemberships", "true");
    configureProvisionerSuffix(input, "deleteMembershipsIfNotExistInGrouper", "true");

    if (input.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(input, "entity2advanced", "true");
      configureProvisionerSuffix(input, "groupIdOfUsersToProvision", input.getGroupOfUsersToProvision().getUuid());
    }

    configureProvisionerSuffix(input, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(input, "showAdvanced", "true");
    configureProvisionerSuffix(input, "subjectSourcesToProvision", "jdbc");

    // entity attributes: id (target-assigned user id) and email (the first-link match key). The id
    // is selected from the target and cached so it can form the membership matching id.
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");

    // match by native id first (stable), email is the backup for the first link
    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "2");
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "id");
    configureProvisionerSuffix(input, "entityMatchingAttribute1name", "email");

    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(input, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache1entityAttribute", "email");

    // group attributes: id (target-assigned), name (extension), description
    configureProvisionerSuffix(input, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");

    // match by native id first (so a rename is an update), name is the backup for the first link
    configureProvisionerSuffix(input, "groupMatchingAttributeCount", "2");
    configureProvisionerSuffix(input, "groupMatchingAttribute0name", "id");
    configureProvisionerSuffix(input, "groupMatchingAttribute1name", "name");

    configureProvisionerSuffix(input, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(input, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(input, "groupAttributeValueCache1has", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache1source", "target");
    configureProvisionerSuffix(input, "groupAttributeValueCache1type", "groupAttribute");
    configureProvisionerSuffix(input, "groupAttributeValueCache1groupAttribute", "name");
  }

  /**
   * Configure the full Smartsheet provisioner plus the full-sync and incremental job entries.
   * @param input the test config input
   */
  public static void configureSmartsheetProvisioner(SmartsheetProvisionerTestConfigInput input) {

    configureProvisioner(input);

    for (String key : input.getExtraConfig().keySet()) {
      String theValue = input.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("provisioner." + input.getConfigId() + "." + key).value(theValue).store();
      }
    }

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + input.getConfigId() + ".class")
        .value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + input.getConfigId() + ".quartzCron")
        .value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + input.getConfigId() + ".provisionerConfigId")
        .value(input.getConfigId()).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".class")
        .value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".quartzCron")
        .value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".provisionerConfigId")
        .value(input.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".provisionerJobSyncType")
        .value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".publisher.class")
        .value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + input.getConfigId() + ".publisher.debug")
        .value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

}
