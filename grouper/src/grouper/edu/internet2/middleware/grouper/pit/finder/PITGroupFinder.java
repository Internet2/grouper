package edu.internet2.middleware.grouper.pit.finder;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * Find point in time groups.
 * 
 * @author shilen
 * $Id$
 */
public class PITGroupFinder {

  /**
   * Find point in time group by id.
   * If the group currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param id
   * @param exceptionIfNotFound
   * @return pit group
   */
  public static PITGroup findById(String id, boolean exceptionIfNotFound) {

    GrouperSession session = GrouperSession.staticGrouperSession();
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(id);
    
    if (pitGroup == null) {
      if (exceptionIfNotFound) {
        throw new GroupNotFoundException("Point in time group with id " + id + " does not exist.");
      }
      
      return null;
    }
    
    if (!pitGroup.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
      if (exceptionIfNotFound) {
        throw new GroupNotFoundException("Point in time group with id " + id + " does not exist.");
      }

      return null;
    }
    
    if (pitGroup.isActive()) {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(id, true);
      
      if (!PrivilegeHelper.canView(session.internal_getRootSession(), group, session.getSubject())) {
        if (exceptionIfNotFound) {
          throw new GroupNotFoundException("Point in time group with id " + id + " does not exist.");
        }

        return null;
      }
    }
    
    return pitGroup;
  }
  
  /**
   * Find point in time groups by name.
   * If the group currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param exceptionIfNotFound
   * @return set of pit group
   */
  public static Set<PITGroup> findByName(String name, boolean exceptionIfNotFound) {

    Set<PITGroup> pitGroupsSecure = new LinkedHashSet<PITGroup>();
    
    GrouperSession session = GrouperSession.staticGrouperSession();
    Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findByName(name);
    
    for (PITGroup pitGroup : pitGroups) {
      if (!pitGroup.isActive() && !PrivilegeHelper.isWheelOrRoot(session.getSubject())) {
        continue;
      }
      
      if (pitGroup.isActive()) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(pitGroup.getId(), true);
        
        if (!PrivilegeHelper.canView(session.internal_getRootSession(), group, session.getSubject())) {
          continue;
        }
      }
      
      pitGroupsSecure.add(pitGroup);
    }
    
    if (pitGroupsSecure.size() == 0) {
      if (exceptionIfNotFound) {
        throw new GroupNotFoundException("Point in time group with name " + name + " does not exist.");
      }
    }
        
    return pitGroupsSecure;
  }
  
  /**
   * Find the most recent point in time group by name.
   * If the group currently exists, you must have view access to it.  If it has been deleted, you must be wheel or root.
   * @param name
   * @param exceptionIfNotFound
   * @return pit group
   */
  public static PITGroup findMostRecentByName(String name, boolean exceptionIfNotFound) {
    Set<PITGroup> pitGroups = findByName(name, exceptionIfNotFound);
    
    Long lastEndTime = 0L;
    PITGroup lastPITGroup = null;
    
    for (PITGroup pitGroup : pitGroups) {
      if (pitGroup.isActive()) {
        return pitGroup;
      }
      
      if (pitGroup.getEndTimeDb() > lastEndTime) {
        lastEndTime = pitGroup.getEndTimeDb();
        lastPITGroup = pitGroup;
      }
    }
    
    return lastPITGroup;
  }
}
