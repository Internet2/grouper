package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.misc.GrouperCloneable;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class ProvisioningUpdatableAttributeAndValue implements GrouperCloneable {

  private GrouperProvisioner grouperProvisioner;
  
  /**
   * compare value might be different than the actual value
   */
  private String compareValue;
  
  /**
   * compare value might be different than the actual value
   * @return
   */
  public String getCompareValue() {
    return compareValue;
  }

  /**
   * compare value might be different than the actual value
   * @param compareValue
   */
  public void setCompareValue(String compareValue) {
    this.compareValue = compareValue;
  }

  /** 
   * clone an object (deep clone, on fields that make sense)
   * @see Object#clone()
   * @return the clone of the object
   */
  public Object clone() {
    ProvisioningUpdatableAttributeAndValue clone = new ProvisioningUpdatableAttributeAndValue();
    clone.grouperProvisioner = this.grouperProvisioner;
    clone.attributeName = this.attributeName;
    clone.attributeValue = this.attributeValue;
    clone.compareValue = this.compareValue;
    clone.currentValue = this.currentValue;
    return clone;
  }

  
  @Override
  public String toString() {
    return "[" + this.attributeName + ", val: " + this.attributeValue + ", compareVal: " + this.compareValue + (this.currentValue == null ? "" : (", currentValue: " + this.currentValue) ) + "]";
  }

  /**
   * 
   */
  private ProvisioningUpdatableAttributeAndValue() {
  }

  /**
   * 
   * @param attributeName
   * @param attributeValue
   */
  public ProvisioningUpdatableAttributeAndValue(GrouperProvisioner grouperProvisioner, String attributeName, Object attributeValue, 
      GrouperProvisioningConfigurationAttributeType grouperProvisioningConfigurationAttributeType) {
    super();
    this.grouperProvisioner = grouperProvisioner;
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
    
    if (grouperProvisioningConfigurationAttributeType == GrouperProvisioningConfigurationAttributeType.group) {
      this.compareValue = this.grouperProvisioner
          .retrieveGrouperProvisioningCompare().attributeValueForCompareGroup(
          attributeName, attributeValue);
    } else if (grouperProvisioningConfigurationAttributeType == GrouperProvisioningConfigurationAttributeType.entity) {
      this.compareValue = this.grouperProvisioner
          .retrieveGrouperProvisioningCompare().attributeValueForCompareEntity(
          attributeName, attributeValue);
    } else if (grouperProvisioningConfigurationAttributeType == GrouperProvisioningConfigurationAttributeType.membership) {
      this.compareValue = GrouperUtil.stringValue(attributeValue);
    } else {
      throw new RuntimeException("Not expecting type: " + grouperProvisioningConfigurationAttributeType);
    }
  }

  /**
   * if this is a current value or a past value.  This is only applicable on the Grouper side which holds past values
   */
  private Boolean currentValue = null;

  /**
   * if this is a current value or a past value.  This is only applicable on the Grouper side which holds past values
   * @return
   */
  public Boolean getCurrentValue() {
    return currentValue;
  }

  /**
   * if this is a current value or a past value.  This is only applicable on the Grouper side which holds past values
   * @param currentValue
   */
  public void setCurrentValue(Boolean currentValue) {
    this.currentValue = currentValue;
  }

  /**
   * attribute name
   */
  private String attributeName;

  /**
   * this is generally a string, perhaps and integer, or a multikey
   */
  private Object attributeValue;

  /**
   * 
   * @return
   */
  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
    this.hashCode = -1;
  }

  public Object getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(Object attributeValue) {
    this.attributeValue = attributeValue;
    this.hashCode = -1;
  }

  private int hashCode = -1;
  
  @Override
  public int hashCode() {
    if (this.hashCode == -1) {
      this.hashCode = new HashCodeBuilder()
          .append( this.attributeName)
          .append( this.compareValue)
          .toHashCode();
    }
    return this.hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProvisioningUpdatableAttributeAndValue other = (ProvisioningUpdatableAttributeAndValue) obj;

    return new EqualsBuilder()
      .append(this.attributeName, other.attributeName)
      .append(this.compareValue, other.compareValue)
      .isEquals();
  }
  
  
  
}
