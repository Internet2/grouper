package edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

public class GuiGrouperProvisioningAttributeValue {
  
  private GuiGrouperProvisioningAttributeValue(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue) {
    this.grouperProvisioningAttributeValue = grouperProvisioningAttributeValue;
  }
  
  private GrouperProvisioningAttributeValue grouperProvisioningAttributeValue;

  public GrouperProvisioningAttributeValue getGrouperProvisioningAttributeValue() {
    return grouperProvisioningAttributeValue;
  }
  
  /**
   * return the gui folder with settings
   * @return gui stem
   */
  public GuiStem getGuiFolderWithSettings() {
    if (this.grouperProvisioningAttributeValue == null) {
      return null;
    }
    
    String stemId = this.grouperProvisioningAttributeValue.getOwnerStemId();
    Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, false);
    
    if (stem == null) {
      return null;
    }
    
    return new GuiStem(stem);
  }

  
  public static List<GuiGrouperProvisioningAttributeValue> convertFromGrouperProvisioningAttributeValues(List<GrouperProvisioningAttributeValue> attributeValues) {
    
    List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = new ArrayList<GuiGrouperProvisioningAttributeValue>();
    
    for (GrouperProvisioningAttributeValue singleAttributeValue: attributeValues) {
      guiGrouperProvisioningAttributeValues.add(new GuiGrouperProvisioningAttributeValue(singleAttributeValue));
    }
    
    return guiGrouperProvisioningAttributeValues;
    
  }

}
