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

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * @author shilen
 */
public class GrouperLoaderOtherJobsTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperLoaderOtherJobsTest(String name) {
    super(name);
  }
  
  /**
   * @throws Exception
   */
  public void testScheduleJobs() throws Exception {
    try {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test1.class", "edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob1");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test1.quartzCron", "1 2 3 * * ?");
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test2.class", "edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob2");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test2.quartzCron", "4 5 6 * * ?");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test2.priority", "7");
      
      GrouperLoader.scheduleOtherJobs();
      
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      
      /** Verify jobs **/
      {
        JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test1"));
        assertEquals("edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob1", job1Detail.getJobClass().getName());

        assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).size());
        CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).iterator().next();
        assertEquals("triggerOtherJob_OTHER_JOB_test1", job1Trigger.getKey().getName());
        assertEquals(5, job1Trigger.getPriority());
        assertEquals("1 2 3 * * ?", job1Trigger.getCronExpression());
      }
      
      {
        JobDetail job2Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test2"));
        assertEquals("edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob2", job2Detail.getJobClass().getName());
  
        assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test2")).size());
        CronTrigger job2Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test2")).iterator().next();
        assertEquals("triggerOtherJob_OTHER_JOB_test2", job2Trigger.getKey().getName());
        assertEquals(7, job2Trigger.getPriority());
        assertEquals("4 5 6 * * ?", job2Trigger.getCronExpression());
      }
      
      /** Make changes and verify again **/
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test1.quartzCron", "1 2 4 * * ?");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.test2.priority", "8");
      GrouperLoader.scheduleOtherJobs();

      {
        JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test1"));
        assertEquals("edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob1", job1Detail.getJobClass().getName());
  
        assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).size());
        CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).iterator().next();
        assertEquals("triggerOtherJob_OTHER_JOB_test1", job1Trigger.getKey().getName());
        assertEquals(5, job1Trigger.getPriority());
        assertEquals("1 2 4 * * ?", job1Trigger.getCronExpression());
      }
      
      {
        JobDetail job2Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test2"));
        assertEquals("edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob2", job2Detail.getJobClass().getName());
  
        assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test2")).size());
        CronTrigger job2Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test2")).iterator().next();
        assertEquals("triggerOtherJob_OTHER_JOB_test2", job2Trigger.getKey().getName());
        assertEquals(8, job2Trigger.getPriority());
        assertEquals("4 5 6 * * ?", job2Trigger.getCronExpression());
      }
      
      /** Remove a job and verify **/
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.class");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.quartzCron");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.priority");
      GrouperLoader.scheduleOtherJobs();

      {
        JobDetail job1Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test1"));
        assertEquals("edu.internet2.middleware.grouper.app.loader.GrouperLoaderOtherJobsTestJob1", job1Detail.getJobClass().getName());
  
        assertEquals(1, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).size());
        CronTrigger job1Trigger = (CronTrigger)scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test1")).iterator().next();
        assertEquals("triggerOtherJob_OTHER_JOB_test1", job1Trigger.getKey().getName());
        assertEquals(5, job1Trigger.getPriority());
        assertEquals("1 2 4 * * ?", job1Trigger.getCronExpression());
      }
      
      {
        JobDetail job2Detail = scheduler.getJobDetail(new JobKey("OTHER_JOB_test2"));
        assertNull(job2Detail);
  
        assertEquals(0, scheduler.getTriggersOfJob(new JobKey("OTHER_JOB_test2")).size());
      }
    } finally {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test1.class");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test1.quartzCron");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.class");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.quartzCron");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("otherJob.test2.priority");

      GrouperLoader.scheduleOtherJobs();
    }
  }
}