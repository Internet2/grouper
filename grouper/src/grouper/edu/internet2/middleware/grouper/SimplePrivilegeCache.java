/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.collections.map.*;

/** 
 * A simple caching implementation of {@link PrivilegeCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimplePrivilegeCache.java,v 1.1 2006-08-21 18:46:10 blair Exp $
 * @since   1.1.0     
 */
public class SimplePrivilegeCache extends BasePrivilegeCache {

  // PRIVATE INSTANCE VARIABLES //
  private MultiKeyMap cache = MultiKeyMap.decorate(new HashedMap());


  // PUBLIC INSTANCE METHODS //

  /**
   * Cache a {@link Privilege} without any side-effects.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void add(Owner o, Subject subj, Privilege p, boolean hasPriv)
    throws  PrivilegeCacheException
  {
    // Store the value without any cache flushing
    this.cache.put(
      o, p, subj, new PrivilegeCacheElement(o, subj, p, hasPriv)
    );
  } // public void add(o, subj, p, hasPriv)

  /**
   * Retrieve a potentially cached {@link Privilege}.
   * <p/>
   * @return  A {@link PrivilegeCacheElement} or {@link nullPrivilegeCacheElement}.
   * @since   1.1.0
   */
  public PrivilegeCacheElement get(Owner o, Subject subj, Privilege p) {
    if (this.cache.containsKey(o, p, subj)) {
      return (PrivilegeCacheElement) this.cache.get(o, p, subj);
    }
    return new NullPrivilegeCacheElement(o, subj, p);
  } // public PrivilegeCacheElement get(o, subj, p)

  /**
   * Remove all cached entries for {@link Privilege} on {@link Owner}.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void remove(Owner o, Privilege p) 
    throws  PrivilegeCacheException
  {
    this.cache.removeAll(o, p);
  } // public void remove(o, p)

  /**
  /**
   * Remove all cached {@link Privilege}s.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void removeAll() 
    throws  PrivilegeCacheException
  {
    this.cache.clear(); 
  } // public void removeAll()

  /**
   * Cache a {@link Privilege}.
   * <p>This will flush existing cached results</p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void update(Owner o, Subject subj, Privilege p, boolean hasPriv) 
    throws  PrivilegeCacheException
  {
    this.removeAll();               // Flush everything
    if (hasPriv) {
      this.add(o, subj, p, hasPriv);  // And then store this
    }
  } // public void update(o, subj, p, hasPriv)

} // public class SimplePrivilegeCache extends BasePrivilegeCache

