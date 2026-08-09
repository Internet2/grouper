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

    assertTrue(noCache.get(true));
  }

  /**
   * seconds-granularity put: a sub-minute time to live must cache (not throw). The whole-minutes
   * put(key, value, int) truncates e.g. 30 seconds to 0 minutes and rejects it -- the seconds unit
   * caches it for its real lifetime. Regression for the short-lived OAuth token caching bug
   * (GRP-7228 follow-up: cache sub-minute tokens instead of skipping the cache).
   */
  public void testPutSeconds() {

    ExpirableCache<String, String> cache = new ExpirableCache<String, String>();

    // 30 seconds is 0 whole minutes -- the minutes put would throw; the seconds put must cache it
    cache.put("k", "v", 30, ExpirableCache.ExpirableCacheUnit.SECOND);
    assertEquals("v", cache.get("k"));

    // the whole-minutes put still rejects a <= 0 minute time to live (behavior preserved)
    try {
      cache.put("k2", "v2", 0);
      fail("expected exception for a 0-minute time to live");
    } catch (RuntimeException e) {
      // expected
    }

    // a 1-second entry is cached now and gone after it expires
    cache.put("k3", "v3", 1, ExpirableCache.ExpirableCacheUnit.SECOND);
    assertEquals("v3", cache.get("k3"));
    GrouperClientCommonUtils.sleep(1500);
    assertNull(cache.get("k3"));
  }

}
