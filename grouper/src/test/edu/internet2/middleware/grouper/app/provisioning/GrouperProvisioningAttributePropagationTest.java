package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic;
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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

/**
 * Note that for these tests, we don't care about updates to the target (ldap).  We're only looking at the 
 * provisioning attribute propagation.  
 * TODO simply the provisioner config to something minimal
 * 
 * @author shilen
 */
public class GrouperProvisioningAttributePropagationTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningAttributePropagationTest("testIncrementalPolicyRestrictionUsingFolder"));    
  }
  
  public GrouperProvisioningAttributePropagationTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperProvisioningAttributePropagationTest(String name) {
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    
    runIncrementalJobs(true, true);
    
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
      assertEquals(testGroup2.getName(), testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }

    testGroup.setExtension("testGroup");
    testGroup.store();
    testGroup2.setExtension("testGroup");
    testGroup2.store();
    
    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      assertEquals(testGroup.getName(), testGroupSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), testGroupSyncGroup.getGroupIdIndex());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
      assertEquals(testGroup2.getName(), testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }
    
    // rename again
    testGroup.setExtension("testGroup_excludes");
    testGroup.store();
    testGroup2.setExtension("testGroup_excludesxx");
    testGroup2.store();

    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb()); 
      assertEquals("test:testGroup", testGroupSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), testGroupSyncGroup.getGroupIdIndex());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertEquals("test:test2:testGroup", testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }
  }
  
  public void testFullRegexRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    
    runFullJob();
    
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
      assertEquals(testGroup2.getName(), testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    testGroup2.setExtension("testGroup");
    testGroup2.store();
    
    runFullJob();
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      assertEquals(testGroup.getName(), testGroupSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), testGroupSyncGroup.getGroupIdIndex());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
      assertEquals(testGroup2.getName(), testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }
    
    // rename again
    testGroup.setExtension("testGroup_excludes");
    testGroup.store();
    testGroup2.setExtension("testGroup_excludesxx");
    testGroup2.store();

    runFullJob();
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb()); 
      assertEquals("test:testGroup", testGroupSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), testGroupSyncGroup.getGroupIdIndex());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertEquals("test:test2:testGroup", testGroup2SyncGroup.getGroupName());
      assertEquals(testGroup2.getIdIndex(), testGroup2SyncGroup.getGroupIdIndex());
    }
  }
  
  public void testFullPolicyRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    metadataNameValues.put("md_grouper_allowPolicyGroupOverride", false);
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
        
    runFullJob();
    
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    addGroupType(testGroup, "policy");
    
    runFullJob();
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }

    removeGroupTypes(testGroup);

    runFullJob();
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    addGroupType(testGroup, "ref");
    
    runFullJob();
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
    }
  }
  
  public void testIncrementalPolicyRestriction() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    metadataNameValues.put("md_grouper_allowPolicyGroupOverride", false);
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
        
    runIncrementalJobs(true, true);

    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }

    addGroupType(testGroup, "policy");
    
    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    removeGroupTypes(testGroup);

    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    addGroupType(testGroup, "ref");
    
    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    } 
  }
  
  public void testIncrementalPolicyRestrictionUsingFolder() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    metadataNameValues.put("md_grouper_allowPolicyGroupOverride", false);
    test2StemAttributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test2StemAttributeValue, test2Stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:test2:testGroup").save();
        
    runIncrementalJobs(true, true);

    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }

    addGroupType(testStem, "policy");
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    removeGroupTypes(testStem);
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    }
    
    addGroupType(testStem, "ref");
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    runIncrementalJobs(true, true);
    
    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("F", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNotNull(testGroup2SyncGroup.getMetadataJson());
    } 
  }
  
  public void testIncrementalStemScopeOne() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
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
        
    runIncrementalJobs(true, true);
    
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
    }
    

    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runIncrementalJobs(true, true);

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runIncrementalJobs(true, true);

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    // create folder and group and make sure attributes are set

    new StemSave(this.grouperSession).assignName("test:test2:test3:test4b").save();
    Group testGroup3b = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroupb").save();

    runIncrementalJobs(true, true);
    
    {
      assertEquals(5, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      GcGrouperSyncGroup testGroup3bSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3b.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3bSyncGroup.getMetadataJson());
    }
  }
  
  public void testFullStemScopeOne() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
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
        
    runFullJob();
        
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
    }
    

    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runFullJob();

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    GrouperProvisioningAttributeValue test3StemAttributeValue = new GrouperProvisioningAttributeValue();
    test3StemAttributeValue.setDirectAssignment(true);
    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    test3StemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    testStemAttributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    runFullJob();

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    // create folder and group and make sure attributes are set

    new StemSave(this.grouperSession).assignName("test:test2:test3:test4b").save();
    Group testGroup3b = new GroupSave(this.grouperSession).assignName("test:test2:test3:testGroupb").save();

    runFullJob();
    
    {
      assertEquals(5, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      GcGrouperSyncGroup testGroup3bSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3b.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3bSyncGroup.getMetadataJson());
    }
  }
  
  public void testIncrementalStemNotProvisionable() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
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
        
    runIncrementalJobs(true, true);

    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
    }

    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    test3StemAttributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runIncrementalJobs(true, true);
  
    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }
  }
  
  public void testFullStemNotProvisionable() {


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    new StemSave(this.grouperSession).assignName("test:test2").save();
    Stem test3Stem = new StemSave(this.grouperSession).assignName("test:test2:test3").save();
    new StemSave(this.grouperSession).assignName("test:test2:test3:test4").save();
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
        
    runFullJob();

    {
      assertEquals(2, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
    }

    test3StemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();

    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }

    test3StemAttributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(test3StemAttributeValue, test3Stem);

    runFullJob();
  
    {
      assertEquals(4, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      GcGrouperSyncGroup testGroup2SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      GcGrouperSyncGroup testGroup3SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId());
      GcGrouperSyncGroup testGroup4SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
      
      assertEquals("T", testGroup2SyncGroup.getProvisionableDb());
      assertNull(testGroup2SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup3SyncGroup.getProvisionableDb());
      assertNull(testGroup3SyncGroup.getMetadataJson());
      
      assertEquals("F", testGroup4SyncGroup.getProvisionableDb());
      assertNull(testGroup4SyncGroup.getMetadataJson());
    }
  }
  
  public void testIncrementalDirectToIndirectGroup() {

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();

    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue testGroupAttributeValue = new GrouperProvisioningAttributeValue();
    testGroupAttributeValue.setDirectAssignment(true);
    testGroupAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testGroupAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testGroupAttributeValue, testGroup);
    
    runIncrementalJobs(true, true);
    
    Set<AttributeAssign> testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    Set<AttributeAssign> testStemAssigns = testStem.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(1, testGroupAssigns.size());
    assertEquals(3, testGroupAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals(null, testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testGroupAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    assertEquals(1, testStemAssigns.size());
    assertEquals(4, testStemAssigns.iterator().next().getAttributeDelegate().retrieveAssignments().size());
    assertEquals("true", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDirectAssign"));
    assertEquals("sub", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningStemScope"));
    assertEquals(null, testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningOwnerStemId"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningDoProvision"));
    assertEquals("junitProvisioningAttributePropagationTest", testStemAssigns.iterator().next().getAttributeValueDelegate().retrieveValueString("etc:provisioning:provisioningTarget"));
    
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
    }
    
    testGroupAssigns.iterator().next().delete();

    testGroupAssigns = testGroup.getAttributeDelegate().retrieveAssignmentsByAttributeDef("etc:provisioning:provisioningDef");
    assertEquals(0, testGroupAssigns.size());

    runIncrementalJobs(true, true);
  
    {
      assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
      GcGrouperSyncGroup testGroupSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
       
      assertEquals("T", testGroupSyncGroup.getProvisionableDb());
      assertNull(testGroupSyncGroup.getMetadataJson());
    }
  }
  
  public void testFullMultipleProvisioners() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest2.logAllObjectsVerbose", "true");

    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.provisionerConfigId", "junitProvisioningAttributePropagationTest2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.junitProvisioningAttributePropagationTest2CLC.publisher.debug", "true");
    


    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.translateFromMemberSyncField", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
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
    
    GrouperProvisioningAttributeValue testStemAttributeValue = new GrouperProvisioningAttributeValue();
    testStemAttributeValue.setDirectAssignment(true);
    testStemAttributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    testStemAttributeValue.setStemScopeString("one");
    testStemAttributeValue.setMetadataNameValues(Collections.singletonMap("test", "test"));
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    GrouperProvisioningAttributeValue testStemAttributeValue2 = new GrouperProvisioningAttributeValue();
    testStemAttributeValue2.setDirectAssignment(true);
    testStemAttributeValue2.setDoProvision("junitProvisioningAttributePropagationTest2");
    testStemAttributeValue2.setTargetName("junitProvisioningAttributePropagationTest2");
    testStemAttributeValue2.setStemScopeString("one");
    testStemAttributeValue2.setMetadataNameValues(Collections.singletonMap("test2", "test2"));
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue2, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
        
    runFullJob();
    runFullJob2();
    
    Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup2 = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest2").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    
    assertEquals(1, grouperSyncGroupIdToSyncGroup.size());
    assertEquals("T", grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getProvisionableDb());
    assertEquals("{\"test\":\"test\"}", grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getMetadataJson());
   
    assertEquals(1, grouperSyncGroupIdToSyncGroup2.size());
    assertEquals("T", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getProvisionableDb());
    assertEquals("{\"test2\":\"test2\"}", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getMetadataJson());
    
    testStemAttributeValue.setMetadataNameValues(Collections.singletonMap("testx", "testx"));
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);
    
    testStemAttributeValue2.setMetadataNameValues(Collections.singletonMap("test2x", "test2x"));
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue2, testStem);
    
    runFullJob();
    runFullJob2();
    
    grouperSyncGroupIdToSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    grouperSyncGroupIdToSyncGroup2 = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest2").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    
    assertEquals(1, grouperSyncGroupIdToSyncGroup.size());
    assertEquals("T", grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getProvisionableDb());
    assertEquals("{\"testx\":\"testx\"}", grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getMetadataJson());
   
    assertEquals(1, grouperSyncGroupIdToSyncGroup2.size());
    assertEquals("T", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getProvisionableDb());
    assertEquals("{\"test2x\":\"test2x\"}", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getMetadataJson());

    testStemAttributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(testStemAttributeValue, testStem);

    runFullJob();
    runFullJob2();
    
    grouperSyncGroupIdToSyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    grouperSyncGroupIdToSyncGroup2 = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest2").getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(Collections.singletonList(testGroup.getId()));
    
    assertEquals(1, grouperSyncGroupIdToSyncGroup.size());
    assertEquals("F", grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getProvisionableDb());
    assertNotNull(grouperSyncGroupIdToSyncGroup.get(testGroup.getId()).getProvisionableEnd());
    
    assertEquals(1, grouperSyncGroupIdToSyncGroup2.size());
    assertEquals("T", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getProvisionableDb());
    assertEquals("{\"test2x\":\"test2x\"}", grouperSyncGroupIdToSyncGroup2.get(testGroup.getId()).getMetadataJson());    
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
  
  private static void addGroupType(Stem stem, String typeString) {

    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), typeString);

    attributeAssign.saveOrUpdate();
  }
  
  private static void removeGroupTypes(Stem stem) {
    stem.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameBase());
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
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("junitProvisioningAttributePropagationTest");
    grouperProvisioner.retrieveGrouperProvisioningOutput(); // make sure to initialize
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  }
  
  private void runFullJob2() {
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("junitProvisioningAttributePropagationTest2");
    grouperProvisioner.retrieveGrouperProvisioningOutput(); // make sure to initialize
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  }
}
