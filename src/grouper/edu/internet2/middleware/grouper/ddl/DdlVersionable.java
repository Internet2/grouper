/*
 * @author mchyzer
 * $Id: DdlVersionable.java,v 1.3 2008-07-29 07:05:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;
 
import org.apache.ddlutils.model.Database;


/**
 * enums which are ddl version need to implement this interface
 */
public interface DdlVersionable {

  /**
   * add all foreign keys
   * @param database ddlutils database object
   * @param additionalScripts add additional scripts after the db ddl (e.g. sql).  scripts should be semicolon delimited
   * @param buildingToVersion version we are building towards (in case unit testing)
   */
  public void addAllForeignKeys(Database database, StringBuilder additionalScripts, int buildingToVersion);

  /**
   * get the version of this enum
   * @return the version
   */
  public int getVersion();

  /**
   * get the object name of this enum, e.g. if GrouperEnum, the object name is Grouper
   * @return the object name
   */
  public String getObjectName();

  /**
   * <pre>
   * get the table pattern for this dbname (would be nice if there were no overlap,
   * so ext's should not start with grouper, e.g. grouploader_
   * note that underscore is a wildcard which is unfortunate
   * @return the table patter, e.g. "GROUPER%"
   * </pre>
   */
  public String getDefaultTablePattern();
  
  /**
   * check to see if the changes are already made, and then add the changes
   * to the database object
   * that should be used to update from the previous version
   * @param database ddlutils database object
   * @param ddlVersionBean has references to stuff you need
   */
  public void updateVersionFromPrevious(Database database, DdlVersionBean ddlVersionBean);
}
