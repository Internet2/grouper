package edu.internet2.middleware.grouper.app.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAzureUser {

  private boolean accountEnabled;

  private String displayName;

  private String id;

  private String mailNickname;

  private String onPremisesImmutableId;

  private String userPrincipalName;

  public static final String fieldsToSelect="accountEnabled,displayName,id,mailNickname,onPremisesImmutableId,userPrincipalName";
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.assignAttributeValue("accountEnabled", this.accountEnabled);
    targetEntity.assignAttributeValue("displayName", this.displayName);
    targetEntity.setId(this.id);
    targetEntity.assignAttributeValue("mailNickName", this.mailNickname);
    targetEntity.assignAttributeValue("onPremisesImmutableId", this.onPremisesImmutableId);
    targetEntity.assignAttributeValue("userPrincipalName", this.userPrincipalName);
    return targetEntity;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isAccountEnabled() {
    return accountEnabled;
  }

  public void setAccountEnabled(boolean accountEnabled) {
    this.accountEnabled = accountEnabled;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getOnPremisesImmutableId() {
    return onPremisesImmutableId;
  }

  public void setOnPremisesImmutableId(String onPremisesImmutableId) {
    this.onPremisesImmutableId = onPremisesImmutableId;
  }

  public String getMailNickname() {
    return mailNickname;
  }

  public void setMailNickname(String mailNickName) {
    this.mailNickname = mailNickName;
  }

  public String getUserPrincipalName() {
    return userPrincipalName;
  }

  public void setUserPrincipalName(String userPrincipalName) {
    this.userPrincipalName = userPrincipalName;
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperAzureUser fromJson(JsonNode entityNode) {
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    
    grouperAzureUser.accountEnabled = GrouperUtil.jsonJacksonGetBoolean(entityNode, "accountEnabled", false);
    grouperAzureUser.displayName = GrouperUtil.jsonJacksonGetString(entityNode, "displayName");
    grouperAzureUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
    grouperAzureUser.mailNickname = GrouperUtil.jsonJacksonGetString(entityNode, "mailNickname");
    grouperAzureUser.onPremisesImmutableId = GrouperUtil.jsonJacksonGetString(entityNode, "onPremisesImmutableId");
    grouperAzureUser.userPrincipalName = GrouperUtil.jsonJacksonGetString(entityNode, "userPrincipalName");
    
    return grouperAzureUser;
  }

  public static void main(String[] args) {
    
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    
    grouperAzureUser.setAccountEnabled(true);
    grouperAzureUser.setDisplayName("dispName");
    grouperAzureUser.setId("id");
    grouperAzureUser.setMailNickname("mailNick");
    grouperAzureUser.setOnPremisesImmutableId("onPrem");
    grouperAzureUser.setUserPrincipalName("userPri");
  
    String json = GrouperUtil.jsonJacksonToString(grouperAzureUser.toJson());
    System.out.println(json);
    
    grouperAzureUser = GrouperAzureUser.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperAzureUser.toString());
    
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
  
    GrouperUtil.jsonJacksonAssignBoolean(result, "accountEnabled", this.accountEnabled);
    GrouperUtil.jsonJacksonAssignString(result, "displayName", this.displayName);
    GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    GrouperUtil.jsonJacksonAssignString(result, "mailNickname", this.mailNickname);
    GrouperUtil.jsonJacksonAssignString(result, "onPremisesImmutableId", this.onPremisesImmutableId);
    GrouperUtil.jsonJacksonAssignString(result, "userPrincipalName", this.userPrincipalName);
    
    return result;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
