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
   * @param scimProvisioningTestConfigInput     
   * ScimProvisionerTestUtils.configureScimProvisioner(
   *       new ScimProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureScimProvisioner(ScimProvisionerTestConfigInput scimProvisioningTestConfigInput) {

    GrouperUtil.assertion(!StringUtils.isBlank(scimProvisioningTestConfigInput.getConfigId()), "Config ID required");

    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getAcceptHeader())) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.acceptHeader")
        .value(scimProvisioningTestConfigInput.getAcceptHeader()).store();

    }
    
    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getBearerTokenExternalSystemConfigId())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "bearerTokenExternalSystemConfigId", 
           scimProvisioningTestConfigInput.getBearerTokenExternalSystemConfigId());
    }

    for (String key : scimProvisioningTestConfigInput.getSubjectLink().keySet()) {
      
      String value = scimProvisioningTestConfigInput.getSubjectLink().get(key);
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "provisioner.githubProvisioner.common.subjectLink." + key, value);
    }

    configureProvisionerSuffix(scimProvisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "debugLog", "true");
    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, scimProvisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getEntityDeleteType())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "deleteEntities", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, scimProvisioningTestConfigInput.getEntityDeleteType(), "true");
    }
    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getMembershipDeleteType())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "deleteMemberships", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, scimProvisioningTestConfigInput.getMembershipDeleteType(), "true");
    }
    if (scimProvisioningTestConfigInput.getGroupOfUsersToProvision() != null) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "groupIdOfUsersToProvision", scimProvisioningTestConfigInput.getGroupOfUsersToProvision().getUuid());
    }
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "insertEntities", "true");
    
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "insertGroups", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "insertMemberships", "true");
    }

    configureProvisionerSuffix(scimProvisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "numberOfEntityAttributes", "5");
    
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "numberOfGroupAttributes", "" + scimProvisioningTestConfigInput.getGroupAttributeCount() + "");
    }
    
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "operateOnGrouperEntities", "true");
    
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      
    }
    
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "provisioningType", "membershipObjects");
    
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "replaceMemberships", "true");
    }

    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getScimMembershipType())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "scimMembershipType", scimProvisioningTestConfigInput.getScimMembershipType());
    }
    if (!StringUtils.isBlank(scimProvisioningTestConfigInput.getScimType())) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "scimType", scimProvisioningTestConfigInput.getScimType());
    }
    if (scimProvisioningTestConfigInput.isSelectAllEntities()) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "selectAllEntities", scimProvisioningTestConfigInput.isSelectAllEntities() + "");
    }
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "selectEntities", "true");
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "selectGroups", "true");
    }
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "selectMemberships", "false");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.0.select", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.insert", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.matchingId", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.name", "userName");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.searchAttribute", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.select", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.1.update", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.insert", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.name", "givenName");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.select", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.2.update", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.insert", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.name", "familyName");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.select", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.3.update", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.insert", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.select", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.update", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.name", scimProvisioningTestConfigInput.getEntityAttribute4name());
    if (StringUtils.equals(scimProvisioningTestConfigInput.getEntityAttribute4name(), "displayName")) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", "name");
    } else if (StringUtils.equals(scimProvisioningTestConfigInput.getEntityAttribute4name(), "emailValue")) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${gcGrouperSyncMember.memberFromId2}");
    } else {
      throw new RuntimeException("Not value entityAttribute5Name: '" + scimProvisioningTestConfigInput.getEntityAttribute4name() + "'");
    }
    if (scimProvisioningTestConfigInput.getGroupAttributeCount() > 0) {
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.insert", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.matchingId", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.name", "displayName");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.searchAttribute", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.select", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.1.insert", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.1.name", "id");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.1.select", "true");
      configureProvisionerSuffix(scimProvisioningTestConfigInput, "targetGroupAttribute.1.translateToGroupSyncField", "groupToId2");
    }
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "updateEntities", "true");
    configureProvisionerSuffix(scimProvisioningTestConfigInput, "updateGroups", "false");

    
    for (String key: scimProvisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = scimProvisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + scimProvisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }

    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerConfigId", "githubProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + scimProvisioningTestConfigInput.getChangelogConsumerConfigId() + ".publisher.debug", "true");

    ConfigPropertiesCascadeBase.clearCache();
  
  }
  
}
