package edu.internet2.middleware.grouper.app.attestation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

public class AttestationGroupSave {
  
  /**
   * days before attestation to remind
   */
  private Integer daysBeforeToRemind = null;

  /**
   * days before attestation to remind
   * @return this for chaining
   */
  public AttestationGroupSave assignDaysBeforeToRemind(int theDaysBeforeToRemind) {
    daysBeforeToRemindAssigned = true;
    this.daysBeforeToRemind = theDaysBeforeToRemind;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean daysBeforeToRemindAssigned = false;
  
  /**
   * days until needs a reertify
   */
  private Integer daysUntilRecertify = null;

  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignDaysUntilRecertify(int theDaysUntilRecertify) {
    this.daysUntilRecertify = theDaysUntilRecertify;
    daysUntilRecertifyAssigned = true;
    return this;
  }
 
  
  private boolean daysUntilRecertifyAssigned = false;

  /**
   * email addresses that get a message during recertify
   */
  private Set<String> emailAddresses = new LinkedHashSet<String>();


  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignEmailAddresses(String theEmailAddresses) {
    theEmailAddresses = GrouperUtil.replace(theEmailAddresses, ";", ",");
    this.emailAddresses = GrouperUtil.splitTrimToSet(theEmailAddresses, ",");
    emailAddressesAssigned = true;
    return this;
  }

  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave addEmailAddress(String theEmailAddress) {
    if (this.emailAddresses == null) {
      this.emailAddresses = new TreeSet<String>();
    }
    this.emailAddresses.add(theEmailAddress);
    emailAddressesAssigned = true;
    return this;
  }

  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave addEmailAddress(Subject subject) {
    
    if (this.emailAddresses == null) {
      this.emailAddresses = new TreeSet<String>();
    }
    String emailAddress = GrouperEmail.retrieveEmailAddress(subject);
    if (!StringUtils.isBlank(emailAddress)) {
      this.emailAddresses.add(emailAddress);
    }
    emailAddressesAssigned = true;
    return this;
  }

  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave addEmailAddresses(Group group) {
    
    
    if (this.emailAddresses == null) {
      this.emailAddresses = new TreeSet<String>();
    }
    Set<String> emailAddresses = GrouperEmail.retrieveEmailAddresses(group, true);
    
    this.emailAddresses.addAll(GrouperUtil.nonNull(emailAddresses));

    emailAddressesAssigned = true;
    return this;
  }

  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignEmailAddresses(Set<String> theEmailAddresses) {
    this.emailAddresses = theEmailAddresses;
    emailAddressesAssigned = true;
    return this;
  }

  private boolean emailAddressesAssigned = false;

  private boolean replaceAllSettings = true;
  
  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignReplaceAllSettings(boolean theReplaceAllSettings) {
    
    this.replaceAllSettings = theReplaceAllSettings;
    return this;
  }

  
  /**
   * if the attestation should be marked as attested
   */
  private Boolean markAsAttested = null;


  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignMarkAsAttested(boolean theMarkAsAttested) {
    this.markAsAttested = theMarkAsAttested;
    return this;
  }

  /**
   * type of attestation, defaults to group
   */
  private AttestationType attestationType;


  /**
   * 
   * @return this for chaining
   */
  public AttestationGroupSave assignAttestationType(AttestationType theAttestationType) {
    attestationTypeAssigned = true;
    this.attestationType = theAttestationType;
    return this;
  }

  private boolean attestationTypeAssigned = false;
  
  /**
   * if should send email
   */
  private Boolean sendEmail;

  private boolean sendEmailAssigned = false;

  /**
   * 
   * @param theSendEmail
   * @return
   */
  public AttestationGroupSave assignSendEmail(boolean theSendEmail) {
    this.sendEmail = theSendEmail;
    this.sendEmailAssigned = true;
    return this;
  }
  
  /**
   * group
   */
  private Group group;
  
  /**
   * group id to add to, mutually exclusive with group name
   */
  private String groupId;
  /**
   * group name to add to, mutually exclusive with group id
   */
  private String groupName;

  /** save mode */
  private SaveMode saveMode;
  
  /** save type after the save */
  private SaveResultType saveResultType = null;
  
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public AttestationGroupSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }

  public AttestationGroupSave() {
    
  }

  /**
   * assign a group
   * @param theGroup
   * @return this for chaining
   */
  public AttestationGroupSave assignGroup(Group theGroup) {
    this.group = theGroup;
    return this;
  }

  /**
   * group id to add to, mutually exclusive with group name and group
   * @param theGroupId
   * @return this for chaining
   */
  public AttestationGroupSave assignGroupId(String theGroupId) {
    this.groupId = theGroupId;
    return this;
  }

  /**
   * group name to add to, mutually exclusive with group id and group
   * @param theGroupName
   * @return this for chaining
   */
  public AttestationGroupSave assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public AttestationGroupSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }

  /**
   * <pre>
   * create or update or delete a composite
   * </pre>
   * @return the composite that was updated or created or deleted
   */
  public AttributeAssign save() throws InsufficientPrivilegeException, GroupNotFoundException {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    final Group[] GROUP = new Group[1];
    
    AttributeAssign attributeAssign = (AttributeAssign)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();

          return (AttributeAssign) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              if (group == null && !StringUtils.isBlank(AttestationGroupSave.this.groupId)) {
                group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), AttestationGroupSave.this.groupId, false, new QueryOptions().secondLevelCache(false));
              } 
              if (group == null && !StringUtils.isBlank(AttestationGroupSave.this.groupName)) {
                group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), AttestationGroupSave.this.groupName, false, new QueryOptions().secondLevelCache(false));
              }
              GrouperUtil.assertion(group != null,  "Group not found");

              GROUP[0] = group;
              
              if (!runAsRoot) {
                if (!group.canHavePrivilege(SUBJECT_IN_SESSION, AccessPrivilege.UPDATE.getName(), false)) {
                  throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                    + "' cannot ADMIN group '" + group.getName() + "'");
                }
              }
              
              AttributeAssign markerAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
              
              boolean hasAttestation = GrouperUtil.booleanValue(
                  markerAttributeAssign == null ? null : markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()), false);

              boolean directAssignment = GrouperUtil.booleanValue(
                  markerAttributeAssign == null ? null : markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()), false);
              
              // handle deletes
              if (saveMode == SaveMode.DELETE) {

                if (!hasAttestation || !directAssignment) {
                  AttestationGroupSave.this.saveResultType = SaveResultType.NO_CHANGE;
                  return null;
                }
                
                group.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef());
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_DELETE, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Delete group attestation: "+group.getName());
                auditEntry.saveOrUpdate(true);
                
                AttestationGroupSave.this.saveResultType = SaveResultType.DELETE;
                
                return markerAttributeAssign;
              }
              
              if (saveMode == SaveMode.INSERT && hasAttestation && directAssignment) {
                throw new RuntimeException("Inserting attestation but it already exists!");
              }
              if (saveMode == SaveMode.UPDATE && (!hasAttestation || !directAssignment)) {
                throw new RuntimeException("Updating membership but it doesnt exist!");
              }

              boolean hasChange = false;

              // get current values

              boolean markerAttributeNewlyAssigned = markerAttributeAssign == null;
              
              if (!hasAttestation) {
                hasChange = true;
                
                if (markerAttributeNewlyAssigned) {
                  markerAttributeAssign = group.getAttributeDelegate().assignAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
                  
                }
                
                markerAttributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
              }
              if (!directAssignment) {
                hasChange = true;

                markerAttributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
              }

              hasChange = updateAttribute(hasChange, replaceAllSettings, markerAttributeAssign, markerAttributeNewlyAssigned, 
                  GrouperAttestationJob.retrieveAttributeDefNameType(), attestationType == null ? null : attestationType.name().toLowerCase(), attestationTypeAssigned);
              hasChange = updateAttribute(hasChange, replaceAllSettings, markerAttributeAssign, markerAttributeNewlyAssigned, 
                  GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind(), daysBeforeToRemind == null ? null : daysBeforeToRemind.toString(), daysBeforeToRemindAssigned);
              hasChange = updateAttribute(hasChange, replaceAllSettings, markerAttributeAssign, markerAttributeNewlyAssigned, 
                  GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify(), daysUntilRecertify == null ? null : daysUntilRecertify.toString(), daysUntilRecertifyAssigned);
              hasChange = updateAttribute(hasChange, replaceAllSettings, markerAttributeAssign, markerAttributeNewlyAssigned, 
                  GrouperAttestationJob.retrieveAttributeDefNameSendEmail(), sendEmail == null ? null : (sendEmail ? "true" : "false") , sendEmailAssigned);
              hasChange = updateAttribute(hasChange, replaceAllSettings, markerAttributeAssign, markerAttributeNewlyAssigned, 
                  GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses(), GrouperUtil.length(emailAddresses) == 0 ? null : GrouperUtil.join(emailAddresses.iterator(), ','), emailAddressesAssigned);

              
//              {
//                String daysUntilRecertifyString = markerAttributeAssign == null ? null : 
//                  markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(
//                      GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
//                
//                boolean[] madeChange = new boolean[1];
//                GrouperAttestationJob.updateCalculatedDaysLeft(markerAttributeAssign, newDateCertified, daysUntilRecertifyString, false, madeChange);
//                hasChange = hasChange || madeChange[0];
//              }

              if (!markerAttributeNewlyAssigned && !hasChange) {
                AttestationGroupSave.this.saveResultType = SaveResultType.NO_CHANGE;
                return markerAttributeAssign;
              }
              
              // insert
              if (markerAttributeNewlyAssigned) {
                AttestationGroupSave.this.saveResultType = SaveResultType.INSERT;
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_ADD, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Add group attestation: "+ group.getName());
      
              } else {

                AttestationGroupSave.this.saveResultType = SaveResultType.UPDATE;
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Update group attestation: "+group.getName());
              }

              return markerAttributeAssign;
            }
          });
          
        }
    });
    
    String newDateCertified = GrouperUtil.booleanValue(markAsAttested, false) ?  new SimpleDateFormat("yyyy/MM/dd").format(new Date()) : null;

    if (this.saveResultType == SaveResultType.NO_CHANGE && StringUtils.isBlank(newDateCertified)) {
      return attributeAssign;
    }
    
    if (this.saveResultType == SaveResultType.NO_CHANGE) {
      this.saveResultType = SaveResultType.UPDATE;
    }
    
    return attributeAssign;
    
  }

  /**
   * 
   * @param hasChange
   * @param replaceAllSettings2
   * @param markerAttributeAssign
   * @param markerAttributeNewlyAssigned
   * @param attributeDefName
   * @param object
   * @param attestationTypeAssigned2
   * @return
   */
  private static boolean updateAttribute(boolean hasChange, boolean replaceAllSettings,
      AttributeAssign markerAttributeAssign, boolean markerAttributeNewlyAssigned,
      AttributeDefName attributeDefName, String value,
      boolean thisValueAssigned) {
    
    String currentValue = markerAttributeNewlyAssigned ? null : 
      markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(
          attributeDefName.getName());

    boolean valuesDiffer = !StringUtils.equals(StringUtils.trimToNull(currentValue), StringUtils.trimToNull(value));
    
    if (valuesDiffer && StringUtils.equals(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), attributeDefName.getName())) {
      
      // compare email addresses
      currentValue = StringUtils.replace(currentValue, ";", ",");
      value = StringUtils.replace(value, ";", ",");
      Set<String> currentEmails = GrouperUtil.splitTrimToSet(currentValue, ",");
      Set<String> newEmails = GrouperUtil.splitTrimToSet(value, ",");
      
      valuesDiffer = !GrouperUtil.equalsSet(currentEmails, newEmails);
    }
    
    if (valuesDiffer) {
      if (replaceAllSettings || thisValueAssigned) {
        hasChange = true;
        if (value == null) {
          markerAttributeAssign.getAttributeDelegate().removeAttribute(attributeDefName);
        } else {
          markerAttributeAssign.getAttributeValueDelegate().assignValueString(attributeDefName.getName(), value);
        }
      }
    }
    
    return hasChange;
  }

}
