/*
 * @author mchyzer
 * $Id: GrouperCache.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;


/**
 * wrapper around ehcache which makes it genericized
 * @param <K> 
 * @param <V> 
 */
public class GrouperCache<K,V> {

  /** cache that this wraps */
  private Cache cache = null;
  
  /**
   * 
   * @param theCache
   */
  public GrouperCache(Cache theCache) {
    this.cache = theCache;
  }

  /**
   * construct with cache name
   * @param cacheName should be unique, prefix with fully qualified classname
   */
  public GrouperCache(String cacheName) {
    this(EhcacheController.ehcacheController().getCache(cacheName));
  }

  /**
   * construct with cache name
   * @param cacheName should be unique, prefix with fully qualified classname
   * @param defaultMaxElementsInMemory if not in config file, this is max elements in memory
   * @param defaultEternal if not in config file,  true to never expire stuff
   * @param defaultTimeToIdleSeconds  if not in config file, time where if not accessed, will expire
   * @param defaultTimeToLiveSeconds  if not in config file, time where even if accessed, will expire
   * @param defaultOverflowToDisk  if not in config file, if it should go to disk in overflow
   */
  public GrouperCache(String cacheName, int defaultMaxElementsInMemory, 
      boolean defaultEternal, int defaultTimeToIdleSeconds, 
      int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) {
    this(EhcacheController.ehcacheController().getCache(cacheName, true, defaultMaxElementsInMemory, defaultEternal, defaultTimeToIdleSeconds, defaultTimeToLiveSeconds, defaultOverflowToDisk));
  }
  
  
  /**
   * generally you wont need this method, but if you need any methods not exposed in this class,
   * use the cache directly...
   * @return the cache
   */
  public Cache getCache() {
    return this.cache;
  }
  
  /**
   * get a value or null if not there or expired
   * this will check for eviction, and evict if evictable
   * @param key
   * @return the value or null if not there or evicted
   */
  public synchronized V get(K key) {
    Element element = this.cache.get(key);
    //note dont use getValue since the vlaue might not be serializable
    return element == null ? null : (V)element.getObjectValue();
  }

  /**
   * put a value into the cache, accept the default time to live for this cache
   * @param key
   * @param value
   */
  public synchronized void put(K key, V value) {
    this.cache.put(new Element(key, value));
  }
  

}
