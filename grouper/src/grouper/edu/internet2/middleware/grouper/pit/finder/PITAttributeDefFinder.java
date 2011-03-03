package edu.internet2.middleware.grouper.pit.finder;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

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

    GrouperSession session = GrouperSession.staticGrouperSession();
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(id);
    
    if (pitAttributeDef == null) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist.");
      }
      
      return null;
    }
    
    if (!pitAttributeDef.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
      if (exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist.");
      }

      return null;
    }
    
    if (pitAttributeDef.isActive()) {
      
      if (GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(id, false) == null) {
        if (exceptionIfNotFound) {
          throw new AttributeDefNotFoundException("Point in time attribute def with id " + id + " does not exist.");
        }

        return null;
      }
    }
    
    return pitAttributeDef;
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

    Set<PITAttributeDef> pitAttributeDefsSecure = new LinkedHashSet<PITAttributeDef>();
    
    GrouperSession session = GrouperSession.staticGrouperSession();
    Set<PITAttributeDef> pitAttributeDefs = GrouperDAOFactory.getFactory().getPITAttributeDef().findByName(name, true);
    
    for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
      if (!pitAttributeDef.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        continue;
      }
      
      if (pitAttributeDef.isActive()) {
        if (GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(pitAttributeDef.getId(), false) == null) {
          continue;
        }
      }
      
      pitAttributeDefsSecure.add(pitAttributeDef);
    }
    
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
}