/*
 * @author mchyzer
 * $Id: GrouperCacheUtils.java,v 1.2 2009-08-11 20:18:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cache;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;


/**
 *
 */
public class GrouperCacheUtils {

  /**
   * 
   */
  public static void clearAllCaches() {
    List<CacheManager> cacheManagers = new ArrayList<CacheManager>(CacheManager.ALL_CACHE_MANAGERS);
    for (CacheManager cacheManager : GrouperUtil.nonNull(cacheManagers)) {
      
      //if not alive we get an exception
      if (cacheManager.getStatus() == Status.STATUS_ALIVE) {
        cacheManager.clearAll();
      }
    }
  }
  
}
