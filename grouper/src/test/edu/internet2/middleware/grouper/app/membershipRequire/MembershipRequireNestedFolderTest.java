package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
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
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * A membership requirement assigned to a folder must apply to groups in every subfolder, whatever
 * is in the folder cache, and the full sync must clean up ineligible members of those nested
 * groups.  Before the fix: (1) when the requirement folder was already cached, the requirement
 * was not propagated to a subfolder, so the hook let an ineligible subject into a nested group;
 * (2) the full sync and change log only looked at groups directly in the requirement folder.
 */
public class MembershipRequireNestedFolderTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipRequireNestedFolderTest("testHookNestedGroupWarmCache"));
  }

  /**
   *
   */
  public MembershipRequireNestedFolderTest() {
    super();
  }

  /**
   * @param name
   */
  public MembershipRequireNestedFolderTest(String name) {
    super(name);
  }

  /** folder with the requirement */
  private Stem proofStem;

  /** group directly in the requirement folder */
  private Group warmupGroup;

  /** group one subfolder down */
  private Group nestedGroup;

  /** group in a sibling folder whose name starts the same, not protected */
  private Group siblingGroup;

  /**
   * set up: requirement test:mrProof needs membership in test:mrEligible, SUBJ1 eligible, SUBJ2 not
   * @param hookEnable
   */
  private void setupRequirement(boolean hookEnable) {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    this.proofStem = new StemSave(grouperSession).assignName("test:mrProof").assignCreateParentStemsIfNotExist(true).save();
    this.warmupGroup = new GroupSave(grouperSession).assignName("test:mrProof:warmup").assignCreateParentStemsIfNotExist(true).save();
    this.nestedGroup = new GroupSave(grouperSession).assignName("test:mrProof:child:protected").assignCreateParentStemsIfNotExist(true).save();
    this.siblingGroup = new GroupSave(grouperSession).assignName("test:mrProofX:grp").assignCreateParentStemsIfNotExist(true).save();

    // eligibility group is outside the protected folder
    Group eligibleGroup = new GroupSave(grouperSession).assignName("test:mrEligible").assignCreateParentStemsIfNotExist(true).save();
    eligibleGroup.addMember(SubjectTestHelper.SUBJ1);

    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:mrRequireDef").assignCreateParentStemsIfNotExist(true)
        .assignValueType(AttributeDefValueType.marker).assignToGroup(true).assignToStem(true)
        .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false).assignMultiValued(false).save();
    attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef)
        .assignName("test:mrRequireEligible").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.nestedProof.uiKey", "nestedProof");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.nestedProof.attributeName", "test:mrRequireEligible");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.nestedProof.requireGroupName", "test:mrEligible");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.nestedProof.hookEnable", hookEnable ? "true" : "false");

    this.proofStem.getAttributeDelegate().assignAttribute(attributeDefName);

    MembershipRequireEngine.clearCaches();
  }

  /**
   * add a member and return true if the hook vetoed it
   * @param group
   * @return true if vetoed
   */
  private static boolean addVetoed(Group group) {
    try {
      group.addMember(SubjectTestHelper.SUBJ2);
      return false;
    } catch (HookVeto hv) {
      return true;
    }
  }

  /**
   * config ids of beans.  MembershipRequireConfigBean has no equals(), and clearCaches() rebuilds
   * the beans as new instances, so compare by config id instead of by bean identity
   * @param beans
   * @return the config ids
   */
  private static Set<String> configIds(Set<MembershipRequireConfigBean> beans) {
    Set<String> result = new HashSet<String>();
    for (MembershipRequireConfigBean bean : GrouperUtil.nonNull(beans)) {
      result.add(bean.getConfigId());
    }
    return result;
  }

  /**
   * cold cache: the nested add is vetoed
   */
  public void testHookNestedGroupColdCache() {
    setupRequirement(true);
    assertTrue("nested group, cold cache", addVetoed(this.nestedGroup));
    assertTrue("direct group still protected", addVetoed(this.warmupGroup));
    assertFalse(this.nestedGroup.hasImmediateMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * warm cache: an add to the direct child group caches the requirement folder; the nested add
   * must still be vetoed (before the fix it succeeded)
   */
  public void testHookNestedGroupWarmCache() {
    setupRequirement(true);
    assertTrue("direct group", addVetoed(this.warmupGroup));
    assertTrue("nested group after the parent folder is cached", addVetoed(this.nestedGroup));
    assertFalse(this.nestedGroup.hasImmediateMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * the effective beans of a subfolder do not depend on cache order, and a lookup does not change
   * the cached entry of an ancestor
   */
  public void testStemBeansIndependentOfCache() {
    setupRequirement(true);

    // cold
    Set<MembershipRequireConfigBean> coldBeans = new HashSet<MembershipRequireConfigBean>(
        MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProof:child"));
    assertEquals(1, coldBeans.size());
    assertEquals(GrouperUtil.toSet("nestedProof"), configIds(coldBeans));

    // warm the parent first, then the child
    MembershipRequireEngine.clearCaches();
    Set<MembershipRequireConfigBean> parentBeans = new HashSet<MembershipRequireConfigBean>(
        MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProof"));
    assertEquals(1, parentBeans.size());
    Set<MembershipRequireConfigBean> warmBeans = new HashSet<MembershipRequireConfigBean>(
        MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProof:child"));
    assertEquals(configIds(coldBeans), configIds(warmBeans));

    // deeper, with a mix of cached and uncached ancestors
    Set<MembershipRequireConfigBean> deeperBeans = MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProof:child:a:b");
    assertEquals(configIds(coldBeans), configIds(deeperBeans));

    // the ancestor entry is unchanged (same cache generation as parentBeans, so same instances)
    assertEquals(parentBeans, new HashSet<MembershipRequireConfigBean>(
        MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProof")));

    // unrelated folder has nothing
    assertEquals(0, GrouperUtil.length(MembershipRequireEngine.stemNameToConfigBeanAssigned("test:mrProofX")));
  }

  /**
   * with the hook off, an ineligible nested member is removed by the full sync, and
   * groupsInStems returns the whole subtree but not a sibling folder
   */
  public void testFullSyncCleansNestedGroup() {
    setupRequirement(false);

    this.nestedGroup.addMember(SubjectTestHelper.SUBJ2);
    this.nestedGroup.addMember(SubjectTestHelper.SUBJ1);
    this.siblingGroup.addMember(SubjectTestHelper.SUBJ2);

    Set<String> groupNames = MembershipRequireEngine.groupsInStems(GrouperUtil.toSet("test:mrProof"));
    assertTrue(groupNames.toString(), groupNames.contains("test:mrProof:warmup"));
    assertTrue(groupNames.toString(), groupNames.contains("test:mrProof:child:protected"));
    assertFalse(groupNames.toString(), groupNames.contains("test:mrProofX:grp"));

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperMembershipRequireFull");

    assertFalse("ineligible nested member removed", this.nestedGroup.hasImmediateMember(SubjectTestHelper.SUBJ2));
    assertTrue("eligible nested member kept", this.nestedGroup.hasImmediateMember(SubjectTestHelper.SUBJ1));
    assertTrue("sibling folder untouched", this.siblingGroup.hasImmediateMember(SubjectTestHelper.SUBJ2));
  }

}
