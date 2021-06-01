package edu.internet2.middleware.grouper.app.externalSystem;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * refresh external system connections when properties change
 * @author shilen
 */
public class GrouperExternalSystemConnectionRefresher {

  private static final Log LOG = GrouperUtil.getLog(GrouperExternalSystemConnectionRefresher.class);

  private static Thread grouperExternalSystemConnectionRefresherThread = null;
  
  private static void assignThread() {
    if (grouperExternalSystemConnectionRefresherThread == null) {
      grouperExternalSystemConnectionRefresherThread = new Thread(new Runnable() {

        @Override
        public void run() {
          
         while (true) {
            try {
              int checkIntervalInSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.externalSystem.connectionRefresher.checkIntervalInSeconds", 60);

              GrouperUtil.sleep(checkIntervalInSeconds * 1000L);
              
              for (GrouperExternalSystem grouperExternalSystem : GrouperExternalSystem.retrieveAllGrouperExternalSystems()) {
                try {
                  grouperExternalSystem.refreshConnectionsIfNeeded();
                } catch (UnsupportedOperationException e) {
                  // ok
                }
              }

            } catch (Exception e) {
              LOG.error("Error in external system connection refresher thread", e);
            }
            
          }
        }
        
      });
      grouperExternalSystemConnectionRefresherThread.setDaemon(true);
    }
  }

  public static void startThreadIfNotStarted() {
    if (grouperExternalSystemConnectionRefresherThread == null || !grouperExternalSystemConnectionRefresherThread.isAlive()) {
      if (grouperExternalSystemConnectionRefresherThread == null) {
        assignThread();
      }
      grouperExternalSystemConnectionRefresherThread.start();
    }
  }  
}
