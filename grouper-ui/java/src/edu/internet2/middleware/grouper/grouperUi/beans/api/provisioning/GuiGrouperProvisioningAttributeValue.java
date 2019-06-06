package edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
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

  /**
   * externalized name or the key
   * @return the name
   */
  public String getExternalizedName() {

    String externalizedName = TextContainer.textOrNull("provisioningUiLabelForKey_" + this.getTargetKey());
    externalizedName = StringUtils.defaultIfEmpty(externalizedName, this.getTargetKey());
    return externalizedName;

  }

  /**
   * get target key for current target
   * @return target key
   */
  public String getTargetKey() {
    return GrouperProvisioningSettings.getTargets().get(this.grouperProvisioningAttributeValue.getTargetName()).getKey();
  }
  
  public static List<GuiGrouperProvisioningAttributeValue> convertFromGrouperProvisioningAttributeValues(List<GrouperProvisioningAttributeValue> attributeValues) {
    
    List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = new ArrayList<GuiGrouperProvisioningAttributeValue>();
    
    for (GrouperProvisioningAttributeValue singleAttributeValue: attributeValues) {
      guiGrouperProvisioningAttributeValues.add(new GuiGrouperProvisioningAttributeValue(singleAttributeValue));
    }
    
    return guiGrouperProvisioningAttributeValues;
    
  }

}
