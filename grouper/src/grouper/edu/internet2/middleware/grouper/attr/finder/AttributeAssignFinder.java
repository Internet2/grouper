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
/**
 * @author mchyzer
 * $Id: AttributeDefFinder.java,v 1.2 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * finder methods for attribute assign
 */
public class AttributeAssignFinder {

  /**
   * if filtering by owner, this is the filter
   */
  private String filter;
  
  /**
   * filter text for groups, folders, attributes of owner
   * @param theFilter
   * @return this for chaining
   */
  public AttributeAssignFinder assignFilter(String theFilter) {
    this.filter = theFilter;
    return this;
  }
  
  /**
   * if filter should be split by whitespace, by default yes
   */
  private Boolean splitFilter = null;
  
  /**
   * if filter should be split by whitespace, by default yes
   * @param theSplitFilter
   * @return this for chaining
   */
  public AttributeAssignFinder assignSplitFilter(boolean theSplitFilter) {
    this.splitFilter = theSplitFilter;
    return this;
  }
  
  /**
   * if should retrieve values
   */
  private boolean retrieveValues;
  
  /**
   * if should retrieve values
   * @param theRetrieveValues
   * @return this for chaining
   */
  public AttributeAssignFinder assignRetrieveValues(boolean theRetrieveValues) {
    this.retrieveValues = theRetrieveValues;
    return this;
  }
  
  /**
   * queryOptions for calls
   */
  private QueryOptions queryOptions;
  
  /**
   * query options paging and sorting
   * @param theQueryOptions
   * @return this for chaining
   */
  public AttributeAssignFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }
  
  /**
   * id of attribute def name that there is an assignment on assignment of with a value or values (optional)
   */
  private String idOfAttributeDefNameOnAssignment0;
  
  /**
   * id of attribute def name that there is an assignment on assignment of with a value or values (optional)
   * @param theIdOfAttributeDefNameOnAssignment0
   * @return this for chaining
   */
  public AttributeAssignFinder assignIdOfAttributeDefNameOnAssignment0(String theIdOfAttributeDefNameOnAssignment0) {
    this.idOfAttributeDefNameOnAssignment0 = theIdOfAttributeDefNameOnAssignment0;
    return this;
  }
  
  /**
   * values that the attribute def name on assignment of assignment has
   */
  private Set<Object> attributeValuesOnAssignment0;

  /**
   * values that the attribute def name on assignment of assignment has
   * @param theAttributeValuesOnAssignment0
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeValuesOnAssignment0(Set<Object> theAttributeValuesOnAssignment0) {
    this.attributeValuesOnAssignment0 = theAttributeValuesOnAssignment0;
    return this;
  }

  /**
   * second id of attribute def name that there is an assignment on assignment of with a value
   */
  private String idOfAttributeDefNameOnAssignment1;

  /**
   * id of second attribute def name that there is an assignment on assignment of with a value or values (optional)
   * @param theIdOfAttributeDefNameOnAssignment1
   * @return this for chaining
   */
  public AttributeAssignFinder assignIdOfAttributeDefNameOnAssignment1(String theIdOfAttributeDefNameOnAssignment1) {
    this.idOfAttributeDefNameOnAssignment1 = theIdOfAttributeDefNameOnAssignment1;
    return this;
  }

  /**
   * second values that the attribute def name on assignment of assignment has
   */
  private Set<Object> attributeValuesOnAssignment1;

  /**
   * values that the second attribute def name on assignment of assignment has
   * @param theAttributeValuesOnAssignment1
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeValuesOnAssignment1(Set<Object> theAttributeValuesOnAssignment1) {
    this.attributeValuesOnAssignment1 = theAttributeValuesOnAssignment1;
    return this;
  }

  /**
   * if check attribute read on owner if applicable
   */
  private Boolean checkAttributeReadOnOwner;
  
  /**
   * check read on owner
   * @param theCheckAttributeReadOnOwner
   * @return this for chaining
   */
  public AttributeAssignFinder assignCheckAttributeReadOnOwner(boolean theCheckAttributeReadOnOwner) {
    this.checkAttributeReadOnOwner = theCheckAttributeReadOnOwner;
    return this;
  }
  
  /**
   * use security around attribute def?  default is true
   */
  private Boolean attributeCheckReadOnAttributeDef = null;
  
  /**
   * use security around attribute def?  default is true
   * @param theAttributeDefNameUseSecurity
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeCheckReadOnAttributeDef(boolean theAttributeDefNameUseSecurity) {
    this.attributeCheckReadOnAttributeDef = theAttributeDefNameUseSecurity;
    return this;
  }
  
  /**
   * query these attribute assign types (if querying by attribute)
   */
  private AttributeAssignType attributeAssignType;
  
  /**
   * assign the attribute assign type for querying by attribute
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeAssignType(AttributeAssignType theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
  /**
   * attribute def names ids
   */
  private Collection<String> attributeDefNameIds;
  
  /**
   * attribute def ids
   */
  private Collection<String> attributeDefIds;
  
  /**
   * attribute def name id to find
   * @param attributeDefNameId
   * @return this for chaining
   */
  public AttributeAssignFinder addAttributeDefNameId(String attributeDefNameId) {
    if (this.attributeDefNameIds == null) {
      this.attributeDefNameIds = new LinkedHashSet<String>();
    }
    this.attributeDefNameIds.add(attributeDefNameId);
    return this;
  }
  
  /**
   * attribute def id to find
   * @param attributeDefId
   * @return this for chaining
   */
  public AttributeAssignFinder addAttributeDefId(String attributeDefId) {
    if (this.attributeDefIds == null) {
      this.attributeDefIds = new LinkedHashSet<String>();
    }
    this.attributeDefIds.add(attributeDefId);
    return this;
  }
  
  /**
   * attribute def name ids to find
   * @param theAttributeDefNameIds
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeDefNameIds(Collection<String> theAttributeDefNameIds) {
    this.attributeDefNameIds = theAttributeDefNameIds;
    return this;
  }
  
  /**
   * attribute def ids to find
   * @param theAttributeDefIds
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeDefIds(Collection<String> theAttributeDefIds) {
    this.attributeDefIds = theAttributeDefIds;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerGroupIds;
  
  /**
   * add owner group id
   * @param ownerGroupId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerGroupId(String ownerGroupId) {
    if (this.ownerGroupIds == null) {
      this.ownerGroupIds = new LinkedHashSet<String>();
    }
    this.ownerGroupIds.add(ownerGroupId);
    return this;
  }
  
  /**
   * add owner group id
   * @param ownerGroupIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerGroupIds(Collection<String> ownerGroupIds1) {
    this.ownerGroupIds = ownerGroupIds1;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerAttributeAssignIds;
  
  /**
   * add owner assign id
   * @param ownerAttributeAssignId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerAttributeAssignId(String ownerAttributeAssignId) {
    if (this.ownerAttributeAssignIds == null) {
      this.ownerAttributeAssignIds = new LinkedHashSet<String>();
    }
    this.ownerAttributeAssignIds.add(ownerAttributeAssignId);
    return this;
  }
  
  /**
   * add owner assign id
   * @param ownerAttributeAssignIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerAttributeAssignIds(Collection<String> ownerAttributeAssignIds1) {
    this.ownerAttributeAssignIds = ownerAttributeAssignIds1;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerStemIds;
  
  /**
   * add owner stem id
   * @param ownerStemId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerStemId(String ownerStemId) {
    if (this.ownerStemIds == null) {
      this.ownerStemIds = new LinkedHashSet<String>();
    }
    this.ownerStemIds.add(ownerStemId);
    return this;
  }
  
  /**
   * add owner stem id
   * @param ownerStemIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerStemIds(Collection<String> ownerStemIds1) {
    this.ownerStemIds = ownerStemIds1;
    return this;
  }
  
  /**
   * if assignments on assignments should also be included
   */
  private boolean includeAssignmentsOnAssignments = false;
  /**
   * 
   */
  private Collection<String> ownerAttributeDefIds;
  
  /**
   * if assignments on assignments should also be included
   * @param theIncludeAssignAssignmentsOnAssignments
   * @return this for chaining
   */
  public AttributeAssignFinder assignIncludeAssignmentsOnAssignments(boolean theIncludeAssignAssignmentsOnAssignments) {
    this.includeAssignmentsOnAssignments = theIncludeAssignAssignmentsOnAssignments;
    return this;
  }

  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public AttributeAssignFinderResults findAttributeAssignFinderResults() {

    if (GrouperUtil.length(this.ownerGroupIds) > 0 || GrouperUtil.length(this.ownerStemIds) > 0
      || GrouperUtil.length(this.ownerAttributeDefIds) > 0 
      || GrouperUtil.length(this.ownerAttributeAssignIds) > 0) {
      throw new RuntimeException("Invalid Query");
    }
    
    Set<Object[]> results = null;
    AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinderResults();
    
    if (this.attributeAssignType != null) {
      results = findAttributeAssignFinderResultsHelper(this.attributeDefIds, this.attributeDefNameIds, 
          this.attributeAssignType);
    } else {

      // lets get all the attribute defs for each attributeDefId and attributeDefNameId
      // note, this is not a secure query, that will come later
      final Map<String, AttributeDef> attributeDefIdToAttributeDefMap = new HashMap<String, AttributeDef>();
      final Map<String, AttributeDef> attributeDefNameIdToAttributeDefMap = new HashMap<String, AttributeDef>();

      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          Set<AttributeDefName> attributeDefNamesFound = null;
          Set<String> attributeDefIdsToQuery = new HashSet<String>();
          if (AttributeAssignFinder.this.attributeDefIds != null) {
            attributeDefIdsToQuery.addAll(AttributeAssignFinder.this.attributeDefIds);
          }
          if (GrouperUtil.length(AttributeAssignFinder.this.attributeDefNameIds) > 0) {
            attributeDefNamesFound = new AttributeDefNameFinder().assignIdsOfAttributeDefNames(
                AttributeAssignFinder.this.attributeDefNameIds).findAttributeNames();
            for (AttributeDefName attributeDefName : attributeDefNamesFound) {
              attributeDefIdsToQuery.add(attributeDefName.getAttributeDefId());
            }
          }
          if (GrouperUtil.length(attributeDefIdsToQuery) > 0) {
            Set<AttributeDef> attributeDefsFound = new AttributeDefFinder().assignAttributeDefIds(attributeDefIdsToQuery).findAttributes();
            for (AttributeDef attributeDef : attributeDefsFound) {
              attributeDefIdToAttributeDefMap.put(attributeDef.getId(), attributeDef);
            }
          }
          for (AttributeDefName attributeDefName : attributeDefNamesFound) {
            AttributeDef attributeDefFound = attributeDefIdToAttributeDefMap.get(attributeDefName.getAttributeDefId());
            attributeDefNameIdToAttributeDefMap.put(attributeDefName.getId(), attributeDefFound);
          }
          
          return null;
        }
        
      });
      
      // we know all the attribute defs, sort them by type
      Map<AttributeAssignType, Set<String>> attributeAssignTypeToAttributeDefIds = new TreeMap<AttributeAssignType, Set<String>>();
      Map<AttributeAssignType, Set<String>> attributeAssignTypeToAttributeDefNameIds = new TreeMap<AttributeAssignType, Set<String>>();
      
      for (String attributeDefId : GrouperUtil.nonNull(this.attributeDefIds)) {
        AttributeDef attributeDef = attributeDefIdToAttributeDefMap.get(attributeDefId);
        for (AttributeAssignType attributeAssignType : attributeDef.getAttributeAssignTypes()) {
          Set<String> theseAttributeDefIds = attributeAssignTypeToAttributeDefIds.get(attributeAssignType);
          if (theseAttributeDefIds == null) {
            theseAttributeDefIds = new HashSet<String>();
            attributeAssignTypeToAttributeDefIds.put(attributeAssignType, theseAttributeDefIds);
          }
          theseAttributeDefIds.add(attributeDefId);
        }
      }
      
      for (String attributeDefNameId : GrouperUtil.nonNull(this.attributeDefNameIds)) {
        AttributeDef attributeDef = attributeDefNameIdToAttributeDefMap.get(attributeDefNameId);
        for (AttributeAssignType attributeAssignType : attributeDef.getAttributeAssignTypes()) {
          Set<String> theseAttributeDefNameIds = attributeAssignTypeToAttributeDefNameIds.get(attributeAssignType);
          if (theseAttributeDefNameIds == null) {
            theseAttributeDefNameIds = new HashSet<String>();
            attributeAssignTypeToAttributeDefNameIds.put(attributeAssignType, theseAttributeDefNameIds);
          }
          theseAttributeDefNameIds.add(attributeDefNameId);
        }
      }
      
      results = new LinkedHashSet();
      AttributeAssignType[] attributeAssignTypes = AttributeAssignType.values();
      Arrays.sort(attributeAssignTypes);
      
      // do the specific query for each type
      for (AttributeAssignType attributeAssignType : attributeAssignTypes) {
        Set<String> attributeDefIdsToQuery = attributeAssignTypeToAttributeDefIds.get(attributeAssignType);
        Set<String> attributeDefIdNamesToQuery = attributeAssignTypeToAttributeDefNameIds.get(attributeAssignType);
        
        if (GrouperUtil.length(attributeDefIdsToQuery) > 0 || GrouperUtil.length(attributeDefIdNamesToQuery) > 0) {
          Set<Object[]> tempResults = findAttributeAssignFinderResultsHelper(attributeDefIdsToQuery, attributeDefIdNamesToQuery, 
              attributeAssignType);
          results.addAll(tempResults);
        }
        
      }
    }

    attributeAssignFinderResults.setResultObjects(results);
    
    return attributeAssignFinderResults;

  }

  /**
   * get the attribute assign finder results for a certain type
   * @param theAttributeDefIds
   * @param theAttributeDefNameIds
   * @param theAttributeAssignType
   * @return the results
   */
  private Set<Object[]> findAttributeAssignFinderResultsHelper(Collection<String> theAttributeDefIds, Collection<String> theAttributeDefNameIds, 
      AttributeAssignType theAttributeAssignType) {
    if (theAttributeAssignType == AttributeAssignType.stem) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsByAttribute(theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, 
          this.queryOptions, this.retrieveValues, this.includeAssignmentsOnAssignments, this.filter, this.splitFilter);

    } else if (theAttributeAssignType == AttributeAssignType.attr_def) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignmentsByAttribute(theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, 
          this.queryOptions, this.retrieveValues, this.includeAssignmentsOnAssignments, this.filter, this.splitFilter);

    } else if (theAttributeAssignType == AttributeAssignType.imm_mem) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findImmediateMembershipAttributeAssignmentsByAttribute(theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, 
          this.queryOptions, this.retrieveValues, this.includeAssignmentsOnAssignments, this.filter, this.splitFilter);

    } else if (theAttributeAssignType == AttributeAssignType.group) {
        
      return GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, 
          this.queryOptions, this.retrieveValues, this.includeAssignmentsOnAssignments, this.filter, this.splitFilter);
        
    } else if (theAttributeAssignType == AttributeAssignType.member) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.attributeCheckReadOnAttributeDef, 
          this.queryOptions, this.retrieveValues, this.includeAssignmentsOnAssignments, this.filter, this.splitFilter);
        
    } else if (theAttributeAssignType == AttributeAssignType.group_asgn) {
        
      return GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsOnAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
            null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, this.queryOptions, this.retrieveValues, this.filter, this.splitFilter);
              
    } else if (theAttributeAssignType == AttributeAssignType.imm_mem_asgn) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findImmediateMembershipAttributeAssignmentsOnAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
            null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, this.queryOptions, this.retrieveValues, this.filter, this.splitFilter);
              
    } else if (theAttributeAssignType == AttributeAssignType.stem_asgn) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsOnAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, this.queryOptions, this.retrieveValues, this.filter, this.splitFilter);
            
    } else if (theAttributeAssignType == AttributeAssignType.attr_def_asgn) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.attributeCheckReadOnAttributeDef, this.queryOptions, this.retrieveValues, this.filter, this.splitFilter);
            
    } else if (theAttributeAssignType == AttributeAssignType.mem_asgn) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignmentsOnAssignmentsByAttribute(
          theAttributeDefIds, theAttributeDefNameIds, 
          null, true, this.attributeCheckReadOnAttributeDef, this.queryOptions, this.retrieveValues, this.filter, this.splitFilter);
            
    }
    return new HashSet<Object[]>();
    //throw new RuntimeException("Not support type: " + theAttributeAssignType);
  }
  
  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public Set<AttributeAssign> findAttributeAssigns() {
  
    if (!StringUtils.isBlank(this.filter) ) {
      throw new RuntimeException("filter not supported in this call");
      
    }
    if (this.retrieveValues) {
      throw new RuntimeException("retrieveValues not supported in this call");
    }

    if (this.queryOptions != null) {
      throw new RuntimeException("queryOptions not supported in this call");
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
  
      // if passed in empty set of group ids and no names, then no groups found
      if (this.ownerGroupIds != null && this.ownerGroupIds.size() == 0) {
        return new HashSet<AttributeAssign>();
      }
      
    }

    int ownerCount = 0;
    
    if (GrouperUtil.length(this.ownerGroupIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerStemIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerAttributeDefIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerAttributeAssignIds) > 0) {
      ownerCount++;
    }
    if (this.attributeAssignType != null) {
      ownerCount++;
    }
    if (ownerCount > 1) {
      throw new RuntimeException("Can only pass one type of owner: groups, stems, attributeDefs, attributeAssigns, attributeAssignType, but has " + ownerCount + " types");
    }

    if (this.ownerGroupIds != null) {
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findGroupAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerGroupIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef, 
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerStemIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findStemAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerStemIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef,
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerAttributeDefIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findAttributeDefAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerAttributeDefIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef,
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerAttributeAssignIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, false);
      if (this.attributeCheckReadOnAttributeDef) {
        throw new RuntimeException("Invalid query: attributeCheckReadOnAttributeDef");
      }

      if (this.includeAssignmentsOnAssignments) {
        throw new RuntimeException("Invalid query: includeAssignmentsOnAssignments");
      }
      
      if (GrouperUtil.length(this.attributeDefNameIds) > 0) {
        throw new RuntimeException("Invalid query: attributeDefNameIds");
      }
      
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findAssignmentsFromAssignmentsByIds(this.attributeDefNameIds, null, null, true);

    }
    
    if (this.attributeAssignType != null) {
      if (GrouperUtil.length(this.attributeDefNameIds) == 0 ) {
        throw new RuntimeException("You need to pass in attributeDefNameIds if you are querying by attribute");
      }
      throw new RuntimeException("This query is not yet supported");
    }
    
    throw new RuntimeException("Bad query");
  }

  /**
   * add owner AttributeDef id
   * @param ownerAttributeDefId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerAttributeDefId(String ownerAttributeDefId) {
    if (this.ownerAttributeDefIds == null) {
      this.ownerAttributeDefIds = new LinkedHashSet<String>();
    }
    this.ownerAttributeDefIds.add(ownerAttributeDefId);
    return this;
  }

  /**
   * add owner AttributeDef id
   * @param ownerAttributeDefIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerAttributeDefIds(Collection<String> ownerAttributeDefIds1) {
    this.ownerAttributeDefIds = ownerAttributeDefIds1;
    return this;
  }

  /**
   * find an attributeAssign by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeAssign
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute assign or null
   * @throws AttributeAssignNotFoundException
   */
  public static AttributeAssign findById(String id, boolean exceptionIfNull) {
    
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(id, exceptionIfNull);
    
    //at this point no exception should be thrown
    if (attributeAssign == null) {
      return null;
    }
    
    //now we need to check security
    if (PrivilegeHelper.canViewAttributeAssign(GrouperSession.staticGrouperSession(), attributeAssign, true)) {
      return attributeAssign;
    }
    if (exceptionIfNull) {
      throw new AttributeAssignNotFoundException("Not allowed to view attribute assign by id: " + id);
    }
    return null;
  }  

}
