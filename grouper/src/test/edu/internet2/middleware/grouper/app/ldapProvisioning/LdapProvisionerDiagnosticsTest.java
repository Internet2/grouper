package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerDiagnosticsTest extends GrouperTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerDiagnosticsTest("testEntityAndMembershipInsertAndDelete"));    
  }
  
  public LdapProvisionerDiagnosticsTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerDiagnosticsTest(String name) {
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
    
    setupLdap();
  }

  public static void setupLdap() {
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
  
  public void testGroupAndMembershipInsertAndDelete() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        .assignGroupDnTypeBushy(true)
        .assignEntityAttributeCount(6)
        .assignInsertEntityAndAttributes(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignSubjectSourcesToProvision("jdbc"));

    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
  
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "banderson", "person", "banderson");
    Subject banderson = SubjectFinder.findById("banderson", true);

    testGroup.addMember(testSubject0, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    {
      GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    }
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=test.subject.0,ou=People,dc=example,dc=edu"));
  
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
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    // delete group
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntries.get(0).getDn());

    // insert group
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
    
    // add member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=test.subject.0,ou=People,dc=example,dc=edu"));
    
    // add another member
    {
      testGroup.addMember(banderson);
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("banderson");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=test.subject.0,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));
    
    // delete member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("banderson");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=test.subject.0,ou=People,dc=example,dc=edu"));
    
    // delete another member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
    
    // select all groups
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
      assertTrue(grouperProvisioningDiagnosticsContainer.getReportFinal().contains("Selected 2 groups"));
    }
  }
  
  public void testGroupAndMembershipWithoutEntitiesInsertAndDelete() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .assignPosixGroup(true)
        .assignMembershipAttribute("description")
        .assignEntityAttributeCount(0)
        .assignSubjectSourcesToProvision("jdbc")
        .assignGroupDnTypeBushy(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        );
    
        
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
  
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "banderson", "person", "banderson");
    Subject banderson = SubjectFinder.findById("banderson", true);

    testGroup.addMember(testSubject0, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());
  
    {
      GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    }
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup2.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    
    // delete group
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("cn=testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntries.get(0).getDn());

    // insert group
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    
    // add member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    
    // add another member
    {
      testGroup.addMember(banderson);
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("banderson");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("banderson"));
    
    // delete member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("banderson");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test.subject.0"));
    
    // delete another member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).get(0);
    assertEquals("cn=testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("description").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    
    // select all groups
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
      assertTrue(grouperProvisioningDiagnosticsContainer.getReportFinal().contains("Selected 2 groups"));
    }
  }
  
 public void testEntityAndMembershipInsertAndDelete() {
    
   LdapProvisionerTestUtils.configureLdapProvisioner(
       new LdapProvisionerTestConfigInput()
         .assignMembershipStructureEntityAttributes(true)
         .assignGroupAttributeCount(1)
         .assignUpdateEntitiesAndDn(true)
         .assignInsertEntityAndAttributes(true)
         .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
         .assignEntityAttributeCount(7)
         .assignSubjectSourcesToProvision("jdbc")
         .addExtraConfig("targetGroupAttribute.0.translateExpression", "${'someprefix:' + grouperProvisioningGroup.name}")
       );          
    
    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testStem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
  
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    
    testGroup.addMember(testSubject0, false);
    testGroup.addMember(testSubject1, false);
    testGroup2.addMember(testSubject0, false);
      
    {
      GrouperProvisioningOutput grouperProvisioningOutput = GrouperProvisioner.retrieveProvisioner("ldapProvTest").provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    }
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup"));
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup2"));
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.1,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.1,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.1", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup"));

    // delete entity
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    assertEquals("uid=test.subject.1,ou=People,dc=example,dc=edu", ldapEntries.get(0).getDn());

    // insert entity
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());
 
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    // add member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());

    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup"));
    
    // add another member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityAttributesMembershipInsert(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup"));
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup2"));
    
    // delete member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertTrue(ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().contains("someprefix:test:testGroup"));
    
    // delete another member
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityAttributesMembershipDelete(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=eduPerson)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null);
    assertEquals(2, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "uid=test.subject.0,ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "uid", "givenName", "sn", "eduPersonEntitlement"}, null).get(0);
    assertEquals("uid=test.subject.0,ou=People,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(5, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("uid").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("givenName").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("sn").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals(0, ldapEntry.getAttribute("eduPersonEntitlement").getStringValues().size());
    assertEquals("test.subject.0", ldapEntry.getAttribute("uid").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("givenName").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("sn").getStringValues().iterator().next());
    assertEquals("something", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    // select all groups
    {
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(true);
      GrouperProvisioningOutput grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
      assertTrue(grouperProvisioningDiagnosticsContainer.getReportFinal().contains("Selected 2 entities"));
    }
  }
  
  private void validateNoErrors(GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    String[] lines = grouperProvisioningDiagnosticsContainer.getReportFinal().split("\n"); 
    List<String> errorLines = new ArrayList<String>();
    for (String line : lines) {
      if (line.contains("'red'") || line.contains("Error:")) {
        errorLines.add(line);
      }
    }
    
    if (errorLines.size() > 0) {
      fail("There are " + errorLines.size() + " errors in report: " + errorLines);
    }
  }
}
