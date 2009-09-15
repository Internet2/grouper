/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author mchyzer
 *
 */
public class AttributeDefNameTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameTest("testHibernate"));
  }
  
  /**
   * 
   */
  public AttributeDefNameTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefNameTest(String name) {
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
    
    assertNotNull(attributeDefName.getId());

    //lets retrieve by id
    AttributeDefName attributeDefName2 = GrouperDAOFactory.getFactory().getAttributeDefName().findById(attributeDefName.getId(), true);

    assertEquals(attributeDefName.getId(), attributeDefName2.getId());
    
    //lets retrieve by name
    attributeDefName2 = GrouperDAOFactory.getFactory().getAttributeDefName().findByName("top:testName", true);
    
    assertEquals("top:testName", attributeDefName2.getName());
    assertEquals("top display name:test name", attributeDefName2.getDisplayName());
    assertEquals(attributeDefName.getId(), attributeDefName2.getId());

    //try to add another
    try {
      attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    } catch (AttributeDefNameAddException adae) {
      assertTrue(ExceptionUtils.getFullStackTrace(adae), adae.getMessage().contains("attribute def name already exists"));
    }

    attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");
    
    
  }

}
