/**
 * @author mchyzer
 * $Id$
 */
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
