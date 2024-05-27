package edu.internet2.middleware.grouper.privs;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.PrivilegeGroupInheritanceSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import junit.textui.TestRunner;

public class PrivilegeGroupInheritanceFinderTest extends GrouperTest {
  
  
  public static void main(String[] args) throws Exception {
    TestRunner.run(new PrivilegeGroupInheritanceFinderTest("testFindAssignedEffectivePrivileges"));
  }
  
  public PrivilegeGroupInheritanceFinderTest(String name) {
    super(name);
  }
  
  
  public void testFindAssignedEffectivePrivileges() {
    GrouperSession.startRootSession();
    
    Stem childStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(true).save();
    
    Group testGroup = new GroupSave().assignName("test2:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Set<Privilege> assignedPrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertTrue(assignedPrivileges.isEmpty());
    
    Stem parentStem = StemFinder.findByName("test", true);

    testGroup.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeGroupInheritanceSave().assignStem(parentStem).assignStemScope(Scope.SUB).assignSubject(testGroup.toSubject())
      .addPrivilege(AccessPrivilege.UPDATE).save();
    
    assignedPrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AccessPrivilege.UPDATE));
    
    Set<Privilege> effectivePrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(4, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AccessPrivilege.UPDATE));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.VIEW));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTIN));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTOUT));
    
    Group testGroup1 = new GroupSave().assignName("test1:testGroup1").assignCreateParentStemsIfNotExist(true).save();

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeGroupInheritanceSave().assignStem(parentStem).assignStemScope(Scope.ONE).assignSubject(testGroup1.toSubject())
    .addPrivilege(AccessPrivilege.READ).save();
    
    assignedPrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(parentStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(2, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AccessPrivilege.READ));
    assertTrue(assignedPrivileges.contains(AccessPrivilege.UPDATE));
    
    effectivePrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(parentStem.getName()).findEffectivePrivileges();
        
    assertEquals(5, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AccessPrivilege.READ));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.UPDATE));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.VIEW));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTIN));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTOUT));
    
    assignedPrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AccessPrivilege.UPDATE));
    
    effectivePrivileges = new PrivilegeGroupInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(4, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AccessPrivilege.UPDATE));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.VIEW));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTIN));
    assertTrue(effectivePrivileges.contains(AccessPrivilege.OPTOUT));
    
  }
  
}
