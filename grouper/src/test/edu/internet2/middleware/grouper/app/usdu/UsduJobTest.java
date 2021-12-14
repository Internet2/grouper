package edu.internet2.middleware.grouper.app.usdu;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import junit.textui.TestRunner;

public class UsduJobTest extends GrouperTest {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new UsduJobTest("testNoLongerSubjectResolutionEligibleLocalEntities"));
  }
  
  public UsduJobTest(String name) {
    super(name);
  }
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
  }
  
  public void testNoLongerSubjectResolutionEligibleLocalEntities() throws InterruptedException {
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    Entity testEntity1 = new EntitySave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity1").save();
    Entity testEntity2 = new EntitySave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity2").save();

    Group group1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    group1.addMember(testEntity1.toSubject());
    
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity1.toSubject(), false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity2.toSubject(), false).isSubjectResolutionEligible());

    testEntity1.delete();
    
    // testEntity1
    assertEquals(1, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    // run usdu
    UsduJob.runDaemonStandalone();

    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity1.toSubject(), false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity2.toSubject(), false).isSubjectResolutionEligible());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    testEntity2.delete();
    
    // testEntity2
    assertEquals(1, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity1.toSubject(), false).isSubjectResolutionEligible());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testEntity2.toSubject(), false).isSubjectResolutionEligible());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
  }
  
  public void testNoLongerSubjectResolutionEligible() throws InterruptedException {
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    Group group1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group group3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    
    group2.addMember(subject0);
    group2.addMember(subject1);
    group3.addMember(subject0);
    group3.delete();
    
    // group3
    assertEquals(1, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    group2.deleteMember(subject0);

    // group3, subject0
    assertEquals(2, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    // run usdu
    UsduJob.runDaemonStandalone();

    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject1, false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.internal_findAllMember().isSubjectResolutionEligible());
    assertTrue(MemberFinder.internal_findRootMember().isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), group1.toSubject(), false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), group2.toSubject(), false).isSubjectResolutionEligible());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), group3.toSubject(), false).isSubjectResolutionEligible());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // add subject0 again so it's eligible again
    group1.addMember(subject0);
    
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // subject0 gone but still in a group so eligible
    deleteSubject(subject0);
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());

    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // even if membership is deleted, subject0 is still eligible because resolvable=F and deleted=F
    group1.deleteMember(subject0);

    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // subject0 will get cleaned up
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "-1");

    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // subject0 becomes resolvable again    
    RegistrySubject newSubject = new RegistrySubject();
    newSubject.setId(subject0.getId());
    newSubject.setName(subject0.getName());
    newSubject.setTypeString("person");
    GrouperDAOFactory.getFactory().getRegistrySubject().create(newSubject);
    SubjectFinder.flushCache();
    
    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
    
    // and add back to a group
    group1.addMember(subject0);
    
    // run usdu
    UsduJob.runDaemonStandalone();
    
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionEligible());
    assertTrue(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionResolvable());
    assertFalse(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject0, false).isSubjectResolutionDeleted());
    assertEquals(0, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsNoLongerSubjectResolutionEligible().size());
  }
  
  public void testUsduWithUnusedMemberMarkedUnresolvable() throws InterruptedException {
    int initialSize = GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size();

    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);

    group.addMember(subject0);
    group.addMember(subject1);

    deleteSubject(subject0);

    UsduJob.runDaemonStandalone();
    assertTrue(group.hasMember(subject0));
    assertTrue(group.hasMember(subject1));

    group.deleteMember(subject0);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "-1");

    assertEquals(initialSize + 2, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size());
    UsduJob.runDaemonStandalone();
    assertEquals(initialSize + 1, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size());

    assertFalse(group.hasMember(subject0));
    assertTrue(group.hasMember(subject1));
  }
  
  public void testUsduJobWithAttributes() throws InterruptedException {
    int initialSize = GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size();
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Stem stem = group.getParentStem();
    Subject subject = SubjectFinder.findById("test.subject.0", true);
    
    AttributeDef attributeDef = stem.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToMember(true);
    attributeDef.setAssignToMemberAssn(true);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName = stem.addChildAttributeDefName(attributeDef, "attributeDefName", "attributeDefName");
    
    group.addMember(subject);
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
    AttributeAssign assign = member.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    assign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test2");
    new GroupMember(group, member).getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test3");
    
    deleteSubject(subject);
    
    UsduJob.runDaemonStandalone();
    assertTrue(group.hasMember(subject));
    
    assertTrue(member.getAttributeDelegate().retrieveAttributes().contains(UsduAttributeNames.retrieveAttributeDefNameBase()));
    assertTrue(member.getAttributeDelegate().retrieveAttributes().contains(attributeDefName));
    assertTrue(new GroupMember(group, member).getAttributeDelegate().retrieveAttributes().contains(attributeDefName));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "-1");

    assertEquals(initialSize + 1, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size());
    UsduJob.runDaemonStandalone();
    assertEquals(initialSize, GrouperDAOFactory.getFactory().getMember().findAllMemberIdsForUnresolvableCheck().size());

    assertFalse(group.hasMember(subject));
    
    assertTrue(member.getAttributeDelegate().retrieveAttributes().contains(UsduAttributeNames.retrieveAttributeDefNameBase()));
    assertFalse(member.getAttributeDelegate().retrieveAttributes().contains(attributeDefName));
    assertFalse(new GroupMember(group, member).getAttributeDelegate().retrieveAttributes().contains(attributeDefName));
  }
  
  public void testUsduJobWhenSubjectIsDeleted() throws InterruptedException {
    
    //Given
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "-1");

    R r = R.populateRegistry(1, 6, 1);
    
    Group gA = r.getGroup("a", "a");
    Group gB = r.getGroup("a", "b");
    Group gC = r.getGroup("a", "c");
    Group gD = r.getGroup("a", "d");
    Group gE = r.getGroup("a", "e");
    Group gF = r.getGroup("a", "f");

    Subject subjA = SubjectFinder.findById("a", true);

    gA.addMember(subjA);
    gB.addMember(gA.toSubject());
    gC.addCompositeMember(CompositeType.UNION, gA, gD);
    gE.addCompositeMember(CompositeType.INTERSECTION, gA, gB);
    gF.addCompositeMember(CompositeType.COMPLEMENT, gA, gD);

    assertTrue(gA.hasMember(subjA));
    assertTrue(gB.hasMember(subjA));
    assertTrue(gC.hasMember(subjA));
    assertTrue(gE.hasMember(subjA));
    assertTrue(gF.hasMember(subjA));
    
    deleteSubject(subjA);
    
    //When
    UsduJob.runDaemonStandalone();
    
    //Then
    assertFalse(gA.hasMember(subjA));
    assertFalse(gB.hasMember(subjA));
    assertFalse(gC.hasMember(subjA));
    assertFalse(gE.hasMember(subjA));
    assertFalse(gF.hasMember(subjA));
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subjA, true);
    
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
      .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
      .findAttributeAssignValuesResult();

    SubjectResolutionAttributeValue subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    assertNotNull(subjectResolutionAttributeValue);
    
    assertEquals("true", subjectResolutionAttributeValue.getSubjectResolutionDeletedString());
    assertNull(subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    assertEquals("false", subjectResolutionAttributeValue.getSubjectResolutionResolvableString());
    assertNull(subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolvedString());
    
  }
  
  public void testUsduJobWhenSubjectIsMarkedAsUnresolvable() throws InterruptedException {
    
    //Given
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "10");

    R r = R.populateRegistry(1, 6, 1);
    
    Group gA = r.getGroup("a", "a");
    Group gB = r.getGroup("a", "b");
    Group gC = r.getGroup("a", "c");
    Group gD = r.getGroup("a", "d");
    Group gE = r.getGroup("a", "e");
    Group gF = r.getGroup("a", "f");

    Subject subjA = SubjectFinder.findById("a", true);

    gA.addMember(subjA);
    gB.addMember(gA.toSubject());
    gC.addCompositeMember(CompositeType.UNION, gA, gD);
    gE.addCompositeMember(CompositeType.INTERSECTION, gA, gB);
    gF.addCompositeMember(CompositeType.COMPLEMENT, gA, gD);

    assertTrue(gA.hasMember(subjA));
    assertTrue(gB.hasMember(subjA));
    assertTrue(gC.hasMember(subjA));
    assertTrue(gE.hasMember(subjA));
    assertTrue(gF.hasMember(subjA));
    
    deleteSubject(subjA);
    
    //When
    UsduJob.runDaemonStandalone();
    
    //Then
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subjA, true);
    
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
    .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
    .findAttributeAssignValuesResult();

    SubjectResolutionAttributeValue subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    assertNotNull(subjectResolutionAttributeValue);
    
    assertEquals("false", subjectResolutionAttributeValue.getSubjectResolutionDeletedString());
    assertNotNull(subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    assertEquals("false", subjectResolutionAttributeValue.getSubjectResolutionResolvableString());
    assertNotNull(subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolvedString());
      
  }
  
  public void testUsduJobWhenSubjectBecomesResolvableAgain() throws InterruptedException {
    
    //Given
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("usdu.delete.ifAfterDays", "10");
    
    R r = R.populateRegistry(1, 6, 1);
    
    Group gA = r.getGroup("a", "a");
    Group gB = r.getGroup("a", "b");
    Group gC = r.getGroup("a", "c");
    Group gD = r.getGroup("a", "d");
    Group gE = r.getGroup("a", "e");
    Group gF = r.getGroup("a", "f");

    Subject subjA = SubjectFinder.findById("a", true);
    String id = subjA.getId();
    String name = subjA.getName();

    gA.addMember(subjA);
    gB.addMember(gA.toSubject());
    gC.addCompositeMember(CompositeType.UNION, gA, gD);
    gE.addCompositeMember(CompositeType.INTERSECTION, gA, gB);
    gF.addCompositeMember(CompositeType.COMPLEMENT, gA, gD);

    assertTrue(gA.hasMember(subjA));
    assertTrue(gB.hasMember(subjA));
    assertTrue(gC.hasMember(subjA));
    assertTrue(gE.hasMember(subjA));
    assertTrue(gF.hasMember(subjA));
    
    deleteSubject(subjA);
    
    UsduJob.runDaemonStandalone();
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subjA, true);
    
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
      .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
      .findAttributeAssignValuesResult();

    SubjectResolutionAttributeValue subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    // add the same subject again
    RegistrySubjectDAO  dao   = GrouperDAOFactory.getFactory().getRegistrySubject();
    
    RegistrySubject sameSubjectAsSubjA = new RegistrySubject();
    sameSubjectAsSubjA.setId(id);
    sameSubjectAsSubjA.setName(name);
    sameSubjectAsSubjA.setTypeString("person");
    dao.create(sameSubjectAsSubjA);
    
    SubjectFinder.flushCache();
    
    subjA = SubjectFinder.findById("a", true);

    gA.addMember(subjA, false);
    assertTrue(gA.hasMember(subjA));
    
    attributeAssignValueFinderResult = new AttributeAssignValueFinder()
    .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
    .findAttributeAssignValuesResult();

    subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    assertNotNull(subjectResolutionAttributeValue);
    
    //When - run the job again
    UsduJob.runDaemonStandalone();
    
    //Then
    member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subjA, true);
    attributeAssignValueFinderResult = new AttributeAssignValueFinder()
      .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
      .findAttributeAssignValuesResult();
  
    subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
    
    assertNull(subjectResolutionAttributeValue);
            
  }

  private void deleteSubject(Subject subject) throws InterruptedException {
    
    List<RegistrySubject> registrySubjects = HibernateSession.byCriteriaStatic()
      .list(RegistrySubject.class, Restrictions.eq("id", subject.getId()));

    for (RegistrySubject registrySubject : registrySubjects) {
      registrySubject.delete(GrouperSession.staticGrouperSession());
    }

    SubjectFinder.flushCache();

    try {
      SubjectFinder.findById(subject.getId(), true);
      fail("should not find subject " + subject.getId());
    } catch (SubjectNotFoundException e) {
      // OK
    } catch (SubjectNotUniqueException e) {
      fail("subject should be unique " + subject.getId());
    }
  }
}
