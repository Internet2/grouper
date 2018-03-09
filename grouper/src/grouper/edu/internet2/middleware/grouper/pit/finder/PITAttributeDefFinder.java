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
package edu.internet2.middleware.grouper.pit.finder;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Find point in time attribute defs.
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeDefFinder {
  
  /**
   * Find point in time attribute defs by id.
   * If the attribute def currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param exceptionIfNotFound
   * @return pit attribute def
   */
  public static PITAttributeDef findById(String id, boolean exceptionIfNotFound) {

    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(id, false);
    
    Set<PITAttributeDef> pitAttributeDefsSecure = new LinkedHashSet<PITAttributeDef>();
    if (pitAttributeDef != null) {
      pitAttributeDefsSecure = securityFilter(GrouperUtil.toSet(pitAttributeDef));
    }
    
    if (pitAttributeDefsSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist.");
      }
      
      return null;
    }
        
    return pitAttributeDefsSecure.iterator().next();
  }
  
  /**
   * Find point in time attribute defs by id.
   * If the attribute def currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param exceptionIfNotFound
   * @return set of pit attribute def
   */
  public static Set<PITAttributeDef> findBySourceId(String id, boolean exceptionIfNotFound) {

    Set<PITAttributeDef> pitAttributeDefs = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceId(id, false);
    
    Set<PITAttributeDef> pitAttributeDefsSecure = securityFilter(pitAttributeDefs);
    
    if (pitAttributeDefsSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist.");
      }
    }
        
    return pitAttributeDefsSecure;
  }
  
  private static Set<PITAttributeDef> securityFilter(Set<PITAttributeDef> pitAttributeDefs) {
    Set<PITAttributeDef> pitAttributeDefsSecure = new LinkedHashSet<PITAttributeDef>();
    
    GrouperSession session = GrouperSession.staticGrouperSession();
    
    for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
      if (!pitAttributeDef.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        continue;
      }
      
      if (pitAttributeDef.isActive()) {
        if (AttributeDefFinder.findById(pitAttributeDef.getSourceId(), false) == null) {
          continue;
        }
      }
      
      pitAttributeDefsSecure.add(pitAttributeDef);
    }
    
    return pitAttributeDefsSecure;
  }
  
  
  /**
   * Find point in time attribute defs by name.
   * If the attribute def currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param exceptionIfNotFound
   * @param orderByStartTime
   * @return set of pit attribute def
   */
  public static Set<PITAttributeDef> findByName(String name, boolean exceptionIfNotFound, boolean orderByStartTime) {

    Set<PITAttributeDef> pitAttributeDefs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByName(name, true);
    
    Set<PITAttributeDef> pitAttributeDefsSecure = securityFilter(pitAttributeDefs);
    
    if (pitAttributeDefsSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with name " + name + " does not exist.");
      }
    }
        
    return pitAttributeDefsSecure;
  }

  /**
   * Find point in time attribute defs by name and date ranges
   * If the attribute def currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @param exceptionIfNotFound
   * @param orderByStartTime
   * @return set of pit attribute def
   */
  public static Set<PITAttributeDef> findByName(String name, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, 
      boolean exceptionIfNotFound, boolean orderByStartTime) {
    
    Set<PITAttributeDef> pitAttributeDefs = findByName(name, exceptionIfNotFound, orderByStartTime);
    Set<PITAttributeDef> pitAttributeDefsInRange = new LinkedHashSet<PITAttributeDef>();

    for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
      if (pointInTimeFrom != null) {
        if (!pitAttributeDef.isActive() && pitAttributeDef.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (pitAttributeDef.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      pitAttributeDefsInRange.add(pitAttributeDef);
    }
    
    if (pitAttributeDefsInRange.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with name " + name + " does not exist in the given date range.");
      }
    }
    
    return pitAttributeDefsInRange;
  }
  
  /**
   * Find point in time attribute defs by id and date ranges
   * If the attribute def currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @param exceptionIfNotFound
   * @return set of pit attribute def
   */
  public static Set<PITAttributeDef> findBySourceId(String id, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, 
      boolean exceptionIfNotFound) {
    
    Set<PITAttributeDef> pitAttributeDefs = findBySourceId(id, exceptionIfNotFound);
    Set<PITAttributeDef> pitAttributeDefsInRange = new LinkedHashSet<PITAttributeDef>();

    for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
      if (pointInTimeFrom != null) {
        if (!pitAttributeDef.isActive() && pitAttributeDef.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (pitAttributeDef.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      pitAttributeDefsInRange.add(pitAttributeDef);
    }
    
    if (pitAttributeDefsInRange.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist in the given date range.");
      }
    }
    
    return pitAttributeDefsInRange;
  }
}
