package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ExampleWsExternalSystem extends GrouperExternalSystem {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.exampleWsExternalSystem." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.exampleWsExternalSystem)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myExampleExternalSystem";
  }
  
  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    Map<String, Object> debugMap = new LinkedHashMap<>();
    
    /**
     * grouper.exampleWsExternalSystem.myExampleExternalSystem1.testSource = testSource
    grouper.exampleWsExternalSystem.myExampleExternalSystem1.testRole = testRole
    grouper.exampleWsExternalSystem.myExampleExternalSystem1.testNetIds = test1,test2
     */
    String netIds = GrouperConfig.retrieveConfig().propertyValueString("grouper.exampleWsExternalSystem." + this.getConfigId() + ".testNetIds");
    String source = GrouperConfig.retrieveConfig().propertyValueString("grouper.exampleWsExternalSystem." + this.getConfigId() + ".testSource");
    String role = GrouperConfig.retrieveConfig().propertyValueString("grouper.exampleWsExternalSystem." + this.getConfigId() + ".testRole");
    
    List<String> resultErrorMessages = new ArrayList<>();
    
    if (StringUtils.isBlank(netIds)) {
      resultErrorMessages.add("testNetIds is a required config for testing");
    }
    if (StringUtils.isBlank(source)) {
      resultErrorMessages.add("testSource is a required config for testing");
    }
    if (StringUtils.isBlank(role)) {
      resultErrorMessages.add("testRole is a required config for testing");
    }
    
    if (resultErrorMessages.size() != 0) {
      return resultErrorMessages;
    }
    
    List<String> netIdsList = GrouperUtil.splitTrimToList(netIds, ",");
    
    GrouperExampleWsTargetDao.replaceMembers(debugMap, this.getConfigId(), 
        netIdsList, source, role);

    return resultErrorMessages;
  }

}
