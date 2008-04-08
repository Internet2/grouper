package edu.internet2.middleware.grouper;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

public class TestUSDU extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(TestUSDU.class);

  // TESTS //

  private void deleteSubject(Subject subject) throws InterruptedException {

    RegistrySubjectDAO dao = GrouperDAOFactory.getFactory().getRegistrySubject();
    RegistrySubjectDTO dto = new RegistrySubjectDTO().setId(subject.getId()).setName(subject.getName()).setType(
        subject.getType().toString());
    dao.delete(dto);

    // must be longer than ehcache
    Thread.sleep(1000);

    try {
      SubjectFinder.findById("a");
      fail("should not find subject");
    } catch (SubjectNotFoundException e) {
      // OK
    } catch (SubjectNotUniqueException e) {
      fail("huh");
    }
  }

  public void testImmediateMembership() {

    try {
      LOG.info("testImmediateMembership");
      R r = R.populateRegistry(1, 6, 1);

      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("a", "b");
      Group gC = r.getGroup("a", "c");
      Group gD = r.getGroup("a", "d");
      Group gE = r.getGroup("a", "e");
      Group gF = r.getGroup("a", "f");

      Subject subjA = SubjectFinder.findById("a");

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

      Set<Member> unresolvable = USDU.getUnresolvableMembers(r.getSession(), null);

      assertTrue(unresolvable.size() == 1);
      assertTrue(unresolvable.iterator().next().getSubjectId().equals(subjA.getId()));

      for (Member member : unresolvable) {
        for (Object group : member.getImmediateGroups()) {
          USDU.deleteUnresolvableMember(member, (Group) group);
        }
      }

      assertFalse(gA.hasMember(subjA));
      assertFalse(gB.hasMember(subjA));
      assertFalse(gC.hasMember(subjA));
      assertFalse(gE.hasMember(subjA));
      assertFalse(gF.hasMember(subjA));

      assertTrue(USDU.getUnresolvableMembers(r.getSession(), null).size() == 0);

    } catch (Exception e) {
      T.e(e);
    }
  }
}
