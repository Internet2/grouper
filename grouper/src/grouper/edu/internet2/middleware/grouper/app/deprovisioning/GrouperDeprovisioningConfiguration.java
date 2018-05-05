package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.GrouperObject;

/**
 * configuration on an object
 */
public class GrouperDeprovisioningConfiguration {

  /**
   * Group or stem with configuration
   */
  private GrouperObject originalOwner;
  
  /**
   * @return the originalOwner
   */
  public GrouperObject getOriginalOwner() {
    return this.originalOwner;
  }

  
  /**
   * @param originalOwner1 the originalOwner to set
   */
  public void setOriginalOwner(GrouperObject originalOwner1) {
    this.originalOwner = originalOwner1;
  }

  /**
   * Stem that is the inherited owner
   */
  private Stem inheritedOwner;

  
  /**
   * @return the inheritedOwner
   */
  public Stem getInheritedOwner() {
    return this.inheritedOwner;
  }

  
  /**
   * @param inheritedOwner1 the inheritedOwner to set
   */
  public void setInheritedOwner(Stem inheritedOwner1) {
    this.inheritedOwner = inheritedOwner1;
  }

  /**
   * original config in the database
   */
  private GrouperDeprovisioningAttributeValue originalConfig;
  
  
  /**
   * @return the originalConfig
   */
  public GrouperDeprovisioningAttributeValue getOriginalConfig() {
    return this.originalConfig;
  }

  
  /**
   * @param originalConfig1 the originalConfig to set
   */
  public void setOriginalConfig(GrouperDeprovisioningAttributeValue originalConfig1) {
    this.originalConfig = originalConfig1;
  }

  /**
   * new config after calculations
   */
  private GrouperDeprovisioningAttributeValue newConfig;

  
  
  /**
   * new config after calculations
   * @return the newConfig
   */
  public GrouperDeprovisioningAttributeValue getNewConfig() {
    return this.newConfig;
  }

  
  /**
   * new config after calculations
   * @param newConfig1 the newConfig to set
   */
  public void setNewConfig(GrouperDeprovisioningAttributeValue newConfig1) {
    this.newConfig = newConfig1;
  }
  
}
