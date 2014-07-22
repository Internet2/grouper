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
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Find point in time attribute def names.
 * 
 * @author shilen
 * $Id$
 */
public class PITAttributeDefNameFinder {

  /**
   * Find point in time attribute def names by id.
   * If the attribute def name currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param exceptionIfNotFound
   * @return pit attribute def name
   */
  public static PITAttributeDefName findById(String id, boolean exceptionIfNotFound) {
    
    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(id, false);
    
    Set<PITAttributeDefName> pitAttributeDefNamesSecure = new LinkedHashSet<PITAttributeDefName>();
    if (pitAttributeDefName != null) {
      pitAttributeDefNamesSecure = securityFilter(GrouperUtil.toSet(pitAttributeDefName));
    }
    
    if (pitAttributeDefNamesSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist.");
      }
      
      return null;
    }
        
    return pitAttributeDefNamesSecure.iterator().next();
  }
  
  /**
   * Find point in time attribute def names by id.
   * If the attribute def name currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param exceptionIfNotFound
   * @return set of pit attribute def name
   */
  public static Set<PITAttributeDefName> findBySourceId(String id, boolean exceptionIfNotFound) {
    
    Set<PITAttributeDefName> pitAttributeDefNames = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceId(id, false);
    
    Set<PITAttributeDefName> pitAttributeDefNamesSecure = securityFilter(pitAttributeDefNames);
    
    if (pitAttributeDefNamesSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist.");
      }
    }
        
    return pitAttributeDefNamesSecure;
  }
  
  private static Set<PITAttributeDefName> securityFilter(Set<PITAttributeDefName> pitAttributeDefNames) {
    Set<PITAttributeDefName> pitAttributeDefNamesSecure = new LinkedHashSet<PITAttributeDefName>();
    
    GrouperSession session = GrouperSession.staticGrouperSession();
    
    for (PITAttributeDefName pitAttributeDefName : pitAttributeDefNames) {
      if (!pitAttributeDefName.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        continue;
      }
      
      if (pitAttributeDefName.isActive()) {
        if (GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(pitAttributeDefName.getSourceId(), false) == null) {
          continue;
        }
      }
      
      pitAttributeDefNamesSecure.add(pitAttributeDefName);
    }
    
    return pitAttributeDefNamesSecure;
  }
  
  
  /**
   * Find point in time attribute def names by name.
   * If the attribute def name currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param exceptionIfNotFound
   * @param orderByStartTime
   * @return set of pit attribute def name
   */
  public static Set<PITAttributeDefName> findByName(String name, boolean exceptionIfNotFound, boolean orderByStartTime) {
    
    Set<PITAttributeDefName> pitAttributeDefNames = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByName(name, true);
    
    Set<PITAttributeDefName> pitAttributeDefNamesSecure = securityFilter(pitAttributeDefNames);
    
    if (pitAttributeDefNamesSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with name " + name + " does not exist.");
      }
    }
        
    return pitAttributeDefNamesSecure;
  }

  /**
   * Find point in time attribute def names by name and date ranges
   * If the attribute def name currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @param exceptionIfNotFound
   * @param orderByStartTime
   * @return set of pit attribute def name
   */
  public static Set<PITAttributeDefName> findByName(String name, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, 
      boolean exceptionIfNotFound, boolean orderByStartTime) {
    
    Set<PITAttributeDefName> pitAttributeDefNames = findByName(name, exceptionIfNotFound, orderByStartTime);
    Set<PITAttributeDefName> pitAttributeDefNamesInRange = new LinkedHashSet<PITAttributeDefName>();

    for (PITAttributeDefName pitAttributeDefName : pitAttributeDefNames) {
      if (pointInTimeFrom != null) {
        if (!pitAttributeDefName.isActive() && pitAttributeDefName.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (pitAttributeDefName.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      pitAttributeDefNamesInRange.add(pitAttributeDefName);
    }
    
    if (pitAttributeDefNamesInRange.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with name " + name + " does not exist in the given date range.");
      }
    }
    
    return pitAttributeDefNamesInRange;
  }
  
  /**
   * Find point in time attribute def names by id and date ranges
   * If the attribute def name currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @param exceptionIfNotFound
   * @return set of pit attribute def name
   */
  public static Set<PITAttributeDefName> findBySourceId(String id, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, 
      boolean exceptionIfNotFound) {
    
    Set<PITAttributeDefName> pitAttributeDefNames = findBySourceId(id, exceptionIfNotFound);
    Set<PITAttributeDefName> pitAttributeDefNamesInRange = new LinkedHashSet<PITAttributeDefName>();

    for (PITAttributeDefName pitAttributeDefName : pitAttributeDefNames) {
      if (pointInTimeFrom != null) {
        if (!pitAttributeDefName.isActive() && pitAttributeDefName.getEndTime().before(pointInTimeFrom)) {
          continue;
        }
      }
      
      if (pointInTimeTo != null) {
        if (pitAttributeDefName.getStartTime().after(pointInTimeTo)) {
          continue;
        }
      }
      
      pitAttributeDefNamesInRange.add(pitAttributeDefName);
    }
    
    if (pitAttributeDefNamesInRange.size() == 0) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist in the given date range.");
      }
    }
    
    return pitAttributeDefNamesInRange;
  }
}
