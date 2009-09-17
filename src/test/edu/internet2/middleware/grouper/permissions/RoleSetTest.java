/**
 * 
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class RoleSetTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RoleSetTest("testComplexRemoveBfromA"));
  }

  /**
   * 
   */
  public RoleSetTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public RoleSetTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }

  /**
   * role def
   */
  public void testHibernate() {
    Role role = this.top.addChildRole("test", "test");
    Role role2 = this.top.addChildRole("test22", "test2");

    RoleSet roleSet = new RoleSet();
    roleSet.setId(GrouperUuid.getUuid());
    roleSet.setParentRoleSetId(roleSet.getId());
    roleSet.setDepth(1);
    roleSet.setIfHasRoleId(role.getId());
    roleSet.setThenHasRoleId(role2.getId());
    roleSet.setType(RoleHierarchyType.immediate);
    roleSet.saveOrUpdate();

  }

  /**
   * <pre>
   * complex relationships: ^ means relationship pointing up, v means down -> means right
   * e.g. if someone has A, then that someone also effectively has B.  
   * So B is in the roleSet of A, 
   * as is C, D, E, F, G, H, I, J, and L (not K)
   * 
   *          K       G---\ 
   *           \     ^     \
   *            \   /       \
   *             v /         \
   *              C       L   \
   *             ^ \     ^    |
   *            /   \   /     |
   *           /     v /      v
   * A -----> B       E ----> F
   * |\        \     ^       ^
   * | \        \   /       /
   * |  \        v /       /
   * |   \        D       J
   * |    \              ^|
   * |     \            / |
   * v      v          /  |
   * H----> I --------/   |
   *  ^                  /
   *   \                /
   *    \--------------/ 
   *     
   *     
   * So the immediate relationships are:
   * A -> B
   * A -> H
   * A -> I
   * B -> C
   * B -> D
   * C -> E
   * C -> G
   * D -> E
   * E -> F
   * E -> L
   * G -> F
   * H -> I
   * I -> J
   * J -> H
   * J -> F
   * K -> C
   *  
   * </pre>
   */
  public void setupStructure() {
  
    //TODO add constraint
  
    int initialRoleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    Role orgA = this.top.addChildRole("orgA",
        "orgA");
  
    int roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 1, roleSetViewCount);
  
    Role orgB = this.top.addChildRole( "orgB",
        "orgB");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 2, roleSetViewCount);
  
    // A -> B
    assertTrue(orgA.addToRoleSet(orgB));
    assertFalse(orgA.addToRoleSet(orgB));
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 3, roleSetViewCount);
  
    // A -> H
    Role orgH = this.top.addChildRole( "orgH",
        "orgH");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 4, roleSetViewCount);
  
    orgA.addToRoleSet(orgH);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 5, roleSetViewCount);
  
    // A -> I
    Role orgI = this.top.addChildRole( "orgI",
        "orgI");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 6, roleSetViewCount);
  
    orgA.addToRoleSet(orgI);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 7, roleSetViewCount);
  
    // orgC
    Role orgC = this.top.addChildRole( "orgC",
        "orgC");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 8, roleSetViewCount);
  
    // B -> C
    orgB.addToRoleSet(orgC);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    // B->C, A->C
    assertEquals(initialRoleSetViewCount + 10, roleSetViewCount);
  
    Role orgD = this.top.addChildRole( "orgD",
        "orgD");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 11, roleSetViewCount);
  
    // B -> D
    orgB.addToRoleSet(orgD);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    // B->D, A->D
    assertEquals(initialRoleSetViewCount + 13, roleSetViewCount);
  
    Role orgE = this.top.addChildRole( "orgE",
        "orgE");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 14, roleSetViewCount);
  
    // C -> E
    orgC.addToRoleSet(orgE);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds C->E, B->E, A->E
    assertEquals(initialRoleSetViewCount + 17, roleSetViewCount);
  
    Role orgG = this.top.addChildRole( "orgG",
        "orgG");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 18, roleSetViewCount);
  
    // C -> G
    orgC.addToRoleSet(orgG);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds C->G, B->G, A->G
    assertEquals(initialRoleSetViewCount + 21, roleSetViewCount);
  
    // D -> E
    orgD.addToRoleSet(orgE);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds D->E, B->E, A->E
    assertEquals(initialRoleSetViewCount + 24, roleSetViewCount);
  
    Role orgF = this.top.addChildRole( "orgF",
        "orgF");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 25, roleSetViewCount);
  
    // E -> F
    orgE.addToRoleSet(orgF);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds E->F, C->F, D->F, B->F (x2, two parents), A->F (x2, two parents)
    assertEquals(initialRoleSetViewCount + 32, roleSetViewCount);
  
    Role orgL = this.top.addChildRole( "orgL",
        "orgL");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 33, roleSetViewCount);
  
    // E -> L
    orgE.addToRoleSet(orgL);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds E->L, C->L, D->L, B->L (x2), A->L (x2)
    assertEquals(initialRoleSetViewCount + 40, roleSetViewCount);
  
    // G -> F
    orgG.addToRoleSet(orgF);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds G->F, C->F, B->F, A->F)
    assertEquals(initialRoleSetViewCount + 44, roleSetViewCount);
  
    // H -> I
    orgH.addToRoleSet(orgI);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds H->I, A->I
    assertEquals(initialRoleSetViewCount + 46, roleSetViewCount);
  
    Role orgJ = this.top.addChildRole( "orgJ",
        "orgJ");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 47, roleSetViewCount);
  
    // I -> J
    orgI.addToRoleSet(orgJ);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds I->J, H->J, A->J (x2)
    assertEquals(initialRoleSetViewCount + 51, roleSetViewCount);
  
    // J -> F
    orgJ.addToRoleSet(orgF);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds J->F, I->F, H->F, A->F (x2)
    assertEquals(initialRoleSetViewCount + 56, roleSetViewCount);
  
    // J -> H
    orgJ.addToRoleSet(orgH);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds J->H, A->H, J->I, I->H
    assertEquals(initialRoleSetViewCount + 60, roleSetViewCount);
  
    Role orgK = this.top.addChildRole( "orgK",
        "orgK");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 61, roleSetViewCount);
  
    // K -> C
    orgK.addToRoleSet(orgC);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    //adds K->C, K->G, K->F, K->E, K->L, K->F
    assertEquals(initialRoleSetViewCount + 67, roleSetViewCount);
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveBfromA() {
    setupStructure();
    Role orgA = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgA", true);
    Role orgB = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgB", true);
    orgA.removeFromRoleSet(orgB);
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgB", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgD", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index = 5;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note, there are two E's since there are two paths to it
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note there are two of these since two A->E's
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveCfromB() {
    setupStructure();
    Role orgB = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgB", true);
    Role orgC = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgC", true);
    assertFalse(orgC.removeFromRoleSet(orgB));
    assertTrue(orgB.removeFromRoleSet(orgC));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgC", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgE", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgC", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgE", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
    public void testComplexRemoveCfromK() {
      setupStructure();
      Role orgK = GrouperDAOFactory.getFactory().getRole()
          .findByName("top:orgK", true);
      Role orgC = GrouperDAOFactory.getFactory().getRole()
          .findByName("top:orgC", true);
      assertTrue(orgK.removeFromRoleSet(orgC));
      //lets look at them all
      List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
          GrouperDAOFactory.getFactory()
          .getRoleSetView().findByRoleSetViews(
          GrouperUtil.toSet(
          "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
          "top:orgG", "top:orgH",
          "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
      int index = 0;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgA", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgB", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgC", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgD", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note, there are two E's since there are two paths to it
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note, there are two A->J's
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgG", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgH", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgH", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgI", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgI", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note there are two of these since two A->E's
      assertEquals("top:orgA", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(4, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgB", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgC", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgD", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //two of these since two B->E's
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgG", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgB", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgC", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgG", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgC", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgD", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgD", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgD", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgD", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgD", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgE", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgE", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgE", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgE", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgF", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgG", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgG", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgG", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgH", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(3, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgH", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgH", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgH", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgI", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgH", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgI", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgI", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgH", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgI", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgI", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgI", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgF", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgH", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(1, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.immediate, roleSetViews
          .get(index).getType());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgI", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(2, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.effective, roleSetViews
          .get(index).getType());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgJ", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("top:orgK", roleSetViews.get(index)
  //        .getIfHasRoleName());
  //    assertEquals("top:orgC", roleSetViews.get(index)
  //        .getThenHasRoleName());
  //    assertEquals(1, roleSetViews.get(index).getDepth());
  //    assertEquals(RoleHierarchyType.immediate, roleSetViews
  //        .get(index).getType());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("top:orgK", roleSetViews.get(index)
  //        .getIfHasRoleName());
  //    assertEquals("top:orgE", roleSetViews.get(index)
  //        .getThenHasRoleName());
  //    assertEquals(2, roleSetViews.get(index).getDepth());
  //    assertEquals(RoleHierarchyType.effective, roleSetViews
  //        .get(index).getType());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
  //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("top:orgK", roleSetViews.get(index)
  //        .getIfHasRoleName());
  //    assertEquals("top:orgF", roleSetViews.get(index)
  //        .getThenHasRoleName());
  //    assertEquals(3, roleSetViews.get(index).getDepth());
  //    assertEquals(RoleHierarchyType.effective, roleSetViews
  //        .get(index).getType());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
  //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("top:orgK", roleSetViews.get(index)
  //        .getIfHasRoleName());
  //    assertEquals("top:orgF", roleSetViews.get(index)
  //        .getThenHasRoleName());
  //    assertEquals(3, roleSetViews.get(index).getDepth());
  //    assertEquals(RoleHierarchyType.effective, roleSetViews
  //        .get(index).getType());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
  //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("top:orgK", roleSetViews.get(index)
  //        .getIfHasRoleName());
  //    assertEquals("top:orgG", roleSetViews.get(index)
  //        .getThenHasRoleName());
  //    assertEquals(2, roleSetViews.get(index).getDepth());
  //    assertEquals(RoleHierarchyType.effective, roleSetViews
  //        .get(index).getType());
  //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
  //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgK", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgK", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
      //      index++;
      //
      //      assertEquals("top:orgK", roleSetViews.get(index)
      //          .getIfHasRoleName());
      //      assertEquals("top:orgL", roleSetViews.get(index)
      //          .getThenHasRoleName());
      //      assertEquals(3, roleSetViews.get(index).getDepth());
      //      assertEquals(RoleHierarchyType.effective, roleSetViews
      //          .get(index).getType());
      //      assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
      //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("top:orgL", roleSetViews.get(index)
          .getIfHasRoleName());
      assertEquals("top:orgL", roleSetViews.get(index)
          .getThenHasRoleName());
      assertEquals(0, roleSetViews.get(index).getDepth());
      assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
          .getType());
      assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
      assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
    }

  /**
     * 
     */
  public void testComplexRemoveDfromB() {
    setupStructure();
    Role orgB = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgB", true);
    Role orgD = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgD", true);
    assertFalse(orgD.removeFromRoleSet(orgB));
    assertTrue(orgB.removeFromRoleSet(orgD));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgD", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgE", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(4, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgL", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(4, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgD", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(1, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.immediate, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgE", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgL", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveEfromC() {
    setupStructure();
    Role orgC = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgC", true);
    Role orgE = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgE", true);
    assertTrue(orgC.removeFromRoleSet(orgE));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgE", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(4, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgL", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(4, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgB", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgE", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgB", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgB", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgL", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgC", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgE", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(1, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.immediate, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgC", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgC", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgL", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgE", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
     * 
     */
  public void testComplexRemoveEfromD() {
    setupStructure();
    Role orgD = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgD", true);
    Role orgE = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgE", true);
    assertTrue(orgD.removeFromRoleSet(orgE));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgE", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(4, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      //note there are two of these since two A->E's
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgL", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(4, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgE", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
    //
    //      index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgL", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgD", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgE", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(1, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.immediate, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgD", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("top:orgD", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgL", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveFfromE() {
    setupStructure();
    Role orgE = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgE", true);
    Role orgF = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgF", true);
    assertTrue(orgE.removeFromRoleSet(orgF));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(4, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        //note, there are two E's since there are two paths to it
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(4, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgB", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
    //
    //        index++;
    //
    //        //two of these since two B->E's
    //        assertEquals("top:orgB", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgC", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgD", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgE", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(1, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.immediate, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgK", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
     * 
     */
  public void testComplexRemoveFfromG() {
    setupStructure();
    Role orgG = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgG", true);
    Role orgF = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgF", true);
    assertTrue(orgG.removeFromRoleSet(orgF));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgA", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(4, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgB", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgC", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgG", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(1, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.immediate, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("top:orgK", roleSetViews.get(index)
    //          .getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index)
    //          .getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveFfromJ() {
    setupStructure();
    Role orgJ = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgJ", true);
    Role orgF = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgF", true);
    assertTrue(orgJ.removeFromRoleSet(orgF));
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveGfromC() {
    setupStructure();
    Role orgC = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgC", true);
    Role orgG = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgG", true);
    assertTrue(orgC.removeFromRoleSet(orgG));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgC", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgC", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgG", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveHfromA() {
    setupStructure();
    Role orgA = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgA", true);
    Role orgH = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgH", true);
    orgA.removeFromRoleSet(orgH);
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveHfromJ() {
    setupStructure();
    Role orgJ = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgJ", true);
    Role orgH = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgH", true);
    assertTrue(orgJ.removeFromRoleSet(orgH));
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
    //
    //    index++;
    //
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveIfromA() {
    setupStructure();
    Role orgA = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgA", true);
    Role orgI = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgI", true);
    orgA.removeFromRoleSet(orgI);
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //      assertEquals("top:orgF", roleSetViews.get(index).getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //      assertEquals("top:orgH", roleSetViews.get(index).getThenHasRoleName());
    //      assertEquals(3, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //      assertEquals("top:orgI", roleSetViews.get(index).getThenHasRoleName());
    //      assertEquals(1, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.immediate, roleSetViews.get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("top:orgA", roleSetViews.get(index).getIfHasRoleName());
    //      assertEquals("top:orgJ", roleSetViews.get(index).getThenHasRoleName());
    //      assertEquals(2, roleSetViews.get(index).getDepth());
    //      assertEquals(RoleHierarchyType.effective, roleSetViews.get(index).getType());
    //      assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveIfromH() {
    setupStructure();
    Role orgH = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgH", true);
    Role orgI = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgI", true);
    assertTrue(orgH.removeFromRoleSet(orgI));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        //note, there are two A->J's
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(4, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgI", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgA", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgJ", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgH", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgF", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(3, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgH", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgI", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(1, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.immediate, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgH", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgJ", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("top:orgJ", roleSetViews.get(index)
    //            .getIfHasRoleName());
    //        assertEquals("top:orgI", roleSetViews.get(index)
    //            .getThenHasRoleName());
    //        assertEquals(2, roleSetViews.get(index).getDepth());
    //        assertEquals(RoleHierarchyType.effective, roleSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveJfromI() {
    setupStructure();
    Role orgI = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgI", true);
    Role orgJ = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgJ", true);
    assertTrue(orgI.removeFromRoleSet(orgJ));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgF", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgH", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgI", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgJ", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveLfromE() {
    setupStructure();
    Role orgE = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgE", true);
    Role orgL = GrouperDAOFactory.getFactory().getRole()
        .findByName("top:orgL", true);
    assertTrue(orgE.removeFromRoleSet(orgL));
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));
  
    int index = 0;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgA", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(4, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgA", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note there are two of these since two A->E's
    //    assertEquals("top:orgA", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(4, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgB", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgB", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgB", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgC", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgC", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgD", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgD", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgD", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(2, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgD", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgE", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgE", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(1, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.immediate, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgF", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgF", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgG", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgG", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgH", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgH", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgI", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgI", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgH", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgI", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgJ", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgC", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(1, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgE", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgF", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(3, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgG", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(2, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews
        .get(index).getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgK", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgK", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", roleSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("top:orgK", roleSetViews.get(index)
    //        .getIfHasRoleName());
    //    assertEquals("top:orgL", roleSetViews.get(index)
    //        .getThenHasRoleName());
    //    assertEquals(3, roleSetViews.get(index).getDepth());
    //    assertEquals(RoleHierarchyType.effective, roleSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", roleSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", roleSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("top:orgL", roleSetViews.get(index)
        .getIfHasRoleName());
    assertEquals("top:orgL", roleSetViews.get(index)
        .getThenHasRoleName());
    assertEquals(0, roleSetViews.get(index).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(index)
        .getType());
    assertEquals("top:orgL", roleSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", roleSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * <pre>
   * complex relationships ( ^ means relationship pointing up, v means down -> means right
   * e.g. if has A, then has B.  So B is in the roleibuteSet of A
   * 
   * 1 -----> 2       4 
   *           \     ^
   *            \   /
   *             v /
   *              3
   *
   * So the immediate relationships are:
   * 1 -> 2
   * 2 -> 3
   * 3 -> 4
   */
  public void testSetLogic() {
  
    int initialRoleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    Role org1 = this.top.addChildRole( "org1",
        "org1");
  
    int roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 1, roleSetViewCount);
  
    //lets make sure one record was created
    RoleSet roleSet = HibernateSession.byHqlStatic().createQuery(
        "from RoleSet")
        .uniqueResult(RoleSet.class);
  
    assertEquals(0, roleSet.getDepth());
    assertEquals(org1.getId(), roleSet.getIfHasRoleId());
    assertEquals(org1.getId(), roleSet.getThenHasRoleId());
    assertEquals(RoleHierarchyType.self, roleSet.getType());
    assertEquals(roleSet.getId(), roleSet
        .getParentRoleSetId());
  
    Role org2 = this.top.addChildRole( "org2",
        "org2");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 2, roleSetViewCount);
  
    org1.addToRoleSet(org2);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 3, roleSetViewCount);
  
    Role org3 = this.top.addChildRole( "org3",
        "org3");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 4, roleSetViewCount);
  
    Role org4 = this.top.addChildRole( "org4",
        "org4");
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 5, roleSetViewCount);
  
    org3.addToRoleSet(org4);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 6, roleSetViewCount);
  
    //connect the branches
    org2.addToRoleSet(org3);
  
    roleSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_role_set_v");
  
    assertEquals(initialRoleSetViewCount + 10, roleSetViewCount);
  
    //lets look at them all
    List<RoleSetView> roleSetViews = new ArrayList<RoleSetView>(
        GrouperDAOFactory.getFactory()
        .getRoleSetView().findByRoleSetViews(
        GrouperUtil.toSet("top:org1", "top:org2", "top:org3", "top:org4")));
  
    assertEquals("top:org1", roleSetViews.get(0).getIfHasRoleName());
    assertEquals("top:org1", roleSetViews.get(0).getThenHasRoleName());
    assertEquals(0, roleSetViews.get(0).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(0)
        .getType());
    assertEquals("top:org1", roleSetViews.get(0).getParentIfHasName());
    assertEquals("top:org1", roleSetViews.get(0).getParentThenHasName());
  
    assertEquals("top:org1", roleSetViews.get(1).getIfHasRoleName());
    assertEquals("top:org2", roleSetViews.get(1).getThenHasRoleName());
    assertEquals(1, roleSetViews.get(1).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews.get(1)
        .getType());
    assertEquals("top:org1", roleSetViews.get(1).getParentIfHasName());
    assertEquals("top:org1", roleSetViews.get(1).getParentThenHasName());
  
    assertEquals("top:org1", roleSetViews.get(2).getIfHasRoleName());
    assertEquals("top:org3", roleSetViews.get(2).getThenHasRoleName());
    assertEquals(2, roleSetViews.get(2).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews.get(2)
        .getType());
    assertEquals("top:org1", roleSetViews.get(2).getParentIfHasName());
    assertEquals("top:org2", roleSetViews.get(2).getParentThenHasName());
  
    assertEquals("top:org1", roleSetViews.get(3).getIfHasRoleName());
    assertEquals("top:org4", roleSetViews.get(3).getThenHasRoleName());
    assertEquals(3, roleSetViews.get(3).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews.get(3)
        .getType());
    assertEquals(
        roleSetViews.get(3).getParentIfHasName() + " -> "
        + roleSetViews.get(3).getParentThenHasName(),
        "top:org1", roleSetViews.get(3).getParentIfHasName());
    assertEquals(roleSetViews.get(3).getParentIfHasName() + " -> "
        + roleSetViews.get(3).getParentThenHasName(),
        "top:org3", roleSetViews.get(3).getParentThenHasName());
  
    assertEquals("top:org2", roleSetViews.get(4).getIfHasRoleName());
    assertEquals("top:org2", roleSetViews.get(4).getThenHasRoleName());
    assertEquals(0, roleSetViews.get(4).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(4)
        .getType());
    assertEquals("top:org2", roleSetViews.get(4).getParentIfHasName());
    assertEquals("top:org2", roleSetViews.get(4).getParentThenHasName());
  
    assertEquals("top:org2", roleSetViews.get(5).getIfHasRoleName());
    assertEquals("top:org3", roleSetViews.get(5).getThenHasRoleName());
    assertEquals(1, roleSetViews.get(5).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews.get(5)
        .getType());
    assertEquals("top:org2", roleSetViews.get(5).getParentIfHasName());
    assertEquals("top:org2", roleSetViews.get(5).getParentThenHasName());
  
    assertEquals("top:org2", roleSetViews.get(6).getIfHasRoleName());
    assertEquals("top:org4", roleSetViews.get(6).getThenHasRoleName());
    assertEquals(2, roleSetViews.get(6).getDepth());
    assertEquals(RoleHierarchyType.effective, roleSetViews.get(6)
        .getType());
    assertEquals("top:org2", roleSetViews.get(6).getParentIfHasName());
    assertEquals("top:org3", roleSetViews.get(6).getParentThenHasName());
  
    assertEquals("top:org3", roleSetViews.get(7).getIfHasRoleName());
    assertEquals("top:org3", roleSetViews.get(7).getThenHasRoleName());
    assertEquals(0, roleSetViews.get(7).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(7)
        .getType());
    assertEquals("top:org3", roleSetViews.get(7).getParentIfHasName());
    assertEquals("top:org3", roleSetViews.get(7).getParentThenHasName());
  
    assertEquals("top:org3", roleSetViews.get(8).getIfHasRoleName());
    assertEquals("top:org4", roleSetViews.get(8).getThenHasRoleName());
    assertEquals(1, roleSetViews.get(8).getDepth());
    assertEquals(RoleHierarchyType.immediate, roleSetViews.get(8)
        .getType());
    assertEquals("top:org3", roleSetViews.get(8).getParentIfHasName());
    assertEquals("top:org3", roleSetViews.get(8).getParentThenHasName());
  
    assertEquals("top:org4", roleSetViews.get(9).getIfHasRoleName());
    assertEquals("top:org4", roleSetViews.get(9).getThenHasRoleName());
    assertEquals(0, roleSetViews.get(9).getDepth());
    assertEquals(RoleHierarchyType.self, roleSetViews.get(9)
        .getType());
    assertEquals("top:org4", roleSetViews.get(9).getParentIfHasName());
    assertEquals("top:org4", roleSetViews.get(9).getParentThenHasName());
  
  }

}
