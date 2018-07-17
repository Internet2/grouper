/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntityFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiEntity;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPrivilege;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class GuiAuditEntry {

  /**
   * duration in seconds
   * @return duration in seconds
   */
  public String getDurationLabel() {
    int millis = Math.round(this.auditEntry.getDurationMicroseconds()/1000);

    if (millis < 400) {
      return millis + " " + TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterColumnDurationMillis");
    }
    int seconds = millis / 1000;
    int tenths = (millis % 1000) / 100;
    return seconds + "." + tenths + " " + TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterColumnDurationSeconds");
    
  }
  
  /**
   * 
   * @param auditEntries
   * @param configMax
   * @param max
   * @return gui audit entries
   */
  public static Set<GuiAuditEntry> convertFromAuditEntries(Collection<AuditEntry> auditEntries) {
    return convertFromAuditEntries(auditEntries, null, -1);
  }

  /**
   * @param auditEntries
   * @param configMax
   * @param max
   * @return gui audit entries
   */
  public static Set<GuiAuditEntry> convertFromAuditEntries(Collection<AuditEntry> auditEntries, String configMax, int defaultMax) {
    Set<GuiAuditEntry> tempAuditEntries = new LinkedHashSet<GuiAuditEntry>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (AuditEntry auditEntry : GrouperUtil.nonNull(auditEntries)) {
      tempAuditEntries.add(new GuiAuditEntry(auditEntry));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempAuditEntries;
    
  }

  
  /** gui attribute def */
  private GuiAttributeDef guiAttributeDef;
  
  /**
   * gui attribute def
   * @return gui attribute def
   */
  public GuiAttributeDef getGuiAttributeDef() {
    return this.guiAttributeDef;
  }

  /**
   * gui attribute def
   * @param guiAttributeDef1
   */
  public void setGuiAttributeDef(GuiAttributeDef guiAttributeDef1) {
    this.guiAttributeDef = guiAttributeDef1;
  }

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
   * gui entity for the audit if applicable
   */
  private GuiEntity guiEntity;
  
  /**
   * gui entity for the audit if applicable
   * @return entity
   */
  public GuiEntity getGuiEntity() {
    return this.guiEntity;
  }

  /**
   * gui entity for the audit if applicable
   * @param guiEntity1
   */
  public void setGuiEntity(GuiEntity guiEntity1) {
    this.guiEntity = guiEntity1;
  }

  /**
   * gui privilege
   */
  private GuiPrivilege guiPrivilege;

  /**
   * gui privilege
   * @return privilege
   */
  public GuiPrivilege getGuiPrivilege() {
    return this.guiPrivilege;
  }

  /**
   * gui privilege
   * @param guiPrivilege1
   */
  public void setGuiPrivilege(GuiPrivilege guiPrivilege1) {
    this.guiPrivilege = guiPrivilege1;
  }

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
   * export size for the audit
   */
  private int exportSize;
  
  /**
   * file name for import and export
   */
  private String file;
  
  private int importTotalAdded;
  
  private int importTotalDeleted;
  
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
   * get the gui subject who performed this action
   * default to the act as id, and if null, then the logged in id
   */
  private GuiSubject guiSubjectPerformedAction;
  
  /**
   * get the gui subject who performed this action
   * default to the act as id, and if null, then the logged in id
   * @return the gui subject
   */
  public GuiSubject getGuiSubjectPerformedAction() {

    if (this.guiSubjectPerformedAction == null) {
      String memberId = this.auditEntry.getActAsMemberId();
      if (StringUtils.isBlank(memberId)) {
        memberId = this.auditEntry.getLoggedInMemberId();
      }
      if (!StringUtils.isBlank(memberId)) {
        Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(),
            memberId, false);
        Subject subject = member == null ? null : member.getSubject();
        this.guiSubjectPerformedAction = new GuiSubject(subject);
      }
    }
    
    return this.guiSubjectPerformedAction;
    
  }

  /**
   * try to get a friendly label for engine from the externalized text file
   * @return engine
   */
  public String getGrouperEngineLabel() {
    String engine = this.auditEntry.getGrouperEngine();
    
    if (StringUtils.isBlank(engine)) {
      return null;
    }
    
    String engineLabel = TextContainer.textOrNull("auditLogEngine_" + engine);
    
    return StringUtils.defaultString(engineLabel, engine);
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
    
    try {
    
      switch (theAuditTypeBuiltin) {
        
        case GROUP_ATTESTATION_ADD:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTESTATION_ADD");

        case GROUP_ATTESTATION_DELETE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTESTATION_DELETE");

        case GROUP_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE");

        case GROUP_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE");

        case GROUP_ATTESTATION_UPDATE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTESTATION_UPDATE");

        case GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE");

        case GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE:
          
          this.setupGroup();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE");

        case STEM_ATTESTATION_ADD:
          
          this.setupStem();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_ATTESTATION_ADD");

        case STEM_ATTESTATION_DELETE:
          
          this.setupStem();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_ATTESTATION_DELETE");

        case STEM_ATTESTATION_UPDATE:

          this.setupStem();

          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_ATTESTATION_UPDATE");

        case STEM_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE:

          this.setupStem();

          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE");

        case STEM_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE:

          this.setupStem();

          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE");

        case ATTRIBUTE_ASSIGN_ANYMSHIP_ADD:
          
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ANYMSHIP_ADD");
          
        case MEMBER_DEPROVISIONING:
          
          this.setupMember();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_MEMBER_DEPROVISIONING");
          
        case ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE:
        
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE");
        
        case ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE:
          
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE");
        
        case ATTRIBUTE_ASSIGN_ASSIGN_ADD:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ASSIGN_ADD");
        
        case ATTRIBUTE_ASSIGN_ASSIGN_DELETE:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ASSIGN_DELETE");
        
        case ATTRIBUTE_ASSIGN_ASSIGN_UPDATE:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ASSIGN_UPDATE");
        
        case ATTR_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE:
          
          this.setupAttributeDefName();
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTR_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE");
        
        case ATTR_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE:
          
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTR_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE");
        
        case ATTRIBUTE_ASSIGN_ATTRDEF_ADD:
          
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ATTRDEF_ADD");
        
        case ATTRIBUTE_ASSIGN_ATTRDEF_DELETE:
          
          this.setupAttributeDefName();
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ATTRDEF_DELETE");
        
        case ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE:
          
          this.setupAttributeDefName();
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE");
        
        case ATTRIBUTE_ASSIGN_GROUP_ADD:
          
          this.setupAttributeDefName();
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_GROUP_ADD");
        
        case ATTRIBUTE_ASSIGN_GROUP_DELETE:
          
          this.setupAttributeDefName();
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_GROUP_DELETE");
        
        case ATTRIBUTE_ASSIGN_GROUP_UPDATE:
          
          this.setupAttributeDefName();
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_GROUP_UPDATE");
        
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
          
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_MEMBER_ADD");
        
        case ATTRIBUTE_ASSIGN_MEMBER_DELETE:
          
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_MEMBER_DELETE");
        
        case ATTRIBUTE_ASSIGN_MEMBER_UPDATE:
          
          this.setupMember();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_MEMBER_UPDATE");
        
        case ATTRIBUTE_ASSIGN_STEM_ADD:
          
          this.setupStem();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_STEM_ADD");
        
        case ATTRIBUTE_ASSIGN_STEM_DELETE:
          
          this.setupStem();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_STEM_DELETE");
        
        case ATTRIBUTE_ASSIGN_STEM_UPDATE:
          
          this.setupStem();
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_STEM_UPDATE");
        
        case ATTRIBUTE_ASSIGN_VALUE_ADD:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_VALUE_ADD");
        
        case ATTRIBUTE_ASSIGN_VALUE_DELETE:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_VALUE_DELETE");
        
        case ATTRIBUTE_ASSIGN_VALUE_UPDATE:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_ASSIGN_VALUE_UPDATE");
        
        case ATTRIBUTE_DEF_ADD:
          
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_ADD");
        
        case ATTRIBUTE_DEF_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_DELETE");
        
        case ATTRIBUTE_DEF_NAME_ADD:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_NAME_ADD");
        
        case ATTRIBUTE_DEF_NAME_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_NAME_DELETE");
        
        case ATTRIBUTE_DEF_NAME_UPDATE:
          
          this.setupAttributeDefName();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_NAME_UPDATE");
        
        case ATTRIBUTE_DEF_UPDATE:
          
          this.setupAttributeDef();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ATTRIBUTE_DEF_UPDATE");

        case ENTITY_ADD:
          
          this.setupEntity();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ENTITY_ADD");
        
        case ENTITY_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ENTITY_DELETE");
        
        case ENTITY_UPDATE:
          
          this.setupEntity();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_ENTITY_UPDATE");
        
        case EXTERNAL_SUBJ_ATTR_ADD:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJ_ATTR_ADD");
        
        case EXTERNAL_SUBJ_ATTR_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJ_ATTR_DELETE");
        
        case EXTERNAL_SUBJ_ATTR_UPDATE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJ_ATTR_UPDATE");
        
        case EXTERNAL_SUBJECT_ADD:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_ADD");
        
        case EXTERNAL_SUBJECT_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_DELETE");
        
        case EXTERNAL_SUBJECT_INVITE_EMAIL:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_INVITE_EMAIL");
        
        case EXTERNAL_SUBJECT_INVITE_IDENTIFIER:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_INVITE_IDENTIFIER");
        
        case EXTERNAL_SUBJECT_REGISTER_ADD:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_REGISTER_ADD");
        
        case EXTERNAL_SUBJECT_REGISTER_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_REGISTER_DELETE");
        
        case EXTERNAL_SUBJECT_REGISTER_UPDATE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_REGISTER_UPDATE");
        
        case EXTERNAL_SUBJECT_UPDATE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_EXTERNAL_SUBJECT_UPDATE");
        
        case GROUP_ADD:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ADD");
  
        case GROUP_ATTRIBUTE_ADD:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTRIBUTE_ADD");
        
        case GROUP_ATTRIBUTE_DELETE:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTRIBUTE_DELETE");
        
        case GROUP_ATTRIBUTE_UPDATE:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_ATTRIBUTE_UPDATE");
        
        case GROUP_COMPOSITE_ADD:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_COMPOSITE_ADD");
        
        case GROUP_COMPOSITE_DELETE:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_COMPOSITE_DELETE");
        
        case GROUP_COMPOSITE_UPDATE:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_COMPOSITE_UPDATE");
        
        case GROUP_COPY:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_COPY");
        
        case GROUP_DELETE:
  
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_DELETE");
                
        case GROUP_FIELD_ADD:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_FIELD_ADD");
        
        case GROUP_FIELD_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_FIELD_DELETE");
        
        case GROUP_FIELD_UPDATE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_FIELD_UPDATE");
        
        case GROUP_MOVE:
          
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_MOVE");
        
        case GROUP_TYPE_ADD:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_TYPE_ADD");
        
        case GROUP_TYPE_ASSIGN:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_TYPE_ASSIGN");
        
        case GROUP_TYPE_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_TYPE_DELETE");
        
        case GROUP_TYPE_UNASSIGN:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_TYPE_UNASSIGN");
        
        case GROUP_TYPE_UPDATE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_TYPE_UPDATE");
        
        case GROUP_UPDATE:
  
          this.setupGroup();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_GROUP_UPDATE");
          
        case MEMBER_CHANGE_SUBJECT:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_MEMBER_CHANGE_SUBJECT");
        
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
          
        case MEMBERSHIP_GROUP_EXPORT:
        	
        	this.setupGroup();
        	this.setupExportProperties();
        	return TextContainer.retrieveFromRequest().getText().get("audits_MEMBERSHIP_GROUP_EXPORT");
        	
        case MEMBERSHIP_GROUP_IMPORT:
        	this.setupGroup();
        	this.setupImportProperties();
        	return TextContainer.retrieveFromRequest().getText().get("audits_MEMBERSHIP_GROUP_IMPORT");
        	
        case PRIVILEGE_GROUP_ADD:
          
          this.setupGroup();
          this.setupMember();
          this.setupPrivilege();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_GROUP_ADD");
          
        case PRIVILEGE_GROUP_DELETE:
          
          this.setupGroup();
          this.setupMember();
          this.setupPrivilege();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_GROUP_DELETE");
          
        case PRIVILEGE_GROUP_UPDATE:
          
          this.setupGroup();
          this.setupMember();
          this.setupPrivilege();
            
          return TextContainer.retrieveFromRequest().getText().get("audits_PRIVILEGE_GROUP_UPDATE");
          
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
          
          this.setupStem();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_COPY");
        
        case STEM_DELETE:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_DELETE");
        
        case STEM_MOVE:
          
          this.setupStem();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_MOVE");
        
        case STEM_UPDATE:
          
          this.setupStem();
          
          return TextContainer.retrieveFromRequest().getText().get("audits_STEM_UPDATE");
        
        case XML_IMPORT:
          
          return TextContainer.retrieveFromRequest().getText().get("audits_XML_IMPORT");
        
        default:
          LOG.error("Cant find audit builtin for category: " + category + " and action: " + actionName);
          return TextContainer.retrieveFromRequest().getText().get("auditsUndefinedAction");
          
      }
    } catch (RuntimeException re) {
      LOG.error("Problem displaying audit for category: " + category + " and action: " + actionName, re);
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
    if (theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_UPDATE) {
      groupIdName = "id";
    }
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_UPDATE) {
      groupIdName = "ownerGroupId";
    }
    if (theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_COMPOSITE_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_COMPOSITE_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_COMPOSITE_UPDATE) {
      groupIdName = "ownerId";
    }
    if (theAuditTypeBuiltin == AuditTypeBuiltin.GROUP_COPY) {
      groupIdName = "oldGroupId";
    }
    String groupId = this.auditEntry.retrieveStringValue(groupIdName);
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);
    GuiGroup guiGroup = new GuiGroup(group);
    this.setGuiGroup(guiGroup);
    
  }

  /**
   * setup an attribute def from an audit
   */
  private void setupAttributeDef() {
    String attributeDefIdName = "attributeDefId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_UPDATE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_DELETE) {
      attributeDefIdName = "id";
    }
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE) {
      attributeDefIdName = "ownerAttributeDefId";
    }

    String attributeDefId = this.auditEntry.retrieveStringValue(attributeDefIdName);
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
    GuiAttributeDef guiAttributeDef = new GuiAttributeDef(attributeDef);
    this.setGuiAttributeDef(guiAttributeDef);
    
  }

  /**
   * setup a group from an audit
   */
  private void setupAttributeDefName() {
    String attributeDefNameIdName = "attributeDefNameId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE) {
      attributeDefNameIdName = "id";
    }
    
    String attributeDefNameId = this.auditEntry.retrieveStringValue(attributeDefNameIdName);
    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
    if (attributeDefName != null) {
      GuiAttributeDefName theGuiAttributeDefName = new GuiAttributeDefName(attributeDefName);
      this.setGuiAttributeDefName(theGuiAttributeDefName);
    }    
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
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_MEMBER_UPDATE) {
      memberIdName = "ownerMemberId";
    }
    String memberId = this.auditEntry.retrieveStringValue(memberIdName);
    Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);
    GuiMember guiMember = new GuiMember(member);
    this.setGuiMember(guiMember);
  }
  
  /**
   * setup privilege
   */
  private void setupPrivilege() {

    String privilegeNameLabel = "privilegeName";
    
    String privilegeName = this.auditEntry.retrieveStringValue(privilegeNameLabel);
    
    Privilege privilege = Privilege.getInstance(privilegeName);
    
    GuiPrivilege guiPrivilege = new GuiPrivilege(privilege);
    
    this.setGuiPrivilege(guiPrivilege);
    
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
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_ADD || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_UPDATE) {
      stemIdName = "ownerStemId";
    }
    if (theAuditTypeBuiltin == AuditTypeBuiltin.STEM_COPY) {
      stemIdName = "oldStemId";
    }
    String stemId = this.auditEntry.retrieveStringValue(stemIdName);
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, false);
    GuiStem guiStem = new GuiStem(stem);
    this.setGuiStem(guiStem);
    
  }

  /**
   * setup a entity from an audit
   */
  private void setupEntity() {
    String entityIdName = "entityId";
    AuditTypeBuiltin theAuditTypeBuiltin = this.getAuditTypeBuiltin();
    if (theAuditTypeBuiltin == AuditTypeBuiltin.ENTITY_ADD 
        || theAuditTypeBuiltin == AuditTypeBuiltin.ENTITY_DELETE
        || theAuditTypeBuiltin == AuditTypeBuiltin.ENTITY_UPDATE) {
      entityIdName = "id";
    }
    String entityId = this.auditEntry.retrieveStringValue(entityIdName);
    Entity entity = new EntityFinder().addId(entityId).findEntity(false);
    GuiEntity guiEntity = new GuiEntity(entity);
    this.setGuiEntity(guiEntity);
    
  }
  
  private void setupExportProperties() {
  	int exportSize = Integer.valueOf(this.auditEntry.retrieveStringValue("exportSize"));
  	String file = this.auditEntry.retrieveStringValue("file");
  	this.file = file;
  	this.exportSize = exportSize;
  }
  
  public int getExportSize() {
    return exportSize;
  }
  
  public String getFile() {
    return file;
 }

  private void setupImportProperties() {
	  int added = Integer.valueOf(this.auditEntry.retrieveStringValue("totalAdded"));
	  int deleted = Integer.valueOf(this.auditEntry.retrieveStringValue("totalDeleted"));
	  String file = this.auditEntry.retrieveStringValue("file");
	  this.importTotalAdded = added;
	  this.importTotalDeleted = deleted;
	  this.file = file;
  }
  
  public int getImportTotalAdded() {
  	return importTotalAdded;
  }


  public int getImportTotalDeleted() {
	  return importTotalDeleted;
  }

}
