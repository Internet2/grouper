package edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GuiGrouperProvisioningAttributeValue {
  
  public GuiGrouperProvisioningAttributeValue(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue) {
    this.grouperProvisioningAttributeValue = grouperProvisioningAttributeValue;
  }
  
  private GrouperProvisioner grouperProvisioner;
  
  public void setGrouperProvisioner(GrouperProvisioner provisioner) {
    grouperProvisioner = provisioner;
    
  }

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  private GrouperProvisioningAttributeValue grouperProvisioningAttributeValue;
  
  private Timestamp lastTimeWorkWasDone;
  
  private boolean inTarget;
  
  private boolean provisionable;
  
  private boolean hasDirectSettings;
  
  private boolean canAssignProvisioning;
  
  private List<GrouperProvisioningObjectMetadataItem> metadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();
  
  private Map<String, Object> metadataNameValuesExternalized = new HashMap<>();

  private boolean parentWillMakeThisProvisionable;
  
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
  
  public static List<GuiGrouperProvisioningAttributeValue> convertFromGrouperProvisioningAttributeValues(List<GrouperProvisioningAttributeValue> attributeValues, GrouperObject grouperObject) {
    
    List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = new ArrayList<GuiGrouperProvisioningAttributeValue>();
    
    for (GrouperProvisioningAttributeValue singleAttributeValue: attributeValues) {
      GuiGrouperProvisioningAttributeValue guiGrouperProvisioningAttributeValue = new GuiGrouperProvisioningAttributeValue(singleAttributeValue);
      guiGrouperProvisioningAttributeValue.setProvisionable(singleAttributeValue.getDoProvision() != null);
      
      GrouperProvisioningAttributeValue parentProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(grouperObject, singleAttributeValue.getTargetName(), true);
      if (parentProvisioningAttributeValue != null) {
        guiGrouperProvisioningAttributeValue.setParentWillMakeThisProvisionable(true);
      }
      
      Map<String, Object> metadataNameValues = singleAttributeValue.getMetadataNameValues();
      for (String metadataName: metadataNameValues.keySet()) {
        //md_anotherMetadata_myTeamDynamixProvisioner_label
        
        if (metadataName.equals("md_grouper_allowPolicyGroupOverride")) {
          String labelOrNull = GrouperTextContainer.textOrNull("grouperProvisioningObjectMetadataProvisionOnlyPolicyGroupsLabel");
          String stringValue = GrouperUtil.stringValue(metadataNameValues.get(metadataName));
          stringValue = GrouperUtil.defaultIfBlank(stringValue, "");
          stringValue = GrouperUtil.xmlEscape(stringValue);
          guiGrouperProvisioningAttributeValue.metadataNameValuesExternalized.put(GrouperUtil.defaultString(labelOrNull, metadataName), stringValue);
          continue;
        } else if (metadataName.equals("md_grouper_allowProvisionableRegexOverride")) {
          String labelOrNull = GrouperTextContainer.textOrNull("grouperProvisioningObjectMetadataProvisionableRegexLabel");
          String stringValue = GrouperUtil.stringValue(metadataNameValues.get(metadataName));
          stringValue = GrouperUtil.defaultIfBlank(stringValue, "");
          stringValue = GrouperUtil.xmlEscape(stringValue);
          guiGrouperProvisioningAttributeValue.metadataNameValuesExternalized.put(GrouperUtil.defaultString(labelOrNull, metadataName), stringValue);
          continue;
        }
        
        String labelOrNull = GrouperTextContainer.textOrNull(metadataName+"_"+singleAttributeValue.getTargetName()+"_label");
        String stringValue = GrouperUtil.stringValue(metadataNameValues.get(metadataName));
        stringValue = GrouperUtil.defaultIfBlank(stringValue, "");
        stringValue = GrouperUtil.xmlEscape(stringValue);
        guiGrouperProvisioningAttributeValue.metadataNameValuesExternalized.put(GrouperUtil.defaultString(labelOrNull, metadataName), stringValue);
      }
      
      guiGrouperProvisioningAttributeValues.add(guiGrouperProvisioningAttributeValue);
    }
    
    return guiGrouperProvisioningAttributeValues;
    
  }
  
  
  private void setParentWillMakeThisProvisionable(boolean parentWillMakeThisProvisionable) {
    this.parentWillMakeThisProvisionable = parentWillMakeThisProvisionable;
  }

  
  public boolean isParentWillMakeThisProvisionable() {
    return parentWillMakeThisProvisionable;
  }


  public Map<String, Object> getMetadataNameValuesExternalized() {
    return metadataNameValuesExternalized;
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

  public boolean isCanAssignProvisioning() {
    return canAssignProvisioning;
  }

  
  public void setCanAssignProvisioning(boolean canAssignProvisioning) {
    this.canAssignProvisioning = canAssignProvisioning;
  }
  
}
