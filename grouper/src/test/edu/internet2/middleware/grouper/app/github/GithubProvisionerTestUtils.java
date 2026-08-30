package edu.internet2.middleware.grouper.app.github;

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
 * Test helpers for the GitHub provisioner: configure the WsBearerToken external
 * system pointing at the mock servlet, and configure the provisioner (v1 is
 * membership-driven: teams pre-exist, accounts are matched via the SAML map,
 * memberships are added by team-add, and entity delete is a full org deprovision).
 */
public class GithubProvisionerTestUtils {

  /** external system config id used by direct GithubApiCommands calls */
  public static final String EXTERNAL_SYSTEM_CONFIG_ID = "githubDev";

  /** the bearer token the mock validates */
  public static final String MOCK_TOKEN = "testGithubToken123";

  /**
   * Point a WsBearerToken external system at the mock GitHub servlet.
   */
  public static void setupGithubExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + EXTERNAL_SYSTEM_CONFIG_ID + ".endpoint")
        .value((ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/github").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + EXTERNAL_SYSTEM_CONFIG_ID + ".httpAuthnType").value("bearerToken").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("grouper.wsBearerToken." + EXTERNAL_SYSTEM_CONFIG_ID + ".accessTokenPassword").value(MOCK_TOKEN).store();

    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.exampleGithub.mockExternalSystem.configId").value(EXTERNAL_SYSTEM_CONFIG_ID).store();
  }

  /**
   * Write one provisioner.&lt;configId&gt;.&lt;suffix&gt; property unless overridden in extraConfig.
   * @param input the test config input
   * @param suffix the config suffix
   * @param value the value
   */
  public static void configureProvisionerSuffix(GithubProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).value(value).store();
    }
  }

  /**
   * Write the default v1 (membership-driven) provisioner config.
   * @param input the test config input
   */
  private static void configureProvisioner(GithubProvisionerTestConfigInput input) {
    GrouperUtil.assertion(!StringUtils.isBlank(input.getConfigId()), "Config ID required");

    configureProvisionerSuffix(input, "startWith", "this is start with read only");
    configureProvisionerSuffix(input, "class", "edu.internet2.middleware.grouper.app.github.GithubProvisioner");
    configureProvisionerSuffix(input, "githubExternalSystemConfigId", EXTERNAL_SYSTEM_CONFIG_ID);
    configureProvisionerSuffix(input, "githubOrgs", "myorg");
    configureProvisionerSuffix(input, "githubEnterpriseSlug", "myenterprise");
    configureProvisionerSuffix(input, "debugLog", "true");

    // entities: select all + link (target login differs from subject); v1 supports
    // entity delete (full org deprovision) but not entity create (accounts are
    // invited via team-add, not created).
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "true");
    configureProvisionerSuffix(input, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(input, "makeChangesToEntities", "true");
    configureProvisionerSuffix(input, "customizeEntityCrud", "true");
    configureProvisionerSuffix(input, "deleteEntities", "true");
    configureProvisionerSuffix(input, "deleteEntitiesIfNotExistInGrouper", "true");

    // groups: teams pre-exist, so no group create/delete; match on slug via link
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(input, "selectAllGroups", "true");
    configureProvisionerSuffix(input, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(input, "customizeGroupCrud", "true");
    configureProvisionerSuffix(input, "insertGroups", "false");
    configureProvisionerSuffix(input, "deleteGroups", "false");

    // memberships
    configureProvisionerSuffix(input, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(input, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(input, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(input, "deleteMemberships", "true");
    configureProvisionerSuffix(input, "deleteMembershipsIfNotExistInGrouper", "true");

    if (input.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(input, "entity2advanced", "true");
      configureProvisionerSuffix(input, "groupIdOfUsersToProvision", input.getGroupOfUsersToProvision().getUuid());
    }

    configureProvisionerSuffix(input, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(input, "showAdvanced", "true");
    configureProvisionerSuffix(input, "subjectSourcesToProvision", "jdbc");

    // entity attributes: id (= the GitHub login, the target key used for
    // memberships; comes from the target via link) + samlNameId (match key,
    // translated from the subject id on the Grouper side). The id MUST be named
    // "id" so ProvisioningEntity.getId() (which memberships key on) resolves it.
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "samlNameId");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");

    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "samlNameId");

    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");

    // group attributes: id (= the team slug, the target key used for memberships;
    // from the group extension) + name + org (static). Named "id" so
    // ProvisioningGroup.getId() (which memberships key on) resolves it.
    configureProvisionerSuffix(input, "numberOfGroupAttributes", "3");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(input, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.name", "org");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateExpressionType", "staticValues");
    configureProvisionerSuffix(input, "targetGroupAttribute.2.translateExpression", "'myorg'");

    configureProvisionerSuffix(input, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "groupMatchingAttribute0name", "id");

    configureProvisionerSuffix(input, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(input, "groupAttributeValueCache0groupAttribute", "id");
  }

  /**
   * Configure the provisioner plus the full-sync and incremental daemon jobs.
   * @param input the test config input
   */
  public static void configureGithubProvisioner(GithubProvisionerTestConfigInput input) {

    configureProvisioner(input);

    for (String key : input.getExtraConfig().keySet()) {
      String theValue = input.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("provisioner." + input.getConfigId() + "." + key).value(theValue).store();
      }
    }

    String id = input.getConfigId();

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + id + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + id + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("otherJob.provisioner_full_" + id + ".provisionerConfigId").value(id).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".provisionerConfigId").value(id).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".provisionerJobSyncType")
        .value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("changeLog.consumer.provisioner_incremental_" + id + ".publisher.debug").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

}
