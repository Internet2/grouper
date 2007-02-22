/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper;
import  java.util.HashMap;
import  java.util.Map;

/** 
 * Simple cache implementation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimpleCache.java,v 1.2 2007-02-22 18:01:38 blair Exp $
 * @since   1.2.0     
 */
class SimpleCache {

  // PRIVATE INSTANCE VARIABLES //
  private Map cache = new HashMap();


  // PUBLIC INSTANCE METHODS //

  /**
   * Does cache contain key?
   * <p/>
   * @return  true if this cache contains this key.
   * @since   1.2.0
   */
  protected boolean containsKey(Object key) {
    return ( (Map) this.getCache() ).containsKey(key);
  } // protected boolean containsKey(key)

  /**
   * Retrieve a cached {@link Object}.
   * <p/>
   * @return  Cached {@link Object} or null.
   * @since   1.2.0
   */
  protected Object get(Object key) {
    return ( (Map) this.getCache() ).get(key);
  } // protected Object get(key)

  /**
   * Retrieve the raw cache.
   * <p/>
   * @return  The actual cache {@link Object}. 
   * @since   1.2.0
   */
  protected Object getCache() {
    return this.cache;
  } // protected Object getCache()

  /**
   * Cache an {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  protected void put(Object key, Object value) {
    ( (Map) this.getCache() ).put(key, value);  
  } // protected void put(key, value)

  /**
   * Remove all cached {@link Object}s.
   * <p/>
   * @since   1.2.0
   */
  void removeAll() {
    this.setCache( new HashMap() );
  } // protected void removeAll()

  /**
   * Set the cache {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  protected void setCache(Object cache) {
    this.cache = (Map) cache;
  } // protected void setCache(cache)

} // class SimpleCache

