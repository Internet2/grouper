package edu.internet2.middleware.grouper.app.google;


import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
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

    
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouper.google.provisioning.real", false)) {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.domain").
        value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.google.provisioning.real.domain")).store();
      
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountEmail")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.google.provisioning.real.serviceAccountEmail")).store();
      
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountPrivateKeyPEM")
      .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.google.provisioning.real.serviceAccountPrivateKeyPEM")).store();

      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceImpersonationUser")
      .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.google.provisioning.real.serviceImpersonationUser")).store();
      
    } else {
      
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
    }
    
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
  
  // not ready for testing. it doesn't work.
  public static void configureGoogleProvisionerDavidJardim(GoogleProvisionerTestConfigInput provisioningTestConfigInput) {
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "defaultMessageDenyNotificationText", "You are not allowed to send email to this group.");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeSameAsSearchAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "googleExternalSystemConfigId", "google");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeSameAsSearchAttribute", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeName", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMembershipAttributeValue", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchAttribute0name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupSearchAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerboseToDaemonDbLog", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerboseToLogFile", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "messageModerationLevel", "MODERATE_ALL_MESSAGES");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "12");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "groupAttributes");
    configureProvisionerSuffix(provisioningTestConfigInput, "readOnly", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "replyTo", "REPLY_TO_IGNORE");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "sendMessageDenyNotification", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "spamModerationLevel", "REJECT");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "wesad");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectIdentifier2");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "\u0024{\"grouper_\"+ grouperProvisioningGroup.name.split(\"\u003A\").2 + \"_\" + grouperProvisioningGroup.extension + \"_\" + grouperProvisioningGroup.idIndex}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.10.name", "whoCanPostMessage");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.10.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.10.translateFromStaticValues", "NONE_CAN_POST");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.11.name", "allowWebPosting");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.11.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.11.translateFromStaticValues", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "\u0024{\"grouper_\"+ grouperProvisioningGroup.name.split(\"\u003A\").2 + \"_\" + grouperProvisioningGroup.extension + \"_\" + grouperProvisioningGroup.idIndex + \"@wesleyan.edu\"}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "whoCanAdd");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateFromStaticValues", "NONE_CAN_ADD");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "whoCanJoin");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromStaticValues", "INVITED_CAN_JOIN");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.name", "whoCanViewMembership");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.6.translateFromStaticValues", "ALL_MANAGERS_CAN_VIEW");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.name", "whoCanViewGroup");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.7.translateFromStaticValues", "ALL_OWNERS_CAN_VIEW");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.8.name", "whoCanInvite");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.8.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.8.translateFromStaticValues", "ALL_OWNERS_CAN_INVITE");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.9.name", "allowExternalMembers");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.9.translateExpressionType", "staticValues");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.9.translateFromStaticValues", "false");
  }
  
  /**
   * @param provisioningTestConfigInput     
   * GoogleProvisionerTestUtils.configureGoogleProvisioner(
   *       new GoogleProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureGoogleProvisioner(GoogleProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisionerSuffix(provisioningTestConfigInput, "allowWebPosting", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    if (GrouperUtil.booleanValue(provisioningTestConfigInput.getExtraConfig().get("deleteEntities"), true)) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
    }
    
    if (GrouperUtil.booleanValue(provisioningTestConfigInput.getExtraConfig().get("deleteEntitiesIfGrouperDeleted"), true)) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfGrouperDeleted", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "false");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfGrouperDeleted", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "googleExternalSystemConfigId", "myGoogle");

    configureProvisionerSuffix(provisioningTestConfigInput, "handleDeletedGroup", "archive");
    configureProvisionerSuffix(provisioningTestConfigInput, "defaultMessageDenyNotificationText", "test deny message");
    configureProvisionerSuffix(provisioningTestConfigInput, "messageModerationLevel", "MODERATE_NONE");
    configureProvisionerSuffix(provisioningTestConfigInput, "replyTo", "REPLY_TO_IGNORE");
    configureProvisionerSuffix(provisioningTestConfigInput, "sendMessageDenyNotification", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "spamModerationLevel", "ALLOW");
    
    
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    
    if (GrouperUtil.booleanValue(provisioningTestConfigInput.getExtraConfig().get("insertEntities"), true)) {
      configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "6");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllMembershipsDuringDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.update", "false");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "subjectTranslationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0translationScript", "${subject.getAttributeValue('email')}");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "givenName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "familyName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpression", "${grouperProvisioningEntity.getId() + '@viveksachdeva.com'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "translationScript"); 
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "orgUnitPath");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "name");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "email");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "name");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "email");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperProvisioningGroup.getId() + '@viveksachdeva.com'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript"); 
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "allowWebPosting");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "${'true'}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "whoCanViewGroup");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpression", "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_whoCanViewGroup'), 'ALL_IN_DOMAIN_CAN_VIEW')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "translationScript");
    if (GrouperUtil.booleanValue(provisioningTestConfigInput.getExtraConfig().get("updateEntities"), true)) {
      configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "whoCanViewGroup", "true");
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      
      if (StringUtils.equalsAny(key, "updateEntities", "insertEntities", "deleteEntities", "deleteEntitiesIfGrouperDeleted")) {
        continue;
      }
      
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
