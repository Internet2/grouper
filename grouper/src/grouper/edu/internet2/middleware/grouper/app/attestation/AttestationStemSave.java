package edu.internet2.middleware.grouper.app.attestation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
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
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to add/edit/delete attestation on folders.</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * AttestationStemSave attestationStemSave = new AttestationStemSave();
 * AttributeAssign attributeAssign = attestationStemSave
 *   .assignStem(stem)
 *   .addEmailAddress("test@example.com")
 *   .assignAttestationType(AttestationType.report)
 *   .assignDaysBeforeToRemind(5)
 *   .assignDaysUntilRecertify(10)
 *   .assignSendEmail(true)
 *   .save();
 * System.out.println(attestationStemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to remove attestation from a folder
 * <blockquote>
 * <pre>
 * new AttestationStemSave()
 *  .assignStem(stem)
 *  .assignSaveMode(SaveMode.DELETE)
 *  .save();
 * </pre>
 * </blockquote>
 * </p>
 * <p> Sample call to update only one attribute
 * <blockquote>
 * <pre>
 * new AttestationStemSave()
 *  .assignStem(stem)
 *  .assignReplaceAllSettings(false)
 *  .assignSendEmail(true);
 *  .save();
 * </pre>
 * </blockquote>
 * </p>
 */
public class AttestationStemSave {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttestationStemSave attestationStemSave = null;
    
//    attestationStemSave = new AttestationStemSave().assignStemName("test");
//    attestationStemSave.save();

    attestationStemSave = new AttestationStemSave().assignStemName("test").assignDaysBeforeToRemind(15).assignStemScope(Scope.SUB);
    attestationStemSave.save();

//    attestationStemSave = new AttestationStemSave().assignStemName("test").assignSaveMode(SaveMode.DELETE);
//    attestationStemSave.save();
    
    System.out.println(attestationStemSave.getSaveResultType());
    
    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * days before attestation to remind
   */
  private Integer daysBeforeToRemind = null;

  /**
   * days before attestation to remind
   * @return this for chaining
   */
  public AttestationStemSave assignDaysBeforeToRemind(int theDaysBeforeToRemind) {
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
   * days until recertify
   * @return this for chaining
   */
  public AttestationStemSave assignDaysUntilRecertify(int theDaysUntilRecertify) {
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
   * assign email addresses (separated by semicolon)
   * @return this for chaining
   */
  public AttestationStemSave assignEmailAddresses(String theEmailAddresses) {
    theEmailAddresses = GrouperUtil.replace(theEmailAddresses, ";", ",");
    this.emailAddresses = GrouperUtil.splitTrimToSet(theEmailAddresses, ",");
    emailAddressesAssigned = true;
    return this;
  }

  /**
   * add email address
   * @return this for chaining
   */
  public AttestationStemSave addEmailAddress(String theEmailAddress) {
    if (this.emailAddresses == null) {
      this.emailAddresses = new TreeSet<String>();
    }
    this.emailAddresses.add(theEmailAddress);
    emailAddressesAssigned = true;
    return this;
  }

  /**
   * add email address of the given subject
   * @return this for chaining
   */
  public AttestationStemSave addEmailAddress(Subject subject) {
    
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
   * add email addresses from members of the given group
   * @return this for chaining
   */
  public AttestationStemSave addEmailAddresses(Group group) {
    
    
    if (this.emailAddresses == null) {
      this.emailAddresses = new TreeSet<String>();
    }
    Set<String> emailAddresses = GrouperEmail.retrieveEmailAddresses(group, true);
    
    this.emailAddresses.addAll(GrouperUtil.nonNull(emailAddresses));

    emailAddressesAssigned = true;
    return this;
  }

  /**
   * assign email addresses
   * @return this for chaining
   */
  public AttestationStemSave assignEmailAddresses(Set<String> theEmailAddresses) {
    this.emailAddresses = theEmailAddresses;
    emailAddressesAssigned = true;
    return this;
  }

  private boolean emailAddressesAssigned = false;

  /**
   * the stem scope of the attestation
   */
  private Scope stemScope = null;

  /**
   * assign stem scope for propagation
   * @return this for chaining
   */
  public AttestationStemSave assignStemScope(String theStemScope) {
    Scope scope = Scope.valueOfIgnoreCase(theStemScope, true);
    return this.assignStemScope(scope);
  }

  /**
   * assign stem scope for propagation
   * @return this for chaining
   */
  public AttestationStemSave assignStemScope(Scope theStemScope) {
    stemScopeAssigned = true;
    this.stemScope = theStemScope;
    return this;
  }

  private boolean stemScopeAssigned = false;

  /**
   * replace all existing settings. defaults to true.
   */
  private boolean replaceAllSettings = true;

  private boolean useThreadForPropagation = false;

  /**
   * assign use thread for propagation
   * @param theUseThreadForPropagation
   * @return this for chaining
   */
  public AttestationStemSave assignUseThreadForPropagation(boolean theUseThreadForPropagation) {
    this.useThreadForPropagation = theUseThreadForPropagation;
    return this;
  }
  
  /**
   * replace all existing settings. defaults to true.
   * @return this for chaining
   */
  public AttestationStemSave assignReplaceAllSettings(boolean theReplaceAllSettings) {
    
    this.replaceAllSettings = theReplaceAllSettings;
    return this;
  }

  
  /**
   * if the attestation should be marked as attested
   */
  private Boolean markAsAttested = null;


  /**
   * mark stem as attested
   * @return this for chaining
   */
  public AttestationStemSave assignMarkAsAttested(boolean theMarkAsAttested) {
    this.markAsAttested = theMarkAsAttested;
    return this;
  }

  /**
   * type of attestation, defaults to group
   */
  private AttestationType attestationType;


  /**
   * add attestation type
   * @return this for chaining
   */
  public AttestationStemSave assignAttestationType(AttestationType theAttestationType) {
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
   * assign send email
   * @param theSendEmail
   * @return
   */
  public AttestationStemSave assignSendEmail(boolean theSendEmail) {
    this.sendEmail = theSendEmail;
    this.sendEmailAssigned = true;
    return this;
  }
  
  /**
   * stem
   */
  private Stem stem;
  
  /**
   * stem id to add to, mutually exclusive with stem name
   */
  private String stemId;
  /**
   * stem name to add to, mutually exclusive with stem id
   */
  private String stemName;

  /** save mode */
  private SaveMode saveMode;
  
  /** save type after the save */
  private SaveResultType saveResultType = null;

  public AttestationStemSave() {
    
  }

  /**
   * assign a stem
   * @param theStem
   * @return this for chaining
   */
  public AttestationStemSave assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }

  /**
   * stem id to add to, mutually exclusive with stem name and stem
   * @param theStemId
   * @return this for chaining
   */
  public AttestationStemSave assignStemId(String theStemId) {
    this.stemId = theStemId;
    return this;
  }

  /**
   * stem name to add to, mutually exclusive with stem id and stem
   * @param theStemName
   * @return this for chaining
   */
  public AttestationStemSave assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public AttestationStemSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * get the save result type after save call
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public AttestationStemSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }

  /**
   * <pre>
   * create or update or delete attestation attributes on a stem
   * </pre>
   * @return the attribute assign
   */
  public AttributeAssign save() throws InsufficientPrivilegeException, GroupNotFoundException {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    final Stem[] STEM = new Stem[1];
    
    AttributeAssign attributeAssign = (AttributeAssign)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
          
          return (AttributeAssign) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              if (stem == null && !StringUtils.isBlank(AttestationStemSave.this.stemId)) {
                stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), AttestationStemSave.this.stemId, false, new QueryOptions().secondLevelCache(false));
              } 
              if (stem == null && !StringUtils.isBlank(AttestationStemSave.this.stemName)) {
                stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), AttestationStemSave.this.stemName, false, new QueryOptions().secondLevelCache(false));
              }
              GrouperUtil.assertion(stem!=null,  "Stem not found");

              STEM[0] = stem;
              
              if (!runAsRoot) {
                if (!stem.canHavePrivilege(SUBJECT_IN_SESSION, NamingPrivilege.STEM_ADMIN.getName(), false)) {
                  throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                    + "' cannot ADMIN stem '" + stem.getName() + "'");
                }
              }
              

              AttributeAssign markerAttributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
              
              boolean hasAttestation = GrouperUtil.booleanValue(
                  markerAttributeAssign == null ? null : markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()), false);

              boolean directAssignment = GrouperUtil.booleanValue(
                  markerAttributeAssign == null ? null : markerAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()), false);
              
              // handle deletes
              if (saveMode == SaveMode.DELETE) {

                if (!hasAttestation || !directAssignment) {
                  AttestationStemSave.this.saveResultType = SaveResultType.NO_CHANGE;
                  return null;
                }
                
                stem.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef());
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_DELETE, "stemId", stem.getId(), "stemName", stem.getName());
                auditEntry.setDescription("Delete stem attestation: "+stem.getName());
                auditEntry.saveOrUpdate(true);
                
                AttestationStemSave.this.saveResultType = SaveResultType.DELETE;
                
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
                  markerAttributeAssign = stem.getAttributeDelegate().assignAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
                  
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
                  GrouperAttestationJob.retrieveAttributeDefNameStemScope(), stemScope == null ? Stem.Scope.SUB.name().toLowerCase() : stemScope.name().toLowerCase() , stemScopeAssigned);
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
                AttestationStemSave.this.saveResultType = SaveResultType.NO_CHANGE;
                return markerAttributeAssign;
              }
              
              // insert
              if (markerAttributeNewlyAssigned) {
                AttestationStemSave.this.saveResultType = SaveResultType.INSERT;
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_ADD, "stemId", stem.getId(), "stemName", stem.getName());
                auditEntry.setDescription("Add stem attestation: "+ stem.getName());
      
              } else {

                AttestationStemSave.this.saveResultType = SaveResultType.UPDATE;
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_UPDATE, "stemId", stem.getId(), "stemName", stem.getName());
                auditEntry.setDescription("Update stem attestation: "+stem.getName());
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
    
    this.finished = GrouperAttestationJob.stemAttestationProcess(stem, 
        this.saveResultType == SaveResultType.DELETE ? null : attributeAssign, 
        this.saveResultType == SaveResultType.DELETE, newDateCertified, this.useThreadForPropagation);
    
    return attributeAssign;
    
  }

  /**
   * if this is finished
   */
  private boolean finished = false;
  
  /**
   * if this is finished
   * @return if finished
   */
  public boolean isFinished() {
    return finished;
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
