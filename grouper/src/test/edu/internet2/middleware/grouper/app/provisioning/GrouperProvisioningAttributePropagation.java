package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Note that for these tests, we don't care about updates to the target (ldap).  We're only looking at the 
 * provisioning attribute propagation.
 * 
 * @author shilen
 */
public class GrouperProvisioningAttributePropagation extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningAttributePropagation("testIncrementalStemNotProvisionable"));    
  }
  
  public GrouperProvisioningAttributePropagation() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperProvisioningAttributePropagation(String name) {
    super(name);
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "does not matter");
  }
  
  public void testIncrementalRegexRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisionableRegex", "groupExtension not matches ^.*_includes$|^.*_excludes$");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.allowProvisionableRegexOverride", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");

    // init stuff
    runIncrementalJobs(true, true);
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test2StemAttributeValue = new GrouperProvisioningAttributeValue();
    test2StemAttributeValue.setDirectAssignment(true);
    test2StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_allowProvisionableRegexOverride", "groupExtension not matches ^.*_includesxx$|^.*_excludesxx$");
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup_includes").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup_includes").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
    
    runIncrementalJobs(true, true);
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    testGroup2.setExtension("testGroup");
    testGroup2.store();
    
    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    // rename again
    testGroup.setExtension("testGroup_excludes");
    testGroup.store();
    testGroup2.setExtension("testGroup_excludesxx");
    testGroup2.store();

    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(0, testGroup2Assigns.size());
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  public void testFullRegexRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisionableRegex", "groupExtension not matches ^.*_includes$|^.*_excludes$");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.allowProvisionableRegexOverride", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test2StemAttributeValue = new GrouperProvisioningAttributeValue();
    test2StemAttributeValue.setDirectAssignment(true);
    test2StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_allowProvisionableRegexOverride", "groupExtension not matches ^.*_includesxx$|^.*_excludesxx$");
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup_includes").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup_includes").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
    
    runFullJob();
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    testGroup2.setExtension("testGroup");
    testGroup2.store();
    
    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    // rename again
    testGroup.setExtension("testGroup_excludes");
    testGroup.store();
    testGroup2.setExtension("testGroup_excludesxx");
    testGroup2.store();

    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(0, testGroup2Assigns.size());
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  public void testFullPolicyRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.onlyProvisionPolicyGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.allowPolicyGroupOverride", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test2StemAttributeValue = new GrouperProvisioningAttributeValue();
    test2StemAttributeValue.setDirectAssignment(true);
    test2StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_allowPolicyGroupOverride", "false");
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runFullJob();
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    addGroupType(testGroup, "policy");
    
    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    removeGroupTypes(testGroup);

    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    addGroupType(testGroup, "ref");
    
    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  
  public void testIncrementalPolicyRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.onlyProvisionPolicyGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.allowPolicyGroupOverride", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");

    // init stuff
    runIncrementalJobs(true, true);
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test2StemAttributeValue = new GrouperProvisioningAttributeValue();
    test2StemAttributeValue.setDirectAssignment(true);
    test2StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test2StemAttributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_allowPolicyGroupOverride", "false");
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runIncrementalJobs(true, true);
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    addGroupType(testGroup, "policy");
    
    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    removeGroupTypes(testGroup);

    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    addGroupType(testGroup, "ref");
    
    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(5, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test2Stem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    assertNotNull(testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningMetadataJson"));
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  public void testIncrementalStemScopeOne() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");

    // init stuff
    runIncrementalJobs(true, true);
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroup").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runIncrementalJobs(true, true);
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup2Assigns.size());
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test3StemAssigns.size());
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup2Assigns.size());

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(0, testGroup4Assigns.size());

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
        
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    // create folder and group and make sure attributes are set

    Stem test4bStem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4b").save();
    Group testGroup3b = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroupb").save();

    runIncrementalJobs(true, true);

    Set<AttributeAssign> test4bStemAssigns = test4bStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3bAssigns = testGroup3b.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");

    assertEquals(1, test4bStemAssigns.size());
    assertEquals(4, test4bStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
        
    assertEquals(1, testGroup3bAssigns.size());
    assertEquals(4, testGroup3bAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
  }
  
  public void testFullStemScopeOne() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroup").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runFullJob();
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup2Assigns.size());
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test3StemAssigns.size());
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup2Assigns.size());

    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));

    assertEquals(0, testGroup4Assigns.size());

    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("one", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
        
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    // create folder and group and make sure attributes are set

    Stem test4bStem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4b").save();
    Group testGroup3b = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroupb").save();

    runFullJob();

    Set<AttributeAssign> test4bStemAssigns = test4bStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3bAssigns = testGroup3b.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");

    assertEquals(1, test4bStemAssigns.size());
    assertEquals(4, test4bStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4bStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
        
    assertEquals(1, testGroup3bAssigns.size());
    assertEquals(4, testGroup3bAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3bAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
  }
  
  public void testIncrementalStemNotProvisionable() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");

    // init stuff
    runIncrementalJobs(true, true);
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    //test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroup").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runIncrementalJobs(true, true);
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(3, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    test3StemAttributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(3, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  public void testFullStemNotProvisionable() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test2Stem = new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
    new StemSave(this.grouperSession).assignName("anotherStem").save();
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    //test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroup").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group anotherStemTestGroup = new GroupSave(this.grouperSession).assignName("anotherStem:testGroup").save();
        
    runFullJob();
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(3, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
    
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup3Assigns.size());
    assertEquals(4, testGroup3Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup3Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup4Assigns.size());
    assertEquals(4, testGroup4Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(test3Stem.getUuid(), testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup4Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(4, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test4StemAssigns.size());
    assertEquals(4, test4StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(test3Stem.getUuid(), test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test4StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());

    test3StemAttributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();
    
    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup2Assigns = testGroup2.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup3Assigns = testGroup3.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testGroup4Assigns = testGroup4.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test2StemAssigns = test2Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test3StemAssigns = test3Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    test4StemAssigns = test4Stem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(4, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testGroup2Assigns.size());
    assertEquals(4, testGroup2Assigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(testStem.getUuid(), testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroup2Assigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, testGroup3Assigns.size());
    assertEquals(0, testGroup4Assigns.size());
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test2StemAssigns.size());
    assertEquals(4, test2StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("false", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(testStem.getUuid(), test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test2StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, test3StemAssigns.size());
    assertEquals(3, test3StemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals(null, test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", test3StemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(0, test4StemAssigns.size());

    assertEquals(0, anotherStemTestGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef").size());
  }
  
  private static void addGroupType(Group group, String typeString) {

    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), typeString);

    attributeAssign.saveOrUpdate();
  }
  
  private static void removeGroupTypes(Group group) {
    group.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameBase());
  }
  
  private void runIncrementalJobs(boolean runChangeLog, boolean runConsumer) {
    
    if (runChangeLog) {
      ChangeLogTempToEntity.convertRecords();
    }
    
    if (runConsumer) {
      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_junitProvisioningAttributePropagationTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("junitProvisioningAttributePropagationTestCLC", hib3GrouploaderLog, esbConsumer);
    }
  }
  
  private void runFullJob() {
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("junitProvisioningAttributePropagationTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  }
}
