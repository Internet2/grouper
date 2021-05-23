package edu.internet2.middleware.grouper.app.duo;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeManipulation;


public class DuoProvisioningAttributeManipulation
    extends GrouperProvisioningAttributeManipulation {

  public DuoProvisioningAttributeManipulation() {
  }

  @Override
  public boolean isConvertNullValuesToEmpty() {
    return true;
  }

  
}
