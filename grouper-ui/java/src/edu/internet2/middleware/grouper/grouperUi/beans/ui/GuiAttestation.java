/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author vsachdeva
 *
 */
public class GuiAttestation {
  
  private AttributeAssignable attributeAssignable;
  
  private Boolean grouperAttestationSendEmail;
  
  private String grouperAttestationEmailAddresses; // list of comma separated emails
  
  private String grouperAttestationDaysUntilRecertify;
  
  /** days before attestation needed */
  private Integer grouperAttestationDaysLeftUntilRecertify;

  /**
   * gui group associated with the group the attestation is on if applicable 
   */
  private GuiGroup guiGroup;

  /**
   * gui stem associates with the stem the attestation is on if applicable
   */
  private GuiStem guiStem;

  /**
   * gui stem associated with the stem the attestation is on if applicable 
   * @return gui stem
   */
  public GuiStem getGuiStem() {
    if (this.guiStem == null) {
      if (this.attributeAssignable instanceof Stem) {
        this.guiStem = new GuiStem((Stem)this.attributeAssignable);
      }
    }
    return this.guiStem;
  }
   

  /**
   * gui group associated with the group the attestation is on if applicable 
   * @return gui group
   */
  public GuiGroup getGuiGroup() {
    if (this.guiGroup == null) {
      if (this.attributeAssignable instanceof Group) {
        this.guiGroup = new GuiGroup((Group)this.attributeAssignable);
      }
    }
    return this.guiGroup;
  }
   
  /**
   * days before attestation needed
   * @return days left before recertify
   */
  public Integer getGrouperAttestationDaysLeftUntilRecertify() {
    return this.grouperAttestationDaysLeftUntilRecertify;
  }

  /**
   * days before attestation needed
   * @param grouperAttestationDaysLeftUntilRecertify1
   */
  public void setGrouperAttestationDaysLeftUntilRecertify(
      Integer grouperAttestationDaysLeftUntilRecertify1) {
    this.grouperAttestationDaysLeftUntilRecertify = grouperAttestationDaysLeftUntilRecertify1;
  }

  private String grouperAttestationLastEmailedDate;
  
  private String grouperAttestationDaysBeforeToRemind;
  
  private String grouperAttestationStemScope;
  
  private String grouperAttestationDateCertified;
  
  private Boolean grouperAttestationDirectAssignment = false;

  private Boolean grouperAttestationHasAttestation = false;

  private Mode mode;
  
  public enum Mode {
    EDIT, ADD
  }
  
  public GuiAttestation(AttributeAssignable attributeAssignable) {
    this.mode = Mode.ADD;
    this.attributeAssignable = attributeAssignable;
  }
  
  
  public GuiAttestation(AttributeAssignable attributeAssignable, Boolean grouperAttestationSendEmail,
      Boolean grouperAttestationHasAttestation,
      String grouperAttestationEmailAddresses,
      String grouperAttestationDaysUntilRecertify,
      String grouperAttestationLastEmailedDate,
      String grouperAttestationDaysBeforeToRemind, String grouperAttestationStemScope,
      String grouperAttestationDateCertified, Boolean grouperAttestationDirectAssignment, Integer daysLeftUntilRecertify) {
    
    super();
    this.mode = Mode.EDIT;
    this.grouperAttestationHasAttestation = grouperAttestationHasAttestation;
    this.attributeAssignable = attributeAssignable;
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
    this.grouperAttestationLastEmailedDate = grouperAttestationLastEmailedDate;
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
    this.grouperAttestationStemScope = grouperAttestationStemScope;
    this.grouperAttestationDateCertified = grouperAttestationDateCertified;
    this.grouperAttestationDirectAssignment = grouperAttestationDirectAssignment;
    this.grouperAttestationDaysLeftUntilRecertify = daysLeftUntilRecertify;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Group.class);

  /**
   * if needs recertify
   * @return if needs recertify
   */
  public boolean isNeedsRecertify() {
    return needsRecertifyHelper(0);
  }

  /**
   * if has attestation
   * @return if has attestation
   */
  public boolean isHasAttestation() {
    return this.grouperAttestationHasAttestation == null || this.grouperAttestationHasAttestation;
  }
  
  /**
   * @param daysBuffer is 0 for needs recertify now, or more than that for buffer
   * @return if needs recertify
   */
  public boolean needsRecertifyHelper(int daysBuffer) {
    
    if (this.grouperAttestationHasAttestation != null && this.grouperAttestationHasAttestation == false) {
      return false;
    }
    
    int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(this.grouperAttestationDaysUntilRecertify)) {
      try {
        daysUntilRecertify = GrouperUtil.intValue(this.grouperAttestationDaysUntilRecertify);
      } catch (Exception e) {
        //swallow
      }
      
    }
    
    boolean needsRecertify = false;
    if (StringUtils.isBlank(this.grouperAttestationDateCertified)) {
      needsRecertify = true;
    } else {
      // find the difference between today's date and last certified date
      try {
        Date lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.grouperAttestationDateCertified);
        long diff = new Date().getTime() - lastCertifiedDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (diffInDays+daysBuffer > daysUntilRecertify) {
          needsRecertify = true;
        }
      } catch (ParseException e) {
        LOG.error("Could not convert "+this.grouperAttestationDateCertified+" to date. Attribute assign id is: "
            +this.getAttributeAssignable().getAttributeDelegate().getAttributeAssigns().iterator().next().getId(), e);
      }
    }
    return needsRecertify;
  }
  
  /**
   * if needs recertify soon
   * @return if the group needs recertify soon
   */
  public boolean isNeedsRecertifySoon() {

    if (this.grouperAttestationHasAttestation != null && this.grouperAttestationHasAttestation == false) {
      return false;
    }

    int daysBeforeNeeds = GrouperConfig.retrieveConfig().propertyValueInt("attestation.daysBeforeNeedsAttestationToShowButton", 14);
    return needsRecertifyHelper(daysBeforeNeeds);
  }
  
  /**
   * date this group needs recertify
   * @return the date
   */
  public String getGrouperAttestationDateNeedsCertify() {
    if (this.grouperAttestationHasAttestation != null && this.grouperAttestationHasAttestation == false) {
      return null;
    }

    int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(this.grouperAttestationDaysUntilRecertify)) {
      try {
        daysUntilRecertify = GrouperUtil.intValue(this.grouperAttestationDaysUntilRecertify);
      } catch (Exception e) {
        //swallow
      }
      
    }

    Date dateNeedsCertify = null;
    if (StringUtils.isBlank(this.grouperAttestationDateCertified)) {
      //now
      dateNeedsCertify = new Date();
    } else {
      // find the difference between today's date and last certified date
      try {
        Date lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.grouperAttestationDateCertified);
        
        Calendar lastCertifiedCalendar = new GregorianCalendar();
        lastCertifiedCalendar.setTime(lastCertifiedDate);
        
        lastCertifiedCalendar.add(Calendar.DAY_OF_YEAR, daysUntilRecertify);
        
        dateNeedsCertify = new Date(lastCertifiedCalendar.getTimeInMillis());
        
      } catch (ParseException e) {
        LOG.error("Could not convert "+this.grouperAttestationDateCertified+" to date. Attribute assign id is: "
            +this.getAttributeAssignable().getAttributeDelegate().getAttributeAssigns().iterator().next().getId(), e);
      }
    }
    return new SimpleDateFormat("yyyy/MM/dd").format(dateNeedsCertify);
  }
  
  /**
   * return the gui folder with settings
   * @return gui stem
   */
  public GuiStem getGuiFolderWithSettings() {
    AttributeAssignable attributeAssignable = this.getAttributeAssignable();
    if (attributeAssignable == null) {
      return null;
    }
    Stem stem = attributeAssignable.getAttributeDelegate().getAttributeAssigns().iterator().next().getOwnerStemFailsafe();
    if (stem == null) {
      return null;
    }
    return new GuiStem(stem);
  }
  
  public AttributeAssignable getAttributeAssignable() {
    return attributeAssignable;
  }

  public Boolean getGrouperAttestationSendEmail() {
    return grouperAttestationSendEmail;
  }
  
  public void setGrouperAttestationSendEmail(Boolean grouperAttestationSendEmail) {
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
  }

  
  /**
   * @return the grouperAttestationHasAttestation
   */
  private Boolean getGrouperAttestationHasAttestation() {
    return this.grouperAttestationHasAttestation;
  }


  public Boolean getGrouperAttestationDirectAssignment() {
    return grouperAttestationDirectAssignment;
  }

  public String getGrouperAttestationEmailAddresses() {
    return grouperAttestationEmailAddresses;
  }
  
  public void setGrouperAttestationEmailAddresses(String grouperAttestationEmailAddresses) {
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
  }


  public String getGrouperAttestationDaysUntilRecertify() {
    return grouperAttestationDaysUntilRecertify;
  }

  public void setGrouperAttestationDaysUntilRecertify(String grouperAttestationDaysUntilRecertify) {
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
  }

  public String getGrouperAttestationLastEmailedDate() {
    return grouperAttestationLastEmailedDate;
  }
  
  
  public String getGrouperAttestationDaysBeforeToRemind() {
    return grouperAttestationDaysBeforeToRemind;
  }
  
  
  public void setGrouperAttestationDaysBeforeToRemind(String grouperAttestationDaysBeforeToRemind) {
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
  }


  public String getGrouperAttestationStemScope() {
    return grouperAttestationStemScope;
  }
  
  /**
   * 
   * @return true if blank or sub
   */
  public boolean isGrouperAttestationStemScopeSub() {
    return this.grouperAttestationStemScope == null 
        || StringUtils.equalsIgnoreCase(this.grouperAttestationStemScope, Scope.SUB.toString());
  }
  
  public String getGrouperAttestationDateCertified() {
    return grouperAttestationDateCertified;
  }

  public Mode getMode() {
    return mode;
  }

  /**
   * convert groups into gui attestations
   * @param groups
   * @param attributeAssignValueFinderResult
   * @return the list of gui attestations
   */
  public static List<GuiAttestation> convertGroupIntoGuiAttestation(
      final Set<Group> groups,
      final AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "convertGroupIntoGuiAttestation");
      debugMap.put("entered", GrouperUtil.timestampToString(new Date()));
      debugMap.put("groupsSize", GrouperUtil.length(groups));
    }
    try {
      final List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
      
      final Map DEBUG_MAP = debugMap;
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          //now we have groups, assignments, assignments on assignments, and values
          int i = 0;
          for (Group group : groups) {
            
            Map<String, String> attributes = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(group.getId());
            
            String attestationDirectAssignment = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
            String attestationHasAssignment = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
            String attestationSendEmail = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName());
            String attestationEmailAddresses = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
            String attestationDaysUntilRecertify = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
            String attestationLastEmailedDate = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameEmailedDate().getName());
            String attestationDaysBeforeToRemind = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName());
            String attestationStemScope = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());
            String attestationDateCertified = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName());
        
            String daysLeftBeforeAttestation = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());

            if (LOG.isDebugEnabled()) {
              DEBUG_MAP.put("attestationDirectAssignment_" + i, attestationDirectAssignment);
              DEBUG_MAP.put("attestationHasAssignment_" + i, attestationHasAssignment);
              DEBUG_MAP.put("attestationSendEmail_" + i, attestationSendEmail);
              DEBUG_MAP.put("attestationEmailAddresses_" + i, attestationEmailAddresses);
              DEBUG_MAP.put("attestationDaysUntilRecertify_" + i, attestationDaysUntilRecertify);
              DEBUG_MAP.put("attestationLastEmailedDate_" + i, attestationLastEmailedDate);
              DEBUG_MAP.put("attestationDaysBeforeToRemind_" + i, attestationDaysBeforeToRemind);
              DEBUG_MAP.put("attestationStemScope_" + i, attestationStemScope);
              DEBUG_MAP.put("attestationDateCertified_" + i, attestationDateCertified);
              DEBUG_MAP.put("daysLeftBeforeAttestation_" + i, daysLeftBeforeAttestation);
            }

            
            if (StringUtils.isBlank(daysLeftBeforeAttestation)) {
        
              AttributeAssign attributeAssign = attributeAssignValueFinderResult.getMapOwnerIdToAttributeAssign().get(group.getId());
              
              if (LOG.isDebugEnabled()) {
                DEBUG_MAP.put("attributeAssignInNull_" + i, attributeAssign == null);
              }
              GrouperAttestationJob.updateCalculatedDaysUntilRecertify(group, attributeAssign);
        
            }
            daysLeftBeforeAttestation = attributes.get(
                GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());
            if (LOG.isDebugEnabled()) {
              DEBUG_MAP.put("daysLeftBeforeAttestation2_" + i, daysLeftBeforeAttestation);
            }
            Integer daysLeft = null;
            try {
              daysLeft = GrouperUtil.intValue(daysLeftBeforeAttestation);
            } catch (Exception e) {
              LOG.error("Invalid days left: '" + daysLeftBeforeAttestation + "' for group: " + group.getName());
            }
            GuiAttestation guiAttestation = new GuiAttestation(group, GrouperUtil.booleanObjectValue(attestationSendEmail), 
                GrouperUtil.booleanObjectValue(attestationHasAssignment),
                attestationEmailAddresses, attestationDaysUntilRecertify,
                attestationLastEmailedDate, attestationDaysBeforeToRemind, attestationStemScope, attestationDateCertified, 
                GrouperUtil.booleanValue(attestationDirectAssignment, false), daysLeft);
        
            guiAttestations.add(guiAttestation);
            
            i++;
          }
          
          return null;
        }
      });
      
      return guiAttestations;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }


  /**
   * convert stems into gui attestations
   * @param stems
   * @param attributeAssignValueFinderResult
   * @return the list of gui attestations
   */
  public static List<GuiAttestation> convertStemIntoGuiAttestation(
      Set<Stem> stems,
      AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
    
    //now we have groups, assignments, assignments on assignments, and values
    for (Stem stem : stems) {
      
      Map<String, String> attributes = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(stem.getId());
      
      String attestationSendEmail = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName());
      String attestationHasAssignment = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName());
      String attestationEmailAddresses = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
      String attestationDaysUntilRecertify = attributes.get(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
      String attestationDaysBeforeToRemind = attributes.get(
          GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName());
      String attestationStemScope = attributes.get(
          GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());
 
      GuiAttestation guiAttestation = new GuiAttestation(stem, GrouperUtil.booleanObjectValue(attestationSendEmail), 
          GrouperUtil.booleanObjectValue(attestationHasAssignment),
          attestationEmailAddresses, attestationDaysUntilRecertify,
          null, attestationDaysBeforeToRemind, attestationStemScope, null, 
          null, null);
 
      guiAttestations.add(guiAttestation);
      
    }
    return guiAttestations;
  }


  
}
