package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.util.EmailObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * attestation daemon
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
  
  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);
  
  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDefName retrieveAttributeDefNameFromDbOrCache(final String name) {
    
    AttributeDefName attributeDefName = attributeDefNameCache.get(name);

    if (attributeDefName == null) {
      
      attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefNameFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDefName == null) {
        return null;
      }
      attributeDefNameCache.put(name, attributeDefName);
    }
    
    return attributeDefName;
  }
  
  /** attribute def cache */
  private static ExpirableCache<String, AttributeDef> attributeDefCache = new ExpirableCache<String, AttributeDef>(5);
  
  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDef retrieveAttributeDefFromDbOrCache(final String name) {
    
    AttributeDef attributeDef = attributeDefCache.get(name);

    if (attributeDef == null) {
      
      attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDef == null) {
        return null;
      }
      attributeDefCache.put(name, attributeDef);
    }
    
    return attributeDef;
  }
  

  /**
   * custom email body for emails, if blank use the default configured body. 
   * Note there are template variables $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  public static final String DEPROVISIONING_EMAIL_BODY = "deprovisioningEmailBody";
  
  /**
   * email body attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailBody() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_EMAIL_BODY);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email body attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * custom subject for emails, if blank use the default configured subject. 
   * Note there are template variables $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  public static final String DEPROVISIONING_EMAIL_SUBJECT = "deprovisioningEmailSubject";
    
  /**
   * email subject attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailSubject() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_EMAIL_SUBJECT);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email subject attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String DEPROVISIONING_DEF = "deprovisioningDef";

  /**
   * attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDef() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_DEF);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning def attribute def name be found?");
    }
    return attributeDefName;
  }

  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String DEPROVISIONING_VALUE_DEF = "deprovisioning";

  /**
   * attribute value def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameValueDef() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_VALUE_DEF);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning def attribute value def name be found?");
    }
    return attributeDefName;
  }

  /**
   * deprovisioning deprovision attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDeprovision() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_DEPROVISION);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning deprovision attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * if this object should be in consideration for the deprovisioning system.
   * can be: blank, true, or false.  Defaults to true
   */
  public static final String DEPROVISIONING_DEPROVISION = "deprovisioningDeprovision";
  

  /**
   * deprovisioning stem scope attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameStemScope() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_STEM_SCOPE);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning stem scope attribute def name be found?");
    }
    return attributeDefName;

  }

   
  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   */
  public static final String DEPROVISIONING_INHERITED_FROM_FOLDER_ID = "deprovisioningInheritedFromFolderId";

  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameInheritedFromFolderId() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_INHERITED_FROM_FOLDER_ID);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning inherited from folder id attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * If configuration is assigned to a folder, then this is "one" or "sub".  "one" means only applicable to objects
   * directly in this folder.  "sub" (default) means applicable to all objects in this folder and
   * subfolders.  Note, the inheritance stops when a sub folder or object has configuration assigned.
   */
  public static final String DEPROVISIONING_STEM_SCOPE = "deprovisioningStemScope";

  /**
   * deprovisioning direct assignment attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_DIRECT_ASSIGNMENT);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning direct assignment attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * if deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   */
  public static final String DEPROVISIONING_DIRECT_ASSIGNMENT = "deprovisioningDirectAssignment";

  /**
   * deprovisioning auto change loader attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAutoChangeLoader() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_AUTO_CHANGE_LOADER);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning auto change loader attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * If this is a loader job, if being in a deprovisioned job means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   */
  public static final String DEPROVISIONING_AUTO_CHANGE_LOADER = "deprovisioningAutoChangeLoader";

  /**
   * deprovisioning auto-select for removal attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAutoSelectForRemoval() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_AUTOSELECT_FOR_REMOVAL);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning auto select for removal attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   */
  public static final String DEPROVISIONING_AUTOSELECT_FOR_REMOVAL = "deprovisioningAutoselectForRemoval";
  
  /**
   * deprovisioning show for removal attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameShowForRemoval() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_SHOW_FOR_REMOVAL);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning show for removal attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * If the deprovisioning screen should show this object if the user as an assignment.
   * can be: blank, true, or false.  If blank, will default to true unless auto change loader is false.
   */
  public static final String DEPROVISIONING_SHOW_FOR_REMOVAL = "deprovisioningShowForRemoval";
  
  /**
   * deprovisioning allow adds while deprovisioned attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameAllowAddsWhileDeprovisioned() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_ALLOW_ADDS_WHILE_DEPROVISIONED);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning allow adds while deprovisioned attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   */
  public static final String DEPROVISIONING_ALLOW_ADDS_WHILE_DEPROVISIONED = "deprovisioningAllowAddsWhileDeprovisioned";
  
  /**
   * deprovisioning send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameSendEmail() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_SEND_EMAIL);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning send email attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * required, is the realm for this metadata
   */
  public static final String DEPROVISIONING_REALM = "deprovisioningRealm";
  
  /**
   * deprovisioning send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameRealm() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_REALM);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning realm attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * If this is true, then send an email about the deprovisioning event.  If the assignments were removed, then give a description of the action.  
   * If assignments were not removed, then remind the managers to unassign.  Can be <blank>, true, or false.  Defaults to false unless the assignments 
   * were not removed.
   */
  public static final String DEPROVISIONING_SEND_EMAIL = "deprovisioningSendEmail";

  /**
   * deprovisioning email addresses attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailAddresses() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_EMAIL_ADDRESSES);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning email addresses attribute def name be found?");
    }
    return attributeDefName;

  }

  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   */
  public static final String DEPROVISIONING_EMAIL_ADDRESSES = "deprovisioningEmailAddresses";

  /**
   * deprovisioning mail to group attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameMailToGroup() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + DEPROVISIONING_MAIL_TO_GROUP);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant deprovisioning mail to group attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * Group ID which holds people to email members of that group to send deprovisioning messages (mutually exclusive with deprovisioningEmailAddresses)
   */
  public static final String DEPROVISIONING_MAIL_TO_GROUP = "deprovisioningMailToGroup";
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String deprovisioningStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.systemFolder"), ":");
  }

  /**
   * if deprovisioning is enabled
   * @return if deprovisioning enabled
   */
  public static boolean deprovisioningEnabled() {
    // if turned off or if no realms then this is not enabled
    return GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.enable", true) || GrouperUtil.length(retrieveDeprovisioningRealms()) == 0;
  }
  
  /**
   * get the configured deprovisioning realms
   * @return the realms
   */
  public static Set<String> retrieveDeprovisioningRealms() {
    // dont call the method since could be a circular problem
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.enable", true)) {
      return new HashSet<String>();
    }
    return GrouperConfig.retrieveConfig().deprovisioningRealms();
  }
  
  /**
   * users in this group who are admins of a realm but who are not Grouper SysAdmins
   * @return the group name
   */
  public static String retrieveDeprovisioningAdminGroupName() {
    
    // # users in this group who are admins of a realm but who are not Grouper SysAdmins, will be 
    // # able to deprovision from all grouper groups/objects, not just groups they have access to UPDATE/ADMIN
    // deprovisioning.admin.group = $$deprovisioning.systemFolder$$:deprovisioningAdmins
    return GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.admin.group");

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
    
    return deprovisioningStemName() + ":managersWhoCanDeprovision_" + realm;

  }

  /**
   * group name which has been deprovisioned
   * @return the group name
   */
  public static String retrieveGroupNameWhichHasBeenDeprovisioned(String realm) {
    
    //  # e.g. managersWhoCanDeprovision_<realmName>
    //  # e.g. usersWhoHaveBeenDeprovisioned_<realmName>
    //  deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning
    
    return deprovisioningStemName() + ":usersWhoHaveBeenDeprovisioned_" + realm;
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
   * cache the sources allowed for a tad
   */
  private static ExpirableCache<Boolean, Set<Source>> retrieveSourcesAllowedToDeprovisionCache = new ExpirableCache<Boolean, Set<Source>>();
  
  /**
   * get the sources to deprovision, dont include the group source or the internal source
   * @return the sources to deprovision
   */
  public static Set<Source> retrieveSourcesAllowedToDeprovision() {
    
    Set<Source> result = retrieveSourcesAllowedToDeprovisionCache.get(Boolean.TRUE);
    
    if (result == null) {
    
      synchronized(retrieveSourcesAllowedToDeprovisionCache) {
  
        result = retrieveSourcesAllowedToDeprovisionCache.get(Boolean.TRUE);
        
        if (result == null) {
          result = new LinkedHashSet<Source>();
          
          for (Source source : SourceManager.getInstance().getSources()) {
            if (StringUtils.equals(source.getId(), GrouperSourceAdapter.groupSourceId())) {
              continue;
            }
            if (StringUtils.equals(source.getId(), InternalSourceAdapter.ID)) {
              continue;
            }
            result.add(source);
          }
          
          retrieveSourcesAllowedToDeprovisionCache.put(Boolean.TRUE, result);
        }
        
      }
    }
    
    return result;
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
  private Map<String, Set<EmailObject>> buildAttestationStemEmails() {
  
    //TODO only look at direct assignments?  are there duplicate assignments?
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, retrieveAttributeDefNameValueDef().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      
//      Map<String, Set<EmailObject>> localEmailMap = stemAttestationProcessHelper(attributeAssign.getOwnerStem(), attributeAssign);
//
//      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
//      
//      // skip sending email for this attribute assign
//      if (!sendEmailAttributeValue) {
//        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is not set to true so skipping sending email.");
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
    
    if (!deprovisioningEnabled()) {
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
        null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);

    if (attributeAssignBase == null) {
    
      Stem parentStem = retrieveAttributeAssignableParentStem(attributeAssignable);
      
      if (parentStem != null) {
        attributeAssignable = parentStem.getAttributeDelegate()
            .getAttributeOrAncestorAttribute(retrieveAttributeDefNameValueDef().getName(), false);
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
