package edu.internet2.middleware.grouper.app.azure;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAzureGroup {

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

    String json = GrouperUtil.jsonJacksonToString(grouperAzureGroup.toJson());
    System.out.println(json);
    
    grouperAzureGroup = GrouperAzureGroup.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperAzureGroup.toString());
    
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
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperAzureGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
    
    if (fieldNamesToSet.contains("description")) {      
      grouperAzureGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    if (fieldNamesToSet.contains("displayName")) {      
      grouperAzureGroup.setDisplayName(targetGroup.getDisplayName());
    }
    if (fieldNamesToSet.contains("groupTypeMailEnabled")) {      
      grouperAzureGroup.setGroupTypeMailEnabled(targetGroup.retrieveAttributeValueBoolean("groupTypeMailEnabled"));
    }
    if (fieldNamesToSet.contains("groupTypeMailEnabledSecurity")) {      
      grouperAzureGroup.setGroupTypeMailEnabledSecurity(targetGroup.retrieveAttributeValueBoolean("groupTypeMailEnabledSecurity"));
    }
    if (fieldNamesToSet.contains("groupTypeSecurity")) {      
      grouperAzureGroup.setGroupTypeSecurity(targetGroup.retrieveAttributeValueBoolean("groupTypeSecurity"));
    }
    if (fieldNamesToSet.contains("groupTypeUnified")) {      
      grouperAzureGroup.setGroupTypeUnified(targetGroup.retrieveAttributeValueBoolean("groupTypeUnified"));
    }
    if (fieldNamesToSet.contains("id")) {      
      grouperAzureGroup.setId(targetGroup.getId());
    }
    if (fieldNamesToSet.contains("mailEnabled")) {      
      grouperAzureGroup.setMailEnabled(targetGroup.retrieveAttributeValueBoolean("mailEnabled"));
    }
    if (fieldNamesToSet.contains("mailNickname")) {      
      grouperAzureGroup.setMailNickname(targetGroup.retrieveAttributeValueString("mailNickname"));
    }
    if (fieldNamesToSet.contains("securityEnabled")) {      
      grouperAzureGroup.setSecurityEnabled(targetGroup.retrieveAttributeValueBoolean("securityEnabled"));
    }
    if (fieldNamesToSet.contains("visibility")) {      
      grouperAzureGroup.setVisibilityString(targetGroup.retrieveAttributeValueString("visibility"));
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

  public static final String fieldsToSelect="description,displayName,groupTypeMailEnabled,groupTypeMailEnabledSecurity,groupTypeSecurity,groupTypeUnified,id,mailEnabled,mailNickname,securityEnabled,visibility";
  
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
  
  public void setMailEnabled(boolean mailEnabled) {
    this.mailEnabled = mailEnabled;
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
  
  public void setSecurityEnabled(boolean securityEnabled) {
    this.securityEnabled = securityEnabled;
  }
  
  public boolean isGroupTypeSecurity() {
    return groupTypeSecurity;
  }
  
  public void setGroupTypeSecurity(boolean groupTypeMailSecurity) {
    this.groupTypeSecurity = groupTypeMailSecurity;
  }
  
  public boolean isGroupTypeUnified() {
    return groupTypeUnified;
  }
  
  public void setGroupTypeUnified(boolean groupTypeUnified) {
    this.groupTypeUnified = groupTypeUnified;
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
    grouperAzureGroup.setVisibilityString(GrouperUtil.jsonJacksonGetString(groupNode, "visibility"));
    
    return grouperAzureGroup;
  }

  public void setVisibilityString(String visibilityString) {
    this.visibility = AzureVisibility.valueOfIgnoreCase(visibilityString, false);    
  }

  public String getVisibilityString() {
    return this.visibility == null ? null : this.visibility.name();
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

    GrouperUtil.jsonJacksonAssignString(result, "description", this.description);
    GrouperUtil.jsonJacksonAssignString(result, "displayName", this.displayName);
    
    {
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
      if (groupTypes.size() > 0) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "groupTypes", groupTypes);
      }
    }
    
    GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    GrouperUtil.jsonJacksonAssignBoolean(result, "mailEnabled", this.mailEnabled);
    GrouperUtil.jsonJacksonAssignString(result, "mailNickname", this.mailNickname);
    GrouperUtil.jsonJacksonAssignBoolean(result, "securityEnabled", this.securityEnabled);
    GrouperUtil.jsonJacksonAssignString(result, "visibility", this.getVisibilityString());
    
    return result;
  }

  
  public boolean isGroupTypeMailEnabled() {
    return groupTypeMailEnabled;
  }

  
  public void setGroupTypeMailEnabled(boolean groupTypeMailEnabled) {
    this.groupTypeMailEnabled = groupTypeMailEnabled;
  }

  
  public boolean isGroupTypeMailEnabledSecurity() {
    return groupTypeMailEnabledSecurity;
  }

  
  public void setGroupTypeMailEnabledSecurity(boolean groupTypeMailEnabledSecurity) {
    this.groupTypeMailEnabledSecurity = groupTypeMailEnabledSecurity;
  }
  
}
