/**
 * Copyright 2012 Internet2
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
/*
 * @author mchyzer
 * $Id: GrouperLoaderScheduleType.java,v 1.2 2008-10-30 20:57:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * schedule types for loaders.  note that two jobs never run at the same time.
 * If the interval or schedule is up, and a job is still running, then another
 * will not run concurrently.
 */
public enum GrouperLoaderScheduleType {

  /**
   * quartz cron string to configure the schedule
   * http://www.opensymphony.com/quartz/wikidocs/TutorialLesson6.html
   */
  CRON {
    
    /**
     * create a trigger based on the type (this), and the params (e.g. cron string, interval seconds, etc)
     * @param quartzCronString
     * @param intervalSeconds
     * @return the trigger
     */
    @Override
    public Trigger createTrigger(String quartzCronString, Integer intervalSeconds) {
      CronTrigger cronTrigger = new CronTrigger();
      try {
        cronTrigger.setCronExpression(quartzCronString);
      } catch (ParseException pe) {
        throw new RuntimeException("Problems parsing: '" + quartzCronString + "'", pe);
      }
      return cronTrigger;
    }
  },

  /**
   * periodic interval, based on the time between the start of one job, and the
   * start of another job
   */
  START_TO_START_INTERVAL {
    
    /**
     * create a trigger based on the type (this), and the params (e.g. cron string, interval seconds, etc)
     * @param quartzCronString
     * @param intervalSeconds
     * @return the trigger
     */
    @Override
    public Trigger createTrigger(String quartzCronString, Integer intervalSeconds) {
      SimpleTrigger simpleTrigger = new SimpleTrigger();
      simpleTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

      //default to daily
      intervalSeconds = GrouperUtil.defaultIfNull(intervalSeconds, 60*60*24);
      simpleTrigger.setRepeatInterval(intervalSeconds * 1000);
      
      //start time is the interval seconds / 5, rand
      int startSeconds = (int)(Math.random() * intervalSeconds);
      Date startTime = new Date(System.currentTimeMillis() + ((long)startSeconds*1000));
      
      simpleTrigger.setStartTime(startTime);
      return simpleTrigger;
    }
  };
  
  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderScheduleType.class);

  /**
   * create a trigger based on the type (this), and the params (e.g. cron string, interval seconds, etc)
   * @param quartzCronString
   * @param intervalSeconds
   * @return the trigger
   */
  public abstract Trigger createTrigger(String quartzCronString, Integer intervalSeconds);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperLoaderScheduleType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperLoaderScheduleType.class, 
        string, exceptionOnNull);

  }

}
