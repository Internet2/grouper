/**
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
 */
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
