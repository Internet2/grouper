/*
 * @author mchyzer
 * $Id: GrouperStartup.java,v 1.15 2008-11-13 07:12:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryInstall;
import edu.internet2.middleware.grouper.util.GrouperToStringStyle;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * this should be called when grouper starts up
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
  private static boolean started = false;
  
  /** if we should ignore checkconfig */
  public static boolean ignoreCheckConfig = false;
  
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
  
      //dont print big classname, dont print nulls
      ToStringBuilder.setDefaultStyle(new GrouperToStringStyle());
      
      if (!ignoreCheckConfig) {
        //make sure configuration is ok
        GrouperCheckConfig.checkConfig();
      }
      
      //startup hooks
      GrouperHooksUtils.fireGrouperStartupHooksIfNotFiredAlready();
  
      //register hib objects
      Hib3DAO.initHibernateIfNotInitted();
      
      if (runDdlBootstrap) {
        //first make sure the DB ddl is up to date
        GrouperDdlUtils.bootstrap(false, false, false);
      }
      
      initData(true);
      
      //init include exclude type
      initIncludeExcludeType();
      
      //init loader types and attributes if configured
      initLoaderType();
      
      return true;
    } catch (RuntimeException re) {
      //NOTE, the caller might not handle this exception, so print now. 
      //ALSO, the logger might not work, so print to stderr first
      String error = "Couldnt startup grouper: " + re.getMessage();
      System.err.println(error);
      re.printStackTrace();
      LOG.error(error, re);
      throw re;
    }
  }
  
  /**
   * init the loader types and attributes if configured to
   */
  public static void initLoaderType() {
    String autoadd = ApiConfig.testConfig.get("loader.autoadd.typesAttributes");
    if (StringUtils.isBlank(autoadd)) {
      try {
        autoadd = GrouperLoaderConfig.getPropertyString("loader.autoadd.typesAttributes");
      } catch (Exception e) {
        //dont worry if cant get this property
      }
    }
    boolean autoaddBoolean = GrouperUtil.booleanValue(autoadd, false);
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

            loaderType.addAttribute(grouperSession,"grouperLoaderType", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            
            loaderType.addAttribute(grouperSession,"grouperLoaderDbName", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderScheduleType", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderQuery", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderQuartzCron", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderIntervalSeconds", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderPriority", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderAndGroups", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderGroupTypes", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            loaderType.addAttribute(grouperSession,"grouperLoaderGroupsLike", 
                AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);

            
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
    
    final boolean useGrouperIncludeExclude = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.use", false);
    final boolean useGrouperRequireGroups = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    final String includeExcludeGroupTypeName = GrouperConfig.getProperty("grouperIncludeExclude.type.name");
    final String requireGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");

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
            String attributeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.attributeName");

            if (useGrouperRequireGroups && !StringUtils.isBlank(attributeName)) {
              requireGroupsType.addAttribute(grouperSession,attributeName, 
                  AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
            }

            if (useGrouperRequireGroups) {
              //add types/attributes from grouper.properties
              int i=0;
              while (true) {
                String propertyName = "grouperIncludeExclude.requireGroup.name." + i;
                String attributeOrTypePropertyName = "grouperIncludeExclude.requireGroup.attributeOrType." + i;

                String propertyValue = GrouperConfig.getProperty(propertyName);
                if (StringUtils.isBlank(propertyValue)) {
                  break;
                }
                String attributeOrTypeValue = GrouperConfig.getProperty(attributeOrTypePropertyName);
                boolean attributeOrType = StringUtils.equals("attribute", attributeOrTypeValue);
                if (attributeOrType) {
                  requireGroupsType.addAttribute(grouperSession, propertyValue, 
                      AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
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
        needsInit = needsInit || FieldFinder.find("name") == null ;
        needsInit = needsInit || GroupTypeFinder.find("base") == null ;
      } catch (Exception e) {
        LOG.error("Error initializing data, might just need to auto-create some data to fix...", e);
        needsInit = true;
      }
      if (needsInit) {
        if (GrouperConfig.getPropertyBoolean("registry.autoinit", true)) {
          try {
            
            RegistryInstall.install();
            
          } catch (Exception e) {
            if (logError) {
              String error = "Couldnt auto-create data: " + e.getMessage();
              LOG.fatal(error, e);
            }
          }
        } else {
          LOG.fatal("grouper.properties registry.autoinit is false, so not auto initting.  " +
              "But the registry needs to be auto-initted.  Please init the registry with GSH: registryInstall()  " +
              "Initting means adding some default data like the root stem, built in fields, etc.");
        }
      }
    } catch (Exception e) {
      LOG.error("Error initting data", e);
    }
  }
  
  /** if we should run the boot strap from startup */
  public static boolean runDdlBootstrap = true;
  
  
}
