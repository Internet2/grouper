package edu.internet2.middleware.grouper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.textui.TestRunner;

public class PrivilegeAttributeDefInheritanceSaveTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new PrivilegeAttributeDefInheritanceSaveTest("testSavePrivilegesOnAllAttributeDefsInAStemSubjectDoesNotHaveAdmin"));
  }
  
  /**
   * 
   * @param name
   */
  public PrivilegeAttributeDefInheritanceSaveTest(String name) {
    super(name);
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    AttributeDef attributeDef2 = stem.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.store();
    
    new PrivilegeAttributeDefInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addAttributeDef(attributeDef1)
        .addAttributeDef(attributeDef2)
        .assignFieldType(FieldType.ATTRIBUTE_DEF)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignMemberIds(Arrays.asList(member.getId()))
        .assignHasMembershipTypeForMember(true)
        .assignSplitScopeForMember(true);
      
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    Assert.assertEquals(2, results.size());
    
    MembershipSubjectContainer membershipSubjectContainer1 = results.iterator().next();
    MembershipSubjectContainer membershipSubjectContainer2 = results.iterator().next();
    
    Set<String> attributeDefIds = new HashSet<String>();
    attributeDefIds.add(attributeDef1.getId());
    attributeDefIds.add(attributeDef2.getId());
    
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer1.getAttributeDefOwner().getId()));
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer2.getAttributeDefOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("attrAdmins"));
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemLookupByStemId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    AttributeDef attributeDef2 = stem.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.store();
    
    
    new PrivilegeAttributeDefInheritanceSave()
      .assignStemId(stem.getId())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addAttributeDef(attributeDef1)
        .addAttributeDef(attributeDef2)
        .assignFieldType(FieldType.ATTRIBUTE_DEF)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignMemberIds(Arrays.asList(member.getId()))
        .assignHasMembershipTypeForMember(true)
        .assignSplitScopeForMember(true);
      
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    Assert.assertEquals(2, results.size());
    
    MembershipSubjectContainer membershipSubjectContainer1 = results.iterator().next();
    MembershipSubjectContainer membershipSubjectContainer2 = results.iterator().next();
    
    Set<String> attributeDefIds = new HashSet<String>();
    attributeDefIds.add(attributeDef1.getId());
    attributeDefIds.add(attributeDef2.getId());
    
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer1.getAttributeDefOwner().getId()));
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer2.getAttributeDefOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("attrAdmins"));
  }
  
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemLookupByStemName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    AttributeDef attributeDef2 = stem.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.store();
    
    
    new PrivilegeAttributeDefInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addAttributeDef(attributeDef1)
        .addAttributeDef(attributeDef2)
        .assignFieldType(FieldType.ATTRIBUTE_DEF)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignMemberIds(Arrays.asList(member.getId()))
        .assignHasMembershipTypeForMember(true)
        .assignSplitScopeForMember(true);
      
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    Assert.assertEquals(2, results.size());
    
    MembershipSubjectContainer membershipSubjectContainer1 = results.iterator().next();
    MembershipSubjectContainer membershipSubjectContainer2 = results.iterator().next();
    
    Set<String> attributeDefIds = new HashSet<String>();
    attributeDefIds.add(attributeDef1.getId());
    attributeDefIds.add(attributeDef2.getId());
    
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer1.getAttributeDefOwner().getId()));
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer2.getAttributeDefOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("attrAdmins"));
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemButStemNotFound() {
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeAttributeDefInheritanceSave()
      .assignStemName("non_existent_stem_name")
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemSubjectNotFound() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeAttributeDefInheritanceSave()
        .assignStemName(stem.getName())
        .assignStemScope(Scope.SUB)
        .assignSubjectId("non_existent_subject_id")
        .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
        .save();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemSubjectNotAdminButRunningAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    AttributeDef attributeDef2 = stem.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.store();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeAttributeDefInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignRunAsRoot(true)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addAttributeDef(attributeDef1)
        .addAttributeDef(attributeDef2)
        .assignFieldType(FieldType.ATTRIBUTE_DEF)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignMemberIds(Arrays.asList(member.getId()))
        .assignHasMembershipTypeForMember(true)
        .assignSplitScopeForMember(true);
      
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    Assert.assertEquals(2, results.size());
    
    MembershipSubjectContainer membershipSubjectContainer1 = results.iterator().next();
    MembershipSubjectContainer membershipSubjectContainer2 = results.iterator().next();
    
    Set<String> attributeDefIds = new HashSet<String>();
    attributeDefIds.add(attributeDef1.getId());
    attributeDefIds.add(attributeDef2.getId());
    
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer1.getAttributeDefOwner().getId()));
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer2.getAttributeDefOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("attrAdmins"));
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemSubjectHasAdmin() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN);
   
    AttributeDef attributeDef1 = stem.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.store();
    
    AttributeDef attributeDef2 = stem.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.store();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeAttributeDefInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addAttributeDef(attributeDef1)
        .addAttributeDef(attributeDef2)
        .assignFieldType(FieldType.ATTRIBUTE_DEF)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignMemberIds(Arrays.asList(member.getId()))
        .assignHasMembershipTypeForMember(true)
        .assignSplitScopeForMember(true);
      
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    Assert.assertEquals(2, results.size());
    
    MembershipSubjectContainer membershipSubjectContainer1 = results.iterator().next();
    MembershipSubjectContainer membershipSubjectContainer2 = results.iterator().next();
    
    Set<String> attributeDefIds = new HashSet<String>();
    attributeDefIds.add(attributeDef1.getId());
    attributeDefIds.add(attributeDef2.getId());
    
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer1.getAttributeDefOwner().getId()));
    Assert.assertTrue(attributeDefIds.contains(membershipSubjectContainer2.getAttributeDefOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("attrAdmins"));
  }
  
  public void testSavePrivilegesOnAllAttributeDefsInAStemSubjectDoesNotHaveAdmin() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      new PrivilegeAttributeDefInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN)
      .save();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
   
  }
  
  
}
