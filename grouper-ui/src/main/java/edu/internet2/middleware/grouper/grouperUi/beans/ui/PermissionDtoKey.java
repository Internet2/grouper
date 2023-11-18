package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;

public class PermissionDtoKey {

  public AttributeDef permissionDef;
  public AttributeDefName permissionDefName;
  
  public PermissionDtoKey() {}
  
  public PermissionDtoKey(AttributeDef permissionDef, AttributeDefName permissionDefName) {
    this.permissionDef = permissionDef;
    this.permissionDefName = permissionDefName;
  }
  
  public AttributeDef getPermissionDef() {
    return permissionDef;
  }
  
  public void setPermissionDef(AttributeDef permissionDef) {
    this.permissionDef = permissionDef;
  }
  
  public AttributeDefName getPermissionDefName() {
    return permissionDefName;
  }
  
  public void setPermissionDefName(AttributeDefName permissionDefName) {
    this.permissionDefName = permissionDefName;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.permissionDef)
        .append(this.permissionDefName)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PermissionDtoKey permissionDtoKey = (PermissionDtoKey) obj;
   
    return new EqualsBuilder()
        .append(permissionDef, permissionDtoKey.getPermissionDef())
        .append(permissionDefName, permissionDtoKey.getPermissionDefName())
        .isEquals();
    
  }
  
}
