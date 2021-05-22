package edu.internet2.middleware.grouper.app.duo;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoUser {

  // email, first_name, groups, last_name, real_name, user_id, user_name
  
  private String email;
  
  private String firstName;
  
  private Set<GrouperDuoGroup> groups;
  
  private String lastName;
  
  private String realName;
  
  private String id; // userId

  private String userName;
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.assignAttributeValue("firstname", this.firstName);
    targetEntity.assignAttributeValue("lastname", this.lastName);
    targetEntity.setId(this.id);
    targetEntity.setEmail(this.email);
    targetEntity.setLoginId(this.userName);
    targetEntity.setName(this.realName);
    return targetEntity;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  
  public String getEmail() {
    return email;
  }

  
  public void setEmail(String email) {
    this.email = email;
  }

  
  public String getFirstName() {
    return firstName;
  }

  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  
  
  
  public Set<GrouperDuoGroup> getGroups() {
    return groups;
  }

  
  public void setGroups(Set<GrouperDuoGroup> groups) {
    this.groups = groups;
  }

  public String getLastName() {
    return lastName;
  }

  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  
  public String getRealName() {
    return realName;
  }

  
  public void setRealName(String realName) {
    this.realName = realName;
  }

  
  public String getUserName() {
    return userName;
  }

  
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperDuoUser fromJson(JsonNode entityNode) {
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    
    grouperDuoUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "firstname");
    grouperDuoUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "lastname");
    grouperDuoUser.realName = GrouperUtil.jsonJacksonGetString(entityNode, "realname");
    grouperDuoUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "username");
    grouperDuoUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");
    grouperDuoUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "user_id");
    
    ArrayNode groupsNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(entityNode, "groups");
    Set<GrouperDuoGroup> grouperDuoGroups = new HashSet<GrouperDuoGroup>();
    if (groupsNode != null) {
      Iterator<JsonNode> groupsIterator = groupsNode.iterator();
      while(groupsIterator.hasNext()) {
        JsonNode groupNode = groupsIterator.next();
        GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromJson(groupNode);
        grouperDuoGroups.add(grouperDuoGroup);
      }
    }
    
    grouperDuoUser.groups = grouperDuoGroups;
    
    return grouperDuoUser;
  }

  public static void main(String[] args) {
    
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    
    grouperDuoUser.setFirstName("firstName");
    grouperDuoUser.setLastName("lastName");
    grouperDuoUser.setRealName("realName");
    grouperDuoUser.setEmail("test@example.com");
    grouperDuoUser.setId("id");
    grouperDuoUser.setUserName("userName");
    
    Set<GrouperDuoGroup> groups = new HashSet<GrouperDuoGroup>();
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.setDesc("test description");
    grouperDuoGroup.setGroup_id("testGroupId");
    grouperDuoGroup.setName("groupName");
    groups.add(grouperDuoGroup);
    grouperDuoUser.setGroups(groups);
  
    String json = GrouperUtil.jsonJacksonToString(grouperDuoUser.toJson());
    System.out.println(json);
    
    grouperDuoUser = GrouperDuoUser.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperDuoUser.toString());
    
  }

  /**
   * convert from jackson json
   * @return the grouper duo user
   */
  public ObjectNode toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
  
    GrouperUtil.jsonJacksonAssignString(result, "alias1", null);
    GrouperUtil.jsonJacksonAssignString(result, "alias2", null);
    GrouperUtil.jsonJacksonAssignString(result, "alias3", null);
    GrouperUtil.jsonJacksonAssignString(result, "alias4", null);
    GrouperUtil.jsonJacksonAssignString(result, "aliases", null);
    GrouperUtil.jsonJacksonAssignLong(result, "created", new Date().getTime()); //TODO should we store in in database?
    GrouperUtil.jsonJacksonAssignString(result, "email", this.email);
    GrouperUtil.jsonJacksonAssignString(result, "firstname", this.firstName);
    GrouperUtil.jsonJacksonAssignBoolean(result, "is_enrolled", true);
    GrouperUtil.jsonJacksonAssignLong(result, "last_directory_sync", new Date().getTime());
    GrouperUtil.jsonJacksonAssignLong(result, "last_login", new Date().getTime());
    
    
    ArrayNode groupsNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperDuoGroup grouperDuoGroup: GrouperUtil.nonNull(this.groups)) {
      groupsNode.add(grouperDuoGroup.toJson());
    }
    
    result.set("groups", groupsNode);
    
    GrouperUtil.jsonJacksonAssignString(result, "lastname", this.lastName);
    GrouperUtil.jsonJacksonAssignString(result, "notes", "");
    GrouperUtil.jsonJacksonAssignStringArray(result, "phones", new ArrayList<String>());
    GrouperUtil.jsonJacksonAssignString(result, "realname", this.realName);
    GrouperUtil.jsonJacksonAssignString(result, "status", "active");
    GrouperUtil.jsonJacksonAssignStringArray(result, "tokens", new ArrayList<String>());
    GrouperUtil.jsonJacksonAssignStringArray(result, "u2ftokens", new ArrayList<String>());
    
    GrouperUtil.jsonJacksonAssignString(result, "user_id", this.id);
    GrouperUtil.jsonJacksonAssignString(result, "username", this.userName);
    GrouperUtil.jsonJacksonAssignStringArray(result, "webauthncredentials", new ArrayList<String>());
    
    return result;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDuoUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_duo_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      // email, first_name, groups, last_name, real_name, user_id, user_name
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "real_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_name", Types.VARCHAR, "256", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_duo_user_unique_user_name", true, "user_name");
      
    }
    
  }

}
