package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrouperDataRowWrapper {
  
  public GrouperDataRowWrapper() {
    super();
  }

  public GrouperDataRowWrapper(GrouperDataEngine grouperDataEngine, GrouperDataRow grouperDataRow) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataRow = grouperDataRow;
    this.grouperDataRowConfig = grouperDataEngine.getRowConfigByConfigId().get(grouperDataRow.getConfigId());
  }

  private GrouperDataRowConfig grouperDataRowConfig;
  
  public GrouperDataRowConfig getGrouperDataRowConfig() {
    return grouperDataRowConfig;
  }
  
  public void setGrouperDataRowConfig(GrouperDataRowConfig grouperDataRowConfig) {
    this.grouperDataRowConfig = grouperDataRowConfig;
  }

  private GrouperDataEngine grouperDataEngine;
  
  
  private Map<Long, List<GrouperDataRowFieldAssignWrapper>> rowFieldAssignsByFieldInternalId = new HashMap<>();

  
  public Map<Long, List<GrouperDataRowFieldAssignWrapper>> getRowFieldAssignsByFieldInternalId() {
    return rowFieldAssignsByFieldInternalId;
  }

  
  public void setRowFieldAssignsByFieldInternalId(
      Map<Long, List<GrouperDataRowFieldAssignWrapper>> rowFieldAssignsByFieldInternalId) {
    this.rowFieldAssignsByFieldInternalId = rowFieldAssignsByFieldInternalId;
  }

  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  private GrouperDataRow grouperDataRow;

  
  public GrouperDataRow getGrouperDataRow() {
    return grouperDataRow;
  }

  
  public void setGrouperDataRow(GrouperDataRow grouperDataRow) {
    this.grouperDataRow = grouperDataRow;
  }
  
}
