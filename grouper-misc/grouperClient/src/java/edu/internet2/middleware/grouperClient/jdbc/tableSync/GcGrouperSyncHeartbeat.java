package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

/**
 * heartbeat and other logic for sync jobs
 * @author mchyzer
 *
 */
public class GcGrouperSyncHeartbeat {

  /**
   * if this job is interrupted from another job, this communicates that out to callers
   * check this while running
   */
  private boolean interrupted;
  
  /**
   * if this job is interrupted from another job, this communicates that out to callers
   * check this while running
   * @return if interrupted
   */
  public boolean isInterrupted() {
    return interrupted;
  }

  /**
   * if this is a full sync
   */
  private boolean fullSync = false;

  /**
   * 
   * @return if full sync
   */
  public boolean isFullSync() {
    return this.fullSync;
  }

  /**
   * 
   * @param fullSync1
   */
  public void setFullSync(boolean fullSync1) {
    this.fullSync = fullSync1;
  }

  /**
   * if done, stop thread, set this from caller to say done
   */
  private boolean done;
  
  /**
   * if done, stop thread, set this from caller to say done
   * @param done1
   */
  public void setDone(boolean done1) {
    this.done = done1;
  }

  /**
   * job
   */
  private GcGrouperSyncJob gcGrouperSyncJob;

  
  
  /**
   * job
   * @return job
   */
  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return this.gcGrouperSyncJob;
  }

  /**
   * job
   * @param gcGrouperSyncJob1
   */
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob1) {
    this.gcGrouperSyncJob = gcGrouperSyncJob1;
  }

  /**
   * 
   */
  public synchronized void runHeartbeatThread() {
    
    if (this.started) {
      throw new RuntimeException("This is already started!");
    }
    
    this.started = true;
    
    if (this.gcGrouperSyncJob == null) {
      throw new RuntimeException("Need to get job!");
    }
    
    this.thread = new Thread(new Runnable() {

      public void run() {
        
        try {
          while(true) {
            long loopStarted = System.currentTimeMillis();
            for (int i=0;i<60;i++) {
              if (GcGrouperSyncHeartbeat.this.done) {
                return;
              }
              // maybe 60 sleeps dont add up due to CPU
              if (System.currentTimeMillis()-loopStarted > 60000) {
                break;
              }
              Thread.sleep(1000);
              if (GcGrouperSyncHeartbeat.this.done) {
                return;
              }
            }
            if (!GcGrouperSyncHeartbeat.this.paused) {
              synchronized (GcGrouperSyncHeartbeat.this) {
                if (GcGrouperSyncHeartbeat.this.done) {
                  return;
                }
                
                // its been a minute, update the heartbeat, see if a more important job is running
                boolean shouldKeepRunning = GcGrouperSyncHeartbeat.this.gcGrouperSyncJob.assignHeartbeatAndCheckForPendingJobs(GcGrouperSyncHeartbeat.this.fullSync);
                
                // run heartbeat logics
                for (Runnable heartbeatLogic : GcGrouperSyncHeartbeat.this.heartbeatLogics) {
                  heartbeatLogic.run();
                }

                if (!shouldKeepRunning) {
                  GcGrouperSyncHeartbeat.this.interrupted=true;
                  // dont exit, we still need a heartbeat until the claler exits
                }
              }
            }
          }
        } catch (InterruptedException ie) {
          // we done
        } catch (Exception e) {
          LOG.error("Error assigning status and logging", e);
        } finally {
          // in case this is a problem, tell the caller to stop...  something wrong
          GcGrouperSyncHeartbeat.this.interrupted = true;
        }
        
      }
      
    });
    
    this.thread.setDaemon(true);
    this.thread.start();

  }
  
  /**
   * the thread
   */
  private Thread thread = null;
  
  /**
   * the thread
   * @return
   */
  public Thread getThread() {
    return this.thread;
  }

  /**
   * the thread
   * @param thread1
   */
  public void setThread(Thread thread1) {
    this.thread = thread1;
  }

  /**
   * add a logic to run periodically
   * @param heartbeatLogic
   */
  public void addHeartbeatLogic(Runnable heartbeatLogic) {
    this.heartbeatLogics.add(heartbeatLogic);
  }

  /**
   * add a logic to run periodically
   * @param heartbeatLogic
   */
  public void insertHeartbeatLogic(Runnable heartbeatLogic) {
    this.heartbeatLogics.add(0, heartbeatLogic);
  }

  /**
   * if this thread is started
   */
  private boolean started = false;
  
  /**
   * if is started
   * @return if started
   */
  public boolean isStarted() {
    return this.started;
  }

  /**
   * if this is paused, call this from caller to pause things
   */
  private boolean paused = false;
  
  /**
   * put heartbeat logic to kick off at heartbeat
   */
  private List<Runnable> heartbeatLogics = Collections.synchronizedList(new ArrayList<Runnable>());
  
  /**
   * log object
   */
  private static final Log LOG = LogFactory.getLog(GcGrouperSyncHeartbeat.class);

  public GcGrouperSyncHeartbeat() {
  }

  /**
   * if this is paused, call this from caller to pause things
   * @param paused1
   */
  public void setPaused(boolean paused1) {
    this.paused = paused1;
  }

  /**
   * end this heartbeat and wait for it
   * @param gcGrouperSyncHeartbeat
   */
  public static void endAndWaitForThread(GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat) {
    if (gcGrouperSyncHeartbeat != null) {
      gcGrouperSyncHeartbeat.setDone(true);
      if (gcGrouperSyncHeartbeat.getThread() != null) {
        try {
          gcGrouperSyncHeartbeat.getThread().interrupt();
        } catch (Exception e) {
          
        }
        GrouperClientUtils.join(gcGrouperSyncHeartbeat.getThread());
        
      }
    }

  }
}
