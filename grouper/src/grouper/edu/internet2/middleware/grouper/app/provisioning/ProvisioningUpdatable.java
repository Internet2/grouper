package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public abstract class ProvisioningUpdatable {

  /**
   * if there is a problem syncing this object to the target set the exception here
   */
  private Exception exception;
  
  private Map<MultiKey, Object> internal_fieldsToUpdate = null;
  /**
   * more attributes in name/value pairs
   */
  private Map<String, ProvisioningAttribute> attributes = null;

  /**
   * multikey is either the string "field", "attribute", the second param is field name or attribute name
   * third param is "insert", "update", or "delete"
   * and the value is the old value
   * @return
   */
  public Map<MultiKey, Object> getInternal_fieldsToUpdate() {
    return internal_fieldsToUpdate;
  }

  public void setInternal_fieldsToUpdate(Map<MultiKey, Object> internal_fieldsToUpdate) {
    this.internal_fieldsToUpdate = internal_fieldsToUpdate;
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
  
}
