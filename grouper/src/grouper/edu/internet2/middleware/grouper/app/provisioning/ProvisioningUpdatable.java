package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public abstract class ProvisioningUpdatable {

  /**
   * get the object type name, e.g. group, entity, membership
   * @return the object type name
   */
  public abstract String objectTypeName();
  
  private static final String TRUNC_ATTRS = "trunc_attrs";

  /**
   * get the config for an attribute based on name if a wrapper object is 
   * associated with this object
   * @param attributeName
   * @return the attribute configuration
   */
  public GrouperProvisioningConfigurationAttribute retriveAttributeConfig(String attributeName) {
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    if (grouperProvisioner == null) {
      return null;
    }
    if (this instanceof ProvisioningGroup) {
      return grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeName);
    }
    if (this instanceof ProvisioningEntity) {
      return grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(attributeName);
    }
    if (this instanceof ProvisioningMembership) {
      return grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(attributeName);
    }
    throw new RuntimeException("Not expecting object of type: " + this.getClass());
  }
  
  /**
   * if this object is hooked up to a wrapper and hooked up to a provisioner, then return that provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    if (this instanceof ProvisioningGroup) {
      ProvisioningGroupWrapper provisioningGroupWrapper = ((ProvisioningGroup)this).getProvisioningGroupWrapper();
      return provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperProvisioner();
    }
    if (this instanceof ProvisioningEntity) {
      ProvisioningEntityWrapper provisioningEntityWrapper = ((ProvisioningEntity)this).getProvisioningEntityWrapper();
      return provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperProvisioner();
    }
    if (this instanceof ProvisioningMembership) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = ((ProvisioningMembership)this).getProvisioningMembershipWrapper();
      return provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGrouperProvisioner();
    }
    return null;
  }
  
  /**
   * convert from cached json string to hydrate this object
   * @param json
   */
  public void fromJsonForCache(String json) {
    
    if (StringUtils.isBlank(json)) {
      return;
    }

    JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
    
    Iterator<String> attributeNames = rootNode.fieldNames();
    while(attributeNames.hasNext()) {
      String attributeName = attributeNames.next();
      JsonNode fieldNode = rootNode.get(attributeName);
      if (fieldNode instanceof NullNode) {
        continue;
      }
      if (StringUtils.equals(attributeName, TRUNC_ATTRS)) {
        // this is an array of strings
        if (fieldNode != null) {
            
          if (fieldNode.isArray()) {
            this.truncatedAttributeNames = new HashSet<String>();
            for (int i=0;i<fieldNode.size();i++) {
              String truncName = fieldNode.get(i).asText();
              this.truncatedAttributeNames.add(truncName);
            }
          }
        }
        //this is a normal string attribute
      } else if (fieldNode.isTextual()) {
        String value = fieldNode.asText();
        this.assignAttributeValue(attributeName, value);
        //this is a normal string attribute
      } else if (fieldNode.isIntegralNumber()) {
        // we know its an integer, but is it a long or an int???
        Object value = fieldNode.asLong();
        GrouperProvisioningConfigurationAttribute configuration = retriveAttributeConfig(attributeName);
        if (configuration != null && configuration.getValueType() == GrouperProvisioningConfigurationAttributeValueType.INT) {
          value = GrouperUtil.intValue(value);
        }
        this.assignAttributeValue(attributeName, value);
      } else if (fieldNode.isArray()) {
        boolean isInt = false;
        // this is either strings or ints or longs
        for (int i=0;i<fieldNode.size();i++) {
          JsonNode eachIndexNode = fieldNode.get(i);
          if (eachIndexNode.isTextual()) {
            String value = eachIndexNode.asText();
            this.addAttributeValue(attributeName, value);
            
          } else if (eachIndexNode.isIntegralNumber()) {
            // we know its an integer, but is it a long or an int???
            Object value = eachIndexNode.asLong();
            // just figure this out once
            if (i == 0) {
              GrouperProvisioningConfigurationAttribute configuration = retriveAttributeConfig(attributeName);
              if (configuration != null && configuration.getValueType() == GrouperProvisioningConfigurationAttributeValueType.INT) {
                isInt = true;
              }
            }
            if (isInt) {
              value = GrouperUtil.intValue(value);
            }
            this.addAttributeValue(attributeName, value);
          } else {
            throw new RuntimeException("Not expecting node type: " + eachIndexNode.getNodeType());
          }
        }
      } else {
        throw new RuntimeException("Not expecting node type: " + fieldNode.getNodeType());
      }
    }
  }

  /**
   * if hydrating from json cache, these are the attribute names that were truncated
   */
  private Set<String> truncatedAttributeNames;

  /**
   * if hydrating from json cache, these are the attribute names that were truncated
   * @return the attribute names that were truncated
   */
  public Set<String> getTruncatedAttributeNames() {
    return this.truncatedAttributeNames;
  }

  /**
   * convert to json for cache, keep under 4k, keep track of truncated fields
   * @param membershipAttribute 
   * @return the json of this group for cache
   */
  public String toJsonForCache(String membershipAttribute) {

    Map<String, ProvisioningAttribute> theAttributes = this.getAttributes();
    Map<String, Integer> attributeSize = new HashMap<String, Integer>();
    Integer overByChars = null;

    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    Set<String> theTruncatedAttributeNames = new TreeSet<String>();
    
    // max length of each field as we try to fit into 4k
    for (int truncateTo : new int[] {-1, 2000, 1000, 500, 250, 100}) {
      
      for (String attributeName : GrouperUtil.nonNull(theAttributes).keySet()) {
        
        // ignore memberships
        if (StringUtils.equals(membershipAttribute, attributeName)) {
          continue;
        }
        
        // ignore nulls
        Object value = theAttributes.get(attributeName);
        if (value == null) {
          continue;
        }
        
        if (value instanceof ProvisioningAttribute) {
          ProvisioningAttribute provisioningAttribute = (ProvisioningAttribute)value;
          value = provisioningAttribute.getValue();
        }
        if (value == null) {
          continue;
        }
        
        if (value instanceof Integer || value instanceof Long) {
          // just add during first pass
          if (truncateTo == -1) {
            GrouperUtil.jsonJacksonAssignLong(objectNode, attributeName, GrouperUtil.longValue(value));
          }
        } else if (value instanceof String) {
          
          if (truncateTo > -1 && ((String)value).length() > truncateTo && overByChars != null && overByChars > 0) {
            
            theTruncatedAttributeNames.add(attributeName);
            int oldSize = GrouperUtil.intValue(attributeSize.get(attributeName), ((String)value).length());
            overByChars -= oldSize - truncateTo;
            value = StringUtils.abbreviate((String)value, truncateTo);
            GrouperUtil.jsonJacksonAssignString(objectNode, attributeName, (String)value);
            attributeSize.put(attributeName, ((String)value).length());            
          } else {
            if (truncateTo == -1) {
              GrouperUtil.jsonJacksonAssignString(objectNode, attributeName, (String)value);
              attributeSize.put(attributeName, ((String)value).length());
            }
          }
        } else if (value instanceof Collection) {
          // if its the first pass or has already been yanked for being too big
          if (truncateTo == -1 || !theTruncatedAttributeNames.contains(attributeName)) {
            int collectionSize = GrouperUtil.intValue(attributeSize.get(attributeName), -1);
            ArrayNode arrayNode = (ArrayNode)objectNode.get(attributeName);
            if (collectionSize == -1) {
              arrayNode = GrouperUtil.jsonJacksonArrayNode();
              collectionSize = 0;
              Collection<?> valueCollection = (Collection<?>)value;
              for (Object eachValue : valueCollection) {
                if (eachValue == null) {
                  continue;
                }
                if (eachValue instanceof Integer) {
                  collectionSize += 8;
                  arrayNode.add((Integer)eachValue);
                } else if (eachValue instanceof Long) {
                  collectionSize += 12;
                  arrayNode.add((Long)eachValue);
                } else if (eachValue instanceof String) {
                  collectionSize += ((String)eachValue).length();
                  arrayNode.add((String)eachValue);
                } else {
                  throw new RuntimeException("Invalid collection type: " + eachValue.getClass());
                }
              }
              attributeSize.put(attributeName, collectionSize);
            }            
            if (truncateTo > -1 && collectionSize > truncateTo && overByChars != null && overByChars > 0) {
              
              // just skip it
              theTruncatedAttributeNames.add(attributeName);
              overByChars -= collectionSize;
            } else {
  
              if (truncateTo == -1) {
                objectNode.set(attributeName, arrayNode);
              }
            }
          }          
        } else {
          throw new RuntimeException("Invalid field type: " + value.getClass());
        }
        
      }
      if (theTruncatedAttributeNames.size() > 0) {
        ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
        for (String truncatedAttributeName : theTruncatedAttributeNames) {
          arrayNode.add(truncatedAttributeName);
        }
        objectNode.set(TRUNC_ATTRS, arrayNode);
      }
      String json = GrouperUtil.jsonJacksonToString(objectNode);
      if (json.length() <= 3700) {
        return json;
      }
      // add 50 so we can add some truncated fields or whatever
      overByChars = 50+json.length() - 3700;
    }
    
    return null;
  }

  /**
   * see which attribute names are different when comparing to a cached object from json
   * note if there is a truncated field, consider that so the same prefix is ok
   * @param provisioningUpdatable
   * @param membershipAttribute
   * @return the changed fields
   */
  public Set<String> attributeNamesDifferentForCache(ProvisioningUpdatable provisioningUpdatable, String membershipAttribute) {

    boolean isGroup = this instanceof ProvisioningGroup && provisioningUpdatable instanceof ProvisioningGroup;
    boolean isEntity = this instanceof ProvisioningEntity && provisioningUpdatable instanceof ProvisioningEntity;
    boolean isMembership = this instanceof ProvisioningMembership && provisioningUpdatable instanceof ProvisioningMembership;
    
    if (!isGroup && !isEntity && !isMembership) {
      throw new RuntimeException("Not expecting object type: " + this.getClass() + " not compatible with " + (provisioningUpdatable == null ? null : provisioningUpdatable.getClass()));
    }
    
    Map<String, ProvisioningAttribute> thisAttributes = GrouperUtil.nonNull(this.getAttributes());
    Map<String, ProvisioningAttribute> thatAttributes = GrouperUtil.nonNull(provisioningUpdatable.getAttributes());

    Set<String> thisTruncatedAttributeNames = GrouperUtil.nonNull(this.getTruncatedAttributeNames());
    Set<String> thatTruncatedAttributeNames = GrouperUtil.nonNull(provisioningUpdatable.getTruncatedAttributeNames());
    
    Set<String> differentAttributes = new HashSet<String>();
    
    for (String attributeName : GrouperUtil.nonNull(thisAttributes).keySet()) {
        
      // ignore memberships
      if (StringUtils.equals(membershipAttribute, attributeName)) {
        continue;
      }
      
      Object thisValue = thisAttributes.get(attributeName);
      Object thatValue = thatAttributes.get(attributeName);
      
      if (thisValue instanceof ProvisioningAttribute) {
        ProvisioningAttribute provisioningAttribute = (ProvisioningAttribute)thisValue;
        thisValue = provisioningAttribute.getValue();
      }
      if (thatValue instanceof ProvisioningAttribute) {
        ProvisioningAttribute provisioningAttribute = (ProvisioningAttribute)thatValue;
        thatValue = provisioningAttribute.getValue();
      }

      // null or empty is ok
      if ((thisValue == null || GrouperUtil.isEmpty(thisValue)) && (thatValue == null || GrouperUtil.isEmpty(thatValue))) {
        continue;
      }


      if (thisValue instanceof Collection || thatValue instanceof Collection) {
        
        if (thisTruncatedAttributeNames.contains(attributeName) || thatTruncatedAttributeNames.contains(attributeName)) {
          // we ignore this...
          continue;
        }
        
        if (!(thisValue instanceof Collection) || !(thatValue instanceof Collection)) {
          differentAttributes.add(attributeName);
          continue;
        }

        Collection<?> thisCollection = (Collection<?>)thisValue;
        Collection<?> thatCollection = (Collection<?>)thatValue;

        if (GrouperUtil.equalsCollectionStringLong(thisCollection, thatCollection)) {
          continue;
        }
        
        differentAttributes.add(attributeName);
        continue;
      }        

      // if one is its different
      if (thisValue == null || GrouperUtil.isEmpty(thisValue) || thatValue == null || GrouperUtil.isEmpty(thatValue)) {
        differentAttributes.add(attributeName);
        continue;
      }
      
      if (thisValue instanceof Integer || thisValue instanceof Long || thatValue instanceof Integer || thatValue instanceof Long) {

        if (GrouperUtil.equalsLong(thisValue, thatValue)) {
          continue;
        }
        
        differentAttributes.add(attributeName);
        continue;

      }
      
      if (thisValue instanceof String || thatValue instanceof String) {
        
        thisValue = GrouperUtil.stringValue(thisValue);
        thatValue = GrouperUtil.stringValue(thatValue);
        
        if (thisTruncatedAttributeNames.contains(attributeName)) {
          thatValue = StringUtils.abbreviate((String)thatValue, ((String)thisValue).length());
        }
        if (thatTruncatedAttributeNames.contains(attributeName)) {
          thisValue = StringUtils.abbreviate((String)thisValue, ((String)thatValue).length());
        }
        
        if (GrouperUtil.equalsString(thisValue, thatValue)) {
          continue;
        }
        
        differentAttributes.add(attributeName);
        continue;
      }
      // not sure how it got here
      differentAttributes.add(attributeName);
    }
    return differentAttributes;
  }
  
  public String provisioningUpdatableTypeShort() {
    
    if (this instanceof ProvisioningGroup) {
      return "G";
    }
    if (this instanceof ProvisioningEntity) {
      return "E";
    }
    if (this instanceof ProvisioningMembership) {
      return "M";
    }
    throw new RuntimeException("Not expecting provisioning updatable: " + this.getClass());
  }

  public boolean isRecalc() {
    if (this instanceof ProvisioningGroup) {
      return ((ProvisioningGroup)this).getProvisioningGroupWrapper().isRecalc();
    }
    if (this instanceof ProvisioningEntity) {
      return ((ProvisioningEntity)this).getProvisioningEntityWrapper().isRecalc();
    }
    if (this instanceof ProvisioningMembership) {
      return ((ProvisioningMembership)this).getProvisioningMembershipWrapper().isRecalc();
    }
    throw new RuntimeException("Not expecting type: " + this.getClass().getName());
  }
  
  public abstract boolean canInsertAttribute(String name);
  public abstract boolean canUpdateAttribute(String name);
  public abstract boolean canDeleteAttribute(String name);
  public abstract boolean canDeleteAttributeValue(String name, Object deleteValue);

  /**
   * if searching for this object, this is the search filter, translated and ready to use
   */
  private String searchFilter;
  
  /**
   * see if this object is empty e.g. after translating if empty then dont keep track of group
   * since the translation might have affected another object
   * @return true if empty
   */
  public boolean isEmpty() {
    if (this.matchingId == null && GrouperUtil.length(this.attributes) == 0) {
      return true;
    }
    return false;
  }


  /**
   * 
   * @return
   */
  public String getSearchFilter() {
    return searchFilter;
  }

  /**
   * 
   * @param searchFilter
   */
  public void setSearchFilter(String searchFilter) {
    this.searchFilter = searchFilter;
  }



  /**
   * if this object has been provisioned or deprovisioned successfully, set this to true. 
   * e.g. if the insert/update/delete was successful, this should be "true"
   * otherwise set to false and set the exception field
   */
  private Boolean provisioned = null;
  
  /**
   * if this object has been provisioned or deprovisioned successfully, set this to true. 
   * e.g. if the insert/update/delete was successful, this should be "true"
   * otherwise set to false and set the exception field
   * @return if provisioned
   */
  public Boolean getProvisioned() {
    return provisioned;
  }

  /**
   * if this object has been provisioned or deprovisioned successfully, set this to true. 
   * e.g. if the insert/update/delete was successful, this should be "true"
   * otherwise set to false and set the exception field
   * @param provisioned
   */
  public void setProvisioned(Boolean provisioned) {
    this.provisioned = provisioned;
  }

  /**
   * do a deep clone of the data
   * @param provisioningUpdatables
   * @return the cloned list
   */
  public static List<ProvisioningUpdatable> clone(List<ProvisioningUpdatable> provisioningUpdatables) {
    if (provisioningUpdatables == null) {
      return null;
    }
    List<ProvisioningUpdatable> result = new ArrayList<ProvisioningUpdatable>();
    for (ProvisioningUpdatable provisioningUpdatable : provisioningUpdatables) {
      try {
        ProvisioningUpdatable provisioningUpdatableClone = (ProvisioningUpdatable)provisioningUpdatable.clone();
        result.add(provisioningUpdatableClone);
      } catch (CloneNotSupportedException cnse) {
        throw new RuntimeException("error", cnse);
      }
    }
    return result;
  }


  
  /**
   * string, number, or multikey of strings and numbers
   */
  private Object matchingId;
  

  /**
   * string, number, or multikey of strings and numbers
   * @return
   */
  public Object getMatchingId() {
    return matchingId;
  }

  /**
   * string, number, or multikey of strings and numbers
   * @param matchingId
   */
  public void setMatchingId(Object matchingId) {
    this.matchingId = matchingId;
  }

  /**
   * 
   * @param action insert or delete
   * @param attributeName
   * @param attributeValue
   */
  public void manageAttributeValue(String action, String attributeName, Object attributeValue) {
    if (StringUtils.equals(action, "insert")) {
      this.addAttributeValue(attributeName, attributeValue);
    } else if (StringUtils.equals(action, "delete")) {
      this.addAttributeValue(attributeName, attributeValue);
    } else {
      throw new RuntimeException("Invalid action: '" + action + "'");
    }

  }
  
  /**
   * after translation, toss this object
   */
  private boolean removeFromList = false;
  
  
  /**
   * after translation, toss this object
   * @return
   */
  public boolean isRemoveFromList() {
    return removeFromList;
  }

  /**
   * after translation, toss this object
   * @param removeFromList
   */
  public void setRemoveFromList(boolean removeFromList) {
    this.removeFromList = removeFromList;
  }

  /**
   * if there is a problem syncing this object to the target set the exception here
   */
  private Exception exception;
  
  private Set<ProvisioningObjectChange> internal_objectChanges = null;
  /**
   * more attributes in name/value pairs
   */
  private Map<String, ProvisioningAttribute> attributes = null;

  /**
   * 
   * @param provisioningObjectChange
   */
  public void addInternal_objectChange(ProvisioningObjectChange provisioningObjectChange) {
    
    if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert
        && provisioningObjectChange.getNewValue() == null) {
      return;
    }
    
    if (this.internal_objectChanges == null) {
      this.internal_objectChanges = new LinkedHashSet<ProvisioningObjectChange>();
    }
    this.internal_objectChanges.add(provisioningObjectChange);
  }
  
  /**
   * multikey is either the string "field", "attribute", the second param is field name or attribute name
   * third param is "insert", "update", or "delete"
   * and the value is the old value
   * @return
   */
  public Set<ProvisioningObjectChange> getInternal_objectChanges() {
    return internal_objectChanges;
  }

  /**
   * add attribute value for membership, assume this happens in a translation, and link the membership with the attribute
   * (to track when it gets saved to target or error handling)
   * @param name
   * @param value
   */
  public void addAttributeValueForMembership(String name, Object value) {
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = GrouperProvisioningTranslator.retrieveProvisioningMembershipWrapper();
    
    if (provisioningMembershipWrapper == null) {
      throw new NullPointerException("Cant find membership wrapper! " + name + ", " + value + ", " + this);
    }

    this.addAttributeValue(name, value);

    // keep track of membership this attribute value represents
    ProvisioningAttribute provisioningAttribute = this.getAttributes().get(name);
    
    if (provisioningAttribute == null) {
      // maybe this is a delete? 
      if (this.attributes == null) {
        this.attributes = new TreeMap<String, ProvisioningAttribute>();
      }
      
      provisioningAttribute = new ProvisioningAttribute();
      this.attributes.put(name, provisioningAttribute);
      provisioningAttribute.setName(name);
    }
    
    Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper();
    
    if (valueToProvisioningMembershipWrapper == null) {
      valueToProvisioningMembershipWrapper = new HashMap<Object, ProvisioningMembershipWrapper>();
      provisioningAttribute.setValueToProvisioningMembershipWrapper(valueToProvisioningMembershipWrapper);
    }

    // If there are two memberships with the same value, overwrite if the previous one was marked as delete.
    if (!valueToProvisioningMembershipWrapper.containsKey(value) || valueToProvisioningMembershipWrapper.get(value).isDelete()) {
      valueToProvisioningMembershipWrapper.put(value, provisioningMembershipWrapper);
    }

  }

  /**
   * this is a multivalued attribute using sets
   * @param name
   * @param value
   */
  public void addAttributeValue(String name, Object value) {

    if (this.attributes == null) {
      this.attributes = new TreeMap<String, ProvisioningAttribute>();
    }
    
    ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
    
    Collection<Object> values = null;
    
    if (provisioningAttribute == null) {
      provisioningAttribute = new ProvisioningAttribute();
      this.attributes.put(name, provisioningAttribute);
      provisioningAttribute.setName(name);
    } else {
      values = (Collection<Object>)provisioningAttribute.getValue();
    }
    
    if (values == null) {
      values = new TreeSet<Object>();
      provisioningAttribute.setValue(values);
    }
    
    values.add(value);

  }

  /**
   * 
   * @param name
   * @param value
   */
  public void assignAttributeValue(String name, Object value) {
    
    if (this.attributes == null) {
      this.attributes = new TreeMap<String, ProvisioningAttribute>();
    }
    
    ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
    
    if (provisioningAttribute == null) {
      provisioningAttribute = new ProvisioningAttribute();
      provisioningAttribute.setName(name);
      this.attributes.put(name, provisioningAttribute);
    }
    
    provisioningAttribute.setValue(value);
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public void removeAttribute(String name) {
    
    if (this.attributes == null) {
      return;
    }

    if (this.attributes.containsKey(name)) {
      this.attributes.remove(name);
    }
  }

  /**
   * 
   * @param name
   * @param value
   */
  public Object retrieveAttributeValue(String name) {
    
    if (this.attributes == null) {
      return null;
    }
    
    ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
    
    if (provisioningAttribute == null) {
      return null;
    }
    
    return provisioningAttribute.getValue();
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public String retrieveAttributeValueString(String name) {
    
    return GrouperUtil.stringValue(this.retrieveAttributeValue(name));
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public Set<?> retrieveAttributeValueSet(String name) {
    
    return (Set<?>)this.retrieveAttributeValue(name);
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public Integer retrieveAttributeValueInteger(String name) {
    
    return GrouperUtil.intObjectValue(this.retrieveAttributeValue(name), true);
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public Long retrieveAttributeValueLong(String name) {
    
    return GrouperUtil.longObjectValue(this.retrieveAttributeValue(name), true);
    
  }

  /**
   * 
   * @param name
   * @param value
   */
  public Boolean retrieveAttributeValueBoolean(String name) {
    
    return GrouperUtil.booleanObjectValue(this.retrieveAttributeValue(name));
    
  }

  
  
  /**
   * more attributes in name/value pairs
   * @return attributes
   */
  public Map<String, ProvisioningAttribute> getAttributes() {
    return this.attributes;
  }

  /**
   * more attributes in name/value pairs
   * @param attributes1
   */
  public void setAttributes(Map<String, ProvisioningAttribute> attributes1) {
    this.attributes = attributes1;
  }

  /**
   * if there is a problem syncing this object to the target set the exception here
   * @return
   */
  public Exception getException() {
    return exception;
  }
  
  /**
   * if there is a problem syncing this object to the target set the exception here
   * @param internal_exception
   */
  public void setException(Exception internal_exception) {
    this.exception = internal_exception;
  }
 
  protected boolean toStringProvisioningUpdatable(StringBuilder result, boolean firstField) {
    firstField = toStringAppendField(result, firstField, "matchingId", this.matchingId);
    firstField = toStringAppendField(result, firstField, "exception", this.exception);
    firstField = toStringAppendField(result, firstField, "provisioned", this.provisioned);
    if (this.removeFromList) {
      firstField = toStringAppendField(result, firstField, "removeFromList", this.removeFromList);
    }
    firstField = toStringAppendField(result, firstField, "searchFilter", this.searchFilter);
    if (GrouperUtil.length(this.attributes) > 0) {
      int attrCount = 0;
      // order these
      Set<String> keySet = new TreeSet<String>(this.attributes.keySet());
      for (String key : keySet) {
        
        // dont go crazy here
        if (attrCount++ > 100) {
          result.append(", Only first 100/" + GrouperUtil.length(this.attributes) + " attributes displayed");
          break;
        }

        ProvisioningAttribute attrValue = this.attributes.get(key);
        firstField = toStringAppendField(result, firstField, "attr[" + key + "]", attrValue.getValue(), true);
        
      }
    }
    if (GrouperUtil.length(this.internal_objectChanges) > 0) {
      int changeCount = 0;
      
      for (ProvisioningObjectChange provisioningObjectChange : this.internal_objectChanges) {
        
        if (changeCount++ > 100) {
          result.append(", Only first 100/" + GrouperUtil.length(this.internal_objectChanges) + " object changes displayed");
          break;
        }

        if (!firstField) {
          result.append(", ");
        } else {
          firstField = false;
        }

        result.append(provisioningObjectChange.getProvisioningObjectChangeAction().name().substring(0, 3)).append(" ").append(provisioningObjectChange.getAttributeName()).append(" ");
        switch(provisioningObjectChange.getProvisioningObjectChangeAction()) {
          case insert:
            result.append(stringValueWithType(provisioningObjectChange.getNewValue()));
            break;
          case delete:
            result.append(stringValueWithType(provisioningObjectChange.getOldValue()));
            break;
          case update:
            result.append(stringValueWithType(provisioningObjectChange.getOldValue()));
            result.append(" -> ");
            result.append(stringValueWithType(provisioningObjectChange.getNewValue()));
            break;
        }
      }
    }
    return firstField;
  }
  
  protected boolean toStringAppendField(StringBuilder result, boolean firstField,
      String fieldName, Object fieldValue) {
    return toStringAppendField(result, firstField, fieldName, fieldValue, false);
  }


  /**
   * 
   * @param result
   * @param firstField
   * @param fieldName
   * @param fieldValue
   * @return
   */
  protected static boolean toStringAppendField(StringBuilder result, boolean firstField, String fieldName, Object fieldValue, boolean appendIfEmpty) {
    if (!appendIfEmpty && (fieldValue == null || GrouperUtil.length(fieldValue) == 0)) {
      return firstField;
    }
    if (!firstField) {
      result.append(", ");
    }
    firstField = false;
    result.append(fieldName).append(": ");

    if (fieldValue == null) {
      result.append("<null>");
      return firstField;
    }
    if (GrouperUtil.length(fieldValue) == 0) {
      result.append("<empty>");
      return firstField;
    }
    
    if (fieldValue instanceof Collection) {
      int resultInitialLength = result.length();
      int index = 0;
      result.append(fieldValue.getClass().getSimpleName()).append("(").append(GrouperUtil.length(fieldValue)).append("): ");
      for (Object item : (Collection)fieldValue) {
        if (index > 0) {
          result.append(", ");
        }
        result.append("[").append(index).append("]: ").append(GrouperUtil.stringValue(item));
        if (result.length() - resultInitialLength > 1000) {
          result.append("...");
          break;
        }
        index++;
      }
    } else {
      result.append(stringValueWithType(fieldValue));
    }
    
    return firstField;
  }

  public static String stringValueWithType(Object value) {
    if (value == null) {
      return "<null>";
    }
    if (value instanceof String) {
      return "\"" + value + "\"";
    }
    return GrouperUtil.stringValue(value);
  }
  
  /**
   * deep clone the fields in this object
   */
  public void cloneUpdatable(ProvisioningUpdatable provisioningUpdatable) {

    Map<String, ProvisioningAttribute> newAttributes = null;
    if (this.attributes != null) {
      newAttributes = new TreeMap<String, ProvisioningAttribute>();
      for (String attributeName : this.attributes.keySet()) {
        ProvisioningAttribute provisioningAttributeToClone = this.attributes.get(attributeName);
        ProvisioningAttribute newProvisioningAttribute = null;
        if (provisioningAttributeToClone != null) {
          newProvisioningAttribute = provisioningAttributeToClone.clone();
        }
        newAttributes.put(attributeName, newProvisioningAttribute);
      }
      
    }
    provisioningUpdatable.attributes = newAttributes;
    provisioningUpdatable.exception = exception;
    // dont clone object changes
    provisioningUpdatable.exception = exception;
    provisioningUpdatable.provisioned = provisioned;
    provisioningUpdatable.removeFromList = removeFromList;
    provisioningUpdatable.searchFilter = searchFilter;
    provisioningUpdatable.matchingId = matchingId;
    
    
    
  }

  /**
   * 
   * @param groupMembershipAttribute
   */
  public void clearAttribute(String name) {
    
    if (this.attributes != null) {
    
      ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
      
      if (provisioningAttribute != null) {
        Collection collection = (Collection)provisioningAttribute.getValue();
        collection.clear();
      }
    }
    
  }

}
