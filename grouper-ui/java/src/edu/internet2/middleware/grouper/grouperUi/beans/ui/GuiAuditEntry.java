/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;


/**
 * @author mchyzer
 *
 */
public class GuiAuditEntry {

  /**
   * gui attribute def name for the audit if applicable
   */
  private GuiAttributeDefName guiAttributeDefName;
  
  
  
  /**
   * gui attribute def name for the audit if applicable
   * @return attr def name
   */
  public GuiAttributeDefName getGuiAttributeDefName() {
    return this.guiAttributeDefName;
  }

  /**
   * gui attribute def name for the audit if applicable
   * @param guiAttributeDefName1
   */
  public void setGuiAttributeDefName(GuiAttributeDefName guiAttributeDefName1) {
    this.guiAttributeDefName = guiAttributeDefName1;
  }

  /**
   * gui group for the audit if applicable
   */
  private GuiGroup guiGroup;


  /**
   * gui stem for the audit if applicable
   */
  private GuiStem guiStem;

  /**
   * gui stem for the audit if applicable
   * @return stem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }

  /**
   * gui stem for the audit if applicable
   * @param guiStem1
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
  }

  /**
   * gui group for the audit if applicable
   * @return the gui group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group for the audit if applicable
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }

  /**
   * gui member for the audit if applicable
   * @return the member
   */
  public GuiMember getGuiMember() {
    return this.guiMember;
  }

  /**
   * gui member for the audit if applicable
   * @param guiMember1
   */
  public void setGuiMember(GuiMember guiMember1) {
    this.guiMember = guiMember1;
  }

  /**
   * gui member for the audit if applicable
   */
  private GuiMember guiMember;
  
  /**
   * default constructor
   */
  public GuiAuditEntry() {
    
  }
  
  /**
   * 
   * @param theAuditEntry
   */
  public GuiAuditEntry(AuditEntry theAuditEntry) {
    this.auditEntry = theAuditEntry;
  }

  
  /**
   * 2/1/2013 8:03 AM
   * @return the date for screen
   */
  public String getGuiDate() {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    Locale locale = httpServletRequest.getLocale();
    DateFormat guiDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm aa", locale);
    return guiDateFormat.format(this.auditEntry.getCreatedOn());
  }
  
  /**
   * underlying audit entry
   */
  private AuditEntry auditEntry;
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(GuiAuditEntry.class);

  /**
   * underlying audit entry
   * @return audit
   */
  public AuditEntry getAuditEntry() {
    return this.auditEntry;
  }

  /**
   * underlying audit entry
   * @param auditEntry1
   */
  public void setAuditEntry(AuditEntry auditEntry1) {
    this.auditEntry = auditEntry1;
  }
  
  /**
   * enum for this audit category and action
   */
  private AuditTypeBuiltin auditTypeBuiltin = null;
  
  /**
   * enum for this audit category and action
   * @return enum
   */
  private AuditTypeBuiltin getAuditTypeBuiltin() {
    if (this.auditTypeBuiltin == null) {
      this.auditTypeBuiltin = AuditTypeBuiltin.valueOfIgnoreCase(this.auditEntry.getAuditType().getAuditCategory(), 
          this.auditEntry.getAuditType().getActionName(), false);
      
    }
    return this.auditTypeBuiltin;
  }
  
  /**
   * convert the audit to an audit line for screen
   * @return the audit line
   */
  public String getAuditLine() {

    String actionName = this.auditEntry.getAuditType().getActionName();
    String category = this.auditEntry.getAuditType().getAuditCategory();
    
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    
    if (theAuditTypeBuiltin == null) {
      LOG.error("Cant find audit builtin for category: " + category + " and action: " + actionName);
      return TextContainer.retrieveFromRequest().getText().get("auditsUndefinedAction");
    }
    
    //set this so it can be accessed from text
    GrouperRequestContainer.retrieveFromRequestOrCreate().setGuiAuditEntry(this);
    
    switch (theAuditTypeBuiltin) {
      
      case ATTRIBUTE_ASSIGN_ANYMSHIP_ADD:
        
        break;
        
      case ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE:
      
        break;
      
      case ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ASSIGN_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ASSIGN_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ASSIGN_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ATTRDEF_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ATTRDEF_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_GROUP_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_GROUP_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_GROUP_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_IMMMSHIP_ADD:

        this.setupMember();
        this.setupAttributeDefName();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_IMMMSHIP_ADD");

        
      case ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE:

        this.setupMember();
        this.setupAttributeDefName();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE");

      case ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE:
        
        this.setupMember();
        this.setupAttributeDefName();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE");
      
      case ATTRIBUTE_ASSIGN_MEMBER_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_MEMBER_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_MEMBER_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_STEM_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_STEM_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_STEM_UPDATE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_VALUE_ADD:
        
        break;
      
      case ATTRIBUTE_ASSIGN_VALUE_DELETE:
        
        break;
      
      case ATTRIBUTE_ASSIGN_VALUE_UPDATE:
        
        break;
      
      case ATTRIBUTE_DEF_ADD:
        
        break;
      
      case ATTRIBUTE_DEF_DELETE:
        
        break;
      
      case ATTRIBUTE_DEF_NAME_ADD:
        
        break;
      
      case ATTRIBUTE_DEF_NAME_DELETE:
        
        break;
      
      case ATTRIBUTE_DEF_NAME_UPDATE:
        
        break;
      
      case ATTRIBUTE_DEF_UPDATE:
        
        break;
      
      case ENTITY_ADD:
        
        break;
      
      case ENTITY_DELETE:
        
        break;
      
      case ENTITY_UPDATE:
        
        break;
      
      case EXTERNAL_SUBJ_ATTR_ADD:
        
        break;
      
      case EXTERNAL_SUBJ_ATTR_DELETE:
        
        break;
      
      case EXTERNAL_SUBJ_ATTR_UPDATE:
        
        break;
      
      case EXTERNAL_SUBJECT_ADD:
        
        break;
      
      case EXTERNAL_SUBJECT_DELETE:
        
        break;
      
      case EXTERNAL_SUBJECT_INVITE_EMAIL:
        
        break;
      
      case EXTERNAL_SUBJECT_INVITE_IDENTIFIER:
        
        break;
      
      case EXTERNAL_SUBJECT_REGISTER_ADD:
        
        break;
      
      case EXTERNAL_SUBJECT_REGISTER_DELETE:
        
        break;
      
      case EXTERNAL_SUBJECT_REGISTER_UPDATE:
        
        break;
      
      case EXTERNAL_SUBJECT_UPDATE:
        
        break;
      
      case GROUP_ADD:
        
        this.setupGroup();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ADD");

      case GROUP_ATTRIBUTE_ADD:
        
        break;
      
      case GROUP_ATTRIBUTE_DELETE:
        
        break;
      
      case GROUP_ATTRIBUTE_UPDATE:
        
        break;
      
      case GROUP_COMPOSITE_ADD:
        
        break;
      
      case GROUP_COMPOSITE_DELETE:
        
        break;
      
      case GROUP_COMPOSITE_UPDATE:
        
        break;
      
      case GROUP_COPY:
        
        break;
      
      case GROUP_DELETE:

        this.setupGroup();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_DELETE");
              
      case GROUP_FIELD_ADD:
        
        break;
      
      case GROUP_FIELD_DELETE:
        
        break;
      
      case GROUP_FIELD_UPDATE:
        
        break;
      
      case GROUP_MOVE:
        
        break;
      
      case GROUP_TYPE_ADD:
        
        break;
      
      case GROUP_TYPE_ASSIGN:
        
        break;
      
      case GROUP_TYPE_DELETE:
        
        break;
      
      case GROUP_TYPE_UNASSIGN:
        
        break;
      
      case GROUP_TYPE_UPDATE:
        
        break;
      
      case GROUP_UPDATE:

        this.setupGroup();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_UPDATE");
      
      case MEMBER_CHANGE_SUBJECT:
        
        break;
      
      case MEMBERSHIP_GROUP_ADD:
        
        this.setupGroup();
        
        this.setupMember();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_MEMBERSHIP_GROUP_ADD");
        
        // <%-- <strong>Added</strong> <a href="#">John Smith</a> as a member of 
        // the&nbsp;<a href="#" rel="tooltip" data-html="true" data-delay-show='200' 
        // data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br 
        // /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the 
        // description for this entity. Lorem ipsum dolor sit amet, consectetur 
        // adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>&nbsp;group. --%>

        
      case MEMBERSHIP_GROUP_DELETE:
        
        this.setupGroup();
        
        this.setupMember();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_MEMBERSHIP_GROUP_DELETE");
      
      case MEMBERSHIP_GROUP_UPDATE:
        
        this.setupGroup();
        
        this.setupMember();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_MEMBERSHIP_GROUP_UPDATE");
      
      case PRIVILEGE_GROUP_ADD:
        
        break;
      
      case PRIVILEGE_GROUP_DELETE:
        
        break;
      
      case PRIVILEGE_GROUP_UPDATE:
        
        break;
        
      case PRIVILEGE_STEM_ADD:
        
        this.setupStem();
        this.setupMember();
        this.setupPrivilege();
          
        return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_STEM_ADD");

      case PRIVILEGE_STEM_DELETE:
        
        this.setupStem();
        this.setupMember();
        this.setupPrivilege();
          
        return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_STEM_DELETE");
      
      case PRIVILEGE_STEM_UPDATE:
        
        this.setupStem();
        this.setupMember();
        this.setupPrivilege();
          
        return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_STEM_UPDATE");
      
      case STEM_ADD:

        this.setupStem();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_STEM_ADD");
      
      case STEM_COPY:
        
        break;
      
      case STEM_DELETE:
        
        this.setupStem();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_STEM_DELETE");
      
      case STEM_MOVE:
        
        break;
      
      case STEM_UPDATE:
        
        this.setupStem();
        
        return TextContainer.retrieveFromRequest().getText().get("audits_STEM_UPDATE");
      
      case XML_IMPORT:
        
        break;
      
      default:
        LOG.error("Cant find audit builtin for category: " + category + " and action: " + actionName);
        return TextContainer.retrieveFromRequest().getText().get("auditsUndefinedAction");
        
    }
    
    if (actionName != null) {
      return category + " - " + actionName;
    }

    LOG.error("Cant find audit builtin for category: " + category + " and action: " + actionName);
    return TextContainer.retrieveFromRequest().getText().get("auditsUndefinedAction");
    
  }

  /**
   * setup a group from an audit
   */
  private void setupGroup() {
    String groupIdName = "groupId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_ADD || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_UPDATE) {
      groupIdName = "id";
    }
    String groupId = this.auditEntry.retrieveStringValue(groupIdName);
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);
    GuiGroup guiGroup = new GuiGroup(group);
    this.setGuiGroup(guiGroup);
    
  }


  /**
   * setup a group from an audit
   */
  private void setupAttributeDefName() {
    String attributeDefNameIdName = "attributeDefNameId";
    String attributeDefNameId = this.auditEntry.retrieveStringValue(attributeDefNameIdName);
    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
    GuiAttributeDefName guiAttributeDefName = new GuiAttributeDefName(attributeDefName);
    this.setGuiAttributeDefName(guiAttributeDefName);
    
  }

  /**
   * setup a member from an audit
   */
  private void setupMember() {
    
    String memberIdName = "memberId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_ADD || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE) {
      memberIdName = "ownerMemberId";
    }
    String memberId = this.auditEntry.retrieveStringValue(memberIdName);
    Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);
    GuiMember guiMember = new GuiMember(member);
    this.setGuiMember(guiMember);
  }
  
  /**
   * privilege name for audit
   */
  private String privilegeName = null;
  
  /**
   * privilegeName for audit
   * @return the privilege name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * setup privilege
   */
  private void setupPrivilege() {

    String privilegeNameLabel = "privilegeName";
    
    this.privilegeName = this.auditEntry.retrieveStringValue(privilegeNameLabel);
    
  }
  
  /**
   * setup a group from an audit
   */
  private void setupStem() {
    String stemIdName = "stemId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.STEM_ADD || theAuditTypeBuiltin == AuditTypeBuiltin.STEM_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.STEM_UPDATE) {
      stemIdName = "id";
    }
    String stemId = this.auditEntry.retrieveStringValue(stemIdName);
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false);
    GuiStem guiStem = new GuiStem(stem);
    this.setGuiStem(guiStem);
    
  }
  
}
