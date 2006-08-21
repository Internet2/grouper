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
 * Privilege Cache interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeCache.java,v 1.10 2006-08-21 18:46:10 blair Exp $
 * @since   1.1.0     
 */
interface PrivilegeCache {

  // PUBLIC INSTANCE METHODS //

  /**
   * Cache a {@link Privilege} without any side-effects.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  void add(Owner o, Subject subj, Privilege p, boolean hasPriv) throws PrivilegeCacheException;

  /**
   * Retrieve a cached {@link Privilege}.
   * <p/>
   * @return  A {@link PrivilegeCacheElement} or {@link NullPrivilegeCacheElement}.
   * @since   1.1.0
   */
  PrivilegeCacheElement get(Owner o, Subject subj, Privilege p);

  /**
   * Remove all cached entries for {@link Privilege} on {@link Owner}.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  void remove(Owner o, Privilege p) throws PrivilegeCacheException;

  /**
   * Remove all cached {@link Privilege}s.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  void removeAll() throws PrivilegeCacheException;

  /**
   * Cache a {@link Privilege}.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  void update(Owner o, Subject subj, Privilege p, boolean hasPriv) throws PrivilegeCacheException;

} // interface PrivilegeCache

