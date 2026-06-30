package edu.internet2.middleware.grouper.app.interfolio;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * Test helpers for the Interfolio provisioner: point the external system at the mock servlet and lay
 * down a default (entity-only) provisioner config.
 */
public class InterfolioProvisionerTestUtils {

  /** the external system config id used by the tests */
  public static final String EXTERNAL_SYSTEM_CONFIG_ID = "intfTest";

  /**
   * Configure the Interfolio external system to point at the local mock servlet.  Both the IAM and
   * the byc/core hosts point at the one mock (it dispatches by path).
   */
  public static void setupInterfolioExternalSystem() {

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    String mockUrl = "http://" + domainName + ":" + port + "/grouper/mockServices/interfolio/";

    store("grouper.interfolio." + EXTERNAL_SYSTEM_CONFIG_ID + ".publicKey", "fakePublicKey");
    store("grouper.interfolio." + EXTERNAL_SYSTEM_CONFIG_ID + ".privateKey", "fakePrivateKey");
    store("grouper.interfolio." + EXTERNAL_SYSTEM_CONFIG_ID + ".databaseId", "31697");
    store("grouper.interfolio." + EXTERNAL_SYSTEM_CONFIG_ID + ".bycUrl", mockUrl);
    store("grouper.interfolio." + EXTERNAL_SYSTEM_CONFIG_ID + ".iamUrl", mockUrl);

    ConfigPropertiesCascadeBase.clearCache();
  }

  private static void store(String propertyName, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(propertyName).value(value).store();
  }

  private static void configureProvisionerSuffix(InterfolioProvisionerTestConfigInput input, String suffix, String value) {
    if (!input.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + input.getConfigId() + "." + suffix).value(value).store();
    }
  }

  /**
   * Lay down a default entity-only Interfolio provisioner config plus the full-sync and incremental
   * daemon job entries.  This is a starting point; the target entity attribute translations may need
   * tuning per institution.
   * @param input the test config input
   */
  public static void configureInterfolioProvisioner(InterfolioProvisionerTestConfigInput input) {

    configureProvisionerSuffix(input, "class", InterfolioProvisioner.class.getName());
    configureProvisionerSuffix(input, "interfolioExternalSystemConfigId", EXTERNAL_SYSTEM_CONFIG_ID);
    // default to RPT + FS; tests override with addExtraConfig("enableFs", "false") for RPT only
    configureProvisionerSuffix(input, "enableFs", "true");
    configureProvisionerSuffix(input, "debugLog", "true");
    configureProvisionerSuffix(input, "logAllObjectsVerbose", "true");

    // entity-only provisioning (membershipObjects type, but no group/membership operations)
    configureProvisionerSuffix(input, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(input, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(input, "operateOnGrouperGroups", "false");
    configureProvisionerSuffix(input, "operateOnGrouperMemberships", "false");
    configureProvisionerSuffix(input, "makeChangesToEntities", "true");
    configureProvisionerSuffix(input, "selectAllEntities", "false");
    configureProvisionerSuffix(input, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(input, "showAdvanced", "true");

    // cache the target entity id (pid) keyed on the matching attribute
    configureProvisionerSuffix(input, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(input, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(input, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(input, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(input, "entityAttributeValueCacheHas", "true");

    // match on email (byc users/search returns email)
    configureProvisionerSuffix(input, "entityMatchingAttribute0name", "email");
    configureProvisionerSuffix(input, "entityMatchingAttributeCount", "1");

    // target entity attributes
    configureProvisionerSuffix(input, "numberOfEntityAttributes", "7");
    configureProvisionerSuffix(input, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.name", "institution_user_id");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.name", "saml_id");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.name", "user_type");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateExpressionType", "staticValues");
    configureProvisionerSuffix(input, "targetEntityAttribute.3.translateFromStaticValues", "internal");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.name", "first_name");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(input, "targetEntityAttribute.5.name", "last_name");
    configureProvisionerSuffix(input, "targetEntityAttribute.5.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.5.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(input, "targetEntityAttribute.6.name", "email");
    configureProvisionerSuffix(input, "targetEntityAttribute.6.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(input, "targetEntityAttribute.6.translateFromGrouperProvisioningEntityField", "email");

    configureProvisionerSuffix(input, "threadPoolSize", "1");
    configureProvisionerSuffix(input, "errorHandlingShow", "true");
    configureProvisionerSuffix(input, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");

    for (String key : input.getExtraConfig().keySet()) {
      String theValue = input.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
            .propertyName("provisioner." + input.getConfigId() + "." + key).value(theValue).store();
      }
    }

    // full sync + incremental daemon job entries
    String configId = input.getConfigId();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + configId + ".class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + configId + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + configId + ".provisionerConfigId").value(configId).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + configId + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + configId + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + configId + ".provisionerConfigId").value(configId).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + configId + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + configId + ".publisher.debug").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

}
