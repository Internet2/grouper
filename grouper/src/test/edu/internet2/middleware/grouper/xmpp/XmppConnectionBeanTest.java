/**
 * 
 */
package edu.internet2.middleware.grouper.xmpp;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;



/**
 * @author mchyzer
 *
 */
public class XmppConnectionBeanTest extends GrouperTest {

  /**
   * 
   * @param name
   */
  public XmppConnectionBeanTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmppConnectionBeanTest("testEqualsHashcode"));
  }

  /**
   * 
   */
  public void testEqualsHashcode() {
    XmppConnectionBean xmppConnectionBean = new XmppConnectionBean("server", 123, "user", "resource", "pass");
    XmppConnectionBean xmppConnectionBean2 = new XmppConnectionBean("server", 123, "user", "resource", "pass");
    
    assertTrue(xmppConnectionBean.equals(xmppConnectionBean2));
    assertEquals(xmppConnectionBean.hashCode(), xmppConnectionBean2.hashCode());
    
  }
  
}
