package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Regression test for the custom composite HQL injection (issue
 * advisory C2).  Hib3MembershipDAO used to concatenate the
 * custom composite group's id into HQL.  A group id can be assigned by the caller on create
 * (WS group save with a uuid), so an id such as <code>' or '1'='1</code> turned
 * "member of this group" into "member of any group".
 * <p>
 * Each test compares an empty group with a normal id against an empty group with a crafted
 * id.  Both are empty, so they must give the same result.  Before the fix the INTERSECTION
 * queries with the crafted group return memberships and the COMPLEMENT queries return none.
 * The three tests cover the three query builders: group owner, stem owner and attribute
 * definition owner memberships.
 * </p>
 */
public class Hib3MembershipDAOCustomCompositeTest extends GrouperTest {

  /** a group id with HQL syntax in it, the predicate-only payload from the report */
  private static final String CRAFTED_ID = "' or '1'='1";

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Hib3MembershipDAOCustomCompositeTest("testGroupOwnerCustomComposite"));
  }

  /**
   * @param name
   */
  public Hib3MembershipDAOCustomCompositeTest(String name) {
    super(name);
  }

  /** root session for the test */
  private GrouperSession grouperSession;

  /** empty custom composite group with a normal id */
  private Group normalEmpty;

  /** empty custom composite group with the crafted id */
  private Group craftedEmpty;

  /**
   * create the two empty custom composite groups, plus a group with SUBJ0 and SUBJ1 in it so
   * that "member of any group" is true for them
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    this.grouperSession = GrouperSession.startRootSession();

    this.normalEmpty = new GroupSave(this.grouperSession).assignName("test:normalEmpty")
        .assignCreateParentStemsIfNotExist(true).save();

    // same as the WS group save with a caller supplied uuid
    this.craftedEmpty = new GroupSave(this.grouperSession).assignName("test:craftedEmpty")
        .assignUuid(CRAFTED_ID).assignCreateParentStemsIfNotExist(true).save();
    assertEquals(CRAFTED_ID, this.craftedEmpty.getId());

    Group other = new GroupSave(this.grouperSession).assignName("test:other")
        .assignCreateParentStemsIfNotExist(true).save();
    other.addMember(SubjectTestHelper.SUBJ0, false);
    other.addMember(SubjectTestHelper.SUBJ1, false);
  }

  /**
   * stop the session
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * group owner memberships (findAllByGroupOwnerOptions)
   */
  public void testGroupOwnerCustomComposite() {
    Group target = new GroupSave(this.grouperSession).assignName("test:target")
        .assignCreateParentStemsIfNotExist(true).save();
    target.addMember(SubjectTestHelper.SUBJ0, false);
    target.addMember(SubjectTestHelper.SUBJ1, false);

    for (CompositeType compositeType : new CompositeType[] {CompositeType.INTERSECTION, CompositeType.COMPLEMENT}) {
      Set<String> normalResult = subjectIds(new MembershipFinder()
          .assignGroupIds(GrouperUtil.toSet(target.getId())).assignField(Group.getDefaultList())
          .assignCustomCompositeGroup(this.normalEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      Set<String> craftedResult = subjectIds(new MembershipFinder()
          .assignGroupIds(GrouperUtil.toSet(target.getId())).assignField(Group.getDefaultList())
          .assignCustomCompositeGroup(this.craftedEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      assertSameAsNormal(compositeType, normalResult, craftedResult);
    }
  }

  /**
   * stem owner memberships (findAllByStemOwnerOptions)
   */
  public void testStemOwnerCustomComposite() {
    Stem stem = new StemSave(this.grouperSession).assignName("test:stemA")
        .assignCreateParentStemsIfNotExist(true).save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);

    for (CompositeType compositeType : new CompositeType[] {CompositeType.INTERSECTION, CompositeType.COMPLEMENT}) {
      Set<String> normalResult = subjectIds(new MembershipFinder()
          .assignStemIds(GrouperUtil.toSet(stem.getId())).assignFieldType(FieldType.NAMING)
          .assignCustomCompositeGroup(this.normalEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      Set<String> craftedResult = subjectIds(new MembershipFinder()
          .assignStemIds(GrouperUtil.toSet(stem.getId())).assignFieldType(FieldType.NAMING)
          .assignCustomCompositeGroup(this.craftedEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      assertSameAsNormal(compositeType, normalResult, craftedResult);
    }
  }

  /**
   * attribute definition owner memberships (findAllByAttributeDefOwnerOptions)
   */
  public void testAttributeDefOwnerCustomComposite() {
    Stem stem = new StemSave(this.grouperSession).assignName("test:stemB")
        .assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDef = stem.addChildAttributeDef("attrDef", AttributeDefType.attr);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);

    for (CompositeType compositeType : new CompositeType[] {CompositeType.INTERSECTION, CompositeType.COMPLEMENT}) {
      Set<String> normalResult = subjectIds(new MembershipFinder()
          .assignAttributeDefIds(GrouperUtil.toSet(attributeDef.getId())).assignFieldType(FieldType.ATTRIBUTE_DEF)
          .assignCustomCompositeGroup(this.normalEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      Set<String> craftedResult = subjectIds(new MembershipFinder()
          .assignAttributeDefIds(GrouperUtil.toSet(attributeDef.getId())).assignFieldType(FieldType.ATTRIBUTE_DEF)
          .assignCustomCompositeGroup(this.craftedEmpty).assignCustomCompositeType(compositeType)
          .findMembershipsMembers());
      assertSameAsNormal(compositeType, normalResult, craftedResult);
    }
  }

  /**
   * both custom composite groups are empty: INTERSECTION must find nobody, COMPLEMENT must find
   * SUBJ0 and SUBJ1, and the crafted id must behave exactly like the normal one
   * @param compositeType
   * @param normalResult
   * @param craftedResult
   */
  private static void assertSameAsNormal(CompositeType compositeType, Set<String> normalResult, Set<String> craftedResult) {
    if (compositeType == CompositeType.INTERSECTION) {
      assertEquals("normal empty group, intersection", 0, normalResult.size());
      // before the fix the injected "or" matched SUBJ0 and SUBJ1 (members of other groups)
      assertEquals("crafted id must be data, not HQL: " + craftedResult, 0, craftedResult.size());
    } else {
      assertTrue("normal empty group, complement: " + normalResult, normalResult.contains(SubjectTestHelper.SUBJ0_ID));
      assertTrue("normal empty group, complement: " + normalResult, normalResult.contains(SubjectTestHelper.SUBJ1_ID));
    }
    assertEquals("crafted id must behave like a normal id, " + compositeType, normalResult, craftedResult);
  }

  /**
   * subject ids of the members in membership finder results (membership, owner, member)
   * @param results
   * @return the subject ids
   */
  private static Set<String> subjectIds(Set<Object[]> results) {
    Set<String> subjectIds = new HashSet<String>();
    for (Object[] row : GrouperUtil.nonNull(results)) {
      subjectIds.add(((Member)row[2]).getSubjectId());
    }
    return subjectIds;
  }

}
