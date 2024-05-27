package edu.internet2.middleware.grouper.privs;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.PrivilegeStemInheritanceSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.rules.RuleApi;
import junit.textui.TestRunner;

public class PrivilegeStemInheritanceFinderTest extends GrouperTest {
  
  public static void main(String[] args) throws Exception {
    TestRunner.run(new PrivilegeStemInheritanceFinderTest("testFindAssignedEffectivePrivileges"));
  }
  
  public PrivilegeStemInheritanceFinderTest(String name) {
    super(name);
  }
  
  public void testFindAssignedEffectivePrivileges() {
    GrouperSession.startRootSession();
    
    Stem childStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(true).save();
    
    Group testGroup = new GroupSave().assignName("test2:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Set<Privilege> assignedPrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertTrue(assignedPrivileges.isEmpty());
    
    Stem parentStem = StemFinder.findByName("test", true);

    testGroup.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeStemInheritanceSave().assignStem(parentStem).assignStemScope(Scope.SUB).assignSubject(testGroup.toSubject())
      .addPrivilege(NamingPrivilege.CREATE).save();
    
    assignedPrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(NamingPrivilege.CREATE));
    
    Set<Privilege> effectivePrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(2, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(NamingPrivilege.CREATE));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM_ADMIN));
    
    Group testGroup1 = new GroupSave().assignName("test1:testGroup1").assignCreateParentStemsIfNotExist(true).save();

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeStemInheritanceSave().assignStem(parentStem).assignStemScope(Scope.ONE).assignSubject(testGroup1.toSubject())
    .addPrivilege(NamingPrivilege.STEM).save();
    
    assignedPrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(parentStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(2, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(NamingPrivilege.STEM));
    assertTrue(assignedPrivileges.contains(NamingPrivilege.CREATE));
    
    effectivePrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(parentStem.getName()).findEffectivePrivileges();
        
    assertEquals(5, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.CREATE));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM_ADMIN));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM_ATTR_READ));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM_ATTR_UPDATE));
    
    assignedPrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(NamingPrivilege.CREATE));
    
    effectivePrivileges = new PrivilegeStemInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(2, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(NamingPrivilege.STEM_ADMIN));
    assertTrue(effectivePrivileges.contains(NamingPrivilege.CREATE));
    
  }

}
