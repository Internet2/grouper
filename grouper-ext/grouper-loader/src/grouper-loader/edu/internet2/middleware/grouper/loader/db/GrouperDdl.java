/**
 * @author mchyzer
 * $Id: GrouperDdl.java,v 1.1 2008-04-29 13:54:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.db;


/**
 * maps to the grouper ddl table
 */
public class GrouperDdl {
  
  /** uuid of the row */
  private String id = null;
  
  /** object name in db (in java its converted to a java name) */
  private String objectName = null;
  
  /** version of the object in the db */
  private int dbVersion = 0;
  
  /** version of the object that grouper expects */
  private int grouperVersion = 0;
  
  /**
   * object name in db (in java its converted to a java name)
   * @return the objectName
   */
  public String getObjectName() {
    return this.objectName;
  }
  
  /**
   * object name in db (in java its converted to a java name)
   * @param objectName1 the objectName to set
   */
  public void setObjectName(String objectName1) {
    this.objectName = objectName1;
  }
  
  /**
   * version of the object in the db
   * @return the dbVersion
   */
  public int getDbVersion() {
    return this.dbVersion;
  }
  
  /**
   * version of the object in the db
   * @param dbVersion1 the dbVersion to set
   */
  public void setDbVersion(int dbVersion1) {
    this.dbVersion = dbVersion1;
  }
  
  /**
   * version of the object that grouper expects
   * @return the grouperVersion
   */
  public int getGrouperVersion() {
    return this.grouperVersion;
  }
  
  /**
   * version of the object that grouper expects
   * @param grouperVersion1 the grouperVersion to set
   */
  public void setGrouperVersion(int grouperVersion1) {
    this.grouperVersion = grouperVersion1;
  }

  
  /**
   * uuid of the row
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * uuid of the row
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

}
