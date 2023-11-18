package edu.internet2.middleware.grouper.app.scim;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
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
public class ScimProvisionerTestUtils {

  public static void setupAwsExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    //String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouper.aws.scim.provisioning.real", false)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").
        value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.aws.scim.provisioning.real.endpoint")).store();
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword")
        .value(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.aws.scim.provisioning.real.accessTokenPassword")).store();
      
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/awsScim/v2/").store();
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value("abcdef").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword").value("abcdef").store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.testUrlSuffix").value("/Users?count=0").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.testUrlResponseBodyRegex").value(".*totalResults.*").store();
    
  }
  
  public static void setupGithubExternalSystem() {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword").value("abc123").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/githubScim/v2/organizations/orgName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.accessTokenPassword").value("abc123").store();
    
  }
  
  /**
   * 
   * @param scimProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(ScimProvisionerTestConfigInput scimProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!scimProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + scimProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
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
  public static void configureScimProvisioner(ScimProvisionerTestConfigInput provisioningTestConfigInput) {

    GrouperUtil.assertion(!StringUtils.isBlank(provisioningTestConfigInput.getConfigId()), "Config ID required");

    if (!StringUtils.isBlank(provisioningTestConfigInput.getAcceptHeader())) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "acceptHeader", provisioningTestConfigInput.getAcceptHeader());

    }
    
    if (!StringUtils.isBlank(provisioningTestConfigInput.getBearerTokenExternalSystemConfigId())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "bearerTokenExternalSystemConfigId", 
           provisioningTestConfigInput.getBearerTokenExternalSystemConfigId());
    }

    if (provisioningTestConfigInput.isUseFirstLastName()) {
      if (StringUtils.isNotBlank(provisioningTestConfigInput.getSubjectLinkCache0())) {
        throw new RuntimeException("Cannot use first/last name and subject link cache 0");
      }
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "subjectTranslationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0translationScript", "${subject.getAttributeValue('firstname')}");

      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "subjectTranslationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1translationScript", "${subject.getAttributeValue('lastname')}");

    }
    
    if (StringUtils.isNotBlank(provisioningTestConfigInput.getSubjectLinkCache0())) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "subjectTranslationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0translationScript", provisioningTestConfigInput.getSubjectLinkCache0());
      
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
      
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    
    
    if (provisioningTestConfigInput.getScimType().equals("Github")) {
      //configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "false");
    } else {
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
    }
    
    
    if (provisioningTestConfigInput.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(provisioningTestConfigInput, "groupIdOfUsersToProvision", provisioningTestConfigInput.getGroupOfUsersToProvision().getUuid());
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
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
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", provisioningTestConfigInput.isUseEmails() ? "6" : "5");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount() + "");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "replaceMemberships", "true");
    }

    if (!StringUtils.isBlank(provisioningTestConfigInput.getScimType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "scimType", provisioningTestConfigInput.getScimType());
    }
    if (provisioningTestConfigInput.isSelectAllEntities()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", provisioningTestConfigInput.isSelectAllEntities() + "");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    
    if (provisioningTestConfigInput.getScimType().equals("Github")) {
      
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "false");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "userName");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "userName");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "givenName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    if (provisioningTestConfigInput.isUseFirstLastName()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    }
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "familyName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    if (provisioningTestConfigInput.isUseFirstLastName()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache1");
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    }

    int totalEntityAttributesSoFar = 3; 
    if (provisioningTestConfigInput.isUseEmails()) {
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "emailValue");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "email");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.name", "emailValue2");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.5.translateExpression", "${grouperProvisioningEntity.email + '2'}");
      
      totalEntityAttributesSoFar = 5;
    } else {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", provisioningTestConfigInput.getEntityAttribute4name());
      if (StringUtils.equals(provisioningTestConfigInput.getEntityAttribute4name(), "displayName")) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "name");
      } else if (StringUtils.equals(provisioningTestConfigInput.getEntityAttribute4name(), "emailValue")) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${gcGrouperSyncMember.getEntityAttributeValueCache0()}");
      } else {
        throw new RuntimeException("Not value entityAttribute4Name: '" + provisioningTestConfigInput.getEntityAttribute4name() + "'");
      }
      totalEntityAttributesSoFar = 4;
    }
    
    if (provisioningTestConfigInput.isUseActiveOnUser()) {
      int attributeIndex = totalEntityAttributesSoFar + 1;
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute."+attributeIndex+".name", "active");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute."+attributeIndex+".translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute."+attributeIndex+".translateExpression", "${provisioningEntityWrapper.isInGroup('test2:testGroup2')}");
      
    }
    
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "displayName");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "id");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "displayName");

      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");
    }
    
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
