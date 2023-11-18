/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api.deprovisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;


/**
 *
 */
public class GuiGrouperDeprovisioningAttributeValue {

  /**
   * grouper deprovisioning attribute value
   */
  private GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue;
  
  /**
   * 
   */
  public GuiGrouperDeprovisioningAttributeValue() {
  }
  
  /**
   * @param grouperDeprovisioningAttributeValue2
   */
  public GuiGrouperDeprovisioningAttributeValue(GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue2) {
    this.grouperDeprovisioningAttributeValue = grouperDeprovisioningAttributeValue2;
  }
  
  /**
   * @return the grouperDeprovisioningAttributeValue
   */
  public GrouperDeprovisioningAttributeValue getGrouperDeprovisioningAttributeValue() {
    return this.grouperDeprovisioningAttributeValue;
  }
  
  /**
   * @param grouperDeprovisioningAttributeValue1 the grouperDeprovisioningAttributeValue to set
   */
  public void setGrouperDeprovisioningAttributeValue(
      GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue1) {
    this.grouperDeprovisioningAttributeValue = grouperDeprovisioningAttributeValue1;
  }
  
  /**
   * return the gui folder with settings
   * @return gui stem
   */
  public GuiStem getGuiFolderWithSettings() {
    if (this.grouperDeprovisioningAttributeValue == null) {
      return null;
    }
    
    Stem stem = this.grouperDeprovisioningAttributeValue.getInheritedFromFolder();
    
    if (stem == null) {
      return null;
    }
    return new GuiStem(stem);
  }

  
}
