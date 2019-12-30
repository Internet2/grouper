/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.List;


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
   * grouping unique vals from source
   */
  private List<Object> groupingUniqueValues;

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

}
