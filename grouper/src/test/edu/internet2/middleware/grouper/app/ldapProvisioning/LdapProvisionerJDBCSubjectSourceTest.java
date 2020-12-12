package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
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
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();    
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
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

  public void testIncrementalDoNotCreateUsers() {
  
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttributeCount", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpression", "${'cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGroupSyncField", "groupId");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpression", "${grouperProvisioningGroup.getName()}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpression", "'cn' + '=' + 'somethingbogussincethisisrequired'");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttributeCount", "2");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.insert", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.insert", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromMemberSyncField", "subjectIdentifier");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createMissingUsers", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteInTargetIfInTargetAndNotGrouper", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
  
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
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(true);
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
  
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttributeCount", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpression", "${'cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGroupSyncField", "groupId");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpression", "${grouperProvisioningGroup.getName()}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpression", "'cn' + '=' + 'somethingbogussincethisisrequired'");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttributeCount", "6");
  
    
    // TODO update to allow entity creations
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateExpressionCreateOnly", "${'uid=' + grouperProvisioningEntity.retrieveAttributeValueString('subjectIdentifier0') + ',ou=People,dc=example,dc=edu'}");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.required", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromMemberSyncField", "subjectIdentifier");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.name", "sn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.select", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.2.translateExpressionCreateOnly", "'something'");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.select", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.3.translateExpressionCreateOnly", "'something'");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.name", "givenName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.select", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.4.translateExpressionCreateOnly", "'something'");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.delete", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.select", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.5.translateExpressionCreateOnly", "${grouperUtil.toSet('top', 'organizationalPerson', 'person', 'inetOrgPerson', 'eduPerson')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "jdbc");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.createMissingUsers", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteInTargetIfInTargetAndNotGrouper", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");
  
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
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(true);
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
}
