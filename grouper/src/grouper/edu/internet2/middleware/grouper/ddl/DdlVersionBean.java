/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: DdlVersionBean.java,v 1.6 2009-06-05 12:32:56 shilen Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Connection;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.SqlBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 * bean to pass to versions to update stuff
 */
public class DdlVersionBean {

  /** ddl object name (e.g. Grouper or Subject) */
  private String objectName;
  
  /** ddlutils platform */
  private Platform platform;
  
  /** jdbc connection */
  private Connection connection;
  
  /** schema */
  private String schema;
  
  /** ddlutils sql builder */
  private SqlBuilder sqlBuilder;
  
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
   * @return true if postgres
   */
  public boolean isPostgres() {
    return this.getPlatform().getName().toLowerCase().contains("postgres");
  }

  /**
   * @return true if mysql
   */
  public boolean isMysql() {
    return this.getPlatform().getName().toLowerCase().contains("mysql");
  }

  /**
   * if small indexes
   * @return true if small indexes
   */
  public boolean isSmallIndexes() {
    return this.isMysql();
  }
  
  /**
   * @return true if postgres
   */
  public boolean isOracle() {
    return this.getPlatform().getName().toLowerCase().contains("oracle");
  }
  
  /**
   * @return true if hsql
   */
  public boolean isHsql() {
    return this.getPlatform().getName().toLowerCase().contains("hsqldb");
  }
  
  /**
   * @return true if hsql
   */
  public boolean isSqlServer() {
    return this.getPlatform().getName().toLowerCase().contains("mssql");
  }
  
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

  
  /**
   * ddlutils platform
   * @return the platform
   */
  public Platform getPlatform() {
    return this.platform;
  }

  
  /**
   * ddlutils platform
   * @param platform the platform to set
   */
  public void setPlatform(Platform platform) {
    this.platform = platform;
  }

  
  /**
   * jdbc connection
   * @return the connection
   */
  public Connection getConnection() {
    return this.connection;
  }

  
  /**
   * jdbc connection
   * @param connection the connection to set
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  
  /**
   * schema
   * @return the schema
   */
  public String getSchema() {
    return this.schema;
  }

  
  /**
   * schema
   * @param schema the schema to set
   */
  public void setSchema(String schema) {
    this.schema = schema;
  }

  
  /**
   * ddlutils schemabuilder
   * @return the sqlBuilder
   */
  public SqlBuilder getSqlBuilder() {
    return this.sqlBuilder;
  }

  
  /**
   * construct
   * @param objectName
   * @param platform
   * @param connection
   * @param schema
   * @param sqlBuilder
   * @param oldDatabase
   * @param database
   * @param additionalScripts
   * @param isDestinationVersion
   * @param buildingToVersion
   * @param fullScript
   */
  public DdlVersionBean(String objectName, Platform platform, Connection connection,
      String schema, SqlBuilder sqlBuilder, Database oldDatabase, Database database,
      StringBuilder additionalScripts, boolean isDestinationVersion,
      int buildingToVersion, StringBuilder fullScript) {
    super();
    this.objectName = objectName;
    this.platform = platform;
    this.connection = connection;
    this.schema = schema;
    this.sqlBuilder = sqlBuilder;
    this.oldDatabase = oldDatabase;
    this.database = database;
    this.additionalScripts = additionalScripts;
    this.isDestinationVersion = isDestinationVersion;
    this.buildingToVersion = buildingToVersion;
    this.fullScript = fullScript;
  }

  /**
   * ddlutils schemabuilder
   * @param sqlBuilder the sqlBuilder to set
   */
  public void setSqlBuilder(SqlBuilder sqlBuilder) {
    this.sqlBuilder = sqlBuilder;
  }

  
  /**
   * ddl object name (e.g. Grouper or Subject)
   * @return the objectName
   */
  public String getObjectName() {
    return this.objectName;
  }

  
  /**
   * ddl object name (e.g. Grouper or Subject)
   * @param objectName the objectName to set
   */
  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  
}
