/**
 * @author mchyzer
 * $Id: SimpleMembershipUpdateFilter.java,v 1.6 2009-11-13 07:32:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;


/**
 * ajax methods for simple membership update filters (autocompletes)
 */
public class SimpleMembershipUpdateFilter {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(SimpleMembershipUpdateFilter.class);

  /**
   * clear the membership filter
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void clearMemberFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    simpleMembershipUpdateContainer.setMemberFilter(null);
    simpleMembershipUpdateContainer.setMemberFilterForScreen(null);
    new SimpleMembershipUpdate().retrieveMembers(httpServletRequest, httpServletResponse);
  }

  /**
   * filter groups to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterGroups(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleMembershipUpdate.errorNotEnoughGroupChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleMembershipUpdate.groupComboboxResultSize", 200), 1, true).sortAsc("theGroup.displayNameDb");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%" + searchTerm + "%", grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
        if (GrouperUtil.length(groups) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleMembershipUpdate.errorNoGroupsFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getUuid();
        String label = GrouperUiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = GrouperUiUtils.imageFromSubjectSource("g:gsa");
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleMembershipUpdate.errorTooManyGroups", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for groups: '" + GrouperUiUtils.escapeHtml(searchTerm, true) + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for groups: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * when text is typed into the combobox, this activates the list
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterMembers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
    Group group = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      boolean notEnoughChars = false;
      //minimum input length
      boolean[] tooManyResults = new boolean[]{false};
      if (StringUtils.defaultString(searchTerm).length() < 3) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            simpleMembershipUpdateContainer.getText().getErrorNotEnoughFilterChars(), null);
        notEnoughChars = true;
      } else {
        GuiPaging guiPaging = new GuiPaging();
        guiPaging.setPageNumber(1);
        int maxSubjectsDropDown = TagUtils.mediaResourceInt("simpleMembershipUpdate.subjectComboboxResultSize", 50);
        guiPaging.setPageSize(maxSubjectsDropDown);
        Set<Member> members = retrieveMembersFilter(guiPaging, group, searchTerm, tooManyResults);
        subjects = GrouperUiUtils.convertMembersToSubject(members);
      }
      
      //convert to XML for DHTMLX
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GrouperUiUtils.convertSubjectToValue(subject);
  
        String imageName = GrouperUiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GrouperUiUtils.escapeHtml(GrouperUiUtils.convertSubjectToLabelConfigured(subject), true);
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
      int maxSubjectsSort = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
      
      //this might not be correct, but probably is
      if (tooManyResults[0] || (!notEnoughChars && GrouperUtil.length(subjects) == maxSubjectsSort)) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, simpleMembershipUpdateContainer.getText().getErrorUserSearchTooManyResults(), 
            "bullet_error.png");
      } else if (!notEnoughChars && GrouperUtil.length(subjects) == 0) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", simpleMembershipUpdateContainer.getText().getErrorUserSearchNoResults(), "bullet_error.png");
      }
  
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for members: '" + searchTerm + "', " + se.getMessage(), se);

      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for members: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * called in the combobox to list the users
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unchecked")
  public void filterUsers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      QueryPaging queryPaging = null;
      
      //minimum input length
      boolean tooManyResults = false;
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            simpleMembershipUpdateContainer.getText().getErrorNotEnoughSubjectChars(), null);
      } else {
        try {
          
          final String requireGroup = simpleMembershipUpdateContainer.configValue(
              "simpleMembershipUpdate.subjectSearchRequireGroup", false);
          
          final String requireSources = simpleMembershipUpdateContainer.configValue(
              "simpleMembershipUpdate.subjectSearchRequireSources", false);
          
          if (!StringUtils.isBlank(requireSources)) {
            Set<Source> sources = GrouperUtil.convertSources(requireSources);
            subjects = SubjectFinder.findAll(searchTerm, sources);
          } else {
            subjects = SubjectFinder.findAll(searchTerm);
          }
          
          
          if (!StringUtils.isBlank(requireGroup)) {

            final Set<Subject> SUBJECTS = subjects;
            
            subjects = (Set<Subject>)GrouperSession.callbackGrouperSession(
                grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
                
                Group groupFilter = GroupFinder.findByName(rootGrouperSession, requireGroup, true);
                
                return SubjectFinder.findBySubjectsInGroup(rootGrouperSession, SUBJECTS, groupFilter, Group.getDefaultList(), null);
              }
            });
            

          }

          
          String maxSubjectsDropDownString = TagUtils.mediaResourceString("simpleMembershipUpdate.subjectComboboxResultSize");
          int maxSubjectsDropDown = GrouperUtil.intValue(maxSubjectsDropDownString, 50);
    
          queryPaging = new QueryPaging(maxSubjectsDropDown, 1, true);
        
          //sort and page the results
          subjects = GrouperUiUtils.subjectsSortedPaged(subjects, queryPaging);
        } catch (SubjectTooManyResults stmr) {
          tooManyResults = true;
        }
      }
      
      //convert to XML for DHTMLX
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GrouperUiUtils.convertSubjectToValue(subject);
  
        String imageName = GrouperUiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GrouperUiUtils.escapeHtml(GrouperUiUtils.convertSubjectToLabelConfigured(subject), true);
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //maybe add one more if we hit the limit
      if (tooManyResults || queryPaging != null && GrouperUtil.length(subjects) < queryPaging.getTotalRecordCount()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
            simpleMembershipUpdateContainer.getText().getErrorUserSearchTooManyResults(), 
            "bullet_error.png");
      } else if (GrouperUtil.length(subjects) == 0) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            simpleMembershipUpdateContainer.getText().getErrorUserSearchNoResults(), "bullet_error.png");
      }
  
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for members: '" + searchTerm + "', " + se.getMessage(), se);

      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for members: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * @param guiPaging
   * @param group
   * @param filterString filter by this string (could be sourceId__subjectId
   * @param tooManyResultsArray array of size one to pass back too many results
   * @return the set of members
   * @throws SchemaException
   */
  @SuppressWarnings("unchecked")
  Set<Member> retrieveMembersFilter(GuiPaging guiPaging, Group group, String filterString, boolean[] tooManyResultsArray)
      throws SchemaException {
    Set<Member> members = new LinkedHashSet<Member>();
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = 
      SimpleMembershipUpdateContainer.retrieveFromSession();
    
    //lets do the subject search
    if (StringUtils.defaultString(filterString).length() < TagUtils.mediaResourceInt("simpleMembershipUpdate.filterComboMinChars", 3)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getErrorNotEnoughFilterCharsAlert()));
      return members;
    } 
    
    Set<Subject> subjects = null;
    boolean filterByOneSubject = false;
    
    //lets see if there is a subject in there
    try {
      Subject subject = GrouperUiUtils.findSubject(filterString);
      if (subject != null) {
        //if there was a subject selected...
        filterByOneSubject = true;
        subjects = GrouperUtil.toSet(subject);
        //show filter string on screen
        simpleMembershipUpdateContainer.setMemberFilterForScreen(GrouperUiUtils.convertSubjectToLabelConfigured(subject));
      }
    } catch (Exception e) {
      //just ignore
    }
  
    if (!filterByOneSubject) {
      //show filter string on screen
      simpleMembershipUpdateContainer.setMemberFilterForScreen(filterString);
      boolean tooManyResults = false;
      try {
        subjects = SubjectFinder.findAll(filterString);
      } catch (SubjectTooManyResults stmr) {
        tooManyResults = true;
      }
      if (tooManyResults || GrouperUtil.length(subjects) > TagUtils.mediaResourceInt("simpleMembershipUpdate.filterMaxSearchSubjects", 1000)) {
        if (GrouperUtil.length(tooManyResultsArray) > 0) {
          tooManyResultsArray[0] = true;
        }
        simpleMembershipUpdateContainer.setMemberFilterForScreen(filterString);
        guiResponseJs.addAction(GuiScreenAction.newAlert(
            simpleMembershipUpdateContainer.getText().getErrorMemberFilterTooManyResults()));
        return members;
      }
      
    }    
      
    //lets convert these subjects to members
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    String groupName = null;
    boolean startedSession = false;
    try {
      grouperSession = GrouperSession.staticGrouperSession();
      if (grouperSession == null) {
        startedSession = true;
        grouperSession = GrouperSession.start(loggedInSubject);
      }
  
      groupName = group.getName();
      
      members = GrouperUiUtils.convertSubjectsToMembers(grouperSession, group, subjects, true);
    
    } catch (Exception se) {
      throw new RuntimeException("Error searching for members: '" + filterString 
          + "', " + groupName + ", " + se.getMessage(), se);
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    } 
      
    int maxSubjectsSort = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
  
    QueryPaging queryPaging = new QueryPaging(maxSubjectsSort, 1, true);
    
    int totalSize = members.size();
    
    int pageSize = guiPaging.getPageSize();
    
    guiPaging.setTotalRecordCount(totalSize);
    guiPaging.setPageSize(pageSize);
    
    //if there are less than the sort limit, then just get all, no problem
    queryPaging = new QueryPaging();
    queryPaging.setPageSize(pageSize);
    queryPaging.setPageNumber(guiPaging.getPageNumber());
    
    if (totalSize <= maxSubjectsSort) {
      members = GrouperUiUtils.membersSortedPaged(members, queryPaging);
    } else {
  
      members = new LinkedHashSet<Member>(GrouperUtil.batchList(members, pageSize, guiPaging.getPageNumber()-1));
    }       
    
    return members;
  }

  /**
   * retrieve members
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void retrieveMembersFilterButton(HttpServletRequest request, HttpServletResponse response) {
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = 
      SimpleMembershipUpdateContainer.retrieveFromSession();
    String simpleMembershipFilterMember = request.getParameter("simpleMembershipFilterMember");
    simpleMembershipUpdateContainer.setMemberFilter(simpleMembershipFilterMember);
    new SimpleMembershipUpdate().retrieveMembers(request, response);
  }

}
