package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsResponse;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class LdapProvisionerTargetDaoTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerTargetDaoTest("testRetrieveMembership"));
  }
  
  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  public LdapProvisionerTargetDaoTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisionerTargetDaoTest(String name) {
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
    super.tearDown();
    
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();   
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  public void testRetrieveMembership() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignUpdateGroupsAndDn(true)
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignExplicitFilters(true));

    Subject jsmith = SubjectFinder.findById("jsmith", true);
    Subject banderson = SubjectFinder.findById("banderson", true);
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(jsmith, false);
    testGroup.addMember(banderson, false);
    testGroup.store();
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

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
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("top"));
    assertTrue(ldapEntry.getAttribute("objectClass").getStringValues().contains("groupOfNames"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=jsmith,ou=People,dc=example,dc=edu"));
    assertTrue(ldapEntry.getAttribute("member").getStringValues().contains("uid=banderson,ou=People,dc=example,dc=edu"));    

    assertNull(grouperProvisioner.getProvisioningSyncResult().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll().get(0).getErrorMessage());

    // now test retrieveMembership
    {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.assignAttributeValue("ldap_dn", "cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetGroup.addAttributeValue("member", "uid=jsmith,ou=People,dc=example,dc=edu");
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest = new TargetDaoRetrieveMembershipRequest();
      targetDaoRetrieveMembershipRequest.setTargetGroup(targetGroup);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
      targetDaoRetrieveMembershipsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
          targetDaoRetrieveMembershipsRequest);

      assertNotNull(targetDaoRetrieveMembershipsResponse.getTargetGroups());
      assertNotNull(targetDaoRetrieveMembershipsResponse.getTargetGroups().get(0).retrieveAttributeValue("member"));
    }
    
    {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.assignAttributeValue("ldap_dn", "cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetGroup.addAttributeValue("member", "uid=banderson,ou=People,dc=example,dc=edu");
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
      targetDaoRetrieveMembershipsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
          targetDaoRetrieveMembershipsRequest);

      assertNotNull(targetDaoRetrieveMembershipsResponse.getTargetGroups());
      assertNotNull(targetDaoRetrieveMembershipsResponse.getTargetGroups().get(0).retrieveAttributeValue("member"));
    }
    
    {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.assignAttributeValue("ldap_dn", "cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetGroup.addAttributeValue("member", "uid=hdavis,ou=People,dc=example,dc=edu");
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
      targetDaoRetrieveMembershipsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
          targetDaoRetrieveMembershipsRequest);

      assertEquals(0, GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetGroups()));
    }
    
    {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.assignAttributeValue("ldap_dn", "cn=test:testGroup:does:not:exist,ou=Groups,dc=example,dc=edu");
      targetGroup.addAttributeValue("member", "uid=hdavis,ou=People,dc=example,dc=edu");
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
      targetDaoRetrieveMembershipsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
          targetDaoRetrieveMembershipsRequest);

      assertEquals(0, GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetGroups()));
    }
  }
}
