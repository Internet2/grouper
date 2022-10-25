package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.usdu.UsduJob;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerJDBCSubjectSourceTest extends GrouperProvisioningBaseTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerJDBCSubjectSourceTest("testFullWithUnresolvableRemove"));    
  }
  
  public LdapProvisionerJDBCSubjectSourceTest() {
    super();
  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
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
    
    try {
      SourceManager.getInstance().getSource("personLdapSource");
      fail("Test cant run with personLdapSource!");
    } catch (SourceUnavailableException sue) {
      // this is good
    }
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
    incrementalProvision();
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    incrementalProvision();

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
  
    incrementalProvision(); // mark as provisionable
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    incrementalProvision(); // actually provision to ldap
    
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
  
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
    
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
  
    incrementalProvision();
  
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
    incrementalProvision();
    
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
  
    incrementalProvision();
    
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
  
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
    
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
  
    incrementalProvision();
  
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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
    
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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
  
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
  
    grouperProvisioningOutput = fullProvision();

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
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
  
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());

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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());

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
  
    grouperProvisioningOutput = fullProvision();

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();

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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
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

    try {
      grouperProvisioningOutput = fullProvision(this.defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      // matching error on banderson
    }

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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
  
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
  
    grouperProvisioningOutput = fullProvision();
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
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
    incrementalProvision();
    
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
  
    incrementalProvision();
    
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

    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
    
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
  
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
    } catch (Exception e) {
      
    }
  
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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
    
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
    
    try {
      grouperProvisioningOutput = fullProvision(defaultConfigId(), true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(2, grouperProvisioningOutput.getRecordsWithErrors());
  
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
  
    grouperProvisioningOutput = fullProvision();

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
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
    incrementalProvision();
    
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
  
    incrementalProvision();
    
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
  
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
    
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
  
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
  
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
    } catch (Exception e) {
      
    }
  
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  
  public void testFullCreateUsersUsingMemberIdIndex() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("idIndex")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
            
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    Subject subj = SubjectFinder.findById("notinldap1", true);
    testGroup.addMember(subj);
    
    Member member = MemberFinder.findBySubject(grouperSession, subj, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=" + member.getIdIndex() + ")", new String[] { "uid" }, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    LdapEntry ldapEntry = ldapEntries.get(0);    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=" + member.getIdIndex() + ",ou=People,dc=example,dc=edu"));
  }

  public void testFullCreateUsersGettesPosix() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignProvisioningStrategy("gettesPosix")
        .assignSubjectSourcesToProvision("jdbc")
        .assignConfigId("openldapTestUnixPosixGroups"));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("openldapTestUnixPosixGroups");
    attributeValue.setTargetName("openldapTestUnixPosixGroups");
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
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("openldapTestUnixPosixGroups");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
  
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("j-smith"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("b-anderson"));    
  
    // try update
    testGroup.addMember(notinldap1);
    testGroup.addMember(notinldap2);
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    
    grouperProvisioningOutput = fullProvision("openldapTestUnixPosixGroups");

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
        
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(5, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("j-smith"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("b-lopez"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("h-davis"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("notinldap1"));    
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("notinldap2"));    
  
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    grouperProvisioningOutput = fullProvision("openldapTestUnixPosixGroups", true);

    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=testGroupRenamed,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(6, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("j-smith"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("b-lopez"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("h-davis"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("b-williams466"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("notinldap1"));    
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("notinldap2"));    
  
    // try delete
    testGroup.delete();
  
    grouperProvisioningOutput = fullProvision("openldapTestUnixPosixGroups");
  
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "description", "businessCategory"}, null).size());
  }
  

  public void testFullWithUnresolvableDontInsertAndDontRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't remove unresolvable
    deleteSubject(notinldap1, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
  }
  
  public void testFullWithUnresolvableInsertAndDontRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6)
        .addExtraConfig("unresolvableSubjectsInsert", "true"));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
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
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap2,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't remove unresolvable
    deleteSubject(notinldap1, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
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
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap2,ou=People,dc=example,dc=edu"));    
    }
  }
  

  public void testFullWithUnresolvableRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6)
        .addExtraConfig("unresolvableSubjectsRemove", "true"));
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test remove unresolvable
    deleteSubject(notinldap1, true);    
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    {
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    }
  }
  
  public void testIncrementalWithUnresolvableDontInsertAndDontRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6));
    
    // init stuff
    incrementalProvision();
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    incrementalProvision();
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    incrementalProvision();
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
  }
  
  public void testIncrementalWithUnresolvableInsertAndDontRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6)
        .addExtraConfig("unresolvableSubjectsInsert", "true"));
    
    // init stuff
    incrementalProvision();
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    incrementalProvision();
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    incrementalProvision();
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
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
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap2,ou=People,dc=example,dc=edu"));    
    }
  }
  

  public void testIncrementalWithUnresolvableRemove() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignMembershipAttribute("description")
        .assignEntityUidTranslateFromGrouperProvisioningEntityField("subjectIdentifier0")
        .assignInsertEntityAndAttributes(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignEntityDnTranslate(false)
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignBusinessCategoryTranslateFromGrouperProvisioningGroupField("id")
        .assignSubjectSourcesToProvision("jdbc")
        .assignEntityAttributeCount(6)
        .addExtraConfig("unresolvableSubjectsRemove", "true"));
    
    // init stuff
    incrementalProvision();
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    RegistrySubject.add(grouperSession, "notinldap1", "person", "notinldap1 name", "notinldap1 name", "notinldap1", null, null);
    RegistrySubject.add(grouperSession, "notinldap2", "person", "notinldap2 name", "notinldap2 name", "notinldap2", null, null);

    Subject notinldap1 = SubjectFinder.findById("notinldap1", true);
    Subject notinldap2 = SubjectFinder.findById("notinldap2", true);

    testGroup.addMember(notinldap1);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
    
    incrementalProvision();  
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "uid=notinldap1,ou=People,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=*)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
    
    // test don't insert unresolvable
    testGroup.addMember(notinldap2);
    deleteSubject(notinldap2, true);    
    incrementalProvision();
    
    {
      assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap1)", new String[] {}, null).size());
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=notinldap2)", new String[] {}, null).size());
      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
      assertEquals(1, ldapEntries.size());
      
      LdapEntry ldapEntry = ldapEntries.get(0);
      
      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
      assertEquals(testGroup.getId().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
      assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
      assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=notinldap1,ou=People,dc=example,dc=edu"));    
    }
  }
  
  
  private void deleteSubject(Subject subject, boolean runUsdu) {

    List<RegistrySubject> registrySubjects = HibernateSession.byCriteriaStatic()
      .list(RegistrySubject.class, Restrictions.eq("id", subject.getId()));

    for (RegistrySubject registrySubject : registrySubjects) {
      registrySubject.delete(GrouperSession.staticGrouperSession());
    }

    SubjectFinder.flushCache();

    try {
      SubjectFinder.findById(subject.getId(), true);
      fail("should not find subject " + subject.getId());
    } catch (SubjectNotFoundException e) {
      // OK
    } catch (SubjectNotUniqueException e) {
      fail("subject should be unique " + subject.getId());
    }
    
    Hib3MemberDAO.membersCacheClear();
    
    if (runUsdu) {
      UsduJob.runDaemonStandalone();
      Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
      assertFalse(member.isSubjectResolutionResolvable());
    }
  }
}
