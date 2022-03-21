package edu.internet2.middleware.grouper.app.google;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class GoogleProvisionerTestUtils {
  
  public static void main(String[] args) {
    String[] rsaKeypair = GrouperUtil.generateRsaKeypair(2048);
    System.out.println(rsaKeypair[0]);
    System.out.println(rsaKeypair[1]);
  }
  
  public static void setupGoogleExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.domain").value("viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountEmail").value("vivek-grouper@industrial-keep-335804.iam.gserviceaccount.com").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.tokenUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/token/").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.directoryApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.groupSettingsApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/settings").store();

    //String[] rsaKeypair = GrouperUtil.generateRsaKeypair(2048);
    String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuaGc9tsPiKesuG4u534VbiLXIm55oAsV5PX+EaXRQ0Ah+B3VN2K/lO3lL3Dp8KJWiAaN0ItSpfRsWMBcjZgJVSK4Ah3DAejIpuiEU6BU5puukX/j9OuHgBwZ9KycFUZwUL2i//8ChL+2hvgSha3TtGRBLMrGU/HhY/UEBb5UoMmtiTim95YzuoIs0Q85+Ti5tL/JljAU3zjkYfhoGYjQj7EqQyROSjxB52xYFmABWR2FfXSzMJdyVi6w6QWJKt0VtwOzboiJqSl+QypiK6pdn8jKAB5uErYF5Zbf50K38rSF2BzhAqwNEIVWhrx/jB9iu9cyXNx328bWQw2hpDZ6hwIDAQAB";  // rsaKeypair[0];
    String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC5oZz22w+Ip6y4bi7nfhVuItcibnmgCxXk9f4RpdFDQCH4HdU3Yr+U7eUvcOnwolaIBo3Qi1Kl9GxYwFyNmAlVIrgCHcMB6Mim6IRToFTmm66Rf+P064eAHBn0rJwVRnBQvaL//wKEv7aG+BKFrdO0ZEEsysZT8eFj9QQFvlSgya2JOKb3ljO6gizRDzn5OLm0v8mWMBTfOORh+GgZiNCPsSpDJE5KPEHnbFgWYAFZHYV9dLMwl3JWLrDpBYkq3RW3A7NuiImpKX5DKmIrql2fyMoAHm4StgXllt/nQrfytIXYHOECrA0QhVaGvH+MH2K71zJc3HfbxtZDDaGkNnqHAgMBAAECggEBAKf061GLmT17ANMKlpPLx9YT3fWAYbmF7jRwwoXzMykXAOU/EOkBBvjYWpKXJoQcThDbZTr4pDoVsmaG/fb7Rg5q0HTRutyiY9Jo9Tm5Crhwyf1J6tZyuPIX+wAfoUW6qurS+oWHlR7JW3w5PoEHa1J/l1zQx6uyYc2QJiiQMoAbDUPIEQBJGAmZ3E72Kf/V1goFavanPXsrKS3tEwP9jTldqCCV6mS4o+k9pO+FGODlEVxexj3OvjpWg9MhpKlSjPoiGW0nQHV2BsoIz8YlNyUwU8qCTPJLta7q82upMMXwLVKQSKWpafTN5THOY0ISMinO1mtL0KwNEHIJPikwb2ECgYEA9Fmmo5wAqW0+gCqs+eNLWkplEorW2NJjuv6t7HS27BkFEmkO2TFV5EPi0B6IXkH+S4+jG4FJ6MANuh0d+I0MIypC3Q4QLtXuzIAHU1TpOuEAvMr/fLh4sL46BVDW6lHwofBcnioxdtGG7yIKaxPWiQz4ozxWMLew37NnR6AlPBcCgYEAwntI7WT19vJddjSzLWeoZp8eKUzQgbUK/NZ4Q2uE1lRJLbNQkb1OEDskyqkKF0+MddMaXaMHbNZntLAe+2ZFAKNsPidgOjNtsExmvYYOcqJb5EAd07QPu+JkU1e+tJ0AMmGNDaT1+b6rH8aYbBl0Rsr975Y4a7VqChikTwfrixECgYEAhA+f/HTn9qnQSbzG2Bd8NkRW8/qNu4mZ1QqoPU+nPVsYXqbhG4mKfmAiSZD26tqH8Zaj9M2fgGesA5aRCDBTCv5gPNDI9kcxVN0tGGCf3O6WU3LzOhkJQZzOBul1/hZjE2Kw69qp+Sms37lqIA0Mue2Ew9RsUNA2i/CONSvcc+kCgYAjeHrfmWdnB+NV+NypLlu/g8vrenAZCB0d6jv7B/QtZygFpsvOGPnQ6giW0efeQor6vmrzoxVqm1xEz06HSarSJ/xJBcN+Of0Kh5TBgl7GN6iM48jM4O1xtiPYM4u7w1rS1Yn1cB3Q1B6/5+fK54WWl9ViykI2GtthRgdJxscGEQKBgHX1e4cB+lXq4B9txTD24b/Nvs8jXZJ9BpY9L06/V/iFOhll9v65OKySExVTA5jbrVNlNQcOSCrjrDSNdjfaVk1U+0jl+FR0o4E2HDpJ8yfv22zDQ0keDPqyBc1Lfa9kMqemIuSEuqCgawlsyQevdXo1/VaJtCQAaUtXtbTWEvR9"; // rsaKeypair[1];
    

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.publicKey").value(publicKey).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.configId").value("myGoogle").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountPrivateKeyPEM").value(privateKey).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceImpersonationUser").value("vivek@viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.enabled").value("true").store();

  }
  
  /**
   * 
   * @param googleProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(GoogleProvisionerTestConfigInput googleProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!googleProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + googleProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  /**
   * @param googleProvisioningTestConfigInput     
   * GoogleProvisionerTestUtils.configureGoogleProvisioner(
   *       new GoogleProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureGoogleProvisioner(GoogleProvisionerTestConfigInput googleProvisioningTestConfigInput) {

    configureProvisionerSuffix(googleProvisioningTestConfigInput, "allowWebPosting", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "common.subjectLink.memberFromId2", "${subject.getAttributeValue('email')}");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteEntitiesIfGrouperDeleted", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "false");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteGroupsIfGrouperDeleted", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "false");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "googleExternalSystemConfigId", "myGoogle");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "numberOfGroupAttributes", "6");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectAllMembershipsDuringDiagnostics", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.name", "givenName");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.1.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.name", "familyName");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.2.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.matchingId", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.name", "email");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpression", "${grouperProvisioningEntity.getId() + '@viveksachdeva.com'}");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "translationScript"); 
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetEntityAttribute.3.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.matchingId", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.1.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.name", "email");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperProvisioningGroup.getId() + '@viveksachdeva.com'}");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript"); 
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.3.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.name", "allowWebPosting");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "${'true'}");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.4.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.insert", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.name", "whoCanViewGroup");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.select", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpression", "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_whoCanViewGroup'), 'ALL_IN_DOMAIN_CAN_VIEW')}");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "translationScript");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "targetGroupAttribute.5.update", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "updateGroups", "true");
    configureProvisionerSuffix(googleProvisioningTestConfigInput, "whoCanViewGroup", "true");
    
    for (String key: googleProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = googleProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + googleProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.provisionerConfigId", "myGoogleProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.publisher.debug", "true");
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
