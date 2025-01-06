package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.misc.GrouperVersion;

public class UpgradeTaskV18 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.8.5");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();

      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("DEFAULT"))) {
        String jobName = jobKey.getName();
        if (jobName.startsWith("MAINTENANCE__groupSync__")) {
          String triggerName = "trigger_" + jobName;
          scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName));
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", removed quartz trigger " + triggerName);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
