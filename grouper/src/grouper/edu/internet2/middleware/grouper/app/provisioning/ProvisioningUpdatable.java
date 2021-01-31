package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public abstract class ProvisioningUpdatable {

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
  
  /**
   * if searching for this object, this is the search filter, translated and ready to use
   */
  private String searchFilter;
  
  /**
   * see if this object is empty e.g. after translating if empty then dont keep track of group
   * since the translation might have affected another object
   * @return
   */
  protected final boolean isEmptyUpdatable() {
    if (matchingId == null && GrouperUtil.length(this.attributes) == 0) {
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
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = GrouperProvisioningTranslatorBase.retrieveProvisioningMembershipWrapper();
    
    if (provisioningMembershipWrapper == null) {
      throw new NullPointerException("Cant find membership wrapper! " + name + ", " + value + ", " + this);
    }

    if (!provisioningMembershipWrapper.isDelete()) {
      this.addAttributeValue(name, value);
    }

    // keep track of membership this attribute value represents
    ProvisioningAttribute provisioningAttribute = this.getAttributes().get(name);
    
    if (provisioningAttribute == null) {
      // maybe this is a delete? 
      if (this.attributes == null) {
        this.attributes = new HashMap<String, ProvisioningAttribute>();
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

    valueToProvisioningMembershipWrapper.put(value, provisioningMembershipWrapper);

  }

  /**
   * this is a multivalued attribute using sets
   * @param name
   * @param value
   */
  public void addAttributeValue(String name, Object value) {

    if (this.attributes == null) {
      this.attributes = new HashMap<String, ProvisioningAttribute>();
    }
    
    ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
    
    Set<Object> values = null;
    
    if (provisioningAttribute == null) {
      provisioningAttribute = new ProvisioningAttribute();
      this.attributes.put(name, provisioningAttribute);
      provisioningAttribute.setName(name);
    } else {
      values = (Set<Object>)provisioningAttribute.getValue();
    }
    
    if (values == null) {
      values = new HashSet<Object>();
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
      this.attributes = new HashMap<String, ProvisioningAttribute>();
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

        result.append(provisioningObjectChange.getProvisioningObjectChangeAction()).append(" ");
        result.append(provisioningObjectChange.getProvisioningObjectChangeDataType()).append(" ");
        result.append(
            provisioningObjectChange.getProvisioningObjectChangeDataType() == ProvisioningObjectChangeDataType.field ? 
                provisioningObjectChange.getFieldName() : provisioningObjectChange.getAttributeName()).append(" ");
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
    } else if (fieldValue.getClass().isArray() || fieldValue instanceof Map) {
      result.append(GrouperUtil.toStringForLog(fieldValue, 1000));
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
    if (provisioningUpdatable.attributes != null) {
      newAttributes = new HashMap<String, ProvisioningAttribute>();
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
