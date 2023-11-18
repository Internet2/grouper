package edu.internet2.middleware.grouper.app.workflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;

public class GrouperWorkflowInstanceValidatorTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }
  
  public void testValidations() {
    
    // given
    Map<GrouperWorkflowConfigParam, String> paramNamesValues = new HashMap<GrouperWorkflowConfigParam, String>();
    GrouperWorkflowConfigParam param = new GrouperWorkflowConfigParam();
    param.setEditableInStates(Arrays.asList("initiate"));
    param.setRequired(true);
    param.setParamName("paramName");
    paramNamesValues.put(param, null);
    
    // when
    List<String> errors = new GrouperWorkflowInstanceValidator().validateFormValues(paramNamesValues, "initiate");
    
    // then
    List<String> expected = Arrays.asList("Field paramName is required");
    assertEquals(expected, errors);
    
  }
  
}
