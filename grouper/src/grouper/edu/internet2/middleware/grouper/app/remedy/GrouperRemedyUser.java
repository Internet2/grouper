package edu.internet2.middleware.grouper.app.remedy;

import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * grouper box user
 * @author mchyzer
 *
 */
public class GrouperRemedyUser {

  /**
   * cache connections
   */
  private static ExpirableCache<Boolean, Map<String, GrouperRemedyUser>> retrieveUsersCache = new ExpirableCache<Boolean, Map<String, GrouperRemedyUser>>(60*12);
  
  /**
   * 
   * @return box api connection never null
   */
  public synchronized static Map<String, GrouperRemedyUser> retrieveUsers() {
    
    Map<String, GrouperRemedyUser> usersMap = retrieveUsersCache.get(Boolean.TRUE);
    
    if (usersMap == null) {
      
      usersMap = GrouperRemedyCommands.retrieveRemedyUsers();
      
      retrieveUsersCache.put(Boolean.TRUE, usersMap);
    }
    
    return usersMap;
  }
  
  /**
   * remedy id for a person
   */
  private String personId;

  /**
   * netId of user
   */
  private String remedyLoginId;

  
  /**
   * remedy id for a person
   * @return the personId
   */
  public String getPersonId() {
    return this.personId;
  }

  
  /**
   * remedy id for a person
   * @param personId1 the personId to set
   */
  public void setPersonId(String personId1) {
    this.personId = personId1;
  }

  
  /**
   * netId of user
   * @return the remedyLoginId
   */
  public String getRemedyLoginId() {
    return this.remedyLoginId;
  }

  
  /**
   * netId of user
   * @param remedyLoginId1 the remedyLoginId to set
   */
  public void setRemedyLoginId(String remedyLoginId1) {
    this.remedyLoginId = remedyLoginId1;
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableRemedyUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_remedy_user";
  
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
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_remedy_user_unique_user_name", true, "user_name");
      
    }
    
  }
  
  /**
   * convert from jackson json
   * @param entityNode
   * @return the user
   */
  public static GrouperRemedyUser fromJson(JsonNode entityNode) {
    GrouperRemedyUser grouperRemedyUser = new GrouperRemedyUser();
    
//    grouperDuoUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "firstname");
//    grouperDuoUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "lastname");
//    grouperDuoUser.realName = GrouperUtil.jsonJacksonGetString(entityNode, "realname");
//    grouperDuoUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "username");
//    grouperDuoUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");
//    grouperDuoUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "user_id");
    
//    ArrayNode groupsNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(entityNode, "groups");
//    Set<GrouperDuoGroup> grouperDuoGroups = new HashSet<GrouperDuoGroup>();
//    if (groupsNode != null) {
//      Iterator<JsonNode> groupsIterator = groupsNode.iterator();
//      while(groupsIterator.hasNext()) {
//        JsonNode groupNode = groupsIterator.next();
//        GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromJson(groupNode);
//        grouperDuoGroups.add(grouperDuoGroup);
//      }
//    }
    
//    grouperDuoUser.groups = grouperDuoGroups;
    
    return grouperRemedyUser;
  }
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    targetEntity.setId(this.personId);
    targetEntity.setLoginId(this.remedyLoginId);
    return targetEntity;
  }

  
  
}
