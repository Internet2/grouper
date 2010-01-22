package edu.internet2.middleware.grouper.ddl;

/**
 * change the database in some way (in between dropping foreign keys etc).
 * this is generally done during testing
 *
 */
public interface DdlUtilsChangeDatabase {
  
  /**
   * callback to change the database
   * @param ddlVersionBean 
   */
  public void changeDatabase(DdlVersionBean ddlVersionBean);
  
}
