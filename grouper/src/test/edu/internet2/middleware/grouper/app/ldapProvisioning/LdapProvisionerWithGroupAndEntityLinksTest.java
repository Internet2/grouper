package edu.internet2.middleware.grouper.app.ldapProvisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.sql.Timestamp;
import java.util.List;

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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.examples.GroupAttributeNameValidationHook;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
public class LdapProvisionerWithGroupAndEntityLinksTest extends GrouperProvisioningBaseTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerWithGroupAndEntityLinksTest("testLdapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalseFull"));    
  }
  
  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  public LdapProvisionerWithGroupAndEntityLinksTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerWithGroupAndEntityLinksTest(String name) {
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

    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();   
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);

    super.tearDown();
    
  }
  
  /**
   * provisioning with group and entity links
   */
  public void testLdapProvisionerWithGroupAndEntityLinksFullLatestConfig_1() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .assignPosixGroup(true)
          .assignMembershipAttribute("description"));
          
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findById("bwilliams466", true);
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
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
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    
    // try delete, not configured to
    attributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
  }
  
  /**
   * provisioning with group deleteGroupsIfUnmarkedProvisionable=false
   */
  public void ldapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalse(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .assignPosixGroup(true)
          .assignMembershipAttribute("description")
          .addExtraConfig("deleteGroupsIfUnmarkedProvisionable", "false"));
          
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
        
    new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test:testGroup4").save();
    Group testGroup5 = new GroupSave(this.grouperSession).assignName("test:testGroup5").save();
    Group testGroup6 = new GroupSave(this.grouperSession).assignName("test:testGroup6").save();
    Group testGroup7 = new GroupSave(this.grouperSession).assignName("test:testGroup7").save();
    Group testGroup8 = new GroupSave(this.grouperSession).assignName("test:testGroup8").save();
    Group testGroup9 = new GroupSave(this.grouperSession).assignName("test:testGroup9").save();
    Group testGroup10 = new GroupSave(this.grouperSession).assignName("test:testGroup10").save();
    
    testGroup.addMember(jsmith);
    testGroup.addMember(banderson);
    
    final GrouperProvisioningAttributeValue attributeValueProvision = new GrouperProvisioningAttributeValue();
    attributeValueProvision.setDirectAssignment(true);
    attributeValueProvision.setDoProvision("ldapProvTest");
    attributeValueProvision.setTargetName("ldapProvTest");
    
    final GrouperProvisioningAttributeValue attributeValueNotProvision = new GrouperProvisioningAttributeValue();
    attributeValueNotProvision.setDirectAssignment(true);
    attributeValueNotProvision.setDoProvision(null);
    attributeValueNotProvision.setTargetName("ldapProvTest");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup2);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup3);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup4);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup5);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup6);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup7);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup8);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup9);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueProvision, testGroup10);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"dn"}, null).size());
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"dn"}, null);
    assertEquals(10, ldapEntries.size());
    
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup2)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup3)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup4)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup5)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup6)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup7)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup8)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup9)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup10)", new String[] {"dn"}, null).size());
    
    // mark 3 as do not provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueNotProvision, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueNotProvision, testGroup2);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValueNotProvision, testGroup3);

    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    // should all still exists in ldap
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"dn"}, null);
    assertEquals(10, ldapEntries.size());
    
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup2)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup3)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup4)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup5)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup6)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup7)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup8)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup9)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup10)", new String[] {"dn"}, null).size());
    
    // testGroup memberships should still exist
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"description"}, null).get(0);    
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
    // check group sync objects
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup5.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup6.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup7.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup8.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup9.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup10.getId()).isProvisionable());
    
    // delete some groups - some previously unmarked and some not - these should be removed from ldap
    testGroup2.delete();
    testGroup3.delete();
    testGroup4.delete();
    testGroup5.delete();
    testGroup6.delete();
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"dn"}, null);
    assertEquals(5, ldapEntries.size());
    
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"dn"}, null).size());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup2)", new String[] {"dn"}, null).size());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup3)", new String[] {"dn"}, null).size());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup4)", new String[] {"dn"}, null).size());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup5)", new String[] {"dn"}, null).size());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup6)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup7)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup8)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup9)", new String[] {"dn"}, null).size());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup10)", new String[] {"dn"}, null).size());
    
    // testGroup memberships should still exist
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"description"}, null).get(0);    
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
    // check group sync objects
    gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapProvTest");
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup3.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup4.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup5.getId()).isProvisionable());
    assertFalse(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup6.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup7.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup8.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup9.getId()).isProvisionable());
    assertTrue(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup10.getId()).isProvisionable());
  }
  
  public void testLdapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalseFull() {
    ldapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalse(true);
  }

  public void testLdapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalseIncremental() {
    ldapProvisionerWithDeleteGroupsIfUnmarkedProvisionableFalse(false);
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveMatchingFull() {
    ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveMatchingHelper(true);
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveMatchingIncremental() {
    ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveMatchingHelper(false);
  }
  
  /**
   * provisioning with group and entity links
   */
  public void ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveMatchingHelper(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .addExtraConfig("targetEntityAttribute.1.showAdvancedAttribute", "true")
          .addExtraConfig("targetEntityAttribute.1.showAttributeValueSettings", "true")
          .addExtraConfig("targetEntityAttribute.1.caseSensitiveCompare", "false")
          .addExtraConfig("subjectSourcesToProvision", "personLdapSource,jdbc")
          .assignPosixGroup(true)
          .assignMembershipAttribute("description"));
    
    
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    
    RegistrySubject.add(grouperSession, "Jsmith", "person", "jsmith name", "jsmith name", "Jsmith_identifier", null, null);
     
    Subject jsmith = SubjectFinder.findById("Jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
 
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    // try update
    testGroup.deleteMember(jsmith);
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupMembershipFull() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .addExtraConfig("targetGroupAttribute.4.showAdvancedAttribute", "true")
          .addExtraConfig("targetGroupAttribute.4.showAttributeValueSettings", "true")
          .addExtraConfig("targetGroupAttribute.4.caseSensitiveCompare", "false")
          .assignPosixGroup(true)
          .assignMembershipAttribute("description"));
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(jsmith);
    testGroup.addMember(banderson);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    fullProvision();
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));   
    
    // try update
    LdapSessionUtils.ldapSession().internal_modifyHelper("personLdap", "cn=test:testGroup,ou=Groups,dc=example,dc=edu", 
        List.of(
            new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("description", "uid=jsmith,ou=People,dc=example,dc=edu")),
            new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "uid=Jsmith,ou=People,dc=example,dc=edu")),
            new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("description", "uid=banderson,ou=People,dc=example,dc=edu")),
            new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "uid=Banderson,ou=People,dc=example,dc=edu")),
            new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "uid=bogus,ou=People,dc=example,dc=edu"))
        ));

    testGroup.addMember(kwhite);

    fullProvision();

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));   
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=Jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=Banderson,ou=People,dc=example,dc=edu"));  
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));  
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveMultiValuedAttrFull() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignGroupAttributeCount(6)
          .addExtraConfig("targetGroupAttribute.5.multiValued", "true")
          .addExtraConfig("targetGroupAttribute.5.name", "description")
          .addExtraConfig("targetGroupAttribute.5.translateExpressionType", "translationScript")
          .addExtraConfig("targetGroupAttribute.5.translateExpression", "${grouperUtil.toSet('Value1', 'Value2')}")
          .addExtraConfig("targetGroupAttribute.5.showAdvancedAttribute", "true")
          .addExtraConfig("targetGroupAttribute.5.showAttributeValueSettings", "true")
          .addExtraConfig("targetGroupAttribute.5.caseSensitiveCompare", "false")
          .addExtraConfig("targetGroupAttribute.4.defaultValue", "")
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .assignPosixGroup(true));
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    fullProvision();
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

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
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("Value1"));  
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("Value2"));  
    
    // try update
    LdapSessionUtils.ldapSession().internal_modifyHelper("personLdap", "cn=test:testGroup,ou=Groups,dc=example,dc=edu", 
        List.of(
            new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("description", "Value1")),
            new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("description", "VALUE1"))
        ));

    fullProvision();

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("VALUE1"));  
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("Value2"));    
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupCNAndDNFull() {
    ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupCNAndDNHelper(true);
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupCNAndDNIncremental() {
    ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupCNAndDNHelper(false);
  }
  
  
  
  /**
   * provisioning with group and entity links
   */
  public void ldapProvisionerWithGroupAndEntityLinksCaseInsensitiveGroupCNAndDNHelper(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .addExtraConfig("targetGroupAttribute.0.showAdvancedAttribute", "true")
          .addExtraConfig("targetGroupAttribute.0.showAttributeValueSettings", "true")
          .addExtraConfig("targetGroupAttribute.0.caseSensitiveCompare", "false")
          .addExtraConfig("targetGroupAttribute.2.showAdvancedAttribute", "true")
          .addExtraConfig("targetGroupAttribute.2.showAttributeValueSettings", "true")
          .addExtraConfig("targetGroupAttribute.2.caseSensitiveCompare", "false")
          .assignPosixGroup(true)
          .assignMembershipAttribute("description"));
    
    
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));  

    // try update
    testGroup.setExtension("TestGroup");
    testGroup.store();
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));    
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseSensitiveGroupCNAndDNFull() {
    ldapProvisionerWithGroupAndEntityLinksCaseSensitiveGroupCNAndDNHelper(true);
  }
  
  public void testLdapProvisionerWithGroupAndEntityLinksCaseSensitiveGroupCNAndDNIncremental() {
    ldapProvisionerWithGroupAndEntityLinksCaseSensitiveGroupCNAndDNHelper(false);
  }
  
  
  
  /**
   * provisioning with group and entity links
   */
  public void ldapProvisionerWithGroupAndEntityLinksCaseSensitiveGroupCNAndDNHelper(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true)
          .assignPosixGroup(true)
          .assignMembershipAttribute("description"));
    
    
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));  

    // try update
    testGroup.setExtension("TestGroup");
    testGroup.store();
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:TestGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:TestGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));    
  }
  
  /**
   * provisioning with group and entity links using the member attribute
   */
  public void testLdapProvisionerWithGroupAndEntityLinksFullLatestConfig_2() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true));

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findById("bwilliams466", true);
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroupRenamed,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupRenamed", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=bwilliams466,ou=People,dc=example,dc=edu"));
    
    // try delete, not configured to
    attributeValue.setDoProvision(null);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  }

  /**
   * provisioning with group and entity links
   */
  public void testLdapProvisionerWithGroupAndEntityLinksFullLatestConfig_1_maxLength() {
    
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignPosixGroup(true)
        .assignExplicitFilters(true)
        .assignMembershipAttribute("description")
        .addExtraConfig("targetGroupAttribute.4.showAttributeValidation", "true")
        .addExtraConfig("targetGroupAttribute.4.maxlength", "40")
        );

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findById("bwilliams466", true);
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    //lets sync these over
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    
    try {
      grouperProvisioningOutput = fullProvision();
      fail();
    } catch (Exception e) {
      
    }
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(1, grouperProvisioningOutput.getRecordsWithErrors());
  
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
  }
  
  public void testPolicyGroups() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true)
        .addExtraConfig("allowPolicyGroupOverride", "true")
        .addExtraConfig("onlyProvisionPolicyGroups", "true"));
             
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

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findById("bwilliams466", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    addPolicyType(testGroup);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    // delete policy type
    testGroup.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameBase());

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(0, ldapEntries.size());  
    
    // set override
    attributeValue.getMetadataNameValues().put("md_grouper_allowPolicyGroupOverride", false);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

  }
  
  public void testRegexRestriction() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .addExtraConfig("allowProvisionableRegexOverride", "true")
          .addExtraConfig("provisionableRegex", "groupExtension not matches ^.*_includes$|^.*_excludes$"));
   
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup_includes").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
    Subject bwilliams466 = SubjectFinder.findById("bwilliams466", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    // rename again
    testGroup.setExtension("testGroup_excludes");
    testGroup.store();

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(0, ldapEntries.size());   
  }
  

  /**
   *
   */
  public void testDoNotDeleteFull() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .assignExplicitFilters(true));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = stem.addChildStem("test", "test").addChildStem("test2", "test2").addChildStem("test3", "test3").addChildGroup("testGroup3x", "testGroup3x");

    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    LdapSessionUtils.ldapSession().delete("personLdap", "cn=test:test:test2:test3:testGroup3x,ou=Groups,dc=example,dc=edu");
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  }

  /**
   *
   */
  public void testDoNotDeleteIncremental() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignExplicitFilters(true));

    // initialize
    incrementalProvision();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    Group testGroup = stem.addChildStem("test", "test").addChildStem("test2", "test2").addChildStem("test3", "test3").addChildGroup("testGroup3x", "testGroup3x");

    //lets sync these over
    incrementalProvision();
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("one");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    incrementalProvision();
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    LdapSessionUtils.ldapSession().delete("personLdap", "cn=test:test:test2:test3:testGroup3x,ou=Groups,dc=example,dc=edu");
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    incrementalProvision();
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    testGroup.delete();

    incrementalProvision();
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  }
  

  public void testFullNullDefaultValue() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .addExtraConfig("targetGroupAttribute.4.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.4.defaultValue", "<emptyString>")
        .addExtraConfig("targetGroupAttribute.5.name", "seeAlso")
        .addExtraConfig("targetGroupAttribute.5.defaultValue", "<emptyString>")
        .addExtraConfig("targetGroupAttribute.5.showAttributeValueSettings", "true")
        );
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
  
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
        
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  
    // try update
    testGroup.setDescription("cn=bogusdescription");
    testGroup.store();
    testGroup.addMember(kwhite);
    testGroup.addMember(whenderson);
  
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    
    assertEquals("T", grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), kwhite, false).getUuid()).getInTargetDb());
    assertEquals("T", grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), whenderson, false).getUuid()).getInTargetDb());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains("cn=bogusdescription"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));  
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=whenderson,ou=People,dc=example,dc=edu"));  
    
    // clear members and description
    testGroup.setDescription(null);
    testGroup.store();
    testGroup.deleteMember(kwhite);
    testGroup.deleteMember(whenderson);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  }
  
  public void testIncrementalNullDefaultValueWithDescriptionChange() {
    incrementalNullDefaultValue(true);
  }
  
  public void testIncrementalNullDefaultValueWithNoDescriptionChange() {
    incrementalNullDefaultValue(false);
  }
  
  
  public void incrementalNullDefaultValue(boolean changeDescriptionToo) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .addExtraConfig("targetGroupAttribute.4.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.4.defaultValue", "<emptyString>")
        .addExtraConfig("targetGroupAttribute.5.name", "seeAlso")
        .addExtraConfig("targetGroupAttribute.5.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.5.defaultValue", "<emptyString>"));
  
    // initialize
    incrementalProvision();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    // sync over
    incrementalProvision();
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
  
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
        
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    incrementalProvision();

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  
    // try update
    if (changeDescriptionToo) {
      testGroup.setDescription("cn=bogusdescription");
      testGroup.store();
    }
    testGroup.addMember(kwhite);
    testGroup.addMember(whenderson);
  
    incrementalProvision();
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
    assertEquals("T", grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), kwhite, false).getUuid()).getInTargetDb());
    assertEquals("T", grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), whenderson, false).getUuid()).getInTargetDb());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    if (changeDescriptionToo) {
      assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains("cn=bogusdescription"));
    }
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));  
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=whenderson,ou=People,dc=example,dc=edu"));  
    
    // clear members and description
    if (changeDescriptionToo) {
      testGroup.setDescription(null);
      testGroup.store();
    }
    testGroup.deleteMember(kwhite);
    testGroup.deleteMember(whenderson);

    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    if (changeDescriptionToo) {
      assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
      assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    }
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  }
  
  
  //TODO: we need to make changes to ldap target dao to support search attributes for entities just like groups before this test can pass
  // We need to tweak attributes in this test for users because it was copied from groups and haven't been tested
  
//  public void testIncrementalNullDefaultValueForEntities() {
//    
//    LdapProvisionerTestUtils.configureLdapProvisioner(
//        new LdapProvisionerTestConfigInput()
//          .assignMembershipStructureEntityAttributes(true)
//          .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
//          .assignGroupAttributeCount(0)
//          .assignEntityAttributeCount(3)
//          .assignExplicitFilters(true)
//          .addExtraConfig("targetEntityAttribute.2.showAttributeValueSettings", "true")
//          .addExtraConfig("targetEntityAttribute.2.defaultValue", "<emptyString>")
//          .addExtraConfig("targetEntityAttribute.3.name", "businessCategory")
//          .addExtraConfig("targetEntityAttribute.3.showAttributeValueSettings", "true")
//          .addExtraConfig("targetEntityAttribute.3.defaultValue", "<emptyString>")
//          );
//          
//    long started = System.currentTimeMillis();
//    
//    // initialize
//    incrementalProvision();
//
//    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
//    
//    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
//    attributeValue.setDirectAssignment(true);
//    attributeValue.setDoProvision("ldapProvTest");
//    attributeValue.setTargetName("ldapProvTest");
//    attributeValue.setStemScopeString("sub");
//  
//    // mark some folders to provision
//    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
//    
//    // sync over
//    incrementalProvision();
//    
//    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
//    Group testGroup1 = new GroupSave(this.grouperSession).assignName("test:testGroup1").save();
//  
//    Subject kwhite = SubjectFinder.findById("kwhite", true);
//        
//    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(&(objectClass=person)(uid=kwhite))", new String[] {"objectClass", "uid", "eduPersonEntitlement", "businessCategory"}, null).size());
//  
//    testGroup.addMember(kwhite);
//    testGroup1.addMember(kwhite);
//    
//    incrementalProvision();
//
//    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(&(objectClass=person)(uid=kwhite))", new String[] {"objectClass", "uid", "eduPersonEntitlement", "businessCategory"}, null);
//    assertEquals(1, ldapEntries.size());
//    
//    LdapEntry ldapEntry = ldapEntries.get(0);
//    
//    assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
//    
//    Collection<String> groups = ldapEntry.getAttribute("eduPersonEntitlement").getStringValues();
//    assertTrue(groups.contains("testGroup"));
//    assertTrue(groups.contains("testGroup1"));
//    
////    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
////    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
////    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
////    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
////    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
////    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
//  
//    
//    testGroup.deleteMember(kwhite);
//  
//    incrementalProvision();
//    
//    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
//    grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
//    assertEquals("T", grouperProvisioner.getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), kwhite, false).getUuid()).getInTargetDb());
//
//    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(&(objectClass=person)(uid=kwhite))", new String[] {"objectClass", "uid", "eduPersonEntitlement", "businessCategory"}, null);
//    assertEquals(1, ldapEntries.size());
//    
//    ldapEntry = ldapEntries.get(0);
//    
//    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
//    assertEquals("testGroup1", ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().iterator().next());
//    
//    
////    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
////    assertEquals(1, ldapEntries.size());
////    
////    ldapEntry = ldapEntries.get(0);
////    
////    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
////    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
////    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
////    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
////    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
////    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
////    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains("cn=bogusdescription"));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
////    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));  
////    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=whenderson,ou=People,dc=example,dc=edu"));  
////    
////    // clear members and description
////    testGroup.setDescription(null);
////    testGroup.store();
////    testGroup.deleteMember(kwhite);
////
////    incrementalProvision();
////
////    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
////    assertEquals(1, ldapEntries.size());
////    
////    ldapEntry = ldapEntries.get(0);
////    
////    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
////    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
////    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
////    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
////    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
////    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
////    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
////    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
////    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
//  }
  
  public void testIncrementalNullDefaultValue2() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .addExtraConfig("targetGroupAttribute.4.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.4.defaultValue", "<emptyString>")
        .addExtraConfig("targetGroupAttribute.5.name", "seeAlso")
        .addExtraConfig("targetGroupAttribute.5.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.5.defaultValue", "<emptyString>"));
  
    // initialize
    incrementalProvision();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    // sync over
    incrementalProvision();
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
  
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
        
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    incrementalProvision();

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  
    // try update
    testGroup.addMember(kwhite);
    testGroup.addMember(whenderson);
  
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));  
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=whenderson,ou=People,dc=example,dc=edu"));  
    
    // clear members
    testGroup.deleteMember(kwhite);
    testGroup.deleteMember(whenderson);

    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "seeAlso"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("seeAlso").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("seeAlso").getStringValues().contains(""));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains(""));
  }
  
  public void testFullAndIncrementalTogether() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignUpdateGroupsAndDn(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true));
  
    // initialize
    incrementalProvision();

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    Subject whenderson = SubjectFinder.findById("whenderson", true);
    Subject blopez = SubjectFinder.findById("blopez", true);
    Subject hdavis = SubjectFinder.findById("hdavis", true);
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(kwhite, false);
    testGroup2.addMember(whenderson, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
    
    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);

    // full sync this time
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    // incremental does nothing
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    
    
    // try add member and incremental
    testGroup.addMember(banderson);

    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(4, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=blopez,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=hdavis,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
  }
  
  public void testDnOverrideTranslationScript() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignDnOverrideScript(true));
    
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    // try update
    testGroup.addMember(kwhite, false);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    // try delete
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  public void testFullDnOverrideFlat() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .addExtraConfig("groupSearchAllFilter", null)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignDnOverrideConfig(true)
        );
            
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test:testGroup2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test:testGroup2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }

  public void testFullDnOverrideBushy() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignDnOverrideConfig(true)
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  /**
   * this fails for some reason in harness but works individually, try it
   */
  public void testIncrementalDnOverrideFlat() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .addExtraConfig("groupSearchAllFilter", null)
          .assignDnOverrideConfig(true)
          .assignGroupDeleteType("deleteGroupsIfGrouperCreated")
        );

    // initialize
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    incrementalProvision();
    
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test:testGroup2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);

    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test:testGroup2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }

  public void testIncrementalDnOverrideBushy() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignDnOverrideConfig(true)
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    // initialize
    incrementalProvision();

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);

    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  public void testFullDnCompareOUCase() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.folderRdnAttribute", "oU");

    
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // should be no updates
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(0, grouperProvisioningOutput.getInsert());
    assertEquals(0, grouperProvisioningOutput.getUpdate());
    assertEquals(0, grouperProvisioningOutput.getDelete());
    
    // try update
    testGroup.addMember(kwhite, false);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  public void testFullDnCompareComma() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.validateExtensionByDefault", "false");
    GroupAttributeNameValidationHook.clearHook();
    
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:test,Group").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test\\,Group,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test\\2CGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test,Group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // should be no updates
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(0, grouperProvisioningOutput.getInsert());
    assertEquals(0, grouperProvisioningOutput.getUpdate());
    assertEquals(0, grouperProvisioningOutput.getDelete());
    
    // try update
    testGroup.addMember(kwhite, false);

    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test\\,Group,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=test\\2CGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test,Group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  public void testFullBushyOUCaseChange() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    
    Group testGroup1 = new GroupSave(this.grouperSession).assignName("test:testGroup1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup1);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup1.addMember(jsmith, false);
    testGroup1.setDescription("test description");
    testGroup1.store();
    
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup1,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup1,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup1", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup1.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
    testStem.setExtension("TEST");
    testStem.store();
   
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup1,ou=TEST,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup1,ou=TEST,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup1", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup1.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=TEST,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=TEST,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
  }
  
  public void testIncrementalBushyOUCaseChange() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignTranslateFromGrouperProvisioningGroupField("extension")
          .assignGroupDnTypeBushy(true)
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignExplicitFilters(true)
          .addExtraConfig("groupSearchAllFilter", null)
          );

    // initialize
    incrementalProvision();

    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    
    Group testGroup1 = new GroupSave(this.grouperSession).assignName("test:testGroup1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup1);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup2);

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup1.addMember(jsmith, false);
    testGroup1.setDescription("test description");
    testGroup1.store();
    
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup1,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup1,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup1", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup1.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
    testStem.setExtension("TEST");
    testStem.store();
   
    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup1,ou=TEST,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup1,ou=TEST,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup1", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup1.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=TEST,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=TEST,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
  }
  
  public void testFullOnlyDnOverrideFlat() {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .addExtraConfig("groupSearchAllFilter", null)
          .addExtraConfig("onlyLdapGroupDnOverride", "true")
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignDnOverrideConfig(true)
          .addExtraConfig("logCommandsAlways", "true")
        );
            
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    attributeValue2.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override2,ou=Groups,dc=example,dc=edu");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    LdapEntry ldapEntry = new LdapEntry("cn=override,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    ldapEntry = new LdapEntry("cn=override2,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override2"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    assertEquals(2, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(2, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetGroupsRetrieved")));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);
  
    grouperProvisioningOutput = fullProvision(); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
  
    grouperProvisioningOutput = fullProvision(); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }

  public void testFlatHarvardMarkExistsFull() {
    flatHarvardMarkExists(true);
  }

  public void testFlatHarvardMarkExistsIncremental() {
    flatHarvardMarkExists(false);
  }

  public void flatHarvardMarkExists(boolean isFull) {
    
    long started = System.currentTimeMillis();
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("gpf_groupOfNamesHlsLdap")
          .assignProvisioningStrategy("harvardGroupOfNames")
        );

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("gpf_groupOfNamesHlsLdap");
    attributeValue.setTargetName("gpf_groupOfNamesHlsLdap");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    LdapEntry ldapEntry = new LdapEntry("cn=testGroup:Member,ou=Groups,dc=example,dc=edu");
    LdapAttribute ldapAttribute = new LdapAttribute("objectClass", "groupOfNames");
    ldapAttribute.addStringValue("top");
    ldapEntry.addAttribute(ldapAttribute);
    ldapEntry.addAttribute(new LdapAttribute("member", "uid=banderson,ou=People,dc=example,dc=edu"));
    ldapEntry.addAttribute(new LdapAttribute("businessCategory", "ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)"));
    ldapEntry.addAttribute(new LdapAttribute("description", "test:testGroup"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
  
    //lets sync these over
    if (isFull) {
      fullProvision("gpf_groupOfNamesHlsLdap");
    } else {
      incrementalProvision("gpf_groupOfNamesHlsLdap");
    } 

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(2, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetGroupsRetrieved")));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "openldapTestUnixPosixGroups");
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
    assertNotNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
    assertNull(gcGrouperSyncGroup.getErrorMessage());
    assertNull(gcGrouperSyncGroup.getErrorTimestamp());
    assertNotNull(gcGrouperSyncGroup.getLastGroupSync());
  
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
    assertEquals("test.subject.0", gcGrouperSyncMember.getEntityAttributeValueCache0());
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
    
    // try delete
    testGroup.delete();
    
  
    //lets sync these over
    if (isFull) {
      fullProvision("gpf_groupOfNamesHlsLdap");
    } else {
      incrementalProvision("gpf_groupOfNamesHlsLdap");
    } 

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }


  public void testFullFlatHarvard() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("gpf_groupOfNamesHlsLdap")
          .assignProvisioningStrategy("harvardGroupOfNames")
        );

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("gpf_groupOfNamesHlsLdap");
    attributeValue.setTargetName("gpf_groupOfNamesHlsLdap");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("gpf_groupOfNamesHlsLdap");
    attributeValue2.setTargetName("gpf_groupOfNamesHlsLdap");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    LdapEntry ldapEntry = new LdapEntry("cn=override,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    ldapEntry = new LdapEntry("cn=override2,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override2"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    assertEquals(2, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(2, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetGroupsRetrieved")));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup2:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup2.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);
  
    grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup2:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup2.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
  
    grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }
  
  public void testFullFlatHarvardDontSelectAll() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("gpf_groupOfNamesHlsLdap")
          .assignProvisioningStrategy("harvardGroupOfNames")
          .addExtraConfig("selectAllGroups", "false")
          .addExtraConfig("selectAllEntities", "false")
        );

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("gpf_groupOfNamesHlsLdap");
    attributeValue.setTargetName("gpf_groupOfNamesHlsLdap");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("gpf_groupOfNamesHlsLdap");
    attributeValue2.setTargetName("gpf_groupOfNamesHlsLdap");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    LdapEntry ldapEntry = new LdapEntry("cn=override,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    ldapEntry = new LdapEntry("cn=override2,ou=Groups,dc=example,dc=edu");
    ldapEntry.addAttribute(new LdapAttribute("cn", "override2"));
    ldapEntry.addAttribute(new LdapAttribute("objectClass", "groupOfNames"));
    ldapEntry.addAttribute(new LdapAttribute("member", "cn=admin,dc=example,dc=edu"));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);

    assertEquals(2, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup2:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup2.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);
  
    grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2:Member,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2:Member", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals("ldap:///ou=People-qa,dc=law,dc=harvard,dc=edu??one?(isMemberOf=cn=testGroup2:Member,ou=Groups,dc=example,dc=edu)", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(testGroup2.getName()));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
  
    grouperProvisioningOutput = fullProvision("gpf_groupOfNamesHlsLdap"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
  }

  public void testIncrementalOnlyDnOverrideFlat() {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .addExtraConfig("groupSearchAllFilter", null)
          .addExtraConfig("insertGroups", null)
          .addExtraConfig("onlyLdapGroupDnOverride", "true")
          .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
          .assignDnOverrideConfig(true)
        );
            
    // initialize
    incrementalProvision();

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override,ou=Groups,dc=example,dc=edu");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("ldapProvTest");
    attributeValue2.setTargetName("ldapProvTest");
    attributeValue2.getMetadataNameValues().put("md_grouper_ldapGroupDnOverride", "cn=override2,ou=Groups,dc=example,dc=edu");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    // initialize
    incrementalProvision();

    GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);
  
    // initialize
    incrementalProvision();
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=override2,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=override2,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("override2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
    // initialize
    incrementalProvision();
    grouperProvisioningOutput = GrouperProvisioner.retrieveInternalLastProvisioner().retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
  }

  public void testFullUmassActiveDirectory() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("prodActiveDirectory")
          .assignProvisioningStrategy("umassActiveDirectory")
        );
  
    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("prodActiveDirectory");
    attributeValue.setTargetName("prodActiveDirectory");
    
    final GrouperProvisioningAttributeValue attributeValue2 = new GrouperProvisioningAttributeValue();
    attributeValue2.setDirectAssignment(true);
    attributeValue2.setDoProvision("prodActiveDirectory");
    attributeValue2.setTargetName("prodActiveDirectory");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue2, testGroup2);
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject kwhite = SubjectFinder.findById("kwhite", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    testGroup2.addMember(jsmith, false);
    testGroup2.addMember(banderson, false);
    testGroup2.setDescription("test description");
    testGroup2.store();
    
    LdapEntry ldapEntry = null;
  
    //assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision("prodActiveDirectory");
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetGroupsRetrieved"), 0));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.addMember(kwhite, false);
    testGroup2.addMember(kwhite, false);
  
    grouperProvisioningOutput = fullProvision("prodActiveDirectory"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.OBJECT_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=kwhite,ou=People,dc=example,dc=edu"));    
    
    // try delete
    testGroup.delete();
    testGroup2.delete();
    
  
    grouperProvisioningOutput = fullProvision("prodActiveDirectory"); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    try {
      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
      assertEquals(0, ldapEntries.size());
    } catch (RuntimeException e) {
      // this is just the thing not being found
    }
  }

  public void testColoradoSingleEntityAttributeFull() {
    coloradoSingleEntityAttribute(true);
  }

  public void testColoradoSingleEntityAttributeIncremental() {
    coloradoSingleEntityAttribute(false);
  }

  public void coloradoSingleEntityAttribute(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
          .assignConfigId("ADTESTExtensionAttribute15Provisioner")
          .assignProvisioningStrategy("coloradoSingleEntityAttribute")
        );
  
    // init stuff
    if (!isFull) {
      fullProvision("ADTESTExtensionAttribute15Provisioner");
      incrementalProvision("ADTESTExtensionAttribute15Provisioner");
    }
    
    //lets sync these over
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(description=SomeStaticValue)", new String[] {"uid"}, null).size());

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ADTESTExtensionAttribute15Provisioner");
    attributeValue.setTargetName("ADTESTExtensionAttribute15Provisioner");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
  
    Subject aanderson = SubjectFinder.findById("aanderson", true);
    Subject abrown = SubjectFinder.findById("abrown", true);
    Subject aclark = SubjectFinder.findById("aclark", true);
    
    testGroup.addMember(aanderson, false);
    testGroup.addMember(abrown, false);
    testGroup.setDescription("test description");
    testGroup.store();
    
    //assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision("ADTESTExtensionAttribute15Provisioner");
    } else {
      incrementalProvision("ADTESTExtensionAttribute15Provisioner");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetGroupsRetrieved"), 0));

    assertEquals(2, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(description=SomeStaticValue)", new String[] {"uid"}, null).size());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
      
      
    // try update
    testGroup.addMember(aclark, false);
  
    if (isFull) {
      fullProvision("ADTESTExtensionAttribute15Provisioner");
    } else {
      incrementalProvision("ADTESTExtensionAttribute15Provisioner");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(3, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(description=SomeStaticValue)", new String[] {"uid"}, null).size());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
    
    // try update
    testGroup.deleteMember(aclark, false);
  
    if (isFull) {
      fullProvision("ADTESTExtensionAttribute15Provisioner");
    } else {
      incrementalProvision("ADTESTExtensionAttribute15Provisioner");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(2, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(description=SomeStaticValue)", new String[] {"uid"}, null).size());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("SomeStaticValue", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());
    
    // try delete
    testGroup.delete();
    
  
    if (isFull) {
      fullProvision("ADTESTExtensionAttribute15Provisioner");
    } else {
      incrementalProvision("ADTESTExtensionAttribute15Provisioner");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(description=SomeStaticValue)", new String[] {"uid"}, null).size());
  }

  public void internet2groupOfNames(boolean isFull, boolean twoCaches, boolean twoSearchAttributes) {
      
    LdapProvisionerTestConfigInput ldapProvisionerTestConfigInput = new LdapProvisionerTestConfigInput()
      .assignConfigId("ldapGroupOfNames")
      .assignProvisioningStrategy("internet2groupOfNames")
      .addExtraConfig("logCommandsAlways", "true");

    ldapProvisionerTestConfigInput.addExtraConfig("groupIdOfUsersNotToProvision", "ref:excludeUsers");

    if (twoCaches) {
      ldapProvisionerTestConfigInput.addExtraConfig("groupAttributeValueCache1groupAttribute", "businessCategory");
      ldapProvisionerTestConfigInput.addExtraConfig("groupAttributeValueCache1has", "true");
      ldapProvisionerTestConfigInput.addExtraConfig("groupAttributeValueCache1source", "target");
      ldapProvisionerTestConfigInput.addExtraConfig("groupAttributeValueCache1type", "groupAttribute");

      ldapProvisionerTestConfigInput.addExtraConfig("entityAttributeValueCache1entityAttribute", "uid");
      ldapProvisionerTestConfigInput.addExtraConfig("entityAttributeValueCache1has", "true");
      ldapProvisionerTestConfigInput.addExtraConfig("entityAttributeValueCache1source", "target");
      ldapProvisionerTestConfigInput.addExtraConfig("entityAttributeValueCache1type", "entityAttribute");
    
    }

    if (twoSearchAttributes) {
      ldapProvisionerTestConfigInput.addExtraConfig("groupMatchingAttribute1name", "ldap_dn");
      ldapProvisionerTestConfigInput.addExtraConfig("groupMatchingAttributeCount", "2");

      ldapProvisionerTestConfigInput.addExtraConfig("entityMatchingAttribute1name", "ldap_dn");
      ldapProvisionerTestConfigInput.addExtraConfig("entityMatchingAttributeCount", "2");
      
    }
    LdapProvisionerTestUtils.configureLdapProvisioner(ldapProvisionerTestConfigInput);
  
    Group excludeGroup = new GroupSave().assignName("ref:excludeUsers").assignCreateParentStemsIfNotExist(true).save();
    Subject banderson = SubjectFinder.findById("banderson", true);
    excludeGroup.addMember(banderson);
    
    // init stuff
    if (!isFull) {
      fullProvision("ldapGroupOfNames");
      incrementalProvision("ldapGroupOfNames");
    }
    
    //lets sync these over
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"uid"}, null).size());

    new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapGroupOfNames");
    attributeValue.setTargetName("ldapGroupOfNames");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
  
    Subject aanderson = SubjectFinder.findById("aanderson", true);
    Subject abrown = SubjectFinder.findById("abrown", true);
    Subject aclark = SubjectFinder.findById("aclark", true);
    
    testGroup.addMember(aanderson, false);
    testGroup.addMember(abrown, false);

    //excluded user
    testGroup.addMember(banderson, false);
    
    testGroup.setDescription("test description");
    testGroup.store();
      
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));    
      
    // change the idIndex in the grouper sync group table
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapGroupOfNames");
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    gcGrouperSyncGroup.setGroupIdIndex(123L);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);
    
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu")); 
      
    // change id index
    testGroup = new GroupSave().assignName("test:testGroup").assignIdIndex(123L).save();
    
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu")); 

    // try update
    testGroup.addMember(aclark, false);
  
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));    
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aclark,ou=People,dc=example,dc=edu"));    
    
    // try update
    testGroup.deleteMember(aclark, false);
  
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());

    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));   
    
    // empty group
    testGroup.deleteMember(abrown, false);
    testGroup.deleteMember(aanderson, false);

    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());

    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));

    
    //redo empty group, should be no insert/delete
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());

    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));

    if (isFull) {
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().iterator().next().getGrouperTargetGroup().getInternal_objectChanges()));
    } else {
      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    }
    
    // add a member so we can remove later
    testGroup.addMember(abrown, false);
    
    // add a group to the group, and a non provisionable member, and see what happens, should be nothing
    testGroup.addMember(SubjectFinder.findRootSubject());
    Group testGroup2 = new GroupSave()
        .assignName("test2:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    testGroup.addMember(testGroup2.toSubject());

    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());

    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));


    testGroup.deleteMember(abrown, false);
    
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());

    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));

    
    // try delete
    testGroup.delete();
    
  
    if (isFull) {
      fullProvision("ldapGroupOfNames");
    } else {
      incrementalProvision("ldapGroupOfNames");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries));
  }

  public void testInternet2groupOfNamesTwoMatchesFull() {
    internet2groupOfNames(true, false, true);
  }

  public void testInternet2groupOfNamesTwoMatchesIncremental() {
    internet2groupOfNames(false, false, true);
  }

  public void testInternet2groupOfNamesTwoCachesFull() {
    internet2groupOfNames(true, true, false);
  }

  public void testInternet2groupOfNamesTwoCachesIncremental() {
    internet2groupOfNames(false, true, false);
  }

  public void testInternet2groupOfNamesTwoMatchesTwoCachesFull() {
    internet2groupOfNames(true, true, true);
  }

  public void testInternet2groupOfNamesTwoMatchesTwoCachesIncremental() {
    internet2groupOfNames(false, true, true);
  }
  
  public void testKorandaGroupOfNamesFull() {
    korandaGroupOfNames(true);
  }
  
  public void testKorandaGroupOfNamesIncremental() {
    korandaGroupOfNames(false);
  }
  
  public void korandaGroupOfNames(boolean isFull) {
   
    LdapProvisionerTestConfigInput ldapProvisionerTestConfigInput = new LdapProvisionerTestConfigInput()
        .assignConfigId("ldapGroupOfNames")
        .assignProvisioningStrategy("korandaGroupOfNames")
        .addExtraConfig("logCommandsAlways", "true");

      LdapProvisionerTestUtils.configureLdapProvisioner(ldapProvisionerTestConfigInput);
    
      // init stuff
      if (!isFull) {
        fullProvision("ldapGroupOfNames");
        incrementalProvision("ldapGroupOfNames");
      }
      
      //lets sync these over
      assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"uid"}, null).size());

      new StemSave(this.grouperSession).assignName("test").save();
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("ldapGroupOfNames");
      attributeValue.setTargetName("ldapGroupOfNames");
      
      Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
      
      // mark some groups to provision
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    
      Subject aanderson = SubjectFinder.findById("aanderson", true);
      Subject abrown = SubjectFinder.findById("abrown", true);
      Subject aclark = SubjectFinder.findById("aclark", true);
      
      testGroup.addMember(aanderson, false);
      testGroup.addMember(abrown, false);

      testGroup.setDescription("test description");
      testGroup.store();
        
      GrouperProvisioningOutput grouperProvisioningOutput = null;
      GrouperProvisioner grouperProvisioner = null;
      if (isFull) {
        fullProvision("ldapGroupOfNames");
        
        // change the idIndex in the grouper sync group table
        GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapGroupOfNames");
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
        gcGrouperSyncGroup.setInTargetInsertOrExists(false);
        gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);
        
        List<GcGrouperSyncMembership> membershipRetrieveAll = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
        for (GcGrouperSyncMembership gcGrouperSyncMembership : membershipRetrieveAll) {
          gcGrouperSyncMembership.setInTargetDb(null);
          gcGrouperSyncMembership.setInTargetInsertOrExistsDb(null);
        }
        gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStoreAll();
        
        fullProvision("ldapGroupOfNames");
        
        gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "ldapGroupOfNames");
        membershipRetrieveAll = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
        
        for (GcGrouperSyncMembership gcGrouperSyncMembership : membershipRetrieveAll) {
          assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
          assertEquals("F", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
        }
        
      } else {
        incrementalProvision("ldapGroupOfNames");
      }
      
      
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
      
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

      List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=testGroup)", new String[] {"objectClass", "cn", "member", "description"}, null);
      assertEquals(1, ldapEntries.size());
      assertEquals(2, ldapEntries.get(0).getAttribute("member").getStringValues().size());
      assertTrue(ldapEntries.get(0).getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));
      assertTrue(ldapEntries.get(0).getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
        
      testGroup.deleteMember(aanderson, false);
      
      
      if (isFull) {
        fullProvision("ldapGroupOfNames");
      } else {
        incrementalProvision("ldapGroupOfNames");
      }
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=testGroup)", new String[] {"objectClass", "cn", "member", "description"}, null);
      assertEquals(1, ldapEntries.size());
      assertEquals(1, ldapEntries.get(0).getAttribute("member").getStringValues().size());
      assertTrue(ldapEntries.get(0).getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));
      
      testGroup.deleteMember(abrown, false);
      
      
      if (isFull) {
        fullProvision("ldapGroupOfNames");
      } else {
        incrementalProvision("ldapGroupOfNames");
      }
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=testGroup)", new String[] {"objectClass", "cn", "member", "description"}, null);
      assertEquals(1, ldapEntries.size());
      assertEquals(1, ldapEntries.get(0).getAttribute("member").getStringValues().size());
      assertTrue(ldapEntries.get(0).getAttribute("member").getStringValues().contains(""));
      
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu")); 
        
      // change id index
//      testGroup = new GroupSave().assignName("test:testGroup").assignIdIndex(123L).save();
//      
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//      
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu")); 
//
//      // try update
//      testGroup.addMember(aclark, false);
//    
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//      
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 3, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));    
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aclark,ou=People,dc=example,dc=edu"));    
//      
//      // try update
//      testGroup.deleteMember(aclark, false);
//    
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 2, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
//      assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=aanderson,ou=People,dc=example,dc=edu"));
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));   
//      
//      // empty group
//      testGroup.deleteMember(abrown, false);
//      testGroup.deleteMember(aanderson, false);
//
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));
//
//      
//      //redo empty group, should be no insert/delete
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));
//
//      if (isFull) {
//        assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
//        assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().iterator().next().getGrouperTargetGroup().getInternal_objectChanges()));
//      } else {
//        assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
//      }
//      
//      // add a member so we can remove later
//      testGroup.addMember(abrown, false);
//      
//      // add a group to the group, and a non provisionable member, and see what happens, should be nothing
//      testGroup.addMember(SubjectFinder.findRootSubject());
//      Group testGroup2 = new GroupSave()
//          .assignName("test2:testGroup2").assignCreateParentStemsIfNotExist(true).save();
//      testGroup.addMember(testGroup2.toSubject());
//
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=abrown,ou=People,dc=example,dc=edu"));
//
//
//      testGroup.deleteMember(abrown, false);
//      
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(1, ldapEntries.size());
//
//      ldapEntry = ldapEntries.get(0);
//      
//      assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
//      assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
//      assertEquals(testGroup.getIdIndex()+"", ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
//      assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
//      assertEquals(GrouperUtil.toStringForLog(ldapEntry.getAttribute("member").getStringValues()), 1, ldapEntry.getAttribute("member").getStringValues().size());
//      assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin-seed-data,dc=example,dc=edu"));
//
//      
//      // try delete
//      testGroup.delete();
//      
//    
//      if (isFull) {
//        fullProvision("ldapGroupOfNames");
//      } else {
//        incrementalProvision("ldapGroupOfNames");
//      }
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
//      
//      ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(cn=test:testGroup)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
//      assertEquals(0, GrouperUtil.length(ldapEntries));

  }

  public void internet2memberOf(boolean isFull) {
      
    LdapProvisionerTestConfigInput ldapProvisionerTestConfigInput = new LdapProvisionerTestConfigInput()
      .assignConfigId("ldapIsMemberOf")
      .assignProvisioningStrategy("internet2memberOf")
      .addExtraConfig("logCommandsAlways", "true");
  
    LdapProvisionerTestUtils.configureLdapProvisioner(ldapProvisionerTestConfigInput);
  
    // init stuff
    if (!isFull) {
      fullProvision("ldapIsMemberOf");
      incrementalProvision("ldapIsMemberOf");
    }
        
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapIsMemberOf");
    attributeValue.setTargetName("ldapIsMemberOf");
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    // mark some groups to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    
    Group test2Group2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignCreateParentStemsIfNotExist(true).save();

    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapIsMemberOf");
    attributeValue.setTargetName("ldapIsMemberOf");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, test2Group2);
  
    Subject aanderson = SubjectFinder.findById("aanderson", true);
    Subject abrown = SubjectFinder.findById("abrown", true);
    Subject aclark = SubjectFinder.findById("aclark", true);
    
    testGroup.addMember(aanderson, false);
    testGroup.addMember(abrown, false);

    test2Group2.addMember(abrown, false);
    test2Group2.addMember(aclark, false);

    List<LdapEntry> ldapEntries = null;
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(2, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(1, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(1, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));

    testGroup.deleteMember(aanderson, false);

    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(2, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("test2:testGroup2", ldapEntries.get(0).getAttribute("description").getStringValues().iterator().next());

    
    // try update
    testGroup.addMember(aclark, false);
  
    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(2, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(2, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));
    
    // try update
    testGroup.deleteMember(aclark, false);
  
    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(2, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(1, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test2:testGroup2"));
  
    
    // try delete
    test2Group2.delete();
    
  
    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(1, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    assertTrue(ldapEntries.get(0).getAttribute("description").getStringValues().contains("test:testGroup"));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    
    // remove provisionable
    testGroup.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase());

    if (isFull) {
      fullProvision("ldapIsMemberOf");
    } else {
      incrementalProvision("ldapIsMemberOf");
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=abrown)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aanderson)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=aclark)", new String[] {"description"}, null);
    assertEquals(0, GrouperUtil.length(ldapEntries.get(0).getAttribute("description").getStringValues()));
    
  }
  
  public void testInternet2memberOfFull() {
    internet2memberOf(true);
  }

  public void testInternet2memberOfIncremental() {
    internet2memberOf(false);
  }

  private static void addPolicyType(Group group) {

    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "policy");

    attributeAssign.saveOrUpdate();
  }
}
