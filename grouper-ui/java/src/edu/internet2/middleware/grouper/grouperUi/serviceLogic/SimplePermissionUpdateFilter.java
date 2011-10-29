package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;

/**
 * filters on attribute update
 * @author mchyzer
 */
public class SimplePermissionUpdateFilter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimplePermissionUpdateFilter.class);

  /**
   * filter attribute defs to pick one to edit for permission type
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterPermissionAttributeDefs(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
            
      Set<AttributeDef> attributeDefs = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simplePermissionUpdate.attributeDefComboboxResultSize", 200), 1, true).sortAsc("theAttributeDef.nameDb");
        attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, null, AttributeDefType.perm);
        
        if (GrouperUtil.length(attributeDefs) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNoAttributeDefsFound", false), "bullet_error.png");
        }
      }
      
      for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
  
        String value = attributeDef.getId();
        String label = GrouperUiUtils.escapeHtml(attributeDef.getName(), true);
        String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && attributeDefs != null && queryOptions.getCount() > attributeDefs.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorTooManyAttributeDefs", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for attributeDef: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for attributeDefs: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();

  }

  /**
   * filter permission resources to pick one to assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterPermissionResources(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
        
      String attributeDefIdParam = httpServletRequest.getParameter("attributeAssignAttributeDef");
      
      //from the permission filter
      if (StringUtils.isBlank(attributeDefIdParam)) {
        attributeDefIdParam = httpServletRequest.getParameter("permissionAssignAttributeDef");
      }
      if (StringUtils.isBlank(attributeDefIdParam)) {
        attributeDefIdParam = httpServletRequest.getParameter("permissionAddAssignAttributeDef");
      }
      
      String attributeDefId = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      boolean foundError = false;
      if (!StringUtils.isBlank(attributeDefIdParam)) {
        
        try {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefIdParam, true);
          attributeDefId = attributeDef.getId();
        } catch (Exception e) {
          //this is ok, just not found
          LOG.debug(e.getMessage(), e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorCantFindAttributeDef", false), "bullet_error.png");
          foundError = true;
        }
      }
      
      if (!foundError) {
        Set<AttributeDefName> attributeDefNames = null;
        
        QueryOptions queryOptions = null;
        
        if (StringUtils.defaultString(searchTerm).length() < 2) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNotEnoughChars", false), "bullet_error.png");
        } else {
          queryOptions = new QueryOptions()
            .paging(TagUtils.mediaResourceInt("simplePermissionUpdate.permissionResourceComboboxResultSize", 200), 1, true)
            .sortAsc("theAttributeDefName.displayNameDb");
          attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              searchTerm, grouperSession, attributeDefId, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, null, AttributeDefType.perm);
          
          if (GrouperUtil.length(attributeDefNames) == 0) {
            GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
                GrouperUiUtils.message("simplePermissionUpdate.errorNoPermissionResourcesFound", false), "bullet_error.png");
          }
        }
        
        for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
    
          String value = attributeDefName.getId();
          String label = GrouperUiUtils.escapeHtml(attributeDefName.getDisplayName(), true);
          //application image
          String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
    
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
        }
    
        //add one more for more options if we didnt get them all
        if (queryOptions != null && queryOptions.getCount() != null 
            && attributeDefNames != null && queryOptions.getCount() > attributeDefNames.size()) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorTooManyPermissionResources", false), "bullet_error.png");
        }
      }      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for permission resource: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for permission resources: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
      
  }

  
  /**
   * filter roles to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterRoles(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
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
            GrouperUiUtils.message("simpleGroupUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleGroupUpdate.groupComboboxResultSize", 200), 1, true).sortAsc("theGroup.displayNameDb");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN), queryOptions, TypeOfGroup.role);
        
        if (GrouperUtil.length(groups) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNoRolesFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getId();
        String label = GrouperUiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = null;
        if (group.getTypeOfGroup() == TypeOfGroup.role) {
          imageName = GrouperUiUtils.imageFromSubjectSource("g:rsa");
        } else {
          imageName = GrouperUiUtils.imageFromSubjectSource("g:gsa");
        }
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorTooManyRoles", false), "bullet_error.png");
      }
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for role: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for roles: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * filter subjects to pick one to view/assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterSubjects(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
  
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Set<Subject> subjects = null;
      
      QueryPaging queryPaging = null;
      
      Group group = null;
      String permissionAssignRoleId = httpServletRequest.getParameter("permissionAssignRoleId");
      if (StringUtils.isBlank(permissionAssignRoleId)) {
        permissionAssignRoleId = httpServletRequest.getParameter("permissionAddAssignRoleId");
      }
      if (!StringUtils.isBlank(permissionAssignRoleId)) {
        group = GroupFinder.findByUuid(grouperSession, permissionAssignRoleId, false);
      }
      
      //minimum input length
      boolean tooManyResults = false;
      if (!StringUtils.isBlank(permissionAssignRoleId) && group == null) {
        
        //if role not found, but is in the combobox
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.assignErrorPickRole", false), null);
        
      } else if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorNotEnoughChars", false), null);
      } else {
        try {
          
          //TODO there should be a better performance way to do this
          subjects = SubjectFinder.findPage(searchTerm).getResults();            
          
          if (group != null) {
            
            if (group.getTypeOfGroup() != TypeOfGroup.role) {
              throw new RuntimeException("Why is group not a role??? " + group.getName());
            }
            subjects = SubjectFinder.findBySubjectsInGroup(grouperSession, subjects, group, null, null);
          }
          
          //dont allow groups here...  its too confusing, why would you do this?
          Iterator<Subject> iterator = subjects.iterator();
          while (iterator.hasNext()) {
            Subject subject = iterator.next();
            if (StringUtils.equals(subject.getSourceId(), "g:gsa")) {
              iterator.remove();
            }
                
          }
          
          int maxSubjectsDropDown = TagUtils.mediaResourceInt("simplePermissionUpdate.subjectComboboxResultSize", 50);
  
          queryPaging = new QueryPaging(maxSubjectsDropDown, 1, true);
        
          //sort and page the results
          subjects = GrouperUiUtils.subjectsSortedPaged(subjects, queryPaging);
  
        } catch (SubjectTooManyResults stmr) {
          tooManyResults = true;
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
              GrouperUiUtils.message("simplePermissionUpdate.errorTooManySubjects", false), 
              "bullet_error.png");
        } else if (GrouperUtil.length(subjects) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNoSubjectResults", false), 
              "bullet_error.png");
        }
    
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
   * filter actions to pick one to assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterActions(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
        
      String attributeDefIdParam = httpServletRequest.getParameter("attributeAssignAttributeDef");

      //we might be on the permissions filter screen
      if (StringUtils.isBlank(attributeDefIdParam)) {
        attributeDefIdParam = httpServletRequest.getParameter("permissionAssignAttributeDef");
      }
      
      //we might be adding a new permission
      if (StringUtils.isBlank(attributeDefIdParam)) {
        attributeDefIdParam = httpServletRequest.getParameter("permissionAddAssignAttributeDef");
      }
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      AttributeDef attributeDef = null;
      
      boolean foundError = false;
      if (!StringUtils.isBlank(attributeDefIdParam)) {
        
        try {
          attributeDef = AttributeDefFinder.findById(attributeDefIdParam, true);
        } catch (Exception e) {
          //this is ok, just not found
          LOG.debug(e.getMessage(), e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorCantFindAttributeDef", false), "bullet_error.png");
          foundError = true;
        }
      }

      String attributeDefNameIdParam = httpServletRequest.getParameter("permissionAssignAttributeName");

      if (StringUtils.isBlank(attributeDefNameIdParam)) {
        attributeDefNameIdParam = httpServletRequest.getParameter("permissionAddAssignAttributeName");
      }
      
      if (!foundError && !StringUtils.isBlank(attributeDefNameIdParam)) {
        
        try {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameIdParam, true);
          attributeDef = attributeDefName.getAttributeDef();
        } catch (Exception e) {
          //this is ok, just not found
          LOG.debug(e.getMessage(), e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorCantFindAttributeDefName", false), "bullet_error.png");
          foundError = true;
        }
      }
      
      if (!foundError && attributeDef == null) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorCantFilterByActionWithNoDefOrName", false), "bullet_error.png");
        foundError = true;
      }
      
      if (!foundError) {
        List<String> actions = new ArrayList<String>();
        
        Set<String> availableActions = attributeDef.getAttributeDefActionDelegate().allowedActionStrings();
        
        if (!StringUtils.isBlank(searchTerm)) {
        
          searchTerm = searchTerm.toLowerCase();
          
          for (String action : availableActions) {
            
            if (action.toLowerCase().contains(searchTerm)) {
              actions.add(action);
            }
            
          }

          //if none, return all
          
        }
        
        if (StringUtils.isBlank(searchTerm) || actions.size() == 0) {
          
          actions.addAll(availableActions);
          
        }
        
        Collections.sort(actions);
        
        for (String action : actions) {
    
          String value = action;
          String label = action;
          //application image
          String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
    
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
        }
    
      }      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for action: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for action: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
      
  }

  /**
   * filter permission limit names to pick one to assign
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterLimitNames(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
        
      String attributeDefIdParam = httpServletRequest.getParameter("permissionAddLimitDef");
      
      String attributeDefId = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      boolean foundError = false;
      if (!StringUtils.isBlank(attributeDefIdParam)) {
        
        try {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefIdParam, true);
          attributeDefId = attributeDef.getId();
        } catch (Exception e) {
          //this is ok, just not found
          LOG.debug(e.getMessage(), e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorCantFindAttributeDef", false), "bullet_error.png");
          foundError = true;
        }
      }
      
      if (!foundError) {
        Set<AttributeDefName> attributeDefNames = null;
        
        QueryOptions queryOptions = null;
        
        if (StringUtils.defaultString(searchTerm).length() < 2) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNotEnoughChars", false), "bullet_error.png");
        } else {
          queryOptions = new QueryOptions()
            .paging(TagUtils.mediaResourceInt("simplePermissionUpdate.permissionResourceComboboxResultSize", 200), 1, true)
            .sortAsc("theAttributeDefName.displayNameDb");
          attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              searchTerm, grouperSession, attributeDefId, loggedInSubject, 
              GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN, AttributeDefPrivilege.ATTR_UPDATE), queryOptions, null, AttributeDefType.limit);
          
          if (GrouperUtil.length(attributeDefNames) == 0) {
            GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
                GrouperUiUtils.message("simplePermissionUpdate.errorNoPermissionResourcesFound", false), "bullet_error.png");
          }
        }
        
        for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
    
          String value = attributeDefName.getId();
          String label = GrouperUiUtils.escapeHtml(attributeDefName.getDisplayName(), true);
          //application image
          String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
    
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
        }
    
        //add one more for more options if we didnt get them all
        if (queryOptions != null && queryOptions.getCount() != null 
            && attributeDefNames != null && queryOptions.getCount() > attributeDefNames.size()) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorTooManyPermissionResources", false), "bullet_error.png");
        }
      }      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for limit resource: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for limit resources: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
      
  }

  /**
   * filter limit defs to pick one to edit for permission type
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterLimitDefinitions(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
            
      Set<AttributeDef> attributeDefs = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simplePermissionUpdate.attributeDefComboboxResultSize", 200), 1, true).sortAsc("theAttributeDef.nameDb");
        attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN,AttributeDefPrivilege.ATTR_UPDATE), queryOptions, null, AttributeDefType.limit);
        
        if (GrouperUtil.length(attributeDefs) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simplePermissionUpdate.errorNoAttributeDefsFound", false), "bullet_error.png");
        }
      }
      
      for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
  
        String value = attributeDef.getId();
        String label = GrouperUiUtils.escapeHtml(attributeDef.getName(), true);
        String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && attributeDefs != null && queryOptions.getCount() > attributeDefs.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simplePermissionUpdate.errorTooManyAttributeDefs", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for attributeDef: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for attributeDefs: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }


}
