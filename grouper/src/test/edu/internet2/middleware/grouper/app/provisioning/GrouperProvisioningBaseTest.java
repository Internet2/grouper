package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Base test for provisioning
 */
public abstract class GrouperProvisioningBaseTest extends GrouperTest {
  
  public GrouperProvisioningBaseTest(String name) {
    super(name);
  }
  
  public GrouperProvisioningBaseTest() {
    super();
  }

  public abstract String defaultConfigId(); 
  
  /**
   * @param configId
   * @return GrouperProvisioningOutput
   */
  public GrouperProvisioningOutput fullProvision() {
    return fullProvision(defaultConfigId());
  }

  /**
   * @param configId
   * @return GrouperProvisioningOutput
   */
  public GrouperProvisioningOutput fullProvision(String configId) {
    return fullProvision(configId, false);
  }

  /**
   * @param configId
   * @return GrouperProvisioningOutput
   */
  public GrouperProvisioningOutput fullProvision(String configId, boolean allowErrors) {
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_provisioner_full_" + configId);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput(); 

    GrouperUtil.sleep(1000);
    if (!allowErrors) {
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    }
   
    return grouperProvisioningOutput;
  }
  
  /**
   * @return GrouperProvisioningOutput
   */
  public Hib3GrouperLoaderLog incrementalProvision() {
    return incrementalProvision(defaultConfigId(), true, true, false);
  }
  
  /**
   * @param runChangeLog
   * @param runConsumer
   * @return GrouperProvisioningOutput
   */
  public Hib3GrouperLoaderLog incrementalProvision(String configId, boolean runChangeLog, boolean runConsumer) {
    return incrementalProvision(configId, runChangeLog, runConsumer, false);
  }
  
  /**
   * @param runChangeLog
   * @param runConsumer
   * @return GrouperProvisioningOutput
   */
  public Hib3GrouperLoaderLog incrementalProvision(String configId, boolean runChangeLog, boolean runConsumer, boolean allowErrors) {
    
    // wait for message cache to clear
    GrouperUtil.sleep(10000);

    if (runChangeLog) {
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    }
    
    if (runConsumer) {
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_provisioner_incremental_" + configId);

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

      GrouperUtil.sleep(1000);
      
      if (!allowErrors) {
        assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      }
      
      return Hib3GrouperLoaderLog.retrieveMostRecentLog("CHANGE_LOG_consumer_provisioner_incremental_" + configId);
    }
    
    
    //  if (runChangeLog) {
    //    ChangeLogTempToEntity.convertRecords();
    //  }
    //  
    //  if (runConsumer) {
    //    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    //    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    //    hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_ldapProvTestCLC");
    //    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    //    EsbConsumer esbConsumer = new EsbConsumer();
    //    ChangeLogHelper.processRecords("ldapProvTestCLC", hib3GrouploaderLog, esbConsumer);
    //
    //    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    //    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    //    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    //    
    //    return hib3GrouploaderLog;
    //  }

    
    return null;

    
  }
  
}
