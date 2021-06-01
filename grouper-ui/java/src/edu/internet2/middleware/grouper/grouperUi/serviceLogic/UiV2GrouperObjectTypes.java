package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration.findStemsWhereCurrentUserIsAdminOfService;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.getDataOwnerMemberDescriptionRequiringObjectTypeNames;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.getServiceRequiringObjectTypeNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeGroupFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeGroupSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeStemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GdgTypeStemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesJob;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.app.grouperTypes.StemOrGroupObjectType;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiGrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiStemObjectType;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ObjectTypeContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2GrouperObjectTypes {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2GrouperObjectTypes.class);
  
  
  /**
   * make sure attribute def is there and enabled etc
   * @return true if k
   */
  private boolean checkObjectTypes() {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (!GrouperObjectTypesSettings.objectTypesEnabled()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("objectTypeNotEnabledError")));
      return false;
    }

    AttributeDef attributeDefBase = null;
    try {
      
      attributeDefBase = GrouperObjectTypesAttributeNames.retrieveAttributeDefBaseDef();

    } catch (RuntimeException e) {
      if (attributeDefBase == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("objectTypeAttributeNotFoundError")));
        return false;
      }
      throw e;
    }
    
    return true;
  }
  
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
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return null;
          }
          
          Set<GrouperObjectTypesAttributeValue> gdgTypeGroupAssignments = new GdgTypeGroupFinder().assignGroup(GROUP).findGdgTypeGroupAssignments();
          
          List<GrouperObjectTypesAttributeValue> attributeValuesForGroup = new ArrayList<GrouperObjectTypesAttributeValue>(gdgTypeGroupAssignments);
          
          objectTypeContainer.setGuiGrouperObjectTypesAttributeValues(GuiGrouperObjectTypesAttributeValue.convertFromGrouperObjectTypesAttributeValues(attributeValuesForGroup));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesGroupSettingsView.jsp"));
          
          return null;
        }
      });
      
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
      
      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return null;
          }
            
          Set<GrouperObjectTypesAttributeValue> gdgTypeStemAssignments = new GdgTypeStemFinder().assignStem(STEM).findGdgTypeStemAssignments();
          
          List<GrouperObjectTypesAttributeValue> attributeValuesForStem = new ArrayList<GrouperObjectTypesAttributeValue>(gdgTypeStemAssignments);
          
          // convert from raw to gui
          objectTypeContainer.setGuiGrouperObjectTypesAttributeValues(GuiGrouperObjectTypesAttributeValue.convertFromGrouperObjectTypesAttributeValues(attributeValuesForStem));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesFolderSettingsView.jsp"));
          
          return null;
        }
      });
      
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
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      String objectTypePreviousName = request.getParameter("grouperObjectTypePreviousTypeName");
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      //switch over to admin so attributes work
      GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = (GrouperObjectTypesAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(objectTypeName)) {
            objectTypeContainer.setObjectTypeName(objectTypeName);
            
            if (getDataOwnerMemberDescriptionRequiringObjectTypeNames().contains(objectTypeName)) {     
              objectTypeContainer.setShowDataOwnerMemberDescription(true);
            }
            
            if (getServiceRequiringObjectTypeNames().contains(objectTypeName)) {
              List<Stem> serviceStems = findStemsWhereCurrentUserIsAdminOfService(loggedInSubject);
              objectTypeContainer.setServiceStems(serviceStems);
              objectTypeContainer.setShowServiceName(true);
            }
            
            return new GdgTypeGroupFinder().assignGroup(GROUP).assignType(objectTypeName).findGdgTypeGroupAssignment();
          }
          
          return null;
        }
      });
      
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
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      final Stem STEM = stem;
      String objectTypePreviousName = request.getParameter("grouperObjectTypePreviousTypeName");
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      //switch over to admin so attributes work
      GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = (GrouperObjectTypesAttributeValue)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (StringUtils.isNotBlank(objectTypeName)) {
            objectTypeContainer.setObjectTypeName(objectTypeName);
            
            if (getDataOwnerMemberDescriptionRequiringObjectTypeNames().contains(objectTypeName)) {     
              objectTypeContainer.setShowDataOwnerMemberDescription(true);
            }
            
            if (getServiceRequiringObjectTypeNames().contains(objectTypeName)) {
              List<Stem> serviceStems = findStemsWhereCurrentUserIsAdminOfService(loggedInSubject);
              objectTypeContainer.setServiceStems(serviceStems);
              objectTypeContainer.setShowServiceName(true);
            }
            return new GdgTypeStemFinder().assignStem(STEM).assignType(objectTypeName).findGdgTypeStemAssignment();
          }
          
          return null;
        }
      });
      
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
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      boolean shouldContinue = checkObjectTypes();
      
      if (!shouldContinue) {
        return;
      }
      
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      final Group GROUP = group;
      String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
      String objectTypeServiceName = request.getParameter("grouperObjectTypeServiceName");
      String objectTypeDataOwner = request.getParameter("grouperObjectTypeDataOwner");
      String objectTypeMemberDescription = request.getParameter("grouperObjectTypeMemberDescription");
      
      final boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      if (StringUtils.isBlank(objectTypeName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperObjectTypeNameId",
            TextContainer.retrieveFromRequest().getText().get("objectTypeTypeNameRequired")));
        return;
      }
      
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave();
      gdgTypeGroupSave.assignDataOwner(objectTypeDataOwner).assignMemberDescription(objectTypeMemberDescription)
        .assignServiceName(objectTypeServiceName)
        .assignRunAsRoot(true)
        .assignSaveMode(isDirect ? SaveMode.INSERT_OR_UPDATE : SaveMode.DELETE)
        .assignReplaceAllSettings(true).assignGroup(GROUP).assignType(objectTypeName)
        .save();
        
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
      
      boolean shouldContinue = checkObjectTypes();
      
      if (!shouldContinue) {
        return;
      }
      
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      String configurationType = request.getParameter("grouperObjectTypeHasConfigurationName");
      String objectTypeServiceName = request.getParameter("grouperObjectTypeServiceName");
      String objectTypeDataOwner = request.getParameter("grouperObjectTypeDataOwner");
      String objectTypeMemberDescription = request.getParameter("grouperObjectTypeMemberDescription");
      
      final Stem STEM = stem;
      final boolean isDirect = GrouperUtil.booleanValue(configurationType, false);
      
      if (StringUtils.isBlank(objectTypeName)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperObjectTypeNameId",
            TextContainer.retrieveFromRequest().getText().get("objectTypeTypeNameRequired")));
        return;
      }

      GdgTypeStemSave gdgTypeStemSave = new GdgTypeStemSave();
      gdgTypeStemSave.assignDataOwner(objectTypeDataOwner).assignMemberDescription(objectTypeMemberDescription)
        .assignServiceName(objectTypeServiceName)
        .assignRunAsRoot(true)
        .assignSaveMode(isDirect ? SaveMode.INSERT_OR_UPDATE : SaveMode.DELETE)
        .assignReplaceAllSettings(true).assignStem(STEM).assignType(objectTypeName)
        .save();
          
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * remove type configuration from folder
   * @param request
   * @param response
   */
  public void removeObjectTypeFromFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      if (StringUtils.isBlank(objectTypeName)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("objectTypeTypeNameRequired")));
        return;
      }
      
      final Stem STEM = stem;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          new GdgTypeStemSave().assignSaveMode(SaveMode.DELETE).assignStem(STEM).assignType(objectTypeName).save();
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * remove type configuration from group
   * @param request
   * @param response
   */
  public void removeObjectTypeFromGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      final String objectTypeName = request.getParameter("grouperObjectTypeName");
      
      if (StringUtils.isBlank(objectTypeName)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("objectTypeTypeNameRequired")));
        return;
      }
      
      final Group GROUP = group;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          new GdgTypeGroupSave().assignSaveMode(SaveMode.DELETE).assignGroup(GROUP).assignType(objectTypeName).save();
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * retrieve folders that are candidates for auto assigning types
   * @param request
   * @param response
   */
  public void findAutoAssignTypes(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      final Stem STEM = stem;
            
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
      
          List<StemOrGroupObjectType> objectTypes = GrouperObjectTypesConfiguration.getAutoAssignTypeCandidates(STEM, loggedInSubject);
          int totalSize = objectTypes.size();
          
          int maximumItemsToshow = GrouperConfig.retrieveConfig().propertyValueInt("objectTypes.max.autoAssign.objectCount", 2000);
          
          List<GuiStemObjectType> guiStemObjectTypes = new ArrayList<GuiStemObjectType>();
          if (totalSize > maximumItemsToshow) {
            guiStemObjectTypes.addAll(GuiStemObjectType.convertFromStemObjectType(objectTypes.subList(0, maximumItemsToshow)));
          } else {
            guiStemObjectTypes.addAll(GuiStemObjectType.convertFromStemObjectType(objectTypes));
          }
          
          objectTypeContainer.setGuiStemObjectTypes(guiStemObjectTypes);
          objectTypeContainer.setMaxAutoAssignSize(maximumItemsToshow);
          objectTypeContainer.setTotalAutoAssignSize(totalSize);
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/grouperObjectTypes/grouperObjectTypesFolderAutoAssign.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  
  /**
   * assign bulk object types to selected folders 
   */
  public void objectTypeAutoAssignFolderSave(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkObjectTypes()) {
            return false;
          }
          
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
      
      final String[] stemObjectTypes = request.getParameterValues("stemObjectType[]");
      
      final Map<GrouperObject, GrouperObjectTypesAttributeValue> attributeValues = new HashMap<GrouperObject, GrouperObjectTypesAttributeValue>();
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          for (String stemObjectType: stemObjectTypes) {
            
            String[] stemObjectTypeArray = stemObjectType.split("_");
            String stemOrGroup = stemObjectTypeArray[2];
            
            String stemObjectTypeWithNoStemGroupSuffix = stemObjectTypeArray[0] + "_" + stemObjectTypeArray[1];
            
            GrouperObject grouperObject = null;
            
            if (stemOrGroup.equals("stem")) {
              String stemId = stemObjectTypeArray[0];
              
              Stem stemToAssignType = StemFinder.findByUuid(theGrouperSession, stemId, true);
              
              if (!stemToAssignType.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
                throw new RuntimeException(GrouperUtil.subjectToString(loggedInSubject) + " cannot ADMIN folder: " + stemToAssignType.getName());
              }
              grouperObject = stemToAssignType;
            } else if (stemOrGroup.equals("group")) {
              
              String groupId = stemObjectTypeArray[0];
              
              Group groupToAssignType = GroupFinder.findByUuid(theGrouperSession, groupId, true);
              
              if (!groupToAssignType.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
                throw new RuntimeException(GrouperUtil.subjectToString(loggedInSubject) + " cannot ADMIN group: " + groupToAssignType.getName());
              }
              grouperObject = groupToAssignType;
            } else {
              throw new RuntimeException("Invalid input: "+stemObjectType+" must end with stem or group");
            }
            
            String objectType = stemObjectTypeArray[1];
            
            if (!GrouperObjectTypesSettings.getObjectTypeNames().contains(objectType)) {
              throw new RuntimeException("Invalid type: "+objectType);
            }
            
            String dataOwner = request.getParameter(stemObjectTypeWithNoStemGroupSuffix+"_dataOwner");
            String memberDescription = request.getParameter(stemObjectTypeWithNoStemGroupSuffix+"_memberDescription");
            String service = request.getParameter(stemObjectTypeWithNoStemGroupSuffix+"_service");
            
            GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
            attributeValue.setDirectAssignment(true);
            attributeValue.setObjectTypeDataOwner(dataOwner);
            attributeValue.setObjectTypeMemberDescription(memberDescription);
            attributeValue.setObjectTypeName(objectType);
            attributeValue.setObjectTypeServiceName(service);
            
            attributeValues.put(grouperObject, attributeValue);
            
          }
          
          return null;
        }
    });

     
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          for (GrouperObject grouperObject: attributeValues.keySet()) {
            
            GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValues.get(grouperObject), grouperObject);
          }
          
          return null;
          
        }
        
      });
      
    guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=" + stem.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("objectTypeEditSaveSuccess")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void runDaemon(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final ObjectTypeContainer objectTypeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getObjectTypeContainer();

      if (!objectTypeContainer.isCanRunDaemon()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final boolean[] DONE = new boolean[]{false};
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          GrouperSession grouperSession = GrouperSession.startRootSession();
          try {
            GrouperObjectTypesJob.runDaemonStandalone();
            DONE[0] = true;
          } catch (RuntimeException re) {
            LOG.error("Error in running daemon", re);
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          
        }
        
      });

      thread.start();
      
      try {
        thread.join(45000);
      } catch (Exception e) {
        throw new RuntimeException("Exception in thread", e);
      }

      if (DONE[0]) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("objectTypeSuccessDaemonRan")));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("objectTypeInfoDaemonInRunning")));

      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    
  }
  
}
