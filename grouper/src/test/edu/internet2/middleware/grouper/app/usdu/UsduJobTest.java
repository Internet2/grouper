package edu.internet2.middleware.grouper.app.usdu;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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

public class UsduJobTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
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
    if (registrySubjects.size() > 0) {
      
      RegistrySubjectDAO dao = GrouperDAOFactory.getFactory().getRegistrySubject();
      dao.delete(registrySubjects.get(0));
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
