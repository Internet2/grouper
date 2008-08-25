/*
 * @author mchyzer
 * $Id: GrouperStartup.java,v 1.3 2008-08-25 01:17:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.registry.RegistryInstall;


/**
 * this should be called when grouper starts up
 */
public class GrouperStartup {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(GrouperStartup.class);
  /**
   * keep track if started or not
   */
  private static boolean started = false;
  
  /**
   * call this when grouper starts up
   * @return false if already started, true if this started it
   */
  public synchronized static boolean startup() {
    if (started) {
      return false;
    }
    started = true;
    //startup hooks
    GrouperHooksUtils.fireGrouperStartupHooksIfNotFiredAlready();

    //register hib objects
    Hib3DAO.initHibernateIfNotInitted();
    
    if (runDdlBootstrap) {
      //first make sure the DB ddl is up to date
      GrouperDdlUtils.bootstrap(false, false);
    }
    //lets see if we need to
    boolean needsInit;
    try {
      GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
      needsInit = StemFinder.findRootStem(grouperSession) == null;
      needsInit = needsInit || FieldFinder.find("name") == null ;
      needsInit = needsInit || GroupTypeFinder.find("base") == null ;
    } catch (Exception e) {
      needsInit = true;
    }
    if (needsInit) {
      try {
        
        RegistryInstall.main(new String[]{"internal"});
        
      } catch (Exception e) {
        String error = "Couldnt auto-create data: " + e.getMessage();
        LOG.error(error);
      }
    }

    return true;
  }

  /** if we should run the boot strap from startup */
  public static boolean runDdlBootstrap = true;
  
  
}
