package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAttestation;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2Attestation {
  
  /**
   * group attestation https://spaces.internet2.edu/display/Grouper/Grouper+attestation
   * @param request
   * @param response
   */
  public void groupAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
      groupAttestationHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private GuiAttestation retrieveGroupAttestation(AttributeAssignable attributeAssignable, AttributeDefName attributeDefName) {
    GuiAttestation result = null;
    AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDirectAssignment");
    String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationSendEmail");
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationEmailAddresses");
    String attestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDaysUntilRecertify");
    String attestationLastEmailedDate = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationLastEmailedDate");
    String attestationDaysBeforeToRemind = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDaysBeforeToRemind");
    String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationStemScope");
    String attestationDateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDateCertified");

    if (attributeAssignable instanceof Group) {
      result = new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), attestationEmailAddresses, attestationDaysUntilRecertify,
          attestationLastEmailedDate, attestationDaysBeforeToRemind, attestationStemScope, attestationDateCertified, GrouperUtil.booleanValue(attestationDirectAssignment, false), GuiAttestation.Type.DIRECT);
    } else if (attributeAssignable instanceof Stem) {
      result = new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), attestationEmailAddresses, attestationDaysUntilRecertify,
          attestationLastEmailedDate, attestationDaysBeforeToRemind, attestationStemScope, attestationDateCertified, GrouperUtil.booleanValue(attestationDirectAssignment, false), GuiAttestation.Type.INDIRECT);
    }
    return result;
  }
  
  /**
   * @param request
   * @param response
   * @param group
   */
  private void groupAttestationHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
    if (attributeDefName == null) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
      return;
    }
    
    // group has direct attestation
    if (group.getAttributeDelegate().hasAttribute(attributeDefName)) {
      
      AttributeAssignable attributeAssignable = group.getAttributeDelegate().getAttributeOrAncestorAttribute("etc:attribute:attestation:attestation", false);
      
      GuiAttestation guiAttestation = retrieveGroupAttestation(attributeAssignable, attributeDefName);
    
      if (guiAttestation != null) {
        // if attribute directAttestation is set to false, we need to look at the closest parent stem with attestation attributes and populate the guiAttestation with that.
        if (guiAttestation.getGrouperAttestationDirectAssignment() == null || !guiAttestation.getGrouperAttestationDirectAssignment()) {
          
          Stem parentStem = group.getParentStem();
          if (parentStem.getAttributeDelegate().hasAttributeOrAncestorHasAttribute("etc:attribute:attestation:attestation", false)) {
            AttributeAssignable parentAttributeAssignable = parentStem.getAttributeDelegate().getAttributeOrAncestorAttribute("etc:attribute:attestation:attestation", false);
            // overwrite the blank/null values of group attributes with stem attributes;
            
            GuiAttestation parentGuiAttestation = retrieveGroupAttestation(parentAttributeAssignable, attributeDefName);
            
            // parent has attestation attributes??
            if (parentGuiAttestation != null) {
              // overwrite the attribute values which are not present in the group.
              
              if (guiAttestation.getGrouperAttestationSendEmail() == null) {
                guiAttestation.setGrouperAttestationSendEmail(parentGuiAttestation.getGrouperAttestationSendEmail());
              }
              
              if (guiAttestation.getGrouperAttestationEmailAddresses() == null) {
                guiAttestation.setGrouperAttestationEmailAddresses(parentGuiAttestation.getGrouperAttestationEmailAddresses());
              }
              
              if (guiAttestation.getGrouperAttestationDaysUntilRecertify() == null) {
                guiAttestation.setGrouperAttestationDaysUntilRecertify(parentGuiAttestation.getGrouperAttestationDaysUntilRecertify());
              }
              
              if (guiAttestation.getGrouperAttestationDaysBeforeToRemind() == null) {
                guiAttestation.setGrouperAttestationDaysBeforeToRemind(parentGuiAttestation.getGrouperAttestationDaysBeforeToRemind());
              }
              
            }
            
          }
          
        }
        grouperRequestContainer.getGroupContainer().setGuiAttestation(guiAttestation);
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
          "/WEB-INF/grouperUi2/group/groupViewAttestation.jsp"));
      
    } else if (group.getAttributeDelegate().hasAttributeOrAncestorHasAttribute("etc:attribute:attestation:attestation", false)) {
      
      AttributeAssignable attributeAssignable = group.getAttributeDelegate().getAttributeOrAncestorAttribute("etc:attribute:attestation:attestation", false);
      
      GuiAttestation guiAttestation = retrieveGroupAttestation(attributeAssignable, attributeDefName);
    
      if (guiAttestation != null) {
        grouperRequestContainer.getGroupContainer().setGuiAttestation(guiAttestation);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
          "/WEB-INF/grouperUi2/group/groupViewAttestation.jsp"));
      
    } else {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
          "/WEB-INF/grouperUi2/group/groupNoAttestation.jsp"));
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void updateGroupAttestationLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
      
      updateAttestationLastCertifiedDate(group);
      groupAttestationHelper(request, response, group);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_UPDATE_LAST_CERTIFIED_DATE, "groupId", group.getId(), "displayName", group.getDisplayName());
      auditEntry.setDescription("Updated last certified date attribute of group: "+group.getDisplayName());
      auditEntry.saveOrUpdate(false);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param group
   */
  private void updateAttestationLastCertifiedDate(Group group) {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
    if (!group.getAttributeDelegate().hasAttributeByName("etc:attribute:attestation:attestation")) {
      group.getAttributeDelegate().assignAttribute(attributeDefName);
    } 
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    
    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDateCertified", date);
    
    // add/update the directAssignment attribute
    String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDirectAssignment");
    if (attestationDirectAssignment == null) {
      attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDirectAssignment", "false");
    }
    attributeAssign.saveOrUpdate(false);
  }
  
  /**
   * @param request
   * @param response
   */
  public void addGroupAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return;
      }
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      GuiAttestation guiAttestation = new GuiAttestation(group, GuiAttestation.Type.DIRECT);
      grouperRequestContainer.getGroupContainer().setGuiAttestation(guiAttestation);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
          "/WEB-INF/grouperUi2/group/groupEditAttestation.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void editGroupAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = null;
    Group group = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      GuiAttestation attestation = retrieveGroupAttestation(group, attributeDefName);
      if (attestation != null) {
        grouperRequestContainer.getGroupContainer().setGuiAttestation(attestation);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
            "/WEB-INF/grouperUi2/group/groupEditAttestation.jsp"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * edit attestation attributes
   * @param request
   * @param response
   */
  public void editGroupAttestationSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
      
      boolean directAssignment = GrouperUtil.booleanValue(request.getParameter("grouperAttestationDirectAssignment[]"), false);
      boolean sendEmail = GrouperUtil.booleanValue(request.getParameter("grouperAttestationSendEmail[]"), false);
      boolean updateLastCertifiedDate = GrouperUtil.booleanValue(request.getParameter("attestationUpdateLastCertified[]"), false);
      String emailAddresses = request.getParameter("grouperAttestationEmailAddresses");
      String daysUntilRectify = request.getParameter("grouperAttestationDaysUntilRecertify");
      String daysBeforeReminder = request.getParameter("grouperAttestationDaysBeforeToRemind");
      
      if (!NumberUtils.isNumber(daysUntilRectify)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysUntilRecertify",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysUntilRectifyValidationError")));
        return;
      }
      if (!NumberUtils.isNumber(daysBeforeReminder)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysBeforeToRemind",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysBeforeReminderValidationError")));
        return;
      }
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return;
      }
      
      AuditEntry auditEntry = null;
      if (!group.getAttributeDelegate().hasAttributeByName("etc:attribute:attestation:attestation")) {
        group.getAttributeDelegate().assignAttribute(attributeDefName); // we are adding attribute here
        auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ADD_ATTESTATION, "groupId", group.getId(), "displayName", group.getDisplayName());
        auditEntry.setDescription("Add group attestation: "+group.getDisplayName());
      } else {
        auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_UPDATE_ATTESTATION, "groupId", group.getId(), "displayName", group.getDisplayName());
        auditEntry.setDescription("Update group attestation: "+group.getDisplayName());
      }
      updateGroupAttestationAttributes(group, attributeDefName, directAssignment, sendEmail, emailAddresses, daysUntilRectify, daysBeforeReminder, updateLastCertifiedDate);
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.groupAttestation&groupId=" + group.getId() + "')"));
      //groupAttestationHelper(request, response, group);
      
      auditEntry.saveOrUpdate(false);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param group
   * @param attributeDefName
   * @param sendEmail
   * @param emailAddresses
   * @param daysUntilRectify
   * @param daysBeforeReminder
   * @param updateLastCertifiedDate
   */
  private void updateGroupAttestationAttributes(Group group, AttributeDefName attributeDefName, boolean directAssignment, boolean sendEmail, 
      String emailAddresses, String daysUntilRectify, String daysBeforeReminder, boolean updateLastCertifiedDate) {
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDirectAssignment", directAssignment ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationSendEmail", sendEmail ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationEmailAddresses", emailAddresses);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDaysUntilRecertify", daysUntilRectify);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDaysBeforeToRemind", daysBeforeReminder);
    if (updateLastCertifiedDate) {
      String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
      attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDateCertified", date);
    }
    attributeAssign.saveOrUpdate(false);
  }
  
  /**
   * stem attestation
   * @param request
   * @param response
   */
  public void stemAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      stemAttestationHelper(request, response, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private GuiAttestation retrieveStemAttestation(AttributeAssignable attributeAssignable, AttributeDefName attributeDefName, GuiAttestation.Type type) {
    GuiAttestation result = null;
    AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationSendEmail");
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationEmailAddresses");
    String attestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDaysUntilRecertify");
    String attestationDaysBeforeToRemind = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDaysBeforeToRemind");
    String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationStemScope");
    
    if (attributeAssignable instanceof Stem) {
      result = new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), 
          attestationEmailAddresses, attestationDaysUntilRecertify,
          null, attestationDaysBeforeToRemind, attestationStemScope, null, false, type);
    }
    return result;
  }
  
  private void stemAttestationHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
    if (attributeDefName == null) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
      return;
    }
    
    // stem has direct attestation
    if (stem.getAttributeDelegate().hasAttribute(attributeDefName)) {
      
      AttributeAssignable attributeAssignable = stem.getAttributeDelegate().getAttributeOrAncestorAttribute("etc:attribute:attestation:attestation", false);
      
      GuiAttestation guiAttestation = retrieveStemAttestation(attributeAssignable, attributeDefName, GuiAttestation.Type.DIRECT);
    
      if (guiAttestation != null) {
        grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemViewAttestation.jsp"));
      
    } else if (stem.getAttributeDelegate().hasAttributeOrAncestorHasAttribute("etc:attribute:attestation:attestation", false)) {
      
      AttributeAssignable attributeAssignable = stem.getAttributeDelegate().getAttributeOrAncestorAttribute("etc:attribute:attestation:attestation", false);
      
      GuiAttestation guiAttestation = retrieveStemAttestation(attributeAssignable, attributeDefName, GuiAttestation.Type.INDIRECT);
    
      if (guiAttestation == null) {
        grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemViewAttestation.jsp"));
      
    } else {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemNoAttestation.jsp"));
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addStemAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return;
      }
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      GuiAttestation guiAttestation = new GuiAttestation(stem, GuiAttestation.Type.DIRECT);
      grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemEditAttestation.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void updateUncertifiedGroupAttestationLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      updateAttestationLastCertifiedDate(stem, true);
      stemAttestationHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * update the last certified date for all the groups under this folder. 
   * @param request
   * @param response
   */
  public void updateAllGroupAttestationLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      updateAttestationLastCertifiedDate(stem, false);
      stemAttestationHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param stem
   */
  private void updateAttestationLastCertifiedDate(Stem stem, boolean onlyIfNeverCertified) {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
    
    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    
    Set<Group> childGroups = stem.getChildGroups(Scope.SUB);
    
    // go through all the child groups and certify if they have attestation attributes
    for (Group group: childGroups) {
      AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
      if (attributeAssign != null) {
        if (onlyIfNeverCertified && attributeAssign.getAttributeValueDelegate().retrieveValueString("etc:attribute:attestation:attestationDateCertified") == null) {
          attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDateCertified", date);
          attributeAssign.saveOrUpdate(false);
        } else if (!onlyIfNeverCertified) {
          attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDateCertified", date);
          attributeAssign.saveOrUpdate(false);
        }
      }
    }
        
  }
  
  /**
   * @param request
   * @param response
   */
  public void editStemAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = null;
    Stem stem = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      GuiAttestation attestation = retrieveStemAttestation(stem, attributeDefName, GuiAttestation.Type.DIRECT);
      
      if (attestation != null) {
        grouperRequestContainer.getStemContainer().setGuiAttestation(attestation);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation", 
            "/WEB-INF/grouperUi2/stem/stemEditAttestation.jsp"));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void editStemAttestationSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      
      boolean sendEmail = GrouperUtil.booleanValue(request.getParameter("grouperAttestationSendEmail[]"), false);
      boolean updateLastCertifiedDate = GrouperUtil.booleanValue(request.getParameter("attestationUpdateLastCertified[]"), false);
      String emailAddresses = request.getParameter("grouperAttestationEmailAddresses");
      String daysUntilRectify = request.getParameter("grouperAttestationDaysUntilRecertify");
      String daysBeforeReminder = request.getParameter("grouperAttestationDaysBeforeToRemind");
      String stemScope = request.getParameter("levelsName");
      
      if (!NumberUtils.isNumber(daysUntilRectify)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysUntilRecertify",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysUntilRectifyValidationError")));
        return;
      }
      if (!NumberUtils.isNumber(daysBeforeReminder)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysBeforeToRemind",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysBeforeReminderValidationError")));
        return;
      }
      
      Scope scope = Scope.valueOfIgnoreCase(stemScope, false);
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:attestation:attestation", false);
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return;
      }
      
      AuditEntry auditEntry = null;
      if (!stem.getAttributeDelegate().hasAttributeByName("etc:attribute:attestation:attestation")) {
        stem.getAttributeDelegate().assignAttribute(attributeDefName); // we are adding attribute here
        auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ADD_ATTESTATION, "stemId", stem.getId(), "displayName", stem.getDisplayName());
        auditEntry.setDescription("Add stem attestation: "+stem.getDisplayName());
      } else {
        auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_UPDATE_ATTESTATION, "stemId", stem.getId(), "displayName", stem.getDisplayName());
        auditEntry.setDescription("Update stem attestation: "+stem.getDisplayName());
      }
      updateStemAttestationAttributes(stem, attributeDefName, sendEmail, emailAddresses, daysUntilRectify, daysBeforeReminder, updateLastCertifiedDate, scope);
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.stemAttestation&stemId=" + stem.getId() + "')"));
      auditEntry.saveOrUpdate(false);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  private void updateStemAttestationAttributes(Stem stem, AttributeDefName attributeDefName, boolean sendEmail, 
      String emailAddresses, String daysUntilRectify, String daysBeforeReminder, boolean updateLastCertifiedDate, Scope scope) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationSendEmail", sendEmail ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationEmailAddresses", emailAddresses);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDaysUntilRecertify", daysUntilRectify);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationDaysBeforeToRemind", daysBeforeReminder);
    attributeAssign.getAttributeValueDelegate().assignValue("etc:attribute:attestation:attestationStemScope", scope.name().toLowerCase());
    if (updateLastCertifiedDate) {
      updateAttestationLastCertifiedDate(stem, false);
    }
    attributeAssign.saveOrUpdate(false);
  }
  

}
