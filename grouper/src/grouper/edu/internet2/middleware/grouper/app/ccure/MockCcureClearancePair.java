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
 * Model object for a CCure PersonnelClearancePair record (a Personnel/Clearance membership),
 * used to back the mock service. Unlike Personnel and Clearance, this is created and removed
 * through the real API (PersistToContainer / RemoveFromContainer), so the mock handler manages
 * this table's rows directly.
 */
public class MockCcureClearancePair {

  private Integer objectId;

  private Integer personnelId;

  private Integer clearanceId;

  public Integer getObjectId() {
    return objectId;
  }

  public void setObjectId(Integer objectId) {
    this.objectId = objectId;
  }

  public Integer getPersonnelId() {
    return personnelId;
  }

  public void setPersonnelId(Integer personnelId) {
    this.personnelId = personnelId;
  }

  public Integer getClearanceId() {
    return clearanceId;
  }

  public void setClearanceId(Integer clearanceId) {
    this.clearanceId = clearanceId;
  }

  /**
   * The DisplayProperties shape the real client requests: ObjectID, PersonnelID, ClearanceID.
   * @return the pair JSON
   */
  public ObjectNode toJson() {
    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    objectNode.put("ObjectID", this.objectId);
    objectNode.put("ClearanceID", this.clearanceId);
    objectNode.put("PersonnelID", this.personnelId);
    return objectNode;
  }

  /**
   * DDL for the mock table.
   * @param ddlVersionBean ddl version bean
   * @param database database
   */
  public static void createTableCcureClearancePair(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_ccure_clearance_pair";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "object_id", Types.INTEGER, "11", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "personnel_id", Types.INTEGER, "11", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "clearance_id", Types.INTEGER, "11", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_ccure_pair_pers_idx", false, "personnel_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_ccure_pair_clear_idx", false, "clearance_id");
    }
  }

  @Override
  public String toString() {
    return "MockCcureClearancePair[objectId=" + objectId + ", personnelId=" + personnelId + ", clearanceId=" + clearanceId + "]";
  }

}
