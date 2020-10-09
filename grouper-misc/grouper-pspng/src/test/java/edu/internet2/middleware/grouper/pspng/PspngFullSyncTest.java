package edu.internet2.middleware.grouper.pspng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

public class PspngFullSyncTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new PspngFullSyncTest("testFullSyncWithCacheBulk"));
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

    super.setUp();
    try {
      this.grouperSession = GrouperSession.startRootSession();  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
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
  public void testFullSyncWithCacheBulk() throws Exception {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngCacheGroupProvisionable", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("pspngNonScriptProvisionable", "true");

    
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
          
    List<Group> groups = new ArrayList<Group>();
    
    List<Subject> subjects = new ArrayList<Subject>();

    subjects.add(SubjectFinder.findByIdAndSource("aanderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("aanderson727", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("abrown", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("abrown643", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("aclark", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("adoe", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("agasper", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("agonazles", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ahenderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ahenderson594", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ajohnson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ajohnson871", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("alangenberg", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("alangenberg704", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("alangenberg855", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("alee", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("alewis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("amorrison", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("amorrison30", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("apeterson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("aprice", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("aroberts", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("asmith", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("asmith583", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("asmith765", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("avales", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("avales508", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("avales695", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("awhite", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("awhite728", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("awhite847", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("awilliams", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("banderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("banderson572", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("banderson971", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbrown", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbrown705", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbrown721", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbutler", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbutler437", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bbutler843", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bclark", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bclark226", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bclark446", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bclark730", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bclark968", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdavis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdavis369", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdavis480", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdavis570", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdoe", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdoe365", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdoe422", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bdoe672", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgasper", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgasper28", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgasper456", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgasper872", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgonazles", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgonazles345", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgonazles633", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgonazles994", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady136", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady203", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady415", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady505", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bgrady967", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bhenderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bjohnson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bjohnson177", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bjohnson513", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bjohnson992", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blangenberg", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blee", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blee483", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blewis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blewis390", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blewis553", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blewis798", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blewis840", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blopez", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blopez563", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("blopez966", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmartinez", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmartinez582", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmorrison", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmorrison491", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmorrison620", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bmorrison655", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bpeterson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bpeterson304", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bpeterson881", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bpeterson928", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bprice", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bprice170", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bprice574", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bprice745", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bprice903", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("broberts", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("broberts298", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("broberts750", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bscott", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bscott527", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bsmith", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bsmith471", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bsmith649", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bthompson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bthompson878", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bvales", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bvales414", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bvales580", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bvales722", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwalters", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwalters566", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwalters958", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwhite", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwhite551", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwhite914", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwilliams", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwilliams457", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("bwilliams466", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cbrown", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cdavis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cdavis900", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cdoe", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cdoe981", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cgasper", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("chenderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cjohnson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cjohnson758", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("clangenberg", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("clewis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("clewis800", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("clopez", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cmorrison", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cmorrison129", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cmorrison684", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cpeterson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cpeterson772", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cprice", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("csmith", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cthompson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("cwalters", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("danderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("danderson228", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("danderson523", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("danderson634", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("danderson96", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbrown", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbrown597", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbrown739", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbrown834", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbutler", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbutler347", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dbutler979", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dclark", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dclark671", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dclark720", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dclark839", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dclark888", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddavis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddavis141", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddavis27", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddavis822", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddavis919", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe431", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe577", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe605", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe638", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe688", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe814", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("ddoe895", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgasper", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgonazles", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgonazles682", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgonazles785", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgrady", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgrady229", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgrady427", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgrady76", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dgrady97", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dhenderson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dhenderson425", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dhenderson833", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dhenderson848", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dhenderson867", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("djohnson", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("djohnson606", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg121", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg358", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg397", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg509", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg61", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlangenberg934", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlee", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlewis", "personLdapSource", true));
    subjects.add(SubjectFinder.findByIdAndSource("dlewis327", "personLdapSource", true));    
    
    int secondLevelGroupCount = 5;
    
    int membershipCount = 0;

    int membershipAddCount = 0;

    int membershipNotProvisionableCount = 0;
    Subject subjectToRemove = null;
    
    for (char char0 = 'a'; char0<='e'; char0++) {
      for (char char1 = 'a'; char1<='e'; char1++) {
        for (char char2 = 'a'; char2<='a'; char2++) {
          //new StemSave(this.grouperSession).assignName(char0 + ":" + char1 + ":" + char2).save();
          Group group = new GroupSave(this.grouperSession).assignName(char0 + ":" + char1 + ":" + char2 + ":group").assignCreateParentStemsIfNotExist(true).save();
          groups.add(group);

          int numSubjects = (int)Math.round(Math.random() * 10);
          if (group.getName().equals("a:a:a:group") && numSubjects < 1) {
            // need at least one subject
            numSubjects = 1;
          }
          for (int i=0;i<numSubjects;i++) {
            int subjectIndex = (int)Math.floor(Math.random()*200);
            Subject subject = subjects.get(subjectIndex);
            
            if (i==0 && group.getName().equals("a:a:a:group")) {
              subjectToRemove = subject;
            }
            
            if (group.addMember(subject, false)) {
              // System.out.println("adding " + subjects.get(subjectIndex).getId() + " to group " + group.getName());
              if (group.getName().startsWith("a")
                  || group.getName().startsWith("b")
                  || group.getName().startsWith("c")
                  || group.getName().startsWith("d:a")
                  || group.getName().startsWith("d:b")
                  || group.getName().startsWith("d:c:a:group")
                  || group.getName().startsWith("d:d:a:group")
                  ) {
                if (!group.getName().startsWith("a:b")
                  && !group.getName().startsWith("a:c:a:group")
                  && !group.getName().startsWith("b:b")
                  && !group.getName().startsWith("b:c:a:group")
                  ) {
                    membershipCount++;
                }
              }
              if (group.getName().startsWith("e")
                  ) {
                if (!group.getName().startsWith("e:b")
                  ) {
                  //System.out.println("adding " + subjects.get(subjectIndex).getId() + " to group " + group.getName());
                  membershipAddCount++;
                }
              }
              if (group.getName().startsWith("c")
                  ) {
                membershipNotProvisionableCount++;
              }
            }
          }
        }
      }
    }
    
    Group group = null;
    
    // 5-2 groups
    Stem stem = StemFinder.findByName(grouperSession, "a", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    stem = StemFinder.findByName(grouperSession, "a:b", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");
    
    group = GroupFinder.findByName(grouperSession, "a:c:a:group", true);
    
    group.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");
    
    // 5-2 groups
    stem = StemFinder.findByName(grouperSession, "b", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    stem = StemFinder.findByName(grouperSession, "b:b", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");
    
    group = GroupFinder.findByName(grouperSession, "b:c:a:group", true);
    
    group.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");

    // 5 groups
    stem = StemFinder.findByName(grouperSession, "c", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");
    
    // 1 group
    stem = StemFinder.findByName(grouperSession, "d:a", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");
    
    // 1 group
    stem = StemFinder.findByName(grouperSession, "d:b", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    // 1 group
    group = GroupFinder.findByName(grouperSession, "d:c:a:group", true);
    
    group.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");
    
    // 1 group
    group = GroupFinder.findByName(grouperSession, "d:d:a:group", true);
    
    group.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    // 0 group
    group = GroupFinder.findByName(grouperSession, "d:e:a:group", true);
    
    group.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");

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

    assertEquals(membershipCount, hib3GrouploaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouploaderLog.getUpdateCount().intValue());
    assertEquals(0, hib3GrouploaderLog.getDeleteCount().intValue());
    assertEquals(membershipCount, hib3GrouploaderLog.getTotalCount().intValue());
    
    //fullSyncProvisioner
    Provisioner provisioner = fullSyncProvisioner.getProvisioner();
    
    Set<GrouperGroupInfo> grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner();
    Set<String> groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    int provisionedGroupCount = (secondLevelGroupCount-2) + (secondLevelGroupCount-2) + (secondLevelGroupCount) +4;
    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner2();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(provisionedGroupCount, ldapEntries.size());
    
    Map<String, LdapEntry> ldapEntryNameToEntry = new HashMap<String, LdapEntry>();
    
    for (LdapEntry ldapEntry1 : ldapEntries) {
      ldapEntryNameToEntry.put(ldapEntry1.getAttribute("cn").getStringValues().iterator().next(), ldapEntry1);
    }

    LdapEntry ldapEntry = ldapEntryNameToEntry.get("d:d:a:group");
    
    assertEquals("cn=d:d:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("d:d:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));

    ldapEntry = ldapEntryNameToEntry.get("a:a:a:group");
    
    assertEquals("cn=a:a:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("a:a:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    
    // run it again it should do nothing
    fullSyncProvisioner = FullSyncProvisionerFactory.getFullSyncer("pspng1");
    if ( fullSyncProvisioner == null ) {
      throw new Exception("No provisioner found for job: " + "pspng1");
    }

    hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("OTHER_JOB_pspng1_full");
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());

    stats = fullSyncProvisioner.startFullSyncOfAllGroupsAndWaitForCompletion(hib3GrouploaderLog);

    assertEquals(0, hib3GrouploaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouploaderLog.getUpdateCount().intValue());
    assertEquals(0, hib3GrouploaderLog.getDeleteCount().intValue());
    assertEquals(membershipCount, hib3GrouploaderLog.getTotalCount().intValue());
    
    //fullSyncProvisioner
    provisioner = fullSyncProvisioner.getProvisioner();
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    provisionedGroupCount = (secondLevelGroupCount-2) + (secondLevelGroupCount-2) + (secondLevelGroupCount) +4;
    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner2();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(provisionedGroupCount, ldapEntries.size());
    
    ldapEntryNameToEntry = new HashMap<String, LdapEntry>();
    
    for (LdapEntry ldapEntry1 : ldapEntries) {
      ldapEntryNameToEntry.put(ldapEntry1.getAttribute("cn").getStringValues().iterator().next(), ldapEntry1);
    }

    ldapEntry = ldapEntryNameToEntry.get("d:d:a:group");
    
    assertEquals("cn=d:d:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("d:d:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));

    ldapEntry = ldapEntryNameToEntry.get("a:a:a:group");
    
    assertEquals("cn=a:a:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("a:a:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));
    
    
    //lets make some changes...  make a group provisionable
    stem = StemFinder.findByName(grouperSession, "e", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:provision_to", "pspng1");

    stem = StemFinder.findByName(grouperSession, "e:b", true);
    
    stem.getAttributeValueDelegate().assignValue("etc:pspng:do_not_provision_to", "pspng1");

    //lets make a group not provisionable
    stem = StemFinder.findByName(grouperSession, "c", true);
    
    stem.getAttributeDelegate().removeAttributeByName("etc:pspng:provision_to");

    // add a member

    group = GroupFinder.findByName(grouperSession, "a:a:a:group", true);
    group.deleteMember(subjectToRemove);

    group = GroupFinder.findByName(grouperSession, "a:d:a:group", true);
    group.addMember(SubjectFinder.findByIdAndSource("msmith896", "personLdapSource", true), true);
    
    fullSyncProvisioner = FullSyncProvisionerFactory.getFullSyncer("pspng1");
    if ( fullSyncProvisioner == null ) {
      throw new Exception("No provisioner found for job: " + "pspng1");
    }

    hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("OTHER_JOB_pspng1_full");
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());

    stats = fullSyncProvisioner.startFullSyncOfAllGroupsAndWaitForCompletion(hib3GrouploaderLog);

    assertEquals(membershipAddCount + 1, hib3GrouploaderLog.getInsertCount().intValue());
    assertEquals(0, hib3GrouploaderLog.getUpdateCount().intValue());
    assertEquals(membershipNotProvisionableCount+1, hib3GrouploaderLog.getDeleteCount().intValue());
    assertEquals(membershipCount + membershipAddCount + 1 - (membershipNotProvisionableCount + 1), hib3GrouploaderLog.getTotalCount().intValue());
    
    //fullSyncProvisioner
    provisioner = fullSyncProvisioner.getProvisioner();
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    provisionedGroupCount = (secondLevelGroupCount-2) + (secondLevelGroupCount-2) +4 + (secondLevelGroupCount-1);
    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !groupNames.contains("c:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !groupNames.contains("e:b:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("e:a:a:group"));
    
    grouperGroupInfoSet = provisioner.getAllGroupsForProvisioner2();
    groupNames = new HashSet<String>();
    for (GrouperGroupInfo grouperGroupInfo : grouperGroupInfoSet) {
      groupNames.add(grouperGroupInfo.getGrouperGroup().getName());
    }

    assertEquals(GrouperUtil.toStringForLog(groupNames), provisionedGroupCount, groupNames.size());
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("d:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !groupNames.contains("c:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !groupNames.contains("e:b:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), groupNames.contains("e:a:a:group"));
    
    ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=posixGroup)", new String[] {"objectClass", "cn", "description", "gidNumber"}, null);
    assertEquals(provisionedGroupCount, ldapEntries.size());
    
    ldapEntryNameToEntry = new HashMap<String, LdapEntry>();
    
    for (LdapEntry ldapEntry1 : ldapEntries) {
      ldapEntryNameToEntry.put(ldapEntry1.getAttribute("cn").getStringValues().iterator().next(), ldapEntry1);
    }

    assertTrue(GrouperUtil.toStringForLog(groupNames), ldapEntryNameToEntry.containsKey("a:a:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), ldapEntryNameToEntry.containsKey("d:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !ldapEntryNameToEntry.containsKey("c:d:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), !ldapEntryNameToEntry.containsKey("e:b:a:group"));
    assertTrue(GrouperUtil.toStringForLog(groupNames), ldapEntryNameToEntry.containsKey("e:a:a:group"));
    
    ldapEntry = ldapEntryNameToEntry.get("d:d:a:group");
    
    assertEquals("cn=d:d:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("d:d:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));

    ldapEntry = ldapEntryNameToEntry.get("a:a:a:group");
    
    assertEquals("cn=a:a:a:group,ou=Groups,dc=example,dc=edu", ldapEntry.getDn());
    assertEquals("a:a:a:group", ldapEntry.getAttribute("cn").getStringValues().iterator().next());
    assertEquals(1, ldapEntry.getAttribute("objectClass").getStringValues().size());
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("posixGroup"));

  }
  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;
}
