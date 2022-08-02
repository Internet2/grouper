package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.misc.GrouperCloneable;

/**
 * 
 * @author mchyzer
 *
 */
public class ProvisioningUpdatableAttributeAndValue implements GrouperCloneable {

  /** 
   * clone an object (deep clone, on fields that make sense)
   * @see Object#clone()
   * @return the clone of the object
   */
  public Object clone() {
    ProvisioningUpdatableAttributeAndValue clone = new ProvisioningUpdatableAttributeAndValue(
        this.attributeName, this.attributeValue);
    clone.currentValue = this.currentValue;
    return clone;
  }

  
  @Override
  public String toString() {
    return "[" + this.attributeName + ", " + this.attributeValue + (this.currentValue == null ? "" : (", currentValue: " + this.currentValue) ) + "]";
  }

  /**
   * 
   */
  public ProvisioningUpdatableAttributeAndValue() {
  }

  /**
   * 
   * @param attributeName
   * @param attributeValue
   */
  public ProvisioningUpdatableAttributeAndValue(String attributeName, Object attributeValue) {
    super();
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
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
          .append( this.attributeValue)
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
      .append(this.attributeValue, other.attributeValue)
      .isEquals();
  }
  
  
  
}
