/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class GcTableSyncTableMetadata {

  /**
   * 
   */
  public GcTableSyncTableMetadata() {
  }
  
  /**
   * primary key col(s), lazy loaded
   */
  private List<GcTableSyncColumnMetadata> primaryKey;

  /**
   * primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getPrimaryKey() {
    if (this.primaryKey == null) {
      
      List<GcTableSyncColumnMetadata> thePrimaryKey = new ArrayList<GcTableSyncColumnMetadata>();
      
      // column metadata better be initted
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.columnMetadata) {
        if (gcTableSyncColumnMetadata.isPrimaryKey()) {
          thePrimaryKey.add(gcTableSyncColumnMetadata);
        }
      }
      
      this.primaryKey = thePrimaryKey;
    }
    return this.primaryKey;
  }
  
  /**
   * non primary key col(s), lazy loaded
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKey;

  /**
   * non primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getNonPrimaryKey() {
    if (this.nonPrimaryKey == null) {
      
      List<GcTableSyncColumnMetadata> theNonPrimaryKey = new ArrayList<GcTableSyncColumnMetadata>();
      
      // column metadata better be initted
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.columnMetadata) {
        if (!gcTableSyncColumnMetadata.isPrimaryKey()) {
          theNonPrimaryKey.add(gcTableSyncColumnMetadata);
        }
      }
      
      this.nonPrimaryKey = theNonPrimaryKey;
    }
    return this.nonPrimaryKey;
  }
  
  /**
   * 
   * @param sqlBuilder
   */
  public void appendColumns(StringBuilder sqlBuilder) {
    boolean isFirst = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.getColumnMetadata()) {
      
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
  public void appendQuestionsAllCols(StringBuilder sqlBuilder) {
    boolean isFirst = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.getColumnMetadata()) {
      
      if (!isFirst) {
        sqlBuilder.append(", ");
      }
      sqlBuilder.append("?");
      isFirst = false;
    }
  }

  /**
   * get grouping column metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getGroupingColumnMetadata() {
    for (GcTableSyncColumnMetadata theColumnMetadata : GrouperClientUtils.nonNull(this.columnMetadata)) {
      if (theColumnMetadata.isGroupingColumn()) {
        return theColumnMetadata;
      }
    }
    throw new RuntimeException("Cant find grouping column! " + this.tableNameFrom);
  }
  
  /**
   * get realTimeLastUpdatedColumn metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getRealTimeLastUpdatedColumnMetadata() {
    for (GcTableSyncColumnMetadata theColumnMetadata : GrouperClientUtils.nonNull(this.columnMetadata)) {
      if (theColumnMetadata.isRealTimeLastUpdatedColumn()) {
        return theColumnMetadata;
      }
    }
    throw new RuntimeException("Cant find realTimeLastUpdatedColumn! " + this.tableNameFrom);
  }
  
  /**
   * schema from
   */
  private String schemaFrom;
  
  /**
   * @return the schemaFrom
   */
  public String getSchemaFrom() {
    return this.schemaFrom;
  }
  
  /**
   * @param schemaFrom1 the schemaFrom to set
   */
  public void setSchemaFrom(String schemaFrom1) {
    this.schemaFrom = schemaFrom1;
  }

  /**
   * schema to
   */
  private String schemaTo;
  
  /**
   * schema to
   * @return the schemaTo
   */
  public String getSchemaTo() {
    return this.schemaTo;
  }
  
  /**
   * schema to
   * @param schemaTo1 the schemaTo to set
   */
  public void setSchemaTo(String schemaTo1) {
    this.schemaTo = schemaTo1;
  }

  /**
   * table name to
   */
  private String tableNameTo;
  
  /**
   * table name to
   * @return the tableNameTo
   */
  public String getTableNameTo() {
    return this.tableNameTo;
  }
  
  /**
   * table name to
   * @param tableNameTo1 the tableNameTo to set
   */
  public void setTableNameTo(String tableNameTo1) {
    this.tableNameTo = tableNameTo1;
  }

  /**
   * table name from
   */
  private String tableNameFrom;

  
  /**
   * table name from
   * @return the tableName
   */
  public String getTableNameFrom() {
    return this.tableNameFrom;
  }

  
  /**
   * table name from
   * @param tableName1 the tableName to set
   */
  public void setTableNameFrom(String tableName1) {
    this.tableNameFrom = tableName1;
  }
  
  /**
   * columns in table
   */
  private List<GcTableSyncColumnMetadata> columnMetadata;

  
  /**
   * columns in table
   * @return the columnMetadata
   */
  public List<GcTableSyncColumnMetadata> getColumnMetadata() {
    return this.columnMetadata;
  }

  
  /**
   * columns in table
   * @param columnMetadata1 the columnMetadata to set
   */
  public void setColumnMetadata(List<GcTableSyncColumnMetadata> columnMetadata1) {
    this.columnMetadata = columnMetadata1;
  }

  /**
   * @param sqlBuilder
   */
  public void appendNonPrimaryKeyUpdateColumnNames(StringBuilder sqlBuilder) {
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = this.getNonPrimaryKey();
    if (nonPrimaryKeyMetadata.size() == 0) {
      throw new RuntimeException("Why is non primary key size 0 with an update????");
    }
    
    int colNum = 0;
    for (GcTableSyncColumnMetadata nonPrimaryKeyMetadatum : nonPrimaryKeyMetadata) {
      if (colNum != 0) {
        sqlBuilder.append(", ");
      }
      sqlBuilder.append(nonPrimaryKeyMetadatum.getColumnName() + " = ?");
      colNum++;
    }

  }

  /**
   * @param sqlBuilder
   */
  public void appendPrimaryKeyColumnNames(StringBuilder sqlBuilder) {
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.getPrimaryKey();
    if (primaryKeyMetadata.size() == 0) {
      throw new RuntimeException("Why is primary key size 0????");
    }
    int colNum = 0;
    for (GcTableSyncColumnMetadata primaryKeyMetadatum : primaryKeyMetadata) {
      if (colNum != 0) {
        sqlBuilder.append(" and ");
      }
      sqlBuilder.append(primaryKeyMetadatum.getColumnName() + " = ?");
      colNum++;
    }

  }
  
}
