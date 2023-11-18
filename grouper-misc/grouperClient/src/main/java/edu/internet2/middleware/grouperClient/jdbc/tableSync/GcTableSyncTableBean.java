/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * info about the table.  columns, typees, metadata, data.
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
   */
  public GcTableSyncTableBean() {
  }

  /**
   * construct with gcTableSync
   */
  public GcTableSyncTableBean(GcTableSync theGcTableSync) {
    this.gcTableSync = theGcTableSync;
  }

  /**
   * get the metadata for this table
   * @param databaseFrom
   * @param tableFrom
   */
  public void configureMetadata(String databaseFrom, String tableFrom) {
    this.tableMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromCacheOrDatabase(databaseFrom, tableFrom);
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
   * data in the table
   */
  private GcTableSyncTableData dataInitialQuery;

  
  /**
   * group unique vals from source
   */
  private List<Object> groupUniqueValues;

  /**
   * data in the table
   * @return the data
   */
  public GcTableSyncTableData getDataInitialQuery() {
    return this.dataInitialQuery;
  }

  
  /**
   * data in the table
   * @param data1 the data to set
   */
  public void setDataInitialQuery(GcTableSyncTableData data1) {
    this.dataInitialQuery = data1;
  }

  /**
   * group unique vals from source
   * @return the fromGroupUniqueValues
   */
  public List<Object> getGroupUniqueValues() {
    return this.groupUniqueValues;
  }

  /**
   * group unique vals from source
   * @param fromGroupUniqueValues1 the fromGroupUniqueValues to set
   */
  public void setGroupUniqueValues(List<Object> fromGroupUniqueValues1) {
    this.groupUniqueValues = fromGroupUniqueValues1;
  }

}
