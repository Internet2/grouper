package edu.internet2.middleware.grouper.app.google;

import java.sql.Types;
import java.util.Set;

import org.jsoup.internal.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;

public class GrouperGoogleGroup {
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableGoogleGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String groupTableName = "mock_google_group";

    try {
      new GcDbAccess().sql("select count(*) from " + groupTableName).select(int.class);
    } catch (Exception e) {
    
      
      Table groupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, groupTableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "email", Types.VARCHAR, "1024", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "managers", Types.VARCHAR, "4000", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "owners", Types.VARCHAR, "4000", false, false);
      
      //settings
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_add", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "default_message_deny_text", Types.VARCHAR, "500", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "message_moderation_level", Types.VARCHAR, "25", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "reply_to", Types.VARCHAR, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "spam_moderation_level", Types.VARCHAR, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_join", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_view_membership", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_view_group", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_invite", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_post_message", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "allow_external_members", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "allow_web_posting", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "allow_google_communication", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "send_msg_dny_notif", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "archive_only", Types.VARCHAR, "1", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTableName, "mock_google_group_name_idx", true, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.setName(this.name);
    targetGroup.setId(this.id);
    targetGroup.assignAttributeValue("email", this.email);
    targetGroup.assignAttributeValue("managers", this.managers);
    targetGroup.assignAttributeValue("owners", this.owners);

    // group settings
    targetGroup.assignAttributeValue("defaultMessageDenyNotificationText", this.defaultMessageDenyNotificationText);
    targetGroup.assignAttributeValue("whoCanAdd", this.whoCanAdd);
    targetGroup.assignAttributeValue("whoCanJoin", this.whoCanJoin);
    targetGroup.assignAttributeValue("whoCanViewMembership", this.whoCanViewMembership);
    targetGroup.assignAttributeValue("whoCanViewGroup", this.whoCanViewGroup);
    targetGroup.assignAttributeValue("whoCanInvite", this.whoCanInvite);
    targetGroup.assignAttributeValue("whoCanPostMessage", this.whoCanPostMessage);
    targetGroup.assignAttributeValue("allowExternalMembers", this.allowExternalMembers);
    targetGroup.assignAttributeValue("allowGoogleCommunication", this.allowGoogleCommunication);
    targetGroup.assignAttributeValue("allowWebPosting", this.allowWebPosting);
    targetGroup.assignAttributeValue("messageModerationLevel", this.messageModerationLevel);
    targetGroup.assignAttributeValue("replyTo", this.replyTo);
    targetGroup.assignAttributeValue("spamModerationLevel", this.spamModerationLevel);
    targetGroup.assignAttributeValue("sendMessageDenyNotification", this.sendMessageDenyNotification);

    if (this.getArchiveOnly() != null && this.getArchiveOnly() == Boolean.TRUE) {
      targetGroup.assignAttributeValue("handleDeletedGroup", "archive");
    } else {
      targetGroup.assignAttributeValue("handleDeletedGroup", "delete");
    }
    
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperGoogleGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperGoogleGroup grouperGoogleGroup = new GrouperGoogleGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) { 
      grouperGoogleGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("managers")) { 
      Set<String> managers = (Set<String>)targetGroup.retrieveAttributeValueSet("managers");
      grouperGoogleGroup.setManagers(managers);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("owners")) { 
      Set<String> owners = (Set<String>)targetGroup.retrieveAttributeValueSet("owners");
      grouperGoogleGroup.setOwners(owners);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperGoogleGroup.setName(targetGroup.getName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperGoogleGroup.setId(targetGroup.getId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      grouperGoogleGroup.setEmail(targetGroup.retrieveAttributeValueString("email"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanAdd")) {      
      grouperGoogleGroup.setWhoCanAdd(targetGroup.retrieveAttributeValueString("whoCanAdd"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanJoin")) {      
      grouperGoogleGroup.setWhoCanJoin(targetGroup.retrieveAttributeValueString("whoCanJoin"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanViewMembership")) {      
      grouperGoogleGroup.setWhoCanViewMembership(targetGroup.retrieveAttributeValueString("whoCanViewMembership"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanViewGroup")) {      
      grouperGoogleGroup.setWhoCanViewGroup(targetGroup.retrieveAttributeValueString("whoCanViewGroup"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanInvite")) {      
      grouperGoogleGroup.setWhoCanInvite(targetGroup.retrieveAttributeValueString("whoCanInvite"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanPostMessage")) {      
      grouperGoogleGroup.setWhoCanPostMessage(targetGroup.retrieveAttributeValueString("whoCanPostMessage"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowExternalMembers")) {      
      grouperGoogleGroup.setAllowExternalMembers(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("allowExternalMembers"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowGoogleCommunication")) {      
      grouperGoogleGroup.setAllowGoogleCommunication(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("allowGoogleCommunication"), false));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowWebPosting")) {      
      grouperGoogleGroup.setAllowWebPosting(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("allowWebPosting"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("defaultMessageDenyNotificationText")) {      
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      if (StringUtils.isNotBlank(grouperGoogleConfiguration.getDefaultMessageDenyNotificationText())) {
        grouperGoogleGroup.setDefaultMessageDenyNotificationText(grouperGoogleConfiguration.getDefaultMessageDenyNotificationText());
      }
      
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("messageModerationLevel")) {   
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      if (StringUtils.isNotBlank(grouperGoogleConfiguration.getMessageModerationLevel())) {
        grouperGoogleGroup.setMessageModerationLevel(grouperGoogleConfiguration.getMessageModerationLevel());
      }
      
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("replyTo")) {  
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      if (StringUtils.isNotBlank(grouperGoogleConfiguration.getReplyTo())) {
        grouperGoogleGroup.setReplyTo(grouperGoogleConfiguration.getReplyTo());
      }
      
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("spamModerationLevel")) {  
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      if (StringUtils.isNotBlank(grouperGoogleConfiguration.getSpamModerationLevel())) {
        grouperGoogleGroup.setSpamModerationLevel(grouperGoogleConfiguration.getSpamModerationLevel());
      }
      
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("sendMessageDenyNotification")) {  
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      grouperGoogleGroup.setSendMessageDenyNotification(grouperGoogleConfiguration.isSendMessageDenyNotification());
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("handleDeletedGroup")) {
      
      GrouperGoogleConfiguration grouperGoogleConfiguration = (GrouperGoogleConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      if (StringUtils.equals(grouperGoogleConfiguration.getHandleDeletedGroup(), "archive")) {
        grouperGoogleGroup.setArchiveOnly(true);
      } else {
        grouperGoogleGroup.setArchiveOnly(false);
      }
    }
    
    return grouperGoogleGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private String id;
  private String name;
  private String description;
  private String email;
  
  private Set<String> owners;
  private Set<String> managers;
  
  // group settings
  private String whoCanAdd;
  private String whoCanJoin;
  private String whoCanViewMembership;
  private String whoCanViewGroup;
  private String whoCanInvite;
  private Boolean allowExternalMembers;
  private Boolean allowGoogleCommunication;
  private String whoCanPostMessage;
  private Boolean allowWebPosting;
  
  private String defaultMessageDenyNotificationText;
  private String messageModerationLevel;
  private String replyTo;
  private String spamModerationLevel;
  private Boolean sendMessageDenyNotification;
  
  private Boolean archiveOnly;
  
  public String getAllowExternalMembersDb() {
    return allowExternalMembers == null ? "F" : allowExternalMembers ? "T" : "F";
  }

  
  public void setAllowExternalMembersDb(String allowExternalMembers) {
    this.allowExternalMembers = GrouperUtil.booleanObjectValue(allowExternalMembers);
  }
  
  
  
  
  public String getSendMessageDenyNotificationDb() {
    return sendMessageDenyNotification == null ? "F" : sendMessageDenyNotification ? "T" : "F";
  }

  
  public void setSendMessageDenyNotificationDb(String sendMessageDenyNotification) {
    this.sendMessageDenyNotification = GrouperUtil.booleanObjectValue(sendMessageDenyNotification);
  }
  
  
  
  
  public String getAllowGoogleCommunicationDb() {
    return allowGoogleCommunication == null ? "F" : allowGoogleCommunication ? "T" : "F";
  }

  
  public void setAllowGoogleCommunicationDb(String allowGoogleCommunication) {
    this.allowGoogleCommunication = GrouperUtil.booleanObjectValue(allowGoogleCommunication);
  }
  
  public String getAllowWebPostingDb() {
    return allowWebPosting == null ? "F": allowWebPosting ? "T" : "F";
  }

  
  public void setAllowWebPostingDb(String allowWebPosting) {
    this.allowWebPosting = GrouperUtil.booleanObjectValue(allowWebPosting);
  }
  

  
  public String getDefaultMessageDenyNotificationText() {
    return defaultMessageDenyNotificationText;
  }

  
  public void setDefaultMessageDenyNotificationText(
      String defaultMessageDenyNotificationText) {
    this.defaultMessageDenyNotificationText = defaultMessageDenyNotificationText;
  }
  
  
  public String getMessageModerationLevel() {
    return messageModerationLevel;
  }

  
  public void setMessageModerationLevel(String messageModerationLevel) {
    this.messageModerationLevel = messageModerationLevel;
  }

  public String getDescription() {
    return description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getEmail() {
    return email;
  }

  
  public void setEmail(String email) {
    this.email = email;
  }

  public String getId() {
    return id;
  }

  
  public void setId(String groupId) {
    this.id = groupId;
  }

  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }
  
  
  
  public Boolean getAllowExternalMembers() {
    return allowExternalMembers;
  }

  
  public void setAllowExternalMembers(Boolean allowExternalMembers) {
    this.allowExternalMembers = allowExternalMembers;
  }
  
  
  
  public Boolean getAllowGoogleCommunication() {
    return allowGoogleCommunication;
  }

  
  public void setAllowGoogleCommunication(Boolean allowGoogleCommunication) {
    this.allowGoogleCommunication = allowGoogleCommunication;
  }

  public Boolean getAllowWebPosting() {
    return allowWebPosting;
  }

  
  public void setAllowWebPosting(Boolean allowWebPosting) {
    this.allowWebPosting = allowWebPosting;
  }

  public String getWhoCanAdd() {
    return whoCanAdd;
  }

  
  public void setWhoCanAdd(String whoCanAdd) {
    this.whoCanAdd = whoCanAdd;
  }

  
  public String getWhoCanJoin() {
    return whoCanJoin;
  }

  
  public void setWhoCanJoin(String whoCanJoin) {
    this.whoCanJoin = whoCanJoin;
  }

  
  public String getWhoCanViewMembership() {
    return whoCanViewMembership;
  }

  
  public void setWhoCanViewMembership(String whoCanViewMembership) {
    this.whoCanViewMembership = whoCanViewMembership;
  }

  
  public String getWhoCanViewGroup() {
    return whoCanViewGroup;
  }

  
  public void setWhoCanViewGroup(String whoCanViewGroup) {
    this.whoCanViewGroup = whoCanViewGroup;
  }

  
  public String getWhoCanInvite() {
    return whoCanInvite;
  }

  
  public void setWhoCanInvite(String whoCanInvite) {
    this.whoCanInvite = whoCanInvite;
  }

  
  public String getWhoCanPostMessage() {
    return whoCanPostMessage;
  }

  
  public void setWhoCanPostMessage(String whoCanPostMessage) {
    this.whoCanPostMessage = whoCanPostMessage;
  }
  
  
  public String getReplyTo() {
    return replyTo;
  }

  
  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }
  
  
  public Boolean getSendMessageDenyNotification() {
    return sendMessageDenyNotification;
  }

  
  public void setSendMessageDenyNotification(Boolean sendMessageDenyNotification) {
    this.sendMessageDenyNotification = sendMessageDenyNotification;
  }
  
  
  public String getSpamModerationLevel() {
    return spamModerationLevel;
  }

  
  public void setSpamModerationLevel(String spamModerationLevel) {
    this.spamModerationLevel = spamModerationLevel;
  }
  
  
  public Boolean getArchiveOnly() {
    return archiveOnly;
  }

  
  public void setArchiveOnly(Boolean archiveOnly) {
    this.archiveOnly = archiveOnly;
  }
  
  
  public Set<String> getOwners() {
    return owners;
  }

  
  public void setOwners(Set<String> owners) {
    this.owners = owners;
  }

  
  public Set<String> getManagers() {
    return managers;
  }

  
  public void setManagers(Set<String> managers) {
    this.managers = managers;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperGoogleGroup fromJson(JsonNode groupNode) {
    
    GrouperGoogleGroup grouperGoogleGroup = new GrouperGoogleGroup();
    grouperGoogleGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    // google sends back blank description even when we explicitly set it to null
    // To prevent unnecessary updates, we're going to mark blank description as null
    if (StringUtil.isBlank(grouperGoogleGroup.description)) {
      grouperGoogleGroup.description = null;
    }
    
    grouperGoogleGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    
    grouperGoogleGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    grouperGoogleGroup.email = GrouperUtil.jsonJacksonGetString(groupNode, "email");
    
    return grouperGoogleGroup;
  }
  
  public void populateGroupSettings(JsonNode groupSettingsNode) {
    
    this.defaultMessageDenyNotificationText = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "defaultMessageDenyNotificationText");
    this.messageModerationLevel = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "messageModerationLevel");
    this.replyTo = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "replyTo");
    this.spamModerationLevel = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "spamModerationLevel");
    this.whoCanAdd = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanAdd");
    this.whoCanJoin = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanJoin");
    this.whoCanViewMembership = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanViewMembership");
    this.whoCanViewGroup = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanViewGroup");
    this.whoCanInvite = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanInvite");
    this.allowExternalMembers = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "allowExternalMembers");
    this.allowGoogleCommunication = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "allowGoogleCommunication");
    this.whoCanPostMessage = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanPostMessage");
    this.allowWebPosting = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "allowWebPosting");
    this.sendMessageDenyNotification = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "sendMessageDenyNotification");
    this.archiveOnly = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "archiveOnly");
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJsonGroupOnly(Set<String> fieldNamesToSet) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      result.put("id", this.id);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      result.put("name", this.name);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      result.put("description", this.description);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      result.put("email", this.email);
    }
    
    return result;
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJsonGroupSettings(Set<String> fieldNamesToSet) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanAdd")) {      
      result.put("whoCanAdd", this.whoCanAdd);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanJoin")) {      
      result.put("whoCanJoin", this.whoCanJoin);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanViewMembership")) {      
      result.put("whoCanViewMembership", this.whoCanViewMembership);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanViewGroup")) {      
      result.put("whoCanViewGroup", this.whoCanViewGroup);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanInvite")) { 
      result.put("whoCanInvite", this.whoCanInvite);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowExternalMembers")) {   
      result.put("allowExternalMembers", this.allowExternalMembers);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("whoCanPostMessage")) {      
      result.put("whoCanPostMessage", this.whoCanPostMessage);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowWebPosting")) {      
      result.put("allowWebPosting", this.allowWebPosting);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowGoogleCommunication")) {      
      result.put("allowGoogleCommunication", this.allowGoogleCommunication);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("defaultMessageDenyNotificationText")) {      
      result.put("defaultMessageDenyNotificationText", this.defaultMessageDenyNotificationText);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("messageModerationLevel")) {      
      result.put("messageModerationLevel", this.messageModerationLevel);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("replyTo")) {      
      result.put("replyTo", this.replyTo);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("sendMessageDenyNotification")) {      
      result.put("sendMessageDenyNotification", this.sendMessageDenyNotification);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("spamModerationLevel")) {      
      result.put("spamModerationLevel", this.spamModerationLevel);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("archiveOnly")) {      
      result.put("archiveOnly", this.archiveOnly);
    }
    
    return result;
  }

}
