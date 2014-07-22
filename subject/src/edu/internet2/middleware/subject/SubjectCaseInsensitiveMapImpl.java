/**
 * Copyright 2012 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.subject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * linked hash map with case insensitive matching and tolower attribute names (e.g. for keyset)
 * @author mchyzer
 * @param <K> key type must be string
 * @param <V> value type
 *
 */
@SuppressWarnings("serial")
public class SubjectCaseInsensitiveMapImpl<K,V> extends LinkedHashMap<K,V> implements
    SubjectCaseInsensitiveMap {

  /** need to know when the map is initialized */
  private boolean initted = false;
  
  /**
   * need to know when the map is initialized
   * @return if initted
   */
  public boolean isInitted() {
    return this.initted;
  }

  /**
   * 
   */
  public SubjectCaseInsensitiveMapImpl() {
    this.initted = true;
  }
  
  /**
   * The default initial capacity - MUST be a power of two.
   */
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The load factor used when none specified in constructor.
   */
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;


  /**
   * @param m
   */
  public SubjectCaseInsensitiveMapImpl(Map<K,V> m) {
    super(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
        DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
    for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry<? extends K, ? extends V> e = i.next();
      put(e.getKey(), e.getValue());
    }
    this.initted = true;
  }

  /**
   * @see Map#get(Object)
   */
  @Override
  public V get(Object key) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
    return super.get(key);
  }
  
  /**
   * @see Map#containsKey(Object)
   */
  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
    return super.containsKey(key);
  }

  /**
   * @see Map#put(Object, Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public V put(K key, V value) {
    if (key instanceof String) {
      key = (K)((String)key).toLowerCase();
    }
    return super.put(key, value);
  }

  /**
   * @see Map#putAll(Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry<? extends K, ? extends V> e = i.next();
      K key = e.getKey();
      if (key instanceof String) {
        key = (K)((String)key).toLowerCase();
      }
      put(key, e.getValue());
    }
  }

  /**
   * @see Map#remove(Object)
   */
  @Override
  public V remove(Object key) {
    if (key instanceof String) {
      key = ((String)key).toLowerCase();
    }
    return super.remove(key);
  }

  
  
}
