package edu.internet2.middleware.grouper.app.remedyV2;

import java.sql.Types;
import java.util.Set;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperRemedyUser {
  
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

  public static final String fieldsToSelect="Person ID,Remedy Login ID";
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.assignAttributeValue("personId", this.personId);
    targetEntity.assignAttributeValue("remedyLoginId", this.remedyLoginId);
    
    
    return targetEntity;
  }
  
  
  /**
   * 
   * @param targetEntity
   * @return
   */
  public static GrouperRemedyUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperRemedyUser grouperRemedyUser = new GrouperRemedyUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("personId")) {      
      grouperRemedyUser.setPersonId(targetEntity.retrieveAttributeValueString("personId"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("remedyLoginId")) {      
      grouperRemedyUser.setRemedyLoginId(targetEntity.retrieveAttributeValueString("remedyLoginId"));
    }
    
    return grouperRemedyUser;

  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperRemedyUser fromJson(JsonNode entityNode) {

    if (entityNode == null || !entityNode.has("Person ID")) {
      return null;
    }

    GrouperRemedyUser grouperRemedyUser = new GrouperRemedyUser();
    
    grouperRemedyUser.remedyLoginId = GrouperUtil.jsonJacksonGetString(entityNode, "Remedy Login ID");

    return grouperRemedyUser;
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
  
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Person ID")) {      
      GrouperUtil.jsonJacksonAssignString(result, "Person ID", this.personId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Remedy Login ID")) {      
      GrouperUtil.jsonJacksonAssignString(result, "Remedy Login ID", this.remedyLoginId);
    }
    
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
  public static void createTableRemedyUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_remedy_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "remedy_login_id", Types.VARCHAR, "40", true, true);
      
    }
    
  }

}
