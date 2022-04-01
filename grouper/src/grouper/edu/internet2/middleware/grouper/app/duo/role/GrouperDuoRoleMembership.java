package edu.internet2.middleware.grouper.app.duo.role;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoRoleMembership {

  /**
   * @param ddlVersionBean
   * @param database
   */
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }


  private String id;
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  private String groupId;
  
  private String userId;
  
  public String getGroupId() {
    return groupId;
  }


  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }


  
  public String getUserId() {
    return userId;
  }


  
  public void setUserId(String userId) {
    this.userId = userId;
  }
}
