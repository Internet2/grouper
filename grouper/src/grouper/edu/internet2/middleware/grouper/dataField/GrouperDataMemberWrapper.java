package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;

/**
 * wraps a single member's data in the context of a data provider engine.
 * holds both the raw data from the data provider source (queries) and the
 * indexed current state from Grouper (field assigns, row assigns).
 *
 * during a full sync, the data provider logic populates the "source" side
 * (dataProviderDataByDataFieldInternalId, dataProviderDataByDataRowInternalId,
 * queryConfigIdToRowData, fieldIdToDataProviderValues) from SQL queries,
 * and the "grouper" side (fieldIdToValues, fieldIdToValueToFieldAssignWrapper,
 * fieldAssignWrappersByFieldInternalId, rowAssignWrappersByRowInternalId) from
 * the existing database state. the sync then compares these two sides to
 * determine inserts, deletes, and updates.
 */
public class GrouperDataMemberWrapper {

  public GrouperDataMemberWrapper() {
  }

  /**
   * @param grouperDataEngine the data engine this member belongs to
   * @param memberInternalId the member internal ID (from grouper_members table)
   */
  public GrouperDataMemberWrapper(GrouperDataEngine grouperDataEngine, long memberInternalId) {
    this.grouperDataEngine = grouperDataEngine;
    this.internalId = memberInternalId;
  }

  /**
   * raw row data from the data provider source, indexed by data row internal ID.
   * each row internal ID maps to a list of row instances (since a member can have
   * multiple rows of the same type, e.g. multiple addresses). each row instance
   * is a map from field internal ID to a list of values for that field.
   */
  private Map<Long, List<Map<Long, List<Object>>>> dataProviderDataByDataRowInternalId = new HashMap<>();

  /**
   * @return raw row data from the data provider source, keyed by data row internal ID
   */
  public Map<Long, List<Map<Long, List<Object>>>> getDataProviderDataByDataRowInternalId() {
    return dataProviderDataByDataRowInternalId;
  }

  /**
   * raw field data from the data provider source, indexed by data field internal ID.
   * each field internal ID maps to a list of values (multi-valued fields are possible).
   * this is populated during retrieveSourceData from the SQL query results.
   */
  private Map<Long, List<Object>> dataProviderDataByDataFieldInternalId = new HashMap<>();

  /**
   * @return raw field data from the data provider source, keyed by data field internal ID
   */
  public Map<Long, List<Object>> getDataProviderDataByDataFieldIternalId() {
    return dataProviderDataByDataFieldInternalId;
  }

  /**
   * raw query results from the data provider source, indexed by query config ID.
   * each query config ID maps to a list of result rows (Object arrays matching
   * the column order of the query). this is the intermediate form before the data
   * is processed into dataProviderDataByDataFieldInternalId / dataProviderDataByDataRowInternalId.
   */
  private Map<String, List<Object[]>> queryConfigIdToRowData = new HashMap<>();

  /**
   * @return raw query results keyed by query config ID
   */
  public Map<String, List<Object[]>> getQueryConfigIdToRowData() {
    return queryConfigIdToRowData;
  }

  /**
   * the data engine this member wrapper belongs to
   */
  private GrouperDataEngine grouperDataEngine;

  /**
   * the Grouper Member object, populated during indexing from the database
   */
  private Member member;

  /**
   * @return the data engine
   */
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  /**
   * @param grouperDataEngine the data engine
   */
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  /**
   * the member internal ID (primary key from grouper_members)
   */
  private long internalId;

  /**
   * @return the member internal ID
   */
  public long getInternalId() {
    return internalId;
  }

  /**
   * @param internalId the member internal ID
   */
  public void setInternalId(long internalId) {
    this.internalId = internalId;
  }

  /**
   * values from the data provider source for each field, keyed by field internal ID.
   * this is the "source of truth" from the data provider, used during sync to compare
   * against fieldIdToValues (the current Grouper state) to determine what changed.
   */
  private Map<Long, Set<Object>> fieldIdToDataProviderValues = new HashMap<Long, Set<Object>>();

  /**
   * @return data provider values keyed by field internal ID
   */
  public Map<Long, Set<Object>> getFieldIdToDataProviderValues() {
    return fieldIdToDataProviderValues;
  }

  /**
   * current Grouper field assign wrappers indexed by field internal ID then value.
   * used during sync to quickly look up whether a specific field+value combination
   * already exists in Grouper (to avoid re-inserting it).
   */
  private Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = new HashMap<>();

  /**
   * @return field assign wrappers keyed by field internal ID then value
   */
  public Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> getFieldIdToValueToFieldAssignWrapper() {
    return fieldIdToValueToFieldAssignWrapper;
  }

  /**
   * current Grouper values for each field, keyed by field internal ID.
   * this is the "current state" side, populated during indexing from existing
   * grouper_data_field_assign rows. compared against fieldIdToDataProviderValues
   * during sync to compute set differences (inserts = source - grouper, deletes = grouper - source).
   */
  private Map<Long, Set<Object>> fieldIdToValues = new HashMap<>();

  /**
   * @return current Grouper values keyed by field internal ID
   */
  public Map<Long, Set<Object>> getFieldIdToValues() {
    return fieldIdToValues;
  }

  /**
   * current Grouper row assign wrappers for this member, keyed by row internal ID.
   * each row internal ID maps to a list of row assign wrappers (a member can have
   * multiple row assigns for the same row type, e.g. multiple phone numbers).
   */
  private Map<Long, List<GrouperDataRowAssignWrapper>> rowAssignWrappersByRowInternalId = new HashMap<>();

  /**
   * current Grouper field assign wrappers for this member, keyed by field internal ID.
   * each field internal ID maps to a list of field assign wrappers (multi-valued fields
   * can have multiple assigns for the same field).
   */
  private Map<Long, List<GrouperDataFieldAssignWrapper>> fieldAssignWrappersByFieldInternalId = new HashMap<>();

  /**
   * @return row assign wrappers keyed by row internal ID
   */
  public Map<Long, List<GrouperDataRowAssignWrapper>> getRowAssignWrappersByRowInternalId() {
    return rowAssignWrappersByRowInternalId;
  }

  /**
   * @return field assign wrappers keyed by field internal ID
   */
  public Map<Long, List<GrouperDataFieldAssignWrapper>> getFieldAssignWrappersByFieldInternalId() {
    return fieldAssignWrappersByFieldInternalId;
  }

  /**
   * @return the Grouper Member object
   */
  public Member getMember() {
    return member;
  }

  /**
   * @param member the Grouper Member object
   */
  public void setMember(Member member) {
    this.member = member;
  }

}
