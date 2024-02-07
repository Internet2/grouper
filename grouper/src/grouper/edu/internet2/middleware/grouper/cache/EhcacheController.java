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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperCacheConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;



/**
 * Base class for common cache operations.
 * @author  blair christensen.
 * @version $Id: EhcacheController.java,v 1.13 2009-08-11 20:34:18 mchyzer Exp $
 * @since   1.2.1
 */
public class EhcacheController implements CacheController {

  /**
   * save the config
   */
  private Configuration configuration;
  
  
  /**
   * save the config
   * @return the configuration
   */
  public Configuration getconfiguration() {
    return this.configuration;
  }

  
  /**
   * if configured via properties and not ehcache.xml
   */
  private boolean configuredViaProperties = false;

  /**
   * if configured via properties and not ehcache.xml
   * @return the configuredViaProperties
   */
  public boolean isConfiguredViaProperties() {
    return this.configuredViaProperties;
  }

  /**
   * singleton cache controller
   */
  private static EhcacheController ehcacheController = null;

  /**
   * get the mgr
   * @return the mge
   */
  public CacheManager getCacheManager() {
    return this.mgr;
  }
  
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
   * not public since we only want one of these...
   * Initialize caching.
   * @since   2.1.0
   */
  private EhcacheController() {
  }



  /**
   * Flush all caches.
   * @since   1.2.1
   */
  public void flushCache() {
    if (this.mgr != null && this.mgr.getStatus() == Status.STATUS_ALIVE) {
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
      Cache cache = this.mgr.getCache(name);
      return cache;

    }
    if (useDefaultIfNotInConfigFile) {
      if (LOG != null) {
        LOG.info("cache not configured explicitly: " + name + ", to override default values, " +
        		"configure in the resource /grouper.cache.base.properties and /grouper.cache.properties.  Default values are:" +
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
        " config is correct, the resource: /grouper.cache.base.properties and /grouper.cache.properties");
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(EhcacheController.class);

  /** 
   * Initialize privilege cache.
   * @since   1.2.1
   */
  public void initialize() {
    if (this.mgr == null) {
      synchronized(EhcacheController.class) {
        if (this.mgr == null) {

          URL grouperCachePropertiesUrl = this.getClass().getResource("/grouper.cache.properties");
          URL ehcacheUrl = null;
          boolean ehcacheXmlEligible = !GrouperVersion.valueOfIgnoreCase("2.3.1").greaterOrEqualToArg(GrouperVersion.currentVersion());
          if (!ehcacheXmlEligible) {
            ehcacheUrl = null;
          } else {
            ehcacheUrl = this.getClass().getResource("/ehcache.xml");
            if (ehcacheUrl != null) {
              LOG.error("ERROR: You have an ehache.xml on the classpath, "
                  + "you must convert the ehcache.xml file to grouper.cache.properties and remove it.");
            }
            ehcacheUrl = null;
          }

          //trying to avoid warning of using the same dir
          String tmpDir = GrouperUtil.tmpDir();
          try {
            String newTmpdir = StringUtils.trimToEmpty(tmpDir);
            if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
              newTmpdir += File.separator;
            }
            newTmpdir += "grouper_ehcache_auto_" + GrouperUtil.uniqueId();
            System.setProperty(GrouperUtil.JAVA_IO_TMPDIR, newTmpdir);
          
            synchronized(CacheManager.class) {

              if (grouperCachePropertiesUrl != null && ehcacheUrl != null) {
                if (ehcacheXmlEligible) {
                  LOG.error("ERROR: You have an ehache.xml and grouper.cache.properties on the classpath, "
                    + "you should only have one or the other.  "
                    + "You should probably delete the ehcache.xml file.");
                } else {
                  LOG.error("ERROR: You have an ehache.xml on the classpath, "
                      + "you should the ehcache.xml file.");
                }
              }

              boolean configured = false;
              
//              //if no grouper.cache.properties url, and an ehcache.xml, then use that
//              if (ehcacheXmlEligible && grouperCachePropertiesUrl == null && ehcacheUrl != null) {
//                LOG.debug("Configuring ehcache with ehcache.xml");
//                //now it should be using a unique directory
//                this.mgr = new CacheManager(ehcacheUrl);
//
//                configured = true;
//              }
              
              GrouperCacheConfig grouperCacheConfig = configured ? null : GrouperCacheConfig.retrieveConfig();
//              if (!configured) {
//
//                //use config file?
//                String ehcacheXmlFile = grouperCacheConfig.propertyValueString("grouper.cache.ehcache.xml.filename");
//                
//                if (!StringUtils.isBlank(ehcacheXmlFile)) {
//                  LOG.debug("Configuring ehcache with xml file configured in grouper.cache.properties: " + ehcacheXmlFile);
//                  try {
//                    ehcacheUrl = new File(ehcacheXmlFile).toURI().toURL();
//                  } catch (MalformedURLException mue) {
//                    throw new RuntimeException(mue);
//                  }
//                  this.mgr = new CacheManager(ehcacheUrl);
//                  configured = true;
//                }
//              }
//              if (!configured) {
//
//                //use config file?
//                String ehcacheXmlResource = grouperCacheConfig.propertyValueString("grouper.cache.ehcache.xml.resource");
//                
//                if (!StringUtils.isBlank(ehcacheXmlResource)) {
//                  LOG.debug("Configuring ehcache with xml resource configured in grouper.cache.properties: " + ehcacheXmlResource);
//                  ehcacheUrl = this.getClass().getResource(ehcacheXmlResource);
//                  this.mgr = new CacheManager(ehcacheUrl);
//                  configured = true;
//                }
//              }

              if (!configured) {
                this.configuredViaProperties = true;
                LOG.debug("Configuring ehcache with grouper.cache.properties");
                
                
                
                Configuration ehcacheConfiguration = new Configuration();
                
                //disk store
                {
                  DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
                  diskStoreConfiguration.setPath(grouperCacheConfig.propertyValueStringRequired("grouper.cache.diskStorePath"));
                  ehcacheConfiguration.addDiskStore(diskStoreConfiguration);
                }

                {
                  //default cache
                  CacheConfiguration defaultCache = new CacheConfiguration();
                  assignCacheFromProperties(grouperCacheConfig, defaultCache, "cache.defaultCache");
                  ehcacheConfiguration.addDefaultCache(defaultCache);
                }
                
                //set all caches
                Pattern ehcachePattern = Pattern.compile("^(cache\\.name\\.[^.]+)\\.name$");
                Map<String, String> cacheNameMap = grouperCacheConfig.propertiesMap(ehcachePattern);
                for (String propertyKey : cacheNameMap.keySet()) {
                  Matcher matcher = ehcachePattern.matcher(propertyKey);
                  matcher.matches();
                  String configPrefix = matcher.group(1);
                  CacheConfiguration cacheConfiguration = new CacheConfiguration();
                  assignCacheFromProperties(grouperCacheConfig, cacheConfiguration, configPrefix);
                  ehcacheConfiguration.addCache(cacheConfiguration);
                }

                this.configuration = ehcacheConfiguration;
                this.mgr = new CacheManager(ehcacheConfiguration);
                
                configured = true;

              }
              if (!configured) {
                throw new RuntimeException("ehcache is not configured, do you have a "
                    + "grouper.cache.properties and grouper.cache.base.properties on your classpath?");
              }
            }
          } finally {
            
            //put tmpdir back
            if (tmpDir == null) {
              System.clearProperty(GrouperUtil.JAVA_IO_TMPDIR);
            } else {
              System.setProperty(GrouperUtil.JAVA_IO_TMPDIR, tmpDir);
            }
          }
        }
      }
    }
  }

  /**
   * assign cache from properties
   * @param grouperCacheConfig 
   * @param cacheConfiguration
   * @param propertyPrefix
   */
  private static void assignCacheFromProperties(GrouperCacheConfig grouperCacheConfig, CacheConfiguration cacheConfiguration, String propertyPrefix) {
    //  cache.defaultCache.maxElementsInMemory = 1000
    //  cache.defaultCache.eternal = 1000
    //  cache.defaultCache.timeToIdleSeconds = 1000
    //  cache.defaultCache.timeToLiveSeconds = 1000
    //  cache.defaultCache.overflowToDisk = false
    //  cache.defaultCache.statistics = false
    if (grouperCacheConfig.containsKey(propertyPrefix + ".name")) {
      cacheConfiguration.setName(grouperCacheConfig.propertyValueStringRequired(propertyPrefix + ".name"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".maxElementsInMemory")) {
      cacheConfiguration.setMaxElementsInMemory(grouperCacheConfig.propertyValueInt(propertyPrefix + ".maxElementsInMemory"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".eternal")) {
      cacheConfiguration.setEternal(grouperCacheConfig.propertyValueBooleanRequired(propertyPrefix + ".eternal"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".timeToIdleSeconds")) {
      cacheConfiguration.setTimeToIdleSeconds(grouperCacheConfig.propertyValueInt(propertyPrefix + ".timeToIdleSeconds"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".timeToLiveSeconds")) {
      cacheConfiguration.setTimeToLiveSeconds(grouperCacheConfig.propertyValueInt(propertyPrefix + ".timeToLiveSeconds"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".overflowToDisk")) {
      cacheConfiguration.setOverflowToDisk(grouperCacheConfig.propertyValueBooleanRequired(propertyPrefix + ".overflowToDisk"));
    }
    if (grouperCacheConfig.containsKey(propertyPrefix + ".statistics")) {
      cacheConfiguration.setStatistics(grouperCacheConfig.propertyValueBooleanRequired(propertyPrefix + ".statistics"));
    }

  }

  @Override
  public CacheStats getStats(String cache) {
    return null;
  }
  
}

