package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * attestation daemon
 */
@DisallowConcurrentExecution
public class GrouperDaemonSchedulerCheck extends OtherJobBase {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonSchedulerCheck.class);

  /**
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_schedulerCheckDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperDaemonSchedulerCheck().run(otherJobInput);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    List<String> badJobs = new ArrayList<String>();
        
    List<String> firedTriggerNames = HibernateSession.bySqlStatic().listSelect(String.class, "select trigger_name from grouper_QZ_FIRED_TRIGGERS", null, null);

    Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(Calendar.MINUTE, -1);
    long millis = calendar.getTimeInMillis();

    List<String> triggerNames = HibernateSession.bySqlStatic().listSelect(String.class, "select trigger_name from grouper_QZ_TRIGGERS where trigger_state = 'BLOCKED' and next_fire_time < ?", 
        GrouperUtil.toListObject(millis), HibUtils.listType(LongType.INSTANCE));
    
    for (String triggerName : triggerNames) {
      LOG.info("Found blocked trigger with name=" + triggerName + ".  Checking to see if it's being fired.");
      
      if (firedTriggerNames.contains(triggerName)) {
        LOG.info("Trigger with name=" + triggerName + " is being fired so the block may be okay.");
      } else {
        LOG.info("Trigger with name=" + triggerName + " is not being fired.  Updating trigger state.");
        badJobs.add(triggerName);
        
        HibernateSession.bySqlStatic().executeSql("update grouper_QZ_TRIGGERS set trigger_state='WAITING' where trigger_name=? and trigger_state='BLOCKED'",
            GrouperUtil.toListObject(triggerName), HibUtils.listType(StringType.INSTANCE));
      }
    }
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Fixed " + badJobs.size() + " jobs: " + badJobs.toString());
    otherJobInput.getHib3GrouperLoaderLog().store();

    LOG.info("GrouperDaemonSchedulerCheck finished successfully.");
    return null;
  }
}
