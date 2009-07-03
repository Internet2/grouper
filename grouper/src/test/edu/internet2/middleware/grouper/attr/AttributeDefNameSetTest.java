/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

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
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    AttributeDefName attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");

    AttributeDefNameSet attributeDefNameSet = new AttributeDefNameSet();
    attributeDefNameSet.setId(GrouperUuid.getUuid());
    attributeDefNameSet.setDepth(1);
    attributeDefNameSet.setIfHasAttributeDefNameId(attributeDefName.getId());
    attributeDefNameSet.setThenHasAttributeDefNameId(attributeDefName2.getId());
    attributeDefNameSet.setType(AttributeDefAssignmentType.immediate);
    attributeDefNameSet.saveOrUpdate();
    
    
  }

  /**
   * attribute def
   */
  public void testSetLogic() {
    
    int initialAttrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("orgs", AttributeDefType.attr);
    AttributeDefName org1 = this.top.addChildAttributeDefName(attributeDef, "org1", "org1");
    
    int attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 1, attrDefNameSetViewCount);
    
    //lets make sure one record was created
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery("from AttributeDefNameSet")
      .uniqueResult(AttributeDefNameSet.class);
    
    assertEquals(0, attributeDefNameSet.getDepth());
    assertEquals(org1.getId(), attributeDefNameSet.getIfHasAttributeDefNameId());
    assertEquals(org1.getId(), attributeDefNameSet.getThenHasAttributeDefNameId());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSet.getType());
    assertEquals(attributeDefNameSet.getId(), attributeDefNameSet.getParentId());
    
    AttributeDefName org2 = this.top.addChildAttributeDefName(attributeDef, "org2", "org2");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 2, attrDefNameSetViewCount);

    org1.addToAttributeDefNameSet(org2);
    
    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 3, attrDefNameSetViewCount);
    
    AttributeDefName org3 = this.top.addChildAttributeDefName(attributeDef, "org3", "org3");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 4, attrDefNameSetViewCount);

    AttributeDefName org4 = this.top.addChildAttributeDefName(attributeDef, "org4", "org4");

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 5, attrDefNameSetViewCount);

    org3.addToAttributeDefNameSet(org4);

    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 6, attrDefNameSetViewCount);

    //connect the branches
    org2.addToAttributeDefNameSet(org3);
    
    attrDefNameSetViewCount = HibernateSession.bySqlStatic().select(
        int.class, "select count(1) from grouper_attr_def_name_set_v");
    
    assertEquals(initialAttrDefNameSetViewCount + 10, attrDefNameSetViewCount);
    
    //lets look at them all
    List<AttributeDefNameSetView> attributeDefNameSetViews = new ArrayList<AttributeDefNameSetView>(GrouperDAOFactory.getFactory()
      .getAttributeDefNameSetView().findByAttributeDefNameSetViews(GrouperUtil.toSet("top:org1", "top:org2", "top:org3", "top:org4")));
    
    assertEquals("top:org1", attributeDefNameSetViews.get(0).getIfHasAttrDefNameName());
    assertEquals("top:org1", attributeDefNameSetViews.get(0).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(0).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(0).getType());

    assertEquals("top:org1", attributeDefNameSetViews.get(1).getIfHasAttrDefNameName());
    assertEquals("top:org2", attributeDefNameSetViews.get(1).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(1).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(1).getType());

    assertEquals("top:org1", attributeDefNameSetViews.get(2).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(2).getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(2).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(2).getType());

    assertEquals("top:org1", attributeDefNameSetViews.get(3).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(3).getThenHasAttrDefNameName());
    assertEquals(3, attributeDefNameSetViews.get(3).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(3).getType());

    assertEquals("top:org2", attributeDefNameSetViews.get(4).getIfHasAttrDefNameName());
    assertEquals("top:org2", attributeDefNameSetViews.get(4).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(4).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(4).getType());

    assertEquals("top:org2", attributeDefNameSetViews.get(5).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(5).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(5).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(5).getType());

    assertEquals("top:org2", attributeDefNameSetViews.get(6).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(6).getThenHasAttrDefNameName());
    assertEquals(2, attributeDefNameSetViews.get(6).getDepth());
    assertEquals(AttributeDefAssignmentType.effective, attributeDefNameSetViews.get(6).getType());

    assertEquals("top:org3", attributeDefNameSetViews.get(7).getIfHasAttrDefNameName());
    assertEquals("top:org3", attributeDefNameSetViews.get(7).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(7).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(7).getType());

    assertEquals("top:org3", attributeDefNameSetViews.get(8).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(8).getThenHasAttrDefNameName());
    assertEquals(1, attributeDefNameSetViews.get(8).getDepth());
    assertEquals(AttributeDefAssignmentType.immediate, attributeDefNameSetViews.get(8).getType());

    assertEquals("top:org4", attributeDefNameSetViews.get(9).getIfHasAttrDefNameName());
    assertEquals("top:org4", attributeDefNameSetViews.get(9).getThenHasAttrDefNameName());
    assertEquals(0, attributeDefNameSetViews.get(9).getDepth());
    assertEquals(AttributeDefAssignmentType.self, attributeDefNameSetViews.get(9).getType());

  }

}
