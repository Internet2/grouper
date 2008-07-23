/*
 * @author mchyzer
 * $Id: GrouperStartup.java,v 1.1 2008-07-23 06:41:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;


/**
 * this should be called when grouper starts up
 */
public class GrouperStartup {

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
    
    //first make sure the DB ddl is up to date
    GrouperDdlUtils.bootstrap();
    
    return true;
  }
  
}
