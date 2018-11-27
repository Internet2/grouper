package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiGrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class ObjectTypeContainer {
  
  /**
   * object type name user is currently working on
   */
  private String objectTypeName;
  
  /**
   * attribute value for given group/stem and type
   */
  private GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue;
  
  /**
   * list of all grouper object types attribute values for a given group/stem
   */
  private List<GuiGrouperObjectTypesAttributeValue> guiGrouperObjectTypesAttributeValues = new ArrayList<GuiGrouperObjectTypesAttributeValue>();
  
  /**
   * list of all configured onnly grouper object types attribute values for a given group/stem
   */
  private List<GuiGrouperObjectTypesAttributeValue> guiConfiguredGrouperObjectTypesAttributeValues = new ArrayList<GuiGrouperObjectTypesAttributeValue>();
  
  /**
   * show data owner and member description field?
   */
  private boolean showDataOwnerMemberDescription;
  
  /**
   * show service name?
   */
  private boolean showServiceName;
  
  /**
   * list of service stems
   */
  private List<Stem> serviceStems = new ArrayList<Stem>();
  
  /**
   * object type name user is currently working on
   * @return
   */
  public String getObjectTypeName() {
    return objectTypeName;
  }

  /**
   * object type name user is currently working on
   * @param objectTypeName
   */
  public void setObjectTypeName(String objectTypeName) {
    this.objectTypeName = objectTypeName;
  }
  
  /**
   * show data owner and member description field?
   * @return
   */
  public boolean isShowDataOwnerMemberDescription() {
    return showDataOwnerMemberDescription;
  }

  /**
   * show data owner and member description field?
   * @param showDataOwnerMemberDescription
   */
  public void setShowDataOwnerMemberDescription(boolean showDataOwnerMemberDescription) {
    this.showDataOwnerMemberDescription = showDataOwnerMemberDescription;
  }
  
  /**
   * show service name?
   * @return
   */
  public boolean isShowServiceName() {
    return showServiceName;
  }

  /**
   * show service name?
   * @param showServiceName
   */
  public void setShowServiceName(boolean showServiceName) {
    this.showServiceName = showServiceName;
  }
  
  /**
   * attribute value for given group/stem and type
   * @return
   */
  public GrouperObjectTypesAttributeValue getGrouperObjectTypesAttributeValue() {
    return grouperObjectTypesAttributeValue;
  }

  /**
   * attribute value for given group/stem and type
   * @param grouperObjectTypesAttributeValue
   */
  public void setGrouperObjectTypesAttributeValue(GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue) {
    this.grouperObjectTypesAttributeValue = grouperObjectTypesAttributeValue;
  }
  
  /**
   * list of all grouper object types attribute values for a given group/stem
   * @return
   */
  public List<GuiGrouperObjectTypesAttributeValue> getGuiGrouperObjectTypesAttributeValues() {
    return guiGrouperObjectTypesAttributeValues;
  }

  /**
   * list of all grouper object types attribute values for a given group/stem
   * @param guiGrouperObjectTypesAttributeValues
   */
  public void setGuiGrouperObjectTypesAttributeValues(List<GuiGrouperObjectTypesAttributeValue> guiGrouperObjectTypesAttributeValues) {
    this.guiGrouperObjectTypesAttributeValues = guiGrouperObjectTypesAttributeValues;
  }
  
  /**
   * list of all configured onnly grouper object types attribute values for a given group/stem
   * @return
   */
  public List<GuiGrouperObjectTypesAttributeValue> getGuiConfiguredGrouperObjectTypesAttributeValues() {
    return guiConfiguredGrouperObjectTypesAttributeValues;
  }

  /**
   * list of all configured onnly grouper object types attribute values for a given group/stem
   * @param guiConfiguredGrouperObjectTypesAttributeValues
   */
  public void setGuiConfiguredGrouperObjectTypesAttributeValues(
      List<GuiGrouperObjectTypesAttributeValue> guiConfiguredGrouperObjectTypesAttributeValues) {
    this.guiConfiguredGrouperObjectTypesAttributeValues = guiConfiguredGrouperObjectTypesAttributeValues;
  }
  
  public List<String> getObjectTypeNames() {
    return GrouperObjectTypesSettings.getObjectTypeNames();
  }
  
  /**
   * service stems
   * @return
   */
  public List<GuiStem> getServiceStems() {
    List<GuiStem> guiServiceStems = new ArrayList<GuiStem>();
    
    for (Stem stem: serviceStems) {
      guiServiceStems.add(new GuiStem(stem));
    }
    
    return guiServiceStems;
  }

  /**
   * service stems
   * @param serviceStems
   */
  public void setServiceStems(List<Stem> serviceStems) {
    this.serviceStems = serviceStems;
  }

  /**
   * 
   * @return true if can read
   */
  public boolean isCanReadObjectType() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteObjectType() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanUpdate()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * if this object or any parent object has type settings configured
   * @return if there is type
   */
  public boolean isHasObjectTypeOnThisObjectOrParent() {
    
    for (GuiGrouperObjectTypesAttributeValue attributeValue: guiGrouperObjectTypesAttributeValues) {
      if (attributeValue.getGrouperObjectTypesAttributeValue().isDirectAssignment() || StringUtils.isNotBlank(attributeValue.getGrouperObjectTypesAttributeValue().getObjectTypeOwnerStemId())) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if can run daemon
   */
  public boolean isCanRunDaemon() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    return false;
  }
  
}
