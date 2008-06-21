/*
 * @author mchyzer
 * $Id: GrouperCacheTest.java,v 1.2 2008-06-21 04:16:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.grouper.cache.EhcacheController;
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
