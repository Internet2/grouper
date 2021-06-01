/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


/**
 *
 */
public abstract class OtherJobBase implements Job {

  /**
   * other job input
   */
  private OtherJobInput otherJobInput = null;
  
  /**
   * other job input
   * @return
   */
  public OtherJobInput getOtherJobInput() {
    return otherJobInput;
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(OtherJobBase.class);

  /**
   * only update for 24 hours, then evict
   * other job log updaters --> otherjob class, millis1970 was added as updater, millis1970 last updated
   */
  private static Map<OtherJobLogUpdater, MultiKey> otherJobLogUpdaters = new ConcurrentHashMap<OtherJobLogUpdater, MultiKey>();

  /**
   * this is true when work is happening
   */
  private static boolean otherJobLogUpdaterWorking = false;
  
  /**
   * this is true when work is happening
   */
  private static Thread otherJobLogUpdaterThread = null;
  
  /**
   * 
   * @param otherJobInput
   */
  public void setOtherJobInput(OtherJobInput otherJobInput) {
    this.otherJobInput = otherJobInput;
  }

  public static void otherJobLogUpdaterInit() {
    if (otherJobLogUpdaterThread == null) {
      synchronized (OtherJobBase.class) {
        if (otherJobLogUpdaterThread == null) {
          otherJobLogUpdaterThread = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                long millisLastStarted = -1;
                
                while (true) {
                  
                  otherJobLogUpdaterWorking = false;

                  // allow an exit
                  if (!GrouperConfig.retrieveConfig().propertyValueBoolean("grouperOtherJobLogUpdaterRun", true)) {
                    return;
                  }
                  
                  int secondsToUpdateLoaderLog = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.otherJobUpdateLoaderLogDbAfterSeconds", 60);
                  secondsToUpdateLoaderLog = Math.min(1, secondsToUpdateLoaderLog);
                  int millisToUpdateLoaderLog = secondsToUpdateLoaderLog * 1000;
                  
                  long sleepForMillis = millisLastStarted <= 0 ? millisToUpdateLoaderLog : (millisToUpdateLoaderLog - (System.currentTimeMillis() - millisLastStarted));
                  sleepForMillis = Math.min(1, sleepForMillis);
                  sleepForMillis = Math.max(millisToUpdateLoaderLog, sleepForMillis);
                  GrouperUtil.sleep(sleepForMillis);
                  //Systemoutprintln("sleepForMillis: " + sleepForMillis);
                  millisLastStarted = System.currentTimeMillis();
                  
                  // lets see if there is work to do
                  if (otherJobLogUpdaters.size() > 0) {
                    
                    // tell removes to wait
                    otherJobLogUpdaterWorking = true;
                    
                    Iterator<OtherJobLogUpdater> iterator = otherJobLogUpdaters.keySet().iterator();
                    
                    List<Hib3GrouperLoaderLog> hib3GrouperLoaderLogsToUpdate = new ArrayList<Hib3GrouperLoaderLog>();
                    
                    while (iterator.hasNext()) {
                      
                      OtherJobLogUpdater otherJobLogUpdater = iterator.next();
                      try {
                        
                        MultiKey otherJobWhenAddedLastUpdated = otherJobLogUpdaters.get(otherJobLogUpdater);
                        OtherJobBase otherJobBase = (OtherJobBase)otherJobWhenAddedLastUpdated.getKey(0);
                        long whenAdded = (Long)otherJobWhenAddedLastUpdated.getKey(1);
                        long lastUpdated = (Long)otherJobWhenAddedLastUpdated.getKey(2);
                        
                        //if added more than 1 day ago lets give up
                        if (millisLastStarted - whenAdded > 1000 * 60 * 60 * 24L) {
                          iterator.remove();
                          continue;
                        }

                        //if updated recently (less than 29 seconds ago then ignore
                        if (lastUpdated > 10  && millisLastStarted - lastUpdated < 1000L * Math.min(1, -1 + secondsToUpdateLoaderLog/2)) {
                          // dont remove just skip
                          continue;
                        }

                        // if things arent there just ignore
                        OtherJobInput otherJobInput = otherJobBase.getOtherJobInput();
                        if (otherJobInput == null) {
                          iterator.remove();
                          continue;
                        }
                        Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
                        GrouperLoaderStatus grouperLoaderStatus = GrouperLoaderStatus.valueOfIgnoreCase(hib3GrouperLoaderLog.getStatus(), true);
                        // if the job is not there or its done
                        if (hib3GrouperLoaderLog == null ||  (grouperLoaderStatus != GrouperLoaderStatus.RUNNING && grouperLoaderStatus != GrouperLoaderStatus.STARTED)) {
                          iterator.remove();
                          continue;
                        }
                        long thisLogUpdaterStarted = System.currentTimeMillis();

                        //lets update
                        otherJobLogUpdater.changeLoaderLogJavaObjectWithoutStoringToDb();
                        
                        // get again just in case?
                        hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();

                        
                        if (hib3GrouperLoaderLog == null ||  (grouperLoaderStatus != GrouperLoaderStatus.RUNNING && grouperLoaderStatus != GrouperLoaderStatus.STARTED)) {
                          iterator.remove();
                          continue;
                        }

                        hib3GrouperLoaderLogsToUpdate.add(hib3GrouperLoaderLog);

                        // if something takes longer than a second, then dont include it
                        if (System.currentTimeMillis() - thisLogUpdaterStarted > 1000) {
                          iterator.remove();
                          continue;
                        }

                      } catch (RuntimeException re) {
                        LOG.error("Error updating daemon log: " + otherJobLogUpdater, re);
                        try {
                          iterator.remove();
                        } catch (Exception e) {}
                      }
                    }
                    
                    if (hib3GrouperLoaderLogsToUpdate.size() > 0) {
                      HibernateSession.byObjectStatic().update(hib3GrouperLoaderLogsToUpdate);
                    }
                  }
                  
                }
              } catch (RuntimeException e) {
                LOG.error("Error in other job log updater", e);
                // throw into the ether since thread
                throw e;
              } finally {
                otherJobLogUpdaterWorking = false;
              }
            }
          });
          otherJobLogUpdaterThread.start();  
          
        }
      }
    }
    

  }
  
  /**
   * do not register an updater if you are updating this object too...  might be fine but might have some contention.
   * deregister in finally block
   * @param otherJobLogUpdater
   */
  public void otherJobLogUpdaterRegister(OtherJobLogUpdater otherJobLogUpdater) {
    
    otherJobLogUpdaterInit();
    
    if (this.getOtherJobInput() == null || this.getOtherJobInput().getHib3GrouperLoaderLog() == null) {
      // not sure if there is a legitimate case where this could happen
      LOG.error("Not other job input found when registering other job log updater!");
      return;
    }
    otherJobLogUpdaters.put(otherJobLogUpdater, new MultiKey(this, System.currentTimeMillis(), 
        GrouperUtil.longValue(this.getOtherJobInput().getHib3GrouperLoaderLog().getLastUpdated(), -1)));

  }

  /**
   * 
   * @param otherJobLogUpdater
   */
  public void otherJobLogUpdaterDeregister(OtherJobLogUpdater otherJobLogUpdater) {
    otherJobLogUpdaters.remove(otherJobLogUpdater);

    // the worker will take a break for a minute, in that time, this hib3 log is not being updated
    for (int i=0;i<600;i++) {
      if (!otherJobLogUpdaterWorking) {
        break;
      }
      GrouperUtil.sleep(100);
    }
  }

  /**
   * 
   */
  public static class OtherJobInput {
    
    /**
     * grouper session
     */
    private GrouperSession grouperSession;
    
    /**
     * @return the grouperSession
     */
    public GrouperSession getGrouperSession() {
      return this.grouperSession;
    }
    
    /**
     * @param grouperSession the grouperSession to set
     */
    public void setGrouperSession(GrouperSession grouperSession) {
      this.grouperSession = grouperSession;
    }

    /**
     * job name, e.g. OTHER_JOB_attestationDaemon
     */
    private String jobName;

    
    /**
     * @return the jobName
     */
    public String getJobName() {
      return this.jobName;
    }

    
    /**
     * @param jobName the jobName to set
     */
    public void setJobName(String jobName) {
      this.jobName = jobName;
    }
    
    /**
     * loader log for status
     */
    private Hib3GrouperLoaderLog hib3GrouperLoaderLog;
    
    /**
     * @return the hib3GrouperLoaderLog
     */
    public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
      return this.hib3GrouperLoaderLog;
    }
    
    /**
     * @param hib3GrouperLoaderLog1 the hib3GrouperLoaderLog to set
     */
    public void setHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
      this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
    }
    
  }
  
  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  public static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
      boolean throwException, long startTime) {
    //store this safely
    try {
      
      long endTime = System.currentTimeMillis();
      hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
      hib3GrouploaderLog.setMillis((int)(endTime-startTime));
      
      hib3GrouploaderLog.store();
      
    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }


  
  /**
   * 
   */
  public static class OtherJobOutput {
    
  }
  
  /**
   * 
   */
  public OtherJobBase() {
  }

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  final public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = null;
    jobName = context.getJobDetail().getKey().getName();

    if (GrouperLoader.isJobRunning(jobName)) {
      LOG.warn("Data in grouper_loader_log suggests that job " + jobName + " is currently running already.  Aborting this run.");
      return;
    }

    try {
      execute(jobName, null);
    } catch (Exception e) {
      //make sure this is job execution exception
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      throw (JobExecutionException)e;
    }
  }
    
  /**
   * @param jobName e.g. OTHER_JOB_attestationDaemon
   * @param hib3GrouperLoaderLog if null create a new one
   */
  final public void execute(final String jobName, final Hib3GrouperLoaderLog hib3GrouperLoaderLogInput) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        long startTime = System.currentTimeMillis();
        boolean assignedContext = false;
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = hib3GrouperLoaderLogInput;
        try {
          if (GrouperContext.retrieveDefaultContext() == null) {
            GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
            assignedContext = true;
          }
          boolean madeChange = false;
          if (hib3GrouperLoaderLog == null) {
            hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getJobName())) {
            hib3GrouperLoaderLog.setJobName(jobName);
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getHost())) {
            hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
            madeChange = true;
          }
          if (null == hib3GrouperLoaderLog.getStartedTime()) {
            hib3GrouperLoaderLog.setStartedTime(new Timestamp(startTime));
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getJobType())) {
            hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
            madeChange = true;
          }
          if (StringUtils.isBlank(hib3GrouperLoaderLog.getStatus())) {
            hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
            madeChange = true;
          }
          if (madeChange) {
            hib3GrouperLoaderLog.store();
          }
          
          OtherJobInput otherJobInput = new OtherJobInput();
          
          otherJobInput.setJobName(jobName);
          otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
          otherJobInput.setGrouperSession(grouperSession);
          OtherJobBase.this.otherJobInput = otherJobInput;
          OtherJobBase.this.run(otherJobInput);
          
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
          storeLogInDb(hib3GrouperLoaderLog, true, startTime);
          
        } catch (RuntimeException e) {
          LOG.error("Error occurred while running job: " + jobName, e);
          hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          hib3GrouperLoaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));
          
          storeLogInDb(hib3GrouperLoaderLog, false, startTime);
          throw e;
        } finally {
          if (assignedContext) {
            GrouperContext.deleteDefaultContext();
          }
        }

        return null;
      }
    });
    
  }

  /**
   * implement this method for the logic of the OtherJob
   * @param otherJobInput
   * @return the output
   */
  public abstract OtherJobOutput run(OtherJobInput otherJobInput);
  
}
