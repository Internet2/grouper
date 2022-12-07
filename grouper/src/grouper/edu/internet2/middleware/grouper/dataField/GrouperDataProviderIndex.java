package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataProviderIndex {


  private Map<Long, GrouperDataFieldAssignWrapper> fieldAssignWrapperByInternalId = new HashMap<>();
  
  
  
  
  public Map<Long, GrouperDataFieldAssignWrapper> getFieldAssignWrapperByInternalId() {
    return fieldAssignWrapperByInternalId;
  }

  private GrouperDataEngine grouperDataEngine;
  
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }
  
  private Map<Long, GrouperDataRowWrapper> rowWrapperByInternalId = new HashMap<Long, GrouperDataRowWrapper>();

  
  
  
  public Map<Long, GrouperDataRowWrapper> getRowWrapperByInternalId() {
    return rowWrapperByInternalId;
  }

  
  private Map<Long, GrouperDataFieldWrapper> fieldWrapperByInternalId = new HashMap<Long, GrouperDataFieldWrapper>();

  
  public Map<Long, GrouperDataFieldWrapper> getFieldWrapperByInternalId() {
    return fieldWrapperByInternalId;
  }

  
  private Map<Long, String> dictionaryTextByInternalId = new HashMap<>();

  private Map<Long, GrouperDataMemberWrapper> memberWrapperByInternalId = new HashMap<>();

  private Map<Long, GrouperDataRowAssignWrapper> rowAssignWrapperByInternalId = new HashMap<>();

  private Map<Long, GrouperDataRowFieldAssignWrapper> rowFieldAssignWrapperByInternalId = new HashMap<>();

  private Map<String, GrouperDataFieldWrapper> fieldWrapperByConfigId = new HashMap<String, GrouperDataFieldWrapper>();
  
  
  public Map<String, GrouperDataFieldWrapper> getFieldWrapperByConfigId() {
    return fieldWrapperByConfigId;
  }



  public Map<Long, GrouperDataRowFieldAssignWrapper> getRowFieldAssignWrapperByInternalId() {
    return rowFieldAssignWrapperByInternalId;
  }



  public Map<Long, GrouperDataRowAssignWrapper> getRowAssignWrapperByInternalId() {
    return rowAssignWrapperByInternalId;
  }



  public Map<Long, String> getDictionaryTextByInternalId() {
    return dictionaryTextByInternalId;
  }

  
  
  public Map<Long, GrouperDataMemberWrapper> getMemberWrapperByInternalId() {
    return memberWrapperByInternalId;
  }

  
}
