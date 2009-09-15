/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author mchyzer
 *
 */
public class AttributeDefTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefTest("testHibernate"));
  }
  
  /**
   * 
   */
  public AttributeDefTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefTest(String name) {
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

    assertNotNull(attributeDef.getId());

    //lets retrieve by id
    AttributeDef attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findById(attributeDef.getId(), true);

    assertEquals(attributeDef.getId(), attributeDef2.getId());
    
    //lets retrieve by name
    attributeDef2 = GrouperDAOFactory.getFactory().getAttributeDef().findByName("top:test", true);
    
    assertEquals("top:test", attributeDef2.getName());
    assertEquals(attributeDef.getId(), attributeDef2.getId());

    //try to add another
    try {
      attributeDef2 = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    } catch (AttributeDefAddException adae) {
      assertTrue(adae.getMessage(), adae.getMessage().contains("attribute def already exists"));
    }

    attributeDef2 = this.top.addChildAttributeDef("test2", AttributeDefType.attr);
    
    
  }

}
