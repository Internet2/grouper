package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class CustomUiEngineTest extends GrouperTest {

  
  public static void main(String[] args) {
    TestRunner.run(new CustomUiEngineTest("testCreateCustomUiConfigFromLegacyAttributes"));
  }

  public CustomUiEngineTest() {
    super();
  }

  public CustomUiEngineTest(String name) {
    super(name);
  }
  
  public void testLoadRules() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group o365twoStepSelfEnrolled = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:o365twoStep:o365twoStepSelfEnrolled").save();
    
    Group o365twoStepRequiredToEnroll = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:o365twoStep:o365twoStepRequiredToEnroll").save();

    // SubjectTestHelper.SUBJ0 manager
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
    
    // SubjectTestHelper.SUBJ1 can enroll and not enrolled
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.OPTIN);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.OPTOUT);
    
    // SubjectTestHelper.SUBJ2 can enroll and enrolled
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.OPTIN);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.OPTOUT);
    o365twoStepSelfEnrolled.addMember(SubjectTestHelper.SUBJ2);
    
    // SubjectTestHelper.SUBJ3 is required to enroll
    o365twoStepRequiredToEnroll.addMember(SubjectTestHelper.SUBJ3);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.groupUUIDOrName", o365twoStepSelfEnrolled.getId());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.numberOfQueries", "4");
    
    // setup the manager
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.userQueryType", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.label", "Allowed to manage");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.variableToAssign", "cu_o365twoStepAllowedToManage");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.fieldNames", "updaters,readers");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.order", "100");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.forLoggedInUser", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.0.groupName", o365twoStepSelfEnrolled.getName());
    
    // allowed to see button?
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.userQueryType", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.label", "Can enroll and unenroll");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.variableToAssign", "cu_o365twoStepCanEnrollUnenroll");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.fieldNames", "optins,optouts");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.order", "30");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.forLoggedInUser", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.1.groupName", o365twoStepSelfEnrolled.getName());
    
    // self enrolled?
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.userQueryType", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.label", "Self enrolled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.variableToAssign", "cu_o365twoStepSelfEnrolled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.fieldNames", "members");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.order", "20");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.forLoggedInUser", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.2.groupName", o365twoStepSelfEnrolled.getName());
    
    // required to enroll
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.userQueryType", "grouper");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.label", "Required to enroll");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.variableToAssign", "cu_o365twoStepRequiredToEnroll");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.fieldNames", "members");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.order", "40");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.forLoggedInUser", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuQuery.3.groupName", o365twoStepRequiredToEnroll.getName());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.numberOfTextConfigs", "6");

    String header = "O365 TwoStep";
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.0.textType", "header");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.0.defaultText", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.0.text", header);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.1.textType", "canAssignVariables");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.1.index", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.1.text", "${cu_o365twoStepAllowedToManage}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.1.endIfMatches", "true");
      
    String instructions1notAllowedToEnroll = "You are not allowed to enroll";
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.2.textType", "instructions1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.2.index", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.2.defaultText", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.2.text", instructions1notAllowedToEnroll);
      
    String instructions1requiredToEnroll = "You are required to enroll";
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.3.textType", "instructions1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.3.index", "10");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.3.script", "${cu_o365twoStepRequiredToEnroll}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.3.text", instructions1requiredToEnroll);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.3.endIfMatches", "true");

    String instructions1canEnrollAndEnrolled = "You are enrolled";
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.4.textType", "instructions1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.4.index", "20");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.4.script", "${cu_o365twoStepCanEnrollUnenroll && cu_o365twoStepSelfEnrolled}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.4.text", instructions1canEnrollAndEnrolled);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.4.endIfMatches", "false");

    String instructions1canEnrollAndNotEnrolled = "You are allowed to enroll";
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.5.textType", "instructions1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.5.index", "30");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.5.script", "${cu_o365twoStepCanEnrollUnenroll && !cu_o365twoStepSelfEnrolled}");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.5.text", instructions1canEnrollAndNotEnrolled);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.test.cuTextConfig.5.endIfMatches", "false");
    
    CustomUiEngine customUiEngine = null;
    
    // ############## subj0 using the app for themself
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ0);

    String result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("true", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1canEnrollAndNotEnrolled, result);
    
    // ############## subj0 using the app for subj1
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1);

    result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("true", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1canEnrollAndNotEnrolled, result);
    
    grouperSession.stop();
    
    // ############## subj1 using the app for subj1
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ1);

    result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("false", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1canEnrollAndNotEnrolled, result);
    
    // ############## subj2 using the app for subj2
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ2);

    result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("false", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1canEnrollAndEnrolled, result);
    
    // ############## subj3 using the app for subj3
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ3);

    result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("false", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1requiredToEnroll, result);
    
    // ############## subj4 using the app for subj4
    customUiEngine = new CustomUiEngine();
    customUiEngine.processGroup(o365twoStepSelfEnrolled, SubjectTestHelper.SUBJ4, SubjectTestHelper.SUBJ4);

    result = customUiEngine.findBestText(CustomUiTextType.header, null);
    assertEquals(header, result);

    result = customUiEngine.findBestText(CustomUiTextType.canAssignVariables, null);
    assertEquals("false", result);
    
    result = customUiEngine.findBestText(CustomUiTextType.instructions1, null);
    assertEquals(result, instructions1notAllowedToEnroll, result);
    
    grouperSession.stop();
      
  }
  
  public void testCreateCustomUiConfigFromLegacyAttributes() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group o365twoStepSelfEnrolled = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:o365twoStep:o365twoStepSelfEnrolled").save();

    Group o365twoStepRequiredToEnroll = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:o365twoStep:o365twoStepRequiredToEnroll").save();

    // SubjectTestHelper.SUBJ0 manager
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
    
    // SubjectTestHelper.SUBJ1 can enroll and not enrolled
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.OPTIN);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.OPTOUT);
    
    // SubjectTestHelper.SUBJ2 can enroll and enrolled
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.OPTIN);
    o365twoStepSelfEnrolled.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.OPTOUT);
    o365twoStepSelfEnrolled.addMember(SubjectTestHelper.SUBJ2);
    
    // SubjectTestHelper.SUBJ3 is required to enroll
    o365twoStepRequiredToEnroll.addMember(SubjectTestHelper.SUBJ3);
    
    AttributeAssignResult attributeAssignResult = o365twoStepSelfEnrolled.getAttributeDelegate()
        .assignAttribute(CustomUiAttributeNames.retrieveAttributeDefNameMarker());
    AttributeAssign attributeAssignMarker = attributeAssignResult.getAttributeAssign();
    
    {
      // setup the manager
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean = new CustomUiUserQueryConfigBean();
      customUiUserQueryConfigBean.setLabel("Allowed to manage");
      customUiUserQueryConfigBean.setUserQueryType("grouper");
      customUiUserQueryConfigBean.setVariableToAssign("cu_o365twoStepAllowedToManage");
      customUiUserQueryConfigBean.setVariableType("boolean");
      customUiUserQueryConfigBean.setGroupName(o365twoStepSelfEnrolled.getName());
      customUiUserQueryConfigBean.setFieldNames("updaters,readers");
      customUiUserQueryConfigBean.setOrder(100);
      customUiUserQueryConfigBean.setForLoggedInUser(true);
      String json = GrouperUtil.jsonConvertTo(customUiUserQueryConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameUserQueryConfigBeans().getName(), json);
    }
    
    {
      // allowed to see button?
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean = new CustomUiUserQueryConfigBean();
      customUiUserQueryConfigBean.setLabel("Can enroll and unenroll");
      customUiUserQueryConfigBean.setUserQueryType("grouper");
      customUiUserQueryConfigBean.setVariableToAssign("cu_o365twoStepCanEnrollUnenroll");
      customUiUserQueryConfigBean.setVariableType("boolean");
      customUiUserQueryConfigBean.setGroupName(o365twoStepSelfEnrolled.getName());
      customUiUserQueryConfigBean.setFieldNames("optins,optouts");
      customUiUserQueryConfigBean.setOrder(30);
      customUiUserQueryConfigBean.setForLoggedInUser(true);
      String json = GrouperUtil.jsonConvertTo(customUiUserQueryConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameUserQueryConfigBeans().getName(), json);

    }
    
    {
      // self enrolled?
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean = new CustomUiUserQueryConfigBean();
      customUiUserQueryConfigBean.setLabel("Self enrolled");
      customUiUserQueryConfigBean.setUserQueryType("grouper");
      customUiUserQueryConfigBean.setVariableToAssign("cu_o365twoStepSelfEnrolled");
      customUiUserQueryConfigBean.setVariableType("boolean");
      customUiUserQueryConfigBean.setGroupName(o365twoStepSelfEnrolled.getName());
      customUiUserQueryConfigBean.setFieldNames("members");
      customUiUserQueryConfigBean.setOrder(20);
      customUiUserQueryConfigBean.setForLoggedInUser(true);
      String json = GrouperUtil.jsonConvertTo(customUiUserQueryConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameUserQueryConfigBeans().getName(), json);
    }

    {
      // required to enroll
      CustomUiUserQueryConfigBean customUiUserQueryConfigBean = new CustomUiUserQueryConfigBean();
      customUiUserQueryConfigBean.setLabel("Required to enroll");
      customUiUserQueryConfigBean.setUserQueryType("grouper");
      customUiUserQueryConfigBean.setVariableToAssign("cu_o365twoStepRequiredToEnroll");
      customUiUserQueryConfigBean.setVariableType("boolean");
      customUiUserQueryConfigBean.setGroupName(o365twoStepRequiredToEnroll.getName());
      customUiUserQueryConfigBean.setFieldNames("members");
      customUiUserQueryConfigBean.setOrder(40);
      customUiUserQueryConfigBean.setForLoggedInUser(true);
      String json = GrouperUtil.jsonConvertTo(customUiUserQueryConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameUserQueryConfigBeans().getName(), json);
      
    }

    String header = "O365 TwoStep";
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.header.name());
      customUiTextConfigBean.setDefaultText(true);
      customUiTextConfigBean.setText(header);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);

    }
    
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.canAssignVariables.name());
      customUiTextConfigBean.setIndex(0);
      customUiTextConfigBean.setText("${cu_o365twoStepAllowedToManage}");
      customUiTextConfigBean.setEndIfMatches(true);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);

    }
    String instructions1notAllowedToEnroll = "You are not allowed to enroll";
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
      customUiTextConfigBean.setIndex(0);
      customUiTextConfigBean.setDefaultText(true);
      customUiTextConfigBean.setText(instructions1notAllowedToEnroll);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);
    }

    String instructions1requiredToEnroll = "You are required to enroll";
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
      customUiTextConfigBean.setIndex(10);
      customUiTextConfigBean.setScript("${cu_o365twoStepRequiredToEnroll}");
      customUiTextConfigBean.setText(instructions1requiredToEnroll);
      customUiTextConfigBean.setEndIfMatches(true);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);
    }
    String instructions1canEnrollAndEnrolled = "You are enrolled";
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
      customUiTextConfigBean.setIndex(20);
      customUiTextConfigBean.setScript("${cu_o365twoStepCanEnrollUnenroll && cu_o365twoStepSelfEnrolled}");
      customUiTextConfigBean.setText(instructions1canEnrollAndEnrolled);
      customUiTextConfigBean.setEndIfMatches(false);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);
    }
    String instructions1canEnrollAndNotEnrolled = "You are allowed to enroll";
    {
      CustomUiTextConfigBean customUiTextConfigBean = new CustomUiTextConfigBean();
      customUiTextConfigBean.setCustomUiTextType(CustomUiTextType.instructions1.name());
      customUiTextConfigBean.setIndex(30);
      customUiTextConfigBean.setScript("${cu_o365twoStepCanEnrollUnenroll && !cu_o365twoStepSelfEnrolled}");
      customUiTextConfigBean.setText(instructions1canEnrollAndNotEnrolled);
      customUiTextConfigBean.setEndIfMatches(false);
      String json = GrouperUtil.jsonConvertTo(customUiTextConfigBean, false); 
      attributeAssignMarker.getAttributeValueDelegate().addValue(
          CustomUiAttributeNames.retrieveAttributeDefNameTextConfigBeans().getName(), json);
    }

    // migrate from legacy attributes to new custom ui config
    new CustomUiEngine().createCustomUiConfig(o365twoStepSelfEnrolled, "newTestConfig", true);
    
    //Then
    CustomUiConfig customUiConfigBean = new CustomUiEngine().retrieveCustomUiConfigBean(o365twoStepSelfEnrolled);
    
    assertEquals(true, customUiConfigBean.isEnabled());
    assertEquals(o365twoStepSelfEnrolled.getId(), customUiConfigBean.getGroupUUIDOrName());
    assertEquals(4, customUiConfigBean.getCustomUiUserQueryConfigBeans().size());
    assertEquals(6, customUiConfigBean.getCustomUiTextConfigBeans().size());
    
    assertEquals(0, o365twoStepSelfEnrolled.getAttributeDelegate().retrieveAssignments(CustomUiAttributeNames.retrieveAttributeDefNameMarker()).size());
    
  }

}
