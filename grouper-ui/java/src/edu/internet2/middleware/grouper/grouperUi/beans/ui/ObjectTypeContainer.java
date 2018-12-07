package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiGrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
   * list of all configured only grouper object types attribute values for a given group/stem
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
  
  public String getUserFriendlyStringForConfiguredAttributes() {
    
    StringBuilder output = new StringBuilder();
    
    List<String> types = new ArrayList<String>();
    List<String> dataOwners = new ArrayList<String>();
    List<String> memberDescriptions = new ArrayList<String>();
    
    final StringBuilder wikiTextWithLink = new StringBuilder();
    final StringBuilder studentSystemsTextWithLink = new StringBuilder();
    
    for (GuiGrouperObjectTypesAttributeValue guiGrouperObjectTypesAttributeValue: guiConfiguredGrouperObjectTypesAttributeValues) {
      
      final GrouperObjectTypesAttributeValue typesAttributeValue = guiGrouperObjectTypesAttributeValue.getGrouperObjectTypesAttributeValue();
      types.add(typesAttributeValue.getObjectTypeName());
      
      if (StringUtils.isNotBlank(typesAttributeValue.getObjectTypeDataOwner())) {        
        dataOwners.add(typesAttributeValue.getObjectTypeDataOwner());
      }
      
      if (StringUtils.isNotBlank(typesAttributeValue.getObjectTypeMemberDescription())) {        
        memberDescriptions.add(typesAttributeValue.getObjectTypeMemberDescription());
      }
      
      if (typesAttributeValue.getObjectTypeName().equals("app") && !typesAttributeValue.isDirectAssignment()) {
        
         GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                Stem stem = StemFinder.findByUuid(grouperSession, typesAttributeValue.getObjectTypeOwnerStemId(), false);
                if (stem != null && stem.getDisplayExtension().equals("Wiki")) {
                  String link = new GuiStem(stem).getShortLink();
                  String wikiText = TextContainer.retrieveFromRequest().getText().get("objectTypeWikiAppText");
                  wikiText = wikiText.replace("$$folder$$", link);
                  wikiTextWithLink.append(wikiText);
                  wikiTextWithLink.append(" ");
                }
                return null;
              }
            });
        
      }
      
      if (typesAttributeValue.getObjectTypeName().equals("service") && !typesAttributeValue.isDirectAssignment()) {
        
        GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
             
             @Override
             public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
               Stem stem = StemFinder.findByUuid(grouperSession, typesAttributeValue.getObjectTypeOwnerStemId(), false);
               if (stem != null && stem.getDisplayExtension().equals("Student systems")) {
                 String link = new GuiStem(stem).getShortLink();
                 String studentSystemsText = TextContainer.retrieveFromRequest().getText().get("objectTypeStudentSystemsText");
                 studentSystemsText = studentSystemsText.replace("$$folder$$", link);
                 studentSystemsTextWithLink.append(studentSystemsText);
               }
               return null;
             }
          });
       
      }
      
    }
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (types.size() > 0) {
      String typeLabelProperty = guiGroup != null ? "objectTypeGroupTypesLabel": "objectTypeFolderTypesLabel";
      output.append(TextContainer.retrieveFromRequest().getText().get(typeLabelProperty));
      output.append(" ");
      output.append(StringUtils.join(types, ", "));
      output.append(".");
      output.append(" ");
    }
    
    if (dataOwners.size() > 0) {
      output.append(TextContainer.retrieveFromRequest().getText().get("objectTypeDataOwnerLabel"));
      output.append(" ");
      output.append(StringUtils.join(dataOwners, ", "));
      output.append(".");
      output.append(" ");
    }
    
    if (memberDescriptions.size() > 0) {
      output.append(TextContainer.retrieveFromRequest().getText().get("objectTypeMemberDescriptionLabel"));
      output.append(" ");
      output.append(StringUtils.join(memberDescriptions, ", "));
      output.append(".");
      output.append(" ");
    }
    
    output.append(wikiTextWithLink);
    output.append(studentSystemsTextWithLink);
    
    return output.toString();
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
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
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
  
  public static void main(String[] args) {
    List<String> types = Arrays.asList("a", "b");
    String ans = StringUtils.join(types, ", ");
    System.out.println(ans);
  }
  
}
