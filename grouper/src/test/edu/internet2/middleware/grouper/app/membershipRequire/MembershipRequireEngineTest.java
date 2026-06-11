/**
 * 
 */
package edu.internet2.middleware.grouper.app.membershipRequire;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
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
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * @author mchyzer
 *
 */
public class MembershipRequireEngineTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new MembershipRequireEngineTest("testHook"));
  }
  
  /**
   * @param name
   */
  public MembershipRequireEngineTest(String name) {
    super(name);
  }

  public void testFull() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.hookEnable", "false");
    
    assertEquals(0, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave().assignName("test").assignCreateParentStemsIfNotExist(true).save();
    Stem appStem = new StemSave().assignName("app").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtected = new GroupSave().assignName("test:testPolicyGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Group testPolicyGroupUnprotected = new GroupSave().assignName("test:testPolicyGroupUnprotected").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtectedByStem = new GroupSave().assignName("app:testPolicyGroup2").assignCreateParentStemsIfNotExist(true).save();

    Group populationGroup = new GroupSave().assignName("test:populationGroup").assignCreateParentStemsIfNotExist(true).save();
    Group populationGroup2 = new GroupSave().assignName("test:populationGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    
    populationGroup.addMember(SubjectTestHelper.SUBJ2);
    populationGroup.addMember(SubjectTestHelper.SUBJ3);
    populationGroup.addMember(SubjectTestHelper.SUBJ4);
    populationGroup.addMember(SubjectTestHelper.SUBJ5);

    populationGroup2.addMember(SubjectTestHelper.SUBJ2);
    populationGroup2.addMember(SubjectTestHelper.SUBJ3);
    populationGroup2.addMember(SubjectTestHelper.SUBJ4);
    populationGroup2.addMember(SubjectTestHelper.SUBJ5);

    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ3);
    
    AttributeDef testPopulationDef = new AttributeDefSave(grouperSession).assignName("test:populationDef").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    testPopulationDef.getAttributeDefActionDelegate().configureActionList("assign");

    // couple names
    AttributeDefName testRequirePopulationGroup = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup").assignCreateParentStemsIfNotExist(true).save(); 
    AttributeDefName testRequirePopulationGroup2 = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup2").assignCreateParentStemsIfNotExist(true).save(); 

    //  # ui key to externalize text
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.uiKey$"}
    //  #grouper.membershipRequirement.someConfigId.uiKey = customVetoCompositeRequireEmployee
    //
    //  # attribute name that signifies this requirement
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.attributeName$"}
    //  #grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:customComposite:requireEmployee
    //
    //  # group name which is the population group
    //  # {valueType: "group", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.groupName$"}
    //  #grouper.membershipRequirement.someConfigId.requireGroupName = org:centralIt:staff:itStaff

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.uiKey", "testRequirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.attributeName", "test:requirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.requireGroupName", "test:populationGroup");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.uiKey", "testRequirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.attributeName", "test:requirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.requireGroupName", "test:populationGroup2");

    testPolicyGroupProtected.getAttributeDelegate().assignAttribute(testRequirePopulationGroup);
    appStem.getAttributeDelegate().assignAttribute(testRequirePopulationGroup2);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperMembershipRequireFull");

    assertEquals(2, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));
    assertEquals(1, GrouperUtil.intValue(
        new GcDbAccess().sql("select count(1) from grouper_mship_req_change where group_id = ? and member_id = ? and engine = 'F'")
        .addBindVar(testPolicyGroupProtected.getId()).addBindVar(member1.getId()).select(Integer.class)));
    assertEquals(1, GrouperUtil.intValue(
        new GcDbAccess().sql("select count(1) from grouper_mship_req_change where group_id = ? and member_id = ? and engine = 'F'")
        .addBindVar(testPolicyGroupProtectedByStem.getId()).addBindVar(member1.getId()).select(Integer.class)));
    
    Hib3GrouperLoaderLog hib3loaderLog = MembershipRequireFullSyncJob.internal_mostRecentHib3GrouperLoaderLog;
    
    assertEquals(2, GrouperUtil.intValue(hib3loaderLog.getDeleteCount()));

    assertEquals(2, testPolicyGroupProtectedByStem.getMembers().size());
    assertEquals(2, testPolicyGroupProtected.getMembers().size());
    assertEquals(3, testPolicyGroupUnprotected.getMembers().size());
    
    assertFalse(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(testPolicyGroupProtectedByStem.hasMember(SubjectTestHelper.SUBJ1));
    
  }
  
  public void testHook() {
    
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave().assignName("test").assignCreateParentStemsIfNotExist(true).save();
    Stem appStem = new StemSave().assignName("app").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtected = new GroupSave().assignName("test:testPolicyGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Group testPolicyGroupUnprotected = new GroupSave().assignName("test:testPolicyGroupUnprotected").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtectedByStem = new GroupSave().assignName("app:testPolicyGroup2").assignCreateParentStemsIfNotExist(true).save();

    Group populationGroup = new GroupSave().assignName("test:populationGroup").assignCreateParentStemsIfNotExist(true).save();
    Group populationGroup2 = new GroupSave().assignName("test:populationGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    populationGroup.addMember(SubjectTestHelper.SUBJ2);
    populationGroup.addMember(SubjectTestHelper.SUBJ3);
    populationGroup.addMember(SubjectTestHelper.SUBJ4);
    populationGroup.addMember(SubjectTestHelper.SUBJ5);

    populationGroup2.addMember(SubjectTestHelper.SUBJ2);
    populationGroup2.addMember(SubjectTestHelper.SUBJ3);
    populationGroup2.addMember(SubjectTestHelper.SUBJ4);
    populationGroup2.addMember(SubjectTestHelper.SUBJ5);

    
    AttributeDef testPopulationDef = new AttributeDefSave(grouperSession).assignName("test:populationDef").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    testPopulationDef.getAttributeDefActionDelegate().configureActionList("assign");

    // couple names
    AttributeDefName testRequirePopulationGroup = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup").assignCreateParentStemsIfNotExist(true).save(); 
    AttributeDefName testRequirePopulationGroup2 = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup2").assignCreateParentStemsIfNotExist(true).save(); 

    //  # ui key to externalize text
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.uiKey$"}
    //  #grouper.membershipRequirement.someConfigId.uiKey = customVetoCompositeRequireEmployee
    //
    //  # attribute name that signifies this requirement
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.attributeName$"}
    //  #grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:customComposite:requireEmployee
    //
    //  # group name which is the population group
    //  # {valueType: "group", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.groupName$"}
    //  #grouper.membershipRequirement.someConfigId.requireGroupName = org:centralIt:staff:itStaff

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.uiKey", "testRequirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.attributeName", "test:requirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.requireGroupName", "test:populationGroup");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.uiKey", "testRequirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.attributeName", "test:requirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.requireGroupName", "test:populationGroup2");

    testPolicyGroupProtected.getAttributeDelegate().assignAttribute(testRequirePopulationGroup);
    appStem.getAttributeDelegate().assignAttribute(testRequirePopulationGroup2);

    MembershipRequireEngine.clearCaches();

    try {
      testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ1);
      fail();
    } catch (HookVeto hv) {
      
    }
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ3);
    
    try {
      testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
      fail();
    } catch (HookVeto hv) {
      
    }
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ3);
    
    // remove the members and do again
    testPolicyGroupProtectedByStem.deleteMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtectedByStem.deleteMember(SubjectTestHelper.SUBJ3);
    testPolicyGroupProtected.deleteMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.deleteMember(SubjectTestHelper.SUBJ3);
    testPolicyGroupUnprotected.deleteMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupUnprotected.deleteMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupUnprotected.deleteMember(SubjectTestHelper.SUBJ3);
    
    // disable hook for one config
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.hookEnable", "false");
    MembershipRequireEngine.clearCaches();

    try {
      testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ1);
      fail();
    } catch (HookVeto hv) {
      
    }
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ3);
    
    // full sync should remove testPolicyGroupProtected -> SUBJ1
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperMembershipRequireFull");
    Hib3GrouperLoaderLog hib3loaderLog = MembershipRequireFullSyncJob.internal_mostRecentHib3GrouperLoaderLog;
    assertEquals(1, GrouperUtil.intValue(hib3loaderLog.getDeleteCount()));

    assertFalse(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ3));
  }

  /**
   * subjects from a source in sourceIdsToIgnore are exempt from the requirement in the hook.
   * test subjects are in source 'jdbc', GrouperSystem / GrouperAll are in source 'g:isa',
   * so those two sources stand in for e.g. people vs service principals
   */
  public void testSourceIdsToIgnoreHook() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    new StemSave().assignName("test").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtected = new GroupSave().assignName("test:testPolicyGroup").assignCreateParentStemsIfNotExist(true).save();

    Group populationGroup = new GroupSave().assignName("test:populationGroup").assignCreateParentStemsIfNotExist(true).save();

    populationGroup.addMember(SubjectTestHelper.SUBJ2);

    AttributeDef testPopulationDef = new AttributeDefSave(grouperSession).assignName("test:populationDef").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    testPopulationDef.getAttributeDefActionDelegate().configureActionList("assign");

    AttributeDefName testRequirePopulationGroup = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.uiKey", "testRequirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.attributeName", "test:requirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.requireGroupName", "test:populationGroup");

    // global exemption applies to all requirement configs
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.sourceIdsToIgnore", "g:isa");

    testPolicyGroupProtected.getAttributeDelegate().assignAttribute(testRequirePopulationGroup);

    MembershipRequireEngine.clearCaches();

    // not in the population group and not an exempt source: veto
    try {
      testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
      fail();
    } catch (HookVeto hv) {

    }

    // in the population group: ok
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);

    // not in the population group but the source is exempt: ok
    testPolicyGroupProtected.addMember(SubjectFinder.findRootSubject());

    // per config exemption replaces the global one: jdbc is now exempt, g:isa is not
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.sourceIdsToIgnore", "jdbc");
    MembershipRequireEngine.clearCaches();

    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);

    // GrouperSystem (g:isa) got in under the global exemption, but with the per config
    // override in place a fresh add of a g:isa subject is vetoed again
    testPolicyGroupProtected.deleteMember(SubjectFinder.findRootSubject());
    try {
      testPolicyGroupProtected.addMember(SubjectFinder.findRootSubject());
      fail();
    } catch (HookVeto hv) {

    }

  }

  /**
   * subjects from a source in sourceIdsToIgnore are not removed by the full sync daemon
   */
  public void testSourceIdsToIgnoreFullSync() {

    // let ineligible members in so the daemon has something to clean up
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.hookEnable", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    new StemSave().assignName("test").assignCreateParentStemsIfNotExist(true).save();

    Group testPolicyGroupProtected = new GroupSave().assignName("test:testPolicyGroup").assignCreateParentStemsIfNotExist(true).save();

    Group populationGroup = new GroupSave().assignName("test:populationGroup").assignCreateParentStemsIfNotExist(true).save();

    populationGroup.addMember(SubjectTestHelper.SUBJ2);

    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.addMember(SubjectFinder.findRootSubject());

    AttributeDef testPopulationDef = new AttributeDefSave(grouperSession).assignName("test:populationDef").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    testPopulationDef.getAttributeDefActionDelegate().configureActionList("assign");

    AttributeDefName testRequirePopulationGroup = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.uiKey", "testRequirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.attributeName", "test:requirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.requireGroupName", "test:populationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.sourceIdsToIgnore", "g:isa");

    testPolicyGroupProtected.getAttributeDelegate().assignAttribute(testRequirePopulationGroup);

    MembershipRequireEngine.clearCaches();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperMembershipRequireFull");

    Hib3GrouperLoaderLog hib3loaderLog = MembershipRequireFullSyncJob.internal_mostRecentHib3GrouperLoaderLog;

    // only the jdbc subject not in the population group is removed, the exempt g:isa subject stays
    assertEquals(1, GrouperUtil.intValue(hib3loaderLog.getDeleteCount()));

    assertFalse(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(testPolicyGroupProtected.hasMember(SubjectFinder.findRootSubject()));

  }

  public void testConsumer() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.hookEnable", "false");
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    assertEquals(0, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperMembershipRequireFull");

    Stem testStem = new StemSave().assignName("test").assignCreateParentStemsIfNotExist(true).save();
    Stem appStem = new StemSave().assignName("app").assignCreateParentStemsIfNotExist(true).save();
  
    Group testPolicyGroupProtected = new GroupSave().assignName("test:testPolicyGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Group testPolicyGroupUnprotected = new GroupSave().assignName("test:testPolicyGroupUnprotected").assignCreateParentStemsIfNotExist(true).save();
  
    Group testPolicyGroupProtectedByStem = new GroupSave().assignName("app:testPolicyGroup2").assignCreateParentStemsIfNotExist(true).save();
  
    Group populationGroup = new GroupSave().assignName("test:populationGroup").assignCreateParentStemsIfNotExist(true).save();
    Group populationGroup2 = new GroupSave().assignName("test:populationGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);

    populationGroup.addMember(SubjectTestHelper.SUBJ2);
    populationGroup.addMember(SubjectTestHelper.SUBJ3);
    populationGroup.addMember(SubjectTestHelper.SUBJ4);
    populationGroup.addMember(SubjectTestHelper.SUBJ5);
  
    populationGroup2.addMember(SubjectTestHelper.SUBJ2);
    populationGroup2.addMember(SubjectTestHelper.SUBJ3);
    populationGroup2.addMember(SubjectTestHelper.SUBJ4);
    populationGroup2.addMember(SubjectTestHelper.SUBJ5);
  
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtectedByStem.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupProtected.addMember(SubjectTestHelper.SUBJ3);
    
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ1);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ2);
    testPolicyGroupUnprotected.addMember(SubjectTestHelper.SUBJ3);
    
    AttributeDef testPopulationDef = new AttributeDefSave(grouperSession).assignName("test:populationDef").assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false).assignMultiValued(false).save();
    testPopulationDef.getAttributeDefActionDelegate().configureActionList("assign");
  
    // couple names
    AttributeDefName testRequirePopulationGroup = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup").assignCreateParentStemsIfNotExist(true).save(); 
    AttributeDefName testRequirePopulationGroup2 = new AttributeDefNameSave(grouperSession, testPopulationDef).assignName("test:requirePopulationGroup2").assignCreateParentStemsIfNotExist(true).save(); 
  
    //  # ui key to externalize text
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.uiKey$"}
    //  #grouper.membershipRequirement.someConfigId.uiKey = customVetoCompositeRequireEmployee
    //
    //  # attribute name that signifies this requirement
    //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.attributeName$"}
    //  #grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:customComposite:requireEmployee
    //
    //  # group name which is the population group
    //  # {valueType: "group", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.groupName$"}
    //  #grouper.membershipRequirement.someConfigId.requireGroupName = org:centralIt:staff:itStaff
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.uiKey", "testRequirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.attributeName", "test:requirePopulationGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.myConfig.requireGroupName", "test:populationGroup");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.uiKey", "testRequirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.attributeName", "test:requirePopulationGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.anotherConfig.requireGroupName", "test:populationGroup2");
  
    MembershipRequireEngine.clearCaches();

    // prime the pump
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_membershipRequire");
    GrouperUtil.sleep(2000);
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_membershipRequire");
        
    assertEquals(0, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));

    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount()));
  
    assertEquals(3, testPolicyGroupProtectedByStem.getMembers().size());
    assertEquals(3, testPolicyGroupProtected.getMembers().size());
    assertEquals(3, testPolicyGroupUnprotected.getMembers().size());
    
    assertTrue(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(testPolicyGroupProtectedByStem.hasMember(SubjectTestHelper.SUBJ1));
    
    testPolicyGroupProtected.getAttributeDelegate().assignAttribute(testRequirePopulationGroup);
    appStem.getAttributeDelegate().assignAttribute(testRequirePopulationGroup2);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_membershipRequire");
    GrouperUtil.sleep(2000);

    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_membershipRequire");

    assertEquals(2, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));
    assertEquals(1, GrouperUtil.intValue(
        new GcDbAccess().sql("select count(1) from grouper_mship_req_change where group_id = ? and member_id = ? and engine = 'C'")
        .addBindVar(testPolicyGroupProtected.getId()).addBindVar(member1.getId()).select(Integer.class)));
    assertEquals(1, GrouperUtil.intValue(
        new GcDbAccess().sql("select count(1) from grouper_mship_req_change where group_id = ? and member_id = ? and engine = 'C'")
        .addBindVar(testPolicyGroupProtectedByStem.getId()).addBindVar(member1.getId()).select(Integer.class)));

    assertEquals(2, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount()));
    
    assertEquals(2, testPolicyGroupProtectedByStem.getMembers().size());
    assertEquals(2, testPolicyGroupProtected.getMembers().size());
    assertEquals(3, testPolicyGroupUnprotected.getMembers().size());
    
    assertFalse(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(testPolicyGroupProtectedByStem.hasMember(SubjectTestHelper.SUBJ1));
    GrouperUtil.sleep(2000);

    // exercise the memberId scoped removal: drop SUBJ2 from the population group and let the
    // consumer clean it out of the protected group.  also configure an exempt source so the
    // sql has every bind variable segment at once: group name, member id, exempt source, require group
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.sourceIdsToIgnore", "g:isa");
    MembershipRequireEngine.clearCaches();

    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);

    populationGroup.deleteMember(SubjectTestHelper.SUBJ2);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_membershipRequire");
    GrouperUtil.sleep(2000);

    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_membershipRequire");

    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount()));

    assertEquals(3, GrouperUtil.intValue(new GcDbAccess().sql("select count(1) from grouper_mship_req_change").select(Integer.class)));
    assertEquals(1, GrouperUtil.intValue(
        new GcDbAccess().sql("select count(1) from grouper_mship_req_change where group_id = ? and member_id = ? and engine = 'C'")
        .addBindVar(testPolicyGroupProtected.getId()).addBindVar(member2.getId()).select(Integer.class)));

    // removed from the group requiring populationGroup, but stays in the stem protected
    // group since it is still in populationGroup2
    assertFalse(testPolicyGroupProtected.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(testPolicyGroupProtectedByStem.hasMember(SubjectTestHelper.SUBJ2));

  }
}
