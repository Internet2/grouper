package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import net.sf.ehcache.Statistics;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Objects;

/**
 * A GrouperCache that also provides controls of how old elements are when they are
 * requested. This might not be needed if GrouperCache provided access to its Element<V>
 * wrapper, but this is so thin that it does not seem worth polluting GrouperCache with
 * this time-based access control.
 *
 * @param <K>
 * @param <V>
 */
public class PspDatedCache<K, V> {
  GrouperCache<K, PspDatedCacheElement<V>> cache;

  public PspDatedCache(String cacheName, int defaultMaxElementsInMemory, boolean defaultEternal, int defaultTimeToIdleSeconds, int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) {
    cache = new GrouperCache<K, PspDatedCacheElement<V>>(cacheName, defaultMaxElementsInMemory, defaultEternal, defaultTimeToIdleSeconds, defaultTimeToLiveSeconds, defaultOverflowToDisk);
  }

  public synchronized V get(K key, long oldestAllowedAge_ms) {
    PspDatedCacheElement<V> result = cache.get(key);

    if ( result==null ) {
      return null;
    }

    return result.getValueIfNewEnough(oldestAllowedAge_ms);
  }


  public synchronized V get(K key, DateTime oldestAllowedAge) {
    if ( oldestAllowedAge!=null ) {
      return get(key, oldestAllowedAge.getMillis());
    } else {
      return get(key, 0);
    }
  }

  public synchronized void put(K key, V value) {
    V existingValue = get(key, 0);

    // If this is a change in value, put it into the Cache. Otherwise, keep the old
    // value so its age is correctly tracked
    if ( !Objects.equals(existingValue, value) ) {
      cache.put(key, new PspDatedCacheElement<V>(value));
    }
  }

  public synchronized V remove(K key) {
    PspDatedCacheElement<V> result = cache.remove(key);
    if ( result!=null ) {
      return result.getValue();
    }

    return null;
  }

  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  public Statistics getStats() { return cache.getStats(); }

}
