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
package edu.internet2.middleware.grouperClient.util;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This is like a map, but the setters also take an expire time which 
 * will mean the items in the cache will be automatically deleted.  
 * Also, every so often (e.g. 2 minutes) all items are checked for expiration.
 * If no timetolive is set, it will default to 1 day.  The max time to live is
 * one day.
 * This is synchronized so that all access is safe.
 * <p />
 * Note, evictions are check for periodically, but only when the map is accessed (and even then only every few minutes).
 * so you can check for evictions externally, or clear the map if you are done with it.
 * @version $Id: ExpirableCache.java,v 1.1 2008-11-27 14:25:50 mchyzer Exp $
 * @author mchyzer
 * @param <K> key type
 * @param <V> value type
 */
@SuppressWarnings("serial")
public class ExpirableCache<K,V> implements Serializable {

  /**
   * if there is a database clearable cache with this name, clear it
   * @param name
   * @return true if there was such a cache
   */
  public static boolean clearCache(String name) {
    
    WeakReference<ExpirableCache<?,?>> weakReference = databaseClearableCaches.get(name);
    
    ExpirableCache<?,?> expirableCache = weakReference == null ? null : weakReference.get();
    
    if (expirableCache == null) {
      // assume no race condition here :)
      databaseClearableCaches.remove(name);
      return false;
    }
    expirableCache.clear();
    return true;
  }
  
  /**
   * if the cache should be able to clear across JVMs through the database
   */
  private static Map<String, WeakReference<ExpirableCache<?,?>>> databaseClearableCaches 
    = Collections.synchronizedMap(new HashMap<String, WeakReference<ExpirableCache<?,?>>>());

  /**
   * if database clearable across jvms
   */
  private boolean databaseClearable = false;
  
  /**
   * database clearable name
   */
  private String databaseClearableName = null;
  
  /**
   * register a cache for database clearable.  Note you cant register one that is already there
   * @param name
   */
  public void registerDatabaseClearableCache(String name) {
    
    if (databaseClearableCaches.containsKey(name)) {
      throw new RuntimeException("Cache name already in use: '" + name + "'");
    }
    this.databaseClearable = true;
    this.databaseClearableName = name;
    databaseClearableCaches.put(name, new WeakReference<ExpirableCache<?,?>>(this));
  }
  
  /** max time to live in millis */
  static long MAX_TIME_TO_LIVE_MILLIS = 1000 * 60 * 60 * 24; //1 day

  /** time to live for content (when not specified this is one day, and max one day) */
  long defaultTimeToLiveInMillis = MAX_TIME_TO_LIVE_MILLIS;
  
  /** time between looking for evictions in millis, default to two minutes */
  static long TIME_BETWEEN_EVICTIONS_MILLIS = 2 * 60 * 1000;
  
  /** last time the cache was checked for evictions */
  long lastEvictionCheck = System.currentTimeMillis();
  
  /** cache map */
  private Map<K,ExpirableValue<V>> cache = new HashMap<K,ExpirableValue<V>>();
  
  /** number of elements inserted into the cache */
  private int cacheInserts = 0;
  
  /** numebr of times an element was retrieved from cache successfully */
  private int cacheHits = 0;
  
  /** number of evictions from cache when thigns expire */
  private int cacheEvictions = 0;
  
  /** global number of elements inserted into the cache, no need to synchronize */
  private static int globalCacheInserts = 0;
  
  /** numebr of times an element was retrieved from cache successfully, no need to synchronize */
  private static int globalCacheHits = 0;
  
  /** number of evictions from cache when thigns expire, no need to synchronize */
  private static int globalCacheEvictions = 0;
  
  /** when was the last clear of all */
  private static long lastClearStatic = -1;
  
  /** when was the last clear of this instance */
  private long lastClear = System.currentTimeMillis();
  
  /**
   * 
   */
  public ExpirableCache() {
    super();
  }
  
  /**
   * delete the cache
   *
   */
  public synchronized void clear() {
    this.cache.clear();
  }

  /**
   * @param defaultTimeToLiveInMinutes time in minutes is the default cache time to live for content
   */
  public ExpirableCache(int defaultTimeToLiveInMinutes) {
    super();
    if (defaultTimeToLiveInMinutes >=0) {
      //make sure this is less than the max
      long newTimeToLiveMillis = (long)defaultTimeToLiveInMinutes * 60 * 1000;
      if (newTimeToLiveMillis < MAX_TIME_TO_LIVE_MILLIS) {
        this.defaultTimeToLiveInMillis = newTimeToLiveMillis;
      }
    }
  }

  /**
   * unit of time for expirable cache
   * @author mchyzer
   *
   */
  public static enum ExpirableCacheUnit {
    /** minutes */
    MINUTE {

      /** 
       * @see ExpirableCacheUnit#defaultTimeToLiveMillis(int)
       */
      @Override
      public long defaultTimeToLiveMillis(int input) {
        return (long)input * 60 * 1000;
      }
    },
    
    /** seconds */
    SECOND {

      /** 
       * @see ExpirableCacheUnit#defaultTimeToLiveMillis(int)
       */
      @Override
      public long defaultTimeToLiveMillis(int input) {
        return (long)input * 1000;
      }
    };
    
    /** 
     * default time to live based on units
     * @param input
     * @return the millis
     */
    public abstract long defaultTimeToLiveMillis(int input);
    
  }
  
  /**
   * @param defaultTimeToLive time in whatever unit is the default cache time to live for content
   * @param expirableCacheUnit is minutes or seconds
   */
  public ExpirableCache(ExpirableCacheUnit expirableCacheUnit, int defaultTimeToLive) {
    super();
    if (defaultTimeToLive >=0) {
      //make sure this is less than the max
      long newTimeToLiveMillis = expirableCacheUnit.defaultTimeToLiveMillis(defaultTimeToLive);
      if (newTimeToLiveMillis < MAX_TIME_TO_LIVE_MILLIS) {
        this.defaultTimeToLiveInMillis = newTimeToLiveMillis;
      }
    }
  }

  /**
   * expose the length of cache
   * @return length of cache
   */
  public long getDefaultTimeToLiveInMillis() {
    return this.defaultTimeToLiveInMillis;
  }

  /**
   * 
   */
  public void notifyDatabaseOfChanges() {
    if (this.databaseClearable) {
      Class<?> grouperCacheDatabaseClass = null;
      try {
        grouperCacheDatabaseClass = GrouperClientUtils.forName("edu.internet2.middleware.grouper.cache.GrouperCacheDatabase");
      } catch (Exception e) {
        // maybe we are running in client without Grouper there...  so ignore this
        return;
      }
      String realCacheName = "expirableCache__" + this.databaseClearableName;
      GrouperClientUtils.callMethod(grouperCacheDatabaseClass, null, "notifyDatabaseOfCacheUpdate", String.class, realCacheName);
    }
  }
  
  /**
   * put a value into the cache, accept the default time to live for this cache
   * @param key
   * @param value
   */
  public synchronized void put(K key, V value) {

    this.putHelper(key, value, this.defaultTimeToLiveInMillis);
  
  }
  
  /**
   * put a value into the cache, accept the default time to live for this cache
   * @param key
   * @param value
   * @param timeToLiveInMinutes time to live for this item in minutes.
   * If -1 then use the default
   */
  public synchronized void put(K key, V value, int timeToLiveInMinutes) {
        
    //see if the default
    if (timeToLiveInMinutes == -1) {
      this.put(key,value);
      return;
    }
    
    if (timeToLiveInMinutes <= 0) {
      throw new RuntimeException("Time to live in minutes must be greater than 0");
    }
    this.putHelper(key, value, (long)timeToLiveInMinutes * 60 * 1000);
  }

  /**
   * put a value into the cache, accept the default time to live for this cache
   * @param key
   * @param value
   * @param proposedTimeToLiveInMillis millis time to live
   */
  synchronized void putHelper(K key, V value, long proposedTimeToLiveInMillis) {
    
    this.checkForEvictions(true);
    long newTimeToLiveInMillis = this.defaultTimeToLiveInMillis;
    //dont use what was inputted if it is out of range
    if (proposedTimeToLiveInMillis >= 0 
        && proposedTimeToLiveInMillis <= ExpirableCache.MAX_TIME_TO_LIVE_MILLIS) {
      newTimeToLiveInMillis = proposedTimeToLiveInMillis;
    }
    ExpirableValue<V> expirableValue = new ExpirableValue<V>(value, newTimeToLiveInMillis);
    
    //might be expired if not caching
    if (!expirableValue.expired()) {
      this.cache.put(key, expirableValue);
      this.cacheInserts++;
      globalCacheInserts++;
    }
  }
  
  /**
   * clear out all caches everywhere (session, request, context, etc)
   */
  public static void clearAll() {
    lastClearStatic = System.currentTimeMillis();
  }
  
  /**
   * check and remove elements that are stale
   * @param onlyCheckIfNeeded true if only check every so often (e.g. every two minutes)
   */
  public synchronized void checkForEvictions(boolean onlyCheckIfNeeded) {
    long now = System.currentTimeMillis();
    
    //first see if there is an all clear
    if (lastClearStatic >= this.lastClear) {
      this.clear();
      this.lastClear = now;
      return;
    }
    
    if (onlyCheckIfNeeded) {
      if (now - this.lastEvictionCheck < ExpirableCache.TIME_BETWEEN_EVICTIONS_MILLIS) {
        return;
      }
    }
    
    //go through all elements, evict if stale
    Set<K> keySet = this.cache.keySet();
    Iterator<K> keyIterator = keySet.iterator();
    while (keyIterator.hasNext()) {
      K key = keyIterator.next();
      ExpirableValue<V> expirableValue = this.cache.get(key);
      if (expirableValue.expired()) {
        keyIterator.remove();
        this.cacheEvictions++;
        ExpirableCache.globalCacheEvictions++;
      }
    }
    
    //set that we just checked
    this.lastEvictionCheck = now;
  }
  
  /**
   * get a value or null if not there or expired
   * this will check for eviction, and evict if evictable
   * @param key
   * @return the value or null if not there or evicted
   */
  public synchronized V get(K key) {

    this.checkForEvictions(true);
    return this.getHelper(key);
  }
  
  
  /**
   * get all keys
   * this will check for eviction, and evict if evictable
   * @param key
   * @return the value or null if not there or evicted
   */
  public synchronized Set<K> keySet() {
    this.checkForEvictions(true);
    Set<K> keysToReturn = new HashSet<>();
    
    for (K key: this.cache.keySet()) {
      if (this.getHelper(key) != null) {
        keysToReturn.add(key);
      }
    }
    return keysToReturn;
  }
  
  
  /**
   * get a value or null if not there or expired
   * this will check for eviction, and evict if evictable
   * @param key
   * @return the value or null if not there or evicted
   */
  private synchronized V getHelper(K key) {

    ExpirableValue<V> value = this.cache.get(key);
    if (value == null) {
      //shouldnt have a key with no value, probably doesnt exist, but just in case
      this.cache.remove(key);
      return null;
    }
    if (value.expired()) {
      this.cacheEvictions++;
      ExpirableCache.globalCacheEvictions++;
      this.cache.remove(key);
      return null;
    }
    V content = value.getContent();
    this.cacheHits++;
    ExpirableCache.globalCacheHits++;
    return content;
  }
  
  /**
   * number of elements in map (and check for 
   * @param evictEvictables true if we should evict values that are stale 
   * (even if recently checked)
   * @return the number of elements
   */
  public synchronized int size(boolean evictEvictables) {
    if (evictEvictables) {
      this.checkForEvictions(false);
    }
    return this.cache.size();
  }

  
  /**
   * number of items inserted into the cache
   * @return Returns the cacheInserts.
   */
  public int getCacheInserts() {
    return this.cacheInserts;
  }

  
  /**
   * number of items evicted from cache
   * @return Returns the cacheEvictions.
   */
  public int getCacheEvictions() {
    return this.cacheEvictions;
  }

  
  /**
   * number of items successfully retrieved from cache
   * @return Returns the cacheHits.
   */
  public int getCacheHits() {
    return this.cacheHits;
  }

  /**
   * string representation of cache
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    this.checkForEvictions(true);
    return this.getClass().getSimpleName() + ": size: " + this.size(false)
      + ", cacheHits: " + this.getCacheHits() + ", cacheInserts: " 
      + this.getCacheInserts() + ", cacheEvictions: " + this.cacheEvictions;
  }
  
  /**
   * string representation of cache
   * @return the string value
   */
  public static String staticToString() {
    return "ExpirableCacheGlobal, cacheHits: " + globalCacheHits + ", cacheInserts: " 
      + globalCacheInserts + ", cacheEvictions: " + globalCacheEvictions;
  }
}
