/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * data from a row
 */
public class GcTableSyncRowData {

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
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.gcTableSyncTableData.getGcTableSync().getTableMetadata().getPrimaryKey();
    Object[] primaryKeyValues = new Object[primaryKeyMetadata.size()];
    int i=0;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : primaryKeyMetadata) {
      primaryKeyValues[i++] = this.data[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()];
    }
    MultiKey multiKey = new MultiKey(primaryKeyValues);
    return multiKey;
  }
  
  /**
   * get the non primary key
   * @return the non primary key
   */
  public MultiKey getNonPrimaryKey() {
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = this.gcTableSyncTableData.getGcTableSync().getTableMetadata().getNonPrimaryKey();
    Object[] nonPrimaryKeyValues = new Object[nonPrimaryKeyMetadata.size()];
    int i=0;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : nonPrimaryKeyMetadata) {
      nonPrimaryKeyValues[i++] = this.data[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()];
    }
    MultiKey multiKey = new MultiKey(nonPrimaryKeyValues);
    return multiKey;
  }

  /**
   * @return the data
   */
  public List<Object> nonPrimaryAndThenPrimaryKeyData() {
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = this.gcTableSyncTableData.getGcTableSync().getTableMetadata().getNonPrimaryKey();
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.gcTableSyncTableData.getGcTableSync().getTableMetadata().getPrimaryKey();
    List<Object> values = new ArrayList<Object>();
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : nonPrimaryKeyMetadata) {
      values.add(this.data[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()]);
    }
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : primaryKeyMetadata) {
      values.add(this.data[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()]);
    }
    return values;
    
  }

  /**
   * @return all columns data
   */
  public List<Object> allColumnsData() {
    List<GcTableSyncColumnMetadata> columnMetadatas = this.gcTableSyncTableData.getGcTableSync().getTableMetadata().getColumnMetadata();
    List<Object> values = new ArrayList<Object>();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas) {
      values.add(this.data[columnMetadata.getColumnIndexZeroIndexed()]);
    }
    return values;
  }
}
