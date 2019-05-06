/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * data from a table
 */
public class GcTableSyncTableData {

  /**
   * index the data by primary key
   */
  private Map<MultiKey, GcTableSyncRowData> indexByPrimaryKey = null;

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
   * @param primaryKey
   * @return the row
   */
  public GcTableSyncRowData findRowFromPrimaryKey(MultiKey primaryKey) {
    return this.indexByPrimaryKey.get(primaryKey);
  }
  
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
