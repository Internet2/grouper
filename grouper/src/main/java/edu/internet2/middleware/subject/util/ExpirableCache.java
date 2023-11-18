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
package edu.internet2.middleware.subject.util;

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
 * @version $Id$
 * @author mchyzer
 * @param <K> key type
 * @param <V> value type
 * @deprecated use edu.internet2.middleware.grouperClient.util.ExpirableCache
 */
@Deprecated
public class ExpirableCache<K,V> extends edu.internet2.middleware.grouperClient.util.ExpirableCache<K,V> {

  public ExpirableCache() {
    super();
  }

  public ExpirableCache(ExpirableCacheUnit expirableCacheUnit, int defaultTimeToLive) {
    super(expirableCacheUnit, defaultTimeToLive);
  }

  public ExpirableCache(int defaultTimeToLiveInMinutes) {
    super(defaultTimeToLiveInMinutes);
  }

}
