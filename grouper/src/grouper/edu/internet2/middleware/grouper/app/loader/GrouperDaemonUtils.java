package edu.internet2.middleware.grouper.app.loader;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperDaemonUtils {

  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonUtils.class);
  
  private static Set<String> pausedJobs = ConcurrentHashMap.newKeySet();
  
  private static boolean daemonPausedJobsCheckThreadShouldStop = false;

  private static Thread daemonPausedJobsCheckThread = new Thread(new Runnable() {

    @Override
    public void run() {
      while (true) {
        try {
          String sql = "select distinct job_name from grouper_qz_triggers where (trigger_state = 'PAUSED' or trigger_state='PAUSED_BLOCKED') and trigger_name not like 'MT_%'";
          
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> currentPausedJobs = gcDbAccess.sql(sql.toString()).selectList(String.class);
          pausedJobs.retainAll(currentPausedJobs);
          pausedJobs.addAll(currentPausedJobs);
        } catch (Exception e) {
          LOG.warn("Exception checking for paused jobs", e);
        }
        
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          // ignore
        }
        
        if (daemonPausedJobsCheckThreadShouldStop) {
          break;
        }
      }
    }
  });
  
  public static void startDaemonPausedJobsCheckThread() {
    daemonPausedJobsCheckThreadShouldStop = false;
    if (daemonPausedJobsCheckThread.isAlive()) {
      return;
    }
    daemonPausedJobsCheckThread.setDaemon(true);
    daemonPausedJobsCheckThread.start();
  }
  
  public static void stopProcessingIfJobPaused(String jobName) {
    if (pausedJobs.contains(jobName)) {
      throw new RuntimeException("Job " + jobName + " is marked as disabled (paused in Quartz).  This was likely done from the UI.");
    }
  }
  
  public static void stopDaemonPausedJobsCheckThread() {
    daemonPausedJobsCheckThreadShouldStop = true;
    
    while (true) {
      if (!daemonPausedJobsCheckThread.isAlive()) {
        return;
      }
      
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
