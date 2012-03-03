/*
 * @author mchyzer
 * $Id: GrouperCache.java,v 1.4 2009-02-09 21:36:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;


/**
 * wrapper around ehcache which makes it genericized
 * @param <K> 
 * @param <V> 
 */
public class GrouperCache<K,V> {

  /**
   * values
   * @return the collection of values
   */
  public Collection<V> values() {
    Collection<V> result = new ArrayList<V>();
    for (K key : this.keySet()) {
      
      V value = this.get(key);
      if (value != null) {
        result.add(value);
      }
      
    }
    return result;
  }
  
  /**
   * remove all in cache
   */
  public void clear() {
    this.cache.removeAll();
  }
  
  /** cache that this wraps */
  private Cache cache = null;
  

  /**
   * 
   * @return cache controller
   */
  public Cache internal_getCache() {
    return this.cache;
  }

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
    this.clear();
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
   * see if the cache has this element in it.  This updates cache stats
   * @param key
   * @return true if contains key (though value still could be null)
   */
  public synchronized boolean containsKey(K key) {
    Element element = this.cache.get(key);
    return element != null;
  }
  
  /**
   * get a set of the keys in the cache
   * @return the set of the keys, never returns null
   */
  public synchronized Set<K> keySet() {
    List keyList = GrouperUtil.nonNull(this.cache.getKeys());
    Set<K> result = new LinkedHashSet<K>(keyList);
    return result;
  }
  
  /**
   * remove an item if it exists
   * @param key
   * @return the previous value associated or null (to match Map interface)
   */
  public synchronized V remove(K key) {
    V result = this.get(key);
    this.cache.remove(key);
    return result;
  }
  
  /**
   * put a value into the cache, accept the default time to live for this cache
   * @param key
   * @param value
   */
  public synchronized void put(K key, V value) {
    this.cache.put(new Element(key, value));
  }
  
  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public Statistics getStats() {
    return this.cache.getStatistics();
  }

}
