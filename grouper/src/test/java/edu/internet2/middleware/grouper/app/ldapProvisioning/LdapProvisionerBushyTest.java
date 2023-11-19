package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerBushyTest extends GrouperProvisioningBaseTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerBushyTest("testFullLdapBushy"));    
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

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
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
  
  public void testFullLdapBushy() {
      
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()   
        .assignGroupAttributeCount(6)
        .assignEntityAttributeCount(2)
        .assignGroupDnTypeBushy(true)
        .addExtraConfig("logCommandsAlways", "true")
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignUpdateGroupsAndDn(true));
    
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
  
    fullProvision();
    
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
    
    fullProvision();

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
  
    fullProvision();
  
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
    
    fullProvision();

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
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    fullProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
  
  public void testFullLdapBushyMoveGroupToAnotherFolderThatDoesNotExistInLdap() {
    testLdapBushyMoveGroupToAnotherFolderThatDoesNotExistInLdap(true);
  }
  
  public void testIncrementalLdapBushyMoveGroupToAnotherFolderThatDoesNotExistInLdap() {
    testLdapBushyMoveGroupToAnotherFolderThatDoesNotExistInLdap(false);
  }
  
  
  private void testLdapBushyMoveGroupToAnotherFolderThatDoesNotExistInLdap(boolean isFull) {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()   
        .assignGroupAttributeCount(6)
        .assignEntityAttributeCount(2)
        .assignGroupDnTypeBushy(true)
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignUpdateGroupsAndDn(true));
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    
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
  
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
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
    
    // move the group into another folder that doesn't exist in ldap and full provisioning should add
    // an ou for that folder
    
    Stem cStem = new StemSave(this.grouperSession)
        .assignName("testStemToMoveTo:a:b:c")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, cStem);
    
    testGroup.move(cStem);
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=c,ou=b,ou=a,ou=testStemToMoveTo,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=testGroup,ou=c,ou=b,ou=a,ou=testStemToMoveTo,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=testGroup,ou=c,ou=b,ou=a,ou=testStemToMoveTo,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
  }
  
  public void testIncrementalLdapBushy() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(new LdapProvisionerTestConfigInput()
        .assignGroupDnTypeBushy(true)
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        .assignGroupAttributeCount(6)
        .assignEntityAttributeCount(2)
        .assignGroupDnTranslate(false)
        .assignExplicitFilters(true)
        .addExtraConfig("logCommandsAlways", "true")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignUpdateGroupsAndDn(true));
    

    ConfigPropertiesCascadeBase.clearCache();

    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").assignCreateParentStemsIfNotExist(true).save();
    
    // init stuff
    incrementalProvision();
    
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
  
    incrementalProvision();
    
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
    
    incrementalProvision();

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
  
    incrementalProvision();
  
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
    
    incrementalProvision();

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
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
  
  public void testFullLdapBushyWithCNName() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDnTypeBushy(true)
        .assignTranslateFromGrouperProvisioningGroupField("name")
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true)
        .addExtraConfig("logCommandsAlways", "true")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        );

    ConfigPropertiesCascadeBase.clearCache();

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
  
    fullProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4:testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4:testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:test2:test3:test4:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4:testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4:testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:test2:test3:test4:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
  
    fullProvision();
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4b:testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4b:testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:test2:test3:test4b:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4b:testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4b:testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:test2:test3:test4b:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
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
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    fullProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    fullProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
  
  public void testIncrementalLdapBushyWithCNName() {
    
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDnTypeBushy(true)
        .assignTranslateFromGrouperProvisioningGroupField("name")
        .assignExplicitFilters(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignUpdateGroupsAndDn(true));
    
    ConfigPropertiesCascadeBase.clearCache();

    Stem testStem = new StemSave(this.grouperSession).assignName("test").save();
    Stem test4Stem = new StemSave(this.grouperSession).assignName("test:test2:test3:test4").assignCreateParentStemsIfNotExist(true).save();
    
    // init stuff
    incrementalProvision();
    
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
  
    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(2, ldapEntries.size());
    
    LdapEntry ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("test description"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4:testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4:testGroup,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:test2:test3:test4:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(test4Group.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("member").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("cn=admin,dc=example,dc=edu"));
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4:testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4:testGroup2,ou=test4,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:test2:test3:test4:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
  
    incrementalProvision();
  
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
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
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(4, ldapEntries.size());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:testGroup,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:testGroup2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4b:testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4b:testGroup,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:test2:test3:test4b:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
  
    ldapEntry = LdapSessionUtils.ldapSession().list("personLdap", "cn=test:test2:test3:test4b:testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=*)", new String[] {"cn"}, null).get(0);
    assertEquals("cn=test:test2:test3:test4b:testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals(1, ldapEntry.getAttribute("cn").getStringValues().size());
    assertEquals("test:test2:test3:test4b:testGroup2", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    
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
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(3, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(5, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), test4Group2.getName(), true).delete();
    
    incrementalProvision();
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup.getName(), true).delete();
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(2, ldapEntries.size());
    
    GroupFinder.findByName(GrouperSession.staticGrouperSession(), testGroup2.getName(), true).delete();
    
    incrementalProvision();

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass"}, null);
    assertEquals(0, ldapEntries.size());
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=organizationalUnit)", new String[] {"objectClass"}, null);
    assertEquals(1, ldapEntries.size());
  }
}
