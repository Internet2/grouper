/**
 * Copyright 2016 Internet2
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

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * @author shilen
 */
public class GrouperLoaderQuartzTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperLoaderQuartzTest(String name) {
    super(name);
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzStoringCronTrigger() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);

      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals("* * * * * ?", job1Trigger.getCronExpression());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzStoringSimpleTrigger() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .startAt(new Date(System.currentTimeMillis()))
        .withPriority(6)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(30)
            .repeatForever())
        .build();  
      
      GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger);

      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      SimpleTrigger job1Trigger = (SimpleTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals(30000, job1Trigger.getRepeatInterval());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzScheduleUpdateInterval() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .startAt(new Date(System.currentTimeMillis()))
        .withPriority(6)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(30)
            .repeatForever())
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));
      
      // no change
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .startAt(new Date(System.currentTimeMillis()))
        .withPriority(6)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(30)
            .repeatForever())
        .build();
      assertFalse(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // change interval
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .startAt(new Date(System.currentTimeMillis()))
        .withPriority(6)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(40)
            .repeatForever())
        .build();
        
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      SimpleTrigger job1Trigger = (SimpleTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals(40000, job1Trigger.getRepeatInterval());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzScheduleUpdateCronExpression() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // no change
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertFalse(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // update cron
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("1 * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));
      
      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals("1 * * * * ?", job1Trigger.getCronExpression());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzScheduleUpdateClass() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // no change
      jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      assertFalse(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // update class
      jobDetail = JobBuilder.newJob(GrouperLoaderOtherJobsTestJob1.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));
      
      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderOtherJobsTestJob1.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals("* * * * * ?", job1Trigger.getCronExpression());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzScheduleUpdatePriority() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // no change
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertFalse(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // update priority
      trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(4)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));
      
      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(4, job1Trigger.getPriority());
      assertEquals("* * * * * ?", job1Trigger.getCronExpression());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(2, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void testQuartzScheduleUpdateJobData() throws Exception {
    
    try {
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      JobDetail jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("triggerOtherJob_OTHER_JOB_1")
        .withPriority(6)
        .withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ?"))
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // no change
      jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 2)
        .build();
      
      assertFalse(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));

      // update job data
      jobDetail = JobBuilder.newJob(GrouperLoaderJob.class)
        .withIdentity("OTHER_JOB_1")
        .usingJobData("attr1", "value1")
        .usingJobData("attr2", 3)
        .build();
      
      assertTrue(GrouperLoader.scheduleJobIfNeeded(jobDetail, trigger));
      
      // verify
      JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_1"));
      assertEquals(GrouperLoaderJob.class.getName(), job1Detail.getJobClass().getName());

      assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).size());
      CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_1")).iterator().next();
      assertEquals("triggerOtherJob_OTHER_JOB_1", job1Trigger.getKey().getName());
      assertEquals(6, job1Trigger.getPriority());
      assertEquals("* * * * * ?", job1Trigger.getCronExpression());
      assertEquals("value1", job1Detail.getJobDataMap().getString("attr1"));
      assertEquals(3, job1Detail.getJobDataMap().getInt("attr2"));
      
    } finally {
      GrouperLoader.schedulerFactory().getScheduler().unscheduleJob(TriggerKey.triggerKey("triggerOtherJob_OTHER_JOB_1"));
      GrouperLoader.schedulerFactory().getScheduler().shutdown(true);
    }
  }
}