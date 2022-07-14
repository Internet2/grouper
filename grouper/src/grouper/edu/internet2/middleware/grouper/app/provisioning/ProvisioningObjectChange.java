package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.app.graph.GraphEdge;

public class ProvisioningObjectChange {

  /**
   * if there is a specific exception for this one attribute, list it here
   */
  private Exception exception;

  /**
   * true if this change has been successfully made in the target
   */
  private Boolean provisioned;
  
  public ProvisioningObjectChange() {
    super();
  }

  
  /**
   * if there is a specific exception for this one attribute, list it here
   * @return
   */
  public Exception getException() {
    return exception;
  }


  /**
   * if there is a specific exception for this one attribute, list it here
   * @param exception
   */
  public void setException(Exception exception) {
    this.exception = exception;
  }


  /**
   * true if this change has been successfully made in the target
   * @return
   */
  public Boolean getProvisioned() {
    return provisioned;
  }


  /**
   * true if this change has been successfully made in the target
   * @param provisioned
   */
  public void setProvisioned(Boolean provisioned) {
    this.provisioned = provisioned;
  }


  public ProvisioningObjectChange(
      String attributeName, ProvisioningObjectChangeAction provisioningObjectChangeAction,
      Object oldValue, Object newValue) {
    super();
    this.attributeName = attributeName;
    this.provisioningObjectChangeAction = provisioningObjectChangeAction;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }
  
  /**
   * if attribute this is the attribute name
   */
  private String attributeName;
  
  /**
   * if this is an insert, update, or delete
   */
  private ProvisioningObjectChangeAction provisioningObjectChangeAction;
  
  /**
   * previous value if not an insert
   */
  private Object oldValue;
  
  /**
   * new value if not a delete
   */
  private Object newValue;
  
  public String getAttributeName() {
    return attributeName;
  }

  
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  
  public ProvisioningObjectChangeAction getProvisioningObjectChangeAction() {
    return provisioningObjectChangeAction;
  }

  
  public void setProvisioningObjectChangeAction(
      ProvisioningObjectChangeAction provisioningObjectChangeAction) {
    this.provisioningObjectChangeAction = provisioningObjectChangeAction;
  }

  
  public Object getOldValue() {
    return oldValue;
  }

  
  public void setOldValue(Object oldValue) {
    this.oldValue = oldValue;
  }

  
  public Object getNewValue() {
    return newValue;
  }

  
  public void setNewValue(Object newValue) {
    this.newValue = newValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProvisioningObjectChange other = (ProvisioningObjectChange) obj;

    return new EqualsBuilder()
      .append(this.attributeName, other.attributeName)
      .append(this.newValue, other.newValue)
      .append(this.oldValue, other.oldValue)
      .append(this.provisioned, other.provisioned)
      .append(this.provisioningObjectChangeAction, other.provisioningObjectChangeAction)
      .isEquals();
  }

  @Override
  public int hashCode() {
      return new HashCodeBuilder()
              .append( this.attributeName)
              .append( this.newValue)
              .append( this.oldValue)
              .append( this.provisioned)
              .append( this.provisioningObjectChangeAction)
              .toHashCode();
  }

  
}
