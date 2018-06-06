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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
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
    private Map<String, AttributeAssign> mapOwnerIdToAttributeAssign = new LinkedHashMap<String, AttributeAssign>();
    private Map<String, Set<AttributeAssign>> mapOwnerIdToAttributeAssigns = new LinkedHashMap<String, Set<AttributeAssign>>();
    
    /**
     * get the map from group id to the attribute assign
     */
    public Map<String, AttributeAssign> getMapOwnerIdToAttributeAssign() {
      return this.mapOwnerIdToAttributeAssign;
    }
    
    /**
     * get the map from group id to the attribute assign
     */
    public Map<String, Set<AttributeAssign>> getMapOwnerIdToAttributeAssigns() {
      return this.mapOwnerIdToAttributeAssigns;
    }
    
    /**
     * map of attributeAssignId to attributeAssign
     */
    public Map<String, AttributeAssign> getMapAttributeAssignIdToAttributeAssign() {
      return this.mapAttributeAssignIdToAttributeAssign;
    }
    
    private Map<String, Set<String>> mapAttributeAssignIdToAssignAssignIds = new LinkedHashMap<String, Set<String>>();
    private Map<String, AttributeAssign> mapAttributeAssignIdToAttributeAssign = new LinkedHashMap<String, AttributeAssign>();
    private Set<AttributeAssignValue> attributeAssignValues = null;
    private Map<String, AttributeDefName> mapAttributeDefNameIdToAttributeDefName = new LinkedHashMap<String, AttributeDefName>();
    private Map<String, AttributeDef> mapAttributeDefIdToAttributeDef = new LinkedHashMap<String, AttributeDef>();
    private Map<String, AttributeAssignValue> mapAttributeAssignOnAssignIdToAttributeAssignValue = new LinkedHashMap<String, AttributeAssignValue>();
    private Map<MultiKey, AttributeAssign> mapOwnerIdAndNameOfAttributeDefNameToAttributeAssignOnAssign = new LinkedHashMap<MultiKey, AttributeAssign>();
    
    /**
     * get the map of nameOfattribuetDefName to value
     * @param ownerId
     * @return the map
     */
    public Map<String, String> retrieveAttributeDefNamesAndValueStrings(String ownerId) {
      
      Map<String, String> result = new LinkedHashMap<String, String>();
      
      if (GrouperUtil.length(this.attributeAssignValues) > 0) {
        
        AttributeAssign attributeAssign = this.mapOwnerIdToAttributeAssign.get(ownerId);
        
        if (attributeAssign != null) {
          
          for (String attributeAssignAssignId : GrouperUtil.nonNull(this.mapAttributeAssignIdToAssignAssignIds.get(attributeAssign.getId()))) {
                        
            AttributeAssign attributeAssignOnAssign = this.mapAttributeAssignIdToAttributeAssign.get(attributeAssignAssignId);
            
            AttributeDefName attributeDefName = this.mapAttributeDefNameIdToAttributeDefName.get(attributeAssignOnAssign.getAttributeDefNameId());
            
            if (result.containsKey(attributeDefName.getName())) {
              throw new RuntimeException("AttributeDefName '" + attributeDefName.getName() + "' already exists!");
            }
            
            AttributeAssignValue attributeAssignValue = this.mapAttributeAssignOnAssignIdToAttributeAssignValue.get(attributeAssignOnAssign.getId());
            
            if (attributeAssignValue != null) {
              result.put(attributeDefName.getName(), attributeAssignValue.valueString());
            }
          }
          
        }
        
      }
      
      return result;
    }
    
    /**
     * get the map of attribute assign id (base attrbute) to map of names and values (assign of assign)
     * @param ownerId
     * @return the map
     */
    public Map<String, Map<String, String>> retrieveAssignIdsToAttributeDefNamesAndValueStrings(String ownerId) {
      
      Map<String, Map<String, String>> result = new LinkedHashMap<String, Map<String, String>>();
      
      if (GrouperUtil.length(this.attributeAssignValues) > 0) {
        
        Set<AttributeAssign> attributeAssigns = this.mapOwnerIdToAttributeAssigns.get(ownerId);

        if (GrouperUtil.length(attributeAssigns) > 0) {
          for (AttributeAssign attributeAssign : attributeAssigns) {
            
            Map<String, String> attributeDefNameToValue = new LinkedHashMap<String, String>();
            result.put(attributeAssign.getId(), attributeDefNameToValue);
            
            for (String attributeAssignAssignId : GrouperUtil.nonNull(this.mapAttributeAssignIdToAssignAssignIds.get(attributeAssign.getId()))) {

              AttributeAssign attributeAssignOnAssign = this.mapAttributeAssignIdToAttributeAssign.get(attributeAssignAssignId);
              
              AttributeDefName attributeDefName = this.mapAttributeDefNameIdToAttributeDefName.get(attributeAssignOnAssign.getAttributeDefNameId());
              
              if (result.containsKey(attributeDefName.getName())) {
                throw new RuntimeException("AttributeDefName '" + attributeDefName.getName() + "' already exists!");
              }
              
              AttributeAssignValue attributeAssignValue = this.mapAttributeAssignOnAssignIdToAttributeAssignValue.get(attributeAssignOnAssign.getId());
              
              if (attributeAssignValue != null) {
                attributeDefNameToValue.put(attributeDefName.getName(), attributeAssignValue.valueString());
              }
            }
          }          
        }
        
      }
      
      return result;
    }
    
    /**
     * get the attribute assign on assign based on group id and name of the attributeDefName
     * @param ownerId
     * @param nameOfAttributeDefName
     * @return the assignment
     */
    public AttributeAssign retrieveAttributeAssignOnAssign(String ownerId, String nameOfAttributeDefName) {
      MultiKey multiKey = new MultiKey(ownerId, nameOfAttributeDefName);
      return this.mapOwnerIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.get(multiKey);
    }
    
  }

  /**
   * attributeDefNameUseSecurity use security around attribute def?  default is true
   */
  private boolean attributeCheckReadOnAttributeDef = true;
  
  /**
   * use security around attribute def?  default is true
   * @param theAttributeDefNameUseSecurity
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignAttributeCheckReadOnAttributeDef(boolean theAttributeDefNameUseSecurity) {
    this.attributeCheckReadOnAttributeDef = theAttributeDefNameUseSecurity;
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
   * look for values of stems where it is an assignment on assignment
   */
  private Collection<String> ownerStemIdsOfAssignAssign;

  /**
   * look for values of stems where it is an assignment on assignment
   * @param ownerStemIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerStemIdsOfAssignAssign(Collection<String> ownerStemIdsOfAssignAssign1) {
    this.ownerStemIdsOfAssignAssign = ownerStemIdsOfAssignAssign1;
    return this;
  }
  
  /**
   * look for values of stems where it is an assignment on assignment
   * @param ownerStemIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerStemIdsOfAssignAssign(String ownerStemIdsOfAssignAssign1) {
    if (this.ownerStemIdsOfAssignAssign == null) {
      this.ownerStemIdsOfAssignAssign = new LinkedHashSet<String>();
    }
    this.ownerStemIdsOfAssignAssign.add(ownerStemIdsOfAssignAssign1);
    return this;
  }
  
  /**
   * look for values of stems where it is an assignment on assignment
   * @param ownerStemIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerStemsOfAssignAssign(Collection<Stem> ownerStemIdsOfAssignAssign1) {
    
    if (ownerStemIdsOfAssignAssign1 == null) {
      this.ownerStemIdsOfAssignAssign = null;
    } else {
    
      this.ownerStemIdsOfAssignAssign = new LinkedHashSet<String>();
      for (Stem stem : ownerStemIdsOfAssignAssign1) {
        this.ownerStemIdsOfAssignAssign.add(stem.getId());
      }
    }
    return this;
  }
  
  /**
   * look for values of stems where it is an assignment on assignment
   * @param ownerStemIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerStemOfAssignAssign(Stem ownerStemIdsOfAssignAssign1) {
    return this.addOwnerStemIdsOfAssignAssign(ownerStemIdsOfAssignAssign1.getId());
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
   * look for values of attribute defs where it is an assignment on assignment
   */
  private Collection<String> ownerAttributeDefIdsOfAssignAssign;
  
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
   * @return the result which has the values
   */
  public AttributeAssignValueFinderResult findAttributeAssignValuesResult() {
    
    AttributeAssignValueFinderResult result = new AttributeAssignValueFinderResult();
    
    
    AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder()
      .assignOwnerGroupIds(this.ownerGroupIdsOfAssignAssign)
      .assignOwnerStemIds(this.ownerStemIdsOfAssignAssign)
      .assignOwnerAttributeDefIds(this.ownerAttributeDefIdsOfAssignAssign)
      .assignIncludeAssignmentsOnAssignments(true)
      .assignAttributeDefNameIds(this.attributeDefNameIds)
      .assignAttributeCheckReadOnAttributeDef(this.attributeCheckReadOnAttributeDef);
    
    result.allAttributeAssigns = attributeAssignFinder.findAttributeAssigns();

    if (GrouperUtil.length(result.allAttributeAssigns) == 0) {
      return result;
    }
    //separate into assigns and assigns on assigns
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(result.allAttributeAssigns)) {
      
      result.mapAttributeAssignIdToAttributeAssign.put(attributeAssign.getId(), attributeAssign);
      
      if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
        result.attributeAssignsOnAssigns.add(attributeAssign);
        result.attributeAssignsOnAssignsIds.add(attributeAssign.getId());
      } else {
        
        result.attributeAssigns.add(attributeAssign);

        if (attributeAssign.isHasSingleId()) {
          //single assign map
          String ownerId = attributeAssign.getOwnerSingleId();
          result.mapOwnerIdToAttributeAssign.put(ownerId, attributeAssign);
        
          // multi assign map
          Set<AttributeAssign> attributeAssigns = result.mapOwnerIdToAttributeAssigns.get(attributeAssign.getOwnerSingleId());
          if (attributeAssigns == null) {
            attributeAssigns = new HashSet<AttributeAssign>();
            result.mapOwnerIdToAttributeAssigns.put(ownerId, attributeAssigns);
          }
          attributeAssigns.add(attributeAssign);
        }
        
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

    Set<String> theAttributeDefNameIds = new HashSet<String>();
    
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(result.allAttributeAssigns)) {

      String attributeDefNameId = attributeAssign.getAttributeDefNameId();
      if (!theAttributeDefNameIds.contains(attributeDefNameId)) {
        attributeDefNameFinder.addIdOfAttributeDefName(attributeDefNameId);
        theAttributeDefNameIds.add(attributeDefNameId);
      }
      
    }
   
    if (this.attributeCheckReadOnAttributeDef) {
      attributeDefNameFinder.assignPrivileges(AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES);
    }
    
    Set<AttributeDefName> attributeDefNames = attributeDefNameFinder.findAttributeNames();

    final Set<String> attributeDefIds = new HashSet<String>();
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      result.mapAttributeDefNameIdToAttributeDefName.put(attributeDefName.getId(), attributeDefName);
      attributeDefIds.add(attributeDefName.getAttributeDefId());
      result.mapAttributeDefIdToAttributeDef.put(attributeDefName.getAttributeDefId(), attributeDefName.getAttributeDef());
    }
    
    //setup the attribute defs and attribute def names in assignments
    for (AttributeAssign attributeAssign : GrouperUtil.nonNull(result.allAttributeAssigns)) {
      String attributeDefNameId = attributeAssign.getAttributeDefNameId();
      AttributeDefName attributeDefName = result.mapAttributeDefNameIdToAttributeDefName.get(attributeDefNameId);
      
      String attributeDefId = attributeDefName.getAttributeDefId();
      
      AttributeDef attributeDef = result.mapAttributeDefIdToAttributeDef.get(attributeDefId);
      
      attributeDefName.internalSetAttributeDef(attributeDef);
      attributeAssign.internalSetAttributeDef(attributeDef);
      attributeAssign.internalSetAttributeDefName(attributeDefName);
      
    }
    
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(result.attributeAssignValues)) {
      
      String attributeAssignId = attributeAssignValue.getAttributeAssignId();
      
      if (result.mapAttributeAssignOnAssignIdToAttributeAssignValue.get(attributeAssignId) != null) {
        throw new RuntimeException("AttributeAssignId: " + attributeAssignId + " should only have one value but has multiple!");
      }

      result.mapAttributeAssignOnAssignIdToAttributeAssignValue.put(attributeAssignId, attributeAssignValue);

      AttributeAssign attributeAssign = result.mapAttributeAssignIdToAttributeAssign.get(attributeAssignId);
      attributeAssignValue.internalSetAttributeAssign(attributeAssign);

    }
    
    Collection<String> ownerIdsOfAssignAssign = GrouperUtil.defaultIfNull(
        GrouperUtil.defaultIfNull(this.ownerGroupIdsOfAssignAssign, this.ownerStemIdsOfAssignAssign), this.ownerAttributeDefIdsOfAssignAssign);
    
    for (String ownerId : GrouperUtil.nonNull(ownerIdsOfAssignAssign)) {
      
      AttributeAssign attributeAssign = result.mapOwnerIdToAttributeAssign.get(ownerId);
      
      Set<String> attributeAssignAssignIds = result.mapAttributeAssignIdToAssignAssignIds.get(attributeAssign.getId());
      
      for (String attributeAssignAssignId : GrouperUtil.nonNull(attributeAssignAssignIds)) {
        AttributeAssign attributeAssignOnAssign = result.mapAttributeAssignIdToAttributeAssign.get(attributeAssignAssignId);
            
        AttributeDefName attributeDefName = result.mapAttributeDefNameIdToAttributeDefName.get(attributeAssignOnAssign.getAttributeDefNameId());

        MultiKey multiKey = new MultiKey(ownerId, attributeDefName.getName());
        
        if (result.mapOwnerIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.get(multiKey) != null) {
          throw new RuntimeException("Why does ownerId and attributeDefName already exist? " + ownerId + ", " + attributeDefName.getName());
        }
        
        result.mapOwnerIdAndNameOfAttributeDefNameToAttributeAssignOnAssign.put(multiKey, attributeAssignOnAssign);
        
      }
      
    }
    
    return result;
    
  }

  /**
   * find all the attribute assigns
   * @return the set of values or the empty set if none found
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

  /**
   * look for values of AttributeDefs where it is an assignment on assignment
   * @param ownerAttributeDefIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerAttributeDefIdsOfAssignAssign(String ownerAttributeDefIdsOfAssignAssign1) {
    if (this.ownerAttributeDefIdsOfAssignAssign == null) {
      this.ownerAttributeDefIdsOfAssignAssign = new LinkedHashSet<String>();
    }
    this.ownerAttributeDefIdsOfAssignAssign.add(ownerAttributeDefIdsOfAssignAssign1);
    return this;
  }

  /**
   * look for values of AttributeDefs where it is an assignment on assignment
   * @param ownerAttributeDefIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder addOwnerAttributeDefOfAssignAssign(Stem ownerAttributeDefIdsOfAssignAssign1) {
    return this.addOwnerAttributeDefIdsOfAssignAssign(ownerAttributeDefIdsOfAssignAssign1.getId());
  }

  /**
   * look for values of AttributeDefs where it is an assignment on assignment
   * @param ownerAttributeDefIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerAttributeDefIdsOfAssignAssign(Collection<String> ownerAttributeDefIdsOfAssignAssign1) {
    this.ownerAttributeDefIdsOfAssignAssign = ownerAttributeDefIdsOfAssignAssign1;
    return this;
  }

  /**
   * look for values of AttributeDefs where it is an assignment on assignment
   * @param ownerAttributeDefIdsOfAssignAssign1
   * @return this for chaining
   */
  public AttributeAssignValueFinder assignOwnerAttributeDefsOfAssignAssign(Collection<AttributeDef> ownerAttributeDefIdsOfAssignAssign1) {
    
    if (ownerAttributeDefIdsOfAssignAssign1 == null) {
      this.ownerAttributeDefIdsOfAssignAssign = null;
    } else {
    
      this.ownerAttributeDefIdsOfAssignAssign = new LinkedHashSet<String>();
      for (AttributeDef attributeDef : ownerAttributeDefIdsOfAssignAssign1) {
        this.ownerAttributeDefIdsOfAssignAssign.add(attributeDef.getId());
      }
    }
    return this;
  }  

}
