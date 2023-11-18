package edu.internet2.middleware.grouper.app.provisioningExamples.exampleFileReplace;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestConfigInput;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;

public class ExampleFileReplace {
  
  private static void configureProvisionerSuffix(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.testGeneric." + suffix).value(value).store();
  }
  
  public void configure() {
    configureProvisionerSuffix("addDisabledFullSyncDaemon", "true");
    configureProvisionerSuffix("addDisabledIncrementalSyncDaemon", "true");
    configureProvisionerSuffix("class", "edu.internet2.middleware.grouper.app.genericProvisioner.GrouperGenericProvisioner");
    configureProvisionerSuffix("customizeEntityCrud", "true");
    configureProvisionerSuffix("customizeMembershipCrud", "true");
    configureProvisionerSuffix("genericProvisionerDaoClassName", "edu.internet2.middleware.grouper.app.provisioningExamples.exampleFileWriter.ProvisioningExampleFileWriter");
    configureProvisionerSuffix("groupMatchingAttribute0name", "fileName");
    configureProvisionerSuffix("groupMatchingAttributeCount", "1");
    configureProvisionerSuffix("logAllObjectsVerbose", "true");
    configureProvisionerSuffix("logCommandsAlways", "true");
    configureProvisionerSuffix("membership2AdvancedOptions", "true");
    configureProvisionerSuffix("membershipMatchingIdExpression", "\u0024{new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('fileName'), targetMembership.retrieveAttributeValueString('email'))}");
    configureProvisionerSuffix("numberOfEntityAttributes", "2");
    configureProvisionerSuffix("numberOfGroupAttributes", "1");
    configureProvisionerSuffix("numberOfMembershipAttributes", "3");
    configureProvisionerSuffix("operateOnGrouperEntities", "true");
    configureProvisionerSuffix("operateOnGrouperGroups", "true");
    configureProvisionerSuffix("operateOnGrouperMemberships", "true");
    configureProvisionerSuffix("provisioningType", "membershipObjects");
    configureProvisionerSuffix("recalculateAllOperations", "true");
    configureProvisionerSuffix("replaceMemberships", "true");
    configureProvisionerSuffix("selectEntities", "false");
    configureProvisionerSuffix("selectMemberships", "false");
    configureProvisionerSuffix("showAdvanced", "true");
    configureProvisionerSuffix("startWith", "this is start with read only");
    configureProvisionerSuffix("subjectSourcesToProvision", "jdbc,personLdapSource");
    configureProvisionerSuffix("targetEntityAttribute.0.name", "name");
    configureProvisionerSuffix("targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "name");
    configureProvisionerSuffix("targetEntityAttribute.1.name", "email");
    configureProvisionerSuffix("targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix("targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "email");
    configureProvisionerSuffix("targetGroupAttribute.0.name", "fileName");
    configureProvisionerSuffix("targetGroupAttribute.0.translateExpression", "${grouperProvisioningGroup.getExtension() + '.txt'}");
    configureProvisionerSuffix("targetGroupAttribute.0.translateExpressionType", "translationScript");
    configureProvisionerSuffix("targetMembershipAttribute.0.name", "fileName");
    configureProvisionerSuffix("targetMembershipAttribute.0.translateExpressionType", "grouperTargetGroupField");
    configureProvisionerSuffix("targetMembershipAttribute.0.translateFromGrouperTargetGroupField", "fileName");
    configureProvisionerSuffix("targetMembershipAttribute.1.name", "name");
    configureProvisionerSuffix("targetMembershipAttribute.1.translateExpressionType", "grouperTargetEntityField");
    configureProvisionerSuffix("targetMembershipAttribute.1.translateFromGrouperTargetEntityField", "name");
    configureProvisionerSuffix("targetMembershipAttribute.2.name", "email");
    configureProvisionerSuffix("targetMembershipAttribute.2.translateExpressionType", "grouperTargetEntityField");
    configureProvisionerSuffix("targetMembershipAttribute.2.translateFromGrouperTargetEntityField", "email");

  }
}
