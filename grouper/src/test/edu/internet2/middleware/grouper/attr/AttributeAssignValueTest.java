/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * @author mchyzer
 *
 */
public class AttributeAssignValueTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueTest("testHibernate"));
  }
  
  /**
   * 
   */
  public AttributeAssignValueTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignValueTest(String name) {
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

    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName);
    attributeAssign.saveOrUpdate();
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue.setValueString("hello");
    attributeAssignValue.setId(GrouperUuid.getUuid());
    attributeAssignValue.saveOrUpdate();
    
    
    
  }

}
