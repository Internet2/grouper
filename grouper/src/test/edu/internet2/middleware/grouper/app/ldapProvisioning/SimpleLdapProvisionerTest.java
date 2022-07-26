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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningValidationIssue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
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
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class SimpleLdapProvisionerTest extends GrouperProvisioningBaseTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SimpleLdapProvisionerTest("testSimpleLdapProvisionerFullOverrideDn"));    
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

    LdapProvisionerTestUtils.setupSubjectSource();
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapProvisionerFullLatestConfig_1() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));
    
    long started = System.currentTimeMillis();
    
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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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
    assertEquals(1, gcGrouperSync.getRecordsCount().intValue());
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
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
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
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
  }
  
  /**
   * 
   */
  public void testSimpleLdapProvisionerFullSubjectIdentifier2() {
    
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute2", "lfname");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignGroupAttributeValueCache2dn(false)
        .assignSubjectSourcesToProvision("jdbc")
        .addExtraConfig("subjectIdentifierForMemberSyncTable", "subjectIdentifier2")
        .addExtraConfig("groupMembershipAttributeValue", "subjectIdentifier")
        );

    long started = System.currentTimeMillis();
    
        
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
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("name.test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("name.test.subject.1"));
    
    // try update
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("name.test.subject.0"));
    
    GrouperUtil.sleep(2000);
    
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    assertEquals(1, gcGrouperSync.getUserCount().intValue());
    assertEquals(1, gcGrouperSync.getRecordsCount().intValue());
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
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
    assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
    assertNull(gcGrouperSyncGroup.getErrorMessage());
    assertNull(gcGrouperSyncGroup.getErrorTimestamp());
    assertNull(gcGrouperSyncGroup.getLastGroupSync());

    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
    assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
    assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
    assertEquals(testSubject0member.getSubjectIdentifier2(), gcGrouperSyncMember.getSubjectIdentifier());
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
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
  }
  
  public void testDeletingAGroupOnGrouperSideShouldRemoveFromLdapAsWell() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

    
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
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

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
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(0, ldapEntries.size());
    
  }
  
  
  
  public void testDeletingAGroupOnGrouperSideVariousDeleteTypesFullProvisioning() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();
    
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
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
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
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    //assert that we have two groups on ldap side (testGroup2 and testGroup3)
    // testGroup2 still stays in ldap because we're only deleting groups that grouper created
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "true");
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    //assert that there's only one group in ldap which is testGroup3 because testGroup2 was deleted by grouper
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    //assert that there's zero groups in ldap because grouper dictates what exists in ldap 100%
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(0, ldapEntries.size());
    
  }
  
  public void testDeletingAGroupOnGrouperSideVariousDeleteTypesIncrementalProvisioning() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated").value("true").store();
    
    ConfigPropertiesCascadeBase.clearCache();

    // init stuff
    incrementalProvision();

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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    incrementalProvision(); 

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
    
    incrementalProvision();
    
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
    
    incrementalProvision();
    
    //assert that we have two groups on ldap side (testGroup2 and testGroup3)
    // testGroup2, testGroup4 still stays in ldap because we're only deleting groups that grouper created
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(3, ldapEntries.size());
    
    testGroup4.delete();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperCreated", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfGrouperDeleted", "true");
    
    incrementalProvision(); 
    //assert that there're only two groups in ldap which are testGroup3 and testGroup2, testGroup4 was deleted
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
  }
  
  
  
  
  public void testAddSingleGroupToLdapWithoutAnyMembers() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));
    
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
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
 
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
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc"));

    // note, targetGroupLink was false for some reason in original test
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.targetGroupAttribute.4.delete").delete();

    long started = System.currentTimeMillis();
    
    // test some validation, this used to be 'name' instead of 'displayName'
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "displayName");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    // init the config
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
    List<ProvisioningValidationIssue> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();

    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    // provisioning.configuration.validation.dnRequired = Error: ${type} field 'name' is required.  It represents the LDAP DN
    // provisioning.configuration.validation.dnString = Error: ${type} field 'name' is must be value type 'string'.  It represents the LDAP DN
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"))));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString")).assignJqueryHandle("targetGroupAttribute.0.name")));
        
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "ldap_dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "int");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    // init the config
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"))));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString")).assignJqueryHandle("targetGroupAttribute.0.name")));

    GrouperTextContainer.resetThreadLocalVariableMap();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.showAttributeValueSettings", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");

    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    // init the config
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    
    GrouperTextContainer.assignThreadLocalVariable("type", "group");

    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"))));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(
        new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString")).assignJqueryHandle("targetGroupAttribute.0.name")));

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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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

  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapEntityProvisionerFull() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("eduPersonEntitlement")
          .assignMembershipStructureEntityAttributes(true)
          .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
          .assignGroupAttributeCount(0)
          .assignEntityAttributeCount(3)
          .assignExplicitFilters(true)
          );
          
    long started = System.currentTimeMillis();

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
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();


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
    grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
  
  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapEntityProvisionerIncremental() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignMembershipStructureEntityAttributes(true)
          .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
          .assignGroupAttributeCount(0)
          .assignEntityAttributeCount(3)
          .assignExplicitFilters(true)
          );

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
          incrementalProvision();
    long started = System.currentTimeMillis();

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
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
    
          incrementalProvision();
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
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
    
    incrementalProvision();
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
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
  
  
  public void testSimpleLdapEntityProvisionerFull_1() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner_1(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("eduPersonEntitlement")
          );
          
    long started = System.currentTimeMillis();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:testGroup3").save();
    
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    testGroup3.addMember(banderson, false);
    testGroup3.addMember(kwhite, false);
    
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup3"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup3"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    
    // try update
    testGroup.deleteMember(jsmith);
    grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=banderson)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup3"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=jsmith)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=kwhite)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("test:testGroup3"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=whenderson)", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());  
  }

  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapEntityMetadataProvisionerFull() {
    
    LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "somethingExisting"));
    List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(item);

    new LdapSyncDaoForLdap().modify("personLdap", "uid=banderson,ou=People,dc=example,dc=edu", ldapModificationItems);

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("eduPersonEntitlement")
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignMembershipStructureEntityAttributes(true)
          .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
          .assignGroupAttributeCount(1)
          .assignEntityAttributeCount(3)
          .assignExplicitFilters(true)
          .assignEntitlementMetadata(true)
          );
     
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
    
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(eduPersonEntitlement=*)", new String[] {"uid"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    // see that the entitlement value is on groupAttributeValueCache0
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "eduPersonEntitlement");
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals("student", gcGrouperSyncGroup.getGroupAttributeValueCache0());

    
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
    grouperProvisioningOutput = fullProvision("eduPersonEntitlement");
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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

  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapProvisionerRestrictGroup() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignGroupAttributeValueCache2dn(false)
        .assignSubjectSourcesToProvision("jdbc"));
    
    long started = System.currentTimeMillis();

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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
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
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

  }

  /**
   * simple provisioning of subject ids to ldap group
   */
  public void testSimpleLdapProvisionerFullOverrideDn() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignGroupAttributeValueCache2dn(false)
        .assignSubjectSourcesToProvision("jdbc"));
    
    long started = System.currentTimeMillis();
        
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
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
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
    assertEquals(1, gcGrouperSync.getRecordsCount().intValue());
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
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
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
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
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
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }
  
  
}

