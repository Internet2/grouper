/**
 * @author GRP-7187: convert a normal group to a composite in place without change log churn.
 */
package edu.internet2.middleware.grouper;

import java.util.List;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Verifies the target behavior for GRP-7187: turning an existing normal group into a composite via
 * {@link CompositeInPlaceConverter} does not churn the change log (or PIT) for members whose
 * effective membership does not change. Only genuine joins and leaves are logged; the overlap is
 * relabeled from mship_type=immediate to mship_type=composite in place with no change log entry.
 *
 * The three cases here are: a union whose result equals the current members (no churn), an
 * intersection whose result equals the current members (no churn), and a case whose result differs
 * by one member each way (exactly one MEMBERSHIP_ADD and one MEMBERSHIP_DELETE, and nothing for the
 * overlap).
 */
public class CompositeConvertInPlaceTest extends GrouperTest {

  /**
   * @param args command line args
   */
  public static void main(String[] args) {
    TestRunner.run(new CompositeConvertInPlaceTest("testConvertNormalToCompositeInPlaceNoChurn"));
  }

  /**
   * @param name test method name
   */
  public CompositeConvertInPlaceTest(String name) {
    super(name);
  }

  /**
   * default constructor
   */
  public CompositeConvertInPlaceTest() {
    super();
  }

  /**
   * Convert a normal group with immediate members into a composite whose factors resolve to
   * exactly the same member set. Because no member's effective membership changes, a correct
   * in-place conversion must emit zero membership change log entries for the group.
   */
  public void testConvertNormalToCompositeInPlaceNoChurn() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    // group1 will become the composite; group2 and group3 are its union factors
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();

    // group1 currently has two immediate members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);

    // set up the factors so that (group2 union group3) resolves to exactly SUBJ0 and SUBJ1, the
    // members group1 already has. The conversion is therefore a pure relabel: no genuine joins
    // and no genuine leaves, so the effective membership never changes.
    group2.addMember(SubjectTestHelper.SUBJ0);
    group3.addMember(SubjectTestHelper.SUBJ1);

    // flush all of the above out of the change log, then start counting from a clean slate
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // ---- conversion under test ------------------------------------------------------------------
    // GRP-7187 in-place relabel: relabel the overlap immediate rows (mship_type immediate ->
    // composite) instead of deleting and re-adding them, so members whose effective membership does
    // not change produce no change log entry.  A composite group cannot hold immediate members
    // (AddCompositeMemberValidator throws GROUP_ACTM), and the membership_uniq_idx unique index on
    // (owner_id, member_id, field_id) forbids an immediate and a composite row from coexisting, so
    // the relabel -- not an add-then-remove -- is the only in-place option.
    CompositeInPlaceConverter.convert(group1, CompositeType.UNION, group2, group3);
    // ---------------------------------------------------------------------------------------------

    // let the compositeMemberships consumer materialize the composite rows, then flush the log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();

    // the effective membership must be unchanged: group1 still has exactly SUBJ0 and SUBJ1
    assertEquals(2, group1.getMembers().size());
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));

    // no member joined or left, so there must be zero membership change log entries for group1
    assertEquals("no membership adds for an unchanged set", 0,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_ADD));
    assertEquals("no membership deletes for an unchanged set", 0,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_DELETE));
  }

  /**
   * Intersection variant of the no-churn case: group1's immediate members equal
   * (group2 intersect group3), so the conversion is again a pure relabel with zero membership
   * change log entries.
   */
  public void testConvertNormalToCompositeIntersectionInPlaceNoChurn() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();

    // group1 currently has SUBJ0 and SUBJ1 as immediate members
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);

    // group2 intersect group3 == {SUBJ0, SUBJ1}: each factor holds both shared members plus one
    // extra non-shared member, so the intersection is exactly group1's current set.
    group2.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group3.addMember(SubjectTestHelper.SUBJ0);
    group3.addMember(SubjectTestHelper.SUBJ1);
    group3.addMember(SubjectTestHelper.SUBJ3);

    // flush setup out of the change log and start counting from a clean slate
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    CompositeInPlaceConverter.convert(group1, CompositeType.INTERSECTION, group2, group3);

    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();

    // effective membership unchanged and no churn
    assertEquals(2, group1.getMembers().size());
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));
    assertEquals("no membership adds for an unchanged set", 0,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_ADD));
    assertEquals("no membership deletes for an unchanged set", 0,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_DELETE));
  }

  /**
   * When the composite result differs from the current immediate set, only the genuine delta is
   * logged: the overlap is relabeled silently, one genuine leave produces exactly one
   * MEMBERSHIP_DELETE, and one genuine join produces exactly one MEMBERSHIP_ADD.
   */
  public void testConvertNormalToCompositeInPlaceGenuineJoinAndLeave() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();

    // group1 currently has SUBJ0 (will stay) and SUBJ1 (will leave)
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);

    // group2 union group3 == {SUBJ0, SUBJ2}: SUBJ0 overlaps, SUBJ2 is a genuine join, and SUBJ1
    // (in neither factor) is a genuine leave.
    group2.addMember(SubjectTestHelper.SUBJ0);
    group3.addMember(SubjectTestHelper.SUBJ2);

    // flush setup out of the change log and start counting from a clean slate
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    CompositeInPlaceConverter.convert(group1, CompositeType.UNION, group2, group3);

    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    ChangeLogTempToEntity.convertRecords();

    // final membership: SUBJ0 (relabeled in place) and SUBJ2 (joined); SUBJ1 has left
    assertEquals(2, group1.getMembers().size());
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(group1.hasMember(SubjectTestHelper.SUBJ1));

    // exactly one genuine join and one genuine leave are logged; the SUBJ0 overlap logs nothing
    assertEquals("exactly one genuine join logged", 1,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_ADD));
    assertEquals("exactly one genuine leave logged", 1,
        countMembershipChangeLogEntries(group1.getId(), ChangeLogTypeBuiltin.MEMBERSHIP_DELETE));
  }

  /**
   * Count the flattened change log entries of the given membership type (MEMBERSHIP_ADD or
   * MEMBERSHIP_DELETE) that reference the given group. Assumes ChangeLogTempToEntity.convertRecords()
   * has already promoted the temp entries to real change log entries.
   * @param groupId uuid of the group whose membership entries are counted
   * @param changeLogType MEMBERSHIP_ADD or MEMBERSHIP_DELETE
   * @return number of matching change log entries referencing the group
   */
  private static long countMembershipChangeLogEntries(String groupId, ChangeLogTypeBuiltin changeLogType) {
    String typeId = changeLogType.getChangeLogType().getId();

    List<ChangeLogEntry> entries = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :typeId")
        .setString("typeId", typeId)
        .list(ChangeLogEntry.class);

    boolean isAdd = changeLogType == ChangeLogTypeBuiltin.MEMBERSHIP_ADD;
    long count = 0;
    for (ChangeLogEntry entry : entries) {
      String entryGroupId = isAdd
          ? entry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId)
          : entry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
      if (GrouperUtil.equals(groupId, entryGroupId)) {
        count++;
      }
    }
    return count;
  }

}
