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
import  java.util.*;

/** 
 * A simple caching implementation of {@link PrivilegeCache} with better support
 * for wheel group-related privilege updates.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SimpleWheelPrivilegeCache.java,v 1.4 2006-10-23 14:18:46 blair Exp $
 * @since   1.1.0     
 */
public class SimpleWheelPrivilegeCache extends SimplePrivilegeCache {

  // PRIVATE INSTANCE VARIABLES //
  private long  lastModified  = 0;
  private Group wheel         = null;


  // PUBLIC INSTANCE METHODS //

  /**
   * Retrieve a potentially cached {@link Privilege}.
   * <p/>
   * @return  A {@link PrivilegeCacheElement} or {@link NullPrivilegeCacheElement}.
   * @since   1.1.0
   */
  public PrivilegeCacheElement get(Owner o, Subject subj, Privilege p) {
    // I'm not sure the logic is entirely correct within here but it does a
    // better job of tracking changes to the wheel group so...
    PrivilegeCacheElement result = new NullPrivilegeCacheElement(o, subj, p);
    if (this.getCache().containsKey(o, p, subj)) { 
      // The privilege is cached ...
      // ... But is the wheel group enabled?
      boolean useCached = true;
      if (Boolean.valueOf(GrouperConfig.getProperty(GrouperConfig.GWU))) {
        try {
          // Does the wheel group exist?
          if (this.wheel == null) {
            this.wheel = GroupFinder.findByName(
              o.getSession().getRootSession(), GrouperConfig.getProperty(GrouperConfig.GWG)
            );
          }
          else {
            this.wheel.setSession( o.getSession().getRootSession() );
          }
          // If the wheel group has been modified since the last time the cache
          // was updated then do not return the cached result.  Instead, let the
          // privilege resolution code resolve the privilege.
          if (this.wheel.getModifyTime().getTime() > lastModified) {
            useCached = false;  
          }
          // Otherwise return nothing and let the privilege resolution code
          // figure it out.
        }
        catch (GroupNotFoundException eGNF) {
          // Ignore
        }
      }
      if (useCached) {
        result = (PrivilegeCacheElement) this.getCache().get(o, p, subj);
      }
    }
    return result;
  } // public PrivilegeCacheElement get(o, subj, p)

  /**
   * Cache a {@link Privilege}.
   * </p>
   * @throws  PrivilegeCacheException
   * @since   1.1.0
   */
  public void put(Owner o, Subject subj, Privilege p, boolean hasPriv)
    throws  PrivilegeCacheException
  {
    super.put(o, subj, p, hasPriv); // Store the value without any cache flushing
    this._setLastModified();        // Update cache modification time
  } // public void put(o, subj, p, hasPriv)


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private void _setLastModified() {
    this.lastModified = new Date().getTime();
  } // private void _setLastModified()

} // public class SimpleWheelPrivilegeCache extends BasePrivilegeCache

