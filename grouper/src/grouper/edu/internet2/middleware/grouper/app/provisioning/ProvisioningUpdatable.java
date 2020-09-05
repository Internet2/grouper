package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public abstract class ProvisioningUpdatable {

  /**
   * string, number, or multikey of strings and numbers
   */
  private Object targetId;
  

  /**
   * string, number, or multikey of strings and numbers
   * @return
   */
  public Object getTargetId() {
    return targetId;
  }

  /**
   * string, number, or multikey of strings and numbers
   * @param targetId
   */
  public void setTargetId(Object targetId) {
    this.targetId = targetId;
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
    this.addAttributeValue(name, value);
    ProvisioningAttribute provisioningAttribute = this.attributes.get(name);
    ProvisioningMembershipWrapper provisioningMembershipWrapper = GrouperProvisioningTranslatorBase.retrieveProvisioningMembershipWrapper();
    if (provisioningMembershipWrapper == null) {
      throw new NullPointerException("Cant find membership wrapper! " + name + ", " + value + ", " + this);
    }
    provisioningAttribute.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
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
      values = new HashSet<Object>();
      provisioningAttribute.setValue(values);
    } else {
      values = (Set<Object>)provisioningAttribute.getValue();
    }
    
    values.add(value);

  }

  /**
   * 
   * @param name
   * @param value
   */
  public void assignAttribute(String name, Object value) {
    
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
    firstField = toStringAppendField(result, firstField, "targetId", this.targetId);
    firstField = toStringAppendField(result, firstField, "exception", this.exception);
    if (this.removeFromList) {
      firstField = toStringAppendField(result, firstField, "removeFromList", this.removeFromList);
    }
    if (GrouperUtil.length(this.attributes) > 0) {
      int attrCount = 0;
      // order these
      Set<String> keySet = new TreeSet<String>(this.attributes.keySet());
      for (String key : keySet) {
        
        // dont go crazy here
        if (attrCount++ > 100) {
          result.append(", Only first 100 attributes displayed");
          return firstField;
        }

        ProvisioningAttribute attrValue = this.attributes.get(key);
        firstField = toStringAppendField(result, firstField, "attr_" + key, attrValue == null ? "null" : attrValue.getValue());
        
      }
    }
    return firstField;
  }
  
  /**
   * 
   * @param result
   * @param firstField
   * @param fieldName
   * @param fieldValue
   * @return
   */
  protected static boolean toStringAppendField(StringBuilder result, boolean firstField, String fieldName, Object fieldValue) {
    if (fieldValue == null || GrouperUtil.length(fieldValue) == 0) {
      return firstField;
    }
    if (!firstField) {
      result.append(", ");
    }
    firstField = false;
    result.append(fieldName).append(": ");
    
    if (fieldValue.getClass().isArray() || fieldValue instanceof Collection || fieldValue instanceof Map) {
      result.append(GrouperUtil.toStringForLog(fieldValue, 1000));
    } else {
      result.append(GrouperUtil.stringValue(fieldValue));
    }
    
    return firstField;
  }

}
