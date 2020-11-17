package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * name value pair could be multi valued
 * @author mchyzer
 *
 */
public class ProvisioningAttribute {

  private static String scalarString(Object value) {
    if (value == null) {
      return "<null>";
    }
    if (value instanceof String) {
      return "\"" + (String)value + "\"";
    }
    return GrouperUtil.stringValue(value);
  }
  
  public String toString() {
    if (value instanceof Set) {
      
      StringBuilder result = new StringBuilder();
      
      result.append("[");
      
      Set valueSet = (Set)value;
      
      if (GrouperUtil.length(valueSet) > 0) {
        int attrCount = 0;
        // order these
        for (Object eachValue : valueSet) {
          
          if (attrCount > 0) {
            result.append(", ");
          }
          // dont go crazy here
          if (attrCount >= 100) {
            result.append("100/" + GrouperUtil.length(valueSet) + " displayed");
            break;
          }
          
          result.append(scalarString(eachValue));
          
          attrCount++;
        }
      }
      
      
      result.append("]");
      
      return result.toString();
    }

    return scalarString(this.value);
    
  }

  /**
   * if this attribute is multivalued and represents a membership, this map links a value with a specific membership
   * so it can be logged and tracked
   */
  private Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = null;
  
  
  
  /**
   * if this attribute is multivalued and represents a membership, this map links a value with a specific membership
   * so it can be logged and tracked
   * @return the map
   */
  public Map<Object, ProvisioningMembershipWrapper> getValueToProvisioningMembershipWrapper() {
    return valueToProvisioningMembershipWrapper;
  }

  /**
   * if this attribute is multivalued and represents a membership, this map links a value with a specific membership
   * so it can be logged and tracked
   * @param valueToProvisioningMembershipWrapper
   */
  public void setValueToProvisioningMembershipWrapper(
      Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper) {
    this.valueToProvisioningMembershipWrapper = valueToProvisioningMembershipWrapper;
  }

  /**
   * name of attribute
   */
  private String name;
  
  /**
   * value could be multi valued
   */
  private Object value;

  /**
   * name of attribute
   * @return name of attribute
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of attribute
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value could be multi valued
   * @return value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * value could be multi valued
   * @param value1
   */
  public void setValue(Object value1) {
    this.value = value1;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public ProvisioningAttribute clone() {

    ProvisioningAttribute provisioningAttribute = new ProvisioningAttribute();

    provisioningAttribute.name = this.name;
    provisioningAttribute.valueToProvisioningMembershipWrapper = 
        this.valueToProvisioningMembershipWrapper == null ? null 
            : new HashMap<Object, ProvisioningMembershipWrapper>(this.valueToProvisioningMembershipWrapper);
    
    Object newValue = null;
    if (this.value != null) {
      if (this.value instanceof Set) {
        newValue = new HashSet((Set)this.value);
      } else if (this.value instanceof List) {
          newValue = new ArrayList((Set)this.value);
      } else if (this.value instanceof Collection || this.value instanceof Map || this.value.getClass().isArray()) {
        throw new RuntimeException("Not expecting type of attribute: " + this.value.getClass().getName());
      } else {
        newValue = this.value;
      }
    }      
    provisioningAttribute.value = newValue;

    return provisioningAttribute;
  }

}
