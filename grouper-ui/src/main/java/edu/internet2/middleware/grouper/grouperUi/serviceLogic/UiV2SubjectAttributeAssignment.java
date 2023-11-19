package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2SubjectAttributeAssignment {

  /**
   * view attributes assigned to a subject.
   * @param request
   * @param response
   */
  public void viewAttributeAssignments(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/subjectAttribute/viewSubjectAttributeAssigns.jsp"));
      
      filterHelper(member);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show attribute assignments for the given member
   * @param group
   */
  private void filterHelper(Member member) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String attributeAssignAttributeDef = null;
    String attributeAssignAttributeName = null;
    String attributeAssignGroup = null;
    String attributeAssignStem = null;
    String attributeAssignOwnerAttributeDef = null;
    String attributeAssignMembershipId = null;
    Boolean enabledDisabledBoolean = null;
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.member, 
        attributeAssignAttributeDef, attributeAssignAttributeName, attributeAssignGroup, 
        attributeAssignStem, member.getUuid(), attributeAssignOwnerAttributeDef, 
        attributeAssignMembershipId, 
        enabledDisabledBoolean, false);
    
    Set<GuiAttributeAssign> guiAttributeAssigns = new TreeSet<GuiAttributeAssign>();
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
      guiAttributeAssign.setAttributeAssign(attributeAssign);
      guiAttributeAssigns.add(guiAttributeAssign);
    }
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    grouperRequestContainer.getSubjectContainer().setGuiAttributeAssigns(guiAttributeAssigns);
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#viewAttributeAssignments",
        "/WEB-INF/grouperUi2/subjectAttribute/subjectViewAttributeAssignsContents.jsp"));
  }
  
  /**
   * assign attribute to subject.
   * @param request
   * @param response
   */
  public void assignAttributeSubmit(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
      
      final String attributeDefNameId = request.getParameter("attributeDefNameComboName");
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (StringUtils.isBlank(attributeDefNameId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#attributeDefNameComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAssignAttributeAttributeDefNameRequired")));
        return;
      }
      
      if (member == null) {
        throw new RuntimeException("why is member blank/null??");
      }
      
      final AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefNameComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAssignAttributeAttributeDefNameRequired")));
        return;
      }
         
      final boolean multiAssignable = attributeDefName.getAttributeDef().isMultiAssignable();
      if (!multiAssignable) {
        if (GrouperUtil.length(member.getAttributeDelegate().retrieveAssignments(attributeDefName)) > 0) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#attributeDefComboErrorId",
              TextContainer.retrieveFromRequest().getText().get("subjectAssignAttributeNotMultiAssignableError")));
          return;
        }
      }
      
      AttributeAssignBaseDelegate attributeDelegate = member.getAttributeDelegate();
      
      if (multiAssignable) {
        attributeDelegate.addAttribute(attributeDefName);
      } else {
        attributeDelegate.assignAttribute(attributeDefName);
      }
      
      filterHelper(member);
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectAssignAttributeSuccess")));
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectAttributeAssignments", 
          "/WEB-INF/grouperUi2/subjectAttribute/attributeAssignAddValue.jsp"));
  
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
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, true);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectAttributeAssignment.viewAttributeAssignments&subjectId=" + subject.getId() + "&sourceId=" + subject.getSourceId() + "')"));
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectAttributeAssignments", 
          "/WEB-INF/grouperUi2/subjectAttribute/subjectAttributeAssignAddMetadataAssignment.jsp"));
      
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
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(httpServletRequest, true);

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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SubjectAttributeAssignment.viewAttributeAssignments&subjectId=" + subject.getId() + "&sourceId=" + subject.getSourceId() + "')"));
      
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
      
      Member member = attributeAssign.getOwnerMember();
      if (member == null) {        
        member = attributeAssign.getOwnerAttributeAssign().getOwnerMember();
      }
      
      attributeAssign.delete();
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      filterHelper(member);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectAttributeAssignments", 
          "/WEB-INF/grouperUi2/subjectAttribute/subjectAttributeAssignEdit.jsp"));

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
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(httpServletRequest, true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);

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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/subjectAttribute/viewSubjectAttributeAssigns.jsp"));
      filterHelper(member);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

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
      
      if (values.length != 3) {
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

      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      attributeAssign.getValueDelegate().deleteValue(attributeAssignValue);
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignValueSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      
      Member member = attributeAssign.getOwnerMember();
      if (member == null) {
        member = attributeAssign.getOwnerAttributeAssign().getOwnerMember();
      }
      
      filterHelper(member);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
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
      
      if (values.length != 3) {
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectAttributeAssignments", 
          "/WEB-INF/grouperUi2/subjectAttribute/subjectAttributeAssignValueEdit.jsp"));
      
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
      
      Subject subject = UiV2Subject.retrieveSubjectHelper(httpServletRequest, true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/subjectAttribute/viewSubjectAttributeAssigns.jsp"));
      filterHelper(member);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
}
