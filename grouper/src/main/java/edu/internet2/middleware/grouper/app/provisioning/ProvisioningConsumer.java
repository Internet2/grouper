package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * real time provisioning listener
 * @author mchyzer
 *
 */
public class ProvisioningConsumer extends ProvisioningSyncConsumer {

  public ProvisioningConsumer() {
  }

  private GrouperProvisioner grouperProvisioner;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  @Override
  public Integer getBatchSize() {
    return 100000;
  }

  /**
   * some change log consumers might want to be called even if nothing happened in change log
   * e.g. check messages in provisioners
   */
  @Override
  public boolean callAtLeastOnce() {
    return true;
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
    
    // not sure why this would happen... if there are no change log events but there might be
    // messages
//    if (GrouperUtil.length(esbEventContainers) == 0) {
//      return null;
//    }
    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();

    Map<String, Object> debugMapOverall = this.getEsbConsumer().getDebugMapOverall();
    
    String provisioningConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." + this.getChangeLogProcessorMetadata().getConsumerName() + ".provisionerConfigId");
    
    this.grouperProvisioner = GrouperProvisioner.retrieveProvisioner(provisioningConfigId);
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = ProvisioningConsumer.this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    this.grouperProvisioner.retrieveGrouperProvisioningOutput().setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    grouperProvisioner.setJobName(hib3GrouperLoaderLog.getJobName());
    grouperProvisioner.setProvisioningConsumer(this);
    grouperProvisioner.setDebugMap(debugMapOverall);
    grouperProvisioner.getGcGrouperSyncHeartbeat().insertHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
        grouperProvisioningOutput.copyToHib3LoaderLog();
        hib3GrouperLoaderLog.store();
      }
      
    });
    
    //TODO get provisioning type from config
    GrouperProvisioningType grouperProvisioningType = GrouperProvisioningType.incrementalProvisionChangeLog;
    grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().setEsbEventContainers(esbEventContainers);

    // if we didnt throw an exception, then we processed all of them
    // get the last sequence before the provisioner re-orders them
    Long lastSequenceNumber = -1L;
    if (GrouperUtil.length(esbEventContainers) > 0) {
      EsbEventContainer lastEvent = esbEventContainers.get(esbEventContainers.size()-1);
      lastSequenceNumber = lastEvent.getSequenceNumber();
    }

    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(grouperProvisioningType);
    grouperProvisioningOutput.copyToHib3LoaderLog();
    hib3GrouperLoaderLog.store();

    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(lastSequenceNumber);
    return provisioningSyncConsumerResult;
  }

  @Override
  public void disconnect() {

  }

}
