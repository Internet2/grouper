/*******************************************************************************
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
 ******************************************************************************/
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
