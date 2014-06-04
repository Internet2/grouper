/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * refresh grouper membership lists full after so often (e.g. daily)
 */
public class MembershipFullRefreshJob implements Job, StatefulJob {

  /**
   * logger
   */
  private static Log log = GrouperClientUtils.retrieveLog(MembershipFullRefreshJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  // @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = null;
    try {
      jobName = context.getJobDetail().getName();
      if (log.isDebugEnabled()) {
        log.debug("Full refresh on job: " + jobName);
      }
      if (jobName.startsWith("fullRefresh_")) {
        jobName = jobName.substring("fullRefresh_".length());
      } else {
        throw new RuntimeException("Job name should start with fullRefresh_: " + jobName);
      }
      GrouperClientXmppJob grouperClientXmppJob = GrouperClientXmppJob.retrieveJob(jobName, true);
      for (String groupName : grouperClientXmppJob.getGroupNames()) {
        GrouperClientXmppMain.fullRefreshGroup(grouperClientXmppJob, groupName);
      }
    } catch (RuntimeException re) {
      log.error("Error in job: " + jobName, re);
      throw re;
    }

  }

}
