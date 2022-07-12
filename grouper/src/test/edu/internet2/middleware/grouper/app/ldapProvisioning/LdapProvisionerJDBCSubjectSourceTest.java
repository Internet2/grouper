package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import com.jcraft.jsch.ConfigRepository.Config;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
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
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
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
public class LdapProvisionerJDBCSubjectSourceTest extends GrouperTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerJDBCSubjectSourceTest("testIncrementalCreateUsers"));    
  }
  
  public LdapProvisionerJDBCSubjectSourceTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerJDBCSubjectSourceTest(String name) {
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
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();    
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
    // wait for message cache to clear
    //GrouperUtil.sleep(10000);

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
  
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
      return hib3GrouploaderLog;
    }
    
    return null;
  }

  public void testIncrementalDoNotCreateUsers() {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignSubjectSourcesToProvision("jdbc"));
  
    // init stuff
    runJobs(true, true);
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    runJobs(true, true);

    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1, false);
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    runJobs(true, true); // mark as provisionable
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    runJobs(true, true); // actually provision to ldap
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    testGroup.deleteMember(notinldap1);
    testGroup.addMember(notinldap2);
  
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    
    // try delete
    testGroup.delete();
  
    runJobs(true, true);
  
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  

  public void testIncrementalCreateUsers() {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignSubjectSourcesToProvision("jdbc")
        .assignInsertEntityAndAttributes(true)
        .assignEntityAttributeCount(6));
  

    // init stuff
    runJobs(true, true);
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
  
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try delete
    testGroup.delete();
  
    runJobs(true, true);
  
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  
  public void testFullCreateUsers() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try delete
    testGroup.delete();
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  
  public void testFullDoNotCreateUsers() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
      new LdapProvisionerTestConfigInput()
      .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
      .assignExplicitFilters(true)
      .assignUpdateGroupsAndDn(true)
      .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
      .assignMembershipAttribute("description")
      .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
      .assignSubjectSourcesToProvision("jdbc"));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1, false);
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    testGroup.deleteMember(notinldap1);
    testGroup.addMember(notinldap2);
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    
    // try delete
    testGroup.delete();
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  
  public void testFullSubjectIdentifierChanged() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignUpdateGroupsAndDn(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignSubjectSourcesToProvision("jdbc"));
        

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.addMember(kwhite, false);
    testGroup.store();
    
    Member jsmithMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), jsmith, false);
    Member bandersonMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), banderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
    
    // now let's say j-smith and b-anderson are the same.  delete j-smith.  update identifier for b-anderson to jsmith.
    testGroup.deleteMember(jsmith);
    RegistrySubject.find("j-smith", true).delete(GrouperSession.staticGrouperSession());
    LdapSessionUtils.ldapSession().delete("personLdap", "uid=banderson,ou=People,dc=example,dc=edu");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='jsmith', searchvalue='jsmith' where subjectid='b-anderson' and name='loginid'", null, null);

    Hib3MemberDAO.membersCacheClear();
    SubjectFinder.flushCache();
    SubjectFinder.findById("b-anderson", true);

    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "ldapProvTest");
    assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(jsmithMember.getId()).getSubjectIdentifier());
    assertEquals("jsmith", gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(bandersonMember.getId()).getSubjectIdentifier());
  }
  
  public void testFullCreateUsersSubjectIdentifier1() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute0", "lfname");
    source.addInitParam("subjectIdentifierAttribute1", "loginid");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true)
        .assignEntityAttributeCount(6)
        .assignInsertEntityAndAttributes(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier1")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description"));

    

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try delete
    testGroup.delete();
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  

  public void testIncrementalCreateUsersSubjectIdentifier1() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute0", "lfname");
    source.addInitParam("subjectIdentifierAttribute1", "loginid");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true)
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier1")
        .assignSubjectSourcesToProvision("jdbc")
        .assignInsertEntityAndAttributes(true)
        .assignEntityAttributeCount(6));
  
    // init stuff
    runJobs(true, true);
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
  
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try delete
    testGroup.delete();
  
    runJobs(true, true);
  
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  
  public void testFullCreateUsersSubjectIdentifier2() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute0", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "loginid");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true)
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier2")
        .assignSubjectSourcesToProvision("jdbc")
        .assignInsertEntityAndAttributes(true)
        .assignEntityAttributeCount(6));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), bwilliams466, false);
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member.getId());
    assertEquals("bwilliams466", gcGrouperSyncMember.getSubjectIdentifier());
    
    // try delete
    testGroup.delete();
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  

  public void testIncrementalCreateUsersSubjectIdentifier2() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute0", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "loginid");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true)
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier2")
        .assignSubjectSourcesToProvision("jdbc")
        .assignInsertEntityAndAttributes(true)
        .assignEntityAttributeCount(6));
  
    // init stuff
    runJobs(true, true);
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    RegistrySubject.add(grouperSession, "j-smith", "person", "jsmith name", "jsmith name", "jsmith", null, null);
    RegistrySubject.add(grouperSession, "b-anderson", "person", "banderson name", "banderson name", "banderson", null, null);
    RegistrySubject.add(grouperSession, "k-white", "person", "kwhite name", "kwhite name", "kwhite", null, null);
    RegistrySubject.add(grouperSession, "w-henderson", "person", "whenderson name", "whenderson name", "whenderson", null, null);
    RegistrySubject.add(grouperSession, "b-lopez", "person", "blopez name", "blopez name", "blopez", null, null);
    RegistrySubject.add(grouperSession, "h-davis", "person", "hdavis name", "hdavis name", "hdavis", null, null);
    RegistrySubject.add(grouperSession, "b-williams466", "person", "bwilliams466 name", "bwilliams466 name", "bwilliams466", null, null);
  
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", null, null, null);

    Subject jsmith = SubjectFinder.findByIdentifier("jsmith", true);
    Subject banderson = SubjectFinder.findByIdentifier("banderson", true);
    Subject kwhite = SubjectFinder.findByIdentifier("kwhite", true);
    Subject whenderson = SubjectFinder.findByIdentifier("whenderson", true);
    Subject blopez = SubjectFinder.findByIdentifier("blopez", true);
    Subject hdavis = SubjectFinder.findByIdentifier("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findByIdentifier("bwilliams466", true);
    
    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
  
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    runJobs(true, true);
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), bwilliams466, false);
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member.getId());
    assertEquals("bwilliams466", gcGrouperSyncMember.getSubjectIdentifier());
    
    // try delete
    testGroup.delete();
  
    runJobs(true, true);
  
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
}
