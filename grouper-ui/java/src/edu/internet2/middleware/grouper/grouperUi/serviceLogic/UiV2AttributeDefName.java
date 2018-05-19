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
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate.AttributeNameUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefNameContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
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
      
      attributeDefName.setExtensionDb(extension);
      attributeDefName.setDescription(description);
      attributeDefName.setDisplayExtensionDb(displayExtension);
      
      attributeDefName.store();
      
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
            throw new RuntimeException("shouldn't have found more than one attribute def name: '" + query + "'");
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
      
      if (defNamesThatImply != null) {
        for (AttributeDefName attributeDefNameThatImply:  attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThis()) {   
          attributeDefNameThatImply.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefName);
        }
        
        for (String attributeDefNameIdAddImply: defNamesThatImply) {
          AttributeDefName attributeDefNameThatImply = AttributeDefNameFinder.findById(attributeDefNameIdAddImply, true);
          attributeDefNameThatImply.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName);
        }
      }
      
      if (defNamesImpliedBy != null) {
        
        for (AttributeDefName attributeDefNameImpliedBy:  attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis()) {   
          attributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefNameImpliedBy);
        }
        
        for (String attributeDefNameImpliedByAdd: defNamesImpliedBy) {        
          AttributeDefName attributeDefNameImpliedBy = AttributeDefNameFinder.findById(attributeDefNameImpliedByAdd, true);
          attributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefNameImpliedBy);
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
