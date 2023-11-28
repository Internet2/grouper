package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;

/**
 * called when grouper is shutting down
 * @author mchyzer
 *
 */
public class GrouperShutdown {

  /**
   * 
   */
  public GrouperShutdown() {
  }

  /**
   * 
   */
  public static void shutdown() {
    //FrameworkStarter.getInstance().stop();
    
    // this has a daemon thread
    EhcacheController.ehcacheController().stop();
    
    GrouperHttpClient.shutdown();
  }
}
