package edu.internet2.middleware.grouper.pit.finder;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

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

    GrouperSession session = GrouperSession.staticGrouperSession();
    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(id);
    
    if (pitAttributeDefName == null) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist.");
      }
      
      return null;
    }
    
    if (!pitAttributeDefName.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist.");
      }

      return null;
    }
    
    if (pitAttributeDefName.isActive()) {
      
      if (GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(id, false) == null) {
        if (exceptionIfNotFound) {
          throw new AttributeDefNameNotFoundException("Point in time attribute def name with id " + id + " does not exist.");
        }

        return null;
      }
    }
    
    return pitAttributeDefName;
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

    Set<PITAttributeDefName> pitAttributeDefNamesSecure = new LinkedHashSet<PITAttributeDefName>();
    
    GrouperSession session = GrouperSession.staticGrouperSession();
    Set<PITAttributeDefName> pitAttributeDefNames = GrouperDAOFactory.getFactory().getPITAttributeDefName().findByName(name, true);
    
    for (PITAttributeDefName pitAttributeDefName : pitAttributeDefNames) {
      if (!pitAttributeDefName.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        continue;
      }
      
      if (pitAttributeDefName.isActive()) {
        if (GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(pitAttributeDefName.getId(), false) == null) {
          continue;
        }
      }
      
      pitAttributeDefNamesSecure.add(pitAttributeDefName);
    }
    
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
}