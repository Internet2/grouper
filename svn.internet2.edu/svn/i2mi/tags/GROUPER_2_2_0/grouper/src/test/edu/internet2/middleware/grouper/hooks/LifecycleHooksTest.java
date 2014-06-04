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
/*
 * @author mchyzer
 * $Id: LifecycleHooksTest.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class LifecycleHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new LifecycleHooksTest("sdfsad"));
    TestRunner.run(LifecycleHooksTest.class);
  }
  
  /**
   * @param name
   */
  public LifecycleHooksTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testLifecycle() {
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitGrouperStartup);
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitHibernateInit);
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitHooksInit);
  }
}
