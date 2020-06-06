/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinderResults;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssignFinderResults;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate.AttributeNameUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefNameContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
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
  public void deleteAttributeDefNameSubmit(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AttributeDefName attributeDefName = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      attributeDefName.delete();

      //go to the view attributeDef screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDefName.getAttributeDefId() + "')"));

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefAttributeDefNameDeleteSuccess")));
              
      GrouperUserDataApi.recentlyUsedAttributeDefNameAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDefName);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete an attribute def name (show confirm screen)
   * @param request
   * @param response
   */
  public void deleteAttributeDefName(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      AttributeDefName attributeDefName = null;
      
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  
  /**
   * delete attribute def names
   * @param request
   * @param response
   */
  public void deleteAttributeDefNamesSubmit(HttpServletRequest request, HttpServletResponse response) {
  
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

      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      String extension = request.getParameter("attributeDefNameToEditExtension");

      String displayExtension = request.getParameter("attributeDefNameToEditDisplayExtension");
      String description = request.getParameter("attributeDefNameToEditDescription");
      
      if (StringUtils.isBlank(displayExtension)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#name", TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateErrorDisplayExtensionRequired")));
        return;
      }
      if (StringUtils.isBlank(extension)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefNameId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefNameCreateErrorExtensionRequired")));
        return;
      }
      
      new AttributeDefNameSave(grouperSession, attributeDefName.getAttributeDef())
        .assignUuid(attributeDefName.getId())
        .assignName(attributeDefName.getParentStem().getName() + ":" + extension)
        .assignDisplayExtension(displayExtension).assignDescription(description).save();
            
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName?attributeDefNameId=" + attributeDefName.getId() + "')"));

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

      AttributeDefName attributeDefName = null;
      
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      //initialize the bean
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
      attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameEdit.jsp"));
      

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
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
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
      
      String attributeDefId = request.getParameter("attributeDefId");
      
      if (StringUtils.isNotBlank(attributeDefId)) {
        AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
        if (attributeDef == null) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDef")));
          return;
        }
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer()
        .setObjectAttributeDefId(attributeDef.getId());
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/newAttributeDefName.jsp"));
  
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
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeNameUpdate.errorCantEditAttributeDef")));
        return;
      }
         
      attributeDefName = parentFolder.addChildAttributeDefName(attributeDef, extension, displayExtension);
      attributeDefName.setDescription(description);
      
      attributeDefName.setDisplayExtensionDb(displayExtension);
      
      attributeDefName.store();
      
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName?attributeDefNameId=" + attributeDefName.getId() + "')"));

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
   * results from retrieving results
   *
   */
  public static class RetrieveAttributeDefNameHelperResult {

    /**
     * attributeDefName
     */
    private AttributeDefName attributeDefName;

    /**
     * attributedef
     * @return attributedef
     */
    public AttributeDefName getAttributeDefName() {
      return this.attributeDefName;
    }

    /**
     * attributeDef
     * @param attributeDefName1
     */
    public void setAttributeDefName(AttributeDefName attributeDefName1) {
      this.attributeDefName = attributeDefName1;
    }
    
    /**
     * if added error to screen
     */
    private boolean addedError;

    /**
     * if added error to screen
     * @return if error
     */
    public boolean isAddedError() {
      return this.addedError;
    }

    /**
     * if added error to screen
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
      this.addedError = addedError1;
    }
    
    
    
  }


  /**
   * get the attribute def from the request
   * @param request
   * @param requirePrivilege
   * @param requireAttributeDefName
   * @return the stem finder result
   */
  public static RetrieveAttributeDefNameHelperResult retrieveAttributeDefNameHelper(HttpServletRequest request, 
      Privilege requirePrivilege, boolean requireAttributeDefName) {

    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
    AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
    
    RetrieveAttributeDefNameHelperResult result = new RetrieveAttributeDefNameHelperResult();

    AttributeDefName attributeDefName = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String attributeDefNameId = request.getParameter("attributeDefNameId");
    String attributeDefNameIndex = request.getParameter("attributeDefNameIndex");
    String nameOfAttributeDefName = request.getParameter("nameOfAttributeDefName");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(attributeDefNameId)) {
      attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
    } else if (!StringUtils.isBlank(nameOfAttributeDefName)) {
      attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDefName, false);
    } else if (!StringUtils.isBlank(attributeDefNameIndex)) {
      long idIndex = GrouperUtil.longValue(attributeDefNameIndex);
      attributeDefName = AttributeDefNameFinder.findByIdIndexSecure(idIndex, false, null);
    } else {
      
      if (!requireAttributeDefName) {
        return result;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameCantFindAttributeDefNameId")));
      addedError = true;
    }

    
    if (attributeDefName != null) {
      attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
      
      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      attributeDefContainer.setGuiAttributeDef(new GuiAttributeDef(attributeDef));      
      
      boolean privsOk = true;

      if (requirePrivilege != null) {
        if (requirePrivilege.equals(AttributeDefPrivilege.ATTR_ADMIN)) {
          if (!attributeDefContainer.isCanAdmin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToAdminAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AttributeDefPrivilege.ATTR_VIEW)) {
          if (!attributeDefContainer.isCanView()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToViewAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AttributeDefPrivilege.ATTR_READ)) {
          if (!attributeDefContainer.isCanRead()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToReadAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AttributeDefPrivilege.ATTR_UPDATE)) {
          if (!attributeDefContainer.isCanUpdate()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToUpdateAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        }  
      }
      
      if (privsOk) {
        result.setAttributeDefName(attributeDefName);
      }

    } else {
      
      if (!addedError && (!StringUtils.isBlank(attributeDefNameId) || !StringUtils.isBlank(nameOfAttributeDefName) || !StringUtils.isBlank(attributeDefNameIndex))) {
        result.setAddedError(true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDefName")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
  
    //go back to the main screen, cant find group
    if (addedError) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }

    return result;
    
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
  
      AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      //initialize the bean
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
      attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameView.jsp"));

      if (GrouperUiUtils.isMenuRefreshOnView()) {
        guiResponseJs.addAction(GuiScreenAction.newScript("openFolderTreePathToObject(" + GrouperUiUtils.pathArrayToCurrentObject(grouperSession, attributeDefName) + ")"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * see owners where attribute def name is assigned
   * @param request
   * @param response
   */
  public void viewAttributeDefNameAssignedOwners(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      viewAttributeDefNameAssignedOwnersHelper(request, attributeDefName);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  } 
  
  
  private void viewAttributeDefNameAssignedOwnersHelper(final HttpServletRequest request, final AttributeDefName attributeDefName) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final AttributeDefNameContainer attributeDefNameContainer = grouperRequestContainer.getAttributeDefNameContainer();
    attributeDefNameContainer.setGuiAttributeDefName(new GuiAttributeDefName(attributeDefName));
    
    final String filter = request.getParameter("filter");
    
    //switch over to admin so attributes work
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
        
        GuiPaging guiPaging = attributeDefNameContainer.getGuiPaging();
        QueryOptions queryOptions = new QueryOptions().sortAsc("displayExtension");
        
        GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
        
        AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinder()
            .addAttributeDefNameId(attributeDefName.getId())
            .assignIncludeAssignmentsOnAssignments(true) //if first level
            .assignRetrieveValues(true) // get values
            .assignQueryOptions(queryOptions)
            .assignFilter(filter)
            .findAttributeAssignFinderResults();
        
        GuiAttributeAssignFinderResults guiAttributeAssignFinderResults = new GuiAttributeAssignFinderResults(attributeAssignFinderResults);
        
        attributeDefNameContainer.setGuiAttributeAssignFinderResults(guiAttributeAssignFinderResults);
        guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignments",
            "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameViewOwnerEntities.jsp"));
        
        return null;
      }
    });
    
  }
  
  /**
   * make the structure of the attribute assignment value
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentValueMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addMetadataAssignmentMenuItem = new DhtmlxMenuItem();
      addMetadataAssignmentMenuItem.setId("editValue");
      addMetadataAssignmentMenuItem.setText(TagUtils.navResourceString("simpleAttributeUpdate.editValueAssignmentAlt"));
      dhtmlxMenu.addDhtmlxItem(addMetadataAssignmentMenuItem);
    }
    
    {
      DhtmlxMenuItem addMetadataAssignmentMenuItem = new DhtmlxMenuItem();
      addMetadataAssignmentMenuItem.setId("deleteValue");
      addMetadataAssignmentMenuItem.setText(TagUtils.navResourceString("simpleAttributeUpdate.assignDeleteValueAlt"));
      dhtmlxMenu.addDhtmlxItem(addMetadataAssignmentMenuItem);
    }

    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" +
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);

    throw new ControllerDone();
  }
  
  /**
   * handle a click or select from the assignment value menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentValueMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");

    if (StringUtils.equals(menuItemId, "editValue")) {
      this.assignValueEdit();
    } else if (StringUtils.equals(menuItemId, "deleteValue")) {
      this.assignValueDelete();
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  }
  
  /**
   * edit an attribute assignment value
   */
  private void assignValueEdit() {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      
      String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");

      if (StringUtils.isBlank(menuIdOfMenuTarget)) {
        throw new RuntimeException("Missing id of menu target");
      }
      if (!menuIdOfMenuTarget.startsWith("assignmentValueButton_")) {
        throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
      }
      
      String[] values = menuIdOfMenuTarget.split("_");
      
      if (values.length != 4) {
        throw new RuntimeException("Invalid id of menu target");
      }
      
      String attributeAssignId = values[1];
  
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
  
      //now we need to check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }

      String attributeAssignValueId = values[2];
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }
  
      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      attributeUpdateRequestContainer.setAttributeAssignValue(attributeAssignValue);
      AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();

      if (attributeAssignType.isAssignmentOnAssignment()) {
        AttributeAssign underlyingAssignment = attributeAssign.getOwnerAttributeAssign();
        AttributeAssignType underlyingAttributeAssignType = underlyingAssignment.getAttributeAssignType();
        
        //set the type to underlying, so that the labels are correct
        GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
        guiUnderlyingAttributeAssign.setAttributeAssign(underlyingAssignment);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
        
        GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
        guiAttributeAssignAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
        attributeUpdateRequestContainer.setAttributeAssignType(underlyingAttributeAssignType);
        attributeUpdateRequestContainer.setAttributeAssignAssignType(attributeAssignType);
        
      } else {
        attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);
        
        GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
        guiAttributeAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
                
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignments", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameAssignValueEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  /**
   * delete an attribute assignment value
   */
  private void assignValueDelete() {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      
      String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");

      if (StringUtils.isBlank(menuIdOfMenuTarget)) {
        throw new RuntimeException("Missing id of menu target");
      }
      if (!menuIdOfMenuTarget.startsWith("assignmentValueButton_")) {
        throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
      }
      
      String[] values = menuIdOfMenuTarget.split("_");
      
      if (values.length != 4) {
        throw new RuntimeException("Invalid id of menu target");
      }
      
      String attributeAssignId = values[1];

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);

      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }
      
      String attributeAssignValueId = values[2];
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }
      
      String attributeDefNameId = values[3];
      
      if (StringUtils.isBlank(attributeDefNameId)) {
        throw new RuntimeException("Why is attributeDefNameId blank???");
      }
      
      final AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
            
      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      attributeAssign.getValueDelegate().deleteValue(attributeAssignValue);
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignValueSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      viewAttributeDefNameAssignedOwnersHelper(httpServletRequest, attributeDefName);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * submit the edit value screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignValueEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);

      //now we need to check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }
      
      String attributeAssignValueId = httpServletRequest.getParameter("attributeAssignValueId");
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }

      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(httpServletRequest, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      {
        String valueToEdit = httpServletRequest.getParameter("valueToEdit");
        
        if (StringUtils.isBlank(valueToEdit) ) {
          String required = TagUtils.navResourceString("simpleAttributeUpdate.editValueRequired");
          required = GrouperUiUtils.escapeHtml(required, true);
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, required));
          return;
          
        }
        
        attributeAssignValue.assignValue(valueToEdit);
        attributeAssignValue.saveOrUpdate();
        
      }
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignEditValueSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      viewAttributeDefNameAssignedOwnersHelper(httpServletRequest, attributeDefName);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * add a value
   * @param request
   * @param response
   */
  public void assignmentMenuAddValue(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    String attributeAssignId = request.getParameter("attributeAssignId");

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign attributeAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

      AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();

      if (attributeAssignType.isAssignmentOnAssignment()) {
        AttributeAssign underlyingAssignment = attributeAssign.getOwnerAttributeAssign();
        AttributeAssignType underlyingAttributeAssignType = underlyingAssignment.getAttributeAssignType();
        
        //set the type to underlying, so that the labels are correct
        GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
        guiUnderlyingAttributeAssign.setAttributeAssign(underlyingAssignment);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
        
        GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
        guiAttributeAssignAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
        attributeUpdateRequestContainer.setAttributeAssignType(underlyingAttributeAssignType);
        attributeUpdateRequestContainer.setAttributeAssignAssignType(attributeAssignType);
        
      } else {
        attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);
        
        GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
        guiAttributeAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignments", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameAssignAddValue.jsp"));
      
    } catch (Exception e) {
      throw new RuntimeException("Error addValue: " + e.getMessage(), e);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  
  /**
   * assign attribute value submit
   * @param request
   * @param response
   */
  public void attributeAssignAddValueSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeJavascript(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }
      
      {
        String valueToAdd = request.getParameter("valueToAdd");
        
        if (StringUtils.isBlank(valueToAdd) ) {
          String required = TagUtils.navResourceString("simpleAttributeUpdate.addValueRequired");
          required = GrouperUiUtils.escapeJavascript(required, true);
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, required));
          return;
          
        }
        
        attributeAssign.getValueDelegate().addValue(valueToAdd);
        
      }
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignAddValueSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      viewAttributeDefNameAssignedOwnersHelper(request, attributeDefName);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * add an assignment on an assignment
   * @param request
   * @param response
   */
  public void assignmentMenuAddMetadataAssignment(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String attributeAssignId = request.getParameter("attributeAssignId");
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign attributeAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.assignCantAddMetadataOnAssignmentOfAssignment")));
        return;
        
      }
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      attributeUpdateRequestContainer.setAttributeAssignType(attributeAssign.getAttributeAssignType());
      
      GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
      guiAttributeAssign.setAttributeAssign(attributeAssign);
      
      attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignments", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameAssignAddMetadataAssignment.jsp"));
      
    } catch (Exception e) {
      throw new RuntimeException("Error addMetadataAssignment: " + e.getMessage(), e);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  /**
   * submit the add metadata screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignMetadataAddSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }
      
      final AttributeDefName attributeDefNameOrig = retrieveAttributeDefNameHelper(httpServletRequest, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefNameOrig == null) {
        return;
      }
      
      //todo check more security, e.g. where it is assigned
      
      {
        String attributeAssignAssignAttributeNameId = httpServletRequest.getParameter("attributeAssignAssignAttributeComboName");
        
        AttributeDefName attributeDefName = null;

        if (!StringUtils.isBlank(attributeAssignAssignAttributeNameId) ) {
          attributeDefName = AttributeDefNameFinder.findById(attributeAssignAssignAttributeNameId, false);
          
        }
        
        if (attributeDefName == null) {
          String required = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAttributeNameRequired");
          required = GrouperUiUtils.escapeHtml(required, true);
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, required));
          return;
          
        }
        
        if (attributeDefName.getAttributeDef().isMultiAssignable()) {
          
          attributeAssign.getAttributeDelegate().addAttribute(attributeDefName);
          
        } else {
          
          if (attributeAssign.getAttributeDelegate().hasAttribute(attributeDefName)) {
            
            String alreadyAssigned = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAlreadyAssigned");
            alreadyAssigned = GrouperUiUtils.escapeHtml(alreadyAssigned, true);
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, alreadyAssigned));
            return;
          }
          
          attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName);

        }
        
      }
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAddSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      viewAttributeDefNameAssignedOwnersHelper(httpServletRequest, attributeDefNameOrig);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * edit an attribute assignment
   * @param request
   * @param response
   */
  public void assignEdit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);

      //we need the type so we know how to display it
      AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();
      
      if (attributeAssignType.isAssignmentOnAssignment()) {
        AttributeAssign underlyingAssignment = attributeAssign.getOwnerAttributeAssign();
        AttributeAssignType underlyingAttributeAssignType = underlyingAssignment.getAttributeAssignType();
        
        //set the type to underlying, so that the labels are correct
        GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
        guiUnderlyingAttributeAssign.setAttributeAssign(underlyingAssignment);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
        
        GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
        guiAttributeAssignAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
        attributeUpdateRequestContainer.setAttributeAssignType(underlyingAttributeAssignType);
        attributeUpdateRequestContainer.setAttributeAssignAssignType(attributeAssignType);
        
      } else {
        attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);
        
        GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
        guiAttributeAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignments", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameAssignEdit.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * submit the assign edit screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(httpServletRequest, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }

      {
        String enabledDate = httpServletRequest.getParameter("enabledDate");
        
        if (StringUtils.isBlank(enabledDate) ) {
          attributeAssign.setEnabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
          attributeAssign.setEnabledTime(enabledTimestamp);
        }
      }
      
      {
        String disabledDate = httpServletRequest.getParameter("disabledDate");
  
        if (StringUtils.isBlank(disabledDate) ) {
          attributeAssign.setDisabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
          attributeAssign.setDisabledTime(disabledTimestamp);
        }
      }
      
      attributeAssign.saveOrUpdate();
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignEditSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
     
      viewAttributeDefNameAssignedOwnersHelper(httpServletRequest, attributeDefName);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * delete an attribute assignment
   * @param request
   * @param response
   */
  public void assignDelete(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
     
      String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }
      
      final AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      // check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, notAllowed));
        return;
      }
      
      attributeAssign.delete();
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      viewAttributeDefNameAssignedOwnersHelper(request, attributeDefName);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
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
            //throw new RuntimeException("shouldn't have found more than one attribute def name.");
            // this can return multiple results so we don't want an exception, right?
            LOG.debug("found more than one attribute def name for query: " + query);
            return null;
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
            //throw new RuntimeException("shouldn't have found more than one attribute def name: '" + query + "'");
            // this can return multiple results so we don't want an exception, right?
            LOG.debug("found more than one attribute def name for query: " + query);
            return null;
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
        String attributeDefTypeString = request.getParameter("attributeDefType");

        AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, false);
        AttributeDefType attributeDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, false);

        int attributeDefsComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.attributeDefNamesComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, attributeDefsComboSize);
        
        if (StringUtils.isNotBlank(attributeDefId)) {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
          if (attributeDef == null) {
            throw new RuntimeException("given attribute def id "+attributeDefId+" is not valid.");
          }
        }
        
        return GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
            query, grouperSession, attributeDefId, loggedInSubject, 
            GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, attributeAssignType, attributeDefType);
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
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/cog.png\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    AttributeDefName attributeDefName = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, false).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteAttributeDefNameAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDefName);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameSuccessAddedToMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefNameMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameMoreActionsButtonContents.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * ajax logic to remove from my favorites
   * @param request
   * @param response
   */
  public void removeFromMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDefName attributeDefName = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, false).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteAttributeDefNameRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDefName);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameSuccessRemovedFromMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefNameMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameMoreActionsButtonContents.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * delete an attribute def name (show confirm screen)
   * @param request
   * @param response
   */
  public void deleteAttributeDefNames(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      AttributeDefName attributeDefName = null;
      
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNamesDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show attribute def name inheritance screen
   * @param request
   * @param response
   */
  public void editAttributeDefNameInheritance(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      AttributeDefName attributeDefName = null;
      
      attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      if (attributeDef.getAttributeDefType() != AttributeDefType.perm) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeNameUpdate.errorNotPermission")));
        return;
      }
      
      AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      QueryOptions queryOptions = QueryOptions.create("name", true, null, null);
      
      AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder()
        .assignAttributeDefId(attributeDef.getId()).assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject());
      
      attributeDefNameFinder.assignQueryOptions(queryOptions);
      
      Set<AttributeDefName> allAttributeDefNames = attributeDefNameFinder.findAttributeNames();
      
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      attributeNameUpdateRequestContainer.setAttributeDef(attributeDefName.getAttributeDef());
      attributeNameUpdateRequestContainer.setAllAttributeDefNamesForCurrentAttributeDef(allAttributeDefNames);
      
      {
        Set<AttributeDefName> attributeDefNamesThatImplyThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThis();
        attributeNameUpdateRequestContainer.setAttributeDefNamesThatImplyThis(attributeDefNamesThatImplyThis);
      }
      
      {
        Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThisImmediate();
        attributeNameUpdateRequestContainer.setAttributeDefNamesThatImplyThisImmediate(attributeDefNamesThatImplyThisImmediate);
      }
      
      {
        Set<AttributeDefName> attributeDefNamesImpliedByThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
        attributeNameUpdateRequestContainer.setAttributeDefNamesImpliedByThis(attributeDefNamesImpliedByThis);
      }
      
      {
        Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate();
        attributeNameUpdateRequestContainer.setAttributeDefNamesImpliedByThisImmediate(attributeDefNamesImpliedByThisImmediate);
      }
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefName/attributeDefNameInheritance.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save attribute def name inheritance
   * @param request
   * @param response
   */
  public void editAttributeDefNameInheritanceSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AttributeDefName attributeDefName = retrieveAttributeDefNameHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDefName();
      
      if (attributeDefName == null) {
        return;
      }
      
      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      
      String[] defNamesThatImply = request.getParameterValues("defNamesThatImmediatelyImply[]");
      String[] defNamesImpliedBy = request.getParameterValues("defNamesImpliedByImmediate[]");
      
      if (defNamesThatImply == null) {
        defNamesThatImply = new String[0];
      }
      
      if (defNamesImpliedBy == null) {
        defNamesImpliedBy = new String[0];
      }
      
      if (defNamesThatImply != null) {
        Set<String> existingAttributeDefNameIds = new HashSet<String>();
        Set<String> newAttributeDefNameIds = new HashSet<String>();
        CollectionUtils.addAll(newAttributeDefNameIds, defNamesThatImply);
        
        for (AttributeDefName attributeDefNameThatImply:  attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThisImmediate()) {
          if (newAttributeDefNameIds.contains(attributeDefNameThatImply.getId())) {
            existingAttributeDefNameIds.add(attributeDefNameThatImply.getId());
          } else {
            attributeDefNameThatImply.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefName);
          }
        }
        
        for (String attributeDefNameIdAddImply: newAttributeDefNameIds) {
          if (!existingAttributeDefNameIds.contains(attributeDefNameIdAddImply)) {
            AttributeDefName attributeDefNameThatImply = AttributeDefNameFinder.findById(attributeDefNameIdAddImply, true);
            attributeDefNameThatImply.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName);
          }
        }
      }
      
      if (defNamesImpliedBy != null) {
        Set<String> existingAttributeDefNameIds = new HashSet<String>();
        Set<String> newAttributeDefNameIds = new HashSet<String>();
        CollectionUtils.addAll(newAttributeDefNameIds, defNamesImpliedBy);
        
        for (AttributeDefName attributeDefNameImpliedBy:  attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate()) {   
          if (newAttributeDefNameIds.contains(attributeDefNameImpliedBy.getId())) {
            existingAttributeDefNameIds.add(attributeDefNameImpliedBy.getId());
          } else {
            attributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefNameImpliedBy);
          }
        }
        
        for (String attributeDefNameImpliedByAdd: newAttributeDefNameIds) {  
          if (!existingAttributeDefNameIds.contains(attributeDefNameImpliedByAdd)) {
            AttributeDefName attributeDefNameImpliedBy = AttributeDefNameFinder.findById(attributeDefNameImpliedByAdd, true);
            attributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefNameImpliedBy);
          }
        }
      }
      
      //go to the view attributeDef screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDefName.getAttributeDefId() + "')"));
      
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameInheritanceEditSuccess")));

    } catch (Exception e) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefNameInheritanceEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
