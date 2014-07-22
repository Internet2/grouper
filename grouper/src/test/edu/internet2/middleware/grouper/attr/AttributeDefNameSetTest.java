/**
 * Copyright 2014 Internet2
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
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.RoleHierarchyType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeDefNameSetTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameSetTest("testSetLogic"));
  }

  /**
   * 
   */
  public AttributeDefNameSetTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefNameSetTest(String name) {
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
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test",
        AttributeDefType.attr);
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef,
        "testName", "test name");
    AttributeDefName attributeDefName2 = this.top.addChildAttributeDefName(attributeDef,
        "testName2", "test name2");

    AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
    attributeDefNameSet.setId(GrouperUuid.getUuid());
    attributeDefNameSet.setDepth(1);
    attributeDefNameSet.setIfHasAttributeDefNameId(attributeDefName.getId());
    attributeDefNameSet.setThenHasAttributeDefNameId(attributeDefName2.getId());
    attributeDefNameSet.setType(AttributeDefAssignmentType.immediate);
    attributeDefNameSet.saveOrUpdate();

    attributeDefName2.delete();

    attributeDefName.delete();

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

    int initialAttrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    AttributeDef attributeDef = this.top.addChildAttributeDef("orgs",
        AttributeDefType.attr);
    AttributeDefName org1 = this.top.addChildAttributeDefName(attributeDef, "org1",
        "org1");

    int attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 1, attrDefNameSetViewCount);

    //lets make sure one record was created
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefNameSet where ifHasAttributeDefNameId = :theIfHasId")
        .setString("theIfHasId", org1.getId())
        .uniqueResult(AttributeDefNameSet.class);

    assertEquals(0, attributeDefNameSet.getDepth());
    assertEquals(org1.getId(), attributeDefNameSet.getIfHasAttributeDefNameId());
    assertEquals(org1.getId(), attributeDefNameSet.getThenHasAttributeDefNameId());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSet.getType());
    assertEquals(attributeDefNameSet.getId(), attributeDefNameSet
        .getParentAttrDefNameSetId());

    AttributeDefName org2 = this.top.addChildAttributeDefName(attributeDef, "org2",
        "org2");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 2, attrDefNameSetViewCount);

    org1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(org2);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 3, attrDefNameSetViewCount);

    AttributeDefName org3 = this.top.addChildAttributeDefName(attributeDef, "org3",
        "org3");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 4, attrDefNameSetViewCount);

    AttributeDefName org4 = this.top.addChildAttributeDefName(attributeDef, "org4",
        "org4");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 5, attrDefNameSetViewCount);

    org3.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(org4);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 6, attrDefNameSetViewCount);

    //connect the branches
    org2.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(org3);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 10, attrDefNameSetViewCount);

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet("top:org1", "top:org2", "top:org3", "top:org4")));

    assertEquals("top:org1", attributeDefNameSetViews.get(0).getIfHasAttrDefNameName());
    assertEquals("top:org1", attributeDefNameSetViews.get(0).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(0).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(0)
        .getType());
    assertEquals("top:org1", attributeDefNameSetViews.get(0).getParentIfHasName());
    assertEquals("top:org1", attributeDefNameSetViews.get(0).getParentThenHasName());

    assertEquals("top:org1", attributeDefNameSetViews.get(1).getIfHasAttrDefNameName());
    assertEquals("top:org2", attributeDefNameSetViews.get(1).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(1).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(1)
        .getType());
    assertEquals("top:org1", attributeDefNameSetViews.get(1).getParentIfHasName());
    assertEquals("top:org1", attributeDefNameSetViews.get(1).getParentThenHasName());

    assertEquals("top:org1", attributeDefNameSetViews.get(2).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(2).getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(2).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(2)
        .getType());
    assertEquals("top:org1", attributeDefNameSetViews.get(2).getParentIfHasName());
    assertEquals("top:org2", attributeDefNameSetViews.get(2).getParentThenHasName());

    assertEquals("top:org1", attributeDefNameSetViews.get(3).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(3).getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(3).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(3)
        .getType());
    assertEquals(
        attributeDefNameSetViews.get(3).getParentIfHasName() + " -> "
        + attributeDefNameSetViews.get(3).getParentThenHasName(),
        "top:org1", attributeDefNameSetViews.get(3).getParentIfHasName());
    assertEquals(attributeDefNameSetViews.get(3).getParentIfHasName() + " -> "
        + attributeDefNameSetViews.get(3).getParentThenHasName(),
        "top:org3", attributeDefNameSetViews.get(3).getParentThenHasName());

    assertEquals("top:org2", attributeDefNameSetViews.get(4).getIfHasAttrDefNameName());
    assertEquals("top:org2", attributeDefNameSetViews.get(4).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(4).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(4)
        .getType());
    assertEquals("top:org2", attributeDefNameSetViews.get(4).getParentIfHasName());
    assertEquals("top:org2", attributeDefNameSetViews.get(4).getParentThenHasName());

    assertEquals("top:org2", attributeDefNameSetViews.get(5).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(5).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(5).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(5)
        .getType());
    assertEquals("top:org2", attributeDefNameSetViews.get(5).getParentIfHasName());
    assertEquals("top:org2", attributeDefNameSetViews.get(5).getParentThenHasName());

    assertEquals("top:org2", attributeDefNameSetViews.get(6).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(6).getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(6).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(6)
        .getType());
    assertEquals("top:org2", attributeDefNameSetViews.get(6).getParentIfHasName());
    assertEquals("top:org3", attributeDefNameSetViews.get(6).getParentThenHasName());

    assertEquals("top:org3", attributeDefNameSetViews.get(7).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(7).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(7).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(7)
        .getType());
    assertEquals("top:org3", attributeDefNameSetViews.get(7).getParentIfHasName());
    assertEquals("top:org3", attributeDefNameSetViews.get(7).getParentThenHasName());

    assertEquals("top:org3", attributeDefNameSetViews.get(8).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(8).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(8).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(8)
        .getType());
    assertEquals("top:org3", attributeDefNameSetViews.get(8).getParentIfHasName());
    assertEquals("top:org3", attributeDefNameSetViews.get(8).getParentThenHasName());

    assertEquals("top:org4", attributeDefNameSetViews.get(9).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(9).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(9).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(9)
        .getType());
    assertEquals("top:org4", attributeDefNameSetViews.get(9).getParentIfHasName());
    assertEquals("top:org4", attributeDefNameSetViews.get(9).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveBfromA() {
    setupStructure();
    AttributeDefName orgA = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgA", true);
    AttributeDefName orgB = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgB", true);
    orgA.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgB);

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index = 5;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    //note, there are two E's since there are two paths to it
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    //note there are two of these since two A->E's
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

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

    int initialAttrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    AttributeDef attributeDef = this.top.addChildAttributeDef("orgs",
        AttributeDefType.attr);
    AttributeDefName orgA = this.top.addChildAttributeDefName(attributeDef, "orgA",
        "orgA");

    int attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 1, attrDefNameSetViewCount);

    AttributeDefName orgB = this.top.addChildAttributeDefName(attributeDef, "orgB",
        "orgB");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 2, attrDefNameSetViewCount);

    // A -> B
    assertTrue(orgA.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgB));
    Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate = orgA.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate();
    assertTrue(attributeDefNamesImpliedByThisImmediate.contains(orgB));
    assertFalse(orgA.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgB));

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 3, attrDefNameSetViewCount);

    // A -> H
    AttributeDefName orgH = this.top.addChildAttributeDefName(attributeDef, "orgH",
        "orgH");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 4, attrDefNameSetViewCount);

    orgA.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgH);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 5, attrDefNameSetViewCount);

    // A -> I
    AttributeDefName orgI = this.top.addChildAttributeDefName(attributeDef, "orgI",
        "orgI");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 6, attrDefNameSetViewCount);

    orgA.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgI);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 7, attrDefNameSetViewCount);

    // orgC
    AttributeDefName orgC = this.top.addChildAttributeDefName(attributeDef, "orgC",
        "orgC");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 8, attrDefNameSetViewCount);

    // B -> C
    orgB.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgC);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    // B->C, A->C
    assertEquals(initialAttrDefNameSetViewCount + 10, attrDefNameSetViewCount);

    AttributeDefName orgD = this.top.addChildAttributeDefName(attributeDef, "orgD",
        "orgD");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 11, attrDefNameSetViewCount);

    // B -> D
    orgB.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgD);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    // B->D, A->D
    assertEquals(initialAttrDefNameSetViewCount + 13, attrDefNameSetViewCount);

    AttributeDefName orgE = this.top.addChildAttributeDefName(attributeDef, "orgE",
        "orgE");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 14, attrDefNameSetViewCount);

    // C -> E
    orgC.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgE);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds C->E, B->E, A->E
    assertEquals(initialAttrDefNameSetViewCount + 17, attrDefNameSetViewCount);

    AttributeDefName orgG = this.top.addChildAttributeDefName(attributeDef, "orgG",
        "orgG");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 18, attrDefNameSetViewCount);

    // C -> G
    orgC.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgG);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds C->G, B->G, A->G
    assertEquals(initialAttrDefNameSetViewCount + 21, attrDefNameSetViewCount);

    // D -> E
    orgD.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgE);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds D->E, B->E, A->E
    assertEquals(initialAttrDefNameSetViewCount + 24, attrDefNameSetViewCount);

    AttributeDefName orgF = this.top.addChildAttributeDefName(attributeDef, "orgF",
        "orgF");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 25, attrDefNameSetViewCount);

    // E -> F
    orgE.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgF);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds E->F, C->F, D->F, B->F (x2, two parents), A->F (x2, two parents)
    assertEquals(initialAttrDefNameSetViewCount + 32, attrDefNameSetViewCount);

    AttributeDefName orgL = this.top.addChildAttributeDefName(attributeDef, "orgL",
        "orgL");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 33, attrDefNameSetViewCount);

    // E -> L
    orgE.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgL);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds E->L, C->L, D->L, B->L (x2), A->L (x2)
    assertEquals(initialAttrDefNameSetViewCount + 40, attrDefNameSetViewCount);

    // G -> F
    orgG.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgF);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds G->F, C->F, B->F, A->F)
    assertEquals(initialAttrDefNameSetViewCount + 44, attrDefNameSetViewCount);

    // H -> I
    orgH.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgI);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds H->I, A->I
    assertEquals(initialAttrDefNameSetViewCount + 46, attrDefNameSetViewCount);

    AttributeDefName orgJ = this.top.addChildAttributeDefName(attributeDef, "orgJ",
        "orgJ");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 47, attrDefNameSetViewCount);

    // I -> J
    orgI.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgJ);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds I->J, H->J, A->J (x2)
    assertEquals(initialAttrDefNameSetViewCount + 51, attrDefNameSetViewCount);

    // J -> F
    orgJ.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgF);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds J->F, I->F, H->F, A->F (x2)
    assertEquals(initialAttrDefNameSetViewCount + 56, attrDefNameSetViewCount);

    // J -> H
    orgJ.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgH);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds J->H, A->H, J->I, I->H
    assertEquals(initialAttrDefNameSetViewCount + 60, attrDefNameSetViewCount);

    AttributeDefName orgK = this.top.addChildAttributeDefName(attributeDef, "orgK",
        "orgK");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    assertEquals(initialAttrDefNameSetViewCount + 61, attrDefNameSetViewCount);

    // K -> C
    orgK.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(orgC);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");

    //adds K->C, K->G, K->F, K->E, K->L, K->F
    assertEquals(initialAttrDefNameSetViewCount + 67, attrDefNameSetViewCount);

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveHfromA() {
    setupStructure();
    AttributeDefName orgA = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgA", true);
    AttributeDefName orgH = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgH", true);
    orgA.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgH);

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //    
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveIfromA() {
    setupStructure();
    AttributeDefName orgA = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgA", true);
    AttributeDefName orgI = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgI", true);
    orgA.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgI);

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //      
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //      
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("top:orgH", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //      
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("top:orgI", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //      
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getIfHasAttrDefNameName());
    //      assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveCfromB() {
    setupStructure();
    AttributeDefName orgB = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgB", true);
    AttributeDefName orgC = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgC", true);
    assertFalse(orgC.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgB));
    assertTrue(orgB.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgC));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
     * 
     */
  public void testComplexRemoveDfromB() {
    setupStructure();
    AttributeDefName orgB = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgB", true);
    AttributeDefName orgD = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgD", true);
    assertFalse(orgD.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgB));
    assertTrue(orgB.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgD));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
       * 
       */
  public void testComplexRemoveEfromC() {
    setupStructure();
    AttributeDefName orgC = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgC", true);
    AttributeDefName orgE = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgE", true);
    assertTrue(orgC.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgE));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveGfromC() {
    setupStructure();
    AttributeDefName orgC = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgC", true);
    AttributeDefName orgG = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgG", true);
    assertTrue(orgC.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgG));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
     * 
     */
  public void testComplexRemoveEfromD() {
    setupStructure();
    AttributeDefName orgD = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgD", true);
    AttributeDefName orgE = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgE", true);
    assertTrue(orgD.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgE));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      //note there are two of these since two A->E's
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());
    //
    //      index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());
    //
    //      index++;
    //
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
       * 
       */
  public void testComplexRemoveFfromE() {
    setupStructure();
    AttributeDefName orgE = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgE", true);
    AttributeDefName orgF = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgF", true);
    assertTrue(orgE.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgF));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        //note, there are two E's since there are two paths to it
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());
    //
    //        index++;
    //
    //        //two of these since two B->E's
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveLfromE() {
    setupStructure();
    AttributeDefName orgE = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgE", true);
    AttributeDefName orgL = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgL", true);
    assertTrue(orgE.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgL));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    //note there are two of these since two A->E's
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
     * 
     */
  public void testComplexRemoveFfromG() {
    setupStructure();
    AttributeDefName orgG = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgG", true);
    AttributeDefName orgF = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgF", true);
    assertTrue(orgG.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgF));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgC", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
       * 
       */
  public void testComplexRemoveIfromH() {
    setupStructure();
    AttributeDefName orgH = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgH", true);
    AttributeDefName orgI = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgI", true);
    assertTrue(orgH.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgI));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        //note, there are two A->J's
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //        index++;
    //
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //            .getIfHasAttrDefNameName());
    //        assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //            .getThenHasAttrDefNameName());
    //        assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //        assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //            .get(index).getType());
    //        assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    //        assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveJfromI() {
    setupStructure();
    AttributeDefName orgI = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgI", true);
    AttributeDefName orgJ = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgJ", true);
    assertTrue(orgI.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgJ));

    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveFfromJ() {
    setupStructure();
    AttributeDefName orgJ = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgJ", true);
    AttributeDefName orgF = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgF", true);
    assertTrue(orgJ.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgF));
    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    //note, there are two A->J's
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * 
   */
  public void testComplexRemoveHfromJ() {
    setupStructure();
    AttributeDefName orgJ = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgJ", true);
    AttributeDefName orgH = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgH", true);
    assertTrue(orgJ.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgH));
    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    //    index++;
    //
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());
    //
    //    index++;
    //
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
    //        .getIfHasAttrDefNameName());
    //    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
    //        .getThenHasAttrDefNameName());
    //    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    //    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //        .get(index).getType());
    //    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    //    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
     * 
     */
  public void testComplexRemoveCfromK() {
    setupStructure();
    AttributeDefName orgK = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgK", true);
    AttributeDefName orgC = GrouperDAOFactory.getFactory().getAttributeDefName()
        .findByNameSecure("top:orgC", true);
    assertTrue(orgK.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(orgC));
    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(
        GrouperDAOFactory.getFactory()
        .getAttributeDefNameSetView().findByAttributeDefNameSetViews(
        GrouperUtil.toSet(
        "top:orgA", "top:orgB", "top:orgC", "top:orgD", "top:orgE", "top:orgF",
        "top:orgG", "top:orgH",
        "top:orgI", "top:orgJ", "top:orgK", "top:orgL")));

    int index = 0;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two E's since there are two paths to it
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note, there are two A->J's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //note there are two of these since two A->E's
    assertEquals("top:orgA", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(4, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgA", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    //two of these since two B->E's
    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgB", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgB", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgD", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgD", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgI", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
        .get(index).getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgH", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgJ", attributeDefNameSetViews.get(index).getParentThenHasName());

//    index++;
//
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
//        .getIfHasAttrDefNameName());
//    assertEquals("top:orgC", attributeDefNameSetViews.get(index)
//        .getThenHasAttrDefNameName());
//    assertEquals(1, attributeDefNameSetViews.get(index).getDepth());
//    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews
//        .get(index).getType());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

//    index++;
//
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
//        .getIfHasAttrDefNameName());
//    assertEquals("top:orgE", attributeDefNameSetViews.get(index)
//        .getThenHasAttrDefNameName());
//    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
//    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
//        .get(index).getType());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
//    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

//    index++;
//
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
//        .getIfHasAttrDefNameName());
//    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
//        .getThenHasAttrDefNameName());
//    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
//    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
//        .get(index).getType());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
//    assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

//    index++;
//
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
//        .getIfHasAttrDefNameName());
//    assertEquals("top:orgF", attributeDefNameSetViews.get(index)
//        .getThenHasAttrDefNameName());
//    assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
//    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
//        .get(index).getType());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
//    assertEquals("top:orgG", attributeDefNameSetViews.get(index).getParentThenHasName());

//    index++;
//
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
//        .getIfHasAttrDefNameName());
//    assertEquals("top:orgG", attributeDefNameSetViews.get(index)
//        .getThenHasAttrDefNameName());
//    assertEquals(2, attributeDefNameSetViews.get(index).getDepth());
//    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
//        .get(index).getType());
//    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
//    assertEquals("top:orgC", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentThenHasName());

    //      index++;
    //
    //      assertEquals("top:orgK", attributeDefNameSetViews.get(index)
    //          .getIfHasAttrDefNameName());
    //      assertEquals("top:orgL", attributeDefNameSetViews.get(index)
    //          .getThenHasAttrDefNameName());
    //      assertEquals(3, attributeDefNameSetViews.get(index).getDepth());
    //      assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews
    //          .get(index).getType());
    //      assertEquals("top:orgK", attributeDefNameSetViews.get(index).getParentIfHasName());
    //      assertEquals("top:orgE", attributeDefNameSetViews.get(index).getParentThenHasName());

    index++;

    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getIfHasAttrDefNameName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index)
        .getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(index).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(index)
        .getType());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentIfHasName());
    assertEquals("top:orgL", attributeDefNameSetViews.get(index).getParentThenHasName());

  }

  /**
   * make an example role set for testing
   * @return an example role set
   */
  public static AttributeDefNameSet exampleAttributeDefNameSet() {
    AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
    attributeDefNameSet.setContextId("contextId");
    attributeDefNameSet.setCreatedOnDb(new Long(4L));
    attributeDefNameSet.setDepth(5);
    attributeDefNameSet.setIfHasAttributeDefNameId("ifHasAttributeDefNameId");
    attributeDefNameSet.setHibernateVersionNumber(3L);
    attributeDefNameSet.setLastUpdatedDb(new Long(7L));
    attributeDefNameSet.setParentAttrDefNameSetId("parentAttributeDefNameSetId");
    attributeDefNameSet.setThenHasAttributeDefNameId("thenHasAttributeDefNameSetId");
    attributeDefNameSet.setType(AttributeDefAssignmentType.effective);
    attributeDefNameSet.setId("id");

    
    return attributeDefNameSet;
  }
  
  /**
   * make an example AttributeDefName set from db for testing
   * @return an example AttributeDefName set
   */
  public static AttributeDefNameSet exampleAttributeDefNameSetDb() {
    return exampleAttributeDefNameSetDb("attributeDefNameSetTest");
  }
  
  /**
   * make an example AttributeDefName set from db for testing
   * @param label
   * @return an example AttributeDefName set
   */
  public static AttributeDefNameSet exampleAttributeDefNameSetDb(String label) {
    
    AttributeDefName attributeDefNameIf = AttributeDefNameTest.exampleAttributeDefNameDb("test", label + "DefIf");
    AttributeDefName attributeDefNameThen = AttributeDefNameTest.exampleAttributeDefNameDb("test", label + "DefThen");
    
    attributeDefNameIf.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefNameThen);
    
    AttributeDefNameSet attributeDefNameSet = GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .findByIfThenImmediate(attributeDefNameIf.getId(), attributeDefNameThen.getId(), true);
    
    return attributeDefNameSet;
  }

  /**
   * retrieve example AttributeDefName set from db for testing
   * @return an example AttributeDefName set
   */
  public static AttributeDefNameSet exampleRetrieveAttributeDefNameSetDb() {
    
    return exampleRetrieveAttributeDefNameSetDb("attributeDefNameSetTest");
    
  }
  
  /**
   * retrieve example AttributeDefName set from db for testing
   * @param label
   * @return an example AttributeDefName set
   */
  public static AttributeDefNameSet exampleRetrieveAttributeDefNameSetDb(String label) {
    
    AttributeDefName attributeDefNameIf = AttributeDefNameFinder.findByName("test:" + label + "DefIf", true);
    AttributeDefName attributeDefNameThen = AttributeDefNameFinder.findByName("test:" + label + "DefThen", true);
    
    AttributeDefNameSet attributeDefNameSet = GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .findByIfThenImmediate(attributeDefNameIf.getId(), attributeDefNameThen.getId(), true);
    
    return attributeDefNameSet;
    
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeDefNameSet attributeDefNameSetOriginal = exampleAttributeDefNameSetDb("attributeDefNameSetInsert");
    
    //do this because last membership update isnt there, only in db
    attributeDefNameSetOriginal = exampleRetrieveAttributeDefNameSetDb("attributeDefNameSetInsert");
    AttributeDefNameSet attributeDefNameSetCopy = exampleRetrieveAttributeDefNameSetDb("attributeDefNameSetInsert");
    AttributeDefNameSet attributeDefNameSetCopy2 = exampleRetrieveAttributeDefNameSetDb("attributeDefNameSetInsert");
    attributeDefNameSetCopy.delete();
    
    //lets insert the original
    attributeDefNameSetCopy2.xmlSaveBusinessProperties(null);
    attributeDefNameSetCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeDefNameSetCopy = exampleRetrieveAttributeDefNameSetDb("attributeDefNameSetInsert");
    
    assertFalse(attributeDefNameSetCopy == attributeDefNameSetOriginal);
    assertFalse(attributeDefNameSetCopy.xmlDifferentBusinessProperties(attributeDefNameSetOriginal));
    assertFalse(attributeDefNameSetCopy.xmlDifferentUpdateProperties(attributeDefNameSetOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefNameSet attributeDefNameSet = null;
    AttributeDefNameSet exampleAttributeDefNameSet = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      attributeDefNameSet.setContextId("abc");
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertTrue(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setContextId(exampleAttributeDefNameSet.getContextId());
      attributeDefNameSet.xmlSaveUpdateProperties();

      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
      
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setCreatedOnDb(99L);
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertTrue(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setCreatedOnDb(exampleAttributeDefNameSet.getCreatedOnDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setLastUpdatedDb(99L);
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertTrue(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setLastUpdatedDb(exampleAttributeDefNameSet.getLastUpdatedDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

    }

    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setHibernateVersionNumber(99L);
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertTrue(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setHibernateVersionNumber(exampleAttributeDefNameSet.getHibernateVersionNumber());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setDepth(5);
      
      assertTrue(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setDepth(exampleAttributeDefNameSet.getDepth());
      attributeDefNameSet.xmlSaveBusinessProperties(exampleRetrieveAttributeDefNameSetDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setId("abc");
      
      assertTrue(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setId(exampleAttributeDefNameSet.getId());
      attributeDefNameSet.xmlSaveBusinessProperties(exampleRetrieveAttributeDefNameSetDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setIfHasAttributeDefNameId("abc");
      
      assertTrue(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setIfHasAttributeDefNameId(exampleAttributeDefNameSet.getIfHasAttributeDefNameId());
      attributeDefNameSet.xmlSaveBusinessProperties(exampleRetrieveAttributeDefNameSetDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setThenHasAttributeDefNameId("abc");
      
      assertTrue(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setThenHasAttributeDefNameId(exampleAttributeDefNameSet.getThenHasAttributeDefNameId());
      attributeDefNameSet.xmlSaveBusinessProperties(exampleRetrieveAttributeDefNameSetDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    
    }
    
    {
      attributeDefNameSet = exampleAttributeDefNameSetDb();
      exampleAttributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();

      attributeDefNameSet.setTypeDb(RoleHierarchyType.effective.name());
      
      assertTrue(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));

      attributeDefNameSet.setTypeDb(exampleAttributeDefNameSet.getTypeDb());
      attributeDefNameSet.xmlSaveBusinessProperties(exampleRetrieveAttributeDefNameSetDb());
      attributeDefNameSet.xmlSaveUpdateProperties();
      
      attributeDefNameSet = exampleRetrieveAttributeDefNameSetDb();
      
      assertFalse(attributeDefNameSet.xmlDifferentBusinessProperties(exampleAttributeDefNameSet));
      assertFalse(attributeDefNameSet.xmlDifferentUpdateProperties(exampleAttributeDefNameSet));
    
    }
    
  }


  
  
}
