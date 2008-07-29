/*
 * @author mchyzer
 * $Id: DdlVersionBean.java,v 1.1 2008-07-29 07:05:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import org.apache.ddlutils.model.Database;


/**
 * bean to pass to versions to update stuff
 */
public class DdlVersionBean {

  /** current state of the database (after modifications :) ).  Note, this is null if this is working on old db */
  private Database oldDatabase;

  /** database we are operating on */
  private Database database;
  
  /** additional scripts to add after ddlutils scripts */
  private StringBuilder additionalScripts; 
  
  /** if this is the version we are building to, though, it might
   * be an intermediate build
   */
  private boolean isDestinationVersion;
  
  /**
   * the eventual version we are build to
   */
  private int buildingToVersion;
  
  /**
   * full script so far (to make sure we dont have duplicate scripts, shouldnt add
   * directly to it from here though
   */
  private StringBuilder fullScript;

  /**
   * append an additionalScript, but only if it is not already in the body of the script (or additionalScript)
   * @param script should contain script (or scripts), and should end in a semicolon (each line should), and should end in newline 
   */
  public void appendAdditionalScriptUnique(String script) {
    if (this.getFullScript().indexOf(script) > -1) {
      return;
    }
 
    if (this.getAdditionalScripts().indexOf(script) > -1) {
      return;
    }
    
    this.getAdditionalScripts().append(script);
    
  }
  
  /**
   * construct
   * @param oldDatabase
   * @param database
   * @param additionalScripts
   * @param isDestinationVersion
   * @param buildingToVersion
   * @param fullScript
   */
  public DdlVersionBean(Database oldDatabase, Database database,
      StringBuilder additionalScripts, boolean isDestinationVersion,
      int buildingToVersion, StringBuilder fullScript) {
    super();
    this.oldDatabase = oldDatabase;
    this.database = database;
    this.additionalScripts = additionalScripts;
    this.isDestinationVersion = isDestinationVersion;
    this.buildingToVersion = buildingToVersion;
    this.fullScript = fullScript;
  }


  /**
   * current state of the database (after modifications :) ).  Note, this is null if this is working on old db
   * @return the oldDatabase
   */
  public Database getOldDatabase() {
    return this.oldDatabase;
  }

  
  /**
   * current state of the database (after modifications :) ).  Note, this is null if this is working on old db
   * @param oldDatabase the oldDatabase to set
   */
  public void setOldDatabase(Database oldDatabase) {
    this.oldDatabase = oldDatabase;
  }

  
  /**
   * database we are operating on
   * @return the database
   */
  public Database getDatabase() {
    return this.database;
  }

  
  /**
   * database we are operating on
   * @param database the database to set
   */
  public void setDatabase(Database database) {
    this.database = database;
  }

  
  /**
   * additional scripts to add after ddlutils scripts
   * @return the additionalScripts
   */
  public StringBuilder getAdditionalScripts() {
    return this.additionalScripts;
  }

  
  /**
   * additional scripts to add after ddlutils scripts
   * @param additionalScripts the additionalScripts to set
   */
  public void setAdditionalScripts(StringBuilder additionalScripts) {
    this.additionalScripts = additionalScripts;
  }

  
  /**
   * @return the isDestinationVersion
   */
  public boolean isDestinationVersion() {
    return this.isDestinationVersion;
  }

  
  /**
   * @param isDestinationVersion the isDestinationVersion to set
   */
  public void setDestinationVersion(boolean isDestinationVersion) {
    this.isDestinationVersion = isDestinationVersion;
  }

  
  /**
   * the eventual version we are build to
   * @return the buildingToVersion
   */
  public int getBuildingToVersion() {
    return this.buildingToVersion;
  }

  
  /**
   * the eventual version we are build to
   * @param buildingToVersion the buildingToVersion to set
   */
  public void setBuildingToVersion(int buildingToVersion) {
    this.buildingToVersion = buildingToVersion;
  }

  
  /**
   * full script so far (to make sure we dont have duplicate scripts, shouldnt add
   * directly to it from here though
   * @return the fullScript
   */
  public StringBuilder getFullScript() {
    return this.fullScript;
  }

  
  /**
   * full script so far (to make sure we dont have duplicate scripts, shouldnt add
   * directly to it from here though
   * @param fullScript the fullScript to set
   */
  public void setFullScript(StringBuilder fullScript) {
    this.fullScript = fullScript;
  }
  
}
