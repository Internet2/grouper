package edu.internet2.middleware.grouper.app.remedy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyApiCommands;
import edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyGroup;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class RemedyGrouperExternalSystem extends GrouperExternalSystem {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.remedyConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.remedyConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myConnector";
  }
  
  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    List<String> errors = new ArrayList<>();
    Long testFakeGroupId = 132L;
    // try to retrieve a fake group and if it's 200, it's all good
    try {
      GrouperRemedyGroup remedyGroup = GrouperRemedyApiCommands.retrieveRemedyGroup(this.getConfigId(), testFakeGroupId);
    } catch (Exception e) {
      errors.add("Could not connect with remedy successfully "+GrouperUtil.escapeHtml(e.getMessage(), true));
    }
    
    return errors;
  }

  
}
