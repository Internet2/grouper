package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDao;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

public class LdapProvisioningEntityAttributesTest extends GrouperProvisioningBaseTest {

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  public LdapProvisioningEntityAttributesTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public LdapProvisioningEntityAttributesTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapProvisioningEntityAttributesTest("testProvisioningDeleteNotExistInGrouperIncremental"));    
    //TestRunner.run(LdapProvisioningEntityAttributesTest.class);
  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  private static class EntityAttributeTestConfig {
    Boolean deleteMemberships;
    Boolean deleteMembershipsIfNotExistInGrouper;
    Boolean deleteValueIfManagedByGrouper;
    Boolean deleteMembershipsOnlyInTrackedGroups;
    Boolean deleteMembershipsIfGroupUnmarkedProvisionable;
    Boolean deleteMembershipsIfGrouperDeleted;
    Boolean deleteMembershipsIfGrouperCreated;
    
  }
  
  private static class UserResult {
    
    boolean valueNotInGrouper;
    boolean valueUnprovisionableGroup;
    boolean valueProvisionableGroupCreatedByGrouper;
    boolean valueProvisionableGroupExisting;
    boolean valueProvisionableGroupCreatedByGrouperThenDeleted;
    boolean valueProvisionableGroupMembershipExistedDeletedByGrouper;
    boolean valueProvisionableGroupMembershipNotInGrouper;
    boolean valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable;
    boolean valueProvisionableGroupExistedUnmarkedProvisionable;
    boolean valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper;
    boolean valueProvisionableGroupExistedGroupDeletedInGrouper;
    

    boolean valueProvisionableGroupRenamedOldNameCreatedByGrouper;
    boolean valueProvisionableGroupRenamedNewNameCreatedByGrouper;
    boolean valueProvisionableGroupRenamedOldNameExisted;
    boolean valueProvisionableGroupRenamedNewNameExisted;
    
    
    
  }
  
  private static class OverallResult {
    
    UserResult aclarkResult = new UserResult();
    UserResult adoeResult = new UserResult();
  }
  
  public void testProvisioningEntityAttributesDoNotDeleteFull() {
    provisioningEntityAttributesDoNotDeleteHelper(true);
  }
  
  //need to run individually
  public void testProvisioningEntityAttributesAssignErrorsToMemberships() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = false;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = null;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = null;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = null;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = null;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = null;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = null;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;
    
    LdapModificationItem item = null;
    List<LdapModificationItem> ldapModificationItems = null;

    for (String user : new String[] {"aclark", "adoe"}) {
      ldapModificationItems = new ArrayList<LdapModificationItem>();

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "valueNotInGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "unprovisionableGroup"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExisting"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupMembershipExistedDeletedByGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupMembershipNotInGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExistedUnmarkedProvisionable"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExistedGroupDeletedInGrouper"));
      ldapModificationItems.add(item);
      
      if (StringUtils.equals(user,  "adoe")) {
        item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupRenamedCreatedByGrouperOldName"));
        ldapModificationItems.add(item);
        
      }

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupRenamedExistedOldName"));
      ldapModificationItems.add(item);

      
      new LdapSyncDaoForLdap().modify("personLdap", "uid=" + user + ",ou=People,dc=example,dc=edu", ldapModificationItems);
      
    }

    // agasper starts with student2
    List<LdapEntry> ldapEntries = null;
    
    LdapProvisionerTestConfigInput ldapProvisionerTestConfigInput = new LdapProvisionerTestConfigInput()
      .assignConfigId("eduPersonEntitlement")
      .assignTranslateFromGrouperProvisioningGroupField("extension")
      .assignMembershipStructureEntityAttributes(true)
      .assignGroupAttributeCount(1)
      .assignEntityAttributeCount(3)
      .assignExplicitFilters(true)
      .assignEntitlementMetadata(true)
      .addExtraConfig("deleteMemberships", "false")
      .addExtraConfig("deleteMembershipsIfNotExistInGrouper", "false")
      .addExtraConfig("customizeMembershipCrud", "true");
//      .addExtraConfig("groupAttributeValueCache1has", "true")
//      .addExtraConfig("groupAttributeValueCache1source", "target")
//      .addExtraConfig("groupAttributeValueCache1type", "groupAttribute")
//      .addExtraConfig("groupAttributeValueCache1groupAttribute", "entitlement");
    
    LdapProvisionerTestUtils.configureLdapProvisioner(ldapProvisionerTestConfigInput);
     
     // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.logAllObjectsVerbose", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
  
    Stem prov = new StemSave(this.grouperSession).assignName("prov").save();
    Stem unprov = new StemSave(this.grouperSession).assignName("unprov").save();
    Stem provThenUnprov = new StemSave(this.grouperSession).assignName("provThenUnprov").save();
    
    Group provisionableGroupCreatedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouper").save();
    Group provisionableGroupExisting = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupExisting").save();
    Group unprovisionableGroup = new GroupSave(this.grouperSession).assignName("unprov:unprovisionableGroup").save();
    Group provisionableGroupCreatedByGrouperThenDeleted = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouperThenDeleted").save();
    Group provisionableGroupMembershipExistedDeletedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupMembershipExistedDeletedByGrouper").save();
    Group provisionableGroupMembershipNotInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupMembershipNotInGrouper").save();
    Group provisionableGroupCreatedByGrouperUnmarkedProvisionable = new GroupSave(this.grouperSession).assignName("provThenUnprov:provisionableGroupCreatedByGrouperUnmarkedProvisionable").save();
    Group provisionableGroupExistedUnmarkedProvisionable = new GroupSave(this.grouperSession).assignName("provThenUnprov:provisionableGroupExistedUnmarkedProvisionable").save();
    Group provisionableGroupCreatedByGrouperGroupDeletedInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouperGroupDeletedInGrouper").save();
    Group provisionableGroupExistedGroupDeletedInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupExistedGroupDeletedInGrouper").save();
    Group provisionableGroupRenamedCreatedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupRenamedCreatedByGrouperOldName").save();
    Group provisionableGroupRenamedExisted = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupRenamedExistedOldName").save();
    
    // mark some folders to provision
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, prov);
    
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, provThenUnprov);
    
    
    Subject aclark = SubjectFinder.findById("aclark", true);
    Subject adoe = SubjectFinder.findById("adoe", true);
  
    unprovisionableGroup.addMember(aclark);
    unprovisionableGroup.addMember(adoe);
  
    provisionableGroupExisting.addMember(aclark);
    provisionableGroupExistedGroupDeletedInGrouper.addMember(aclark);

    provisionableGroupCreatedByGrouper.addMember(aclark);

    provisionableGroupCreatedByGrouperThenDeleted.addMember(aclark);
    
    provisionableGroupMembershipExistedDeletedByGrouper.addMember(aclark);
    
    provisionableGroupCreatedByGrouperUnmarkedProvisionable.addMember(aclark);
    
    provisionableGroupExistedUnmarkedProvisionable.addMember(aclark);
    
    provisionableGroupCreatedByGrouperGroupDeletedInGrouper.addMember(aclark);
 
    provisionableGroupRenamedCreatedByGrouper.addMember(aclark);

    provisionableGroupRenamedExisted.addMember(aclark);

    for (int i=0;i<2;i++) {
      fullProvision("eduPersonEntitlement");
    
      Set<String> aclarkValues = retrieveLdapAttributes("aclark");
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("valueNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("unprovisionableGroup"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExisting"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupMembershipNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedExistedOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedExistedNewName"));
  
      Set<String> adoeValues = retrieveLdapAttributes("adoe");
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("valueNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("unprovisionableGroup"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExisting"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupMembershipNotInGrouper"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedExistedOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedExistedNewName"));

    }
    
    
    if (entityAttributeTestConfig.deleteMemberships != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMemberships").value(entityAttributeTestConfig.deleteMemberships ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfNotExistInGrouper").value(entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteValueIfManagedByGrouper != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteValueIfManagedByGrouper").value(entityAttributeTestConfig.deleteValueIfManagedByGrouper ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsOnlyInTrackedGroups").value(entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGroupUnmarkedProvisionable").value(entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperDeleted").value(entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGrouperCreated != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperCreated").value(entityAttributeTestConfig.deleteMembershipsIfGrouperCreated ? "true" : "false").store();
    }
    
    provisionableGroupCreatedByGrouperThenDeleted.deleteMember(aclark);
    provisionableGroupMembershipExistedDeletedByGrouper.deleteMember(aclark);
    
    GrouperProvisioningService.deleteAttributeAssign(provThenUnprov, "eduPersonEntitlement");
    
    provisionableGroupCreatedByGrouperGroupDeletedInGrouper.delete();
    provisionableGroupExistedGroupDeletedInGrouper.delete();

    int i=0;
    for (i=0;i<2;i++) {
      fullProvision("eduPersonEntitlement");
  
      checkResult(i, overallResult, false);
    }

    new GroupSave().assignGroupNameToEdit(provisionableGroupRenamedCreatedByGrouper.getName()).assignName("prov:provisionableGroupRenamedCreatedByGrouperNewName").save();
    new GroupSave().assignGroupNameToEdit(provisionableGroupRenamedExisted.getName()).assignName("prov:provisionableGroupRenamedExistedNewName").save();
    
    fullProvision("eduPersonEntitlement");

    checkResult(i, overallResult, true);
    
    
    incrementalProvision("eduPersonEntitlement"); // It should do nothing
    
    Group anotherProvisionableGroup = new GroupSave(this.grouperSession).assignName("prov:anotherProvisionableGroup").save();
    aclark = SubjectFinder.findById("aclark", true);
    adoe = SubjectFinder.findById("adoe", true);
  
    anotherProvisionableGroup.addMember(aclark);
    anotherProvisionableGroup.addMember(adoe);
    
    int membershipSyncErrorCount = new GcDbAccess().sql("select count(1) from grouper_sync_membership where error_code is not null").select(int.class);
    assertEquals(0, membershipSyncErrorCount);
    
    try {
      LdapSyncDao.testingThroughErrors = true;
      incrementalProvision("eduPersonEntitlement", true, true, true); // It should throw an error now and that should populate the grouper_sync_membership table's error column correctly
      
      membershipSyncErrorCount = new GcDbAccess().sql("select count(1) from grouper_sync_membership where error_code is not null").select(int.class);
      assertEquals(2, membershipSyncErrorCount);
    } finally {
      LdapSyncDao.testingThroughErrors = false;
    }
    
    incrementalProvision("eduPersonEntitlement");
    
    membershipSyncErrorCount = new GcDbAccess().sql("select count(1) from grouper_sync_membership where error_code is not null").select(int.class);
    assertEquals(0, membershipSyncErrorCount);
    
  }

  public void provisioningEntityAttributesDoNotDeleteHelper(boolean isFull) {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = false;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = null;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = null;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = null;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = null;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = null;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = null;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;


    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    provisioningEntityAttributesHelper(isFull, entityAttributeTestConfig, overallResult); 
  }
  
  public void testProvisioningEntityAttributesDoNotDeleteIncremental() {
    provisioningEntityAttributesDoNotDeleteHelper(false);
  }

  
  /**
   * simple provisioning of subject ids to ldap group
   */
  public void provisioningEntityAttributesHelper(boolean isFull, EntityAttributeTestConfig entityAttributeTestConfig, OverallResult overallResult) {
    
    LdapModificationItem item = null;
    List<LdapModificationItem> ldapModificationItems = null;

    for (String user : new String[] {"aclark", "adoe"}) {
      ldapModificationItems = new ArrayList<LdapModificationItem>();

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "valueNotInGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "unprovisionableGroup"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExisting"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupMembershipExistedDeletedByGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupMembershipNotInGrouper"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExistedUnmarkedProvisionable"));
      ldapModificationItems.add(item);
      
      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupExistedGroupDeletedInGrouper"));
      ldapModificationItems.add(item);
      
      if (StringUtils.equals(user,  "adoe")) {
        item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupRenamedCreatedByGrouperOldName"));
        ldapModificationItems.add(item);
        
      }

      item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonEntitlement", "provisionableGroupRenamedExistedOldName"));
      ldapModificationItems.add(item);

      
      new LdapSyncDaoForLdap().modify("personLdap", "uid=" + user + ",ou=People,dc=example,dc=edu", ldapModificationItems);
      
    }

    // agasper starts with student2
    List<LdapEntry> ldapEntries = null;
    
    LdapProvisionerTestConfigInput ldapProvisionerTestConfigInput = new LdapProvisionerTestConfigInput()
      .assignConfigId("eduPersonEntitlement")
      .assignTranslateFromGrouperProvisioningGroupField("extension")
      .assignMembershipStructureEntityAttributes(true)
      .assignGroupAttributeCount(1)
      .assignEntityAttributeCount(3)
      .assignExplicitFilters(true)
      .assignEntitlementMetadata(true)
      .addExtraConfig("deleteMemberships", "false")
      .addExtraConfig("deleteMembershipsIfNotExistInGrouper", "false")
      .addExtraConfig("customizeMembershipCrud", "true");
//      .addExtraConfig("groupAttributeValueCache1has", "true")
//      .addExtraConfig("groupAttributeValueCache1source", "target")
//      .addExtraConfig("groupAttributeValueCache1type", "groupAttribute")
//      .addExtraConfig("groupAttributeValueCache1groupAttribute", "entitlement");
    
    LdapProvisionerTestUtils.configureLdapProvisioner(ldapProvisionerTestConfigInput);
     
     // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.eduPersonEntitlement.logAllObjectsVerbose", "true");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
  
    Stem prov = new StemSave(this.grouperSession).assignName("prov").save();
    Stem unprov = new StemSave(this.grouperSession).assignName("unprov").save();
    Stem provThenUnprov = new StemSave(this.grouperSession).assignName("provThenUnprov").save();
    
    Group provisionableGroupCreatedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouper").save();
    Group provisionableGroupExisting = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupExisting").save();
    Group unprovisionableGroup = new GroupSave(this.grouperSession).assignName("unprov:unprovisionableGroup").save();
    Group provisionableGroupCreatedByGrouperThenDeleted = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouperThenDeleted").save();
    Group provisionableGroupMembershipExistedDeletedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupMembershipExistedDeletedByGrouper").save();
    Group provisionableGroupMembershipNotInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupMembershipNotInGrouper").save();
    Group provisionableGroupCreatedByGrouperUnmarkedProvisionable = new GroupSave(this.grouperSession).assignName("provThenUnprov:provisionableGroupCreatedByGrouperUnmarkedProvisionable").save();
    Group provisionableGroupExistedUnmarkedProvisionable = new GroupSave(this.grouperSession).assignName("provThenUnprov:provisionableGroupExistedUnmarkedProvisionable").save();
    Group provisionableGroupCreatedByGrouperGroupDeletedInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupCreatedByGrouperGroupDeletedInGrouper").save();
    Group provisionableGroupExistedGroupDeletedInGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupExistedGroupDeletedInGrouper").save();
    Group provisionableGroupRenamedCreatedByGrouper = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupRenamedCreatedByGrouperOldName").save();
    Group provisionableGroupRenamedExisted = new GroupSave(this.grouperSession).assignName("prov:provisionableGroupRenamedExistedOldName").save();
    
    // mark some folders to provision
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, prov);
    
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, provThenUnprov);
    
    
    Subject aclark = SubjectFinder.findById("aclark", true);
    Subject adoe = SubjectFinder.findById("adoe", true);
  
    unprovisionableGroup.addMember(aclark);
    unprovisionableGroup.addMember(adoe);
  
    provisionableGroupExisting.addMember(aclark);
    provisionableGroupExistedGroupDeletedInGrouper.addMember(aclark);

    provisionableGroupCreatedByGrouper.addMember(aclark);

    provisionableGroupCreatedByGrouperThenDeleted.addMember(aclark);
    
    provisionableGroupMembershipExistedDeletedByGrouper.addMember(aclark);
    
    provisionableGroupCreatedByGrouperUnmarkedProvisionable.addMember(aclark);
    
    provisionableGroupExistedUnmarkedProvisionable.addMember(aclark);
    
    provisionableGroupCreatedByGrouperGroupDeletedInGrouper.addMember(aclark);
 
    provisionableGroupRenamedCreatedByGrouper.addMember(aclark);

    provisionableGroupRenamedExisted.addMember(aclark);

    for (int i=0;i<2;i++) {
      if (isFull || i ==0) {
        fullProvision("eduPersonEntitlement");
      }
      if (!isFull) {
        incrementalProvision("eduPersonEntitlement");
      }
    
      Set<String> aclarkValues = retrieveLdapAttributes("aclark");
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("valueNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("unprovisionableGroup"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExisting"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupMembershipNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedExistedOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), aclarkValues.contains("provisionableGroupRenamedExistedNewName"));
  
      Set<String> adoeValues = retrieveLdapAttributes("adoe");
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("valueNotInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("unprovisionableGroup"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExisting"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupMembershipNotInGrouper"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
      assertFalse(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(adoeValues), adoeValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertTrue(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedExistedOldName"));
      assertFalse(GrouperUtil.toStringForLog(aclarkValues), adoeValues.contains("provisionableGroupRenamedExistedNewName"));

    }
    
    
    if (entityAttributeTestConfig.deleteMemberships != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMemberships").value(entityAttributeTestConfig.deleteMemberships ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfNotExistInGrouper").value(entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteValueIfManagedByGrouper != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteValueIfManagedByGrouper").value(entityAttributeTestConfig.deleteValueIfManagedByGrouper ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsOnlyInTrackedGroups").value(entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGroupUnmarkedProvisionable").value(entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperDeleted").value(entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted ? "true" : "false").store();
    }
    if (entityAttributeTestConfig.deleteMembershipsIfGrouperCreated != null) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.deleteMembershipsIfGrouperCreated").value(entityAttributeTestConfig.deleteMembershipsIfGrouperCreated ? "true" : "false").store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.eduPersonEntitlement.scoreConvertToFullSyncThreshold").value("200").store();
    
    provisionableGroupCreatedByGrouperThenDeleted.deleteMember(aclark);
    provisionableGroupMembershipExistedDeletedByGrouper.deleteMember(aclark);
    
    GrouperProvisioningService.deleteAttributeAssign(provThenUnprov, "eduPersonEntitlement");
    
    provisionableGroupCreatedByGrouperGroupDeletedInGrouper.delete();
    provisionableGroupExistedGroupDeletedInGrouper.delete();

    int i=0;
    for (i=0;i<2;i++) {
      if (isFull) {
        fullProvision("eduPersonEntitlement");
      } else {
        incrementalProvision("eduPersonEntitlement");
      }
  
      checkResult(i, overallResult, false);
    }

    new GroupSave().assignGroupNameToEdit(provisionableGroupRenamedCreatedByGrouper.getName()).assignName("prov:provisionableGroupRenamedCreatedByGrouperNewName").save();
    new GroupSave().assignGroupNameToEdit(provisionableGroupRenamedExisted.getName()).assignName("prov:provisionableGroupRenamedExistedNewName").save();
    
    if (isFull) {
      fullProvision("eduPersonEntitlement");
    } else {
      incrementalProvision("eduPersonEntitlement");
    }

    checkResult(i, overallResult, true);

    
  }

  private static void checkResult(int i, OverallResult overallResult, boolean postRename) {
    Set<String> aclarkValues = retrieveLdapAttributes("aclark");
    String aclarkValuesForLog = GrouperUtil.toStringForLog(aclarkValues);
    assertEquals(i + ": aclark valueNotInGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueNotInGrouper, aclarkValues.contains("valueNotInGrouper"));
    assertEquals(i + ": aclark unprovisionableGroup: " + aclarkValuesForLog, overallResult.aclarkResult.valueUnprovisionableGroup, aclarkValues.contains("unprovisionableGroup"));
    assertEquals(i + ": aclark provisionableGroupCreatedByGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper, aclarkValues.contains("provisionableGroupCreatedByGrouper"));
    assertEquals(i + ": aclark provisionableGroupExisting: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupExisting, aclarkValues.contains("provisionableGroupExisting"));
    assertEquals(i + ": aclark provisionableGroupCreatedByGrouperThenDeleted: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted, aclarkValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
    assertEquals(i + ": aclark provisionableGroupMembershipExistedDeletedByGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper, aclarkValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
    assertEquals(i + ": aclark provisionableGroupMembershipNotInGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper, aclarkValues.contains("provisionableGroupMembershipNotInGrouper"));
    assertEquals(i + ": aclark provisionableGroupCreatedByGrouperUnmarkedProvisionable: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable, aclarkValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
    assertEquals(i + ": aclark provisionableGroupExistedUnmarkedProvisionable: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable, aclarkValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
    assertEquals(i + ": aclark provisionableGroupCreatedByGrouperGroupDeletedInGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper, aclarkValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
    assertEquals(i + ": aclark provisionableGroupExistedGroupDeletedInGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper, aclarkValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));

    if (postRename) {

      assertEquals(i + ": aclark provisionableGroupRenamedOldNameCreatedByGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper, aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertEquals(i + ": aclark provisionableGroupRenamedNewNameCreatedByGrouper: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper, aclarkValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertEquals(i + ": aclark provisionableGroupRenamedOldNameExisted: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted, aclarkValues.contains("provisionableGroupRenamedExistedOldName"));
      assertEquals(i + ": aclark provisionableGroupRenamedNewNameExisted: " + aclarkValuesForLog, overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted, aclarkValues.contains("provisionableGroupRenamedExistedNewName"));
      
    }
    
    Set<String> adoeValues = retrieveLdapAttributes("adoe");
    String adoeValuesForLog = GrouperUtil.toStringForLog(adoeValues);
    assertEquals(i + ": adoe valueNotInGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueNotInGrouper, adoeValues.contains("valueNotInGrouper"));
    assertEquals(i + ": adoe unprovisionableGroup: " + adoeValuesForLog, overallResult.adoeResult.valueUnprovisionableGroup, adoeValues.contains("unprovisionableGroup"));
    assertEquals(i + ": adoe provisionableGroupCreatedByGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper, adoeValues.contains("provisionableGroupCreatedByGrouper"));
    assertEquals(i + ": adoe provisionableGroupExisting: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupExisting, adoeValues.contains("provisionableGroupExisting"));
    assertEquals(i + ": adoe provisionableGroupCreatedByGrouperThenDeleted: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted, adoeValues.contains("provisionableGroupCreatedByGrouperThenDeleted"));
    assertEquals(i + ": adoe provisionableGroupMembershipExistedDeletedByGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper, adoeValues.contains("provisionableGroupMembershipExistedDeletedByGrouper"));
    assertEquals(i + ": adoe provisionableGroupMembershipNotInGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper, adoeValues.contains("provisionableGroupMembershipNotInGrouper"));
    assertEquals(i + ": adoe provisionableGroupCreatedByGrouperUnmarkedProvisionable: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable, adoeValues.contains("provisionableGroupCreatedByGrouperUnmarkedProvisionable"));
    assertEquals(i + ": adoe provisionableGroupExistedUnmarkedProvisionable: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable, adoeValues.contains("provisionableGroupExistedUnmarkedProvisionable"));
    assertEquals(i + ": adoe provisionableGroupCreatedByGrouperGroupDeletedInGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper, adoeValues.contains("provisionableGroupCreatedByGrouperGroupDeletedInGrouper"));
    assertEquals(i + ": adoe provisionableGroupExistedGroupDeletedInGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper, adoeValues.contains("provisionableGroupExistedGroupDeletedInGrouper"));

    if (postRename) {
      
      assertEquals(i + ": adoe provisionableGroupRenamedOldNameCreatedByGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper, adoeValues.contains("provisionableGroupRenamedCreatedByGrouperOldName"));
      assertEquals(i + ": adoe provisionableGroupRenamedNewNameCreatedByGrouper: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper, adoeValues.contains("provisionableGroupRenamedCreatedByGrouperNewName"));
      assertEquals(i + ": adoe provisionableGroupRenamedOldNameExisted: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted, adoeValues.contains("provisionableGroupRenamedExistedOldName"));
      assertEquals(i + ": adoe provisionableGroupRenamedNewNameExisted: " + adoeValuesForLog, overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted, adoeValues.contains("provisionableGroupRenamedExistedNewName"));
      
    }
  
  }

  public static Set<String> retrieveLdapAttributes(String user) {
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list("personLdap", "ou=People,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(uid=" + user + ")", new String[] {"eduPersonEntitlement"}, null);
    assertEquals(1, ldapEntries.size());
    LdapEntry ldapEntry = null;
    
    ldapEntry = ldapEntries.get(0);

    LdapAttribute attribute = ldapEntry.getAttribute("eduPersonEntitlement");
    if (attribute == null) {
      return new HashSet<String>();
    }
    Set<String> result = new HashSet<String>();
    for (String value : attribute.getStringValues()) {
      GrouperUtil.assertion(!result.contains(value), "Why is attribute there? '" + value + "', " + GrouperUtil.toStringForLog(result));
      result.add(value);
    }
    return result;
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
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
  
    LdapProvisionerTestUtils.setupSubjectSource();
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);
  
    super.tearDown();
  }

  public void testProvisioningDeleteNotExistInGrouperFull() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = true;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = false;
    overallResult.aclarkResult.valueUnprovisionableGroup = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    overallResult.adoeResult.valueNotInGrouper = false;
    overallResult.adoeResult.valueUnprovisionableGroup = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    provisioningEntityAttributesHelper(true, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeleteNotExistInGrouperIncremental() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = true;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;
    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

   
    // provisionableGroupCreatedByGrouperUnmarkedProvisionable aclark expected:<false> but was:<true>
    provisioningEntityAttributesHelper(false, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeleteManagedValueInGrouperFull() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = true;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = true;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;
    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;
    
    provisioningEntityAttributesHelper(true, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeletedByGrouperInGrouperFull() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(true, entityAttributeTestConfig, overallResult); 
  }


  public void testProvisioningDeleteManagedValueInGrouperIncremental() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = true;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = true;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;
    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(false, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeletedByGrouperInGrouperIncremental() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(false, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningCreatedByGrouperInGrouperFull() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = true;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(true, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningCreatedByGrouperInGrouperIncremental() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = true;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(false, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeletedByGrouperInGrouperNotIfUnmarkedProvisionableFull() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(true, entityAttributeTestConfig, overallResult); 
  }

  public void testProvisioningDeletedByGrouperInGrouperNotIfUnmarkedProvisionableIncremental() {
    
    EntityAttributeTestConfig entityAttributeTestConfig = new EntityAttributeTestConfig();
    entityAttributeTestConfig.deleteMemberships = true;
    entityAttributeTestConfig.deleteMembershipsIfNotExistInGrouper = false;
    entityAttributeTestConfig.deleteValueIfManagedByGrouper = false;
    entityAttributeTestConfig.deleteMembershipsOnlyInTrackedGroups = false;
    entityAttributeTestConfig.deleteMembershipsIfGroupUnmarkedProvisionable = false;
    entityAttributeTestConfig.deleteMembershipsIfGrouperDeleted = true;
    entityAttributeTestConfig.deleteMembershipsIfGrouperCreated = false;
    
    OverallResult overallResult = new OverallResult();
    overallResult.aclarkResult.valueNotInGrouper = true;
    overallResult.aclarkResult.valueUnprovisionableGroup = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupExisting = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.aclarkResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupExistedGroupDeletedInGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = true;
    overallResult.aclarkResult.valueProvisionableGroupRenamedOldNameExisted = false;
    overallResult.aclarkResult.valueProvisionableGroupRenamedNewNameExisted = true;

    
    overallResult.adoeResult.valueNotInGrouper = true;
    overallResult.adoeResult.valueUnprovisionableGroup = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExisting = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperThenDeleted = false;
    overallResult.adoeResult.valueProvisionableGroupMembershipExistedDeletedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupMembershipNotInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperUnmarkedProvisionable = false;
    overallResult.adoeResult.valueProvisionableGroupExistedUnmarkedProvisionable = true;
    overallResult.adoeResult.valueProvisionableGroupCreatedByGrouperGroupDeletedInGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupExistedGroupDeletedInGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameCreatedByGrouper = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameCreatedByGrouper = false;
    overallResult.adoeResult.valueProvisionableGroupRenamedOldNameExisted = true;
    overallResult.adoeResult.valueProvisionableGroupRenamedNewNameExisted = false;

    
    provisioningEntityAttributesHelper(false, entityAttributeTestConfig, overallResult); 
  }

}
