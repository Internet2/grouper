package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * TODO refactor tests for 2.6.9 config
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogicTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String args[]) {
    TestRunner.run(new GrouperProvisioningLogicTest("testConfigurationMetadata"));
  }
  
  public GrouperProvisioningLogicTest() {
    super();
    // TODO Auto-generated constructor stub
  }

  public GrouperProvisioningLogicTest(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }

  public void testConfigurationProvisionerGroupMatchingAttribute() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");


    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), 
        errorsAndSuffixes.contains(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMatchingId"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.matchingId", "true");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMatchingId"))));

  }
  
  public void testConfigurationProvisionerEntityMatchingAttribute() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.entityAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfEntityAttributes", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");


    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMatchingId"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.matchingId", "true");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMatchingId"))));

  }

  public void testConfigurationProvisionerGroupMembershipAttribute() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");


    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2groupAttribute", "name");


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMembershipAttribute"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.membershipAttribute", "true");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMembershipAttribute"))));

  }
  
  public void testConfigurationProvisionerEntityMembershipAttribute() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.entityAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfEntityAttributes", "2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");


    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "member");
    
    // TODO fix this
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityMembershipAttributeName", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.entityMembershipAttributeValue", "groupAttributeValueCache0");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMembershipAttribute"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.membershipAttribute", "true");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMembershipAttribute"))));

  }
  
  public void testConfigurationMetadata() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfMetadata", "1");


    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.name", "abc");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataNotValidFormat")).assignJqueryHandle("#config_metadata.0.name_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.name", "md_abc");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataNotValidFormat")).assignJqueryHandle("#config_metadata.0.name_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.valueType", "INTEGER");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.defaultValue", "abc");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    String error = GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataDefaultValueNotCorrectType");
    error = GrouperUtil.replace(error, "$$defaultValue$$", GrouperUtil.xmlEscape("abc"));
    error = GrouperUtil.replace(error, "$$selectedType$$", "integer");
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(error).assignJqueryHandle("#config_metadata.0.defaultValue_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.defaultValue", "123");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    error = GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataDefaultValueNotCorrectType");
    error = GrouperUtil.replace(error, "$$defaultValue$$", GrouperUtil.xmlEscape("123"));
    error = GrouperUtil.replace(error, "$$selectedType$$", "integer");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(error).assignJqueryHandle("#config_metadata.0.defaultValue_spanid")));

  }

  public void testConfigurationProvisionerDoingSomething() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("", "");

    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.ldapProvTest.operateOnGrouperEntities");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.ldapProvTest.operateOnGrouperGroups");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"))));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.ldapProvTest.operateOnGrouperMemberships");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"))));


    
  }
// TODO
//  public void testTranslateGrouperToTarget() {
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", "groupMemberships");
//
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", "${grouperTargetGroup.setId(grouperProvisioningGroup.getName())}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", "${grouperTargetEntity.setId(grouperProvisioningEntity.retrieveAttributeValue(\"subjectId\"))}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "entity");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
//        "${grouperTargetMembership.assignAttributeValue('group_name', grouperProvisioningMembership.getProvisioningGroup().getName())}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.3.script", 
//        "${grouperTargetMembership.assignAttributeValue('subject_id', grouperProvisioningMembership.getProvisioningEntity().retrieveAttributeValueString('subjectId'))}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.3.for", "membership");
//
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
// 
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
//
//    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
//    
//    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
//    
//    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
//    
//    GrouperProvisioningTranslator translator = new GrouperProvisioningTranslator();
//    translator.setGrouperProvisioner(grouperProvisioner);
//    GrouperProvisioningDataGrouper grouperProvisioningDataGrouper = grouperProvisioner.retrieveGrouperProvisioningDataGrouper();
//    GrouperProvisioningDataGrouperTarget grouperProvisioningDataGrouperTarget = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget();
//    
//    GrouperProvisioningLists grouperProvisioningObjects = grouperProvisioningDataGrouper.getGrouperProvisioningObjects();
//    
//    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
//    ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
//    grouperProvisioningGroup.setId("testId");
//    grouperProvisioningGroup.setName("testName");
//    grouperProvisioningGroup.setDisplayName("testDisplayName");
//    grouperProvisioningGroup.setIdIndex(Long.parseLong("2313122331"));
//    grouperProvisioningGroup.assignAttributeValue("description", "testDescription");
//    grouperProvisioningGroups.add(grouperProvisioningGroup);
//    
//    grouperProvisioningObjects.setProvisioningGroups(grouperProvisioningGroups);
//    
//    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
//    
//    ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
//    grouperProvisioningEntity.assignAttributeValue("subjectId", "testSubjectId");
//    grouperProvisioningEntities.add(grouperProvisioningEntity);
//    
//    grouperProvisioningObjects.setProvisioningEntities(grouperProvisioningEntities);
//    
//    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
//    
//    ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
//    grouperProvisioningMembership.setProvisioningGroup(grouperProvisioningGroup);
//    grouperProvisioningMembership.setProvisioningEntity(grouperProvisioningEntity);
//    
//    grouperProvisioningMemberships.add(grouperProvisioningMembership);
//    grouperProvisioningObjects.setProvisioningMemberships(grouperProvisioningMemberships);
//    
//    grouperProvisioner.retrieveGrouperDao().processWrappers();
//    
//    translator.translateGrouperToTarget();
//    
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().size());
//    ProvisioningGroup targetGroup = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().get(0);
//    assertEquals("testName", targetGroup.getId());
//    
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().size());
//    ProvisioningEntity targetEntity = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().get(0);
//    assertEquals("testSubjectId", targetEntity.getId());
//    
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().size());
//    ProvisioningMembership targetMembership = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().get(0);
//    assertEquals("testName", targetMembership.getAttributes().get("group_name").getValue().toString());
//    assertEquals("testSubjectId", targetMembership.getAttributes().get("subject_id").getValue().toString());
//  }

// TODO
//  public void testMatchingIdGrouperObjects() {
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", "groupMemberships");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
//        "${targetGroup.retrieveAttributeValueString('groupName')}");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipMatchingIdExpression", 
//        "new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('groupName'), "
//        + "targetMembership.retrieveAttributeValueString('subjectId'))");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityMatchingIdExpression", 
//        "${targetEntity.retrieveAttributeValueString('subjectId')}");
//
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
// 
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
//
//    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
//    
//    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
//    
//    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
//    
//    GrouperProvisioningTranslator translator = new GrouperProvisioningTranslator();
//    translator.setGrouperProvisioner(grouperProvisioner);
//    GrouperProvisioningDataGrouper grouperProvisioningDataGrouper = grouperProvisioner.retrieveGrouperProvisioningDataGrouper();
//    GrouperProvisioningDataGrouperTarget grouperProvisioningDataGrouperTarget = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget();
//    
//    GrouperProvisioningLists grouperTargetObjects = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects();
//    
//    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
//    ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
//    grouperProvisioningGroup.setId("testId");
//    grouperProvisioningGroup.assignAttributeValue("groupName", "testName");
//    
//    grouperProvisioningGroups.add(grouperProvisioningGroup);
//    
//    grouperTargetObjects.setProvisioningGroups(grouperProvisioningGroups);
//    
//    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
//    
//    ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
//    grouperProvisioningEntity.assignAttributeValue("subjectId", "testSubjectId");
//    grouperProvisioningEntities.add(grouperProvisioningEntity);
//    
//    grouperTargetObjects.setProvisioningEntities(grouperProvisioningEntities);
//    
//    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
//    
//    ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
//    grouperProvisioningMembership.assignAttributeValue("groupName", "testName");
//    grouperProvisioningMembership.assignAttributeValue("subjectId", "testSubjectId");
//    
//    grouperProvisioningMemberships.add(grouperProvisioningMembership);
//    grouperTargetObjects.setProvisioningMemberships(grouperProvisioningMemberships);
//    
//    grouperProvisioner.retrieveGrouperDao().processWrappers();
//    
//    // when
//    translator.matchingIdGrouperObjects();
//    
//    // then
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().size());
//    ProvisioningGroup targetGroup = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().get(0);
//    assertEquals("testName", targetGroup.getMatchingId());
//    
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().size());
//    ProvisioningEntity targetEntity = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().get(0);
//    assertEquals("testSubjectId", targetEntity.getMatchingId());
//    
//    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().size());
//    ProvisioningMembership targetMembership = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().get(0);
//    assertEquals("testName", ((MultiKey)targetMembership.getMatchingId()).getKey(0));
//    assertEquals("testSubjectId", ((MultiKey)targetMembership.getMatchingId()).getKey(1));
//    
//  }

  public void testConfigurationValidationGroups() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperGroups", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.numberOfGroupAttributes", "2");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpression", "${'cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroups", "true");
    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType")).assignJqueryHandle("#config_deleteGroups_spanid")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType")).assignJqueryHandle("#config_deleteGroups_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfGrouperCreated", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType")).assignJqueryHandle("#config_deleteGroups_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.deleteGroupsIfGrouperCreated");
    
    // if there is entity link something else must be set
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetEntityLink", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetEntityLink_spanid")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.name", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0entityAttribute", "name");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetEntityLink_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.entityAttributeValueCacheHas");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.entityAttributeValueCache0has");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.entityAttributeValueCache0source");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.entityAttributeValueCache0type");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.entityAttributeValueCache0entityAttribute");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetEntityLink_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0type", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0translationScript", "${targetEntity.name}");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetEntityLink_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache0entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache1has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache1source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache1type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeValueCache1entityAttribute", "name");

    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleFromSameAttribute")).assignJqueryHandle("#config_entityAttributeValueCache0entityAttribute_spanid")));

    //TODO fix this
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.common.entityLink.entityAttributeValueCache0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetEntityLink");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetEntityLink_spanid")));
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket")).assignJqueryHandle("#config_entityAttributeValueCache0entityAttribute_spanid")));
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket")).assignJqueryHandle("#config_common.entityLink.entityAttributeValueCache0_spanid")));

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.numberOfGroupAttributes", "3");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "someAttr");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    //provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName")).assignJqueryHandle("#config_targetGroupAttribute.2.name_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "description");
    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    // provisioning.configuration.validation.attributeNameRequired = Error: ${type} ${fieldType} name is required
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName")).assignJqueryHandle("#config_targetGroupAttribute.2.name_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetGroupAttribute.2.name");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName")).assignJqueryHandle("#config_targetGroupAttribute.2.name_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();

  }
  
  public void testConfigurationValidationMemberships() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.membershipObjects.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMemberships", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");

    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType")).assignJqueryHandle("#config_deleteMemberships_spanid")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType")).assignJqueryHandle("#config_deleteMemberships_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfGrouperCreated", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType")).assignJqueryHandle("#config_deleteMemberships_spanid")));

  }

  public void testConfigurationValidationEntities() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.membershipObjects.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "membership_table");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntities", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
  
    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    
    List<ProvisioningValidationIssue> errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType")).assignJqueryHandle("#config_deleteEntities_spanid")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntitiesIfNotExistInGrouper", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType")).assignJqueryHandle("#config_deleteEntities_spanid")));
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntitiesIfGrouperCreated", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType")).assignJqueryHandle("#config_deleteEntities_spanid")));
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.deleteEntitiesIfGrouperCreated");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType")).assignJqueryHandle("#config_deleteEntities_spanid")));
    
    // if there is group link something else must be set
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetGroupLink", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.name", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0groupAttribute", "name");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.groupAttributeValueCacheHas");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.groupAttributeValueCache0has");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.groupAttributeValueCache0source");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.groupAttributeValueCache0type");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.groupAttributeValueCache0groupAttribute");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0type", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0groupAttribute", "${targetGroup.name}");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache0groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache1has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache1source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache1type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeValueCache1groupAttribute", "name");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleFromSameAttribute")).assignJqueryHandle("#config_groupAttributeValueCache0groupAttribute_spanid")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.common.groupLink.groupAttributeValueCache0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetGroupLink");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleFromSameAttribute")).assignJqueryHandle("#config_common.groupLink.groupAttributeValueCache0_spanid")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.abc123", "def456");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    GrouperTextContainer.assignThreadLocalVariable("extraneousConfig", "abc123");
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.extraneousConfigs"))));
    GrouperTextContainer.resetThreadLocalVariableMap();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.abc123");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetGroupLink", "true1");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validate();
    GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", "Has target group link");
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperConfigurationValidationInvalidBoolean")).assignJqueryHandle("#config_hasTargetGroupLink_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetGroupLink");
    
    
  }
  

}
