/*
 * @author shilen
 * $Id: EffectiveMembershipHooksTest.java,v 1.1 2009-08-18 23:11:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class EffectiveMembershipHooksTest extends GrouperTest {

  
  /**
   * @param name
   */
  public EffectiveMembershipHooksTest(String name) {
    super(name);
  }

  /** root stem */
  private Stem root;
  
  /** grouper session */
  static GrouperSession grouperSession; 
  
  /** custom type */
  private GroupType customType;
  
  /** custom field */
  private Field customField;
  
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    customType = GroupType.createType(grouperSession, "customType");
    customField = customType.addList(grouperSession, "customList", AccessPrivilege.READ, AccessPrivilege.UPDATE);
  }

  /**
   * @throws Exception 
   */
  public void testNoEffectiveHooks() throws Exception {
    
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl9.class);

    MembershipHooksImpl9.resetVariables();
    Stem stem = root.addChildStem("stem", "stem");
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    Group top = stem.addChildGroup("top", "top");
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    top.addMember(SubjectFinder.findById("test.subject.0", true));
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    top.addMember(SubjectFinder.findById("test.subject.1", true));
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
    
    Group owner = stem.addChildGroup("owner", "owner");
    Group right = stem.addChildGroup("right", "right");
    Group left = stem.addChildGroup("left", "left");
    owner.addCompositeMember(CompositeType.UNION, left, right);
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    top.addMember(owner.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    owner.deleteCompositeMember();
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
  }
  
  /**
   * @throws Exception
   */
  public void testHookMembershipObject() throws Exception {
    
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl9.class);

    MembershipHooksImpl9.resetVariables();
    Stem stem = root.addChildStem("stem", "stem");

    Group one = stem.addChildGroup("one", "one");
    Group two = stem.addChildGroup("two", "two");
    Group three = stem.addChildGroup("three", "three");
    
    one.addMember(two.toSubject());
    
    // verify gThree -> gOne
    MembershipHooksImpl9.resetVariables();
    two.addMember(three.toSubject());
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(one.getUuid(), MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(Group.getDefaultList().getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
    
    // verify gThree -> gOne (note the order of membership adds is different from above)
    one.deleteMember(two.toSubject());
    MembershipHooksImpl9.resetVariables();
    one.addMember(two.toSubject());
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(one.getUuid(), MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(Group.getDefaultList().getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
    
    // verify gThree -> gOne (admin priv this time)
    one.deleteMember(two.toSubject());
    MembershipHooksImpl9.resetVariables();
    one.grantPriv(two.toSubject(), AccessPrivilege.ADMIN);
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(one.getUuid(), MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(FieldFinder.find("admins", true).getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
    
    // verify gThree -> gOne (admin priv this time) (note the order of the membership adds is different from above)
    two.deleteMember(three.toSubject());
    MembershipHooksImpl9.resetVariables();
    two.addMember(three.toSubject());
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(one.getUuid(), MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(FieldFinder.find("admins", true).getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
    
    // verify stem -> gThree
    one.revokePriv(two.toSubject(), AccessPrivilege.ADMIN);
    MembershipHooksImpl9.resetVariables();
    stem.grantPriv(two.toSubject(), NamingPrivilege.STEM);
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(stem.getUuid(), MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(FieldFinder.find("stemmers", true).getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
    
    // verify stem -> gThree (note the order of the membership adds is different from above)
    two.deleteMember(three.toSubject());
    MembershipHooksImpl9.resetVariables();
    two.addMember(three.toSubject());
    assertEquals(Membership.EFFECTIVE, MembershipHooksImpl9.ms.getType());
    assertEquals(1, MembershipHooksImpl9.ms.getDepth());
    assertEquals(null, MembershipHooksImpl9.ms.getOwnerGroupId());
    assertEquals(stem.getUuid(), MembershipHooksImpl9.ms.getOwnerStemId());
    assertEquals(three.toMember().getUuid(), MembershipHooksImpl9.ms.getMemberUuid());
    assertEquals(FieldFinder.find("stemmers", true).getUuid(), MembershipHooksImpl9.ms.getFieldId());
    assertEquals(two.getUuid(), MembershipHooksImpl9.ms.getViaGroupId());
    assertEquals(null, MembershipHooksImpl9.ms.getViaCompositeId());
  }
  
  /**
   * @throws Exception 
   */
  public void testEffectiveHooksWithoutComposites() throws Exception {
    
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl9.class);

    MembershipHooksImpl9.resetVariables();
    Stem stem = root.addChildStem("stem", "stem");
    Group top = stem.addChildGroup("top", "top");
    Group top1 = stem.addChildGroup("top1", "top1");
    Group top2 = stem.addChildGroup("top2", "top2");

    Group one = stem.addChildGroup("one", "one");
    Group two = stem.addChildGroup("two", "two");
    Group three = stem.addChildGroup("three", "three");
    
    
    
    three.addMember(SubjectFinder.findById("test.subject.0", true));
    three.addMember(SubjectFinder.findById("test.subject.1", true));
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
    
    two.addMember(three.toSubject());
    verifyandResetNumHookFires(2, 2, 2, 0, 0, 0, 0, 0, 0);
    
    one.addMember(two.toSubject());
    verifyandResetNumHookFires(3, 3, 3, 0, 0, 0, 0, 0, 0);

    top.addMember(top1.toSubject());
    top.addMember(top2.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    // test access privilege
    top1.grantPriv(one.toSubject(), AccessPrivilege.ADMIN);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);
    
    // test naming privilege
    stem.grantPriv(one.toSubject(), NamingPrivilege.STEM);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);

    // test custom list
    top2.addType(customType);
    top2.addMember(one.toSubject(), customField);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);

    // try adding and removing some memberships to make sure all combinations work
    one.addMember(SubjectFinder.findById("test.subject.3", true));
    verifyandResetNumHookFires(3, 3, 3, 0, 0, 0, 0, 0, 0);
    one.deleteMember(SubjectFinder.findById("test.subject.3", true));
    verifyandResetNumHookFires(0, 0, 0, 3, 3, 3, 0, 0, 0);

    // try removing and adding some memberships to make sure all combinations work
    three.deleteMember(SubjectFinder.findById("test.subject.0", true));
    verifyandResetNumHookFires(0, 0, 0, 5, 5, 5, 0, 0, 0);
    three.addMember(SubjectFinder.findById("test.subject.0", true));
    verifyandResetNumHookFires(5, 5, 5, 0, 0, 0, 0, 0, 0);
    
    two.deleteMember(three.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 14, 14, 14, 0, 0, 0);
    two.addMember(three.toSubject());
    verifyandResetNumHookFires(14, 14, 14, 0, 0, 0, 0, 0, 0);
    
    one.deleteMember(two.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 15, 15, 15, 0, 0, 0);
    one.addMember(two.toSubject());
    verifyandResetNumHookFires(15, 15, 15, 0, 0, 0, 0, 0, 0);
    
    top1.revokePriv(one.toSubject(), AccessPrivilege.ADMIN);
    verifyandResetNumHookFires(0, 0, 0, 4, 4, 4, 0, 0, 0);
    top1.grantPriv(one.toSubject(), AccessPrivilege.ADMIN);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);

    stem.revokePriv(one.toSubject(), NamingPrivilege.STEM);
    verifyandResetNumHookFires(0, 0, 0, 4, 4, 4, 0, 0, 0);
    stem.grantPriv(one.toSubject(), NamingPrivilege.STEM);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);
    
    top2.deleteMember(one.toSubject(), customField);
    verifyandResetNumHookFires(0, 0, 0, 4, 4, 4, 0, 0, 0);
    top2.addMember(one.toSubject(), customField);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);
    
    top.deleteMember(top1.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
    top.addMember(top1.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);

    top.deleteMember(top2.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
    top.addMember(top2.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 0, 0, 0, 0, 0, 0);
    
    // try disabling and re-enabling memberships.
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top1.getUuid(), one.toMember().getUuid(), FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(0, 0, 0, 4, 4, 4, 0, 0, 0);
    ms.setEnabled(true);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(4, 4, 4, 0, 0, 0, 0, 0, 0);
  }
  
  /**
   * @throws Exception 
   */
  public void testEffectiveHooksWithComposites() throws Exception {
    
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl9.class);

    MembershipHooksImpl9.resetVariables();
    Stem stem = root.addChildStem("stem", "stem");
    Group top1 = stem.addChildGroup("top1", "top1");
    Group top2 = stem.addChildGroup("top2", "top2");

    Group one = stem.addChildGroup("one", "one");
    Group two = stem.addChildGroup("two", "two");
    Group two_alt = stem.addChildGroup("two_alt", "two_alt");

    Group owner = stem.addChildGroup("owner", "owner");
    Group left = stem.addChildGroup("left", "left");
    Group right = stem.addChildGroup("right", "right");

    owner.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    left.addMember(one.toSubject());
    left.addMember(SubjectFinder.findById("test.subject.0", true));
    one.addMember(two.toSubject());
    one.addMember(SubjectFinder.findById("test.subject.1", true));
    two.addMember(SubjectFinder.findById("test.subject.2", true));
    
    top1.addMember(owner.toSubject());
    top2.grantPriv(owner.toSubject(), AccessPrivilege.ADMIN);
    stem.grantPriv(owner.toSubject(), NamingPrivilege.STEM);
    
    MembershipHooksImpl9.resetVariables();
    
    // try removing and adding some memberships to make sure all combinations work
    two.deleteMember(SubjectFinder.findById("test.subject.2", true));
    verifyandResetNumHookFires(0, 0, 0, 5, 5, 5, 0, 0, 0);
    two.addMember(SubjectFinder.findById("test.subject.2", true));
    verifyandResetNumHookFires(5, 5, 5, 0, 0, 0, 0, 0, 0);

    one.deleteMember(two.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 9, 9, 9, 0, 0, 0);
    one.addMember(two.toSubject());
    verifyandResetNumHookFires(9, 9, 9, 0, 0, 0, 0, 0, 0);
    
    left.deleteMember(one.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 15, 15, 15, 0, 0, 0);
    left.addMember(one.toSubject());
    verifyandResetNumHookFires(15, 15, 15, 0, 0, 0, 0, 0, 0);
    
    top1.deleteMember(owner.toSubject());
    verifyandResetNumHookFires(0, 0, 0, 5, 5, 5, 0, 0, 0);
    top1.addMember(owner.toSubject());
    verifyandResetNumHookFires(5, 5, 5, 0, 0, 0, 0, 0, 0);
    
    top2.revokePriv(owner.toSubject(), AccessPrivilege.ADMIN);
    verifyandResetNumHookFires(0, 0, 0, 5, 5, 5, 0, 0, 0);
    top2.grantPriv(owner.toSubject(), AccessPrivilege.ADMIN);
    verifyandResetNumHookFires(5, 5, 5, 0, 0, 0, 0, 0, 0);
    
    stem.revokePriv(owner.toSubject(), NamingPrivilege.STEM);
    verifyandResetNumHookFires(0, 0, 0, 5, 5, 5, 0, 0, 0);
    stem.grantPriv(owner.toSubject(), NamingPrivilege.STEM);
    verifyandResetNumHookFires(5, 5, 5, 0, 0, 0, 0, 0, 0);
    
    owner.deleteCompositeMember();
    verifyandResetNumHookFires(0, 0, 0, 15, 15, 15, 0, 0, 0);
    owner.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    verifyandResetNumHookFires(15, 15, 15, 0, 0, 0, 0, 0, 0);
   
    right.addMember(two.toSubject());
    verifyandResetNumHookFires(1, 1, 1, 6, 6, 6, 0, 0, 0);
    right.deleteMember(two.toSubject());
    verifyandResetNumHookFires(6, 6, 6, 1, 1, 1, 0, 0, 0);
    
    // try disabling and re-enabling memberships.
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        one.getUuid(), two.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(0, 0, 0, 8, 8, 8, 1, 1, 1);  // because of the way the grouper_membership_all_v view works, one of the effective memberships is disabled rather than deleted....
    ms.setEnabled(true);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(8, 8, 8, 0, 0, 0, 1, 1, 1);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        left.getUuid(), one.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(0, 0, 0, 15, 15, 15, 0, 0, 0);
    ms.setEnabled(true);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(15, 15, 15, 0, 0, 0, 0, 0, 0);
    
    // try a member rename
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        one.getUuid(), two.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setMemberUuid(two_alt.toMember().getUuid());
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    verifyandResetNumHookFires(3, 3, 3, 8, 8, 8, 1, 1, 1); // when members are changed, effective memberships are deleted and then the appropriate ones are re-added with the new member.  this keeps it simple but probably not best for performance...
  }
  

  /**
   * @param numPreInsert
   * @param numPostInsert
   * @param numPostCommitInsert
   * @param numPreDelete
   * @param numPostDelete
   * @param numPostCommitDelete
   * @param numPreUpdate
   * @param numPostUpdate
   * @param numPostCommitUpdate
   */
  private void verifyandResetNumHookFires(int numPreInsert, int numPostInsert, int numPostCommitInsert, 
      int numPreDelete, int numPostDelete, int numPostCommitDelete,
      int numPreUpdate, int numPostUpdate, int numPostCommitUpdate) {
    assertEquals(numPreInsert, MembershipHooksImpl9.numPreInsert);
    assertEquals(numPostInsert, MembershipHooksImpl9.numPostInsert);
    assertEquals(numPostCommitInsert, MembershipHooksImpl9.numPostCommitInsert);
    assertEquals(numPreDelete, MembershipHooksImpl9.numPreDelete);
    assertEquals(numPostDelete, MembershipHooksImpl9.numPostDelete);
    assertEquals(numPostCommitDelete, MembershipHooksImpl9.numPostCommitDelete);
    assertEquals(numPreUpdate, MembershipHooksImpl9.numPreUpdate);
    assertEquals(numPostUpdate, MembershipHooksImpl9.numPostUpdate);
    assertEquals(numPostCommitUpdate, MembershipHooksImpl9.numPostCommitUpdate);
    
    MembershipHooksImpl9.resetVariables();
  }
}