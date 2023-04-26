package edu.internet2.middleware.grouper.app.ldapProvisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerIncrementalTest extends GrouperProvisioningBaseTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerIncrementalTest("testDoNotAddIfNotExistFull"));    
  }
  
  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  public LdapProvisionerIncrementalTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerIncrementalTest(String name) {
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
    
    LdapProvisionerTestUtils.setupSubjectSource();
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
  
  public void testIncremental1() {

    long millisStart = System.currentTimeMillis();
    GrouperUtil.sleep(100);

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignExplicitFilters(true));
    

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

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
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

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);

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
    

    // send a message to sync group, before the previous group sync, it should be ignored
    ProvisioningMessage provisioningMessage = new ProvisioningMessage();
    provisioningMessage.setGroupIdsForSync(new String[] {testGroup.getId()});
    provisioningMessage.setBlocking(false);
    provisioningMessage.setMillisSince1970(millisStart);
    provisioningMessage.send("ldapProvTest");

    // let the message cache clear
    GrouperUtil.sleep(20000);

    incrementalProvision();

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    assertEquals(1, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("filterByGroupSyncGroups"), -1));
    
  }
  
  public void testIncremental2() {
  
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true));

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
    
    // not sure why we need to process again, but the attribute propagation was sending a message
    incrementalProvision();

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
  
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
    
    // try rename and add member together
    testGroup.addMember(bwilliams466);
    testGroup.setExtension("testGroupRenamed");
    testGroup.store();
    
    incrementalProvision();
  
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
  
    incrementalProvision();
  
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
  
    //GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    //incrementalProvision();
  
    //assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  }
  
  
  public void testIncrementalPolicyGroups() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true));

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.onlyProvisionPolicyGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.allowPolicyGroupOverride").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

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

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    addPolicyType(testGroup);
    
    incrementalProvision();
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    incrementalProvision();

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

    incrementalProvision();
    incrementalProvision();  // run again to get the message processed

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(0, ldapEntries.size());   
  }
  
  
  public void testIncrementalPolicyGroupsUsingFolder() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true));

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.onlyProvisionPolicyGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.allowPolicyGroupOverride").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

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

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    addPolicyType(stem);
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    incrementalProvision();
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    incrementalProvision();

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
    stem.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameBase());
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    incrementalProvision();
    incrementalProvision();  // run again to get the message processed

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(0, ldapEntries.size());   
  }
  
  public void testIncrementalRegexRestriction() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true));

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.provisionableRegex").value("groupExtension not matches ^.*_includes$|^.*_excludes$").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.ldapProvTest.allowProvisionableRegexOverride").value("true").store();

    ConfigPropertiesCascadeBase.clearCache();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.", "true");

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

    incrementalProvision();
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    
    incrementalProvision();
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    incrementalProvision();

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

    incrementalProvision();
    incrementalProvision();  // run again to get the message processed

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null);
    assertEquals(0, ldapEntries.size());   
  }
  
  public void testIncremental1recalc() {
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.recalculateAllOperations", "true");
    testIncremental1();
  }

  public void testIncremental2recalc() {
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.recalculateAllOperations", "true");
    testIncremental2();
  }

  public void testDoNotAddIfNotExistFull() {
    doNotAddIfNotExist(true);
  }

  public void testDoNotAddIfNotExistIncremental() {
    doNotAddIfNotExist(false);
  }

  public void doNotAddIfNotExist(boolean isFull) {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput().assignMembershipAttribute("description")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("onlyAddMembershipsIfUserExistsInTarget", "true")
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false")
        .assignGroupAttributeCount(5));
  
    // init stuff
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    Subject aclark = SubjectFinder.findById("aclark", true);
    Subject adoe = SubjectFinder.findById("adoe", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    Member memberJsmith = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), jsmith, true);
    Member memberBanderson = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), banderson, true);
    Member memberAclark = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), aclark, true);
    Member memberAdoe = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), adoe, true);

    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
    
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

    ProvisioningEntityWrapper provisioningEntityWrapperJsmith = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberJsmith.getId());

    assertEquals(true, provisioningEntityWrapperJsmith.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperJsmith.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperJsmith.getProvisioningStateEntity().isRecalcEntityMemberships());
    assertEquals(false, provisioningEntityWrapperJsmith.getProvisioningStateEntity().isCreate());

    ProvisioningEntityWrapper provisioningEntityWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberBanderson.getId());

    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcEntityMemberships());
    assertEquals(false, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isCreate());

    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

    ProvisioningMembershipWrapper provisioningMembershipWrapperJsmith = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberJsmith.getId()));

    ProvisioningMembershipWrapper provisioningMembershipWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberBanderson.getId()));
    
    assertEquals(true, provisioningMembershipWrapperJsmith.getProvisioningStateMembership().isRecalcObject());
    assertEquals(false, provisioningMembershipWrapperJsmith.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperJsmith.getProvisioningStateMembership().isSelectResultProcessed());

    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isRecalcObject());
    assertEquals(false, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelectResultProcessed());

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    LdapEntry ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    
      
    // try update
    testGroup.deleteMember(banderson);
  
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
    provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
    
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
    assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships());

    provisioningEntityWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberBanderson.getId());

    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcEntityMemberships());

    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

    provisioningMembershipWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberBanderson.getId()));
    
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelectResultProcessed());


    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));

    testGroup.addMember(banderson);

    LdapSessionUtils.ldapSession().delete("personLdap", "uid=banderson,ou=People,dc=example,dc=edu");

    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
    
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelect());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
    assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

    provisioningEntityWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberBanderson.getId());

    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isSelect());
    assertEquals(true, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperBanderson.getProvisioningStateEntity().isRecalcEntityMemberships());

    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

    provisioningMembershipWrapperBanderson = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberBanderson.getId()));
    
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperBanderson.getProvisioningStateMembership().isSelectResultProcessed());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));

    testGroup.addMember(aclark);
    testGroup.addMember(adoe);

    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

    provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
    
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
    assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

    ProvisioningEntityWrapper provisioningEntityWrapperAclark = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberAclark.getId());

    assertEquals(true, provisioningEntityWrapperAclark.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperAclark.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperAclark.getProvisioningStateEntity().isRecalcEntityMemberships());

    ProvisioningEntityWrapper provisioningEntityWrapperAdoe = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberAdoe.getId());

    assertEquals(true, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isRecalcEntityMemberships());

    ProvisioningMembershipWrapper provisioningMembershipWrapperAclark = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberAclark.getId()));

    ProvisioningMembershipWrapper provisioningMembershipWrapperAdoe = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberAdoe.getId()));
    
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isSelectResultProcessed());
    
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isSelectResultProcessed());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(3, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=aclark,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=adoe,ou=People,dc=example,dc=edu"));


    testGroup.deleteMember(aclark);
    testGroup.deleteMember(adoe);

    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
    
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
    assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

    provisioningEntityWrapperAclark = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberAclark.getId());

    assertEquals(true, provisioningEntityWrapperAclark.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperAclark.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperAclark.getProvisioningStateEntity().isRecalcEntityMemberships());
    assertEquals(false, provisioningEntityWrapperAclark.getProvisioningStateEntity().isCreate());

    provisioningEntityWrapperAdoe = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberAdoe.getId());

    assertEquals(true, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isRecalcObject());
    assertEquals(true, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isSelectResultProcessed());
    assertEquals(false, provisioningEntityWrapperAdoe.getProvisioningStateEntity().isRecalcEntityMemberships());

    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

    provisioningMembershipWrapperAclark = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberAclark.getId()));

    provisioningMembershipWrapperAdoe = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), memberAdoe.getId()));
    
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperAclark.getProvisioningStateMembership().isSelectResultProcessed());
    
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isRecalcObject());
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isSelect());
    assertEquals(true, provisioningMembershipWrapperAdoe.getProvisioningStateMembership().isSelectResultProcessed());

    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(1, ldapEntries.size());
    
    ldapEntry = ldapEntries.get(0);
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("businessCategory").getStringValues().iterator().next());
    assertEquals(2, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(1, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));



  }

  public void testReadonlyTestFull() {
    readonlyTest(true);
  }

  public void testReadonlyTestIncremental() {
    readonlyTest(false);
  }

  public void readonlyTest(boolean isFull) {
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput().assignMembershipAttribute("description")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("onlyAddMembershipsIfUserExistsInTarget", "true")
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false")
        .addExtraConfig("readOnly", "true")
        .assignGroupAttributeCount(5));
  
    // init stuff
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
  
    // mark some folders to provision
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
  
    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory"}, null).size());
  
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
  
  }

  private static void addPolicyType(Group group) {

    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "policy");

    attributeAssign.saveOrUpdate();
  }
  
  private static void addPolicyType(Stem stem) {

    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "policy");

    attributeAssign.saveOrUpdate();
  }
}
