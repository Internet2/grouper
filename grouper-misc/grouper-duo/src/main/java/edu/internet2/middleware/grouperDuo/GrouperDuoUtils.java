/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.duosecurity.client.Http;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 *
 */
public class GrouperDuoUtils {

  /**
   * 
   */
  public GrouperDuoUtils() {
  }

  /**
   * cache the folder for duo
   */
  private static ExpirableCache<Boolean, Stem> duoStemCache = new ExpirableCache<Boolean, Stem>(5);

  /**
   * get duo stem from expirable cache or from database
   * duo stem
   * @param debugMap
   * @return the stem
   */
  public static Stem duoStem(Map<String, Object> debugMap) {
    
    Stem duoStem  = duoStemCache.get(Boolean.TRUE);
    if (debugMap != null) {
      debugMap.put("duoStemInExpirableCache", duoStem != null);
    }

    if (duoStem == null) {
      duoStem = duoStemHelper(debugMap);
      duoStemCache.put(Boolean.TRUE, duoStem);
    }
    return duoStem;
  }
  
  /**
   * duo stem
   * @param debugMap
   * @return the stem
   */
  public static Stem duoStemHelper(Map<String, Object> debugMap) {

    //# put groups in here which go to duo, the name in duo will be the extension here
    //grouperDuo.folder.name.withDuoGroups = duo
    String grouperDuoFolderName = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.folder.name.withDuoGroups");
    boolean useUiProvisioningConfiguration = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.use.ui.provisioning.configuration", false);
    
    if (useUiProvisioningConfiguration && !StringUtils.isBlank(grouperDuoFolderName)) {
      throw new RuntimeException("If you are using ui provisioning configuration, you cant configure a folder in the grouper-loader.properties 'grouperDuo.folder.name.withDuoGroups'!!!!");
    }
    
    Stem grouperDuoFolder = null;
    
    if (useUiProvisioningConfiguration) {
      
      String uiProvisioningTargetName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.ui.provisioning.targetName");
      
      if (debugMap != null) {
        debugMap.put("uiProvisioningTargetName", uiProvisioningTargetName);
      }
      
      List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
          .assignNameOfAttributeDefName(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)
          .addAttributeValuesOnAssignment(uiProvisioningTargetName)
          .assignNameOfAttributeDefName2(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)
          .addAttributeValuesOnAssignment2("true")
          .findStems());

      GrouperUtil.stemRemoveChildStemsOfTopStem(stems);
      
      if (debugMap != null) {
        debugMap.put("folderCount", GrouperUtil.length(stems));
      }
      
      if (GrouperUtil.length(stems) > 1) {
        throw new RuntimeException("Folder count can only be 0 or 1!!! " + GrouperUtil.length(stems));
      }
      if (GrouperUtil.length(stems) == 1) {
        grouperDuoFolder = stems.iterator().next();
      }
    } else {
    
      grouperDuoFolder = StemFinder.findByName(GrouperSession.staticGrouperSession(), grouperDuoFolderName, true);
    }
    return grouperDuoFolder;
  }
  
  /**
   * folder for duo groups, ends in colon
   * @return the config folder for duo groups
   */
  public static String configFolderForDuoGroups() {
    return duoStem(null).getName() + ":";
  }

  /**
   * Folder for duo admin sync
   * @return the config folder for duo admins
   */
  public static String configFolderForDuoAdmins() {

    String duoFolderName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.folder.name.withDuoAdmins");
    if (!duoFolderName.endsWith(":")) {
      duoFolderName += ":";
    }

    GrouperDuoLog.duoLog(String.format("Using folder '%s' for duo admin sync.", duoFolderName));

    return duoFolderName;
  }

  public static boolean provisionAdminAccountsWithRandomPasswords() {
    return true;
  }

  /**
   * subject attribute to get the duo username from the subject, could be "id" for subject id
   * @return the subject attribute name
   */
  public static String configSubjectAttributeForDuoUsername() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.subjectAttributeForDuoUsername");
  }
  
  /**
   * sources for subjects
   * @return the config sources for subjects
   */
  public static Set<String> configSourcesForSubjects() {
  
    //# put the comma separated list of sources to send to duo
    //grouperDuo.sourcesForSubjects = someSource
    String sourcesForSubjectsString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.sourcesForSubjects");
    
    return GrouperUtil.splitTrimToSet(sourcesForSubjectsString, ",");
  }

  /**
   * The attribute name that holds the admin_id value for a member
   * @return attribute name
   */
  public static String configAttributeForAdminId() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.attributeForAdminId");
  }

  /**
   * The attribute name that holds the role for an admin group.
   * @return attribute name
   */
  public static String configAttributeForAdminRole() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.attributeForAdminRole");
  }

  /**
   * The attribute name that holds the string to append to the end of a user's name.
   * @return attribute name
   */
  public static String configAttributeForAdminNameSuffix() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.attributeForAdminNameSuffix");
  }

  public static String configSubjectAttributeForName() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.subjectAttributeForName", "name");
  }

  public static String configSubjectAttributeForPhone() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.subjectAttributeForPhoneName", "phone");
  }

  public static String configSubjectAttributeForEmail() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.subjectAttributeForEmailName", "email");
  }
  
  public static String configEmailRecipientsGroupName() {
	  return GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.emailRecipientsGroupName", "");
  }

  /**
   * must be in stem and not have invalid suffix
   * @param groupName
   * @return true if valid group name
   */
  public static boolean validDuoGroupName(String groupName) {
    
    String duoFolderName = configFolderForDuoGroups();
  
    if (!groupName.startsWith(duoFolderName)) {
      return false;
    }
    
    groupName = groupName.substring(duoFolderName.length());
    
    //must be directly in folder
    if (groupName.contains(":")) {
      return false;
    }
  
    //cant be include/exclude and not overall
    if (groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordAndIncludesExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.includeExtensionSuffix())
        || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.excludeExtensionSuffix())) {
      return false;
    }
  
    return true;
  }

  /**
   * Checks that a group name is within the admin sync folder and that the extension is a valid duo admin role.
   *
   * @param groupName
   * @return true if valid group name
   */
  public static boolean isValidDuoAdminGroup(GrouperSession grouperSession, String groupName) {
    if (groupName == null)
      return false;

    String duoFolderName = configFolderForDuoAdmins();

    GrouperDuoLog.duoLog(String.format("Checking if group '%s' is a direct child of '%s'", groupName, duoFolderName));
    if (!groupName.startsWith(duoFolderName)) {
      GrouperDuoLog.duoLog(String.format("Group name does not start with '%s'", duoFolderName));
      return false;
    }

    GrouperDuoLog.duoLog(String.format("Finding group: %s", groupName));
    Group group = GroupFinder.findByName(grouperSession, groupName, false);
    if (group == null) {
      GrouperDuoLog.duoLog("Could not find group by name..." + groupName);
      return false;
    }

    GrouperDuoLog.duoLog("Getting group attribute to check for role");
    AttributeValueDelegate attributeValueDelegate = group.getAttributeValueDelegate();
    String adminRole = attributeValueDelegate.retrieveValueString(configAttributeForAdminRole());
    if (adminRole == null) {
      GrouperDuoLog.duoLog("Group does not have an admin role associated with it...");
      return false;
    }

    GrouperDuoLog.duoLog(String.format("Checking that %s is a manageable role.", adminRole));
    // Verify that the groupName matches a duo role name
    if (!manageableAdminRoles().contains(adminRole)) {
      GrouperDuoLog.duoLog(String.format("'%s' is not within manageable admin roles", groupName));
      return false;
    }


    GrouperDuoLog.duoLog("Checking include / exclude.");
    //cant be include/exclude and not overall
    if (groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordExtensionSuffix())
            || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.systemOfRecordAndIncludesExtensionSuffix())
            || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.includeExtensionSuffix())
            || groupName.endsWith(GroupTypeTupleIncludeExcludeHook.excludeExtensionSuffix())) {
      GrouperDuoLog.duoLog(String.format("Group name cant be include/exclude and not overall"));
      return false;
    }

    GrouperDuoLog.duoLog("validDuoAdminName return true");
    return true;
  }

  public static boolean isDuoAdminSyncEnabled() {
    boolean enabled = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.adminSyncEnabled", false);
    GrouperDuoLog.duoLog(String.format("isDuoAdminSyncEnabled: %s", enabled));

    return enabled;
  }

  public static Set<String> manageableAdminRoles() {
    String roles = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.manageableAdminRoles", "Owner,Administrator,Application Manager,User Manager,Help Desk,Billing,Phishing Manager,Read-only");

    return GrouperUtil.splitTrimToSet(roles, ",");
  }

  public static String getAdminIdFromMember(Member member) {
    // Check that the attribute exists
    AttributeAssignValue attributeAssignValue = member.getAttributeValueDelegate().retrieveAttributeAssignValue(configAttributeForAdminId());
    if (attributeAssignValue == null)
      return null;

    return attributeAssignValue.getValueString();
  }

  public static boolean attachAdminIdToMember(Member member, String adminId) {
    member.getAttributeDelegate().removeAttributeByName(configAttributeForAdminId());

    return member.getAttributeValueDelegate().assignValue(configAttributeForAdminId(), adminId).isChanged();
  }

  /**
   * Creates a password for a new administrator account.
   *
   * The behavior of this method can be changed directly from the configuration files:
   *
   * @return
   */
  public static String createAdminAccountPassword() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouperDuo.defaultAdminPassword");
  }

  /**
   * Checks if the user is already a member of another admin role.
   *
   * @param session GrouperSession
   * @param member Member
   * @return True if the user already belongs to another admin role group.
   */
  public static Group getExistingAdminRole(GrouperSession session, Member member) {
    String folderName = GrouperDuoUtils.configFolderForDuoAdmins();
    folderName = folderName.substring(0, folderName.length() - 1);

    Stem roleStem = StemFinder.findByName(session, folderName, true);

    for (Object object : roleStem.getChildGroups()) {
      Group group = (Group) object;

      if (group.hasEffectiveMember(member.getSubject()) || group.hasMember(member.getSubject()) || group.hasNonImmediateMember(member.getSubject()) || group.hasImmediateMember(member.getSubject())) {
        if (isValidDuoAdminGroup(session, group.getName())) {
          return group;
        }
      }
    }

    return null;
  }

  /**
   * Fetches a GrouperDuoAdministrator object for a user, and handles updating the attributes attached to the user.
   *
   * This method will create an administrator account for the specified member, but will not handle the sync logic
   * from the Groups. All GrouperDuoAdministrator accounts should have their state verified each iteration to
   * evaluate all of the business logic.
   *
   * @param member
   * @param createIfNotFound
   * @return
   */
  public static GrouperDuoAdministrator fetchOrCreateGrouperDuoAdministrator(Member member, boolean createIfNotFound, Map<String, GrouperDuoAdministrator> administrators) {
    GrouperDuoAdministrator administrator = null;
    String duoAdminId = getAdminIdFromMember(member);
    Subject subject = member.getSubject();

    // Attempt to find an existing Administrator account
    administrator = administrators.get(duoAdminId);

    if (duoAdminId == null || administrator == null) {
      // Try to associate an existing Administrator account by email address
      for (GrouperDuoAdministrator admin : administrators.values()) {
        if (admin.getEmail().equals(member.getSubject().getAttributeValue(configSubjectAttributeForEmail()))) {
          administrator = admin;
          break;
        }
      }
    }

    // Create a new account for the user if one was not found
    if (createIfNotFound && administrator == null) {
      String phone = null;
      try {
        JSONObject duoUser = GrouperDuoCommands.retrieveDuoUserByIdOrUsername(subject.getAttributeValue(configSubjectAttributeForDuoUsername()), false, 30);

        if (duoUser != null && duoUser.has("phones")) {
          JSONArray phones = duoUser.getJSONArray("phones");

          for (int i = 0; i < phones.size(); i++) {
            JSONObject phoneObj = phones.getJSONObject(i);

            if (phoneObj.has("number")) {
              String number = phoneObj.getString("number");

              if (number != null && number.length() > 0)
                phone = number;
            }
          }
        }
      }catch(Exception e) {
    	  GrouperDuoLog.logError("Exception while retrieving and processing user record: " + e.getMessage(), e);
      }

      if (phone == null)
        phone = subject.getAttributeValue(configSubjectAttributeForPhone());
      if (phone == null) {
    	  GrouperDuoLog.logError(String.format("Failed to locate a phone number for subject: %s, %s", subject.getId(), subject.getName()));
    	  throw new RuntimeException(String.format("Failed to locate a phone number for subject: %s, %s", subject.getId(), subject.getName()));
      }

      administrator = GrouperDuoCommands.createNewAdminAccount(
              subject.getAttributeValue(configSubjectAttributeForName()),
              subject.getAttributeValue(configSubjectAttributeForEmail()),
              GrouperDuoUtils.createAdminAccountPassword(),
              phone,
              "Read-only",
              true,
              true
      );
    }

    // Remove the administrator id attribute if an admin account couldn't be created / found.
    if (administrator == null && duoAdminId != null)
      member.getAttributeDelegate().removeAttributeByName(configAttributeForAdminId());
    // Update the administrator id if it has changed
    if (administrator != null && !administrator.getAdminId().equals(duoAdminId))
      attachAdminIdToMember(member, administrator.getAdminId());

    return administrator;
  }

  public static void synchronizeMemberAndDuoAdministrator(GrouperSession session, Member member, GrouperDuoAdministrator administrator) {
	  GrouperDuoLog.duoLog("Syncing duo administrator and member");
    Group adminGroup = getExistingAdminRole(session, member);
    
    Subject subject = null;
    try {
    	subject = member.getSubject();
    } catch (SubjectNotFoundException ex) {
    	GrouperDuoLog.logError(String.format("Could not find subject for member: %s (Id: %s)", member.getName(), member.getId()));
    	throw ex;
    }

    String effectiveRole = null;
    if (adminGroup != null) {
      effectiveRole = adminGroup.getAttributeValueDelegate().retrieveValueString(configAttributeForAdminRole());
    }
    
    if (!StringUtils.equals(GrouperDuoUtils.getAdminIdFromMember(member), administrator.getAdminId())) {
    	GrouperDuoLog.duoLog("Updating adminId attribute on member.");
    	GrouperDuoUtils.attachAdminIdToMember(member, administrator.getAdminId());
    }else {
    	GrouperDuoLog.duoLog("AdminId matches member and administrator object.");
    }

    // Verify the state of the GrouperDuoAdministrator object...
    boolean hasRoleChanged = !administrator.getRole().equals(effectiveRole);
    boolean hasStatusChanged = administrator.isActive() == (effectiveRole == null);

    String groupNameSuffix = "";
    if (adminGroup != null) groupNameSuffix = adminGroup.getAttributeValueDelegate().retrieveValueString(configAttributeForAdminNameSuffix());

    String expectedName = subject.getAttributeValue(configSubjectAttributeForName()) + (groupNameSuffix != null ? groupNameSuffix : "");
    boolean hasNameChanged = !expectedName.equals(administrator.getName());
    boolean isActive = effectiveRole != null;

    if (hasRoleChanged || hasStatusChanged || hasNameChanged) {
      Http updateRequest = GrouperDuoCommands.startAdminUpdateRequest(administrator);

      if (hasNameChanged)
        GrouperDuoCommands.updateAdminName(updateRequest, expectedName);

      if (hasStatusChanged) {
        GrouperDuoCommands.updateAdminStatus(updateRequest, isActive);

        // When we are disabling accounts, set the role to read only (unless overriden by another role change).
        if (!isActive)
          GrouperDuoCommands.updateAdminRole(updateRequest, "Read-only");
      }

      if (hasRoleChanged)
        GrouperDuoCommands.updateAdminRole(updateRequest, effectiveRole != null ? effectiveRole : "Read-only");

      GrouperDuoCommands.executeAdminUpdateRequest(administrator, updateRequest);
    }
  }

  public static boolean isDuoGroupSyncEnabled() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.groupSyncEnabled", true);
  }

  /**
   * Config option for enabling the disabling of administrator accounts not associated with a Grouper user.
   * @return
   */
  public static boolean isDisableUnknownAdminAccountsEnabled() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.disableUnknownAdmins", false);
  }

  /**
   * Configuration option for whether or not to delete administrators.
   *
   * @return true if deleting admin accounts is enabled.
   */
  public static boolean isDeleteUnknownAdminAccountsEnabled() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouperDuo.deleteUnknownAdmins", false);
  }

  /**
   * Configuration option for how long since the last login before a disabled admin account can be enabled.
   *
   * @return number of seconds to wait before removing an admin account.
   */
  public static long deleteUnknownAdminAccountsAfterSeconds() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouperDuo.deleteUnknownAdminsAfterSeconds", 2592000); // Default after 30 days
  }
  
  /**
   * Sends an email to all members of a Grouper group.
   * 
   * @param groupMembersToNotify The group containing all of the receiving members
   * @param subject The subject of the email
   * @param body The body of the email
   */
  public static void sendEmailToGroupMembers(Group groupMembersToNotify, String subject, String body) {
	  if (groupMembersToNotify == null) {
		  GrouperDuoLog.logError(String.format("groupMembersToNotify is null, cannot send an email notification to a null group. %s -- %s", subject, body));
	  }
	String To = "";
	Set<Member> members = groupMembersToNotify.getMembers();
	String subjectEmailAttribute = GrouperDuoUtils.configSubjectAttributeForEmail();
	
	for (Member member : members) {
		String seperator = To.length() > 0 ? ";" : "";
		To += member.getSubject().getAttributeValue(subjectEmailAttribute) != null ? seperator + member.getSubject().getAttributeValue(subjectEmailAttribute) : "";
	}
	
	if (To.length() > 0) {		
		GrouperEmail gm = new GrouperEmail();
		gm.setBody(body).setSubject(subject).setTo(To);
		gm.send();
		
		GrouperDuoLog.logInfo(String.format("Sent a notification email '%s' to %s.", subject, To));
	} else {
		GrouperDuoLog.logError(String.format("No recipients for email notification: %s -- %s", subject, body));
	}
  }
  
  /**
   * Build a formatted string of subject attributes for an error notification.
   */
  public static String getSubjectAttributesForEmail(Subject subject) {
  	if (subject == null)
  		return "Null subject, no attribute values.\n";
  	
  	String message = "";
  	
  	String attributeList = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDuo.emailNotificationSubjectAttributes", "mail,email,name");
  	Set<String> attributes = GrouperUtil.splitTrimToSet(attributeList, ",");
  	
  	for (String attribute : attributes) {
		message += String.format("%s:%s\n", attribute, subject.getAttributeValue(attribute));
  	}
  	
  	return message;
  }
  
  /**
   * Removes a subject from all Duo Admin groups.
   * @param session
   * @param subject
   * @return
   */
  public static boolean removeSubjectFromDuoAdminGroups(GrouperSession session, Subject subject) {
    String folderName = GrouperDuoUtils.configFolderForDuoAdmins();
    folderName = folderName.substring(0, folderName.length() - 1);
    boolean memberRemoved = false;

    try {
      Stem roleStem = StemFinder.findByName(session, folderName, true);

      for (Object object : roleStem.getChildGroups()) {
        Group group = (Group) object;

        if (group.hasMember(subject)) {
          group.deleteMember(subject, false);
          memberRemoved = true;
        }
      }
    } catch (StemNotFoundException e) {
      GrouperDuoLog.logError("Could not find stem for " + folderName, e);
      return false;
    }

    return memberRemoved;
  }

}
