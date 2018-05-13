package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Map;
import java.util.TreeMap;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;

/**
 * configuration on an object
 */
public class GrouperDeprovisioningConfiguration {

  /**
   * base existing attribute assign for this configuration
   */
  private AttributeAssign attributeAssignBase = null;
  
  /**
   * base existing attribute assign for this configuration
   * @return the attribute assign
   */
  public AttributeAssign getAttributeAssignBase() {
    return this.attributeAssignBase;
  }

  /**
   * base existing attribute assign for this configuration
   * @param attributeAssignBase1
   */
  public void setAttributeAssignBase(AttributeAssign attributeAssignBase1) {
    this.attributeAssignBase = attributeAssignBase1;
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
