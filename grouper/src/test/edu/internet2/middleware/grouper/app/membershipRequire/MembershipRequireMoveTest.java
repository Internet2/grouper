package edu.internet2.middleware.grouper.app.membershipRequire;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupCopy;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import junit.textui.TestRunner;

/**
 * A populated group that is moved,
 * or a folder whose groups are moved, or a group copied with its members, into a folder with a
 * membership requirement must have its ineligible members removed by the membership-requirement
 * change-log consumer (CHANGE_LOG_consumer_membershipRequire), not only at the next daily full
 * sync.  The consumer now gets GROUP_ADD and GROUP_UPDATE events (elfilter in
 * grouper-loader.base.properties) and MembershipRequireEsbListener cleans the group.
 * <p>
 * Setup: requirement moveProof (marker test:mrMoveRequire on folder test:mrMoveProof, eligibility
 * group test:mrMoveEligible with SUBJ1; SUBJ2 not eligible).  The hook is off for this
 * requirement so only the consumer path is tested.  Groups outside the folder hold SUBJ1 and
 * SUBJ2.
 * </p>
 * Before the fix each test fails: SUBJ2 is still a member after the consumer runs.
 */
public class MembershipRequireMoveTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipRequireMoveTest("testGroupMove"));
  }

  /**
   *
   */
  public MembershipRequireMoveTest() {
    super();
  }

  /**
   * @param name
   */
  public MembershipRequireMoveTest(String name) {
    super(name);
  }

  /** root session */
  private GrouperSession grouperSession;

  /** folder with the requirement */
  private Stem proofStem;

  /**
   * requirement on test:mrMoveProof, hook off, then run the consumer once so later passes only
   * see the move
   */
  @Override
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();

    this.proofStem = new StemSave(this.grouperSession).assignName("test:mrMoveProof").assignCreateParentStemsIfNotExist(true).save();

    // eligibility group is outside the protected folder
    Group eligibleGroup = new GroupSave(this.grouperSession).assignName("test:mrMoveEligible").assignCreateParentStemsIfNotExist(true).save();
    eligibleGroup.addMember(SubjectTestHelper.SUBJ1);

    AttributeDef attributeDef = new AttributeDefSave(this.grouperSession).assignName("test:mrMoveRequireDef").assignCreateParentStemsIfNotExist(true)
        .assignValueType(AttributeDefValueType.marker).assignToGroup(true).assignToStem(true)
        .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).save();
    attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
    AttributeDefName attributeDefName = new AttributeDefNameSave(this.grouperSession, attributeDef)
        .assignName("test:mrMoveRequire").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.moveProof.uiKey", "moveProof");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.moveProof.attributeName", "test:mrMoveRequire");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.moveProof.requireGroupName", "test:mrMoveEligible");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.moveProof.hookEnable", "false");

    this.proofStem.getAttributeDelegate().assignAttribute(attributeDefName);

    MembershipRequireEngine.clearCaches();

    // process everything so far (the requirement assignment); the folder is empty, nothing removed
    runConsumer();
  }

  /**
   * remove the config overrides
   */
  @Override
  public void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.moveProof.uiKey");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.moveProof.attributeName");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.moveProof.requireGroupName");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.moveProof.hookEnable");
    MembershipRequireEngine.clearCaches();
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * move the change log into the change log table and run the membership require consumer, the
   * same path the daemon runs every minute
   */
  private static void runConsumer() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_membershipRequire");
  }

  /**
   * a group with SUBJ1 and SUBJ2
   * @param name
   * @return the group
   */
  private Group populatedGroup(String name) {
    Group group = new GroupSave(this.grouperSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ2);
    return group;
  }

  /**
   * after the consumer, the group at its new name has SUBJ1 and not SUBJ2
   * @param label
   * @param groupName
   */
  private void assertCleaned(String label, String groupName) {
    Group group = GroupFinder.findByName(this.grouperSession, groupName, true);
    assertTrue(label + ": eligible member removed", group.hasImmediateMember(SubjectTestHelper.SUBJ1));
    assertFalse(label + ": ineligible member still there after the consumer ran", group.hasImmediateMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * move a populated group into the requirement folder
   */
  public void testGroupMove() {
    Group group = populatedGroup("test:mrMoveOutside:moved");
    runConsumer();
    // still outside: untouched
    assertTrue(group.hasImmediateMember(SubjectTestHelper.SUBJ2));

    group.move(this.proofStem);
    runConsumer();

    assertCleaned("group move", "test:mrMoveProof:moved");
  }

  /**
   * move a folder holding a populated group (in a subfolder) under the requirement folder; the
   * move renames the groups, which is what the consumer sees
   */
  public void testFolderMove() {
    populatedGroup("test:mrMoveFolder:sub:grp");
    runConsumer();

    Stem folder = StemFinder.findByName(this.grouperSession, "test:mrMoveFolder", true);
    folder.move(this.proofStem);
    runConsumer();

    assertCleaned("folder move", "test:mrMoveProof:mrMoveFolder:sub:grp");
  }

  /**
   * copy a populated group with its members into the requirement folder (GROUP_ADD)
   */
  public void testGroupCopy() {
    Group group = populatedGroup("test:mrMoveCopySource:grp");
    runConsumer();

    new GroupCopy(group, this.proofStem).copyListMembersOfGroup(true).save();
    runConsumer();

    assertCleaned("group copy", "test:mrMoveProof:grp");
    // the source outside the folder keeps its members
    assertTrue(GroupFinder.findByName(this.grouperSession, "test:mrMoveCopySource:grp", true).hasImmediateMember(SubjectTestHelper.SUBJ2));
  }

}
