package edu.internet2.middleware.grouper.app.azure;

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
public class AzureProvisionerTestUtils {
  
  public static void setupAzureExternalSystem(boolean realAzure) {

    if (realAzure) {
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphEndpoint").value("https://graph.microsoft.com").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphVersion").value("beta").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupAttribute").value("displayName").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupValueFormat").value("${group.getName()}").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.loginEndpoint").value("https://login.microsoftonline.com/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resource").value("https://graph.microsoft.com").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("https://graph.microsoft.com/beta/").store();
      

    } else {
      int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
      boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
      String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientId").value("myClient").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientSecret").value("pass").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphEndpoint").value("https://graph.microsoft.com").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphVersion").value("v1.0").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupAttribute").value("displayName").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupValueFormat").value("${group.getName()}").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.loginEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/auth/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resource").value("https://graph.microsoft.com").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.tenantId").value("myTenant").store();
    }
  }
  
  /**
   * 
   * @param azureProvisioningTestConfigInput
   * @param suffix
   * @param value
   */
  private static void configureProvisionerSuffix(AzureProvisionerTestConfigInput azureProvisioningTestConfigInput, String suffix, String value) {
    // if its overridden then dont set
    if (!azureProvisioningTestConfigInput.getExtraConfig().containsKey(suffix)) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + azureProvisioningTestConfigInput.getConfigId() + "." + suffix).value(value).store();
    }
  }
  
  private static void configureMichiganAzure(AzureProvisionerTestConfigInput provisioningTestConfigInput) {
    
        
    configureProvisionerSuffix(provisioningTestConfigInput, "azureGroupType", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1entityAttribute", "userPrincipalName");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache1type", "entityAttribute");
    
    // add
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");

    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "userPrincipalName");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingMatchingValidationIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingRequiredValidationIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroups", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "userPrincipalName");
    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.subjectIdentifier0 + '@"+domain+"'}");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.subjectId ? (grouperProvisioningEntity.subjectId + '@" + domain + "') : null}");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.insert", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAdvancedAttribute", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAttributeCrud", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.update", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "mailNickname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpression", "${edu.internet2.middleware.grouper.util.GrouperUtil.stringFormatNameReverseReplaceTruncate(grouperProvisioningGroup.name, '_', 64).replaceAll('[^a-zA-Z0-9]', '_')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "description");
   
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

 

    
    
  } 
  
  private static void configureUncgAzure(AzureProvisionerTestConfigInput provisioningTestConfigInput) {
    
    configureProvisionerSuffix(provisioningTestConfigInput, "assignableToRole", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "azureGroupType", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "userPrincipalName");
    configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingShow", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "2");
    configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "4");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
    configureProvisionerSuffix(provisioningTestConfigInput, "resourceProvisioningOptionsTeam", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
    configureProvisionerSuffix(provisioningTestConfigInput, "startWith", "this is start with read only");
    configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "userPrincipalName");
    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.subjectIdentifier0 + '@"+domain+"'}");
    
    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.getSubjectId() + '@" + domain + "'}");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "translationScript");

    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "displayName");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpression", "${grouperProvisioningGroup.name.replaceAll('[^a-zA-Z0-9]', '_')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "translationScript");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "mailNickname");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperProvisioningGroup.name.replaceAll('[^a-zA-Z0-9]', '_')}");
    configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
    
    
    
    //configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.insert", "false");
    //configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAdvancedAttribute", "true");
    //configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAttributeCrud", "true");
    //configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.update", "false");
//    configureProvisionerSuffix(provisioningTestConfigInput, "threadPoolSize", "1");
   
    
  } 
  
  /**
   * @param provisioningTestConfigInput     
   * AzureProvisionerTestUtils.configureAzureProvisioner(
   *       new AzureProvisioningTestConfigInput()
   *    .assignConfigId(string)
   *    .assignGroupAttributeCount(8)
   *    .addExtraConfig("allowProvisionableRegexOverride", "true")
   *
   */
  public static void configureAzureProvisioner(AzureProvisionerTestConfigInput provisioningTestConfigInput) {

    AzureProvisionerTestUtils.setupAzureExternalSystem(provisioningTestConfigInput.isRealAzure());

    configureProvisionerSuffix(provisioningTestConfigInput, "azureExternalSystemConfigId", "myAzure");
    configureProvisionerSuffix(provisioningTestConfigInput, "class", "edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner");
    configureProvisionerSuffix(provisioningTestConfigInput, "logCommandsAlways", "false");

    configureProvisionerSuffix(provisioningTestConfigInput, "debugLog", "true");
    
    if (StringUtils.equals("uncgAzure", provisioningTestConfigInput.getProvisioningStrategy())) {
      configureUncgAzure(provisioningTestConfigInput);  
    } else if (StringUtils.equals("michiganAzure", provisioningTestConfigInput.getProvisioningStrategy())) {
      configureMichiganAzure(provisioningTestConfigInput);  
    } else {
      
      if (3 != provisioningTestConfigInput.getGroupAttributeCount() && 5 != provisioningTestConfigInput.getGroupAttributeCount()
          && 6 != provisioningTestConfigInput.getGroupAttributeCount()) {
        throw new RuntimeException("Expecting 3, 5, 6 for groupAttributeCount but was '" + provisioningTestConfigInput.getGroupAttributeCount() + "'");
      }
      
      if (provisioningTestConfigInput.getEntityAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntities", "false");
//        configureProvisionerSuffix(provisioningTestConfigInput, "deleteEntitiesIfNotExistInGrouper", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroups", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteGroupsIfNotExistInGrouper", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteMemberships", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "deleteMembershipsIfNotExistInGrouper", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "insertEntities", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "customizeEntityCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "makeChangesToEntities", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "customizeGroupCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "insertGroups", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "customizeMembershipCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "insertMemberships", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "selectEntities", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "selectGroups", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "selectMemberships", "true");
      }
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetEntityLink", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "hasTargetGroupLink", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "logAllObjectsVerbose", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfEntityAttributes", "" + provisioningTestConfigInput.getEntityAttributeCount());
      configureProvisionerSuffix(provisioningTestConfigInput, "numberOfGroupAttributes", "" + provisioningTestConfigInput.getGroupAttributeCount());
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperGroups", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "operateOnGrouperMemberships", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "provisioningType", "membershipObjects");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntities", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllEntitiesDuringDiagnostics", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllGroupsDuringDiagnostics", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "selectAllMembershipsDuringDiagnostics", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "showAdvanced", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "showProvisioningDiagnostics", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "subjectSourcesToProvision", "jdbc");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.name", "id");
      
      if (provisioningTestConfigInput.getEntityAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAdvancedAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.showAttributeCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.0.update", "false");
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCacheHas", "true");
      
      if (provisioningTestConfigInput.getEntityAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0has", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0source", "grouper");
        configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0type", "subjectTranslationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache0translationScript", "${subject.getAttributeValue('email')}");
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2type", "entityAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "entityAttributeValueCache2entityAttribute", "id");

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.name", "userPrincipalName");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
//    configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "name");
    
      configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttributeCount", "1");

      if (provisioningTestConfigInput.getEntityAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "displayName");
        
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "entityMatchingAttribute0name", "userPrincipalName");
        
      }

      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.name", "displayName");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.name", "mailNickname");

      if (provisioningTestConfigInput.isUdelUseCase()) {
        
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "translationScript");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpression", "${grouperUtil.prefixOrSuffix(grouperProvisioningEntity.name, '@', true) }");
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "id");
      }


      if (provisioningTestConfigInput.getEntityAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");

        
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.name", "accountEnabled");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpression", "${'true'}");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.4.translateExpressionType", "translationScript");
        
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      }
      
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.name", "id");
      
      if (!provisioningTestConfigInput.isUdelUseCase()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAdvancedAttribute", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.showAttributeCrud", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.0.update", "false");
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCacheHas", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2has", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2source", "target");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2type", "groupAttribute");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache2groupAttribute", "id");
      
      if (!provisioningTestConfigInput.isUdelUseCase()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0has", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0source", "target");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0type", "groupAttribute");
        configureProvisionerSuffix(provisioningTestConfigInput, "groupAttributeValueCache0groupAttribute", "displayName");
      }
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.name", "displayName");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", provisioningTestConfigInput.getDisplayNameMapping());
      
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttributeCount", "1");
      configureProvisionerSuffix(provisioningTestConfigInput, "groupMatchingAttribute0name", "displayName");

      
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.showAttributeCrud", "true");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.name", "mailNickname");
      configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      if (provisioningTestConfigInput.getGroupAttributeCount() == 5) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "extension");
      } else {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "extension");
      }
      if (!provisioningTestConfigInput.isUdelUseCase()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.select", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.insert", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.2.update", "true");
      }
      
      if (!provisioningTestConfigInput.isUdelUseCase()) {
        configureProvisionerSuffix(provisioningTestConfigInput, "updateEntities", "true");
        configureProvisionerSuffix(provisioningTestConfigInput, "updateGroups", "true");
    
        if (provisioningTestConfigInput.getGroupAttributeCount() >= 4) {
          configureProvisionerSuffix(provisioningTestConfigInput, "allowOnlyMembersToPost", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "resourceProvisioningOptionsTeam", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "hideGroupInOutlook", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.showAttributeCrud", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.insert", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.name", "allowOnlyMembersToPost");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.select", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpression", "${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_allowOnlyMembersToPost'), 'false')}");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.translateExpressionType", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.3.update", "false");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.showAttributeCrud", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.insert", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.name", "welcomeEmailDisabled");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.select", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpression", "${'true'}");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.translateExpressionType", "translationScript");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.4.update", "false");
        }
        
        if (provisioningTestConfigInput.getGroupAttributeCount() == 6) {
          
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.showAttributeCrud", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.insert", "true");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.name", "groupOwners");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.select", "false");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateFromStaticValues", "https://example.com/1,https://example.com/2,https://example.com/3");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.translateExpressionType", "staticValues");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetGroupAttribute.5.update", "false");
        }
        
        if (provisioningTestConfigInput.isRealAzure()) {
          String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
          configureProvisionerSuffix(provisioningTestConfigInput, "targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.subjectId ? (grouperProvisioningEntity.subjectId + '@" + domain + "') : null}");
        }
        
      }    
    }
    
    for (String key: provisioningTestConfigInput.getExtraConfig().keySet()) {
      String theValue = provisioningTestConfigInput.getExtraConfig().get(key);
      if (!StringUtils.isBlank(theValue)) {
        new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner." + provisioningTestConfigInput.getConfigId() + "." + key).value(theValue).store();
      }
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".class").value("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerConfigId").value(provisioningTestConfigInput.getConfigId()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".publisher.debug").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_" + provisioningTestConfigInput.getConfigId() + ".quartzCron").value("0 0 0 1 1 ? 2200").store();
  
    ConfigPropertiesCascadeBase.clearCache();
  
  }
}
