package edu.internet2.middleware.grouper.app.provisioning;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;

/**
 * 
 * @author mchyzer
 */
public abstract class ProvisioningUpdatableWrapper {
  
  /**
   * after an action happens, process result only once. Do not process twice during inserts and then during sending changes to target.
   */
  private boolean resultProcessed;
  
  
  public boolean isResultProcessed() {
    return resultProcessed;
  }
  
  public void setResultProcessed(boolean resultProcessed) {
    this.resultProcessed = resultProcessed;
  }


  /**
   * if incremental and recalc, this applies to the entity / group / membership, but not the groupMemberships (for that provisioning) or entityMemberships (for that provisioning)
   */
  private boolean recalcObject;
  
  /**
   * if incremental and recalc, this applies to the entity / group / membership, but not the groupMemberships (for that provisioning) or entityMemberships (for that provisioning)
   * @return
   */
  public boolean isRecalcObject() {
    return recalcObject;
  }

  /**
   * if incremental and recalc, this applies to the entity / group / membership, but not the groupMemberships (for that provisioning) or entityMemberships (for that provisioning)
   * @param recalc
   */
  public void setRecalcObject(boolean recalc) {
    this.recalcObject = recalc;
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
