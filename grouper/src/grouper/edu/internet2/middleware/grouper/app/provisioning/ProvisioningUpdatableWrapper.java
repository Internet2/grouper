package edu.internet2.middleware.grouper.app.provisioning;

/**
 * 
 * @author mchyzer
 */
public abstract class ProvisioningUpdatableWrapper {

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
