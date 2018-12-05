package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiGrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ObjectTypeContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2GrouperObjectTypes {
  
  /**
   * view type settings for a group
   * @param request
   * @param response
   */
  public void viewObjectTypesOnGroup(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
     
      if (group == null) {
        return;
      }
      
        
      List stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
            .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true").findStems());
        
      System.out.println(stems.size());
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      List<GrouperObjectTypesAttributeValue> attributeValuesForGroup = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(group);
      
      List<String> typesNotConfigured = new ArrayList<String>(GrouperObjectTypesSettings.getObjectTypeNames());
      
      for (GrouperObjectTypesAttributeValue attributeValue: attributeValuesForGroup) {
        typesNotConfigured.remove(attributeValue.getObjectTypeName());
      }
      
      for (String typeNotConfigured: typesNotConfigured) {
        GrouperObjectTypesAttributeValue notConfiguredAttributeValue = new GrouperObjectTypesAttributeValue();
        notConfiguredAttributeValue.setObjectTypeName(typeNotConfigured);
        attributeValuesForGroup.add(notConfiguredAttributeValue);
      }
      
      objectTypeContainer.setGuiGrouperObjectTypesAttributeValues(GuiGrouperObjectTypesAttributeValue.convertFromGrouperObjectTypesAttributeValues(attributeValuesForGroup));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupSettingsView.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view type settings for a folder
   * @param request
   * @param response
   */
  public void viewObjectTypesOnFolder(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
     
      if (stem == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      List<GrouperObjectTypesAttributeValue> attributeValuesForStem = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stem);
      
      List<String> typesNotConfigured = new ArrayList<String>(GrouperObjectTypesSettings.getObjectTypeNames());
      
      for (GrouperObjectTypesAttributeValue attributeValue: attributeValuesForStem) {
        typesNotConfigured.remove(attributeValue.getObjectTypeName());
      }
      
      for (String typeNotConfigured: typesNotConfigured) {
        GrouperObjectTypesAttributeValue notConfiguredAttributeValue = new GrouperObjectTypesAttributeValue();
        notConfiguredAttributeValue.setObjectTypeName(typeNotConfigured);
        attributeValuesForStem.add(notConfiguredAttributeValue);
      }
      
      // convert from raw to gui
      objectTypeContainer.setGuiGrouperObjectTypesAttributeValues(GuiGrouperObjectTypesAttributeValue.convertFromGrouperObjectTypesAttributeValues(attributeValuesForStem));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesFolderSettingsView.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * edit type settings for a group
   * @param request
   * @param response
   */
  public void editObjectTypesOnGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;

      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      
      String objectTypePreviousName = request.getParameter("grouperObjectTypePreviousTypeName");
      String objectTypeName = request.getParameter("grouperObjectTypeName");
      List<String> dataOwnerRequiringTypeNames = Arrays.asList("ref", "basis", "policy", "bundle", "org");
      
      GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = null;
      
      if (StringUtils.isNotBlank(objectTypeName)) {
        objectTypeContainer.setObjectTypeName(objectTypeName);
        
        grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(group, objectTypeName);
        
        if (dataOwnerRequiringTypeNames.contains(objectTypeName)) {     
          objectTypeContainer.setShowDataOwnerMemberDescription(true);
        }
        if (objectTypeName.equals("app")) {
          List<Stem> serviceStems = GrouperObjectTypesConfiguration.findStemsWhereCurrentUserIsAdminOfService(loggedInSubject);
          objectTypeContainer.setServiceStems(serviceStems);
          objectTypeContainer.setShowServiceName(true);
        }
      }
      
      if (grouperObjectTypesAttributeValue == null) {
        grouperObjectTypesAttributeValue = new GrouperObjectTypesAttributeValue();
      }
      
      if (StringUtils.equals(objectTypeName, objectTypePreviousName)) {
        String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
        if (!StringUtils.isBlank(configurationType)) {
          boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
          grouperObjectTypesAttributeValue.setDirectAssignment(isDirect);
        }
      }
      
      objectTypeContainer.setGrouperObjectTypesAttributeValue(grouperObjectTypesAttributeValue);
            
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!objectTypeContainer.isCanWriteObjectType()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperObjectTypeNotAllowedToWriteGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * edit type settings for a folder
   * @param request
   * @param response
   */
  public void editObjectTypesOnFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      
      String objectTypePreviousName = request.getParameter("grouperObjectTypePreviousTypeName");
      String objectTypeName = request.getParameter("grouperObjectTypeName");
      List<String> dataOwnerRequiringTypeNames = Arrays.asList("ref", "basis", "policy", "bundle", "org");
      
      GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = null;
      
      if (StringUtils.isNotBlank(objectTypeName)) {
        objectTypeContainer.setObjectTypeName(objectTypeName);
        
        grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stem, objectTypeName);
        
        if (dataOwnerRequiringTypeNames.contains(objectTypeName)) {     
          objectTypeContainer.setShowDataOwnerMemberDescription(true);
        }
        if (objectTypeName.equals("app")) {
          //TODO run the following logic as admin so the attributes work.
          List<Stem> serviceStems = GrouperObjectTypesConfiguration.findStemsWhereCurrentUserIsAdminOfService(loggedInSubject);
          objectTypeContainer.setServiceStems(serviceStems);
          objectTypeContainer.setShowServiceName(true);
        }
      }
      
      if (grouperObjectTypesAttributeValue == null) {
        grouperObjectTypesAttributeValue = new GrouperObjectTypesAttributeValue();
      }
      
      if (StringUtils.equals(objectTypeName, objectTypePreviousName)) {
        String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
        if (!StringUtils.isBlank(configurationType)) {
          boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
          grouperObjectTypesAttributeValue.setDirectAssignment(isDirect);
        }
      }
      
      objectTypeContainer.setGrouperObjectTypesAttributeValue(grouperObjectTypesAttributeValue);
            
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!objectTypeContainer.isCanWriteObjectType()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("grouperObjectTypeNotAllowedToWriteStem")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesFolderSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * save changes to type settings for a group
   * @param request
   * @param response
   */
  public void editObjectTypesOnGroupSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
      String objectTypeServiceName = request.getParameter("grouperObjectTypeServiceName");
      String objectTypeDataOwner = request.getParameter("grouperObjectTypeDataOwner");
      String objectTypeMemberDescription = request.getParameter("grouperObjectTypeMemberDescription");
      
      boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      //validate()
      
      GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
      attributeValue.setDirectAssignment(isDirect);
      attributeValue.setObjectTypeDataOwner(objectTypeDataOwner);
      attributeValue.setObjectTypeMemberDescription(objectTypeMemberDescription);
      attributeValue.setObjectTypeName(objectTypeName);
      attributeValue.setObjectTypeServiceName(objectTypeServiceName);
      
      if (isDirect) {
        GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, group); 
      } else {
        GrouperObjectTypesConfiguration.copyConfigFromParent(group, objectTypeName);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save changes to type settings for a folder
   * @param request
   * @param response
   */
  public void editObjectTypesOnFolderSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
      String objectTypeServiceName = request.getParameter("grouperObjectTypeServiceName");
      String objectTypeDataOwner = request.getParameter("grouperObjectTypeDataOwner");
      String objectTypeMemberDescription = request.getParameter("grouperObjectTypeMemberDescription");
      
      boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      //validate()
      
      GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
      attributeValue.setDirectAssignment(isDirect);
      attributeValue.setObjectTypeDataOwner(objectTypeDataOwner);
      attributeValue.setObjectTypeMemberDescription(objectTypeMemberDescription);
      attributeValue.setObjectTypeName(objectTypeName);
      attributeValue.setObjectTypeServiceName(objectTypeServiceName);
      if (isDirect) {        
        GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, stem);
      } else {
        GrouperObjectTypesConfiguration.copyConfigFromParent(stem, objectTypeName);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
}
