package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperDataRowAssignWrapper {
  
  public GrouperDataRowAssignWrapper(GrouperDataEngine grouperDataEngine, GrouperDataRowAssign grouperDataRowAssign) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataRowAssign = grouperDataRowAssign;
  }

  /**
   * for a user, get the row field assign wrappers by field internal id
   */
  private Map<Long, List<GrouperDataRowFieldAssignWrapper>> rowFieldAssignWrappersByFieldInternalId = new HashMap<>();

  
  public Map<Long, List<GrouperDataRowFieldAssignWrapper>> getRowFieldAssignWrappersByFieldInternalId() {
    return rowFieldAssignWrappersByFieldInternalId;
  }


  public GrouperDataRowAssignWrapper() {
  }

  private GrouperDataEngine grouperDataEngine;
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  private GrouperDataMemberWrapper memberWrapper;
  
  public GrouperDataMemberWrapper getMemberWrapper() {
    return memberWrapper;
  }
  
  public void setMemberWrapper(GrouperDataMemberWrapper memberWrapper) {
    this.memberWrapper = memberWrapper;
  }

  private GrouperDataRowAssign grouperDataRowAssign;

  
  public GrouperDataRowAssign getGrouperDataRowAssign() {
    return grouperDataRowAssign;
  }

  
  public void setGrouperDataRowAssign(GrouperDataRowAssign grouperDataRowAssign) {
    this.grouperDataRowAssign = grouperDataRowAssign;
  }
  
  private GrouperDataRowWrapper grouperDataRowWrapper;


  
  public GrouperDataRowWrapper getGrouperDataRowWrapper() {
    return grouperDataRowWrapper;
  }


  
  public void setGrouperDataRowWrapper(GrouperDataRowWrapper grouperDataRowWrapper) {
    this.grouperDataRowWrapper = grouperDataRowWrapper;
  }

  private MultiKey rowKey = null;

  /**
   * note text values are the actual value and not the dictionary id
   * this is lazy loaded so if data changes it needs to be cleared
   * @return
   */
  public MultiKey rowKey() {

    if (rowKey == null) {
      GrouperDataRowConfig grouperDataRowConfig = this.grouperDataRowWrapper.getGrouperDataRowConfig();
      Set<String> rowKeyFieldConfigIds = grouperDataRowConfig.getRowKeyFieldConfigIds();
      
      if (GrouperUtil.length(rowKeyFieldConfigIds) == 0) {
        throw new RuntimeException("Needs to have a row key: " + grouperDataRowConfig.getConfigId());
      }
      Object[] keyValues = new Object[rowKeyFieldConfigIds.size()];
      int i = 0;
      for (String rowKeyFieldConfigId : rowKeyFieldConfigIds) {
        GrouperDataFieldConfig grouperDataFieldConfig = this.grouperDataEngine.getFieldConfigByConfigId().get(rowKeyFieldConfigId);
        GrouperDataField grouperDataField = this.grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(rowKeyFieldConfigId).getGrouperDataField();
        List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = this.rowFieldAssignWrappersByFieldInternalId.get(grouperDataField.getInternalId());
        GrouperUtil.assertion(GrouperUtil.length(grouperDataRowFieldAssignWrappers) == 1, 
            "Data row field key must have one value: " + grouperDataRowConfig.getConfigId() 
            + ", rowAssignId: " + this.grouperDataRowAssign.getInternalId() + ", field: " + grouperDataFieldConfig.getConfigId());
        if (grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string) {
          keyValues[i] = grouperDataRowFieldAssignWrappers.get(0).getTextValue();
        } else {
          keyValues[i] = grouperDataRowFieldAssignWrappers.get(0).getGrouperDataRowFieldAssign().getValueInteger();
        }
//        GrouperUtil.assertion(keyValues[i] != null, 
//            "Data row field key must not have a null value: " + grouperDataRowConfig.getConfigId() 
//            + ", rowAssignId: " + this.grouperDataRowAssign.getInternalId() + ", field: " + grouperDataFieldConfig.getConfigId());
        i++;
      }
      
      this.rowKey = new MultiKey(keyValues);
      
    }
    return rowKey;
  }

}
