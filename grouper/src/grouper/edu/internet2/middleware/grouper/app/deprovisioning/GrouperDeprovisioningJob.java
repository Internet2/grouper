package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.grouper.util.EmailObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * deprovisioning daemon
 */
@DisallowConcurrentExecution
public class GrouperDeprovisioningJob extends OtherJobBase {

  /**
   * enter a group or the group which controls a loader job
   * @param group
   * @return true if group should be deprovisioned
   */
  public static boolean deprovisionGroup(Group group) {
    
    //TODO fill in logic
    
    return true;
  }
  
  /**
   * group that users who are allowed to deprovision other users are in
   * @param affiliation deprovi
   * @return the group name
   */
  public static String retrieveDeprovisioningManagersMustBeInGroupName(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_" + affiliation;

  }

  /**
   * group name which has been deprovisioned
   * @param affiliation
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned(String affiliation) {
    
    //  # e.g. managersWhoCanDeprovision_<affiliationName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + affiliation;
  }

  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_deprovisioningDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperDeprovisioningJob().run(otherJobInput);
  }

  /**
   * go through groups and folders marked with deprovisioning metadata and make sure its up to date with inheritance
   * @param stem 
   */
  public static void updateDeprovisioningMetadata(Stem stem) {

    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> grouperDeprovisioningOverallConfigurationMap 
      = GrouperDeprovisioningOverallConfiguration.retrieveConfigurationForStem(stem, true);

    for (GrouperObject grouperObject: grouperDeprovisioningOverallConfigurationMap.keySet()) {

      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfigurationMap.get(grouperObject);

      for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
        
        GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);

        // we good
        if (grouperDeprovisioningConfiguration.getOriginalConfig().isDirectAssignment()) {
          continue;
        }
        
        GrouperDeprovisioningConfiguration inheritedConfiguration = grouperDeprovisioningConfiguration.getInheritedConfig();

        if (inheritedConfiguration != null) {

          GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
          GrouperDeprovisioningAttributeValue inheritedAttributeValue = inheritedConfiguration.getOriginalConfig();

          grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(inheritedAttributeValue.getAllowAddsWhileDeprovisionedString());
          grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(inheritedAttributeValue.getAutoChangeLoaderString());
          grouperDeprovisioningAttributeValue.setAutoselectForRemovalString(inheritedAttributeValue.getAutoselectForRemovalString());
          grouperDeprovisioningAttributeValue.setDeprovisionString(inheritedAttributeValue.getDeprovisionString());
          grouperDeprovisioningAttributeValue.setDirectAssignmentString(inheritedAttributeValue.getDirectAssignmentString());
          grouperDeprovisioningAttributeValue.setEmailAddressesString(inheritedAttributeValue.getEmailAddressesString());
          grouperDeprovisioningAttributeValue.setEmailBodyString(inheritedAttributeValue.getEmailBodyString());
          grouperDeprovisioningAttributeValue.setEmailSubjectString(inheritedAttributeValue.getEmailSubjectString());
          grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString(inheritedAttributeValue.getInheritedFromFolderIdString());
          grouperDeprovisioningAttributeValue.setMailToGroupString(inheritedAttributeValue.getMailToGroupString());
          grouperDeprovisioningAttributeValue.setAffiliationString(inheritedAttributeValue.getAffiliationString());
          grouperDeprovisioningAttributeValue.setSendEmailString(inheritedAttributeValue.getSendEmailString());
          grouperDeprovisioningAttributeValue.setShowForRemovalString(inheritedAttributeValue.getShowForRemovalString());
          grouperDeprovisioningAttributeValue.setStemScopeString(inheritedAttributeValue.getStemScopeString());
          grouperDeprovisioningConfiguration.storeConfiguration();
          
        } else {

          // there is no local config or inherited config, delete it all
          grouperDeprovisioningConfiguration.setNewConfig(null);
          grouperDeprovisioningConfiguration.storeConfiguration();
        }
        
      }
    }
    
  }
  
  /**
   * get map of email addresses to email objects for stem attributes 
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildDeprovisioningStemEmails() {
  
    //TODO only look at direct assignments?  are there duplicate assignments?
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      String deprovisioningSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameSendEmail().getName());
      
//      Map<String, Set<EmailObject>> localEmailMap = stemDeprovisioningProcessHelper(attributeAssign.getOwnerStem(), attributeAssign);
//
//      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(deprovisioningSendEmail, true);
//      
//      // skip sending email for this attribute assign
//      if (!sendEmailAttributeValue) {
//        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" deprovisioningSendEmail attribute is not set to true so skipping sending email.");
//        continue;
//      }
//
//      if (sendEmailAttributeValue) {
//        mergeEmailObjects(emails, localEmailMap);
//      }
    }
    
    return emails;
    
  }

  /**
   * get the list of recently deprovisioned users
   * @param affiliation 
   * @return the list of members
   */
  public static Set<Member> retrieveRecentlyDeprovisionedUsers(final String affiliation) {
    
    //switch over to admin so attributes work
    return (Set<Member>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Group deprovisionedGroup = GroupFinder.findByName(grouperSession, retrieveGroupNameWhichHasBeenDeprovisioned(affiliation), true);
        
        Set<Member> members = deprovisionedGroup.getMembers();
        return members;
      }
    });

  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningJob.class);
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      LOG.debug("Deprovisioning is not enabled!  Quitting daemon!");
      return null;
    }
    
    return null;
  }
  
  /**
   * if this is in cache it means null and it is in cache
   */
  private static final AttributeAssign nullAttributeAssign = new AttributeAssign();
  
  /**
   * 
   * @param attributeAssignable
   * @return the id
   */
  private static String retrieveAttributeAssignableId(AttributeAssignable attributeAssignable) {
    if (attributeAssignable == null) {
      return null;
    }
    if (attributeAssignable instanceof Group) {
      return ((Group)attributeAssignable).getId();
    }
    if (attributeAssignable instanceof Stem) {
      return ((Stem)attributeAssignable).getId();
    }
    throw new RuntimeException("Not expecting object type: " + attributeAssignable.getClass() + ", " + attributeAssignable);
  }
  
  /**
   * 
   * @param attributeAssignable
   * @return the id
   */
  private static Stem retrieveAttributeAssignableParentStem(AttributeAssignable attributeAssignable) {
    if (attributeAssignable == null) {
      return null;
    }
    if (attributeAssignable instanceof Group) {
      return ((Group)attributeAssignable).getParentStem();
    }
    if (attributeAssignable instanceof Stem) {
      return ((Stem)attributeAssignable).getParentStem();
    }
    throw new RuntimeException("Not expecting object type: " + attributeAssignable.getClass() + ", " + attributeAssignable);
  }
  
  /**
   * take a stem attribute assign and process it, make sure child metadata is correct
   * @param stem is the stem the attribute is on
   * @param stemAttributeAssign
   * @return the email objects
   */
  public static Map<String, Set<EmailObject>> stemDeprovisioningProcessHelper(Stem stem, AttributeAssign stemAttributeAssign) {
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    String affiliation = stemAttributeAssign.getAttributeValueDelegate()
        .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameAffiliation().getName());
    
    String stemHasDeprovisioningString = "false";

    if (stemAttributeAssign != null) {

      stemHasDeprovisioningString = stemAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName());
        
      // it needs this if it doesnt have it (from earlier upgrade)
      if (StringUtils.isBlank(stemHasDeprovisioningString)) {
        
        stemAttributeAssign.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningAttributeNames
            .retrieveAttributeDefNameDeprovision().getName(), "true");
        stemHasDeprovisioningString = "true";
      }

    }

    String attestationStemScope = stemAttributeAssign == null ? Scope.SUB.name() : 
      stemAttributeAssign.getAttributeValueDelegate().retrieveValueString(
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDeprovision().getName());

    // go through each group and check if they have their own deprovisioning attribute and use them if they are present.
    // if not, then use the stem attributes.
    Scope scope = GrouperUtil.defaultIfNull(Scope.valueOfIgnoreCase(attestationStemScope, false), Scope.SUB);
        
    Set<Group> childGroups = stem.getChildGroups(scope);
    
    for (Group group: childGroups) {
      
      AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, 
          GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase(), false, false);
              
      if (groupAttributeAssign == null) {
        groupAttributeAssign = group.getAttributeDelegate().assignAttribute(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
      }
      
      String directAssignmentString = groupAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName());
      
      if (StringUtils.isBlank(directAssignmentString)) {
        groupAttributeAssign.getAttributeValueDelegate().assignValueString(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameDirectAssignment().getName(), "false");
        directAssignmentString = "false";
      }

      // group has direct attestation, don't use stem attributes at all.  This will be in group assignment calculations
      if (GrouperUtil.booleanValue(directAssignmentString, false)) { 
        continue;
      }
//TODO needs affiliation
      //start at stem and look for assignment, needs affiliation
      AttributeAssignable attributeAssignable = group.getParentStem().getAttributeDelegate()
        .getAttributeOrAncestorAttribute(
            GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameBase().getName(), false);

      //there is no direct assignment and no stem with attestation
      if (attributeAssignable == null) {

// TODO
//        groupAttributeAssign.getAttributeValueDelegate().assignValueString(
//            retrieveAttributeDefNameHasAttestation().getName(), "false");
//        groupAttributeAssign.getAttributeDelegate().removeAttribute(retrieveAttributeDefNameCalculatedDaysLeft());
        continue;
        
      }
      
//      //make sure its the right stem that has the assignment
//      if (!StringUtils.equals(((Stem)attributeAssignable).getName(), stem.getName())) {
//        continue;
//      }
//
//      //make sure group is in sync with stem
//      groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameHasAttestation().getName(), stemHasDeprovisioningString);
//      
//      if (!GrouperUtil.booleanValue(stemHasDeprovisioningString, true)) {
//        continue;
//      }
//
//      Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
//      singleGroupAttributeAssign.add(groupAttributeAssign);
//      
//      // skip sending email for this attribute assign
//      Map<String, Set<EmailObject>> buildAttestationGroupEmails = buildAttestationGroupEmails(stemAttributeAssign, singleGroupAttributeAssign);
//     
//      mergeEmailObjects(emails, buildAttestationGroupEmails);

    }
    
    return emails;
  }

}
