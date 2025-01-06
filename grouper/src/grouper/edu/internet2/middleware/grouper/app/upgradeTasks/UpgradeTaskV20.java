package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.misc.GrouperVersion;

public class UpgradeTaskV20 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.12.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      List<TriggerKey> triggerKeys = new ArrayList<TriggerKey>();
      triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_cleanLogs"));
      triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_enabledDisabled"));
      triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_Messaging"));
      triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_externalSubjCalcFields"));
      triggerKeys.add(TriggerKey.triggerKey("triggerMaintenance_rules"));
      
      for (TriggerKey triggerKey : triggerKeys) {
        if (scheduler.checkExists(triggerKey)) {
          scheduler.unscheduleJob(triggerKey);
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", removed quartz trigger " + triggerKey.getName());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
