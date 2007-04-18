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

package edu.internet2.middleware.grouper.internal.cache;
import  java.util.HashMap;
import  java.util.Map;

/** 
 * Simple cache implementation.
 * <p><b>NOTE: THIS IS AN EVOLVING IMPLEMENTATION.</b></p>
 * <p>
 * This class provides a uniform API for caching throughout the Grouper API.  The default
 * implementation is just a thin wrapper around a {@link HashMap}.  While all of the
 * current implementations and extensions to {@link SimpleCache} use a {@link HashMap} in
 * some form, in theory other mechanisms can be used.
 * </p>
 * <p><b>Other Implementations</b></p>
 * <p>
 * {@link SimpleBooleanCache} is a wrapper around {@link SimpleCache} that allows
 * <i>boolean</i> values to be stored-and-retrieved in the cache.  {@link SimplePrivilegeCache},
 * {@link SimpleWheelPrivilegeCache} and {@link SimpleSubjectCache} all use {@link SimpleCache}
 * internally.
 * </p>
 * <p><b>Using {@link SimpleCache}</b></p>
 * <pre class="eg">
 * // Create a new cache
 * SimpleCache cache = new SimpleCache();
 *
 * // Add an object to the cache
 * cache.put("key", "value");
 *
 * // Check if the cache contains the specified key
 * boolean rv = cache.containsKey("key");
 * 
 * // Get an object from the cache
 * String value = cache.get("key");
 * 
 * // Remove an object from the cache
 * cache.remove("key");
 *
 * // Remove all elements from the cache
 * cache.removeAll();
 * </pre>
 * @author  blair christensen.
 * @version $Id: SimpleCache.java,v 1.2 2007-04-18 15:56:59 blair Exp $
 * @since   1.2.0     
 */
public class SimpleCache {

  // PRIVATE INSTANCE VARIABLES //
  private Object cache;


  // CONSTRUCTORS //

  /**
   * Initialize a new {@link SimpleCache}.
   * <p/>
   * @since   1.2.0
   */
  public SimpleCache() {
    this.setCache( new HashMap() );
  } // public SimpleCache()


  // PUBLIC INSTANCE METHODS //

  /**
   * Does cache contain key?
   * <p/>
   * @return  true if this cache contains this key.
   * @since   1.2.0
   */
  public boolean containsKey(Object key) {
    return ( (Map) this.getCache() ).containsKey(key);
  } 

  /**
   * Retrieve a cached {@link Object}.
   * <p/>
   * @return  Cached {@link Object} or null.
   * @since   1.2.0
   */
  public Object get(Object key) {
    return ( (Map) this.getCache() ).get(key);
  } 

  /**
   * Retrieve the raw cache.
   * <p/>
   * @return  The actual cache {@link Object}. 
   * @since   1.2.0
   */
  public Object getCache() {
    return this.cache;
  } 

  /**
   * Cache an {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  public void put(Object key, Object value) {
    ( (Map) this.getCache() ).put(key, value);  
  } 

  /**
   * Remove an {@link Object} from the cache.
   * <p/>
   * @return  The removed {@link Object}.
   * @since   1.2.0
   */
  public Object remove(Object key) {
    return ( (Map) this.getCache() ).remove(key);
  } 

  /**
   * Remove all cached {@link Object}s.
   * <p/>
   * @since   1.2.0
   */
  public void removeAll() {
    this.setCache( new HashMap() );
  } 

  /**
   * Set the cache {@link Object}.
   * <p/>
   * @since   1.2.0
   */
  protected void setCache(Object cache) {
    this.cache = cache;
  } // protected void setCache(cache)

} 

