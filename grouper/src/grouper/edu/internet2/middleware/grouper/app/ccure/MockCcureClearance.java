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
 * Model object for a CCure Clearance record (the "group" in provisioning terms), used to back
 * the mock service. There is no create-Clearance endpoint in the real CCure API, so this table
 * starts empty and is seeded directly (e.g. via Hibernate) by whatever is testing against the
 * mock, rather than through an HTTP call.
 */
public class MockCcureClearance {

  private Integer objectId;

  private String guid;

  private String name;

  private String partitionId;

  public Integer getObjectId() {
    return objectId;
  }

  public void setObjectId(Integer objectId) {
    this.objectId = objectId;
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

  public String getPartitionId() {
    return partitionId;
  }

  public void setPartitionId(String partitionId) {
    this.partitionId = partitionId;
  }

  /**
   * The DisplayProperties shape the real client requests: ObjectID, GUID, Name, PartitionID.
   * @return the clearance JSON
   */
  public ObjectNode toJson() {
    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    objectNode.put("ObjectID", this.objectId);
    GrouperUtil.jsonJacksonAssignString(objectNode, "GUID", this.guid);
    GrouperUtil.jsonJacksonAssignString(objectNode, "Name", this.name);
    GrouperUtil.jsonJacksonAssignString(objectNode, "PartitionID", this.partitionId);
    return objectNode;
  }

  /**
   * DDL for the mock table.
   * @param ddlVersionBean ddl version bean
   * @param database database
   */
  public static void createTableCcureClearance(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_ccure_clearance";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "object_id", Types.INTEGER, "11", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "guid", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "partition_id", Types.VARCHAR, "256", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_ccure_clear_name_idx", false, "name");
    }
  }

  @Override
  public String toString() {
    return "MockCcureClearance[objectId=" + objectId + ", guid=" + guid + ", name=" + name + ", partitionId=" + partitionId + "]";
  }

}
