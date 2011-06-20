/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;

/**
 * 
 */
public interface PITAttributeAssignDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssign
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign);
  
  /**
   * insert or update
   * @param pitAttributeAssigns
   */
  public void saveOrUpdate(Set<PITAttributeAssign> pitAttributeAssigns);
  
  /**
   * delete
   * @param pitAttributeAssign
   */
  public void delete(PITAttributeAssign pitAttributeAssign);
  
  /**
   * @param id
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findById(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerAttributeAssignId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerAttributeAssignId(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerMembershipId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerMembershipId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerMembershipId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerGroupId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerStemId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerAttributeDefId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerAttributeAssignId(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param attributeAssigns
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return pit assignments
   */
  public Set<PITAttributeAssign> findAssignmentsOnAssignments(Collection<PITAttributeAssign> attributeAssigns, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByAttributeDefNameId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByAttributeAssignActionId(String id);
  
  /**
   * @return active attribute assigns that are missing in point in time
   */
  public Set<AttributeAssign> findMissingActivePITAttributeAssigns();
  
  /**
   * @return active point in time attribute assigns that should be inactive
   */
  public Set<PITAttributeAssign> findMissingInactivePITAttributeAssigns();
}
