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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
        if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
          if (!attributeDefContainer.isCanAdmin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToAdminAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
          if (!attributeDefContainer.isCanView()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToViewAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.READ)) {
          if (!attributeDefContainer.isCanRead()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToReadAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
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
            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
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
    QueryOptions queryOptions = new QueryOptions();
  
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);

    AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder()
      .assignAttributeDefId(attributeDef.getId()).assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES);
    
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

    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
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

  
}
