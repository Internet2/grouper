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

/** 
 * A no-caching implementation of {@link PrivilegeCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NoCachePrivilegeCache.java,v 1.2 2006-08-21 19:20:09 blair Exp $
 * @since   1.1.0     
 */
public class NoCachePrivilegeCache extends BasePrivilegeCache {

  // PUBLIC INSTANCE METHODS //

  /**
   * Create an uncached {@link Privilege}.
   * <p/>
   * @return  A {@link NullPrivilegeCacheElement}.
   * @since   1.1.0
   */
  public PrivilegeCacheElement get(Owner o, Subject subj, Privilege p) {
    return new NullPrivilegeCacheElement(o, subj, p);
  } // public PrivilegeCacheElement get(o, subj, p)

  /**
   * Update to cache to reflect {@link Privilege} granting.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void grantPriv(Owner o, Subject subj, Privilege p) 
    throws  PrivilegeCacheException
  {
    // Nothing
  } // public void grantPriv(o, subj, p, hasPriv)

  /**
   * Cache a {@link Privilege} without any side-effects.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void put(Owner o, Subject subj, Privilege p, boolean hasPriv)
    throws  PrivilegeCacheException
  {
    // Nothing
  } // public void put(o, subj, p, hasPriv)

  /**
   * Remove all cached {@link Privilege}s.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void removeAll() 
    throws  PrivilegeCacheException
  {
    // Nothing
  } // public void removeAll()

  /**
   * Update to cache to reflect {@link Privilege} revoking.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void revokePriv(Owner o, Privilege p) 
    throws  PrivilegeCacheException
  {
    // Nothing
  } // public void revokePriv(o, p)

  /**
   * Update to cache to reflect {@link Privilege} revoking.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void revokePriv(Owner o, Subject subj, Privilege p) 
    throws  PrivilegeCacheException
  {
    // Nothing
  } // public void revokePriv(o, subj, p)

} // public class NoCachePrivilegeCache extends BasePrivilegeCache

