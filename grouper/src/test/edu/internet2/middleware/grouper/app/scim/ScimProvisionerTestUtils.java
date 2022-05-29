package edu.internet2.middleware.grouper.app.scim;

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
public class ScimProvisionerTestUtils {

  public static void setupAwsExternalSystem() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    //String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/awsScim/v2/").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value("abcdef").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword").value("abcdef").store();

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
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.acceptHeader")
        .value(provisioningTestConfigInput.getAcceptHeader()).store();

    }
    
    if (!StringUtils.isBlank(provisioningTestConfigInput.getBearerTokenExternalSystemConfigId())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "bearerTokenExternalSystemConfigId", 
           provisioningTestConfigInput.getBearerTokenExternalSystemConfigId());
    }

    for (String key : provisioningTestConfigInput.getSubjectLink().keySet()) {
      
      String value = provisioningTestConfigInput.getSubjectLink().get(key);
      configureProvisionerSuffix(provisioningTestConfigInput, "provisioner.githubProvisioner.common.subjectLink." + key, value);
    }

    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, provisioningTestConfigInput.getMembershipDeleteType(), "true");
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
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "5");
    
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

    if (!StringUtils.isBlank(provisioningTestConfigInput.getScimMembershipType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "scimMembershipType", provisioningTestConfigInput.getScimMembershipType());
    }
    if (!StringUtils.isBlank(provisioningTestConfigInput.getScimType())) {
      configureProvisionerSuffix(provisioningTestConfigInput, "scimType", provisioningTestConfigInput.getScimType());
    }
    if (provisioningTestConfigInput.isSelectAllEntities()) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", provisioningTestConfigInput.isSelectAllEntities() + "");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
    if (provisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
    }
    configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "false");
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
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "familyName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", provisioningTestConfigInput.getEntityAttribute4name());
    if (StringUtils.equals(provisioningTestConfigInput.getEntityAttribute4name(), "displayName")) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "name");
    } else if (StringUtils.equals(provisioningTestConfigInput.getEntityAttribute4name(), "emailValue")) {
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${gcGrouperSyncMember.entityAttributeValueCache0}");
    } else {
      throw new RuntimeException("Not value entityAttribute5Name: '" + provisioningTestConfigInput.getEntityAttribute4name() + "'");
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
    configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "false");

    
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
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerConfigId", "githubProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + provisioningTestConfigInput.getChangelogConsumerConfigId() + ".publisher.debug", "true");

    ConfigPropertiesCascadeBase.clearCache();
  
  }
  
}
