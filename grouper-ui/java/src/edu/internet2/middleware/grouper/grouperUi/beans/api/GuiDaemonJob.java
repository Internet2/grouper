/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import net.redhogs.cronparser.CronExpressionDescriptor;

/**
 * @author shilen
 */
public class GuiDaemonJob implements Serializable, Comparable<GuiDaemonJob> {

  private static final Log LOG = LogFactory.getLog(GuiDaemonJob.class);

  /**
   * 
   */
  private static final long serialVersionUID = 4685545018479996910L;
  
  /**
   * job name of loader job
   */
  private String jobName;
  
  /**
   * schedule for job
   */
  private String schedule;
  
  /**
   * state of job
   */
  private String state;
  
  /**
   * next fire time
   */
  private String nextFireTime;
  
  /**
   * prev fire time based on quartz data
   */
  private String prevFireTime;
  
  /**
   * Whether to show this additional action
   */
  private boolean showMoreActionsRunNow;
  
  /**
   * Whether to show this additional action
   */
  private boolean showMoreActionsEnable;
  
  /**
   * Whether to show this additional action
   */
  private boolean showMoreActionsDisable;
  
  /**
   * Last status based on loader log
   */
  private String lastRunStatus;
  
  /**
   * Last host based on loader log
   */
  private String lastRunHost;
  
  /**
   * Last start time based on loader log
   */
  private String lastRunStartTime;
  
  /**
   * last summary based on loader log
   */
  private String lastRunSummary;
  
  
  /**
   * last running time based on loader log
   */
  private String lastRunTotalTime;
  
  /**
   * @param jobName
   */
  public GuiDaemonJob(String jobName) {

    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
  
      this.setJobName(jobName);
      
      Date nextFireTime = null;
      Date prevFireTime = null;
      
      boolean isEnabled = false;
      Date startTimeIfRunning = GrouperLoader.internal_getJobStartTimeIfRunning(jobName);
      boolean isRunning = startTimeIfRunning != null;
      
      List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(jobName));
      StringBuilder schedule = new StringBuilder();
  
      for (Trigger trigger : triggers) {
        
        Date currPrevFireTime = trigger.getPreviousFireTime();
        if (currPrevFireTime != null) {
          if (prevFireTime == null || currPrevFireTime.after(prevFireTime)) {
            prevFireTime = currPrevFireTime;
          }
        }
        
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        if (triggerState == Trigger.TriggerState.COMPLETE) {
          // looks like this trigger is done so skip it
          continue;
        }
        
        if (triggerState != Trigger.TriggerState.PAUSED) {
          isEnabled = true;
        }
        
        if (schedule.length() > 0) {
          schedule.append("<br /><br />");
        }
                  
        Date currNextFireTime = trigger.getNextFireTime();
        if (currNextFireTime != null) {
          if (nextFireTime == null || currNextFireTime.before(nextFireTime)) {
            nextFireTime = currNextFireTime;
          }
        }
                  
        if (trigger instanceof SimpleTrigger) {
          int repeatCount = ((SimpleTrigger)trigger).getRepeatCount();
          if (repeatCount == 0) {
            schedule.append("ONE TIME: ");
            Date startAt = ((SimpleTrigger)trigger).getStartTime();
            if (startAt != null) {
              schedule.append(sdf.format(startAt));
            }
          } else {
            if (repeatCount == -1) {
              schedule.append("INTERVAL: ");
            } else {
              schedule.append("INTERVAL (COUNT ").append(repeatCount).append("): ");
            }
            Long intervalSeconds = ((SimpleTrigger)trigger).getRepeatInterval();
            schedule.append(intervalSeconds).append(" ").append(TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlScheduleIntervalSeconds"));
            schedule.append("<br />" + GrouperUiUtils.convertSecondsToString(intervalSeconds.intValue()));
          }
        } else if (trigger instanceof CronTrigger) {
          schedule.append("CRON: ");
          String cron = ((CronTrigger)trigger).getCronExpression();
          schedule.append(GrouperUiUtils.escapeHtml(cron, true)).append("<br />");
          if (!StringUtils.isEmpty(cron)) {
            try {
              schedule.append(GrouperUiUtils.escapeHtml(CronExpressionDescriptor.getDescription(cron), true));
            } catch (Exception e) {
              LOG.error("Cant parse cron string:" + cron, e);                
              schedule.append(TextContainer.retrieveFromRequest().getText().get("adminDaemonJobsCronDescriptionError"));
            }
          }
        } else {
          LOG.warn("Unsupported trigger: " + trigger.getKey().getName());
        }
      }
      
      this.setSchedule(schedule.toString());
      
      if (isEnabled && nextFireTime != null) {
        this.setNextFireTime(sdf.format(nextFireTime));
      }
      
      if (prevFireTime != null) {
        // if it's currently running, the time there is more accurate
        if (isRunning) {
          this.setPrevFireTime(sdf.format(startTimeIfRunning));
        } else {
          this.setPrevFireTime(sdf.format(prevFireTime));
        }
      }
      
      if (isRunning) {
        Long timeRunningSeconds = (System.currentTimeMillis() - startTimeIfRunning.getTime()) / 1000;
        String currentState = TextContainer.retrieveFromRequest().getText().get("adminDaemonJobsStateRunning") +
            "<br />" + GrouperUiUtils.convertSecondsToString(timeRunningSeconds.intValue());
        this.setState(currentState);
      } else if (isEnabled) {
        this.setState(TextContainer.retrieveFromRequest().getText().get("adminDaemonJobsStateEnabled"));
      } else {
        this.setState(TextContainer.retrieveFromRequest().getText().get("adminDaemonJobsStateDisabled"));
      }
      
      if (!isRunning && isEnabled) {
        this.setShowMoreActionsRunNow(true);
      }
      
      if (isEnabled) {
        this.setShowMoreActionsDisable(true);
      } else {
        this.setShowMoreActionsEnable(true);
      }
      
      {
        List<Criterion> criterionList = new ArrayList<Criterion>();
                
        criterionList.add(Restrictions.eq("jobName", jobName));
        criterionList.add(Restrictions.ne("status", "STARTED"));
  
        QueryOptions queryOptions = QueryOptions.create("lastUpdated", false, 1, 1);
        
        Criterion allCriteria = HibUtils.listCrit(criterionList);
        
        List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byCriteriaStatic()
          .options(queryOptions).list(Hib3GrouperLoaderLog.class, allCriteria);
        
        if (loaderLogs.size() > 0) {
          Hib3GrouperLoaderLog firstLoaderLog = loaderLogs.get(0);
          this.setLastRunStatus(firstLoaderLog.getStatus());
          this.setLastRunHost(firstLoaderLog.getHost());
          
          if (firstLoaderLog.getStartedTime() != null) {
            this.setLastRunStartTime(sdf.format(new Date(firstLoaderLog.getStartedTime().getTime())));
          }
  
          if (firstLoaderLog.getMillis() != null) {
            this.setLastRunTotalTime(GrouperUiUtils.convertSecondsToString((firstLoaderLog.getMillis() / 1000)));
          }
          
          this.setLastRunSummary(firstLoaderLog.getTotalCount() + " total records, " + firstLoaderLog.getInsertCount() + " inserts, " + firstLoaderLog.getDeleteCount() + " deletes, " + firstLoaderLog.getUpdateCount() + " updates");
        }
      }
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(GuiDaemonJob o) {
    if (o == null) {
      return -1;
    }
    if (StringUtils.equals(this.getJobName(), o.getJobName())) {
      return 0;
    }
    if (o.getJobName() == null) {
      return -1;
    }
    if (this.getJobName() == null) {
      return 1;
    }
    return this.getJobName().compareTo(o.getJobName());
  }

  
  /**
   * @return the jobName
   */
  public String getJobName() {
    return jobName;
  }

  
  /**
   * @param jobName the jobName to set
   */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }


  
  /**
   * @return the schedule
   */
  public String getSchedule() {
    return schedule;
  }


  
  /**
   * @param schedule the schedule to set
   */
  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }


  
  /**
   * @return the nextFireTime
   */
  public String getNextFireTime() {
    return nextFireTime;
  }


  
  /**
   * @param nextFireTime the nextFireTime to set
   */
  public void setNextFireTime(String nextFireTime) {
    this.nextFireTime = nextFireTime;
  }


  
  /**
   * @return the state
   */
  public String getState() {
    return state;
  }


  
  /**
   * @param state the state to set
   */
  public void setState(String state) {
    this.state = state;
  }


  
  /**
   * @return the prevFireTime
   */
  public String getPrevFireTime() {
    return prevFireTime;
  }


  
  /**
   * @param prevFireTime the prevFireTime to set
   */
  public void setPrevFireTime(String prevFireTime) {
    this.prevFireTime = prevFireTime;
  }


  
  /**
   * @return the showMoreActionsRunNow
   */
  public boolean isShowMoreActionsRunNow() {
    return showMoreActionsRunNow;
  }


  
  /**
   * @param showMoreActionsRunNow the showMoreActionsRunNow to set
   */
  public void setShowMoreActionsRunNow(boolean showMoreActionsRunNow) {
    this.showMoreActionsRunNow = showMoreActionsRunNow;
  }


  
  /**
   * @return the showMoreActionsEnable
   */
  public boolean isShowMoreActionsEnable() {
    return showMoreActionsEnable;
  }


  
  /**
   * @param showMoreActionsEnable the showMoreActionsEnable to set
   */
  public void setShowMoreActionsEnable(boolean showMoreActionsEnable) {
    this.showMoreActionsEnable = showMoreActionsEnable;
  }


  
  /**
   * @return the showMoreActionsDisable
   */
  public boolean isShowMoreActionsDisable() {
    return showMoreActionsDisable;
  }


  
  /**
   * @param showMoreActionsDisable the showMoreActionsDisable to set
   */
  public void setShowMoreActionsDisable(boolean showMoreActionsDisable) {
    this.showMoreActionsDisable = showMoreActionsDisable;
  }


  
  /**
   * @return the lastRunStatus
   */
  public String getLastRunStatus() {
    return lastRunStatus;
  }


  
  /**
   * @param lastRunStatus the lastRunStatus to set
   */
  public void setLastRunStatus(String lastRunStatus) {
    this.lastRunStatus = lastRunStatus;
  }


  
  /**
   * @return the lastRunHost
   */
  public String getLastRunHost() {
    return lastRunHost;
  }


  
  /**
   * @param lastRunHost the lastRunHost to set
   */
  public void setLastRunHost(String lastRunHost) {
    this.lastRunHost = lastRunHost;
  }


  
  /**
   * @return the lastRunSummary
   */
  public String getLastRunSummary() {
    return lastRunSummary;
  }


  
  /**
   * @param lastRunSummary the lastRunSummary to set
   */
  public void setLastRunSummary(String lastRunSummary) {
    this.lastRunSummary = lastRunSummary;
  }


  
  /**
   * @return the lastRunTotalTime
   */
  public String getLastRunTotalTime() {
    return lastRunTotalTime;
  }


  
  /**
   * @param lastRunTotalTime the lastRunTotalTime to set
   */
  public void setLastRunTotalTime(String lastRunTotalTime) {
    this.lastRunTotalTime = lastRunTotalTime;
  }


  
  /**
   * @return the lastRunStartTime
   */
  public String getLastRunStartTime() {
    return lastRunStartTime;
  }


  
  /**
   * @param lastRunStartTime the lastRunStartTime to set
   */
  public void setLastRunStartTime(String lastRunStartTime) {
    this.lastRunStartTime = lastRunStartTime;
  }
}
