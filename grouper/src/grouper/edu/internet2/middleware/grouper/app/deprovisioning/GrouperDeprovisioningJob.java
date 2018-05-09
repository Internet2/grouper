package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.EmailObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

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
   * @param realm deprovi
   * @return the group name
   */
  public static String retrieveDeprovisioningManagersMustBeInGroupName(String realm) {
    
    //  # e.g. managersWhoCanDeprovision_<realmName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<realmName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_" + realm;

  }

  /**
   * group name which has been deprovisioned
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned(String realm) {
    
    //  # e.g. managersWhoCanDeprovision_<realmName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<realmName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return GrouperDeprovisioningSettings.deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + realm;
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
   */
  public static void updateDeprovisioningMetadata() {
    
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
        null, GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameValueDef().getId(), null, 
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
   * @param realm 
   * @return the list of members
   */
  public static Set<Member> retrieveRecentlyDeprovisionedUsers(final String realm) {
    
    //switch over to admin so attributes work
    return (Set<Member>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Group deprovisionedGroup = GroupFinder.findByName(grouperSession, retrieveGroupNameWhichHasBeenDeprovisioned(realm), true);
        
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
  
  /** attribute assign cache */
  private static ExpirableCache<String, AttributeAssign> attributeAssignCache = new ExpirableCache<String, AttributeAssign>(1);
  
  /**
   * if this is in cache it means null and it is in cache
   */
  private static final AttributeAssign nullAttributeAssign = new AttributeAssign();
  
  /**
   * clear the attribute assign cache
   */
  public static void clearAttributeAssignCache() {
    attributeAssignCache.clear();
  }

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
   * get the attribute assign base for this object or a parent
   * @return the attribute assign base
   */
  public static AttributeAssign attributeAssignBase(AttributeAssignable attributeAssignable) {

    String id = retrieveAttributeAssignableId(attributeAssignable);
    AttributeAssign attributeAssign = attributeAssignCache.get(id);
    
    if (attributeAssign != null) {
      return attributeAssign == nullAttributeAssign ? null : attributeAssign;
    }
    
    AttributeAssign attributeAssignBase = attributeAssignable.getAttributeDelegate().retrieveAssignment(
        null, GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameValueDef(), false, true);

    if (attributeAssignBase == null) {
    
      Stem parentStem = retrieveAttributeAssignableParentStem(attributeAssignable);
      
      if (parentStem != null) {
        attributeAssignable = parentStem.getAttributeDelegate()
            .getAttributeOrAncestorAttribute(GrouperDeprovisioningAttributeNames.retrieveAttributeDefNameValueDef().getName(), false);
      }      
    }

    attributeAssignCache.put(id, attributeAssign == null ? nullAttributeAssign : attributeAssign);
    
    return attributeAssign;
  }
  
  /**
   * 
   * @param group
   * @return if marked for deprovisioning
   */
  public static boolean groupMarkedForDeprovisioning(Group group) {
    
    AttributeAssign attributeAssignBase = attributeAssignBase(group);
    
    //String directAssignmentString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());
    // TODO
    return false;
    
  }
  
}
