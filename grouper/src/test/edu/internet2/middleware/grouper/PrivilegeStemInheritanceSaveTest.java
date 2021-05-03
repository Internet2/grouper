package edu.internet2.middleware.grouper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.textui.TestRunner;

public class PrivilegeStemInheritanceSaveTest extends GrouperTest {
  
  public static void main(String[] args) {
    TestRunner.run(new PrivilegeStemInheritanceSaveTest("testSavePrivilegesOnAllStemsInAStem"));
  }
  
  /**
   * 
   * @param name
   */
  public PrivilegeStemInheritanceSaveTest(String name) {
    super(name);
  }
  
  public void testSavePrivilegesOnAllStemsInAStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Stem stem1 = new StemSave(grouperSession).assignName("test:test1")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:test2")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    new PrivilegeStemInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addStem(stem1)
        .addStem(stem2)
        .assignFieldType(FieldType.NAMING)
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
    
    Set<String> stemIds = new HashSet<String>();
    stemIds.add(stem1.getId());
    stemIds.add(stem2.getId());
    
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer1.getStemOwner().getId()));
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer2.getStemOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("stemAdmins"));
  }
  
  public void testSavePrivilegesOnAllStemsInAStemLookupStemById() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Stem stem1 = new StemSave(grouperSession).assignName("test:test1")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:test2")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    new PrivilegeStemInheritanceSave()
      .assignStemId(stem.getId())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addStem(stem1)
        .addStem(stem2)
        .assignFieldType(FieldType.NAMING)
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
    
    Set<String> stemIds = new HashSet<String>();
    stemIds.add(stem1.getId());
    stemIds.add(stem2.getId());
    
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer1.getStemOwner().getId()));
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer2.getStemOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("stemAdmins"));
  }
  
  public void testSavePrivilegesOnAllStemsInAStemLookupStemByName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Stem stem1 = new StemSave(grouperSession).assignName("test:test1")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:test2")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    new PrivilegeStemInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addStem(stem1)
        .addStem(stem2)
        .assignFieldType(FieldType.NAMING)
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
    
    Set<String> stemIds = new HashSet<String>();
    stemIds.add(stem1.getId());
    stemIds.add(stem2.getId());
    
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer1.getStemOwner().getId()));
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer2.getStemOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("stemAdmins"));
    
  }
  
  public void testSavePrivilegesStemNotFound() {
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeStemInheritanceSave()
      .assignStemName("non_existent_stem_name")
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
   
  }
  
  public void testSavePrivilegesOnStemsSubjectNotFound() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeStemInheritanceSave()
      .assignStem(stem)
      .assignStemScope(Scope.SUB)
      .assignSubjectId("non_existent_subject_id")
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
   
  }
  
  
  public void testSavePrivilegesOnAllStemsInAStemSubjectDoesNotHaveAdminButRunningAsRoot() {
    // subject doesn't have admin on the stem and running as root; it should work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    Stem stem1 = new StemSave(grouperSession).assignName("test:test1")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:test2")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeStemInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignRunAsRoot(true)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addStem(stem1)
        .addStem(stem2)
        .assignFieldType(FieldType.NAMING)
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
    
    Set<String> stemIds = new HashSet<String>();
    stemIds.add(stem1.getId());
    stemIds.add(stem2.getId());
    
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer1.getStemOwner().getId()));
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer2.getStemOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("stemAdmins"));
    
  }
  
  public void testSavePrivilegesOnAllStemsInAStemSubjectHasAdmin() {
    // subject has admin on the stem; it should work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    Stem stem1 = new StemSave(grouperSession).assignName("test:test1")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    Stem stem2 = new StemSave(grouperSession).assignName("test:test2")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    new PrivilegeStemInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
    
    grouperSession = GrouperSession.startRootSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MembershipFinder membershipFinder = new MembershipFinder()
        .addStem(stem1)
        .addStem(stem2)
        .assignFieldType(FieldType.NAMING)
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
    
    Set<String> stemIds = new HashSet<String>();
    stemIds.add(stem1.getId());
    stemIds.add(stem2.getId());
    
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer1.getStemOwner().getId()));
    Assert.assertTrue(stemIds.contains(membershipSubjectContainer2.getStemOwner().getId()));
    
    Assert.assertTrue(membershipSubjectContainer1.getAllMemberships().containsKey("stemAdmins"));
    
  }
  
  public void testSavePrivilegesOnAllStemsInAStemSubjectDoesNotHaveAdminOnTheStem() {
    // subject does not have admin on the stem and not running as root; it should not work
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test").save();

    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      new PrivilegeStemInheritanceSave()
      .assignStemName(stem.getName())
      .assignStemScope(Scope.SUB)
      .assignSubject(SubjectTestHelper.SUBJ0)
      .addPrivilege(NamingPrivilege.STEM_ADMIN)
      .save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);

    
  }
  

}
