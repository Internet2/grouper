package edu.internet2.middleware.grouper.app.duo.role;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeManipulation;


public class DuoRoleProvisioningAttributeManipulation extends GrouperProvisioningAttributeManipulation {

  public DuoRoleProvisioningAttributeManipulation() {
  }

  @Override
  public boolean isConvertNullValuesToEmpty() {
    return true;
  }

  
}
