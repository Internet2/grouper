/**
 * @author mchyzer
 * $Id: SimpleMembershipUpdateFilter.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class SimpleMembershipUpdateFilter {

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
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GuiUtils.message("simpleMembershipUpdate.errorNotEnoughGroupChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleMembershipUpdate.groupComboboxResultSize", 200), 1, true).sortAsc("displayName");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%" + searchTerm + "%", grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
        if (GrouperUtil.length(groups) == 0) {
          GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GuiUtils.message("simpleMembershipUpdate.errorNoGroupsFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getUuid();
        String label = GuiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = GuiUtils.imageFromSubjectSource("g:gsa");
  
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GuiUtils.message("simpleMembershipUpdate.errorTooManyGroups", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);
  
    } catch (NoSessionException se) {
      throw se;
    } catch (Exception se) {
      throw new RuntimeException("Error searching for groups: '" + GuiUtils.escapeHtml(searchTerm, true) + "', " + se.getMessage(), se);
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
  
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
    Group group = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);
      boolean notEnoughChars = false;
      //minimum input length
      if (StringUtils.defaultString(searchTerm).length() < 3) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", GuiUtils.message("simpleMembershipUpdate.errorNotEnoughFilterChars"), null);
        notEnoughChars = true;
      } else {
        GuiPaging guiPaging = new GuiPaging();
        guiPaging.setPageNumber(1);
        int maxSubjectsDropDown = TagUtils.mediaResourceInt("simpleMembershipUpdate.subjectComboboxResultSize", 50);
        guiPaging.setPageSize(maxSubjectsDropDown);
        Set<Member> members = retrieveMembersFilter(guiPaging, group, searchTerm);
        subjects = GuiUtils.convertMembersToSubject(members);
      }
      
      //convert to XML for DHTMLX
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GuiUtils.convertSubjectToValue(subject);
  
        String imageName = GuiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GuiUtils.escapeHtml(GuiUtils.convertSubjectToLabelConfigured(subject), true);
  
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
      int maxSubjectsSort = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
      
      //this might not be correct, but probably is
      if (!notEnoughChars && GrouperUtil.length(subjects) == maxSubjectsSort) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, null, GuiUtils.message("simpleMembershipUpdate.errorUserSearchTooManyResults", false), 
            "bullet_error.png");
      } else if (!notEnoughChars && GrouperUtil.length(subjects) == 0) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", GuiUtils.message("simpleMembershipUpdate.errorUserSearchNoResults", false), "bullet_error.png");
      }
  
      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);
  
    } catch (NoSessionException se) {
      throw se;
    } catch (Exception se) {
      throw new RuntimeException("Error searching for members: '" + searchTerm + "', " + se.getMessage(), se);
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
  public void filterUsers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);
      QueryPaging queryPaging = null;
      
      //minimum input length
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GuiUtils.message("simpleMembershipUpdate.errorNotEnoughSubjectChars"), null);
      } else {
        subjects = SubjectFinder.findAll(searchTerm);
        
        String maxSubjectsDropDownString = TagUtils.mediaResourceString("simpleMembershipUpdate.subjectComboboxResultSize");
        int maxSubjectsDropDown = GrouperUtil.intValue(maxSubjectsDropDownString, 50);
  
        queryPaging = new QueryPaging(maxSubjectsDropDown, 1, true);
        
        //sort and page the results
        subjects = GuiUtils.subjectsSortedPaged(subjects, queryPaging);
        
      }
      
      //convert to XML for DHTMLX
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GuiUtils.convertSubjectToValue(subject);
  
        String imageName = GuiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GuiUtils.escapeHtml(GuiUtils.convertSubjectToLabelConfigured(subject), true);
  
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //maybe add one more if we hit the limit
      if (queryPaging != null && GrouperUtil.length(subjects) < queryPaging.getTotalRecordCount()) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, null, GuiUtils.message("simpleMembershipUpdate.errorUserSearchTooManyResults", false), 
            "bullet_error.png");
      } else if (GrouperUtil.length(subjects) == 0) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", GuiUtils.message("simpleMembershipUpdate.errorUserSearchNoResults", false), "bullet_error.png");
      }
  
      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);
  
    } catch (NoSessionException se) {
      throw se;
    } catch (Exception se) {
      throw new RuntimeException("Error searching for members: '" + searchTerm + "', " + se.getMessage(), se);
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
   * @return the set of members
   * @throws SchemaException
   */
  @SuppressWarnings("unchecked")
  Set<Member> retrieveMembersFilter(GuiPaging guiPaging, Group group, String filterString)
      throws SchemaException {
    Set<Member> members = new LinkedHashSet<Member>();
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = 
      SimpleMembershipUpdateContainer.retrieveFromSession();
    
    //lets do the subject search
    if (StringUtils.defaultString(filterString).length() < TagUtils.mediaResourceInt("simpleMembershipUpdate.filterComboMinChars", 3)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          GuiUtils.message("simpleMembershipUpdate.errorNotEnoughFilterCharsAlert")));
      return members;
    } 
    
    Set<Subject> subjects = null;
    boolean filterByOneSubject = false;
    
    //lets see if there is a subject in there
    try {
      Subject subject = GuiUtils.findSubject(filterString);
      if (subject != null) {
        //if there was a subject selected...
        filterByOneSubject = true;
        subjects = GrouperUtil.toSet(subject);
        //show filter string on screen
        simpleMembershipUpdateContainer.setMemberFilterForScreen(GuiUtils.convertSubjectToLabelConfigured(subject));
      }
    } catch (Exception e) {
      //just ignore
    }
  
    if (!filterByOneSubject) {
      //show filter string on screen
      simpleMembershipUpdateContainer.setMemberFilterForScreen(filterString);
  
      subjects = SubjectFinder.findAll(filterString);
      if (GrouperUtil.length(subjects) > TagUtils.mediaResourceInt("simpleMembershipUpdate.filterMaxSearchSubjects", 1000)) {
        simpleMembershipUpdateContainer.setMemberFilterForScreen(filterString);
        guiResponseJs.addAction(GuiScreenAction.newAlert(
            GuiUtils.message("simpleMembershipUpdate.errorMemberFilterTooManyResults")));
        return members;
      }
      
    }    
      
    //lets convert these subjects to members
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    String groupName = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      groupName = group.getName();
      
      members = GuiUtils.convertSubjectsToMembers(grouperSession, group, subjects, true);
    
    } catch (Exception se) {
      throw new RuntimeException("Error searching for members: '" + filterString 
          + "', " + groupName + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
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
      members = GuiUtils.membersSortedPaged(members, queryPaging);
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
