package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipResponse;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * @author shilen
 */
public class LdapProvisionerTargetDaoTest extends GrouperTest {

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

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
    
    assertEquals(0, LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", new String[] {"objectClass", "cn", "member", "businessCategory"}, null).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
      ProvisioningGroup targetMembership = new ProvisioningGroup();
      targetMembership.setName("cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetMembership.addAttributeValue("member", "uid=jsmith,ou=People,dc=example,dc=edu");
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest = new TargetDaoRetrieveMembershipRequest();
      targetDaoRetrieveMembershipRequest.setTargetMembership(targetMembership);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
      assertNotNull(targetDaoRetrieveMembershipResponse.getTargetMembership());
    }
    
    {
      ProvisioningGroup targetMembership = new ProvisioningGroup();
      targetMembership.setName("cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetMembership.addAttributeValue("member", "uid=banderson,ou=People,dc=example,dc=edu");
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest = new TargetDaoRetrieveMembershipRequest();
      targetDaoRetrieveMembershipRequest.setTargetMembership(targetMembership);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
      assertNotNull(targetDaoRetrieveMembershipResponse.getTargetMembership());
    }
    
    {
      ProvisioningGroup targetMembership = new ProvisioningGroup();
      targetMembership.setName("cn=test:testGroup,ou=Groups,dc=example,dc=edu");
      targetMembership.addAttributeValue("member", "uid=hdavis,ou=People,dc=example,dc=edu");
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest = new TargetDaoRetrieveMembershipRequest();
      targetDaoRetrieveMembershipRequest.setTargetMembership(targetMembership);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
      assertNull(targetDaoRetrieveMembershipResponse.getTargetMembership());
    }
    
    {
      ProvisioningGroup targetMembership = new ProvisioningGroup();
      targetMembership.setName("cn=test:testGroup:does:not:exist,ou=Groups,dc=example,dc=edu");
      targetMembership.addAttributeValue("member", "uid=hdavis,ou=People,dc=example,dc=edu");
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest = new TargetDaoRetrieveMembershipRequest();
      targetDaoRetrieveMembershipRequest.setTargetMembership(targetMembership);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("ldapProvTest");
      grouperProvisioner.initialize(GrouperProvisioningType.incrementalProvisionChangeLog);
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
      assertNull(targetDaoRetrieveMembershipResponse.getTargetMembership());
    }
  }
}
