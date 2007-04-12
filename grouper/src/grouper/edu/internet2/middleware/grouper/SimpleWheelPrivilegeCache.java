/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  edu.internet2.middleware.subject.Subject;
import  java.util.Date;
import  org.apache.commons.collections.keyvalue.MultiKey;

/** 
 * A simple caching implementation of {@link PrivilegeCache} with better support
 * for wheel group-related privilege updates.
 * <p>
 * The behavior of this cache was changed between Grouper 1.1 and Grouper 1.2.
 * When an instance of this cache was initialized in 1.1, the wheel group would
 * be retrieved and then cached for use in all future operations.  If changes
 * were made to the wheel group outside of this session this cache could return
 * stale privilege results.  This cache was modified in Grouper 1.2 to impose a
 * maximum age on the cached wheel group.  If the cached wheel exceeds that
 * maximum age it will be replaced by a freshly fetched instance of the wheel group.  
 * The default maximum lifespan in Grouper 1.2 is 5 seconds (5000 milliseconds).
 * This value can be adjusted by setting the
 * <tt>edu.internet2.middleware.SimpleWheelPrivilegeCache.maxWheelAge</tt>
 * paramater in the <tt>conf/grouper.properties</tt> configuration file.
 * </p>
 * <pre class="eg">
 * # File: `grouper.properties`
 * # Refetch the cached wheel every 10 seconds instead of the default 5 seconds
 * edu.internet2.middleware.SimpleWheelPrivilegeCache.maxWheelAge = 10000
 * </pre>
 * @author  blair christensen.
 * @version $Id: SimpleWheelPrivilegeCache.java,v 1.13 2007-04-12 17:56:03 blair Exp $
 * @since   1.1.0     
 */
public class SimpleWheelPrivilegeCache extends SimplePrivilegeCache {

  // PROTECTED CLASS CONSTANTS //
  protected static final long DEFAULT_MAX_AGE = 5000; // protected for testing purposes


  // PRIVATE CLASS CONSTANTS //
  private static final String EXPIRING_WHEEL_GROUP  = "expiring cached wheel group due to age";
  private static final String FOUND_WHEEL_GROUP     = "found wheel group";
  private static final String NULL_VALUE            = "null value";
  private static final String REUSING_WHEEL_GROUP   = "reusing previously found wheel group";
  private static final String USING_DEFAULT_MAX_AGE = "using default max age for wheel group: ";


  // PRIVATE INSTANCE VARIABLES //
  private long  lastModified    = 0;
  private long  maxWheelAge     = internal_getMaxWheelAge();
  private GroupDTO _wheel       = null;
  private long  wheelFetchTime  = 0;


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
    MultiKey              k       = new MultiKey(o, p, subj);
    PrivilegeCacheElement result  = new NullPrivilegeCacheElement(o, subj, p);
    if ( this.internal_getCache().containsKey(k) ) {
      // The privilege is cached ...
      // ... But is the wheel group enabled?
      boolean useCached = true;
      if ( Boolean.valueOf( GrouperConfig.getProperty(GrouperConfig.GWU) ).booleanValue() ) {
        try {
          // Does the wheel group exist or has it been too long since we last fetched it?
          if ( (this._wheel == null) || this._isItTimeToUpdateWheel() ) {
            this._wheel = GrouperDAOFactory.getFactory().getGroup().findByName( GrouperConfig.getProperty(GrouperConfig.GWG) );
            this.wheelFetchTime = new Date().getTime();
            DebugLog.info(SimpleWheelPrivilegeCache.class, FOUND_WHEEL_GROUP);
          }
          else {
            DebugLog.info(SimpleWheelPrivilegeCache.class, REUSING_WHEEL_GROUP);
          }
          // If the wheel group has been modified since the last time the cache
          // was updated then do not return the cached result.  Instead, let the
          // privilege resolution code resolve the privilege.
          if ( this._wheel.getModifyTime() > this.lastModified ) {
            useCached = false;  
          }
          // Otherwise return nothing and let the privilege resolution code
          // figure it out.
        }
        catch (GroupNotFoundException eGNF) {
          useCached = false;
        }
      }
      if (useCached) {
        result = (PrivilegeCacheElement) this.internal_getCache().get(k);
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


  // PROTECTED CLASS METHODS //

  // `protected` for testing purposes
  // @since   1.2.0
  protected static long internal_getMaxWheelAge() {
    String val = GrouperConfig.getProperty(GrouperConfig.MAX_WHEEL_AGE);
    try {
      if (val == null) {
        throw new NumberFormatException(NULL_VALUE);
      }
      return Long.parseLong(val);
    }
    catch (NumberFormatException eNF) {
      DebugLog.info(SimpleWheelPrivilegeCache.class, USING_DEFAULT_MAX_AGE + eNF.getMessage());
      return DEFAULT_MAX_AGE;
    }
  } // protected static long internal_getMaxWheelAge()


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private boolean _isItTimeToUpdateWheel() {
    if ( (new Date().getTime() - this.wheelFetchTime) > this.maxWheelAge ) {
      DebugLog.info(SimpleWheelPrivilegeCache.class, EXPIRING_WHEEL_GROUP);
      return true;
    }
    return false;
  } // private boolean _isItTimeToUpdateWheel()

  // @since   1.1.0
  private void _setLastModified() {
    this.lastModified = new Date().getTime();
  } // private void _setLastModified()

} // public class SimpleWheelPrivilegeCache extends BasePrivilegeCache

