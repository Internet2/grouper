/**
 * @author mchyzer
 * $Id: AttributeAssignActionTest.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 *
 */
public class AttributeAssignActionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignActionTest("testHibernate"));
  }
  
  /**
   * 
   * @param name
   */
  public AttributeAssignActionTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testHibernate() {

    GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top display name");
    AttributeDef attributeDef = top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setId(GrouperUuid.getUuid());
    attributeAssignAction.setAttributeDefId(attributeDef.getId());

    attributeAssignAction.save();
    
    attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignAction.getId(), true);
    
    attributeAssignAction.delete();

    attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignAction.getId(), false);
    
    assertNull(attributeAssignAction);
  }
  
  
}
