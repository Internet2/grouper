/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

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
    TestRunner.run(new AttributeDefNameSetTest("testHibernate"));
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

}
