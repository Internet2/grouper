/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class ExpirableCacheTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExpirableCacheTest("testNoCache"));
  }
  
  /**
   * 
   */
  public ExpirableCacheTest() {
    super();
    
  }

  /**
   * @param name
   */
  public ExpirableCacheTest(String name) {
    super(name);
    
  }

  /**
   * test nocache
   */
  public void testNoCache() {
    
    ExpirableCache<Boolean, Boolean> noCache = new ExpirableCache(0);
    
    noCache.put(true, true);
    
    assertNull("" + noCache.get(true), noCache.get(true));
    
    noCache = new ExpirableCache(-1);
    
    noCache.put(true, true);
    
    assertNull(noCache.get(true));
  }
  
}
