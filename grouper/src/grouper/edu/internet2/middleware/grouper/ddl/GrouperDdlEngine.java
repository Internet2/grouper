package edu.internet2.middleware.grouper.ddl;

import java.io.File;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.SqlBuilder;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.gsh.GrouperShell;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdlWorker;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeFinder;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils.DbMetadataBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.registry.RegistryInstall;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * runs ddl updates
 * @author mchyzer
 *
 */
public class GrouperDdlEngine {

  private boolean callFromCommandLine;
  
  public GrouperDdlEngine assignCallFromCommandLine(boolean callFromCommandLine1) {
    this.callFromCommandLine = callFromCommandLine1;
    return this;
  }

  /**
   * true if just testing this method
   */
  private boolean fromUnitTest;

  /**
   * true if just testing this method
   */
  public GrouperDdlEngine assignFromUnitTest(boolean fromUnitTest1) {
    fromUnitTest = fromUnitTest1;
    return this;
  }
  
  private boolean compareFromDbVersion; 

  public GrouperDdlEngine assignCompareFromDbVersion(boolean theCompareFromDbVersion1) {
    compareFromDbVersion = theCompareFromDbVersion1;
    return this;
  }

  private boolean dropBeforeCreate;

  public GrouperDdlEngine assignDropBeforeCreate(boolean theDropBeforeCreate) {
    dropBeforeCreate = theDropBeforeCreate;
    return this;
  }

  private boolean writeAndRunScript;

  public GrouperDdlEngine assignWriteAndRunScript(boolean theWriteAndRunScript) {
    writeAndRunScript = theWriteAndRunScript;
    return this;
  }

  /**
   * just drop stuff, e.g. for unit test
   */
  private boolean dropOnly;

  /**
   * just drop stuff, e.g. for unit test
   */
  public GrouperDdlEngine assignDropOnly(boolean dropOnly1) {
    dropOnly = dropOnly1;
    return this;
  }

  /**
   * if registry install should be called afterwards
   */
  private boolean installDefaultGrouperData;

  /**
   * if registry install should be called afterwards
   */
  public GrouperDdlEngine assignInstallDefaultGrouperData(boolean installDefaultGrouperData1) {
    installDefaultGrouperData = installDefaultGrouperData1;
    return this;
  }

  /**
   * if unit testing, and not going to max, then associate object name with max version
   */
  private Map<String, DdlVersionable> maxVersions;

  /**
   * if unit testing, and not going to max, then associate object name with max version
   */
  public GrouperDdlEngine assignMaxVersions(Map<String, DdlVersionable> maxVersions1) {
    maxVersions = maxVersions1;
    return this;
  }

  /**
   * promptUser to see if they want to do this... if they havent been prompted already and if not configured not to prompt
   */
  private boolean promptUser; 

  /**
   * promptUser to see if they want to do this... if they havent been prompted already and if not configured not to prompt
   */
  public GrouperDdlEngine assignPromptUser(boolean promptUser1) {
    promptUser = promptUser1;
    return this;
  }

  /**
   * if being called when grouper starts up to check DDL
   */
  private boolean fromStartup;
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdlEngine.class);

  /**
   * if being called when grouper starts up to check DDL
   * @param fromStartup1
   * @return this for chaining
   */
  public GrouperDdlEngine assignFromStartup(boolean fromStartup1) {
    fromStartup = fromStartup1;
    return this;
  }
  
  /** if everything is the right version */
  public static boolean everythingRightVersion = true;

  private Thread heartbeatThread = null;
  
  boolean done = false;
  
  private String thisDdlDatabaseLockingUuid;
  
  private Boolean runDdlForObjectName(String objectName, Connection connection, String schema, Platform platform, StringBuilder result) {

    if (StringUtils.equals("GrouperLoader", objectName)) {
      LOG.warn("GrouperLoader should not be in the Grouper_ddl table, deleting");
      HibernateSession.bySqlStatic().executeSql("delete from grouper_ddl where object_name = 'GrouperLoader'");
      return null;
    }
    
    Class<Enum> objectEnumClass = null;
    
    try {
      objectEnumClass = GrouperDdlUtils.retrieveDdlEnum(objectName);
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
    int javaVersion = GrouperDdlUtils.retrieveDdlJavaVersion(objectName); 
    
    //maybe override this if unit testing
    if (maxVersions!=null && maxVersions.containsKey(objectName)) {
      javaVersion = maxVersions.get(objectName).getVersion();
    }
    
    DdlVersionable ddlVersionableJava = GrouperDdlUtils.retieveVersion(objectName, javaVersion);
    
    StringBuilder historyBuilder = GrouperDdlUtils.retrieveHistory(objectName);
    
    //this is the version in the db
    int realDbVersion = GrouperDdlUtils.retrieveDdlDbVersion(objectName);
    
    String versionStatus = null;
    GrouperVersion grouperVersionDatabase = null;
    GrouperVersion grouperVersionJava = new GrouperVersion(ddlVersionableJava.getGrouperVersion());
    {
      DdlVersionable dbDdlVersionable = GrouperDdlUtils.retieveVersion(objectName, realDbVersion);
      grouperVersionDatabase = dbDdlVersionable == null ? null : new GrouperVersion(dbDdlVersionable.getGrouperVersion());
      versionStatus = "Grouper ddl object type '" + objectName + "' has dbVersion: " 
        + realDbVersion + " (" + grouperVersionDatabase + ") and java version: " + javaVersion + " (" + grouperVersionJava + ")";
    }          
    boolean versionMismatch = javaVersion != realDbVersion;

    boolean okIfSameMajorAndMinorVersion = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("registry.auto.ddl.okIfSameMajorAndMinorVersion", true);
    boolean sameMajorAndMinorVersion = grouperVersionDatabase == null ? false : grouperVersionDatabase.sameMajorMinorArg(grouperVersionJava);

    if (versionMismatch && okIfSameMajorAndMinorVersion && sameMajorAndMinorVersion) {
      versionMismatch = false;
    }
    
    if (versionMismatch) {
      if (GrouperDdlUtils.internal_printDdlUpdateMessage) {
        System.err.println(versionStatus);
        LOG.error(versionStatus);
      }
    } else {
      LOG.warn(versionStatus);
    }
    
    //this is the logic version in the objects
    int dbVersion = realDbVersion;

    if (!compareFromDbVersion || dropBeforeCreate) {
      //if going from nothing, then go from nothing
      dbVersion = 0;
    }
    
    //reset to take into account if starting from scratch
    versionMismatch = javaVersion != dbVersion;
    
    //see if same version, just continue, all good
    if (!versionMismatch && !dropOnly) {
      return null;
    }
    
    //if the java is less than db, then grouper was rolled back... that might not be good
    if (javaVersion < dbVersion && !dropOnly && sameMajorAndMinorVersion && okIfSameMajorAndMinorVersion) {
      LOG.warn("Java version of db object name: " + objectName + " is " 
          + javaVersion + " (" + grouperVersionJava + ") which is less than the dbVersion " + dbVersion
          + " (" + grouperVersionDatabase + ").  This is probably ok, another JVM has a slightly higher version.");
      return null;
    }
    
    //if the java is less than db, then grouper was rolled back... that might not be good
    if (javaVersion < dbVersion && !dropOnly) {
      LOG.error("Java version of db object name: " + objectName + " is " 
          + javaVersion + " (" + grouperVersionJava + ") which is less than the dbVersion " + dbVersion
          + " (" + grouperVersionDatabase + ").  This means grouper was upgraded and rolled back?  Check in the enum "
          + objectEnumClass.getName() + " for details on if things are compatible.");
      //not much we can do here... good luck!
      return null;
    }
    
    if (!dropOnly && !dropBeforeCreate ) {
      
      Boolean hasResult = checkIfChangeLogEmptyRequired(objectName, javaVersion, realDbVersion);
      if (hasResult != null) {
        return hasResult;
      }
    }

    //shut down hibernate if not just testing
    if (!fromUnitTest) {
      everythingRightVersion = false;
    }

    // lets lock until we can make changes
    if (fromStartup) {
      writeAndRunScript = writeAndRunScript || GrouperDdlUtils.autoDdlFor(grouperVersionJava);
      if (writeAndRunScript) {
        
        Boolean hasResult = waitForOtherJvmsOrLockInDatabase();
        if (hasResult != null) {
          return hasResult;
        }
      }
    }
    
    DbMetadataBean dbMetadataBean = GrouperDdlUtils.findDbMetadataBean(ddlVersionableJava);
    
    //to be safe lets only deal with tables related to this object
    platform.getModelReader().setDefaultTablePattern(dbMetadataBean.getDefaultTablePattern());
    //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
    platform.getModelReader().setDefaultSchemaPattern(dbMetadataBean.getSchema());
      
    SqlBuilder sqlBuilder = platform.getSqlBuilder();
    
    boolean recreateViewsAndForeignKeys = dropViewsAndForeignKeysIfNeeded(objectName,
        connection, schema, platform, result, javaVersion, ddlVersionableJava, dbVersion,
        dbMetadataBean, sqlBuilder);
    
    dropEverythingIfNeeded(objectName, connection, platform, result, sqlBuilder);
    
    if (!dropOnly) {
      //the db version is less than the java version
      //lets go up one version at a time until we are current
//        for (int version = dbVersion+1; version<=javaVersion;version++) {

//        ddlVersionable = GrouperDdlUtils.retieveVersion(objectName, version);
//        //we just want a script, see if one exists for this version
//        String script = findScriptOverride(ddlVersionable, dbname);
//        
//        //if there was no override
//        if (StringUtils.isBlank(script)) {
          
      //it needs a name, just use "grouper"
      Database oldDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
          null, null);
      GrouperDdlUtils.dropAllForeignKeys(oldDatabase);
      
      Database newDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
          null, null);
      GrouperDdlUtils.dropAllForeignKeys(newDatabase);
      
      if (dropBeforeCreate) {
        GrouperDdlUtils.removeAllTables(oldDatabase);
        GrouperDdlUtils.removeAllTables(newDatabase);
      }
      
      //get this to the previous version, dont worry about additional scripts
      GrouperDdlUtils.upgradeDatabaseVersion(oldDatabase, null, 0, objectName, dbVersion, 
          new StringBuilder(), result, platform, connection, schema, sqlBuilder);
      
      StringBuilder additionalScripts = new StringBuilder();
      
      //get this to the current version
      GrouperDdlUtils.upgradeDatabaseVersion(newDatabase, oldDatabase, dbVersion, objectName, javaVersion, 
          additionalScripts, result, platform, connection, schema, sqlBuilder);
      
      if (recreateViewsAndForeignKeys) {
        //now we need to add the foreign keys back in
        //just get the first version since we need an instance, any instance
        addViewsAndForeignKeysIfNeeded(objectName,
            connection, schema, platform, result, javaVersion, ddlVersionableJava, dbVersion,
            dbMetadataBean, sqlBuilder, oldDatabase, newDatabase, additionalScripts);
      }

      String script = GrouperDdlUtils.convertChangesToString(objectName, sqlBuilder, oldDatabase,
          newDatabase);
      
      script = StringUtils.trimToEmpty(script);
      
      String additionalScriptsString = additionalScripts.toString();
      if (!StringUtils.isBlank(additionalScriptsString)) {
        script += "\n" + additionalScriptsString;
      }
      
      //String ddl = platform.getAlterTablesSql(connection, database);

      //make sure no single quotes in any of these...
      String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
      //is this db independent?  if not, figure out what the issues are and fix so we can have comments
      String summary = timestamp + ": upgrade " + objectName + " from V" + (dbVersion) + " to V" + javaVersion;
      
      boolean scriptNotBlank = !StringUtils.isBlank(script);
      //dont do this if shouldnt
      boolean upgradeDdlTable = dbVersion == 0 || dropBeforeCreate;

      if (scriptNotBlank || upgradeDdlTable) {
        //result.append("\n-- " + summary + " \n");
      }
      
      if (scriptNotBlank) {
        result.append(script).append("\n\n");
      }

      addGrouperDdlLogEntryIfNeeded(objectName, result, historyBuilder, javaVersion,
          timestamp, summary, upgradeDdlTable);
      
    }            
    return null;
  }

  private void addViewsAndForeignKeysIfNeeded(String objectName, Connection connection,
      String schema, Platform platform, StringBuilder result, int javaVersion,
      DdlVersionable ddlVersionableJava, int dbVersion, DbMetadataBean dbMetadataBean,
      SqlBuilder sqlBuilder, Database oldDatabase, Database newDatabase, StringBuilder additionalScripts) {
    
    //drop all views since postgres will drop view cascade (and we dont know about it), and cant create or replace with changes
    DdlVersionBean tempDdlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, oldDatabase, newDatabase, additionalScripts, true, javaVersion, result, 0);
    GrouperDdlUtils.ddlVersionBeanThreadLocalAssign(tempDdlVersionBean);
    try {
      ddlVersionableJava.addAllForeignKeysViewsEtc(tempDdlVersionBean);

    } finally {
      GrouperDdlUtils.ddlVersionBeanThreadLocalClear();
    }

  }

  private void addGrouperDdlLogEntryIfNeeded(String objectName, StringBuilder result,
      StringBuilder historyBuilder, int javaVersion, String timestamp, String summary,
      boolean upgradeDdlTable) {
    historyBuilder.insert(0, summary + ", ");
  
    String historyString = StringUtils.abbreviate(historyBuilder.toString(), 3800);
    
    //see if already in db
    if (upgradeDdlTable || ((!GrouperDdlUtils.containsDbRecord(objectName) || dropBeforeCreate) 
        && !GrouperDdlUtils.alreadyInsertedForObjectName.contains(objectName))) {
    
      result.append("\ninsert into grouper_ddl (id, object_name, db_version, " +
          "last_updated, history) values ('" + GrouperUuid.getUuid() 
          +  "', '" + objectName + "', " + javaVersion + ", '" + timestamp + "', \n'" + historyString + "');\n");
      //dont insert again for this object
      GrouperDdlUtils.alreadyInsertedForObjectName.add(objectName);

    } else {
      
      result.append("\nupdate grouper_ddl set db_version = " + javaVersion
          + ", last_updated = '" + timestamp + "', \nhistory = '" + historyString 
          + "' where object_name = '" + objectName + "';\n");

    }
    result.append("commit;\n\n");
  }

  private void dropEverythingIfNeeded(String objectName, Connection connection,
      Platform platform, StringBuilder result, SqlBuilder sqlBuilder) {
    // if deleting all, lets delete all:
    if (dropBeforeCreate || dropOnly) {
      //it needs a name, just use "grouper"
      Database oldDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
          null, null);
      GrouperDdlUtils.dropAllForeignKeys(oldDatabase);
      
      Database newDatabase = platform.readModelFromDatabase(connection, GrouperDdlUtils.PLATFORM_NAME, null,
          null, null);
      GrouperDdlUtils.dropAllForeignKeys(newDatabase);

      GrouperDdlUtils.removeAllTables(newDatabase);
      
      String script = GrouperDdlUtils.convertChangesToString(objectName, sqlBuilder, oldDatabase, newDatabase);
      
      if (!StringUtils.isBlank(script)) {
        //result.append("\n-- we are configured in grouper.properties to drop all tables \n");
        result.append(script).append("\n");
        //result.append("\n-- end drop all tables \n\n");
      }

      GrouperDdl.alreadyAddedTableIndices = false;
      
    }
  }

  private boolean dropViewsAndForeignKeysIfNeeded(String objectName,
      Connection connection, String schema, Platform platform, StringBuilder result,
      int javaVersion, DdlVersionable ddlVersionableJava, int dbVersion,
      DbMetadataBean dbMetadataBean, SqlBuilder sqlBuilder) {
    // see if we really need to recreate views/keys
    boolean recreateViewsAndForeignKeys = true;
    if (!dropBeforeCreate && !dropOnly) {
      boolean reallyNeedToRecreate = false;
      for (int version = dbVersion+1; version<=javaVersion; version++) {
        DdlVersionable v = GrouperDdlUtils.retieveVersion(objectName, version);
        if (v.recreateViewsAndForeignKeys()) {
          reallyNeedToRecreate = true;
          break;
        }
      }
      
      if (!reallyNeedToRecreate) {
        recreateViewsAndForeignKeys = false;
      }
    }

    {
      if (recreateViewsAndForeignKeys) {
        if (fromStartup && dbVersion > 0) {
          // dont run a script that will go off the rails
          writeAndRunScript = false;
        }
        //drop all views since postgres will drop view cascade (and we dont know about it), and cant create or replace with changes
        DdlVersionBean tempDdlVersionBean = new DdlVersionBean(objectName, platform, connection, schema, sqlBuilder, null, null, null, false, -1, result, 0);
        GrouperDdlUtils.ddlVersionBeanThreadLocalAssign(tempDdlVersionBean);
        try {
          ddlVersionableJava.dropAllViews(tempDdlVersionBean);

          //drop all foreign keys since ddlutils likes to do this anyways, lets do it before the script starts
          GrouperDdlUtils.dropAllForeignKeysScript(dbMetadataBean, tempDdlVersionBean);
        } finally {
          GrouperDdlUtils.ddlVersionBeanThreadLocalClear();
        }
      }
    }
    return recreateViewsAndForeignKeys;
  }

  private Boolean checkIfChangeLogEmptyRequired(String objectName, int javaVersion,
      int realDbVersion) {
    // see if any version between the database and java requires change log checking
    boolean checkAboutChangeLog = false;
    for (int i=realDbVersion+1; i<=javaVersion; i++) {
      DdlVersionable currentVersionable = GrouperDdlUtils.retieveVersion(objectName, i);
      if (currentVersionable.requiresEmptyChangelog()) {
        checkAboutChangeLog = true;
      }
    }
    if (checkAboutChangeLog) {
      // if the temp change log has entries, don't let the upgrade continue..
      int tempChangeLogCount = GrouperDdlUtils.getTableCount("grouper_change_log_entry_temp", false);
      if (tempChangeLogCount > 0) {
        System.err.println("NOTE: Grouper database schema DDL may require updates, but the temp change log must be empty to perform an upgrade.  To process the temp change log, start up your current version of GSH and run: loaderRunOneJob(\"CHANGE_LOG_changeLogTempToChangeLog\")");
        return false;
      }
    }
    return null;
  }

  private Boolean waitForOtherJvmsOrLockInDatabase() {
    // we need to lock with the DB so two JVMs dont try to run DDL at the same time
    List<Hib3GrouperDdlWorker> grouperDdlWorkers = HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null);
    
    boolean waitForOtherProcessesToDoDdl = false;
    
    Hib3GrouperDdlWorker grouperDdlWorker = null;

    if (thisDdlDatabaseLockingUuid == null) {
      thisDdlDatabaseLockingUuid = GrouperUuid.getUuid();
    }

    if (GrouperUtil.length(grouperDdlWorkers) == 0) {
      grouperDdlWorker = new Hib3GrouperDdlWorker();

      // this is the only value since it is unique and one row in table
      grouperDdlWorker.setGrouper("grouper");
    } else {
     
      grouperDdlWorker = grouperDdlWorkers.get(0);
      
      if (!StringUtils.equals(this.thisDdlDatabaseLockingUuid, grouperDdlWorker.getWorkerUuid()) 
          && grouperDdlWorker.getHeartbeat() != null && System.currentTimeMillis() - grouperDdlWorker.getHeartbeat().getTime() < 20000) {
        waitForOtherProcessesToDoDdl = true; 
      }
      
    }
    
    if (!waitForOtherProcessesToDoDdl) {
    
      grouperDdlWorker.setHeartbeat(new Timestamp(System.currentTimeMillis()));
      grouperDdlWorker.setLastUpdated(new Timestamp(System.currentTimeMillis()));
      grouperDdlWorker.setWorkerUuid(thisDdlDatabaseLockingUuid);
      try {
        HibernateSession.byObjectStatic().saveOrUpdate(grouperDdlWorker);
        
        //ok, we stored, are we in there?
        GrouperUtil.sleep(3000);
        
        grouperDdlWorker = HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null).get(0);
        
        if (!StringUtils.equals(thisDdlDatabaseLockingUuid, grouperDdlWorker.getWorkerUuid())) {
          waitForOtherProcessesToDoDdl = true;
        }

        // lets do it!
        
      } catch (Exception e) {
        waitForOtherProcessesToDoDdl = true;
      }
    }
    if (waitForOtherProcessesToDoDdl) {
      // some other jvm did this at the same time
      // lets wait until done, and then exit
      for (int i=0;i<2000;i++) {
        if (i==40) {
          String waitingErrorMessage = "Waiting for another process to finish DDL updates...";
          LOG.error(waitingErrorMessage);
          System.out.println(waitingErrorMessage);
        }
        GrouperUtil.sleep(5000);
        grouperDdlWorker = HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null).get(0);
        if (grouperDdlWorker.getHeartbeat() == null) {
          return false;
        }
        if (System.currentTimeMillis() - grouperDdlWorker.getHeartbeat().getTime() > 90000) {
          throw new RuntimeException("Heartbeat of DDL worker is not updating!!!!");
        }
      }
      throw new RuntimeException("DDL updates never completed successfully!");
    }
    startHeartbeatThreadIfNull(grouperDdlWorker);
    return null;
  }

  private void startHeartbeatThreadIfNull(final Hib3GrouperDdlWorker GROUPER_DDL_WORKER) {
    if (heartbeatThread == null) {
      heartbeatThread = new Thread(new Runnable() {
 
        @Override
        public void run() {
          try {
            
            for (int i=0;i<10000;i++) {
              
              // update the heartbeat every 5 seconds
              for (int j=0;j<5;j++) {
                GrouperUtil.sleep(1000);
                if (GrouperDdlEngine.this.done) {
                  // we done
                  GROUPER_DDL_WORKER.setHeartbeat(null);
                  GROUPER_DDL_WORKER.setLastUpdated(new Timestamp(System.currentTimeMillis()));
                  HibernateSession.byObjectStatic().saveOrUpdate(GROUPER_DDL_WORKER);
                  return;
                }
              }
              Timestamp timestamp = new Timestamp(System.currentTimeMillis());
              GROUPER_DDL_WORKER.setHeartbeat(timestamp);
              GROUPER_DDL_WORKER.setLastUpdated(timestamp);
              HibernateSession.byObjectStatic().saveOrUpdate(GROUPER_DDL_WORKER);
            }
            throw new RuntimeException("DDL didnt end!!!!!");
          } catch (Exception e) {
            LOG.error("Error running heartbeat", e);
          } finally {
            GrouperDdlEngine.this.heartbeatThread = null;
          }
          
        }
        
      });
      heartbeatThread.start();
    }
  }
  
  /**
   * @return true if up to date, false if needs to run a script
   */
  public boolean runDdl() {
    
    this.thisDdlDatabaseLockingUuid = null;
    this.done = false;

    heartbeatThread = null;
    
    //start with success
    everythingRightVersion = true;
    
    if (promptUser) {
      String prompt = GrouperUtil.PROMPT_KEY_SCHEMA_EXPORT_ALL_TABLES + " (dropThenCreate=" + (dropBeforeCreate ? "T" : "F")
        + ",writeAndRunScript=" + (writeAndRunScript ? "T" : "F") + ")";
     
     //make sure it is ok to change db
     GrouperUtil.promptUserAboutDbChanges(prompt, true);
 
    }
    boolean upToDate = false;
    String resultString = null;
    
    try {
      GrouperDdlUtils.insideBootstrap = true;
      GrouperDdlUtils.isDropBeforeCreate = dropBeforeCreate;
  
      //clear out for this run (in case testing, might call this multiple times)
      GrouperDdlUtils.alreadyInsertedForObjectName.clear();
      
      //clear out cache of object versions since if multiple calls from unit tests, can get bad data
      GrouperDdlUtils.cachedDdls = null;
      
      //if we are messing with ddl, lets clear caches
      FieldFinder.clearCache();
      GroupTypeFinder.clearCache();
      
      Platform platform = GrouperDdlUtils.retrievePlatform(false);
      
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
  
        List<String> objectNames = GrouperDdlUtils.retrieveObjectNames();
        //System.err.println(GrouperUtil.toStringForLog(objectNames));
        for (String objectName : objectNames) {
          Boolean subResult = runDdlForObjectName(objectName, connection, schema, platform, result);
          if (subResult != null) {
            return subResult;
          }
        }
  
      } finally {
        GrouperUtil.closeQuietly(connection);
      }
  
      resultString = result.toString();
      
      //if mysql, substitute varchar4000 for text
      if (GrouperDdlUtils.isMysql() && !GrouperConfig.retrieveConfig().propertyValueBoolean("ddlutils.dontSubstituteVarchar4000forTextMysql", false)) {
        resultString = StringUtils.replace(resultString, "VARCHAR(4000)", "text");
      }
      
      if (StringUtils.isNotBlank(resultString)) {
  
        writeAndRunScript(resultString, grouperDb);
      } else {
        boolean printed = false;
        String note = "NOTE: database table/object structure (ddl) is up to date";
        if (!compareFromDbVersion) {
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
      ConfigPropertiesCascadeBase.assignInitted();
      if (installDefaultGrouperData && !dropOnly && (upToDate || writeAndRunScript)) {
        registryInstall();
      }
    } finally {
      GrouperDdlUtils.insideBootstrap = false;
      GrouperDdlUtils.isDropBeforeCreate = false;
      
      // tell the heartbeat we are done
      this.done=true;
      
      // wait for the heartbeat to return
      if (heartbeatThread != null) {
        GrouperUtil.threadJoin(heartbeatThread);
      }

    }
    return false;

  }

  private void writeAndRunScript(String resultString, GrouperLoaderDb grouperDb) {
    String scriptDirName = GrouperConfig.retrieveConfig().propertyValueString("ddlutils.directory.for.scripts");
    
    File scriptFile = GrouperUtil.newFileUniqueName(scriptDirName, "grouperDdl", ".sql", true);
    GrouperUtil.saveStringIntoFile(scriptFile, resultString);
 
    String logMessage = "Grouper database schema DDL requires updates\n(should run script manually and carefully, in sections, verify data before drop statements, backup/export important data before starting, follow change log on confluence, dont run exact same script in multiple envs - generate a new one for each env),\nscript file is:\n" + GrouperUtil.fileCanonicalPath(scriptFile);
    if (GrouperDdlUtils.internal_printDdlUpdateMessage) {
    LOG.error(logMessage);
    System.err.println(logMessage);
    }
    logMessage = "";
    if (writeAndRunScript) {
      GrouperDdlUtils.sqlRun(scriptFile, grouperDb.getDriver(), grouperDb.getUrl(), 
          grouperDb.getUser(), grouperDb.getPass(), fromUnitTest, callFromCommandLine);
      //lets clear the type cache
      AuditTypeFinder.clearCache();
      ChangeLogTypeFinder.clearCache();
      MemberFinder.clearInternalMembers();
      if (fromStartup) {
        everythingRightVersion = true;
      }
    } else {
      if (callFromCommandLine || GrouperShell.runFromGsh) {
        System.err.println("Note: this script was not executed due to option passed in");
        System.err.println("To run script via gsh, carefully review it, then run this:\ngsh -registry -runsqlfile " 
            + GrouperUtil.fileCanonicalPath(scriptFile).replace("\\", "\\\\"));
      }
    }
  }

  private void registryInstall() {
    try {
      //lets reset the hibernate configuration so it can get properly configured
      Hib3DAO.hibernateInitted = false;
      RegistryInstall.install();
    } catch (RuntimeException e) {
      if (!GrouperShell.runFromGsh && callFromCommandLine && !writeAndRunScript) {
        String addendum = LOG.isInfoEnabled() ? "" : ".  The specifics are not logged";
        String error = "FATAL: could not install grouper data, you need to run the SQL script, then try again" + addendum + ": " + e.getMessage();
        System.err.println(error);
        LOG.fatal(error);
        LOG.info("stack", e);
        System.exit(1);
      } else {
        
        everythingRightVersion = false;
        throw e;
      }
    }
  }
  
}
