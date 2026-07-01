package edu.internet2.middleware.grouper.app.dropbox;

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
 * Test utilities for the Dropbox provisioner: configures the mock external system
 * (pointing the WsBearerToken endpoint at the in-JVM mock servlet) and a full
 * default provisioner configuration plus the full-sync and incremental daemon jobs.
 */
public class DropboxProvisionerTestUtils {

  /**
   * Configure the WsBearerToken external system used by the Dropbox provisioner tests
   * so its endpoint points at the in-JVM Dropbox mock service.  Unlike TrueFoundry,
   * the Dropbox token is a single plain bearer string (not a JSON blob).
   */
  public static void setupDropboxExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.dropboxDev.endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/dropbox").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.dropboxDev.httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.dropboxDev.accessTokenPassword")
        .value("testDropboxToken123").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.exampleDropbox.mockExternalSystem.configId").value("dropboxDev").store();
  }

  /**
   * Override the Dropbox external system to use an OAuth2 refresh-token secret (a JSON object with
   * appKey / appSecret / refreshToken) instead of a static bearer token, so DropboxApiCommands
   * exercises the refresh-token exchange + access-token cache against the mock /oauth2/token endpoint.
   */
  public static void setupDropboxRefreshTokenExternalSystem() {
    setupDropboxExternalSystem();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.dropboxDev.accessTokenPassword")
        .value("{\"appKey\": \"app-key-123\", \"appSecret\": \"app-secret-456\", \"refreshToken\": \"refresh-789\"}").store();
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * Store one provisioner config property unless the test already supplied it via extraConfig.
   * @param provisioningTestConfigInput the test config input
   * @param suffix the config suffix
   * @param value the config value
   */
  public static void configureProvisionerSuffix(DropboxProvisionerTestConfigInput provisioningTestConfigInput, String suffix, String value) {
    if (!provisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }

  private static void configureProvisioner(DropboxProvisionerTestConfigInput provisioningTestConfigInput) {
    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "dropboxExternalSystemConfigId", "dropboxDev");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.dropbox.DropboxProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");

    // entity config
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    // note: do NOT set insertEntities directly -- it is a derived key (makeChangesToEntities=true
    // enables inserts); setting it explicitly fails validation with "should be refactored with an
    // upgrade task". TrueFoundry/Datadog likewise never set it.
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

    // entity attributes: id (target-assigned team_member_id), externalId (Grouper match key), email.
    // id has no translation - it is assigned by Dropbox on create and captured via the entity link.
    // Matching is on id + externalId (externalId sourced from the Grouper subjectId so it is stable).
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "externalId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "email");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute1name", "externalId");

    // entity attribute value caches: cache0=id, cache1=externalId
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "externalId");

    // group attributes: id (target-assigned group_id), name (from extension), externalId (from idIndex).
    // Matching is on id + externalId.
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "externalId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "idIndex");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute1name", "externalId");

    // group attribute value cache: cache0=id, cache1=externalId
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache1groupAttribute", "externalId");
  }

  /**
   * Configure the full default Dropbox provisioner plus full-sync and incremental daemon jobs.
   * @param provisioningTestConfigInput the test config input
   */
  public static void configureDropboxProvisioner(DropboxProvisionerTestConfigInput provisioningTestConfigInput) {

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
