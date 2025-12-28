package edu.internet2.middleware.grouper.app.provisioningExamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * https://docs.logicgate.com/
 */
public class LogicGateRecordsLoader extends GrouperProvisioningLoader {

  @Override
  public String getLoaderEntityTableName() {
    return "prov_logicgate_record";
  }

  @Override
  public List<String> getLoaderEntityColumnNames() {
    LogicGateRecordsProvisionerDao logicGateRecordsProvisionerDao = (LogicGateRecordsProvisionerDao)this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
    Set<String> userAttributes = logicGateRecordsProvisionerDao.getLogicGateCommands().getUserAttributes();
    
    List<String> userAttributesList = new ArrayList<>();

    userAttributesList.add("config_id");
    userAttributesList.add("id");
    
    for (String attr : userAttributes) {
      String cleaned = attr.replaceAll("[^A-Za-z0-9]", "_");
      userAttributesList.add(cleaned);
    }
    return userAttributesList;
  }

  @Override
  public List<String> getLoaderEntityKeyColumnNames() {
    return GrouperUtil.toList("config_id", "id");
  }

  @Override
  public List<Object[]> retrieveLoaderEntityTableDataFromDataBean() {
    
    List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();
    
    List<Object[]> result = new ArrayList<>();
    
    LogicGateRecordsProvisionerDao logicGateRecordsProvisionerDao = (LogicGateRecordsProvisionerDao)this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
    Set<String> userAttributes = logicGateRecordsProvisionerDao.getLogicGateCommands().getUserAttributes();
    int columnCount = this.getLoaderEntityColumnNames().size();
    
    for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
      
      Object[] row = new Object[columnCount];
      
      row[0] = this.getGrouperProvisioner().getConfigId();
      row[1] = targetProvisioningEntity.getId();
      
      // go through attributes and add each
      int index = 2;
      
      for (String attr : userAttributes) {
        String value = targetProvisioningEntity.retrieveAttributeValueString(attr);
        row[index] = value;
        index++;
      }
      
      result.add(row);
      
    }
    
    return result;
    
  }
}
