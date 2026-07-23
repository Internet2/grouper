package edu.internet2.middleware.grouper.app.ccure;

import java.sql.Types;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Model object for a CCure Personnel record, used to back the mock service. There is no
 * create-Personnel endpoint in the real CCure API (people/badges are provisioned outside of
 * Grouper), so this table starts empty and is seeded directly (e.g. via Hibernate) by whatever
 * is testing against the mock, rather than through an HTTP call.
 */
public class MockCcurePersonnel {

  private Integer personnelId;

  private String guid;

  private String name;

  private String int1;

  public Integer getPersonnelId() {
    return personnelId;
  }

  public void setPersonnelId(Integer personnelId) {
    this.personnelId = personnelId;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getInt1() {
    return int1;
  }

  public void setInt1(String int1) {
    this.int1 = int1;
  }

  /**
   * The DisplayProperties shape the real client requests: ObjectID, GUID, Name, Int1.
   * @return the personnel JSON
   */
  public ObjectNode toJson() {
    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    objectNode.put("ObjectID", this.personnelId);
    GrouperUtil.jsonJacksonAssignString(objectNode, "GUID", this.guid);
    GrouperUtil.jsonJacksonAssignString(objectNode, "Name", this.name);
    GrouperUtil.jsonJacksonAssignString(objectNode, "Int1", this.int1);
    return objectNode;
  }

  /**
   * DDL for the mock table.
   * @param ddlVersionBean ddl version bean
   * @param database database
   */
  public static void createTableCcurePersonnel(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_ccure_personnel";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "personnel_id", Types.INTEGER, "11", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "guid", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "int1", Types.VARCHAR, "256", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_ccure_pers_int1_idx", false, "int1");
    }
  }

  @Override
  public String toString() {
    return "MockCcurePersonnel[personnelId=" + personnelId + ", guid=" + guid + ", name=" + name + ", int1=" + int1 + "]";
  }

}
