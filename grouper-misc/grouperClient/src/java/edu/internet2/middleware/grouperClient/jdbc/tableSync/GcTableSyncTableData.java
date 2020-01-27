/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.bcel.internal.classfile.Code;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * data from a table
 */
public class GcTableSyncTableData {

  /**
   * take the data and find the max incremental progress value
   * @param progressColumn
   * @return the max value
   */
  public Object maxProgressValue(GcTableSyncColumnMetadata progressColumnMetadata) {
    
    if (GrouperClientUtils.length(this.rows) == 0) {
      return null;
    }
    
    Object maxIncrementalAllColumnsValue = null;
    
    for (GcTableSyncRowData gcTableSyncRowData : this.rows) {
      Object currentIncrementalAllColumnsValue = gcTableSyncRowData.incrementalProgressValue(progressColumnMetadata);
      if (maxIncrementalAllColumnsValue == null || ((Comparable)currentIncrementalAllColumnsValue).compareTo(maxIncrementalAllColumnsValue) > 0) {
        maxIncrementalAllColumnsValue = currentIncrementalAllColumnsValue;
      }
    }
    return maxIncrementalAllColumnsValue;
  }
  
  /**
   * column metadata (might be a subset of all columns)
   */
  private List<GcTableSyncColumnMetadata> columnMetadata;
  
  /**
   * column metadata (might be a subset of all columns)
   * @return columns
   */
  public List<GcTableSyncColumnMetadata> getColumnMetadata() {
    return this.columnMetadata;
  }

  /**
   * column metadata (might be a subset of all columns)
   * @param columnMetadata1
   */
  public void setColumnMetadata(List<GcTableSyncColumnMetadata> columnMetadata1) {
    this.columnMetadata = columnMetadata1;
  }

  /**
   * construct
   * @param gcTableSyncTableBean1
   * @param data
   */
  public void init(GcTableSyncTableBean gcTableSyncTableBean1, List<GcTableSyncColumnMetadata> columnMetadata1, List<Object[]> data) {
    this.gcTableSyncTableBean = gcTableSyncTableBean1;
    this.columnMetadata = columnMetadata1;
    this.indexByPrimaryKey = null;
    this.rows = new ArrayList<GcTableSyncRowData>();
    
    for (Object[] row : GrouperClientUtils.nonNull(data)) {
      
      GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
      gcTableSyncRowData.setGcTableSyncTableData(this);
      gcTableSyncRowData.setData(row);
      this.rows.add(gcTableSyncRowData);
      
    }
    
  }
  
  /**
   * construct
   * @param gcTableSyncTableBean1
   * @param data
   */
  public void init(GcTableSyncTableBean gcTableSyncTableBean1, List<GcTableSyncColumnMetadata> columnMetadata1, Map<MultiKey, GcTableSyncRowData> data) {
    this.gcTableSyncTableBean = gcTableSyncTableBean1;
    this.columnMetadata = columnMetadata1;
    this.indexByPrimaryKey = null;
    this.rows = new ArrayList<GcTableSyncRowData>();
    this.indexByPrimaryKey = data;
    
    for (MultiKey primaryKey : data.keySet()) {
      GcTableSyncRowData gcTableSyncRowData = data.get(primaryKey);
      this.rows.add(gcTableSyncRowData);
    }
    
  }
  
  /**
   * link back up to table bean
   */
  private GcTableSyncTableBean gcTableSyncTableBean;
  
  /**
   * link back up to table bean
   * @return the gcTableSyncTableBean
   */
  public GcTableSyncTableBean getGcTableSyncTableBean() {
    return this.gcTableSyncTableBean;
  }

  
  /**
   * link back up to table bean
   * @param gcTableSyncTableBean1 the gcTableSyncTableBean to set
   */
  public void setGcTableSyncTableBean(GcTableSyncTableBean gcTableSyncTableBean1) {
    this.gcTableSyncTableBean = gcTableSyncTableBean1;
  }

  /**
   * index the data by primary key
   */
  private Map<MultiKey, GcTableSyncRowData> indexByPrimaryKey = null;

  /**
   * 
   * @return the multikeys
   */
  public Set<MultiKey> allPrimaryKeys() {
    
    if (this.indexByPrimaryKey == null) {
      this.indexData();
    }
    
    return this.indexByPrimaryKey.keySet();
    
  }
  
  /**
   * 
   * @return the multikeys
   */
  public Set<MultiKey> allDataInColumns(List<GcTableSyncColumnMetadata> gcTableSyncColumnMetadatas) {

    Set<MultiKey> results = new LinkedHashSet<MultiKey>();
    
    GcTableSyncColumnMetadata[] gcTableSyncColumnMetadataThises = new GcTableSyncColumnMetadata[GrouperClientUtils.length(gcTableSyncColumnMetadatas)];
    for (int i=0; i<GrouperClientUtils.length(gcTableSyncColumnMetadatas); i++) {
      
      GcTableSyncColumnMetadata gcTableSyncColumnMetadataOther = gcTableSyncColumnMetadatas.get(i);

      GcTableSyncColumnMetadata gcTableSyncColumnMetadataThis = 
          this.getGcTableSyncTableBean().getTableMetadata().lookupColumn(gcTableSyncColumnMetadataOther.getColumnName(), true);
      
      gcTableSyncColumnMetadataThises[i] = gcTableSyncColumnMetadataThis;
      
    }

    for (GcTableSyncRowData row : GrouperClientUtils.nonNull(this.rows)) {
      Object[] values = new Object[GrouperClientUtils.length(gcTableSyncColumnMetadatas)];
      int i=0;
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncColumnMetadataThises) {
        values[i++] = row.getData()[row.lookupColumnToIndexZeroIndexed(gcTableSyncColumnMetadata.getColumnName(), true)];
      }
      MultiKey multiKey = new MultiKey(values);
      results.add(multiKey);
    }

    return results;
    
  }
  
  /**
   * 
   * @return the multikeys
   */
  public Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey() {
    
    if (this.indexByPrimaryKey == null) {
      this.indexData();
    }
    
    return this.indexByPrimaryKey;
    
  }
  
  /**
   * index the data by primary key
   */
  public void indexData() {
    
    this.indexByPrimaryKey = new HashMap<MultiKey, GcTableSyncRowData>();
    
    for (GcTableSyncRowData row : GrouperClientUtils.nonNull(this.rows)) {
      this.indexByPrimaryKey.put(row.getPrimaryKey(), row);
    }
    
  }

  /**
   * if just selecting groups, these are the groupings
   * @return the multikeys
   */
  public Set<Object> allGroupings() {
    
    Set<Object> groupings = new HashSet<Object>();
    
    for (GcTableSyncRowData row : GrouperClientUtils.nonNull(this.rows)) {
      Object[] rowData = row.getData();
      
      if (GrouperClientUtils.length(rowData) > 1) {
        throw new RuntimeException("Expecting 1 column for groupings, but was: " + GrouperClientUtils.length(rowData));
      }
      Object grouping = rowData[0];
      if (grouping == null) {
        throw new RuntimeException("Expecting non null grouping but was null");
      }
      groupings.add(grouping);
    }
    
    return groupings;
    
  }
  

  /**
   * @param primaryKey
   * @return the row
   */
  public GcTableSyncRowData findRowFromPrimaryKey(MultiKey primaryKey) {
    if (this.indexByPrimaryKey == null) {
      this.indexData();
    }
    return this.indexByPrimaryKey.get(primaryKey);
  }
  
  /**
   * 
   */
  public GcTableSyncTableData() {
  }

  /**
   * row data
   */
  private List<GcTableSyncRowData> rows;
  
  /**
   * row data
   * @return the rows
   */
  public List<GcTableSyncRowData> getRows() {
    return this.rows;
  }

  
  /**
   * row data
   * @param rows1 the rows to set
   */
  public void setRows(List<GcTableSyncRowData> rows1) {
    this.rows = rows1;
  }
  
  
}
