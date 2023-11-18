package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDigitalMarketplaceMembership {
  
  /**
   * group extension
   */
  private String groupName;
  
  /**
   * group extension
   * @return the statusString
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * group extension
   * @param statusString1 the statusString to set
   */
  public void setGroupName(String statusString1) {
    this.groupName = statusString1;
  }
  
  /**
   * netId
   */
  private String loginName;
  
  /**
   * netId
   * @return the netId
   */
  public String getLoginName() {
    return this.loginName;
  }
  
  /**
   * netId
   * @param netId the userName to set
   */
  public void setLoginName(String netId) {
    this.loginName = netId;
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDigitalMarketplaceMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_digital_mp_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "login_name", Types.VARCHAR, "40", false, true);
      
    }
    
  }

  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
    

    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupName")) {      
      result.put("groupName", this.groupName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("loginName")) {      
      result.put("loginName", this.loginName);
    }
    return result;
  }

}
