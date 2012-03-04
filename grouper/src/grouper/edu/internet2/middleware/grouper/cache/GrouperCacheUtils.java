/*
 * @author mchyzer
 * $Id: GrouperCacheUtils.java,v 1.2 2009-08-11 20:18:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;


/**
 *
 */
public class GrouperCacheUtils {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperCacheUtils.class);

  /**
   * 
   */
  public static void clearAllCaches() {
    List<CacheManager> cacheManagers = new ArrayList<CacheManager>(CacheManager.ALL_CACHE_MANAGERS);
    for (CacheManager cacheManager : GrouperUtil.nonNull(cacheManagers)) {
      
      //if not alive we get an exception
      if (cacheManager.getStatus() == Status.STATUS_ALIVE) {
        //note we get an exception if the cache is not alive, so do this manually
        //cacheManager.clearAll();
        String[] cacheNames = cacheManager.getCacheNames();
        for (int i = 0; i < cacheNames.length; i++) {
            String cacheName = cacheNames[i];
            Ehcache cache = cacheManager.getEhcache(cacheName);
            try {
              if (cache.getStatus().equals(Status.STATUS_ALIVE) ) {
                //CH 20110808: Uh, sometimes there is an underlying null pointer here, not sure why?  on Cache.memoryStore
                cache.removeAll();
              } else {
                //i dont know, maybe remove it?
                cacheManager.removeCache(cacheName);
              }
            } catch (Throwable t) {
              LOG.warn("Problem removing cache (non fatal?)", t);
            }
        }

      }
    }
  }
  
}
