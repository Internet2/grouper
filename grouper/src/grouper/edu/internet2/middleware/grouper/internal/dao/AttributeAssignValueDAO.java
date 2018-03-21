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
/*
 * @author mchyzer
 * $Id: AttributeAssignValueDAO.java,v 1.3 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeAssignValueNotFoundException;

/**
 * attribute assign value data access methods
 */
public interface AttributeAssignValueDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute assign value object 
   * @param attributeAssignValue 
   */
  public void saveOrUpdate(AttributeAssignValue attributeAssignValue);
  
  /** 
   * delete an attribute assign value object 
   * @param attributeAssignValue 
   */
  public void delete(AttributeAssignValue attributeAssignValue);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute assign value or null if not there
   * @throws AttributeAssignValueNotFoundException 
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound)
    throws AttributeAssignValueNotFoundException;

  /**
   * find values of assignment
   * @param attributeAssignId
   * @return the attribute assign values or empty if not there
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId);

  /**
   * find values of assignment based on ids, batched (i.e. pass in as meny ids as you want)
   * 
   * this assumes you are allowed to read them... doesnt check security
   * 
   * @param attributeAssignIds
   * @return the attribute assign values or empty if not there
   */
  public Set<AttributeAssignValue> findByAttributeAssignIds(Collection<String> attributeAssignIds);

  /**
   * find values of assignment
   * @param attributeAssignId
   * @param queryOptions 
   * @return the attribute assign values or empty if not there
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId, QueryOptions queryOptions);

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeAssignValue
   */
  public void saveUpdateProperties(AttributeAssignValue attributeAssignValue);

  /**
   * @param id if find by id, that is it
   * @param idsToIgnore dont return anything in this list, already used or will be used
   * @param attributeAssignId to get values from
   * @param exceptionIfNull 
   * @param valueInteger try to match this if possible
   * @param valueMemberId 
   * @param valueString 
   * @return the attribute assign value or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  AttributeAssignValue findByUuidOrKey(Collection<String> idsToIgnore,
      String id, String attributeAssignId, boolean exceptionIfNull, 
      Long valueInteger, String valueMemberId, String valueString) throws GrouperDAOException;

  /**
   * @param id if find by id, that is it
   * @param idsToIgnore dont return anything in this list, already used or will be used
   * @param attributeAssignId to get values from
   * @param exceptionIfNull 
   * @param valueInteger try to match this if possible
   * @param valueMemberId 
   * @param valueString 
   * @param queryOptions
   * @return the attribute assign value or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  AttributeAssignValue findByUuidOrKey(Collection<String> idsToIgnore,
      String id, String attributeAssignId, boolean exceptionIfNull, 
      Long valueInteger, String valueMemberId, String valueString, QueryOptions queryOptions) throws GrouperDAOException;

  /**
   * find values by value string
   * @param value
   * @return the values
   */
  public Set<AttributeAssignValue> findByValueString(String value);

  /**
   * find values on this assignment and values on assignments on this assignment
   * @param attributeAssignIds 
   * @param attributeAssignType 
   * @param attributeDefType 
   * @param enabled 
   * @return the set of values
   */
  public Set<AttributeAssignValue> findValuesOnAssignments(Collection<String> attributeAssignIds, 
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType, Boolean enabled);
  
  /**
   * securely search for assignments.  need to pass in member ids
   * cannot have more than 100 bind variables
   * @param memberIds optional
   * @param enabled (null means all, true means enabled, false means disabled)
   * @return the assignments
   */
  public Map<AttributeAssign, Set<AttributeAssignValue>> findMemberAttributeAssignmentValues(
      Collection<String> memberIds,
      Boolean enabled);

  /**
   * Returns legacy attributes (assigned to a given group) either migrated or created in the new attribute framework.
   * The keys of the map are the legacy attribute field names.
   * @param groupId
   * @return the values
   */
  public Map<String, AttributeAssignValue> findLegacyAttributesByGroupId(String groupId);

  /**
   * Returns legacy attributes (assigned to a given group) either migrated or created in the new attribute framework.
   * The keys of the map are the legacy attribute field names.
   * @param groupId
   * @return the map of maps of values by group id
   */
  public Map<String, Map<String, AttributeAssignValue>> findLegacyAttributesByGroupIds(Collection<String> groupId);

}
