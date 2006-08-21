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
 * Base implementation of {@link PrivilegeCache}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BasePrivilegeCache.java,v 1.2 2006-08-21 19:20:09 blair Exp $
 * @since   1.1.0     
 */
public abstract class BasePrivilegeCache implements PrivilegeCache {

  // CONSTRUCTORS //
  
  // @since   1.1.0
  protected BasePrivilegeCache() {
    super();
  } // protected BasePrivilegeCache()

 
  // PUBLIC CLASS METHODS //

  /**
   * @param   klass   Name of the implementing class to return.
   * @return  A {@link PrivilegeCache} implementation.
   * @throws  GrouperRuntimeException
   * @since   1.1.0
   */
  public static PrivilegeCache getCache(String klass) 
    throws  GrouperRuntimeException
  {
    return (PrivilegeCache) U.realizeInterface(klass);
  } // public static PrivilegeCache getCache(klass)


  // PUBLIC ABSTRACT INSTANCE METHODS //

  /**
   * Retrieve a cached {@link Privilege}.
   * <p/>
   * @return  A {@link PrivilegeCacheElement} or {@link BasePrivilegeCacheElement}.
   * @since   1.1.0
   */
  public abstract PrivilegeCacheElement get(Owner o, Subject subj, Privilege p);

  /**
   * Update to cache to reflect {@link Privilege} granting.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public abstract void grantPriv(Owner o, Subject subj, Privilege p) throws PrivilegeCacheException;

  /**
   * Cache a {@link Privilege}.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public abstract void put(Owner o, Subject subj, Privilege p, boolean hasPriv) throws PrivilegeCacheException;

  /**
   * Remove all cached {@link Privilege}s.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public abstract void removeAll() throws PrivilegeCacheException;

  /**
   * Update to cache to reflect {@link Privilege} revoking.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public abstract void revokePriv(Owner o, Privilege p) throws PrivilegeCacheException;

  /**
   * Update to cache to reflect {@link Privilege} revoking.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public abstract void revokePriv(Owner o, Subject subj, Privilege p) throws PrivilegeCacheException;

} // public class BasePrivilegeCache implements PrivilegeCache

