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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

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
   * view this groups privileges inherited from folders
   * @param request
   * @param response
   */
  public void thisAttributeDefsPrivilegesInheritedFromFolders(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      {
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findAttributeDefPrivilegeInheritRules(attributeDef.getParentStem());
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
          if (guiRuleDefinition.getOwnerGuiStem() != null) {
            guiRuleDefinitions.add(guiRuleDefinition);
          }
        }
      }
      
      for (GuiRuleDefinition guiRuleDefinition : guiRuleDefinitions) {
        if (StringUtils.equals(attributeDef.getParentStem().getUuid(), guiRuleDefinition.getOwnerGuiStem().getStem().getUuid())) {
          guiRuleDefinition.setDirect(true);
        }
      }
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/thisAttributeDefsPrivilegesInheritedFromFolders.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * get the attributeDef from the request where the attributeDef is required and require privilege is either needed or not
   * @param request
   * @param requireAttributeDefPrivilege 
   * @return the stem finder result
   */
  public static RetrieveAttributeDefHelperResult retrieveAttributeDefHelper(HttpServletRequest request, 
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
        try {
          subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
        } catch (SubjectNotUniqueException snue) {
          //ignore
        }
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

  /**
   * new attributeDef (show create screen)
   * @param request
   * @param response
   */
  public void newAttributeDef(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/newAttributeDef.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param guiResponseJs
   * @param gve
   */
  private void handleGrouperValidationException(GuiResponseJs guiResponseJs,
      GrouperValidationException gve) {
    //# attribute definition validations fields too long
    //attributeDefValidation_attributeDefDescriptionTooLong = Error: attribute definition description is too long
    //attributeDefValidation_attributeDefExtensionTooLong = Error: attribute definition ID is too long
    //attributeDefValidation_attributeDefNameTooLong = Error: the attribute definition ID causes the ID path to be too long, please shorten it
    
    if (StringUtils.equals(AttributeDef.VALIDATION_DESCRIPTION_OF_ATTRIBUTE_DEF_TOO_LONG_KEY, gve.getGrouperValidationKey())) {
      
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#attributeDefDescription",
          TextContainer.retrieveFromRequest().getText().get("attributeDefValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else if (StringUtils.equals(AttributeDef.VALIDATION_EXTENSION_OF_ATTRIBUTE_DEF_TOO_LONG_KEY, gve.getGrouperValidationKey())
        || StringUtils.equals(AttributeDef.VALIDATION_NAME_OF_ATTRIBUTE_DEF_TOO_LONG_KEY, gve.getGrouperValidationKey())) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#attributeDefId",
          TextContainer.retrieveFromRequest().getText().get("attributeDefValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else {
      LOG.error("Non-fatal error, not expecting GrouperValidationException: " + gve.getGrouperValidationKey(), gve);
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, gve.getMessage()));
      return;
    }
  }

  /**
   * when the attribute def type changes, make sure the assign to shows the right stuff
   * @param request
   * @param response
   */
  public void attributeDefTypeChanged(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      final String attributeDefTypeString = request.getParameter("attributeDefType");
      
      AttributeDefType attributeDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, false);
      
      if (attributeDefType == null) {
        
        //lets show all
        guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));
        
      } else {

        switch(attributeDefType) {
          case attr:

            //lets show all
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));

            break;
            
          case type:

            //lets show all
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToTypeHideCheckbox').hide('slow')"));

            break;
            
          case limit:

            //lets show all, hide the non limit ones
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToLimitHideCheckbox').hide('slow')"));

            break;
            
          case perm:
            
            //lets show all, hide the non perm ones
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToPermHideCheckbox').hide('slow')"));

            break;
            
          case service:

            //lets show all, hide the non service ones
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToCheckbox').show('slow')"));
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.assignToServiceHideCheckbox').hide('slow')"));

            break;
          
        }
        
      }

      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * when the attribute def value type changes, make sure the multi-valued gets updated
   * @param request
   * @param response
   */
  public void attributeDefValueTypeChanged(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      final String attributeDefValueTypeString = request.getParameter("attributeDefValueType");
      
      AttributeDefValueType attributeDefValueType = AttributeDefValueType.valueOfIgnoreCase(attributeDefValueTypeString, false);
      
      if (attributeDefValueType == null) {
        
        //lets show it
        guiResponseJs.addAction(GuiScreenAction.newScript("$('.multiAssignFieldClass').show('slow')"));
        
      } else {

        switch(attributeDefValueType) {
          case marker:
            //lets show all
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.multiAssignFieldClass').hide('slow')"));
            
            
            break;
          case floating:
          case integer:
          case memberId:
          case string:
          case timestamp:

            //lets show it
            guiResponseJs.addAction(GuiScreenAction.newScript("$('.multiAssignFieldClass').show('slow')"));


            break;
          
        }
        
      }

      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * new attributeDef submit
   * @param request
   * @param response
   */
  public void newAttributeDefSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final String extension = request.getParameter("extension");
      final String description = request.getParameter("description");
      final boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrAdmins[]"), false);
      final boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrUpdaters[]"), false);
      final boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrReaders[]"), false);
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrViewers[]"), false);
      final boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptins[]"), false);
      final boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptouts[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrReaders[]"), false);
      final boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrUpdaters[]"), false);
      final String attributeDefTypeString = request.getParameter("attributeDefType");
      final String attributeDefValueTypeString = request.getParameter("attributeDefValueType");
      String attributeDefToEditMultiAssignable = request.getParameter("attributeDefMultiAssignable");
      String attributeDefToEditMultiValued = request.getParameter("attributeDefMultiValued");
      
      boolean multiAssignable = GrouperUtil.booleanValue(attributeDefToEditMultiAssignable, false);
      boolean multiValued = GrouperUtil.booleanValue(attributeDefToEditMultiValued, false);
      
      String attributeDefToEditAssignToAttributeDef = request.getParameter("attributeDefToEditAssignToAttributeDef");
      boolean attributeDefToEditAssignToAttributeDefBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDef, false);

      String attributeDefToEditAssignToAttributeDefAssign = request.getParameter("attributeDefToEditAssignToAttributeDefAssign");
      boolean attributeDefToEditAssignToAttributeDefAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDefAssign, false);

      String attributeDefToEditAssignToStem = request.getParameter("attributeDefToEditAssignToStem");
      boolean attributeDefToEditAssignToStemBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStem, false);

      String attributeDefToEditAssignToStemAssign = request.getParameter("attributeDefToEditAssignToStemAssign");
      boolean attributeDefToEditAssignToStemAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStemAssign, false);

      String attributeDefToEditAssignToGroup = request.getParameter("attributeDefToEditAssignToGroup");
      boolean attributeDefToEditAssignToGroupBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroup, false);

      String attributeDefToEditAssignToGroupAssign = request.getParameter("attributeDefToEditAssignToGroupAssign");
      boolean attributeDefToEditAssignToGroupAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroupAssign, false);

      String attributeDefToEditAssignToMember = request.getParameter("attributeDefToEditAssignToMember");
      boolean attributeDefToEditAssignToMemberBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMember, false);

      String attributeDefToEditAssignToMemberAssign = request.getParameter("attributeDefToEditAssignToMemberAssign");
      boolean attributeDefToEditAssignToMemberAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMemberAssign, false);

      String attributeDefToEditAssignToMembership = request.getParameter("attributeDefToEditAssignToMembership");
      boolean attributeDefToEditAssignToMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembership, false);

      String attributeDefToEditAssignToMembershipAssign = request.getParameter("attributeDefToEditAssignToMembershipAssign");
      boolean attributeDefToEditAssignToMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembershipAssign, false);

      String attributeDefToEditAssignToImmediateMembership = request.getParameter("attributeDefToEditAssignToImmediateMembership");
      boolean attributeDefToEditAssignToImmediateMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembership, false);

      String attributeDefToEditAssignToImmediateMembershipAssign = request.getParameter("attributeDefToEditAssignToImmediateMembershipAssign");
      boolean attributeDefToEditAssignToImmediateMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembershipAssign, false);
            
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      if (StringUtils.isBlank(parentFolderId)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateRequiredParentStemId")));
        return;
      }
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
  
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateCantFindParentStemId")));
        return;
        
      }
      
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorExtensionRequired")));
        return;
        
      }
  
      if (parentFolder.isRootStem()) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateCantCreateInRoot")));
        return;
        
      }
  
      final String nameOfAttributeDef = parentFolder.getName() + ":" + extension;
      
      //search as an admin to see if the group exists
      attributeDef = (AttributeDef)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(nameOfAttributeDef, false);
        }
      });
  
      if (attributeDef != null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateCantCreateAlreadyExists")));
        return;
      }
      
      if (StringUtils.isBlank(attributeDefTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefTypeId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAttributeTypeRequired")));

        return;
      }

      AttributeDefType attributeDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, true);

      //make sure the type and assign to's match
      switch(attributeDefType) {
        case attr:
          //allow all
          break;
        case type:
          //type can only be non assignments
          //attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          //attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          //attributeDefToEditAssignToGroupBoolean = false;
          attributeDefToEditAssignToGroupAssignBoolean = false;
          //attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          //attributeDefToEditAssignToMembershipBoolean = false;
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          //attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case perm:
          //perm can only be group and effective membership
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          //attributeDefToEditAssignToGroupBoolean
          attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          //attributeDefToEditAssignToMembershipBoolean
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case limit:
          //limit can only be group assign and membership assign
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          attributeDefToEditAssignToGroupBoolean = false;
          //attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          attributeDefToEditAssignToMembershipBoolean = false;
          //attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case service:
          //service can only be  stem
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          //attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          attributeDefToEditAssignToGroupBoolean = false;
          attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          attributeDefToEditAssignToMembershipBoolean = false;
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
          
      }
      

      
      if (StringUtils.isBlank(attributeDefValueTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefValueTypeId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAttributeValueTypeRequired")));

        return;
      }

      AttributeDefValueType attributeDefValueType = AttributeDefValueType.valueOfIgnoreCase(attributeDefValueTypeString, true);

      
      //validate that at least one assign to is selected
      if (!attributeDefToEditAssignToAttributeDefBoolean && !attributeDefToEditAssignToAttributeDefAssignBoolean
          && !attributeDefToEditAssignToStemBoolean && !attributeDefToEditAssignToStemAssignBoolean
          && !attributeDefToEditAssignToGroupBoolean && !attributeDefToEditAssignToGroupAssignBoolean
          && !attributeDefToEditAssignToMemberBoolean && !attributeDefToEditAssignToMemberAssignBoolean
          && !attributeDefToEditAssignToMembershipBoolean && !attributeDefToEditAssignToMembershipAssignBoolean
          && !attributeDefToEditAssignToImmediateMembershipBoolean && !attributeDefToEditAssignToImmediateMembershipAssignBoolean) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#assignToLabelId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAssignToRequired")));
        return;
      }

      switch (attributeDefValueType) {
        case marker:
          //cant be multi valued
          multiValued = false;
          break;
          
        case floating:
        case integer:
        case memberId:
        case string:
        case timestamp:
          //do nothing
          break;
      }

      try {
  
        //create the attribute def
        attributeDef = new AttributeDefSave(grouperSession).assignName(nameOfAttributeDef).assignSaveMode(SaveMode.INSERT)
            .assignDescription(description).assignAttributeDefType(attributeDefType)
            .assignValueType(attributeDefValueType)
            .assignMultiAssignable(multiAssignable)
            .assignMultiValued(multiValued)
            .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
            .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
            .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked)
            .assignToAttributeDef(attributeDefToEditAssignToAttributeDefBoolean)
            .assignToAttributeDefAssn(attributeDefToEditAssignToAttributeDefAssignBoolean)
            .assignToStem(attributeDefToEditAssignToStemBoolean)
            .assignToStemAssn(attributeDefToEditAssignToStemAssignBoolean)
            .assignToGroup(attributeDefToEditAssignToGroupBoolean)
            .assignToGroupAssn(attributeDefToEditAssignToGroupAssignBoolean)
            .assignToMember(attributeDefToEditAssignToMemberBoolean)
            .assignToMemberAssn(attributeDefToEditAssignToMemberAssignBoolean)
            .assignToEffMembership(attributeDefToEditAssignToMembershipBoolean)
            .assignToEffMembershipAssn(attributeDefToEditAssignToMembershipAssignBoolean)
            .assignToImmMembership(attributeDefToEditAssignToImmediateMembershipBoolean)
            .assignToImmMembershipAssn(attributeDefToEditAssignToImmediateMembershipAssignBoolean)
            .save();
  
      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;
  
        
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for attribute def create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error creating attributeDef: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeDef, sde);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
  
      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDef.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefCreateSuccess")));
  
      GrouperUserDataApi.recentlyUsedAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit an attribute def, show the edit screen
   * @param request
   * @param response
   */
  public void attributeDefEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().getGuiAttributeDef().setShowBreadcrumbLink(true);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer().getGuiAttributeDef().setShowBreadcrumbLinkSeparator(false);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDef/attributeDefEdit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit attribute def submit
   * @param request
   * @param response
   */
  public void attributeDefEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      final String extension = request.getParameter("extension");
      final String description = request.getParameter("description");
      final boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrAdmins[]"), false);
      final boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrUpdaters[]"), false);
      final boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrReaders[]"), false);
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrViewers[]"), false);
      final boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptins[]"), false);
      final boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptouts[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrReaders[]"), false);
      final boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrUpdaters[]"), false);
      final String attributeDefTypeString = request.getParameter("attributeDefType");
      final String attributeDefValueTypeString = request.getParameter("attributeDefValueType");
      String attributeDefToEditMultiAssignable = request.getParameter("attributeDefMultiAssignable");
      String attributeDefToEditMultiValued = request.getParameter("attributeDefMultiValued");
      
      boolean multiAssignable = GrouperUtil.booleanValue(attributeDefToEditMultiAssignable, false);
      boolean multiValued = GrouperUtil.booleanValue(attributeDefToEditMultiValued, false);
      
      String attributeDefToEditAssignToAttributeDef = request.getParameter("attributeDefToEditAssignToAttributeDef");
      boolean attributeDefToEditAssignToAttributeDefBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDef, false);

      String attributeDefToEditAssignToAttributeDefAssign = request.getParameter("attributeDefToEditAssignToAttributeDefAssign");
      boolean attributeDefToEditAssignToAttributeDefAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDefAssign, false);

      String attributeDefToEditAssignToStem = request.getParameter("attributeDefToEditAssignToStem");
      boolean attributeDefToEditAssignToStemBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStem, false);

      String attributeDefToEditAssignToStemAssign = request.getParameter("attributeDefToEditAssignToStemAssign");
      boolean attributeDefToEditAssignToStemAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStemAssign, false);

      String attributeDefToEditAssignToGroup = request.getParameter("attributeDefToEditAssignToGroup");
      boolean attributeDefToEditAssignToGroupBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroup, false);

      String attributeDefToEditAssignToGroupAssign = request.getParameter("attributeDefToEditAssignToGroupAssign");
      boolean attributeDefToEditAssignToGroupAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroupAssign, false);

      String attributeDefToEditAssignToMember = request.getParameter("attributeDefToEditAssignToMember");
      boolean attributeDefToEditAssignToMemberBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMember, false);

      String attributeDefToEditAssignToMemberAssign = request.getParameter("attributeDefToEditAssignToMemberAssign");
      boolean attributeDefToEditAssignToMemberAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMemberAssign, false);

      String attributeDefToEditAssignToMembership = request.getParameter("attributeDefToEditAssignToMembership");
      boolean attributeDefToEditAssignToMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembership, false);

      String attributeDefToEditAssignToMembershipAssign = request.getParameter("attributeDefToEditAssignToMembershipAssign");
      boolean attributeDefToEditAssignToMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembershipAssign, false);

      String attributeDefToEditAssignToImmediateMembership = request.getParameter("attributeDefToEditAssignToImmediateMembership");
      boolean attributeDefToEditAssignToImmediateMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembership, false);

      String attributeDefToEditAssignToImmediateMembershipAssign = request.getParameter("attributeDefToEditAssignToImmediateMembershipAssign");
      boolean attributeDefToEditAssignToImmediateMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembershipAssign, false);
      
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorExtensionRequired")));
        return;
        
      }

      String nameOfAttributeDef = attributeDef.getName();

      
      if (!StringUtils.equals(extension, attributeDef.getExtension())) {
        
        nameOfAttributeDef = attributeDef.getParentStemName() + ":" + extension;
        
        final String NAME_OF_ATTRIBUTE_DEF = nameOfAttributeDef;
        
        //search as an admin to see if the group exists
        AttributeDef theAttributeDef = (AttributeDef)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            
            return AttributeDefFinder.findByName(NAME_OF_ATTRIBUTE_DEF, false);
          }
        });
    
        if (theAttributeDef != null) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#attributeDefId",
              TextContainer.retrieveFromRequest().getText().get("attributeDefCreateCantCreateAlreadyExists")));
          return;
        }
        
      }
      
      if (StringUtils.isBlank(attributeDefTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefTypeId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAttributeTypeRequired")));

        return;
      }

      AttributeDefType attributeDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, true);

      //make sure the type and assign to's match
      switch(attributeDefType) {
        case attr:
          //allow all
          break;
        case type:
          //type can only be non assignments
          //attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          //attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          //attributeDefToEditAssignToGroupBoolean = false;
          attributeDefToEditAssignToGroupAssignBoolean = false;
          //attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          //attributeDefToEditAssignToMembershipBoolean = false;
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          //attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case perm:
          //perm can only be group and effective membership
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          //attributeDefToEditAssignToGroupBoolean
          attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          //attributeDefToEditAssignToMembershipBoolean
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case limit:
          //limit can only be group assign and membership assign
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          attributeDefToEditAssignToGroupBoolean = false;
          //attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          attributeDefToEditAssignToMembershipBoolean = false;
          //attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
        case service:
          //service can only be  stem
          attributeDefToEditAssignToAttributeDefBoolean = false;
          attributeDefToEditAssignToAttributeDefAssignBoolean = false;
          //attributeDefToEditAssignToStemBoolean = false;
          attributeDefToEditAssignToStemAssignBoolean = false;
          attributeDefToEditAssignToGroupBoolean = false;
          attributeDefToEditAssignToGroupAssignBoolean = false;
          attributeDefToEditAssignToMemberBoolean = false;
          attributeDefToEditAssignToMemberAssignBoolean = false;
          attributeDefToEditAssignToMembershipBoolean = false;
          attributeDefToEditAssignToMembershipAssignBoolean = false;
          attributeDefToEditAssignToImmediateMembershipBoolean = false;
          attributeDefToEditAssignToImmediateMembershipAssignBoolean = false;
          break;
          
      }
      

      
      if (StringUtils.isBlank(attributeDefValueTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefValueTypeId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAttributeValueTypeRequired")));

        return;
      }

      AttributeDefValueType attributeDefValueType = AttributeDefValueType.valueOfIgnoreCase(attributeDefValueTypeString, true);

      
      //validate that at least one assign to is selected
      if (!attributeDefToEditAssignToAttributeDefBoolean && !attributeDefToEditAssignToAttributeDefAssignBoolean
          && !attributeDefToEditAssignToStemBoolean && !attributeDefToEditAssignToStemAssignBoolean
          && !attributeDefToEditAssignToGroupBoolean && !attributeDefToEditAssignToGroupAssignBoolean
          && !attributeDefToEditAssignToMemberBoolean && !attributeDefToEditAssignToMemberAssignBoolean
          && !attributeDefToEditAssignToMembershipBoolean && !attributeDefToEditAssignToMembershipAssignBoolean
          && !attributeDefToEditAssignToImmediateMembershipBoolean && !attributeDefToEditAssignToImmediateMembershipAssignBoolean) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#assignToLabelId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateErrorAssignToRequired")));
        return;
      }

      switch (attributeDefValueType) {
        case marker:
          //cant be multi valued
          multiValued = false;
          break;
          
        case floating:
        case integer:
        case memberId:
        case string:
        case timestamp:
          //do nothing
          break;
      }

      try {
  
        //create the attribute def
        AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession)
            .assignId(attributeDef.getId())
            .assignName(nameOfAttributeDef).assignSaveMode(SaveMode.UPDATE)
            .assignDescription(description).assignAttributeDefType(attributeDefType)
            .assignValueType(attributeDefValueType)
            .assignMultiAssignable(multiAssignable)
            .assignMultiValued(multiValued)
            .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
            .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
            .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked)
            .assignToAttributeDef(attributeDefToEditAssignToAttributeDefBoolean)
            .assignToAttributeDefAssn(attributeDefToEditAssignToAttributeDefAssignBoolean)
            .assignToStem(attributeDefToEditAssignToStemBoolean)
            .assignToStemAssn(attributeDefToEditAssignToStemAssignBoolean)
            .assignToGroup(attributeDefToEditAssignToGroupBoolean)
            .assignToGroupAssn(attributeDefToEditAssignToGroupAssignBoolean)
            .assignToMember(attributeDefToEditAssignToMemberBoolean)
            .assignToMemberAssn(attributeDefToEditAssignToMemberAssignBoolean)
            .assignToEffMembership(attributeDefToEditAssignToMembershipBoolean)
            .assignToEffMembershipAssn(attributeDefToEditAssignToMembershipAssignBoolean)
            .assignToImmMembership(attributeDefToEditAssignToImmediateMembershipBoolean)
            .assignToImmMembershipAssn(attributeDefToEditAssignToImmediateMembershipAssignBoolean);

        attributeDef = attributeDefSave.save();
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDef.getId() + "')"));
    
        //lets show a success message on the new screen
        if (attributeDefSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("attributeDefEditNoChangeNote")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("attributeDefEditSuccess")));
        }
      
  
      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;
  
        
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for attribute def create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error editing attributeDef: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeDef, sde);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefEditError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete attributeDef (show confirm screen)
   * @param request
   * @param response
   */
  public void attributeDefDelete(HttpServletRequest request, HttpServletResponse response) {
    
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
          "/WEB-INF/grouperUi2/attributeDef/attributeDefDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * hit submit on the delete attributeDef screen
   * @param request
   * @param response
   */
  public void attributeDefDeleteSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, false).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
      
      String stemId = attributeDef.getParentUuid();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      try {
  
        //delete the group
        attributeDef.delete();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for attributeDef delete: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //go to the view attributeDef screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDef.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefDeleteInsufficientPrivileges")));
        return;
  
      } catch (GroupDeleteException sde) {
        
        LOG.warn("Error deleting attributeDef: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeDef, sde);
        
        //go to the view attributeDef screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=" + attributeDef.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefErrorCantDelete")));
  
        return;
  
      }
      
      //go to the view stem screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stemId + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefDeleteSuccess")));
      
      GrouperUserDataApi.recentlyUsedAttributeDefRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);
  
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
  /**
   * combo filter for attribute def
   * @param request
   * @param response
   */
  public void createAttributeDefNameParentAttributeDefFilter(final HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<AttributeDef>() {
  
      /**
       * 
       */
      @Override
      public AttributeDef lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        return new AttributeDefFinder().assignPrivileges(AttributeDefPrivilege.ATTR_READ_PRIVILEGES).assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findAttribute();
      }
  
      /**
       * 
       */
      @Override
      public Collection<AttributeDef> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        String attributeAssignTypeString = request.getParameter("attributeAssignType");

        int attributeDefsComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.attributeDefsComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, attributeDefsComboSize);
        
        AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, false);
        
        if (attributeAssignType != null) {
          return GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSplitScopeSecure(query, grouperSession, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, attributeAssignType, null);
        } else {
          return GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSplitScopeSecure(query, grouperSession, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, null, null);
        }
      }
  
      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, AttributeDef t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, AttributeDef t) {
        return t.getDisplayName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, AttributeDef t) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(t.getDisplayName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/folder.gif\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }
  
}
