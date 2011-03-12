package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
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
public class SimpleAttributeUpdateFilter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleAttributeUpdateFilter.class);

  /**
   * filter attribute defs to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterAttributeDefs(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
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
            GrouperUiUtils.message("simpleAttributeUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleAttributeUpdate.attributeDefComboboxResultSize", 200), 1, true).sortAsc("theAttributeDef.nameDb");
        attributeDefs = GrouperDAOFactory.getFactory().getAttributeDef().getAllAttributeDefsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
        if (GrouperUtil.length(attributeDefs) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeUpdate.errorNoAttributeDefsFound", false), "bullet_error.png");
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
            GrouperUiUtils.message("simpleAttributeUpdate.errorTooManyAttributeDefs", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for attributeDef: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for folders: " + searchTerm + ", " + se.getMessage(), true), null);
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
  public void filterPrivilegeUsers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    String attributeId = httpServletRequest.getParameter("attributeDefToEditId");

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    String searchTerm = httpServletRequest.getParameter("mask");

    boolean error = false;
    
    try {

      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      if (!error && StringUtils.isBlank(attributeId)) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false), null);
        error = true;
      }

      grouperSession = GrouperSession.start(loggedInSubject);
      
      AttributeDef attributeDef = null;

      if (!error) {
        try {
        
          attributeDef = AttributeDefFinder.findById(attributeId, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + attributeId, e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false), null);
          error = true;
          
        }
      }
      
      if (!error && !attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false), null);
        error = true;
      }
      
      if (!error) {

        attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
        
        Set<Subject> subjects = null;
        
        QueryPaging queryPaging = null;
        
        //minimum input length
        boolean tooManyResults = false;
        if (StringUtils.defaultString(searchTerm).length() < 2) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeUpdate.errorNotEnoughChars", false), null);
        } else {
          try {
            
            subjects = SubjectFinder.findAllInStem(attributeDef.getStem().getName(), searchTerm);            
            
            int maxSubjectsDropDown = TagUtils.mediaResourceInt("simpleAttributeUpdate.attributeDefPrivilegeUserComboboxResultSize", 50);

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
              GrouperUiUtils.message("simpleAttributeUpdate.errorTooManyPrivilegeSubjects", false), 
              "bullet_error.png");
        } else if (GrouperUtil.length(subjects) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeUpdate.errorPrivilegeUserSearchNoResults", false), 
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
   * add a subject to the panel below
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addPrivilegeSubject(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
            
      String additionalSubjectString = httpServletRequest.getParameter("simpleAttributeUpdatePrivilegeSubject");

      if (StringUtils.isBlank(additionalSubjectString)) {
        LOG.error("Why is subject blank?");
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.additionalPrivilegeSubjectNotFound", false)));
        return;
      }

      Subject additionalSubject = null;
      
      try {
        additionalSubject = GrouperUiUtils.findSubject(additionalSubjectString); 
      } catch (Exception e) {
        LOG.error("Error finding subject: " + additionalSubjectString, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.additionalPrivilegeSubjectNotFound", false)));
        return;
      }
      
      List<GuiMember> guiMembers = attributeUpdateRequestContainer.privilegeAdditionalGuiMembers();
      
      Member additionalMember = MemberFinder.findBySubject(grouperSession, additionalSubject, true);
      guiMembers.add(0,new GuiMember(additionalMember));
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the whole panel again
    new SimpleAttributeUpdate().attributeEditPanelPrivileges(httpServletRequest, httpServletResponse);  
      

  }  

  /**
   * edit an attribute def
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void editAttributeDefsButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    String attributeId = httpServletRequest.getParameter("simpleAttributeUpdatePickAttributeDef");

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (StringUtils.isBlank(attributeId)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
      return;
    }
    
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AttributeDef attributeDef = null;
      
      try {
      
        attributeDef = AttributeDefFinder.findById(attributeId, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + attributeId, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setCreate(false);
      

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeEditPanel.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }  

  /**
   * new attribute def
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void newAttributeDefButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AttributeDef attributeDef = new AttributeDef();
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setCreate(true);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeEditPanel.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }


  }  

   
  /**
   * filter creatable folders
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterCreatableNamespace(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Set<Stem> stems = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleAttributeUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleAttributeUpdate.attributeDefComboboxResultSize", 200), 1, true).sortAsc("theStem.nameDb");
        stems = GrouperDAOFactory.getFactory().getStem().getAllStemsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(NamingPrivilege.CREATE), queryOptions);
        
        if (GrouperUtil.length(stems) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeUpdate.errorNoFoldersFound", false), "bullet_error.png");
        }
      }
      
      for (Stem stem : GrouperUtil.nonNull(stems)) {
  
        String value = stem.getUuid();
        String label = GrouperUiUtils.escapeHtml(stem.getName(), true);
        String imageName = "../../grouperExternal/public/assets/images/folder.gif";
  
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && stems != null && queryOptions.getCount() > stems.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleAttributeUpdate.errorTooManyFolders", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for folder: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for folders: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();

  }


  
  
}


