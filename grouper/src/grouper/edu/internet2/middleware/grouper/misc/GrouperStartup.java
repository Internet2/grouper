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
/*
 * @author mchyzer
 * $Id: GrouperStartup.java,v 1.21 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAutoSourceAdapter;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.registry.RegistryInstall;
import edu.internet2.middleware.grouper.subj.InternalSourceAdapter;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperToStringStyle;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * this should be called when grouper starts up.  this file needs to be utf-8
 */
public class GrouperStartup {

  /** if running from main and expecting to print to the screen */
  public static boolean runFromMain = false;
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperStartup.class);
  
  /**
   * keep track if started or not
   */
  public static boolean started = false;
  
  /** if we should ignore checkconfig */
  public static boolean ignoreCheckConfig = false;
  
  /** if errors should be logged (perhaps in all cases except registry init) */
  public static boolean logErrorStatic = true;
  /**
   * if startup has finished sucessfully
   * @return the finishedStartupSuccessfully
   */
  public static boolean isFinishedStartupSuccessfully() {
    return finishedStartupSuccessfully;
  }

  /**
   * if startup has finished sucessfully
   */
  private static boolean finishedStartupSuccessfully = false;
  

  /** print this once */
  private static boolean printedConfigLocation = false;
  
  /**
   * print where config is read from, to sys out and log warn
   */
  private static void printConfigOnce() {
    
    if (printedConfigLocation) {
      return;
    }

    printedConfigLocation = true;

    Properties properties = GrouperUtil.propertiesFromResourceName("grouper.properties");
    
    String displayMessageString = GrouperUtil.propertiesValue(properties, "configuration.display.startup.message");
    
    String grouperStartup = "Grouper starting up: " + versionTimestamp();
    if (!GrouperUtil.booleanValue(displayMessageString, true)) {
      //just log this to make sure we can
      try {
        LOG.warn(grouperStartup);
      } catch (RuntimeException re) {
        //this is bad, print to stderr rightaway (though might dupe)
        System.err.println(GrouperUtil.LOG_ERROR);
        re.printStackTrace();
        throw new RuntimeException(GrouperUtil.LOG_ERROR, re);
      }
      
      return;
    }

    StringBuilder resultString = new StringBuilder();
    resultString.append(grouperStartup + "\n");
    File grouperPropertiesFile = GrouperUtil.fileFromResourceName("grouper.properties");
    String propertiesFileLocation = grouperPropertiesFile == null ? "not found" 
        : GrouperUtil.fileCanonicalPath(grouperPropertiesFile); 
    resultString.append("grouper.properties read from: " + propertiesFileLocation + "\n");

    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.api.readonly", false)) {
      resultString.append("grouper.api.readonly:         true\n");
    }
    
    resultString.append("Grouper current directory is: " + new File("").getAbsolutePath() + "\n");
    //get log4j file
    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    String log4jFileLocation = log4jFile == null ? " [cant find log4j.properties]" :
      GrouperUtil.fileCanonicalPath(log4jFile);
    resultString.append("log4j.properties read from:   " + log4jFileLocation + "\n");
    
    resultString.append(GrouperUtil.logDirPrint());    
    File hibPropertiesFile = GrouperUtil.fileFromResourceName("grouper.hibernate.properties");
    String hibPropertiesFileLocation = hibPropertiesFile == null ? " [cant find grouper.hibernate.properties]" :
      GrouperUtil.fileCanonicalPath(hibPropertiesFile);
    resultString.append("grouper.hibernate.properties: " + hibPropertiesFileLocation + "\n");
    
    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties");
    String url = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    resultString.append("grouper.hibernate.properties: " + user + "@" + url + "\n");
    String sourcesString = "problem with sources";
    try {
      sourcesString = SourceManager.getInstance().printConfig();
    } catch (Exception e) {
      LOG.error("problem with sources", e);
    }
    resultString.append(sourcesString);
    System.out.println(resultString);
    try {
      if (!GrouperUtil.isPrintGrouperLogsToConsole()) {
        LOG.warn(resultString);
      } else {
        //print something to log to make sure we can
        LOG.warn(grouperStartup);
      }
    } catch (RuntimeException re) {
      //this is bad, print to stderr rightaway (though might dupe)
      System.err.println(GrouperUtil.LOG_ERROR);
      re.printStackTrace();
      throw new RuntimeException(GrouperUtil.LOG_ERROR, re);
    }
  }


  /**
   * @return version timestamp
   */
  public static String versionTimestamp() {
    String buildTimestamp = null;
    try {
      buildTimestamp = GrouperCheckConfig.manifestProperty(GrouperStartup.class, new String[]{"Build-Timestamp"});
    } catch (Exception e) {
      //its ok, might not be running in jar
    }
    Properties properties = GrouperUtil.propertiesFromResourceName("grouper.properties");
    
    String env = GrouperUtil.propertiesValue(properties, "grouper.env.name");
    env = StringUtils.defaultIfEmpty(env, "<no label configured>");
    String grouperStartup = "version: " + GrouperVersion.GROUPER_VERSION 
      + ", build date: " + buildTimestamp + ", env: " + env;
    
    return grouperStartup;
  }

  /**
   * call this when grouper starts up
   * @return false if already started, true if this started it
   */
  public synchronized static boolean startup() {
    try {
      if (started) {
        return false;
      }
      started = true;
  
      printConfigOnce();
  
      //check java version
//      if (GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.checkJavaVersion", true)) {
//        String javaVersion = System.getProperty("java.version");
//        if (javaVersion != null && !javaVersion.startsWith("1.6") && !javaVersion.startsWith("1.7")) {
//          String error = "Error: Java should be version 6 or 7 (1.6 or 1.7), but is detected as: " + javaVersion;
//          LOG.error(error);
//          System.out.println(error);
//        }
//      }

      //add in custom sources
      SourceManager.getInstance().loadSource(SubjectFinder.internal_getGSA());
      SourceManager.getInstance().loadSource(InternalSourceAdapter.instance());
      
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("externalSubjects.autoCreateSource", true)) {
        
        SourceManager.getInstance().loadSource(ExternalSubjectAutoSourceAdapter.instance());
        
      }
      
//      if (GrouperConfig.retrieveConfig().propertyValueBoolean("entities.autoCreateSource", true)) {
//        
//        SourceManager.getInstance().loadSource(EntitySourceAdapter.instance());
//        
//      }
      
      //dont print big classname, dont print nulls
      ToStringBuilder.setDefaultStyle(new GrouperToStringStyle());

      //first check databases
      
      if (!ignoreCheckConfig) {
        GrouperCheckConfig.checkGrouperDb();
      }
      
      if (runDdlBootstrap) {
        //first make sure the DB ddl is up to date
        GrouperDdlUtils.bootstrap(false, false, false);
      }

      if (!ignoreCheckConfig) {
        //make sure configuration is ok
        GrouperCheckConfig.checkConfig();
      }
      
      //startup hooks
      GrouperHooksUtils.fireGrouperStartupHooksIfNotFiredAlready();
  
      //register hib objects
      Hib3DAO.initHibernateIfNotInitted();
      
      initData(true);
      
      boolean legacyAttributeMigrationIncomplete = GrouperDdlUtils.getTableCount("grouper_types_legacy", false) > 0;
      
      // skip for now if legacy attribute migration is not done
      if (!legacyAttributeMigrationIncomplete) {
        //init include exclude type
        initIncludeExcludeType();
        
        //init loader types and attributes if configured
        initLoaderType();
        
        //init membership lite config type
        initMembershipLiteConfigType();
      }
      
      // verify member search and sort config
      verifyMemberSortAndSearchConfig();
      
      // verify id indexes
      verifyTableIdIndexes();

      verifyUtf8andTransactions();
      
      finishedStartupSuccessfully = true;
      
      return true;
    } catch (RuntimeException re) {
      if (logErrorStatic) {
        //NOTE, the caller might not handle this exception, so print now. 
        //ALSO, the logger might not work, so print to stderr first
        String error = "Couldnt startup grouper: " + re.getMessage();
        System.err.println(error);
        re.printStackTrace();
        LOG.error(error, re);
      }
      throw re;
    }
  }

  /**
   * make sure grouper can handle utf8
   */
  public static void verifyUtf8andTransactions() {
    
    GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("verifyUtf8andTransactions") {
      
      @Override
      public Void callLogic() {
        try {
          verifyUtf8andTransactionsHelper();
        } catch (Exception e) {
          String error = "Error: Problems checking UTF and database features";
          LOG.error(error, e);
          System.out.println(error);
          e.printStackTrace();
        }
        return null;
      }
    };
   
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.checkDatabaseAndUtf.inNewThread", true)) {
      grouperCallable.callLogic();
    } else {
      GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
    }
  }
  
  /**
   * make sure grouper can handle utf8
   */
  public static void verifyUtf8andTransactionsHelper() {

    //hibernate not ok yet, try next time starting up
    if (!GrouperDdlUtils.okToUseHibernate()) {
      return;
    }
    
    boolean detectTransactionProblems = GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.detect.db.transaction.problems", true);

    boolean detectUtf8Problems = GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.detect.utf8.problems", true);

    boolean detectUtf8FileProblems = GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.detect.utf8.file.problems", true);
    
    boolean detectCaseSensitiveProblems = GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.detect.db.caseSensitive.problems", true);

    boolean utfProblems = false;
    
    if (!detectUtf8Problems && !detectTransactionProblems && !detectCaseSensitiveProblems) {
      return;
    }

    final String someUtfString = "ٹٺٻټكلل";

    final String id = GrouperUuid.getUuid();
    final String name = "grouperUtf_" + GrouperUuid.getUuid();
    
    if (detectUtf8FileProblems && !utfProblems) {
      String theStringFromFile = null;
      try {
        theStringFromFile = GrouperUtil.readResourceIntoString("grouperUtf8.txt", false);
      } catch (Exception e) {
        String error = "Error: Cannot read string from resource grouperUtf8.txt";
        LOG.error(error, e);
        System.out.println(error);
        e.printStackTrace();
        utfProblems = true;
      }
      if (!utfProblems && !StringUtils.equals(theStringFromFile, someUtfString)) {
        String error = "Error: Cannot properly read UTF string from resource: grouperUtf8.txt: '" + theStringFromFile
            + "'";

        String fileEncoding = System.getProperty("file.encoding");
        if (fileEncoding == null || !fileEncoding.toLowerCase().startsWith("utf")) {
          error += ", make sure you pass in the JVM switch -Dfile.encoding=utf-8 (currently is '" 
              + fileEncoding + "')";
        }

        fileEncoding = GrouperConfig.retrieveConfig().propertyValueString("grouper.default.fileEncoding");
        if (fileEncoding == null || !fileEncoding.toLowerCase().startsWith("utf")) {
          error += ", make sure you have grouper.default.fileEncoding set to UTF-8 in the grouper.properties (or leave it out since the default should be UTF-8)";
        }

        LOG.error(error);
        System.out.println(error);
        utfProblems = true;
      }
      if (!utfProblems & GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.display.utf8.success.message", false)) {
        System.out.println("Grouper can read UTF characters correctly from files");
      }
    }

    //lets check case sensitive
    if (detectCaseSensitiveProblems) {
      //load the grouper object in app upper

      Hib3GrouperDdl grouperDdl = GrouperDdlUtils.retrieveDdlByNameFromDatabase("GROUPER");

      if (grouperDdl != null) {

        String error = "Error: Queries in your database seem to be case insensitive, "
            + "this can be a problem for Grouper, if you are using MySQL you should use a bin collation";
        LOG.error(error);
        System.out.println(error);

      } else if (GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.display.db.caseSensitive.success.message", false)) {

        System.out.println("Your database can handle case sensitive queries correctly");

      }
    }

    //this shouldnt exist, just make sure
    GrouperDdlUtils.deleteUtfDdls();

    Hib3GrouperDdl grouperDdl = GrouperDdlUtils.storeAndReadUtfString(someUtfString, id, name);

    //lets check transactions
    if (detectTransactionProblems) {
      Hib3GrouperDdl grouperDdlNew = GrouperDdlUtils.retrieveDdlByIdFromDatabase(id);
      if (grouperDdlNew != null) {

        String error = "Error: Your database does not seem to support transactions, Grouper requires a transactional database";
        LOG.error(error);
        System.out.println(error);

        //delete it again
        GrouperDdlUtils.deleteDdlById(id);

      } else if (GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.display.transaction.success.message", false)) {
        System.out.println("Your database can handle transactions correctly");
      }
    }

    if (grouperDdl == null) {
      String error = "Error: Why is grouperDdl utf null???";
      LOG.error(error);
      System.out.println(error);
      utfProblems = true;
    } else if (!utfProblems) {

      if (!StringUtils.equals(grouperDdl.getHistory(), someUtfString)) {
        String error = "Error: Cannot properly read UTF string from database: '" + grouperDdl.getHistory()
            + "', make sure your database has UTF tables and perhaps a hibernate.connection.url in grouper.hibernate.properties";
        LOG.error(error);
        System.out.println(error);
        utfProblems = true;
      }
    }
    if (!utfProblems & GrouperConfig.retrieveConfig().propertyValueBoolean("configuration.display.utf8.success.message", false)) {
      System.out.println("Grouper and the database can handle UTF characters correctly");
    }
  }

  /**
   * verify that at least one search/sort column is specified for each source
   */
  public static void verifyMemberSortAndSearchConfig() {
    for (Source source : SourceManager.getInstance().getSources()) {
      if (source.getSortAttributes() == null || source.getSortAttributes().size() == 0) {
        throw new RuntimeException("At least one sort column should be specified for source " + source.getId());
      }
      
      if (source.getSearchAttributes() == null || source.getSearchAttributes().size() == 0) {
        throw new RuntimeException("At least one search column should be specified for source " + source.getId());
      }
    }
  }
  
  /**
   * verify that table id indexes
   */
  public static void verifyTableIdIndexes() {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.tableIndex.verifyOnStartup", true)) {
      return;
    }
        
    //lets see if there are any nulls
    for (TableIndexType tableIndexType : TableIndexType.values()) {
      
      List<String> ids = HibernateSession.bySqlStatic().listSelect(
          String.class, "select id from " + tableIndexType.tableName() + " where id_index is null order by id" , null);

      if (GrouperUtil.length(ids) > 0) {
        LOG.error("Found " + GrouperUtil.length(ids) + " " + tableIndexType.name() + " records with null id_index... correcting... please wait...");
        for (int i=0;i<GrouperUtil.length(ids); i++) {
          HibernateSession.bySqlStatic().executeSql("update " + tableIndexType.tableName() 
              + " set id_index = ? where id = ?", GrouperUtil.toListObject(TableIndex.reserveId(tableIndexType), ids.get(i)));
          if (i+1 % 1000 == 0) {
            LOG.warn("Updated " + (i-1) + "/" + GrouperUtil.length(ids) + " " + tableIndexType + " id_index records...");
          }
        }
        LOG.warn("Finished " + GrouperUtil.length(ids) + " " + tableIndexType + " id_index records...");
      }
    }
  }
  
  /**
   * init membership lite config type
   */
  public static void initMembershipLiteConfigType() {
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("membershipUpdateLiteTypeAutoCreate", false)) {
      
      GrouperSession grouperSession = null;

      try {

        grouperSession = GrouperSession.startRootSession(false);

        GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

          public Object callback(GrouperSession grouperSession)
              throws GrouperSessionException {
            try {
              
              GroupType groupMembershipLiteSettingsType = GroupType.createType(grouperSession, "grouperGroupMembershipSettings", false);

              groupMembershipLiteSettingsType.addAttribute(grouperSession,"grouperGroupMshipSettingsUrl", false);
              

            } catch (Exception e) {
              throw new RuntimeException(e.getMessage(), e);
            } finally {
              GrouperSession.stopQuietly(grouperSession);
            }
            return null;
          }

        });

      } catch (Exception e) {
        throw new RuntimeException("Problem adding membership lite type/attributes", e);
      }

      
    }
    
  }
  
  /**
   * init the loader types and attributes if configured to
   */
  public static void initLoaderType() {
    boolean autoaddBoolean = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.autoadd.typesAttributes", true);

    if (!autoaddBoolean) {
      return;
    }

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.startRootSession(false);

      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            
            GroupType loaderType = GroupType.createType(grouperSession, "grouperLoader", false);

            loaderType.addAttribute(grouperSession,"grouperLoaderType", false);
            
            loaderType.addAttribute(grouperSession,"grouperLoaderDbName", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderScheduleType", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderQuery", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderQuartzCron", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderIntervalSeconds", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderPriority", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderAndGroups", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderGroupTypes", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderGroupsLike", false);
            loaderType.addAttribute(grouperSession,"grouperLoaderGroupQuery", false);

          } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          return null;
        }

      });

      //register the hook if not already
      GroupTypeTupleIncludeExcludeHook.registerHookIfNecessary(true);
      
    } catch (Exception e) {
      throw new RuntimeException("Problem adding loader type/attributes", e);
    }

  }
  
  /**
   * init the include/exclude type if configured in the grouper.properties
   */
  public static void initIncludeExcludeType() {
    
    final boolean useGrouperIncludeExclude = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.use", false);
    final boolean useGrouperRequireGroups = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    final String includeExcludeGroupTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.type.name");
    final String requireGroupsTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.type.name");

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.startRootSession(false);

      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            
            @SuppressWarnings("unused")
            GroupType includeExcludeGroupType = useGrouperIncludeExclude ? 
                GroupType.createType(grouperSession, includeExcludeGroupTypeName, false) : null;

            GroupType requireGroupsType = useGrouperRequireGroups ? 
                GroupType.createType(grouperSession, requireGroupsTypeName, false) : null;

            //first the requireGroups
            String attributeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.attributeName");

            if (useGrouperRequireGroups && !StringUtils.isBlank(attributeName)) {
              requireGroupsType.addAttribute(grouperSession,attributeName, false);
            }

            if (useGrouperRequireGroups) {
              //add types/attributes from grouper.properties
              int i=0;
              while (true) {
                String propertyName = "grouperIncludeExclude.requireGroup.name." + i;
                String attributeOrTypePropertyName = "grouperIncludeExclude.requireGroup.attributeOrType." + i;

                String propertyValue = GrouperConfig.retrieveConfig().propertyValueString(propertyName);
                if (StringUtils.isBlank(propertyValue)) {
                  break;
                }
                String attributeOrTypeValue = GrouperConfig.retrieveConfig().propertyValueString(attributeOrTypePropertyName);
                boolean attributeOrType = StringUtils.equals("attribute", attributeOrTypeValue);
                if (attributeOrType) {
                  requireGroupsType.addAttribute(grouperSession, propertyValue, false);
                } else {
                  GroupType.createType(grouperSession, propertyValue, false);
                }
                i++;
              }
            }
            
          } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          return null;
        }
        
      });
      
      //register the hook if not already
      GroupTypeTupleIncludeExcludeHook.registerHookIfNecessary(true);
      
    } catch (Exception e) {
      throw new RuntimeException("Problem adding include/exclude type: " + includeExcludeGroupTypeName, e);
    }

  }
  
  /**
   * init data
   * @param logError
   */
  public static void initData(boolean logError) {
    try {
      //lets see if we need to
      boolean needsInit;
      GrouperSession grouperSession = null;
      try {
        grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
      } catch (SessionException se) {
        throw new RuntimeException(se);
      }
      try {
        needsInit = StemFinder.findRootStem(grouperSession) == null;
        needsInit = needsInit || FieldFinder.find(Field.FIELD_NAME_ADMINS, true) == null ;
      } catch (Exception e) {
        if (logError && logErrorStatic) {
          LOG.error("Error initializing data, might just need to auto-create some data to fix...", e);
        }
        needsInit = true;
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      if (needsInit) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("registry.autoinit", true)) {
          try {
            
            RegistryInstall.install();
            
          } catch (Exception e) {
            if (logError && logErrorStatic) {
              String error = "Couldnt auto-create data: " + e.getMessage();
              LOG.fatal(error, e);
            }
          }
        } else {
          
          if (logError && logErrorStatic) {
            LOG.fatal("grouper.properties registry.autoinit is false, so not auto initting.  " +
              "But the registry needs to be auto-initted.  Please init the registry with GSH: registryInstall()  " +
              "Initting means adding some default data like the root stem, built in fields, etc.");
          }
        }
      }
    } catch (Exception e) {
      if (logError && logErrorStatic) {
        LOG.error("Error initting data", e);
      }
    }
  }
  
  /** if we should run the boot strap from startup */
  public static boolean runDdlBootstrap = true;
  
  
}
