package edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

public class GuiGrouperProvisioningAttributeValue {
  
  public GuiGrouperProvisioningAttributeValue(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue) {
    this.grouperProvisioningAttributeValue = grouperProvisioningAttributeValue;
  }
  
  private GrouperProvisioningAttributeValue grouperProvisioningAttributeValue;
  
  private Timestamp lastTimeWorkWasDone;
  
  private boolean inTarget;
  
  private boolean provisionable;
  
  private boolean hasDirectSettings;
  
  private List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
  
  public Timestamp getLastTimeWorkWasDone() {
    return lastTimeWorkWasDone;
  }

  public boolean isInTarget() {
    return inTarget;
  }
  
  
  public void setLastTimeWorkWasDone(Timestamp lastTimeWorkWasDone) {
    this.lastTimeWorkWasDone = lastTimeWorkWasDone;
  }

  
  public void setInTarget(boolean inTarget) {
    this.inTarget = inTarget;
  }
  
  
  public boolean isProvisionable() {
    return provisionable;
  }

  
  public void setProvisionable(boolean provisionable) {
    this.provisionable = provisionable;
  }

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
    return GrouperProvisioningSettings.getTargets(true).get(this.grouperProvisioningAttributeValue.getTargetName()).getKey();
  }
  
  public static List<GuiGrouperProvisioningAttributeValue> convertFromGrouperProvisioningAttributeValues(List<GrouperProvisioningAttributeValue> attributeValues) {
    
    List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = new ArrayList<GuiGrouperProvisioningAttributeValue>();
    
    for (GrouperProvisioningAttributeValue singleAttributeValue: attributeValues) {
      GuiGrouperProvisioningAttributeValue guiGrouperProvisioningAttributeValue = new GuiGrouperProvisioningAttributeValue(singleAttributeValue);
      guiGrouperProvisioningAttributeValue.setProvisionable(singleAttributeValue.getDoProvision() != null);
      guiGrouperProvisioningAttributeValues.add(guiGrouperProvisioningAttributeValue);
    }
    
    return guiGrouperProvisioningAttributeValues;
    
  }

  
  public List<GrouperProvisioningObjectMetadataItem> getMetadataItems() {
    return metadataItems;
  }

  
  public void setMetadataItems(List<GrouperProvisioningObjectMetadataItem> metadataItems) {
    this.metadataItems = metadataItems;
  }

  
  public boolean isHasDirectSettings() {
    return hasDirectSettings;
  }

  
  public void setHasDirectSettings(boolean hasDirectSettings) {
    this.hasDirectSettings = hasDirectSettings;
  }
  
}
