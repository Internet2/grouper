/*
 * @author mchyzer
 * $Id: AttributeAssignDAO.java,v 1.10 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;

/**
 * attribute assign data access methods
 */
public interface AttributeAssignDAO extends GrouperDAO {
  
  /**
   * find records which are disabled which shouldnt be, and enabled which shouldnt be
   * @return the attribute assignments
   */
  public Set<AttributeAssign> findAllEnabledDisabledMismatch();

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
   * find attribute assigns by ids, as root (no security).  this is for one type assignment.
   * @param attributeTypeAssignId type assign id
   * @return attributes
   */
  public Set<AttributeAssignValueContainer> findByAssignTypeId(
      String attributeTypeAssignId);

  /**
   * find attribute assigns by ids, as root (no security).  order by attribute type def name, so they are in order
   * @param attributeTypeDefNameId attribute def name of the type on the owner
   * @param queryOptions
   * @return attributes grouped by the type assignment
   */
  public Map<AttributeAssign, Set<AttributeAssignValueContainer>> findByAttributeTypeDefNameId(
      String attributeTypeDefNameId, QueryOptions queryOptions);
  
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
   * @param ownerAttributeAssignId
   * @param queryOptions
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerAttributeAssignId(String ownerAttributeAssignId, QueryOptions queryOptions);

  /**
   * @param ownerAttributeDefId
   * @return the attribute assigns or empty if not there
   */
  public Set<AttributeAssign> findByOwnerAttributeDefId(String ownerAttributeDefId);

  /**
   * @param attributeDefNameId
   * @return the attribute defs or empty if not there
   */
  public Set<AttributeDef> findAttributeDefsByAttributeDefNameId(String attributeDefNameId);

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
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param groupIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findGroupAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or stem ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param stemIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findStemAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or member ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param memberIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findMemberAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or attribute def assign to ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param attributeDefAssignToIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findAttributeDefAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> attributeDefAssignToIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or membership ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param membershipIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findMembershipAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * securely search for assignments.  need to pass in either the assign ids, def ids, def name ids, or membership ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param groupIdsAndMemberIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @param includeAssignmentsOnAssignments if assignments on assignments should also be included
   * @return the assignments
   */
  public Set<AttributeAssign> findAnyMembershipAttributeAssignments(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<MultiKey> groupIdsAndMemberIds, Collection<String> actions, 
      Boolean enabled, boolean includeAssignmentsOnAssignments);

  /**
   * find assignments on assignments.  Note, it is assumed the current user can read the assignments passed in (and other underlying objects),
   * so only the attributeDefs of the assignments on assignments are checked for security
   * @param attributeAssigns to find assignments on these assignments
   * @param attributeAssignType of the assignments we are looking for
   * @param enabled null for all, true for enabled only, false for disabled only
   * @return the assignments
   */
  public Set<AttributeAssign> findAssignmentsOnAssignments(Collection<AttributeAssign> attributeAssigns, 
      AttributeAssignType attributeAssignType, Boolean enabled);

  /**
   * securely search for attribute def names.  need to pass in either the assign ids, def ids, def name ids, or group ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param groupIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findGroupAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> groupIds, Collection<String> actions, Boolean enabled);

  /**
   * securely search for attribute def names.  need to pass in either the assign ids, def ids, def name ids, or member ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param memberIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findMemberAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> memberIds, Collection<String> actions, 
      Boolean enabled);

  /**
   * securely search for attribute def names.  need to pass in either the assign ids, def ids, def name ids, or membership ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param membershipIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findMembershipAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> membershipIds, Collection<String> actions, 
      Boolean enabled);

  /**
   * securely search for attribute def names.  need to pass in either the assign ids, def ids, def name ids, or stem ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param stemIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findStemAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> stemIds, Collection<String> actions, 
      Boolean enabled);

  /**
   * securely search for attribute def names.  need to pass in either the assign ids, def ids, def name ids, or membership ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param groupIdsAndMemberIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findAnyMembershipAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<MultiKey> groupIdsAndMemberIds, Collection<String> actions, 
      Boolean enabled);

  /**
   * securely search for attributeDefNames.  need to pass in either the assign ids, def ids, def name ids, or attribute def assign to ids
   * cannot have more than 100 bind variables
   * @param attributeAssignIds
   * @param attributeDefIds optional
   * @param attributeDefNameIds mutually exclusive with attributeDefIds
   * @param attributeDefAssignToIds optional
   * @param actions (null means all actions)
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Set<AttributeDefName> findAttributeDefAttributeDefNames(
      Collection<String> attributeAssignIds,
      Collection<String> attributeDefIds, 
      Collection<String> attributeDefNameIds,
      Collection<String> attributeDefAssignToIds, Collection<String> actions, 
      Boolean enabled);

  /**
   * find by attribute name and value
   * @param attributeDefNameId
   * @param value
   * @param queryOptions
   * @return the attribute assigns that match
   */
  public Set<AttributeAssign> findByAttributeDefNameAndValueString(String attributeDefNameId, String value, QueryOptions queryOptions);
  

  
}
