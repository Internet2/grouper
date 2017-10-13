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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * finder methods for attribute assign
 */
public class AttributeAssignValueFinder {

  
  /**
   * result for finding values of attributes
   */
  @SuppressWarnings("javadoc")
  public static class AttributeAssignValueFinderResult {
    private Set<AttributeAssign> allAttributeAssigns = null;
    
    private Set<AttributeAssign> attributeAssigns = new LinkedHashSet<AttributeAssign>();
    private Set<AttributeAssign> attributeAssignsOnAssigns = new LinkedHashSet<AttributeAssign>();
    private Set<String> attributeAssignsOnAssignsIds = new LinkedHashSet<String>();
    private Map<String, AttributeAssign> mapGroupIdToAttributeAssign = new LinkedHashMap<String, AttributeAssign>();
    
    /**
     * get the map from group id to the attribute assign
     */
    public Map<String, AttributeAssign> getMapGroupIdToAttributeAssign() {
      return this.mapGroupIdToAttributeAssign;
    }
    
    private Map<String, Set<String>> mapAttributeAssignIdToAssignAssignIds = new LinkedHashMap<String, Set<String>>();
    private Map<String, AttributeAssign> mapAttributeAssignIdToAttributeAssign = new LinkedHashMap<String, AttributeAssign>();
    private Set<AttributeAssignValue> attributeAssignValues = null;
    private Map<String, AttributeDefName> mapAttributeDefNameIdToAttributeDefName = new LinkedHashMap<String, AttributeDefName>();
    private Map<String, AttributeDef> mapAttributeDefIdToAttributeDef = new LinkedHashMap<String, AttributeDef>();
    private Map<String, AttributeAssignValue> mapAttributeAssignOnAssignIdToAttributeAssignValue = new LinkedHashMap<String, AttributeAssignValue>();
    private Map<MultiKey, AttributeAssign> mapGroupIdAndNameOfAttributeDefNameToAttributeAssignOnAssign = new LinkedHashMap<MultiKey, AttributeAssign>();
    
    /**
     * get the map of nameOfattribuetDefName to value
     * @param groupId
     * @return the map
     */
    public Map<String, String> retrieveAttributeDefNamesAndValueStrings(String groupId) {
      
      Map<String, String> result = new LinkedHashMap<String, String>();
      
      if (GrouperUtil.length(this.attributeAssignValues) > 0) {
        
        AttributeAssign attributeAssign = this.mapGroupIdToAttributeAssign.get(groupId);
        
        if (attributeAssign != null) {
          
          for (String attributeAssignAssignId : GrouperUtil.nonNull(this.mapAttributeAssignIdToAssignAssignIds.get(attributeAssign.getId()))) {
                        
            AttributeAssign attributeAssignOnAssign = this.mapAttributeAssignIdToAttributeAssign.get(attributeAssignAssignId);
            
            AttributeDefName attributeDefName = this.mapAttributeDefNameIdToAttributeDefName.get(attributeAssignOnAssign.getAttributeDefNameId());
            
            if (result.containsKey(attributeDefName.getName())) {
              throw new RuntimeException("AttributeDefName '" + attributeDefName.getName() + "' already exists!");
            }
            
            AttributeAssignValue attributeAssignValue = this.mapAttributeAssignOnAssignIdToAttributeAssignValue.get(attributeAssignOnAssign.getId());
            
            result.put(attributeDefName.getName(), attributeAssignValue.valueString());
            
          }
          
        }
        
      }
      
      return result;
    }
    
    /**
     * get the attribute assign on assign based on group id and name of the attributeDefName
     * @param groupId
     * @param nameOfAttributeDefName
     * @return the assignment
     */
    public AttributeAssign retrieveAttributeAssignOnAssign(String groupId, String nameOfAttributeDefName) {
      MultiKey multiKey = new MultiKey(groupId, nameOfAttributeDefName);
      return this.mapGroupIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.get(multiKey);
    }
    
  }

  /**
   * attributeDefNameUseSecurity use security around attribute def?  default is true
   */
  private boolean attributeDefNameUseSecurity = true;
  
  /**
   * use security around attribute def?  default is true
   * @param theAttributeDefNameUseSecurity
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignAttributeDefNameUseSecurity(boolean theAttributeDefNameUseSecurity) {
    this.attributeDefNameUseSecurity = theAttributeDefNameUseSecurity;
    return this;
  }
  
  /**
   * look for values of groups where it is an assignment on assignment
   */
  private Collection<String> ownerGroupIdsOfAssignAssign;

  /**
   * look for values of groups where it is an assignment on assignment
   * @param ownerGroupIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerGroupIdsOfAssignAssign(Collection<String> ownerGroupIdsOfAssignAssign1) {
    this.ownerGroupIdsOfAssignAssign = ownerGroupIdsOfAssignAssign1;
    return this;
  }
  
  /**
   * look for values of groups where it is an assignment on assignment
   * @param ownerGroupIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerGroupIdsOfAssignAssign(String ownerGroupIdsOfAssignAssign1) {
    if (this.ownerGroupIdsOfAssignAssign == null) {
      this.ownerGroupIdsOfAssignAssign = new LinkedHashSet<String>();
    }
    this.ownerGroupIdsOfAssignAssign.add(ownerGroupIdsOfAssignAssign1);
    return this;
  }
  
  /**
   * look for values of groups where it is an assignment on assignment
   * @param ownerGroupIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerGroupsOfAssignAssign(Collection<Group> ownerGroupIdsOfAssignAssign1) {
    
    if (ownerGroupIdsOfAssignAssign1 == null) {
      this.ownerGroupIdsOfAssignAssign = null;
    } else {
    
      this.ownerGroupIdsOfAssignAssign = new LinkedHashSet<String>();
      for (Group group : ownerGroupIdsOfAssignAssign1) {
        this.ownerGroupIdsOfAssignAssign.add(group.getId());
      }
    }
    return this;
  }
  
  /**
   * look for values of groups where it is an assignment on assignment
   * @param ownerGroupIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerGroupOfAssignAssign(Group ownerGroupIdsOfAssignAssign1) {
    return this.addOwnerGroupIdsOfAssignAssign(ownerGroupIdsOfAssignAssign1.getId());
  }
  
  /**
   * if we are looking for assignments on assignments, this is the base attribute def name
   */
  private Collection<String> attributeDefNameIdsOfBaseAssignment;
  
  /**
   * if we are looking for assignments on assignments, this is the base attribute def name
   * @param attributeDefNameIdOfBaseAssignment1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addAttributeDefNameIdsOfBaseAssignment(String attributeDefNameIdOfBaseAssignment1) {
    if (this.attributeDefNameIdsOfBaseAssignment == null) {
      this.attributeDefNameIdsOfBaseAssignment = new LinkedHashSet<String>();
    }
    this.attributeDefNameIdsOfBaseAssignment.add(attributeDefNameIdOfBaseAssignment1);
    return this;
  }

  /**
   * if we are looking for assignments on assignments, this is the base attribute def name
   * @param attributeDefNameIdsOfBaseAssignment1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignAttributeDefNameIdsOfBaseAssignment(Collection<String> attributeDefNameIdsOfBaseAssignment1) {
    this.attributeDefNameIdsOfBaseAssignment = attributeDefNameIdsOfBaseAssignment1;
    return this;
  }
  
  /**
   * lookup attribute assign values by attribute assign value
   */
  private Collection<String> attributeAssignIds;
  
  /**
   * add attribute assign id
   * @param attributeAssignId
   * @return this for chaining
   */
  public AttributeAssignValueFinder addAttributeAssignId(String attributeAssignId) {
    if (this.attributeAssignIds == null) {
      this.attributeAssignIds = new LinkedHashSet<String>();
    }
    this.attributeAssignIds.add(attributeAssignId);
    return this;
  }
  
  /**
   * add owner group id
   * @param attributeAssignIds1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignAttributeAssignIds(Collection<String> attributeAssignIds1) {
    this.attributeAssignIds = attributeAssignIds1;
    return this;
  }

  /**
   * attribute def names ids
   */
  private Collection<String> attributeDefNameIds;
  
  /**
   * attribute def name id to find
   * @param attributeDefNameId
   * @return this for chaining
   */
  public AttributeAssignValueFinder addAttributeDefNameId(String attributeDefNameId) {
    if (this.attributeDefNameIds == null) {
      this.attributeDefNameIds = new LinkedHashSet<String>();
    }
    this.attributeDefNameIds.add(attributeDefNameId);
    return this;
  }
  
  /**
   * attribute def name ids to find
   * @param theAttributeDefNameIds
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignAttributeDefNameIds(Collection<String> theAttributeDefNameIds) {
    this.attributeDefNameIds = theAttributeDefNameIds;
    return this;
  }

  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public AttributeAssignValueFinderResult findAttributeAssignValuesResult() {
    
    AttributeAssignValueFinderResult result = new AttributeAssignValueFinderResult();
    
    
    if (GrouperUtil.length(this.ownerGroupIdsOfAssignAssign) > 0) {
      
      GrouperSessionHandler grouperSessionHandler = new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          Set<AttributeAssign> theAttributeAssigns = new AttributeAssignFinder()
            .assignOwnerGroupIds(AttributeAssignValueFinder.this.ownerGroupIdsOfAssignAssign).assignIncludeAssignmentsOnAssignments(true)
            .assignAttributeDefNameIds(AttributeAssignValueFinder.this.attributeDefNameIds).findAttributeAssigns();
          return theAttributeAssigns;
        }
      };
      if (this.attributeDefNameUseSecurity) {
        result.allAttributeAssigns = (Set<AttributeAssign>)grouperSessionHandler.callback(GrouperSession.staticGrouperSession());
      } else {
        result.allAttributeAssigns = (Set<AttributeAssign>)GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
      }
    }
    
    //separate into assigns and assigns on assigns
    
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(result.allAttributeAssigns)) {
      
      result.mapAttributeAssignIdToAttributeAssign.put(attributeAssign.getId(), attributeAssign);
      
      if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
        result.attributeAssignsOnAssigns.add(attributeAssign);
        result.attributeAssignsOnAssignsIds.add(attributeAssign.getId());
      } else {
        result.attributeAssigns.add(attributeAssign);
        result.mapGroupIdToAttributeAssign.put(attributeAssign.getOwnerGroupId(), attributeAssign);
      }
      
    }
    
    // link up the attribute assign with the assign ids
    for (AttributeAssign attributeAssignOnAssign : GrouperUtil.nonNull(result.attributeAssignsOnAssigns)) {
      String attributeAssignId = attributeAssignOnAssign.getOwnerAttributeAssignId();
      Set<String> attributeAssignAssignIds = result.mapAttributeAssignIdToAssignAssignIds.get(attributeAssignId);
      if (attributeAssignAssignIds == null) {
        attributeAssignAssignIds = new LinkedHashSet<String>();
        result.mapAttributeAssignIdToAssignAssignIds.put(attributeAssignId, attributeAssignAssignIds);
      }
      attributeAssignAssignIds.add(attributeAssignOnAssign.getId());
    }
    
    //get the values
    result.attributeAssignValues = new AttributeAssignValueFinder()
      .assignAttributeAssignIds(result.attributeAssignsOnAssignsIds).findAttributeAssignValues();

    //get all attribute def names
    final AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder();
    Set<String> attributeDefNameIds = new HashSet<String>();
    
    for (AttributeAssign attributeAssign : result.allAttributeAssigns) {

      String attributeDefNameId = attributeAssign.getAttributeDefNameId();
      if (!attributeDefNameIds.contains(attributeDefNameId)) {
        attributeDefNameFinder.addIdOfAttributeDefName(attributeDefNameId);
        attributeDefNameIds.add(attributeDefNameId);
      }
      
    }
    
    Set<AttributeDefName> attributeDefNames = null;
        
    GrouperSessionHandler grouperSessionHandler = new GrouperSessionHandler() {
      
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        return attributeDefNameFinder.findAttributeNames();
      }
    };
    if (this.attributeDefNameUseSecurity) {
      attributeDefNames = (Set<AttributeDefName>)grouperSessionHandler.callback(GrouperSession.staticGrouperSession());
    } else {
      attributeDefNames = (Set<AttributeDefName>)GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
    }

    final Set<String> attributeDefIds = new HashSet<String>();
    for (AttributeDefName attributeDefName : attributeDefNames) {
      result.mapAttributeDefNameIdToAttributeDefName.put(attributeDefName.getId(), attributeDefName);
      attributeDefIds.add(attributeDefName.getAttributeDefId());
    }

    grouperSessionHandler = new GrouperSessionHandler() {
      
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        return new AttributeDefFinder().assignAttributeDefIds(attributeDefIds).findAttributes();
      }
    };
    
    Set<AttributeDef> attributeDefs = null;

    if (this.attributeDefNameUseSecurity) {
      attributeDefs = (Set<AttributeDef>)grouperSessionHandler.callback(GrouperSession.staticGrouperSession());
    } else {
      attributeDefs = (Set<AttributeDef>)GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
    }

    for (AttributeDef attributeDef : attributeDefs) {
      result.mapAttributeDefIdToAttributeDef.put(attributeDef.getId(), attributeDef);
    }
    
    //setup the attribute defs and attribute def names in assignments
    for (AttributeAssign attributeAssign : result.allAttributeAssigns) {
      String attributeDefNameId = attributeAssign.getAttributeDefNameId();
      AttributeDefName attributeDefName = result.mapAttributeDefNameIdToAttributeDefName.get(attributeDefNameId);
      
      String attributeDefId = attributeDefName.getAttributeDefId();
      
      AttributeDef attributeDef = result.mapAttributeDefIdToAttributeDef.get(attributeDefId);
      
      attributeDefName.internalSetAttributeDef(attributeDef);
      attributeAssign.internalSetAttributeDef(attributeDef);
      attributeAssign.internalSetAttributeDefName(attributeDefName);
      
    }
    
    for (AttributeAssignValue attributeAssignValue : result.attributeAssignValues) {
      
      String attributeAssignId = attributeAssignValue.getAttributeAssignId();
      
      if (result.mapAttributeAssignOnAssignIdToAttributeAssignValue.get(attributeAssignId) != null) {
        throw new RuntimeException("AttributeAssignId: " + attributeAssignId + " should only have one value but has multiple!");
      }

      result.mapAttributeAssignOnAssignIdToAttributeAssignValue.put(attributeAssignId, attributeAssignValue);

      AttributeAssign attributeAssign = result.mapAttributeAssignIdToAttributeAssign.get(attributeAssignId);
      attributeAssignValue.internalSetAttributeAssign(attributeAssign);

    }
    
    for (String ownerGroupId : this.ownerGroupIdsOfAssignAssign) {
      
      AttributeAssign attributeAssign = result.mapGroupIdToAttributeAssign.get(ownerGroupId);
      
      Set<String> attributeAssignAssignIds = result.mapAttributeAssignIdToAssignAssignIds.get(attributeAssign.getId());
      
      for (String attributeAssignAssignId : attributeAssignAssignIds) {
        AttributeAssign attributeAssignOnAssign = result.mapAttributeAssignIdToAttributeAssign.get(attributeAssignAssignId);
            
        AttributeDefName attributeDefName = result.mapAttributeDefNameIdToAttributeDefName.get(attributeAssignOnAssign.getAttributeDefNameId());

        MultiKey multiKey = new MultiKey(ownerGroupId, attributeDefName.getName());
        
        if (result.mapGroupIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.get(multiKey) != null) {
          throw new RuntimeException("Why does groupId and attributeDefName already exist? " + ownerGroupId + ", " + attributeDefName.getName());
        }
        
        result.mapGroupIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.put(multiKey, attributeAssignOnAssign);
        
      }
      
    }
    
    return result;
    
  }

  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public Set<AttributeAssignValue> findAttributeAssignValues() {
  
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
  
      // if passed in empty set of group ids and no names, then no groups found
      if (this.attributeAssignIds != null && this.attributeAssignIds.size() == 0) {
        return new HashSet<AttributeAssignValue>();
      }
      
    }
    
    if (this.attributeAssignIds != null) {
      
      return GrouperDAOFactory.getFactory().getAttributeAssignValue()
          .findByAttributeAssignIds(this.attributeAssignIds);

    }
    
    throw new RuntimeException("Bad query");
  }  

}
