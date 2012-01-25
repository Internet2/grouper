/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;

/**
 * 
 */
public interface PITAttributeDefNameDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefName
   */
  public void saveOrUpdate(PITAttributeDefName pitAttributeDefName);

  /**
   * insert or update
   * @param pitAttributeDefNames
   */
  public void saveOrUpdate(Set<PITAttributeDefName> pitAttributeDefNames);
  
  /**
   * delete
   * @param pitAttributeDefName
   */
  public void delete(PITAttributeDefName pitAttributeDefName);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param name
   * @param orderByStartTime
   * @return set of pit attribute def names
   */
  public Set<PITAttributeDefName> findByName(String name, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findByAttributeDefId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findByStemId(String id);
  
  /**
   * @return active attribute def names that are missing in point in time
   */
  public Set<AttributeDefName> findMissingActivePITAttributeDefNames();
  
  /**
   * @return active point in time attribute def names that should be inactive
   */
  public Set<PITAttributeDefName> findMissingInactivePITAttributeDefNames();
}
