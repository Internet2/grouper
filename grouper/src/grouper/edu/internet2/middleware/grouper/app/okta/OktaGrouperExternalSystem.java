package edu.internet2.middleware.grouper.app.okta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class OktaGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.oktaConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.oktaConnector)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myOkta";
  }

  @Override
  public void validatePreSave(boolean isInsert, boolean fromUi,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);
    
  }

  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    List<String> errors = new ArrayList<>();
    String testFakeGroupId = "testFakeGroupId";
    // try to retrieve a fake group and if it's 200 or 404, it's all good
    try {
      GrouperOktaGroup oktaGroup = GrouperOktaApiCommands.retrieveOktaGroup(this.getConfigId(), testFakeGroupId, false, false);
    } catch (Exception e) {
      errors.add("Could not connect with okta external system successfully "+GrouperUtil.escapeHtml(e.getMessage(), true));
    }
    
    return errors;
  }

}
