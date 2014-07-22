/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 * $Id: AttributeAssignActionSetTest.java,v 1.3 2009-11-08 13:07:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AttributeAssignActionSetTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new AttributeAssignActionSetTest("testHibernate"));
    TestRunner.run(new AttributeAssignActionSetTest("testSetLogic"));
    //TestRunner.run(AttributeAssignActionSetTest.class);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  /** root stem */
  private Stem root;
  /** top stem */
  private Stem top;
  /** attribute def */
  private AttributeDef attributeDef;
  /**
   * 
   * @param name
   */
  public AttributeAssignActionSetTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testHibernate() {

    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setAttributeDefId(attributeDef.getId());

    attributeAssignAction.save();
    
    AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
    attributeAssignActionSet.setId(GrouperUuid.getUuid());
    
    attributeAssignActionSet.setIfHasAttrAssignActionId(attributeAssignAction.getId());
    attributeAssignActionSet.setThenHasAttrAssignActionId(attributeAssignAction.getId());
    attributeAssignActionSet.saveOrUpdate();
    
    attributeAssignActionSet = GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .findById(attributeAssignActionSet.getId(), true);
    
    attributeAssignActionSet.delete();

    attributeAssignActionSet = GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .findById(attributeAssignActionSet.getId(), false);
    
    assertNull(attributeAssignActionSet);
  }

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
   * <pre>
   * complex relationships: ^ means relationship pointing up, v means down -> means right
   * e.g. if someone has A, then that someone also effectively has B.  
   * So B is in the attributeSet of A, 
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
  
    int initialAttrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    this.attributeDef = this.top.addChildAttributeDef("orgs",
        AttributeDefType.attr);
    this.attributeDef.getAttributeDefActionDelegate().configureActionList("orgA");
    AttributeAssignAction orgA = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgA", true);
  
    int attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 1, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgB = this.attributeDef.getAttributeDefActionDelegate().addAction("orgB");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 2, attrAssignActionSetViewCount);
  
    // A -> B
    assertTrue(orgA.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgB));
    assertFalse(orgA.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgB));
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 3, attrAssignActionSetViewCount);
  
    // A -> H
    AttributeAssignAction orgH = this.attributeDef.getAttributeDefActionDelegate().addAction("orgH");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 4, attrAssignActionSetViewCount);
  
    orgA.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgH);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 5, attrAssignActionSetViewCount);
  
    // A -> I
    AttributeAssignAction orgI = this.attributeDef.getAttributeDefActionDelegate().addAction("orgI");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 6, attrAssignActionSetViewCount);
  
    orgA.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgI);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 7, attrAssignActionSetViewCount);
  
    // orgC
    AttributeAssignAction orgC = this.attributeDef.getAttributeDefActionDelegate().addAction("orgC");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 8, attrAssignActionSetViewCount);
  
    // B -> C
    orgB.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgC);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    // B->C, A->C
    assertEquals(initialAttrAssignActionSetViewCount + 10, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgD = this.attributeDef.getAttributeDefActionDelegate().addAction("orgD");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 11, attrAssignActionSetViewCount);
  
    // B -> D
    orgB.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgD);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    // B->D, A->D
    assertEquals(initialAttrAssignActionSetViewCount + 13, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgE = this.attributeDef.getAttributeDefActionDelegate().addAction("orgE");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 14, attrAssignActionSetViewCount);
  
    // C -> E
    orgC.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgE);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds C->E, B->E, A->E
    assertEquals(initialAttrAssignActionSetViewCount + 17, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgG = this.attributeDef.getAttributeDefActionDelegate().addAction("orgG");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 18, attrAssignActionSetViewCount);
  
    // C -> G
    orgC.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgG);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds C->G, B->G, A->G
    assertEquals(initialAttrAssignActionSetViewCount + 21, attrAssignActionSetViewCount);
  
    // D -> E
    orgD.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgE);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds D->E, B->E, A->E
    assertEquals(initialAttrAssignActionSetViewCount + 24, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgF = this.attributeDef.getAttributeDefActionDelegate().addAction("orgF");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 25, attrAssignActionSetViewCount);
  
    // E -> F
    orgE.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgF);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds E->F, C->F, D->F, B->F (x2, two parents), A->F (x2, two parents)
    assertEquals(initialAttrAssignActionSetViewCount + 32, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgL = this.attributeDef.getAttributeDefActionDelegate().addAction("orgL");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 33, attrAssignActionSetViewCount);
  
    // E -> L
    orgE.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgL);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds E->L, C->L, D->L, B->L (x2), A->L (x2)
    assertEquals(initialAttrAssignActionSetViewCount + 40, attrAssignActionSetViewCount);
  
    // G -> F
    orgG.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgF);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds G->F, C->F, B->F, A->F)
    assertEquals(initialAttrAssignActionSetViewCount + 44, attrAssignActionSetViewCount);
  
    // H -> I
    orgH.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgI);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds H->I, A->I
    assertEquals(initialAttrAssignActionSetViewCount + 46, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgJ = this.attributeDef.getAttributeDefActionDelegate().addAction("orgJ");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 47, attrAssignActionSetViewCount);
  
    // I -> J
    orgI.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgJ);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds I->J, H->J, A->J (x2)
    assertEquals(initialAttrAssignActionSetViewCount + 51, attrAssignActionSetViewCount);
  
    // J -> F
    orgJ.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgF);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds J->F, I->F, H->F, A->F (x2)
    assertEquals(initialAttrAssignActionSetViewCount + 56, attrAssignActionSetViewCount);
  
    // J -> H
    orgJ.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgH);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds J->H, A->H, J->I, I->H
    assertEquals(initialAttrAssignActionSetViewCount + 60, attrAssignActionSetViewCount);
  
    AttributeAssignAction orgK = this.attributeDef.getAttributeDefActionDelegate().addAction("orgK");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 61, attrAssignActionSetViewCount);
  
    // K -> C
    orgK.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(orgC);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    //adds K->C, K->G, K->F, K->E, K->L, K->F
    assertEquals(initialAttrAssignActionSetViewCount + 67, attrAssignActionSetViewCount);
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveBfromA() {
    setupStructure();
    AttributeAssignAction orgA = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgA", true);
    AttributeAssignAction orgB = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgB", true);
    orgA.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgB);
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgD", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index = 5;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note, there are two E's since there are two paths to it
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note there are two of these since two A->E's
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveCfromB() {
    setupStructure();
    AttributeAssignAction orgB = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgB", true);
    AttributeAssignAction orgC = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgC", true);
    assertFalse(orgC.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgB));
    assertTrue(orgB.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgC));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
    public void testComplexRemoveCfromK() {
      setupStructure();
      AttributeAssignAction orgK = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgK", true);
      AttributeAssignAction orgC = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgC", true);
      assertTrue(orgK.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgC));
      //lets look at them all
      List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
          GrouperDAOFactory.getFactory()
          .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
          GrouperUtil.toSet(
          "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
          "orgG", "orgH",
          "orgI", "orgJ", "orgK", "orgL")));
  
      int index = 0;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note, there are two E's since there are two paths to it
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note, there are two A->J's
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //note there are two of these since two A->E's
      assertEquals("orgA", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      //two of these since two B->E's
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgB", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgC", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgD", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgE", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgF", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgI", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
          .get(index).getType());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
  //        .getIfHasAttrDefNameName());
  //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
  //        .getThenHasAttrDefNameName());
  //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
  //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
  //        .get(index).getType());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
  //        .getIfHasAttrDefNameName());
  //    assertEquals("orgE", attributeAssignActionSetViews.get(index)
  //        .getThenHasAttrDefNameName());
  //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
  //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
  //        .get(index).getType());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
  //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
  //        .getIfHasAttrDefNameName());
  //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
  //        .getThenHasAttrDefNameName());
  //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
  //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
  //        .get(index).getType());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
  //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
  //        .getIfHasAttrDefNameName());
  //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
  //        .getThenHasAttrDefNameName());
  //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
  //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
  //        .get(index).getType());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
  //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  //    index++;
  //
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
  //        .getIfHasAttrDefNameName());
  //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
  //        .getThenHasAttrDefNameName());
  //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
  //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
  //        .get(index).getType());
  //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
  //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgK", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgK", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      //      index++;
      //
      //      assertEquals("orgK", attributeAssignActionSetViews.get(index)
      //          .getIfHasAttrDefNameName());
      //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
      //          .getThenHasAttrDefNameName());
      //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
      //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
      //          .get(index).getType());
      //      assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
      //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
      index++;
  
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getIfHasAttrAssignActionName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index)
          .getThenHasAttrAssignActionName());
      assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
      assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
          .getType());
      assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
      assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    }

  /**
     * 
     */
  public void testComplexRemoveDfromB() {
    setupStructure();
    AttributeAssignAction orgB = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgB", true);
    AttributeAssignAction orgD = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgD", true);
    assertFalse(orgD.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgB));
    assertTrue(orgB.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgD));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveEfromC() {
    setupStructure();
    AttributeAssignAction orgC = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgC", true);
    AttributeAssignAction orgE = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgE", true);
    assertTrue(orgC.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgE));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
     * 
     */
  public void testComplexRemoveEfromD() {
    setupStructure();
    AttributeAssignAction orgD = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgD", true);
    AttributeAssignAction orgE = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgE", true);
    assertTrue(orgD.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgE));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      //note there are two of these since two A->E's
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
    //
    //      index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveFfromE() {
    setupStructure();
    AttributeAssignAction orgE = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgE", true);
    AttributeAssignAction orgF = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgF", true);
    assertTrue(orgE.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgF));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        //note, there are two E's since there are two paths to it
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
    //
    //        index++;
    //
    //        //two of these since two B->E's
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
     * 
     */
  public void testComplexRemoveFfromG() {
    setupStructure();
    AttributeAssignAction orgG = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgG", true);
    AttributeAssignAction orgF = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgF", true);
    assertTrue(orgG.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgF));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //
    //      assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //          .get(index).getType());
    //      assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveFfromJ() {
    setupStructure();
    AttributeAssignAction orgJ = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgJ", true);
    AttributeAssignAction orgF = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgF", true);
    assertTrue(orgJ.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgF));
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveGfromC() {
    setupStructure();
    AttributeAssignAction orgC = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgC", true);
    AttributeAssignAction orgG = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgG", true);
    assertTrue(orgC.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgG));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgG", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveHfromA() {
    setupStructure();
    AttributeAssignAction orgA = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgA", true);
    AttributeAssignAction orgH = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgH", true);
    orgA.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgH);
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    //note, there are two A->J's
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //    
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveHfromJ() {
    setupStructure();
    AttributeAssignAction orgJ = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgJ", true);
    AttributeAssignAction orgH = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgH", true);
    assertTrue(orgJ.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgH));
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
    //
    //    index++;
    //
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveIfromA() {
    setupStructure();
    AttributeAssignAction orgA = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgA", true);
    AttributeAssignAction orgI = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgI", true);
    orgA.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgI);
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("orgF", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("orgH", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("orgI", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //      index++;
    //      
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("orgJ", attributeAssignActionSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //      assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(index).getType());
    //      assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //      assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
       * 
       */
  public void testComplexRemoveIfromH() {
    setupStructure();
    AttributeAssignAction orgH = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgH", true);
    AttributeAssignAction orgI = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgI", true);
    assertTrue(orgH.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgI));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        //note, there are two A->J's
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //        index++;
    //
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //        assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //            .get(index).getType());
    //        assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //        assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveJfromI() {
    setupStructure();
    AttributeAssignAction orgI = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgI", true);
    AttributeAssignAction orgJ = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgJ", true);
    assertTrue(orgI.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgJ));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note there are two of these since two A->E's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgF", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgH", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * 
   */
  public void testComplexRemoveLfromE() {
    setupStructure();
    AttributeAssignAction orgE = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgE", true);
    AttributeAssignAction orgL = this.attributeDef.getAttributeDefActionDelegate().allowedAction("orgL", true);
    assertTrue(orgE.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(orgL));
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet(
        "orgA", "orgB", "orgC", "orgD", "orgE", "orgF",
        "orgG", "orgH",
        "orgI", "orgJ", "orgK", "orgL")));
  
    int index = 0;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two E's since there are two paths to it
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //note, there are two A->J's
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgA", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    //note there are two of these since two A->E's
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgA", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    //two of these since two B->E's
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgB", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgB", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgD", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgD", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgD", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgI", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgH", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgJ", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgF", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgG", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
        .get(index).getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgC", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    //    index++;
    //
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("orgL", attributeAssignActionSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeAssignActionSetViews.get(index).getDepth());
    //    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews
    //        .get(index).getType());
    //    assertEquals("orgK", attributeAssignActionSetViews.get(index).getParentIfHasName());
    //    assertEquals("orgE", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
    index++;
  
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getIfHasAttrAssignActionName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index)
        .getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(index).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(index)
        .getType());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentIfHasName());
    assertEquals("orgL", attributeAssignActionSetViews.get(index).getParentThenHasName());
  
  }

  /**
   * <pre>
   * complex relationships ( ^ means relationship pointing up, v means down -> means right
   * e.g. if has A, then has B.  So B is in the attributeSet of A
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
  
    int initialAttrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    AttributeDef attributeDef = this.top.addChildAttributeDef("orgs",
        AttributeDefType.attr);
    attributeDef.getAttributeDefActionDelegate().configureActionList("org1");
    AttributeAssignAction org1 = attributeDef.getAttributeDefActionDelegate().allowedAction("org1", true);
  
    int attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 1, attrAssignActionSetViewCount);
  
    //lets make sure one record was created
    AttributeAssignActionSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignActionSet where ifHasAttrAssignActionId = :theIfHasAttrAssignActionId ")
        .setString("theIfHasAttrAssignActionId",  org1.getId())
        .uniqueResult(AttributeAssignActionSet.class);
  
    assertEquals(0, attributeDefNameSet.getDepth());
    assertEquals(org1.getId(), attributeDefNameSet.getIfHasAttrAssignActionId());
    assertEquals(org1.getId(), attributeDefNameSet.getThenHasAttrAssignActionId());
    assertEquals(AttributeAssignActionType.self, attributeDefNameSet.getType());
    assertEquals(attributeDefNameSet.getId(), attributeDefNameSet
        .getParentAttrAssignActionSetId());
  
    AttributeAssignAction org2 = attributeDef.getAttributeDefActionDelegate().addAction("org2");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 2, attrAssignActionSetViewCount);
  
    org1.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(org2);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 3, attrAssignActionSetViewCount);
  
    AttributeAssignAction org3 = attributeDef.getAttributeDefActionDelegate().addAction("org3");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 4, attrAssignActionSetViewCount);
  
    AttributeAssignAction org4 = attributeDef.getAttributeDefActionDelegate().addAction("org4");
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 5, attrAssignActionSetViewCount);
  
    org3.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(org4);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 6, attrAssignActionSetViewCount);
  
    //connect the branches
    org2.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(org3);
  
    attrAssignActionSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_assn_action_set_v");
  
    assertEquals(initialAttrAssignActionSetViewCount + 10, attrAssignActionSetViewCount);
  
    //lets look at them all
    List<AttributeAssignActionSetView> attributeAssignActionSetViews = new ArrayList<AttributeAssignActionSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeAssignActionSetView().findByAttributeAssignActionSetViews(
        GrouperUtil.toSet("org1", "org2", "org3", "org4")));
  
    assertEquals("org1", attributeAssignActionSetViews.get(0).getIfHasAttrAssignActionName());
    assertEquals("org1", attributeAssignActionSetViews.get(0).getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(0).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(0)
        .getType());
    assertEquals("org1", attributeAssignActionSetViews.get(0).getParentIfHasName());
    assertEquals("org1", attributeAssignActionSetViews.get(0).getParentThenHasName());
  
    assertEquals("org1", attributeAssignActionSetViews.get(1).getIfHasAttrAssignActionName());
    assertEquals("org2", attributeAssignActionSetViews.get(1).getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(1).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(1)
        .getType());
    assertEquals("org1", attributeAssignActionSetViews.get(1).getParentIfHasName());
    assertEquals("org1", attributeAssignActionSetViews.get(1).getParentThenHasName());
  
    assertEquals("org1", attributeAssignActionSetViews.get(2).getIfHasAttrAssignActionName());
    assertEquals("org3", attributeAssignActionSetViews.get(2).getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(2).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(2)
        .getType());
    assertEquals("org1", attributeAssignActionSetViews.get(2).getParentIfHasName());
    assertEquals("org2", attributeAssignActionSetViews.get(2).getParentThenHasName());
  
    assertEquals("org1", attributeAssignActionSetViews.get(3).getIfHasAttrAssignActionName());
    assertEquals("org4", attributeAssignActionSetViews.get(3).getThenHasAttrAssignActionName());
    assertEquals(3, attributeAssignActionSetViews.get(3).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(3)
        .getType());
    assertEquals(
        attributeAssignActionSetViews.get(3).getParentIfHasName() + " -> "
        + attributeAssignActionSetViews.get(3).getParentThenHasName(),
        "org1", attributeAssignActionSetViews.get(3).getParentIfHasName());
    assertEquals(attributeAssignActionSetViews.get(3).getParentIfHasName() + " -> "
        + attributeAssignActionSetViews.get(3).getParentThenHasName(),
        "org3", attributeAssignActionSetViews.get(3).getParentThenHasName());
  
    assertEquals("org2", attributeAssignActionSetViews.get(4).getIfHasAttrAssignActionName());
    assertEquals("org2", attributeAssignActionSetViews.get(4).getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(4).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(4)
        .getType());
    assertEquals("org2", attributeAssignActionSetViews.get(4).getParentIfHasName());
    assertEquals("org2", attributeAssignActionSetViews.get(4).getParentThenHasName());
  
    assertEquals("org2", attributeAssignActionSetViews.get(5).getIfHasAttrAssignActionName());
    assertEquals("org3", attributeAssignActionSetViews.get(5).getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(5).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(5)
        .getType());
    assertEquals("org2", attributeAssignActionSetViews.get(5).getParentIfHasName());
    assertEquals("org2", attributeAssignActionSetViews.get(5).getParentThenHasName());
  
    assertEquals("org2", attributeAssignActionSetViews.get(6).getIfHasAttrAssignActionName());
    assertEquals("org4", attributeAssignActionSetViews.get(6).getThenHasAttrAssignActionName());
    assertEquals(2, attributeAssignActionSetViews.get(6).getDepth());
    assertEquals(AttributeAssignActionType.effective, attributeAssignActionSetViews.get(6)
        .getType());
    assertEquals("org2", attributeAssignActionSetViews.get(6).getParentIfHasName());
    assertEquals("org3", attributeAssignActionSetViews.get(6).getParentThenHasName());
  
    assertEquals("org3", attributeAssignActionSetViews.get(7).getIfHasAttrAssignActionName());
    assertEquals("org3", attributeAssignActionSetViews.get(7).getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(7).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(7)
        .getType());
    assertEquals("org3", attributeAssignActionSetViews.get(7).getParentIfHasName());
    assertEquals("org3", attributeAssignActionSetViews.get(7).getParentThenHasName());
  
    assertEquals("org3", attributeAssignActionSetViews.get(8).getIfHasAttrAssignActionName());
    assertEquals("org4", attributeAssignActionSetViews.get(8).getThenHasAttrAssignActionName());
    assertEquals(1, attributeAssignActionSetViews.get(8).getDepth());
    assertEquals(AttributeAssignActionType.immediate, attributeAssignActionSetViews.get(8)
        .getType());
    assertEquals("org3", attributeAssignActionSetViews.get(8).getParentIfHasName());
    assertEquals("org3", attributeAssignActionSetViews.get(8).getParentThenHasName());
  
    assertEquals("org4", attributeAssignActionSetViews.get(9).getIfHasAttrAssignActionName());
    assertEquals("org4", attributeAssignActionSetViews.get(9).getThenHasAttrAssignActionName());
    assertEquals(0, attributeAssignActionSetViews.get(9).getDepth());
    assertEquals(AttributeAssignActionType.self, attributeAssignActionSetViews.get(9)
        .getType());
    assertEquals("org4", attributeAssignActionSetViews.get(9).getParentIfHasName());
    assertEquals("org4", attributeAssignActionSetViews.get(9).getParentThenHasName());
  
  }
  
  /**
   * make an example role set for testing
   * @return an example role set
   */
  public static AttributeAssignActionSet exampleAttributeAssignActionSet() {
    AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
    attributeAssignActionSet.setContextId("contextId");
    attributeAssignActionSet.setCreatedOnDb(new Long(4L));
    attributeAssignActionSet.setDepth(5);
    attributeAssignActionSet.setIfHasAttrAssignActionId("ifHasAttributeAssignActionId");
    attributeAssignActionSet.setHibernateVersionNumber(3L);
    attributeAssignActionSet.setLastUpdatedDb(new Long(7L));
    attributeAssignActionSet.setParentAttrAssignActionSetId("parentAttributeAssignActionSetId");
    attributeAssignActionSet.setThenHasAttrAssignActionId("thenHasAttributeAssignActionSetId");
    attributeAssignActionSet.setType(AttributeAssignActionType.effective);
    attributeAssignActionSet.setId("id");

    return attributeAssignActionSet;
  }
  
  /**
   * make an example AttributeAssignAction set from db for testing
   * @return an example AttributeAssignAction set
   */
  public static AttributeAssignActionSet exampleAttributeAssignActionSetDb() {
    return exampleAttributeAssignActionSetDb("attributeAssignActionSetTest");
  }
  
  /**
   * make an example AttributeAssignAction set from db for testing
   * @param label
   * @return an example AttributeAssignAction set
   */
  public static AttributeAssignActionSet exampleAttributeAssignActionSetDb(String label) {
    
    AttributeAssignAction attributeAssignActionIf = AttributeAssignActionTest.exampleAttributeAssignActionDb(label + "If");
    AttributeAssignAction attributeAssignActionThen = AttributeAssignActionTest.exampleAttributeAssignActionDb(label + "Then");
    
    attributeAssignActionIf.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(attributeAssignActionThen);
    
    AttributeAssignActionSet attributeAssignActionSet = GrouperDAOFactory.getFactory()
      .getAttributeAssignActionSet().findByIfThenImmediate(
          attributeAssignActionIf.getId(), attributeAssignActionThen.getId(), true);
    return attributeAssignActionSet;
  }

  /**
   * retrieve example AttributeAssignAction from db for testing
   * @return an example AttributeAssignAction
   */
  public static AttributeAssignActionSet exampleRetrieveAttributeAssignActionSetDb() {
    
    return exampleRetrieveAttributeAssignActionSetDb("attributeAssignActionSetTest");
    
  }
  
  /**
   * retrieve example AttributeAssignAction set from db for testing
   * @param label
   * @return an example AttributeAssignAction set
   */
  public static AttributeAssignActionSet exampleRetrieveAttributeAssignActionSetDb(String label) {
    
    AttributeAssignAction attributeAssignActionIf = AttributeAssignActionTest.exampleRetrieveAttributeAssignActionDb(label + "If");
    AttributeAssignAction attributeAssignActionThen = AttributeAssignActionTest.exampleRetrieveAttributeAssignActionDb(label + "Then");
    
    AttributeAssignActionSet attributeAssignActionSet = GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .findByIfThenImmediate(attributeAssignActionIf.getId(), attributeAssignActionThen.getId(), true);
    
    return attributeAssignActionSet;
    
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeAssignActionSet attributeAssignActionSetOriginal = exampleAttributeAssignActionSetDb("attributeAssignActionSetInsert");
    
    //do this because last membership update isnt there, only in db
    attributeAssignActionSetOriginal = exampleRetrieveAttributeAssignActionSetDb("attributeAssignActionSetInsert");
    AttributeAssignActionSet attributeAssignActionSetCopy = exampleRetrieveAttributeAssignActionSetDb("attributeAssignActionSetInsert");
    AttributeAssignActionSet attributeAssignActionSetCopy2 = exampleRetrieveAttributeAssignActionSetDb("attributeAssignActionSetInsert");
    attributeAssignActionSetCopy.delete();
    
    //lets insert the original
    attributeAssignActionSetCopy2.xmlSaveBusinessProperties(null);
    attributeAssignActionSetCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignActionSetCopy = exampleRetrieveAttributeAssignActionSetDb("attributeAssignActionSetInsert");
    
    assertFalse(attributeAssignActionSetCopy == attributeAssignActionSetOriginal);
    assertFalse(attributeAssignActionSetCopy.xmlDifferentBusinessProperties(attributeAssignActionSetOriginal));
    assertFalse(attributeAssignActionSetCopy.xmlDifferentUpdateProperties(attributeAssignActionSetOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssignActionSet attributeAssignActionSet = null;
    AttributeAssignActionSet exampleAttributeAssignAction = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();
      
      attributeAssignActionSet.setContextId("abc");
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setContextId(exampleAttributeAssignAction.getContextId());
      attributeAssignActionSet.xmlSaveUpdateProperties();

      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
      
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setCreatedOnDb(99L);
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setCreatedOnDb(exampleAttributeAssignAction.getCreatedOnDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setLastUpdatedDb(exampleAttributeAssignAction.getLastUpdatedDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

    }

    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertTrue(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setHibernateVersionNumber(exampleAttributeAssignAction.getHibernateVersionNumber());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setDepth(5);
      
      assertTrue(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setDepth(exampleAttributeAssignAction.getDepth());
      attributeAssignActionSet.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignActionSetDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setId("abc");
      
      assertTrue(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setId(exampleAttributeAssignAction.getId());
      attributeAssignActionSet.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignActionSetDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setIfHasAttrAssignActionId("abc");
      
      assertTrue(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setIfHasAttrAssignActionId(exampleAttributeAssignAction.getIfHasAttrAssignActionId());
      attributeAssignActionSet.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignActionSetDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setThenHasAttrAssignActionId("abc");
      
      assertTrue(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setThenHasAttrAssignActionId(exampleAttributeAssignAction.getThenHasAttrAssignActionId());
      attributeAssignActionSet.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignActionSetDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
    {
      attributeAssignActionSet = exampleAttributeAssignActionSetDb();
      exampleAttributeAssignAction = exampleRetrieveAttributeAssignActionSetDb();

      attributeAssignActionSet.setTypeDb(RoleHierarchyType.effective.name());
      
      assertTrue(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));

      attributeAssignActionSet.setTypeDb(exampleAttributeAssignAction.getTypeDb());
      attributeAssignActionSet.xmlSaveBusinessProperties(exampleRetrieveAttributeAssignActionSetDb());
      attributeAssignActionSet.xmlSaveUpdateProperties();
      
      attributeAssignActionSet = exampleRetrieveAttributeAssignActionSetDb();
      
      assertFalse(attributeAssignActionSet.xmlDifferentBusinessProperties(exampleAttributeAssignAction));
      assertFalse(attributeAssignActionSet.xmlDifferentUpdateProperties(exampleAttributeAssignAction));
    
    }
    
  }


}
