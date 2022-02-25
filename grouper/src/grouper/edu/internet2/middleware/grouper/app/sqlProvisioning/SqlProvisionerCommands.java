package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisionerCommands {

  
  /**
   * 
   * @param dbExternalSystemConfigId 
   * @param columnList
   * @param tableName
   * @param mainTableIdColumnName 
   * @param attributeTableName 
   * @param attributeForeignKeyColumnName 
   * @param attributeTableAttributeNameColumn 
   * @param attributeTableAttributeValueColumn 
   * @param attributeNameFilter 
   * @param attributeValuesFilter 
   * @return the data
   */
  public static List<Object[]> retrieveObjectsAttributeFilter(String dbExternalSystemConfigId, List<String> columnList, String tableName, 
      String mainTableIdColumnName, String attributeTableName,  String attributeForeignKeyColumnName,
      String attributeTableAttributeNameColumn, String attributeTableAttributeValueColumn,
      String attributeNameFilter, List<?> attributeValuesFilter) {

    StringBuilder sqlInitial = new StringBuilder("select " + GrouperUtil.join(columnList.iterator(), ", ") + " from " + tableName);

    List<Object[]> overallResults = new ArrayList<Object[]>();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeValuesFilter, 900);

    for (int i = 0; i < numberOfBatches; i++) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      List<?> currentBatchFilterValues = GrouperUtil.batchList(attributeValuesFilter, 900, i);
      StringBuilder sql = new StringBuilder(sqlInitial);
      
      boolean first = true;

      sql.append(" where exists (select 1 from " + attributeTableName 
          + " where " + attributeTableName + "." + attributeForeignKeyColumnName + " = " + tableName + "." + mainTableIdColumnName
          + " and " + attributeTableName + "." + attributeTableAttributeNameColumn + " = ? " + " and ");
      gcDbAccess.addBindVar(attributeNameFilter);
      sql.append(" ( ");
      for (Object filterValue : GrouperUtil.nonNull(currentBatchFilterValues)) {
        if (!first) {
          sql.append(" or ");
        }
        sql.append( attributeTableName + "." + attributeTableAttributeValueColumn);
        if (GrouperUtil.isBlank(filterValue)) {
          sql.append(" is null");
        } else {
          sql.append(" = ?");
          gcDbAccess.addBindVar(filterValue);
        }
        first = false;
      }
      sql.append(" ) ");
      sql.append(" ) ");

      List<Object[]> results = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
      overallResults.addAll(GrouperUtil.nonNull(results));
    }
    return overallResults;
  }
  
  /**
   * 
   * @param dbExternalSystemConfigId 
   * @param columnList
   * @param tableName
   * @param filterColumns0small optional filter with number of values less than 100
   * @param filterValuesByColumn0small optional filter with number of values less than 100.  If there is more than one column to filter, put each item in an object[]
   * @param filterColumns1large optional filter with values any size
   * @param filterValuesByColumn1large optional filter with values any size.  If there is more than one column to filter, put each item in an object[]
   * @return the data
   */
  public static List<Object[]> retrieveObjectsColumnFilter(String dbExternalSystemConfigId, List<String> columnList, String tableName,
      List<String> filterColumns0small, List<Object> filterValuesByColumn0small, List<String> filterColumns1large, List<Object> filterValuesByColumn1large) {

    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);

    StringBuilder sqlInitial = new StringBuilder("select " + GrouperUtil.join(columnList.iterator(), ", ") + " from " + tableName);

    List<Object[]> overallResults = new ArrayList<Object[]>();

    GrouperUtil.assertion(GrouperUtil.length(filterColumns0small) < 100, "First filter attributes needs to be small, e.g. which attribute names to retrieve");

    boolean hasfilter0 = GrouperUtil.length(filterColumns0small) > 0;
    
    if (hasfilter0 && GrouperUtil.length(filterColumns0small) == 0) {
      return overallResults;
    }
    
    boolean hasfilter1 = GrouperUtil.length(filterColumns1large) > 0;

    if (hasfilter1 && GrouperUtil.length(filterColumns1large) == 0) {
      return overallResults;
    }
    
    int batchSize = 900;
    if (hasfilter1 && GrouperUtil.length(filterColumns1large) > 1) {
      batchSize = 900 / ((Object[])filterValuesByColumn1large.get(0)).length;
    }
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(filterValuesByColumn1large, batchSize);

    for (int i = 0; i < numberOfBatches; i++) {
      
      List<Object> currentBatchFilterValues1large = GrouperUtil.batchList(filterValuesByColumn1large, 900, i);
      StringBuilder sql = new StringBuilder(sqlInitial);
      
      boolean first = true;
      sql.append(" where  ");
      
      if (hasfilter0) {
        sql.append(" ( ");
        for (Object filterValuesObject : GrouperUtil.nonNull(filterValuesByColumn0small)) {
          if (!first) {
            sql.append(" or ");
          }
          
          Object[] filterValues = (filterValuesObject instanceof Object[]) ? (Object[]) filterValuesObject : new Object[] {filterValuesObject};
          
          sql.append(" ( ");
          
          GrouperUtil.assertion(GrouperUtil.length(filterColumns0small) == GrouperUtil.length(filterValues), 
              "Size of filter columns " + GrouperUtil.length(filterColumns0small) + " must equal filter values " + GrouperUtil.length(filterValues));
  
          for (int j=0;j<filterColumns0small.size();j++) {
            if (j>0) {
              sql.append(" and ");
            }
            GrouperUtil.assertion(!StringUtils.isBlank(filterColumns0small.get(j)), "Column is missing! " + j);
            sql.append(filterColumns0small.get(j));
            if (GrouperUtil.isBlank(filterValues[j])) {
              sql.append(" is null");
            } else {
              sql.append(" = ? ");
              gcDbAccess.addBindVar(filterValues[j]);
            }
            
          }
          sql.append(" ) ");
          first = false;
        }
        sql.append(" ) ");
        if (hasfilter1) {
          sql.append(" and ");
        }
      }
      if (hasfilter1) {
        first = true;
        
        sql.append(" ( ");
        for (Object filterValuesObject : GrouperUtil.nonNull(currentBatchFilterValues1large)) {
          if (!first) {
            sql.append(" or ");
          }
          
          Object[] filterValues = (filterValuesObject instanceof Object[]) ? (Object[]) filterValuesObject : new Object[] {filterValuesObject};

          sql.append(" ( ");
          
          GrouperUtil.assertion(GrouperUtil.length(filterColumns1large) == GrouperUtil.length(filterValues), 
              "Size of filter columns " + GrouperUtil.length(filterColumns1large) + " must equal filter values " + GrouperUtil.length(filterValues));
  
          for (int j=0;j<filterColumns1large.size();j++) {
            if (j>0) {
              sql.append(" and ");
            }
            GrouperUtil.assertion(!StringUtils.isBlank(filterColumns1large.get(j)), "Column is missing! " + j);
            sql.append(filterColumns1large.get(j));
            if (GrouperUtil.isBlank(filterValues[j])) {
              sql.append(" is null");
            } else {
              sql.append(" = ? ");
              gcDbAccess.addBindVar(filterValues[j]);
            }
            
          }
          sql.append(" ) ");
          first = false;
        }
      }        
      sql.append(" ) ");

      List<Object[]> results = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
      overallResults.addAll(GrouperUtil.nonNull(results));
    }
    return overallResults;
  }

  /**
   * 
   * @param dbExternalSystemConfigId 
   * @param columnList
   * @param tableName
   * @return the data
   */
  public static List<Object[]> retrieveObjectsNoFilter(String dbExternalSystemConfigId, List<String> columnList, String tableName) {

    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);

    StringBuilder sql = new StringBuilder("select " + GrouperUtil.join(columnList.iterator(), ", ") + " from " + tableName);

    List<Object[]> overallResults = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
    return overallResults;
  }

  /**
   * this will delete all attributes for owner and the owner itself
   * @param dataToDelete
   * @param dbExternalSystemConfigId
   * @param ownerTableName
   * @param columnNames
   * @param ownerAttributesTableName
   * @param attributesOwnerForeignKeyColumn
   */
  public static void deleteObjects(List<Object[]> dataToDelete, 
      String dbExternalSystemConfigId, String ownerTableName, List<String> columnNames, String ownerAttributesTableName,
      String attributesOwnerForeignKeyColumn) {

    if (GrouperUtil.length(dataToDelete) == 0) { 
      return;
    }

    if (!StringUtils.isBlank(ownerAttributesTableName)) {
      deleteObjects(dataToDelete, dbExternalSystemConfigId, ownerAttributesTableName, GrouperUtil.toList(attributesOwnerForeignKeyColumn), null, null);
    }    

    List<Object[]> rowsLastValueNotNull = new ArrayList<Object[]>();
    List<Object[]> rowsLastValueNull = new ArrayList<Object[]>();
  
    for (Object[] row : dataToDelete) {
      if (row[row.length-1] == null) {
        rowsLastValueNull.add(row);
      } else {
        rowsLastValueNotNull.add(row);
      }
    }
    
    if (GrouperUtil.length(rowsLastValueNotNull) > 0) {
      deleteObjectsLastValueNotNull(rowsLastValueNotNull, dbExternalSystemConfigId, ownerTableName,
          columnNames);
    }
    if (GrouperUtil.length(rowsLastValueNull) > 0) {
      deleteObjectsLastValueNull(rowsLastValueNull, dbExternalSystemConfigId, ownerTableName,
          columnNames);
    }

  }

  /**
   * this will delete all attributes for owner and the owner itself
   * @param dataToDelete
   * @param dbExternalSystemConfigId
   * @param ownerTableName
   * @param columnNames
   */
  public static void deleteObjectsLastValueNull(List<Object[]> dataToDelete, 
      String dbExternalSystemConfigId, String ownerTableName, List<String> columnNames) {
    
    if (GrouperUtil.length(dataToDelete) == 0) { 
      return;
    }

    StringBuilder sql = new StringBuilder("delete from  " + ownerTableName + " where ");

    // loop through columns
    for (int i=0;i<columnNames.size();i++) {
      
      if (i>0) {
        sql.append(" and ");
      }
      sql.append(columnNames.get(i));
      if (i != columnNames.size()-1) {
        sql.append(" = ? ");
      } else {
        sql.append(" is null ");
      }

    }
    
    int batchSize = 900 / dataToDelete.get(0).length;
    List<List<Object>> batchBindVarsForTable = new ArrayList<List<Object>>();
    for (int i=0;i<dataToDelete.size();i++) {

      Object[] currentRow = dataToDelete.get(i);
      List<Object> bindVars = new ArrayList<Object>();
      batchBindVarsForTable.add(bindVars);

      for (int j=0;j<currentRow.length;j++) {
        if (j != currentRow.length-1) {
          GrouperUtil.assertion(currentRow[j] != null, "value should not be null: " + GrouperUtil.toStringForLog(currentRow));
          bindVars.add(currentRow[j]);
        } else {
          GrouperUtil.assertion(currentRow[j] == null, "value should be null: " + GrouperUtil.toStringForLog(currentRow));
        }
      }
    }
    
    new GcDbAccess().connectionName(dbExternalSystemConfigId).batchSize(batchSize)
        .sql(sql.toString()).batchBindVars(batchBindVarsForTable).executeBatchSql();
    
  }

  /**
   * this will delete all attributes for owner and the owner itself
   * @param dataToDelete
   * @param dbExternalSystemConfigId
   * @param ownerTableName
   * @param columnNames
   */
  private static void deleteObjectsLastValueNotNull(List<Object[]> dataToDelete, 
      String dbExternalSystemConfigId, String ownerTableName, List<String> columnNames) {
    
    if (GrouperUtil.length(dataToDelete) == 0) { 
      return;
    }

    StringBuilder sql = new StringBuilder("delete from  " + ownerTableName + " where ");

    // loop through columns
    for (int i=0;i<columnNames.size();i++) {
      
      if (i>0) {
        sql.append(" and ");
      }
      sql.append(columnNames.get(i));
      sql.append(" = ? ");

    }
    
    int batchSize = 900 / dataToDelete.get(0).length;
    List<List<Object>> batchBindVarsForTable = new ArrayList<List<Object>>();
    for (int i=0;i<dataToDelete.size();i++) {

      Object[] currentRow = dataToDelete.get(i);
      List<Object> bindVars = new ArrayList<Object>();
      batchBindVarsForTable.add(bindVars);

      for (int j=0;j<currentRow.length;j++) {
        GrouperUtil.assertion(currentRow[j] != null, "value should not be null: " + GrouperUtil.toStringForLog(currentRow));
        bindVars.add(currentRow[j]);
      }
    }
    
    new GcDbAccess().connectionName(dbExternalSystemConfigId).batchSize(batchSize)
        .sql(sql.toString()).batchBindVars(batchBindVarsForTable).executeBatchSql();
    
  }
 

  /**
   * @param dbExternalSystemConfigId 
   * @param tableName 
   * @param columnsToUpdate 
   * @param valuesToUpdate 
   * @param whereClauseColumns 
   * @param whereClauseValues 
   */
  public static void updateObjects(String dbExternalSystemConfigId, String tableName, 
      List<String> columnsToUpdate, 
      List<Object[]> valuesToUpdate, List<String> whereClauseColumns, List<Object[]> whereClauseValues ) {
    
    if (GrouperUtil.length(whereClauseValues) == 0) {
      return;
    }
    
    List<Object[]> groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNotNull = new ArrayList<Object[]>();
    List<Object[]> groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNull = new ArrayList<Object[]>();
  
    for (Object[] whereClauseRow : whereClauseValues) {
      if (whereClauseRow[whereClauseRow.length-1] == null) {
        groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNull.add(whereClauseRow);
      } else {
        groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNotNull.add(whereClauseRow);
      }
    }
    
    if (GrouperUtil.length(groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNotNull) > 0) {
      updateObjectsOldValueNotNull(dbExternalSystemConfigId, tableName, columnsToUpdate, valuesToUpdate, whereClauseColumns, groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNotNull);
    }
    if (GrouperUtil.length(groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNull) > 0) {
      updateObjectsOldValueNull(dbExternalSystemConfigId, tableName, columnsToUpdate, valuesToUpdate, whereClauseColumns, groupIdAttributeNameAttributeOldValueAttributeNewValuesOldValueNull);
    }
  }

  /**
   * @param dbExternalSystemConfigId
   * @param ownerTableName
   * @param columnsToInsertInPrimaryTable 
   * @param attributeValuesPrimaryTable 
   */
  public static void insertObjects(String dbExternalSystemConfigId, String ownerTableName, List<String> columnsToInsertInPrimaryTable, List<Object[]> attributeValuesPrimaryTable) {

    if (GrouperUtil.length(attributeValuesPrimaryTable) == 0) {
      return;
    }
    
    String commaSeparatedColNamesPrimaryTable = StringUtils.join(columnsToInsertInPrimaryTable, ",");
    
    String commaSeparatedQuestionMarksPrimaryTable = GrouperClientUtils.appendQuestions(columnsToInsertInPrimaryTable.size());
    
    List<List<Object>> batchBindVarsForPrimaryTable = new ArrayList<List<Object>>();

    String sqlForPrimaryTable = "insert into " + ownerTableName + "(" + commaSeparatedColNamesPrimaryTable + ") values ("+commaSeparatedQuestionMarksPrimaryTable+")";

    for (Object[] columnData : attributeValuesPrimaryTable) {

      List<Object> bindVarsForPrimaryTable = new ArrayList<Object>();
      batchBindVarsForPrimaryTable.add(bindVarsForPrimaryTable);
      
      GrouperUtil.assertion(columnData.length == columnsToInsertInPrimaryTable.size(), "Columns to insert " + columnsToInsertInPrimaryTable.size() + " does not equal " + columnData.length + ", " + sqlForPrimaryTable + ", " + GrouperUtil.toStringForLog(columnsToInsertInPrimaryTable));
      
      for (int i=0;i<columnData.length;i++) {
        bindVarsForPrimaryTable.add(columnData[i]);
      }

    }
    
    // assume that worked
    new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForPrimaryTable.toString()).batchBindVars(batchBindVarsForPrimaryTable).executeBatchSql();
    
  }

  /**
   * 
   * @param dbExternalSystemConfigId 
   * @param tableName 
   * @param columnsToUpdate 
   * @param valuesToUpdate 
   * @param whereClauseColumns 
   * @param whereClauseValues 
   */
  private static void updateObjectsOldValueNotNull(String dbExternalSystemConfigId, String tableName, 
      List<String> columnsToUpdate, 
      List<Object[]> valuesToUpdate, List<String> whereClauseColumns, List<Object[]> whereClauseValues) {

    if (GrouperUtil.length(valuesToUpdate) == 0) {
      return;
    }

    GrouperUtil.assertion(GrouperUtil.length(valuesToUpdate) == GrouperUtil.length(whereClauseValues), 
        "Number in update batch doesnt match where clause batch size");
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    for (int i=0; i<GrouperUtil.length(valuesToUpdate); i++) {

      Object[] valuesToUpdateRow = valuesToUpdate.get(i);
      Object[] whereClauseRow = whereClauseValues.get(i);
      
      List<Object> bindUpdateVars = new ArrayList<Object>();
      batchBindVars.add(bindUpdateVars);
      
      for (Object valueToUpdate : valuesToUpdateRow) {
        bindUpdateVars.add(valueToUpdate);
      }
      for (int j=0;j<GrouperUtil.length(whereClauseRow);j++) {
        Object whereClauseValue = whereClauseRow[j];
        
        bindUpdateVars.add(whereClauseValue);

        // last one is not null
        if (j == whereClauseRow.length-1) {
          GrouperUtil.assertion(whereClauseValue != null, "last where clause value should not be null");
        }
      }
      
    }
      
    StringBuilder sql = new StringBuilder("update "+tableName + " set ");
    
    for (int i=0;i<columnsToUpdate.size();i++) {
      if (i!=0) {
        sql.append(", ");
      }
      sql.append(columnsToUpdate.get(i)).append(" = ? ");
    }
    
    sql.append(" where ");
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    for (int i=0;i<whereClauseColumns.size();i++) {
      if (i!=0) {
        sql.append(" and ");
      }
      sql.append(whereClauseColumns.get(i));
      
      if (i == whereClauseColumns.size()-1) {
        GrouperUtil.assertion(whereClauseValues.get(i) != null, "Last param should not be null");
      }
      sql.append(" = ? ");
      
    }
    
    int batchSize = 900/(batchBindVars.get(0).size());
    gcDbAccess.sql(sql.toString()).batchSize(batchSize).batchBindVars(batchBindVars).executeBatchSql();
    
  }

  /**
   * 
   * @param dbExternalSystemConfigId 
   * @param tableName 
   * @param columnsToUpdate 
   * @param valuesToUpdate 
   * @param whereClauseColumns 
   * @param whereClauseValues 
   */
  private static void updateObjectsOldValueNull(String dbExternalSystemConfigId, String tableName, 
      List<String> columnsToUpdate, 
      List<Object[]> valuesToUpdate, List<String> whereClauseColumns, List<Object[]> whereClauseValues) {
  
    if (GrouperUtil.length(valuesToUpdate) == 0) {
      return;
    }
  
    GrouperUtil.assertion(GrouperUtil.length(valuesToUpdate) == GrouperUtil.length(whereClauseValues), 
        "Number in update batch doesnt match where clause batch size");
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    for (int i=0; i<GrouperUtil.length(valuesToUpdate); i++) {
  
      Object[] valuesToUpdateRow = valuesToUpdate.get(i);
      Object[] whereClauseRow = whereClauseValues.get(i);
      
      List<Object> bindUpdateVars = new ArrayList<Object>();
      batchBindVars.add(bindUpdateVars);
      
      for (Object valueToUpdate : valuesToUpdateRow) {
        bindUpdateVars.add(valueToUpdate);
      }
      for (int j=0;j<GrouperUtil.length(whereClauseRow);j++) {
        Object whereClauseValue = whereClauseRow[j];
        
        // last one is null
        if (j != whereClauseRow.length-1) {
          bindUpdateVars.add(whereClauseValue);
        } else {
          GrouperUtil.assertion(whereClauseValue == null, "last where clause value should be null");
        }
      }
      
    }
      
    StringBuilder sql = new StringBuilder("update "+tableName + " set ");
    
    for (int i=0;i<columnsToUpdate.size();i++) {
      if (i!=0) {
        sql.append(", ");
      }
      sql.append(columnsToUpdate.get(i)).append(" = ? ");
    }
    
    sql.append(" where ");
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    for (int i=0;i<whereClauseColumns.size();i++) {
      if (i!=0) {
        sql.append(" and ");
      }
      
      sql.append(whereClauseColumns.get(i));

      if (i == whereClauseColumns.size()-1) {
        GrouperUtil.assertion(whereClauseValues.get(i) == null, "Last param should be null");
        sql.append(" is null ");
      } else {
        GrouperUtil.assertion(whereClauseValues.get(i) != null, "Last param should not be null");
        sql.append(" = ? ");
      }
      
    }
    
    
    gcDbAccess.sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();
    
  }

}
