package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataMemberWrapper {

  public GrouperDataMemberWrapper() {
  }

  public GrouperDataMemberWrapper(GrouperDataEngine grouperDataEngine, long memberInternalId) {
    this.grouperDataEngine = grouperDataEngine;
    this.internalId = memberInternalId;
  }

  /**
   * key is row internal id.
   * value is a list of rows
   * a row is a map of fieldInternalId to list of values
   */
  private Map<Long, List<Map<Long, List<Object>>>> dataProviderDataByDataRowInternalId = new HashMap<>();

  
  public Map<Long, List<Map<Long, List<Object>>>> getDataProviderDataByDataRowInternalId() {
    return dataProviderDataByDataRowInternalId;
  }

  private Map<Long, List<Object>> dataProviderDataByDataFieldInternalId = new HashMap<>();
  
  public Map<Long, List<Object>> getDataProviderDataByDataFieldIternalId() {
    return dataProviderDataByDataFieldInternalId;
  }

  private Map<String, List<Object[]>> queryConfigIdToRowData = new HashMap<>();
  
  public Map<String, List<Object[]>> getQueryConfigIdToRowData() {
    return queryConfigIdToRowData;
  }

  private GrouperDataEngine grouperDataEngine;
  
  private Member member;

  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }
  
  private long internalId;
  
  

  
  public long getInternalId() {
    return internalId;
  }


  
  public void setInternalId(long internalId) {
    this.internalId = internalId;
  }
  private Map<Long, Set<Object>> fieldIdToDataProviderValues = new HashMap<Long, Set<Object>>();
  
  public Map<Long, Set<Object>> getFieldIdToDataProviderValues() {
    return fieldIdToDataProviderValues;
  }

  private Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = new HashMap<>();

  
  public Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> getFieldIdToValueToFieldAssignWrapper() {
    return fieldIdToValueToFieldAssignWrapper;
  }

  /**
   * for a field id, list the values
   */
  private Map<Long, Set<Object>> fieldIdToValues = new HashMap<>();

  public Map<Long, Set<Object>> getFieldIdToValues() {
    return fieldIdToValues;
  }

  private Map<Long, List<GrouperDataRowAssignWrapper>> rowAssignWrappersByRowInternalId = new HashMap<>();
  
  /**
   * for a user, get the field assign wrappers by field internal id
   */
  private Map<Long, List<GrouperDataFieldAssignWrapper>> fieldAssignWrappersByFieldInternalId = new HashMap<>();


  
  public Map<Long, List<GrouperDataRowAssignWrapper>> getRowAssignWrappersByRowInternalId() {
    return rowAssignWrappersByRowInternalId;
  }


  
  public Map<Long, List<GrouperDataFieldAssignWrapper>> getFieldAssignWrappersByFieldInternalId() {
    return fieldAssignWrappersByFieldInternalId;
  }

  
  public Member getMember() {
    return member;
  }

  
  public void setMember(Member member) {
    this.member = member;
  }
}
