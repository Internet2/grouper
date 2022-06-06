package edu.internet2.middleware.grouper.app.ldapProvisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
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
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerIncrementalTest extends GrouperTest {


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerIncrementalTest("testIncrementalPolicyGroupsUsingFolder"));    
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

    runJobs(true, true);
    
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

    runJobs(true, true);

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

    runJobs(true, true);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    assertEquals(1, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("filterByGroupSyncGroups"), -1));
    
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

  public void testIncremental2() {
  
  
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignExplicitFilters(true)
        .assignUpdateGroupsAndDn(true));

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
  
    runJobs(true, true);
    
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
    runJobs(true, true);

    // try update
    testGroup.deleteMember(banderson);
    testGroup.addMember(blopez);
    testGroup.addMember(hdavis);
  
    runJobs(true, true);
  
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
    
    runJobs(true, true);
  
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
  
    runJobs(true, true);
  
    assertEquals(1, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
  
    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroupsIfNotExistInGrouper", "true");
  
    //GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    //runJobs(true, true);
  
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

    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    addPolicyType(testGroup);
    
    runJobs(true, true);
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    runJobs(true, true);

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

    runJobs(true, true);
    runJobs(true, true);  // run again to get the message processed

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

    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    addPolicyType(stem);
    GrouperObjectTypesDaemonLogic.fullSyncLogic();

    runJobs(true, true);
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    runJobs(true, true);

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

    runJobs(true, true);
    runJobs(true, true);  // run again to get the message processed

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

    runJobs(true, true);
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory", "description"}, null);
    assertEquals(0, ldapEntries.size());
    
    testGroup.setExtension("testGroup");
    testGroup.store();
    
    runJobs(true, true);
    try { Thread.sleep(10000); } catch (Exception e) { }  // give some time for the message
    runJobs(true, true);

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

    runJobs(true, true);
    runJobs(true, true);  // run again to get the message processed

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
