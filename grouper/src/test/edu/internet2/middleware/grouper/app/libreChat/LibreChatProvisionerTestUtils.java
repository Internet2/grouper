package edu.internet2.middleware.grouper.app.libreChat;

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
 * Test utilities for the LibreChat provisioner: points a web service external system at the mock
 * service, and builds the provisioner config (managed groups, read-only users matched on username,
 * memberships added and removed one at a time) plus the full sync and incremental jobs.
 *
 * GRP-7594
 */
public class LibreChatProvisionerTestUtils {

  /** external system config id used in tests */
  public static final String CONFIG_ID = "libreChatDev";

  /** static bearer token the mock checks */
  public static final String TEST_TOKEN = "testLibreChatToken123";

  /**
   * Point the libreChatDev web service external system at the local mock service with a static bearer
   * token.  In production this is usually httpAuthnType=dynamic (an SSO login template).
   */
  public static void setupLibreChatExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/libreChat").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + CONFIG_ID + ".accessTokenPassword").value(TEST_TOKEN).store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.libreChat.mockExternalSystem.configId").value(CONFIG_ID).store();

    // make the config visible to this JVM now; the mock (in the test Tomcat) re-reads on a token mismatch
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * Store one provisioner setting unless the test overrides it
   * @param input config input
   * @param suffix setting after provisioner.&lt;id&gt;.
   * @param value value
   */
  public static void configureProvisionerSuffix(LibreChatProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).value(value).store();
    }
  }

  /**
   * @param input config input
   */
  private static void configureProvisioner(LibreChatProvisionerTestConfigInput input) {

    GrouperUtil.assertion(!StringUtils.isBlank(input.getConfigId()), "Config ID required");

    configureProvisionerSuffix(input, "class", LibreChatProvisioner.class.getName());
    configureProvisionerSuffix(input, "libreChatExternalSystemConfigId", CONFIG_ID);
    configureProvisionerSuffix(input, "debugLog", "true");
    configureProvisionerSuffix(input, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(input, "showAdvanced", "true");
    configureProvisionerSuffix(input, "subjectSourcesToProvision", "jdbc");

    // a member who has not logged in to LibreChat yet has no user, and Grouper cannot create one.  That is
    // normal, not a misconfiguration, so do not fail the job (the membership is added on a later sync)
    configureProvisionerSuffix(input, "errorHandlingShow", "true");
    configureProvisionerSuffix(input, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");

    // users: read only, matched on username = the subject's email in the test data (eppn at Penn).
    // makeChangesToEntities is left off, so only selectEntities may be set (the validation rejects
    // insertEntities / updateEntities / deleteEntities, even set to false)
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "true");
    configureProvisionerSuffix(input, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(input, "customizeEntityCrud", "true");
    configureProvisionerSuffix(input, "selectEntities", "true");
    // remove these if an earlier run stored them
    for (String suffix : new String[] {"insertEntities", "updateEntities", "deleteEntities"}) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).delete();
    }
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "username");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "username");
    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(input, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache1type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache1entityAttribute", "username");

    // groups: created, renamed and deleted by Grouper; matched on idOnTheSource = Grouper group id
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(input, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(input, "customizeGroupCrud", "true");
    configureProvisionerSuffix(input, "selectGroups", "true");
    configureProvisionerSuffix(input, "insertGroups", "true");
    configureProvisionerSuffix(input, "updateGroups", "true");
    configureProvisionerSuffix(input, "deleteGroups", "true");
    configureProvisionerSuffix(input, "deleteGroupsIfGrouperDeleted", "true");
    configureProvisionerSuffix(input, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "displayExtension");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.name", "idOnTheSource");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "id");
    configureProvisionerSuffix(input, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "groupMatchingAttribute0name", "idOnTheSource");
    configureProvisionerSuffix(input, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(input, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(input, "groupAttributeValueCache1has", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache1source", "grouper");
    configureProvisionerSuffix(input, "groupAttributeValueCache1type", "groupAttribute");
    configureProvisionerSuffix(input, "groupAttributeValueCache1groupAttribute", "idOnTheSource");

    // memberships: add and remove
    configureProvisionerSuffix(input, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(input, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(input, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(input, "selectMemberships", "true");
    configureProvisionerSuffix(input, "insertMemberships", "true");
    configureProvisionerSuffix(input, "deleteMemberships", "true");
    configureProvisionerSuffix(input, "deleteMembershipsIfNotExistInGrouper", "true");
  }

  /**
   * Configure the provisioner plus the full sync and incremental job entries
   * @param input config input
   */
  public static void configureLibreChatProvisioner(LibreChatProvisionerTestConfigInput input) {
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
