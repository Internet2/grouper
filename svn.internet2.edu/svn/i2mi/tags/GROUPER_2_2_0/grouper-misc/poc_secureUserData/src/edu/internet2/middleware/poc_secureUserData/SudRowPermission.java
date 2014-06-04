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
 * holds row permissions per schema
 */
public class SudRowPermission {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    //should be blank:
    
    System.out.println("Should be blank: ");
    for (SudRowPermission sudRowPermission : SudRowPermission.retrieveAllRowPermissions()) {
      System.out.println(sudRowPermission);
    }
    
    System.out.println("Insert: ");
    
    SudRowPermission theSudRowPermission = new SudRowPermission();
    theSudRowPermission.setAction("write");
    theSudRowPermission.setGroupExtension("students");
    theSudRowPermission.setSchemaName("some_schema");
    theSudRowPermission.store();
    
    for (SudRowPermission sudRowPermission : SudRowPermission.retrieveAllRowPermissions()) {
      System.out.println(sudRowPermission);
    }
    
    System.out.println("Update: ");
    
    theSudRowPermission.setGroupExtension("faculty");
    theSudRowPermission.store();
    
    for (SudRowPermission sudRowPermission : SudRowPermission.retrieveAllRowPermissions()) {
      System.out.println(sudRowPermission);
    }

    System.out.println("Delete (should be blank): ");
    
    theSudRowPermission.delete();
    
    for (SudRowPermission sudRowPermission : SudRowPermission.retrieveAllRowPermissions()) {
      System.out.println(sudRowPermission);
    }

    
    
  }
  
  /**
   * @see Object#equals(Object) 
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SudRowPermission)) {
      return false;
    }
    
    SudRowPermission other = (SudRowPermission)obj;
    
    return new GcEqualsBuilder().append(this.groupExtension, other.groupExtension)
      .append(this.action, other.action)
      .append(this.schemaName, other.schemaName).isEquals();
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new GcHashCodeBuilder().append(this.groupExtension).append(this.action).append(this.schemaName).toHashCode();
  }

  /**
   * row permissions
   * @return all row permissions
   */
  public static List<SudRowPermission> retrieveAllRowPermissions() {
    
    List<Object[]> rows = GcDbUtils.listSelect(Object[].class, 
        "select id, group_extension, schema_name, action from secureuserdata_row_permiss", 
        GrouperClientUtils.toList(DbType.STRING, DbType.STRING, DbType.STRING, DbType.STRING));
    
    List<SudRowPermission> results = new ArrayList<SudRowPermission>();
    
    for (Object[] row : rows) {
      
      SudRowPermission sudRowPermission = new SudRowPermission();
      results.add(sudRowPermission);
      
      sudRowPermission.setId((String)row[0]);
      sudRowPermission.setGroupExtension((String)row[1]);
      sudRowPermission.setSchemaName((String)row[2]);
      sudRowPermission.setAction((String)row[3]);
      
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
      return GcDbUtils.executeUpdate("insert into secureuserdata_row_permiss (id, group_extension, schema_name, action) values (?, ?, ?, ?)", 
          GrouperClientUtils.toList((Object)this.id, this.groupExtension, this.schemaName, this.action)) > 0;
      
    }
    
    //update
    return GcDbUtils.executeUpdate("update secureuserdata_row_permiss set group_extension = ?, schema_name = ?, action = ? where id = ?", 
        GrouperClientUtils.toList((Object)this.groupExtension, this.schemaName, this.action, this.id)) > 0;
    
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
    return GcDbUtils.executeUpdate("delete from secureuserdata_row_permiss where id = ?", 
        GrouperClientUtils.toList((Object)this.id));
    
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SudRowPermission [action=" + this.action + ", groupExtension=" + this.groupExtension
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
  
  /** group extension in a certain folder in grouper */
  private String groupExtension;

  
  /**
   * group extension in a certain folder in grouper
   * @return the group extension
   */
  public String getGroupExtension() {
    return this.groupExtension;
  }

  
  /**
   * group extension in a certain folder in grouper
   * @param groupExtension1 the group extension to set
   */
  public void setGroupExtension(String groupExtension1) {
    this.groupExtension = groupExtension1;
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
