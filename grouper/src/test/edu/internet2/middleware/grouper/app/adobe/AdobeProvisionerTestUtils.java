package edu.internet2.middleware.grouper.app.adobe;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * @author 
 */
public class AdobeProvisionerTestUtils {

  public static void setupAdobeExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    //String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouper.adobe.provisioning.real", false)) {
      // is this supposed to be aws???
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").
        value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.aws.scim.provisioning.real.endpoint")).store();
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.aws.scim.provisioning.real.accessTokenPassword")).store();
      
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.tokenUrl").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/adobe/token/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/adobe/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.scopes").value("openid,AdobeID,user_management_sdk").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.httpAuthnType").value("oauthClientCredentials").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.grantType").value("client_credentials").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.clientSecret").value("clientSecret").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.clientId").value("clientId").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.apiKeyHeaderName").value("x-api-key").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.adobe.apiKeyPassword").value("secret").store();
    }
    
  }
  
  // TODO why is this here?  this is adobe
  public static void setupGithubExternalSystem(boolean includeOrgName) {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword").value("abc123").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.endpoint")
      .value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/githubScim/v2/organizations" + (includeOrgName ? "/orgName" : "")).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.accessTokenPassword").value("abc123").store();
    
  }
  
  /**
   * 
   * @param scimProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  public static void configureProvisionerSuffix(AdobeProvisionerTestConfigInput scimProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!scimProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + scimProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  private static void configureProvisioner(AdobeProvisionerTestConfigInput provisioningTestConfigInput) {
    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "orgId", "testOrgId");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "adobeExternalSystemConfigId", "adobe");
    
    if (StringUtils.isNotBlank(provisioningTestConfigInput.getSubjectLinkCache0())) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "subjectTranslationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0translationScript", provisioningTestConfigInput.getSubjectLinkCache0());
      
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.adobe.GrouperAdobeProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");

    configureProvisionerSuffix(provisioningTestConfigInput, "loadEntitiesToGrouperTable", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getGroupDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getGroupDeleteType(), "true");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getMembershipDeleteType(), "true");
    }
    if (provisioningTestConfigInput.getGroupAttributeCount() == 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "false");
    }
    
    if (provisioningTestConfigInput.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(provisioningTestConfigInput, "entity2advanced", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupIdOfUsersToProvision", provisioningTestConfigInput.getGroupOfUsersToProvision().getUuid());
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
    }

    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount() + "");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
      
    }
        

    if (provisioningTestConfigInput.isSelectAllEntities()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", provisioningTestConfigInput.isSelectAllEntities() + "");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "false");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

    //id, email, firstname, lastname, type, country
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "email");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "email");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "firstname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "lastname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    
    int totalEntityAttributesSoFar = 4; 
    
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "" + totalEntityAttributesSoFar);
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "name");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "id");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "name");

      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");
    }
  }
  
  /**
   * @param provisioningTestConfigInput     
   * ScimProvisionerTestUtils.configureScimProvisioner(
   *       new ScimProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureAdobeProvisioner(AdobeProvisionerTestConfigInput provisioningTestConfigInput) {

    configureProvisioner(provisioningTestConfigInput);
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".quartzCron",  "9 59 23 31 12 ? 2099");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerConfigId", provisioningTestConfigInput.getConfigId());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".publisher.debug", "true");

    
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
