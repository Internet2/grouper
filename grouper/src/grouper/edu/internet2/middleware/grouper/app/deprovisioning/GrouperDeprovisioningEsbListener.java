package edu.internet2.middleware.grouper.app.deprovisioning;

import static edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings.deprovisioningStemName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperDeprovisioningEsbListener extends EsbListenerBase {

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers) {

    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    boolean runFullSync = false;
    
    Set<String> attributeAssignIds = new HashSet<String>();
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      String attributeDefNameName = esbEventContainer.getEsbEvent().getAttributeDefNameName();
      String attributeAssignId = esbEventContainer.getEsbEvent().getAttributeAssignId();
      
      //add attribute Assign Id to the set if it is not blank
      if (StringUtils.isNotBlank(attributeDefNameName) &&
          StringUtils.startsWith(attributeDefNameName, deprovisioningStemName()) && StringUtils.isNotBlank(attributeAssignId)) {
        attributeAssignIds.add(attributeAssignId);
      }
    }
    
    List<String> deprovisioningStemAttributeAssignIds = new GcDbAccess().sql("select attribute_assign_id2 from grouper_aval_asn_asn_stem_v gaaasv")
      .selectMultipleColumnName("attribute_assign_id2").addBindVars(attributeAssignIds).selectList(String.class);
    
    if (deprovisioningStemAttributeAssignIds.size() > 0) {
      GrouperDeprovisioningDaemonLogic.fullSyncLogic();
      runFullSync = true;
    }
    
    
    if (!runFullSync) {      
      new GrouperDeprovisioningDaemonLogic().incrementalLogic(esbEventContainers);
    }
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    
    return provisioningSyncConsumerResult;
    
  }
  
}
