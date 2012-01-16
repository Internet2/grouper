/*
 * @author mchyzer $Id: GrouperDdlUtils.java,v 1.49 2009-12-05 06:39:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperShell;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeFinder;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleDdlInitBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.registry.RegistryInstall;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

/**
 *
 */
public class GrouperDdlUtils {

  /**
   * see if the config file seems to be hsql
   * @return see if hsql
   */
  public static boolean isHsql() {
    return isHsql(GrouperConfig.getHibernateProperty("hibernate.connection.url"));
  }

  /**
   * see if the config file seems to be hsql
   * @param connectionUrl url to check against
   * @return see if hsql
   */
  public static boolean isHsql(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":hsqldb:");
  }
  
  /**
   * see if the config file seems to be postgres
   * @return see if postgres
   */
  public static boolean isPostgres() {
    return isPostgres(GrouperConfig.getHibernateProperty("hibernate.connection.url"));
  }
  
  /**
   * see if the config file seems to be postgres
   * @param connectionUrl
   * @return see if postgres
   */
  public static boolean isPostgres(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":postgresql:");
  }
  
  /**
   * see if the config file seems to be oracle
   * @return see if oracle
   */
  public static boolean isOracle() {
    return isOracle(GrouperConfig.getHibernateProperty("hibernate.connection.url"));
  }
  
  /**
   * see if the config file seems to be oracle
   * @param connectionUrl
   * @return see if oracle
   */
  public static boolean isOracle(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":oracle:");
  }
  
  /**
   * see if the config file seems to be mysql
   * @return see if mysql
   */
  public static boolean isMysql() {
    return isMysql(GrouperConfig.getHibernateProperty("hibernate.connection.url"));
  }
  
  /**
   * see if the config file seems to be mysql
   * @param connectionUrl
   * @return see if mysql
   */
  public static boolean isMysql(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":mysql:");
  }
  
  /**
   * see if the config file seems to be sql server
   * @return see if sql server
   */
  public static boolean isSQLServer() {
    return isSQLServer(GrouperConfig.getHibernateProperty("hibernate.connection.url"));
  }
  
  /**
   * see if the config file seems to be sql server
   * @param connectionUrl
   * @return see if sql server
   */
  public static boolean isSQLServer(String connectionUrl) {
    return StringUtils.defaultString(connectionUrl).toLowerCase().contains(":sqlserver:");
  }
  
  /**
   * bean 
   */
  public static class DbMetadataBean {
    /** default table pattern, prefix in db metadata to retrieve objects */
    private String defaultTablePattern;
  
    /** schema to use for db metadata */
    private String schema;
  
    /**
     * default table pattern, prefix in db metadata to retrieve objects
     * @return string
     */
    public String getDefaultTablePattern() {
      return this.defaultTablePattern;
    }
  
    
    /**
     * default table pattern, prefix in db metadata to retrieve objects
     * @param defaultTablePattern1
     */
    public void setDefaultTablePattern(String defaultTablePattern1) {
      this.defaultTablePattern = defaultTablePattern1;
    }
  
    /**
     * schema to use for db metadata
     * @return the schema to use for db metadata
     */
    public String getSchema() {
      return this.schema;
    }
  
    /**
     * schema to use for db metadata
     * @param schema1
     */
    public void setSchema(String schema1) {
      this.schema = schema1;
    }
  }
  /**
   * 
   */
  private static final String PLATFORM_NAME = "grouper";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdlUtils.class);

  /** if inside bootstrap, ok to use hibernate */
  private static boolean insideBootstrap = false;

  /**
   * if we are inside the bootstrap, or if everything is ok, we are good to go
   * @return true if ok
   */
  public static boolean okToUseHibernate() {
    return justTesting || insideBootstrap || everythingRightVersion || RegistryInitializeSchema.inInitSchema;
  }
  
  /** cache the platform */
  private static Platform cachedPlatform = null;
  
  /**
   * retrieve the ddl utils platform
   * @return the platform object
   */
  public static Platform retrievePlatform() {
    return retrievePlatform(true);
  }
  /**
   * retrieve the ddl utils platform
   * @param useCache if we should get from cache if it is available
   * @return the platform object
   */
  public static Platform retrievePlatform(boolean useCache) {
    
    if (cachedPlatform == null || !useCache) {
      
      String ddlUtilsDbnameOverride = GrouperConfig.getProperty("ddlutils.dbname.override");
  
      //convenience to get the url, user, etc of the grouper db
      GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
  
      if (StringUtils.isBlank(ddlUtilsDbnameOverride)) {
        cachedPlatform = PlatformFactory.createNewPlatformInstance(grouperDb.getDriver(),
            grouperDb.getUrl());
      } else {
        cachedPlatform = PlatformFactory.createNewPlatformInstance(ddlUtilsDbnameOverride);
      }
    }
    return cachedPlatform;
  }

  /**
   * kick off bootstrap
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
  }
  
  
  /**
   * true to compare ddl from db version to the current java version, 
   * false to start over and find all diffs (without deleting existing) 
   */
  public static boolean compareFromDbDllVersion = true;

  /**
   * only run this once
   */
  private static boolean bootstrapDone = false;
  
  /** if everything is the right version */
  public static boolean everythingRightVersion = true;
  
  /** set to true if versions will mismatch but we want to continue anyways... */
  static boolean justTesting = false;
  
  /**
   * startup the process, if the version table is not there, print out that ddl
   * @param callFromCommandLine
   * @param installDefaultGrouperData 
   * @param promptUser prompt user to see if they really want to do this
   */
  @SuppressWarnings("unchecked")
  public static void bootstrap(boolean callFromCommandLine, boolean installDefaultGrouperData, boolean promptUser) {
    if (bootstrapDone) {
      if (callFromCommandLine) {
        throw new RuntimeException("DDL bootstrap is already done, something is wrong...");
      }
      return;
    }

    try {
      //do here so we arent re-entrant
      bootstrapDone = true;
  
      bootstrapHelper(callFromCommandLine, false, !callFromCommandLine || compareFromDbDllVersion, 
          false, 
          false, false, installDefaultGrouperData, null, promptUser);
    } catch (RuntimeException re) {
      everythingRightVersion = false;
      throw re;
    }
  }

  /** keep track if we have already inserted a record here, then subsequent ones are updates */
  private static Set<String> alreadyInsertedForObjectName = new HashSet<String>();
  
  /**
   * make a max version map
   * @param maxVersion
   * @return the map
   */
  static Map<String, DdlVersionable> maxVersionMap(DdlVersionable maxVersion) {
    Map<String, DdlVersionable> result = new HashMap<String, DdlVersionable>();
    result.put(maxVersion.getObjectName(), maxVersion);
    return result;
  }
  
  /** set this to false for testing */
  public static boolean internal_printDdlUpdateMessage = true;
  
  /**
   * helper method which is more easily testable
   * @param callFromCommandLine
   * @param fromUnitTest true if just testing this method
   * @param theCompareFromDbVersion 
   * @param theDropBeforeCreate
   * @param theWriteAndRunScript
   * @param dropOnly just drop stuff, e.g. for unit test
   * @param installDefaultGrouperData if registry install should be called afterwards
   * @param maxVersions if unit testing, and not going to max, then associate object name
   * with max version
   * @param promptUser promptUser to see if they want to do this...
   * @return true if up to date, false if needs to run a script
   */
  @SuppressWarnings("unchecked")
  public static boolean bootstrapHelper(boolean callFromCommandLine, boolean fromUnitTest,
      boolean theCompareFromDbVersion, boolean theDropBeforeCreate, boolean theWriteAndRunScript,
      boolean dropOnly, boolean installDefaultGrouperData, Map<String, DdlVersionable> maxVersions,
      boolean promptUser) {
        
    //start with success
    everythingRightVersion = true;
    
    if (promptUser) {
      String prompt = GrouperUtil.PROMPT_KEY_SCHEMA_EXPORT_ALL_TABLES + " (dropThenCreate=" + (theDropBeforeCreate ? "T" : "F")
        + ",writeAndRunScript=" + (theWriteAndRunScript ? "T" : "F") + ")";
     
     //make sure it is ok to change db
     GrouperUtil.promptUserAboutDbChanges(prompt, true);
 
    }
    boolean upToDate = false;
    String resultString = null;
    
    try {
      insideBootstrap = true;
  
      //clear out for this run (in case testing, might call this multiple times)
      alreadyInsertedForObjectName.clear();
      
      //clear out cache of object versions since if multiple calls from unit tests, can get bad data
      cachedDdls = null;
      
      //if we are messing with ddl, lets clear caches
      FieldFinder.clearCache();
      GroupTypeFinder.clearCache();
      
      Platform platform = retrievePlatform(false);
      
      //this is in the config or just in the driver
      String dbname = platform.getName();
      
      LOG.info("Ddl db name is: '" + dbname + "'");
      
      //convenience to get the url, user, etc of the grouper db, helps get db connection
      GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
      
      Connection connection = null;
      
      String schema = grouperDb.getUser().toUpperCase();
      
      //postgres needs lower I think
      if (platform.getName().toLowerCase().contains("postgre")) {
        schema = schema.toLowerCase();
      }
      
      StringBuilder result = new StringBuilder();
      
      try {
        connection = grouperDb.connection();
  
        List<String> objectNames = retrieveObjectNames();
        //System.err.println(GrouperUtil.toStringForLog(objectNames));
        for (String objectName : objectNames) {
  
          if (StringUtils.equals("GrouperLoader", objectName)) {
            LOG.warn("GrouperLoader should not be in the Grouper_ddl table, deleting");
            HibernateSession.bySqlStatic().executeSql("delete from grouper_ddl where object_name = 'GrouperLoader'");
            continue;
          }
          
          Class<Enum> objectEnumClass = null;
          
          try {
            objectEnumClass = retrieveDdlEnum(objectName);
          } catch (RuntimeException e) {
            //if this is grouper or subject, we have problems
            if (StringUtils.equals(objectName, "Grouper") || StringUtils.equals(objectName, "Subject")) {
              //kill the app
              everythingRightVersion = false;
              throw e;
            }
            //this is probably ok I guess, since the UI tables might not have logic in ws or whatever...
            LOG.warn("This might be ok, since the DDL isnt managed from this app, but here is the issue for ddl app '" + objectName + "' " + e.getMessage(), e);
          }
          
          //this is the version in java
          int javaVersion = retrieveDdlJavaVersion(objectName); 
          
          //maybe override this if unit testing
          if (maxVersions!=null && maxVersions.containsKey(objectName)) {
            javaVersion = maxVersions.get(objectName).getVersion();
          }
          
          DdlVersionable ddlVersionable = retieveVersion(objectName, javaVersion);
          
          StringBuilder historyBuilder = retrieveHistory(objectName);
          
          //this is the version in the db
          int realDbVersion = retrieveDdlDbVersion(objectName);
          
          String versionStatus = "Grouper ddl object type '" + objectName + "' has dbVersion: " 
            + realDbVersion + " and java version: " + javaVersion;
          
          boolean versionMismatch = javaVersion != realDbVersion;
  
          if (versionMismatch) {
            if (internal_printDdlUpdateMessage) {
            System.err.println(versionStatus);
            LOG.error(versionStatus);
            }
          } else {
            LOG.info(versionStatus);
          }
  
          //one originally in the DB
          @SuppressWarnings("unused")
          int originalDbVersion = realDbVersion;
          
          //this is the logic version in the objects
          int dbVersion = realDbVersion;
  
          if (!theCompareFromDbVersion || theDropBeforeCreate) {
            //if going from nothing, then go from nothing
            dbVersion = 0;
          }
          
          //reset to take into account if starting from scratch
          versionMismatch = javaVersion != dbVersion;
          
          //see if same version, just continue, all good
          if (!versionMismatch && !dropOnly) {
            continue;
          }
          
          //if the java is less than db, then grouper was rolled back... that might not be good
          if (javaVersion < dbVersion && !dropOnly) {
            LOG.error("Java version of db object name: " + objectName + " is " 
                + javaVersion + " which is less than the dbVersion " + dbVersion
                + ".  This means grouper was upgraded and rolled back?  Check in the enum "
                + objectEnumClass.getName() + " for details on if things are compatible.");
            //not much we can do here... good luck!
            continue;
          }
  
          //shut down hibernate if not just testing
          if (!fromUnitTest) {
            everythingRightVersion = false;
          }
          
          DbMetadataBean dbMetadataBean = findDbMetadataBean(ddlVersionable);
          
          //to be safe lets only deal with tables related to this object
          platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
          //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
          platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
            
          SqlBuilder sqlBuilder = platform.getSqlBuilder();
  
          {
            //drop all views since postgres will drop view cascade (and we dont know about it), and cant create or replace with changes
            DdlVersionBean tempDdlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, null, null, null, false, -1, result);
            ddlVersionable.dropAllViews(tempDdlVersionBean);
  
            //drop all foreign keys since ddlutils likes to do this anyways, lets do it before the script starts
            dropAllForeignKeysScript(dbMetadataBean, tempDdlVersionBean);
          }
          
          // if deleting all, lets delete all:
          if (theDropBeforeCreate || dropOnly) {
            //it needs a name, just use "grouper"
            Database oldDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                null, null);
            dropAllForeignKeys(oldDatabase);
            
            Database newDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                null, null);
            dropAllForeignKeys(newDatabase);
  
            removeAllTables(newDatabase);
            
            String script = convertChangesToString(objectName, sqlBuilder, oldDatabase, newDatabase);
            
            if (!StringUtils.isBlank(script)) {
              //result.append("\n-- we are configured in grouper.properties to drop all tables \n");
              result.append(script).append("\n");
              //result.append("\n-- end drop all tables \n\n");
            }
            
          }
          
          if (!dropOnly) {
            //the db version is less than the java version
            //lets go up one version at a time until we are current
            for (int version = dbVersion+1; version<=javaVersion;version++) {
    
              ddlVersionable = retieveVersion(objectName, version);
              //we just want a script, see if one exists for this version
              String script = findScriptOverride(ddlVersionable, dbname);
              
              //if there was no override
              if (StringUtils.isBlank(script)) {
                
                //it needs a name, just use "grouper"
                Database oldDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                    null, null);
                dropAllForeignKeys(oldDatabase);
                
                Database newDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                    null, null);
                dropAllForeignKeys(newDatabase);
                
                if (theDropBeforeCreate) {
                  removeAllTables(oldDatabase);
                  removeAllTables(newDatabase);
                }
                
                //get this to the previous version, dont worry about additional scripts
                upgradeDatabaseVersion(oldDatabase, null, dbVersion, objectName, version-1, javaVersion, 
                    new StringBuilder(), result, platform, connection, schema, sqlBuilder);
                
                StringBuilder additionalScripts = new StringBuilder();
                
                //get this to the current version
                upgradeDatabaseVersion(newDatabase, oldDatabase, dbVersion, objectName, version, 
                    javaVersion, additionalScripts, result, platform, connection, schema, sqlBuilder);
                
                script = convertChangesToString(objectName, sqlBuilder, oldDatabase,
                    newDatabase);
                
                script = StringUtils.trimToEmpty(script);
                
                String additionalScriptsString = additionalScripts.toString();
                if (!StringUtils.isBlank(additionalScriptsString)) {
                  script += "\n" + additionalScriptsString;
                }
                
                //String ddl = platform.getAlterTablesSql(connection, database);
              }
              //make sure no single quotes in any of these...
              String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
              //is this db independent?  if not, figure out what the issues are and fix so we can have comments
              String summary = timestamp + ": upgrade " + objectName + " from V" + (version-1) + " to V" + version;
              
              boolean scriptNotBlank = !StringUtils.isBlank(script);
              //dont do this if shouldnt
              boolean upgradeDdlTable = realDbVersion < version || theDropBeforeCreate;
    
              if (scriptNotBlank || upgradeDdlTable) {
                //result.append("\n-- " + summary + " \n");
              }
              
              if (scriptNotBlank) {
                result.append(script).append("\n\n");
              }
    
              if (upgradeDdlTable) {
                realDbVersion = version;
                historyBuilder.insert(0, summary + ", ");
                
                String historyString = StringUtils.abbreviate(historyBuilder.toString(), 4000);
                
                //mssql needs begin tx
                if (platform.getName().toLowerCase().contains("mssql")) {
                  result.append("\nbegin transaction");
                }
                
                //see if already in db
                if ((!containsDbRecord(objectName) || (version == 1 && theDropBeforeCreate)) 
                    && !alreadyInsertedForObjectName.contains(objectName)) {
                
                  result.append("\ninsert into grouper_ddl (id, object_name, db_version, " +
                  		"last_updated, history) values ('" + GrouperUuid.getUuid() 
                      +  "', '" + objectName + "', 1, '" + timestamp + "', \n'" + historyString + "');\n");
                  //dont insert again for this object
                  alreadyInsertedForObjectName.add(objectName);
  
                } else {
                  
                  result.append("\nupdate grouper_ddl set db_version = " + version 
                      + ", last_updated = '" + timestamp + "', \nhistory = '" + historyString 
                      + "' where object_name = '" + objectName + "';\n");
  
                }
                result.append("commit;\n\n");
              }
            }
            
            //now we need to add the foreign keys back in
            //just get the first version since we need an instance, any instance
            {
              
              //get the latest, doesnt really matter
              ddlVersionable = retieveVersion(objectName, javaVersion);
  
              //it needs a name, just use "grouper"
              Database oldDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                  null, null);
              dropAllForeignKeys(oldDatabase);
              
              Database newDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
                  null, null);
              dropAllForeignKeys(newDatabase);
              
              //get this to the current version, dont worry about additional scripts
              upgradeDatabaseVersion(oldDatabase, null, dbVersion, objectName, javaVersion, javaVersion, 
                  new StringBuilder(), result, platform, connection, schema, sqlBuilder);
              
              //get this to the current version, dont worry about additional scripts
              upgradeDatabaseVersion(newDatabase, oldDatabase, dbVersion, objectName, javaVersion, 
                  javaVersion, new StringBuilder(), result, platform, connection, schema, sqlBuilder);
  
              StringBuilder additionalScripts = new StringBuilder();
              
              DdlVersionBean ddlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, 
                  sqlBuilder, oldDatabase, newDatabase, additionalScripts, true, javaVersion, result);
              
              ddlVersionable.addAllForeignKeysViewsEtc(ddlVersionBean);
  
              ////lets add table / col comments
              //for (Table table : newDatabase.getTables()) {
              //  GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, table.getName(), table.getDescription());
              //  for (Column column : table.getColumns()) {
              //    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, table.getName(), column.getName(), column.getDescription());
              //  }
              //}

              String script = convertChangesToString(objectName, sqlBuilder, oldDatabase,
                  newDatabase);
              
              script = StringUtils.trimToEmpty(script);
              
              String additionalScriptsString = additionalScripts.toString();
              if (!StringUtils.isBlank(additionalScriptsString)) {
                script += "\n" + additionalScriptsString;
              }
  
              
              if (!StringUtils.isBlank(script)) {
                //result.append("\n-- add back all the foreign keys */\n");
                result.append(script).append("\n");
                //result.append("\n-- end add back all foreign keys */\n\n");
              }
            }
  
            
          }
        }
  
      } finally {
        GrouperUtil.closeQuietly(connection);
      }
  
      resultString = result.toString();
      
      //if mysql, substitute varchar4000 for text
      if (isMysql() && !GrouperConfig.getPropertyBoolean("ddlutils.dontSubstituteVarchar4000forTextMysql", false)) {
        resultString = StringUtils.replace(resultString, "VARCHAR(4000)", "text");
      }
      
      if (StringUtils.isNotBlank(resultString)) {
  
        String scriptDirName = GrouperConfig.getProperty("ddlutils.directory.for.scripts");
        
        File scriptFile = GrouperUtil.newFileUniqueName(scriptDirName, "grouperDdl", ".sql", true);
        GrouperUtil.saveStringIntoFile(scriptFile, resultString);
  
        String logMessage = "Grouper database schema DDL requires updates\n(should run script manually and carefully, in sections, verify data before drop statements, backup/export important data before starting, follow change log on confluence, dont run exact same script in multiple envs - generate a new one for each env),\nscript file is:\n" + GrouperUtil.fileCanonicalPath(scriptFile);
        if (internal_printDdlUpdateMessage) {
        LOG.error(logMessage);
        System.err.println(logMessage);
        }
        logMessage = "";
        if (theWriteAndRunScript) {
          sqlRun(scriptFile, grouperDb.getDriver(), grouperDb.getUrl(), 
              grouperDb.getUser(), grouperDb.getPass(), fromUnitTest, callFromCommandLine);
          //lets clear the type cache
          AuditTypeFinder.clearCache();
          ChangeLogTypeFinder.clearCache();
        } else {
          if (callFromCommandLine || GrouperShell.runFromGsh) {
            System.err.println("Note: this script was not executed due to option passed in");
            System.err.println("To run script via gsh, carefully review it, then run this:\ngsh -registry -runsqlfile " 
                + GrouperUtil.fileCanonicalPath(scriptFile).replace("\\", "\\\\"));
          }
        }
      } else {
        boolean printed = false;
        String note = "NOTE: database table/object structure (ddl) is up to date";
        if (!theCompareFromDbVersion) {
          //no script to update
          if (LOG.isErrorEnabled()) {
            LOG.error(note);
          } else {
            printed = true;
            System.err.println(note);
          }
        }
        if (!printed && callFromCommandLine) {
          System.err.println(note);
        }
        upToDate = true;
      }
      if (installDefaultGrouperData && !dropOnly && (upToDate || theWriteAndRunScript)) {
        try {
          //lets reset the hibernate configuration so it can get properly configured
          Hib3DAO.hibernateInitted = false;
          RegistryInstall.install();
        } catch (RuntimeException e) {
          if (!GrouperShell.runFromGsh && callFromCommandLine && !theWriteAndRunScript) {
            String addendum = LOG.isInfoEnabled() ? "" : ".  The specifics are not logged";
            String error = "FATAL: could not install grouper data, you need to run the SQL script, then try again" + addendum + ": " + e.getMessage();
            System.err.println(error);
            LOG.fatal(error);
            LOG.info("stack", e);
            System.exit(1);
          } else {
            
            GrouperDdlUtils.everythingRightVersion = false;
            throw e;
          }
        }
      }
    } finally {
      insideBootstrap = false;
    }
    return false;
  }
  

  /** run a script file against the default database
   * 
   * @param scriptFile
   * @param fromUnitTest 
   * @param printErrorToStdOut 
   * @return the output
   */
  public static String sqlRun(File scriptFile, boolean fromUnitTest, boolean printErrorToStdOut) {
   Properties properties = GrouperUtil.propertiesFromResourceName(
       "grouper.hibernate.properties");
     
   String user = properties.getProperty("hibernate.connection.username");
   String pass = properties.getProperty("hibernate.connection.password");
   String url = properties.getProperty("hibernate.connection.url");
   String driver = properties.getProperty("hibernate.connection.driver_class");
   pass = Morph.decryptIfFile(pass);
   driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(url, driver);
   return GrouperDdlUtils.sqlRun(scriptFile, driver, url, user, pass, fromUnitTest, printErrorToStdOut);

  }
  
  /**
   * if there is no driver class specified, then try to derive it from the URL
   * @param connectionUrl
   * @param driverClassName
   * @return the driver class
   */
  public static String convertUrlToDriverClassIfNeeded(String connectionUrl, String driverClassName) {
    //default some of the stuff
    if (StringUtils.isBlank(driverClassName)) {
      
      if (GrouperDdlUtils.isHsql(connectionUrl)) {
        driverClassName = "org.hsqldb.jdbcDriver";
      } else if (GrouperDdlUtils.isMysql(connectionUrl)) {
        driverClassName = "com.mysql.jdbc.Driver";
      } else if (GrouperDdlUtils.isOracle(connectionUrl)) {
        driverClassName = "oracle.jdbc.driver.OracleDriver";
      } else if (GrouperDdlUtils.isPostgres(connectionUrl)) { 
        driverClassName = "org.postgresql.Driver";
      } else if (GrouperDdlUtils.isSQLServer(connectionUrl)) {
        driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      } else {
        
        //if this is blank we will figure it out later
        if (!StringUtils.isBlank(connectionUrl)) {
        
          String error = "Cannot determine the driver class from database URL: " + connectionUrl;
          System.err.println(error);
          LOG.error(error);
          return null;
        }
      }
    }
    return driverClassName;

  }
  
  /**
   * if there is no driver class specified, then try to derive it from the URL
   * @param connectionUrl
   * @param hibernateDialect
   * @return the driver class
   */
  public static String convertUrlToHibernateDialectIfNeeded(String connectionUrl, String hibernateDialect) {
    //default some of the stuff
    if (StringUtils.isBlank(hibernateDialect)) {
      
      if (GrouperDdlUtils.isHsql()) {
        hibernateDialect = "org.hibernate.dialect.HSQLDialect";
      } else if (GrouperDdlUtils.isMysql()) {
        hibernateDialect = "org.hibernate.dialect.MySQL5Dialect";
      } else if (GrouperDdlUtils.isOracle()) {
        hibernateDialect = "org.hibernate.dialect.Oracle10gDialect";
      } else if (GrouperDdlUtils.isPostgres()) { 
        hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect";
      } else if (GrouperDdlUtils.isSQLServer()) {
        hibernateDialect = "org.hibernate.dialect.SQLServerDialect";
      } else {
        
        //if this is blank we will figure it out later
        if (!StringUtils.isBlank(connectionUrl)) {
        
          String error = "Cannot determine the hibernate dialect from database URL: " + connectionUrl;
          System.err.println(error);
          LOG.error(error);
          return null;
        }
      }
    }
    return hibernateDialect;

  }
  
  /**
   * run some sql
   * @param scriptFile
   * @param driver 
   * @param url 
   * @param user 
   * @param pass 
   * @param fromUnitTest 
   * @param printErrorToStdOut 
   * @return the output
   */
  public static String sqlRun(File scriptFile, String driver, String url, String user, String pass, boolean fromUnitTest, boolean printErrorToStdOut) {
    
    PrintStream err = System.err;
    PrintStream out = System.out;
    InputStream in = System.in;

    //dont let ant mess up or close the streams
    ByteArrayOutputStream baosOutErr = new ByteArrayOutputStream();
    PrintStream newOutErr = new PrintStream(baosOutErr);

    System.setErr(newOutErr);
    System.setOut(newOutErr);
    
    SQLExec sqlExec = new SQLExec();
    
    boolean deleteScriptAfterward = false;
    
    if (url.contains(":sqlserver:")) {
      
      String script = GrouperUtil.readFileIntoString(scriptFile);
      //we need to strip these out
      if (script.contains("\ngo\n")) {
        
        script = StringUtils.replace(script, "\ngo\n", "\n");
        scriptFile = new File(scriptFile.getAbsolutePath() + ".tmp");
        GrouperUtil.saveStringIntoFile(scriptFile, script);
      }
      
    }
    sqlExec.setSrc(scriptFile);
    
    
    sqlExec.setDriver(driver);
    sqlExec.setUrl(url);
    sqlExec.setUserid(user);
    sqlExec.setPassword(pass);

    Project project = new GrouperAntProject();

    //tell output where to go
    DefaultLogger defaultLogger = new DefaultLogger();
    defaultLogger.setErrorPrintStream(newOutErr);
    defaultLogger.setOutputPrintStream(newOutErr);
    project.addBuildListener(defaultLogger);
    String logMessage = null;
    try {
      sqlExec.setProject(project);

      sqlExec.execute();

      logMessage = "Script was executed successfully\n";
    } catch (Exception e) {
      String error = "Error running script: " + scriptFile.getAbsolutePath();
      logMessage = error + ", " + ExceptionUtils.getFullStackTrace(e) + "\n";
      if (fromUnitTest) {
        throw new RuntimeException(error, e);
      }
    } finally {
    
      newOutErr.flush();
      newOutErr.close();
      
      System.setErr(err);
      System.setOut(out);
      System.setIn(in);
    }
    
    String antOutput = StringUtils.trimToEmpty(baosOutErr.toString());

    if (!StringUtils.isBlank(antOutput)) {
      logMessage += antOutput + "\n";
    }
    //if call from command line, print to screen
    if (LOG.isErrorEnabled() && !printErrorToStdOut) {
      LOG.error(logMessage);
    } else {
      if (internal_printDdlUpdateMessage) {
      System.out.println(logMessage);
    }
    }
    
    if (deleteScriptAfterward) {
      scriptFile.delete();
    }
    
    //clear all caches
    GrouperCacheUtils.clearAllCaches();
    
    return logMessage;
  } 
  
  /**
   * <pre>
   * helper method to run custom db ddl, which is more easily testable
   * TODO consolidate this code with the bootstrap code in the DdlVersionBean or somewhere
   * Here is an example:
   * 
   *     GrouperDdlUtils.changeDatabase(GrouperDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
   * 
   *       public void changeDatabase(DdlVersionBean ddlVersionBean) {
   *         
   *         Database database = ddlVersionBean.getDatabase();
   *         {
   *           Table attributesTable = database.findTable(Attribute.TABLE_GROUPER_ATTRIBUTES);
   *           Column attributesFieldIdColumn = attributesTable.findColumn(Attribute.COLUMN_FIELD_ID);
   *           attributesTable.removeColumn(attributesFieldIdColumn);
   *         }
   *         
   *         {
   *           Table membershipsTable = database.findTable(Membership.TABLE_GROUPER_MEMBERSHIPS);
   *           Column membershipsFieldIdColumn = membershipsTable.findColumn(Membership.COLUMN_FIELD_ID);
   *           membershipsTable.removeColumn(membershipsFieldIdColumn);
   *         }
   *         
   *       }
   *       
   *     });
   * 
   * 
   * </pre>
   * @param objectName (from enum of ddl utils type)
   * @param ddlUtilsChangeDatabase is the callback to change the database
   * @return string
   */
  @SuppressWarnings("unchecked")
  public static String changeDatabase(String objectName, DdlUtilsChangeDatabase ddlUtilsChangeDatabase) {
        
    String resultString = null;
    
    try {
      insideBootstrap = true;

      //clear out cache of object versions since if multiple calls from unit tests, can get bad data
      cachedDdls = null;
      
      //if we are messing with ddl, lets clear caches
      FieldFinder.clearCache();
      GroupTypeFinder.clearCache();
      
      Platform platform = retrievePlatform(false);
      
      //convenience to get the url, user, etc of the grouper db, helps get db connection
      GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
      
      Connection connection = null;
      
      String schema = grouperDb.getUser().toUpperCase();
      
      //postgres needs lower I think
      if (platform.getName().toLowerCase().contains("postgre")) {
        schema = schema.toLowerCase();
      }
      
      StringBuilder result = new StringBuilder();
      
      try {
        connection = grouperDb.connection();
  
        //this is the version in java
        int javaVersion = retrieveDdlJavaVersion(objectName); 
        
        DdlVersionable ddlVersionable = retieveVersion(objectName, javaVersion);
        
        DbMetadataBean dbMetadataBean = findDbMetadataBean(ddlVersionable);
        
        //to be safe lets only deal with tables related to this object
        platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
        //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
        platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
        
        SqlBuilder sqlBuilder = platform.getSqlBuilder();
        
        {
          DdlVersionBean tempDdlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder,
              null, null, null, false, -1, result);
          ddlVersionable.dropAllViews(tempDdlVersionBean);
  
          //drop all foreign keys since ddlutils likes to do this anyways, lets do it before the script starts
          dropAllForeignKeysScript(dbMetadataBean, tempDdlVersionBean);
        }
        
        //it needs a name, just use "grouper"
        Database oldDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
            null, null);
        dropAllForeignKeys(oldDatabase);
          
        Database newDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
            null, null);
        dropAllForeignKeys(newDatabase);

        StringBuilder additionalScripts = new StringBuilder();
        //callback
        DdlVersionBean ddlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, oldDatabase, newDatabase, additionalScripts, true, -1, result);
        ddlUtilsChangeDatabase.changeDatabase(ddlVersionBean);
        
        String script = convertChangesToString(objectName, sqlBuilder, oldDatabase, newDatabase);
          
        if (!StringUtils.isBlank(script)) {
          //result.append("\n-- we are configured in grouper.properties to drop all tables \n");
          result.append(script).append("\n");
          //result.append("\n-- end drop all tables \n\n");
        }
        
        if (additionalScripts.length() > 0) {
          result.append(additionalScripts).append("\n");
          additionalScripts = new StringBuilder();
          ddlVersionBean.setAdditionalScripts(additionalScripts);
        }
        //add back in the foreign keys
        ddlVersionable.addAllForeignKeysViewsEtc(ddlVersionBean);
          
        //lets add table / col comments
        for (Table table : newDatabase.getTables()) {
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, table.getName(), table.getDescription());
          for (Column column : table.getColumns()) {
            GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, table.getName(), column.getName(), column.getDescription());
          }
        }

      } finally {
        GrouperUtil.closeQuietly(connection);
      }
  
      resultString = result.toString();
      
      String scriptDirName = GrouperConfig.getProperty("ddlutils.directory.for.scripts");
      
      File scriptFile = GrouperUtil.newFileUniqueName(scriptDirName, "grouperDdl", ".sql", true);
      GrouperUtil.saveStringIntoFile(scriptFile, resultString);

      String logMessage = "Ran this DDL:\n" + scriptFile.getAbsolutePath();
      if (LOG.isErrorEnabled()) {
        LOG.error(logMessage);
      } else {
        System.err.println(logMessage);
      }
      logMessage = "";

      PrintStream err = System.err;
      PrintStream out = System.out;
      InputStream in = System.in;

      //dont let ant mess up or close the streams
      ByteArrayOutputStream baosOutErr = new ByteArrayOutputStream();
      PrintStream newOutErr = new PrintStream(baosOutErr);

      System.setErr(newOutErr);
      System.setOut(newOutErr);
      
      SQLExec sqlExec = new SQLExec();
      
      sqlExec.setSrc(scriptFile);
      
      sqlExec.setDriver(grouperDb.getDriver());

      sqlExec.setUrl(grouperDb.getUrl());
      sqlExec.setUserid(grouperDb.getUser());
      sqlExec.setPassword(grouperDb.getPass());

      Project project = new GrouperAntProject();

      //tell output where to go
      DefaultLogger defaultLogger = new DefaultLogger();
      defaultLogger.setErrorPrintStream(newOutErr);
      defaultLogger.setOutputPrintStream(newOutErr);
      project.addBuildListener(defaultLogger);
      
      try {
        sqlExec.setProject(project);

        sqlExec.execute();

        logMessage += "Script was executed successfully\n";
      } catch (Exception e) {
        throw new RuntimeException("Error running script", e);
      } finally {
      
        newOutErr.flush();
        newOutErr.close();
        
        System.setErr(err);
        System.setOut(out);
        System.setIn(in);
      }
      
      String antOutput = StringUtils.trimToEmpty(baosOutErr.toString());

      if (!StringUtils.isBlank(antOutput)) {
        logMessage += antOutput + "\n";
      }
      //if call from command line, print to screen
      if (LOG.isErrorEnabled()) {
        LOG.error(logMessage);
      } else {
        System.out.println(logMessage);
      }
    } finally {
      insideBootstrap = false;
    }
    return resultString;
  }

  /**
   * drop all foreign keys (database dependent), and generate the script
   * @param dbMetadataBean 
   * @param ddlVersionBean
   */
  public static void dropAllForeignKeysScript(DbMetadataBean dbMetadataBean, DdlVersionBean ddlVersionBean) {
    
    Connection connection = ddlVersionBean.getConnection();
    
    Platform platform = ddlVersionBean.getPlatform();
    
    StringBuilder fullScript = ddlVersionBean.getFullScript();

    if (platform.getName().toLowerCase().contains("oracle")) {
      
      for (String type : new String[]{"R", "U"}) {
        
        //for oracle, get the foreign keys from user_constraints, and drop check constraints while we are at it
        String sql = "select TABLE_NAME, CONSTRAINT_NAME from user_constraints where table_name like ? " +
            "and constraint_type = '" + type + "' order by table_name, constraint_name"; 
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
          statement = connection.prepareStatement(sql);
          statement.setString(1, platform.getModelReader().getDefaultTablePattern());
          resultSet = statement.executeQuery();
          
          while (resultSet.next()) {
            //ALTER TABLE grouper_composites DROP constraint fk_composites_right_factor;
            String tableName = resultSet.getString("TABLE_NAME");
            String constraintName = resultSet.getString("CONSTRAINT_NAME");
            String dropStatement = "ALTER TABLE " + tableName 
              + " DROP constraint " + constraintName + ";\n";
            fullScript.append(dropStatement);
          }
          
        } catch (SQLException sqle) {
          throw new RuntimeException(sqle);
        } finally {
          GrouperUtil.closeQuietly(resultSet);
          GrouperUtil.closeQuietly(statement);
        }
      }
    } else if (platform.getName().toLowerCase().contains("postgres")) {
      
      for (String type : new String[]{"FOREIGN KEY", "UNIQUE"}) {
        
        //for postgres, get the foreign keys from information_schema, and drop check constraints while we are at it
        String sql = "SELECT * FROM information_schema.table_constraints where constraint_type = '" + type 
          + "' and lower(table_name) like ?";
          
        boolean hasSchema = !StringUtils.isBlank(dbMetadataBean.getSchema());
        if (hasSchema) {
          sql += " and lower(table_schema) like ? ";
        }
        boolean hasPlatform = !StringUtils.isBlank(platform.getModelReader().getDefaultCatalogPattern());
        if (hasPlatform) {
          sql += " and lower(table_catalog) like ? ";
        }
        
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
          statement = connection.prepareStatement(sql);
          statement.setString(1, StringUtils.lowerCase(platform.getModelReader().getDefaultTablePattern()));
          
          int currentIndex = 2;
          if (hasSchema) {
            statement.setString(currentIndex++, StringUtils.lowerCase(dbMetadataBean.getSchema()));
          }
          if (hasPlatform) {
            statement.setString(currentIndex++, StringUtils.lowerCase(platform.getModelReader().getDefaultCatalogPattern()));

          }
          
          String sqlLogMessage = "Constraint SQL: " + sql + ", params: " + platform.getModelReader().getDefaultTablePattern()
              + ", " + dbMetadataBean.getSchema()
              + ", " + platform.getModelReader().getDefaultCatalogPattern();
          LOG.info(sqlLogMessage);
          
          resultSet = statement.executeQuery();
          
          while (resultSet.next()) {
            //ALTER TABLE grouper_composites DROP constraint fk_composites_right_factor;
            String tableName = resultSet.getString("TABLE_NAME");
            String constraintName = resultSet.getString("CONSTRAINT_NAME");
            String dropStatement = "ALTER TABLE " + tableName 
              + " DROP constraint " + constraintName + ";\n";
            fullScript.append(dropStatement);
          }
          
        } catch (SQLException sqle) {
          throw new RuntimeException(sqle);
        } finally {
          GrouperUtil.closeQuietly(resultSet);
          GrouperUtil.closeQuietly(statement);
        }
      }

    } else {
      //just get from jdbc metadata
      SqlBuilder sqlBuilder = ddlVersionBean.getSqlBuilder();
      
      String objectName = ddlVersionBean.getObjectName();
      
      //it needs a name, just use "grouper"
      Database oldDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
          null, null);
      
      Database newDatabase = platform.readModelFromDatabase(connection, PLATFORM_NAME, null,
          null, null);
      dropAllForeignKeys(newDatabase);
  
      String script = convertChangesToString(objectName, sqlBuilder, oldDatabase, newDatabase);
    
      if (!StringUtils.isBlank(script)) {
        //result.append("\n-- first drop all foreign keys\n");
        fullScript.append(script).append("\n");
        //result.append("\n-- end drop all foreign keys\n\n");
      }
    }  
    
  }
  
  /**
   * @param objectName
   * @param sqlBuilder
   * @param oldDatabase
   * @param newDatabase
   * @return string
   */
  private static String convertChangesToString(String objectName, SqlBuilder sqlBuilder,
      Database oldDatabase, Database newDatabase) {
    String script;
    //upgrade to version: version
    //we need to upgrade from one version to another, but dont want to get the version from the DB, so 
    //call protected method via reflection
    StringWriter buffer = new StringWriter();
    sqlBuilder.setWriter(buffer);
    try {
      
      sqlBuilder.alterDatabase(oldDatabase, newDatabase, null);
    } catch (Exception e) {
      throw new RuntimeException("Problem with object name: " + objectName, e);
    }
    
    //GrouperUtil.callMethod(sqlBuilder.getClass(), sqlBuilder, "processTableStructureChanges",
    //  new Class[]{Database.class, Database.class, Table.class, Table.class, Map.class, List.class},
    //  new Object[]{oldDatabase, newDatabase, oldDatabase.findTable("grouper_ext_loader_log"), table, null, GrouperUtil.toList(addColumnChange)});

    script = buffer.toString();
    return script;
  }

  /**
   * remove all objects, the foreign keys, then the tables
   * @param database
   */
  public static void removeAllTables(Database database) {
    
    //delete all foreign keys
    for (Table table : GrouperUtil.nonNull(database.getTables(), Table.class)) {
      database.removeTable(table);
    }

  }
  
  /**
   * find history for a certain object name
   * @param objectName
   * @return the history or new stringbuilder if none available
   */
  public static StringBuilder retrieveHistory(String objectName) {
    retrieveDdlsFromCache();
    for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
      if (StringUtils.equals(objectName, hib3GrouperDdl.getObjectName())) {
        return hib3GrouperDdl.getHistory() == null ? new StringBuilder() 
          : new StringBuilder(hib3GrouperDdl.getHistory());
      }
    }
    return new StringBuilder();
  }
  
  /**
   * retrieve a version of a ddl object versionable
   * @param objectName
   * @param version
   * @return the ddl versionable
   */
  public static DdlVersionable retieveVersion(String objectName, int version) {
    
    String enumName = "V" + version;
    Class<Enum> enumClass = retrieveDdlEnum(objectName);
    
    try {
      return (DdlVersionable)Enum.valueOf(enumClass, enumName);
    } catch (Exception e) {
      throw new RuntimeException("Cant find version " + version + "(" + enumName 
          + ")  in objectName: " + objectName + ", " + enumClass.getName(), e);
    }
  }
  
  /** cache the ddls */
  private static List<Hib3GrouperDdl> cachedDdls = null;

  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlJavaVersion(String objectName) {
    
    Class<Enum> objectEnum = retrieveDdlEnum(objectName);
    
    //call currentVersion with reflection
    Integer currentVersion = (Integer)GrouperUtil.callMethod(objectEnum, "currentVersion");
    
    return currentVersion;
    
  }
  
  /**
   * the relationship between object name and the enum is as follows.  If the object name
   * is "Grouper", then the enum is edu.internet2.middleware.grouper.ddl.GrouperDdl
   * @param objectName
   * @return the enum
   */
  @SuppressWarnings("unchecked")
  public static Class<Enum> retrieveDdlEnum(String objectName) {
    return GrouperUtil.forName("edu.internet2.middleware.grouper.ddl." + objectName + "Ddl");
  }
  
  /**
   * get the cached ddls from the db and update the hibernate version if they are there
   */
  private static void retrieveDdlsFromCache() {
    //lazy load the cached ddls
    if (cachedDdls == null) {
      try {
        cachedDdls = retrieveDdlsFromDb();
        
        for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
          
          String objectName = hib3GrouperDdl.getObjectName();
          
          int ddlJavaVersion = retrieveDdlJavaVersion(objectName);
          
          LOG.info("Current java version for ddl '" + objectName + "' is " + ddlJavaVersion);
        }
      } catch (Exception e) {
        //just log, maybe the table isnt there
        LOG.error("maybe the grouper_ddl table isnt there... if that is the reason its ok.  " +
        		"info level logging will show underlying reason." + e.getMessage());
        //send this as info, since most of the time it isnt needed
        LOG.info("ddl issue: ", e);
      }
    
      cachedDdls = GrouperUtil.defaultIfNull(cachedDdls, new ArrayList<Hib3GrouperDdl>());
      
      //call hook so they can be removed or added
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_DDL_INIT, HooksLifecycleDdlInitBean.class, 
          (Object)cachedDdls, List.class, null);

      
    }
  }

  /**
   * 
   * @param ddlName
   * @return true if record exists
   */
  private static boolean containsDbRecord(String ddlName) {
    retrieveDdlsFromCache();
    for (Hib3GrouperDdl hib3GrouperDdl  : cachedDdls) {
      if (StringUtils.equals(hib3GrouperDdl.getObjectName(), ddlName)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * drop a view if it is detected as existing
   * @param ddlVersionBean
   * @param viewName
   */
  public static void ddlutilsDropViewIfExists(DdlVersionBean ddlVersionBean, String viewName) {
    boolean exists = assertTablesThere(false, false, viewName);
    if (!exists) {
      viewName = viewName.toUpperCase();
      //MCH 20090131 mysql can be case sensitive, and we moved to lower case view names,
      //so see if the upper case one if there...
      //at some point (grouper 1.6 or 1.7?) we can remove this
      exists = assertTablesThere(false, false, viewName);
    }
    if (exists) {
      if (ddlVersionBean.isPostgres()) {
        //GRP-459: postgres wont drop a view if other views depend on it
        //we need if exists since cascade might drop other dependent views
        ddlVersionBean.getFullScript().append("\nDROP VIEW IF EXISTS " + viewName + " cascade;\n");
      } else if (ddlVersionBean.isHsql()) {
        //GRP-459: hsql wont drop a view if other views depend on it
        //we need if exists since cascade might drop other dependent views
        ddlVersionBean.getFullScript().append("\nDROP VIEW " + viewName + " IF EXISTS cascade;\n");
      } else {
        ddlVersionBean.getFullScript().append("\nDROP VIEW " + viewName + ";\n");
      }
    }
    LOG.debug("View " + viewName + " exists? " + exists + ", will " + (exists ? "" : "not ") + "be dropped");
  }
  
  /**
   * backup a table into another table (which should not exist)
   * @param ddlVersionBean
   * @param tableName
   * @param backupTableName 
   */
  public static void ddlutilsBackupTable(DdlVersionBean ddlVersionBean, String tableName, 
      String backupTableName) {
    
    boolean backupThere = tableExists(backupTableName);
    if (backupThere) {
      throw new RuntimeException("Backup table already exists... something is wrong (if not, manually " +
      		"delete that table and try again): " + backupTableName);
    }
    
    String script = "\ncreate table " + backupTableName + " as (select * from " + tableName + ");\n";
    
    //not sure if this works on all dbs... it does work on mysql, oracle, and postgres
    ddlVersionBean.appendAdditionalScriptUnique(script);
  }
  
  /**
   * add a view if the DB supports it
   * @param ddlVersionBean
   * @param viewName
   * @param viewComment 
   * @param aliases
   * @param columnComments 
   * @param sql should not have a semicolon at end
   */
  public static void ddlutilsCreateOrReplaceView(DdlVersionBean ddlVersionBean, String viewName, 
      String viewComment, Set<String> aliases, Set<String> columnComments, String sql) {
    
    if (aliases.size() != columnComments.size()) {
      throw new RuntimeException("Alias size " + aliases.size() 
          + " doesnt match up with column comment size " + columnComments.size()
          + " for db view: " + viewName);
    }

    // views are required now
    // if (GrouperConfig.getPropertyBoolean("ddlutils.disableViews", false)) {
    //  return;
    // }
    
    //if this is postgres, we need to drop first because if the number of columns change, it bombs
    //if (ddlVersionBean.isPostgres()) {
    //  boolean exists = assertTablesThere(false, false, viewName);
    //  if (exists) {
    //    ddlVersionBean.appendAdditionalScriptUnique("\nDROP VIEW " + viewName + ";\n");
    //  }
    //  LOG.debug("Postgres, and view " + viewName + " exists? " + exists);
    //}
    String aliasesString = StringUtils.join(aliases.iterator(), ", ");
    
    String fullSql;
//    if (ddlVersionBean.isHsql()) {
      fullSql = "\nCREATE VIEW ";
//    } else {
//      // since views are being dropped before creation, maybe this should just be "CREATE VIEW" also...
//      fullSql = "\nCREATE OR REPLACE VIEW ";
//    }
    
    fullSql += viewName + " (" + aliasesString + ") AS " + sql + ";\n";
    if (ddlVersionBean.isSqlServer()) {
      //need this since sql server cant output more than one view in a script without it
      //http://www.tek-tips.com/viewthread.cfm?qid=1447087&page=14
      fullSql = "\ngo\n" + fullSql + "go\n\n";
    }
    ddlVersionBean.appendAdditionalScriptUnique(fullSql);

    ddlutilsViewComment(ddlVersionBean, viewName, viewComment);
    
    Iterator<String> aliasIterator = aliases.iterator();
    Iterator<String> columnCommentsIterator = columnComments.iterator();
    
    while (aliasIterator.hasNext() && columnCommentsIterator.hasNext()) {
      String alias = aliasIterator.next();
      String columnComment = columnCommentsIterator.next();
      ddlutilsColumnComment(ddlVersionBean, viewName, alias, columnComment);
    }
  }
  
  /**
   * add a table comment if the DB supports it
   * @param ddlVersionBean
   * @param tableName 
   * @param tableComment 
   */
  public static void ddlutilsTableComment(DdlVersionBean ddlVersionBean, String tableName, 
      String tableComment) {
    ddlutilsTableViewCommentHelper(ddlVersionBean, tableName, tableComment, true);
  }

  /**
   * add a view comment if the DB supports it
   * @param ddlVersionBean
   * @param viewName 
   * @param tableComment 
   */
  public static void ddlutilsViewComment(DdlVersionBean ddlVersionBean, String viewName, 
      String tableComment) {
    ddlutilsTableViewCommentHelper(ddlVersionBean, viewName, tableComment, false);
  }

  /**
   * <pre>
   * add a table or view column comment if the DB supports it
   *   COMMENT ON COLUMN zip_code.zip_code IS '5 Digit Zip Code';
   * </pre>
   * @param ddlVersionBean
   * @param objectName 
   * @param comment 
   * @param columnName 
   */
  public static void ddlutilsColumnComment(DdlVersionBean ddlVersionBean, String objectName, 
      String columnName,
      String comment) {
    
    if (GrouperConfig.getPropertyBoolean("ddlutils.disableComments", false)) {
      return;
    }

    if (StringUtils.isBlank(comment)) {
      LOG.warn("No comment for db column " + objectName + "." + columnName);
      return;
    }
    
    //only do this if oracle, or postgres
    boolean isOracle = ddlVersionBean.isOracle();
    boolean isPostgres = ddlVersionBean.isPostgres();
    if (isPostgres || isOracle) {
      
      //dont let a single quote mess up the sql...
      comment = StringUtils.replace(comment, "'", "^");
      
      String sql = null;
      
      sql = "\nCOMMENT ON COLUMN " + objectName + "." + columnName + " IS '" + comment + "';\n";

      ddlVersionBean.appendAdditionalScriptUnique(sql);
    }
  }
  
  /**
   * add a table or view comment if the DB supports it
   * @param ddlVersionBean
   * @param objectName 
   * @param comment 
   * @param tableNotView 
   */
  private static void ddlutilsTableViewCommentHelper(DdlVersionBean ddlVersionBean, String objectName, 
      String comment, boolean tableNotView) {
    
    if (GrouperConfig.getPropertyBoolean("ddlutils.disableComments", false)) {
      return;
    }
    
    if (StringUtils.isBlank(comment)) {
      LOG.debug("No comment for db object " + objectName);
      return;
    }
    
    //only do this if oracle, or postgres
    boolean isOracle = ddlVersionBean.isOracle();
    boolean isPostgres = ddlVersionBean.isPostgres();
    if (isPostgres || isOracle) {
      
      //dont let a single quote mess up the sql...
      comment = StringUtils.replace(comment, "'", "^");
      
      String sql = null;
      
      String objectString = "TABLE";
      if (!tableNotView && isPostgres) {
        objectString = "VIEW";
      }

      sql = "\nCOMMENT ON " + objectString + " " + objectName + " IS '" + comment + "';\n";

      ddlVersionBean.appendAdditionalScriptUnique(sql);
      
    }
    
  }
  
  
  /**
   * get the object names from the 
   * @return the list of object names
   */
  public static List<String> retrieveObjectNames() {
    //init stuff
    retrieveDdlsFromCache();
    
    List<String> objectNames = new ArrayList<String>();
    for (Hib3GrouperDdl hib3GrouperDdl : cachedDdls) {
      objectNames.add(hib3GrouperDdl.getObjectName());
    }
    
    //make sure Grouper is in there
    if (!objectNames.contains("Grouper")) {
      objectNames.add("Grouper");
    }
    if (!objectNames.contains("Subject")) {
      objectNames.add("Subject");
    }
    if (!objectNames.contains("GrouperOrg")) {
      objectNames.add("GrouperOrg");
    }
    if (GrouperConfig.getPropertyBoolean("ddlutils.exclude.subject.tables", false)) {
      objectNames.remove("Subject");
    }
    if (!GrouperConfig.getPropertyBoolean("orgs.includePocOrgsTablesInDdl", false)) {
      objectNames.remove("GrouperOrg");
    }
    
    return objectNames;
  }
  
  /** if check from scratch */
  public static boolean deepCheck = false;
  
  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlDbVersion(String objectName) {

    if (deepCheck) {
      return 0;
    }
    
    //init stuff
    retrieveDdlsFromCache();
    
    //find the ddl in the list
    Hib3GrouperDdl hib3GrouperDdl = Hib3GrouperDdl.findInList(cachedDdls, objectName);
    if (hib3GrouperDdl != null) {
      return hib3GrouperDdl.getDbVersion();
    }
    return 0;
    
  }
  
  /**
   * get all the ddls, put grouper at the front
   * @return the ddls
   */
  @SuppressWarnings("unchecked")
  public static List<Hib3GrouperDdl> retrieveDdlsFromDb() {

    List<Hib3GrouperDdl> grouperDdls = HibernateSession.byCriteriaStatic().list(Hib3GrouperDdl.class, null); 

    //move the grouper one to the front
    if (grouperDdls != null) {
      for (int i = 0; i < GrouperUtil.length(grouperDdls); i++) {
        Hib3GrouperDdl hib3GrouperDdl = grouperDdls.get(i);
        if (StringUtils.equals(hib3GrouperDdl.getObjectName(), "Grouper")) {
          grouperDdls.remove(i);
          grouperDdls.add(0, hib3GrouperDdl);
          break;
        }
      }
    }
    return grouperDdls;

  }

  /**
   * <pre>
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * The dbname must be a valid ddlutils dbname:
   * axion, cloudscape, db2, db2v8, derby, firebird, hsqldb, interbase, maxdb, mckoi, 
   * mssql, mysql, mysql5, oracle, oracle10, oracle9, postgresql, sapdb, sybase, sybasease15
   *
   * Also the following catchalls are acceptable: oracleall, mysqlall, db2all, sybaseall
   * </pre>
   * @param dbObjectVersion e.g. Grouper or GrouperLoader
   * @param dbname e.g. oracle10 or mysql5
   * @return the script or blank if it is not found
   */
  public static String findScriptOverride(DdlVersionable dbObjectVersion, String dbname) {
    String objectName = dbObjectVersion.getObjectName();
    int version = dbObjectVersion.getVersion();
    //lets see if there is a specific one:
    String script = findScriptOverride(objectName, version, dbname);
    if (StringUtils.isBlank(script)) {
      //now see if there is a general one...
      String generalName = null;
      //this is not an exact science...  but here is the algorithm
      if (dbname.startsWith("oracle")) {
        generalName = "oracleall";
      } else if (dbname.startsWith("mysql")) {
        generalName = "mysqlall";
      } else if (dbname.startsWith("db2")) {
        generalName = "db2all";
      } else if (dbname.startsWith("sybase")) {
        generalName = "sybaseall";
      }
      if (StringUtils.isNotBlank(generalName)) {
        script = findScriptOverride(objectName, version, generalName);
      }
    }
    return script;
  }

  /**
   * <pre>
   * get an override file (exact, dont look for the all ones like oracleall)
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * </pre>
   * @param objectName
   * @param version
   * @param dbNameExact
   * @return the script or null if none found
   */
  public static String findScriptOverride(String objectName, int version, String dbNameExact) {
    String resourceName = "/ddl/" + objectName + "." + version + "." + dbNameExact + ".sql";
    String script = null;
    script = GrouperUtil.readResourceIntoString(resourceName, true);
    return script;
  }
  
  /**
   * find a version from an enum version int
   * @param ddlVersion
   * @return the version
   */
  public static int versionIntFromEnum(Enum ddlVersion) {
    String name = ddlVersion.name();
    if (!name.startsWith("V")) {
      throw new RuntimeException("Version enums must start with V: " + name);
    }
    String version = name.substring(1);
    return GrouperUtil.intValue(version);

  }
  
  
  /**
   * find the object name from the db object version
   * @param dbObjectVersion
   * @return the object name
   */
  public static String objectName(Enum dbObjectVersion) {
    String className = dbObjectVersion.getDeclaringClass().getSimpleName();
    
    //now we have GrouperEnum, strip off the Enum part
    if (!className.endsWith("Ddl")) {
      throw new RuntimeException("Db object version classes MUST end in Ddl! '" + className + "'");
    }
    String objectName = className.substring(0, className.length()-3);
    return objectName;
  }
  
  /**
   * get a database object of a certain version based on the existing database, and tack on
   * all the enums up to the version we want (if any)
   * @param baseVersion
   * @param oldVersion old version if there is one, null if not
   * @param baseDatabaseVersion
   * @param objectName
   * @param requestedVersion
   * @param upgradeToVersion eventual upgrade version
   * @param additionalScripts 
   * @param fullScript so far
   * @param platform 
   * @param connection 
   * @param schema 
   * @param sqlBuilder 
   */
  public static void upgradeDatabaseVersion(Database baseVersion, Database oldVersion, int baseDatabaseVersion, 
      String objectName, int requestedVersion, int upgradeToVersion, StringBuilder additionalScripts, 
      StringBuilder fullScript, Platform platform, Connection connection, String schema, SqlBuilder sqlBuilder) {
    if (baseDatabaseVersion == requestedVersion) {
      return;
    }
    //loop up to the version we need
    for (int version = baseDatabaseVersion+1; version<=requestedVersion; version++) {
      //get the enum
      DdlVersionable ddlVersionable = retieveVersion(objectName, version);
      //do an incremental update
      DdlVersionBean ddlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, oldVersion, baseVersion, additionalScripts, 
          version == requestedVersion, upgradeToVersion, fullScript);
      ddlVersionable.updateVersionFromPrevious(baseVersion, ddlVersionBean);
    }
  }
  
  /**
   * find or create table
   * @param database
   * @param tableName
   * @return the table
   */
  public static Table ddlutilsFindOrCreateTable(Database database, String tableName) {
    Table table = database.findTable(tableName);
    if (table == null) {
      table = new Table();
      table.setName(tableName);
      database.addTable(table);
    }
    return table;
  }

  /**
   * add an index on a table.  drop a misnamed or a misuniqued index which is existing
   * @param database
   * @param tableName
   * @param indexName 
   * @param unique
   * @param columnNames
   * @return the index which is the new one, or existing one if it already exists, or null if a custom index
   */
  public static Index ddlutilsFindOrCreateIndex(Database database,  
      String tableName, String indexName, 
      boolean unique, String... columnNames) {
    return ddlutilsFindOrCreateIndex(database, null, tableName, indexName, null, unique, columnNames);
  }

  /**
   * add an index on a table.  drop a misnamed or a misuniqued index which is existing
   * @param database
   * @param ddlVersionBean can be null unless custom script
   * @param tableName
   * @param indexName 
   * @param customScript use this script to create the index, not ddlutils
   * @param unique
   * @param columnNames
   * @return the index which is the new one, or existing one if it already exists, or null if a custom index
   */
  public static Index ddlutilsFindOrCreateIndex(Database database, DdlVersionBean ddlVersionBean, 
      String tableName, String indexName, String customScript,
      boolean unique, String... columnNames) {
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName, true);

    //search for the index
    OUTERLOOP:
    for (Index existingIndex : table.getIndices()) {

      if (existingIndex.getColumnCount() == columnNames.length) {
        
        //no need to check if unique.  you dont want two of the same index, one unique, the other not
        //look through existing columns (order is important)
        //see if this is not a match
        for (int i=0;i<columnNames.length;i++) {
          if (!StringUtils.equalsIgnoreCase(existingIndex.getColumn(i).getName(), columnNames[i])) {
            
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same index!
        
        //if exactly the same, leave it be (dont rename if already there)
        if (unique == existingIndex.isUnique()) {
          return existingIndex;
        }
        
        table.removeIndex(existingIndex);
      }
    }
    
    //at this point, there should not be one of the same name in there
    for (Index existingIndex : table.getIndices()) {
      //if same name but not same, then get rid of it
      if (StringUtils.equalsIgnoreCase(existingIndex.getName(), indexName)) {
        table.removeIndex(existingIndex);
      }
    }
    
    if (!StringUtils.isBlank(customScript)) {
      //this better be there
      ddlVersionBean.appendAdditionalScriptUnique(customScript);
      return null;
    }
    
    //add this index with ddl utils
    Index index = unique ? new UniqueIndex() : new NonUniqueIndex();
    index.setName(indexName);

    for (String columnName : columnNames) {
      
      Column column = GrouperDdlUtils.ddlutilsFindColumn(table, columnName, true);
      IndexColumn nameColumn = new IndexColumn(column);
      index.addColumn(nameColumn);
      
    }
    
    table.addIndex(index);
    return index;
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnName
   * @param foreignColumnName 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, String localColumnName, String foreignColumnName) {
    return ddlutilsFindOrCreateForeignKey(database, tableName, foreignKeyName, foreignTableName, 
        GrouperUtil.toList(localColumnName), GrouperUtil.toList(foreignColumnName));
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnNames 
   * @param foreignColumnNames 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, List<String> localColumnNames, List<String> foreignColumnNames) {
    
    //validate inputs
    if (localColumnNames.size() != foreignColumnNames.size()) {
      throw new RuntimeException("Local col size must equal foreign col size: " 
          + localColumnNames.size() + " != " + foreignColumnNames.size());
    }
    
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName, true);
    Table foreignTable = GrouperDdlUtils.ddlutilsFindTable(database,foreignTableName, true);
    
    //search for the foreign key
    OUTERLOOP:
    for (ForeignKey foreignKey : table.getForeignKeys()) {
      if (foreignKey.getReferences().length == localColumnNames.size()) {
        for (int i=0;i<localColumnNames.size();i++) {

          Reference reference = foreignKey.getReferences()[i];
          
          //if this isnt a match
          if (!StringUtils.equalsIgnoreCase(reference.getForeignColumnName(), foreignColumnNames.get(i))
              || !StringUtils.equalsIgnoreCase(reference.getLocalColumnName(), localColumnNames.get(i))) {
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same foreign key!
        
        //if exactly the same, leave it be
        if (StringUtils.equalsIgnoreCase(foreignKeyName, foreignKey.getName())) {
          return foreignKey;
        }
        
        table.removeForeignKey(foreignKey);
      }
    }
    
    ForeignKey foreignKey = new ForeignKey(foreignKeyName);
    foreignKey.setForeignTableName(foreignTableName);
    
    for (int i=0;i<localColumnNames.size();i++) {
      
      Column localColumn = GrouperDdlUtils.ddlutilsFindColumn(table, localColumnNames.get(i), true);
      Column foreignColumn = GrouperDdlUtils.ddlutilsFindColumn(foreignTable, foreignColumnNames.get(i), true);
      
      Reference reference = new Reference(localColumn, foreignColumn);
      
      foreignKey.addReference(reference);
    }
    
    table.addForeignKey(foreignKey);
    return foreignKey;
  }
  
  /**
   * drop all foreign keys from a ddlutils database object
   * @param database
   */
  public static void dropAllForeignKeys(Database database) {
    for (Table table : GrouperUtil.nonNull(database.getTables(), Table.class)) {
      for (ForeignKey foreignKey : GrouperUtil.nonNull(table.getForeignKeys(), ForeignKey.class)) {
        table.removeForeignKey(foreignKey);
      }
    }
  }
  
  /**
   * find table, if not exist, throw exception
   * @param database
   * @param tableName
   * @param exceptionOnNotFound 
   * @return the table
   */
  public static Table ddlutilsFindTable(Database database, String tableName, boolean exceptionOnNotFound) {
    Table table = database.findTable(tableName);
    if (table == null && exceptionOnNotFound) {
      throw new RuntimeException("Cant find table: '" + tableName 
          + "', perhaps you need to rollback your ddl version in the DB and sync up");
    }
    return table;
  }
  
  /**
   * find table, if not exist, throw exception
   * @param database
   * @param tableName
   * @param columnName 
   * @param exceptionIfNotFound 
   * @return the table
   */
  public static Column ddlutilsFindColumn(Database database, String tableName, 
      String columnName, boolean exceptionIfNotFound) {
    
    Table table = database.findTable(tableName);
    if (table == null) {
      if (exceptionIfNotFound) {
        throw new RuntimeException("Cant find table: '" + tableName 
            + "', perhaps you need to rollback your ddl version in the DB and sync up");
      }
      return null;
    }
    Column column = table.findColumn(columnName);
    if (column == null) {
      if (exceptionIfNotFound) {
        throw new RuntimeException("Cant find column '" + columnName + "' in table '" + tableName + "'," +
        		" perhaps you need to rollback your ddl version in the DB and sync up");
      }
      return null;
    }
    return column;
  }

  /**
   * find column, if not exist, throw exception
   * @param table table to get column from
   * @param columnName column name of column (case insensitive)
   * @param exceptionOnNotFound 
   * @return the column
   */
  public static Column ddlutilsFindColumn(Table table, String columnName, boolean exceptionOnNotFound) {
    Column[] columns = table.getColumns();
    
    for (Column column : GrouperUtil.nonNull(columns, Column.class)) {
      
      if (StringUtils.equalsIgnoreCase(columnName, column.getName())) {
        return column;
      }
    }
    if (exceptionOnNotFound) {
      throw new RuntimeException("Cant find table: '" + table.getName() 
          + "' columns: '" + columnName + "', perhaps you need to rollback your ddl version in the DB and sync up");
    }
    return null;
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @return the column
   */
  public static Column ddlutilsFindOrCreateColumn(Table table, String columnName, 
      int typeCode, String size, boolean primaryKey, boolean required) {
    return ddlutilsFindOrCreateColumn(table, columnName, 
        typeCode, size, primaryKey, required, null);
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @param defaultValue is null for none, or something for default value
   * @return the column
   */
  public static Column ddlutilsFindOrCreateColumn(Table table, String columnName, 
      int typeCode, String size, boolean primaryKey, boolean required, String defaultValue) {

    Column column = table.findColumn(columnName);
    
    if (column == null) {
      column = new Column();
      column.setName(columnName);
      //just add to end of columns
      table.addColumn(column);
      
      //eave this stuff in here so it doesnt mess up existing columns and drop/create tables
      column.setRequired(required);
      column.setTypeCode(typeCode);
      if (!StringUtils.equals(size, column.getSize())) {
        column.setSize(size);
      }
      if (defaultValue != null) {
        column.setDefaultValue(defaultValue);
      }
    }

    column.setPrimaryKey(primaryKey);

    return column;
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @return the column
   */
  public static Column ddlutilsFixSizeColumn(Table table, String columnName, 
      int typeCode, String size, boolean primaryKey, boolean required) {
    return ddlutilsFixSizeColumn(table, columnName, typeCode, size, primaryKey, required, null);
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @param defaultValue is null for none, or something for default value
   * @return the column
   */
  public static Column ddlutilsFixSizeColumn(Table table, String columnName, 
      int typeCode, String size, boolean primaryKey, boolean required, String defaultValue) {

    Column column = table.findColumn(columnName);
    
    if (column == null) {
      column = new Column();
      column.setName(columnName);
      //just add to end of columns
      table.addColumn(column);
      column.setRequired(required);
    }
    
    column.setTypeCode(typeCode);
    if (!StringUtils.equals(size, column.getSize())) {
      column.setSize(size);
    }
    if (defaultValue != null) {
      column.setDefaultValue(defaultValue);
    }

    column.setPrimaryKey(primaryKey);

    return column;
  }
  
  /**
   * find and drop a column if it is there.   If table not there, thats ok
   * also drop all related indexes
   * @param database
   * @param tableName
   * @param ddlVersionBean 
   * @param columnName 
   */
  public static void ddlutilsDropColumn(Database database, String tableName, String columnName, DdlVersionBean ddlVersionBean) {
    Table table = database.findTable(tableName);
    if (table != null) {
      ddlutilsDropColumn(table, columnName, ddlVersionBean);
    }
  }

  /**
   * find and drop a table if it is there
   * @param ddlVersionBean 
   * @param tableName
   */
  public static void ddlutilsDropTable(DdlVersionBean ddlVersionBean, String tableName) {
    if (GrouperDdlUtils.assertTablesThere(false, false, tableName)) {
      if (ddlVersionBean.isOracle()) {
        ddlVersionBean.appendAdditionalScriptUnique("\ndrop table " + tableName + " cascade constraints;\n");
      } else {
        //this is tested for postgres or mysql, and might work with other db's, who knows
        ddlVersionBean.appendAdditionalScriptUnique("\ndrop table " + tableName + " cascade;\n");
      }
    }
  }

  /**
   * find and drop a column if it is there
   * also drop all related indexes
   * @param table 
   * @param columnName 
   * @param ddlVersionBean 
   */
  public static void ddlutilsDropColumn(Table table, String columnName, DdlVersionBean ddlVersionBean) {
    ddlutilsDropIndexes(table, columnName);
    
    Column column = table.findColumn(columnName);
    
    if (column != null) {
      table.removeColumn(column);
      
      //if we dont move this column to the end of the old table, then ddlutils will think we are moving it
      Database oldDatabase = ddlVersionBean.getOldDatabase();
      Table oldTable = oldDatabase == null ? null : oldDatabase.findTable(table.getName());
      Column oldColumn = oldTable == null ? null : oldTable.findColumn(columnName);
      if (oldColumn != null) {
        //move to end
        oldTable.removeColumn(oldColumn);
        oldTable.addColumn(oldColumn);
      }
    }

  }
  
  /**
   * drop all indexes by column name (e.g. if removing column)
   * @param table 
   * @param columnName 
   */
  public static void ddlutilsDropIndexes(Table table, String columnName) {
    if (table == null) {
      return;
    }
    for (Index index: table.getIndices()) {
      for (IndexColumn indexColumn : index.getColumns()) {
        if (StringUtils.equalsIgnoreCase(columnName, indexColumn.getName())) {
          table.removeIndex(index);
          break;
        }
      }
    }
  }
  
  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectRecords 
   * @param expectTrue pritn exception if expecting true
   * @return true if expect records, and records there.  false if records not there.  exception
   * if exception is thrown and expect true.false if exception and not expect true
   */
  public static boolean assertTablesThere(boolean expectRecords, boolean expectTrue) {
    return assertTablesThere(expectRecords, expectTrue, "grouper_stems");
  }

  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectRecords 
   * @param expectTrue pritn exception if expecting true
   * @param tableName 
   * @return true if expect records, and records there.  false if records not there.  exception
   * if exception is thrown and expect true.false if exception and not expect true
   */
  public static boolean assertTablesThere(boolean expectRecords, boolean expectTrue, String tableName) {
    try {
      //first, see if tables are there
      int count = HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from " + tableName);
      if (!expectRecords) {
        return true;
      }
      return count > 0;
    } catch (RuntimeException e) {
      if (expectTrue) {
        throw e;
      }
      return false;
    }
  
  }

  /**
   * see if tables are there (at least the grouper groups one)
   * @param expectTrue throw exception if expecting true and not there or vice versa
   * @param tableName 
   * @param columnName 
   * @return true if everything ok, false if not
   */
  public static boolean assertColumnThere(boolean expectTrue, String tableName, String columnName) {
    try {
      //first, see if column are there
      HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from " + tableName + " where " + columnName + " is null");
      if (expectTrue) {
        return true;
      }
      return false;
    } catch (RuntimeException e) {
      if (expectTrue) {
        return false;
      }
      return true;
    }
  
  }

  /**
   * see if tables are there (at least the grouper groups one)
   * @param tableName 
   * @return true if everything ok, false if not
   */
  public static boolean tableExists(String tableName) {
    try {
      //first, see if tables are there
      HibernateSession.bySqlStatic().select(int.class, 
          "select count(*) from " + tableName);
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  
  }
  /**
   * find the correct metadata for the DB
   * @param ddlVersionable
   * @return the metadatabean
   */
  public static GrouperDdlUtils.DbMetadataBean findDbMetadataBean(DdlVersionable ddlVersionable) {
    final GrouperDdlUtils.DbMetadataBean dbMetadataBean = new GrouperDdlUtils.DbMetadataBean();

    //convenience to get the url, user, etc of the grouper db, helps get db connection
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
    
    String schema = grouperDb.getUser().toUpperCase();
  
  //    if (true) {
  //      dbMetadataBean.setDefaultTablePattern(ddlVersionable.getDefaultTablePattern());
  //      dbMetadataBean.setSchema(schema);
  //      return dbMetadataBean;
  //    }

    //see if the table is there
    final String[] sampleTablenames = ddlVersionable.getSampleTablenames();
    boolean tableThere = false;
    String sampleTableNameExists = null;
    for (String sampleTableName : sampleTablenames) {
      tableThere = assertTablesThere(false, false, sampleTableName);
      if (tableThere) {
        sampleTableNameExists = sampleTableName;
        break;
      }
    }
    
    //pattern to get only certain objects (e.g. GROUPERLOADER% )
    String defaultTablePattern = ddlVersionable.getDefaultTablePattern(); 
    
    Platform platform = retrievePlatform(false);
  
    String ddlUtilsSchemaOverride = StringUtils.trimToEmpty(GrouperConfig.getProperty("ddlutils.schema"));
  
    //in postgres, try public
    String extraSchema = "";
    
    //postgres needs lower I think
    if (platform.getName().toLowerCase().contains("postgre")) {
      schema = schema.toLowerCase();
      defaultTablePattern = defaultTablePattern.toLowerCase();
      extraSchema = "public";
    }
  
    if (platform.getName().toLowerCase().contains("mssql")) {
      extraSchema = "dbo";
    }
  
    final boolean isHsqldb = platform.getName().toLowerCase().contains("hsql");
    //seems like this is best...
    if (isHsqldb) {
      extraSchema = null;
    }
    
    if (!tableThere) {
      
      if (isHsqldb) {
        schema = null;
      }
      if (!StringUtils.isBlank(ddlUtilsSchemaOverride)) {
        schema = ddlUtilsSchemaOverride;
      } 
      dbMetadataBean.setSchema(schema);
      dbMetadataBean.setDefaultTablePattern(defaultTablePattern);
      
    } else {
      
      //lets do some trial and error to see what the values should be (since case sensitive)
      final Set<String> defaultTablePatterns = GrouperUtil.toSet(defaultTablePattern, defaultTablePattern.toLowerCase(), 
          defaultTablePattern.toUpperCase());
  
      final Set<String> schemas = GrouperUtil.toSet(
          ddlUtilsSchemaOverride, ddlUtilsSchemaOverride.toLowerCase(), ddlUtilsSchemaOverride.toUpperCase(),
          schema, StringUtils.lowerCase(schema), StringUtils.upperCase(schema), 
          extraSchema, StringUtils.lowerCase(extraSchema), StringUtils.lowerCase(extraSchema));
      
      final String sampleTableNameExistsFinal = sampleTableNameExists;
      
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
        @SuppressWarnings("deprecation")
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
          //try all combinations
          for (String theDefaultTablePattern : defaultTablePatterns) {
            for (String theSchema : schemas) {
              boolean hsqlSchemaOk = theSchema == null && isHsqldb;
              if (StringUtils.isBlank(theDefaultTablePattern) || (isHsqldb && !hsqlSchemaOk)) {
                continue;
              }
              Connection connection = hibernateSession.getSession().connection();
              
              DatabaseMetaData databaseMetaData = null;
              ResultSet fkData = null;
  
              try {
                databaseMetaData = connection.getMetaData();
                fkData = databaseMetaData.getTables(null, theSchema, theDefaultTablePattern, null);
                ResultSetMetaData resultSetMetaData = fkData.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                @SuppressWarnings("unused")
                int fk = 0;
                while (fkData.next()) {
                  //System.out.println(fk++ + ": ");
  
                  for (int i = 1; i <= columnCount; i++) {
                    //System.out.println("  " + resultSetMetaData.getColumnName(i) + ": "
                      //  + fkData.getString(i));
                    if (StringUtils.equalsIgnoreCase("TABLE_NAME", resultSetMetaData.getColumnName(i))) {
                      if (StringUtils.equalsIgnoreCase(sampleTableNameExistsFinal, fkData.getString(i))) {
                        //we found it!
                        dbMetadataBean.setSchema(theSchema);
                        dbMetadataBean.setDefaultTablePattern(theDefaultTablePattern);
                        return null;
                      }
                    }
                  }
                }
              } catch (Exception e) {
                throw new RuntimeException("Problem with db connection", e);
              } finally {
                GrouperUtil.closeQuietly(fkData);
                if (hibernateSession.isTransactionActive()) {
                  hibernateSession.rollback(GrouperRollbackType.ROLLBACK_NOW);
                }
              }
            }
          }
          //it never found the connection criteria!
          throw new RuntimeException("The table: '" + sampleTableNameExistsFinal + "' exists, but " +
              "cant find it with DB metadata... is the ddlutils.schema set correctly");
        }
        
      });
    }
    return dbMetadataBean;
    
  }
  
  /**
   * Returns the sql to concatenate these two fields separated by the separator
   * @param field1
   * @param field2
   * @param separator
   * @return sql for concatenation
   */
  public static String sqlConcatenation(String field1, String field2, String separator) {
    if (GrouperDdlUtils.isSQLServer()) {
      return field1 + " + '" + separator + "' + " + field2;
    } else if (GrouperDdlUtils.isMysql()) {
      return "concat(" + field1 + ", '" + separator + "', " + field2 + ")";
    } else {
      // this should work for Oracle, Hsql, and Postgres
      return field1 + " || '" + separator + "' || " + field2;
    }
  }
  
  /**
   * Returns the sql to concatenate these two fields separated by the separator
   * @param field1
   * @param field2
   * @return sql for concatenation
   */
  public static String sqlConcatenation(String field1, String field2) {
    if (GrouperDdlUtils.isSQLServer()) {
      return field1 + " + " + field2;
    } else if (GrouperDdlUtils.isMysql()) {
      return "concat(" + field1 + "," + field2 + ")";
    } else {
      // this should work for Oracle, Hsql, and Postgres
      return field1 + " || " + field2;
    }
  }

  /**
   * Get the number of records in a table
   * @param database
   * @param tableName
   * @param exceptionIfTableDoesNotExist
   * @return count
   */
  public static int getTableCount(Database database, String tableName, boolean exceptionIfTableDoesNotExist) {
    try {
      return HibernateSession.bySqlStatic().select(int.class, "select count(*) from " + tableName);
    } catch (RuntimeException e) {
      if (exceptionIfTableDoesNotExist) {
        throw e;
      }
      
      return 0;
    }
  }
}
