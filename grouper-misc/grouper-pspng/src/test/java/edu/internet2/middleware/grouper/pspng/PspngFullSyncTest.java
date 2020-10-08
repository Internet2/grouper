package edu.internet2.middleware.grouper.pspng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class PspngFullSyncTest extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(new PspngFullSyncTest("testFullSync"));
  }

  public PspngFullSyncTest() {
    super();
  }

  public PspngFullSyncTest(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    
    try {
      this.grouperSession = GrouperSession.startRootSession();  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    
    // clear config cache
    GrouperCacheUtils.clearAllCaches();
    
    GrouperCheckConfig.checkObjects();

    Provisioner.checkAttributeDefinitions();
    
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

  public void testFullSyncWithCache() throws Exception {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngCacheGroupProvisionable", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngNonScriptProvisionable", "true");
    fullSyncHelper();
  }
  
  public void testFullSyncWithoutCache() throws Exception {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngCacheGroupProvisionable", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngNonScriptProvisionable", "false");
    fullSyncHelper();
  }
  
  public void fullSyncHelper() throws Exception {

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.type", LdapGroupProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.class", PspChangelogConsumerShim.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.quartzCron", "*/5 * * * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.ldapPoolName", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.memberAttributeName", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.memberAttributeValueFormat", "${subject.getId()}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.groupCreationBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.allGroupsSearchFilter", "objectclass=posixGroup");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.singleGroupSearchFilter", "(&(objectclass=posixGroup)(gidNumber=${idIndex}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.groupCreationLdifTemplate", 
        "dn: ${utils.escapeLdapRdn(\"cn=${group.name}\")}"
        + "||cn: ${group.name}||objectclass: posixGroup||gidNumber: ${group.idIndex}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.userSearchBaseDn", "dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.userSearchFilter", "uid=${subject.id}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.pspng1.grouperIsAuthoritative", "true");
            
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroupA = new GroupSave(this.grouperSession).assignName("test:testGroupA").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    Subject subject_aanderson = SubjectFinder.findById("aanderson", true);
    Subject subject_ajohnson = SubjectFinder.findById("ajohnson", true);
    Subject subject_adoe = SubjectFinder.findById("adoe", true);
    Subject subject_agasper = SubjectFinder.findById("agasper", true);
    Subject subject_alewis = SubjectFinder.findById("alewis", true);
    Subject subject_amorrison = SubjectFinder.findById("amorrison", true);
    
    testGroup.addMember(subject_aanderson, false);
    testGroup.addMember(subject_ajohnson, false);
    
    testGroupA.addMember(subject_alewis, false);
    testGroupA.addMember(subject_amorrison, false);
    
    testGroup2.addMember(subject_adoe, false);
    testGroup2.addMember(subject_agasper, false);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null).size());

    // Find the name of the provisioner
    FullSyncProvisioner fullSyncProvisioner = FullSyncProvisionerFactory.getFullSyncer("pspng1");
    if ( fullSyncProvisioner == null ) {
      throw new Exception("No provisioner found for job: " + "pspng1");
    }

    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("OTHER_JOB_pspng1_full");
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());

    JobStatistics stats = fullSyncProvisioner.startFullSyncOfAllGroupsAndWaitForCompletion(hib3GrouploaderLog);

    //fullSyncProvisioner
    Provisioner provisioner = fullSyncProvisioner.getProvisioner();
    
    Set<GrouperGroupInfo> grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner();
    Set<String> groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    assertEquals(2, groupNames.size());
    assertTrue(groupNames.contains("test:testGroup"));
    assertTrue(groupNames.contains("test:testGroupA"));
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner2();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    assertEquals(2, groupNames.size());
    assertTrue(groupNames.contains("test:testGroup"));
    assertTrue(groupNames.contains("test:testGroupA"));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(2, ldapEntries.size());
    
    Map<String, LdapEntry> ldapEntryNameToEntry = new HashMap<String, LdapEntry>();
    
    for (LdapEntry ldapEntry : ldapEntries) {
      ldapEntryNameToEntry.put(ldapEntry.getAttribute("cn").getStringValues().iterator().next(), ldapEntry);
    }

    LdapEntry ldapEntry = ldapEntryNameToEntry.get("test:testGroup");
    
    assertEquals("cn=test:testGroup,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroup", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroup.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(subject_aanderson.getId()));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(subject_ajohnson.getId()));

    ldapEntry = ldapEntryNameToEntry.get("test:testGroupA");
    
    assertEquals("cn=test:testGroupA,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("test:testGroupA", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(testGroupA.getIdIndex().toString(), ldapEntry.getAttribute("gidNumber").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertEquals(2, ldapEntry.getAttribute("description").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(subject_alewis.getId()));
    assertTrue(ldapEntry.getAttribute("description").getStringValues().contains(subject_amorrison.getId()));
  }
  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;
}
