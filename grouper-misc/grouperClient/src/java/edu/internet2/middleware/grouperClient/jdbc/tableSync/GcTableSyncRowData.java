/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * data from a row
 */
public class GcTableSyncRowData {

  /**
   * lookup column by name (case insensitive) to column index 
   */
  private Map<String, Integer> columnLookupToIndexZeroIndexed = new HashMap<String, Integer>();

  /**
   * lookup column by name (case insensitive) to column index 
   * @param columnName
   * @return the index
   */
  public Integer lookupColumnToIndexZeroIndexed(String columnName, boolean exceptionIfNotFound) {
    Integer columnIndex = columnLookupToIndexZeroIndexed.get(columnName);
    if (columnIndex == null) {
      
      List<GcTableSyncColumnMetadata> columnMetadata = this.getGcTableSyncTableData().getColumnMetadata();
      for (int i=0;i<columnMetadata.size();i++) {
        if (GrouperClientUtils.equalsIgnoreCase(columnName, columnMetadata.get(i).getColumnName())) {
          columnIndex = i;
          break;
        }
      }
      if (columnIndex == null) {
        if (!exceptionIfNotFound) {
          return null;
        }
        throw new RuntimeException("Cant find column: " + columnName + ", in list: " + GrouperClientUtils.toStringForLog(columnMetadata));
      }
      columnLookupToIndexZeroIndexed.put(columnName, columnIndex);
    }
    return columnIndex;
  }
  
  /**
   * referenec to the gc table
   */
  private GcTableSyncTableData gcTableSyncTableData;
  
  /**
   * referenec to the gc table
   * @return the gcTableSyncTableData
   */
  public GcTableSyncTableData getGcTableSyncTableData() {
    return this.gcTableSyncTableData;
  }
  
  /**
   * referenec to the gc table
   * @param gcTableSyncTableData1 the gcTableSyncTableData to set
   */
  public void setGcTableSyncTableData(GcTableSyncTableData gcTableSyncTableData1) {
    this.gcTableSyncTableData = gcTableSyncTableData1;
  }

  /**
   * 
   */
  public GcTableSyncRowData() {
  }

  /**
   * column data
   */
  private Object[] data;

  /**
   * multikey of the data
   */
  private MultiKey multiKey;
  
  public MultiKey multiKey() {
    if (this.data == null) {
      throw new RuntimeException("Why is data null????");
    }
    if (this.multiKey == null) {
      this.multiKey = new MultiKey(this.data);
    }
    return this.multiKey;
  }
  
  /**
   * column data
   * @return the data
   */
  public Object[] getData() {
    return this.data;
  }
  
  /**
   * column data
   * @param data1 the data to set
   */
  public void setData(Object[] data1) {
    this.data = data1;
  }
  
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    
    if (!(obj instanceof GcTableSyncRowData)) {
      return false;
    }
    
    GcTableSyncRowData other = (GcTableSyncRowData)obj;
    
    // check to make sure the arrays are similar
    if (this.data == other.data) {
      return true;
    }
    
    if (this.data == null || other.data == null) {
      return false;
    }
    
    if (this.data.length != other.data.length) {
      return false;
    }
    
    // go through each data element
    for (int i=0;i<this.data.length;i++) {
      Object thisObject = this.data[i];
      Object otherObject = other.data[i];
      if (thisObject == otherObject) {
        continue;
      }
      if (thisObject == null || otherObject == null) {
        return false;
      }
      if (!thisObject.equals(otherObject)) {
        return false;
      }
    }
    
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    hashCodeBuilder.append(this.data);
    return hashCodeBuilder.toHashCode();
  }

  /**
   * get the primary key
   * @return the primary key
   */
  public MultiKey getPrimaryKey() {
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.gcTableSyncTableData.getGcTableSyncTableBean().getTableMetadata().getPrimaryKey();
    Object[] primaryKeyValues = new Object[primaryKeyMetadata.size()];
    int i=0;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : primaryKeyMetadata) {
      primaryKeyValues[i++] = this.data[lookupColumnToIndexZeroIndexed(gcTableSyncColumnMetadata.getColumnName(), true)];
    }
    MultiKey multiKey = new MultiKey(primaryKeyValues);
    return multiKey;
  }

  /**
   * get the incremental progress value column
   * @param incrementalProgressValueMetadata
   * @return the value
   */
  public Object incrementalProgressValue(GcTableSyncColumnMetadata incrementalProgressValueMetadata) {
    
    if (incrementalProgressValueMetadata == null) {
      throw new RuntimeException("Incremental progress column is required!");
    }
    
    return this.data[lookupColumnToIndexZeroIndexed(incrementalProgressValueMetadata.getColumnName(), true)];
    
  }
  
  /**
   * get the incremental progress value column
   * @param incrementalProgressValueMetadata
   * @return the value
   */
  public Object incrementalProgressValue() {
    return incrementalProgressValue(this.getGcTableSyncTableData().getGcTableSyncTableBean().getTableMetadata().getIncrementalProgressColumn());
  }
  
  /**
   * get the non primary key
   * @return the non primary key
   */
  public MultiKey getNonPrimaryKey() {
    
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = nonPrimaryKeyColumns();
    Object[] nonPrimaryKeyValues = new Object[nonPrimaryKeyMetadata.size()];
    int i=0;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : nonPrimaryKeyMetadata) {
      nonPrimaryKeyValues[i++] = this.data[lookupColumnToIndexZeroIndexed(gcTableSyncColumnMetadata.getColumnName(), true)];
    }
    MultiKey multiKey = new MultiKey(nonPrimaryKeyValues);
    return multiKey;
  }

  /**
   * non primary key metadata
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata;
  
  /**
   * non primary key metadata
   * @return the metadata
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKeyColumns() {
    
    if (this.nonPrimaryKeyMetadata == null) {
      // start with primary keys
      List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.gcTableSyncTableData.getGcTableSyncTableBean().getTableMetadata().getPrimaryKey();
  
      // index them
      Set<String> primaryColsLower = new HashSet<String>();
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : primaryKeyMetadata) {
        primaryColsLower.add(gcTableSyncColumnMetadata.getColumnName().toLowerCase());
      }
      this.nonPrimaryKeyMetadata = new ArrayList<GcTableSyncColumnMetadata>();
  
      // go through cols retrieved
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.gcTableSyncTableData.getColumnMetadata()) {
        
        // if primary key then skip
        if (primaryColsLower.contains(gcTableSyncColumnMetadata.getColumnName().toLowerCase())) {
          continue;
        }
        
        this.nonPrimaryKeyMetadata.add(gcTableSyncColumnMetadata);
      }
  
      if (this.nonPrimaryKeyMetadata.size() == 0 ) {
        throw new RuntimeException("there is no non primary key: " + this.getGcTableSyncTableData().getGcTableSyncTableBean().getTableMetadata().getTableName());
      }
    }
    return nonPrimaryKeyMetadata;
  }

  /**
   * @return the data
   */
  public List<Object> nonPrimaryAndThenPrimaryKeyData() {
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = this.nonPrimaryKeyColumns();
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.gcTableSyncTableData.getGcTableSyncTableBean().getTableMetadata().getPrimaryKey();
    List<Object> values = new ArrayList<Object>();
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : nonPrimaryKeyMetadata) {
      values.add(this.data[lookupColumnToIndexZeroIndexed(gcTableSyncColumnMetadata.getColumnName(), true)]);
    }
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : primaryKeyMetadata) {
      values.add(this.data[lookupColumnToIndexZeroIndexed(gcTableSyncColumnMetadata.getColumnName(), true)]);
    }
    return values;
    
  }

  /**
   * @return all columns data
   */
  public List<Object> allColumnsData() {
    List<GcTableSyncColumnMetadata> columnMetadatas = this.gcTableSyncTableData.getColumnMetadata();
    List<Object> values = new ArrayList<Object>();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas) {
      values.add(this.data[lookupColumnToIndexZeroIndexed(columnMetadata.getColumnName(), true)]);
    }
    return values;
  }
}
