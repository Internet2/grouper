package edu.internet2.middleware.grouper.app.google;

import java.sql.Types;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.jsoup.internal.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
      
      //settings
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_add", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_join", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_view_membership", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_view_group", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_invite", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "who_can_post_message", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "allow_external_members", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "allow_web_posting", Types.VARCHAR, "1", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTableName, "mock_google_group_name_idx", true, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.setName(this.name);
    targetGroup.setId(this.id);
    targetGroup.assignAttributeValue("email", this.email);

    // group settings
    targetGroup.assignAttributeValue("whoCanAdd", this.whoCanAdd);
    targetGroup.assignAttributeValue("whoCanJoin", this.whoCanJoin);
    targetGroup.assignAttributeValue("whoCanViewMembership", this.whoCanViewMembership);
    targetGroup.assignAttributeValue("whoCanViewGroup", this.whoCanViewGroup);
    targetGroup.assignAttributeValue("whoCanInvite", this.whoCanInvite);
    targetGroup.assignAttributeValue("whoCanPostMessage", this.whoCanPostMessage);
    targetGroup.assignAttributeValue("allowExternalMembers", this.allowExternalMembers);
    targetGroup.assignAttributeValue("allowWebPosting", this.allowWebPosting);
    
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

    if (fieldNamesToSet == null || fieldNamesToSet.contains("allowWebPosting")) {      
      grouperGoogleGroup.setAllowWebPosting(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("allowWebPosting"), false));
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
  
  // group settings
  private String whoCanAdd;
  private String whoCanJoin;
  private String whoCanViewMembership;
  private String whoCanViewGroup;
  private String whoCanInvite;
  private Boolean allowExternalMembers;
  private String whoCanPostMessage;
  private Boolean allowWebPosting;
  
  public String getAllowExternalMembersDb() {
    return allowExternalMembers == null ? "F" : allowExternalMembers ? "T" : "F";
  }

  
  public void setAllowExternalMembersDb(String allowExternalMembers) {
    this.allowExternalMembers = GrouperUtil.booleanObjectValue(allowExternalMembers);
  }
  
  public String getAllowWebPostingDb() {
    return allowWebPosting == null ? "F": allowWebPosting ? "T" : "F";
  }

  
  public void setAllowWebPostingDb(String allowWebPosting) {
    this.allowWebPosting = GrouperUtil.booleanObjectValue(allowWebPosting);
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
    
    this.whoCanAdd = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanAdd");
    this.whoCanJoin = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanJoin");
    this.whoCanViewMembership = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanViewMembership");
    this.whoCanViewGroup = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanViewGroup");
    this.whoCanInvite = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanInvite");
    this.allowExternalMembers = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "allowExternalMembers");
    this.whoCanPostMessage = GrouperUtil.jsonJacksonGetString(groupSettingsNode, "whoCanPostMessage");
    this.allowWebPosting = GrouperUtil.jsonJacksonGetBoolean(groupSettingsNode, "allowWebPosting");
    
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJsonGroupOnly(Set<String> fieldNamesToSet) {
    
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

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
    
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

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
    
    return result;
  }

}
