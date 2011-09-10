/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.poc_secureUserData.SudOracleUtils.DbType;


/**
 * holds column permissions per schema
 */
public class SudColPermission {

  /**
   * col permissions
   * @return all col permissions
   */
  public static List<SudColPermission> retrieveAllColPermissions() {
    
    List<Object[]> rows = SudOracleUtils.retrieveRows("select id, colset, schema_name, action from secureuserdata_col_permiss", 
        GrouperClientUtils.toList(DbType.STRING, DbType.STRING, DbType.STRING, DbType.STRING));
    
    List<SudColPermission> results = new ArrayList<SudColPermission>();
    
    for (Object[] row : rows) {
      
      SudColPermission sudColPermission = new SudColPermission();
      results.add(sudColPermission);
      
      sudColPermission.setId((String)row[0]);
      sudColPermission.setColset((String)row[1]);
      sudColPermission.setSchemaName((String)row[2]);
      sudColPermission.setAction((String)row[3]);
      
    }
    return results;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SudColPermission [action=" + this.action + ", colset=" + this.colset
        + ", id=" + this.id + ", schemaName=" + this.schemaName + "]";
  }

  /** uuid */
  private String id;

  /**
   * uuid
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /** predefined column sets, e.g. all, name, contact, ids */
  private String colset;

  
  /**
   * predefined column sets, e.g. all, name, contact, ids
   * @return the colset
   */
  public String getColset() {
    return this.colset;
  }

  
  /**
   * predefined column sets, e.g. all, name, contact, ids
   * @param colset1 the colset to set
   */
  public void setColset(String colset1) {
    this.colset = colset1;
  }
  
  /** the schema name in all caps */
  private String schemaName;

  
  /**
   * the schema name in all caps
   * @return the schemaName
   */
  public String getSchemaName() {
    return this.schemaName;
  }

  
  /**
   * the schema name in all caps
   * @param schemaName1 the schemaName to set
   */
  public void setSchemaName(String schemaName1) {
    this.schemaName = schemaName1;
  }
  
  /**
   * read or write
   */
  private String action;

  
  /**
   * read or write
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  
  /**
   * read or write
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }
  
  
  
}
