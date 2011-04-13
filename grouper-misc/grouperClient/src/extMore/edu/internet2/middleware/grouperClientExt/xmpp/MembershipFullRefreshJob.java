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
