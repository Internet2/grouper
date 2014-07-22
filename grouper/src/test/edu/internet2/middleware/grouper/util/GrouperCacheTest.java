/**
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
 */
/*
 * @author mchyzer
 * $Id: GrouperCacheTest.java,v 1.3 2008-07-21 04:43:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import junit.framework.TestCase;


/**
 * test grouper cache
 */
public class GrouperCacheTest extends TestCase {

  /**
   * @param name
   */
  public GrouperCacheTest(String name) {
    super(name);
  }

  /**
   * test the cache
   */
  public void testCache() {
    
    String name = "edu.internet2.middleware.grouper.util.GrouperCacheTest.testCache";
    GrouperCache<String, Object> grouperCache = 
      new GrouperCache<String, Object>(name,
          1000, false, 1, 1, false);
    Object value = new Object();
    String key = "test";
    grouperCache.put(key, value);
    assertTrue("references should be the same", value==grouperCache.get(key));
    
    //get this again from factory to be sure
    grouperCache = (GrouperCache<String, Object>)EhcacheController.ehcacheController().getGrouperCache(name);
    
    assertTrue("references should still be the same", value==grouperCache.get(key));

    //wait 1.5 seconds and it should expire
    GrouperUtil.sleep(1500);
    
    assertNull("wait 1.5 seconds and it should expire", grouperCache.get(key));
    
    //here is a cache that should exist... and shouldnt fail
    new GrouperCache("edu.internet2.middleware.grouper.Membership.getGroup");
    
    //here is a cache that doesnt exist and should fail
    try {
      new GrouperCache("isntConfigured");
      fail("An unconfigured cache with no defaults should fail construction");
    } catch (IllegalStateException ise) {
      //good
    }
  }
  
}
