/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 *
 */
public class GcTableSyncTableBean {

  
  /**
   * referenec to the gc table sync
   */
  private GcTableSync gcTableSync;

  /**
   * @return the gcTableSync
   */
  public GcTableSync getGcTableSync() {
    return this.gcTableSync;
  }
  
  /**
   * @param gcTableSync1 the gcTableSync to set
   */
  public void setGcTableSync(GcTableSync gcTableSync1) {
    this.gcTableSync = gcTableSync1;
  }

  /**
   * if grouping this is the grouping column
   */
  private GcTableSyncColumnMetadata groupingColumn;

  /**
   * 
   * @param groupingColumnName
   */
  public void assignGroupingColumn(String groupingColumnName) {
    if (this.tableMetadata == null) {
      throw new RuntimeException("There is not table metadata yet!");
    }
    
    this.groupingColumn = this.tableMetadata.lookupColumn(groupingColumnName);
  }
  
  /**
   * @param theColumns could be * or list of columns
   */
  public void assignColumns(String theColumns) {
    if (this.tableMetadata == null) {
      throw new RuntimeException("There is not table metadata yet!");
    }
    this.columns = this.tableMetadata.lookupColumns(theColumns);
  }
  
  /**
   * @param theColumns could be * or list of columns
   */
  public void assignPrimaryKeyColumns(String theColumns) {
    if (this.tableMetadata == null) {
      throw new RuntimeException("There is not table metadata yet!");
    }
    this.primaryKeyColumns = this.tableMetadata.lookupColumns(theColumns);
  }
  
  /**
   * 
   * @param sqlBuilder
   * @param gcTableSyncColumnMetadataList 
   */
  public static void appendColumns(StringBuilder sqlBuilder, List<GcTableSyncColumnMetadata> gcTableSyncColumnMetadataList) {
    boolean isFirst = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncColumnMetadataList) {
      
      if (!isFirst) {
        sqlBuilder.append(", ");
      }
      sqlBuilder.append(gcTableSyncColumnMetadata.getColumnName());
      isFirst = false;
    }
  }
  
  /**
   * 
   * @param sqlBuilder
   */
  public void appendQuestionsAllCols(StringBuilder sqlBuilder, List<GcTableSyncColumnMetadata> gcTableSyncColumnMetadatas) {
    boolean isFirst = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncColumnMetadatas) {
      
      if (!isFirst) {
        sqlBuilder.append(", ");
      }
      sqlBuilder.append("?");
      isFirst = false;
    }
  }


  /**
   * 
   */
  public GcTableSyncTableBean() {
  }

  /**
   * 
   */
  private GcTableSyncTableMetadata tableMetadata;
  
  
  /**
   * @return the tableMetadata
   */
  public GcTableSyncTableMetadata getTableMetadata() {
    return this.tableMetadata;
  }


  
  /**
   * @param tableMetadata1 the tableMetadata to set
   */
  public void setTableMetadata(GcTableSyncTableMetadata tableMetadata1) {
    this.tableMetadata = tableMetadata1;
  }


  /**
   * data in the table e.g. for grouping or whatever
   */
  private GcTableSyncTableData dataPartialColumns;

  /**
   * data in the table
   */
  private GcTableSyncTableData dataAllNeededColumns;

  
  /**
   * mtadata for columns synced
   * @return the columns
   */
  public List<GcTableSyncColumnMetadata> getColumns() {
    return this.columns;
  }

  
  /**
   * mtadata for columns synced
   * @param columns1 the columns to set
   */
  public void setColumns(List<GcTableSyncColumnMetadata> columns1) {
    this.columns = columns1;
  }

  /**
   * grouping unique vals from source
   */
  private List<Object> groupingUniqueValues;

  /**
   * non primary key col(s), which is sync'ed columns with primary key removed
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKey;

  /**
   * "primary key" col(s), as assigned by configuration
   */
  private List<GcTableSyncColumnMetadata> primaryKeyColumns;

  /**
   * all columns to sync that we care about
   */
  private List<GcTableSyncColumnMetadata> columns;

  
  /**
   * data in the table
   * @return the data
   */
  public GcTableSyncTableData getDataAllNeededColumns() {
    return this.dataAllNeededColumns;
  }

  
  /**
   * data in the table
   * @param data1 the data to set
   */
  public void setDataAllNeededColumns(GcTableSyncTableData data1) {
    this.dataAllNeededColumns = data1;
  }

  /**
   * grouping unique vals from source
   * @return the fromGroupingUniqueValues
   */
  public List<Object> getGroupingUniqueValues() {
    return this.groupingUniqueValues;
  }

  /**
   * grouping unique vals from source
   * @param fromGroupingUniqueValues1 the fromGroupingUniqueValues to set
   */
  public void setGroupingUniqueValues(List<Object> fromGroupingUniqueValues1) {
    this.groupingUniqueValues = fromGroupingUniqueValues1;
  }


  /**
   * get grouping column metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getGroupingColumnMetadata() {
    return this.getGroupingColumnMetadata();
  }

  /**
   * non primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getNonPrimaryKey() {
    if (this.nonPrimaryKey == null) {
      
      List<GcTableSyncColumnMetadata> result = new ArrayList();
      result.addAll(this.getColumns());
      result.removeAll(this.getPrimaryKey());
      
    }
    
    return this.nonPrimaryKey;
  }

  /**
   * primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getPrimaryKey() {
    return this.primaryKeyColumns;
  }

}
