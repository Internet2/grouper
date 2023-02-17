package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;

/**
 * 
 * @author mchyzer
 */
public abstract class ProvisioningUpdatableWrapper {


  public ProvisioningStateBase getProvisioningState() {
    if (this instanceof ProvisioningGroupWrapper) {
      return ((ProvisioningGroupWrapper)this).getProvisioningStateGroup();
    }
    if (this instanceof ProvisioningEntityWrapper) {
      return ((ProvisioningEntityWrapper)this).getProvisioningStateEntity();
    }
    if (this instanceof ProvisioningMembershipWrapper) {
      return ((ProvisioningMembershipWrapper)this).getProvisioningStateMembership();
    }
    throw new RuntimeException("Not expecting type: " + this);
  }
  
  /**
   * if this object should not be provisioned because there is an error, list it here
   */
  private GcGrouperSyncErrorCode errorCode;
  
  /**
   * if this object should not be provisioned because there is an error, list it here
   * @return
   */
  public GcGrouperSyncErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * if this object should not be provisioned because there is an error, list it here
   * @param errorCode
   */
  public void setErrorCode(GcGrouperSyncErrorCode errorCode) {
    this.errorCode = errorCode;
  }


  private GrouperProvisioner grouperProvisioner;
  
  
  
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }



  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  /**
   * 
   */
  public ProvisioningUpdatableWrapper() {
  }

  /**
   * get the object type name, e.g. group, entity, membership
   * @return the object type name
   */
  public abstract String objectTypeName();

  /**
   * 
   * @return
   */
  public abstract String toStringForError();
}
