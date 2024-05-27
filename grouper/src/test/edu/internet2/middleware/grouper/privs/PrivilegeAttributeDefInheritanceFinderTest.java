package edu.internet2.middleware.grouper.privs;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.PrivilegeAttributeDefInheritanceSave;
import edu.internet2.middleware.grouper.PrivilegeStemInheritanceSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.rules.RuleApi;
import junit.textui.TestRunner;

public class PrivilegeAttributeDefInheritanceFinderTest extends GrouperTest {
  
  public static void main(String[] args) throws Exception {
    TestRunner.run(new PrivilegeAttributeDefInheritanceFinderTest("testFindAssignedEffectivePrivileges1"));
  }
  
  public PrivilegeAttributeDefInheritanceFinderTest(String name) {
    super(name);
  }
  
  public void testFindAssignedEffectivePrivileges() {
    GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Set<Privilege> assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("test")
        .findAssignedPrivileges();
    
    assertTrue(assignedPrivileges.isEmpty());
    
    Stem stem = StemFinder.findByName("test", true);

    testGroup.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Privilege> privileges = new HashSet<>();
    privileges.add(AttributeDefPrivilege.ATTR_ADMIN);
    
    RuleApi.inheritAttributeDefPrivileges(stem, Scope.SUB, testGroup.toSubject(), privileges);
    RuleApi.runRulesForOwner(stem);
    
    assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("test")
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertEquals("attrAdmin", assignedPrivileges.iterator().next().getName());
    
    Set<Privilege> effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName("test").findEffectivePrivileges();
    
    assertEquals(8, effectivePrivileges.size());
    
    Group testGroup1 = new GroupSave().assignName("test1:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = StemFinder.findByName("test1", true);

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Privilege> privileges1 = new HashSet<>();
    privileges1.add(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE);
    
    RuleApi.inheritAttributeDefPrivileges(stem1, Scope.SUB, testGroup1.toSubject(), privileges1);
    RuleApi.runRulesForOwner(stem1);
    
    assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName("test1")
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertEquals("attrDefAttrUpdate", assignedPrivileges.iterator().next().getName());
    
    effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName("test1").findEffectivePrivileges();
        
    assertEquals(2, effectivePrivileges.size());
  }
  
  public void testFindAssignedEffectivePrivileges1() {
    GrouperSession.startRootSession();
    
    Stem childStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(true).save();
    
    Group testGroup = new GroupSave().assignName("test2:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Set<Privilege> assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertTrue(assignedPrivileges.isEmpty());
    
    Stem parentStem = StemFinder.findByName("test", true);

    testGroup.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeAttributeDefInheritanceSave().assignStem(parentStem).assignStemScope(Scope.SUB).assignSubject(testGroup.toSubject())
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).save();
    
    assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    
    Set<Privilege> effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(4, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_VIEW));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTIN));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTOUT));
    
    Group testGroup1 = new GroupSave().assignName("test1:testGroup1").assignCreateParentStemsIfNotExist(true).save();

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    new PrivilegeAttributeDefInheritanceSave().assignStem(parentStem).assignStemScope(Scope.ONE).assignSubject(testGroup1.toSubject())
    .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN).save();
    
    assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(parentStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(2, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    assertTrue(assignedPrivileges.contains(AttributeDefPrivilege.ATTR_ADMIN));
    
    effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(parentStem.getName()).findEffectivePrivileges();
        
    assertEquals(8, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_VIEW));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTIN));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTOUT));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_DEF_ATTR_READ));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_READ));
    
    assignedPrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true)
        .assignStemName(childStem.getName())
        .findAssignedPrivileges();
    
    assertEquals(1, assignedPrivileges.size());
    assertTrue(assignedPrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    
    effectivePrivileges = new PrivilegeAttributeDefInheritanceFinder()
        .assignSubject(SubjectTestHelper.SUBJ0)
        .assignRunAsRoot(true).assignStemName(childStem.getName()).findEffectivePrivileges();
    
    assertEquals(4, effectivePrivileges.size());
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_UPDATE));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_VIEW));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTIN));
    assertTrue(effectivePrivileges.contains(AttributeDefPrivilege.ATTR_OPTOUT));
    
  }

}
