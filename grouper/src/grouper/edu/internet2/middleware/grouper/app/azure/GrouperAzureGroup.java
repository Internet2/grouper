package edu.internet2.middleware.grouper.app.azure;

import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAzureGroup {

  public static final boolean defaultMailEnabled = false;
  public static final boolean defaultSecurityEnabled = false;
  public static final String defaultVisibility = "Public";

  public static void main(String[] args) {
    
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    
    grouperAzureGroup.setDescription("desc");
    grouperAzureGroup.setDisplayName("dispName");
    grouperAzureGroup.setGroupTypeMailEnabled(true);
    grouperAzureGroup.setGroupTypeMailEnabledSecurity(true);
    grouperAzureGroup.setGroupTypeSecurity(true);
    grouperAzureGroup.setGroupTypeUnified(true);
    grouperAzureGroup.setId("id");
    grouperAzureGroup.setMailEnabled(true);
    grouperAzureGroup.setMailNickname("mailNick");
    grouperAzureGroup.setSecurityEnabled(true);
    grouperAzureGroup.setVisibility(AzureVisibility.Private);

    String json = GrouperUtil.jsonJacksonToString(grouperAzureGroup.toJson(null));
    System.out.println(json);
    
    grouperAzureGroup = GrouperAzureGroup.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperAzureGroup.toString());
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAzureGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_azure_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
    
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_mail_enabled", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_mail_enabled_sec", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_security", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type_unified", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "mail_enabled", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "mail_nickname", Types.VARCHAR, "64", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "security_enabled", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "visibility", Types.VARCHAR, "32", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rbo_allow_only_members_to_post", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rbo_hide_group_in_outlook", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rbo_sub_new_group_members", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rbo_welcome_email_disbled", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rpo_teams", Types.VARCHAR, "1", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_azure_group_disp_idx", false, "display_name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.setDisplayName(this.displayName);
    targetGroup.assignAttributeValue("groupTypeMailEnabled", this.groupTypeMailEnabled);
    targetGroup.assignAttributeValue("groupTypeMailEnabledSecurity", this.groupTypeMailEnabledSecurity);
    targetGroup.assignAttributeValue("groupTypeSecurity", this.groupTypeSecurity);
    targetGroup.assignAttributeValue("groupTypeUnified", this.groupTypeUnified);
    targetGroup.setId(this.id);
    targetGroup.assignAttributeValue("mailEnabled", this.mailEnabled);
    targetGroup.assignAttributeValue("mailNickname", this.mailNickname);
    targetGroup.assignAttributeValue("securityEnabled", this.securityEnabled);
    targetGroup.assignAttributeValue("visibility", this.visibility);
    
    targetGroup.assignAttributeValue("allowOnlyMembersToPost", this.resourceBehaviorOptionsAllowOnlyMembersToPost);
    targetGroup.assignAttributeValue("hideGroupInOutlook", this.resourceBehaviorOptionsHideGroupInOutlook);
    targetGroup.assignAttributeValue("subscribeNewGroupMembers", this.resourceBehaviorOptionsSubscribeNewGroupMembers);
    targetGroup.assignAttributeValue("welcomeEmailDisabled", this.resourceBehaviorOptionsWelcomeEmailDisabled);
    targetGroup.assignAttributeValue("resourceProvisioningOptionsTeams", this.resourceProvisioningOptionsTeams);
    
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperAzureGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      grouperAzureGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      grouperAzureGroup.setDisplayName(targetGroup.getDisplayName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupTypeMailEnabled")) {      
      grouperAzureGroup.setGroupTypeMailEnabled(targetGroup.retrieveAttributeValueBoolean("groupTypeMailEnabled"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupTypeMailEnabledSecurity")) {      
      grouperAzureGroup.setGroupTypeMailEnabledSecurity(targetGroup.retrieveAttributeValueBoolean("groupTypeMailEnabledSecurity"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupTypeSecurity")) {      
      grouperAzureGroup.setGroupTypeSecurity(targetGroup.retrieveAttributeValueBoolean("groupTypeSecurity"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupTypeUnified")) {      
      grouperAzureGroup.setGroupTypeUnified(targetGroup.retrieveAttributeValueBoolean("groupTypeUnified"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperAzureGroup.setId(targetGroup.getId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailEnabled")) {      
      grouperAzureGroup.setMailEnabled(targetGroup.retrieveAttributeValueBoolean("mailEnabled"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailNickname")) {      
      grouperAzureGroup.setMailNickname(targetGroup.retrieveAttributeValueString("mailNickname"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("securityEnabled")) {      
      grouperAzureGroup.setSecurityEnabled(targetGroup.retrieveAttributeValueBoolean("securityEnabled"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("visibility")) {      
      grouperAzureGroup.setVisibilityDb(targetGroup.retrieveAttributeValueString("visibility"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowOnlyMembersToPost")) {      
      grouperAzureGroup.setResourceBehaviorOptionsAllowOnlyMembersToPost(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("allowOnlyMembersToPost"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("hideGroupInOutlook")) {      
      grouperAzureGroup.setResourceBehaviorOptionsHideGroupInOutlook(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("hideGroupInOutlook"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("subscribeNewGroupMembers")) {      
      grouperAzureGroup.setResourceBehaviorOptionsSubscribeNewGroupMembers(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("subscribeNewGroupMembers"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("welcomeEmailDisabled")) {      
      grouperAzureGroup.setResourceBehaviorOptionsWelcomeEmailDisabled(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("welcomeEmailDisabled"), false));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("resourceProvisioningOptionsTeams")) {      
      grouperAzureGroup.setResourceProvisioningOptionsTeams(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("resourceProvisioningOptionsTeams"), false));
    }
    
    return grouperAzureGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private String id;
  private String displayName;
  private boolean mailEnabled;
  private String mailNickname;
  private boolean securityEnabled;
  private boolean resourceBehaviorOptionsAllowOnlyMembersToPost;
  private boolean resourceBehaviorOptionsHideGroupInOutlook;
  private boolean resourceBehaviorOptionsSubscribeNewGroupMembers;
  private boolean resourceBehaviorOptionsWelcomeEmailDisabled;
  private boolean resourceProvisioningOptionsTeams;


  /** if this is true then it has the MailEnabled group type */
  private boolean groupTypeMailEnabled;

  /** if this is true then it has the MailEnabledSecurity group type */
  private boolean groupTypeMailEnabledSecurity;
  
  /** if this is true then it has the Security group type */
  private boolean groupTypeSecurity;
  
  /** if this is true then it has the Unified group type */
  private boolean groupTypeUnified;
  private String description;
  private AzureVisibility visibility;

  public static final String fieldsToSelect="description,displayName,groupTypes,id,mailEnabled,mailNickname,securityEnabled,visibility";
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public boolean isMailEnabled() {
    return mailEnabled;
  }
  
  public void setMailEnabled(Boolean mailEnabled) {
    this.mailEnabled = (mailEnabled == null ? defaultMailEnabled : mailEnabled);
  }
  
  public String getMailEnabledDb() {
    return mailEnabled ? "T" : "F";
  }
  
  public void setMailEnabledDb(String mailEnabledDb) {
    this.mailEnabled = GrouperUtil.booleanValue(mailEnabledDb, false);
  }
  
  public String getMailNickname() {
    return mailNickname;
  }
  
  public void setMailNickname(String mailNickname) {
    this.mailNickname = mailNickname;
  }
  
  public boolean isSecurityEnabled() {
    return securityEnabled;
  }
  
  public void setSecurityEnabled(Boolean securityEnabled) {
    this.securityEnabled = (securityEnabled == null ? defaultSecurityEnabled : securityEnabled);
  }
  
  public String getSecurityEnabledDb() {
    return securityEnabled ? "T" : "F";
  }
  
  public void setSecurityEnabledDb(String securityEnabledDb) {
    this.securityEnabled = GrouperUtil.booleanValue(securityEnabledDb, false);
  }
  
  public boolean isGroupTypeSecurity() {
    return groupTypeSecurity;
  }
  
  public void setGroupTypeSecurity(Boolean groupTypeMailSecurity) {
    this.groupTypeSecurity = (groupTypeMailSecurity == null ? false : groupTypeMailSecurity);
  }
  
  public String getGroupTypeSecurityDb() {
    return groupTypeSecurity ? "T" : "F";
  }
  
  public void setGroupTypeSecurityDb(String groupTypeMailSecurityDb) {
    this.groupTypeSecurity = GrouperUtil.booleanValue(groupTypeMailSecurityDb, false);
  }
  
  public boolean isGroupTypeUnified() {
    return groupTypeUnified;
  }
  
  public void setGroupTypeUnified(Boolean groupTypeUnified) {
    this.groupTypeUnified = (groupTypeUnified == null ? false : groupTypeUnified);
  }
  
  public String getGroupTypeUnifiedDb() {
    return groupTypeUnified ? "T" : "F";
  }
  
  public void setGroupTypeUnifiedDb(String groupTypeUnifiedDb) {
    this.groupTypeUnified = GrouperUtil.booleanValue(groupTypeUnifiedDb, false);
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public AzureVisibility getVisibility() {
    return visibility;
  }
  
  public void setVisibility(AzureVisibility visibility) {
    this.visibility = visibility;
  }
  
  
  public boolean isResourceBehaviorOptionsAllowOnlyMembersToPost() {
    return resourceBehaviorOptionsAllowOnlyMembersToPost;
  }

  
  public void setResourceBehaviorOptionsAllowOnlyMembersToPost(
      boolean resourceBehaviorOptionsAllowOnlyMembersToPost) {
    this.resourceBehaviorOptionsAllowOnlyMembersToPost = resourceBehaviorOptionsAllowOnlyMembersToPost;
  }

  
  public boolean isResourceBehaviorOptionsHideGroupInOutlook() {
    return resourceBehaviorOptionsHideGroupInOutlook;
  }

  
  public void setResourceBehaviorOptionsHideGroupInOutlook(
      boolean resourceBehaviorOptionsHideGroupInOutlook) {
    this.resourceBehaviorOptionsHideGroupInOutlook = resourceBehaviorOptionsHideGroupInOutlook;
  }

  
  public boolean isResourceBehaviorOptionsSubscribeNewGroupMembers() {
    return resourceBehaviorOptionsSubscribeNewGroupMembers;
  }

  
  public void setResourceBehaviorOptionsSubscribeNewGroupMembers(
      boolean resourceBehaviorOptionsSubscribeNewGroupMembers) {
    this.resourceBehaviorOptionsSubscribeNewGroupMembers = resourceBehaviorOptionsSubscribeNewGroupMembers;
  }

  
  public boolean isResourceBehaviorOptionsWelcomeEmailDisabled() {
    return resourceBehaviorOptionsWelcomeEmailDisabled;
  }

  
  public void setResourceBehaviorOptionsWelcomeEmailDisabled(boolean resourceBehaviorOptionsWelcomeEmailDisabled) {
    this.resourceBehaviorOptionsWelcomeEmailDisabled = resourceBehaviorOptionsWelcomeEmailDisabled;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperAzureGroup fromJson(JsonNode groupNode) {
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    grouperAzureGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    grouperAzureGroup.displayName = GrouperUtil.jsonJacksonGetString(groupNode, "displayName");
    Set<String> groupTypes = GrouperUtil.jsonJacksonGetStringSet(groupNode, "groupTypes");
    if (groupTypes != null) {
      if (groupTypes.contains("Unified")) {
        grouperAzureGroup.groupTypeUnified = true;
      }
      if (groupTypes.contains("Security")) {
        grouperAzureGroup.groupTypeSecurity = true;
      }
      if (groupTypes.contains("MailEnabled")) {
        grouperAzureGroup.groupTypeMailEnabled = true;
      }
      if (groupTypes.contains("MailEnabledSecurity")) {
        grouperAzureGroup.groupTypeMailEnabledSecurity = true;
      }
    }
    
    grouperAzureGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    grouperAzureGroup.mailEnabled = GrouperUtil.jsonJacksonGetBoolean(groupNode, "mailEnabled", false);
    grouperAzureGroup.mailNickname = GrouperUtil.jsonJacksonGetString(groupNode, "mailNickname");
    grouperAzureGroup.securityEnabled = GrouperUtil.jsonJacksonGetBoolean(groupNode, "securityEnabled", false);
    grouperAzureGroup.setVisibilityDb(GrouperUtil.jsonJacksonGetString(groupNode, "visibility", defaultVisibility));
    
    Set<String> resourceBehaviorOptions = GrouperUtil.jsonJacksonGetStringSet(groupNode, "resourceBehaviorOptions");
    
    if (resourceBehaviorOptions != null) {
      if (resourceBehaviorOptions.contains("AllowOnlyMembersToPost")) {
        grouperAzureGroup.resourceBehaviorOptionsAllowOnlyMembersToPost = true;
      }
      if (resourceBehaviorOptions.contains("HideGroupInOutlook")) {
        grouperAzureGroup.resourceBehaviorOptionsHideGroupInOutlook = true;
      }
      if (resourceBehaviorOptions.contains("SubscribeNewGroupMembers")) {
        grouperAzureGroup.resourceBehaviorOptionsSubscribeNewGroupMembers = true;
      }
      if (resourceBehaviorOptions.contains("WelcomeEmailDisabled")) {
        grouperAzureGroup.resourceBehaviorOptionsWelcomeEmailDisabled = true;
      }
    }
    
    Set<String> resourceProvisioningOptions = GrouperUtil.jsonJacksonGetStringSet(groupNode, "resourceProvisioningOptions");
    
    if (resourceProvisioningOptions != null) {
      if (resourceProvisioningOptions.contains("Teams")) {
        grouperAzureGroup.resourceProvisioningOptionsTeams = true;
      }
    }
    
    return grouperAzureGroup;
  }

  public void setVisibilityDb(String visibilityString) {
    this.visibility = AzureVisibility.valueOfIgnoreCase(visibilityString, false);    
  }

  public String getVisibilityDb() {
    return this.visibility == null ? null : this.visibility.name();
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      result.put("description", this.description);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      if (!StringUtils.isBlank(this.displayName)) {
        result.put("displayName", this.displayName);
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupTypes") || fieldNamesToSet.contains("groupTypeMailEnabled")
        || fieldNamesToSet.contains("groupTypeMailEnabledSecurity") 
        || fieldNamesToSet.contains("groupTypeSecurity") || fieldNamesToSet.contains("groupTypeUnified") ) {      

      Set<String> groupTypes = new HashSet<String>();
      if (this.groupTypeMailEnabled) {
        groupTypes.add("MailEnabled");
      }
      if (this.groupTypeMailEnabledSecurity) {
        groupTypes.add("MailEnabledSecurity");
      }
      if (this.groupTypeSecurity) {
        groupTypes.add("Security");
      }
      if (this.groupTypeUnified) {
        groupTypes.add("Unified");
      }
      // do we need to set null if none set?  hmmm
      if (groupTypes.size() > 0) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "groupTypes", groupTypes);
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
      if (!StringUtils.isBlank(this.id)) {
        result.put("id", this.id);
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailEnabled")) {
      result.put("mailEnabled", this.mailEnabled);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailNickname")) {
      if (!StringUtils.isBlank(this.mailNickname)) {
        result.put("mailNickname", this.mailNickname);
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("securityEnabled")) {
      result.put("securityEnabled", this.securityEnabled);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("visibility")) {
      result.put("visibility", this.getVisibilityDb());
    }
    
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("resourceBehaviorOptions") || fieldNamesToSet.contains("allowOnlyMembersToPost") || 
        fieldNamesToSet.contains("hideGroupInOutlook") || fieldNamesToSet.contains("subscribeNewGroupMembers")
        || fieldNamesToSet.contains("welcomeEmailDisabled")) {
      
      
      Set<String> resourceBehaviorOptions = new HashSet<String>();
      if (this.resourceBehaviorOptionsAllowOnlyMembersToPost ) {
        resourceBehaviorOptions.add("AllowOnlyMembersToPost");
      }
      if (this.resourceBehaviorOptionsHideGroupInOutlook) {
        resourceBehaviorOptions.add("HideGroupInOutlook");
      }
      if (this.resourceBehaviorOptionsSubscribeNewGroupMembers) {
        resourceBehaviorOptions.add("SubscribeNewGroupMembers");
      }
      if (this.resourceBehaviorOptionsWelcomeEmailDisabled) {
        resourceBehaviorOptions.add("WelcomeEmailDisabled");
      }
      // do we need to set null if none set?  hmmm
      if (resourceBehaviorOptions.size() > 0) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "resourceBehaviorOptions", resourceBehaviorOptions);
      }
      
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("resourceProvisioningOptions") || fieldNamesToSet.contains("resourceProvisioningOptionsTeams")) {
      
      Set<String> resourceProvisioningOptions = new HashSet<String>();
      
      if (this.resourceProvisioningOptionsTeams) {
        resourceProvisioningOptions.add("Teams");
      }
      // do we need to set null if none set?  hmmm
      if (resourceProvisioningOptions.size() > 0) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "resourceProvisioningOptions", resourceProvisioningOptions);
      }
      
    }
    
    return result;
  }
  
  
  public boolean isResourceProvisioningOptionsTeams() {
    return resourceProvisioningOptionsTeams;
  }

  
  public void setResourceProvisioningOptionsTeams(
      boolean resourceProvisioningOptionsTeams) {
    this.resourceProvisioningOptionsTeams = resourceProvisioningOptionsTeams;
  }

  public String getResourceProvisioningOptionsTeamsDb() {
    return resourceProvisioningOptionsTeams ? "T" : "F";
  }
  
  public void setResourceProvisioningOptionsTeamsDb(String resourceProvisioningOptionsTeams) {
    this.resourceProvisioningOptionsTeams = GrouperUtil.booleanValue(resourceProvisioningOptionsTeams, false);
  }

  public String getResourceBehaviorOptionsAllowOnlyMembersToPostDb() {
    return resourceBehaviorOptionsAllowOnlyMembersToPost ? "T" : "F";
  }
  
  public void setResourceBehaviorOptionsAllowOnlyMembersToPostDb(String resourceBehaviorOptionsAllowOnlyMembersToPost) {
    this.resourceBehaviorOptionsAllowOnlyMembersToPost = GrouperUtil.booleanValue(resourceBehaviorOptionsAllowOnlyMembersToPost, false);
  }
  
  public String getResourceBehaviorOptionsHideGroupInOutlookDb() {
    return resourceBehaviorOptionsHideGroupInOutlook ? "T" : "F";
  }
  
  public void setResourceBehaviorOptionsHideGroupInOutlookDb(String resourceBehaviorOptionsHideGroupInOutlook) {
    this.resourceBehaviorOptionsHideGroupInOutlook = GrouperUtil.booleanValue(resourceBehaviorOptionsHideGroupInOutlook, false);
  }
  
  
  public String getResourceBehaviorOptionsSubscribeNewGroupMembersDb() {
    return resourceBehaviorOptionsSubscribeNewGroupMembers ? "T" : "F";
  }
  
  public void setResourceBehaviorOptionsSubscribeNewGroupMembersDb(String resourceBehaviorOptionsSubscribeNewGroupMembers) {
    this.resourceBehaviorOptionsSubscribeNewGroupMembers = GrouperUtil.booleanValue(resourceBehaviorOptionsSubscribeNewGroupMembers, false);
  }
  
  public String getResourceBehaviorOptionsWelcomeEmailDisabledDb() {
    return resourceBehaviorOptionsWelcomeEmailDisabled ? "T" : "F";
  }
  
  public void setResourceBehaviorOptionsWelcomeEmailDisabledDb(String resourceBehaviorOptionsWelcomeEmailDisabled) {
    this.resourceBehaviorOptionsWelcomeEmailDisabled = GrouperUtil.booleanValue(resourceBehaviorOptionsWelcomeEmailDisabled, false);
  }
  
  
  public boolean isGroupTypeMailEnabled() {
    return groupTypeMailEnabled;
  }

  
  public void setGroupTypeMailEnabled(Boolean groupTypeMailEnabled) {
    this.groupTypeMailEnabled = (groupTypeMailEnabled == null ? false : groupTypeMailEnabled);
  }

  public String getGroupTypeMailEnabledDb() {
    return groupTypeMailEnabled ? "T" : "F";
  }

  
  public void setGroupTypeMailEnabledDb(String groupTypeMailEnabled) {
    this.groupTypeMailEnabled = GrouperUtil.booleanValue(groupTypeMailEnabled, false);
  }

  
  public boolean isGroupTypeMailEnabledSecurity() {
    return groupTypeMailEnabledSecurity;
  }

  
  public void setGroupTypeMailEnabledSecurity(Boolean groupTypeMailEnabledSecurityDb) {
    this.groupTypeMailEnabledSecurity = (groupTypeMailEnabledSecurityDb == null ? false : groupTypeMailEnabledSecurityDb);
  }
  
  public String getGroupTypeMailEnabledSecurityDb() {
    return groupTypeMailEnabledSecurity ? "T" : "F";
  }

  
  public void setGroupTypeMailEnabledSecurityDb(String groupTypeMailEnabledSecurityDb) {
    this.groupTypeMailEnabledSecurity = GrouperUtil.booleanValue(groupTypeMailEnabledSecurityDb, false);
  }
  
}
