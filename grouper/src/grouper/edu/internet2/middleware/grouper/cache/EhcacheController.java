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
import java.io.File;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * Base class for common cache operations.
 * @author  blair christensen.
 * @version $Id: EhcacheController.java,v 1.13 2009-08-11 20:34:18 mchyzer Exp $
 * @since   1.2.1
 */
public class EhcacheController implements CacheController {

  /**
   * 
   */
  public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
  
  /**
   * singleton cache controller
   */
  private static EhcacheController ehcacheController = null;

  /**
   * utility cache controller if you dont want to create your own...
   * @return ehcache controller
   */
  public static EhcacheController ehcacheController() {
    if (ehcacheController == null) {
      ehcacheController = new EhcacheController();
    }
    return ehcacheController;
  }
 
  /**
   * manager
   */
  private CacheManager mgr;

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.stop();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    if (this.mgr != null) {
      synchronized(CacheManager.class) {
        this.mgr.shutdown();
      }
    }
  }

 
  /**
   * Initialize caching.
   * @since   1.2.1
   */
  public EhcacheController() {
  }



  /**
   * Flush all caches.
   * @since   1.2.1
   */
  public void flushCache() {
    if (this.mgr != null) {
      this.mgr.clearAll(); // TODO 20070823 how much of a performance hit is calling this method?
    }
  }

  /**
   * Retrieve a grouper cache (like a generic Map)
   * @param name should be unique, prefix with fully qualified classname
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public GrouperCache getGrouperCache(String name) 
    throws  IllegalStateException { 
    //dont use defaults
    return getGrouperCache(name, false, -1, false, -1, -1, false);
  }
  
  /**
   * Retrieve a GrouperCache which is a generic Map cache.  Note the defaults are only used
   * in the first invocation of the cache retrieval.
   * @param name should be unique, prefix with fully qualified classname
   * @param useDefaultIfNotInConfigFile use the defaults if not in the config file
   * @param defaultMaxElementsInMemory if not in config file, this is max elements in memory
   * @param defaultEternal if not in config file,  true to never expire stuff
   * @param defaultTimeToIdleSeconds  if not in config file, time where if not accessed, will expire
   * @param defaultTimeToLiveSeconds  if not in config file, time where even if accessed, will expire
   * @param defaultOverflowToDisk  if not in config file, if it should go to disk in overflow
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public GrouperCache getGrouperCache(String name, boolean useDefaultIfNotInConfigFile,
      int defaultMaxElementsInMemory, 
      boolean defaultEternal, int defaultTimeToIdleSeconds, 
      int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) 
    throws  IllegalStateException { 
    return new GrouperCache(this.getCache(name, useDefaultIfNotInConfigFile, 
        defaultMaxElementsInMemory, defaultEternal, defaultTimeToIdleSeconds, 
        defaultTimeToLiveSeconds, defaultOverflowToDisk));
  }
  
  /**
   * Note, this might be better to be used from GrouperCache
   * @param name should be unique, prefix with fully qualified classname
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public Cache getCache(String name) 
    throws  IllegalStateException { 
    //dont use defaults
    return getCache(name, false, -1, false, -1, -1, false);
  }
  
  /**
   * Note, this might be better to be used from GrouperCache
   * @param name should be unique, prefix with fully qualified classname
   * @param useDefaultIfNotInConfigFile use the defaults if not in the config file
   * @param defaultMaxElementsInMemory if not in config file, this is max elements in memory
   * @param defaultEternal if not in config file,  true to never expire stuff
   * @param defaultTimeToIdleSeconds  if not in config file, time where if not accessed, will expire
   * @param defaultTimeToLiveSeconds  if not in config file, time where even if accessed, will expire
   * @param defaultOverflowToDisk  if not in config file, if it should go to disk in overflow
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public Cache getCache(String name, boolean useDefaultIfNotInConfigFile,
      int defaultMaxElementsInMemory, 
      boolean defaultEternal, int defaultTimeToIdleSeconds, 
      int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) 
    throws  IllegalStateException { 
    this.initialize();
    if (this.mgr.cacheExists(name) ) {
      return this.mgr.getCache(name);
    }
    if (useDefaultIfNotInConfigFile) {
      if (LOG != null) {
        LOG.info("cache not configured explicitly: " + name + ", to override default values, " +
        		"configure in the resource /grouper.ehcache.xml.  Default values are:" +
        		"maxElementsInMemory: " + defaultMaxElementsInMemory + ", eternal: " + defaultEternal
        		+ ", timeToIdleSeconds: " + defaultTimeToIdleSeconds + ", timeToLiveSeconds: " 
        		+ defaultTimeToLiveSeconds + ", overFlowToDisk: " + defaultOverflowToDisk);
      }
      Cache cache = new Cache(name, defaultMaxElementsInMemory, defaultOverflowToDisk, 
          defaultEternal, defaultTimeToLiveSeconds, defaultTimeToIdleSeconds);
      //TODO CH 20081118, see if cache by name there already...
      this.mgr.addCache(cache);
      return cache;
    }
    
    throw new IllegalStateException("cache not found: " + name + " make sure the cache" +
        " config is correct, the resource: /grouper.ehcache.xml");
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(EhcacheController.class);

  /**
   * @param cache 
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    //not sure if we need to initialize, since no stats will be found...
    this.initialize();
    Cache c = this.getCache(cache);
    c.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
    return new EhcacheStats( c.getStatistics() );
  }

  /** original tmp dir */
  public static final String ORIGINAL_TMP_DIR = System.getProperty(JAVA_IO_TMPDIR);
  
  /** 
   * Initialize privilege cache.
   * @since   1.2.1
   */
  public void initialize() {
    if (this.mgr == null) {
      synchronized(EhcacheController.class) {
        if (this.mgr == null) {
          URL url = this.getClass().getResource("/grouper.ehcache.xml");
          if (url == null) {
            throw new RuntimeException("Cant find resource /grouper.ehcache.xml, " +
                "make sure it is on the classpath");
          }
          
          //trying to avoid warning of using the same dir
          try {
            String newTmpdir = StringUtils.trimToEmpty(ORIGINAL_TMP_DIR);
            if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
              newTmpdir += File.separator;
            }
            newTmpdir += "grouper_ehcache_auto_" + GrouperUtil.uniqueId();
            System.setProperty(JAVA_IO_TMPDIR, newTmpdir);
            
            synchronized(CacheManager.class) {
              //now it should be using a unique directory
              this.mgr = new CacheManager(url);
            }
          } finally {
            //put tmpdir back
            if (ORIGINAL_TMP_DIR == null) {
              System.clearProperty(JAVA_IO_TMPDIR);
            } else {
              System.setProperty(JAVA_IO_TMPDIR, ORIGINAL_TMP_DIR);
            }
          }
          
        }
      }
    }
  }
  
}

