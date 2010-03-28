/*
 * @author mchyzer
 * $Id: AttributeAssignDAO.java,v 1.10 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
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

  /**
   * @param ownerAttributeAssignId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerAttributeAssignId(String ownerAttributeAssignId);

  /**
   * @param ownerAttributeDefId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerAttributeDefId(String ownerAttributeDefId);

  /**
   * @param ownerGroupId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerGroupId(String ownerGroupId);

  /**
   * @param ownerStemId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerStemId(String ownerStemId);

  /**
   * @param ownerMemberId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerMemberId(String ownerMemberId);

  /**
   * @param ownerMembershipId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerMembershipId(String ownerMembershipId);



  /**
   * find attribute def names (distinct) by attribute def id
   * @param membershipIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, 
      String attributeDefIdToAssign);

  /**
   * @param membershipIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefId(
      String membershipIdToAssignTo, String attributeDefIdToAssign);

  /**
   * @param membershipIdToAssignTo
   * @param attributeDefNameIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByMembershipIdAndAttributeDefNameId(
      String membershipIdToAssignTo, String attributeDefNameIdToAssign);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param attrAssignIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, 
      String attributeDefIdToAssign);

  /**
   * @param attrAssignIdToAssignTo
   * @param attributeDefIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefId(
      String attrAssignIdToAssignTo, String attributeDefIdToAssign);

  /**
   * @param attributeDefNameId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByAttributeDefNameId(
      String attributeDefNameId);

  /**
   * @param attrAssignIdToAssignTo
   * @param attributeDefNameIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByAttrAssignIdAndAttributeDefNameId(
      String attrAssignIdToAssignTo, String attributeDefNameIdToAssign);

  /**
   * find attribute def names (distinct) by attribute def id
   * @param groupId
   * @param memberId
   * @param attributeDefIdToAssign
   * @return the attribute defs
   */
  public Set<AttributeDefName> findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(
      String groupId, String memberId, 
      String attributeDefIdToAssign);

  /**
   * @param groupId
   * @param memberId
   * @param attributeDefIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefId(
      String groupId, String memberId, String attributeDefIdToAssign);

  /**
   * @param groupId
   * @param memberId
   * @param attributeDefNameIdToAssign
   * @return the attribute assigns or null if not there
   */
  public Set<AttributeAssign> findByGroupIdMemberIdAndAttributeDefNameId(
      String groupId, String memberId, String attributeDefNameIdToAssign);

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeAssign
   */
  public void saveUpdateProperties(AttributeAssign attributeAssign);

  /**
   * @param id if find by id, that is it
   * @param idsToIgnore dont return anything in this list, already used or will be used
   * @param attributeDefNameId which attribute is assigned
   * @param attributeAssignActionId is the action for this assignment
   * @param ownerAttributeAssignId owner must match 
   * @param ownerAttributeDefId owner must match
   * @param ownerGroupId owner must match
   * @param ownerMemberId owner must match
   * @param ownerMembershipId owner must match
   * @param ownerStemId owner must match
   * @param exceptionIfNull 
   * @param disabledTimeDb if there are multiple without id match, and this matches, that is good
   * @param enabledTimeDb if there are multiple without id match, this is good
   * @param notes if there are multiple without id match, this is good
   * @return the attribute assign or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  AttributeAssign findByUuidOrKey(Collection<String> idsToIgnore,
      String id, String attributeDefNameId, String attributeAssignActionId, String ownerAttributeAssignId, String ownerAttributeDefId, String ownerGroupId,
      String ownerMemberId, String ownerMembershipId, String ownerStemId, boolean exceptionIfNull, 
      Long disabledTimeDb, Long enabledTimeDb, String notes) throws GrouperDAOException;

  /**
   * @param actionId 
   * @return the assignments
   */
  Set<AttributeAssign> findByActionId(String actionId);

  /**
   * securely search for assignments.  need to pass in either the def ids, def names, or group ids
   * cannot have more than 100 bind variables
   * @param attributeDefIds optional
   * @param groupIds
   * @param enabled
   * @return the assignments
   */
  public Set<AttributeAssign> findGroupAttributeAssignments(Collection<String> attributeDefIds, 
      Collection<String> groupIds, Boolean enabled);
  

}
