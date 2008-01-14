/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.cache;
import java.net.URL;

import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheStats;
import  net.sf.ehcache.Cache;
import  net.sf.ehcache.CacheManager;
import  net.sf.ehcache.Statistics;


/**
 * Base class for common cache operations.
 * @author  blair christensen.
 * @version $Id: EhcacheController.java,v 1.5 2008-01-14 04:53:44 mchyzer Exp $
 * @since   1.2.1
 */
public class EhcacheController implements CacheController {

 
  private CacheManager mgr;


 
  /**
   * Initialize caching.
   * @since   1.2.1
   */
  public EhcacheController() {
    this.initialize();
  }



  /**
   * Flush all caches.
   * @since   1.2.1
   */
  public void flushCache() {
    this.mgr.clearAll(); // TODO 20070823 how much of a performance hit is calling this method?
  }

  /**
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public Cache getCache(String name) 
    throws  IllegalStateException
  {
    if ( this.mgr.cacheExists(name) ) {
      return this.mgr.getCache(name);
    }
    throw new IllegalStateException( "cache not found: " + name );
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    Cache c = this.getCache(cache);
    c.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
    return new EhcacheStats( c.getStatistics() );
  }

  /** 
   * Initialize privilege cache.
   * @since   1.2.1
   */
  public void initialize() {
    URL url = this.getClass().getResource("/grouper.ehcache.xml");
    if (url == null) {
      throw new RuntimeException("Cant find resourse /grouper.ehcache.xml, " +
      		"make sure it is on the classpath");
    }
    this.mgr = new CacheManager(url);
  }
  
}

