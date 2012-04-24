/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcEqualsBuilder;
import edu.internet2.middleware.poc_secureUserData.util.GcHashCodeBuilder;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType;


/**
 * holds column permissions per schema
 */
public class SudColPermission {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    //should be blank:
    
    System.out.println("Should be blank: ");
    for (SudColPermission sudColPermission : SudColPermission.retrieveAllColPermissions()) {
      System.out.println(sudColPermission);
    }
    
    System.out.println("Insert: ");
    
    SudColPermission theSudColPermission = new SudColPermission();
    theSudColPermission.setAction("write");
    theSudColPermission.setColset("name");
    theSudColPermission.setSchemaName("some_schema");
    theSudColPermission.store();
    
    for (SudColPermission sudColPermission : SudColPermission.retrieveAllColPermissions()) {
      System.out.println(sudColPermission);
    }
    
    System.out.println("Update: ");
    
    theSudColPermission.setColset("contact");
    theSudColPermission.store();
    
    for (SudColPermission sudColPermission : SudColPermission.retrieveAllColPermissions()) {
      System.out.println(sudColPermission);
    }

    System.out.println("Delete (should be blank): ");
    
    theSudColPermission.delete();
    
    for (SudColPermission sudColPermission : SudColPermission.retrieveAllColPermissions()) {
      System.out.println(sudColPermission);
    }

    
    
  }
  
  /**
   * @see Object#equals(Object) 
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SudColPermission)) {
      return false;
    }
    
    SudColPermission other = (SudColPermission)obj;
    
    return new GcEqualsBuilder().append(this.colset, other.colset)
      .append(this.action, other.action)
      .append(this.schemaName, other.schemaName).isEquals();
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new GcHashCodeBuilder().append(this.colset).append(this.action).append(this.schemaName).toHashCode();
  }

  /**
   * col permissions
   * @return all col permissions
   */
  public static List<SudColPermission> retrieveAllColPermissions() {
    
    List<Object[]> rows = GcDbUtils.listSelect(Object[].class, 
        "select id, colset, schema_name, action from secureuserdata_col_permiss", 
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
   * insert/update into database (if there is an ID, then update, if not, then store)
   * @return true if it happened
   */
  public boolean store() {
    
    if (GrouperClientUtils.isBlank(this.id)) {
      //TODO make this a uuid
      this.id = GrouperClientUtils.uniqueId();

      //insert
      return GcDbUtils.executeUpdate("insert into secureuserdata_col_permiss (id, colset, schema_name, action) values (?, ?, ?, ?)", 
          GrouperClientUtils.toList((Object)this.id, this.colset, this.schemaName, this.action)) > 0;
      
    }
    
    //update
    return GcDbUtils.executeUpdate("update secureuserdata_col_permiss set colset = ?, schema_name = ?, action = ? where id = ?", 
        GrouperClientUtils.toList((Object)this.colset, this.schemaName, this.action, this.id)) > 0;
    
  }
  
  /**
   * delete from database
   * @return the rowcount
   */
  public int delete() {
    
    if (GrouperClientUtils.isBlank(this.id)) {
      throw new RuntimeException("Why are you deleting something with null id? " + this);
    }
    
    //delete
    return GcDbUtils.executeUpdate("delete from secureuserdata_col_permiss where id = ?", 
        GrouperClientUtils.toList((Object)this.id));
    
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
