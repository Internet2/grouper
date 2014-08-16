/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * operations in the stem screen
 * @author mchyzer
 *
 */
public class UiV2AttributeDef {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2AttributeDef.class);

  /**
   * results from retrieving results
   *
   */
  public static class RetrieveAttributeDefHelperResult {

    /**
     * attributeDef
     */
    private AttributeDef attributeDef;

    /**
     * attributedef
     * @return attributedef
     */
    public AttributeDef getAttributeDef() {
      return this.attributeDef;
    }

    /**
     * attributeDef
     * @param attributeDef1
     */
    public void setAttributeDef(AttributeDef attributeDef1) {
      this.attributeDef = attributeDef1;
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
   * get the attributeDef from the request where the attributeDef is required and require privilege is either needed or not
   * @param request
   * @param requireAttributeDefPrivilege 
   * @return the stem finder result
   */
  public static RetrieveAttributeDefHelperResult retrieveStemHelper(HttpServletRequest request, 
      Privilege requireAttributeDefPrivilege) {
    return retrieveAttributeDefHelper(request, requireAttributeDefPrivilege, true);
  }

  /**
   * get the attribute def from the request
   * @param request
   * @param requirePrivilege
   * @param requireAttributeDef
   * @return the stem finder result
   */
  public static RetrieveAttributeDefHelperResult retrieveAttributeDefHelper(HttpServletRequest request, 
      Privilege requirePrivilege, boolean requireAttributeDef) {

    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
    
    RetrieveAttributeDefHelperResult result = new RetrieveAttributeDefHelperResult();

    AttributeDef attributeDef = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String attributeDefId = request.getParameter("attributeDefId");
    String attributeDefIndex = request.getParameter("attributeDefIndex");
    String nameOfAttributeDef = request.getParameter("nameOfAttributeDef");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(attributeDefId)) {
      attributeDef = AttributeDefFinder.findById(attributeDefId, false);
    } else if (!StringUtils.isBlank(nameOfAttributeDef)) {
      attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
    } else if (!StringUtils.isBlank(attributeDefIndex)) {
      long idIndex = GrouperUtil.longValue(attributeDefIndex);
      attributeDef = AttributeDefFinder.findByIdIndexSecure(idIndex, false, null);
    } else {
      
      if (!requireAttributeDef) {
        return result;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDefId")));
      addedError = true;
    }

    
    if (attributeDef != null) {
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
        result.setAttributeDef(attributeDef);
      }

    } else {
      
      if (!addedError && (!StringUtils.isBlank(attributeDefId) || !StringUtils.isBlank(nameOfAttributeDef) || !StringUtils.isBlank(attributeDefIndex))) {
        result.setAddedError(true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDef")));
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
   * view attribute def
   * @param request
   * @param response
   */
  public void viewAttributeDef(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/viewAttributeDef.jsp"));
  
      filterHelper(request, response, attributeDef);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed, or paging or sorting, or view attribute def or something
   * @param attributeDef
   * @param request
   * @param response
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, AttributeDef attributeDef) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
    
    GuiPaging guiPaging = attributeDefContainer.getGuiPaging();

    QueryOptions queryOptions = QueryOptions.create("name", true, null, null);
      
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);

    AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder()
      .assignAttributeDefId(attributeDef.getId()).assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
      .assignSubject(GrouperSession.staticGrouperSession().getSubject());
    
    attributeDefNameFinder.assignQueryOptions(queryOptions);
    
    if (!StringUtils.isBlank(filterText)) {
      attributeDefNameFinder.assignScope(filterText);
      attributeDefNameFinder.assignSplitScope(true);
    }
  
    //set of subjects, and what memberships each subject has
    Set<AttributeDefName> results = attributeDefNameFinder.findAttributeNames();
  
    attributeDefContainer.setGuiAttributeDefNames(GuiAttributeDefName.convertFromAttributeDefNames(results));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefFilterResultsId", 
        "/WEB-INF/grouperUi2/attributeDef/attributeDefContents.jsp"));
  
  }

  /**
   * view group privileges
   * @param request
   * @param response
   */
  public void attributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp"));
      filterPrivilegesHelper(request, response, attributeDef);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button was pressed for privileges, or paging or sorting, or view AttributeDef privileges or something
   * @param request
   * @param response
   * @param attributeDef
   */
  private void filterPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, AttributeDef attributeDef) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String privilegeFilterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }

    GuiPaging guiPaging = grouperRequestContainer.getAttributeDefContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addAttributeDefId(attributeDef.getId()).assignCheckSecurity(true)
      .assignFieldType(FieldType.ATTRIBUTE_DEF)
      .assignEnabled(true)
      .assignHasFieldForMember(true)
      .assignHasMembershipTypeForMember(true)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }

    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
    }

    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForMember(privilegeFilterText);
    }

    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerAttributeDefPrivilegeInheritance(results);

    grouperRequestContainer.getAttributeDefContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefPrivilegeFilterResultsId", 
        "/WEB-INF/grouperUi2/attributeDef/attributeDefPrivilegeContents.jsp"));
  
  }

  /**
   * the filter button was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filter(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      filterHelper(request, response, attributeDef);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

   
  
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
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
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
      
      filterHelper(request, response, attributeDef);

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
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
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
      
      
      filterHelper(request, response, attributeDef);
  
      GrouperUserDataApi.recentlyUsedAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button for privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      AttributeDef attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
      
      if (attributeDef == null) {
        return;
      }
  
      filterPrivilegesHelper(request, response, attributeDef);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefSuccessAddedToMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp"));
  
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
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteAttributeDefRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefSuccessRemovedFromMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * search for a subject to add to the group
   * @param request
   * @param response
   */
  public void addMemberSearch(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().addMemberSearch(request, response);
  }

  /**
   * submit button on add member form pressed
   * @param request
   * @param response
   */
  public void addMemberSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
    
      String subjectString = request.getParameter("groupAddMemberComboName");
  
      Subject subject = null;
      
      if (subjectString != null && subjectString.contains("||")) {
        String sourceId = GrouperUtil.prefixOrSuffix(subjectString, "||", true);
        String subjectId = GrouperUtil.prefixOrSuffix(subjectString, "||", false);
        subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
  
      } else {
        subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
      }
  
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberCantFindSubject")));
        return;
      }      
  
      boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrAdmins[]"), false);
      boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrUpdaters[]"), false);
      boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrReaders[]"), false);
      boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrViewers[]"), false);
      boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptins[]"), false);
      boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptouts[]"), false);
      boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attributeDefAttrReaders[]"), false);
      boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attributeDefAttrUpdaters[]"), false);
      
      if (!adminChecked && !updateChecked
          && !readChecked && !viewChecked && !optinChecked && !optoutChecked
          && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberPrivRequired")));
        return;
        
      }

      boolean madeChanges = attributeDef.getPrivilegeDelegate().grantPrivs(subject, adminChecked, 
          updateChecked, readChecked, viewChecked, optinChecked, optoutChecked, attrReadChecked, 
          attrUpdateChecked, false);
      
      if (madeChanges) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberMadeChangesSuccess")));
  
        //what subscreen are we on?
        String groupRefreshPart = request.getParameter("attributeDefRefreshPart");
        if (StringUtils.equals(groupRefreshPart, "privileges")) {
          filterPrivileges(request, response);
        }
  
      } else {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberNoChangesSuccess")));
  
      }
  
      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
  /**
   * submit button on parent folder search model dialog
   * @param request
   * @param response
   */
  public void addMemberFilter(final HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().addMemberFilter(request, response);
  }

  /**
   * assign or remove a privilege from a user, and redraw the filter screen... put a success at top
   * @param request
   * @param response
   */
  public void assignPrivilege(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String memberId = request.getParameter("memberId");
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      Privilege privilege = AttributeDefPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        attributeDef.getPrivilegeDelegate().grantPriv(member.getSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        attributeDef.getPrivilegeDelegate().revokePriv(member.getSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      //reset the data (not really necessary, just in case)
      attributeDefContainer.setPrivilegeGuiMembershipSubjectContainers(null);
      
      
      filterPrivilegesHelper(request, response, attributeDef);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void assignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
  
      String attributeDefPrivilegeBatchUpdateOperation = request.getParameter("attributeDefPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(attributeDefPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + attributeDefPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      boolean readersUpdaters = StringUtils.equals(fieldName, "attrReadersAttrUpdaters");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<Member> members = new LinkedHashSet<Member>();
      
      //loop through all the checkboxes and collect all the members
      for (int i=0;i<pageSize;i++) {
        String memberId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
          members.add(member);
        }
      }
  
      if (GrouperUtil.length(members) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefErrorEntityRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        return;
      }
      
      int changes = 0;
      
      Privilege[] privileges = assignAll ? (assign ? new Privilege[]{  
          AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_ADMINS)} : new Privilege[]{  
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_ADMINS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_OPTOUTS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_READERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_VIEWERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_UPDATERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_OPTINS)
          } ) : (readersUpdaters ? new Privilege[]{AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_READERS),
              AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_UPDATERS)
          } : new Privilege[]{AttributeDefPrivilege.listToPriv(fieldName)});
      
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += attributeDef.getPrivilegeDelegate().grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += attributeDef.getPrivilegeDelegate().revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
        }
      }
      
      //reset the data (not really necessary, just in case)
      attributeDefContainer.setPrivilegeGuiMembershipSubjectContainers(null);
  
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "attributeDefSuccessGrantedPrivileges" : "attributeDefSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "attributeDefNoteNoGrantedPrivileges" : "attributeDefNoteNoRevokedPrivileges")));

      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));

      filterPrivilegesHelper(request, response, attributeDef);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  
}
