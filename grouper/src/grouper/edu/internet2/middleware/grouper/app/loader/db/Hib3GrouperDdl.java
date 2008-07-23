/**
 * @author mchyzer
 * $Id: Hib3GrouperDdl.java,v 1.2 2008-07-23 06:41:30 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * maps to the grouper ddl table
 */
public class Hib3GrouperDdl {
  
  /** uuid of the row */
  private String id = null;
  
  /** object name in db (in java its converted to a java name) */
  private String objectName = null;
  
  /** version of the object in the db */
  private int dbVersion = 0;
  
  /** last updated timestamp, in string form so easy to update */
  private String lastUpdated = null;
  
  /** history, with most recent first */
  private String history = null;
  
  
  /**
   * last updated timestamp, in string form so easy to update
   * @return the lastUpdated
   */
  public String getLastUpdated() {
    return this.lastUpdated;
  }

  
  /**
   * last updated timestamp, in string form so easy to update
   * @param lastUpdated1 the lastUpdated to set
   */
  public void setLastUpdated(String lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  
  /**
   * history, with most recent first
   * @return the history
   */
  public String getHistory() {
    return this.history;
  }

  
  /**
   * history, with most recent first
   * @param history1 the history to set
   */
  public void setHistory(String history1) {
    this.history = history1;
  }

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
  
  /**
   * find a ddl in a list
   * @param hib3GrouperDdls
   * @param objectName
   * @return the ddl object
   */
  public static Hib3GrouperDdl findInList(List<Hib3GrouperDdl> hib3GrouperDdls, String objectName) {
    if (hib3GrouperDdls != null) {
      for (Hib3GrouperDdl hib3GrouperDdl : hib3GrouperDdls) {
        if (StringUtils.equals(hib3GrouperDdl.getObjectName(), objectName)) {
          return hib3GrouperDdl;
        }
      }
    }
    return null;
  }
    
}
