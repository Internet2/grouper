package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Test utilities for the AssetSonar provisioner: point an AssetSonar external system at the mock
 * service, and lay down an entity-only provisioner config plus the full-sync and incremental jobs.
 */
public class AssetSonarProvisionerTestUtils {

  /** external system config id used in tests */
  public static final String CONFIG_ID = "assetSonarTest";

  /** static token the mock validates */
  public static final String TEST_TOKEN = "testAssetSonarToken123";

  /** tier ids, deliberately with a non-small Agent id like the real tenant */
  public static final String STAFF_USER_ROLE_ID = "2";
  public static final String AGENT_ROLE_ID = "1734";
  public static final String ADMINISTRATOR_ROLE_ID = "1";

  /**
   * Point the test AssetSonar external system at the local mock service.
   */
  public static void setupAssetSonarExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    String prefix = AssetSonarExternalSystem.PROPERTY_PREFIX + CONFIG_ID + ".";
    storeLoader(prefix + "baseUrl", (ssl ? "https://" : "http://") + domainName + ":" + port + "/grouper/mockServices/assetSonar");
    storeLoader(prefix + "apiToken", TEST_TOKEN);
    storeLoader(prefix + "staffUserRoleId", STAFF_USER_ROLE_ID);
    storeLoader(prefix + "agentRoleId", AGENT_ROLE_ID);
    storeLoader(prefix + "administratorRoleId", ADMINISTRATOR_ROLE_ID);

    // tells the mock (in the Tomcat JVM) which external system's token to validate
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.exampleAssetSonar.mockExternalSystem.configId").value(CONFIG_ID).store();

    ConfigPropertiesCascadeBase.clearCache();
  }

  private static void storeLoader(String propertyName, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(propertyName).value(value).store();
  }

  private static void configureProvisionerSuffix(AssetSonarProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      storeLoader("provisioner." + input.getConfigId() + "." + suffix, value);
    }
  }

  /**
   * Configure the AssetSonar provisioner plus the full-sync and incremental job entries.
   * @param input the test config input
   */
  public static void configureAssetSonarProvisioner(AssetSonarProvisionerTestConfigInput input) {

    configureProvisionerSuffix(input, "class", AssetSonarProvisioner.class.getName());
    configureProvisionerSuffix(input, "assetSonarExternalSystemConfigId", CONFIG_ID);
    configureProvisionerSuffix(input, "debugLog", "true");
    configureProvisionerSuffix(input, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(input, "showAdvanced", "true");
    configureProvisionerSuffix(input, "subjectSourcesToProvision", "jdbc");

    // entity-only provisioning
    configureProvisionerSuffix(input, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "false");
    configureProvisionerSuffix(input, "operateOnGrouperMemberships", "false");
    configureProvisionerSuffix(input, "makeChangesToEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "true");
    configureProvisionerSuffix(input, "hasTargetEntityLink", "true");

    // delete = deactivate (status 0) in the DAO; out-of-scope members already inactive are no-ops
    configureProvisionerSuffix(input, "customizeEntityCrud", "true");
    configureProvisionerSuffix(input, "insertEntities", "true");
    configureProvisionerSuffix(input, "updateEntities", "true");
    configureProvisionerSuffix(input, "deleteEntities", "true");
    configureProvisionerSuffix(input, "deleteEntitiesIfNotExistInGrouper", "true");

    // match on email, cache the member id
    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "email");
    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");

    // role_id and status have no translation: AssetSonarProvisioningTranslator computes them
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "7");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.name", "first_name");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.name", "last_name");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.name", "employee_identification_number");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(input, "targetEntityAttribute.5.name", "role_id");
    configureProvisionerSuffix(input, "targetEntityAttribute.6.name", "status");

    configureProvisionerSuffix(input, "threadPoolSize", "1");
    configureProvisionerSuffix(input, "errorHandlingShow", "true");

    for (String key : input.getExtraConfig().keySet()) {
      String theValue = input.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        storeLoader("provisioner." + input.getConfigId() + "." + key, theValue);
      }
    }

    String configId = input.getConfigId();
    storeLoader("otherJob.provisioner_full_" + configId + ".class", GrouperProvisioningFullSyncJob.class.getName());
    storeLoader("otherJob.provisioner_full_" + configId + ".quartzCron", "9 59 23 31 12 ? 2099");
    storeLoader("otherJob.provisioner_full_" + configId + ".provisionerConfigId", configId);

    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".class", EsbConsumer.class.getName());
    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".quartzCron", "9 59 23 31 12 ? 2099");
    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".provisionerConfigId", configId);
    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".provisionerJobSyncType",
        GrouperProvisioningType.incrementalProvisionChangeLog.name());
    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".publisher.class", ProvisioningConsumer.class.getName());
    storeLoader("changeLog.consumer.provisioner_incremental_" + configId + ".publisher.debug", "true");

    ConfigPropertiesCascadeBase.clearCache();
  }

}
