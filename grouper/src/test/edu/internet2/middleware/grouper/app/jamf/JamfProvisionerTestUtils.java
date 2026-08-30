package edu.internet2.middleware.grouper.app.jamf;

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
 * Test utilities for the Jamf provisioner: configures a mock external system pointing at the mock
 * service, and builds the full provisioner config (read-only roles, create-only accounts,
 * full-CRUD memberships) plus the full-sync and incremental job entries.
 */
public class JamfProvisionerTestUtils {

  /** external system config id used in tests */
  public static final String CONFIG_ID = "jamfDev";

  /** static bearer token the mock validates */
  public static final String TEST_TOKEN = "testJamfToken123";

  /**
   * Point the jamfDev WsBearerToken external system at the local mock service, using a static
   * bearer token (the OAuth client-credentials flow is a prod concern; tests use a fixed token).
   */
  public static void setupJamfExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/jamf").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".accessTokenPassword").value(TEST_TOKEN).store();

    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.exampleJamf.mockExternalSystem.configId").value(CONFIG_ID).store();

    // make the just-written config visible to the current JVM immediately (so API-level tests that
    // call the mock right after setup send the correct bearer token). The mock runs in the separate
    // Tomcat JVM; it picks up this DB config on its own refresh -- bounce Tomcat if it has never seen
    // the jamfDev / mock configId keys before.
    ConfigPropertiesCascadeBase.clearCache();
  }

  public static void configureProvisionerSuffix(JamfProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(JamfProvisionerTestConfigInput input) {
    GrouperUtil.assertion(!StringUtils.isBlank(input.getConfigId()), "Config ID required");

    configureProvisionerSuffix(input, "startWith", "this is start with read only");
    configureProvisionerSuffix(input, "class", JamfProvisioner.class.getName());
    configureProvisionerSuffix(input, "jamfExternalSystemConfigId", CONFIG_ID);
    configureProvisionerSuffix(input, "debugLog", "true");

    // entities (accounts): create-only -- insert yes, update/delete no
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "makeChangesToEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "true");
    configureProvisionerSuffix(input, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(input, "customizeEntityCrud", "true");
    configureProvisionerSuffix(input, "insertEntities", "true");
    configureProvisionerSuffix(input, "updateEntities", "true");
    configureProvisionerSuffix(input, "deleteEntities", "false");
    configureProvisionerSuffix(input, "deleteEntitiesIfNotExistInGrouper", "false");

    // groups (roles): read-only -- no insert/update/delete
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(input, "customizeGroupCrud", "true");
    configureProvisionerSuffix(input, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(input, "insertGroups", "false");
    configureProvisionerSuffix(input, "updateGroups", "false");
    configureProvisionerSuffix(input, "deleteGroups", "false");
    configureProvisionerSuffix(input, "deleteGroupsIfGrouperDeleted", "false");

    // memberships: full CRUD via replace
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

    // entity attributes: id (target-assigned native account id), name (=EPPN, match key), fullName, email.
    // The id is selected from the target (not translated) and cached so it is available to form the
    // membership matching id [groupTargetId, entityTargetId] -- exactly like the group's id below.
    // Matching is still by name.
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "name");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.name", "fullName");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.name", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "email");

    // match by native id first (stable across a rename); name (EPPN) is the backup for first linking
    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "2");
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "id");
    configureProvisionerSuffix(input, "entityMatchingAttribute1name", "name");

    // cache the target id (for membership matching) and name (for matching / incremental)
    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(input, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache1entityAttribute", "name");

    // group attributes: id (target-assigned), name (link key). Match by name.
    configureProvisionerSuffix(input, "numberOfGroupAttributes", "2");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");

    configureProvisionerSuffix(input, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "groupMatchingAttribute0name", "name");

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
   * Configure the full Jamf provisioner plus the full-sync and incremental job entries.
   * @param input the test config input
   */
  public static void configureJamfProvisioner(JamfProvisionerTestConfigInput input) {

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
