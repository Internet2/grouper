package edu.internet2.middleware.grouper.abac;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

public class GrouperAbacScriptTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperAbacScriptTest("testSimpleAttributeAssignmentBoolean"));
  }

  public GrouperAbacScriptTest(String name) {
    super(name);
  }

  /**
   * max ABAC membership size tiers: highest applicable cap wins; default applies to users in no
   * tier; no config at all means unlimited (null).
   */
  public void testMaxAbacMembershipSizeForSubject() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    // nothing configured -> unlimited
    GrouperAbac.clearCaches();
    assertNull(GrouperAbac.maxAbacMembershipSizeForSubject(testSubject0));

    // two tier groups; subj0 in both, subj1 in medium only, subj2 in neither
    Group largeEditors = new GroupSave().assignName("test:largeEditors").assignCreateParentStemsIfNotExist(true).save();
    Group mediumEditors = new GroupSave().assignName("test:mediumEditors").assignCreateParentStemsIfNotExist(true).save();
    largeEditors.addMember(testSubject0);
    mediumEditors.addMember(testSubject0);
    mediumEditors.addMember(testSubject1);

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.maxMembershipSizeLimit.0.groupName").value("test:largeEditors").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.maxMembershipSizeLimit.0.maxSize").value("200000").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.maxMembershipSizeLimit.1.groupName").value("test:mediumEditors").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.maxMembershipSizeLimit.1.maxSize").value("20000").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.defaultMaxMembershipSizeLimit").value("2000").store();
    GrouperAbac.clearCaches();

    // subj0 is in both tiers -> highest wins
    assertEquals(Integer.valueOf(200000), GrouperAbac.maxAbacMembershipSizeForSubject(testSubject0));
    // subj1 is in the medium tier only
    assertEquals(Integer.valueOf(20000), GrouperAbac.maxAbacMembershipSizeForSubject(testSubject1));
    // subj2 is in no tier -> base default
    assertEquals(Integer.valueOf(2000), GrouperAbac.maxAbacMembershipSizeForSubject(testSubject2));

    // remove the base default -> users in no tier are unlimited again
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.defaultMaxMembershipSizeLimit").value("").store();
    GrouperAbac.clearCaches();
    assertNull(GrouperAbac.maxAbacMembershipSizeForSubject(testSubject2));
    // but a tier member still gets their tier cap
    assertEquals(Integer.valueOf(20000), GrouperAbac.maxAbacMembershipSizeForSubject(testSubject1));
  }

  public void testSimpleAttributeAssignmentBoolean() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('active')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentString() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('org', '123')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));

  }

  public void testSimpleAttributeGreaterThan() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeGreaterThan('jobNumber', 300)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));

  }

  public void testSimpleAttributeLessThan() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLessThan('jobNumber', 300)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeLessThanOrEqual() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLessThanOrEqual('jobNumber', 234)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeGreaterThanOrEqual() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeGreaterThanOrEqual('jobNumber', 456)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));

  }

  public void testSimpleHasAttributeAssignmentString() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('org')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));

  }

  public void testSimpleAttributeAssignmentStringArrayAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeAny('org', ['123', '234'])");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentStringLike() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLike(org, '%2%')");

    //  test.subject.0
    //  test.subject.2
    //  test.subject.1
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentNumberArrayAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeAny('jobNumber', [123, 234])");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentStringRegex() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeRegex(org, '^.*2.*$')");

    //  test.subject.0
    //  test.subject.2
    //  test.subject.1
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentStringIdentifier() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute(org, '123')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));

  }

  public void testRowAttributeAssignmentString() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode == staff')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));

    int exists = new GcDbAccess().sql("""
        select count(1)
        from grouper_data_row_field_asgn_v gdrfav1,
        grouper_data_row_field_asgn_v gdrfav2
        where gdrfav1.data_row_assign_internal_id = gdrfav2.data_row_assign_internal_id
        and gdrfav1.data_field_config_id = 'affiliationCode'
        and gdrfav1.value_text = 'emer'
        and gdrfav2.data_field_config_id = 'affiliationOrg'
        and gdrfav2.value_text is null
        """).select(Integer.class);

    assertEquals(0, exists);


  }

  public void testRowAttributeLessThan() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber<300')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeLessThanOrEqual() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber<=468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeGreaterThanOrEqual() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>=468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testRowAttributeGreaterThanOrEqualNegative() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>=-468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeGreaterThan() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(1, members.size());

    assertTrue(members.contains(member2));

  }

  public void testRowAttributeEqualNumber() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber == 468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(1, members.size());

    assertTrue(members.contains(member1));

  }

  public void testRowAttributeNotEqualNumber() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber != 468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeNotEqualNegativeNumber() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber != -468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeEqualNegativeNumber() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber == -468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> members = testGroup.getMembers();
    assertEquals(0, members.size());


  }

  public void testRowAttributeAssignmentStringOneEquals() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode==staff')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));

  }

  public void testRowAttributeAssignmentStringAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode =~ [staff, fac, alum]')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  /**
   * affiliationCode !~ [staff, fac, alum] -- "not in list". Per-row semantics, mirroring !=:
   * matches a member who has an affiliation row whose code is NOT in the list.
   * sub0 rows staff,alum (both in list) -> no; sub1 rows stu,contr (neither in list) -> yes;
   * sub2 row staff (in list) -> no; sub3 rows fac,emer (emer not in list) -> yes.
   */
  public void testRowAttributeAssignmentStringNotAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode !~ [staff, fac, alum]')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeAssignmentStringLike() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeLike(affiliationCode, '%f%')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  public void testRowAttributeAssignmentStringRegex() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeRegex(affiliationCode, '^.*f.*$')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  /**
   * GRP-6828: ordering comparisons on string fields should throw an exception
   */
  public void testRowAttributeStringLessThanThrowsException() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession.startRootSession();

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    try {
      GrouperLoaderJexlScriptFullSync.analyzeJexlScript(
          grouperDataEngine,
          "entity.hasRow('affiliation', \"affiliationOrg <= 'math'\")");
      fail("Expected exception for ordering comparison on string field");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Ordering comparisons"));
      assertTrue(e.getMessage().contains("hasAttributeBetween"));
    }
  }

  /**
   * GRP-6828: test hasAttributeBetween on string field in hasRow (inclusive both ends)
   * 'engl' <= affiliationOrg, affiliationOrg <= 'math' matches engl, math => subjects 0,3
   */
  public void testRowAttributeStringBetween() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeBetween('engl' <= affiliationOrg, affiliationOrg <= 'math')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member3));
  }

  /**
   * GRP-6828: test hasAttributeBetween on string field with exclusive lower bound
   * 'comp' < affiliationOrg, affiliationOrg <= 'math' matches engl, math (not comp) => subjects 0,3
   */
  public void testRowAttributeStringBetweenExclusiveLower() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeBetween('comp' < affiliationOrg, affiliationOrg <= 'math')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member3));
  }

  /**
   * GRP-6828: test hasAttributeBetween on integer field in hasRow (inclusive both ends)
   * 200 <= affiliationDeptNumber, affiliationDeptNumber <= 468 => rows with 246(subj0), 468(subj1), 246(subj3) => subjects 0,1,3
   */
  public void testRowAttributeIntegerBetween() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'hasAttributeBetween(200 <= affiliationDeptNumber, affiliationDeptNumber <= 468)')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
  }

  /**
   * attributeCompare(affiliationCode == affiliationCodePrimary)
   * sub0: staff==staff match, sub1: stu==stu match, sub2: staff!=fac no, sub3: fac==fac match
   */
  public void testAttributeCompareStringEquals() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    String script = "entity.hasRow('affiliation', 'attributeCompare(affiliationCode == affiliationCodePrimary)')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // test analyze
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, null, true);
    assertNull(analysis.getErrorMessage());

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
  }

  /**
   * attributeCompare(affiliationCode != affiliationCodePrimary)
   * all subjects have at least one row where code != codePrimary
   */
  public void testAttributeCompareStringNotEquals() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    String script = "entity.hasRow('affiliation', 'attributeCompare(affiliationCode != affiliationCodePrimary)')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // test analyze
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, null, true);
    assertNull(analysis.getErrorMessage());

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
  }

  /**
   * attributeCompare(affiliationDeptNumber == affiliationDeptNumberPrimary)
   * sub0: 135==135 yes, sub1: 468==468 yes (null==null is false in SQL), sub2: 579!=580 no, sub3: 135==135 yes
   */
  public void testAttributeCompareIntegerEquals() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    String script = "entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber == affiliationDeptNumberPrimary)')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // test analyze
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, null, true);
    assertNull(analysis.getErrorMessage());

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
  }

  /**
   * attributeCompare(affiliationDeptNumber < affiliationDeptNumberPrimary)
   * sub0: 135<135 no, 246<200 no; sub1: null no, 468<468 no; sub2: 579<580 yes; sub3: 135<135 no, 246<100 no
   */
  public void testAttributeCompareIntegerLessThan() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    String script = "entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber < affiliationDeptNumberPrimary)')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // test analyze
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, null, true);
    assertNull(analysis.getErrorMessage());

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(1, members.size());

    assertTrue(members.contains(member2));
  }

  /**
   * attributeCompare(affiliationDeptNumber - 1 <= affiliationDeptNumberPrimary)
   * sub0: 134<=135 yes; sub1: 467<=468 yes; sub2: 578<=580 yes; sub3: 134<=135 yes
   */
  public void testAttributeCompareIntegerWithMath() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    String script = "entity.hasRow('affiliation', 'attributeCompare(affiliationDeptNumber - 1 <= affiliationDeptNumberPrimary)')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // test analyze
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, null, true);
    assertNull(analysis.getErrorMessage());

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
  }

  /**
   * test that analysis warns about unresolvable/deleted subjects matching the script
   */
  public void testAnalysisUnresolvableSubjectsWarning() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    // this script matches test.subject.0 and test.subject.2 (the ones with active=true)
    String script = "entity.hasAttribute('active')";
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // first, analyze with all subjects resolvable - no warning expected
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    Subject loggedInSubject = grouperSession.getSubject();
    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, loggedInSubject, true);
    assertNull(analysis.getErrorMessage());
    assertNull(analysis.getWarningMessage());

    // mark test.subject.0 as unresolvable in grouper_members
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    new GcDbAccess().sql("update grouper_members set subject_resolution_resolvable = 'F' where id = ?")
        .addBindVar(member0.getId()).executeSql();

    // analyze again - should now warn about 1 unresolvable subject
    analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, loggedInSubject, true);
    assertNull(analysis.getErrorMessage());
    assertNotNull(analysis.getWarningMessage());
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("1"));
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("test.subject.0"));

    // also mark test.subject.2 as deleted
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    new GcDbAccess().sql("update grouper_members set subject_resolution_deleted = 'T' where id = ?")
        .addBindVar(member2.getId()).executeSql();

    // analyze again - should now warn about 2 unresolvable/deleted subjects
    analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, null, loggedInSubject, true);
    assertNull(analysis.getErrorMessage());
    assertNotNull(analysis.getWarningMessage());
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("2"));
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("test.subject.0"));
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("test.subject.2"));
  }

}
