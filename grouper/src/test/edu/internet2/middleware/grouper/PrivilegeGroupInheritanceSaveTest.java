package edu.internet2.middleware.grouper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class PrivilegeGroupInheritanceSaveTest extends GrouperTest {
  
  public static void main(String[] args) {
    TestRunner.run(new PrivilegeGroupInheritanceSaveTest("testSavePrivilegesOnAllGroupsInAStemLookupStemById2"));
  }
  
  /**
   * 
   * @param name
   */
  public PrivilegeGroupInheritanceSaveTest(String name) {
    super(name);
  }

  
  
  public void testSavePrivilegesOnAllGroupsInAStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    new PrivilegeGroupInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
  }
  
  public void testSavePrivilegesOnAllGroupsInAStemLookupStemById() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    new PrivilegeGroupInheritanceSave()
      .assignStemId(stem.getId())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
  }
  
  public void testSavePrivilegesOnAllGroupsInAStemLookupStemByName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    new PrivilegeGroupInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
  }
  
  public void testSavePrivilegesOnAllGroupsStemNotFound() {
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeGroupInheritanceSave()
      .assignStemName("non_existent_stem_name")
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
   
  }
  
  public void testSavePrivilegesOnAllGroupsSubjectNotFound() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeGroupInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubjectId("non_existent_subject_id")
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
   
  }
  
  
  public void testSavePrivilegesOnAllGroupsInAStemSubjectDoesNotHaveAdminButRunningAsRoot() {
    // subject doesn't have admin on the stem and running as root; it should work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeGroupInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignRunAsRoot(true)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
    
  }
  
  public void testSavePrivilegesOnAllGroupsInAStemSubjectHasAdmin() {
    // subject has admin on the stem; it should work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeGroupInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignRunAsRoot(false)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
    
  }
  
  public void testSavePrivilegesOnAllGroupsInAStemSubjectDoesNotHaveAdminOnTheStem() {
    // subject does not have admin on the stem and not running as root; it should not work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeGroupInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(AccessPrivilege.ADMIN)
      .addPrivilege(AccessPrivilege.OPTIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);

    
  }

  public void testSavePrivilegesOnAllGroupsInAStemLookupStemById2() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
  
    Group group1 = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .save();
    
    Group group2 = new GroupSave(grouperSession)
        .assignName("test:test1:someGroupB")
        .assignCreateParentStemsIfNotExist(true)
        .save();
    
    RuleApi.inheritGroupPrivileges(stem, Scope.SUB, SubjectTestHelper.SUBJ0, GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.OPTIN));
        
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addGroup(group1)
        .addGroup(group2)
        .assignFieldType(FieldType.ACCESS)
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
    
    Set<String> groupIds = new HashSet<String>();
    groupIds.add(group1.getId());
    groupIds.add(group2.getId());
    
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer1.getGroupOwner().getId()));
    Assert.assertTrue(groupIds.contains(membershipSubjectContainer2.getGroupOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("admins"));
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("optins"));
  }
  

}
