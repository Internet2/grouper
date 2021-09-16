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

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.4.translateFromMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "attribute__description");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberToId2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.subjectSourcesToProvision", "personLdapSource");
    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.updateGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteGroups", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.insertMemberships", "true");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.groupDnType", "flat");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

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
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
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
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
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
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
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
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveMembership(targetDaoRetrieveMembershipRequest);
      assertNull(targetDaoRetrieveMembershipResponse.getTargetMembership());
    }
  }
}
