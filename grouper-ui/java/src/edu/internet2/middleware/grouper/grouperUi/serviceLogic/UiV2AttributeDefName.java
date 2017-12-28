/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefNameContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 */
public class UiV2AttributeDefName {
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2AttributeDefName.class);
  
  /**
   * delete attribute def name
   * @param request
   * @param response
   */
  public void deleteAttributeDefName(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }

      final String attributeDefNameId = request.getParameter("attributeDefNameId");
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
      
      //not sure why this would happen
      if (attributeDefName == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefDeleteAttributeDefNameCantFindAttributeDefName")));

      } else {

        attributeDefName.delete();

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAttributeDefNameDeleteSuccess")));
              
      }
      
      //filterHelper(request, response, attributeDef);

      GrouperUserDataApi.recentlyUsedAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete attribute def names
   * @param request
   * @param response
   */
  public void deleteAttributeDefNames(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    AttributeDef attributeDef = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
  
      Set<String> attributeDefNameIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String attributeDefNameId = request.getParameter("attributeDefName_" + i + "[]");
        if (!StringUtils.isBlank(attributeDefNameId)) {
          attributeDefNameIds.add(attributeDefNameId);
        }
      }
  
      if (attributeDefNameIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefRemoveNoAttributeDefNameSelects")));
        return;
      }
      int successes = 0;
      int failures = 0;
      
      for (String attributeDefNameId : attributeDefNameIds) {
        try {

          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
          
          //not sure why this would happen
          if (attributeDefName == null) {
            
            if (LOG.isDebugEnabled()) {
              LOG.debug("Cant find attribute def name for id: " + attributeDefNameId + ", maybe it was already deleted");
            }

            failures++;
            
          } else {
      
            attributeDefName.delete();
      
            successes++;
          }
          
          
        } catch (Exception e) {
          LOG.warn("Error deleting attributeDefName id: " + attributeDefNameId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().setSuccessCount(successes);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().setFailureCount(failures);
  
      if (failures > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefDeleteAttributeDefNamesErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefDeleteAttributeDefNamesSuccesses")));
      }
      
      
      //filterHelper(request, response, attributeDef);
  
      GrouperUserDataApi.recentlyUsedAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save attribute def name from edit screen
   * @param request
   * @param response
   */
  public void attributeDefNameEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    AttributeDefName attributeDefName = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String displayExtension = request.getParameter("attributeDefNameToEditDisplayExtension");
      String description = request.getParameter("attributeDefNameToEditDescription");
      String attributeDefNameId = request.getParameter("attributeDefNameId");
      
      if (StringUtils.isBlank(displayExtension)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#name", TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateErrorDisplayExtensionRequired")));
        return;
      }
      
      attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameNotFoundError")));
        return;
      }
      
      if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("attributeDefNoAdminPriv")));
        return;
      }
         
      attributeDefName.setDescription(description);
      attributeDefName.setDisplayExtensionDb(displayExtension);
      
      attributeDefName.store();
      
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef?attributeDefId=" + attributeDefName.getAttributeDefId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameEditSuccess")));

    } catch (Exception e) {
      LOG.warn("Error editing attribute def name: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeDefName, e);
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show attribute def name edit screen 
   * @param request
   * @param response
   */
  public void editAttributeDefName(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      final String attributeDefNameId = request.getParameter("attributeDefNameId");
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
      
      //not sure why this would happen
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefEditAttributeDefNameCantFindAttributeDefName")));
        return;
      }
      
      //initialize the bean
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
      attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/attributeDefNameEdit.jsp"));
      

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show the add attribute name screen
   * @param request
   * @param response
   */
  public void newAttributeDefName(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      //see if there is a stem id for this
      String objectStemId = request.getParameter("objectStemId");
      
      Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
      
      if (!StringUtils.isBlank(objectStemId) && pattern.matcher(objectStemId).matches()) {
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setObjectStemId(objectStemId);
      }
      
      UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      
      if (objectStemId != null) {
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, 2);
        Set<AttributeDef> attributes = new AttributeDefFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
            .assignStemScope(Scope.ONE).assignSubject(loggedInSubject)
            .assignQueryOptions(queryOptions).assignParentStemId(objectStemId).findAttributes();
        
        if (attributes.size() == 1) {
          GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer()
          .setObjectAttributeDefId(attributes.iterator().next().getId());
        }
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/newAttributeDefName.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save new attribute def name
   * @param request
   * @param response
   */
  public void newAttributeDefNameSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    AttributeDefName attributeDefName = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String parentFolderId = request.getParameter("parentFolderComboName");
      String attributeDefId = request.getParameter("attributeDefComboName");
      String displayExtension = request.getParameter("attributeDefNameToEditDisplayExtension");
      String extension = request.getParameter("attributeDefNameToEditExtension");
      String description = request.getParameter("attributeDefNameToEditDescription");
      
      if (StringUtils.isBlank(attributeDefId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateRequiredAttributeDef")));
        return;
      }
      
      if (StringUtils.isBlank(parentFolderId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateRequiredParentStemId")));
        return;
      }
      
      if (StringUtils.isBlank(displayExtension)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#name",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateErrorDisplayExtensionRequired")));
        return;
      }

      if (StringUtils.isBlank(extension)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefNameId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateErrorExtensionRequired")));
        return;
      }
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();

      if (parentFolder == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateCantFindStemId")));
        return;
      }
      
      String stemName = parentFolder.getName();
      String attributeDefNameName = stemName + ":" + extension;
      if (AttributeDefNameFinder.findByName(attributeDefNameName, false) != null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameExistsError")));
        return;
      }
      
      AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
      if (attributeDef == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateRequiredAttributeDef")));
        return;
      }
         
      attributeDefName = parentFolder.addChildAttributeDefName(attributeDef, extension, displayExtension);
      attributeDefName.setDescription(description);
      
      attributeDefName.setDisplayExtensionDb(displayExtension);
      
      attributeDefName.store();
      
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef?attributeDefId=" + attributeDef.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateSuccess")));

    } catch (Exception e) {
      LOG.warn("Error creating attribute def name: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeDefName, e);
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * attribute def name view screen 
   * @param request
   * @param response
   */
  public void viewAttributeDefName(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final String attributeDefNameId = request.getParameter("attributeDefNameId");
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
      
      //not sure why this would happen
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefEditAttributeDefNameCantFindAttributeDefName")));
        return;
      }
      
      //initialize the bean
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
      attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/attributeDef/attributeDefNameView.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * combo filter for attribute def name
   * @param request
   * @param response
   */
  public void attributeDefNameFilter(final HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<AttributeDefName>() {
  
      @Override
      public AttributeDefName lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        String attributeDefId = request.getParameter("attributeDefComboName");
        
        String attributeAssignTypeString = request.getParameter("attributeAssignType");
        
        AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, false);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, 2);
        
        if (StringUtils.isNotBlank(attributeDefId)) {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
          if (attributeDef == null) {
            throw new RuntimeException("given attribute def id "+attributeDefId+" is not valid.");
          }
          
          Set<AttributeDefName> attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              query, grouperSession, attributeDefId, loggedInSubject, 
              AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES, queryOptions, attributeAssignType, null);
          
          if (attributeDefNames.size() > 1) {
            throw new RuntimeException("shouldn't have found more than one attribute def name.");
          }
          if (attributeDefNames.size() == 1) {
            return attributeDefNames.iterator().next();
          }
          return null;
        } else {
          
          Set<AttributeDefName> attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              query, grouperSession, null, loggedInSubject, 
              AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES, queryOptions, attributeAssignType, null);
          
          if (attributeDefNames.size() > 1) {
            throw new RuntimeException("shouldn't have found more than one attribute def name.");
          }
          if (attributeDefNames.size() == 1) {
            return attributeDefNames.iterator().next();
          }
          return null;
        }

      }

      @Override
      public Collection<AttributeDefName> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        String attributeDefId = request.getParameter("attributeDefComboName");
        String attributeAssignTypeString = request.getParameter("attributeAssignType");

        AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, false);

        int attributeDefsComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.attributeDefNamesComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, attributeDefsComboSize);
        
        if (StringUtils.isNotBlank(attributeDefId)) {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
          if (attributeDef == null) {
            throw new RuntimeException("given attribute def id "+attributeDefId+" is not valid.");
          }
          return GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              query, grouperSession, attributeDefId, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, attributeAssignType, null);
          
        } else {
          return GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              query, grouperSession, null, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, attributeAssignType, null);
        }
      }
  
      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, AttributeDefName t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, AttributeDefName t) {
        return t.getDisplayName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, AttributeDefName t) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(t.getDisplayName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/folder.gif\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }

}
