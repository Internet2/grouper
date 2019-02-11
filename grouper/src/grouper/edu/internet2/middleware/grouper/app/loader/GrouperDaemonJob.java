/**
 * Copyright 2019 Internet2
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

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperDaemonJob implements Job {

  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonJob.class);
      
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = context.getJobDetail().getKey().getName();

    String otherJobPrefix = GrouperLoaderType.OTHER_JOB.name() + "_";
    if (!jobName.startsWith(otherJobPrefix)) {
      throw new RuntimeException("Unexpected job name: " + jobName);
    }
    
    String jobKey = jobName.substring(otherJobPrefix.length());
    
    String jobClassName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobKey + ".class");
    
    Class<Job> jobClass = GrouperUtil.forName(jobClassName);
    Job job = GrouperUtil.newInstance(jobClass);
    
    LOG.info("Running job: " + jobName + ", class=" + jobClassName);
    job.execute(context);
  }

}
