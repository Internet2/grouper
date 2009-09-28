/*
 * @author mchyzer
 * $Id: AttributeAssignDAO.java,v 1.7 2009-09-28 16:05:54 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;

/**
 * attribute assign data access methods
 */
public interface AttributeAssignDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute assign object 
   * @param attributeAssign 
   */
  public void saveOrUpdate(AttributeAssign attributeAssign);
  
  /** 
   * delete an attribute assign object 
   * @param attributeAssign 
   */
  public void delete(AttributeAssign attributeAssign);
  
  /**
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute assign or null if not there
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound);

  /**
   * @param groupId
   * @param attributeDefNameId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefNameId(String groupId, String attributeDefNameId);

  /**
   * @param groupId
   * @param attributeDefId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByGroupIdAndAttributeDefId(String groupId, String attributeDefId);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param groupId
   * @param attributeDefId
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdAndAttributeDefId(String groupId, String attributeDefId);

  /**
   * @param memberId
   * @param attributeDefNameId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefNameId(String memberId, String attributeDefNameId);

  /**
   * @param memberId
   * @param attributeDefId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByMemberIdAndAttributeDefId(String memberId, String attributeDefId);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param memberId
   * @param attributeDefId
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByMemberIdAndAttributeDefId(String memberId, String attributeDefId);

  /**
   * @param stemId
   * @param attributeDefNameId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefNameId(String stemId, String attributeDefNameId);

  /**
   * @param stemId
   * @param attributeDefId
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByStemIdAndAttributeDefId(String stemId, String attributeDefId);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param stemId
   * @param attributeDefId
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByStemIdAndAttributeDefId(String stemId, String attributeDefId);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param attributeDefIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttributeDefIdAndAttributeDefId(String attributeDefIdToAssignTo, 
      String attributeDefIdToAssign);

  /**
   * @param attributeDefIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefId(String attributeDefIdToAssignTo, String attributeDefIdToAssign);

  /**
   * @param attributeDefIdToAssignTo
   * @param attributeDefNameIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByAttributeDefIdAndAttributeDefNameId(String attributeDefIdToAssignTo, String attributeDefNameIdToAssign);

 
}
