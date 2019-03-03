package edu.internet2.middleware.grouper.pspng;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * This is a wrapper that stores a value along with the date it was cached.
 * Theoretically, this is not needed if GrouperCache provided access to its
 * Element<V> cached items, but it seems easiest to create a thin wrapper
 * so that GrouperCache's API is not made more complicated.
 *
 * This is used by the PspDatedCache so that cached values can be used only
 * when they're "new enough" to satisfy the current request.
 *
 * @param <V>
 */
public class PspDatedCacheElement<V> {
  protected long storedTime_ms = System.currentTimeMillis();
  protected V value;

  public PspDatedCacheElement(V value) {
    this.value = value;
  }

  public boolean isNewEnough(DateTime oldestAllowedAge) {
    return !isOlderThan(oldestAllowedAge);
  }


  public boolean isNewEnough(long oldestAllowedAge_ms) {
    return !isOlderThan(oldestAllowedAge_ms);
  }

  public boolean isOlderThan(DateTime aDate) {
    return isOlderThan(aDate.getMillis());
  }

  public boolean isOlderThan(long time_ms) {
    return storedTime_ms < time_ms;
  }

  public V getValue() {
    return value;
  }


  /**
   * Get the value if it is new enough
   * @param oldestAllowedAge the date you require the object to be fresher than.
   *                         Null means to bypass age checks and always return the object.
   * @return the saved value, or null if the saved value was too old
   */
  public V getValueIfNewEnough(DateTime oldestAllowedAge) {
    if ( oldestAllowedAge==null ) {
      return getValue();
    }

    return getValueIfNewEnough(oldestAllowedAge.getMillis());
  }


  /**
   * Get the value if it is new enough. otherwise null is returned
   * @param oldestAllowedAge_ms the java epoch that you wish the object to be
   *                            fresher than. <=0 means to bypass age checks and
   *                            always return the object.
   * @return the saved value, or null if the saved value was too old
   */
  public V getValueIfNewEnough(long oldestAllowedAge_ms) {
    if ( oldestAllowedAge_ms <= 0 ) {
      return getValue();
    }

    if ( isOlderThan(oldestAllowedAge_ms) ) {
      return null;
    } else {
      return value;
    }
  }

  /**
   * Only compares CacheElements based on the value and not the date they were cached.
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if ( o==null && value==null ) return true;

    if ( o==null ) return false;

    // Be able to compare this Cache item to the class it stores
    if (value.getClass().isInstance(o)) return Objects.equals(value, o);

    if (o == null || getClass() != o.getClass()) return false;
    PspDatedCacheElement<?> that = (PspDatedCacheElement<?>) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
