package edu.internet2.middleware.grouper.app.loader;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperDaemonUtils {

  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonUtils.class);
  
  private static ThreadLocal<Hib3GrouperLoaderLog> threadLocalHib3GrouperLoaderLogOverall = new InheritableThreadLocal<Hib3GrouperLoaderLog>();

  private static Map<String, Date> pausedRunningJobs = new ConcurrentHashMap<String, Date>();
  
  private static boolean daemonPausedJobsCheckThreadShouldStop = false;

  private static Thread daemonPausedJobsCheckThread = new Thread(new Runnable() {

    @Override
    public void run() {
      while (true) {
        try {
          String sql = "select distinct gqft.job_name from grouper_QZ_TRIGGERS gqt, grouper_QZ_FIRED_TRIGGERS gqft where gqt.job_name=gqft.job_name and gqft.state='EXECUTING' and (gqt.trigger_state = 'PAUSED' or gqt.trigger_state='PAUSED_BLOCKED') and gqt.trigger_name not like 'MT_%'";
          
          GcDbAccess gcDbAccess = new GcDbAccess();
          List<String> currentPausedRunningJobs = gcDbAccess.sql(sql.toString()).selectList(String.class);
          Long currentTimeInMillis = System.currentTimeMillis();
          for (String jobName : pausedRunningJobs.keySet()) {
            if (!currentPausedRunningJobs.contains(jobName)) {
              pausedRunningJobs.remove(jobName);
            }
          }
          
          for (String jobName : currentPausedRunningJobs) {
            if (!pausedRunningJobs.containsKey(jobName)) {
              pausedRunningJobs.put(jobName, new Date(currentTimeInMillis));
            }
          }
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
  
  public static void stopProcessingIfJobPaused() {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = GrouperDaemonUtils.getThreadLocalHib3GrouperLoaderLogOverall();
    if (hib3GrouperLoaderLog == null || hib3GrouperLoaderLog.getStartedTime() == null || hib3GrouperLoaderLog.getJobName() == null) {
      // can't do anything here
      return;
    }
    
    Date dateWhenDetectedPausedRunning = pausedRunningJobs.get(hib3GrouperLoaderLog.getJobName());
    
    if (dateWhenDetectedPausedRunning != null) {
      if (hib3GrouperLoaderLog.getStartedTime().getTime() < dateWhenDetectedPausedRunning.getTime()) {
        throw new RuntimeException("Job " + hib3GrouperLoaderLog.getJobName() + " is marked as disabled (paused in Quartz).  This was likely done from the UI.");
      }
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

  /**
   * Set the overall grouper loader log in a threadlocal
   * @param hib3GrouperLoaderLog
   */
  public static void setThreadLocalHib3GrouperLoaderLogOverall(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    threadLocalHib3GrouperLoaderLogOverall.set(hib3GrouperLoaderLog);
  }
  
  /**
   * @return overall grouper loader log
   */
  public static Hib3GrouperLoaderLog getThreadLocalHib3GrouperLoaderLogOverall() {
    return threadLocalHib3GrouperLoaderLogOverall.get();
  }
  
  /**
   * Clear the overall grouper loader log in a threadlocal
   */
  public static void clearThreadLocalHib3GrouperLoaderLogOverall() {
    threadLocalHib3GrouperLoaderLogOverall.remove();
  }
}
