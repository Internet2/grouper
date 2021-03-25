package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
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
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerBushyTest extends GrouperTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerBushyTest("testIncrementalLdapBushy"));    
  }
  
  public LdapProvisionerBushyTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerBushyTest(String name) {
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
    
    setupLdapAndSubjectSource();
  }

  public static void setupLdapAndSubjectSource() {
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
    
    SourceManager.getInstance().loadSource(SubjectConfig.retrieveConfig().retrieveSourceConfigs().get("personLdapSource"));
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();  
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  private Hib3GrouperLoaderLog runIncrementalJobs(boolean runChangeLog, boolean runConsumer) {
    
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
  
  private void runFullJob() {
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  }
  
  public void testFullLdapBushy() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "extension");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "personLdapSource");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "bushy");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.updateGroups", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.class", EsbConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerConfigId", "ldapProvTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.debug", "true");
  
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").assignCreateParentStemsIfNotExist(true).save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    runFullJob();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test"));
    
    // add more groups
    Group test4Group = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group test4Group2 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup2").save();
    
    runFullJob();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test2"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test3"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test4"));
    
    // try update
    testGroup.setDescription("new test description");
    testGroup.store();
    testGroup.deleteMember(jsmith);
    testGroup.addMember(banderson);
  
    runFullJob();
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("new test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
  
    
    // try rename
    test4Stem.setExtension("test4b");
    test4Stem.store();
    
    runFullJob();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test2"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test3"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test4b"));
        
    // try deletes
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group.getName(), true).delete();
    
    runFullJob();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    runFullJob();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    runFullJob();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    runFullJob();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
  
  public void testIncrementalLdapBushy() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "extension");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "personLdapSource");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "bushy");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.updateGroups", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.class", EsbConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerConfigId", "ldapProvTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.ldapProvTestCLC.publisher.debug", "true");
  
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").assignCreateParentStemsIfNotExist(true).save();
    
    // init stuff
    runIncrementalJobs(true, true);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    runIncrementalJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test"));
    
    // add more groups
    Group test4Group = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup").save();
    Group test4Group2 = new GroupSave(this.grouperSession).assignName("test:test2:test3:test4:testGroup2").save();
    
    runIncrementalJobs(true, true);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test2"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test3"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("organizationalUnit"));
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test4"));
    
    // try update
    testGroup.setDescription("new test description");
    testGroup.store();
    testGroup.deleteMember(jsmith);
    testGroup.addMember(banderson);
  
    runIncrementalJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("new test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
  
    
    // try rename
    test4Stem.setExtension("test4b");
    test4Stem.store();
    
    runIncrementalJobs(true, true);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test2"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test3"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {"objectClass", "ou"}, null).get(0);
    assertEquals("ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("ou").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("ou").getStringValues().contains("test4b"));
        
    // try deletes
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group.getName(), true).delete();
    
    runIncrementalJobs(true, true);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    runIncrementalJobs(true, true);
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    runIncrementalJobs(true, true);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    runIncrementalJobs(true, true);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
}
