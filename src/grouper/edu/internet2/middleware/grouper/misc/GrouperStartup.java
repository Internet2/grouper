/*
 * @author mchyzer
 * $Id: GrouperStartup.java,v 1.11 2008-10-30 20:57:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
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
      
      if (!GrouperConfig.getPropertyBoolean("registry.autoinit", true)) {
        LOG.fatal("grouper.properties registry.autoinit is false, so not auto initting.  " +
            "But the registry needs to be auto-initted.  Please init the registry with GSH: registryInstall()  " +
            "Initting means adding some default data like the root stem, built in fields, etc.");
      } else {
        initData(true);
      }
      
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
   * init data
   * @param logError
   */
  public static void initData(boolean logError) {
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
      }
    }
  }
  
  /** if we should run the boot strap from startup */
  public static boolean runDdlBootstrap = true;
  
  
}
