package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class SimpleLdapProvisionerTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SimpleLdapProvisionerTest("testDeletingAGroupOnGrouperSideVariousDeleteTypesIncrementalProvisioning"));    
//    TestRunner.run(new SimpleLdapProvisionerTest("testSimpleLdapProvisionerFullLegacyConfig_1"));    
  }
  
  public SimpleLdapProvisionerTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public SimpleLdapProvisionerTest(String name) {
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
    
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.id", "personLdapSource");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.name", "personLdapSource");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.types", "person");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.adapterClass", "edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.ldapServerId.value", "personLdap");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.SubjectID_AttributeType.value", "uid");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.SubjectID_formatToLowerCase.value", "false");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.Name_AttributeType.value", "cn");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.Description_AttributeType.value", "cn");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.subjectVirtualAttribute_0_searchAttribute0.value", "${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('uid'), \"\")},${subjectUtils.defaultIfBlank(subject.getAttributeValueOrCommaSeparated('cn'), \"\")}");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.sortAttribute0.value", "cn");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.searchAttribute0.value", "searchAttribute0");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubject.param.filter.value", "(&(uid=%TERM%)(objectclass=person))");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubject.param.scope.value", "SUBTREE_SCOPE");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubject.param.base.value", "dc=example,dc=edu");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.filter.value", "(&(uid=%TERM%)(objectclass=person))");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.scope.value", "SUBTREE_SCOPE");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.searchSubjectByIdentifier.param.base.value", "dc=example,dc=edu");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.search.param.filter.value", "(&(|(uid=%TERM%)(cn=*%TERM%*))(objectclass=person))");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.search.param.scope.value", "SUBTREE_SCOPE");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.search.search.param.base.value", "dc=example,dc=edu");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.attributes", "cn, uid, eduPersonAffiliation, givenName, sn");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.internalAttributes", "searchAttribute0");
    SubjectConfig.retrieveConfig().propertiesOverrideMap().put("subjectApi.source.personLdapSource.param.subjectIdentifierAttribute0.value", "uid");
    SourceManager.getInstance().reloadSource("personLdapSource");
    SourceManager.getInstance().loadSource(SubjectConfig.retrieveConfig().retrieveSourceConfigs().get("personLdapSource"));
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();    
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
//  /**
//   * simple provisioning of subject ids to ldap group
//   */
//  public void testSimpleLdapProvisionerFullLegacyConfig_1() {
//        
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAttributes", "cn, gidNumber");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeNameForMemberships", "description");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupMatchingIdExpression", "${targetGroup.retrieveAttributeValueString('gidNumber')}");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.scriptCount", "4");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.0.script", "${grouperTargetGroup.setName('cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu')}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.1.script", "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex().toString())}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.2.script", "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName())}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.3.script", "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('top', 'posixGroup'))}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.scriptCount", "1");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.0.script", "${grouperTargetGroup.addAttributeValueForMembership('description', grouperProvisioningEntity.getSubjectId())}");
//
//    internal_testSimpleLdapProvisionerFull_1();
//  }
  
  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapProvisionerFullLatestConfig_1() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");
        
    long started = System.currentTimeMillis();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    
    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
    
    // try update
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    
    GrouperUtil.sleep(2000);
    
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    assertEquals(1, gcGrouperSync.getUserCount().intValue());
    assertEquals(1+1+1, gcGrouperSync.getRecordsCount().intValue());
    assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
        System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
    
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
    assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
    assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
    assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
    assertNull(gcGrouperSyncJob.getErrorMessage());
    assertNull(gcGrouperSyncJob.getErrorTimestamp());
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
    assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
    assertNull(gcGrouperSyncGroup.getInTargetEnd());
    assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncGroup.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
    assertNull(gcGrouperSyncGroup.getGroupToId2());
    assertNull(gcGrouperSyncGroup.getGroupFromId2());
    assertNull(gcGrouperSyncGroup.getGroupFromId3());
    assertNull(gcGrouperSyncGroup.getGroupToId3());
    assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
    assertNull(gcGrouperSyncGroup.getErrorMessage());
    assertNull(gcGrouperSyncGroup.getErrorTimestamp());
    assertNull(gcGrouperSyncGroup.getLastGroupSync());

    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
    assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
    assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
    assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
    assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
    assertNull(gcGrouperSyncMember.getInTargetDb());
    assertNull(gcGrouperSyncMember.getInTargetInsertOrExistsDb());
    assertNull(gcGrouperSyncMember.getInTargetStart());
    assertNull(gcGrouperSyncMember.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncMember.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMember.getMemberFromId2());
    assertNull(gcGrouperSyncMember.getMemberFromId3());
    assertNull(gcGrouperSyncMember.getMemberToId2());
    assertNull(gcGrouperSyncMember.getMemberToId3());
    assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
    assertNull(gcGrouperSyncMember.getErrorMessage());
    assertNull(gcGrouperSyncMember.getErrorTimestamp());
    assertNull(gcGrouperSyncMember.getLastUserSync());

    GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
    assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
    assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
    assertNull(gcGrouperSyncMembership.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMembership.getMembershipId());
    assertNull(gcGrouperSyncMembership.getMembershipId2());
    assertNull(gcGrouperSyncMembership.getErrorMessage());
    assertNull(gcGrouperSyncMembership.getErrorTimestamp());

    
    // try delete, not configured to
    attributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
  }
  
  
  public void testDeletingAGroupOnGrouperSideShouldRemoveFromLdapAsWell() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createGroups", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());


    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));

    //delete the group from grouper side
    testGroup.delete();
    
    Group group = GroupFinder.findByUuid(grouperSession, testGroup.getUuid(), false);
    
    assertNull(group);
    
    // reprovision - testGroup should be gone from ldap now
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(0, ldapEntries.size());
    
  }
  
  
  
  public void testDeletingAGroupOnGrouperSideVariousDeleteTypesFullProvisioning() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.debugLog", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createGroups", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());


    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
    
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    ldapEntry = new LdapEntry("cn=test:testGroup2,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "test:testGroup2"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "top"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "posixGroup"));
    ldapEntry.addAttribute(new LdapAttribute("gidNumber", String.valueOf(testGroup2.getIdIndex())));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
    
    ldapEntry = new LdapEntry("cn=test:testGroup3,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "test:testGroup3"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "top"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "posixGroup"));
    ldapEntry.addAttribute(new LdapAttribute("gidNumber", "121"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(3, ldapEntries.size());
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    GcGrouperSyncGroup gcGrouperSyncTestGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    GcGrouperSyncGroup gcGrouperSyncTestGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
    int size = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll().size();
    
    assertEquals(2, size);
    assertTrue(gcGrouperSyncTestGroup.isInTarget());
    assertTrue(gcGrouperSyncTestGroup.isInTargetInsertOrExists());
    assertTrue(gcGrouperSyncTestGroup2.isInTarget());
    assertFalse(gcGrouperSyncTestGroup2.isInTargetInsertOrExists());
    

    //delete the groups from grouper side
    testGroup.delete();
    testGroup2.delete();
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    
    //assert that we have two groups on ldap side (testGroup2 and testGroup3)
    // testGroup2 still stays in ldap because we're only deleting groups that grouper created
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "true");
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    //assert that there's only one group in ldap which is testGroup3 because testGroup2 was deleted by grouper
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    //assert that there's zero groups in ldap because grouper dictates what exists in ldap 100%
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(0, ldapEntries.size());
    
  }
  
  public void testDeletingAGroupOnGrouperSideVariousDeleteTypesIncrementalProvisioning() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.debugLog", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createGroups", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.class", EsbConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerConfigId", "ldapProvTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.debug", "true");
  
    // init stuff
    runJobs(true, true);

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    runJobs(true, true); 

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
    
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:testGroup4").save();
    
    ldapEntry = new LdapEntry("cn=test:testGroup2,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "test:testGroup2"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "top"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "posixGroup"));
    ldapEntry.addAttribute(new LdapAttribute("gidNumber", String.valueOf(testGroup2.getIdIndex())));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
    
    ldapEntry = new LdapEntry("cn=test:testGroup3,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "test:testGroup3"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "top"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "posixGroup"));
    ldapEntry.addAttribute(new LdapAttribute("gidNumber", "121"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
    
    ldapEntry = new LdapEntry("cn=test:testGroup4,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "test:testGroup4"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "top"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "posixGroup"));
    ldapEntry.addAttribute(new LdapAttribute("gidNumber", String.valueOf(testGroup4.getIdIndex())));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
    
    runJobs(true, true);
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(4, ldapEntries.size());
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    GcGrouperSyncGroup gcGrouperSyncTestGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    GcGrouperSyncGroup gcGrouperSyncTestGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
    int size = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll().size();
    
    assertEquals(3, size);
    assertTrue(gcGrouperSyncTestGroup.isInTarget());
    assertTrue(gcGrouperSyncTestGroup.isInTargetInsertOrExists());
    assertTrue(gcGrouperSyncTestGroup2.isInTarget());
    assertFalse(gcGrouperSyncTestGroup2.isInTargetInsertOrExists());
    

    //delete the groups from grouper side
    testGroup.delete();
    testGroup2.delete();
    
    runJobs(true, true);
    
    //assert that we have two groups on ldap side (testGroup2 and testGroup3)
    // testGroup2, testGroup4 still stays in ldap because we're only deleting groups that grouper created
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(3, ldapEntries.size());
    
    testGroup4.delete();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "true");
    
    runJobs(true, true); 
    //assert that there're only two groups in ldap which are testGroup3 and testGroup2, testGroup4 was deleted
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
  }
  
  
  
  
  public void testAddSingleGroupToLdapWithoutAnyMembers() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");
        
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());


    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
  }
  
  public void testAddSingleGroupToLdapWithoutParentFolderProvisioning() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
    
  }
  
  public void testAddGroupThenRemoveManuallyThenAddAgainUsingProvisioning() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");
        
    long started = System.currentTimeMillis();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "false");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
        
    // test some validation, this used to be 'name' instead of 'displayName'
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "displayName");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    List<MultiKey> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    // provisioning.configuration.validation.dnRequired = Error: ${type} field 'name' is required.  It represents the LDAP DN
    // provisioning.configuration.validation.dnString = Error: ${type} field 'name' is must be value type 'string'.  It represents the LDAP DN
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"), null)));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "targetGroupAttribute.0.fieldName")));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "int");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")})));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "#config_targetGroupAttribute.0.fieldName_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")})));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), "#config_targetGroupAttribute.0.fieldName_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    // end test some config
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
    
    // Now delete the group from ldap manually (without provisioning)
    LdapSessionUtils.ldapSession().delete("personLdap", "cn=test:testGroup,ou=Groups,dc=example,dc=edu");
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    // reprovision
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());


    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
  }

  //  /**
  //   * simple provisioning of subject ids to ldap group
  //   */
  //  public void testSimpleLdapProvisionerFullLegacyConfig_1() {
  //        
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAttributes", "cn, gidNumber");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeNameForMemberships", "description");
  //    
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupMatchingIdExpression", "${targetGroup.retrieveAttributeValueString('gidNumber')}");
  //    
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.scriptCount", "4");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.0.script", "${grouperTargetGroup.setName('cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu')}");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.1.script", "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex().toString())}");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.2.script", "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName())}");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.3.script", "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('top', 'posixGroup'))}");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.scriptCount", "1");
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.0.script", "${grouperTargetGroup.addAttributeValueForMembership('description', grouperProvisioningEntity.getSubjectId())}");
  //
  //    internal_testSimpleLdapProvisionerFull_1();
  //  }
    
    /**
     * simple provisioning of subject ids to ldap group
     */
    public void testSimpleLdapEntityProvisionerFull() {
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.numberOfEntityAttributes", "3");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.class", "edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync");

      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.fieldName", "name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.isFieldElseAttribute", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpressionType", "translationScript");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpression", "${'uid=' + grouperProvisioningEntity.subjectId + ',ou=People,dc=example,dc=edu'}");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.valueType", "string");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.matchingId", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.name", "uid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.searchAttribute", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.valueType", "string");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.membershipAttribute", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.multiValued", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.name", "eduPersonEntitlement");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.translateFromGroupSyncField", "groupExtension");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.valueType", "string");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.userSearchAllFilter", "(uid=*)");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.userSearchBaseDn", "ou=People,dc=example,dc=edu");

      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.deleteMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperDeleted", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.insertMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.ldapExternalSystemConfigId", "personLdap");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.operateOnGrouperEntities", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.operateOnGrouperMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.provisioningType", "entityAttributes");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.selectEntities", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.selectMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.subjectSourcesToProvision", "personLdapSource");

      long started = System.currentTimeMillis();

      // ldap specific properties
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.logAllObjectsVerbose", "true");
  
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
  
      Stem stem = new StemSave(this.grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
      Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:testGroup3").save();
      Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
      
      Subject jsmith = SubjectFinder.findById("jsmith", true);
      Subject banderson = SubjectFinder.findById("banderson", true);
      Subject kwhite = SubjectFinder.findById("kwhite", true);
      Subject whenderson = SubjectFinder.findById("whenderson", true);


      testGroup.addMember(jsmith, false);
      testGroup.addMember(banderson, false);
      
      testGroup3.addMember(banderson, false);
      testGroup3.addMember(kwhite, false);
      
      testGroup2.addMember(kwhite, false);
      testGroup2.addMember(whenderson, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("eduPersonEntitlement");
      attributeValue.setTargetName("eduPersonEntitlement");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("eduPersonEntitlement");
      
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup"));
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup"));
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      
      // try update
      testGroup.deleteMember(jsmith);
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("eduPersonEntitlement");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup"));
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
      
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
      assertEquals(1, ldapEntries.size());
      
      ldapEntry = ldapEntries.get(0);
      
      assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());  
    }

    //  /**
    //   * simple provisioning of subject ids to ldap group
    //   */
    //  public void testSimpleLdapProvisionerFullLegacyConfig_1() {
    //        
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAttributes", "cn, gidNumber");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeNameForMemberships", "description");
    //    
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupMatchingIdExpression", "${targetGroup.retrieveAttributeValueString('gidNumber')}");
    //    
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.scriptCount", "4");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.0.script", "${grouperTargetGroup.setName('cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu')}");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.1.script", "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex().toString())}");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.2.script", "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName())}");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.3.script", "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('top', 'posixGroup'))}");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.scriptCount", "1");
    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.0.script", "${grouperTargetGroup.addAttributeValueForMembership('description', grouperProvisioningEntity.getSubjectId())}");
    //
    //    internal_testSimpleLdapProvisionerFull_1();
    //  }
      
      /**
       * simple provisioning of subject ids to ldap group
       */
      public void testSimpleLdapEntityMetadataProvisionerFull() {
        
        
        
        LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "somethingExisting"));
        List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
        ldapModificationItems.add(item);

        new LdapSyncDaoForLdap().modify("personLdap", "uid=banderson,ou=People,dc=example,dc=edu", ldapModificationItems);

        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.class", "edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync");
    
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.configureMetadata", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.deleteMemberships", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperDeleted", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.insertMemberships", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.ldapExternalSystemConfigId", "personLdap");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.metadata.0.formElementType", "text");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.metadata.0.name", "md_entitlementValue");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.metadata.0.showForGroup", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.metadata.0.valueType", "string");

        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.numberOfEntityAttributes", "3");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.numberOfGroupAttributes", "1");

        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.numberOfMetadata", "1");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.operateOnGrouperEntities", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.operateOnGrouperGroups", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.operateOnGrouperMemberships", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.provisioningType", "entityAttributes");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.selectEntities", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.selectMemberships", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.subjectSourcesToProvision", "personLdapSource");

        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.fieldName", "name");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.isFieldElseAttribute", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.select", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpressionType", "translationScript");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.translateExpression", "${'uid=' + grouperProvisioningEntity.subjectId + ',ou=People,dc=example,dc=edu'}");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.0.valueType", "string");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.isFieldElseAttribute", "false");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.matchingId", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.name", "uid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.searchAttribute", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.select", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.1.valueType", "string");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.isFieldElseAttribute", "false");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.membershipAttribute", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.multiValued", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.name", "eduPersonEntitlement");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.select", "true");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.translateFromGroupSyncField", "groupFromId2");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetEntityAttribute.2.valueType", "string");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.isFieldElseAttribute", "false");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.name", "entitlement");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpression", 
            "${grouperUtil.defaultIfBlank(grouperProvisioningGroup.retrieveAttributeValueString('md_entitlementValue') , grouperProvisioningGroup.extension )}");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateExpressionType", "translationScript");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.translateGrouperToGroupSyncField", "groupFromId2");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.targetGroupAttribute.0.valueType", "string");

        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.userSearchAllFilter", "(uid=*)");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    
        long started = System.currentTimeMillis();
    
        // ldap specific properties
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.logAllObjectsVerbose", "true");
    
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
        Stem stem = new StemSave(this.grouperSession).assignName("test").save();
        Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
        
        // mark some folders to provision
        Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
        Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:testGroup3").save();
        Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
        
        Subject jsmith = SubjectFinder.findById("jsmith", true);
        Subject banderson = SubjectFinder.findById("banderson", true);
        Subject kwhite = SubjectFinder.findById("kwhite", true);
        Subject whenderson = SubjectFinder.findById("whenderson", true);
    
    
        testGroup.addMember(jsmith, false);
        testGroup.addMember(banderson, false);
        
        testGroup3.addMember(banderson, false);
        testGroup3.addMember(kwhite, false);
        
        testGroup2.addMember(kwhite, false);
        testGroup2.addMember(whenderson, false);
        
        GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
        attributeValue.setDirectAssignment(true);
        attributeValue.setDoProvision("eduPersonEntitlement");
        attributeValue.setTargetName("eduPersonEntitlement");
        attributeValue.setStemScopeString("sub");
    
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

        attributeValue = new GrouperProvisioningAttributeValue();
        attributeValue.setDirectAssignment(true);
        attributeValue.setDoProvision("eduPersonEntitlement");
        attributeValue.setTargetName("eduPersonEntitlement");
        attributeValue.setMetadataNameValues((Map<String, Object>)(Object)GrouperUtil.toMap("md_entitlementValue","student"));

        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    
        
        //lets sync these over
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("eduPersonEntitlement");
        
        assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
        assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
        // see that the entitlement value is on groupFromId2
        GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "eduPersonEntitlement");
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
        assertEquals("student", gcGrouperSyncGroup.getGroupFromId2());

        
        List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        LdapEntry ldapEntry = ldapEntries.get(0);
        
        assertEquals(3, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("student"));
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("somethingExisting"));
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("student"));
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        
        // try update
        testGroup.deleteMember(jsmith);
        grouperProvisioner = GrouperProvisioner.retrieveProvisioner("eduPersonEntitlement");
        grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
        assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(3, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("student"));
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("somethingExisting"));
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
        assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("testGroup3"));
        
        ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
        assertEquals(1, ldapEntries.size());
        
        ldapEntry = ldapEntries.get(0);
        
        assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());  
      }

      //  /**
      //   * simple provisioning of subject ids to ldap group
      //   */
      //  public void testSimpleLdapProvisionerFullLegacyConfig_1() {
      //        
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAttributes", "cn, gidNumber");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeNameForMemberships", "description");
      //    
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupMatchingIdExpression", "${targetGroup.retrieveAttributeValueString('gidNumber')}");
      //    
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.scriptCount", "4");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.0.script", "${grouperTargetGroup.setName('cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu')}");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.1.script", "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex().toString())}");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.2.script", "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName())}");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.3.script", "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('top', 'posixGroup'))}");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.scriptCount", "1");
      //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.0.script", "${grouperTargetGroup.addAttributeValueForMembership('description', grouperProvisioningEntity.getSubjectId())}");
      //
      //    internal_testSimpleLdapProvisionerFull_1();
      //  }
        
        /**
         * simple provisioning of subject ids to ldap group
         */
        public void testSimpleLdapProvisionerRestrictGroup() {
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
          
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");
              
          long started = System.currentTimeMillis();
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "false");
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
          
          Group populationValidUsers = new GroupSave(this.grouperSession).assignName("population:validUsers").assignCreateParentStemsIfNotExist(true).save();
          Group populationValidUsers2 = new GroupSave(this.grouperSession).assignName("population:validUsers2").assignCreateParentStemsIfNotExist(true).save();
          populationValidUsers.addMember(populationValidUsers2.toSubject());
          
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupIdOfUsersToProvision", populationValidUsers.getId());
          
          // ldap specific properties
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
      
          GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

          populationValidUsers2.addMember(SubjectTestHelper.SUBJ0);
          populationValidUsers2.addMember(SubjectTestHelper.SUBJ1);
              
          Stem stem = new StemSave(this.grouperSession).assignName("test").save();
          Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
          
          // mark some folders to provision
          Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
          Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
          
          testGroup.addMember(SubjectTestHelper.SUBJ0, false);
          testGroup.addMember(SubjectTestHelper.SUBJ1, false);
          testGroup.addMember(SubjectTestHelper.SUBJ4, false);
          
          testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
          testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
          
          final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
          attributeValue.setDirectAssignment(true);
          attributeValue.setDoProvision("ldapProvTest");
          attributeValue.setTargetName("ldapProvTest");
          attributeValue.setStemScopeString("sub");
      
          GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
          //lets sync these over
          GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
          
          assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
          
          GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
          assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
          List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
          assertEquals(1, ldapEntries.size());
          
          LdapEntry ldapEntry = ldapEntries.get(0);
          
          assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
          assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
          assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
          assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
          assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
          assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
          assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
          assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
          assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
          
          // try update
          testGroup.deleteMember(SubjectTestHelper.SUBJ1);
          grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
          grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
          assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
          ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
          assertEquals(1, ldapEntries.size());
          
          ldapEntry = ldapEntries.get(0);
          
          assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
          assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
          assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
          assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
          assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
          assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
          assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
          assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
          
          GrouperUtil.sleep(2000);
          
          //get the grouper_sync and check cols
          GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
          assertEquals(1, gcGrouperSync.getGroupCount().intValue());
          //TODO fix the failing grouper sync assertions
          //assertEquals(1, gcGrouperSync.getUserCount().intValue());
          //assertEquals(1+1+1, gcGrouperSync.getRecordsCount().intValue());
          assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
          assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
              System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
          assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
          
          GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
          assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
          assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
          assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
          assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
          assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
          assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
          assertNull(gcGrouperSyncJob.getErrorMessage());
          assertNull(gcGrouperSyncJob.getErrorTimestamp());
          
          GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
          assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
          assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
          assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
          assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
          assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
          assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
          assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
          assertNull(gcGrouperSyncGroup.getInTargetEnd());
          assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
          assertNull(gcGrouperSyncGroup.getProvisionableEnd());
          assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
          assertNull(gcGrouperSyncGroup.getGroupToId2());
          assertNull(gcGrouperSyncGroup.getGroupFromId2());
          assertNull(gcGrouperSyncGroup.getGroupFromId3());
          assertNull(gcGrouperSyncGroup.getGroupToId3());
          assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
          assertNull(gcGrouperSyncGroup.getErrorMessage());
          assertNull(gcGrouperSyncGroup.getErrorTimestamp());
          assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
          Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
          
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
          assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
          assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
          assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
          assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
          assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
          assertNull(gcGrouperSyncMember.getInTargetDb());
          assertNull(gcGrouperSyncMember.getInTargetInsertOrExistsDb());
          assertNull(gcGrouperSyncMember.getInTargetStart());
          assertNull(gcGrouperSyncMember.getInTargetEnd());
          assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
          assertNull(gcGrouperSyncMember.getProvisionableEnd());
          assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
          assertNull(gcGrouperSyncMember.getMemberFromId2());
          assertNull(gcGrouperSyncMember.getMemberFromId3());
          assertNull(gcGrouperSyncMember.getMemberToId2());
          assertNull(gcGrouperSyncMember.getMemberToId3());
          assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
          assertNull(gcGrouperSyncMember.getErrorMessage());
          assertNull(gcGrouperSyncMember.getErrorTimestamp());
          assertNull(gcGrouperSyncMember.getLastUserSync());
      
          GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
          assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
          assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
          assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
          assertNull(gcGrouperSyncMembership.getInTargetEnd());
          assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
          assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
          assertNull(gcGrouperSyncMembership.getMembershipId());
          assertNull(gcGrouperSyncMembership.getMembershipId2());
          assertNull(gcGrouperSyncMembership.getErrorMessage());
          assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
          
          // try delete, not configured to
          attributeValue.setDoProvision(null);
          GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
          grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
          grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
          assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
          assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
      
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
      
          GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
          grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
          grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
          assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
          assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
      
        }

        //  /**
        //   * simple provisioning of subject ids to ldap group
        //   */
        //  public void testSimpleLdapProvisionerFullLegacyConfig_1() {
        //        
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAttributes", "cn, gidNumber");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupAttributeNameForMemberships", "description");
        //    
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupMatchingIdExpression", "${targetGroup.retrieveAttributeValueString('gidNumber')}");
        //    
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.scriptCount", "4");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.0.script", "${grouperTargetGroup.setName('cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu')}");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.1.script", "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex().toString())}");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.2.script", "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName())}");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationGroup.3.script", "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('top', 'posixGroup'))}");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.scriptCount", "1");
        //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.grouperToTargetTranslationMembership.0.script", "${grouperTargetGroup.addAttributeValueForMembership('description', grouperProvisioningEntity.getSubjectId())}");
        //
        //    internal_testSimpleLdapProvisionerFull_1();
        //  }
          
          /**
           * simple provisioning of subject ids to ldap group
           */
          public void testSimpleLdapProvisionerFullOverrideDn() {
            
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "5");
            
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "gidNumber");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
            
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'posixGroup')}");    
            
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "subjectId");
                
            long started = System.currentTimeMillis();
            
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc, personLdapSource");    
            
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "false");
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=posixGroup)");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
            
            // ldap specific properties
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
        
            GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
        
                
            Stem stem = new StemSave(this.grouperSession).assignName("test").save();
            Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
            
            // mark some folders to provision
            Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
            Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
            
            testGroup.addMember(SubjectTestHelper.SUBJ0, false);
            testGroup.addMember(SubjectTestHelper.SUBJ1, false);
            
            testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
            testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
            
            final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
            attributeValue.setDirectAssignment(true);
            attributeValue.setDoProvision("ldapProvTest");
            attributeValue.setTargetName("ldapProvTest");
            attributeValue.setStemScopeString("sub");
        
            GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
        
            //lets sync these over
            GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
            
            assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
            
            GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
            assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
        
            List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
            assertEquals(1, ldapEntries.size());
            
            LdapEntry ldapEntry = ldapEntries.get(0);
            
            assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
            assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
            assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
            assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
            assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
            assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
            assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
            assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
            assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.1"));
            
            // try update
            testGroup.deleteMember(SubjectTestHelper.SUBJ1);
            grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
            grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
            assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
        
            ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
            assertEquals(1, ldapEntries.size());
            
            ldapEntry = ldapEntries.get(0);
            
            assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
            assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
            assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
            assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
            assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
            assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
            assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
            assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
            
            GrouperUtil.sleep(2000);
            
            //get the grouper_sync and check cols
            GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
            assertEquals(1, gcGrouperSync.getGroupCount().intValue());
            assertEquals(1, gcGrouperSync.getUserCount().intValue());
            assertEquals(1+1+1, gcGrouperSync.getRecordsCount().intValue());
            assertTrue(started <=  gcGrouperSync.getLastFullSyncRun().getTime());
            assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
                System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
            assertTrue(started <= gcGrouperSync.getLastUpdated().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
            
            GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
            assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
            assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
            assertTrue(started <= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
            assertTrue(started <= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
            assertTrue(started <= gcGrouperSyncJob.getHeartbeat().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
            assertTrue(started <= gcGrouperSyncJob.getLastUpdated().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
            assertNull(gcGrouperSyncJob.getErrorMessage());
            assertNull(gcGrouperSyncJob.getErrorTimestamp());
            
            GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
            assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
            assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
            assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
            assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
            assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
            assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
            assertTrue(started <= gcGrouperSyncGroup.getInTargetStart().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
            assertNull(gcGrouperSyncGroup.getInTargetEnd());
            assertTrue(started <= gcGrouperSyncGroup.getProvisionableStart().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
            assertNull(gcGrouperSyncGroup.getProvisionableEnd());
            assertTrue(started <= gcGrouperSyncGroup.getLastUpdated().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
            assertNull(gcGrouperSyncGroup.getGroupToId2());
            assertNull(gcGrouperSyncGroup.getGroupFromId2());
            assertNull(gcGrouperSyncGroup.getGroupFromId3());
            assertNull(gcGrouperSyncGroup.getGroupToId3());
            assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
            assertNull(gcGrouperSyncGroup.getErrorMessage());
            assertNull(gcGrouperSyncGroup.getErrorTimestamp());
            assertNull(gcGrouperSyncGroup.getLastGroupSync());
        
            Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
            
            GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
            assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
            assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
            assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
            assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
            assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
            assertNull(gcGrouperSyncMember.getInTargetDb());
            assertNull(gcGrouperSyncMember.getInTargetInsertOrExistsDb());
            assertNull(gcGrouperSyncMember.getInTargetStart());
            assertNull(gcGrouperSyncMember.getInTargetEnd());
            assertTrue(started <= gcGrouperSyncMember.getProvisionableStart().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
            assertNull(gcGrouperSyncMember.getProvisionableEnd());
            assertTrue(started <= gcGrouperSyncMember.getLastUpdated().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
            assertNull(gcGrouperSyncMember.getMemberFromId2());
            assertNull(gcGrouperSyncMember.getMemberFromId3());
            assertNull(gcGrouperSyncMember.getMemberToId2());
            assertNull(gcGrouperSyncMember.getMemberToId3());
            assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
            assertNull(gcGrouperSyncMember.getErrorMessage());
            assertNull(gcGrouperSyncMember.getErrorTimestamp());
            assertNull(gcGrouperSyncMember.getLastUserSync());
        
            GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
            assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
            assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
            assertTrue(started <= gcGrouperSyncMembership.getInTargetStart().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
            assertNull(gcGrouperSyncMembership.getInTargetEnd());
            assertTrue(started <= gcGrouperSyncMembership.getLastUpdated().getTime());
            assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
            assertNull(gcGrouperSyncMembership.getMembershipId());
            assertNull(gcGrouperSyncMembership.getMembershipId2());
            assertNull(gcGrouperSyncMembership.getErrorMessage());
            assertNull(gcGrouperSyncMembership.getErrorTimestamp());
        
            
            // try delete, not configured to
            attributeValue.setDoProvision(null);
            GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
            grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
            grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
            assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
        
            assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
        
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
            GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
        
            GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
            grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
            grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
            assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
        
            assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
        
          }

          
    private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
      
      // wait for message cache to clear
      GrouperUtil.sleep(10000);
      
      if (runChangeLog) {
        ChangeLogTempToEntity.convertRecords();
      }
      
      if (runConsumer) {
        Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
        hib3GrouploaderLog.setHost(GrouperUtil.hostname());
        hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_ldapProvTestCLC");
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
        EsbConsumer esbConsumer = new EsbConsumer();
        ChangeLogHelper.processRecords("ldapProvTestCLC", hib3GrouploaderLog, esbConsumer);
    
        return hib3GrouploaderLog;
      }
      
      return null;
    }
}

