package edu.internet2.middleware.grouper.app.remedy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleApiCommands;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleGroup;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceApiCommands;
import edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace.GrouperDigitalMarketplaceGroup;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class RemedyDigitalMarketplaceGrouperExternalSystem extends GrouperExternalSystem {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.remedyDigitalMarketplaceConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.remedyDigitalMarketplaceConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myConnector";
  }
  
  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    List<String> errors = new ArrayList<>();
    String testFakeGroupId = "testFakeGroupId";
    // try to retrieve a fake group and if it's 200, it's all good
    try {
      GrouperDigitalMarketplaceGroup digitalMarketplaceGroup = GrouperDigitalMarketplaceApiCommands.retrieveDigitalMarketplaceGroup(this.getConfigId(), testFakeGroupId);
    } catch (Exception e) {
      errors.add("Could not connect with remedy digital marketplace successfully "+GrouperUtil.escapeHtml(e.getMessage(), true));
    }
    
    return errors;
  }
  
}